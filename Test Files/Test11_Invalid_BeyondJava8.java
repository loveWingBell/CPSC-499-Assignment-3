// I got Claude to generate this

import java.util.*;
import java.util.stream.*;

/**
 * Test file demonstrating features BEYOND Java 8.
 * This file will NOT parse correctly with a Java 8 grammar or
 * JavaParser configured for JAVA_8 language level.
 *
 * Features used:
 *   - var (local variable type inference)         [Java 10]
 *   - Text blocks (multiline strings)             [Java 15]
 *   - Records                                     [Java 16]
 *   - Sealed classes                              [Java 17]
 *   - Pattern matching instanceof                 [Java 16]
 *   - Switch expressions with arrow syntax        [Java 14]
 */
public class Test_BeyondJava8 {

    // Record (Java 16)
    record Point(int x, int y) {
        double distanceFromOrigin() {
            return Math.sqrt(x * x + y * y);
        }
    }

    // Sealed class hierarchy (Java 17)
    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}

    public static void main(String[] args) {

        // var — local variable type inference (Java 10)
        var names = List.of("Alice", "Bob", "Charlie");
        var filtered = names.stream()
            .filter(n -> n.length() > 3)
            .collect(Collectors.toList());
        filtered.forEach(System.out::println);

        // Text block (Java 15)
        var json = """
                {
                    "name": "Alice",
                    "age": 30
                }
                """;
        System.out.println(json);

        // Record usage (Java 16)
        var p = new Point(3, 4);
        System.out.println("Distance: " + p.distanceFromOrigin());

        // Pattern matching instanceof (Java 16)
        Object obj = "Hello, world!";
        if (obj instanceof String s) {
            System.out.println(s.toUpperCase());
        }

        // Switch expression with arrow syntax (Java 14)
        int day = 3;
        String dayName = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            default -> "Other";
        };
        System.out.println(dayName);

        // Sealed class + pattern matching
        Shape shape = new Circle(5.0);
        double area = switch (shape) {
            case Circle c    -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
        };
        System.out.println("Area: " + area);
    }
}
