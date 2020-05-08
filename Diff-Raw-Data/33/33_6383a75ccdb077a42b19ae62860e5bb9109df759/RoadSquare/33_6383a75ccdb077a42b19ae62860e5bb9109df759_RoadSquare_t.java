 package se.chalmers.towerdefence.model;
 
 /**
  * A square that represents a part of a road where monsters can go. 
  * @author Julia, Jonathan, Emma, Oskar
  *
  */
 
 public class RoadSquare implements ISquare{
 	
 	private final int x;
 	private final int y;
 	private final int width;
 	private final int height;
 	
 	public RoadSquare(int x, int y, int squareHeight, int squareWidth){
         width = squareWidth;
         height = squareHeight;
 		this.x = x * width;
         this.y = y * width;
     }
 	
 //	public int[] getPosition(){
 //		return new int[] {x,y};
 //	}
 	
 	public int getX(){
 		return x;
 	}
 	
 	public int getWidth(){
 		return width;
 	}
 	
 	public int getY(){
 		return y;
 	}
 	@Override
 	public String toString(){
 		return "\n" + x + " " + y ;
 	}
 	
 	
 	public boolean isBlocked(){
 		return true;
 	}
 
 	@Override
 	public boolean isRoad() {
 		return true;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 }
