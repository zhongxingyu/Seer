 package net.minecraft.src;
 
 import java.util.Random;
 
 import net.minecraft.src.forge.*;
 
 public class LightStoneBall extends Item implements ITextureProvider
 {
 	
 	
 	public LightStoneBall (int i)
 	{
 		super(i);
 		maxStackSize = 64;
 	}
 	
     public String getTextureFile()
     {
            return "/imgz/items.png";
     }
 
 }
