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
 * @summary Test DSL block edge case: null returns
 * @run main DslBlockEdge01
 */

public class DslBlockEdge01 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Container {
        int value = 0;

        void setValue(int v) {
            this.value = v;
        }
    }

    static Container returnsNull() {
        return null;
    }

    static Container returnsNonNull() {
        return new Container();
    }

    public static void main(String[] args) {
        // Test 1: Method returns null - block should still execute
        // but calling methods on null will throw NPE
        boolean caught = false;
        try {
            Container c = returnsNull() {
                setValue(42);  // This will throw NPE
            };
        } catch (NullPointerException e) {
            caught = true;
        }
        assertTrue(caught, "Expected NPE when calling method on null");

        // Test 2: Non-null return with empty block works fine
        Container c2 = returnsNonNull() {
        };
        assertTrue(c2 != null, "Expected non-null result");
        assertTrue(c2.value == 0, "Expected default value");

        // Test 3: The DSL block returns the method's result, not a modified value
        Container c3 = returnsNonNull() {
            setValue(99);
        };
        assertTrue(c3.value == 99, "Expected value to be 99");

        System.out.println("All edge case tests passed!");
    }
}
