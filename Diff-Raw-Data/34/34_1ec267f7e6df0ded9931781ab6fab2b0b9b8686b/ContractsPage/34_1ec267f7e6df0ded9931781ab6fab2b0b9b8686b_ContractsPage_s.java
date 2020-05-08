 package com.roosterpark.rptime.selenium.page;
 
 import com.roosterpark.rptime.selenium.AdminPageObject;
 import com.roosterpark.rptime.selenium.control.Button;
 import com.roosterpark.rptime.selenium.control.CheckBox;
 import com.roosterpark.rptime.selenium.control.complex.form.CreateContractForm;
 import com.roosterpark.rptime.selenium.control.complex.list.contract.ContractEditList;
 import com.roosterpark.rptime.selenium.exception.NotDirectlyOpenableException;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  * User: John
  * Date: 10/25/13
  * Time: 2:11 PM
  */
 public class ContractsPage extends AdminPageObject {
 
     private static final String CHECK_BOX_ID = "showExpiredContracts";
 
     private ContractEditList contractEditList;
     private CheckBox expiredContractsCheckBox;
 
     public ContractsPage(WebDriver driver) {
         super(driver);
     }
 
     @Override
     public void openPage() {
         throw new NotDirectlyOpenableException("Contracts page should not be opened directly.");
     }
 
     public CreateContractForm getCreateContractForm() {
         NewButton newButton = new NewButton(getWebDriver());
         newButton.click();
         return new CreateContractForm(getWebDriver());
     }
 
     public void waitForContractsRedraw() {
         WebElement contractsDiv = getWebDriver().findElement(By.id("contracts"));
         WebElement contractsHeader = contractsDiv.findElement(By.xpath(".//div[@class='panel-heading']/h4"));
         WaitForVisible waitForVisible = new WaitForVisible(contractsHeader);
         waitForVisible.defaultWaitForVisible();
     }
 
     public void initContractEditList() {
         contractEditList = new ContractEditList(getWebDriver());
         contractEditList.init();
     }
 
     public ContractEditList getContractEditList() {
         return contractEditList;
     }
 
     public void checkShowExpiredContracts() {
         if (expiredContractsCheckBox == null) {
             expiredContractsCheckBox = new CheckBox(getWebDriver(), CHECK_BOX_ID);
         }
         expiredContractsCheckBox.check();
     }
 
     public boolean isShowExpiredContractsChecked() {
         if (expiredContractsCheckBox == null) {
             expiredContractsCheckBox = new CheckBox(getWebDriver(), CHECK_BOX_ID);
         }
         return expiredContractsCheckBox.isChecked();
     }
 
     private class NewButton extends Button<Void> {
 
         private static final String NEW_BUTTON = "new";
 
        private WaitForVisible waitForVisible;

         public NewButton(WebDriver driver) {
             super(driver, NEW_BUTTON);
         }
 
         @Override
         public Void click() {
            waitForVisible = new WaitForVisible(getElement());
             waitForVisible.defaultWaitForVisible();
             getElement().click();
             return null;
         }
     }
 
 }
