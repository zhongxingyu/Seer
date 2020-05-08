 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 /*
  * Created on Jan 27, 2005
  * One session bean to expose as a single web service, thus, one wsdl
  */
 package com.sapienter.jbilling.server.util;
 
 
 import com.sapienter.jbilling.client.authentication.CompanyUserDetails;
 import com.sapienter.jbilling.common.InvalidArgumentException;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.IInvoiceSessionBean;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.item.*;
 import com.sapienter.jbilling.server.item.db.*;
 import com.sapienter.jbilling.server.mediation.IMediationSessionBean;
 import com.sapienter.jbilling.server.mediation.MediationConfigurationBL;
 import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
 import com.sapienter.jbilling.server.mediation.MediationProcessWS;
 import com.sapienter.jbilling.server.mediation.MediationRecordBL;
 import com.sapienter.jbilling.server.mediation.MediationRecordLineWS;
 import com.sapienter.jbilling.server.mediation.MediationRecordWS;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.mediation.RecordCountWS;
 import com.sapienter.jbilling.server.mediation.db.MediationConfiguration;
 import com.sapienter.jbilling.server.mediation.db.MediationProcess;
 import com.sapienter.jbilling.server.mediation.db.MediationProcessDAS;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordDAS;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDAS;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDAS;
 import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDTO;
 import com.sapienter.jbilling.server.mediation.task.IMediationProcess;
 import com.sapienter.jbilling.server.mediation.task.MediationResult;
 import com.sapienter.jbilling.server.metafields.MetaFieldBL;
 import com.sapienter.jbilling.server.metafields.db.EntityType;
 import com.sapienter.jbilling.server.notification.INotificationSessionBean;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.order.IOrderSessionBean;
 import com.sapienter.jbilling.server.order.OrderBL;
 import com.sapienter.jbilling.server.order.OrderHelper;
 import com.sapienter.jbilling.server.order.OrderLineBL;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderPeriodWS;
 import com.sapienter.jbilling.server.order.OrderProcessWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.order.TimePeriod;
 import com.sapienter.jbilling.server.order.db.OrderDAS;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDAS;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
 import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
 import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentBL;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentWS;
 import com.sapienter.jbilling.server.payment.db.PaymentDAS;
 import com.sapienter.jbilling.server.payment.db.PaymentDTO;
 import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
 import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDAS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
 import com.sapienter.jbilling.server.pricing.RateCardBL;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDAS;
 import com.sapienter.jbilling.server.pricing.db.RateCardDTO;
 import com.sapienter.jbilling.server.pricing.db.RateCardWS;
 import com.sapienter.jbilling.server.process.*;
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDAS;
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
 import com.sapienter.jbilling.server.provisioning.IProvisioningProcessSessionBean;
 import com.sapienter.jbilling.server.rule.task.IRulesGenerator;
 import com.sapienter.jbilling.server.user.*;
 import com.sapienter.jbilling.server.user.contact.db.ContactTypeDAS;
 import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO;
 import com.sapienter.jbilling.server.user.db.AchDTO;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.user.db.CreditCardDAS;
 import com.sapienter.jbilling.server.user.db.CreditCardDTO;
 import com.sapienter.jbilling.server.user.db.CustomerDTO;
 import com.sapienter.jbilling.server.user.db.CustomerPriceDTO;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.partner.PartnerBL;
 import com.sapienter.jbilling.server.user.partner.PartnerWS;
 import com.sapienter.jbilling.server.user.partner.db.Partner;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import com.sapienter.jbilling.server.util.db.*;
 import grails.plugins.springsecurity.SpringSecurityService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.naming.NamingException;
 import javax.sql.rowset.CachedRowSet;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.util.*;
 
 @Transactional( propagation = Propagation.REQUIRED )
 public class WebServicesSessionSpringBean implements IWebServicesSessionBean {
     private static final Logger LOG = Logger.getLogger(WebServicesSessionSpringBean.class);
 
     private SpringSecurityService springSecurityService;
 
     public SpringSecurityService getSpringSecurityService() {
         if (springSecurityService == null)
             this.springSecurityService = Context.getBean(Context.Name.SPRING_SECURITY_SERVICE);
         return springSecurityService;
     }
 
     public void setSpringSecurityService(SpringSecurityService springSecurityService) {
         this.springSecurityService = springSecurityService;
     }
 
     /*
      * Returns the user ID of the authenticated user account making the web service call.
      *
      * @return caller user ID
      */
     public Integer getCallerId() {
         CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
         return details.getUserId();
     }
 
     /**
      * Returns the company ID of the authenticated user account making the web service call.
      *
      * @return caller company ID
      */
     public Integer getCallerCompanyId() {
         CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
         return details.getCompanyId();
     }
 
     /**
      * Returns the language ID of the authenticated user account making the web service call.
      *
      * @return caller language ID
      */
     public Integer getCallerLanguageId() {
         CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
         return details.getLanguageId();
     }
     
     /**
      * Returns the currency ID of the authenticated user account making the web service call.
      *
      * @return caller currency ID
      */
     public Integer getCallerCurrencyId() {
         CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
         return details.getCurrencyId();
     }
 
 
     // todo: reorganize methods and reformat code. should match the structure of the interface to make things readable.
 
 
     /*
         Invoices
      */
 
     public InvoiceWS getInvoiceWS(Integer invoiceId)
             throws SessionInternalError {
         if (invoiceId == null) {
             return null;
         }
         InvoiceDTO invoice = new InvoiceDAS().find(invoiceId);
 
         if (invoice.getDeleted() == 1) {
             return null;
         }
 
         InvoiceWS wsDto= InvoiceBL.getWS(invoice);
         if ( null != invoice.getInvoiceStatus())
         {
         	wsDto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));
         }
         return wsDto;
     }
 
     public InvoiceWS[] getAllInvoicesForUser(Integer userId) {
         IInvoiceSessionBean invoiceBean = Context.getBean(Context.Name.INVOICE_SESSION);
         Set<InvoiceDTO> invoices = invoiceBean.getAllInvoices(userId);
 
         List<InvoiceWS> ids = new ArrayList<InvoiceWS>(invoices.size());
         for (InvoiceDTO invoice : invoices)
         {
         	InvoiceWS wsdto= InvoiceBL.getWS(invoice);
         	if ( null != invoice.getInvoiceStatus())
         		wsdto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));
 
         	ids.add(wsdto);
         }
         return ids.toArray(new InvoiceWS[ids.size()]);
     }
 
     public InvoiceWS[] getAllInvoices() {
 
         List<InvoiceDTO> invoices = new InvoiceDAS().findAll();
 
         List<InvoiceWS> ids = new ArrayList<InvoiceWS>(invoices.size());
         for (InvoiceDTO invoice : invoices)
         {
         	InvoiceWS wsdto= InvoiceBL.getWS(invoice);
         	if ( null != invoice.getInvoiceStatus())
         		wsdto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));
 
         	ids.add(wsdto);
         }
         return ids.toArray(new InvoiceWS[ids.size()]);
     }
 
     public boolean notifyInvoiceByEmail(Integer invoiceId) {
     	INotificationSessionBean notificationSession =
 	            (INotificationSessionBean) Context.getBean(
 	            Context.Name.NOTIFICATION_SESSION);
         
         boolean emailInvoice;
         try{
             emailInvoice = notificationSession.emailInvoice(invoiceId); 
         } catch (Exception e){
             LOG.warn("Exception in web service: notifying invoice by email "
                     + e);
             emailInvoice = false;
         }
         return emailInvoice;
     }
 
     public boolean notifyPaymentByEmail(Integer paymentId) {
         INotificationSessionBean notificationSession =
                 (INotificationSessionBean) Context.getBean(
                 Context.Name.NOTIFICATION_SESSION);
         return notificationSession.emailPayment(paymentId);
     }
 
     public Integer[] getAllInvoices(Integer userId) {
         IInvoiceSessionBean invoiceBean = Context.getBean(Context.Name.INVOICE_SESSION);
         Set<InvoiceDTO> invoices = invoiceBean.getAllInvoices(userId);
 
         List<Integer> ids = new ArrayList<Integer>(invoices.size());
         for (InvoiceDTO invoice : invoices)
             ids.add(invoice.getId());
         return ids.toArray(new Integer[ids.size()]);
     }
 
     public InvoiceWS getLatestInvoice(Integer userId)
             throws SessionInternalError {
         InvoiceWS retValue = null;
         try {
             if (userId == null) {
                 return null;
             }
             InvoiceBL bl = new InvoiceBL();
             Integer invoiceId = bl.getLastByUser(userId);
             if (invoiceId != null) {
                 retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
             }
             return retValue;
         } catch (Exception e) { // needed because the sql exception :(
             LOG.error("Exception in web service: getting latest invoice" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting latest invoice");
         }
     }
 
     public Integer[] getLastInvoices(Integer userId, Integer number)
             throws SessionInternalError {
         if (userId == null || number == null) {
             return null;
         }
 
         InvoiceBL bl = new InvoiceBL();
         return bl.getManyWS(userId, number);
     }
 
     public Integer[] getInvoicesByDate(String since, String until)
             throws SessionInternalError {
         try {
             Date dSince = com.sapienter.jbilling.common.Util.parseDate(since);
             Date dUntil = com.sapienter.jbilling.common.Util.parseDate(until);
             if (since == null || until == null) {
                 return null;
             }
 
             Integer entityId = getCallerCompanyId();
 
             InvoiceBL invoiceBl = new InvoiceBL();
             return invoiceBl.getInvoicesByCreateDateArray(entityId, dSince, dUntil);
         } catch (Exception e) { // needed for the SQLException :(
             LOG.error("Exception in web service: getting invoices by date" +
                     since + until, e);
             throw new SessionInternalError("Error getting last invoices");
         }
     }
 
     /**
      * Returns the invoices for the user within the given date range.
      */
     public Integer[] getUserInvoicesByDate(Integer userId, String since,
             String until) throws SessionInternalError {
         if (userId == null || since == null || until == null) {
             return null;
         }
 
         Date dSince = com.sapienter.jbilling.common.Util.parseDate(since);
         Date dUntil = com.sapienter.jbilling.common.Util.parseDate(until);
 
         InvoiceBL invoiceBl = new InvoiceBL();
 
         Integer[] results = invoiceBl.getUserInvoicesByDate(userId, dSince,
                 dUntil);
 
         return results;
     }
 
     /**
      * Returns an array of IDs for all unpaid invoices under the given user ID.
      *
      * @param userId user IDs
      * @return array of un-paid invoice IDs
      */
     public Integer[] getUnpaidInvoices(Integer userId) {
         try {
             CachedRowSet rs = new InvoiceBL().getPayableInvoicesByUser(userId);
 
             Integer[] invoiceIds = new Integer[rs.size()];
             int i = 0;
             while (rs.next())
                 invoiceIds[i++] = rs.getInt(1);
 
             rs.close();
             return invoiceIds;
 
         } catch (SQLException e) {
             throw new SessionInternalError("Exception occurred querying payable invoices.");
         } catch (Exception e) {
             throw new SessionInternalError("An un-handled exception occurred querying payable invoices.");
         }
     }
 
     /**
      * Generates and returns the paper invoice PDF for the given invoiceId.
      *
      * @param invoiceId invoice to generate PDF for
      * @return PDF invoice bytes
      * @throws SessionInternalError
      */
     public byte[] getPaperInvoicePDF(Integer invoiceId) throws SessionInternalError {
         IInvoiceSessionBean invoiceSession = (IInvoiceSessionBean) Context.getBean(Context.Name.INVOICE_SESSION);
         return invoiceSession.getPDFInvoice(invoiceId);
     }
 
     /**
      * Un-links a payment from an invoice, effectivley making the invoice "unpaid" by
      * removing the payment balance.
      *
      * If either invoiceId or paymentId parameters are null, no operation will be performed.
      *
      * @param invoiceId target Invoice
      * @param paymentId payment to be unlink
      */
     public void removePaymentLink(Integer invoiceId, Integer paymentId) {
 		if (invoiceId == null || paymentId == null)
             return;
 
         // check if the payment is a refund , if it is do not allow it
         if(new PaymentBL(paymentId).getEntity().getIsRefund()==1) {
             LOG.debug("This payment id "+paymentId+" is a refund so we cannot unlink it from the invoice");
             throw new SessionInternalError("This payment is a refund and hence cannot be unlinked from any invoice",
                         new String[] {"PaymentWS,unlink,validation.error.payment.unlink"});
         }
 
         // if the payment has been refunded
         if(PaymentBL.ifRefunded(paymentId)) {
             throw new SessionInternalError("This payment has been refunded and hence cannot be unlinked from the invoice",
                         new String[] {"PaymentWS,unlink,validation.error.delete.refunded.payment"});
         }
 
         boolean result= new PaymentBL(paymentId).unLinkFromInvoice(invoiceId);
         if (!result)
 			throw new SessionInternalError("Unable to find the Invoice Id " + invoiceId + " linked to Payment Id " + paymentId);
 	}
 
     /**
      * Applies an existing payment to an invoice.
      *
      * If either invoiceId or paymentId parameters are null, no operation will be performed.
      *
      * @param invoiceId target invoice
      * @param paymentId payment to apply
      */
     public void createPaymentLink(Integer invoiceId, Integer paymentId) {
         IPaymentSessionBean session = Context.getBean(Context.Name.PAYMENT_SESSION);
         session.applyPayment(paymentId, invoiceId);
     }
 
     /**
      * Deletes an invoice
      * @param invoiceId
      * The id of the invoice to delete
      */
     public void deleteInvoice(Integer invoiceId) {
         IInvoiceSessionBean session = Context.getBean(Context.Name.INVOICE_SESSION);
         session.delete(invoiceId, getCallerId());
     }
 
     /**
      * Deletes an Item
      * @param itemId
      * The id of the item to delete
      */
     public void deleteItem(Integer itemId) throws SessionInternalError {
         IItemSessionBean itemSession = (IItemSessionBean) Context.getBean(Context.Name.ITEM_SESSION);
         itemSession.delete(getCallerId(), itemId);
     	LOG.debug("Deleted Item Id " + itemId);
     }
 
     /**
      * Deletes an Item Category
      * @param itemCategoryId
      * The id of the Item Category to delete
      */
     public void deleteItemCategory(Integer itemCategoryId) throws SessionInternalError {
 
     	ItemTypeBL bl = new ItemTypeBL(itemCategoryId);
 		bl.delete(getCallerId());
     }
 
     /**
      * Generates invoices for orders not yet invoiced for this user.
      * Optionally only allow recurring orders to generate invoices.
      * Returns the ids of the invoices generated.
      */
     public Integer[] createInvoice(Integer userId, boolean onlyRecurring) {
         return createInvoiceWithDate(userId, null, null, null, onlyRecurring);
     }
 
 
     /**
      * Generates an invoice for a customer using an explicit billing date & due date period.
      *
      * If the billing date is left blank, the invoice will be generated for today.
      *
      * If the due date period unit or value is left blank, then the due date will be calculated from the
      * order period, or from the customer due date period if set.
      *
      * @param userId user id to generate an invoice for.
      * @param billingDate billing date for the invoice generation run
      * @param dueDatePeriodId due date period unit
      * @param dueDatePeriodValue due date period value
      * @param onlyRecurring only include recurring orders? false to include all orders in invoice.
      * @return array of generated invoice ids.
      */
     public Integer[] createInvoiceWithDate(Integer userId, Date billingDate, Integer dueDatePeriodId,
                                            Integer dueDatePeriodValue, boolean onlyRecurring) {
 
         UserDTO user = new UserDAS().find(userId);
         BillingProcessConfigurationDTO config = new BillingProcessConfigurationDAS().findByEntity(user.getCompany());
 
         // Create a mock billing process object, because the method
         // we are calling was meant to be called by the billing process.
         BillingProcessDTO billingProcess = new BillingProcessDTO();
         billingProcess.setId(0);
         billingProcess.setEntity(user.getCompany());
         billingProcess.setBillingDate(billingDate != null ? billingDate : new Date());
         billingProcess.setPeriodUnit(config.getPeriodUnit());
         billingProcess.setPeriodValue(config.getPeriodValue());
         billingProcess.setIsReview(0);
         billingProcess.setRetriesToDo(0);
 
         // optional target due date
         TimePeriod dueDatePeriod = null;
         if (dueDatePeriodId != null && dueDatePeriodValue != null) {
             dueDatePeriod = new TimePeriod();
             dueDatePeriod.setUnitId(dueDatePeriodId);
             dueDatePeriod.setValue(dueDatePeriodValue);
             LOG.debug("Using provided due date " + dueDatePeriod);
         }
 
         // generate invoices
         InvoiceDTO[] invoices = new BillingProcessBL().generateInvoice(billingProcess, dueDatePeriod,
                                                                        user, false, onlyRecurring, getCallerId());
 
         // generate invoices should return an empty array instead of null... bad design :(
         if (invoices == null)
             return new Integer[0];
 
         // build the list of generated ID's and return
         List<Integer> invoiceIds = new ArrayList<Integer>(invoices.length);
         for (InvoiceDTO invoice : invoices) {
             invoiceIds.add(invoice.getId());
         }
         return invoiceIds.toArray(new Integer[invoiceIds.size()]);
     }
 
 
 
     public Integer applyOrderToInvoice(Integer orderId, InvoiceWS invoiceWs) {
         if (orderId == null) throw new SessionInternalError("Order id cannot be null.");
 
         // validate order to be processed
         OrderDTO order = new OrderDAS().find(orderId);
         if (order == null || !Constants.ORDER_STATUS_ACTIVE.equals(order.getStatusId())) {
             LOG.debug("Order must exist and be active to generate an invoice.");
             return null;
         }
 
         // create an invoice template that contains the meta field values
         NewInvoiceDTO template = new NewInvoiceDTO();
         MetaFieldBL.fillMetaFieldsFromWS(getCallerCompanyId(), template, invoiceWs.getMetaFields());
 
         LOG.debug("Updating invoice with order: " + orderId);
         LOG.debug("Invoice WS: " + invoiceWs);
         LOG.debug("Invoice template fields: " + template.getMetaFields());
 
         // update the invoice
         try {
             BillingProcessBL process = new BillingProcessBL();
             InvoiceDTO invoice = process.generateInvoice(order.getId(), invoiceWs.getId(), template, getCallerId());
             return invoice != null ? invoice.getId() : null;
 
         } catch (SessionInternalError e) {
             throw e;
 
         } catch (Exception e) {
             throw new SessionInternalError("Error while generating a new invoice", e);
         }
     }
 
     /**
      * Generates a new invoice for an order, or adds the order to an
      * existing invoice.
      *
      * @param orderId order id to generate an invoice for
      * @param invoiceId optional invoice id to add the order to. If null, a new invoice will be created.
      * @return id of generated invoice, null if no invoice generated.
      * @throws SessionInternalError if user id or order id is null.
      */
     public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId) throws SessionInternalError {
         if (orderId == null) throw new SessionInternalError("Order id cannot be null.");
 
         // validate order to be processed
         OrderDTO order = new OrderDAS().find(orderId);
         if (order == null || !Constants.ORDER_STATUS_ACTIVE.equals(order.getStatusId())) {
             LOG.debug("Order must exist and be active to generate an invoice.");
             return null;
         }
 
         // create new invoice, or add to an existing invoice
         InvoiceDTO invoice;
         if (invoiceId == null) {
             LOG.debug("Creating a new invoice for order " + order.getId());
             invoice = doCreateInvoice(order.getId());
             if ( null == invoice) {
             	throw new SessionInternalError("Invoice could not be generated. The purchase order may not have any applicable periods to be invoiced.");
             }
         } else {
             LOG.debug("Adding order " + order.getId() + " to invoice " + invoiceId);
             IBillingProcessSessionBean process = (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
             invoice = process.generateInvoice(order.getId(), invoiceId, null, getCallerId());
         }
 
         return invoice == null ? null : invoice.getId();
     }
 
     /*
      * USERS
      */
     /**
      * Creates a new user. The user to be created has to be of the roles customer
      * or partner.
      * The username has to be unique, otherwise the creating won't go through. If
      * that is the case, the return value will be null.
      * @param newUser
      * The user object with all the information of the new user. If contact or
      * credit card information are present, they will be included in the creation
      * although they are not mandatory.
      * @return The id of the new user, or null if non was created
      */
     public Integer createUser(UserWS newUser)
             throws SessionInternalError {
 
 //        validateUser(newUser);
         newUser.setUserId(0);
 
         Integer entityId = getCallerCompanyId();
         UserBL bl = new UserBL();
 
         if (bl.exists(newUser.getUserName(), entityId)) {
             throw new SessionInternalError("User already exists with username " + newUser.getUserName(),
                                             new String[] { "UserWS,userName,validation.error.user.already.exists" });
         }
 
         ContactBL cBl = new ContactBL();
         UserDTOEx dto = new UserDTOEx(newUser, entityId);
         Integer userId = bl.create(dto, getCallerId());
         if (newUser.getContact() != null) {
             newUser.getContact().setId(0);
             cBl.createPrimaryForUser(new ContactDTOEx(newUser.getContact()), userId, entityId, getCallerId());
         }
 
         if (newUser.getCreditCard() != null) {
             CreditCardDTO card = new CreditCardDTO(newUser.getCreditCard()); // new CreditCardDTO
             card.setId(0);
             card.getBaseUsers().add(bl.getEntity());
 
             CreditCardBL ccBL = new CreditCardBL();
             ccBL.create(card);
 
             UserDTO userD = new UserDAS().find(userId);
             userD.getCreditCards().add(ccBL.getEntity());
         }
 
         if (newUser.getAch() != null) {
             AchDTO ach = new AchDTO(newUser.getAch());
             ach.setId(0);
             ach.setBaseUser(bl.getEntity());
             AchBL abl = new AchBL();
             abl.create(ach);
         }
         return userId;
     }
 
     public void deleteUser(Integer userId) throws SessionInternalError {
         UserBL bl = new UserBL();
         Integer executorId = getCallerId();
         bl.set(userId);
         bl.delete(executorId);
     }
 
     /**
      * Returns true if a user exists with the given user name, false if not.
      *
      * @param userName user name
      * @return true if user exists, false if not.
      */
     public boolean userExistsWithName(String userName) {
         return new UserBL().exists(userName, getCallerCompanyId());
     }
 
     /**
      * Returns true if a user with the given ID exists and is accessible by the caller, false if not.
      *
      * @param userId user id
      * @return true if user exists, false if not.
      */
     public boolean userExistsWithId(Integer userId) {
         return new UserBL().exists(userId, getCallerCompanyId());
     }
 
     /**
      * Fetches the ContactTypeWS for the given contact type ID. The returned WS object
      * contains a list of international descriptions for all available languages.
      *
      * @param contactTypeId contact type ID
      * @return contact type WS object
      * @throws SessionInternalError
      */
     public ContactTypeWS getContactTypeWS(Integer contactTypeId) throws SessionInternalError {
         ContactTypeDTO contactType = new ContactTypeDAS().find(contactTypeId);
         List<LanguageDTO> languages = new LanguageDAS().findAll();
 
         return new ContactTypeWS(contactType, languages);
     }
 
     /**
      * Creates a new contact type from the given WS object. This method also stores the international
      * description for each description/language in the WS object.
      *
      * @param contactType contact type WS
      * @return ID of created contact type
      * @throws SessionInternalError
      */
     public Integer createContactTypeWS(ContactTypeWS contactType) throws SessionInternalError {
         ContactTypeDTO dto = new ContactTypeDTO();
         dto.setEntity(new CompanyDTO(getCallerCompanyId()));
         dto.setIsPrimary(contactType.getPrimary());
 
         ContactTypeDAS contactTypeDas = new ContactTypeDAS();
         dto = contactTypeDas.save(dto);
 
         for (InternationalDescriptionWS description : contactType.getDescriptions()) {
             dto.setDescription(description.getContent(), description.getLanguageId());
         }
 
         // flush changes to the DB & clear cache
         contactTypeDas.flush();
         contactTypeDas.clear();
 
         return dto.getId();
     }
 
     public void updateUserContact(Integer userId, Integer typeId, ContactWS contact) throws SessionInternalError {
         // todo: support multiple WS method param validations through WSSecurityMethodMapper
         ContactTypeDTO type = new ContactTypeDAS().find(typeId);
         if (type == null || type.getEntity() == null || !getCallerCompanyId().equals(type.getEntity().getId()))
             throw new SessionInternalError("Invalid contact type.");
 
         // update the contact
         ContactBL cBl = new ContactBL();
         cBl.updateForUser(new ContactDTOEx(contact), userId, typeId, getCallerId());
     }
 
     /**
      * @param user
      */
     public void updateUser(UserWS user)
             throws SessionInternalError {
 
     	//TODO commenting validate user for create/edit customer grails impl. - vikasb
         //validateUser(user);
 
         UserBL bl = new UserBL(user.getUserId());
 
         // get the entity
         Integer entityId = getCallerCompanyId();
         Integer executorId = getCallerId();
 
         // convert user WS to a DTO that includes customer data
         UserDTOEx dto = new UserDTOEx(user, entityId);
 
         // update the user info and customer data
         bl.update(executorId, dto);
 
         // now update the contact info
         if (user.getContact() != null) {
             ContactDTOEx primaryContact = new ContactDTOEx(user.getContact());
             new ContactBL().createUpdatePrimaryForUser(primaryContact, user.getUserId(), entityId, getCallerId());
         }
 
         // and the credit card
         if (user.getCreditCard() != null) {
             updateCreditCard(user.getUserId(), user.getCreditCard());
         }
     }
 
     /**
      * Retrieves a user with its contact and credit card information.
      * @param userId
      * The id of the user to be returned
      */
     public UserWS getUserWS(Integer userId) throws SessionInternalError {
         UserBL bl = new UserBL(userId);
         return bl.getUserWS();
     }
 
     /**
      * Retrieves all the contacts of a user
      * @param userId
      * The id of the user to be returned
      */
     public ContactWS[] getUserContactsWS(Integer userId)
             throws SessionInternalError {
         ContactWS[] dtos = null;
         ContactBL contact = new ContactBL();
         List result = contact.getAll(userId);
         dtos = new ContactWS[result.size()];
         for (int f = 0; f < result.size(); f++) {
             dtos[f] = new ContactWS((ContactDTOEx) result.get(f));
         }
 
         return dtos;
     }
 
     /**
      * Retrieves the user id for the given username
      */
     public Integer getUserId(String username)
             throws SessionInternalError {
         UserDAS das = new UserDAS();
         Integer retValue = das.findByUserName(username,
                 getCallerCompanyId()).getId();
         return retValue;
     }
 
     /**
      * Retrieves an array of users in the required status
      */
     public Integer[] getUsersInStatus(Integer statusId) throws SessionInternalError {
         return getUsersByStatus(statusId, true);
     }
 
     /**
      * Retrieves an array of users in the required status
      */
     public Integer[] getUsersNotInStatus(Integer statusId) throws SessionInternalError {
         return getUsersByStatus(statusId, false);
     }
 
     @Deprecated
     private Integer[] getByCCNumber(Integer entityId, String number) {
         List<Integer> usersIds = new CreditCardDAS().findByLastDigits(entityId, number);
 
         Integer[] ids = new Integer[usersIds.size()];
         return usersIds.toArray(ids);
 
     }
 
     /**
      * Retrieves an array of users in the required status
      */
     public Integer[] getUsersByCreditCard(String number) throws SessionInternalError {
         Integer entityId = getCallerCompanyId();
 
         Integer[] ret = getByCCNumber(entityId, number);
         return ret;
     }
 
     /**
      * Retrieves an array of users in the required status
      */
     public Integer[] getUsersByStatus(Integer statusId, boolean in) throws SessionInternalError {
         try {
             UserBL bl = new UserBL();
             CachedRowSet users = bl.getByStatus(getCallerCompanyId(), statusId, in);
             LOG.debug("got collection. Now converting");
             Integer[] ret = new Integer[users.size()];
             int f = 0;
             while (users.next()) {
                 ret[f] = users.getInt(1);
                 f++;
             }
             users.close();
             return ret;
         } catch (Exception e) { // can't remove because of SQLException :(
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * Creates a user, then an order for it, an invoice out the order
      * and tries the invoice to be paid by an online payment
      * This is ... the mega call !!!
      */
     public CreateResponseWS create(UserWS user, OrderWS order)
             throws SessionInternalError {
 
         CreateResponseWS retValue = new CreateResponseWS();
 
         // the user first
         final Integer userId = createUser(user);
         retValue.setUserId(userId);
 
         if (userId == null) {
             return retValue;
         }
 
         // the order and (if needed) invoice
         order.setUserId(userId);
 
         Integer orderId = doCreateOrder(order, true).getId();
         InvoiceDTO invoice = doCreateInvoice(orderId);
 
         retValue.setOrderId(orderId);
 
         if (invoice != null) {
             retValue.setInvoiceId(invoice.getId());
 
             //the payment, if we have a credit card
             if (user.getCreditCard() != null) {
                 PaymentDTOEx payment = doPayInvoice(invoice, new CreditCardDTO(user.getCreditCard()));
                 PaymentAuthorizationDTOEx result = null;
                 if (payment != null) {
                     result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
                     result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
                 }
                 retValue.setPaymentResult(result);
                 retValue.setPaymentId(payment.getId());
             }
         } else {
             throw new SessionInternalError("Invoice expected for order: " + orderId);
         }
 
         return retValue;
     }
 
 
     /**
      * Processes partner payouts for all partners that have a payout due before the given run date.
      *
      * @param runDate date to process payouts for
      */
     public void triggerPartnerPayoutProcess(Date runDate) {
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.processPayouts(runDate);
     }
 
     /**
      * Process partner payout for a single given partner ID.
      *
      * @param partnerId partner id to process payouts for
      */
     public void processPartnerPayout(Integer partnerId) {
         try {
             new PartnerBL().processPayout(partnerId);
 
         } catch (SQLException e) {
             throw new SessionInternalError("SQL exception occurred while processing payout.", e);
         } catch (PluggableTaskException e) {
             throw new SessionInternalError("Required plug-in was not configured.", e);
         } catch (TaskException e) {
             throw new SessionInternalError("Exception occurred processing pluggable task.", e);
         } catch (NamingException e) {
             throw new SessionInternalError("Could not fetch bean from application context.", e);
         }
     }
 
     public void processPartnerPayouts(Date runDate) {
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.processPayouts(runDate);
     }
 
     public PartnerWS getPartner(Integer partnerId) throws SessionInternalError {
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         Partner dto = userSession.getPartnerDTO(partnerId);
 
         return PartnerBL.getWS(dto);
     }
 
     public Integer createPartner(UserWS newUser, PartnerWS partner) throws SessionInternalError {
 
         UserBL bl = new UserBL();
         newUser.setUserId(0);
 
         Integer entityId = getCallerCompanyId();
         if (bl.exists(newUser.getUserName(), entityId)) {
             throw new SessionInternalError("User already exists with username " + newUser.getUserName(),
                     new String[] { "UserWS,userName,validation.error.user.already.exists" });
         }
 
         Partner partnerDto = partner.getPartnerDTO();
         MetaFieldBL.fillMetaFieldsFromWS(entityId, partnerDto, newUser.getMetaFields());
 
         UserDTOEx dto = new UserDTOEx(newUser, entityId);
         dto.setPartner(partnerDto);
 
         Integer userId = bl.create(dto, getCallerId());
 
         ContactBL cBl = new ContactBL();
         if (newUser.getContact() != null) {
             newUser.getContact().setId(0);
             cBl.createPrimaryForUser(new ContactDTOEx(newUser.getContact()), userId, entityId, getCallerId());
         }
 
         return bl.getDto().getPartner().getId();
 
     }
 
     public void updatePartner(UserWS user, PartnerWS partner) throws SessionInternalError {
         Integer entityId = getCallerCompanyId();
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
 
         if (user != null) {
             UserDTOEx userDto = new UserDTOEx(user, entityId);
             userSession.update(getCallerId(), userDto);
         }
 
         if (partner != null) {
             Partner partnerDto = partner.getPartnerDTO();
 
             if (user != null) {
                 MetaFieldBL.fillMetaFieldsFromWS(entityId, partnerDto, user.getMetaFields());
             }
 
             userSession.updatePartner(getCallerId(), partnerDto);
         }
     }
 
     public void deletePartner (Integer partnerId) throws SessionInternalError {
         PartnerBL bl= new PartnerBL(partnerId);
         bl.delete(getCallerId());
     }
 
     /**
      * Pays given invoice, using the first credit card available for invoice'd
      * user.
      *
      * @return <code>null</code> if invoice has not positive balance, or if
      *         user does not have credit card
      * @return resulting authorization record. The payment itself can be found by
      * calling getLatestPayment
      */
     public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId) throws SessionInternalError {
 
         if (invoiceId == null) {
             throw new SessionInternalError("Can not pay null invoice");
         }
 
         final InvoiceDTO invoice = findInvoice(invoiceId);
         CreditCardDTO creditCard = getCreditCard(invoice.getBaseUser().getUserId());
         if (creditCard == null) {
             return null;
         }
 
         PaymentDTOEx payment = doPayInvoice(invoice, creditCard);
 
         PaymentAuthorizationDTOEx result = null;
         if (payment != null) {
             result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
             result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
         }
 
         return result;
     }
 
     /*
      * ORDERS
      */
     /**
      * @return the information of the payment aurhotization, or NULL if the
      * user does not have a credit card
      */
     public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order)
             throws SessionInternalError {
 
         PaymentAuthorizationDTOEx retValue = null;
         // start by creating the order. It'll do the checks as well
         Integer orderId = createOrder(order);
 
         Integer userId = order.getUserId();
         CreditCardDTO cc = getCreditCard(userId);
         UserBL user = new UserBL();
         Integer entityId = user.getEntityId(userId);
         if (cc != null) {
             CreditCardBL ccBl = new CreditCardBL();
             OrderDAS das = new OrderDAS();
             OrderDTO dbOrder = das.find(orderId);
 
             try {
                 retValue = ccBl.validatePreAuthorization(entityId, userId, cc, dbOrder.getTotal(), dbOrder.getCurrencyId(), getCallerId());
             } catch (PluggableTaskException e) {
                 throw new SessionInternalError("doing validation", WebServicesSessionSpringBean.class, e);
             }
         }
         return retValue;
     }
 
     public Integer createOrder(OrderWS order)
             throws SessionInternalError {
 
         Integer orderId = doCreateOrder(order, true).getId();
         return orderId;
     }
 
     /**
      * Update the given order, or create it if it doesn't already exist.
      *
      * @param order order to update or create
      * @return order id
      * @throws SessionInternalError
      */
     public Integer createUpdateOrder(OrderWS order) throws SessionInternalError {
         IOrderSessionBean orderSession = Context.getBean(Context.Name.ORDER_SESSION);
 
         OrderDTO dto = new OrderBL().getDTO(order);
         return orderSession.createUpdate(getCallerCompanyId(), getCallerId(), dto, getCallerLanguageId());
     }
 
     public OrderWS rateOrder(OrderWS order)
             throws SessionInternalError {
 
         OrderWS ordr = doCreateOrder(order, false);
         return ordr;
     }
 
     public OrderWS[] rateOrders(OrderWS orders[])
             throws SessionInternalError {
 
         if (orders == null || orders.length == 0) {
             LOG.debug("Call to rateOrders without orders to rate");
             return null;
         }
 
         OrderWS retValue[] = new OrderWS[orders.length];
         for (int index = 0; index < orders.length; index++) {
             retValue[index] = doCreateOrder(orders[index],false);
         }
         return retValue;
     }
 
     public void updateItem(ItemDTOEx item) {
         // check if all descriptions are to delete
         List<InternationalDescriptionWS> descriptions = item.getDescriptions();
         boolean noDescriptions = true;
         for (InternationalDescriptionWS description : descriptions) {
             if (!description.isDeleted()) {
                 noDescriptions = false;
                 break;
             }
         }
         if (noDescriptions) {
             throw new SessionInternalError("Must have a description", new String[] {
                 "ItemDTOEx,descriptions,validation.error.is.required"
             });
         }
 
         UserBL bl = new UserBL();
         Integer executorId = getCallerId();
         Integer languageId = getCallerLanguageId();
         
         item.setEntityId(getCallerCompanyId());
 
         // do some transformation from WS to DTO :(
         ItemBL itemBL = new ItemBL();
         ItemDTO dto = itemBL.getDTO(item);
 
         // Set description to null
         dto.setDescription(null);
 
         IItemSessionBean itemSession = (IItemSessionBean) Context.getBean(Context.Name.ITEM_SESSION);
         itemSession.update(executorId, dto, languageId);
 
         // save-delete descriptions
         for (InternationalDescriptionWS description : descriptions) {
             if (description.getLanguageId() != null) {
                 if (description.isDeleted()) {
                     dto.deleteDescription(description.getLanguageId());
                 } else {
                     dto.setDescription(description.getContent(), description.getLanguageId());
                 }
             }
         }
     }
 
     /**
      * Creates the given Order in jBilling, generates an Invoice for the same.
      * Returns the generated Invoice ID
      */
     public Integer createOrderAndInvoice(OrderWS order)
             throws SessionInternalError {
 
         Integer orderId = doCreateOrder(order, true).getId();
         InvoiceDTO invoice = doCreateInvoice(orderId);
 
         return invoice == null ? null : invoice.getId();
     }
 
     private void processLines(OrderDTO order, Integer languageId, Integer entityId, Integer userId, Integer currencyId,
                               String pricingFields) throws SessionInternalError {
 
         OrderHelper.synchronizeOrderLines(order);
 
         for (OrderLineDTO line : order.getLines()) {
             LOG.debug("Processing line " + line);
 
             if (line.getUseItem()) {
                 List<PricingField> fields = pricingFields != null
                                             ? Arrays.asList(PricingField.getPricingFieldsValue(pricingFields))
                                             : null;
 
                 ItemBL itemBl = new ItemBL(line.getItemId());
                 itemBl.setPricingFields(fields);
 
                 // get item with calculated price
                 ItemDTO item = itemBl.getDTO(languageId, userId, entityId, currencyId, line.getQuantity(), order);
                 LOG.debug("Populating line using item " + item);
 
                 // set price or percentage from item
                 if (item.getPrice() == null) {
                     line.setPrice(item.getPercentage());
                 } else {
                     line.setPrice(item.getPrice());
                 }
 
                 // set description and line type
                 line.setDescription(item.getDescription());
                 line.setTypeId(item.getOrderLineTypeId());
             }
         }
 
         OrderHelper.desynchronizeOrderLines(order);
     }
 
 
     public void updateOrder(OrderWS order)
             throws SessionInternalError {
         validateOrder(order);
         try {
             // start by locking the order
             OrderBL oldOrder = new OrderBL();
             oldOrder.setForUpdate(order.getId());
 
             // do some transformation from WS to DTO :(
             OrderBL orderBL = new OrderBL();
             OrderDTO dto = orderBL.getDTO(order);
 
             // get the info from the caller
             Integer executorId = getCallerId(); 
             Integer entityId = getCallerCompanyId();
             Integer languageId = getCallerLanguageId(); 
 
             // see if the related items should provide info
             processLines(dto, languageId, entityId, order.getUserId(), order.getCurrencyId(), order.getPricingFields());
 
             // recalculate
             orderBL.set(dto);
             orderBL.recalculate(entityId);
 
             // update
             oldOrder.update(executorId, dto);
 
         } catch (Exception e) {
             LOG.error("WS - updateOrder", e);
             throw new SessionInternalError("Error updating order");
         }
 
     }
 
     public OrderWS getOrder(Integer orderId) throws SessionInternalError {
             // get the info from the caller
         Integer languageId = getCallerLanguageId();
 
         // now get the order. Avoid the proxy since this is for the client
         OrderDAS das = new OrderDAS();
         OrderDTO order = das.findNow(orderId);
         if (order == null) { // not found
             return null;
         }
         OrderBL bl = new OrderBL(order);
         if (order.getDeleted() == 1) {
             LOG.debug("Returning deleted order " + orderId);
         }
         return bl.getWS(languageId);
     }
 
     public Integer[] getOrderByPeriod(Integer userId, Integer periodId)
             throws SessionInternalError {
         if (userId == null || periodId == null) {
             return null;
         }
         // now get the order
         OrderBL bl = new OrderBL();
         return bl.getByUserAndPeriod(userId, periodId);
     }
 
     public OrderLineWS getOrderLine(Integer orderLineId) throws SessionInternalError {
         // now get the order
         OrderBL bl = new OrderBL();
         OrderLineWS retValue = null;
 
         retValue = bl.getOrderLineWS(orderLineId);
 
         return retValue;
     }
 
     public void updateOrderLine(OrderLineWS line) throws SessionInternalError {
             // now get the order
         OrderBL bl = new OrderBL();
         bl.updateOrderLine(line);
     }
 
     public OrderWS getLatestOrder(Integer userId) throws SessionInternalError {
         if (userId == null) {
             throw new SessionInternalError("User id can not be null");
         }
         OrderWS retValue = null;
 
         // now get the order
         OrderBL bl = new OrderBL();
         Integer orderId = bl.getLatest(userId);
         if (orderId != null) {
             bl.set(orderId);
             retValue = bl.getWS(getCallerLanguageId());
         }
         return retValue;
     }
 
     public Integer[] getLastOrders(Integer userId, Integer number)
             throws SessionInternalError {
         if (userId == null || number == null) {
             return null;
         }
         UserBL userbl = new UserBL();
 
         OrderBL order = new OrderBL();
         return order.getListIds(userId, number, userbl.getEntityId(userId));
     }
 
     public void deleteOrder(Integer id) throws SessionInternalError {
         // now get the order
         OrderBL bl = new OrderBL();
         bl.setForUpdate(id);
         bl.delete(getCallerId());
     }
 
     /**
      * Returns the current order (order collecting current one-time charges) for the
      * period of the given date and the given user.
      * Returns null for users with no main subscription order.
      */
     public OrderWS getCurrentOrder(Integer userId, Date date) {
         OrderWS retValue = null;
         // get the info from the caller
         Integer languageId = getCallerLanguageId();
 
         // now get the current order
         OrderBL bl = new OrderBL();
         if (bl.getCurrentOrder(userId, date) != null) {
             retValue = bl.getWS(languageId);
         }
 
         return retValue;
     }
 
     /**
      * Updates the uesr's current one-time order for the given date.
      * Returns the updated current order. Throws an exception for
      * users with no main subscription order.
      */
     public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines,
             String pricing, Date date, String eventDescription) {
         try {
             UserBL userbl = new UserBL(userId);
             // check user has a main subscription order
             if (userbl.getEntity().getCustomer().getCurrentOrderId() == null) {
                 throw new SessionInternalError("No main subscription order for userId: " + userId);
             }
 
             // get currency from the user
             Integer currencyId = userbl.getCurrencyId();
 
             // get language from the caller
             Integer languageId = getCallerLanguageId(); 
 
             // pricing fields
             List<Record> records = null;
             PricingField[] fieldsArray = PricingField.getPricingFieldsValue(pricing);
             if (fieldsArray != null) {
                 Record record = new Record();
                 for (PricingField field : fieldsArray) {
                     record.addField(field, false); // don't care about isKey
                 }
                 records = new ArrayList<Record>(1);
                 records.add(record);
             }
 
             List<OrderLineDTO> diffLines = null;
             OrderBL bl = new OrderBL();
             if (lines != null) {
                 // get the current order
                 bl.set(OrderBL.getOrCreateCurrentOrder(userId, date, currencyId, true));
                 List<OrderLineDTO> oldLines = OrderLineBL.copy(bl.getDTO().getLines());
 
                 // add the line to the current order
                 for (OrderLineWS line : lines) {
                     bl.addItem(line.getItemId(), line.getQuantityAsDecimal(), languageId, userId, getCallerCompanyId(), currencyId, records);
                 }
 
                 // process lines to update prices and details from the source items
                 processLines(bl.getEntity(), languageId, getCallerCompanyId(), userId, currencyId, pricing);
                 diffLines = OrderLineBL.diffOrderLines(oldLines, bl.getDTO().getLines());
 
                 // generate NewQuantityEvents
                 bl.checkOrderLineQuantities(oldLines, bl.getDTO().getLines(), getCallerCompanyId(), bl.getDTO().getId(), true);
 
             } else if (records != null) {
                 // Since there are no lines, run the mediation process
                 // rules to create them.
                 PluggableTaskManager<IMediationProcess> tm =
                         new PluggableTaskManager<IMediationProcess>(
                         getCallerCompanyId(),
                         Constants.PLUGGABLE_TASK_MEDIATION_PROCESS);
                 IMediationProcess processTask = tm.getNextClass();
 
                 MediationResult result = new MediationResult("WS", true);
                 result.setUserId(userId);
                 result.setEventDate(date);
                 List results = new ArrayList(1);
                 results.add(result);
                 processTask.process(records, results, "WS");
                 diffLines = result.getDiffLines();
 
                 if (result.getCurrencyId() != null) {
                     currencyId = result.getCurrencyId();
                 }
 
                 // the mediation process might not have anything for you...
                 if (result.getCurrentOrder() == null) {
                     LOG.debug("Call to updateOrder did not resolve to a current order lines = " +
                             Arrays.toString(lines) + " fields= " + Arrays.toString(fieldsArray));
                     return null;
                 }
                 bl.set(result.getCurrentOrder());
             } else {
                 throw new SessionInternalError("Both the order lines and " +
                         "pricing fields were null. At least one of either " +
                         "must be provided.");
             }
 
             // save the event
             // assign to record DONE and BILLABLE status
             MediationRecordStatusDTO status = new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE);
             MediationRecordDTO record = new MediationRecordDTO(String.valueOf(date.getTime()),
                                                                new Date(),
                                                                null,
                                                                status);
             record = new MediationRecordDAS().save(record);
 
             IMediationSessionBean mediation = (IMediationSessionBean) Context.getBean(Context.Name.MEDIATION_SESSION);
             mediation.saveEventRecordLines(new ArrayList<OrderLineDTO>(diffLines), record, date,eventDescription);
 
             // return the updated order
             return bl.getWS(languageId);
 
         } catch (Exception e) {
             LOG.error("WS - getCurrentOrder", e);
             throw new SessionInternalError("Error updating current order");
         }
     }
 
     public OrderWS[] getUserSubscriptions(Integer userId) throws SessionInternalError {
     	if (userId == null) throw new SessionInternalError("User Id cannot be null.");
 
         List<OrderDTO> subscriptions= new OrderDAS().findByUserSubscriptions(userId);
         if (null == subscriptions)
         {
         	return new OrderWS[0];
         }
         OrderWS[] orderArr= new OrderWS[subscriptions.size()];
         OrderBL bl = null;
         for(OrderDTO dto: subscriptions) {
         	bl= new OrderBL(dto);
         	orderArr[subscriptions.indexOf(dto)]= bl.getWS(getCallerLanguageId());
         }
 
 		return orderArr;
 	}
 
     public boolean updateOrderPeriods(OrderPeriodWS[] orderPeriods) throws SessionInternalError {
         //IOrderSessionBean orderSession = Context.getBean(Context.Name.ORDER_SESSION);
 
 		List<OrderPeriodDTO> periodDtos= new ArrayList<OrderPeriodDTO>(orderPeriods.length);
 		OrderPeriodDAS periodDas= new OrderPeriodDAS();
 		OrderPeriodDTO periodDto= null;
 		for (OrderPeriodWS periodWS: orderPeriods) {
 			if ( null != periodWS.getId()) {
 				periodDto= periodDas.find(periodWS.getId());
 			}
 			if ( null == periodDto ) {
 				periodDto= new OrderPeriodDTO();
 				periodDto.setCompany(new CompanyDAS().find(getCallerCompanyId()));
 				//periodDto.setVersionNum(new Integer(0));
 			}
 			periodDto.setValue(periodWS.getValue());
 			if (null != periodWS.getPeriodUnitId()) {
 				periodDto.setUnitId(periodWS.getPeriodUnitId().intValue());
 			}
 			periodDto= periodDas.save(periodDto);
 			if (periodWS.getDescriptions() != null && periodWS.getDescriptions().size() > 0 ) {
 				periodDto.setDescription(((InternationalDescriptionWS)periodWS.getDescriptions().get(0)).getContent(), ((InternationalDescriptionWS)periodWS.getDescriptions().get(0)).getLanguageId());
 			}
 			LOG.debug("Converted to DTO: " + periodDto);
 			periodDas.flush();
 			periodDas.clear();
  			//periodDtos.add(periodDto);
 			periodDto= null;
 		}
         //orderSession.setPeriods(getCallerLanguageId(), periodDtos.toArray(new OrderPeriodDTO[periodDtos.size()]));
         return true;
     }
 
     public boolean updateOrCreateOrderPeriod(OrderPeriodWS orderPeriod)
             throws SessionInternalError {
 
         OrderPeriodDAS periodDas = new OrderPeriodDAS();
         OrderPeriodDTO periodDto = null;
         if (null != orderPeriod.getId()) {
             periodDto = periodDas.find(orderPeriod.getId());
         }
 
         if (null == periodDto) {
             periodDto = new OrderPeriodDTO();
             periodDto.setCompany(new CompanyDAS().find(getCallerCompanyId()));
             // periodDto.setVersionNum(new Integer(0));
         }
         periodDto.setValue(orderPeriod.getValue());
         if (null != orderPeriod.getPeriodUnitId()) {
             periodDto.setUnitId(orderPeriod.getPeriodUnitId().intValue());
         }
         periodDto = periodDas.save(periodDto);
         if (orderPeriod.getDescriptions() != null
                 && orderPeriod.getDescriptions().size() > 0) {
             periodDto.setDescription(((InternationalDescriptionWS) orderPeriod
                     .getDescriptions().get(0)).getContent(),
                     ((InternationalDescriptionWS) orderPeriod.getDescriptions()
                             .get(0)).getLanguageId());
         }
         LOG.debug("Converted to DTO: " + periodDto);
         periodDas.flush();
         periodDas.clear();
         return true;
     }
 
     public boolean deleteOrderPeriod(Integer periodId) throws SessionInternalError {
         try {
             // now get the order
             OrderBL bl = new OrderBL();
             return new Boolean(bl.deletePeriod(periodId));
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /*
      * PAYMENT
      */
 
     public Integer createPayment(PaymentWS payment) {
         return applyPayment(payment, null);
     }
 
     public void updatePayment(PaymentWS payment) {
         PaymentDTOEx dto = new PaymentDTOEx(payment);
         new PaymentBL(payment.getId()).update(getCallerId(), dto);
     }
 
     public void deletePayment(Integer paymentId) throws SessionInternalError {
 
         PaymentBL paymentBL = new PaymentBL(paymentId);
 
         // check if the payment is a refund , if it is do not allow it
         if(paymentBL.getEntity().getIsRefund() == 1) {
             LOG.debug("This payment " + paymentId
                     + " is a refund so we cannot delete it.");
             throw new SessionInternalError("A Refund cannot be deleted",
                         new String[] {"PaymentWS,isRefund,validation.error.delete.refund.payment"});
         }
 
         // check if payment has been refunded
         if( PaymentBL.ifRefunded(paymentId) ) {
             throw new SessionInternalError("This payment has been refunded and hence cannot be deleted.",
             new String[] {"PaymentWS,id,validation.error.delete.refunded.payment"});
         }
 
         paymentBL.delete();
     }
 
     /**
      * Enters a payment and applies it to the given invoice. This method DOES NOT process
      * the payment but only creates it as 'Entered'. The entered payment will later be
      * processed by the billing process.
      *
      * Invoice ID is optional. If no invoice ID is given the payment will be applied to
      * the payment user's account according to the configured entity preferences.
      *
      * @param payment payment to apply
      * @param invoiceId invoice id
      * @return created payment id
      * @throws SessionInternalError
      */
     public Integer applyPayment(PaymentWS payment, Integer invoiceId)
             throws SessionInternalError {
 //        payment.setIsRefund(0);
 
         // apply validations for refund payments
         if(payment.getIsRefund() == 1) {
             // check for validations
             if(!PaymentBL.validateRefund(payment)){
                 throw new SessionInternalError("Either refund payment was not linked to any payment or the refund amount is different from the linked payment",
                         new String[] {"PaymentWS,paymentId,validation.error.apply.without.payment.or.different.linked.payment.amount"});
             }
         }
 
         if (payment.getMethodId() == null) {
             throw new SessionInternalError("Cannot apply a payment without a payment method.",
                                            new String[] { "PaymentWS,paymentMethodId,validation.error.apply.without.method" });
         }
 
         IPaymentSessionBean session = (IPaymentSessionBean) Context.getBean(Context.Name.PAYMENT_SESSION);
         LOG.debug("payment has "+payment);
         return session.applyPayment(new PaymentDTOEx(payment), invoiceId, getCallerId());
     }
 
     /**
      * Processes a payment and applies it to the given invoice. This method will actively
      * processes the payment using the configured payment plug-in.
      *
      * Payment is optional when an invoice ID is provided. If no payment is given, the payment
      * will be processed using the invoiced user's configured "automatic payment" instrument.
      *
      * Invoice ID is optional. If no invoice ID is given the payment will be applied to the
      * payment user's account according to the configured entity preferences.
      *
      * @param payment payment to process
      * @param invoiceId invoice id
      * @return payment authorization from the payment processor
      */
     public PaymentAuthorizationDTOEx processPayment(PaymentWS payment, Integer invoiceId) {
     	LOG.debug("In process payment");
 
         if (payment == null && invoiceId != null) {
         	return payInvoice(invoiceId);
         }
         
 		if (payment.getCreditCard() != null) {
 			LOG.debug("\n Are they sending the correct value in payment gateway field which should be Pares value the client got from the URL ??? >>>>>>>>> "
 					+ payment.getCreditCard().getGatewayKey());
 			
 			// if its a new credit card, it must be saved first
 			if (null != payment.getCreditCard()
 					&& (null == payment.getCreditCard().getId() || 0 == payment
 							.getCreditCard().getId().intValue())) {
 				LOG.debug("Payment is being made with a new Credit Card. This must be updated first.");
 
                 IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
                 Integer ccId = userSession.createCreditCard(payment.getUserId(), new CreditCardDTO(payment.getCreditCard()));
                 CreditCardDTO cc = new CreditCardDAS().find(ccId);
                 //this step re-initializes the Payment object with saved gateway key if involved.
                 payment.setCreditCard(cc.getOldDTO());
                 LOG.debug("CreditCard gateway key : "+payment.getCreditCard().getGatewayKey());
 			}
 		}
         
         // apply validations for refund payment
         if(payment.getIsRefund() == 1) {
             // check for validations
             if(!PaymentBL.validateRefund(payment)){
                 throw new SessionInternalError("Either refund payment was not linked to any payment or the refund amount is different from the linked payment",
                         new String[] {"PaymentWS,paymentId,validation.error.apply.without.payment.or.different.linked.payment.amount"});
             }
         }
         
         Integer entityId = getCallerCompanyId();
         PaymentDTOEx dto = new PaymentDTOEx(payment);
 
         // payment without Credit Card or ACH, fetch the users primary payment instrument for use
         if (payment.getCreditCard() == null && payment.getAch() == null) {
             LOG.debug("processPayment() called without payment method, fetching users automatic payment instrument.");
             PaymentDTO instrument;
             try {
                 instrument = PaymentBL.findPaymentInstrument(entityId, payment.getUserId());
 
             } catch (PluggableTaskException e) {
                 throw new SessionInternalError("Exception occurred fetching payment info plug-in.",
                                                new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });
 
             } catch (TaskException e) {
                 throw new SessionInternalError("Exception occurred with plug-in when fetching payment instrument.",
                                                new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });
             }
 
             if (instrument == null || (instrument.getCreditCard() == null && instrument.getAch() == null)) {
                 throw new SessionInternalError("User " + payment.getUserId() + "does not have a default payment instrument.",
                                                new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });
             }
 
             dto.setCreditCard(instrument.getCreditCard());
             dto.setAch(instrument.getAch());
         }
 
         // populate payment method based on the payment instrument
         if (dto.getCreditCard() != null) {
             dto.setPaymentMethod(new PaymentMethodDTO(dto.getCreditCard().getCcType()));
         } else if (dto.getAch() != null) {
             dto.setPaymentMethod(new PaymentMethodDTO(Constants.PAYMENT_METHOD_ACH));
         }
 
         // process payment
         IPaymentSessionBean session = (IPaymentSessionBean) Context.getBean(Context.Name.PAYMENT_SESSION);
         Integer result = session.processAndUpdateInvoice(dto, invoiceId, entityId, getCallerId());
         LOG.debug("paymentBean.processAndUpdateInvoice() Id=" + result);
 
         PaymentAuthorizationDTOEx auth = null;
         if (dto != null && dto.getAuthorization() != null) {
             LOG.debug("PaymentAuthorizationDTO Id =" + dto.getAuthorization().getId());
             auth = new PaymentAuthorizationDTOEx(dto.getAuthorization().getOldDTO());
             LOG.debug("PaymentAuthorizationDTOEx Id =" + auth.getId());
             auth.setResult(result.equals(Constants.RESULT_OK));
 
         } else {
             auth = new PaymentAuthorizationDTOEx();
             auth.setPaymentId(dto.getId());
             auth.setResult(result.equals(Constants.RESULT_FAIL));
         }
         return auth;
     }
 
     /*
      * Validate credit card information
      * using pre-Auth call to the Payment plugin for level 3
      * (non-Javadoc)
      *
      * Level 1 - Simple checks on Credit Card Number, name, and mod10
      * Level 2 - Address and Security Code validation
      * Level 3 - Check number against a payment gateway using pre-auth transaction
      */
     public CardValidationWS validateCreditCard(com.sapienter.jbilling.server.entity.CreditCardDTO creditCard, ContactWS contact, int level) {
         CardValidationWS validation = new CardValidationWS(level);
 
         /*
            Level 1 validations (default), card has a name & number, number passes mod10 luhn check
         */
 
         if (StringUtils.isBlank(creditCard.getName())) {
             validation.addError("Credit card name is missing.", 1);
         }
 
         if (StringUtils.isBlank(creditCard.getNumber())) {
             validation.addError("Credit card number is missing.", 1);
 
         } else {
             if (creditCard.getNumber().matches("^\\D+$")) {
                 validation.addError("Credit card number is not a valid number.", 1);
             }
 
             if (!com.sapienter.jbilling.common.Util.luhnCheck(creditCard.getNumber())) {
                 validation.addError("Credit card mod10 validation failed.", 1);
             }
         }
 
 
         /*
            Level 2 validations, card has an address & a valid CVV security code
         */
         if (level > 1) {
             if (StringUtils.isBlank(contact.getAddress1())) {
                 validation.addError("Customer address is missing.", 2);
             }
 
             if (StringUtils.isBlank(creditCard.getSecurityCode())) {
                 validation.addError("Credit card CVV security code is missing.", 2);
 
             } else {
                 if (creditCard.getSecurityCode().matches("^\\D+$")) {
                     validation.addError("Credit card CVV security code is not a valid number.", 2);
                 }
             }
         }
 
 
         /*
            Level 3 validations, attempted live pre-authorization against payment gateway
         */
         if (level > 2) {
             PaymentAuthorizationDTOEx auth = null;
             try {
                 // entity id, user id, credit card, amount, currency id, executor
                 auth = new CreditCardBL().validatePreAuthorization(getCallerCompanyId(),
                                                                    getCallerId(),
                                                                    new CreditCardDTO(creditCard),
                                                                    new BigDecimal("0.01"),
                                                                    1,
                                                                    getCallerId());
             } catch (PluggableTaskException e) {
                 // log plug-in exception and ignore
                 LOG.error("Exception occurred processing pre-authorization", e);
             }
 
             if (auth == null || !auth.getResult()) {
                 validation.addError("Credit card pre-authorization failed.", 3);
             }
             validation.setPreAuthorization(auth);
         }
 
         return validation;
     }
 
     public PaymentWS getPayment(Integer paymentId)
             throws SessionInternalError {
         // get the info from the caller
         Integer languageId = getCallerLanguageId();
 
         PaymentBL bl = new PaymentBL(paymentId);
         return PaymentBL.getWS(bl.getDTOEx(languageId));
     }
 
     public PaymentWS getLatestPayment(Integer userId) throws SessionInternalError {
         PaymentWS retValue = null;
         // get the info from the caller
         Integer languageId = getCallerLanguageId();
 
         PaymentBL bl = new PaymentBL();
         Integer paymentId = bl.getLatest(userId);
         if (paymentId != null) {
             bl.set(paymentId);
             retValue = PaymentBL.getWS(bl.getDTOEx(languageId));
         }
         return retValue;
     }
 
     public Integer[] getLastPayments(Integer userId, Integer number) throws SessionInternalError {
         if (userId == null || number == null) {
             return null;
         }
         Integer languageId = getCallerLanguageId();
 
         PaymentBL payment = new PaymentBL();
         return payment.getManyWS(userId, number, languageId);
     }
 
     public PaymentWS getUserPaymentInstrument(Integer userId) throws SessionInternalError {
         PaymentDTO instrument;
         try {
             instrument = PaymentBL.findPaymentInstrument(getCallerCompanyId(), userId);
         } catch (PluggableTaskException e) {
             throw new SessionInternalError("Exception occurred fetching payment info plug-in.", e);
         } catch (TaskException e) {
             throw new SessionInternalError("Exception occurred with plug-in when fetching payment instrument.", e);
         }
 
         if (instrument == null) {
         	return null;
         }
         PaymentDTOEx paymentDTOEx = new PaymentDTOEx(instrument);
         paymentDTOEx.setUserId(userId);
         return PaymentBL.getWS(paymentDTOEx);
     }
 
     public BigDecimal getTotalRevenueByUser (Integer userId) throws SessionInternalError {
     	return new PaymentDAS().findTotalRevenueByUser(userId);
     }
 
     /*
      * ITEM
      */
     public Integer createItem(ItemDTOEx item) throws SessionInternalError {
         // check if all descriptions are to delete
         List<InternationalDescriptionWS> descriptions = item.getDescriptions();
         boolean noDescriptions = true;
         for (InternationalDescriptionWS description : descriptions) {
             if (!description.isDeleted()) {
                 noDescriptions = false;
                 break;
             }
         }
         if (noDescriptions) {
             throw new SessionInternalError("Must have a description", new String[] {
                     "ItemDTOEx,descriptions,validation.error.is.required"
             });
         }
         
         item.setEntityId(getCallerCompanyId());
 
         ItemBL itemBL = new ItemBL();
         ItemDTO dto = itemBL.getDTO(item);
 
         // Set description to null
         dto.setDescription(null);
 
         // get the info from the caller
         Integer languageId = getCallerLanguageId();
         Integer entityId = getCallerCompanyId(); 
         dto.setEntity(new CompanyDTO(entityId));
 
         // call the creation
         Integer id = itemBL.create(dto, languageId);
 
         dto = itemBL.getEntity();
 
         // save-delete descriptions
         for (InternationalDescriptionWS description : descriptions) {
             if (description.getLanguageId() != null && description.getContent() != null) {
                 if (description.isDeleted()) {
                     dto.deleteDescription(description.getLanguageId());
                 } else {
                     dto.setDescription(description.getContent(), description.getLanguageId());
                 }
             }
         }
         return id;
     }
 
     /**
      * Retrieves an array of items for the caller's entity.
      * @return an array of items from the caller's entity
      */
     public ItemDTOEx[] getAllItems() throws SessionInternalError {
         Integer entityId = getCallerCompanyId();
         ItemBL itemBL = new ItemBL();
         ItemDTOEx[] items = itemBL.getAllItems(entityId);
         for (ItemDTOEx item : items) {
             item.setDescriptions(getAllItemDescriptions(item.getId()));
         }
         return items;
     }
 
     /**
      * Implementation of the User Transitions List webservice. This accepts a
      * start and end date as arguments, and produces an array of data containing
      * the user transitions logged in the requested time range.
      * @param from Date indicating the lower limit for the extraction of transition
      * logs. It can be <code>null</code>, in such a case, the extraction will start
      * where the last extraction left off. If no extractions have been done so far and
      * this parameter is null, the function will extract from the oldest transition
      * logged.
      * @param to Date indicatin the upper limit for the extraction of transition logs.
      * It can be <code>null</code>, in which case the extraction will have no upper
      * limit.
      * @return UserTransitionResponseWS[] an array of objects containing the result
      * of the extraction, or <code>null</code> if there is no data thas satisfies
      * the extraction parameters.
      */
     public UserTransitionResponseWS[] getUserTransitions(Date from, Date to)
             throws SessionInternalError {
 
         UserTransitionResponseWS[] result = null;
         Integer last = null;
         // Obtain the current entity and language Ids
 
         UserBL user = new UserBL();
         Integer callerId = getCallerId();
         Integer entityId = getCallerCompanyId();
         EventLogger evLog = EventLogger.getInstance();
 
         if (from == null) {
             last = evLog.getLastTransitionEvent(entityId);
         }
 
         if (last != null) {
             result = user.getUserTransitionsById(entityId, last, to);
         } else {
             result = user.getUserTransitionsByDate(entityId, from, to);
         }
 
         if (result == null) {
             LOG.info("Data retrieved but resultset is null");
         } else {
             LOG.info("Data retrieved. Result size = " + result.length);
         }
 
         // Log the last value returned if there was any. This happens always,
         // unless the returned array is empty.
         if (result != null && result.length > 0) {
             LOG.info("Registering transition list event");
             evLog.audit(callerId, null, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                     EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                     result[0].getId().toString(), null);
         }
         return result;
     }
 
     /**
      * @return UserTransitionResponseWS[] an array of objects containing the result
      * of the extraction, or <code>null</code> if there is no data thas satisfies
      * the extraction parameters.
      */
     public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id)
             throws SessionInternalError {
 
         UserTransitionResponseWS[] result = null;
         // Obtain the current entity and language Ids
 
         UserBL user = new UserBL();
         Integer callerId = getCallerId();
         Integer entityId = getCallerCompanyId();
         EventLogger evLog = EventLogger.getInstance();
 
         result = user.getUserTransitionsById(entityId, id, null);
 
         if (result == null) {
             LOG.debug("Data retrieved but resultset is null");
         } else {
             LOG.debug("Data retrieved. Result size = " + result.length);
         }
 
         // Log the last value returned if there was any. This happens always,
         // unless the returned array is empty.
         if (result != null && result.length > 0) {
             LOG.debug("Registering transition list event");
             evLog.audit(callerId, null, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                     EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                     result[0].getId().toString(), null);
         }
         return result;
     }
 
     public ItemDTOEx getItem(Integer itemId, Integer userId, String pricing) {
         PricingField[] fields = PricingField.getPricingFieldsValue(pricing);
 
         ItemBL helper = new ItemBL(itemId);
         List<PricingField> f = new ArrayList<PricingField>();
         if (fields != null) f.addAll(Arrays.asList(fields));
         helper.setPricingFields(f);
 
         Integer callerId = getCallerId(); 
         Integer entityId = getCallerCompanyId(); 
         Integer languageId = getCallerLanguageId(); 
 
         // use the currency of the given user if provided, otherwise
         // default to the currency of the caller (admin user)
         Integer currencyId = (userId != null
                               ? new UserBL(userId).getCurrencyId()
                               : getCallerCurrencyId());
 
         ItemDTOEx retValue = helper.getWS(helper.getDTO(languageId, userId, entityId, currencyId));
         // get descriptions
         retValue.setDescriptions(getAllItemDescriptions(retValue.getId()));
         return retValue;
     }
 
     private List<InternationalDescriptionWS> getAllItemDescriptions(int itemId) {
         JbillingTableDAS tableDas = Context.getBean(Context.Name.JBILLING_TABLE_DAS);
         JbillingTable table = tableDas.findByName(Constants.TABLE_ITEM);
 
         InternationalDescriptionDAS descriptionDas = (InternationalDescriptionDAS) Context
                 .getBean(Context.Name.DESCRIPTION_DAS);
         Collection<InternationalDescriptionDTO> descriptionsDTO = descriptionDas.findAll(table.getId(), itemId,
                 "description");
 
         List<InternationalDescriptionWS> descriptionsWS = new ArrayList<InternationalDescriptionWS>();
         for (InternationalDescriptionDTO descriptionDTO : descriptionsDTO) {
             descriptionsWS.add(new InternationalDescriptionWS(descriptionDTO));
         }
         return descriptionsWS;
     }
 
     public Integer createItemCategory(ItemTypeWS itemType)
             throws SessionInternalError {
 
         UserBL bl = new UserBL(getCallerId());
         Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
 
         ItemTypeDTO dto = new ItemTypeDTO();
         dto.setDescription(itemType.getDescription());
         dto.setOrderLineTypeId(itemType.getOrderLineTypeId());
         dto.setEntity(new CompanyDTO(entityId));
 
         ItemTypeBL itemTypeBL = new ItemTypeBL();
 
         //Check if the category already exists to throw an error to the user.
         if (itemTypeBL.exists(entityId, dto.getDescription())) {
             throw new SessionInternalError("The product category already exists with name " + dto.getDescription(),
                     new String[]{"ItemTypeWS,name,validation.error.category.already.exists"});
         }
 
         itemTypeBL.create(dto);
         return itemTypeBL.getEntity().getId();
     }
 
     public void updateItemCategory(ItemTypeWS itemType) throws SessionInternalError {
         UserBL bl = new UserBL(getCallerId());
         Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
         Integer executorId = bl.getEntity().getUserId();
 
         ItemTypeBL itemTypeBL = new ItemTypeBL(itemType.getId());
 
         ItemTypeDTO dto = new ItemTypeDTO();
         dto.setDescription(itemType.getDescription());
         dto.setOrderLineTypeId(itemType.getOrderLineTypeId());
 
         // make sure that item category names are unique. If the name was changed, then check
         // that the new name isn't a duplicate of an existing category.
         if (!itemTypeBL.getEntity().getDescription().equals(itemType.getDescription())
             && itemTypeBL.exists(entityId, dto.getDescription())) {
             throw new SessionInternalError("The product category already exists with name " + dto.getDescription(),
                     new String[]{"ItemTypeWS,name,validation.error.category.already.exists"});
         }
 
         itemTypeBL.update(executorId, dto);
     }
 
     private Integer zero2null(Integer var) {
         if (var != null && var.intValue() == 0) {
             return null;
         } else {
             return var;
         }
     }
 
     private Date zero2null(Date var) {
         if (var != null) {
             Calendar cal = Calendar.getInstance();
             cal.setTime(var);
             if (cal.get(Calendar.YEAR) == 1) {
                 return null;
             }
         }
 
         return var;
 
     }
 
     private void validateUser(UserWS newUser)
             throws SessionInternalError {
         // do the validation
         if (newUser == null) {
             throw new SessionInternalError("Null parameter");
         }
         // C# sends a 0 when it is null ...
         newUser.setCurrencyId(zero2null(newUser.getCurrencyId()));
         newUser.setPartnerId(zero2null(newUser.getPartnerId()));
         newUser.setParentId(zero2null(newUser.getParentId()));
         newUser.setMainRoleId(zero2null(newUser.getMainRoleId()));
         newUser.setLanguageId(zero2null(newUser.getLanguageId()));
         newUser.setStatusId(zero2null(newUser.getStatusId()));
         // clean up the cc number from spaces and '-'
         if (newUser.getCreditCard() != null &&
                 newUser.getCreditCard().getNumber() != null) {
             newUser.getCreditCard().setNumber(CreditCardBL.cleanUpNumber(
                     newUser.getCreditCard().getNumber()));
         }
 
         // todo: additional hibernate validations
         // additional validation
         if (newUser.getMainRoleId().equals(Constants.TYPE_CUSTOMER) ||
                 newUser.getMainRoleId().equals(Constants.TYPE_PARTNER)) {
         } else {
             throw new SessionInternalError("Valid user roles are customer (5) " +
                     "and partner (4)");
         }
         if (newUser.getCurrencyId() != null &&
                 newUser.getCurrencyId().intValue() <= 0) {
             throw new SessionInternalError("Invalid currency code");
         }
         if (newUser.getStatusId().intValue() <= 0) {
             throw new SessionInternalError("Invalid status code");
         }
     }
 
     private void validateOrder(OrderWS order) throws SessionInternalError {
         if (order == null) {
             throw new SessionInternalError("Null parameter");
         }
 
         // meta fields validation
         MetaFieldBL.validateMetaFields(getCallerCompanyId(), EntityType.ORDER, order.getMetaFields());
 
         order.setUserId(zero2null(order.getUserId()));
         order.setPeriod(zero2null(order.getPeriod()));
         order.setBillingTypeId(zero2null(order.getBillingTypeId()));
         order.setStatusId(zero2null(order.getStatusId()));
         order.setCurrencyId(zero2null(order.getCurrencyId()));
         order.setNotificationStep(zero2null(order.getNotificationStep()));
         order.setDueDateUnitId(zero2null(order.getDueDateUnitId()));
         //Bug Fix: 1385: Due Date may be zero
         //order.setDueDateValue(zero2null(order.getDueDateValue()));
         order.setDfFm(zero2null(order.getDfFm()));
         order.setAnticipatePeriods(zero2null(order.getAnticipatePeriods()));
         order.setActiveSince(zero2null(order.getActiveSince()));
         order.setActiveUntil(zero2null(order.getActiveUntil()));
         order.setNextBillableDay(zero2null(order.getNextBillableDay()));
         order.setLastNotified(null);
 
         // CXF seems to pass empty array as null
         if (order.getOrderLines() == null) {
             order.setOrderLines(new OrderLineWS[0]);
         }
 
         // todo: additional hibernate validations
         // the lines
         for (int f = 0; f < order.getOrderLines().length; f++) {
             OrderLineWS line = order.getOrderLines()[f];
             if (line.getUseItem() == null) {
             line.setUseItem(false);
             }
             line.setItemId(zero2null(line.getItemId()));
             String error = "";
             // if use the item, I need the item id
             if (line.getUseItem()) {
                 if (line.getItemId() == null || line.getItemId().intValue() == 0) {
                     error += "OrderLineWS: if useItem == true the itemId is required - ";
                 }
                 if (line.getQuantityAsDecimal() == null || BigDecimal.ZERO.compareTo(line.getQuantityAsDecimal()) == 0) {
                     error += "OrderLineWS: if useItem == true the quantity is required - ";
                 }
             } else {
                 // I need the amount and description
                 if (line.getAmount() == null) {
                     error += "OrderLineWS: if useItem == false the item amount " +
                             "is required - ";
                 }
                 if (line.getDescription() == null ||
                         line.getDescription().length() == 0) {
                     error += "OrderLineWS: if useItem == false the description " +
                             "is required - ";
                 }
             }
             if (error.length() > 0) {
                 throw new SessionInternalError(error);
             }
         }
     }
 
     private InvoiceDTO doCreateInvoice(Integer orderId) {
         try {
             BillingProcessBL process = new BillingProcessBL();
             InvoiceDTO invoice = process.generateInvoice(orderId, null, getCallerId());
             return invoice;
         } catch (Exception e) {
             LOG.error("WS - create invoice:", e);
             throw new SessionInternalError("Error while generating a new invoice");
         }
     }
 
     private PaymentDTOEx doPayInvoice(InvoiceDTO invoice, CreditCardDTO creditCard)
             throws SessionInternalError {
 
         if (invoice.getBalance() == null || BigDecimal.ZERO.compareTo(invoice.getBalance()) >= 0) {
             LOG.warn("Can not pay invoice: " + invoice.getId() + ", balance: " + invoice.getBalance());
             return null;
         }
 
         IPaymentSessionBean payment = (IPaymentSessionBean) Context.getBean(
                 Context.Name.PAYMENT_SESSION);
         PaymentDTOEx paymentDto = new PaymentDTOEx();
         paymentDto.setIsRefund(0);
         paymentDto.setAmount(invoice.getBalance());
         paymentDto.setCreditCard(creditCard);
         paymentDto.setCurrency(new CurrencyDAS().find(invoice.getCurrency().getId()));
         paymentDto.setUserId(invoice.getBaseUser().getUserId());
         paymentDto.setPaymentMethod(new PaymentMethodDAS().find(
                 com.sapienter.jbilling.common.Util.getPaymentMethod(
                 creditCard.getNumber())));
         paymentDto.setPaymentDate(new Date());
 
         // make the call
         payment.processAndUpdateInvoice(paymentDto, invoice, getCallerId());
 
         return paymentDto;
     }
 
     /**
      * Conveniance method to find a credit card
      */
     private CreditCardDTO getCreditCard(Integer userId) {
         if (userId == null) {
             return null;
         }
 
         CreditCardDTO result = null;
         try {
             UserBL user = new UserBL(userId);
             Integer entityId = user.getEntityId(userId);
             if (user.hasCreditCard()) {
                 // find it
                 PaymentDTOEx paymentDto = PaymentBL.findPaymentInstrument(
                         entityId, userId);
                 // it might have a credit card, but it might not be valid or
                 // just not found by the plug-in
                 if (paymentDto != null) {
                     result = paymentDto.getCreditCard();
                 }
             }
         } catch (Exception e) { // forced by checked exceptions :(
             LOG.error("WS - finding a credit card", e);
             throw new SessionInternalError("Error finding a credit card for user: " + userId);
         }
 
         return result;
     }
 
     private OrderWS doCreateOrder(OrderWS order, boolean create)
             throws SessionInternalError {
 
         validateOrder(order);
         // get the info from the caller
         Integer executorId = getCallerId();
         Integer entityId = getCallerCompanyId();
 
         // convert to a DTO
         OrderBL orderBL = new OrderBL();
         OrderDTO dto = orderBL.getDTO(order);
 
         // we'll need the langauge later
         UserBL bl = new UserBL(order.getUserId());
         Integer languageId = bl.getEntity().getLanguageIdField();
         
         // process the lines and let the items provide the order line details
         LOG.debug("Processing order lines");
         processLines(dto, languageId, entityId, order.getUserId(), order.getCurrencyId(), order.getPricingFields());
 
         LOG.info("before cycle start");
         // set a default cycle starts if needed (obtained from the main
         // subscription order, if it exists)
         if (dto.getCycleStarts() == null && dto.getIsCurrent() == null) {
             Integer mainOrderId = orderBL.getMainOrderId(dto.getUser().getId());
             if (mainOrderId != null) {
                 // only set a default if preference use current order is set
                 PreferenceBL preferenceBL = new PreferenceBL();
                 try {
                     preferenceBL.set(entityId, Constants.PREFERENCE_USE_CURRENT_ORDER);
                 } catch (EmptyResultDataAccessException e) {
                     // default preference will be used
                     }
                 if (preferenceBL.getInt() != 0) {
                     OrderDAS das = new OrderDAS();
                     OrderDTO mainOrder = das.findNow(mainOrderId);
                     LOG.debug("Copying cycle starts from main order");
                     dto.setCycleStarts(mainOrder.getCycleStarts());
                 }
             }
         }
 
         orderBL.set(dto);
         orderBL.recalculate(entityId);
 
         if (create) {
             LOG.debug("creating order");
 
             dto.setId(null);
             dto.setVersionNum(null);
             for (OrderLineDTO line : dto.getLines()) {
                 line.setId(0);
                 line.setVersionNum(null);
             }
 
             Integer id = orderBL.create(entityId, executorId, dto);
             orderBL.set(id);
             return orderBL.getWS(languageId);
         }
 
 
         return getWSFromOrder(orderBL, languageId);
     }
 
     private OrderWS getWSFromOrder(OrderBL bl, Integer languageId) {
         OrderDTO order = bl.getDTO();
         OrderWS retValue = new OrderWS(order.getId(), order.getBillingTypeId(),
                 order.getNotify(), order.getActiveSince(), order.getActiveUntil(),
                 order.getCreateDate(), order.getNextBillableDay(),
                 order.getCreatedBy(), order.getStatusId(), order.getDeleted(),
                 order.getCurrencyId(), order.getLastNotified(),
                 order.getNotificationStep(), order.getDueDateUnitId(),
                 order.getDueDateValue(), order.getAnticipatePeriods(),
                 order.getDfFm(), order.getIsCurrent(), order.getNotes(),
                 order.getNotesInInvoice(), order.getOwnInvoice(),
                 order.getOrderPeriod().getId(),
                 order.getBaseUserByUserId().getId(),
                 order.getVersionNum(), order.getCycleStarts());
 
         retValue.setPeriodStr(order.getOrderPeriod().getDescription(languageId));
         retValue.setBillingTypeStr(order.getOrderBillingType().getDescription(languageId));
         retValue.setTotal(order.getTotal());
 
         retValue.setMetaFields(MetaFieldBL.convertMetaFieldsToWS(getCallerCompanyId(), order));
 
         List<OrderLineWS> lines = new ArrayList<OrderLineWS>();
         for (Iterator<OrderLineDTO> it = order.getLines().iterator(); it.hasNext();) {
             OrderLineDTO line = (OrderLineDTO) it.next();
 
             if (line.getDeleted() == 0) {
                 OrderLineWS lineWS = new OrderLineWS(line.getId(), line.getItem().getId(), line.getDescription(),
                         line.getAmount(), line.getQuantity(), line.getPrice(),
                         line.getCreateDatetime(), line.getDeleted(), line.getOrderLineType().getId(),
                         line.getEditable(), (line.getPurchaseOrder() != null ? line.getPurchaseOrder().getId() : null),
                         line.getUseItem(), line.getVersionNum(),line.getProvisioningStatusId(),line.getProvisioningRequestId());
 
                 lines.add(lineWS);
             }
         }
         retValue.setOrderLines(new OrderLineWS[lines.size()]);
         lines.toArray(retValue.getOrderLines());
         return retValue;
     }
 
     private InvoiceDTO findInvoice(Integer invoiceId) {
         final InvoiceDTO invoice;
         invoice = new InvoiceBL(invoiceId).getEntity();
         return invoice;
     }
 
     // TODO: This method is not secured or in a jUnit test
     public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId)
             throws SessionInternalError {
         InvoiceWS retValue = null;
         try {
             if (userId == null) {
                 return null;
             }
             InvoiceBL bl = new InvoiceBL();
             Integer invoiceId = bl.getLastByUserAndItemType(userId, itemTypeId);
             if (invoiceId != null) {
                 retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
             }
             return retValue;
         } catch (Exception e) { // forced by SQLException
             LOG.error("Exception in web service: getting latest invoice" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting latest invoice");
         }
     }
 
     /**
      * Return 'number' most recent invoices that contain a line item with an
      * item of the given item type.
      */
     // TODO: This method is not secured or in a jUnit test
     public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number)
             throws SessionInternalError {
         if (userId == null || itemTypeId == null || number == null) {
             return null;
         }
 
         InvoiceBL bl = new InvoiceBL();
         return bl.getManyByItemTypeWS(userId, itemTypeId, number);
     }
 
     // TODO: This method is not secured or in a jUnit test
     public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId)
             throws SessionInternalError {
         if (userId == null) {
             throw new SessionInternalError("User id can not be null");
         }
         if (itemTypeId == null) {
             throw new SessionInternalError("itemTypeId can not be null");
         }
         OrderWS retValue = null;
         // get the info from the caller
         Integer languageId = getCallerLanguageId(); 
 
         // now get the order
         OrderBL bl = new OrderBL();
         Integer orderId = bl.getLatestByItemType(userId, itemTypeId);
         if (orderId != null) {
             bl.set(orderId);
             retValue = bl.getWS(languageId);
         }
         return retValue;
     }
 
     // TODO: This method is not secured or in a jUnit test
     public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number)
             throws SessionInternalError {
         if (userId == null || number == null) {
             return null;
         }
         OrderBL order = new OrderBL();
         return order.getListIdsByItemType(userId, itemTypeId, number);
     }
 
     public String isUserSubscribedTo(Integer userId, Integer itemId) {
         OrderDAS das = new OrderDAS();
         BigDecimal quantity = das.findIsUserSubscribedTo(userId, itemId);
         return quantity != null ? quantity.toString() : null;
     }
 
     public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId) {
         Integer[] result = null;
         OrderDAS das = new OrderDAS();
         result = das.findUserItemsByCategory(userId, categoryId);
         return result;
     }
 
     public ItemDTOEx[] getItemByCategory(Integer itemTypeId) {
         return new ItemBL().getAllItemsByType(itemTypeId);
     }
 
     public ItemTypeWS[] getAllItemCategories() {
         return new ItemTypeBL().getAllItemTypesByEntity(getCallerCompanyId());
     }
 
     public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId,
             String fields) {
         Integer[] itemIds = null;
         if (itemId != null) {
             itemIds = new Integer[] { itemId };
         }
 
         String[] fieldsArray = null;
         if (fields != null) {
             fieldsArray = new String[] { fields };
         }
 
         return doValidatePurchase(userId, itemIds, fieldsArray);
     }
 
     public ValidatePurchaseWS validateMultiPurchase(Integer userId,
             Integer[] itemIds, String[] fields) {
 
         return doValidatePurchase(userId, itemIds, fields);
     }
 
     private ValidatePurchaseWS doValidatePurchase(Integer userId, Integer[] itemIds, String[] fields) {
 
         if (userId == null || (itemIds == null && fields == null)) {
             return null;
         }
 
         List<List<PricingField>> fieldsList = null;
         if (fields != null) {
             fieldsList = new ArrayList<List<PricingField>>(fields.length);
             for (int i = 0; i < fields.length; i++) {
                 fieldsList.add(new ArrayList(Arrays.asList(PricingField.getPricingFieldsValue(fields[i]))));
             }
         }
 
         List<Integer> itemIdsList = null;
         List<BigDecimal> prices = new ArrayList<BigDecimal>();
         List<ItemDTO> items = new ArrayList<ItemDTO>();
 
         if (itemIds != null) {
             itemIdsList = new ArrayList(Arrays.asList(itemIds));
         } else if (fields != null) {
             itemIdsList = new LinkedList<Integer>();
 
             for (List<PricingField> pricingFields : fieldsList) {
                 try {
                     // Since there is no item, run the mediation process rules
                     // to create line/s. This will run pricing and
                     // item management rules as well
 
                     // fields need to be in records
                     Record record = new Record();
                     for (PricingField field : pricingFields) {
                         record.addField(field, false); // don't care about isKey
                     }
                     List<Record> records = new ArrayList<Record>(1);
                     records.add(record);
 
                     PluggableTaskManager<IMediationProcess> tm
                             = new PluggableTaskManager<IMediationProcess>(getCallerCompanyId(),
                                                                           Constants.PLUGGABLE_TASK_MEDIATION_PROCESS);
 
                     IMediationProcess processTask = tm.getNextClass();
 
                     MediationResult result = new MediationResult("WS", false);
                     result.setUserId(userId);
                     result.setEventDate(new Date());
                     ArrayList results = new ArrayList(1);
                     results.add(result);
                     processTask.process(records, results, "WS");
 
                     // from the lines, get the items and prices
                     for (OrderLineDTO line : result.getDiffLines()) {
                         items.add(new ItemBL(line.getItemId()).getEntity());
                         prices.add(line.getAmount());
                     }
 
                 } catch (Exception e) {
                     // log stacktrace
                     StringWriter sw = new StringWriter();
                     PrintWriter pw = new PrintWriter(sw);
                     e.printStackTrace(pw);
                     pw.close();
                     LOG.error("Validate Purchase error: " + e.getMessage() + "\n" + sw.toString());
 
                     ValidatePurchaseWS result = new ValidatePurchaseWS();
                     result.setSuccess(false);
                     result.setAuthorized(false);
                     result.setQuantity(BigDecimal.ZERO);
                     result.setMessage(new String[] { "Error: " + e.getMessage() } );
 
                     return result;
                 }
             }
         } else {
             return null;
         }
 
         // find the prices first
         // this will do nothing if the mediation process was uses. In that case
         // the itemIdsList will be empty
         int itemNum = 0;
         for (Integer itemId : itemIdsList) {
             ItemBL item = new ItemBL(itemId);
 
             if (fieldsList != null && !fieldsList.isEmpty()) {
                 int fieldsIndex = itemNum;
                 // just get first set of fields if only one set
                 // for many items
                 if (fieldsIndex > fieldsList.size()) {
                     fieldsIndex = 0;
                 }
                 item.setPricingFields(fieldsList.get(fieldsIndex));
             }
 
             // todo: validate purchase should include the quantity purchased for validations
             prices.add(item.getPrice(userId, BigDecimal.ONE, getCallerCompanyId()));
             items.add(item.getEntity());
             itemNum++;
         }
 
         ValidatePurchaseWS ret = new UserBL(userId).validatePurchase(items, prices, fieldsList);
         return ret;
     }
 
 
     /**
      * Updates a users stored credit card to the given details. If the given credit card is
      * null then the user's existing credit card will be deleted.
      *
      * @param userId user to update
      * @param creditCard credit card details
      * @throws SessionInternalError
      */
     public void updateCreditCard(Integer userId, com.sapienter.jbilling.server.entity.CreditCardDTO creditCard)
             throws SessionInternalError {
 
         // card can be null, passing null will delete the user's card
         if (creditCard != null) {
             
             if (creditCard.getName() == null || creditCard.getExpiry() == null) {
                 throw new SessionInternalError("Missing credit card name or expiry date");
             }
             
             //if existing card, retrieve from db
             if (creditCard.getId() != null && creditCard.getId().intValue() !=0 ) {
                 creditCard.setHasChanged(true);
                 if (creditCard.getNumber() == null || creditCard.getNumber().contains("*")) {
                     LOG.debug("number was not updated");
                     CreditCardDTO dbCard= new CreditCardDAS().find(creditCard.getId());
                     Calendar newExp= Calendar.getInstance();
                     	newExp.setTime(creditCard.getExpiry());
                     Calendar oldExp= Calendar.getInstance();
                     	oldExp.setTime(dbCard.getExpiry());
                     LOG.debug("New Expiry: " + creditCard.getExpiry() + " VS. old Expiry: " + dbCard.getExpiry());
                     
                     if ( creditCard.getName().equals( dbCard.getName() ) 
                             && newExp.get(Calendar.MONTH) == oldExp.get(Calendar.MONTH)
                             && newExp.get(Calendar.YEAR) == oldExp.get(Calendar.YEAR) ) {
                         LOG.debug("Nothing changed in this credit card, will return");
                         creditCard.setHasChanged(false);
                         return;
                     } else {
                         creditCard.setNumber(dbCard.getNumber());
                     }
                     dbCard= null;
                 }
             }
         }
         //go ahead update the card.
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.updateCreditCard(getCallerId(), userId, creditCard != null ? new CreditCardDTO(creditCard) : null);
     }
 
     /**
      * Deletes a users stored credit card. Payments that were made using the deleted credit
      * card will not be affected.
      *
      * @param userId user to delete the credit card from
      */
     public void deleteCreditCard(Integer userId) {
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.deleteCreditCard(getCallerId(), userId);
     }
 
     /**
      * Updates a users stored ACH details.
      *
      * @param userId user to update
      * @param ach ach details
      * @throws SessionInternalError
      */
     public void updateAch(Integer userId, com.sapienter.jbilling.server.entity.AchDTO ach)
             throws SessionInternalError {
 
         if (ach == null)
             return;
 
         if (ach.getAbaRouting() == null || ach.getBankAccount() == null)
             throw new SessionInternalError("Missing ACH routing number of bank account number.");
 
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.updateACH(userId, getCallerId(), new AchDTO(ach));
     }
 
     /**
      * Deletes a users stored ACH details. Payments that were made using the deleted ACH
      * details will not be affected.
      *
      * @param userId user to delete the ACH details from.
      */
     public void deleteAch(Integer userId) {
         IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
         userSession.removeACH(userId, getCallerId());
     }
 
     public Integer getAuthPaymentType(Integer userId)
             throws SessionInternalError {
 
         IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                 Context.Name.USER_SESSION);
         return sess.getAuthPaymentType(userId);
     }
 
     public void setAuthPaymentType(Integer userId, Integer autoPaymentType, boolean use)
             throws SessionInternalError {
 
         IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                 Context.Name.USER_SESSION);
         sess.setAuthPaymentType(userId, autoPaymentType, use);
     }
 
     public AgeingWS[] getAgeingConfiguration(Integer languageId) throws SessionInternalError {
 	    try {
 		    IBillingProcessSessionBean processSession =
 		    	(IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
 		    AgeingDTOEx[] dtoArr= processSession.getAgeingSteps(getCallerCompanyId(), getCallerLanguageId(), languageId);
 		    AgeingWS[] wsArr= new AgeingWS[dtoArr.length];
 		    AgeingBL bl= new AgeingBL();
 		    for (int i = 0; i < wsArr.length; i++) {
 				wsArr[i]= bl.getWS(dtoArr[i]);
 			}
 		    return wsArr;
 	    } catch (Exception e) {
 	    	throw new SessionInternalError(e);
 	    }
     }
 
     public void saveAgeingConfiguration(AgeingWS[] steps, Integer gracePeriod, Integer languageId) throws SessionInternalError {
     	AgeingBL bl= new AgeingBL();
     	AgeingDTOEx[] dtoList= new AgeingDTOEx[steps.length];
 	    for (int i = 0; i < steps.length; i++) {
 	    	dtoList[i]= bl.getDTOEx(steps[i]);
 		}
 	    IBillingProcessSessionBean processSession =
 	    (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
 	    processSession.setAgeingSteps (getCallerCompanyId(), languageId, bl.validate(dtoList));
 
 	    // update the grace period in another call
 	    IUserSessionBean userSession = (IUserSessionBean) Context.getBean(Context.Name.USER_SESSION);
 	    userSession.setEntityParameter(getCallerCompanyId(),
                                        Constants.PREFERENCE_GRACE_PERIOD,
                                        (gracePeriod != null ? gracePeriod.toString() : null));
     }
 
     /*
         Billing process
      */
 
     public void triggerBillingAsync(final Date runDate) {
 	final Integer companyId = getCallerCompanyId();
 	Thread t = new Thread(new Runnable() {
 	    IBillingProcessSessionBean processBean = Context
 		    .getBean(Context.Name.BILLING_PROCESS_SESSION);
 
 	    public void run() {
 		processBean.trigger(runDate, companyId);
 	    }
 	});
 
 	t.start();
     }
 
     public boolean triggerBilling(Date runDate) {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         return processBean.trigger(runDate, getCallerCompanyId());
     }
 
     public boolean isBillingProcessRunning() {
     	IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         return processBean.isBillingProcessRunning();
 	}
 
     /**
      * Returns the status of the last run (or currently running) billing process.
      *
      * @return billing process status
      */
     public ProcessStatusWS getBillingProcessStatus() {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         return processBean.getBillingProcessStatus(getCallerCompanyId());
     }
 
     public void triggerAgeing(Date runDate) {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         processBean.reviewUsersStatus(getCallerCompanyId(), runDate);
     }
 
     /**
      * Returns true if the ageing process is currently running for the caller's
      * entity, false if not.
      *
      * @return true if ageing process is running, false if not
      */
     public boolean isAgeingProcessRunning() {
         return new AgeingBL().isAgeingProcessRunning(getCallerCompanyId());
     }
 
     /**
      * Returns the status of the last run (or currently running) ageing process.
      *
      * That the ageing process currently does not report a start date, end date, or process id.
      * The status returned by this method will only report the RUNNING/FINISHED/FAILED state of the process.
      *
      * @return ageing process status
      */
     public ProcessStatusWS getAgeingProcessStatus() {
         return new AgeingBL().getAgeingProcessStatus(getCallerCompanyId());
     }
 
     public BillingProcessConfigurationWS getBillingProcessConfiguration() throws SessionInternalError {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         BillingProcessConfigurationDTO configuration = processBean.getConfigurationDto(getCallerCompanyId());
 
         return ConfigurationBL.getWS(configuration);
     }
 
     public Integer createUpdateBillingProcessConfiguration(BillingProcessConfigurationWS ws)
             throws SessionInternalError {
 
     	//validation
     	if (!ConfigurationBL.validate(ws)) {
     		throw new SessionInternalError("Error: Invalid Next Run Date.");
     	}
         BillingProcessConfigurationDTO dto = ConfigurationBL.getDTO(ws);
 
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         return processBean.createUpdateConfiguration(getCallerId(), dto);
     }
 
     public BillingProcessWS getBillingProcess(Integer processId) {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         BillingProcessDTOEx dto = processBean.getDto(processId, getCallerLanguageId());
 
         return BillingProcessBL.getWS(dto);
     }
 
     public Integer getLastBillingProcess() throws SessionInternalError {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         return processBean.getLast(getCallerCompanyId());
     }
 
     public List<OrderProcessWS> getOrderProcesses(Integer orderId) {
         OrderDTO order = new OrderBL(orderId).getDTO();
 
         if (order == null)
             return Collections.emptyList();
 
         List<OrderProcessWS> ws = new ArrayList<OrderProcessWS>(order.getOrderProcesses().size());
         for (OrderProcessDTO process : order.getOrderProcesses())
             ws.add(new OrderProcessWS(process));
 
         return ws;
     }
 
     public List<OrderProcessWS> getOrderProcessesByInvoice(Integer invoiceId) {
         InvoiceDTO invoice = new InvoiceBL(invoiceId).getDTO();
 
         if (invoice == null)
             return Collections.emptyList();
 
         List<OrderProcessWS> ws = new ArrayList<OrderProcessWS>(invoice.getOrderProcesses().size());
         for (OrderProcessDTO process : invoice.getOrderProcesses())
             ws.add(new OrderProcessWS(process));
 
         return ws;
     }
 
     public BillingProcessWS getReviewBillingProcess() {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         BillingProcessDTOEx dto = processBean.getReviewDto(getCallerCompanyId(), getCallerLanguageId());
 
         return BillingProcessBL.getWS(dto);
     }
 
     public BillingProcessConfigurationWS setReviewApproval(Boolean flag) throws SessionInternalError {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
         BillingProcessConfigurationDTO dto = processBean.setReviewApproval(getCallerId(), getCallerCompanyId(), flag);
 
         return ConfigurationBL.getWS(dto);
     }
 
     public List<Integer> getBillingProcessGeneratedInvoices(Integer processId) {
         IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
 
         // todo: IBillingProcessSessionBean#getGeneratedInvoices() should have a proper generic return type
         @SuppressWarnings("unchecked")
         Collection<InvoiceDTO> invoices  = processBean.getGeneratedInvoices(processId);
 
         List<Integer> ids = new ArrayList<Integer>(invoices.size());
         for (InvoiceDTO invoice : invoices)
             ids.add(invoice.getId());
         return ids;
     }
 
 
     /*
        Mediation process
      */
 
     public void triggerMediation() {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         mediationBean.trigger(getCallerCompanyId());
     }
 
     /**
      * Triggers the mediation process for a specific configuration and returns the mediation
      * process id of the running process.
      *
      * @param cfgId mediation configuration id
      * @return mediation process id
      */
     public Integer triggerMediationByConfiguration(Integer cfgId) {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         return mediationBean.triggerMediationByConfiguration(cfgId, getCallerCompanyId());
     }
 
     public boolean isMediationProcessRunning() throws SessionInternalError {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         return mediationBean.isMediationProcessRunning(getCallerCompanyId());
     }
 
     /**
      * Returns the status of the last run (or currently running) mediation process.
      *
      * @return mediation process status
      */
     public ProcessStatusWS getMediationProcessStatus() {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         return mediationBean.getMediationProcessStatus(getCallerCompanyId());
     }
 
     /**
      * Returns the mediation process for the given process id.
      *
      * @param mediationProcessId mediation process id
      * @return mediation process, or null if not found
      */
     public MediationProcessWS getMediationProcess(Integer mediationProcessId) {
         MediationProcess process = new MediationProcessDAS().find(mediationProcessId);
         if (process != null && process.getConfiguration() != null
                 && getCallerCompanyId().equals(process.getConfiguration().getEntityId())) {
             return new MediationProcessWS(process);
         } else {
             return null;
         }
     }
 
     public List<MediationProcessWS> getAllMediationProcesses() {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         List<MediationProcess> processes = mediationBean.getAll(getCallerCompanyId());
 
         // convert to web-service mediation process
         List<MediationProcessWS> ws = new ArrayList<MediationProcessWS>(processes.size());
         for (MediationProcess process : processes)
             ws.add(new MediationProcessWS(process));
         return ws;
     }
 
     public List<MediationRecordLineWS> getMediationEventsForOrder(Integer orderId) {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         List<MediationRecordLineDTO> events = mediationBean.getMediationRecordLinesForOrder(orderId);
 
         return MediationRecordBL.getWS(events);
     }
 
     public List<MediationRecordLineWS> getMediationEventsForInvoice(Integer invoiceId) {
         List<MediationRecordLineDTO> events = new MediationRecordLineDAS().findByInvoice(invoiceId);
         return MediationRecordBL.getWS(events);
     }
 
     public List<MediationRecordWS> getMediationRecordsByMediationProcess(Integer mediationProcessId) {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         List<MediationRecordDTO> records = mediationBean.getMediationRecordsByMediationProcess(mediationProcessId);
 
         return MediationRecordBL.getWS(records);
     }
 
     public List<RecordCountWS> getNumberOfMediationRecordsByStatuses() {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         Map<MediationRecordStatusDTO, Long> records = mediationBean.getNumberOfRecordsByStatuses(getCallerCompanyId());
 
         // convert to a simple object for web-services
         List<RecordCountWS> counts = new ArrayList<RecordCountWS>(records.size());
         for (Map.Entry<MediationRecordStatusDTO, Long> record : records.entrySet())
             counts.add(new RecordCountWS(record.getKey().getId(), record.getValue()));
         return counts;
     }
 
     public List<MediationConfigurationWS> getAllMediationConfigurations() {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
 
         List<MediationConfiguration> configurations = mediationBean.getAllConfigurations(getCallerCompanyId());
         return MediationConfigurationBL.getWS(configurations);
     }
 
     public void createMediationConfiguration(MediationConfigurationWS cfg) {
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
 
         MediationConfiguration dto = MediationConfigurationBL.getDTO(cfg);
         mediationBean.createConfiguration(dto);
     }
 
     public List<Integer> updateAllMediationConfigurations(List<MediationConfigurationWS> configurations)
             throws SessionInternalError {
 
         // update all configurations
         List<MediationConfiguration> dtos = MediationConfigurationBL.getDTO(configurations);
         List<MediationConfiguration> updated;
         try {
             IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
             updated = mediationBean.updateAllConfiguration(getCallerId(), dtos);
         } catch (InvalidArgumentException e) {
             throw new SessionInternalError(e);
         }
 
         // return list of updated ids
         List<Integer> ids = new ArrayList<Integer>(updated.size());
         for (MediationConfiguration cfg : updated)
             ids.add(cfg.getId());
         return ids;
     }
 
     public void deleteMediationConfiguration(Integer cfgId) throws SessionInternalError {
 
         IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
         try {
             mediationBean.delete(getCallerId(), cfgId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
 
     }
 
 
     /*
         Provisioning
      */
 
     public void triggerProvisioning() {
         IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
         provisioningBean.trigger();
     }
 
     public void updateOrderAndLineProvisioningStatus(Integer inOrderId, Integer inLineId, String result)
             throws SessionInternalError {
         IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
         provisioningBean.updateProvisioningStatus(inOrderId, inLineId, result);
     }
 
     public void updateLineProvisioningStatus(Integer orderLineId, Integer provisioningStatus) throws SessionInternalError {
         IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
         provisioningBean.updateProvisioningStatus(orderLineId, provisioningStatus);
     }
 
 
     /*
         Utilities
      */
 
     public void generateRules(String rulesData) throws SessionInternalError {
         try {
             PluggableTaskManager<IRulesGenerator> tm =
                     new PluggableTaskManager<IRulesGenerator>(
                     getCallerCompanyId(),
                     Constants.PLUGGABLE_TASK_RULES_GENERATOR);
             IRulesGenerator rulesGenerator = tm.getNextClass();
 
             rulesGenerator.unmarshal(rulesData);
             rulesGenerator.process();
 
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
 
     /*
         Preferences
      */
 
     public void updatePreferences(PreferenceWS[] prefList) {
         PreferenceBL bl = new PreferenceBL();
         for (PreferenceWS pref: prefList) {
             bl.createUpdateForEntity(getCallerCompanyId(), pref.getPreferenceType().getId(), pref.getValue());
         }
     }
 
     public void updatePreference(PreferenceWS preference) {
         new PreferenceBL().createUpdateForEntity(getCallerCompanyId(),
                                                  preference.getPreferenceType().getId(),
                                                  preference.getValue());
     }
 
     public PreferenceWS getPreference(Integer preferenceTypeId) {
         PreferenceDTO preference = null;
         try {
             preference = new PreferenceBL(getCallerCompanyId(), preferenceTypeId).getEntity();
         } catch (DataAccessException e) {
             /* ignore */
         }
 
         if (preference != null) {
             // return preference if set
             return new PreferenceWS(preference);
 
         } else {
             // preference is not set, return empty
             PreferenceTypeDTO preferenceType = new PreferenceTypeDAS().find(preferenceTypeId);
             return preferenceType != null ? new PreferenceWS(preferenceType) : null;
         }
     }
 
 
     /*
         Currencies
      */
 
     public CurrencyWS[] getCurrencies() {
         CurrencyBL currencyBl = new CurrencyBL();
 
         CurrencyDTO[] currencies;
         try {
             currencies = currencyBl.getCurrencies(getCallerCompanyId(), getCallerLanguageId());
         } catch (SQLException e) {
             throw new SessionInternalError("Exception fetching currencies for entity " + getCallerCompanyId(), e);
         } catch (NamingException e) {
             throw new SessionInternalError("Exception fetching currencies for entity " + getCallerCompanyId(), e);
         }
 
         // Id of the default currency for this entity
         Integer entityDefault = currencyBl.getEntityCurrency(getCallerCompanyId());
 
         // convert to WS
         List<CurrencyWS> ws = new ArrayList<CurrencyWS>(currencies.length);
         for (CurrencyDTO currency : currencies) {
             ws.add(new CurrencyWS(currency, (currency.getId() == entityDefault)));
         }
 
         return ws.toArray(new CurrencyWS[ws.size()]);
     }
 
     public void updateCurrencies(CurrencyWS[] currencies) {
         UserDAS userDAS = new UserDAS();
         PriceModelDAS priceModelDAS = new PriceModelDAS();
         String inUSeCurrencies = "";
         Long inUseCount;
         Boolean currencyInUse = false;
         for (CurrencyWS currency : currencies) {
             if(!currency.getInUse()){
                 inUseCount = 0l;
 
                 //currency in use for users
                 inUseCount += userDAS.findUserCountByCurrency(currency.getId());
 
                 //currency in use for products
                 inUseCount += priceModelDAS.findPriceCountByCurrency(currency.getId());
 
                 if(inUseCount > 0){
                     currencyInUse = true;
                     LOG.debug("Currency "+currency.getCode()+" is in use.");
                     inUSeCurrencies += currency.getCode()+", ";
                 } else{
                     updateCurrency(currency);
                 }
             }

         }
 
         if(currencyInUse){
             inUSeCurrencies = inUSeCurrencies.substring(0,inUSeCurrencies.lastIndexOf(','));
             LOG.debug("Currency(s) "+inUSeCurrencies+" is in use.");
             throw new SessionInternalError("Currency(s) "+inUSeCurrencies+" is in use.");
         }
     }
 
     public void updateCurrency(CurrencyWS ws) {
         CurrencyDTO currency = new CurrencyDTO(ws);
 
         // update currency
         CurrencyBL currencyBl = new CurrencyBL(currency.getId());
         final Integer entityId = getCallerCompanyId();
         currencyBl.update(currency, entityId);
 
         // set as entity currency if flagged as default
         if (ws.isDefaultCurrency()) {
             CurrencyBL.setEntityCurrency(entityId, currency.getId());
         }
 
         // update the description if its changed
         if ((ws.getDescription() != null && !ws.getDescription().equals(currency.getDescription()))) {
             currency.setDescription(ws.getDescription(), getCallerLanguageId());
         }
 
         // update exchange rates for date
         final Date fromDate = ws.getFromDate();
         currencyBl.setOrUpdateExchangeRate(ws.getRateAsDecimal(), entityId, fromDate);
     }
 
     public Integer createCurrency(CurrencyWS ws) {
         CurrencyDTO currency = new CurrencyDTO(ws);
 
         // save new currency
         CurrencyBL currencyBl = new CurrencyBL(currency.getId());
         final Integer entityId = getCallerCompanyId();
         currencyBl.create(currency, entityId);
         if(ws.getRate() != null) {
             currencyBl.setOrUpdateExchangeRate(ws.getRateAsDecimal(), entityId, new Date());
         }
         currency = currencyBl.getEntity();
 
         // set as entity currency if flagged as default
         if (ws.isDefaultCurrency()) {
             currencyBl.setEntityCurrency(entityId, currency.getId());
         }
 
         // set description
         if (ws.getDescription() != null) {
             currency.setDescription(ws.getDescription(), getCallerLanguageId());
         }
 
         return currency.getId();
     }
     
     public boolean deleteCurrency(Integer currencyId) throws SessionInternalError {
     	
         try {
         	CurrencyBL currencyBl = new CurrencyBL(currencyId);
         	return currencyBl.delete();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     public CompanyWS getCompany() {
         CompanyDTO company= new CompanyDAS().find(getCallerCompanyId());
         LOG.debug(company);
         return new CompanyWS(company);
     }
 
     public void updateCompany(CompanyWS companyWS) {
         new EntityBL().updateEntityAndContact(companyWS, getCallerCompanyId(), getCallerId());
     }
 
     /*
        Notifications
      */
 
     public void createUpdateNotification(Integer messageId, MessageDTO dto) {
         if (null == messageId) {
             new NotificationBL().createUpdate(getCallerCompanyId(), dto);
         } else {
             new NotificationBL(messageId).createUpdate(getCallerCompanyId(), dto);
         }
     }
 
     /*Secured via WSSecurityMethodMapper entry.*/
     public void saveCustomerNotes(Integer userId, String notes) {
         CustomerDTO cust = UserBL.getUserEntity(userId).getCustomer();
         if (notes.length() > 1000) {
             throw new SessionInternalError("Customer notes cannot be null", new String[] {"CustomerWS,notes,customer.error.notes.length.exceeded"});
         } else if (null != cust) {
             cust.setNotes(notes);
         } else {
             throw new SessionInternalError("Not a customer");
         }
     }
 
 
     /*
      * Plug-ins
      */
 
     public PluggableTaskWS getPluginWS(Integer pluginId) {
         PluggableTaskDTO dto = new PluggableTaskBL(pluginId).getDTO();
         return new PluggableTaskWS(dto);
     }
 
     public Integer createPlugin(PluggableTaskWS plugin) {
         return new PluggableTaskBL().create(getCallerId(), new PluggableTaskDTO(getCallerCompanyId(), plugin));
     }
 
     public void updatePlugin(PluggableTaskWS plugin) {
         new PluggableTaskBL().update(getCallerId(), new PluggableTaskDTO(getCallerCompanyId(), plugin));
     }
 
     public void deletePlugin(Integer id) {
         new PluggableTaskBL(id).delete(getCallerId());
 
         // invalidate the plug-in cache to clear the deleted plug-in reference
         PluggableTaskDAS pluggableTaskDas = Context.getBean(Context.Name.PLUGGABLE_TASK_DAS);
         pluggableTaskDas.invalidateCache();
     }
 
 
     /*
         Plans and special pricing
      */
 
     public PlanWS getPlanWS(Integer planId) {
         PlanBL bl = new PlanBL(planId);
         return PlanBL.getWS(bl.getEntity());
     }
 
     public List<PlanWS> getAllPlans() {
         List<PlanDTO> plans = new PlanDAS().findAll(getCallerCompanyId());
         return PlanBL.getWS(plans);
     }
 
     public Integer createPlan(PlanWS plan) {
         return new PlanBL().create(PlanBL.getDTO(plan));
     }
 
     public void updatePlan(PlanWS plan) {
         PlanBL bl = new PlanBL(plan.getId());
         bl.update(PlanBL.getDTO(plan));
     }
 
     public void deletePlan(Integer planId) {
         new PlanBL(planId).delete();
     }
 
     public void addPlanPrice(Integer planId, PlanItemWS price) {
         PlanBL bl = new PlanBL(planId);
         bl.addPrice(PlanItemBL.getDTO(price));
     }
 
     public boolean isCustomerSubscribed(Integer planId, Integer userId) {
         PlanBL bl = new PlanBL(planId);
         return bl.isSubscribed(userId);
     }
 
     public Integer[] getSubscribedCustomers(Integer planId) {
         List<CustomerDTO> customers = new PlanBL().getCustomersByPlan(planId);
 
         int i = 0;
         Integer[] customerIds = new Integer[customers.size()];
         for (CustomerDTO customer : customers) {
             customerIds[i++] = customer.getId();
         }
         return customerIds;
     }
 
     public Integer[] getPlansBySubscriptionItem(Integer itemId) {
         List<PlanDTO> plans = new PlanBL().getPlansBySubscriptionItem(itemId);
 
         int i = 0;
         Integer[] planIds = new Integer[plans.size()];
         for (PlanDTO plan : plans) {
             planIds[i++] = plan.getId();
         }
         return planIds;
     }
 
     public Integer[] getPlansByAffectedItem(Integer itemId) {
         List<PlanDTO> plans = new PlanBL().getPlansByAffectedItem(itemId);
 
         int i = 0;
         Integer[] planIds = new Integer[plans.size()];
         for (PlanDTO plan : plans) {
             planIds[i++] = plan.getId();
         }
         return planIds;
     }
 
     public PlanItemWS createCustomerPrice(Integer userId, PlanItemWS planItem) {
         PlanItemDTO dto = PlanItemBL.getDTO(planItem);
         CustomerPriceDTO price = new CustomerPriceBL(userId).create(dto);
 
         return PlanItemBL.getWS(price.getPlanItem());
     }
 
     public void updateCustomerPrice(Integer userId, PlanItemWS planItem) {
         PlanItemDTO dto = PlanItemBL.getDTO(planItem);
         new CustomerPriceBL(userId, dto.getId()).update(dto);
     }
 
     public void deleteCustomerPrice(Integer userId, Integer planItemId) {
         new CustomerPriceBL(userId, planItemId).delete();
     }
 
     public PlanItemWS[] getCustomerPrices(Integer userId) {
         List<PlanItemDTO> prices = new CustomerPriceBL(userId).getCustomerPrices();
         List<PlanItemWS> ws = PlanItemBL.getWS(prices);
         return ws.toArray(new PlanItemWS[ws.size()]);
     }
 
     public PlanItemWS getCustomerPrice(Integer userId, Integer itemId) {
         CustomerPriceBL bl = new CustomerPriceBL(userId);
         return PlanItemBL.getWS(bl.getPrice(itemId));
     }
 
 	public Integer createRateCard(RateCardWS rateCardWs, File rateCardFile) {
 		RateCardDTO rateCardDTO = rateCardWs.toRateCardDTO();
 		rateCardDTO.setCompany(getCompany().getDTO());
 		return new RateCardBL().create(rateCardDTO, rateCardFile);
 	}
 
 	public void updateRateCard(RateCardWS rateCardWs, File rateCardFile) {
 		RateCardDTO rateCardDTO = rateCardWs.toRateCardDTO();
 		rateCardDTO.setCompany(getCompany().getDTO());
 		new RateCardBL(rateCardDTO.getId()).update(rateCardDTO, rateCardFile);
 	}
 
 	public void deleteRateCard(Integer rateCardId) {
 		 new RateCardBL(rateCardId).delete();
 	}
 }
