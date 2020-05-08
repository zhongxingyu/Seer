 /*
 * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.eclipse.javascript.jstestdriver.core.model;
 
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.Multimap;
import com.google.jstestdriver.browser.BrowserCaptureEvent;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.SlaveBrowser;
 
 import java.util.Collection;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Root object to hold information about all the slave browsers. Is registered
  * as a listener so that it knows whenever a slave browser is captured.
  *
  * @author shyamseshadri@gmail.com (Shyam Seshadri)
  */
 public class SlaveBrowserRootData extends Observable implements Observer {
 
   private static class SingletonHolder {
     public static SlaveBrowserRootData instance = new SlaveBrowserRootData();
   }
 
   /**
    * Gets a singleton instance of {@link SlaveBrowserRootData}. Always guaranteed to be non-
    * {@code null}
    *
    * @return the singleton instance.
    */
   public static SlaveBrowserRootData getInstance() {
     return SingletonHolder.instance;
   }
 
   private final Multimap<Browser, SlaveBrowser> slaves = LinkedHashMultimap.create();
   private SlaveBrowserRootData() {}
 
 
   /**
    * @param browser the browser
    * @return All the slave browsers captured by the js test driver server for the particular browser
    */
   public Collection<SlaveBrowser> getSlaves(Browser browser) {
     return slaves.get(browser);
   }
 
   /**
    * @return the total number of slave browsers
    */
   public int getNumberOfSlaves() {
     return slaves.values().size();
   }
 
   /**
    * Observes Captured browsers by the JS Test Driver and gets notified whenever a new browser
    * has been captured. The observable object passed to this is the new slave browser which
    * has been captured by JS Test Driver server.
    */
   @Override
   public void update(Observable o, Object arg) {
     BrowserCaptureEvent event = (BrowserCaptureEvent) arg;
     SlaveBrowser slave = event.getBrowser();
     BrowserInfo browserInfo = slave.getBrowserInfo();
     if (browserInfo.getName().contains("Firefox")) {
       slaves.put(Browser.FIREFOX, slave);
     } else if (browserInfo.getName().contains("Chrome")) {
       slaves.put(Browser.CHROME, slave);
     } else if (browserInfo.getName().contains("Safari")) {
       slaves.put(Browser.SAFARI, slave);
     } else if (browserInfo.getName().contains("Microsoft Internet Explorer")) {
       slaves.put(Browser.IE, slave);
     } else if (browserInfo.getName().contains("Opera")) {
       slaves.put(Browser.OPERA, slave);
     }
     setChanged();
     notifyObservers(this);
   }
 
   /**
    * Clears all the slave browsers
    */
   public void clear() {
     slaves.clear();
     setChanged();
     notifyObservers(this);
   }
 
   /**
    * @return true if any slave browsers are captured
    */
   public boolean hasSlaves() {
     return getNumberOfSlaves() > 0;
   }
 
   @Override
   public String toString() {
     return "Browsers";
   }
 }
