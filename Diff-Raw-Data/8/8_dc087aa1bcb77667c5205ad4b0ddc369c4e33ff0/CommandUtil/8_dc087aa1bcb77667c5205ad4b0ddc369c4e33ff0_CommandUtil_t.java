 /*
  * This file is part of VisualVM for IDEA
  *
  * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *
  *     * Neither the name of the copyright holder nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.orfjackal.visualvm4idea.core.commands;
 
 import com.sun.tools.visualvm.application.Application;
 import com.sun.tools.visualvm.application.jvm.*;
 import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
 import net.orfjackal.visualvm4idea.visualvm.ProfilerSupportWrapper;
 import org.netbeans.lib.profiler.common.AttachSettings;
 import org.netbeans.modules.profiler.NetBeansProfiler;
 
import javax.swing.*;
 import java.util.Set;
 
 /**
  * @author Esko Luontola
  * @since 12.11.2008
  */
 public class CommandUtil {
 
     private CommandUtil() {
     }
 
     public static Application getProfiledApplication(int appUniqueId) {
         Application app;
         do {
             sleep(500);
             app = findProfiledApp(appUniqueId);
         } while (app == null);
         return app;
     }
 
     private static void sleep(int millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
             // ignore
         }
     }
 
     private static Application findProfiledApp(int appUniqueId) {
         Set<Application> apps = DataSourceRepository.sharedInstance().getDataSources(Application.class);
         for (Application app : apps) {
             Jvm jvm = JvmFactory.getJVMFor(app);
             if (jvm.getJvmArgs().contains(getAppUniqueIdCommand(appUniqueId))) {
                 return app;
             }
         }
         return null;
     }
 
     public static String getAppUniqueIdCommand(int appUniqueId) {
         return "-Dvisualvm4idea.appUniqueId=" + appUniqueId;
     }
 
     public static AttachSettings getAttachSettings(int profilerPort) {
         // com.sun.tools.visualvm.profiler.ApplicationProfilerView.MasterViewSupport.MasterViewSupport()
         // com.sun.tools.visualvm.profiler.ApplicationProfilerView.MasterViewSupport.initSettings()
         final AttachSettings attachSettings = new AttachSettings();
         attachSettings.setDirect(true);
         attachSettings.setHost("localhost");
         attachSettings.setPort(profilerPort);
         return attachSettings;
     }
 
     public static void openProfilerView(Application app) {
         // TODO: freezes visualvm if the app ends execution before the view opens
         //
         // A solution would be to start attach the profiler without starting the application, but
         // because org.netbeans.modules.profiler.NetBeansProfiler.attachToApp()
         // always calls org.netbeans.lib.profiler.TargetAppRunner.attachToTargetVMOnStartup()
         // this is not possible.
         //
         // The application gets frozen here:
         //
         // com.sun.tools.visualvm.profiler.ProfilerSupport.selectProfilerView()
         // -> com.sun.tools.visualvm.core.ui.DataSourceWindowManager.selectView()
         // -> com.sun.tools.visualvm.core.ui.DataSourceWindowManager.openWindowAndSelectView()
         //      * Is run in thread "DataSourceWindowManager Processor"
         //      * Apparently gets stuch in the first call to DataSourceViewsManager.sharedInstance().getViews(viewMaster)
         // ...
         // -> com.sun.tools.visualvm.jmx.JmxModelProvider.createModelFor(JmxModelProvider.java:65)
         // ...
         // -> sun.tools.attach.HotSpotVirtualMachine.loadAgentLibrary(HotSpotVirtualMachine.java:40)
         // -> sun.tools.attach.WindowsVirtualMachine.execute(WindowsVirtualMachine.java:82)
         // -> sun.tools.attach.WindowsVirtualMachine.connectPipe(Native Method)  *FREEZE*
 
         if (NetBeansProfiler.getDefaultNB().getTargetAppRunner().targetJVMIsAlive()) {
             ProfilerSupportWrapper.selectProfilerView(app);
         } else {
            JOptionPane.showMessageDialog(null, "" +
                    "Target JVM died before the profiler view could be opened, so opening the view was cancelled\n" +
                    "(otherwise VisualVM might have frozen). Add a Thread.sleep(1000) or a longer delay to the\n" +
                    "beginning of the main method to avoid this situation.",
                    "VisualVM Profiler for IntelliJ IDEA", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /*
     The point where the above code gets stuck:
 
 "DataSourceWindowManager Processor" daemon prio=2 tid=0x041bd800 nid=0x728 runnable [0x037df000..0x037dfa14]
    java.lang.Thread.State: RUNNABLE
 	at sun.tools.attach.WindowsVirtualMachine.connectPipe(Native Method)
 	at sun.tools.attach.WindowsVirtualMachine.execute(WindowsVirtualMachine.java:82)
 	at sun.tools.attach.HotSpotVirtualMachine.loadAgentLibrary(HotSpotVirtualMachine.java:40)
 	at sun.tools.attach.HotSpotVirtualMachine.loadAgentLibrary(HotSpotVirtualMachine.java:61)
 	at sun.tools.attach.HotSpotVirtualMachine.loadAgent(HotSpotVirtualMachine.java:85)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl$LocalVirtualMachine.loadManagementAgent(JmxModelImpl.java:908)
 	- locked <0x1c270370> (a com.sun.tools.visualvm.jmx.JmxModelImpl$LocalVirtualMachine)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl$LocalVirtualMachine.startManagementAgent(JmxModelImpl.java:865)
 	- locked <0x1c270370> (a com.sun.tools.visualvm.jmx.JmxModelImpl$LocalVirtualMachine)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl$ProxyClient.tryConnect(JmxModelImpl.java:585)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl$ProxyClient.connect(JmxModelImpl.java:555)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl.connect(JmxModelImpl.java:233)
 	at com.sun.tools.visualvm.jmx.JmxModelImpl.<init>(JmxModelImpl.java:199)
 	at com.sun.tools.visualvm.jmx.JmxModelProvider.createModelFor(JmxModelProvider.java:65)
 	at com.sun.tools.visualvm.jmx.JmxModelProvider.createModelFor(JmxModelProvider.java:42)
 	at com.sun.tools.visualvm.core.model.ModelFactory.getModel(ModelFactory.java:96)
 	- locked <0x1be05308> (a com.sun.tools.visualvm.jvmstat.application.JvmstatApplication)
 	at com.sun.tools.visualvm.tools.jmx.JmxModelFactory.getJmxModelFor(JmxModelFactory.java:69)
 	at com.sun.tools.visualvm.application.views.threads.ApplicationThreadsViewProvider.supportsViewFor(ApplicationThreadsViewProvider.java:44)
 	at com.sun.tools.visualvm.application.views.threads.ApplicationThreadsViewProvider.supportsViewFor(ApplicationThreadsViewProvider.java:41)
 	at com.sun.tools.visualvm.core.ui.DataSourceViewsManager.getViews(DataSourceViewsManager.java:121)
 	at com.sun.tools.visualvm.core.ui.DataSourceWindowManager.openWindowAndSelectView(DataSourceWindowManager.java:155)
 	at com.sun.tools.visualvm.core.ui.DataSourceWindowManager.access$000(DataSourceWindowManager.java:50)
 	at com.sun.tools.visualvm.core.ui.DataSourceWindowManager$3.run(DataSourceWindowManager.java:126)
 	at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:561)
 	at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:986)
 
    Locked ownable synchronizers:
 	- None
 
      */
 }
