 import java.util.Scanner;
 import java.net.*;
 import java.io.*;
 
 public class Main {
 
     final String[] shipNames = {"patrol boat", "destroyer", "submarine", "battleship", "aircraft carrier"};
 
     //variables for communication
     boolean isHost;
    boolean local;
     ServerSocket serverSocket = null;
     Socket clientSocket = null;
     PrintWriter out = null;
     Scanner stdIn = null;
     Scanner in = null;
 
     //game variables
     final int[] shipLens = {2,3,3,4,5};
 
     Board ships;
     Board enemyShips;
 
     int[][] shipLocations;
     boolean[] shipOrients;
 
     public Main() {
 	//is this a host or a client?
 	stdIn = new Scanner(System.in);
 	boolean success = false;
 	while (!success) {
	    System.out.println("local, host, or client? ");
 	    String response = stdIn.nextLine();
 	    success = true;
 	    if (response.equals("host"))
 		isHost = true;
 	    else if (response.equals("client"))
 		isHost = false;
	    else if (response.equals("local"))
		local = true;
 	    else
 		success = false;
 	}
 	
 	try {
 	    if (isHost) {
 		//open server socket
 		System.out.print("opening server socket... ");
 		serverSocket = new ServerSocket(4444);
 		System.out.println("opened.");
 
 		//accept client socket
 		System.out.print("accepting client socket... ");
 		clientSocket = serverSocket.accept();
 		System.out.println("accepted.");
 	    } else {
 		System.out.println("Enter the host's IP address: ");
 		String ip = stdIn.nextLine();
 
 		//connect to host
 		System.out.print("connecting to host... ");
 		clientSocket = new Socket(ip, 4444);
 		System.out.println("connected");
 	    }
 	    out = new PrintWriter(clientSocket.getOutputStream(), true);
 	    in = new Scanner(clientSocket.getInputStream());
 	} catch (UnknownHostException e) {
 	    System.err.println("Can't find host.");
 	    System.exit(-1);
 	} catch (IOException e) {
 	    System.err.println("Connection failed: " + e);
	    System.exit(-1);
 	}
 
 	//at this point we are connected to our partner
 	//the game can begin.
 	quit: //full quit breakpoint for if the player does not wish to play again
 	while (true) {
 	    ships = new Board();
 	    enemyShips = new Board();
 	    shipLocations = new int[2][5];
 	    shipOrients = new boolean[5];
 	    placeShips();
 	    
 	    //if you're the host, send ship locations
 	    if (isHost)
 		for (int i=0; i<5; i++)
 		    out.println(shipLocations[0][i] + " " + shipLocations[1][i] + " " + shipOrients[i]);
 	    //get other player's shipLocations
 	    for (int i=0; i<5; i++) {
 		int x = in.nextInt();
 		int y = in.nextInt();
 		boolean orient = in.nextBoolean();
 		enemyShips.addShip(x, y, new Ship(shipLens[i], orient));
 	    }
 	    in.nextLine();
 
 	    if (!isHost) {
 		for (int i=0; i<5; i++)
 		    out.println(shipLocations[0][i] + " " + shipLocations[1][i] + " " + shipOrients[i]);
 	    }
 
 	    //the host gets to pick who goes first
 	    boolean goesFirst = false;
 	    if (isHost) {
 		boolean done = false;
 		while (!done) {
 		    System.out.print("Would you like to go first? (y/n) ");
 		    String response = stdIn.nextLine();
 		    done = true;
 		    if (response.equals("y"))
 			goesFirst = true;
 		    else if (response.equals("n"))
 			goesFirst = false;
 		    else
 			done = false;
 		}		
 		//tell the other player if they go first
 		out.println(!goesFirst);
 	    } else {
 		//if not the host, check who the host chose to go first
 		goesFirst = in.nextBoolean();
 		if (goesFirst)
 		    System.out.println("You go first.");
 		else
 		    System.out.println("The host will go first.");
 	    }
 	    
 	    //if we go first, take a turn
 	    if (goesFirst) {
 		printBoard();
 		takeTurn();
 	    }
 
 	    endgame: //endgame breakpoint
 	    while (true) {
 		getEnemyMove();
 		printBoard();
 		if (endgame())
 		    break endgame;
 		takeTurn();
 		printBoard();
 		if (endgame())
 		    break endgame;
 	    }
 	    
 	    if (won()) {
 		System.out.println("You won!");
 	    } else {
 		System.out.println("You lost.");
 	    }
 	    
 	    //check if player wants to play again
 	    System.out.println("Do you want to play again? (y/n) ");
 	    boolean playAgain = false;
 	    boolean done = false;
 	    while (!done) {
 		String response = stdIn.nextLine();
 		done = true;
 		if (response.equals("y"))
 		    playAgain = true;
 		else if (response.equals("n"))
 		    playAgain = false;
 		else
 		    done = false;
 	    }
 
 	    boolean otherPlayerPlayAgain;
 	    //inform other player of our decision
 	    if (isHost) {
 		out.println(playAgain);
 		otherPlayerPlayAgain = in.nextBoolean();
 	    } else {
 		otherPlayerPlayAgain = in.nextBoolean();
 		out.println(playAgain);
 	    }
 
 	    if (!playAgain) {
 		System.out.println("Bye!");
 		break quit;
 	    }
 	    if (!otherPlayerPlayAgain) {
 		System.out.println("The other guy doesn't want to play again.");
 		break quit;
 	    }
 	}
 	try {
 	    if (serverSocket != null)
 		serverSocket.close();
 	    clientSocket.close();
 	} catch (IOException e) {
 	    System.err.println("Error closing sockets.");
 	}
 	stdIn.close();
 	out.close();
 	in.close();
     }
 
     public void placeShips() {
 	for (int i=0; i<5; i++) {
 	    boolean done = false;
 	    while (!done) {
 		printBoard();
 		System.out.println("Where do you want to place your " + shipNames[i] + "? (x y) ");
 		int x = stdIn.nextInt();
 		int y = stdIn.nextInt();
 		stdIn.nextLine();
 		System.out.println("Horizontal? (y/n) ");
 		boolean orient = stdIn.nextLine().equals("y");
 		if (ships.addShip(x,y,new Ship(shipLens[i], orient))) {
 		    done = true;
 		    shipLocations[0][i] = x;
 		    shipLocations[1][i] = y;
 		    shipOrients[i] = orient;
 		} else
 		    System.out.println("That place is not valid. Try again.");
 	    }
 	}
 	printBoard();
 	System.out.println("Done. Waiting for opponent.");
        
     }
 
     public void takeTurn() {
 	boolean done = false;
 	int result = 0;
 	int x = 0;
 	int y = 0;
 	while (!done) {
 	    System.out.println("Where do you want to fire? (x y)");
 	    x = stdIn.nextInt();
 	    y = stdIn.nextInt();
 	    result = enemyShips.fire(x,y);
 
 	    //inform player of result
 	    if (result >= 0) {
 		done = true;
 		if (result == 0)
 		    System.out.println("Miss.");
 		else if (result == 1)
 		    System.out.println("A hit!");
 		else
 		    System.out.println("You sunk a ship!");
 	    } else if (result == -1)
 		System.out.println("You have already shot there!");
 	    else if (result == -2)
 		System.out.println("Those are not valid coordinates.");
 	}
 	
 	//send shot to enemy
 	out.println(x + " " + y);
 	System.out.println("sent");
     }
 
     public void getEnemyMove() {
 	//get shot coordinates
 	int xpos = in.nextInt();
 	int ypos = in.nextInt();
 	//fire there to update board
 	int result = ships.fire(xpos, ypos);
 
 	if (result == 0)
 	    System.out.println("The enemy misses.");
 	else
 	    System.out.println("The enemy hits!");
     }
 
     public void printBoard() {
 	String s = "  ";
 	for (int i=0; i<10; i++)
 	    s += " " + (i+1);
 	for (int i=0; i<10; i++) {
 	    s += "\n";
 	    s += i+1;
 	    if (i < 9)
 		s += " ";
 	    for (int j=0; j<10; j++)
 		s += " " + getChar(enemyShips.get(j,i), false);
 	}
 	System.out.println(s);
 	s = "  ";
 	for (int i=0; i<10; i++)
 	    s += " " + (i+1);
 	for (int i=0; i<10; i++) {
 	    s += "\n";
 	    s += i+1;
 	    if (i < 9)
 		s += " ";
 	    for (int j=0; j<10; j++)
 		s += " " + getChar(ships.get(j,i), true);
 	}
 	System.out.println(s);
     }
 
     public String getChar(int tile, boolean showShips) {
 	if (tile == Board.BLANK || (!showShips && tile >= 0))
 	    return "~";
 	else if (tile == Board.MISS)
 	    return "m";
 	else if (tile == Board.HIT)
 	    return "x";
 	else
 	    return "O";
     }
 
     public boolean endgame() {
 	return ships.shipsAlive == 0 || enemyShips.shipsAlive == 0;
     }
 
     public boolean won() {
 	return enemyShips.shipsAlive == 0;
     }
 
     public static void main(String[] args) {
 	new Main();
     }
 
 }
