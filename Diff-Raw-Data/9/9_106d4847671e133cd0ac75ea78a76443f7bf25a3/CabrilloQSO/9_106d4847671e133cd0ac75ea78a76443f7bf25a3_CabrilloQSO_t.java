 package com.bsandersen.CabConverter;
 
 import java.util.Formatter;
 
 /**
  * This class holds the details for the Cabrillo QSO formatting as
  * presecribed by this contest's XML file. This also provides the
  * formatting capability to convert a set of ADIF QSO data into
  * a properly formatted QSO detail line.
  * 
  * @author B. Scott Andersen (NE1RD)
  */
 
 /* 
  * CabConverter by B. Scott Andersen (NE1RD) is licensed under a 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  */
 public class CabrilloQSO {
 	CabFileNode qsoDetailHead = null;
 	CabFileNode qsoDetailTail = null;
 	
 	/**
 	 * addElement adds the essence of the element to the QSO description.
 	 * If the element is a Text item, the type "Text" and the text element's
 	 * value is added. If the element is a reference to some data item, then
 	 * the data item like "Frequency" is added.
 	 * @param elementType the type of the element at this point in the QSO line
 	 * @param elementValue the value of the element at this point in the QSO line
 	 */
 	public void addElement(String elementType, String elementValue) {
 		CabFileNode e = new CabFileNode(elementType, elementValue);
 		if (qsoDetailHead != null) {
 			qsoDetailTail.setNext(e);
 			qsoDetailTail = e;
 		} else {
 			qsoDetailHead = e;
 			qsoDetailTail = e;
 		}
 	}
 	
 	/**
 	 * Using the QSO detail information supplied by the XML file, this
 	 * method will take the passed ADIF record (r) and format it into
 	 * a text string suitable for a Cabrillo file.
 	 * @param r The ADIF record to format
 	 * @return A properly formatted QSO: line for the Cabrillo file
 	 */
 	public String formatQSO(ADIFrecord r) {
 		String s;
 		String key;
 		CabFileNode e = qsoDetailHead;
 		Formatter f;
 		PersonalData personalData = PersonalData.getInstance();
 
 		s = "";
 		while (e != null) {
 			key = e.getType();
 
 			if (key.compareToIgnoreCase("TheirCallsign") == 0) {
 				s = s.concat(r.call + " ");
 			} else if (key.compareToIgnoreCase("MyCallsign") == 0) {
 				// Special case: we get this from the personal data
 				s = s.concat(personalData.getMyCallSign() + " ");
 			} else if (key.compareToIgnoreCase("Date") == 0) {
 				s = s.concat(r.date + " ");
 			} else if (key.compareToIgnoreCase("Time") == 0) {
 				s = s.concat(r.time + " ");
 			} else if (key.compareToIgnoreCase("Frequency") == 0) {
 				f = new Formatter();
 				long v = new Double(r.frequency * 1000.0).longValue();
 				f.format("%05d ", v);
 				s = s.concat(f.toString());
 				f.close();
 			} else if (key.compareToIgnoreCase("Mode") == 0) {
 				s = s.concat(mapMode(r.mode) + " ");
 			} else if (key.compareToIgnoreCase("RSTs") == 0) {
 				s = s.concat(r.rstSent + " ");
 			} else if (key.compareToIgnoreCase("RSTr") == 0) {
 				s = s.concat(r.rstReceived + " ");
 			} else if (key.compareToIgnoreCase("STXn") == 0) {
 				f = new Formatter();
 				f.format("%3d", r.serialNumberSent);
 				s = s.concat(new String(f.toString()) + " ");
 				f.close();
 			} else if (key.compareToIgnoreCase("SRXn") == 0) {
 				f = new Formatter();
 				f.format("%3d", r.serialNumberReceived);
 				s = s.concat(new String(f.toString()) + " ");
 				f.close();
 			} else if (key.compareToIgnoreCase("STX") == 0) {
 				if (r.exchangeSent.isEmpty()) {
 					s = s.concat(e.value() + " ");
 				} else {
 					s = s.concat(r.exchangeSent + " ");
 				}
 			} else if (key.compareToIgnoreCase("SRX") == 0) {
 				if (r.exchangeReceived.isEmpty()) {
 					s = s.concat(e.value() + " ");
 				} else {
 					s = s.concat(r.exchangeReceived + " ");
 				}
			} else if (key.compareToIgnoreCase("Text") == 0) {
				s = s.concat(e.value() + " ");
 			} else {
 				s = s.concat(key + " ");
 			}
 			e = e.getNext();
 		}
 		return s;
 	}
 	
 	/*
 	 * Map the mode passed to the mode suitable for the Cabrillo file
 	 * @param modeIn The string from the log file to be translated
 	 * @returns The mode string suitable for the Cabrillo file
 	 */
 	String mapMode(String modeIn) {
 		String s = new String("");
 		
 		if ((modeIn.compareToIgnoreCase("SSB") == 0) ||
 				(modeIn.compareToIgnoreCase("USB") == 0) ||
 				(modeIn.compareToIgnoreCase("LSB") == 0)) {
 				s = s.concat("PH");
 			} else if (modeIn.compareTo("AM") == 0) {
 				s = s.concat("AM");
 			} else if (modeIn.compareTo("FM") == 0) {
 				s = s.concat("FM");
 			} else if ((modeIn.compareToIgnoreCase("CW") == 0) ||
 					   (modeIn.compareToIgnoreCase("CWR") == 0)) {
 				s = s.concat("CW");
 			} else if (modeIn.compareTo("DV") == 0) {
 				s = s.concat("DV");
 			} else if ((modeIn.compareToIgnoreCase("PSK") == 0) ||
 				  (modeIn.compareToIgnoreCase("FSK") == 0) ||
 				  (modeIn.compareToIgnoreCase("FSK-R") == 0)) {
 				s = s.concat("PH");
 			} else if ((modeIn.compareToIgnoreCase("RTTY") == 0) ||
 					   (modeIn.compareToIgnoreCase("RTTY-R") == 0)) {
 				s = s.concat("RT");
 			} else {
 				s = s.concat(modeIn);
 			}
 		return s;
 	}
 }
