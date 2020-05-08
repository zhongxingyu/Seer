 package controllers;
 
 import java.util.*;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 import play.db.jpa.*;
 
 import views.html.*;
 
 import models.*;
 
 /**
  * Manage a database of employees
  */
 public class Application extends Controller {
     
     /**
      * Handle default path requests, redirect to employees list
      */
     public static Result index() {
         return GO_HOME;
     }
 
     /**
      * This result directly redirect to application home.
      */
     public static Result GO_HOME = redirect(
         routes.Application.list(0, "id", "asc", "")
     );
 
     /**
      * Display the paginated list of employees.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on employee last names
      */
     @Transactional(readOnly=true)
     public static Result list(int page, String sortBy, String order, String filter) {
         return ok(
             list.render(
                 Employee.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
     
     /**
      * Display the 'New employee form'.
      */
     @Transactional(readOnly=true)
     public static Result create() {
         Form<Employee> employeeForm = form(Employee.class);
         return ok(
             add.render(employeeForm)
         );
     }
     
     /**
     * Handle the 'New employee form' submission
      */
     @Transactional
     public static Result save() {
         Form<Employee> employeeForm = form(Employee.class).bindFromRequest();
         if(employeeForm.hasErrors()) {
             return badRequest(add.render(employeeForm));
         }
         employeeForm.get().save();
         flash("success", "Employee " + employeeForm.get().first_name 
             + " " + employeeForm.get().last_name + " has been added");
         return GO_HOME;
     }
 
     /**
      * Display the 'Edit form' of a existing Employee.
      *
      * @param id Id of the employee to edit
      */
     @Transactional(readOnly=true)
     public static Result edit(Long id) {
         Form<Employee> employeeForm = form(Employee.class).fill(
             Employee.findById(id)
         );
         return ok(
             edit.render(id, employeeForm)
         );
     }
     
     /**
      * Handle the 'Edit form' submission 
      *
      * @param id Id of the employee to edit
      */
     @Transactional
     public static Result update(Long id) {
         Form<Employee> employeeForm = form(Employee.class).bindFromRequest();
         if(employeeForm.hasErrors()) {
             return badRequest(edit.render(id, employeeForm));
         }
         employeeForm.get().update(id);
         flash("success", "Employee " + employeeForm.get().first_name 
             + " " + employeeForm.get().last_name + " has been updated");
         return GO_HOME;
     }

 }
