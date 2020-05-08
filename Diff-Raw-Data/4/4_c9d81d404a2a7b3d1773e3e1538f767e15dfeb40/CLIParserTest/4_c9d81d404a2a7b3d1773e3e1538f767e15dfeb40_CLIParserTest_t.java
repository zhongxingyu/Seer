 package com.googlecode.commandme;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Dmitry Sidorenko
  * @date Jun 3, 2010
  */
 public class CLIParserTest {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(CLIParserTest.class);
 
    @Test(expected = CliException.class)
    public void testCreateModuleNull() throws CliException {
         CLIParser.createModule(null);
     }
 
     @Test
     public void testCreateModuleNotNull() throws Exception {
         assertThat(CLIParser.createModule(Object.class), notNullValue());
     }
 }
