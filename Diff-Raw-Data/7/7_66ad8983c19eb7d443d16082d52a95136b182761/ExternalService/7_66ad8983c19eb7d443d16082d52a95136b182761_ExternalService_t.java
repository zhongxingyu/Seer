 package fr.cg95.cvq.external.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 
 import fr.cg95.cvq.business.external.ExternalServiceIdentifierMapping;
 import fr.cg95.cvq.business.external.ExternalServiceIndividualMapping;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.business.users.payment.PaymentState;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.external.IExternalServiceMappingDAO;
 import fr.cg95.cvq.dao.external.IExternalServiceTraceDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.util.Critere;
 
 public class ExternalService implements IExternalService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(ExternalService.class);
 
     private IGenericDAO genericDAO;
     private IExternalServiceTraceDAO externalServiceTraceDAO;
     private IExternalServiceMappingDAO externalServiceMappingDAO;
     private IHomeFolderService homeFolderService;
     private IRequestService requestService;
     
     private ListableBeanFactory beanFactory;
 
     public void init() {
         this.homeFolderService = (IHomeFolderService)
             beanFactory.getBeansOfType(IHomeFolderService.class, false, false).values().iterator().next();
         this.requestService =
             (IRequestService)beanFactory.getBean("defaultRequestService");
     }
 
     @Override
     public boolean authenticate(String externalServiceLabel, String password) {
         IExternalProviderService externalProviderService =
             getExternalServiceByLabel(externalServiceLabel);
         if (externalProviderService == null)
             return false;
         
         ExternalServiceBean esb = getBeanForExternalService(externalProviderService);
         if (esb.getPassword().equals(password))
             return true;
         
         return false;
     }
 
     @Override
     @Context(type=ContextType.AGENT, privilege=ContextPrivilege.WRITE)
     public void sendRequest(Request request) throws CvqException {
 
         // get the external services interested by this request type
         String requestTypeLabel = request.getRequestType().getLabel();
         Set<IExternalProviderService> externalProviderServices = 
             getExternalServicesByRequestType(requestTypeLabel);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return;
         if (!request.getState().equals(RequestState.VALIDATED)) {
             throw new CvqException("Request must be validated before sending",
                 "plugins.externalservices.error.requestMustBeValidated");
         }
         HomeFolder homeFolder = homeFolderService.getById(request.getHomeFolderId());
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             // before sending the request to the external service, eventually set 
             // the external identifiers if they are known ...
             String externalServiceLabel = externalProviderService.getLabel();
             ExternalServiceIdentifierMapping esim = 
                 getIdentifierMapping(externalServiceLabel, homeFolder.getId());
             if (esim != null) {
                 homeFolder.setExternalId(esim.getExternalId());
                 homeFolder.setExternalCapDematId(esim.getExternalCapDematId());
                 for (Individual individual : homeFolder.getIndividuals()) {
                     Set<ExternalServiceIndividualMapping> esimSet =
                         esim.getIndividualsMappings();
                     for (ExternalServiceIndividualMapping esimTemp : esimSet) {
                         if (esimTemp.getIndividualId().equals(individual.getId())) {
                             individual.setExternalId(esimTemp.getExternalId());
                             individual.setExternalCapDematId(esimTemp.getExternalCapDematId());
                             break;
                         }
                     }
                 }
             } else {
                 // no existing external service mapping : create a new one to store
                 // the CapDemat external identifier
                 esim = new ExternalServiceIdentifierMapping();
                 esim.setExternalServiceLabel(externalServiceLabel);
                 esim.setHomeFolderId(homeFolder.getId());
                 esim.setExternalCapDematId(UUID.randomUUID().toString());
                 for (Individual individual : homeFolder.getIndividuals()) {
                     String externalCapDematId = UUID.randomUUID().toString();
                     esim.addIndividualMapping(individual.getId(), externalCapDematId, "");
                     individual.setExternalCapDematId(externalCapDematId);
                 }
                 
                 genericDAO.create(esim);
             }
             ExternalServiceTrace est = null;
             if (!externalProviderService.handlesTraces()) {
                 est = new ExternalServiceTrace();
                 est.setDate(new Date());
                 est.setKeyOwner("capdemat");
                 est.setKey(String.valueOf(request.getId()));
                 est.setName(externalServiceLabel);
             }
             try {
                 String externalId = 
                     externalProviderService.sendRequest(requestService.fillRequestXml(request));
                 if (externalId != null && !externalId.equals("")) {
                     esim.setExternalId(externalId);
                     genericDAO.update(esim);
                 }
                 if (!externalProviderService.handlesTraces()) {
                     est.setStatus(TraceStatusEnum.SENT);
                 }
             } catch (CvqException ce) {
                 logger.error("sendRequest() error while sending request to " 
                         + externalServiceLabel);
                 if (!externalProviderService.handlesTraces()) {
                     est.setStatus(TraceStatusEnum.NOT_SENT);
                 }
             }
             if (!externalProviderService.handlesTraces()) {
                 externalServiceTraceDAO.create(est);
             }
         }
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN)
     public void creditHomeFolderAccounts(Payment payment)
         throws CvqException {
 
         if (!payment.getState().equals(PaymentState.VALIDATED)) {
             logger.info("creditHomeFolderAccounts() not re-routing non validated payment");
             return;
         }
         
         Map<String, List<PurchaseItem>> externalServicesToNotify = 
             new HashMap<String, List<PurchaseItem>>();
         Set<PurchaseItem> purchaseItems = payment.getPurchaseItems();
         for (PurchaseItem purchaseItem : purchaseItems) {
             
             // if purchase item is managed by an external service, 
             // stack it for notification of the associated external service
             if (purchaseItem instanceof ExternalAccountItem) {
                 logger.debug("creditHomeFolderAccounts() item managed by an external service : " 
                         + purchaseItem.getFriendlyLabel());
                 ExternalAccountItem externalAccountItem = (ExternalAccountItem) purchaseItem;
                 externalAccountItem.setSupportedBroker(payment.getBroker());
                 String externalServiceLabel = externalAccountItem.getExternalServiceLabel();
                 if (externalServicesToNotify.get(externalServiceLabel) == null) {
                     externalServicesToNotify.put(externalServiceLabel, 
                             new ArrayList<PurchaseItem>());
                 }
                 externalServicesToNotify.get(externalServiceLabel).add(purchaseItem);
             }
         }
         
         if (!externalServicesToNotify.isEmpty()) {
             for (String externalServiceLabel : externalServicesToNotify.keySet()) {
                 IExternalProviderService service = getExternalServiceByLabel(externalServiceLabel);
                 if (service == null) {
                     logger.error("notifyPayments() No external service with label " + 
                             externalServiceLabel + " has been found");
                     continue;
                 }
                 ExternalServiceIdentifierMapping esim = 
                     getIdentifierMapping(externalServiceLabel, payment.getHomeFolder().getId());
                 service.creditHomeFolderAccounts(externalServicesToNotify.get(externalServiceLabel), 
                         payment.getCvqReference(), payment.getBankReference(), 
                         payment.getHomeFolder().getId(), 
                         esim == null ? null : esim.getExternalCapDematId(), 
                         esim == null ? null : esim.getExternalId(), payment.getCommitDate());
             }
         }
 
     }
 
     @Override
     public boolean hasMatchingExternalService(String requestLabel) {
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestType(requestLabel);
         return externalProviderServices != null
             && !externalProviderServices.isEmpty();
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Date, String> getConsumptionsByRequest(Request request, Date dateFrom, Date dateTo)
         throws CvqException {
 
         Map<Date, String> resultMap = new LinkedHashMap<Date, String>();
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestType(request.getRequestType().getLabel());
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return resultMap;
 
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             Map<Date, String> serviceMap = 
                 externalProviderService.getConsumptionsByRequest(request, dateFrom, dateTo);
             if (serviceMap != null && !serviceMap.isEmpty())
                 resultMap.putAll(serviceMap);            
         }
         
         return resultMap;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<ExternalAccountItem> getExternalAccounts(Long homeFolderId, 
             Set<String> homeFolderRequestTypes, String type) 
         throws CvqException {
 
         Set<ExternalAccountItem> accountsInfoSet = new HashSet<ExternalAccountItem>();
         if (homeFolderRequestTypes == null || homeFolderRequestTypes.isEmpty())
             return accountsInfoSet;
         
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestTypes(homeFolderRequestTypes);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return accountsInfoSet;
 
         for (IExternalProviderService service : externalProviderServices) {
             ExternalServiceBean esb = getBeanForExternalService(service);
             // ask accounts information by home folder or by request
             // according to what the external service supports
             if (esb.supportAccountsByHomeFolder()) {
                 ExternalServiceIdentifierMapping esim = 
                     getIdentifierMapping(service.getLabel(), homeFolderId);
                 Map<String, List<ExternalAccountItem>> homeFolderAccounts = 
                     service.getAccountsByHomeFolder(homeFolderId, 
                             esim == null ? null : esim.getExternalCapDematId(), 
                             esim == null ? null : esim.getExternalId());
                 if (homeFolderAccounts != null && homeFolderAccounts.get(type) != null) {
                     accountsInfoSet.addAll(homeFolderAccounts.get(type));
                 }
             } else if (esb.supportAccountsByRequest()) {
                 logger.warn("getExternalAccounts() accounts by request is currently not supported");
             }
         }
         
         return accountsInfoSet;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId, 
             Set<String> homeFolderRequestTypes)
             throws CvqException {
         
         Map<Individual, Map<String,String>> result = new HashMap<Individual, Map<String,String>>();
         
         if (homeFolderRequestTypes == null || homeFolderRequestTypes.isEmpty())
             return result;
         
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestTypes(homeFolderRequestTypes);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return result;
         
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             ExternalServiceIdentifierMapping esim = 
                 getIdentifierMapping(externalProviderService.getLabel(), homeFolderId);
             Map<Individual, Map<String, String>> serviceResults = 
                 externalProviderService.getIndividualAccountsInformation(homeFolderId, 
                         esim == null ? null : esim.getExternalCapDematId(), 
                         esim == null ? null : esim.getExternalId());
             if (serviceResults != null) {
                 for (Individual individual : serviceResults.keySet()) {
                     if (result.get(individual) == null) {
                         result.put(individual, serviceResults.get(individual));
                     } else {
                         Map<String, String> currentIndividualInfo = result.get(individual);
                         currentIndividualInfo.putAll(serviceResults.get(individual));
                     }
                 }
             }
         }
         
         return result;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
         IExternalProviderService externalProviderService = 
             getExternalServiceByLabel(edai.getExternalServiceLabel());
         externalProviderService.loadDepositAccountDetails(edai);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
         IExternalProviderService externalProviderService = 
             getExternalServiceByLabel(eii.getExternalServiceLabel());
         externalProviderService.loadInvoiceDetails(eii);
     }
 
     @Override
     public Collection<String>
         getRequestTypesForExternalService(String externalServiceLabel) {
        ExternalServiceBean esb =
            getBeanForExternalService(getExternalServiceByLabel(externalServiceLabel));
        return esb == null ? Collections.EMPTY_LIST : esb.getRequestTypes();
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.NONE)
     public Set<String> getGenerableRequestTypes() {
         Set<String> result = new HashSet<String>();
         for (ExternalServiceBean esb :
             SecurityContext.getCurrentConfigurationBean().getExternalServices()
             .values()) {
             if (esb.getGenerateTracedRequest())
                 result.addAll(esb.getRequestTypes());
         }
         return result;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public ExternalServiceIdentifierMapping
         getIdentifierMapping(String externalServiceLabel, Long homeFolderId) {
         return externalServiceMappingDAO
             .getIdentifierMapping(externalServiceLabel, homeFolderId);
     }
 
     /**
      * Get the configuration bean associated to the given external provider service.
      */
     private ExternalServiceBean getBeanForExternalService(IExternalProviderService externalProviderService) {
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         return lacb.getExternalServices().get(externalProviderService);
     }
 
     /**
      * Get the external provider service that has the given label.
      */
     private IExternalProviderService getExternalServiceByLabel(final String externalServiceLabel) {
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Map<IExternalProviderService, ExternalServiceBean> externalProviderServices = 
             lacb.getExternalServices();
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return null;
 
         for (IExternalProviderService service : externalProviderServices.keySet()) {
             if (service.getLabel().equals(externalServiceLabel))
                 return service;
         }
 
         return null;
     }
 
     @Override
     public Set<IExternalProviderService> getExternalServicesByRequestType(final String requestTypeLabel) {
 
         Set<String> requestTypesLabels = new HashSet<String>();
         requestTypesLabels.add(requestTypeLabel);
         return getExternalServicesByRequestTypes(requestTypesLabels);
     }
 
     @Override
     public IExternalProviderService getExternalServiceByRequestType(final String requestTypeLabel) {
         if (hasMatchingExternalService(requestTypeLabel)) {
             return getExternalServicesByRequestType(requestTypeLabel).toArray(new IExternalProviderService[1])[0];
         }
         return null;
     }
 
     /**
      * Get the list of external services objects for the current local authority
      * interested in events about the given request types.
      */
     private Set<IExternalProviderService>
         getExternalServicesByRequestTypes(final Set<String> requestTypesLabels) {
         Set<IExternalProviderService> resultSet =
             new HashSet<IExternalProviderService>();
         for (Map.Entry<IExternalProviderService, ExternalServiceBean> entry :
             SecurityContext.getCurrentConfigurationBean().getExternalServices()
             .entrySet()) {
             for (String requestTypeLabel : requestTypesLabels) {
                 if (entry.getValue().supportRequestType(requestTypeLabel)) {
                     resultSet.add(entry.getKey());
                 }
             }
         }
         return resultSet;
     }
 
     @Override
     @Context(type = ContextType.AGENT, privilege = ContextPrivilege.WRITE)
     public Long addTrace(ExternalServiceTrace trace) {
         trace.setDate(new Date());
         return externalServiceTraceDAO.create(trace);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<ExternalServiceTrace> getTraces(Set<Critere> criteriaSet,
         String sort, String dir) {
         return externalServiceTraceDAO.get(criteriaSet, sort, dir);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void setExternalId(String externalServiceLabel, Long homeFolderId, Long individualId, 
             String externalId) {
         ExternalServiceIndividualMapping newMapping = new ExternalServiceIndividualMapping();
         newMapping.setExternalId(externalId);
         newMapping.setIndividualId(individualId);
         ExternalServiceIdentifierMapping identifierMapping = 
             getIdentifierMapping(externalServiceLabel, homeFolderId);
         Iterator<ExternalServiceIndividualMapping> it = identifierMapping.getIndividualsMappings().iterator();
         while (it.hasNext()) {
             ExternalServiceIndividualMapping esim = it.next();
             if (esim.getIndividualId().equals(individualId)) {
                 newMapping.setExternalCapDematId(esim.getExternalCapDematId());
                 it.remove();
                 break;
             }
         }
         identifierMapping.getIndividualsMappings().add(newMapping);
         genericDAO.update(identifierMapping);
     }
 
     @Override
     @Context(type = ContextType.AGENT, privilege = ContextPrivilege.READ)
     public List<String> checkExternalReferential(Request request) {
         if (!hasMatchingExternalService(request.getRequestType().getLabel()))
             return Collections.<String>emptyList();
         List<String> errors = new ArrayList<String>();
         for (IExternalProviderService eps :
             getExternalServicesByRequestType(request.getRequestType().getLabel())) {
             try {
                 errors.addAll(eps.checkExternalReferential(requestService.fillRequestXml(request)));
             } catch (CvqException e) {
                 errors.add("Erreur lors de la vérification des référentiels pour " + eps.getLabel());
             }
         }
         return errors;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<String, Object> loadExternalInformations(Request request)
         throws CvqException {
         Map<String, Object> informations = new TreeMap<String, Object>();
         HomeFolder homeFolder = homeFolderService.getById(request.getHomeFolderId());
         for (IExternalProviderService eps :
             getExternalServicesByRequestType(request.getRequestType().getLabel())) {
             ExternalServiceIdentifierMapping esim = getIdentifierMapping(eps.getLabel(), homeFolder.getId());
             if (esim != null) {
                 homeFolder.setExternalId(esim.getExternalId());
                 homeFolder.setExternalCapDematId(esim.getExternalCapDematId());
                 for (Individual individual : homeFolder.getIndividuals()) {
                     Set<ExternalServiceIndividualMapping> esimSet =
                         esim.getIndividualsMappings();
                     for (ExternalServiceIndividualMapping esimTemp : esimSet) {
                         if (esimTemp.getIndividualId().equals(individual.getId())) {
                             individual.setExternalId(esimTemp.getExternalId());
                             individual.setExternalCapDematId(esimTemp.getExternalCapDematId());
                             break;
                         }
                     }
                 }
                 try {
                     informations.putAll(
                         eps.loadExternalInformations(requestService.fillRequestXml(request)));
                 } catch (CvqException e) {
                     // TODO JSB
                 }
             }
         }
         return informations;
     }
 
     public void setExternalServiceTraceDAO(IExternalServiceTraceDAO externalServiceTraceDAO) {
         this.externalServiceTraceDAO = externalServiceTraceDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     @Override
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
 
     public void setRequestService(IRequestService requestService) {
         this.requestService = requestService;
     }
 
     public void setExternalServiceMappingDAO(IExternalServiceMappingDAO externalServiceMappingDAO) {
         this.externalServiceMappingDAO = externalServiceMappingDAO;
     }
 }
