// Test6_Invalid_BadSyntax.java - Invalid: various syntax errors
public class Test6_Invalid_BadSyntax {
    // Missing return type
    foo() {
        return 42;
    }

    // Mismatched braces
    public void bar() {
        if (true) {
            System.out.println("open");
        // Missing closing brace for if
    }

    // Invalid expression
    public void baz() {
        int x = * 5;
    }
}
