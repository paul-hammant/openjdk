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
 * @summary Test DSL block edge case: inheritance and polymorphism
 * @run main DslBlockEdge04
 */

public class DslBlockEdge04 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Base {
        int baseValue = 0;

        void setBaseValue(int v) {
            this.baseValue = v;
        }
    }

    static class Derived extends Base {
        int derivedValue = 0;

        void setDerivedValue(int v) {
            this.derivedValue = v;
        }
    }

    static Base createBase() {
        return new Base();
    }

    static Derived createDerived() {
        return new Derived();
    }

    static Base createDerivedAsBase() {
        return new Derived();
    }

    public static void main(String[] args) {
        // Test 1: DSL block on base class
        Base b = createBase() {
            setBaseValue(10);
        };
        assertTrue(b.baseValue == 10, "Expected base value 10");

        // Test 2: DSL block on derived class
        Derived d = createDerived() {
            setBaseValue(20);
            setDerivedValue(30);
        };
        assertTrue(d.baseValue == 20, "Expected base value 20");
        assertTrue(d.derivedValue == 30, "Expected derived value 30");

        // Test 3: Polymorphic return - runtime type is Derived but compile-time is Base
        Base b2 = createDerivedAsBase() {
            setBaseValue(40);
            // Cannot call setDerivedValue() here - not visible at compile time
        };
        assertTrue(b2.baseValue == 40, "Expected base value 40");
        assertTrue(b2 instanceof Derived, "Runtime type should be Derived");
        // The runtime object is Derived, but we can only access Base methods in the block

        System.out.println("All inheritance edge case tests passed!");
    }
}
