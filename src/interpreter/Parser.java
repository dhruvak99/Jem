package interpreter;
import java.util.List;
import static interpreter.TokenTypes.*;
public class Parser {

    private final List<Token> tokens;
    private int current =0 ;

    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

}
