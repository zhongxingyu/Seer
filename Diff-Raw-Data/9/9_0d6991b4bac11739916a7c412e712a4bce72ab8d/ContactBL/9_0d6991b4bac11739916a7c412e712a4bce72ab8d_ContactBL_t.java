 /*
     jbilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2007 Sapienter Billing Software Corp. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.user;
 
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.interfaces.ContactEntityLocal;
 import com.sapienter.jbilling.interfaces.ContactEntityLocalHome;
 import com.sapienter.jbilling.interfaces.ContactFieldEntityLocal;
 import com.sapienter.jbilling.interfaces.ContactFieldEntityLocalHome;
 import com.sapienter.jbilling.interfaces.ContactFieldTypeEntityLocal;
 import com.sapienter.jbilling.interfaces.ContactMapEntityLocal;
 import com.sapienter.jbilling.interfaces.ContactMapEntityLocalHome;
 import com.sapienter.jbilling.interfaces.ContactTypeEntityLocal;
 import com.sapienter.jbilling.interfaces.ContactTypeEntityLocalHome;
 import com.sapienter.jbilling.server.entity.ContactFieldTypeDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.util.Constants;
 
 public class ContactBL {
     // contact types in synch with the table contact_type
     static public final Integer ENTITY = new Integer(1);
     
     // private methods
     private JNDILookup EJBFactory = null;
     private ContactEntityLocalHome contactHome = null;
     private ContactTypeEntityLocalHome contactTypeHome = null;
     private ContactFieldEntityLocalHome contactFieldHome = null;
     private ContactEntityLocal contact = null;
     private Logger log = null;
     private Integer entityId = null;
     
     public ContactBL(Integer contactId) 
             throws NamingException, FinderException {
         init();
         contact = contactHome.findByPrimaryKey(contactId);
     }
     
     public ContactBL() throws NamingException {
         init();
     }
     
     public void set(Integer userId) 
             throws FinderException {
         contact = contactHome.findPrimaryContact(userId);
         setEntityFromUser(userId);
     }
     
     private void setEntityFromUser(Integer userId) {
         // id the entity
         UserBL user;
         try {
             user = new UserBL();
             entityId = user.getEntityId(userId);
         } catch (Exception e) {
             log.error("Finding the entity", e);
         } 
 
     }
  
     public void set(Integer userId, Integer contactTypeId)
             throws FinderException {
         contact = contactHome.findContact(userId, contactTypeId);
         setEntityFromUser(userId);
     }
 
     public void setEntity(Integer entityId) throws FinderException {
         this.entityId = entityId;
     	contact = contactHome.findEntityContact(entityId);
     }
 
     
     public boolean setInvoice(Integer invoiceId) throws FinderException {
         boolean retValue = false;
         try {
             contact = contactHome.findInvoiceContact(invoiceId);
             InvoiceBL invoice = new InvoiceBL(invoiceId);
             // this is needed to fetch the entity's custom fields
             entityId = invoice.getEntity().getUser().getEntity().getId();
             retValue = true;
         } catch (FinderException e) {
             // the invoice doesn't have a explicit contact.
             // Use the user's primary
             try {
                 InvoiceBL invoice = new InvoiceBL(invoiceId);
                 set(invoice.getEntity().getUser().getUserId());
             } catch (NamingException e1) {
                 log.error("Exception finding contact for invoice " + 
                         invoiceId, e1);
             } 
         } catch (NamingException e1) {
             log.error("Exception finding entity for invoice " + 
                     invoiceId, e1);
         } 
         
         return retValue;
     }
 
     public Integer getPrimaryType(Integer entityId) 
             throws FinderException {
         return contactTypeHome.findPrimary(entityId).getId();
     }
     
     /**
      * Rather confusing considering the previous method, but necessary
      * to follow the convention
      * @return
      */
     public ContactEntityLocal getEntity() {
     	return contact;
     }
     
     public ContactTypeEntityLocalHome getTypeHome() {
         return contactTypeHome;
     }
     
     public ContactDTOEx getVoidDTO(Integer myEntityId) 
             throws NamingException, FinderException {
         entityId = myEntityId;
         ContactDTOEx retValue = new ContactDTOEx();
         retValue.setFields(initializeFields());
         return retValue;
     }
     
     public ContactDTOEx getDTO() {
         
         ContactDTOEx retValue =  new ContactDTOEx(
             contact.getId(),
             contact.getOrganizationName(),
             contact.getAddress1(),
             contact.getAddress2(),
             contact.getCity(),
             contact.getStateProvince(),
             contact.getPostalCode(),
             contact.getCountryCode(),
             contact.getLastName(),
             contact.getFirstName(),
             contact.getInitial(),
             contact.getTitle(),
             contact.getPhoneCountryCode(),
             contact.getPhoneAreaCode(),
             contact.getPhoneNumber(),
             contact.getFaxCountryCode(),
             contact.getFaxAreaCode(),
             contact.getFaxNumber(),
             contact.getEmail(),
             contact.getCreateDate(),
             contact.getDeleted(),
             contact.getInclude());
         
         log.debug("ContactDTO: getting custom fields");
         try {
             retValue.setFields(initializeFields());
             for (Iterator it = contact.getFields().iterator(); it.hasNext();) {
                 ContactFieldEntityLocal field = (ContactFieldEntityLocal) it
                         .next();
                 // now find the field of this type
                 ContactFieldDTOEx dto = (ContactFieldDTOEx) retValue
                         .getFields().get(field.getTypeId().toString());
                 if (field != null && dto != null) {
                     dto.setContent(field.getContent());
                     dto.setId(field.getId());
                 }
             }
         } catch (Exception e) {
             log.error("Error initializing fields", e);
         } 
         
         log.debug("Returning dto with " + retValue.getFields().size() + 
                 " fields");
         
         return retValue;
     }
     
     public Vector<ContactDTOEx> getAll(Integer userId) 
             throws NamingException, FinderException {
         Vector<ContactDTOEx> retValue = new Vector<ContactDTOEx>();
        UserBL user = new UserBL(userId);
         entityId = user.getEntityId(userId);
         for (Iterator it = user.getEntity().getEntity().getContactTypes().
                 iterator(); it.hasNext(); ) {
             ContactTypeEntityLocal type = (ContactTypeEntityLocal) it.next();
             
             try {
                 contact = contactHome.findContact(userId, type.getId());
                 ContactDTOEx dto = getDTO();
                 dto.setType(type.getId());
                 retValue.add(dto);
             } catch (FinderException e) {
                 // it is ok to not have a contact type
             }
         }
         return retValue;
     }
     
     /**
      * Create a Hashtable with the key beign the field type for the
      * entity
      * @return
      * @throws NamingException
      * @throws FinderException
      */
     private Hashtable initializeFields() 
             throws NamingException, FinderException {
         // now go over the entity specific fields
         Hashtable fields = new Hashtable();
         EntityBL entity = new EntityBL(entityId);
         for (Iterator it = entity.getEntity().getContactFieldTypes().iterator();
                 it.hasNext();) {
             ContactFieldDTOEx fieldDto = new ContactFieldDTOEx();
             ContactFieldTypeEntityLocal field = 
                 (ContactFieldTypeEntityLocal) it.next();
             fieldDto.setTypeId(field.getId());
             ContactFieldTypeDTO type = new ContactFieldTypeDTO();
             type.setDataType(field.getDataType());
             type.setId(field.getId());
             type.setPromptKey(field.getPromptKey());
             type.setReadOnly(field.getReadOnly());
             fieldDto.setType(type);
             //fieldDto.setContent(new String()); // can't be null
             // the key HAS to be a String if we want struts to be able to
             // read the Hashtabe
             fields.put(type.getId().toString(), fieldDto);
         }
         
         return fields;
     }
     
     private void init() throws NamingException {
         log = Logger.getLogger(ContactBL.class);             
         EJBFactory = JNDILookup.getFactory(false);
         contactHome = (ContactEntityLocalHome) EJBFactory.lookUpLocalHome(
                 ContactEntityLocalHome.class,
                 ContactEntityLocalHome.JNDI_NAME);
         contactTypeHome = (ContactTypeEntityLocalHome) EJBFactory.lookUpLocalHome(
                 ContactTypeEntityLocalHome.class,
                 ContactTypeEntityLocalHome.JNDI_NAME);
         contactFieldHome = (ContactFieldEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                     ContactFieldEntityLocalHome.class,
                     ContactFieldEntityLocalHome.JNDI_NAME);
     }
     
     public Integer createPrimaryForUser(ContactDTOEx dto, Integer userId, Integer entityId) 
             throws SessionInternalError {
         // find which type id is the primary for this entity
         try {
             Integer retValue;
             ContactTypeEntityLocal type = contactTypeHome.findPrimary(entityId);
             retValue =  createForUser(dto, userId, type.getId());
             // this is the primary contact, the only one with a user_id
             // denormilized for performance
             contact.setUserId(userId); 
             return retValue;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
     }
     
     /**
      * Finds what is the next contact type and creates a new
      * contact with it
      * @param dto
      */
     public boolean append(ContactDTOEx dto, Integer userId) 
             throws NamingException, FinderException, CreateException, 
                 SessionInternalError {
         UserBL user = new UserBL(userId);
         Vector types = new Vector(user.getEntity().getEntity().
                 getContactTypes());
         Collections.sort(types, new ContactTypeComparator());
         for (int f = 0; f < types.size(); f++) {
             ContactTypeEntityLocal type = (ContactTypeEntityLocal) 
                     types.get(f);
             try {
                 set(userId, type.getId());
             } catch (FinderException e) {
                 // this one is available
                 createForUser(dto, userId, type.getId());
                 return true;
             }
         }
         
         return false; // no type was avaiable
     }
     
     public Integer createForUser(ContactDTOEx dto, Integer userId, 
             Integer typeId) throws SessionInternalError {
         try {
             return create(dto, Constants.TABLE_BASE_USER, userId, typeId);
         } catch (Exception e) {
             log.debug("Error creating contact for " +
                     "user " + userId);
             throw new SessionInternalError(e);
         }
     }
     
     public Integer createForInvoice(ContactDTOEx dto, Integer invoiceId) 
             throws NamingException, FinderException, CreateException {
         return create(dto, Constants.TABLE_INVOICE, invoiceId, new Integer(1));
     }
     
     /**
      * 
      * @param dto
      * @param table
      * @param foreignId
      * @param typeId Use 1 if it is not for a user (like and entity or invoice)
      * @return
      * @throws NamingException
      * @throws FinderException
      * @throws CreateException
      */
     public Integer create(ContactDTOEx dto, String table,  
             Integer foreignId, Integer typeId) 
             throws NamingException, FinderException, CreateException {
         // first thing is to create the map to the user
         ContactMapEntityLocalHome contactMapHome = (ContactMapEntityLocalHome)
                 EJBFactory.lookUpLocalHome(
                 ContactMapEntityLocalHome.class,
                 ContactMapEntityLocalHome.JNDI_NAME);
         ContactMapEntityLocal map = contactMapHome.create(typeId, 
                 table, foreignId);
         
         // now the contact itself
         contact = contactHome.create(map);
         
         // set all the optional fields
         contact.setAddress1(dto.getAddress1());
         contact.setAddress2(dto.getAddress2());
         contact.setCity(dto.getCity());
         contact.setCountryCode(dto.getCountryCode());
         contact.setEmail(dto.getEmail());
         contact.setFaxAreaCode(dto.getFaxAreaCode());
         contact.setFaxCountryCode(dto.getFaxCountryCode());
         contact.setFaxNumber(dto.getFaxNumber());
         contact.setFirstName(dto.getFirstName());
         contact.setInitial(dto.getInitial());
         contact.setLastName(dto.getLastName());
         contact.setOrganizationName(dto.getOrganizationName());
         contact.setPhoneAreaCode(dto.getPhoneAreaCode());
         contact.setPhoneCountryCode(dto.getPhoneCountryCode());
         contact.setPhoneNumber(dto.getPhoneNumber());
         contact.setPostalCode(dto.getPostalCode());
         contact.setStateProvince(dto.getStateProvince());
         contact.setTitle(dto.getTitle());
         contact.setInclude(dto.getInclude());
         
         updateCreateFields(dto.getFields(), false);
         
         return contact.getId();
     }
     
     public void updatePrimaryForUser(ContactDTOEx dto, Integer userId)
             throws FinderException, CreateException {
         contact = contactHome.findPrimaryContact(userId);
         update(dto);
     }
     
     public void updateForUser(ContactDTOEx dto, Integer userId,
             Integer contactTypeId) throws SessionInternalError {
         try {
             contact = contactHome.findContact(userId, contactTypeId);
             update(dto);
         } catch (FinderException e) {
             try {
                 createForUser(dto, userId, contactTypeId);
             } catch (Exception e1) {
                 throw new SessionInternalError(e1);
             }
         } catch (CreateException e1) {
             throw new SessionInternalError(e1);
         }
     }
     
     private void update(ContactDTOEx dto) 
             throws CreateException {
         contact.setAddress1(dto.getAddress1());
         contact.setAddress2(dto.getAddress2());
         contact.setCity(dto.getCity());
         contact.setCountryCode(dto.getCountryCode());
         contact.setEmail(dto.getEmail());
         contact.setFaxAreaCode(dto.getFaxAreaCode());
         contact.setFaxCountryCode(dto.getFaxCountryCode());
         contact.setFaxNumber(dto.getFaxNumber());
         contact.setFirstName(dto.getFirstName());
         contact.setInitial(dto.getInitial());
         contact.setLastName(dto.getLastName());
         contact.setOrganizationName(dto.getOrganizationName());
         contact.setPhoneAreaCode(dto.getPhoneAreaCode());
         contact.setPhoneCountryCode(dto.getPhoneCountryCode());
         contact.setPhoneNumber(dto.getPhoneNumber());
         contact.setPostalCode(dto.getPostalCode());
         contact.setStateProvince(dto.getStateProvince());
         contact.setTitle(dto.getTitle());
         contact.setInclude(dto.getInclude());
         
         updateCreateFields(dto.getFields(), true);
     }
     
     private void updateCreateFields(Hashtable fields, boolean isUpdate) 
             throws CreateException {
         if (fields == null) {
             // if the fields are not there, do nothing
             return;
         }
         // now the per-entity fields
         for (Iterator it = fields.keySet().iterator(); 
                 it.hasNext();) {
             String type = (String) it.next();
             ContactFieldDTOEx field = (ContactFieldDTOEx) fields.
                     get(type);
             //we can't create or update custom fields with null value
             if (field.getContent() == null) {
                 continue;
             }
             if (isUpdate) {
                 try {
                     if (field.getId() != null) {
                         contactFieldHome.findByPrimaryKey(field.getId())
                                 .setContent(field.getContent());
                     } else {
                         try {
                             // it is un update, but don't know the field id
                             contactFieldHome.findByType(Integer.valueOf(type),
                                     contact.getId()).setContent(field.getContent());
                         
                         } catch (FinderException e) {
                             // not there yet. It's ok
                             contact.getFields().add(contactFieldHome.create(
                                     Integer.valueOf(type), field.getContent()));
 
                         }
                     }
                 } catch (FinderException e) {
                     log.error("Updating contact", e);
                 }
             } else {
                 // create the new field
                 contact.getFields().add(contactFieldHome.create(
                         Integer.valueOf(type), field.getContent()));
             }
         }
 
     }
     
     public void delete() 
             throws RemoveException {
         
         if (contact == null) return;
         
         log.debug("Deleting contact " + contact.getId());
         // delete the map first
         contact.getContactMap().remove();
         
         // now the fields
         Iterator it = contact.getFields().iterator();
         while(it.hasNext()) {
             ContactFieldEntityLocal field = (ContactFieldEntityLocal) it.next();
             field.remove();
             // the collection has to be refreshed, or the container will throw
             it = contact.getFields().iterator();
         }
         
         // last the contact
         contact.remove();
         contact = null;
     }
     
     /**
      * Sets this contact object to that on the parent, taking the children id
      * as a parameter. 
      * @param customerId
      */
     public void setFromChild(Integer userId) throws FinderException {
         UserBL customer = new UserBL(userId);
         set(customer.getEntity().getCustomer().getParent().getUser().getUserId());
     }
 }
