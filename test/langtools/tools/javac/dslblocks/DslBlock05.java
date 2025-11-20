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
 * @summary Test DSL block chaining
 * @run main DslBlock05
 */

public class DslBlock05 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Node {
        int value;
        Node next;

        Node setValue(int v) {
            this.value = v;
            return this;
        }

        Node setNext(Node n) {
            this.next = n;
            return this;
        }
    }

    static Node createNode() {
        return new Node();
    }

    public static void main(String[] args) {
        // Test DSL block with chained method calls
        Node head = createNode() {
            setValue(1);
            setNext(createNode() {
                setValue(2);
                setNext(createNode() {
                    setValue(3);
                });
            });
        };

        assertTrue(head.value == 1, "Expected head value to be 1");
        assertTrue(head.next.value == 2, "Expected second value to be 2");
        assertTrue(head.next.next.value == 3, "Expected third value to be 3");
        assertTrue(head.next.next.next == null, "Expected end of chain");

        System.out.println("All tests passed!");
    }
}
