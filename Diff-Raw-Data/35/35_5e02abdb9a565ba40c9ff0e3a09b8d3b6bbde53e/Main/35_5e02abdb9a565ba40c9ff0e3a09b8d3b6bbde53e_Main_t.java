 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class Main {
 
     public static void main(String[] args) {
         int dbnum=loaddatabase();
         String fileDb="";
         if(dbnum==0)
             return;
         else
             fileDb= String.valueOf(dbnum)+".txt";
         try {
             List<String> database = readLines(fileDb);
             List<String> categories = readLines("categories.txt");
             Classifier temp = new Classifier(database,categories);
             System.out.println("Everything loaded correctly, analyzing please wait...");
             temp.analyze();
             readline();
         } catch (Exception ex) {
            System.out.println("An error occured: " + ex.getMessage());
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println(ex.toString());
             readline();
         }
 
 
 
     }
 
 
     public static String readline()
     {
         String CurLine ="";
         InputStreamReader converter = new InputStreamReader(System.in);
         BufferedReader in = new BufferedReader(converter);
         try {
             CurLine= in.readLine();
         } catch (IOException ex1) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
         }
         return CurLine;
     }
 
     public static List<String> readLines(String filename) throws IOException {
         FileReader fileReader = new FileReader(filename);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         List<String> lines = new ArrayList<String>();
         String line = null;
         while ((line = bufferedReader.readLine()) != null) {
             lines.add(line);
         }
         bufferedReader.close();
         return lines;
     }
 
     public static int loaddatabase()
     {
         try{
             int x=10;
             do{
                 System.out.println("Please select the db:");
                 System.out.println("1- For privacy in title 3641 records");
                 System.out.println("2- For privacy in metadata 16780 records");
                 System.out.println("3- For privacy in metadata+full text 60527 records");
                 System.out.println("0- To exit");
 
                 BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                 String s = bufferRead.readLine();
                 x = Integer.valueOf(s);
             }
             while(x>3)    ;
             return x;
         }
         catch(Exception e)
         {
             return loaddatabase();
         }
     }
 
 }
