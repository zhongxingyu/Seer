 package com.github.rocketsurgery;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.newdawn.slick.GameContainer;
 
 public class Level {
 
 	private List<Wire> wires;
 	private List<Node> nodes;
 	
 	private static final int MAX_RANDOM_WIDTH = 12;
 	private static final int MIN_RANDOM_WIDTH = 8;
 	
 	private static final int MAX_RANDOM_HEIGHT = 11;
 	private static final int MIN_RANDOM_HEIGHT = 7;
 	
 	private Level(List<Wire> wires, List<Node> nodes) {
 		this.wires = wires;
 		this.nodes = nodes;
 	}
 	
 	public static Level loadLevel(String title, GameContainer gc) {
 		
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		ArrayList<Wire> wires = new ArrayList<Wire>();
 
 		float xSize, ySize;
 		
 		Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"));
 		// find header for level
 		String next;
 		do {
 			next = scan.next();
 		} while (!next.equalsIgnoreCase(title));
 
 		xSize = scan.nextInt();
 		ySize = scan.nextInt();
 
 		// find "nodes" header
 		scan.next();
 
 		// parse nodes
 		while (scan.hasNextInt()) {
 			if (scan.nextInt() != nodes.size()) {
 				scan.close();
 				throw new IllegalArgumentException();
 			}
 			nodes.add(new Node(scan.nextFloat() * (gc.getWidth() - 200) / xSize + 100, scan.nextFloat() / ySize * (gc.getHeight() - 200) + 100));
 
 		}
 
 		System.out.println(gc.getWidth() + " " + gc.getHeight());
 		
 		for (Node node : nodes)
 			System.out.println(node.getCenterX() + " " + node.getCenterY());
 
 		// skip "connections" header
 		scan.next();
 
 		while (scan.hasNextInt()) {
 			// read in values from next line
 			String[] values = scan.nextLine().split(" ");
 			
 			// create wires from values
 			for (int i = 1; i < values.length; i++)
 				wires.add(new Wire(nodes.get(Integer.parseInt(values[0])), nodes.get(Integer.parseInt(values[i]))));
 		}
 
 		// close scan
 		scan.close();
 		
 		// create level and return it
 		return new Level(wires, nodes);
 	}
 	
 	public static Level generateLevel(GameContainer gc) {
 		
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		ArrayList<Wire> wires = new ArrayList<Wire>();
 		
 		Random rand = new Random();
 		
 		int maximumXResolution = Math.min((int) (gc.getWidth() / Node.sizeOnScreen), MAX_RANDOM_WIDTH);
 		int maximumYResolution = Math.min((int) (gc.getHeight() / Node.sizeOnScreen), MAX_RANDOM_HEIGHT);
 		int minimumXResolution = (int) Math.max(.66 * maximumXResolution, MIN_RANDOM_WIDTH);
 		int minimumYResolution = (int) Math.max(.66 * maximumYResolution, MIN_RANDOM_HEIGHT);
 		
 		//generate width and height between the constants. inclusive.
 		float xSize = rand.nextInt(maximumXResolution - minimumXResolution + 1) + minimumXResolution; 
		float ySize = rand.nextInt(maximumYResolution - minimumXResolution + 1) + minimumXResolution;
 		
 		int numNodes = rand.nextInt(5) + 6;// 6-10 inclusive;
 		
 		//does not account for nodes on top of nodes
 		for (int i = 0; i < numNodes; i++) {
 			
 			//generate random location more than 1 unit from the edges
 			float x = rand.nextInt((int)xSize - 2) + 1; 
 			float y = rand.nextInt((int)ySize - 2) + 1;
 			
 			x *= gc.getWidth() / xSize;
 			y *= gc.getHeight() / ySize;;
 			
 			// test to see if node already exists
 			boolean alreadyExists = false;
 			for (Node node : nodes)
 				if (node.getCenterX() == x && node.getCenterY() == y)
 					alreadyExists = true;
 			
 			if (alreadyExists) {
 				i--;
 			} else	
 				nodes.add(new Node(x, y));
 		}
 		
 		for (Node node : nodes) {
 			
 			Node otherNode;
 			
 			boolean wireDuplicated;
 			
 			do {
 				
 				//make sure randomly picked node isnt the same as the first node
 				do {
 					otherNode = nodes.get(rand.nextInt(numNodes));
 				} while (otherNode == node);
 				
 				wireDuplicated = false;
 				
 				for (Wire wire : wires) {
 					if (wire.hasEnds(node, otherNode)) {
 						wireDuplicated = true;
 						break;
 					}
 				}
 				
 			} while (wireDuplicated);
 			
 			wires.add(new Wire(node, otherNode));
 		}
 		
 		//test if it's already solved
 		boolean levelComplete = true;
 		for (int i = 0; i < wires.size() - 1; i++) {
 			for (int j = i + 1; j < wires.size(); j++) {
 				if (wires.get(i).intersects(wires.get(j))) {
 					levelComplete = false;
 					break;
 				}
 			}
 		}
 		
 		if (levelComplete)
 			return generateLevel(gc);
 		else
 			return new Level(wires, nodes); // create level and return it
 	}
 	
 	public static String[] loadLevelNames() {
 		Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"));
 
 		ArrayList<String> tempLevels = new ArrayList<>();
 
 		while (scan.hasNext()) {
 			String temp = scan.next();
 			System.out.println(temp);
 			tempLevels.add(temp);
 			scan.next();
 			scan.next();
 			scan.next();
 			while (scan.hasNextInt())
 				scan.next();
 			scan.next();
 			while (scan.hasNextInt())
 				scan.next();
 		}
 
 		scan.close();
 
 		return tempLevels.toArray(new String[0]);
 	}
 
 	public List<Wire> getWires() {
 		return this.wires;
 	}
 
 	public List<Node> getNodes() {
 		return this.nodes;
 	}
 
 }
