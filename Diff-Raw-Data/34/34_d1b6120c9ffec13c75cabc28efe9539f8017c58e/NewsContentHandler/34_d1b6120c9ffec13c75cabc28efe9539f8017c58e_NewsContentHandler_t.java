 package controllers.acquisition;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 /**
  * Liest eine &uuml;bergebene XML-Datei mittels SAX-Parser ein
  * und speichert die vorgegebenen extrahierten Werte.
  * @author Christian Ochsenk&uuml;hn
  * @version 1.0
  */
 public class NewsContentHandler implements ContentHandler {
 	
 	private String currentValue;
 	
 	private String newsPortal;
 	private String publicationDate;
 	private String urlSource;
 	private String urlPicture;
 	private StringBuffer textBuf;
 	private StringBuffer titleBuf;
 	
 	private boolean isInItem = false;
 	private boolean isEndOfDocument = false;
 	
 	private boolean isExtractedText = false;
 	private boolean isInTitle = false;
 	
 	public NewsContentHandler() {
 		newsPortal = "";
 		publicationDate = "";
 		urlSource = "";
 		urlPicture = "";
 		textBuf = new StringBuffer();
 	}
 
 	/**
 	 * Wird vom SAX-Parser selbst aufgerufen. Maskiert einige Sonderzeichen.
 	 * Speichert die aktuell ausgelesenen Daten in der Variable currentValue.
 	 */
 	@Override
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		StringBuffer buf = new StringBuffer();
 		
 		for (int i = start; i < start + length; i++) {
 		    switch (ch[i]) {
 		    case '\\':
 		    	buf.append("\\\\");
 		    	break;
 //		    case '"':
 //				buf.append("\\\"");
 //				break;
 		    case '\n':
 //		    	buf.append("\\n");
 		    	buf.append(" ");
 				break;
 		    case '\r':
 				buf.append("\\r");
 				break;
 		    case '\t':
 				buf.append("\\t");
 				break;
 		    default:
 				buf.append(ch[i]);
 				break;
 		    }
 		}
 		currentValue = buf.toString();
 		
 		if(isExtractedText)
 			textBuf.append(buf);
 		else if(isInTitle)
 			titleBuf.append(buf);
 	}
 
 	/**
 	 * Wird automatisch aufgerufen, wenn beim Parsen ein &ouml;ffnendes XML-Element auftaucht.
 	 */
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes attr) throws SAXException {
 		
 		if (localName.equals("item")) {
 			isInItem = true;
 		}
 		if(localName.equals("title") && isInItem) {
 			isInTitle = true;
 			titleBuf = new StringBuffer();
 		} else if (localName.equals("enclosure") && isInItem) {	// picture-url
 			urlPicture = attr.getValue("url");
 		} else if (localName.equals("ExtractedText") && isInItem) {
 			isExtractedText = true;
 		}
 	}
 
 	/**
 	 * Wird automatisch aufgerufen, wenn beim Parsen ein schlie&szlig;endes XML-Element auftaucht.
 	 * Speichert die zuvor gelesenen Inhalte jenes Elements in der passenden Variable.
 	 */
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		
 		if(isInItem) {
 			if (localName.equals("pubDate")) {
				if(currentValue!="")
					publicationDate = currentValue;
 			} else if (localName.equals("link")) {	
 				urlSource = currentValue;
 			} else if (localName.equals("title")) {			// article-title
 				isInTitle = false;
 			} else if (localName.equals("ExtractedText")) {
 				isExtractedText = false;
 			} 
 		} else {
 			if (localName.equals("title")) {				// newsPortal-title
 				newsPortal = currentValue;
			} else if (localName.equals("pubDate")) {
				publicationDate = currentValue;
 			}
 		}
 	}
 	
 	/**
 	 * Wird automatisch aufgerufen, wenn der Parser das Ende der XML-Datei erreicht hat.
 	 */
 	@Override
 	public void endDocument() throws SAXException {
 		isEndOfDocument = true;
 	}
 	
 	/**
 	 * Gibt alle gespeicherten Daten der XML-Datei als formattierten String zur&uuml;ck.
 	 * @return Alle gespeicherten Daten der XML-Datei
 	 */
 	public String getXMLString() {
 		return ("Portal: "+getNewsPortal()+"\nTitel: "+getTitle()+"\nDatum: "+getPublicationDate().toString()+"\nTeaser: "+getTeaser()+"\nSrc: "+getUrlSource()+"\nPic: "+getUrlPicture()+"\n\nText: "+getText());
 	}
 	
 	/**
 	 * Zeigt an, ob der Handler mit dem Auslesen der XML-Datei fertig ist.
 	 * @return True, wenn fertig mit Auslesen der XML-Datei
 	 */
 	public boolean hasStoppedReading() {
 		return isEndOfDocument;
 	}
 	
 	public void setIsEndOfDocument(boolean isEndofDocument) {
 		this.isEndOfDocument = isEndofDocument;
 	}
 	
 	/**
 	 * Gibt das ausgelesene Nachrichtenportal zur&uuml;ck.
 	 * @return	Nachrichtenportal
 	 */
 	public String getNewsPortal() {
 		return newsPortal;
 	}
 
 	/**
 	 * Gibt den ausgelesenen Titel des Artikels zur&uuml;ck.
 	 * @return Titel des Artikels
 	 */
 	public String getTitle() {
 		return titleBuf.toString();
 	}
 
 	/**
 	 * Gibt die ersten 230 Zeichen des extrahierten Texts als Teaser zur&uuml;ck.
 	 * @return 230 Zeichen langer Artikel-Teaser
 	 */
 	public String getTeaser() {
 		if(getText().length() > 230)
 			return getText().substring(0, 230)+"...";
 		else
 			return getText()+"...";
 	}
 
 	/**
 	 * Wandelt Datum aus der XML-Datei in Java Date Objekt um und gibt es zur&uuml;ck.
 	 * @return Ver&ouml;ffentlichungsdatum des Artikels der XML-Datei
 	 */
 	public Date getPublicationDate() {
 		Date pubDate;
 		try {
 			// example: Sun, 06 Oct 2013 20:58:57 +0200
 			pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(publicationDate);
 		} catch (ParseException e) {
 			System.out.println("NewsContentHandler.java: Could not parse date ("+publicationDate+")");
 			try {
 				// example: Sun, 06 Oct 2013 20:58:57 GMT
 				pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(publicationDate);
 				System.out.println("NewsContentHandler.java: Next try parsing date succeeded.");
 			} catch (ParseException e1) {
 				System.out.println("NewsContentHandler.java: Could not parse date, again!");
 				try {
 					// example: Sun, 06 Oct 2013 20:58:57
 					pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH).parse(publicationDate);
 					System.out.println("NewsContentHandler.java: Next try parsing date succeeded.");
 				} catch (ParseException e2) {
 					System.out.println("NewsContentHandler.java: Could not parse date, again! Using current date.");
 					pubDate = Calendar.getInstance().getTime();
 				}
 			}
 		}
 		return pubDate;
 	}
 
 	/**
 	 * Gibt die URL des urspr&uuml;nglichen Artikels zur&uuml;ck.
 	 * @return URL des Artikels
 	 */
 	public String getUrlSource() {
 		return urlSource;
 	}
 
 	/**
 	 * Gibt - wenn vorhanden - die URL des Artikel-Bildes zur&uuml;ck.
 	 * @return URL des Artikel-Bilds
 	 */
 	public String getUrlPicture() {
 		return urlPicture;
 	}
 
 	/**
 	 * Gibt den Nachrichtentext des Artikels zur&uuml;ck.
 	 * @return Nachrichtentext des Artikels
 	 */
 	public String getText() {
 		return textBuf.toString();
 	}
 	
 
 	@Override
 	public void endPrefixMapping(String arg0) throws SAXException { }
 
 	@Override
 	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException { }
 
 	@Override
 	public void processingInstruction(String arg0, String arg1) throws SAXException { }
 
 	@Override
 	public void setDocumentLocator(Locator arg0) { }
 
 	@Override
 	public void skippedEntity(String arg0) throws SAXException { }
 
 	@Override
 	public void startDocument() throws SAXException { }
 
 	@Override
 	public void startPrefixMapping(String arg0, String arg1) throws SAXException { }
 
 }
