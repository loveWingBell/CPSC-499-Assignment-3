// Test7_Invalid_Java5Features.java - Invalid for Java 1.2: uses generics and enhanced for
import java.util.ArrayList;
import java.util.List;

public class Test7_Invalid_Java5Features {
    // Generics - not in Java 1.2
    private List<String> items = new ArrayList<String>();

    // Enhanced for loop - not in Java 1.2
    public void printAll() {
        for (String item : items) {
            System.out.println(item);
        }
    }

    // Autoboxing - not in Java 1.2
    public void autobox() {
        Integer x = 5;
    }

    // Enum - not in Java 1.2
    enum Color { RED, GREEN, BLUE }
}
