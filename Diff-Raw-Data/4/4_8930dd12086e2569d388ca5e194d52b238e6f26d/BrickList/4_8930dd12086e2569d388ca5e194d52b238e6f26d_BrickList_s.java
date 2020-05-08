 package essentials.objects;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import essentials.enums.LetterEnum;
 import essentials.enums.OrientationEnum;
 import essentials.interfaces.Cloneable;
 
 /**
  * Class for generating a list of bricks.
  * Also contains parameters row, column and orientation
  * to use class to set bricks on game map.
  * @author hannes
  *
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "bricklist")
 public class BrickList implements Cloneable{
 
 	@XmlElement
 	protected List<Brick> bricks;
 	@XmlElement
 	protected int row;
 	@XmlElement
 	protected int col;
 	@XmlElement
 	protected OrientationEnum orientation;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public BrickList() {
 		this(0, 0, OrientationEnum.HORIZONTAL);
 	}
 	
 	/**
 	 * Constructor
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public BrickList(int aRow, int aColumn, OrientationEnum aOrientation){
 		row = aRow;
 		col = aColumn;
 		orientation = aOrientation;
 		bricks = new ArrayList<Brick>();
 	}
 	
 	/**
 	 * Constructor
 	 * @param aBricks
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public BrickList(List<Brick> aBricks, int aRow, int aColumn, OrientationEnum aOrientation){
 		this(aRow, aColumn, aOrientation);
 		bricks = aBricks;
 	}
 	
 	/**
 	 * Constructor
 	 * Clones another bricklist
 	 * @param aBrickList
 	 */
 	public BrickList(BrickList aBrickList){
 		this(
 				aBrickList.getBricks(),
 				aBrickList.getRow(),
 				aBrickList.getColumn(),
 				aBrickList.getOrientation()
 				);		
 	}
 	
 	/**
 	 * Get one brick a specific position
 	 * @param i
 	 * @return brick at position i (starting at position 0)
 	 */
 	public Brick get(int i){
 		return bricks.get(i);
 	}
 	
 	/**
 	 * @return size of bricklist
 	 */
 	public int size(){
 		return bricks.size();
 	}
 	
 	/**
 	 * @return row
 	 */
 	@XmlTransient
 	public int getRow(){
 		return row;
 	}
 	
 	/**
 	 * Set row
 	 * @param aRow
 	 */
 	public void setRow(int aRow){
 		row = aRow;
 	}
 	
 	/**
 	 * @return column
 	 */
 	public int getColumn(){
 		return col;
 	}
 	
 	/**
 	 * Set column
 	 * @param aColumn
 	 */
 	public void setColumn(int aColumn){
 		col = aColumn;
 	}
 	
 	/**
 	 * @return orientation
 	 */
 	@XmlTransient
 	public OrientationEnum getOrientation(){
 		return orientation;
 	}
 	
 	/**
 	 * Set orientation (horizontal or vertical)
 	 * @param aOrientation
 	 */
 	public void setOrientation(OrientationEnum aOrientation){
 		orientation = aOrientation;
 	}
 	
 	/**
 	 * Set position (row and column)
 	 * @param aRow
 	 * @param aColumn
 	 */
 	public void setPosition(int aRow, int aColumn)
 	{
 		setRow(aRow);
 		setColumn(aColumn);
 	}
 	
 	/**
 	 * Sets position (row, column and orientation)
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public void setPosition(int aRow, int aColumn, OrientationEnum aOrientation)
 	{
 		setRow(aRow);
 		setColumn(aColumn);
 		setOrientation(aOrientation);
 	}
 	
 	/**
 	 * @return list of bricks
 	 */
 	@XmlTransient
 	public List<Brick> getBricks(){
 		return bricks;
 	}
 	
 	/**
 	 * Sets a list of bricks
 	 * @param aBricks
 	 */
 	public void setBricks(List<Brick> aBricks){
 		bricks = aBricks;
 	}
 	
 	
 	/**
 	 * Add a brick to bricklist
 	 * @param aBrick
 	 */
 	public void add(Brick aBrick){
 		bricks.add(aBrick);
 	}
 	
 	/**
 	 * Adds a bricklist's bricks to this list
 	 * @param aBrickList
 	 */
 	public void add(BrickList aBrickList){
 		for( Brick b : aBrickList.getBricks() ){
 			add(b);
 		}
 	}
 	
 	/**
 	 * Remove a brick with one specific letter
 	 * @param aLetter
 	 */
 	public void remove(String aLetter){
 		for(Brick b : bricks){
 			if( b.getLetter().toString().equals(aLetter) ){
 				bricks.remove(b);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Remove a brick with one specific letter
 	 * from LetterEnum
 	 * @param aLetter
 	 */
 	public void remove(LetterEnum aLetter){
 		remove(aLetter.toString());
 	}
 	
 	/**
 	 * Remove a brick from list
 	 * ATTENTION: Only the letter is important,
 	 * not the weight of the brick!
 	 * @param aBrick
 	 */
 	public void remove(Brick aBrick){
 		if( aBrick.getWeight() >= 0 ){
 			
 			if( aBrick.getWeight() == 0 ){
 				aBrick.setLetter( LetterEnum.JOKER );
 			}
 			
 			remove( aBrick.getLetter() );
 		}
 	}
 	
 	/**
 	 * Removes a set of bricks from list
 	 * @param aBrickList
 	 */
 	public void remove(BrickList aBrickList){
 		for( Brick b : aBrickList.getBricks() ){
 			remove(b);
 		}
 	}
 	
 	public Brick get(String aLetter){
 		Brick rBrick = new Brick();
 		return rBrick;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter is inside list
 	 * @param aLetter
 	 * @return true if brick found, otherwise false
 	 */
 	public boolean contains(String aLetter){
 		for(Brick b : bricks){
 			if( b.getLetter().toString().equals(aLetter) ){
 				return true;
 			}
 		}		
 		return false;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter from LetterEnum
 	 * is inside list.
 	 * ATTENTION: Compares only the letters,
 	 * not the weight of the brick!
 	 * @param aLetter
 	 * @return true if brick found, otherwise false
 	 */
 	public boolean contains(LetterEnum aLetter){
 		return contains(aLetter.toString());
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter and weight is inside list
 	 * @param letter
 	 * @param weight
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(String letter, int weight){
 		for( Brick b : bricks ){
 			int bWeight = b.getWeight();
 			LetterEnum bLetter = b.getLetter();
 			
 			if( (bLetter.toString().equals(letter) && bWeight == weight)
 					|| (bLetter == LetterEnum.JOKER && weight == 0)
 					|| (weight < 0) ){
 				return true;
 			}			
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter from LetterEnum
 	 * is inside list.
 	 * @param aLetter
 	 * @param weight
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(LetterEnum aLetter, int weight){
 		return contains(aLetter.toString(), weight);
 	}
 	
 	/**
 	 * Checks if a brick is inside list.
 	 * @param aBrick
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(Brick aBrick) {
 		return contains(aBrick.getLetter(), aBrick.getWeight());
 	}
 	
 	/**
 	 * Checks if a set of bricks is in list
 	 * Checks letter AND weight!
 	 * @param aBrickList
 	 * @return true if all bricks are in list, otherwise false
 	 */
 	public boolean contains(BrickList aBrickList){
 		for( Brick b : aBrickList.getBricks() ){
 			if( !contains(b) ){
 				return false;
 			}
 		}		
 		return true;
 	}
 	
 	/**
 	 * Checks if there are enough bricks bricks in list to build a bricklist
 	 * @param aBricklist
 	 * @return True if there are enough bricks in list to build the bricklist
 	 */
 	public boolean containsN(BrickList aBricklist){
 		// check for suitable bricks
 		for( Brick b : aBricklist.getBricks() ){
 			if( !containsN(b, aBricklist.count(b)) ){
 				return false;
 			}
 		}
 		
 		// also check for needed jokers
 		if( countJokers() < aBricklist.countJokers() ){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Checks if list contains at least n specuific bricks
 	 * @param aBrick Brick to search for
 	 * @param n Number of min. needed bricks
 	 * @return True if at least n bricks are in list, otherwise false.
 	 */
 	public boolean containsN(Brick aBrick, int n){
 		if( n <= count(aBrick) ){
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean containsN(LetterEnum aLetter, int n){
 		if( n <= count( aLetter, 0 ) ){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if list contains at least n specific bricks with speicied letter an weight 
 	 * @param aLetter
 	 * @param aWeight
 	 * @param n Number of bricks needed
 	 * @return True if at least n bricks with that letter and weight are in list, otherwise false.
 	 */
 	public boolean containsN(LetterEnum aLetter, int aWeight, int n){
 		return containsN( new Brick(aLetter, aWeight), n );
 	}
 	
 	
 	/**
 	 * Counts the bricks with a specific letter and weight
 	 * @param aBrick Brick to serach for
 	 * @return Number of bricks with the same letter and weight
 	 */
 	public int count(Brick aBrick){
 		int n = 0;
 		for( Brick b : bricks ){
 			if( b.getWeight() >= 0 && b.equals(aBrick) ){
 				n++;
 			}
 		}
 		return n;
 	}
 	
 	/**
 	 * Counts the bricks with a specific letter
 	 * @param aLetter
 	 * @return
 	 */
 	public int count(LetterEnum aLetter){
 		int n = 0;
 		for( Brick b : bricks ){
 			if( b.getLetter() == aLetter ){
 				n++;
 			}
 		}
 		return n;
 	}
 	
 	/**
 	 * Counts the bricks with a specific letter and weight
 	 * @param aLetter Letter to search for
 	 * @return Number of bricks with that letter and weight
 	 */
 	public int count(LetterEnum aLetter, int aWeight){
 		return count( new Brick(aLetter, aWeight) );
 	}
 	
 	/**
 	 * Counts the bricks with a specific letter and weight
 	 * @param aLetter Letter to search for
 	 * @return Number of bricks with that letter and weight
 	 */
 	public int count(String aLetter, int aWeight){
 		LetterEnum letter = LetterEnum.valueOf(aLetter);
 		return count( new Brick(letter, aWeight) );
 	}
 	
 	/**
 	 * @return numer of jokers in list
 	 */
 	public int countJokers(){
 		int n = 0;
 		for( Brick b : bricks ){
 			if( b.getWeight() == 0 ){
 				n++;
 			}
 		}
 		return n;
 	}
 	
 	
 	/**
 	 * Converts object to string
 	 */
 	public String toString(){
 		StringWriter dataWriter = new StringWriter();
 		JAXB.marshal(this, dataWriter);
 		return dataWriter.toString();
 	}
 	
 	/**
 	 * Converts bricklist to a word
 	 * @return
 	 */
 	public String toWord(){
 		String word = "";
 		for(Brick b : bricks){
 			if(b != null)
 				word = word.concat( b.getLetter().toString() );
 		}
 		return word;
 	}
 	
 	/**
 	 * Calculates score from bricks in bricklist
 	 * @return score
 	 */
 	public int getScore(){
 		int score = 0;
 		for(Brick b : bricks){
			score += b.getWeight();
 		}
 		return score;
 	}
 	
 	/**
 	 * Converts a word into a list of bricks
 	 * @param word
 	 * @return List of bricks with no weight
 	 */
 	public static BrickList toBricks(String word){
 		BrickList bricks = new BrickList();
 		for( char c : word.toCharArray() ){
 			try{
 				LetterEnum letter = LetterEnum.valueOf( String.valueOf(c) );
 				bricks.add( new Brick(letter, 0) );
 			} catch(Exception e){
 				
 			}
 		}		
 		return bricks;
 	}
 	
 	/**
 	 * Calculates score for a set of bricklists
 	 * @param aBrickLists List of bricklists
 	 * @return score
 	 */
 	public static int getScore(List<BrickList> aBrickLists){
 		int score = 0;
 		for( BrickList bL : aBrickLists ){
 			if(bL.getScore() > 0){
 				score += bL.getScore();
 			}
 		}
 		return score;
 	}
 	
 	/**
 	 * Clones this object
 	 */
 	public BrickList clone(){
 		BrickList rBrickList = new BrickList();
 		rBrickList.setRow(row );
 		rBrickList.setColumn(col);
 		rBrickList.setOrientation(orientation);
 		
 		for(Brick b : bricks){			
 			rBrickList.add( new Brick(b.getLetter(), b.getWeight()) );			
 		}
 		
 		return rBrickList;
 	}
 	
 }
