 /*
     Copyright (C) 2011  Kristian 'Bobby' Lundkvist, Niclas 'Prosten' Bj�rner
 
 	This file is a part of GLaDOS
 
     This GLaDOS is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package se.bthstudent.sis.afk.GLaDOS;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 
 /**
  * Main class for GLaDOS
  * @author Bobby, Prosten
  * @version 0.0.25
  *
  */
 public class Initrd {
 
 	/**
 	 * Starts GLaDOS, connects to server and join channels.
 	 * @param args Runtime arguments
 	 */
 	public static void main(String[] args)
 	{
 		GLaDOS bot = new GLaDOS();
 		
 		bot.setVerbose(true);
 		
 		try {
 			bot.connect("irc.bsnet.se");
 		} catch (NickAlreadyInUseException e) {
 			System.err.println("Error: Nick was already in use");
 			e.printStackTrace();
 		}
 		catch (IrcException e) {
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
<<<<<<< HEAD
		
		bot.joinChannel("#GLaDOStest");
=======
 
 		bot.joinChannel("#GLaDOS-test");
>>>>>>> 56b8a1c751f9f9b59a2d8dac8d9c4ec91d770a7b
 	}
 }
