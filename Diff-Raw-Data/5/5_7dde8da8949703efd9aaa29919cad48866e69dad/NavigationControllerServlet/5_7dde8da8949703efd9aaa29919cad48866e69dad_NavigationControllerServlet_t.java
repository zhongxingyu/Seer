 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: NavigationControllerServlet.java,v 1.34 2003-11-25 01:03:06 shahid.shah Exp $
  */
 
 package com.netspective.sparx.navigate;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.*;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.tools.ant.BuildLogger;
 import org.apache.tools.ant.NoBannerLogger;
 
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationSkin;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.ProjectManager;
 import com.netspective.sparx.ProjectComponent;
 import com.netspective.sparx.ProjectLifecyleListener;
 import com.netspective.sparx.ProjectEvent;
 import com.netspective.sparx.ant.AntProject;
 import com.netspective.sparx.util.HttpUtils;
 import com.netspective.commons.io.MultipleUriAddressableFileLocators;
 import com.netspective.commons.io.UriAddressableFileLocator;
 import com.netspective.commons.io.UriAddressableInheritableFileResource;
 import com.netspective.sparx.security.HttpLoginManager;
 import com.netspective.sparx.security.LoginDialogMode;
 import com.netspective.sparx.value.BasicDbHttpServletValueContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.theme.Themes;
 import com.netspective.commons.RuntimeEnvironmentFlags;
 import com.netspective.commons.RuntimeEnvironment;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.xdm.XdmComponentFactory;
 import com.netspective.commons.io.FileFind;
 import com.netspective.commons.text.TextUtils;
 
 public class NavigationControllerServlet extends HttpServlet implements RuntimeEnvironment, ProjectManager
 {
     private static final Log log = LogFactory.getLog(NavigationControllerServlet.class);
     private static final Set allControllerServlets = Collections.synchronizedSet(new HashSet());
 
     public static final String REQATTRNAME_RENDER_START_TIME = NavigationControllerServlet.class.getName() + ".START_TIME";
     public static final String PROPNAME_INIT_COUNT = "SERVLET_INITIALIZATION_COUNT";
     public static final String REQPARAMNAME_COMMAND_ONLY = "command-only";
 
     public static Set getAllControllerServlets()
     {
         return allControllerServlets;
     }
 
     private NavigationControllerServletOptions servletOptions;
     private String projectSourceFileName;
     private Class projectComponentClass;
     private int lastProjectComponentRetrievedId;
     private Project project;
     private HttpLoginManager loginManager;
     private Theme theme;
     private NavigationTree navigationTree;
     private UriAddressableFileLocator resourceLocator;
     private RuntimeEnvironmentFlags runtimeEnvironmentFlags;
     private boolean cacheComponents;
     private String executionPropertiesFileName;
     private Properties executionProperties;
     private long initializationCount;
     private boolean initCountWritten;
     private Map staticPagesRendered = new HashMap();
 
     public void init(ServletConfig servletConfig) throws ServletException
     {
         allControllerServlets.add(this);
         super.init(servletConfig);
 
         servletOptions = constructServletOptions(servletConfig);
         if(servletOptions.isHelpRequested())
             servletOptions.printHelp();
         if(servletOptions.isDebugOptionsRequested())
             System.out.println("** Servlet Options:\n" + servletOptions);
 
         loadExecutionProperties(servletConfig);
         if(getInitializationCount() == 1)
             initOnlyFirstExecution(servletConfig);
         initEachExecution(servletConfig);
 
         // if the init success is determined to be END_INIT we persist now, otherwise it will be done on first GET/POST
         if(servletOptions.getInitSuccessType().equals("END_INIT"))
             persistInitCount();
     }
 
     protected NavigationControllerServletOptions constructServletOptions(ServletConfig servletConfig)
     {
         return new NavigationControllerServletOptions(servletConfig);
     }
 
     protected void loadExecutionProperties(ServletConfig servletConfig) throws ServletException
     {
         executionPropertiesFileName = checkWebInfAndGetRealPath(servletOptions.getServletExecutionPropertiesFileName());
         executionProperties = new Properties();
         try
         {
             executionProperties.load(new FileInputStream(new File(executionPropertiesFileName)));
             initializationCount = Long.valueOf(executionProperties.getProperty(getClass().getName() + '.' + PROPNAME_INIT_COUNT, "0")).longValue();
         }
         catch (FileNotFoundException e)
         {
             initializationCount = 0;
         }
         catch (IOException e)
         {
             throw new ServletException(e);
         }
         initializationCount++;
         log.debug("Initialization count is " + getInitializationCount());
     }
 
     protected void persistInitCount() throws ServletException
     {
         executionProperties.setProperty(getClass().getName() + '.' + PROPNAME_INIT_COUNT, Long.toString(initializationCount));
         saveExecutionProperties();
         initCountWritten = true;
     }
 
     protected void saveExecutionProperties() throws ServletException
     {
         try
         {
             executionProperties.store(new FileOutputStream(new File(executionPropertiesFileName)), "Project execution properties");
         }
         catch (IOException e)
         {
             throw new ServletException(e);
         }
     }
 
     protected void initRuntimeEnvironmentFlags(ServletConfig servletConfig)
     {
         String envFlagsText = servletOptions.getRuntimeEnvFlags();
         try
         {
             Class envClass = Class.forName(servletOptions.getRuntimeEnvClassName());
             runtimeEnvironmentFlags = (RuntimeEnvironmentFlags) envClass.newInstance();
         }
         catch (Exception e)
         {
             log.error("Unable to instantiate environment flags using SPI -- creating statically instead", e);
             runtimeEnvironmentFlags = new RuntimeEnvironmentFlags();
         }
         runtimeEnvironmentFlags.setValue(envFlagsText);
         setCacheComponents(! runtimeEnvironmentFlags.flagIsSet(RuntimeEnvironmentFlags.DEVELOPMENT | RuntimeEnvironmentFlags.FRAMEWORK_DEVELOPMENT));
     }
 
     protected void executAntBuild(ServletConfig servletConfig, File buildFile, String target) throws ServletException
     {
         log.debug("Executing Ant build " + buildFile + " target " + target);
 
         org.apache.tools.ant.Project antProject = AntProject.getConfiguredProject(buildFile);
         antProject.setProperty("app.home", servletConfig.getServletContext().getRealPath("/"));
         antProject.setProperty("app.init-count", Long.toString(getInitializationCount()));
 
         Properties servletOptionsProps = servletOptions.setProperties(new Properties(), "app.servlet-options", false);
         for(Iterator i = servletOptionsProps.keySet().iterator(); i.hasNext(); )
         {
             String propName = (String) i.next();
             antProject.setProperty(propName, servletOptionsProps.getProperty(propName));
         }
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         PrintStream pstream = new PrintStream(ostream);
 
         BuildLogger logger = new NoBannerLogger();
         logger.setMessageOutputLevel(org.apache.tools.ant.Project.MSG_INFO);
         logger.setOutputPrintStream(pstream);
         logger.setErrorPrintStream(pstream);
 
         PrintStream saveOut = System.out;
         PrintStream saveErr = System.err;
         System.setOut(pstream);
         System.setErr(pstream);
 
         antProject.addBuildListener(logger);
         Exception exceptionThrown = null;
         try
         {
             Vector targets = new Vector();
             if(target != null)
             {
                 String[] targetNames = TextUtils.split(target, ",", true);
                 for(int i = 0; i < targetNames.length; i++)
                     targets.add(targetNames[i]);
             }
             else
                 targets.add(antProject.getDefaultTarget());
             antProject.executeTargets(targets);
         }
         catch (Exception e)
         {
             exceptionThrown = e;
         }
 
         if(exceptionThrown != null)
         {
             log.error(ostream.toString());
             log.error("Error running ant build file " + buildFile + " target " + target, exceptionThrown);
         }
         else
             log.debug(ostream.toString());
 
         System.setOut(saveOut);
         System.setErr(saveErr);
     }
 
     protected void initUsingAnt(ServletConfig servletConfig, String optionText) throws ServletException
     {
         String buildFileName = optionText;
         String target = null;
         int targetNameDelimPos = optionText.lastIndexOf(':');
         if(targetNameDelimPos > 0)
         {
             buildFileName = optionText.substring(0, targetNameDelimPos);
             target = optionText.substring(targetNameDelimPos+1);
         }
 
         executAntBuild(servletConfig, new File(checkWebInfAndGetRealPath(buildFileName)), target);
     }
 
     /**
      * Called when the servlet is intialized for the very first time in this servlet container. The init count is
      * stored in a properties file.
      * @param servletConfig
      */
     protected void initOnlyFirstExecution(ServletConfig servletConfig) throws ServletException
     {
         if(servletOptions.getInitFirstTimeUsingAnt() != null)
             initUsingAnt(servletConfig, servletOptions.getInitFirstTimeUsingAnt());
     }
 
     protected void initEachExecution(ServletConfig servletConfig) throws ServletException
     {
         if(servletOptions.getInitUsingAnt() != null)
             initUsingAnt(servletConfig, servletOptions.getInitUsingAnt());
 
         try
         {
             projectComponentClass = Class.forName(servletOptions.getProjectComponentClassName());
         }
         catch (ClassNotFoundException e)
         {
             log.error("Unable to find class for ProjectComponent instance.", e);
             throw new ServletException(e);
         }
 
         projectSourceFileName = checkWebInfAndGetRealPath(servletOptions.getProjectFileName());
         File xdmSourceFile = new File(projectSourceFileName);
         if(! xdmSourceFile.exists())
             throw new ServletException("Sparx XDM source file '"+ xdmSourceFile.getAbsolutePath() +"' does not exist. Please " +
                     "correct the servlet-param called '"+ NavigationControllerServletOptions.INITPARAMNAME_SERVLET_OPTIONS +"' in your WEB-INF/web.xml file.");
 
         initRuntimeEnvironmentFlags(servletConfig);
         if(isCacheComponents())
         {
             // go ahead and grab all the components now -- so that we don't have to synchronize calls later
             getProject();
             getLoginManager();
             getNavigationTree();
         }
     }
 
     /**
      * Initializes the web resource locators to the following:
      *   - APP_ROOT/resources/sparx (will only exist if user is overriding any defaults)
      *   - APP_ROOT/sparx (will exist in ITE mode when sparx directory is inside application)
      *   - [CLASS_PATH]/Sparx/resources (only useful during development in SDE, not production since it won't be found)
      * TODO: this method is _not_ thread-safe because two requests could call the method at the same time FIX IT
      * @throws ServletException
      */
     protected UriAddressableFileLocator getResourceLocator(HttpServletRequest request) throws ServletException
     {
         if(resourceLocator != null)
             return resourceLocator;
 
         ServletContext servletContext = getServletContext();
         try
         {
             String[] webAppLocations = TextUtils.split(servletOptions.getSparxResourceLocators(), ",", false);
             List locators = new ArrayList();
             for(int i = 0; i < webAppLocations.length; i++)
             {
                 String webAppRelativePath = webAppLocations[i];
                 File webAppPhysicalDir = new File(servletContext.getRealPath(webAppRelativePath));
                 if(webAppPhysicalDir.exists() && webAppPhysicalDir.isDirectory())
                     locators.add(new UriAddressableInheritableFileResource(request.getContextPath() + webAppRelativePath, webAppPhysicalDir, isCacheComponents()));
             }
 
             // this will only match the SDE development environment
             FileFind.FileFindResults ffResults = FileFind.findInClasspath("Sparx/resources", FileFind.FINDINPATHFLAG_DEFAULT);
             if(ffResults.isFileFound() && ffResults.getFoundFile().isDirectory())
                 locators.add(new UriAddressableInheritableFileResource(request.getContextPath() + "/sparx", ffResults.getFoundFile(), isCacheComponents()));
 
             if(log.isDebugEnabled())
             {
                 for(int i = 0; i < locators.size(); i++)
                     log.debug("Registered web resources locator " + locators.get(i));
             }
 
             if(locators.size() == 0)
                 System.err.println("Unable to register any web resource locators ("+ TextUtils.join(webAppLocations, ", ") +" were not found).");
 
             resourceLocator = new MultipleUriAddressableFileLocators((UriAddressableFileLocator[]) locators.toArray(new UriAddressableFileLocator[locators.size()]), isCacheComponents());
             return resourceLocator;
         }
         catch (IOException e)
         {
             log.error("error initializing resource locator", e);
             throw new ServletException(e);
         }
     }
 
     public String checkWebInfAndGetRealPath(String path)
     {
         if(path.startsWith("/WEB-INF"))
             return getServletContext().getRealPath(path);
         if(path.startsWith("WEB-INF"))
             return getServletContext().getRealPath("/" + path);
         return path;
     }
 
     public NavigationControllerServletOptions getServletOptions()
     {
         return servletOptions;
     }
 
     public String getExecutionPropertiesFileName()
     {
         return executionPropertiesFileName;
     }
 
     public Properties getExecutionProperties()
     {
         return executionProperties;
     }
 
     public long getInitializationCount()
     {
         return initializationCount;
     }
 
     public RuntimeEnvironmentFlags getRuntimeEnvironmentFlags()
     {
         return runtimeEnvironmentFlags;
     }
 
     public boolean isCacheComponents()
     {
         return cacheComponents;
     }
 
     protected void setCacheComponents(boolean cacheComponents)
     {
         this.cacheComponents = cacheComponents;
     }
 
     public boolean isSecure() throws ServletException
     {
         return getLoginManager() != null;
     }
 
     public Theme getTheme(HttpServletRequest request) throws ServletException
     {
         if(theme == null || ! isCacheComponents())
         {
             String themeName = servletOptions.getThemeName();
             Themes themes = getProject().getThemes();
             theme = themeName != null ? themes.getTheme(themeName) : themes.getDefaultTheme();
             theme.setWebResourceLocator(getResourceLocator(request));
         }
 
         return theme;
     }
 
     public NavigationTree getNavigationTree() throws ServletException
     {
         if(navigationTree == null || ! isCacheComponents())
         {
             String navTreeName = servletOptions.getNavigationTreeName();
             Project project = getProject();
             navigationTree = navTreeName != null ? project.getNavigationTree(navTreeName) : project.getDefaultNavigationTree();
         }
 
         return navigationTree;
     }
 
     public HttpLoginManager getLoginManager() throws ServletException
     {
         if(servletOptions.getLoginManagerName() != null && (loginManager == null || ! isCacheComponents()))
             loginManager = getProject().getLoginManagers().getLoginManager(servletOptions.getLoginManagerName());
         else
             loginManager = getProject().getLoginManagers().getDefaultManager();
         return loginManager;
     }
 
     public ProjectComponent getProjectComponent()
     {
         try
         {
             int compFlags = XdmComponentFactory.XDMCOMPFLAG_CACHE_ALWAYS;
             if(getRuntimeEnvironmentFlags().flagIsSet(RuntimeEnvironmentFlags.DEVELOPMENT | RuntimeEnvironmentFlags.FRAMEWORK_DEVELOPMENT))
                 compFlags |= XdmComponentFactory.XDMCOMPFLAG_ALLOWRELOAD;
 
             // never store the ProjectComponent instance since it may change if it needs to be reloaded
             // (always use the factory get() method)
             ProjectComponent projectComponent =
                 (ProjectComponent) XdmComponentFactory.get(projectComponentClass, projectSourceFileName, compFlags);
 
             if(lastProjectComponentRetrievedId != projectComponent.hashCode())
             {
                 if(projectComponent.getErrors().size() > 0)
                 {
                     String message = "You have " + projectComponent.getErrors().size() + " error(s) in the project. To see the messages, visit\nhttp://<your-host>"+ getServletContext().getServletContextName() +"/console/project/input-source#errors.";
                     if(log.isErrorEnabled())
                         log.error(message);
                     else
                         System.err.println(message);
 
                     for(int i = 0; i < projectComponent.getErrors().size(); i++)
                         System.err.println(projectComponent.getErrors().get(i));
                 }
                 if(projectComponent.getWarnings().size() > 0)
                 {
                     String message = "You have " + projectComponent.getWarnings().size() + " warning(s) in the project. To see the messages, visit\nhttp://<your-host>"+ getServletContext().getServletContextName() +"/console/project/input-source#warnings.";
                     if(log.isWarnEnabled())
                         log.warn(message);
                     else
                         System.out.println(message);
                 }
 
                 String[] listeners = servletOptions.getProjectLifecycleListenerClassNames();
                 if(listeners != null)
                 {
                     for(int i = 0; i < listeners.length; i++)
                     {
                         ProjectEvent event = new ProjectEvent(projectComponent.getProject());
                         Class listenerClass = Class.forName(listeners[i]);
                         if(ProjectLifecyleListener.class.isAssignableFrom(listenerClass))
                         {
                             ProjectLifecyleListener pll = (ProjectLifecyleListener) listenerClass.newInstance();
                             pll.projectLoadedFromXml(event);
                         }
                         else
                             log.error("Unknown listener: " + listenerClass);
                     }
                 }
 
                 // clear the currently cached project if there is one
                 project = null;
 
                 // save this for the next time so that we don't reinitialize or run the listeners again
                 lastProjectComponentRetrievedId = projectComponent.hashCode();
             }
 
             return projectComponent;
         }
         catch(Exception e)
         {
             throw new NestableRuntimeException(e);
         }
     }
 
     public Project getProject()
     {
         if(project == null || ! isCacheComponents())
             project = getProjectComponent().getProject();
         return project;
     }
 
     public NavigationContext createNavigationContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
     {
         Project project = getProject();
 
         Theme theme = getTheme(httpServletRequest);
         httpServletRequest.setAttribute(BasicDbHttpServletValueContext.REQATTRNAME_ACTIVE_THEME, theme);
 
         NavigationTree tree = null;
 
         if(isSecure())
         {
             // check to see if there is an active user and the user wants a user-specific navigation tree
             AuthenticatedUser user = getLoginManager().getAuthenticatedUser(httpServletRequest);
             if(user instanceof NavigationControllerAuthenticatedUser)
             {
                 NavigationControllerAuthenticatedUser ncUser = (NavigationControllerAuthenticatedUser) user;
                 if(ncUser.hasUserSpecificNavigationTree())
                     tree = ncUser.getUserSpecificNavigationTree(this, httpServletRequest, httpServletResponse);
             }
         }
 
         // if we get to here it means the user is not overriding the current tree so we'll use the default
         if(tree == null)
             tree = getNavigationTree();
 
         // if the tree is still null we've got a big problem
         if(tree == null)
             throw new ServletException("Navigation tree '"+ servletOptions.getNavigationTreeName() +"' not found. Available: " + project.getNavigationTrees());
 
         String activePageId = httpServletRequest.getPathInfo();
         if(activePageId == null)
             activePageId = "/";
 
         NavigationSkin skin = theme.getDefaultNavigationSkin();
 
         return skin.createContext(this, httpServletRequest, httpServletResponse,
                                         tree, activePageId);
     }
 
     protected boolean logoutRequested(NavigationContext nc) throws ServletException, IOException
     {
         if(isSecure())
         {
             String logoutActionReqParamValue = nc.getHttpRequest().getParameter(servletOptions.getLogoutActionReqParamName());
             if(logoutActionReqParamValue != null && TextUtils.toBoolean(logoutActionReqParamValue))
             {
                 getLoginManager().logout(nc);
                 return true;
             }
         }
 
         return false;
     }
 
     protected void renderPage(NavigationContext nc) throws ServletException, IOException
     {
         if(isSecure())
         {
             HttpLoginManager loginManager = getLoginManager();
             LoginDialogMode loginDialogMode = LoginDialogMode.ACCESS_ALLOWED;
             if(loginManager != null)
             {
                 nc.getRequest().setAttribute(BasicDbHttpServletValueContext.REQATTRNAME_ACTIVE_LOGIN_MANAGER, loginManager);
                 loginDialogMode = loginManager.getLoginDialogMode(nc);
 
                 // if we're getting input or we're denying login it means that the presentation is complete (HTML is already on the screen)
                 if(loginDialogMode == LoginDialogMode.GET_INPUT || loginDialogMode == LoginDialogMode.LOGIN_DENIED)
                     return;
             }
 
             // if we get to here, it means that the login dialog mode is either LOGIN_ACCEPTED (user has just logged in) or ACCESS_ALLOWED
             // which means that access was previously granted and the user is still valid
 
             // check to see if the user has recently logged in and is using the wrong navigation tree
             if(loginDialogMode == LoginDialogMode.LOGIN_ACCEPTED)
             {
                 AuthenticatedUser user = nc.getAuthenticatedUser();
                 if(user instanceof NavigationControllerAuthenticatedUser)
                 {
                     NavigationControllerAuthenticatedUser ncUser = (NavigationControllerAuthenticatedUser) user;
                     if(ncUser.hasUserSpecificNavigationTree())
                     {
                         NavigationTree userTree = ncUser.getUserSpecificNavigationTree(this, nc.getHttpRequest(), nc.getHttpResponse());
                         if(userTree != null && nc.getOwnerTree() != userTree)
                         {
                             // we want to redirect back to the home page of the navigation tree so that the proper tree
                             // will be picked up by the createContext() method
                             ncUser.redirectToUserTree(nc);
                             return;
                         }
                     }
                 }
             }
         }
 
         NavigationPage activePage = nc.getActivePage();
         Writer writer = nc.getResponse().getWriter();
 
         if(activePage != null)
         {
             if(nc.isActivePageValid())
             {
                 if(activePage.getFlags().flagIsSet(NavigationPage.Flags.STATIC_CONTENT))
                 {
                     final HttpServletRequest httpRequest = nc.getHttpRequest();
                     String staticPageKey = httpRequest.getServletPath() + httpRequest.getPathInfo();
                     Date lastModfTime = (Date) staticPagesRendered.get(staticPageKey);
 
                     // If the client sent an If-Modified-Since header equal or after the
                     // servlet's last modified time, send a short "Not Modified" status code
                     // Round down to the nearest second since client headers are in seconds
                    if (lastModfTime != null && httpRequest.getMethod().equals("GET") &&
                         (lastModfTime.getTime() / 1000 * 1000) <= httpRequest.getDateHeader("If-Modified-Since"))
                     {
                         nc.getHttpResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                         return;
                     }
                     else
                         staticPagesRendered.put(staticPageKey, new Date());
                 }
 
                 // if we get to there we're not static content or we're static but being rendered for the first
                 // time in this instance of the servlet
                 activePage.handlePage(writer, nc);
             }
             else
                 activePage.handleInvalidPage(writer, nc);
         }
         else
         {
             NavigationSkin skin = nc.getSkin();
             NavigationTree tree = nc.getOwnerTree();
 
             skin.renderPageMetaData(writer, nc);
             skin.renderPageHeader(writer, nc);
             writer.write("No page located for path '"+ nc.getActivePathFindResults().getSearchedForPath() +"'.");
             if(nc.getRuntimeEnvironmentFlags().flagIsSet(RuntimeEnvironmentFlags.DEVELOPMENT))
             {
                 writer.write("<pre>\n");
                 writer.write(tree.toString());
                 writer.write("</pre>\n");
             }
             skin.renderPageFooter(writer, nc);
         }
     }
 
     protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
     {
         // record the starting time because it may be used by skins to show complete render times.
         httpServletRequest.setAttribute(REQATTRNAME_RENDER_START_TIME, new Long(System.currentTimeMillis()));
 
         NavigationContext nc = createNavigationContext(httpServletRequest, httpServletResponse);
         if(nc.getActivePage() == null)
         {
             httpServletResponse.setContentType("text/html");
             httpServletResponse.getWriter().println("No active page located in NavigationTree '" + getNavigationTree().getName() + "' -- did you set a default page in the tree?<p>For example <code>&lt;page name=\"blah\" default=\"yes\"/&gt;</code>");
             return;
         }
 
         if(logoutRequested(nc))
         {
             httpServletResponse.sendRedirect(nc.getServletRootUrl());
             return;
         }
 
         if(nc.isRedirectToAlternateChildRequired())
         {
             String url =nc.getActivePage().getUrl(nc);
             if(url.indexOf('?') == -1) // see if we've appened any parameters (if not, we want to include all)
                 url = HttpUtils.appendParams(httpServletRequest, url, "*");
             httpServletResponse.sendRedirect(url);
             return;
         }
         else
             renderPage(nc);
 
         // if we get to here it means no exceptions were thrown during initialization and the first get/post -- this
         // means we're going to assume our initialization was properly done and increment the persistent init count
         if(! initCountWritten)
             persistInitCount();
     }
 
     protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
     {
         doGet(httpServletRequest, httpServletResponse);
     }
 }
