 package com.openhr.taxengine.impl;
 
 import java.util.List;
 
 import com.openhr.data.EmpDependents;
 import com.openhr.data.Employee;
 import com.openhr.data.EmployeePayroll;
 import com.openhr.data.Exemptionstype;
 import com.openhr.factories.DeductionFactory;
 import com.openhr.taxengine.ExemptionCalculator;
 import com.openhr.taxengine.TaxDetails;
 
 public class BaseEC implements ExemptionCalculator {
 
 	public static final String SUPPORTING_SPOUSE = "Supporting Spouse";
 	public static final String CHILDREN = "Children";
 	public static final String BASIC_ALLOWANCE = "Basic Allowance";
 	
 	public static Integer SPOUSE = 0;
 	public static Integer CHILD = 1;
 	
 	public static Integer STUDENT = 0;
 	public static Integer NONE = 1;
	public static Integer WORKING = 2;
 	
 	@Override
 	public void calculate(Employee emp, EmployeePayroll empPayroll) {
 		TaxDetails taxDetails = TaxDetails.getTaxDetailsForCountry();
 		List<EmpDependents> dependents = emp.getDependents();
 		
 		List<Exemptionstype> exemptionsTypes = DeductionFactory.findAllExemptionTypes();
 		
 		// Handle Married person supporting spouse
 		if(emp.isMarried()) {
 			for(EmpDependents dependent : dependents) {
				if(dependent.getType().compareTo(SPOUSE) == 0
				  && dependent.getOccupationType().compareTo(NONE) == 0) {
 					empPayroll.addExemption(getExemptionType(exemptionsTypes, SUPPORTING_SPOUSE),
 							taxDetails.getExemption(TaxDetails.SUPPORTING_SPOUSE));
 				}
 			}
 		} 
 		
 		// Handle Children exemptions
 		int noOfChildern = 0;
 		for(EmpDependents dependent : dependents) {
 			if((dependent.getType().compareTo(CHILD) == 0)
 			&& ( dependent.getAge() < 18 || (dependent.getOccupationType().compareTo(STUDENT) == 0))) {
 				noOfChildern++;
 			}
 		}
 		
 		empPayroll.addExemption(getExemptionType(exemptionsTypes, CHILDREN),
 				taxDetails.getExemption(TaxDetails.CHILDREN),
 				noOfChildern);
 		
 		
 		// Basic allowance
 		Double basicAllowanceRate = taxDetails.getExemption(TaxDetails.BASIC_ALLOWANCE_PERCENTAGE);
 		Double basicAllowanceLimit = taxDetails.getExemption(TaxDetails.BASIC_ALLOWANCE_LIMIT);
 		
 		Double basicAllowancePerRate = basicAllowanceRate * empPayroll.getTotalIncome() / 100;
 		
 		if(basicAllowancePerRate > basicAllowanceLimit) {
 			empPayroll.addExemption(getExemptionType(exemptionsTypes, BASIC_ALLOWANCE), basicAllowanceLimit);
 		} else {
 			empPayroll.addExemption(getExemptionType(exemptionsTypes, BASIC_ALLOWANCE), basicAllowancePerRate);
 		}
 			
 		
 		
 	}
 
 	private Exemptionstype getExemptionType(
 			List<Exemptionstype> eTypes, String typeStr) {
 		for(Exemptionstype eType: eTypes) {
 			if(eType.getName().equalsIgnoreCase(typeStr))
 				return eType;
 		}
 		
 		return null;
 	}
 }
