 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2011, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 1st February 2011
  */
 package au.edu.uts.eng.remotelabs.schedserver.rigoperations;
 
 import org.hibernate.Session;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigLogDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener.RigStateChangeEvent;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.RigClientAsyncService;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.RigClientAsyncServiceCallbackHandler;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.intf.types.ErrorType;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.intf.types.OperationResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.intf.types.SetMaintenanceResponse;
 
 /**
  * Sets of clears rig maintnenace states.
  * <br />
  * If making the request fails or the response either fails or indicates failure,
  * the rig is taken offline. The rig shouldn't be in session if this is to be called.
  */
 public class RigMaintenance extends RigClientAsyncServiceCallbackHandler
 {
     /** Request rig. */
     private Rig rig;
     
     /** Logger. */
     private Logger logger;
     
     public RigMaintenance()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     /**
     * Sets mainteance state on rig. 
      * 
      * @param rig rig to set
      * @param runTests whether exerciser tests should be run
      * @param db database session
      */
     public void putMaintenance(Rig rig, boolean runTests, Session db)
     {
         this.logger.info("Setting maintenance state on rig " + rig.getName() + ". Tests will " + 
                 (runTests ? "" : "not ") + "be run.");
         
         this.makeMaintenaceRequest(rig, true, runTests, db);
     }
     
     /**
      * Clears maintenance state on rig.
      * 
      * @param rig rig to clear
      * @param db database session
      */
     public void clearMaintenance(Rig rig, Session db)
     {
         this.logger.info("Clearning maintenance state from rig " + rig.getName() + '.');
         this.makeMaintenaceRequest(rig, false, true, db);
     }
     
     /**
     * Actually makes the maintenace request.
      * 
      * @param rig rig to request
      * @param offline whether the rig s being put offline
     * @param tests whehter exerciser tests should be run
      * @param db database session
      */
     private void makeMaintenaceRequest(Rig rig, boolean offline, boolean tests, Session db)
     {
         try
         {
             RigClientAsyncService service = new RigClientAsyncService(rig.getName(), db);
             service.setMaintenance(offline, tests, this);
         }
         catch (Exception e)
         {
             this.logger.error("Failed calling rig client set maintenance to " + rig.getName() + " at " + 
                     rig.getContactUrl() + " because of error " + e.getMessage() + ".");
 
             /* Set rig inactive. */
             rig.setInSession(false);
             rig.setOnline(false);
             rig.setOfflineReason("Set maintenance operation failed.");
             rig.setSession(null);
             db.beginTransaction();
             db.flush();
             db.getTransaction().commit();
             
             /* Log when the rig is offline. */
             RigLogDao rigLogDao = new RigLogDao(db);
             rigLogDao.addOfflineLog(rig, "Set maintenance with failed. Supplied params: offline=" + offline + 
                     ", tests=" + tests + ".");
             
             /* Fire event the rig is offline. */
             RigEventListener evts[] = RigOperationsActivator.getRigEventListeners();
             for (RigEventListener evt : evts)
             {
                 evt.eventOccurred(RigStateChangeEvent.OFFLINE, rig, db);
             }
         }
     }
     
     @Override
     public void setMaintenanceResponseCallback(SetMaintenanceResponse response)
     {
         OperationResponseType op = response.getSetMaintenanceResponse();
         
         if (!op.getSuccess())
         {
             ErrorType err = op.getError();
             this.logger.error("Set maintenance for rig " + this.rig.getName() + " failed. Error reason is '" + 
                     err.getReason() + "'.");
             
             Session db = DataAccessActivator.getNewSession();
             this.rig.setInSession(false);
             this.rig.setOnline(false);
             this.rig.setOfflineReason("Set maintenance failed with reason: '" + err.getReason() + "'.");
             this.rig.setSession(null);
             db.beginTransaction();
             db.flush();
             db.getTransaction().commit();
 
             /* Log when the rig is offline. */
             RigLogDao rigLogDao = new RigLogDao(db);
             rigLogDao.addOfflineLog(this.rig, "Set maintenance failed with reason: " + err.getReason());
 
             /* Fire event the rig is offline. */
             RigEventListener evts[] = RigOperationsActivator.getRigEventListeners();
             for (RigEventListener evt : evts)
             {
                 evt.eventOccurred(RigStateChangeEvent.OFFLINE, this.rig, db);
             }
         }
     }
     
     @Override
     public void setMaintenanceErrorCallback(final Exception e)
     {
         this.logger.error("Set maintenance for rig " + this.rig.getName() + " failed with exception: '" + 
                 e.getClass().getSimpleName() + ", message: " + e.getMessage() + "'.");
         
         Session db = DataAccessActivator.getNewSession();
         this.rig.setInSession(false);
         this.rig.setOnline(false);
         this.rig.setOfflineReason("Set maintenance failed with exception: '" + e.getClass().getSimpleName() + 
                 ", message: " + e.getMessage() + "'.");
         this.rig.setSession(null);
         
         db.beginTransaction();
         db.flush();
         db.getTransaction().commit();
 
         /* Log when the rig is offline. */
         RigLogDao rigLogDao = new RigLogDao(db);
         rigLogDao.addOfflineLog(this.rig, "Set maintenance failed with exception: '" + e.getClass().getSimpleName() + 
                 ", message: " + e.getMessage() + "'.");
 
         /* Fire event the rig is offline. */
         RigEventListener evts[] = RigOperationsActivator.getRigEventListeners();
         for (RigEventListener evt : evts)
         {
             evt.eventOccurred(RigStateChangeEvent.OFFLINE, this.rig, db);
         }
     }
 }
