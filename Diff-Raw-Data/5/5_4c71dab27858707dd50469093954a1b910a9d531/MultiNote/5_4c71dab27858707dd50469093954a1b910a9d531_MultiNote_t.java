 package music;
 
 import java.util.ArrayList;
 
 /* MultiNote is a broad class definition used to describe either a rest, a single note, or an
  * arbitrary number of notes being played in unison.
  */
 public class MultiNote extends Duration {
 	ArrayList<Pitch> _pitches;					// all pitches in the MultiNote, from highest to lowest; an empty list of Pitches represents a rest.
 	
	public MultiNote(Rational duration) {
 		super(duration);
		_pitches = new ArrayList<Pitch>();
 	}
 	
 	public ArrayList<Pitch> getPitches() {
 		return _pitches;
 	}
 }
