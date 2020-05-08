 // Example.java
 
 /*
 * Example opens the source files, calls the AStyleInterface methods
 * to format the files, and saves the reformatted source. The files
 * are in a test-j directory. The option mode=java must be included
 * for java files.
 */
 
 import java.io.*;
 
 public class Example
 {   public static void main(String[] args)
     {   // files to pass to AStyle
         String fileName[] =  { "AStyleDev/test-j/JSBeautifier.java",
                                "AStyleDev/test-j/JSFormatter.java" ,
                                "AStyleDev/test-j/JSLineBreaker.java" ,
                              };
 
         // options to pass to AStyle
         // mode=java is required for Java files
         String options = "style=java mode=java";
 
         // create an object
         AStyleInterface astyle = new AStyleInterface();
 
         // get Artistic Style version
         // does not need to terminate on an error
         String version = astyle.getVersion();
         if (version.length() != 0)
             System.out.println("Example Java - AStyle " + version);
 
         // process the files
         for (int i = 0; i < fileName.length; i++)
         {   // get the text to format
             String filePath = getProjectDirectory(fileName[i]);
             String textIn = getText(filePath);
 
             // call the Artistic Style formatting function
             // does not need to terminate on an error
             String textOut = astyle.formatSource(textIn, options);
             if (textOut.length() == 0)
             {   System.out.println("Cannot format "  + filePath);
                 continue;
             }
 
             // return the formatted text
             System.out.println("Formatted " + fileName[i]);
             setText(textOut, filePath);
         }
 
         return;
     }
 
     /*
     *  Error message function for this example.
     */
     private static void error(String why, String what)
     {   System.out.println(why + ' ' + what);
         System.out.println("The program has terminated!");
         System.exit(1);
     }
 
     /*
     * Prepend the project directory to the subpath.
     * This may need to be changed for your directory structure.
     */
     private static String getProjectDirectory(String subPath)
     {   String homeDirectory = System.getProperty("user.home");
         String projectPath = homeDirectory + "/Projects/" + subPath;
         return projectPath;
     }
 
     /*
     *  Get the text to be formatted.
     *  Usually the text would be obtained from an edit control.
     */
     private static String getText(String filePath)
     {   // create input buffers
         File inFile = new File(filePath);
        int readSize = 1024;
         StringBuffer bufferIn = new StringBuffer(readSize);
         char fileIn[] = new char[readSize];
 
         // read file data
         try
         {   BufferedReader in =
                 new BufferedReader(new FileReader(inFile));
             // use read to preserve the current line endings
             int charsIn = in.read(fileIn, 0, readSize);
             while (charsIn != -1)
             {   bufferIn.append(fileIn, 0, charsIn);
                 charsIn = in.read(fileIn, 0, readSize);
             }
             in.close();
         }
         catch (Exception e)
         {   if (e instanceof FileNotFoundException)
                 error("Cannot open input file", filePath);
             else if (e instanceof IOException)
                 error("Error reading file", filePath);
             else
                 error(e.getMessage(), filePath);
         }
 
         return bufferIn.toString();
     }
 
     /*
     *  Return the formatted text.
     *  Usually the text would be returned to an edit control.
     */
     private static void setText(String textOut, String filePath)
     {   // create a backup file
         String origfilePath = filePath +  ".orig";
         File origFile = new File(origfilePath);
         File outFile = new File(filePath);
         origFile.delete();                  // remove a pre-existing file
         if (!outFile.renameTo(origFile))
             error("Cannot create backup file", origfilePath);
 
         // write the output file - same name as input
         try
         {   BufferedWriter out =
                 new BufferedWriter(new FileWriter(filePath));
             out.write(textOut, 0, textOut.length());
             out.close();
         }
         catch (IOException e)
         {   error("Cannot write to output", filePath);
         }
     }
 
 }   // class Example
