 package net.lotrcraft.strategycraft.buildings;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockFace;
 
 public abstract class Building {
 	
 	static Castle castle;
 	
 	static Location location;
 	
 	public void build(Location l){
 		Schematic s = getSchematic();
 		byte[] bytes = s.getBlocks();
 		
 		
 		/*
 		if (blockFace == BlockFace.NORTH){
 			
 			location = new Location(l.getWorld(), l.getBlockX() - s.getWidth()/2, l.getBlockY(), l.getBlockZ() - s.getLength()/2);
 			
 		} else if (blockFace == BlockFace.EAST){
 			
 			location = new Location(l.getWorld(), l.getBlockX() + s.getWidth()/2, l.getBlockY(), l.getBlockZ() - s.getLength()/2);
 			
 		} else if (blockFace == BlockFace.WEST){
 			
 			location = new Location(l.getWorld(), l.getBlockX() - s.getWidth()/2, l.getBlockY(), l.getBlockZ() + s.getLength()/2);
 			
 		} else {
 			
 			location = new Location(l.getWorld(), l.getBlockX() + s.getWidth()/2, l.getBlockY(), l.getBlockZ() + s.getLength()/2);
 			
 		}
 		*/
 		
 		for (int y = 0; y < s.getHeight(); y++){
 			for (int x = 0; x < s.getWidth(); x++){
 				for (int z = 0; z < s.getLength(); z++){
 					l.getWorld().getBlockAt(location).setData(bytes[y + x + z]);
 				}
 			}
 		}
 		
 	}
 	
 	public void destroy(){
 		
 	}
 	
	public String getName(){
		return this.getClass().getName().substring(0, this.getClass().getName().indexOf('.'));
	}
 	
 	Schematic getSchematic(){
 		return new Schematic(getClass().getClassLoader().getResourceAsStream(getName() + ".schematic"));
 	}
 	
 	BuildingManager getBuildingManager(){
 		BuildingManager bm = new BuildingManager();
 		return bm;
 	}
 
 	public Castle getCastle() {
 		return castle;
 		
 	}
 
 	public void setCastle(Castle cstl) {
 		this.castle = cstl;
 	}
 
 
 }
