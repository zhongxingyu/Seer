 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zed;
 
 // Java for file reading
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 // Java for exception handling
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 // Slick for drawing to screen and input
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 /**
  *
  * @author Richard Barella Jr.
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 
 // TODO: Edit Level Manager
 
 public class Level_Manager {   
     
     private static final int MAX_WIDTH = 100; // TODO: might not need this
     private static final int MAX_HEIGHT = 100;
     private static final int TILE_SIZE = 16;
     
     private SpriteSheet tileset; // data for tiles
     private SpriteSheet character_sprites; // data for character sprites
     ArrayList<Object> objectlist; //this is going to be the array that the Level_Manager instance uses to hold all the objects
     
     
     int width; // Number of tile columns
     int height; // Number of tile rows
     int xpos; // x position of top-left of tiles
     int ypos; // y position of top-left of tiles
     int scale; // By how many times is the pixels larger?
     int[][] bot_tile_x; // tileset x index of bot_tile[x][y]
     int[][] bot_tile_y; // tileset y index of bot_tile[x][y]
     //int[][] top_tile_x; // tileset x index of top_tile[x][y]
     //int[][] top_tile_y; // tileset y index of top_tile[x][y]
     
     Player_Character player; // data for player character
     
     // Default instantiation for Level_Manager
     public Level_Manager() throws SlickException{
         tileset = new SpriteSheet("images/tileset.png", 16, 16);
         character_sprites = new SpriteSheet("images/spritesheet.png", 16, 32);
 
         
         // initialize player
         int[] player_spritesheet_index = {3, 1, 0, 2,
                                           3, 1, 0, 2};
         int[] player_animation_length  = {1, 1, 1, 1,
                                           4, 4, 4, 4};
         player = new Player_Character(0, 0, true,
            16, 16, // how far sprite is shifted and size in pixels
             character_sprites, player_spritesheet_index, player_animation_length, 0,
             5, 5.0f,
             0, 0);
         
         width = 16;
         height = 16;
         xpos = 0;
         ypos = 0;
         scale = 2;
         bot_tile_x = new int[width][height];
         bot_tile_y = new int[width][height];
         for (int i = 0; i < 16; i++ ){
         	for(int j = 0; j < 16; j++){
         		bot_tile_x[i][j] = 5;
         		bot_tile_y[i][j] = 0;
         	}
         }
         //top_tile_x = null;
         //top_tile_y = null;
     }
     
     // Instantiate Level_Manager with file
     public Level_Manager(String filepath){ // TODO: design lvl file and then make this to parse that file
         
         BufferedReader br = null;
         player = new Player_Character(/*todo:initialization parameters*/);
         width = 0;
         height = 0;
         xpos = 0;
         ypos = 0;
         scale = 0;
         objectlist = new ArrayList();
         
         try
         {
             String cur_line;
             char[] file_chars = new char[256];
             String[] file_files = new String[256];
             int file_num = 0;
             int cur_y = 0;
             
             br = new BufferedReader(new FileReader(filepath));
             
             while ((cur_line = br.readLine()) != null)
             {
                 // TODO: parse text file to load a new level
             }
         }
         catch (IOException e){
             e.printStackTrace();
         } finally {
             try {
                 if (br != null) br.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
     }
     
     // Edit class variables
     public void setwidth(int newwidth){
         
         width = newwidth;
     }
     public void setheight(int newheight){
         
         height = newheight;
     }
     public void setxpos(int newxpos){
         
         xpos = newxpos;
     }
     public void setypos(int newypos){
         
         ypos = newypos;
     }
     public void setscale(int newscale){
         
         scale = newscale;
     }
     public void settile(Image newtile, int x, int y){
         
         tileset.equals(newtile);
     }
     
     public void display(GameContainer gc, Graphics g){
         
         for (int i = 0; i < width; i++)
         {
             for (int j = 0; j < height; j++)
             {
                 g.drawImage(tileset.getSubImage(bot_tile_x[i][j], bot_tile_y[i][j]),
                         xpos + i*TILE_SIZE*scale,
                         ypos + j*TILE_SIZE*scale,
                         xpos + i*TILE_SIZE*scale + TILE_SIZE*scale,
                         ypos + j*TILE_SIZE*scale + TILE_SIZE*scale,
                         0, 0,
                         TILE_SIZE, TILE_SIZE);
             }
         }
         
         player.Render(scale, xpos, ypos, gc, g);
     }
     
     public void update()
     {
         player.Update(null);
     }
     
     public void move_player(int new_x_mov, int new_y_mov)
     {
         player.New_Movement(new_x_mov, new_y_mov);
     }
 }
