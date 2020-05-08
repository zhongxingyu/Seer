
 package hangman;
 
 import java.util.Scanner;
 
 public class Hangman {
 String name;
 String instructions = "This is the game of Hangman \n\n";
     
 
 
     
     public static void main(String[] args) {
 
         Hangman myGame = new Hangman();
         myGame.getName();
         myGame.displayHelp();
         AskForWord myAskForWord = new AskForWord();
         myAskForWord.displayBlankSpaces();
         Losses myLosses = new Losses();
        Losses.displayNumLosses();
         Wins myWins = new Wins();
        Wins.displaynumWins();
     }
     public void getName() {
         Scanner input = new Scanner(System.in);
         System.out.println("Enter Your Name: ");
         this.name = input.next();
 
     }
 
     private void displayHelp() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
