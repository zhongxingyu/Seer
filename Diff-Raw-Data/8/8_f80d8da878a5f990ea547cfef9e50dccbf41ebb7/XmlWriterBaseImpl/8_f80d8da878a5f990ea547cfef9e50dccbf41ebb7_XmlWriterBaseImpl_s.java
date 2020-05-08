 /*   Copyright 2004 BEA Systems, Inc.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package bingo.lang.xml;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.xml.namespace.NamespaceContext;
 
 //from com.bea.xml.stream
 
 @SuppressWarnings({"unchecked","rawtypes"})
 public class XmlWriterBaseImpl extends XmlWriterBase implements XmlWriter {
 
 	protected static final String	DEFAULTNS = "";
 
 	private Writer writer;
 	private boolean startElementOpened	 = false;
 	private boolean isEmpty = false;
 	private CharsetEncoder encoder;
 
 	// these two stacks are used to implement the
 	// writeEndElement() method
 	private Stack	                localNameStack = new Stack();
 	private Stack	                prefixStack	   = new Stack();
 	private Stack	                uriStack	   = new Stack();
 	private NamespaceContextImpl	context	       = new NamespaceContextImpl();
 
 	private HashSet	  needToWrite;
 	private boolean isPrefixDefaulting;
 	private int	  defaultPrefixCount = 0;
 	private HashSet	setNeedsWritingNs	= new HashSet();
 
 	XmlWriterBaseImpl() {
 
 	}
 
 	XmlWriterBaseImpl(Writer writer) {
 		setWriter(writer);
 	}
 
 	public XmlWriter startDocument() throws XmlException {
 		write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 		return this;
 	}
 	
 	public XmlWriter startDocument(String version) throws XmlException {
 		write("<?xml version=\"");
 		write(version);
 		write("\"?>");
 		return this;
 	}
 
 	public XmlWriter startDocument(String encoding, String version) throws XmlException {
 		write("<?xml version=\"");
 		write(version);
 		write("\" encoding=\"");
 		write(encoding);
 		write("\"?>");
 		return this;
 	}
 	
 	public XmlWriter startElement(String namespaceURI, String localName) throws XmlException {
 		context.openScope();
 		return writeStartElementInternal(namespaceURI, localName);
 	}
 
 	public XmlWriter startElement(String prefix, String namespaceURI, String localName) throws XmlException {
 		
 		if (namespaceURI == null)
 			throw new IllegalArgumentException("The namespace URI may not be null");
 		if (localName == null)
 			throw new IllegalArgumentException("The local name may not be null");
 		if (prefix == null)
 			throw new IllegalArgumentException("The prefix may not be null");
 		
 		context.openScope();
 		prepareNamespace(namespaceURI);
 		context.bindNamespace(prefix, namespaceURI);
 		return writeStartElementInternal(namespaceURI, localName);
 	}
 
 	public XmlWriter startElement(String localName) throws XmlException {
 		context.openScope();
 		return startElement("", localName);
 	}
 	
 	public XmlWriter emptyElement(String localName) throws XmlException {
 		return emptyElement("", localName);
 	}
 	
 	public XmlWriter emptyElement(String namespaceURI, String localName) throws XmlException {
 		openStartElement();
 		prepareNamespace(namespaceURI);
 		isEmpty = true;
 		write("<");
 		writeName("", namespaceURI, localName);
 		
 		return this;
 	}
 
 	public XmlWriter emptyElement(String prefix,String namespaceURI,String localName) throws XmlException {
 		openStartElement();
 		prepareNamespace(namespaceURI);
 		isEmpty = true;
 		write("<");
 		write(prefix);
 		write(":");
 		write(localName);
 		return this;
 	}
 	
 	public XmlWriter namespace(String namespaceURI) throws XmlException {
 		if (!isOpen())
 			throw new XmlException("A start element must be written before the default namespace");
 		if (needsWritingNs(DEFAULTNS)) {
 			write(" xmlns");
 			write("=\"");
 			write(namespaceURI);
 			write("\"");
 			setPrefix(DEFAULTNS, namespaceURI);
 		}
 		return this;
 	}
 	
 	public XmlWriter namespace(String prefix, String namespaceURI) throws XmlException {
 		if (!isOpen())
 			throw new XmlException("A start element must be written before a namespace");
 		if (prefix == null || "".equals(prefix) || "xmlns".equals(prefix)) {
 			namespace(namespaceURI);
 			return this;
 		}
 		if (needsWritingNs(prefix)) {
 			write(" xmlns:");
 			write(prefix);
 			write("=\"");
 			write(namespaceURI);
 			write("\"");
 			setPrefix(prefix, namespaceURI);
 		}
 		return this;
 	}
 	
 	public XmlWriter attribute(String localName, String value) throws XmlException {
 		return attribute("", localName, value);
 	}
 	
 	public XmlWriter attribute(String namespaceURI, String localName, String value) throws XmlException {
 		
 		if (!isOpen()) {
 			throw new XmlException("A start element must be written before an attribute");
 		}
 		
 		prepareNamespace(namespaceURI);
 		write(" ");
 		writeName("", namespaceURI, localName);
 		write("=\"");
 		writeCharactersInternal(value.toCharArray(), 0, value.length(), true);
 		write("\"");
 		return this;
 	}
 
 	public XmlWriter attribute(String prefix, String namespaceURI, String localName, String value) throws XmlException {
 		if (!isOpen())
 			throw new XmlException("A start element must be written before an attribute");
 		prepareNamespace(namespaceURI);
 		context.bindNamespace(prefix, namespaceURI);
 		write(" ");
 		writeName(prefix, namespaceURI, localName);
 		write("=\"");
 		writeCharactersInternal(value.toCharArray(), 0, value.length(), true);
 		write("\"");
 		return this;
 	}
 
 	public XmlWriter text(String text) throws XmlException {
 		return text(text,true);
 	}
 	
 	public XmlWriter text(String text, boolean escape) {
 		closeStartElement();
 		
		if(escape){
 			return writeCharactersInternal(text.toCharArray(), 0, text.length(), false);
 		}else{
 			write(text);
 			return this;
 		}
     }
 
 	public XmlWriter cdata(String data) throws XmlException {
 		closeStartElement();
 		write("<![CDATA[");
 		if (data != null)
 			write(data);
 		write("]]>");
 		return this;
 	}
 	
 	public XmlWriter comment(String data) throws XmlException {
 		closeStartElement();
 		write("<!--");
 		if (data != null)
 			write(data);
 		write("-->");
 		return this;
 	}
 	
 	public XmlWriter raw(String xml) throws XmlException {
 		closeStartElement();
 		write(xml);
 		return this;
 	}
 	
 	public XmlWriter endElement() throws XmlException {
 		/*
 		 * 07-Mar-2006, TSa: Empty elements do not need special handling, since this call only 'closes' them as a side
 		 * effect: real effect is for the open non-empty start element (non-empty just meaning it was written using full
 		 * writeStartElement() as opposed to writeEmptyElement()).
 		 */
 		//boolean wasEmpty = isEmpty;
 		if (isOpen()) {
 			closeStartElement();
 		}
 		try {
 	        String prefix = (String) prefixStack.pop();
 	        String local = (String) localNameStack.pop();
 	        uriStack.pop();
 	        //if(!wasEmpty) {
 	        openEndTag();
 	        writeName(prefix, "", local);
 	        closeEndTag();
 	        //}
 	        context.closeScope();
         } catch (EmptyStackException e) {
         	throw new XmlException("there is no start element to end",e);
         }
 		
 		return this;
 	}
 	
 	public XmlWriter processingInstruction(String target) throws XmlException {
 		closeStartElement();
 		return processingInstruction(target, null);
 	}
 
 	public XmlWriter processingInstruction(String target, String text) throws XmlException {
 		closeStartElement();
 		write("<?");
 		if (target != null) { // isn't passing null an error, actually?
 			write(target);
 		}
 		if (text != null) {
 			// Need a separating white space
 			write(' ');
 			write(text);
 		}
 		write("?>");
 		return this;
 	}
 	
 	public XmlWriter endDocument() throws XmlException {
 		while (!localNameStack.isEmpty()) {
 			endElement();
 		}
 		
 		return this;
 	}
 	
 	public XmlWriter flush() throws XmlException {
 		try {
 			writer.flush();
 		} catch (IOException e) {
 			throw new XmlException(e);
 		}
 		return this;
 	}
 	
 	public void close() throws XmlException {
 		try {
 			writer.flush();
 		} catch (IOException e) {
 			throw new XmlException(e);
 		} finally {
 			localNameStack.clear();
 			prefixStack.clear();
 			uriStack.clear();
 			setNeedsWritingNs.clear();
 		}
 	}
 	
 	protected void setWriter(Writer writer) {
 		this.writer = writer;
 		if (writer instanceof OutputStreamWriter) {
 			String charsetName = ((OutputStreamWriter) writer).getEncoding();
 			this.encoder = Charset.forName(charsetName).newEncoder();
 		} else {
 			this.encoder = null;
 		}
 	}
 
 	protected void write(String s) throws XmlException {
 		try {
			writer.write(s);
 		} catch (IOException e) {
 			throw new XmlException(e);
 		}
 	}
 
 	protected void write(char c) throws XmlException {
 		try {
 			writer.write(c);
 		} catch (IOException e) {
 			throw new XmlException(e);
 		}
 	}
 
 	protected void write(char[] c) throws XmlException {
 		try {
 			writer.write(c);
 		} catch (IOException e) {
 			throw new XmlException(e);
 		}
 	}
 
 	protected void write(char[] c, int start, int len) throws XmlException {
 		try {
 			writer.write(c, start, len);
 		} catch (IOException e) {
 			throw new XmlException(e);
 		}
 	}
 
 	protected XmlWriter writeCharactersInternal(char characters[], int start, int length, boolean isAttributeValue) throws XmlException {
 		if (length == 0)
 			return this;
 
 		// We expect the common case to be that people do not have these
 		// characters in their XML so we make a pass through the char
 		// array and check.  If they're all good, we can call the
 		// underlying Writer once with the entire char array.  If we find
 		// a bad character then we punt it to the slow routine.
 
 		int i = 0;
 
 		loop: for (; i < length; i++) {
 			char c = characters[i + start];
 			switch (c) {
 				case '\"':
 					if (!isAttributeValue) { // no need to escape in regular content
 						continue;
 					}
 				case '&':
 				case '<':
 				case '>':
 					break loop;
 			}
 			if (c < 32) {
 				/*
 				 * All non-space white space needs to be encoded in attribute values; in normal text tabs and LFs can be
 				 * left as is, but everything else (including CRs) needs to be quoted
 				 */
 				if (isAttributeValue || (c != '\t' && c != '\n')) {
 					break loop;
 				}
 			} else if (c > 127) { // Above ascii range?
 				if (encoder != null && !encoder.canEncode(c)) {
 					break loop;
 				}
 			}
 		}
 
 		if (i < length) { // slow path
 			slowWriteCharacters(characters, start, length, isAttributeValue);
 		} else { // fast path
 			write(characters, start, length);
 		}
 		
 		return this;
 	}
 
 	private XmlWriter slowWriteCharacters(char chars[], int start, int length, boolean isAttributeValue) throws XmlException {
 		loop: for (int i = 0; i < length; i++) {
 			final char c = chars[i + start];
 			switch (c) {
 				case '&':
 					write("&amp;");
 					continue loop;
 				case '<':
 					write("&lt;");
 					continue loop;
 				case '>':
 					write("&gt;");
 					continue loop;
 				case '\"':
 					if (isAttributeValue) {
 						write("&quot;");
 						continue loop;
 					}
 					break;
 				default:
 					if (c < 32) {
 						if (isAttributeValue || (c != '\t' && c != '\n')) {
 							write("&#");
 							write(Integer.toString(c));
 							write(';');
 							continue loop;
 						}
 					} else if (c > 127 && encoder != null && !encoder.canEncode(c)) {
 						write("&#");
 						write(Integer.toString(c));
 						write(';');
 						continue loop;
 					}
 			}
 			write(c);
 		}
 		return this;
 	}
 
 	protected void closeStartElement() throws XmlException {
 		if (startElementOpened) {
 			closeStartTag();
 			startElementOpened = false;
 		}
 	}
 
 	protected boolean isOpen() {
 		return startElementOpened;
 	}
 
 	protected void closeStartTag() throws XmlException {
 		flushNamespace();
 		clearNeedsWritingNs();
 		if (isEmpty) {
 			write("/>");
 			isEmpty = false;
 		} else
 			write(">");
 	}
 
 	private void openStartElement() throws XmlException {
 		if (startElementOpened) {
 			closeStartTag();
 		} else {
 			startElementOpened = true;
 		}
 	}
 
 	protected String writeName(String prefix, String namespaceURI, String localName) throws XmlException {
 		if (!("".equals(namespaceURI)))
 			prefix = getPrefixInternal(namespaceURI);
 		if (!("".equals(prefix))) {
 			write(prefix);
 			write(":");
 		}
 		write(localName);
 		return prefix;
 
 	}
 
 	private String getPrefixInternal(String namespaceURI) {
 		String prefix = context.getPrefix(namespaceURI);
 		if (prefix == null) {
 			return "";
 		}
 		return prefix;
 	}
 
 	protected String getURIInternal(String prefix) {
 		String uri = context.getNamespaceURI(prefix);
 		if (uri == null) {
 			return "";
 		}
 		return uri;
 	}
 
 	protected void openStartTag() throws XmlException {
 		write("<");
 	}
 
 	private boolean needToWrite(String uri) {
 		if (needToWrite == null) {
 			needToWrite = new HashSet();
 		}
 		boolean needs = needToWrite.contains(uri);
 		needToWrite.add(uri);
 		return needs;
 	}
 
 	private void prepareNamespace(String uri) throws XmlException {
 		if (!isPrefixDefaulting)
 			return;
 		if ("".equals(uri))
 			return;
 		String prefix = getPrefix(uri);
 		// if the prefix is bound then we can ignore and return
 		if (prefix != null)
 			return;
 
 		defaultPrefixCount++;
 		prefix = "ns" + defaultPrefixCount;
 		setPrefix(prefix, uri);
 	}
 
 	//    private void removeNamespace(String uri) {
 	//        if (!isPrefixDefaulting || needToWrite == null) return;
 	//        needToWrite.remove(uri);
 	//    }
 
 	private void flushNamespace() throws XmlException {
 		if (!isPrefixDefaulting || needToWrite == null)
 			return;
 		Iterator i = needToWrite.iterator();
 		while (i.hasNext()) {
 			String uri = (String) i.next();
 			String prefix = context.getPrefix(uri);
 			if (prefix == null) {
 				throw new XmlException("Unable to default prefix with uri:" + uri);
 			}
 			namespace(prefix, uri);
 		}
 		needToWrite.clear();
 	}
 
 	protected XmlWriter writeStartElementInternal(String namespaceURI, String localName) throws XmlException {
 		if (namespaceURI == null)
 			throw new IllegalArgumentException("The namespace URI may not be null");
 		if (localName == null)
 			throw new IllegalArgumentException("The local name  may not be null");
 
 		openStartElement();
 		openStartTag();
 		prepareNamespace(namespaceURI);
 		prefixStack.push(writeName("", namespaceURI, localName));
 		localNameStack.push(localName);
 		uriStack.push(namespaceURI);
 		
 		return this;
 	}
 	
 	private void clearNeedsWritingNs() {
 		setNeedsWritingNs.clear();
 	}
 
 	private boolean needsWritingNs(String prefix) {
 		boolean needs = !setNeedsWritingNs.contains(prefix);
 		if (needs) {
 			setNeedsWritingNs.add(prefix);
 		}
 		return needs;
 	}
 
 	protected void writeDTD(String dtd) throws XmlException {
 		write(dtd);
 	}
 
 	protected void writeEntityRef(String name) throws XmlException {
 		closeStartElement();
 		write("&");
 		write(name);
 		write(";");
 	}
 
 	protected void writeCharacters(char[] text, int start, int len) throws XmlException {
 		closeStartElement();
 		writeCharactersInternal(text, start, len, false);
 	}
 	
 	public String getPrefix(String uri) throws XmlException {
 		return context.getPrefix(uri);
 	}
 
 	public void setPrefix(String prefix, String uri) throws XmlException {
 		//if() {
 		needToWrite(uri);
 		context.bindNamespace(prefix, uri);
 		//}
 	}
 
 	public void setDefaultNamespace(String uri) throws XmlException {
 		needToWrite(uri);
 		context.bindDefaultNameSpace(uri);
 	}
 
 	public void setNamespaceContext(NamespaceContext context) throws XmlException {
 		if (context == null)
 			throw new NullPointerException("The namespace " + " context may" + " not be null.");
 		this.context = new NamespaceContextImpl(context);
 	}
 
 	public NamespaceContext getNamespaceContext() {
 		return context;
 	}
 	
 	protected void openEndTag() throws XmlException {
 		write("</");
 	}
 
 	protected void closeEndTag() throws XmlException {
 		write(">");
 	}
 
 	private static final class Symbol {
 		String	name;
 		String	value;
 		int		depth;
 
 		Symbol(String name, String value, int depth) {
 			this.name = name;
 			this.value = value;
 			this.depth = depth;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getValue() {
 			return value;
 		}
 
 		//		public int getDepth() {
 		//			return depth;
 		//		}
 
 		public String toString() {
 			return ("[" + depth + "][" + name + "][" + value + "]");
 		}
 	}
 
 	/**
 	 * Maintains a table for namespace scope
 	 * 
 	 * 
 	 * values = map from strings to stacks [a]->[value1,0],[value2,0]
 	 * 
 	 * table = a stack of bindings
 	 */
 	private static final class SymbolTable {
 		private int		depth;
 		private Stack	table;
 		private Map		values;
 
 		public SymbolTable() {
 			depth = 0;
 			table = new Stack();
 			values = new HashMap();
 		}
 
 		//		public void clear() {
 		//			depth = 0;
 		//			table.clear();
 		//			values.clear();
 		//		}
 		//
 		//		//
 		//		// Gets the current depth
 		//		//
 		//		public int getDepth() {
 		//			return depth;
 		//		}
 		//
 		//		public boolean withinElement() {
 		//			return depth > 0;
 		//		}
 
 		//
 		// Adds a name/value pair
 		//
 		public void put(String name, String value) {
 			table.push(new Symbol(name, value, depth));
 			if (!values.containsKey(name)) {
 				Stack valueStack = new Stack();
 				valueStack.push(value);
 				values.put(name, valueStack);
 			} else {
 				Stack valueStack = (Stack) values.get(name);
 				valueStack.push(value);
 			}
 		}
 
 		//
 		// Gets the value for a variable
 		//
 		public String get(String name) {
 			Stack valueStack = (Stack) values.get(name);
 			if (valueStack == null || valueStack.isEmpty())
 				return null;
 			return (String) valueStack.peek();
 		}
 
 		public Set getAll(String name) {
 			HashSet result = new HashSet();
 			Iterator i = table.iterator();
 			while (i.hasNext()) {
 				Symbol s = (Symbol) i.next();
 				if (name.equals(s.getName()))
 					result.add(s.getValue());
 			}
 			return result;
 		}
 
 		//
 		// add a new highest level scope to the table
 		// 
 		public void openScope() {
 			depth++;
 		}
 
 		//
 		// remove the highest level scope from the table
 		// 
 
 		public void closeScope() {
 			// Get the top binding
 			Symbol symbol = (Symbol) table.peek();
 			int symbolDepth = symbol.depth;
 
 			// check if it needs to be popped of the table
 			while (symbolDepth == depth && !table.isEmpty()) {
 				symbol = (Symbol) table.pop();
 
 				// pop its value as well
 				Stack valueStack = (Stack) values.get(symbol.name);
 				valueStack.pop();
 
 				// check the next binding
 				if (!table.isEmpty()) {
 					symbol = (Symbol) table.peek();
 					symbolDepth = symbol.depth;
 				} else
 					break;
 			}
 			depth--;
 		}
 
 		public String toString() {
 			Iterator i = table.iterator();
 			String retVal = "";
 			while (i.hasNext()) {
 				Symbol symbol = (Symbol) i.next();
 				retVal = retVal + symbol + "\n";
 			}
 			return retVal;
 		}
 	}
 
 	private static final class NamespaceContextImpl implements NamespaceContext {
 		SymbolTable		 prefixTable	= new SymbolTable();
 		SymbolTable		 uriTable		= new SymbolTable();
 		NamespaceContext	rootContext;
 
 		public NamespaceContextImpl() {
 			init();
 		}
 
 		public NamespaceContextImpl(NamespaceContext rootContext) {
 			this.rootContext = null;
 			init();
 		}
 
 		public void init() {
 			bindNamespace("xml", "http://www.w3.org/XML/1998/namespace");
 			bindNamespace("xmlns", "http://www.w3.org/XML/1998/namespace");
 		}
 
 		public void openScope() {
 			prefixTable.openScope();
 			uriTable.openScope();
 		}
 
 		public void closeScope() {
 			prefixTable.closeScope();
 			uriTable.closeScope();
 		}
 
 		public void bindNamespace(String prefix, String uri) {
 			prefixTable.put(prefix, uri);
 			uriTable.put(uri, prefix);
 		}
 
 		//		public int getDepth() {
 		//			return prefixTable.getDepth();
 		//		}
 
 		public String getNamespaceURI(String prefix) {
 			String value = prefixTable.get(prefix);
 			if (value == null && rootContext != null)
 				return rootContext.getNamespaceURI(prefix);
 			else
 				return value;
 		}
 
 		public String getPrefix(String uri) {
 			String value = uriTable.get(uri);
 			if (value == null && rootContext != null)
 				return rootContext.getPrefix(uri);
 			else
 				return value;
 		}
 
 		public void bindDefaultNameSpace(String uri) {
 			bindNamespace("", uri);
 		}
 
 		//		public void unbindDefaultNameSpace() {
 		//			bindNamespace("", null);
 		//		}
 		//
 		//		public void unbindNamespace(String prefix, String uri) {
 		//			prefixTable.put(prefix, null);
 		//			prefixTable.put(uri, null);
 		//		}
 		//
 		//		public String getDefaultNameSpace() {
 		//			return getNamespaceURI("");
 		//		}
 
 		public Iterator getPrefixes(String uri) {
 			return (uriTable.getAll(uri)).iterator();
 		}
 	}
 }
