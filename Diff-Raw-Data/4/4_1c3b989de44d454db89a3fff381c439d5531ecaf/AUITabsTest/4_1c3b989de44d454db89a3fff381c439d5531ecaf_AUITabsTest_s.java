 package it.com.atlassian.aui.javascript.integrationTests;
 
 public class AUITabsTest extends AbstractAUISeleniumTestCase
 {
 
     private static final String TEST_PAGE = "test-pages/tabs/tabs-test.html";
 
     // First pane of tabs should  be visible by default
     public void testAUITabsFirstPaneVisibleByDefault()
     {
         openTestPage(TEST_PAGE);
 
         assertThat.elementVisible("css=div#horizontal-first");
         assertThat.elementVisible("css=div#vertical-fifth");
     }
 
     // Only one pane should be visible
     public void testAUITabsOnlyOnePaneVisible()
     {
         openTestPage(TEST_PAGE);
 
         assertThat.elementVisible("css=div#horizontal-first");
 
         assertThat.elementNotVisible("css=div#horizontal-second");
         assertThat.elementNotVisible("css=div#horizontal-third");
         assertThat.elementNotVisible("css=div#horizontal-fourth");
 
         assertThat.elementVisible("css=div#vertical-fifth");
 
         assertThat.elementNotVisible("css=div#vertical-sixth");
         assertThat.elementNotVisible("css=div#vertical-seventh");
         assertThat.elementNotVisible("css=div#vertical-eight");
     }
 
     // The visible pane should be the active-pane
     public void testAUITabsVisiblePaneIsActive()
     {
         openTestPage(TEST_PAGE);
 
         //check that the first horizontal pane is visible
         assertThat.elementVisible("css=div#horizontal-first");
 
         //check that the first horizontal pane is active
         assertThat.attributeContainsValue("css=div#horizontal-first", "class", "active-pane");
 
         //check that all other panes are not active
         assertThat.attributeDoesntContainValue("css=div#horizontal-second", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div#horizontal-third", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div#horizontal-fourth", "class", "active-pane");
 
         //check that the first vertical pane is visible
         assertThat.elementVisible("css=div#vertical-fifth");
 
         //check that the first vertical pane is active
         assertThat.attributeContainsValue("css=div#vertical-fifth", "class", "active-pane");
 
         //check that the rest of the panes are notactive
         assertThat.attributeDoesntContainValue("css=div#vertical-sixth", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div#vertical-seventh", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div#vertical-eighth", "class", "active-pane");
     }
 
     // First  menu item should be active by default
     public void testAUITabsFirstMenuItemActiveByDefault()
     {
         openTestPage(TEST_PAGE);
 
        assertThat.attributeContainsValue("css=div#horizontal li.first", "class", "active-tab");
        assertThat.attributeContainsValue("css=div#vertical li.first", "class", "active-tab");
     }
 
     // Only one menu item should be active
     public void testAUITabsOnlyOneMenuItemActive()
     {
 
         openTestPage(TEST_PAGE);
 
         //check tab first horizontal menu item is active
         assertThat.attributeContainsValue("css=div#horizontal ul.tabs-menu li.menu-item:nth-child(1)", "class", "active-tab");
 
         //check that the rest of the menu items arent active
         assertThat.attributeDoesntContainValue("css=div#horizontal ul.tabs-menu li.menu-item:nth-child(2)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div#horizontal ul.tabs-menu li.menu-item:nth-child(3)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div#horizontal ul.tabs-menu li.menu-item:nth-child(4)", "class", "active-tab");
 
         //check tab first vertical menu item is active
         assertThat.attributeContainsValue("css=div#vertical ul.tabs-menu li.menu-item:nth-child(1)", "class", "active-tab");
 
         //check that the rest of the menu items arent active
         assertThat.attributeDoesntContainValue("css=div#vertical ul.tabs-menu li.menu-item:nth-child(2)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div#vertical ul.tabs-menu li.menu-item:nth-child(3)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div#vertical ul.tabs-menu li.menu-item:nth-child(4)", "class", "active-tab");
     }
 
     // clicking menu item should show associated pane and hide all others
     public void testAUITabsClickingMenuItemShouldShowAssociatedPane()
     {
         openTestPage(TEST_PAGE);
         client.waitForPageToLoad();
         client.getEval("window.AJS.$(\"#horizontal ul.tabs-menu .menu-item:nth-child(2)>a\").trigger(\"click\")");
 
         assertThat.elementVisible("css=div#horizontal-second");
         assertThat.elementNotVisible("css=div#horizontal-first");
         assertThat.elementNotVisible("css=div#horizontal-third");
         assertThat.elementNotVisible("css=div#horizontal-fourth");
     }
 
 
 }
