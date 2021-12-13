package cs451.Parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigParser {

    private String path;
    private int nbMessage;
    private int [][] cause;

    public boolean populate(String value, int nbHosts) {
        cause = new int [nbHosts][nbHosts];
        File file = new File(value);
        path = file.getPath();
        Scanner scanner;
        int i = 0;
        try {
            scanner = new Scanner(file);
            nbMessage = scanner.nextInt();scanner.nextLine();
            while(scanner.hasNextLine()){
                String[] ls = scanner.nextLine().split(" ");
                for(String s : ls){
                    cause[i][Integer.parseInt(s)-1] = 1;
                }
                i++;
            }

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

    public int[][] getCause() {
        return cause;
    }
}
