 package pacman.event;
 
 import java.util.EventListener;
 
 public interface FieldListener extends EventListener{
 	
 	void pelletEaten(PelletEatenEvent event);
 
 }
 
