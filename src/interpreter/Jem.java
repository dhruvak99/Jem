package interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//using java new io libraries
import java.nio.charset.Charset;//A named mapping between sequences of sixteen-bit Unicode code units and sequences of bytes.
import java.nio.file.Files; //contains exclusive static methods that operate on files, directories etc
import java.nio.file.Paths; //object used to locate local files on the system
import java.util.List;


public class Jem {
    static boolean hadError = false;
    public static void main(String args[]) throws IOException{

        if(args.length > 1)
        {
            System.out.println("Usage: Jem [script]");
            /*
            using exit code 64, the command was used incorrectly example if wrong number of
            arguments are given, a bad flag, bad syntax in parameter
            refer https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
            for more info
            */
            System.exit(64); //EX_USAGE
            //64 is for wrong number of arguments, bad flag
        }else if(args.length == 1)
        {
            runfile(args[0]);
        }
        else{
            runPrompt();
        }
    }
    private static void runfile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes,Charset.defaultCharset())); // default charset used in the machine Charset.defaultcharset()

        //if has error stop execution
        if(hadError) System.exit(65); //EX_DATAERR
        //65 is for incorrect data

    }
    //for interactive interpreter
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;)
        {
            System.out.print(">");
            String line = reader.readLine();
            if(line==null) break;
            //if nothing is entered break the prompt
            //Ctrl+D to stop the prompt
            run(line);
            //reset flag in the interactive loop, if user makes mistake it should not kill entire process.
            hadError = false;

        }
    }
    private static void run(String source)
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        //for now just print the tokens.
        for (Token token : tokens)
        {
            System.out.println(token);
        }
    }

    static void error(int line , String message)
    {
        report(line,"",message);
    }
    private static void report(int line, String where, String message){
        System.err.println(
                "[line "+line+"] Error"+where+": "+message
        );
        hadError = true;
    }
}
