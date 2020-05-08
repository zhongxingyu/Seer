 package entities;
 
 import interfaces.Constants;
 import interfaces.PlayerImages;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import javax.swing.JPanel;
 
 public class Being implements Constants, PlayerImages {
 
 	// Constants
 	final double PLAYER_FRAME_DELAY = 0.2;
 	/*--Variables--*/
 	private boolean left, right, up, down, pick;
 	private int x, y, dx, dy;
 	private int xSpeed = 4;
 	private int ySpeed = 6;
 	private boolean isAirborne = false;
 	private double frameNumber = 0;
 	private Block[][] currMap;
 	/*--End Variables--*/
 
 	// Default Constructor
 	public Being(Point spawnPoint, Block[][] currMap) {
 		this.x = spawnPoint.x * TILE_SIZE;
 		this.y = spawnPoint.y * TILE_SIZE;
 		this.currMap = currMap;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public int getDx() {
 		return dx;
 	}
 
 	public int getDy() {
 		return dy;
 	}
 
 	public int getxSpeed() {
 		return xSpeed;
 	}
 
 	public int getySpeed() {
 		return ySpeed;
 	}
 
 	public double getFrameNumber() {
 		return frameNumber;
 	}
 
 	public void setCurrMap(Block[][] currMap) {
 		this.currMap = currMap;
 	}
 
 	public boolean isLeft() {
 		return left;
 	}
 
 	// TODO Change the way dx and dy are set.
 	public void setLeft(boolean left) {
 		if (left == true) {
 			dx = -xSpeed;
 		} else if (!right) {
 			dx = 0;
 		}
 		this.left = left;
 	}
 
 	public boolean isRight() {
 		return right;
 	}
 
 	public void setRight(boolean right) {
 		if (right == true) {
 			dx = xSpeed;
 		} else if (!left) {
 			dx = 0;
 		}
 		this.right = right;
 	}
 
 	public boolean isUp() {
 		return up;
 	}
 
 	public void setUp(boolean up) {
 		if (up == true) {
 			dy = -ySpeed;
 		} else {
 			dy = ySpeed;
 		}
 		this.up = up;
 	}
 
 	public boolean isDown() {
 		return down;
 	}
 
 	public void setDown(boolean down) {
 		this.down = down;
 	}
 
 	public boolean isPick() {
 		return pick;
 	}
 
 	public void setPick(boolean pick) {
 		this.pick = pick;
 	}
 
 	public boolean isAirborne() {
 		return isAirborne;
 	}
 
 	public void draw(Graphics2D g2d, JPanel rootPane, Block[][] map) {
 		frameNumber += PLAYER_FRAME_DELAY;
 		if (((int) frameNumber) > 3 && !left && !right) {
 			frameNumber = 0;
 		} else if (left || right) {
 			if (((int) frameNumber) > 4) {
 				frameNumber = 0;
 			}
 		}
 		if (left) {
 			g2d.drawImage(walkLeftImages[(int) frameNumber], x, y, rootPane);
 		} else if (right) {
 			g2d.drawImage(walkRightImages[(int) frameNumber], x, y, rootPane);
 		} else { // Standing, jumping, or falling.
 			g2d.drawImage(standImages[(int) frameNumber], x, y, rootPane);
 		}
 		move(dx, dy);
 	}
 	/**
 	 * @return the "x" index in the 2D map array.
 	 */
 	public int getXMapIndex() {
 		return x / TILE_SIZE;
 	}
 
 	/**
 	 * @return the "y" index in the 2D map array.
 	 */
 	public int getYMapIndex() {
 		return y / TILE_SIZE;
 	}
 
 	// FIXME the move method kind of works, I feel like the conditions in the
 	// method might be too restrictive. Needs a little tweaking/debugging
 	// shouldnt be TOO much work.
 	// FIX ArrayIndexOutOfBounds for top and bottom of map.
 	// FIX Character gets stuck when hitting left border, handle x == -1
 	public void move(int xMove, int yMove) {
 		// Are we moving up or down?
 		if (yMove != 0) {// Move up and down
 			isAirborne = true; // In the air
 			/*----DOWN----*/
 			if (yMove > 0) {
 				// Are we above two blocks?
 				if (x % TILE_SIZE != 0) {
 					// Are either solid?
 					if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid
 							|| currMap[getXMapIndex() + 1][getYMapIndex() + 1].isSolid
 							&& getYMapIndex() + 1 <= NUM_CHUNKS) {
 						yMove = 0;
 					} else { // Neither are solid
 						y += yMove / Math.abs(yMove);
 						yMove += yMove / Math.abs(yMove);
 					}
 				} else { // Above only one block.
 					if (currMap[getXMapIndex()][getYMapIndex() + 1].isSolid
 							&& getYMapIndex() + 1 <= NUM_CHUNKS) {
 						yMove = 0;
 					} else { // Block is not solid.
 						y += yMove / Math.abs(yMove);
 						yMove += yMove / Math.abs(yMove);
 					}
 				}
 				/*----UP----*/
 			} else if (yMove < 0) { // Up
 				// Are we below two blocks?
 				if (x % TILE_SIZE != 0) {
 					// Are either solid?
 					try {
 						if (currMap[getXMapIndex()][getYMapIndex() - 1].isSolid
 								|| currMap[getXMapIndex() + 1][getYMapIndex() - 1].isSolid) {
 							yMove = 0;
 						} else { // Neither are solid
 							y += yMove / Math.abs(yMove);
 							yMove -= yMove / Math.abs(yMove);
 						}
 					} catch (ArrayIndexOutOfBoundsException e) {
 						if(getYMapIndex() - 1 < 0 && y >= 0) {
 							y += yMove / Math.abs(yMove);
 							yMove -= yMove / Math.abs(yMove);
						} else {
							yMove = 0;
 						}
 					}
 				} else { // below only one block.
 					try {
 						if (currMap[getXMapIndex()][getYMapIndex() - 1].isSolid) {
 							yMove = 0;
 						} else { // Block is not solid.
 							y += yMove / Math.abs(yMove);
 							yMove -= yMove / Math.abs(yMove);
 						}
 					} catch (ArrayIndexOutOfBoundsException e) {
 						if(getYMapIndex() - 1 < 0 && y >= 0) {
 							y += yMove / Math.abs(yMove);
 							yMove -= yMove / Math.abs(yMove);
						} else {
							yMove = 0;
 						}
 					}
 				}
 			}
 		} else { // Landed
 			isAirborne = false; // No longer Airborn
 		}
 		// Moving Left or Right
 		if (xMove != 0) {
 			// Checking left and right border.
 			if (x >= 0 && x <= (CHUNK_SIZE - 1) * TILE_SIZE) {
 				/*----RIGHT----*/
 				if (xMove > 0) {
 					if (getXMapIndex() + 1 < CHUNK_SIZE
 							&& currMap[getXMapIndex() + 1][getYMapIndex()].isSolid) {
 						xMove = 0;
 					} else { // Neither are solid
 						x += xMove / Math.abs(xMove);
 						xMove -= xMove / Math.abs(xMove);
 					}
 					/*----LEFT----*/
 				} else if (xMove < 0) {
 					if (getXMapIndex() - 1 >= 0
 							&& currMap[getXMapIndex() - 1][getYMapIndex()].isSolid) {
 						xMove = 0;
 					} else { // Neither are solid
 						x += xMove / Math.abs(xMove);
 						xMove -= xMove / Math.abs(xMove);
 					}
 				}
 			} else { // Hitting Border
 				// TODO Implement a new map when walking far right/left
 				xMove = 0; // Stop moving left or right.
 			}
 		}
 		// Are we still moving?
 		if (xMove != 0 || yMove != 0) {
 			move(xMove, yMove);
 		} else {
 			return; // Done Moving.
 		}
 	}
 }
