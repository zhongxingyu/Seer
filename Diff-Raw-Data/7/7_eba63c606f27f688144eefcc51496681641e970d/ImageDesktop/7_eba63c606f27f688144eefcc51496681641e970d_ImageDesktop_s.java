 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.DefaultDesktopManager;  
 
 /**
  * HCI Project Phase 1
  * 
  * @author Sam Shelley, Khurram Aslam
  */
 public class ImageDesktop extends JDesktopPane implements MouseListener, MouseMotionListener{
 
 	private ImageLabeller parent;
 	
 	private int x1,x2,y1,y2;
 	private boolean dragging;
 	
 	private Point startpoint = null;
 	private Point lastdragpoint = null;
 	
 	private ArrayList<Point> currentPolygon = null;
 	private ArrayList<ArrayList<Point>> polygonsList = null;
 	
 	private JPanel drawings;
 	
 	private BufferedImage image;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public ImageDesktop(BufferedImage readImage, ImageLabeller parent) {
 		super();
 		image = readImage;
 		
 		this.setDesktopManager(new PaintDesktopManager());  
 		
 		this.parent = parent;
 		currentPolygon = new ArrayList<Point>();
 		polygonsList = new ArrayList<ArrayList<Point>>();
 		dragging = false;
 		
 		drawings = new JPanel();
 		drawings.setVisible(true);
 		drawings.setBackground(Color.blue);
 		this.add(drawings);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 	
 	private static class PaintDesktopManager extends DefaultDesktopManager {  
 		private static final long serialVersionUID = 1L;
 		public void beginDraggingFrame(JComponent f)
         {
 			f.repaint();
         }	
 		public void endDraggingFrame(JComponent f)
         {
 			f.repaint();
         }	
     } 
 	private boolean isOpenDialog() {
 		return getAllFrames().length > 0;
 	}
 	public void deletePolygon(int num) {
 		if(num == -1) {
 			polygonsList.remove(polygonsList.size()-1);
 			this.repaint();
 		} else {
 			polygonsList.remove(num);
 		}
 	}
 	public void deletePolygon(String name) {
 		
 	}
 	public void addNewPolygon() {
 		//finish the current polygon if any
 		if (currentPolygon != null ) {
 			polygonsList.add(currentPolygon);
 		}
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		g.setColor(Color.RED);
 		g.fillOval(startpoint.getX()-7,startpoint.getY()-7,15,15);
 		
 		//REMEMBER TO ADD CHECK FOR EDGE OF SCREEN LATER
 		parent.createFrame(startpoint.getX()-50,startpoint.getY()-50);
 		startpoint = null;
   	  	lastdragpoint = null;
 		currentPolygon = new ArrayList<Point>();
 		
 	}
 	
 	public void undo() {
 		if(currentPolygon.size() > 1) {
 			currentPolygon.remove(currentPolygon.size()-1);
 			while(!currentPolygon.get(currentPolygon.size()-1).isPrimary()) {
 				currentPolygon.remove(currentPolygon.size()-1);
 			}
 			repaint();
 		} else if(currentPolygon.size() == 1) {
 			currentPolygon.remove(0);
 			startpoint = null;
 			lastdragpoint = null;
 			repaint();
 		}
 	}
 	
 	@Override
 	public void mouseDragged( MouseEvent e ) {
 		if(!isOpenDialog()) {
 			x2 = e.getX();
 			y2 = e.getY();
 			dragging = true;
 			Point cur = new Point(x2,y2,4);
 			currentPolygon.add(cur);
 			Graphics2D g = (Graphics2D) this.getGraphics();
 			g.setStroke(new BasicStroke(4.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 			g.setColor(Color.RED);
 			g.drawLine(x1,y1,x2,y2);
 			
 			mouseOverCheck(e);
 			
 			x1 = x2;
 			y1 = y2;
 		}
 	}
 	
 	public void drawPolygon(ArrayList<Point> polygon) {
 		Graphics2D g = (Graphics2D)this.getGraphics();
 		g.setColor(Color.RED);
 		g.setStroke(new BasicStroke(4.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 		for(int i = 0; i < polygon.size(); i++) {
 			Point currentVertex = polygon.get(i);
 			if (i != 0) {
 				Point prevVertex = polygon.get(i - 1);
 				g.drawLine(prevVertex.getX(), prevVertex.getY(), currentVertex.getX(), currentVertex.getY());
 			}
 			if(currentVertex.isPrimary()) {
 				g.fillOval(currentVertex.getX() - 7, currentVertex.getY() - 7, 15, 15);
 			}
 		}
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		if(!isOpenDialog()) {
 			mouseOverCheck(e);
 		}
 	}
 	@Override
 	protected void paintComponent(Graphics g)
     {
         g.drawImage(image, 0, 0, null);
         for(ArrayList<Point> polygon : polygonsList) {
 			drawPolygon(polygon);
 		}
 		
 		//display current polygon
 		drawPolygon(currentPolygon);
 		
 		//repaint the frames to make sure they overlap
 		for(JInternalFrame f : this.getAllFrames()) {
 			f.repaint();
 		}
 		
     }
 	public void addLabel(String text) {
 		parent.addLabel(text);
 	}
 	public void mouseOverCheck(MouseEvent e) {
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		for(int i=0;i<currentPolygon.size();i++) {
 			if(currentPolygon.get(i).isPrimary()) {
 				if(currentPolygon.get(i).near(new Point(e.getX(),e.getY(),1))) {
 					g.setColor(Color.WHITE);
 					g.fillOval(currentPolygon.get(i).getX()-7,currentPolygon.get(i).getY()-7,15,15);
 				} else {
 					g.setColor(Color.RED);
 					g.fillOval(currentPolygon.get(i).getX()-7,currentPolygon.get(i).getY()-7,15,15);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		
 	}
 	private void drawVertex(MouseEvent e) {
 		  boolean end = false;
 		  boolean dragpoint = false;
 		  x1 = e.getX();
 	      y1 = e.getY();
 	      if(startpoint == null) {
 	    	  startpoint = new Point(x1,y1,8, true);
 	    	  currentPolygon.add(startpoint);
 	      } else if(startpoint.near(new Point(x1,y1,1))) {
 	    	  currentPolygon.add(startpoint);
 	    	  x1 = startpoint.getX();
 	    	  y1 = startpoint.getY();
 	    	  end = true;
 	      } else if(lastdragpoint != null && lastdragpoint.near(new Point(x1,y1,1))) {
 	    	  System.out.println("hiya");
 	    	  currentPolygon.add(lastdragpoint);
 	    	  dragpoint = true;
 	      } else {
 	    	  currentPolygon.add(new Point(x1,y1,8,true));
 	      }
 	      
 			
 	      Graphics2D g = (Graphics2D)this.getGraphics();
 			
 	      //if the left button than we will add a vertex to poly
 	      if (e.getButton() == MouseEvent.BUTTON1) {
 	    	  g.setColor(Color.RED);
 	    	  if (currentPolygon.size() > 1) {
 	    		  Point lastVertex = currentPolygon.get(currentPolygon.size() - 2);
 	    		  g.setStroke(new BasicStroke(4.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 	    		  g.drawLine(lastVertex.getX(), lastVertex.getY(), x1, y1);
 	    	  }
 	    	  if(!end && !dragpoint) {
 	    		  g.fillOval(x1-7,y1-7,15,15);
 	    	  }
 	    	  if(end) {
 	    		  addNewPolygon();
 	    	  }
 
 	    	  //System.out.println(x1 + " " + y1);
 	      }
 		
 	}
 
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if(!isOpenDialog()) {
 			drawVertex(e);
 			mouseOverCheck(e);
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if(!isOpenDialog()) {
 			if(dragging) {
 			
 				dragging = false;
 				if(startpoint != null && startpoint.near(new Point(e.getX(),e.getY(),1))) {
 			    	  currentPolygon.add(startpoint);
 			    	  addNewPolygon();
 			    } else {
 			    	Graphics2D g = (Graphics2D)this.getGraphics();
			    	System.out.println("setting dragpoint");
 			    	lastdragpoint = new Point(e.getX(),e.getY(),8,true);
 			    	currentPolygon.add(lastdragpoint);
 			    	g.setColor(Color.RED);
 			    	g.fillOval(e.getX()-7,e.getY()-7,15,15);
 			    }
 			}
 			mouseOverCheck(e);
 		}
 		
 	}
 
 	
 }
