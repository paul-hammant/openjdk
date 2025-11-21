---
layout: post
title: "DSL Block Feature for Java: Implementation and Lessons Learned"
date: 2025-02-24 09:00:00
categories: [java, language-features, compilers]
tags: [java, dsl, openjdk, javac, compiler]
comments: true
---

A couple weeks ago, I [proposed a method-centric DSL feature for Java](https://paulhammant.com/2025/02/17/another-feature-for-the-java-language/). Well, I went ahead and implemented it in OpenJDK to see how it would actually work. Here's what I learned.

## What to Call This Feature?

Before diving in, let's clear up some terminology confusion. Someone suggested this is "tail recursion" - **it's not**. There's no recursion happening at all.

The correct computer science terms for this pattern are:

### Established Terms from Other Languages:

- **Scope function** (Kotlin terminology) - a function that creates a scope for operations on an object
- **Block with implicit receiver** (Smalltalk/Ruby terminology) - a code block where method calls implicitly target a receiver object
- **Trailing closure/lambda** (Swift terminology) - a closure that appears after the method call
- **Let expression** (functional programming) - binding a value to use in a scope, though ours is imperative

### Terms Emphasizing the "After" Aspect:

You're right that the temporal aspect matters - the block comes *after* the method invocation. Good descriptive terms include:

- **Post-invocation block** - emphasizes it executes after the method returns
- **Trailing block** - following Swift's "trailing closure" terminology
- **Method closure** - a closure attached to a method call
- **Suffix block** - the block is syntactically a suffix to the call
- **Post-method block** - clear and descriptive
- **Continuation block** - though "continuation" has other meanings in CS

**Swift's "trailing closure"** is probably the most established precedent:

```swift
// Swift trailing closure
array.map() { item in
    return item * 2
}
```

Our Java version would be called a **"trailing block"** or **"post-invocation block"** by this nomenclature.

### Closest Existing Features:

- **Kotlin's `apply`/`also`** - scope functions with lambda receivers
- **Ruby's block with `instance_eval`** - evaluating a block in an object's context
- **Groovy's DSL builder blocks** - builder pattern with closure delegation
- **Smalltalk's cascade operator** - chaining messages to the same receiver (though that's `object msg1; msg2; msg3` syntax)
- **Swift's trailing closures** - closures that appear after function arguments

Our implementation is essentially a **trailing block** or **scope function** where the method's return value becomes the implicit target for all unqualified method calls within the block.

### What It's NOT:

**Tail recursion**, by contrast, is when a function's last action is to call itself (or another function), allowing stack optimization:

```java
// This is tail recursion (not our feature!)
int factorial(int n, int acc) {
    if (n == 0) return acc;
    return factorial(n - 1, n * acc); // tail call
}
```

Completely unrelated - no recursion happens in our feature.

## The Feature, Refresher

The idea was to allow this syntax:

```java
Outer outr = outer() {
    System.out.println("Outer: " + this);
    middle() {
        System.out.println("Middle: " + this);
        inner() {
            System.out.println("Inner: " + this);
        }
    }
};
```

Instead of the verbose builder pattern:

```java
Outer outr = outer();
outr.middle(new Runnable() {
    public void run() {
        Middle m = middle();
        m.inner(new Runnable() {
            public void run() {
                Inner i = inner();
                // ...
            }
        });
    }
});
```

## How I Implemented It

The implementation touches four main parts of the javac compiler:

### 1. Parser Changes

Modified `JavacParser.java` to recognize the pattern `method() { ... }`:

```java
JCExpression arguments(List<JCExpression> typeArgs, JCExpression t) {
    int pos = token.pos;
    List<JCExpression> args = arguments();

    // Check for DSL block syntax: method() { ... }
    if (token.kind == LBRACE && isMode(EXPR)) {
        JCBlock body = block();
        JCExpression dslInvocation = F.at(pos).DslBlockInvocation(typeArgs, t, args, body);
        return toP(dslInvocation);
    }

    // Regular method invocation
    JCExpression mi = F.at(pos).Apply(typeArgs, t, args);
    return toP(mi);
}
```

The parser now looks ahead after method arguments and if it sees a `{`, it creates a `JCDslBlockInvocation` node instead of a regular `JCMethodInvocation`.

### 2. AST Node Addition

Added a new AST node type to `JCTree.java`:

```java
public static class JCDslBlockInvocation extends JCExpression {
    public List<JCExpression> typeargs;
    public JCExpression meth;
    public List<JCExpression> args;
    public JCBlock body;

    // ... visitor methods
}
```

This represents the entire DSL block construct in the abstract syntax tree.

### 3. Type Checking

Added attribution logic in `Attr.java`:

