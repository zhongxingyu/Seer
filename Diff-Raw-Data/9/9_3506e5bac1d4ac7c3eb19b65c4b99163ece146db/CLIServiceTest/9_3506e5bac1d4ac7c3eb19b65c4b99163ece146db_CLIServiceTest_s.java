 package org.gethydrated.hydra.test.cli;
 
 import org.gethydrated.hydra.api.configuration.ConfigItemNotFoundException;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.gethydrated.hydra.cli.CLIService;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * 
  * @author Hanno Sternberg
  * @since 0.1.0
  * 
  */
 public class CLIServiceTest {
 
     /**
          * 
          */
     private CLIService dut;
 
     /**
          * 
          */
     private CLITestContext ctx;
 
     /**
      * 
      * @throws Exception .
      */
     @Before
     public final void setUp() throws Exception {
         ctx = new CLITestContext();
         dut = new CLIService(ctx);
     }
 
     /**
      * 
      * @throws Exception .
      */
     @After
     public void tearDown() throws Exception {
     }
 
     /**
          * 
          */
     @Test
     public final void testEcho() {
         dut.handleInputString("echo Hallo");
         assertEquals("Hallo", ctx.getOutput().trim());
     }
     
     /**
      * 
      */
     @Test
     public final void testEchoEmpty() {
         dut.handleInputString("echo");
         assertEquals("", ctx.getOutput().trim());
     }
     
     /**
      * 
      */
     @Test
     public final void testEchoBlank() {
         dut.handleInputString("echo ");
         assertEquals("", ctx.getOutput().trim());
     }   
    
    /**
     * 
     */
    @Test
    public final void testEchoString() {
        dut.handleInputString("echo \"Test\"");
        assertEquals("Test", ctx.getOutput().trim());
    }
 
     /**
          * 
          */
     @Test
     public final void testConfigSet() {
         dut.handleInputString("configuration set Network.Host localhost");
         assertEquals("Network.Host = localhost", ctx.getOutput().trim());
         try {
             assertEquals("localhost",
                     ctx.getConfiguration().getString("Network.Host"));
         } catch (ConfigItemNotFoundException e) {
             fail("Configuration Item not found");
         }
     }
 
     /**
          * 
          */
     @Test
     public final void testConfigGet() {
         dut.handleInputString("configuration get Network.Port");
         assertEquals("1337", ctx.getOutput().trim());
     }
 
     /**
      * Test method for "configuration list".
      */
     @Test
     public final void testList() {
         dut.handleInputString("configuration list Network");
         assertEquals("Port" + System.getProperty("line.separator") + "Host"
                 + System.getProperty("line.separator"), ctx.getOutput());
     }
 
 }
