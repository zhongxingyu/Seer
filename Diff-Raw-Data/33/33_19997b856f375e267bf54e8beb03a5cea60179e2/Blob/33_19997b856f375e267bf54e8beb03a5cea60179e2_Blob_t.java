 package zed;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 
 public class Blob extends GCharacter {
 
 	static int Type = 2;
 	private boolean moving = false;
 	private long started_moving;
 	
 	// Rat default constructor does nothing
 	public Blob() throws SlickException{
 	}
 	
 	// Rat constructor
 	public Blob(int tile_x, int tile_y, SpriteSheet sprites) throws SlickException {
 
 		Tilesize = 16;
 		Animation[] zombie_animations = new Animation[4];
 		
 		// Define index of first animation on spritesheet
 		int i0 = 18;
 		// Define relative indexes of animations on spritesheet
 		int[] spritesheet_index = {i0, i0 + 1, i0 + 2, i0 + 3};
 		// Define the length of each animation
 		int[] animation_length = {3, 3, 3, 3};
 		// Define whether each animation loops
 		boolean[] looping = {true, true, true, true};
 		// Define how each animation is shifted relative to position
 		int[] sprite_shift_x = {0, 0, 0, 0};
 		int[] sprite_shift_y = {16, 16, 16, 16};
 		
 		// Initialize walking and standing animations
         for (int i = 0; i < 4; i++)
         {
         	STUN_TIME = 1000; // Set the amount of time stunned if
         	                  // hit by player's sword
         	
 	        zombie_animations[i] = new Animation(
 	        		sprites, // spritesheet to use
 	        		// location of first sprite in spritesheet
 	                0,                       spritesheet_index[i],
 	                // location of last sprite in spritesheet
 	                animation_length[i] - 1, spritesheet_index[i],
 	                false, // horizontalScan value
 	                200, // speed value for animation
 	                true // autoupdate value
 	                );
 	        zombie_animations[i].setLooping(true); // intialize whether animation loops
 	        zombie_animations[i].setPingPong(true); // initialize whether animation ping-pongs between last and first frame
         }
         
         // Define which animations are for which states
         FRAME_STATE_UP = 3; FRAME_STATE_DOWN = 0;
         FRAME_STATE_LEFT = 1; FRAME_STATE_RIGHT = 2;
         FRAME_STATE_UP_WALK = 3; FRAME_STATE_DOWN_WALK = 0;
         FRAME_STATE_LEFT_WALK = 1; FRAME_STATE_RIGHT_WALK = 2;
 		
         // Initialize zombie based on constructor
 		super.Init(
 				tile_x, tile_y, // Tile to put in
 				16, 16,
 				true, true, 1, // Visible? Solid? Damage?
 				sprite_shift_x, sprite_shift_y, // shift for each animation
 				16, // Size of a tile
 				zombie_animations, 0, // Initialize animations and current animation
 				1, // Health value
 				2.0f, // Speed value in tiles per second
 				0, 0); // Initial movement values
 		
 		Hurt_Sound = new Sound("soundtrack/effects/sword_hit_flesh.wav");
 		
 		started_moving = System.currentTimeMillis();
 		
 		Artificial_Intelligence(true, null);
 	}
 	
 	// Override GCharacter's Artificial Intelligence function
 	// Knows if this character has collided and knows player's position
 	public void Artificial_Intelligence(boolean collision, Player_Character player)
 	{
		if (moving)
 		{
			if (this.Alligned_With_Tiles())
			{
				X_Movement = 0;
				Y_Movement = 0;
				moving = false;
			}
 		}
 		else
 		{
 			if (!moving && System.currentTimeMillis() > started_moving + rnd.nextInt(2000))
 			{
 				if (rnd.nextBoolean())
 				{
 					X_Movement = (rnd.nextBoolean() ? 1:-1);
 					Y_Movement = 0;
 				}
 				else
 				{
 					X_Movement = 0;
 					Y_Movement = (rnd.nextBoolean() ? 1:-1);
 				}
 				moving = true;
 				started_moving = System.currentTimeMillis();
 			}
 		}
 	}
 	
	public boolean X_Collision(GObject col){
		
		boolean iscol = super.X_Collision(col);
		
		if (iscol && col.getClass() == this.getClass())
			X_Movement *= -1;
		
		return iscol;
	}
	
	public boolean Y_Collision(GObject col){
		
		boolean iscol = super.Y_Collision(col);
		
		if (iscol && col.getClass() == this.getClass())
			Y_Movement *= -1;
		
		return iscol;
	}
	
 	public int Get_Type(){
 		
 		return Type;
 	}
 }
