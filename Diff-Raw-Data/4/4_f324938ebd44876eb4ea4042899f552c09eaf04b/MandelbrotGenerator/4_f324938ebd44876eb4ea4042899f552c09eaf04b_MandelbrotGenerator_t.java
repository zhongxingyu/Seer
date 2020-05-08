 //test
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferStrategy;
 import java.util.Random;
 
 import javax.swing.*;
 
 public class MandelbrotGenerator extends Canvas {
 BufferStrategy strategy;	
 boolean running = true;
 double xMax = 1;
 double xMin = -1;
 double yMax = 1;
 double ymin = -1;
 
 int SCREEN_WIDTH=800;
 int SCREEN_HEIGHT=800;
 	
 	MandelbrotGenerator(){
 		Dimension size = new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT);
 		
 		setPreferredSize(size);
 		setMaximumSize(size);
 		setMinimumSize(size);
 		setIgnoreRepaint(true);
 		
 		JFrame frame = new JFrame("MandelbrotGenerator");
 		frame.add(this);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		
 		createBufferStrategy(2);
 		strategy = getBufferStrategy();
 		
 	}
 	
 	public Point2D.Double convertToImaginary(int x, int y){
 		Point2D.Double p=new Point2D.Double((x-SCREEN_WIDTH/2)/(SCREEN_WIDTH/2f)*xMax,(SCREEN_HEIGHT/2-y)/(SCREEN_HEIGHT/2f)*yMax);
 		return p;
 		
 	}
 	public Point2D.Double convertToPolar(Point2D.Double p){
 		double x = p.getX();
 		double y = p.getY();
 		
 		double theta = Math.atan2(y, x);
 		double magnitude = Math.sqrt(x*x+y*y);
 		
 		Point2D.Double point = new Point2D.Double(magnitude,theta); 
 		return point;
 		
 		
 	}
 	
 	public Point2D.Double convertToCart(Point2D.Double p){
 		double x= ( p.getX()*Math.cos(p.getY()));
		double y= ( p.getX()*Math.sin(p.getY()));
 		Point2D.Double point=new Point2D.Double(x,y);
 		return point;
 	}
 	
 	public void renderLoop(){
 		
 		while(running){
 			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 			g.setColor(Color.WHITE);
 			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
 			g.setColor(Color.BLUE);
 			
 			for (int x=0; x<SCREEN_WIDTH; x++ ){
 				for (int y=0; y<SCREEN_HEIGHT; y++ ){
 					Point2D.Double p = convertToImaginary( x, y);
 					//System.out.println(p);
 					//Point2D.Double pointPolar = convertToPolar(p);
 					Point2D.Double pointPolar = new Point2D.Double(0,0);
 					//Point2D.Double initialPolar = convertToPolar(p);
 					
 					
 				for(int i=0;i<50;i++)
 				{
 					
 					double magnitude = pointPolar.getX();
 					double theta = pointPolar.getY();
 					
 					magnitude *= magnitude;
 					theta *= 2;
 					
 					Point2D.Double z2 = new Point2D.Double(magnitude, theta);
 					Point2D.Double z2Cart = convertToCart(z2);
 					Point2D.Double zn1 = new Point2D.Double(z2Cart.getX() + p.getX(), z2Cart.getY() + p.getY());
 					Point2D.Double zn1polar = convertToPolar(zn1);
 					pointPolar = zn1polar;
 					
 					//System.out.println(pointPolar.getX());
 					
 					if(pointPolar.getX()<.001){
 						g.setColor(Color.BLACK);
 						break;
 					}
 					else if(pointPolar.getX()>10){
 						g.setColor(Color.RED);
 						break;
 					}
 					/*
 					else if(Math.abs(zn1.getX()-p.getX())<.000001 && Math.abs(zn1.getY()-p.getY()) < .000001){
 						g.setColor(Color.BLUE);
 						break;
 					}
 					*/
 					
 					if(i==49)
 					{
 						g.setColor(Color.BLACK);
 						break;
 					}
 					
 				}	
 					
 				g.drawLine(x,y,x,y);
 				}
 			}
 			g.dispose();
 			strategy.show();
 			
 			
 			
 			try{
 			Thread.sleep(1);
 			}
 			catch(Exception e){
 				
 			}
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		MandelbrotGenerator generator = new MandelbrotGenerator();
 		generator.renderLoop();
 	}
 
 }
