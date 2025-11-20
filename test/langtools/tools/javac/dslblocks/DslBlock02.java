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
 * @summary Test nested DSL block invocations
 * @run main DslBlock02
 */

import java.util.*;

public class DslBlock02 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Outer {
        List<Middle> middles = new ArrayList<>();

        Middle middle() {
            Middle m = new Middle();
            middles.add(m);
            return m;
        }
    }

    static class Middle {
        List<Inner> inners = new ArrayList<>();

        Inner inner() {
            Inner i = new Inner();
            inners.add(i);
            return i;
        }
    }

    static class Inner {
        int value = 0;

        void setValue(int v) {
            this.value = v;
        }
    }

    static Outer outer() {
        return new Outer();
    }

    public static void main(String[] args) {
        // Test nested DSL blocks
        Outer o = outer() {
            middle() {
                inner() {
                    setValue(100);
                }
                inner() {
                    setValue(200);
                }
            }
            middle() {
                inner() {
                    setValue(300);
                }
            }
        };

        assertTrue(o.middles.size() == 2, "Expected 2 middle elements");
        assertTrue(o.middles.get(0).inners.size() == 2, "Expected 2 inners in first middle");
        assertTrue(o.middles.get(1).inners.size() == 1, "Expected 1 inner in second middle");
        assertTrue(o.middles.get(0).inners.get(0).value == 100, "Expected first inner value to be 100");
        assertTrue(o.middles.get(0).inners.get(1).value == 200, "Expected second inner value to be 200");
        assertTrue(o.middles.get(1).inners.get(0).value == 300, "Expected third inner value to be 300");

        System.out.println("All tests passed!");
    }
}
