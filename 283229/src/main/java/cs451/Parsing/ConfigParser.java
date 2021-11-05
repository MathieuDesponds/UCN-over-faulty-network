package cs451.Parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigParser {

    private String path;
    private int nbMessage;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            nbMessage = scanner.nextInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public int getNbMessage() {
        return nbMessage;
    }
}
