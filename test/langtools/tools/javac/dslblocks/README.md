# DSL Block Invocation Feature

## Overview

This feature implements a method-centric DSL (Domain-Specific Language) syntax for Java, allowing nested block syntax similar to Ruby and Groovy. It enables more fluent and readable builder patterns.

## Syntax

The new syntax allows method invocations to be followed by a block:

```java
Outer outer = outer() {
    System.out.println("Outer: " + this);
    middle() {
        System.out.println("Middle: " + this);
        inner() {
            System.out.println("Inner: " + this);
        }
    }
};
```

## How It Works

The compiler transforms DSL block invocations as follows:

1. **Parsing**: The parser recognizes the pattern `method() { ... }` as a `JCDslBlockInvocation` AST node
2. **Type Checking**: The attribution phase resolves the method and type-checks the block
3. **Desugaring**: The Lower phase transforms the DSL block into:
   - A regular method call to get the returned object
   - An anonymous `Runnable` that executes the block
   - Invocation of the `Runnable`'s `run()` method

## Implementation Details

### Modified Files

1. **JCTree.java**: Added `JCDslBlockInvocation` AST node and `DSLBLOCKINVOCATION` tag
2. **TreeMaker.java**: Added `DslBlockInvocation()` factory method
3. **JavacParser.java**: Modified `arguments()` method to recognize block syntax after method calls
4. **Attr.java**: Added `visitDslBlockInvocation()` for type checking
5. **Lower.java**: Added `visitDslBlockInvocation()` for desugaring transformation

### Example Transformation

Source code:
```java
Outer outer = outer() {
    System.out.println("Outer: " + this);
};
```

Desugared to approximately:
```java
Outer temp = outer();
new Runnable() {
    public void run() {
        System.out.println("Outer: " + temp);
    }
}.run();
Outer outer = temp;
```

## Benefits

- More readable builder patterns
- Nested DSL syntax for configuration
- Implicit context management (the returned object becomes the implicit `this`)
- Compatible with existing Java code

## Reference

Based on the blog post: https://paulhammant.com/2025/02/17/another-feature-for-the-java-language/

## Testing

Run the example:
```bash
javac test/langtools/tools/javac/dslblocks/DslBlockExample.java
java -cp test/langtools/tools/javac/dslblocks DslBlockExample
```
