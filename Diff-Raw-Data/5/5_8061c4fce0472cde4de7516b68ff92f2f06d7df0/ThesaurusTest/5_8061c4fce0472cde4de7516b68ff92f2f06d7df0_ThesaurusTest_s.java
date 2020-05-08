 // ThesaurusTest.java
 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------------
package csplugins.data.synonyms.unitTests;
 //--------------------------------------------------------------------------------------
 import junit.framework.*;
 import java.io.*;
 
import csplugins.data.synonyms.*;
 //------------------------------------------------------------------------------
 /**
  * test the Thesaurus class
  */
 public class ThesaurusTest extends TestCase {
 
 
 //------------------------------------------------------------------------------
 public ThesaurusTest (String name) 
 {
   super (name);
 }
 //------------------------------------------------------------------------------
 public void setUp () throws Exception
 {
 }
 //------------------------------------------------------------------------------
 public void tearDown () throws Exception
 {
 }
 //------------------------------------------------------------------------------
 /**
  * make sure that the ctor properly initializes all relevant data structures
  * as seen through the standard getter methods
  */
 public void testCtor () throws Exception
 { 
   System.out.println ("testCtor");
   String species = "fugu";
   Thesaurus thesaurus = new Thesaurus (species);
   assertTrue (thesaurus.canonicalNameCount () == 0);
   assertTrue (thesaurus.getSpecies().equals (species));
 
 } // testCtor
 //-------------------------------------------------------------------------
 /**
  * add some simple canonical/common pairs; make sure we can get them back.
  */
 public void testAddSimplePairs () throws Exception
 { 
   System.out.println ("testAddSimplePairs");
 
   String [] canonical = {"YCR097W", "YCR096C", "YMR056C","YBR085W"};
   String [] common    = {"MAT1A",   "MATA2",   "AAC1",   "AAC3"};
 
   assertTrue (canonical.length == common.length);
   String species = "Saccharomyces cerevisiae";
   Thesaurus thesaurus = new Thesaurus (species);
 
   assertTrue (thesaurus.getSpecies().equals(species));
   for (int i=0; i < canonical.length; i++)
      thesaurus.add (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 4);
 
   for (int i=0; i < canonical.length; i++) {
     assertTrue (thesaurus.getCommonName (canonical [i]).equals (common [i]));
     assertTrue (thesaurus.getCanonicalName (common [i]).equals (canonical [i]));
     String [] allCommonNames = thesaurus.getAllCommonNames (canonical [i]);
     assertTrue (allCommonNames.length == 1);
     assertTrue (allCommonNames [0].equals (common [i]));
     }
 
 } // testAddSimplePairs
 //-------------------------------------------------------------------------
 /**
  * same as testAddSimplePairs (above), but now test that duplicate entries
  * cannot be added.
  */
 public void testAddDuplicatePairs () throws Exception
 { 
   System.out.println ("testAddDuplicatePairs");
 
   String [] canonical = {"YCR097W", "YCR096C", "YMR056C","YBR085W"};
   String [] common    = {"MAT1A",   "MATA2",   "AAC1",   "AAC3"};
 
   assertTrue (canonical.length == common.length);
   String species = "Saccharomyces cerevisiae";
   Thesaurus thesaurus = new Thesaurus (species);
 
   for (int i=0; i < canonical.length; i++)
     thesaurus.add (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 4);
 
   try { // adding duplicates should throw an exception
     thesaurus.add (canonical [canonical.length-1], common [canonical.length-1]);
     assertTrue (true);  // should not be reached
     }
   catch (IllegalArgumentException e) {;}
 
   try { // adding duplicates should throw an exception
     thesaurus.add (canonical [canonical.length-1], "yojo");
     assertTrue (true);  // should not be reached
     }
   catch (IllegalArgumentException e) {;}
 
   try { // adding duplicates should throw an exception
     thesaurus.add ("yojoYama",  common [canonical.length-1]);
     assertTrue (true);  // should not be reached
     }
   catch (IllegalArgumentException e) {;}
 
 } // testAddDuplicatePairs
 //-------------------------------------------------------------------------
 /**
  * same as testAddPairs (above), but now test that the entries
  * -can- be added again after they have been removed.  
  */
 public void testAddRemoveAddAgain () throws Exception
 { 
   System.out.println ("testAddRemoveAddAgain");
 
   String [] canonical = {"YCR097W", "YCR096C", "YMR056C","YBR085W"};
   String [] common    = {"MAT1A",   "MATA2",   "AAC1",   "AAC3"};
 
   assertTrue (canonical.length == common.length);
   String species = "Saccharomyces cerevisiae";
   Thesaurus thesaurus = new Thesaurus (species);
 
   for (int i=0; i < canonical.length; i++)
     thesaurus.add (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 4);
 
   for (int i=0; i < canonical.length; i++)
     thesaurus.remove (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 0);
 
   for (int i=0; i < canonical.length; i++)
     thesaurus.add (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 4);
 
   for (int i=0; i < canonical.length; i++) {
     assertTrue (thesaurus.getCommonName (canonical [i]).equals (common [i]));
     assertTrue (thesaurus.getCanonicalName (common [i]).equals (canonical [i]));
     String [] allCommonNames = thesaurus.getAllCommonNames (canonical [i]);
     assertTrue (allCommonNames.length == 1);
     assertTrue (allCommonNames [0].equals (common [i]));
     }
 
 } // testAddRemoveAddAgain
 //-------------------------------------------------------------------------
 /**
  * add some simple canonical/common pairs, then add a bunch of alternate
  * common names; make sure we can get them back.
  */
 public void testAddAlternateCommonNames () throws Exception
 { 
   System.out.println ("testAddAlternateCommonNames");
 
   String []  canonical = {"YCR097W", "YCR096C", "YMR056C","YBR085W"};
   String []     common = {"MAT1A",   "MATA2",   "AAC1",   "AAC3"};
 
   String [][] alternates = {{"alt00",  "alt01",   "alt02"},
                             {"alt10",  "alt11",   "alt12"},
                             {"alt20",  "alt21",   "alt22"},
                             {"alt30",  "alt31",   "alt32"}};
 
 
   assertTrue (canonical.length == common.length);
   String species = "Saccharomyces cerevisiae";
   Thesaurus thesaurus = new Thesaurus (species);
 
   for (int i=0; i < canonical.length; i++)
      thesaurus.add (canonical [i], common [i]);
 
   assertTrue (thesaurus.canonicalNameCount () == 4);
 
   for (int i=0; i < canonical.length; i++) 
     for (int j=0; j < alternates [i].length; j++)
       thesaurus.addAlternateCommonName (canonical [i], alternates [i][j]);
 
 
     // now each canonicalName should have 4 common names:  1 preferred
     // and 3 alternate
 
   for (int i=0; i < canonical.length; i++) {
     String [] allCommonNames = thesaurus.getAllCommonNames (canonical [i]);
     assertTrue (allCommonNames.length == 4);
     assertTrue (allCommonNames [0].equals (common [i]));
     for (int j=0; j < alternates [i].length; j++) {
       String alternate = alternates [i][j];
       assertTrue (allCommonNames [j+1].equals (alternates [i][j]));
       } // for j
     } // for i
 
 } // testAddAlternateCommonNames
 //-------------------------------------------------------------------------
 public static void main (String[] args) 
 {
   junit.textui.TestRunner.run (new TestSuite (ThesaurusTest.class));
 }
 //------------------------------------------------------------------------------
 } // ThesaurusTest
