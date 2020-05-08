 package de.htwg.monopoly.cards;
 
 import java.util.Collections;
 import java.util.Deque;
 import java.util.LinkedList;
 
 public class CommunityCardsStack implements ICardStack {
 	
 	private Deque<ICards> cards = new LinkedList<ICards>();
 	
 	public CommunityCardsStack() {
 		// Idee: For-Schleife ber ein bestimmtes FILE und dann pushOnTop() entweder hier oder im Controller
 		//TODO elemente und Inhalte (Texte)
 		//TODO generelle Frage: Werden die Karteninhalte am Anfang eingelesen, oder hardcodiert von vornerein drin?
 		// denn dann muss evtl die Fehlerbehandlung erweitert werden.
 	}
 
 	@Override
 	public ICards getNextCard() {
 		ICards tmp = cards.pollFirst();
 		cards.offerLast(tmp);
 		return tmp;
 	}
 	
 	@Override
 	public void pushOnTop(ICards newCard)
 	{
 		cards.push(newCard);
 	}
 	
 
 	@Override
	public void shuffle() { //TODO Randomseed bergeben und berhaupt mal blicken was der seed macht... Bisher geht der Test nur mit einem Element auf dem Stack
 		Collections.shuffle((LinkedList<ICards>) this.cards);
 	}
 
 }
