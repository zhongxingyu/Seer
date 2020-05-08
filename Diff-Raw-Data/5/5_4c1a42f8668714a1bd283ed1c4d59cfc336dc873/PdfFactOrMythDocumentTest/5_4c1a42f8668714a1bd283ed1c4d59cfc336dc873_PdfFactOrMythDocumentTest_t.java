 package com.valuablecode.tool;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import org.junit.Test;
 
 public class PdfFactOrMythDocumentTest {
 
 	@Test public void
 	fail_when_close_called_prior_to_adding_a_card() {
		FactOrMythDocument sut = new PdfFactOrMythDocument();
 		 
 		 try {
 			sut.close(); 
 			fail("Expected to fail when close called prior to adding a card");
 		} catch (Exception expected) {
			assertThat(expected.getMessage(), containsString("Can't close document without first adding a card."));
 		}
 		
 	}
 }
