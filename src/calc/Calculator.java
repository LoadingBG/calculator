package calc;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

record Operands(Queue<Token> left, Queue<Token> right)
{

}

public class Calculator
{
    public static Result<Double> calculate(String expression)
    {
        return tokenize(expression).andThen(tokens -> Calculator.calculate(tokens.stream()));
    }

    private static void removeUnnecessaryParentheses(ArrayList<Token> tokens)
    {
        if(tokens.get(0).type() == TokenType.OPEN_PAREN
                && tokens.get(tokens.size() - 1).type() == TokenType.CLOSE_PAREN)
        {
            tokens.remove(0);
            tokens.remove(tokens.size() - 1);
        }
    }

    private static int indexOfLeastPriorityOperator(ArrayList<Token> tokens)
    {
        int leastPriorityOperatorIndex = 0;
        int leastPriority = 10;

        int openParen = 0;
        for(int t = 0; t < tokens.size(); t++)
        {
            Token token = tokens.get(t);

            if(token.type() == TokenType.OPEN_PAREN)
            {
                openParen++;
                continue;
            }
            else if(token.type() == TokenType.CLOSE_PAREN)
            {
                openParen--;
                continue;
            }

            if(openParen == 0 && token.type().priority() <= leastPriority) {
                leastPriority = token.type().priority();
                leastPriorityOperatorIndex = t;
            }
        }

        return leastPriorityOperatorIndex;
    }

    private static Operands getOperands(ArrayList<Token> tokens, int operatorIndex)
    {
        Queue<Token> leftOperand = new ArrayDeque<>();
        Queue<Token> rightOperand = new ArrayDeque<>();

        boolean operatorFound = false;
        int openParen = 0;

        for(int t = 0; t < tokens.size(); t++)
        {
            Token token = tokens.get(t);

            if(token.type() == TokenType.OPEN_PAREN)
            {
                openParen++;
            }
            else if(token.type() == TokenType.CLOSE_PAREN)
            {
                openParen--;
            }

            if(openParen == 0 && t == operatorIndex)
            {
                operatorFound = true;
                continue;
            }

            if(operatorFound)
            {
                rightOperand.offer(token);
            }
            else
            {
                leftOperand.offer(token);
            }
        }

        return new Operands(leftOperand, rightOperand);
    }

    private static Result<Double> calculate(Stream<Token> tokens)
    {
        ArrayList<Token> listOfTokens = new ArrayList<Token>(tokens.toList());

        removeUnnecessaryParentheses(listOfTokens);

        if(listOfTokens.size() == 1)
        {
            return Result.of(Double.parseDouble(listOfTokens.get(0).value()));
        }

        int leastPriorityOperatorIndex = indexOfLeastPriorityOperator(listOfTokens);

        Operands operands = getOperands(listOfTokens, leastPriorityOperatorIndex);

        Queue<Token> leftOperand = operands.left();
        Queue<Token> rightOperand = operands.right();

        TokenType operator = listOfTokens.get(leastPriorityOperatorIndex).type();

        return switch(operator)
                {
                    case ADD -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 + r2)));
                    case SUB -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 - r2)));
                    case MUL -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 * r2)));
                    case DIV -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 / r2)));
                    case NEG -> calculate(rightOperand.stream()).andThen(r -> Result.of(-r));
                    default -> Result.error("Fail");
                };
    }

    static Map<Character, Supplier<Token>> operatorsMap = new HashMap<>();
    static {
        operatorsMap.put('+', () -> new Token("+"));
        operatorsMap.put('-', () -> new Token("-"));
        operatorsMap.put('*', () -> new Token("*"));
        operatorsMap.put('/', () -> new Token("/"));
        operatorsMap.put('(', () -> new Token("("));
        operatorsMap.put(')', () -> new Token(")"));
    }

    private static Result<Queue<Token>> tokenize(String code) {
        ArrayDeque<Token> stream = new ArrayDeque<>();
        while (!code.isBlank()) {
            code = code.strip();
            if (operatorsMap.containsKey(code.charAt(0))) {
                if(code.charAt(0) == '-'
                        && (stream.isEmpty()
                            || (stream.peekLast().type() != TokenType.NUM
                                && stream.peekLast().type() != TokenType.CLOSE_PAREN)))
                {
                    stream.offer(new Token("_"));
                }
                else
                {
                    stream.offer(operatorsMap.get(code.charAt(0)).get());
                }
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
}