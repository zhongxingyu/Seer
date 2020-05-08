 package it.com.atlassian.aui.javascript.integrationTests;
 
 public class AUISeleniumQUnitTest extends AbstractAUISeleniumTestCase
 {
     public void testWhenITypeUnitTests()
     {
         openQunitTestPage("whenitype");
         runQunitTests("WhenIType");
     }
 
     public void testDialogUnitTests()
     {
         openQunitTestPage("dialog");
         runQunitTests("Dialog");
     }
 
 
     public void testDropdownUnitTests()
     {
         openQunitTestPage("dropdown");
         runQunitTests("Dropdown");
     }
 
     public void testFormatUnitTests()
     {
         openQunitTestPage("format");
         runQunitTests("Format");
     }
 
     public void testFormsUnitTests()
     {
         openQunitTestPage("forms");
         runQunitTests("Forms");
     }
 
     public void testInlineDialogUnitTests()
     {
         openQunitTestPage("inline-dialog");
         runQunitTests("Inline-Dialog");
     }
 
     public void testMessagesUnitTests()
     {
         openQunitTestPage("messages");
         runQunitTests("Messages");
     }
 
     public void testStalkerUnitTests()
     {
         openQunitTestPage("stalker");
         runQunitTests("Stalker");
     }
 
     public void testTablesUnitTests()
     {
         openQunitTestPage("tables");
         runQunitTests("Tables");
     }
 
     public void testTabsUnitTests()
     {
         openQunitTestPage("tabs");
         runQunitTests("Tabs");
     }
 
     public void testToolbarUnitTests()
     {
         openQunitTestPage("toolbar");
         runQunitTests("Toolbar");
     }
 
     public void testEventsUnitTests()
     {
         openQunitTestPage("events");
         runQunitTests("Events");
     }
 
     //HELPER FUNCTIONS
 
     //runs qunit tests on the page, component argument for reporting purposes only
     private void runQunitTests(String component)
     {
         client.waitForCondition("selenium.isElementPresent('qunit-testresult')");
        try
        {
            client.wait(3000);
            client.wait();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
         int numberOfFailedTests = Integer.valueOf(client.getEval("window.AJS.$('li.fail li.fail').size()"));
         if (numberOfFailedTests != 0)
         {
             String failedTests[] = getFailedAssertionsText();
             String failedTestListString = "";
 
             for (int i = 0; i < failedTests.length; i++)
             {
 
                 failedTestListString = failedTestListString + "FAILED! >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + failedTests[i] + "\n";
             }
 
             fail("There are " + (numberOfFailedTests) + " failed unit tests for" + component + " \n\n" + failedTestListString);
         }
     }
 
     //Function to retrive all the failed assertions and place them in an array to be used for reporting
     private String[] getFailedAssertionsText()
     {
 
         String result = client.getEval("var string = \"\";function getText(){window.AJS.$('li.fail li.fail').each( function(){string = string + window.AJS.$(this).text() + \"|\";});return string;} getText();");
 
         String resultSplit[];
 
         resultSplit = result.split("\\|");
 
 
         return resultSplit;
     }
 
     //Opens a qunit test page for the specified component (assumes correct test file structure)
     private void openQunitTestPage(String component)
     {
         openTestPage("unit-tests/tests/" + component + "-unit-tests/" + component + "-unit-tests.html");
     }
 
 
 }
