 package com.axiomalaska.sos.xmlbuilder;
 
 import java.io.StringWriter;
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 /**
  * A base class for building SOS XML
  * 
  * @author Lance Finfrock
  */
 public abstract class SosXmlBuilder {
 
	private SimpleDateFormat parseDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-0000");
	
 	public abstract String build();
 	
 	/**
 	 * Get a string value from document
 	 * 
 	 * @param doc - the document to build the string from. 
 	 * @return - the xml string from the document past in. 
 	 */
 	protected String getString(Document doc) throws TransformerException{
         //set up a transformer
         TransformerFactory transfac = TransformerFactory.newInstance();
         Transformer trans = transfac.newTransformer();
         trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
         //create string from xml tree
         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult(sw);
         DOMSource source = new DOMSource(doc);
         trans.transform(source, result);
         String xmlString = sw.toString();
         
         return xmlString;
 	}
 	
 	/**
 	 * Format calendar to 2012-04-17T20:02:05-0000
	 * @param calendar - date to be formated
 	 * @return
 	 */
 	protected String formatCalendarIntoGMTTime(Calendar calendar) {
 		Calendar copyCalendar = (Calendar) calendar.clone();
 		copyCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.set(Calendar.YEAR, copyCalendar.get(Calendar.YEAR));
		localCalendar.set(Calendar.MONTH, copyCalendar.get(Calendar.MONTH));
		localCalendar.set(Calendar.DAY_OF_MONTH,
				copyCalendar.get(Calendar.DAY_OF_MONTH));
		localCalendar.set(Calendar.HOUR_OF_DAY,
				copyCalendar.get(Calendar.HOUR_OF_DAY));
		localCalendar.set(Calendar.MINUTE, copyCalendar.get(Calendar.MINUTE));
		localCalendar.set(Calendar.SECOND, copyCalendar.get(Calendar.SECOND));

		return parseDate.format(localCalendar.getTime());
 	}
 }
