 package map;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 
 import mapobjects.Bomb;
 import mapobjects.MapObject;
 import mapobjects.Player;
 
 import imageloader.ImageLoader;
 
 public class Map {
 	public ImageLoader graphics = new ImageLoader();
 	public Vector<Vector<MapObject>> mapObjects = new Vector<Vector<MapObject>>();
 
 	private String mapName;
     private String mapSizeX;
     private String mapSizeY;
     private int drawLevels;
     public Player mapplayer;
    
     BufferedImage collisionMap;
     MapReader mr;
 	
     public Map(String mn){
     	mr = new MapReader(mn);
     	
     	//read Mapheader
     	mapName = mr.getHeader("mapname");
     	mapSizeX = mr.getHeader("sizex");
     	mapSizeY = mr.getHeader("sizey");
     	drawLevels = mr.getDrawLevels();
     	
     	mr.loadGraphics(graphics);
     	mr.getMap(mapObjects,graphics);
 
     	for(int i=0; i<drawLevels; i++){
     		for(int j=0; j<mapObjects.get(i).size(); j++){
     			if(mapObjects.get(i).get(j) instanceof Player){
     				mapplayer = (Player) mapObjects.get(i).get(j);	
     			}
     		}
     	}
     	//TODO make enemies
     }
     
     //TODO public void loadNewMap(){}
 	
     public void drawMap(Graphics2D g2d){
    	collisionMap = new BufferedImage(Integer.parseInt(mapSizeX),Integer.parseInt(mapSizeY),BufferedImage.TYPE_INT_ARGB);
		Graphics2D b = collisionMap.createGraphics();
     	for(int i=0; i<drawLevels; i++){
     		for(int j=0; j<mapObjects.get(i).size(); j++){
     			mapObjects.get(i).get(j).draw(g2d,graphics,b);
     		}
     	}
     	b.dispose();
     }
 
 	public void update() {
 		for(int i=0; i<drawLevels; i++){
     		for(int j=0; j<mapObjects.get(i).size(); j++){
     			mapObjects.get(i).get(j).update();
     		}
     	}
 		mapplayer.move(collisionMap);
 	}
 	
 	public void layBomb(){
 		if(!mapplayer.reachedMaxBombs()){
 			mapObjects.get(1).add(new Bomb(
 					mapplayer.getPosX(),
 					mapplayer.getPosY(),
 					true,
 					false,
 					false,
 					"simplebomb",
 					graphics));
 			mapplayer.addBomb();
 		}
 	}
 }
