// Test1_Valid.java - Valid Java 1.2 code with various invocations
package com.test;

import java.util.Vector;
import java.io.*;

public class Test1_Valid {

    private int x;
    private String name;

    // Constructor
    public Test1_Valid(int x, String name) {
        this.x = x;
        this.name = name;
    }

    // Default constructor calling another constructor
    public Test1_Valid() {
        this(0, "default");
    }

    // Simple method
    public int getX() {
        return x;
    }

    // Method with invocations
    public void doStuff() {
        // Simple method call
        int val = getX();

        // Method call on object
        String s = name.toString();

        // Chained method call
        String trimmed = name.trim().toLowerCase();

        // Static-style method call
        int result = Math.abs(-42);

        // Constructor invocation
        Test1_Valid other = new Test1_Valid(10, "hello");

        // Constructor invocation with chained call
        String str = new String("test").trim();

        // Method call with multiple arguments
        System.out.println("Value: " + val);

        // Vector usage (Java 1.2 collections)
        Vector v = new Vector();
        v.addElement("item1");
        v.addElement("item2");
        int size = v.size();
    }

    // Method calling super
    public String toString() {
        return "Test1_Valid(" + x + ", " + name + ")";
    }

    // Static method
    public static void main(String[] args) {
        Test1_Valid t = new Test1_Valid(5, "main");
        t.doStuff();
        System.out.println(t.toString());
    }
}
