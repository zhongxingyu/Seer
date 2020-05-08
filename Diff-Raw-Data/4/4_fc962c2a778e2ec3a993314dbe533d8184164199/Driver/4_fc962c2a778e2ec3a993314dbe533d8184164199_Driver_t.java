 package game;
 
 import java.util.Scanner;
 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		/////////// Makes new Person with name a ///////////
 
 		Scanner s = new Scanner(System.in);
 		System.out.println("Welcome to Decisions");
 		System.out.println("What is your name?");
 		String a = s.nextLine();
 		Person p = new Person(a);
 
 		/////////// Outputs stats generated randomly upon creation of Person p ///////////
 
 		System.out.println("These are your stats:");
 		System.out.println(p.getName() + ":");
 		System.out.println("You are male");
 		System.out.println("You are currently a child");
 		System.out.println("Charisma = " + p.getCharisma());
 		System.out.println("Intelligence = " + p.getIntelligence());
 		System.out.println("Strength = " + p.getStrength());
 		System.out.println("Wealth = " + p.getWealth());
 		System.out.println("Confidence = " + p.getConfidence());
 
 		s.nextLine();
 		
 		/////////// Makes new ChoiceStorage object cs ///////////
 
 		ChoiceStorage cs = new ChoiceStorage(p);
 
 		/////////// Makes new Choice object c ///////////
 
 		Choice c = cs.getNextChoice(p);
 
 		/////////// Makes new Outcome object o ///////////
 
 		Outcome o = new Outcome(true, c.getCharismaReq(),
 		c.getIntelligenceReq(), c.getStrengthReq(), c.getWealthReq(),
 		c.getConfindenceReq(), c.getAgeReq());
 
 		/////////// Runs the Game ///////////
 		
 		while (p.isAlive() == true){ 
 		c = cs.getNextChoice(p); // Makes c the next choice
 		c.print();  // Outputs the choice to the user 
 		int x = s.nextInt(); // Takes in the user's input and stores it in x
 		o=c.execute(x);  // Makes decision with x
 		o.updateAttributes(p); // Updates the players Attributes
 		}
 		
 		/////////// Prints Final stats ///////////
 
 		System.out.println("You Died.");
 		System.out.println("These are your final stats:");
 		System.out.println(p.getName() + ":");
 		System.out.println("Charisma = " + p.getCharisma());
 		System.out.println("Intelligence = " + p.getIntelligence());
 		System.out.println("Strength = " + p.getStrength());
 		System.out.println("Wealth = " + p.getWealth());
 		System.out.println("Confidence = " + p.getConfidence());
 
 	}
 
 }
