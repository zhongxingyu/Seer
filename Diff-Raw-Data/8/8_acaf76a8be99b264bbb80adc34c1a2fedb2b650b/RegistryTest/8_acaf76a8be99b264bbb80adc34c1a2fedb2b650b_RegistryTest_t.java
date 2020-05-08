 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 import junit.framework.TestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.HostProperties;
 import org.fusesource.meshkeeper.MavenTestSupport;
 import org.fusesource.meshkeeper.MeshContainer;
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.MeshContainer.Hostable;
 import org.fusesource.meshkeeper.MeshContainer.MeshContainerContext;
 
 /**
  * RegistryTest
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class RegistryTest extends TestCase {
 
     private Log LOG = LogFactory.getLog(RegistryTest.class);
     MeshKeeper meshKeeper;
 
     protected void setUp() throws Exception {
         //Use MavenTestSupport to create MeshKeeper under target directory:
         meshKeeper = MavenTestSupport.createMeshKeeper(RegistryTest.class.getSimpleName());
     }
 
     protected void tearDown() throws Exception {
         if (meshKeeper != null) {
             meshKeeper.destroy();
             meshKeeper = null;
         }
     }
 
     @SuppressWarnings("serial")
     public static class Greeter implements Hostable {
         private transient Log LOG = LogFactory.getLog(Greeter.class);
         private transient MeshKeeper mk;
         public String name;
 
         //Cycle through the /greetings node looking for greetings:
         public void checkWhosThere() throws Exception {
             for (int i = 1; i <= 2; i++) {
                 String greeter = mk.registry().getRegistryObject("/greetings/greeter" + i);
                 if (greeter != null) {
                     LOG.info("greeter" + i + " says: " + greeter);
                 } else {
                     LOG.info("greeter" + i + " not there!");
                 }
             }
         }
 
         //////////////////////////////////////////////////////////////////////////////////////////
         //Hostable
         public void initialize(MeshContainerContext ctx) throws Exception {
             LOG = LogFactory.getLog(Greeter.class + "." + name);
             //Note that we pick up MeshKeeper from the MeshContainerContext:
             mk = ctx.getContainerMeshKeeper();
 
             //Let's add our greeting:
             mk.registry().addRegistryObject("/greetings/" + name, false, new String("Greetings from " + name + "!"));
         }
 
         public void destroy(MeshContainerContext ctx) throws Exception {
             //Let's remove our greeting when we're destroyed:
             mk.registry().removeRegistryData("/greetings/" + name, true);
         }
         //Hostable
         //////////////////////////////////////////////////////////////////////////////////////////
 
     }
 
     public void testGreetings() throws Exception {
         meshKeeper.launcher().waitForAvailableAgents(60000);
         HostProperties[] hosts = meshKeeper.launcher().getAvailableAgents();
 
         MeshContainer c1 = meshKeeper.launcher().launchMeshContainer(hosts[0].getAgentId());
         MeshContainer c2 = meshKeeper.launcher().launchMeshContainer((hosts.length > 1 ? hosts[1] : hosts[0]).getAgentId());
 
         Greeter g1 = new Greeter();
         g1.name = "greeter1";
 
         Greeter g2 = new Greeter();
         g2.name = "greeter2";
 
         //Let's host them in their containers,
         //Note that we replace the actual references
         //with their proxy counterparts.
         g1 = c1.host(g1.name, g1);
         g2 = c2.host(g2.name, g2);
         
         LOG.info("Checking greeter1 greetings");
         g1.checkWhosThere();
         
         LOG.info("Checking greeter2 greetings");
         g2.checkWhosThere();
         
         LOG.info("Unhosting greeter2");
         c2.unhost("greeter2");
         
         LOG.info("Checking greeter1 greetings, greeter2 shouldn't be there");
         g1.checkWhosThere();
         
     }
 }
