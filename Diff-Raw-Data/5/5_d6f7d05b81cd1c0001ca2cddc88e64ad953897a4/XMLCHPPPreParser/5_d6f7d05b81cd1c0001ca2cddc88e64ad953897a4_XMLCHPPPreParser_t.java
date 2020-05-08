 package de.hattrickorganizer.logik.xml;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.xml.XMLManager;
 
 public class XMLCHPPPreParser {
 
 	public XMLCHPPPreParser() {
     }
 	
 	public String Error(String xmlIn) {
 		String sError  = "";
         Document doc = null;
         doc = XMLManager.instance().parseString(xmlIn);
         if (doc != null) {
         	Element ele = null;
             Element root = doc.getDocumentElement();
             try {
             	// See if an error is found
             	if (root.getElementsByTagName("ErrorCode").getLength() > 0) {
             		ele = (Element) root.getElementsByTagName("ErrorCode").item(0);
             		String sTmpError = XMLManager.instance().getFirstChildNodeValue(ele);
             		String sErrString = "";
             		HOVerwaltung hov = HOVerwaltung.instance();
             		switch (Integer.parseInt(sTmpError)) {
 	            		case -1:
 	            		case 0:
 	            		case 1:
 	            		case 2:
 	            		case 3:
 	            		case 4:
 	            		case 5:
 	            		case 6:
 	            		case 7:
 	            		case 10:
 	            		case 50:
 	            		case 51:
 	            		case 52:
 	            		case 53:
 	            		case 54:
 	            		case 55:
 	            		case 56:
 	            		case 57:
 	            		case 58:
 	            		case 90:
 	            		case 91:
 	            		case 99:
	            			sErrString = sTmpError + " - " + hov.getLanguageString("CHPP.Error" + sTmpError);
 	            			break;
             			default:
            				sErrString = sTmpError + " - " + hov.getLanguageString("CHPP.Unknown");
             				break;
             		}
             		sError = hov.getLanguageString("CHPP.Error") + " - " + sErrString;
             	}
             }
         	catch (Exception ex)
         	{
         		 HOLogger.instance().log(getClass(),"XMLCHPPPreParser Exception: " + ex);
         		 sError = ex.getMessage();
         	}
                 
         } else {
         	sError = "No CHPP data found.";
         }
         return sError;
     }
 }
