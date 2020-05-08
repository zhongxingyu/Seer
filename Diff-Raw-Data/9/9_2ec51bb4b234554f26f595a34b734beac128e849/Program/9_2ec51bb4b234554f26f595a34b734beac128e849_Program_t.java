 /**
 * Master Time Keeper – The perfect graphical terminal schedule viewer
  * 
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.mastertimekeeper;
 
 import java.io.*;
 import java.util.*;
 
 
 /**
  * This is the main class of the program
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Program
 {
     /**
      * Hidden constructor
      */
     private Program()
     {
 	//Nullify default constructor
     }
     
     
     
     /**
      * This is the main entry point of the program
      *
      * @param   args       Start up arguments (unused)
      * @throws  Throwable  On any error
      */
     public static void main(final String... args) throws Throwable
     {
 	colourmap.put("!", "31");
 	colourmap.put("*", "33;1");
 	colourmap.put("~", "35;1");
 	colourmap.put("^", "36");
 	colourmap.put("-", "35");
 	colourmap.put("+", "31");
 	colourmap.put("/", "32;1");
 	colourmap.put("?", "33");
 	colourmap.put("&", "34");
 	colourmap.put("#", "32");
 	colourmap.put(">", "34;1");
 	
 	try
 	{
 	    final ArrayList<String> lines = new ArrayList<String>();
 	    final ArrayList<String> raws = new ArrayList<String>();
 	    final Scanner fileScanner = new Scanner(new BufferedInputStream(new FileInputStream(new File(args[0]))));
 	    while (fileScanner.hasNext())
 	    {
 		final String line;
 		lines.add(manipulateLine(line = fileScanner.nextLine()));
 		raws.add(line);
 	    }
 	    
 	    Terminal.setEchoFlag(false);
 	    Terminal.setBufferFlag(false);
 	    Terminal.setSignalFlag(false);
 	    Terminal.setCursorVisibility(false);
 	    
 	    int width = Terminal.getTerminalWidth();
 	    int height = Terminal.getTerminalHeight();
 	    int bottom = 2;
 	    int top = 0;
 	    while ((top < raws.size()) && (raws.get(top).startsWith("--- ") == false))
 		top++;
 	    if (top >= raws.size())
 		top = 0;
 	    int cur = top;
 	    
 	    for (int i = 0; i < top; i++)
 		System.out.print(lines.get(i));
 	    for (int i = 0; i < height - bottom - top; i++)
 		System.out.print(lines.get(cur + i));
 	    String procent = String.valueOf((cur - top) * 100 / (lines.size() - top));
 	    if (procent.length() == 1)
 		procent = '0' + procent;
 	    if (lines.size() < height - bottom)  procent = "ALL";
 	    else if (procent.equals("00"))	 procent = "TOP";
 	    else if (procent.equals("100"))	 procent = "BOT";
 	    else
 		procent += '%';
 	    System.out.println("\033[44;33;1m\033[J  " + procent + "  \033[0m");
 	    
 	    for (int d = 0; d >= 0; d = System.in.read())
 	    {
 		if (d == 'q')
 		    break;
 		
 		int nav = 0;
 		
 		switch (d)
 		{
 		    case 12: //^L
 			width = Terminal.getTerminalWidth();
 			height = Terminal.getTerminalHeight();
 			for (int i = 0; i < top; i++)
 			    System.out.print(lines.get(i));
 			for (int i = 0; i < height - bottom - top; i++)
 			    System.out.print(lines.get(cur + i));
 			procent = String.valueOf((cur - top) * 100 / (lines.size() - top));
 			if (procent.length() == 1)
 			    procent = '0' + procent;
 			if (lines.size() < height - bottom)  procent = "ALL";
 			else if (procent.equals("00"))	     procent = "TOP";
 			else if (procent.equals("100"))	     procent = "BOT";
 			else
 			    procent += '%';
 			System.out.println("\033[44;33;1m\033[J  " + procent + "  \033[0m");
 			break;
 			
 		    case 53: //page up
 			nav -= height - bottom - top;
 			nav++;
 			//$FALL-THRU$
 		    case 65: //up
 			nav--;
 			cur += nav;
 			if (cur < top)
 			{
 			    cur = top;
 			    break;
 			}
 			break;
 			
 		    case 54: //page down
 		    case 32: //space
 			nav += height - bottom - top;
 			//$FALL-THRU$
 			nav--;
 		    case 66: //down
 		    case 10: //enter
 			nav++;
 			cur += nav;
 			break;
 		}
 	    }
 	}
 	catch (final Throwable err)
 	{
 	    System.err.println("\033[31mFatal exception:\033[m\n");
 	    throw err;
 	}
 	finally
         {
 	    Terminal.setEchoFlag(true);
 	    Terminal.setBufferFlag(true);
 	    Terminal.setSignalFlag(true);
 	    Terminal.setCursorVisibility(true);
 	}
     }
     
     
     
     /**
      * Used by {@link #manipulateLine(String)}
      */
     private static int legendState = 0;
     
     /**
      * Used by {@link #manipulateLine(String)}
      */
     private static final HashMap<String, String> colourmap = new HashMap<String, String>();
     
     
     
     /**
      * Adds colours to a line
      *
      * @param   line  The line
      * @return        The line colourised
      */
     public static String manipulateLine(final String line)
     {
 	final StringBuilder out = new StringBuilder();
 	
 	if (line.startsWith("--- "))
 	{
 	    if (line.startsWith("--- Legend ---"))
 		legendState = 1;
 	    else if (legendState == 1)
 		legendState = 2;
 	    out.append("\033[47;30m\033[J");
 	    out.append(line);
 	    out.append("\033[0m\n");
 	}
 	else if (legendState < 2)
 	{
 	    final String colour;
 	    if ((legendState == 1) && (line.length() > 0) && ((colour = colourmap.get(line.substring(0, 1))) != null))
 	    {
 		out.append("\033[" + colour + "m");
 		out.append(line.substring(0, 1));
 		out.append("\033[0m");
 		out.append(line.substring(1));
 	    }
 	    else
 		out.append(line);
 	    out.append('\n');
 	}
 	else if (line.startsWith(">>"))
 	{
 	    out.append("\033[31m");
 	    out.append(line);
 	    out.append("\033[0m\n");
 	}
 	else if (line.startsWith("::"))
 	{
 	    out.append("\033[34;1m");
 	    out.append(line.substring(0, 2));
 	    out.append("\033[0;34m");
 	    out.append(line.substring(2));
 	    out.append("\033[0m\n");
 	}
 	else if (line.startsWith("#"))
 	{
 	    out.append("\033[32m");
 	    out.append(line);
 	    out.append("\033[0m\n");
 	}
 	else if (line.length() > 53)
 	{
 	    String colour;
 	    if ((colour = colourmap.get(line.substring(16, 17))) != null)
 	    {
 		out.append(line.substring(0, 16));
 		out.append("\033[" + colour + "m");
 		out.append(line.substring(16, 17));
 		out.append("\033[0m");
 		if ((colour = colourmap.get(line.substring(29, 30))) != null)
 		{
 		    out.append(line.substring(17, 29));
 		    out.append("\033[" + colour + "m");
 		    out.append(line.substring(29, 30));
 		    out.append("\033[0m");
 		}
 		else
 		    out.append(line.substring(17, 30));
 	    }
 	    else if ((colour = colourmap.get(line.substring(29, 30))) != null)
 	    {
 		out.append(line.substring(0, 29));
 		out.append("\033[" + colour + "m");
 		out.append(line.substring(29, 30));
 		out.append("\033[0m");
 	    }
 	    else
 		out.append(line.substring(0, 30));
 	    
 	    if (line.substring(30, 41).toLowerCase().equals(line.substring(30, 41).toUpperCase()))
 		out.append("\033[32m");
 	    else if (line.substring(30, 41).equals(line.substring(30, 41).toUpperCase()))
 		out.append("\033[31m");
 	    else if (line.substring(30, 41).equals(line.substring(30, 41).toLowerCase()))
 		out.append("\033[33m");
 	    
 	    out.append(line.substring(30, 43));
 	    out.append("\033[0m");
 	    if (line.substring(43, 51).startsWith("<"))
 		out.append("\033[31m");
 	    out.append(line.substring(43, 53));
 	    out.append("\033[0m");
 	    if (line.charAt(53) == '?')
 	        out.append("\033[31m?\033[0m");
 	    else
 		out.append(line.charAt(53));
 	    out.append(line.substring(54));
 	    out.append('\n');
 	}
 	else
 	{
 	    out.append(line);
 	    out.append('\n');
 	}
 	return out.toString();
     }
     
 }
 
