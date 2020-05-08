 package effect;
 import game.Event;
 import grid.Square;


 /**
  * Represents an effect that 'effects' a square.
  * An effect is calculated according to power failures or active light grenades.
  * 
  * @author 	Groep 8
  * @version February 2013
  */
 public abstract class Effect implements Event{
 	
 	private Square square;
 	
 	/**
 	 * @return the square
 	 */
 	public Square getSquare() {
 		return square;
 	}
 
 	/**
 	 * @param square the square to set
 	 */
 	public void setSquare(Square square) {
 		this.square = square;
 	}
 	
 	public abstract void linkEffect(Effect effect);
 	
 	public abstract void unlinkEffect(Effect effect);
 	
 	public abstract boolean canCombineWith(Effect effect);
 
 }
