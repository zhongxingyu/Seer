 package server;
 
 import java.awt.Color;
 import java.io.*;
 import java.net.Socket;
 import java.net.SocketException;
 
 /**
  * User thread which main passes off the initialization to (deals with passing the user to 
  * @author Boys
  *
  */
 public class Secretary extends Fileio{
 
 	public Secretary(Socket clientSocket){
 		// basically, initialize stuff and check to see if this is a old user
 		this.clientSocket = clientSocket;
 		isPlayer = false;
 		id = ServerMAIN.UsrNum++;
 		mif = new UInfo();
 		stats = new MyStats();
 		try{
 			out = new DataOutputStream(clientSocket.getOutputStream());
 			in = new DataInputStream(clientSocket.getInputStream());
 			Printu("User has connected sucessfully", Color.green);
 		} catch (IOException e){
 			Printu("User had an error connecting: " + e.getMessage(), Color.red);
 			try {
 				out.close();
 			} catch (IOException e1) {
 				Printu("^^User could not close output stream" + e1.getMessage(), Color.red);
 			}
 			try {
 				in.close();
 			} catch (IOException e1) {
 				Printu("^^User could not close input stream" + e1.getMessage(), Color.red);
 			}
 		}
 		System.out.println("GOT FINISHED INITIALIZING USER");
 	}
 
 	public void run() {
 		int num = 7;
 		boolean read = false;
 		boolean repeat = true;
 		String fromUser = null;
 		String[] values = null;
 		// states which we need to mess with: login, ship change, high-scores (later)
 		while (repeat){
 			try{
 				System.out.println("reading from user");
 				num = in.readInt();								// reads an integer from the input stream
 				System.out.println("HIHIHI");
 				repeat = false;
 			} catch (IOException ioe){
 				Printu("Error in input");
 			}
 		}
 		System.out.println("Read user number" + num);
 		//
 		// Case 0: user login, verify
 		// Case 1: new user, create file
 		// Case 2: ship change, change number
 		// Case 3: Want to play the game
 		switch (num){
 		case 0: 								/// Basic logic (for cases): read data sent from user, check/create/read userFile, compare/replace values
 			read = comparePreferences(0);
 			try{
 				if (read){
 					out.writeBoolean(true);
 					
 					out.writeUTF(this.mif.Encode());		// sending them their personal information (ship, etc)
 					Printu("successful" + mif.shipNum);
 				} else{
 					out.writeBoolean(false);
 					//out.writeUTF("Socccer");
 				}
 			} catch (IOException ioe){
 				Printu("Error in giving confirmation to user; " + ioe.getMessage());
 			}
 			Printu("Client Connection Successful: disconnecting");
 
 			//			try {
 			//				Thread.sleep(7000);
 			//			} catch (InterruptedException e1) {
 			//				// TODO Auto-generated catch block
 			//				e1.printStackTrace();
 			//			}
 			break;
 
 		case 1:
 
 			/*
 			 * Read sent user data
 			 */
 			try {
 				fromUser = in.readUTF();
 				read = true;
 			} catch (IOException ioe) {
 				Printu("Reading username and password failed");
 				read = false;
 			}
 			values = fromUser.split("\\|");
 
 			/*
 			 * Read current information (on file) and write to to new file (if we couldn't read or there was already a file, return false)
 			 */
 			int isNot = ServerMAIN.findFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext);
 			if (read && (isNot == 0)){
 				createFile (ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext, fromUser);
 				try{
 					out.writeBoolean(true);
 					out.writeUTF(this.readFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext));
 					Printu("New Client Connecting: " +
 							"" + values[0]);
 				} catch (IOException ioe){
 					Printu("Error in giving confirmation to user; " + ioe.getMessage(), Color.red);
 				}
 			} else {
 				try{
 					out.writeBoolean(false);
 					Printu("Client tried to connect with old user name: " + values[0], Color.red);
 				} catch (IOException ioe) {
 					Printu("Error in giving confirmation to user; " + ioe.getMessage(), Color.red);
 				}
 			}
 			Printu("Client Connection Successful: disconnecting", Color.green);
 			break;
 
 		case 2:	
 			/**
 			 * +string
 			 * -boolean
 			 * 
 			 * **********************************************************************************************************************
 			 * UNUSED??????
 			 * 
 			 * Probably dont really need: the user can update his/her new ship to the server when they connect to the server to play (case 3:)
 			 * **********************************************************************************************************************
 			 */
 			/*
 			 * Read sent user data (have client add initial values)
 			 */
 			read = comparePreferences(3);
 			try{
 				if (read){
 					out.writeBoolean(true);
 				} else{
 					out.writeBoolean(false);
 					break;
 				}
 			} catch (IOException ioe){
 				Printu("Error in giving confirmation to user; " + ioe.getMessage(), Color.red);
 				break;
 			}
 
 			//			try {
 			//				fromUser = in.readUTF();
 			//				read = true;
 			//			} catch (IOException ioe) {
 			//				Printu("Reading username and password failed");
 			//				read = false;
 			//			}
 			//			values = fromUser.split("\\|");
 			//			/*
 			//			 * Write the information to the users file
 			//			 */
 			//			String[] check = this.readFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext).split("|");
 			//			repeat = writeFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext, (check[0] + "|" + check[1] + "|" + values[2]));
 			//			if (read){
 			//				Printu("Wrote to Player file sucessfully");
 			//				try {
 			//					out.writeBoolean(true);
 			//				} catch (IOException e) {
 			//					Printu("Error in giving confirmation to user; " + e.getMessage());
 			//					e.printStackTrace();
 			//				}
 			//			} else {
 			//				Printu("Could not find user file");
 			//				try{
 			//					out.writeBoolean(false);
 			//				} catch (IOException ioe) {
 			//					Printu("Error in giving confirmation to user; " + ioe.getMessage());
 			//				}
 			//			}
 			//			Printu("Client Connection Successful: disconnecting");
 			break;
 
 		case 3: 
 			// logic to set up game: double-check preferences with current preferences, 
 			// set up/read user log file, send info back to user, start game
 			/*
 			 * check preferences for user
 			 */
 			System.out.println("Got through Step1");
 			read = comparePreferences(3);
 
 			try{
 				if (read){
 					out.writeBoolean(true);
 				} else{
 					out.writeBoolean(false);
 					break;
 				}
 			} catch (IOException ioe){
 				Printu("Error in giving confirmation to user; " + ioe.getMessage(), Color.red);
 				break;
 			}
 
 			/*
 			 * If the client has been verified, then set up a personal player stats file
 			 */
 			this.fileP = ServerMAIN.resources + ServerMAIN.slash + this.mif.myName;
 			int filecheck = ServerMAIN.findFile(this.fileP + ServerMAIN.slash + ServerMAIN.stats + ServerMAIN.ext);
 			if (read && (filecheck == 0)){
 				createFile (this.fileP + ServerMAIN.slash + ServerMAIN.stats + ServerMAIN.ext, "0|0|0");
 				this.stats.Decode("0|0|0");
 			} else if (read && (filecheck == 1)){
 				this.stats.Decode(readFile(this.fileP + ServerMAIN.slash + ServerMAIN.stats + ServerMAIN.ext));
 			} else {
 				Printu("We have a file Error in the Secretary", Color.red);
 				break;
 			}
 			/*
 			 * Send info back to user
 			 */
 			try{
 				out.writeUTF(this.stats.Encode());						// sending user stats back to user
 			} catch(IOException ioe){
 				Printu("Error in giving information to user; " + ioe.getMessage(), Color.red);
 				break;
 			}
 			/*
 			 * while loop for user (for checking servers - will exit when user selects a server or exits)
 			 */
 
 			fromUser = waitForSelection();
 			if (fromUser == null){
 				Printu("User exited serverConnections");
 				break;
 			}
 
 			Printu("Client connection successful: transferring him to game client", Color.green);
 			connectToGame(fromUser);
 
 			break;
 
 		default:
 			Printu("Error in input");
 			try {
 				out.writeBoolean(false);
 			} catch (IOException ioe) {
 				Printu("User has an error in input: " + ioe.getMessage(), Color.red);
 			}
 			break;
 		}
 		//System.out.println("gotHere");
 		close();
 	}
 
 	protected void close() {
 		//this.writeFile(ServerMAIN.resources + ServerMAIN.slash + mif.myName + ServerMAIN.slash + mif.myName, mif.Encode());
 		//System.out.println("HI");
 		ServerMAIN.addRemoveUser(this, false);
 		try {							// try to close the connection, piece by piece
 			if(out != null) 
 				out.close();
 		} catch (IOException e) {
 			Printu("Could not close output stream");
 		}
 
 		try {
 			if(in != null) 
 				in.close();
 		} catch (IOException e) {
 			Printu("Could not close input stream");
 		}
 
 		try {
 			if(clientSocket != null) 
 				clientSocket.close();
 		} catch (IOException e) {
 			Printu("Could not close socket");
 		}
 
 		Printu("Closing Successful: Client Disconnected", Color.GREEN);
 
 	}
 
 	/**
 	 * Compares user preferences (username, password) with that on file. 
 	 * @return
 	 */
 	private boolean comparePreferences(int ix){
 		boolean read = false;
 		boolean repeat = true;
 		String fromuser = null;
 		String[] values = null;
 		String fromFile = null;
 		/*
 		 * Read sent user data
 		 */
 		try {
 			fromuser = in.readUTF();
 			read = true;
 			values = fromuser.split("\\|");
 		} catch (IOException ioe) {
 			Printu("Reading username and password failed");
 			read = false;
 		}
 		//values = fromuser.split("\\|");
		Printu("HI" + fromuser);
 		/*
 		 * update current user info on file
 		 */
 		//System.out.println(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext);
 		fromFile = readFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext);
 		//System.out.println("REAL VALUES: " + fromFile);
 		if (fromFile == null){
 			repeat = false;
 		}else{
 			this.mif.Decode(fromFile);
 		}
 		/*
 		 * Check for similarities (if the same, then return true to user)
 		 */
 		//		Printu("VALUES: " + values[0]);
 		//		Printu("VALUES: " + values[1]);
 		//		Printu("VALUES: " + values[2]);
 		//		Printu("VALUES: " + mif.myName);
 		//		Printu("VALUES: " + mif.myPassword);
 		//		Printu("compareUNAME: " + values[0].equals(mif.myName));
 
 		if (read && repeat){
 			if (values[0].equals(mif.myName) && values[1].equals(mif.myPassword)){
 				read = true;
 				if (ix == 3){
 					//mif.shipNum = Integer.parseInt(values[2]); // fix these!!
 					//writeFile(ServerMAIN.resources + ServerMAIN.slash + values[0] + ServerMAIN.slash + values[0] + ServerMAIN.ext, mif.Encode());
 				}
 			} else {
 				read = false;
 			}
 
 		} 
 		if (!read || !repeat){
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean createFile(String filePath, String information){
 		try {
 			File myfile = new File(filePath);
 			if (!myfile.getParentFile().exists())
 				myfile.getParentFile().mkdir();
 			myfile.createNewFile();
 			// Write to the newly created file (initial values from user)
 			boolean check = writeFile(filePath, information);
 			if (!check){
 				Printu("ERROR CREATING USER FILE: " + id);
 			}
 
 		} catch (IOException e) {
 			Printu("ERROR CREATING USER FILE" + e.getMessage(), Color.red);
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * loops updating the user with current server information. When the user chooses a server and hits accept, the string 
 	 * (GameRoom.myName) is sent to the server to connect the user to the right server.
 	 * 
 	 * user sends: 
 	 * 0: no update called
 	 * 1: update user
 	 * 2: gets users selected servername and exits function
 	 * 
 	 * @return the string representing the server name or null
 	 */
 	private String waitForSelection(){
 		boolean read = true;
 		int repeat = 0;
 		String toReturn = null;
 		StringBuilder GRooms = new StringBuilder();
 		try{
 			while(read){
 				repeat = in.readInt();
 				if (repeat == 1){
 					for (GameRoom gm: ServerMAIN.getGameRoom()){
 						GRooms.append("|" + gm.getId() + "|" + gm.getName() + "|" + gm.connected.size());		// if user wants more specifics, then add them later
 					}
 					out.writeUTF(GRooms.toString().substring(1));
 
 				} else if (repeat == 2){
 					read = false;
 					toReturn = in.readUTF();
 				}
 			}
 		} catch (IOException ioe){
 			Printu("Error with user input! Exiting: " + ioe.getMessage());
 			ioe.printStackTrace();
 			return null;
 		}
 		return toReturn;
 	}
 
 	private void connectToGame(String gm){
 		boolean isThere = false;
 		GameRoom gRoom = null;
 		Player newPlayer;
 		for (GameRoom nextRoom : ServerMAIN.getGameRoom()){
 			if (nextRoom.myName.equals(gm)){
 				isThere = true;
 				gRoom = nextRoom;
 				break;
 			}
 		}
 		if (isThere){
 			Printu("Found game: connecting...");
 			newPlayer = new Player(this, gRoom);			// may need to change this (also look at player class) - passing reference rather than value
 			//ServerMAIN.addRemoveUser(newPlayer, true);
 			gRoom.addUser(newPlayer);
 			try{
 				newPlayer.asdf();
 			} catch(SocketException se){
 				Printu("Player Disconnected");
 			}
 		} else {
 			Printu("Game not found: creating...");
 			gRoom = new GameRoom(gm);
 			newPlayer = new Player(this, gRoom);		// may need to change this (also look at player class)
 			ServerMAIN.addRemoveGame(gRoom, true);
 			//ServerMAIN.addRemoveUser(newPlayer, true);
 			gRoom.addUser(newPlayer);
 			gRoom.start();
 			try{
 				newPlayer.asdf();
 			} catch(SocketException se){
 				Printu("Player Disconnected");
 			}
 		}
 	}
 
 }
