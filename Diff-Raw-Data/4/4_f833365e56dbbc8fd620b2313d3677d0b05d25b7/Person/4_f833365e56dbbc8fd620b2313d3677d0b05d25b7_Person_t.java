 package week8;
 
 import week8.village.Village;
 
 public class Person {
 	
 	/**
 	 * The next number to allocate to a villager.
 	 */
 	private static int nextNumber = 7;
 	
 	/**
 	 * Your number is your name
 	 */
 	protected int number;
 	
 	/**
 	 * A notepad for noting down people's numbers.
 	 */
 	protected Notepad notepad = new Notepad();
 		
 	/**
 	 * Gives this villager a number as they enter the village
 	 */
 	protected void allocateNumber() {
 		this.number = nextNumber;
 		nextNumber++;
 	}
 	
 	/**
 	 * Welcome to the village.
 	 * @param village
 	 */
 	public void enterVillage() {
 		allocateNumber();
 		Village.INSTANCE.enter(this);
 	}
 	
 	/**
 	 * Sorry to see you go.
 	 * @param village
 	 */
 	public void leaveVillage() {
 		Village.INSTANCE.leave(this);
 	}	
 	
 	/**
 	 * Finds another villager
 	 * @param number
 	 * @return
 	 */
 	public Person find(int number) {
 		for (Person p : Village.INSTANCE.getOccupants()) {			
 			if (p != this) {
 				try {
 					// Ask them their number
 					int n = p.getNumber(this);
 					
 					// Write it down
 					notePerson(n, p);
 					
 					// Is this who I'm looking for?
 					if (n == number) {
 						return p;
 					}
 				} catch (UnsupportedOperationException ex) {
 					/*
 					 *  There's a grumpy prisoner that always refuses to tell people his own number: 
 					 *  "I am not a number, I am a free man!" he shouts.
 					 *  Just ignore him and keep looking ...
 					 */				
 				}
 			}
 		}
 		return null;
 	}	
 	
 	/**
 	 * Note down what number someone said they had
 	 * @param n
 	 * @param p
 	 */
 	protected void notePerson(int n, Person p) {
 		notepad.addPerson(n, p);
 	}
 	
 	/**
 	 * Give the numbers you've collected to another villager
 	 * @param other
 	 */
 	public void shareNotepad(Person other) {
 		other.addToNotepad(notepad);
 	}
 	
 	/**
 	 * Add numbers from another notepad into your notepad
 	 * @param other
 	 */
 	public void addToNotepad(Notepad other) {
 		notepad.addAll(other);
 	}
 	
 
 	/**
	 * Another villager (whosAsking) asks what this villager's number is
	 * @return this villager's number (or so we claim...)
 	 */
 	public int getNumber(Person whosAsking) {
 		return number;
 	}
 	
 	/**
 	 * Accuses this villager of being Number One.  
 	 * As Number One might have been masquerading as any other number, we need to catch
 	 * him in a lie -- having pretended to be two different numbers. 
 	 */
 	public void youAreNumberOne(int firstNumber, int secondNumber, Person whosAsking) {
 		// Failed accusation.
 		String msg = "";
 		if (firstNumber != number) {
 			msg = String.format("I never said I was %d! ", firstNumber);
 		} else {
 			msg = String.format("I never said I was %d! ", secondNumber);
 		}
 		throw new IllegalArgumentException(msg + "Are you mad, accusing an innocent villager like this? Medics, take this villager to the clinic!");
 	}
 
 }
