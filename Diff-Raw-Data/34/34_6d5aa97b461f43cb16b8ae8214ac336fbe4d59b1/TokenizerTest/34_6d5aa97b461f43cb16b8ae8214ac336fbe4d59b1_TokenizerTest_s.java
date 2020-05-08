 package group1.inverted;
 
 import group1.inverted.Tokenizer.TextBounds;
 
 import java.util.Iterator;
 
 import junit.framework.Assert;
 
 import org.apache.hadoop.io.Text;
 import org.junit.Test;
 
 public class TokenizerTest {
 
 	protected String simpleXml = "<DOC>\n"
 			+ "<DOCID>REU001-0013.940701</DOCID>\n"
 			+ "<TDTID>TDT000002</TDTID>\n"
 			+ "<SOURCE>REUTERS</SOURCE>\n"
 			+ "<DATE>07/01/94 00:57</DATE>\n"
 			+ "<TITLE> PALESTINIANS TO RECEIVE AID TO POLICE GAZA, JERICHO</TITLE>\n"
 			+ "<HEADLINE> PALESTINIANS TO RECEIVE AID TO POLICE GAZA, JERICHO</HEADLINE>\n"
 			+ "<SUBJECT> BC-MIDEAST-AID </SUBJECT>\n" + "<AUTHOR></AUTHOR>\n"
 			+ "<TEXT>the cake is a lie  flabber newline</TEXT></DOC>";
 
 	protected String badXml = "<DOC>\n"
 			+ "<DOCID>REU001-0013.940701<WhoKnows>\n"
 			+ "<TDTID>TDT000002</TDTID>\n"
 			+ "<SOURCE>REUTERS</SOURCE>\n"
 			+ "<DATE>07/01/94 00:57</DATE>\n"
 			+ "<TITLE> PALESTINIANS TO RECEIVE AID TO POLICE GAZA, JERICHO</TITLE>\n"
 			+ "<HEADLINE> PALESTINIANS TO RECEIVE AID TO POLICE GAZA, JERICHO</HEADLINE>\n"
 			+ "<SUBJECT> BC-MIDEAST-AID </SUBJECT>\n" + "<AUTHOR></AUTHOR>\n"
 			+ "<TEXT>the cake is a lie  flabber newline";
 
 	@Test
 	public void simpleTest() throws Exception {
 		assertXml(simpleXml);
 	}
 
 	public void assertXml(String xml) throws Exception {
 		Text text = new Text(xml);
		Tokenizer tokenizer = new Tokenizer(text);
 		String docId = tokenizer.getDocId();
		Assert.assertEquals("REU001-0013.940701", docId);
 		Iterator<String> i = tokenizer.iterator();
 		Assert.assertEquals("PALESTINIANS", i.next());
 		Assert.assertEquals("RECEIVE", i.next());
 		Assert.assertEquals("POLICE", i.next());
 		Assert.assertEquals("JERICHO", i.next());
 		Assert.assertEquals("flabber", i.next());
 		Assert.assertEquals("newline", i.next());
 		Assert.assertEquals(false, i.hasNext());
 	}
 
 	@Test
 	public void tagTest() {
 		Tokenizer toker = new Tokenizer();
 		String xml = "<foo>bar</foo><next>one</next>";
 		toker.data = xml;
 		TextBounds open = toker.findTag(0);
 		TextBounds close = toker.findTag(open.end);
 		Assert.assertEquals("foo", toker.getBeween(open));
 		Assert.assertEquals("bar", toker.getBeween(open, close));
 		open = toker.findTag(close.end);
 		close = toker.findTag(open.end);
 		Assert.assertEquals("next", toker.getBeween(open));
 		Assert.assertEquals("one", toker.getBeween(open, close));
 		open = toker.findTag(close.end);
 		Assert.assertTrue(open.done);
 
 	}
 
 	@Test
 	public void malformTest() throws Exception {
 		assertXml(badXml);
 	}
 }
