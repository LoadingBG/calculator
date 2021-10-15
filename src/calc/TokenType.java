package calc;

enum TokenType {
    NUM(10),
    ADD(1),
    SUB(1),
    MUL(2),
    DIV(2),
    NEG(10),
    OPEN_PAREN(0),
    CLOSE_PAREN(6);

    private int priority;

    TokenType(int priority) {
        this.priority = priority;
    }

    int priority() {
        return priority;
    }

    static TokenType fromString(String token) {
        return switch (token) {
            case "+" -> ADD;
            case "-" -> SUB;
            case "*" -> MUL;
            case "/" -> DIV;
            case "_" -> NEG;
            case "(" -> OPEN_PAREN;
            case ")" -> CLOSE_PAREN;
            default  -> NUM;
        };
    }
}
