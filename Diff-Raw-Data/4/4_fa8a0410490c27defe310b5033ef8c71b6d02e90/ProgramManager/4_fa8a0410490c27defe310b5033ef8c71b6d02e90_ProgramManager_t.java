 package hu.sch.kfc.ejb;
 
 import hu.sch.kfc.domain.OrderInterval;
 import hu.sch.kfc.domain.Program;
 import java.util.List;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 @Stateless
 @LocalBean
 public class ProgramManager {
 
     @PersistenceContext
     EntityManager em;
 
     public Program findProgram(Long id) {
         return em.find(Program.class, id);
     }
 
     public OrderInterval findOrderInterval(Long id) {
         return em.find(OrderInterval.class, id);
     }
 
     public List<Program> findProgramsByGroupToken(String groupToken) {
         return em.createNamedQuery(Program.retrieveByGroupToken, Program.class)
                 .setParameter("token", groupToken).getResultList();
     }
 
     public void persist(Program p) {
        for(OrderInterval oi : p.getOrderIntervals()) {
            em.merge(oi);
        }
        
         System.out.println(p);
         System.out.println(p.getName());
         if (p.getId() != null) {
             em.merge(p);
         } else {
             em.persist(p);
         }
     }
 }
