 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class Game {
     private Deck deck = new Deck();
     private ArrayList<Player> players = new ArrayList<Player>();
     private ArrayList<Card> tableCards = new ArrayList<Card>();
     private int smallBlind;
     private int dealerButton;
     private int pot;
     private int i;
     private int playerTurn;
     private int bet = 0;
     private int raise = 0;
     private Scanner in = new Scanner(System.in);
     private int numCalls = 0; // number of consecutive calls
     private int numChecks = 0; //number of consecutive checks
     private int numPlayers;
    
     public Game(ArrayList<Player> p ,Deck d, int sB, int bP)
     {
         
         players = p;
         smallBlind = sB;
         deck = d;
         dealerButton = bP;
         numPlayers = players.size();
         
     }
     
     
     public ArrayList<Card> flop()
     {
         tableCards.add(deck.getNext());
         tableCards.add(deck.getNext());
         tableCards.add(deck.getNext());
         
         return tableCards;
     }
 
     public Card turn()
     {
         deck.getNext(); //burn
         return deck.getNext();
     }
     
 
     public Card river()
     {
         deck.getNext(); //burn
         return deck.getNext();
     }
     public String toString()
     {
         String retVal = "";
         for(Card card:tableCards)
             retVal += card.toString();
         return retVal;
     }
     public int getPot()
     {
         return pot;
     }
     public void addToPot(int a)
     {
         pot+=a;
     }
     public void resetPot()
     {
         pot = 0;
         
     }
     public void anteUp(int t)
     {
     	players.get((t+1) % numPlayers).bet(smallBlind);
     	addToPot(smallBlind);
     	players.get((t+2) % numPlayers).bet(smallBlind*2);
     	addToPot(smallBlind*2);
     	bet = smallBlind*2;
     }
     public void start()
     {
 
         Scanner in = new Scanner(System.in); 
         String choice = null;
 
 
         
         ArrayList<Card> tableCards = new ArrayList<Card>();
 
         for (i=0;i < 2*players.size();i++)
             players.get((playerTurn + 1 + i) % numPlayers).deal(deck.getNext());
             
 
         System.out.println("\nHands:" + "\n" + "-----------");
         
         for(i=0; i < players.size(); ++i)
         System.out.println(players.get(i).getName() + " has " + players.get(i).getCard(0) + " " + players.get(i).getCard(1));
 
         
         System.out.println("\n" + players.get(dealerButton).getName() + " Deals");
         	
         
         anteUp(dealerButton);
         playerTurn = dealerButton + 3;
         System.out.println("\n\n\nPot: " + pot);
         sleep();
         System.out.println("\n" + "Round of betting.." + "\n");
         
         while (true)
         {   //modified version of betRound() for pre-flop scenario
             
             playerTurn = playerTurn % numPlayers;
         	askChoice();
         	if (numCalls == numPlayers || numChecks == numPlayers) //only this way pre-flop so big blind has a chance to raise
         		break;
         }
         
 
         
         
 
 
         //deal
         sleep();
         System.out.println("Here Comes the Flop!");
         
         
 
         
         //FLOP*********************************************************************
         sleep();
         
         tableCards = flop();
         
         displayCards(tableCards);
         
         sleep();
         sleep();
         
         System.out.println("\n" + "Round of betting.." + "\n");
         sleep();
         
         betRound();
         
         //TURN*********************************************************************
         tableCards.add(turn());
         
         System.out.println("The Turn");
         sleep();
         displayCards(tableCards);
         
         sleep();
         System.out.println("\n" + "Round of betting.." + "\n");
         sleep();    
         betRound();
         
         //RIVER*********************************************************************
         tableCards.add(river());
         
         System.out.println("The River");
         sleep();
         displayCards(tableCards);
 
         sleep();
         System.out.println("\n" + "Round of betting.." + "\n");
         sleep();
         betRound();
         System.out.println("Results:" + "\n" + "blah blah blah");
     }          
 
     
         
         
     void sleep()
     {
         try {
             Thread.currentThread().sleep(1000);
         } catch(Exception e){}
     }
     
     
     void askChoice()
     {   
         
         while (true) //I wrote a lot of this at 6:30AM. I hope it makes sense.
         {
         		if (players.get(playerTurn).getFold() == true)
         		{
         			playerTurn++;
         			break;  //skip if player is folded
         		}
         		System.out.println(players.get(playerTurn).getName() + "'s turn (Bank:" + players.get(playerTurn).getBank() + ")\nCurrent bet is " + bet);
                 System.out.print("Choose and press Enter\n----------------------\n");
                 if(bet == 0)
                 	System.out.println("C:Check\nB:Bet\nF:Fold"); //no bets
                 else if(bet != 0 && bet != players.get(playerTurn).getBet())
                 	System.out.println("C:Call\nR:Raise\nF:Fold"); //someone has bet
                 else
                 	System.out.println("C:Check\nR:Raise\nF:Fold"); //should only be reached on big blind with no raise pre-flop
     
                 String choice = in.nextLine();
                 //if B and there is no current bet, BET
                 if(bet == 0 && (choice.equals("B") || choice.equals("b")))
                 {
                    System.out.print("\nEnter bet amount: ");
                    bet = Integer.parseInt(in.nextLine());
            		   addToPot(players.get(playerTurn).bet(bet));
            		   System.out.println(players.get(playerTurn).getName() + " Bets " + bet + "\n");
                    System.out.println("Pot: " + getPot());
                    numChecks = 0;
                    playerTurn++;
                    break;
                 }
                 
                 //if C, check for bet. if no bet, CHECK. if bet, CALL
                 else if(choice.equals("C") || choice.equals("c"))
                 {
                 	if(bet == 0)
                 	{
                 		// do nothing, AKA check
                 		System.out.println(players.get(playerTurn).getName() + " Checks" + "\n");
                 		numChecks++;
                 		playerTurn++;
                 	}
                 	else
                 	{
                 		//call
                 		addToPot(players.get(playerTurn).call(bet));
                 		System.out.println(players.get(playerTurn).getName() + " Calls (" + bet + ")");
                         System.out.println("Pot: " + getPot() + "\n");
                         numCalls++;
                         playerTurn++;
                 	}
                 		break;
                 }
                 //if R, RAISE, but make sure raise > bet before applying
                 else if(bet > 0 && (choice.equals("R") || choice.equals("r")))
                 {
                     System.out.print("\nEnter raise amount: ");
                     raise = Integer.parseInt(in.nextLine());
                     if (raise > bet)
                     {
                     	players.get(playerTurn).raise(raise);
                     	addToPot(raise);
                        	bet=raise;
                     	System.out.println(players.get(playerTurn).getName() + " Raises to " + bet);
                         System.out.println("Pot: " + getPot() + "\n");
                    	numCalls=1;
                     	playerTurn++;
                     }
                     else
                     {
                     	System.out.println("Raise must be greater than current bet (" + bet + ")");
                     	askChoice();
                     }
                     	break;
                 }
                 else if(choice.equals("F") || choice.equals("f"))
                 {
                     players.get(playerTurn).fold();
                     playerTurn++;
                     numPlayers--;
                     break;
                 }
                 else
                 {
                 	System.out.println("Invalid Entry");
                 	askChoice();
                 }
 
         }
     }
     
     void displayCards(ArrayList<Card> c)
     {
         String retVal="";
         for(Card s : c)
         {
             retVal += s + " ";
         }
         System.out.println(retVal);
     }
     void betRound()
     {
     	bet = 0;
     	numCalls = 0;
     	numChecks = 0;
     	raise = 0;
     	playerTurn = dealerButton + 1;
         while (true)
         {
             
             playerTurn = playerTurn % numPlayers;
         	askChoice();
         	if (numCalls == numPlayers - 1 || numChecks == numPlayers) //only this way pre-flop so big blind has a chance to raise
         		break;
         }
     }
 }
