 package com.develogical;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class QueryProcessorTest {
 
     QueryProcessor queryProcessor = new QueryProcessor();
 
     @Test
     public void returnsEmptyStringIfCannotProcessQuery() throws Exception {
         assertThat(queryProcessor.process("test"), is(""));
     }
 
     @Test
     public void knowsAboutProgramming() throws Exception {
         assertThat(queryProcessor.process("programming"), containsString("computing"));
     }
 
     @Test
     public void knowsAboutGitHub() {
         assertThat(queryProcessor.process("github"), containsString("share code"));
     }
 
 
 
     @Test
     public void knowsWhatItsNameIs()
     {
        assertThat(queryProcessor.process("what is your name"), containsString("damp peak"));
     }
 }
