 package edu.gatech.oad.antlab.person;
 
 /**
  *  A simple class for person 2
  *  returns their name and a
  *  modified string 
  *
  * @author Vraj Patel
  * @version 1.1
  */
 public class Person2 {
     /** Holds the persons real name */
     private String name;
 	 	/**
 	 * The constructor, takes in the persons
 	 * name
 	 * @param pname the person's real name
 	 */
 	 public Person2(String pname) {
 	   name = pname;
 	 }
 	/**
 	 * This method should take the string
 	 * input and return its characters in
 	 * random order.
 	 * given "gtg123b" it should return
 	 * something like "g3tb1g2".
 	 *
 	 * @param input the string to be modified
 	 * @return the modified string
 	 */
 	private String calc(String input) {
 	  //Person 2 put your implementation here
 		char[] charArray = input.toCharArray();
 		int shuffle = input.length();
 
 		for (int i = 0; i < shuffle; i++) {
 
 			int index1 = (int) (Math.random() * charArray.length);
 			int index2 = (int) (Math.random() * charArray.length);
			char temp1 = charArray[index1];
			char temp2 = charArray[index2];
 
 			charArray[index2] = temp1;
 			charArray[index1] = temp2;
 		}
 
 		String newString = new String(charArray);
 
 		return newString;
 	}
 	/**
 	 * Return a string rep of this object
 	 * that varies with an input string
 	 *
 	 * @param input the varying string
 	 * @return the string representing the 
 	 *         object
 	 */
 	public String toString(String input) {
 	  return name + calc(input);
 	}
 }
