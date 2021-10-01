package calc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Supplier;

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
                tokenize(line)
                        .andThen(Main::getNotation)
                        .andThen(Main::calculate)
                        .consume(r -> System.out.println("Result: " + r), System.out::println);
            }
        }
    }

    private static Map<Character, Supplier<Token>> operatorsMap = new HashMap<>();
    static {
        operatorsMap.put('+', () -> new Token("+"));
        operatorsMap.put('-', () -> new Token("-"));
        operatorsMap.put('*', () -> new Token("*"));
        operatorsMap.put('/', () -> new Token("/"));
        operatorsMap.put('(', () -> new Token("("));
        operatorsMap.put(')', () -> new Token(")"));
    }

    public static Result<Queue<Token>> tokenize(String code) {
        Queue<Token> stream = new ArrayDeque<>();
        while (!code.isBlank()) {
            code = code.strip();
            if (operatorsMap.containsKey(code.charAt(0))) {
                stream.offer(operatorsMap.get(code.charAt(0)).get());
                code = code.substring(1);
            } else {
                StringBuilder number = new StringBuilder();
                int length = 0;
                while (!code.isEmpty() && (Character.isDigit(code.charAt(0)) || code.charAt(0) == '.')) {
                    number.append(code.charAt(0));
                    code = code.substring(1);
                    ++length;
                }
                if (length == 0) {
                    return Result.error("The token \"" + code.charAt(0) + "\" is invalid.");
                }
                stream.offer(new Token(number.toString()));
            }
        }
        return Result.of(stream);
    }

    private static Result<Queue<Token>> getNotation(Queue<Token> stream) {
        Queue<Token> notation = new ArrayDeque<>();
        Queue<Token> operators = new ArrayDeque<>();
        TokenType prevTokenType = null;
        // Maybe streamify this
        for (Token token : stream) {
            switch (token.type()) {
                case NUM -> notation.offer(token);
                case OPEN_PAREN -> operators.offer(token);
                case CLOSE_PAREN -> {
                    try {
                        while (operators.element().type() != TokenType.OPEN_PAREN) {
                            notation.offer(operators.poll());
                        }
                        operators.poll();
                    } catch (NoSuchElementException e) {
                        return Result.error("Found a closing parenthesis without an opening one.");
                    }
                }
                default -> {
                    if (token.type() == TokenType.SUB && prevTokenType != TokenType.NUM) {
                        operators.offer(new Token("_"));
                    } else if (!(token.type() == TokenType.ADD && prevTokenType != TokenType.NUM)) {
                        while (!operators.isEmpty()
                                && operators.peek().type().priority() >= token.type().priority()) {
                            notation.offer(operators.poll());
                        }
                        operators.offer(token);
                    }
                }
            }
            prevTokenType = token.type();
        }
        while (!operators.isEmpty()) {
            if (operators.peek().type() == TokenType.OPEN_PAREN) {
                return Result.error("Found an opening parenthesis without a closing one.");
            }
            notation.offer(operators.poll());
        }
        return Result.of(notation);
    }

    public static Result<Double> calculate(Queue<Token> rpn) {
        Deque<Double> values = new ArrayDeque<>();
        for (Token token : rpn) {
            try {
                switch (token.type()) {
                    case NUM -> values.offer(Double.valueOf(token.value()));
                    case NEG -> values.offer(-values.removeLast());
                    case ADD -> values.offer(values.removeLast() + values.removeLast());
                    case SUB -> values.offer(-values.removeLast() + values.removeLast());
                    case MUL -> values.offer(values.removeLast() * values.removeLast());
                    case DIV -> values.offer(1.0 / values.removeLast() * values.removeLast());
                    default  -> throw new IllegalArgumentException("Unreachable");
                }
            } catch (NoSuchElementException e) {
                return Result.error("The operator \"" + token.value() + "\" is missing an operand.");
            }
        }
        if (values.size() > 1) {
            return Result.error("Operands without operators were found.");
        }
        return Result.of(values.pollLast());
    }
}
