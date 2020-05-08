 /*
  * Copyright (c) Henrik Niehaus
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
 package lazybones.programmanager;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import lazybones.ChannelManager;
 import lazybones.LazyBones;
 import lazybones.LazyBonesTimer;
 import devplugin.Date;
 import devplugin.Program;
 
 /**
  * A class to lookup TVB programs by different criteria like a LazyBonesTimer, the unique program ID or time and channel.
  *
  * @author <a href="mailto:hampelratte@users.berlios.de">hampelratte@users.berlios.de</a>
  */
 public class ProgramDatabase {
 
     public static Program getProgram(LazyBonesTimer timer) {
         // determine channel
         devplugin.Channel chan = ChannelManager.getInstance().getTvbrowserChannel(timer);
 
         // determine middle of the program
         long startTime = timer.getStartTime().getTimeInMillis();
         long endTime = timer.getEndTime().getTimeInMillis();
         long duration = endTime - startTime;
         Calendar time = GregorianCalendar.getInstance();
         long middleTime = startTime + duration / 2;
         time.setTimeInMillis(middleTime);
 
         return getProgramAt(timer.getStartTime(), time, chan);
     }
 
     /**
      * @param startTime
      *            the startTime of the Program
      * @param middleTime
      *            the middleTime of the Program
      * @param chan
      *            the channel of the Program
      * @return the Program or null
      *
      *         startTime ist notwendig, weil getChannelDayProgram benutzt wird. Bsp.: start 23:30 ende 01:00 middleTime würde dann schon am nächsten tag liegen
      *         (00:15), so dass man nicht mehr das richtige channelDayProgram bekommt und das Program nicht findet
      */
     private static Program getProgramAt(Calendar startTime, Calendar middleTime, devplugin.Channel chan) {
         Iterator<Program> dayProgram = LazyBones.getPluginManager().getChannelDayProgram(new Date(startTime), chan);
         while (dayProgram != null && dayProgram.hasNext()) {
             Program prog = dayProgram.next();
 
             Calendar progStart = createStartTimeCalendar(prog);
 
             Calendar progEnd = GregorianCalendar.getInstance();
             progEnd.setTimeInMillis(progStart.getTimeInMillis());
             progEnd.add(Calendar.MINUTE, prog.getLength());
 
             if (middleTime.after(progStart) && middleTime.before(progEnd)) {
                 return prog;
             }
         }
         return null;
     }
 
     private static Calendar createStartTimeCalendar(Program prog) {
         Calendar progStart = GregorianCalendar.getInstance();
         progStart.set(Calendar.YEAR, prog.getDate().getYear());
         progStart.set(Calendar.MONTH, prog.getDate().getMonth() - 1);
         progStart.set(Calendar.DAY_OF_MONTH, prog.getDate().getDayOfMonth());
         progStart.set(Calendar.HOUR_OF_DAY, prog.getHours());
         progStart.set(Calendar.MINUTE, prog.getMinutes());
         return progStart;
     }
 
     /**
      *
      * @param uniqueProgID
      *            the unique program ID of the program
      * @return {@link devplugin.PluginManager#getProgram(String)}
      * @see devplugin.Program#getUniqueID()
      */
     public static Program getProgram(String uniqueProgID) {
         return LazyBones.getPluginManager().getProgram(uniqueProgID);
     }
 
     /**
      * Returns the day program of a day + the previous day's program + the next day's program
      *
      * @param date
      * @param chan
      * @return
      */
     public static List<Program> getThreeDayProgram(Date date, devplugin.Channel chan) {
         Date dayBefore = date.addDays(-1);
         Date dayAfter = date.addDays(1);
 
         List<Program> list = new ArrayList<Program>();
         addDayProgramToList(list, getChannelDayProgram(dayBefore, chan));
         addDayProgramToList(list, getChannelDayProgram(date, chan));
         addDayProgramToList(list, getChannelDayProgram(dayAfter, chan));
         return list;
     }
 
     private static void addDayProgramToList(List<Program> list, Iterator<Program> dayProgram) {
         while (dayProgram.hasNext()) {
             list.add(dayProgram.next());
         }
     }
 
     private static Iterator<Program> getChannelDayProgram(Date date, devplugin.Channel chan) {
         if (chan != null) {
            return new EmptyChannelDayProgram();
        } else {
             return LazyBones.getPluginManager().getChannelDayProgram(date, chan);
         }
     }
 
     private static class EmptyChannelDayProgram implements Iterator<Program> {
         @Override
         public boolean hasNext() {
             return false;
         }
 
         @Override
         public Program next() {
             return null;
         }
 
         @Override
         public void remove() {
         }
     }
 }
