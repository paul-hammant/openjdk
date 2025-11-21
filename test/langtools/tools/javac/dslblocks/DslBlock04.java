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
 * @summary Test DSL block with instance methods
 * @run main DslBlock04
 */

public class DslBlock04 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class StringBuilder2 {
        private String content = "";

        StringBuilder2 append(String s) {
            content += s;
            return this;
        }

        String build() {
            return content;
        }
    }

    StringBuilder2 createBuilder() {
        return new StringBuilder2();
    }

    public void test() {
        // Test instance method with DSL block
        StringBuilder2 sb = createBuilder() {
            append("Hello");
            append(" ");
            append("World");
        };

        assertTrue(sb.build().equals("Hello World"), "Expected 'Hello World'");
    }

    public static void main(String[] args) {
        DslBlock04 instance = new DslBlock04();
        instance.test();
        System.out.println("All tests passed!");
    }
}
