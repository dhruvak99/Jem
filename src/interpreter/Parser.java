package interpreter;
import java.util.List;
import static interpreter.TokenTypes.*;
public class Parser {

    private final List<Token> tokens;
    private int current =0 ;
    //parser constructor
    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }
    private Expr expression(){
        return equality();
    }
}
