 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
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
 
         public void init() {
 
         }
     }
 
     @Before
     public void resetModuleManager() {
         ModuleManager.removeInstance();
     }
 
 	@Test
     public void testGetInstance() {
         ModuleManager m = ModuleManager.getInstance();
         assertTrue( m instanceof ModuleManager);
         ModuleManager other = ModuleManager.getInstance();
         assertTrue( m == other );
     }
 
     // The module manager should have an instance of ModuleManagerMetaData that
     // has been correctly instantiated with the given xml file.
 	@Test
     public void testLoadModuleManagerConfig() throws ManifestLoadException {
         String xmlPath = "module_manager/testModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
         ModuleManagerMetaData shouldEqual = new ModuleManagerMetaData("com.example.test","");
         assertTrue( m.getMetaData().equals( shouldEqual ));
     }
 
     // expect a throw when the xml is baddly formed
     @Test( expected=Exception.class )
     public void testBadXMLModuleManagerConfig() throws ManifestLoadException {
         String xmlPath = "module_manager/testBadXMLModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // expect a throw when an xml attribute is missing.
     @Test( expected=Exception.class )
     public void testBadDataModuleManagerConfig() throws ManifestLoadException {
         String xmlPath = "module_manager/testBadDataModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // This should go through the test/resources/modules directory and get the
     // appropriate ModuleMetaData structures from jar files.
     @Test
     public void testLoadAllModuleConfigs() {
         ModuleManager m = ModuleManager.getInstance();
         m.loadAllModuleConfigs( "modules" );
         // TODO check that modules are loaded correctly
         fail( "Test not complete" );
     }
 
     /**
      * Most of this functionality will be tested in the ModuleLoader, however
      * this just ensures that ModuleManager gets what it needs.
      */
     @Test
     public void testLoadModuleFromMetaData() {
         // not sure if we need this since it will be calling another tested
         // function.
         // TODO
         fail( "Test not complete" );
     }
 
     private ModuleMetaData createEmptyModuleMetaData(String packageName, String className) {
         Map<InputType, DependencyType> inputTypesA = new HashMap<InputType, DependencyType>();
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
                 inputTypesA,
                 moduleDepA);
         return a;
     }
 
 
     @Test
     public void testCheckModuleDependencies() {
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
     public void testCheckModuleDependencyMissing() {
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
     public void testCheckModuleDependencyMissingWhenOptional() {
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
     public void testCheckCircularModuleDependencies() {
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
     public void testCheckRecursiveMissingModuleDependcies() {
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
     public void testRun() {
         // TODO
         fail( "Test not complete" );
     }
 
     @Test
     public void testSetDefaultModuleValid() {
        String path = "src/test/resources/module_manager/HorseyGoodManifest.xml";
         ModuleManager.setPathToManifest(path);
         ModuleManager m = ModuleManager.getInstance();
         assertEquals("com.andrew.random", m.getMetaData().getDefaultModule());
     }
 
     @Test(expected=Exception.class)
     public void testSetDefaultModuleInvalid() {
        String path = "src/test/resources/module_manager/HorseyBadManifest.xml";
         ModuleManager.setPathToManifest(path);
         ModuleManager m = ModuleManager.getInstance();
     }
 
     // default case
     @Test
     public void testSetNextModuleRequired() {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
         TestModule aModule = new TestModule();
         
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.REQUIRED);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         m.setModuleMetaDataMap(modConfigs);
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == true);
     }
 
     // fails because current module tries to open module it didn't
     // specify in its manifest
     @Test
     public void testSetNextModuleIllegal() {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         ModuleMetaData b = createEmptyModuleMetaData("com.test.B", "B");
         TestModule aModule = new TestModule();
         
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
         modConfigs.put(b.getPackageName(), b);
 
         ModuleManager m = ModuleManager.getInstance();
 
         m.setModuleMetaDataMap(modConfigs);
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == false);
     }
 
     // passes because optional module is preset
     @Test
     public void testSetNextModuleOptionalWorks() {
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
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == true);
     }
 
     // fails because optional module isn't present
     @Test
     public void testSetNextModuleOptionalFails() {
         ModuleMetaData a = createEmptyModuleMetaData("com.test.A", "A");
         TestModule aModule = new TestModule();
         
         Map<String, DependencyType> alist = new HashMap<String, DependencyType>();
         alist.put("com.test.B",DependencyType.OPTIONAL);
         a.setModuleDependencies(alist);
 
         ModuleManager m = ModuleManager.getInstance();
         Map<String,ModuleMetaData> modConfigs = new HashMap<String,ModuleMetaData>();
         modConfigs.put(a.getPackageName(), a);
 
         m.setModuleMetaDataMap(modConfigs);
         m.setCurrentModule(aModule);
         assertTrue(m.setNextModule("com.test.B") == false);
     }
 
 }
