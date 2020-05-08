 package se.chalmers.kangaroo.model.creatures;
 
 import java.awt.Polygon;
 
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.model.utils.Position;
 
 public class MatryoshkaCreature implements Creature {
 	private final static int id = 118;
 	private Position pos;
 	private Position startPos;
 	private boolean isJumping = false;
 	private double verticalSpeed;
 	private double horizontalSpeed;
 	private int lifesLeft;
 	private Direction direction;
	private boolean fuckYouModel;
 
 	public MatryoshkaCreature(Position pos) {
 		startPos = pos;
 		this.pos = pos;
 		direction = Direction.DIRECTION_WEST;
 		lifesLeft = 2;
		this.fuckYouModel = false;
 	}
 
 	@Override
 	public boolean isKillable() {
 		return true;
 	}
 
 	@Override
 	public Polygon getPolygon() {
 		if (lifesLeft == 2) {
 			int polyX[] = { pos.getX() + 0, pos.getX() + 64, pos.getX() + 64,
 					pos.getX() + 0 };
 			int polyY[] = { pos.getY() + 0, pos.getY() + 0, pos.getY() + 96,
 					pos.getY() + 96 };
 			return new Polygon(polyX, polyY, 4);
 		} else if (lifesLeft == 1) {
 			int polyX[] = { pos.getX() + 0, pos.getX() + 44, pos.getX() + 44,
 					pos.getX() + 0 };
 			int polyY[] = { pos.getY() + 32, pos.getY() + 32, pos.getY() + 96,
 					pos.getY() + 96 };
 			return new Polygon(polyX, polyY, 4);
 		} else {
 			int polyX[] = { pos.getX() + 0, pos.getX() + 32, pos.getX() + 32,
 					pos.getX() + 0 };
 			int polyY[] = { pos.getY() + 64, pos.getY() + 64, pos.getY() + 96,
					pos.getY() + 96};
 			return new Polygon(polyX, polyY, 4);
 		}
 	}
 
 	@Override
 	public void move() {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Decrease number of lifes and return true. If it has no lifes left, it
 	 * returns false.
 	 * 
 	 * @return
 	 */
 	public boolean decreaseLife() {
 		if (lifesLeft <= 0) {
 			return false;
 		} else {
 			lifesLeft--;
 			return true;
 		}
 	}
 	
 	public int getLife(){
 		return lifesLeft;
 	}
 
 	@Override
 	public void updateCreature() {
 		int tmp = (int) (Math.random() * 600);
 		if (tmp >= 580 && !isJumping) {
 			if((Math.random()*4) >= 3){
				fuckYouModel = true;
 				changeDirection();
				fuckYouModel = false;
 			}
 			isJumping = true;
 			verticalSpeed = -4;
 			if (getDirection() == Direction.DIRECTION_WEST) {
 				horizontalSpeed = -2;
 			} else {
 				horizontalSpeed = 2;
 			}
 		}
 		if (isJumping == true) {
 			jump();
 		}
 
 	}
 
 	private void jump() {
 		if (Math.abs(verticalSpeed - 4) > 10e-10) {
 			if(getDirection() == Direction.DIRECTION_WEST){
				pos = new Position(pos.getX() + (int) (horizontalSpeed),
 						(int) (pos.getY() + verticalSpeed + 0.5));
 			}else{
				pos = new Position(pos.getX() + (int) (horizontalSpeed),
 						(int) (pos.getY() + verticalSpeed + 0.5));
 			}
 			verticalSpeed += 0.1;
 		} else {
 			isJumping = false;
 		}
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 
 	@Override
 	public void changeDirection() {
		if(fuckYouModel){
			if (this.direction == Direction.DIRECTION_WEST) {
				this.direction = Direction.DIRECTION_EAST;
			} else {
				this.direction = Direction.DIRECTION_WEST;
			}
 		}
 	}
 
 	@Override
 	public Direction getDirection() {
 		return this.direction;
 	}
 
 	@Override
 	public void resetPosition() {
 		pos = startPos;
 		lifesLeft = 2;
 		verticalSpeed = 0;
 		horizontalSpeed = 0;
 		isJumping = false;
 	}
 
 }
