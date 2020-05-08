 package org.dialogix.timing;
 
 
 import java.io.Serializable;
 import java.sql.Timestamp; // FIXME - shouldn't we be moving away from sql Timestamps?
 import java.util.*;
 
 import org.dialogix.entities.*;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import java.util.logging.*;
 import org.dialogix.session.*;
 
 /**
 This class consolidates all of the timing functionality, including processing events, and determining response times
  */
 public class DialogixTimingCalculator implements Serializable {
 
     private static final String LoggerName = "org.dialogix.timing.DialogixTimingCalculator";
     private boolean initialized = false;
     private long priorTimePageSentToUser;
     private long timeBeginServerProcessing;
     private long timePageSentToUser;
     private int networkDuration;
     private int serverDuration;
     private int loadDuration;
     private int pageDuration;
     private int totalDuration;
     private int storageDuration;
     private InstrumentSession instrumentSession = null;
     private InstrumentVersion instrumentVersion = null;
     private SubjectSession  subjectSession = null;  // this is only used for running a study which might pre-fill some data
     private PageUsage pageUsage = null;
     private HashMap<String, DataElement> dataElementHash = null;
     private HashMap<Integer, Integer> groupNumVisits = null;
     private HashMap<String, ActionType> actionTypeHash = null;
     private HashMap<String, NullFlavor> nullFlavorHash = null;
     private HashMap<Integer, NullFlavor> nullFlavorIntegerHash = null;
     private HashMap<String, NullFlavorChange> nullFlavorChangeHash = null;
     private HashMap<String, ItemEventsBean> itemEventsHash = null;
     private int pageUsageEventCounter = 0;
     private int itemUsageCounter = 0;
     private boolean finished = false;
     private DialogixEntitiesFacadeLocal dialogixEntitiesFacade = null;
     private HashMap<String, ItemUsage> itemUsageHash = new HashMap<String, ItemUsage>();
     private ArrayList<String> excludedReserveds = new  ArrayList<String>();
     private HashMap<String, String> pageVarList = null;
     private String subjectSessionString = null;
     private String personString = null;
     private String studyString = null;
     private Locale locale;
     private HashMap<String,String> localizedStrings;
     private HashMap<String, String> englishStrings;
     private Hashtable<Long, String> updatedValues;
 
     /**
     Empty constructor to avoid NullPointerException
      */
     public DialogixTimingCalculator() {
         lookupDialogixEntitiesFacade();
         initialized = false;
     }
 
     public String getLocalizedString(String localizeThis) {
         String val = localizedStrings.get(localizeThis);
         if (val == null) {
             Logger.getLogger(LoggerName).log(Level.WARNING,"unable to localize: " + localizeThis);
             return localizeThis;
         }
         else {
             return val;
         }
     }
 
     public void setLocale(Locale loc) {
         this.locale = loc;
         // retrieve hashmap of values from DB, using English as default language
         localizedStrings = new HashMap<String,String>(englishStrings);
         try {
             Iterator<I18n> i18nIterator = dialogixEntitiesFacade.getI18nByIso3language(loc.getISO3Language()).iterator();
             while (i18nIterator.hasNext()) {
                 I18n i18n = i18nIterator.next();
                 localizedStrings.put(i18n.getLabel(), i18n.getVal());
             }
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"no content for Locale", e);
         }
     }
 
     private void setExcludedReserveds() {
         excludedReserveds.add("__BROWSER_TYPE__");
         excludedReserveds.add("__CONNECTION_TYPE__");
         excludedReserveds.add("__CURRENT_LANGUAGE__");
         excludedReserveds.add("__DISPLAY_COUNT__");
         excludedReserveds.add("__IP_ADDRESS__");
         excludedReserveds.add("__LANGUAGES__");
         excludedReserveds.add("__RECORD_EVENTS__");
         excludedReserveds.add("__SCHED_AUTHORS__");
         excludedReserveds.add("__SCHED_VERSION_MAJOR__");
         excludedReserveds.add("__SCHED_VERSION_MINOR__");
         excludedReserveds.add("__STARTING_STEP__");
         excludedReserveds.add("__START_TIME__");
         excludedReserveds.add("__TRICEPS_FILE_TYPE__");
         excludedReserveds.add("__TRICEPS_VERSION_MAJOR__");
         excludedReserveds.add("__TRICEPS_VERSION_MINOR__");
     }
 
     private void initializeInstrumentSession(HashMap<String,String> reserveds, boolean isFromSubjectSession) {
         try {
             groupNumVisits = new HashMap<Integer, Integer>();
 
             int startingStep = instrumentSession.getCurrentVarNum();
 
             Timestamp startTime = new Timestamp(System.currentTimeMillis());
             instrumentSession.setActionTypeId(parseActionType("START"));
             instrumentSession.setBrowser("-");
             instrumentSession.setCurrentGroup(0);   // default in case there are no questions
             instrumentSession.setDisplayNum(0);
             instrumentSession.setFinished(0);
             instrumentSession.setInstrumentStartingGroup(0);  // default in case there are no questions
             instrumentSession.setIpAddress("-");
             instrumentSession.setLanguageCode("en");
             instrumentSession.setLastAccessTime(startTime);
             instrumentSession.setMaxGroupVisited(0);    // default in case there are no questions
             instrumentSession.setMaxVarNumVisited(startingStep);
             instrumentSession.setNumGroups(instrumentVersion.getInstrumentHashId().getNumGroups());
             instrumentSession.setNumVars(instrumentVersion.getInstrumentHashId().getNumVars());
             instrumentSession.setPersonId(null);  // means anonymous
             instrumentSession.setPageUsageCollection(new ArrayList<PageUsage>());
             instrumentSession.setStudyId(null); // means anonymous
             instrumentSession.setStartTime(startTime);
             instrumentSession.setStatusMsg("init");
 
             // Create the collection of DataElements
             ArrayList<DataElement> dataElements = new ArrayList<DataElement>();
             dataElementHash = new HashMap<String, DataElement>();
             int lastVarNumVisited = -1;
 
             Iterator<InstrumentContent> iterator = instrumentVersion.getInstrumentContentCollection().iterator();
             while (iterator.hasNext()) {
                 InstrumentContent instrumentContent = iterator.next();
                 DataElement dataElement = new DataElement();
                 dataElement.setDataElementSequence(instrumentContent.getItemSequence());
                 dataElement.setGroupNum(instrumentContent.getGroupNum());
                 dataElement.setItemUsageCollection(new ArrayList<ItemUsage>());
                 dataElement.setInstrumentContentId(instrumentContent);
                 dataElement.setInstrumentSessionId(instrumentSession);
                 dataElement.setItemVisits(0); // will be incremented again (setting it to 1) with incrementDisplayNum below
                 dataElement.setLastItemUsageId(null);   // initially, there are no itemUsages attached to a dataElement
                 dataElement.setVarNameId(instrumentContent.getVarNameId());
                 dataElements.add(dataElement);
 
                 dataElementHash.put(instrumentContent.getVarNameId().getVarName(), dataElement);
                 if (instrumentContent.getItemSequence() == (startingStep+1)) {
                     instrumentSession.setCurrentGroup(instrumentContent.getGroupNum());
                     instrumentSession.setInstrumentStartingGroup(instrumentContent.getGroupNum());
                     instrumentSession.setMaxGroupVisited(instrumentContent.getGroupNum()); // may be called several times
                     lastVarNumVisited = startingStep;
                 }
                 groupNumVisits.put(dataElement.getGroupNum(), 0);
             }
 
             instrumentSession.setMaxVarNumVisited(lastVarNumVisited);  // last VarNum on the screen of maxGroup
             instrumentSession.setFinished(isFinished() ? 1 : 0);
 
             Iterator<String> reservedKeys = reserveds.keySet().iterator();
             while (reservedKeys.hasNext()) {
                 String varNameString = reservedKeys.next();
                 if (skipReservedWrite(varNameString)) {
                     continue;
                 }
                 String value = reserveds.get(varNameString);
                 instrumentSession.setReserved(varNameString, value);
             }
 
             instrumentSession.setDataElementCollection(dataElements);
             incrementDisplayNum();
 
             // FIXME - should this be here? - might be easier to just pass Integer without ejb lookup
             setPerson(getPersonString());
             setStudy(getStudyString());
 
             dialogixEntitiesFacade.persist(instrumentSession);
 
             if (isFromSubjectSession) {
                 /* Need to get subjectSession from ejb */
                 subjectSession = dialogixEntitiesFacade.findSubjectSessionById(subjectSession.getSubjectSessionId());
                 subjectSession.setInstrumentSessionId(instrumentSession);
                 instrumentSession.setSubjectSessionId(subjectSession);
                 dialogixEntitiesFacade.merge(subjectSession);
             }
 
             dialogixEntitiesFacade.persistHorizontal(instrumentSession.getInstrumentSessionId(),
                     instrumentSession.getInstrumentVersionId().getInstrumentVersionId());
 
             initialized = true;
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"initializeInstrument", e);
         }
     }
 
     /**
      * Either load an instrument from a InstrumentVersion, or restore from an InstrumentSession
      * @param id
      * @param isRestore
      */
     public DialogixTimingCalculator(String src, boolean isRestore, HashMap<String,String> reserveds, HashMap<String,String> requestParams) {
 //        setPriorTimePageSentToUser(getTimeBeginServerProcessing());    // what does this do?
         setExcludedReserveds();
 
         if (requestParams != null) {
             setPersonString(requestParams.get("p"));
             setStudyString(requestParams.get("s"));
             setSubjectSessionString(requestParams.get("ss"));
             setSubjectSession(getSubjectSessionString());
         }
 
         if (subjectSession != null) {
             startOrRestoreSubjectSession(reserveds);
         }
         else {
             Long id = null;
             try {
                 id = Long.parseLong(src);
             } catch (Exception e) {
                 throw new RuntimeException("Unable to find or access instrument " + src);
             }
 
             if (isRestore == true) {
                 restoreInstrumentSession(id);
             }
             else {
                 loadInstrumentVersion(id, reserveds, false);
             }
         }
     }
 
     public String getInstrumentTitle() {
         if (initialized) {
             return instrumentVersion.getVersionString();
         }
         else {
             return "";
         }
     }
 
     /**
      * Load instrument from a InstrumentVersion entry.  This will also make available the source contents needed for the legacy org.dianexus.triceps code.
      * @param id
      */
     private void loadInstrumentVersion(Long id, HashMap<String,String> reserveds, boolean isFromSubjectSession) {   // CHECK THIS
         lookupDialogixEntitiesFacade();
 
         instrumentVersion = dialogixEntitiesFacade.getInstrumentVersion(id);
         if (instrumentVersion == null) {
             throw new RuntimeException("Unable to find InstrumentVersion " + id);
         }
 
         instrumentSession = new InstrumentSession();
         instrumentSession.setInstrumentVersionId(instrumentVersion);
         instrumentSession.setCurrentVarNum(0);
 
         /* Overwrite default reserveds with those from the database - but this might duplicate load from instrument_content */
         Iterator<InstrumentHeader> iterator = instrumentVersion.getInstrumentHeaderCollection().iterator();
         while (iterator.hasNext()) {
             InstrumentHeader instrumentHeader = iterator.next();
             ReservedWord reservedWord = instrumentHeader.getReservedWordId();
             reserveds.put(reservedWord.getReservedWord(), instrumentHeader.getHeaderValue());
         }
         initializeInstrumentSession(reserveds, isFromSubjectSession);
     }
 
     private NullFlavorChange parseNullFlavorChange(NullFlavor null0,
                                                    NullFlavor null1) {
         int code0 = 1;  // FIXME - hack - means Unasked
         int code1 = 0;
         if (null0 != null) {
             code0 = null0.getNullFlavorId();
         }
         if (null1 != null) {
             code1 = null1.getNullFlavorId();
         }
         String code = new StringBuffer().append(code0).append(".").append(code1).toString();
 
         if (nullFlavorChangeHash.containsKey(code)) {
             return nullFlavorChangeHash.get(code);
         }
         else {
             return null;
         }
     }
 
     private void restoreInstrumentSession(Long id) {
         try {
             lookupDialogixEntitiesFacade();
 
             instrumentSession = dialogixEntitiesFacade.getInstrumentSession(id);
             if (instrumentSession == null) {
                 throw new Exception("Unable to find InstrumentSession " + id);
             }
             instrumentVersion = instrumentSession.getInstrumentVersionId();
 
             groupNumVisits = new HashMap<Integer, Integer>();
             dataElementHash = new HashMap<String, DataElement>();
             Iterator<DataElement> iterator = instrumentSession.getDataElementCollection().iterator();
             while (iterator.hasNext()) {
                 DataElement dataElement = iterator.next();
                 String varNameString = dataElement.getVarNameId().getVarName();
                 dataElementHash.put(varNameString, dataElement);
                 groupNumVisits.put(dataElement.getGroupNum(), dataElement.getItemVisits());
             }
             incrementDisplayNum();
             initialized = true;
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"restoreInstrument", e);
         }
     }
 
     private void incrementDisplayNum() {
         instrumentSession.setDisplayNum(instrumentSession.getDisplayNum() + 1);
         setPerPageParams();
     }
 
     /**
     This is called when the server receives a request.  It starts the the clock for server processing time.
     Side effects include:
     (1) Increasing DisplayNum
     (2) Setting DisplayNum, groupNum, and storing that information to pageHits
     @param timestamp	System time in milliseconds
      */
     public void beginServerProcessing() {
         setTimeBeginServerProcessing(System.currentTimeMillis());
         if (initialized) {
             incrementDisplayNum();
         }
     }
 
     public void logBrowserInfo(String ipAddress,
                         String userAgent) {
         try {
             if (initialized) {
                 instrumentSession.setBrowser(userAgent);
                 instrumentSession.setIpAddress(ipAddress);
                 if (pageUsage != null) {
                     pageUsage.setBrowser(userAgent);
                     pageUsage.setIpAddress(ipAddress);
                 }
             }
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"logBrowserInfo",e);
         }
     }
 
     private void setPerPageParams() {
         try {
             if (instrumentSession.getDisplayNum() > 1) {
                 pageUsage = new PageUsage();
                 pageUsageEventCounter = 0;
                 pageUsage.setPageUsageEventCollection(new ArrayList<PageUsageEvent>());
                 pageUsage.setFromGroupNum(instrumentSession.getCurrentGroup());
                 int priorVisitCount = groupNumVisits.get(pageUsage.getFromGroupNum());
                 groupNumVisits.put(pageUsage.getFromGroupNum(), priorVisitCount + 1);
                 pageVarList = new HashMap<String,String>();
             }
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"beginServerProcessing", e);
         }
     }
 
     /**
     This is called when the server is ready to return a response to the dialogixUser.
     ServerProcessingTime = (finishServerProcessing.timestamp - beginServerProcessing.timestamp)
     @param timestamp	Current system time
      */
     public void finishServerProcessing(String eventString) {
         try {
             Timestamp ts = new Timestamp(System.currentTimeMillis());   // this is when page was sent to user
             setTimePageSentToUser(System.currentTimeMillis());
 
             // Update Session State
             instrumentSession.setLastAccessTime(ts);    // time page sent to user
 
             if (pageVarList != null) {
 //                Logger.getLogger(LoggerName).log(Level.WARNING, "**" + eventString);
                 Logger.getLogger(LoggerName).warning(eventString);
                 processEvents(eventString);
 
                 Iterator<String> varNames = pageVarList.keySet().iterator();
                 while (varNames.hasNext()) {
                     String varNameString = varNames.next();
 //                    Logger.getLogger(LoggerName).log(Level.WARNING,"==procesEvents for " + varNameString);
                     ItemUsage itemUsage = null;
                     if (itemUsageHash.containsKey(varNameString)) {
                         itemUsage = itemUsageHash.get(varNameString);
                     }
                     else {
                         Logger.getLogger(LoggerName).log(Level.WARNING,"Unable to find variable " + varNameString);
                         continue;
                     }
 //                    Long id = itemUsageHash.get(varNameString).getItemUsageId(); // needed to get original itemUsage from ejb store; but eval nodes aren't persisted yet
 //                    if (id == null) {
 //                        Logger.getLogger(LoggerName).log(Level.SEVERE,"null id for supposedly persisted ItemUsage " + varNameString);
 //                        continue;
 //                    }
 //                    itemUsage = dialogixEntitiesFacade.getItemUsage(id);
 //                    if (itemUsage == null) {
 //                        Logger.getLogger(LoggerName).log(Level.SEVERE,"Unable to retrieve ejb for ItemUsage " + varNameString);
 //                        continue;
 //                    }
                     try {
                         ItemEventsBean itemEventsBean = itemEventsHash.get(varNameString);
                         if (itemEventsBean != null) {
                             Logger.getLogger(LoggerName).warning(varNameString + ": " + itemEventsBean);
                             itemUsage.setResponseLatency(itemEventsBean.getTotalResponseLatency());
                             itemUsage.setResponseDuration(itemEventsBean.getTotalResponseDuration());
                             itemUsage.setVacillation(itemEventsBean.getVacillation());
                         }
 //                        else {
 //                            itemUsage.setResponseDuration(-1);
 //                            itemUsage.setResponseLatency(-1);
 //                            itemUsage.setVacillation(0);
 //                        }
                     } catch (NullPointerException e) {
                         Logger.getLogger(LoggerName).log(Level.WARNING,"No events found for varName " + varNameString);
                     }
                 }
             }
 
             // Need to determine GroupNum from VarNum
             Iterator<DataElement> iterator = instrumentSession.getDataElementCollection().iterator();
             while (iterator.hasNext()) {
                 DataElement dataElement = iterator.next();
                 if (dataElement.getDataElementSequence() == instrumentSession.getCurrentVarNum()) {
                     instrumentSession.setCurrentGroup(dataElement.getGroupNum());
                 }
             }
             instrumentSession.setLanguageCode(getLangCode());
 
             if (pageUsage != null) {
                 pageUsage.setDisplayNum(instrumentSession.getDisplayNum() - 1); // since relates to prior page
                 pageUsage.setLanguageCode(instrumentSession.getLanguageCode());
                 pageUsage.setToGroupNum(instrumentSession.getCurrentGroup());
                 pageUsage.setActionTypeId(instrumentSession.getActionTypeId());
                 pageUsage.setPageVisits(groupNumVisits.get(pageUsage.getFromGroupNum()));   // should be about prior page
                 pageUsage.setStatusMsg(instrumentSession.getStatusMsg());
                 pageUsage.setServerSendTime(new Timestamp(getPriorTimePageSentToUser()));   // time page was sent to user
 
                 Runtime rt = Runtime.getRuntime();
                 pageUsage.setUsedJvmMemory(rt.totalMemory() - rt.freeMemory());
 
                 setServerDuration((int) (getTimePageSentToUser() - getTimeBeginServerProcessing()));
                 setTotalDuration((int) (getTimeBeginServerProcessing() - getPriorTimePageSentToUser()));
                 setNetworkDuration(getTotalDuration() - getPageDuration() - getLoadDuration());
 
                 pageUsage.setLoadDuration(getLoadDuration());
                 pageUsage.setNetworkDuration(getNetworkDuration());
                 pageUsage.setPageDuration(getPageDuration());
                 pageUsage.setServerDuration(getServerDuration());
                 pageUsage.setTotalDuration(getTotalDuration());
                 pageUsage.setStorageDuration(getStorageDuration()); // will be value from last page
 
                 pageUsage.setInstrumentSessionId(instrumentSession);
                 instrumentSession.getPageUsageCollection().add(pageUsage);
             }
 
             setPriorTimePageSentToUser(getTimePageSentToUser());
             instrumentSession.setFinished(isFinished() ? 1 : 0);
 
             dialogixEntitiesFacade.merge(instrumentSession);
 
             dialogixEntitiesFacade.mergeHorizontal(instrumentSession.getInstrumentSessionId(), 
                       instrumentSession.getInstrumentVersionId().getInstrumentVersionId(), updatedValues);
             updatedValues = null; // to reset them
 
             setStorageDuration((int) (System.currentTimeMillis() - getTimePageSentToUser()));
 
             // set server processing time, then re-merge so part of current record/ - no, make part of next pageUsage record
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"finishServerProcessing", e);
         }
     }
 
     private Answer findAnswerId(DataElement dataElement, String encodedAnswer) {
         try {
             AnswerList answerList = dataElement.getInstrumentContentId().getItemId().getAnswerListId();
             if (answerList == null) {
                 return null;
             }
             Iterator<AnswerListContent> iterator = answerList.getAnswerListContentCollection().iterator();
             while (iterator.hasNext()) {
                 AnswerListContent answerListContent = iterator.next();
                 if (answerListContent.getAnswerCode().equals(encodedAnswer)) {
                     return answerListContent.getAnswerId();
                 }
             }
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"Unable to find AnswerId for " + encodedAnswer, e);
         }
         return null;
     }
 
     /**
      * This sets the values of a question before they are asked, so will not include the answer, but may include default (or prior) answers
      * @param VarNameString
      * @param questionAsAsked
      * @param answerCode
      * @param answerString
      * @param comment
      * @param timestamp
      * @param nullFlavor
      */
     public void writeNodePreAsking(String varNameString, String questionAsAsked, Date timestamp) {
         try {
             DataElement dataElement = dataElementHash.get(varNameString);
             if (dataElement == null) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"Attempt to pre-write to unitialized DataElement " + varNameString);
                 return;
             }
             dataElement.setItemVisits(dataElement.getItemVisits() + 1);
 
             ItemUsage itemUsage = new ItemUsage();
             itemUsage.setDisplayNum(instrumentSession.getDisplayNum());
             itemUsage.setItemUsageSequence(++itemUsageCounter);
             itemUsage.setItemVisit(dataElement.getItemVisits());
             itemUsage.setLanguageCode(instrumentSession.getLanguageCode());
             itemUsage.setQuestionAsAsked(questionAsAsked);
             itemUsage.setTimeStamp(timestamp);
             itemUsage.setWhenAsMs(timestamp.getTime());
 
             itemUsage.setDataElementId(dataElement);
             dataElement.getItemUsageCollection().add(itemUsage);
 
             itemUsageHash.put(varNameString, itemUsage);
 
             // Compute position relative to end
             if (dataElement.getGroupNum() > instrumentSession.getMaxGroupVisited()) {
                 instrumentSession.setMaxGroupVisited(dataElement.getGroupNum());
             }
             if (dataElement.getDataElementSequence() > instrumentSession.getMaxVarNumVisited()) {
                 instrumentSession.setMaxVarNumVisited(dataElement.getDataElementSequence());
             }
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"WriteNodePreAsking Error", e);
         }
     }
 
     /**
      * Interface for writing values of data elements
      * @param varNameString
      * @param questionAsAsked
      * @param answerCode
      * @param answerString
      * @param comment
      * @param timestamp
      * @param nullFlavor
      */
     public void writeNode(String varNameString, String answerCode, String answerString, String comment, Integer nullFlavor, boolean isEval) {
        if (!initialized) {
             return;
         }
         try {
             // Update in-memory (and persisted) data store
             DataElement dataElement = dataElementHash.get(varNameString);
             if (dataElement == null) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"Attempt to write to unitialized DataElement " + varNameString);
                 return;
             }
 
             ItemUsage itemUsage;
             Long id = null;
             NullFlavor oldNullFlavor = null;
             ItemUsage oldItemUsage = dataElement.getLastItemUsageId();
             if (oldItemUsage != null) {
                 oldNullFlavor = oldItemUsage.getNullFlavorId();
             }
 
             if (itemUsageHash.containsKey(varNameString)) {
                 itemUsage = itemUsageHash.get(varNameString);
             }
             else {
                 Logger.getLogger(LoggerName).log(Level.WARNING,"Unable to find variable " + varNameString);
                 return;
             }
             if (!isEval) {
                 id = itemUsageHash.get(varNameString).getItemUsageId(); // needed to get original itemUsage from ejb store; but eval nodes aren't persisted yet
                 if (id == null) {
                     Logger.getLogger(LoggerName).log(Level.SEVERE,"null id for supposedly persisted ItemUsage " + varNameString);
                     return;
                 }
                 itemUsage = dialogixEntitiesFacade.getItemUsage(id);
             }
             if (itemUsage == null) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"Unable to retrieve ejb for ItemUsage " + varNameString);
                 return;
             }
             // update itemusage in hashtable so don't have to retreive it again
             itemUsageHash.put(varNameString, itemUsage);
 
             dataElement.setLastItemUsageId(itemUsage);
 
             itemUsage.setAnswerCode(answerCode);
             itemUsage.setAnswerId(findAnswerId(dataElement,answerCode));
             itemUsage.setAnswerString(answerString);
             itemUsage.setComments(comment);
             itemUsage.setNullFlavorId(parseNullFlavor(nullFlavor));
             NullFlavorChange nullFlavorChange = parseNullFlavorChange(oldNullFlavor, itemUsage.getNullFlavorId());
             if (nullFlavorChange.getNullFlavorChangeId() == 1) {
                 /* Means Ok2Ok - so check whether really changed */
                 if (itemUsage.getAnswerCode() != null && oldItemUsage.getAnswerCode() != null && itemUsage.getAnswerCode().equals(oldItemUsage.getAnswerCode())) {
                     nullFlavorChange = nullFlavorChangeHash.get("-");
                 }
             }
             itemUsage.setNullFlavorChangeId(nullFlavorChange);
 //            itemUsage.setTimeStamp(timestamp);
 //            itemUsage.setWhenAsMs(timestamp.getTime());
 
             itemUsage.setDataElementId(dataElement);
             dataElement.getItemUsageCollection().add(itemUsage);
 
             pageVarList.put(varNameString,varNameString); // needed for processing event strings later
 
             if (dataElement.getGroupNum() > instrumentSession.getMaxGroupVisited()) {
                 instrumentSession.setMaxGroupVisited(dataElement.getGroupNum());
             }
             if (dataElement.getDataElementSequence() > instrumentSession.getMaxVarNumVisited()) {
                 instrumentSession.setMaxVarNumVisited(dataElement.getDataElementSequence());
             }
 
             updateColumnValue(dataElement.getVarNameId().getVarNameId(), answerCode);
 
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"WriteNode Error", e);
         }
     }
 
 
     public void updateColumnValue(Long column, String value) {
         if (updatedValues == null) {
             updatedValues = new Hashtable<Long,String>();
         }
         updatedValues.put(column, value);
     }
 
     public void writeReserved(String reservedName, String value) {
         try {
             instrumentSession.setReserved(reservedName, value);
         } catch (Throwable e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"WriteReserved Error for (" + reservedName + "," + value +")", e);
         }
     }
 
     /**
      * Takes parameter src which contains the event timing data from the
      * http request parameter EVENT_TIMINGS and creates an event timing bean for each
      * event.
      *
      * @param eventStrng
      * @return
      */
     private void processEvents(String eventString) {
         if (eventString == null || eventString.trim().length() == 0) {
             return;
         }
         if (initialized == false || pageUsage == null) {
             return;
         }
         itemEventsHash = new HashMap<String,ItemEventsBean>();
 
         StringTokenizer st = new StringTokenizer(eventString, "\t", false);
         int tokenCount = st.countTokens();
         int priorBlurTime = -1;
         int itemEventCount = 0;
         String priorVarName = null;
         for (int count = 1; st.hasMoreTokens(); ++count) {
             String src = st.nextToken();
             PageUsageEvent pageUsageEvent = tokenizeEventString(src);
             if (pageUsageEvent.getVarName() == null || pageUsageEvent.getVarName().trim().equals("") || pageUsageEvent.getVarName().trim().equals("null")) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"Null Varname in " + src);
                 continue;
             }
             else if (pageUsageEvent.getVarName().trim().equals("undefined")) {
                 continue;
             }
             pageUsage.getPageUsageEventCollection().add(pageUsageEvent);
 
             if (!pageUsageEvent.getVarName().equals(priorVarName)) {
                 itemEventCount = 0;
             }
             ++itemEventCount;
 
             if (itemEventsHash.containsKey(pageUsageEvent.getVarName())) {
                 itemEventsHash.get(pageUsageEvent.getVarName()).processPageUsageEvent(pageUsageEvent,priorBlurTime,itemEventCount);
             }
             else {
                 itemEventsHash.put(pageUsageEvent.getVarName(), new ItemEventsBean(pageUsageEvent.getVarName()));
                 itemEventsHash.get(pageUsageEvent.getVarName()).processPageUsageEvent(pageUsageEvent,priorBlurTime,itemEventCount);
             }
 
             if (count == 1) {
                 setLoadDuration(pageUsageEvent.getDuration());
             }
             if (count == tokenCount) {
                 setPageDuration(pageUsageEvent.getDuration() - getLoadDuration());
             }
             priorBlurTime = pageUsageEvent.getDuration();
             priorVarName = pageUsageEvent.getVarName();
         }
     }
 
     private void startOrRestoreSubjectSession(HashMap<String,String> reserveds) {
         InstrumentSession _instrumentSession = subjectSession.getInstrumentSessionId();
         if (_instrumentSession != null) {
             restoreInstrumentSession(_instrumentSession.getInstrumentSessionId());
         }
         else {
             loadInstrumentVersion(subjectSession.getInstrumentVersionId().getInstrumentVersionId(), reserveds, true);
             // If there are startup variables, set them now
             Iterator<SubjectSessionData> subjectSessionDataCollection = subjectSession.getSubjectSessionDataCollection().iterator();
             while (subjectSessionDataCollection.hasNext()) {
                 SubjectSessionData subjectSessionData = subjectSessionDataCollection.next();
                 this.writeNodePreAsking(subjectSessionData.getVarName(), "", new Timestamp(System.currentTimeMillis()));
             }
             this.finishServerProcessing(null);
             this.beginServerProcessing();
             while (subjectSessionDataCollection.hasNext()) {
                 SubjectSessionData subjectSessionData = subjectSessionDataCollection.next();
                 this.writeNode(subjectSessionData.getVarName(), subjectSessionData.getValue(), "", "", null, false);
             }
         }
         if (_instrumentSession != null) {
           dialogixEntitiesFacade.persistHorizontal(_instrumentSession.getInstrumentSessionId(),
                   _instrumentSession.getInstrumentVersionId().getInstrumentVersionId());
         }
     }
 
     /**
     Parses a single line from Event Timings, storing them within an PageUsageEvent
      * @param src
      * @return
      */
     private PageUsageEvent tokenizeEventString(String src) {
         PageUsageEvent pageUsageEvent = new PageUsageEvent();
 
         StringTokenizer str = new StringTokenizer(src, ",", false);
         int tokenCount = 0;
         while (str.hasMoreTokens()) {
             String token = str.nextToken();
             switch (tokenCount) {
                 case 0: {
                     pageUsageEvent.setVarName(token);
                     break;
                 }
                 case 1: {
                     pageUsageEvent.setGuiActionType(token);
                     break;
                 }
                 case 2: {
                     pageUsageEvent.setEventType(token);
                     break;
                 }
                 case 3: {
                     long ts = new Long(token).longValue();
                     Date d = new Date(ts);
                     Timestamp tms = new Timestamp(d.getTime());
                     pageUsageEvent.setTimeStamp(tms);
                     break;
                 }
                 case 4: {
                     pageUsageEvent.setDuration(new Integer(token).intValue());
                     break;
                 }
                 case 5: {
                     pageUsageEvent.setValue1(stripWhitespace(token));
                     break;
                 }
                 case 6: {
                     StringBuffer sb2 = new StringBuffer(token);
                     // remaining contents may contain commas, and thus be incorrectly treated as tokens
                     // so, merge remaining contents into a single value
                     while (str.hasMoreTokens()) {
                         sb2.append(",").append(str.nextToken());
                     }
                     token = sb2.toString();
 
                     pageUsageEvent.setValue2(stripWhitespace(token));
                     break;
                 }
                 default: {
                     Logger.getLogger(LoggerName).log(Level.SEVERE,"Should never get here, but got '" + token + "'");
                     break;
                 }
             }
 
             tokenCount++;
         }
         if (pageUsageEvent.getValue1() == null) {
             pageUsageEvent.setValue1("");
         }
         if (pageUsageEvent.getValue2() == null) {
             pageUsageEvent.setValue2("");
         }
         if (pageUsageEvent.getGuiActionType() == null) {
             pageUsageEvent.setGuiActionType("");
         }
         if (pageUsageEvent.getEventType() == null) {
             pageUsageEvent.setEventType("");
         }
 
         pageUsageEvent.setPageUsageId(pageUsage);
         pageUsageEvent.setPageUsageEventSequence(++pageUsageEventCounter);
 
         return pageUsageEvent;
     }
 
     public void setLastAction(String lastAction) {
         if (!initialized) {
             return;
         }
         instrumentSession.setActionTypeId(parseActionType(lastAction));
     }
 
     public void setStatusMsg(String statusMsg) {
         if (!initialized) {
             return;
         }
         instrumentSession.setStatusMsg(statusMsg);
         if ("finished".equals(statusMsg)) {
             setFinished(true);
         }
     }
 
     private boolean isFinished() {
         if (finished == true ||
                 instrumentSession.getMaxGroupVisited() == instrumentSession.getNumGroups() ||
                 instrumentSession.getMaxVarNumVisited() == instrumentSession.getNumVars()) {
             return true;
         } else {
             return false;
         }
     }
 
     private void setFinished(boolean finished) {
         this.finished = finished;   // overrides MaxGroup & VarNum calculations -  for explicitly setting finished status
     }
 
     public void setToVarNum(int varNum) {
         if (!initialized) {
             return;
         }
         instrumentSession.setCurrentVarNum(varNum);
     }
 
     private void setTimeBeginServerProcessing(long time) {
         timeBeginServerProcessing = time;
     }
 
     private long getTimeBeginServerProcessing() {
         return timeBeginServerProcessing;
     }
 
     private void setPriorTimePageSentToUser(long time) {
         priorTimePageSentToUser = time;
     }
 
     private long getPriorTimePageSentToUser() {
         return priorTimePageSentToUser;
     }
 
     private void setTimePageSentToUser(long time) {
         timePageSentToUser = time;
     }
 
     private long getTimePageSentToUser() {
         return timePageSentToUser;
     }
 
     private void setNetworkDuration(int time) {
         networkDuration = time;
     }
 
     private int getNetworkDuration() {
         return networkDuration;
     }
 
     private int getTotalDuration() {
         return totalDuration;
     }
 
     private void setTotalDuration(int totalDuration) {
         this.totalDuration = totalDuration;
     }
 
     private int getLoadDuration() {
         return loadDuration;
     }
 
     private void setLoadDuration(int loadDuration) {
         this.loadDuration = loadDuration;
     }
 
     private void setServerDuration(int serverDuration) {
         this.serverDuration = serverDuration;
     }
 
     private int getServerDuration() {
         return serverDuration;
     }
 
     private int getPageDuration() {
         return pageDuration;
     }
 
     private void setPageDuration(int pageDuration) {
         this.pageDuration = pageDuration;
     }
 
     public int getStorageDuration() {
         return storageDuration;
     }
 
     public void setStorageDuration(int storageDuration) {
         this.storageDuration = storageDuration;
     }
 
 
 
     public void setLangCode(String langCode) {
         if (!initialized) {
             return;
         }
         if (langCode == null) {
             langCode = "en";
         } else if (langCode.length() > 2) {
             langCode = langCode.substring(0, 2);
         }
         instrumentSession.setLanguageCode(langCode);
     }
 
     public String getLangCode() {
         if (!initialized) {
             return "en";
         } else {
             return instrumentSession.getLanguageCode();
         }
     }
 
     private void lookupDialogixEntitiesFacade() {
         if (dialogixEntitiesFacade != null) {
             return; // since already loaded
         }
 //        dialogixEntitiesFacade = new DialogixEntitiesFacade();
 //        init();
         if (dialogixEntitiesFacade == null) {
             try {
                 Context c = new InitialContext();
                 dialogixEntitiesFacade = (DialogixEntitiesFacadeLocal) c.lookup("java:comp/env/DialogixEntitiesFacade_ejbref");
                 init();
             } catch (Exception e) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
             }
         }
     }
 
     private void init() {
         if (initialized) {
             return;
         }
         Iterator<ActionType> actionTypeIterator = dialogixEntitiesFacade.getActionTypes().iterator();
         actionTypeHash = new HashMap<String,ActionType>();
         while (actionTypeIterator.hasNext()) {
             ActionType actionType = actionTypeIterator.next();
             actionTypeHash.put(actionType.getActionName(), actionType);
         }
 
         Iterator<NullFlavor> nullFlavorIterator = dialogixEntitiesFacade.getNullFlavors().iterator();
         nullFlavorHash = new HashMap<String,NullFlavor>();
         nullFlavorIntegerHash = new HashMap<Integer,NullFlavor>();
         while (nullFlavorIterator.hasNext()) {
             NullFlavor nullFlavor = nullFlavorIterator.next();
             nullFlavorHash.put(nullFlavor.getNullFlavor(), nullFlavor);
             nullFlavorIntegerHash.put(nullFlavor.getNullFlavorId(), nullFlavor);
         }
 
         Iterator<NullFlavorChange> nullFlavorChangeIterator = dialogixEntitiesFacade.getNullFlavorChanges().iterator();
         nullFlavorChangeHash = new HashMap<String,NullFlavorChange>();
         while (nullFlavorChangeIterator.hasNext()) {
             NullFlavorChange nullFlavorChange = nullFlavorChangeIterator.next();
             nullFlavorChangeHash.put(nullFlavorChange.getNullFlavorChangeCode(), nullFlavorChange);
         }
 
         englishStrings = new HashMap<String,String>();
 
         try {
             Iterator<I18n> i18nIterator = dialogixEntitiesFacade.getI18nByIso3language("eng").iterator();
             while (i18nIterator.hasNext()) {
                 I18n i18n = i18nIterator.next();
                 englishStrings.put(i18n.getLabel(), i18n.getVal());
             }
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"no content for Locale", e);
         }
     }
 
     private ActionType parseActionType(String token) {
         if (actionTypeHash.containsKey(token)) {
             return actionTypeHash.get(token);
         }
         else {
             return null;
         }
     }
 
     private NullFlavor parseNullFlavor(String token) {
         if (nullFlavorHash.containsKey(token)) {
             return nullFlavorHash.get(token);
         }
         else {
             return null;
         }
     }
 
     private NullFlavor parseNullFlavor(Integer token) {
         if (nullFlavorIntegerHash.containsKey(token)) {
             return nullFlavorIntegerHash.get(token);
         }
         else {
             return null;
         }
     }
 
     private String stripWhitespace(String s) {
         StringBuffer sb = new StringBuffer();
         if (s == null) {
             return "";
         }
         char[] chars = s.toCharArray();
         char c;
 
         for (int i=0;i<chars.length;++i) {
                 c = chars[i];
                 if (Character.isWhitespace(c)) {
                         sb.append(' ');
                 }
                 else {
                         sb.append(c);
                 }
         }
         return sb.toString();
     }
 
     public boolean isInitialized() {
         return initialized;
     }
 
     public String getInstrumentAsSpreadsheet() {
         if (initialized) {
             return instrumentVersion.getInstrumentAsSpreadsheet();
         }
         else {
             return null;
         }
     }
 
     private boolean skipReservedWrite(String reservedName) {
         if (excludedReserveds.contains(reservedName)) {
             return true;
         }
         return false;
     }
 
 
     public String getCurrentVarNum() {
         if (initialized) {
             return "" + instrumentSession.getCurrentVarNum();
         }
         else {
             return "0";
         }
     }
 
     public HashMap<String,String> getCurrentValues() {
         if (!initialized) {
             return null;
         }
         HashMap<String,String> currentValues = new HashMap<String,String>();
 
         Iterator<DataElement> dataElements = instrumentSession.getDataElementCollection().iterator();
         while (dataElements.hasNext()) {
             DataElement dataElement = dataElements.next();
             ItemUsage itemUsage = dataElement.getLastItemUsageId();
             if (itemUsage != null) {
                 currentValues.put(dataElement.getVarNameId().getVarName(), itemUsage.getAnswerCode());
             }
             else {
                 currentValues.put(dataElement.getVarNameId().getVarName(), "*UNASKED*");
             }
         }
 
         /* Now add the RESERVED values */
         Iterator<String> reservedNames = InstrumentSession.getReservedWordMap().keySet().iterator();
         while (reservedNames.hasNext()) {
             String key = reservedNames.next();
             String value = instrumentSession.getReserved(key);
             currentValues.put(key, value);
 //            Logger.getLogger(LoggerName).log(Level.SEVERE,"**" + key + " = " + value);
         }
 
         return currentValues;
     }
 
 
     public void setPerson(String person) {
         if (person == null || person.trim().length() == 0) {
             return;
         }
         if (initialized) {
             Long id = (long) 1;
             try {
                 id = Long.parseLong(person);
             } catch (Exception e) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"setPerson", e.getMessage());
             }
             instrumentSession.setPersonId(dialogixEntitiesFacade.findPersonById(id));
         }
     }
 
     public void setStudy(String study) {
         if (study == null || study.trim().length() == 0) {
             return;
         }
         if (initialized) {
             Long id = (long) 1;
             try {
                 id = Long.parseLong(study);
             } catch (Exception e) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE,"setStudy", e.getMessage());
             }
             instrumentSession.setStudyId(dialogixEntitiesFacade.findStudyById(id));
         }
     }
 
     public void setSubjectSession(String ss) {
         if (ss == null || ss.trim().length() == 0) {
             return;
         }
         lookupDialogixEntitiesFacade();
         Long id = (long) 1;
         try {
             id = Long.parseLong(ss);
             this.subjectSession = dialogixEntitiesFacade.findSubjectSessionById(id);
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE,"setSubjectSession", e.getMessage());
         }
     }
 
     public String getPersonString() {
         return personString;
     }
 
     public void setPersonString(String personString) {
         this.personString = personString;
     }
 
     public String getStudyString() {
         return studyString;
     }
 
     public void setStudyString(String studyString) {
         this.studyString = studyString;
     }
 
     public String getSubjectSessionString() {
         return subjectSessionString;
     }
 
     public void setSubjectSessionString(String subjectSessionString) {
         this.subjectSessionString = subjectSessionString;
     }
 
     public String getGoogleAnalyticsPageView() {
         /*
          *  /Title/Version/MaxGroups/CurrentGroup
          */
         return "\"/Dialogix/" + instrumentVersion.getInstrumentId().getInstrumentName().replaceAll("\\W", "_") +
                 "/" + instrumentVersion.getVersionString().replaceAll("\\W", "_") +
                 "/maxGrp_" + instrumentSession.getNumGroups() +
                 "/curGrp_" + instrumentSession.getCurrentGroup() + ".html\"";
     }
 
     public Long getInstrumentSessionId() {
         if (this.initialized) {
             return instrumentSession.getInstrumentSessionId();
         }
         else {
             return 0L;
         }
     }
 
     public Long getInstrumentVersionId() {
         if (this.initialized) {
             return instrumentSession.getInstrumentVersionId().getInstrumentVersionId();
         }
         else {
             return 0L;
         }
     }
 }
