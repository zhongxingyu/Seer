 package ca.b02.a01.dbctrl.xmlhandler;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import ca.b02.a01.dbctrl.Signal;
 
 public class SignalHandler extends DefaultHandler {
 	private String currentElement;
 	public Signal signal = new Signal();
 
 	@Override
 	public void startDocument() throws SAXException {
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		currentElement = qName;
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 		if (currentElement.equalsIgnoreCase("e2snrdb")) {
 			String db = new String(ch, start, length);
 			signal.setDb(Float.parseFloat(db.subSequence(3, db.length() - 5)
 					.toString()));
 		}
 		if (currentElement.equalsIgnoreCase("e2snr")) {
 			String snr = new String(ch, start, length);
 			signal.setSnr(Integer.parseInt(snr.subSequence(3, snr.length() - 4)
 					.toString()));
 		}
 		if (currentElement.equalsIgnoreCase("e2ber")) {
 			String ber = new String(ch, start, length);
 			// FIXME: Why +2? Why is there an empty e2ber event?
 			if (ber.length() > 2) {
 				signal.setBer(Integer.parseInt(ber.subSequence(3, ber.length())
 						.toString()));
 			}
 		}
 		if (currentElement.equalsIgnoreCase("e2acg")) {
 			String acg = new String(ch, start, length);
 			// FIXME: Why +3? Why is there an empty e2acg event?
 			if (acg.length() > 3) {
				signal.setAcg(Integer.parseInt(acg.subSequence(0,
						acg.length() - 4).toString()));
 			}
 		}
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		currentElement = "";
 	}
 
 	@Override
 	public void endDocument() throws SAXException {
 	}
 }
