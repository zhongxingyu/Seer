 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.amazon.boot;
 
 import com.elasticgrid.config.EC2Configuration;
 import com.xerox.amazonws.ec2.EC2Exception;
 import junit.framework.Assert;
 import org.apache.commons.io.FileUtils;
 import org.testng.annotations.Test;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Bootstrapper tests.
  * @author Jerome Bernard
  */
 public class BootstrapperTest {
 
     @Test
     public void testMonitorLaunch() throws IOException, EC2Exception {
         String data = "CLUSTER_NAME=test\n" +
                 "AWS_ACCESS_ID=123456123456\n" +
                 "AWS_SECRET_KEY=123456123456\n" +
                 "AWS_EC2_AMI32=ami-bdcb2dd4\n" +
                 "AWS_EC2_AMI64=ami-fdcb2d94\n" +
                 "AWS_EC2_KEYPAIR=eg-keypair\n" +
                 "AWS_SQS_SECURED=true\n" +
                 "DROP_BUCKET=elastic-grid-drop-target";
         FileUtils.writeStringToFile(new File("/tmp/user-data"), data);
         System.setProperty("EG_HOME", System.getProperty("java.io.tmpdir"));
         new File(System.getProperty("java.io.tmpdir") + File.separatorChar + "config").mkdir();
         Bootstrapper bootstrapper = new Bootstrapper();
 
         Properties props = new Properties();
         props.load(new FileInputStream(System.getProperty("java.io.tmpdir") + "/config/eg.properties"));
         Assert.assertEquals("Wrong AWS Access ID", "123456123456", props.getProperty(EC2Configuration.AWS_ACCESS_ID));
         Assert.assertEquals("Wrong AWS Secret Key", "123456123456", props.getProperty(EC2Configuration.AWS_SECRET_KEY));
         Assert.assertEquals("Wrong AWS AMI for 32 bits", "ami-bdcb2dd4", props.getProperty(EC2Configuration.AWS_EC2_AMI32));
         Assert.assertEquals("Wrong AWS AMI for 64 bits", "ami-fdcb2d94", props.getProperty(EC2Configuration.AWS_EC2_AMI64));
         Assert.assertEquals("Wrong AWS KeyPair", "eg-keypair", props.getProperty(EC2Configuration.AWS_EC2_KEYPAIR));
         Assert.assertEquals("Wrong AWS SQS security setting", "true", props.getProperty(EC2Configuration.AWS_SQS_SECURED));
         Assert.assertEquals("Wrong EG Cluster name", "test", props.getProperty(EC2Configuration.EG_CLUSTER_NAME));
         Assert.assertEquals("Wrong EG Drop Bucket", "elastic-grid-drop-target", props.getProperty(EC2Configuration.EG_DROP_BUCKET));
     }
 
     @Test
     public void testMonitorLaunchWithMissingClusterName() throws IOException, EC2Exception {
         String data = "AWS_ACCESS_ID=123456123456\n" +
                 "AWS_SECRET_KEY=123456123456\n" +
                 "AWS_EC2_AMI32=ami-bdcb2dd4\n" +
                 "AWS_EC2_AMI64=ami-fdcb2d94\n" +
                 "AWS_EC2_KEYPAIR=eg-keypair\n" +
                 "AWS_SQS_SECURED=true\n" +
                 "DROP_BUCKET=elastic-grid-drop-target";
         FileUtils.writeStringToFile(new File("/tmp/user-data"), data);
         System.setProperty("EG_HOME", System.getProperty("java.io.tmpdir"));
         new File(System.getProperty("java.io.tmpdir") + File.separatorChar + "config").mkdir();
         Bootstrapper bootstrapper = new Bootstrapper();
 
         Properties props = new Properties();
<<<<<<< HEAD:modules/amazon-ec2-bootstrapper/src/test/java/com/elasticgrid/amazon/boot/BootstrapperTest.java
         props.load(new FileInputStream(System.getProperty("java.io.tmpdir") + "/config/eg.properties"));
=======
        props.load(new FileReader(System.getProperty("java.io.tmpdir") + "/config/eg.properties"));
>>>>>>> Fixed wrong path.:modules/amazon-ec2-bootstrapper/src/test/java/com/elasticgrid/amazon/boot/BootstrapperTest.java
         Assert.assertEquals("Wrong AWS Access ID", "123456123456", props.getProperty(EC2Configuration.AWS_ACCESS_ID));
         Assert.assertEquals("Wrong AWS Secret Key", "123456123456", props.getProperty(EC2Configuration.AWS_SECRET_KEY));
         Assert.assertEquals("Wrong AWS AMI for 32 bits", "ami-bdcb2dd4", props.getProperty(EC2Configuration.AWS_EC2_AMI32));
         Assert.assertEquals("Wrong AWS AMI for 64 bits", "ami-fdcb2d94", props.getProperty(EC2Configuration.AWS_EC2_AMI64));
         Assert.assertEquals("Wrong AWS KeyPair", "eg-keypair", props.getProperty(EC2Configuration.AWS_EC2_KEYPAIR));
         Assert.assertEquals("Wrong AWS SQS security setting", "true", props.getProperty(EC2Configuration.AWS_SQS_SECURED));
         Assert.assertEquals("Wrong EG Cluster name", "elastic-grid", props.getProperty(EC2Configuration.EG_CLUSTER_NAME));
         Assert.assertEquals("Wrong EG Drop Bucket", "elastic-grid-drop-target", props.getProperty(EC2Configuration.EG_DROP_BUCKET));
     }
 
 }
