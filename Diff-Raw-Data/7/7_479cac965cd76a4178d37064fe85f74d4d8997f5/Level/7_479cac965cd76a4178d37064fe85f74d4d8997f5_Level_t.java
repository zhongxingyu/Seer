 package com.dat255.Wood.model;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 //Use this or the normal one?
 
 public class Level {
 
 	//An array of blocks that our Level will consist off.
 	private Array<Block> blocks;
 
 	//The player
 	private Player player;
 
 	//Get functions
 
 	//Get the blocks
 	public Array<Block> getBlocks()
 	{
 		return blocks;
 	}
 
 	//Get the player
 	public Player getPlayer()
 	{
 		return player;
 	}
 
 	//CONSTRUCTOR
 	public Level()
 	{
 		createLevel();
 	}
 
 	//Creates a for now test Level
 	private void createLevel()
 	{
 
 		blocks = new Array<Block>();
 		FileHandle file = Gdx.files.internal("data/levels/level1.txt");
 
 		String text = file.readString();
 
 
 		for(int y=0;y<16;y++){						
 			for(int x=0;x<16;x++){
 				char c = text.charAt(y*16+x+y*2);
 				String cc= new String(""+c);
 
 				if(cc.equals("1")){
 					blocks.add(new Block(new Vector2(y,x)));
 				}
				
				if(cc.equals("s")){
					player = new Player(new Vector2(y,x));
				}

 			}
 		}	
 	}
 
 }
