 package com.fs.solr.scheduler;
 
 import com.fs.solr.exception.ReIndexException;
 import com.fs.solr.index.ReIndexHelper;
 import org.apache.log4j.Logger;
 
 import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
 import javax.ejb.Timer;
 import java.io.Serializable;
 
@Stateless
 public class SolrIndexScheduler implements Serializable {
 
     public static final Logger log = Logger.getLogger(SolrIndexScheduler.class);
 
     @Schedule(hour = "*", minute = "*/15", persistent = false)
     public void runIndexer() {
         ReIndexHelper reIndexHelper = new ReIndexHelper();
 
         try {
             reIndexHelper.performReIndexOnEmployees();
         } catch (ReIndexException re) {
             log.error("ReIndexException thrown while ReIndexing",re);
         } catch (Exception e) {
             log.error("Unexpected Exception thrown while ReIndexing",e);
         }
     }
 
    @Timeout
     public void timeout(Timer timer) {
         log.info("Timeout occurred");
     }
 }
