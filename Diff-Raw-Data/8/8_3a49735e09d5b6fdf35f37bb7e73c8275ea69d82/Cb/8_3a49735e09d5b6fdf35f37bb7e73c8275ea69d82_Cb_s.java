 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.ShortMessage;
 import javax.sound.midi.Track;
 class note {
      public int pitch;
     public int octave;
     public int duration;
      public note() {
         pitch = 0;
         octave = 0;
         duration = 0;
     }
      public note(int p, int o, int d) {
         pitch = p;
         octave = o;
         duration = d;
     }
      public boolean isValid() {
         return -1 <= pitch && 11 >= pitch && -5 <= octave && 5 <= octave;
     }
      public String toString()  {
         return "(" + pitch + "," + octave + "," + duration + ")";
     }
      public chord toChord() {
         ArrayList<note> tmp_list = new ArrayList<note>(1);
         tmp_list.add(new note(pitch, octave, duration));
         return new chord(tmp_list, duration);
     }
      public note deepCopy() {
         return new note(this.pitch, this.octave, this.duration);
     }
 }
  class chord {
      public ArrayList<note> notelist;
     public int chord_duration;
      public chord() {
         notelist = new ArrayList<note>();
         chord_duration = 0;
     }
      public chord(ArrayList<note> nl, int cd) {
         notelist = nl;
         chord_duration = cd;
     }
      public String toString() {
         String s = "([";
         for(note n : notelist) {
             s += n.toString();
         }
         return s + "], " + chord_duration + ")";
     }
      private ArrayList<note> nlCopy() {
         ArrayList<note> tmp = new ArrayList<note>(notelist.size());
         for(note n : notelist) {
             tmp.add(n.deepCopy());
         }
         return tmp;
     }
      public chord deepCopy() {
         return new chord(nlCopy(), this.chord_duration);
     }
 }
  class scale {
      public ArrayList<note> scale_notelist;
      public scale() {
         scale_notelist = new ArrayList<note>();
     }
      public scale(ArrayList<note> snl) {
         scale_notelist = snl;
     }
      public String toString() {
         String s = "[";
         for(note n : scale_notelist) {
             s += n.toString();
         }
         return s + "]";
     }
      public scale deepCopy() {
         ArrayList<note> tmp = new ArrayList<note>(scale_notelist.size());
         for(note n : scale_notelist) {
             tmp.add(n.deepCopy());
         }
         return new scale(tmp);
     }
 }
  class stanza {
      public ArrayList<chord> chordlist;
      public stanza() {
         chordlist = new ArrayList<chord>();
     }
      public stanza(ArrayList<chord> cl) {
         chordlist = cl;
     }
      public String toString() {
         String s = "[";
         for(chord c : chordlist) {
             s += c.toString();
         }
         return s + "]";
     }
      public stanza deepCopy() {
         ArrayList<chord> tmp = new ArrayList<chord>(chordlist.size());
         for(chord c : chordlist) {
             tmp.add(c.deepCopy());
         }
         return new stanza(tmp);
     }
 }
  class score {
      public ArrayList<stanza> stanzalist;
     public int instrument;
      public score() {
         stanzalist = new ArrayList<stanza>();
         instrument = 0;
     }
      public score(ArrayList<stanza> sl, int i) {
         stanzalist = sl;
         instrument = i;
     }
      public String toString() {
         String s = "([";
         for(stanza st : stanzalist) {
             s += st.toString();
         }
         return s + "], instrument=" + instrument + ")";
     }
      public score deepCopy() {
         ArrayList<stanza> tmp = new ArrayList<stanza>(stanzalist.size());
         for(stanza s : stanzalist) {
             tmp.add(s.deepCopy());
         }
         return new score(tmp, this.instrument);
     }
 }
  public class Cb {
     /**
      * ********************compose helper functions****************************
      * source: http://www.penguinpeepshow.com/CSV2MIDI.php
      * helper function for mapping values of pitch and octave to range 0 - 127
      */
     static long map(long x, long in_min, long in_max, long out_min, long out_max) {
         return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
     }
     /*
      * note representation: tick, duration, pitch, volume/loudness/velocity
      * turns note on
      */
      private static MidiEvent createNoteOnEvent(int nKey, long lTick, int channel, int velocity) {
         return createNoteEvent(ShortMessage.NOTE_ON, nKey, velocity, lTick, channel);
     }
      /*
      * turns note off
      */
     private static MidiEvent createNoteOffEvent(int nKey, long lTick, int channel) {
         return createNoteEvent(ShortMessage.NOTE_OFF, nKey, 0, lTick, channel);  //set note to 0 velocity
     }
      /*
      * turns note on or off
      */
     private static MidiEvent createNoteEvent(int nCommand, int nKey, int nVelocity, long lTick, int channel) {
         ShortMessage message = new ShortMessage();
         try {
             message.setMessage(nCommand, channel, nKey, nVelocity);
         } catch (InvalidMidiDataException e) {
             e.printStackTrace();
             System.exit(1);
         }
         MidiEvent event = new MidiEvent(message, lTick);
         return event;
     }
     /*
      * Here are all of the built in methods that we have and while we
      * are translating we need to make sure you do not overwrite any of these
      * bad boys unless you give it different args (or maybe we can just let
      * java take care of that, I am not fully certain)
      * Note: this score list has to hold at most 16 stanzas
      */
     public static void compose(ArrayList<score> data) throws InvalidMidiDataException {
         int nChannels = data.size();
         Sequence sequence = null;
  //      for(int i=0;i<data.size();i++)
 //          System.out.println(data.get(i));
           //***** Read in timing resolution and instruments *****
         int timingRes = 4, instrument[] = new int[nChannels];
          //read in instrument numbers
         for (int inst = 0; inst < data.size(); inst++) {
             //check if this is an integer
             instrument[inst] = data.get(inst).instrument; //this is a number, it has to be an intrument
             System.out.println("Instrument set to " + instrument[inst] + " on channel " + inst);
         }
          //***** Initialize Sequencer *****
         try {
             sequence = new Sequence(Sequence.PPQ, timingRes);   //initialize sequencer with timingRes
         } catch (InvalidMidiDataException e) {
             e.printStackTrace();
             System.exit(1);
         }
           //***** Create tracks and notes *****
         /* Track objects cannot be created by invoking their constructor
          directly. Instead, the Sequence object does the job. So we
          obtain the Track there. This links the Track to the Sequence
          automatically.
          */
         Track track[] = new Track[nChannels];
         for (int i = 0; i < nChannels; i++) {
             track[i] = sequence.createTrack();                    //create tracks
              ShortMessage sm = new ShortMessage();
             sm.setMessage(ShortMessage.PROGRAM_CHANGE, i, instrument[i], 0);  //put in instrument[i] in this track
             track[i].add(new MidiEvent(sm, 0));
         }
          int nt = 0,
                 tick = 0,
                 duration = 5,
                 velocity = 90; // this is the volume of the sound
          for (int channel = 0; channel < data.size(); channel++) {
             //populating the ith track
             ArrayList<stanza> stan = data.get(channel).stanzalist;
             for (int tr = 0; tr < stan.size(); tr++) {
                  ArrayList<chord> chl = stan.get(tr).chordlist;
                 for (int cl = 0; cl < chl.size(); cl++) { //chord list
                      ArrayList<note> tnote = chl.get(cl).notelist;
                     for (int nti = 0; nti < tnote.size(); nti++) { //note list
                          duration = (int) (chl.get(cl).chord_duration * (5 / 16));  //second number is duration
                         // octave (-5 to 5); pitch (0 to 11)
                         nt = (int) map((long) (tnote.get(nti).pitch + tnote.get(nti).octave), -5, 16, 0, 127); //this is the pitch representation; middle c = c_4 = 60
                         velocity = 127;  //velocity can not be changed for now
                          if (tnote.get(nti).pitch < 0) { // a rest is received -- any negative note is rest
                              nt = 0;
                             track[channel].add(createNoteOffEvent(nt, tick, channel));              //add note to this track
                          } else {
                             track[channel].add(createNoteOnEvent(nt, tick, channel, velocity));             //add note to this track
                         }
                         tick = tick + duration;  //first number is tick
                         track[channel].add(createNoteOffEvent(nt, tick + duration, channel));
                     }
                     tick = tick + duration;
                 }
             }
             tick = 0;
         }
           // Print track information
 //      System.out.println();
 //      if ( track != null ) {
 //          for ( int i = 0; i < track.length; i++ ) {
 //              System.out.println("Track " + i + ":" );
 //
 //          for ( int j = 0; j < track[i].size(); j++ ) {
 //                  MidiEvent event = track[i].get( j );
 //                  System.out.println(" tick "+event.getTick()+", "+MessageInfo.toString(event.getMessage()));
 //              }
 //          }
 //      }
          /* Now we just save the Sequence to the file we specified.
          The '0' (second parameter) means saving as SMF type 0.
          (type 1 is for multiple tracks).
          */
         try {
             MidiSystem.write(sequence, 1, new File("out.mid"));
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
      // assumes it is passed a minor scale
     public static chord major(scale s) throws Exception {
         ArrayList<note> notes = new ArrayList<note>();
         if(s.scale_notelist.size() >= 5){
             notes.add(s.scale_notelist.get(0));
             notes.add(flat(s.scale_notelist.get(2)));
             notes.add(s.scale_notelist.get(4));
         }
         else
             throw new Exception("To convert a scale into a major chord, you must pass a scale of at least five notes");
          int duration = s.scale_notelist.get(0).duration;
          return new chord(notes, duration);
     }
      public static chord major(scale s, int duration) throws Exception {
         ArrayList<note> notes = new ArrayList<note>();
         if(s.scale_notelist.size() >= 5){
             notes.add(s.scale_notelist.get(0));
             notes.add(flat(s.scale_notelist.get(2)));
             notes.add(s.scale_notelist.get(4));
         }
         else
             throw new Exception("To convert a scale into a major chord, you must pass a scale of at least five notes");
         return new chord(notes, duration);
     }
      public static chord minor(scale s) throws Exception {
         ArrayList<note> notes = new ArrayList<note>();
         if(s.scale_notelist.size() >= 5){
             notes.add(s.scale_notelist.get(0));
             notes.add(flat(s.scale_notelist.get(2)));
             notes.add(s.scale_notelist.get(4));
         }
         else
             throw new Exception("To convert a scale into a minor chord, you must pass a scale of at least five notes");
         int duration = s.scale_notelist.get(0).duration;
         return new chord(notes, duration);
     }
      public static chord minor(scale s, int duration) throws Exception {
         ArrayList<note> notes = new ArrayList<note>();
         if(s.scale_notelist.size() >= 5){
             notes.add(s.scale_notelist.get(0));
             notes.add(flat(s.scale_notelist.get(2)));
             notes.add(s.scale_notelist.get(4));
         }
         else
             throw new Exception("To convert a scale into a minor chord, you must pass a scale of at least five notes");
         return new chord(notes, duration);
     }
       public static note sharp(note n) throws Exception {
         note sharped = new note();
         if(n.pitch == 11)
             sharped = new note(0, n.octave+1, n.duration);
         else
             sharped = new note(n.pitch + 1, n.octave, n.duration);
         if(sharped.isValid())
             return sharped;
         else
             throw new Exception("To convert a scale into a minor chord, you must pass a scale of at least five notes");
     }
      public static note flat(note n) {
         return new note();
     }
      /**
      * Return a random integer between 0 and i
      * @param i The maximum value for the integer
      * @return A random integer between 0 and i
      */
     public static int randint(int i) {
         return (int)(Math.random()*i);
     }
      public static chord chordOfNote(note n) {
         return n.toChord();
     }
      public static chord rest(int d) {
         ArrayList<note> temp = new ArrayList<note>(1);
         temp.add(new note(-1, 0, d));
         return new chord(temp, d);
     }
      public static chord prepend(note n, chord c) {
         chord tmp = c.deepCopy();
         tmp.notelist.add(0, n.deepCopy());
         return tmp;
     }
      public static scale prepend(note n, scale s) {
         scale tmp = s.deepCopy();
         tmp.scale_notelist.add(0, n.deepCopy());
         return tmp;
     }
      public static stanza prepend(chord c, stanza s) {
         stanza tmp = s.deepCopy();
         tmp.chordlist.add(0, c.deepCopy());
         return tmp;
     }
      public static score prepend(stanza st, score sc) {
         score tmp = sc.deepCopy();
         tmp.stanzalist.add(0, st.deepCopy());
         return tmp;
     }
      public static chord append(note n, chord c) {
         chord tmp = c.deepCopy();
         tmp.notelist.add(n.deepCopy());
         return tmp;
     }
      public static scale append(note n, scale s) {
         scale tmp = s.deepCopy();
         tmp.scale_notelist.add(n.deepCopy());
         return tmp;
     }
      public static stanza append(chord c, stanza s) {
         stanza tmp = s.deepCopy();
         tmp.chordlist.add(c.deepCopy());
         return tmp;
     }
      public static score append(stanza st, score sc) {
         score tmp = sc.deepCopy();
         tmp.stanzalist.add(st.deepCopy());
         return tmp;
     }
      public static scale concat(scale s1, scale s2) {
         scale tmp = s1.deepCopy();
         for(note n : s2.scale_notelist) {
             tmp.scale_notelist.add(n.deepCopy());
         }
         return tmp;
     }
      public static stanza concat(stanza s1, stanza s2) {
         stanza tmp = s1.deepCopy();
         for(chord c : s2.chordlist) {
             tmp.chordlist.add(c.deepCopy());
         }
         return tmp;
     }
      public static score concat(score s1, score s2) {
         score tmp = s1.deepCopy();
         for(stanza s : s2.stanzalist) {
             tmp.stanzalist.add(s.deepCopy());
         }
         return tmp;
     }
      public static scale repeat(note n, int i) throws Exception {
         scale tmp = new scale();
         if (i < 1) {
             throw new Exception("repeat function takes an integer that must be 1 or greater");
         }
         for(int j = 0; j < i; j++) {
             tmp.scale_notelist.add(n.deepCopy());
         }
         return new scale();
     }
      public static stanza repeat(chord c, int i) throws Exception {
         stanza tmp = new stanza();
         if (i < 1) {
             throw new Exception("repeat function takes an integer that must be 1 or greater");
         }
         for(int j = 0; j < i; j++) {
             tmp.chordlist.add(c.deepCopy());
         }
         return new stanza();
     }
      public static score repeat(stanza s, int i) throws Exception{
         score tmp = new score();
         if (i < 1) {
             throw new Exception("repeat function takes an integer that must be 1 or greater");
         }
         tmp.instrument = 0;
         for(int j = 0; j < i; j++) {
             tmp.stanzalist.add(s.deepCopy());
         }
         return tmp;
     }
      public static score repeat(score s, int i) throws Exception{
         score tmp = new score();
         if (i < 1) {
             throw new Exception("repeat function takes an integer that must be 1 or greater");
         }
         for(int j = 0; j < i; j++) {
             for(stanza st : s.stanzalist) {
                 tmp.stanzalist.add(st.deepCopy());
             }
         }
         tmp.instrument = s.instrument;
         return tmp;
     }
  
 int a = 5;
 
 note n =  new note(9,-1,16);
 
 chord c = new chord(new ArrayList<note>() {{
 add(n);}}, 32);
 
 scale s = new scale(new ArrayList<note>() {{
 add(n);}});
 
 stanza st = new stanza(new ArrayList<chord>() {{
 add(c);}});
 
 stanza st2 = new stanza(new ArrayList<chord>() {{
 add(c);}});
 
 score sc = new score(new ArrayList<stanza>() {{
 add(st);
 add(st2);
 add(st2);}});
      public static void main(String[] args) throws Exception { Cb runner = new Cb(); runner.run(); }
public void run() throws Exception {
     }
  }
