 package info.harmia.polyglot.springapp.mvc.core.service;
 
 import info.harmia.polyglot.springapp.mvc.core.model.Department;
 import info.harmia.polyglot.springapp.mvc.core.model.Employee;
 import info.harmia.polyglot.springapp.mvc.core.model.Municipality;
 import info.harmia.polyglot.springapp.mvc.core.repositories.EmployeeRepository;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.jpa.JpaSystemException;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: harmia
  * Date: 16.4.2013
  * Time: 17:08
  * Copyright (C) 2013 Juhana "harmia" Harmanen
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Service
 public class DefaultEmployeeService implements EmployeeService {
     @Autowired
     private EmployeeRepository employeeRepository;
     @Autowired
     private DepartmentService departmentService;
     @Autowired
     private MunicipalityService municipalityService;
 
     @Override
     public List<Employee> listEmployees() {
         return employeeRepository.findAll();
     }
 
     @Override
     public void addEmployee(EmployeeForm employeeForm) {
         Employee employee = new Employee();
         employee.setFirstName(employeeForm.getFirstName());
         employee.setLastName(employeeForm.getLastName());
         employee.setEmail(employeeForm.getEmail());
 
         Department department = departmentService.getDepartment(employeeForm.getDepartmentId());
         department.addEmployee(employee);
         employee.setDepartment(department);
 
         Municipality municipality = municipalityService.getMunicipality(employeeForm.getMunicipalityId());
         employee.setMunicipality(municipality);
 
        employee.setContractBeginDate(employeeForm.getContractBeginDate());
         employeeRepository.save(employee);
     }
 
     @Override
     public boolean deleteEmployee(Long employeeId) {
         try {
             employeeRepository.delete(employeeRepository.findOne(employeeId));
             return true;
         } catch (JpaSystemException e) {
             return false;
         }
     }
 
     @Override
     public boolean changeDepartment(Long employeeId, Long departmentId) {
         try {
             Employee employee = employeeRepository.findOne(employeeId);
             employee.setDepartment(departmentService.getDepartment(departmentId));
             employeeRepository.save(employee);
             return true;
         } catch (JpaSystemException e) {
             return false;
         }
     }
 
     @Override
     public JSONArray listEmployeesJson() throws JSONException {
         JSONArray employeeArray = new JSONArray();
         for(Employee employee : employeeRepository.findAll()) {
             JSONObject employeeJSON = new JSONObject();
             employeeJSON.put("id", employee.getId());
             employeeJSON.put("firstName", employee.getFirstName());
             employeeJSON.put("lastName", employee.getLastName());
             employeeJSON.put("email", employee.getEmail());
             JSONObject departmentJSON = new JSONObject();
             departmentJSON.put("id", employee.getDepartment().getId());
             departmentJSON.put("name", employee.getDepartment().getName());
             employeeJSON.put("department", departmentJSON);
             employeeJSON.put("contractBeginDate", employee.getContractBeginDate());
             employeeArray.put(employeeJSON);
         }
         return employeeArray;
     }
 }
