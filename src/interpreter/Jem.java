package interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//using java new io libraries
import java.nio.charset.Charset;//A named mapping between sequences of sixteen-bit Unicode code units and sequences of bytes.
import java.nio.file.Files; //contains exclusive static methods that operate on files, directories etc
import java.nio.file.Paths; //object used to locate local files on the system
import java.util.List;
import java.util.Scanner;


public class Jem {
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
            System.exit(64);
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
}
