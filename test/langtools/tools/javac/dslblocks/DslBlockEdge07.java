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
 * @summary Test DSL block edge case: return statements and control flow
 * @run main DslBlockEdge07
 */

public class DslBlockEdge07 {

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

        void conditional(boolean flag) {
            if (flag) {
                value = 100;
            } else {
                value = 200;
            }
        }
    }

    static Container createContainer() {
        return new Container();
    }

    public static void main(String[] args) {
        // Test 1: Control flow within the DSL block
        Container c1 = createContainer() {
            setValue(10);
            if (value == 10) {
                setValue(20);
            }
        };
        assertTrue(c1.value == 20, "Expected value 20");

        // Test 2: Early return in enclosing method doesn't affect DSL block structure
        Container c2 = methodWithReturn(true);
        assertTrue(c2.value == 100, "Expected value 100");

        // Test 3: Loops in DSL block
        Container c3 = createContainer() {
            for (int i = 0; i < 5; i++) {
                setValue(value + 1);
            }
        };
        assertTrue(c3.value == 5, "Expected value 5");

        // Test 4: Break and continue work normally in DSL blocks
        Container c4 = createContainer() {
            for (int i = 0; i < 10; i++) {
                if (i == 5) break;
                setValue(value + 1);
            }
        };
        assertTrue(c4.value == 5, "Expected value 5");

        System.out.println("All control flow edge case tests passed!");
    }

    static Container methodWithReturn(boolean flag) {
        if (flag) {
            return createContainer() {
                setValue(100);
            };
        }
        return createContainer() {
            setValue(200);
        };
    }
}
