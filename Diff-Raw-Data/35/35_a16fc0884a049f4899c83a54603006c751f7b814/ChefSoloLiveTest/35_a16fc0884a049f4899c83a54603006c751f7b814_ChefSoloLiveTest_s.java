 package io.brooklyn.provisioners.chef.solo;
 
 import io.brooklyn.provisioners.chef.solo.entities.MySqlChefSoloSshDriver;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import brooklyn.entity.AbstractEc2LiveTest;
 import brooklyn.entity.basic.SoftwareProcess;
 import brooklyn.entity.database.mysql.MySqlDriver;
 import brooklyn.entity.database.mysql.MySqlNode;
 import brooklyn.entity.drivers.EntityDriverManager;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.location.Location;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.test.EntityTestUtils;
 
 import com.google.common.collect.ImmutableList;
 
 @Test
 public class ChefSoloLiveTest extends AbstractEc2LiveTest {
 
     public static final Logger LOG = LoggerFactory.getLogger(ChefSoloLiveTest.class);
 
     private MySqlNode mysql;
 
     @Override
     protected void doTest(Location loc) throws Exception {
         EntityDriverManager entityDriverManager = ctx.getEntityDriverManager();
         entityDriverManager.registerDriver(MySqlDriver.class, SshMachineLocation.class, MySqlChefSoloSshDriver.class);
 
         mysql = app.createAndManageChild(BasicEntitySpec.newInstance(MySqlNode.class));
         app.start(ImmutableList.of(loc));
 
         EntityTestUtils.assertAttributeEqualsEventually(mysql, SoftwareProcess.SERVICE_UP, true);
     }
 
 }
