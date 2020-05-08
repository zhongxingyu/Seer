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
 package com.google.jstestdriver.idea.ui;
 
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FilesCache;
 import com.google.jstestdriver.ServerShutdownAction;
 import com.google.jstestdriver.ServerStartupAction;
 import com.google.jstestdriver.idea.MessageBundle;
 import com.google.jstestdriver.idea.PluginResources;
 import com.google.jstestdriver.ui.CapturedBrowsersPanel;
 import com.google.jstestdriver.ui.StatusBar;
 
 import java.awt.event.ActionEvent;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Observer;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * A section of the Tool Window which controls the server and shows the captured browser status.
  *
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class ServerControlPanel extends JPanel {
   private StatusBar statusBar;
   private CapturedBrowsersPanel capturedBrowsersPanel;
   private ServerStartupAction serverStartupAction;
   // TODO - make configurable
   private static int serverPort = 9876;
   private FilesCache cache = new FilesCache(new HashMap<String, FileInfo>());
   private JTextField captureUrl;
 
   public ServerControlPanel() {
     statusBar = new StatusBar(StatusBar.Status.NOT_RUNNING, MessageBundle.getBundle());
     capturedBrowsersPanel = new CapturedBrowsersPanel();
     captureUrl = new JTextField() {{
       setEditable(false);
     }};
 
     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     add(new JPanel() {{
       setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       add(statusBar);
       add(new JButton(new ServerStartAction()));
       add(new JButton(new ServerStopAction()));
     }});
     add(new JPanel() {{
       setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       add(new JLabel(PluginResources.getCaptureUrlMessage()));
       add(captureUrl);
     }});
     add(capturedBrowsersPanel);
   }
 
     private class ServerStartAction extends AbstractAction {
     ServerStartAction() {
       super("", PluginResources.getServerStartIcon());
       putValue(SHORT_DESCRIPTION, "Start a local server");
     }
     public void actionPerformed(ActionEvent e) {
       CapturedBrowsers browsers = new CapturedBrowsers();
       browsers.addObserver(capturedBrowsersPanel);
       browsers.addObserver(statusBar);
      serverStartupAction = new ServerStartupAction(serverPort, browsers, cache);
       serverStartupAction.addObservers(Arrays.<Observer>asList(statusBar));
       serverStartupAction.run();
     }
   }
 
   private class ServerStopAction extends AbstractAction {
     ServerStopAction() {
       super("", PluginResources.getServerStopIcon());
       putValue(SHORT_DESCRIPTION, "Stop the local server");
     }
     public void actionPerformed(ActionEvent e) {
       if (serverStartupAction != null) {
         new ServerShutdownAction(serverStartupAction).run();
       }
     }
   }
 }
