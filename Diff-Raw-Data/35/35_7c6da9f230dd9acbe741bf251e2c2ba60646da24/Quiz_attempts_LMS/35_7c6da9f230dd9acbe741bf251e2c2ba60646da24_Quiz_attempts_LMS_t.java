 package de.lemo.dms.connectors.moodle_2_3.moodleDBclass;
 
 public class Quiz_attempts_LMS {
 	
 	private long id;
 	private long uniqueid;
	private Double sumgrades;
 	private long attempt;
 	private String userid;
 	private long quiz;
 	private long timestart;
 	private long timefinish;
 	private long timemodified;
 	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public long getUniqueid() {
 		return uniqueid;
 	}
 	public void setUniqueid(long uniqueid) {
 		this.uniqueid = uniqueid;
 	}
 	public long getAttempt() {
 		return attempt;
 	}
 	public void setAttempt(long attempt) {
 		this.attempt = attempt;
 	}
 	public String getUserid() {
 		return userid;
 	}
 	public void setUserid(String userid) {
 		this.userid = userid;
 	}
 	public long getQuiz() {
 		return quiz;
 	}
 	public void setQuiz(long quiz) {
 		this.quiz = quiz;
 	}
 	public long getTimestart() {
 		return timestart;
 	}
 	public void setTimestart(long timestart) {
 		this.timestart = timestart;
 	}
 	public long getTimefinish() {
 		return timefinish;
 	}
 	public void setTimefinish(long timefinish) {
 		this.timefinish = timefinish;
 	}
 	public long getTimemodified() {
 		return timemodified;
 	}
 	public void setTimemodified(long timemodified) {
 		this.timemodified = timemodified;
 	}
	public void setSumgrades(Double sumgrades) {
 		this.sumgrades = sumgrades;
 	}
	public Double getSumgrades() {
 		return sumgrades;
 	}
 }
