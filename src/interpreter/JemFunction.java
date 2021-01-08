package interpreter;

import java.util.List;

public class JemFunction implements JemCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    LoxFunction(Stmt.Function declaration, Environment closure,boolean isInitializer){
        this.isInitializer = isInitializer;
        this.closure = closure;
        this.declaration = declaration;
    }
    JemFunction bind(JemInstance instance)
    {
        Environment environment = new Environment(closure);
        environment.define("this",instance);
        return new JemFunction(declaration,environment,isInitializer);
    }
    public String toString()
    {
        return "<fn "+ declaration.name.lexeme+">";
    }
    @Override
    public int arity()
    {
        return declaration.params.size();
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        Environment environment = new Environment(closure);
        //each fucntion call gets its own environment
        //Environment environment  = new Environment(interpreter.globals);
        for(int i =0;i<declaration.params.size(); i++ ){
            environment.define(declaration.params.get(i).lexeme,arguments.get(i));
        }
        /*
        we wrap the call to executeBlock() in a try-catch block.
        when it catches an exception, it pulls the value and makes that the return value from call.
        if it never catches one of those exceptions, it means the function reached the end of its body without hitting a return.
        in that case returns a nil;
         */
        try{
            interpreter.executeBlock(declaration.body,environment);
        }catch(Return returnValue){
            if(isInitializer) return closure.getAt(0,"this");
            return returnValue.value;
        }
        if (isInitializer) return closure.getAt(0,"This");
        return null;
    }
}
