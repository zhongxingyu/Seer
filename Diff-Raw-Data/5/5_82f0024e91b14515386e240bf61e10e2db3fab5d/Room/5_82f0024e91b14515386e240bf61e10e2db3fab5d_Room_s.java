 /**
  * Copyright (c) 2011-2013 Robert Maupin
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *    1. The origin of this software must not be misrepresented; you must not
  *    claim that you wrote the original software. If you use this software
  *    in a product, an acknowledgment in the product documentation would be
  *    appreciated but is not required.
  * 
  *    2. Altered source versions must be plainly marked as such, and must not be
  *    misrepresented as being the original software.
  * 
  *    3. This notice may not be removed or altered from any source
  *    distribution.
  */
 package org.csdgn.fxm.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.UUID;
 
 import org.csdgn.fxm.net.Session;
 
 public class Room extends Thing {
 	public String name;
 	public String description;
 	public ArrayList<Exit> exits;
 	public transient HashSet<Character> characters;
 	
 	public Room() {
 		uuid = null;
 		exits = new ArrayList<Exit>();
 		characters = new HashSet<Character>();
 	}
 	
 	public void displayRoomTo(Session session) {
 		session.writeLn("",name + "-",description);
 		
 		int exitCount = exits.size(); 
 		if(exitCount > 0) {
 			session.write("You see an exit ");
 			for(int i=0;i<exitCount;++i) {
 				if (i > 0) {
 					if (i == exitCount - 1) {
 						session.write("and ");
 					} else {
 						session.write(", ");
 					}
 				}
 				session.write(exits.get(i).name);
 			}
 			session.write(".");
 			session.writeLn();
 		}
 		
 		//players
 		if(characters.size() > 1) {
 			for(Character p : characters) {
 				if(p == session.character)
 					continue;
				session.write(String.format("%s %s is standing here.", p.givenName, p.familyName));
 			}
 		}
 	}
 	
 	public Exit getExit(UUID uuid) {
 		for(Exit ex : exits)
			if(ex.uuid == uuid)
 				return ex;
 		return null;
 	}
 }
