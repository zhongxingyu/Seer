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
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Logs viewer page.
  */
 public class LogsPage extends AbstractPage
 {
     @Override
     public void preService(HttpServletRequest req)
     {
         if (req.getRequestURI().endsWith("update"))
         {
             this.framing = false;
         }
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         String logs[] = this.logger.getLogBuffer();
         
         /* Reverse the array to get the latest logs first. */
         for (int i = 0; i < logs.length / 2; i++)
         {
             String t = logs[i];
             logs[i] = logs[logs.length - i - 1];
             logs[logs.length - i - 1] = t;
         }
         
         if (req.getRequestURI().endsWith("update"))
         {
             /* Update of the logs. */
             this.generateLogList(logs);
         }
         else
         {
             /* Normal logs page generation. */
             this.indexPage(logs);
         }
     }
 
     /**
      * Generates the index page.
      * 
      * @param logs log messages
      */
     private void indexPage(String logs[])
     {
         /* Add the list of logs. */
         if (logs.length == 0)
         {
             this.println("<div class='ui-state ui-state-highlight errdialog ui-corner-all'>");
             this.println("  <p>");
             this.println("      <span class='erricon ui-icon ui-icon-info'></span>");
             this.println("      There has been no log messages since rig client startup.");
             this.println("  </p>");
             this.println("</div>");
         }
         
         /* Display logs. */
         this.println("  <div id='logscontainer' class='detailspanel ui-corner-all'>");
         this.println("      <div class='detailspaneltitle'>");
         this.println("          <p>");
         this.println("              <span class='detailspanelicon ui-icon ui-icon-script'> </span>");
         this.println("              Logs:");
         this.println("          </p>");
         this.println("      </div>");
         this.println("      <div id='logscontent' class='detailspanelcontents'>");
         this.println("          <ul id='logslist'>");
         
         this.generateLogList(logs);
         
         this.println("          </ul>");
         this.println("      </div>"); // panel contents
         this.println("  </div>"); // logscontainer
         
         this.flushOut();
         
         /* Display some checkboxes to disable some log types. */
         this.println("  <div id='logchecks' class='detailspanel ui-corner-all'>");
         this.println("      <div class='detailspaneltitle'>");
         this.println("          <p>");
         this.println("              <span class='detailspanelicon ui-icon ui-icon-search'> </span>");
         this.println("              Show:");
         this.println("          </p>");
         this.println("      </div>");
         this.println("      <div class='detailspanelcontents'>");
         this.println("         <ul id='logschecklist'>");
         this.println("             <li>Fatal: <input id='fatalcheck' type='checkbox' checked='checked' /></li>");
         this.println("             <li>Priority: <input id='pricheck' type='checkbox' checked='checked' /></li>");
         this.println("             <li>Error: <input id='errorcheck' type='checkbox' checked='checked' /></li>");
         this.println("             <li>Warn: <input id='warncheck' type='checkbox' checked='checked' /></li>");
         this.println("             <li>Info: <input id='infocheck' type='checkbox' checked='checked' /></li>");
         this.println("             <li>Debug: <input id='debugcheck' type='checkbox' checked='checked' /></li>");
         this.println("          </ul>");
         this.println("      </div>");
         this.println("   </div>");
         
         /* Check events. */
         this.println("<script type='text/javascript'>");
         this.println(
                 "$('#fatalcheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.fatallog').slideDown();\n" + 
                 "    else $('.fatallog').slideUp();\n" + 
                 "});");
         
         this.println(
                 "$('#pricheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.prilog').slideDown();\n" + 
                 "    else $('.prilog').slideUp();\n" + 
                 "});");
         
         this.println(
                 "$('#errorcheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.errorlog').slideDown();\n" + 
                 "    else $('.errorlog').slideUp();\n" + 
                 "});");
         
         this.println(
                 "$('#warncheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.warnlog').slideDown();\n" + 
                 "    else $('.warnlog').slideUp();\n" + 
                 "});");
         
         this.println(
                 "$('#infocheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.infolog').slideDown();\n" + 
                 "    else $('.infolog').slideUp();\n" + 
                 "});");
        
         this.println(
                 "$('#debugcheck').change(function() {\n" + 
                 "    if ($(this).is(':checked')) $('.debuglog').slideDown();\n" + 
                 "    else $('.debuglog').slideUp();\n" + 
                 "});");
         
         /* Logs auto-update script. */
         this.println(
                 "$(document).ready(function() {\n" +
                 "   setTimeout(updateLogs, 5000);\n" +
                 /* Contents pane height. */
                "  $('#logscontent').css('height', $(window).height() - 258);" + 
                 "  $(window).resize(function() { " +
                "    $('#logscontent').css('height', $(window).height() - 258);\n" +
                 "  });" +
                 "});");
         
         this.println("</script>");
     }
 
     /**
      * Generates the lists of logs.
      * 
      * @param logs log messages
      */
     private void generateLogList(String[] logs)
     {
         for (int i = 0; i < logs.length; i++)
         {
             String type = "";
             if      (logs[i].contains("FATAL")) type = "fatallog";
             else if (logs[i].contains("PRIORITY")) type = "prilog";
             else if (logs[i].contains("ERROR")) type = "errorlog";
             else if (logs[i].contains("WARN")) type = "warnlog";
             else if (logs[i].contains("INFO")) type = "infolog";
             else if (logs[i].contains("DEBUG")) type = "debuglog";
             
             
             this.println("<li class='" + type + " " + (i % 2 == 0 ? "evenlog" : "oddlog")+ "'>");
             this.println("  <span class='logmessage'>" + logs[i] + "</span>");
             this.println("</li>");
         }
     }
     
     @Override
     protected String getPageHeader()
     {
         return "Log Viewer";
     }
 
     @Override
     protected String getPageType()
     {
         return "Logs";
     }
 }
