 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.application.businesslogic;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import org.sola.common.DateUtility;
 import org.sola.common.Money;
 import org.sola.common.SOLAException;
 import org.sola.common.messaging.ServiceMessage;
 import org.sola.services.common.br.ValidationResult;
 import org.sola.services.ejb.administrative.businesslogic.AdministrativeEJBLocal;
 import org.sola.services.ejb.application.repository.entities.Application;
 import org.sola.services.ejb.application.repository.entities.ApplicationActionTaker;
 import org.sola.services.ejb.application.repository.entities.ApplicationActionType;
 import org.sola.services.ejb.application.repository.entities.ApplicationLog;
 import org.sola.services.ejb.application.repository.entities.ApplicationProperty;
 import org.sola.services.ejb.application.repository.entities.ApplicationStatusType;
 import org.sola.services.ejb.application.repository.entities.RequestCategoryType;
 import org.sola.services.ejb.application.repository.entities.RequestType;
 import org.sola.services.ejb.application.repository.entities.Service;
 import org.sola.services.common.ejbs.AbstractEJB;
 import org.sola.common.RolesConstants;
 import org.sola.services.common.faults.SOLAValidationException;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.ejb.application.repository.entities.LodgementTiming;
 import org.sola.services.ejb.application.repository.entities.LodgementView;
 import org.sola.services.ejb.application.repository.entities.LodgementViewParams;
 import org.sola.services.ejb.application.repository.entities.RequestTypeRequiresSourceType;
 import org.sola.services.ejb.application.repository.entities.TypeAction;
 import org.sola.services.ejb.application.repository.entities.ServiceActionTaker;
 import org.sola.services.ejb.application.repository.entities.ServiceActionType;
 import org.sola.services.ejb.application.repository.entities.ServiceBasic;
 import org.sola.services.ejb.application.repository.entities.ServiceStatusType;
 import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
 import org.sola.services.ejb.party.businesslogic.PartyEJBLocal;
 import org.sola.services.ejb.source.businesslogic.SourceEJBLocal;
 import org.sola.services.ejb.source.repository.entities.Source;
 import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
 import org.sola.services.ejb.system.repository.entities.BrValidation;
 import org.sola.services.ejb.transaction.businesslogic.TransactionEJBLocal;
 import org.sola.services.ejb.transaction.repository.entities.RegistrationStatusType;
 import org.sola.services.ejb.transaction.repository.entities.TransactionBasic;
 
 /**
  * Provides methods for managing application objects. <p>
  * <p/>
  */
 @Stateless
 @EJB(name = "java:global/SOLA/ApplicationEJBLocal", beanInterface = ApplicationEJBLocal.class)
 public class ApplicationEJB extends AbstractEJB implements ApplicationEJBLocal {
 
     @EJB
     private PartyEJBLocal partyEJB;
     @EJB
     private SourceEJBLocal sourceEJB;
     @EJB
     private SystemEJBLocal systemEJB;
     @EJB
     private TransactionEJBLocal transactionEJB;
     @EJB
     private AdministrativeEJBLocal administrativeEJB;
     @EJB
     private CadastreEJBLocal cadastreEJB;
 
     @Override
     protected void postConstruct() {
         setEntityPackage(Application.class.getPackage().getName());
     }
 
     private void treatApplicationSources(Application application) {
         if (application.getSourceList() != null) {
             for (Source source : application.getSourceList()) {
                 //Elton. New sources must be sure to have LaNr null. So it can be assigned from
                 // the system.
                 if (source.isNew()) {
                     source.setLaNr(null);
                 }
             }
         }
     }
 
     /**
      * Returns an application based on the id value
      *
      * @param id The id of the application to retrieve
      * @return The found application or null.
      */
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VIEW_APPS)
     public Application getApplication(String id) {
         Application result = null;
         if (id != null) {
             result = getRepository().getEntity(Application.class, id);
         }
         return result;
     }
 
     /**
      * Creates a new application record and any new child objects. <p> Sets the
      * initial action for the application (e.g. lodged) using a business rule.
      * Also sets the lodged date and expected completion date. </p>
      *
      * @param application
      * @return The application after the insert.
      */
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_CREATE_APPS)
     public Application createApplication(Application application) {
         if (application == null) {
             return application;
         }
 
         if (application.getLodgingDatetime() == null) {
             application.setLodgingDatetime(DateUtility.now());
         }
         if (application.getServiceList() != null) {
             for (Service ser : application.getServiceList()) {
                 ser.setLodgingDatetime(application.getLodgingDatetime());
             }
         }
 
         calculateFeesAndDates(application);
         treatApplicationSources(application);
         application.getContactPerson().setTypeCode("naturalPerson");
         application = getRepository().saveEntity(application);
 
         return application;
     }
 
     /**
      * Calculates the lodgement fees as well as the expected completions dates
      * for each service as well as the application.
      *
      * @param application
      * @return
      */
     @Override
     public Application calculateFeesAndDates(Application application) {
         if (application == null) {
             return application;
         }
         calculateCompletionDates(application);
         calculateLodgementFees(application);
         return application;
     }
 
     /**
      * Determines the completion dates for each service based on the number of
      * dates to complete for the service type. Also determines the application
      * complete date as the maximum of the service completion dates.
      *
      * @param application
      */
     private void calculateCompletionDates(Application application) {
 
         //Elton: Not important in which language the request types are asked
         List<RequestType> requestTypes = this.getRequestTypes("en");
         Date baseDate = application.getLodgingDatetime();
         if (baseDate == null) {
             baseDate = DateUtility.now();
         }
         if (application.getServiceList() != null && requestTypes != null) {
             for (Service ser : application.getServiceList()) {
                 ser.setExpectedCompletionDate(
                         calculateServiceCompletionDate(requestTypes, ser, baseDate));
                 application.setExpectedCompletionDate(
                         DateUtility.maxDate(ser.getExpectedCompletionDate(),
                         application.getExpectedCompletionDate()));
             }
         }
     }
 
     private Date calculateServiceCompletionDate(List<RequestType> requestTypes,
             Service ser, Date baseDate) {
         Date result = DateUtility.now();
         for (RequestType type : requestTypes) {
             if (ser.getRequestTypeCode().equals(type.getCode())) {
                 if (type.getNrDaysToComplete() > 0) {
                     result = DateUtility.startOfDay(DateUtility.addDays(baseDate,
                             type.getNrDaysToComplete(), false));
                 }
                 break;
             }
         }
         return result;
     }
 
     /**
      * Calculates the fees applicable for lodgement based on the services that
      * have been associated with the application. Values on the application are
      * updated directly.
      *
      * @param application
      */
     private void calculateLodgementFees(Application application) {
 
         // Get the total area and the total value of the properties on the application
         BigDecimal totalArea = BigDecimal.ZERO;
         Money totalValue = new Money(BigDecimal.ZERO);
         if (application.getPropertyList() != null) {
             for (ApplicationProperty prop : application.getPropertyList()) {
                 if (prop.getArea() != null) {
                     totalArea = totalArea.add(prop.getArea().abs());
                 }
                 if (prop.getTotalValue() != null) {
                     Money propertyValue = new Money(prop.getTotalValue().abs());
                     totalValue = totalValue.plus(propertyValue);
                 }
             }
         }
 
         // Calculate the fee for each service and the total services fee for the application.
         // Uses the money type to ensure all calculations yeild consisent results. Note that the
         // Money type applies Bankers Rounding to all calculations. 
 
         //Elton: Not important in this context what language the request types are asked
         List<RequestType> requestTypes = this.getRequestTypes("en");
         Money servicesFeeTotal = new Money(BigDecimal.ZERO);
         if (application.getServiceList() != null) {
             for (Service ser : application.getServiceList()) {
                 Money baseFee = new Money(BigDecimal.ZERO);
                 Money areaFee = new Money(BigDecimal.ZERO);
                 Money valueFee = new Money(BigDecimal.ZERO);
                 if (requestTypes != null) {
                     for (RequestType type : requestTypes) {
                         if (ser.getRequestTypeCode().equals(type.getCode())) {
                             if (type.getBaseFee() != null) {
                                 baseFee = new Money(type.getBaseFee().abs());
                             }
                             if (type.getAreaBaseFee() != null) {
                                 areaFee = new Money(type.getAreaBaseFee().abs()).times(totalArea);
                             }
                             if (type.getValueBaseFee() != null) {
                                 valueFee = totalValue.times(type.getValueBaseFee().abs());
                             }
                             break;
                         }
                     }
                 }
                 ser.setBaseFee(baseFee.getAmount());
                 ser.setAreaFee(areaFee.getAmount());
                 ser.setValueFee(valueFee.getAmount());
                 servicesFeeTotal = servicesFeeTotal.plus(baseFee).plus(areaFee).plus(valueFee);
             }
         }
 
         // Calculate the tax and the total fee for the application.
         application.setServicesFee(servicesFeeTotal.getAmount());
         Money taxAmount = servicesFeeTotal.times(systemEJB.getTaxRate());
         application.setTax(taxAmount.getAmount());
         application.setTotalFee((servicesFeeTotal.plus(taxAmount)).getAmount());
 
         if (application.getTotalAmountPaid() == null) {
             application.setTotalAmountPaid(BigDecimal.ZERO);
         }
     }
 
     /**
      * Saves changes to the application and child objects. <p> Note that this
      * method should only be used after the application record has been created
      * in the database. Merge by itself will insert a new Application record,
      * but it will not flow the new app id to any new service_in_application
      * objects and cause a db exception. </p>
      *
      * @param application
      * @return The merged application. This is a different object from the
      * application object that was passed in as the Merge action applies the
      * changes from the original application onto records retrieved from the
      * database.
      */
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_CREATE_APPS)
     public Application saveApplication(Application application) {
         if (application == null) {
             return application;
         }
         Date now = DateUtility.now();
         if (application.getServiceList() != null) {
             List<RequestType> requestTypes = this.getRequestTypes("en");
             for (Service ser : application.getServiceList()) {
                 if (ser.isNew()) {
                     //ser.setStatusCode(ServiceStatusType.STATUS_LODGED);
                     //ser.setActionCode(ServiceActionType.LODGED);
                     ser.setLodgingDatetime(now);
                     ser.setExpectedCompletionDate(
                             calculateServiceCompletionDate(requestTypes, ser, now));
                 }
             }
         }
 
         calculateLodgementFees(application);
 
         treatApplicationSources(application);
         application = getRepository().saveEntity(application);
 
         return application;
     }
 
     @Override
    @RolesAllowed(RolesConstants.REPORTS_VIEW)
     public List<LodgementView> getLodgementView(LodgementViewParams params) {
 
         List<LodgementView> result;
         Map queryParams = new HashMap<String, Object>();
         queryParams.put(CommonSqlProvider.PARAM_QUERY, LodgementView.QUERY_GETLODGEMENT);
 
         queryParams.put(LodgementView.PARAMETER_FROM,
                 params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
         queryParams.put(LodgementView.PARAMETER_TO,
                 params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());
 
         result = getRepository().executeFunction(queryParams, LodgementView.class);
         return result;
     }
 
     @Override
    @RolesAllowed(RolesConstants.REPORTS_VIEW)
     public List<LodgementTiming> getLodgementTiming(LodgementViewParams params) {
 
         List<LodgementTiming> result = null;
         Map queryParams = new HashMap<String, Object>();
         queryParams.put(CommonSqlProvider.PARAM_QUERY, LodgementTiming.QUERY_GETLODGEMENT);
 
         queryParams.put(LodgementTiming.PARAMETER_FROM,
                 params.getFromDate() == null ? new GregorianCalendar(1, 1, 1).getTime() : params.getFromDate());
         queryParams.put(LodgementView.PARAMETER_TO,
                 params.getToDate() == null ? new GregorianCalendar(2500, 1, 1).getTime() : params.getToDate());
 
         result = getRepository().executeFunction(queryParams, LodgementTiming.class);
         return result;
     }
 
     @Override
     @RolesAllowed(RolesConstants.REPORTS_VIEW)
     public List<ApplicationLog> getApplicationLog(String applicationId) {
         List<ApplicationLog> result = null;
         if (applicationId == null) {
             return result;
         }
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationLog.QUERY_WHERE_BYAPPLICATIONID);
         params.put(ApplicationLog.QUERY_PARAMETER_APPLICATIONID, applicationId);
         result = getRepository().getEntityList(ApplicationLog.class, params);
 
         return result;
     }
 
     @Override
     public List<ApplicationLog> getUserActions(String username, Date fromTime, Date toTime) {
         List<ApplicationLog> result = null;
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_WHERE_PART, ApplicationLog.QUERY_WHERE_BYTIMESPAN);
         params.put(ApplicationLog.QUERY_PARAMETER_USERNAME, username);
         params.put(ApplicationLog.QUERY_PARAMETER_FROM, fromTime);
         params.put(ApplicationLog.QUERY_PARAMETER_TO, toTime);
         result = getRepository().getEntityList(ApplicationLog.class, params);
         return result;
     }
 
     @Override
     public List<RequestType> getRequestTypes(String languageCode) {
         return getRepository().getCodeList(RequestType.class, languageCode);
     }
 
     @Override
     public List<ApplicationStatusType> getApplicationStatusTypes(String languageCode) {
         return getRepository().getCodeList(ApplicationStatusType.class, languageCode);
     }
 
     @Override
     public List<ApplicationActionType> getApplicationActionTypes(String languageCode) {
         return getRepository().getCodeList(ApplicationActionType.class, languageCode);
     }
 
     @Override
     public List<TypeAction> getTypeActions(String languageCode) {
         return getRepository().getCodeList(TypeAction.class, languageCode);
     }
 
     @Override
     public List<ServiceStatusType> getServiceStatusTypes(String languageCode) {
         return getRepository().getCodeList(ServiceStatusType.class, languageCode);
     }
 
     @Override
     public List<ServiceActionType> getServiceActionTypes(String languageCode) {
         return getRepository().getCodeList(ServiceActionType.class, languageCode);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_SERVICE_COMPLETE)
     public List<ValidationResult> serviceActionComplete(
             String serviceId, String languageCode, int rowVersion) {
         return this.takeActionAgainstService(
                 serviceId, ServiceActionType.COMPLETE, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_SERVICE_REVERT)
     public List<ValidationResult> serviceActionRevert(
             String serviceId, String languageCode, int rowVersion) {
         return this.takeActionAgainstService(
                 serviceId, ServiceActionType.REVERT, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_SERVICE_START)
     public List<ValidationResult> serviceActionStart(
             String serviceId, String languageCode, int rowVersion) {
         return this.takeActionAgainstService(
                 serviceId, ServiceActionType.START, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_SERVICE_CANCEL)
     public List<ValidationResult> serviceActionCancel(
             String serviceId, String languageCode, int rowVersion) {
         return this.takeActionAgainstService(
                 serviceId, ServiceActionType.CANCEL, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_WITHDRAW)
     public List<ValidationResult> applicationActionWithdraw(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.WITHDRAW, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_REJECT)
     public List<ValidationResult> applicationActionCancel(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.CANCEL, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_REQUISITE)
     public List<ValidationResult> applicationActionRequisition(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.REQUISITION, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_VALIDATE)
     public List<ValidationResult> applicationActionValidate(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.VALIDATE, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_APPROVE)
     public List<ValidationResult> applicationActionApprove(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.APPROVE, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_ARCHIVE)
     public List<ValidationResult> applicationActionArchive(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.ARCHIVE, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_DESPATCH)
     public List<ValidationResult> applicationActionDespatch(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.DESPATCH, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_LAPSE)
     public List<ValidationResult> applicationActionLapse(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.LAPSE, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed({RolesConstants.APPLICATION_ASSIGN_TO_OTHERS, RolesConstants.APPLICATION_ASSIGN_TO_YOURSELF})
     public List<ValidationResult> applicationActionAssign(
             String applicationId, String userId, String languageCode, int rowVersion) {
         ApplicationActionTaker application =
                 getRepository().getEntity(ApplicationActionTaker.class, applicationId);
         if (application == null) {
             throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
         }
         application.setAssigneeId(userId);
         application.setAssignedDatetime(Calendar.getInstance().getTime());
         return this.takeActionAgainstApplication(
                 application, ApplicationActionType.ASSIGN, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed({RolesConstants.APPLICATION_UNASSIGN_FROM_OTHERS, RolesConstants.APPLICATION_UNASSIGN_FROM_YOURSELF})
     public List<ValidationResult> applicationActionUnassign(
             String applicationId, String languageCode, int rowVersion) {
         ApplicationActionTaker application =
                 getRepository().getEntity(ApplicationActionTaker.class, applicationId);
         if (application == null) {
             throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
         }
         application.setAssigneeId(null);
         application.setAssignedDatetime(null);
         return this.takeActionAgainstApplication(
                 application, ApplicationActionType.UNASSIGN, languageCode, rowVersion);
     }
 
     @Override
     @RolesAllowed(RolesConstants.APPLICATION_RESUBMIT)
     public List<ValidationResult> applicationActionResubmit(
             String applicationId, String languageCode, int rowVersion) {
         return this.takeActionAgainstApplication(
                 applicationId, ApplicationActionType.RESUBMIT, languageCode, rowVersion);
     }
 
     /**
      * It registers a service of category type informationServices. If it is of
      * another kind of not specified it throws an exception. If the service
      * exists, it is only logged an action of type completed, otherwise it is
      * created.
      *
      * @param service The service to be saved
      * @param languageCode current language code. Used if business rules are
      * invoked.
      * @return
      */
     @Override
     @RolesAllowed({RolesConstants.APPLICATION_PRINT_STATUS_REPORT,
         RolesConstants.ADMINISTRATIVE_BA_UNIT_PRINT_CERT,
         RolesConstants.GIS_PRINT, RolesConstants.SOURCE_PRINT})
     public Service saveInformationService(Service service, String languageCode) {
         RequestType requestType = this.getCodeEntity(
                 RequestType.class, service.getRequestTypeCode());
         if (requestType == null || !requestType.getRequestCategoryCode().equals(
                 RequestCategoryType.INFORMATION_SERVICES)) {
             throw new SOLAException(
                     ServiceMessage.EJB_APPLICATION_SERVICE_REQUEST_TYPE_INFORMATION_REQUIRED);
         }
         Service existingService = this.getRepository().getEntity(Service.class, service.getId());
         if (existingService == null) {
             service.setLodgingDatetime(DateUtility.now());
             service.setExpectedCompletionDate(DateUtility.now());
             existingService = this.saveEntity(service);
         }
         this.serviceActionComplete(
                 existingService.getId(), languageCode, existingService.getRowVersion());
         return existingService;
     }
 
     private List<ServiceActionTaker> getServiceActionTakerList(String applicationId) {
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_WHERE_PART, ServiceActionTaker.QUERY_WHERE_BYAPPLICATIONID);
         params.put(ServiceBasic.QUERY_PARAMETER_APPLICATIONID, applicationId);
         return getRepository().getEntityList(ServiceActionTaker.class, params);
     }
 
     /**
      * It validates a service. For the moment, it is called from the validate
      * method. Perhaps in the future can be used directly to validate a single
      * service.
      *
      * @param service the service
      * @param languageCode the language code to translate the feedback
      * @return
      */
     private List<ValidationResult> validateService(
             ServiceActionTaker service, String languageCode, ServiceActionType serviceActionType) {
         List<ValidationResult> resultList = new ArrayList<ValidationResult>();
 
         // Skip validation for cancelled service
         if (service.getStatusCode() != null && service.getStatusCode().equalsIgnoreCase(ServiceStatusType.STATUS_CANCELLED)) {
             return resultList;
         }
 
         List<BrValidation> brValidationList = this.systemEJB.getBrForValidatingService(
                 serviceActionType.getCode(), service.getRequestTypeCode());
 
         HashMap<String, Serializable> params = new HashMap<String, Serializable>();
         params.put("id", service.getId());
         //Run the validation
         resultList = this.systemEJB.checkRulesGetValidation(
                 brValidationList, languageCode, params);
         if (serviceActionType.getStatusToSet().equals(ServiceStatusType.STATUS_COMPLETED)) {
             resultList.addAll(this.approveApplicationService(
                     service.getId(), service.getStatusCode(),
                     service.getRequestTypeCode(), languageCode, true));
         }
 
         return resultList;
     }
 
     private List<ValidationResult> takeActionAgainstService(
             String serviceId, String actionCode, String languageCode, int rowVersion) {
         ServiceActionTaker service = getRepository().getEntity(ServiceActionTaker.class, serviceId);
         if (service == null) {
             throw new SOLAException(ServiceMessage.EJB_APPLICATION_SERVICE_NOT_FOUND);
         }
 
         ServiceActionType serviceActionType =
                 getRepository().getCode(ServiceActionType.class, actionCode, languageCode);
         List<ValidationResult> validationResultList = this.validateService(
                 service, languageCode, serviceActionType);
         if (systemEJB.validationSucceeded(validationResultList)) {
             transactionEJB.changeTransactionStatusFromService(
                     serviceId, serviceActionType.getStatusToSet());
             service.setStatusCode(serviceActionType.getStatusToSet());
             service.setActionCode(actionCode);
             service.setRowVersion(rowVersion);
             getRepository().saveEntity(service);
         } else {
             throw new SOLAValidationException(validationResultList);
         }
         return validationResultList;
     }
 
     private List<ValidationResult> takeActionAgainstApplication(
             String applicationId, String actionCode, String languageCode, int rowVersion) {
         ApplicationActionTaker application =
                 getRepository().getEntity(ApplicationActionTaker.class, applicationId);
         if (application == null) {
             throw new SOLAException(ServiceMessage.EJB_APPLICATION_APPLICATION_NOT_FOUND);
         }
         return this.takeActionAgainstApplication(application, actionCode, languageCode, rowVersion);
     }
 
     private List<ValidationResult> takeActionAgainstApplication(
             ApplicationActionTaker application, String actionCode,
             String languageCode, int rowVersion) {
 
         List<BrValidation> brValidationList =
                 this.systemEJB.getBrForValidatingApplication(actionCode);
 
         HashMap<String, Serializable> params = new HashMap<String, Serializable>();
         params.put("id", application.getId());
         //Run the validation
         List<ValidationResult> resultList = this.systemEJB.checkRulesGetValidation(
                 brValidationList, languageCode, params);
 
         boolean validationSucceeded = systemEJB.validationSucceeded(resultList);
 
         ApplicationActionType applicationActionType =
                 getRepository().getCode(ApplicationActionType.class, actionCode, languageCode);
 
         // applicationActionType is null if the action is Validate. 
         String statusToSet = applicationActionType == null ? null
                 : applicationActionType.getStatusToSet();
 
         //For each specific action type, can be done extra validations
         if (ApplicationActionType.VALIDATE.equals(actionCode)) {
             //If action lodge or validate
             List<ServiceActionTaker> serviceList =
                     this.getServiceActionTakerList(application.getId());
             ServiceActionType serviceActionType = getRepository().getCode(
                     ServiceActionType.class, ServiceActionType.COMPLETE, languageCode);
             for (ServiceActionTaker service : serviceList) {
                 List<ValidationResult> serviceValidation =
                         this.validateService(service, languageCode, serviceActionType);
                 validationSucceeded = validationSucceeded
                         && systemEJB.validationSucceeded(serviceValidation);
                 resultList.addAll(serviceValidation);
             }
         } else if (ApplicationActionType.APPROVE.equals(actionCode)) {
             brValidationList = this.systemEJB.getBrForValidatingApplication(
                     ApplicationActionType.VALIDATE);
             List<ValidationResult> resultValidationForAppList =
                     this.systemEJB.checkRulesGetValidation(brValidationList, languageCode, params);
             validationSucceeded = validationSucceeded
                     && systemEJB.validationSucceeded(resultValidationForAppList);
             resultList.addAll(resultValidationForAppList);
             List<ServiceActionTaker> serviceList =
                     this.getServiceActionTakerList(application.getId());
             for (ServiceActionTaker service : serviceList) {
                 List<ValidationResult> serviceValidation =
                         this.approveApplicationService(service.getId(), service.getStatusCode(),
                         service.getRequestTypeCode(), languageCode, !validationSucceeded);
                 validationSucceeded = validationSucceeded
                         && systemEJB.validationSucceeded(serviceValidation);
                 resultList.addAll(serviceValidation);
             }
         } else if (ApplicationStatusType.ANULLED.equals(statusToSet)) {
             List<ServiceActionTaker> serviceList =
                     this.getServiceActionTakerList(application.getId());
             for (ServiceActionTaker service : serviceList) {
                 transactionEJB.rejectTransaction(service.getId());
             }
         }
 
         if (validationSucceeded || ApplicationActionType.VALIDATE.equals(actionCode)) {
             if (statusToSet != null) {
                 application.setStatusCode(statusToSet);
             }
             if (ApplicationActionType.VALIDATE.equals(actionCode)) {
                 actionCode = validationSucceeded
                         ? ApplicationActionType.VALIDATE_PASSED : ApplicationActionType.VALIDATE_FAILED;
             }
             application.setActionCode(actionCode);
             application.setRowVersion(rowVersion);
             getRepository().saveEntity(application);
         } else {
             throw new SOLAValidationException(resultList);
         }
         return resultList;
     }
 
     /**
      * It approves the transactions that are hanging to a service.
      *
      * @param service
      * @param languageCode
      * @param validationOnly If true the approval is simulated only for the sake
      * of validation
      * @return
      */
     private List<ValidationResult> approveApplicationService(
             String serviceId, String serviceStatusCode, String serviceRequestTypeCode,
             String languageCode, boolean validationOnly) {
         List<ValidationResult> validationResultList = new ArrayList<ValidationResult>();
 
         if (!validationOnly && serviceStatusCode.equals(ServiceStatusType.STATUS_CANCELLED)) {
             transactionEJB.rejectTransaction(serviceId);
         } else if (serviceStatusCode == null || (serviceStatusCode != null && !serviceStatusCode.equalsIgnoreCase(ServiceStatusType.STATUS_CANCELLED))) {
             // Skip validation for cancelled service
             TransactionBasic transaction =
                     transactionEJB.getTransactionByServiceId(serviceId, false, TransactionBasic.class);
             if (transaction != null) {
                 String statusOnApproval = RegistrationStatusType.STATUS_CURRENT;
                 String actionOnRequestType = getRepository().getCode(RequestType.class,
                         serviceRequestTypeCode, null).getTypeActionCode();
 
                 if (actionOnRequestType != null
                         && actionOnRequestType.equals(TypeAction.CANCEL)) {
                     statusOnApproval = RegistrationStatusType.STATUS_HISTORIC;
                 }
 
                 List<ValidationResult> approvalResult = null;
 
                 approvalResult = administrativeEJB.approveTransaction(
                         transaction.getId(), statusOnApproval, validationOnly, languageCode);
                 validationResultList.addAll(approvalResult);
                 validationOnly = validationOnly || !systemEJB.validationSucceeded(approvalResult);
 
                 approvalResult = sourceEJB.approveTransaction(
                         transaction.getId(), statusOnApproval, validationOnly, languageCode);
                 validationResultList.addAll(approvalResult);
                 validationOnly = validationOnly || !systemEJB.validationSucceeded(approvalResult);
 
                 approvalResult = transactionEJB.approveTransaction(
                         serviceRequestTypeCode, serviceId, languageCode, validationOnly);
                 validationResultList.addAll(approvalResult);
             }
         }
         return validationResultList;
     }
 
     @Override
     public List<RequestCategoryType> getRequestCategoryTypes(String languageCode) {
         return getRepository().getCodeList(RequestCategoryType.class, languageCode);
     }
 }
