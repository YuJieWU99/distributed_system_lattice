package cs451;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogUtil {
    public static void Log(String message){
        try{
            PrintStream out = new PrintStream(new FileOutputStream("log.txt",true));
            System.setOut(out);
            System.out.println(message);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
