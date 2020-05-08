 package fr.cg95.cvq.service.authority.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlOptions;
 
 import fr.cg95.cvq.business.authority.LocalReferentialEntry;
 import fr.cg95.cvq.business.authority.LocalReferentialType;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqLocalReferentialException;
 import fr.cg95.cvq.schema.referential.LocalReferentialDocument;
 import fr.cg95.cvq.schema.referential.LocalReferentialEntryType;
 import fr.cg95.cvq.schema.referential.LocalReferentialDocument.LocalReferential.Data;
 import fr.cg95.cvq.schema.referential.LocalReferentialDocument.LocalReferential.Data.Label;
 import fr.cg95.cvq.schema.referential.LocalReferentialEntryType.Entry;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityLifecycleAware;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.ILocalReferentialService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestTypeLifecycleAware;
 
 public class LocalReferentialService 
     implements ILocalReferentialService, ILocalAuthorityLifecycleAware, 
         IRequestTypeLifecycleAware {
 
     private static Logger logger = Logger.getLogger(LocalReferentialService.class);
 
     protected ILocalAuthorityRegistry localAuthorityRegistry;
 
     private String commonReferentialFilename = "local_referential_cvq.xml";
 
     /**
      * A map of (localAuthorityName, {@link Map}(requestTypeLabel, {@link LocalReferentialDocument})).
      */
     private Map<String, Map> localReferentialFileMap = new LinkedHashMap<String, Map>();
 
     /**
      * A map of (localAuthorityName, {@link Map}(requestTypeLabel, parsingTimestamp)).
      * Used to know if local referential file stored on disk changed since parsing time.
      */
     private Map<String, Map> localReferentialTimestampMap = new LinkedHashMap<String, Map>();
     
     /**
      * A map of (dataName, requestTypeLabel).
      */
     private Map<String, String> localReferentialDataMap = new LinkedHashMap<String, String>();
 
     private Set<String> allLocalAuthorities = new LinkedHashSet<String>();
     private Set<IRequestService> parsedRequestServices = new LinkedHashSet<IRequestService>();
     private Set<IRequestService> unparsedRequestServices = new LinkedHashSet<IRequestService>();
 
     /**
      * ILocalAuthorityLifecycleAware interface
      */
     public void addLocalAuthority(final String localAuthorityName) {
 
         logger.debug("adding local authority : " + localAuthorityName);
         synchronized(this) {
             allLocalAuthorities.add(localAuthorityName);
         }
     }
 
     /**
      * ILocalAuthorityLifecycleAware interface
      */
     public void removeLocalAuthority(final String localAuthorityName) {
 
         logger.debug("removing local authority : " + localAuthorityName);
 
         synchronized(this) {
             // remove entries for local referential file map
             localReferentialFileMap.remove(localAuthorityName);
 
             allLocalAuthorities.remove(localAuthorityName);
         }
     }
 
     /**
      * IRequestTypeLifecycleAware interface
      */
     public void addRequestTypeService(final IRequestService service) {
 
         logger.debug("inspecting request type : " + service.getLabel());
 
         if (service.getLocalReferentialFilename() == null) {
             logger.debug("addRequestTypeService() Service does not have a local referential");
             return;
         } else {
             synchronized(this) {
                 unparsedRequestServices.add(service);
             }
         }
     }
 
     /**
      * IRequestTypeLifecycleAware interface
      */
     public void removeRequestType(String requestTypeLabel) {
 
         logger.debug("removing request type : " + requestTypeLabel);
 
         synchronized(this) {
             // remove entries from local referential file map
             Iterator localReferentialFileMapIt = localReferentialFileMap.values().iterator();
             while (localReferentialFileMapIt.hasNext()) {
                 Map requestTypeDoc = (Map) localReferentialFileMapIt.next();
                 requestTypeDoc.remove(requestTypeLabel);
             }
             Iterator localReferentialTimestampMapIt = localReferentialTimestampMap.values().iterator();
             while (localReferentialTimestampMapIt.hasNext()) {
                 Map requestTypeDoc = (Map) localReferentialTimestampMapIt.next();
                 requestTypeDoc.remove(requestTypeLabel);
             }
 
             // remove entries from local referential data map
         }
     }
 
     /**
      * Load and parse the local referential file pointed to by the {@link File} object
      *
      * @throws CvqException if file does not exist or is not parsable
      */
     private LocalReferentialDocument parse(File file)
         throws CvqException {
 
         LocalReferentialDocument refDoc = null;
         try {
             refDoc = LocalReferentialDocument.Factory.parse(file);
         } catch (XmlException xe) {
             logger.error("parse() unable to parse referential data file");
             xe.printStackTrace();
             throw new CvqException("unable to parse referential data file");
         } catch (IOException xe) {
             logger.error("parse() unable to load referential data file");
             xe.printStackTrace();
             throw new CvqException("unable to load referential data file");
         }
 
         return refDoc;
     }
 
     /**
      * update local authority with all already parsed request types
      */
     private void parseLocalReferentialForLocalAuthority(final String localAuthorityName) {
 
         synchronized(this) {
 
             // add entry for local authority and default request type
             addLocalReferentialForLocalAuthority(localAuthorityName,
                                                  commonReferentialFilename, "All");
 
             // for all already known request type services, add an entry for local authority
             Iterator requestServicesIt = parsedRequestServices.iterator();
             while (requestServicesIt.hasNext()) {
                 IRequestService service = (IRequestService) requestServicesIt.next();
                 if (service.getLocalReferentialFilename() != null) {
                     addLocalReferentialForLocalAuthority(localAuthorityName,
                                                          service.getLocalReferentialFilename(),
                                                          service.getLabel());
                 }
             }
         }
     }
 
     /**
      * update table of known request types and data names with currently pending entries
      */
     private void parseDataNamesForPendingRequestServices() {
 
         synchronized(this) {
             if (localReferentialDataMap.isEmpty()) {
                 // add entries for common local referential data
                 addDataNamesForRequestType(commonReferentialFilename, "All");
             }
 
             Iterator unparsedRequestServicesIt = unparsedRequestServices.iterator();
             while (unparsedRequestServicesIt.hasNext()) {
                 IRequestService service = (IRequestService) unparsedRequestServicesIt.next();
                 // add entries for local referential data map
                 addDataNamesForRequestType(service.getLocalReferentialFilename(),
                                            service.getLabel());
 
                 // update all known local authorities with new request type
                 Iterator localAuthoritiesIt = allLocalAuthorities.iterator();
                 while (localAuthoritiesIt.hasNext()) {
                     String localAuthorityName = (String) localAuthoritiesIt.next();
                     addLocalReferentialForLocalAuthority(localAuthorityName,
                                                          service.getLocalReferentialFilename(),
                                                          service.getLabel());
                 }
 
                 parsedRequestServices.add(service);
             }
 
             unparsedRequestServices.clear();
         }
     }
 
     private void checkForDataName(final String dataName)
         throws CvqException {
 
         synchronized(this) {
             if (localReferentialDataMap.get(dataName) == null && unparsedRequestServices.isEmpty()) {
                 throw new CvqException(dataName + " not found !");
             } else {
                 parseDataNamesForPendingRequestServices();
                 if (localReferentialDataMap.get(dataName) == null)
                     throw new CvqException(dataName + " not found !");
             }
         }
     }
 
     /**
      * If it does not exist yet, add data related to the given request type for the given
      * local authority
      */
     private void addLocalReferentialForLocalAuthority(final String localAuthorityName,
                                                       final String localReferentialFilename,
                                                       final String requestTypeLabel) {
 
         synchronized(this) {
             if (localReferentialFileMap.get(localAuthorityName) != null
                 && ((Map) localReferentialFileMap.get(localAuthorityName)).get(requestTypeLabel) != null) {
                 logger.debug("addLocalReferentialForLocalAuthority() " + localAuthorityName
                              + " already contains data for request type " + requestTypeLabel);
                 return;
             }
 
             Date now = new Date();
             File localReferentialFile =
                 localAuthorityRegistry.getLocalAuthorityResource(
                         localAuthorityName, 
                         ILocalAuthorityRegistry.LOCAL_REFERENTIAL_RESOURCE_TYPE, localReferentialFilename, true);
             LocalReferentialDocument refDoc = null;
             try {
                 refDoc = parse(localReferentialFile);
             } catch (CvqException e) {
                 logger.error("Error while parsing local referential file for request type : " 
                         + requestTypeLabel);
                 return;
             }
 
             if (localReferentialFileMap.get(localAuthorityName) == null) {
                 Map<String, LocalReferentialDocument> requestTypeFileMap = 
                     new LinkedHashMap<String, LocalReferentialDocument>();
                 requestTypeFileMap.put(requestTypeLabel, refDoc);
                 localReferentialFileMap.put(localAuthorityName, requestTypeFileMap);
                 Map<String, Long> requestTypeTimestampMap = new LinkedHashMap<String, Long>();
                 requestTypeTimestampMap.put(requestTypeLabel, new Long(now.getTime()));
                 localReferentialTimestampMap.put(localAuthorityName, requestTypeTimestampMap);
             } else {
                 Map requestTypeFileMap = localReferentialFileMap.get(localAuthorityName);
                 requestTypeFileMap.put(requestTypeLabel, refDoc);
                 Map requestTypeTimestampMap = localReferentialTimestampMap.get(localAuthorityName);
                 requestTypeTimestampMap.put(requestTypeLabel, new Long(now.getTime()));
             }
         }
     }
 
     /*
      * Check parsed referential files for the current site and refresh those whose corresponding
      * file has changed on filesystem.
      * 
      * @return whether at least a referential file has been refreshed. 
      */
     private boolean checkCurrentSiteCache() 
             throws CvqException {
     
         synchronized (this) {
 
             String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
             Map requestTypeTimestampMap = localReferentialTimestampMap.get(currentSiteName);
             if (requestTypeTimestampMap == null) {
                 logger.info("checkCurrentSite() timestamp map not initialized, returning");
                 return false;
             }
             Iterator requestTypeTimestampIt = requestTypeTimestampMap.keySet().iterator();
             boolean didARefresh = false;
             while (requestTypeTimestampIt.hasNext()) {
                 String requestTypeLabel = (String) requestTypeTimestampIt.next();
                 Long requestTypeTimestamp = (Long) requestTypeTimestampMap.get(requestTypeLabel);
                 String referentialFileName = getReferentialFilename(requestTypeLabel);
                 File referentialFile =
                     localAuthorityRegistry.getCurrentLocalAuthorityResource(
                             ILocalAuthorityRegistry.LOCAL_REFERENTIAL_RESOURCE_TYPE, 
                             referentialFileName, false);
                 if (referentialFile.lastModified() > requestTypeTimestamp.longValue()) {
                     logger.info("checkCurrentSiteCache() refreshing cache for " + referentialFileName);
                     Map requestTypeFileMap = (Map) localReferentialFileMap.get(currentSiteName);
                     requestTypeFileMap.remove(requestTypeLabel);
                     addLocalReferentialForLocalAuthority(currentSiteName, referentialFileName, requestTypeLabel);
                     didARefresh = true;
                 }
             }
 
             return didARefresh;
         }
     }
     
     /**
      * Return the name of the referential file thant contains referential data related to
      * the given request type label.
      */
     private String getReferentialFilename(final String requestTypeLabel) 
             throws CvqException {
 
         if (requestTypeLabel.equals("All")) {
             return commonReferentialFilename;
         } else {
             Iterator servicesIt = parsedRequestServices.iterator();
             IRequestService associatedService = null;
             while (servicesIt.hasNext()) {
                 IRequestService service = (IRequestService) servicesIt.next();
                 if (service.getLabel().equals(requestTypeLabel))
                     associatedService = service;
             }
 
             if (associatedService == null)
                 throw new CvqException("No service found for " + requestTypeLabel);
 
             return associatedService.getLocalReferentialFilename();
         }
     }
     
     private void addDataNamesForRequestType(final String localReferentialFilename,
                                             final String requestTypeLabel) {
 
         logger.debug("addDataNamesForRequestType() adding file " + localReferentialFilename
                      + " for request type : " + requestTypeLabel);
 
         synchronized(this) {
             File localReferentialFile =
                 localAuthorityRegistry.getReferentialResource(
                         ILocalAuthorityRegistry.LOCAL_REFERENTIAL_RESOURCE_TYPE, 
                         localReferentialFilename);
             if (localReferentialFile == null) {
                 logger.error("File not found");
                 return;
             }
 
             LocalReferentialDocument refDoc = null;
             try {
                 refDoc = parse(localReferentialFile);
             } catch (CvqException e) {
                 logger.error("Error while parsing local referential file for request type : " 
                         + requestTypeLabel);
                 return;
             }
 
             Data[] dataElements = refDoc.getLocalReferential().getDataArray();
             for (int i = 0; i < dataElements.length; i++) {
                 logger.debug("addDataNamesForRequestType() adding data " + dataElements[i].getName() 
                         + " for request type " + requestTypeLabel);
                 localReferentialDataMap.put(dataElements[i].getName(), requestTypeLabel);
             }
         }
     }
 
     private void parseXmlEntries(LocalReferentialEntryType lretXml, LocalReferentialType lrt,
                                  LocalReferentialEntry lre) throws CvqLocalReferentialException {
 
         logger.debug("parseXmlEntries()");
 
         Entry[] entryTab = lretXml.getEntryArray();
         for (int i = 0; i < entryTab.length; i++) {
             Entry entry = entryTab[i];
             logger.debug("parseXmlEntries() parsing entry " + entry.getKey());
 
             LocalReferentialEntry tempLre = new LocalReferentialEntry();
             tempLre.setKey(entry.getKey());
 
             // set labels
             if (entry.sizeOfLabelArray() > 0) {
                 Entry.Label[] labelTab = entry.getLabelArray();
                 for (int j = 0; j < labelTab.length; j++) {
                     Entry.Label label = labelTab[j];
                     tempLre.addLabel(label.getLang(), label.getStringValue());
                 }
             }
 
             // set precisions
             if (entry.isSetPrecision()) {
                 Entry.Precision precision = entry.getPrecision();
                 Map<String, String> labelsMap = new LinkedHashMap<String, String>();
                 if (precision.sizeOfLabelArray() > 0) {
                     Entry.Precision.Label[] labelTab = precision.getLabelArray();
                     for (int j = 0; j < labelTab.length; j++) {
                         Entry.Precision.Label label = labelTab[j];
                         labelsMap.put(label.getLang(), label.getStringValue());
                     }
                 }
                 tempLre.addPrecision(precision.getKey(), labelsMap);
             }
 
             // set messages
             if (entry.sizeOfMessageArray() > 0) {
                 Entry.Message[] messageTab = entry.getMessageArray();
                 for (int j = 0; j < messageTab.length; j++) {
                     Entry.Message message = messageTab[j];
                     tempLre.addMessage(message.getLang(), message.getStringValue());
                 }
             }
 
             // set children entries
             if (entry.isSetEntries()) {
                 LocalReferentialEntryType innerLretXml = entry.getEntries();
                 tempLre.setEntriesSupportPriority(innerLretXml.getSupportPriority());
                 tempLre.setEntriesSupportPrecision(innerLretXml.getSupportPrecision());
                 tempLre.setEntriesSupportMultiple(innerLretXml.getSupportMultiple());
 
                 parseXmlEntries(innerLretXml, lrt, tempLre);
             }
             lrt.addEntry(tempLre, lre);
         }
     }
 
     /**
      * Transform a local referential data type from an XML Beans object
      * to a Model object
      * @throws CvqLocalReferentialException 
      */
     private LocalReferentialType xmlToModel(final String requestLabel,
                                             final Data refData) throws CvqLocalReferentialException {
 
         logger.debug("xmlToModel()");
 
         LocalReferentialType lrt = new LocalReferentialType();
         lrt.setRequest(requestLabel);
         lrt.setDataName(refData.getName());
 
         Label[] labelTab = refData.getLabelArray();
         for (int i = 0; i < labelTab.length; i++) {
             Label label = labelTab[i];
             lrt.addLabel(label.getLang(), label.getStringValue());
         }
 
         if (refData.getEntries() != null) {
             logger.debug("xmlToModel() parsing data entries");
 
             LocalReferentialEntryType lret = refData.getEntries();
 
             // first level, set support-* fields on master type
             lrt.setEntriesSupportPriority(lret.getSupportPriority());
             lrt.setEntriesSupportPrecision(lret.getSupportPrecision());
             lrt.setEntriesSupportMultiple(lret.getSupportMultiple());
 
             parseXmlEntries(lret, lrt, null);
 
         }
 
         return lrt;
     }
 
     private void parseModelEntries(LocalReferentialEntry lre, LocalReferentialEntryType lretXml) {
 
         logger.debug("parseModelEntries()");
 
         Entry entry = lretXml.addNewEntry();
 
         entry.setKey(lre.getKey());
 
         // set labels
         if (lre.getLabelsMap() != null) {
             Iterator labelsIt = lre.getLabelsMap().keySet().iterator();
             while (labelsIt.hasNext()) {
                 String lang = (String) labelsIt.next();
                 String value = (String) lre.getLabelsMap().get(lang);
                 Entry.Label label = entry.addNewLabel();
                 label.setLang(lang);
                 label.setStringValue(value);
             }
         }
 
         // set precisions
         if (lre.getPrecisionsMap() != null) {
             Iterator precisionsIt = lre.getPrecisionsMap().keySet().iterator();
             while (precisionsIt.hasNext()) {
                 String key = (String) precisionsIt.next();
                 Map valuesMap = (Map) lre.getPrecisionsMap().get(key);
                 Entry.Precision precision = entry.addNewPrecision();
                 precision.setKey(key);
                 Iterator valuesIt = valuesMap.keySet().iterator();
                 while (valuesIt.hasNext()) {
                     String lang = (String) valuesIt.next();
                     String value = (String) valuesMap.get(lang);
                     Entry.Precision.Label label = precision.addNewLabel();
                     label.setLang(lang);
                     label.setStringValue(value);
                 }
             }
         }
 
         // set messages
         if (lre.getMessagesMap() != null) {
             Iterator messagesIt = lre.getMessagesMap().keySet().iterator();
             while (messagesIt.hasNext()) {
                 String lang = (String) messagesIt.next();
                 String value = (String) lre.getMessagesMap().get(lang);
                 Entry.Message message = entry.addNewMessage();
                 message.setLang(lang);
                 message.setStringValue(value);
             }
         }
 
         // set children entries
         if (lre.getEntries() != null) {
             Iterator entriesIt = lre.getEntries().iterator();
             LocalReferentialEntryType innerLretXml = entry.addNewEntries();
             innerLretXml.setSupportPriority(lre.getEntriesSupportPriority());
             innerLretXml.setSupportPrecision(lre.getEntriesSupportPrecision());
             innerLretXml.setSupportMultiple(lre.getEntriesSupportMultiple());
 
             while (entriesIt.hasNext()) {
                 LocalReferentialEntry innerLre = (LocalReferentialEntry) entriesIt.next();
                 parseModelEntries(innerLre, innerLretXml);
             }
         }
     }
 
     private Data modelToXml(final LocalReferentialType lrt) {
 
         Data data = Data.Factory.newInstance();
         data.setName(lrt.getDataName());
 
         Map labelsMap = lrt.getLabelsMap();
         Iterator labelsMapIt = labelsMap.keySet().iterator();
         while (labelsMapIt.hasNext()) {
             String lang = (String) labelsMapIt.next();
             String value = (String) labelsMap.get(lang);
             Data.Label label = data.addNewLabel();
             label.setLang(lang);
             label.setStringValue(value);
         }
 
         if (lrt.getEntries() != null) {
             Iterator entriesIt = lrt.getEntries().iterator();
             LocalReferentialEntryType lret = data.addNewEntries();
             lret.setSupportPriority(lrt.getEntriesSupportPriority());
             lret.setSupportPrecision(lrt.getEntriesSupportPrecision());
             lret.setSupportMultiple(lrt.getEntriesSupportMultiple());
 
             while (entriesIt.hasNext()) {
                 LocalReferentialEntry lre = (LocalReferentialEntry) entriesIt.next();
                 parseModelEntries(lre, lret);
             }
         }
 
         return data;
     }
 
     public final Set getAllLocalReferentialData()
         throws CvqException {
 
         logger.debug("getAllLocalReferentialData()");
 
         checkCurrentSiteCache();
         parseDataNamesForPendingRequestServices();
 
         Set resultSet = new LinkedHashSet();
         String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
         parseLocalReferentialForLocalAuthority(currentSiteName);
         Map localAuthorityReferentialDataMap =
             (Map) localReferentialFileMap.get(currentSiteName);
         Iterator localReferentialDocsIt = localAuthorityReferentialDataMap.values().iterator();
         while (localReferentialDocsIt.hasNext()) {
             LocalReferentialDocument refDoc =
                 (LocalReferentialDocument) localReferentialDocsIt.next();
             if (refDoc.getLocalReferential() != null
                 && refDoc.getLocalReferential().sizeOfDataArray() > 0) {
                 Data[] refDataTab = refDoc.getLocalReferential().getDataArray();
                 for (int j = 0; j < refDataTab.length; j++) {
                     logger.debug("getAllLocalReferentialData() parsing " + refDataTab[j].getName());
                     resultSet.add(xmlToModel(refDoc.getLocalReferential().getRequest(),
                                              refDataTab[j]));
                 }
             }
         }
 
         return resultSet;
     }
 
     public Map getAllLocalReferentialDataNames()
         throws CvqException {
 
         logger.debug("getAllLocalReferentialDataNames()");
 
         checkCurrentSiteCache();
         parseDataNamesForPendingRequestServices();
 
         Map resultMap = new LinkedHashMap();
         String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
         parseLocalReferentialForLocalAuthority(currentSiteName);
         Map localAuthorityReferentialDataMap =
             localReferentialFileMap.get(SecurityContext.getCurrentSite().getName().toLowerCase());
         Iterator localReferentialDocsIt = localAuthorityReferentialDataMap.values().iterator();
         while (localReferentialDocsIt.hasNext()) {
             LocalReferentialDocument refDoc =
                 (LocalReferentialDocument) localReferentialDocsIt.next();
             logger.debug("getAllLocalReferentialDataNames() parsing data for request " + refDoc.getLocalReferential().getRequest());
             if (refDoc.getLocalReferential() != null
                 && refDoc.getLocalReferential().sizeOfDataArray() > 0) {
                 Data[] refDataTab = refDoc.getLocalReferential().getDataArray();
                 for (int j = 0; j < refDataTab.length; j++) {
                     String dataName = refDataTab[j].getName();
                     logger.debug("getAllLocalReferentialDataNames() parsing data " + dataName);
                     Map<String, String> labelsMap = new LinkedHashMap<String, String>();
                     Label[] labelTab = refDataTab[j].getLabelArray();
                     for (int k = 0; k < labelTab.length; k++) {
                         Label label = labelTab[k];
                         labelsMap.put(label.getLang(), label.getStringValue());
                     }
                     resultMap.put(dataName, labelsMap);
                 }
             }
         }
 
         return resultMap;
     }
 
     public LocalReferentialType getLocalReferentialDataByName(final String dataName)
         throws CvqException {
 
         checkCurrentSiteCache();
         checkForDataName(dataName);
 
         String requestTypeLabel = (String) localReferentialDataMap.get(dataName);
         String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
         parseLocalReferentialForLocalAuthority(currentSiteName);
         Map localAuthorityReferentialDataMap =
             (Map) localReferentialFileMap.get(currentSiteName);
         LocalReferentialDocument refDoc =
             (LocalReferentialDocument) localAuthorityReferentialDataMap.get(requestTypeLabel);
         if (refDoc.getLocalReferential() != null
             && refDoc.getLocalReferential().sizeOfDataArray() > 0) {
             Data[] refDataTab = refDoc.getLocalReferential().getDataArray();
             for (int j = 0; j < refDataTab.length; j++) {
                 if (refDataTab[j].getName().equals(dataName))
                     return xmlToModel(refDoc.getLocalReferential().getRequest(),
                                       refDataTab[j]);
             }
         }
 
         return null;
     }
 
     public Set getLocalReferentialDataByRequestType(final String requestTypeLabel)
         throws CvqException {
 
         checkCurrentSiteCache();
         parseDataNamesForPendingRequestServices();
         String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
         parseLocalReferentialForLocalAuthority(currentSiteName);
         Map localAuthorityReferentialDataMap =
             (Map) localReferentialFileMap.get(currentSiteName);
         if (localAuthorityReferentialDataMap.get(requestTypeLabel) == null
             && unparsedRequestServices.isEmpty()) {
             throw new CvqException(requestTypeLabel + " not found !");
         } else {
             parseDataNamesForPendingRequestServices();
             if (localAuthorityReferentialDataMap.get(requestTypeLabel) == null)
                 throw new CvqException(requestTypeLabel + " not found !");
         }
 
         Set resultSet = new LinkedHashSet();
         LocalReferentialDocument refDoc =
             (LocalReferentialDocument) localAuthorityReferentialDataMap.get(requestTypeLabel);
         if (refDoc.getLocalReferential() != null
             && refDoc.getLocalReferential().sizeOfDataArray() > 0) {
             Data[] refDataTab = refDoc.getLocalReferential().getDataArray();
             for (int j = 0; j < refDataTab.length; j++) {
                 logger.debug("getLocalReferentialDataByRequestType() parsing "
                              + refDataTab[j].getName());
                 resultSet.add(xmlToModel(refDoc.getLocalReferential().getRequest(),
                                          refDataTab[j]));
             }
         }
 
         return resultSet;
     }
 
    public boolean isLocalReferentialConfigure(final String requestTypeLabel) throws CvqException {
         Set<LocalReferentialType> lrTypes = getLocalReferentialDataByRequestType(requestTypeLabel);
         boolean isConfigure = true;
         for (LocalReferentialType lrType : lrTypes)
             if (lrType.getEntries() == null || lrType.getEntries().isEmpty())
                 isConfigure = false;
         return isConfigure;
     }
     
     public void setLocalReferentialData(LocalReferentialType newLrt)
         throws CvqException {
 
         synchronized(this) {
 
             if (checkCurrentSiteCache())
                 logger.warn("Cache of parsed referential files has been refreshed !");
                 
             checkForDataName(newLrt.getDataName());
 
             String requestTypeLabel = (String) localReferentialDataMap.get(newLrt.getDataName());
             String currentSiteName = SecurityContext.getCurrentSite().getName().toLowerCase();
             parseLocalReferentialForLocalAuthority(currentSiteName);
             Map localAuthorityReferentialDataMap =
                 (Map) localReferentialFileMap.get(currentSiteName);
             LocalReferentialDocument refDoc =
                 (LocalReferentialDocument) localAuthorityReferentialDataMap.get(requestTypeLabel);
             int dataIndex = -1;
             if (refDoc.getLocalReferential() != null
                 && refDoc.getLocalReferential().sizeOfDataArray() > 0) {
                 Data[] refDataTab = refDoc.getLocalReferential().getDataArray();
                 for (int j = 0; j < refDataTab.length; j++) {
                     if (refDataTab[j].getName().equals(newLrt.getDataName()))
                         dataIndex = j;
                 }
             }
 
             refDoc.getLocalReferential().setDataArray(dataIndex, modelToXml(newLrt));
 
             // Set up the validation error listener.
             ArrayList validationErrors = new ArrayList();
             XmlOptions validationOptions = new XmlOptions();
             validationOptions.setErrorListener(validationErrors);
             if (!refDoc.validate(validationOptions)) {
                 Iterator iter = validationErrors.iterator();
                 while (iter.hasNext()) {
                     logger.error("setLocalReferentialData() Validation error : " + iter.next());
                 }
                 throw new CvqLocalReferentialException();
             }
 
             // update in-memory map
             localAuthorityReferentialDataMap.put(requestTypeLabel, refDoc);
 
             String filenameToWriteIn = getReferentialFilename(requestTypeLabel);
             File referentialFile =
                 localAuthorityRegistry.getCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.LOCAL_REFERENTIAL_RESOURCE_TYPE,
                         filenameToWriteIn, false);
             try {
                 XmlOptions opts = new XmlOptions();
                 opts.setSavePrettyPrint();
                 opts.setSavePrettyPrintIndent(4);
                 opts.setUseDefaultNamespace();
                 opts.setCharacterEncoding("UTF-8");
                 refDoc.save(referentialFile, opts);
             } catch (IOException xe) {
                 logger.error("setLocalReferentialData() error while saving referential data file");
                 xe.printStackTrace();
                 throw new CvqException("error while saving referential data file");
             }
         }
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 }
