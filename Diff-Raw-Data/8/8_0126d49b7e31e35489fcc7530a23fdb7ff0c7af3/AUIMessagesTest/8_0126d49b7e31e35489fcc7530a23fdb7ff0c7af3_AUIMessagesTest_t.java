 package it.com.atlassian.aui.javascript.integrationTests;
 
 /**
  * Selenium test for events.js
  */
 public class AUIMessagesTest extends AbstractAUISeleniumTestCase
 {
     private static final String TEST_PAGE = "test-pages/messages/messages-test.html";
 
     //Make sure javascript messages work when no context is supplied
 
     public void testJavascriptMessagesWithNoContext()
     {
         openTestPage(TEST_PAGE);
         assertThat.elementPresent("css=div#aui-message-bar .success");
         assertThat.elementPresent("css=div#aui-message-bar .info");
     }
 
     //Make sure javascript messsages work when a context is supplied
 
     public void testJavscriptMessagesWithContext()
     {
         openTestPage(TEST_PAGE);
 
         assertThat.elementPresent("css=div#context .warning");
         assertThat.elementPresent("css=div#context .hint");
     }
 
     //Make sure closeable HTML messages close correctly
 
     public void testHTMLCloseable()
     {
         openTestPage(TEST_PAGE);
 
        assertThat.elementPresent("css=div#closeable-html-test .aui-icon.icon-close");
 
        client.click("css=div#closeable-html-test .closeable .aui-icon.icon-close");
         assertThat.elementNotPresent("css=div#closeable-html-test .closeable");
 
     }
 
     public void testJSCloseable()
     {
         openTestPage(TEST_PAGE);
 
         assertThat.elementPresent("css=div#closeable-js-test .closeable");
 
        client.click("css=div#closeable-js-test .closeable .aui-icon.icon-close");
         assertThat.elementNotPresent("css=div#closeable-js-test .closeable");
 
     }
 
 
 }
