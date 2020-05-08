 package com.fs.humanResources.admin.employee;
 
 import com.fs.domain.page.admin.AdminPage;
 import com.fs.domain.page.admin.dialog.EditEmployeeDialog;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForDelete;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForEdit;
 import com.fs.domain.page.browse.BrowseEmployeesPage;
 import com.fs.humanResources.common.BaseSeleniumTest;
 import com.fs.humanResources.model.address.entities.Address;
 import com.fs.humanResources.model.employee.entities.Employee;
 import org.junit.*;
 
 import java.text.ParseException;
 
 public class EditEmployeeDialogTest extends BaseSeleniumTest {
 
     AdminPage adminPage;
 
     EditEmployeeDialog editEmployeeDialog;
 
     Employee employee;
 
     Address address;
 
     @Before
     public void setUp() throws ParseException {
         address = new Address();
         address.setHouseNumber("50");
         address.setAddressFirstLine(persitenceHelper.getUniqueString(8));
         address.setAddressSecondLine("Domain Court");
         address.setTownCity("Progammer City");
         address.setPostCode("AB1CDX");
         address.setPrimaryAddress(true);
 
         employee = new Employee();
         employee.setFirstName("James");
         employee.setLastName(persitenceHelper.getUniqueString(8));
 
         employee.setDateOfBirth(simpleDateFormat.parse("15/07/1976"));
 
         employee.addAddress(address);
 
         persitenceHelper.beginTransaction();
 
         persitenceHelper.persistNewEmployee(employee);
         persitenceHelper.addDeletionCandidate(employee);
         persitenceHelper.commitTransaction();
 
         humanResourcesHome.openHomePage();
         humanResourcesHome.assertPageIsPresent();
 
         adminPage = humanResourcesHome.clickLoginBtn();
         adminPage.assertPageIsPresent();
     }
 
     private void navigateToEditEmployee() {
         adminPage.openEmployeeAdminMenu();
 
         FindEmployeeDialogForEdit findEmployeeDialogForEdit = adminPage.clickEditEmployeeMenuItem();
         findEmployeeDialogForEdit.assertDialogIsPresent();
 
         findEmployeeDialogForEdit.setEmployeeId(employee.getId() + "");
         editEmployeeDialog = findEmployeeDialogForEdit.clickFindEmployeeBtn();
 
         editEmployeeDialog.assertDialogIsPresent();
     }
 
     @After
     public void tearDown() {
         persitenceHelper.deleteCandidates();
         super.tearDown();
     }
 
     @Test
     public void editEmployeeFormElements_displayedAsExpected() {
         navigateToEditEmployee();
         Assert.assertEquals("First Name:", editEmployeeDialog.firstNameLabelDisplayed().getText());
         editEmployeeDialog.firstNameInputDisplayed();
 
         Assert.assertEquals("Last Name:", editEmployeeDialog.lastNameLabelDisplayed().getText());
         editEmployeeDialog.lastNameInputDisplayed();
 
         Assert.assertEquals("DOB (dd/mm/yyyy):", editEmployeeDialog.dateOfBirthLabelDisplayed().getText());
         editEmployeeDialog.dateOfBirthInputDisplayed();
 
         Assert.assertEquals("House No:", editEmployeeDialog.houseNumberLabelDisplayed().getText());
         editEmployeeDialog.houseNumberInputDisplayed();
 
         Assert.assertEquals("Address Line 1:", editEmployeeDialog.addressFirstLineLabelDisplayed().getText());
         editEmployeeDialog.addressFirstLineInputDisplayed();
 
         Assert.assertEquals("Address Line 2:", editEmployeeDialog.addressSecondLineLabelDisplayed().getText());
         editEmployeeDialog.addressSecondLineInputDisplayed();
 
         Assert.assertEquals("Town/City:", editEmployeeDialog.townCityLabelDisplayed().getText());
         editEmployeeDialog.townCityInputDisplayed();
 
         Assert.assertEquals("Postcode:", editEmployeeDialog.postCodeLabelDisplayed().getText());
         editEmployeeDialog.postCodeInputDisplayed();
     }
 
     @Test
     public void validationMessages_displayedAsExpected() {
         navigateToEditEmployee();
         editEmployeeDialog.firstNameInputDisplayed().clear();
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertGrowlMessageDisplayed("Firstname is required");
 
         editEmployeeDialog.setFirstName(employee.getFirstName());
 
         editEmployeeDialog.lastNameInputDisplayed().clear();
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertGrowlMessageDisplayed("Lastname is required");
 
         editEmployeeDialog.setLastName(employee.getLastName());
 
         editEmployeeDialog.houseNumberInputDisplayed().clear();
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertGrowlMessageDisplayed("House No is required");
 
         editEmployeeDialog.setHouseNumber(employee.getAddressList().get(0).getHouseNumber());
 
         editEmployeeDialog.postCodeInputDisplayed().clear();
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertGrowlMessageDisplayed("Postcode is required");
 
         editEmployeeDialog.setPostcode(employee.getAddressList().get(0).getPostCode());
 
         editEmployeeDialog.dateOfBirthInputDisplayed().clear();
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertGrowlMessageDisplayed("Date Of Birth is required");
     }
 
     @Test
     public void employeeDetails_displayedAsExpected() {
         navigateToEditEmployee();
         Assert.assertEquals(employee.getId() + "", editEmployeeDialog.employeeIdInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getFirstName(), editEmployeeDialog.firstNameInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getLastName(), editEmployeeDialog.lastNameInputDisplayed().getAttribute("value"));
         Assert.assertEquals(formateDate(employee.getDateOfBirth()), editEmployeeDialog.dateOfBirthInputDisplayed().getAttribute("value"));
 
         Assert.assertEquals(employee.getAddressList().get(0).getHouseNumber(), editEmployeeDialog.houseNumberInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getAddressFirstLine(), editEmployeeDialog.addressFirstLineInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getAddressSecondLine(), editEmployeeDialog.addressSecondLineInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getTownCity(), editEmployeeDialog.townCityInputDisplayed().getAttribute("value"));
         Assert.assertEquals("AB1-CDX", editEmployeeDialog.postCodeInputDisplayed().getAttribute("value"));
     }
 
     @Test
     public void employeeDetails_updatedAsExpected() {
         navigateToEditEmployee();
         editEmployeeDialog.setFirstName(employee.getFirstName() + "-upt");
         editEmployeeDialog.setLastName(employee.getLastName() + "-upt");
 
         editEmployeeDialog.setHouseNumber(employee.getAddressList().get(0).getHouseNumber() + "-upt");
 
         editEmployeeDialog.clickEditEmployeeBtn();
         editEmployeeDialog.assertDialogIsNotPresent();
 
         FindEmployeeDialogForEdit findEmployeeDialogForEdit = adminPage.clickEditEmployeeMenuItem();
         findEmployeeDialogForEdit.assertDialogIsPresent();
 
         findEmployeeDialogForEdit.setEmployeeId(employee.getId() + "");
         editEmployeeDialog = findEmployeeDialogForEdit.clickFindEmployeeBtn();
         findEmployeeDialogForEdit.assertDialogIsNotPresent();
 
         editEmployeeDialog.assertDialogIsPresent();
 
         Assert.assertEquals(employee.getId() + "", editEmployeeDialog.employeeIdInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getFirstName() + "-upt", editEmployeeDialog.firstNameInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getLastName() + "-upt", editEmployeeDialog.lastNameInputDisplayed().getAttribute("value"));
 
         Assert.assertEquals(employee.getAddressList().get(0).getHouseNumber() + "-upt", editEmployeeDialog.houseNumberInputDisplayed().getAttribute("value"));
     }
 
     @Test
     public void employee_canBeEdited_fromBrowse() {
         adminPage.assertBrowseMenuDisplayed().click();
         BrowseEmployeesPage browseEmployeesPage = adminPage.clickBrowseEmployeesMenuItem();
         browseEmployeesPage.assertDialogIsPresent();
 
         editEmployeeDialog = browseEmployeesPage.editRowWithText(employee.getLastName());
 
         editEmployeeDialog.setLastName(employee.getLastName() + "-upt");
 
         editEmployeeDialog.clickEditEmployeeBtn();
        editEmployeeDialog.assertDialogIsNotPresent();
 
         adminPage.openEmployeeAdminMenu();
 
         FindEmployeeDialogForEdit findEmployeeDialogForEdit = adminPage.clickEditEmployeeMenuItem();
         findEmployeeDialogForEdit.assertDialogIsPresent();
 
         findEmployeeDialogForEdit.setEmployeeId(employee.getId() + "");
         editEmployeeDialog = findEmployeeDialogForEdit.clickFindEmployeeBtn();
         findEmployeeDialogForEdit.assertDialogIsNotPresent();
 
         editEmployeeDialog.assertDialogIsPresent();
 
         Assert.assertEquals(employee.getId() + "", editEmployeeDialog.employeeIdInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getLastName() + "-upt", editEmployeeDialog.lastNameInputDisplayed().getAttribute("value"));
     }
 }
