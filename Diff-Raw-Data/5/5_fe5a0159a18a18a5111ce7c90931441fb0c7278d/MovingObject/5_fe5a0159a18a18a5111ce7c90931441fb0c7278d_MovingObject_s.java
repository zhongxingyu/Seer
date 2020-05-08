 package game;
 
 import game.gameplayStates.GamePlayState;
 
 public class MovingObject extends GameObject{
 	protected GamePlayState m_game;
 	protected static final int SIZE = 64;
 	protected static final int BUFFER = 14;
 	protected static final int RAD = 30;
 	protected float m_x, m_y;
 
 	
 	
 	public float getX() { return m_x; }
 	public void setX(float x) { m_x = x; }
 	public float getY() { return m_y; }
 	public void setY(float y) { m_y = y; }
 	
 	public MovingObject(GamePlayState game){
 		m_game = game;
 		this.setName("MovingObject");
 	}
 	public MovingObject(String name, GamePlayState game) {
 		super(name);
 		m_game = game;
 	}
 	/**
 	 * Moves the MovingObject in the given direction, with the given
 	 * delta.
 	 * @param dir
 	 * @param delta
 	 */
 	protected void move(Direction dir, int delta) {
 		if(dir==Direction.DOWN) {
 			if (!isBlocked(m_x, m_y + SIZE + delta * 0.1f, dir)) {
 	            m_y += delta * 0.1f;
 	        }
 		} else if(dir==Direction.LEFT) {
 			if (!isBlocked(m_x - delta * 0.1f, m_y, Direction.LEFT)) {
                 m_x -= delta * 0.1f;
             }
 		} else if(dir==Direction.RIGHT) {
 			if (!isBlocked(m_x + SIZE + delta * 0.1f, m_y, Direction.RIGHT)) {
                 m_x += delta * 0.1f;
             }
 		} else if(dir==Direction.UP) {
 			if (!isBlocked(m_x, m_y - delta * 0.1f, Direction.UP)) {
             	m_y -= delta * 0.1f;
             }
 		}
 	}
 	
 
 	
 	protected boolean isBlocked(float x, float y, Direction dir) {
     	switch(dir){
 			case UP: {
 				int xBlock1 = ((int)x +BUFFER) / SIZE;
 		        int yBlock = (int)y / SIZE;
 		        int xBlock2 = ((int)x + SIZE-BUFFER)/SIZE;
 		        return m_game.blocked(xBlock1, yBlock)|m_game.blocked(xBlock2, yBlock);
 			}
 			case DOWN: {
 				int xBlock1 = ((int)x +BUFFER) / SIZE;
 		        int yBlock = (int)y / SIZE;
 		        int xBlock2 = ((int)x + SIZE-BUFFER)/SIZE;
 		        return m_game.blocked(xBlock1, yBlock)|m_game.blocked(xBlock2, yBlock);
 			}
 			case LEFT: {
 				int xBlock = (int)x / SIZE;
 		        int yBlock1 = ((int)y +BUFFER)/ SIZE;
		        int yBlock2 = ((int) y +SIZE - BUFFER)/SIZE;
 		        return m_game.blocked(xBlock, yBlock1)||m_game.blocked(xBlock, yBlock2);
 			}
 			case RIGHT: {
 				int xBlock = (int)x / SIZE;
 		        int yBlock1 = ((int)y +BUFFER)/ SIZE;
		        int yBlock2 = ((int) y +SIZE - BUFFER)/SIZE;
 		        return m_game.blocked(xBlock, yBlock1)||m_game.blocked(xBlock, yBlock2);
 			} default: {
 				System.out.println("ERROR WHRE IS THIS " + dir + " ENUM COMING FROM");
 				return false;
 			}
 		}
 	}
 	@Override
 	public boolean isBlocking() {
 		return false;
 	}
 	@Override
 	public int[] getSquare() {
 		// TODO Auto-generated method stub
 		return new int[] {(int) (m_x/SIZE), (int) (m_y/SIZE)};
 	}
 	@Override
 	public Types getType() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	protected boolean checkCollision(MovingObject mo1, MovingObject mo2){
 		if(mo1.m_game!=mo2.m_game)
 			return false;
 		float xDist = mo1.getX()-mo2.getX();
 		float yDist = mo1.getY()-mo2.getY();
 		float radSum = 2*RAD;
 		return radSum*radSum>xDist*xDist+yDist*yDist;
 	}
 }
