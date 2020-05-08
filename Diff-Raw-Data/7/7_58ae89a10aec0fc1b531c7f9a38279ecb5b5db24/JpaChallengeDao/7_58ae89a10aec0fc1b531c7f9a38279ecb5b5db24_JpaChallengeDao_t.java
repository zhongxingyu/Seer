 package net.chrissearle.flickrvote.dao;
 
 import net.chrissearle.common.jpa.JpaDao;
 import net.chrissearle.flickrvote.model.Challenge;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.util.Date;
 import java.util.List;
 
 @Repository
 public class JpaChallengeDao extends JpaDao<String, Challenge> implements ChallengeDao {
     private Logger logger = Logger.getLogger(JpaChallengeDao.class);
 
     @SuppressWarnings("unchecked")
     public Challenge findByTag(String tag) {
         Query query = entityManager.createQuery("select c from Challenge c where c.tag = :tag");
         query.setParameter("tag", tag);

        try {
            return (Challenge) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
     }
 
     @SuppressWarnings("unchecked")
     public List<Challenge> getAll() {
         Query query = entityManager.createQuery("select c from Challenge c");
         return (List<Challenge>) query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     public List<Challenge> getClosedChallenges() {
         Query query = entityManager.createQuery("select c from Challenge c where c.endDate < :now");
         query.setParameter("now", new Date());
         return (List<Challenge>) query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     public Challenge getCurrentChallenge() {
         Query query = entityManager.createQuery("select c from Challenge c where c.startDate <= :now and c.votingOpenDate > :now");
         query.setParameter("now", new Date());
 
         List<Challenge> challenges = (List<Challenge>) query.getResultList();
 
         if (challenges.size() > 0) {
             return challenges.iterator().next();
         }
 
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public Challenge getVotingChallenge() {
         Query query = entityManager.createQuery("select c from Challenge c where c.votingOpenDate <= :now and c.endDate > :now");
         query.setParameter("now", new Date());
 
         List<Challenge> challenges = (List<Challenge>) query.getResultList();
 
         if (logger.isDebugEnabled()) {
             logger.debug("Voting challenges: " + challenges);
         }
 
         if (challenges.size() > 0) {
             return challenges.iterator().next();
         }
 
         return null;
     }
 
     /**
      * Return challenge with votes
      *
      * @return challenge with votes - null if no votes found
      */
     @SuppressWarnings("unchecked")
     public Challenge getVotedChallenge() {
         Query query = entityManager.createQuery("select distinct c FROM Vote v, IN(v.image) i, IN(i.challenge) c");
         List<Challenge> challenges = (List<Challenge>) query.getResultList();
 
         if (challenges != null && challenges.size() > 0) {
             return challenges.iterator().next();
         }
 
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public List<Challenge> findWithin(Date date) {
         Query query = entityManager.createQuery("select c from Challenge c where c.startDate <= :date and c.votingOpenDate > :date");
         query.setParameter("date", date);
 
         return (List<Challenge>) query.getResultList();
     }
 
     public Challenge getMostRecent() {
         Query query = entityManager.createQuery("select c from Challenge c where c.startDate = (select MAX(c.startDate) FROM c)");
 
         try {
             return (Challenge) query.getSingleResult();
         } catch (NoResultException e) {
             // Means nothing - just that the db is empty
             return null;
         }
     }
 }
