 package pl.agh.enrollme.repository;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Term;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 @Repository
 public class TermDAO extends GenericDAO<Term> implements ITermDAO {
 
     @PersistenceContext
     private EntityManager em;
 
     public TermDAO() {
         super(Term.class);
     }
 
     @Override
     @Transactional
     public List<Term> getTermsBySubject(Subject subject) {
        final TypedQuery<Term> query = em.createQuery("Select t from Term t where t.termId.subject = :subject",
                Term.class).setParameter("subject", subject);
 
         return query.getResultList();
     }
 
 }
