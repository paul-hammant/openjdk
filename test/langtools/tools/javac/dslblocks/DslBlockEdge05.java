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
 * @summary Test DSL block edge case: variable capture
 * @run main DslBlockEdge05
 */

public class DslBlockEdge05 {

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

        void addValue(int v) {
            this.value += v;
        }
    }

    static Container createContainer() {
        return new Container();
    }

    public static void main(String[] args) {
        // Test 1: Capture local variable (effectively final)
        final int capturedValue = 100;
        Container c1 = createContainer() {
            setValue(capturedValue);
        };
        assertTrue(c1.value == 100, "Expected captured value 100");

        // Test 2: Capture effectively final variable
        int effectivelyFinal = 200;
        Container c2 = createContainer() {
            addValue(effectivelyFinal);
        };
        assertTrue(c2.value == 200, "Expected 200");

        // Test 3: Multiple variable captures
        final int x = 10;
        final int y = 20;
        Container c3 = createContainer() {
            setValue(x);
            addValue(y);
        };
        assertTrue(c3.value == 30, "Expected 30");

        // Test 4: Outer class field access (if in instance method)
        DslBlockEdge05 instance = new DslBlockEdge05();
        instance.testInstanceCapture();

        System.out.println("All variable capture edge case tests passed!");
    }

    int instanceField = 500;

    void testInstanceCapture() {
        // Can capture 'this' and access instance fields
        Container c = createContainer() {
            setValue(instanceField);
        };
        assertTrue(c.value == 500, "Expected instance field value 500");
    }
}