```java
public void visitDslBlockInvocation(JCDslBlockInvocation tree) {
    // Attribute the method call
    Type mtype = attribTree(tree.meth, localEnv, ...);

    // Attribute arguments
    List<Type> argtypes = attribArgs(...);

    // Resolve the method
    Type restype = rs.resolveQualifiedMethod(...);

    // Attribute the block body
    attribStat(tree.body, localEnv);

    // Result type is the method's return type
    result = tree.type = restype;
}
```

The type checker resolves the method, checks the arguments, and ensures the block is well-typed.

### 4. Desugaring

The magic happens in `Lower.java`, which transforms DSL blocks into regular Java:

```java
public void visitDslBlockInvocation(JCDslBlockInvocation tree) {
    // Desugar to:
    // 1. Type temp = method(args);
    // 2. new Runnable() { public void run() { body } }.run();
    // 3. result = temp;

    VarSymbol tmpVar = makeSyntheticVar(FINAL, "dsl$temp", tree.type, ...);
    JCVariableDecl tmpDecl = make.VarDef(tmpVar, make.Apply(...));

    JCClassDecl runnableClass = makeAnonymousRunnable(tree.pos, transformedBody);
    JCNewClass newRunnable = make.NewClass(..., runnableClass);
    JCMethodInvocation runCall = make.Apply(..., make.Select(newRunnable, "run"), ...);

    // Combine into expression
    this.result = make.LetExpr(tmpDecl, ...);
}
```

So this code:

```java
Builder b = createBuilder() {
    setValue(42);
};
```

Gets desugared to approximately:

```java
Builder dsl$temp = createBuilder();
new Runnable() {
    public void run() {
        setValue(42);
    }
}.run();
Builder b = dsl$temp;
```

This is essentially a **let expression** in imperative form: bind the method result to a temporary, execute a block that uses it, then return the temporary.

## What Works Well

### ✅ Builder Patterns

This is the killer use case. Building complex object graphs becomes much cleaner:

```java
Config config = createConfig("myapp") {
    setPort(8080);
    setThreads(4);
    database() {
        setUrl("jdbc:postgresql://localhost/db");
        setUser("admin");
    }
};
```

### ✅ Nested Structures

Three-level (or deeper) nesting works beautifully:

```java
Outer outer = outer() {
    middle() {
        inner() {
            setValue(100);
        }
    }
};
```

### ✅ Generics

Generic methods work with full type inference:

```java
GenericBuilder<String> sb = createBuilder("initial") {
    setValue(getValue() + " more");
};
```

### ✅ Method Overloading

Overload resolution happens before the block is considered:

```java
Config c1 = config("name") { /* ... */ };
Config c2 = config("name", 8080) { /* ... */ };
```

## What Doesn't Work (And Why)

### ❌ The 'this' Reference Gotcha

This is the **biggest limitation** I discovered. The `this` keyword does NOT refer to the returned object. It still refers to the enclosing class instance.

```java
class MyClass {
    int outerField = 999;

    void method() {
        Container c = createContainer() {
            setValue(10);           // OK - implicit call on Container
            int x = this.outerField; // 'this' is MyClass, not Container!
        };
    }
}
```

This is fundamentally different from Kotlin's `apply`:

```kotlin
val c = Container().apply {
    value = 10  // 'this' is Container
}
```

**Why?** Because the block executes in a `Runnable`, which runs in the caller's context. To make `this` refer to the returned object, I'd need much deeper changes to Java's scoping rules, possibly requiring:
- New bytecode instructions
- JVM-level support
- Changes to method dispatch

That's beyond a simple compiler transformation.

### ❌ Void Methods

You can't use DSL blocks on void methods:

```java
void doSomething() { }

// ERROR: Cannot use DSL block on void
doSomething() {
    // What would 'this' call methods on?
};
```

This makes sense - there's no return value to call methods on.

### ❌ Primitives and Arrays

```java
int getInt() { return 42; }
String[] getArray() { return new String[10]; }

// ERROR: Primitives have no methods
int x = getInt() { };

// ERROR: Not meaningful for arrays
String[] arr = getArray() { };
```

Primitives don't have methods, and while arrays technically do (like `clone()`), the pattern isn't useful.

### ❌ Type Visibility

Only methods visible on the **compile-time type** are accessible:

```java
Base b = createDerivedAsBase() {  // Returns Derived, typed as Base
    setBaseValue(10);     // OK
    setDerivedValue(20);  // ERROR - not visible on Base
};
```

This is standard Java type safety, but it might surprise users who expect the runtime type to matter.

## Edge Cases I Tested

### Null Returns

```java
Container c = returnsNull() {
    setValue(42);  // NullPointerException!
};
```

If the method returns null, calling methods in the block throws NPE. This is expected behavior but worth documenting.

### Exceptions

