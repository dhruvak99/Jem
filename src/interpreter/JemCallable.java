package interpreter;

import java.util.List;

public interface JemCallable {
    int arity();//check number of arguments
    Object call(Interpreter interpreter, List<Object> arguments);//we pass in the interpreter in case the class implementing call() needs it.
}
