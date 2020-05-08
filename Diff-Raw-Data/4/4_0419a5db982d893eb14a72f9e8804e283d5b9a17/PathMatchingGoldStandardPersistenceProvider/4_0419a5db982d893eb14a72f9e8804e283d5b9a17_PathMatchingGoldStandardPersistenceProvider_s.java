 package edu.cmu.lti.oaqa.framework.collection;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.regex.MatchResult;
 import java.util.regex.Pattern;
 
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceSpecifier;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 
 /**
  * A gold standard persistence provider that can read a file (or files) containing gold standard
  * annotations into the memory, and stored in a map structure, and populate gold standard labels for
  * each input element.
  * 
  * Required parameter: DataSet, LineSyntax (specifying what the line syntax of the gold standard
  * annotation, e.g. "(\d+)\s+(\d+)\s+(\d+)" represent the sequence id, begin and end are separated
  * by white-spaces), and PathPattern (refer to the PathMatchingResourcePatternResolver in the spring
  * framework for more detail)
  * 
  * @author Zi Yang <ziy@cs.cmu.edu>
  * 
  */
 public class PathMatchingGoldStandardPersistenceProvider extends
         AbstractGoldStandardPersistenceProvider {
 
   private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
 
   private Map<DatasetSequenceId, List<GoldStandardSpan>> id2gsSpans = new HashMap<DatasetSequenceId, List<GoldStandardSpan>>();
 
   @Override
   public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
           throws ResourceInitializationException {
     boolean ret = super.initialize(aSpecifier, aAdditionalParams);
     String dataset = (String) getParameterValue("DataSet");
     Pattern lineSyntaxPattern = Pattern.compile((String) getParameterValue("LineSyntax"));
     try {
       Resource[] resources = resolver.getResources((String) getParameterValue("PathPattern"));
       for (Resource resource : resources) {
         Scanner scanner = new Scanner(resource.getInputStream());
        while (scanner.nextLine() != null && scanner.findInLine(lineSyntaxPattern) != null) {
           MatchResult result = scanner.match();
           DatasetSequenceId id = new DatasetSequenceId(dataset, result.group(1));
           if (!id2gsSpans.containsKey(id)) {
             id2gsSpans.put(id, new ArrayList<GoldStandardSpan>());
           }
           GoldStandardSpan annotation = new GoldStandardSpan(Integer.parseInt(result.group(2)),
                   Integer.parseInt(result.group(3)));
           id2gsSpans.get(id).add(annotation);
         }
         scanner.close();
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
     return ret;
   }
 
   @Override
   public List<Annotation> populateGoldStandard(String dataset, String sequenceId, JCas gsView) {
     List<Annotation> gsAnnotations = new ArrayList<Annotation>();
     List<GoldStandardSpan> gsSpans = id2gsSpans.get(new DatasetSequenceId(dataset, sequenceId));
     if (gsSpans != null) {
       for (GoldStandardSpan gsSpan : gsSpans) {
         gsAnnotations.add(new Annotation(gsView, gsSpan.begin, gsSpan.end));
       }
     }
     return gsAnnotations;
   }
 
   /**
    * A dataset, sequenceId pair used as the key of the GSProvider to populate gold-standards wrt
    * particular input.
    * 
    * @author Zi Yang <ziy@cs.cmu.edu>
    * 
    */
   public class DatasetSequenceId {
     String dataset;
 
     String sequenceId;
 
     public DatasetSequenceId(String dataset, String sequenceId) {
       super();
       this.dataset = dataset;
       this.sequenceId = sequenceId;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + getOuterType().hashCode();
       result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
       result = prime * result + ((sequenceId == null) ? 0 : sequenceId.hashCode());
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
       DatasetSequenceId other = (DatasetSequenceId) obj;
       if (!getOuterType().equals(other.getOuterType()))
         return false;
       if (dataset == null) {
         if (other.dataset != null)
           return false;
       } else if (!dataset.equals(other.dataset))
         return false;
       if (sequenceId == null) {
         if (other.sequenceId != null)
           return false;
       } else if (!sequenceId.equals(other.sequenceId))
         return false;
       return true;
     }
 
     private PathMatchingGoldStandardPersistenceProvider getOuterType() {
       return PathMatchingGoldStandardPersistenceProvider.this;
     }
 
   }
 
   /**
    * Equivalent to Annotation, without the requirement to specify a CAS for the annotation, since
    * during initialize(), the CAS is still not clear to the pipeline.
    * 
    * @author Zi Yang <ziy@cs.cmu.edu>
    * 
    */
   public class GoldStandardSpan {
     int begin, end;
 
     public GoldStandardSpan(int begin, int end) {
       super();
       this.begin = begin;
       this.end = end;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + getOuterType().hashCode();
       result = prime * result + begin;
       result = prime * result + end;
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
       GoldStandardSpan other = (GoldStandardSpan) obj;
       if (!getOuterType().equals(other.getOuterType()))
         return false;
       if (begin != other.begin)
         return false;
       if (end != other.end)
         return false;
       return true;
     }
 
     private PathMatchingGoldStandardPersistenceProvider getOuterType() {
       return PathMatchingGoldStandardPersistenceProvider.this;
     }
 
   }
 
 }
