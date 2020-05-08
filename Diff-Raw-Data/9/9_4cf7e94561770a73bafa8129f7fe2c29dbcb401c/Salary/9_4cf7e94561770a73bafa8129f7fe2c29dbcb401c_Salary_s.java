 package com.eric4tw.pair3;
 
 public class Salary {
 
 	private final char gender;
 	private final int totalSalary;
 	private final int age;
 
 	public Salary(char gender, int age, int totalSalary) {
 		this.gender = gender;
 		this.age = age;
 		this.totalSalary = totalSalary;
 	}
 
 	public double calculatetax() {
 		if (age < 60) { // for very general citizens
 			switch (gender) {
 			case 'M': // if male
 				if (totalSalary <= 180000)
 					return 0.0;
 				if (totalSalary <= 500000)
 					return (totalSalary - 180000) * 0.10;
 				if (totalSalary <= 800000)
 					return 32000 + (totalSalary - 500000) * 0.20;
 				return 92000 + (totalSalary - 800000) * 0.30;
 			case 'F': // if female
 				if (totalSalary <= 190000)
 					return 0.0;
 				if (totalSalary <= 500000)
 					return (totalSalary - 190000) * 0.10;
 				if (totalSalary <= 800000)
 					return 31000 + (totalSalary - 500000) * 0.20;
 				return 91000 + (totalSalary - 800000) * 0.30;
 			default:
 				break;
 			}
 		} else if (age < 80) { // for senior citizens
 			if (totalSalary <= 250000)
 				return 0.0;
 			if (totalSalary <= 500000)
 				return (totalSalary - 250000) * 0.10;
 			if (totalSalary <= 800000)
 				return 25000 + (totalSalary - 500000) * 0.20;
			return 75000 + (totalSalary - 800000) * 0.30;
 		}
 
 		// for very senior citizens
 		if (totalSalary <= 500000)
 			return 0.0;
 		else if (totalSalary <= 800000)
 			return (totalSalary - 500000) * 0.20;
 		return 60000 + (totalSalary - 800000) * 0.30;
 	}
 }
