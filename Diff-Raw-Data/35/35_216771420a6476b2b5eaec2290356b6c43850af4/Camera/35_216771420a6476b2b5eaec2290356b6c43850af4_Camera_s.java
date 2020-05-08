 package edu.chl.codenameg.model;
 import java.util.ArrayList;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 /** 
  * Class that represents a camera
  * with width, height and position 
  * depending on the player characters.
  */
 public class Camera {
 	
	private int minWidth = 500;
	private int minHeight = 500;
	private int width;
	private int height;
	private float camerax = 0;
	private float Xmax,Ymax,Xmin,Ymin;
	private float cameray;
	private World world;
	private ArrayList<PlayerCharacter> players;
 
 	public Camera(World world) {
 		this.world = world;
 		this.players = new ArrayList<PlayerCharacter>();
 	}
	
 	public float getX(){
		
 		return camerax;
 	}
 	public float getY(){
		
 		return cameray;
 	}
	
 	public int getWidth() {
 		return width;
 	}
 	public int getHeight() {
 		return height;
 	}
	
 	public void update() {
 		this.update(10);
 	}
 	public void update(int elapsedTime) {
 		
 		for(PlayerCharacter c : world.getPlayers()){
 			players.add(c);
 		}
		
 		Xmax = players.get(0).getPosition().getX();
 		Ymax = players.get(0).getPosition().getY();
 		Xmin = players.get(0).getPosition().getX();
 		Ymin = players.get(0).getPosition().getY();
 		
 		for(int i = 1; i < world.getAmountOfPlayers();i++){
 		Xmax = Math.max(Xmax, players.get(i).getPosition().getX());
 		Xmin = Math.min(Xmin, players.get(i).getPosition().getX());
 		Ymax = Math.max(Ymax, players.get(i).getPosition().getY());
 		Ymin = Math.min(Ymin, players.get(i).getPosition().getY());
 		}
 		
		width = (int)(Xmax - Xmin + minWidth);
		height = (int)(Ymax - Ymin + minHeight);
 		
 		camerax =((Xmax+Xmin)/2)-this.getWidth()/2;
 		cameray =((Ymax+Ymin)/2+ players.get(0).getHitbox().getHeight())-this.getHeight()/2;
 		
 		for(PlayerCharacter c : world.getPlayers()){
 			players.remove(c);
 		}
 }
 }
