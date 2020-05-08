 package essentials.objects;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import essentials.core.Game;
 import essentials.enums.LetterEnum;
 
 
 /**
  * Brick pool class 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "brickpool")
 public class Brickpool extends BrickList {
 	
 	public Brickpool(){
 		super();
 	}
 	
 	/**
 	 * Constructor
 	 * @param aBrickpool
 	 */
 	public Brickpool( Brickpool aBrickpool ){
 		super( aBrickpool );
 	}
 	
 	/**
      * Gets a brick from brick pool
      * @param aLetter Letter to search for
      * @param remove Remove brick if true
      * @return Brick or null if no brick found inside pool
      */
     protected Brick getBrick(LetterEnum aLetter, boolean remove){    	   	
     	// search through all bricks
     	for( Brick b : bricks ){
     		if( b.getLetter() == aLetter ){    						
     			if( remove ) {
     				bricks.remove(b);
     			}
     			return b.clone();
     		}    		
     	}    	
     	return null;
     }
     
     /**
      * Gets a brick from brick pool (brick leaves in pool)
      * @param aLetter Letter to search for
      * @return Brick or null if no brick found inside pool
      */
     public Brick getBrick(LetterEnum aLetter) {
     	return getBrick(aLetter, false);
     }
     
     /**
      * Takes a brick from brick pool (removes brick from pool)
      * @param aLetter Letter to search for
      * @return Brick or null if no brick found inside pool
      */
     public Brick takeBrick(LetterEnum aLetter) {
     	return getBrick(aLetter, true);
     }
     
     /**
      * Takes n random bricks from pool (removes bricks from pool)
      * @param n
      * @return Bricklist
      */
     public BrickList takeRandomBricks(int n){
     	BrickList rBricklist = new BrickList();
     	for( int i = 0; i < n; i++ ){
     		rBricklist.add( takeRandomBrick() );
     	}
     	return rBricklist;
     }
     
     /**
      * Take a random brick from brickpool (removes bricks from pool)
      * @return Random brick
      */
     public Brick takeRandomBrick(){
     	Brick rBrick = new Brick();
     	if( bricks.size() > 0 ){
     		rBrick = get( Game.intRandom(0, bricks.size()) ).clone();
     		remove(rBrick);
     	}
     	return rBrick;
     }
     
     /**
 	 * Converts object to string
 	 */
 	public String toString(){
 		String rString= "";
 		for( Brick b : bricks ){
 			rString = rString.concat( "["
 					+ b.getLetter().toString() + "_"
 					+ b.getWeight() + "]" );
 		}
 		return rString;
 	}
 	
 	/**
 	 * Converts a word into a bricklist of bricks,
 	 * using bricks from pool
 	 * @param word Word. Uses . and * as commands for replacing bricks
 	 * 				with bricks already on game mpa or jokers with specific letter.
 	 * @return Bricklist with valid bricks from pool.
 	 */
 	public BrickList toValidBricks(String word){
 		return toValidBricks(word, true);
 	}
 	
 	/**
 	 * Converts a word into a bricklist of brick
 	 * using bricks from pool
 	 * @param word
 	 * @param useKeyChars if true chars like . and * are interpreted as commands not as letters
 	 * @return Bricklist with valid bricks from pool
 	 */
 	public BrickList toValidBricks(String word, boolean useKeyChars){
 		BrickList rBrickList = new BrickList();		
 		word = word.trim().toUpperCase();
 		boolean cReplace = false;
 		boolean cJoker = false;
 		
 		for( char c : word.toCharArray() ){
 			
 			if( c == '.' && useKeyChars ){
 				cReplace = true;
 			} 
 			else if( c == '*' && useKeyChars ){
 				cJoker = true;
 			}
 			else {
 				LetterEnum letter = LetterEnum.JOKER;
 				Brick b = new Brick();
 				
 				// get letter for brick
 				if( c != '*' ){
 					try{
 						
 						letter = LetterEnum.valueOf( String.valueOf(c) );
 						
 					} catch( IllegalArgumentException e ){
 						// Encoding error. Try different encodings for c
 						String s = String.valueOf(c);
 						
 						for( String ch : Charset.availableCharsets().keySet() ){
 			                byte[] bytes;
 			                try {
 			                	
 			                     bytes = s.getBytes(ch);
 			                     String l = new String( bytes, "UTF-8" );
 			                     letter = LetterEnum.valueOf(l);
 			                     
 			                } catch (IllegalArgumentException e1) {
 			                    continue;
 			                } catch (UnsupportedEncodingException e2) {
							};
 							break;
 						}
 						
 						
 					} catch(Exception e){
 						System.err.println( e.getMessage() );
 						return new BrickList();
 					}
 				}
 
 				// create brick
 				if( cReplace ){
 					b = new Brick( letter, -1 );
 				}
 				else if( cJoker ){
 					b = new Brick( letter, 0 );
 				}
 				else {
 					b = getBrick( letter );
 				}
 				
 				// check if brick was found
 				if( b != null ){
 					rBrickList.add(b);
 				}
 				cReplace = false;
 				cJoker = false;
 			}
 			
 		}
 		
 		return rBrickList;
 	}
 	
 	
 	/**
 	 * Loads the bricks from xml file
 	 * @param aBricksFile xml encoded file with brick information
 	 */
 	public static Brickpool loadBrickpool(String aBricksFile){
 		return (Brickpool) JAXB.unmarshal( new File(aBricksFile), Brickpool.class );		
 	}
 
 }
