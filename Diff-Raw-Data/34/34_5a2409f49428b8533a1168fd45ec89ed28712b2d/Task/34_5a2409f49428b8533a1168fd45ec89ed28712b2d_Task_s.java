 package com.lnu.todomorrow.utils;
 
 import java.util.Calendar;
 
 public class Task {
 
 	private long id;
 	private String name;
 	private Goal goal;
 	private Calendar deadline;
 	private boolean finished;
	private Calendar finishedAt;
 	private int value;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Goal getGoal() {
 		return goal;
 	}
 
 	public void setGoal(Goal goal) {
 		this.goal = goal;
 	}
 
 	public Calendar getDeadline() {
 		return deadline;
 	}
 
 	public void setDeadline(Calendar deadline) {
 		this.deadline = deadline;
 	}
 
 	public boolean isFinished() {
 		return finished;
 	}
 
 	public void setFinished(boolean finished) {
 		this.finished = finished;
 	}
 
 	public int getValue() {
 		return value;
 	}
 
 	public void setValue(int value) {
 		this.value = value;
 	}
 
 	public Calendar getFinishedAt() {
 		return finishedAt;
 	}
 
 	public void setFinishedAt(Calendar finishedAt) {
 		this.finishedAt = finishedAt;
 	}
 
 	@Override
 	public String toString() {
 		return "Task [id=" + id + ", name=" + name + ", goal=" + goal + ", deadline="
 				+ TimeUtil.getFormattedDate(deadline) + ", finished=" + finished + ", finishedAt="
 				+ TimeUtil.getFormattedDate(finishedAt) + ", value=" + value + "]";
 	}
 
 }
