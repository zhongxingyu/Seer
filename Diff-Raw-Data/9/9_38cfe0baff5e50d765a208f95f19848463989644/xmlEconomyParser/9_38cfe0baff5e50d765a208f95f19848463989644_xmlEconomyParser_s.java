 // %2960699267:de.hattrickorganizer.logik.xml%
 /*
  * xmlEconomyParser.java
  *
  * Created on 7. Mai 2004, 16:29
  */
 package de.hattrickorganizer.logik.xml;
 
 import java.util.Hashtable;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.xml.XMLManager;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thetom
  */
 public class xmlEconomyParser {
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new instance of xmlEconomyParser
      */
     public xmlEconomyParser() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param dateiname TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final Hashtable parseEconomy(String dateiname) {
         Document doc = null;
 
         doc = XMLManager.instance().parseFile(dateiname);
 
         return parseDetails(doc);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param datei TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final Hashtable parseEconomy(java.io.File datei) {
         Document doc = null;
 
         doc = XMLManager.instance().parseFile(datei);
 
         return parseDetails(doc);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //parse public
     ////////////////////////////////////////////////////////////////////////////////
     public final Hashtable parseEconomyFromString(String inputStream) {
         Document doc = null;
 
         doc = XMLManager.instance().parseString(inputStream);
 
         return parseDetails(doc);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //Parser Helper private
     ////////////////////////////////////////////////////////////////////////////////
 
     /**
      * erstellt das MAtchlineup Objekt
      *
      * @param doc TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final Hashtable parseDetails(Document doc) {
         Element ele = null;
         Element root = null;
         final de.hattrickorganizer.model.MyHashtable hash = new de.hattrickorganizer.model.MyHashtable();
 
         if (doc == null) {
             return hash;
         }
 
         //Tabelle erstellen
         root = doc.getDocumentElement();
 
         try {
             //Daten fllen
             //Fetchdate
             ele = (Element) root.getElementsByTagName("FetchedDate").item(0);
             hash.put("FetchedDate", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             //Root wechseln
             root = (Element) root.getElementsByTagName("Team").item(0);
             ele = (Element) root.getElementsByTagName("TeamID").item(0);
             hash.put("TeamID", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("TeamName").item(0);
             hash.put("TeamName", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("Cash").item(0);
             hash.put("Cash", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("ExpectedCash").item(0);
             hash.put("ExpectedCash", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             //Root wechseln
             // root =  (Element) doc.getDocumentElement().getElementsByTagName( "Team").item( 0 );
             ele = (Element) root.getElementsByTagName("SponsorsPopularity").item(0);
 
             if (XMLManager.instance().getAttributeValue(ele, "Available").trim().equalsIgnoreCase("true")) {
                 hash.put("SponsorsPopularity", (XMLManager.instance().getFirstChildNodeValue(ele)));
             }
 
             ele = (Element) root.getElementsByTagName("SupportersPopularity").item(0);
 
             if (XMLManager.instance().getAttributeValue(ele, "Available").trim().equalsIgnoreCase("true")) {
                hash.put("SupportersPopularity", (XMLManager.instance().getFirstChildNodeValue(ele)));
             }
 
             ele = (Element) root.getElementsByTagName("FanClubSize").item(0);
             hash.put("FanClubSize", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("IncomeSpectators").item(0);
             hash.put("IncomeSpectators", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("IncomeSponsors").item(0);
             hash.put("IncomeSponsors", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             //Root wechseln
             //root =  (Element) root.getElementsByTagName( "League").item( 0 );
             ele = (Element) root.getElementsByTagName("IncomeFinancial").item(0);
             hash.put("IncomeFinancial", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("IncomeTemporary").item(0);
             hash.put("IncomeTemporary", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("IncomeSum").item(0);
             hash.put("IncomeSum", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("CostsArena").item(0);
             hash.put("CostsArena", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("CostsPlayers").item(0);
             hash.put("CostsPlayers", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("CostsFinancial").item(0);
             hash.put("CostsFinancial", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("CostsTemporary").item(0);
             hash.put("CostsTemporary", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("CostsStaff").item(0);
             hash.put("CostsStaff", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("CostsYouth").item(0);
             hash.put("CostsYouth", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("CostsSum").item(0);
             hash.put("CostsSum", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("ExpectedWeeksTotal").item(0);
             hash.put("ExpectedWeeksTotal", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("LastIncomeSpectators").item(0);
             hash.put("LastIncomeSpectators", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("LastIncomeSponsors").item(0);
             hash.put("LastIncomeSponsors", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("IncomeSpectators").item(0);
             hash.put("IncomeSpectators", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("IncomeSponsors").item(0);
             hash.put("IncomeSponsors", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastIncomeFinancial").item(0);
             hash.put("LastIncomeFinancial", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("LastIncomeTemporary").item(0);
             hash.put("LastIncomeTemporary", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastIncomeSum").item(0);
             hash.put("LastIncomeSum", (XMLManager.instance().getFirstChildNodeValue(ele)));
 
             ele = (Element) root.getElementsByTagName("LastCostsArena").item(0);
             hash.put("LastCostsArena", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsPlayers").item(0);
             hash.put("LastCostsPlayers", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsFinancial").item(0);
             hash.put("LastCostsFinancial", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsTemporary").item(0);
             hash.put("LastCostsTemporary", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsStaff").item(0);
             hash.put("LastCostsStaff", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsYouth").item(0);
             hash.put("LastCostsYouth", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastCostsSum").item(0);
             hash.put("LastCostsSum", (XMLManager.instance().getFirstChildNodeValue(ele)));
             ele = (Element) root.getElementsByTagName("LastWeeksTotal").item(0);
             hash.put("LastWeeksTotal", (XMLManager.instance().getFirstChildNodeValue(ele)));
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"XMLExonom<Parser.parseDetails Exception gefangen: " + e);
             HOLogger.instance().log(getClass(),e);
         }
 
         return hash;
     }
 }
