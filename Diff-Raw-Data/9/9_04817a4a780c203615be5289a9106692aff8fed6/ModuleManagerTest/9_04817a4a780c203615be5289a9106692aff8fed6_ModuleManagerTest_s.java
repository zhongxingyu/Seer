 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit test for ModuleManager
  */
 public class ModuleManagerTest {
 
     private class TestModule extends CommandlineModule {
         public TestModule() {
 
         }
 
 		@Override
         public void run() {
 
         }
     }
 
     @Before
     public void resetModuleManager() {
         ModuleManager.removeInstance();
         ModuleManager.createEmptyInstance();
     }
 
     @Test
     public void testLoadModuleInModuleManager() throws ManifestLoadException,
             ModuleLoadException {
         Map<String, DependencyType> desiredInputs =  new HashMap<String, DependencyType>();
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
                 desiredModules,
                 false);
         moduleToLoadData.setJarFileName("HorseSimpleGood.jar");
         ModuleManagerMetaData metaNeeded = new ModuleManagerMetaData("", "src/test/resources/modules/");
         ModuleManager m = ModuleManager.getInstance();
         m.setMetaData( metaNeeded );
         ModuleInterface module = m.loadModuleFromMetaData(moduleToLoadData);
         assertTrue( module != null );
     }
 
 
     @Test
     public void testGetInstance() throws ManifestLoadException, ModuleLoadException {
         ModuleManager m = ModuleManager.getInstance();
         assertTrue( m instanceof ModuleManager);
         ModuleManager other = ModuleManager.getInstance();
         assertTrue( m == other );
     }
 
     // expect a throw when the xml is baddly formed
     @Test( expected=Exception.class )
     public void testBadXMLModuleManagerConfig() throws ManifestLoadException, ModuleLoadException {
         String xmlPath = "src/test/resources/module_manager/testBadXMLModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // expect a throw when an xml attribute is missing.
     @Test( expected=Exception.class )
     public void testBadDataModuleManagerConfig() throws ManifestLoadException, ModuleLoadException {
         String xmlPath = "src/test/resources/module_manager/testBadDataModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // This should go through the test/resources/modules directory and get the
     // appropriate ModuleMetaData structures from jar files.
     @Test
     public void testLoadAllModuleConfigs() throws ManifestLoadException, ModuleLoadException {
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> metas = m.loadAllModuleConfigs( "src/test/resources/test_load_modules" );
         assertEquals(2,metas.size());
     }
     
     /** 
      * This test should ensure that only jar files get loaded and everything
      * else is skipped.
      */
     @Test
     public void testLoadAllModuleConfigsWhenOtherStuffInDirectory() throws ManifestLoadException, ModuleLoadException {
 		ModuleManager manager = ModuleManager.getInstance();
 		Map<String, ModuleMetaData> map = manager.loadAllModuleConfigs("src/test/resources/test_load_modules");
 		assertTrue(map.size() == 2);
     }
 
     /**
      * This test should ensure that the module loading is not recursive and wont
      * load in jar files in subdirectories of the main module directory
      */
     @Test
     public void testLoadAllModuleConfigsIsNotRecursive() throws ManifestLoadException, ModuleLoadException {
 		ModuleManager manager = ModuleManager.getInstance();
 		Map<String, ModuleMetaData> map = manager.loadAllModuleConfigs("src/test/resources/test_load_modules");
 		assertTrue(map.containsKey("com.should.nothave") == false);
     }
 
     /**
      * Most of this functionality will be tested in the ModuleLoader, however
      * this just ensures that ModuleManager gets what it needs.
      */
     @Test
     public void testLoadModuleFromMetaData() throws ManifestLoadException, ModuleLoadException {
         // not sure if we need this since it will be calling another tested
         // function.
         // TODO
         Map<String, DependencyType> desiredInputs =  new HashMap<String, DependencyType>();
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
                 desiredModules,
                 false);
		ModuleManagerMetaData data = new ModuleManagerMetaData("com.andrew.random", "src/test/resources/modules/HorseSimpleGood.jar");
 		ModuleManager manager = ModuleManager.getInstance();
 		manager.setMetaData(data);
 		manager.setCurrentModuleMetaData("com.andrew.random");
 		assertTrue(manager.loadModuleFromMetaData(moduleToLoadData) instanceof ModuleInterface);
 
     }
 
     private ModuleMetaData createEmptyModuleMetaData(String packageName, String className) {
         Map<String, DependencyType> inputTypes = new HashMap<String, DependencyType>();
         Map<String, DependencyType> moduleDepA = new HashMap<String, DependencyType>();
         ModuleMetaData a = new ModuleMetaData(
                 packageName,
                 className,
                 "2.3",
                 "2.3",
                 "icon.png",
                 "Title" + className,
                 "Andrew",
                 "0.1",
                 inputTypes,
                 moduleDepA,
                 false);
         return a;
     }
 
 
     @Test
     public void testCheckModuleDependencies() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
 
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
         m.setModuleMetaDataMap(modConfigs);
         m.checkDependencies();
         assertTrue( m.getModuleMetaDataMap().size() == 2 );
         assertTrue( m.getModuleMetaDataMap().get(a.getPackageName()) == a);
         assertTrue( m.getModuleMetaDataMap().get(b.getPackageName()) == b);
     }
 
     @Test
     public void testCheckModuleDependencyMissing() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
 
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
 
         m.setModuleMetaDataMap(modConfigs);
         m.checkDependencies();
 
         assertTrue( m.getModuleMetaDataMap().size() == 0 );
     }
 
     @Test
     public void testCheckModuleDependencyMissingWhenOptional() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
 
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.OPTIONAL);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
 
         m.setModuleMetaDataMap(modConfigs);
         m.checkDependencies();
 
         assertTrue( m.getModuleMetaDataMap().size() == 1 );
         assertTrue( m.getModuleMetaDataMap().get(a.getPackageName()) == a);
     }
 
     @Test
     public void testCheckCircularModuleDependencies() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
 
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
         
         Map<String, DependencyType> blist = new HashMap<String, DependencyType>();
         blist.put("com.test.A",DependencyType.REQUIRED);
         b.setModuleDependencies(blist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
         m.setModuleMetaDataMap(modConfigs);
         m.checkDependencies();
         assertTrue( m.getModuleMetaDataMap().size() == 2 );
         assertTrue( m.getModuleMetaDataMap().get(a.getPackageName()) == a);
         assertTrue( m.getModuleMetaDataMap().get(b.getPackageName()) == b);
     }
 
     @Test
     public void testCheckRecursiveMissingModuleDependcies() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
 
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
         
         Map<String, DependencyType> blist = new HashMap<String, DependencyType>();
         blist.put("com.test.C",DependencyType.REQUIRED);
         b.setModuleDependencies(blist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         m.setModuleMetaDataMap(modConfigs);
         m.checkDependencies();
 
         assertTrue( m.getModuleMetaDataMap().size() == 0 );
     }
 
     @Test
     public void testSetDefaultModuleValid() throws Exception {
         ModuleManager.removeInstance();
         ModuleManager.createEmptyInstance();
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modMetas = new HashMap<String,ModuleMetaData>();
         ModuleMetaData defaultModMeta = new ModuleMetaData(
                 "com.andrew.random",
                 "Horses",null, null, null, null, null, null, 
                 new HashMap<String,DependencyType>(),
                 new HashMap<String, DependencyType>(), false);
         modMetas.put("com.andrew.random", defaultModMeta );
         URL pathToModules = this.getClass().getClassLoader().getResource("modules");
         m.setMetaData(new ModuleManagerMetaData("com.andrew.random", pathToModules.toString()));
         m.setModuleMetaDataMap( modMetas );
         Method setDefault = ModuleManager.class.getDeclaredMethod("setDefaultModule",String.class);
         setDefault.setAccessible( true );
 
         setDefault.invoke(m, "com.andrew.random" );
         assertEquals("com.andrew.random", m.getMetaData().getDefaultModuleName());
     }
 
     @Test(expected=ModuleLoadException.class)
     public void testSetDefaultModuleInvalid() throws ModuleLoadException, ManifestLoadException {
         String path = "src/test/resources/module_manager/HorseyBadManifest.xml";
         String defaultModuleName = "com.example.test";
         ModuleManager.setPathToManifest(path);
         ModuleManager m = ModuleManager.getInstance();
         m.setMetaData(new ModuleManagerMetaData(defaultModuleName, "src/test/resources/modules"));
         m.testSetDefaultModule(defaultModuleName);
         
     }
 
     // default case
     @Test
     public void testSetNextModuleRequired() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
         TestModule aModule = new TestModule();
         
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
         
         // also need to set mm meta data.
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         m.setModuleMetaDataMap(modConfigs);
 		m.setCurrentModuleMetaData(a.getPackageName());
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == true);
     }
 
     // fails because current module tries to open module it didn't
     // specify in its manifest
     @Test
     public void testSetNextModuleIllegal() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
         TestModule aModule = new TestModule();
         
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         ModuleManager m = ModuleManager.getInstance();
 
         m.setModuleMetaDataMap(modConfigs);
 		m.setCurrentModuleMetaData(a.getPackageName());
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == false);
     }
 
     // passes because optional module is preset
     @Test
     public void testSetNextModuleOptionalWorks() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
         TestModule aModule = new TestModule();
         
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.OPTIONAL);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         m.setModuleMetaDataMap(modConfigs);
 		m.setCurrentModuleMetaData(a.getPackageName());
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == true);
     }
 
     // fails because optional module isn't present
     @Test
     public void testSetNextModuleOptionalFails() throws ManifestLoadException, ModuleLoadException {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         TestModule aModule = new TestModule();
         
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.OPTIONAL);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
 
         m.setModuleMetaDataMap(modConfigs);
 		m.setCurrentModuleMetaData(a.getPackageName());
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == false);
     }
 
 	/**
 	 * This test ensures that we can load resources as InputStreams
 	 * from the jars the Modules came from
 	 * @throws ModuleLoadException 
 	 * @throws ManifestLoadException 
 	 */
 	@Test
 	public void testLoadingResourcesFromDifferentModules() throws ManifestLoadException, ModuleLoadException {
 		// remove the instance so we can actually call the constructor
 		ModuleManager.removeInstance();
 		String xmlPath = "src/test/resources/module_manager/CLoaderModuleManagerManifest.xml";
         ModuleManager.setPathToManifest(xmlPath);
 		ModuleManager m = ModuleManager.getInstance();
 
 		InputStream test = m.loadResourceFromModule("resources/images/horse.jpg", "com.andrew.random" );
 
 		assertTrue( test != null );
 	}
 
 	/**
 	 * This test ensures that the ModuleManager uses the current
 	 * module for loading resources from if it is not specified
 	 * @throws ModuleLoadException 
 	 * @throws ManifestLoadException 
 	 */
 	@Test
 	public void testLoadingResourcesFromCurrentModule() throws ManifestLoadException, ModuleLoadException {
 		ModuleManager.removeInstance();
 		String xmlPath = "src/test/resources/module_manager/CLoaderModuleManagerManifest.xml";
         ModuleManager.setPathToManifest(xmlPath);
 		ModuleManager m = ModuleManager.getInstance();
 		ModuleMetaData current = m.getModuleMetaDataMap().get("com.andrew.random");
 		m.setCurrentModuleMetaData(current);
 		InputStream test = m.loadResourceFromModule("resources/images/horse.jpg");
 
 		assertTrue( test != null );
 
 	}
 
 }
