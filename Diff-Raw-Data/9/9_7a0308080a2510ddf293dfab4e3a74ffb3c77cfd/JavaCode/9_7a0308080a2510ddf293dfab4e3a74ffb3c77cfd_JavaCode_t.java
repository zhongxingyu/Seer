package fr.emn.examination.view;

 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class JavaCode {
 
 	private HashMap<String, CodeQuestion> questions;
 
 	private ArrayList<String> 
 	roughCode,
 	questionIds;
 	
 	public JavaCode() {
		questions = new HashMap<String, CodeQuestion>();
		roughCode = new ArrayList<String>();
		questionIds = new ArrayList<String>();
 	}
 
 	public HashMap<String, CodeQuestion> getQuestions() {
 		return questions;
 	}
 
 	public ArrayList<String> getRoughCode() {
 		return roughCode;
 	}
 
 	public ArrayList<String> getQuestionIds() {
 		return questionIds;
 	}
 
 }
