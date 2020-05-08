 package com.benjaminlanders.taptorun.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class World 
 {
 	public List<Box> boxes;
 	public Player player;
 	public float score=0;
 	public World()
 	{
 		boxes = new ArrayList<Box>();
 		reset();
 	}
 	public void reset()
 	{
 		boxes.clear();
 		boxes.add(new Box(Box.BLOCK,.1f,.2f,.5f,.2f));
 		boxes.add(new Box(Box.BLOCK,.5f,.3f,.5f,.2f));
 		boxes.add(new Box(Box.BLOCK,1f,.4f,.5f,.2f));
 		boxes.add(new Box(Box.BLOCK,1.7f,.2f,.5f,.2f));
 		boxes.add(new Box(Box.BLOCK,2.2f,.1f,.5f,.2f));
 		player = new Player(.3f,.6f, .20f,.2f);
 	}
 }
