 package com.benjaminlanders.taptorun.controller;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.benjaminlanders.taptorun.helper.HighScroreKeeper;
 import com.benjaminlanders.taptorun.helper.SoundManager;
 import com.benjaminlanders.taptorun.model.Box;
 import com.benjaminlanders.taptorun.model.World;
 
 public class Controller
 {
 	World world;
 	public int ref;
 	
 	public Controller(World world)
 	{
 		this.world = world;
		ref = 4;
 	}
 	
 	public void update(float delta)
 	{
 		
 		if(world.player.alive)
 		{
 			updateBlocks(delta);
 			playerCollision(delta);
 			world.player.y += world.player.vY;
 			world.score += delta*5;
 			if(world.player.y + world.player.h/2 < 0)
 			{
 				world.player.alive = false;
 				HighScroreKeeper.writeScoreToFile((int)world.score);
 			}
 		}else
 		{
 			updateBlocks(delta/10);
 		}
 		
 	}
 	
 	private void updateBlocks(float delta)
 	{
 		for(int i = 0; i < world.boxes.length;i++)
 		{
 			Box box = world.boxes[i];
 			if(box.x + box.w < 0)
 			{
 				box.y = (float) Math.random()/2-.30f + this.world.boxes[ref].y;
 				box.y = (box.y > 0)?box.y:0;
 				box.y += (Math.abs(box.y-this.world.boxes[ref].y) < .1f)?.1f:0;
 				box.y = (box.y < .66f)?box.y:.66f;
 				box.w = (float) Math.random()/3+.33f;
 				box.x = this.world.boxes[ref].x + this.world.boxes[ref].w + (((float) Math.random() > .2f)?(float) Math.random()/3:0);				
 				ref = i;
 				
 			}else
 			{
 				box.x -= delta*.5f;
 			}
 			
 		}
 	}
 	
 	private void playerCollision(float delta)
 	{
 		List<Box> collided = new ArrayList<Box>();
		boolean flying = true;
 		for(Box block: world.boxes)
 		{
 			if(block.collides(world.player.x, world.player.y - world.player.h/2))
			{
 				collided.add(block);
				flying = false;
			}
 		}
 		if(!collided.isEmpty())
 		{
 			world.player.x -= .0017f;
 			for(Box collides: collided)
 			{
 				if(world.player.y - world.player.h/2 <= collides.y+collides.h)
 				{
 					world.player.y = collides.y+collides.h+world.player.h/2-.01f;
 					world.player.vY = 0;
 					if(Gdx.input.isTouched()||Gdx.input.isKeyPressed(Input.Keys.SPACE))
 					{
 						world.player.vY = .02f;
 						SoundManager.playJump();
 					}
 				}
 			}
 		}else
 		{
 			world.player.vY-=.04f*delta;
 			world.player.x +=.001;
 		}
 		collided.clear();
 		for(Box block: world.boxes)
 		{
 			if(block.collides(world.player.x+world.player.w/2, world.player.y))
 				collided.add(block);
 		}
 		if(!collided.isEmpty())
 		{
 			for(Box collides: collided)
 			{
 				if(world.player.x + world.player.w/2 > collides.x)
 				{
 					world.player.x = collides.x - world.player.w/2;
					if(flying)
					{
						world.player.x -= .02f;
						world.player.vY = .01f;
					}
 				}
 			}
 		}
 		collided.clear();
 	}
 }
