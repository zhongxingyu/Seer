 package cards;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import cards.hazards.Accident;
 import cards.hazards.FlatTire;
 import cards.hazards.OutOfGas;
 import cards.hazards.SpeedLimit;
 import cards.hazards.Stop;
 import cards.remedies.EndOfLimit;
 import cards.remedies.Gasoline;
 import cards.remedies.GoRoll;
 import cards.remedies.Repairs;
 import cards.remedies.SpareTire;
 import cards.safeties.DrivingAce;
 import cards.safeties.ExtraTank;
 import cards.safeties.PunctureProof;
 import cards.safeties.RightOfWay;
 
 public class Deck {
 	
 	private List<Card> deck = new LinkedList<Card>();
 	
 	public Deck()
 	{
 		//ajout des Safeties
 		deck.add(new DrivingAce());
 		deck.add(new ExtraTank());
 		deck.add(new PunctureProof());
 		deck.add(new RightOfWay());
 		
 		//ajout d'Attaque
 		for(int i=0;i<3;i++)
 		{
 			deck.add(new Accident());
 			deck.add(new FlatTire());
 			deck.add(new OutOfGas());
 		}
 		//ajout d'Attaque et de Distance
 		for(int i=0;i<4;i++)
 		{
 			deck.add(new SpeedLimit());
 			deck.add(DistanceCard.get200km());
 		}
 		//ajout D'attaque
 		for(int i=0;i<5;i++)
 			deck.add(new Stop());
 		//ajout des reparations
 		for(int i=0;i<6;i++)
 		{
 			deck.add(new Repairs());
 			deck.add(new EndOfLimit());
 			deck.add(new Gasoline());
 			deck.add(new SpareTire());
 		}
 		//ajout de distances
 		for(int i=0;i<10;i++)
 		{
 			deck.add(DistanceCard.get25km());
 			deck.add(DistanceCard.get50km());
 			deck.add(DistanceCard.get75km());
 		}
 		//ajout de distances
 		for(int i=0;i<12;i++)
 			deck.add(DistanceCard.get100km());
 		//ajout de feu vert	
 		for(int i=0;i<14;i++)
 			deck.add(new GoRoll());
 	}
 	
 	public void shuffle() {
 		List<Card> deck2 = new LinkedList<Card>();
 		while (!this.deck.isEmpty()) {
			deck2.add(this.deck.get((int)Math.floor(Math.random() * this.deck.size())));
 		}
 		this.deck = deck2;
 	}
 	
 }
