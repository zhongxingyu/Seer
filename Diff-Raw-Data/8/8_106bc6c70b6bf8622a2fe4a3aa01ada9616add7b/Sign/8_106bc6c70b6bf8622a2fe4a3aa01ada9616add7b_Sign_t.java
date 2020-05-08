 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.signs;
 
import org.bukkit.ChatColor;

 /**
  * An AntiShare blocked/whitelisted sign
  * 
  * @author turt2live
  */
 public class Sign {
 
 	/**
 	 * An enum to represent a line in a sign
 	 * 
 	 * @author turt2live
 	 */
 	public static enum Line{
 		LINE1(0),
 		LINE2(1),
 		LINE3(2),
 		LINE4(3);
 
 		private int index = 0;
 
 		private Line(int index){
 			this.index = index;
 		}
 
 		/**
 		 * Array index for the line
 		 * 
 		 * @return the index of the line in a standard array
 		 */
 		public int getIndex(){
 			return index;
 		}
 	}
 
 	private String[] lines;
 	private boolean enabled = false;
 	private boolean caseSensitive = false;
 	private String name;
 
 	/**
 	 * Creates a new sign
 	 * 
 	 * @param name the name
 	 * @param lines the lines
 	 * @param enabled true to enable
 	 * @param caseSensitive true if case sensitive
 	 */
 	public Sign(String name, String[] lines, boolean enabled, boolean caseSensitive){
 		this.name = name;
 		this.lines = lines;
 		this.enabled = enabled;
 		this.caseSensitive = caseSensitive;
 	}
 
 	/**
 	 * Gets the name of this sign
 	 * 
 	 * @return the name
 	 */
 	public String getName(){
 		return name;
 	}
 
 	/**
 	 * Gets the lines in this sign
 	 * 
 	 * @return the lines
 	 */
 	public String[] getLines(){
 		return lines;
 	}
 
 	/**
 	 * Gets the enabled state of this sign
 	 * 
 	 * @return true if enabled
 	 */
 	public boolean isEnabled(){
 		return enabled;
 	}
 
 	/**
 	 * Determines if this sign is case sensitive or not
 	 * 
 	 * @return true if case sensitive
 	 */
 	public boolean isCaseSensitive(){
 		return caseSensitive;
 	}
 
 	// Commented to avoid configuration breaking
 	//public void setName(String name){
 	//	this.name = name;
 	//}
 
 	/**
 	 * Sets the lines of this sign
 	 * 
 	 * @param lines the lines
 	 */
 	public void setLines(String[] lines){
 		this.lines = lines;
 	}
 
 	/**
 	 * Sets the enabled state of this sign
 	 * 
 	 * @param enabled true to enable
 	 */
 	public void setEnabled(boolean enabled){
 		this.enabled = enabled;
 	}
 
 	/**
 	 * Sets the case sensitive value of this sign
 	 * 
 	 * @param caseSensitive true if case sensitive
 	 */
 	public void setCaseSensitive(boolean caseSensitive){
 		this.caseSensitive = caseSensitive;
 	}
 
 	/**
 	 * Gets a specific line of this sign
 	 * 
 	 * @param line the line number
 	 * @return the line
 	 */
 	public String getLine(Line line){
 		return lines[line.getIndex()];
 	}
 
 	/**
 	 * Determines if a CraftBukkit sign matches this sign's requirements
 	 * 
 	 * @param cbSign the sign
 	 * @return true if matches
 	 */
 	public boolean matches(org.bukkit.block.Sign cbSign){
 		if(!enabled){ // False if not enabled
 			return false;
 		}
 
 		// Setup match
 		boolean[] match = new boolean[4];
 		for(int i = 0; i < match.length; i++){
 			match[i] = false;
 		}
 
 		// Check the CraftBukkit Sign's lines
 		String[] cblines = cbSign.getLines();
 		for(int i = 0; i < cblines.length; i++){
 			if(cblines[i] == null){
 				cblines[i] = "";
 			}
			cblines[i] = ChatColor.stripColor(cblines[i].trim());
 		}
 
 		// Check the match
 		for(int i = 0; i < lines.length; i++){
			String line = ChatColor.stripColor(lines[i].trim());
 			if(line.equalsIgnoreCase("*")){
 				match[i] = true;
 			}else if(line.equalsIgnoreCase("*blank")){
 				match[i] = cblines[i].length() < 1;
 			}else{
 				if(caseSensitive){
 					match[i] = cblines[i].equals(line);
 				}else{
 					match[i] = cblines[i].equalsIgnoreCase(line);
 				}
 			}
 		}
 
 		// Return the match
 		return match[0] && match[1] && match[2] && match[3];
 	}
 }
