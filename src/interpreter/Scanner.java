package interpreter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import static interpreter.TokenTypes.*;
public class Scanner {
    private static final Map<String, TokenTypes> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",AND);
        keywords.put("or",OR);
        keywords.put("function",FUNCTION);
        keywords.put("for",FOR);
        keywords.put("while",WHILE);
        keywords.put("if",IF);
        keywords.put("else",ELSE);
        keywords.put("nil", NIL);
        keywords.put("return",RETURN);
        keywords.put("true",TRUE);
        keywords.put("false",FALSE);
        keywords.put("var",VAR);
        keywords.put("print",PRINT);
    }
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0; //points to first character in the lexeme.
    private int current =0;//points to current character being considered.
    //start and current are offsets that index the string
    private int line =1;
    /*
    we have raw source code stored in a string
    and a list ready to be filled with tokens
     */
    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd())
        {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF,"",null,line));
        return tokens;
    }
    private void scanToken()
    {
        char c = advance();
        switch(c) {
            case '(':
                addToken(LEFT_PARAM);
                break;
            case ')':
                addToken(RIGHT_PARAM);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                //if next character after greater symbol is '=' then it is '>=' or else '>'
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                //first match for comments
                if (match('/')) {
                    //for comments
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    //add the division token if it is not a comment
                    addToken(SLASH);
                }
                break;
            case ' ': //ignore whitespace
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                }else if(isAlpha(c))
            {
                identifier();
            } else{//throw an error if that character is not a type of token
                    Jem.error(line, "Unexpected character");
                    break;
                }
        }
    }
    private void identifier()
    {
        while(isAlphaNumeric(peek())) advance();
        /*
        check for the keywords map if the identifier is keyword add it
        or it is a variable name , huh, this doesn't even makes sense
        hope you understand by looking at the code XD
         */
        String text = source.substring(start,current);
        TokenTypes type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }
    private void number()
    {
        while(isDigit(peek())) advance();

        //for decimal digit
        if(peek() == '.' && isDigit(peekNext())){
            //consume '.'
          advance();
          while(isDigit(peek())) advance();
        }
        addToken(NUMBER,
                Double.parseDouble(source.substring(start,current)));
    }
    private void string()
    {
        while(peek()!='"' && !isAtEnd())
        {
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd())
        {
            Jem.error(line,"Unterminated string value.");
            return;
        }
        advance();

        String value = source.substring(start+1,current-1);
        addToken(STRING,value);
    }
    private boolean match(char expected)
    {
        if (isAtEnd()) return false;
        if(source.charAt(current)!=expected)return false;

        current++;
        return true;
    }
    private char peek()
    {
        //similar to advance but doesn't consume character
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char peekNext()
    {
        if (current+1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }
    private boolean isAlpha(char c)
    {
        return ( c >= 'a' && c <= 'z') || (c>='A' && c<='Z') || c=='_';
    }
    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }
    private boolean isDigit(char n)
    {
        return n>='0' && n<='9';
    }
    private boolean isAtEnd()
    {
        return current>=source.length();
    }
    private char advance()
    {
        current++;
        return source.charAt(current-1);
    }
    private void addToken(TokenTypes type)
    {
        addToken(type,null);
    }
    private void addToken(TokenTypes type, Object literal)
    {
        String text = source.substring(start,current);
        tokens.add(new Token(type,text,literal, line));
    }
}
