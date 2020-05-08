 public class Mho extends Item{
 
 	boolean alive = true;
 
 	public Mho(Grid g, int x, int y){
 
 		super(g,x,y);
 		name = 'M';
 		color = 4;
 	
 	}
 
 	public void act(){
 
 		int playerX = 0;
 		int playerY = 0;
 
 		//First, to find the players location.
 		for(int x = 1; x < 11; x++)
 		{
 			boolean keepLooking = true;
 
 			for(int y = 1; y < 11; y++)
 			{
 				if(myGrid.whatsAt(x, y).whoAmI() == 'Y')
 				{
					keepLooking = false
 					playerX = x;
 					playerY = y;
 				}
 			}
 		}
 
 		if(playerX == myX)
 		{
 			if(myY < playerY){
 				attemptMove(myX, myY + 1);
 			} else {
 				attemptMove(myX, myY - 1);
 			}
 		} else if (playerY == myY) {
 			if(myX < playerX){
 				attemptMove(myX + 1, myY);
 			} else {
 				attemptMove(myX - 1, myY);
 			}
 		}
 	}
 
 	public boolean alive()
 	{
 		return alive;
 	}
 
 	private void attemptMove(int x, int y)
 	{
 		if(myGrid.whatsAt(x, y).whoAmI() == 'F')
 		{
 			alive = false;
 			myGrid.removeItem(myX, myY);	
 		} else if(myGrid.whatsAt(x, y).whoAmI() == 'Y') {
 			System.exit(0);
 		} else {
 			myGrid.translateItem(myX, myY, x ,y);
 		} 
 	}
 
	public String whoAmI(){

		return "Mho";

	}
 
 }
