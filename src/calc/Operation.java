package calc;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

class Expression
{
    public static Result<Double> calculate(String expression)
    {
        return tokenize(expression).andThen(tokens -> Expression.calculate(tokens.stream()));
    }

    static Result<Double> calculate(Stream<Token> tokens)
    {
        ArrayList<Token> listOfTokens = new ArrayList<Token>(tokens.toList());
        if(listOfTokens.get(0).type() == TokenType.OPEN_PAREN
                && listOfTokens.get(listOfTokens.size() - 1).type() == TokenType.CLOSE_PAREN)
        {
            listOfTokens.remove(0);
            listOfTokens.remove(listOfTokens.size() - 1);
        }

        if(listOfTokens.size() == 1)
        {
            return Result.of(Double.parseDouble(listOfTokens.get(0).value()));
        }

        Queue<Token> leftOperand = new ArrayDeque<>();
        Queue<Token> rightOperand = new ArrayDeque<>();

        int leastPriorityOperatorIndex = 0;
        int leastPriority = 10;

        int openParen = 0;
        for(int t = 0; t < listOfTokens.size(); t++)
        {
            Token token = listOfTokens.get(t);

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

        boolean operatorFound = false;
        TokenType operator = TokenType.ADD;
        openParen = 0;

        for(int t = 0; t < listOfTokens.size(); t++)
        {
            Token token = listOfTokens.get(t);

            if(token.type() == TokenType.OPEN_PAREN)
            {
                openParen++;
            }
            else if(token.type() == TokenType.CLOSE_PAREN)
            {
                openParen--;
            }

            if(openParen == 0 && t == leastPriorityOperatorIndex)
            {
                operatorFound = true;
                operator = token.type();
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

        return switch(operator)
                {
                    case ADD -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 + r2)));
                    case SUB -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 - r2)));
                    case MUL -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 * r2)));
                    case DIV -> calculate(leftOperand.stream()).andThen(r1 -> calculate(rightOperand.stream()).andThen(r2 -> Result.of(r1 / r2)));
                    default -> Result.error("Fail");
                };

        //return Result.error("Fail");
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
}

interface Operation
{
    double execute();
}

interface UnaryOperation extends Operation
{
    double argument = 0;
}

abstract class BinaryOperation implements Operation
{
    double firstArg = 0;
    double secondArg = 0;

    BinaryOperation(double firstArg, double secondArg)
    {
        this.firstArg = firstArg;
        this.secondArg = secondArg;
    }
}


class Addition extends BinaryOperation
{
    Addition(double firstArg, double secondArg)
    {
        super(firstArg, secondArg);
    }

    @Override
    public double execute() {
        return firstArg + secondArg;
    }
}

class Subtraction extends BinaryOperation
{
    Subtraction(double firstArg, double secondArg)
    {
        super(firstArg, secondArg);
    }

    @Override
    public double execute() {
        return firstArg - secondArg;
    }
}

class Multiplication extends BinaryOperation
{
    Multiplication(double firstArg, double secondArg)
    {
        super(firstArg, secondArg);
    }

    @Override
    public double execute() {
        return firstArg * secondArg;
    }
}

class Division extends BinaryOperation
{
    Division(double firstArg, double secondArg)
    {
        super(firstArg, secondArg);
    }

    @Override
    public double execute() {
        return firstArg / secondArg;
    }
}