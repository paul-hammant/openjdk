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
 * @summary Test DSL block with method arguments
 * @run main DslBlock03
 */

public class DslBlock03 {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static class Config {
        String name;
        int port;

        void setName(String n) {
            this.name = n;
        }

        void setPort(int p) {
            this.port = p;
        }
    }

    static Config config(String defaultName) {
        Config c = new Config();
        c.name = defaultName;
        c.port = 8080;
        return c;
    }

    static Config config(String defaultName, int defaultPort) {
        Config c = new Config();
        c.name = defaultName;
        c.port = defaultPort;
        return c;
    }

    public static void main(String[] args) {
        // Test 1: DSL block with single argument
        Config c1 = config("myapp") {
            setPort(9090);
        };

        assertTrue(c1.name.equals("myapp"), "Expected name to be 'myapp'");
        assertTrue(c1.port == 9090, "Expected port to be 9090");

        // Test 2: DSL block with multiple arguments
        Config c2 = config("webapp", 3000) {
            setName("newapp");
        };

        assertTrue(c2.name.equals("newapp"), "Expected name to be 'newapp'");
        assertTrue(c2.port == 3000, "Expected port to be 3000");

        // Test 3: DSL block with no modifications
        Config c3 = config("default") {
        };

        assertTrue(c3.name.equals("default"), "Expected name to be 'default'");
        assertTrue(c3.port == 8080, "Expected port to be 8080");

        System.out.println("All tests passed!");
    }
}
