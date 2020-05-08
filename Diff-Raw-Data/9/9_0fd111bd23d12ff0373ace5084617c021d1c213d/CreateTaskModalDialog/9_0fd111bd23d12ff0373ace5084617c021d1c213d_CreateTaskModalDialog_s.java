 package com.teamdev.projects.test.web.component.modaldialog;
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.util.Elements;
 import com.teamdev.projects.test.web.component.AbstractComponent;
 import com.teamdev.projects.test.web.component.element.Button;
 import com.teamdev.projects.test.web.component.element.InputBox;
 import com.teamdev.projects.test.web.component.element.Link;
 import org.openqa.selenium.By;
 
 import static com.teamdev.projects.test.webdriver.WebDriverManager.getDriver;
 
 /**
  * @author Alexander Orlov
  */
 public class CreateTaskModalDialog extends AbstractComponent {
 
     public CreateTaskModalDialog(By... byExpressions) {
         super(byExpressions);
     }
 
     private InputBox inputBox(By by) {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.ADVANCED_TASK_CREATE_PANEL,
                 by, Locators.INPUT));
     }
 
     public InputBox nameInputBox() {
         return inputBox(Locators.TASK_NAME_INPUT);
     }
 
     public InputBox notesTextArea() {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.ADVANCED_TASK_CREATE_PANEL,
                 Locators.TASK_NOTES_INPUT, Locators.TEXT_AREA));
     }
 
     public InputBox tagsInputBox() {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.ADVANCED_TASK_CREATE_PANEL,
                 Locators.TAGS_INPUT));
     }
 
     public InputBox assigneeInputBox() {
        return new InputBox(Elements.addBy(getByExpressions(), Locators.TASK_ASSIGNEE_INPUT));
     }
 
     public InputBox estimateInputBox() {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.TASK_ESTIMATE_INPUT, Locators.INPUT_BOX));
     }
 
     public InputBox dueByInputBox() {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.TASK_DUE_BY, Locators.INPUT_BOX));
     }
 
     public InputBox scheduleInputBox() {
         return new InputBox(Elements.addBy(getByExpressions(), Locators.TASK_SCHEDULE));
     }
 
     public Button createButton() {
         return new Button(Elements.addBy(getByExpressions(), Locators.TASK_CREATE_BUTTON));
     }
 
     public Button dropDownToSelectAreaOrProject() {
         return new Button(Elements.addBy(getByExpressions(), Locators.TASK_DROP_LINK_FOR_PROJECT_AREAS_LIST));
     }
 
     public Link areaInDropDownList(AreaData areaData) {
         return new Link(Elements.addBy(getByExpressions(), Locators.TASK_DROP_SECTION,
                 By.linkText(areaData.getName())));
     }
 
     public Link projectInDropDownList(ProjectData projectData) {
         return new Link(Elements.addBy(getByExpressions(), Locators.TASK_DROP_SECTION,
                 By.linkText(projectData.getShortName())));
     }
 
     public boolean isClosed() {
         return !getDriver().findElement(Locators.MODAL_WINDOW).isDisplayed();
     }
 
 }
