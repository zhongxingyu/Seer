 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 /**
  * Unit test for ModuleLoader
  */
 public class ModuleLoaderTest {
 
     /**
      * This test should ensure that a proper module can be loaded given a jar
      * path to its jar file
      * @throws ModuleLoadException 
      */
     @Test
     public void testLoadModule() throws ModuleLoadException {
         Map<InputType, DependencyType> desiredInputs =  new HashMap<InputType, DependencyType>();
         Map<String, DependencyType> desiredModules =  new HashMap<String, DependencyType>();
         ModuleMetaData moduleToLoadData = new ModuleMetaData(
                 "com.andrew.random",
                 "Horses",
                 "0.0.0",
                 "0.0.0",
                 "horse.jpg",
                 "i_love_horseys",
                 "andrew demaria",
                 "0.1",
                 desiredInputs,
                 desiredModules);
        ModuleInterface m = ModuleLoader.loadModule("modules/HorseSimpleGood.jar", moduleToLoadData);
         assertTrue( m != null );
     }
 
     @Test(expected=ModuleLoadException.class)
     public void testLoadModuleWithoutProperClassImplementing() throws ModuleLoadException {
         Map<InputType, DependencyType> desiredInputs =  new HashMap<InputType, DependencyType>();
         Map<String, DependencyType> desiredModules =  new HashMap<String, DependencyType>();
         ModuleMetaData moduleToLoadData = new ModuleMetaData(
                 "com.andrew.badclass",
                 "Badgers",
                 "0.0.0",
                 "0.0.0",
                 "horse.jpg",
                 "i_love_horseys",
                 "andrew demaria",
                 "0.1",
                 desiredInputs,
                 desiredModules);
         ModuleInterface m =
            ModuleLoader.loadModule("modules/HorseBadClassNotImplementedCorrectly.jar",
                     moduleToLoadData); 
     }
 
     @Test(expected=ModuleLoadException.class)
     public void testLoadModuleWithoutExistingClass() throws ModuleLoadException {
         Map<InputType, DependencyType> desiredInputs =  new HashMap<InputType, DependencyType>();
         Map<String, DependencyType> desiredModules =  new HashMap<String, DependencyType>();
         ModuleMetaData moduleToLoadData = new ModuleMetaData(
                 "com.andrew.classnotfound",
                 "Horses",
                 "0.0.0",
                 "0.0.0",
                 "horse.jpg",
                 "i_love_horseys",
                 "andrew demaria",
                 "0.1",
                 desiredInputs,
                 desiredModules);
         ModuleInterface m =
            ModuleLoader.loadModule("modules/SnakeAteTheHorseSoThisIsBad.jar",
                     moduleToLoadData); 
     }
 
     /**
      * This test should ensure that a Module is loaded in the same context as
      * the ModuleManager so that the Singleton only has one instance.
      */
     @Test
     public void testLoadModuleKeepsSingleton() {
         // TODO
         fail( "Test not complete" );
     }
 
     /**
      * This test ensures that all the correct and needed functions are available
      * on the instance loaded.
      */
     @Test
     public void testEnsureThatAllInterfacesExistOnLoadedInstance() {
         // TODO
         fail( "Test not complete" );
     }
 }
 
 
