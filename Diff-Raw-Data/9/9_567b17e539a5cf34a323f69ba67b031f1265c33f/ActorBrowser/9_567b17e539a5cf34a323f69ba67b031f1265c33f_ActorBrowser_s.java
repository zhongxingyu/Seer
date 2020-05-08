 package net.selenate.client.user;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.selenate.common.comms.*;
 import net.selenate.common.comms.req.*;
 import net.selenate.common.comms.res.*;
 import net.selenate.common.user.*;
 
 import akka.actor.ActorRef;
 
 public class ActorBrowser extends ActorBase implements IBrowser {
   public ActorBrowser(final ActorRef session) {
     super(session);
   }
 
   @Override
   public void open(final String url) throws IOException {
     typedBlock(new SeReqGet(url), SeResGet.class);
   }
 
   @Override
   public Capture capture(final String name) throws IOException {
     final SeResCapture res = typedBlock(new SeReqCapture(name), SeResCapture.class);
     return resToUserCapture(res);
   }
 
   @Override
   public Capture capture(final String name, final boolean takeScreenshot) throws IOException {
     final SeResCapture res = typedBlock(new SeReqCapture(name, takeScreenshot), SeResCapture.class);
     return resToUserCapture(res);
   }
 
   @Override
   public byte[] captureWindow(final ElementSelector selector) throws IOException {
     final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(selector.method);
     final SeResCaptureWindow res = typedBlock(
       new SeReqCaptureWindow(reqMethod, selector.query),
       SeResCaptureWindow.class);
     return res.screenshot;
   }
 
   @Override
   public String executeScript(final String javascript) throws IOException {
     final SeResExecuteScript res = typedBlock(new SeReqExecuteScript(javascript), SeResExecuteScript.class);
     return res.result;
   }
 
   @Override
   public void resetFrame() throws IOException {
     typedBlock(new SeReqResetFrame(), SeResResetFrame.class);
   }
 
   @Override
   public void switchFrame(final int frame) throws IOException {
     typedBlock(new SeReqSwitchFrame(frame), SeResSwitchFrame.class);
   }
 
   @Override
   public boolean waitFor(final List<ElementSelector> selectorList) throws IOException {
     return waitFor(new BrowserPage("default", selectorList));
   }
 
   @Override
   public boolean waitFor(final ElementSelector... selectorList) throws IOException {
     return waitFor(new BrowserPage("default", new ArrayList<ElementSelector>(Arrays.asList(selectorList))));
   }
 
   @Override
   public boolean waitFor(final BrowserPage page) throws IOException {
     final List<BrowserPage> pageList = new ArrayList<BrowserPage>();
     pageList.add(page);
     String res = waitForAny(pageList);
     return (res != null);
   }
 
   @Override
   public String waitForAny(BrowserPage ... pageList) throws IOException {
    return waitForAnyPage(Arrays.asList(pageList)).name;
   }
 
   @Override
   public String waitForAny(final List<BrowserPage> pageList) throws IOException {
    return waitForAnyPage(pageList).name;
   }
 
   @Override
   public void quit() throws IOException {
     typedBlock(new SeReqQuit(), SeResQuit.class);
   }
 
   @Override
   public boolean elementExists(
       final ElementSelectMethod method,
       final String query)
           throws IOException {
     final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
     final SeResElementExists res = typedBlock(new SeReqElementExists(reqMethod, query), SeResElementExists.class);
 
     return res.isFound;
   }
 
   @Override
   public boolean elementExists(final ElementSelector selector) throws IOException {
     return elementExists(selector.method, selector.query);
   }
 
   @Override
   public IElement findElement(
       final ElementSelectMethod method,
       final String query)
       throws IOException {
     final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
     final SeResFindElement res = typedBlock(new SeReqFindElement(reqMethod, query), SeResFindElement.class);
 
     return resToUserElement(res.element);
   }
 
   @Override
   public IElement findElement(final ElementSelector selector) throws IOException {
     return findElement(selector.method, selector.query);
   }
 
   @Override
   public List<IElement> findElementList(
       final ElementSelectMethod method,
       final String query)
           throws IOException {
     final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
     final SeResFindElementList res = typedBlock(new SeReqFindElementList(reqMethod, query), SeResFindElementList.class);
 
     return resToUserElementList(res.elementList);
   }
 
   @Override
   public List<IElement> findElementList(final ElementSelector selector) throws IOException {
     return findElementList(selector.method, selector.query);
   }
 
   @Override
   public String findAlert() throws IOException {
     final SeResFindAlert res = typedBlock(new SeReqFindAlert(), SeResFindAlert.class);
     return res.text;
   }
 
   @Override
   public void deleteCookieNamed(String name) throws IOException {
     typedBlock(new SeReqDeleteCookieNamed(name), SeResDeleteCookieNamed.class);
   }
 
   @Override
   public void addCookie(Cookie cookie) throws IOException {
     typedBlock(new SeReqAddCookie(cookie), SeResAddCookie.class);
   }
 
   @Override
   public INavigation navigate() throws IOException {
     return new ActorNavigation();
   }
 
   @Override
   public byte[] download(String url) throws IOException {
     final SeResDownload res = typedBlock(new SeReqDownload(url), SeResDownload.class);
     return res.body;
   }
 
   @Override
   public void startKeepaliveClick(long delayMillis, ElementSelector selector) throws IOException {
     startKeepaliveClick(delayMillis, selector.method, selector.query);
   }
 
   @Override
   public void startKeepaliveClick(long delayMillis, ElementSelectMethod method, String query) throws IOException {
     final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
     final List<SeCommsReq> reqList = new ArrayList<SeCommsReq>();
     reqList.add(new SeReqFindAndClick(reqMethod, query));
 
     startKeepalive(delayMillis, reqList);
   }
 
   @Override
   public void startKeepalive(long delayMillis, SeCommsReq... reqList) throws IOException {
     startKeepalive(delayMillis, Arrays.asList(reqList));
   }
 
   @Override
   public void startKeepalive(long delayMillis, List<SeCommsReq> reqList) throws IOException {
     typedBlock(new SeReqStartKeepalive(delayMillis, reqList), SeResStartKeepalive.class);
   }
 
   @Override
   public void stopKeepalive() throws IOException {
     typedBlock(new SeReqStopKeepalive(), SeResStopKeepalive.class);
   }
 
 
   private class ActorNavigation implements INavigation {
     @Override
     public void back() throws IOException {
       typedBlock(new SeReqNavigateBack(), SeResNavigateBack.class);
     }
 
     @Override
     public void forward() throws IOException {
       typedBlock(new SeReqNavigateForward(), SeResNavigateForward.class);
     }
 
     @Override
     public void refresh() throws IOException {
       typedBlock(new SeReqNavigateRefresh(), SeResNavigateRefresh.class);
     }
   }
 
   @Override
   public BrowserPage waitForAnyPage(BrowserPage... pageList) throws IOException {
     return waitForAnyPage(Arrays.asList(pageList));
   }
 
   @Override
   public BrowserPage waitForAnyPage(List<BrowserPage> pageList) throws IOException {
     final SeResWaitForBrowserPage res = typedBlock(new SeReqWaitForBrowserPage(userToReqPageList(pageList)), SeResWaitForBrowserPage.class);
 
     return res.isSuccessful ? res.foundPage : null;
   }
 }
