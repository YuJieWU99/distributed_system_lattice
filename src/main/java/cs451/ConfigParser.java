package cs451;

import java.io.File;

public class ConfigParser {

    private String path;

    public boolean populate(String value) { //check that the file exits, value is the path, eg:c://program//cs451.py
        File file = new File(value);
        path = file.getPath();
        return true;
    }

    public String getPath() {
        return path;
    }//get the file's path

}
