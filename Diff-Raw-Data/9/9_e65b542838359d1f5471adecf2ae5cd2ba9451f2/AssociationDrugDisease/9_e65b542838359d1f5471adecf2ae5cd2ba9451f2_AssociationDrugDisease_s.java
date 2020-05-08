 /**
  * 
  */
 package consumers;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.AnalysisComponent;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 
 
 import analysis.Association;
 import analysis.AssociationReport;
 import bioentities.Disease;
 import bioentities.Drug;
 
 import textual_features.MetaInformation;
 
 
 /**
  * @author Samuel Croset
  *
  */
 public class AssociationDrugDisease extends JCasAnnotator_ImplBase {
 
     AssociationReport report;
     int numberOfCas = 0;
 
 
     /**
      * @see AnalysisComponent#initialize(UimaContext)
      */
     public void initialize(UimaContext aContext) throws ResourceInitializationException {
 
 	super.initialize(aContext);
 
 	//	String path = (String) aContext.getConfigParameterValue("DrugDictionaryPath");
 	report = new AssociationReport();
 
     }
 
     /**
      * @see JCasAnnotator_ImplBase#process(JCas)
      */
     public void process(JCas jcas) {
 
 	try {
 	    jcas = jcas.getView("text");
 	} catch (CASException e) {
 	    e.printStackTrace();
 	}
 
 	numberOfCas++;
 
 	FSIterator<Annotation> itpmid = jcas.getAnnotationIndex(MetaInformation.type).iterator();
 	FSIterator<Annotation> itDrug = jcas.getAnnotationIndex(Drug.type).iterator();
 	FSIterator<Annotation> itDisease = jcas.getAnnotationIndex(Disease.type).iterator();
 
 	String pmid = null;
 	if(itpmid.hasNext()){
 	    MetaInformation metaAnnot = (MetaInformation) itpmid.next();
 	    pmid = metaAnnot.getPmid();
 	}
 
 	ArrayList<Drug> drugs = new ArrayList<Drug>();
 	while(itDrug.hasNext()){
 	    Drug drugAnnot = (Drug) itDrug.next();
 	    drugs.add(drugAnnot);
 	}
 
 	ArrayList<Disease> diseases = new ArrayList<Disease>();
 	while(itDisease.hasNext()){
 	    Disease diseaseAnnot = (Disease) itDisease.next();
 	    diseases.add(diseaseAnnot);
 	}
 
 	for (Drug drug : drugs) {
 	    for (Disease disease : diseases) {		
 		report.addAssociation(drug.getName(), disease.getName(), pmid);
 	    }
 	}
 
     }
 
     /* (non-Javadoc)
      * @see org.apache.uima.analysis_component.AnalysisComponent_ImplBase#collectionProcessComplete()
      */
     @Override
     public void collectionProcessComplete()
     throws AnalysisEngineProcessException {
 
 	FileWriter out = null;
 	try {
	    out = new FileWriter("data/report.txt");
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 	BufferedWriter bw = new BufferedWriter(out);
 
 	System.out.println("printing report...");
 
 	try {
 	    bw.write("===Number of document: " + numberOfCas + "===\n");
 	} catch (IOException e1) {
 	    e1.printStackTrace();
 	}
 
 	for (Association association : report.getAssociations()) {
 	    try {
 		bw.write(association.getDrug() + " associated_with " + association.getDisease() + " --> " + association.getNumberOfObservation() + "\n");
 	    } catch (IOException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	try {
 	    bw.close();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 
 	System.out.println("collection analyzed");
     }
 }
