 package uk.ac.aber.dcs.cs221.monstermash.data;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import uk.ac.aber.dcs.cs221.monstermash.util.Name;
 
 /**
  * 
  * @author Jacob Smith, jas32
  *
  */
 public class Monster {
 	public static final int MAX_NUM_CHILDREN = 10;
 	
 	private static volatile long nextPrimaryKey = 1;
 	
 	private volatile long primaryKey;
 	private volatile UserAccount owner;
 	
 	private volatile String name;
 	
 	public enum Gender { MALE, FEMALE };
 	private volatile Gender gender;
 	
 	private volatile Date dateOfBirth;
 	
 	protected volatile double ageRate;
 	protected volatile int strengthCoefficient;
 	protected volatile int evadeCoefficient;
 	protected volatile int toughnessCoefficient;
 	
 	protected volatile double fertility;
 	
 	
 	protected volatile int injuryChance;
 	
 	protected static Name nameGen;
 	
 	private volatile int injuries;
 	
 	/**
 	 * Public constructor.
 	 * 
 	 * Assigns the next available primary key and initialises the date of
 	 * birth field to this instant.
 	 */
 	public Monster() {
 		synchronized (getClass() ) {
 			primaryKey = nextPrimaryKey++;
 			if (nameGen == null) { nameGen = new Name(); }
 		}
 		
 		dateOfBirth = new Date();
 		
 		injuries = 0;
 	}
 	
 	/**
 	 * 
 	 * @return This monster's primary key.
 	 */
 	public long getUID() { return primaryKey; }
 	
 	/**
 	 * 
 	 * @param owner The monster's owner.
 	 * @return This monster object.
 	 */
 	public synchronized Monster setOwner(UserAccount owner) { this.owner = owner; return this; }
 	/**
 	 * 
 	 * @return The monster's owner.
 	 */
 	public UserAccount getOwner() { return owner; }
 	
 	/**
 	 * Customise a monster with a name.
 	 * @param name The monster's new name.
 	 * @return The monster object.
 	 */
 	public synchronized Monster setName(String name) { this.name = name; return this; }
 	/**
 	 * 
 	 * @return The monster's name.
 	 */
 	public String getName() { return name; }
 	
 	/**
 	 * Sets the monster's gender and immediately gives the monster a random
 	 * gender appropriate name. @see Gender.
 	 * @param gender The monster's gender.
 	 * @return This monster object.
 	 */
 	public synchronized Monster setGender(Gender gender) {
 		this.gender = gender; 
 		this.setName((this.isMale() ) ? nameGen.male(new java.util.Random() ): nameGen.female(new java.util.Random() ) );
 		return this; }
 	/**
 	 * 
  	 * @return True iff this monster is male.
 	 */
 	public boolean isMale() { return (gender == Gender.MALE); }
 	/**
 	 * 
 	 * @return The monster's age in seconds.
 	 */
	public long getAge() {
		long instantOfBirth = dateOfBirth.getTime();
		long currentInstant = new Date().getTime();
		return currentInstant - instantOfBirth;
	}
 	/**
 	 * 
 	 * @return The monster's health, less than 1. If less than 0 this indicates the monster should be reaped.
 	 */
 	public double getHealth() {
 		return 2 - Math.exp(ageRate * getAge() ); //$2-e^{\lambda t}$
 	}
 	/**
 	 * 
 	 * @return The monster's strength score.
 	 */
 	public int getStrength() {
 		return (int) (strengthCoefficient*(Math.exp(ageRate * getAge() ) - 1 ) * getHealth());
 	}
 	/**
 	 * 
 	 * @return The monster's chance at evading an incoming blow.
 	 */
 	public int getEvade() {
 		return (int) (evadeCoefficient*(Math.exp(ageRate * getAge() ) - 1) * getHealth() );
 	}
 	/**
 	 * 
 	 * @return The monster's toughness score.
 	 */
 	public int getToughness() {
 		return (int) (toughnessCoefficient*(Math.exp(ageRate * getAge() ) - 1) * getHealth() );
 	}
 	
 	/**
 	 * Mutation function implemented using a binomial distribution with number of trials, n=93 and p=1/2.
 	 * @param The PRNG state object.
 	 * @return A random number with a binomial distribution on the interval (0,1)
 	 */
 	protected static double mutation(java.util.Random rand) {
 		final int ITERATIONS = 3;
 		final int WORDLENGTH = 31;
 		
 		int count = 0;
 		
 		for (int i = 0; i < ITERATIONS; ++i) {
 			int stream = rand.nextInt();
 			for (int mask = 1; mask >0; mask *= 2) {
 				count += (stream & mask)!=0 ? 1: 0; 
 			}
 		}
 		return (double)(count) / (WORDLENGTH*ITERATIONS);
 	}
 	
 	/**
 	 * Generates a new Monster populated with random attributes.
 	 * @return A new 'starter' Monster.
 	 */
 	public static Monster generateRandom() {
 		Monster monster = new Monster();
 		java.util.Random rand = new java.util.Random();
 		
 		monster.setGender((rand.nextBoolean() ) ? Gender.MALE: Gender.FEMALE);
 		
 		
 		
 		monster.ageRate = mutation(rand) * 1e-6f;
 		monster.strengthCoefficient = (int) (mutation(rand) * 50);
 		monster.toughnessCoefficient = (int) (mutation(rand) * 50);
 		monster.evadeCoefficient = (int) (mutation(rand) * 50);
 		
 		monster.fertility =  Math.abs(rand.nextDouble() );
 		monster.injuryChance = (int) (mutation(rand) * 20);
 		
 		return monster;
 		
 }
 	
 	
 	/**
 	 * Utility function for genetic crossover.
 	 * @param rand The PRNG state object.
 	 * @param a The first parent's gene.
 	 * @param b The second parent's gene.
 	 * @return One of the two genes, each with a 50/50 chance.
 	 */
 	protected static double crossover (java.util.Random rand, double a, double b) {
 		return (rand.nextBoolean() ) ? a: b;
 	}
 	
 	/**
 	 * Breeds two monsters together to produce a list of children. The children automatically belong
 	 * to the mother's owner.
 	 * @param father The father with which to breed.
 	 * @return A list of the new children produced.
 	 */
 	public ArrayList<Monster> breed (Monster father) {
 		java.util.Random rand = new java.util.Random(); 
 		int numChildren = (int) (Math.sqrt(this.fertility * father.fertility) * MAX_NUM_CHILDREN);
 		ArrayList<Monster> returnedChildren = new ArrayList<Monster>(numChildren);
 		
 		while (returnedChildren.size() < numChildren) {
 			Monster monster = new Monster();
 			
 			monster.setGender((rand.nextBoolean()) ? Gender.MALE: Gender.FEMALE);
 			monster.setOwner(this.getOwner() );
 			
 			monster.ageRate = crossover(rand, this.ageRate, father.ageRate);
 			monster.ageRate *= mutation(rand) + 0.5;
 			
 			monster.strengthCoefficient = (int) crossover(rand, this.strengthCoefficient, father.strengthCoefficient);
 			monster.strengthCoefficient *= mutation(rand) + 0.5;
 			
 			monster.evadeCoefficient = (int) crossover(rand, this.evadeCoefficient, father.evadeCoefficient);
 			monster.evadeCoefficient *= mutation(rand) + 0.5;
 			
 			monster.toughnessCoefficient = (int) crossover(rand, this.toughnessCoefficient, father.toughnessCoefficient);
 			monster.toughnessCoefficient *= mutation(rand) + 0.5;
 			
 			monster.fertility = crossover(rand, this.fertility, father.fertility);
 			monster.fertility *= mutation(rand) + 0.5;
 			
 			monster.injuryChance = (int) crossover(rand, this.injuryChance, father.injuryChance);
 			monster.injuryChance *= mutation(rand) + 0.5;
 			
 			returnedChildren.add(monster);
 		}
 		
 		return returnedChildren;
 		
 	}
 	
 
 }
