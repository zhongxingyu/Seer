 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.python.core.PyDictionary;
 import org.python.core.PyException;
 import org.python.core.PyFunction;
 import org.python.core.PyInteger;
 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.core.PyList;
 import org.python.core.PySyntaxError;
import org.python.core.PyType;
 import org.python.util.PythonInterpreter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.scheduling.Trigger;
 import org.springframework.scheduling.TriggerContext;
 import org.zenoss.amqp.AmqpConnectionManager;
 import org.zenoss.amqp.AmqpException;
 import org.zenoss.amqp.ExchangeConfiguration;
 import org.zenoss.amqp.ZenossQueueConfig;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventTrigger;
 import org.zenoss.protobufs.zep.Zep.EventTriggerSubscription;
 import org.zenoss.protobufs.zep.Zep.Signal;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.dao.EventSignalSpool;
 import org.zenoss.zep.dao.EventSignalSpoolDao;
 import org.zenoss.zep.dao.EventStoreDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.EventTriggerDao;
 import org.zenoss.zep.dao.EventTriggerSubscriptionDao;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Post processing plug-in used to determine if NEW  or CLEARED events match a specified trigger. If the
  * trigger doesn't match, no signal is sent. If the trigger matches and doesn't specify a delay or repeat
  * interval, then it is sent immediately. If the trigger matches and specifies a delay, then a signal is
  * sent only if the event is still in NEW state after the delay. If the trigger specifies a repeat
  * interval, then after the initial signal is sent, the event is checked again after the repeat interval. If
  * the event is still in NEW state after the repeat interval, then another signal is sent and the check is
  * repeated again at the repeat interval.
  *
  * If an event which previously triggered a signal is cleared by another event, a final signal is sent
  * with the clear attribute set to true.
  */
 public class TriggerPlugin extends AbstractPostProcessingPlugin {
 
     private static final Logger logger = LoggerFactory.getLogger(TriggerPlugin.class);
 
     private EventTriggerDao triggerDao;
     private EventSignalSpoolDao signalSpoolDao;
     private EventStoreDao eventStoreDao;
     private EventSummaryDao eventSummaryDao;
     private EventTriggerSubscriptionDao eventTriggerSubscriptionDao;
 
     private AmqpConnectionManager connectionManager;
 
     protected PythonInterpreter python;
     protected PyFunction toObject;
     protected static final Map<EventSeverity, PyString> severityAsPyString = new EnumMap<EventSeverity, PyString>(
             EventSeverity.class);
     protected static final int MAX_RULE_CACHE_SIZE = 200;
     protected static final Map<String, PyFunction> ruleFunctionCache = Collections
             .synchronizedMap(ZepUtils
                     .<String, PyFunction> createBoundedMap(MAX_RULE_CACHE_SIZE));
 
     // The maximum amount of time to wait between processing the signal spool.
     private static final long MAXIMUM_DELAY_MS = TimeUnit.SECONDS.toMillis(60);
 
     private TaskScheduler scheduler;
     private ScheduledFuture<?> spoolFuture;
 
     static {
         PythonInterpreter.initialize(System.getProperties(), new Properties(), new String[0]);
 
         severityAsPyString.put(EventSeverity.SEVERITY_CLEAR, new PyString("clear"));
         severityAsPyString.put(EventSeverity.SEVERITY_DEBUG, new PyString("debug"));
         severityAsPyString.put(EventSeverity.SEVERITY_INFO, new PyString("info"));
         severityAsPyString.put(EventSeverity.SEVERITY_WARNING, new PyString("warning"));
         severityAsPyString.put(EventSeverity.SEVERITY_ERROR, new PyString("error"));
         severityAsPyString.put(EventSeverity.SEVERITY_CRITICAL, new PyString("critical"));
     }
 
     public TriggerPlugin() {
     }
 
     @Autowired
     public void setTaskScheduler(TaskScheduler scheduler) {
         this.scheduler = scheduler;
     }
 
     @Override
     public void init(Map<String, String> properties) {
         super.init(properties);
 
         this.python = new PythonInterpreter();
 
         // define basic infrastructure class for evaluating rules
         this.python.exec(
             "class DictAsObj(object):\n" +
             "  def __init__(self, **kwargs):\n" +
             "    for k,v in kwargs.items(): setattr(self,k,v)");
 
         // expose to Java a Python dict->DictAsObj conversion function
         this.toObject = (PyFunction)this.python.eval("lambda dd : DictAsObj(**dd)");
 
         // import some helpful modules from the standard lib
         this.python.exec("import string, re, datetime");
 
         scheduleSpool();
     }
 
     public void shutdown() throws InterruptedException {
         this.python.cleanup();
         if (spoolFuture != null) {
             spoolFuture.cancel(true);
         }
     }
 
     private void scheduleSpool() {
         if (spoolFuture != null) {
             spoolFuture.cancel(false);
         }
         Trigger trigger = new Trigger() {
             @Override
             public Date nextExecutionTime(TriggerContext triggerContext) {
                 Date nextExecution = null;
                 try {
                     long nextFlushTime = signalSpoolDao.getNextFlushTime();
                     if (nextFlushTime > 0) {
                         logger.debug("Next flush time: {}", nextFlushTime);
                         nextExecution = new Date(nextFlushTime);
                     }
                 } catch (Exception e) {
                     logger.warn("Exception getting next flush time", e);
                 }
                 if (nextExecution == null) {
                     nextExecution = new Date(System.currentTimeMillis() + MAXIMUM_DELAY_MS);
                 }
                 return nextExecution;
             }
         };
         Runnable runnable = new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 processSpool(System.currentTimeMillis());
             }
         }, "ZEP_TRIGGER_PLUGIN_SPOOL");
         spoolFuture = scheduler.schedule(runnable, trigger);
     }
 
     public void setTriggerDao(EventTriggerDao triggerDao) {
         this.triggerDao = triggerDao;
     }
 
     public void setSignalSpoolDao(EventSignalSpoolDao spoolDao) {
         this.signalSpoolDao = spoolDao;
     }
 
     public void setEventSummaryDao(EventSummaryDao eventSummaryDao) {
         this.eventSummaryDao = eventSummaryDao;
     }
 
     public void setEventTriggerSubscriptionDao(EventTriggerSubscriptionDao eventTriggerSubscriptionDao) {
         this.eventTriggerSubscriptionDao = eventTriggerSubscriptionDao;
     }
 
     public void setConnectionManager(AmqpConnectionManager connmgr) {
         this.connectionManager = connmgr;
     }
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     private void putIdAndUuidInDict(PyDictionary dict, String id, String uuid){
         if (id != null) {
             dict.put("name", id);
         }
         if (uuid != null) {
             dict.put("uuid", uuid);
         }
     }
 
     protected boolean eventSatisfiesRule(EventSummary evtsummary,
                                          String ruleSource) {
         // set up interpreter environment to evaluate the rule source
         PyDictionary eventdict = new PyDictionary();
         PyDictionary devdict = new PyDictionary();
         PyDictionary elemdict = new PyDictionary();
         PyDictionary subelemdict = new PyDictionary();
         // extract event data from most recent occurrence
         if (evtsummary.getOccurrenceCount() > 0) {
             Event event = evtsummary.getOccurrence(0);
 
             // copy event data to eventdict
             if (event.hasSummary()) {
                 eventdict.put("summary", new PyString(event.getSummary()));
             }
             if (event.hasMessage()) {
                 eventdict.put("message", new PyString(event.getMessage()));
             }
             if (event.hasEventClass()) {
                 eventdict.put("event_class", new PyString(event.getEventClass()));
             }
             if (event.hasFingerprint()) {
                 eventdict.put("fingerprint", new PyString(event.getFingerprint()));
             }
             if (event.hasEventKey()) {
                 eventdict.put("event_key", new PyString(event.getEventKey()));
             }
             if (event.hasAgent()) {
                 eventdict.put("agent", new PyString(event.getAgent()));
             }
             if (event.hasMonitor()) {
                 eventdict.put("monitor", new PyString(event.getMonitor()));
             }
             eventdict.put("severity", event.getSeverity().getNumber());
             eventdict.put("status", evtsummary.getStatus().getNumber());
 
             if (event.hasEventClassKey()) {
                 eventdict.put("event_class_key", new PyString(event.getEventClassKey()));
             }
             if (event.hasSyslogPriority()) {
                 eventdict.put("syslog_priority", event.getSyslogPriority());
             }
             if (event.hasSyslogFacility()) {
                 eventdict.put("syslog_facility", event.getSyslogFacility());
             }
             if (event.hasNtEventCode()) {
                 eventdict.put("nt_event_code", event.getNtEventCode());
             }
 
             if (event.hasActor()) {
                 EventActor actor = event.getActor();
 
                 if (actor.hasElementTypeId()) {
                     if (actor.getElementTypeId() == ModelElementType.DEVICE) {
                         devdict = elemdict;
                     }
 
                     elemdict.put("type", actor.getElementTypeId().name());
                     final String id = (actor.hasElementIdentifier()) ? actor.getElementIdentifier() : null;
                     final String uuid = actor.hasElementUuid() ? actor.getElementUuid() : null;
 
                     putIdAndUuidInDict(elemdict, id, uuid);
                 }
 
                 if (actor.hasElementSubTypeId()) {
                     if (actor.getElementSubTypeId() == ModelElementType.DEVICE) {
                         devdict = subelemdict;
                     }
 
                     subelemdict.put("type", actor.getElementSubTypeId().name());
                     final String id = (actor.hasElementSubIdentifier()) ? actor.getElementSubIdentifier() : null;
                     final String uuid = actor.hasElementSubUuid() ? actor.getElementSubUuid() : null;
 
                     putIdAndUuidInDict(subelemdict, id, uuid);
                 }
             }
 
             for (EventDetail detail : event.getDetailsList()) {
                 final String detailName = detail.getName();
                 if (ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE.equals(detailName)) {
                     try {
                         int prodState = Integer.parseInt(detail.getValue(0));
                         devdict.put("production_state", new PyInteger(prodState));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving production state", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_PRIORITY.equals(detailName)) {
                     try {
                         int priority = Integer.parseInt(detail.getValue(0));
                         devdict.put("priority", new PyInteger(priority));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device priority", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_CLASS.equals(detailName)) {
                     try {
                         // expect that this is a single-value detail.
                         String device_class = String.valueOf(detail.getValue(0));
                         devdict.put("device_class", new PyString(device_class));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device class", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_SYSTEMS.equals(detailName)) {
                     try {
                         // expect that this is a multi-value detail.
                         List systems = detail.getValueList();
                         devdict.put("systems", new PyList(systems));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device systems", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_GROUPS.equals(detailName)) {
                     try {
                         // expect that this is a multi-value detail.
                         List groups = detail.getValueList();
                         devdict.put("groups", new PyList(groups));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device groups", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_IP_ADDRESS.equals(detailName)) {
                     try {
                         // expect that this is a single-value detail.
                         String ip_address = String.valueOf(detail.getValue(0));
                         devdict.put("ip_address", new PyString(ip_address));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device ip address", e);
                     }
                 }
                 else if (ZepConstants.DETAIL_DEVICE_LOCATION.equals(detailName)) {
                     try {
                         // expect that this is a single-value detail.
                         String location = String.valueOf(detail.getValue(0));
                         devdict.put("location", new PyString(location));
                     } catch (Exception e) {
                         logger.warn("Failed retrieving device location", e);
                     }
                 }
             }
         }
 
         if (evtsummary.hasCurrentUserName()) {
             eventdict.put("current_user_name", new PyString(evtsummary.getCurrentUserName()));
         }
 
         // add more data from the EventSummary itself
         eventdict.put("count", new PyInteger(evtsummary.getCount()));
 
         PyObject result;
         try {
             // use rule to build and evaluate a Python lambda expression
             PyFunction fn = ruleFunctionCache.get(ruleSource);
             if (fn == null) {
                 fn = (PyFunction)this.python.eval(
                     "lambda evt, dev, elem, sub_elem : " + ruleSource
                     );
                 ruleFunctionCache.put(ruleSource, fn);
             }
 
             // create vars to pass to rule expression function
             PyObject evtobj = this.toObject.__call__(eventdict);
             PyObject devobj = this.toObject.__call__(devdict);
             PyObject elemobj = this.toObject.__call__(elemdict);
             PyObject subelemobj = this.toObject.__call__(subelemdict);
             // evaluate the rule function
             result = fn.__call__(evtobj, devobj, elemobj, subelemobj);
         } catch (PySyntaxError pysynerr) {
             // evaluating rule raised an exception - treat as "False" eval
             logger.warn("syntax error exception raised while compiling rule: " + ruleSource, pysynerr);
             result = new PyInteger(0);
         } catch (PyException pyexc) {
             // evaluating rule raised an exception - treat as "False" eval
            // If it's an AttributeError it just means the event doesn't have a value for the field
            // and an eval of False is fine. Otherwise we should log in case there's a real issue.
            if (!((PyType) pyexc.type).getName().equals("AttributeError")) {
                logger.warn("exception raised while evaluating rule: {} \n{}", ruleSource, pyexc);
            }
             result = new PyInteger(0);
         }
 
         // return result as a boolean, using Python __nonzero__
         // object-as-bool evaluator
         return result.__nonzero__();
     }
 
     @Override
     public void processEvent(EventSummary eventSummary) throws ZepException {
         final EventStatus evtstatus = eventSummary.getStatus();
 
         if (ZepConstants.OPEN_STATUSES.contains(evtstatus)) {
             processOpenEvent(eventSummary);
         }
         else {
             List<EventSignalSpool> spools = this.signalSpoolDao.findAllByEventSummaryUuid(eventSummary.getUuid());
             for (EventSignalSpool spool : spools) {
                 if (evtstatus == EventStatus.STATUS_CLEARED) {
                     // Send clear signal
                     final EventTriggerSubscription subscription =
                             this.eventTriggerSubscriptionDao.findByUuid(spool.getSubscriptionUuid());
                     logger.debug("sending clear signal for event: {}", eventSummary);
                     publishSignal(eventSummary, subscription);
 
                 }
                 this.signalSpoolDao.delete(spool.getUuid());
             }
         }
     }
 
     private void processOpenEvent(EventSummary eventSummary) throws ZepException {
         final long now = System.currentTimeMillis();
         List<EventTrigger> triggers = this.triggerDao.findAllEnabled();
 
         // iterate over all enabled triggers to see if any rules will match
         // for this event summary
         boolean rescheduleSpool = false;
         for (EventTrigger trigger : triggers) {
 
             // verify trigger has a defined rule
             if (!(trigger.hasRule() && trigger.getRule().hasSource())) {
                 continue;
             }
             // confirm trigger has any subscriptions registered with it
             List<EventTriggerSubscription> subscriptions = trigger.getSubscriptionsList();
             if (subscriptions.isEmpty()) {
                 logger.debug("no subscriptions for this trigger, go on to the next");
                 continue;
             }
 
             final String ruleSource = trigger.getRule().getSource();
 
             // Determine if event matches trigger rule
             final boolean eventSatisfiesRule = eventSatisfiesRule(eventSummary, ruleSource);
 
             if (eventSatisfiesRule) {
                 logger.debug("Trigger: {} MATCHES event {}", ruleSource, eventSummary);
             }
             else {
                 logger.debug("Trigger: {} DOES NOT match event: {}", ruleSource, eventSummary);
             }
 
             // handle interval evaluation/buffering
             for (EventTriggerSubscription subscription : subscriptions) {
                 final int delaySeconds = subscription.getDelaySeconds();
                 final int repeatSeconds = subscription.getRepeatSeconds();
 
                 // see if any signalling spool already exists for this trigger-eventSummary
                 // combination
                 EventSignalSpool currentSpool =
                         this.signalSpoolDao.findBySubscriptionAndEventSummaryUuids(
                                 subscription.getUuid(), eventSummary.getUuid()
                         );
                 boolean spoolExists = (currentSpool != null);
 
                 logger.debug("delay|repeat|existing spool: {}|{}|{}", 
                              new Object[] { delaySeconds, repeatSeconds, spoolExists });
 
                 boolean onlySendInitial = subscription.getSendInitialOccurrence();
 
                 // If the rule wasn't satisfied
                 if (!eventSatisfiesRule) {
                     // If the rule previously matched and now no longer matches, ensure that the
                     // repeated signaling will not occur again.
                     if (spoolExists && currentSpool.getFlushTime() < Long.MAX_VALUE) {
                         logger.debug("Event previously matched trigger - disabling repeats: event={}, trigger={}",
                             eventSummary, trigger);
                         this.signalSpoolDao.updateFlushTime(currentSpool.getUuid(), Long.MAX_VALUE);
                         rescheduleSpool = true;
                     }
                 }
                 // Send signal immediately if no delay
                 else if (delaySeconds <= 0) {
                     if (!onlySendInitial) {
                         logger.debug("delay <= 0 and not only send initial, send signal for event {}", eventSummary);
                         this.publishSignal(eventSummary, subscription);
                     }
                     else {
                         if (!spoolExists) {
                             currentSpool = EventSignalSpool.buildSpool(subscription, eventSummary);
                             this.signalSpoolDao.create(currentSpool);
                             rescheduleSpool = true;
                             logger.debug("delay <=0 and not spool exists, send signal for event {}", eventSummary);
                             this.publishSignal(eventSummary, subscription);
                         }
                         else {
                             if (repeatSeconds > 0 &&
                                 currentSpool.getFlushTime() > now + TimeUnit.SECONDS.toMillis(repeatSeconds)) {
                                 logger.debug("adjust spool flush time to reflect new repeat seconds");
                                 this.signalSpoolDao.updateFlushTime(currentSpool.getUuid(),
                                         now + TimeUnit.SECONDS.toMillis(repeatSeconds));
                                 rescheduleSpool = true;
                             }
                         }
                     }
                 }
                 else {
                     // delaySeconds > 0
                     if (!spoolExists) {
                         currentSpool = EventSignalSpool.buildSpool(subscription, eventSummary);
                         this.signalSpoolDao.create(currentSpool);
                         rescheduleSpool = true;
                     }
                     else {
                         if (repeatSeconds == 0) {
                             if (!onlySendInitial && currentSpool.getFlushTime() == Long.MAX_VALUE) {
                                 this.signalSpoolDao.updateFlushTime(currentSpool.getUuid(),
                                         now + TimeUnit.SECONDS.toMillis(delaySeconds));
                                 rescheduleSpool = true;
                             }
                         }
                         else {
                             if (currentSpool.getFlushTime() > now + TimeUnit.SECONDS.toMillis(repeatSeconds)) {
                                 this.signalSpoolDao.updateFlushTime(currentSpool.getUuid(),
                                         now + TimeUnit.SECONDS.toMillis(repeatSeconds));
                                 rescheduleSpool = true;
                             }
                         }
                     }
                 }
             }
         }
         if (rescheduleSpool) {
             scheduleSpool();
         }
     }
 
     protected void publishSignal(EventSummary summary, EventTriggerSubscription subscription) throws ZepException {
         try {
             final Event occurrence = summary.getOccurrence(0);
             Signal.Builder signalBuilder = Signal.newBuilder();
             signalBuilder.setUuid(UUID.randomUUID().toString());
             signalBuilder.setCreatedTime(System.currentTimeMillis());
             signalBuilder.setEvent(summary);
             signalBuilder.setSubscriberUuid(subscription.getSubscriberUuid());
             signalBuilder.setTriggerUuid(subscription.getTriggerUuid());
 
             final boolean cleared = (summary.getStatus() == EventStatus.STATUS_CLEARED);
             signalBuilder.setClear(cleared);
             if (cleared) {
                 // Look up event which cleared this one
                 EventSummary clearEventSummary = this.eventStoreDao.findByUuid(summary.getClearedByEventUuid());
                 if (clearEventSummary != null) {
                     signalBuilder.setClearEvent(clearEventSummary);
                 }
                 else {
                     logger.warn("Unable to look up clear event with UUID: {}", summary.getClearedByEventUuid());
                 }
             }
             signalBuilder.setMessage(occurrence.getMessage());
             ExchangeConfiguration destExchange = ZenossQueueConfig.getConfig()
                     .getExchange("$Signals");
             this.connectionManager.publish(destExchange, "zenoss.signal",
                     signalBuilder.build());
         } catch (IOException ioe) {
             throw new ZepException(ioe);
         } catch (AmqpException e) {
             throw new ZepException(e);
         }
     }
 
     protected synchronized void processSpool(long processCutoffTime) {
         logger.debug("Processing signal spool");
         try {
             // get spools that need to be processed
             List<EventSignalSpool> spools = this.signalSpoolDao.findAllDue();
             for (EventSignalSpool spool : spools) {
                 EventSummary eventSummary = this.eventSummaryDao.findByUuid(spool.getEventSummaryUuid());
                 EventStatus status = (eventSummary != null) ? eventSummary.getStatus() : null;
 
                 if (ZepConstants.OPEN_STATUSES.contains(status)) {
 
                     EventTriggerSubscription trSub = this.eventTriggerSubscriptionDao
                             .findByUuid(spool.getSubscriptionUuid());
 
                     // Check to see if trigger is still enabled
                     boolean triggerEnabled = false;
                     if (trSub != null) {
                         EventTrigger trigger = this.triggerDao.findByUuid(trSub.getTriggerUuid());
                         if (trigger != null) {
                             triggerEnabled = trigger.getEnabled();
                         }
                     }
 
                     if (triggerEnabled) {
                         logger.debug("sending spooled signal for event {}", eventSummary);
                         publishSignal(eventSummary, trSub);
                     }
 
                     int repeatInterval = trSub.getRepeatSeconds();
                     if (repeatInterval > 0 && repeatInterval != Long.MAX_VALUE) {
                         long nextFlush = processCutoffTime + TimeUnit.SECONDS.toMillis(repeatInterval);
                         this.signalSpoolDao.updateFlushTime(spool.getUuid(), nextFlush);
                     }
                     else {
                         boolean onlySendInitial = trSub.getSendInitialOccurrence();
                         if (!onlySendInitial) {
                             this.signalSpoolDao.delete(spool.getUuid());
                         }
                         else {
                             this.signalSpoolDao.updateFlushTime(spool.getUuid(), Long.MAX_VALUE);
                         }
                     }
                 }
             }
 
         } catch (Exception e) {
             logger.warn("Failed to process signal spool", e);
         }
     }
 }
 
