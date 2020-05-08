 package org.vamdc.validator.validator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.map.MultiValueMap;
 import org.vamdc.validator.interfaces.DocumentElement;
 import org.vamdc.validator.interfaces.DocumentElementsLocator;
 import org.vamdc.validator.interfaces.DocumentError;
 import org.vamdc.validator.interfaces.DocumentElement.ElementTypes;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 
 public class ElementHandler implements ContentHandler,ErrorHandler, DocumentElementsLocator{
 
 	private MultiValueMap elements; //Map that keeps all elements we're interested in
 	private Map<String,Element> lastElements;	//Map that keeps elements last encountered.
 	private Locator locator;
 	private int myLine;
 	private int myCol;
 	private List<DocumentError> allErrors;
 	private List<Error> newErrors;
 	
 	public ElementHandler(){
 		elements = new MultiValueMap();
 		lastElements = new HashMap<String,Element>();
 		allErrors = new ArrayList<DocumentError>();
 		myLine=1;
 		myCol=1;
 	}
 	
 	private static class Element extends DocumentElement{
 		private String name;
 		private int startLine,endLine;
 		private int startCol,endCol;
 		
 		/**
 		 * Constructor, sets name and begin position of element.
 		 * @param line
 		 * @param col
 		 * @param name
 		 * @param comments
 		 */
 		public Element(int line, int col, String name){
 			this.startLine = line;
 			this.startCol = col;
 			this.name = name;
 		}
 		
 		/**
 		 * Set end position of this element
 		 * @param line end line
 		 * @param col end column
 		 */
 		public void setEnd(int line, int col){
 			this.endLine = line;
 			this.endCol = col;
 		}
 
 		@Override
 		public int getLastCol() {
 			return endCol;
 		}
 
 		@Override
 		public long getLastLine() {
 			return endLine;
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public int getFirstCol() {
 			return startCol;
 		}
 
 		@Override
 		public long getFirstLine() {
 			return startLine;
 		}
 
 		
 		
 	}
 	
 	private static class Error implements DocumentError{
 
 		private Element element;
 		private String errorMessage;
		private String errorClass;
 		private Type myType=Type.element;
 		private String mySearch="";
 		
 		
 		public Error(SAXParseException e){
 			errorMessage = e.getMessage();
 			handleErrorMessage();
 			Element pos = new Element(e.getLineNumber(),e.getColumnNumber(),"element");
 			pos.setEnd(e.getLineNumber(), e.getColumnNumber());
 			this.setElement(pos);
 		}
 		
 		public void setElement(Element el){
 			element = el;
 		}
 		
 		@Override
 		public DocumentElement getElement() {
 			return element;
 		}
 
 		@Override
 		public String getMessage() {
 			return errorMessage;
 		}
 
 		@Override
 		public String getSearchString() {
 			return mySearch;
 		}
 
 		@Override
 		public Type getType() {
 			return myType;
 		}
 		
 		private void handleErrorMessage(){
			String[] einf=errorMessage.split(":",2);
			errorMessage=einf[1];
			errorClass=einf[0];
			
			if (errorClass.startsWith("cvc-id.")){
 				myType=Type.search;
 				mySearch=errorMessage.split("'")[1];
 			}
 		}
 	}
 	
 	@Override
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		updateLocation();
 	}
 
 	@Override
 	public void endDocument() throws SAXException {
 	}
 	
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes atts) throws SAXException {
 		Element el = new Element(myLine,myCol,localName);
 		
 		//Check if there are errors, attach element, save errors
 		if (newErrors!=null){
 			for (Error err:newErrors)
 				err.setElement(el);
 			allErrors.addAll(newErrors);
 			newErrors=null;
 		}
 		
 		//Save element position
 		lastElements.put(localName, el);
 		//Save element if we are searching through this type.
 		try{
 			ElementTypes elType = ElementTypes.valueOf(localName);
 			elements.put(elType, el);
 		}catch (IllegalArgumentException e){
 			//Ignore if element is not listed in types
 		}
 		
 		updateLocation();
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		Element last = lastElements.get(localName);
 		if (last!=null && locator!=null){
 			last.setEnd(locator.getLineNumber(), locator.getColumnNumber());
 			//Check if there are errors, attach element, save errors
 			if (newErrors!=null){
 				for (Error err:newErrors)
 					err.setElement(last);
 				allErrors.addAll(newErrors);
 				newErrors=null;
 			}
 		};
 		updateLocation();
 	}
 
 	@Override
 	public void endPrefixMapping(String prefix) throws SAXException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void ignorableWhitespace(char[] ch, int start, int length)
 			throws SAXException {
 		
 		updateLocation();
 	}
 
 	@Override
 	public void processingInstruction(String target, String data)
 			throws SAXException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setDocumentLocator(Locator locator) {
 		this.locator = locator;
 	}
 
 	@Override
 	public void skippedEntity(String name) throws SAXException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void startDocument() throws SAXException {
 		updateLocation();
 	}
 
 	@Override
 	public void startPrefixMapping(String prefix, String uri)
 			throws SAXException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Update location where next element can start
 	 */
 	private void updateLocation(){
 		myLine = locator.getLineNumber();
 		myCol = locator.getColumnNumber();
 	}
 	
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
 	 */
 	@Override
 	public void error(SAXParseException exception) throws SAXException {
 		if (newErrors == null)
 			newErrors = new ArrayList<Error>();
 		newErrors.add(new Error(exception));
 	}
 	@Override
 	public void fatalError(SAXParseException exception) throws SAXException {
 		System.out.print("Fatal");
 		error(exception);
 	}
 	@Override
 	public void warning(SAXParseException exception) throws SAXException {
 		System.out.print("Warning");
 		error(exception);
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<DocumentElement> getElements(ElementTypes type) {
 		List<DocumentElement> list = (List<DocumentElement>) elements.getCollection(type);
 		if (list == null) return new ArrayList<DocumentElement>();
 		return list;
 	}
 
 	@Override
 	public List<DocumentError> getErrors() {
 		return allErrors;
 	}
 
 
 
 
 	
 	
 }
