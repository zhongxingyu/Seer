 package com.jverkamp.chesslike.actor;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.*;
 
 import com.jverkamp.chesslike.Glyph;
 import com.jverkamp.chesslike.world.World;
 
 /**
  * Anything that can move about and potentially respond to user input.
  */
 public abstract class Actor {
 	World World;
 	
 	public Point Location;
 	public Glyph Glyph;
 	
 	// 0 means player controlled. Teams have to be different to capture.
 	public int Team = 0;
 	static Color[] TeamColor = new Color[]{
 		Color.RED,
 		Color.BLUE,
 		Color.YELLOW,
 		Color.GREEN
 	};
 	
 	/**
 	 * Create a new actor.
 	 * @param world The world to place the actor in.
 	 */
 	public Actor(World world, char glyph, int team) {
 		World = world;
 		Glyph = new Glyph(glyph, TeamColor[team]);
 		Team = team;
 		
 		Location = new Point();
 		do {
 			Location.x = World.Rand.nextInt(World.Height);
 			Location.y = World.Rand.nextInt(World.Width);
 		} while(!World.getTile(Location.x, Location.y).IsWalkable);
 	}
 	
 	/**
 	 * Stringify this piece.
 	 * @return A string version.
 	 */
 	@Override
 	public String toString() {
 		return "" + getClass().getSimpleName() + "@" + super.toString().split("@")[1] + "(" + Location.x + ", " + Location.y + ")";
 	}
 	
 	/**
 	 * Move the actor to the given location if it's walkable; otherwise, stay put.
 	 * 
 	 * @param x Location x
 	 * @param y Location y
 	 * @return If we can successfully move there.
 	 */
 	public boolean go(int x, int y) {
 		// If the tile isn't walkable, don't move.
 		if (!World.getTile(x, y).IsWalkable)
 			return false;
 		
 		// Check if there's something there to capture.
 		Actor that = World.getActorAt(x, y);
 		if (this == that) that = null;
 		
 		// Empty, check if it's valid.
 		if (that == null) {
 			if (validMove(x, y)) {
 				Location.x = x;
 				Location.y = y;
 				return true;
 			} else {
 				return false;
 			}
 		} 
 		
 		// Potential enemy, try to attack it.
 		else {
 			if (Team != that.Team && validCapture(x, y)) {
 				// Remove the target piece and take its place
 				Actor a = World.removeActorAt(x, y);
 				Location.x = x;
 				Location.y = y;
 				
 				World.log(
 					(Team == 0 ? "Player" : "Computer " + Team) + "'s " + getClass().getSimpleName() + 
 					" captured " +
 					(a.Team == 0 ? "Player" : "Computer " + Team) + "'s " + a.getClass().getSimpleName()
 				);
 				
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 	
 	/**
 	 * Test if a given piece can make a valid move to the given location.
 	 * @param x The x to move to.
 	 * @param y The y to move to.
 	 * @return If the piece can be moved.
 	 */
 	public abstract boolean validMove(int x, int y);
 	
 	/**
 	 * Test if a given piece can make a valid capture to the given location.
 	 * @param x The x to capture at.
 	 * @param y The y to capture at.
 	 * @return If the piece can capture that location (assuming there is a piece there).
 	 */
 	public abstract boolean validCapture(int x, int y);
 
 	/**
 	 * Run the actor's AI.
 	 */
 	public void AI() {
 		// Get a list of all valid moves and captures
 		List<int[]> moves = new ArrayList<int[]>();
 		List<int[]> captures = new ArrayList<int[]>();
 		
		for (int x = 0; x < World.Width; x++) {
			for (int y = 0; y < World.Height; y++) {
 				if (x == Location.x && y == Location.y)
 					continue;
 				
 				Actor a = World.getActorAt(x, y);
 				
 				if (validMove(x, y)) moves.add(new int[]{x, y});
 				if (validCapture(x, y) && a != null && a.Team != Team) captures.add(new int[]{x, y});
 			}
 		}
 		
 		// If we can capture, do so
 		if (!captures.isEmpty()) {
 			int[] target = captures.get(World.Rand.nextInt(captures.size()));
 			go(target[0], target[1]);
 		}
 		
 		// If we can't, just move somewhere
 		else if (!moves.isEmpty()) {
 			int[] target = moves.get(World.Rand.nextInt(moves.size()));
 			go(target[0], target[1]);
 		}
 		
 		// If we can't even do that, just don't do anything.
 	}
 }
