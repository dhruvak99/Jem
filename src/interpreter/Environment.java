package interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    //a map for variable names and values
    private final Map<String,Object> values = new HashMap<>();
    Environment()
    {
        //this constructor is for global scope which ends the chain
        enclosing = null;
    }
    Environment(Environment enclosing)
    {
        //creates a local scope nested inside the given outer one
        this.enclosing = enclosing;
    }
    Object get(Token name)
    {
        if(values.containsKey(name.lexeme))
        {
            return values.get(name.lexeme);
        }
        if (enclosing!=null) return enclosing.get(name);

        throw new RuntimeError(name,"Undefined variable '"+name.lexeme+"'.");
    }
    void assign(Token name, Object value)
    {
        if(values.containsKey(name.lexeme))
        {
            values.put(name.lexeme,value);
            return;
        }
        if(enclosing!=null)
        {
            enclosing.assign(name,value);
            return;
            //if the variable is not in the environment then it checks the outer one, recursively.
        }
        throw new RuntimeError(name,
                "Undefined variable '"+name.lexeme+ "'.");
    }
    void define(String name, Object value)
    {
        values.put(name,value);
    }
    Environment ancestor (int distance){
        Environment environment = this;
        for(int i =0 ;i<distance;i++)
        {
            environment = environment.enclosing;
        }
        return environment;
    }
    Object getAt(int distance,String name){
        return ancestor(distance).values.get(name);
    }
    void assignAt(int distance, Token name, Object value)
    {
        ancestor(distance).values.put(name.lexeme,value);
    }
}
