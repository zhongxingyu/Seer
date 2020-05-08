 /**
  * 
  */
 package name.blah;
 
 import java.util.Random;
 
 enum Direction {
 	NORTH, SOUTH, EAST, WEST
 }
 
 /**
  * @author Jett Peterson
  *
  */
 public class RoguelikeMap {
 	
 	String[][] map;
 	int playerX;
 	int playerY;
 	
 	RoguelikeMap()
 	{
 		map = new String[24][24];
 		
 		//Fill the map with the base tile - Impassable terrain
 		for(int i = 0; i < 24; i++)
 		{
 			for(int j = 0; j < 24; j++)
 			{
 				if((i > 2 && i < 21) && j == 11)
 					map[i][j] = ".";
 				else if((j > 3 && j < 20) && i == 11)
 					map[i][j] = ".";
 				else
					map[i][j] = "#";
 			}
 		}
 		
 		Random rand = new Random();
 		
 		int halls, length, dir, x, y;
 		halls = 2;
 		
 		while(halls > 0)
 		{
 			x = rand.nextInt(24);
 			y = rand.nextInt(24);
 			length = rand.nextInt(8) + 4;
 			if(map[x][y].equals("."))
 			{
 				dir = rand.nextInt(4);
 				switch(dir)
 				{
 				case 0:	//Go north
 					for(int j = 0; j < length; j++)
 					{
 						if(x+j < 24)
 							map[x+j][y] = ".";
 					}
 					break;
 
 				case 1:	//Go south
 					for(int j = 0; j < length; j++)
 					{
 						if(x-j >= 0)
 							map[x-j][y] = ".";
 					}
 					break;
 
 				case 2: //Go east
 					for(int j = 0; j < length; j++)
 					{
 						if(y+j < 24)
 							map[x][y+j] = ".";
 					}
 					break;
 
 				case 3:	//Go west
 					for(int j = 0; j < length; j++)
 					{
 						if(y-j >= 0)
 							map[x][y-j] = ".";
 					}
 					break;
 				}
 				halls--;
 			}
 		}
 		
 		/*
 		for(int i = 11; i < 14; i++)
 		{
 			for(int j = 11; j < 14; j++)
 			{
 				map[i][j] = ".";
 			}
 		}
 		*/
 		
 		for(int i = 1500; i > 1; i--)
 		{
 			x = rand.nextInt(24);
 			y = rand.nextInt(24);
 			
 			if(x < 0)
 				x *= -1;
 			if(y < 0)
 				y *= -1;
 			System.out.println("X: " + x + ", Y: " + y);
 			
 			
 			if(this.checkAdjacent(x, y) > 0)
 			{
 				map[x][y] = ".";
 			}
 		}
 		
 	}
 	
 	public String toString()
 	{
 		String out = "";
 		for(int i = 0; i < 24; i++)
 		{
 			for(int j = 0; j < 24; j++)
 			{
 				if(i == playerX && j == playerY)
 					out += "@";
 				else
 					out += map[i][j];
 			}
 			out += "\n";
 		}
 		return out;
 	}
 	
 	private int checkAdjacent(int x, int y)
 	{
 		if((x-1 > 0) && map[x-1][y].equals("."))
 		{
 			//this.pickFeature(x, y, Direction.EAST);
 			return 0;
 		}	
 		else if((x+1 < 24) && map[x+1][y].equals("."))
 		{
 			//this.pickFeature(x, y, Direction.WEST);
 			return 1;
 		}
 		else if((y-1 > 0) && map[x][y-1].equals("."))
 		{
 			//this.pickFeature(x, y, Direction.SOUTH);
 			return 2;
 		}
 		else if((y+1 < 24) && map[x][y+1].equals("."))
 		{
 			//this.pickFeature(x, y, Direction.NORTH);
 			return 3;
 		}
 		return -1;
 	}
 	
 	public boolean checkOpen(int x, int y)
 	{
		if(map[x][y].equals("."))
 			return true;
 		return false;
 	}
 	
 	public void setPlayer(int x, int y)
 	{
 		playerX = x;
 		playerY = y;
 	}
 
 }
