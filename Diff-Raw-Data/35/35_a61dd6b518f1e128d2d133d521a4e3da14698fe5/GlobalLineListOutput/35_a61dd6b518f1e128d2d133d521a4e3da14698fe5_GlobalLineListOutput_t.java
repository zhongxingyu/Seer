 package tests.unit;
 
 import static org.junit.Assert.*;
 
import character.Character;
 import line.Line; 
 import line.LineType;
 import line.meta_information.MetaInformation;
import line.quote.Quote;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import gll.GlobalLineList;
 import gll.LineNodeHeader;
 
 public class GlobalLineListOutput {
 
 	private GlobalLineList gll = null; 
 	
 	@Before
 	public void instantiateGll() {
		Character character = new Character("Vivek Nair", "Some description");
		
 		gll = new GlobalLineList();
 		Line nextLine2 = new MetaInformation("LineType 2");
		Line nextLine1 = new MetaInformation("LineType 1");
		Line nextQuote1 = new Quote(character, "Some raw quote [Gestures action]");
		
 		gll.addLine(nextLine1, LineNodeHeader.DEFAULT);
 		gll.addLine(nextLine2, LineNodeHeader.DEFAULT);
		gll.addLine(nextQuote1, LineNodeHeader.DEFAULT);
 	}
 	
 	@Test
 	public void test() {
 		System.out.println(gll.toString());
 		assertTrue(true);
 	}
 
 }
