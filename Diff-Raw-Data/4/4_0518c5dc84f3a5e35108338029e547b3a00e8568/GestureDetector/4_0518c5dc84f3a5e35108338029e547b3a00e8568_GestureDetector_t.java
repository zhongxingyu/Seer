 package fr.umlv.escape.gesture;
 
 import android.graphics.Point;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.jbox2d.common.Vec2;
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.world.EscapeWorld;
 
 /** Static class that allow to detect gestures and calculate forces that they represent
  */
 public class GestureDetector {
 	private final ArrayList<Point> pointList;
 	private final ArrayList<Gesture> gestureList;
 	private Ship playerShip;
 	Vec2 lastForce;
 	private final float SHOOT_SENSIBILITY;
 	private boolean mustShoot;
 	
 	/**
 	 * Enum that represent a gesture.
 	 */
 	public enum GestureType{
 		/**No gesture detected.
 		 */
 		NOT_DETECTED,
 		/**Bad gesture detected.
 		 */
 		NOT_GESTURE,
 	
 		HORIZONTAL_LEFT,
 		/**Horizontal right detected.
 		 */
 		HORIZONTAL_RIGHT,
 		/**Cheat code detected.
 		 */
 		CHEAT_CODE,
 		/**Stats detected.
 		 */
 		STATS
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public GestureDetector(){
 		this.pointList=new ArrayList<Point>();
 		this.gestureList=new ArrayList<Gesture>();
 		this.playerShip = null;
 		this.SHOOT_SENSIBILITY = 3;
 	}
 //	
 //	/** Detect if a point list represent a horizontal line left or right.
 //	 *  
 //	 * @param pointList the list of point used to detect the gesture.
 //	 * @return True if the gesture is recognized else false.
 //	 */
 //	private boolean isHorizontalLine(){
 //		Iterator<Point> iterPoint=pointList.iterator();
 //		Point firstPoint;
 //		Point previous;
 //
 //		if(!iterPoint.hasNext()){
 //			return false;
 //		}
 //		previous=iterPoint.next();
 //		firstPoint=previous;
 //		int numOfPoint=1;
 //		Point tmp;
 //		while(iterPoint.hasNext()){
 //			tmp=iterPoint.next();
 //			if(tmp.y<(firstPoint.y-marginErrorBack)		||
 //			   tmp.y>(firstPoint.y+marginErrorBack)){
 //				return false;
 //			}
 //			previous=tmp;
 //			numOfPoint++;
 //		}
 //		if(numOfPoint>minNumOfPoint){
 //			float forceX=(float)previous.x-firstPoint.x;
 //			if(forceX>0){
 //				this.lastDetected=GestureType.HORIZONTAL_RIGHT;
 //			}
 //			else{
 //				this.lastDetected=GestureType.HORIZONTAL_LEFT;
 //			}
 //			setLastForce(forceX,0f);
 //			return true;
 //		}
 //		return false;
 //	}
 //	
 //	/** Detect if a point list represent a cheat code.
 //	 *  
 //	 * @param pointList the list of point used to detect the gesture.
 //	 * @return True if the gesture is recognized else false.
 //	 */
 //	private boolean isCheatCode(){
 //		Iterator<Point> iterPoint=pointList.iterator();
 //		
 //		Point previous;
 //		int stateOfDetection=0;
 //		
 //		previous=iterPoint.next();
 //		Point firstPoint=previous;
 //		
 //		if((firstPoint.x<width-150) || (firstPoint.y<height-150)){
 //			return false;
 //		}
 //		int numOfPoint=1;
 //		int badPoint=0;
 //		Point tmp;
 //		while(iterPoint.hasNext()){
 //			tmp=iterPoint.next();
 //			switch (stateOfDetection){
 //			case 0: // detect straight line to the left
 //				if(numOfPoint<5){
 //					if(tmp.y>firstPoint.y + marginErrorBack){
 //						return false;
 //					}
 //				}
 //				if((tmp.x>previous.x)||(tmp.y<firstPoint.y - marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				if(tmp.y>firstPoint.y + marginErrorBack){
 //					stateOfDetection++;
 //					firstPoint=previous;
 //					numOfPoint=0;
 //				}
 //				break;
 //			case 1:  // detect back off
 //				if(numOfPoint<5){
 //					if(tmp.x>(firstPoint.x+marginErrorBack)){
 //						return false;
 //					}
 //				}
 //				if(tmp.y<previous.y || tmp.x<(firstPoint.x-marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				if(tmp.x>(firstPoint.x+marginErrorBack)){
 //					stateOfDetection++;
 //					numOfPoint=0;
 //					firstPoint=previous;
 //				}
 //				break;
 //			case 2:  // detect straight line to the right
 //				if((tmp.x<previous.x)||(tmp.y<firstPoint.y - marginErrorBack)||(tmp.y>firstPoint.y + marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				break;
 //			default:
 //				return false;
 //			}
 //			previous=tmp;
 //			numOfPoint++;
 //		}
 //		if(stateOfDetection==2 && numOfPoint>5){
 //			lastDetected=GestureType.CHEAT_CODE;
 //			setLastForce(0f,0f);
 //			return true;
 //		}
 //		return false;
 //	}
 //	
 //	/** Detect if a point list represent a stat gesture.
 //	 *  
 //	 * @param pointList the list of point used to detect the gesture.
 //	 * @return True if the gesture is recognized else false.
 //	 */
 //	private boolean isStats(){
 //		Iterator<Point> iterPoint=pointList.iterator();	
 //		Point previous;
 //		int stateOfDetection=0;
 //		
 //		previous=iterPoint.next();
 //		Point firsPoint=previous;
 //		int numOfPoint=1;
 //		int badPoint=0;
 //		Point tmp;
 //	
 //		while(iterPoint.hasNext()){
 //			tmp=iterPoint.next();
 //			switch (stateOfDetection){
 //			case 0:  // detect straight line to the right
 //				if(numOfPoint<5){
 //					if(tmp.y>firsPoint.y + marginErrorBack){
 //						return false;
 //					}
 //				}
 //				if((tmp.x<previous.x)||(tmp.y<firsPoint.y - marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				if(tmp.y>firsPoint.y + marginErrorBack){
 //					stateOfDetection++;
 //					firsPoint=previous;
 //					numOfPoint=0;
 //				}
 //				break;
 //			case 1:  // detect back off
 //				if(numOfPoint<5){
 //					if(tmp.x<(firsPoint.x-marginErrorBack)){
 //						return false;
 //					}
 //				}
 //				if(tmp.y<previous.y || tmp.x>(firsPoint.x+marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				if(tmp.x<(firsPoint.x-marginErrorBack)){
 //					stateOfDetection++;
 //					numOfPoint=0;
 //					firsPoint=previous;
 //				}
 //				break;
 //			case 2:  // detect straight line to the left
 //				if((tmp.x>previous.x)||(tmp.y<firsPoint.y - marginErrorBack)||(tmp.y>firsPoint.y + marginErrorBack)){
 //					badPoint++;
 //					if(badPoint>10){
 //						return false;
 //					}
 //				}
 //				break;
 //			default:
 //				return false;
 //			}
 //			previous=tmp;
 //			numOfPoint++;
 //		}
 //		if(stateOfDetection==2 && numOfPoint>5){
 //			lastDetected=GestureType.STATS;
 //			setLastForce(0f,0f);
 //			return true;
 //		}
 //		return false;
 //	}
 	
 	/**
 	 * Try to detect a gesture. You can get the gesture detected by calling {@link #getLastGestureDetected()}.
 	 * @return true a gesture has been detected else false.
 	 */
 	public boolean detect(){
 		int size = gestureList.size();
 		
 		if(this.playerShip==null) {
 			this.playerShip = Game.getTheGame().getPlayer1().getShip();
 		}
 		System.out.println(gestureList.size());
 		System.out.println(pointList.size());
 		System.out.println(this.playerShip);
 		
 		if(mustShoot){
 			//TODO tirer
 			
 			mustShoot = false;
 			return false;
 		}
 		
 		for(int i = 0; i < size; i++){
 			Gesture g = gestureList.get(i);
 			System.out.println(g.getClass().toString());
 			if(g.isRecognized(pointList)){
 				System.out.println("RECOGNIZED");
 				System.out.println(g.getClass().toString());
 				g.apply(this.playerShip);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Add a point to the gesture detector. When the method {@link #detectGesture()} will be called
 	 * it will uses all the point added to detect a gesture.
 	 * @param point
 	 * @return true if the point could have been added else false.
 	 */
 	public boolean addPoint(Point point){
 		Objects.requireNonNull(point);
		//If it is not the first point, the ship must follow the points
 		if(this.pointList.size()!=0){
 			Point last = pointList.get(pointList.size());
 			Vec2 force = new Vec2(point.x-last.x,point.y-last.y);
			
			//If the distance between two point is too high it is a shoot
 			if(force.x < SHOOT_SENSIBILITY){
 				this.playerShip.body.setLinearVelocity(force);
 			} else {
 				this.mustShoot = true;
 			}
 		}
 		return this.pointList.add(point);
 	}
 	
 	public boolean addGesture(Gesture gesture){
 		Objects.requireNonNull(gesture);
 		return this.gestureList.add(gesture);
 	}
 	
 	/**
 	 * Get the list of point added to the gesture detector.
 	 * @return The list of point added to the gesture detector.
 	 */
 	public List<Point> getListPoint(){
 		return this.pointList;
 	}
 	
 	/**
 	 * Clear the gesture detector to an empty state with no point added and nothing detected.
 	 */
 	public void clear(){
 		this.pointList.clear();
 		if(this.lastForce != null){
 			this.lastForce.x = 0;
 			this.lastForce.y = 0;
 		}
 	}
 	
 	public Vec2 getLastForce() {
 		return lastForce;
 	}
 }
