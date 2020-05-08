 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Random;
 import java.util.Set;
 
 import javax.ejb.CreateException;
 import javax.ejb.EJBException;
 import javax.ejb.FinderException;
 import javax.ejb.SessionBean;
 import javax.ejb.SessionContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.process.AgeingBL;
 import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.partner.PartnerBL;
 import com.sapienter.jbilling.server.user.partner.db.Partner;
 import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
 import com.sapienter.jbilling.server.user.partner.db.PartnerRange;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.DTOFactory;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import com.sapienter.jbilling.server.util.db.LanguageDAS;
 import com.sapienter.jbilling.server.util.db.generated.Ach;
 import com.sapienter.jbilling.server.util.db.generated.CreditCard;
 
 /**
  *
  * This is the session facade for the user. All interaction from the client
  * to the server is made through calls to the methods of this class. This 
  * class uses helper classes (Business Logic -> BL) for the real logic.
  *
  * @author emilc
  * @ejb:bean name="com/sapienter/jbilling/server/user/UserSession"
  *           display-name="The user session facade"
  *           type="Stateless"
  *           transaction-type="Container"
  *           view-type="both"
  *           jndi-name="com/sapienter/jbilling/server/user/UserSession"
  * 
  * @ejb.transaction type="Required"
  * 
  */
 
 public class UserSessionBean implements SessionBean, PartnerSQL {
 
     private static final Logger LOG = Logger.getLogger(UserSessionBean.class);
     private SessionContext context = null;
     
 
     // -------------------------------------------------------------------------
     // Methods
     // -------------------------------------------------------------------------  
 
     /**
     * @ejb:interface-method view-type="remote"
     * @return the populated userDTO if ok, or null if fails.
     * @param clientUser The userDTO with the username and password to authenticate
     */
     public Integer authenticate(UserDTOEx clientUser) 
             throws SessionInternalError {
         Integer result = Constants.AUTH_WRONG_CREDENTIALS;
         try {
             LOG.debug("Authentication of " + clientUser.getUserName() + 
             		" password = [" + clientUser.getPassword() + "]" +
                     " entity = " + clientUser.getEntityId());
             UserDTOEx dbUser = DTOFactory.getUserDTO(clientUser.getUserName(), 
                     clientUser.getEntityId());
             if (dbUser == null) return result; // username is wrong (not found in DB)
             LOG.debug("DB password is [" + dbUser.getPassword() + "]");
             // the permissions and menu will get loaded only for
             // successfulls logins
             UserBL user = new UserBL();
             if(user.validateUserNamePassword(clientUser, dbUser)) {
                 result = Constants.AUTH_OK;
                 user.successLoginAttempt();
             } else {
                 user.set(clientUser.getUserName(), clientUser.getEntityId());
                 if (user.failedLoginAttempt()) {
                     result = Constants.AUTH_LOCKED;
                 }
             }
 
         } catch (Exception e) { // all the rest are internal error
             // I catch everything here, and let know to the client that an
             // internal error has happened.
             throw new SessionInternalError(e);
         }
         
         LOG.debug("result is " + result);
         return result;
     }
     
     /**
      * This returns more than a DTOEx, it includes the permissions and menu that
      * the GUI needs
      * @param userId
      * @return
      * 
      * @ejb:interface-method view-type="remote"
      */
     public UserDTOEx getGUIDTO(String username, Integer entityId) {
         UserDTOEx retValue;
         try {
             UserBL bl = new UserBL(username, entityId);
             retValue = DTOFactory.getUserDTOEx(bl.getEntity());
             retValue.setAllPermissions(bl.getPermissions());
             retValue.setMenu(bl.getMenu(retValue.getAllPermissions()));
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
         
         return retValue;
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      * @return the new user id if everthing ok, or null if the username is already 
      * taken, any other problems go as an exception
      */
     public Integer create(UserDTOEx newUser, ContactDTOEx contact) 
             throws SessionInternalError {
         try {
             UserBL bl = new UserBL();
             if (!bl.exists(newUser.getUserName(), newUser.getEntityId())) {
                 
                 ContactBL cBl = new ContactBL();
                 
                 Integer userId = bl.create(newUser);
                 if (userId != null) {
                     // children inherit the contact of the parent user
                     if (newUser.getCustomer() != null && 
                             newUser.getCustomer().getParent() != null) {
                         cBl.setFromChild(userId);
                         contact = cBl.getDTO();
                         LOG.debug("Using parent's contact " + contact.getId());
                     }
                     cBl.createPrimaryForUser(contact, userId, newUser.getEntityId());
                 } else {
                     // means that the partner doens't exist
                     userId = new Integer(-1);
                 }
                 return userId;
             }
             
             return null;
             
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */    
     public Integer createEntity(ContactDTO contact, UserDTOEx user, 
             Integer pack, Boolean config, String language,
             ContactDTO paymentContact)
             throws SessionInternalError {
         try {
             // start by creating the new entity
             EntityBL bl = new EntityBL();
             Integer languageId = new LanguageDAS().findByCode(language).getId();
             Integer entityId = bl.create(user, contact, languageId);
             
             final String notCryptedPassword = user.getPassword();
             
             if (paymentContact != null) {
                 // now a new customer for Sapienter
                 user.setEntityId(new Integer(1));
                 String userName = user.getUserName();
                 Integer newUserId = null;
                 Random rnd = new Random();
                 while (newUserId == null) {
                     newUserId = create(user, new ContactDTOEx(paymentContact));
                     if (newUserId == null) {
                         user.setUserName(userName + rnd.nextInt(100));
                     }
                 }
                 createCreditCard(newUserId, user.getCreditCard());
                 
                 String params[] = new String[6];
                 params[0] = contact.getFirstName();
                 params[1] = contact.getLastName();
                 params[2] = userName;
                 params[3] = notCryptedPassword;
                 params[4] = entityId.toString();
                 params[5] = entityId.toString();
                 NotificationBL.sendSapienterEmail(entityId, "signup.welcome", 
                         null, params);
             }
             
             return entityId;
         } catch (Exception e) {
             context.setRollbackOnly();
             throw new SessionInternalError(e);
         }
     }
     /**
      * @ejb:interface-method view-type="remote"
      */    
     public UserDTO getUserDTO(String userName, Integer entityId) 
             throws SessionInternalError {
         UserDTO dto = null;
         try {
             UserBL user = new UserBL(userName, entityId);
             dto = user.getDto();
         } catch (Exception e) { 
             throw new SessionInternalError(e);
         }
         
         return dto;
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */    
     public String getCustomerNotes(Integer userId)
     		throws SessionInternalError {
     	try {
     		UserBL user = new UserBL(userId);
     		return user.getEntity().getCustomer().getNotes();
         } catch (Exception e) { 
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */    
     public Locale getLocale(Integer userId)
             throws SessionInternalError {
         try {
             UserBL user = new UserBL(userId);
             return user.getLocale();
         } catch (Exception e) { 
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */    
     public void setCustomerNotes(Integer userId, String notes)
     		throws SessionInternalError {
     	try {
     		UserBL user = new UserBL(userId);
     		user.getEntity().getCustomer().setNotes(notes);
         } catch (Exception e) { 
             throw new SessionInternalError(e);
         }
     }
 
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void delete(Integer executorId, Integer userId) 
             throws SessionInternalError {
         if (userId == null) {
             throw new SessionInternalError("userId can't be null");
         }
         try {
             UserBL bl = new UserBL(userId);
             bl.delete(executorId);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void delete(String userName, Integer entityId) 
             throws SessionInternalError {
         if (userName == null) {
             throw new SessionInternalError("userId can't be null");
         }
         try {
             UserBL user = new UserBL(userName, entityId);
             user.delete(null);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
 
     /**
      * @param userId The user that is doing this change, it could be
      * the same user or someone else in behalf.
      * @ejb:interface-method view-type="remote"
      */
     public void update(Integer executorId, UserDTOEx dto) 
             throws SessionInternalError {
         try {
             UserBL bl = new UserBL(dto.getUserId());
             bl.update(executorId, dto);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void updatePartner(Integer executorId, Partner dto) 
             throws SessionInternalError {
         try {
             PartnerBL bl = new PartnerBL(dto.getId());
             bl.update(executorId, dto);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void updatePartnerRanges(Integer executorId, Integer partnerId, 
             PartnerRange[] ranges) 
             throws SessionInternalError {
         try {
             PartnerBL bl = new PartnerBL(partnerId);
             bl.setRanges(executorId, ranges);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     
     /**
     * @ejb:interface-method view-type="remote"
     */
     public ContactDTOEx getPrimaryContactDTO(Integer userId)
             throws SessionInternalError {
         try {
             ContactBL bl = new ContactBL();
             bl.set(userId);
             return bl.getDTO();
         } catch (Exception e) {
             LOG.error("Exception retreiving the customer contact", e);
             throw new SessionInternalError("Customer primary contact");
         }
     }
     
     /**
     * @ejb:interface-method view-type="remote"
     */
     public void setPrimaryContact(ContactDTOEx dto, Integer userId)
             throws SessionInternalError {
         try {
             ContactBL cbl = new ContactBL();
             
             cbl.updatePrimaryForUser(dto, userId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }                    
 
     /**
      * @ejb:interface-method view-type="remote"
      */
      public ContactDTOEx getContactDTO(Integer userId, Integer contactTypeId)
              throws SessionInternalError, FinderException {
         ContactBL bl = new ContactBL();
         bl.set(userId, contactTypeId);
        return bl.getDTO();
      }
  
      /**
       * @ejb:interface-method view-type="remote"
       */
       public ContactDTOEx getVoidContactDTO(Integer entityId)
               throws SessionInternalError {
          try {
              ContactBL bl = new ContactBL();
              return bl.getVoidDTO(entityId);
          } catch (Exception e) {
              throw new SessionInternalError(e);
          } 
       }
 
      /**
       * @ejb:interface-method view-type="remote"
       */
      public void setContact(ContactDTOEx dto, Integer userId, Integer 
              contactTypeId)
              throws SessionInternalError {
         try {
             ContactBL cbl = new ContactBL();
 
             cbl.updateForUser(dto, userId, contactTypeId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }                    
 
      /**
       * @ejb:interface-method view-type="remote"
       */
      public boolean addContact(ContactDTOEx dto, String username,
              Integer entityId)
              throws SessionInternalError {
         try {
             UserBL user = new UserBL(username, entityId);
             ContactBL cbl = new ContactBL();
 
             return cbl.append(dto, user.getEntity().getUserId());
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }                    
 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public UserDTOEx getUserDTOEx(Integer userId) 
             throws SessionInternalError, FinderException {
         UserDTOEx dto = null;
         
         try {
             dto = DTOFactory.getUserDTOEx(userId);
             dto.touch();
         } catch (FinderException e) {
             throw new FinderException();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return dto;
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Boolean isParentCustomer(Integer userId) 
             throws SessionInternalError, FinderException {
         try {
             UserBL user = new UserBL(userId);
             Integer isParent = user.getEntity().getCustomer().getIsParent();
             if (isParent == null || isParent.intValue() == 0) {
                 return new Boolean(false);
             } else {
                 return new Boolean(true);
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public UserDTOEx getUserDTOEx(String userName, Integer entityId) 
             throws SessionInternalError{
         UserDTOEx dto = null;
         
         try {
             UserBL bl = new UserBL();
             bl.set(userName, entityId);
             dto = DTOFactory.getUserDTOEx(bl.getEntity());
             dto.touch();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return dto;
     }
 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Integer createCreditCard(Integer userId,
             CreditCardDTO dto) throws SessionInternalError {
         try {
             // create the cc record
             CreditCardBL ccBL = new CreditCardBL();
             ccBL.create(dto);
             // now find this user to add the cc to it
             ccBL.getEntity().setUserId(userId);
             
             return ccBL.getEntity().getId();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * This actually creates a credit card record for a user without it,
      * or updates an existing one.
      * Since now we are only supporting one cc per user, this will
      * just get the first cc and update it (it could have deleted 
      * all of them and create one, but it was too crapy).
      * @ejb:interface-method view-type="remote"
      */
     public void updateCreditCard(Integer executorId, Integer userId,
             CreditCardDTO dto) throws SessionInternalError {
         try {
             // find this user and get the first cc
             UserBL userBL = new UserBL(userId);
             updateCreditCard(userBL.getEntity(), dto, executorId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void updateCreditCard(String username, Integer entityId,
             CreditCardDTO dto) throws SessionInternalError {
         try {
             UserBL userBL = new UserBL(username, entityId);
             updateCreditCard(userBL.getEntity(), dto, null);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
     
     private void updateCreditCard(UserDTO user, 
             CreditCardDTO dto, Integer executorId) 
             throws SessionInternalError {
         // find this user and get the first cc
 
         try {
 
             UserBL userBL = new UserBL();
             userBL.set(user);
             // if it starts with a *, it is passing a masked cc, which means no update
             if (dto != null && (dto.getNumber() == null || dto.getNumber().charAt(0) != '*')) { // it is providing a new cc
                 if (!userBL.getEntity().getCreditCards().isEmpty()) {
                     CreditCardBL ccBL = new CreditCardBL(((CreditCard)
                             userBL.getEntity().getCreditCards().iterator().next()).getId());
                     ccBL.update(executorId, dto, user.getId());
                 } else { // this is really a create
                     createCreditCard(user.getUserId(), dto);
                 }
             } else { // no new card, really
                 deleteCreditCard(executorId, user.getUserId());
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
 
     }   
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void setAuthPaymentType(Integer userId, Integer newMethod, 
     		Boolean use) 
     		throws SessionInternalError {
     	try {
     		UserBL user = new UserBL(userId);
             if (user.getEntity().getCustomer() == null) {
                 LOG.warn("Trying to update the automatic payment type of a " +
                         "non customer");
                 return;
             }
         	Integer method = user.getEntity().getCustomer().
 					getAutoPaymentType();
         	// it wants to use this one now
         	if (use.booleanValue()) {
         		user.getEntity().getCustomer().setAutoPaymentType(newMethod);
         	}
         	// it has this method, and doesn't want to use it any more
         	if (method != null && method.equals(newMethod) && 
         			!use.booleanValue()) {
         		user.getEntity().getCustomer().setAutoPaymentType(null);
         	}
     	} catch (Exception e) {
     		throw new SessionInternalError(e);
     	}
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Integer getAuthPaymentType(Integer userId)
     		throws SessionInternalError {
     	try {
     		UserBL user = new UserBL(userId);
         	Integer method;
             if (user.getEntity().getCustomer() != null) {
                 method = user.getEntity().getCustomer().getAutoPaymentType();
             } else {
                 // this will be necessary as long as non-customers can have
                 // a credit card
                 method = new Integer(0); 
             }
         	return method;
     	} catch (Exception e) {
     		throw new SessionInternalError(e);
     	}
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void updateACH(Integer userId, Integer executorId, AchDTO ach)
 			throws SessionInternalError {
 		try {
 			UserBL user = new UserBL(userId);
 			user.updateAch(ach, executorId);
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}
 	}
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public AchDTO getACH(Integer userId)
 			throws SessionInternalError {
 		try {
 			UserBL user = new UserBL(userId);
 			Set<Ach> ach = user.getEntity().getAchs();
 			if (ach.size() > 0) {
 				AchBL bl = new AchBL(((Ach)ach.toArray()[0]).getId());
 				return bl.getDTO();
 			} 
 			return null;
 			
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}
 	}
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void removeACH(Integer userId, Integer executorId)
 			throws SessionInternalError {
 		try {
 			UserBL user = new UserBL(userId);
             Set<Ach> ach = user.getEntity().getAchs();
             if (ach.size() > 0) {
                 AchBL bl = new AchBL(((Ach)ach.toArray()[0]).getId());
 				bl.delete(executorId);
 			}
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}
 	}
 
     /**
      * Since now we are only supporting one cc per user, this will
      * just get the first cc .
      * @ejb:interface-method view-type="remote"
      */
     public CreditCardDTO getCreditCard(Integer userId) 
             throws SessionInternalError {
         CreditCardDTO retValue;
         try {
             // find this user and get the first cc
             UserBL userBL = new UserBL(userId);
             if (!userBL.getEntity().getCreditCards().isEmpty()) {
                 CreditCardBL ccBL = new CreditCardBL(((CreditCard)
                         userBL.getEntity().getCreditCards().toArray()[0]).getId());
                 retValue = ccBL.getDTO();
             } else { // return a blank one
                 retValue = null;
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return retValue;
     }
     
     /**
      * @return The path or url of the css to use for the given entity
      * @ejb.interface-method view-type="remote"
      */
     public String getEntityPreference(Integer entityId, Integer preferenceId) 
             throws SessionInternalError {
         try {
             String result = null;
             PreferenceBL preference = new PreferenceBL();
             try {
                 preference.set(entityId, preferenceId);
                 result = preference.getValueAsString();
             } catch (FinderException e) { 
                 // it is missing, so it will pick up the default
             }
 
             if (result == null  || result.trim().length() == 0) {
                 result = preference.getDefaultAsString(preferenceId);
                 LOG.debug("Using default");
             }
             
             if (result == null) {
                 LOG.warn("Preference " + preferenceId + " does not have a " +
                         " default.");
             }
         
             LOG.debug("result for " + preferenceId + " =" + result);
             return result;    
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * Get the entity's contact information
      * @param entityId
      * @return
      * @throws SessionInternalError
      * @ejb.interface-method view-type="remote"
      */
     public ContactDTOEx getEntityContact(Integer entityId) 
             throws SessionInternalError {
         try {
             ContactBL bl = new ContactBL();
             bl.setEntity(entityId);
             return bl.getDTO();
         } catch (Exception e) {
             LOG.error("Exception retreiving the entity contact", e);
             throw new SessionInternalError("Customer primary contact");
         }
     }
     
     /**
      * 
      * @param entityId
      * @return
      * @throws SessionInternalError
      * @ejb.interface-method view-type="remote"
      */
     public Integer getEntityPrimaryContactType(Integer entityId) 
             throws SessionInternalError {
         try {
             ContactBL contact = new ContactBL();
             return contact.getPrimaryType(entityId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * This is really an entity level class, there is no user involved.
      * This means that the lookup of parameters will be based on the table
      * entity.
      * 
      * @param ids
      * An array of the parameter ids that will be looked up and returned in
      * the hashtable
      * @return
      * The paramteres in "id - value" pairs. The value is of type String
      * 
      * @ejb.interface-method view-type="remote"
      */    
     public HashMap getEntityParameters(Integer entityId, Integer[] ids) 
             throws SessionInternalError {
         HashMap retValue = new HashMap();
         
         try {
             PreferenceBL preference = new PreferenceBL();
             for (int f = 0; f < ids.length; f++) {
                 try {
                     preference.set(entityId, ids[f]);
                     retValue.put(ids[f], preference.getValueAsString());
                 } catch (FinderException e1) {
                     // use a default
                     retValue.put(ids[f], 
                             preference.getDefaultAsString(ids[f]));
                 }
             }        
             return retValue;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * @param entityId
      * @param params
      * @throws SessionInternalError
      * 
      * @ejb.interface-method view-type="remote"
      */
     public void setEntityParameters(Integer entityId, HashMap params) 
             throws SessionInternalError {
         try {
             PreferenceBL preference = new PreferenceBL();
             for (Iterator it = params.keySet().iterator(); it.hasNext();) {
                 Integer preferenceId = (Integer) it.next();
                 
                 Object value = params.get(preferenceId);
                 if (value != null) {
                     if (value instanceof Integer) {
                         preference.createUpdateForEntity(entityId, preferenceId, 
                                 (Integer) value, null, null);
                         
                     } else if (value instanceof String) {
                         preference.createUpdateForEntity(entityId, preferenceId, null, 
                                 (String) value, null);
                     } else if (value instanceof Float) {
                         preference.createUpdateForEntity(entityId, preferenceId, null, 
                                 null, (Float) value);
                     }
                 } else {
                     preference.createUpdateForEntity(entityId, preferenceId, null, 
                             null, null);
                 }
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /** 
     * @ejb.interface-method view-type="remote"
     */
     public void updatePreference(Integer userId, Integer typeId, Integer intValue,
             String strValue, Float floatValue) 
         throws SessionInternalError {
         try {
             LOG.debug("updateing preference " + typeId + " for user " +
                     userId);
             PreferenceBL preference = new PreferenceBL();
             preference.createUpdateForUser(userId, typeId, intValue, strValue, 
                     floatValue);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
     }
     /**
      * This now only working with String parameters
      * @param entityId
      * @param params
      * @throws SessionInternalError
      * 
      * @ejb.interface-method view-type="remote"
      */
     public void setEntityParameter(Integer entityId, Integer preferenceId,
             String paramStr, Integer paramInt, Float paramFloat) 
             throws SessionInternalError {
         try {
         	LOG.debug("updateing preference " + preferenceId + " for entity " +
         			entityId);
             PreferenceBL preference = new PreferenceBL();
             preference.createUpdateForEntity(entityId, preferenceId, paramInt, 
                     paramStr, paramFloat);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * Marks as deleted all the credit cards associated with this user
      * and removes the relationship
      * @ejb:interface-method view-type="remote"
      */
     public void deleteCreditCard(Integer executorId, Integer userId) 
             throws SessionInternalError {
         try {
             // find this user and get the first cc
             UserBL userBL = new UserBL(userId);
             Iterator it = userBL.getEntity().getCreditCards().iterator();
             while (it.hasNext()) {
                 CreditCardBL bl = new CreditCardBL(((CreditCard)
                         it.next()).getId());
                 bl.delete(executorId);
             } 
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      */
     public void setUserStatus(Integer executorId, Integer userId, 
             Integer statusId) 
             throws SessionInternalError {
         try {
             AgeingBL age = new AgeingBL();
             age.setUserStatus(executorId, userId, statusId, 
                     Calendar.getInstance().getTime());
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }   
     
 
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      */
     public String getWelcomeMessage(Integer entityId, Integer languageId, 
             Integer statusId) 
             throws SessionInternalError {
         String retValue;
         try {
             AgeingBL age = new AgeingBL();
             LOG.debug("Getting welcome message for " + entityId +
                     " language " + languageId + " status " + statusId);
             try {
                 retValue = age.getWelcome(entityId, languageId, statusId);
                 //log.debug("welcome = " + retValue);
             } catch (FinderException e1) {
                 LOG.warn("No message found. Looking for active status");
                 try {
                     retValue = age.getWelcome(entityId, languageId, 
                             UserDTOEx.STATUS_ACTIVE);
                 } catch (FinderException e2) {
                     LOG.warn("Using welcome default");
                     retValue = "Welcome!";
                 }
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return retValue;
     }   
     
     /**
     * Create the Session Bean
     *
     * @throws CreateException 
     *
     * @ejb:create-method view-type="remote"
     */
     public void ejbCreate() throws CreateException {
     }
 
     /**
     * Describes the instance and its content for debugging purpose
     *
     * @return Debugging information about the instance and its content
     */
     public String toString() {
         return "UserSessionBean [ " + " ]";
     }
 
     /**
      * This is the entry method for the payout batch. It goes over all the
      * partners with a next_payout_date <= today and calls the mthods to
      * process the payout
      * @param today
      * @ejb:interface-method view-type="remote"
      */
     public void processPayouts(Date today) 
             throws SessionInternalError {
         try {
             JNDILookup jndi = JNDILookup.getFactory();
             Connection conn = jndi.lookUpDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(duePayout);
             stmt.setDate(1, new java.sql.Date(today.getTime()));
             ResultSet result = stmt.executeQuery();
             // since esql doesn't support dates, a direct call is necessary
             while (result.next()) {
                 processPayout(new Integer(result.getInt(1)));
             }
             result.close();
             stmt.close();
             conn.close();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * 
      * @ejb.transaction type="RequiresNew"
      */
     public void processPayout(Integer partnerId) 
             throws SessionInternalError {
         try {
             LOG.debug("Processing partner " + partnerId);
             PartnerBL partnerBL = new PartnerBL();
             partnerBL.processPayout(partnerId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public PartnerPayout calculatePayout(Integer partnerId, Date start, 
             Date end, Integer currencyId) 
             throws SessionInternalError {
         try {
             PartnerBL partnerBL = new PartnerBL(partnerId);
             if (currencyId == null) {
                 // we default to this partners currency
                 currencyId = partnerBL.getEntity().getUser().getEntity().
                         getCurrencyId();
             }
             return partnerBL.calculatePayout(start, end, currencyId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Partner getPartnerDTO(Integer partnerId) 
             throws SessionInternalError, FinderException {
         try {
             PartnerBL partnerBL = new PartnerBL(partnerId);
             Partner retValue = partnerBL.getDTO();
             retValue.touch();
             return retValue;
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public PartnerPayout getPartnerLastPayoutDTO(Integer partnerId) 
             throws SessionInternalError {
         try {
             PartnerBL partnerBL = new PartnerBL();
             return partnerBL.getLastPayoutDTO(partnerId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public PartnerPayout getPartnerPayoutDTO(Integer payoutId) 
             throws SessionInternalError {
         try {
             PartnerBL partnerBL = new PartnerBL();
             partnerBL.setPayout(payoutId);
             return partnerBL.getPayoutDTO();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Date[] getPartnerPayoutDates(Integer partnerId) 
             throws SessionInternalError {
         try {
             PartnerBL partnerBL = new PartnerBL(partnerId);
             return partnerBL.calculatePayoutDates();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
     
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void notifyCreditCardExpiration(Date today) 
             throws SessionInternalError {
         try {
             Calendar cal = Calendar.getInstance();
             cal.setTime(today);
             if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                 CreditCardBL bl = new CreditCardBL();
                 bl.notifyExipration(today);
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public void setUserBlacklisted(Integer executorId, Integer userId, 
             Boolean isBlacklisted) throws SessionInternalError {
         try {
             UserBL bl = new UserBL(userId);
             bl.setUserBlacklisted(executorId, isBlacklisted);
         } catch(Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * @throws FinderException throws when no user with the specified username
      * @throws NamingException 
      * @throws NumberFormatException 
      * @throws CreateException 
      * @throws NotificationNotFoundException 
      * @throws SessionInternalError 
      * @ejb:interface-method view-type="remote"
      */
     public void sendLostPassword(String entityId, String username) throws NumberFormatException, NamingException,  SessionInternalError, NotificationNotFoundException, CreateException, FinderException {
     	UserBL user = new UserBL(username, Integer.valueOf(entityId));
 
 		user.sendLostPassword(Integer.valueOf(entityId), user.getEntity().getUserId(),  user.getEntity().getLanguageIdField());	
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      */
    public boolean isPasswordExpired(Integer userId) {
         UserBL user;
         user = new UserBL(userId);
         return user.isPasswordExpired();
     }
 
 
     // -------------------------------------------------------------------------
     // Framework Callbacks
     // -------------------------------------------------------------------------  
 
     public void setSessionContext(SessionContext aContext)
             throws EJBException {
         context = aContext;
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbActivate()
      */
     public void ejbActivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbPassivate()
      */
     public void ejbPassivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbRemove()
      */
     public void ejbRemove() throws EJBException, RemoteException {
     }
 
 }
