 package ambassadorTest;
 
 import Entries.Article;
 import Entries.Book;
 import Entries.Entry;
 import Entries.Inproceedings;
 import ambassador.Bibwriter;
 import applicationLogic.Build;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 public class BibwriterTest extends TestCase {
 
     public BibwriterTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     
     @Test
     public void testisWrittenFileInThere() throws FileNotFoundException {
         Inproceedings i = Build.Inproceedings("Jerry", "Julkaisu", "SUPERSIISTIMIES", 1992);
         ArrayList<Entry> ents = new ArrayList<Entry>();
         ents.add(i);
         Bibwriter.writeReferencesFromList(ents);
         List<Entry> juniorEnts = Bibwriter.readAndListReferences();
         System.out.println(juniorEnts.get(juniorEnts.size()-1).toString());
         assertTrue(juniorEnts.get(juniorEnts.size()-1).toString().contains("SUPERSIISTIMIES"));
     }
     
     @Test
    public void readReferencesWorks(){
         Build.Inproceedings("Read", "Reference", "Works", 1992);
        List list = Bibwriter.readAndListReferences1();
         assertFalse(list.isEmpty());
     }
 }
