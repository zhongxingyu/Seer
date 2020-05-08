 package fr.iutvalence.java.mp.thelasttyper.client;
 
 import fr.iutvalence.java.mp.thelasttyper.client.data.Game;
import fr.iutvalence.java.mp.thelasttyper.client.util.WordsManagerSetup;
 
 
 /**
  * The test main class, will be used to test new classes and functions.
  * 
  * @author culasb
  * 
  * 
  */
 public class Main
 {
 
     /**
      * this function will be use at the beginning of the execution. This is the
      * main function. Args are the arguments of the function. Right now we don't
      * need them for testing
      * 
      * @param args
      *            arguments passed
      */
     public static void main(String[] args)
     {
        new Game(WordsManagerSetup.setup()).play();
     }
 
 }
