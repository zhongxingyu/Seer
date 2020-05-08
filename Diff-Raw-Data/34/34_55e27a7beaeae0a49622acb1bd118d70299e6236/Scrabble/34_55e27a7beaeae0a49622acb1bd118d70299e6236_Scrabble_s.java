// STUDENT_NAME:
// STUDENT_ID:
 
 import java.util.*;
 import java.io.*;
 
 public class Scrabble {
 
     static HashSet<String> myDictionary; // this is where the words of the dictionary are stored
 
     // you should not need to change this method
     // Reads dictionary from file
     public static void readDictionaryFromFile(String fileName) throws Exception {
         myDictionary=new HashSet<String>();
 
         BufferedReader myFileReader = new BufferedReader(new FileReader(fileName) );
 
         String word;
         while ((word=myFileReader.readLine())!=null) myDictionary.add(word);
 
 	myFileReader.close();
     }
 
 
 
     /* Arguments: 
         char availableLetters[] : array of characters containing the letters that remain available
         String wordToDate : Word assembled to date
      Behavior:
         Prints out all English words that start with wordToDate, combined with any number (including zero) of letters from availableLetters 
      Returns:
         Nothing
      */
     public static void printValidWords(char availableLetters[], String wordToDate) {
 	   
       if (this.myDictionary.contains(wordToDate)) System.out.println(wordToDate);
        for (int i = 0; i < availableLetters.length; i++){
             String newWordToDate = wordToDate + availableLetters[i];
            char[] newAvailableLetters = new char[availableLetters.length-1];
 
            // Create new array with the character removed
            for(int j = 0; j < availableLetters.length; j++){
                if (availableLetters[j] != availableLetters[i]) {
                    for(int w = 0; w < availableLetters.length; w++){
                        newAvailableLetters.add(availableLetters[j]);
                    }
                 }
             }
             printValidWords(newAvailableLetters, newWordToDate);
        }
     }
     
     
     /* main method
         You should not need to change anything here.
      */
     public static void main (String args[]) throws Exception {
        
     	// First, read the dictionary
     	try {
     	    readDictionaryFromFile("englishDictionary.txt");
         }
         catch(Exception e) {
             System.out.println("Error reading the dictionary: "+e);
         }
             
         // Ask user to type in letters
         BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in) );
         char letters[]; 
         do {
             System.out.println("Enter your letters (no spaces or commas):");
             
             letters = keyboard.readLine().toCharArray();
 
             // now, enumerate the words that can be formed
             printValidWords(letters, "");
         } while (letters.length!=0);
 
         keyboard.close();
     }
 }
