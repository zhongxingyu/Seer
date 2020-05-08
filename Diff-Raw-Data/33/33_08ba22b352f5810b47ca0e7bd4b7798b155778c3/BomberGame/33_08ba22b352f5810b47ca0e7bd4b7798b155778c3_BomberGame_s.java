 package net.evtr.bombersteve;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.util.Vector;
 
 public class BomberGame {
 	public java.util.Vector<BomberPlayer> players;
 	public java.util.Vector<BomberNPC> npcs;
 	private int id;
 	public boolean bStarted;
 	
 	private Vector bottomLeft, size;
 	private DamageOwner[][] damageOwner;
 	
 	public BomberSteve plugin;
 	public World world;
 	
 	public int hardDensity, softDensity, hardSpacing, lastReady, timeUntilStart, maxNPCs, sizePerPlayer, powerRarity;
 	
 	public boolean autoScale;
 	
 	public Random random;
 	
 	public static abstract class DamageOwner {
 		public abstract String getName();
 		public abstract void addKill();
 	}
 	
 	public static class PlayerDamageOwner extends DamageOwner {
 		BomberPlayer player;
 		
 		public String getName() {
 			return player.player.getDisplayName();
 		}
 		
 		public void addKill() {
 			player.points++;
 		}
 		
 		public PlayerDamageOwner(BomberPlayer player) {
 			this.player = player;
 		}
 	}
 	
 	public static class NPCDamageOwner extends DamageOwner {
 		BomberNPC npc;
 
 		public String getName() {
 			return BomberNPC.entityType.toString();
 		}
 
 		public void addKill() {
 		}
 		
 		public NPCDamageOwner(BomberNPC npc) {
 			this.npc = npc;
 		}
 	}
 	
 	public int getID() {
 		return id;
 	}
 	
 	public static boolean isPerpindicular(int x1, int z1, int x2, int z2) {
 		return x1 == x2 || z1 == z2;
 	}
 	
 	public Material newPowerup() {
 		int n = random.nextInt(powerRarity);
 		switch ( n ) {
 			case 0:
 				return Material.RED_ROSE;
 			case 1:
 				return Material.YELLOW_FLOWER;
 			default:
 				return Material.AIR;
 		}
 	}
 	
 	public void removePlayer(BomberPlayer player) {
 		players.remove(player);
 		lastReady = getNumReady();
 	}
 	public void addPlayer(BomberPlayer player) {
 		if ( !players.contains(player) ) {
 			players.add(player);
 		}
 		player.gameID = id;
 	}
 	
 	public BomberPlayer getBombOwner(Block b) {
 		BomberPlayer owner = null;
 		for ( int i = 0; i < players.size(); i++ ) {
 			if ( players.get(i).ownsBomb(b) ) {
 				owner = players.get(i);
 				break;
 			}
 		}
 		return owner;
 	}
 	
 	public BomberPlayer getNearestLivingPlayer(Location loc) {
 		double len = -1;
 		BomberPlayer nearestPlayer = null;
 		for ( int i = 0; i < players.size(); i++ ) {
 			BomberPlayer player = players.get(i);
 			
 			if ( player.hasDied ) continue;
 			
 			double plen = player.player.getLocation().distance(loc);
 			if ( len == -1 || plen < len ) {
 				len = plen;
 				nearestPlayer = player;
 			}
 		}
 		
 		return nearestPlayer;
 	}
 	
 	public boolean hasPlayer(BomberPlayer player) {
 		return players.contains(player);
 	}
 	
 	public BomberNPC getNPC(Entity ent) {
 		for ( int i = 0; i < npcs.size(); i++ ) {
 			BomberNPC npc = npcs.get(i);
 			if ( npc.ent == ent ) {
 				return npc;
 			}
 		}
 		return null;
 	}
 	
 	public void spawnNPCs() {
 		java.util.Vector<Integer> indexes = new java.util.Vector<Integer>();
 		
 		for ( int i = npcs.size(); i < maxNPCs; i++ ) {
 			indexes.add(i);
 		}
 		while ( npcs.size() < maxNPCs ) {
 			Location loc = spawnLocation(indexes.remove(random.nextInt(indexes.size())), maxNPCs, 0.5);
			clearColumn(loc.getBlockX(), loc.getBlockZ());
 			npcs.add(new BomberNPC(loc));
 		}
 	}
 	
 	public void killNPCs() {
 		for ( int i = 0; i < npcs.size(); i++ ) {
 			BomberNPC npc = npcs.get(i);
 			world.playEffect(npc.ent.getLocation(), Effect.POTION_BREAK, 0);
 			npc.ent.remove();
 		}
 		npcs.clear();
 	}
 	
 	public int getBombY() {
 		return bottomLeft.getBlockY() + 1;
 	}
 	
 	public boolean containsBlock(int x, int y, int z) {
 		return x >= bottomLeft.getBlockX() && x < bottomLeft.getBlockX() + size.getBlockX()
 			&& y >= bottomLeft.getBlockY() && y < bottomLeft.getBlockY() + size.getBlockY()
 			&& z >= bottomLeft.getBlockZ() && z < bottomLeft.getBlockZ() + size.getBlockZ();
 	}
 	
 	public boolean containsBlock(Block b) {
 		return containsBlock(b.getX(), b.getY(), b.getZ());
 	}
 	
 	public void clearGameColumn(int x, int z) {
 		if ( containsBlock(x, getBombY(), z) ) {
 			for ( int y = getBombY(); y < bottomLeft.getBlockY() + size.getBlockY(); y++ ) {
 				Block b = world.getBlockAt(x, y, z);
 				if ( b.getType() == Material.COBBLESTONE || b.getType() == Material.BRICK ) {
 					b.setType(Material.AIR);
 				}
 			}
 		}
 	}
 	
 	
 	
 	public void ensureSafeSpawn(int x, int z) {
 		clearGameColumn(x, z);
 		clearGameColumn(x - 1, z);
 		clearGameColumn(x + 1, z);
 		clearGameColumn(x, z - 1);
 		clearGameColumn(x, z + 1);
 	}
 	
 	public void stopGame() {
 		if ( bStarted ) {
 			bStarted = false;
 			lastReady = 0;
 			removeBombs();
 			clearRegion();
 			resetPlayerInfo();
 			unReadyPlayers();
 			killNPCs();
 		}
 	}
 	
 	public Location spawnLocation(int index, int maxIndex, double radiusScalar) {
 		if ( radiusScalar > 1 ) radiusScalar = 1;
 		if ( radiusScalar < -1 ) radiusScalar = -1;
 		int widthRadius = size.getBlockX()/2,
 			heightRadius = size.getBlockZ()/2,
 			centerX = bottomLeft.getBlockX() + (int)(widthRadius),
 			centerZ = bottomLeft.getBlockZ() + (int)(heightRadius);
 		
 		Location loc = new Location(world, centerX, getBombY(), centerZ);
 		if ( maxIndex == 0 ) {
 			return loc;
 		}
 		double angle = (index/(double)maxIndex)*Math.PI*2;
 		loc.setX(centerX + Math.cos(angle)*(widthRadius - 2)*radiusScalar);
 		loc.setZ(centerZ + Math.sin(angle)*(heightRadius - 2)*radiusScalar);
 		return loc;
 	}
 	
 	public void scaleToFitPlayers() {
 		deleteRegionBlocks();
 		size.setX(Math.max(8, players.size()*sizePerPlayer));
 		size.setZ(size.getX());
 		initRegion();
 	}
 	
 	public void unReadyPlayers() {
 		for ( int i = 0; i < players.size(); i++ ) {
 			players.get(i).isReady = false;
 		}
 	}
 	
 	public void repair() {
 		removeBombs();
 		initRegion();
 	}
 	
 	public void clearColumn(int x, int z) {
 		for ( int y = getBombY(); y < bottomLeft.getBlockY() + size.getBlockY() - 1; y++ ) {
 			world.getBlockAt(x, y, z).setType(Material.AIR);
 		}
 	}
 	
 	public int getNumReady() {
 		if ( players.size() == 0 ) return 0;
 		int numReady = 0;
 		for ( int i = 0; i < players.size(); i++ ) {
 			if ( players.get(i).isReady && !players.get(i).player.isDead() ) {
 				numReady++;
 			}
 		}
 		return numReady;
 	}
 	
 	public int getReadyPercent() {
 		if ( players.size() == 0 ) return 0;
 		
 		return (int)Math.round((getNumReady()/(float)players.size())*100);
 	}
 	
 	public boolean checkForStart() {
 		int numReady = getNumReady(), readyPercent = getReadyPercent();
 		
 		if ( numReady != lastReady ) {
 			lastReady = numReady;
 			
 			sendMessage(ChatColor.YELLOW + "Now ready: " + (readyPercent >= plugin.readyMargin ? ChatColor.GREEN : ChatColor.BLUE) + numReady + ChatColor.GOLD + "/" + ChatColor.BLUE + players.size() + ChatColor.GOLD + " (" + Math.max((int)Math.ceil((players.size()*plugin.readyMargin)/100f), 2) + " required).");
 		}
 		
 		return readyPercent >= plugin.readyMargin;
 	}
 	
 	public boolean checkForEnd() {
 		if ( !bStarted ) return false;
 		int numLiving = 0;
 		BomberPlayer possibleWinner = null;
 		for ( int i = 0; i < players.size(); i++ ) {
 			if ( players.get(i).hasDied == false ) {
 				numLiving++;
 				possibleWinner = players.get(i);
 			}
 		}
 		
 		boolean gameEnded = numLiving <= 1;
 		if ( gameEnded ) {
 			if ( possibleWinner != null ) {
 				possibleWinner.wins++;
 				plugin.getServer().broadcastMessage(ChatColor.GREEN + possibleWinner.player.getDisplayName() + ChatColor.GOLD + " won game " + ChatColor.GREEN + id + ChatColor.GOLD + "!" + ChatColor.GREEN + " (" + possibleWinner.wins + (possibleWinner.wins == 0 || possibleWinner.wins != 1 ? " wins" : " win" ) + " total).");
 			} else {
 				plugin.getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + id + ChatColor.GOLD + " ended in a draw!");
 			}
 			stopGame();
 		}
 		
 		return gameEnded;
 	}
 	
 	public void sendMessage(String message) {
 		for ( int i = 0; i < players.size(); i++ ) {
 			players.get(i).player.sendMessage(message);
 		}
 	}
 	
 	public void resetPlayerInfo() {
 		for ( int i = 0; i < players.size(); i++ ) {
 			BomberPlayer player = players.get(i);
 			player.hasDied = false;
 			player.maxBombs = 1;
 			player.range = 1;
 			player.killer = null;
 		}
 	}
 	
 	public void removeBombs() {
 		for ( int i = 0; i < players.size(); i++ ) {
 			players.get(i).bombs.clear();
 		}
 	}
 	
 	public boolean bombBlock(Block b, DamageOwner player, int range) {
 		boolean continueBombing = false, setDamageOwner = true;
 		switch (b.getType() ) {
 			case FIRE:
 				continueBombing = true;
 				break;
 			case COBBLESTONE:
 				clearColumn(b.getX(), b.getZ());
 				b.setType(newPowerup());
 				setDamageOwner = false;
 				break;
 			case AIR:
 			case RED_ROSE:
 			case YELLOW_FLOWER:
 			case LONG_GRASS:
 				b.setType(Material.FIRE);
 				continueBombing = true;
 				break;
 			case TNT:
 				BomberPlayer owner = getBombOwner(b);
 				if ( owner != null ) {
 					owner.disownBomb(b);
 					setDamageOwner = false;
 					detonateBomb(new PlayerDamageOwner(owner), b.getX(), b.getZ(), owner.range);
 				} else {
 					b.setType(Material.FIRE);
 					setDamageOwner = true;
 					continueBombing = true;
 				}
 				break;
 			default:
 				setDamageOwner = false;
 		}
 		if ( setDamageOwner ) {
 			setDamageOwner(b.getX(), b.getZ(), player);
 		}
 		return continueBombing;
 	}
 	
 	public DamageOwner getDamageOwner(int x, int z) {
 		x -= bottomLeft.getBlockX();
 		z -= bottomLeft.getBlockZ();
 		if ( x >= 0 && x < size.getBlockX() && z >= 0 && z < size.getBlockZ() ) {
 			return damageOwner[x][z];
 		}
 		return null;
 	}
 	
 	public boolean isBlockSafe(int x, int z) {
 		if ( !bStarted ) return true;
 		for ( int i = 0; i < players.size(); i++ ) {
 			BomberPlayer player = players.get(i);
 			for ( int j = 0; j < player.bombs.size(); j++ ) {
 				Block b = player.bombs.get(j).block;
 				if ( (b.getX() == x && Math.abs(b.getX() - x) <= player.range)
 				  || (b.getZ() == z && Math.abs(b.getZ() - z) <= player.range) ) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public void setDamageOwner(int x, int z, DamageOwner player) {
 		x -= bottomLeft.getBlockX();
 		z -= bottomLeft.getBlockZ();
 		if ( x >= 0 && x < size.getBlockX() && z >= 0 && z < size.getBlockZ() ) {
 			damageOwner[x][z] = player;
 		}
 	}
 	
 	public void detonateBomb(DamageOwner player, int x, int z, int range) {
 		world.getBlockAt(x, getBombY(), z).setType(Material.AIR);
 		world.createExplosion(x, getBombY(), z, 0);
 		for ( int fx = x; fx <= x + range; fx++ ) {
 			if ( !bombBlock(world.getBlockAt(fx, getBombY(), z), player, range) )
 				break;
 		}
 		for ( int fx = x - 1; fx >= x - range; fx-- ) {
 			if ( !bombBlock(world.getBlockAt(fx, getBombY(), z), player, range) )
 				break;
 		}
 		
 		for ( int fz = z + 1; fz <= z + range; fz++ ) {
 			if ( !bombBlock(world.getBlockAt(x, getBombY(), fz), player, range) )
 				break;
 		}
 		for ( int fz = z - 1; fz >= z - range; fz-- ) {
 			if ( !bombBlock(world.getBlockAt(x, getBombY(), fz), player, range) )
 				break;
 		}
 	}
 	
 	public void startGame(){
 		if ( !bStarted && players.size() > 1 ) {
 			if ( autoScale ) {
 				scaleToFitPlayers();
 			}
 			addComplexity();
 			bStarted = true;
 			resetPlayerInfo();
 			
 			java.util.Vector<BomberPlayer> toRemove = new java.util.Vector<BomberPlayer>();
 			java.util.Vector<Integer> indexes = new java.util.Vector<Integer>();
 			
 			for ( int i = 0; i < players.size(); i++ ) {
 				indexes.add(i);
 			}
 			
 			for ( int i = 0; i < players.size(); i++ ) {
 				BomberPlayer player = players.get(i);
 				if ( player.player.isDead() ) {
 					toRemove.add(player);
 					sendMessage(ChatColor.RED + "Removing " + ChatColor.YELLOW + player.player.getDisplayName() + ChatColor.RED + " from the game because they are dead.");
 					continue;
 				}
 				
 				player.player.setGameMode(GameMode.SURVIVAL);
 				bringPlayer(player, indexes.remove(random.nextInt(indexes.size())));
 			}
 			for ( int i = 0; i < toRemove.size(); i++ ) {
 				players.remove(toRemove.get(i));
 			}
 			spawnNPCs();
 			timeUntilStart = 4;
 			sendMessage(ChatColor.GOLD + "Start in...");
 		}
 	}
 	
 	public Vector getBottomLeft() {
 		return bottomLeft;
 	}
 	
 	public Vector getSize() {
 		return size;
 	}
 	
 	public void deleteRegionBlocks() {
 		for ( int x = bottomLeft.getBlockX(); x <= bottomLeft.getBlockX() + size.getBlockX(); x++ ) {
 			for ( int z = bottomLeft.getBlockZ(); z <= bottomLeft.getBlockZ() + size.getBlockZ(); z++ ) {
 				for ( int y = bottomLeft.getBlockY(); y <= bottomLeft.getBlockY() + size.getBlockY(); y++ ) {
 					world.getBlockAt(x, y, z).setType(Material.AIR);
 				}
 			}
 		}
 	}
 	
 	public void deleteEverything() {
 		bStarted = false;
 		removeBombs();
 		resetPlayerInfo();
 		players.clear();
 		
 		deleteRegionBlocks();
 	}
 	
 	public void onTimer() {
 		if ( !bStarted && checkForStart() ) {
 			startGame();
 		} else if ( timeUntilStart > 0 ) {
 			timeUntilStart--;
 			if ( timeUntilStart <= 0 ) {
 				sendMessage(ChatColor.GREEN + "Begin!");
 			} else {
 				sendMessage(ChatColor.YELLOW + "..." + ChatColor.GOLD + timeUntilStart);
 			}
 		}
 		
 		if ( !bStarted || checkForEnd() || timeUntilStart > 0) return;
 		
 		for ( int x = bottomLeft.getBlockX(); x < bottomLeft.getBlockX() + size.getBlockX(); x++ ) {
 			for ( int z = bottomLeft.getBlockZ(); z < bottomLeft.getBlockZ() + size.getBlockZ(); z++ ) {
 				damageOwner[x - bottomLeft.getBlockX()][z - bottomLeft.getBlockZ()] = null;
 				Block b = world.getBlockAt(x, getBombY(), z);
 				if ( b.getType() == Material.FIRE ) {
 					b.setType(Material.AIR);
 				}
 			}
 		}
 		for ( int i = 0; i < players.size(); i++ ) {
 			BomberPlayer player = players.get(i);
 			java.util.Vector<BomberPlayer.Bomb> toRemove = new java.util.Vector<BomberPlayer.Bomb>();
 			for ( int j = 0; j < player.bombs.size(); j++ ) {
 				BomberPlayer.Bomb bomb = player.bombs.get(j);
 				bomb.timeLeft--;
 				
 				if ( bomb.timeLeft == 1 ) {
 					world.playEffect(bomb.block.getLocation(), Effect.BOW_FIRE, 0);
 				}
 				
 				if ( bomb.timeLeft <= 0 ) {
 					detonateBomb(new PlayerDamageOwner(player), bomb.block.getX(), bomb.block.getZ(), player.range);
 					toRemove.add(bomb);
 				}
 			}
 			
 			for ( int j = 0; j < toRemove.size(); j++ ) {
 				player.bombs.remove(toRemove.get(j));
 			}
 		}
 		
 		for ( int i = 0; i < npcs.size(); i++ ) {
 			BomberNPC npc = npcs.get(i);
 			BomberPlayer player = getNearestLivingPlayer(npc.ent.getLocation());
 			if ( player != null ) {
 				if ( npc.ent.getTarget() != player.player ) {
 					npc.ent.setTarget(player.player);
 					/*if ( npc.ent.getTarget() != null && npc.ent.getTarget().getType() == EntityType.PLAYER ) {
 						((Player)npc.ent.getTarget()).sendMessage(ChatColor.GREEN + "A " + ChatColor.RED + BomberNPC.entityType.toString() + ChatColor.GREEN + " is no longer targeting you!");
 					}
 					
 					player.player.sendMessage(ChatColor.GOLD + "A " + ChatColor.RED + BomberNPC.entityType.toString() + ChatColor.GOLD + " is targeting you!");
 					*/
 				}
 			}
 			/*if ( !isBlockSafe(npc.ent.getLocation().getBlockX(), npc.ent.getLocation().getBlockZ()) ) {
 				npc.ent.setVelocity(new Vector(Math.random()*10 - 5, 1, Math.random()*10 - 5));
 			}*/
 		}
 	}
 	
 	public boolean placeBomb(BomberPlayer player, int x, int y, int z) {
 		if ( bStarted && timeUntilStart <= 0 && hasPlayer(player) && x > bottomLeft.getBlockX() && x < bottomLeft.getBlockX() + size.getBlockX() - 1 && z > bottomLeft.getBlockZ() && z < bottomLeft.getBlockZ() + size.getBlockZ() - 1 ) {
 			if ( player.bombs.size() < player.maxBombs ) {
 				Block b = world.getBlockAt(x, y, z);
 				b.setType(Material.TNT);
 				player.bombs.add(new BomberPlayer.Bomb(b, 3));
 				return true;
 			}
 		}
 		player.player.sendMessage(ChatColor.RED + "Can't place bomb!");
 		return false;
 	}
 	
 	public void addColumns(int spacing) {
 		for ( int x = bottomLeft.getBlockX() + spacing; x < bottomLeft.getBlockX() + size.getBlockX(); x += spacing ) {
 			for ( int z = bottomLeft.getBlockZ() + spacing; z < bottomLeft.getBlockZ() + size.getBlockZ(); z += spacing ) {
 				for ( int y = bottomLeft.getBlockY() + 1; y < bottomLeft.getBlockY() + size.getBlockY() - 1; y++ ) {
 					world.getBlockAt(x, y, z).setType(Material.BRICK);
 				}
 			}
 		}
 	}
 	
 	
 	//Note: density is from 0 to 100
 	public void addRandomBlocks(int density, boolean soft) {
 		Random r = new Random();
 		r.setSeed(System.currentTimeMillis());
 		for ( int x = bottomLeft.getBlockX() + 1; x < bottomLeft.getBlockX() + size.getBlockX() - 1; x++ ) {
 			for ( int z = bottomLeft.getBlockZ() + 1; z < bottomLeft.getBlockZ() + size.getBlockZ() - 1; z++ ) {
 				if ( r.nextInt(99) < density ) {
 					for ( int y = bottomLeft.getBlockY() + 1; y < bottomLeft.getBlockY() + size.getBlockY() - 1; y++ ) {
 						Block b = world.getBlockAt(x, y, z);
 						if ( b.getType() != Material.BRICK ) {
 							b.setType(soft ? Material.COBBLESTONE : Material.BRICK);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void clearRegion() { 
 		for ( int x = bottomLeft.getBlockX() + 1; x < bottomLeft.getBlockX() + size.getBlockX() - 1; x++ ) {
 			for ( int z = bottomLeft.getBlockZ() + 1; z < bottomLeft.getBlockZ() + size.getBlockZ() - 1; z++ ) {
 				for ( int y = bottomLeft.getBlockY() + 1; y < bottomLeft.getBlockY() + size.getBlockY() - 1; y++ ) {
 					world.getBlockAt(x, y, z).setType(Material.AIR);
 				}
 			}
 		}
 	}
 	
 	public void initRegion() {
 		damageOwner = new DamageOwner[size.getBlockX()][size.getBlockZ()];
 		
 		boolean other = true;
 		for ( int x = bottomLeft.getBlockX(); x < bottomLeft.getBlockX() + size.getBlockX(); x++ ) {
 			other = !other;
 			for ( int z = bottomLeft.getBlockZ(); z < bottomLeft.getBlockZ() + size.getBlockZ(); z++ ) {
 				world.getBlockAt(x, bottomLeft.getBlockY(), z).setType(other ? Material.DIRT : Material.GRASS);
 				world.getBlockAt(x, bottomLeft.getBlockY() + size.getBlockY() - 1, z).setType(Material.GLASS);
 				other = !other;
 				for ( int y = bottomLeft.getBlockY() + 1; y < bottomLeft.getBlockY() + size.getBlockY() - 1; y++ ) {
 					if ( z == bottomLeft.getBlockZ()
 					  || z == bottomLeft.getBlockZ() + size.getBlockZ() - 1
 					  || x == bottomLeft.getBlockX()
 					  || x == bottomLeft.getBlockX() + size.getBlockX() - 1 ) {
 						world.getBlockAt(x, y, z).setType(Material.GLASS);
 					} else {
 						world.getBlockAt(x, y, z).setType(Material.AIR);
 					}
 				}
 			}
 		}
 	}
 	public void addComplexity() {
 		addRandomBlocks(hardDensity, false);
 		addRandomBlocks(softDensity, true);
 		addColumns(hardSpacing);
 	}
 	private Location randomSpawn(int numTries) {
 		int bx = bottomLeft.getBlockX() + 1 + random.nextInt(size.getBlockX() - 2), by =  getBombY(), bz = bottomLeft.getBlockZ() + 1 + random.nextInt(size.getBlockZ() - 2);
 		Block b = world.getBlockAt(bx, by, bz);
 		if ( b.getType() == Material.AIR || numTries >= 10 ) {
 			return new Location(world, b.getX() + 0.5, b.getY(), b.getZ() + 0.5);
 		} else {
 			return randomSpawn(numTries + 1);
 		}
 	}
 	
 	public Location randomSpawn() {
 		return randomSpawn(0);
 	}
 	
 	public void bringPlayer(BomberPlayer player, int spawnIndex) {
 		if ( hasPlayer(player)) {
 			Location loc = spawnLocation(spawnIndex, players.size(), 1);
 			ensureSafeSpawn(loc.getBlockX(), loc.getBlockZ());
 			player.player.teleport(loc);
 			
 			player.player.sendMessage(ChatColor.GOLD + "To arena " + ChatColor.GREEN + id + ChatColor.GOLD + "!");
 		}
 	}
 	
 	public BomberGame(BomberSteve plugin, int id, Vector bottomLeft, Vector size) {
 		this.plugin = plugin;
 		bStarted = false;
 		this.id = id;
 		players = new java.util.Vector<BomberPlayer>();
 		npcs = new java.util.Vector<BomberNPC>();
 		
 		this.bottomLeft = bottomLeft;
 		this.size = size;
 		
 		world = plugin.getServer().getWorld("world");
 		
 		hardSpacing = 2;
 		hardDensity = 10;
 		softDensity = 25;
 		
 		lastReady = 0;
 		timeUntilStart = 0;
 		
 		random = new Random(System.currentTimeMillis());
 		maxNPCs = 2;
 		
 		autoScale = false;
 		sizePerPlayer = 8;
 		powerRarity = 5;
 	}
 }
