/* $Id: TimerManager.java,v 1.22 2007-05-15 19:12:32 hampelratte Exp $
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
 
 import java.util.*;
 
 import javax.swing.JOptionPane;
 
 import lazybones.actions.DeleteTimerAction;
 import lazybones.actions.ModifyTimerAction;
 import lazybones.gui.TimerOptionsDialog;
 import lazybones.gui.TimerSelectionDialog;
 import lazybones.gui.utils.TitleMapping;
 import lazybones.utils.Utilities;
 
 import org.hampelratte.svdrp.Connection;
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.VDRVersion;
 import org.hampelratte.svdrp.commands.LSTE;
 import org.hampelratte.svdrp.commands.LSTT;
 import org.hampelratte.svdrp.commands.NEWT;
 import org.hampelratte.svdrp.responses.R250;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.VDRTimer;
 import org.hampelratte.svdrp.util.EPGParser;
 import org.hampelratte.svdrp.util.TimerParser;
 
 import devplugin.Date;
 import devplugin.Program;
 
 /**
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  * 
  * Class to manage all timers.
  */
 public class TimerManager extends Observable {
     
     private transient static Logger logger = Logger.getLogger();
 
     private static TimerManager instance;
 
     /**
      * Stores all timers as Timer objects
      */
     private ArrayList<Timer> timers;
     
     /**
      * The VDR timers from the last session, which have been stored to disk
      */
     private ArrayList<Timer> storedTimers = new ArrayList<Timer>();
     
     /**
      * Stores mappings the user has made for later use.
      * The user has to map one Program only once. Later 
      * the mapping will be looked up here 
      */
     private TitleMapping titleMapping = new TitleMapping();
 
     private TimerManager() {
         timers = new ArrayList<Timer>();
     }
 
     public synchronized static TimerManager getInstance() {
         if (instance == null) {
             instance = new TimerManager();
         }
         return instance;
     }
 
     public void addTimer(Timer timer, boolean calculateRepeatingTimers) {
         addTimer(timer, calculateRepeatingTimers, true);
     }
     
     private void addTimer(Timer timer, boolean calculateRepeatingTimers, boolean notifyObservers) {
         if(!timer.isRepeating() || !calculateRepeatingTimers) {
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
             
             if(calculateRepeatingTimers) {
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
         
         if(notifyObservers) {
             setChanged();
             notifyObservers(new TimersChangedEvent(TimersChangedEvent.TIMER_ADDED, timer));
         }
     }
 
     public void removeTimer(Timer timer) {
         timers.remove(timer);
         setChanged();
         notifyObservers(new TimersChangedEvent(TimersChangedEvent.TIMER_REMOVED, timer));
     }
     
     @Override
     public void notifyObservers() {
         super.notifyObservers();
     }
 
     @Override
     public void notifyObservers(Object arg) {
         super.notifyObservers(arg);
     }
 
     public void removeAll() {
         timers.clear();
         setChanged();
         notifyObservers(new TimersChangedEvent(TimersChangedEvent.ALL, timers));
     }
 
     /**
      * @return a List of Timer objects
      */
     public List<Timer> getTimers() {
         return timers;
     }
     
     /**
      * @param vdrTimers an ArrayList of VDRTimer objects
      */
     public void setTimers(List vdrTimers, boolean calculateRepeatingTimers) {
         for (Iterator it = vdrTimers.iterator(); it.hasNext();) {
             VDRTimer element = (VDRTimer) it.next();
             addTimer(new Timer(element), calculateRepeatingTimers, false);
         }
         
         setChanged();
         notifyObservers(new TimersChangedEvent(TimersChangedEvent.ALL, getTimers()));
     }
 
     /**
      * 
     * @param progID
     *            the programID of a devplugin.Program object
      * @return the timer for this program or null
     * @see Program#getID()
      */
    public Timer getTimer(String progID) {
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             List<String> tvBrowserProdIDs = timer.getTvBrowserProgIDs();
             for (Iterator iter = tvBrowserProdIDs.iterator(); iter.hasNext();) {
                if (progID.equals((String) iter.next())) {
                     return timer;
                 }
             }
         }
         return null;
     }
     
     /**
      * 
      * @param timerNumber The number of the timer
      * @return The timer with the specified number
      */
     public Timer getTimer(int timerNumber) {
         for (Iterator iter = timers.iterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             if(timer.getID() == timerNumber) {
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
         ArrayList<Timer> list = new ArrayList<Timer>();
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             if(!timer.isAssigned()) {
                 list.add(timer);
             }
         }
         return list;
     }
     
     /**
      * For DEBUG only - print all timers to System.out
      *
      */
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
 
     public void setStoredTimers(ArrayList<Timer> storedTimers) {
         this.storedTimers = storedTimers;
     }
     
     /**
      * Checks storedTimers, if this timer has been mapped to Program before
      * @param timer
      * @return the ProgramID or null
      */
     public List<String> hasBeenMappedBefore(Timer timer) {
         for (Iterator it = storedTimers.iterator(); it.hasNext();) {
             Timer storedTimer = (Timer) it.next();
             if(timer.getUniqueKey().equals(storedTimer.getUniqueKey())) {
                 if(storedTimer.getReason() == Timer.NO_PROGRAM) {
                     List<String> timers = new ArrayList<String>();
                     timers.add("NO_PROGRAM");
                     return timers;
                 } else {
                     return storedTimer.getTvBrowserProgIDs();
                 }
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
                 return;
             }
         }
         
         // timer couldn't be found -> this is a new timer
         storedTimers.add(timer);
     }
 
     /**
      * 
      * @see TimerManager#titleMapping
      */
 	public HashMap getTitleMappingValues() {
 		return titleMapping.getAsMap();
 	}
 
 	/**
      * 
      * @see TimerManager#titleMapping
      */
 	public void setTitleMappingValues(HashMap titleMapping) {
 		this.titleMapping.setMappingFromMap(titleMapping);
 	}
     
     
     /**
      * Returns the next day, on which a timer events starts or stops, after the given calendar
      * @param currentDay
      * @return the next day, on which a timer events starts or stops, after the given calendar
      */
     public Calendar getNextDayWithEvent(Calendar currentDay) {
         List<Timer> timers = TimerManager.getInstance().getTimers();
         TreeSet<Calendar> events = new TreeSet<Calendar>();
         for (Iterator<Timer> iter = timers.iterator(); iter.hasNext();) {
             Timer timer = iter.next();
             events.add(timer.getStartTime());
             events.add(timer.getEndTime());
         }
         
         for (Iterator<Calendar> iter = events.iterator(); iter.hasNext();) {
             Calendar event = iter.next();
             if( !event.before(currentDay) & !Utilities.sameDay(event, currentDay)) {
                 return event;
             }
         }
         
         return null;
     }
     
     /**
      * @see #getNextDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public Calendar getPreviousDayWithEvent(Calendar currentDay) {
         List<Timer> timers = TimerManager.getInstance().getTimers();
         TreeSet<Calendar> events = new TreeSet<Calendar>();
         for (Iterator<Timer> iter = timers.iterator(); iter.hasNext();) {
             Timer timer = iter.next();
             events.add(timer.getStartTime());
             events.add(timer.getEndTime());
         }
         
         ArrayList<Calendar> eventList = new ArrayList<Calendar>(events);
         Collections.reverse(eventList);
         for (Iterator<Calendar> iter = eventList.iterator(); iter.hasNext();) {
             Calendar event = iter.next();
             if( !event.after(currentDay) & !Utilities.sameDay(event, currentDay)) {
                 return event;
             }
         }
         
         return null;
     }
     
     /**
      * @see #getNextDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public boolean hasNextDayWithEvent(Calendar currentDay) {
         return getNextDayWithEvent(currentDay) != null;
     }
     
     /**
      * @see #getPreviousDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public boolean hasPreviousDayWithEvent(Calendar currentDay) {
         return getPreviousDayWithEvent(currentDay) != null;
     }
 
     public TitleMapping getTitleMapping() {
         return this.titleMapping;
     }
 
     public void setTitleMapping(TitleMapping mapping) {
         this.titleMapping = mapping;
     }
     
     /**
      * Fetches the timer list from vdr
      */
     public void synchronize() {
         // unmark all tvbrowser programs
         unmarkPrograms();
         
         // clear timer list
         removeAll();
         
         // fetch current timer list from vdr
         Response res = VDRConnection.send(new LSTT());
         if (res != null && res.getCode() == 250) {
             logger.log("Timers retrieved from VDR",Logger.OTHER, Logger.INFO);
             String timersString = res.getMessage();
             List vdrtimers = TimerParser.parse(timersString);
             setTimers(vdrtimers, true);
         } else if (res != null && res.getCode() == 550) {
             // no timers are defined, do nothing
             logger.log("No timer defined on VDR",Logger.OTHER, Logger.INFO);
         } else { /* something went wrong, we have no timers -> 
                   * load the stored ones */
             logger.log(LazyBones.getTranslation("using_stored_timers",
                 "Couldn't retrieve timers from VDR, using stored ones."), 
                 Logger.CONNECTION, Logger.ERROR);
             
             ArrayList vdrtimers = getStoredTimers();
             setTimers(vdrtimers, false);
         }
         
         // detect conflicts
         ConflictFinder.getInstance().findConflicts();
         ConflictFinder.getInstance().handleConflicts();
     }
     
     /**
      * Unmarks all programs, which are marked by LazyBones
      */
     private void unmarkPrograms() {
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             for (Iterator iter = timer.getTvBrowserProgIDs().iterator(); iter.hasNext();) {
                 String progID = (String) iter.next();
                 if(progID != null) { // timer could be assigned
                     Date date = new Date(timer.getStartTime());
                     Program prog = LazyBones.getPluginManager().getProgram(date, progID);
                     if(prog != null) {
                         prog.unmark(LazyBones.getInstance());
                     }
                 }
             }
         }
     }
     
     public void deleteTimer(Timer timer) {
         DeleteTimerAction dta = new DeleteTimerAction(timer);
         if(!dta.execute()) {
             logger.log(LazyBones.getTranslation(
                     "couldnt_delete", "Couldn\'t delete timer:")
                     + " " + dta.getResponse().getMessage(), Logger.OTHER, Logger.ERROR);
             return;
         }
 
         List<String> progIDs = timer.getTvBrowserProgIDs();
         for (Iterator iter = progIDs.iterator(); iter.hasNext();) {
             String id = (String) iter.next();
             Program prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), id);
             if(prog != null) {
                 prog.unmark(LazyBones.getInstance());
             } else { // can be null, if program time is near 00:00, because then
                      // the wrong day is taken to ask tvb for the programm
                 prog = ProgramManager.getInstance().getProgram(timer.getEndTime(), id);
                 if(prog != null) {
                     prog.unmark(LazyBones.getInstance());
                 }
             }
         }
         TimerManager.getInstance().synchronize();
     }
     
     public void createTimer() {
         Timer timer = new Timer();
         timer.setChannelNumber(1);
         Program prog = ProgramManager.getInstance().getProgram(timer);
         
         // in this situation it makes sense to show the timer options
         // so we override the user setting (hide options dialog)
         boolean showTimerOptions = Boolean.TRUE.toString().equals(LazyBones.getProperties().getProperty("showTimerOptionsDialog"));
         LazyBones.getProperties().setProperty("showTimerOptionsDialog",Boolean.TRUE.toString());
         createTimer(prog);
         LazyBones.getProperties().setProperty("showTimerOptionsDialog",Boolean.toString(showTimerOptions));
     }
     
     public void createTimer(Program prog) {
         if (prog.isExpired()) {
             logger.log(LazyBones.getTranslation(
                     "expired", "This program has expired"), Logger.OTHER, Logger.ERROR);
             return;
         }
 
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
         long millis = cal.getTimeInMillis();
 
         Object o = ChannelManager.getChannelMapping().get(prog.getChannel().getId());
         if (o == null) {
             logger.log(LazyBones.getTranslation("no_channel_defined",
                     "No channel defined", prog.toString()), Logger.OTHER, Logger.ERROR);
             return;
         }
         int id = ((Channel) o).getChannelNumber();
         Response res = VDRConnection.send(new LSTE(Integer.toString(id), "at "
                 + Long.toString(millis / 1000)));
 
         if (res != null && res.getCode() == 215) {
             List epgList = EPGParser.parse(res.getMessage());
 
             if (epgList.size() <= 0) {
                 noEPGAvailable(prog, id);
                 return;
             }
 
             /*
              * VDR 1.3 already returns the matching entry, for 1.2 we need to
              * search for a match
              */
             VDRVersion version = Connection.getVersion();
             boolean isOlderThan1_3 = version.getMajor() < 1
                     || (version.getMajor() == 1 && version.getMinor() < 3);
             EPGEntry vdrEPG = isOlderThan1_3 ? Utilities.filterEPGDate(epgList,
                     ((Channel) o).getName(), millis) : (EPGEntry) epgList
                     .get(0);
 
             Timer timer = new Timer();
             timer.setChannelNumber(id);
             timer.addTvBrowserProgID(prog.getID());
             int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
             timer.setPriority(prio);
             int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
             timer.setLifetime(lifetime);
 
             int buffer_before = Integer.parseInt(LazyBones.getProperties()
                     .getProperty("timer.before"));
             int buffer_after = Integer.parseInt(LazyBones.getProperties()
                     .getProperty("timer.after"));
 
             if (vdrEPG != null) {
                 Calendar calStart = vdrEPG.getStartTime();
                 // start the recording x min before the beginning of the program
                 calStart.add(Calendar.MINUTE, -buffer_before);
                 timer.setStartTime(calStart);
 
                 Calendar calEnd = vdrEPG.getEndTime();
                 // stop the recording x min after the end of the program
                 calEnd.add(Calendar.MINUTE, buffer_after);
                 timer.setEndTime(calEnd);
 
                 timer.setFile(vdrEPG.getTitle());
                 timer.setDescription(vdrEPG.getDescription());
             } else { // VDR has no EPG data
                 noEPGAvailable(prog, id);
                 return;
             }
 
             boolean showOptionsDialog = Boolean.TRUE.toString().equals(
                     LazyBones.getProperties().getProperty("showTimerOptionsDialog"));
             
             if(showOptionsDialog) {
                 new TimerOptionsDialog(timer, prog, false);
             } else {
                 createTimerCallBack(timer, null, prog, false);
             }
             
 
         } else if(res != null && res.getCode() == 550 & "No schedule found\n".equals(res.getMessage())) {
             noEPGAvailable(prog, id);
         } else {
             String msg = res != null ? res.getMessage() : "Reason unknown";
             logger.log(LazyBones.getTranslation("couldnt_create",
                     "Couldn\'t create timer\n: ") + " " + msg,Logger.OTHER, Logger.ERROR);
         }
     }
     
     private void noEPGAvailable(Program prog, int channelID) {
         int buffer_before = Integer.parseInt(LazyBones.getProperties().getProperty("timer.before"));
         int buffer_after = Integer.parseInt(LazyBones.getProperties().getProperty("timer.after"));
 
         boolean dontCare = Boolean.FALSE.toString().equals(LazyBones.getProperties().getProperty("logEPGErrors"));
         int result = JOptionPane.NO_OPTION;
         if(!dontCare) {
             result = JOptionPane.showConfirmDialog(null, LazyBones.getTranslation(
                 "noEPGdata", ""), "", JOptionPane.YES_NO_OPTION);
         }
         if (dontCare || result == JOptionPane.OK_OPTION) {
             Timer newTimer = new Timer();
             newTimer.setState(VDRTimer.ACTIVE);
             newTimer.setChannelNumber(channelID);
             int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
             int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
             newTimer.setLifetime(lifetime);
             newTimer.setPriority(prio);
             newTimer.setTitle(prog.getTitle());
             newTimer.addTvBrowserProgID(prog.getID());
 
             Date d = prog.getDate();
             Calendar startTime = d.getCalendar();
 
             int start = prog.getStartTime();
             int hour = start / 60;
             int minute = start % 60;
             startTime.set(Calendar.HOUR_OF_DAY, hour);
             startTime.set(Calendar.MINUTE, minute);
 
             Calendar endTime = (Calendar) startTime.clone();
             endTime.add(Calendar.MINUTE, prog.getLength());
             
             // add buffers
             startTime.add(Calendar.MINUTE, -buffer_before);
             newTimer.setStartTime(startTime);
             endTime.add(Calendar.MINUTE, buffer_after);
             newTimer.setEndTime(endTime);
 
             new TimerOptionsDialog(newTimer, prog, false);
         }
     }
     
     /**
      * Called by TimerOptionsDialog, when the user confirms the dialog
      * 
      * @param timer
      *            The new created / updated Timer
      * @param oldTimer
      *            A clone of the timer with the old settings. Can be null for
      *            new timers.
      * @param prog
      *            The according Program
      * @param update
      *            If the Timer is a new one or if the timer has been edited
      */
     public void createTimerCallBack(Timer timer, Timer oldTimer, Program prog, boolean update) {
         int id = -1;
         if(prog != null) {
             Object o = ChannelManager.getChannelMapping().get(prog.getChannel().getId());
             if (o == null) {
                 logger.log(LazyBones.getTranslation("no_channel_defined","No channel defined", prog.toString()), Logger.OTHER, Logger.ERROR);
                 return;
             }
             id = ((Channel) o).getChannelNumber();
         }
         
         if (update) {
             ModifyTimerAction mta = new ModifyTimerAction(timer, oldTimer);
             if(mta.execute()) {
                 TimerManager.getInstance().synchronize();
             } else {
                 String mesg =  LazyBones.getTranslation(
                         "couldnt_change", "Couldn\'t change timer:")
                         + " " + mta.getResponse().getMessage();
                 logger.log(mesg, Logger.OTHER, Logger.ERROR);
             }
         } else {
             if (timer.getTitle() != null) {
                 int percentage = Utilities.percentageOfEquality(
                         prog.getTitle(), timer.getTitle());
                 if (timer.getFile().indexOf("EPISODE") >= 0
                         || timer.getFile().indexOf("TITLE") >= 0
                         || timer.isRepeating()) {
                     percentage = 100;
                 }
                 int threshold = Integer.parseInt(LazyBones.getProperties().getProperty("percentageThreshold"));
                 if (percentage > threshold) {
                     Response response = VDRConnection.send(new NEWT(timer));
                     
                     if (response == null) {
                         String mesg = LazyBones.getTranslation("couldnt_create",
                                 "Couldn\'t create timer:")
                                 + "\n"
                                 + LazyBones.getTranslation("couldnt_connect",
                                         "Couldn\'t connect to VDR");
                         logger.log(mesg, Logger.CONNECTION, Logger.ERROR);                
                         return;
                     }
                     
                     if (response instanceof R250) {
                         // since we dont have the ID of the new timer, we
                         // have to get the whole timer list again :-(
                         TimerManager.getInstance().synchronize();
                     } else {
                         logger.log(LazyBones.getTranslation("couldnt_create",
                                 "Couldn\'t create timer:")
                                 + " " + response.getMessage(), Logger.OTHER,
                                 Logger.ERROR);
                     }
                 } else {
                     logger.log("Looking in title mapping for timer "+timer, Logger.OTHER, Logger.DEBUG);
                     // lookup in mapping history
                     TimerManager tm = TimerManager.getInstance();
                     String timerTitle = (String)tm.getTitleMapping().getVdrTitle(prog.getTitle());
                     if(timer.getTitle().equals(timerTitle)) {
                         Response response = VDRConnection.send(new NEWT(timer));
                         if (response.getCode() == 250) {
                             timerCreatedOK(prog, timer);
                         } else {
                             logger.log(LazyBones.getTranslation("couldnt_create",
                                     "Couldn\'t create timer:")
                                     + " " + response.getMessage(), Logger.OTHER,
                                     Logger.ERROR);
                         }
                     } else { // no mapping found -> ask the user
                         showTimerConfirmDialog(timer, prog);
                     }
                 }
             } else { // VDR has no EPG data
                 noEPGAvailable(prog, id);
             }
         }
     }
     
     public void timerCreatedOK(Program prog, Timer timer) {
         timer.addTvBrowserProgID(prog.getID());
         replaceStoredTimer(timer);
         
         // since we dont have the ID of the new timer, we have
         // to get the whole timer list again :-(
         synchronize();
     }
     
     /**
      * If a Program can't be assigned to a VDR-Program, this method shows a
      * dialog to select the right VDR-Program
      * 
      * @param prog
      *            the Program selected in TV-Browser
      * @param timerOptions
      *            the timer from TimerOptionsDialog
      */
     private void showTimerConfirmDialog(Timer timerOptions, Program prog) {
         // get all programs 2 hours before and after the given program
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
 
         devplugin.Channel chan = prog.getChannel();
 
         TreeSet<Timer> programSet = new TreeSet<Timer>();
         for (int i = 10; i <= 120; i += 10) {
             // get the program before the given one
             Calendar c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             Timer t1 = ProgramManager.getInstance().getVDRProgramAt(c, chan);
             if (t1 != null) {
                 programSet.add(t1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             Timer t2 = ProgramManager.getInstance().getVDRProgramAt(c, chan);
             if (t2 != null) {
                 programSet.add(t2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (Iterator iter = programSet.iterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             Calendar time = timer.getStartTime();
             TimerProgram p = new TimerProgram(chan, new Date(time), time
                     .get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
             p.setTitle(timer.getTitle());
             p.setDescription("");
             p.setTimer(timer);
             programs[i++] = p;
         }
 
         // reverse the order of the programs
         Program[] temp = new Program[programs.length];
         for (int j = 0; j < programs.length; j++) {
             temp[j] = programs[programs.length - 1 - j];
         }
         programs = temp;
 
         // show dialog
         new TimerSelectionDialog(programs, timerOptions, prog);
     }
     
     public void deleteTimer(Program prog) {
        Timer timer = TimerManager.getInstance().getTimer(prog.getID());
         DeleteTimerAction dta = new DeleteTimerAction(timer);
         if(!dta.execute()) {
             logger.log(LazyBones.getTranslation(
                     "couldnt_delete", "Couldn\'t delete timer:")
                     + " " + dta.getResponse().getMessage(), Logger.OTHER, Logger.ERROR);
             return;
         }
         
         //prog.unmark(this);
         TimerManager.getInstance().synchronize();
     }
     
     public void editTimer(Timer timer) {
         Program prog = null;
         if(timer.getTvBrowserProgIDs().size() > 0) {
             prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), timer.getTvBrowserProgIDs().get(0));
         }
         new TimerOptionsDialog(timer, prog, true);
     }
     
     public boolean lookUpTimer(Timer timer, Program candidate) {
         logger.log("Looking in storedTimers for: " + timer.toString(),Logger.OTHER, Logger.DEBUG);
         List<String> progIDs = TimerManager.getInstance().hasBeenMappedBefore(timer);
         if (progIDs != null) { // we have a mapping of this timer to a program
             for (Iterator iter = progIDs.iterator(); iter.hasNext();) {
                 String progID = (String) iter.next();
 
                 if(progID.equals("NO_PROGRAM")) {
                     logger.log("Timer " + timer.toString()+" should never be assigned",Logger.OTHER, Logger.DEBUG);
                     timer.setReason(Timer.NO_PROGRAM);
                     return true;
                 } else {
                     devplugin.Channel c = ChannelManager.getInstance().getChannel(timer);
                     if (c != null) {
                         Date date = new Date(timer.getStartTime());
                         Iterator iterator = LazyBones.getPluginManager()
                                 .getChannelDayProgram(date, c);
                         while (iterator.hasNext()) {
                             Program p = (Program) iterator.next();
                             if (p.getID().equals(progID)
                                     && p.getDate().equals(date)) {
                                 p.mark(LazyBones.getInstance());
                                 timer.setTvBrowserProgIDs(progIDs);
                                 logger.log("Mapping found for: " + timer.toString(),
                                         Logger.OTHER, Logger.DEBUG);
                                 return true;
                             }
                         }
                     }
                 }
             }
         } else  {
             logger.log("No mapping found for: " + timer.toString(),Logger.OTHER, Logger.DEBUG);
             if(candidate != null) {
                 logger.log("Looking up old mappings", Logger.OTHER, Logger.DEBUG);
                 TimerManager tm = TimerManager.getInstance();
                 String progTitle = (String)tm.getTitleMapping().getTvbTitle(timer.getTitle());
                 if(candidate.getTitle().equals(progTitle)) {
                     candidate.mark(LazyBones.getInstance()); // wieso mark hier drin? lookup hrt sich nicht danach an
                     timer.addTvBrowserProgID(candidate.getID());
                     logger.log("Old mapping found for: " + timer.toString(),
                             Logger.OTHER, Logger.DEBUG);
                     return true;
                 }
             }
         }
 
         return false;
     }
 }
