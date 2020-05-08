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
  * @date 1st April 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.queuer.impl;
 
 import static au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission.CAPS_PERMISSION;
 import static au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission.RIG_PERMISSION;
 import static au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission.TYPE_PERMISSION;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RequestCapabilitiesDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigTypeDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 /**
  * The queue.
  */
 public class Queue
 {
     /** Singleton instance. */
     private static Queue queue = new Queue();
     
     /** The rig queues. */
     private Map<Long, InnerQueue> rigQueues;
     
     /** The rig type queues. */
     private Map<Long, InnerQueue> typeQueues;
     
     /** The request capabilities queues. */
     private Map<Long, InnerQueue> capabilityQueues;
     
     /** Comparator to compare which session should be assigned first. */
     private QueueSessionComparator comparator;
     
     /** Logger. */
     private Logger logger;
     
     /** Flag to specify if this is a test run. */
     private boolean notTest = true;
     
     private Queue()
     {
         this.rigQueues = new HashMap<Long, InnerQueue>();
         this.typeQueues = new HashMap<Long, InnerQueue>();
         this.capabilityQueues = new HashMap<Long, InnerQueue>();
         
         this.comparator = new QueueSessionComparator();
         
         this.logger = LoggerActivator.getLogger();
     }
     
     /**
      * Returns true if the rig, the rig's type or a matching request capabilites is queued.
      * That is, if a session is queued that a rig may be assigned to, <code>true</code>
      * is returned.
      * 
      * @param id identifier of rig
      * @return true if the rig is queued for
      */
     public synchronized boolean isRigQueued(Long id, org.hibernate.Session db)
     {
         Rig rig = new RigDao(db).get(id);
         
         if (this.rigQueues.containsKey(rig.getId()) && this.rigQueues.get(rig.getId()).size() > 0)
         {
             /* The rig itself is queued. */
             return true;
         }
 
         if (this.typeQueues.containsKey(rig.getRigType().getId()) && 
                 this.typeQueues.get(rig.getRigType().getId()).size() > 0)
         {
             /* The rig's type is queued. */
             return true;
         }
 
         for (MatchingCapabilities match : rig.getRigCapabilities().getMatchingCapabilitieses())
         {
             RequestCapabilities caps = match.getRequestCapabilities();
             if (this.capabilityQueues.containsKey(caps.getId()) && 
                     this.capabilityQueues.get(caps.getId()).size() > 0)
             {
                 /* A matching request capability is queued. */
                 return true;
             }
         }
         
         return false;
     }
     
     /**
      * Attempts to assigned the specified rig to a queued session. If there is
      * queued session for the rig, a queued session for its rig type or a
      * queued session for a request capabilities matching its rig capabilites,
      * the highest precedence request given by the {@link QueueSessionComparator} 
      * is assigned.
      * 
      * @param id identifier of rig
      */
     public synchronized void runRigAssignment(Long id, org.hibernate.Session db)
     {
         Rig rig = new RigDao(db).get(id);
         if (rig == null || !rig.isOnline() || rig.isInSession()) return;
         
         Session targetSes = null;
         String queueType = null;
         Long innerQueueId = null;
         
         /**********************************************************************
          ** 1) Check if there is a queued session for this specific rig.     **
          **********************************************************************/
         if (this.rigQueues.containsKey(rig.getId()) && this.rigQueues.get(rig.getId()).size() > 0)
         {
             /* The rig itself is queued, so the rig's queued session is the
              * first target to assign to the rig. */
             innerQueueId = rig.getId();
             targetSes = this.rigQueues.get(innerQueueId).peekHead();
             queueType = RIG_PERMISSION;
         }
 
         /**********************************************************************
          ** 2) Check if there is a queued session for the rig's rig type.    **
          **********************************************************************/
         if (this.typeQueues.containsKey(rig.getRigType().getId()) && 
                 this.typeQueues.get(rig.getRigType().getId()).size() > 0)
         {
             Session typeSes = this.typeQueues.get(rig.getRigType().getId()).peekHead();
             
             if (targetSes == null || this.comparator.compare(targetSes, typeSes) > 0)
             {
                 innerQueueId = rig.getRigType().getId();
                 targetSes = typeSes;
                 queueType = TYPE_PERMISSION;
             }
         }
         
         /**********************************************************************
          ** 3) Check each of the rig's rig capabilities matching request     **
          **    capabilities.                                                 **
          **********************************************************************/
         for (MatchingCapabilities match : rig.getRigCapabilities().getMatchingCapabilitieses())
         {
             RequestCapabilities caps = match.getRequestCapabilities();
             if (this.capabilityQueues.containsKey(caps.getId()) && 
                     this.capabilityQueues.get(caps.getId()).size() > 0)
             {
                 Session capsSes = this.capabilityQueues.get(caps.getId()).peekHead();
                 
                 if (targetSes == null || this.comparator.compare(targetSes, capsSes) > 0)
                 {
                     innerQueueId = caps.getId();
                     targetSes = capsSes;
                     queueType = CAPS_PERMISSION;
                 }
             }
         }
         
         /**********************************************************************
          ** 4) If a target is found, assign it.                              **
          **********************************************************************/
         if (targetSes != null)
         {
             /* Remove them from the inner queue and if the inner queue is then
              * empty, remove the inner queue itself. */
             if (RIG_PERMISSION.equals(queueType))
             {
                 targetSes = this.rigQueues.get(innerQueueId).getHead();
                 if (this.rigQueues.get(innerQueueId).size() == 0)
                 {
                     this.rigQueues.remove(innerQueueId);
                 }
             }
             else if (TYPE_PERMISSION.equals(queueType))
             {
                 targetSes = this.typeQueues.get(innerQueueId).getHead();
                 if (this.typeQueues.get(innerQueueId).size() == 0)
                 {
                     this.typeQueues.remove(innerQueueId);
                 }
             }
             else if (CAPS_PERMISSION.equals(queueType))
             {
                 targetSes = this.capabilityQueues.get(innerQueueId).getHead();
                 if (this.capabilityQueues.get(innerQueueId).size() == 0)
                 {
                     this.capabilityQueues.remove(innerQueueId);
                 }
             }
             
             /* Assign the rig. */
             targetSes = (Session)db.merge(targetSes);
             
             targetSes.setAssignmentTime(new Date());
             targetSes.setAssignedRigName(rig.getName());
             targetSes.setRig(rig);
             rig.setInSession(true);
             db.beginTransaction();
             db.flush();
             db.getTransaction().commit();
             
             this.logger.info("Assigned " + targetSes.getUserNamespace() + ':' + targetSes.getUserName() + " to rig " +
                     rig.getName() + " (session=" + targetSes.getId() + ").");
             
             /******************************************************************
              ** 5) Allocate the user to the rig.                             **
              ******************************************************************/
             if (this.notTest)
             {
                 new Allocator().allocate(targetSes, db);
             }
         }
     }
     
     /**
      * Runs rig type assigning by loading free rigs in the type and calling
      * rig assignment.
      *
      * @param id rig type id
      * @param db database session
      */
     public synchronized void runTypeAssignment(Long id, org.hibernate.Session db)
     {
         RigType type = new RigTypeDao(db).get(id);
         if (type == null) return;
         
         /* Create a query to find a free rig in the rig type. */
         Criteria query = db.createCriteria(Rig.class);
         query.add(Restrictions.eq("rigType", type))
              .add(Restrictions.eq("active", true))
              .add(Restrictions.eq("online", true))
             .add(Restrictions.eq("inSession", false));
         
         Rig freeRig;
         while (this.typeQueues.containsKey(id) && this.typeQueues.get(id).size() > 0 &&
                 (freeRig = (Rig) query.uniqueResult()) != null)
         {
             this.runRigAssignment(freeRig.getId(), db);
         }
     }
     
     /**
      * Runs request capability assignment by searching for a free matching rig
      * and calling rig assignment.
      * 
      * @param id request capabilities assignmetn
      * @param db database session
      */
     public synchronized void runRequestCapabilitiesAssignment(Long id, org.hibernate.Session db)
     {
         RequestCapabilities caps = new RequestCapabilitiesDao(db).get(id);
         for (MatchingCapabilities match : caps.getMatchingCapabilitieses())
         {
             RigCapabilities rigCaps = match.getRigCapabilities();
             for (Rig rig : rigCaps.getRigs())
             {
                 if (rig.isOnline() && !rig.isInSession())
                 {
                     /* When a free rig is found, run rig assignment, which should assign
                      * the rig. */
                     this.runRigAssignment(rig.getId(), db);
                     
                     /* If the capability has been assigned, no more work to do. */
                     if (!this.capabilityQueues.containsKey(id) || this.capabilityQueues.get(id).size() == 0)
                     {
                         return;
                     }
                 }
             }
         }
     }
     
     /**
      * Adds a session to the queue and attempts to assign it to a rig.
      * 
      * @param ses session to add
      * @param db database session the session is attached to
      * @return return session 
      */
     public synchronized Session addEntry(Session ses, org.hibernate.Session db)
     {   
         String resourceType = ses.getResourceType();
         Long rID = ses.getRequestedResourceId();
         String requestedName = ses.getRequestedResourceName();
         
         if (RIG_PERMISSION.equals(resourceType))
         {
             this.logger.debug("Adding a session (id=" + ses.getId() + ") to the queue for a rig with id=" + 
                      rID + " and name " + requestedName + ".");
             
             if (!this.rigQueues.containsKey(rID))
             {
                 this.rigQueues.put(rID, new InnerQueue(resourceType, rID, requestedName));                
             }
             this.rigQueues.get(rID).add(ses);
             this.runRigAssignment(rID, db);
         }
         else if (TYPE_PERMISSION.equals(ses.getResourceType()))
         {
             this.logger.debug("Adding a session (id=" + ses.getId() + ") to the queue for a rig type with id=" + 
                      rID + " and name " + requestedName + ".");
             
             if (!this.typeQueues.containsKey(rID))
             {
                 this.typeQueues.put(rID, new InnerQueue(resourceType, rID, requestedName));
             }
             this.typeQueues.get(rID).add(ses);
             this.runTypeAssignment(rID, db);
         }
         else if (CAPS_PERMISSION.equals(ses.getResourceType()))
         {
             this.logger.debug("Adding a session (id=" + ses.getId() + ") to the queue for a request capabibilites " + 
                      " with id " + rID + " and capabilities " + requestedName + ".");
 
             if (!this.capabilityQueues.containsKey(rID))
             {
                 this.capabilityQueues.put(rID, new InnerQueue(resourceType, rID, requestedName));
             }
             this.capabilityQueues.get(rID).add(ses);
             this.runRequestCapabilitiesAssignment(rID, db);
         }
         
         return ses;
     }
     
     /**
      * Removes the session from the queue.
      * 
      * @param ses session to remove
      * @param db database session
      */
     public synchronized void removeEntry(Session ses, org.hibernate.Session db)
     {
         long rID = ses.getRequestedResourceId();
         if (ses.getRig() == null)
         {
             /******************************************************************
              ** Session is in queue, so remove it from its requested resource *
              ** inner queue.                                                  *
              ******************************************************************/
             if (RIG_PERMISSION.equals(ses.getResourceType()) && 
                     (this.rigQueues.containsKey(rID) && this.rigQueues.get(rID).contains(ses)))
             {
                 this.rigQueues.get(rID).remove(ses);
                 if (this.rigQueues.get(rID).size() == 0)
                 {
                     this.rigQueues.remove(rID);
                 }   
             }
             else if (TYPE_PERMISSION.equals(ses.getResourceType()) && 
                     (this.typeQueues.containsKey(rID) && this.typeQueues.get(rID).contains(ses)))
             {
                 this.typeQueues.get(rID).remove(ses);
                 if (this.typeQueues.get(rID).size() == 0)
                 {
                     this.typeQueues.remove(rID);
                 } 
             }
             else if (CAPS_PERMISSION.equals(ses.getResourceType()) &&
                     (this.capabilityQueues.containsKey(rID) && this.capabilityQueues.get(rID).contains(ses)))
             {
                 this.capabilityQueues.get(rID).remove(ses);
                 if (this.capabilityQueues.get(rID).size() == 0)
                 {
                     this.capabilityQueues.remove(rID);
                 } 
             }
         }
         else
         {
             // TODO rig session logoff.
         }
     }
     
     /**
      * Gets the position in the queue for the specified session.
      * 
      * @param ses session to find position of
      * @return queue position
      */
     public synchronized int getEntryPosition(Session ses, org.hibernate.Session db)
     {
         String resourceType = ses.getResourceType();
         Long rID = ses.getRequestedResourceId();
         int pos = 0;
         
         if (RIG_PERMISSION.equals(resourceType))
         {
             /* Position in the rig queue. */
             if (this.rigQueues.containsKey(rID))
             {
                 pos += this.rigQueues.get(rID).position(ses);
             }
             
             /* Position in the type queue for the rig. */
             Rig rig = new RigDao(db).get(rID);
             if (this.typeQueues.containsKey(rig.getRigType().getId()))
             {
                 pos += this.typeQueues.get(rig.getRigType().getId()).numberBefore(ses);
             }
             
             /* Position in the request capabilities queues. */
             for (MatchingCapabilities match : rig.getRigCapabilities().getMatchingCapabilitieses())
             {
                 RequestCapabilities caps = match.getRequestCapabilities();
                 if (this.capabilityQueues.containsKey(caps.getId()))
                 {
                     pos += this.capabilityQueues.get(caps.getId()).numberBefore(ses);
                 }
             }
         }
         else if (TYPE_PERMISSION.equals(resourceType))
         {
             /* Position in the type queue. */
             if (this.typeQueues.containsKey(rID))
             {
                 pos += this.typeQueues.get(rID).position(ses);
             }
             
             /* Position in the rig queues for the types rigs. */
             RigType rigType = new RigTypeDao(db).get(rID);
             List<Long> checkedCaps = new ArrayList<Long>();
             
             for (Rig rig : rigType.getRigs())
             {
                 if (this.rigQueues.containsKey(rig.getId()))
                 {
                     pos += this.rigQueues.get(rig.getId()).numberBefore(ses);
                 }
                 
                 /* Check unique rig capabilities. */
                 Long capsID = rig.getRigCapabilities().getId();
                 if (!checkedCaps.contains(capsID))
                 {
                    checkedCaps.add(capsID);
                    /* Position in the request capabilities queues. */
                    for (MatchingCapabilities match : rig.getRigCapabilities().getMatchingCapabilitieses())
                    {
                        RequestCapabilities caps = match.getRequestCapabilities();
                        if (this.capabilityQueues.containsKey(caps.getId()))
                        {
                            pos += this.capabilityQueues.get(caps.getId()).numberBefore(ses);
                        }
                    }
                 }
             }
         }
         else if (CAPS_PERMISSION.equals(resourceType))
         {
             List<Long> checkedCaps = new ArrayList<Long>();
             List<Long> checkedTypes = new ArrayList<Long>();
             
             /* The capability queue. */
             if (this.capabilityQueues.containsKey(rID))
             {
                 checkedCaps.add(rID);
                 pos += this.capabilityQueues.get(rID).position(ses);
             }
             
             RequestCapabilities reqCaps = new RequestCapabilitiesDao(db).get(rID);
             for (MatchingCapabilities match : reqCaps.getMatchingCapabilitieses())
             {
                 RigCapabilities rigCaps = match.getRigCapabilities();
                 for (Rig r : rigCaps.getRigs())
                 {
                     /* The rig's queue. */
                     if (this.rigQueues.containsKey(r.getId()))
                     {
                         pos += this.rigQueues.get(r.getId()).numberBefore(ses);
                     }
                     
                     /* The rig type's queue */
                     Long typeID = r.getRigType().getId();
                     if (!checkedTypes.contains(typeID))
                     {
                         checkedTypes.contains(typeID);
                         if (this.typeQueues.containsKey(typeID))
                         {
                             pos += this.typeQueues.get(typeID).numberBefore(ses);
                         }
                     }
                     
                     /* The rig's rig capabilities queues. */
                     for (MatchingCapabilities m : r.getRigCapabilities().getMatchingCapabilitieses())
                     {
                         Long capsID = m.getRequestCapabilities().getId();
                         if (!checkedCaps.contains(capsID))
                         {
                             checkedCaps.add(capsID);
                             if (this.capabilityQueues.containsKey(capsID))
                             {
                                 pos += this.capabilityQueues.get(capsID).numberBefore(ses);
                             }
                         }
                     }
                 }
             }
         }
         
         return pos;
     }
     
     /**
      * Clears the queue of all sessions.
      */
     public void expunge()
     {
         this.rigQueues.clear();
         this.typeQueues.clear();
         this.capabilityQueues.clear();
     }
     
     /**
      * Returns the singleton instance of this queue.
      * 
      * @return queue instance.
      */
     public static Queue getInstance()
     {
         return Queue.queue;
     }
 }
