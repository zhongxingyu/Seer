 package tests.unit;
 
 import static org.junit.Assert.*;
 
 import line.Line; 
 import line.LineType;
 import line.meta_information.MetaInformation;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import gll.GlobalLineList;
 import gll.LineNodeHeader;
 
 public class GlobalLineListOutput {
 
 	private GlobalLineList gll = null; 
 	
 	@Before
 	public void instantiateGll() {
 		gll = new GlobalLineList();
		Line nextLine1 = new MetaInformation("LineType 1");
 		Line nextLine2 = new MetaInformation("LineType 2");
 		gll.addLine(nextLine1, LineNodeHeader.DEFAULT);
 		gll.addLine(nextLine2, LineNodeHeader.DEFAULT);
 	}
 	
 	@Test
 	public void test() {
 		System.out.println(gll.toString());
 		assertTrue(true);
 	}
 
 }
