 package nruth.fingar.domain.specs;
 
 import static org.junit.Assert.*;
 
 import nruth.fingar.domain.guitar.FingeredNote;
 import nruth.fingar.domain.guitar.FingeredNote.FingerAssignmentException;
 import nruth.fingar.domain.guitar.FingeredNote.FretAssignmentException;
 import nruth.fingar.domain.guitar.Guitar.GuitarString;
 import nruth.fingar.domain.music.NamedNote;
 import nruth.fingar.domain.music.Note;
 import nruth.fingar.domain.music.TimedNote;
 import nruth.fingar.domain.specs.NoteSpec.NoteFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class FingeredNoteSpec {
 	private FingeredNote initialised_fingerednote_fixture = null;
 	private int finger;
 	private int fret;
 	private GuitarString string;
 	private Note note;
 	private float notestart;
 	private float notedur;
 	private TimedNote tnote = null;
 
 	@Test
 	public void testFingeredNoteTimedNote() {
 		//creates a random fingering solution for the given note
 		FingeredNote note = new FingeredNote(tnote);
 		assertFalse(note.finger()==-1);
 		assertFalse(note.fret()==-1);
 		assertFalse(note.string()==null);
 	}
 	
 	@Test
 	public void can_create_with_known_fingering_data() { //test filled-in fingered note constructor
 		finger = 3;
 		fret = 5;
 		string = GuitarString.D;
 		note = NoteFactory.getRandomNote();
 		notestart=3.0f;
 		notedur=2.0f;
 		tnote = new TimedNote(note, notestart, notedur);
 		
 		FingeredNote testfn = new FingeredNote(finger, fret, string, tnote);
 		
 		//check values are correct
 		assertEquals(finger, testfn.finger());
 		assertEquals(fret, testfn.fret());
 		assertEquals(string, testfn.string());
 		assertEquals(note, testfn.note());
 		assertEquals(notestart, testfn.start_beat(),0.01f);
 		assertEquals(notedur, testfn.duration(),0.01f);
 	}
 
 	@Before
 	public void test_fixture(){ //reliant on testFingeredNoteIntIntGuitarStringTimedNote
 		finger = 3;
 		fret = 5;
 		string = GuitarString.A;
 		note = NoteFactory.getRandomNote();
 		notestart=1.0f;
 		notedur=1.5f;
 		tnote = new TimedNote(note, notestart, notedur);	
 		initialised_fingerednote_fixture = new FingeredNote(finger, fret, string, tnote);
 	}
 
 	@Test
 	public void note_can_be_retrieved() {
 		assertEquals("accessor failure, or Note equality broken", note, initialised_fingerednote_fixture.note());
 	}
 
 	//trivial accessor tests
 	@Test
 	public void testFinger() { assertEquals("accessor failure", finger, initialised_fingerednote_fixture.finger());	}
 	@Test
 	public void testFret() { assertEquals("accessor failure", fret, initialised_fingerednote_fixture.fret());	}
 	@Test
 	public void testString() { assertEquals("accessor failure, or GuitarString equality broken", string, initialised_fingerednote_fixture.string());	}
 	
 	
 	//FINGER SETTING
 	@Test
 	public void finger_can_be_set() {
 		initialised_fingerednote_fixture.setFinger(finger+1);
 		assertEquals(finger+1, initialised_fingerednote_fixture.finger());
 	}
 	
 	@Test(expected=FingerAssignmentException.class)
 	public void error_when_setting_open_string_finger(){
 		//reject fret 0 finger not 0
 		initialised_fingerednote_fixture.setFret(0);
 		initialised_fingerednote_fixture.setFinger(1);
 	}
 	
 	@Test(expected=FingerAssignmentException.class)
 	public void error_when_setting_no_finger_to_position(){
 		//reject nonzero fret, no finger
 		initialised_fingerednote_fixture.setFret(1);
 		initialised_fingerednote_fixture.setFinger(0);
 	}
 	
 	@Test(expected=FingerAssignmentException.class)
 	public void error_when_finger_below_range(){
 		initialised_fingerednote_fixture.setFinger(-1);
 	}
 	
 	@Test(expected=FingerAssignmentException.class)
 	public void error_when_finger_above_range(){
 		initialised_fingerednote_fixture.setFinger(5);
 	}
 	
 	//FRET SETTING
 	@Test
 	public void fret_can_be_set() {
 		initialised_fingerednote_fixture.setFret(fret+1);
 		assertEquals(fret+1, initialised_fingerednote_fixture.fret());
 		
 		//fret 0 must set finger to 0
 		initialised_fingerednote_fixture.setFret(0);
 		assertEquals(0, initialised_fingerednote_fixture.fret());
 		assertEquals(0, initialised_fingerednote_fixture.finger());		
 	}
 
 	@Test(expected=FretAssignmentException.class)
 	public void reject_negative_frets(){//doesn't accept negative frets
 		initialised_fingerednote_fixture.setFret(-2);
 	}
 		
 	@Test
 	public void string_can_be_set() {
 		initialised_fingerednote_fixture.setString(GuitarString.LOW_E);
 		assertEquals(GuitarString.LOW_E, initialised_fingerednote_fixture.string());
 		initialised_fingerednote_fixture.setString(GuitarString.A);
 		assertEquals(GuitarString.A, initialised_fingerednote_fixture.string());
 	}
 
 	@Test
 	public void start_beat_accessible() {
 		assertEquals("accessor failure, or Note equality broken", notestart, initialised_fingerednote_fixture.start_beat(), 0.01f);
 		assertEquals(4f,new FingeredNote(new TimedNote(NoteFactory.getRandomNote(), 4f, 2f)).start_beat(),0.01f);
 	}
 
 	@Test
 	public void beat_duration_accessible() {
 		assertEquals("accessor failure, or Note equality broken", notedur, initialised_fingerednote_fixture.duration(), 0.01f);
 		assertEquals(2f,new FingeredNote(new TimedNote(NoteFactory.getRandomNote(), 4f, 2f)).duration(),0.01f);
 	}
 
 	@Test
 	public void fingering_values_can_be_randomised() {
 		FingeredNote initial = initialised_fingerednote_fixture;
 		FingeredNote random = initial.clone();
		
		boolean finger_changed, fret_changed, string_changed;
		finger_changed = fret_changed = string_changed = false;
		
		for(int n=0; n<50;n++){ //do it a few times to catch any quirky runtime exceptions & chance of 'false positive'
			random.randomise_fingering();
			if(random.finger() != initial.finger()) finger_changed = true;
			if(random.fret() != initial.fret()) fret_changed = true;
			if(!random.string().equals(initial.string())) string_changed = true;
			assertEquals("timing should not change",random.duration(), initial.duration(),0.01f);
			assertEquals("timing should not change",random.start_beat(), initial.start_beat(),0.01f);
		}
		
		if(!finger_changed){fail("finger did not change");}
		if(!fret_changed){fail("fret did not change");}
		if(!string_changed){fail("string did not change");}
 	}
 
 	@Test
 	public void clone_supported_correctly(){
 		FingeredNote clone = initialised_fingerednote_fixture.clone();
 		assertEquals("clone did not maintain equality", initialised_fingerednote_fixture, clone);
 		assertNotSame("clone returned same object", initialised_fingerednote_fixture, clone);
 		
 		clone.setFinger(initialised_fingerednote_fixture.finger()+1);
 		assertFalse("clone did not create a distinct object or equality broken", clone.equals(initialised_fingerednote_fixture));
 	}
 	
 	@Test
 	public void testEquals() {
 		FingeredNote a = initialised_fingerednote_fixture;
 		assertEquals("same object",a,a);
 		assertFalse("null object", a.equals(null));
 		
 		FingeredNote b = a.clone();
 		assertNotSame("Tests rely on working cloning mechanism",a, b);
 		assertEquals("Tests rely on working cloning mechanism, this could also be equality broken",a, b);
 		
 		
 		b = a.clone(); b.setFret(b.fret()+1);
 		assertFalse("different fret",a.equals(b));
 		assertFalse("different fret",b.equals(a));
 		
 		b = a.clone(); b.setFinger(b.finger()+1);
 		assertFalse("different finger",a.equals(b));
 		assertFalse("different finger",b.equals(a));
 		
 		b = a.clone(); b.string();
 		b.setString(GuitarString.values()[b.string().ordinal()+1]);
 		assertFalse("different finger",a.equals(b));
 		assertFalse("different finger",b.equals(a));
 		
 		//TimedNote tests
 		TimedNote note1, note2;
 		note1 = new TimedNote(new Note(NamedNote.B, 1), 1f, 1f);
 		note2 = new TimedNote(new Note(NamedNote.C, 2), 2f, 2f);
 		
 		a = new FingeredNote(note1);
 		b = new FingeredNote(note2);
 		
 		a.setFinger(1); b.setFinger(1);
 		a.setString(GuitarString.A); b.setString(GuitarString.A);
 		a.setFret(10); b.setFret(10);
 		
 		assertFalse("different note", a.equals(b));
 		assertFalse("different note", b.equals(a));
 	}
 	
 	@Test
 	public void testHashCode() {
 		FingeredNote a = initialised_fingerednote_fixture; 
 		assertEquals("same object different hashcode",a.hashCode(), a.hashCode());
 		
 		FingeredNote b = a.clone();
 		assertNotSame("Tests rely on working cloning mechanism",a, b);
 		assertEquals("Tests rely on working cloning mechanism, this could also be equality broken",a, b);
 		
 		
 		b = a.clone(); b.setFret(b.fret()+1);
 		assertFalse("different fret",a.hashCode() == b.hashCode());
 		assertFalse("different fret",b.hashCode() == a.hashCode());
 		
 		b = a.clone(); b.setFinger(b.finger()+1);
 		assertFalse("different finger",a.hashCode() == b.hashCode());
 		assertFalse("different finger",b.hashCode() == a.hashCode());
 		
 		b = a.clone(); b.string();
 		b.setString(GuitarString.values()[b.string().ordinal()+1]);
 		assertFalse("different finger",a.hashCode() == b.hashCode());
 		assertFalse("different finger",b.hashCode() == a.hashCode());
 		
 		//TimedNote tests
 		TimedNote note1, note2;
 		note1 = new TimedNote(new Note(NamedNote.B, 1), 1f, 1f);
 		note2 = new TimedNote(new Note(NamedNote.C, 2), 2f, 2f);
 		a = new FingeredNote(note1);
 		b = new FingeredNote(note2);
 		a.setFinger(1); b.setFinger(1);
 		a.setString(GuitarString.A); b.setString(GuitarString.A);
 		a.setFret(10); b.setFret(10);
 		
 		assertFalse("different note", a.hashCode() == b.hashCode());
 		assertFalse("different note", b.hashCode() == a.hashCode());
 	}
 
 	//TODO: doesn't reject being given invalid fingering positions for the note, e.g. E first fret for G (should be F)
 	@Test
 	public void rejects_invalid_positions(){
 		
 	}
 	
 	//it was decided to remove uninitialised fingered notes, since no use of them was found within the system
 //	@Test
 //	public void can_create_for_a_note_with_blank_default_fingering_values() {	uninitialised_fingered_note();	}
 
 //	/**
 //	 * causes errors if read before values assigned
 //	 */
 //	@Test(expected=NullPointerException.class)
 //	public void finger_not_initialised_error(){	uninitialised_fingered_note().finger(); }
 //	@Test(expected=NullPointerException.class)
 //	public void string_not_initialised_error(){	uninitialised_fingered_note().string(); }
 //	@Test(expected=NullPointerException.class)
 //	public void fret_not_initialised_error(){	uninitialised_fingered_note().fret(); }
 
 //	public static FingeredNote uninitialised_fingered_note(){
 //		return new FingeredNote(TimedNoteSpec.create_random_monophonic_arranged_notes(1)[0]);
 //	}
 	
 	
 	//TODO try equality on unint object to check for exception throwing
 //	assertFalse(initialised_fingered_note().equals(uninitialised_fingered_note()));
 }
 
