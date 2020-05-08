 package interaction;
 
 import game.Game;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import util.Vector2D;
 
 class Remover extends TimerTask {
 	int key;
 	HashMap<Integer, Remover> pressedKeys;
 	public Remover(int k, HashMap<Integer, Remover> pK) {
 		key = k;
 		pressedKeys = pK;
 	}
 	
 	@Override
 	public void run() {
 		pressedKeys.remove(key);
 	}
 }
 
 
 public class Input implements KeyListener, MouseListener, MouseMotionListener {
 
 	Game g;
 	Renderer r;
 	public Dimension size=null;
 	
 	public boolean mouse1down=false;
 	public boolean mouse2down=false;
 	public boolean mouse1clicked=false;
 	public boolean mouse2clicked=false;
 	public Vector2D cursorPos=new Vector2D(0,0);
 	
 	HashMap<Integer, Remover> pressedKeys;
 	Timer timer;
 	
 	public Input(JFrame frame, Renderer r) {
 		
 		this.r = r;
 		frame.getContentPane().addMouseMotionListener(this);
 		frame.getContentPane().addMouseListener(this);
 		frame.addKeyListener(this);
 		size = new Dimension();
 		size.width = ((JPanel)frame.getContentPane()).getWidth();
 		size.height = ((JPanel)frame.getContentPane()).getHeight();
 		pressedKeys = new HashMap<Integer, Remover>();
 		timer = new Timer();
 	}
 	
 	public boolean getMouse1Click() {
 		if(mouse1clicked) {
 			mouse1clicked = false;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean getMouse2Click() {
 		if(mouse2clicked) {
 			mouse2clicked = false;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isPressed(Integer key) {
 		return pressedKeys.containsKey(key);
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		if(arg0.getButton()==MouseEvent.BUTTON1) 
 			mouse1clicked = true;
 		else if(arg0.getButton()==MouseEvent.BUTTON3)
 			mouse2clicked = true;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 	
 		if(arg0.getButton()==MouseEvent.BUTTON1) 
 			mouse1down = true;
 		else if(arg0.getButton()==MouseEvent.BUTTON3)
 			mouse2down = true;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		if(arg0.getButton()==MouseEvent.BUTTON1) 
 			mouse1down = false;
 		else if(arg0.getButton()==MouseEvent.BUTTON3)
 			mouse2down = false;
 		
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		Point p = arg0.getPoint();
 		cursorPos = r.screenToWorld(p);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		Point p = arg0.getPoint();
 		cursorPos = r.screenToWorld(p);
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		int c = arg0.getKeyCode();
 		if(!pressedKeys.containsKey(c))
 			pressedKeys.put((Integer)c, null);
 		else
			pressedKeys.get(c).cancel();
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		int c = arg0.getKeyCode();
 		pressedKeys.put((Integer)c, new Remover(c, pressedKeys));
 		timer.schedule(pressedKeys.get(c), 2);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
