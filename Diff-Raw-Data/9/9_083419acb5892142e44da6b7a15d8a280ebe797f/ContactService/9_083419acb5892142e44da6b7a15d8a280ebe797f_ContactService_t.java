 package de.hswt.hrm.contact.service;
 
 import java.util.Collection;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.contact.dao.core.IContactDao;
import de.hswt.hrm.contact.dao.jdbc.ContactDao;
 import de.hswt.hrm.contact.model.Contact;
 
 /**
  * Service class which should be used to interact with the storage system for contacts.
  */
 public class ContactService {
     
     private ContactService() { };
     
//    @Inject
    private static IContactDao dao = new ContactDao();
     
     /**
      * @return All contacts from storage.
      * @throws DatabaseException 
      */
     public static Collection<Contact> findAll() throws DatabaseException {
         return dao.findAll();
     }
     
     /**
      * @param id of the target contact.
      * @return Contact with the given id.
      * @throws DatabaseException 
      */
     public static Contact findById(int id) throws DatabaseException {
         return dao.findById(id);
     }
     
     /**
      * Add a new contact to storage.
      * 
      * @param contact Contact that should be stored.
      * @return The created contact.
      * @throws SaveException If the contact could not be inserted.
      */
     public static Contact insert(Contact contact) throws SaveException {
         return insert(contact);
     }
     
     /**
      * Update an existing contact in storage.
      * 
      * @param contact Contact that should be updated.
      * @throws ElementNotFoundException If the given contact is not present in the database.
      * @throws SaveException If the contact could not be updated.
      */
     public static void update(Contact contact) throws ElementNotFoundException, SaveException {
         dao.update(contact);
     }
 }
