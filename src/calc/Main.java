package calc;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Type \"quit\" or \"exit\" to quit.");
            while (true) {
                System.out.print(">>> ");
                String line = sc.nextLine();
                if ("quit".equals(line) || "exit".equals(line)) {
                    break;
                }

                Calculator.calculate(line).consume(r -> System.out.println("Result: " + r), System.out::println);
            }
        }
    }
}
