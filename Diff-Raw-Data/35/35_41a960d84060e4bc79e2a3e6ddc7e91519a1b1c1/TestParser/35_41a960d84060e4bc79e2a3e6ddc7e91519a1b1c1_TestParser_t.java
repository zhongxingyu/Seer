 package test.unit;
 
 import static org.junit.Assert.assertEquals;
 import io.reader.Parser;
 import io.reader.ParserException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import members.Competitor;
 import members.Time;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestParser {
 
 	private Parser parser;
 	private ArrayList<ArrayList<String>> input;
 
 	@Before
 	public void setUp() {
 		parser = new Parser();
 		input = new ArrayList<ArrayList<String>>();
 	}
 
 	@Test (expected = ParserException.class)
 	public void testEmptyInput() throws ParserException {
 		parser.parse(input);
 	}
 	
 	@Test (expected = ParserException.class)
 	public void testMissingStartNbr() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		ArrayList<String> row2 = new ArrayList<String>();
 		ArrayList<String> row3 = new ArrayList<String>();
 		row1.add("1");
 		row1.add("Bengt Bsson");
 		row2.add("Chris Csson");
 		row3.add("3");
 		row3.add("David Dsson");
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 		parser.parse(input);
 	}
 	
 	@Test (expected = ParserException.class)
 	public void testFileTooShortWithFileIdentifier() throws ParserException {
 		Map<Integer, Competitor> cs = new HashMap<Integer, Competitor>();
 		parser.parse(input, cs, Parser.FileIdentifier.name_file);
 	}
 	
 	@Test (expected = ParserException.class)
 	public void testFileTooShortWithoutFileIdentifier() throws ParserException {
 		Map<Integer, Competitor> cs = new HashMap<Integer, Competitor>();
 		parser.parse(input, cs);
 	}
 	
 	@Test (expected = ParserException.class)
 	public void testInvalidType() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		ArrayList<String> row2 = new ArrayList<String>();
 		ArrayList<String> row3 = new ArrayList<String>();
 		row1.add("startNr");
 		row1.add("FinnsEj");
 		row2.add("1");
 		row2.add("Chris Csson");
 		row3.add("3");
 		row3.add("David Dsson");
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 		parser.parse(input);
 	}
 	
 	@Test (expected = ParserException.class)
 	public void testColumnLengthMismatcht() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		ArrayList<String> row2 = new ArrayList<String>();
 		ArrayList<String> row3 = new ArrayList<String>();
 		row1.add("startNr");
 		row1.add("Namn");
 		row1.add("Starttid");
 		row2.add("1");
 		row2.add("Hans");
 		row3.add("3");
 		row3.add("David Dsson");
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 		parser.parse(input);
 	}
 
 	@Test
 	public void testParseSimple() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		row1.add("StartNr");
 		row1.add("StartTid");
 
 		Time t1 = Time.parse("12.11.10"), t2 = Time.parse("05.11.19");
 		ArrayList<String> row2 = new ArrayList<String>();
 		row2.add("1");
 		row2.add(t1.toString());
 
 		ArrayList<String> row3 = new ArrayList<String>();
 		row3.add("2");
 		row3.add(t2.toString());
 
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 
 		Map<Integer, Competitor> competitors = parser.parse(input);
 		assertEquals(t1, competitors.get(1).getStartTimes().get(0));
 		assertEquals(t2, competitors.get(2).getStartTimes().get(0));
 	}
 
 	@Test
 	public void testParseComplex() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		row1.add("StartNr");
 		row1.add("StartTid");
 		row1.add("MalTid");
 
 		Time t1 = Time.parse("12.11.10"), t2 = Time.parse("18.11.10"), t3 = Time
 				.parse("05.11.19"), t4 = Time.parse("20.11.19");
 
 		ArrayList<String> row2 = new ArrayList<String>();
 		row2.add("1");
 		row2.add(t1.toString());
 		row2.add(t2.toString());
 
 		ArrayList<String> row3 = new ArrayList<String>();
 		row3.add("2");
 		row3.add(t3.toString());
 		row3.add(t4.toString());
 
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 
 		Map<Integer, Competitor> competitors = parser.parse(input);
 		assertEquals(t1, competitors.get(1).getStartTimes().get(0));
 		assertEquals(t2, competitors.get(1).getFinishTimes().get(0));
 		assertEquals(t3, competitors.get(2).getStartTimes().get(0));
 		assertEquals(t4, competitors.get(2).getFinishTimes().get(0));
 	}
 
 	@Test
 	public void testParseManyFiles() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		row1.add("StartNr");
 		row1.add("StartTid");
 
 		Time t1 = Time.parse("12.11.10"), t2 = Time.parse("05.11.19"), t3 = Time
 				.parse("20.11.10"), t4 = Time.parse("22.11.19");
 
 		ArrayList<String> row2 = new ArrayList<String>();
 		row2.add("1");
 		row2.add(t1.toString());
 
 		ArrayList<String> row3 = new ArrayList<String>();
 		row3.add("2");
 		row3.add(t2.toString());
 
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 
 		Map<Integer, Competitor> competitors = parser.parse(input);
 
 		input = new ArrayList<ArrayList<String>>();
 		row1 = new ArrayList<String>();
 		row1.add("StartNr");
 		row1.add("MalTid");
 
 		row2 = new ArrayList<String>();
 		row2.add("1");
 		row2.add(t3.toString());
 
 		row3 = new ArrayList<String>();
 		row3.add("2");
 		row3.add(t4.toString());
 
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 
 		competitors = parser.parse(input, competitors);
 
 		assertEquals(t1, competitors.get(1).getStartTimes().get(0));
 		assertEquals(t3, competitors.get(1).getFinishTimes().get(0));
 		assertEquals(t2, competitors.get(2).getStartTimes().get(0));
 		assertEquals(t4, competitors.get(2).getFinishTimes().get(0));
 	}
 
 	@Test
 	public void testMultipleFinishTimes() throws ParserException {
 		ArrayList<String> row1 = new ArrayList<String>();
 		row1.add("StartNr");
 		row1.add("Maltid");
 
 		Time t1 = Time.parse("12.11.10"), t2 = Time.parse("05.11.19"), t3 = Time
 				.parse("13.21.10"), t4 = Time.parse("14.11.10"), t5 = Time
 				.parse("14.33.10");
 
 		ArrayList<String> row2 = new ArrayList<String>();
 		row2.add("1");
 		row2.add(t1.toString());
 
 		ArrayList<String> row3 = new ArrayList<String>();
 		row3.add("2");
 		row3.add(t2.toString());
 
 		ArrayList<String> row4 = new ArrayList<String>();
 		row4.add("1");
 		row4.add(t3.toString());
 
 		ArrayList<String> row5 = new ArrayList<String>();
 		row5.add("3");
 		row5.add(t4.toString());
 
 		ArrayList<String> row6 = new ArrayList<String>();
 		row6.add("2");
 		row6.add(t5.toString());
 
 		input.add(row1);
 		input.add(row2);
 		input.add(row3);
 		input.add(row4);
 		input.add(row5);
 		input.add(row6);
 
 		Map<Integer, Competitor> competitors = parser.parse(input);
 
 		assertEquals(2, competitors.get(1).getFinishTimes().size());
 		assertEquals(t1, competitors.get(1).getFinishTimes().get(0));
 		assertEquals(t3, competitors.get(1).getFinishTimes().get(1));
 		assertEquals(2, competitors.get(2).getFinishTimes().size());
 		assertEquals(t2, competitors.get(2).getFinishTimes().get(0));
 		assertEquals(t5, competitors.get(2).getFinishTimes().get(1));
 		assertEquals(1, competitors.get(3).getFinishTimes().size());
 		assertEquals(t4, competitors.get(3).getFinishTimes().get(0));
 
 	}
 
 }
