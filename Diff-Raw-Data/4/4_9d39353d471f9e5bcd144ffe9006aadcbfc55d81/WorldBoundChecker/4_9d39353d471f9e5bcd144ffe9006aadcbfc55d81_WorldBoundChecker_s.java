 package com.github.sashman.MC_tehboyz_survival;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 public class WorldBoundChecker implements Runnable{
 	private Server server;
 	private Coord bottom_left;
 	private Coord top_right;
 	
 	public WorldBoundChecker(Server server, int height, int width){
 		this.server = server;
 		bottom_left = new Coord((height/2)*-1, (width/2)*-1);
 		top_right = new Coord((height/2), (width/2));
 	}
 	
 	@Override
 	public void run() {
 		if(server == null)
 			return;
 		
 		Player[] players = server.getOnlinePlayers();
 		
 		if(players.length == 0) return;
 		for(Player p: players){
 			if(!inBounds(p)){
 				//TODO punishment. Should also have a pre-out-of-bounds warning distance maybe?
 				
 				//needs to know correct Y location
 				Location l = p.getLocation();
 				if(l.getX() < bottom_left.getX()) l.setX(bottom_left.getX());
 				if(l.getX() > top_right.getX()) l.setX(top_right.getX());
 				if(l.getZ() < bottom_left.getZ()) l.setZ(bottom_left.getZ());
 				if(l.getZ() > top_right.getZ()) l.setZ(top_right.getZ());
 
 				l.setY(p.getWorld().getHighestBlockYAt((int)l.getX(), (int)l.getZ()));
 				p.teleport(l);
 			}
 		}
 		
 	}
 	
 	private boolean inBounds(Player p){
 		double pX = p.getLocation().getX();
 		double pZ = p.getLocation().getZ();
 		
		if(pX > bottom_left.getX() && pX < top_right.getX() && pZ > bottom_left.getZ() && pZ < top_right.getZ())
			return true;
		return false;
 	}
 
 }
