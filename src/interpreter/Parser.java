package interpreter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static interpreter.TokenTypes.*;
public class Parser {
    /*
        The parser consumes a flat sequence of tokens instead of characters.
        We store the list of tokens and use a current to point to the next token
         */
    private static class ParseError extends RuntimeException {} //simple sentinel class we use to unwind the parser
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }
    List<Stmt> parse() {
        /*
        it parses a series of statements, as many as it can find
        until the end of file
        this is direct translation of program to recursive style
         */
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd())
        {
            statements.add(declaration());
        }
        return statements;
    }

    private Expr expression()
    {
        return assignment();
    }
    private Stmt declaration()
    {
        try{
            if(match(CLASS)) return classDeclaration(); //classes
            if(match(FUNCTION)) return function("function");
            //parsing happens here
            if(match(VAR))  return varDeclaration(); //checks if its a var declaration
            //else bumps up in precedence to statement
            return statement();
        }catch(ParseError error){
            synchronize();
            return null;
        }
    }
    private Stmt classDeclaration()
    {
        Token name = consume(IDENTIFIER,"Expect class name.");
        Expr.Variable superclass = null;
        if (match(LESS)){
            consume(IDENTIFIER,"Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }
        consume(LEFT_BRACE, "Expect '{' before class body");

        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE,"Expect '}' after class body");

        return new Stmt.Class(name,superclass , methods);
    }
    private Stmt statement()
    {
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(RETURN)) return returnStatement();
        if(match(FOR)) return forStatement();
        if(match(WHILE)) return whileStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }
    private Stmt forStatement()
    {
        consume(LEFT_PARAM,"Expect '(' after 'for'.");
        //first clause is initializer
        Stmt initializer;
        if(match(SEMICOLON)){
            initializer = null;
        }else if(match(VAR)){
            initializer = varDeclaration();
        }else{
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON,"Expect ';' after loop condition");

        Expr increment = null;
        if(!check(RIGHT_PARAM)){
            increment = expression();
        }
        consume(RIGHT_PARAM,"Expect ')' after for clauses.");
        Stmt body = statement();
        /*
        if the increment is one, executes after the body in each iteration of the loop
        we do that by replacing the body with a little block that contains the original body followed by an expression statement that
        evaluates the increment.
         */
        if(increment!=null)
        {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }
        if(condition==null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition,body);
        if(initializer !=null ){
            body = new Stmt.Block(Arrays.asList(initializer,body));
        }
        return body;
    }
    private Stmt ifStatement()
    {
        consume(LEFT_PARAM, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PARAM, "Expect ')' after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        //parser looks for an else clause if it is not present then elseBranch field in syntax tree is null.
        if(match(ELSE))
        {
            elseBranch = statement();
        }
        return new Stmt.If(condition,thenBranch,elseBranch);
    }
    //printstatement method
    private Stmt printStatement()
    {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    //returnStatement method
    private Stmt returnStatement(){
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }
        consume(SEMICOLON,"Expect ';' after return statement.");
        return new Stmt.Return(keyword, value);
    }
    //varDeclaration method
    private Stmt varDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)){
            initializer =expression();
        }
        consume(SEMICOLON,"Expect ';' after variable declaration");
        return new Stmt.Var(name , initializer);
    }
    //while method
    private Stmt whileStatement()
    {
        consume(LEFT_PARAM, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PARAM, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition,body);
    }
    //expressionstatement
    private Stmt expressionStatement()
    {
        /*
        parses an expression followed by a semicolon.
        it wraps that Expr in a Stmt and returns it.
         */
        Expr expr = expression();
        consume(SEMICOLON,"Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    private Stmt.Function function(String kind){
        Token name = consume(IDENTIFIER, "Expect "+ kind + " name.");
        consume(LEFT_PARAM, "Expect '(' after "+kind+" name.");
        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PARAM)){
            do{
                if(parameters.size()>=255)
                {
                    error(peek(),"Can't have more than 255 parameters.");
                }
                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name.")
                );

            }while(match(COMMA));
        }
        consume(RIGHT_PARAM,"Expect ')' after parameters.");

        //we parse the body
        consume(LEFT_BRACE, "Expect '{' before "+kind+" body.");
        List<Stmt> body = block();
        return new Stmt.Function(name,parameters,body);
    }
    // {} block
    private List<Stmt> block()
    {
        /*
        we create an empty list and parse statements and add them to the list until we reach the end of the block,
        marked by closing }.
        isAtEnd() is important for parser to stop if user forgets '}'.
         */
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(RIGHT_BRACE,"Expect '}' after block.");
        return statements;
    }
    private Expr assignment()
    {
        /*
        parsing an assignment expression looks similar to
        the other binary operators like +.
        we parse the left-hand sie, which can be any expression of higher precedence
        if we find a =, we parse the right-hand side
        and then wrap it all up in an assignment expression tree node.
         */
        Expr expr = or();

        if(match(EQUAL))
        {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable)
            {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name,value);
            }
            else if(expr instanceof Expr.Get)
            {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.Object,get.name,value);
            }
            error(equals, "Invalid Assignment target");
        }
        return expr;
    }
    //this code parses a series of OR expressions
    private Expr or()
    {
        Expr expr = and();

        while(match(OR))
        {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr,operator,right);
        }
        return expr;
    }
    private Expr and()
    {
        Expr expr = equality();
        while(match(AND))
        {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr,operator,right);
        }
        return expr;
    }
    /*
    equality → comparison ( ( "!=" | "==" ) comparison )* ;
     translate expression grammar to java code
     the expression rule simply expands to equality
     */
    private Expr equality()
    {
        Expr expr = comparison();

        while(match(BANG_EQUAL,EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }
    private Expr comparison()
    {
        // comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        Expr expr = term();

        while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }
    private Expr term()
    {
        // term → factor ( ( "-" | "+" ) factor )* ;
        Expr expr = factor();
        while(match(PLUS,MINUS))
        {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr factor()
    {
        // factor  → unary ( ( "/" | "*" ) unary )* ;
        Expr expr = unary();
        while (match(STAR,SLASH,MODULUS,POWER))
        {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }
    private Expr unary()
    {
        /* unary  → ( "!" | "-" ) unary | primary ;
        we look at the current token to see how to parse.
        if it's a ! or - we must have a unary expression
        in that case we grab the token and recursively call unary() to parse the operand.
        wrap all that in unary parse tree
         */
        if(match(BANG,MINUS))
        {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator,right);
        }
        return call();
    }
    private Expr finishCall(Expr callee)
    {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PARAM)){
            do{
                if(arguments.size() >= 255){
                    error(peek(),"Can't have more than 255 arguments");
                }
                arguments.add(expression());
            }while(match(COMMA));
        }
        Token paren = consume(RIGHT_PARAM, "Expect ')' after arguments");

        return new Expr.Call(callee,paren,arguments);
    }
    private Expr call()
    {
        /*
        first we parse the a primary expression, the "left operand" to the call
        then each time we see a (, we call finishCall() to parse the call expression using previously parsed expression as the callee.
        the returned expression becomes the new expr and we loop to see if the result is itself called.
         */
        Expr expr = primary();

        while(true){
            if(match(LEFT_PARAM))
            {
                expr =finishCall(expr);
            }else if(match(DOT)){
                Token name = consume(IDENTIFIER,"Expect property name after '.'.");
                expr = new Expr.Get(expr,name);
            }else{
                break;
            }
        }
        return expr;
    }
    private Expr primary()
    {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }
        if(match(SUPER))
        {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                    "Expect superclass method name");
            return new Expr.Super(keyword,method);
        }
        if(match(THIS)) return new Expr.This(previous());
        if(match(IDENTIFIER))
        {
            return new Expr.Variable(previous());
        }
        if(match(LEFT_PARAM)){
            /*
            after we match a left paran and parse the expression inside it
            we must find a right paran else it's an error.
             */
            Expr expr = expression();
            consume(RIGHT_PARAM, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect Expression");
    }

    private boolean match(TokenTypes... types)
    {
        /*
        This method checks if the current token is of any of the given types
        if so, it will consume the token and return true.
        Otherwise false.
         */
        for(TokenTypes type:types)
        {
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }
    private Token consume(TokenTypes type,String message)
    {
        /*
        similar to match,checks if next token is of expected type
        if yes,
        consumes and advances
        else,
        reports error.
         */
        if(check(type)) return advance();

        throw error(peek(),message);
    }
    private boolean check(TokenTypes type)
    {
        /*
        this method returns true is the type is of given type
        it doesn't consume any tokens like match
         */
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance()
    {
        //this methods consumes the current token and returns it
        if(!isAtEnd()) current++;
        return previous();
    }
    /*
    fucntions below are handy functions
     */

    private boolean isAtEnd()
    {
        //checks if we're running out of tokens to parse
        return peek().type == EOF;
    }
    private Token previous()
    {
        //returns the most recent consumed token
        return tokens.get(current-1);
    }
    private Token peek()
    {
        //returns the current token we have yet to consume
        return tokens.get(current);
    }
    private ParseError error(Token token, String message)
    {
        Jem.error(token,message);
        return new ParseError();
    }

    private void synchronize()
    {
        advance();

        while(!isAtEnd())
        {
            if(previous().type == SEMICOLON) return;

            switch (peek().type)
            {
                case CLASS:
                case FUNCTION:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
