 package abc.midi;
 
 import java.util.Vector;
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.Track;
 
 import abc.notation.AccidentalType;
 import abc.notation.BarLine;
 import abc.notation.KeySignature;
 import abc.notation.MultiNote;
 import abc.notation.Note;
 import abc.notation.RepeatBarLine;
 import abc.notation.Tempo;
 import abc.notation.Tune;
 import abc.parser.PositionableMultiNote;
 import abc.parser.PositionableNote;
 
 /** MidiConverter class defines various static methods to convert abc related stuff
  * to midi : notes, tunes etc... */
 public abstract class MidiConverterAbstract implements MidiConverterInterface {
 	/** The resolution of the sequence : this will correspond to a quarter note. */
 	private static final int SEQUENCE_RESOLUTION = Note.QUARTER;
 
   	/** Converts the given tune to a midi sequence.
   	 * @param tune The tune to be converted.
   	 * @return The midi sequence of the tune. */
   	public Sequence toMidiSequence(Tune tune) {
   		Sequence sequence = null;
   		try {
   			// Sequence in ticks per quarter note : PPQ = Pulse Per Quarter Note
   			// Resolution is expressed in ticks per beat.
   			// Last parameter "1" is the number of tracks.
   			sequence = new Sequence (Sequence.PPQ, SEQUENCE_RESOLUTION, 1);
   			Track track = sequence.createTrack();
   			//long trackLengthInTicks = track.ticks();
   			int lastRepeatOpen = -1;
   			int repeatNumber = 1;
   			boolean inWrongEnding = false;
   			int i = 0;// StaffItem iterator
   			KeySignature tuneKey = null;
   			KeySignature currentKey = null;
 
   			long elapsedTime = 0;
   			Tune.Score staff = tune.getScore();
   			while (i < staff.size()) {
   				if (!inWrongEnding) {
   					//==================================================================== TEMPO
   					if (staff.elementAt(i) instanceof abc.notation.Tempo) {
   						addTempoEventsFor(track, elapsedTime, getMidiMessagesFor((Tempo)staff.elementAt(i)));//, trackLengthInTicks));
   					}
   					else
   						//==================================================================== KEY SIGNATURE
   						if (staff.elementAt(i) instanceof abc.notation.KeySignature) {
   							tuneKey = (KeySignature)(staff.elementAt(i));
   							currentKey = new KeySignature(tuneKey.getAccidentals());
   						}
   						else
   							//==================================================================== NOTE
   							if (staff.elementAt(i) instanceof abc.notation.Note) {
   								PositionableNote note = (PositionableNote)staff.elementAt(i);
   								long noteDuration;
   								//The note duration if the note isn't part of a tuplet.
   								noteDuration = getNoteLengthInTicks(note);
   								playNote(note, i, currentKey, elapsedTime, noteDuration, track);
   								elapsedTime+=noteDuration;
   							}
   							else
   								//==================================================================== MULTI NOTE
   								if ((staff.elementAt(i) instanceof abc.notation.MultiNote)) {
   									PositionableMultiNote multiNote = (PositionableMultiNote)staff.elementAt(i);
   									playMultiNote(multiNote, i, currentKey, elapsedTime, track);
   									elapsedTime+=getNoteLengthInTicks(multiNote);
   								}
   				}
   				//====================================================================== REPEAT BAR LINE
   				if (staff.elementAt(i) instanceof abc.notation.RepeatBarLine) {
   					RepeatBarLine bar = (RepeatBarLine)staff.elementAt(i);
   					if (repeatNumber<bar.getRepeatNumber() && lastRepeatOpen!=-1) {
   						repeatNumber++;
   						i=lastRepeatOpen;
   					}
   					else
   						if (repeatNumber>bar.getRepeatNumber())
   							inWrongEnding = true;
   						else
   							inWrongEnding = false;
   				}
   				else
  					//====================================================================== BAR LINE
   					if (staff.elementAt(i) instanceof abc.notation.BarLine) {
  						currentKey = new KeySignature(tuneKey.getAccidentals());
   						switch ( ((BarLine)(staff.elementAt(i))).getType()) {
   							case BarLine.SIMPLE : break;
   							case BarLine.REPEAT_OPEN : lastRepeatOpen=i; repeatNumber=1; break;
   							case BarLine.REPEAT_CLOSE :
   								if (repeatNumber<2 && lastRepeatOpen!=-1) { 
   									repeatNumber++; i=lastRepeatOpen;
   								}
   								else {
   									repeatNumber=1; lastRepeatOpen=-1;
   								}
   								break;
   						}
   					}
   				i++;
   			}
   		}
   		catch (InvalidMidiDataException e) {
   			e.printStackTrace();
   		}
   		return sequence;
   	}
 
   	/** Generates the midi events requiered to play the given note in the context 
   	 * described by the others parameters.
   	 * @param note The note to be played.
   	 * @param indexInScore The index of the note in the score.
   	 * @param currentKey The current key this note is refering to.
   	 * @param timeReference The time reference expressed in ticks when the note should be played.
   	 * @param duration The duration of the note expressed in ticks.
   	 * @param track The track where the note should be played.
   	 * @throws InvalidMidiDataException */
   	 protected void playNote(Note note, int indexInScore, KeySignature currentKey, long timeReference, 
 		  long duration, Track track) throws InvalidMidiDataException {
   		 if (!note.isRest()) {
   			 addNoteOnEventsFor(track, timeReference, getNoteOneMessageFor(note, currentKey));
   			 addNoteOffEventsFor(track, timeReference + duration, getNoteOffMessageFor(note, currentKey));
   			 // In case the note to be played had an accidental, the current key needs
   			 // to be updated.
   			 updateKey(currentKey, note);
   		 }
   	 }
 
   protected void playMultiNote(MultiNote multiNote, int indexInScore, KeySignature currentKey, long reference, Track track) throws InvalidMidiDataException
   {
     Vector notesVector = multiNote.getNotesAsVector();
     for (int j=0; j<notesVector.size(); j++)
     {
       Note note = (Note)(notesVector.elementAt(j));
       if (!note.isRest())
         addNoteOnEventsFor(track, reference, getNoteOneMessageFor(note, currentKey));
     }
     for (int j=0; j<notesVector.size(); j++)
     {
       Note note = (Note)(notesVector.elementAt(j));
       long noteDuration = getNoteLengthInTicks(note);
       if (!note.isRest())
         addNoteOffEventsFor(track, reference+noteDuration, getNoteOffMessageFor(note, currentKey));
     }
     for (int j=0; j<notesVector.size(); j++)
       updateKey(currentKey, (Note)notesVector.elementAt(j));
   }
 
   private static void addNoteOnEventsFor(Track track, long timeReference, MidiMessage[] messages)
   {
     MidiMessage myNoteOn = messages[0];
     MidiEvent[] events = new MidiEvent[1];
     events[0] = new MidiEvent(myNoteOn,timeReference);
     addEventsToTrack(track, events);
   }
 
   private void addTempoEventsFor(Track track, long timeReference, MidiMessage[] messages) {
 	  for (int i=0; i<messages.length; i++) {
 		  MidiEvent me = new MidiEvent(messages[i], timeReference);
 		  //MidiEvent[] events = {me, new MidiEvent(new TempoMessageWA(), timeReference)};
 		  addEventsToTrack(track, me);
 	  }
   }
 
   private void addNoteOffEventsFor(Track track, long timeReference, MidiMessage[] messages)
   {
     MidiMessage myNoteOn = messages[0];
     MidiEvent[] events = new MidiEvent[1];
     events[0] = new MidiEvent(myNoteOn,timeReference);
     addEventsToTrack(track, events);
   }
 
 
   private static void updateKey(KeySignature key, Note note)
   {
     if (note.getAccidental()!=AccidentalType.NONE)
       key.setAccidental(note.toRootOctaveHeigth(), note.getAccidental());
   }
 
   /**
    * @return The length of the track in ticks, once events have been added to it.
    */
   protected static long addEventsToTrack(Track track, MidiEvent[] events) {
     if (events!=null)
       for (int i=0; i<events.length; i++)
         track.add(events[i]);
     return track.ticks();
   }
   
   protected static long addEventsToTrack(Track track, MidiEvent event) {
 	  track.add(event);
 	  return track.ticks();
   }
 
   /** */
   public abstract MidiMessage[] getNoteOneMessageFor(Note note, KeySignature key) throws InvalidMidiDataException;
 
   /** */
   public abstract MidiMessage[] getNoteOffMessageFor(Note note, KeySignature key) throws InvalidMidiDataException;
 
   /** Returns the corresponding midi events for a tempo change. */
   public abstract MidiMessage[] getMidiMessagesFor(Tempo tempo) throws InvalidMidiDataException;
 
   	/** Returns the length of the note in ticks, thanks to the sequence
   	 * resolution and the default note length. */
   	protected static long getNoteLengthInTicks(Note note) {
   		short noteLength = note.getDuration();
   		float numberOfQuarterNotesInThisNote = (float)noteLength / Note.QUARTER;
   		float lengthInTicks = (float)SEQUENCE_RESOLUTION * numberOfQuarterNotesInThisNote;
   		return (long)lengthInTicks;
   	}
 
   /** Returns the length of the multi note in ticks.
    * This length is calculated from the resolution of the midi sequence
    * manipulated internally.
    * @return The length of the multi note in ticks : this is equal to the length
    * of the longest note of the multi note. */
   public static long getNoteLengthInTicks(MultiNote note)
   {
     short longestLength = note.getLongestNote().getDuration();
     float numberOfQuarterNotesInThisNote =  (float)longestLength / Note.QUARTER;
     float lengthInTicks = (float)SEQUENCE_RESOLUTION * numberOfQuarterNotesInThisNote;
     return (long)lengthInTicks;
   }
 
   /** Returns the midi note number corresponding a note in the given key.
    * @param note The note.
    * @param key The key this note should refer to for accidentals.
    * @return The midi height of the note in the given key. */
   public static byte getMidiNoteNumber (Note note, KeySignature key)
   {
     byte heigth = note.getStrictHeight();
     byte accidental = note.getAccidental();
     byte midiNoteNumber = (byte)(heigth+(69-Note.A));
     midiNoteNumber = (byte)(midiNoteNumber + note.getOctaveTransposition()*12);
     if (accidental == AccidentalType.NONE)
     {
       byte absoluteAccidental = AccidentalType.NATURAL;
       byte heightOnOneOctave = (byte)(heigth % 12);
       absoluteAccidental = key.getAccidentalFor(heightOnOneOctave);
       switch (absoluteAccidental)
       {
           case AccidentalType.FLAT :
             midiNoteNumber --; break;
           case AccidentalType.NATURAL	:
             break;
           case AccidentalType.SHARP :
             midiNoteNumber ++; break;
       }
     }
     else
     {
       switch (accidental)
       {
           case AccidentalType.FLAT : midiNoteNumber --; break;
           case AccidentalType.NATURAL	: break;
           case AccidentalType.SHARP : midiNoteNumber ++; break;
       }
     }
     return midiNoteNumber;
   }
 }
 
