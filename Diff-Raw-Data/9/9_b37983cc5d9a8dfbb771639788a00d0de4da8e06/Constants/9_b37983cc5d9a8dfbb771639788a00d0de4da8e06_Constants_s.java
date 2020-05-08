 package pt.up.fe.pt.lpoo.bombermen;
 
 
 public final class Constants
 {
     // Window
     public static final String WINDOW_TITLE = "Bombermen";
     public static final int DEFAULT_WIDTH = 800;
     public static final int DEFAULT_HEIGHT = 480;
     public static final boolean USE_GL20 = true;
 
     // Control Pad
     public static final int DEFAULT_PAD_WIDTH = 200;
     public static final int DEFAULT_PAD_HEIGHT = 200;
     public static final float DEFAULT_PAD_BUTTON_SIZE_MULT = 2.f / 3.f;
     public static final boolean SHOW_PAD = false;
 
     // Game
     public static final int INIT_NUM_MAX_BOMBS = 1;
     public static final int INIT_BOMB_RADIUS = 2;
     public static final int BOMB_TIMER = 3000; // 3 seconds
     public static final int EXPLOSION_TIMER = 600; // milliseconds
     public static final int DEFAULT_BOMB_STRENGTH = 1; // 1 hp damage
     public static final int CELL_SIZE = 36; // px
     public static final float POWER_UP_SPAWN_CHANCE = 0.8f;
 
     // Maps
     public static final String DEFAULT_MAP_FILE_CHARSET = "US-ASCII";
     public static final float WALL_CHANCE = 0.5f;
     public static final float INIT_PLAYER_SPEED = 108f / 1000f;
 
     // Sizes
     public static final float PLAYER_WIDTH = 18.f * CELL_SIZE * 0.75f / 26f;
     public static final float PLAYER_HEIGHT = CELL_SIZE * 0.75f;
 
    public static final float PLAYER_BOUNDING_WIDTH = 14.f * CELL_SIZE * 0.65f / 24.f;
    public static final float PLAYER_BOUNDING_HEIGHT = CELL_SIZE * 0.65f;
 
     public static final float BOMB_WIDTH = 0.9f * CELL_SIZE;
     public static final float BOMB_HEIGHT = BOMB_WIDTH;
 
     public static final float EXPLOSION_WIDTH = CELL_SIZE;
     public static final float EXPLOSION_HEIGHT = EXPLOSION_WIDTH;
 
     public static final float POWER_UP_WIDTH = 0.9f * CELL_SIZE;
     public static final float POWER_UP_HEIGHT = POWER_UP_WIDTH;
 
     public static final float WALL_WIDTH = CELL_SIZE;
     public static final float WALL_HEIGHT = WALL_WIDTH;
 
    public static final float WALL_BOUNDING_WIDTH = WALL_WIDTH * 0.95f;
    public static final float WALL_BOUNDING_HEIGHT = WALL_HEIGHT * 0.95f;
 
     // PowerUp types
     public static final int POWER_UP_TYPE_BOMB_UP = 0; // increase amount of bombs by 1
     public static final int POWER_UP_TYPE_SKATE = 1; // increase speed by 1
     public static final int POWER_UP_TYPE_KICK = 2; // send bomb sliding across the stage until it collides with a wall/player/bomb
     public static final int POWER_UP_TYPE_PUNCH = 3; // knock them away (out of screen or screen wrap to the other side)
     public static final int POWER_UP_TYPE_FIRE = 4; // increase bomb radius
     public static final int POWER_UP_TYPE_SKULL = 5; // debuffs (bomberman.wikia.com/wiki/Skull)
     public static final int POWER_UP_TYPE_FULL_FIRE = 6; // biggest possible explosion radius
     public static final int NUMBER_OF_POWER_UP_TYPES = 7;
 
     private Constants()
     {
     }
 }
