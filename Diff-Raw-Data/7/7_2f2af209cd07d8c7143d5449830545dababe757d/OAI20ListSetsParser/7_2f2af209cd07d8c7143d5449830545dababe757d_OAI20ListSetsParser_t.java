 /*
  * Based on com.sun.syndication.io.impl.RSS090Parser
  * 
  * Copyright 2004 Sun Microsystems, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package edu.usu.cosl.syndication.io.impl;
 
 import com.sun.syndication.feed.WireFeed;
 import com.sun.syndication.feed.rss.Channel;
 import com.sun.syndication.feed.rss.Item;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.impl.BaseWireFeedParser;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  */
 public class OAI20ListSetsParser extends BaseWireFeedParser {
 
     private static final String OAI_URI = "http://www.openarchives.org/OAI/2.0/";
     
     private static final Namespace OAI_NS = Namespace.getNamespace(OAI_URI);
 
     public OAI20ListSetsParser() {
         this("oai_2.0_list_sets");
     }
 
     protected OAI20ListSetsParser(String type) {
         super(type, OAI_NS);
     }
 
     public boolean isMyType(Document document) 
     {
         Element oaiRoot = document.getRootElement();
         Namespace defaultNS = oaiRoot.getNamespace();
         if (defaultNS ==null || !defaultNS.equals(getOAINamespace())) return false;
     	Element request = oaiRoot.getChild("request", getOAINamespace());
     	String sVerb = request.getAttribute("verb").getValue();
     	return "ListSets".equals(sVerb);
     }
 
     public WireFeed parse(Document document, boolean validate) throws IllegalArgumentException,FeedException {
         if (validate) {
             validateFeed(document);
         }
         Element oaiRoot = document.getRootElement();
         return parseSetList(oaiRoot);
     }
 
     protected void validateFeed(Document document) throws FeedException {
         // TBD
         // here we have to validate the Feed against a schema or whatever
         // not sure how to do it
         // one posibility would be to inject our own schema for the feed (they don't exist out there)
         // to the document, produce an ouput and attempt to parse it again with validation turned on.
         // otherwise will have to check the document elements by hand.
     }
 
     /**
      * Returns the namespace used by RDF elements in document of the RSS version the parser supports.
      * <P>
      * This implementation returns the EMTPY namespace.
      * <p>
      *
      * @return returns the EMPTY namespace.
      */
     protected Namespace getOAINamespace() {
         return OAI_NS;
     }
     /**
      * Parses the root element of an RSS document into a Channel bean.
      * <p/>
      * It reads title, link and description and delegates to parseImage, parseItems
      * and parseTextInput. This delegation always passes the root element of the RSS
      * document as different RSS version may have this information in different parts
      * of the XML tree (no assumptions made thanks to the specs variaty)
      * <p/>
      *
      * @param oaiRoot the root element of the RSS document to parse.
      * @return the parsed Channel bean.
      */
     protected WireFeed parseSetList(Element oaiRoot) throws FeedException
     {
         Element eChannel = oaiRoot.getChild("ListSets", getOAINamespace()); 
 
         // if there are no new records, there will be no ListRecords element
         if (eChannel == null)
         {
             String sError = oaiRoot.getChildText("error", getOAINamespace());
             System.out.println(sError);
             return null;
         }
         
         Channel channel = new Channel(getType());
         channel.setItems(parseSets(eChannel));
         
         // store any resumption token as foreign markup 
         String sResumptionToken = eChannel.getChildTextTrim("resumptionToken", getOAINamespace());
         if (sResumptionToken != null)
         {
 	        ArrayList<String> lMarkup = new ArrayList<String>();
 	        lMarkup.add(sResumptionToken);
 	        channel.setForeignMarkup(lMarkup);
         }
 
         return channel;
     }
 
 
     /**
      * This method exists because RSS0.90 and RSS1.0 have the 'item' elements under the root elemment.
      * And RSS0.91, RSS0.02, RSS0.93, RSS0.94 and RSS2.0 have the item elements under the 'channel' element.
      * <p/>
      */
     protected List getSets(Element oaiRoot) {
         return oaiRoot.getChildren("set", getOAINamespace());
     }
 
     /**
      * Parses the root element of an RSS document looking for all items information.
      * <p/>
      * It iterates through the item elements list, obtained from the getItems() method, and invoke parseItem()
      * for each item element. The resulting RSSItem of each item element is stored in a list.
      * <p/>
      *
      * @param oaiRoot the root element of the RSS document to parse for all items information.
      * @return a list with all the parsed RSSItem beans.
      */
     protected List parseSets(Element recordList)  {
         Collection eItems = getSets(recordList);
 
         List items = new ArrayList();
         int nItem = 1;
         for (Iterator i=eItems.iterator();i.hasNext();) {
             Element eItem = (Element) i.next();
             Item item = parseSet(recordList,eItem);
             if (item != null) items.add(item);
         }
         return items;
     }
 
     /**
      * Parses an item element of an RSS document looking for item information.
      * <p/>
      * It reads title and link out of the 'item' element.
      * <p/>
      *
      * @param oaiRoot the root element of the RSS document in case it's needed for context.
      * @param eItem the item element to parse.
      * @return the parsed RSSItem bean.
      */
     protected Item parseSet(Element oaiRoot, Element eSet) 
     {
     	Element eSetSpec = eSet.getChild("setSpec", getOAINamespace());
     	Element eSetName = eSet.getChild("setName", getOAINamespace());
     	
     	if (eSetSpec == null || eSetName == null) return null;
     	
     	String sID = eSetSpec.getText();
     	String sTitle = eSetName.getText();

    	// HACK: OER Collections provides a single set with all collections titled 'All collections'
    	// We want individual collections, not one with all of them
    	// the correct fix is to implement an approval mechanism for auto discovered feeds
    	if (sTitle.startsWith("All collections")) return null;
     	
     	// store the set information
     	Item item = new Item();
     	item.setUri(sID);
     	item.setTitle(sTitle);
         
         return item;
     }
 }
