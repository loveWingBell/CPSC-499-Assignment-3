// I got Claude to generate this

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Test file demonstrating Java 8 features:
 *   - Lambda expressions
 *   - Stream API
 *   - Method references
 *   - Default interface methods
 *   - Optional
 */
public class Test_Java8Features {

    // Default interface method (Java 8)
    interface Greeter {
        String greet(String name);

        default String greetLoudly(String name) {
            return greet(name).toUpperCase();
        }
    }

    public static void main(String[] args) {

        // Lambda expression
        Greeter g = name -> "Hello, " + name;
        System.out.println(g.greet("World"));
        System.out.println(g.greetLoudly("World"));

        // Stream API + lambda
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Diana");

        List<String> filtered = names.stream()
            .filter(n -> n.length() > 3)
            .map(String::toUpperCase)       // method reference
            .sorted()
            .collect(Collectors.toList());

        filtered.forEach(System.out::println);  // method reference

        // Optional
        Optional<String> opt = names.stream()
            .filter(n -> n.startsWith("C"))
            .findFirst();

        opt.ifPresent(n -> System.out.println("Found: " + n));

        // Comparator using lambda
        names.sort((a, b) -> a.compareTo(b));

        // Function composition
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addTen   = x -> x + 10;
        Function<Integer, Integer> combined = doubleIt.andThen(addTen);
        System.out.println(combined.apply(5));  // 20
    }
}
