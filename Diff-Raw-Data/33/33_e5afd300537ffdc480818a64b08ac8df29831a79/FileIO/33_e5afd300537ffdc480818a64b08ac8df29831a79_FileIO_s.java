 package project.phase2.file;
 
 import project.phase2.structs.StringMatchList;
 import project.phase2.structs.StringMatchTuple;
 
 import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
 
 /**
  * File editing tool.
  */
 public class FileIO {
 
    private FileIO() {}
 
     public static void writeFile(final File dest, final String toWrite) throws IOException {
         BufferedWriter out = new BufferedWriter(new FileWriter(dest));
         out.write(toWrite);
         out.close();
     }
 
     /**
      * Convert an entire file to a string.
      *
      * @param file the file
      * @return the string representing the file
      * @throws IOException file reading has been blocked
      */
     public static String readEntireFile(final File file) throws IOException {
        StringBuffer a = new StringBuffer();
        for (String s : readEntireFileIntoLines(file)) {
            a.append("\n").append(s);
         }
        return a.toString().replaceFirst("\n", "");
     }
 
     public static List<String> readEntireFileIntoLines(final File file) throws IOException {
        List<String> list = new ArrayList<String>();
        Scanner s = new Scanner(file);
        while (s.hasNextLine()) {
            list.add(s.nextLine());
        }
        return list;
     }
 }
