 package db;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import model.Finalurllist;
 import model.Temporaryurllist;
 
 public class DatabaseUtil {
 
     private static DatabaseUtil INSTANCE;
 	private static final String PERSISTENCE_UNIT_NAME = "JPATestProject";
 	private static EntityManagerFactory factory;
 	private EntityManager em;
 
     public static DatabaseUtil getInstance() {
     	if (INSTANCE == null) {
      	   INSTANCE = new DatabaseUtil();    		
     	}
        return INSTANCE;	
     }
 
 	private DatabaseUtil(){
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
 		em = factory.createEntityManager();
 	}
 
 	public void insertRecordTemporaryTable(String url, int priority){
 
 //		System.out.println("Trying to create new entity in temporary table");
 		 em.getTransaction().begin(); 
 		 Temporaryurllist todo2 = new Temporaryurllist();
 		 todo2.setUrl(url);
 		 todo2.setPriority(priority);
 		 //todo2.setId('%');
 		 em.persist(todo2);
 		 em.getTransaction().commit();
 		
 	}
 
 	public void insertRecordTemporaryTable(int id, String url, int priority){
 
 //		System.out.println("Trying to create new entity in temporary table");
 		 em.getTransaction().begin(); 
 		 Temporaryurllist todo2 = new Temporaryurllist();
 		 todo2.setId(id);
 		 todo2.setUrl(url);
 		 todo2.setPriority(priority);
 		 //todo2.setId('%');
 		 em.persist(todo2);
 		 em.getTransaction().commit();
 		
 	}
 	
 	public void insertRecordFinalTable(String url, int priority){
 //		 System.out.println("Trying to create new entity in final table");
 		 em.getTransaction().begin(); 
 		 Finalurllist todo2 = new Finalurllist();
 		 todo2.setUrl(url);
 		 todo2.setPriority(priority);
 		 //todo2.setId('%');
 		 em.persist(todo2);
 		 em.getTransaction().commit();
 	}
 
 	public void insertRecordFinalTable(int id, String url, int priority){
 
 //		System.out.println("Trying to create new entity in temporary table");
 		 em.getTransaction().begin(); 
 		 Finalurllist todo2 = new Finalurllist();
 		 todo2.setId(id);
 		 todo2.setUrl(url);
 		 todo2.setPriority(priority);
 		 //todo2.setId('%');
 		 em.persist(todo2);
 		 em.getTransaction().commit();
 		
 	}
 
 	public Temporaryurllist getTemporaryURLListById(int id){
 		// Get a Temporaryurllist record by key.
 //		em.getTransaction().begin();
 		Query q = em.createQuery("select frul FROM Temporaryurllist frul where frul.id = " + id);
 		Temporaryurllist temporaryurlList = (Temporaryurllist)q.getSingleResult();
 		return temporaryurlList;
 	}
 
 	public Finalurllist getFinalURLListById(int id){
 		// Get a Finalurllist record by key.
 //		em.getTransaction().begin();
 		Query q = em.createQuery("select frul FROM Temporaryurllist frul where frul.id = " + id);
 		Finalurllist finalurlList = (Finalurllist)q.getSingleResult();
 		return finalurlList;
 	}
 
 //	public int getTemporaryURLListSize() {
 //		em.getTransaction().begin();
 //		Query q = em.createQuery("select count(frul) FROM Temporaryurllist frul");
 //		Long result = (Long)q.getSingleResult();
 //		return result.intValue();
 //	}
 	
 	public List<Temporaryurllist> queryTemporaryURLList(){
 		// Read the existing entries and write to console
 		em.getTransaction().begin();
 		Query q = em.createQuery("select frul FROM Temporaryurllist frul");
 		@SuppressWarnings("unchecked")
 		List<Temporaryurllist> temporaryurlList = (List<Temporaryurllist>)q.getResultList();
 		/*
 		System.out.println("Getting Size " + finalURLListList.get(0).getUrl());
 		for (Finalurllist furl : finalURLListList) {
 			System.out.println(furl.getId());
 			System.out.println(furl.getUrl());
 		}
 		System.out.println("Size: " + finalURLListList.size());
 		*/
 		return temporaryurlList;
 
 	}
 
 	public List<Finalurllist> queryFinalURLList(){
 		// Read the existing entries and write to console
 		Query q = em.createQuery("select frul FROM Finalurllist frul");
 		@SuppressWarnings("unchecked")
 		List<Finalurllist> finalURLListList = (List<Finalurllist>)q.getResultList();
 		/*
 		System.out.println("Getting Size " + finalURLListList.get(0).getUrl());
 		for (Finalurllist furl : finalURLListList) {
 			System.out.println(furl.getId());
 			System.out.println(furl.getUrl());
 		}
 		System.out.println("Size: " + finalURLListList.size());
 		*/
 		return finalURLListList;
 
 	}
 
 	public void deleteAllFromTemporaryTable() {
 		//Query q = em.createQuery("delete FROM Temporaryurllist frul");
 		//int deletedCount = q.executeUpdate();
 		em.getTransaction().begin();
//		int deletedCount = 
		em.createQuery("delete FROM Temporaryurllist frul").executeUpdate();
//		System.out.println("Deleted " + deletedCount + " rows");
 		//List<Finalurllist> finalURLListList = q.getResultList();
 		em.getTransaction().commit();
 	}
 
 	public void deleteAllFromFinalTable() {
 		em.getTransaction().begin();
//		int deletedCount = 
		em.createQuery("delete FROM Finalurllist frul").executeUpdate();
 		//List<Finalurllist> finalURLListList = q.getResultList();
 		
 //		System.out.println("Deleted " + deletedCount + " rows");
 		em.getTransaction().commit();
 	}
 
 	// em.close();
 }
