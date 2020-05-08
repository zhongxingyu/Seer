 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 11, 2012
  * Author(s): andres
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course.lecture;
 
 import java.util.Date;
 
 
 /**
  * This class holds the information about a lecture. It contains a start date, a length of the lecture and a list of
  * several timeblocks
  * 
  * @author andres
  * 
  */
 public class Lecture {
 	
 	private Date			start;
 	private TimeBlocks	timeBlocks;
 	
 	
	@Deprecated
 	public Lecture(Date start) {
 		this.start = start;
 		this.timeBlocks = new TimeBlocks();
 		timeBlocks.add(new TimeBlock(200, BlockType.theory));
 	}
 	
 	
 	public Lecture(Date start, TimeBlocks timeBlocks) {
 		this.start = start;
 		setTimeBlocks(timeBlocks);
 	}
 	
 	
 	/**
 	 * @return the start
 	 */
 	public Date getStart() {
 		return start;
 	}
 	
 	
 	/**
 	 * @param start the start to set
 	 */
 	public void setStart(Date start) {
 		this.start = start;
 	}
 	
 	
 	/**
 	 * @return the length
 	 */
 	public int getLength() {
 		return timeBlocks.getTotalLength();
 	}
 	
 	
 	/**
 	 * @return the timeBlocks
 	 */
 	public TimeBlocks getTimeBlocks() {
 		return timeBlocks;
 	}
 	
 	
 	/**
 	 * @param timeBlocks the timeBlocks to set
 	 */
 	public void setTimeBlocks(TimeBlocks timeBlocks) {
 		this.timeBlocks = timeBlocks;
 		// for (TimeBlock timeBlock : timeBlocks) {
 		// addTimeBlock(timeBlock);
 		// }
 	}
 	
 	
 }
