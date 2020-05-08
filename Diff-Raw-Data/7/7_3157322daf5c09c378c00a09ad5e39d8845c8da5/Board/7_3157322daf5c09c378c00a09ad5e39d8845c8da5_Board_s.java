 /**
  * Stores the current state of the board in a 2D array of Locations.
  * It also capable of turning the board into a string to be printed.
  * 
  * Board created with http://sandbox.yoyogames.com/games/214376/download
  *
  * @author Ciaran Byrne, Lord Lachlan Ridley, Supreme Ruler and minor Deity
  *
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class Board {
 
 	private int WIDTH = 10;
 	private int HEIGHT = 10;
 
	// we have this in main on't want to duplicate stuff.
	private List<Room> rooms;

 	private Location[][] board;
 	private boolean[][] visited;
 
 	public Board(List<Room> rooms) {
 		board = new Location[WIDTH][HEIGHT];
 		generateBoard();
 	}
 
 	public Board(File boardFile, List<Character> characters) {
 		try {
 			Scanner sc = new Scanner(boardFile);
 
 			WIDTH = sc.nextInt();
 			HEIGHT = sc.nextInt();
 
 			board = new Location[WIDTH][HEIGHT];
 
 			for (int y = 0; y < HEIGHT; y++) {
 				for (int x = 0; x < WIDTH; x++) {
 					board[x][y] = convertStringToLocation(sc.nextLine(),
 							characters);
 				}
 			}
 		} catch (FileNotFoundException e) {
 		}
 	}
 
 	private Location convertStringToLocation(String next,
 			List<Character> characters) {
 		if (next.equals("."))
 			return new Tile();
 		else if (next.equals("#"))
 			return new Wall();
 		// we've gone through all the potential starting points, so this must be
 		// a character start
 		else {
 			Tile charStart = new Tile();
 			Character character = null;
 			for (Character c : characters) {
 				if (c.toString().equals(next)) {
 					character = c;
 					break;
 				}
 			}
 			charStart.setCharacter(character);
 			return charStart;
 		}
 	}
 
 	public void generateBoard() {
 		// create the rooms
 		// TODO: Generate rooms in board class
 		// TODO there were no fields so I added some
 		// rooms = new ArrayList<Room>();
 		// List<Weapon> temp = new ArrayList<Weapon>();
 		// temp.addAll(weapons);
 		// int rand;
 		// for (String r : ROOM) {
 		// rand = (int) Math.random() * temp.size();
 		// rooms.add(new Room(r, temp.get(rand)));
 		// temp.remove(rand);
 		// TODO: ^^^^^ Confirm that we no longer need this (all handles in Main)
 
 		// TODO: replace this with a working board
 		for (int x = 0; x < WIDTH; x++) {
 			for (int y = 0; y < HEIGHT; y++) {
 				board[x][y] = new Tile();
 			}
 
 		}
 	}
 
 	public int findDistanceBetweenTiles(Location l1, Location l2) {
 		int x1 = 1, y1 = 1, x2 = 1, y2 = 1;
 		return findDistanceBetweenLocations(x1, y1, x2, y2);
 	}
 
 	public int findDistanceBetweenLocations(int x1, int y1, int x2, int y2) {
 		// TODO: implement dijkstra's algorithm to find shortest distance
 		// between coordinates
 		// (could use A* I guess but I'm not sure it's really worth the time to
 		// implement it.
 		// The board is so small that dijkstra's will never be that inefficient)
 
 		// TODO is this over the top? Could we got that they have movements
 		// according
 		// to the number of dice rolls, and use those movemnent points by going.
 		// if 5 movement points they can move directions one at a time like.
 		// North then North, then North then West, then west
 		visited = new boolean[WIDTH][HEIGHT];
 		exploreNode(x1, y1, x2, y2);
 
 		return 1;
 	}
 
 	private int exploreNode(int x1, int y1, int x2, int y2) {
 		visited[x1][y1] = true;
 		int smallestDistance = Integer.MAX_VALUE;
 
 		// TODO: final version probably doesn't need to check for edges as my
 		// proposed map doesn't allow a player to reach them (e.g. whole board
 		// is surrounded by walls)
 
 		// up
 		if (!visited[x1][y1 - 1] && y1 > 0
 				&& !(board[x1][y1 - 1] instanceof Wall)) {
 			int dist = exploreNode(x1, y1 - 1, x2, y2);
 			if (dist < smallestDistance)
 				smallestDistance = dist;
 		}
 		// down
 		if (!visited[x1][y1 + 1] && y1 > 0
 				&& !(board[x1][y1 + 1] instanceof Wall)) {
 			int dist = exploreNode(x1, y1 + 1, x2, y2);
 			if (dist < smallestDistance)
 				smallestDistance = dist;
 		}
 		// left
 		if (!visited[x1 - 1][y1] && y1 > 0
 				&& !(board[x1 - 1][y1] instanceof Wall)) {
 			int dist = exploreNode(x1 - 1, y1, x2, y2);
 			if (dist < smallestDistance)
 				smallestDistance = dist;
 		}
 		// right
 		if (!visited[x1 + 1][y1] && y1 > 0
 				&& !(board[x1 + 1][y1] instanceof Wall)) {
 			int dist = exploreNode(x1 + 1, y1, x2, y2);
 			if (dist < smallestDistance)
 				smallestDistance = dist;
 		}
 
 		return smallestDistance;
 	}
 
 	public boolean validLocation(int x, int y) {
 		// TODO: just check the coordinates are in the array
 		return false;
 	}
 
 	public Location findCharacter(Character c) {
 		// TODO: findCharacter
 		return board[0][0];
 	}
 
 	public void moveCharacter(int x, int y) {
 		// TODO: moveCharacter
 	}
 
 	public void moveWeapon() {
 		// TODO: moveWeapon
 	}
 
 	public void displayBoard() {
 		System.out.println();
 		for (int x = 0; x < WIDTH; x++) {
 			for (int y = 0; y < HEIGHT; y++) {
 				if (board[x][y] != null)
 					System.out.print(board[x][y].getBoardToken());
 			}
 			System.out.print("\n");
 		}
 		System.out.println();
 	}
 }
