 package com.cg.trashman.object;
 
 import static javax.media.opengl.GL2.GL_QUADS;
 
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 import java.util.List;
 
 import javax.media.opengl.GL2;
 
 import com.cg.trashman.ISimpleObject;
 import com.cg.trashman.Score;
 import com.jogamp.opengl.util.awt.TextRenderer;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureCoords;
 
 public class Car implements ISimpleObject {
 	private float pX;
 	private float pZ;
 	private float desX;
 	private float desZ;
 	private float pDefaultSpeed = 0.2000000000000000000000f;
 	private float pSpeed = pDefaultSpeed;
 	private Direction direction;
 	private Direction lastDirection;
 	private boolean[][] mazeGrid;
 	private int gridX;
 	private int gridZ;
 
 	private Texture[] textures;
 	private float textureTop;
 	private float textureBottom;
 	private float textureLeft;
 	private float textureRight;
 	private List<Trash> trashes;
 	public Score score;
 	private TextRenderer textScore;
 	private int lastScore;
 	private float scoreTrans;
 
 	enum Direction {
 		Stop, Up, Down, Left, Right
 	}
 
 	private float size;
 
 	public Car(boolean[][] mazeGrid, Texture[] textures, List<Trash> trashes) {
 		pX = 0f;
 		pZ = 0f;
 		desX = pX;
 		desZ = pZ;
 		direction = Direction.Stop;
 		lastDirection = Direction.Stop;
 		this.mazeGrid = mazeGrid;
 		gridX = 0;
 		gridZ = 0;
 
 		this.textures = textures;
 		TextureCoords textureCoords = textures[0].getImageTexCoords();
 		textureTop = textureCoords.top();
 		textureBottom = textureCoords.bottom();
 		textureLeft = textureCoords.left();
 		textureRight = textureCoords.right();
 
 		this.trashes = trashes;
 		score = Score.getInstance();
		score.reset();
 
 		textScore = new TextRenderer(new Font("SansSerif", Font.BOLD, 40));
 		lastScore = 0;
 		scoreTrans = 0f;
 		size = 0.85f;
 	}
 
 	public void updateMazePosition(int row, int col) {
 		desX = row * 2f;
 		desZ = col * 2f;
 	}
 
 	public void addMazePositionX(int dX) {
 		// out of range
 		if (gridX + dX >= mazeGrid.length || gridX + dX < 0) {
 			lastDirection = direction;
 			direction = Direction.Stop;
 			return;
 		}
 		if (mazeGrid[gridX + dX][gridZ]) {
 			lastDirection = direction;
 			direction = Direction.Stop;
 		} else {
 			gridX += dX;
 			desX += dX * 2f;
 
 			// check trash collision
 			updateTrashCollision();
 		}
 	}
 
 	public void addMazePositionZ(int dZ) {
 		// out of range
 		if (gridZ + dZ >= mazeGrid[0].length || gridZ + dZ < 0) {
 			lastDirection = direction;
 			direction = Direction.Stop;
 			return;
 		}
 		if (mazeGrid[gridX][gridZ + dZ]) {
 			lastDirection = direction;
 			direction = Direction.Stop;
 		} else {
 			gridZ += dZ;
 			desZ += dZ * 2f;
 
 			// check trash collision
 			updateTrashCollision();
 		}
 	}
 
 	@Override
 	public void update(GL2 gl, Object arg) {
 		// visibility of popup score
 		if (scoreTrans >= 0) {
 			scoreTrans -= 0.04f;
 		}
 		if (isStable()) {
 
 			// next move
 			if (direction == Direction.Left) {
 				addMazePositionX(-1);
 			} else if (direction == Direction.Right) {
 				addMazePositionX(1);
 			} else if (direction == Direction.Up) {
 				addMazePositionZ(1);
 			} else if (direction == Direction.Down) {
 				addMazePositionZ(-1);
 			}
 		}
 
 		if (Math.abs((this.pX - this.desX) * 1000f) / 1000f <= pSpeed) {
 			this.pX = this.desX;
 		} else {
 			this.pX += Math.signum(desX - pX) * pSpeed;
 		}
 		if (Math.abs((this.pZ - this.desZ) * 1000f) / 1000f <= pSpeed) {
 			this.pZ = this.desZ;
 		} else {
 			this.pZ += Math.signum(desZ - pZ) * pSpeed;
 		}
 
 		render(gl);
 		drawPopupScore();
 
 	}
 
 	public float getX() {
 		return pX;
 	}
 
 	public float getZ() {
 		return pZ;
 	}
 
 	private void drawPopupScore() {
 
 		// draw Popup Score
 		textScore.beginRendering(800, 600);
 		textScore.setColor(1f, 1f, 1f, scoreTrans);
 		textScore.draw("$" + lastScore, 380, 440 - (int) (scoreTrans * 50));
 		textScore.endRendering();
 	}
 
 	private void render(GL2 gl) {
 		// ----- Render the Color Cube -----
 		gl.glLoadIdentity(); // reset the current model-view matrix
 		// gl.glTranslatef(pX, 0, 0); // translate right and into the
 		// screen
 		gl.glTranslatef(0, 0, -pZ);
 		gl.glTranslatef(pX, 0, 0);
 		Direction movingDirection = direction;
 		if (direction == Direction.Stop)
 			movingDirection = lastDirection;
 		if (movingDirection == Direction.Up)
 			gl.glRotatef(-90f, 0f, 1f, 0f);
 		else if (movingDirection == Direction.Down)
 			gl.glRotatef(90f, 0f, 1f, 0f);
 		else if (movingDirection == Direction.Left)
 			gl.glRotatef(0f, 0f, 1f, 0f);
 		else if (movingDirection == Direction.Right)
 			gl.glRotatef(180f, 0f, 1f, 0f);
 
 		// side car (for front face)
 		textures[11].enable(gl);
 		textures[11].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Front Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glEnd();
 
 		// inverse front face
 		textures[11].enable(gl);
 		textures[11].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Back Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, -0.5f * size);
 		gl.glEnd();
 
 		// top car (for Top face)
 		textures[14].enable(gl);
 		textures[14].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Top Face
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glEnd();
 
 		// carBack
 		textures[13].enable(gl);
 		textures[13].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Right face
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, 0.5f * size);
 		gl.glEnd();
 
 		// carFront
 		textures[12].enable(gl);
 		textures[12].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Left Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glEnd();
 	}
 
 	public int getScore() {
 		return score.getScore();
 	}
 
 	public boolean isStable() {
 		return Math.abs((this.pX - this.desX) * 1000f) / 1000f <= pSpeed
 				&& Math.abs((this.pZ - this.desZ) * 1000f) / 1000f <= pSpeed;
 	}
 
 	public void updateTrashCollision() {
 		for (int i = 0; i < trashes.size(); i++) {
 			Trash t = trashes.get(i);
 			if (t.getRow() == gridX && t.getCol() == gridZ) {
 				// score on each id
 				int trashPoint = t.getScore();
 				score.add(trashPoint);
 				trashes.remove(t);
 
 				// update popup Score
 				lastScore = trashPoint;
 				scoreTrans = 1f;
 
 				// update Truck size
 				size = 0.85f + (score.getScore() / 15000f);
 				i--;
 			}
 		}
 	}
 
 	public void keyPressed(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.VK_W
 				|| event.getKeyCode() == KeyEvent.VK_UP) {
 			direction = Direction.Up;
 		}
 		if (event.getKeyCode() == KeyEvent.VK_S
 				|| event.getKeyCode() == KeyEvent.VK_DOWN) {
 			direction = Direction.Down;
 		}
 		if (event.getKeyCode() == KeyEvent.VK_A
 				|| event.getKeyCode() == KeyEvent.VK_LEFT) {
 			direction = Direction.Left;
 		}
 		if (event.getKeyCode() == KeyEvent.VK_D
 				|| event.getKeyCode() == KeyEvent.VK_RIGHT) {
 			direction = Direction.Right;
 		}
 	}
 
 	public void keyReleased(KeyEvent event) {
 	}
 
 	public void keyTyped(KeyEvent event) {
 
 	}
 }
