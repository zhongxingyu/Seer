 package Checkers;
 
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JPanel;
 
 public class Checkers extends JPanel
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6799064229355729609L;
 	private static int x=0;
 	private static int y=0;
 	private static int[][] pieces;
 	
 	public void paintComponent(Graphics g)
 	{
 		int height=getHeight();
 		int width=getWidth();
 		//horizontal lines
 		g.drawLine(0, height/8, width, height/8);
 		g.drawLine(0, height/4, width,height/4);
 		g.drawLine(0, (3*height)/8, width, (3*height)/8);
 		g.drawLine(0, height/2, width, height/2);
 		g.drawLine(0, (5*height)/8, width, (5*height)/8);
 		g.drawLine(0, (3*height)/4, width, (3*height)/4);
 		g.drawLine(0, (7*height)/8, width,  (7*height)/8);
 		g.drawLine(0, height, width, height);
 		//vertical lines
 		g.drawLine(width/8,0,width/8,height);
 		g.drawLine(width/4,0,width/4,height);
 		g.drawLine((3*width)/8,0,(3*width)/8,height);
 		g.drawLine(width/2,0,width/2,height);
 		g.drawLine((5*width)/8,0,(5*width)/8,height);
 		g.drawLine((3*width)/4,0,(3*width)/4,height);
 		g.drawLine((7*width)/8,0,(7*width)/8,height);
 		g.drawLine(width,0,width,height);
 		
 	}
 	
 	public Checkers()
 	{
 		addMouseListener(new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{
 				System.out.println(e);
				x=(int)e.getX()/74;//divide by 74 so the position of the cell can be used in a 
				y=(int)e.getY()/74;//2D array
 				pieces();
 			}
 		});
 	}
 	
 	public static void pieces()
 	{
 		pieces=Pieces.pieces();
 		int selectedPiece=pieces[x][y];
 		System.out.println(selectedPiece);
 		
 	}
 	
 
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
