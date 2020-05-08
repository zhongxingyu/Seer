 /*
  * This file is part of SmartStreets.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ru.jcorp.smartstreets.gui;
 
 import ru.jcorp.smartstreets.core.DataServiceImpl;
 import ru.jcorp.smartstreets.core.RoutingServiceImpl;
 import ru.jcorp.smartstreets.gui.sys.Utf8ResourceBundle;
 import ru.jcorp.smartstreets.services.DataService;
 import ru.jcorp.smartstreets.services.RoutingService;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  * <p>$Id$</p>
  *
  * @author Artamonov Yuriy
  */
 public class SmartStreetsApp {
 
     private ResourceBundle resourceBundle;
     private Map<String, Object> services = new HashMap<String, Object>();
 
     private static SmartStreetsApp instance;
 
     public SmartStreetsApp() {
         resourceBundle = Utf8ResourceBundle.getBundle("locale.messages");
 
         services.put(DataService.NAME, new DataServiceImpl());
         services.put(RoutingService.NAME, new RoutingServiceImpl());
     }
 
     public ImageIcon getResourceIcon(String name) {
         return new ImageIcon(SmartStreetsApp.class.getResource("/icons/" + name));
     }
 
     public Image getResourceImage(String name) {
         return getResourceIcon(name).getImage();
     }
 
     public String getMessage(String key) {
         return resourceBundle.getString(key);
     }
 
     @SuppressWarnings("unchecked")
     public <T> T getService(String name) {
         return (T) services.get(name);
     }
 
     public static SmartStreetsApp getInstance() {
         return instance;
     }
 
     public static void main(String[] args) {
         // Set localization
 //        Locale.setDefault(java.util.Locale.ENGLISH);
 
         // Application instance
         instance = new SmartStreetsApp();
 
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                 } catch (Exception e) {
                     e.printStackTrace();
                     return;
                 }
 
                 MainFrame mainFrame = new MainFrame();
                 mainFrame.setLocationByPlatform(true);
                 mainFrame.setVisible(true);
             }
         });
     }
 }
