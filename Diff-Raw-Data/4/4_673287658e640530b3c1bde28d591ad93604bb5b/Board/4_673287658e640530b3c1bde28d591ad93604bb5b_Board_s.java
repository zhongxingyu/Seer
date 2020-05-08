 package Board;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.Random;
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import Board.Card.type;
 import Board.RoomCell.DoorDirection;
 
 
 //Authors: Arnaud Filliat and Vy Ta
 //Make sure to note that for the cells we use (x,y) coordinates so (column, row)
 public class Board extends JPanel{
 	public static final int CELL_SIZE = 25;
 
 
 	private ArrayList<BoardCell> cells;
 	static public int numRows;
 	static public int numColumns;
 	private Map<Character, String> rooms;
 	private Map<Integer, ArrayList<Integer>> adjs = new HashMap<Integer, ArrayList<Integer>>();
 	private LinkedList<Integer> adjList;
 	private ArrayList<Integer> visited = new ArrayList<Integer>();
 	private Set<BoardCell> targets = new HashSet<BoardCell>();
 	private String LegendFile;
 	private String BoardFile;
 	private int die;
 	private HumanPlayer human;
 	private int turnCounter;
 	private boolean humansTurn;
 	private ArrayList<Player> players = new ArrayList<Player>();
 	private ArrayList<Card> deck = new ArrayList<Card>();
 	private ArrayList<Card> testDeck = new ArrayList<Card>(); //Only used for testing.
 	private ArrayList<Card> Solution = new ArrayList<Card>();
 	private ArrayList<Card> weapons = new ArrayList<Card>();
 	private ArrayList<Card> people = new ArrayList<Card>();
 	private ArrayList<Card> roomCards = new ArrayList<Card>();
 
 	public Board(String BoardFile, String LegendFile) {	
 		this.LegendFile = LegendFile;
 		this.BoardFile = BoardFile;
 		try {
 			loadConfigFiles();
 			calcAdjacencies();
 			addMouseListener(new PlayerClick());
 		} catch (Exception e) {
 			System.out.println(e.getLocalizedMessage());
 		}
 	}
 
 	public Board() {
 		this.BoardFile = "ClueLayout.csv";
 		this.LegendFile = "legend.txt";
 		turnCounter = 0;
 		try {
 			loadConfigFiles();
 			calcAdjacencies();
 			addMouseListener(new PlayerClick());
 		} catch (Exception e) {
 			System.out.println(e.getLocalizedMessage());
 		}
 	}
 
 	public void loadConfigFiles() {
 		try {
 			loadLegend(LegendFile);
 			loadBoard(BoardFile);
 			loadCards("cards.txt");
 			loadPlayers("People.txt");
 			//paintComponent(Graphics g = new Graphics();)
 			//loadCards("cards.txt");
 			dealCards();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadBoard(String fileName) throws BadConfigException, FileNotFoundException {
 		FileReader reader = new FileReader(fileName);
 		Scanner in = new Scanner(reader);
 		cells = new ArrayList<BoardCell>();
 		numRows = 0;
 		numColumns = -1;
 
 		while(in.hasNextLine()){
 			String line = in.nextLine();
 			String[] line2 = line.split(",");
 
 			//check columns
 			if(line2.length != numColumns && numColumns != -1){
 				throw new BadConfigException();
 			}
 			numColumns = line2.length;
 
 			//add celldata to arraylist
 			for(int i = 0; i < line2.length; i++){
 
 				//adds doors
 				if(line2[i].length() == 2){
 					if(line2[i].charAt(1) == 'U'){
 						cells.add(new RoomCell(i, numRows, RoomCell.DoorDirection.UP, line2[i].charAt(0)));
 					}else if(line2[i].charAt(1) == 'D'){
 						cells.add(new RoomCell(i, numRows, RoomCell.DoorDirection.DOWN, line2[i].charAt(0)));
 					}else if(line2[i].charAt(1) == 'R'){
 						cells.add(new RoomCell(i, numRows, RoomCell.DoorDirection.RIGHT, line2[i].charAt(0)));
 					}else if(line2[i].charAt(1) == 'L'){
 						cells.add(new RoomCell(i, numRows, RoomCell.DoorDirection.LEFT, line2[i].charAt(0)));
 					} else {
 						throw new BadConfigException();
 					}
 				} else{
 					//add walkway
 					if( line2[i].equalsIgnoreCase("w")){
 						cells.add(new WalkwayCell(i, numRows));
 
 						//add other rooms
 					} else if(rooms.containsKey((Character)line2[i].charAt(0))){
 						cells.add(new RoomCell(i, numRows, RoomCell.DoorDirection.NONE, line2[i].charAt(0)));
 					} else {
 						throw new BadConfigException();
 					}
 				}
 			}
 			numRows++;
 		}
 	}
 
 	public void loadLegend(String fileName) throws FileNotFoundException, BadConfigException {
 		FileReader reader = new FileReader(fileName);
 		Scanner in = new Scanner(reader);
 		rooms = new HashMap<Character, String>();
 
 		while(in.hasNextLine()){
 			String line = in.nextLine();
 			String line2 = line.substring(3);
 
 			//check format
 			if(line.charAt(1) == ',' && !line2.contains(",")) {
 				rooms.put(line.charAt(0), line.substring(3));
 			} else {
 				throw new BadConfigException();
 			}
 		}
 	}
 
 	public void loadPlayers(String fileName) throws FileNotFoundException {
 
 		FileReader reader = new FileReader(fileName);
 		Scanner in = new Scanner(reader);
 		players.clear();
 
 		String line = in.nextLine();
 		String[] data = line.split(",");
 
 		String name = data[0];
 		String color = data[1];
 		String spot = data[2];
 		int index = Integer.parseInt(spot);
 
 		human = new HumanPlayer(name, color, index, getWeapons(), getPeople(), getRoomCards());
 		human.setCurrentIndex(index);
 		players.add(human);
 
 		while(in.hasNextLine()){
 			line = in.nextLine();
 			data = line.split(",");
 
 			name = data[0];
 			color = data[1];
 			spot = data[2];
 			index = Integer.parseInt(spot);
 
 			Player next = new ComputerPlayer (name, color, index, getWeapons(), getPeople(), getRoomCards());
 			next.setCurrentIndex(index);
 			players.add(next);				
 		}
 	}
 
 	public void loadCards(String fileName) throws FileNotFoundException  {
 		FileReader reader = new FileReader(fileName);
 		Scanner in = new Scanner(reader);
 		deck.clear();
 		type cardType;
 
 		while(in.hasNextLine()){
 			String line = in.nextLine();
 			String[] data = line.split(",");
 
 			if (data[0].equalsIgnoreCase("w")) {
 				cardType = type.WEAPON;
 				getWeapons().add(new Card(cardType, data[1]));
 			} else if (data[0].equalsIgnoreCase("p")) {
 				cardType = type.PERSON;
 				getPeople().add(new Card(cardType, data[1]));
 			} else {
 				cardType = type.ROOM;
 				getRoomCards().add(new Card(cardType, data[1]));
 			}
 
 			String content = data[1];
 
 			Card next = new Card  (cardType, content);
 			deck.add(next);				
 		}	
 
 		testDeck.clear();
 		for(int i = 0; i < deck.size() ; i++) {
 			testDeck.add(deck.get(i));
 		}
 
 
 		//testDeck = deck;
 	}
 
 	public void dealCards () {
 		//System.out.println(testDeck);
 		Random generator = new Random();
 		int choice = generator.nextInt(9);
 		Solution.add(deck.get(choice + 12));
 		deck.remove(choice + 12);
 		choice = generator.nextInt(6);
 		Solution.add(deck.get(choice + 6));
 		deck.remove(choice + 6);
 		choice = generator.nextInt(6);
 		Solution.add(deck.get(choice));
 		deck.remove(choice);
 		//System.out.println("Soln " + Solution);
 
 		while(!(deck.isEmpty())) {
 			for (int i = 0 ; i < players.size(); i++) {
 				choice = generator.nextInt(deck.size());
 				players.get(i).getCards().add(deck.get(choice));
 				//if(i != 5)
 				//	players.get(i +1).getCards().add(deck.get(choice));
 				//else
 				//	players.get(1).getCards().add(deck.get(choice));
 				//System.out.println("Gave " + players.get(i).getName() + deck.get(choice));
 				deck.remove(choice);
 				if (deck.isEmpty())
 					break;
 			}
 		}
 	}
 
 
 	public Boolean makeAccusation (String room, String person, String weapon) {
 		if (Solution.get(0).getContent().equalsIgnoreCase(room) 
 				&& Solution.get(1).getContent().equalsIgnoreCase(person)
 				&& Solution.get(2).getContent().equalsIgnoreCase(weapon))
 			return true;
 		else
 			return false;
 	}
 
 	public String handelSuggestion(String room, String person, String weapon, Player accuser) {
 		String info = null;
 		ArrayList<String> dissapprovals = new ArrayList<String>();
 		Boolean cardShown = false;
 		for (int i = 0; i < players.size(); i++ ) {
 			if (cardShown)
 				break;
 			if (players.get(i).getName().equalsIgnoreCase(accuser.getName()))
 				continue;
 			else {
 				for (int j = 0; j < players.get(i).getCards().size(); j++) {
 					if (players.get(i).getCards().get(j).getContent().equalsIgnoreCase(room)
 							|| players.get(i).getCards().get(j).getContent().equalsIgnoreCase(person)
 							|| players.get(i).getCards().get(j).getContent().equalsIgnoreCase(weapon)) {
 						dissapprovals.add(players.get(i).revealCard(players.get(i).getCards().get(j)).getContent());
 						cardShown = true;
 						//break;
 					}			
 				}
 			}
 		}
 		if (dissapprovals.size() > 0) {
 			Random generator = new Random();
 			info = dissapprovals.get(generator.nextInt(dissapprovals.size()));
 		}	
 		return info;
 	}
 
 	public int getNumRows() {
 		return numRows;
 	}
 
 	public int getNumColumns(){
 		return numColumns;
 	}
 
 	public RoomCell getRoomCellAt(int column, int row){
 		int index = calcIndex(column, row);
 		if(cells.get(index).isRoom()){
 			return (RoomCell) cells.get(index);
 		} else {
 			return null;
 		}
 	}
 
 	public RoomCell getRoomCellAt(int index){
 		if(cells.get(index).isRoom()){
 			return (RoomCell) cells.get(index);
 		} else {
 			return null;
 		}
 	}
 
 	public BoardCell getCellAt(int index){
 		BoardCell cell = cells.get(index);
 		return cell;
 	}
 
 	public int calcIndex(int col, int row) {
 		if (col == this.getNumColumns()) {
 			col--;
 		}
 		if (row == this.getNumRows()) {
 			row--;
 		}
 		return col + row*(numColumns);
 	}
 
 	public Map<Character, String> getRooms() {
 		return rooms;
 	}
 
 	public void calcAdjacencies() {
 		int index;
 		for (index = 0; index <= calcIndex(getNumColumns(), getNumRows()); index ++ ) {
 
 			if(index > 117){
 				int i = 0;
 			}
 
 
 			ArrayList<Integer> spots = new ArrayList<Integer>();
 			if (cells.get(index).isWalkway() || cells.get(index).isDoorway()) {
 				if ((index+1) % (getNumColumns()) != 0) {
 					RoomCell testRoom = getRoomCellAt(index + 1);
 					if (cells.get(index + 1).isWalkway()|| ((cells.get(index + 1).isDoorway()) && testRoom.getDoorDirection() == DoorDirection.LEFT )){
 						spots.add(index + 1);
 					}
 				}
 				if (index % getNumColumns() != 0){
 					RoomCell testRoom = getRoomCellAt(index - 1);
 					if (cells.get(index - 1).isWalkway() || ((cells.get(index - 1).isDoorway()) && testRoom.getDoorDirection() == DoorDirection.RIGHT )) {
 						spots.add(index - 1);
 					}
 				}
 				if ((index + this.getNumColumns()) <= calcIndex(this.getNumColumns(), this.getNumRows())){
 					RoomCell testRoom = getRoomCellAt(index + this.getNumColumns());
 					if (cells.get(index + this.getNumColumns()).isWalkway() || ((cells.get(index + this.getNumColumns()).isDoorway()) && testRoom.getDoorDirection() == DoorDirection.UP )) {
 						spots.add(index + this.getNumColumns());
 					}
 				}
 				if (index - this.getNumColumns() >= 0){
 					RoomCell testRoom = getRoomCellAt(index - this.getNumColumns());
 					if (cells.get(index - this.getNumColumns()).isWalkway() || ((cells.get(index - this.getNumColumns()).isDoorway()) && testRoom.getDoorDirection() == DoorDirection.DOWN )) {
 						spots.add(index - this.getNumColumns());
 					}
 				}
 			}	
 			adjs.put(index, spots);
 			//System.out.println("Cell indexed " + index + " " + adjs.get(index));
 		}
 	}
 
 	public ArrayList<Integer> getAdjList(int index) {
 		return adjs.get(index);
 	}
 
 	public String getRoomName(char init) {
 		return rooms.get(init);
 	}
 
 	public void startTargets(int index, int numSteps){
 		visited.clear();
 		visited.add(index);
 		targets.clear();
 		calcTargets(index, numSteps);
 	}
 
 	public void calcTargets(int index, int numSteps) {
 		ArrayList<Integer> possibleSpots = new ArrayList<Integer>();
 		for (Integer i : getAdjList(index)) {
 			if (!(visited.contains(i))) {
 				possibleSpots.add(i);
 			}
 		}
 
 		for (Integer j: possibleSpots) {
 			visited.add(j);
 			if (this.getCellAt(j).isDoorway())
 				targets.add(this.getCellAt(j));
 			else if (numSteps == 1) {
 				targets.add(this.getCellAt(j));
 				visited.remove(j);
 			}
 			else {		
 				calcTargets(j, numSteps - 1);
 				visited.remove(j);
 			}
 		}
 	}
 
 	public Set<BoardCell> getTargets() {
 		return targets;
 	}
 
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	public ArrayList<Card> getDeck() {
 		return deck;
 	}
 
 	public ArrayList<Card> getTestDeck() {
 		return testDeck;
 	}
 
 	public void setSoln(Card roomCard, Card personCard, Card weaponCard) { //ONLY USE FOR TESTING!!
 		Solution.clear();
 		Solution.add(roomCard);
 		Solution.add(personCard);
 		Solution.add(weaponCard);
 	}
 
 
 	public Color convertColor(String strColor) {
 		Color color; 
 		try {     
 			// We can use reflection to convert the string to a color
 			System.out.println(strColor.trim());
 			Field field = Class.forName("java.awt.Color").getField(strColor.trim());     
 			color = (Color)field.get(null); } 
 		catch (Exception e) {  
 			color = null; // Not defined } 
 		}
 		return color;
 	}
 
 
 
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		int counter = 0;
 		g.setColor(Color.GRAY);
 		g.fillRect(0, 0, numColumns * CELL_SIZE, numRows * CELL_SIZE);
 
 		for(BoardCell cell : cells)
 			cell.draw(g);
 
 		/*
         for (int i = 0; i < numRows; ++i) {
                 for (int j = 0; j < numColumns; ++j) {
                         int index = calcIndex(i, j);
                         int x = j * CELL_SIZE;
                         int y = i * CELL_SIZE;
                         //int counter = 0;
 
                         BoardCell cell = getCellAt(index);
                         if (getRoomCellAt(index) != null) {
                         	System.out.println("A room Cell!");
                         	counter++;
                         	System.out.println(counter);
                             //RoomCell roomCell = (RoomCell) cell;
                         	RoomCell room = getRoomCellAt(index);
                             room.draw(g);
                         } else if (cell.isWalkway()) {
                             cell.draw(g);
                         }        
 
                 }
         }
 		 */
 
 		for (Player p : players) {
 			p.draw(g);
 		}
 		g.setColor(Color.BLACK);
 		g.drawRect(0, 0, numColumns * CELL_SIZE, numRows * CELL_SIZE);
 
 
 		//g.setColor(Color.BLACK);
 
 
 		g.drawString("Study", 90, 45);
 		g.drawString("Hall", 280, 45);
 		g.drawString("Lounge", 450, 45);
 		g.drawString("Library", 70, 180);
 		g.drawString("Dining Room", 450, 270);
 		g.drawString("Billiard Room", 40, 315);
 		g.drawString("Conservatory", 50, 500);
 		g.drawString("BallRoom", 260, 500);
 		g.drawString("Kitchen", 480, 500);
 
 	}
 
 	public HumanPlayer getHuman(){
 		return human;
 	}
 
 	public Card getType(){
 		return getType();
 	}
 
 
 
 
 	public ArrayList<Card> getRoomCards() {
 		return roomCards;
 	}
 
 	public void setRoomCards(ArrayList<Card> roomCards) {
 		this.roomCards = roomCards;
 	}
 
 	public ArrayList<Card> getWeapons() {
 		return weapons;
 	}
 
 	public void setWeapons(ArrayList<Card> weapons) {
 		this.weapons = weapons;
 	}
 
 	public ArrayList<Card> getPeople() {
 		return people;
 	}
 
 	public void setPeople(ArrayList<Card> people) {
 		this.people = people;
 	}
 
 	public void rollDie(){
 		Random roll = new Random();
 		die = Math.abs(roll.nextInt());
 		die = die % 6 + 1;
 		System.out.println(die + "in Board");
 	}
 
 	public int getDie(){
 		return die;
 	}
 
 
 
 	public void makeMove(Player p){
 
 		rollDie();
 		startTargets(p.currentIndex, die);
 
 		if(p instanceof ComputerPlayer){
 			humansTurn = false;
 			ArrayList<BoardCell> roomList = new ArrayList<BoardCell>();
 			ArrayList<BoardCell> list = new ArrayList<BoardCell>();
 			boolean hasRoom = false;
 			
 			for(BoardCell cell : getTargets()) {
 				if(cell.isRoom() && ((ComputerPlayer) p).getLastVisited() != ((RoomCell) cell).getInitial()){
 					roomList.add(cell);
 					hasRoom = true;
					((ComputerPlayer) p).setLastVisited(((RoomCell) cell).getInitial());
 				} else {
 					list.add(cell);
 				}
 			}
 			
 			Random generator = new Random();
 			
 			if(hasRoom){
 				int room = generator.nextInt(roomList.size());
 				p.setCurrentIndex(roomList.get(room).getIndex());
 			} else {
 				int cell = generator.nextInt(list.size());
 				p.setCurrentIndex(list.get(cell).getIndex());
 			}
 
 		} else {
 			humansTurn = true;
 			for(BoardCell cell : getTargets()) {
 				cell.setTarget(true);
 			}
 
 		}
 
 		paintComponent(super.getGraphics());
 	}
 
 	public void nextTurn(){
 
 		if (humansTurn){
 			JOptionPane.showMessageDialog(this, "You have to make a more before we can contiue");
 			System.out.println("human check");
 			return;
 		}
 
 		turnCounter = (++turnCounter % players.size());
 		makeMove(players.get(turnCounter));
 
 
 	}
 
 	public boolean isHumansTurn(){
 		return humansTurn;
 	}
 	public int getTurnCounter(){
 		return turnCounter;
 	}
 
 	private class PlayerClick implements MouseListener {
 		public void mousePressed (MouseEvent event){
 			if(humansTurn) {
 				Point click = event.getPoint();
 				System.out.println(click);
 
 				click.x = click.x/CELL_SIZE;
 				click.y = click.y/CELL_SIZE;
 				if(click.x < numColumns && click.y < numRows) {
 
 
 
 					int index = calcIndex(click.x, click.y);
 					System.out.println(index);
 					System.out.println(targets);
 					if(getCellAt(index).isTarget()) {
 						human.setCurrentIndex(index);
 
 						targets.clear();
 						humansTurn = false;
 					}
 				}
 			}
 		}
 		public void mouseClicked(MouseEvent event){}
 
 		public void mouseEntered(MouseEvent arg0) {}
 
 		public void mouseExited(MouseEvent arg0) {}
 
 
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public static void main(String [ ] args) {
 
 		//Board b = new Board("ClueLayout.csv" , "Legend.txt");
 		Board b = new Board("ClueLayout.csv", "ClueLegend.txt");
 
 		//System.out.println(b.getAdjList(505));
 		//System.out.println(b.getAdjList(b.calcIndex(15,6)));
 		//System.out.println(b.getAdjList(b.calcIndex(7,4)));
 		//System.out.println(b.getNumRows());
 		//System.out.println(b.getNumColumns());
 		//System.out.println(b.calcIndex(b.getNumColumns(), b.getNumRows()));
 		//System.out.println(b.getRoomName('K'));
 		System.out.println(b.human.getCards());
 		System.out.println(b.players.get(0).getColor());
 
 	}
 }
