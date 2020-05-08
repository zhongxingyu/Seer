 package com.valuablecode.tool;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.both;
 import static org.junit.matchers.JUnitMatchers.containsString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import org.junit.Test;
 
 public class FileBasedCardProviderTest {
 
 	@Test public void
 	creates_single_card() {
 		FileBasedCardProvider sut = createFileBasedCardProvider("First Card");
 		List<FactOrMythCard> cards = sut.getCards();
 		
 		assertThat(cards.size(), equalTo(1));
 		assertThat(cards.get(0).getCardText(), equalTo("First Card"));
 	}
 
 	@Test public void
 	creates_multiple_cards() {
 		FileBasedCardProvider sut = createFileBasedCardProvider("First Card\nSecond Card");
 		List<FactOrMythCard> cards = sut.getCards();
 		
 		assertThat(cards.size(), equalTo(2));
 		assertThat(cards.get(0).getCardText(), equalTo("First Card"));
 		assertThat(cards.get(1).getCardText(), equalTo("Second Card"));
 	}
 
 	@Test public void
 	creates_multiline_cards() {
 		FileBasedCardProvider sut = createFileBasedCardProvider("First Card|Extra Line|Extra Extra Line\nSecond Card");
 		List<FactOrMythCard> cards = sut.getCards();
 		
 		assertThat(cards.size(), equalTo(2));
 		assertThat(cards.get(0).getCardText(), equalTo("First Card\nExtra Line\nExtra Extra Line"));
 		assertThat(cards.get(1).getCardText(), equalTo("Second Card"));
 	}
 	
 	@Test public void
 	fails_for_an_invalid_card_source_file_name() {
 		String invalidCardSourceFileName = "_INVALID_FILE_NAME_";
 		 
 		try {
 			new FileBasedCardProvider(invalidCardSourceFileName); 
 			fail("Expected to fail to find the card source file");
 		} catch (RuntimeException expected) {
			assertThat(expected.getMessage(), both(containsString("Can't find Fact or Myth file"))
					.and(containsString(invalidCardSourceFileName))); 
 		}
 	}
 
 	@Test public void
 	fails_for_an_invalid_card_source_file() {
 		String invalidCardSourceFileName = "_INVALID_FILE_NAME_";
 		BufferedReader reader = mock(BufferedReader.class);
 		
 		try {
 			when(reader.readLine()).thenThrow(new IOException());
 			 
 			new FileBasedCardProvider(invalidCardSourceFileName, reader); 
 			fail("Expected to fail to read a line from the BufferedReader");
 		} catch (IOException e) {
 			fail("The IO Exception should have been transformed to a RuntimeException");
 		} catch (RuntimeException expected) {
			assertThat(expected.getMessage(), both(containsString("Can't read Fact or Myth phrases from"))
					.and(containsString(invalidCardSourceFileName))); 
 		}
 	}
 
 	private FileBasedCardProvider createFileBasedCardProvider(String cardSource) {
 		BufferedReader cardSourceReader = new BufferedReader(new StringReader(cardSource));	
 		
 		return new FileBasedCardProvider("Card Source File Name", cardSourceReader);
 	}
 }
