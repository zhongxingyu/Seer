 package com.benjaminlanders.taptorun.model;
 
 public class World 
 {
 	public Box[] boxes;
 	public Player player;
 	public float score=0;
 	
 	public World()
 	{
		boxes = new Box[5];
 		reset();
 	}
 	
 	public void reset()
 	{
 		score = 0;
 		boxes[0] = new Box(Box.BLOCK,.1f,.2f,.5f,.2f);
 		boxes[1] = (new Box(Box.BLOCK,.5f,.3f,.5f,.2f));
 		boxes[2] = (new Box(Box.BLOCK,1f,.4f,.5f,.2f));
 		boxes[3] = (new Box(Box.BLOCK,1.7f,.2f,.5f,.2f));
 		boxes[4] = (new Box(Box.BLOCK,2.2f,.1f,.3f,.2f));
 		player = new Player(.3f,.6f, .12f,.08f);
 	}
 }
