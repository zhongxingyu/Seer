 /*
  * Copyright (C) 2013 RoketGamer <http://roketgamer.com> and contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.roketgamer.achievement;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.roketgamer.RoketGamer;
 
 public class Achievement {
 	
 	private int id;
 	private String name;
 	private boolean isSecret;
 	
 	/**
 	 * Create a new <code>Achievement</code>.
 	 * @param id
 	 * @param name
 	 * @param isSecret
 	 */
 	public Achievement(int id, String name, boolean isSecret) {
 		this.id = id;
 		this.name = name;
 		this.isSecret = isSecret;
 	}
 	
 	/**
 	 * Get <code>Achievement</code> ID.
 	 * @return id
 	 */
 	public int getID() {
 		return id;
 	}
 	
 	/**
 	 * Get <code>Achievement</code> name.
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Returns if <code>Achievement</code> is secret.
 	 * @return
 	 */
 	public boolean isSecret() {
 		return isSecret;
 	}
 	
 	/**
 	 * Returns if current logged in user has achieved achievement.
 	 * @return
 	 */
 	public boolean hasAchieved() {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/achievement/check.php?session=" + RoketGamer.getInstance().getSession().getSessionKey().trim() + "id=" + id);
 
 			URLConnection connection = url.openConnection();
 		    connection.addRequestProperty("Protocol", "Http/1.1");
 		    connection.addRequestProperty("Connection", "keep-alive");
 		    connection.addRequestProperty("Keep-Alive", "1000");
 		    connection.addRequestProperty("User-Agent", "Web-Agent");
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 			
 			String result = in.readLine();
 			if (result.contains("true")) {
 				in.close();
 				return true;
 			} else {
 				in.close();
 				return false;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Submit an achievement. Returns if operation is successful.
 	 * The server will not record duplicates but it is good practice to
 	 * not submit achievements that have already been awarded.
 	 * @return
 	 */
 	public boolean submit() {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/achievement/submit.php?session=" + RoketGamer.getInstance().getSession().getSessionKey().trim() + "id=" + id);
 
 			URLConnection connection = url.openConnection();
 		    connection.addRequestProperty("Protocol", "Http/1.1");
 		    connection.addRequestProperty("Connection", "keep-alive");
 		    connection.addRequestProperty("Keep-Alive", "1000");
 		    connection.addRequestProperty("User-Agent", "Web-Agent");
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String result = in.readLine();
 			if (result.contains("true")) {
 				in.close();
 				return true;
 				
 			} else {
 				in.close();
 				return false;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
