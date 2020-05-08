 /*
  * Copyright 2013 Dan Mercer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.themercerbros.liarsdice.desktop.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.themercerbros.liarsdice.desktop.Call;
 import com.themercerbros.liarsdice.desktop.Guess;
 import com.themercerbros.liarsdice.desktop.io.IO;
 import com.themercerbros.liarsdice.desktop.player.RemotePlayer;
 
 public class Client {
 	private final IO io = new IO();
 	private final Pattern msgPattern = Pattern.compile("\"(.*)\"");
 	
 	private final Socket conn;
 	private final BufferedReader in;
 	private final PrintWriter out;
 	private ClientPlayer player;
 	
 	public Client(String address, int port, HashMap<String, String> vars) {
 		try {
 			io.say("Connecting to " + address + " on port " + port + "...");
 			conn = new Socket(address, port);
 			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			out = new PrintWriter(conn.getOutputStream());
 			io.say("Connected.");
 		} catch (UnknownHostException e) {
 			io.say("Error. Unknown host " + address);
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			io.say("Connection error.");
 			throw new RuntimeException(e);
 		}
 		
 		String hostPlayerName;
 		int numOfDice;
 		try {
 			hostPlayerName = in.readLine();
 			numOfDice = Integer.parseInt(in.readLine());
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
		String name = vars.get("--name");
		if (name == null) {
			player = new ClientPlayer(numOfDice);
		} else {
			player = new ClientPlayer(numOfDice, name);;
		}
 		out.println(player.name);
 		out.flush();
 		
 		io.say("You have joined " + hostPlayerName + "'s game. Each player starts with " + numOfDice + " dice.");
 		io.say("Waiting for the game to start...");
 	}
 	
 	public void run()  {
 		while (!conn.isClosed()) {
 			String input;
 			try {
 				input = in.readLine();
 			} catch (IOException e) {
 				io.say("Connection down. Exiting.");
 				break;
 			}
 
 			if (input.matches(RemotePlayer.MESSAGE_REGEX)) {
 				Matcher m = msgPattern.matcher(input);
 				m.find();
 				io.say(m.group(1));
 			} else if (input.matches(RemotePlayer.ROLLED + ".*")) {
 				Pattern p = Pattern.compile("\\d+");
 				Matcher m = p.matcher(input);
 				int count = 0;
 				while (m.find()) count++;
 				int[] dice = new int[count];
 				m.reset();
 				for (int i = 0; i < count; i++) {
 					m.find();
 					dice[i] = Integer.parseInt(m.group());
 				}
 				player.setDiceRolls(dice);
 				
 			} else if (input.matches(RemotePlayer.TAKE_TURN_REGEX)) {
 				Guess last = Guess.fromString(input);
 				Pattern p = Pattern.compile(RemotePlayer.TAKE_TURN_REGEX);
 				Matcher m = p.matcher(input);
 				m.find();
 				int diceInPlay = Integer.parseInt(m.group(3));
 				try {
 					Guess next = player.takeTurn(last, diceInPlay);
 					out.println(RemotePlayer.GUESS + next.toString());
 					out.flush();
 				} catch (Call e) {
 					out.println(RemotePlayer.CALL);
 					out.flush();
 				}
 			} else if (input.matches(RemotePlayer.TAKE_TURN_FIRST_REGEX)) {
 				Pattern p = Pattern.compile(RemotePlayer.TAKE_TURN_FIRST_REGEX);
 				Matcher m = p.matcher(input);
 				m.find();
 				int diceInPlay = Integer.parseInt(m.group(1));
 				try {
 					Guess next = player.takeTurn(null, diceInPlay);
 					out.println(RemotePlayer.GUESS + next.toString());
 					out.flush();
 				} catch (Call e) {
 					out.println(RemotePlayer.CALL);
 					out.flush();
 				}
 			}
 		}
 		try {
 			in.close();
 		} catch (IOException e) {
 			// Nothing we can do
 		}
 		out.close();
 		try {
 			conn.close();
 		} catch (IOException e) {
 			// Nothing we can do
 		}
 	}
 
 }
