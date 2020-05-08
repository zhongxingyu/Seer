 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.Stack;
 
 
 public class Model {
 	private ArrayList<Enemy> d_enemies;
 	private ArrayList<Player> d_players;
 	
 	private ArrayList<Bullet> d_bullets;
 	private int d_numOfBullets;
 
 	// tileMap with blocks (true means block, false means noblock)
 	private boolean[][] d_tileMap;
 	private final int d_tileSize = 10;
 	
 	private final int d_mapWidth = 800;
 	private final int d_mapHeight = 768;
 	
 	private int d_maxTileX;
 	private int d_maxTileY;
 	
 	// this rectangle is here to temporarily draw a block when creating them
 	private Rectangle d_tmpBlock;
 	
 	// hill related
 	private final int HILL_WIDTH = 100;
 	private Rectangle d_hill;
 	private int d_points;
 	private int d_timeSinceLastHillMove;
 	private boolean d_hillTouched;
 	
 	// pickup related
 	private ArrayList<HealthPickup> d_health;
 	private final int d_maxNumOfHealth = 3;
 	private int d_timeSinceHealthPlacement;
 	
 	private ArrayList<AmmoPickup> d_ammo;
 	private final int d_maxNumOfAmmo = 3;
 	private int d_timeSinceAmmoPlacement;
 	
 	private Random d_randGenerator;
 	
 	// enemy related
 	private final int MAX_ENEMIES_PER_PLAYER = 15;
 	private final int INITIAL_ENEMIES = 10;
 	private final int TIME_BETWEEN_SPAWNS = 5000;
 	private boolean d_initial_spawned = false;;
 	private long d_timeSinceLastEnemy;
 	
 	
 	// debug
 	private boolean d_drawPaths;
 	
 	public int tileSize() {
 		return d_tileSize;
 	}
 	
 	public Model() {
 		d_drawPaths = false;
 		
 		d_enemies = new ArrayList<Enemy>();
 		d_players = new ArrayList<Player>();
 		
 		d_bullets = new ArrayList<Bullet>();
 		d_numOfBullets = 0;
 
 		d_randGenerator = new Random();
 		d_health = new ArrayList<HealthPickup>();
 		d_timeSinceHealthPlacement = d_randGenerator.nextInt(330);
 		d_ammo = new ArrayList<AmmoPickup>();
 		d_timeSinceAmmoPlacement = d_randGenerator.nextInt(330);
 		
 		d_maxTileX = d_mapWidth / d_tileSize - 1;
 		d_maxTileY = d_mapHeight / d_tileSize - 1;
 		d_tileMap = new boolean[d_mapHeight / d_tileSize][d_mapWidth / d_tileSize];
 		d_tmpBlock = null;
 		
 		int hillX = d_randGenerator.nextInt(d_maxTileX - HILL_WIDTH / d_tileSize);
 		int hillY = d_randGenerator.nextInt(d_maxTileY - HILL_WIDTH / d_tileSize);
 		d_hill = new Rectangle(hillX * d_tileSize, hillY * d_tileSize, HILL_WIDTH, HILL_WIDTH);
 		d_timeSinceLastHillMove = 0;
 		d_hillTouched = false;
 
 		try {		
 			DataInputStream file = new DataInputStream(new FileInputStream("map.dat"));
 			
 			for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 				for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 					d_tileMap[y][x] = file.readBoolean();
 				}
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 		
 		d_timeSinceLastEnemy = 0;
 	}
 	
 	public Bullet addBullet() {
 		synchronized(d_bullets) {
 			if(d_numOfBullets == d_bullets.size()) {
 				Bullet newBullet = new Bullet();
 				d_bullets.add(newBullet);
 				++d_numOfBullets;
 				return newBullet;
 			}
 			else // d_numOfBullets < d_bullets.size()
 			{
 				++d_numOfBullets;
 				return d_bullets.get(d_numOfBullets - 1);
 			}
 		}
 	}
 	
 	public void removeBullet(Bullet bullet) {
 		synchronized(d_bullets) {
 			--d_numOfBullets;
 			bullet.instantiate(d_bullets.get(d_numOfBullets));
 			d_bullets.get(d_numOfBullets).destroy();
 		}
 	}
 	
 	public void save() {
 		try {		
 			DataOutputStream file = new DataOutputStream(new FileOutputStream("map.dat"));
 			
 			for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 				for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 					file.writeBoolean(d_tileMap[y][x]);
 				}
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 	}
 	
 	public void addEnemy(Enemy enemy) {
 		d_enemies.add(enemy);
 	}
 	
 	public void addPlayer(Player player) {
 		synchronized(d_players) {
 			d_players.add(player);
 		}
 	}
 	
 	public boolean isDrawPaths() {
 		return d_drawPaths;
 	}
 
 	public void setDrawPaths(boolean drawPaths) {
 		this.d_drawPaths = drawPaths;
 	}
 
 	public ArrayList<Player> players() {
 		return d_players;
 	}
 	
 	public Rectangle tmpBlock() {
 		return d_tmpBlock;
 	}
 	
 	public void setTmpBlock(Rectangle rect) {
 		d_tmpBlock = rect;
 	}
 	
 	public Rectangle hill() {
 		return d_hill;
 	}
 	
 	public int converToTile(float num) {
 		return (int) num/d_tileSize;
 	}
 	
 	public boolean inTile(float x1, float y1, int x2, int y2) {
 		return (Math.floor(x1 / d_tileSize) == x2) && (Math.floor(y1 / d_tileSize) == y2);
 	}
 		
 	public void addBlock(int x, int y, int width, int height) {
 		// add blocks to the effected part of the map
 		for(int yIdx = y / d_tileSize; yIdx != (height + y) / d_tileSize; ++yIdx) {
 			for(int xIdx = x / d_tileSize; xIdx != (width + x) / d_tileSize; ++xIdx)
 				d_tileMap[yIdx][xIdx] = true;
 		}
 	}
 	
 	public void addEnemy() {
 		int x = d_randGenerator.nextInt(d_mapWidth - 10);
 		int y = d_randGenerator.nextInt(d_mapHeight - 10);
 		
 		boolean xOrY = d_randGenerator.nextBoolean();
 		int maxOrMin = d_randGenerator.nextInt(2);
 		
 		if(xOrY)
 			x = maxOrMin * (d_mapWidth - 10);
 		else
 			y = maxOrMin * (d_mapHeight - 10);
 				
 		synchronized(d_enemies) {
 			if(!d_tileMap[y / d_tileSize][x / d_tileSize]) {
 				d_enemies.add(new Enemy(x, y, Color.RED));
 				d_timeSinceLastEnemy = System.currentTimeMillis();
 			}
 		}
 	}
 	
 	public void step() {
 		if(!d_initial_spawned) {
 			for(int i = 0; i != INITIAL_ENEMIES; ++i)
 				addEnemy();
 			d_initial_spawned = true;
 		}
 		
 		if(d_health.size() < d_maxNumOfHealth) {
 			++d_timeSinceHealthPlacement;
 			
 			// find free spot to place health
 			while(d_timeSinceHealthPlacement > 330) {
 				int x = d_randGenerator.nextInt(d_mapWidth - 10);
 				int y = d_randGenerator.nextInt(d_mapHeight - 10);
 				
 				synchronized(d_health) {
 					if(!d_tileMap[y / d_tileSize][x / d_tileSize]) {
 						d_health.add(new HealthPickup(x, y));
 						d_timeSinceHealthPlacement = 0;
 						break;
 					}
 				}
 			}
 		}
 		
 		// Place ammo pickups
 		if(d_ammo.size() < d_maxNumOfAmmo) {
 			++d_timeSinceAmmoPlacement;
 			
 			// find free spot to place ammo
 			while(d_timeSinceAmmoPlacement > 330) {
 				int x = d_randGenerator.nextInt(d_mapWidth - 10);
 				int y = d_randGenerator.nextInt(d_mapHeight - 10);
 				
 				synchronized(d_ammo) {
 					if(!d_tileMap[y / d_tileSize][x / d_tileSize]) {
 						d_ammo.add(new AmmoPickup(x, y));
 						d_timeSinceAmmoPlacement = 0;
 						break;
 					}
 				}
 			}
 		}
 		
 		// Place enemy on random location on the edge of the map;
 		if(System.currentTimeMillis() - d_timeSinceLastEnemy > TIME_BETWEEN_SPAWNS && d_enemies.size() < MAX_ENEMIES_PER_PLAYER * d_players.size()) {
 			addEnemy();
 		}
 		
 		// Move hill if needed, 1800 steps ~ 60 seconds
 		if(d_timeSinceLastHillMove > 1800) {
 			d_hill.x = d_randGenerator.nextInt(d_maxTileX - HILL_WIDTH / d_tileSize) * d_tileSize;
 			d_hill.y = d_randGenerator.nextInt(d_maxTileY - HILL_WIDTH / d_tileSize) * d_tileSize;
 			d_timeSinceLastHillMove = 0;
 			// Tell enemies the hill moved
 			synchronized(d_enemies) {
 				for(Enemy enemy: d_enemies) {
 					enemy.forceNewPath();
 				}
 			}
 			
 			d_hillTouched = false;
 		}
 		if(d_hillTouched)
 			d_timeSinceLastHillMove++;
 
 		boolean hillCaptured = false;
 		
 		for(Player player : d_players) {
 			if(d_hill.contains(player.x(), player.y()))
 				hillCaptured = true;
 			
 			Iterator<HealthPickup> healthIter = d_health.iterator();
 			while(healthIter.hasNext()) {
 				if(healthIter.next().collision(player))
 					healthIter.remove();
 			}
 			
 			Iterator<AmmoPickup> ammoIter = d_ammo.iterator();
 			while(ammoIter.hasNext()) {
 				if(ammoIter.next().collision(player))
 					ammoIter.remove();
 			}
 		}
 		
 		for(Enemy enemy : d_enemies) {
 			enemy.step();
 			if(d_hill.contains(enemy.x(), enemy.y()))
 				hillCaptured = false;
 		}
 		
 		/*for(int index = 0; index != d_numOfBullets; ++index) {
 			Bullet bullet = d_bullets.get(index);
 		    if(bullet.step())
 			{
 				removeBullet(bullet);
 				--index;
 			}
 		}*/
 		
 		if(hillCaptured) {
 			d_hillTouched = true;
 			d_points += 1;
 		}
 		
 		synchronized(d_bullets) {
 			// check if an enemy got shot
 			for(Bullet bullet : d_bullets) {
 				boolean testThisBullet = false;
 				// check if this bullet exists
 				if(bullet.destroyed())
 					continue;
 				
 				// check if this bullet is shot by a player
 				for(Player player : d_players) {
 					if(bullet.id() == player.id()) {
 						testThisBullet = true;
 						break;
 					}
 				}
 				
 				if(testThisBullet == false)
 					continue;
 				
 				synchronized(d_enemies) {
 					Iterator<Enemy> iter = d_enemies.iterator();
 					while(iter.hasNext()) {
 						if(iter.next().checkIfShot(bullet))
 							iter.remove();
 					}
 				}
 			}
 			
 			// send packet
 			int numOfCharacters = d_players.size() + d_enemies.size();
 			int numOfBytes = 4;
 			// characters
 			numOfBytes += 4 + numOfCharacters * GameCharacter.sendSize();
 			// bullets
 			numOfBytes += 4 + d_numOfBullets * Bullet.sendSize();
 			// pickups
 			numOfBytes += 4 + d_health.size() * HealthPickup.sendSize();
 			numOfBytes += 4 + d_ammo.size() * AmmoPickup.sendSize();
 			
 			
 			// message type
 			ByteBuffer buffer = ByteBuffer.allocate(numOfBytes);
 			buffer.putInt(Message.CHARACTERS.value());
 			
 			// number of items
 			buffer.putInt(numOfCharacters);
 			buffer.putInt(d_numOfBullets);
 			buffer.putInt(d_health.size());
 			buffer.putInt(d_ammo.size());
 			
 			// characters
 			for(Player player : d_players)
 				player.addToBuffer(buffer);
 			
 			for(GameCharacter character : d_enemies)
 				character.addToBuffer(buffer);
 			
 			// bullets
 			for(int idx = 0; idx != d_numOfBullets; ++idx)
 				d_bullets.get(idx).addToBuffer(buffer);
 			
 			// pickups
 			for(HealthPickup pickup : d_health)
 				pickup.addToBuffer(buffer);
 			
 			for(AmmoPickup ammo : d_ammo)
 				ammo.addToBuffer(buffer);
 					
 			// send the buffer	
 			SpectroPolaris.server().send(buffer.array());
 					
 			d_numOfBullets = 0;
 			for(Bullet bullet : d_bullets)
 				bullet.destroy();
 		}
 	}
 	
 	//private static long time = 0;
 	
 	public void draw(Graphics2D g2d) {
 		//System.out.println(System.nanoTime() - time);
 		//time = System.nanoTime();
     	
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 			RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		g2d.setColor(Color.YELLOW);
 		g2d.fill(d_hill);
 		
 		// draw pickups
 		synchronized(d_health) {
 			for(HealthPickup health : d_health)
 				health.draw(g2d);
 		}
 		
 		synchronized(d_ammo) {
 			for(AmmoPickup ammo : d_ammo)
 				ammo.draw(g2d);
 		}
 		
 		// draw enemies
 		synchronized(d_enemies) {
 			for(GameCharacter character : d_enemies)
 				character.draw(g2d);
 		}
 		
 		// draw players
 		synchronized(d_players) {
 			
 			for(Player player : d_players)
 				player.draw(g2d);
 		}
 		
 		g2d.setColor(Color.BLACK);
 		
 		// draw a block if we are creating one through dragging
 		if(d_tmpBlock != null)
 			g2d.fill(d_tmpBlock);
 		
 		// draw map
 		for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 			for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 				if(d_tileMap[y][x]) {
 					g2d.fillRect(x * d_tileSize, y * d_tileSize, d_tileSize, d_tileSize);
 				}
 			}
 		}		
 
 		// draw right most block
 		g2d.fillRect(800, 0, 224, 768);
 		
 		/* 
 		g2d.setColor(Color.BLUE);
 		for(Point point: debug) {
 			g2d.drawRect(point.x * d_tileSize, point.y * d_tileSize, d_tileSize, d_tileSize);
 		}
 		g2d.setColor(Color.BLACK);
 		*/ 
 		
 		// draw players health etc...
 		for(int index = 0; index != d_players.size(); ++index)
 			d_players.get(index).drawUI(g2d, index);
 		
 		g2d.setColor(Color.WHITE);
 		if(SpectroPolaris.paused())
 			g2d.drawString("waiting for players", 805, 20);
 		
 		g2d.drawString("Points: " + d_points, 805, 730);
		try {
			g2d.drawString("Connect to: " + SpectroPolaris.server().ip(), 805, 750);
		} catch(NullPointerException e) {
			System.err.println("Model.draw:drawing serverip - Not yet initialized: " + e.getMessage());
			g2d.drawString("Connect to: <Initializing>", 805, 750);
		}
 	}
 	
 	public void removeGameCharacter(GameCharacter character) {
 		d_enemies.remove(character);
 	}
 
 	public void removeBlock(int x, int y) {
 		d_tileMap[y / d_tileSize][x / d_tileSize] = false;
 	}
 
 	public void removePlayer(Player player) {
 		synchronized(d_players) {
 			d_players.remove(player);
 		}
 		
 	}
 	
 	/*
 	 * Return the closest player within the given range from the given coordinates
 	 */
 	public Player closestPlayer(float x, float y, int range) {
 		Player closestPlayer =  null;
 		float distance = range;
 		
 		for(Player player: d_players) {
 			if(Math.hypot(player.x() - x, player.y() - y) < distance)
 				closestPlayer = player;
 		}
 		
 		return closestPlayer;
 	}
 	
 	/*
 	 * Bresenham's Line Algorithm
 	 * Return null if a direct line between x1, y1 and x2, y2 can be drawn. Otherwise, return a Point with the coordinates of the collision.
 	 */
 	public Point visible(float cX1, float cY1, float cX2, float cY2) {
 		boolean xLast = true;
 		
 		int x1 = (int) (cX1 / d_tileSize);
 		int y1 = (int) (cY1 / d_tileSize);
 		int x2 = (int) (cX2 / d_tileSize);
 		int y2 = (int) (cY2 / d_tileSize);
 		
 		int dx = Math.abs(x2-x1);
 		int dy = Math.abs(y2-y1);
 		int sx = x1 < x2 ? 1 : -1;
 		int sy = y1 < y2 ? 1 : -1;
 		
 		int error = dx - dy;
 		
 		while(true) {
 			if(d_tileMap[y1][x1]) {
 				if(xLast) {
 					return new Point(x1 * d_tileSize, (int)(Math.abs((cX1 - (x1 * d_tileSize)) / (dx * d_tileSize)) * dy * sy * d_tileSize + cY1));
 				} else {
 					return new Point((int)(Math.abs((cY1 - (y1 * d_tileSize)) / (dy * d_tileSize)) * dx * sx * d_tileSize + cX1), y1 * d_tileSize);
 				}
 				
 			}
 			
 			if(x1 == x2 && y1 == y2)
 				return null;
 			
 			int e2 = 2*error;
 			
 			if(e2 > -dy) {
 				error = error - dy;
 				x1 = x1 + sx;
 				xLast = true;
 			}
 			
 			if(e2 < dx) {
 				error = error + dx;
 				y1 = y1 + sy;
 				xLast = false;
 			}
 			
 			// check if points are outside of map range
 			if(x1 < 0 || y1 < 0 || x1 >= d_mapWidth / d_tileSize || y1 >= d_mapHeight / d_tileSize)
 				return null;
 		}
 	}
 	
 	/*
 	 * Find a path from COORDINATES xStartCoord, yStartCoord to COORDINATES xEndCoord, yEndCoord. If no path could be found, return null.
 	 */
 	public Stack<Node> findPath(float xStartCoord, float yStartCoord, float xEndCoord, float yEndCoord) {
 		int xStart = (int) xStartCoord / d_tileSize;
 		int yStart = (int) yStartCoord / d_tileSize;
 		int xEnd = (int) (xEndCoord / d_tileSize);
 		int yEnd = (int) (yEndCoord / d_tileSize);
 		
 		int maxY = d_maxTileY;
 		int maxX = d_maxTileX;
 		
 		Node current = new Node(xStart, yStart, null, 0, heuristicCost(xStart, yStart, xEnd, yEnd));
 		
 		if(xEnd < 0 || xEnd > maxX || yEnd < 0 || yEnd > maxY || xStart < 0 || xStart > maxX || yStart < 0 || yStart > maxY ||
 				d_tileMap[yEnd][xEnd] || d_tileMap[yStart][xStart] ) {
 			Stack<Node> path = new Stack<Node>();
 			path.push(current);
 			return path;
 		}
 
 		// Used to store which nodes have been visisted
 		boolean[][] visited = new boolean[maxY+1][maxX+1];
 		// Used to store references to nodes so they can be updated
 		Node[][] nodes =  new Node[maxY+1][maxX+1];
 		// Used to store nodes ordered by cost
 		PriorityQueue<Node> queue = new PriorityQueue<Node>();
 		
 		queue.add(current);
 		nodes[yStart][xStart] = current;
 		
 		while(!queue.isEmpty()) {
 			current = queue.poll();
 			
 			visited[current.y()][current.x()] = true;
 			
 			if(current.x() == xEnd && current.y() == yEnd) {
 				Stack<Node> path = new Stack<Node>();
 				
 				while(current != null) {
 					path.push(current);
 					current = current.getParent();
 				}
 				
 				return path;
 			}
 				
 			for(int y=-1; y<2; ++y) {
 				for(int x=-1; x<2; ++x) {
 					// Add all neighbors to the PriorityQueue that haven't been visited and aren't blocked
 					int newX = current.x() + x;
 					int newY = current.y() + y;
 					// Check if the new node is still within the map, is not blocked and not already visited
 					if(newX >= 0 && newX <= maxX && newY >= 0 && newY <= maxY && !d_tileMap[newY][newX] && !visited[newY][newX]) {
 						// If a diagonal movement, check for nearby corners which wouldn't allow this move
 						if((x!=0 && y!=0) && (d_tileMap[newY][current.x()] || d_tileMap[current.y()][newX]))
 							continue;
 						
 						float pathCost = pathCost(newX, newY, current);
 						float heuristicCost = heuristicCost(newX, newY, xEnd, yEnd);
 						
 						if(nodes[newY][newX] == null) {
 							// The node has not been visited yet
 							Node newNode = new Node(newX, newY, current, pathCost, heuristicCost);
 							queue.add(newNode);
 							nodes[newY][newX] = newNode;
 							
 							
 						} else if(pathCost < nodes[newY][newX].getPathCost()) {
 							// The node has been visited but this path is better
 							queue.remove(nodes[newY][newX]);
 							nodes[newY][newX].setParent(current);
 							nodes[newY][newX].setPathCost(pathCost);
 							nodes[newY][newX].setHeuristicCost(heuristicCost);
 							queue.add(nodes[newY][newX]);
 						}
 					}
 				}
 			}
 		}
 		
 		
 		// No path could be found
 		System.err.println("No path could be found: from " + xStart + ", " + yStart + " to " + xEnd + ", " + yEnd);
 		Stack<Node> path = new Stack<Node>();
 		path.push(nodes[yStart][xStart]);
 		return path;		
 	}
 	
 	private float heuristicCost(int newX, int newY, int xEnd, int yEnd) {
 		return (float)  Math.hypot(newX - xEnd, newY - yEnd);
 	}
 	
 	private float pathCost(int newX, int newY, Node current) {
 		return (float) (current.getPathCost() + Math.hypot(current.x() - newX, current.y() - newY));
 	}
 }
