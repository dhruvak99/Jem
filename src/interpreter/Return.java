package interpreter;

public class Return extends  RuntimeException{
    final Object value;
    /*
    it is a wrapper around the return value
    the super constructor call with those nulls and falses disables some JVM machinery that we don't need.
     */
    Return(Object value)
    {
        super(null,null,false,false);
        this.value = value;
    }
}
