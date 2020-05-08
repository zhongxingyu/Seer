 package txtfnnl.uima.analysis_component;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import org.apache.uima.UIMAException;
 import org.apache.uima.analysis_engine.AnalysisEngine;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 
 import org.uimafit.factory.AnalysisEngineFactory;
 import org.uimafit.testing.util.DisableLogging;
 
 import txtfnnl.uima.Views;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 import txtfnnl.uima.tcas.SentenceAnnotation;
 import txtfnnl.uima.tcas.TokenAnnotation;
 
 public class TestSyntaxPatternAnnotator {
   AnalysisEngineDescription annotator;
   AnalysisEngine engine;
   File patternResource;
 
  // TODO: test more (complex) patterns!!!
   @Before
   public void setUp() {
     DisableLogging.enableLogging(Level.WARNING);
   }
 
   private void createEngine(String... patterns) throws IOException, UIMAException {
     patternResource = File.createTempFile("patterns_", ".csv");
     patternResource.deleteOnExit();
     final BufferedWriter out = new BufferedWriter(new FileWriter(patternResource));
     for (String p : patterns) {
       out.write(p);
       out.write('\n');
     }
     out.close();
     annotator = SyntaxPatternAnnotator.configure(patternResource);
     engine = AnalysisEngineFactory.createPrimitive(annotator);
   }
 
   private JCas getJCas(String[][][] sentences) throws UIMAException {
     JCas basecas = engine.newJCas();
     JCas jcas = basecas.createView(Views.CONTENT_TEXT.toString());
     StringBuffer docText = new StringBuffer();
     List<SentenceAnnotation> sentenceAnns = new LinkedList<SentenceAnnotation>();
     List<TokenAnnotation> tokenAnns = new LinkedList<TokenAnnotation>();
     int offset = 0;
     for (String[][] tokens : sentences) {
       String sentence = addTokens(jcas, offset, tokens, tokenAnns);
       SentenceAnnotation ann = new SentenceAnnotation(jcas, offset, offset + sentence.length());
       sentenceAnns.add(ann);
       docText.append(sentence);
       docText.append('\n');
       offset = docText.length();
     }
     jcas.setDocumentText(docText.toString());
     for (SentenceAnnotation ann : sentenceAnns)
       ann.addToIndexes();
     for (TokenAnnotation ann : tokenAnns)
       ann.addToIndexes();
     return basecas;
   }
 
   private String addTokens(JCas jcas, int offset, String[][] tokens,
       List<TokenAnnotation> tokenAnns) {
     StringBuffer sentence = new StringBuffer();
     String chunk = null;
     boolean start = false;
     for (String[] token : tokens) {
       if (token.length == 1) {
         if (token[0].length() == 0) {
           tokenAnns.get(tokenAnns.size() - 1).setChunkEnd(true);
           chunk = null;
         } else {
           chunk = token[0];
           start = true;
         }
       } else {
         TokenAnnotation ann = new TokenAnnotation(jcas, offset, offset + token[0].length());
         sentence.append(token[0]);
         sentence.append(' ');
         offset += token[0].length() + 1;
         if (chunk != null) {
           ann.setChunk(chunk);
           ann.setChunkBegin(start);
           start = false;
         }
         ann.setNamespace("http://nlp2rdf.lod2.eu/schema/doc/sso/"); // TODO
         ann.setPos(token[1]);
         ann.setStem(token[2]);
         tokenAnns.add(ann);
       }
     }
     return sentence.toString();
   }
 
   @Test
   public final void testBasic() throws UIMAException, IOException {
     String[][][] tokens = {
         { { "NP" }, { "The", "DT", "the" }, { "CD5", "NN", "cd5" }, { "factor", "NN", "factor" },
             { "" }, { "VP" }, { "has", "VBZ", "have" }, { "been", "VBN", "be" },
             { "shown", "VBN", "show" }, { "to", "TO", "to" }, { "bind", "VB", "bind" }, { "" },
             { "PP" }, { "at", "IN", "at" }, { "" }, { "NP" }, { "the", "DT", "the" },
             { "XYZ", "NN", "xyz" }, { "gene", "NN", "gene" }, { "" }, { "ADVP" },
             { "in", "FW", "in" }, { "vivo", "FW", "vivo" }, { "" }, { ".", ".", "." } },
         { { "NP" }, { "A", "DT", "a" }, { "INK", "NN", "ink" }, { "protein", "NN", "protein" },
             { "" }, { "VP" }, { "was", "VBZ", "be" }, { "regulating", "VB", "regulate" }, { "" },
             { "NP" }, { "a", "DT", "a" }, { "XYZ", "NN", "xyz" },
             { "promoter", "NN", "promoter" }, { "" }, { "ADVP" }, { "in", "FW", "in" },
             { "vivo", "FW", "vivo" }, { "" }, { ".", ".", "." } } };
     createEngine("( [ NP . + ] ) [ VP . * ( . ) ] [ PP ( . * ) ] ( [ NP . + ] )"
         + "\trel\tinteraction\tsem\tactor\tsem\taction\tsem\tqualifier\tsem\tactor");
     JCas doc = getJCas(tokens);
     engine.process(doc);
     JCas text = doc.getView(Views.CONTENT_TEXT.toString());
     FSIterator<Annotation> it = SemanticAnnotation.getIterator(text);
     String[] spans = { "The CD5 factor", "bind", "at", "the XYZ gene", "A INK protein",
         "regulating", "a XYZ promoter" };
     String[] semIds = { "actor", "action", "qualifier", "actor", "actor", "action", "actor" };
     int i = 0;
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       assertEquals(spans[i], ann.getCoveredText());
       assertEquals(semIds[i++], ann.getIdentifier());
     }
     assertEquals(spans.length, i);
   }
 
   @Test
   public final void testGeneRegulation() throws UIMAException, IOException {
     String[][][] tokens = {
         { { "NP" }, { "The", "DT", "the" }, { "CD5", "NN", "cd5" }, { "factor", "NN", "factor" },
             { "" }, { "VP" }, { "has", "VBZ", "have" }, { "been", "VBN", "be" },
             { "shown", "VBN", "show" }, { "to", "TO", "to" }, { "bind", "VB", "bind" }, { "" },
             { "PP" }, { "at", "IN", "at" }, { "" }, { "NP" }, { "the", "DT", "the" },
             { "XYZ", "NN", "xyz" }, { "promoter", "NN", "promoter" }, { "" }, { "ADVP" },
             { "in", "FW", "in" }, { "vivo", "FW", "vivo" }, { "" }, { ".", ".", "." } },
         { { "NP" }, { "A", "DT", "a" }, { "INK", "NN", "ink" }, { "protein", "NN", "protein" },
             { "" }, { "VP" }, { "was", "VBZ", "be" }, { "regulating", "VB", "regulate" }, { "" },
             { "NP" }, { "XYZ", "NN", "xyz" }, { "promoters", "NN", "promoter" }, { "" },
             { "ADVP" }, { "in", "FW", "in" }, { "vivo", "FW", "vivo" }, { "" }, { ".", ".", "." } } };
     createEngine("( [ NP . + ] ) [ VP . * ( bind|regulate ) ] [ PP . * ] [ NP DT_* ? ( . + ) target ? gene|promoter ]"
         + "\trel\tinteraction\tsem\tregulator\tsem\taction\tsem\ttarget");
     JCas doc = getJCas(tokens);
     engine.process(doc);
     JCas text = doc.getView(Views.CONTENT_TEXT.toString());
     FSIterator<Annotation> it = SemanticAnnotation.getIterator(text);
     String[] spans = { "The CD5 factor", "bind", "XYZ", "A INK protein", "regulating", "XYZ" };
     String[] semIds = { "regulator", "action", "target", "regulator", "action", "target" };
     int i = 0;
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       assertEquals(spans[i], ann.getCoveredText());
       assertEquals(semIds[i++], ann.getIdentifier());
     }
     assertEquals(spans.length, i);
   }
 
   @Test
   public final void testWeightedRetrieval() throws UIMAException, IOException {
     String[][][] tokens = { { { "NP" }, { "The", "DT", "the" },
         { "ATP/ADP-binding", "JJ", "atp/adp-binding" }, { "site", "NN", "site" }, { "" },
         { "PP" }, { "in", "IN", "in" }, { "" }, { "NP" }, { "the", "DT", "the" },
         { "Hsp90", "NN", "hsp90" }, { "molecular", "JJ", "molecular" },
         { "chaperone", "NN", "chaperone" }, { "" }, { ".", ".", "." } } };
     createEngine("[ NP DT_* ? ( . + ) site ] [ VP . * ] [ PP . * IN_* ] [ NP DT_* ? ( . + ) ]"
         + "\trel\tinteraction\tsem\tregulator\tsem\ttarget");
     // TODO: somehow, matching fails after the second DT has been found/matched!
     JCas doc = getJCas(tokens);
     engine.process(doc);
     JCas text = doc.getView(Views.CONTENT_TEXT.toString());
     FSIterator<Annotation> it = SemanticAnnotation.getIterator(text);
     String[] spans = { "ATP/ADP-binding", "Hsp90 molecular chaperone" };
     String[] semIds = { "regulator", "target" };
     int i = 0;
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       assertEquals(spans[i], ann.getCoveredText());
       assertEquals(semIds[i++], ann.getIdentifier());
     }
     assertEquals(spans.length, i);
   }
 }
