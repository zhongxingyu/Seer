 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.esper.service;
 
 import ca.uhn.hl7v2.model.Message;
 import com.abada.esper.EsperLoader;
 import com.abada.esper.configuration.model.Statement;
 import com.abada.esper.configuration.model.Statements;
 import com.abada.esper.historic.service.HistoricActionService;
 import com.abada.esper.listener.EsperListener;
 import com.abada.esper.lock.service.LockService;
 import com.abada.eva.historic.dao.HistoricDao;
 import com.abada.eva.historic.entities.HistoricEvent;
 import com.abada.eva.historic.entities.HistoricGenericEventContainer;
 import com.espertech.esper.client.EPAdministrator;
 import com.espertech.esper.client.EPRuntime;
 import com.espertech.esper.client.EPRuntimeIsolated;
 import com.espertech.esper.client.EPServiceProviderIsolated;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.time.CurrentTimeEvent;
 import com.thoughtworks.xstream.XStream;
 import java.io.ByteArrayInputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import javax.annotation.Resource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.scheduling.annotation.Async;
 
 /**
  * This is the core of the application.
  *
  * @author mmartin
  */
 public class EsperService {
 
     private static final Log logger = LogFactory.getLog(EsperService.class);
     private static final String RECOVER_NAME = "recover";
     /**
      * loader for esper
      */
     private EsperLoader loader;
     /**
      * esper core
      */
     private EPRuntime runtime;
     /**
      * Session lock service
      */
     private LockService lockService;
     /**
      * Num max of items per recover task
      */
     private int numMax;
     /**
      * Queue for recover task
      */
     private ExecutorService es;
     /**
      * Dao to recover the historic events
      */
     private HistoricDao historicDao;
     /**
      * Service to register executed events
      */
     private HistoricActionService historicActionService;
     /**
      * Status of recovering mode
      */
     private boolean recovering;
     /**
      *
      */
     private boolean enableRecovery;
 
     public EsperService(URL url, EsperLoader loader, LockService lockService, HistoricActionService historicActionService, HistoricDao historicDao, int nThreads, int numMaxItems,boolean enableRecovery) throws Exception {
         Statements statements = this.getConfiguration(url);
         this.historicDao = historicDao;
         this.historicActionService = historicActionService;
         this.es = Executors.newFixedThreadPool(nThreads);
         this.loader = loader;
         this.lockService = lockService;
         this.runtime = this.loader.getEPRuntime();
         this.numMax = numMaxItems;
         this.loadStatements(statements);
         this.enableRecovery=enableRecovery;
         if (enableRecovery) {
             if (!lockService.isLastLocked()) {
                 lockService.addNewLock();
             } else {
                 recover();
             }
         }
     }
 
     public void recover() throws Exception {
         synchronized (this) {
             recovering = true;
         }
         this.recoverInt();
     }
 
     /**
      * Return true if core accept messages
      *
      * @return
      */
     public boolean canSend() {
         synchronized (this) {
             if (recovering) {
                 return false;
             }
         }
        return lockService.isLocked();
     }
 
     /**
      * Send Message HL7 to Esper core
      *
      * @param message
      */
     public void send(Message message) {
         if (logger.isDebugEnabled()) {
             logger.debug("Sending " + message);
         }
         runtime.sendEvent(message);
         if (logger.isDebugEnabled()) {
             logger.debug("Sended " + message);
         }
     }
 
     /**
      * Send Generic object to Esper core
      *
      * @param obj
      */
     public void send(Object obj) {
         if (logger.isDebugEnabled()) {
             logger.debug("Sending " + obj);
         }
         runtime.sendEvent(obj);
         if (logger.isDebugEnabled()) {
             logger.debug("Sended " + obj);
         }
     }
 
     private Statements getConfiguration(URL url) {
         XStream x = new XStream();
         x.processAnnotations(Statements.class);
         Statements s = (Statements) x.fromXML(url);
 
         return s;
     }
 
     private void loadStatements(Statements statements) throws Exception {
         List<Statement> ls = statements.getStatements();
         EPStatement stmt;
         if (ls != null) {
             EPAdministrator adm = loader.getEPAdministrator();
 
             for (Statement s : ls) {
                 stmt = adm.createEPL(s.getEPL());
 
                 EsperListener el = new EsperListener(this.historicActionService, new ByteArrayInputStream(s.getSpringContext().getBytes()), s);
                 stmt.addListener(el);
 
                 stmt.start();
             }
         }
     }
 
     public void finalice() {
         lockService.releaseLastLock();
         loader.destroy();
     }
 
     @Async
     private void recoverInt() throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("Recovering historic data");
         }
         //Add statemests to isolated service
         EPServiceProviderIsolated isolatedService = loader.getEPServiceIsolated(RECOVER_NAME);
 
         for (String sn : loader.getEPAdministrator().getStatementNames()) {
             isolatedService.getEPAdministrator().addStatement(loader.getEPAdministrator().getStatement(sn));
         }
 
         //Adding task
         Long total = historicDao.getHistoricEventCount();
         if (total != null && total > 0) {
 
 
             for (long i = 0L; i < total; i += numMax) {
                 RecoverTask r = new RecoverTask();
                 r.setInitItem(i);
                 r.setMaxNumItem(numMax);
                 r.setHistoricDao(historicDao);
                 r.setLoader(loader);
 
                 es.submit(r);
             }
 
             es.shutdown();
             es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
         }
 
         //Adding task
         total = historicDao.getHistoricGenericEventCount();
         if (total != null && total > 0) {
 
 
             for (long i = 0L; i < total; i += numMax) {
                 RecoverGenericEventTask r = new RecoverGenericEventTask();
                 r.setInitItem(i);
                 r.setMaxNumItem(numMax);
                 r.setHistoricDao(historicDao);
                 r.setLoader(loader);
 
                 es.submit(r);
             }
 
             es.shutdown();
             es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
         }
 
         //Remove from isolated service            
         for (String sn : loader.getEPAdministrator().getStatementNames()) {
             isolatedService.getEPAdministrator().removeStatement(loader.getEPAdministrator().getStatement(sn));
         }
 
 
         synchronized (this) {
             recovering = false;
         }
         this.lockService.releaseLastLock();
         this.lockService.addNewLock();
         if (logger.isDebugEnabled()) {
             logger.debug("Recovered historic data");
         }
     }
 
     private class RecoverGenericEventTask implements Callable {
 
         private long initItem;
         private long maxNumItem;
         private HistoricDao historicDao;
         private EsperLoader loader;
 
         public void setLoader(EsperLoader loader) {
             this.loader = loader;
         }
 
         public void setInitItem(long initItem) {
             this.initItem = initItem;
         }
 
         public void setMaxNumItem(long maxNumItem) {
             this.maxNumItem = maxNumItem;
         }
 
         public void setHistoricDao(HistoricDao historicDao) {
             this.historicDao = historicDao;
         }
 
         public Object call() throws Exception {
             if (logger.isDebugEnabled()) {
                 logger.debug("Subrecovering (" + initItem + " " + (initItem + maxNumItem) + "]");
             }
 
             EPServiceProviderIsolated isolatedService = loader.getEPServiceIsolated(RECOVER_NAME);
             List<HistoricGenericEventContainer> he = historicDao.getHistoricGenericEvent(initItem, maxNumItem);
             if (logger.isDebugEnabled()) {
                 logger.debug("recovered " + he.size());
             }
 
             if (he != null) {
                 EPRuntimeIsolated isolatedRuntime = isolatedService.getEPRuntime();
                 for (HistoricGenericEventContainer h : he) {
                     isolatedRuntime.sendEvent(new CurrentTimeEvent(h.getEvent().getRun()));
                     logger.debug(h.getDeserializedObject());
                     isolatedRuntime.sendEvent(h.getDeserializedObject());
                 }
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Subrecovered (" + initItem + " " + (initItem + maxNumItem) + "]");
             }
             return null;
         }
     }
 
     private class RecoverTask implements Callable {
 
         private long initItem;
         private long maxNumItem;
         private HistoricDao historicDao;
         private EsperLoader loader;
 
         public void setLoader(EsperLoader loader) {
             this.loader = loader;
         }
 
         public void setInitItem(long initItem) {
             this.initItem = initItem;
         }
 
         public void setMaxNumItem(long maxNumItem) {
             this.maxNumItem = maxNumItem;
         }
 
         public void setHistoricDao(HistoricDao historicDao) {
             this.historicDao = historicDao;
         }
 
         public Object call() throws Exception {
             if (logger.isDebugEnabled()) {
                 logger.debug("Subrecovering (" + initItem + " " + (initItem + maxNumItem) + "]");
             }
 
             EPServiceProviderIsolated isolatedService = loader.getEPServiceIsolated(RECOVER_NAME);
             List<HistoricEvent> he = historicDao.getHistoricEvents(initItem, maxNumItem);
             if (logger.isDebugEnabled()) {
                 logger.debug("recovered " + he.size());
             }
 
             if (he != null) {
                 EPRuntimeIsolated isolatedRuntime = isolatedService.getEPRuntime();
                 for (HistoricEvent h : he) {
                     isolatedRuntime.sendEvent(new CurrentTimeEvent(h.getRun()));
                     logger.debug(h.getTrace());
                     isolatedRuntime.sendEvent(h.getTrace());
                 }
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Subrecovered (" + initItem + " " + (initItem + maxNumItem) + "]");
             }
             return null;
         }
     }
 }
