 package uk.ac.aber.dcs.cs221.monstermash.util;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Scanner;
 
 /**
 * Utility class for generating random names by gender.
  * @author Jacob Smith, jas32
  *
 * 
  */
 public class Name {
 
 	private ArrayList<String> maleNameList;
 	private ArrayList<String> femaleNameList;
 	private Random rand;
 	
 	
 	public Name() {
 		this.init();
 		rand = new Random();
 	}
 	
 	/**
 	 * Initialises the Name generator with a list of names. The data file expected is a public
 	 * record of first names given to newborns in Scotland in 2007.
 	 * 
 	 * Note! In order for this class to work properly the resource data/names.csv must be in the class path.
 	 * 
 	 * @return This name object.
 	 */
 	public Name init() {
 		maleNameList = new ArrayList<String>();
 		femaleNameList = new ArrayList<String>();
 			
 		Scanner in = new Scanner(this.getClass().getClassLoader().getResourceAsStream("data/names.csv") );
 		in.useDelimiter("[,\\s*]");
 		while (in.hasNext() ) {
 			try {
 				String s = in.next();
 				if (!s.equals("") ) {
 					maleNameList.add(s);
 				}
 				in.next();
 				in.next();
 				
 				s = in.next();
 				if (!s.equals("") ) {
 					femaleNameList.add(s);
 				}
 				//in.next();
 				in.next();
 				in.next();
 				
 			} catch (NoSuchElementException e) {}
 			
 		}
 		in.close();
 
 		return this;
 	}
 	
 	/**
 	 *  
 	 * @param male True if a male name is needed, else false.
 	 * @return A random name.
 	 */
 	protected String random (boolean male) {
 		ArrayList<String> list = (male) ? maleNameList: femaleNameList;
 		
 		return list.get(rand.nextInt(list.size() ) );
 	}
 	
 	/**
 	 * Wrapper for @see #random(boolean).
 	 * @return A male name.
 	 */
 	public String male() { return random(true); }
 	/**
 	 * Wrapper for @see #random(boolean).
 	 * @return A female name.
 	 */
 	public String female() { return random(false); }
 
 	public static void main (String[] args) {
 		Name name = new Name();
 		name.init();
 		
 		
 	}
 }
 
