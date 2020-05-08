 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ConsoleNavigationSkin.java,v 1.25 2003-08-19 18:04:01 shahid.shah Exp $
  */
 
 package com.netspective.sparx.theme.console;
 
 import com.netspective.sparx.navigate.NavigationSkin;
 import com.netspective.sparx.navigate.NavigationTree;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationPath;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.navigate.NavigationControllerServlet;
 import com.netspective.sparx.navigate.NavigationPathFlags;
 import com.netspective.sparx.theme.basic.AbstractThemeSkin;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.console.ConsoleServlet;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.io.InheritableFileResources;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 public class ConsoleNavigationSkin extends AbstractThemeSkin implements NavigationSkin
 {
     static public final String HEADING_ACTION_IMAGE = "action-icon";
 
     private int sidebarWidth = 125;
 
     public ConsoleNavigationSkin(Theme theme)
     {
         super(theme);
         setName(ConsoleServlet.CONSOLE_ID);
     }
 
     public int getSidebarWidth()
     {
         return sidebarWidth;
     }
 
     public void setSidebarWidth(int sidebarWidth)
     {
         this.sidebarWidth = sidebarWidth;
     }
 
     public String getInheritableImageUrl(NavigationContext nc, String pageId, String imageId)
     {
         InheritableFileResources resources = getTheme().getResources(nc);
         return (String) resources.getRelativeFileNamesByRelativeResourceName().get(pageId + InheritableFileResources.PATH_SEPARATOR + imageId);
     }
 
     public NavigationContext createContext(javax.servlet.jsp.PageContext jspPageContext, NavigationTree tree, String navTreeId)
     {
         return new NavigationContext(tree,
                 jspPageContext.getServletContext(),
                 (Servlet) jspPageContext.getPage(),
                 jspPageContext.getRequest(),
                 jspPageContext.getResponse(),
                 this, navTreeId);
     }
 
     public NavigationContext createContext(ServletContext context, HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, NavigationTree tree, String navTreeId)
     {
         return new NavigationContext(tree, context, servlet, request, response, this, navTreeId);
     }
 
     public void renderPageMetaData(Writer writer, NavigationContext nc) throws IOException
     {
         NavigationPage activePage = nc.getActivePage();
 
         writer.write("<!-- Application Header Begins -->\n");
         writer.write("<html>\n");
         writer.write("<head>\n");
         writer.write("<title>" + (activePage != null ? nc.getPageTitle() : "") + "</title>\n");
 
         String resourcesUrl = nc.getThemeResourcesRootUrl(getTheme());
         String cssRootUrl = resourcesUrl + "/css";
         String scriptsRootUrl = resourcesUrl + "/scripts";
         String imagesRootUrl = resourcesUrl + "/images";
 
         writer.write("  <LINK REL='SHORTCUT ICON' HREF='"+ imagesRootUrl +"/favicon.ico'>\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/general.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/navigation.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/panel-input.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/panel-output.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/panel-content-dialog.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/panel-content-report.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/panel-content-text.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + cssRootUrl + "/syntax-highlight.css\" type=\"text/css\">\n");
         writer.write("	<link rel=\"stylesheet\" href=\"" + resourcesUrl + "/calendar-0.9.2/calendar-win2k-1.css\" type=\"text/css\">\n");
         writer.write("  <script src=\"" + scriptsRootUrl + "/panel.js\" language=\"JavaScript1.1\"></script>\n");
         writer.write("  <script src=\"" + scriptsRootUrl + "/dialog.js\" language=\"JavaScript1.1\"></script>\n");
 
         writer.write("</head>\n");
     }
 
     /**
      * Render the authenticated user information and the logout navigation link
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderAuthenticatedUser(Writer writer, NavigationContext nc) throws IOException
     {
         AuthenticatedUser authUser = nc.getAuthenticatedUser();
 
         String personName = authUser != null ? authUser.getUserId() : "Not logged in";
         String personId = authUser != null ? authUser.getUserName() : "Not logged in";
 
         if(authUser != null && authUser.isRemembered())
             personName += " (remembered)";
 
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
 
         writer.write("<!-- Active User Begins -->\n");
         writer.write("<table class=\"active-user-table\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("<tr>\n");
         writer.write("	<td><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"10\" border=\"0\"></td>\n");
         writer.write("	<td valign=\"middle\" nowrap >\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + themeImagesPath +
                 "/spacer.gif\" alt=\"\" " +
                 "height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;User&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "/person/summary.jsp?person_id=" + personId + "\">&nbsp;&nbsp;" +
                 personName + "</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
 
         int errorsCount = nc.getProjectComponent().getErrors().size();
         boolean haveErrors = errorsCount > 0;
 
         writer.write("	<td><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
         writer.write(haveErrors ? "	<td>\n" : "	<td width=100%>\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + themeImagesPath +
                 "/spacer.gif\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;App&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getServletContext().getServletContextName() + "\">&nbsp;&nbsp;" +
                 nc.getServletContext().getServletContextName() +" ("+ nc.getServletContext().getServerInfo() +")</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
 
         if(haveErrors)
         {
             writer.write("	<td><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write("	<td width=\"100%\">\n");
 
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"error-alert-anchor\"><img class=\"error-alert-anchor\" src=\"" + themeImagesPath +
                     "/spacer.gif\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
            writer.write("				<td nowrap><a class=\"error-alert\" href=\"" + nc.getServletRootUrl() + "/project/input-source#errors\"><span class=\"error-alert-heading\">&nbsp;Errors&nbsp;</span></a></td>\n");
             writer.write("				<td nowrap><a class=\"error-alert\" href=\"" + nc.getServletRootUrl() + "/project/input-source#errors\">&nbsp;&nbsp;" +
                     errorsCount +"</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
         }
 
         writer.write("	<td nowrap width=\"50\" >\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" +
                 themeImagesPath + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Action&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "/console?_logout=yes\">&nbsp;&nbsp;Logout&nbsp;</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
         writer.write("	<td><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
         writer.write("</tr>\n");
         writer.write("</table>\n");
 
         writer.write("<!-- Active User Ends -->\n");
     }
 
     public void renderPageMasthead(Writer writer, NavigationContext nc) throws IOException
     {
         writer.write("<body leftmargin=\"0\" marginheight=\"0\" marginwidth=\"0\" topmargin=\"0\" onload=\"initializeBody()\">\n");
 
         renderAuthenticatedUser(writer, nc);
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
 
         writer.write("<!-- Master Header Begins -->\n");
 
         writer.write("<table class=\"masthead\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("	<tr>\n");
         writer.write("	<td class=\"masthead-left\" align=\"left\" ><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"65\" width=\"351\" border=\"0\"></td>\n");
         writer.write("	<td class=\"masthead-top-right\" align=\"right\" ><img src=\"" + themeImagesPath + "/spacer.gif\" alt=\"\" height=\"65\" width=\"244\" border=\"0\"></td>\n");
         writer.write("  </tr>\n");
         writer.write("</table>\n");
 
         writer.write("<table class=\"masthead-bottom\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("	<tr>\n");
         writer.write("	    <td valign=\"bottom\">\n");
         writer.write("	        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("<!-- Masthead Ends -->\n");
         writer.write("              <tr>\n");
         writer.write("                  <td align=\"left\" valign=\"bottom\">\n");
     }
 
     /**
      * Render the Level one menu
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageMenusLevelOne(Writer writer, NavigationContext nc) throws IOException
     {
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
 
         writer.write("<!-- Level 1 Begins -->\n");
         NavigationPath activePath = nc.getActivePage();
         String html = generateLevelOneHtml(activePath.getLevel() == 1 ? activePath : (NavigationPath) nc.getActivePage().getAncestorsList().get(1), nc);
         writer.write(html);
         writer.write("<!-- Level 1 Ends -->\n");
 
         writer.write("                    </td>\n");
         writer.write("				</tr>\n");
         writer.write("			</table>\n");
         writer.write("		</td>\n");
         writer.write("		<td class=\"masthead-right\" align=\"right\" valign=\"bottom\" width=\"100%\">" +
                 "<img src=\"" + themeImagesPath + "/spacer.gif\" width=\"100%\" height=\"18\"></td>\n");
         writer.write("	</tr>\n");
         writer.write("</table>\n");
     }
 
     /**
      * Generates the level two HTML
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageMenusLevelTwo(Writer writer, NavigationContext nc) throws IOException
     {
         NavigationPath activePath = nc.getActivePage();
         if (activePath == null)
             return;
 
         switch (activePath.getLevel())
         {
             case 1:
                 List activePathChildren = activePath.getChildrenList();
                 if (activePath.getMaxChildLevel() > 1 && activePathChildren.size() > 0)
                 {
                     writer.write(generateLevelTwoHtml((NavigationPath) activePath.getChildrenList().get(0), nc));
                 }
                 else
                 {
                     // even if there are no level two menu items display the level two background bar
                     writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"20\">");
                     writer.write("	<tr>");
                     writer.write("	<td class=\"menu-level-2-table\" align=\"left\" valign=\"middle\">");
                     writer.write("	</td>");
                     writer.write("	</tr>");
                     writer.write("</table>");
                 }
                 break;
 
             case 2:
                 writer.write(generateLevelTwoHtml(activePath, nc));
                 break;
 
             case 3:
             case 4:
                 writer.write(generateLevelTwoHtml((NavigationPath) activePath.getAncestorsList().get(2), nc));
                 break;
 
         }
         writer.write("<!-- Level Two Ends -->");
     }
 
     /**
      *
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageMenusLevelThree(Writer writer, NavigationContext nc) throws IOException
     {
         NavigationPath activePath = nc.getActivePage();
         if (activePath == null)
             return;
 
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
         switch (activePath.getLevel())
         {
             case 2:
                 List activePathChildren = activePath.getChildrenList();
                 if (activePath.getMaxChildLevel() > 2 && activePathChildren.size() > 0)
                 {
                     writer.write("  <tr>\n");
                     writer.write("      <td class=\"menu-level-3-separator\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\">" +
                             "<img src=\"" + themeImagesPath + "/spacer-big.gif\" alt=\"\" width=\"" + sidebarWidth + "\" height=\"12\" border=\"0\"></td>\n");
                     writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\"" + themeImagesPath + "/spacer-big.gif\" " +
                             "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                     writer.write("      <td align=\"left\" valign=\"top\"><img src=\"" + themeImagesPath +
                             "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                     writer.write("  </tr>\n");
                     writer.write("  <tr>\n");
                     writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\" height=\"100%\">\n");
                     writer.write(generateLevelThreeHtml((NavigationPath) activePath.getChildrenList().get(0), nc));
                     writer.write("      </td>\n");
                 }
                 break;
 
             case 3:
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-level-3-separator\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\">" +
                         "<img src=\"" + themeImagesPath + "/spacer-big.gif\" alt=\"\" width=\"" + sidebarWidth + "\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\"" + themeImagesPath + "/spacer-big.gif\" " +
                         "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td align=\"left\" valign=\"top\"><img src=\"" + themeImagesPath +
                         "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("  </tr>\n");
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\" height=\"100%\">\n");
                 writer.write(generateLevelThreeHtml(activePath, nc));
                 writer.write("      </td>\n");
                 break;
 
             case 4:
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-level-3-separator\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\">" +
                         "<img src=\"" + themeImagesPath + "/spacer-big.gif\" alt=\"\" width=\"" + sidebarWidth + "\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\"" + themeImagesPath + "/spacer-big.gif\" " +
                         "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td align=\"left\" valign=\"top\"><img src=\"" + themeImagesPath +
                         "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("  </tr>\n");
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"" + sidebarWidth + "\" height=\"100%\">\n");
                 writer.write(generateLevelThreeHtml((NavigationPath) activePath.getAncestorsList().get(3), nc));
                 writer.write("      </td>\n");
                 break;
 
             default:
                 writer.write("  <tr>\n");
                 writer.write("      <td colspan=\"3\"><img src=\"" + themeImagesPath + "/spacer-big.gif\" " +
                         "alt=\"\" width=\"100%\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("  </tr>\n");
                 writer.write("  <tr>\n");
                 break;
 
         }
     }
 
     /**
      *
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageHeader(Writer writer, NavigationContext nc) throws IOException
     {
         if (nc.getActiveState().getFlags().flagIsSet(NavigationPage.Flags.IS_POPUP_MODE))
             return;
 
         renderPageMasthead(writer, nc);
         renderPageMenusLevelOne(writer, nc);
         renderPageMenusLevelTwo(writer, nc);
         renderPageHeading(writer, nc);
 
         writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"100%\">\n");
 
         renderPageMenusLevelThree(writer, nc);
 
         writer.write("      <td align=\"left\" valign=\"top\" width=\"12\" height=\"100%\"></td>");
         writer.write("      <td align=\"left\" valign=\"top\">");
         writer.write("          <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         writer.write("              <tr>");
         writer.write("                  <td align=\"left\" valign=\"top\">");
         writer.write("                  <div align=\"left\" style=\"padding-top: 6\">");
     }
 
     /**
      * Renders the page heading if one exists
      * @param writer
      * @param nc
      * @throws IOException
      */
     private void renderPageHeading(Writer writer, NavigationContext nc) throws IOException
     {
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
 
         NavigationPage page = nc.getActivePage();
         String heading = page != null ? nc.getPageHeading() : "No active page";
 
         String actionIcon = page != null ? getInheritableImageUrl(nc, page.getQualifiedName(), HEADING_ACTION_IMAGE) : null;
 
         writer.write("<!-- Page Header Begins -->\n");
         writer.write("<table class=\"page-heading-table\" height=\"36\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("    <tr>\n");
         writer.write("        <td align=\"left\" valign=\"middle\">\n");
         writer.write("            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("                <tr>\n");
         if (actionIcon != null && actionIcon.length() > 0)
             writer.write("                    <td align=\"right\" class=\"page-heading-icon\"><img class=\"page-icon\" src=\"" +
                     nc.getRootUrl() + actionIcon + "\" alt=\"\" height=\"22\" width=\"22\" border=\"0\"></td>\n");
         else
             writer.write("                    <td align=\"right\" class=\"page-heading-icon\"><img class=\"page-icon\" src=\"" +
                     themeImagesPath + "/page-icons/home.gif\" alt=\"\" height=\"22\" width=\"22\" border=\"0\"></td>\n");
 
         writer.write("                    <td class=\"page-heading\">" + heading + "</td>\n");
         writer.write("                </tr>\n");
         writer.write("            </table>\n");
         writer.write("        </td>\n");
         writer.write("    </tr>\n");
         renderPageSubHeading(writer, nc);
         writer.write("</table>\n");
         writer.write("<!-- Page Header Ends -->\n");
     }
 
     /**
      * Render the sub heading in the page content
      * @param writer
      * @param nc
      * @throws IOException
      */
     private void renderPageSubHeading(Writer writer, NavigationContext nc) throws IOException
     {
         NavigationPage page = nc.getActivePage();
         if (page == null)
             return;
 
         String subHeading = nc.getPageSubheading();
         if (subHeading != null && subHeading.length() > 0)
         {
             writer.write("    <tr>\n");
             writer.write("        <td class=\"page-sub-heading-table\" align=\"left\" valign=\"top\">\n");
 
             writer.write("            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("                <tr>\n");
             writer.write("                    <td align=\"left\" valign=\"top\">\n");
             writer.write("                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("                            <tr>\n");
             writer.write("                                <td align=\"left\" valign=\"middle\">\n");
             writer.write("                                    <p><span class=\"page-sub-heading\">" + subHeading + "</p>\n");
             writer.write("                                </td>\n");
             writer.write("                            </tr>\n");
             writer.write("                        </table>\n");
             writer.write("                    </td>\n");
             writer.write("                </tr>\n");
             writer.write("            </table>\n");
             writer.write("        </td>\n");
             writer.write("    </tr>\n");
         }
     }
 
     /**
      * Render the page content footer
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageFooter(Writer writer, NavigationContext nc) throws IOException
     {
         NavigationPathFlags flags = nc.getActiveState().getFlags();
         if (flags.flagIsSet(NavigationPage.Flags.SHOW_RENDER_TIME))
         {
             Long startTime = (Long) nc.getRequest().getAttribute(NavigationControllerServlet.REQATTRNAME_RENDER_START_TIME);
             writer.write("<p align=right>Render time: " + (startTime != null ? (Long.toString((System.currentTimeMillis() - startTime.longValue())) + " milliseconds&nbsp;&nbsp;") : "unknown&nbsp;&nbsp;"));
         }
 
         if (flags.flagIsSet(NavigationPage.Flags.IS_POPUP_MODE))
             return;
 
         writer.write("            </td>");
         writer.write("          </tr>");
         writer.write("	    </table>");
         writer.write("		</td>");
         writer.write("	</tr>");
         writer.write("</table>");
         writer.write("</body>");
     }
 
     /**
      * Generates the HTML for the Level one navigation
      * @param currentNavTree
      * @param nc
      * @throws IOException
      */
     protected String generateLevelOneHtml(NavigationPath currentNavTree, NavigationContext nc) throws IOException
     {
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
         StringBuffer buffer = new StringBuffer();
         List tabElements = currentNavTree.getSibilingList();
 
         if (tabElements == null || tabElements.isEmpty())
         {
             return "";
         }
         buffer.append("            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n");
         buffer.append("                <tr>\n");
         buffer.append("                <td class=\"menu-level-1-start\" valign=\"bottom\" nowrap><img src=\"" +
                 themeImagesPath + "/login/spacer.gif\" width=\"10\" height=\"10\"></td>");
         for (int i = 0; i < tabElements.size(); i++)
         {
             NavigationPage tabElement = (NavigationPage) tabElements.get(i);
             if (!nc.getState(tabElement).getFlags().flagIsSet(NavigationPage.Flags.HIDDEN))
             {
                 if (i == 0)
                 {
                     if (tabElement.isInActivePath(nc))
                     {
                         buffer.append("                <td class=\"menu-level-1-active-start\" valign=\"bottom\" nowrap>" +
                                 "<span class=\"menu-level-1\">&nbsp;&nbsp;&nbsp;&nbsp;</span></td>");
                         buffer.append("                    <td class=\"menu-level-1-table-active\" valign=\"bottom\" nowrap>");
                         buffer.append("<a class=\"menu-level-1-active\"" +
                                 " href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;&nbsp;&nbsp;</a></td>\n");
                     }
                     else
                     {
                         buffer.append("                <td class=\"menu-level-1-table-start\" valign=\"bottom\" nowrap>" +
                                 "<span class=\"menu-level-1\">&nbsp;&nbsp;&nbsp;</span></td>");
                         buffer.append("                    <td class=\"menu-level-1-table\" valign=\"bottom\" nowrap>");
                         buffer.append("<a class=\"menu-level-1\"" +
                                 " href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;&nbsp;</a></td>\n");
                     }
                 }
                 else
                 {
                     if (tabElement.isInActivePath(nc))
                     {
                         buffer.append("                    <td class=\"menu-level-1-table-active-end\" valign=\"bottom\" nowrap>");
                         buffer.append("<a class=\"menu-level-1-active\"" +
                                 " href=\"" + tabElement.getUrl(nc) + "\">&nbsp;&nbsp;&nbsp;&nbsp;</a></td>\n");
 
                         buffer.append("                    <td class=\"menu-level-1-table-active\" valign=\"bottom\" nowrap>");
                         buffer.append("<a class=\"menu-level-1-active\"" +
                                 " href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;&nbsp;&nbsp;</a></td>\n");
                     }
                     else
                     {
                         buffer.append("                    <td class=\"menu-level-1-table-end\" valign=\"bottom\" nowrap><span class=\"menu-level-1\">" +
                                 "&nbsp;&nbsp;&nbsp;</span></td>\n");
 
                         buffer.append("                    <td class=\"menu-level-1-table\" valign=\"bottom\" nowrap>");
                         buffer.append("<a class=\"menu-level-1\"" +
                                 " href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;&nbsp;</a></td>\n");
                     }
                 }
             }
         }
         buffer.append("              <td class=\"menu-level-1-end\" valign=\"bottom\" nowrap><span>&nbsp;&nbsp;</span></td>");
         buffer.append("              <td class=\"menu-level-1-fill\" width=\"100%\" valign=\"bottom\" nowrap>" +
                 "<span>&nbsp;&nbsp;</span></td>");
         buffer.append("               </tr>\n");
         buffer.append("           </table>\n");
 
         return buffer.toString();
     }
 
     /**
      * Generates the HTML for the level two navigation level
      * @param currentNavTree
      * @param nc
      * @return
      * @throws IOException
      */
     protected String generateLevelTwoHtml(NavigationPath currentNavTree, NavigationContext nc) throws IOException
     {
         StringBuffer writer = new StringBuffer();
         List tabElements = currentNavTree.getSibilingList();
 
         if (tabElements == null || tabElements.isEmpty())
         {
             return "";
         }
 
         writer.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" height=\"20\">\n");
         writer.append("<tr>\n");
         writer.append("	<td class=\"menu-level-2-table\" align=\"left\" valign=\"middle\">\n");
         writer.append("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.append("			<tr>\n");
         writer.append("			    <td align=\"center\" nowrap><a class=\"menu-level-2\">&nbsp;&nbsp;</a></td>\n");
         int size = tabElements.size();
         for (int i = 0; i < size; i++)
         {
             NavigationPage tabElement = (NavigationPage) tabElements.get(i);
 
             if (!nc.getState(tabElement).getFlags().flagIsSet(NavigationPage.Flags.HIDDEN))
             {
                 if (tabElement.isInActivePath(nc))
                 {
                     writer.append("                    <td class=\"menu-level-2-table-active-end\" valign=\"bottom\" nowrap>");
                     writer.append("<a class=\"menu-level-2-active\"" +
                             " href=\"" + tabElement.getUrl(nc) + "\">&nbsp;&nbsp;</a></td>\n");
 
                     writer.append("                    <td class=\"menu-level-2-table-active\" valign=\"bottom\" nowrap>");
                     writer.append("<a class=\"menu-level-2-active\"" +
                             " href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></td>\n");
                 }
                 else
                 {
                     writer.append("<td nowrap align=\"center\" " + (i != size - 1 ? "class=\"menu-level-2-separator\"" : "") + ">");
                     writer.append("<a class=\"menu-level-2" +
                             "\" href=\"" + tabElement.getUrl(nc) + "\">&nbsp;&nbsp;" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></TD>\n");
                 }
             }
         }
         writer.append("            </tr>\n");
         writer.append("        </table>\n");
         writer.append("    </td>\n");
         writer.append("</tr>\n");
         writer.append("</table>\n");
         return writer.toString();
     }
 
     /**
      * Generates the html for the third navigation level
      * @param currentNavTree
      * @param nc
      * @return
      * @throws IOException
      */
     protected String generateLevelThreeHtml(NavigationPath currentNavTree, NavigationContext nc) throws IOException
     {
         String themeImagesPath = nc.getThemeImagesRootUrl(getTheme());
 
         StringBuffer writer = new StringBuffer();
         List sideBarElements = currentNavTree.getSibilingList();
         if (sideBarElements == null || sideBarElements.isEmpty())
         {
             return "";
         }
 
         writer.append("      <!-- Level 3 Begins -->\n");
         writer.append("      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"100%\" >\n");
 
         for (int i = 0; i < sideBarElements.size(); i++)
         {
             NavigationPage sideBarElement = (NavigationPage) sideBarElements.get(i);
             if (!nc.getState(sideBarElement).getFlags().flagIsSet(NavigationPage.Flags.HIDDEN))
             {
                 if (sideBarElement.isInActivePath(nc))
                 {
                     writer.append("      <tr>\n");
                     writer.append("      	<td>\n");
                     writer.append("      		<table width=\"" + sidebarWidth + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" >\n");
                     writer.append("     		 <tr>\n");
                     writer.append("                    <td class=\"menu-level-3-table-active-end\" valign=\"bottom\" nowrap>");
                     writer.append("<a class=\"menu-level-3-active\"" +
                             " href=\"" + sideBarElement.getUrl(nc) + "\"></a></td>\n");
 
                     writer.append("                    <td class=\"menu-level-3-table-active\" valign=\"bottom\" nowrap>");
                     writer.append("<a class=\"menu-level-3-active\"" +
                             " href=\"" + sideBarElement.getUrl(nc) + "\">" + sideBarElement.getCaption(nc) + "&nbsp;</a></td>\n");
                     writer.append("      		</tr>\n");
                     writer.append("      		</table>\n");
                     writer.append("     	</td>\n");
                     writer.append("      </tr>\n");
                 }
                 else
                 {
                     writer.append("      <tr>\n");
                     writer.append("          <td class=\"menu-level-3-table\" align=\"left\" valign=\"middle\">" +
                             "<a class=\"menu-level-3\" href=\"" + sideBarElement.getUrl(nc) + "\"><nobr>" +
                             sideBarElement.getCaption(nc) + "</nobr></a></td>\n");
                     writer.append("      </tr>\n");
                 }
             }
         }
         writer.append("          <tr height=\"100%\">\n");
         writer.append("              <td class=\"menu-table-end\" align=\"left\" valign=\"top\" height=\"100%\">" +
                 "<img src=\"" + themeImagesPath + "/spacer-big.gif\" height=\"100%\" width=\"100%\"></td>\n");
         writer.append("          </tr>\n");
         writer.append("      </table>\n");
         writer.append("      <!-- Level 3 Ends -->\n");
         return writer.toString();
     }
 }
