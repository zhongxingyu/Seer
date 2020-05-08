 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.pages;
 
 import static nl.surfnet.bod.web.WebUtils.not;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.FindBy;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.util.concurrent.Uninterruptibles;
 
 public class AbstractListPage extends AbstractPage {
 
   @FindBy(css = "table thead")
   private WebElement tableHeader;
 
   @FindBy(css = "table.table tbody")
   private WebElement table;
 
   @FindBy(id = "si_id")
   private WebElement searchInputField;
 
   @FindBy(id = "sb_id")
   private WebElement searchButton;
 
   public AbstractListPage(RemoteWebDriver driver) {
     super(driver);
   }
 
   public String getTable() {
     return table.getText();
   }
 
   public void delete(String... fields) {
     deleteForIcon("icon-remove", fields);
   }
 
   public void deleteAndVerifyAlert(String alertText, String... fields) {
     deleteForIconAndVerifyAlert("icon-remove", alertText, fields);
   }
 
   protected void deleteForIconAndVerifyAlert(String icon, String alertText, String... fields) {
     delete(icon, fields);
     Alert alert = getDriver().switchTo().alert();
     alert.getText().contains(alertText);
     alert.accept();
 
     // wait for the reload, row should be gone..
     Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
   }
 
   protected void deleteForIcon(String icon, String... fields) {
     delete(icon, fields);
     getDriver().switchTo().alert().accept();
 
     // wait for the reload, row should be gone..
     Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
   }
 
   protected void editRow(String... fields) {
     clickRowIcon("icon-pencil", fields);
   }
 
   protected void clickRowIcon(String icon, String... fields) {
     findRow(fields).findElement(By.cssSelector("a i[class~=" + icon + "]")).click();
   }
 
   private void delete(String icon, String... fields) {
     WebElement row = findRow(fields);
 
     WebElement deleteButton = row.findElement(By.cssSelector(String.format("a i[class~=%s]", icon)));
     deleteButton.click();
   }
 
   public boolean isTableEmpty() {
     try {
       table.findElements(By.tagName("tr"));
       return false;
     }
     catch (NoSuchElementException e) {
       return true;
     }
   }
 
   public List<WebElement> getRows() {
     return table.findElements(By.cssSelector("tbody tr"));
   }
 
   public WebElement findRow(String... fields) {
     List<WebElement> rows = getRows();
 
     for (final WebElement row : rows) {
       if (containsAll(row, fields)) {
         return row;
       }
     }
 
     throw new NoSuchElementException(String.format("row with fields '%s' not found in rows: '%s'", Joiner.on(',').join(
         fields), Joiner.on(" | ").join(Iterables.transform(rows, new Function<WebElement, String>() {
       @Override
       public String apply(WebElement row) {
         return row.getText();
       }
     }))));
   }
 
   private boolean containsAll(final WebElement row, String... fields) {
     return Iterables.all(Arrays.asList(fields), new Predicate<String>() {
       @Override
       public boolean apply(String field) {
         return row.getText().contains(field);
       }
     });
   }
 
   public boolean containsAnyItems() {
     try {
       table.getText();
     }
     catch (NoSuchElementException e) {
       return false;
     }
 
     return true;
   }
 
   public Integer getNumberOfRows() {
     int numberOfRows;
     try {
       numberOfRows = getRows().size();
     }
     catch (NoSuchElementException e) {
       numberOfRows = 0;
     }
 
     return numberOfRows;
   }
 
   /**
    * Overrides the default selected table by the given one in case there are
    * multiple tables on a page.
    * 
    * @param table
    *          Table to set.
    */
   protected void setTable(WebElement table) {
     this.table = table;
   }
 
   public void verifyRowWithLabelExists(String... labels) {
     findRow(labels);
   }
 
   public void verifyRowWithLabelDoesNotExist(String... labels) {
     try {
       findRow(labels);
       fail(String.format("Row related to [%s] exists, but should not be visible", Joiner.on(',').join(labels)));
     }
     catch (NoSuchElementException e) {
       // as expected
     }
   }
 
   public void verifyRowSequence(String sortColumn, boolean reverse, String... labels) {
     sortOn(sortColumn, reverse);
     List<WebElement> rows = getRows();
     assertTrue(labels.length <= rows.size());
 
     for (int i = 0; i < labels.length; i++) {
       assertTrue(containsAll(rows.get(i), labels[i]));
     }
   }
 
   public void search(String searchString) {
     if (StringUtils.hasText(searchString)) {
       searchInputField.sendKeys(searchString);
       searchButton.click();
     }
   }
 
   private void sortOn(String columnName, boolean reverse) {
     if (StringUtils.hasText(columnName)) {
       WebElement sortButton = null;
 
       String sortButtonSelector = "*[href$=\"" + "sort=" + columnName;
       try {
         sortButton = tableHeader.findElement(By.cssSelector(sortButtonSelector + "\"]"));
         // Click to sort, effect depends on current sorting, we don't know so
         // test it later on
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
         sortButton.click();
       }
       catch (NoSuchElementException e) {
         // Happens when list is already sorted
       }
 
       try {
         sortButton = tableHeader.findElement(By.cssSelector(sortButtonSelector + "&order=DESC\"]"));
         // Sorting is ascending now, if we must sort descending click the button
         if (reverse) {
          Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
           sortButton.click();
         }
       }
       catch (NoSuchElementException e) {
         // No descending button found, it should be ascending then
         sortButton = tableHeader.findElement(By.cssSelector(sortButtonSelector + "&order=ASC\"]"));
         // Sort again to sort them ascending
         if (not(reverse)) {
          Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
           sortButton.click();
         }
       }
     }
   }
 
   public void verifyRowsBySearch(String searchString, String... labels) {
     search(searchString);
 
     int expectedAmount = labels == null ? 0 : labels.length;
     assertThat(getNumberOfRows(), is(expectedAmount));
 
     for (String label : labels) {
       verifyRowWithLabelExists(label);
     }
   }
 
   public void verifyAmountOfRowsWithLabel(int expectedAmount, String... labels) {
     int matchedRows = 0;
 
     for (final WebElement row : getRows()) {
       if (containsAll(row, labels)) {
         matchedRows++;
       }
     }
 
     assertThat(matchedRows, is(expectedAmount));
   }
 
   public int getNumberFromRowWithLinkAndClick(String rowLabel, String linkPart, String tooltipTitle) {
     WebElement rowWithLink = findRow(rowLabel);
 
     WebElement link = rowWithLink.findElement(By.xpath(String.format(
         ".//a[contains(@href, '%s') and contains(@data-original-title, '%s')]", linkPart, tooltipTitle)));
 
     int number = Integer.parseInt(link.getText());
 
     link.click();
 
     return number;
   }
 }
