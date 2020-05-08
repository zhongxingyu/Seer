 package eu.trentorise.opendata.ckanalyze.jpa;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import eu.trentorise.opendata.ckanalyze.managers.PersistencyManager;
 
 @Entity
 public class Datatype {
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE)
 	int DatatypeId;
 	String name;
 
 	public int getDatatypeId() {
 		return DatatypeId;
 	}
 
 	public void setDatatypeId(int datatypeId) {
 		DatatypeId = datatypeId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public static Datatype getDatatypeByName(String name) {
 		String hql = "FROM Datatype WHERE name = :name";
 		Session ss = PersistencyManager.getSessionFactory().openSession();
 		Query query = ss.createQuery(hql);
 		query.setParameter("name", name);
		@SuppressWarnings("unchecked")
 		List<Datatype> results = (List<Datatype>) query.list();
 		if (results.isEmpty()) {
 			ss.close();
 			return null;
 		} else {
 			Datatype retval = results.get(0);
 			ss.close();
 			return retval;
 		}
 	}
 
 }
