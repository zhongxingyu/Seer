 package tests;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.Test;
 
 import pages.*;
 
 public class Homework3Test  {
 
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
         Pages.goToSearchPage().menu().isValid();
     }
 
     @Test
     public void addPageHasValidMenu_2() {
         Pages.goToAddPage().menu().isValid();
     }
 
     @Test
     public void unitsCanBeAdded_2() {
         String testCode = String.valueOf(System.nanoTime());
 
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
     public void sampleDataCanBeInserted_3() {
         SearchPage searchPage = Pages.goToSearchPage().menu().clearData();
         searchPage.menu().insertSampleData();
 
         assertThat(searchPage.getIdsStartingWith("row_"),
                 contains("row_1", "row_1-1", "row_1-1-1",
                          "row_1-1-2", "row_1-2", "row_2"));
     }
 
     @Test
     public void unitsCanBeDeleted_2() {
         SearchPage searchPage = Pages.goToSearchPage().menu().clearData();
         long nanoTime = System.nanoTime();
         String testCode1 = String.valueOf(nanoTime++);
         String testCode2 = String.valueOf(nanoTime++);
 
         searchPage = Pages.goToAddPage().insert("testName", testCode1);
         searchPage = Pages.goToAddPage().insert("testName", testCode2);
         searchPage.delete(testCode1);
 
         searchPage.assertElementNotPresent("row_" + testCode1);
         searchPage.assertElementPresent("row_" + testCode2);
     }
 
     @Test
     public void listCanBeFiltered_2() {
         Pages.goToSearchPage().menu().clearData();
         Pages.goToSearchPage().menu().insertSampleData();
 
         SearchPage searchPage = Pages.goToSearchPage();
         searchPage = searchPage.search("c");
 
         assertThat(searchPage.getIdsStartingWith("row_"),
                 contains("row_1", "row_1-1-2", "row_1-2"));
     }
 
     @Test
     public void redirectsAfterInsert_1() {
         SearchPage searchPage =
                 Pages.goToAddPage().insert("testName", "testCode");
 
         assertThat(searchPage.getPagePartFromUrl(), is(Pages.SEARCH_PAGE));
     }
 }
