 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class Map {
 	private Tile[][] map;
 	private File inputfile;
 	private Scanner scan;
 	private int xDimension;
 	private int yDimension;
 	private Character character;
 	private SpriteSheet worldSprites;
 	
 	private ArrayList<MoveTile> movingTiles = new ArrayList<MoveTile>();
 	
 	public static final int TILE_DEPTH = 5;	
 	public static final int BLOCK_SIZE = 32;
 	
 	public Map(String filename) {
 		worldSprites = new SpriteSheet("images/platformTiles.gif");
 		inputfile =  new File(filename);
 		try {
 			 scan = new Scanner(inputfile);
 			  xDimension = scan.nextInt();
 			  yDimension = scan.nextInt();
 			 map = new Tile[xDimension][yDimension];
 			readmap();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	public Tile[][] getMap(){return map;}
 
 	private void readmap() {
 		while(scan.hasNext()){
 			
 			for(int j = 0; j< yDimension; j++){
 				for(int i = 0; i< xDimension; i++){
 					int item = scan.nextInt();
 					if(item==17){
 						character = new Character(i*BLOCK_SIZE,j*BLOCK_SIZE);
 						map[i][j] = new BackgroundTile(BLOCK_SIZE,BLOCK_SIZE,i*BLOCK_SIZE,j*BLOCK_SIZE,TILE_DEPTH,new ImageWrapper(0, BLOCK_SIZE, BLOCK_SIZE,worldSprites));
 					}else if(item<0){
 						map[i][j] = new DestTile(BLOCK_SIZE, BLOCK_SIZE, i*BLOCK_SIZE, j*BLOCK_SIZE, TILE_DEPTH, 
 								new ImageWrapper(item*-1, BLOCK_SIZE, BLOCK_SIZE, worldSprites), 
 								new ImageWrapper(0, BLOCK_SIZE, BLOCK_SIZE, worldSprites));
 					}else if(item>17){
 						map[i][j] = new BackgroundTile(BLOCK_SIZE, BLOCK_SIZE, i*BLOCK_SIZE, j*BLOCK_SIZE,
 								TILE_DEPTH, new ImageWrapper(item, BLOCK_SIZE, BLOCK_SIZE, worldSprites));
 						movingTiles.add(new MoveTile(BLOCK_SIZE, BLOCK_SIZE, i*BLOCK_SIZE, j*BLOCK_SIZE,
								TILE_DEPTH, new ImageWrapper(item-17, BLOCK_SIZE, BLOCK_SIZE, worldSprites),
								true, 10, 200, 8));																//minus 17 as dont want acces to moving background tiles
 					}else{
 						ImageWrapper imgwrap = new ImageWrapper(item, BLOCK_SIZE, BLOCK_SIZE, worldSprites);
 						if(item==0)
 							map[i][j] = new BackgroundTile(BLOCK_SIZE, BLOCK_SIZE, i*BLOCK_SIZE, j*BLOCK_SIZE, TILE_DEPTH, imgwrap);
 						else
 							map[i][j] = new FloorTile(BLOCK_SIZE,BLOCK_SIZE,i*BLOCK_SIZE,j*BLOCK_SIZE,TILE_DEPTH,imgwrap);
 					}
 				}
 			}
 			
 		
 		}
 	}
 
 	public Character getCharacter() {
 		return character;
 	}
 	
 	public ArrayList<MoveTile> movingTiles(){
 		return this.movingTiles;
 	}
 }
