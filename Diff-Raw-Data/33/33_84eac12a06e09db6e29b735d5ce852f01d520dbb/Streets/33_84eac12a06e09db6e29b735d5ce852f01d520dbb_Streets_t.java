 package objects;
 
 import game.Grid;
 import game.Main;
 import game.ResourceManager;
 
 /**
  * This class will provide static methods to build streets and also
  * it will manage traffic etc.
  * @author Benedikt Ringlein
  */
 
 public class Streets {
 
 	private static int startposx;
 	private static int startposy;
 	private static int endposx;
 	private static int endposy;
 	private static int x1;
 	private static int x2;
 	private static int y1;
 	private static int y2;
 	public static int getX1() {
 		return x1;
 	}
 
 	public static int getX2() {
 		return x2;
 	}
 
 	public static int getY1() {
 		return y1;
 	}
 
 	public static int getY2() {
 		return y2;
 	}
 
 	private static int length;
 	private static boolean vertical;
 	
 	public static int getLength() {
 		return length;
 	}
 
 	public static boolean isVertical() {
 		return vertical;
 	}
 
 	/**
 	 * Marks the starting point of a new road
 	 * @param posx
 	 * @param posy
 	 */
 	public static void startBuilding(int posx, int posy)
 	{
 		startposx = posx;
 		startposy = posy;
 	}
 	
 	public static void updateBuilding(int posx, int posy)
 	{
 		if(Math.abs(startposx-posx)<Math.abs(startposy-posy)){
 			// vertical
 			vertical = true;
 			x1 = startposx;
 			x2 = startposx;
 			y1=startposy;
 			y2 = posy;
 			if(startposy>y2){
 				int tmp = startposy;
 				y1 = y2;
 				y2 = tmp;
 			}
 			y2+=1;
 			length = Math.abs(endposy-startposy)+1;
 		}else{
 			// horizontal
 			vertical = false;
 			x1 = startposx;
 			x2 = posx;
 			y1 = startposy;
 			y2 = startposy;
 			if(startposx>x2){
 				int tmp = x1;
 				x1 = x2;
 				x2 = tmp;
 			}
 			x2+=1;
 			length = Math.abs(endposx-startposx)+1;
 		}
 	}
 	
 	/**
 	 * Marks the end point of a new road and builds it
 	 * @param posx
 	 * @param posy
 	 */
 	public static void endBuilding(int posx, int posy)
 	{
 		if(Math.abs(startposx-posx)<Math.abs(startposy-posy)){
 			// vertical
 			endposx = startposx;
 			endposy = posy;
 			if(!Grid.isStripFree(startposx, startposy, endposy-startposy, true, ResourceManager.BUILDINGTYPE_STREET))return;
			int cost=0;
 			if(startposy>endposy){
 				int tmp = startposy;
 				startposy = endposy;
 				endposy = tmp;
 			}
 			for(int i=startposy;i<=endposy;i++){
 				if(Grid.isAreaFree(startposx, i, 1, 1)){
					cost+=ResourceManager.getBuildingType(ResourceManager.BUILDINGTYPE_STREET).getBuidlingcost();
				}
			}
			if(Main.money<cost)return; else Main.money-=cost;
			for(int i=startposy;i<=endposy;i++){
				if(Grid.isAreaFree(startposx, i, 1, 1)){
 					ResourceManager.buildBuilding(startposx, 0, i, ResourceManager.BUILDINGTYPE_STREET);
 				}
 			}
 		}else{
 			// horizontal
 			endposx = posx;
 			endposy = startposy;
 			if(!Grid.isStripFree(startposx, startposy, endposx-startposx, false, ResourceManager.BUILDINGTYPE_STREET))return;
			int cost=0;
 			if(startposx>endposx){
 				int tmp = startposx;
 				startposx = endposx;
 				endposx = tmp;
 			}
 			for(int i=startposx;i<=endposx;i++){
 				if(Grid.isAreaFree(i, startposy, 1, 1)){
					cost+=ResourceManager.getBuildingType(ResourceManager.BUILDINGTYPE_STREET).getBuidlingcost();
				}
			}
			if(Main.money<cost)return; else Main.money-=cost;
			for(int i=startposx;i<=endposx;i++){
				if(Grid.isAreaFree(i, startposy, 1, 1)){
 					ResourceManager.buildBuilding(i, 0, startposy, ResourceManager.BUILDINGTYPE_STREET);
 				}
 			}
 		}
 	}
 
 	public static int getStartposx() {
 		return startposx;
 	}
 
 	public static void setStartposx(int startposx) {
 		Streets.startposx = startposx;
 	}
 
 	public static int getStartposy() {
 		return startposy;
 	}
 
 	public static void setStartposy(int startposy) {
 		Streets.startposy = startposy;
 	}
 
 	public static int getEndposx() {
 		return endposx;
 	}
 
 	public static void setEndposx(int endposx) {
 		Streets.endposx = endposx;
 	}
 
 	public static int getEndposy() {
 		return endposy;
 	}
 
 	public static void setEndposy(int endposy) {
 		Streets.endposy = endposy;
 	}
 }
 
