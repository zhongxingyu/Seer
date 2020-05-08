 package edu.mines.acmX.exhibit.module_manager;
 
 import processing.core.*;
 
 /**
  * TODO add better documenting
  * This class should be inherited from when building your own module. Module
  * manager will deal with running an instance of this class.
  */
 
 public abstract class Module extends PApplet {
 
     // just a slim layer for interfacing with a modulemanager and will return a boolean on whether the requested module can be run.
     protected final boolean setNextModuleToLoad( String moduleName ) {
         // TODO set nextModule in ModuleManager
     }
 
     // TODO
     // layer to query modulemanager
     
     // TODO
     // be able to ask about its own or other module metadatas
 
 
 
 }
 
 
