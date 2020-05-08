 package com.andresjruiz.gradetracker;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Classes implements Serializable{
 	
 	private String Name;
 	private String Professor;
 	private Map<String, Integer> GradeWeights;
 	private Map<String, Grade> Grades;
 	
 	private int Grade;//Tmp variable
 	
 	
 	/**
 	 * A protected class for testing/development purposes
 	 * @param Name - The name of the class
 	 */
 	protected Classes(String Name, int Grade){
 		this.Name = Name;
 		this.Grade = Grade;
 	}
 	
 	protected Classes(String Name, int Grade, Map<String, Integer> GradeWeights){
 		this.Name = Name;
 		this.Grade = Grade;
 		this.setNewWeights(GradeWeights);
 	}
 	
 	public Classes(String Name, Map<String, Integer> GradeWeights){
 		this.Name = Name;
 		this.setNewWeights(GradeWeights);
 	}
 	
 	public Classes(String Name, String Professor, Map<String, Integer> GradeWeights){
 		this.Name = Name;
 		this.Professor = Professor;
 		this.setNewWeights(GradeWeights);
 		Grades = new HashMap<String, Grade>();
 	}
 	
 	
 	public String getProfessor(){
 		return this.Professor;
 	}
 	
 	protected void setProfessor(String newName){
 		this.Professor = newName;
 	}
 	
 	protected void setNewWeights(Map<String, Integer> newWeights){
 		//Check that all the weights add up to 100
 		Collection<Integer> values = (Collection<Integer>) newWeights.values();
 		
 		int sum = 0;
 		for(int i : values){
 			sum += i;
 		}
 		
 		if(sum != 100){
 			throw new IllegalArgumentException("The sum of the items does not equal 100");
 		}
 		
 		//Everything checked out, assign the new weights
 		GradeWeights = newWeights;
 	}
 	
 	protected void addNewGrade(String Category, String name, double grade){
 		//Check to make sure that the category exists before adding things in
 		if(!this.GradeWeights.keySet().contains(Category)){
 			throw new IllegalArgumentException("The Category " + Category + " does not exist for this class!");
 		}
 		Grade newGrade = new Grade(name, grade);
 		this.Grades.put(Category, newGrade);
 	}
 	
 	protected void addNewGrade(String Category, String name, double grade, double maxGrade){
 		//Check to make sure that the category exists before adding things in
 		if(!this.GradeWeights.keySet().contains(Category)){
 			throw new IllegalArgumentException("The Category " + Category + " does not exist for this class!");
 		}
 		Grade newGrade = new Grade(name, grade, maxGrade);
 		this.Grades.put(Category, newGrade);
 	}
 	
 	public String getName(){
 		return this.Name;
 	}
 	
 	public int getFinalGrade(){
 		return this.Grade;//Tmp solution
 	}
 	
 	public String[] getCategories(){
 		String[] categories = new String[this.GradeWeights.size()];
 		Set<String> categories_set = this.GradeWeights.keySet();
 		System.out.println(categories.length);
 		categories_set.toArray(categories);
 		
 		return categories;
 	}
 
 }
