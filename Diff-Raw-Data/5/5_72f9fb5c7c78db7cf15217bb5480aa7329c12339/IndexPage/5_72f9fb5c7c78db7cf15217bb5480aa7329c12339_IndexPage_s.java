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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * The default page.
  */
 public class IndexPage extends AbstractPage
 {
     /** Page links. */
     private final Map<String, String> links;
     
     /** Operations. */
     private final Map<String, String> operations;
     
     /** Icons. */
     private final Map<String, String> icons;
     
     /** Tooltips. */
     private final Map<String, String> toolTips;
     
     public IndexPage()
     {
         this.links = new LinkedHashMap<String, String>(5);
         this.links.put("Status", "/status");
         this.links.put("Configuration", "/config");
         this.links.put("Logs", "/logs");
         this.links.put("Runtime_Information", "/info");
         this.links.put("Documentation", "/doc");
         
         this.operations = new LinkedHashMap<String, String>(4);
         this.operations.put("Restart", "");
         this.operations.put("Shutdown", "");
         
         this.icons = new HashMap<String, String>(9);
         this.icons.put("Status", "status");
         this.icons.put("Configuration", "config");
         this.icons.put("Logs", "logs");
         this.icons.put("Documentation", "doc");
         this.icons.put("Runtime_Information", "runtime");
         this.icons.put("Restart", "restart");
         this.icons.put("Shutdown", "shutdown");
         
         this.toolTips = new HashMap<String, String>(9);
         this.toolTips.put("Status", "The status of the rig client including session details and exerciser tests " +
         		"statuses.");
         this.toolTips.put("Configuration", "Allows configuration properties of the rig client can be changed.");
         this.toolTips.put("Logs", "Log viewer of the recent log messages of the rig client. Only the " + 
                 this.logger.getLogBuffer().length + " most recent log messages since the rig client was started are " +
                 "displayed");
         this.toolTips.put("Documentation", "Documentation about the rig client.");
         this.toolTips.put("Runtime_Information", "Runtime information about the rig client such as classpath, system " +
         		"properties, uptime...");
         this.toolTips.put("Restart", "Restarts the rig client. This is only a soft restart as the Java virtual " +
         		"machine is not restarted. If the classpath is modified, the rig client must be stopped and the rig" +
        		"client service started up.");
         this.toolTips.put("Shutdown", "Shuts down the rig client. Terminates the rig client service");
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         this.println("<div id='alllinks'>");
         
         /* Link pages. */
         this.println("<div id='linklist'>");
         this.println("  <div class='listtitle'>");
         this.println("      Pages");
         this.println("  </div>");
         this.println("  <ul class='ullinklist'>");
 
         int i = 0;
         for (Entry<String, String> e : this.links.entrySet())
         {
             String name = e.getKey();
             String classes = "linkbut plaina";
             if (i == 0) classes += " ui-corner-top";
             else if (i == this.links.size() - 1) classes += " ui-corner-bottom";
 
             this.println("       <li><a id='" + name + "link' class='" + classes + "' href='" + e.getValue() + "'>");
             this.println("           <div class='linkbutcont'>");
             this.println("               <div class='linkbutconticon'>");
             this.println("                   <img src='/img/" + this.icons.get(name) + "_small.png' alt='" + name + "' />");
             this.println("               </div>");
             this.println("               <div class='linkbutcontlabel'>" + this.stringTransform(name) + "</div>");
             this.println("               <div id='" + name + "hover' class='leghov ui-corner-all'>");
             this.println("                  <div class='legimg'><img src='/img/" + this.icons.get(name) + ".png' alt='"+ name + "' /></div>");
             this.println("                  <div class='legdesc'>" + this.toolTips.get(name) + "</div>");
             this.println("               </div>");
             this.println("           </div>");
             this.println("      </a></li>");
 
             i++;
         }
        
        this.println("   </ul>"); // ullinklist
        this.println("</div>"); // linklist
        
        /* Operations pages. */
        this.println("<div id='operationlist'>");
        this.println("  <div class='listtitle'>");
        this.println("      Operations");
        this.println("  </div>");
        this.println("  <ul class='ullinklist'>");
 
        i = 0;
        for (Entry<String, String> e : this.operations.entrySet())
        {
            String name = e.getKey();
            String classes = "linkbut plaina";
            if (i == 0) classes += " ui-corner-top";
            else if (i == this.links.size() - 1) classes += " ui-corner-bottom";
 
            this.println("       <li><a id='" + name + "link' class='" + classes + "' href='" + e.getValue() + "'>");
            this.println("           <div class='linkbutcont'>");
            this.println("               <div class='linkbutconticon'>");
            this.println("                   <img src='/img/" + this.icons.get(name) + "_small.png' alt='" + name + "' />");
            this.println("               </div>");
            this.println("               <div class='linkbutcontlabel'>" + this.stringTransform(name) + "</div>");
            this.println("               <div id='" + name + "hover' class='leghov ui-corner-all'>");
            this.println("                   <div class='legimg'><img src='/img/" + this.icons.get(name) + ".png' alt='"+ name + "' /></div>");
            this.println("                   <div class='legdesc'>" + this.toolTips.get(name) + "</div>");
            this.println("               </div>");
            this.println("           </div>");
            this.println("      </a></li>");
 
            i++;
        }
       
       this.println("   </ul>"); // ullinklist
       this.println("</div>"); // operationlist
       
       this.println("</div>");
       
       /* Tooltip hover events. */
       this.println("<script type='text/javascript'>");
       
       this.println("var ttStates = new Object();");
       
       this.println( 
       		"function loadIndexToolTip(name)\n" + 
       		"{\n" + 
       		"    if (ttStates[name])\n" + 
       		"    {\n" + 
       		"        $('#' + name + 'hover').fadeIn();\n" + 
       		"        $('#' + name + 'link').css('font-weight', 'bold');\n" + 
       		"    }\n" + 
       		"}\n");
       
       this.println("$(document).ready(function() {");
       for (String name : this.toolTips.keySet())
       {
           this.println("    ttStates['" + name + "'] = false;");
           this.println("    $('#" + name + "link').hover(");
           this.println("        function() {");
           this.println("            ttStates['" + name + "'] = true;");
           this.println("            setTimeout('loadIndexToolTip(\"" + name + "\")', 1200);");
           this.println("        },");
           this.println("        function() {");
           this.println("            if (ttStates['" + name + "'])");
           this.println("            {");
           this.println("                $('#" + name + "hover').fadeOut();");
          this.println("                $('#" + name + "link').css('font-weight', 'bold');");
           this.println("                ttStates['" + name + "'] = false;");
           this.println("            }");
           this.println("        }");
           this.println("     )");
           
       }
       this.println("})");
       this.println("</script>");
     }
 
     @Override
     protected String getPageHeader()
     {
         return "Welcome to " + this.config.getProperty("Rig_Name");
     }
     
     @Override
     protected String getPageType()
     {
         return "Main";
     }
 }
