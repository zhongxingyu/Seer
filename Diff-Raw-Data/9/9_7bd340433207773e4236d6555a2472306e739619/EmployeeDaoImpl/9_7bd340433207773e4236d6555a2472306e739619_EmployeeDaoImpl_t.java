 package com.madrone.lms.dao.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Hibernate;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 
 import com.madrone.lms.dao.EmployeeDao;
 import com.madrone.lms.entity.Employee;
 
 @Repository("employeeDao")
 public class EmployeeDaoImpl extends AbstractDaoImpl<Employee, String> 
 							implements EmployeeDao {
 
 	public EmployeeDaoImpl() {
 		super(Employee.class);
 	}
 	
 	@Override
 	public Employee findByIdWithLeaves(String id) {
 		Employee e = findById(id);
 		
 		if(e != null) {
 			Hibernate.initialize(e.getEmployeeLeaves());			
 		}
 		
 		return e;
 	}
 
 	@Override
 	public Employee findByEmailAddress(String primaryEmail) {
 		List<Criterion> criterionList = new ArrayList<Criterion>();
 		criterionList.add(Restrictions.eq("primaryEmail", primaryEmail));
 		List<Employee> employees = findByCriteria(criterionList);
 				
 		if(employees.size() > 1) {
			throw new AssertionError("Duplicate employee records with same " +
 					"primary email address.");
 		}
 		
 		return employees.isEmpty() ? null : employees.get(0);
 	}
 	
 	@Override
 	public Employee findByEmailAddressWithLeaves(String primaryEmail) {
 		Employee e = findByEmailAddress(primaryEmail);
 		
 		if(e != null) {
 			Hibernate.initialize(e.getEmployeeLeaves());			
 		}
 		
 		return e;
 	}
 	
 	@Override
 	public void saveEmployee(Employee employee) {
 		saveOrUpdate(employee);
 	}
 
 	@Override
     public String findRole(String userName) {
             List<Criterion> criterionList = new ArrayList<Criterion>();
             criterionList.add(Restrictions.eq("primaryEmail", userName));
             List<Employee> employees = findByCriteria(criterionList);
             
             return employees.isEmpty() ? "":employees.get(0).getRole().getId();
     }
 }
