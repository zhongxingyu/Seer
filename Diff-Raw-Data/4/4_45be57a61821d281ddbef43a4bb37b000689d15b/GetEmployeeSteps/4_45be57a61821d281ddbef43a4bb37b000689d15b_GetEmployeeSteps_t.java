 package com.akrantha.emanager.eservice.steps;
 
 import org.hamcrest.MatcherAssert;
 import org.hamcrest.Matchers;
 import org.jbehave.core.annotations.Then;
 import org.jbehave.core.annotations.When;
 
 import com.akrantha.emanager.eservice.EmployeeService;
 import com.akrantha.emanager.restclient.RestClientException;
 
 public class GetEmployeeSteps extends AbstractEServiceSteps {
 
     public GetEmployeeSteps(EmployeeService employeeService, EmployeeSharedData employeeSharedData) {
         super(employeeService, employeeSharedData);
     }
 
     @When("I request for all employees in system")
     public void whenIRequestForAllEmployeesInSystem() {
         getEmployeeSharedData().setEmployeeCollection(getEmployeeService().getEmployees());
     }
 
     @Then("I should get list of all employees")
     public void thenIShouldGetListOfAllEmployees() {
        MatcherAssert.assertThat(getEmployeeSharedData().getEmployeeCollection().getEmployees()
                .contains(getEmployeeSharedData().getEmployee()), Matchers.is(true));
     }
 
     @When("I request for details of an existing employee")
     public void whenIRequestForDetailsOfAnExistingEmployee() {
         getEmployeeSharedData().setResponseEmployee(
                 getEmployeeService().getEmployee(getEmployeeSharedData().getEmployee().getId()));
     }
 
     @Then("I should get employee in response")
     public void thenIShouldGetEmployeeInResponse() {
         MatcherAssert.assertThat(getEmployeeSharedData().getResponseEmployee(),
                 Matchers.is(getEmployeeSharedData().getEmployee()));
     }
 
     @When("I request for details of an employee with invalid employee id")
     public void whenIRequestForDetailsOfAnEmployeeWithInvalidEmployeeId() {
         try {
             getEmployeeService().getEmployee(-10);
         } catch (RestClientException e) {
             getEmployeeSharedData().setError(e.getErrorDTO());
         }
     }
 
 }
