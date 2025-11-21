/*
 * Example demonstrating the DSL block feature for Java
 * Based on: https://paulhammant.com/2025/02/17/another-feature-for-the-java-language/
 *
 * This feature allows method-centric DSL syntax with nested blocks
 * where each block has implicit 'this' references to the returned object.
 */

import java.util.*;

public class DslBlockExample {
    public static void main(String[] args) {
        // Example from the blog post
        Outer outr = outer() {
            System.out.println("Outer: " + this);
            middle() {
                System.out.println("Middle: " + this);
                inner() {
                    System.out.println("Inner: " + this);
                }
            }
        };
    }

    // DSL builder methods
    public static Outer outer() {
        return new Outer();
    }

    // Supporting classes
    static class Outer {
        private List<Middle> middles = new ArrayList<>();

        public Middle middle() {
            Middle middle = new Middle();
            middles.add(middle);
            return middle;
        }

        @Override
        public String toString() {
            return "Outer@" + Integer.toHexString(hashCode());
        }
    }

    static class Middle {
        private List<Inner> inners = new ArrayList<>();

        public Inner inner() {
            Inner inner = new Inner();
            inners.add(inner);
            return inner;
        }

        @Override
        public String toString() {
            return "Middle@" + Integer.toHexString(hashCode());
        }
    }

    static class Inner {
        @Override
        public String toString() {
            return "Inner@" + Integer.toHexString(hashCode());
        }
    }
}
