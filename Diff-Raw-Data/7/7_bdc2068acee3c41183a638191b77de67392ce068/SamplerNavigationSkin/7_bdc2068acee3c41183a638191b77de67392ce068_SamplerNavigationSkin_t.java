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
 * $Id: SamplerNavigationSkin.java,v 1.2 2004-06-28 04:10:59 shahid.shah Exp $
  */
 
 package com.netspective.sparx.theme.sampler;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.netspective.commons.security.AuthenticatedOrgUser;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.sparx.ProjectComponent;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.theme.console.ConsoleNavigationSkin;
 
 public class SamplerNavigationSkin extends ConsoleNavigationSkin
 {
     public SamplerNavigationSkin(Theme theme, String name)
     {
         super(theme, name);
     }
 
     /**
      * Render the authenticated user information and the logout navigation link
      * @param writer
      * @param nc
      * @throws java.io.IOException
      */
     public void renderAuthenticatedUser(Writer writer, NavigationContext nc) throws IOException
     {
         AuthenticatedUser authUser = nc.getAuthenticatedUser();
 
         String personId = authUser != null ? authUser.getUserId() : "Not logged in";
         String personName = authUser != null ? authUser.getUserName() : "Not logged in";
         if(authUser != null && authUser.isRemembered())
             personName += " (remembered)";
 
         String orgName = null;
         String orgId = null;
         if (authUser instanceof AuthenticatedOrgUser)
         {
            orgId = ((AuthenticatedOrgUser) authUser).getUserOrgId();
            orgName = ((AuthenticatedOrgUser) authUser).getUserOrgName();
         }
 
         Theme theme = getTheme();
 
         writer.write("<!-- Active User Begins -->\n");
         writer.write("<table class=\"active-user-table\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("<tr>\n");
         writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"10\" border=\"0\"></td>\n");
         writer.write("	<td valign=\"middle\" nowrap >\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" " +
                 "height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;User&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"#\" title=\"User ID is '"+ personId +"'\">&nbsp;&nbsp;" +
                 personName + "</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
 
         ProjectComponent projectManager = nc.getProjectComponent();
         boolean haveErrors = projectManager.hasErrorsOrWarnings();
 
         if(orgName != null && ! orgName.equals(personName))
         {
             writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write(haveErrors ? "	<td>\n" : "	<td width=100%>\n");
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Org&nbsp;</span></td>\n");
             writer.write("				<td nowrap><a class=\"active-user\" href=\"#\" title=\"Org ID is '"+ orgId +"'\">&nbsp;&nbsp;" + orgName +"</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
         }
 
         if(haveErrors)
         {
             int errorsCount = projectManager.getErrors().size() + projectManager.getWarnings().size();
 
             writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write("	<td width=\"100%\">\n");
 
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"error-alert-anchor\"><img class=\"error-alert-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><a class=\"error-alert\" href=\"" + nc.getServletRootUrl() + "/project/input-source#errors\"><span class=\"error-alert-heading\">&nbsp;Errors/Warnings&nbsp;</span></a></td>\n");
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
                 theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Action&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "?_logout=yes\">&nbsp;&nbsp;Logout&nbsp;</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
         writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
         writer.write("</tr>\n");
         writer.write("</table>\n");
 
         writer.write("<!-- Active User Ends -->\n");
     }
 }
