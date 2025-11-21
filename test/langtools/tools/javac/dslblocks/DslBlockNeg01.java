/*
 * @test /nodynamiccopyright/
 * @bug 8888888
 * @summary Test that DSL block cannot be used on void methods
 * @compile/fail/ref=DslBlockNeg01.out -XDrawDiagnostics DslBlockNeg01.java
 */

public class DslBlockNeg01 {

    static void voidMethod() {
        System.out.println("void");
    }

    public static void main(String[] args) {
        // Error: Cannot use DSL block syntax on void method
        voidMethod() {
            System.out.println("This should fail");
        };
    }
}
