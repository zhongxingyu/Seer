 package PeanutCracker;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dongcarl
  * Date: 2/1/13
  * Time: 2:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GraphPanel extends JPanel
 {
 	public static ArrayList<ArrayList<Double>> XArray = new ArrayList<ArrayList<Double>>();
 	public static ArrayList<ArrayList<Double>> YArray = new ArrayList<ArrayList<Double>>();
 
 	public static double numHeight;
 	public static double Ymax;
 
 	public static int step;
 
 	public static int originX;
 	public static int originY;
 	public static int unitX = 10;
 	public static int unitY = 10;
 
 	public static int xsize;
 	public static int ysize;
 
 	public static Window wi;
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 
 		Graphics2D g2d = (Graphics2D) g;
 
 		for (int i = 0; i<XArray.size(); i++)
 		{
 			//System.out.println("plotting "+XArray.get(i)+ YArray.get(i));
 			plot(XArray.get(i), YArray.get(i), g2d);
 		}
 		g2d.setColor(Color.BLUE);
 		g2d.drawLine(0, originX, 500, originX);
 		for (int i = -5; i <= 5 ; i++)
 		{
 			g2d.drawLine(originY+i*unitY, originX-10, originY+i*unitY, originX+10);
 		}
 		g2d.setColor(Color.GREEN);
 		g2d.drawLine(originY, 0, originY, 500);
 		for (int i = -5; i <= 5 ; i++)
 		{
 			g2d.drawLine(originY-10, originX-i*unitX, originY+10, originX-i*unitX);
 		}
 	}
 
 	public GraphPanel()
 	{
 		super();
 		xsize = this.getWidth();
 		ysize = this.getHeight();
 	}
 
 	public static void configureForDisplay(ArrayList<Double> rawX, ArrayList<Double> rawY, Window wind)
 	{
 		double Xrange = Math.abs(wind.getXmax()) + Math.abs(wind.getXmin());
 		double Yrange = wind.getYmax() - wind.getYmin();
 		ArrayList<Double> newXvals = new ArrayList<Double>(rawX.size());
 		ArrayList<Double> newYvals = new ArrayList<Double>(rawY.size());
 		//for (Double d : rawX)
 		//  x     Math.abs(xmin)
 		// 500    Math.abs(xmin)+Math.abs(xmax)
 		double xmin = wind.getXmin();
 		double xmax = wind.getXmax();
 		double sum = Math.abs(xmin)+Math.abs(xmax);
 		double rat1 = 500/sum;
 		System.out.println("rat1 = "+rat1);
 		double rat2 = Math.abs(rat1*xmin);
 		System.out.println("rat2 = "+rat2);
 		originY = (int) (rat2);
 		double ratio = -(500/Xrange);
 		unitX = (int) Math.abs(ratio); unitY = (int) Math.abs(ratio);
 		System.out.println("unitY = "+unitY);
 		double value;
 		for(int i = 0; i<rawX.size(); i++)
 		{
 			value = rawX.get(i)*ratio+(originY);
 			newXvals.add(value);
 		}
		//begin calculations for y values adjusted for display
 		double ymin = wind.getYmin();
 		double ymax = wind.getYmax();
		System.out.println("ymin = "+ymin+" ymax = "+ymax);
 		sum = Math.abs(ymin)+Math.abs(ymax);
		System.out.println("range  = "+sum);
 		rat1 = 500/sum;
 		System.out.println("rat1 y = "+rat1);
 		System.out.println("ymin = "+ymin);
 		rat2 = Math.abs(rat1*ymax);
 		originX = (int) (rat2);
 		System.out.println("rat2 y = "+rat2);
 		for(int i = 0; i<rawY.size(); i++)
 		{
			value = -(rawY.get(i)*ratio)+(originX);
			if (rawY.get(0)>0)
			{
				value = (rawY.get(i)*ratio)+originX;
			}
 			newYvals.add(value);
 		}
 		addToArray(newXvals, newYvals);
 	}
 
 	public static void setWindow(Window w)
 	{
 		wi = w;
 	}
 
 	public static void addGraph(ArrayList<Double> X, ArrayList<Double> Y)
 	{
 
 		if (X.size()!=Y.size())
 		{
 			System.out.println("Number of X's and Y's not the same!");
 		}
 
 		else
 		{
 			configureForDisplay(X, Y, wi);
 		}
 	}
 
 	public static void addToArray(ArrayList x, ArrayList y)
 	{
 		XArray.add(x);
 		System.out.println("added to XArray "+XArray);
 		YArray.add(y);
 		System.out.println("added to YArray "+YArray);
 	}
 
 
 	public static void plot(ArrayList x, ArrayList y, Graphics2D g2d)
 	{
 		for (int i = 0; i<x.size()-1; i++)
 		{
 			int x1 = ((Double)(x.get(i))).intValue();
 			int y1 = ((Double)(y.get(i))).intValue();
 
 			int x2 = ((Double)(x.get(i+1))).intValue();
 			int y2 = ((Double)(y.get(i+1))).intValue();
 
 			g2d.drawLine(x1, y1, x2, y2);
 		}
 
 	}
 }
