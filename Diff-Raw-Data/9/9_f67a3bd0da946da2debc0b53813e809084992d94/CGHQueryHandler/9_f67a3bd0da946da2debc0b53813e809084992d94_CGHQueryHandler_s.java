 package gov.nih.nci.nautilus.queryprocessing;
 
 import gov.nih.nci.nautilus.query.Query;
 import gov.nih.nci.nautilus.query.ComparativeGenomicQuery;
 
 
 import gov.nih.nci.nautilus.criteria.DiseaseOrGradeCriteria;
 import gov.nih.nci.nautilus.criteria.GeneIDCriteria;
 import gov.nih.nci.nautilus.criteria.CopyNumberCriteria;
 import gov.nih.nci.nautilus.criteria.RegionCriteria;
 import gov.nih.nci.nautilus.criteria.CloneOrProbeIDCriteria;
 import gov.nih.nci.nautilus.criteria.SNPCriteria;
 import gov.nih.nci.nautilus.criteria.AlleleFrequencyCriteria;
 import gov.nih.nci.nautilus.criteria.AssayPlatformCriteria;
 
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: BhattarR
  * Date: Aug 20, 2004
  * Time: 3:14:46 PM
  * To change this template use Options | File Templates.
  */
public class CGHQueryHandler implements QueryHandler {
 
 	DiseaseOrGradeCriteria diseaseOrGradeCrit;
 	GeneIDCriteria geneIDCrit;
 	CopyNumberCriteria copyNumberCrit;
 	RegionCriteria regionCrit;
 	CloneOrProbeIDCriteria cloneOrProbeIDCrit;
 	SNPCriteria snpCrit;
 	AlleleFrequencyCriteria alleleFrequencyCrit;
     AssayPlatformCriteria assayPlatformCrit;
 
 
 
    public void handle(Query query) {
         ComparativeGenomicQuery cghQuery = (ComparativeGenomicQuery) query;
 
         diseaseOrGradeCrit = cghQuery.getDiseaseOrGradeCriteria();
         geneIDCrit = cghQuery.getGeneIDCriteria();
         copyNumberCrit = cghQuery.getCopyNumberCriteria();
         regionCrit = cghQuery.getRegionCriteria();
         cloneOrProbeIDCrit = cghQuery.getCloneOrProbeIDCriteria();
         snpCrit = cghQuery.getSNPCriteria();
         alleleFrequencyCrit = cghQuery.getAlleleFrequencyCriteria();
         assayPlatformCrit = cghQuery.getAssayPlatformCriteria();
 
         Collection geneIdDEs = geneIDCrit.getGeneIdentifiers();
         for (Iterator iterator = geneIdDEs.iterator(); iterator.hasNext();) {
             GeneIdentifierDE o = (GeneIdentifierDE) iterator.next();
             if (o instanceof GeneIdentifierDE.LocusLink) {
                 System.out.println("LocuLink: " + o.getValueObject());
             }
         }
     }
 }
