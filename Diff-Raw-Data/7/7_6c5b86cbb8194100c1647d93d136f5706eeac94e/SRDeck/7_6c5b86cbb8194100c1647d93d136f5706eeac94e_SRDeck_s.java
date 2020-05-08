 /*-----------------------------------------
 Programmer: Taylor Krammen
 Class: CS 205 Software Engineering
 Project: Sorry! Boardgame Implementation
 -------------------------------------------*/
 
 import java.util.*;
 
 /*
 This class will implement the drawPile, discard Pile,
 and related functions for use in the Sorry! boardgame.
 */
 
 public class SRDeck {
 	
 	public ArrayList<SRCard> drawPile;
 	public ArrayList<SRCard> discardPile;
 	
 	//Adds new cards to the drawPile
 	
 	public SRDeck() {
 		drawPile = new ArrayList<SRCard>(45);
 		for(int i = 0; i<4; i++) {
 			drawPile.add(new SRCard(1));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(2));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(3));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(4));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(5));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(7));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(8));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(10));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(11));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(12));
 		}for(int i = 0; i<3; i++) {
 			drawPile.add(new SRCard(13));
 		}
 	}
 	
 	//Can be used to shuffle the discardPile so
 	//it can be re-used as the drawPile
 	
 	public void shuffle() {
 		Random rand = new Random();
 		for (int i = 0; i < discardPile.size(); i++) {
 			int j = rand.nextInt(discardPile.size());
 			SRCard temp = discardPile.get(i);
 			discardPile.set(i, discardPile.get(j));
 			discardPile.set(j, temp);			
 		}
 	}		
 	
 	//Will select the top card off of the drawPile and return it
 	
 	public SRCard drawCard() {
 		SRCard drawCard = drawPile.get(0);
 		drawPile.remove(0);
 		return drawCard;		
 	}
 	
 	//Will take the card drawn from the drawPile and put it 
 	//into the discardPile
 	
 	public SRCard discardPile(SRCard drawCard) {
 		discardPile = new ArrayList<SRCard>(45);
 		discardPile.add(drawCard);
 		return drawCard;
 	}
 	
 	//Check to see if drawPile is empty
 	//If so, will return true and will implement the shuffle function
	/*
 	public boolean isEmpty() {
		if (drawPile.size() = 0) {
 		return true;
 		}
 		else return false;
 	}
	*/
 }
