 package net.tirasa.test.addressbook.dao.impl;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.persistence.Query;
 import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
 import net.tirasa.test.addressbook.dao.PersonDAO;
 import net.tirasa.test.addressbook.data.Person;
 import net.tirasa.test.addressbook.exceptions.DatabaseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository
 public class PersonDAOJpaImpl implements PersonDAO {
 
     @PersistenceContext(type = PersistenceContextType.TRANSACTION)
     @Autowired
     protected EntityManager entityManager;
 
     private final static Logger LOG = LoggerFactory.getLogger(PersonDAOJpaImpl.class);
 
     @Transactional
     public Person save(Person person) throws DatabaseException {
         LOG.debug("EXECUTING SAVE OPERATION");
         List<Person> resultList;
         Person auxPerson = null;
         try {
             Query query = entityManager.createQuery("Select a From Person a Where a.name='".concat(person.getName()).
                     concat(
                     "'"),
                     Person.class);
             resultList = query.getResultList();
             // CHECK IF PERSON ARGUMENT IS AN EXISTING PERSON, IF SO WE HAVE TO IMPORT IT IN CONTEXT
             if (resultList.isEmpty()) {
                 LOG.debug("INSERTING A NEW PERSON");
                 auxPerson = entityManager.merge(person);
                 return auxPerson;
             } else {
                 LOG.debug("UPDATING AN EXISTING PERSON");
                 auxPerson = resultList.iterator().next();
                 entityManager.remove(entityManager.find(Person.class, auxPerson.getId()));
                 auxPerson = entityManager.merge(person);
             }
         } catch (Exception e) {
             LOG.error("ERROR DURING SAVE OPERATION");
             throw new DatabaseException(e.getCause());
         }
         return auxPerson;
     }
 
     @Transactional
     public Person find(long id) throws DatabaseException {
         Person p = null;
         try {
             p = entityManager.find(Person.class, id);
         } catch (IllegalArgumentException e) {
             LOG.error("FIND OPERATION BY ENTITY MANAGER FAILED");
             throw new DatabaseException(e.getCause());
         }
         return p;
     }
 
     @Transactional
     public List<Person> list() throws DatabaseException {
         LOG.debug("LISTING PERSONS IN DATABASE...");
         List<Person> resultList = null;
         try {
            TypedQuery<Person> query = entityManager.createQuery("Select a From Person a", Person.class);
             resultList = query.getResultList();
         } catch (IllegalArgumentException e) {
             LOG.error("LIST OPERATION BY ENTITY MANAGER FAILED");
             throw new DatabaseException(e.getCause());
         }
         return resultList;
     }
 
     @Transactional
     public void delete(long id) throws DatabaseException {
         LOG.debug("DELETING ENTRY WITH ID: " + id);
         try {
             LOG.info("DELETION OF ID: " + entityManager.find(Person.class, id));
             entityManager.remove(entityManager.find(Person.class, id));
         } catch (IllegalArgumentException e) {
             LOG.error("DELETE OPERATION BY ENTITY MANAGER FAILED: PROBLEM ON ARGUMENTS");
             throw new DatabaseException(e.getCause());
         } catch (TransactionRequiredException e) {
             LOG.error("DELETE OPERATION BY ENTITY MANAGER FAILED: PROBLEM ON TRANSACTION");
             throw new DatabaseException(e.getCause());
         }
     }
 }
