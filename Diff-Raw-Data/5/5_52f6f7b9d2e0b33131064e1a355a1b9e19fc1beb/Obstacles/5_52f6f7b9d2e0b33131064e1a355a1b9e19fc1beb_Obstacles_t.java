 import java.awt.Color;
 
 public class Obstacles 
 {
 	int level;
 
 	public Obstacles(int level)
 	{
 		this.level = level;
 	}
 		
 	public float[] DrawObstacles(String plane)
 	{
 		float[] positions = new float[120];
 		if (level==1)
 		{
 			for(int i = 0; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 		}
 		if ((level == 2)&&(plane == "x"))
 		{
 			for(int i = 0; i < 15; i++)
 			{
 				positions[i] = 120;
 			}
 			for(int i = 15; i < 30; i++)
 			{
 				positions[i] = (600 - 140);
 			}
 			for(int i =30; i < 45; i++)
 			{
 				positions[i] = (600/2);
 			}
 			for(int i = 45; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 
 		}
 		else if ((level == 2)&&(plane == "y"))
 		{
 			for(int i = 0; i < 15; i++)
 			{
 				positions[i] = (20*i);
 			}
 			for(int i = 15; i < 30; i++)
 			{
 				positions[i] = (20*(i-15));
 			}
 			for(int i = 30; i < 45; i++)
 			{
 				positions[i] = (600 - 20*(i -30));
 			}
 			for(int i = 45; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 		}
 		else if ((level == 3)&&(plane == "x"))
 		{
 			for(int i = 0; i < 10; i++)
 			{
 				positions[i] = (20*i);
 			}
 			for(int i = 10; i < 20; i++)
 			{
 				positions[i] = (600 - (20*((i+1)-10)));
 			}
 			for(int i = 20; i < 30; i++)
 			{
 				positions[i] = (20*(i - 20));
 			}
 			for(int i = 30; i < 40; i++)
 			{
 				positions[i] = (600 - (20*((i+1)-30)));
 			}
 			for(int i = 45; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 		}
 		else if ((level == 3)&&(plane == "y"))
 		{
 			for(int i = 0; i < 10; i++)
 			{
 				positions[i] = (20*i);
 			}
 			for(int i = 10; i < 20; i++)
 			{
 				positions[i] = (20*(i - 10));
 			}
 			for(int i = 20; i < 30; i++)
 			{
 				positions[i] = (600 - (20*((i+1)-20)));
 			}
 			for(int i = 30; i < 40; i++)
 			{
 				positions[i] = (600 - (20*((i+1)-30)));
 			}
 			for(int i = 45; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 		}
 		else if ((level == 4)&&(plane == "x"))
 		{
 			for(int i = 0; i < 20; i++)
 			{
 				positions[i] = 20*i;
 			}
 			for(int i = 20; i < 25; i++)
 			{
 				positions[i] = 380;
 			}
 			for(int i = 25; i < 45; i++)
 			{
 				positions[i] = (600 - 20*(i-25));
 			}
 			for(int i = 45; i<50; i++)
 			{
 				positions[i] = 220;
 			}
 			for(int i = 50; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}
 		}
 		else if ((level == 4)&&(plane == "y"))
 		{
 			for(int i = 0; i < 20; i++)
 			{
 				positions[i] = 140;
 			}
 			for(int i = 20; i< 25; i++)
 			{
 				positions[i] = 160 + ((i-20)*20);
 			}
 			for(int i = 25; i < 45; i++)
 			{
 				positions[i] = (600 - 160);
 			}
 			for(int i = 45; i < 50; i++)
 			{
 				positions[i] = (600 - 160 - (((i-45)+1)*20));
 			}
 			for(int i = 50; i < positions.length; i++)
 			{
 				positions[i] = -1;
 			}	
 		}
 		else if ((level == 5)&&(plane == "x"))
 		{
 			for(int i = 0; i < 15; i++)
 			{
 				positions[i] = 120;
 			}
 			for(int i = 15; i < 30; i++)
 			{
 				positions[i] = (600 - 260);
 			}
 			for(int i =30; i < 45; i++)
 			{
 				positions[i] = (600 - 140);
 			}
 			for(int i = 45; i < 60; i++)
 			{
 				positions[i] = 240;
 			}
 			for(int i = 60; i < 90; i++)
 			{
 				positions[i] = 0;
 			}
 			for(int i = 90; i < 120; i++)
 			{
 				positions[i] = 580;
 			}
 			
 		}
 		else if ((level == 5)&&(plane == "y"))
 		{
 			for(int i = 0; i < 15; i++)
 			{
 				positions[i] = (20*i);
 			}
 			for(int i = 15; i < 30; i++)
 			{
 				positions[i] = (20*(i-15));
 			}
 			for(int i = 30; i < 45; i++)
 			{
 				positions[i] = (600 - 20*(i -30));
 			}
 			for(int i = 45; i < 60; i++)
 			{
 				positions[i] = (600 - 20*(i -45));
 			}
 			for(int i = 60; i < 90; i++)
 			{
 				positions[i] = (20*(i-60));
 			}
 			for(int i = 90; i < 120; i++)
 			{
 				positions[i] = (20*(i-90));
 			}
 		}
 		return positions;
 	}
 	
 	public static Color getColour(int level)
 	{
 		Color[] colors = new Color[5];
         colors[0] = Color.gray;
         colors[1] = Color.cyan;
         colors[2] = Color.darkGray;
         colors[3] = Color.gray;
         colors[4] = Color.magenta;
         
 		if (level==1)
 		{
 			return colors[0];
 		}
 		
 		else if (level==2)
 		{
 			return colors[1];
 		}
 		
 		else if (level==3)
 		{
 			return colors[2];
 		}
 		
 		else if (level==4)
 		{
 			return colors[3];
 		}
 		
 		else if (level ==5)
 		{
 			return colors[4];
 		}
 		
 		else
 		{
 			return colors[0];
 		}
 	}
 	
 	//sets start positions for each level
 	public int getStartPosX(int level) {
 		int xp0=200;
 		if (level==1){
 			xp0=400;
 		}
 		if (level==2){
 			xp0=400;
 		}
 		if (level==3){
			xp0=360;
 		}
 		if (level==4){
			xp0=320;
 		}
 		if (level==5){
 			xp0=400;
 		}
 		return xp0;
 	}
 	public int getStartPosY(int level) {
 		int yp0=300;
 		if (level==1){
 			yp0=300;
 		}
 		if (level==2){
 			yp0=300;
 		}
 		if (level==3){
 			yp0=300;
 		}
 		if (level==4){
 			yp0=80;
 		}
 		if (level==5){
 			yp0=300;
 		}
 		return yp0;
 	}
 	public int getStartPosX2(int level) {
 		int xp20=200;
 		if (level==1){
 			xp20=200;
 		}
 		if (level==2){
 			xp20=200;
 		}
 		if (level==3){
 			xp20=200;
 		}
 		if (level==4){
 			xp20=300;
 		}
 		if (level==5){
 			xp20=200;
 		}
 		return xp20;
 	}
 	public int getStartPosY2(int level) {
 		int yp20=300;
 		if (level==1){
 			yp20=300;
 		}
 		if (level==2){
 			yp20=300;
 		}
 		if (level==3){
 			yp20=300;
 		}
 		if (level==4){
 			yp20=500;
 		}
 		if (level==5){
 			yp20=300;
 		}
 		return yp20;
 	}
 	public int getDx(int level) {
 		int dx=0;
 		if (level==1){
 			dx=0;
 		}
 		if (level==2){
 			dx=0;
 		}
 		if (level==3){
 			dx=0;
 		}
 		if (level==4){
 			dx=20;
 		}
 		if (level==5){
 			dx=0;
 		}
 		return dx;
 	}
 	public int getDy(int level) {
 		int dy=20;
 		if (level==1){
 			dy=20;
 		}
 		if (level==2){
 			dy=20;
 		}
 		if (level==3){
 			dy=20;
 		}
 		if (level==4){
 			dy=0;
 		}
 		if (level==5){
 			dy=20;
 		}
 		return dy;
 	}
 	public int getDx2(int level) {
 		int dx2=0;
 		if (level==1){
 			dx2=0;
 		}
 		if (level==2){
 			dx2=0;
 		}
 		if (level==3){
 			dx2=0;
 		}
 		if (level==4){
 			dx2=20;
 		}
 		if (level==5){
 			dx2=0;
 		}
 		return dx2;
 	}
 	public int getDy2(int level) {
 		int dy2=20;
 		if (level==1){
 			dy2=20;
 		}
 		if (level==2){
 			dy2=-20;
 		}
 		if (level==3){
 			dy2=20;
 		}
 		if (level==4){
 			dy2=0;
 		}
 		if (level==5){
 			dy2=20;
 		}
 		return dy2;
 	}
 	
 }
