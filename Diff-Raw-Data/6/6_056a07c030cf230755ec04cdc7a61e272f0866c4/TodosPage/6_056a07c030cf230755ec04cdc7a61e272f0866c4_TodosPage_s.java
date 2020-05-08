 package com.company.todos.pages;
 
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.selectFirst;
 
 import java.util.List;
 
 import net.thucydides.core.annotations.DefaultUrl;
 import net.thucydides.core.pages.PageObject;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 
 import ch.lambdaj.function.matcher.Predicate;
 
 /**
  * Page object for the todos page.
  *
  * @author Daniel Imre
  *
  */
 @DefaultUrl("http://localhost:8080/todos")
 public class TodosPage extends PageObject {
     @FindBy(id = "todos-list")
     private WebElement todosList;
 
    @FindBy(id = "footer")
    private WebElement footer;

     @FindBy(id = "new-todo")
     private WebElement newTodoField;
 
     @FindBy(id = "todos-count")
     private WebElement status;
 
     @FindBy(css = "#header h1")
     private WebElement headerTitle;
 
     /**
      * Constructs a todos page object.
      *
      * @param driver the driver to use
      */
     public TodosPage(WebDriver driver) {
         super(driver);
     }
 
     /**
      * Checks if the footer is visible.
      *
      * @return true if the footer visible, otherwise false
      */
     public boolean isFooterVisible() {
        return footer.isDisplayed();
     }
 
     /**
      * Returns true if the todos list is empty.
      *
      * @return true if empty, otherwise false
      */
     public boolean isTodosListEmpty() {
         return getTodosInList().size() == 0;
     }
 
     /**
      * Enters text into new todo field and hits enter.
      *
      * @param todoText the text to be added
      */
     public void addNewTodo(String todoText) {
         $(newTodoField).type(todoText);
         newTodoField.sendKeys(Keys.ENTER);
     }
 
     /**
      * Gets the text of todos in todos list.
      *
      * @return collection of todo texts
      */
     public Iterable<? super String> getTodoTexts() {
         return extract(getTodosInList(), on(WebElement.class).getText());
     }
 
     /**
      * Gets the value of the new todo field.
      *
      * @return the value
      */
     public String getNewTodoFieldText() {
         return $(newTodoField).getTextValue();
     }
 
     /**
      * Returns true if the todo with the specified text is marked complete.
      *
      * @param todoTextAdded the todo's text
      * @return true if the todo is complete, otherwise false
      */
     public boolean isTodoMarkedComplete(final String todoTextAdded) {
         return $(getTodoWithText(todoTextAdded).findElement(By.tagName("input"))).isSelected();
     }
 
     /**
      * Gets the status text in the footer.
      *
      * @return the status text
      */
     public String getStatusText() {
         return $(status).getText();
     }
 
     /**
      * Gets the text in the header title.
      * @return the title text
      */
     public String getHeaderText() {
         return $(headerTitle).getText();
     }
 
     private List<WebElement> getTodosInList() {
         return todosList.findElements(By.cssSelector("li"));
     }
 
     private WebElement getTodoWithText(final String todoTextAdded) {
         return selectFirst(getTodosInList(), new Predicate<WebElement>() {
             @Override
             public boolean apply(WebElement item) {
                 return todoTextAdded.equals(item.getText());
             }
         });
     }
 
 }
