 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
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
  * @date 6th April 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.session.impl;
 
 import java.util.Date;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigLogDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.queuer.QueueRun;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.RigClientAsyncService;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.RigClientAsyncServiceCallbackHandler;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.RigProxyActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.intf.types.OperationResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.rigproxy.intf.types.ReleaseResponse;
 
 /**
  * Calls the rig client release operation to remove a user from the rig client.
  */
 public class RigReleaser extends RigClientAsyncServiceCallbackHandler
 {
     /** Rig to release. */
     private Rig rig;
     
     /** Logger. */
     private Logger logger;
     
     public RigReleaser()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     public void release(Session ses, org.hibernate.Session db)
     {
         this.rig = ses.getRig();
         
         try
         {
             int setupTime = this.rig.getRigType().getTearDownTime();
             boolean async = setupTime > 0 && setupTime > RigProxyActivator.getAsyncTimeout() - 10;
             
             RigClientAsyncService service = new RigClientAsyncService(this.rig.getName(), db);
             service.release(ses.getUserName(), async, this);
         }
         catch (Exception e)
         {
             this.logger.error("Failed calling rig client release from " + this.rig.getName() + " at " + 
                     this.rig.getContactUrl() + " because of error " + e.getMessage() + ".");
 
             /* Put the rig offline. */
             this.rig.setInSession(false);
             this.rig.setOnline(false);
             this.rig.setOfflineReason("Release failed with error: " + e.getMessage());
             this.rig.setSession(null);
             db.beginTransaction();
             db.flush();
             db.getTransaction().commit();
             
             /* Log when the rig is offline. */
             RigLogDao rigLogDao = new RigLogDao(db);
             rigLogDao.addOfflineLog(this.rig, "Release failed with error: " + e.getMessage());
         }
     }
     
     @Override
     public void releaseResponseCallback(final ReleaseResponse response)
     {
        RigDao dao = new RigDao();
        this.rig = dao.merge(this.rig);
            
         OperationResponseType op = response.getReleaseResponse();
         
         if (op.getWillCallback())
         {
             /* The response will come in a callback request so no work required now. */
             this.logger.debug("Received notification release of rig " + this.rig.getName() + 
                     " will come in a callback message.");
             return;
         }
         
         if (op.getSuccess())
         {
             this.logger.debug("Received release callback, releasing " + this.rig.getName() + " was successful.");
             
             this.rig.setLastUpdateTimestamp(new Date());
             this.rig.setInSession(false);
             this.rig.setSession(null);
             dao.flush();
             
             /* Give the rig to the queue to attempt allocation. */
             QueueRun.attemptAssignment(this.rig, dao.getSession());
         }
         else
         {
             /* Failed release so take rig offline. */
             this.rig.setOnline(false);
             this.rig.setOfflineReason("Release failed with reason: " + op.getError().getReason());
             this.rig.setInSession(false);
             this.rig.setSession(null);
             dao.flush();
             
             /* Log when the rig is offline. */
             RigLogDao rigLogDao = new RigLogDao(dao.getSession());
             rigLogDao.addOfflineLog(this.rig, "Release failed with reason: " + op.getError().getReason());
         }
         
         dao.closeSession();
     }
     
     @Override
     public void releaseErrorCallback(final Exception e)
     {
         RigDao dao = new RigDao();
         this.rig = dao.merge(this.rig);
         
         this.logger.error("Received error response from release of rig " + this.rig.getName() + " at " 
                 + this.rig.getContactUrl() + ". Error message" + " is " + e.getMessage() + '.');
         
         /* Release failed so take the rig offline. */
         this.rig.setOnline(false);
         this.rig.setOfflineReason("Release failed with reason " + e.getMessage() + '.');
         this.rig.setSession(null);
         this.rig.setInSession(false);
         dao.flush();
         
         /* Log when the rig is offline. */
         RigLogDao rigLogDao = new RigLogDao(dao.getSession());
         rigLogDao.addOfflineLog(this.rig, "Release failed with error: " + e.getMessage());
         
         dao.closeSession();
     }
 }
