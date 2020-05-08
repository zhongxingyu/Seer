 package org.araqne.logdb.query.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.mock;
 
 import java.io.File;
 
 import org.araqne.cron.TickService;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.impl.FunctionRegistryImpl;
 import org.araqne.logdb.query.command.OutputJson;
 import org.araqne.logdb.query.engine.QueryParserServiceImpl;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @since 1.6.7
  * @author darkluster
  * 
  */
 public class OutputJsonParserTest {
 
 	private QueryParserService queryParserService;
 
 	@Before
 	public void setup() {
 		QueryParserServiceImpl p = new QueryParserServiceImpl();
 		p.setFunctionRegistry(new FunctionRegistryImpl());
 		queryParserService = p;
 	}
 
 	@Test
 	public void testNormalCase() {
 		new File("logexport.json").delete();
 		OutputJson json = null;
 		try {
 			OutputJsonParser p = new OutputJsonParser(mock(TickService.class));
 			p.setQueryParserService(queryParserService);
 
 			json = (OutputJson) p.parse(null, "outputjson logexport.json sip, dip ");
 
 			json.onStart();
 
 			File f = json.getTxtFile();
 			assertEquals("logexport.json", f.getName());
 			assertEquals("sip", json.getFields().get(0));
 			assertEquals("dip", json.getFields().get(1));
 
			assertEquals("outputjson encoding=utf-8 logexport.json sip, dip", json.toString());
 		} finally {
 			if (json != null)
 				json.onClose(QueryStopReason.End);
 			new File("logexport.json").delete();
 		}
 	}
 
 	@Test
 	public void testMissingField() {
 		String query = "outputjson ";
 
 		try {
 			OutputJsonParser p = new OutputJsonParser(mock(TickService.class));
 			p.setQueryParserService(queryParserService);
 			p.parse(null, query);
 
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println("query " + query);
 				System.out.println(e.getMessage());
 			}
 			assertEquals("30302", e.getType());
 			assertEquals(11, e.getStartOffset());
 			assertEquals(10, e.getEndOffset());
 		}
 	}
 
 	@Test
 	public void testMissingPartition() {
 		String query = "outputjson {logtime:/yyyy/MM/dd/}{now:HHmm.json} src_ip, dst_ip";
 
 		try {
 			OutputJsonParser p = new OutputJsonParser(mock(TickService.class));
 			p.setQueryParserService(queryParserService);
 
 			p.parse(null, query);
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println("query " + query);
 				System.out.println(e.getMessage());
 			}
 			assertEquals("30301", e.getType());
 			assertEquals(11, e.getStartOffset());
 			assertEquals(62, e.getEndOffset());
 		} finally {
 			new File("logexport.json").delete();
 		}
 	}
 
 	@Test
 	public void testInvalidEndCharacter() {
 		String query = "outputjson logexport.json sip,";
 
 		try {
 			OutputJsonParser p = new OutputJsonParser(mock(TickService.class));
 			p.setQueryParserService(queryParserService);
 
 			p.parse(null, query);
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println("query " + query);
 				System.out.println(e.getMessage());
 			}
 			assertEquals("30300", e.getType());
 			assertEquals(29, e.getStartOffset());
 			assertEquals(29, e.getEndOffset());
 		}
 	}
 }