```java
Container c = createContainer() {
    setValue(-5);  // Throws IllegalArgumentException
};
```

Exceptions propagate normally from both the method call and the block body.

### Variable Capture

```java
final int x = 100;
Container c = createContainer() {
    setValue(x);  // Captures local variable
};
```

Follows lambda capture rules - variables must be final or effectively final.

### Control Flow

```java
Container c = createContainer() {
    for (int i = 0; i < 5; i++) {
        addValue(i);
    }
    if (condition) {
        setValue(100);
    }
};
```

All normal control flow works - loops, conditionals, break, continue.

## Testing Approach

I created 19 automated tests following OpenJDK's jtreg framework:

**Positive Tests:**
- Basic DSL blocks
- Nested blocks
- Method arguments
- Instance methods
- Chaining
- 7 edge case tests (null, exceptions, generics, inheritance, capture, 'this', control flow)

**Negative Tests:**
- Void methods
- Primitive returns
- Array returns

All tests include clear assertions and expected compiler error outputs for negative cases.

## Performance Considerations

The desugaring creates:
1. A temporary variable
2. An anonymous `Runnable` class
3. An object allocation for the Runnable
4. A method call to `run()`

For hot paths, this might add overhead. But for builder patterns (typically used during initialization), it should be negligible.

A future optimization could inline the block directly when the JIT detects the pattern, eliminating the Runnable allocation entirely.

## Comparison with Other Languages

### Kotlin's `apply`

```kotlin
val config = Config().apply {
    port = 8080      // 'this' is Config
    threads = 4
}
```

Kotlin changes the `this` context. Our Java version doesn't, which is a significant limitation but also simpler to implement.

### Groovy/Ruby DSLs

```groovy
outer {
    middle {
        inner {
            value = 100
        }
    }
}
```

These languages have first-class DSL support with changed contexts and implicit receivers. Java's static typing makes this harder.

### C# with Expression Bodies

```csharp
var config = CreateConfig() with {
    Port = 8080,
    Threads = 4
};
```

C#'s `with` expressions are similar but limited to records/immutable objects. Our approach works with any mutable builder.

## Should This Be in Java?

**Pros:**
- Much cleaner builder patterns
- Nested DSL syntax
- No new keywords
- Backward compatible

**Cons:**
- 'this' doesn't change (confusing)
- Limited to object return types
- Adds another way to do the same thing
- Implementation complexity

I'm genuinely unsure. The `this` limitation is significant. Without it, the feature feels incomplete compared to Kotlin's `apply`. But fixing it requires much deeper language changes.

Perhaps a middle ground would be a new keyword that makes the context explicit:

```java
Builder b = createBuilder() with {
    setValue(42);  // 'with' makes it clear this is special
};
```

Or explicit receiver syntax:

```java
Builder b = createBuilder() as it {
    it.setValue(42);  // Explicit receiver
};
```

## Try It Yourself

The implementation is on GitHub at [paul-hammant/openjdk](https://github.com/paul-hammant/openjdk) on branch `claude/code-java-feature-01SxebsnGf3v6Ct48FYaVGhr`.

To build and test:

```bash
git clone https://github.com/paul-hammant/openjdk
cd openjdk
git checkout claude/code-java-feature-01SxebsnGf3v6Ct48FYaVGhr

# Configure and build (requires build tools)
bash configure
make images

# Run tests
build/*/images/jdk/bin/javac \
    test/langtools/tools/javac/dslblocks/DslBlock01.java

build/*/images/jdk/bin/java \
    -cp test/langtools/tools/javac/dslblocks DslBlock01
```

## Conclusion

Implementing this feature taught me:

1. **Parser changes are straightforward** - Adding new syntax patterns is relatively simple
2. **Desugaring is powerful** - Most language features can be transformed to simpler constructs
3. **The 'this' problem is fundamental** - Without JVM/bytecode changes, we can't change scoping
4. **Edge cases matter** - Null, generics, inheritance all need careful thought
5. **Testing is essential** - Comprehensive tests reveal the feature's true boundaries

The feature works, but it's not quite what I envisioned. The `this` limitation means it's more "syntactic sugar for method sequences" than a true DSL feature.

Still, for builder patterns, it's significantly cleaner than current Java. Whether that's enough to justify the complexity is debatable.

What do you think? Would you use this feature despite the `this` limitation? Let me know in the comments.

## Related Posts

- [Another Feature for the Java Language](https://paulhammant.com/2025/02/17/another-feature-for-the-java-language/) - Original proposal
- [Builder Pattern in Java](https://paulhammant.com/2013/12/24/fluent-interfaces/) - Why we need better builder syntax

---

*All code and tests are available in the OpenJDK fork. The CAPABILITIES.md file has comprehensive documentation of what works and what doesn't.*
