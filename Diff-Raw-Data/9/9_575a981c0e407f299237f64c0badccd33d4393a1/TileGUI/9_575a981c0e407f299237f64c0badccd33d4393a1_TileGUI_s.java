 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Scanner;
 
 import javax.swing.*;
 
 public class TileGUI {
 
 	public static void main(String[] args) {
 
 		EventQueue.invokeLater(new Runnable() {
 
 			public void run() {
 
 				SizedFrame frame = new SizedFrame();
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				frame.setVisible(true);
 
 			}
 		});
 
 	}
 
 }
 
 class SimpleFrame extends JFrame {
 	public SimpleFrame() {
 		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		setTitle("hello");
 	}
 
 	public static final int DEFAULT_WIDTH = 600;
 	public static final int DEFAULT_HEIGHT = 500;
 
 }
 
 class SizedFrame extends JFrame implements MouseListener, MouseMotionListener{
 	private int colorOne;
 	private int colorTwo;
 	
 	private int rotation=0;
 	private NotHelloWorldComponent comp;
 	public SizedFrame() {
 		
 		// get screen dimensions
 
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		Dimension screenSize = kit.getScreenSize();
 		int screenHeight = screenSize.height;
 		int screenWidth = screenSize.width;
 
 		
 //	      addMouseListener( this );
 //
 //	      addMouseMotionListener( this);
 	      // set frame width and height and let platform pick location of screen
 		setSize(screenWidth / 2, screenHeight / 2);
 		setLocationByPlatform(true);
 		// can set frame icon and title here, currently no icon
		setTitle("Rectangle");
 		
 		addMouseListener( this );
 
 	    addMouseMotionListener( this);
 	    
 	    
		comp = new NotHelloWorldComponent(rotation);
		add(comp);
		
		repaint();
		rotation=2;
 		comp = new NotHelloWorldComponent(rotation);
 		add(comp);
 		
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		rotation++;
 		
 		comp.setRotation(rotation);
 		repaint();
 		
 		
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
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
 
 class NotHelloWorldComponent extends JComponent {
 	private Polygon hex1;
 	private Polygon hex2;
 	private int r;
 	private int[] xPoly2 = { 236, 193, 193, 236, 279, 279 };
 	private int[] yPoly2 = { 200, 175, 125, 100, 125, 175 };
 
 	public NotHelloWorldComponent(int r){
 		this.r=r;
 	}
 	
 	public void setRotation(int r){
 		this.r=r;
 	}
 
 	public void paintComponent(Graphics g) {
 
 		Graphics2D g2 = (Graphics2D) g;
 
 		// y coordinates for the first hex : 1, .5, -.5, -1, -.5, .5
 		// x coordinates for the first hex : 0, -.866025, -0.866025, 0, .866025,
 		// .866025
 		// The above coordinates are for a hexagon with radius(?) of 1. To get a
 		// larger hexagon, multiply
 		// the coordinates by the radius of the desired hexagon
 		// These coordinates should yield a hexagon that has flat edges on its
 		// left and right sides, so as to allow
 		// us to place two hexagons next to each other cleanly.
 
 		int xPoly[] = { 150, 107, 107, 150, 193, 193 };
 		int yPoly[] = { 200, 175, 125, 100, 125, 175 };
 
 		// coordinates for hex 2
 
 		// xPoly2[] = {236, 193, 193, 236, 279, 279};
 		// yPoly2[] = {200, 175, 125, 100, 125, 175};
 
 		hex1 = new Polygon(xPoly, yPoly, 6);
 		hex2 = new Polygon(xPoly2, yPoly2, 6);
 		
 		g2.setPaint(g2.getBackground());
 		g2.drawPolygon(hex2);
 		g2.fill(hex2);
 
 		int a=3;
 		int b=5;
 		g2.rotate(r * Math.PI / 3, 150, 150);
 
 		if (a == 0) {
 			g2.setPaint(Color.RED);
 		} else if (a == 1) {
 			g2.setPaint(Color.ORANGE);
 		} else if (a == 2) {
 			g2.setPaint(Color.YELLOW);
 		} else if (a == 3) {
 			g2.setPaint(Color.GREEN);
 		} else if (a == 4) {
 			g2.setPaint(Color.BLUE);
 		} else if (a == 5) {
 			g2.setPaint(Color.MAGENTA);
 		}
 
 		g.drawPolygon(hex1);
 		g2.fill(hex1);
 
 		if (b == 0) {
 			g2.setPaint(Color.RED);
 		} else if (b == 1) {
 			g2.setPaint(Color.ORANGE);
 		} else if (b == 2) {
 			g2.setPaint(Color.YELLOW);
 		} else if (b == 3) {
 			g2.setPaint(Color.GREEN);
 		} else if (b == 4) {
 			g2.setPaint(Color.BLUE);
 		} else if (b == 5) {
 			g2.setPaint(Color.MAGENTA);
 		}
 
 		g.drawPolygon(hex2);
 		g2.fill(hex2);
 	}
 
 
 	public static final int MESSAGE_X = 300;
 	public static final int MESSAGE_Y = 100;
 }
