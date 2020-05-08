 package gov.nih.nci.nautilus.queryprocessing.ge;
 
 import gov.nih.nci.nautilus.criteria.GeneIDCriteria;
import gov.nih.nci.nautilus.data.GeneClone;
import gov.nih.nci.nautilus.data.GeneSnp;
import gov.nih.nci.nautilus.data.LlSnp;
import gov.nih.nci.nautilus.data.ProbesetDim;
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 import gov.nih.nci.nautilus.queryprocessing.QueryHandler;
 import gov.nih.nci.nautilus.queryprocessing.cgh.CGHReporterIDCriteria;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.QueryFactory;
 import org.apache.ojb.broker.query.ReportQueryByCriteria;
 
 /**
  * @author BhattarR
  */
 public class GeneIDCriteriaHandler {
      private final static int VALUES_PER_THREAD = 200;
      static HashMap data = new HashMap();
         static {
             data.put(GeneIdentifierDE.GeneSymbol.class.getName(),
                             new TargetClass(GeneSnp.class, GeneSnp.SNP_PROBESET_ID));
             data.put(GeneIdentifierDE.LocusLink.class.getName(),
                             new TargetClass(LlSnp.class, LlSnp.SNP_PROBESET_ID));
             data.put(GeneIdentifierDE.GenBankAccessionNumber.class.getName(),
                            new TargetClass(GeneSnp.class, GeneSnp.SNP_PROBESET_ID));
         }
 
      static ArrayList getGeneIDValues(GeneIDCriteria geneIDCrit) {
         Collection geneIdDEs = geneIDCrit.getGeneIdentifiers();
         ArrayList geneIDs = new ArrayList();
         for (Iterator iterator = geneIdDEs.iterator(); iterator.hasNext();) {
             GeneIdentifierDE obj  = (GeneIdentifierDE) iterator.next();
             String value = null;
             if (obj.getGeneIDType().equals(GeneIdentifierDE.GENESYMBOL))
                value = obj.getValueObject().toUpperCase();
             else value = obj.getValueObject();
             geneIDs.add(value);
         }
         return geneIDs;
     }
 
 
     public static CGHReporterIDCriteria  buildReporterIDCritForCGHQuery(GeneIDCriteria  geneIDCrit, boolean includeSNPs, boolean includeCGH, PersistenceBroker pb) throws Exception {
             Class deClass = getGeneIDClassName(geneIDCrit);
             ArrayList geneIDs = getGeneIDValues(geneIDCrit);
             CGHReporterIDCriteria  snpProbeIDCrit = new CGHReporterIDCriteria ();
 
             if ( includeSNPs) {
                 TargetClass t = (TargetClass) data.get(deClass.getName());
                 snpProbeIDCrit = buildGeneSymbolCrit(deClass, geneIDs, t.target, t.attrToSearch);
             }
             if (includeCGH)  {
                 // TODO: Post Nautilus
             }
             return snpProbeIDCrit;
     }
     private static class  TargetClass {
         Class target;
         String attrToSearch;
         public TargetClass(Class targetClass, String attrToSearch) {
             this.target = targetClass;
             this.attrToSearch = attrToSearch;
         }
     }
 
     private static CGHReporterIDCriteria buildGeneSymbolCrit(Class deClass, ArrayList geneIDs, Class targetClass, String attrToRetrieve) throws Exception {
         CGHReporterIDCriteria snpProbeIDCrit = new CGHReporterIDCriteria();
         String geneIDCol = QueryHandler.getAttrNameForTheDE(deClass.getName(), targetClass.getName());
         Criteria snpProbesetIDCrit = new  Criteria();
         snpProbesetIDCrit.addIn(geneIDCol, geneIDs);
         //String snpProbeIDCol = QueryHandler.getColumnNameForBean(pb, GeneSnp.class.getName(), );
         ReportQueryByCriteria snpSubQuery = QueryFactory.newReportQuery(targetClass,snpProbesetIDCrit, true );
         snpSubQuery.setAttributes(new String[] {attrToRetrieve}) ;
         snpProbeIDCrit.setSnpProbeIDsSubQuery(snpSubQuery);
         return  snpProbeIDCrit;
     }
 
     public static GEReporterIDCriteria buildReporterIDCritForGEQuery(GeneIDCriteria  geneIDCrit, boolean includeClones, boolean includeProbes) throws Exception {
         final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
         pb.clearCache();
         Class deClass = getGeneIDClassName(geneIDCrit);
         ArrayList arrayIDs = getGeneIDValues(geneIDCrit);
         GEReporterIDCriteria cloneIDProbeIDCrit = new GEReporterIDCriteria();
 
          for (int i = 0; i < arrayIDs.size();) {
                 Collection geneIDValues = new ArrayList();
                 int begIndex = i;
                 i += VALUES_PER_THREAD ;
                 int endIndex = (i < arrayIDs.size()) ? endIndex = i : (arrayIDs.size());
                 geneIDValues.addAll(arrayIDs.subList(begIndex,  endIndex));
                 if ( includeProbes) {
                     String probeIDColumn = QueryHandler.getColumnNameForBean(pb, ProbesetDim.class.getName(), ProbesetDim.PROBESET_ID);
                     String deMappingAttrName = QueryHandler.getAttrNameForTheDE(deClass.getName(), ProbesetDim.class.getName());
                     Criteria c = new Criteria();
                     c.addIn(deMappingAttrName, geneIDValues);
                     ReportQueryByCriteria probeIDSubQuery = QueryFactory.newReportQuery(ProbesetDim.class, new String[] {probeIDColumn}, c, true );
                     cloneIDProbeIDCrit.getMultipleProbeIDsSubQueries().add(probeIDSubQuery);
                 }
                 if (includeClones) {
                      String cloneIDColumn = QueryHandler.getColumnNameForBean(pb, GeneClone.class.getName(), GeneClone.CLONE_ID);
                      String deMappingAttrName = QueryHandler.getAttrNameForTheDE(deClass.getName(), GeneClone.class.getName());
                      Criteria c = new Criteria();
                      c.addIn(deMappingAttrName, geneIDValues);
                      ReportQueryByCriteria cloneIDSubQuery = QueryFactory.newReportQuery(GeneClone.class, new String[] {cloneIDColumn}, c, true );
                      cloneIDProbeIDCrit.getMultipleCloneIDsSubQueries().add(cloneIDSubQuery);
                 }
         }
 
         /*  BEFORE MULITTHREADING
         if ( includeProbes) {
             String probeIDColumn = QueryHandler.getColumnNameForBean(pb, ProbesetDim.class.getName(), ProbesetDim.PROBESET_ID);
             String deMappingAttrName = QueryHandler.getAttrNameForTheDE(deClass.getName(), ProbesetDim.class.getName());
             Criteria c = new Criteria();
             c.addIn(deMappingAttrName, geneIDs);
             ReportQueryByCriteria probeIDSubQuery = QueryFactory.newReportQuery(ProbesetDim.class, new String[] {probeIDColumn}, c, true );
             cloneIDProbeIDCrit.setProbeIDsSubQuery(probeIDSubQuery);
         }
         if (includeClones) {
             String cloneIDColumn = QueryHandler.getColumnNameForBean(pb, GeneClone.class.getName(), GeneClone.CLONE_ID);
             String deMappingAttrName = QueryHandler.getAttrNameForTheDE(deClass.getName(), GeneClone.class.getName());
             Criteria c = new Criteria();
             c.addIn(deMappingAttrName, geneIDs);
             ReportQueryByCriteria cloneIDSubQuery = QueryFactory.newReportQuery(GeneClone.class, new String[] {cloneIDColumn}, c, true );
             cloneIDProbeIDCrit.setCloneIDsSubQuery(cloneIDSubQuery);
         }
         */
        	pb.close(); // Release broker instance to the broker-pool
         return cloneIDProbeIDCrit;
     }
 
 
     static Class getGeneIDClassName(GeneIDCriteria geneIDCrit ) {
             Collection geneIDs = geneIDCrit.getGeneIdentifiers();
             GeneIdentifierDE obj =  (GeneIdentifierDE) geneIDs.iterator().next();
             return obj.getClass();
     }
 }
