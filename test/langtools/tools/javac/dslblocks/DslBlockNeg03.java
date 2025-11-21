/*
 * @test /nodynamiccopyright/
 * @bug 8888888
 * @summary Test that DSL block cannot be used on array return types
 * @compile/fail/ref=DslBlockNeg03.out -XDrawDiagnostics DslBlockNeg03.java
 */

public class DslBlockNeg03 {

    static String[] stringArray() {
        return new String[] {"a", "b"};
    }

    static int[] intArray() {
        return new int[] {1, 2, 3};
    }

    public static void main(String[] args) {
        // Error: Arrays don't have user-callable methods in this context
        String[] arr = stringArray() {
            // Arrays have clone() and some Object methods, but this pattern isn't meaningful
        };

        int[] nums = intArray() {
        };
    }
}
