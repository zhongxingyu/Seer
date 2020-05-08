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
  * @date 11th November 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine;
 
 import static au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine.SlotBookingEngine.NUM_SLOTS;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine.MBooking.BType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 /**
  * Contains the bookings for a rig on a single day in terms of slots.
  */
 public class RigBookings
 {
     /** The day these booking refer to. */
     private String dayKey;
     
     /** Transient rig whose bookings these belong to. */
     private Rig rig;
     
     /** The list of booking slots of this rig. */
     private MBooking[] slots;
     
     /** The position of the first in use slot. */
     private int startSlot;
     
     /** The position of the last in use slot. */
     private int endSlot;
     
     /** The number of bookings this rig has. */
     private int numBookings;
     
     /** Rig type. */
     private String rigType;
     
     /** Next matching type bookings in resource loop. */
     private RigBookings typeNext;
     
     /** Next mapping caps bookings in resource loop. */
     private Map<String, RigBookings> capsNext;
     
     /** Logger. */
     private Logger logger;
     
     public RigBookings(Rig rig, String day)
     {
         this.logger = LoggerActivator.getLogger();
         
         this.rig = rig;
         this.dayKey = day;
         
         this.slots = new MBooking[NUM_SLOTS];
         this.startSlot = 0;
         this.endSlot = 0;
         this.numBookings = 0;
         
         this.rigType = rig.getRigType().getName();
         this.capsNext = new HashMap<String, RigBookings>();
     }
 
     /** 
      * Returns true if the number of rig slots free.
      * 
      * @param start the start slot
      * @param end the end slot
      * @return true if slots are free
      */
     public boolean areSlotsFree(int start, int end)
     {
         if (this.numBookings == 0) return true;
         
         start = start > this.startSlot ? start : this.startSlot;
         end = end < this.endSlot ? end : this.endSlot;
         
         for ( ; start <= end; start++)
         {
             if (this.slots[start] != null) return false;
         }
         
         return true;
     }
     
     /**
      * Returns true if the booking range is free.
      * 
      * @param booking booking to get slots
      * @return true if free
      */
     public boolean areSlotsFree(MBooking booking)
     {
         return this.areSlotsFree(booking.getStartSlot(), booking.getEndSlot());
     }
     
     /**
      * Returns true if the range is free.
      * 
      * @param range slots range
      * @return true if free
      */
     public boolean areSlotsFree(MRange range)
     {
         return this.areSlotsFree(range.getStartSlot(), range.getEndSlot());
     }
     
     /**
      * Gets the free slots of this rig. The response is in the form of 2D
      * array containing [slot start][number free].
      * 
      * @return free slots
      */
     public List<MRange> getFreeSlots()
     {
         return this.getFreeSlots(1);
     }
     
     /**
      * Gets the free booking holes for this rig. A hole is the threshold number of 
      * free slots. The minimum threshold is 1 (i.e. just free slots).
      * 
      * @param thres threshold for a hole
      * @return free slots
      */
     public List<MRange> getFreeSlots(int thres)
     {
         return this.getFreeSlots(0, this.slots.length - 1, thres);
     }
     
     /**
      * Gets the free booking holes for this rig within the start and end range.
      * A hole is the threshold number of free slots. The minimum threshold is 
      * 1 (i.e. just free slots).
      * 
      * @param start start index
      * @param end end index
      * @param thres threshold for a hole
      * @return free slots
      */
     public List<MRange> getFreeSlots(int start, int end, int thres)
     {
         List<MRange> free = new ArrayList<MRange>();
         
         if (this.numBookings == 0 || this.startSlot > end)
         {
             free.add(new MRange(start, end, this.dayKey));
             return free;
         }
         
         /* Coerce the threshold to at least 1 slot. */
         if (thres < 1) thres = 1;
         
         if (this.startSlot > start && this.startSlot - start >= thres)
         {
             free.add(new MRange(start, this.startSlot - 1, this.dayKey));
         }
         
         int num = this.numBookings - 1;
         
         int fs = start;
         /* If the first filled slot is passed the seek time, we have already 
          * marked it as free so start seeking at the end of the first booking. */
         if (this.startSlot >= start) fs = this.slots[this.startSlot].getEndSlot() + 1;
         /* If the start slot is already filled, start seeking at the end of that
          * booking. */
         if (this.slots[start] != null) fs = this.slots[start].getEndSlot() + 1;
         
         int es;
         while (num > 0)
         {
             
             /* Make sure the seek start is actually a free slot. This is the
              * case of adjacent booking. */
             while (fs < this.slots.length && this.slots[fs] != null) fs = this.slots[fs].getEndSlot() + 1;
             
             es = fs;
             
             /* Seek to the end of the free slots. */
             while (es < this.endSlot && this.slots[++es] == null);
             
             if (es >= end || es > this.endSlot)
             {
                 if (this.slots[es - 1] == null && fs <= end)
                 {
                     /* We have reached the end of the slots. */
                     if (es - fs >= thres) free.add(new MRange(fs, end < es ? end : end - 1, this.dayKey));
                 }
                 break;
             }
  
             num--;
             if (es - fs >= thres) free.add(new MRange(fs, es - 1, this.dayKey));
             fs = this.slots[es].getEndSlot() + 1;
         }
         
         if (end + 1 - this.endSlot > thres)
         {
             free.add(new MRange(this.endSlot + 1, end, this.dayKey));
         }
 
         return free;
     }
     
     /**
      * Commits a booking to this rig. 
      * 
      * @param booking booking to commit
      * @return true if successfully committed
      */
     public boolean commitBooking(MBooking booking)
     {
         /* Sanity check to make sure the end is after the beginning. */
         if (booking.getStartSlot() > booking.getEndSlot())
         {
             this.logger.error("Tried to commit a booking which ends before it starts. Some form of data corruption " +
             		"(manually editting the database?).");
             return false;
         }
         
         /* Sanity check to make sure the rig is free for this time. */
         if (!this.areSlotsFree(booking.getStartSlot(), booking.getEndSlot()))
         {
             this.logger.error("Tried to commit a booking to a non-free rig.");
             return false;
         }
         
         this.logger.debug("Commiting booking to rig " + this.rig.getName() + " on day " + this.dayKey + ", slot " +
                 booking.getStartSlot() + " through " + booking.getEndSlot() + '.');
         
         int s = booking.getStartSlot();
         if (this.numBookings == 0)
         {
             this.startSlot = s;
             this.endSlot = booking.getEndSlot();
         }
         else
         {
             if (this.startSlot > s) this.startSlot = s;
             if (this.endSlot < booking.getEndSlot()) this.endSlot = booking.getEndSlot();
         }
         this.numBookings++;
         
         for ( ; s <= booking.getEndSlot(); s++)
         {
             this.slots[s] = booking;
         }
        
         return true;
     }
     
     /**
      * Gets an earlier time a booking can be satisfied. This assumes the booking
      * couldn't be satisfied. The early fit may not have full duration if it cannot
      * be fitted at the latest early time.
      * 
      * @param mb booking to find early fit
      * @return range if found, null if not
      */
     public MRange getEarlyFit(MBooking mb)
     {
         int ss = -1, se = -1, rs, re, opt = mb.getNumSlots(), min = opt / 2;
         
         MBooking ex = this.getNextBooking(mb.getStartSlot());
         if (ex == null) return null;
         
         re = ex.getStartSlot() - 1;
         rs = re - min + 1;
         
         while (rs >= 0)
         {
             if (this.areSlotsFree(rs, re))
             {
                 ss = rs;
                 se = re;
                 if (re - rs + 1 < opt) rs--;
                 /* Optimal solution found. */
                 else return new MRange(ss, se, this.dayKey);
             }
             else
             {
                 /* If we already have a solution use that, otherwise go earlier. */
                 if (ss != -1) return new MRange(ss, se, this.dayKey);
                 
                 ex = this.getNextBooking(rs);
                 if (ex == null) return null;
                 
                 re = ex.getStartSlot() - 1;
                 rs = re - min - 1;
             }
         }
         
         if (ss != -1) return new MRange(ss, se, this.dayKey);
         
         /* Nothing found. */
         return null;
     }
     /**
      * Gets an later time a booking can be satisfied. This assumes the booking
      * couldn't be satisfied. The late fit may not have full duration if it cannot
      * be fitted at the earliest later time.
      * 
      * @param mb booking to find late fit
      * @return range if found, null if not
      */
     public MRange getLateFit(MBooking mb)
     {
         int ss = 0, se = 0, rs, re, opt = mb.getNumSlots(), min = opt / 2;
         
         MBooking ex = this.getNextBooking(mb.getStartSlot());
         if (ex == null) return null;
         
         rs = ex.getEndSlot() + 1;
         re = rs + min - 1;
         
         while (re <= SlotBookingEngine.NUM_SLOTS)
         {
             if (this.areSlotsFree(rs, re))
             {
                 ss = rs;
                 se = re;
                 if (re - rs + 1 < opt) re++;
                 /* Optimal solution found. */
                 else return new MRange(ss, se, this.dayKey);
             }
             else
             {
                 /* If we already have a solution use that, otherwise go earlier. */
                 if (ss != 0) return new MRange(ss, se, this.dayKey);
                 
                 ex = this.getNextBooking(re);
                 if (ex == null) return null;
                 
                 rs = ex.getEndSlot() + 1;
                 re = rs + min - 1;
             }
         }
         
         /* Nothing found. */
         return null;
     }
     
     
     /**
      * Returns true if this rig has the supplied booking.
      * 
      * @param booking booking to find
      * @return true if has booking
      */
     public boolean hasBooking(MBooking booking)
     {
         if (this.slots[booking.getStartSlot()] == null) return false;
         return this.slots[booking.getStartSlot()].equals(booking);
     }
     
     /**
      * Extends an existing booking.
      * 
      * @param booking booking to extend
      * @return true if successful
      */
     public boolean extendBooking(MBooking booking)
     {
         if (!this.hasBooking(booking))
         {
             this.logger.error("Attempted to extend of booking on rig " + this.rig.getName() + " which doesn't have the " +
             		"booking.");
             return false;
         }
         
         int s = booking.getStartSlot();
        while (s++ <= booking.getEndSlot()) this.slots[s] = booking;
         if (this.endSlot < booking.getEndSlot()) this.endSlot = booking.getEndSlot();
         return true;
     }
     
     /**
      * Removes a booking from a rig.
      * 
      * @param booking to remove
      * @return true if booking removed
      */
     public boolean removeBooking(MBooking booking)
     {
         this.logger.debug("Removing booking from rig " + this.rig.getName() + " on day " + this.dayKey + ", slot " +
                 booking.getStartSlot() + " through " + booking.getEndSlot() + '.');
         /* Sanity check to make sure this rig has the booking. */
         if (!this.hasBooking(booking))
         {
             this.logger.error("Tried to remove a booking from a rig that doesn't have the booking.");
             return false;
         }
         for (int start = booking.getStartSlot(); start <= booking.getEndSlot(); start++)
         {
             this.slots[start] = null;
         }
         
         if (--this.numBookings != 0)
         {
             /* Find the index of the first booking. */
             int s;
             for (s = 0; s < this.slots.length; s++)
             {
                 if (this.slots[s] != null) break;
             }
             this.startSlot = s;
             
             /* Find the index of the last booking. */
             for (s = this.slots.length - 1; s >= 0; s--)
             {
                 if (this.slots[s] != null) break;
             }
             this.endSlot = s;
         }
         
         return true;
     }
 
     /**
      * Gets the booking that is on the slot or immediately succeeds the
      * slot. If there is booking then <tt>null</tt> is returned.
      * 
      * @param slot start slot
      * @return booking or null if not found
      */
     public MBooking getNextBooking(int slot)
     {   
         /* No point seeking if there isn't a booking the slot following range. */
         if (slot > this.endSlot) return null;
         
         for ( ; slot < this.slots.length; slot++)
         {
             if (this.slots[slot] != null) return this.slots[slot];
         }
         
         return null;
     }
     
     /**
      * Returns a list of type bookings assigned to this rig.
      * 
      * @return type bookings
      */
     public List<MBooking> getTypeBookings()
     {
         if (this.numBookings == 0)
         {
             return Collections.emptyList();
         }
         
         List<MBooking> tb = new ArrayList<MBooking>();
         
         int ss = this.startSlot;
         MBooking mb;
         while (ss <= this.endSlot)
         {
             if ((mb = this.slots[ss]) == null)
             {
                ss++;
                continue;
             }
             
             if (mb.getType() == BType.TYPE) tb.add(mb);
             ss = mb.getEndSlot() + 1;
         }
         
         return tb;
     }
     
     /**
      * Returns a list of the specified capabilities bookings assigned to this
      * rig. 
      * 
      * @param caps request capabilities
      * @return capabilities bookings
      */
     public List<MBooking> getCapabilitiesBookings(RequestCapabilities caps)
     {
         return this.getCapabilitiesBookings(caps.getCapabilities());
     }
     
     /**
      * Returns a list of the specified capabilities bookings assigned to this
      * rig. 
      * 
      * @param caps request capabilities
      * @return capabilities bookings
      */
     public List<MBooking> getCapabilitiesBookings(String caps)
     {
         if (this.numBookings == 0)
         {
             return Collections.emptyList();
         }
         
         List<MBooking> cb = new ArrayList<MBooking>();
         
         int ss = this.startSlot;
         MBooking mb;
         while (ss <= this.endSlot)
         {
             if ((mb = this.slots[ss]) == null)
             {
                ss++;
                continue;
             }
             
             if (mb.getType() == BType.CAPABILITY  && mb.getRequestCapabilities().getCapabilities().equals(caps))
             {
                 cb.add(mb);
             }
             ss = mb.getEndSlot() + 1;
         }
         
         return cb;
     }
     
     /**
      * Returns the booking on the specified slot or null if there is no booking.
      * 
      * @param slot slot to get booking from
      * @return booking on slot or null if none
      */
     public MBooking getSlotBooking(int slot)
     {
         return this.slots[slot];
     }
     
     public int getNumBookings()
     {
         return this.numBookings;
     }
      
     public Rig getRig()
     {
         return this.rig;
     }
     
     public String getDayKey()
     {
         return this.dayKey;
     }
     
     public String getRigType()
     {
         return this.rigType;
     }
     
     public void setRigType(String type)
     {
         this.rigType = type;
     }
 
     public RigBookings getTypeLoopNext()
     {
         return this.typeNext;
     }
 
     public void setTypeLoopNext(RigBookings next)
     {
         this.typeNext = next;
     }
 
     public List<String> getCapabilities()
     {
         return new ArrayList<String>(this.capsNext.keySet());
     }
 
     public RigBookings getCapsLoopNext(RequestCapabilities caps)
     {
         return this.capsNext.get(caps.getCapabilities());
     }
     
     public RigBookings getCapsLoopNext(String caps)
     {
         return this.capsNext.get(caps);
     }
 
     public void setCapsLoopNext(RequestCapabilities caps, RigBookings next)
     {
         this.capsNext.put(caps.getCapabilities(), next);
     }
     
     public void setCapsLoopNext(String caps, RigBookings next)
     {
         this.capsNext.put(caps, next);
     }
     
     public void removeCapsLoopNext(RequestCapabilities caps)
     {
         this.capsNext.remove(caps.getCapabilities());
     }
     
     public void removeCapsLoopNext(String caps)
     {
         this.capsNext.remove(caps);
     }
     
     public void clearCapsLoopNext(RequestCapabilities caps)
     {
         this.capsNext.remove(caps.getCapabilities());
     }
 }   
