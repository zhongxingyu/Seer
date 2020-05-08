 package antColony;
 
 /**
  * A class that represents the resources that a player has. Only one instance of this 
  * class should be created in each ant colony board.
  * @author Nicolas Marquez
  */
 public class Resources {
 
 	private int nbLarvae;
 	private int nbFood;
 	private int nbStone;
 	private int nbEarth;
 
 	/**
 	 * Builds a Resources object and initializes the number of each
 	 * resource according to the rules of the game (1 larva, 0 of each of the 
 	 * other resources)
 	 */
 	public Resources() {
 		// attributes
 		nbLarvae = 1;
 		nbFood = 0;
 		nbStone = 0;
 		nbEarth = 0;
 	}
 
 	/**
 	 * Build an object Resources. The number of each attribute is given.
 	 * 
 	 * @param nbLarvae
 	 * @param nbFood
 	 * @param nbPierre
 	 * @param nbEarth
 	 */
 	public Resources(int nbLarvae, int nbFood, int nbPierre, int nbEarth) {
 		this.nbLarvae = nbLarvae;
 		this.nbFood = nbFood;
 		this.nbStone = nbPierre;
 		this.nbEarth = nbEarth;
 	}
 
 	/**
 	 * Return the number of larvae
 	 * 
 	 * @return nbLarvae
 	 */
 
 	public int getNbLarvae() {
 		return nbLarvae;
 	}
 
 	/**
 	 * Return the number of food resources.
 	 * 
 	 * @return nbFood: the number of food resources.
 	 */
 
 	public int getNbFood() {
 		return nbFood;
 	}
 
 	/**
 	 * Return the number of stone resources.
 	 * 
 	 * @return nbStone: the number of stone resources.
 	 */
 	public int getNbStone() {
 		return nbStone;
 	}
 
 	/**
 	 * Return the number of earth resources
 	 * 
 	 * @return nbEarth: the number of earth resources.
 	 */
 	public int getNbEarth() {
 		return nbEarth;
 	}
 
 	/** 
 	 * Set the number of larvae resources (on a pheromone/special tile)
 	 * @param nbLarvae the number of larvae resources
 	 */
 	public void setNbLarvae(int nbLarvae) {
 		this.nbLarvae = nbLarvae;
 	}
 
 	/**
 	 * Set the number of food resources (on a pheromone/special tile)
 	 * @param nbFood the number of food resources
 	 */
 	public void setNbFood(int nbFood) {
 		this.nbFood = nbFood;
 	}
 
 	/**
 	 * Set the number of stone resources (on a pheromone/special tile)
 	 * @param nbStone the number of stone resources
 	 */
 	public void setNbStone(int nbStone) {
 		this.nbStone = nbStone;
 	}
 
 	/**
 	 * Set the number of earth resources (on a pheromone/special tile)
 	 * @param nbEarth the number of earth resources
 	 */
 	public void setNbEarth(int nbEarth) {
 		this.nbEarth = nbEarth;
 	}
 
 	/**
 	 * Method that converts 3 larvae to 1 food resource. If you don't have
 	 * enough larvae, returns an error message.
 	 */
 	public void convertLarvae() {
 		if (this.nbLarvae >= 3) {
 			this.nbLarvae = this.nbLarvae - 3;
 			nbFood++;
 		} 
 		else {
 			System.out.println("A minimum of 3 larves is required to convert them in food");
 		}
 	}
 
 	/**
 	 * A method to add larvae
 	 */
 	public void addLarvae(int n) {
 		nbLarvae += n;
 	}
 
 	/**
 	 * A method to add food resources
 	 */
 	public void addFood(int n) {
 		nbFood += n;
 	}
 
 	/**
 	 * A method to add stones resources
 	 */
 	public void addStone(int n) {
 		nbStone += n;
 	}
 
 	/**
 	 * A method to add earth resources
 	 */
 	public void addEarth(int n) {
 		nbEarth += n;
 	}
 
 	/**
 	 * A method to delete larvae
 	 */
 	public void removeLarvae(int n){
 		if(n <= nbLarvae){
 			nbLarvae-=n;	
 		}
 
 		else System.out.println("insufficient larvae, cannot remove!");
 	}
 
 	/**
 	 * A method to delete food resources
 	 */
 	public void removeFood(int n) {
 		if(n <= nbFood){
 			nbFood -= n;	
 		}
 		else System.out.println("insufficient food, cannot remove!");
 	}
 
 	/**
 	 * A method to delete stone resources
 	 */
 	public void removeStone(int n) {
 		if(n <= nbStone) {
 			nbStone -= n;
 		}
 		else System.out.println("insufficient stone, cannot remove!");
 	}
 
 	/**
 	 * A method to delete earth resources
 	 */
 	public void removeEarth(int n) {
 		if(n <= nbEarth) {
 			nbEarth -= n;
 		}
 		else System.out.println("insufficient earth, cannot remove!");
 	}
 
 	public class Stock {
 		private final static int STORAGE_CAPACITY_SMALL = 4;
 		private final static int STORAGE_CAPACITY_LARGE = 6;
 		private final static int STORAGE_INCREASE_LEVEL = 2; //the ant colony level for which storage capacity is increased
 		private AntColonyBoard acb;
 		private int antColonyLevel;
 
 		private int nbResources() {
 			return nbFood + nbEarth + nbStone; //For Stock purposes, Larvae are not counted as resources.
 		}
 
 		public Stock(AntColonyBoard acb) {
 			this.acb = acb; 
 			antColonyLevel = acb.getColony().getEffectiveLevel();
 		}
 
 		/**
 		 * Checks if the stockroom is overstocked, by comparing the number of resources to the storage
 		 * capacity, taking into account the ant colony level.
 		 * @return true if the Stockroom is overstocked, false otherwise
 		 */
 		public boolean isOverstocked() {
 			if (antColonyLevel < STORAGE_INCREASE_LEVEL) {
 				return nbResources() > STORAGE_CAPACITY_SMALL;
 			}
 			else return nbResources() > STORAGE_CAPACITY_LARGE;
 		}
 
 		public void discardStock(int nbFood, int nbEarth, int nbStone) {
 			removeFood(nbFood);
 			removeEarth(nbEarth);
 			removeStone(nbStone);
 		}
 
 		/*
 		 * Precondition: This function should only be called during winter.
 		 * @param year This parameter represents the current year. It's here temporarily, 
 		 * until game turns have been implemented
 		 */
 		public void discardForWinter(int year) {
 			/*
 			 * The number of food resources to be discarded is 4 the first year, 5 the second year and 6 the third year.
 			 * -> number of food to be discarded = year + 3.
 			 * The number of soldiers that a player has reduces the number of food to be discarded.
 			 * -> number of food to be discarded = year + 3 - nbSoldiers.
 			 */
 			int nbFoodToDiscard = year + 3 - acb.getAnts().getNbSoldiers();
 
 			if(nbFoodToDiscard <= nbFood) {
 				removeFood(nbFoodToDiscard);
 			}
 
 			else{ 
 				acb.minusVictoryPoints(3*(nbFoodToDiscard-nbFood)); //minus 3 victory points for each missing food resource
 				removeFood(nbFood); //if there is insufficient food, remove ALL food and reduce victory points
 			}
 		}
 
 		public String toString(){
 			if (isOverstocked()){
 				return "Overstocked! Food = " +nbFood +" Earth = " +nbEarth + " Stone = " + nbStone;
 			}
 			else return "Food = " +nbFood +" Earth = " +nbEarth + " Stone = " + nbStone;
 		}
 	}


	public void discard(int food, int earth, int stone){
		removeFood(food);
		removeEarth(earth);
		removeStone(stone);
	}
 }
