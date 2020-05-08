 package org.javaz.util.test;
 
 import junit.framework.Assert;
 import org.javaz.util.UpdateableAuthPropertyUtil;
 import org.javaz.util.UpdateableFilePropertyUtil;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FileWriter;
 
 /**
  *
  */
 public class UpdateableTest
 {
     @Test
     public void testUpdateable() throws Exception
     {
         File file = File.createTempFile("UpdateableTest", "properties");
         file.deleteOnExit();
 
         FileWriter writer = new FileWriter(file, false);
         writer.write("a.b=x\n");
         writer.write("a.c=x");
         writer.close();
 
         UpdateableFilePropertyUtil instance = UpdateableFilePropertyUtil.getInstance(file.getAbsolutePath());
         Assert.assertEquals(instance.getProperty("a.b"), "x");
         Assert.assertEquals(instance.getProperty("a.c"), "x");
 
         writer = new FileWriter(file, false);
         writer.write("a.b=y\n");
         writer.write("o1.o1=*.*\n");
         writer.write("o2.o2=mm.*\n");
         writer.write("o3.o3=mm.xx\n");
         writer.close();
 
         Thread.sleep(2 * UpdateableFilePropertyUtil.FILE_WATCH_UPDATE_PERIOD);
 
         Assert.assertEquals(instance.getProperty("a.b"), "y");
         Assert.assertNull(instance.getProperty("a.c"));
 
         UpdateableAuthPropertyUtil auth = UpdateableAuthPropertyUtil.getInstance(file.getAbsolutePath());
         auth.setUserPasswordSplitExpression(auth.getUserPasswordSplitExpression());
         auth.setMethodsSplitExpression(auth.getMethodsSplitExpression());
         Assert.assertTrue(auth.isAuthorized("a", "b", "y"));
         Assert.assertTrue(auth.isAuthorized("o1", "o1", "y"));
         Assert.assertTrue(auth.isAuthorized("o2", "o2", "mm.xxxx"));
         Assert.assertTrue(auth.isAuthorized("o3", "o3", "mm.xx"));
 
         writer = new FileWriter(file, false);
         writer.write("ab=y");
         writer.write("cd=o");
         writer.write("cdsdsd");
         writer.close();
 
         Thread.sleep(2 * UpdateableFilePropertyUtil.FILE_WATCH_UPDATE_PERIOD);
 
         Assert.assertFalse(auth.isAuthorized("a", "b", "y"));
 
 
     }
 }
