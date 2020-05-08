 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.freedesktop_notifications;
 
 import com.dmdirc.Main;
 import com.dmdirc.addons.freedesktop_notifications.commons.StringEscapeUtils;
 import com.dmdirc.commandparser.CommandManager;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesManager;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.installer.StreamReader;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.Plugin;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.resourcemanager.ResourceManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * This plugin adds freedesktop Style Notifications to dmdirc.
  *
  * @author Shane 'Dataforce' McCormack
  */
 public final class FreeDesktopNotificationsPlugin extends Plugin implements
         ConfigChangeListener{
     /** The DcopCommand we created */
     private FDNotifyCommand command = null;
     /** Files dir */
     private static final String filesDir  = Main.getConfigDir() + "plugins/freedesktop_notifications_files/";
     /** notification timeout. */
     private int timeout;
     /** notification icon. */
     private String icon;
     /** Escape HTML. */
     private boolean escapehtml;
     /** Strict escape. */
     private boolean strictescape;
     /** Strip codes. */
     private boolean stripcodes;
     
     /**
      * Creates a new instance of the FreeDesktopNotifications Plugin.
      */
     public FreeDesktopNotificationsPlugin() {
         super();

        IdentityManager.getGlobalConfig().addChangeListener(getDomain(), this);
        setCachedSettings();
     }
 
     /**
      * Used to show a notification using this plugin.
      *
      * @param title Title of dialog if applicable
      * @param message Message to show
      * @return True if the notification was shown.
      */
     public boolean showNotification(final String title, final String message) {
         final ArrayList<String> args = new ArrayList<String>();
         
         args.add("/usr/bin/env");
         args.add("python");
         args.add(filesDir+"notify.py");
         args.add("-a");
         args.add("DMDirc");
         args.add("-i");
         args.add(icon);
         args.add("-t");
         args.add(Integer.toString(timeout * 1000));
         args.add("-s");
 
         if (title != null && !title.isEmpty()) {
             args.add(prepareString(title));
         } else {
             args.add("Notification from DMDirc");
         }
         args.add(prepareString(message));
         
         try {
             final Process myProcess = Runtime.getRuntime().exec(args.toArray(new String[]{}));
             final StringBuffer data = new StringBuffer();
             new StreamReader(myProcess.getErrorStream()).start();
             new StreamReader(myProcess.getInputStream(), data).start();
             try { myProcess.waitFor(); } catch (InterruptedException e) { }
             return true;
         } catch (SecurityException e) {
         } catch (IOException e) {
         }
         
         return false;
     }
     
     /**
      * Prepare the string for sending to dbus.
      *
      * @param input Input string
      * @return Input string after being processed according to config settings.
      */
     public final String prepareString(final String input) {
         String output = input;
         if (stripcodes) { output = Styliser.stipControlCodes(output); }
         if (escapehtml) {
 	    if (strictescape) {
                 output = StringEscapeUtils.escapeHtml(output);
             } else {
                 output = output.replaceAll("&", "&amp;");
                 output = output.replaceAll("<", "&lt;");
                 output = output.replaceAll(">", "&gt;");
             }
         }
         
         return output;
     }
 
     /**
      * Called when the plugin is loaded.
      */
     @Override
     public void onLoad() {
         command = new FDNotifyCommand(this);
 
         // Extract required Files
         final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName("freedesktop_notifications");
         
         // This shouldn't actually happen, but check to make sure.
         if (pi != null) {
             // Now get the RM
             try {
                 final ResourceManager res = pi.getResourceManager();
                 
                 // Make sure our files dir exists
                 final File newDir = new File(filesDir);
                 if (!newDir.exists()) {
                     newDir.mkdirs();
                 }
             
                 // Now extract the files needed
                 try {
                     res.extractResoucesEndingWith(newDir, ".py");
                     res.extractResoucesEndingWith(newDir, ".png");
                 } catch (IOException ex) {
                     Logger.userError(ErrorLevel.MEDIUM, "Unable to extract files for Free desktop notifications: " + ex.getMessage(), ex);
                 }
             } catch (IOException ioe) {
                 Logger.userError(ErrorLevel.LOW, "Unable to open ResourceManager for freedesktop_notifications: "+ioe.getMessage(), ioe);
             }
         }
     }
 
     /**
      * Called when this plugin is Unloaded.
      */
     @Override
     public synchronized void onUnload() {
         CommandManager.unregisterCommand(command);
     }
     
     /** {@inheritDoc} */
     @Override
     public void domainUpdated() {
         IdentityManager.getAddonIdentity().setOption(getDomain(), "general.icon", filesDir+"icon.png");
     }
     
     /** {@inheritDoc} */
     @Override
     public void showConfig(final PreferencesManager manager) {
         final PreferencesCategory general = new PreferencesCategory("FreeDesktop Notifications", "General configuration for FreeDesktop Notifications plugin.");
         
         general.addSetting(new PreferencesSetting(PreferencesType.INTEGER, getDomain(), "general.timeout", "Timeout", "Length of time in seconds before the notification popup closes."));
         general.addSetting(new PreferencesSetting(PreferencesType.TEXT, getDomain(), "general.icon", "icon", "Path to icon to use on the notification."));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.escapehtml", "Escape HTML", "Some Implementations randomly parse HTML, escape it before showing?"));
 	general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.strictescape", "Strict Escape HTML", "Strictly escape HTML or just the basic characters? (&, < and >)"));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.stripcodes", "Strip Control Codes", "Strip IRC Control codes from messages?"));
         
         manager.getCategory("Plugins").addSubCategory(general);
     }
 
     private void setCachedSettings() {
         timeout = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "general.timeout");
         icon = IdentityManager.getGlobalConfig().getOption(getDomain(), "general.icon");
         escapehtml = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.escapehtml");
 	strictescape = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.strictescape");
         stripcodes = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.stripcodes");
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         setCachedSettings();
     }
 }
 
