 package instruments;
 import javax.swing.JOptionPane;
 
 import errorChecking.FormatException;
 import errorChecking.ErrorDialogs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * Replaces an instrument in Mario Paint Composer
  * with another with unprecedented speed. I intend to add
  * more functionality to this later, though this is already
  * extremely useful.
  * @author RehdBlob
  * @version 1.06
  * @since 1.00
  * @since 2011.0802
  */
 public class InstrumentReplacerMain {
 
     /**
      * The hashtable that holds all of the different key-value pairs
      * for our instrument links.
      */
     public static Hashtable<String, Character> instruments =
             new Hashtable<String, Character>();
 
     /**
      * Starts the InstrumentReplacer program.
      * @param fileName The file that we are performing some instrument
      * replacement operation on.
      */
     public static void start(File fileName) {
         if(fileName==null)
             return;
 
         InstrumentReplacer replace = new InstrumentReplacer();
         putInstruments();
 
         try {
             Scanner file = new Scanner(fileName);
             String songText = file.nextLine();
             replace.setParams(songText,fileName);
             file.close();
         } catch(FileNotFoundException e) {
             ErrorDialogs.fileNotFound();
             e.printStackTrace();
             return;
         } catch(NoSuchElementException e) {
             ErrorDialogs.noText();
             e.printStackTrace();
             return;
         } catch(FormatException e) {
             ErrorDialogs.formatIssue();
             e.printStackTrace();
             return;
         }
 
         String combo = InstrumentRepDialog.showInstrumentRepDialog();
 
         if (combo == null)
             return;
 
         String change = combo.split(" ")[0];
         Character ch = instruments.get(change.toLowerCase());
         String changeTo = combo.split(" ")[1];
         Character chT = instruments.get(changeTo.toLowerCase());
 
         try {
             replace.writeOldFile();
         }
         catch (Exception e) {
             ErrorDialogs.unwritableFile();
             e.printStackTrace();
             return;
         }
         String newFile = replace.changeNew(ch, chT);
 
         try {
             PrintStream writer = new PrintStream(fileName);
             writer.print(newFile);
             writer.close();
         } catch (FileNotFoundException e) {
             ErrorDialogs.fileNotFound();
             e.printStackTrace();
             return;
         } catch (SecurityException e) {
             ErrorDialogs.unwritableFile();
             e.printStackTrace();
             return;
         } catch (Exception e) {
             ErrorDialogs.unanticipatedError();
             e.printStackTrace();
             return;
         }
         JOptionPane.showMessageDialog(null, "Complete!",
                 "Instrument Replacer", JOptionPane.INFORMATION_MESSAGE);
     }
 
     /**
      * This populates the hashtable we're using to get characters.
      * @since 1.00b
      * @since 2011.0802
      */
     private static void putInstruments() {
         String [] res = ("mario mushroom yoshi " +
                 "star flower gameboy dog cat " +
                 "pig swan face plane boat car " +
                "heart piranha coin shyguy boo delete").split("\\s");
         for(char ch = 'a'; ch < 'u'; ch++)
             instruments.put(res[ch-97], new Character(ch));
     }
 }
