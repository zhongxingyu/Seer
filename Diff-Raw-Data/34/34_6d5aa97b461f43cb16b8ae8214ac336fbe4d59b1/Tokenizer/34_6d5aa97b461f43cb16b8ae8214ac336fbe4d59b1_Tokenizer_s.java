 package group1.inverted;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.hadoop.io.Text;
 
 public class Tokenizer implements Iterable<String> {
 
 	protected String docId;
 
 	protected List<String> tokens = new ArrayList<String>(50);
 
 	protected static final String ID_TAG = "DOCID";
 
 	protected static final String TITLE_TAG = "TITLE";
 
 	protected static final String TEXT_TAG = "TEXT";
 
 	protected String data;
 
 	protected int curr;
 
 	protected static SAXParserFactory saxFact = SAXParserFactory.newInstance();
 
 	protected enum TYPE {
 		DOCID, TEXT, PASS
 	};
 
 	protected static class TextRun {
 		public TYPE type = TYPE.TEXT;
 		public String value;
 
 		public TextRun(String value, TYPE type) {
 			this.value = value;
 			this.type = type;
 		}
 	}
 
 	protected static class TextBounds {
 		public boolean done = false;
 		public int start;
 		public int end;
 	}
 
	public Tokenizer(Text t) throws Exception {
 		parse(t);
 	}
 
 	protected Tokenizer() {
 		// unit tests
 	}
 
 	protected TextRun getNext() {
 		TextBounds tag = findTag(curr);
 		if (tag.done) {
 			return null;
 		}
 		TextRun rval;
 		String tagName = getBeween(tag);
 		if (tagName.equalsIgnoreCase(ID_TAG)) {
 			TextBounds close = findClose(tag);
 			String id = getBeween(tag, close).trim();
 			rval = new TextRun(id, TYPE.DOCID);
 			curr = close.end;
 		} else if (tagName.equalsIgnoreCase(TEXT_TAG)
 				|| tagName.equalsIgnoreCase(TITLE_TAG)) {
 			TextBounds close = findClose(tag);
 			String str = getBeween(tag, close);
 			rval = new TextRun(str, TYPE.TEXT);
 			curr = close.end;
 		} else {
 			rval = new TextRun(null, TYPE.PASS);
 			curr = tag.end;
 		}
 		return rval;
 	}
 
 	protected TextBounds findClose(TextBounds open) {
 		TextBounds close = findTag(open.end);
 		if (close.done) {
 			close.start = data.length();
 			close.end = close.start;
 		}
 		return close;
 	}
 
 	protected TextBounds findTag(int i) {
 		TextBounds rval = new TextBounds();
 		rval.start = data.indexOf('<', i);
 		rval.done = rval.start == -1;
 		if (!rval.done) {
 			rval.end = data.indexOf('>', rval.start);
 			if (rval.end == -1) {
 				rval.end = data.length() - 1;
 			}
 		}
 		return rval;
 	}
 
 	protected String getBeween(TextBounds tb) {
 		return data.substring(tb.start + 1, tb.end);
 	}
 
 	protected String getBeween(TextBounds open, TextBounds close) {
 		return data.substring(open.end + 1, close.start);
 	}
 
 	protected void parse(Text t) {
 		data = new String(t.getBytes()).trim();
 
 		TextRun run = getNext();
 		while (run != null) {
 			switch (run.type) {
 			case DOCID:
 				this.docId = run.value;
 				break;
 			case TEXT:
 				StringTokenizer toker = new StringTokenizer(run.value,
 						" \t\n.,");
 				while (toker.hasMoreTokens()) {
 					String next = toker.nextToken();
 					if (next.length() >= 5) {
 						tokens.add(next);
 					}
 				}
 				break;
 			case PASS:
 			default:
 				// do nothing
 			}
 			run = getNext();
 		}
 	}
 
 	public String getDocId() {
		return docId;
 	}
 
 	public Iterator<String> iterator() {
 		return tokens.iterator();
 	}
 
 }
