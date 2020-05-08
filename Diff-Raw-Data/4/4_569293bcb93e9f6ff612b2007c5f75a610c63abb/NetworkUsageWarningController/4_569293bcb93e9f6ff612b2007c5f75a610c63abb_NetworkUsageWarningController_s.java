 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2010 Funambol, Inc.
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 package com.funambol.client.controller;
 
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.localization.Localization;
 import com.funambol.client.customization.Customization;
 import com.funambol.client.controller.DialogController;
 import com.funambol.client.ui.Screen;
 import com.funambol.util.Log;
 
 public class NetworkUsageWarningController {
 
     private static final String TAG_LOG = "NetworkUsageWarningController";
 
     private Controller controller;
     private Runnable useAction;
     private Screen screen;
 
     private Configuration configuration;
     private Localization localization;
     private Customization customization;
 
     public NetworkUsageWarningController(Screen screen, Controller controller, Runnable useAction) {
         this.screen = screen;
         this.controller = controller;
         this.useAction = useAction;
         configuration = controller.getConfiguration();
         localization  = controller.getLocalization();
         customization = controller.getCustomization();
     }
 
     public void askUserNetworkUsageConfirmation() {
 
         // If the server is not cared or the network warning is disabled, we
         // just continue
        if (configuration.getServerType() != Configuration.SERVER_TYPE_FUNAMBOL_CARED ||
            !customization.getShowNetworkUsageWarningForProfiles())
        {
             useAction.run();
             return;
         }
 
         // Check if the configuration requires that we alert the user about a
         // network connection being made (the configuration must be valid,
         // otherwise we always warn the user)
         boolean showNetworkUsageWarning = true;
         long profileExpireDate = configuration.getProfileExpireDate();
         if (profileExpireDate != -1 && profileExpireDate > System.currentTimeMillis()) {
             // The configuration is still valid, check if we shall warn the user
             showNetworkUsageWarning = configuration.getProfileNetworkUsageWarning();
         }
 
         if (showNetworkUsageWarning) {
             DialogController dialControll = controller.getDialogController();
             String msg = localization.getLanguage("dialog_network_usage_warning_question");
             dialControll.askYesNoQuestion(screen, msg, false, useAction, null);
         } else {
             useAction.run();
         }
     }
 }
 
