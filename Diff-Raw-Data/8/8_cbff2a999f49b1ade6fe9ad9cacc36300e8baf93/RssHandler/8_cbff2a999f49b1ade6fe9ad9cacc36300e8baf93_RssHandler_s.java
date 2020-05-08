 package com.nasaimageofday;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class RssHandler extends DefaultHandler {
 	private String urlString;
 	private String title;
 	private String date;
 	private StringBuffer description = new StringBuffer();
 	private Bitmap image;
 	private boolean inImageUrl = false;
 	private boolean inTitle = false;
 	private boolean inDescription = false;
 	private boolean inItem = false;
 	private boolean inDate = false;
 
 	public RssHandler(String url) {
 		this.urlString = url;
 	}
 
 	public void processFeed() {
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		SAXParser parser;
 		try {
 			parser = factory.newSAXParser();
 			XMLReader xmlReader = parser.getXMLReader();
 			xmlReader.setContentHandler(this);
 			URL url = new URL(urlString);
 			InputStream stream = url.openStream();
 			xmlReader.parse(new InputSource(stream));
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public String getDate() {
 		return this.date;
 	}
 
 	public StringBuffer getDescription() {
 		return this.description;
 	}
 
 	public Bitmap getImage() {
 		return this.image;
 	}
 
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		if (localName.startsWith("item")) {
 			inItem = true;
 		} else if (inItem) {
 			inTitle = localName.equals("title");
 			inDescription = localName.equals("description");
 			inDate = localName.equals("pubDate");
 			inImageUrl = localName.equals("enclosure");
 			if (inImageUrl && image == null) {
 				for (int i = 0; i < attributes.getLength(); i++) {
 					if (attributes.getQName(i).equals("url")) {
 						image = getBitmap(attributes.getValue(i));
 					}
 				}
 			}
 		}
 	}
 
 	public void characters(char ch[], int start, int length) {
 		String chars = new String(ch).substring(start, start + length);
 
 		if (inTitle && title == null) {
 			title = chars;
 		}
		if (inDescription) {
 			description = description.append(chars);
		}
		// if(inImageUrl && image == null){
		// image = getBitmap(chars);
		// }
 		if (inDate && date == null) {
 			date = chars;
 		}
 	}
 
 	private Bitmap getBitmap(String url) {
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(url)
 					.openConnection();
 			connection.setDoInput(true);
 			connection.connect();
 			InputStream stream = connection.getInputStream();
 			Bitmap bitmap = BitmapFactory.decodeStream(stream);
 			stream.close();
 			return bitmap;
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
