 package me.cvenomz.OwnBlocks;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.nijiko.coelho.iConomy.system.Account;
 
 public class OwnBlocksBlockListener extends BlockListener{
 	
 	private OwnBlocks pluginRef;
 	//private Map<Location, Player> blockPending;
 	private Map<OBBlock, String> database;
 	Logger log;
 	boolean debug;
 	
 	OwnBlocksBlockListener(OwnBlocks ob)
 	{
 		pluginRef = ob;
 		database = pluginRef.database;
 		log = pluginRef.log;
 		debug = pluginRef.debug;
 	}
 	
 	public void onBlockBreak(BlockBreakEvent e)
 	{	
 		Block b = e.getBlock();
 		OBBlock obb = new OBBlock(b);
 		String player = e.getPlayer().getName();
 		//listAll();
 		//Is block protected
 		if (database.containsKey(obb))
 		{
 			//Is player NOT the owner of the block?
 			if (!database.get(obb).equals(player))
 			{
 				if (pluginRef.permissions.has(e.getPlayer(), "OwnBlocks.ignoreOwnership")) //Is player a mod/OP
 					database.remove(obb);		//break block
 				else							//Player is not a mod/OP
 					e.setCancelled(true);		//dont break block, because they are not an OP, nor are they the owner.
 			}
 			else								//Player is owner of block
 				database.remove(obb);			//break block, because they are the owner
 		}
 	}
 	
 	public void onBlockPlace(BlockPlaceEvent e)
 	{
 		//Check if player is in the 'active' arraylist
 		if (pluginRef.activatedPlayers.contains(e.getPlayer().getName()))
 		{
 			//Check if block ID is excluded
 			//log.info(e.getPlayer().getName() + " placing " + e.getBlockPlaced().getTypeId());
 			if (!pluginRef.exclude.contains(e.getBlockPlaced().getTypeId()))
 			{
 				//log.info("Stored");
 				
 				//check iConomy
 				if (pluginRef.useiConomy())
 				{
 					debugMessage("Use iConomy == true");
 					Account account = pluginRef.iConomy.getBank().getAccount(e.getPlayer().getName());
 					if (account.getBalance() >= pluginRef.getRate())
 					{
 						account.subtract(pluginRef.getRate());
 						OBBlock obb = new OBBlock(e.getBlockPlaced());
 						database.put(obb, e.getPlayer().getName());
 						debugMessage("acct. balance >= Rate, iConomy block placed");
 					}
					else
					{
						debugMessage("acct. funds insufficient, block not placed");
						e.setCancelled(true);
					}
 				}
 				else
 				{
 					OBBlock obb = new OBBlock(e.getBlockPlaced());
 					database.put(obb, e.getPlayer().getName());
 					debugMessage("Block placed - not with iConomy");
 				}
 			}
 		}
 	}
 	
 	
 	private void listAll()
 	{
 		log.info(database.keySet().toString());
 	}
 	
 	private void debugMessage(String str)
 	{
 		pluginRef.debugMessage(str);
 	}
 
 }
