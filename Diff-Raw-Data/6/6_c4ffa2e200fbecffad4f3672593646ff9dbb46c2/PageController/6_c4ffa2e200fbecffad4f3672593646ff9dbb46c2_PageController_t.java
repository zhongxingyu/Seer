 package controllers;
 
 import play.*;
 import play.data.validation.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 import repo.Repository;
 
 @With(Secure.class)
 public class PageController extends Controller {
 	
 	/**
 	 * Displays the index (Main) page
 	 */
     public static void welcome() {
         render();
     }
     
     /**
      * Displays the search page
      */
     public static void search(){
     	if(!Security.check("physician")){
     		//Direct the user to their own exams
     		results(null, Security.getUserId(), null, null);
     	}
     	
     	List<Physician> physicians = Physician.findAll();
     	List<Patient> patients = Patient.findAll();
     	
     	render(physicians, patients);
     }
     
     
     public static void results(Long physicianId, Long patientId, Date start, Date end){
     	if(!Security.check("physician")){
     		physicianId = null;
     		start = null;
     		end = null;
     		patientId = Security.getUserId();
     	}
     	//Need to check for each search condition: Physician, patient, or date
     	List<Exam> exams = null;
     	//Physician
     	if(physicianId != null){
     		exams = Repository.searchByPhysician(physicianId);
     		render( exams );
     		return;
     	}
     	
     	//Patient
     	if(patientId != null){
     		exams = Repository.searchByPatient(patientId);
     		render( exams );
     		return;
     	}
     	
     	exams = Repository.searchByDate(start, end);
     	//Date
     	render( exams );
     }
     
     
     public static void exam(Long examId){
     	Exam exam = Exam.findById(examId);
     	render(exam);
     }
     
     @Check("physician")
     public static void patient(Long id){
     	Patient patient = Patient.findById(id);
     	render(patient);
     }
     
     @Check("physician")
     public static void physician(Long id){
     	Physician physician = Physician.findById(id);
     	render(physician);
     }
     
     @Check("physician")
     public static void createExamForm(){
    	List<Patient> patients = Patient.findAll();
    	
    	render(patients);
     }
     
    @Check("physician")
     public static void createExam(){
     	
     	createExamForm();
     }
     
 
 }
