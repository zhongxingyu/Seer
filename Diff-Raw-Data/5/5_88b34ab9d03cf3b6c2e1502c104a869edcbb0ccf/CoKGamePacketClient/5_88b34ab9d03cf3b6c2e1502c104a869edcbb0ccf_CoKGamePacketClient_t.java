 package de.minestar.cok.network;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.references.Color;
 
 public class CoKGamePacketClient {
 	
 	/**
 	 * sets the gamestate of clients on login.
 	 * @param inputStream
 	 */
 	public static void setGameState(DataInputStream inputStream){
 		try {
 			//read gameState
 			CoKGame.gameRunning = inputStream.readBoolean();
 			//read teams
 			int numberOfTeams = inputStream.readInt();
 			int numberOfPlayers = 0;
 			for(int i = 0; i < numberOfTeams; i++){
 				String teamName = inputStream.readUTF();
 				CoKGame.addTeam(teamName, inputStream.readChar());
 				//read users
 				numberOfPlayers = inputStream.readInt();
 				for(int j = 0; j < numberOfPlayers; i++){
 					CoKGame.addPlayerToTeam(teamName, inputStream.readUTF());
 				}
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void updateGameRunning(DataInputStream inputStream){
 		try {
 			CoKGame.gameRunning = inputStream.readBoolean();
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	public static void addTeam(DataInputStream inputStream){
 		try {
 			String teamName = inputStream.readUTF();
 			String color = inputStream.readUTF();
 			CoKGame.addTeam(teamName, Color.getColorFromString(color));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removeTeam(DataInputStream inputStream){
 		try {
 			String teamName = inputStream.readUTF();
 			CoKGame.removeTeam(teamName);
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void addPlayer(DataInputStream inputStream){
 		try {
 			String teamName = inputStream.readUTF();
 			String playerName = inputStream.readUTF();
 			CoKGame.addPlayerToTeam(teamName, playerName);
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removePlayer(DataInputStream inputStream){
 		try {
 			String teamName = inputStream.readUTF();
 			String playerName = inputStream.readUTF();
 			CoKGame.removePlayerFromTeam(teamName, playerName);
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void addSpectator(DataInputStream inputStream){
 		try{
 			String playerName = inputStream.readUTF();
 			CoKGame.setPlayerSpectator(playerName);
 			EntityPlayer thisPlayer = Minecraft.getMinecraft().thePlayer;
			if(thisPlayer != null && thisPlayer.username.equalsIgnoreCase(playerName)){
 				thisPlayer.capabilities.allowFlying = true;
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removeSpectator(DataInputStream inputStream){
 		try{
 			String playerName = inputStream.readUTF();
 			EntityPlayer thisPlayer = Minecraft.getMinecraft().thePlayer;
			if(thisPlayer != null && thisPlayer.username.equalsIgnoreCase(playerName)){
 				thisPlayer.capabilities.allowFlying = true;
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 }
