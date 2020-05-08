 import java.util.ArrayList;
 
 /**File: Water.java
  * 
  * Version:
  * $Id$
  * 
  * Revisions:
  * $Log$
  */
 import java.util.*;
 
 /**
  * @author Andrew Glaude (ajg2440@rit.edu)
  *
  */
 public class Water implements Puzzle, Comparable<Water>{
 	//Capacity of jugs
 	private ArrayList<Integer> jugCapacity;
 	//Actual amount of water in each jug
 	private ArrayList<Integer> jugs;
 	
 	private int goalWater;
 
 	/**
 	 * @see Puzzle#getGoal()
 	 */
 	@Override
 	public int getGoal() {
 		return goalWater;
 	}
 	
 	public Water(ArrayList<Integer> jugCap, ArrayList<Integer> jugs, int goal)
 	{
 		this.jugCapacity = jugCap;
 		this.jugs = jugs;
 		this.goalWater = goal;
 	}
 
 	/**
 	 * @see Puzzle#getNeighbors(int)
 	 */
 	public ArrayList<ArrayList<Integer>> getNeighbors(ArrayList<Integer> config) {
 		ArrayList<ArrayList<Integer>> newConfigs = new ArrayList<ArrayList<Integer>>();
 		
 		
 		//Adds configs where you fill a jug
 		for(int i = 0; i < jugs.size(); i++)
 		{
 			ArrayList<Integer> newTemp = new ArrayList<Integer>();
 			Collections.copy(newTemp, config);
 			newTemp.set(i, jugCapacity.get(i));
 			newConfigs.add(newTemp);
 		}
 		//Adds configs where you empty a jug
 		for(int i = 0; i < jugs.size(); i++)
 		{
 			ArrayList<Integer> newTemp = new ArrayList<Integer>();
 			Collections.copy(newTemp, config);
 			newTemp.set(i, 0);
 			newConfigs.add(newTemp);
 		}
 		//Adds configs where you pour from one to another
 		//Note pours from j to i
 		for(int i = 0; i < jugs.size(); i++)
 		{
 			for(int j = 0; j < jugs.size(); j++)
 			{
 				ArrayList<Integer> newTemp = new ArrayList<Integer>();
 				Collections.copy(newTemp, config);
 				if(jugCapacity.get(i) > (newTemp.get(j) + newTemp.get(i)))
 				{
 					int oldI = newTemp.get(i);
 					int newI = newTemp.get(i) + newTemp.get(j);
 					newTemp.set(i, newI);
 					newTemp.set(j, (newTemp.get(j) - (newI - oldI)));
 					newConfigs.add(newTemp);
 				}
 			}
 		}
 		return newConfigs;
 	}
 	/**
 	 * Compares if they are equal (To say one is greater than another doesn't make sense)
 	 * @param the Water object to compare to
 	 * @return 0 if equal 1 otherwise
 	 */
 	public int compareTo(Water w)
 	{
 		if(this.jugs.equals(w.jugs))
 			return 0;
 		return 1;
 	}
 	
 	/**
 	 * @see Puzzle#getStart()
 	 */
 	@Override
	public ArrayList<Integer> getStart() {
		return jugs;
 	}
 
 	@Override
 	public boolean isSolution(ArrayList<Integer> config)
 	{
 		for(int i : config)
 		{
 			if (i == goalWater)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
