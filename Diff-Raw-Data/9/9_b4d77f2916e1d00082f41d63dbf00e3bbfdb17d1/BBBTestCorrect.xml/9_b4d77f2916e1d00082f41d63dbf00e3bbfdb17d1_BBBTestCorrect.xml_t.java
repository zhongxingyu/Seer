 package webtest;
 import org.junit.Test;
 
 import org.w3c.dom.Node;
 import com.gargoylesoftware.htmlunit.html.*;
 import com.gargoylesoftware.htmlunit.*;
 import java.util.*;
 
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 
 /**
 // Generated Using HTMLUnitGenerator
 <paths>
  <path id="campaignmodule">
   /html/body/div[2]/div/div[2]/div[2]/div/div[3]
  </path>
  <path id="searchpopup">
   /html/body/div[7]/div/div[9]
  </path>
  <path id="_eventId_search">
   //*[@id="_eventId_search"]
  </path>
  <path id="campaignModuleChoose">
   /html/body/div[2]/div/div[2]/div[2]/div/div[3]/div/span/a/span
  </path>
  <path id="searchPopupChooseFoundOffer">
   /html/body/div[7]/div/div[9]/form[2]/div[2]/div/a[2]/img
  </path>
  <path id="orderCartArea">
   /html/body/div[2]/div/div/div/div[3]/form/div[2]/div[2]/div[3]
  </path>
  <path id="website">
   /html/body
  </path>
  <path id="checkoutOrder">
   /html/body/div[2]/div/div/div/div[3]/form/div[2]/div[2]/div[3]/div[6]/div/input
  </path>
 </paths>
 
 <urls>
  <url id="baspaket">
   http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html
  </url>
 </urls>
 
 <flow>
  <state id="start">
   <transition to="campaignmodulestate" using="baspaket" delay="2000"/>
  </state>
 
  <state id="campaignmodulestate">
   <find path="campaignmodule">
     <tag type="a">
      <attribute name="href" value="/servlet/orderflow/search/search-flow?Id=tcm:142-23371"/>
     </tag>
   </find>
   <transition to="searchflow" using="campaignModuleChoose" delay="5000"/>
  </state>
 
  <state id="searchflow">
   <find path="searchpopup">
     <tag type="input">
      <attribute name="id" value="_eventId_search"/>
     </tag>
   </find>
   <form id="locationForm" submit="_eventId_search">
    <input name="_eventId" value="search"/>
    <input name="phoneNumber.fullNumber" value="0768966787"/>
   </form>
   <transition to="selectfloor" using="locationForm" delay="5000"/>
  </state>
 
  <state id="selectfloor">
   <find path="searchpopup">
     <tag type="input">
      <attribute name="id" value="_eventId_search"/>
     </tag>
   </find>
   <form id="locationForm" submit="_eventId_search">
    <input name="address.floor" value="3"/>
   </form>
   <transition to="searchresults" using="locationForm" delay="10000"/>
  </state>
 
  <state id="searchresults">
   <find path="searchpopup">
     <tag type="a">
      <attribute name="href" value="/orderflow/index.html?Id=tcm:142-23381&fromSearch&page=new"/>
     </tag>
   </find>
   <transition to="ordercart" using="searchPopupChooseFoundOffer" delay="10000"/>
  </state>
 
  <state id="ordercart">
   <find path="orderCartArea">
     <tag type="option">
      <attribute name="value" value="dsl24"/>
     </tag>
   </find>
   <transition to="tvDetails" using="checkoutOrder" delay="10000"/>
  </state>
 
  <state id="tvDetails">
   <find path="website">
     <tag type="input">
      <attribute name="src" value="/res/img/button/tillbaka.png"/>
     </tag>
   </find>
  </state>
 
 </flow>
 */
 
 @SuppressWarnings("unchecked")
 public class BBBTestCorrect extends TestCase {
 WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_8);
 HtmlPage page = null;
 String step = null;
 boolean successfull = false;
 HtmlForm form = null;
 HtmlInput input = null;
 HtmlSelect select = null;
 ArrayList<HtmlElement> matchingElement = null;
 @Test
 public void testHomePage() throws Exception {
 webClient.setCssEnabled(true);
 webClient.setJavaScriptEnabled(true);
 webClient.setThrowExceptionOnFailingStatusCode(false);
 webClient.setThrowExceptionOnScriptError(false);
 webClient.setTimeout(180000);
 webClient.setJavaScriptTimeout(180000);
 
 
 log(System.currentTimeMillis()+") Entering state 1 of 7 0% complete \"start\"");
 /**
  <url id="baspaket">
   http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html
  </url>
   <transition to="campaignmodulestate" using="baspaket" delay="2000"/>
 */
 page = webClient.getPage("http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html");
 Thread.sleep(2000);
 
 step = "campaignmodulestate";
 log(System.currentTimeMillis()+") Entering state 2 of 7 14% complete \"campaignmodulestate\"");
 /**
   <find path="campaignmodule">
     <tag type="a">
      <attribute name="href" value="/servlet/orderflow/search/search-flow?Id=tcm:142-23371"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/div[2]/div/div[2]/div[2]/div/div[3]", "a", "href", "/servlet/orderflow/search/search-flow?Id=tcm:142-23371", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 /**
  <path id="campaignModuleChoose">
   /html/body/div[2]/div/div[2]/div[2]/div/div[3]/div/span/a/span
  </path>
   <transition to="searchflow" using="campaignModuleChoose" delay="5000"/>
 */
 findAndClick("/html/body/div[2]/div/div[2]/div[2]/div/div[3]/div/span/a/span");
 Thread.sleep(5000);
 
 step = "searchflow";
 log(System.currentTimeMillis()+") Entering state 3 of 7 28% complete \"searchflow\"");
 /**
   <find path="searchpopup">
     <tag type="input">
      <attribute name="id" value="_eventId_search"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/div[7]/div/div[9]", "input", "id", "_eventId_search", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 /**
   <form id="locationForm" submit="_eventId_search">
    <input name="_eventId" value="search"/>
    <input name="phoneNumber.fullNumber" value="0768966787"/>
   </form>
   <transition to="selectfloor" using="locationForm" delay="5000"/>
 */
 form = getFormById("locationForm");
 setAttributeValue(form, "_eventId", "search");
 setAttributeValue(form, "phoneNumber.fullNumber", "0768966787");
 findAndClick("//*[@id=\"_eventId_search\"]");
 Thread.sleep(5000);
 
 step = "selectfloor";
 log(System.currentTimeMillis()+") Entering state 4 of 7 42% complete \"selectfloor\"");
 /**
   <find path="searchpopup">
     <tag type="input">
      <attribute name="id" value="_eventId_search"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/div[7]/div/div[9]", "input", "id", "_eventId_search", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 /**
   <form id="locationForm" submit="_eventId_search">
    <input name="address.floor" value="3"/>
   </form>
   <transition to="searchresults" using="locationForm" delay="10000"/>
 */
 form = getFormById("locationForm");
 setAttributeValue(form, "address.floor", "3");
 findAndClick("//*[@id=\"_eventId_search\"]");
 Thread.sleep(10000);
 
 step = "searchresults";
 log(System.currentTimeMillis()+") Entering state 5 of 7 57% complete \"searchresults\"");
 /**
   <find path="searchpopup">
     <tag type="a">
      <attribute name="href" value="/orderflow/index.html?Id=tcm:142-23381&fromSearch&page=new"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/div[7]/div/div[9]", "a", "href", "/orderflow/index.html?Id=tcm:142-23381&fromSearch&page=new", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 /**
  <path id="searchPopupChooseFoundOffer">
   /html/body/div[7]/div/div[9]/form[2]/div[2]/div/a[2]/img
  </path>
   <transition to="ordercart" using="searchPopupChooseFoundOffer" delay="10000"/>
 */
 findAndClick("/html/body/div[7]/div/div[9]/form[2]/div[2]/div/a[2]/img");
 Thread.sleep(10000);
 
 step = "ordercart";
 log(System.currentTimeMillis()+") Entering state 6 of 7 71% complete \"ordercart\"");
 /**
   <find path="orderCartArea">
     <tag type="option">
      <attribute name="value" value="dsl24"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/div[2]/div/div/div/div[3]/form/div[2]/div[2]/div[3]", "option", "value", "dsl24", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 /**
  <path id="checkoutOrder">
   /html/body/div[2]/div/div/div/div[3]/form/div[2]/div[2]/div[3]/div[6]/div/input
  </path>
   <transition to="tvDetails" using="checkoutOrder" delay="10000"/>
 */
 findAndClick("/html/body/div[2]/div/div/div/div[3]/form/div[2]/div[2]/div[3]/div[6]/div/input");
 Thread.sleep(10000);
 
 step = "tvDetails";
 log(System.currentTimeMillis()+") Entering state 7 of 7 85% complete \"tvDetails\"");
 /**
   <find path="website">
     <tag type="input">
      <attribute name="src" value="/res/img/button/tillbaka.png"/>
     </tag>
   </find>
 */
 findOrFail("/html/body", "input", "src", "/res/img/button/tillbaka.png", "http://www.bredbandsbolaget.se/tv/kanalpaket/baspaket.html", 0);
 webClient.closeAllWindows();
 }
 
 private void log(String string) {
  System.out.println(string);
 }
 
 private void findOrFail(String xpath, String tag, String attribute, String value, String currentUrl, int waitAtMost) throws InterruptedException {
  boolean successfull = false;
 long endTime = System.currentTimeMillis() + waitAtMost;
  log("Looking for "+tag+" with attribute "+attribute+" and value "+value+" in "+xpath);
  while (!successfull && (endTime-System.currentTimeMillis()) > 0) {
    webClient.waitForBackgroundJavaScriptStartingBefore(100);
    successfull = find(xpath, tag, attribute, value);
    if (!successfull)
     System.out.print(".");
    }
    if (successfull)
     log(" took "+(System.currentTimeMillis() - endTime + waitAtMost*1000) + "ms");
    if (!successfull) {
     log(page.asXml());
     findClosestXpath(xpath);
     fail(step+") Failed finding tag \""+tag+"\" with attribute \""+attribute+"\" and value \""+value+"\" in \""+xpath+"\" at \""+currentUrl+"\"");
    }
  }
 
 private boolean find(String xpath, String tag, String attribute, String value) {
  ArrayList<HtmlElement> matchingDivs = (ArrayList<HtmlElement>) page.getByXPath(xpath);
  for (HtmlElement div : matchingDivs) {
   if (recursiveFind(div.getChildNodes(), tag, attribute, value))
    return true;
  }
  return false;
 }
 
 private boolean recursiveFind(DomNodeList<DomNode> nodeList, String tag,
  String attribute, String value) {
  for (DomNode node : nodeList) {
   String nodeName = node.getNodeName();
   if (tag.equals(nodeName)) {
    Node nodeAttribute = node.getAttributes().getNamedItem(
      attribute);
    if (nodeAttribute != null) {    String nodeAttributeValue = nodeAttribute.getNodeValue();
     if (value.equals(nodeAttributeValue)) {
      log("Found element "+tag+" with attribute "+attribute+" and value "+value+" at "+node.getCanonicalXPath());
      return true;
     }
    }
   }
   if (recursiveFind(node.getChildNodes(), tag, attribute, value))
    return true;
  }
  return false;
 }
 private void findAndClick(String xpath) throws Exception {
  matchingElement = (ArrayList<HtmlElement>) page.getByXPath(xpath);
  if (matchingElement.size() == 0) {
   log(page.asXml());
   findClosestXpath(xpath);
   fail("Faild to find element " + xpath + "");
  }
  page = matchingElement.get(0).click();
 }
 
 private HtmlForm getFormById(String id) {
  for (HtmlForm form : page.getForms())
   if (form.getAttributes().getNamedItem("id") != null && form.getAttributes().getNamedItem("id").getNodeValue().equals(id)
      || form.getAttributes().getNamedItem("name") != null && form.getAttributes().getNamedItem("name").getNodeValue().equals(id)
      )
    return form;
  return null;
 }
 
 private void setAttributeValue(HtmlForm form, String attribute, String value) {
  HtmlSelect select;
  HtmlInput input;
  try {
  input = form.getInputByName(attribute);
  input.setValueAttribute(value);
  } catch (ElementNotFoundException e) {
  select = form.getSelectByName(attribute);
  select.setSelectedAttribute(value, true);
  }
 }
 
 private void findClosestXpath(String xpath) {
 if (xpath.startsWith("//*") || xpath.equals("/html"))
 	return;
 log("Searching for xpath "+xpath);
 matchingElement = (ArrayList<HtmlElement>) page.getByXPath(xpath);
 if (page.getByXPath(xpath).size() > 0) {
 	log("\nFound close elements at "+xpath+":");
 	for (HtmlElement element : matchingElement) {
  if (element.asXml().length() > 100)
   log(element.asXml().substring(0, 100) + " ...");
  else
   log(element.asXml());
 	}
 	return;
 }
 findClosestXpath(xpath.substring(0, xpath.lastIndexOf("/")));
 }
 
 
 }
