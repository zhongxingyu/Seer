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
 * $Id: NavigationPage.java,v 1.57 2004-02-10 16:29:55 shahid.shah Exp $
  */
 
 package com.netspective.sparx.navigate;
 
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.command.Commands;
 import com.netspective.commons.command.CommandException;
 import com.netspective.commons.command.Command;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.sparx.value.HttpServletValueContext;
 import com.netspective.sparx.panel.HtmlLayoutPanel;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.AbstractPanel;
 import com.netspective.sparx.util.HttpUtils;
 import com.netspective.sparx.util.AlternateOutputDestServletResponse;
 import com.netspective.commons.template.TemplateProcessor;
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.sparx.command.AbstractHttpServletCommand;
 import com.netspective.sparx.command.HttpServletCommand;
 import com.netspective.sparx.navigate.listener.NavigationPathListener;
 import com.netspective.sparx.navigate.listener.NavigationPageEnterListener;
 import com.netspective.sparx.navigate.listener.NavigationPageExitListener;
 import com.netspective.sparx.navigate.handler.NavigationPageBodyDefaultHandler;
 import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;
 import com.netspective.sparx.form.handler.DialogNextActionProvider;
 import com.netspective.sparx.form.DialogContext;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.io.StringWriter;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Constructor;
 import javax.servlet.ServletException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.exception.NestableException;
 
 public class NavigationPage extends NavigationPath implements TemplateConsumer, XmlDataModelSchema.InputSourceLocatorListener, DialogNextActionProvider
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     public static final XdmBitmaskedFlagsAttribute.FlagDefn[] PAGE_FLAG_DEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[NavigationPathFlags.FLAG_DEFNS.length + 17];
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[] { "name" };
     public static final String PARAMNAME_PAGE_FLAGS = "page-flags";
     public static final String REQATTRNAME_NAVIGATION_CONTEXT = "navigationContext";
     private static final int INHERIT_PAGE_FLAGS_FROM_PARENT = NavigationPath.INHERIT_PATH_FLAGS_FROM_PARENT | Flags.REQUIRE_LOGIN | Flags.ALLOW_PAGE_CMD_PARAM | Flags.ALLOW_VIEW_SOURCE;
 
     static
     {
         for(int i = 0; i < NavigationPathFlags.FLAG_DEFNS.length; i++)
             PAGE_FLAG_DEFNS[i] = NavigationPathFlags.FLAG_DEFNS[i];
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 0] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_PRIVATE, "REQUIRE_LOGIN", Flags.REQUIRE_LOGIN);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 1] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "STATIC", Flags.STATIC_CONTENT);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 2] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "REJECT_FOCUS", Flags.REJECT_FOCUS);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 3] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HIDDEN", Flags.HIDDEN);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 4] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "ALLOW_PAGE_CMD_PARAM", Flags.ALLOW_PAGE_CMD_PARAM);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 5] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_RETAIN_PARAMS", Flags.INHERIT_RETAIN_PARAMS);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 6] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_ASSIGN_STATE_PARAMS", Flags.INHERIT_ASSIGN_STATE_PARAMS);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 7] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "POPUP", Flags.IS_POPUP_MODE);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 8] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "PRINT", Flags.IS_PRINT_MODE);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 9] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "SERVICE", Flags.IS_SERVICE_MODE);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 10] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "SHOW_RENDER_TIME", Flags.SHOW_RENDER_TIME);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 11] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_META_DATA", Flags.HANDLE_META_DATA);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 12] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_HEADER", Flags.HANDLE_HEADER);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 13] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HANDLE_FOOTER", Flags.HANDLE_FOOTER);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 14] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "DEBUG_REQUEST", Flags.DEBUG_REQUEST);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 15] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "BODY_AFFECTS_NAVIGATION", Flags.BODY_AFFECTS_NAVIGATION);
         PAGE_FLAG_DEFNS[NavigationPathFlags.FLAG_DEFNS.length + 16] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "ALLOW_VIEW_SOURCE", Flags.ALLOW_VIEW_SOURCE);
     }
 
     protected class PageTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public PageTypeTemplateConsumerDefn()
         {
             super(null, ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
         }
 
         public String getNameSpaceId()
         {
             return getOwner().getPageTypesTemplatesNameSpaceId();
         }
     }
 
     public class Flags extends NavigationPathFlags
     {
         public static final int REQUIRE_LOGIN = NavigationPathFlags.START_CUSTOM;
         public static final int STATIC_CONTENT = REQUIRE_LOGIN * 2;
         public static final int REJECT_FOCUS = STATIC_CONTENT * 2;
         public static final int HIDDEN = REJECT_FOCUS * 2;
         public static final int ALLOW_PAGE_CMD_PARAM = HIDDEN * 2;
         public static final int INHERIT_RETAIN_PARAMS = ALLOW_PAGE_CMD_PARAM * 2;
         public static final int INHERIT_ASSIGN_STATE_PARAMS = INHERIT_RETAIN_PARAMS * 2;
         public static final int IS_POPUP_MODE = INHERIT_ASSIGN_STATE_PARAMS * 2;
         public static final int IS_PRINT_MODE = IS_POPUP_MODE * 2;
         public static final int IS_SERVICE_MODE = IS_PRINT_MODE * 2;
         public static final int SHOW_RENDER_TIME = IS_SERVICE_MODE * 2;
         public static final int HANDLE_META_DATA = SHOW_RENDER_TIME * 2;
         public static final int HANDLE_HEADER = HANDLE_META_DATA * 2;
         public static final int HANDLE_FOOTER = HANDLE_HEADER * 2;
         public static final int DEBUG_REQUEST = HANDLE_FOOTER * 2;
         public static final int BODY_AFFECTS_NAVIGATION = DEBUG_REQUEST * 2;
         public static final int ALLOW_VIEW_SOURCE = BODY_AFFECTS_NAVIGATION * 2;
         public static final int START_CUSTOM = ALLOW_VIEW_SOURCE * 2;
 
         public Flags()
         {
             setFlag(REQUIRE_LOGIN | HANDLE_META_DATA | HANDLE_HEADER | HANDLE_FOOTER | INHERIT_RETAIN_PARAMS | INHERIT_ASSIGN_STATE_PARAMS);
         }
 
         public FlagDefn[] getFlagsDefns()
         {
             return PAGE_FLAG_DEFNS;
         }
 
         public void clearFlag(long flag)
         {
             super.clearFlag(flag);
             if((flag & (REJECT_FOCUS | HIDDEN)) != 0)
                 clearFlagRecursively(flag);
         }
 
         public void setFlag(long flag)
         {
             super.setFlag(flag);
             if((flag & (REJECT_FOCUS | HIDDEN)) != 0)
                 setFlagRecursively(flag);
         }
 
         public boolean isPopup()
         {
             return flagIsSet(IS_POPUP_MODE);
         }
 
         public boolean isHidden()
         {
             return flagIsSet(HIDDEN);
         }
 
         public boolean isRejectFocus()
         {
             return flagIsSet(REJECT_FOCUS);
         }
 
         public boolean isDebuggingRequest()
         {
             return flagIsSet(DEBUG_REQUEST);
         }
 
         public boolean isAllowViewSource()
         {
             return flagIsSet(ALLOW_VIEW_SOURCE);
         }
     }
 
     public class State extends NavigationPath.State
     {
     }
 
     private InputSourceLocator inputSourceLocator;
     private TemplateConsumerDefn templateConsumer;
     private NavigationPageBodyType bodyType = new NavigationPageBodyType(NavigationPageBodyType.NONE);
     private String pageFlagsParamName = PARAMNAME_PAGE_FLAGS;
     private ValueSource caption;
     private ValueSource title;
     private ValueSource heading;
     private ValueSource subHeading;
     private ValueSource retainParams;
     private ValueSource assignStateParams;
     private List requireRequestParams = new ArrayList();
     private ValueSource redirect;
     private String redirectTarget;
     private ValueSource forward;
     private ValueSource include;
     private HtmlLayoutPanel bodyPanel;
     private TemplateProcessor bodyTemplate;
     private TemplateProcessor missingParamsBodyTemplate;
     private ValueSource baseAttributes;
     private Command bodyCommand;
     private ValueSource bodyCommandExpr;
     private List pageTypesConsumed = new ArrayList();
     private List customHandlers = new ArrayList();
     private List enterListeners = new ArrayList();
     private List exitListeners = new ArrayList();
     private ValueSource dialogNextActionUrl;
     private DialogNextActionProvider dialogNextActionProvider;
     private List errorPagesList = new ArrayList();
     private Map errorPagesMap = new HashMap();
     private Map errorPageDescendantsByQualifiedName = new HashMap();
 
     public NavigationPage(NavigationTree owner)
     {
         super(owner);
     }
 
     public InputSourceLocator getInputSourceLocator()
     {
         return inputSourceLocator;
     }
 
     public void setInputSourceLocator(InputSourceLocator inputSourceLocator)
     {
         this.inputSourceLocator = inputSourceLocator;
     }
 
     public void addListener(NavigationPathListener listener)
     {
         super.addListener(listener);
         if(listener instanceof NavigationPageEnterListener)
             enterListeners.add(listener);
         else if(listener instanceof NavigationPageExitListener)
             exitListeners.add(listener);
     }
 
     /* --- Templates consumption ------------------------------------------------------------------------------------*/
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         if(templateConsumer == null)
             templateConsumer = new PageTypeTemplateConsumerDefn();
         return templateConsumer;
     }
 
     public void registerTemplateConsumption(Template template)
     {
         pageTypesConsumed.add(template.getTemplateName());
     }
 
     /* --- XDM Callbacks --------------------------------------------------------------------------------------------*/
 
     public NavigationPage createPage() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         // When inheriting pages, we want our child pages to be the same class as us
         return createPage(getClass());
     }
 
     public NavigationPage createPage(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(NavigationPage.class.isAssignableFrom(cls))
         {
             Constructor c = cls.getConstructor(new Class[] { NavigationTree.class });
             NavigationPage result = (NavigationPage) c.newInstance(new Object[] { getOwner() });
             result.getFlags().inherit(getFlags(), INHERIT_PAGE_FLAGS_FROM_PARENT);
             return result;
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
     public void addPage(NavigationPage page)
     {
         appendChild(page);
     }
 
     public NavigationErrorPage createErrorPage()
     {
         return new NavigationErrorPage(getOwner());
     }
 
     public NavigationErrorPage createErrorPage(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(NavigationErrorPage.class.isAssignableFrom(cls))
         {
             Constructor c = cls.getConstructor(new Class[] { NavigationTree.class });
            return (NavigationErrorPage) c.newInstance(new Object[] { this });
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
     public void registerErrorPage(NavigationErrorPage page)
     {
         errorPageDescendantsByQualifiedName.put(page.getQualifiedName(), page);
         if (getParent() != null)
             ((NavigationPage) getParent()).registerErrorPage(page);
         getOwner().registerErrorPage(page);
     }
 
     public void unregisterErrorPage(NavigationErrorPage page)
     {
         errorPageDescendantsByQualifiedName.remove(page.getQualifiedName());
         if (getParent() != null)
             ((NavigationPage) getParent()).unregisterErrorPage(page);
         getOwner().unregisterErrorPage(page);
     }
 
     public List getErrorPagesList()
     {
         return errorPagesList;
     }
 
     public Map getErrorPagesMap()
     {
         return errorPagesMap;
     }
 
     public void addErrorPage(NavigationErrorPage page)
     {
         page.setParent(this);
         errorPagesList.add(page);
         errorPagesMap.put(page.getName(), page);
         registerErrorPage(page);
     }
 
     public void removeErrorPage(NavigationErrorPage page)
     {
         errorPagesList.remove(page);
         errorPagesMap.remove(page.getName());
         unregisterErrorPage(page);
     }
 
     /**
      * Try to locate the error page that can handle a given exception. First, check if we have any registered pages
      * that handle the class of the exception, then check our ancestors. If we don't handle the given exception class
      * and neither do our ancestors, check the superclass of the exception in our list and our ancestors. Keep doing
      * the check until a navigation page is found. If a page is found the navigation context's error information will
      * be appropriately set.
      * @param t The exception that we would like to find a error page for
      * @return True if we found a page, false if no page was found
      */
     public boolean findErrorPage(NavigationContext nc, Throwable t)
     {
         if(t instanceof ServletException)
         {
             ServletException se = (ServletException) t;
             Throwable rootCause = se.getRootCause();
             if(rootCause != null)
             {
                 if(findErrorPage(nc, rootCause))
                     return true;
             }
         }
 
         // if we're dealing with a nested exception, check to see if one of the nested exceptions is something we
         // need to handle
         if(t instanceof NestableException)
         {
             NestableException ne = (NestableException) t;
             Throwable[] throwables = ne.getThrowables();
             for(int i = 0; i < throwables.length; i++)
             {
                 Throwable nestedException = throwables[i];
                 if(t.getClass() == nestedException.getClass()) // don't get stuck in an infinite loop
                     continue;
 
                 if(findErrorPage(nc, nestedException))
                     return true;
             }
         }
 
         Class exceptionClass = t.getClass();
         while(exceptionClass != null)
         {
             for(int i = 0; i < errorPagesList.size(); i++)
             {
                 NavigationErrorPage errorPage = (NavigationErrorPage) errorPagesList.get(i);
                 if(errorPage.canHandle(exceptionClass, false))
                 {
                     nc.setErrorPageException(errorPage, t, exceptionClass);
                     return true;
                 }
 
                 // check if we can handle of the interfaces of the current exception class
                 Class[] interfaces = exceptionClass.getInterfaces();
                 for(int intf = 0; intf < interfaces.length; intf++)
                 {
                     Class interfaceClass = interfaces[intf];
                     if(errorPage.canHandle(interfaceClass, false))
                     {
                         nc.setErrorPageException(errorPage, t, interfaceClass);
                         return true;
                     }
                 }
             }
 
             exceptionClass = exceptionClass.getSuperclass();
             if(! Throwable.class.isAssignableFrom(exceptionClass))
                 break;
         }
 
         NavigationPage parentPage = (NavigationPage) getParent();
         if(parentPage != null)
         {
             if(parentPage.findErrorPage(nc, t))
                 return true;
         }
 
         // if we get to here, neither we nor our ancestors know how to handle this exception so plead ignorance
         return false;
     }
 
     public NavigationPathFlags createFlags()
     {
         return new Flags();
     }
 
     public NavigationPath.State constructState()
     {
         return new State();
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public void finalizeContents()
     {
         super.finalizeContents();
 
         for(int i = 0; i < errorPagesList.size(); i++)
             ((NavigationErrorPage) errorPagesList.get(i)).finalizeContents();
 
         if(dialogNextActionProvider == null)
         {
             NavigationPage parent = (NavigationPage) getParent();
             while(parent != null && dialogNextActionProvider == null)
             {
                 dialogNextActionProvider = parent.getDialogNextActionProvider();
                 parent = (NavigationPage) parent.getParent();
             }
 
             if(dialogNextActionProvider == null)
                 dialogNextActionProvider = getOwner().getDialogNextActionProvider();
         }
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public void setRequireRequestParams(String params)
     {
         String[] paramNames = TextUtils.split(params, ",", true);
         for(int i = 0; i < paramNames.length; i++)
             requireRequestParams.add(paramNames[i]);
     }
 
     public void setRequireRequestParam(String param)
     {
         requireRequestParams.add(param);
     }
 
     public List getRequireRequestParams()
     {
         return requireRequestParams;
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public boolean isValid(NavigationContext nc)
     {
         List reqParams = getRequireRequestParams();
         if(reqParams.size() > 0)
         {
             ServletRequest request = nc.getRequest();
             for(int i = 0; i < reqParams.size(); i++)
             {
                 String name = (String) reqParams.get(i);
                 if(request.getParameter(name) == null)
                 {
                     nc.setMissingRequiredReqParam(name);
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     /**
      * Determines whether the NavigationPath is part of the active path.
      * @param  nc  A context primarily to obtain the Active NavigationPath.
      * @return  <code>true</code> if the NavigationPath object is:
      *              1. The Active NavigationPath.
      *              2. In the ancestor list of the Active NavigationPath.
      *              3. One of the Default Children.
      */
     public boolean isInActivePath(NavigationContext nc)
     {
         //get the current NavigationPath
         NavigationPath activePath = nc.getActivePage();
 
         if (getQualifiedName().equals(activePath.getQualifiedName())) return true;
 
         //get the parents and for each set the property of current to true
         List ancestors = activePath.getAncestorsList();
         for (int i = 0; i < ancestors.size(); i++)
         {
             NavigationPath checkPath = (NavigationPath) ancestors.get(i);
             if (getQualifiedName().equals(checkPath.getQualifiedName())) return true;
         }
 
         /*
         TODO: [SNS] I commented this out since it was causing problems in ConsoleNavigationSkin -- need to investigate
         //get the default children if any and set the property of current to true
         Map childrenMap = activePath.getChildrenMap();
         List childrenList = activePath.getChildrenList();
         while (!childrenMap.isEmpty() && !childrenList.isEmpty())
         {
             NavigationPath defaultChildPath = (NavigationPath) childrenMap.get(activePath.getDefaultChild());
             if (defaultChildPath == null)
                 defaultChildPath = (NavigationPath) childrenList.get(0);
 
             if (getQualifiedName().equals(defaultChildPath.getQualifiedName()))
                 return true;
 
             childrenMap = defaultChildPath.getChildrenMap();
             childrenList = defaultChildPath.getChildrenList();
         }
         */
 
         return false;
     }
 
     public void enterPage(NavigationContext nc) throws NavigationException
     {
         ValueSource assignParamsVS = getAssignStateParams();
         if(assignParamsVS != null)
         {
             String assignParams = assignParamsVS.getTextValue(nc);
             if(assignParams != null)
             {
                 NavigationPath.State state = nc.getActiveState();
                 try
                 {
                     HttpUtils.assignParamsToInstance(nc.getHttpRequest(), state, assignParams);
                 }
                 catch (Exception e)
                 {
                     throw new NavigationException(e);
                 }
             }
         }
 
         for (int i = 0; i < enterListeners.size(); i++)
             ((NavigationPageEnterListener) enterListeners.get(i)).enterNavigationPage(this, nc);
     }
 
     public void exitPage(NavigationContext nc)
     {
         for (int i = 0; i < exitListeners.size(); i++)
             ((NavigationPageExitListener) exitListeners.get(i)).exitNavigationPage(this, nc);
     }
 
     public void makeStateChanges(NavigationContext nc)
     {
         String pageFlagsParamValue = nc.getRequest().getParameter(getPageFlagsParamName());
         if(pageFlagsParamValue != null)
             nc.getActiveState().getFlags().setValue(pageFlagsParamValue, false);
         super.makeStateChanges(nc);
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     /**
      *  if we have children, get the first child that does not have focus rejected
      */
     public NavigationPage getFirstFocusableChild()
     {
         List childrenList = getChildrenList();
         if(childrenList.size() > 0)
         {
             for(int i = 0; i < childrenList.size(); i++)
             {
                 NavigationPage child = (NavigationPage) childrenList.get(i);
                 if(! child.getFlags().flagIsSet(Flags.REJECT_FOCUS | Flags.HIDDEN))
                     return child;
                 else
                     return child.getNextPath();
             }
         }
 
         return null;
     }
 
     /**
      * Return the next sibling that can be focused
      */
     public NavigationPage getNextFocusableSibling()
     {
         // if we get to here we either have no children or all our children don't allow focus
         NavigationPath parent = getParent();
         if(parent != null)
         {
             List siblings = parent.getChildrenList();
             int thisIndex = siblings.indexOf(this);
             if(thisIndex == -1)
                 throw new RuntimeException("Unable to find " + this + " in siblings list.");
 
             // find the first sibling that allows focus
             for(int i = thisIndex + 1; i < siblings.size(); i++)
             {
                 NavigationPage sibling = (NavigationPage) siblings.get(i);
                 if(! sibling.getFlags().flagIsSet(Flags.REJECT_FOCUS | Flags.HIDDEN))
                     return sibling;
                 else
                     return sibling.getNextPath();
             }
         }
 
         return null;
     }
 
     /**
      * Return the "next" path (the one immediately following this one). This method will try to obtain the parent node
      * of the given NavigationPath and find itself in the parent's list (its siblings).
      */
     protected NavigationPage getNextPath(boolean checkChildren)
     {
         NavigationPage parent = (NavigationPage) getParent();
         NavigationPage nextPath = checkChildren ? getFirstFocusableChild() : null;
         if(nextPath == null)
         {
             nextPath = getNextFocusableSibling();
             if(nextPath == null && parent != null)
                 nextPath = parent.getNextPath(false);
         }
         return nextPath;
     }
 
     /**
      * Return the "next" path (the one immediately following this one). This method will try to obtain the parent node
      * of the given NavigationPath and find itself in the parent's list (its siblings).
      */
     public NavigationPage getNextPath()
     {
         return getNextPath(true);
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public ValueSource getCaption()
     {
         return caption;
     }
 
     public void setCaption(ValueSource caption)
     {
         this.caption = caption;
     }
 
     public ValueSource getHeading()
     {
         return heading;
     }
 
     public void setHeading(ValueSource heading)
     {
         this.heading = heading;
     }
 
     public ValueSource getSubHeading()
     {
         return subHeading;
     }
 
     public void setSubHeading(ValueSource subHeading)
     {
         this.subHeading = subHeading;
     }
 
     public ValueSource getTitle()
     {
         return title;
     }
 
     public void setTitle(ValueSource title)
     {
         this.title = title;
     }
 
     public ValueSource getRedirect()
     {
         return redirect;
     }
 
     public void setRedirect(ValueSource redirect)
     {
         this.redirect = redirect;
     }
 
     public String getRedirectTarget()
     {
         return redirectTarget;
     }
 
     public void setRedirectTarget(String redirectTarget)
     {
         this.redirectTarget = redirectTarget;
     }
 
     public void setTarget(String redirectTarget)
     {
         setRedirectTarget(redirectTarget);
     }
 
     public ValueSource getBaseAttributes()
     {
         return baseAttributes;
     }
 
     public void setBaseAttributes(ValueSource baseAttributes)
     {
         this.baseAttributes = baseAttributes;
     }
 
     public ValueSource getForward()
     {
         return forward;
     }
 
     public void setForward(ValueSource forward)
     {
         this.forward = forward;
         getBodyType().setValue(NavigationPageBodyType.FORWARD);
     }
 
     public ValueSource getInclude()
     {
         return include;
     }
 
     public void setInclude(ValueSource include)
     {
         this.include = include;
         getBodyType().setValue(NavigationPageBodyType.INCLUDE);
     }
 
     public String getCaption(ValueContext vc)
     {
         ValueSource vs = getCaption();
         if(vs == null)
             return getName();
         else
             return vs.getTextValue(vc);
     }
 
     public String getHeading(ValueContext vc)
     {
         ValueSource vs = getHeading();
         if(vs == null)
             return getCaption(vc);
         else
             return vs.getTextValue(vc);
     }
 
     public String getTitle(ValueContext vc)
     {
         ValueSource vs = getTitle();
         if(vs == null)
             return getHeading(vc);
         else
             return vs.getTextValue(vc);
     }
 
     public String getSubHeading(ValueContext vc)
     {
         ValueSource vs = getSubHeading();
         if(vs == null)
             return null;
         else
             return vs.getTextValue(vc);
     }
 
     public String getUrl(HttpServletValueContext vc)
     {
         String result;
         ValueSource vs = getRedirect();
         if(vs == null)
         {
             HttpServletRequest request = vc.getHttpRequest();
             result = request.getContextPath() + request.getServletPath() + getQualifiedName();
         }
         else
             result = vs.getTextValue(vc);
 
         ValueSource retainParamsVS = getRetainParams();
         if(retainParamsVS != null)
             result = HttpUtils.appendParams(vc.getHttpRequest(), result, retainParamsVS.getTextValue(vc));
 
         return result;
     }
 
     public String constructAnchorAttributes(HttpServletValueContext vc)
     {
         StringBuffer sb = new StringBuffer("HREF=\"" + getUrl(vc) + "\"");
         String target = getRedirectTarget();
         if(target != null)
             sb.append("TARGET=\""+ target  +"\"");
         return sb.toString();
     }
 
     public ValueSource getAssignStateParams()
     {
         if(assignStateParams != null)
             return assignStateParams;
 
         if(! getFlags().flagIsSet(Flags.INHERIT_ASSIGN_STATE_PARAMS))
             return null;
 
         NavigationPage parentPage = (NavigationPage) getParent();
         if(parentPage != null)
             return parentPage.getAssignStateParams();
 
         return null;
     }
 
     public void setAssignStateParams(ValueSource assignStateParams)
     {
         this.assignStateParams = assignStateParams;
     }
 
     public ValueSource getRetainParams()
     {
         if(retainParams != null)
             return retainParams;
 
         if(! getFlags().flagIsSet(Flags.INHERIT_RETAIN_PARAMS))
             return null;
 
         NavigationPage parentPage = (NavigationPage) getParent();
         if(parentPage != null)
             return parentPage.getRetainParams();
 
         return null;
     }
 
     public void setRetainParams(ValueSource retainParams)
     {
         this.retainParams = retainParams;
     }
 
     public void setAssignAndRetainParams(ValueSource params)
     {
         setAssignStateParams(params);
         setRetainParams(params);
     }
 
     public Command getCommand()
     {
         return bodyCommand;
     }
 
     public void setCommand(Command command)
     {
         this.bodyCommand = command;
         getBodyType().setValue(NavigationPageBodyType.COMMAND);
         if(command instanceof HttpServletCommand && ((HttpServletCommand) command).isAbleToAffectNavigation())
             getFlags().setFlag(Flags.BODY_AFFECTS_NAVIGATION);
     }
 
     public ValueSource getCommandExpr()
     {
         return bodyCommandExpr;
     }
 
     public void setCommandExpr(ValueSource bodyCommandExpr)
     {
         this.bodyCommandExpr = bodyCommandExpr;
         getBodyType().setValue(NavigationPageBodyType.COMMAND);
         getFlags().setFlag(Flags.BODY_AFFECTS_NAVIGATION); // just to be safe, buffer the output in case it will be a dialog when evaluated
     }
 
     public String getPageFlagsParamName()
     {
         return pageFlagsParamName;
     }
 
     public void setPageFlagsParamName(String pageFlagsParamName)
     {
         this.pageFlagsParamName = pageFlagsParamName;
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public ValueSource getDialogNextActionUrl()
     {
         return dialogNextActionUrl;
     }
 
     public void setDialogNextActionUrl(ValueSource dialogNextActionUrl)
     {
         // if we have a specific next action provided, then we become our own provider
         addDialogNextActionProvider(this);
         this.dialogNextActionUrl = dialogNextActionUrl;
     }
 
     public String getDialogNextActionUrl(DialogContext dc, String defaultUrl)
     {
         return dialogNextActionUrl != null ? dialogNextActionUrl.getTextValue(dc) : defaultUrl;
     }
 
     /**
      * Gets the next action provider for this particular page. The next action represents the action to be performed
      * after dialog execution.
      * @return
      */
     public DialogNextActionProvider getDialogNextActionProvider()
     {
         return dialogNextActionProvider;
     }
 
     /**
      * Sets the next action provider for all dialogs executed by this navigation tree and all children
      * @param nextActionProvider
      */
     public void addDialogNextActionProvider(DialogNextActionProvider nextActionProvider)
     {
         dialogNextActionProvider = nextActionProvider;
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public NavigationPageBodyType getBodyType()
     {
         return bodyType;
     }
 
     public void setBodyType(NavigationPageBodyType bodyType)
     {
         this.bodyType = bodyType;
     }
 
     public NavigationPageBodyHandler createBodyHandler()
     {
         return new NavigationPageBodyDefaultHandler();
     }
 
     public void addBodyHandler(NavigationPageBodyHandler handler)
     {
         customHandlers.add(handler);
         getBodyType().setValue(NavigationPageBodyType.CUSTOM_HANDLER);
     }
 
     public HtmlLayoutPanel createPanels()
     {
         bodyPanel = new HtmlLayoutPanel();
         getBodyType().setValue(NavigationPageBodyType.PANEL);
         return bodyPanel;
     }
 
     public HtmlLayoutPanel getBodyPanel()
     {
         return bodyPanel;
     }
 
     public TemplateProcessor createBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addBody(TemplateProcessor templateProcessor)
     {
         bodyTemplate = templateProcessor;
         getBodyType().setValue(NavigationPageBodyType.TEMPLATE);
     }
 
     public TemplateProcessor getBodyTemplate()
     {
         return bodyTemplate;
     }
 
     public TemplateProcessor createMissingParamsBody()
     {
         return new com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor();
     }
 
     public void addMissingParamsBody(TemplateProcessor templateProcessor)
     {
         missingParamsBodyTemplate = templateProcessor;
     }
 
     public TemplateProcessor getMissingParamsBody()
     {
         return missingParamsBodyTemplate;
     }
 
     public boolean canHandlePage(NavigationContext nc)
     {
         return true;
     }
 
     public void handlePageMetaData(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         NavigationSkin skin = nc.getSkin();
         if(skin != null) skin.renderPageMetaData(writer, nc);
     }
 
     public void handlePageHeader(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         NavigationSkin skin = nc.getSkin();
         if(skin != null) skin.renderPageHeader(writer, nc);
     }
 
     public void handlePageBody(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         // see if dynamic commands should be allowed
         ServletRequest request = nc.getRequest();
 
         if(getFlags().flagIsSet(Flags.ALLOW_PAGE_CMD_PARAM))
         {
             String commandSpec = request.getParameter(AbstractHttpServletCommand.PAGE_COMMAND_REQUEST_PARAM_NAME);
             if(commandSpec != null)
             {
                 HttpServletCommand command = (HttpServletCommand) Commands.getInstance().getCommand(commandSpec);
                 try
                 {
                     command.handleCommand(writer, nc, false);
                 }
                 catch (CommandException e)
                 {
                     getLog().error("Command error in body", e);
                     throw new ServletException(e);
                 }
                 return;
             }
         }
 
         switch(getBodyType().getValueIndex())
         {
             case NavigationPageBodyType.NONE:
                 writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class but has no body.");
                 break;
 
             case NavigationPageBodyType.OVERRIDE:
                 writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class and is set as override class but does not override handlePageBody().");
                 break;
 
             case NavigationPageBodyType.CUSTOM_HANDLER:
                 for(int i = 0; i < customHandlers.size(); i++)
                     ((NavigationPageBodyHandler) customHandlers.get(i)).handleNavigationPageBody(this, writer, nc);
                 break;
 
             case NavigationPageBodyType.COMMAND:
                 ValueSource commandExpr = getCommandExpr();
                 if(commandExpr != null)
                 {
                     String commandText = commandExpr.getTextValue(nc);
                     if(commandText != null)
                     {
                         try
                         {
                             HttpServletCommand httpCommand = (HttpServletCommand) Commands.getInstance().getCommand(commandText);
                             httpCommand.handleCommand(writer, nc, false);
                             break;
                         }
                         catch (Exception e)
                         {
                             getLog().error("Command error in " + this.getClass().getName(), e);
                             throw new ServletException(e);
                         }
                     }
                 }
 
                 // if we get to here, we don't have an expression or the expression returned null so see if we have static
                 // command supplied
                 try
                 {
                     ((HttpServletCommand) getCommand()).handleCommand(writer, nc, false);
                 }
                 catch (CommandException e)
                 {
                     getLog().error("Command error in body", e);
                     throw new ServletException(e);
                 }
                 break;
 
             case NavigationPageBodyType.PANEL:
                 getBodyPanel().render(writer, nc, nc.getActiveTheme(), HtmlPanel.RENDERFLAGS_DEFAULT);
                 break;
 
             case NavigationPageBodyType.TEMPLATE:
                 getBodyTemplate().process(writer, nc, null);
                 break;
 
             case NavigationPageBodyType.FORWARD:
                 // this should never happen -- forwards should never get to this point but we'll add a sanity check
                 writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class and the body type is set to FORWARD but forwarding should happen before any response is committed.");
                 break;
 
             case NavigationPageBodyType.INCLUDE:
                 {
                     String includeUrl = getInclude().getTextValue(nc);
                     RequestDispatcher rd = request.getRequestDispatcher(includeUrl);
                     ServletResponse response = nc.getResponse();
                     if(writer != response.getWriter())
                         response = new AlternateOutputDestServletResponse(writer, response);
                     request.setAttribute(REQATTRNAME_NAVIGATION_CONTEXT, nc);
                     rd.include(request, response);
                     request.removeAttribute(REQATTRNAME_NAVIGATION_CONTEXT);
                 }
                 break;
 
             default:
                 writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " but doesn't know how to handle body type " + getBodyType().getValueIndex() + ".");
         }
     }
 
     public void handlePageFooter(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         renderViewSource(writer, nc);
         NavigationSkin skin = nc.getSkin();
         if(skin != null) skin.renderPageFooter(writer, nc);
     }
 
     public boolean bodyAffectsNavigationContext(NavigationContext nc)
     {
         if(bodyPanel != null && bodyPanel.affectsNavigationContext(nc))
             return true;
         else
             return nc.getActiveState().getFlags().flagIsSet(Flags.BODY_AFFECTS_NAVIGATION);
     }
 
     public void handlePage(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         Flags flags = (Flags) nc.getActiveState().getFlags();
 
         enterPage(nc);
         if(getBodyType().getValueIndex() == NavigationPageBodyType.FORWARD)
         {
             // if we're forwarding to another resource we don't want to put anything into the response otherwise
             // there will be an illegal state exception -- so, we don't create headers, footers, etc because that's
             // the user's responsibility in the forwarded resource.
 
             String forwardUrl = getForward().getTextValue(nc);
             ServletRequest req = nc.getRequest();
             RequestDispatcher rd = req.getRequestDispatcher(forwardUrl);
             req.setAttribute(REQATTRNAME_NAVIGATION_CONTEXT, nc);
             rd.forward(req, nc.getResponse());
             req.removeAttribute(REQATTRNAME_NAVIGATION_CONTEXT);
         }
         else if(bodyAffectsNavigationContext(nc))
         {
             // render the body first and let it modify the navigation context
             StringWriter body = new StringWriter();
             boolean hasError = false;
             try
             {
                 handlePageBody(body, nc);
             }
             catch (Exception e)
             {
                 getLog().error("Error occurred while handling the page.", e);
                 if(! findErrorPage(nc, e))
                     nc.setErrorPageException(getOwner().getDefaultErrorPage(), e, e.getClass());
                 nc.getErrorPage().handlePage(writer, nc);
                 hasError = true;
             }
 
             if(! hasError && ! nc.isRedirected())
             {
                 if(flags.flagIsSet(Flags.HANDLE_META_DATA))
                     handlePageMetaData(writer, nc);
                 if(flags.flagIsSet(Flags.HANDLE_HEADER))
                     handlePageHeader(writer, nc);
                 writer.write(body.getBuffer().toString());
                 if(flags.flagIsSet(Flags.HANDLE_FOOTER))
                     handlePageFooter(writer, nc);
             }
 
             // try and do an early GC if possible
             body = null;
         }
         else
         {
             if(flags.flagIsSet(Flags.HANDLE_META_DATA))
                 handlePageMetaData(writer, nc);
             if(flags.flagIsSet(Flags.HANDLE_HEADER))
                 handlePageHeader(writer, nc);
             try
             {
                 handlePageBody(writer, nc);
             }
             catch (Exception e)
             {
                 getLog().error("Error occurred while handling the page.", e);
                 if(! findErrorPage(nc, e))
                     nc.setErrorPageException(getOwner().getDefaultErrorPage(), e, e.getClass());
                 nc.getErrorPage().handlePageBody(writer, nc);
             }
             if(flags.flagIsSet(Flags.HANDLE_FOOTER))
                 handlePageFooter(writer, nc);
         }
         exitPage(nc);
     }
 
     public void handleInvalidPage(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         Flags flags = (Flags) nc.getActiveState().getFlags();
 
         enterPage(nc);
         if(flags.flagIsSet(Flags.HANDLE_META_DATA))
             handlePageMetaData(writer, nc);
         if(flags.flagIsSet(Flags.HANDLE_HEADER))
             handlePageHeader(writer, nc);
 
         TemplateProcessor templateProcessor = getMissingParamsBody();
         if(templateProcessor != null)
             templateProcessor.process(writer, nc, null);
         else
             writer.write("This page is missing some required parameters.");
 
         if(flags.flagIsSet(Flags.HANDLE_FOOTER))
             handlePageFooter(writer, nc);
         exitPage(nc);
     }
 
     public void renderViewSource(Writer writer, NavigationContext nc) throws IOException
     {
         if(getFlags().flagIsSet(Flags.ALLOW_VIEW_SOURCE))
         {
             writer.write("<p>");
             AbstractPanel.renderXdmObjectViewSource(writer, nc, getQualifiedNameIncludingTreeId() + " Page XDM Code", this.getClass(), getQualifiedNameIncludingTreeId(), getInputSourceLocator());
         }
     }
 }
