package interpreter;
import java.util.List;
import static interpreter.TokenTypes.*;
public class Parser {

//    private final List<Token> tokens;
//    private int current =0 ;
//    //parser constructor
//    Parser(List<Token> tokens)
//    {
//        this.tokens = tokens;
//    }
//    private Expr expression(){
//        return equality();
//
//        while (match(BANG_EQUAL,EQUAL_EQUAL)){
//            Token operator = previous();
//            Expr right = comparision();
//            expr = new Expr.Binary(expr,operator,right);
//        }
//
//        return expr;
//    }
//
//    private boolean match(TokenTypes... types)
//    {
//        for(TokenTypes type:types)
//        {
//            if(check(type))
//            {
//                advance();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean check(TokenTypes type)
//    {
//        if (isAtEnd()) return false;
//        return peek().type == type;
//    }
//
//    private Token advance()
//    {
//        if(isAtEnd()) current++;
//        return previous();
//    }
//
//    private boolean isAtEnd() {
//        return peek().type == EOF;
//
//    }
//
//    private Token peek()
//    {
//        return tokens.get(current);
//    }
//
//    private Token previous()
//    {
//        return tokens.get(current-1);
//    }
}
