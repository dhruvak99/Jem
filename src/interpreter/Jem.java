package interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Jem {
    public static void main(String args[]) throws IOException{

        if(args.length > 1)
        {
            System.out.println("Usage: Jem [script]");
            System.exit(64);
        }else if(args.length == 1)
        {
            runfile(args[0]);
        }
        else{
            runPrompt();
        }
    }
}
