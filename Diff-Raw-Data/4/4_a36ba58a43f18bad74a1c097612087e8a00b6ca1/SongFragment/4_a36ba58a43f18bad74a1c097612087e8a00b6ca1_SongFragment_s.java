 package messaging;
 
 import com.digitalxyncing.document.impl.DocumentFragment;
 import instruments.*;
 
 /**
  * Represents a fragment of a {@link SongDocument}. For now, this is merely a single note of a song but it could be
  * more coarse grained and consist of several notes.
  */
 public class SongFragment extends DocumentFragment<Song> {
 
     private Instrument.Note note;
 
     public SongFragment(Instrument.Note note) {
         this.note = note;
         this.data = note.getFileName().getBytes();
     }
 
 
     /**
      * The serialized fragments
      * @param fragmentString
      */
     public SongFragment(String fragmentString) {
         // TODO This is crude, but using it for testing purposes...
         Instrument instrument = null;
         if (fragmentString.startsWith("piano")) {
             instrument = new Piano();
         }
 
         if(instrument == null && fragmentString.startsWith("drums")) {
             instrument = new Drums();
         }
 
         if(instrument == null && fragmentString.startsWith("guitar")) {
             instrument = new Guitar();
         }
 
         if(instrument == null && fragmentString.startsWith("gameboy")) {
             instrument = new Gameboy();
         }
 
         if (instrument == null && fragmentString.startsWith("daft")) {
             instrument  = new Daft();
         }
 
         if (instrument == null && fragmentString.startsWith("kmart")) {
             instrument = new Kmart();
         }
 
         if (instrument == null && fragmentString.startsWith("scratch")) {
             instrument = new Scratch();
         }
 
         if(instrument == null){
             System.out.println("Unrecognized note " + fragmentString);
         } else {
             String name = fragmentString.substring(fragmentString.indexOf("/") + 1, fragmentString.indexOf("."));
             this.note = instrument.getNoteByName(name);
             this.data = note.getFileName().getBytes();
         }
     }
 
     public Instrument.Note getNote() {
         return note;
     }
 }
