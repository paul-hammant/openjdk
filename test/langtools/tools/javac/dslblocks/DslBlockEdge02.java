/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8888888
 * @summary Test DSL block edge case: exceptions
 * @run main DslBlockEdge02
 */

public class DslBlockEdge02 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Container {
        int value = 0;

        void setValue(int v) {
            if (v < 0) {
                throw new IllegalArgumentException("Value must be positive");
            }
            this.value = v;
        }
    }

    static Container createContainer() {
        return new Container();
    }

    static Container throwingMethod() {
        throw new RuntimeException("Method throws");
    }

    public static void main(String[] args) {
        // Test 1: Exception in the method call itself
        boolean caught1 = false;
        try {
            Container c = throwingMethod() {
                setValue(10);
            };
        } catch (RuntimeException e) {
            caught1 = e.getMessage().equals("Method throws");
        }
        assertTrue(caught1, "Expected exception from method");

        // Test 2: Exception in the block body
        boolean caught2 = false;
        try {
            Container c = createContainer() {
                setValue(-5);  // Will throw IllegalArgumentException
            };
        } catch (IllegalArgumentException e) {
            caught2 = e.getMessage().contains("positive");
        }
        assertTrue(caught2, "Expected exception from block");

        // Test 3: Exception doesn't prevent method from being called first
        // (method is called, result is stored, then block executes)
        boolean caught3 = false;
        try {
            Container c = createContainer() {
                throw new RuntimeException("Block throws");
            };
        } catch (RuntimeException e) {
            caught3 = e.getMessage().equals("Block throws");
        }
        assertTrue(caught3, "Expected exception from block");

        System.out.println("All exception edge case tests passed!");
    }
}
