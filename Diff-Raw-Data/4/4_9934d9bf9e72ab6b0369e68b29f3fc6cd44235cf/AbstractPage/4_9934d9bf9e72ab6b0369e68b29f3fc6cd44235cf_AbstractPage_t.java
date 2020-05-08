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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.StatusUpdater;
 import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Interface for the a embedded server page.
  */
 public abstract class AbstractPage
 {
     /** The output buffer. */
     protected StringBuilder buf;
     
     /** Servlet writer. */
     private PrintWriter out;
     
     /** The rig type class. */
     protected IRig rig;
     
     /** Loaded help documents. */
     protected static final Map<String, String> helpDocs = new HashMap<String, String>();
     
     /** Whether there is page framing with default contents. */
     protected boolean framing;
     
     /** Rig client configuration. */
     protected IConfig config;
     
     /** Logger. */
     protected ILogger logger;
     
     public AbstractPage()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.config = ConfigFactory.getInstance();
         this.rig = RigFactory.getRigInstance();
         
         this.buf = new StringBuilder();
         this.framing = true;
     }
     
     /**
      * Method that is called before page generation.
      * 
      * @param req request
      */
     protected void preService(HttpServletRequest req)
     { /* To be optionally overridden with behaviour. */ }
     
     /**
      * Services the request.
      * 
      * @param req request
      * @param resq response
      * @throws IOException 
      */
     public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         this.out = resp.getWriter();
         
         this.preService(req);
         
         resp.setStatus(HttpServletResponse.SC_OK);
         
         if (this.framing)
         {
             this.println("<!DOCTYPE html>");
             this.println("<html>");
             this.addHead();
             this.println("<body onload='initPage()' onresize='resizeFooter()'>");
             this.println("<div id='wrapper'>");
             this.addHeader();
             this.addNavbar();
             this.addActionBar();
             this.println("<div id='content'>");
             this.addPageHeading();
         }
         
         this.contents(req, resp);
 
         if (this.framing)
         {
             this.println("</div>");
             this.addFooter();
             this.println("</div>");
             this.println("</body>");
             this.println("</html>");
         }
         
         resp.getWriter().print(this.buf);
     }
     
     /**
      * Adds the page specific contents.
      * 
      * @param req request 
      * @param resp response
      * @throws IOException 
      */
     public abstract void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException;
     
     /**
      * Adds a line to the output buffer.
      * 
      * @param line output line
      */
     public void println(String line)
     {
         this.buf.append(line);
         this.buf.append('\n');
     }
     
     /**
      * Flushes the output buffer.
      */
     public void flushOut()
     {
         this.out.print(this.buf);
         this.buf = new StringBuilder();
     }
     
     /**
      * Same string transform as the WI.
      * 
      * @param str string to transform
      * @return transformed string
      */
     public String stringTransform(String str)
     {
         StringBuilder b = new StringBuilder();
         String parts[] = str.split("_");
         
         for (int i = 0; i < parts.length; i++)
         {
             b.append(parts[i]);
             if (i != parts.length - 1) b.append(' ');
         }
         
         return b.toString();
     }
     
     /** 
      * Redirects to the specific location
      * 
      * @param resp response
      * @param loc location
      */
     protected void redirect(HttpServletResponse resp, String loc)
     {
         resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
         resp.setHeader("Location", loc);
     }
     
     /**
      * Adds the head section to the page.
      */
     protected void addHead()
     {
         this.println("<head>");
         this.println("  <title>" + this.stringTransform(this.config.getProperty("Rig_Name")) + " - " + this.getPageTitle() + "</title>");
         this.println("  <meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
         this.println("  <link href='/css/rigclient.css' media='screen' rel='stylesheet' type='text/css' />");
         this.println("  <link href='/css/smoothness/jquery-ui.custom.css' rel='stylesheet' type='text/css' />");
         this.println("  <link href='/css/jqtransform.css' rel='stylesheet' type='text/css' />");
         this.println("  <link href='/css/validationEngine.jquery.css' rel='stylesheet' type='text/css' />");
         this.println("  <script type='text/javascript' src='/js/jquery.js'> </script>");
         this.println("  <script type='text/javascript' src='/js/jquery-ui.js'> </script>");
         this.println("  <script type='text/javascript' src='/js/rigclient.js'> </script>");
         this.println("  <script type='text/javascript' src='/js/jquery.jqtransform.js'> </script>");
         this.println("  <script type='text/javascript' src='/js/jquery.validationEngine-en.js'> </script>");
         this.println("  <script type='text/javascript' src='/js/jquery.validationEngine.js'> </script>");
         this.println("</head>");
     }
    
     /**
      * Adds the header to the page.
      */
     protected void addHeader()
     {
         this.println("<div id='header'>");
         this.println("    <div class='headerimg' >");
         this.println("        <a href='http://sourceforge.net/projects/labshare-sahara/'>" +
         		"<img src='/img/logo.png' alt='Sourceforge Project' /></a>");
         this.println("   </div>");
         this.println("   <div class='headerimg' >");
         this.println("        <img src='/img/sahara.png' alt='Sahara Labs' />");
         this.println("    </div>");
         this.println("    <div id='labshareimg'>");
         this.println("        <a href='http://www.labshare.edu.au/'><img src='/img/labshare.png' alt='LabShare' /></a>");
         this.println("    </div>");
         this.println("</div>");   
     }
     
     /**
      * Adds the header to the page.
      */
     protected void addNavbar()
     {
         this.println("<div class='menu ui-corner-bottom'>");
         this.println("   <ol id='menu'>");
         
         /* Navigation bar contents. */
         this.innerNavBar("Main", "/");
         this.innerNavBar("Status", "/status");
         this.innerNavBar("Configuration", "/config");
         this.innerNavBar("Logs", "/logs");
         this.innerNavBar("Diagnostics", "/info");
         this.innerNavBar("Documentation", "/doc");
         
         this.println("   </ol>");
         this.println("   <div id='slide'> </div>");
         this.println("</div>");
         
         /* Add status icon to right hand side of nav bar. */
         this.println("<div id='rigstatusbox'>");
         this.println("  <div style='float:left; margin-top:8px; color:#606060'>Current Status:</div>");
         this.println("  <a id='currentstatusicon' href='/status' class='plaina'>");
         if (!StatusUpdater.isRegistered())
         {
             this.println("      <img src='/img/blue_small.gif' alt='Not registered' />");
         }
         else if (this.rig.isSessionActive())
         {
             this.println("      <img src='/img/yellow_small.gif' alt='In use' />");
         }
         else if (this.rig.isMonitorStatusGood())
         {
             this.println("      <img src='/img/green_small.gif' alt='Online' />");
         }
         else
         {
             this.println("      <img src='/img/red_anime_small.gif' alt='Offline' />");
         }
         this.println("  </a>");
         this.println("</div>");
         
         this.println(
             "<script type='text/javascript'>\n" +
             "    $(document).ready(function() {\n" +
             "        setTimeout(updateNavStatus, 5000);\n" +
             "    });\n" +
             "</script>"
         );
     }
     
     /**
      * Adds a nav bar item to the navigation bar.
      * 
      * @param name name to display
      * @param path path to page
      */
     private void innerNavBar(String name, String path)
     {
         if (this.getPageType().equals(name))
         {
             this.println("<li value='1'>");
         }
         else
         {
             this.println("<li>");
         }
         this.println("  <a class='plaina apad' href='" + path + "'>" + name + "</a>");
         this.println("</li>");
     }
     
     /**
      * Adds the action bar to the page.
      */
     protected void addActionBar()
     {
         this.println("<div id='actionbar'>");
         this.println("  <ul id='actionbarlist'>");
         
         /* Logout. */
         this.println("      <li><a id='logout' class='actiondialogbutton plaina ui-corner-all'>");
         this.println("          <img style='margin-bottom:10px' src='/img/logout.png' alt='Logout' /><br />Logout");
         this.println("      </a></li>");
         
         /* Help. */
         this.println("      <li><a id='help' class='actiondialogbutton plaina ui-corner-all'>");
         this.println("          <img style='margin-bottom:10px' src='/img/help.png' alt='Help' /><br />Help");
         this.println("      </a></li>");
         
         this.println("  </ul>");
         this.println("</div>");
         
         /* Logout action button contents. */
         this.println(
                 "<div id='logoutdialog' title='Logout'>\n" + 
         		"    <div class='ui-priority-primary'>\n" + 
         		"        Are you sure you want to logout?\n" + 
         		"    </div>\n" + 
         		"    <div class='ui-priority-secondary logoutsecondary'>\n" + 
         		"        <span class='ui-icon ui-icon-alert'></span>\n" + 
         		"        Any unsaved changes will be lost.\n" + 
         		"    </div>\n" + 
         		"</div>");
         
         /* Help action button contents. */
         this.println(
                 "<div id='helpdialog' title='Help and Troubleshooting'>\n" + 
         		"   " + this.getPageHelp() +
         		"</div>");
         
         
         this.println("<script type='text/javascript'>");
         this.println("$(document).ready(function() {");
         
         /* Logout action button script. */
         this.println(
                 "$('#logoutdialog').dialog({\n" + 
         		"     autoOpen: false,\n" + 
         		"     width: 400,\n" + 
         		"     modal: true,\n" + 
         		"     resizable: false,\n" + 
         		"     buttons: {\n" +
         		"         'Yes': function() {\n" +
         		"             window.location.replace('/logout');\n" +
         		"         },\n" +
         		"         'No': function() {\n" +
         		"             $(this).dialog('close');\n" +
         		"         }\n" +
         		"     }" +
         		"});\n");
         this.println(
                 "$('#logout').click(function(){\n" + 
         		"     $('#logoutdialog').dialog('open');\n" + 
         		"     return false;\n" + 
         		"});");
         
         /* Help action button script. */
         this.println(
                 "$('#helpdialog').dialog({\n" + 
         		"     autoOpen: false,\n" + 
         		"     width: 650,\n" + 
         		"     modal: true,\n" + 
         		"     resizable: false,\n" + 
         		"     buttons: {\n" +
         		"         'Close' : function() {\n" +
         		"             $(this).dialog('close');\n" +
         		"         }\n" +
         		"     }" +
         		"});\n");
         this.println(
                 "$('#help').click(function(){\n" + 
                 "   $('#helpdialog').dialog('open');\n" + 
                 "   return false;\n" + 
                 "});");
         
         this.println("});");
         this.println("</script>");
     }
     
     /**
      * Adds the page heading 
      */
     protected void addPageHeading()
     {
         this.println("<div class='contentheader'>");
         this.println("  <h2>" + this.stringTransform(this.getPageHeader()) + "</h2>");
         this.println("</div>");
     }
     
     /**
      * Adds the footer to the page.
      */
     protected void addFooter()
     {
         this.println("<div id='footer' class='ui-corner-top'>");
         this.println("   <a class='plaina alrpad' href='http://www.labshare.edu.au'>Labshare Australia</a>");
         this.println("   | <a class='plaina alrpad' href='http://www.uts.edu.au'>&copy; University of Technology, Sydney 2009 - 2010</a>");
         this.println("</div>");
     }
     
     /**
      * Gets the page header.
      * 
      * @return page header
      */
     protected String getPageHeader()
     {
         return this.getPageType();
     }
     
     /**
      * Gets the page title.
      * 
      * @return page title
      */
     protected String getPageTitle()
     {
         return this.getPageType();
     }
     
     /**
      * Gets the page type.
      * 
      * @return page type
      */
     protected abstract String getPageType();
     
     /**
      * Get page help for the action bar help dialog. The help contents are loaded
      * from a HTML file.
      * 
      * @return help contents
      */
     protected String getPageHelp()
     {
         String page = this.getPageType();
         if (AbstractPage.helpDocs.containsKey(page))
         {
             return AbstractPage.helpDocs.get(page);
         }
         
         synchronized (AbstractPage.helpDocs)
         {
             String line;
             StringBuilder helpBuf = new StringBuilder();
             BufferedReader reader = null;
             try
             {
                 InputStream is = AbstractPage.class.getResourceAsStream("/META-INF/web/doc/" + page + ".html");
                 if (is == null)
                 {
                     this.logger.error("Failed to load help document file '/META-INF/web/doc/" + page + ".html' for page '" +
                 		page + "'. The help file was not found.");
                 
                     /* Failed loading so put up an error dialog. */
                     helpBuf.append("<div class='ui-state ui-state-error ui-corner-all' ");
                     helpBuf.append("style='margin:0 auto; width: 260px; padding: 10px'>\n");
                     helpBuf.append("  <span class='ui-icon ui-icon-alert' style='float:left; margin-right: 5px;'></span>\n");
                     helpBuf.append("  Error loading help for this page.\n");
                     helpBuf.append("</div>\n");
                 }
                 else
                 {
                     reader = new BufferedReader(new InputStreamReader(is));    
                     while ((line = reader.readLine()) != null)
                     {
                         helpBuf.append(line);
                         helpBuf.append('\n');
                     }
                 }
             }
             catch (IOException ex)
             {
                 this.logger.error("Failed to load help document file '/META-INF/web/doc/" + page + ".html' for page '" +
                 		page + "'. Exceptiion message: " + ex.getMessage() + '.');
                 
                 /* Failed loading so put up an error dialog. */
                 helpBuf.append("<div class='ui-state ui-state-error ui-corner-all' ");
                 helpBuf.append("style='margin:0 auto; width: 260px; padding: 10px'>\n");
                 helpBuf.append("  <span class='ui-icon ui-icon-alert' style='float:left; margin-right: 5px;'></span>\n");
                 helpBuf.append("  Error loading help for this page.\n");
                 helpBuf.append("</div>\n");
             }
             finally
             {
                 try 
                 {
                     if (reader != null) reader.close();
                 }
                 catch (IOException ex) { /* Not much to do. */ }
             }
 //            FIXME Re-enable help page caching
 //            AbstractPage.helpDocs.put(page, helpBuf.toString());
            return helpBuf.toString();
         }
         
//        return AbstractPage.helpDocs.get(page);
     }
 
     /**
      * Adds a button to the page. 
      * 
      * @param id button identifier
      * @param text display text
      * @param onclick onclick event
      */
     protected void addButton(String id, String text, String onclick)
     {
         this.println("<button id='" + id + "' onclick='" + onclick + "' >" + text + "</button>");
         this.println("<script type='text/javascript'>");
         this.println("$(document).ready(function() { $('#" + id + "').button(); } );");
         this.println("</script>");
     }
 }
