 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
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
  * @date 19th August 2011
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.server.impl.PageHostingServiceListener;
 
 /**
  * Class that is extended for each of the embedded server pages.
  */
 public abstract class AbstractPage
 {
     /** The output buffer. */
     protected StringBuilder buf;
     
     /** Servlet writer. */
     protected PrintWriter out;
     
     /** The list of CSS files to include. */
     protected final List<String> headCss = Collections.synchronizedList(new ArrayList<String>());
     
     /** The list of Javascript files to include. */
     protected final List<String> headJs = Collections.synchronizedList(new ArrayList<String>());
     
     /** The list of navigation links to add. */
     private static final Map<String, String> navLinks = Collections.synchronizedMap(new LinkedHashMap<String, String>());
     static 
     {
         AbstractPage.addNavLink("Main", "/");
     }
     
     /** Whether there is page framing with default contents. */
     protected boolean framing;
     
     /** Rig client configuration. */
     protected Config config;
     
     /** Logger. */
     protected Logger logger;
     
     public AbstractPage()
     {
         this.logger = LoggerActivator.getLogger();
         this.config = ServerActivator.getConfig();
         
         this.buf = new StringBuilder();
         this.framing = true;
         
         this.headCss.add("/css/schedulingserver.css");
         this.headCss.add("/css/smoothness/jquery-ui.custom.css");
        this.headCss.add("/css/jqtransform.css");
         this.headCss.add("/css/validationEngine.jquery.css");
         
         this.headJs.add("/js/jquery.js");
         this.headJs.add("/js/jquery-ui.js");
        this.headJs.add("/js/jquery.jqtransform.js");
         this.headJs.add("/js/jquery.validationEngine-en.js");
         this.headJs.add("/js/jquery.validationEngine.js");
         this.headJs.add("/js/schedulingserver.js");
     }
     
     /**
      * Method that is called before page generation.
      * 
      * @param req request
      */
     protected void preService(HttpServletRequest req)
     { /* To be optionally overridden with behavior. */ }
     
     /**
      * Services the request.
      * 
      * @param req request
      * @param resp response
      * @throws IOException 
      */
     public final void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
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
         
         try
         {
             this.contents(req, resp);
             
             if (this.framing)
             {
                 this.println("</div>");
                 this.addFooter();
                 this.println("</div>");
                 this.println("</body>");
                 this.println("</html>");
             }
         }
         finally
         {
             this.postService(resp);
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
      * Method that is called after service.
      * 
      * @param resp servlet response
      */
     protected void postService(HttpServletResponse resp)
     { /* To be optionally overwritten with behavior. */ }
     
     /**
      * Adds a line to the output buffer.
      * 
      * @param line output line
      */
     public final void println(String line)
     {
         this.buf.append(line);
         this.buf.append('\n');
     }
     
     /**
      * Flushes the output buffer.
      */
     public final void flushOut()
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
     public final String stringTransform(String str)
     {
         if (str == null) return str;
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
     protected final void redirect(HttpServletResponse resp, String loc)
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
         this.println("  <title>Scheduling Server - " + this.getPageTitle() + "</title>");
         
         for (String css : this.headCss)
         {
             this.println("  <link href='" + css + "' media='screen' rel='stylesheet' type='text/css' />");
         }
         
         for (String js : this.headJs)
         {
             this.println("  <script type='text/javascript' src='" + js + "'> </script>");
         }
 
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
         for (Entry<String, String> nav : navLinks.entrySet()) this.innerNavBar(nav.getKey(), nav.getValue());
         
         this.println("   </ol>");
         this.println("   <div id='slide'> </div>");
         this.println("</div>");
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
         		"     height: 450,\n" +
         		"     modal: false,\n" + 
         		"     resizable: true,\n" + 
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
         this.println("<a class='plaina' href='http://sourceforge.net/projects/labshare-sahara/' target='_blank'>" +
                 "<img src='/img/logo_small.png' alt='Logo' />" +
         		"Powered by the open source <strong>SAHARA Labs r3.2</strong> system.</a>");
         this.println(
                 "<div>" +
                     "<a class='plaina' href='http://www.feit.uts.edu.au/facilities/remote-lab/index.html' target='_blank'>" +
                     "&copy; UTS 2009-2011</a>" +
                 "</div>");
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
         String line, page = this.getPageType();
         StringBuilder helpBuf = new StringBuilder();
         BufferedReader reader = null;
         
         try
         {
             InputStream is = AbstractPage.class.getResourceAsStream("/META-INF/web/doc/" + page + ".html");
             if (is == null)
             {
                 /* Attempt to load the help file from a page hosting bundle. */
                 for (ClassLoader cl : PageHostingServiceListener.getClassLoaders())
                 {
                     is = cl.getResourceAsStream("/META-INF/web/doc/" + page + ".html");
                     if (is != null) break;
                 }
             }
             
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
         
         return helpBuf.toString();
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
     
     /**
      * Adds a navigation link.
      * 
      * @param name name of link
      * @param link actual link.
      */
     public static void addNavLink(String name, String link)
     {
         AbstractPage.navLinks.remove("Diagnostics");
 //        AbstractPage.navLinks.remove("Documentation");
         AbstractPage.navLinks.remove("About");
         
         AbstractPage.navLinks.put(name, link);
         
         navLinks.put("Diagnostics", "/info");
 //        navLinks.put("Documentation", "/doc");
         navLinks.put("About", "/about");
     }
     
     /**
      * Removes a navigation link. 
      * 
      * @param name name of link to remove
      */
     public static void removeNavLink(String name)
     {
         AbstractPage.navLinks.remove(name);
     }
 }
