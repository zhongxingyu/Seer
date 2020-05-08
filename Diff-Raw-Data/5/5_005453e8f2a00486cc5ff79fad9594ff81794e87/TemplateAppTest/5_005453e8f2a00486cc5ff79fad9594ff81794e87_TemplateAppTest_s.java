 package org.melati.app.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 
 import org.melati.Melati;
 import org.melati.app.InvalidArgumentsException;
 import org.melati.app.TemplateApp;
 import org.melati.app.UnhandledExceptionException;
 import org.melati.util.ConfigException;
 import org.melati.util.MelatiConfigurationException;
 
 import junit.framework.TestCase;
 
 /**
  * Generated code for the test suite <b>TemplateAppTest</b> located at
  * <i>/melati/src/test/java/org/melati/app/test/TemplateAppTest.testsuite</i>.
  * 
  */
 public class TemplateAppTest extends TestCase {
   /**
    * Constructor for TemplateAppTest.
    * 
    * @param name
    */
   public TemplateAppTest(String name) {
     super(name);
   }
 
   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
   }
 
   /**
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     //System.gc();
   }
 
   /**
    * @see org.melati.app.TemplateApp#init(String[])
    */
   public void testInit() throws Exception {
     TemplateApp ta = new TemplateApp();
     String[] args = { "appjunit", "user", "0", "method", "field", "value" };
     Melati m = ta.init(args);
 
     assertEquals("appjunit", m.getDatabase().getName());
     Hashtable f = (Hashtable)m.getTemplateContext().get("Form");
     assertEquals("value", f.get("field"));
   }
 
   /**
    * @see org.melati.app.TemplateApp#init(String[])
    */
   public void testInitWithUnmatcheArgs0() throws Exception {
     TemplateApp ta = new TemplateApp();
     String[] args = { "appjunit", "user", "0", "method", "field", "value",
         "unmatched" };
     try {
       ta.init(args);
       fail("Should have bombed");
     } catch (InvalidArgumentsException e) {
       e = null;
     }
   }
 
   /**
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMain() throws Exception {
     String fileName = "t.tmp";
     String[] args = { "appjunit", "user", "0",
         "org/melati/app/TemplateApp", "field", "value", "-o", fileName };
     TemplateApp.main(args);
     String output = "";
     File fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete(); 
     assertEquals("Hello _guest_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : user (from the data structure definition) "  +
             "Object   : _guest_ " + 
             "Troid    : 0 " + 
             "Method   : org/melati/app/TemplateApp " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator" +  
             "Form settings" + 
             "=============" + 
             "  field value", output);
   }
   
   /**
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMainOneArg() throws Exception {
     String fileName = "tttt.tmp";
     String[] args = { "appjunit", "-o", fileName };
     TemplateApp it = new TemplateApp();
     it.run(args);
     String output = "";
     File fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( 
         new InputStreamReader(
             new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete();      
     System.err.println(output);    
     assertEquals("Hello _guest_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : null "  +
             "Object   : null " + 
             "Troid    : null " + 
             "Method   : null " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator",output);
   }
 
   /**
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMainTwoArgs() throws Exception {
     String fileName = "t25555.tmp";
     String[] args = { "appjunit", "user", "-o", fileName };
     TemplateApp it = new TemplateApp();
     try { 
       it.run(args);
       fail("Should have blown up");
     } catch (UnhandledExceptionException e) {
       e.printStackTrace();
      assertEquals("org.melati.template.NotFoundException: Could not find template user.wm",
          e.subException.getMessage());
       e = null;
     }
     File fileIn = new File(fileName);
     assertTrue(fileIn.delete());
     
     fileName = "t2a.tmp";
     args = new String[] { "appjunit", "org/melati/app/TemplateApp", "-o", fileName };
     TemplateApp.main(args);
     String output = "";
     fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete();      
     assertEquals("Hello _guest_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : null "  +
             "Object   : null " + 
             "Troid    : null " + 
             "Method   : org/melati/app/TemplateApp " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator", output);
   }
   
   /**
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMainThreeArgs() throws Exception {
     String fileName = "t3.tmp";
     String[] args = { "appjunit", "user", "0",
          "-o", fileName };
     TemplateApp it = new TemplateApp();
     it.run(args);
     String output = "";
     File fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete();      
     assertEquals("Hello _guest_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : user (from the data structure definition) "  +
             "Object   : _guest_ " + 
             "Troid    : 0 " + 
             "Method   : null " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator" , output);
   }
 
   
   /**
    * Also covers .wm extension.
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMainFourArgs() throws Exception {
     String fileName = "t4.tmp";
     String[] args = { "appjunit", "user", "0",
         "org/melati/app/TemplateApp",  "-o", fileName };
     TemplateApp it = new TemplateApp();
     it.run(args);
     String output = "";
     File fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete();      
     assertEquals("Hello _guest_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : user (from the data structure definition) "  +
             "Object   : _guest_ " + 
             "Troid    : 0 " + 
             "Method   : org/melati/app/TemplateApp " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator" , output);
   }
 
   
   /**
    * @see org.melati.app.TemplateApp#main(String[])
    */
   public void testMainZeroArgs() throws Exception {
     String fileName = "t0.tmp";
     String[] args = { "-o", fileName };
     TemplateApp it = new TemplateApp();
     try { 
       it.run(args);
       fail("Should have bombed");
     } catch (ConfigException e) {
       e = null;
     }
     File fileIn = new File(fileName);
     assertTrue(fileIn.delete());      
   }
 
   /**
    * @throws Exception
    */
   public void testLogin() throws Exception { 
     String fileName = "t.tmp";
     String[] args = { "appjunit", "user", "0",
         "org/melati/app/TemplateApp",  "-u", "_administrator_","-p", "FIXME","-o", fileName};
     TemplateApp it = new ConfiguredTemplateApp();
     it.run(args);
     String output = "";
     File fileIn = new File(fileName);
     BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileIn)));
     while (in.ready()) {
       output += in.readLine();
     }
     in.close();
     fileIn.delete();      
     assertEquals("Hello _administrator_" + 
             "You have expanded template org/melati/app/TemplateApp" + 
             "Your melati contains:" + 
             "Database : jdbc:hsqldb:mem:appjunit " + 
             "Table    : user (from the data structure definition) "  +
             "Object   : _guest_ " + 
             "Troid    : 0 " + 
             "Method   : org/melati/app/TemplateApp " + 
             "System Users" + 
             "============" +
             "  Melati guest user" + 
             "  Melati database administrator" +
             "Form settings=============  -u _administrator_  -p FIXME", output);
     
   }
   /**
    * Test no configured template engine.
    */
   public void testNoTemplateEngineConfigured() throws Exception { 
     String fileName = "junitTest99.tmp";
     String[] args = { "appjunit", "user", "0",
         "org/melati/app/TemplateApp",  "-u", "_administrator_","-p", "FIXME","-o", fileName};
     TemplateApp it = new MisConfiguredTemplateApp();
     try { 
       it.run(args);
       fail("Should have blown up");
     } catch (MelatiConfigurationException e) {
       System.err.println(e.getMessage());
       assertEquals("org.melati.util.MelatiConfigurationException: " +
           "Have you configured a template engine? " +
           "org.melati.MelatiConfig.templateEngine currently set to " + 
           "org.melati.template.NoTemplateEngine",
                    e.getMessage());
       e = null;
     }
     File fileIn = new File(fileName);
     assertTrue(fileIn.delete());      
   }
 }
