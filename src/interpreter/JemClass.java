package interpreter;

import java.util.List;
import java.util.Map;

public class JemClass implements JemCallable {
    final String name;
    final JemClass superclass;
    private final Map<String, JemFunction> methods;
    JemClass(String name,JemClass superclass,Map<String, JemFunction> methods){

        this.superclass = superclass;
        this.name = name;
        this.methods =methods;
    }
    JemFunction findMethod(String name)
    {
        if(methods.containsKey(name))
        {
            return methods.get(name);
        }
        if(superclass !=null)
        {
            return superclass.findMethod(name);
        }
        return null;
    }


    @Override
    public String toString()
    {
        return name;
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        JemInstance instance = new JemInstance(this);
        JemFunction initializer = findMethod("init");
        if(initializer!=null)
        {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity()
    {
        JemFunction initializer = findMethod("init");
        if(initializer == null ) return 0;
        return initializer.arity();
    }
}
