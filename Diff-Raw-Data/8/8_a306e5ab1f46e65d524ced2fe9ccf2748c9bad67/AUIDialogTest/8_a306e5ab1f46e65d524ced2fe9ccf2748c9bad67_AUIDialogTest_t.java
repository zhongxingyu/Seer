 package it.com.atlassian.aui.javascript.integrationTests;
 
 public class AUIDialogTest extends AbstractAUISeleniumTestCase
 {
     private static final String TEST_PAGE = "test-pages/dialog/dialog-test.html";
 
     //Test that dims for popups work
     public void testPopupDim()
     {
         openTestPage(TEST_PAGE);
         client.click("popup-button");
 
         assertThat.elementPresent("css=div.aui-blanket");
         assertThat.elementPresent("css=div.aui-blanket");
 
     }
 
     //Test that dims for dialogs work
     public void testDialogDim()
     {
         openTestPage(TEST_PAGE);
         client.click("dialog-button");
 
         assertThat.elementPresent("css=div.aui-blanket");
         assertThat.elementVisible("css=div.aui-blanket");
     }
 
     //test that the popup shows correctly
     public void testPopupShow()
     {
 
         openTestPage(TEST_PAGE);
         client.click("popup-button");
 
         assertThat.elementVisible("css=div#my-popup");
     }
 
     //test that dialogs show correctly
     public void testDialogShow()
     {
         openTestPage(TEST_PAGE);
         client.click("dialog-button");
 
         assertThat.elementVisible("css=div#dialog-test");
     }
 
     //test that popup hides correctly after escape key is hit
     public void testPopupEscHide()
     {
         openTestPage(TEST_PAGE);
         client.click("popup-button");
 
         assertThat.elementVisible("css=div#my-popup");
 
         client.keyPress("css=body", "\\27");
 
         assertThat.elementNotVisible("css=div#my-popup");
     }
 
     //test that dialog is hidden correctly after escape key is hit
     public void testDialogEscHide()
     {
         openTestPage(TEST_PAGE);
 
         client.click("dialog-button");
         assertThat.elementVisible("css=div#dialog-test");
 
         client.keyPress("css=body", "\\27");
         assertThat.elementNotVisible("css=div#dialog-test");
     }
 
     //test that Dialog hides correctly when pressing a close button
     public void testDialogButtonHide()
     {
         openTestPage(TEST_PAGE);
 
         client.click("dialog-button");
         assertThat.elementVisible("css=div#dialog-test");
 
        client.click("css=div#dialog-test button.button-panel-button:nth-child(3)");
         assertThat.elementNotVisible("css=div#dialog-test");
     }
 
     //test that popups are set to the correct size
     public void testPopupSize()
     {
 
         openTestPage(TEST_PAGE);
 
         client.click("popup-button");
 
         assertEquals("Dialog: 'my-popup' Height is not 200px", 200, client.getElementHeight("css=div#my-popup"));
         assertEquals("Dialog: 'my-popup' Width is not 400px", 400, client.getElementWidth("css=div#my-popup"));
 
     }
 
     //test that dialogs are set to the correct size
     public void testDialogSize()
     {
         openTestPage(TEST_PAGE);
 
         client.click("dialog-button");
 
         assertEquals("Dialog: 'dialog-test' Height is not 530px + 2px of border", 532, client.getElementHeight("css=div#dialog-test"));
         assertEquals("Dialog: 'dialog-test' Width is not 860px + 2px of border", 862, client.getElementWidth("css=div#dialog-test"));
     }
 
     //test that one stacking dialog works correctly
     public void testOneStackingDialogShow()
     {
         openTestPage(TEST_PAGE);
 
         client.click("dialog-button");
        client.click("css=div#dialog-test button.button-panel-button:nth-child(4)");
 
         assertThat.elementPresent("css=div#dialog-test");
         assertThat.elementVisible("css=div#dialog-test");
 
         assertThat.elementPresent("css=div#stack-dialog1");
         assertThat.elementVisible("css=div#stack-dialog1");
     }
 
     //test that 2 stacking dialogs work correctly
     public void testTwoStackingDialogsShow()
     {
         openTestPage(TEST_PAGE);
 
         client.click("dialog-button");
        client.click("css=div#dialog-test button.button-panel-button:nth-child(4)");
         client.click("css=div#stack-dialog1 button.button-panel-button:nth-child(2)");
 
         assertThat.elementPresent("css=div#dialog-test");
         assertThat.elementVisible("css=div#dialog-test");
 
         assertThat.elementPresent("css=div#stack-dialog1");
         assertThat.elementVisible("css=div#stack-dialog1");
 
         assertThat.elementPresent("css=div#stack-dialog2");
         assertThat.elementVisible("css=div#stack-dialog2");
     }
 
     //test addPanel
     public void testAddPanel()
     {
         openTestPage(TEST_PAGE);
 
         client.click("test-add-panel-button");
 
         assertThat.elementPresent("css=div#test-panel-dialog");
         assertThat.elementVisible("css=div#test-panel-dialog");
         assertThat.elementPresent("css=div#test-panel-dialog .dialog-panel-body");
     }
 
 
     //test addButton
     public void testAddButton()
     {
         openTestPage(TEST_PAGE);
 
         client.click("test-add-button-button");
 
         assertThat.elementPresent("css=div#test-button-dialog");
         assertThat.elementVisible("css=div#test-button-dialog");
         assertThat.elementPresent("css=div#test-button-dialog .dialog-button-panel");
         assertThat.elementPresent("css=div#test-button-dialog .button-panel-button:nth-child(1)");
     }
 
 }
