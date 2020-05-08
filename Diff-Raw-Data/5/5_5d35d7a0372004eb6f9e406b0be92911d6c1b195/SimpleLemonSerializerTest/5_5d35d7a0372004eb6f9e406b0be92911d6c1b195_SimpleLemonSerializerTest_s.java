 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.monnetproject.lemon.impl;
 
 import eu.monnetproject.lemon.model.Text;
 import eu.monnetproject.lemon.LemonModel;
 import eu.monnetproject.lemon.LemonModels;
 import eu.monnetproject.lemon.LemonSerializer;
 import eu.monnetproject.lemon.LinguisticOntology;
 import eu.monnetproject.lemon.model.Argument;
 import eu.monnetproject.lemon.model.Frame;
 import eu.monnetproject.lemon.model.LexicalEntry;
 import eu.monnetproject.lemon.model.LexicalForm;
 import eu.monnetproject.lemon.model.LexicalSense;
 import eu.monnetproject.lemon.model.Lexicon;
 import eu.monnetproject.lemon.model.Property;
 import eu.monnetproject.lemon.model.PropertyValue;
 import eu.monnetproject.lemon.model.SynArg;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.util.Collection;
 import java.util.Map;
 import net.lexinfo.LexInfo;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jmccrae 
  */
 public class SimpleLemonSerializerTest {
 
     public SimpleLemonSerializerTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @After
     public void tearDown() {
     }
     private final String testTurtleDoc = "@prefix lemon: <http://www.monnet-project.eu/lemon#> . "
             + "@prefix : <file:test#> . "
             + ":lexicon a lemon:Lexicon ; "
             + " lemon:entry :Cat . "
             + ":Cat a lemon:Word ; "
             + " lemon:canonicalForm [ lemon:writtenRep \"cat\"@en ] ; "
             + " lemon:sense [ lemon:reference <http://dbpedia.org/resource/Cat> ] . ";
     private final String testXMLDoc = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:lemon=\"http://www.monnet-project.eu/lemon#\">"
             + " <lemon:Lexicon rdf:about=\"file:test#lexicon\">"
             + "  <lemon:entry> "
             + "   <lemon:Word rdf:about=\"file:test#Cat\"> "
             + "    <lemon:canonicalForm rdf:parseType=\"Resource\"> "
             + "     <lemon:writtenRep xml:lang=\"en\">cat</lemon:writtenRep> "
             + "    </lemon:canonicalForm> "
             + "    <lemon:sense rdf:parseType=\"Resource\"> "
             + "     <lemon:reference rdf:resource=\"http://dbpedia.org/resource/Cat\"/> "
             + "    </lemon:sense> "
             + "   </lemon:Word> "
             + "  </lemon:entry> "
             + " </lemon:Lexicon> "
             + "</rdf:RDF>";
 
     /**
      * Test of read method, of class SimpleLemonSerializer.
      */
     @Test
     public void testRead_Reader() {
         System.out.println("read");
         Reader source = new StringReader(testXMLDoc);
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         instance.read(source);
         System.out.println("XML read OK");
         source = new StringReader(testTurtleDoc);
         instance.read(source);
         System.out.println("Turtle read OK");
     }
 
     /**
      * Test of write method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWrite_LemonModel_Writer() {
         String expResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
 "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"+
 "  <lemon:Lexicon rdf:about=\"file:test#lexicon\" xmlns:lemon=\"http://www.monnet-project.eu/lemon#\">\n"+
 "    <lemon:language>en</lemon:language>\n"+
 "    <lemon:entry>\n"+
 "      <lemon:LexicalEntry rdf:about=\"file:test#Cat\">\n"+
 "        <lemon:sense>\n"+
 "          <lemon:LexicalSense>\n"+
 "            <lemon:reference rdf:resource=\"http://dbpedia.org/resource/Cat\"/>\n"+
 "          </lemon:LexicalSense>\n"+
 "        </lemon:sense>\n"+
 "        <lemon:canonicalForm>\n"+
 "          <lemon:Form>\n"+
 "            <lemon:writtenRep xml:lang=\"en\">cat</lemon:writtenRep>\n"+
 "          </lemon:Form>\n"+
 "        </lemon:canonicalForm>\n"+
 "      </lemon:LexicalEntry>\n"+
 "    </lemon:entry>\n"+
 "  </lemon:Lexicon>\n"+
 "</rdf:RDF>";
         System.out.println("write");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel model = makeModel(instance);
         Writer target = new StringWriter();
         instance.write(model, target);
         System.out.println(target.toString());
         assertEquals(expResult, target.toString().trim());
     }
 
     private LemonModel lazyModel;
     
     private LemonModel makeModel(SimpleLemonSerializer instance) {
         if(lazyModel != null) {
             return lazyModel;
         }
         LemonModel model = instance.create(URI.create("file:test"));
         final Lexicon lexicon = model.addLexicon(URI.create("file:test#lexicon"), "en");
         LemonModels.addEntryToLexicon(lexicon, URI.create("file:test#Cat"), "cat", URI.create("http://dbpedia.org/resource/Cat"));
         return lazyModel = model;
     }
 
     /**
      * Test of create method, of class SimpleLemonSerializer.
      */
     @Test
     public void testCreate() {
         System.out.println("create");
         URI context = URI.create("file:test");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         instance.create(context);
     }
 
     /**
      * Test of writeEntry method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWriteEntry_4args() {
         System.out.println("writeEntry");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel model = makeModel(instance);
         LexicalEntry entry = model.getLexica().iterator().next().getEntrys().iterator().next();
         LinguisticOntology lingOnto = new LexInfo();
         Writer target = new StringWriter();
         instance.writeEntry(model, entry, lingOnto, target);
         System.out.println(target.toString());
     }
 
     /**
      * Test of writeLexicon method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWriteLexicon_4args() {
         //String expResult = 
         System.out.println("writeLexicon");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel model = makeModel(instance);
         Lexicon lexicon = model.getLexica().iterator().next();
         LinguisticOntology lingOnto = new LexInfo();
         Writer target = new StringWriter();
         instance.writeLexicon(model, lexicon, lingOnto, target);
         System.out.println(target.toString());
     }
 
     /**
      * Test of moveLexicon method, of class SimpleLemonSerializer.
      */
     @Test
     public void testMoveLexicon() {
         System.out.println("moveLexicon");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel from = makeModel(instance);
         Lexicon lexicon = from.getLexica().iterator().next();
         LemonModel to = instance.create(URI.create("file:test2"));
         instance.moveLexicon(lexicon, from, to);
     }
 
     /**
      * Test of read method, of class SimpleLemonSerializer.
      */
     @Test
     public void testRead_LemonModel_Reader() {
         System.out.println("read");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel lm = instance.create(URI.create("file:test"));
         Reader ds = new StringReader(testXMLDoc);
         instance.read(lm, ds);
     }
 
     /**
      * Test of write method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWrite_3args() {
         String expResult = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"+
 "@prefix lemon: <http://www.monnet-project.eu/lemon#> . \n"+
 "\n"+
 "<file:test#Cat> lemon:sense [  a lemon:LexicalSense ;\n"+
 " lemon:reference <http://dbpedia.org/resource/Cat> ] ;\n"+
 " lemon:canonicalForm [  lemon:writtenRep \"cat\"@en ;\n"+
 " a lemon:Form ] ;\n"+
 " a lemon:LexicalEntry .\n"+
 "\n"+
 "<file:test#lexicon> lemon:entry <file:test#Cat> ;\n"+
 " a lemon:Lexicon .";
         System.out.println("write");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel lm = makeModel(instance);
         Writer dt = new StringWriter();
         boolean xml = false;
         instance.write(lm, dt, xml);
         System.out.println(dt.toString());
     }
 
     /**
      * Test of writeEntry method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWriteEntry_5args() {
         String expResult = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
                 + "@prefix lemon: <http://www.monnet-project.eu/lemon#> . \n"
                 + "\n"
                 + "<file:test#Cat> lemon:sense [  a lemon:LexicalSense ;\n"
                 + " lemon:reference <http://dbpedia.org/resource/Cat> ] ;\n"
                 + " lemon:canonicalForm [  lemon:writtenRep \"cat\"@en ;\n"
                 + " a lemon:Form ] ;\n"
                 + " a lemon:LexicalEntry .";
         System.out.println("writeEntry");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel lm = makeModel(instance);
         LexicalEntry le = lm.getLexica().iterator().next().getEntrys().iterator().next();
         LinguisticOntology lo = new LexInfo();
         Writer dt = new StringWriter();
         boolean xml = false;
         instance.writeEntry(lm, le, lo, dt, xml);
         assertEquals(expResult, dt.toString().trim());
     }
 
     /**
      * Test of writeLexicon method, of class SimpleLemonSerializer.
      */
     @Test
     public void testWriteLexicon_5args() {
         String expResult = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
                 + "@prefix lemon: <http://www.monnet-project.eu/lemon#> . \n"
                 + "\n"
                 + "<file:test#Cat> lemon:sense [  a lemon:LexicalSense ;\n"
                 + " lemon:reference <http://dbpedia.org/resource/Cat> ] ;\n"
                 + " lemon:canonicalForm [  lemon:writtenRep \"cat\"@en ;\n"
                 + " a lemon:Form ] ;\n"
                 + " a lemon:LexicalEntry .\n"
                 + "\n"
                 + "<file:test#lexicon> lemon:entry <file:test#Cat> ;\n"
                 + " a lemon:Lexicon .";
         System.out.println("writeLexicon");
         SimpleLemonSerializer instance = new SimpleLemonSerializer();
         LemonModel lm = makeModel(instance);
         Lexicon lxcn = lm.getLexica().iterator().next();
         LinguisticOntology lo = new LexInfo();
         Writer dt = new StringWriter();
         boolean xml = false;
         instance.writeLexicon(lm, lxcn, lo, dt, xml);
         assertEquals(expResult, dt.toString().trim());
     }
     
     
     private final String input = "@prefix MusicBrainzLexicon: <http://monnetproject.deri.ie/lemonsource/user/httpswwwgooglecomaccountso8ididAItOawnRWNkyXGW_lk5kD1JgLCzU9MCwC_R8TY/MusicBrainzLexicon#>.\n"
             + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.\n"
             + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n"
             + "@prefix lemon: <http://www.monnet-project.eu/lemon#>.\n"
             + "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>."
             + "@prefix : <http://monnetproject.deri.ie/lemonsource/user/httpswwwgooglecomaccountso8ididAItOawnRWNkyXGW_lk5kD1JgLCzU9MCwC_R8TY/MusicBrainzLexicon#>.\n\n"
             + "MusicBrainzLexicon:lexicon a lemon:Lexicon ; lemon:entry MusicBrainzLexicon:collaborationOf.\n"
             + "MusicBrainzLexicon:collaborationOf lemon:sense [ lemon:reference <http://purl.org/vocab/relationship/collaboratesWith> ;\n"
             + "                                    lemon:subjOfProp :arg1collaboration ;\n"
            + "                                   lemon:objOfProp  :arg2collaboration ] ;\n"
             + " lexinfo:partOfSpeech lexinfo:noun ;\n"
             + "lemon:synBehavior [ rdf:type lexinfo:NounPPFrame ;\n"
             + " 	              lexinfo:subject :arg2collaboration ;\n"
             + "                    lexinfo:prepositionalObject :arg1collaboration ] ;\n"
             + "lexinfo:partOfSpeech lexinfo:noun ;\n"
             + "lemon:canonicalForm [ lemon:writtenRep \"collaboration\"@en ;\n"
             + "                      lexinfo:number lexinfo:singular ] ;\n"
             + "lemon:otherForm [ lemon:writtenRep \"collaborations\"@en ;\n"
             + "                  lexinfo:number lexinfo:plural ] .\n"
             + ""
             + ":arg2collaboration lemon:marker :Of.\n";
 
     @Test
     public void testSynArgRead() {
         final LemonSerializer lemonSerializer = LemonSerializer.newInstance();
         final LemonModel model = lemonSerializer.read(new StringReader(input));
         final Collection<Lexicon> lexica = model.getLexica();
         assertFalse(lexica.isEmpty());
         final Lexicon lexicon = lexica.iterator().next();
         final Collection<LexicalEntry> entrys = lexicon.getEntrys();
         assertFalse(entrys.isEmpty());
         final LexicalEntry entry = entrys.iterator().next();
         final Collection<Frame> synBehaviors = entry.getSynBehaviors();
         assertFalse(synBehaviors.isEmpty());
         final Frame frame = synBehaviors.iterator().next();
         final Map<SynArg, Collection<Argument>> synArgs = frame.getSynArgs();
         assertFalse(synArgs.isEmpty());
         final Map<Property, Collection<PropertyValue>> props = entry.getPropertys();
         assertFalse(props.isEmpty());
         final LexInfo lexInfo = new LexInfo();
         final Property pos = lexInfo.getProperty("partOfSpeech");
         final Collection<PropertyValue> pvs = entry.getProperty(pos);
         assertFalse(pvs.isEmpty());
         assertEquals(lexInfo.getPropertyValue("noun"),pvs.iterator().next());
         final Collection<LexicalSense> senses = entry.getSenses();
         assertFalse(senses.isEmpty());
         final LexicalSense sense = senses.iterator().next();
         assertEquals(URI.create("http://purl.org/vocab/relationship/collaboratesWith"), sense.getReference());
         assertEquals(null, sense.getRefPref());
         assertEquals(1,sense.getSubjOfProps().size());
         assertFalse(sense.getObjOfProps().isEmpty());
         assertTrue(sense.getIsAs().isEmpty());
         final Argument subject = frame.getSynArg(lexInfo.getSynArg("subject")).iterator().next();
         assertEquals(subject, sense.getObjOfProps().iterator().next());
         assertNotNull(subject.getMarker());
         assertEquals(2,frame.getTypes().size());
         for(URI frameType : frame.getTypes()) {
             assertTrue(URI.create("http://www.lexinfo.net/ontology/2.0/lexinfo#NounPPFrame").equals(frameType) ||
                     URI.create("http://www.monnet-project.eu/lemon#Frame").equals(frameType));
         }
         assertFalse(entry.getOtherForms().isEmpty());
         final LexicalForm otherForm = entry.getOtherForms().iterator().next();
         assertEquals(new Text("collaborations","en"), otherForm.getWrittenRep());
                
     }
 }
