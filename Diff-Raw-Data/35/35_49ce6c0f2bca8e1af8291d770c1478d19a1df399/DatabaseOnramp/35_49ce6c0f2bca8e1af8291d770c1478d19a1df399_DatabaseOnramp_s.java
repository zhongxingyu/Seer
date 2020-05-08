 /**
  * 
  */
 package com.alertscape.pump.onramp.db;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.alertscape.common.logging.ASLogger;
 import com.alertscape.common.model.Alert;
 import com.alertscape.common.model.severity.Severity;
 import com.alertscape.common.model.severity.SeverityFactory;
 import com.alertscape.pump.onramp.AbstractPollingAlertOnramp;
 
 /**
  * @author josh
  * 
  */
 public class DatabaseOnramp<ID> extends AbstractPollingAlertOnramp {
   private static final String LAST_ID = "LAST_ID";
   private static final ASLogger LOG = ASLogger.getLogger(DatabaseOnramp.class);
 
   private AlertOnrampDao<ID> onrampDao;
   private int batchSize = 1000;
   private ID lastId;
 
   /**
    * @return the onrampDao
    */
   public AlertOnrampDao<ID> getOnrampDao() {
     return onrampDao;
   }
 
   /**
    * @param onrampDao
    *          the onrampDao to set
    */
   public void setOnrampDao(AlertOnrampDao<ID> onrampDao) {
     this.onrampDao = onrampDao;
   }
 
   @Override
   protected int readUntilEnd() throws Exception {
     int linesProcessed = 0;
     long startTime = System.currentTimeMillis();
     List<Alert> nextAlerts = new ArrayList<Alert>(batchSize);
 
     while (!isStopped()) {
       nextAlerts.clear();
       lastId = onrampDao.getNextAlerts(batchSize, lastId, nextAlerts);
       if (nextAlerts.isEmpty()) {
         if (linesProcessed > 0) {
           long endTime = System.currentTimeMillis();
           long elapsed = endTime - startTime;
           if (elapsed < 1) {
             elapsed = 1;
           }
           long perSecond = (linesProcessed * 1000 / elapsed);
           LOG.info(getSourceName() + " processed " + linesProcessed + " lines in " + (endTime - startTime) + "ms at "
               + perSecond + "/s");
         }
         saveState();
         return linesProcessed;
       }
       Severity defaultSev = SeverityFactory.getInstance().getSeverity(0);
       for (Alert alert : nextAlerts) {
        if(alert.getSeverity() == null || alert.getSeverity() == defaultSev) {
           Severity severity = determineSeverity(alert);
           alert.setSeverity(severity);
         }
         sendAlert(alert);
         linesProcessed++;
         if (linesProcessed % 1000 == 0) {
           long endTime = System.currentTimeMillis();
           long elapsed = endTime - startTime;
           long perSecond = (linesProcessed * 1000 / elapsed);
           LOG.info(getSourceName() + " processed " + linesProcessed + " lines in " + (endTime - startTime) + "ms at "
               + perSecond + "/s");
           linesProcessed = 0;
           startTime = System.currentTimeMillis();
           state.put(LAST_ID, lastId);
           saveState();
         }
       }
     }
     return linesProcessed;
   }
 
   /**
    * @return the batchSize
    */
   public int getBatchSize() {
     return batchSize;
   }
 
   /**
    * @param batchSize
    *          the batchSize to set
    */
   public void setBatchSize(int batchSize) {
     this.batchSize = batchSize;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   protected void initState(Map<String, Object> state) {
    lastId = (ID) state.get(LAST_ID);
   }
 
 }
