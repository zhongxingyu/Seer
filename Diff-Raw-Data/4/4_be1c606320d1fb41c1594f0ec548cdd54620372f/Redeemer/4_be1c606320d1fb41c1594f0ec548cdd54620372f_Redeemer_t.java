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
  * @date 11th November 2011
  */
 package au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import au.edu.uts.eng.remotelabs.schedserver.bookings.BookingActivator;
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.BookingManagementTask;
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.BookingNotification;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Bookings;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigoperations.RigAllocator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener;
 
 /**
  * Tasks that converts bookings to sessions (i.e. redeems the booking).
  */
 public class Redeemer implements BookingManagementTask, RigEventListener
 {
     /** The number of seconds between redeem runs. */
     public static final int REDEEM_INTERVAL = 30;
     
     /** The number of slots before a booking starts to send a reminder
      *  notification out. */
     public static final int NOTIF_SLOTS = 4;
     
     /** Slots booking engine. */
     private SlotBookingEngine engine;
     
     /** The current day bookings. */
     private DayBookings currentDay;
     
     /** The current time slot. */
     private int currentSlot;
     
     /** The time at which the slot was rolled. */
     private long rollTime;
     
     /** List of bookings that are currently being redeemed. */
     private Map<String, MBooking> redeemingBookings;
     
     /** List of bookings that are currently in session. */
     private Map<String, MBooking> runningBookings;
     
     /** Logger. */
     private Logger logger;
     
     /** Flag to specify if this is a test run. */
     private boolean notTest = true;
     
     public Redeemer(SlotBookingEngine engine, DayBookings startDay)
     {
         this.logger = LoggerActivator.getLogger();
         
         this.engine = engine;
         
         this.redeemingBookings = new HashMap<String, MBooking>();
         this.runningBookings = Collections.synchronizedMap(new HashMap<String, MBooking>());
         
         this.currentDay = startDay;
     }
     
     @Override
     public void run()
     {
         org.hibernate.Session db = null;
         try
         {
             db = DataAccessActivator.getNewSession();
             
             synchronized (this)
             {
                 Calendar now = Calendar.getInstance();
                 String nowDay = TimeUtil.getDayKey(now);
                 int nowSlot = TimeUtil.getDaySlotIndex(now, nowDay);
 
                 /* The later check is to stop time going backwards, which can
                  * occur during daylight saving time changes. */
                 if (!this.currentDay.getDay().equals(nowDay) && System.currentTimeMillis() > this.rollTime)
                 {
                     /* The day has rolled to the next day. */
                     this.logger.debug("Rolling day from " + this.currentDay.getDay() + " to " + nowDay + ".");
 
                     /* Clean up the old day bookings. We don't need that resident 
                      * any more as it is only historical. */
                     this.engine.removeDay(this.currentDay.getDay());
 
                     /* Set the new day. */
                     synchronized (this.currentDay = this.engine.getDayBookings(nowDay))
                     {
                         this.currentDay.fullLoad(db);
                     };
                 }
 
                 /* Time must always go forwards... */
                 if (nowSlot != this.currentSlot && System.currentTimeMillis() > this.rollTime)
                 {
                     /* The slot is being rolled over. */
                     this.rollTime = now.getTimeInMillis();            
                     this.currentSlot = nowSlot;
 
 
                     /* Cancel the remaining previous slot bookings. */
                     for (MBooking mb : this.redeemingBookings.values())
                     {
                         Bookings b = (Bookings) db.merge(mb.getBooking());
                         b.setActive(false);
                         b.setCancelReason("No resources free to redeem booking too.");
                         this.logger.warn("Unable to redeem booking (" + b.getId() + ") for " + b.getUser().qName() + 
                                " because no free resources were found in the slot period.");
                         
                        this.currentDay.removeBooking(mb);
                         new BookingNotification(b).notifyCancel();
                     }
 
                     if (this.redeemingBookings.size() > 0)
                     {
                         this.logger.warn("Cancelling " + this.redeemingBookings.size() + " bookings that were on " +
                                 this.currentDay.getDay() + ", slot " + (this.currentSlot - 1) + '.');
                         db.beginTransaction();
                         db.flush();
                         db.getTransaction().commit();
                     }
 
                     /* Get the new list of bookings. */
                     synchronized (this.currentDay)
                     {
                         this.redeemingBookings = this.currentDay.getSlotStartingBookings(this.currentSlot);
                     }
 
                     if (this.redeemingBookings.size() == 0)
                     {
                         this.logger.debug("No bookings are starting on " + this.currentDay.getDay() + ", slot " + 
                                 this.currentSlot + '.');
                         this.startBookingNotification(db);
                         return;
                     }
 
                     /* Redeem the bookings for rigs that are free. */
                     RigDao rigDao = new RigDao(db);
                     Iterator<Entry<String, MBooking>> it = this.redeemingBookings.entrySet().iterator();
                     while (it.hasNext())
                     {
                         Entry<String, MBooking> e = it.next();
 
                         Rig rig = rigDao.findByName(e.getKey());
                         if (rig != null && rig.isActive() && rig.isOnline() && !rig.isInSession())
                         {
                             /* Rig is free so assign it. */
                             this.logger.info("Rig " + rig.getName() + " is free so is having booking redeemed to it.");
                             this.redeemBooking(e.getValue(), rig, db);
                             it.remove();
                         }
                         else if (rig == null)
                         {
                             /* Rig is not found, serious issue. */
                             this.logger.warn("Booking on " + this.currentDay.getDay() + ", slot " + this.currentSlot + 
                                     " for rig " + e.getKey() + " that doesn't exist.");
                         }
                         else if (!(rig.isActive() && rig.isOnline()))
                         {
                             this.logger.debug("Booking on " + this.currentDay.getDay() + ", slot " + this.currentSlot + 
                                     " for rig " + e.getKey() + " cannot be redeemed because the rig is currently " +
                                     "offline.");
                         }
                         else if (rig.isInSession())
                         {
                             this.logger.debug("Booking on " + this.currentDay.getDay() + ", slot " + this.currentSlot + 
                                     " for rig " + e.getKey() + " cannot be redeemed because the rig is currently " +
                                     "in session.");
                         }
                     }
                     
                     this.startBookingNotification(db);
                 }
                 
                 /* Try to load balance existing booking to a new rig. */
                 if (this.redeemingBookings.size() > 0 && System.currentTimeMillis() - this.rollTime > 60000)
                 {
                     Iterator<Entry<String, MBooking>> it = this.redeemingBookings.entrySet().iterator();
                     while (it.hasNext())
                     {
                         synchronized (this.currentDay)
                         {
                             Entry<String, MBooking> e = it.next();
                             Rig rig = this.currentDay.findViableRig(e.getKey(), e.getValue(), db);
                             if (rig  != null)
                             {
                                 this.redeemBooking(e.getValue(), rig, db);
                                 it.remove();
                             }
                         }
                     }
                 }
             }
         }
         catch (Throwable thr)
         {
             this.logger.error("Unchecked exception caught in redeemer task. Exception type: " + 
                     thr.getClass().getName() + ", message: " + thr.getMessage() + '.');
         }
         finally 
         {
             if (db != null) db.close();
         }
     }
 
     /**
      * Provides notification that a booking is going to start in the recent future.
      * 
      * @param db database connection
      */
     private void startBookingNotification(org.hibernate.Session db)
     {
         /* Notify the users of whose bookings are going to start soon. */
         int notifSlot = this.currentSlot + NOTIF_SLOTS;
         Collection<MBooking> starting;
         if (notifSlot < SlotBookingEngine.NUM_SLOTS)
         {
             synchronized (this.currentDay)
             {
                 starting = this.currentDay.getSlotStartingBookings(notifSlot).values();
             }
         }
         else
         {
             Calendar next = Calendar.getInstance();
             next.add(Calendar.DAY_OF_MONTH, 1);
             
             DayBookings nextDay = this.engine.getDayBookings(TimeUtil.getDayKey(next));
             synchronized (nextDay)
             {
                 nextDay.fullLoad(db);
                 starting = nextDay.getSlotStartingBookings(notifSlot - SlotBookingEngine.NUM_SLOTS).values();
             }
         }
         
         for (MBooking mb : starting)
         {
             new BookingNotification((Bookings)db.merge(mb.getBooking())).notifyStarting();
         }
     }
     
     @Override
     public void eventOccurred(RigStateChangeEvent event, Rig rig, org.hibernate.Session db)
     {
         /* Clean the previous session. */        
         if (this.runningBookings.containsKey(rig.getName()))
         {
             MBooking old = this.runningBookings.remove(rig.getName());
             if (old.isMultiDay() || !this.currentDay.getDay().equals(old.getDay()))
             {
                 DayBookings dayb;
                 MBooking oldNext;
                 for (String day : TimeUtil.getDayKeys(old.getStart().getTime(), old.getEnd().getTime()))
                 {
                     /* If a day isn't loaded we aren't going to load it. */
                     if ((dayb = this.engine.getDayBookings(day, false)) != null)
                     {
                         synchronized (dayb)
                         {
                             if (dayb.getDay().equals(old.getDay())) dayb.removeBooking(old);
                             else if ((oldNext = dayb.getBookingOnSlot(rig, 0)) != null && // Session must be continuous
                                       oldNext.getSession() != null &&                     // Must be assigned a session
                                       /* Must be the same session. */
                                       oldNext.getSession().getId().equals(old.getSession().getId()))
                             {
                                 /* If the next day starting booking is an extension
                                  * of the current booking, then remove it. */
                                 dayb.removeBooking(oldNext);
                             }
                         }
                     }
                 }
             }
             else
             {
                 /* The booking was only on today so it can be safely reaped. */
                 synchronized (this.currentDay) 
                 {
                     this.currentDay.removeBooking(old);
                 }
             }
             
             /* If the rig event was free, and the rig isn't booked (i.e. next
              * slot is free), we need to fire another free broadcast to trigger
              * another queue run. This is because if the initial notification 
              * fired before this in queuer, then the queue attempt would have 
              * falsely been blocked by the memory representation of the terminated 
              * session. Removing the finished booking then notifying again makes 
              * sure the queue attempt will run. */
             if (!this.redeemingBookings.containsKey(rig.getName()) && event == RigStateChangeEvent.FREE)
             {   
                 if (this.currentSlot + 1 < SlotBookingEngine.NUM_SLOTS)
                 {
                     /* Next slot on current day and no booking in next slot. */
                     if (this.currentDay.getBookingOnSlot(rig, this.currentSlot + 1) == null)
                     {
                         this.fireFreeEvent(rig, db);
                     }
                 }
                 else
                 {
                     /* Next booking is on next day. */
                     Calendar cal = Calendar.getInstance();
                     cal.add(Calendar.DAY_OF_MONTH, 1);
                     DayBookings nextBookings = this.engine.getDayBookings(TimeUtil.getDayKey(cal));
                     
                     synchronized (nextBookings)
                     {
                         nextBookings.fullLoad(db);
                     }
                     
                     if (nextBookings.getBookingOnSlot(rig, 0) == null)
                     {
                         this.fireFreeEvent(rig, db);
                     }
                 }
             }
         }
 
         switch (event)
         {
             case ONLINE:
                 /* Falls through. */
             case FREE:
                 /* Remove the finished session. */
                 synchronized (this)
                 {
                     if (this.redeemingBookings.containsKey(rig.getName()))
                     {
                         this.redeemBooking(this.redeemingBookings.remove(rig.getName()), rig, db);
                     }
                 }
                 break;
             default:
                 /* Don't care about the other states. */
         }
     }
 
     /**
      * Fires an online event to us.
      * 
      * @param rig rig event refers to
      * @param db database session
      */
     private void fireFreeEvent(Rig rig, org.hibernate.Session db)
     {
         /* Fire event the rig is online. */
         for (RigEventListener evt : BookingActivator.getRigEventListeners())
         {
             /* Check so we don't fire event to us. */
             if (evt == this) continue;
             evt.eventOccurred(RigStateChangeEvent.FREE, rig, db);
         }
     }
     
     /**
      * Redeems a booking by creating a session and allocating a rig to it.
      * 
      * @param membooking membooking to convert to session
      * @param rig rig to allocate
      * @param db database connection
      */
     private void redeemBooking(MBooking membooking, Rig rig, org.hibernate.Session db)
     {
         Date now = new Date();
         Bookings booking = (Bookings)db.merge(membooking.getBooking());
         this.logger.info("Redeeming a booking (" + booking.getId() + ") for " + booking.getUser().qName() + " using rig " + 
                 rig.getName() + " at " + now + ".");
 
         Session session = new Session();
         session.setActive(true);
         session.setInGrace(false);
         session.setActivityLastUpdated(now);
         session.setReady(false);
         session.setPriority((short) 0);
         
         session.setRequestTime(now);
         session.setAssignmentTime(now);
         
         session.setUser(booking.getUser());
         session.setUserName(booking.getUserName());
         session.setUserNamespace(booking.getUserNamespace());
         
         session.setResourcePermission(booking.getResourcePermission());
         
         /* We need to remove the lag in redeeming the booking so we don't
          * propagate the lag to other bookings. */
         session.setDuration(booking.getDuration() - (int)(now.getTime() - this.rollTime) / 1000);
         session.setExtensions(booking.getResourcePermission().getAllowedExtensions());
         
         session.setResourceType(booking.getResourceType());
         if (ResourcePermission.RIG_PERMISSION.equals(booking.getResourceType()))
         {
             session.setRequestedResourceId(booking.getRig().getId());
             session.setRequestedResourceName(booking.getRig().getName());
         }
         else if (ResourcePermission.TYPE_PERMISSION.equals(booking.getResourceType()))
         {
             session.setRequestedResourceId(booking.getRigType().getId());
             session.setRequestedResourceName(booking.getRigType().getName());
         }
         else if (ResourcePermission.CAPS_PERMISSION.equals(booking.getResourceType()))
         {
             session.setRequestedResourceId(booking.getRequestCapabilities().getId());
             session.setRequestedResourceName(booking.getRequestCapabilities().getCapabilities());
         }
         session.setAssignedRigName(rig.getName());
         session.setRig(rig);
         
         session.setCodeReference(booking.getCodeReference());
         
         db.beginTransaction();
         db.save(session);
         booking.setActive(false);
         booking.setSession(session);
         rig.setInSession(true);
         rig.setSession(session);
         db.getTransaction().commit();
         
         membooking.setBooking(booking);
         membooking.setSession(session);
         this.runningBookings.put(rig.getName(), membooking);
         
         this.logger.info("Assigned " + session.getUser().qName() + " to rig " + rig.getName() + " (session=" +
                 session.getId() + ").");
 
         if (this.notTest)
         {
             new RigAllocator().allocate(session, db);
         }
     }
     
     /**
      * Gets the session running on the rig.
      * 
      * @param rig rig that is running
      * @return running session, or null if none exists
      */
     public MBooking getRunningSession(Rig rig)
     {
         return this.runningBookings.get(rig.getName());
     }
     
     /**
      * Puts a running booking to session.
      * 
      * @param rig rig that is allocated
      * @param mb booking or puesdo booking of session
      */
     public void putRunningSession(Rig rig, MBooking mb)
     {
         this.runningBookings.put(rig.getName(), mb);
     }
     
     @Override
     public int getPeriod()
     {
         return Redeemer.REDEEM_INTERVAL;
     }
 
     @Override
     public void cleanUp()
     {
        /* Does nothing. */
     }
 }
