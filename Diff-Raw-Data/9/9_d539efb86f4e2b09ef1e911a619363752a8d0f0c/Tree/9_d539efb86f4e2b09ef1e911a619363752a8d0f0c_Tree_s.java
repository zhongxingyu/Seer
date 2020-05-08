 package edu.ucsb.cs56.S13.lab04.iamnearl;
 
 /**
  *
  *
  * @author Noah Malik
  * @version 4/28/13, S13, cs56
  * @see Tree
  */
public class Tree{
 
 	private String scientificName;
 	private int avgHeightMeters;
         
         /**Default Constructor sets the tree to a Joshua tree */
 	public Tree(){
 	    this.scientificName = "Yucca brevifolia";
 	    this.avgHeightMeters = 10;
 	}
 
 	/**
 	 * @param scientificName The Latin Genus and Species according to standart binomial nomenclature
 	 * @param avgHeightMeters The height that the tree is expected to grow
 	 */
 	public Tree(String scientificName, int avgHeightMeters) {
 	    this.scientificName = scientificName;
 	    this.avgHeightMeters = avgHeightMeters;
 	}
 
 	public void setScientificName(String scientificName) {
 	    this.scientificName = scientificName;
 	}
 
 	public void setAvgHeightMeters(int avgHeightMeters) {
 	    this.avgHeightMeters = avgHeightMeters;
 	}
 
 	/**
 	 * @return scientificName
 	 */
 	public String getScientificName() {
 	    return this.scientificName;
 	}
 
 	/**
 	 * @return avgHeightMeters
 	 */
 	public int getAvgHeightMeters() {
 	    return this.avgHeightMeters;
 	}
 
 	/**
 	 * Converts a Tree object into a string representation
 	 * Uses format "Tree Name: Height m"
 	 * e.g. for a Japanese Cherry
 	 * "Prunus serrulata, 10 m"
 	 * @return String representation of Tree
 	 */
 	public String toString() {
 	    return (this.scientificName
 		    + ": "
 		    + String.format("%d",this.avgHeightMeters)
 		    + " m");
 	}
 
 	/**
 	 * checks to see if two trees are equal
 	 * @param o another object to compare to
 	 * @return true if this tree is the same as the object o
 	 */
 	public boolean equals(Object o) {
 	    if(!(o instanceof Tree))
 		return false;
 	    Tree t = (Tree) o;
 	    return (this.getScientificName().equals(t.getScientificName())
 		    && this.getAvgHeightMeters() == t.getAvgHeightMeters());
 	}
 
         /**A main method to demonstrate the Tree class */
 	public static void main(String[] args) {
		return; // THIS STUBZ DOES NOTHING!!1!ONE!11!ONE
 	}
 
 }
