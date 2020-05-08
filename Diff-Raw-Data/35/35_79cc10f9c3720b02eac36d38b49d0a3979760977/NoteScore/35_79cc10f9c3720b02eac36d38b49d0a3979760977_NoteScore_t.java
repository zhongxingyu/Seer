 package com.rhythm.generator;
 
 import com.rhythm.music.Note;
 
 public class NoteScore {
 	
	private  final static int baseScore = 50;
	
 	private boolean scorable;
	
	private double startTime;
	private double endTime;
	
	private boolean startRecieved;
	private boolean endRecieved;
	
	private int score = 0;
 	private Note note;
 	
 	public NoteScore() {
 		scorable = true;
 		score = 0;
 		
 	}
 	
 	public void notScorable()
 	{
 		scorable = false;
 	}
 	
 	public void setScore(int points){
 		if (scorable)
 		{
 			score = points;
 		}
 	}
 	
 	public int getScore()
 	{
 		return score;
 	}
 	
 	public Note getNote()
 	{
 		return note;
 	}
 }
