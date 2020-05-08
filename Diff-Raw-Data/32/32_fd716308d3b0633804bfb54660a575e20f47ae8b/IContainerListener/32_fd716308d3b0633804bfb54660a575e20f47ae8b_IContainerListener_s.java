 package btwmods.api.player.listeners;
 
 import btwmods.api.player.events.ContainerEvent;
 
public interface IContainerListener {
 	public void containerAction(ContainerEvent event);
 	//TODO: public void containerContentsEjected(Container container, int x, int y, int z);
 }
