 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.newdawn.slick.opengl.Texture;
 
 public class Map {
 	private HashMap<String, Texture> textureTable = new HashMap<String, Texture>();
 	private static final int GROUND = 0;
 	private static final int BLOCKED = 1;
 	private static final int NULL = 2;
 	//private int[][] mapData;
 	private int[][] mapData;
 	private static int WIDTH;
 	private static int HEIGHT;
 	public int TILE_SIZE = 64;
 
 	// flip bottom left to top right
 	private int[][] convertArrayAndRandomTilePic(int[][] mapData){
 		int WIDTH = mapData.length;
 		int HEIGHT = mapData[0].length;
 		int[][] tmp = new int[HEIGHT][WIDTH];
 		for (int x=0;x<WIDTH;x++) {
 			for (int y=0;y<HEIGHT;y++) {
 				/** GROUND  1-14
 				 *  BLOCKED 101-109
 				 *  NULL    200
 				 * */
				if(mapData[x][y] == GROUND)
					tmp[y][x] = new Random().nextInt(14) +1;	// random 1-14
 				if(mapData[x][y] == BLOCKED)
 					tmp[y][x] = 100+(new Random().nextInt(9) +1);	// random 1-9
 				if(mapData[x][y] == NULL)
 					tmp[y][x] = mapData[x][y]*100;
 			}
 		}
 		return tmp;
 	}
 	
 	public Map() {
 		createMap(1);
 		textureTable.put(String.valueOf(100*(int)NULL),Game.loadTexture("map/null.jpg"));
 		for(int i = 1;i <= 14;i++)
 			textureTable.put(String.valueOf(i),Game.loadTexture("map/ground"+i+".jpg"));
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
 	}
 	
 	/** True if the location is blocked */
 	public boolean blocked(int x, int y) {
 		if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
 			return true;
 		else
 			return mapData[(int) x][(int) y] > 100 && mapData[(int) x][(int) y] < 200;
 	}
 }
