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
 * @summary Test basic DSL block invocation syntax
 * @run main DslBlock01
 */

public class DslBlock01 {

    static int counter = 0;

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Builder {
        int value = 0;

        Builder setValue(int v) {
            this.value = v;
            return this;
        }

        int getValue() {
            return value;
        }
    }

    static Builder createBuilder() {
        counter++;
        return new Builder();
    }

    public static void main(String[] args) {
        // Test 1: Simple DSL block invocation
        Builder b = createBuilder() {
            setValue(42);
        };

        assertTrue(b.getValue() == 42, "Expected value to be 42");
        assertTrue(counter == 1, "Expected createBuilder to be called once");

        // Test 2: DSL block with multiple statements
        Builder b2 = createBuilder() {
            setValue(10);
            setValue(getValue() + 5);
        };

        assertTrue(b2.getValue() == 15, "Expected value to be 15");
        assertTrue(counter == 2, "Expected createBuilder to be called twice");

        // Test 3: Empty DSL block
        Builder b3 = createBuilder() {
        };

        assertTrue(b3.getValue() == 0, "Expected value to be 0");
        assertTrue(counter == 3, "Expected createBuilder to be called three times");

        System.out.println("All tests passed!");
    }
}
