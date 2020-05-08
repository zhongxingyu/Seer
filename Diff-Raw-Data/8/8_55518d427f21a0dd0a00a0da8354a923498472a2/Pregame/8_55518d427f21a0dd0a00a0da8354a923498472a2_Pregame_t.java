 package com.googlecode.prmf;
 
 import java.util.*;
 import java.io.*; 
 import com.googlecode.prmf.starter.*;
 
 public class Pregame {
 	
 	InputStream is;
 	String startName;
 	
 	public Pregame(InputStream is, String startName)
 	{
 		this.is = is;
 		this.startName = startName;
 	}
 
 		public void startGame()
 		{
 			Scanner in = new Scanner(is);
 			ArrayList<Player> players = new ArrayList<Player>();
 			ArrayList<Role> townRoles = new ArrayList<Role>();
 			ArrayList<Role> mafiaRoles = new ArrayList<Role>();
 			ArrayList<Role> roles = new ArrayList<Role>();
 			
 			
 			//joining the game	
 			
 			while(true) //break when the game starts
 			{
 				String line = in.nextLine();
 				String[] msg = line.split(" ", 4);
 				String user = "#UFPT";
 				
 				if(msg[0].indexOf("!")>1)
 					user = msg[0].substring(1,msg[0].indexOf("!"));
 				
 				String destination = msg[2];
 				if(msg[3].equals(":!start") && user.equals(startName))		
 					break;
 					
				if(msg[3].equals(":!join"))
 				{
					players.add(new Player(user));
 					Communicator.getInstance().sendMessage(destination, user + " has joined the game!");
 				}
 				
				if(msg[3].equals(":!quit"))
 				{
 					for(int a = 0; a < players.size(); ++a)
 					{
 						Player temp = players.get(a);
 						if(temp.name.equals(user))
 							players.remove(a);
 					}
 				}	
 			}
 			
 			//assigning roles
 			int numMafia = players.size()/3;
 			//create the Mafia team
 			for(int a = 0; a < numMafia; ++a)
 			{
 				mafiaRoles.add(new Mafia(new MafiaTeam()));
 			}
 			
 			//create the Town team
 			for(int a = 0; a < (players.size() - numMafia); ++a)
 			{
 				townRoles.add(new Citizen(new Town()));
 			}
 			Collections.shuffle(mafiaRoles);
 			Collections.shuffle(townRoles);
 			for(int a = 0; a < numMafia; ++a)
 			{
 				roles.add(mafiaRoles.get(a));
 			}
 			for(int a = 0; a < players.size() - numMafia; ++a)
 			{
 				roles.add(townRoles.get(a));
 			}
 			for(int a = 0; a < players.size(); ++a)
 			{
 				players.get(a).role = roles.get(a);
 			}
 			
 		}
 	}
 
