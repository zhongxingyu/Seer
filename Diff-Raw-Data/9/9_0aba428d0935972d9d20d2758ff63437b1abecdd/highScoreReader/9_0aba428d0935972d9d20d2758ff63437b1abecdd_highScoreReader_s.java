 package highscoreData;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Reads the highscore information from highscore database(external text file)
  * @author Haisin Yip
  */
 public class highScoreReader
 {
     // private properties
     private Scanner fileScanner;
     private String path;
     private File file;
     private final String delims = "Ħ";
     
     // initializes the path and a file
     public highScoreReader(String path)
     {
         this.path = path;
         this.file = new File(path);
     }
     
     // each row contains a player's score summary
     // returns the list of all player scores in a 2D array
     public String[][] readFile()
     {
         this.openFile();
         ArrayList<String[]> scoreList = new ArrayList<String[]>();
         try
         {
             // if file exists, read from it
             if(file.exists())
             {
                 while(fileScanner.hasNextLine())
                 {   
                     String scoresLine = fileScanner.nextLine();
                     String[] scores  = scoresLine.split(delims);
                    for(int i = 0 ; i < scores.length ; i++)
                    {
                        System.out.println(scores[i]);
                        if(i == scores.length - 1)
                        {
                            System.out.println("end of array");
                        }
                    }
                     scoreList.add(scores);
                 }
                 fileScanner.close();
                 return to2Darray(scoreList);
             }
             
             // if it does not exist, abort
             else
             {
                 fileScanner.close();
                 return null;
             }
         }
         
         catch(Exception e)
         {
             e.printStackTrace();
         }
         fileScanner.close();
         return to2Darray(scoreList);
     }
     
     // turns arraylist of string arrays into 2d string array
     public static String[][] to2Darray(ArrayList<String[]> list)
     {
         String[][] arr = new String[list.size()][8];
         for(int i = 0 ; i < list.size() ; i++)
         {
             for(int j = 0 ; j < 8 ; j++)
             {
                 System.out.println(arr[i][j]);
                 arr[i][j] = list.get(i)[j];
             }
         }
         return arr;
     }
     
     // open file for usage
     public void openFile()
     {
         
         if(file.exists())
         {
             try
             {
                 fileScanner = new Scanner(new File(path));
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
         // else if there is no file to read from, create a new blank file
         else
         {
             file = new File(path);
             try {
                 file.createNewFile();
             } catch (IOException ioexception) {
                 ioexception.printStackTrace();
             }
         }
     }
     
     // close file for end of usage
     public void closeFile()
     {
         fileScanner.close();
     }
 }
