// Test2_Valid.java - Interfaces, inheritance, inner classes (Java 1.2)
package com.test;

import java.io.Serializable;

public class Test2_Valid implements Serializable {

    // Interface declaration
    interface Callback {
        void onComplete(int result);
    }

    // Inner class
    static class Helper {
        public int compute(int a, int b) {
            return a + b;
        }

        public static Helper create() {
            return new Helper();
        }
    }

    // Inheritance
    static class SpecialHelper extends Helper {
        public SpecialHelper() {
            super();
        }

        public int compute(int a, int b) {
            int base = super.compute(a, b);
            return base * 2;
        }
    }

    // Method with various expression types
    public void exercise() {
        // Inner class constructor
        Helper h = new Helper();
        int sum = h.compute(3, 4);

        // Static method on inner class
        Helper h2 = Helper.create();

        // Instanceof check (not an invocation)
        boolean isHelper = h instanceof Helper;

        // Ternary with invocations
        int val = (sum > 5) ? h.compute(1, 2) : h.compute(3, 4);

        // Array creation (not a method/constructor invocation)
        int[] arr = new int[10];

        // Array of objects
        Helper[] helpers = new Helper[5];

        // Anonymous expressions
        String s = "hello".substring(1);

        // Nested invocations
        int nested = Math.max(h.compute(1, 2), h.compute(3, 4));
    }

    // Switch statement
    public String describe(int code) {
        String result;
        switch (code) {
            case 0:
                result = "zero";
                break;
            case 1:
                result = "one";
                break;
            default:
                result = String.valueOf(code);
                break;
        }
        return result;
    }

    // Try-catch-finally
    public void riskyOperation() {
        try {
            Integer.parseInt("not a number");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            System.out.println("done");
        }
    }

    // For and while loops with invocations
    public void loops() {
        Vector v = new Vector();
        for (int i = 0; i < 10; i++) {
            v.addElement(new Integer(i));
        }

        int idx = 0;
        while (idx < v.size()) {
            System.out.println(v.elementAt(idx));
            idx++;
        }
    }

    // Static initializer
    static {
        System.out.println("Static init");
    }
}

// Separate class in the same file (allowed in Java)
class AuxiliaryClass {
    public void helper() {
        System.out.println("aux");
    }
}
