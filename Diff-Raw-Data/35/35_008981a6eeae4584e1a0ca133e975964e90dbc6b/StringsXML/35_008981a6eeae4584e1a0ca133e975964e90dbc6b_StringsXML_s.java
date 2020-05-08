 package yang.surface;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import yang.graphics.font.DrawableAnchoredLines;
 
 public class StringsXML {
 
 	public static final String UNKNOWN_KEY = "NONE";
 
 	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
 	private static final String RESOURCES_OPEN = "<resources>";
 	private static final String RESOURCES_CLOSE = "</resources>";
 
 	private final HashMap<String, String> mStrings;
 	private StringsXML mFallbackStringsXML = null;
 
 	StringsXML() {
 		mStrings = new HashMap<String, String>();
 	}
 
 	public StringsXML(InputStream xmlStream,StringsXML fallbackStrings) {
 		this();
 		mFallbackStringsXML = fallbackStrings;
 		load(xmlStream);
 	}
 
 	public StringsXML(InputStream xmlStream) {
 		this(xmlStream,null);
 	}
 
 	public StringsXML(StringsXML fallbackStrings) {
 		this();
 		mFallbackStringsXML = fallbackStrings;
 	}
 
 	public StringsXML(boolean createFallback) {
 		mStrings = new HashMap<String, String>();
 
 		if(createFallback) {
 			mFallbackStringsXML = new StringsXML();
 		}
 	}
 
 	public StringsXML load(InputStream xmlStream) {
 		return load(xmlStream, this);
 	}
 
 	public StringsXML loadFallback(InputStream xmlStream) {
 		return load(xmlStream, mFallbackStringsXML);
 	}
 
 	private final static StringsXML load(InputStream xmlStream, StringsXML stringsXML) {
 		stringsXML.mStrings.clear();
 		try {
 			XMLReader reader = XMLReaderFactory.createXMLReader();
 			reader.setContentHandler(new StringXMLParser(stringsXML));
 			reader.parse(new InputSource(xmlStream));
 			xmlStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return stringsXML;
 	}
 
 	public String getRawString(String name) {
 		final String toReturn = mStrings.get(name);
 		if (toReturn == null) {
 			if(mFallbackStringsXML==null)
 				System.err.println("string:" +name +" not found");
 			else
 				return mFallbackStringsXML.getRawString(name);
 			return UNKNOWN_KEY;
 		}
 		else return toReturn;
 	}
 
 	public DrawableAnchoredLines createDrawableString(String name,float horizontalAnchor) {
 		return (DrawableAnchoredLines)new DrawableAnchoredLines(getRawString(name)).setHorizontalAnchor(horizontalAnchor).setConstant();
 	}
 
 	public DrawableAnchoredLines createDrawableString(String name) {
 		return new DrawableAnchoredLines(getRawString(name)).setConstant();
 	}
 
 	public DrawableAnchoredLines createDrawableFormatString(String name) {
 		return (DrawableAnchoredLines)new DrawableAnchoredLines().allocFormatString(getRawString(name));
 	}
 
 	public DrawableAnchoredLines createDrawableFormatString(String name,float anchor) {
 		return (DrawableAnchoredLines)new DrawableAnchoredLines().setHorizontalAnchor(anchor).allocFormatString(getRawString(name));
 	}
 
 	public void clear() {
 		mStrings.clear();
 	}
 
 	public void createKeyIfMissing(String key) {
 		if(!mFallbackStringsXML.mStrings.containsKey(key)) {
 			mFallbackStringsXML.mStrings.put(key, UNKNOWN_KEY);
 		}
 	}
 
 	public void save(PrintStream stream) {
 		stream.println(XML_HEADER);
 		stream.println(RESOURCES_OPEN);
 
 		for(Entry<String,String> e: mFallbackStringsXML.mStrings.entrySet()){
 			stream.println("\t<string name=\""+e.getKey()+ "\">"+e.getValue()+"</string>");
 		}
 		stream.print(RESOURCES_CLOSE);
 	}
 
 
	private static class StringXMLParser implements ContentHandler {
 
 		private String mCurString;
 		private String mCurKey;
 		private StringsXML mStrings;
 
 		public StringXMLParser(StringsXML strings) {
 			mStrings = strings;
 		}
 
 		@Override
 		public void characters(char[] ch, int start, int length) throws SAXException {
 			mCurString = new String(ch, start, length);
 			mCurString.trim().replace("\n", "\0").replace("\t", "\0").replace("\\n", "\n").replace("\\t", "\t").replace("\\'", "'");
 		}
 
 		@Override
 		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
 			if (localName.equals("string")) {
 				mCurKey = atts.getValue("name");
 			}
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName) throws SAXException {
 			if (localName.equals("string")) {
 				mStrings.mStrings.put(mCurKey, mCurString);
 			}
 		}

		@Override public void endDocument() throws SAXException {}

		@Override public void startPrefixMapping(String suffix, String uri) throws SAXException {}

		@Override public void endPrefixMapping(String prefix) throws SAXException {}

		@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override public void processingInstruction(String target, String data) throws SAXException {}

		@Override public void setDocumentLocator(Locator locator) {}

		@Override public void skippedEntity(String name) throws SAXException {}

		@Override public void startDocument() throws SAXException {}
 	}
 }
