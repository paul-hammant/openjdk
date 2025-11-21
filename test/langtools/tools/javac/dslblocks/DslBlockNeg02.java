/*
 * @test /nodynamiccopyright/
 * @bug 8888888
 * @summary Test that DSL block cannot be used on primitive return types
 * @compile/fail/ref=DslBlockNeg02.out -XDrawDiagnostics DslBlockNeg02.java
 */

public class DslBlockNeg02 {

    static int primitiveInt() {
        return 42;
    }

    static boolean primitiveBoolean() {
        return true;
    }

    public static void main(String[] args) {
        // Error: Cannot use DSL block on primitive return type
        int x = primitiveInt() {
            // Primitives have no methods to call
        };

        // Error: Cannot use DSL block on boolean
        boolean b = primitiveBoolean() {
        };
    }
}
