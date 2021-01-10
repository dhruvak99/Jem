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
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false; // single copy is created and shared among all instances of the class

    public static void main(String args[]) throws IOException {

        if (args.length > 1) {
            System.out.println("Usage: Jem [script]");
            /*
            using exit code 64, the command was used incorrectly example if wrong number of
            arguments are given, a bad flag, bad syntax in parameter
            refer https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
            for more info
            */
            System.exit(64); //EX_USAGE
            //64 is for wrong number of arguments, bad flag
        } else if (args.length == 1) {
            runfile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runfile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset())); // default charset used in the machine Charset.defaultcharset()

        if (hadError) System.exit(65); //input data was incorrect
        if (hadRuntimeError) System.exit(70); //internal software error has been detected (non OS related errors).
        //code 65 indicates input data has some kind of error

    }

    //for interactive interpreter
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print(">");
            String line = reader.readLine();
            if (line == null) break;
            //if nothing is entered break the prompt
            //Ctrl+D to stop the prompt
            run(line);
            //reset flag in the interactive loop, if user makes mistake it should not kill entire process.
            hadError = false;

        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        //stop if there was a syntax error.

        if (hadError) return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;
        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.out.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message
        );
        hadError = true;
    }

    static void error(Token token, String message) {
        /*
        this reports an error at a given token.
        it shows the token's location and token itself.
        this will come in handy as we use tokens to track loactions
         */
        if (token.type == TokenTypes.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
