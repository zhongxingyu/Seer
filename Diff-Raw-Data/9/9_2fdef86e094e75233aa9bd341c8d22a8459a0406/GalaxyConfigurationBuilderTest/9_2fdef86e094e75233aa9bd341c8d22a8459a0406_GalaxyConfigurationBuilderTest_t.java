 package org.mule.galaxy.mule1;
 
 
 import org.mule.galaxy.config.GalaxyConfigurationBuilder;
 import org.mule.galaxy.test.AbstractAtomTest;
 import org.mule.umo.manager.UMOManager;
 import org.mule.umo.model.UMOModel;
 
 public class GalaxyConfigurationBuilderTest extends AbstractAtomTest
 {
     private UMOManager manager;
 
     public void testMuleConfig() throws Exception
     {
 
         String configURL = "http://admin:admin@localhost:9002/api/registry?q=select artifact where mule.id = 'Hello_Sample'";
 
         GalaxyConfigurationBuilder builder = new GalaxyConfigurationBuilder();
 
         manager = builder.configure(configURL);
 
         //Assert components
         UMOModel model = (UMOModel)manager.getModels().get("main");
         assertNotNull(model);
         assertNotNull(model.getComponent("GreeterUMO"));
         assertNotNull(model.getComponent("ChitChatUMO"));
 
     }
 
 
     @Override
     protected void tearDown() throws Exception
     {
         try
         {
             if(manager!=null)
             {
                 manager.dispose();
             }
         }
         finally
         {
             super.tearDown();
         }
     }
 
     protected String getWebappDirectory()
     {
        return "../../web/src/main/webapp";
     }
 }
