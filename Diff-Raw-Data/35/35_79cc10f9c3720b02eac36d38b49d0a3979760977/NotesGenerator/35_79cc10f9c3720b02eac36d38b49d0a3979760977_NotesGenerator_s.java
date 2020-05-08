 package com.rhythm.generator;
 
 import java.util.ArrayList;
 import java.util.Random;
 import com.rhythm.app.Difficulty;
 import com.rhythm.music.*;
 
 public class NotesGenerator
 {
 	private Difficulty difficulty;
 	final static int rests = 5; //a rest should occur on average one every rests notes.
 	
 	public NotesGenerator(Difficulty trickiness)
 	{
 			difficulty = trickiness;
 	}
 	
 	public ArrayList<Note> generateBar()
 	{
 		ArrayList<Note> notes = new ArrayList<Note>();
 		Note next;
 		double length = 1;
 		
 		while (length > 0)
 		{
 			next = chooseNote(length);
 			notes.add(next);
 			length = length - next.getLength();
 		}
 		
 		return notes;
 	}
 	
 	public Note chooseNote(double length)
 	{
 		Random rnd = new Random();
 		boolean rest;
 		int result;
 		
 		int range = 0;
 		int offset;
 		//this restricts the function to only selecting from a maximum of range types of note length
 		//shorter notes, and hence more complicated patterns are only available with a higher range of notes.
 		switch (difficulty)
 		{
 		case EASY :
 			range = 3;
 		case MEDIUM :
 			range = 4;
 		case HARD :
 			range = 5;
 		default :
 			range = 0;
 				
 		}
 		//the offset prevents the randomiser from selecting a note that is too large for the bar
 		//this is only because our current renderer cannot display notes that overflow into the next bar
 		if (length < 0.125)
 		{
 			offset = 4;
 		}
 		else if (length < 0.25)
 		{
 			offset = 3;
 		}
 		else  if (length < 0.5)
 		{
 			offset = 2;
 		}
 		else if (length < 1)
 		{
 			offset = 1;
 		}
 		else
 		{
 			offset = 0;
 		}
 		
 		result = rnd.nextInt(range-offset) + offset;
 		
 		
 		//decide if it should be a rest or not, rest rate determined by constant 'rests'
 		if (rnd.nextInt(rests) == 0)
 		{
 			rest = true;
 		}
 		else
 		{
 			rest = false;
 		}
 		//return a new note of the note type chosen by the random generator
 		switch (result)
 		{
 		case 0 :
 			return new Semibreve(0,rest);
 		case 1 :
 			return new Minim(0,rest);
 		case 2 :
 			return new Crotchet(0,rest);
 		case 3 :
 			return new Quaver(0,rest);
 		case 4 :
 			return new Semiquaver(0,rest);
 		default :
			return  null;
 		}
 		
 		
 	}
 
 }
