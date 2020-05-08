 package com.fastquake.textbasedgame;
 
 import java.io.*;
 
 import com.fastquake.textbasedgame.room.Room;
 import com.fastquake.textbasedgame.room.RoomManager;
 
 public class TextBasedGame {
 	public static RoomManager rm;
 	static Room currentRoom;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		boolean doShutdown = false;
 		int currentRoomId = 1;
 		rm = new RoomManager();
 		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 		
 		System.out.println("Welcome to TextBasedGame.");
 		System.out.println("Type \"play\" to start a new game, or \"load\" to load a previous game.");
 		System.out.println();
 
 		while(!doShutdown){
 			currentRoom = rm.getRoomById(currentRoomId);
 			String command = "";
 			
 			currentRoom.describe();
 			System.out.println();
 			System.out.print("> ");
 			command = input.readLine();
 			handleCommand(command);
 		}
 	}
 	
 	private static void handleCommand(String inputCommand){
 		inputCommand = inputCommand.toLowerCase(); //Change it to lowercase so that there is no confusion due to case
 		String[] splitCommand = inputCommand.split(" ");
 		String command = splitCommand[0];
 		String object = "";
 		
 		if(command.equals("north") || command.equals("n")){
 			move(Direction.NORTH);
 		}else if(command.equals("east") || command.equals("e")){
 			move(Direction.EAST);
 		}else if(command.equals("south") || command.equals("s")){
 			move(Direction.SOUTH);
 		}else if(command.equals("west") || command.equals("w")){
 			move(Direction.WEST);
 		}else if(command.equals("look") || command.equals("examine") || command.equals("l")){
 			if(splitCommand.length>1){
 				//TODO: Handle object arguments, also "look at"
 			}else
 				currentRoom.describe();
 		}else if(command.equals("open")){
			if(splitCommand[1].equals("the")){
				for(int i=2;i<splitCommand.length;i++){
					object += splitCommand[i];
					if(!(i+1>=splitCommand.length))
						object += " ";
				}
			}
 		}
 	}
 	
 	private static void move(Direction direction){
 		if(currentRoom.getDoorTarget(direction, rm) != null)
 			currentRoom = currentRoom.getDoorTarget(direction, rm);
 		else
 			System.out.println("You cannot go that way.");
 	}
 }
