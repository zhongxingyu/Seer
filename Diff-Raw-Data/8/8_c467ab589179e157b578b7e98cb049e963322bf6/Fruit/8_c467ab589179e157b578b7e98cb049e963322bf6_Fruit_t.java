 import java.awt.Color;
 import java.util.LinkedList;
 
 public class Fruit 
 {
 	int x, y;
 	Color type;
 	int count;
 	
 	int animateOffset = 20;
 
 	public Fruit(int col)
 	{
 		y = -40; 
 		x = col*40;
 
 		type = new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
 
 	}
 
 	public boolean fall(Fruit[][] field)
 	{
 		return hasFallen(field, false);
 	}
 	
 	public boolean fall(Fruit[][] field, boolean fakefall)
 	{
 		return hasFallen(field, fakefall);
 	}
 
 
 	public boolean hasFallen(Fruit[][] field, boolean fakefall)
 	{
 		boolean fallen = false;
 
 		fallen = (y >= (320 + animateOffset) || (belowFruit(field) != null && y >= ((int)(y/40))*40 + animateOffset));
 
 		if (fallen)
 		{
 			y = ((int)(y/40))*40;
 
 			if (field[x/40][y/40] == null)
 				field[x/40][y/40] = this;
 			else
 				fallen = true;
 
 			fallen = true;
 			animateOffset = 0;
 		}
 		else
 		{
 			y+= 5;
 			animateOffset = 20;
 		}
 		
 		System.out.println("falling");
 
 		return fallen;
 
 	}
 	
 	Fruit aboveFruit(Fruit[][] matrix)
 	{
 		return matrix[x/40][y/40-1];
 	}
 	
 	Fruit belowFruit(Fruit[][] matrix)
 	{
 		return matrix[x/40][y/40+1];
 	}
 	
 	Fruit leftFruit(Fruit[][] matrix)
 	{
 		return matrix[x/40-1][y/40];
 	}
 	
 	Fruit rightFruit(Fruit[][] matrix)
 	{
 		return matrix[x/40+1][y/40];
 	}
 
 
 	public void applyDiff(int mouseDiffX, int mouseDiffY, Fruit[][] matrix) 
 	{
 		x += mouseDiffX;
 		y += mouseDiffY;
 		
 	}
 
 	public void moveHorizontal(int i) 
 	{
 		x += i;
 		
 		if (x<0)
 			x = 360;
 		
 		if (x>360)
 			x = 0;
 		
 	}
 	
 	public void moveVertical(int i) 
 	{
 		y += i;
 		
 		if (y>320)
 		{
 			y = -40;
 		}
 		
 	}
 
 	public boolean moveRight(Fruit[][] field) 
 	{
 		boolean moved = false;
 
 		moved = count>=9;
 
 		if (moved)
 		{
 			x = ((int)(x/40))*40;
 
 
 			field[x/40][y/40] = this;
 
 			moved = true;
 			animateOffset = 0;
 			count = 0;
 		}
 		else
 		{
 			x += 5;
 			count ++;
 			animateOffset = 20;
 			if (x > 380)
 				x = -20;
 		}
 
 		return moved;
 	}
 
 	public boolean moveLeft(Fruit[][] field) 
 	{
 		boolean moved = false;
 
 		moved = count>=9;
 
 		if (moved)
 		{
			int divisor = (x<20 || x == 360)? 0 : 1;
			
			x = ((int)(x/40)+divisor)*40;
 
 			field[x/40][y/40] = this;
 
 			moved = true;
 			animateOffset = 0;
 			count = 0;
 		}
 		else
 		{
 			x -= 5;
 			count ++;
 			animateOffset = 20;
 			if (x < -20)
			{
 				x = 380;
			}
 		}
 
 		return moved;
 	}
 
 }
