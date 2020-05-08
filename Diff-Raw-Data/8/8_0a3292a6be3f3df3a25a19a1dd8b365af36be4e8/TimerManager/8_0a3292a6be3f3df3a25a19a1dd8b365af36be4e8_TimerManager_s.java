/* $Id: TimerManager.java,v 1.2 2005-11-26 02:18:26 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 
 import de.hampelratte.svdrp.responses.highlevel.VDRTimer;
 
 /**
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  * 
  * Class to manage all timers. No program logic, just a container
  */
 public class TimerManager {
 
     private static TimerManager instance;
 
     /**
      * Stores all timers as Timer objects
      */
     private ArrayList timers;
     
     /**
      * The VDR timers from the last session, which have been stored to disk
      */
     private ArrayList storedTimers = new ArrayList();
 
     private TimerManager() {
         timers = new ArrayList();
     }
 
     public static TimerManager getInstance() {
         if (instance == null) {
             instance = new TimerManager();
         }
         return instance;
     }
 
     public void addTimer(Timer timer) {
         if(!timer.isRepeating()) {
             timers.add(timer);
         } else {
             Calendar startTime = timer.getStartTime();
             Calendar endTime = timer.getEndTime();
             long duration = endTime.getTimeInMillis() - startTime.getTimeInMillis();
                         
             if(timer.hasFirstTime()) {
                 Calendar tmp = timer.getFirstTime();
                 startTime.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH));
                 startTime.set(Calendar.MONTH, tmp.get(Calendar.MONTH));
                 startTime.set(Calendar.YEAR, tmp.get(Calendar.YEAR));
             }
             
             for (int j = 0; j < 21; j++) { // next 3 weeks, more data is never available in TVB
                 Calendar tmpStart = (Calendar)startTime.clone();
                 tmpStart.add(Calendar.DAY_OF_MONTH, j);
                 if(timer.isDaySet(tmpStart)) {
                     Timer oneDayTimer = (Timer)timer.clone();
                     oneDayTimer.setStartTime(tmpStart);
                     long start = tmpStart.getTimeInMillis();
                     oneDayTimer.getEndTime().setTimeInMillis(start + duration);
                     timers.add(oneDayTimer);
                 }
             }
         }
     }
 
     public void removeTimer(Timer timer) {
         timers.remove(timer);
     }
     
     public void removeAll() {
         timers.clear();
     }
 
     /**
      * @return an ArrayList of Timer objects
      */
     public ArrayList getTimers() {
         return timers;
     }
     
     /**
      * @param vdrTimers an ArrayList of VDRTimer objects
      */
     public void setTimers(ArrayList vdrTimers) {
         for (Iterator it = vdrTimers.iterator(); it.hasNext();) {
             VDRTimer element = (VDRTimer) it.next();
             addTimer(new Timer(element));
         }
     }
 
     /**
      * 
      * @param progID
      *            the programID of a devplugin.Program object
      * @return the timer for this program or null
      */
     public Timer getTimer(String progID) {
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             if (progID.equals(timer.getTvBrowserProgID())) {
                 return timer;
             }
         }
         return null;
     }
     
     /**
      * Returns all timers, which couldn't be assigned to a program
      * @return an ArrayList with Timer objects
      */
     public ArrayList getNotAssignedTimers() {
         ArrayList list = new ArrayList();
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             if(!timer.isAssigned()) {
                 list.add(timer);
             }
         }
         return list;
     }
     
     public void printTimers() {
         System.out.println("########## Listing timers #################");
         for (Iterator it = timers.iterator(); it.hasNext();) {
             System.out.println(it.next());
         }
         System.out.println("################ End ######################");
     }
 
     public ArrayList getStoredTimers() {
         return storedTimers;
     }
 
     public void setStoredTimers(ArrayList storedTimers) {
         this.storedTimers = storedTimers;
     }
     
     /**
      * Checks storedTimers, if this timer has been mapped to Program before
      * @param timer
      * @return the ProgramID or null
      */
     public String hasBeenMappedBefore(Timer timer) {
         for (Iterator it = storedTimers.iterator(); it.hasNext();) {
             Timer storedTimer = (Timer) it.next();
             if(timer.getUniqueKey().equals(storedTimer.getUniqueKey())) {
                 return storedTimer.getTvBrowserProgID();
             }
         }
         return null;
     }
     
     
     public void replaceStoredTimer(Timer timer) {
         for (Iterator it = storedTimers.iterator(); it.hasNext();) {
             Timer storedTimer = (Timer) it.next();
             if(timer.getUniqueKey().equals(storedTimer.getUniqueKey())) {
                 storedTimers.remove(storedTimer);
                 storedTimers.add(timer);
             }
         }
     }
 }
