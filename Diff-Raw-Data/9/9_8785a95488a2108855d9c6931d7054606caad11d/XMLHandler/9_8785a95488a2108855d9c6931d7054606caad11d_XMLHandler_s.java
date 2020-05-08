 package org.MMiett.tvlistings;
 
 import org.xml.sax.helpers.DefaultHandler;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.widget.TextView;
 
 /**
  * 
  */
 
 /**
 * @author MMIETT200
  *
  */
 public class XMLHandler extends DefaultHandler {
 	//list for imported Item data
 	private ArrayList<TextView> theViews;
 	//string to track each entry
 	private String currChannel = "";
 	//flag to keep track of XML processing
 	private boolean isItem = false;
 	//context for user interface
 	private Context theContext;
 	//constructor
 	public XMLHandler(Context cont) {
 	    super();
 	    theViews = new ArrayList<TextView>();
 	    theContext = cont;
 	}
 	//start of the XML document
 	public void startDocument () { Log.i("XMLHandler", "Start of XML document"); }
 
 	//end of the XML document
 	public void endDocument () { Log.i("XMLHandler", "End of XML document"); }
 
 	//opening element tag
 	public void startElement (String uri, String name, String qName, Attributes atts)
 	{
 	    //handle the start of an element
 		//find out if the element is a Channel
 		if(qName.equals("channel"))
 		{
 		    //set Item tag to false
 		    isItem = false;
 		    //create View item for Channel display
 		    TextView ChannelView = new TextView(theContext);
 		    ChannelView.setTextColor(Color.rgb(73, 136, 83));
 		    //add the attribute value to the displayed text
 		    String viewText = "Items from " + atts.getValue("name") + ":";
 		    ChannelView.setText(viewText);
 		    //add the new view to the list
 		    theViews.add(ChannelView);
 		}
 		//the element is a Item
 		else if(qName.equals("item"))
 		    isItem = true;
 	}
 
 	//closing element tag
 	public void endElement (String uri, String name, String qName)
 	{
 	    //handle the end of an element
 		if(qName.equals("channel"))
 		{
 		    //create a View item for the Items
 		    TextView ItemView = new TextView(theContext);
 		    ItemView.setTextColor(Color.rgb(192, 199, 95));
 		    //display the compiled items
 		    ItemView.setText(currChannel);
 		    //add to the list
 		    theViews.add(ItemView);
 		    //reset the variable for future items
 		    currChannel = "";
 		}
 	}
 
 	//element content
 	public void characters (char ch[], int start, int length)
 	{
 	    //process the element content
 		//string to store the character content
 		String currText = "";
 		//loop through the character array
 		for (int i=start; i<start+length; i++)
 		{
 		    switch (ch[i]) {
 		    case '\\':
 		        break;
 		    case '"':
 		        break;
 		    case '\n':
 		        break;
 		    case '\r':
 		        break;
 		    case '\t':
 		        break;
 		    default:
 		        currText += ch[i];
 		        break;
 		    }
 		}
 		//prepare for the next item
 		if(isItem && currText.length()>0)
 		    currChannel += currText+"\n";
 	}
 	
 	public ArrayList<TextView> getData()
 	{
 	    //take care of SAX, input and parsing errors
 	    try
 	    {
 	            //set the parsing driver
 	        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
 	            //create a parser
 	        SAXParserFactory parseFactory = SAXParserFactory.newInstance();
 	        SAXParser xmlParser = parseFactory.newSAXParser();
 	            //get an XML reader
 	        XMLReader xmlIn = xmlParser.getXMLReader();
 	            //instruct the app to use this object as the handler
 	        xmlIn.setContentHandler(this);
 	            //provide the name and location of the XML file **ALTER THIS FOR YOUR FILE**
 	        URL xmlURL = new URL("https://dl.dropbox.com/u/23674870/weirdstuff/rss.xml");
 	            //open the connection and get an input stream
 	        URLConnection xmlConn = xmlURL.openConnection();
 	        InputStreamReader xmlStream = new InputStreamReader(xmlConn.getInputStream());
 	            //build a buffered reader
 	        BufferedReader xmlBuff = new BufferedReader(xmlStream);
 	            //parse the data
 	        xmlIn.parse(new InputSource(xmlBuff));
 	    }
 	    catch(SAXException se) { Log.e("AndroidTestsActivity", 
 	            "SAX Error " + se.getMessage()); }
 	    catch(IOException ie) { Log.e("AndroidTestsActivity", 
 	            "Input Error " + ie.getMessage()); }
 	    catch(Exception oe) { Log.e("AndroidTestsActivity", 
 	            "Unspecified Error " + oe.getMessage()); }
 	        //return the parsed Item list
 	    return theViews;
 	}
 
 }
