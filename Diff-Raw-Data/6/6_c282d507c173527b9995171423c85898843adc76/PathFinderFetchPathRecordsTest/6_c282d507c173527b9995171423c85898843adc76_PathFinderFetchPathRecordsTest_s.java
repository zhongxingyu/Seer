 package edu.wustl.cab2b.server.path;
 
 import java.util.List;
 
import junit.framework.TestCase;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.cab2b.common.ejb.sqlquery.SQLQueryBusinessInterface;
 import edu.wustl.cab2b.server.util.ConnectionUtil;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * 
  * @author Chandrakant Talele
  */
public class PathFinderFetchPathRecordsTest extends TestCase {
     SQLQueryBusinessInterface sqlQueryBean = null;
 
     /**
      * @see junit.framework.TestCase#setUp()
      */
     @Override
     protected void setUp() throws Exception {
         Logger.configure();
     }
 
     public void testFindPaths() {
         EntityManagerInterface em = EntityManager.getInstance();
         EntityInterface p = null;
         EntityInterface pmi = null;
         try {
             p = em.getEntityByName("edu.wustl.catissuecore.domain.Participant");
             pmi = em.getEntityByName("edu.wustl.catissuecore.domain.ParticipantMedicalIdentifier");
         } catch (DynamicExtensionsSystemException e) {
             e.printStackTrace();
             fail("Unbale to get entities Participant,ParticipantMedicalIdentifier");
         } catch (DynamicExtensionsApplicationException e) {
             e.printStackTrace();
             fail("Unbale to get entities Participant,ParticipantMedicalIdentifier");
         }
 
         PathFinder pf = PathFinder.getInstance();
         List<IPath> list = pf.getAllPossiblePaths(p, pmi, ConnectionUtil.getConnection());
         assertEquals(2, list.size());
         int s0 = list.get(0).getIntermediateAssociations().size();
         boolean res = (s0 == 1 || s0 == 4);
         assertTrue(res);
 
         s0 = list.get(1).getIntermediateAssociations().size();
         res = (s0 == 1 || s0 == 4);
         assertTrue(res);
     }
 }
