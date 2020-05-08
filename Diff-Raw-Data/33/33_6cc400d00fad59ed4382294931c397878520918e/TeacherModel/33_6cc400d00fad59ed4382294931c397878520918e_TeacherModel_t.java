 package quizsystem.gui.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import quizsystem.gui.abs.AbstractModel;
 import quizsystem.gui.controller.TeacherController;
 import quizsystem.types.AbstractQuestion;
 import quizsystem.types.Test;
 
 public class TeacherModel extends AbstractModel{
 	public Test test;
	private Integer displayedQuestion;
 	public TeacherModel() {
 		test = new Test();
 	}
 	
 	public void setDisplayedQuestion(Integer id) {
 		Integer oldValue = displayedQuestion;
 		this.displayedQuestion = id;
 		firePropertyChange(TeacherController.DISPLAYED_QUESTION, oldValue, id);
 	}
 	
 	public void setName(String name) {
 		String oldValue = test.getName();
 		test.setName(name);
 		firePropertyChange(TeacherController.NAME, oldValue, name);
 	}
 	
 	public void setAuthor(String author) {
 		String oldValue = test.getAuthor();
 		test.setAuthor(author);
 		firePropertyChange(TeacherController.AUTHOR, oldValue, author);
 	}
 	
 	public void setCreateTime(Date date) {
 		Date oldValue = test.getCreateTime();
 		test.setCreateTime(date);
 		firePropertyChange(TeacherController.CREATE_TIME, oldValue, date);
 	}
 	
 	public void setStartTime(Date date) {
 		Date oldValue = test.getStartTime();
 		test.setStartTime(date);
 		firePropertyChange(TeacherController.START_TIME, oldValue, date);
 	}
 	
 	public void setTestTime(Integer time) {
 		Integer oldValue = test.getTestTime();
 		test.setTestTime(time);
 		firePropertyChange(TeacherController.TEST_TIME, oldValue, time);
 	}
 	
 	public void addQuestion(AbstractQuestion question) {
 		test.addQuestion(question);
 		firePropertyAdded(TeacherController.QUESTION, question);
 	}
 	
 	public void setQuestion(Integer i, AbstractQuestion q) {
 		AbstractQuestion oldValue = test.getQuestions().get(i);
 		test.setQuestion(i, q);
 		firePropertyChange(TeacherController.QUESTION, oldValue, q);
 		
 	}
 	
 	public void removeQuestion(Integer i) {
 		AbstractQuestion oldValue = test.getQuestions().get(i);
 		test.removeQuestion(i);
 		firePropertyRemoved(TeacherController.QUESTION, oldValue);
 	}
 	
 	public void removeQuestion(AbstractQuestion question) {
 		test.removeQuestion(question);
 		firePropertyRemoved(TeacherController.QUESTION, question);
 	}
 	
 	public String getName() {
 		return test.getName();
 	}
 	
 	public String getAuthor() {
 		return test.getAuthor();
 	}
 	
 	public Date getCreateTime() {
 		return test.getCreateTime();
 	}
 	
 	public Date getStartTime() {
 		return test.getStartTime();
 	}
 	
 	public Integer getTestTime() {
 		return test.getTestTime();
 	}
 	
 	public ArrayList<AbstractQuestion> getQuestions() {
 		return test.getQuestions();
 	}
 	
 	public AbstractQuestion getQuestionAt(Integer i) {
 		return test.getQuestions().get(i);
 	}
 	
 	
 }
