# DSL Block Invocation: Capabilities and Limitations

## What This Feature DOES Support ✅

### 1. Basic Builder Pattern
```java
Builder b = createBuilder() {
    setValue(42);
    setName("test");
};
```
**Test:** DslBlock01.java

### 2. Nested DSL Blocks
```java
Outer o = outer() {
    middle() {
        inner() {
            setValue(100);
        }
    }
};
```
**Test:** DslBlock02.java

### 3. Method Arguments
```java
Config c = config("myapp", 8080) {
    setName("webapp");
};
```
**Test:** DslBlock03.java

### 4. Instance and Static Methods
```java
// Static method
Builder b = StaticFactory.create() { ... };

// Instance method
Builder b = instance.createBuilder() { ... };
```
**Tests:** DslBlock01.java, DslBlock04.java

### 5. Generic Methods
```java
GenericBuilder<String> sb = createBuilder("initial") {
    setValue(getValue() + " more");
};
```
**Test:** DslBlockEdge03.java

### 6. Inheritance and Polymorphism
```java
Base b = createBase() {
    setBaseValue(10);
};

Derived d = createDerived() {
    setBaseValue(20);
    setDerivedValue(30);
};
```
**Test:** DslBlockEdge04.java
**Note:** Only methods visible on the compile-time type are accessible in the block.

### 7. Variable Capture
```java
final int x = 100;
Container c = createContainer() {
    setValue(x);  // Captures local variables
};
```
**Test:** DslBlockEdge05.java
**Note:** Same capture rules as lambdas - variables must be final or effectively final.

### 8. Control Flow in Blocks
```java
Container c = createContainer() {
    if (condition) {
        setValue(10);
    }
    for (int i = 0; i < 5; i++) {
        addValue(i);
    }
};
```
**Test:** DslBlockEdge07.java

### 9. Exception Handling
```java
Container c = createContainer() {
    setValue(10);  // May throw exception
};
```
**Test:** DslBlockEdge02.java
**Note:** Exceptions propagate normally from both the method call and the block.

### 10. Chaining DSL Blocks
```java
Node head = createNode() {
    setValue(1);
    setNext(createNode() {
        setValue(2);
    });
};
```
**Test:** DslBlock05.java

---

## What This Feature DOES NOT Support ❌

### 1. Void Methods
```java
void voidMethod() { }

// ERROR: Cannot use DSL block on void method
voidMethod() {
    // compilation error
};
```
**Test:** DslBlockNeg01.java
**Reason:** Void methods don't return an object, so there's nothing to call methods on.

### 2. Primitive Return Types
```java
int getInt() { return 42; }

// ERROR: Cannot use DSL block on primitives
int x = getInt() {
    // compilation error
};
```
**Test:** DslBlockNeg02.java
**Reason:** Primitives (int, boolean, double, etc.) don't have methods to call.

### 3. Array Return Types
```java
String[] getArray() { return new String[10]; }

// ERROR: Not meaningful for arrays
String[] arr = getArray() {
    // compilation error
};
```
**Test:** DslBlockNeg03.java
**Reason:** While arrays technically have methods (clone, etc.), this pattern isn't meaningful for arrays.

### 4. Changing Implicit 'this' Reference
```java
class Outer {
    int outerField = 999;

    void method() {
        Container c = createContainer() {
            // 'this' still refers to Outer, NOT Container!
            int x = this.outerField;  // Accesses Outer.outerField

            // To call Container methods, don't use 'this':
            setValue(10);  // Implicitly calls on returned Container
        };
    }
}
```
**Test:** DslBlockEdge06.java
**Reason:** This is a key limitation - the block doesn't create a new 'this' context like in Kotlin's apply/with.

### 5. Accessing Private Members of Returned Object
```java
class Container {
    private int secret = 0;
}

// Can only access public/visible methods
Container c = createContainer() {
    // Cannot access 'secret' field directly
    setSecret(10);  // Must use public methods
};
```
**Reason:** Normal Java access control applies. The block executes in the caller's context, not inside the returned object.

### 6. Modifying the Return Value Reference
```java
Container c = createContainer() {
    // The block CANNOT change which object is returned
    // It can only call methods on the returned object
};
// 'c' is always the object returned by createContainer()
```
**Reason:** The return value is determined by the method call, not by the block.

### 7. Return Statements in Block
```java
Container c = createContainer() {
    setValue(10);
    return something;  // ERROR: Cannot return from DSL block
};
```
**Reason:** The block is not a lambda or method - it's a statement sequence that executes for side effects.

---

## Key Behavioral Notes 📝

### Execution Order
```java
Container c = createMethod() {
    statement1();
    statement2();
};
```
**Order:**
1. `createMethod()` is called
2. Result is stored in temporary variable
3. Block executes with methods called on the result
4. Result is assigned to `c`

**Test:** DslBlockEdge02.java

### Null Handling
```java
Container c = returnsNull() {
    setValue(10);  // NullPointerException!
};
```
**Behavior:** If the method returns null, calling methods in the block throws NPE.
**Test:** DslBlockEdge01.java

### Type Safety
```java
Base b = createDerivedAsBase() {
    setBaseValue(10);     // OK - Base method
    setDerivedValue(20);  // ERROR - not visible on Base type
};
```
**Behavior:** Only methods visible on the compile-time type are accessible.
**Test:** DslBlockEdge04.java

### Method Overload Resolution
```java
Container c = create(42) {        // Calls create(int)
    setValue(10);
};

Container c2 = create("str") {    // Calls create(String)
    setValue(20);
};
```
**Behavior:** Normal overload resolution applies - the method is resolved before considering the block.
**Test:** DslBlock03.java

---

## Comparison with Similar Features

### vs. Java Anonymous Classes with Initializer
```java
// Anonymous class approach (verbose)
Container c = new Container() {
    {  // initializer block
        setValue(10);
    }
};

// DSL block approach (cleaner for builders)
Container c = createContainer() {
    setValue(10);
};
```

### vs. Kotlin's apply/also
**Kotlin:**
```kotlin
val c = Container().apply {
    value = 10  // 'this' is Container
}
```

**Java DSL Block:**
```java
Container c = createContainer() {
    setValue(10);  // implicit call on returned object
    // but 'this' is NOT Container
};
```
**Key difference:** Java doesn't change 'this' context.

### vs. Ruby/Groovy DSL
**Ruby:**
```ruby
outer do
  middle do
    inner do
      puts self  # 'self' is the inner object
    end
  end
end
```

**Java:**
```java
outer() {
    middle() {
        inner() {
            System.out.println(this);  // 'this' is NOT inner
        }
    }
}
```
**Key difference:** Java maintains enclosing 'this' reference.

---

## Use Cases

### ✅ Good Use Cases
1. **Builder patterns** - Configuring builders fluently
2. **Configuration DSLs** - Setting up complex configurations
3. **Test fixtures** - Creating test data structures
4. **Nested object graphs** - Building hierarchical structures

### ❌ Not Ideal Use Cases
1. **Simple method calls** - Use regular chaining instead
2. **Functional operations** - Use lambdas/streams instead
3. **Complex business logic** - Use regular methods
4. **When you need to change 'this'** - Can't do that in Java

---

## Summary

**This feature provides:**
- Syntactic sugar for builder patterns
- Cleaner nested DSL syntax
- Implicit method calls on returned objects

**This feature does NOT provide:**
- Changed 'this' context (like Kotlin's apply)
- Support for primitives or void
- New scoping rules
- Magic member access

The DSL block syntax is best viewed as **syntactic convenience for calling a sequence of methods on a newly created object**, not as a fundamental change to Java's scoping or context rules.
