 package sadmean.mc.nuclearDevice;
 
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 public class nuclearBomb {
 
 	//parts
 	Location eventLocation; //this is the location we are given by the constructor. It should be a diamond block next to a lever
 	Location centerDiamondBlockLocation; // This is where the epicenter of our explosion should be. We will get that location after we know where the gold caps are
 	Location bottomGoldBlockLocation; //the bottom gold cap
 	Location topGoldBlockLocation; //the top gold cap
 	List<Entity> ents; //the list of Entitys found in range of our explosion
 	Entity ent; //value used to manipulate a single Entity in the list of entitys
 	int totalDiamondBlocks = 0; //our total diamond blocks. 
 	int yield; //explosive yield
 	boolean lowerCap = false; //does this have a lower cap?
 	boolean upperCap = false; //does this have an upper cap
 	public Player player; //value used to manipulate our list of players
 	
 	//these values mark what block types are cap/exploder
 	int capTypeID = nuclearDevice.capTypeID; //gold by default (this value is what makes the caps)
 	int payloadTypeID = nuclearDevice.payloadTypeID; //diamond (this value is what adds to yield)
 	
 	//Constructor
 	public nuclearBomb(Location givenLocation) {
 		nuclearDevice.log_It("fine", "attempting constructor");
 		double currentY;
 		Block holderBlock;
 		eventLocation = givenLocation;
 		holderBlock = eventLocation.getBlock();
 		
 		if(holderBlock.getTypeId() == payloadTypeID) {
 			//look for diamond at our eventLocation
 		
 			currentY = eventLocation.getY();
 			Location newLocation = new Location(eventLocation.getWorld(), eventLocation.getX(), currentY, eventLocation.getZ());		
 			holderBlock = newLocation.getBlock();
 			totalDiamondBlocks++;
 			nuclearDevice.log_It("fine", "checked FIRST BLOCK type is " + Integer.toString(holderBlock.getTypeId()));
 			
 			//we found our payload, lets check to see how many we have below eventLocation
 			while(holderBlock.getTypeId() == payloadTypeID) {
 
 				currentY = currentY - 1;
 				newLocation = new Location(eventLocation.getWorld(), eventLocation.getX(), currentY, eventLocation.getZ());		
 				holderBlock = newLocation.getBlock();
 				totalDiamondBlocks++;			
 			}
 		
 			//make sure we have gold caps, nigger!
 			if(holderBlock.getTypeId() == capTypeID) lowerCap = true; bottomGoldBlockLocation = holderBlock.getLocation();//we're out of that while loop. lets see if that was a cap
 			
 			nuclearDevice.log_It("fine", "checked lowerCap type is " + Integer.toString(holderBlock.getTypeId()) + " but our bool is " + Boolean.toString(lowerCap));
 		
 		
 			currentY = eventLocation.getY();
 			newLocation = new Location(eventLocation.getWorld(), eventLocation.getX(), currentY, eventLocation.getZ());		
 			holderBlock = newLocation.getBlock();
 			nuclearDevice.log_It("fine", "reset checker");
 			nuclearDevice.log_It("fine", "reset blockholder type is " + Integer.toString(holderBlock.getTypeId()));
 			//check blocks above our switch
 			while(holderBlock.getTypeId() == payloadTypeID) {
 
 				currentY = currentY + 1;
 				newLocation = new Location(eventLocation.getWorld(), eventLocation.getX(), currentY, eventLocation.getZ());		
 				holderBlock = newLocation.getBlock();
 				totalDiamondBlocks++;			
 			}
 		if(holderBlock.getTypeId() == capTypeID) upperCap = true; topGoldBlockLocation = holderBlock.getLocation();
 		nuclearDevice.log_It("fine", "checked upperCap type is " + Integer.toString(holderBlock.getTypeId()) + " but our bool is " + Boolean.toString(lowerCap));
 		
 		//at this point, we have our total diamond blocks and gold caps. 
 		//lets get the explosive yield
 		
 		yield = totalDiamondBlocks * 9;
 		yield = totalDiamondBlocks + yield; //add total diamond blocks back into the yield, so each block is worth more then the last
 		
 		//get our center diamondblock by using our top and bottom gold blocks
 		
 		double diamondY = (topGoldBlockLocation.getY() + bottomGoldBlockLocation.getY()) / 2;
 		centerDiamondBlockLocation = eventLocation;
 		centerDiamondBlockLocation.setY(diamondY);
 		
 		
 		} else {
 		
 			nuclearDevice.log_It("fine", "This is not a valid bomb design");
 	
 		}
 	}
 	
 	public void explode(int time) {
		World explodeWorld = eventLocation.getWorld();
 		nuclearDevice.log_It("finest", "attempting exploson command");
 		nuclearDevice.log_It("fine", "world name is " + explodeWorld.getName());
 
 		if (lowerCap && upperCap) {
 			
 			if (nuclearDevice.useSimulatedExplosion) {
 				simulatedExplosion explosion = new simulatedExplosion(centerDiamondBlockLocation, yield);
 				if(explosion.explode()) {
 					nuclearDevice.log_It("info", "Bomb armed on " + explodeWorld.getName() + ". DUN DUN DUN.");
 				} else {
 					nuclearDevice.log_It("warning", "simulated explosion failed");
 				}
 			}
 			else {
 			
 				nuclearDevice.log_It("info", "Bomb armed on " + explodeWorld.getName() + ". DUN DUN DUN.");
 				explodeWorld.createExplosion(centerDiamondBlockLocation, yield);
 
 				//This is where shit gets weird. We have to create an entity at the epicenter and then use that to collect ALL entites nearby
 				//Then we check to see what these entites are. IF they are items. WE remove() THEM!
 				LivingEntity checkCreeper = explodeWorld.spawnCreature(centerDiamondBlockLocation, CreatureType.CREEPER);
 				ents = checkCreeper.getNearbyEntities(yield, yield, yield);
 			
 				int entNumber = 0;
 				while (entNumber < ents.size()) {
 					ent = ents.get(entNumber);
 					if (ent instanceof org.bukkit.entity.Item) {
 						ent.remove();
 					}
 					entNumber++;
 				}
 
 				//and don't forget to remove the creeper!
 				checkCreeper.remove();
 			}
 			//warn all players
 				int Xmessage = eventLocation.getBlockX();
 				int Zmessage = eventLocation.getBlockZ();
 				int playerNumber = 0;
 				List<Player> players = explodeWorld.getPlayers();
 				while (playerNumber < players.size()) {
 					player = players.get(playerNumber);
 					player.sendMessage("WARNING: NUKE DETECTED AT " + Integer.toString(Xmessage) + " BY " + Integer.toString(Zmessage));
 					playerNumber++;
 				}
			
 				
 			nuclearDevice.log_It("info", "yield is " + Integer.toString(yield));
 		}
 		
 		
 
 	}
 }
