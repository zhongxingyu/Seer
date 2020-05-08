 package com.rhythm.generator;
 
 import com.rhythm.music.Note;
 
 public class NoteScore {
 	
 	private boolean scorable;
	private int score;
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
