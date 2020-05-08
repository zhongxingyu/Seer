 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.server.user;
 

 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Vector;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.PermissionConstants;
 import com.sapienter.jbilling.common.PermissionIdComparator;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.interfaces.AchEntityLocal;
 import com.sapienter.jbilling.interfaces.CreditCardEntityLocal;
 import com.sapienter.jbilling.interfaces.CustomerEntityLocalHome;
 import com.sapienter.jbilling.interfaces.EntityEntityLocalHome;
 import com.sapienter.jbilling.interfaces.ItemUserPriceEntityLocal;
 import com.sapienter.jbilling.interfaces.LanguageEntityLocal;
 import com.sapienter.jbilling.interfaces.LanguageEntityLocalHome;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocalHome;
 import com.sapienter.jbilling.interfaces.OrderEntityLocal;
 import com.sapienter.jbilling.interfaces.PermissionEntityLocal;
 import com.sapienter.jbilling.interfaces.PermissionUserEntityLocal;
 import com.sapienter.jbilling.interfaces.ReportUserEntityLocal;
 import com.sapienter.jbilling.interfaces.RoleEntityLocal;
 import com.sapienter.jbilling.interfaces.RoleEntityLocalHome;
 import com.sapienter.jbilling.interfaces.UserEntityLocal;
 import com.sapienter.jbilling.interfaces.UserEntityLocalHome;
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.entity.PermissionDTO;
 import com.sapienter.jbilling.server.entity.UserDTO;
 import com.sapienter.jbilling.server.list.ResultList;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.payment.PaymentBL;
 import com.sapienter.jbilling.server.process.AgeingBL;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.DTOFactory;
 import com.sapienter.jbilling.server.util.EventLogger;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 
 
 /**
  * Business Logic class to assist login procedures, like authentication
  * 
  * @author emilc
  *
  */
 public class UserBL  extends ResultList 
         implements UserSQL {
     private JNDILookup EJBFactory = null;
     private UserEntityLocalHome userHome = null;
     private UserEntityLocal user = null;
     private Logger log = null;
     private EventLogger eLogger = null;
     private Integer mainRole = null;
     private CustomerEntityLocalHome customerHome = null;
     
     public UserBL(Integer userId) throws FinderException {
         init();
 
         set(userId);
     }
     
     public UserBL() throws NamingException {
         init();
     }
     
     public UserBL(UserEntityLocal entity) 
             throws NamingException{
         user = entity;
         init();
     }
     
     public UserBL(String username, Integer entityId) 
             throws NamingException, FinderException {
         init();
         user = userHome.findByUserName(username, entityId);
     }
     
     public void set(Integer userId) 
             throws FinderException {
         user = userHome.findByPrimaryKey(userId);
     }
     
     public void set(String userName, Integer entityId) 
             throws FinderException {
         user = userHome.findByUserName(userName, entityId);
     }
     
     public void set(UserEntityLocal user) {
         this.user = user;
     }
      
     public void setRoot(String userName) 
             throws FinderException {
         user = userHome.findRoot(userName);
     }
     
     public UserEntityLocalHome getHome() {
         return userHome;
     }
     
     private void init() {
         try {
             log = Logger.getLogger(UserBL.class);     
             eLogger = EventLogger.getInstance();        
             EJBFactory = JNDILookup.getFactory(false);
             userHome = (UserEntityLocalHome) EJBFactory.lookUpLocalHome(
                     UserEntityLocalHome.class,
                     UserEntityLocalHome.JNDI_NAME);
             customerHome = (CustomerEntityLocalHome) EJBFactory.lookUpLocalHome(
                     CustomerEntityLocalHome.class,
                     CustomerEntityLocalHome.JNDI_NAME);
         } catch (NamingException e) {
             throw new SessionInternalError("init UserBL", this.getClass(), e);
         }
     }
     
     /**
      * @param userId This is the user that has ordered the update
      * @param dto This is the user that will be updated 
      */
     public void update(Integer executorId, UserDTOEx dto) 
             throws NamingException, FinderException, SessionInternalError,
               CreateException, RemoveException {
         // password is the only one that might've not been set
         if (dto.getPassword() != null &&
                 !dto.getPassword().equals(user.getPassword())) {
             // if it is not the same user changing its password, I'll log it
             if (!executorId.equals(user.getUserId())) {
                 eLogger.audit(executorId, Constants.TABLE_BASE_USER, user.getUserId(),
                         EventLogger.MODULE_USER_MAINTENANCE, 
                         EventLogger.PASSWORD_CHANGE, null, user.getPassword(), 
                         null);
             }
             user.setPassword(dto.getPassword());
         }
         if (dto.getUserName() != null && !user.getUserName().equals(
         		dto.getUserName())) {
         	user.setUserName(dto.getUserName());
         }
         if (dto.getLanguageId() != null && !user.getLanguageIdField().equals(
         		dto.getLanguageId())) {
         		user.setLanguageId(dto.getLanguageId());
         }
         if (dto.getEntityId() != null && !user.getEntity().getId().equals(
         		dto.getEntityId())) {
 	        EntityEntityLocalHome entityHome = (EntityEntityLocalHome) 
 	                EJBFactory.lookUpLocalHome(EntityEntityLocalHome.class,
 	                EntityEntityLocalHome.JNDI_NAME);
 	        user.setEntity(entityHome.findByPrimaryKey(dto.getEntityId()));
         }
         if (dto.getStatusId() != null && !user.getStatus().getId().equals(
         		dto.getStatusId())) {
             AgeingBL age = new AgeingBL();
             age.setUserStatus(executorId, user.getUserId(), dto.getStatusId(), 
                     Calendar.getInstance().getTime());
         }
         if (dto.getCurrencyId() != null && !user.getCurrencyId().equals(
         		dto.getCurrencyId())) {
         	user.setCurrencyId(dto.getCurrencyId());
         }
         if (dto.getCustomerDto() != null) {
             if (dto.getCustomerDto().getInvoiceDeliveryMethodId() != null) {
                 user.getCustomer().setInvoiceDeliveryMethodId(
                         dto.getCustomerDto().getInvoiceDeliveryMethodId());
             }
             user.getCustomer().setDueDateUnitId(
                     dto.getCustomerDto().getDueDateUnitId());
             user.getCustomer().setDueDateValue(
                     dto.getCustomerDto().getDueDateValue());
             user.getCustomer().setDfFm(dto.getCustomerDto().getDfFm());
             if (dto.getCustomerDto().getPartnerId() != null) {
                 PartnerBL partner = new PartnerBL(dto.getCustomerDto().getPartnerId());
                 user.getCustomer().setPartner(partner.getEntity());    
             } else {
                 user.getCustomer().setPartner(null);
             }
             if (dto.getCustomerDto().getExcludeAging() != null) {
                 user.getCustomer().setExcludeAging(
                         dto.getCustomerDto().getExcludeAging());
             }
         }
         
         updateRoles(dto.getRoles(), dto.getMainRoleId());
     }
     
     private void updateRoles(Collection rolesIds, Integer main) 
             throws NamingException, SessionInternalError {    
 
         if (rolesIds == null || rolesIds.isEmpty()) {
             if (main != null) {
                 if (rolesIds == null) {
                     rolesIds = new Vector();
                 }
                 rolesIds.add(main);
             } else {
                 return; // nothing to do
             }
         }
         
         RoleEntityLocalHome roleHome = (RoleEntityLocalHome) 
                EJBFactory.lookUpLocalHome(
                RoleEntityLocalHome.class,
                RoleEntityLocalHome.JNDI_NAME);
 
         user.getRoles().clear();
         for (Iterator it = rolesIds.iterator(); it.hasNext();) {
              Integer roleId = (Integer) it.next();
              
              try {   
                 RoleEntityLocal role = roleHome.findByPrimaryKey(roleId);
                 user.getRoles().add(role);
              } catch (FinderException e) {
                  log.error("Trying to add unexisting role to user " + roleId, e);
                  throw new SessionInternalError("Invalid role " + roleId);
              }
         };        
                 
     }
     
     public boolean exists(String userName, Integer entityId) {
         if (userName == null) {
             log.error("exists is being call with a null username");
             return true;
         }
         try {
             userHome.findByUserName(userName, entityId);
             return true;
         } catch (FinderException e) {
             return false;
         }
     }
     
     public Integer create(UserDTOEx dto) throws CreateException,
             NamingException, SessionInternalError, RemoveException {
         
         Integer newId;
         log.debug("Creating user " + dto);
         if (dto.getRoles() == null || dto.getRoles().size() == 0) {
             if (dto.getMainRoleId() != null) {
                 Vector roles = new Vector();
                 roles.add(dto.getMainRoleId());
                 dto.setRoles(roles);
             } else {
                 log.warn("Creating user without any role...");
             }
         }
         // may be this is a partner
         if (dto.getPartnerDto() != null) {
             newId = create(dto.getEntityId(), dto.getUserName(), dto.getPassword(), 
             		dto.getLanguageId(), dto.getRoles(), dto.getCurrencyId(),
 					dto.getStatusId());
             PartnerBL partner = new PartnerBL();
             partner.create(dto.getPartnerDto());
             user.setPartner(partner.getEntity());
         } else if (dto.getCustomerDto() != null) {
             // link the partner
             try {
                 PartnerBL partner = null;
                 if (dto.getCustomerDto().getPartnerId() != null) {
                     partner = new PartnerBL(dto.getCustomerDto().
                             getPartnerId());
                     // see that this partner is valid
                     if (!partner.getEntity().getUser().getEntity().getId().
                             equals(dto.getEntityId()) || partner.getEntity().
                             getUser().getDeleted().intValue() == 1) {
                         partner = null;
                     }
                 }
                 newId = create(dto.getEntityId(), dto.getUserName(), 
                         dto.getPassword(), dto.getLanguageId(), 
                         dto.getRoles(), dto.getCurrencyId(),
 						dto.getStatusId());
                 user.setCustomer(customerHome.create());
                 user.getCustomer().setReferralFeePaid(dto.getCustomerDto().
                         getReferralFeePaid());
                 if (partner != null) {
                     user.getCustomer().setPartner(partner.getEntity());
                 }
                 // set the sub-account fields
                 user.getCustomer().setIsParent(
                         dto.getCustomerDto().getIsParent());
                 if (dto.getCustomerDto().getParentId() != null) {
                     UserBL parent = new UserBL(dto.getCustomerDto().getParentId());
                     user.getCustomer().setParent(parent.getEntity().getCustomer());
                     
                 }
             } catch (FinderException e) {
                 newId = null;
             }
         } else { // all the rest
             newId = create(dto.getEntityId(), dto.getUserName(), dto.getPassword(), 
                     dto.getLanguageId(), dto.getRoles(), dto.getCurrencyId(),
 					dto.getStatusId());
         }
         
         log.debug("created user id " + newId);
         
         return newId;
     }    
 
     private Integer create(Integer entityId, String userName, String password,
             Integer languageId, Vector roles, Integer currencyId, 
 			Integer statusId) 
             throws CreateException, NamingException, SessionInternalError {
         // Default the language and currency to that one of the entity         
         try {
 			if (languageId == null) {
 				EntityBL entity = new EntityBL(entityId);
 				languageId = entity.getEntity().getLanguageId();
 			}
 			if (currencyId == null) {
 				EntityBL entity = new EntityBL(entityId);
 				currencyId = entity.getEntity().getCurrencyId();
 			}
 		} catch (FinderException e) {
 			throw new SessionInternalError("entity not found when creating " +
 					" user: " + entityId);
 		}
 		
 		if (statusId == null) {
 			statusId = UserDTOEx.STATUS_ACTIVE;
 		}
     	user = userHome.create(entityId, userName, password, languageId, 
                 currencyId, statusId);
         updateRoles(roles, null);
         
         return user.getUserId();
     }    
         
     
     public boolean validateUserNamePassword(UserDTOEx loggingUser, 
            UserDTOEx db) throws FinderException, NamingException {
         // children accounts can not login. They have no invoices and
         // can't make any payments
         if (db.getCustomerDto() != null && 
                 db.getCustomerDto().getParentId() != null) {
             return false;
         }
         // the user status is not part of this check, as a customer that
         // can't login to the entity's service still has to be able to
         // login to sapienter to pay
         if (db.getDeleted().intValue() == 0 && loggingUser.getPassword().
                 compareTo(db.getPassword()) == 0 &&
                 loggingUser.getEntityId().equals(db.getEntityId())) {
             user = getUserEntity(db.getUserId());
             return true;
         }
         
         return false;
     }
      
      public static UserEntityLocal getUserEntity(Integer userId) 
             throws NamingException, FinderException {
          JNDILookup EJBFactory = JNDILookup.getFactory(false);
          UserEntityLocalHome UserHome =
                 (UserEntityLocalHome) EJBFactory.lookUpLocalHome(
                  UserEntityLocalHome.class,
                  UserEntityLocalHome.JNDI_NAME);
 
          UserEntityLocal user = UserHome.findByPrimaryKey(userId);
          return user;
          
      }
      
      /**
       * sent the lost password to the user
       * @param entityId
       * @param userId
       * @param languageId
       * @throws NamingException
       * @throws SessionInternalError
       * @throws FinderException
       * @throws NotificationNotFoundException when no message row or message row is not activated for the specified entity
       * @throws CreateException
       */
      public void sendLostPassword(Integer entityId, Integer userId, Integer languageId) throws NamingException, SessionInternalError, FinderException, NotificationNotFoundException, CreateException {
     	 NotificationBL notif = new NotificationBL();
     	 MessageDTO message = notif.getForgetPasswordEmailMessage(entityId, userId, languageId);
     	 NotificationSessionLocalHome notificationHome =
     		 (NotificationSessionLocalHome) EJBFactory.lookUpLocalHome(
     				 NotificationSessionLocalHome.class,
     				 NotificationSessionLocalHome.JNDI_NAME);
     	NotificationSessionLocal notificationSess = notificationHome.create();
     	notificationSess.notify(userId, message);
      }
      
      public UserEntityLocal getEntity() {
          return user;
      }
      
      public Vector getPermissions() { 
          Vector ret = new Vector();
 
          log.debug("Reading permisions for user " + user.getUserId());
          
          Collection roles = user.getRoles();
          for (Iterator it = roles.iterator(); it.hasNext();) {
              RoleEntityLocal role = (RoleEntityLocal) it.next();
              
              // now get the permissions
              Collection permissions = role.getPermissions();
              for (Iterator it2 = permissions.iterator(); it2.hasNext();) {
                  PermissionEntityLocal permission = 
                         (PermissionEntityLocal) it2.next();
                  ret.add(new PermissionDTO(permission.getId(), 
                         permission.getTypeId(), permission.getForeignId()));
                  //log.debug("Adding permission from role " + permission.getId());
              }
          }
          // get it sorted to allow binary searches ;)
          PermissionIdComparator comparator = new PermissionIdComparator();
          Collections.sort(ret, comparator);
          
          // now add / remove those privileges that were granted / revoked
          // to this particular user
          for(Iterator it = user.getPermissions().iterator(); it.hasNext();) {
              PermissionUserEntityLocal permission = (PermissionUserEntityLocal)
                      it.next();
              PermissionDTO thisPerm = new PermissionDTO();
              thisPerm.setId(permission.getPermission().getId());
              int idx = Collections.binarySearch(ret, thisPerm, comparator);
              if (permission.getIsGrant().intValue() == 1) {
                  // see that this guy has it
                  if (idx < 0) {
                      // not there, add it
                      //log.debug("adding " + thisPerm.getId());
                      ret.add(new PermissionDTO(permission.getPermission().getId(),
                              permission.getPermission().getTypeId(),
                              permission.getPermission().getForeignId()));
                      Collections.sort(ret, comparator);
                  }
              } else {
                  // make sure she doesn't
                  if (idx >= 0) {
                      //log.debug("removing " + thisPerm.getId());
                      ret.remove(idx);
                  }
              }
          }
                  
          return ret;
      }
      
     public Menu getMenu(Vector permissions) 
             throws NamingException, FinderException, SessionInternalError {
 
         Menu menu = new Menu();
         // this should be doable in EJB/QL !! :( :(
         log.debug("getting menu for user=" + user.getUserId());
 
         for (Iterator iPer = permissions.iterator(); iPer.hasNext();) {
             PermissionDTO permission = (PermissionDTO)
                     iPer.next();
             if (permission.getTypeId().equals(Constants.PERMISSION_TYPE_MENU)) {
                 // get the menu
                 MenuOption option = DTOFactory.getMenuOption(
                         permission.getForeignId(), 
                         user.getLanguageIdField());
                 if (specialMenuFilter(option.getId())) {
                     menu.addOption(option);
                 }
                 //log.debug("adding option " + option + " to menu");
             }
         }
         
         menu.init();
         
         return menu;
      }
     
     /**
      * Some menu options depend on more than a permission, like payment
      * types are on the entity's accepted methods.
      * @param menuOptionId
      * @return
      */
     private boolean specialMenuFilter(Integer menuOptionId) {
         boolean retValue = true;
         
         // this constants have to be in synch with the DB
         final int OPTION_SUB_ACCOUNTS = 78;
         final int OPTION_PAYMENT_CHEQUE = 24;
         final int OPTION_PAYMENT_CC = 25;
         final int OPTION_PAYMENT_ACH = 75;
         final int OPTION_PAYMENT_PAYPAL = 90;
         final int OPTION_CUSTOMER_CONTACT_EDIT = 13;
         final int OPTION_PLUG_IN_EDIT = 93;
         
         switch (menuOptionId.intValue()) {
         case OPTION_SUB_ACCOUNTS:
             // this one is only for parents
             if (user.getCustomer() == null || 
                     user.getCustomer().getIsParent() == null ||
                     user.getCustomer().getIsParent().intValue() == 0) {
                 retValue = false;
             }
             break;
         case OPTION_PAYMENT_CHEQUE:
             try {
                 PaymentBL payment = new PaymentBL();
                 retValue = payment.isMethodAccepted(user.getEntity().getId(), 
                         Constants.PAYMENT_METHOD_CHEQUE);
             } catch (Exception e) {
                 log.error("Exception ", e);
             }
             break;
         case OPTION_PAYMENT_ACH:
             try {
                 PaymentBL payment = new PaymentBL();
                 retValue = payment.isMethodAccepted(user.getEntity().getId(), 
                         Constants.PAYMENT_METHOD_ACH);
             } catch (Exception e) {
                 log.error("Exception ", e);
             }
             break;
         case OPTION_PAYMENT_CC:
             try {
                 PaymentBL payment = new PaymentBL();
                 retValue = payment.isMethodAccepted(user.getEntity().getId(), 
                         Constants.PAYMENT_METHOD_AMEX) ||
                         payment.isMethodAccepted(user.getEntity().getId(), 
                                 Constants.PAYMENT_METHOD_VISA) ||
                         payment.isMethodAccepted(user.getEntity().getId(), 
                                 Constants.PAYMENT_METHOD_MASTERCARD) ||
                         payment.isMethodAccepted(user.getEntity().getId(), 
                                 Constants.PAYMENT_METHOD_DINERS) ||
                         payment.isMethodAccepted(user.getEntity().getId(), 
                                 Constants.PAYMENT_METHOD_DISCOVERY);
             } catch (Exception e) {
                 log.error("Exception ", e);
             }
             break;
         case OPTION_PAYMENT_PAYPAL:
             try {
                 PaymentBL payment = new PaymentBL();
                 retValue = payment.isMethodAccepted(user.getEntity().getId(), 
                         Constants.PAYMENT_METHOD_PAYPAL);
             } catch (Exception e) {
                 log.error("Exception ", e);
             }
             break;
         case OPTION_CUSTOMER_CONTACT_EDIT:
             PreferenceBL preference = null;
             try {
                 preference = new PreferenceBL();
                 preference.set(user.getEntity().getId(), 
                         Constants.PREFERENCE_CUSTOMER_CONTACT_EDIT);
                 retValue = (preference.getInt() == 1);
             } catch (FinderException e) {
                 // It doesn't matter, I will take the default
             } catch (Exception e) {
                 log.error("Exception ", e);
             } 
             
             retValue = (preference.getInt() == 1);
             break;
         case OPTION_PLUG_IN_EDIT:
             UserDTOEx dto = new UserDTOEx();
             dto.setPermissions(getPermissions());
             retValue = dto.isGranted(PermissionConstants.P_TASK_MODIFY);
             break;
         }
         
         return retValue;
     }
 
     public UserWS getUserWS() 
             throws NamingException, FinderException, SessionInternalError {
         UserDTOEx dto = DTOFactory.getUserDTOEx(user);
         UserWS retValue = new UserWS(dto);
         // the contact is not included in the Ex
         ContactBL bl= new ContactBL();
         
         try {
             bl.set(dto.getUserId());
             retValue.setContact(new ContactWS(bl.getDTO()));
         } catch (FinderException e) {
             // this user has no contact ...
         }
         
         return retValue;
     }
     
     public Integer getMainRole() {
         
         if (mainRole == null) {
             // the main role is the smallest of them, so they have to be ordered in the
             // db in ascending order (small = important);
             for (Iterator it = user.getRoles().iterator(); it.hasNext(); ) {
                 RoleEntityLocal role = (RoleEntityLocal) it.next();
                 if (mainRole == null || role.getId().compareTo(mainRole) < 0) {
                     mainRole = role.getId();
                 }
             }
         }
 
         return mainRole;
     }
 
     public Locale getLocale() 
             throws NamingException, FinderException {
         Locale retValue = null;
         // get the language first
         Integer languageId = user.getLanguageIdField();
         LanguageEntityLocalHome languageHome = (LanguageEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(LanguageEntityLocalHome.class,
                         LanguageEntityLocalHome.JNDI_NAME);
         LanguageEntityLocal language = languageHome
                 .findByPrimaryKey(languageId);
         String languageCode = language.getCode();
 
         // now the country
         ContactBL contact = new ContactBL();
         contact.set(user.getUserId());
         String countryCode = contact.getEntity().getCountryCode();
 
         if (countryCode != null) {
             retValue = new Locale(languageCode, countryCode);
         } else {
             retValue = new Locale(languageCode);
         }
 
         return retValue;
     }
 
     public Integer getCurrencyId() {
         Integer retValue;
         
         if (user.getCurrencyId() == null) {
             retValue = user.getEntity().getCurrencyId();
         } else {
             retValue = user.getCurrencyId();
         }
         
         return retValue;
     }
 
     /**
      * Will mark the user as deleted (deleted = 1), and do the same
      * with all her invoices and orders, etc ...
      * Not deleted for reporting reasong: invoices, payments
      */
     public void delete(Integer executorId) 
            throws RemoveException{
         Integer deleted = new Integer(1);
         user.setDeleted(deleted);
        user.setStatusId(UserDTOEx.STATUS_DELETED);
        user.setLastStatusChange(Calendar.getInstance().getTime());
         
         // credit cards
         for (Iterator it = user.getCreditCard().iterator(); it.hasNext();) {
             CreditCardEntityLocal cc =  (CreditCardEntityLocal) it.next();
             cc.setDeleted(deleted);
         }
         // item prices
         for (Iterator it = user.getItemPrices().iterator(); it.hasNext();) {
             ItemUserPriceEntityLocal itemPrice =  
                 (ItemUserPriceEntityLocal) it.next();
             itemPrice.remove();
             // since the collection has been modified by the remove, we
             // need to refresh it or we'll get an Exception
             it = user.getItemPrices().iterator();
         }
         // orders
         for (Iterator it = user.getOrders().iterator(); it.hasNext();) {
             OrderEntityLocal order =  (OrderEntityLocal) it.next();
             order.setDeleted(deleted);
         }
         // permisions
         user.getPermissions().clear();
         // promotions
         user.getPromotions().clear();
         // user saved reports
         for (Iterator it = user.getReports().iterator(); it.hasNext();) {
             ReportUserEntityLocal report =  (ReportUserEntityLocal) it.next();
             report.remove();
         }
         // roles
         user.getRoles().clear();
 
         if (executorId != null) {
             eLogger.audit(executorId, Constants.TABLE_BASE_USER, user.getUserId(),
                     EventLogger.MODULE_USER_MAINTENANCE, 
                     EventLogger.ROW_DELETED, null, null, null);
         }
 
     }
  
     public UserDTO getDto() {
         return new UserDTO(user.getUserId(), user.getUserName(), 
                 user.getPassword(), user.getDeleted(), 
                 user.getLanguageIdField(), user.getCurrencyId(),
                 user.getCreateDateTime(), user.getLastStatusChange(),
                 user.getLastLogin());
     }   
     /**
      * Verifies that both user belong to the same entity. 
      * @param rootUserName 
      *  This has to be a root user
      * @param callerUserId
      * @return
      */    
     public boolean validateUserBelongs(String rootUserName, 
             Integer callerUserId) 
             throws SessionInternalError {
                 
         boolean retValue;
         try {
             user = userHome.findByPrimaryKey(callerUserId);
             set(rootUserName, user.getEntity().getId());
             if (user.getDeleted().equals(new Integer(1))) {
                 throw new SessionInternalError("the caller is set as deleted");
             }
             if (!getMainRole().equals(Constants.TYPE_ROOT)) {
                 throw new SessionInternalError("can't validate but root users");
             }
             retValue = true;
         } catch (FinderException e) {
             retValue = false;
         }
         
         return retValue;
     }
     
     public void updateAch(AchDTO ach, Integer executorId)
     		throws NamingException, CreateException, SessionInternalError {
     	AchBL bl = new AchBL();
     	// let's see if this guy already has an ach record
     	AchEntityLocal row = user.getAch();
     	if (row == null) {
     		bl.create(ach);
     		user.setAch(bl.getEntity());
     	} else { // its an update
     		bl.set(row);
     		bl.update(executorId, ach);
     	}
     }
     
     public static boolean validate(UserWS userWS) {
         return validate(new UserDTOEx(userWS, null));
     }
     
     /**
      * Validates the user info and the credit card if present
      * @param dto
      * @return
      */
     public static boolean validate(UserDTOEx dto) {
         boolean retValue = true;
         
         if (dto == null || dto.getUserName() == null ||
                 dto.getPassword() == null || dto.getLanguageId() == null ||
                 dto.getMainRoleId() == null || dto.getStatusId() == null) {
             retValue = false;
             Logger.getLogger(UserBL.class).debug("invalid " + dto);
         } else if (dto.getCreditCard() != null) {
             retValue = CreditCardBL.validate(dto.getCreditCard());
         }
         
         return retValue;
     }
     
     public UserWS[] convertEntitiesToWS(Collection dtos) 
             throws SessionInternalError {
         try {
             UserWS[] ret = new UserWS[dtos.size()];
             int index = 0;
             for (Iterator it = dtos.iterator(); it.hasNext();) {
                 user = (UserEntityLocal) it.next();
                 ret[index] = entity2WS();
                 index++;
             }
             
             return ret;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     public UserWS entity2WS() 
             throws NamingException, FinderException {
         UserWS retValue = new UserWS();
         retValue.setCreateDateTime(user.getCreateDateTime());
         retValue.setCurrencyId(getCurrencyId());
         retValue.setDeleted(user.getDeleted());
         retValue.setLanguageId(user.getLanguageIdField());
         retValue.setLastLogin(user.getLastLogin());
         retValue.setLastStatusChange(user.getLastStatusChange());
         mainRole = null;
         retValue.setMainRoleId(getMainRole());
         if (user.getPartner() != null) {
             retValue.setPartnerId(user.getPartner().getId());
         }
         retValue.setPassword(user.getPassword());
         retValue.setStatusId(user.getStatus().getId());
         retValue.setUserId(user.getUserId());
         retValue.setUserName(user.getUserName());
         // now the contact
         ContactBL contact = new ContactBL();
         contact.set(retValue.getUserId());
         retValue.setContact(new ContactWS(contact.getDTO()));
         // the credit card
         Collection ccs = user.getCreditCard();
         if (ccs.size() > 0) {
             CreditCardBL cc = new CreditCardBL((CreditCardEntityLocal) 
                     ccs.toArray()[0]);
             retValue.setCreditCard(cc.getDTO());
         }
         return retValue;
     }
     
     public CachedRowSet findActiveWithOpenInvoices() 
             throws SQLException, NamingException {
         prepareStatement(UserSQL.findActiveWithOpenInvoices);
         execute();
         conn.close();
         return cachedResults;
     }
 }
