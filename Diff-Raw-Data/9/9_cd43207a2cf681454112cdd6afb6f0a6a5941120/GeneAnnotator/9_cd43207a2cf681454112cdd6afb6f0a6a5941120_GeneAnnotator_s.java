 package txtfnnl.uima.analysis_component;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSArray;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ExternalResourceDescription;
 
 import txtfnnl.uima.Views;
 import txtfnnl.uima.cas.Property;
 import txtfnnl.uima.resource.GnamedGazetteerResource;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 import txtfnnl.uima.tcas.TokenAnnotation;
 import txtfnnl.utils.Offset;
 
 /**
  * A system to create normalized (mapped) annotations of gene names. This AE requires a
  * {@link GnamedGazetteerResource} with gene <i>names</i> mapped to their <i>gene <b>and</b> taxon
  * IDs</i>.
  * <p>
  * The taxon ID is set as a property with the name defined by {@link GeneAnnotator#TAX_ID_PROPERTY}
  * . Furthermore, all Greek letters are replaced with their Latin equivalents to ensure alternative
  * spellings are matched, too. Other than that, this annotator works just as any other
  * {@link GazetteerAnnotator}.
  * 
  * @see GazetteerAnnotator
  * @author Florian Leitner
  */
 public class GeneAnnotator extends GazetteerAnnotator {
   /** The URI of this Annotator (namespace and ID are defined dynamically). */
   @SuppressWarnings("hiding")
   public static final String URI = GeneAnnotator.class.getName();
   /** The name of the property used to set the taxon ID of the matched gene name. */
   public static final String TAX_ID_PROPERTY = "taxId";
   /**
    * Greek alphabet, uppercase: alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota, kappa,
    * lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega.
    */
   private static final char[] GREEK_UPPER = new char[] { '\u0391', '\u0392', '\u0393', '\u0394',
       '\u0395', '\u0396', '\u0397', '\u0398', '\u0399', '\u039A', '\u039B', '\u039C', '\u039D',
       '\u039E', '\u039F', '\u03A0', '\u03A1', '\u03A3', '\u03A4', '\u03A5', '\u03A6', '\u03A7',
       '\u03A8', '\u03A9' };
   /** Corresponding Latin uppercase letters for the Greek uppercase letters. */
   private static final char[] LATIN_UPPER = new char[] { 'A', 'B', 'C', 'D', 'E', 'Z', 'H', 'Q',
       'I', 'K', 'L', 'M', 'N', 'G', 'O', 'P', 'R', 'S', 'T', 'Y', 'F', 'X', 'U', 'W' };
   /**
    * Greek alphabet, lowercase: alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota, kappa,
    * lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega.
    */
   private static final char[] GREEK_LOWER = new char[] { '\u03B1', '\u03B2', '\u03B3', '\u03B4',
       '\u03B5', '\u03B6', '\u03B7', '\u03B8', '\u03B9', '\u03BA', '\u03BB', '\u03BC', '\u03BD',
       '\u03BE', '\u03BF', '\u03C0', '\u03C1', '\u03C3', '\u03C4', '\u03C5', '\u03C6', '\u03C7',
       '\u03C8', '\u03C9' };
   /** Corresponding Latin lowercase letters for the Greek lowercase letters. */
   private static final char[] LATIN_LOWER = new char[] { 'a', 'b', 'c', 'd', 'e', 'z', 'h', 'q',
       'i', 'k', 'l', 'm', 'n', 'g', 'o', 'p', 'r', 's', 't', 'y', 'f', 'x', 'u', 'w' };
   private static final char FIRST_GREEK = '\u0391';
   private static final char LAST_GREEK = '\u03C9';
   private Map<String, String> normalizedGreekLetters = new HashMap<String, String>();
 
   public static class Builder extends GazetteerAnnotator.Builder {
     Builder(String entityNamespace, ExternalResourceDescription geneGazetteerResourceDescription) {
       super(GeneAnnotator.class, entityNamespace, geneGazetteerResourceDescription);
     }
   }
 
   /**
    * Create a new gazetteer configuration builder with a pre-configured gazetteer resource.
    * 
    * @param entityNamespace to use for the {@link SemanticAnnotation SemanticAnnotations} of the
    *        entity DB IDs
    * @param geneGazetteerResourceDescription a pre-configured {@link GeneGazetteerResource}
    *        description.
    */
   public static Builder configure(String entityNamespace,
       ExternalResourceDescription geneGazetteerResourceDescription) {
     return new Builder(entityNamespace, geneGazetteerResourceDescription);
   }
 
   @Override
   public void process(JCas jcas) throws AnalysisEngineProcessException {
     // TODO: use default view
     String text = null;
     try {
       text = jcas.getView(Views.CONTENT_TEXT.toString()).getDocumentText();
     } catch (final CASException e) {
       throw new AnalysisEngineProcessException(e);
     }
     normalizedGreekLetters.put(text, replaceGreekLetters(text));
     super.process(jcas);
     normalizedGreekLetters.remove(text);
   }
 
   @Override
   protected Map<Offset, Set<String>> matchDocument(JCas jcas) {
     String text = jcas.getDocumentText();
     Map<Offset, Set<String>> matches = gazetteer.match(text);
     text = normalizedGreekLetters.get(text);
     if (text != null) {
       Map<Offset, Set<String>> more = gazetteer.match(text);
       for (Offset off : more.keySet()) {
         if (matches.containsKey(off)) matches.get(off).addAll(more.get(off));
         else matches.put(off, more.get(off));
       }
     }
     return matches;
   }
 
   @Override
   protected void findEntities(JCas jcas, Annotation annotation, List<SemanticAnnotation> buffer) {
     int begin = annotation.getBegin();
     int end = annotation.getEnd();
     String text = jcas.getDocumentText();
     FSIterator<Annotation> tokenIt = jcas.getAnnotationIndex(TokenAnnotation.type).subiterator(
         annotation);
     Map<Offset, Set<String>> matches = scan(jcas, tokenIt, text, begin, end, buffer);
     text = normalizedGreekLetters.get(text);
     if (text != null) {
       Map<Offset, Set<String>> more = scan(jcas, tokenIt, text, begin, end, buffer);
       for (Offset off : more.keySet()) {
         if (matches.containsKey(off)) matches.get(off).addAll(more.get(off));
         else matches.put(off, more.get(off));
       }
     }
     for (Offset offset : matches.keySet())
       buffer.addAll(annotateEntities(jcas, matches.get(offset), offset));
   }
 
   /**
    * Replace the string with all Greek letter replaced with Latin characters.
    * 
    * @param text to replace characters in
    * @return the replaced version or <code>null</code> if no Greek letter was found
    */
   private String replaceGreekLetters(String text) {
     boolean found = false;
     if (text.length() > 0) {
       char[] result = text.toCharArray();
       for (int idx = result.length - 1; idx > -1; --idx) {
         if (result[idx] >= FIRST_GREEK && result[idx] <= LAST_GREEK) {
           found = replace(GREEK_LOWER, LATIN_LOWER, result, idx) || found;
           found = replace(GREEK_UPPER, LATIN_UPPER, result, idx) || found;
         }
       }
       if (found) return new String(result);
     }
     return null;
   }
 
   /**
    * If a char in <code>greek</code> matches the char at <code>target[idx]</code>, replace that
    * char in <code>target</code> with the <code>latin</code> char at the same position as the
    * matching <code>greek</code> char.
    */
   private boolean replace(final char[] greek, final char[] latin, char[] target, int idx) {
     int pos = Arrays.binarySearch(greek, target[idx]);
    if (pos != -1) {
       target[idx] = latin[pos];
       return true;
     } else {
       return false;
     }
   }
 
   /** Expands the parent method, adding a taxon ID property to the annotation. */
   @Override
   protected SemanticAnnotation annotate(String id, JCas jcas, Offset offset, double confidence) {
     SemanticAnnotation entity = super.annotate(id, jcas, offset, confidence);
     entity.setAnnotator(URI); // update with static URI
     Property taxId = new Property(jcas);
     taxId.setName(TAX_ID_PROPERTY);
     taxId.setValue(((GnamedGazetteerResource) gazetteer).getTaxId(id));
     FSArray a = new FSArray(jcas, 1);
     a.set(0, taxId);
     entity.setProperties(a);
     return entity;
   }
 }
