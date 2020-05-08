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
  <path id="mainArea">
   /html/body/center/table/tbody/tr[3]/td/table
  </path>
  <path id="linksLink">
   /html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[2]/a
  </path>
  <path id="imagesLink">
   /html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[3]/a
  </path>
 </paths>
 
 <urls>
  <url id="url1">
   http://bjurr.se/
  </url>
 </urls>
 
 <flow>
  <state id="start">
   <transition to="startPage" using="url1" delay="0"/>
  </state>
 
  <state id="startPage">
   <transition to="links" using="linksLink" delay="0"/>
  </state>
 
  <state id="links">
   <find path="mainArea">
     <tag type="a">
      <attribute name="href" value="/link/category/blandat"/>
     </tag>
   </find>
   <transition to="images" using="imagesLink" delay="0"/>
  </state>
 
  <state id="images">
   <find path="mainArea">
     <tag type="a">
      <attribute name="href" value="/image/view/?i=/080112 Fiske/"/>
     </tag>
   </find>
  </state>
 
 </flow>
 */
 
 @SuppressWarnings("unchecked")
 public class BjurrTestCorrect extends TestCase {
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
 
 
 log(System.currentTimeMillis()+") Entering state 1 of 4 0% complete \"start\"");
 /**
  <url id="url1">
   http://bjurr.se/
  </url>
   <transition to="startPage" using="url1" delay="0"/>
 */
 page = webClient.getPage("http://bjurr.se/");
 
 step = "startPage";
 log(System.currentTimeMillis()+") Entering state 2 of 4 25% complete \"startPage\"");
 /**
  <path id="linksLink">
   /html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[2]/a
  </path>
   <transition to="links" using="linksLink" delay="0"/>
 */
 findAndClick("/html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[2]/a");
 
 step = "links";
 log(System.currentTimeMillis()+") Entering state 3 of 4 50% complete \"links\"");
 /**
   <find path="mainArea">
     <tag type="a">
      <attribute name="href" value="/link/category/blandat"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/center/table/tbody/tr[3]/td/table", "a", "href", "/link/category/blandat", "http://bjurr.se/", 0);
 /**
  <path id="imagesLink">
   /html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[3]/a
  </path>
   <transition to="images" using="imagesLink" delay="0"/>
 */
 findAndClick("/html/body/center/table/tbody/tr[2]/th/table/tbody/tr/td[3]/a");
 
 step = "images";
 log(System.currentTimeMillis()+") Entering state 4 of 4 75% complete \"images\"");
 /**
   <find path="mainArea">
     <tag type="a">
      <attribute name="href" value="/image/view/?i=/080112 Fiske/"/>
     </tag>
   </find>
 */
 findOrFail("/html/body/center/table/tbody/tr[3]/td/table", "a", "href", "/image/view/?i=/080112 Fiske/", "http://bjurr.se/", 0);
 webClient.closeAllWindows();
 }
 
 private void log(String string) {
  System.out.println(string);
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
 
 private void findOrFail(String xpath, String tag, String attribute, String value, String currentUrl, int waitAtMost) throws InterruptedException {
  boolean successfull = false;
 long endTime = System.currentTimeMillis() + waitAtMost*1000;
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
 
 }
