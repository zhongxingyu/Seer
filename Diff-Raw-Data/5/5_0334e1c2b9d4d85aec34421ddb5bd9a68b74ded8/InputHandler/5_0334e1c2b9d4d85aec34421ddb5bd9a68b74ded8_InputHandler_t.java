 package inputhandler;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 
 public class InputHandler implements KeyListener {
 
 	private List<Key> keys = new ArrayList<Key>();
 
 	static int PLAYER1_LEFT = KeyEvent.VK_A;
 	static int PLAYER1_RIGHT = KeyEvent.VK_D;
 	static int PLAYER1_UP = KeyEvent.VK_W;
 	static int PLAYER1_DOWN = KeyEvent.VK_S;
 	static int PLAYER1_ROTATEL = KeyEvent.VK_1;
 	static int PLAYER1_ROTATER = KeyEvent.VK_3;
 	static int PLAYER1_FIRE = KeyEvent.VK_2;
 
 	static int PLAYER2_LEFT = KeyEvent.VK_LEFT;
 	static int PLAYER2_RIGHT = KeyEvent.VK_RIGHT;
 	static int PLAYER2_UP = KeyEvent.VK_UP;
 	static int PLAYER2_DOWN = KeyEvent.VK_DOWN;
 	static int PLAYER2_ROTATEL =  KeyEvent.VK_J;
	static int PLAYER2_ROTATER = KeyEvent.VK_L;
	static int PLAYER2_FIRE = KeyEvent.VK_K;
	
 
 	public Key menu = new Key(keys);
 	public Key menuOff = new Key(keys);
 	public Key tab = new Key(keys);
 
 	public Key up1 = new Key(keys);
 	public Key down1 = new Key(keys);
 	public Key left1 = new Key(keys);
 	public Key right1 = new Key(keys);
 	public Key fire1 = new Key(keys);
 	public Key rotateR1 = new Key(keys);
 	public Key rotateL1 = new Key(keys);
 
 	public Key up2 = new Key(keys);
 	public Key down2 = new Key(keys);
 	public Key left2 = new Key(keys);
 	public Key right2 = new Key(keys);
 	public Key fire2 = new Key(keys);
 	public Key rotateR2 = new Key(keys);
 	public Key rotateL2 = new Key(keys);
 	public Key grenadeSpam = new Key(keys);
 
 	public void releaseAll() {
 		for (Key key : keys) {
 			key.down = false;
 		}
 	}
 
 	public void tick(double dt) {
 		for (Key key : keys) {
 			key.tick(dt);
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent ke) {
 		toggle(ke, true);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent ke) {
 		toggle(ke, false);
 	}
 
 	public void toggle(KeyEvent e, boolean pressed) {
 
 		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
 			menu.toggle(pressed);
 		if (e.getKeyCode() == KeyEvent.VK_2)
 			menuOff.toggle(pressed);
 
 		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
 			tab.toggle(pressed);
 
 		if (e.getKeyCode() == KeyEvent.VK_0)
 			grenadeSpam.toggle(pressed);
 
 		if (e.getKeyCode() == PLAYER1_UP)
 			up1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_DOWN)
 			down1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_LEFT)
 			left1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_RIGHT)
 			right1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_FIRE)
 			fire1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_ROTATER)
 			rotateR1.toggle(pressed);
 		if (e.getKeyCode() == PLAYER1_ROTATEL)
 			rotateL1.toggle(pressed);
 
 		if (e.getKeyCode() == PLAYER2_UP)
 			up2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_DOWN)
 			down2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_LEFT)
 			left2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_RIGHT)
 			right2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_FIRE)
 			fire2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_ROTATER)
 			rotateR2.toggle(pressed);
 		if (e.getKeyCode() == PLAYER2_ROTATEL)
 			rotateL2.toggle(pressed);
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 }
