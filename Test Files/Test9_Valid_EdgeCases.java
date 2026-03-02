// Test9_Valid_EdgeCases.java - Edge cases in Java 1.2 syntax
public class Test9_Valid_EdgeCases {

    // strictfp modifier (new in Java 1.2)
    strictfp double compute(double a, double b) {
        return a / b;
    }

    // Multiple array dimensions
    int[][] matrix = new int[3][4];

    // Empty static initializer
    static { }

    // Instance initializer
    { System.out.println("instance init"); }

    // Array initializers
    int[] primes = {2, 3, 5, 7, 11};
    int[][] grid = {{1, 2}, {3, 4}};

    // Method returning array
    public int[] getArray() {
        return new int[] {1, 2, 3};
    }

    // Labeled statements
    public void labeledLoops() {
        outer:
        for (int i = 0; i < 10; i++) {
            inner:
            for (int j = 0; j < 10; j++) {
                if (i == j) continue outer;
                if (i + j > 15) break outer;
            }
        }
    }

    // Complex expressions
    public void complexExpressions() {
        int a = 1, b = 2, c = 3;

        // Ternary
        int max = (a > b) ? a : b;

        // Instanceof
        Object o = "hello";
        boolean isStr = o instanceof String;

        // Shift operators
        int shifted = a << 2;
        int rshift = a >> 1;
        int urshift = a >>> 1;

        // Bitwise
        int and = a & b;
        int or = a | b;
        int xor = a ^ b;
        int not = ~a;

        // Compound assignments
        a += 5;
        b -= 3;
        c *= 2;
        a <<= 1;
        b >>= 1;
        c >>>= 1;

        // Cast expressions
        double d = (double) a;
        int i = (int) 3.14;
        Object obj = (Object) "test";

        // String concatenation with various types
        String s = "a" + 1 + 2.0 + 'c' + true + null;
    }

    // Synchronized method
    public synchronized void syncMethod() {
        synchronized (this) {
            notify();
        }
    }

    // Throws clause
    public void throwsMethod() throws Exception, RuntimeException {
        throw new Exception("test");
    }

    // Abstract inner class
    abstract class AbstractInner {
        abstract void doIt();
        void concrete() {
            doIt();
        }
    }

    // Interface with constants
    interface Constants {
        int MAX = 100;
        String NAME = "constants";
        void action();
    }
}
