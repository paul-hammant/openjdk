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
 * @summary Test DSL block edge case: 'this' reference behavior
 * @run main DslBlockEdge06
 */

public class DslBlockEdge06 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Counter {
        int count = 0;

        void increment() {
            count++;
        }

        Counter getThis() {
            return this;
        }
    }

    static Counter createCounter() {
        return new Counter();
    }

    int outerField = 999;

    public void testThisReference() {
        // IMPORTANT: In the DSL block, 'this' does NOT refer to the returned object
        // It still refers to the enclosing class instance
        Counter c = createCounter() {
            increment();  // Calls method on the returned Counter object

            // But 'this' still refers to DslBlockEdge06, not Counter
            int outerValue = this.outerField;  // 'this' is DslBlockEdge06
            assertTrue(outerValue == 999, "this refers to outer class");
        };

        assertTrue(c.count == 1, "Expected count to be 1");
    }

    public static void main(String[] args) {
        DslBlockEdge06 instance = new DslBlockEdge06();
        instance.testThisReference();

        // Test in static context - 'this' is not available
        Counter c2 = createCounter() {
            increment();
            // 'this' is not available in static context, just like regular code
        };
        assertTrue(c2.count == 1, "Expected count to be 1");

        System.out.println("All 'this' reference tests passed!");
    }
}
