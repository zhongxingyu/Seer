 package com.develogical;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.StringContains.containsString;
 
 import org.junit.Test;
 
 public class QueryProcessorTest {
 
     QueryProcessor queryProcessor = new QueryProcessor();
 
     @Test
     public void unkownQuery() throws Exception {
         assertThat(queryProcessor.process("test"), is("Sorry, no clue"));
     }
 
     @Test
     public void knowsAboutSpa() throws Exception {
         assertThat(queryProcessor.process("SPA2012"), containsString("conference"));
     }
     
     @Test
 	public void knowsAboutCDWorkshop() throws Exception {
         assertThat(queryProcessor.process("Eendje"), containsString("Fantastische workshops!"));
 	}

    @Test
	public void oops() throws Exception {
        assertThat(queryProcessor.process("Twweetje"), containsString("Fantastische workshops!"));
	}
     
 }
