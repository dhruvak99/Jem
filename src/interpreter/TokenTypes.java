package interpreter;

public enum TokenTypes {
    //single character tokens
    LEFT_PARAM, RIGHT_PARAM, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT , MINUS, PLUS, SEMICOLON, SLASH, STAR , MODULUS, POWER,
    //one or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    //literals : number , strings etc
    IDENTIFIER, STRING, NUMBER,
    //keywords
    AND,ELSE,FALSE,FUNCTION,FOR,IF,NIL,OR,PRINT,RETURN,TRUE,VAR,WHILE,
    //end of file token
    EOF

}
