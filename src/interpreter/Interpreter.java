package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>,Stmt.Visitor<Void> {
    //unlike Expr statements produce no values so return type is Void
    final Environment globals = new Environment();
    private final Map<Expr, Integer> locals = new HashMap<>();
    private Environment environment = globals;
    Interpreter(){
        //defines a varible named "clock",
        //it's value is a java anonymous class that implements LoxCallable
        //If we wanted to add other native functions—reading input from the user,
        // working with files, etc.—we could add them each as their own anonymous class that implements LoxCallable.
        globals.define("clock", new JemCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis()/1000.0;
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native sine function
        //fixing required if expressions are passed
        //expressions fixed need some tweaking
        globals.define("sin", new JemCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                return (double)Math.sin(Math.toRadians((double)arguments.get(0)));
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native cos function
        //fixing required if expressions are passed
        //expressions fixed need some tweaking
        globals.define("cos", new JemCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                return (double)Math.cos(Math.toRadians((double)arguments.get(0)));
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native square root functions
        //fixing required if expressions are passed
        //expressions fixed need some tweaking
        globals.define("sqrt", new JemCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                return (double)Math.sqrt((double)arguments.get(0));
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native max function
        //fixing required if expressions are passed
        //expressions fixed need some tweaking
        globals.define("Max", new JemCallable() {
            @Override
            public int arity() {
                return 2;
            }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                return (double)Math.max((double)arguments.get(0),(double)arguments.get(1));
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native min function
        //fixing required if expressions are passed
        //expressions fixed need some tweaking
        globals.define("Min", new JemCallable() {
            @Override
            public int arity() {
                return 2;
            }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                return (double)Math.min((double)arguments.get(0),(double)arguments.get(1));
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
        //native println function
        globals.define("println", new JemCallable() {
            @Override
            public int arity() {
                return 0;
            }
            @Override
            public Void call(Interpreter interpreter, List<Object> arguments) {
                //add evaluate expression to compute expression given as argument
                System.out.print("\n");
                return null;
            }
            @Override
            public String toString(){
                return "<natice fn>";
            }
        });
    }
    void interpret(List<Stmt> statements)
    {
        /*
        takes in a syntax tree for an expression and evaluates it
        if that succeeds evaluate() returns an object for the result value
        interpret converts it to string and shows it to user.
         */
        try{
            for(Stmt statement : statements)
            {
                execute(statement);
            }
        }
        catch(RuntimeError error)
        {
            Jem.runtimeError(error);
        }
    }
    //Evaluating literals
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        /*
        We eagerly produced the runtime value way back during scanning and stuffed it in the token.
         The parser took that value and stuck it in the literal tree node,
        so to evaluate a literal, we simply pull it back out.
         */
        return expr.value;
    }
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        /*
        here we calcualate the left operand first and find a short circuit,
        if the short circuit is not found only then we evaluate the right operand.
         */
        Object left = evaluate(expr.left);

        if(expr.Operator.type == TokenTypes.OR)
        {
            if(isTruthy(left)) return left;
        }else {
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }
    @Override
    public Object visitSetExpr(Expr.Set expr){
        Object object = evaluate(expr.object);
        if(!(object instanceof JemInstance)){
            throw new RuntimeError(expr.name,
                    "Only instances have fields.");
        }
        Object value = evaluate(expr.value);
        ((JemInstance)object).set(expr.name,value);
        return value;
    }
    @Override
    public Object visitSuperExpr(Expr.Super expr)
    {
        int distance = locals.get(expr);
        JemClass superclass = (JemClass)environment.getAt(distance,"super");

        JemInstance object = (JemInstance)environment.getAt(distance-1,"this");

        JemFunction method = superclass.findMethod(expr.method.lexeme);
        if(method == null)
        {
            throw new RuntimeError(expr.method,
                    "Undefined property '"+expr.method.lexeme+"'.");
        }
        return method.bind(object);
    }
    @Override
    public Object visitThisExpr(Expr.This expr)
    {
        return lookUpVariable(expr.keyword,expr);
    }
    //Evaluating Unary expressions
    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        //first evaluate the operand expression
        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator , right);
                return -(double)right;
            //apply the unary operator itself to the result of that
        }
        /*
        we can't evaluate the unary operator itself until after we evaluate
        its operand subexpression. that means post-order traversal (left-right-root).
         */
        //unreachable
        return null;
    }
    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return lookUpVariable(expr.name,expr);
    }
    private Object lookUpVariable(Token name,Expr expr){
        Integer distance = locals.get(expr);
        if(distance !=null)
        {
            return environment.getAt(distance,name.lexeme);

        }
        else {
            return globals.get(name);
        }
    }
    private void checkNumberOperand(Token operator, Object operand)
    {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number.");
    }
    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if(left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator , "Operands must be numbers.");
    }
    private boolean isTruthy(Object object)
    {
        /*
        false and nil are falsey
        rest all are truthy
         */
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean)object;
        return true;
    }
    private boolean isEqual(Object a , Object b)
    {
        if(a==null && b==null) return true;
        if(a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) // helper method
    {
        if(object == null) return "nil";

        if(object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")){
                text = text.substring(0,text.length()-2);
            }
            return text;
        }
        return object.toString();
    }

    //Evaluating parenthesis
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
       /*grouping node has a reference to an inner node
        for the expression contained inside the parentheses.
        to evaluate the grouping expression itself, we recursively evaluate that subexpression
        and return it */
        return evaluate(expr.expression);
    }
    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }
    private void execute(Stmt stmt)
    {
        //statement analog to evaluate
        stmt.accept(this);
    }
    void resolve(Expr expr, int depth){
        locals.put(expr,depth);
    }
    void executeBlock(List<Stmt> statements, Environment environment){
        //it executes a list of statements in the context of a given environment.
        //up until now, the environment field in the interpreter always pointed to the same environment - global one
        //now that field represents the current environment
        Environment previous = this.environment;
        try{
            this.environment = environment;
            for (Stmt statement : statements){
                execute(statement);
            }
            /*
            To execute code within a given scope, this method updates the interpreter's environment field,
            visits all the statements and the restores the previous value.
            we restore the previous environment using finally clause.
            that way it gets restored even if an exception is thrown.
             */
        }finally {
            this.environment = previous;
        }
    }
    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        // to execute a block we create a new environment for the block's scope and pass it off to another method
        executeBlock(stmt.statements,new Environment(environment));
        return null;
    }
    @Override
    public Void visitClassStmt(Stmt.Class stmt)
    {
        Object superclass = null;
        if(stmt.superclass != null){
            superclass = evaluate(stmt.superclass);
            if(!(superclass instanceof JemClass)){
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class");
            }
        }
        //declare the class's name in the current environment
        environment.define(stmt.name.lexeme , null);
        if(stmt.superclass !=null)
        {
            environment = new Environment(environment);
            environment.define("super",superclass);
        }
        Map<String, JemFunction> methods = new HashMap<>();
        for( Stmt.Function method : stmt.methods){
            JemFunction function = new JemFunction(method, environment,
                    method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme,function);
        }
        //then we turn class into syntax node of loxClass
        JemClass klass = new JemClass(stmt.name.lexeme,(JemClass)superclass,methods);
        //we circle back and store object in the variable we declared.
        if(superclass!=null)
        {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);
        return null;
    }
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        JemFunction function = new JemFunction(stmt,environment,false);
        environment.define(stmt.name.lexeme,function);
        return null;
    }
    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        if(isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.thenBranch);
        }else if(stmt.elseBranch!=null)
        {
            execute(stmt.elseBranch);
        }
        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object value = null;
        if(stmt.value!=null) value = evaluate(stmt.value);

        throw new Return(value);
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        /* if the variable has a initializer, we evaluate it.
        if not, we have another choice to make.
         */
        Object value = null;
        if(stmt.initializer !=null)
        {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme,value);
        return null;
    }
    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while(isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.body);
        }
        return null;
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance !=null)
        {
            environment.assignAt(distance, expr.name, value);
        }
        else {
            globals.assign(expr.name, value);
        }
        return value;
    }
    //Evaluating binary operators
    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type)
        {
            case GREATER:
                checkNumberOperands(expr.operator,left,right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left-(double)right;
            case SLASH:
                checkNumberOperands(expr.operator,left,right);
                return (double)left/(double)right;
            case STAR:
                checkNumberOperands(expr.operator,left,right);
                return (double)left*(double)right;
            case MODULUS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left % (double)right;
            case POWER:
                checkNumberOperands(expr.operator,left,right);
                return (double)Math.pow((double)left , (double)right);
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                {
                    return (double)left+(double)right;
                }
                if(left instanceof String && right instanceof String)
                {
                    return (String)left+(String)right;
                }
                throw new RuntimeError(expr.operator ,
                        "Operands must be two number or two strings.");
            /*
            unlike comparision operators which require numbers, the equality
            operators support the operands of any type, even mixed ones.
             */
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left,right);
        }
        //unreachable
        return null;
    }
    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee); //evalute the expression for the callee, typically its an identifier

        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.arguments){
            arguments.add(evaluate(argument)); //then evaluate each argument expressions in order to store the resulting value
        }
        //Once we've got the callee and the arguments ready, all that remains is to perform the call
        // we do that by casting the callee to a LoxCallable and then invoking call() method on it

        if(!(callee instanceof JemCallable))
        {
            throw new RuntimeError(expr.paren,"Can only call functions and classes");
        }

        JemCallable function = (JemCallable)callee;
        if(arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren,"Expected "+
                    function.arity()+" arguments but got "+
                    arguments.size() + ".");
        }
        return function.call(this,arguments);
    }
    @Override
    public Object visitGetExpr(Expr.Get expr)
    {
        Object object = evaluate(expr.Object);
        if(object instanceof JemInstance)
        {
            return ((JemInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name,
                "Only instances have properties.");
    }

}
