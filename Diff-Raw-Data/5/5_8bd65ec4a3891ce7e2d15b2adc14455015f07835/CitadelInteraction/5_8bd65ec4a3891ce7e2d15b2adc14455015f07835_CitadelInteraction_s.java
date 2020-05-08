 package com.github.MrTwiggy.MachineFactory.Utility;
 
 import org.bukkit.block.Block;
 
 import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
 import com.untamedears.citadel.Utility;
 import com.untamedears.citadel.entity.IReinforcement;
 import com.untamedears.citadel.entity.PlayerReinforcement;
 
 /**
  * CitadelInteraction.java
  * Purpose: Static methods for interacting with Citadel application
  *
  * @author MrTwiggy
  * @version 0.1 1/18/13
  */
 public class CitadelInteraction
 {
 	
 	/**
 	 * Returns whether the given block is reinforced by citadel
 	 */
 	public static boolean blockReinforced(Block block)
 	{
 		return MachineFactoryPlugin.CITADEL_ENABLED && Utility.isReinforced(block);
 	}
 	
 	/**
 	 * Returns the reinforcement on a block, or null if none exist
 	 */
 	public static IReinforcement getReinforcement(Block block)
 	{
 		if (MachineFactoryPlugin.CITADEL_ENABLED)
 			return Utility.getReinforcement(block);
 		else
 			return null;
 	}
 	
 	/**
 	 * Returns whether the given blocks having matching reinforcements under Citadel
 	 */
 	public static boolean reinforcementsMatch(Block block1, Block block2)
 	{
 		if (!MachineFactoryPlugin.CITADEL_ENABLED)
 			return true;
 		
 		IReinforcement block1Reinforcement = Utility.getReinforcement(block1);
 		IReinforcement block2Reinforcement = Utility.getReinforcement(block2);
 		
 		if (block1Reinforcement instanceof PlayerReinforcement)
 		{
 			String ownerName1 = ((PlayerReinforcement)block1Reinforcement).getOwner().getName();
 			if (block2Reinforcement instanceof PlayerReinforcement)
 			{
 				String ownerName2 = ((PlayerReinforcement)block2Reinforcement).getOwner().getName();
 				
 				return (ownerName1 == ownerName2); 
 			}
 			else
 			{
 				return false;
 			}
 		}
 		
 		return (block1Reinforcement == block2Reinforcement);
 	}
 
 	/**
 	 * Returns whether the given player is a member of the given reinforcement
 	 */
 	public static boolean isMemberOf(String playerName, IReinforcement reinforcement)
 	{
 		if (reinforcement == null || !MachineFactoryPlugin.CITADEL_ENABLED) // All players have access to non-existing reinforcements
 			return true;
 		
 		if (reinforcement instanceof PlayerReinforcement)
 		{
 			PlayerReinforcement playerReinforcement = (PlayerReinforcement)reinforcement;
 			
			return playerReinforcement.getOwner().isMember(playerName);
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
