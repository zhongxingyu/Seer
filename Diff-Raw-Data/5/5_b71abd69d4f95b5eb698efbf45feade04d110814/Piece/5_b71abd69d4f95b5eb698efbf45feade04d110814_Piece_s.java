 package gameObjects;
 
 import utilities.PublicFunctions;
 
 /**
  *The basic piece object. Each piece represents a "piece" in the game.
  */
 public class Piece
 {
 	//<editor-fold defaultstate="collapsed" desc="CONSTANTS">
 	/**
	 *Used to indicate a piece value when it's in the Myserty 1 stage
 	 */
 	public static final int MYSTERY_STAGE_1 = -1;
 	/**
 	 * The mystery 2 stage follows the Mystery 1 stage.
	 *Used to indicate a piece value when it's in the Myserty 2 stage
 	 */
 	public static final int MYSTERY_STAGE_2 = -2;
 	/**
 	 *Used to indicate a piece value when it's in the Empty stage
 	 */
 	public static final int EMPTY = 0;
 	//</editor-fold>
 	
 	//<editor-fold defaultstate="collapsed" desc="MEMBER VARIABLES">
 	private int value;
 	private Type type;
 	private int horizontal;
 	private int vertical;
 	private boolean remove;
 	//</editor-fold>
 	
 	//<editor-fold defaultstate="collapsed" desc="ACCESSORS">
 	/**
 	 * @return the pieces value
 	 */
 	public int getValue()
 	{
             return value;
 	}
 	
 	/**
 	* @param value the value to set
 	*/
 	public void setValue(int value)
 	{
 		switch(value)
 		{
 			//makes the piece a mystery piece
 			case 8:
 				this.type = Type.MYSTERY1;
 				this.value = MYSTERY_STAGE_1;
 				break;
 			//makes the piece an empty piece
 			case EMPTY:
 				this.type = Type.EMPTY;
 				this.value = EMPTY;
 				break;
 			//makes the piece a set piece
 			default:
 				this.type = Type.SET;
 				this.value = value;
 		}
 	}
 
 	/**
 	 * @return the type of the piece
 	 */
 	public Type getType()
 	{
 		return type;
 	}
 
 	/**
 	 * @param type the type of the piece to set
 	 */
 	public void setType(Type type)
 	{
 		this.type = type;
                 
                 switch(type)
                 {
                     case EMPTY:
                         setValue(EMPTY);
                         break;
                     case MYSTERY1:
                         setValue(EMPTY);
                         break;
                     case MYSTERY2:
                         setValue(EMPTY);
                         break;
                 }
 	}
 	
 	/**
 	 * @return the horizontal
 	 */
 	public int getHorizontal()
 	{
 		return this.horizontal;
 	}
 	
 	/**
 	 * 
 	 * @return the vertical 
 	 */
 	public int getVertical()
 	{
 		return this.vertical;
 	}
         
 	/**
 	 * 
 	 * @return the remove boolean
 	 */
 	public boolean getRemove()
 	{
             return this.remove;
 	}
     
 	/**
 	 *
 	 * @param b
 	 */
 	public void setRemove(boolean b)
 	{
             this.remove = b;
     }
 	        
 	//</editor-fold>
 	
 	/**
 	 * An enumeration representing the type that a piece can be
 	 */
 	public enum Type
 	{
 		/**
 		 *
 		 */
 		MYSTERY1,
 		/**
 		 *
 		 */
 		MYSTERY2,
 		/**
 		 *
 		 */
 		SET,
 		/**
 		 *
 		 */
 		EMPTY
 	}
 	
 	/**
 	 * Constructs a piece object
 	 * @param type The type of the piece 
 	 * @param i The horizontal index
 	 * @param j The vertical index
 	 */
 	public Piece(Type type, int i, int j)
 	{
 		this.type = type;
 		this.horizontal = i;
 		this.vertical = j;
 		this.remove = false;
 		
 		switch(type)
 		{
 			case MYSTERY1:
 				value = MYSTERY_STAGE_1;
 				break;
 			case MYSTERY2:
 				value = MYSTERY_STAGE_2;
 				break;
 			case SET:
 				value = PublicFunctions.getRandomNumber();
 				break;
 			case EMPTY:
 				value = EMPTY;
 				break;
 		}
 	}
 	
 	/**
 	 *
 	 * @return
 	 */
 	@Override
 	public String toString()
 	{
 		switch(type)
 		{
 			case EMPTY:
 				return " ";
 			case MYSTERY1:
 				return "#";
 			case MYSTERY2:
 				return "@";
 			default:
 				return Integer.toString(this.getValue());
 		}
 	}
 
 	/**
 	 *
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 
 	}
 }
 
