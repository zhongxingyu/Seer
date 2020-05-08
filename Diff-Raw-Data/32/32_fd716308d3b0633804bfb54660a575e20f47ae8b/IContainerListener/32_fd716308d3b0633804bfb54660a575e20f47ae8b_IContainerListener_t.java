 package btwmods.api.player.listeners;
 
import java.util.EventListener;
 import btwmods.api.player.events.ContainerEvent;
 
public interface IContainerListener extends EventListener {
 	public void containerAction(ContainerEvent event);
 	//TODO: public void containerContentsEjected(Container container, int x, int y, int z);
 }
