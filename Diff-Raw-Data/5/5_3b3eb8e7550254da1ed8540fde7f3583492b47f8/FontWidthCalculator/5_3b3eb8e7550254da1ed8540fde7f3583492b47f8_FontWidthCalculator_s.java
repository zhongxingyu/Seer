 // NOTICE!!!: I originally got this class from https://github.com/tkelly910/Help/blob/master/src/main/java/org/angelsl/minecraft/randomshit/fontwidth/MinecraftFontWidthCalculator.java
 // 
 // I modified this class and, as per the license below, changed the name. The license
// has been left in tact from the original file. THIS IS LICENSE SEPARATELY FROM
 // THE REST OF THIS PROJECT.
 // 
// Thanks to Sam Hocevar, who originally wrote this class!!!
 
 package com.judoguys.bukkit.utils;
 
 /**
  * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE Version 2, December 2004
  * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
  * 
  * Everyone is permitted to copy and distribute verbatim or modified copies of
  * this license document, and changing it is allowed as long as the name is
  * changed.
  * 
  * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE TERMS AND CONDITIONS FOR COPYING,
  * DISTRIBUTION AND MODIFICATION
  * 
  * 0. You just DO WHAT THE FUCK YOU WANT TO.
  */
 public class FontWidthCalculator
 {
 	/**
 	 * The original value was 325, but I've found this value to work better.
 	 */
 	public static int MAX_LINE_WIDTH = 350;
 	
 	private static String charWidthIndexIndex = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";
 	
 	private static int[] charWidths = {4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6,
 		6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6,
 		6, 6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7,
 		6, 6, 6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9,
 		9, 9, 5, 9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1};
 	
 	public static String truncate (String string)
 	{
 		int width = 0;
 		StringBuilder builder = new StringBuilder();
 		
 		if (string != null) {
 			for (int i = 0; i < string.length(); i++) {
 				char chr = string.charAt(i);
 				int charWidth = getCharWidth(chr);
 				if ((width + charWidth) > MAX_LINE_WIDTH) {
 					break;
 				}
 				builder.append(chr);
 				width += charWidth;
 			}
 		}
 		return builder.toString();
 	}
 	
 	public static int getStringWidth (String string)
 	{
 		int width = 0;
 		if (string != null) {
 			for (int i = 0; i < string.length(); i++) {
 				width += getCharWidth(string.charAt(i));
 			}
 		}
 		return width;
 	}
 	
 	public static int getCharWidth (char c)
 	{
 		if (c <= 0xF) {
 			// This is a ChatColor enum value and has no width.
 			return 0;
 		}
 		int k = charWidthIndexIndex.indexOf(c);
 		if (c != '\247' && k >= 0) {
 			return charWidths[k];
 		}
 		return 0;
 	}
 }
