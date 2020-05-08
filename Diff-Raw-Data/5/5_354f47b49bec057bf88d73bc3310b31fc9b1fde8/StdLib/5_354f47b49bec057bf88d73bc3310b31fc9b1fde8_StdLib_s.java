 package com.hexcore.cas.rulesystems;
 
 import com.hexcore.cas.model.Cell;
 
 /**
  * Class StdLib
 
  * @authors Karl Zoller
  */
 
 public class StdLib
 {
 	public static double max(double[] values)
 	{
 		double highest = -Double.MAX_VALUE;
 		
 		for(double x : values)
 		{
 			if(x > highest)
 				highest = x;
 		}
 		
 		return highest;
 	}
 	
 	public static double min(double[] values)
 	{
 		double lowest = Double.MAX_VALUE;
 		
 		for(double x : values)
 		{
 			if(x < lowest)
 				lowest = x;
 		}
 		
 		return lowest;
 	}
 	
 	/* Log base 10*/
 	public static double log(double value)
 	{
 		return Math.log10(value);
 	}
 	
 	/* Natural log*/
 	public static double ln(double value)
 	{
 		return Math.log(value);
 	}
 	
 	public static double sin(double value)
 	{
 		return Math.sin(value);
 	}
 	
 	public static double cos(double value)
 	{
 		return Math.cos(value);
 	}
 	
 	public static double sum(double[] values)
 	{
 		double sum = 0;
 		
 		for(double x : values)
 			sum += x;
 				
 		return sum;
 	}
 	
 	public static int count(double[] values)
 	{
 		return values.length;
 	}
 	
 	public static double random(double val)
 	{
 		return Math.random() * val;
 	}
 	
 	public static int round(double val)
 	{
 		return (int) Math.round(val);
 	}
 	
	public int exists(Cell c)
 	{
		return c == null ? 0 : 1;
 	}
 	
 	public static double[] generatePropertyArray(Cell[] cells, int propertyIndex)
 	{
 		double[] values = new double[cells.length];
 		
 		for(int i = 0; i < cells.length; i++)
 			if(cells[i] != null)
 				values[i] = cells[i].getValue(propertyIndex);
 		
 		return values;
 	}
 	
 	public static void move(boolean active, double target, int step, Cell cell, Cell[] neighbours)
 	{
 		if(!active)
 		{
 			cell.setPrivateProperty("target", -1);
 			return;
 		}
 		
 		if(step == 0)
 		{
 			cell.setPrivateProperty("target", target);
 		}
 		else if(step == 1)
 		{
 			cell.setPrivateProperty("parent", -1);
 		}
 		else if(step == 2)
 		{
 			if(active)
 			{
 				for(int i = 0; i < neighbours.length; i++)
 				{
 					int j = neighbours.length - (i+1);
 					if(neighbours[i] != null && neighbours[i].getPrivateProperty("parent") == j)
 					{
 						cell.setValue(0, neighbours[i].getValue(0));
 						cell.setPrivateProperty("target", -1);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Behaviour function. Cell acceptor for movement engine.
 	 * @param active Indicates if the behaviour is currently active for this cell
 	 * @param step The current movementStep
 	 * @param cell 
 	 * @param neighbours
 	 */
 	public static void accept(boolean active, int step, Cell cell, Cell[] neighbours)
 	{
 		if(!active)
 		{
 			return;
 		}
 		
 		if(step == 0)
 		{
 			cell.setPrivateProperty("target", -1);
 		}
 		else if(step == 1)
 		{
 			boolean done = false;
 			for(int i = 0; i < neighbours.length; i++)
 			{
 				
 				int j = neighbours.length - (i+1);
 				if(neighbours[i] != null && neighbours[i].getPrivateProperty("target") == j)
 				{
 					cell.setPrivateProperty("parent", i);
 					done = true;
 				}
 			}
 			
 			if(!done)
 				cell.setPrivateProperty("parent", -1);
 		}
 		else if(step == 2)
 		{
 			if(cell.getPrivateProperty("parent") != -1)
 			{
 				cell.setValue(0, neighbours[(int)cell.getPrivateProperty("parent")].getValue(0));
 			}
 			cell.setPrivateProperty("parent", -1);
 		}
 		
 	}
 	
 	public static void propagate(boolean active, double index, Cell cell, Cell[] neighbours)
 	{
 		//Hehe ;)
 		double x = StdLib.max(StdLib.generatePropertyArray(neighbours, (int)index));
 		
 		if((x > cell.getValue((int)index)) && (cell.getPrivateProperty("sat1") <= 0) && (cell.getPrivateProperty("sat2") <= 0))
 		{
 			cell.setValue((int)index, x-1);
 			cell.setPrivateProperty("sat1", 1);
 		}
 		else if(cell.getPrivateProperty("sat1") == 1)
 		{
 			cell.setValue((int)index, 0);
 			
 			if(cell.getPrivateProperty("sat2") == 1)
 			{
 				cell.setPrivateProperty("sat2", 0);
 				cell.setPrivateProperty("sat1", 0);
 			}
 			else
 				cell.setPrivateProperty("sat2", 1);
 		}
 	}
 }
