 package it.com.atlassian.aui.javascript.integrationTests;
 
 public class AUIInlineDialogTest extends AbstractAUISeleniumTestCase
 {
     private static final String TEST_PAGE = "test-pages/inline-dialog/inline-dialog-test.html";
 
     //test that a standard InlineDialog shows on click
     public void testAUIInlineDialogStandardClickShow() throws InterruptedException
     {
         openTestPage(TEST_PAGE);
 
        client.click("css=a#popupLink");

         assertThat.elementPresent("css=div#inline-dialog-1");
         assertThat.elementVisible("css=div#inline-dialog-1");
     }
 
     //test that a standard hover inline-dialog show on hover
     public void testAUIInlineDialogStandardHoverShow() throws InterruptedException
     {
         openTestPage(TEST_PAGE);
 
        client.mouseMove("css=a#hoverLink");

         assertThat.elementPresent("css=div#inline-dialog-2");
         assertThat.elementVisible("css=div#inline-dialog-2");
     }
 
     //test noBind
     public void testAUIInlineDialogNoBindOption()
     {
         openTestPage(TEST_PAGE);
         client.click("css=a#noBind");
         assertThat.elementNotPresent("css=div#inline-dialog-3");
         assertThat.elementNotVisible("css=div#inline-dialog-3");
     }
 
     //test noBind with manual Binding
     public void testAUIInlineDialogNoBindOptionWithManualBinding()
     {
         openTestPage(TEST_PAGE);
        client.click("css=a#testNoBind2");
         assertThat.elementPresent("css=div#inline-dialog-16");
         assertThat.elementVisible("css=div#inline-dialog-16");
     }
 
     //test right positioned short trigger
     public void testRightPositionedShortTrigger()
     {
         openTestPage(TEST_PAGE);
        client.click("css=a#testFloat");
         assertThat.elementPresent("css=div#inline-dialog-5");
         assertThat.elementVisible("css=div#inline-dialog-5");
         int expectedPosition = getWindowWidth() - 5 - client.getElementWidth("css=div#inline-dialog-5").intValue();
         int actualPosition = client.getElementPositionLeft("css=div#inline-dialog-5").intValue();
         assertTrue("right positioned inline-dialog is not positioned correctly", isWithinRange(actualPosition, expectedPosition - 20, expectedPosition + 20));
     }
 
     //test right positioned medium trigger
     public void testRightPositionedMediumTrigger()
     {
         openTestPage(TEST_PAGE);
        client.click("css=a#testFloat3");
         assertThat.elementPresent("css=div#inline-dialog-7");
         assertThat.elementVisible("css=div#inline-dialog-7");
 
         int expectedPosition = getWindowWidth() - 5 - client.getElementWidth("css=div#inline-dialog-7").intValue();
         int actualPosition = client.getElementPositionLeft("css=div#inline-dialog-7").intValue();
         assertTrue("right positioned inline-dialog is not positioned correctly", isWithinRange(actualPosition, expectedPosition - 20, expectedPosition + 20));
 
     }
 
     //test right positioned Long trigger
     public void testRightPositionedLongTrigger()
     {
         openTestPage(TEST_PAGE);
        client.click("css=a#testFloat2");
         assertThat.elementPresent("css=div#inline-dialog-6");
         assertThat.elementVisible("css=div#inline-dialog-6");
 
         int expectedPosition = client.getElementPositionLeft("css=a#testFloat2").intValue();
         int actualPosition = client.getElementPositionLeft("css=div#inline-dialog-6").intValue();
         assertTrue("right positioned inline-dialog is not positioned correctly", isWithinRange(actualPosition, expectedPosition - 20, expectedPosition + 20));
     }
 }
