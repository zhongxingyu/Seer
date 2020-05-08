 package com.inertia.solutions.spring.rest.app.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.FilterType;
 
 import com.inertia.solutions.spring.rest.app.bean.Employee;
 
 /**
  * 
  * This class is a basic Spring IOC Container config
  * which does a @Component @Services scan on the base package name,
  * and also allows the AspectJ Style proxy generation.
  *
  */
 @Configuration
@ComponentScan(basePackages = {"com.inertia.solutions.spring.mvc.app"}, 
 	excludeFilters = { @Filter( type=FilterType.ANNOTATION, value=Configuration.class ) })
 public class SpringConfiguration {
 
 	@Bean(name = "employeeMemoryDatabase")
 	public Map<Integer, Employee> getEmployeeMemoryDatabase() {
 		Map<Integer, Employee> returnVal = new HashMap<Integer, Employee>();
 		
 		Employee seedEmployee = new Employee();
 		seedEmployee.setEmployeeId(new Integer(101));
 		seedEmployee.setFirstName("Sam");
 		seedEmployee.setLastName("Iam");
 		seedEmployee.setTitle("CEO");
 		seedEmployee.setSocialSecurity("111-222-3333");
 		
 		returnVal.put(seedEmployee.getEmployeeId(), seedEmployee);
 		return returnVal;
 	}
 }
