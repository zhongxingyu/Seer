 //VisualProblemLine.java
 //holds the main ProblemLine and renders the currently selected ProblemLine
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.util.ArrayList;
 
 public class VisualProblemLine
 {
 	public VisualProblemSolution mainProb;
 	public VisualProblemSolution selectedProb;
 	
 	public int width, height;			//width and height of what is rendered
 	public double circleRadius = 10;	//radi of the circles on the ends of the line
 	public static int linePadding = 40;		//padding on sides of line
 	
 	public int camX = 0;
 	public ArrayList<VisualProblemSolution> breadcrumbTrail = new ArrayList<VisualProblemSolution>();	//list of the  problems entered
 	
 	public VisualProblemLine(int width, int height)
 	{
 		this.width = width;
 		this.height = height;
 		mainProb = new VisualProblemSolution(100, 100);
 		selectedProb = mainProb;
 	}
 	
 	//zoom into the selected problem (by position in the ArrayList)
 	public void zoomTo(int pos)
 	{
 		//zoom into that problem
 		if(pos == -1)
 			return;
 		
 		breadcrumbTrail.add(selectedProb);
 		selectedProb = selectedProb.innerLine.probList.get(pos);
 		camX = 0;
 		adjustLine();
 	}
 	
 	//zoom out to the level above the current one
 	public void zoomOutALevel()
 	{
 		if(breadcrumbTrail.size() == 0)
 		{
 			zoomOutToMain();
 		}
 		else
 		{
 			selectedProb = breadcrumbTrail.get(breadcrumbTrail.size()-1);
 			breadcrumbTrail.remove(breadcrumbTrail.size()-1);
 			adjustLine();
 			camX = 0;
 		}
 	}
 	
 	//zoom out to the main line
 	public void zoomOutToMain()
 	{
 		selectedProb = mainProb;
 		adjustLine();
 		camX = 0;
 	}
 	
 	public void addProblem(int x)
 	{
 		if(!isValidPos(x) || isProblemBox(x))
 			return;
 		
 		//determine where to add in arraylist based on x val
 		int pos = getProblemFromX(x);
 		if(selectedProb.innerLine.probList.size() == 0)	//there are no problems
 			pos = 0;
 		
 		//add in the new problem
 		selectedProb.innerLine.probList.add(pos, new VisualProblemSolution(x, height/2));
 		
 		adjustLine();
 	}
 	
 	//adjust line to fit current problems & equally space problems
 	public void adjustLine()
 	{
 		if(selectedProb.innerLine.probList.size() == 0)
 		{
 			width = Main.pixel.width;
 		}
 		else
 		{
 			//expand line to fit new problems
 			width = selectedProb.innerLine.probList.size()*(VisualProblemSolution.width + VisualProblemSolution.padding) + VisualProblemSolution.padding + linePadding*2;
 		}
 		
 		//adjust x positions of problems
 		for(int i = 0; i < selectedProb.innerLine.probList.size(); i++)
 			selectedProb.innerLine.probList.get(i).x = linePadding+(VisualProblemSolution.padding+VisualProblemSolution.width)*(i+1)-VisualProblemSolution.width/2;
 	}
 	
 	//returns -1 if x value isn't a problem
 	public int getProblemFromX(int x)
 	{
 		if(selectedProb.innerLine.probList.size() == 0)		//there are no problems
 			return -1;
 		
 		if(!isValidPos(x))		//the point given is out of bounds
 			return -1;
 		
 		return (x-VisualProblemLine.linePadding)/(VisualProblemSolution.width+VisualProblemSolution.padding);
 	}
 	
 	//return true if the x value isn't on a problem box
 	public boolean isNotProblemBox(int x)
 	{
 		return !isProblemBox(x);
 	}
 	
 	public boolean isProblemBox(int x)
 	{
 		if(selectedProb.innerLine.probList.size() == 0)		//there are no problems
 			return false;
 		
 		double denom = (VisualProblemSolution.width + VisualProblemSolution.padding);
 		double minVal = ((double)x - VisualProblemLine.linePadding - VisualProblemSolution.padding) / denom;
 		
 		if(minVal - (int)minVal > 1 - VisualProblemSolution.padding / denom && minVal - (int)minVal < 1.0)
 			return false;
 		return true;
 	}
 	
 	//return false if x value is not in the bounds of the line
 	public boolean isValidPos(int x)
 	{
 		if(x < 0+linePadding || x > width-linePadding)
 			return false;
 		return true;
 	}
 	
 	public void tick()
 	{
 		for(int i = 0; i < selectedProb.innerLine.probList.size(); i++)
 			selectedProb.innerLine.probList.get(i).tick();
 	}
 	
 	public void render(Graphics g)
 	{
 		int lineY = height/2;		//y value of both ends
 		int lineX1 = linePadding;	//x value of left side
 		int lineX2 = width-lineX1;	//x value of right side
 		
 		BasicStroke normalStroke = new BasicStroke();
 		BasicStroke thickStroke = new BasicStroke(3.0f);
 		
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		if(mainProb == selectedProb)
 		{
 			//draw the line
 			g2.setColor(Color.black);
 			g2.setStroke(thickStroke);
 			g2.drawLine(lineX1-camX, lineY, lineX2-camX, lineY);
 			
 			//draw the circles on the ends
 			g2.setColor(Color.black);
 			g2.setStroke(normalStroke);
 			g2.fillOval((int)(lineX1-circleRadius)-camX, (int)(lineY-circleRadius), (int)circleRadius*2, (int)circleRadius*2);
 			g2.fillOval((int)(lineX2-circleRadius)-camX, (int)(lineY-circleRadius), (int)circleRadius*2, (int)circleRadius*2);
 		}
 		else
 		{
 			//fill in green and red background
 			g.setColor(new Color(210, 240, 190));
 			g.fillRect(lineX1-camX, 0, width-2*(lineX1), Main.pixel.height/2);
 			g.setColor(new Color(255, 175, 165));
 			g.fillRect(lineX1-camX, Main.pixel.height/2, width-2*(lineX1), Main.pixel.height/2);
 			
 			//draw the line
 			g2.setColor(Color.black);
 			g2.setStroke(thickStroke);
 			g2.drawLine(0, lineY, Main.pixel.width, lineY);
 			
 			//draw colored stripe on the bottom
 			int bigStripeHeight = (int)((double)Main.pixel.height * VisualProblemSolution.stripeHeight / VisualProblemSolution.height);
 			g2.setColor(selectedProb.color);
 			g2.fillRect(lineX1-camX, Main.pixel.height-bigStripeHeight, lineX2-lineX1, bigStripeHeight);
 			g2.setColor(Color.black);
 			g2.setStroke(thickStroke);
 			g2.drawLine(lineX1-camX, Main.pixel.height-bigStripeHeight, width-linePadding-camX, Main.pixel.height-bigStripeHeight);
 			
 			//draw the vertical lines
 			g2.setColor(Color.black);
 			g2.setStroke(thickStroke);
 			g2.drawLine(lineX1-camX, 0, lineX1-camX, height);
 			g2.drawLine(lineX2-camX, 0, lineX2-camX, height);
 			g2.setStroke(normalStroke);
 		}
 		
 		for(int i = 0; i < selectedProb.innerLine.probList.size(); i++)
 		{
 			selectedProb.innerLine.probList.get(i).render(g);
 		}
 	}
 }
