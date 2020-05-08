 package ch.bli.mez.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.dao.EmployeeDAO;
 import ch.bli.mez.view.employee.EmployeePanel;
 import ch.bli.mez.view.employee.EmployeeView;
 
 /**
  * @author leandrafinger
  * @author mbrodmann
  * @version 2.0
  */
 public class EmployeeController {
   private EmployeeView view;
   private EmployeeDAO model;
   private final SearchController searchController;
 
   public EmployeeController() {
     this.model = new EmployeeDAO();
     this.searchController = new SearchController();
     this.view = new EmployeeView(searchController.getSearchPanel());
     addTabs();
   }
 
   public EmployeeView getView() {
     return view;
   }
 
   private void addTabs() {
     view.addTab("Neuer Mitarbeiter", createNewEmployeeTab());
     for (Employee employee : model.findAll()) {
        addEmployeeTab(employee);
     }
   }
   
   private void addEmployeeTab(Employee employee){
 	  view.addTab(employee.getFirstName() + " " + employee.getLastName(),
 	          createEmployeePanel(employee, false));
   }
 
   private EmployeePanel createNewEmployeeTab() {
     Employee employee = new Employee();
     EmployeePanel form = createEmployeePanel(employee, true);
     form.hideStatusButton();
     return form;
   }
 
   private EmployeePanel createEmployeePanel(Employee employee,
       Boolean isNewEmployee) {
     EmployeePanel form = new EmployeePanel();
     setFormActionListeners(employee, form, isNewEmployee);
 
     form.setFirstname(employee.getFirstName());
     form.setLastname(employee.getLastName());
     form.setCity(employee.getCity());
     if (employee.getPlz() != 0)
       form.setPlz(employee.getPlz().toString());
     form.setEmail(employee.getEmail());
     form.setHomeNumber(employee.getHomeNumber());
     form.setMobileNumber(employee.getMobileNumber());
     form.setStreet(employee.getStreet());
     return form;
   }
 
   public Employee updateEmployee(Employee employee, EmployeePanel form) {
	  if(!form.getPlz().equals(""))
     employee.setPlz(Integer.parseInt(form.getPlz()));
     employee.setFirstName(form.getFirstname());
     employee.setLastName(form.getLastname());
     employee.setStreet(form.getStreet());
     employee.setCity(form.getCity());
     employee.setMobileNumber(form.getMobileNumber());
     employee.setHomeNumber(form.getHomeNumber());
     employee.setEmail(form.getEmail());
     return employee;
   }
 
   public void setFormActionListeners(final Employee employee,
       final EmployeePanel form, final Boolean newEmployee) {
     form.setSaveEmployeeListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         form.hideErrors();
         form.hideConfirmation();
         if (!validateFields(form)) {
           return;
         }
         if (!newEmployee) {
           model.updateEmployee(updateEmployee(employee, form));
           form.showConfirmation(employee.getFirstName() + " "
               + employee.getLastName());
         } else {
           Employee safeEmployee = new Employee();
           safeEmployee = updateEmployee(safeEmployee, form);
           model.addEmployee(safeEmployee);
           view.addTab(
               safeEmployee.getFirstName() + " " + safeEmployee.getLastName(),
               createEmployeePanel(safeEmployee, false));
           form.cleanFields();
           form.showConfirmation(safeEmployee.getFirstName() + " "
               + safeEmployee.getLastName());
         }
 
       }
     });
 
     form.setStatusButtonListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         if (employee.getIsActive()){
         	employee.setIsActive(false);
         	form.setStatusButtonName("Aktivieren");
         }
         else {
         	employee.setIsActive(true);
         	form.setStatusButtonName("Deaktivieren");
         	view.removeTab(view.getSelectedIndex());
         }
         model.updateEmployee(employee);
       }
     });
   }
 
   public boolean validateFields(EmployeePanel panel) {
     boolean valid = true;
     if (panel.getFirstname().equals("")) {
       panel.showFirstNameError();
       valid = false;
     }
     if (panel.getLastname().equals("")) {
       panel.showLastNameError();
       valid = false;
     }
     try {
       if (!panel.getPlz().equals(""))
         Integer.parseInt(panel.getPlz());
     } catch (NumberFormatException e) {
       panel.showPlzError();
       valid = false;
     }
     return valid;
   }
 }
