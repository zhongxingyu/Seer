 package dk.itu.mario.level;
 
 // I can't commit
 
 import java.util.Random;
 
 import dk.itu.mario.MarioInterface.Constraints;
 import dk.itu.mario.MarioInterface.GamePlay;
 import dk.itu.mario.MarioInterface.LevelInterface;
 import dk.itu.mario.engine.sprites.SpriteTemplate;
 import dk.itu.mario.engine.sprites.Enemy;
 
 
 public class MyLevel extends RandomLevel{
 	//Store information about the level
 	 public   int ENEMIES = 0; //the number of enemies the level contains
 	 public   int BLOCKS_EMPTY = 0; // the number of empty blocks
 	 public   int BLOCKS_COINS = 0; // the number of coin blocks
 	 public   int BLOCKS_POWER = 0; // the number of power blocks
 	 public   int COINS = 0; //These are the coins in boxes that Mario collect
 	 
 	 private static final int ODDS_STRAIGHT = 0;
 	 private static final int ODDS_HILL_STRAIGHT = 1;
 	 private static final int ODDS_TUBES = 2;
      private static final int ODDS_JUMP = 3;
 	 private static final int ODDS_CANNONS = 4;
 	 private static final int ODDS_CANNONJUMP = 5;
 	 
 	 private int[] odds = new int[6];
 	 private int totalOdds = 0;
  
 	private static Random levelSeedRandom = new Random(15);
 	public static long lastSeed;
 
     Random random;
 
   
     private int difficulty;
     private int type;
 	private int gaps;
 	private GamePlay stats;
 	
 	private final double ENEMY_RATIO = 0.70;
 	private final double COIN_RATIO = 0.30;
 	private final double RUN_RATIO = 0.50;
 	
 	private int Goombas_weight = 10;
 	private int JumpFlowers_weight = 10;
 	private int ChompFlowers_weight = 10;
 	private int RedTurtles_weight = 10;
 	private int GreenTurtles_weight = 10;
 	private int CannonBall_weight = 10;
 	private int ArmoredTurtles_weight = 10;
 	private double Enemy_density = 1;
 	
 	private int Previous = -1;
 	
 	public MyLevel(int width, int height)
     {
 		super(width, height);
     }
 
 
 	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
     {
         this(width, height);
         creat(seed, difficulty, type,playerMetrics);
     }
 
     public void creat(long seed, int difficulty, int type, GamePlay playerMetrics)
     {
         this.type = type;
         this.difficulty = difficulty;
         this.stats = playerMetrics;
         for(int i = 0; i < odds.length; i++)
         	odds[i] = 20;
         evaluatePlayer();
 
         lastSeed = seed;
         random = new Random(seed);
         System.out.println("Seed:  " + seed);
         
         for (int i = 0; i < odds.length; i++) 
         {
             //failsafe (no negative odds)
             if (odds[i] < 0) {
                 odds[i] = 0;
             }
 
             totalOdds += odds[i];
             odds[i] = totalOdds - odds[i];
         }
 
         //create the start location
         int length = 0;
         length += buildStraight(0, width, true);
 
         // Build as many sections as we can, each section built is decided randomly using odds
         while (length < width - 64)
         {
         	length += buildSection(length, width - length);
         }
         
 
         //set the end piece
         int floor = height - 1 - random.nextInt(4);
 
         xExit = length + 8;
         yExit = floor;
 
         // fills the end piece
         for (int x = length; x < width; x++)
         {
             for (int y = 0; y < height; y++)
             {
                 if (y >= floor)
                 {
                     setBlock(x, y, GROUND);
                 }
             }
         }
 
         if (type == LevelInterface.TYPE_CASTLE || type == LevelInterface.TYPE_UNDERGROUND)
         {
             int ceiling = 0;
             int run = 0;
             for (int x = 0; x < width; x++)
             {
                 if (run-- <= 0 && x > 4)
                 {
                     ceiling = random.nextInt(4);
                     run = random.nextInt(4) + 4;
                 }
                 for (int y = 0; y < height; y++)
                 {
                     if ((x > 4 && y <= ceiling) || x < 1)
                     {
                         setBlock(x, y, GROUND);
                     }
                 }
             }
         }
 
         fixWalls();
 
     }
     
     /*
      * Still deciding on actual design, so placing any logic I find about learning about the player here
      * 
      */
     
     public void evaluatePlayer()
     {
     	if(this.stats != null)
     	{
     		if(stats.coinsCollected/(stats.totalCoins+1.0) > COIN_RATIO)
     			//IsCoinCollector, increase number of coins at least, probably enemies as well
     		{
     			System.out.println("Collected Coins");
     			odds[ODDS_HILL_STRAIGHT] = (int)(odds[ODDS_HILL_STRAIGHT]*1.5);
     			Enemy_density += 1;
     		}
     		else
     		{
     			odds[ODDS_STRAIGHT] /= 2;
     			System.out.println("Coins: " + stats.coinsCollected + " Total: " + stats.totalCoins);
     		}
     			
     		if(stats.timeSpentRunning/(stats.totalTime+1.0) > RUN_RATIO)
     		{
     			System.out.println("Runs A Lot");
     			odds[ODDS_STRAIGHT] /= 2;
     			odds[ODDS_CANNONS] = (int) (odds[ODDS_CANNONS]*1.5);
     			Enemy_density += 1;
     		}
     		else
     		{
     			System.out.println("Run: " + stats.timeSpentRunning + " Total: " + stats.totalTime);
     		}
     		for (int i=0; i < stats.timesOfDeathByFallingIntoGap; i++)
     		{
     			odds[ODDS_JUMP] *= 2;
     			odds[ODDS_CANNONJUMP] = (int) (odds[ODDS_CANNONJUMP]*1.5);
     		}
     		int totalKilled = stats.GoombasKilled + stats.RedTurtlesKilled + stats.GreenTurtlesKilled +  stats.ArmoredTurtlesKilled + stats.CannonBallKilled + stats.JumpFlowersKilled + stats.ChompFlowersKilled;
     		
     		if(totalKilled/(stats.totalEnemies+1.0) > ENEMY_RATIO)
     		{
     			System.out.println("Kills a lot");
     			this.difficulty = (totalKilled/(stats.totalEnemies+1))*10;
     			Enemy_density *= 1.5;
     		}
     		else
     		{
     			this.difficulty = (totalKilled/(stats.totalEnemies+1))*5;
     			System.out.println("Kills: " + totalKilled + " Total: " + stats.totalEnemies);
     		}
     		
     		// Calculate weights to try and introduce as many units that the player can't handle
     		Goombas_weight = 10 + stats.timesOfDeathByGoomba*5 - stats.GoombasKilled;
     		JumpFlowers_weight = 10 + stats.timesOfDeathByJumpFlower*5 - stats.JumpFlowersKilled;
     		ChompFlowers_weight = 10 + stats.timesOfDeathByChompFlower*5 - stats.ChompFlowersKilled;
     		RedTurtles_weight = 10 + stats.timesOfDeathByRedTurtle*5 - stats.RedTurtlesKilled;
     		GreenTurtles_weight = 10 + stats.timesOfDeathByGreenTurtle*5 - stats.GreenTurtlesKilled;
    		CannonBall_weight = 10 + stats.CannonBallKilled*5 - stats.CannonBallKilled;
    		ArmoredTurtles_weight = 10 + stats.ArmoredTurtlesKilled*5 - stats.ArmoredTurtlesKilled;
     		
     		if (CannonBall_weight > 10)
     		{
     			odds[ODDS_CANNONS] *= 2;
     			odds[ODDS_CANNONJUMP] = (int) (odds[ODDS_CANNONJUMP]*1.5);
     		}
     		
     		if ((JumpFlowers_weight + ChompFlowers_weight) > 20)
     			odds[ODDS_TUBES] *= 2;
     		
     		System.out.println("Goomba: " + Goombas_weight + " Jump: " + JumpFlowers_weight + " Chomp: " + ChompFlowers_weight + " Red: " + RedTurtles_weight + " Green: " + GreenTurtles_weight + " Spiky: " + ArmoredTurtles_weight + " Cannon: " + CannonBall_weight);
 
     		odds[ODDS_CANNONJUMP] /= 2;
     		
     		for(int i = 0; i < odds.length; i++)
     		{
     			System.out.println(i + ": " + odds[i]);
     		}
     		
     		
     	}
     	
     }
     
     private int buildSection(int x, int maxLength)
     {
         int val = random.nextInt(totalOdds);
         int type = 0;
 
         for (int i = 0; i < odds.length; i++) 
         {
         	if (odds[i] <= val) 
                 type = i;
         }
         
         if (Previous != type)
         {
         	Previous = type;
 		    switch (type) 
 		    {
 		    case ODDS_STRAIGHT:
 		        return buildStraight(x, maxLength, false);
 		    case ODDS_HILL_STRAIGHT:
 		        return buildHillStraight(x, maxLength);
 		    case ODDS_TUBES:
 		        return buildTubes(x, maxLength);
 		    case ODDS_JUMP:
 		        if (gaps < Constraints.gaps)
 		            return buildJump(x, maxLength);
 		        else
 		            return buildStraight(x, maxLength, false);
 		    case ODDS_CANNONS:
 		        return buildCannons(x, maxLength);
 		    case ODDS_CANNONJUMP:
 		    	return buildCannon_Jump(x,maxLength);
 		    }
         }
         return 0;
     }
 
 
     private int buildJump(int xo, int maxLength)
     {	gaps++;
     	//jl: jump length
     	//js: the number of blocks that are available at either side for free
         int js = random.nextInt(2) + 4;
         int jl = random.nextInt(4) + 4;
         int length = js * 2 + jl;
 
         boolean hasStairs = random.nextInt(3) == 0;
         int fo = random.nextInt(3);
 
         int floor = height - 1 - random.nextInt(4);
       //run from the start x position, for the whole length
         for (int x = xo; x < xo + length; x++)
         {
          //   if (x < xo + js || x > xo + length - js - 1)
             {
             	//run for all y's since we need to paint blocks upward
                 for (int y = 0; y < height; y++)
                 {	//paint ground up until the floor
                     if (y >= floor)
                     {
                     	if(x < xo + js || x > xo + length - js - 1)
                     		setBlock(x, y, GROUND);
                         else
                         {
                         	setBlock(x ,height + 2, COIN);
                         	COINS++;
                         }
                     }
  
                   //if it is above ground, start making stairs of rocks
                     else if (hasStairs)
                     {	//LEFT SIDE
                         if (x < xo + js)
                         { //we need to max it out and level because it wont
                           //paint ground correctly unless two bricks are side by side
                             if (y >= floor - (x - xo) + 1)
                             {
                                 setBlock(x, y, ROCK);
                             }
                         }
                         else if(x > xo + length - js - 1)
                         { //RIGHT SIDE
                             if (y >= floor - ((xo + length) - x) + 2)
                             {
                                 setBlock(x, y, ROCK);
                             }
                         }
                         else if (((floor-3-fo) == y) && (x < (xo + js + 3*jl/4)) && (x > (xo + js + jl/4)))
                 		{
                 			setBlock(x, y,COIN);
                 			COINS++;
                 		}
                     }
                 }
             }
         }
 
         return length;
     }
 
     private int buildCannons(int xo, int maxLength)
     {
         int length = random.nextInt(10) + 2;
         if (length > maxLength) length = maxLength;
 
         int floor = height - 1 - random.nextInt(4);
         int xCannon = xo + 1 + random.nextInt(4);
         for (int x = xo; x < xo + length; x++)
         {
             if (x > xCannon)
             {
                 xCannon += 2 + random.nextInt(4);
             }
             if (xCannon == xo + length - 1) xCannon += 10;
             int cannonHeight = floor - random.nextInt(4) - 1;
 
             for (int y = 0; y < height; y++)
             {
                 if (y >= floor)
                 {
                     setBlock(x, y, GROUND);
                 }
                 else
                 {
                     if (x == xCannon && y >= cannonHeight)
                     {
                         if (y == cannonHeight)
                         {
                             setBlock(x, y, (byte) (14 + 0 * 16));
                         }
                         else if (y == cannonHeight + 1)
                         {
                             setBlock(x, y, (byte) (14 + 1 * 16));
                         }
                         else
                         {
                             setBlock(x, y, (byte) (14 + 2 * 16));
                         }
                     }
                 }
             }
         }
 
         return length;
     }
 
     private int buildHillStraight(int xo, int maxLength)
     {
         int length = random.nextInt(10) + 10;
         if (length > maxLength) length = maxLength;
 
         int floor = height - 1 - random.nextInt(4);
         for (int x = xo; x < xo + length; x++)
         {
             for (int y = 0; y < height; y++)
             {
                 if (y >= floor)
                 {
                     setBlock(x, y, GROUND);
                 }
             }
         }
 
         addEnemyLine(xo + 1, xo + length - 1, floor - 1);
 
         int h = floor;
 
         boolean keepGoing = true;
 
         boolean[] occupied = new boolean[length];
         while (keepGoing)
         {
             h = h - 2 - random.nextInt(3);
 
             if (h <= 0)
             {
                 keepGoing = false;
             }
             else
             {
                 int l = random.nextInt(5) + 3;
                 int xxo = random.nextInt(length - l - 2) + xo + 1;
 
                 if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1])
                 {
                     keepGoing = false;
                 }
                 else
                 {
                     occupied[xxo - xo] = true;
                     occupied[xxo - xo + l] = true;
                     addEnemyLine(xxo, xxo + l, h - 1);
                     if (random.nextInt(4) < 2)
                     {
                         decorate(xxo - 1, xxo + l + 1, h);
                         keepGoing = false;
                     }
                     for (int x = xxo; x < xxo + l; x++)
                     {
                         for (int y = h; y < floor; y++)
                         {
                             int xx = 5;
                             if (x == xxo) xx = 4;
                             if (x == xxo + l - 1) xx = 6;
                             int yy = 9;
                             if (y == h) yy = 8;
 
                             if (getBlock(x, y) == 0)
                             {
                                 setBlock(x, y, (byte) (xx + yy * 16));
                             }
                             else
                             {
                                 if (getBlock(x, y) == HILL_TOP_LEFT) setBlock(x, y, HILL_TOP_LEFT_IN);
                                 if (getBlock(x, y) == HILL_TOP_RIGHT) setBlock(x, y, HILL_TOP_RIGHT_IN);
                             }
                         }
                     }
                 }
             }
         }
 
         return length;
     }
 
     private void addEnemyLine(int x0, int x1, int y)
     {
     	int Enemy_start = ENEMIES;
         for (int x = x0; x < x1; x++)
         {
            if ((random.nextInt(15) < difficulty + Enemy_density) && ((ENEMIES - Enemy_start) < (x1-x0)/3))
             {
                 int choice = random.nextInt(Goombas_weight + RedTurtles_weight + GreenTurtles_weight + ArmoredTurtles_weight);
                 int type = random.nextInt(4);
                 
         		if (choice < Goombas_weight)
         			type = Enemy.ENEMY_GOOMBA;
         		else if (choice < GreenTurtles_weight+Goombas_weight)
         			type = Enemy.ENEMY_GREEN_KOOPA;
         		else if (choice < RedTurtles_weight+GreenTurtles_weight+Goombas_weight)
         			type = Enemy.ENEMY_RED_KOOPA;
         		else if (choice < ArmoredTurtles_weight+RedTurtles_weight+GreenTurtles_weight+Goombas_weight)
         			type = Enemy.ENEMY_SPIKY;
         		
                 setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(65) < difficulty*Enemy_density));
                 ENEMIES++;
             }
         }
     }
     // Must hit a cannon to finish jump
     private int buildCannon_Jump(int xo, int maxLength)
     {
     	int js = random.nextInt(2) + 8;
         int jl = random.nextInt(2) + 10;
         int length = js * 2 + jl;
         
         int floor = height - 1 - random.nextInt(4);
         //run from the start x position, for the whole length
         for (int x = xo; x < xo + length; x++)
         {
     		if (x < xo + js || x > xo + length - js - 1)
     		{
 	      	//run for all y's since we need to paint blocks upward
 				for (int y = 0; y < height; y++)
 				{	//paint ground up until the floor
 					if (y >= floor)
 					{
 						setBlock(x, y, GROUND);
 					}
 				}
     		}
     		else if ( (x < (xo + js + 3*jl/4)) && (x > (xo + js + jl/4)))
     		{
     			setBlock(x, floor-3,COIN);
     			COINS++;
     		}
     		if (((x-xo) == (js - 8)) || ((x-xo) == (2*js+jl-1)))
     		{
 	    		for (int y = 0; y < height; y++)
 	            {
 	                if (y >= floor)
 	                {
 	                    setBlock(x, y, GROUND);
 	                }
 	                else
 	                {
 	                    if (y >= floor - 1)
 	                    {
 	                        if (y == floor - 1)
 	                        {
 	                            setBlock(x, y, (byte) (14 + 0 * 16));
 	                        }
 	                        else if (y == floor)
 	                        {
 	                            setBlock(x, y, (byte) (14 + 1 * 16));
 	                        }
 	                        else
 	                        {
 	                            setBlock(x, y, (byte) (14 + 2 * 16));
 	                        }
 	                    }
 	                }
 	            }
     		}
         }
         if (random.nextInt(20) > (difficulty+10))
         	decorate(xo+js+2, xo + js + jl - 2, floor);
 	
         return length;
     }
 
     private int buildTubes(int xo, int maxLength)
     {
         int length = random.nextInt(10) + 5;
         if (length > maxLength) length = maxLength;
 
         int floor = height - 1 - random.nextInt(4);
         int xTube = xo + 1 + random.nextInt(4);
         int tubeHeight = floor - random.nextInt(2) - 2;
         for (int x = xo; x < xo + length; x++)
         {
             if (x > xTube + 1)
             {
                 xTube += 3 + random.nextInt(4);
                 tubeHeight = floor - random.nextInt(2) - 2;
             }
             if (xTube >= xo + length - 2) xTube += 10;
 
             if (x == xTube)
             {
                 setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, random.nextInt(ChompFlowers_weight) > random.nextInt(JumpFlowers_weight)));
                 ENEMIES++;
             }
 
             for (int y = 0; y < height; y++)
             {
                 if (y >= floor)
                 {
                     setBlock(x, y,GROUND);
 
                 }
                 else
                 {
                     if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
                     {
                         int xPic = 10 + x - xTube;
 
                         if (y == tubeHeight)
                         {
                         	//tube top
                             setBlock(x, y, (byte) (xPic + 0 * 16));
                         }
                         else
                         {
                         	//tube side
                             setBlock(x, y, (byte) (xPic + 1 * 16));
                         }
                     }
                 }
             }
         }
 
         return length;
     }
 
     private int buildStraight(int xo, int maxLength, boolean safe)
     {
         int length = random.nextInt(10) + 2;
 
         if (safe)
         	length = 10 + random.nextInt(5);
 
         if (length > maxLength)
         	length = maxLength;
 
         int floor = height - 1 - random.nextInt(4);
 
         //runs from the specified x position to the length of the segment
         for (int x = xo; x < xo + length; x++)
         {
             for (int y = 0; y < height; y++)
             {
                 if (y >= floor)
                 {
                     setBlock(x, y, GROUND);
                 }
             }
         }
 
         if (!safe)
         {
             if (length > 5)
             {
                 decorate(xo, xo + length, floor);
             }
         }
 
         return length;
     }
 
     private void decorate(int xStart, int xLength, int floor)
     {
     	//if its at the very top, just return
         if (floor < 1)
         	return;
 
         //        boolean coins = random.nextInt(3) == 0;
         boolean rocks = true;
 
         //add an enemy line above the box
         addEnemyLine(xStart + 1, xLength - 1, floor - 1);
 
         int s = random.nextInt(4);
         int e = random.nextInt(2);
 
         if (floor - 2 > 0){
             if ((xLength - 1 - e) - (xStart + 1 + s) > 1){
                 for(int x = xStart + 1 + s; x < xLength - 1 - e; x++){
                     setBlock(x, floor - 2, COIN);
                     COINS++;
                 }
             }
         }
 
         s = random.nextInt(4);
         e = random.nextInt(2);
         
         //this fills the set of blocks and the hidden objects inside them
         if (floor - 4 > 0)
         {
             if ((xLength - 1 - e) - (xStart + 1 + s) > 2)
             {
                 for (int x = xStart + 1 + s; x < xLength - 1 - e; x++)
                 {
                     if (rocks)
                     {
                         if (x != xStart + 1 && x != xLength - 2 && random.nextInt(3) == 0)
                         {
                             if (random.nextInt(4) == 0)
                             {
                                 setBlock(x, floor - 4, BLOCK_POWERUP);
                                 BLOCKS_POWER++;
                             }
                             else
                             {	//the fills a block with a hidden coin
                                 setBlock(x, floor - 4, BLOCK_COIN);
                                 BLOCKS_COINS++;
                             }
                         }
                         else if (random.nextInt(4) == 0)
                         {
                             if (random.nextInt(4) == 0)
                             {
                                 setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                             }
                             else
                             {
                                 setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                             }
                         }
                         else
                         {
                             setBlock(x, floor - 4, BLOCK_EMPTY);
                             BLOCKS_EMPTY++;
                         }
                     }
                 }
             }
         }
     }
 
     private void fixWalls()
     {
         boolean[][] blockMap = new boolean[width + 1][height + 1];
 
         for (int x = 0; x < width + 1; x++)
         {
             for (int y = 0; y < height + 1; y++)
             {
                 int blocks = 0;
                 for (int xx = x - 1; xx < x + 1; xx++)
                 {
                     for (int yy = y - 1; yy < y + 1; yy++)
                     {
                         if (getBlockCapped(xx, yy) == GROUND){
                         	blocks++;
                         }
                     }
                 }
                 blockMap[x][y] = blocks == 4;
             }
         }
         blockify(this, blockMap, width + 1, height + 1);
     }
 
     private void blockify(Level level, boolean[][] blocks, int width, int height){
         int to = 0;
         if (type == LevelInterface.TYPE_CASTLE)
         {
             to = 4 * 2;
         }
         else if (type == LevelInterface.TYPE_UNDERGROUND)
         {
             to = 4 * 3;
         }
 
         boolean[][] b = new boolean[2][2];
 
         for (int x = 0; x < width; x++)
         {
             for (int y = 0; y < height; y++)
             {
                 for (int xx = x; xx <= x + 1; xx++)
                 {
                     for (int yy = y; yy <= y + 1; yy++)
                     {
                         int _xx = xx;
                         int _yy = yy;
                         if (_xx < 0) _xx = 0;
                         if (_yy < 0) _yy = 0;
                         if (_xx > width - 1) _xx = width - 1;
                         if (_yy > height - 1) _yy = height - 1;
                         b[xx - x][yy - y] = blocks[_xx][_yy];
                     }
                 }
 
                 if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
                 {
                     if (b[0][0] == b[0][1])
                     {
                         if (b[0][0])
                         {
                             level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                         }
                         else
                         {
                             // KEEP OLD BLOCK!
                         }
                     }
                     else
                     {
                         if (b[0][0])
                         {
                         	//down grass top?
                             level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                         }
                         else
                         {
                         	//up grass top
                             level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                         }
                     }
                 }
                 else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
                 {
                     if (b[0][0])
                     {
                     	//right grass top
                         level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                     }
                     else
                     {
                     	//left grass top
                         level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                     }
                 }
                 else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
                 {
                     level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                 }
                 else if (b[0][0] == b[1][0])
                 {
                     if (b[0][0])
                     {
                         if (b[0][1])
                         {
                             level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                         }
                         else
                         {
                             level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                         }
                     }
                     else
                     {
                         if (b[0][1])
                         {
                         	//right up grass top
                             level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                         }
                         else
                         {
                         	//left up grass top
                             level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                         }
                     }
                 }
                 else if (b[0][1] == b[1][1])
                 {
                     if (b[0][1])
                     {
                         if (b[0][0])
                         {
                         	//left pocket grass
                             level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                         }
                         else
                         {
                         	//right pocket grass
                             level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                         }
                     }
                     else
                     {
                         if (b[0][0])
                         {
                             level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                         }
                         else
                         {
                             level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                         }
                     }
                 }
                 else
                 {
                     level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                 }
             }
         }
     }
     
     public RandomLevel clone() throws CloneNotSupportedException {
 
     	RandomLevel clone=new RandomLevel(width, height);
 
     	clone.xExit = xExit;
     	clone.yExit = yExit;
     	byte[][] map = getMap();
     	SpriteTemplate[][] st = getSpriteTemplate();
     	
     	for (int i = 0; i < map.length; i++)
     		for (int j = 0; j < map[i].length; j++) {
     			clone.setBlock(i, j, map[i][j]);
     			clone.setSpriteTemplate(i, j, st[i][j]);
     	}
     	clone.BLOCKS_COINS = BLOCKS_COINS;
     	clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
     	clone.BLOCKS_POWER = BLOCKS_POWER;
     	clone.ENEMIES = ENEMIES;
     	clone.COINS = COINS;
     	
         return clone;
 
       }
 
 
 }
