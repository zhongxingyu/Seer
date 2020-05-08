 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import java.io.*;
 import java.util.*;
 
 
 public class csvBuilder extends DefaultHandler {
 	static XMLReader xr;
 	static FileReader dataReader;
	public static int orderCounter = 1;
 	
 	public BufferedWriter outputWriter;
 	public FileWriter fileOut;
 	
 	public static Calendar todayNow;
 	public static String yyyy;
 	public static String mm;
 	public static String dd;
 	public static String hh;
 	public static String min;
 
 	public String temp;
 	public String soldTo;
 	public String shipTo;
 	public String pO;
 	public String pN;
 	public String ean;
 	public String qty;
 	public String price;
 	public int lineNum;
 	
 	public static void main() {
 		
 		try {
 			todayNow = Calendar.getInstance();
 			yyyy = String.valueOf(todayNow.get(Calendar.YEAR));
 			if ((todayNow.get(Calendar.MONTH)+1) < 10) {
 				mm = "0" + String.valueOf((todayNow.get(Calendar.MONTH)+1));
 			} else {
 				mm = String.valueOf((todayNow.get(Calendar.MONTH)+1));
 			}
 			if (todayNow.get(Calendar.DAY_OF_MONTH) < 10) {
 				dd = "0" + String.valueOf(todayNow.get(Calendar.DAY_OF_MONTH));
 			} else {
 				dd = String.valueOf(todayNow.get(Calendar.DAY_OF_MONTH));
 			}
 			if (todayNow.get(Calendar.HOUR_OF_DAY) < 10) {
 				hh = "0" + String.valueOf(todayNow.get(Calendar.HOUR_OF_DAY));
 			} else {
 				hh = String.valueOf(todayNow.get(Calendar.HOUR_OF_DAY));
 			}
 			if (todayNow.get(Calendar.MINUTE) < 10) {
 				min = "0" + String.valueOf(todayNow.get(Calendar.MINUTE));
 			} else {
 				min = String.valueOf(todayNow.get(Calendar.MINUTE));
 			}
 			
 			xr = XMLReaderFactory.createXMLReader();
 			xr.setContentHandler(new csvBuilder());
 						
 			dataReader = new FileReader("/Users/gustavopinheiro/Desktop/data.xml");
 			xr.parse(new InputSource(dataReader));
 			     
 			
 		} catch (IOException ioe) {
 			System.out.println("IOException in xlsxBuilder.main " + ioe);
 		} catch (SAXException saxe) {
 			System.out.println("SAXException in xlsxBuilder.main " + saxe.getMessage());
 		}
 	}
 
 	/**Event Handlers
 	 */
 	public void startDocument() throws SAXException {
 		
     }
 
 
     public void endDocument()  throws SAXException{
     	 
     }
 
     public void startElement(String uri, String name,
 			      String qName, Attributes atts) throws SAXException {
  
 	if ("Batch".equals (qName)) {
 	
     } else if ("Order".equals (qName)) {
     	try {
         	fileOut = new FileWriter("/Users/gustavopinheiro/Desktop/output_csv/batch" + String.valueOf((todayNow.get(Calendar.MONTH)+1)) + String.valueOf(todayNow.get(Calendar.DAY_OF_MONTH)) + 
     																	String.valueOf(todayNow.get(Calendar.HOUR_OF_DAY)) + String.valueOf(todayNow.get(Calendar.MINUTE)) + 
     																	"_" + String.valueOf(orderCounter) + ".csv");
         	orderCounter++;
         	outputWriter = new BufferedWriter (fileOut);
         	lineNum = 1;
         } catch (FileNotFoundException fnfe) {
         	System.out.println ("FileNotFoundException in csvBuilder.startElement()" + fnfe);
         } catch (IOException ioe) {
         	System.out.println ("IOException in csvBuilder.startElement()" + ioe);
         }
     } else if ("Item".equals (qName)) {
     	try {
         	outputWriter.write("DTL," + lineNum + ",");
         	
         } catch (IOException ioe) {
         	System.out.println ("IOException in csvBuilder.endElement()" + ioe);
         }
     } else if ("ShipTo".equals (qName)) {
     	
     } else if ("PO".equals (qName)) {
     	
     } else if ("PartNumber".equals (qName)) {
     	
     } else if ("Qty".equals (qName)) {
     	
     	}
     }
 	    
 
     public void endElement(String uri, String name, String qName) throws SAXException {
     	
     	if ("Batch".equals (qName)) {
     		
         } else if ("Order".equals (qName)) {
         	try {
         		outputWriter.write("NPOCT,~");
         		outputWriter.flush();
         		fileOut.close();
         	} catch (IOException ioe) {
             	System.out.println ("IOException in xlsxBuilder.main()" + ioe); 
         	}
         } else if ("SoldTo".equals (qName)) {
             soldTo = temp;
         } else if ("ShipTo".equals (qName)) {
             shipTo = temp;
         } else if ("PO".equals (qName)) {
             pO = temp;
             try {
             	outputWriter.write("NPOCH,850,APPLT," + yyyy+mm+dd +"," +hh+min+","+"T,"+ pO +"," + yyyy+mm+dd +",BRL,N," + yyyy+mm+dd + ",,,~");
             	outputWriter.newLine();
             	outputWriter.write("N1HDR,SO,,," + soldTo + ",,,,,BR,,,,~");
             	outputWriter.newLine();
             	outputWriter.write("N1HDR,ST,,," + shipTo + ",,,,,BR,,,,~");
             	outputWriter.newLine();
             } catch (IOException ioe) {
             	System.out.println ("IOException in csvBuilder.endElement()" + ioe);
             }
         } else if ("Item".equals (qName)) {
         	lineNum++;
     	    
         } else if ("PartNumber".equals (qName)) {
         	pN = temp;
         	
         }  else if ("EAN".equals (qName)) {
         	ean = temp;
         	
         }else if ("Qty".equals (qName)) {
         	qty = temp;
         	
         } else if ("Price".equals (qName)) {
         	price = temp;
         	try {
             	outputWriter.write(qty + ",EA," + price + ",," + ean + "," + pN + ",,~");
             	outputWriter.newLine();
             } catch (IOException ioe) {
             	System.out.println ("IOException in csvBuilder.endElement()" + ioe);
             }
         }
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException   {
 		StringBuilder strBuff = new StringBuilder();
 			strBuff.append(ch);
 		temp = strBuff.toString().substring(start, start+length);
 
     }
     
 }
