 /**
  * 
  */
 package org.melati.admin.test;
 
 import org.melati.JettyWebTestCase;
 
 /**
  * 
  * @author timp
  * @since 2008/01/01
  */
 public class AdminJettyWebTest extends JettyWebTestCase {
    private String dbName = "melatijunit";
   
   /**
    * 
    */
   public AdminJettyWebTest() {
     super();
   }
   /**
    * @param name
    */
   public AdminJettyWebTest(String name) {
     super(name);
   }
 
   // Test Page calls
   /**
    * 
    */
   public void testBadUrl() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/Unknown");
     assertTextPresent("Melati Error Template");
   }
   /**
    * 
    */
   public void testAdminMain() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/Main");
     assertTextPresent("You need a frames enabled browser to use the Admin Suite");
   }
   /**
    * 
    */
   public void testAdminTop() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Melatijunit database");
   }
   /**
    * 
    */
   public void testAdminTopWithTable() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Melatijunit database");
   }
   /**
    * 
    */
   public void testAdminTopWithTableAndTroid() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/0/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Melatijunit database");
   }
   
   /**
    * 
    */
   public void testUpload() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/Upload?field=tim");
     assertTextPresent("File to upload:");
   }
   
   
   /**
    * 
    */
   public void testAdminBottom() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/Bottom");
     // Hmmm Should assert something, coverage is the thing
   }
   /**
    * 
    */
   public void testAdminRight() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/0/Right");
     // Hmmm Should assert something, coverage is the thing
   }
   /**
    * 
    */
   public void testAdminPrimarySelect() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/PrimarySelect");
     assertTextPresent("Full name");
   }
   /**
    * 
    */
   public void testAdminSelection() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/Selection?target=&returnTarget=");
     assertTextPresent("Full name");
     assertTextPresent("Melati guest user");
   }
   /**
    * 
    */
   public void testAdminEditHeader() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/0/EditHeader");
     assertTextPresent("User");
     assertTextPresent("_guest_");
   }
   /**
    * 
    */
   public void testAdminEdit() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/0/Edit");
     assertTextPresent("Full name");
     assertTextPresent("_guest_");
   }
   /**
    * Test that login is required.
    */
   public void testAdminEditAdministrator() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/1/Edit");
     assertTextPresent("You need to log in.");
     setTextField("field_login", "_administrator55_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME_not");
     checkCheckbox("rememberme");
     submit("action");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     assertTextPresent("Full name");
     assertTextPresent("_administrator_");
 
     // Note that logging out has no effect if rememberme was chosen
    gotoPage("/Logout/" + dbName + "");
     gotoPage("/Admin/" + dbName + "/user/1/Edit");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     submit("action");
     assertTextPresent("Updated a User Record");
     assertTextPresent("Done");
   }
   
   /**
    * 
    */
   public void testAdminTree() {
     setScriptingEnabled(true);
     beginAt("/Admin/" + dbName + "/user/0/Record");
     gotoFrame("admin_edit_header");
     assertTextPresent("_guest_");
     assertTextPresent("[ Group membership ]");
     clickLink("recordTree");
     gotoRootWindow();
     gotoFrame("admin_edit_user_0");
     assertTextPresent("Melati guest user tree");
     clickLinkWithText("Melati guest user");
   }
   /**
    * 
    */
   public void testAdminTreeNoScript() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/0/Tree");
     assertTextPresent("Melati guest user tree");
   }
   /**
    * 
    */
   public void testAdminTableTree() {
     setScriptingEnabled(true);
     beginAt("/Admin/" + dbName + "/user/Table");
     gotoFrame("admin_navigation");
     clickLink("tableTree");
     gotoRootWindow();
     gotoFrame("admin_selection");
     assertTextPresent("User table tree");
     assertLinkPresentWithText("Melati guest user");
     assertLinkPresentWithText("Melati database administrator");
   }
   /**
    * 
    */
   public void testAdminSelectionWindow() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/SelectionWindow?returnfield=field_user");
     assertTextPresent("Select a User");
   }
   /**
    * 
    */
   public void testAdminSelectionWindowPrimarySelect() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/SelectionWindowPrimarySelect?returnfield=field_user");
     assertTextPresent("Full name");
   }
   /**
    * 
    */
   public void testAdminSelectionWindowSelection() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/SelectionWindowSelection?returnfield=field_user");
     assertTextPresent("Records 1 to 2 of 2");
   }
   /**
    * 
    */
   public void testAdminPopup() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/user/PopUp");
     assertTextPresent("Search User Table");
   }
   /**
    * 
    */
   public void testAdminDSD() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/DSD");
     assertTextPresent("Generated for _guest_");
     assertTextPresent("package org.melati.poem;");
   }
   /**
    * Move to login
    */
   public void testLoginWithContinuation() {
     setScriptingEnabled(false);
     beginAt("/Login/" + dbName + "?continuationURL=" + contextUrl("/index.html"));
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     assertTextPresent("Hello World!");
   }
   
   /**
    * Test setting the defaults.
    */
   public void testSetupStory() { 
     setScriptingEnabled(false);
     loginAsAdministrator();
     gotoFrame("admin_top");
     clickLinkWithText("Setup");    
     deleteRecord("setting","org.melati.admin.Admin.ScreenStylesheetURL", 0);
     deleteRecord("setting","org.melati.admin.Admin.PrimaryDisplayTable", 1);
     deleteRecord("setting","org.melati.admin.Admin.HomepageURL", 2);
     gotoPage("/Admin/" + dbName + "/setting/Main");
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_selection");
     assertTextPresent("No records found");
   }
 
   
   /**
    * Search for a set of records.
    */
   public void testSearchAndGoto() {
     setScriptingEnabled(false);
     beginAt("/Admin/" + dbName + "/Main");
     gotoRootWindow();
     gotoFrame("admin_top");
     setWorkingForm("goto");
     selectOption("goto","Column");
     assertFormPresent("goto");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_navigation");
     clickLink("search");
     gotoWindow("admin_search");
     setTextField("field_displayname", "Id");
     selectOption("field_order-1","Id");
     selectOption("field_order-2","Owning table");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_selection");
     assertTextPresent("Records 1 to 9 of 9");
     String page = getPageSource();
     System.err.println(page);
   }
   /**
    * User story.
    */
   public void testCreateTableStory() { 
     setScriptingEnabled(false);
     loginAsAdministrator();
     gotoAddRecord("Table");
     setTextField("field_name", "test");
     setTextField("field_displayname", "Test");
     setTextField("field_description", "A Test table");
     setTextField("field_displayorder", "0");
     selectOption("field_category","Normal");
     submit();
     assertTextPresent("Done");
     String tableTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     gotoPage("/Admin/" + dbName + "/tableinfo/" + tableTroid + "/Main"); 
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     gotoFrame("admin_edit_header");
     clickLinkWithText("Column");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");   
     gotoFrame("admin_edit_tableinfo_" + tableTroid);
     clickLink("create_columninfo");
     setTextField("field_name", "test");
     setTextField("field_description", "A Test column");
     setTextField("field_displayorder", "0");
     checkCheckbox("field_usercreateable");
     checkCheckbox("field_indexed");
     // We want to duplicate
     //checkCheckbox("field_unique");
     setTextField("field_displayname", "Test");
     checkCheckbox("field_nullable");
     setTextField("field_size", "20");
     setTextField("field_width", "20");
     setTextField("field_height", "1");
     setTextField("field_precision", "1");
     setTextField("field_scale", "1");
     submit();
     assertTextPresent("Done");
     String columnTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     
 
     gotoAddRecord("Test");
     setTextField("field_test", "test");
     submit();
     assertTextPresent("Done");
     String recordTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     
     clickLink("continue");
     gotoPage("/Admin/" + dbName + "/test/" + recordTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_test_" + recordTroid);
     submit("action","Duplicate");
     assertTextPresent("Done");
     String href = getElementAttributByXPath(
         "//a[@id='" + "continue" + "']", "href");
     System.err.println("Continue:" + href);
     clickLink("continue");
     
     // Records will be sorted by id
     deleteRecord("test", "test", new Integer(recordTroid).intValue());
     deleteRecord("test", "test", new Integer(recordTroid).intValue() + 1);
     
     gotoPage("/Admin/" + dbName + "/columninfo/" + columnTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_columninfo_" + columnTroid);
     submit("action","Delete");
     assertTextPresent("Done");
 
     gotoPage("/Admin/" + dbName + "/tableinfo/" + tableTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_tableinfo_" + tableTroid);
     submit("action","Delete");
     clickLink("edit_columninfo_" + (new Integer(columnTroid).intValue() -1));
     submit("action","Delete");
     assertTextPresent("Done");
     clickLink("continue");
     assertTextPresent("Done");
     clickLink("continue");
     assertTextPresent("Melati Database Admin Suite - Melatijunit database");
   }
   private void gotoAddRecord(String table) {
     gotoRootWindow();
     gotoFrame("admin_top");
     selectOption("goto",table);
     assertFormPresent("goto");
     setWorkingForm("goto");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_navigation");
     clickLink("add");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
   }
   /**
    * Start and end at top window.
    * @param tableName 
    * @param uniqueKeyValue
    */
   private void deleteRecord(String tableName, String uniqueKeyValue, int troid) {
     gotoPage("/Admin/" + dbName + "/" + tableName + "/Main");
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_selection");
     clickLinkWithText(uniqueKeyValue);
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     gotoFrame("admin_edit_" + tableName + "_" + troid);
     clickButton("delete");
     gotoPage("/Admin/" + dbName + "/Main");
   }
   /**
    * Returns us to top frame.
    */
   private void loginAsAdministrator() {
     beginAt("/Admin/" + dbName + "/Main");
     gotoFrame("admin_top");
     clickButton("login");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
   }
 }
