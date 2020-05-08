 package ch.hsr.waktu.controller.datacontroller;
 
 import javax.persistence.EntityManager;
 
 import org.junit.AfterClass;
 
 import ch.hsr.waktu.controller.PersistenceController;
 import ch.hsr.waktu.services.WaktuException;
 
 public class TestSuiteDataController {
 	@AfterClass
 	public static void after() {
 		EntityManager em;
 		try {
 			em = PersistenceController.getInstance().getEMF()
 					.createEntityManager();
 			em.getTransaction().begin();
 			em.createNativeQuery(
 					"TRUNCATE TABLE favorite, permission, projectstaff, worksession, workpackage, project, usr")
 					.executeUpdate();
 			em.getTransaction().commit();
 		} catch (WaktuException e) {
 			e.printStackTrace();
 		}
 	}
 }
