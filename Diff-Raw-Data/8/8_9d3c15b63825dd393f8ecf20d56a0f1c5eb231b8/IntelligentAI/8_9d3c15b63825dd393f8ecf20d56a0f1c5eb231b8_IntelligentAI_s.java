 package testpackage.shared.ship;
 
 import testpackage.shared.ship.exceptions.InvalidInstruction;
 
 import java.lang.Character;
 import java.util.Random;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Arrays;
 
 enum Direction {
 	NORTH, EAST, WEST, SOUTH, UNKNOWN;
 
 	static Direction randomAngular(Random gen, Direction d) {
 		boolean b = gen.nextBoolean();
 		switch (d) {
 			case NORTH:
 			case SOUTH:
 				d = Direction.EAST;
 				break;
 			case EAST:
 			case WEST:
 				d = Direction.NORTH;
 				break;
 		}
 		return (b ? opposite(d) : d);
 	}
 
 	static Direction opposite(Direction d) {
 		switch (d) {
 			case NORTH:
 				return Direction.SOUTH;
 			case SOUTH:
 				return Direction.NORTH;
 			case EAST:
 				return Direction.WEST;
 			case WEST:
 				return Direction.EAST;
 			default:
 				throw new RuntimeException("can't make opposite of " + d);
 		}
 	}
 
 }
 
 class Offset {
 	int x, y;
 	Offset(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 }
 
 public class IntelligentAI extends AI {
 	Set<Direction> tried ;
 
 	Random gen;
 	Engine engine;
 	boolean randomMode;
 	boolean triedOtherDirection;
 	Direction currentDirection;
 	
 	int xwidth;
 	int ywidth;
 
 	int originalShipX, originalShipY, lastX, lastY = -1;
 
 	Map<Direction, Offset> doffset;
 
 	
 	public boolean supportsAmmo() { return false; }
 	
 	private static void Debug(String str) {
		System.err.println(str);
 	}
 
 	public IntelligentAI() {
 		doffset = new HashMap<Direction, Offset>();
 		doffset.put(Direction.NORTH, new Offset(-1, 0));
 		doffset.put(Direction.SOUTH, new Offset( 1, 0));
 		doffset.put(Direction.WEST , new Offset( 0,-1));
 		doffset.put(Direction.EAST , new Offset( 0, 1));
 		tried = new HashSet<Direction>();
 		randomMode();
 	}
 
 	public void setEngine(Engine engine) {
 		gen = new Random();
 		xwidth = engine.getxWidth();
 		ywidth = engine.getyWidth();
 		this.engine = engine;
 	}
 
 	private static boolean hitShip(char c) {
 		return Character.isUpperCase(c);
 	}
 
 	private void randomMode() {
 		tried.clear();
 		triedOtherDirection = false;
 		randomMode = true;
 		currentDirection = Direction.UNKNOWN;
 	}
 
 	@Override
 	public void playAs(int player) {
 		char hit = '\0';
 		if (randomMode) {
 			Debug("  randomMode: TRUE");
 			try {
 				lastX = gen.nextInt(xwidth);
 				lastY = gen.nextInt(ywidth);
 				
 				hit = engine.attack(player, lastX, lastY);
 
 				if (hitShip(hit)) {
 					Debug("      hit: TRUE");
 					randomMode = false;
 					originalShipX = lastX;
 					originalShipY = lastY;
 				}
 				
 			} catch (InvalidInstruction e) {
 				return;
 			}
 		} else {
 			Debug("  randomMode: FALSE");
 			Debug("    CDir: " + currentDirection);
 			if (currentDirection == Direction.UNKNOWN) {
 				assert !triedOtherDirection;
 				if (tried.size() >= 4) { randomMode(); return; }
 
 				int nextInt = 0;
 				if (tried.size() < 4) nextInt = gen.nextInt(4 - tried.size());
 
 				Set<Direction> dset = new HashSet<Direction>();
 
 				dset.addAll(new HashSet(Arrays.asList(new Direction[] {Direction.NORTH, Direction.WEST, Direction.EAST, Direction.SOUTH})));
 				dset.removeAll(tried);
 
 				Direction chosenDirection = (Direction) (dset.toArray())[nextInt];
 				tried.add(chosenDirection);
 
 				try {
 					hit = engine.attack(player, lastX+doffset.get(chosenDirection).x, lastY+doffset.get(chosenDirection).y);
 				} catch (InvalidInstruction e) {
 					Debug("      hit: COULDNT SHOOT");
 					Debug("      lastX: " + lastX);
 					Debug("      lastY: " + lastY);
 					Debug("      nextInt: " + nextInt);
 					if (tried.size() == 4) {
 						randomMode();
 					} else {
 						Debug("        " + Arrays.toString(tried.toArray()));
 					}
 					return;
 				}
 				if (hitShip(hit)) {
 					Debug("      hit: TRUE");
 					lastX = lastX+doffset.get(chosenDirection).x;
 					lastY = lastY+doffset.get(chosenDirection).y;
 					currentDirection = chosenDirection;
 				} else {
 					Debug("      hit: FALSE");
 				}
 			} else {
 				try {
 					lastX = lastX+doffset.get(currentDirection).x;
 					lastY = lastY+doffset.get(currentDirection).y;
 					hit = engine.attack(player, lastX, lastY);
 				} catch (InvalidInstruction e) {
 					Debug(e.getMessage());
 					if (triedOtherDirection)
 						randomMode();
 					else
 						tryOtherDirection();
 						//LOLOL
 					return;
 				}
 				if (!hitShip(hit)) {
 					Debug("      hit: FALSE");
 					if (triedOtherDirection) {
 						Debug("        tried other: TRUE");
 						randomMode();
 					} else {
 						Debug("        tried other: FALSE");
 						tryOtherDirection();
 					}
 				}
 			}
 			if (hit == 'X') {
				System.err.println("Shot ship");
 				randomMode();
 			}
 		}
 	}
 	
 	private void tryOtherDirection() {
		System.err.println("Trying other direction");
 		triedOtherDirection = true;
 		currentDirection = Direction.opposite(currentDirection);
 		lastX = originalShipX;
 		lastY = originalShipY;
 	}
 }
