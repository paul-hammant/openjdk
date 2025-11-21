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
 * @summary Test DSL block edge case: generics
 * @run main DslBlockEdge03
 */

import java.util.*;

public class DslBlockEdge03 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class GenericBuilder<T> {
        T value;

        GenericBuilder<T> setValue(T v) {
            this.value = v;
            return this;
        }

        T getValue() {
            return value;
        }
    }

    static <T> GenericBuilder<T> createBuilder(T initial) {
        GenericBuilder<T> b = new GenericBuilder<>();
        b.value = initial;
        return b;
    }

    static <T> GenericBuilder<T> createEmptyBuilder() {
        return new GenericBuilder<>();
    }

    public static void main(String[] args) {
        // Test 1: Generic method with type inference
        GenericBuilder<String> sb = createBuilder("Hello") {
            setValue(getValue() + " World");
        };
        assertTrue(sb.getValue().equals("Hello World"), "Expected 'Hello World'");

        // Test 2: Generic method with explicit type
        GenericBuilder<Integer> ib = DslBlockEdge03.<Integer>createEmptyBuilder() {
            setValue(42);
        };
        assertTrue(ib.getValue() == 42, "Expected 42");

        // Test 3: Generic builder with collections
        GenericBuilder<List<String>> lb = createEmptyBuilder() {
            setValue(new ArrayList<>());
            getValue().add("item1");
            getValue().add("item2");
        };
        assertTrue(lb.getValue().size() == 2, "Expected 2 items");
        assertTrue(lb.getValue().get(0).equals("item1"), "Expected 'item1'");

        System.out.println("All generic edge case tests passed!");
    }
}
