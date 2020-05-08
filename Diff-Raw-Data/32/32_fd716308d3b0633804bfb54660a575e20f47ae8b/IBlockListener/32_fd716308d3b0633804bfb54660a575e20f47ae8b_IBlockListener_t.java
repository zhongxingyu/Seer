 package btwmods.api.world.listeners;
 
import java.util.EventListener;
 import btwmods.api.world.events.BlockEvent;
 
public interface IBlockListener extends EventListener {
 	public void blockAction(BlockEvent event);
 }
