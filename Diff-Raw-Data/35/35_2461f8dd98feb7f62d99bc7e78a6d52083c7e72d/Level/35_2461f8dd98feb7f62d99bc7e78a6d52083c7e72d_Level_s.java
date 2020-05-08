 package edu.chl.codenameg.model.levels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Position;
 
 public interface Level {
 	//Just a suggestion , different list for singleplayer / multiplayer to enhance the difficulty relative to players.
 	
	public List<Entity> getListOfEnteties();
 	public Position getStartPosition();
 	public int getAmountOfPlayers();
 	
 		
 }
