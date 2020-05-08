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
 * $Id: PageTag.java,v 1.13 2003-02-03 00:50:30 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.taglib;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 import com.netspective.sparx.xaf.html.ComponentCommandException;
 import com.netspective.sparx.xaf.html.ComponentCommandFactory;
 import com.netspective.sparx.xaf.security.LoginDialog;
 import com.netspective.sparx.xaf.security.AuthenticatedUser;
 import com.netspective.sparx.xaf.security.AccessControlList;
 import com.netspective.sparx.xaf.security.AccessControlListFactory;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.skin.HtmlTabbedNavigationSkin;
 import com.netspective.sparx.xaf.navigate.*;
 import com.netspective.sparx.util.value.ServletValueContext;
 import com.netspective.sparx.util.value.ValueContext;
 import com.netspective.sparx.util.config.Configuration;
 import com.netspective.sparx.util.config.ConfigurationManagerFactory;
 import com.netspective.sparx.util.log.LogManager;
 import com.netspective.sparx.Globals;
 import com.netspective.sparx.BuildConfiguration;
 
 public class PageTag extends javax.servlet.jsp.tagext.TagSupport
 {
    public static final String ATTRNAME_ACTIVENAVGPAGE = "active-navigation-page";
     public static final String ATTRNAME_SKIPPEDBODY = "skipped-body";
     public static final String ATTRVALUE_YES = "yes";
     public static final String ATTRVALUE_NO = "no";
     static public final String PAGE_SECURITY_MESSAGE_ATTRNAME = "security-message";
     static public final String PAGE_LOGIN_DIALOG_CLASS_CONFIG_PROP_NAME = "com.netspective.sparx.xaf.taglib.PageTag.login-dialog-class";
     static public final String PAGE_LOGIN_DIALOG_SKIN_CONFIG_PROP_NAME = "com.netspective.sparx.xaf.taglib.PageTag.login-dialog-skin";
     static public final String PAGE_DEFAULT_LOGIN_DIALOG_CLASS = "com.netspective.sparx.xaf.security.LoginDialog";
 
     static private LoginDialog loginDialog;
     static private NavigationPathSkin navSkin;
 
     private String[] permissions;
     private boolean popup;
     private boolean secure = true;
     private long startTime;
     private String navSkinName;
     private String navId;
     private NavigationPage activePage;
     private NavigationPathContext nc;
 
     public void release()
     {
         super.release();
         permissions = null;
         popup = false;
         secure = true;
         nc = null;
         navSkinName = null;
         navId = null;
     }
 
     public final boolean isPopup()
     {
         return popup;
     }
 
     public final boolean secure()
     {
         return secure;
     }
 
     public void setPopup(boolean value)
     {
         popup = value;
     }
 
     public void setPopup(String value)
     {
         setPopup(value == null ? false : (value.equals(ATTRVALUE_YES) ? true : false));
     }
 
     public void setSecure(boolean value)
     {
         secure = value;
     }
 
     public void setSecure(String value)
     {
         setSecure(value == null ? true : (value.equals(ATTRVALUE_NO) ? false : true));
     }
 
     public void setNavSkin(String value)
     {
         navSkinName = value;
     }
 
     public void setNavId(String navId)
     {
         this.navId = navId;
     }
 
     public final String[] getPermissions()
     {
         return permissions;
     }
 
     public void setPermission(String value)
     {
         if(value == null || value.length() == 0)
             return;
 
         java.util.List perms = new java.util.ArrayList();
         java.util.StringTokenizer st = new java.util.StringTokenizer(value, ",");
         while(st.hasMoreTokens())
         {
             perms.add(st.nextToken());
         }
         permissions = (String[]) perms.toArray(new String[perms.size()]);
     }
 
     public String getLoginDialogClassName()
     {
         return PAGE_DEFAULT_LOGIN_DIALOG_CLASS;
     }
 
     public String getLoginDialogSkinName()
     {
         return null; // the "default" skin
     }
 
     protected boolean doLogin(javax.servlet.ServletContext servletContext, javax.servlet.Servlet page, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws java.io.IOException, JspException
     {
         ValueContext vc = null;
         if(loginDialog == null)
         {
             vc = new ServletValueContext(servletContext, page, req, resp);
             Configuration config = ConfigurationManagerFactory.getDefaultConfiguration(servletContext);
             String className = config.getTextValue(vc, PAGE_LOGIN_DIALOG_CLASS_CONFIG_PROP_NAME, getLoginDialogClassName());
             try
             {
                 Class loginDialogClass = Class.forName(className);
                 loginDialog = (LoginDialog) loginDialogClass.newInstance();
             }
             catch(ClassNotFoundException e)
             {
                 throw new javax.servlet.jsp.JspException("Login dialog class '" + className + "' not found in classpath.");
             }
             catch(IllegalAccessException e)
             {
                 throw new javax.servlet.jsp.JspException("Unable to access login dialog class '" + className + "'.");
             }
             catch(InstantiationException e)
             {
                 throw new javax.servlet.jsp.JspException("Unable to instantiate login dialog class '" + className + "'.");
             }
             loginDialog.initialize();
         }
 
         String logout = req.getParameter("_logout");
         if(logout != null)
         {
             if(vc == null) vc = new ServletValueContext(servletContext, page, req, resp);
             loginDialog.logout(vc);
 
             /** If the logout parameter included a non-zero length value, then
              *  we'll redirect to the value provided.
              */
             if(logout.length() == 0 || logout.equals("1") || logout.equals(ATTRVALUE_YES))
                 resp.sendRedirect(req.getContextPath());
             else
                 resp.sendRedirect(logout);
             return true;
         }
 
         if(!loginDialog.accessAllowed(servletContext, req, resp))
         {
             String skinName = getLoginDialogSkinName();
             DialogContext dc = loginDialog.createContext(servletContext, page, req, resp, skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName));
             loginDialog.prepareContext(dc);
             if(dc.inExecuteMode())
             {
                 loginDialog.execute(dc);
             }
             else
             {
                 loginDialog.producePage(resp.getWriter(), dc);
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean hasPermission()
     {
         if(permissions == null)
             return true;
 
         HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
 
         AuthenticatedUser user = (AuthenticatedUser) request.getSession(true).getAttribute("authenticated-user");
         if(user == null)
         {
             request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "No user identified.");
             return false;
         }
 
         AccessControlList acl = AccessControlListFactory.getACL(pageContext.getServletContext());
         if(acl == null)
         {
             request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "No ACL defined.");
             return false;
         }
 
         if(!user.hasAnyPermission(acl, permissions))
         {
             request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "Permission denied.");
             return false;
         }
 
         return true;
     }
 
     public boolean handleDefaultBodyItem() throws javax.servlet.jsp.JspException
     {
         try
         {
             return ComponentCommandFactory.handleDefaultBodyItem(
                     pageContext.getServletContext(),
                     (Servlet) pageContext.getPage(),
                     pageContext.getRequest(), pageContext.getResponse()
                     );
         }
         catch(IOException e)
         {
             throw new JspException(e);
         }
         catch(ComponentCommandException e)
         {
             throw new JspException(e);
         }
     }
 
     public int doStartTag() throws javax.servlet.jsp.JspException
     {
         doPageBegin();
 
         JspWriter out = pageContext.getOut();
 
         HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
         HttpServletResponse resp = (HttpServletResponse) pageContext.getResponse();
         ServletContext servletContext = pageContext.getServletContext();
 
         try
         {
             if (secure && doLogin(servletContext, (Servlet) pageContext.getPage(), req, resp))
             {
                 req.setAttribute(ATTRNAME_SKIPPEDBODY, ATTRVALUE_YES);
                 return SKIP_BODY;
             }
 
             if (secure && !hasPermission())
             {
                 req.setAttribute(ATTRNAME_SKIPPEDBODY, ATTRVALUE_YES);
                 out.print(req.getAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME));
                 return SKIP_BODY;
             }
 
             NavigationTree appNavigationTree = NavigationTreeManagerFactory.getNavigationTree(servletContext);
             if(appNavigationTree != null)
             {
                 if(navSkin == null)
                 {
                     navSkin = navSkinName != null ? SkinFactory.getNavigationSkin(navSkinName) : SkinFactory.getNavigationSkin();
                     if(navSkin == null)
                     {
                         out.print("<i><b>Navigation skin '"+ navSkinName +"' not found, please pass a valid navigation skin name using 'navSkin' attribute.</b></i><p>");
                         if (handleDefaultBodyItem())
                             return SKIP_BODY;
                         else
                             return EVAL_BODY_INCLUDE;
                     }
                 }
 
                 String navId = req.getPathInfo();
                 nc = navSkin.createContext(pageContext, appNavigationTree, navId == null ? this.navId : navId, popup);
 
                 activePage = (NavigationPage) nc.getActivePath();
                 if(activePage != null)
                 {
                    req.setAttribute(ATTRNAME_ACTIVENAVGPAGE, activePage);
                     if(nc.flagIsSet(activePage.getId(),NavigationPath.NAVGPATHFLAG_INVISIBLE))
                     {
                         req.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "The Page requested could not be Displayed.");
                         //TODO: Need to dress this message some more with HTML.
                         req.setAttribute(ATTRNAME_SKIPPEDBODY, ATTRVALUE_YES);
                         out.print(req.getAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME));
                         return SKIP_BODY;
                     }
                     activePage.handlePageMetaData(out, nc);
                     activePage.handlePageHeader(out, nc);
                 } else {
                     req.setAttribute(ATTRNAME_SKIPPEDBODY, ATTRVALUE_YES);
                    out.print("The page '"+ (navId == null ? this.navId : navId) +"' was not found.");
                     return SKIP_BODY;
                 }
             }
             else
                 out.print("NavigationTree navTree is null.");
         }
         catch (Exception e)
         {
             StringWriter stack = new StringWriter();
             e.printStackTrace(new PrintWriter(stack));
             throw new JspException(e.toString() + stack.toString());
         }
 
         if (handleDefaultBodyItem())
             return SKIP_BODY;
         else
             return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws javax.servlet.jsp.JspException
     {
         HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
         if(req.getAttribute(ATTRNAME_SKIPPEDBODY) == null && activePage != null)
         {
             try
             {
                 activePage.handlePageFooter(pageContext.getOut(), nc);
             }
             catch (Exception e)
             {
                 throw new JspException(e.toString());
             }
         }
 
         doPageEnd();
         return EVAL_PAGE;
     }
 
     /**
      * Records the start time when the page is loaded
      */
     public void doPageBegin()
     {
         startTime = new java.util.Date().getTime();
         HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
         org.apache.log4j.NDC.push(request.getSession(true).getId());
     }
 
     /**
      * Records the total time when the page is finished loading
      */
     public void doPageEnd()
     {
         javax.servlet.http.HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
         LogManager.recordAccess(request, null, pageContext.getPage().getClass().getName(), request.getRequestURI(), startTime);
         org.apache.log4j.NDC.pop();
     }
 
     /**
      * Used by Sparx sample applications to show the shell
      */
     public void doSamplePageBegin(String styleSheet) throws IOException
     {
         Configuration config = ConfigurationManagerFactory.getDefaultConfiguration(pageContext.getServletContext());
         ServletValueContext svc = new ServletValueContext(pageContext.getServletContext(), (Servlet) pageContext.getPage(), pageContext.getRequest(), pageContext.getResponse());
 
         String sparxACEUrl = config.getTextValue(svc, "sparx.ace.root-url");
         String sparxSampleImagesUrl = config.getTextValue(svc, Globals.SHARED_CONFIG_ITEMS_PREFIX + "images-url") + "/samples";
 
         javax.servlet.jsp.JspWriter out = pageContext.getOut();
         out.println("<html>");
         out.println("	<head>");
         out.println("		<meta http-equiv='content-type' content='text/html;charset=ISO-8859-1'>");
         if(styleSheet != null)
             out.println("		<link rel='stylesheet' href='"+ styleSheet +"'>");
         out.println("	</head>");
         out.println("");
         out.println("	<body bgcolor='#cccccc' leftmargin='5' marginheight='5' marginwidth='5' topmargin='5'>");
         out.println("		<basefont face='Trebuchet MS' size=2>");
         out.println("		<center>");
         out.println("		<table width='100%' border='0' cellspacing='0' cellpadding='0' height='100%'>");
         out.println("			<tr height='56'>");
         out.println("				<td align='left' valign='top' height='56'>");
         out.println("					<table width='100%' border='0' cellspacing='0' cellpadding='0'>");
         out.println("						<tr>");
         out.println("							<td align='left' valign='top' width='412'><a href='http://www.netspective.com'><img src='"+ sparxSampleImagesUrl +"/sample-apps-01.gif' alt='' width='412' height='56' border='0'></a></td>");
         out.println("							<td align='left' valign='top' width='100%'><img src='"+ sparxSampleImagesUrl +"/sample-apps-02.gif' alt='' width='100%' height='56' border='0'></td>");
         out.println("							<td align='left' valign='top' width='181'>");
         out.println("								<table width='64' border='0' cellspacing='0' cellpadding='0'>");
         out.println("									<tr>");
         out.println("										<td align='left' valign='top'><img src='"+ sparxSampleImagesUrl +"/sample-apps-03.gif' alt='' width='181' height='9' border='0'></td>");
         out.println("									</tr>");
         out.println("									<tr>");
         out.println("										<td align='left' valign='top'>");
         out.println("											<table width='72' border='0' cellspacing='0' cellpadding='0'>");
         out.println("												<tr>");
         out.println("													<td align='left' valign='top'><img src='"+ sparxSampleImagesUrl +"/sample-apps-04.gif' alt='' width='6' height='47' border='0'></td>");
         out.println("													<td align='left' valign='top'>");
         out.println("														<table width='64' border='0' cellspacing='0' cellpadding='0'>");
         out.println("															<tr height='17'>");
         out.println("																<td align='center' valign='middle' bgcolor='white' height='17'><font size=1 face='Arial,Helvetica,Geneva,Swiss,SunSans-Regular'><a href='"+ sparxACEUrl +"'>Admin Console (ACE)</a></font></td>");
         out.println("															</tr>");
         out.println("															<tr>");
         out.println("																<td align='left' valign='top'><img src='"+ sparxSampleImagesUrl +"/sample-apps-07.gif' alt='' width='156' height='30' border='0'></td>");
         out.println("															</tr>");
         out.println("														</table>");
         out.println("													</td>");
         out.println("													<td align='left' valign='top'><img src='"+ sparxSampleImagesUrl +"/sample-apps-06.gif' alt='' width='19' height='47' border='0'></td>");
         out.println("												</tr>");
         out.println("											</table>");
         out.println("										</td>");
         out.println("									</tr>");
         out.println("								</table>");
         out.println("							</td>");
         out.println("						</tr>");
         out.println("					</table>");
         out.println("				</td>");
         out.println("			</tr>");
         out.println("			<tr height='100%'>");
         out.println("				<td align='left' valign='top' height='100%'>");
         out.println("					<table width='100%' border='0' cellspacing='0' cellpadding='0' height='100%'>");
         out.println("						<tr height='100%'>");
         out.println("							<td align='left' valign='top' width='15' height='100%' background='"+ sparxSampleImagesUrl +"/sample-apps-08.gif'><img src='"+ sparxSampleImagesUrl +"/sample-apps-spacer.gif' alt='' width='15' height='100%' border='0'></td>");
         out.println("							<td align='left' valign='top' width='100%' height='100%' bgcolor='white'>");
     }
 
     /**
      * Used by Sparx sample applications to show the shell
      */
     public void doSamplePageEnd() throws IOException
     {
         Configuration config = ConfigurationManagerFactory.getDefaultConfiguration(pageContext.getServletContext());
         ServletValueContext svc = new ServletValueContext(pageContext.getServletContext(), (Servlet) pageContext.getPage(), pageContext.getRequest(), pageContext.getResponse());
         String sparxSampleImagesUrl = config.getTextValue(svc, Globals.SHARED_CONFIG_ITEMS_PREFIX + "images-url") + "/samples";
 
         javax.servlet.jsp.JspWriter out = pageContext.getOut();
         out.println("                         <p><center><font size=2 color=silver>"+ BuildConfiguration.getProductBuild() + "</font></center>");
         out.println("							</td>");
         out.println("							<td align='left' valign='top' width='19' height='100%' background='"+ sparxSampleImagesUrl +"/sample-apps-10.gif'><img src='"+ sparxSampleImagesUrl +"/sample-apps-spacer.gif' alt='' width='19' height='100%' border='0'></td>");
         out.println("						</tr>");
         out.println("					</table>");
         out.println("				</td>");
         out.println("			</tr>");
         out.println("			<tr height='26'>");
         out.println("				<td align='left' valign='top' height='26'>");
         out.println("					<table width='100%' border='0' cellspacing='0' cellpadding='0'>");
         out.println("						<tr valign='bottom'>");
         out.println("							<td align='left' valign='top' width='412'><a href='http://www.netspective.com'><img src='"+ sparxSampleImagesUrl +"/sample-apps-11.gif' alt='' width='412' height='26' border='0'></a></td>");
         out.println("							<td align='left' valign='top' width='100%'><img src='"+ sparxSampleImagesUrl +"/sample-apps-12.gif' alt='' width='100%' height='26' border='0'></td>");
         out.println("							<td align='left' valign='top' width='181'><a href='http://developer.netspective.com'><img src='"+ sparxSampleImagesUrl +"/sample-apps-13.gif' alt='' width='181' height='26' border='0'></a></td>");
         out.println("						</tr>");
         out.println("					</table>");
         out.println("				</td>");
         out.println("			</tr>");
         out.println("		</table>");
         out.println("		</center>");
         out.println("	</body>");
         out.println("</html>");
     }
 }
