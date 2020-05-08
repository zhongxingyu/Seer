 package nruth.fingar.domain.guitar;
 
 import java.util.LinkedList;
 import java.util.Random;
 
 import nruth.fingar.domain.*;
 import nruth.fingar.domain.guitar.Guitar.GuitarString;
 import nruth.fingar.domain.music.Note;
 import nruth.fingar.domain.music.TimedNote;
 
 public class FingeredNote implements Cloneable {
 	private int finger=-1, fret=-1;
 	private Guitar.GuitarString string = null;
 	private TimedNote note;
 	
 	@Override
 	public FingeredNote clone() {
 		FingeredNote clone;
 		//class will not be extended (but is not marked final for mock object purposes) so can catch the error
 		try{	clone = (FingeredNote) super.clone();	}catch (CloneNotSupportedException e){	return null;	} 
 		clone.note = note.clone();
 		return clone;
 	}
 
 	@Override
 	public boolean equals(Object obj_to_chk) {
 		if(obj_to_chk == null) return false;
 		if(obj_to_chk == this) return true;
 		FingeredNote note_to_chk = (FingeredNote) obj_to_chk;
 		
 		return (note_to_chk.finger == this.finger) && 
 			( note_to_chk.fret == this.fret ) &&
 			( note_to_chk.string == this.string) &&
 			( note_to_chk.note.equals(this.note));
 	}
 	
 	/**
 	 * create a FingeredNote with known playing position and finger
 	 * @param finger
 	 * @param fret
 	 * @param string
 	 * @param note the note to be played
 	 */
 	public FingeredNote(int finger, int fret, GuitarString string, TimedNote note) {
 		this.note = note;
 		this.finger = finger;
 		this.fret = fret;
 		this.string = string;
 	}
 	
 	/**
 	 * Create an FingeredNote for a particular note 
 	 * Random position values will be determined
 	 * @param note the note to be played
 	 */
 	public FingeredNote(TimedNote note) {	
 		this.note = note;
 		this.randomise_fingering();
 	}
 	
 	//accessors / getters
 	public Note note(){	return note.note();	}
 	public int finger() {
 		if(finger < 0) throw new RuntimeException("uninitialised finger");
 		return finger;  	
 	}
 	
 	public int fret() {
 		if(fret < 0) throw new RuntimeException("uninitialised fret");
 		return fret; 
 	}
 	
 	public Guitar.GuitarString string() {
 		if(string == null) throw new RuntimeException("uninitialised string");
 		return string;	
 	}
 	public float start_beat() {	return note.start_beat(); }
 	public float duration() {	return note.duration(); }
 	
 	//mutators / setters
 	
 	/**
 	 * sets the fret to the given integer
 	 * given a 0 fret the finger will be set to 0 (no finger for open string)
 	 */
 	public void setFret(int fret) {
 		if(fret<0){ throw new FretAssignmentException("Attempting to set negative fret "+fret+"\n"+this.toString()); }
 		this.fret = fret;
 		if(fret==0){ setFinger(0); }
 	}	
 	
 	
 	public void setFinger(int finger) {
 		if(finger<0 || finger>4){ throw new FingerAssignmentException("Attempting to set invalid finger "+finger+"\n"+this.toString()); }
 		if(fret()==0 && finger!=0){ throw new FingerAssignmentException("attempted to assign finger "+finger+" to an open string.\n"+this.toString()); }
 		if(finger==0 && fret()!=0){ throw new FingerAssignmentException("attempted to assign no finger to fretted position\n"+this.toString()); }
 		this.finger = finger;	
 	}
 	
 	
 	public void setString(Guitar.GuitarString string) {	this.string = string;	}
 
 	/**
 	 * sets valid fret, string and finger values randomly
 	 */
 	public void randomise_fingering() {
 		LinkedList<Position> ps = Guitar.get_positions_for_note(note());
 		Position p = ps.get(seed.nextInt(ps.size()));
 		setString(p.string());
 		setFret(p.fret());
//		if(fret()==0){
//			setFinger(0);
//		}
 		setFinger(Guitar.FINGERS[seed.nextInt(Guitar.FINGERS.length)]);
 	}
 	
 	public String toString(){
 		return note.toString() + " at fi"+finger+";fr"+fret+"; str:"+string;
 	}
 	
 	static final Random seed = new Random();
 	
 	public static class FingerAssignmentException extends RuntimeException {
 		public FingerAssignmentException(String msg){
 			super(msg);
 		}
 		
 		public FingerAssignmentException(){
 			super();
 		}
 	}
 	public static class FretAssignmentException extends RuntimeException {
 		public FretAssignmentException(String msg){
 			super(msg);
 		}
 		
 		public FretAssignmentException(){
 			super();
 		}
 	}
 }
