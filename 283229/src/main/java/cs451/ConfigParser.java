package cs451;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigParser {

    private String path;
    private int nbMessage;
    private int idToSend;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            nbMessage = scanner.nextInt();
            idToSend = scanner.nextInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public int getIdToSend() {
        return idToSend;
    }

    public int getNbMessage() {
        return nbMessage;
    }
}
