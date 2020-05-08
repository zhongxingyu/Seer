 package music;
 
 /**
  * Note represents a note played by an instrument. 
  */
 public class Note implements Music {
     private final double duration;
     private final Pitch pitch;
     private final Instrument instrument;
     
     private void checkRep() {
         assert duration >= 0;
         assert instrument != null;
     }
     
     /**
      * Make a Note.
      * @requires duration >= 0, pitch != null, instrument != null
      * @return note played by instrument for duration beats
      */
     public Note(double duration, Pitch pitch, Instrument instrument) {
         this.duration = duration;
         this.pitch = pitch;
         this.instrument = instrument;
         checkRep();
     }
     
     /**
      * @return duration of this note
      */
     public double duration() {
         return duration;
     }
 
     /**
      * @return pitch of this note
      */
     public Pitch pitch() {
         return pitch;
     }
 
     /**
      * @return instrument that should play this note
      */
     public Instrument instrument() {
         return instrument;
     }
     
     /**
      * @requires v != null
      */
     public <T> T accept(Visitor<T> v) {
         return v.on(this);
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         long temp;
         temp = Double.doubleToLongBits(duration);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         result = prime * result
                 + ((instrument == null) ? 0 : instrument.hashCode());
         result = prime * result + ((pitch == null) ? 0 : pitch.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         final Note other = (Note) obj;
         if (duration != other.duration)
             return false;
         if (!instrument.equals(other.instrument))
             return false;
         if (!pitch.equals(other.pitch))
             return false;
         return true;
     }
 
     @Override
     public String toString() {
         return pitch.toString() + duration;
     }
     
    public static void testNote() {
        Note n = new Note(10, new Pitch('C'), Instrument.PIANO);
        System.out.println("Note is: " + n);
        Note n = new Note(10, new Pitch('C').transpose(1), Instrument.PIANO);
        System.out.println("Note is: " + n);
     }
 }
