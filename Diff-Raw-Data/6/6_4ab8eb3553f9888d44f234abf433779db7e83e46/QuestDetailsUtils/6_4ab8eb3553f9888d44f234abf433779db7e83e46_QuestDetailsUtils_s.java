 /*
  * This file is part of MineQuest-API, version 2, Specifications for the MineQuest system.
  * MineQuest-API, version 2 is licensed under GNU Lesser General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.API.Quest;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.API.Utils.ChatUtils;
 import com.theminequest.MineQuest.API.Utils.FastByteArrayOutputStream;
 
 public class QuestDetailsUtils {
 	
 	/**
 	 * Get a deep copy of this QuestDetails object. This allows
 	 * for the object to be modified without any adverse side-effects
 	 * and does not affect the original <b>master</b> object.<br>
 	 * <h5>WARNING:</h5> This method uses serialization and a
 	 * byte-based stream to accomplish deep copying. As such, it is an
 	 * intensive operation and should not be used lightly by
 	 * methods. In particular, this method is used by classes implementing
 	 * {@link com.theminequest.MineQuest.API.Quest.QuestManager}
 	 * to retrieve a mutable object for every quest started.
 	 * <br>
 	 * <b>Note that this implementation is not synchronized.</b>
 	 * As such, you must be aware of threads that may modify this object
 	 * while copying it.
 	 * @param d Details to copy
 	 * @return completely independent copy of the object
 	 */
 	public static QuestDetails getCopy(QuestDetails d) {
 		FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
 		ObjectOutputStream os = null;
 		try {
 			os = new ObjectOutputStream(bos);
 			os.writeObject(d);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (os != null) {
 				try {
 					os.close();
 				} catch (IOException e) {
 					// memory leak!
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		
 		ObjectInputStream is = null;
 		QuestDetails q = null;
 		try {
 			is = new ObjectInputStream(bos.getInputStream());
 			q = (QuestDetails) is.readObject();
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 					// memory leak!
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		return q;
 	}
 	
 	/**
 	 * Retrieve the overview of the quest. (Human-Readable)
 	 * @param d Details to parse
 	 * @return Quest Overview
 	 */
 	public static String getOverviewString(QuestDetails d) {
 		String tr = "";
 		tr+=ChatUtils.formatHeader((String) d.getProperty(QuestDetails.QUEST_DISPLAYNAME))+"\n";
		tr+=ChatUtils.chatify((String) d.getProperty(QuestDetails.QUEST_DESCRIPTION))+"\n";
 		return tr;
 	}
 	
 	/**
 	 * Check to see if a player meets the requirements for this
 	 * quest to be made available to the player.
 	 * @param d Quest to retrieve requirements from
 	 * @param p Player to check (Leader usually)
 	 * @return true if requirements are met
 	 */
 	public static boolean requirementsMet(QuestDetails d, Player p){
 		List<QuestRequirement> requirements = d.getProperty(QuestDetails.QUEST_REQUIREMENTS);
 		for (QuestRequirement q : requirements){
 			if (!q.isSatisfied(p))
 				return false;
 		}
 		return true;
 	}
 	
 }
