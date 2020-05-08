 package nruth.fingar.ga.specs;
 
 import static org.junit.Assert.*;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import nruth.fingar.domain.guitar.FingeredNote;
 import nruth.fingar.domain.guitar.Guitar;
 import nruth.fingar.domain.guitar.Guitar.GuitarString;
 import nruth.fingar.domain.music.Note;
 import nruth.fingar.domain.music.Score;
 import nruth.fingar.domain.music.TimedNote;
 import nruth.fingar.domain.specs.ScoreSpec;
 import nruth.fingar.domain.specs.NoteSpec.NoteFactory;
 import nruth.fingar.ga.Arrangement;
 
 import org.junit.*;
 
 public class ArrangementSpec {
 	@Test
 	public void given_fingered_notes_can_construct_an_equivalent_model(){
 		Arrangement arrangement = test_arranger(test_score());
 		//put some values into the arrangement first
 		arrangement.randomise();
 		
 		//then pull the notes out of it and try to reconstruct the score via the arrangement constructor AND retain equality of fingering data
 		List<FingeredNote> notes = new LinkedList<FingeredNote>(); 
 		notes.addAll(arrangement.fingered_notes().values());
 		Collections.sort(notes, new Comparator<FingeredNote>() {
 			public int compare(FingeredNote o1, FingeredNote o2) {
 				return Float.compare(o1.start_beat(), o2.start_beat());
 			}
 		});
 		
 		assertEquals(arrangement, new Arrangement(notes));
 	}
 	
 	/**
 	 * given a score
 	 * 	will store string, fret and finger allocations for each note in the score
 	 */
 	@Test
 	public void is_iterable_correctly(){
 		Score score = test_score();
 		Arrangement arrangement = test_arranger(score);
 		
 		int n=1;
 		for(FingeredNote note : arrangement){
 			assertEquals(score.get_nth_note(n++).note(), note.tnote().note());
 		}
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void collection_is_immutable(){ test_arranger(test_score()).iterator().remove();	}
 	
 	/**
 	 * given a score
 	 * 	will store string, fret and finger allocations for each note in the score
 	 */
 	@Test
 	public void stores_string_allocations(){
 		Random seed = new Random();
 		Arrangement arrangement = test_arranger(test_score());
 		List<GuitarString> strings = new LinkedList<GuitarString>();
 		for(FingeredNote note : arrangement){
 			GuitarString string = GuitarString.values()[seed.nextInt(GuitarString.values().length)];
 			note.setString(string); 
 			strings.add(string);
 		}
 		
 		Iterator<GuitarString> string_iterator = strings.iterator();
 		for(FingeredNote note : arrangement){
 			assertEquals(string_iterator.next(), note.string());
 		}
 	}
 	
 	/**
 	 * given a score
 	 * 	will store string, fret and finger allocations for each note in the score
 	 */
 	@Test
 	public void stores_finger_allocations(){
 		Random seed = new Random();
 		Arrangement arrangement = test_arranger(test_score());
 		for(int n=0; n<10; n++){
 			List<Integer> fingers = new LinkedList<Integer>();
 			for(FingeredNote note : arrangement){ 
				if(note.fret()==0){ note.setFinger(0); }
				else{
					int finger = seed.nextInt(Guitar.FINGERS.length) + 1;
					note.setFinger(finger);
					fingers.add(finger);
				}
 			}
 			
 			Iterator<Integer> finger_iterator = fingers.iterator();
 			for(FingeredNote note : arrangement){
 				assertEquals(finger_iterator.next(), (Integer)note.finger());
 			}
 		}
 	}
 	
 	/**
 	 * given a score
 	 * 	will store string, fret and finger allocations for each note in the score
 	 */
 	@Test
 	public void stores_fret_allocations(){
 		Random seed = new Random();
 		Arrangement arrangement = test_arranger(test_score());
 		List<Integer> frets = new LinkedList<Integer>();
 		for(FingeredNote note : arrangement){
 			int fret = seed.nextInt(Guitar.FRETS+1);
 			frets.add(fret);
 			note.setFret(fret); 
 		}
 		
 		Iterator<Integer> fret_iterator = frets.iterator();
 		for(FingeredNote note : arrangement){
 			assertEquals(fret_iterator.next(), (Integer)note.fret());
 		}
 	}
 	
 	/**
 	 * can randomise all fingering data, e.g. for an initial population
 	 */
 	@Test
 	public void randomise_entire_fingering_arrangement(){
 		Arrangement arrangement;
 		for(int z=0; z<40; z++){ //repeat to catch probabilistic weirdness e.g. intermitteny runtime errors
 			Note random_note = NoteFactory.getRandomNote();
 			arrangement = new Arrangement(new Score(new TimedNote[]{
 					new TimedNote(random_note, 1f, 1f), new TimedNote(random_note, 2f, 1f), new TimedNote(random_note, 3f, 1f)
 			}));
 			arrangement.randomise();
 			FingeredNote[] notes = new FingeredNote[arrangement.size()];
 			int n=0;
 			for(FingeredNote note : arrangement ){	notes[n++] = note;	}
 			
 			assertFalse(notes[0].equals(notes[1]) && notes[0].equals(notes[2]));
 			assertFalse(notes[1].equals(notes[2]) && notes[0].equals(notes[2]));
 		}
 	}
 	
 	/**
 	 * can be tested for equality against other arrangements
 	 */
 	@Test
 	public void equality_against_other_arrangements(){
 		/*
 		 * a score object
 		 * a collection of fingered notes
 		 * 
 		 * equality if score same & fingered notes same
 		 */
 		Score score1, score2;
 		score1 = ScoreSpec.get_test_score();
 		score2 = ScoreSpec.get_test_score();
 		Arrangement arr1, arr2, arr3;
 		arr1 = new Arrangement(score1);
 		arr2 = new Arrangement(score2);
 		assertFalse("different score, but considered equal", arr1.equals(arr2));
 		assertEquals("same object equality fail", arr1, arr1);
 		assertEquals("same object equality fail", arr2, arr2);
 		
 		arr3 = arr1.clone();
 		assertEquals("different object same arrangement", arr1, arr3);
 		
 		boolean changed = false, equality = true;
 		for(int n=0;n<5;n++){
 			arr3.randomise();
 			if(! arr3.equals(arr1)){ equality = false; }	
 			Arrangement arr4 = arr3.clone();
 			float key = arr4.fingered_notes().firstKey();
 			arr4.fingered_notes().put(key, arr1.fingered_notes().get(key));
 			if(! arr3.equals(arr4)) changed = true; 
 		}
 		assertFalse("different values (arrangement randomised) considered equal ",equality);
 		assertTrue("should not be equal, since a fingered note has changed",changed);
 	}
 	
 	@Test
 	public void clones_correctly(){
 		Arrangement arrangement = test_arranger(test_score());
 		Arrangement clone = arrangement.clone();
 		assertNotSame("same object returned by clone",clone, arrangement);
 		assertEquals("cloning should maintain equality", clone, arrangement);
 		
 		assertNotSame("fingered notes should be deep collection",clone.fingered_notes(), arrangement.fingered_notes());
 		assertEquals("fingered notes collection contents should not be changed by cloning", clone.fingered_notes(), arrangement.fingered_notes());
 	}
 	
 	/**
 	 * creates hashcodes distinct from nonequal arrangements
 	 */
 	@Test
 	public void hashcode_distinctness(){
 		Arrangement arrangement = test_arranger(test_score());
 		assertEquals("same object hashcode production inconsistent",arrangement.hashCode(), arrangement.hashCode());
 		assertEquals("clone object hashcode production inconsistent",arrangement.hashCode(), arrangement.clone().hashCode());
 		Arrangement a2 = arrangement.clone();	a2.randomise();
 		assertFalse("different values producing same hashcode", arrangement.equals(a2));
 	}
 	
 	/**
 	 * holds some ranking metadata for the evolution algorithms
 	 */
 	@Test
 	public void holds_ranking_metadata_for_evolution(){
 		Arrangement arrangement = test_arranger(test_score());
 		arrangement.assign_cost(100);
 		assertEquals(100, arrangement.cost());
 	}
 	
 	/**
 	 * provides access to fingered notes for replacement by crossover/breeding mechanism
 	 */
 	@Test
 	public void provides_list_access_to_fingered_notes(){
 		assertTrue(test_arranger(test_score()).fingered_notes().size()>0);
 	}
 	
 	public static Score test_score(){
 		return ScoreSpec.get_test_score();
 	}
 	
 	public static Arrangement test_arranger(Score score){
 		return new Arrangement(score);
 	}
 }
