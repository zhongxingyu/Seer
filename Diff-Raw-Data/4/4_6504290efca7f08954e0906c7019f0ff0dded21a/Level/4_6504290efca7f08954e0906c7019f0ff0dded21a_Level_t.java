 package mta13438;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Level {
 
 	private List<Room> roomList = new ArrayList<Room>(); //The list of the rooms inside the level.
 	private float dx;
 	private float dz;
 	private float dy;
 	private Point spawnPoint = new Point();
 	private boolean goThroughDoor = false;
 	private boolean trapDeath = false;
 	private boolean monsterDeath = false;
 
 	//No args constructor
 	public Level() {
 		this.roomList = null;
 		this.dx = 0;
 		this.dz = 0;
 		this.dy = 0;
 		updateLevelSize();
 	}
 
 	//Constructor
 	public Level(List<Room> roomList, float dx, float dz, float dy) {
 		this.roomList.addAll(roomList);
 		this.dx = dx;
 		this.dz = dz;
 		this.dy = dy;
 		updateLevelSize();
 	}
 
 	//Getters and Setters.
 	public List<Room> getRoomList() {
 		return Collections.unmodifiableList(roomList);
 	}
 	public void setRoomList(List<Room> roomList) {
 		this.roomList.clear();
 		this.roomList.addAll(roomList);
 	}
 	public void addRoomList(Room room){
 		this.roomList.add(room);
 	}
 	public float getDx() {
 		return dx;
 	}
 	public void setDx(float dx) {
 		this.dx = dx;
 	}
 	public float getDz() {
 		return dz;
 	}
 	public void setDz(float dz) {
 		this.dz = dz;
 	}
 	public float getDy() {
 		return dy;
 	}
 	public void setDy(float dy) {
 		this.dy = dy;
 	}
 	public int getCurrentRoom(Point playerPos) {
 		Integer currentRoom = 0;
 		
 		//It looks trough every room and compares their location in the level to the players.
 		for (int i = 0; i < getRoomList().size(); i++) {		
 			if(playerPos.getX() >= getRoomList().get(i).getPos().getX() && playerPos.getX() <= (getRoomList().get(i).getPos().getX() + getRoomList().get(i).getDx()) && playerPos.getY() >= getRoomList().get(i).getPos().getY() && playerPos.getY() <= (getRoomList().get(i).getPos().getY() + getRoomList().get(i).getDy())){
 				currentRoom = i;
 			}			
 		}
 		
 		//The room number is the returned out of the function.
 		return currentRoom;
 	}
 	
 	//Automate the position of the rooms.
 	public void autoLevelGenerator(Point startPoint){
 		for (int i = 0; i < getRoomList().size(); i++){
 			if (i == 0){
 				this.roomList.get(i).setPos(new Point(startPoint.getX(),startPoint.getY(),startPoint.getZ()));
 				this.roomList.get(i).setEntrance(new Point (this.roomList.get(i).getEntrance().getX()+startPoint.getX(),this.roomList.get(i).getEntrance().getY()+startPoint.getY(),this.roomList.get(i).getEntrance().getZ()+startPoint.getZ()));
 				this.roomList.get(i).setExit(new Point (this.roomList.get(i).getExit().getX()+startPoint.getX(),this.roomList.get(i).getExit().getY()+startPoint.getY(),this.roomList.get(i).getExit().getZ()+startPoint.getZ()));
 				for (int j = 0; j < this.roomList.get(i).getObsList().size(); j++){
 					this.roomList.get(i).getObsList().get(j).setPos(new Point (this.roomList.get(i).getObsList().get(j).getPos().getX()+startPoint.getX(),this.roomList.get(i).getObsList().get(j).getPos().getY()+startPoint.getY(),this.roomList.get(i).getObsList().get(j).getPos().getZ()+startPoint.getZ()));
 				}
 			} else {
 				float entranceY = this.roomList.get(i).entrance.getY();
 				float entranceX = this.roomList.get(i).entrance.getX();
 				float entranceZ = this.roomList.get(i).entrance.getZ();
 				float previousExitY = this.roomList.get(i-1).exit.getY();
 				float previousExitX = this.roomList.get(i-1).exit.getX();
 				float previousExitZ = this.roomList.get(i-1).exit.getZ();
 				this.roomList.get(i).setPos(new Point(previousExitX-entranceX,previousExitY-entranceY,previousExitZ-entranceZ));
 				this.roomList.get(i).setEntrance(new Point (previousExitX,previousExitY,previousExitZ));
 				this.roomList.get(i).setExit(new Point (this.roomList.get(i).getExit().getX()+this.roomList.get(i).getPos().getX(),this.roomList.get(i).getExit().getY()+this.roomList.get(i).getPos().getY(),this.roomList.get(i).getExit().getZ()+this.roomList.get(i).getPos().getZ()));
 				for (int j = 0; j < this.roomList.get(i).getObsList().size(); j++){
 					this.roomList.get(i).getObsList().get(j).setPos(new Point (this.roomList.get(i).getObsList().get(j).getPos().getX()+this.roomList.get(i).getPos().getX(),this.roomList.get(i).getObsList().get(j).getPos().getY()+this.roomList.get(i).getPos().getY(),this.roomList.get(i).getObsList().get(j).getPos().getZ()+this.roomList.get(i).getPos().getZ()));
 				}
 			}
 			for (int j = 0; j < this.roomList.get(i).getObsList().size(); j++){
 				this.roomList.get(i).getObsList().get(j).getLoopSound().update(this.roomList.get(i).getObsList().get(j).getCenter());
 			}
 		}
 	}
 	
 	public void Draw(){
 	   	 for(Room room : this.roomList){
 	   		 room.draw();
 	   	 }
 	   	for (int i = 0; i < getRoomList().size(); i++) {
 			for (int j = 0; j < getRoomList().get(i).getObsList().size(); j++) {
 				getRoomList().get(i).getObsList().get(j).draw();
 			}
 		}
 	}
 	// updates spawn point
 	public void updateSpawnPoint(Player player, Level level) {
 		Point playerPos = player.getPos();
 		// sets the spawn point to the entrance of the current room
		this.spawnPoint = level.getRoomList().get(getCurrentRoom(playerPos)).entrance;
 		this.goThroughDoor = true;
 	}
 	
 	public Point getSpawnPoint() {
 		return this.spawnPoint;
 	}
 	
 	public boolean isGoThroughDoor() {
 		return goThroughDoor;
 	}
 
 	public void setGoThroughDoor(boolean goThroughDoor) {
 		this.goThroughDoor = goThroughDoor;
 	}
 
 	public boolean isTrapDeath() {
 		return trapDeath;
 	}
 
 	public void setTrapDeath(boolean trapDeath) {
 		this.trapDeath = trapDeath;
 	}
 
 	public boolean isMonsterDeath() {
 		return monsterDeath;
 	}
 
 	public void setMonsterDeath(boolean monsterDeath) {
 		this.monsterDeath = monsterDeath;
 	}
 
 	public String toString() {
 		return "Level [roomList=" + roomList + ", dx=" + dx + ", dz="
 				+ dz + ", dy=" + dy + "]";
 	}
 
 	public void updateLevelSize() {
 		if(roomList.isEmpty() == false){
 			//dxp stands for Delta X Plus and dxm stands for Delta X Minus, these are added in the end to make a total.
 			float dxp = 0;
 			float dzp = 0;
 			float dyp = 0;
 			float dxm = 0;
 			float dzm = 0;
 			float dym = 0;
 
 			/*
 			 * This is the logic behind the calculation.
 			 * It takes the rooms and looks at their positions 
 			 * in comparison to the temporary values and sets them to
 			 * the max of the level size. The minus temp values are here 
 			 * if the map makes a U turn and comes back and extends beyond
 			 * the first rooms starting point. 
 			 */
 			for (int i = 0; i < roomList.size(); i++) {
 				if(roomList.get(i).getPos().getX() > 0 && roomList.get(i).getPos().getY() > 0 && roomList.get(i).getPos().getZ() > 0){
 					if(dxp < roomList.get(i).getPos().getX() + roomList.get(i).getDx()){
 						dxp = roomList.get(i).getPos().getX() + roomList.get(i).getDx();
 					}
 					if(dyp < roomList.get(i).getPos().getY() + roomList.get(i).getDy()){
 						dyp = roomList.get(i).getPos().getY() + roomList.get(i).getDy();
 					}
 					if(dzp < roomList.get(i).getPos().getZ() + roomList.get(i).getDz()){
 						dzp = roomList.get(i).getPos().getZ() + roomList.get(i).getDz();
 					}
 				}else{
 					if(dxm > roomList.get(i).getPos().getX()){
 						dxm = roomList.get(i).getPos().getX();
 					}
 					if(dym > roomList.get(i).getPos().getY()){
 						dym = roomList.get(i).getPos().getY();
 					}
 					if(dzm > roomList.get(i).getPos().getZ()){
 						dzm = roomList.get(i).getPos().getZ();
 					}
 				}
 			}		
 
 			//adding all the temp values absolute values to get the level size.
 			setDx(Math.abs(dxp + dxm));
 			setDz(Math.abs(dzp + dzm));
 			setDy(Math.abs(dyp + dym));
 		}
 	}
 }
