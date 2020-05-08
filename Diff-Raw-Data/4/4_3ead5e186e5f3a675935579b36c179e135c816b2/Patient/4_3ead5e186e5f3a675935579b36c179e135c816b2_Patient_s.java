 package models;
 
 import java.util.ArrayList;
 
 import javax.persistence.*;
 
 import play.db.jpa.*;
 
 /**
  * Class that represents a Patient in the system.<br>
  * Patients have addresses, phone numbers, sex,<br>
  * and their exam history.
  *
  */
 @Entity
 public class Patient extends User {
 	private String address;
 	private String phoneNumber;
 	private char sex;
 	
 	@OneToMany(mappedBy="patient", cascade=CascadeType.ALL)
	private ArrayList<Exam> exams;
 	
 	//Primary doctor?
 	
 	/**
 	 * Creates a new patient with the given information
 	 * @param username the username (Email address)
 	 * @param password the password
 	 * @param address the address
 	 * @param phoneNumber the phone number
 	 * @param sex the sex (M/F/I)
 	 */
 	public Patient(String username, String password, String address,
 			String phoneNumber, char sex) {
 		//Superclass constructor
 		super(username, password);
 		this.address = address;
 		this.phoneNumber = phoneNumber;
 		this.sex = sex;
 		exams = new ArrayList<Exam>();
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	public char getSex() {
 		return sex;
 	}
 	
 	/**
 	 * Adds an exam for the patient.<br>
 	 * Patient is automatically saved.
 	 * @param exam the exam to add
 	 */
 	public void addExam(Exam exam){
 		exams.add(exam);
 		this.save();
 	}
 	
 	
 
 }
