 package drools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drools.builder.KnowledgeBuilderErrors;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.logger.KnowledgeRuntimeLogger;
 import org.drools.logger.KnowledgeRuntimeLoggerFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.QueryResultsRow;
 
 import pdfbox.Format;
 import weito.OutputPaper;
 import debug.Debug;
 import debug.Debug.DebugMode;
 import debug.Printer;
 
 public class Analysis {
 
 	KnowledgeBase kbase;
 	ArrayList<String> keywords;
 	private String fullText;
 
 	public OutputPaper analyse(List<Format> phrases, String inputLocation) throws Exception {
 
 		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
 		KnowledgeRuntimeLogger logger = null;
 		if( Debug.getMode().contains(DebugMode.DROOLSLOG) ) {
 			logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "mylog");
 		}
 
 		ksession = prepareSession(phrases, ksession);
 
 		try {
 			ksession.startProcess("featurealgorithm");
 			ksession.fireAllRules();
 			ksession.startProcess("catalgorithm");
 			ksession.fireAllRules();
 		} finally {
 			if( Debug.getMode().contains(DebugMode.DROOLSLOG) ) logger.close();
 		}
 
 		List<DocumentFeatureResult> results = new ArrayList<DocumentFeatureResult>();
 		List<String> ftfeatures = new ArrayList<String>();
 		ftfeatures.add(fullText);
 		results.add( new DocumentFeatureResult("Full Text", ftfeatures) );
 		
 		for( QueryResultsRow r : ksession.getQueryResults("Results") ) {
 			results.add( (DocumentFeatureResult) r.get("$feature") );
 		}
 		if(Debug.getMode().contains(DebugMode.FEATURE)) Printer.getInstance().printFeatureResults(results, inputLocation);
 		if(Debug.getMode().contains(DebugMode.FEATURESPEC)) Printer.getInstance().printFeatureSpecResults(results, inputLocation, keywords, phrases);
 		
 		
 		if(Debug.getMode().contains(DebugMode.KEYWORD)) Printer.getInstance().printKeywordResults(results, keywords, inputLocation);
 		OutputPaper paper = null;
 		for( QueryResultsRow r : ksession.getQueryResults("OutputPaper") ) {
 			paper = (OutputPaper) r.get("paper");
 
 		}
 		ksession.dispose();
 		return paper; //if no papers found
 	}
 
 
 	/**
 	 * @param phrases the phrases from the extracted document
 	 * @param ksession the session to be prepared - adding extra data from phrases
 	 * @return the prepared session
 	 */
 	private StatefulKnowledgeSession prepareSession(List<Format> phrases, StatefulKnowledgeSession ksession) {
 		double totalFontSize = 0;
 		fullText = "";
 		for(Format p : phrases) {
 			totalFontSize += p.getPrimarystyle().getfontSizePt();
 			fullText += p.getText();
 		}
 		double averageFontSize = totalFontSize / phrases.size();
 
 		ksession.setGlobal("stats", new DocumentStatistics(averageFontSize,fullText));
 		ksession.setGlobal("keywords", keywords);
 
 		for(Format p : phrases) {
 			ksession.insert(p);
 		}
 
 		return ksession;
 	}
 
 
 	public Analysis(List<AlgorithmContents> drlLocs, ArrayList<String> keywords) throws Exception {
 		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 		this.keywords = keywords;
 
 		for(AlgorithmContents a : drlLocs) {
 			kbuilder.add(a.getResource(), a.getResourcetype());
 
 		}
 
 		KnowledgeBuilderErrors errors = kbuilder.getErrors();
 		if (errors.size() > 0) {
			String errorstr = "";
 			for (KnowledgeBuilderError error: errors) {
				errorstr += ("\n"+ error);
 			}
			throw new IllegalArgumentException("Could not parse knowledge. Following Errors:" + errorstr);
 		}
 		kbase = KnowledgeBaseFactory.newKnowledgeBase();
 		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
 	}
 
 
 }
