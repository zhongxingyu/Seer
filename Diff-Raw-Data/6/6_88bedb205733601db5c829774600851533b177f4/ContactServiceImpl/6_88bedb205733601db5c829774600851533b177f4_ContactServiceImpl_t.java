 package com.akolesnik.java.contactmanager.service;
 
 import com.akolesnik.groovy.contactmanager.dao.ContactTypeDAO;
 import com.akolesnik.groovy.contactmanager.domain.ContactType;
 import com.akolesnik.java.contactmanager.dao.ContactDAO;
 import com.akolesnik.java.contactmanager.domain.Contact;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 @Service
 public class ContactServiceImpl implements ContactService {
 
     @Autowired
     private ContactDAO contactDAO;
 
    @Autowired
    private ContactTypeDAO contactTypeDAO;
 
     @Transactional
     @Override
     public void addContact(Contact contact) {
 
         contactDAO.addContact(contact);
 
     }
 
     @Override
     @Transactional
     public List<Contact> listContact() {
 
         return contactDAO.listContact();
     }
 
     @Override
     @Transactional
     public void removeContact(Integer id) {
 
         contactDAO.removeContact(id);
 
     }
 

 
     @Override
     @Transactional
     public List<ContactType> listContactType() {
         return contactTypeDAO.listContactTypes();
     }
 
 
 }
