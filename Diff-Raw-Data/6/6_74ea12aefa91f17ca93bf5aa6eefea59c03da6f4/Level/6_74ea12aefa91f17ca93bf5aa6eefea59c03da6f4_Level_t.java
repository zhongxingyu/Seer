 package com.github.rocketsurgery;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import org.newdawn.slick.GameContainer;
 
 public class Level {
 
 	private List<Wire> wires;
 	private List<Node> nodes;
 	
 	private Level(List<Wire> wires, List<Node> nodes) {
 		this.wires = wires;
 		this.nodes = nodes;
 	}
 	
 	public static Level loadLevel(String title, GameContainer gc) {
 		try (Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"))) {
 			
 			// find header for level
 			String next;
 			do {
 				next = scan.next();
 			} while (!next.equalsIgnoreCase(title));
 			
 			float xSize = scan.nextInt();
 			float ySize = scan.nextInt();
 			
 			ArrayList<Node> nodes = new ArrayList<Node>();
 			ArrayList<Wire> wires = new ArrayList<Wire>();
 			
 			//find "nodes" header
 			do {
 				next = scan.nextLine();
 			} while (!next.equalsIgnoreCase("nodes"));
 			
 			//skip the blank line
 			scan.nextLine();
 			
 			//read in the first line of node info
 			String line = scan.nextLine();
 			
 			while (!line.equals("")) {
 				Scanner lineScanner = new Scanner (line);
 				
				lineScanner.nextInt();
 				float x = lineScanner.nextInt() * gc.getWidth() / xSize;
 				float y = lineScanner.nextInt() * gc.getHeight() / ySize;;
 				
 				nodes.add(new MultiNode(x, y));
 				
 				line = scan.nextLine();
				lineScanner.close();
 			}
 			
 			//find "connections" header
 			do {
 				next = scan.nextLine();
 			} while (!next.equalsIgnoreCase("connections"));
 			
 			//skip the blank line
 			scan.nextLine();
 			
 			//read in the first line of wire info
 			line = scan.nextLine();
 			
 			while (!line.equals("")) {
 				Scanner lineScanner = new Scanner (line);
 				
 				int index = lineScanner.nextInt();
 				
 				while (lineScanner.hasNextInt()) {
 					wires.add(new Wire(nodes.get(index),nodes.get(lineScanner.nextInt())));
 				}
 				
 				line = scan.nextLine();
				lineScanner.close();
 			}
 			
 			// create level and return it
 			return new Level(wires, nodes);
 			
 		} catch (NoSuchElementException e) {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	public List<Wire> getWires() {
 		return this.wires;
 	}
 	
 	public List<Node> getNodes() {
 		return this.nodes;
 	}
 	
 }
