/* $Id: LazyBones.java,v 1.8 2005-08-25 15:02:16 hampelratte Exp $
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
 
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.DateFormat;
 import java.util.*;
 import java.util.logging.Logger;
 
 import javax.swing.*;
 
 import de.hampelratte.svdrp.Connection;
 import de.hampelratte.svdrp.Response;
 import de.hampelratte.svdrp.VDRVersion;
 import de.hampelratte.svdrp.commands.*;
 import de.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import de.hampelratte.svdrp.responses.highlevel.VDRTimer;
 import de.hampelratte.svdrp.util.EPGParser;
 import de.hampelratte.svdrp.util.TimerParser;
 import devplugin.*;
 import devplugin.Date;
 
 /**
  * A VDRRemoteControl Plugin
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class LazyBones extends Plugin {
 
     public static final Logger LOG = Logger
             .getLogger(LazyBones.class.getName());
 
     /** Translator */
     private static final util.ui.Localizer mLocalizer = util.ui.Localizer
             .getLocalizerFor(LazyBones.class);
 
     private JDialog remoteControl;
 
     private PreviewPanel pp;
 
     private Properties props;
 
     /**
      * 
      */
     private Hashtable channelMapping = new Hashtable();
 
     /**
      * The current VDR timers
      */
     // private Vector vdrtimers = new Vector();
     private ArrayList vdrtimers = new ArrayList();
 
     /**
      * The VDR timers from the last session, which have been stored to disk
      */
     private ArrayList storedTimers = new ArrayList();
 
     /**
      * Stores a mapping of tv-browser programs to vdr-timers. If the users
      * deletes a timer, we can lookup the timer for the selected program with
      * this mapping
      */
     private Hashtable program2TimerMap = new Hashtable();
 
     /**
      * Stores a mapping of vdr titles, which totally differ from the titles of
      * tv-browser. This way, we can detect these programs and don't need to ask
      * the user again
      */
     private Hashtable vdr2browser = new Hashtable();
 
     private ArrayList notAssigned = new ArrayList();
 
     public ActionMenu getContextMenuActions(final Program program) {
         AbstractAction action = new AbstractAction() {
             private static final long serialVersionUID = 4909372697426213602L;
 
             public void actionPerformed(ActionEvent evt) {
 
             }
         };
         action.putValue(Action.NAME, mLocalizer.msg("vdr",
                 "Video Disk Recorder"));
         action.putValue(Action.SMALL_ICON,
                 createImageIcon("lazybones/vdr16.png"));
 
         Action[] actions = new Action[4];
         actions[0] = new AbstractAction() {
             private static final long serialVersionUID = -5553013336005178712L;
 
             public void actionPerformed(ActionEvent evt) {
                 watch(program);
             }
         };
         actions[0].putValue(Action.NAME, mLocalizer.msg("watch",
                 "Watch this channel"));
         actions[0].putValue(Action.SMALL_ICON,
                 createImageIcon("lazybones/vdr16.png"));
 
         PluginAccess[] plugins = program.getMarkedByPlugins();
         boolean marked = false;
         for (int i = 0; i < plugins.length; i++) {
             if (plugins[i].getId().equals(this.getId())) {
                 marked = true;
                 break;
             }
         }
 
         if (marked) {
             actions[1] = new AbstractAction() {
                 private static final long serialVersionUID = -3300041354617117202L;
 
                 public void actionPerformed(ActionEvent evt) {
                     deleteTimer(program);
                 }
             };
             actions[1].putValue(Action.NAME, mLocalizer.msg("dont_capture",
                     "Delete timer"));
             actions[1].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/cancel.png"));
 
             actions[2] = new AbstractAction() {
                 private static final long serialVersionUID = 2186961953492186827L;
 
                 public void actionPerformed(ActionEvent evt) {
                     VDRTimer timer = (VDRTimer) program2TimerMap.get(program);
                     editTimer(timer);
                 }
             };
             actions[2].putValue(Action.NAME, mLocalizer.msg("edit",
                     "Edit Timer"));
             actions[2].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/Edit16.png"));
         } else {
             actions[1] = new AbstractAction() {
                 private static final long serialVersionUID = -6045308970837086440L;
 
                 public void actionPerformed(ActionEvent evt) {
                     createTimer(program);
                 }
             };
             actions[1].putValue(Action.NAME, mLocalizer.msg("capture",
                     "Capture with VDR"));
             actions[1].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/capture.png"));
         }
 
         actions[3] = new AbstractAction() {
             private static final long serialVersionUID = -6741513103428614974L;
 
             public void actionPerformed(ActionEvent evt) {
                 getTimersFromVDR();
             }
         };
         actions[3].putValue(Action.NAME, mLocalizer.msg("resync",
                 "Synchronize with VDR"));
         actions[3].putValue(Action.SMALL_ICON,
                 createImageIcon("lazybones/Refresh16.gif"));
 
         return new ActionMenu(action, actions);
     }
 
     public ActionMenu getButtonAction() {
         ButtonAction action = new ButtonAction();
         action.setActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (remoteControl == null) {
                     initRemoteControl();
                 }
                 pp.startGrabbing();
                 remoteControl.setVisible(true);
             }
         });
 
         action.setBigIcon(createImageIcon("lazybones/vdr24.png"));
         action.setSmallIcon(createImageIcon("lazybones/vdr16.png"));
         action
                 .setShortDescription(mLocalizer.msg("vdr",
                         "Video Disk Recorder"));
         action.setText(mLocalizer.msg("vdr", "Video Disk Recorder"));
 
         return new ActionMenu(action);
     }
 
     private void watch(Program program) {
         Player.play(program, this);
     }
 
     private void deleteTimer(Program prog) {
         VDRTimer timer = (VDRTimer) program2TimerMap.get(prog);
         Response res = VDRConnection.send(new DELT(Integer.toString(timer
                 .getID())));
         if (res == null) {
             return;
         } else if (res.getCode() == 250) {
             prog.unmark(this);
             program2TimerMap.remove(prog);
             vdr2browser.remove(timer.toNEWT());
             getTimersFromVDR();
             updateTree();
             Enumeration en = program2TimerMap.keys();
             while (en.hasMoreElements()) {
                 Object key = en.nextElement();
                 // decrease the id of all timers with an id higher than that of
                 // the deleted timer. vdr does this too (how stupid!!)
                 VDRTimer t = (VDRTimer) program2TimerMap.get(key);
                 if (t.getID() > timer.getID()) {
                     t.setID(t.getID() - 1);
                 }
             }
         } else {
             JOptionPane.showMessageDialog(getParent(), mLocalizer.msg(
                     "couldnt_delete", "Couldn\'t delete timer:")
                     + " " + res.getMessage());
         }
     }
 
     private void createTimer(Program prog) {
         if (prog.isExpired()) {
             JOptionPane.showMessageDialog(getParent(), mLocalizer.msg(
                     "expired", "This program has expired"));
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
 
         Object o = channelMapping.get(prog.getChannel().getId());
         if (o == null) {
             String mesg = mLocalizer.msg("no_channel_defined",
                     "No channel defined", prog.toString());
             JOptionPane.showMessageDialog(getParent(), mesg);
             return;
         }
         int id = ((VDRChannel) o).getId();
         // TODO use getVDRProgramAt ?
         Response res = VDRConnection.send(new LSTE(Integer.toString(id), "at "
                 + Long.toString(millis / 1000)));
 
         if (res != null && res.getCode() == 215) {
             List epgList = EPGParser.parse(res.getMessage());
             /*
              * VDR 1.3 already returns the matching entry, for 1.2 we need to
              * search for a match
              */
             VDRVersion version = Connection.getVersion();
             boolean isOlderThan1_3 = version.getMajor() < 1
                     || (version.getMajor() == 1 && version.getMinor() < 3);
             EPGEntry vdrEPG = isOlderThan1_3 ? filterEPGDate(epgList,
                     ((VDRChannel) o).getName(), millis) : (EPGEntry) epgList
                     .get(0);
 
             VDRTimer timer = new VDRTimer();
             timer.setChannel(id);
             timer.setTvBrowserProgID(prog.getID());
             timer.setPriority(50);
             timer.setLifetime(50);
 
             int buffer_before = Integer.parseInt(props
                     .getProperty("timer.before"));
             int buffer_after = Integer.parseInt(props
                     .getProperty("timer.after"));
 
             if (vdrEPG != null) {
                 Calendar calStart = vdrEPG.getStartTime();
                 // start the recording x min before the beggining of the program
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
 
             boolean create = showTimerOptionsDialog(timer, false);
             if (create) {
                 if (timer.getTitle() != null) {
                     int percentage = Utilities.percentageOfEquality(prog
                             .getTitle(), timer.getTitle());
                     if (timer.getFile().indexOf("EPISODE") >= 0
                             || timer.getFile().indexOf("TITLE") >= 0
                             || timer.isRepeating()) {
                         percentage = 100;
                     }
                     int threshold = Integer.parseInt(props
                             .getProperty("percentageThreshold"));
                     if (percentage > threshold) {
                         Response response = VDRConnection.send(new NEWT(timer
                                 .toNEWT()));
                         if (response.getCode() == 250) {
                             // since we dont have the ID of the new timer, we
                             // have to
                             // get the whole timer list again :-(
                             getTimersFromVDR();
                         } else {
                             JOptionPane.showMessageDialog(null, mLocalizer
                                     .msg("couldnt_create",
                                             "Couldn\'t create timer:")
                                     + " " + response.getMessage());
                         }
                     } else {
                         Program selectedProgram = showTimerConfirmDialog(timer,
                                 prog);
                         if (selectedProgram != null) {
                             VDRTimer t = ((TimerProgram) selectedProgram)
                                     .getTimer();
                             // start the recording x min before the beggining of
                             // the program
                             t.getStartTime().add(Calendar.MINUTE,
                                     -buffer_before);
                             // stop the recording x min after the end of the
                             // program
                             t.getEndTime().add(Calendar.MINUTE, buffer_after);
                             Response response = VDRConnection.send(new NEWT(t
                                     .toNEWT()));
                             if (response.getCode() == 250) {
                                 timerCreatedOK(prog, t);
                             } else {
                                 JOptionPane.showMessageDialog(null, mLocalizer
                                         .msg("couldnt_create",
                                                 "Couldn\'t create timer:")
                                         + " " + response.getMessage());
                             }
                         }
                     }
                 } else { // VDR has no EPG data
                     noEPGAvailable(prog, id);
                 }
             }
         } else {
             JOptionPane.showMessageDialog(null, mLocalizer.msg(
                     "couldnt_create", "Couldn\'t create timer:"));
         }
     }
 
     private boolean showTimerOptionsDialog(VDRTimer timer, boolean updateDialog) {
         TimerOptionsDialog tod = new TimerOptionsDialog(this, timer,
                 updateDialog);
         tod.start();
         try {
             tod.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         return tod.getConfirmation();
     }
 
     private void noEPGAvailable(Program prog, int channelID) {
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
 
         int result = JOptionPane.showConfirmDialog(null, mLocalizer.msg(
                 "noEPGdata", ""), "", JOptionPane.YES_NO_OPTION);
         if (result == JOptionPane.OK_OPTION) {
             VDRTimer newTimer = new VDRTimer();
             newTimer.setActive(true);
             newTimer.setChannel(channelID);
             newTimer.setLifetime(50);
             newTimer.setPriority(50);
             newTimer.setTitle(prog.getTitle());
             newTimer.setTvBrowserProgID(prog.getID());
 
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
 
             boolean create = showTimerOptionsDialog(newTimer, false);
             if (create) {
                 Response res = VDRConnection.send(new NEWT(newTimer.toNEWT()));
                 if (res.getCode() == 250) {
                     timerCreatedOK(prog, newTimer);
                 }
             }
         }
     }
 
     private void timerCreatedOK(Program prog, VDRTimer t) {
         // store the vdr-name of this program
         String idString = prog.getID() + "###";
         int year = prog.getDate().getYear();
         int month = prog.getDate().getMonth();
         int day = prog.getDate().getDayOfMonth();
         idString += day + "_" + month + "_" + year + "###";
         idString += prog.getChannel().getId();
         vdr2browser.put(t.toNEWT(), idString);
 
         // since we dont have the ID of the new timer, we have
         // to get the whole timer list again :-(
         getTimersFromVDR();
     }
 
     /**
      * If a Program can't be assigned to a VDR-Program, this method shows a
      * dialog to select the right VDR-Program
      * 
      * @param prog
      *            the Program selected in TV-Browser
      * @param timerOptions
      *            the timer from TimerOptionsDialog
      * @return the selected VDR-Program
      */
     private Program showTimerConfirmDialog(VDRTimer timerOptions, Program prog) {
         // get all programs 2 hours before and after the given program
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
 
         Channel chan = prog.getChannel();
 
         TreeSet programSet = new TreeSet();
         for (int i = 10; i <= 120; i += 10) {
             // get the program before the given one
             Calendar c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             VDRTimer t1 = getVDRProgramAt(c, chan);
             if (t1 != null) {
                 programSet.add(t1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             VDRTimer t2 = getVDRProgramAt(c, chan);
             if (t2 != null) {
                 programSet.add(t2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (Iterator iter = programSet.iterator(); iter.hasNext();) {
             VDRTimer timer = (VDRTimer) iter.next();
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
         TimerSelectionDialog dialog = new TimerSelectionDialog(this);
         dialog.showSelectionDialog(programs);
         Program tmp = dialog.getProgram();
         if (tmp != null) {
             TimerProgram program = (TimerProgram) tmp;
             VDRTimer t = program.getTimer();
             t.setLifetime(timerOptions.getLifetime());
             t.setPriority(timerOptions.getPriority());
             t.setStartTime(timerOptions.getStartTime());
             t.setEndTime(timerOptions.getEndTime());
             t.setHasFirstTime(timerOptions.hasFirstTime());
             t.setFirstTime(timerOptions.getFirstTime());
             t.setRepeatingDays(timerOptions.getRepeatingDays());
             return program;
         }
         return null;
     }
 
     /**
      * If a timer can't be assigned to a Program, this method shows a dialog to
      * select the right Program
      * 
      * @param timer
      *            the timer received from the VDR
      */
     private void showProgramConfirmDialog(VDRTimer timer) {
         Calendar cal = timer.getStartTime();
 
         Calendar start = GregorianCalendar.getInstance();
         start.setTimeInMillis(cal.getTimeInMillis());
         Calendar end = timer.getEndTime();
         long length = end.getTimeInMillis() - start.getTimeInMillis();
         start.add(Calendar.MILLISECOND, (int) length);
 
         Enumeration en = channelMapping.keys();
         Channel chan = null;
         while (en.hasMoreElements()) {
             String channelID = (String) en.nextElement();
             VDRChannel channel = (VDRChannel) channelMapping.get(channelID);
             if (channel.getId() == timer.getChannel()) {
                 chan = getChannelById(channelID);
             }
         }
 
         // if we cant find the channel, stop
         if (chan == null)
             return;
 
         // get all programs 2 hours before and after the given program
         HashSet programSet = new HashSet();
         for (int i = 10; i <= 180; i += 10) {
             // get the program before the given one
             Calendar c = GregorianCalendar.getInstance();
             c.setTimeInMillis(start.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             Program p1 = getProgramAt(c, chan);
             if (p1 != null) {
                 programSet.add(p1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(start.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             Program p2 = getProgramAt(c, chan);
             if (p2 != null) {
                 programSet.add(p2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (Iterator iter = programSet.iterator(); iter.hasNext();) {
             Program p = (Program) iter.next();
             programs[i++] = p;
         }
         Arrays.sort(programs, new ProgramComparator());
 
         // show dialog
         ProgramSelectionDialog dialog = new ProgramSelectionDialog(this,
                 vdr2browser);
         dialog.showSelectionDialog(programs, timer);
     }
 
     private Program getProgramAt(Calendar cal, Channel chan) {
         Iterator dayProgram = getPluginManager().getChannelDayProgram(
                 new Date(cal), chan);
         cal.add(Calendar.MONTH, 1);
         while (dayProgram != null && dayProgram.hasNext()) {
             Program prog = (Program) dayProgram.next();
             Calendar progStart = GregorianCalendar.getInstance();
             progStart.set(Calendar.YEAR, prog.getDate().getYear());
             progStart.set(Calendar.MONTH, prog.getDate().getMonth());
             progStart
                     .set(Calendar.DAY_OF_MONTH, prog.getDate().getDayOfMonth());
             progStart.set(Calendar.HOUR_OF_DAY, prog.getHours());
             progStart.set(Calendar.MINUTE, prog.getMinutes());
 
             Calendar progEnd = GregorianCalendar.getInstance();
             progEnd.setTimeInMillis(progStart.getTimeInMillis());
             progEnd.add(Calendar.MINUTE, prog.getLength());
             if (cal.after(progStart) && cal.before(progEnd)) {
                 return prog;
             }
         }
         return null;
     }
 
     private VDRTimer getVDRProgramAt(Calendar cal, Channel chan) {
         long millis = cal.getTimeInMillis() / 1000;
         Object o = channelMapping.get(chan.getId());
         int id = ((VDRChannel) o).getId();
 
         LSTE cmd = new LSTE(Integer.toString(id), "at " + Long.toString(millis));
         Response res = VDRConnection.send(cmd);
         if (res != null && res.getCode() == 215) {
             List epg = EPGParser.parse(res.getMessage());
             if (epg.size() > 0) {
                 EPGEntry entry = (EPGEntry) epg.get(0);
                 VDRTimer timer = new VDRTimer();
                 timer.setChannel(id);
                 timer.setTitle(entry.getTitle());
                 timer.setStartTime(entry.getStartTime());
                 timer.setEndTime(entry.getEndTime());
                 timer.setDescription(entry.getDescription());
                 timer.setLifetime(50);
                 timer.setPriority(50);
                 return timer;
             }
         }
         return null;
     }
 
     public String getMarkIconName() {
         return "lazybones/vdr16.png";
     }
 
     public PluginInfo getInfo() {
         String name = mLocalizer.msg("vdr", "This program has expired");
         String description = mLocalizer
                 .msg("desc",
                         "This plugin is a remote control for a VDR (by Klaus Schmidinger).");
         String author = "Henrik Niehaus, hampelratte@users.sf.net";
         return new PluginInfo(name, description, author, new Version(0, 1));
     }
 
     /**
      * Called by loadSettings to initialize the GUI and other things
      */
     private void initRemoteControl() {
         remoteControl = new JDialog(getParent(), mLocalizer.msg(
                 "remoteControl", "VDR RemoteControl"), true);
         remoteControl.setSize(800, 450);
         remoteControl.getContentPane().setLayout(new GridBagLayout());
         remoteControl.getContentPane().add(
                 new RemoteControl(this),
                 new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));
         pp = new PreviewPanel(this);
         remoteControl.getContentPane().add(
                 pp,
                 new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));
         remoteControl.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
         remoteControl.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent event) {
                 pp.stopGrabbing();
                 remoteControl.setVisible(false);
             }
         });
     }
 
     public devplugin.SettingsTab getSettingsTab() {
         return new VDRSettingsPanel(this);
     }
 
     protected Hashtable getChannelMapping() {
         return channelMapping;
     }
 
     protected void setChannelMapping(Hashtable channelMapping) {
         this.channelMapping = channelMapping;
     }
 
     public void readData(ObjectInputStream in) {
         try {
             // load channel mapping
             channelMapping = (Hashtable) in.readObject();
 
             // load vdr2browser mapping for programs with totally different
             // titles
             vdr2browser = (Hashtable) in.readObject();
 
             // load stored timers. if no connection is available, we use these
             // ones
             storedTimers = (ArrayList) in.readObject();
         } catch (Exception e) {
             e.printStackTrace(System.out);
         }
 
         Calendar today = GregorianCalendar.getInstance();
 
         // remove old mappings
         Enumeration en = vdr2browser.keys();
         while (en.hasMoreElements()) {
             String key = (String) en.nextElement();
             String prog = (String) vdr2browser.get(key);
             String[] parts = prog.split("###");
             String dateString = parts[1];
             parts = dateString.split("_");
             int day = Integer.parseInt(parts[0]);
             int month = Integer.parseInt(parts[1]);
             int year = Integer.parseInt(parts[2]);
 
             Calendar progTime = GregorianCalendar.getInstance();
             progTime.set(Calendar.DAY_OF_MONTH, day);
             progTime.set(Calendar.MONTH, month - 1);
             progTime.set(Calendar.YEAR, year);
 
             if (progTime.before(today)) {
                 vdr2browser.remove(key);
             }
         }

        for (Iterator iter = storedTimers.iterator(); iter.hasNext();) {
             VDRTimer timer = (VDRTimer) iter.next();
             if (timer.getEndTime().before(today)) {
                storedTimers.remove(timer);
             }
         }
     }
 
     protected void getTimersFromVDR() {
         Enumeration en = program2TimerMap.keys();
         while (en.hasMoreElements()) {
             Program prog = (Program) en.nextElement();
             prog.unmark(this);
         }
 
         vdrtimers = new ArrayList();
         Response res = VDRConnection.send(new LSTT());
         if (res != null && res.getCode() == 250) {
             LOG.info("Timer retrieved from VDR");
             String timers = res.getMessage();
             vdrtimers = TimerParser.parse(timers);
         } else if (res != null && res.getCode() == 550) {
             LOG.info("No timer defined in VDR");
             // no timers are defined, do nothing
         } else { // something went wrong, we have no timers -> load the
             LOG.info("Error/no timer retrieved from VDR - using stored timer");
             // stored ones
             vdrtimers = storedTimers;
             JOptionPane.showMessageDialog(null, mLocalizer.msg(
                     "using_stored_timers",
                     "Couldn't retrieve timers from VDR, using stored ones."));
         }
 
         // mark all "timed" programs
         markPrograms(vdrtimers);
 
         // update the plugin tree
         updateTree();
     }
 
     private void markPrograms(ArrayList affectedTimers) {
         program2TimerMap = new Hashtable();
         // Iterator iter = vdrtimers.iterator();
         Iterator iter = affectedTimers.iterator();
         notAssigned.clear();
 
         // for every timer
         while (iter.hasNext()) {
             VDRTimer timer = (VDRTimer) iter.next();
             Channel chan = getChannel(timer);
             if (chan == null) {
                 // if we can't find a channel for this timer, continue with the
                 // next timer
                 String mesg = mLocalizer.msg("no_channel_defined",
                         "No channel defined", timer.toNEWT());
                 JOptionPane.showMessageDialog(getParent(), mesg);
                 // notAssigned.add(timer);
 
                 // HAMPELRATTE wenn der aufruf von getTimersFromVDR kommt,
                 // darf nicht einfach ein return kommen
                 // deshalb am besten die nicht verfgbaren sender
                 // merken und dann eine "massenmessage" ausgeben
                 return;
             }
 
             if (timer.isRepeating()) {
                 LOG.info("Marking repeating timer");
                 markRepeatingTimer(timer, chan);
             } else {
                 markSingularTimer(timer, chan);
             }
         }
 
         for (Iterator iterator = notAssigned.iterator(); iterator.hasNext();) {
             VDRTimer element = (VDRTimer) iterator.next();
             //FIXME fr repeating timers mssen wir uns noch was berlegen
             if (!element.isRepeating()) {
                 showProgramConfirmDialog(element);
             }
         }
     }
 
     private void markRepeatingTimer(VDRTimer timer, Channel chan) {
         // TODO repeating timers eventuell auch mappen, wenn program
         // nicht zugeordnet werden kann
 
         Calendar startDate = timer.getStartTime();
         Calendar endDate = timer.getEndTime();
 
         while (true) {
             if (timer.isDaySet(startDate)) {
                 Iterator dayProgram = Plugin.getPluginManager()
                         .getChannelDayProgram(new Date(startDate), chan);
                 if (dayProgram != null) {
                     boolean continue_marking = markSingularTimer(timer, chan);
                     /* DEBUG */
                     String date = startDate.get(Calendar.DAY_OF_MONTH) + "."
                             + (startDate.get(Calendar.MONTH) + 1);
                     LOG.info("Trying to mark " + chan + " on" + date + ":\""
                             + timer + "\"");
                     /* DEBUG end */
                     if (!continue_marking) {
                         break;
                     }
                 } else {
                     break;
                 }
             }
             startDate.add(Calendar.DAY_OF_MONTH, 1);
             endDate.add(Calendar.DAY_OF_MONTH, 1);
         }
     }
 
     /**
      * 
      * @param timer
      * @param chan
      * @return Returns, if markRepeatingTimer() should continue or not
      */
     private boolean markSingularTimer(VDRTimer timer, Channel chan) {
         // TODO show a list with timers, which couldn't be assigned
         // and show programConfirmDialog for these timers
 
         Calendar cal = GregorianCalendar.getInstance();
         int today = cal.get(Calendar.DAY_OF_MONTH);
 
         // subtract the recording buffers
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         timer.getStartTime().add(Calendar.MINUTE, buffer_before);
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
         timer.getEndTime().add(Calendar.MINUTE, -buffer_after);
 
         int day = timer.getStartTime().get(Calendar.DAY_OF_MONTH);
         int month = timer.getStartTime().get(Calendar.MONTH) + 1;
         int year = timer.getStartTime().get(Calendar.YEAR);
         // if day < today, then day is a date of the next month
         month = day < today ? month + 1 : month;
         Date date = new Date(year, month, day);
 
         // iterate over all programs of one day
         Iterator it = getPluginManager().getChannelDayProgram(date, chan);
         if (it != null) {
             TreeMap candidates = new TreeMap();
             while (it.hasNext()) {
                 Program prog = (Program) it.next();
                 int startTime = prog.getStartTime();
                 int endTime = startTime + prog.getLength();
 
                 Calendar timerStartCal = timer.getStartTime();
                 int hour = timerStartCal.get(Calendar.HOUR_OF_DAY);
                 int minute = timerStartCal.get(Calendar.MINUTE);
                 int timerstart = hour * 60 + minute;
 
                 Calendar timerEndCal = timer.getEndTime();
                 hour = timerEndCal.get(Calendar.HOUR_OF_DAY);
                 minute = timerEndCal.get(Calendar.MINUTE);
                 int timerend = hour * 60 + minute;
                 timerend = timerend < timerstart ? timerend + 1440 : timerend;
 
                 int deltaStart = startTime - timerstart;
                 int deltaEnd = endTime - timerend;
                 // System.out.println(timer.getTitle() + " " + deltaStart + ":"
                 // + deltaEnd);
                 
                 // collect candidates
                 if (Math.abs(deltaStart) <= 45 && Math.abs(deltaEnd) <= 45) {
                     candidates.put(new Integer(Math.abs(deltaStart)), prog);
                 }
             }
 
             // we subtract the buffers (at the beginning of this block) to find
             // the
             // program. if we have a repeating timer, we have to restore
             // original
             // times, so that we have the same situation for the next day
             timer.getStartTime().add(Calendar.MINUTE, -buffer_before);
             timer.getEndTime().add(Calendar.MINUTE, buffer_after);
 
             // System.out.println("kandidaten: " + candidates.size());
             if (candidates.size() == 0) {
                 boolean found = lookUpTimer(timer);
                 if (found) {
                     return true;
                 } else {
                     notAssigned.add(timer);
                     return false;
                 }
             }
 
             // get the best fitting candidate. this is the first key, because
             // TreeMap is sorted
             Program progMin = (Program) candidates.get(candidates.firstKey());
 
             // calculate the precentage of common words
             int percentage = Utilities.percentageOfEquality(timer.getTitle(),
                     progMin.getTitle());
 
             if (timer.getFile().indexOf("EPISODE") >= 0
                     || timer.getFile().indexOf("TITLE") >= 0
                     || timer.isRepeating()) {
                 percentage = 100;
             }
 
             int threshold = Integer.parseInt(props
                     .getProperty("percentageThreshold"));
             // if the percentage of common words is
             // higher than the config value percentageThreshold, mark this
             // program
             if (percentage > threshold) {
                 progMin.mark(this);
                 timer.setTvBrowserProgID(progMin.getID());
                 program2TimerMap.put(progMin, timer);
             } else {
                 boolean found = lookUpTimer(timer);
                 if (!found) { // we have no mapping
                     LOG.info("Couldn't find a program with that title: "
                             + timer.getTitle());
                     if (timer.isRepeating()) {
                         DateFormat df = DateFormat
                                 .getDateInstance(DateFormat.LONG);
                         JOptionPane.showMessageDialog(null, "["
                                 + df.format(new java.util.Date(timer
                                         .getStartTime().getTimeInMillis()))
                                 + "] "
                                 + timer.getTitle()
                                 + "\n"
                                 + mLocalizer.msg("couldnt_assign_repeating",
                                         "Couldn't assign repeating timer: "));
                         return false;
                     } else {
                         notAssigned.add(timer);
                         LOG.info("Couldn't assign timer: " + timer);
                     }
                 }
             }
         } else { // no channeldayprogram was found
             notAssigned.add(timer);
             LOG.info("Couldn't assign timer: " + timer);
         }
 
         return true;
     }
 
     private boolean lookUpTimer(VDRTimer timer) {
         Object oid = vdr2browser.get(timer.toNEWT());
         if (oid != null) { // we have a mapping of this timer to a program
             String idString = (String) oid;
             String[] parts = idString.split("###");
             String dateString = parts[1];
             String[] d = dateString.split("_");
             Date progDate = new Date(Integer.parseInt(d[2]), Integer
                     .parseInt(d[1]), Integer.parseInt(d[0]));
             String channelId = parts[2];
             String progID = parts[0];
             Channel[] channels = getPluginManager().getSubscribedChannels();
             Channel c = null;
             for (int i = 0; i < channels.length; i++) {
                 if (channels[i].getId().equals(channelId)) {
                     c = channels[i];
                     break;
                 }
             }
 
             if (c != null) {
                 Iterator iterator = getPluginManager().getChannelDayProgram(
                         progDate, c);
                 while (iterator.hasNext()) {
                     Program p = (Program) iterator.next();
                     if (p.getID().equals(progID)
                             && p.getDate().equals(progDate)) {
                         p.mark(this);
                         timer.setTvBrowserProgID(p.getID());
                         program2TimerMap.put(p, timer);
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
     private Channel getChannel(VDRTimer timer) {
         Channel chan = null;
         Enumeration en = channelMapping.keys();
         while (en.hasMoreElements()) {
             String channelID = (String) en.nextElement();
             VDRChannel channel = (VDRChannel) channelMapping.get(channelID);
             if (channel.getId() == timer.getChannel()) {
                 chan = getChannelById(channelID);
             }
         }
         return chan;
     }
 
     private Channel getChannelById(String id) {
         Channel[] channels = getPluginManager().getSubscribedChannels();
         for (int i = 0; i < channels.length; i++) {
             if (channels[i].getId().equals(id)) {
                 return channels[i];
             }
         }
         return null;
     }
 
     public void writeData(ObjectOutputStream out) {
         try {
             out.writeObject(channelMapping);
             out.writeObject(vdr2browser);
             out.writeObject(vdrtimers);
         } catch (IOException e) {
             e.printStackTrace(System.out);
         }
     }
 
     public void onDeactivation() {
         try {
             Player.stop();
         } catch (Exception e) {
         }
     }
 
     public void loadSettings(Properties props) {
         this.props = props;
 
         String host = props.getProperty("host");
         host = host == null ? "localhost" : host;
         props.setProperty("host", host);
         String port = props.getProperty("port");
         port = port == null ? "2001" : port;
         props.setProperty("port", port);
         String timeout = props.getProperty("timeout");
         timeout = timeout == null ? "500" : timeout;
         props.setProperty("timeout", timeout);
         String threshold = props.getProperty("percentageThreshold");
         threshold = threshold == null ? "45" : threshold;
         props.setProperty("percentageThreshold", threshold);
 
         String timer_before = props.getProperty("timer.before");
         timer_before = timer_before == null ? "5" : timer_before;
         String timer_after = props.getProperty("timer.after");
         timer_after = timer_after == null ? "10" : timer_after;
         props.setProperty("timer.before", timer_before);
         props.setProperty("timer.after", timer_after);
 
         String preview_url = props.getProperty("preview.url");
         preview_url = preview_url == null ? "http://htpc:8000/preview.jpg"
                 : preview_url;
         String preview_path = props.getProperty("preview.path");
         preview_path = preview_path == null ? "/pub/web/preview.jpg"
                 : preview_path;
         props.setProperty("preview.url", preview_url);
         props.setProperty("preview.path", preview_path);
 
         String switchBefore = props.getProperty("switchBefore");
         switchBefore = switchBefore == null ? "false" : switchBefore;
         props.setProperty("switchBefore", switchBefore);
 
         VDRConnection.host = host;
         VDRConnection.port = Integer.parseInt(port);
         VDRConnection.timeout = Integer.parseInt(timeout);
 
         getTimersFromVDR();
     }
 
     public Properties storeSettings() {
         return props;
     }
 
     public Properties getProperties() {
         return props;
     }
 
     public Icon getIcon(String path) {
         return createImageIcon(path);
     }
 
     public void handleTvDataAdded(ChannelDayProgram newProg) {
         markPrograms(newProg);
     }
 
     private void markPrograms(ChannelDayProgram prog) {
         ArrayList affectedTimers = new ArrayList();
         for (Iterator iterator = affectedTimers.iterator(); iterator.hasNext();) {
             VDRTimer timer = (VDRTimer) iterator.next();
             String progChan = prog.getChannel().getId();
             Date date = new Date(timer.getStartTime());
             Program timerProgram = Plugin.getPluginManager().getProgram(date,
                     timer.getTvBrowserProgID());
             if (timerProgram != null
                     && timerProgram.getChannel().getId().equals(progChan)) {
                 affectedTimers.add(timer);
             }
         }
         markPrograms(affectedTimers);
     }
 
     public void handleTvDataDeleted(ChannelDayProgram oldProg) {
         markPrograms(oldProg);
     }
 
     public void handleTvDataUpdateFinished() {
         // markPrograms();
     }
 
     public Frame getParent() {
         return getParentFrame();
     }
 
     /**
      * Updates the pluginview of TV-Browser
      */
     public void updateTree() {
         PluginTreeNode node = getRootNode();
         node.removeAllActions();
         node.removeAllChildren();
         Enumeration en = program2TimerMap.keys();
         while (en.hasMoreElements()) {
             Program prog = (Program) en.nextElement();
             node.addProgram(prog);
         }
 
         node.update();
     }
 
     public boolean canUseProgramTree() {
         return true;
     }
 
     private void editTimer(VDRTimer timer) {
         boolean change = showTimerOptionsDialog(timer, true);
 
         if (change) {
             Response response = VDRConnection.send(new UPDT(timer.toNEWT()));
 
             if (response == null) {
                 JOptionPane.showMessageDialog(null, mLocalizer.msg(
                         "couldnt_change", "Couldn\'t change timer:")
                         + "\n"
                         + mLocalizer.msg("couldnt_connect",
                                 "Couldn\'t connect to VDR"));
                 return;
             }
 
             if (response.getCode() == 250) {
                 // since we dont have the ID of the new timer, we have to
                 // get the whole timer list again :-(
                 getTimersFromVDR();
             } else {
                 JOptionPane.showMessageDialog(null, mLocalizer.msg(
                         "couldnt_change", "Couldn\'t change timer:")
                         + " " + response.getMessage());
             }
         }
     }
 
     /*
      * public boolean canReceivePrograms() { return true; }
      * 
      * public void receivePrograms(Program[] progs) {
      * if(VDRConnection.isAvailable()) { for (int i = 0; i < progs.length; i++) {
      * createTimer(progs[i]); } } else { JOptionPane.showMessageDialog(null,
      * "Couldn't connect to VDR"); } }
      */
 
     /**
      * Filters a list of EPGEntries by a provided VDR channel name and a time
      * which has to be in between a EPGEntries start and end time.
      * 
      * @param epgList
      *            list of EPGEntries retrieved from VDR
      * @param vdrChannelName
      *            VDR channel name which has to match a EPGEntry channel name
      * @param middleTime
      *            time of a program which has to be between start and end time
      *            of a EPGEntry
      * @return EPGEntry from the list which matches channel and time
      */
     private static EPGEntry filterEPGDate(List epgList, String vdrChannelName,
             long middleTime) {
         for (Iterator iter = epgList.iterator(); iter.hasNext();) {
             EPGEntry element = (EPGEntry) iter.next();
             if (element.getStartTime().getTimeInMillis() <= middleTime
                     && middleTime <= element.getEndTime().getTimeInMillis()
                     && vdrChannelName.equals(element.getChannelName())) {
                 return element;
             }
         }
         return null;
     }
 }
