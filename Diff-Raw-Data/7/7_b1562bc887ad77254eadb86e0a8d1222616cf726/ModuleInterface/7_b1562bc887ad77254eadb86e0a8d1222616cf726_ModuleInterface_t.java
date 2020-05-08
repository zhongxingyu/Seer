 package edu.mines.acmX.exhibit.module_manager;
 
import java.util.concurrent.CountDownLatch;

 /**
  * Interface that all Modules must implement in some way or another.
  * Contains the functions that are needed to properly interact with
  * Module Manager.
  *
  * @author	Andrew DeMaria
  * @author	Austin Diviness
  */
 public interface ModuleInterface {
 
     public boolean setNextModuleToLoad( String moduleName );
 
 	public void init(CountDownLatch waitForModule);
 	
 	public void execute();
 	
 	public void finishExecution();
 
 }
 
 
