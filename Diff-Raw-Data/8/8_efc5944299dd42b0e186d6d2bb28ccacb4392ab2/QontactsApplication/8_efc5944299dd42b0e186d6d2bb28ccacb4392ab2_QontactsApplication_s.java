 /**
  * Qontacts Mobile Application
  * Qontacts is a mobile application that updates the address book contacts
  * to the new Qatari numbering scheme.
  * 
  * Copyright (C) 2010  Abdulrahman Saleh Alotaiba
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation version 3 of the License.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mawqey.qontacts.main;
 
 import com.mawqey.qontacts.screens.QontactsMainScreen;
 
 import i18n.QontactsResource;
 import net.rim.blackberry.api.homescreen.HomeScreen;
 import net.rim.device.api.i18n.Locale;
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.ApplicationManager;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.UiApplication;
 
 public class QontactsApplication extends UiApplication {
 	public static ResourceBundle res = ResourceBundle.getBundle(QontactsResource.BUNDLE_ID, QontactsResource.BUNDLE_NAME);
 	public static String language = Locale.getDefaultForSystem().getLanguage();
 	
 	public QontactsApplication(boolean autoStart) {
 		if (autoStart) {
 			//The application started using the auto start entry point.
 			//Setup the rollover icons.
 			final Bitmap regIcon = Bitmap.getBitmapResource("icon-precision-idle.png");
 			final Bitmap icon = Bitmap.getBitmapResource("icon-precision-highlight.png");
 		
 			invokeLater(new Runnable() {
 					public void run() {
 						ApplicationManager myApp = ApplicationManager.getApplicationManager();
 						boolean keepGoing = true;
 		
 						while (keepGoing) {
 							if (myApp.inStartup()) {
 								try {
 									Thread.sleep(1000);
 								} catch (Exception ex) {
 									//Couldn't sleep, handle exception.
 								}
 							} else {
 								HomeScreen.updateIcon(regIcon, 0);
 								HomeScreen.setRolloverIcon(icon, 0);
 								keepGoing = false;
 							}
 						}
 						//Exit the application.
 						System.exit(0);
 					}
 			});
 		} else {
 			//The application was started by the user.
 			//Start the application and display a GUI.
 			QontactsMainScreen mainScreen = new QontactsMainScreen();
 			pushScreen(mainScreen);
 		}
 	}
 	
 	public static void main(String[] args) {
 		if ( args != null && args.length > 0 && args[0].equals("gui") ){
 			//alternate entry point 
 			QontactsApplication theApp = new QontactsApplication(true);
 			theApp.enterEventDispatcher();
 		} else {
 			//main entry point 
 			QontactsApplication theApp = new QontactsApplication(false);
 			theApp.enterEventDispatcher();
 		}
 	}
 }
