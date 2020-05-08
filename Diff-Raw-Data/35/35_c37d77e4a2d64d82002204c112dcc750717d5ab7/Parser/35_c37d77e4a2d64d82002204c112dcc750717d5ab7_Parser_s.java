 /* CS145 Fall 2012 */
 /* Parser skeleton for processing item-???.xml files. Must be compiled in
    JDK 1.4 or above. */
 
 /* Instructions:
 
    This program processes all files passed on the command line (to parse
    an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 
    At the point noted below, an individual XML file has been parsed into a
    DOM Document node. You should fill in code to process the node. Java's
    interface for the Document Object Model (DOM) is in package
    org.w3c.dom. The documentation is available online at
 
 http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/package-summary.html
 
 A tutorial of DOM can be found at:
 
 http://www.w3schools.com/dom/default.asp
 
 Some auxiliary methods have been written for you. You may find them
 useful.
 
  */
 
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.ErrorHandler;
 
 
 class Parser {
 
     // Any separator will do, but we recommend you do not use '|'
     static final String SEPARATOR = "<>";
     static final String NULL_STRING = "NULL";
     
     
 
     static DocumentBuilder builder;
 
     static final String[] typeName = {
         "none",
         "Element",
         "Attr",
         "Text",
         "CDATA",
         "EntityRef",
         "Entity",
         "ProcInstr",
         "Comment",
         "Document",
         "DocType",
         "DocFragment",
         "Notation",
     };
 
     static class MyErrorHandler implements ErrorHandler {
 
         public void warning(SAXParseException exception)
             throws SAXException {
                 fatalError(exception);
             }
 
         public void error(SAXParseException exception)
             throws SAXException {
                 fatalError(exception);
             }
 
         public void fatalError(SAXParseException exception)
             throws SAXException {
                 exception.printStackTrace();
                 System.out.println("There should be no errors " +
                         "in the supplied XML files.");
                 System.exit(3);
             }
 
     }
 
     /* Non-recursive (NR) version of Node.getElementsByTagName(...) */
     static Element[] getElementsByTagNameNR(Element e, String tagName) {
         Vector< Element > elements = new Vector< Element >();
         Node child = e.getFirstChild();
         while (child != null) {
             if (child instanceof Element && child.getNodeName().equals(tagName))
             {
                 elements.add( (Element)child );
             }
             child = child.getNextSibling();
         }
         Element[] result = new Element[elements.size()];
         elements.copyInto(result);
         return result;
     }
 
     /* Returns the first subelement of e matching the given tagName, or
      * null if one does not exist. */
     static Element getElementByTagNameNR(Element e, String tagName) {
         Node child = e.getFirstChild();
         while (child != null) {
             if (child instanceof Element && child.getNodeName().equals(tagName))
                 return (Element) child;
             child = child.getNextSibling();
         }
         return null;
     }
 
     /* Returns the text associated with the given element (which must have
      * type #PCDATA) as child, or "" if it contains no text. */
     static String getElementText(Element e) {
         if (e.getChildNodes().getLength() == 1) {
             Text elementText = (Text) e.getFirstChild();
             return elementText.getNodeValue();
         }
         else
             return "";
     }
 
     /* Returns the text (#PCDATA) associated with the first subelement X
      * of e with the given tagName. If no such X exists or X contains no
      * text, "" is returned. */
     static String getElementTextByTagNameNR(Element e, String tagName) {
         Element elem = getElementByTagNameNR(e, tagName);
         if (elem != null)
             return getElementText(elem);
         else
            return "";
     }
 
     /* Returns the amount (in XXXXX.xx format) denoted by a dollar-value
      * string like $3,453.23. Returns the input if the input is an empty
      * string. */
     static String formatDollar(String money) {
        if (money.equals(""))
             return money;
         else {
             double am = 0.0;
             NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
             try { am = nf.parse(money).doubleValue(); }
             catch (ParseException e) {
                 System.out.println("This method should work for all " +
                         "money values you find in our data.");
                 System.exit(20);
             }
             nf.setGroupingUsed(false);
             return nf.format(am).substring(1);
         }
     }
 
     /* Returns the time (in YYYY-MM-DD HH:MM:SS format) denoted by a
      * time string like Dec-31-01 23:59:59. */
     static String formatTime(String time) {
         DateFormat outputDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         DateFormat inputDf = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
         String result = "";
         try { result = outputDf.format(inputDf.parse(time)); }
         catch (ParseException e) {
             System.out.println("This method should work for all date/" +
                     "time strings you find in our data.");
             System.exit(20);
         }
         return result;
     }
 
     /* Process one items-???.xml file. */
    static void processFile(File xmlFile, Writers w, Map<String, UserFields> m) throws IOException {
 
         Document doc = null;
         try {
             doc = builder.parse(xmlFile);
         }
         catch (IOException e) {
             e.printStackTrace();
             System.exit(3);
         }
         catch (SAXException e) {
             System.out.println("Parsing error on file " + xmlFile);
             System.out.println("  (not supposed to happen with supplied XML files)");
             e.printStackTrace();
             System.exit(3);
         }
 
         /* At this point 'doc' contains a DOM representation of an 'Items' XML
          * file. Use doc.getDocumentElement() to get the root Element. */
         System.out.println("Successfully parsed - " + xmlFile);
 
         // Get the root of the tree
         Element root = doc.getDocumentElement();
 
         translateFile(root, w, m);
 
     }
 
     public static void main (String[] args) {
         if (args.length == 0) {
             System.out.println("Usage: java MyParser [file] [file] ...");
             System.exit(1);
         }
 
         /* Initialize parser. */
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(true);
             factory.setIgnoringElementContentWhitespace(true);      
             builder = factory.newDocumentBuilder();
             builder.setErrorHandler(new MyErrorHandler());
         }
         catch (FactoryConfigurationError e) {
             System.out.println("unable to get a document builder factory");
             System.exit(2);
         } 
         catch (ParserConfigurationException e) {
             System.out.println("parser was unable to be configured");
             System.exit(2);
         }
 
         /* Process each of the files */
         try {
         	
         	Writers w = new Writers();
         	Map<String, UserFields> m = new HashMap<String, UserFields>();
 
             /* Process all files listed on command line. */
             for (int i = 0; i < args.length; i++) {
                 File currentFile = new File(args[i]);
                 processFile(currentFile, w, m);
             }
             writeUserInfo(w, m);
             w.close();
 
             // Success!
             System.out.println("Success creating the SQL input files.");
 
         } catch (IOException e) {
             System.err.println("Error: " + e.getMessage());
         }
     }
     
     
     /* Data translation code*/
     
     private static class Writers {
     	final PrintWriter item;
     	final PrintWriter category;
     	final PrintWriter user;
     	final PrintWriter bid;
     	
     	static final int ITEM = 0;
     	static final int CATEGORY = 1;
     	static final int USER = 2;
     	static final int BID = 3;
     	
     	static final String LOAD_DIRECTORY = "load_files";
     	static final String ITEM_LOAD_FILE = "load_files/item.dat";
     	static final String CATEGORY_LOAD_FILE = "load_files/category.dat";
     	static final String USER_LOAD_FILE = "load_files/user.dat";
     	static final String BID_LOAD_FILE = "load_files/bid.dat";
     	
     	
     	private Writers() throws IOException { 	
     		File dir = new File(LOAD_DIRECTORY);
     		if(!dir.exists()) dir.mkdir();
     		
         	item = new PrintWriter(new FileWriter(ITEM_LOAD_FILE));
         	category = new PrintWriter(new FileWriter(CATEGORY_LOAD_FILE));
         	user = new PrintWriter(new FileWriter(USER_LOAD_FILE));
         	bid = new PrintWriter(new FileWriter(BID_LOAD_FILE));
     	}
     	
     	private void write(int table, String s) {
     		PrintWriter w;
     		switch(table) {
     		case ITEM:
     			w = item; break;
     		case CATEGORY:
     			w = category; break;
     		case USER:
     			w = user; break;
     		case BID:
     			w = bid; break;
     		default:
     			return;
     		}
     		w.println(s.replaceAll("\"",""));
     	}
     	
     	private void close() {
     		item.close();
     		category.close();
     		user.close();
     		bid.close();
     	}
     }
     
     private static class UserFields {
     	final String rating;
     	final String location;
 		final String country;
 		
     	private UserFields(String rating, String location, String country) {
 			this.rating = rating;
 			this.location = location;
 			this.country = country;
 		}
     
     	
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			UserFields other = (UserFields) obj;
 			if (country == null) {
 				if (other.country != null)
 					return false;
 			} else if (!country.equals(other.country))
 				return false;
 			if (location == null) {
 				if (other.location != null)
 					return false;
 			} else if (!location.equals(other.location))
 				return false;
 			if (rating == null) {
 				if (other.rating != null)
 					return false;
 			} else if (!rating.equals(other.rating))
 				return false;
 			return true;
 		}
     	
     }
     
    private static void translateFile(Element root, Writers w, Map<String, UserFields> userInfo) throws IOException {
     	Element[] elements = getElementsByTagNameNR(root, "Item");
     	
     	for(Element e : elements) {
     		processItem(e, w, userInfo);
     	}
     }
     
     private static void processItem(Element item, Writers w, Map<String, UserFields> userInfo) {
     	String id = item.getAttribute("ItemID");
     	Element seller = getElementByTagNameNR(item, "Seller");
     	w.write(Writers.ITEM, 
     			id + SEPARATOR + 
     			getElementTextByTagNameNR(item, "Name") + SEPARATOR +
     			formatDollar(getElementTextByTagNameNR(item, "Currently")) + SEPARATOR + 
    			checkNull(formatDollar(getElementTextByTagNameNR(item, "Buy_Price"))) + SEPARATOR + 
     			formatDollar(getElementTextByTagNameNR(item, "First_Bid")) + SEPARATOR +
     			getElementTextByTagNameNR(item, "Number_of_Bids") + SEPARATOR +
     			formatTime(getElementTextByTagNameNR(item, "Started")) + SEPARATOR + 
     			formatTime(getElementTextByTagNameNR(item, "Ends")) + SEPARATOR + 
     			seller.getAttribute("UserID") + SEPARATOR +
     			getElementTextByTagNameNR(item, "Description"));  
     	mergeUser(seller.getAttribute("UserID"),
     			seller.getAttribute("Rating"),
     			getElementTextByTagNameNR(item, "Location"),
     			getElementTextByTagNameNR(item, "Country"), userInfo);
     	for(Element c : getElementsByTagNameNR(item, "Category")) {
     		w.write(Writers.CATEGORY, id + SEPARATOR + getElementText(c));
     	}
     	Element bids = getElementByTagNameNR(item, "Bids");
     	for(Element b : getElementsByTagNameNR(bids, "Bid")) {
     		Element bidder = getElementByTagNameNR(b, "Bidder");
     		w.write(Writers.BID,
     				id + SEPARATOR + 
     				bidder.getAttribute("UserID") + SEPARATOR +
     				formatTime(getElementTextByTagNameNR(b, "Time")) + SEPARATOR +
     				formatDollar(getElementTextByTagNameNR(b, "Amount")) + SEPARATOR + 
    				((getElementTextByTagNameNR(bidder, "Location") == null) ? 1 : 0));
     		mergeUser(bidder.getAttribute("UserID"),
     				bidder.getAttribute("Rating"),
     				getElementTextByTagNameNR(bidder, "Location"),
     				getElementTextByTagNameNR(bidder, "Country"), userInfo);
     	}
     }
     
     private static void writeUserInfo(Writers w, Map<String, UserFields> userInfo) {
     	for(String id : userInfo.keySet()) {
     		UserFields fields = userInfo.get(id);
     		w.write(Writers.USER,
    				id + SEPARATOR + checkNull(fields.rating) + SEPARATOR +
    				checkNull(fields.location) + SEPARATOR + checkNull(fields.country));
     	}
     }
     
     private static void mergeUser(String userID, String rating, String loc, String country,
     								Map<String, UserFields> m) {
     	UserFields info = m.get(userID);
     	if(info != null) {
    		if(!(info.location == null && loc != null) &&
    				!(info.country == null && country != null)) return;
     	}
     	m.put(userID, new UserFields(rating, loc, country));
     }
    
    private static String checkNull(String s){
    	return (s == null) ? NULL_STRING : s;
    }
     											
     
 
     
 }
