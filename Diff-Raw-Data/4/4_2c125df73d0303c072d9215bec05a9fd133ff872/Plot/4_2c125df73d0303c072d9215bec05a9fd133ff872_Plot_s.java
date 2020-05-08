 package game;
 
 import java.awt.Color;
 import java.io.Serializable;
 
 import ui.render.Render;
 import ui.render.Renderable;
 
 /**
  * The plot class represents a plot with a type that contains different
  * production values
  */
 public class Plot implements Renderable, Serializable
 {
 	private static final long serialVersionUID = 9074483871643315710L;
 
 	public static final int SIZE = 70;
 
 	private boolean isOwned;
 
 	private int x;
 	private int y;
 	private Color color;
 	private PlotType plotType;
 	private ImprovementType improvementType;
 
     private Mule mule;
     
     private Render render;
 
 	public Plot (PlotType type, int x, int y)
 	{
 		this.plotType = type;
 		improvementType = ImprovementType.EMPTY;
 		
 		this.x = x;
 		this.y = y;
 		
 		render = new Render();
 		render.x = y * Plot.SIZE; // plot is messed up
 		render.y = x * Plot.SIZE;
 	}
 	
 	public Plot(Plot plot)
 	{
 		this.x = plot.x;
 		this.y = plot.y;
 		this.render = plot.render;
 		
 		this.color = plot.color;
 		this.plotType = plot.plotType;
 		this.improvementType = plot.improvementType;
 		this.mule = plot.mule;
 	}
 	
     /**
      * The get PlotType method returns the current type
      * @return PlotType type - the plot type
      */
 	public PlotType getType()
 	{
 		return plotType;
 	}
 	
     public Mule getMule() 
     {
         return mule;
     }
     
     public boolean hasMule()
     {
     	return mule != null;
     }
 
     public void setMule(Mule mule) 
     {
         this.mule = mule;
         if (mule == null)
         {
             this.improvementType = ImprovementType.EMPTY; 	
         }
     }
 	
 	public int getFoodProduction()
 	{		
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getFood();
 	}
 	
 	/**
      * the getEnergyProduction method returns the energy production 
      * @return int energyProduction - the possible energy production
      */
 	public int getEnergyProduction()
 	{
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getEnergy();
 	}
 	
     /**
      * The getOreProduction method returns the ore production
      * @return int oreProduction - the possible ore production
      */
 	public int getOreProduction()
 	{
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getOre();
 	}
 	
 	public String getBorderImagePath()
 	{		
 		String colorName = "empty";
 				
 		if (color.getRGB() == Color.red.getRGB())
 		{
 			colorName = "red";				
 		}
 		else if (color.getRGB() == Color.blue.getRGB())
 		{
 			colorName = "blue";
 		} 
 		else if (color.getRGB() == Color.black.getRGB())
 		{
 			colorName = "black";
 		}
 		else if (color.getRGB() == Color.green.getRGB()) 
 		{
 			colorName = "green";
 		}
 		
 		return "assets/images/plot/" + colorName + "Border.png";
 	}
 	
 	public int getX()
 	{
 		return (int)(this.x * SIZE);
 	}
 
 	public int getY()
 	{
 		return (int)(this.y * SIZE);
 	}
 	
 	public boolean isOwned()
 	{
 		return isOwned;
 	}
 	
 	public void setIsOwned(boolean bool)
 	{
 		isOwned = bool;
 	}
 	
 	public void setColor(Color color)
 	{
 		this.color = color;
 	}
 
 	public void setX(int x) 
 	{
 		this.x = x;
 	}
 
 	public void setY(int y)
 	{
 		this.y = y;	
 	}
 	
 	public boolean inBounds(int x, int y)
 	{
 		return x > this.y * SIZE && x < this.y * SIZE + SIZE && y > this.x * SIZE && y < this.x * SIZE + SIZE;
 	}
 	
 	public Render getRender()
 	{
 		render.clearImages();
 		render.addImage(plotType.getImagePath());
 		render.addImage(improvementType.getPlotImagePath());
 		if (color != null)
 		{
 			render.addImage(getBorderImagePath());
 		}
 		return render;
 	}
 }
