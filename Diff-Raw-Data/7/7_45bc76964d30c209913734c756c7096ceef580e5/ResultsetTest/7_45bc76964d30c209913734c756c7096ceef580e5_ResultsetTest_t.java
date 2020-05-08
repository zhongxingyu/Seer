 package gov.nih.nci.nautilus.test;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.framework.Test;
 import org.apache.ojb.broker.*;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.Query;
 import org.apache.ojb.broker.query.QueryByCriteria;
 import org.apache.ojb.broker.query.QueryFactory;
 import gov.nih.nci.nautilus.data.ProbesetDim;
 import gov.nih.nci.nautilus.data.DifferentialExpressionSfact;
 import gov.nih.nci.nautilus.resultset.*;
 import gov.nih.nci.nautilus.queryprocessing.ResultsetProcessor;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * @author SahniH
  * Date: September 20, 2004 
 * @version $Revision: 1.4 $
  * This junit test encapsulates the query and resultset tests
  * 
  * 
  *
  * */
 public class ResultsetTest extends TestCase{
     PersistenceBroker broker;
     Collection exprObjects = null;
     private static Class CLASS = ResultsetTest.class;
     public ResultsetTest(String s) {
         super(s);
     }
    	public static junit.framework.Test suite() {	
     	junit.framework.TestSuite suite = new junit.framework.TestSuite();
         suite.addTest(new ResultsetTest("testResultset"));
     	return suite;
 	}
 
 
     public static void main (String[] args) {
             junit.textui.TestRunner.run(suite());
 
         }
 
    public void setUp()
     {
        try {
            broker = PersistenceBrokerFactory.defaultPersistenceBroker();
            createQuery();
        } catch (PBFactoryException e) {
            e.printStackTrace();
            fail("Got " + e.getClass().getName() + ": " + e.getMessage());
        }
 
    }
     public void tearDown()
     {
         broker.close();
     }
 
     /**
 	 * test EqualTo Criteria
 	 */
     public void createQuery()
     {
     	try{
         Criteria exprCrit = new Criteria();
         exprCrit.addEqualTo("geneSymbol", "EGFR");
         Query exprQuery = QueryFactory.newQuery(DifferentialExpressionSfact.class, exprCrit);
         exprObjects = broker.getCollectionByQuery(exprQuery);
 		System.out.println("Got " + exprObjects.size() + " exprObjects objects.");
         assertNotNull(exprObjects);
         assertTrue(exprObjects.size() > 0);
         
 	        for (Iterator iterator = exprObjects.iterator(); iterator.hasNext();) {
 	        	DifferentialExpressionSfact expObj = (DifferentialExpressionSfact) iterator.next();
	            System.out.println("expObj.geneSymbol: " + expObj.getGeneSymbol() + " |expObj.probesetId: " +expObj.getProbesetId()+ " |expObj.dieasesTypeID: " +expObj.getDiseaseTypeId()+ " |expObj.biospecimenID: " +expObj.getBiospecimenId() );
 	        }
  		} catch (Exception ex) {
  			ex.printStackTrace();
  			fail("Got " + ex.getClass().getName() + ": " + ex.getMessage());
  		}  
     }
     public void testResultset(){
     	ResultsetProcessor resultsetProc = new ResultsetProcessor();
     	assertNotNull(exprObjects);
         assertTrue(exprObjects.size() > 0);
     	resultsetProc.createGeneView(exprObjects);
     	GeneViewContainer geneViewContainer = resultsetProc.getGeneViewContainer();
     	Collection genes = geneViewContainer.getGeneResultsets();
     	System.out.println("Gene Count: "+genes.size());
     	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
     		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
     		Collection reporters = geneResultset.getReporterResultsets();
         	System.out.println("Repoter Count: "+reporters.size());
     		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
         		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
         		Collection diseaseTypes = reporterResultset.getDiseaseResultsets();
             	System.out.println("Disease Count: "+diseaseTypes.size());
         		for (Iterator diseaseIterator = diseaseTypes.iterator(); diseaseIterator.hasNext();) {
         			DiseaseResultset dieaseResultset = (DiseaseResultset)diseaseIterator.next();
             		Collection biospecimens = dieaseResultset.getBioSpecimenResultsets();
                 	System.out.println("Biospecimen Count: "+biospecimens.size());
             		for (Iterator biospecimenIterator = biospecimens.iterator(); biospecimenIterator.hasNext();) {
             			BioSpecimenResultset biospecimenResultset = (BioSpecimenResultset)biospecimenIterator.next();
                 	            System.out.println(	"GeneSymbol: "+geneResultset.getGeneSymbol().getValueObject().toString()+
                 	            					"| ReporterId: "+ reporterResultset.getReporter().getValue().toString()+
 													"| DieaseType Id: "+dieaseResultset.getDieaseType().getValue().toString()+
 													"| BioSpecimen Id: "+ biospecimenResultset.getBiospecimen().getValue().toString()+
 													"| FoldChange Value: "+ biospecimenResultset.getFoldChangeValue().getValue().toString());
             		}
         		}
     		}
     	}
     	
     }
 }
