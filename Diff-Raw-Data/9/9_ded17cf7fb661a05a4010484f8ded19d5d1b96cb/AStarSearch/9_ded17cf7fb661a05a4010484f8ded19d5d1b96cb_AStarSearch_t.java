 /*
  * This file is part of the search package.
  *
  * Copyright (C) 2012, Eric Fritz
  * Copyright (C) 2012, Reed Johnson
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
  * and associated documentation files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following conditions: 
  * 
  * The above copyright notice and this permission notice shall be included in all copies or 
  * substantial portions of the Software. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package edu.uwm.ai.search;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 
 /**
  * @author Eric Fritz
  * @author Reed Johnson
  */
 public class AStarSearch extends BaseSearchAlgorithm
 {
 	public AStarSearch(World w)
 	{
 		super(w);
 	}
 
 	@Override
 	public SearchResult search(Point initial, final Point goal)
 	{
		final Map<Point, Point> pred = new HashMap<Point, Point>();

 		PriorityQueue<Point> successors = new PriorityQueue<Point>(16, new Comparator<Point>() {
 			@Override
 			public int compare(Point o1, Point o2)
 			{
				double h1 = heuristic(o1, goal) + backtrace(pred, o1).size();
				double h2 = heuristic(o2, goal) + backtrace(pred, o2).size();

				return (int) (h1 - h2);
 			}
 		});
 
 		successors.add(initial);
 		pred.put(initial, null);
 
 		int cost = 0;
 		while (!successors.isEmpty()) {
 			cost++;
 			Point current = successors.poll();
 
 			if (current.equals(goal)) {
 				return new SearchResult(backtrace(pred, current), cost);
 			}
 
 			for (Point successor : getSuccessors(current)) {
 				if (!hasKey(pred, successor)) {
 					pred.put(successor, current);
 					successors.add(successor);
 				}
 			}
 		}
 
 		return new SearchResult(new ArrayList<Point>(), cost);
 	}
 
 	private boolean hasKey(Map<Point, Point> m, Point p)
 	{
 		for (Point a : m.keySet()) {
 			if (a.equals(p)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private double heuristic(Point p, Point goal)
 	{
 		return Math.abs(p.getX() - goal.getX()) + Math.abs(p.getY() - goal.getY());
 	}
 
 	@Override
 	public String toString()
 	{
 		return "A*";
 	}
 }
