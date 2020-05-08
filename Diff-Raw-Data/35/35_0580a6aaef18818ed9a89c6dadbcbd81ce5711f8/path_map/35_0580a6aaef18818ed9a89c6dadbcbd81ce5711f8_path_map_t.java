 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor3f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import pathFinding.*;
 
 public class path_map implements TileBasedMap {
 	private HashMap<String, Texture> textureTable = new HashMap<String, Texture>();
 	private static final int GROUND = 0;
 	private static final int BLOCKED = 1;
 	private static final int NULL = 2;
 	//private int[][] mapData;
 	private int[][] mapData;
 	private static int WIDTH;
 	private static int HEIGHT;
 	public static int TILE_SIZE = 64;
 	//for path	#new add
 	private boolean[][] visited;
 	private int[][] map4path;
 	
 	// flip bottom left to top right
 	private int[][] convertArrayAndRandomTilePic(int[][] mapData){
 		int WIDTH = mapData.length;//17
 		int HEIGHT = mapData[0].length;//7
 		int[][] tmp = new int[HEIGHT][WIDTH];
		int randGround = new Random().nextInt(2);
 		for (int x=0;x<WIDTH;x++) {
 			for (int y=0;y<HEIGHT;y++) {
 				/** GROUND  1-14
				 *  WGROUND 201-214
 				 *  BLOCKED 101-109
 				 *  NULL    200
 				 * */
				if(mapData[x][y] == GROUND) {
					if(randGround == 0)
						tmp[y][x] = new Random().nextInt(14) +1;	// random 1-14
					else
						tmp[y][x] = 200+(new Random().nextInt(14) +1);	// random 1-14
				}
 				if(mapData[x][y] == BLOCKED)
 					tmp[y][x] = 100+(new Random().nextInt(9) +1);	// random 1-9
 				if(mapData[x][y] == NULL)
 					tmp[y][x] = mapData[x][y]*100;
 			}
 		}
 		return tmp;
 	}
 	
 	private Texture loadTexture(String name) throws FileNotFoundException, IOException{
 		return TextureLoader.getTexture("JPG", new FileInputStream(new File("res/"+name)));
 	}
 	
 	public path_map() {
 		createMap(1);
 		//change this #new add
 		WIDTH = mapData.length;//length = 7
 		HEIGHT = mapData[0].length;//length = 17
 		visited =new boolean[HEIGHT][WIDTH];
 		//end change #new add
 
 		createMap(1);
 		textureTable.put(String.valueOf(100*(int)NULL),Game.loadTexture("map/null.jpg"));
 		for(int i = 1;i <= 14;i++)
 			textureTable.put(String.valueOf(i),Game.loadTexture("map/ground"+i+".jpg"));
		for(int i = 1;i <= 14;i++)
			textureTable.put(String.valueOf(i+200),Game.loadTexture("map/Wground"+i+".jpg"));
 		for(int i = 1;i <= 9;i++)
 			textureTable.put(String.valueOf(i+100),Game.loadTexture("map/block"+i+".jpg"));
 	}
 	
 	public void draw() {
 		Texture tex;
 		WIDTH = mapData.length;
 		HEIGHT = mapData[0].length;
 		
 		for (int x=0;x<WIDTH;x++) {
 			for (int y=0;y<HEIGHT;y++) {
 				tex = textureTable.get(String.valueOf(mapData[x][y]));
 				tex.bind();
 				glPushMatrix();
 				glTranslatef(x*TILE_SIZE, y*TILE_SIZE, 0);
 				glBegin(GL_QUADS);				
 					glTexCoord2f(0, 0);
 					glVertex2f(0, 0);
 					glTexCoord2f(1, 0);
 					glVertex2f(TILE_SIZE, 0);
 					glTexCoord2f(1, 1);
 					glVertex2f(TILE_SIZE,TILE_SIZE);
 					glTexCoord2f(0, 1);
 					glVertex2f(0, TILE_SIZE);
 				glEnd();
             	glPopMatrix();
             	//draw path
 			}
 		}
 	}
 	
 	public void createMap(int LV){
 		switch(LV){
 		case 1:
 			mapData = new int[][]{
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
 			};
 			break;
 		case 2:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 3:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 4:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 5:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		//############################################################
 		case 6:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 7:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 8:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 9:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 10:
 			mapData = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		}
 		mapData = convertArrayAndRandomTilePic(mapData);
 		WIDTH = mapData.length;//length = 7
 		HEIGHT = mapData[0].length;//length = 17
 		visited =new boolean[HEIGHT][WIDTH];
 	}
 	
 	/** True if the location is blocked */
 	public boolean blocked(int x, int y) {
 		if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
 			return true;
 		else
 			return mapData[(int) x][(int) y] > 100 && mapData[(int) x][(int) y] < 200;
 	}
 	
 	/*
 	 * ###########################################################################################
 	 * #####################################  NEW ADD ############################################
 	 * ###########################################################################################
 	 */
 	
 	/**
 	 * Clear the array marking which tiles have been visted by the path 
 	 * finder.
 	 */
 	public void clearVisited() {
 		for (int x=0 ;x<HEIGHT ;x++) {
 			for (int y=0;y<WIDTH;y++) {
 				visited[x][y] = false;
 			}
 		}
 	}
 
 	@Override
 	public int getWidthInTiles() {
 		// TODO Auto-generated method stub
 		return WIDTH;
 	}
 
 	@Override
 	public int getHeightInTiles() {
 		// TODO Auto-generated method stub
 		return HEIGHT;
 	}
 
 	@Override
 	public void pathFinderVisited(int x, int y) {
 		// TODO Auto-generated method stub
 		visited[y][x] = true;
 	}
 
 	@Override
 	public boolean blocked(Mover mover, int x, int y) {
 		// TODO Auto-generated method stub
 		//return map4path[(int) x][(int) y] == 1;
 		//return map4path[(int) x][(int) y] > 100 && map4path[(int) x][(int) y] < 200;
 		if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
 			return true;
 		else
 			return map4path[(int) x][(int) y] > 100 && map4path[(int) x][(int) y] < 200;
 	
 	}
 
 	@Override
 	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 	
 	public void setArrayMap(int LV ,ArrayList<Entity> entities){
 		//clear all visited history ^__^
 		clearVisited(); 
 		//create new map from current state
 		switch(LV){
 		case 1:
 			map4path = new int[][]{
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
 			};
 			break;
 		case 2:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 3:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 4:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 5:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		//############################################################
 		case 6:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 7:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 8:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 9:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		case 10:
 			map4path = new int[][]{
 					{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
 			        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
 			};
 			break;
 		}
 		
 		for(Entity entity : entities){
 			//check all tank
 			/*
 			if(entity instanceof MyTank){
 				tmp_map4path[(int) entity.get_centerY()/TILE_SIZE][(int) entity.get_centerX()/TILE_SIZE] = 2;
 			}else if( entity instanceof Entity && !(entity instanceof MyTank) ){
 				tmp_map4path[(int) entity.get_centerY()/TILE_SIZE][(int) entity.get_centerX()/TILE_SIZE] = 1;
 			}
 			*/
 			if(!(entity instanceof MyTank) && !(entity instanceof Bullet)  && !(entity instanceof Gold
 					&& !(entity instanceof Effect && !(entity instanceof HPpotion)))){
 				//System.out.println(entity);
 				map4path[(int) entity.get_centerY()/TILE_SIZE][(int) entity.get_centerX()/TILE_SIZE] = 1;
 			}
 			
 		}
 		
 		map4path = convertArrayAndRandomTilePic(map4path);
 
 	}
 	
 	public int[][] get_map4path(){
 		return map4path;
 	}
 	
 	public int get_intOfMap(int x,int y){
 		return map4path[x][y];
 	}
 	/*
 	private int[][] convertArray(int[][] mapData){
 		int WIDTH = mapData.length;//17
 		int HEIGHT = mapData[0].length;//7
 		int[][] tmp = new int[HEIGHT][WIDTH];
 		for (int x=0;x<WIDTH;x++) {
 			for (int y=0;y<HEIGHT;y++) {
 
 				if(mapData[x][y] == GROUND)
 					tmp[y][x] = 0;	// random 1-14
 				if(mapData[x][y] == BLOCKED)
 					tmp[y][x] = 1;	// random 1-9
 				if(mapData[x][y] == NULL)
 					tmp[y][x] = 10;
 			}
 		}
 		return tmp;
 	}
 	*/
 	
 		
 
 }
