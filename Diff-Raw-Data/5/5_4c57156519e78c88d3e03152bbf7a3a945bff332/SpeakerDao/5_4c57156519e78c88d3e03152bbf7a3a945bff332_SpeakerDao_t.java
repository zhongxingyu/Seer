 package fr.adrienbrault.notetonsta.dao;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import fr.adrienbrault.notetonsta.entity.Speaker;
 
 public class SpeakerDao extends Dao<Speaker, Integer> {
 	
 	public SpeakerDao(EntityManager entityManager) {
 		super(entityManager);
 	}
 	
 	public Speaker findByEmail(String email) {
 		Query query = entityManager.createQuery(
 			"SELECT s" +
			"FROM " + entityClass.getName() + " s" +
 			"WHERE s.email = :email");
 		query.setParameter("email", email);
 		
 		Speaker speaker;
 		
 		try {
 			speaker = (Speaker) query.getSingleResult();
 		} catch (NoResultException e) {
 			speaker = null;
 		}
 		
 		return speaker;
 	}
 	
 	public Long countByEmail(String email) {
 		Query query = entityManager.createQuery(
 			"SELECT COUNT(s)" +
			"FROM " + entityClass.getName() + " s" +
 			"WHERE s.email = :email"
 		);
 		query.setParameter("email", email);
 		
 		return (Long) query.getSingleResult();
 	}
 	
 }
