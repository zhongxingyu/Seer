/* $Id: LazyBones.java,v 1.77 2007-06-09 19:59:07 hampelratte Exp $
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
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import lazybones.gui.MainDialog;
 import lazybones.gui.settings.VDRSettingsPanel;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.NEWT;
 
 import com.thoughtworks.xstream.XStream;
 
 import devplugin.*;
 
 /**
  * A VDRRemoteControl Plugin
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class LazyBones extends Plugin implements Observer {
 
     public static final Logger logger = Logger.getLogger();
 
     /** Translator */
     private static final util.ui.Localizer mLocalizer = util.ui.Localizer
             .getLocalizerFor(LazyBones.class);
 
     private MainDialog mainDialog;
 
     private static Properties props;
     
     private ContextMenuFactory cmf = new ContextMenuFactory();
     
     private static LazyBones instance;
     
     public static LazyBones getInstance() {
         return instance;
     }
     
     public ActionMenu getContextMenuActions(final Program program) {
         return cmf.createActionMenu(program);
     }
 
     private ButtonAction buttonAction;
     public ActionMenu getButtonAction() {
         buttonAction = new ButtonAction();
         buttonAction.setActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 getMainDialog().setVisible(true);
             }
         });
 
         buttonAction.setBigIcon(createImageIcon("lazybones/vdr24.png"));
         buttonAction.setSmallIcon(createImageIcon("lazybones/vdr16.png"));
         buttonAction.setShortDescription(LazyBones.getTranslation("lazybones", "Lazy Bones"));
         buttonAction.setText(LazyBones.getTranslation("lazybones", "Lazy Bones"));
 
         return new ActionMenu(buttonAction);
     }
 
     /**
      * Called by TimerSelectionDialog, if a VDR-Program has been selected
      * @param selectedProgram
      */
     public void timerSelectionCallBack(Program selectedProgram, Program originalProgram) {
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
         
         Timer t = ((TimerProgram) selectedProgram).getTimer();
         // start the recording x min before the beggining of
         // the program
         t.getStartTime().add(Calendar.MINUTE, -buffer_before);
         // stop the recording x min after the end of the
         // program
         t.getEndTime().add(Calendar.MINUTE, buffer_after);
         Response response = VDRConnection.send(new NEWT(t));
         if (response.getCode() == 250) {
             TimerManager.getInstance().timerCreatedOK(selectedProgram, t);
         } else {
             logger.log(LazyBones.getTranslation("couldnt_create",
                     "Couldn\'t create timer:")
                     + " " + response.getMessage(),
                     Logger.OTHER, Logger.ERROR);
         }
         
         logger.log("Storing " + originalProgram.getTitle() + "-"+t.getTitle()+ " in tvb2vdr", Logger.OTHER, Logger.DEBUG);
         TimerManager.getInstance().getTitleMapping().put(originalProgram.getTitle(), t.getTitle());
     }
 
     public String getMarkIconName() {
         return "lazybones/vdr16.png";
     }
 
     public PluginInfo getInfo() {
         String name = LazyBones.getTranslation("lazybones", "Lazy Bones");
         String description = LazyBones.getTranslation("desc",
                         "This plugin is a remote control for a VDR (by Klaus Schmidinger).");
         String author = "Henrik Niehaus, henrik.niehaus@gmx.de";
         return new PluginInfo(name, description, author, new Version(0, 4, false, "CVS-2007-05-27"));
     }
 
     
     public MainDialog getMainDialog() {
         if(mainDialog == null) {
             mainDialog = new MainDialog(getParent(), LazyBones.getTranslation(
                 "lazybones", "Lazy Bones"), false);
         }
         return mainDialog;
     }
 
     
     public devplugin.SettingsTab getSettingsTab() {
         return new VDRSettingsPanel();
     }
     
 
     public void onDeactivation() {
         try {
             Player.stop();
         } catch (Exception e) {
         }
     }
 
     public void loadSettings(Properties props) {
         LazyBones.props = props;
         
         // load data
         loadData();
         
         String host = props.getProperty("host");
         host = host == null ? "localhost" : host;
         props.setProperty("host", host);
         String streamtype = props.getProperty("streamtype");
         streamtype = streamtype == null ? "TS" : streamtype;
         props.setProperty("streamtype", streamtype);
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
         String timer_prio = props.getProperty("timer.prio");
         timer_prio = timer_prio == null ? "50" : timer_prio;
         String timer_lifetime = props.getProperty("timer.lifetime");
         timer_lifetime = timer_lifetime == null ? "50" : timer_lifetime;
         props.setProperty("timer.before", timer_before);
         props.setProperty("timer.after", timer_after);
         props.setProperty("timer.prio", timer_prio);
         props.setProperty("timer.lifetime", timer_lifetime);
         
         String numberOfCards = props.getProperty("numberOfCards");
         numberOfCards = numberOfCards == null ? "1" : numberOfCards;
         props.setProperty("numberOfCards", numberOfCards);
 
         String preview_url = props.getProperty("preview.url");
         preview_url = preview_url == null ? "http://localhost:8000/preview.jpg" : preview_url;
         String preview_path = props.getProperty("preview.path");
         preview_path = preview_path == null ? "/pub/web/preview.jpg" : preview_path;
         String preview_method = props.getProperty("preview.method");
         preview_method = preview_method == null ? "HTTP" : preview_method;
         props.setProperty("preview.url", preview_url);
         props.setProperty("preview.path", preview_path);
         props.setProperty("preview.method", preview_method);
 
         String switchBefore = props.getProperty("switchBefore");
         switchBefore = switchBefore == null ? "false" : switchBefore;
         props.setProperty("switchBefore", switchBefore);
         
         String logConnectionErrors = props.getProperty("logConnectionErrors");
         logConnectionErrors = logConnectionErrors == null ? "true" : logConnectionErrors;
         props.setProperty("logConnectionErrors", logConnectionErrors);
         Logger.logConnectionErrors = new Boolean(logConnectionErrors).booleanValue();
         
         String logEPGErrors = props.getProperty("logEPGErrors");
         logEPGErrors = logEPGErrors == null ? "true" : logEPGErrors;
         props.setProperty("logEPGErrors", logEPGErrors);
         Logger.logEPGErrors = new Boolean(logEPGErrors).booleanValue();
         
         String showTimerOptionsDialog = props.getProperty("showTimerOptionsDialog");
         showTimerOptionsDialog = showTimerOptionsDialog == null ? "true" : showTimerOptionsDialog;
         props.setProperty("showTimerOptionsDialog", showTimerOptionsDialog);
         
         String minChannelNumber = props.getProperty("minChannelNumber");
         minChannelNumber = minChannelNumber == null ? "0" : minChannelNumber;
         props.setProperty("minChannelNumber", minChannelNumber);
         String maxChannelNumber = props.getProperty("maxChannelNumber");
         maxChannelNumber = maxChannelNumber == null ? "0" : maxChannelNumber;
         props.setProperty("maxChannelNumber", maxChannelNumber);
         
         VDRConnection.host = host;
         VDRConnection.port = Integer.parseInt(port);
         VDRConnection.timeout = Integer.parseInt(timeout);
         
         init();
     }
     
     @SuppressWarnings("unchecked")
     private void loadData() {
         XStream xstream = new XStream();
         
         // load title mapping
         try {
             HashMap titleMapping = (HashMap) xstream.fromXML(props.getProperty("titleMapping"));
             TimerManager.getInstance().setTitleMappingValues(titleMapping);
         } catch (Exception e) {
             logger.log("Couldn't load title mapping: " + e, Logger.OTHER, Logger.WARN);
         }
         
         // load channel mapping
         try {
             Hashtable channelMapping = (Hashtable) xstream.fromXML(props.getProperty("channelMapping"));
             ChannelManager.setChannelMapping(channelMapping);
         } catch (Exception e) {
             logger.log("Couldn't load channel mapping: " + e, Logger.OTHER, Logger.WARN);
         }
         
         // load timers
         try {
             ArrayList timers = (ArrayList) xstream.fromXML(props.getProperty("timers"));
             TimerManager.getInstance().setStoredTimers(timers);
         } catch (Exception e) {
             logger.log("Couldn't load timers: " + e, Logger.OTHER, Logger.WARN);
         }
         
         // load channel list
         try {
             List channelList = (List) xstream.fromXML(props.getProperty("channelList")); 
             ChannelManager.getInstance().setChannels(channelList);
         } catch (Exception e) {
             logger.log("Couldn't load channel list: " + e, Logger.OTHER, Logger.WARN);
         }
         
         // remove outdated timers
         Calendar today = GregorianCalendar.getInstance();
         for (ListIterator iter = TimerManager.getInstance().getStoredTimers()
                 .listIterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             if (timer.getEndTime().before(today) & !timer.isRepeating()) {
                 iter.remove();
             }
         }
     }
 
     public void handleTvBrowserStartFinished() {
         // upload channel list from vdr
         logger.log("Updating channel list", Logger.OTHER, Logger.DEBUG);
         ChannelManager.getInstance().update();
         
         // get all timers from vdr
         TimerManager.getInstance().synchronize();
     }
     
     private void init() {
         instance = this;
         
         // observe the timer list
         TimerManager.getInstance().addObserver(this);
     }
 
     public Properties storeSettings() {
         storeData();        
         return props;
     }
 
     private void storeData() {
         XStream xstream = new XStream();
         props.setProperty( "channelMapping", xstream.toXML(ChannelManager.getChannelMapping()) );
         props.setProperty( "timers", xstream.toXML(TimerManager.getInstance().getTimers()) );
         props.setProperty( "titleMapping", xstream.toXML(TimerManager.getInstance().getTitleMappingValues()) );
         props.setProperty( "channelList", xstream.toXML(ChannelManager.getInstance().getChannels()) );
     }
 
     public static Properties getProperties() {
         return props;
     }
 
     public Icon getIcon(String path) {
         return createImageIcon(path);
     }
 
     public void handleTvDataUpdateFinished() {
         ProgramManager.getInstance().markPrograms();
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
         
         Iterator it = TimerManager.getInstance().getTimers().iterator();
         while(it.hasNext()) {
             Timer timer = (Timer)it.next();
             if(timer.isAssigned()) {
                 for (Iterator iter = timer.getTvBrowserProgIDs().iterator(); iter.hasNext();) {
                     String progID = (String)iter.next();
                     Program prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), progID);
                     if(prog != null) {
                         node.addProgram(prog);
                     } else { // can be null, if program time is near 00:00, because then
                              // the wrong day is taken to ask tvb for the programm
                         prog = ProgramManager.getInstance().getProgram(timer.getEndTime(), progID);
                         if(prog != null) {
                             node.addProgram(prog);
                         }
                     }
                 }
             }
         }
 
         node.update();
     }
 
     public boolean canUseProgramTree() {
         return true;
     }
 
     /*
      * public boolean canReceivePrograms() { return true; }
      * 
      * public void receivePrograms(Program[] progs) {
      * if(VDRConnection.isAvailable()) { for (int i = 0; i < progs.length; i++) {
      * createTimer(progs[i]); } } else { JOptionPane.showMessageDialog(null,
      * "Couldn't connect to VDR"); } }
      */
 
     public static String getTranslation(String key, String altText) {
         return mLocalizer.msg(key,altText);
     }
     
     public static String getTranslation(String key, String altText, String arg1) {
         return mLocalizer.msg(key,altText, arg1);
     }
     
     public static String getTranslation(String key, String altText, String arg1, String arg2) {
         return mLocalizer.msg(key,altText, arg1, arg2);
     }
     
     public JPopupMenu getSimpleContextMenu(Timer timer) {
         return cmf.createSimpleActionMenu(timer);
     }
     
     public void update(Observable arg0, Object arg1) {
         // mark all "timed" programs 
         ProgramManager.getInstance().markPrograms();
 
         // update the plugin tree
         updateTree();
     }
     
     public void synchronize() {
         TimerManager.getInstance().synchronize();
         RecordingManager.getInstance().synchronize();
     }
     
     private class ContextMenuFactory {
         public ActionMenu createActionMenu(final Program program) {
             AbstractAction action = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
 
                 }
             };
             action.putValue(Action.NAME, LazyBones.getTranslation("lazybones", "Lazy Bones"));
             action.putValue(Action.SMALL_ICON, createImageIcon("lazybones/vdr16.png"));
 
             Marker[] markers = program.getMarkerArr();
             boolean marked = false;
             for (int i = 0; i < markers.length; i++) {
                 if (markers[i].getId().equals(getId())) {
                     marked = true;
                     break;
                 }
             }
 
             int size = 3;
             if(marked) {
                 size++;
             }
             
             Action[] actions = null;
             if (marked) {
                 actions = new Action[size];
 
                 actions[1] = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         TimerManager.getInstance().deleteTimer(program);
                     }
                 };
                 actions[1].putValue(Action.NAME, LazyBones.getTranslation("dont_capture", "Delete timer"));
                 actions[1].putValue(Action.SMALL_ICON, createImageIcon("actions", "edit-delete", 16));
 
                 actions[2] = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                        Timer timer = (Timer) TimerManager.getInstance().getTimer(program);
                         TimerManager.getInstance().editTimer(timer);
                     }
                 };
                 actions[2].putValue(Action.NAME, LazyBones.getTranslation("edit", "Edit Timer"));
                 actions[2].putValue(Action.SMALL_ICON, createImageIcon("actions", "document-edit", 16));
 
                 actions[3] = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         LazyBones.getInstance().synchronize();
                     }
                 };
                 actions[3].putValue(Action.NAME, LazyBones.getTranslation("resync", "Synchronize with VDR"));
                 actions[3].putValue(Action.SMALL_ICON, createImageIcon("actions", "view-refresh", 16));
             } else {
                 actions = new Action[size];
 
                 actions[1] = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         TimerManager.getInstance().createTimer(program);
                     }
                 };
                 actions[1].putValue(Action.NAME, LazyBones.getTranslation("capture", "Capture with VDR"));
                 actions[1].putValue(Action.SMALL_ICON, createImageIcon("lazybones/capture.png"));
 
                 actions[2] = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         LazyBones.getInstance().synchronize();
                     }
                 };
                 actions[2].putValue(Action.NAME, LazyBones.getTranslation("resync", "Synchronize with VDR"));
                 actions[2].putValue(Action.SMALL_ICON, createImageIcon("actions", "view-refresh", 16));
             }
 
             actions[0] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     Player.play(program);
                 }
             };
             actions[0].putValue(Action.NAME, LazyBones.getTranslation("watch", "Watch this channel"));
             actions[0].putValue(Action.SMALL_ICON, createImageIcon("actions", "media-playback-start", 16));
 
             return new ActionMenu(action, actions);
         }
         
         JPopupMenu simpleMenu;
         public JPopupMenu createSimpleActionMenu(final Timer timer) {
             if(simpleMenu == null) {
                 simpleMenu = new JPopupMenu();
                 JMenuItem delItem = new JMenuItem(LazyBones.getTranslation("dont_capture", "Delete timer"), createImageIcon("actions", "edit-delete", 16));
                 delItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         TimerManager.getInstance().deleteTimer(timer);
                     }
                     
                 });
                 
                 JMenuItem editItem = new JMenuItem(LazyBones.getTranslation("edit", "Edit Timer"), createImageIcon("actions", "document-edit", 16));
                 editItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         TimerManager.getInstance().editTimer(timer);
                     }
                     
                 });
                 
                 simpleMenu.add(editItem);
                 simpleMenu.add(delItem);
             }
             
             return simpleMenu;
         }
         
     }
 }
