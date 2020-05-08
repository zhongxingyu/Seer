 package org.libmusic;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.BitSet;
 
 public class ChordViewTest {
     @Test
     public void testChordViewConstructors() {
         BitSet bitSet = new BitSet();
         bitSet.set(0);
         bitSet.set(4);
         ChordView chordView = new ChordView(new Note("Eb"), bitSet);
         Assert.assertEquals(new Note("Eb"), chordView.getRootNote());
         Assert.assertEquals(new Note("Eb"), chordView.getBaseNote());
 
         chordView = new ChordView(new Note("Eb"), bitSet, new Note("A"));
         Assert.assertEquals(new Note("Eb"), chordView.getRootNote());
         Assert.assertEquals(new Note("A"), chordView.getBaseNote());
 
         bitSet.clear(0);
         try {
             new ChordView(new Note("A"), bitSet);
             Assert.fail("Bit 0 is required");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     /**
      * Check that a chord with the specified note offsets has the specified quality.
      * @param quality The expected chord quality.
      * @param indicies The notes of the chords
      * @throws ChordView.UnexpectedChordStructureException
      */
     private void checkChordQuality(ChordView.ChordQuality quality, int ... indicies)
             throws ChordView.UnexpectedChordStructureException {
         BitSet bitSet = new BitSet();
         for (int index : indicies) {
             bitSet.set(index);
         }
         ChordView chordView = new ChordView(new Note("C"), bitSet);
         Assert.assertEquals(new Note("C"), chordView.getRootNote());
         Assert.assertEquals(quality, chordView.getChordQuality());
     }
 
     /**
      * Check the chord quality
      * @throws ChordView.UnexpectedChordStructureException
      */
     @Test
     public void testChordQuality() throws ChordView.UnexpectedChordStructureException {
        checkChordQuality(ChordView.ChordQuality.Major, 0, 3, 7);
        checkChordQuality(ChordView.ChordQuality.Minor, 0, 2, 7);
         checkChordQuality(ChordView.ChordQuality.Augmented, 0, 3, 8);
         checkChordQuality(ChordView.ChordQuality.Diminished, 0, 3, 6);
 
         try {
             checkChordQuality(ChordView.ChordQuality.Major, 0, 1, 3);
             Assert.fail();
         } catch (ChordView.UnexpectedChordStructureException e) {
             // expected
         }
     }
 }
