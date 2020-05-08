 package tests;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import java.text.MessageFormat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pages.*;
 
 public class Homework4Test  {
 
     @Before
     public void setUp() {
//        System.setProperty("baseUrl", "http://ci.itcollege.ee/part4example");
         System.setProperty("baseUrl", "http://localhost:8080/jeeHomework4/");
     }
 
     @Test
     public void viewPagesHasDisabledControls_1() {
         String code = randomString();
         ViewPage viewPage = Pages.goToAddPage().insert(code).view(code);
 
         assertThat(viewPage.element("nameBox")
                 .getAttribute("disabled"), is(notNullValue()));
         assertThat(viewPage.element("codeBox")
                 .getAttribute("disabled"), is(notNullValue()));
         assertThat(viewPage.element("superUnitCode")
                 .getAttribute("disabled"), is(notNullValue()));
     }
 
     @Test
     public void viewPagesBackLinkTakesToSearchPage_1() {
         String code = randomString();
         ViewPage viewPage = Pages.goToAddPage().insert(code).view(code);
 
         SearchPage searchPage = viewPage.clickBack();
 
         assertThat(searchPage.getPagePartFromUrl(), is(Pages.SEARCH_PAGE));
     }
 
     @Test
     public void viewHasNoAddButton_1() {
         String code = randomString();
         ViewPage viewPage = Pages.goToAddPage().insert(code).view(code);
 
         viewPage.assertElementNotPresent("addButton");
     }
 
     @Test
     public void viewShowsNameAndCode_2() {
         String name = randomString();
         String code = randomString();
 
         SearchPage searchPage = Pages.goToAddPage().insert(name, code);
 
         ViewPage viewPage = searchPage.view(code);
 
         String actualName = viewPage.element("nameBox").getAttribute("value");
         String actualCode = viewPage.element("codeBox").getAttribute("value");
 
         assertThat(actualName, is(name));
         assertThat(actualCode, is(code));
     }
 
     @Test
     public void superUnitSelectIsPresent_2() {
         AddPage addPage = Pages.goToAddPage();
         addPage.assertElementPresent("superUnitCode");
     }
 
     @Test
     public void superUnitDataCanBeInserted_2() {
         String codeSub = randomString();
         String codeSuper = randomString();
 
         Pages.goToAddPage().insert("super", codeSuper);
         Pages.goToAddPage().insert("sub2", codeSub, codeSuper);
 
         ViewPage viewPage = Pages.goToSearchPage().view(codeSub);
 
         assertThat(viewPage.getSelectedSuperUnit(), is(codeSuper));
     }
 
     @Test
     public void viewShowsSubunits_1() {
         String codeSub1 = randomString();
         String codeSub2 = randomString();
         String codeSuper = randomString();
 
         Pages.goToAddPage().insert("super", codeSuper);
         Pages.goToAddPage().insert("sub1", codeSub1, codeSuper);
         Pages.goToAddPage().insert("sub2", codeSub2, codeSuper);
 
         ViewPage viewPage = Pages.goToSearchPage().view(codeSuper);
 
         viewPage.assertElementPresent("sub_" + codeSub1);
         viewPage.assertElementPresent("sub_" + codeSub2);
     }
 
     @Test
     public void searchPageExists_10() {
         Pages.goToSearchPage().assertElementPresent("listTable");
     }
 
     @Test
     public void addPageExists_5() {
         Pages.goToAddPage().assertElementPresent("formTable");
     }
 
     @Test
     public void searchPageHasValidMenu_2() {
         assertThat(Pages.goToSearchPage().menu().getIds(), contains(
                 "menu_Search", "menu_Add", "menu_ClearData"));
     }
 
     @Test
     public void addPageHasValidMenu_2() {
         assertThat(Pages.goToAddPage().menu().getIds(), contains(
                 "menu_Search", "menu_Add", "menu_ClearData"));
     }
 
     @Test
     public void unitsCanBeAdded_2() {
         String testCode = randomString();
 
         SearchPage searchPage = Pages.goToAddPage().
                 insert("testName", testCode);
 
         searchPage.assertElementPresent("row_" + testCode);
     }
 
     @Test
     public void allDataCanBeDeleted_2() {
         SearchPage searchPage = Pages.goToSearchPage().menu().clearData();
         assertThat(searchPage.getIdsStartingWith("row_"), is(empty()));
     }
 
     @Test
     public void unitsCanBeDeleted_2() {
         SearchPage searchPage = Pages.goToSearchPage().menu().clearData();
         String testCode1 = randomString();
         String testCode2 = randomString();
 
         searchPage = Pages.goToAddPage().insert(testCode1);
         searchPage = Pages.goToAddPage().insert(testCode2);
         searchPage.delete(testCode1);
 
         searchPage.assertElementNotPresent("row_" + testCode1);
         searchPage.assertElementPresent("row_" + testCode2);
     }
 
     private static long seed = System.nanoTime();
     private String randomString() {
         return String.valueOf(seed++);
     }
 
     @Test
     public void listCanBeFiltered_2() {
         Pages.goToSearchPage().menu().clearData();
 
         String testCode = randomString();
         Pages.goToAddPage().insert("-" + testCode + "-");
         Pages.goToAddPage().insert("otherCode");
 
         SearchPage searchPage = Pages.goToSearchPage();
         searchPage = searchPage.search(testCode);
 
         assertThat(searchPage.getIdsStartingWith("row_"),
                 contains(MessageFormat.format("row_-{0}-", testCode)));
     }
 
     @Test
     public void redirectsAfterInsert_1() {
         SearchPage searchPage = Pages.goToAddPage()
                 .insert(randomString());
 
         assertThat(searchPage.getPagePartFromUrl(), is(Pages.SEARCH_PAGE));
     }
 }
