 package com.game.entity;
 
 import org.lwjgl.input.Keyboard;
 
 import com.game.RenderEngine;
 import com.game.world.World;
 
 public class EntityPlayer extends Entity
 {
 	private byte renderAnim;
 	private int wantedSpeed = 500;
 	
 	public EntityPlayer()
 	{
 		xSpeed = wantedSpeed;
 		y = 512 - 128;
 	}
 	
 	public void tick(World world)
 	{
 		anim();
 		
 		box = getNewCollisionBox(32, 64);
 		ySpeed += 100;
 		fixSpeed(world);
 		
 		move(world);
 	}
 	
 	private void fixSpeed(World world)
 	{
 		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
 		{
 			ySpeed -= 200;
 		}
 		
		if(xSpeed < wantedSpeed)
 		{
 			if(blockDown(world))
 			{
 				xSpeed += 75;
 			}
 			else
 			{
 				xSpeed += 25;
 			}
 		}
 		
 		xSpeed = Math.min(xSpeed, wantedSpeed);
 	}
 	
 	private void anim()
 	{
 		renderAnim++;
 		if(renderAnim >= 10)
 		{
 			renderAnim = 0;
 		}
 	}
 	
 	public void render()
 	{
 		RenderEngine.bindTexture("sprites.png");
 		RenderEngine.drawTransparentTexture(x, y, 32, 64, renderAnim < 5 ? 0 : 16, 0, 16, 32);
		if(box != null)
		{
			RenderEngine.setGLColor(0.2f, 0.2f, 0.2f, 0.5f);
			RenderEngine.fillTransparentRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
		}
 	}
 
 	public int getScroll()
 	{
 		 return Math.max(0, x - 256);
 	}
 }
