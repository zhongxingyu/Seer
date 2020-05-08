 /*
  * Copyright 2007, 2008, 2009, 2010, 2011, 2012 GoogleTransitDataFeed
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package transxchange2GoogleTransit.handler;
 import java.io.IOException;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXParseException;
 
 /*
  * Abstract superclass to cover transxchange data aspects (subclasses: TransxchangeAgency, TransxchangeStops etc.
  */
 public abstract class TransxchangeDataAspect {
 
 	String key = ""; // general key
 	String keyNested = ""; // nested key
 	String niceString = ""; // parsed string
 	boolean activeStartElement = false; // control flag to skip characters outside start/endElement()
 
 	TransxchangeHandlerEngine handler;
 
 	public void startElement(String uri, String name, String qName, Attributes atts)
 		throws SAXParseException
 	{
 		if (handler.getParseError().length() > 0)
 			throw new SAXParseException(handler.getParseError(), null);
 		niceString = "";
 	}
 
 	public void endElement (String uri, String name, String qName) {
 	}
 
 	public void clearKeys (String qName) {
 	}
 
 	public void characters (char ch[], int start, int length) {
 		if (key.length() > 0) {
 		  StringBuffer buffer = new StringBuffer(niceString);
 			for (int i = start; i < start + length; i++) {
 				buffer.append(ch[i]);
 			}
 			niceString = buffer.toString();
 		}
 	}
 
 	public void endDocument()
 		throws IOException {
 	}
 
 	public void completeData() {
 	}
 
 	public void dumpValues() {
 	}
 
 	/*
 	 * Read time in transxchange specific format
 	 */
 	static void readTransxchangeTime(int[] timehhmmss, String inString) {
 		StringTokenizer st = new StringTokenizer(inString, ":");
 		int i = 0;
 		while (st.hasMoreTokens() && i < 3) {
 			timehhmmss[i] = Integer.parseInt(st.nextToken());
 			i++;
 		}
 	}
 
 	/*
 	 * Read frequency in transxchange specific format
 	 */
 	static int readTransxchangeFrequency(String inString) {
 		int freq = 0;
 
 		inString = inString.substring(2, inString.length()); // Skip "PT"
 		if (inString.indexOf('H') > 0) { // Hours
 			StringTokenizer st = new StringTokenizer(inString, "H");
 			int i = 0;
 			while (st.hasMoreTokens() && i < 1) {
 				freq = Integer.parseInt(st.nextToken()) * 60 * 60;
 				i++;
 			}
 			inString = inString.substring(inString.indexOf('H') + 1, inString.length());
 		}
 		if (inString.indexOf('M') > 0) { // Minutes
 			StringTokenizer st = new StringTokenizer(inString, "M");
 			int i = 0;
 			while (st.hasMoreTokens() && i < 1) {
 				freq += Integer.parseInt(st.nextToken()) * 60;
 				i++;
 			}
 			inString = inString.substring(inString.indexOf('M') + 1, inString.length());
 		}
 		if (inString.indexOf('S') > 0) { // Seconds
 			StringTokenizer st = new StringTokenizer(inString, "S");
 			int i = 0;
 			while (st.hasMoreTokens() && i < 1) {
 				freq += Integer.parseInt(st.nextToken());
 				i++;
 			}
 		}
 		return freq;
 	}
 
 	/*
 	 * Read date in transxchange specific format
 	 */
 	static String readTransxchangeDate(String inString) {
 		StringTokenizer st = new StringTokenizer(inString, "-");
 		String ret = "";
 		int i = 0;
		while (st.hasMoreTokens() && i < 3) {
 			ret = ret + st.nextToken();
			++i;
 		}
 		return ret;
 	}
 
 	/*
 	 * CSV-"proof" field
 	 */
 	static void csvProofList(List<ValueList> values) {
 		int i, j;
 		String s;
 		ValueList iterator;
 
 		for (i = 0; i < values.size(); i++) {
 		    iterator = values.get(i);
 		    for (j = 0; j < iterator.size(); j++) {
 		    	s = iterator.getValue(j);
 		    	if (s != null) { // v1.6.3: may contain null value
 			    	if (s.lastIndexOf(",") != -1) // || s.lastIndexOf("\"") != -1) // v1.7.2: Remove addition of \" here, leads to duplication if \" used in configuration file
 			    		s = "\"" + s + "\"";
 			    	iterator.setValue(j, s);
 		    	} else {
 		    	  iterator.setValue(j, "");// set to empty string when null
 		    	}
 		    }
 		}
 	}
 
 	/*
 	 * Return date in GTFS format
 	 * introduced to support Java 1.4.2
 	 */
 	static String formatDate(int year, int month, int day_of_month) {
 
 		String result = Integer.toString(year);
 
 		String digis = Integer.toString(month);
 		if (digis.length() == 1)
 			digis = "0" + digis;
 		result = result + digis;
 
 		digis = Integer.toString(day_of_month);
 		if (digis.length() == 1)
 			digis = "0" + digis;
 		result = result + digis;
 
 		return result;
 	}
 
 	/*
 	 * Return time in GTFS format
 	 * introduced to support Java 1.4.2
 	 */
 	static String formatTime(int hour, int mins) {
 		String result = "";
 
 		String digis = Integer.toString(hour);
 		if (digis.length() == 1)
 			digis = "0" + digis;
 		result = result + digis;
 
 		result = result + ":";
 
 		digis = Integer.toString(mins);
 		if (digis.length() == 1)
 			digis = "0" + digis;
 		result = result + digis;
 
 		result = result + ":00";
 
 		return result;
 	}
 
 	TransxchangeDataAspect(TransxchangeHandlerEngine owner) {
 		handler = owner;
 	}
 }
