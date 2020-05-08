 package org.apache.ctakes.temporal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.ctakes.knowtator.KnowtatorAnnotation;
 import org.apache.ctakes.knowtator.KnowtatorXMLParser;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSArray;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.cleartk.util.ViewURIUtil;
 import org.jdom2.JDOMException;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.factory.AnalysisEngineFactory;
 
 import edu.mayo.bmi.uima.core.type.constants.CONST;
 import edu.mayo.bmi.uima.core.type.refsem.Event;
 import edu.mayo.bmi.uima.core.type.refsem.EventProperties;
 import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
 import edu.mayo.bmi.uima.core.type.refsem.UmlsConcept;
 import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
 import edu.mayo.bmi.uima.core.type.relation.RelationArgument;
 import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
 import edu.mayo.bmi.uima.core.type.textsem.EventMention;
 import edu.mayo.bmi.uima.core.type.textsem.TimeMention;
 
 public class KnowtatorXMLReader extends JCasAnnotator_ImplBase {
 
   public static AnalysisEngineDescription getDescription(File knowtatorXMLDirectory)
       throws ResourceInitializationException {
     return AnalysisEngineFactory.createPrimitiveDescription(
         KnowtatorXMLReader.class,
         KnowtatorXMLReader.PARAM_KNOWTATOR_XML_DIRECTORY,
         knowtatorXMLDirectory);
   }
 
   public static final String PARAM_KNOWTATOR_XML_DIRECTORY = "knowtatorXMLDirectory";
 
   @ConfigurationParameter(name = PARAM_KNOWTATOR_XML_DIRECTORY, mandatory = true)
   private File knowtatorXMLDirectory;
 
   @Override
   public void process(JCas jCas) throws AnalysisEngineProcessException {
     // determine Knowtator XML file from URI of CAS
     URI uri = ViewURIUtil.getURI(jCas);
     File file = new File(uri.getPath());
     String subDir = file.getParentFile().getName();
     Matcher matcher = Pattern.compile("^doc(\\d+)$").matcher(subDir);
     if (!matcher.matches()) {
       throw new IllegalArgumentException("Unrecognized subdirectory naming: " + subDir);
     }
     subDir = String.format("Set%02d", Integer.parseInt(matcher.group(1)));
     String fileName = file.getName() + ".knowtator.xml";
     File knowtatorFile = new File(new File(this.knowtatorXMLDirectory, subDir), fileName);
 
     // parse the Knowtator XML file into annotation objects
     KnowtatorXMLParser parser = new KnowtatorXMLParser(
         "consensus set annotator team",
         "consensus set_rel annotator team");
     Collection<KnowtatorAnnotation> annotations;
     try {
       annotations = parser.parse(knowtatorFile);
     } catch (JDOMException e) {
       throw new AnalysisEngineProcessException(e);
     } catch (IOException e) {
       throw new AnalysisEngineProcessException(e);
     }
 
     // mapping from entity types to their numeric constants
     Map<String, Integer> entityTypes = new HashMap<String, Integer>();
     entityTypes.put("Anatomical_site", CONST.NE_TYPE_ID_ANATOMICAL_SITE);
     entityTypes.put("Disease_Disorder", CONST.NE_TYPE_ID_DISORDER);
     entityTypes.put("Medications/Drugs", CONST.NE_TYPE_ID_DRUG);
     entityTypes.put("Procedure", CONST.NE_TYPE_ID_PROCEDURE);
     entityTypes.put("Sign_symptom", CONST.NE_TYPE_ID_FINDING);
 
     // create a CAS object for each annotation
     Map<String, Annotation> idMentionMap = new HashMap<String, Annotation>();
     List<KnowtatorRelation> relations = new ArrayList<KnowtatorRelation>();
     for (KnowtatorAnnotation annotation : annotations) {
       // copy the slots so we can remove them as we use them
       Map<String, String> stringSlots = new HashMap<String, String>(annotation.stringSlots);
       Map<String, Boolean> booleanSlots = new HashMap<String, Boolean>(annotation.booleanSlots);
       Map<String, KnowtatorAnnotation> annotationSlots = new HashMap<String, KnowtatorAnnotation>(
           annotation.annotationSlots);
       KnowtatorAnnotation.Span coveringSpan = annotation.getCoveringSpan();
 
       if (entityTypes.containsKey(annotation.type)) {
         // create the entity mention annotation
         EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
         entityMention.setTypeID(entityTypes.get(annotation.type));
         entityMention.setConfidence(1.0f);
         entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
 
         // convert negation to an integer
         Boolean negation = booleanSlots.remove("Negation");
         entityMention.setPolarity(negation == null ? +1 : negation == true ? -1 : +1);
 
         // convert status as necessary
         String status = stringSlots.remove("Status");
         if (status != null) {
           if ("HistoryOf".equals(status)) {
             // TODO
           } else if ("FamilyHistoryOf".equals(status)) {
             // TODO
           } else if ("Possible".equals(status)) {
             // TODO
           } else {
             throw new UnsupportedOperationException("Unknown status: " + status);
           }
         }
 
         // convert code to ontology concept or CUI
         String code = stringSlots.remove("AssociateCode");
         if (code == null) {
           code = stringSlots.remove("associatedCode");
         }
         OntologyConcept ontologyConcept;
         if (entityMention.getTypeID() == CONST.NE_TYPE_ID_DRUG) {
           ontologyConcept = new OntologyConcept(jCas);
           ontologyConcept.setCode(code);
         } else {
           UmlsConcept umlsConcept = new UmlsConcept(jCas);
           umlsConcept.setCui(code);
           ontologyConcept = umlsConcept;
         }
         ontologyConcept.addToIndexes();
         entityMention.setOntologyConceptArr(new FSArray(jCas, 1));
         entityMention.setOntologyConceptArr(0, ontologyConcept);
 
         // add entity mention to CAS
         entityMention.addToIndexes();
         idMentionMap.put(annotation.id, entityMention);
 
       } else if ("EVENT".equals(annotation.type)) {
 
         // collect the event properties
         EventProperties eventProperties = new EventProperties(jCas);
         eventProperties.setCategory(stringSlots.remove("type"));
         eventProperties.setContextualModality(stringSlots.remove("contextualmoduality"));
         eventProperties.setContextualAspect(stringSlots.remove("contextualaspect"));
         eventProperties.setDegree(stringSlots.remove("degree"));
         eventProperties.setDocTimeRel(stringSlots.remove("DocTimeRel"));
         eventProperties.setPermanence(stringSlots.remove("permanence"));
         String polarityStr = stringSlots.remove("polarity");
         int polarity;
         if (polarityStr == null || polarityStr.equals("POS")) {
           polarity = +1;
         } else if (polarityStr.equals("NEG")) {
           polarity = -1;
         } else {
           throw new IllegalArgumentException("Invalid polarity: " + polarityStr);
         }
         eventProperties.setPolarity(polarity);
 
         // create the event object
         Event event = new Event(jCas);
         event.setConfidence(1.0f);
         event.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
 
         // create the event mention
         EventMention eventMention = new EventMention(jCas, coveringSpan.begin, coveringSpan.end);
         eventMention.setConfidence(1.0f);
         eventMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
 
         // add the links between event, mention and properties
         event.setProperties(eventProperties);
         event.setMentions(new FSArray(jCas, 1));
         event.setMentions(0, eventMention);
         eventMention.setEvent(event);
 
         // add the annotations to the indexes
         eventProperties.addToIndexes();
         event.addToIndexes();
         eventMention.addToIndexes();
         idMentionMap.put(annotation.id, eventMention);
 
       } else if ("DOCTIME".equals(annotation.type)) {
         // TODO
 
       } else if ("SECTIONTIME".equals(annotation.type)) {
         // TODO
 
       } else if ("TIMEX3".equals(annotation.type)) {
         String timexClass = stringSlots.remove("class");
         TimeMention timeMention = new TimeMention(jCas, coveringSpan.begin, coveringSpan.end);
         timeMention.addToIndexes();
         idMentionMap.put(annotation.id, timeMention);
         // TODO
 
       } else if ("ALINK".equals(annotation.type)) {
         // store the ALINK information for later, once all annotations are in the CAS
         KnowtatorAnnotation source = annotationSlots.remove("Event");
         KnowtatorAnnotation target = annotationSlots.remove("related_to");
         String relationType = stringSlots.remove("Relationtype");
         relations.add(new KnowtatorRelation(annotation, source, target, relationType));
         // TODO: store "ALINK" somehow
 
       } else if ("TLINK".equals(annotation.type)) {
         // store the TLINK information for later, once all annotations are in the CAS
         KnowtatorAnnotation source = annotationSlots.remove("Event");
         KnowtatorAnnotation target = annotationSlots.remove("related_to");
         String relationType = stringSlots.remove("Relationtype");
         relations.add(new KnowtatorRelation(annotation, source, target, relationType));
         // TODO: store "TLINK" somehow
 
       } else {
         throw new IllegalArgumentException("Unrecognized type: " + annotation.type);
       }
 
       // make sure all slots have been consumed
       Set<String> remainingSlots = new HashSet<String>();
       remainingSlots.addAll(stringSlots.keySet());
       remainingSlots.addAll(booleanSlots.keySet());
       remainingSlots.addAll(annotationSlots.keySet());
       if (!remainingSlots.isEmpty()) {
         String format = "%s has unprocessed slot(s) %s";
         String message = String.format(format, annotation.type, remainingSlots);
         throw new UnsupportedOperationException(message);
       }
     }
 
     // all mentions should be added, so add the relations now
     for (KnowtatorRelation knowtatorRelation : relations) {
 
       // look up the relations in the map and issue an error if they're missing
       Annotation sourceMention = idMentionMap.get(knowtatorRelation.source.id);
       Annotation targetMention = idMentionMap.get(knowtatorRelation.target.id);
       String badId = null;
       if (sourceMention == null) {
         badId = knowtatorRelation.source.id;
       } else if (targetMention == null) {
         badId = knowtatorRelation.target.id;
       }
       if (badId != null) {
         String message = String.format("no annotation with id '%s'", badId);
         throw new UnsupportedOperationException(message);
       }
 
       // add the relation to the CAS
       RelationArgument sourceRA = new RelationArgument(jCas);
       sourceRA.setArgument(sourceMention);
       sourceRA.addToIndexes();
       RelationArgument targetRA = new RelationArgument(jCas);
       targetRA.setArgument(targetMention);
       targetRA.addToIndexes();
       BinaryTextRelation relation = new BinaryTextRelation(jCas);
      // TODO: do something better with knowtatorRelation.annotation.type
      relation.setCategory(knowtatorRelation.annotation.type + '_' + knowtatorRelation.type);
       relation.setArg1(sourceRA);
       relation.setArg2(targetRA);
       relation.addToIndexes();
     }
   }
 
   private static class KnowtatorRelation {
     public KnowtatorAnnotation annotation;
 
     public KnowtatorAnnotation source;
 
     public KnowtatorAnnotation target;
 
     public String type;
 
     public KnowtatorRelation(
         KnowtatorAnnotation annotation,
         KnowtatorAnnotation source,
         KnowtatorAnnotation target,
         String relationType) {
       this.annotation = annotation;
       this.source = source;
       this.target = target;
       this.type = relationType;
     }
   }
 }
