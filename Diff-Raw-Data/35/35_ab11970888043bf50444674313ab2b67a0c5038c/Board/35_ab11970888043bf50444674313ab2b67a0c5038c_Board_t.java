 package board;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import piece.BoardTile;
 import piece.GameObject;
 import piece.Room;
 import piece.Weapon;
 import piece.GameCharacter;
 
 import game.Player;
 
 /**
  * So well, this is going to where the game loop is,
  * I will first initialize the board, each players hands, 
  * the 'killer' and, the players start positions (guess I 
  * I should probably make that random?)
  * 
  * Also this is where I'll have the 2D array for the board,
  * the only things I'll store on the board is if it'study part of the 
  * hallway or an entrance way, if it'study an entrance way I'll sort
  * out which room it'study trying to get into... somehow.
  * 
  * 
  * How could you ever think I would play Digimon.. I'm quite offended.
  * @author Potato
  *
  */
 public class Board {
 	
 	private ArrayList<Player> players;
 	private ArrayList<Room> rooms;
 	private ArrayList<Weapon> weapons;
 	private ArrayList<GameCharacter> characters;
 	private ArrayList<GameObject> hand;
 	
 	@SuppressWarnings("unused")
 	private BoardTile[][] board;
 
 	
 
 	public Board(){
 		
 		rooms = new ArrayList<Room>(9);
 		weapons = new ArrayList<Weapon>(6);
 		characters = new ArrayList<GameCharacter>(6);
 		hand = new ArrayList<GameObject>();
 		players = new ArrayList<Player>(4);
 		
 		//Only place a player can move on is the 'hallway' or an 'entrance'
 		//change null to a set of starting items in each room.
 		
 		GameCharacter mScar = new GameCharacter("Miss Scarlett");
 		GameCharacter cMust = new GameCharacter("Colonel Mustard");
 		GameCharacter mWhit = new GameCharacter("Mrs White");
 		GameCharacter rGree = new GameCharacter("Reverent Green");
 		GameCharacter mPeac = new GameCharacter("Mrs Peacock");
 		GameCharacter pPlum = new GameCharacter("Professor Plum");
 		
 		characters.add(mScar);
 		characters.add(cMust);
 		characters.add(mWhit);
 		characters.add(rGree);
 		characters.add(mPeac);
 		characters.add(pPlum);
 		
 		hand.addAll(characters);
 		
 		init();
 		
		for(Player p: players){
			System.out.println(p.getCharacter());
		}
		
 		Weapon candleStick = new Weapon("Candle Stick");
 		Weapon dagger = new Weapon("Dagger");
 		Weapon leadPipe = new Weapon("Lead Pipe");
 		Weapon revolver = new Weapon("Revolver");
 		Weapon rope = new Weapon("Rope");
 		Weapon spanner = new Weapon("Spanner");
 		
 		weapons.add(candleStick);
 		weapons.add(dagger);
 		weapons.add(leadPipe);
 		weapons.add(revolver);
 		weapons.add(rope);
 		weapons.add(spanner);
 		
 		Collections.shuffle(weapons);
 		
 		Room kitchen = new Room("Kitchen");
 		kitchen.addWeapon(weapons.get(0));
 		Room diningRoom = new Room("Kitchen");
 		diningRoom.addWeapon(weapons.get(1));
 		Room lounge = new Room("Lounge");
 		lounge.addWeapon(weapons.get(2));
 		Room ballRoom = new Room("Ball Room");
 		ballRoom.addWeapon(weapons.get(3));
 		Room conservatory = new Room("Conservatory");
 		conservatory.addWeapon(weapons.get(4));
 		Room billiardRoom = new Room("Billiard Room");
 		billiardRoom.addWeapon(weapons.get(5));
 		Room library = new Room("Library");
 		Room study = new Room("Study");
 		Room hall = new Room("Hall");
 
 		rooms.add(kitchen);
 		rooms.add(diningRoom);
 		rooms.add(lounge);
 		rooms.add(ballRoom);
 		rooms.add(conservatory);
 		rooms.add(billiardRoom);
 		rooms.add(library);
 		rooms.add(study);
 		rooms.add(hall);
 	
 		Collections.shuffle(rooms);
				
 		hand.addAll(rooms);
 		hand.addAll(weapons);
 		
 		BoardTile k = new BoardTile("K", kitchen); //Kitchen
 		BoardTile dr = new BoardTile("D", diningRoom); //DiningRoom
 		BoardTile l = new BoardTile("l", lounge); //Lounge
 		BoardTile br = new BoardTile("B", ballRoom); //BallRoom
 		BoardTile c = new BoardTile("C", conservatory); //Conservatory
 		BoardTile b = new BoardTile("b", billiardRoom); //BilliardRoom
 		BoardTile li = new BoardTile("L", library); //Library
 		BoardTile s = new BoardTile("S", study); //Study
 		BoardTile h = new BoardTile("H", hall); //Hall
 		BoardTile w = new BoardTile("W", null); //Wall, check if the position is a wall if it is don't move through it... duh.
 		BoardTile r = new BoardTile("-", null); //Room, while a player is inside a room he should be able to move freely without any cost, so before moving check if it's an instance of this.
 		BoardTile hw = new BoardTile(" ", null); //Hall Way, the best kind of way
 		BoardTile st = new BoardTile("S", null); //Start Point, spawn point
 
 		// Each room keeps track of the items in it. Just need to write a contains method or something :D
 		
 		BoardTile[][] board = {
 				{ w, w, w, w, w, w, w, w, w,st, w, w, w,st, w, w, w, w, w, w, w, w, w},
 				{ w, r, r, r, r, w, w,hw,hw,hw, w, r, w,hw,hw,hw, w, w, r, r, r, r, w},
 				{ w, r, r, r, r, w,hw,hw, w, w, w, r, w, w, w,hw,hw, w, r, r, r, r, w},
 				{ w, r, r, r, r, w,hw,hw, w, r, r, r, r, r, w,hw,hw, c, r, r, r, r, w},
 				{ w, r, r, r, r, w,hw,hw,br, r, r, r, r, r,br,hw,hw,hw, w, w, w, w, w},
 				{ w, w, w, w, k, w,hw,hw, w, r, r, r, r, r, w,hw,hw,hw,hw,hw,hw,hw,st},
 				{ w,hw,hw,hw,hw,hw,hw,hw, w,br, w, w, w,br, w,hw,hw,hw,hw,hw,hw,hw, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, w, w, w},
 				{ w, w, w, w, w,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, b, r, r, r, r, w},
 				{ w, r, r, r, w, w, w, w,hw,hw, w, w, w, w,hw,hw,hw, w, r, r, r, r, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw, w, r, r, r, r, w},
 				{ w, r, r, r, r, r, r,dr,hw,hw, w, w, w, w,hw,hw,hw, w, w, w, w, b, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw,hw,hw,hw,hw,hw, w},
 				{ w, r, r, r, r, r, r, w,hw,hw, w, w, w, w,hw,hw,hw, w, w,li, w, w, w},
 				{ w, w, w, w, w, w,dr, w,hw,hw, w, w, w, w,hw,hw, w, w, r, r, r, r, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, w,hw,hw,li, r, r, r, r, r, w},
 				{st,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw,hw, w, w, w, r, r, r, w},
 				{ w,hw,hw,hw,hw,hw,hw,hw,hw, w, w, h, h, w, w,hw,hw,hw, w, w, w, w, w},
 				{ w, w, w, w, w, w, w,hw,hw, w, r, r, r, r, w,hw,hw,hw,hw,hw,hw,hw,st},
 				{ w, r, r, r, r, r, l,hw,hw, w, r, r, r, r, h,hw,hw,hw,hw,hw,hw,hw, w},
 				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, w, s, w, w, w, w},
 				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, w, r, r, r, r, w},
 				{ w, r ,r, r, r, r, w,hw,hw, w, r, r, r, r, w,hw,hw, w, r, r, r, r, w},
 				{ w, w, w, w, w, w, w,st, w, w, w, w, w, w, w, w, w, w, w, w, w, w, w}
 				};
 		
 		this.board = board;
 		
 for(int i = 0; i < board.length; i++){
 	for(int j = 0; j < board[i].length; j++){
 		System.out.printf("%s ",board[i][j].print());
 	}
 	System.out.println();
 }
 
 		//add rooms to deck
 		//add characters to deck
 		//shuffle deck
 
 	}
 	
 	public void init(){
 		System.out.println("Welcome to Cluedo!");
 		System.out.println("Please enter the number of players, it must be between 2 and 4.");
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean selected;
 		try {
 			int player = Integer.parseInt(br.readLine());
 			for(int i = 1; i < player + 1; i++){
 				System.out.println("Player: " + i + " please select a character from:");
 				for(GameCharacter c: characters){
 					System.out.printf("%s \n",c.getName());
 				}
 				System.out.printf("-----------------------------------------\n");
 
				selected = false;
				while(!selected){
					String playerChoice = br.readLine();
					playerChoice.trim();
					for(int j = 0; j < characters.size(); j++){ 
						GameCharacter c = characters.get(j);
						if(c.getName().equalsIgnoreCase(playerChoice)){
							players.add(new Player(c, i));
							characters.remove(j);
							selected = true;
						}
 					}
 				}
 			}
 			System.out.println(player);
 		} catch (Exception e) {}
 	}
 
 
 	/**
 	 * reserved for the combination of room/weapon/killer, needs to be guessed to win.
  * @author Potato
  *
  */
 	private class Crime{
 		public final Weapon w;
 		public final Room r;
 		public final GameCharacter c;
 		public Crime(Weapon w, Room r, GameCharacter c){
 			this.w = w;
 			this.r = r;
 			this.c = c;
 		}
 		
 		
 	}
 }
