 package model;
 
 import java.util.ArrayList;
 
 import model.Teacher;
 import utils.DateQuiz;
 /**
  * 
  * @author Steve
  * @version 13/10/2013
  *
  */
 
 
 public class Quiz implements Comparable<Quiz>{
 	
 	private int quizId;
 	private String subject; // subject = ""
	private int grades; // leerJaren = 0
 	private QuizStatus status;
 	private Teacher teacher;
 	private DateQuiz date;
 	private boolean isUniqueParticipation;
 	private boolean isTest;
 	private ArrayList<QuizExercise> quizExercises; // ArrayList<QuizExercise> quizExercises = new ArrayList<QuizExercise>()
 	
 	
 	//default constructor
 	
 	public Quiz()throws IllegalArgumentException
 	{
 		this.setQuizId(1);
 		this.setSubject("subject");
 		this.setLeerJaren(1);
 		this.setStatus(QuizStatus.READY);
 		this.setTeacher(Teacher.JACOBS);
 		this.setDate(new DateQuiz());
 		this.setUniqueParticipation(false);
 		this.setTest(false);
 		this.setQuizExercises(new ArrayList<QuizExercise>());
 	}
 	
 	public Quiz(String subject)throws IllegalArgumentException
 	{
 		this.setSubject(subject);
 		
 		// Default
 		this.setQuizId(1);
 		this.setLeerJaren(1);
 		this.setStatus(QuizStatus.READY);
 		this.setTeacher(Teacher.JACOBS);
 		this.setDate(new DateQuiz());
 		this.setUniqueParticipation(false);
 		this.setTest(false);
 		this.setQuizExercises(new ArrayList<QuizExercise>());
 	}
 	
 	//constructor with 4 parameters
 	
 	public Quiz(String subject, int leerJaren, boolean uniqueParticipation, boolean isTest)throws IllegalArgumentException
 	{
 		this.setSubject(subject);
 		this.setLeerJaren(leerJaren);
 		this.setUniqueParticipation(uniqueParticipation);
 		this.setTest(isTest);
 		
 		// Default
 		this.setQuizId(1);
 		this.setStatus(QuizStatus.READY);
 		this.setTeacher(Teacher.JACOBS);
 		this.setDate(new DateQuiz());
 		this.setQuizExercises(new ArrayList<QuizExercise>());
 	}
 	
 	//@overrides
 	
 	@Override
 	public int compareTo(Quiz o) {
 		return this.getSubject().compareTo(o.getSubject());
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (isTest ? 1231 : 1237);
 		result = prime * result + (isUniqueParticipation ? 1231 : 1237);
		result = prime * result + grades;
 		result = prime * result
 				+ ((subject == null) ? 0 : subject.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Quiz other = (Quiz) obj;
 		if (isTest != other.isTest)
 			return false;
 		if (isUniqueParticipation != other.isUniqueParticipation)
 			return false;
		if (grades != other.grades)
 			return false;
 		if (subject == null) {
 			if (other.subject != null)
 				return false;
 		} else if (!subject.equals(other.subject))
 			return false;
 		return true;
 	}
 
 	@Override
     public String toString() {
             String quizText = "";
             if (isTest) {
             	quizText = "\nThis is a test.";
             } 
             else {
             	quizText = "\nThis is not a test.";
             }
             
             if (isUniqueParticipation) {
             	quizText += "\nUnique participation.";
             } 
             else {
                 quizText += "\nNo unique participation.";
             }
             
             quizText += "\nStatus: ";
             
             String quizSummary = "";
             quizSummary += "\nQuiz : " + subject
            				+ "\nYears: " + grades
                             + "\nDate: " + date.getDateInEuropeanFormat()
                             + "\nCreated by: " + teacher 
                             + "\n" + quizText 
                             + "\nStatus: " + status;
             
             return quizSummary;
     }
 	
 	//get & set
 	
 	public int getQuizId() {
 		return quizId;
 	}
 
 	public void setQuizId(int quizId) {
 		this.quizId = quizId;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public int getLeerJaren() {
		return grades;
 	}
 
 	public void setLeerJaren(int leerJaren) {
		this.grades = leerJaren;
 	}
 
 	public QuizStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(QuizStatus status) {
 		this.status = status;
 	}
 
 	public Teacher getTeacher() {
 		return teacher;
 	}
 
 	public void setTeacher(Teacher teacher) {
 		this.teacher = teacher;
 	}
 
 	public DateQuiz getDate() {
 		return date;
 	}
 
 	public void setDate(DateQuiz date) {
 		this.date = date;
 	}
 
 	public boolean isUniqueParticipation() {
 		return isUniqueParticipation;
 	}
 
 	public void setUniqueParticipation(boolean isUniqueParticipation) {
 		this.isUniqueParticipation = isUniqueParticipation;
 	}
 
 	public boolean isTest() {
 		return isTest;
 	}
 
 	public void setTest(boolean isTest) {
 		this.isTest = isTest;
 	}
 
 	public ArrayList<QuizExercise> getQuizExercises() {
 		return quizExercises;
 	}
 
 	public void setQuizExercises(ArrayList<QuizExercise> quizExercises) {
 		this.quizExercises = quizExercises;
 	}
 	
 	//methods
 
 	public void addQuizExercise(QuizExercise quizExercise) throws IllegalArgumentException {
 
 		try
 		{
 			for(int i = 0; i < this.quizExercises.size(); i++) 
 			{
 				if (quizExercises.get(i).equals(quizExercise)) 
 				{
 					throw new IllegalArgumentException("Exercise already exists.");
 				}
 			}
 			quizExercises.add(quizExercise);
 		}
 		catch (IllegalArgumentException ex){ throw new IllegalArgumentException(ex.getMessage());
 		}
 	}
 
 
 	public void deleteQuizExercise(QuizExercise quizExercise) throws Exception {
 		
 		try
 		{	
 			for(int i = 0; i < this.quizExercises.size(); i++)
 			{
 				if (quizExercises.get(i).equals(quizExercise)) 
 				{
 					throw new Exception("No such exercise.");
 				}
 			}
 			quizExercises.remove(quizExercise);
 		}
 		catch (Exception ex){ throw new Exception(ex.getMessage());
 		}
 	}
 }
