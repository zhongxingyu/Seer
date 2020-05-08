 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 20th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ClassLoadingMXBean;
 import java.lang.management.CompilationMXBean;
 import java.lang.management.GarbageCollectorMXBean;
 import java.lang.management.ManagementFactory;
 import java.lang.management.OperatingSystemMXBean;
 import java.lang.management.RuntimeMXBean;
 import java.lang.management.ThreadInfo;
 import java.lang.management.ThreadMXBean;
 import java.lang.reflect.Method;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Runtime information page.
  */
 public class InfoPage extends AbstractPage
 {
     /** The names of the tabs. */
     private final Map<String, String> tabNames;
     
     /** Methods that provide specific tab information. */
     private final Map<String, Method> tabMethods;
     
     /** The icons for the tabs. */
     private Map<String, String> tabIcons;
     
     /** The tool tips for the tabs */
     private Map<String, String> tabTooltips;
     
     public InfoPage()
     {
         super();
         
         this.tabNames = new LinkedHashMap<String, String>(4);
         this.tabNames.put("runtime", "Runtime Info");
         this.tabNames.put("resources", "System");
         this.tabNames.put("vm", "Java");
         this.tabNames.put("os", "OS");
         
         this.tabMethods = new HashMap<String, Method>(4);
         try
         {
             this.tabMethods.put("runtime", InfoPage.class.getMethod("runtimeTab"));
             this.tabMethods.put("resources", InfoPage.class.getMethod("resTab"));
             this.tabMethods.put("vm", InfoPage.class.getMethod("vmTab"));
             this.tabMethods.put("os", InfoPage.class.getMethod("osTab"));
         }
         catch (SecurityException e)
         {
             this.logger.error("Security exception access method of info page class, message: " + e.getMessage() + ". " +
             		"This is a bug so please report it.");
         }
         catch (NoSuchMethodException e)
         { 
             this.logger.error("No such method in info page class, message: " + e.getMessage() + ". " +
                     "This is a bug so please report it.");
         }
         
         this.tabIcons = new HashMap<String, String>(4);
         this.tabIcons.put("runtime", "runtime");
         this.tabIcons.put("resources", "res");
         this.tabIcons.put("vm", "javavm");
         this.tabIcons.put("os", "opsys");
         
         this.tabTooltips = new HashMap<String, String>(4);
         this.tabTooltips.put("runtime", "Displays runtime information like classpath and system properties...");
         this.tabTooltips.put("resources", "Displays resources being used by the rig client.");
         this.tabTooltips.put("vm", "Displays information about the in use Java virtual machine.");
         this.tabTooltips.put("os", "Displays operating system information.");
     }
     
     @Override
     public void preService(HttpServletRequest req)
     {
         if (this.tabMethods.containsKey(req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1)))
         {
             this.framing = false;
         }
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         String suf = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
         if (this.tabMethods.containsKey(suf))
         {
             try
             {
                 this.tabMethods.get(suf).invoke(this);
             }
             catch (Exception e)
             {
                 this.logger.error("Exception invoking info page method, message: " + e.getMessage() + ". This is a " +
                 		"bug so please report it.");
             }
         }
         else
         {
             this.indexPage();
         }
     }
     
     public void indexPage()
     {
         /* Tabs. */
         this.println("<div id='lefttabbar'>");
         this.println("  <ul id='lefttablist'>");
         
         int i = 0;
         for (Entry<String, String> t : this.tabNames.entrySet())
         {
             String name = t.getKey();
             String classes = "notselectedtab";
             if (i == 0) classes = "ui-corner-tl selectedtab";
             else if (i == this.tabNames.size() - 1) classes += " ui-corner-bl";
 
             this.println("<li><a id='" + name + "tab' class='" + classes + "' onclick='loadInfoTab(\"" + name + "\")'>");
             this.println("  <div class='linkbutcont'>");
             this.println("    <div class='linkbutconticon'>");
             this.println("      <img src='/img/info/" + this.tabIcons.get(name) + "_small.png' alt='" + name + "' />");
             this.println("    </div>");
             this.println("    <div class='linkbutcontlabel'>" + t.getValue() + "</div>");
             this.println("    <div id='" + name + "hover' class='tooltiphov ui-corner-all'>");
             this.println("      <div class='tooltipimg'><img src='/img/info/" + this.tabIcons.get(name) + ".png' alt='"+ name + "' /></div>");
             this.println("      <div class='tooltipdesc'>" + this.tabTooltips.get(name) + "</div>");
             this.println("    </div>");
             this.println("  </div>");
             this.println("</a></li>");
             
             i++;
         }
         
         this.println("  </ul>");
         this.println("</div>");
         
         /* Content pane. */
         this.println("<div id='contentspane' class='ui-corner-tr ui-corner-bottom'>");
         this.println("  <table id='contentstable'>");
         this.runtimeTab();
         this.println("  </table>");
         this.println("</div>");
 
         /* Tool tip events. */
         this.println("<script type='text/javascript'>");
         this.println("var ttStates = new Object();");
         this.println("var selectedTab = 'runtime';");
 
         this.println( 
                 "function loadInfoToolTip(name)\n" + 
                 "{\n" + 
                 "    if (ttStates[name])\n" + 
                 "    {\n" + 
                 "        $('#' + name + 'hover').fadeIn();\n" + 
                 "        $('#' + name + 'link').css('font-weight', 'bold');\n" + 
                 "    }\n" + 
         "}\n");
 
         this.println("$(document).ready(function() {");
         for (String name : this.tabTooltips.keySet())
         {
             this.println("    ttStates['" + name + "'] = false;");
             this.println("    $('#" + name + "tab').hover(");
             this.println("        function() {");
             this.println("            ttStates['" + name + "'] = true;");
             this.println("            setTimeout('loadInfoToolTip(\"" + name + "\")', 1200);");
             this.println("        },");
             this.println("        function() {");
             this.println("            if (ttStates['" + name + "'])");
             this.println("            {");
             this.println("                $('#" + name + "hover').fadeOut();");
             this.println("                $('#" + name + "link').css('font-weight', 'normal');");
             this.println("                ttStates['" + name + "'] = false;");
             this.println("            }");
             this.println("        }");
             this.println("     )");
 
         }
         
         /* Initial table styling. */
         this.println("  $('#contentstable tr:even').addClass('evenrow');");
 		this.println("  $('#contentstable tr:odd').addClass('oddrow');");
 		
 		/* Contents pane height. */
 		this.println("  $('#contentspane').css('height', $(window).height() - 230);");
 		this.println(
 		        "  $(window).resize(function() { " +
 				"    $('#contentspane').css('height', $(window).height() - 230);\n" +
 				"  });");
 		
 		/* Automatic page updating. */
 		this.println("  setTimeout(updateSelectedInfoTab, 10000);");
         
         this.println("})");
         this.println("</script>");
     }
     
     /**
      * Provides runtime information from the <tt>RuntimeMXBean</tt>. 
      */
     public void runtimeTab()
     {
         RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
         
         /* Name. */
         this.addRow("Name", runtime.getName());
         
         /* Start time. */
         Calendar start = Calendar.getInstance();
         start.setTimeInMillis(runtime.getStartTime());
         this.addRow("Start time",  start.get(Calendar.DAY_OF_MONTH) + "/" + (start.get(Calendar.MONTH) + 1) + "/" +
                 start.get(Calendar.YEAR) + " " + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE) + 
                 ":" + start.get(Calendar.SECOND));
         
         /* Uptime. */
         long uptime = runtime.getUptime() / 1000;
         StringBuilder uptimeStr = new StringBuilder();
         int seconds = (int) (uptime % 60);
         uptime /= 60;
         int mins = (int) uptime % 60;
         uptime /= 60;
         int hours = (int) uptime % 24;
         uptimeStr.append(uptime / 24);
         uptimeStr.append(" days, ");
         uptimeStr.append(hours);
         uptimeStr.append(" hours, ");
         uptimeStr.append(mins);
         uptimeStr.append(" minutes, ");
         uptimeStr.append(seconds);
         uptimeStr.append(" seconds.");
         this.addRow("Uptime", uptimeStr.toString());
         
         /* Working directory. */
         this.addRow("Working directory", System.getProperty("user.dir"));
         
         /* Classpath. */
         this.addRow("Class path", this.getListCell(runtime.getClassPath().split(System.getProperty("path.separator"))));
         this.addRow("Boot class path", this.getListCell(runtime.getBootClassPath().split(System.getProperty("path.separator"))));
         
         /* Library path. */
         this.addRow("Library path", this.getListCell(runtime.getLibraryPath().split(System.getProperty("path.separator"))));
         
         /* System properties. */
         this.addRow("System properties", this.getMapCell(runtime.getSystemProperties()));
         
         /* Environment variables. */
         this.addRow("Environment variables", this.getMapCell(System.getenv()));
     }
 
     /**
      * Resource usage information.
      */
     public void resTab()
     {
         Runtime runtime = Runtime.getRuntime();
         
         /* Memory information. */
         this.addRow("In use memory", runtime.totalMemory() / (1024 * 1024) - runtime.freeMemory() / (1024 * 1024) + " Mb");
         this.addRow("Free memory", runtime.freeMemory() / (1024 * 1024) + " Mb");
         this.addRow("Total memory", runtime.totalMemory() / (1024 * 1024) + " Mb");
         this.addRow("Max memory", runtime.maxMemory() / (1024 * 1024) + " Mb");
         
         /* Threading information. */
         ThreadMXBean thr = ManagementFactory.getThreadMXBean();
         this.addRow("Current threads", String.valueOf(thr.getThreadCount()));
         this.addRow("Daemon threads", String.valueOf(thr.getDaemonThreadCount()));
         this.addRow("Peak threads", String.valueOf(thr.getPeakThreadCount()));
         this.addRow("Total started threads", String.valueOf(thr.getTotalStartedThreadCount()));
         
         ThreadInfo threads[] = thr.dumpAllThreads(true, true);
         StringBuilder tt = new StringBuilder();
         tt.append("<table id='threadstab'>");
         tt.append("  <tr>");
         tt.append("    <th class='threadname'>Thread</th>");;
         tt.append("    <th class='threadivoc'>Stack trace</th>");
         tt.append("  </tr>");
         
         for (ThreadInfo t : threads)
         {
             tt.append("  <tr>");
             tt.append("    <td class='threadname'>");
             tt.append("Id: ");
             tt.append(t.getThreadId());
             tt.append("<br />Name: <br />");
             tt.append(t.getThreadName());
             tt.append("<br />State: ");
             tt.append(t.getThreadState());
             tt.append("<br />CPU time: ");
             tt.append(thr.getThreadCpuTime(t.getThreadId()) / 10e6);
             tt.append(" ms</td>");
             
             StackTraceElement ele[] = t.getStackTrace();
             tt.append("<td class='threadivoc'>");
             for (StackTraceElement e : ele)
             {
                 tt.append(e.toString());
                 tt.append("<br />");
             }
             tt.append("</td>");
             tt.append("  </tr>");
         }
         tt.append("</table>");
         this.addRow(null, tt.toString());
     }
     
     /**
      * Java virtual machine information.
      */
     public void vmTab()
     {
         RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
         
         /* Virtual machine informaion. */
         this.addRow("Virtual machine", runtime.getVmName());
         this.addRow("Version", runtime.getVmVersion());
         this.addRow("Vendor", runtime.getVmVendor());
         this.addRow("Specification", runtime.getSpecVersion());
         
         /* JIT information. */
         CompilationMXBean comp = ManagementFactory.getCompilationMXBean();
         if (comp == null)
         {
             this.addRow("JIT compilation", "No");
         }
         else
         {
             this.addRow("JIT compilation", "Yes");
             this.addRow("JIT compilier", comp.getName());
             this.addRow("Compilation time", String.valueOf(comp.getTotalCompilationTime()) + " ms");
         }
         
         /* Garbage collector time. */
         List<GarbageCollectorMXBean> garb = ManagementFactory.getGarbageCollectorMXBeans();
         this.addRow("Number of garbage collectors", String.valueOf(garb.size()));
         long gCollections = 0;
         long gTime = 0;
         StringBuilder gCollectors = new StringBuilder();
         for (GarbageCollectorMXBean g : garb)
         {
             if (!g.isValid()) continue;
             gCollectors.append(g.getName());
             gCollectors.append(", ");
             gCollections += g.getCollectionCount();
             gTime += g.getCollectionTime();
         }
         if (gCollectors.length() > 0) gCollectors.deleteCharAt(gCollectors.length() - 2);
         this.addRow("Garbage collectors", gCollectors.toString());
         this.addRow("Garbage collections", String.valueOf(gCollections));
         this.addRow("Garbage collector CPU time", String.valueOf(gTime) + " ms");
         
         /* Class loader information. */
         ClassLoadingMXBean clsloading = ManagementFactory.getClassLoadingMXBean();
         this.addRow("Loaded classes", String.valueOf(clsloading.getLoadedClassCount()));
         this.addRow("Unloaded classes", String.valueOf(clsloading.getUnloadedClassCount()));
         this.addRow("Total loaded classes", String.valueOf(clsloading.getTotalLoadedClassCount()));
     }
     
     /**
      * Operating system information.
      */
     public void osTab()
     {
         OperatingSystemMXBean opSys = ManagementFactory.getOperatingSystemMXBean();
         this.addRow("Operating system", opSys.getName());
         this.addRow("Version", opSys.getVersion());
         this.addRow("Arch", opSys.getArch());
         this.addRow("Processors", String.valueOf(opSys.getAvailableProcessors()));
         this.addRow("File separator", System.getProperty("file.separator"));
         this.addRow("Path separator", System.getProperty("path.separator"));
         
         /* File system information. */
         File[] fsRoots = File.listRoots();
         String fsRootsStr[] = new String[fsRoots.length];
         for (int i = 0; i < fsRoots.length; i++) fsRootsStr[i] = fsRoots[i].toString();
         this.addRow("File system roots", this.getListCell(fsRootsStr));
         
         for (File f : fsRoots)
         {
             String space = "Free: " + (f.getUsableSpace() / (1024 * 1024)) + " MB";
             space += "<br />";
             space += "Total: " + (f.getTotalSpace() / (1024 * 1024)) + " MB";
             this.addRow("File system '" + f.getPath() + "'", space);
         }
         
         /* Temporary directory. */
         this.addRow("Temp directory", System.getProperty("java.io.tmpdir"));
     }
     
     /**
      * Adds a row to the output.
      * 
      * @param prop property
      * @param val value
      */
     private void addRow(String prop, String val)
     {
         this.println("<tr>");
         if (prop == null)
         {
             this.println("  <td colspan='2' class='valcol'>" + val + "</td>");
         }
         else
         {
             this.println("  <td class='propcol'>" + prop + ":</td>");
             this.println("  <td class='valcol'>" + val + "</td>");
         }
         this.println("</tr>");
     }
     
     /**
      * Gets the contents of a cell containing a list.
      * 
      * @param contents list contents
      * @return cell
      */
     private String getListCell(String contents[])
     {
         String cwd = System.getProperty("user.dir");
         StringBuilder colbuf = new StringBuilder();
         colbuf.append("<ul class='listcell'>");
         
         for (String c : contents)
         {
             if (c != null) c = c.replace(cwd, ".");
             colbuf.append("  <li>");
             colbuf.append(c);
             colbuf.append("  </li>");
         }
         
         colbuf.append("</ul>");
         return colbuf.toString();
     }
     
     /**
      * Gets the contents of a cell containing a table
      * 
      * @param contents table contents
      * @return cell
      */
     private String getMapCell(Map<String, String> contents)
     {
         String cwd = System.getProperty("user.dir");
         
         StringBuilder sysProps = new StringBuilder();
         sysProps.append("<table class='tablecell'>");
         for (Entry<String, String> p : contents.entrySet())
         {
             String val = p.getValue();
            if (val != null) val = val.replace(cwd, ".");
             
             sysProps.append("<tr>");
             sysProps.append("  <td class='tablecellpropcol'>" + p.getKey() + "</td>");
             sysProps.append("  <td class='tablecellvalcol'>" + val + "</td>");
             sysProps.append("</tr>");
         }
         sysProps.append("</table>");
         return sysProps.toString();
     }
 
     @Override
     protected String getPageType()
     {
         return "Diagnostics";
     }
 }
