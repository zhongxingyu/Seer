 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.hudsonci.plugins.jna;
 
 import hudson.Functions;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import org.eclipse.hudson.jna.NativeSystemMemory;
 import org.junit.Ignore;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit Test for JnaNativeUnixSupport Utility
  * These tests can not run on a windows machine
  * 
  * @author Winston Prakash
  */
 public class UnixSupportTest {
 
     /**
      * Test of getLastError method, of class JnaNativeUnixSupport.
      */
     @Test
     public void testGetLastError() {
         if (Functions.isWindows()) return;
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         String expResult = "Unknown error: 0";
         // Since no operation done, should return error code 0
         String result = instance.getLastError();
        Assert.assertEquals(expResult, result);
         System.out.println(result);
     }
 
     /**
      * Test of getSystemMemory method, of class JnaNativeUnixSupport. 
      * Just print out the result and make sure the method works
      * Ignored because works fine on Solaris and Linux but on MAC fails with
      * the following message
      *   No suitable implementation found: os.name=Mac OS X os.arch=x86_64 sun.arch.data.model=64
      */
     @Ignore
     public void testGetSystemMemory() {
         if (Functions.isWindows()) return;
         System.out.println("getSystemMemory");
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         NativeSystemMemory result = instance.getSystemMemory();
         System.out.println("Total Swap Space: " + result.getTotalSwapSpace());
         System.out.println("Available Swap Space: " + result.getAvailableSwapSpace());
         System.out.println("Total Physical Memory: " + result.getTotalPhysicalMemory());
         System.out.println("Available Physical Memory: " + result.getAvailablePhysicalMemory());
     }
     
      /**
      * Test of canRestartJavaProcess method, of class JnaNativeUnixSupport.
      */
     @Test
     public void testCanRestartJavaProcess() {
         if (Functions.isWindows()) return;
         System.out.println("Can Restart Java Process Test");
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         boolean result = instance.canRestartJavaProcess();
         Assert.assertTrue(result);
         System.out.println("Result: " + result);
     }
 
     /**
      * Test of restartJavaProcess method, of class JnaNativeUnixSupport. This
      * test must be ignored and tested only in a controlled condition, because
      * it restarts the process again and the Test runs in an endless loop. It is
      * exists just to test once in case of library changes
      */
     @Ignore
     public void testRestartJavaProcess() {
         if (Functions.isWindows()) return;
         System.out.println("Restart Java Process Test");
         Map<String, String> properties = null;
         boolean daemonExec = false;
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         instance.restartJavaProcess(properties, daemonExec);
     }
 
     /**
      * Test of checkPamAuthentication method, of class JnaNativeUnixSupport.
      */
     @Test
     public void testCheckPamAuthentication() {
         if (Functions.isWindows()) return;
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         String result = instance.checkPamAuthentication();
         System.out.println("Check PAM Authentication Availability: " + result);
     }
 
     /**
      * Test of pamAuthenticate method, of class JnaNativeUnixSupport. Ignored,
      * because specific user name and password is needed. Remove @Ignore to test
      * in your specific environment
      */
     @Ignore
     public void testPamAuthenticate() {
         if (Functions.isWindows()) return;
         System.out.println("PAM Authenticate Test");
         String serviceName = "sshd"; // Either sshd or ssh (May be on linux it is SSH)
         String userName = "winstonp"; // Your user name
         String password = "<your password>";
         JnaNativeUnixSupport instance = new JnaNativeUnixSupport();
         String expResult = "staff"; // One of the group the user name belongs to
         Set<String> result = instance.pamAuthenticate(serviceName, userName, password);
         Iterator iter = result.iterator();
         while (iter.hasNext()) {
             System.out.println(iter.next());
         }
         Assert.assertTrue(result.contains(expResult));
     }
 }
