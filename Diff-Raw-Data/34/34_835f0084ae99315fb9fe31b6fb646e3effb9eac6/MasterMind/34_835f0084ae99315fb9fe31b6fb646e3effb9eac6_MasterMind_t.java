 package PeanutCracker;
 
 import java.io.ObjectStreamException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * Editted with Aptana Studio 3
  * Creator: dongcarl
  * Editor: Buck
  * Date: 2/1/13
  * Time: 2:56 PM
  */
 
 //TODO write a zeros function
 public class MasterMind implements Operator
 {
 	Double substitute;
 	String modFunction;
 	public static ArrayList<Double> xPoints = new ArrayList<Double>();
 	public static ArrayList<Double> yPoints = new ArrayList<Double>();
 	public static ArrayList<Function> Functions = new ArrayList<Function>();
 	public static ArrayList<Window> Windows = new ArrayList<Window>();
 
 	public static void main(String[] args)
 	{
 		//array list of elements
 		ArrayList<Element> jake = new ArrayList<Element>();
 		//make a polynomial to add to the above list
 		Polynomial polly = new Polynomial();
 		//add a new monomial to the polynomial
		polly.addElement(new Monomial(.5, 2));
 		//polly.addElement(new Monomial(10, 0));
 		//polly.addElement(new Monomial(3, 1));
 		//polly.addElement(new Monomial(.5 , (int) .5));
 		//add the polynomial to the arraylist of elements
 		jake.add(polly);
 		//add the arraylist of elements to the function
 		Function terry = new Function(jake);
 		//make a new window
		Window witherspoon = new Window(-10, 10, .05, -10, 10, .05);
 		MasterMind mindy = new MasterMind(terry, 0, witherspoon);
//		for (Double d : mindy.getZeroes(terry, 0, witherspoon))
//		{
//			double d1 = d;
//			System.out.println("when y = 0, x = "+d);
//		}
 	}
 	public MasterMind(Function func, int operation, Window walrus)
 	{
 		Functions.add(func);
 		Windows.add(walrus);
 		//The constructor takes in a ControlCenter to pass messages to
 		ArrayList<Double> x = processXPoints(func, walrus);
 		ArrayList<Double> y = processFunction(func, operation, walrus);
 		System.out.println("coordinate points");
 		for (int i = 0; i <x.size() && i < y.size(); i++)
 		{
 			System.out.println("coordinate "+i+" x: "+x.get(i)+" y: "+y.get(i));
 		}
 		while (x.size()>y.size()) x.remove(x.size()-1);
 		while (y.size()>x.size()) y.remove(y.size()-1);
 		mailToGraph(x, y, walrus);
 	}
 	public static ArrayList<Double> getZeroes(Function func, int operation, Window walrus)
 	{
 		//returns the xcoordinates of any zeroes
 		//if a zero is predicted between two points, then  it is aproximated by a linearization connecting the two points
 		//this is only as accurate as the resolution, because it can miss zeroes if there is no change in sign or exact match
 		ArrayList<Double> x = processXPoints(func, walrus);
 		ArrayList<Double> y = processFunction(func, operation, walrus);
 		ArrayList<Double> zero = new ArrayList<Double>();
 		for (int i = 0; i<x.size()-1 && i<y.size()-1; i++)
 		{
 			if (y.get(i) == 0 )
 			{
 				zero.add(x.get(i));
 			}
 			else if (y.get(i)<0 && y.get(i+1)>0 || y.get(i)>0 && y.get(i+1)<0)
 			{
 				//a zero is somewhere between two points on a continuous function (one positive, one negative)
 				double y1 = y.get(i);
 				double y2 = y.get(i+1);
 				double x1 = x.get(i);
 				double x2 = x.get(i+1);
 				//the ratio of the distance from x1 to the estimated zero vs x2 to the estimated zero is 
 				//	proportional to distance from y1 to 0 vs y2 to 0 ---> Math.abs(y1/y2)
 				//	when the estimate is a line
 				double xd = Math.abs(x2-x1);
 				double estd = xd*Math.abs(y1/y2);
 				double estimate = x1+estd;
 				//add the linear estimate
 				zero.add(estimate);
 			}
 		}
 		return zero;
 	}
 	public static Window optimizeWindow(ArrayList<Double> x, ArrayList<Double> y, Window walrus)
 	{
 		double xmax = walrus.getXmax();
 		double xmin = walrus.getXmin();
 		double xres = walrus.getXres();
 		double ymax = walrus.getYmax();
 		double ymin = walrus.getYmin();
 		double yres = walrus.getYres();
 		for (Double xp : x)
 		{
 			if ((xp*1.1)<xmin)
 			{
 				xmin = xp*1.1;
 			}
 			if ((xp*1.1)>xmax)
 			{
 				xmax = xp*1.1;
 			}
 			xres = walrus.getXres()*((xmax-xmin)/(walrus.getXmax()-walrus.getXmin()));
 		}
 		for (Double yp : y)
 		{
 			if ((yp*1.1)<xmin)
 			{
 				ymin = yp*1.1;
 			}
 			if ((yp*1.1)>xmax)
 			{
 				ymax = yp*1.1;
 			}
 			xres = walrus.getYres()*((ymax-ymin)/(walrus.getYmax()-walrus.getYmin()));
 		}
 		Window wammy = new Window(xmax, xmin, xres, ymax, ymin, yres);
 		return wammy;
 	}
 	public static void mailToGraph(ArrayList<Double> xpoints, ArrayList<Double> ypoints, Window w)
 	{
 		new GraphFrame(xpoints, ypoints, w);
 	}
 	public static double[] returnOrigin(int xsize, int ysize, ArrayList<Double> xpoints, ArrayList<Double> ypoints)
 	{
 		//returns a double[] with the first value as x, second value as y
 		double[] originCoords = new double[2];
 		double xmax = Collections.max(xpoints);
 		double xmin = Collections.min(xpoints);
 		double revised = ((500*0)/(xmax-xmin))+(xsize/2);
 		originCoords[0] = revised;
 		double ymax = Collections.max(ypoints);
 		double ymin = Collections.min(ypoints);
 		revised = -((500*0)/(ymax-ymin))+(ysize/2);
 		originCoords[1] = revised;
 		return originCoords;
 	}
 	public static ArrayList<Double> adjustToPanelX(int xsize, ArrayList<Double> xpoints)
 	{
 		//xsize of the display window in pixels
 		//arraylist of the xpoint that will be graphed
 		double xmax = Collections.max(xpoints);
 		double xmin = Collections.min(xpoints);
 		ArrayList<Double> adjX = new ArrayList<Double>();
 		for (Double xc : xpoints)
 		{
 			double revised = ((500*xc)/(xmax-xmin))+(xsize/2);
 			adjX.add(revised);
 		}
 		return adjX;
 	}
 	public static ArrayList<Double> adjustToPanelY(int ysize, ArrayList<Double> ypoints)
 	{
 		//ysize of the display window in pixels
 		//arraylist of the ypoints that will be graphed
 		double ymax = Collections.max(ypoints);
 		double ymin = Collections.min(ypoints);
 		ArrayList<Double> adjY = new ArrayList<Double>();
 		for (Double yc : ypoints)
 		{
 			double revised = -((500*yc)/(ymax-ymin))+(ysize/2);
 			adjY.add(revised);
 		}
 		return adjY;
 	}
 	public static ArrayList<Double> processXPoints(Function func, Window walrus)
 	{
 		xPoints = new ArrayList<Double>();
 		for (double x = walrus.getXmin(); x<=walrus.getXmax(); x+=walrus.getXres())
 		{
 			xPoints.add(x);
 		}
 		return xPoints;
 	}
 	public static ArrayList<Double> processFunction(Function func, int operation, Window walrus)
 	{
 		//Operation 0 = none, 1 = integrate, -1 = derive
 		ArrayList<Double> yPoints = new ArrayList<Double>();
 		walrus.getXmin();
 		walrus.getXmax();
 		walrus.getXres(); //units per pixel
 		double xrange = walrus.getXmax()-walrus.getXmin();
 		double xcount = xrange/walrus.getXres();
 		for (double x = walrus.getXmin(); x<=walrus.getXmax(); x+=walrus.getXres())
 		{
 			yPoints.add((double) substitute(func,x));
 		}
 		if (operation == 0)
 		{
 			return yPoints;
 		}
 		else if (operation == 1)
 		{
 			//now I'm looking to estimate the integral
 			System.out.println("Integral operation selected");
 			ArrayList<Double> integral = new ArrayList<Double>();
 			double width = walrus.getXres();
 			double sum = 0;
 			for (Double y : yPoints)
 			{
 				sum += y;
 				integral.add(sum);
 			}
 			return integral;
 		}
 		else if (operation == -1)
 		{
 			//now estimating the slope
 			System.out.println("Derivative operation selected");
 			ArrayList<Double> derivative = new ArrayList<Double>();
 			double width = walrus.getXres();
 			double slope = 0;
 			for (int i = 0; i < yPoints.size() - 1 ; i++)
 			{
 				double rise = yPoints.get(i+1)-yPoints.get(i);
 				slope = rise/width;
 				derivative.add(slope);
 			}
 			return derivative;
 		}
 		else if (operation != 0)
 		{
 			System.out.println("Bad operation passed, function evaluated as operation = 0. Please try again.\n    - Sincerely MGMT");
 		}
 		return yPoints;
 	}
 	public Function operate(Function dave)
 	{
 		//called so that the function operates on itself, and it should return a new function with no operation
 		//this will be moved to the mastermind class
 		if (dave.checkOp())
 		{
 			Function carl = dave;
 			if (carl.getOperator() == Operators.SUBSTITUTION)
 			{
 				double frank = substitute(carl,substitute);
 				Constant fred = new Constant(frank);
 				carl = new Function();
 				carl.add(fred);
 			}
 			else if (carl.getOperator() == Operators.DERIVATIVE)
 			{
 				carl = differentiate(carl);
 			}
 			else if (carl.getOperator() == Operators.INTEGRAL)
 			{
 				carl = integrate(carl);
 			}
 			carl.setOperator(Operators.NONE);
 			return carl;
 		}
 		else
 		{
 			return dave;
 		}
 	}
 	public static ArrayList<ArrayList<Double>> getPoints(Function mode, Window wind)
 	{
 		ArrayList<ArrayList<Double>> setup = new ArrayList<ArrayList<Double>>(2);
 		ArrayList<Double> xpoints = new ArrayList<Double>();
 		ArrayList<Double> ypoints = new ArrayList<Double>();
 		double xmi = wind.xmin;
 		double xma = wind.xmax;
 		double xdif = xma-xmi;
 		double xcount = xdif*wind.xres;
 		double xval;
 		for (int i = 0; i<=xcount; i++)
 		{
 			xval = xma+(i*(xdif/xcount));
 			xpoints.add(xval);
 			ypoints.add(substitute(mode, xval));
 		}
 		setup.add(xpoints);
 		setup.add(ypoints);
 		return setup;
 	}
 	private static double substitute(Function mode, double replace)
 	{
 		//substitutes and evaluates the function for the given variable in place of "x"
 		Function mod = mode;
 		double sum = 0.0;
 		for (Element e:mod)
 		{
 			Constant charlie = e.substitute(replace);
 			sum += charlie.getValue();
 			//should return a constant function with one element e
 		}
 		return sum;
 	}
 	private Function integrate(Function mode)
 	{
 		Function mod = mode;
 		for (Element e:mod)
 		{
 			e = e.derive();
 		}
 		return mod;
 	}
 	private Function differentiate(Function mode)
 	{
 		Function mod = mode;
 		for (Element e:mod)
 		{
 			e = e.derive();
 		}
 		return mod;
 	}
 }
