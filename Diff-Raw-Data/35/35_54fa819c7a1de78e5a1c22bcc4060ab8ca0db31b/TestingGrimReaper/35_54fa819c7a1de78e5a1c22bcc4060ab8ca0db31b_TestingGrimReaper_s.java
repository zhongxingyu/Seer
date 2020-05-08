 package Testers.TestingCards;
 
 import Implementers.GameStateImplementer;
 import framework.cards.Card;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 19/05/12
  * Desc:
  */
 public class TestingGrimReaper {
 
     public static void main(String []args){
         ArrayList<Card> hand = new ArrayList<Card>();
 
         GameStateImplementer gameStateImplementer = new GameStateImplementer("testing");
 
         gameStateImplementer.setWhoseTurn(0);
 
         hand.add(Card.ONAGER);
         hand.add(Card.TEMPLUM);
         hand.add(Card.TEMPLUM);
         hand.add(Card.TEMPLUM);
         gameStateImplementer.setPlayerHand(0, hand);
 
         int[] num = {1,3,6};
         gameStateImplementer.setActionDice(num);
         gameStateImplementer.setPlayerSestertii(0, 1000);
         gameStateImplementer.setPlayerSestertii(1, 1000);
         gameStateImplementer.setPlayerVictoryPoints(0, 17);
         gameStateImplementer.setPlayerVictoryPoints(1, 17);
 
        Card[] diceDiscs1 = {Card.CENTURIO, Card.CENTURIO, Card.CENTURIO, Card.CENTURIO,
                Card.CENTURIO, Card.CENTURIO, Card.CENTURIO};
         gameStateImplementer.setPlayerCardsOnDiscs(0, diceDiscs1);
        Card[] diceDiscs2 = {Card.KAT, Card.CONSUL, Card.GRIMREAPER, Card.CONSUL,
                Card.CONSUL, Card.CONSUL, Card.SICARIUS};
         gameStateImplementer.setPlayerCardsOnDiscs(1, diceDiscs2);
 
         gameStateImplementer.printStats();
         gameStateImplementer.runGame();
 
         System.err.println(gameStateImplementer.getDiscard());
     }
 }
