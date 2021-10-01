package calc;

record Token(TokenType type, String value) {
    Token(String tokenValue) {
        this(TokenType.fromString(tokenValue), tokenValue);
    }
}
