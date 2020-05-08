 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class You extends Player
 {	
 	private ArrayList<Integer> iOptions;
 	
 	public void Play(Deck d, Scanner sc, int r)
 	{
 		handWillPlay = 0;
 		int choice;
 		iOptions = new ArrayList<Integer>();
 		//if (r == 0)
 		//{
 		//	initHand(d);
 		//}
 		
 		for ( handWillPlay = 0; handWillPlay < hand.size(); handWillPlay++ )
 		{
			if ( !(hand.get(handWillPlay).getHand().get(0).getFace() == Rank.ACE && handWillPlay > 0) )
 			{
 				String options = GenerateOptions(r);
 				
 				System.out.println("Now it's your turn!");
 				if (r == 0 && !hand.get(handWillPlay).madeFromSplit)
 				{
 					GetBet(sc);
 				}
 				
 				System.out.println(toString());
 				System.out.println("Hand number: " + (handWillPlay + 1));
 				System.out.println("Number of hands you have: " + hand.size());
 				System.out.println("Hand Value: " + BlackJackCardValues.getCombinedValuesOfCardHand(hand.get(handWillPlay).getHand()));		
 				
 				System.out.println("Choose from the following options");
 				System.out.println(options);
 				
 				choice = GetOption(sc);
 				
 				switch (choice)
 				{
 					case 0:
 						break;
 					case 1: if (!hit(d)){hand.get(handWillPlay).willPlay = false;}
 						break;
 					case 2: stand();
 						break;
 					case 3: surrender();
 						break;
 					case 4:	doubl( sc, d );
 						break;
 					case 5: split(d);
 						break;
 				}
 			}
 			else
 			{
				System.out.println(hand.get(handWillPlay).toString());
				System.out.println("Hand number: " + (handWillPlay + 1));
 				System.out.println("Hand was skipped because it had an ace when it was split");
 			}
 		}
 	}	
 	
 	private String GenerateOptions(int ir)
 	{
 		String r = new String();
 		
 		boolean addHit = false;
 		
 		for ( int i = 0; i < hand.size(); i++)
 		{
 			if (this.hand.get(i).isHandPlayable())
 			{
 				addHit = true;
 				break;
 			}
 		}
 		
 		if ( addHit )
 		{
 			r += "Hit - 1\n";
 			iOptions.add(1);
 		}
 		
 		r += "Stand - 2\n";
 		iOptions.add(2);
 		
 		if ( money >= hand.get(handWillPlay).getBet() && hand.get(handWillPlay).getHand().size() == 2)
 		{
 			r += "Surrender - 3\n";
 			iOptions.add(3);
 			r += "Double - 4\n";
 			iOptions.add(4);
 		}
 		
 		if (hand.get(handWillPlay).getHand().size() == 2)
 		{
 			if (//hand.size() == 1 && hand.get(handWillPlay).getHand().size() == 2 &&
 				hand.get(handWillPlay).getHand().get(0).getFace() == hand.get(handWillPlay).getHand().get(1).getFace() && money >= hand.get(handWillPlay).getBet() * 2)
 			{
 				r += "Split - 5\n";
 				iOptions.add(5);
 			}
 		}
 		return r;
 	}
 	
 	private int GetOption( Scanner sc )
 	{
 		int entered = 0;
 		
 		if ( hand.get(handWillPlay).isHandPlayable() )
 		{
 			do
 			{
 				System.out.print("choose corresponding number: ");
 				entered = sc.nextInt();
 			}
 			while ( !IsOptionAvailable(entered));
 		}
 		else
 		{
 			hand.get(handWillPlay).willPlay = false;
 		}
 		
 		return entered;
 	}
 	
 	private boolean IsOptionAvailable(int e)
 	{
 		boolean r = false;
 		for ( int i = 0; i < iOptions.size(); i++ )
 		{
 			if ( e == iOptions.get(i) )
 			{
 				r = true;
 				break;
 			}
 		}
 		return r;
 	}
 	
 	private void GetBet(Scanner sc)
 	{
 		int entered;
 		do
 		{
 			System.out.println("Bet must be between 1 and " + getMoney());
 			System.out.print("Enter Bet: ");
 			entered = sc.nextInt();
 		}
 		while ( !Bet(entered));
 		
 	}
 	
 	public boolean Lost()
 	{
 		return money <= 0;
 	}
 	
 	public void win(int h)
 	{
 		double num = 2;
 		if ( hand.get(h).isBlackJack() )
 		{
 			num = 2.5;
 		}
 		money += hand.get(h).getBet() * num;
 	}
 
 	public void draw(int h) 
 	{
 		money += hand.get(h).getBet();		
 	}
 }
