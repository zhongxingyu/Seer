 //********************************************************************
 //  PigLatin.java       Author: Lewis/Loftus/Cocking
 //
 //  Driver to exercise the PigLatinTranslator class.
 //********************************************************************
 import java.util.Scanner;
 public class PigLatin
 {
    //-----------------------------------------------------------------
    //  Reads sentences and translates them into Pig Latin.
    //-----------------------------------------------------------------
    public static void main (String[] args)
    {
       String sentence, result, another = "y";
       Scanner scan = new Scanner (System.in);
      PigLatinTranslator translator = new PigLatinTranslator();
       while (another.equalsIgnoreCase("y"))
       {
          System.out.println ();
          System.out.println ("Enter a sentence (no punctuation):");
          sentence = scan.nextLine();
          System.out.println ();
         result = translator.translate (sentence);
          System.out.println ("That sentence in Pig Latin is:");
          System.out.println (result);
          System.out.println ();
          System.out.print ("Translate another sentence (y/n)? ");
          another = scan.nextLine();
       }
    }
 }
 
