 package com.stevengharms.javacard;
 
 public class JavaCardApp{
 	private JavaCardDeck sourceDeck;
 	private JavaCardDeck correctDeck;
 	private JavaCardDeck incorrectDeck;
 	private JavaCardView view;
 	
 	public static  int magicNumber = 1;
 	public static String magicString = "Razzle";
 	
 	// Create an instance of the game
 	public JavaCardApp(){
 		// Model Objects
 		sourceDeck    = new JavaCardDeck();
 		correctDeck   = new JavaCardDeck();
 		incorrectDeck = new JavaCardDeck();
 		
 		// View Object	
 		view          = new JavaCardView();	
		view.setQuestion("Hello, Train Riding Fuckers!!");
 	}
 	
 	public static void main (String[] args)
 	{
 		JavaCardApp app = new JavaCardApp();
 	}
 }
