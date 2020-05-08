 package com.googlecode.commandme;
 
 import com.googlecode.commandme.annotations.Parameter;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Dmitry Sidorenko
  */
 public class CLIParserTest {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(CLIParserTest.class);
 
     @Test(expected = NullPointerException.class)
     public void testCreateModuleNull() throws CliException {
         CLIParser.createModule(null);
     }
 
     @Test
     public void testCreateModuleNotNull() throws Exception {
         assertThat(CLIParser.createModule(TestModule.class), notNullValue());
     }
 
    @Test
    public void testShutdown() throws Exception {
        CommandLine<TestModule> commandLine = CLIParser.createModule(TestModule.class);
        commandLine.execute(new String[]{});
        commandLine.shutdown();

        assertThat(commandLine.getModule(), nullValue());
    }

     public static class TestModule {
         public TestModule() {
         }
 
         @Parameter
         public void setName(String sd) {
         }
 
         @Parameter(longName = "foo", shortName = "f", defaultValue = "0", description = "none", helpRequest = true)
         public void setNoName(int i) {
         }
     }
 
 }
