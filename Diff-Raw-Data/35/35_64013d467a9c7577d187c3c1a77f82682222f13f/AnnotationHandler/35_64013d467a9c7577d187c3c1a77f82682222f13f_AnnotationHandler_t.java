 package gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.annotations;
 
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.QueryFactory;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.DBEvent;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ThreadController;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.rembrandt.dbbean.ProbesetDim;
 import gov.nih.nci.rembrandt.dbbean.UnifiedHugoGene;
 import gov.nih.nci.rembrandt.util.ThreadPool;
 
 /**
  * User: Ram Bhattaru <BR>
  * Date: Feb 1, 2006 <BR>
  * Version: 1.0 <BR>
  */
 public class AnnotationHandler {
     private static Logger logger = Logger.getLogger(AnnotationHandler.class);
     private final static int VALUES_PER_THREAD = 50;
     private static  Map<String, List<String>> unifiedReporterGeneMap = null; 
     private static  Map<String, List<String>> affyReporterGeneMap = null; 
     private static  Map<String, ReporterAnnotations> allAffyReportAnnotationMap = null;
     private static  Map<String, String> affyReporterGeneSymbolMap = null;
 
     /**  This method will return the reporters and corresponding gene symbol in a Hash Map
      *
      * @param reporters Reporters to be queried
      * @return This method retunrs a Map using reporter as key and corresponding gene symbols as value
      */
     @SuppressWarnings("unchecked")
 	public static Map<String, String> getGeneSymbolsFor(List reporters, ArrayPlatformType arrayPlatformtype) throws Exception{
         List annotationsEventList = Collections.synchronizedList(new ArrayList());
         boolean isReporterMissing = false;
         switch( arrayPlatformtype){
         case AFFY_OLIGO_PLATFORM:{
 	    	if(affyReporterGeneSymbolMap != null){
 	    		for(Object obj:reporters){
 	    			String reporterName = (String)obj;
 	    			if(!affyReporterGeneSymbolMap.containsKey(reporterName)){
 	    				isReporterMissing = true;
 	    			}
 	    		}
 	    	}
 	    	else{ //reporterGeneSymbolMap is null the first time
 	    		affyReporterGeneSymbolMap = new HashMap<String, String>();
 	    		isReporterMissing = true;
 	    	}
 	
 	    	if(isReporterMissing){
 	    		
 	    		affyReporterGeneSymbolMap = execQueryGeneSymbols(reporters, annotationsEventList);
 		        try {
 		            ThreadController.sleepOnEvents(annotationsEventList);
 		        } catch (InterruptedException e) {
 		            // no big deal Log it and ignore it
 		            logger.debug("Thread Interrupted during Annotations Retrieval", e);
 		        }
 	    	}
 		        return affyReporterGeneSymbolMap;
         }        
         }
         return Collections.EMPTY_MAP;
     }
 
      /**
      * This method retrieves GenePathways, GeneOntologies, LocusLinks & Accessions
      * Modified to use lazy fetch
      * @param reporters ReporterNames for which Annotations are requested
      * @return Returns Map in which ReporterName is the key and associated ReporterAnnotations
      * as the value
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
 	public static Map<String, ReporterAnnotations> getAllAnnotationsFor(List reporters, ArrayPlatformType arrayPlatformtype) throws Exception{
     	boolean isReporterMissing = false;
         switch( arrayPlatformtype){
         case AFFY_OLIGO_PLATFORM:{
 		    	if(allAffyReportAnnotationMap != null){
 		    		for(Object obj:reporters){
		    			String reporterName = "";
		    			if(obj instanceof Long){
		    				Long reporter = (Long)obj;
		    				reporterName = reporter.toString();
		    			}else if(obj instanceof String){
		    				reporterName = (String) obj;
		    			}
 		    			if(!allAffyReportAnnotationMap.containsKey(reporterName)){
 		    				isReporterMissing = true;
 		    			}
 		    		}
 		    	}
 		    	else{ //allReportAnnotationMap is null the first time
 		    		allAffyReportAnnotationMap = new HashMap<String, ReporterAnnotations> ();
 		    		isReporterMissing = true;
 		    	}
 		
 		    	if(isReporterMissing){
 			        for (Iterator iterator = reporters.iterator(); iterator.hasNext();) {
 			            String repName =  (String) iterator.next();
 			            // create a ReporterAnnotations object for each of the reporters
 			            ReporterAnnotations ra = new ReporterAnnotations();
 			            ra.setReporterName(repName);
 			            allAffyReportAnnotationMap.put(repName, ra);
 			        }
 			
 			        // 1.0 Retrieve all GeneSymbols corresponding to these reporters
 			        Map<String, String> reportersAndGenes = getGeneSymbolsFor(reporters,ArrayPlatformType.AFFY_OLIGO_PLATFORM );
 			        // 1.1 pupulate the above ReporterAnnotations object with the corresponding genesymbols
 			        for (Iterator iterator = reporters.iterator(); iterator.hasNext();) {
 			            String repName =  (String) iterator.next();
 			            ReporterAnnotations ra = allAffyReportAnnotationMap.get(repName);
 			            ra.setGeneSymbol(reportersAndGenes.get(repName));
 			        }
 			
 			
 			
 			        // 2.0 Retrieve GeneAnnotations (GenePathways & GeneOntology) for these gene symbols
 			        Collection geneSymbols = reportersAndGenes.values();
 			        List geneAnnotEventList = Collections.synchronizedList(new ArrayList());
 			        Map<String, GeneExpr.GeneAnnotation> geneAnnotations = Collections.synchronizedMap(new HashMap());
 			        GeneAnnotationsHandler gh = new GeneAnnotationsHandler(geneAnnotEventList );
 			        gh.populateAnnotationsFor(geneSymbols, geneAnnotations);
 			
 			
 			        // 3.0 Retrieve ReporterDimension (LocusLinks & Accns) for these reporters
 			        List reporterAnnotEventList = Collections.synchronizedList(new ArrayList());
 			        ReporterAnnotationsHandler rh = new ReporterAnnotationsHandler();
 			        Map<String,ReporterDimension> repAnnotations = rh.execAnnotationQuery(reporters, reporterAnnotEventList) ;
 			
 			        // 4.0 sleep until the above GeneAnnotations & ReporterDimension are fully retrieved
 			        try {
 			            ThreadController.sleepOnEvents(geneAnnotEventList);
 			            ThreadController.sleepOnEvents(reporterAnnotEventList);
 			        } catch (InterruptedException e) {
 			            // no big deal Log it and ignore it
 			            logger.debug("Thread Interrupted during Annotations Retrieval", e);
 			        }
 			
 			        // 5.0 populate the above ReporterAnnotations object with the corresponding GeneAnnotations
 			        for (Iterator iterator = reporters.iterator(); iterator.hasNext();) {
 			            String repName =  (String) iterator.next();
 			            ReporterAnnotations ra = allAffyReportAnnotationMap.get(repName);
 			            String gs = ra.getGeneSymbol();
 			
 			            // set goIDS & pathways ONLY if Gene Annotations are aivailable annotations
 			            GeneExpr.GeneAnnotation ga = geneAnnotations.get(gs);
 			            if (ga != null)  {
 			                ra.setGoIDS(ga.getGoIDs());
 			                ra.setPathways(ga.getPathwayNames());
 			            }
 			        }
 			
 			        // 6.0 populate the above ReporterAnnotations object with the corresponding ReporetrDimensions
 			        for (Iterator iterator = reporters.iterator(); iterator.hasNext();) {
 			            String repName =  (String) iterator.next();
 			            ReporterAnnotations ra = allAffyReportAnnotationMap.get(repName);
 			
 			            // set LocusLinks & goIDs ONLY if ReporterAnnotations are available
 			            ReporterDimension rd = repAnnotations.get(ra.getReporterName());
 			            if (rd != null) {
 			                ra.setLocusLinks(rd.getLocusLinks());
 			                ra.setAccessions(rd.getAccessions());
 			            }
 			        }
 		    	}
 		        return allAffyReportAnnotationMap;
 	        }
         }
         return Collections.EMPTY_MAP;
     }
 
     @SuppressWarnings("unchecked")
 	private static Map<String, String> execQueryGeneSymbols(List reporters, List annotationsEventList) {
         final Map<String, String> genesAndReporters = Collections.synchronizedMap(new HashMap<String, String>());
         for (int i = 0; i < reporters.size();)   {
             Collection values = new ArrayList();
             int begIndex = i;
             i += VALUES_PER_THREAD ;
             int endIndex = (i < reporters.size()) ? endIndex = i : (reporters.size());
             values.addAll(reporters.subList(begIndex,  endIndex));
             final Criteria reporterCrit = new Criteria();
             reporterCrit.addIn(ProbesetDim.PROBESET_NAME, values);
             String threadID = "AnnotationHandler.ThreadID:" +i;
 
             final DBEvent.FactRetrieveEvent dbEvent = new DBEvent.FactRetrieveEvent(threadID);
             annotationsEventList.add(dbEvent);
 
             ThreadPool.AppThread t =
                     ThreadPool.newAppThread(
                        new ThreadPool.MyRunnable() {
                           public void codeToRun() {
                               // retrieve GeneSymbols for the probesetNames above
                               final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                               pb.clearCache();
                               org.apache.ojb.broker.query.Query annotQuery =
                               QueryFactory.newReportQuery(ProbesetDim.class,new String[] {ProbesetDim.PROBESET_NAME, ProbesetDim.GENE_SYMBOL}, reporterCrit, false);
                               assert(annotQuery  != null);
                               Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
 
                               while (iter.hasNext()) {
                                    Object[] geneAttrs = (Object[]) iter.next();
                                    String reporter = (String)geneAttrs[0];
                                    String geneSymbol = (String)geneAttrs[1];
                                    genesAndReporters.put(reporter, geneSymbol);
                               }
                               pb.close();
                               dbEvent.setCompleted(true);
                           }
                        }
                     );
                logger.debug("BEGIN: (from AnnotationHandler.getGeneSymbolsFor()) Thread Count: " + ThreadPool.THREAD_COUNT);
                t.start();
          }
          return genesAndReporters;
     }
     @SuppressWarnings("unchecked")
 	private Map<String, List<String>> execQueryAffyProbeSets(List<String> geneSymbols, List annotationsEventList) {
         final Map<String, List<String>> reporterGeneMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
         for (int i = 0; i < geneSymbols.size();)   {
             Collection values = new ArrayList();
             int begIndex = i;
             i += VALUES_PER_THREAD ;
             int endIndex = (i < geneSymbols.size()) ? endIndex = i : (geneSymbols.size());
             values.addAll(geneSymbols.subList(begIndex,  endIndex));
             final Criteria reporterCrit = new Criteria();
             reporterCrit.addIn(ProbesetDim.GENE_SYMBOL, values);
             String threadID = "AnnotationHandler.ThreadID:" +i;
 
             final DBEvent.FactRetrieveEvent dbEvent = new DBEvent.FactRetrieveEvent(threadID);
             annotationsEventList.add(dbEvent);
 
             ThreadPool.AppThread t =
                     ThreadPool.newAppThread(
                        new ThreadPool.MyRunnable() {
                           public void codeToRun() {
                               // retrieve GeneSymbols for the probesetNames above
                               final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                               pb.clearCache();
                               org.apache.ojb.broker.query.Query annotQuery =
                               QueryFactory.newReportQuery(ProbesetDim.class,new String[] {ProbesetDim.PROBESET_NAME, ProbesetDim.GENE_SYMBOL}, reporterCrit, false);
                               assert(annotQuery  != null);
                               Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
 
                               while (iter.hasNext()) {
                                    Object[] geneAttrs = (Object[]) iter.next();
                                    String reporter = (String)geneAttrs[0];
                                    String geneSymbol = (String)geneAttrs[1];
                                    if (reporterGeneMap.containsKey(geneSymbol)){
                                 	  List<String> reporters =  reporterGeneMap.get(geneSymbol);
                                 	  reporters.add(reporter);
                                 	  reporterGeneMap.put(geneSymbol,reporters);    
                                    }
                                    else{
                                 	   List<String> reporters = new ArrayList<String>();
                                 	   reporters.add(reporter);
                                 	   reporterGeneMap.put(geneSymbol,reporters);                                	   
                                    }
                               }
                               pb.close();
                               dbEvent.setCompleted(true);
                           }
                        }
                     );
                logger.debug("BEGIN: (from AnnotationHandler.getGeneSymbolsFor()) Thread Count: " + ThreadPool.THREAD_COUNT);
                t.start();
          }
          return reporterGeneMap;
     }
     @SuppressWarnings("unchecked")
 	private Map<String, List<String>> execQueryUnifiedGeneSymbols(List<String> geneSymbols, List annotationsEventList) {
         final Map<String, List<String>> reporterGeneMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
         for (int i = 0; i < geneSymbols.size();)   {
             Collection values = new ArrayList();
             int begIndex = i;
             i += VALUES_PER_THREAD ;
             int endIndex = (i < geneSymbols.size()) ? endIndex = i : (geneSymbols.size());
             values.addAll(geneSymbols.subList(begIndex,  endIndex));
             final Criteria reporterCrit = new Criteria();
             reporterCrit.addIn(UnifiedHugoGene.GENE_SYMBOL, values);
             String threadID = "AnnotationHandler.ThreadID:" +i;
 
             final DBEvent.FactRetrieveEvent dbEvent = new DBEvent.FactRetrieveEvent(threadID);
             annotationsEventList.add(dbEvent);
 
             ThreadPool.AppThread t =
                     ThreadPool.newAppThread(
                        new ThreadPool.MyRunnable() {
                           public void codeToRun() {
                               // retrieve GeneSymbols for the probesetNames above
                               final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                               pb.clearCache();
                               org.apache.ojb.broker.query.Query annotQuery =
                               QueryFactory.newReportQuery(UnifiedHugoGene.class,new String[] {UnifiedHugoGene.UNIFIED_GENE, UnifiedHugoGene.GENE_SYMBOL}, reporterCrit, false);
                               assert(annotQuery  != null);
                               Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
 
                               while (iter.hasNext()) {
                                    Object[] geneAttrs = (Object[]) iter.next();
                                    String reporter = (String)geneAttrs[0];
                                    String geneSymbol = (String)geneAttrs[1];
                                    if (reporterGeneMap.containsKey(geneSymbol)){
                                 	  List<String> reporters =  reporterGeneMap.get(geneSymbol);
                                 	  reporters.add(reporter);
                                 	  reporterGeneMap.put(geneSymbol,reporters);    
                                    }
                                    else{
                                 	   List<String> reporters = new ArrayList<String>();
                                 	   reporters.add(reporter);
                                 	   reporterGeneMap.put(geneSymbol,reporters);                                	   
                                    }
                               }
                               pb.close();
                               dbEvent.setCompleted(true);
                           }
                        }
                     );
                logger.debug("BEGIN: (from AnnotationHandler.getGeneSymbolsFor()) Thread Count: " + ThreadPool.THREAD_COUNT);
                t.start();
          }
          return reporterGeneMap;
     }
     @SuppressWarnings("unchecked")
 	private static Map<String, List<String>> execQueryUnifiedGeneSymbols() {
     	  Map<String, List<String>> reporterGeneMap = new HashMap<String, List<String>>();
           final Criteria reporterCrit = new Criteria();
           // retrieve GeneSymbols for the probesetNames above
           final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
           pb.clearCache();
           org.apache.ojb.broker.query.Query annotQuery =
           QueryFactory.newReportQuery(UnifiedHugoGene.class,new String[] {UnifiedHugoGene.UNIFIED_GENE, UnifiedHugoGene.GENE_SYMBOL}, reporterCrit, false);
           assert(annotQuery  != null);
           Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
 
           while (iter.hasNext()) {
                Object[] geneAttrs = (Object[]) iter.next();
                String reporter = (String)geneAttrs[0];
                String geneSymbol = (String)geneAttrs[1];
                if (reporterGeneMap.containsKey(geneSymbol)){
             	  List<String> reporters =  reporterGeneMap.get(geneSymbol);
             	  reporters.add(reporter);
             	  reporterGeneMap.put(geneSymbol,reporters);    
                }
                else{
             	   List<String> reporters = new ArrayList<String>();
             	   reporters.add(reporter);
             	   reporterGeneMap.put(geneSymbol,reporters);                                	   
                }
           }
           pb.close();
          return reporterGeneMap;
     }
     @SuppressWarnings("unchecked")
 	private static Map<String, List<String>> execQueryAffyProbeSets() {
     	  Map<String, List<String>> reporterGeneMap = new HashMap<String, List<String>>();
           final Criteria reporterCrit = new Criteria();
           // retrieve GeneSymbols for the probesetNames above
           final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
           pb.clearCache();
           org.apache.ojb.broker.query.Query annotQuery =
           QueryFactory.newReportQuery(ProbesetDim.class,new String[] {ProbesetDim.PROBESET_NAME, ProbesetDim.GENE_SYMBOL}, reporterCrit, false);
           assert(annotQuery  != null);
           Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
 
           while (iter.hasNext()) {
                Object[] geneAttrs = (Object[]) iter.next();
                String reporter = (String)geneAttrs[0];
                String geneSymbol = (String)geneAttrs[1];
                if (reporterGeneMap.containsKey(geneSymbol)){
             	  List<String> reporters =  reporterGeneMap.get(geneSymbol);
             	  reporters.add(reporter);
             	  reporterGeneMap.put(geneSymbol,reporters);    
                }
                else{
             	   List<String> reporters = new ArrayList<String>();
             	   reporters.add(reporter);
             	   reporterGeneMap.put(geneSymbol,reporters);                                	   
                }
           }
           pb.close();
          return reporterGeneMap;
     }
     /**  This method will return the affy probesets and corresponding gene symbols in a Hash Map
     *
     * @param geneSymbol Gene Symbols to be queried
     * @return This method retunrs a Map using geneSymbol as key and corresponding reporters as value
     */
     @SuppressWarnings("unchecked")
 	public static Map<String, List<String>> getAffyProbeSetsForGeneSymbols(List<String> geneSymbols) throws Exception{
     	if(affyReporterGeneMap == null){
     		affyReporterGeneMap = execQueryAffyProbeSets();
     	}
     	Map<String, List<String>> reporterGeneMap = new HashMap<String, List<String>>();
     	for(String geneSymbol :geneSymbols){
     		if (affyReporterGeneMap.containsKey(geneSymbol)){
           	  List<String> reporters =  affyReporterGeneMap.get(geneSymbol);
           	  reporterGeneMap.put(geneSymbol,reporters);    
              }
     	}
     	return reporterGeneMap;        
     }
 	public static List<String> getAllReporters(ArrayPlatformType arrayPlatformtype){
     	List<String> reporters = new ArrayList<String>();
     	switch(arrayPlatformtype){
     	case AFFY_OLIGO_PLATFORM:{
     		if(affyReporterGeneMap == null){
         		affyReporterGeneMap = execQueryAffyProbeSets();    		
         	}
     		if(affyReporterGeneSymbolMap == null){
     			affyReporterGeneSymbolMap = new HashMap<String,String>();
     		}
 			for(String geneSymbol:affyReporterGeneMap.keySet()){
 				List<String> myReporters = affyReporterGeneMap.get(geneSymbol);
 				reporters.addAll(myReporters);
 				for(String reporter:myReporters){
 					affyReporterGeneSymbolMap.put(reporter,geneSymbol);
 				}
     		}
 			return reporters;        	
     	}
     	case UNIFIED_GENE:
     		if(unifiedReporterGeneMap == null){
     			unifiedReporterGeneMap = execQueryUnifiedGeneSymbols();    		
         	}
 			for(String geneSymbol:unifiedReporterGeneMap.keySet()){
 				reporters.addAll(unifiedReporterGeneMap.get(geneSymbol));
     		}
 			return reporters;       
     	}
     	return reporters;
     }
     /**  This method will return the unifiedgene reporters and corresponding gene symbols in a Hash Map
     *
     * @param geneSymbol Gene Symbols to be queried
     * @return This method retunrs a Map using geneSymbol as key and corresponding reporters as value
     */
     @SuppressWarnings("unchecked")
 	public static Map<String, List<String>> getUnifiedGeneReportersForGeneSymbols(List<String> geneSymbols) throws Exception{
     	if(unifiedReporterGeneMap == null){
     		unifiedReporterGeneMap = execQueryUnifiedGeneSymbols();
     	}
     	Map<String, List<String>> reporterGeneMap = new HashMap<String, List<String>>();
     	for(String geneSymbol :geneSymbols){
     		if (unifiedReporterGeneMap.containsKey(geneSymbol)){
           	  List<String> reporters =  unifiedReporterGeneMap.get(geneSymbol);
           	  reporterGeneMap.put(geneSymbol,reporters);    
              }
     	}
     	return reporterGeneMap;        
     }
 
 
 }
