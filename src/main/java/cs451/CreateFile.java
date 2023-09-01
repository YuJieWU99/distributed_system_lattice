package cs451;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//public class CreateFile {
//    private static CreateFile instance = new CreateFile();
//    private CreateFile(){}
//    private StringBuffer logBuffer;
//    private String outputPath;
//    public static CreateFile getInstance(){
//        return instance;
//    }
//    public void init(String outputPath){
//        this.logBuffer = new StringBuffer();
//        this.outputPath = outputPath;
//    }
//
//    public void addLogBuffer(String str){
//        logBuffer.append(str);
//    }
//    public void writeLogFile(){
//        try {
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
//            // System.out.println(logBuffer.toString());
//            bufferedWriter.write(logBuffer.toString());
//            bufferedWriter.close();
//            System.out.println("The output is done");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
public class CreateFile {
    private static CreateFile instance = new CreateFile();
    private CreateFile(){}
    private static StringBuffer logBuffer;
    private static String outputPath;
    public static CreateFile getInstance(){
        return instance;
    }
    public void init(String outputPath){
        this.logBuffer = new StringBuffer();
        this.outputPath = outputPath;
    }

    public void addLogBuffer(String str){
        logBuffer.append(str);
    }
    public static void writeLogFile(){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
            // System.out.println(logBuffer.toString());
            bufferedWriter.write(logBuffer.toString());
            bufferedWriter.close();
            System.out.println("The output is done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
    /*
        try {
            File myObj = new File("filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
*/