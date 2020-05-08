 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 @SuppressWarnings("serial")
 public class Exec extends JFrame
 {
 	Image grid;
 	Image i;
 	Graphics g;
 	
 	public int selected=1;
 	public ArrayList<Ship> ships;
 	
 	public Exec(){
 		ships = new ArrayList<Ship>();
 		ships.add(new AircraftCarrier(400,150,1,0));
 		ships.add(new BattleShip(400,250,2,0));
 		ships.add(new BattleShip(400,350,3,0));
 		ships.add(new Submarine(400,450,4,0));
 		init();
 	}
 	public void init()
 	{
 		grid = new BufferedImage(800,600,BufferedImage.TYPE_INT_ARGB);
 		Graphics gs = grid.getGraphics();
 		gs.setColor(Color.black);
 		for (int x = 0; x < 800; x += 50) {
 			gs.drawLine(x,0,x,600);
 		}
 		for (int y = 0; y < 600; y += 50){
 			gs.drawLine(0,y,800,y);
 		}
 		
 		reset();
 		this.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent k) {
 				if(k.getKeyCode() == KeyEvent.VK_LEFT){
 				//	ships.get(selected).addX(-50);
					ships.get(selected).addAngle(45);
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_RIGHT){
 				//	ships.get(selected).addX(50);
					ships.get(selected).addAngle(-45);
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_UP){
 					ships.get(selected).addY(-50);
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_DOWN){
 					ships.get(selected).addY(50);
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_PAGE_UP){
 					if(selected>0){
 						selected--;
 					}
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_PAGE_DOWN){
 					if(selected<3){
 						selected++;
 					}
 					repaint();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_HOME){
 					selected = 1;
 					ships = new ArrayList<Ship>();
 					ships.add(new BattleShip(400,350,1,0));
 					ships.add(new BattleShip(400,250,2,0));
 					ships.add(new AircraftCarrier(400,150,1,0));
 					ships.add(new Submarine(400,450,2,0));
 					repaint();
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 		MouseListener mouse = new MouseAdapter() {public void mousePressed(MouseEvent e){mousePressed2(e);}
 		public void mouseReleased(MouseEvent e){mouseReleased2(e);}};
 		this.addMouseListener(mouse);
 		MouseMotionListener mouse1 = new MouseAdapter() {public void mouseMoved(MouseEvent md){mouseM(md);}
 		public void mouseDragged(MouseEvent md){mouseDrag(md);}};
 		this.addMouseMotionListener(mouse1);
 
 		g = newBackground();
 	}
 	public void reset() {
 		g = newBackground();
 		repaint();
 	}
 	public Graphics newBackground() {
 		 i = clearBuffer();
 		 return i.getGraphics();
 	}
 	public Image clearBuffer() {
 		return new BufferedImage(800+16, 600+38, BufferedImage.TYPE_INT_ARGB);
 	}
 	public static void main(String[] args){
 		Exec battleship = new Exec();
 		battleship.setSize(800,600);
 		battleship.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		battleship.setVisible(true);
 	}
 	public void paint(Graphics g2){
 		g.setColor(Color.white);
 		g.fillRect(0,0,800,600);
 		
 		g.drawImage(grid, 0,0, null);
 		g.setColor(Color.black);
 		g.drawString("Left moves the ship left", 55, 100);
 		g.drawString("Right moves the ship right",55, 125);
 		g.drawString("Down moves the ship down", 55, 150);
 		g.drawString("Up moves the ship up", 55, 175);
 		g.drawString("Page-Down selects the next ship", 55, 200);
 		g.drawString("Page-Up selects the previous ship", 55, 225);
 		g.drawString("Currently selected ship "+" "+ships.get(selected).getClass(),55,250);
 		
 		for(int index = 0; index<ships.size();index++){
 			ships.get(index).drawShip(g);
 		}	
 		g2.drawImage(i,8,32,this);
 		if(needsRepaint())
 			repaint();
 	}
 	public void update(Graphics g) {
 		paint(g);
 		delay(10);
 	}
 	public void delay(int n){
 		try {Thread.sleep(n);} catch (Exception e) {System.out.println("Sleep failed");}
 	}	
 	public boolean needsRepaint(){
 		for(int index = 0; index<ships.size();index++){
 			if(ships.get(index).needsRepaint()){
 				return true;
 			}
 		}	
 		return false;
 	}
 	
 	
 	public void mouseM(MouseEvent e){
     	int x = e.getX()-8;
     	int y = e.getY()-32;
     	ships.get(selected).MouseMoved(x,y);
     	repaint();
     }	
 	public void mouseReleased2(MouseEvent e)
 	{
 		repaint();
 	}
 	public void mousePressed2(MouseEvent e)
     {
     	int x = e.getX()-8;
     	int y = e.getY()-32;
     	ships.get(selected).MouseClicked(x,y);
     	repaint();
     }
 	public void mouseDrag(MouseEvent e)
     {
     	int x = e.getX();
     	int y = e.getY();
     	ships.get(selected).MouseMoved(x,y);
     	repaint();
     }
 }
