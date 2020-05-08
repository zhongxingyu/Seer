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
 * $Id: NavigationPage.java,v 1.21 2003-05-24 20:28:36 shahid.shah Exp $
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
 import com.netspective.sparx.value.HttpServletValueContext;
 import com.netspective.sparx.panel.HtmlLayoutPanel;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.BasicHtmlPanelValueContext;
 import com.netspective.sparx.util.HttpUtils;
 import com.netspective.sparx.template.TemplateProcessor;
 import com.netspective.sparx.command.AbstractHttpServletCommand;
 import com.netspective.sparx.command.HttpServletCommand;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class NavigationPage extends NavigationPath implements TemplateConsumer
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     public static final Log log = LogFactory.getLog(NavigationPage.class);
     public static final XdmBitmaskedFlagsAttribute.FlagDefn[] FLAG_DEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[NavigationPath.FLAG_DEFNS.length + 7];
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[] { "name" };
 
     static
     {
         for(int i = 0; i < NavigationPath.FLAG_DEFNS.length; i++)
             FLAG_DEFNS[i] = NavigationPath.FLAG_DEFNS[i];
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 0] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "REJECT_FOCUS", Flags.REJECT_FOCUS);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 1] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_PRIVATE, "HAS_BODY", Flags.HAS_BODY);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 2] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "HIDDEN", Flags.HIDDEN);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 3] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "ALLOW_PAGE_CMD_PARAM", Flags.ALLOW_PAGE_CMD_PARAM);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 4] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_PRIVATE, "HAS_CONDITIONAL_ACTIONS", Flags.HAS_CONDITIONAL_ACTIONS);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 5] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_RETAIN_PARAMS", Flags.INHERIT_RETAIN_PARAMS);
         FLAG_DEFNS[NavigationPath.FLAG_DEFNS.length + 6] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "INHERIT_ASSIGN_STATE_PARAMS", Flags.INHERIT_ASSIGN_STATE_PARAMS);
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
 
     public class Flags extends NavigationPath.Flags
     {
         public static final int REJECT_FOCUS = NavigationPath.Flags.START_CUSTOM;
         public static final int HAS_BODY = REJECT_FOCUS * 2;
         public static final int HIDDEN = HAS_BODY * 2;
         public static final int ALLOW_PAGE_CMD_PARAM = HIDDEN * 2;
         public static final int HAS_CONDITIONAL_ACTIONS = ALLOW_PAGE_CMD_PARAM * 2;
         public static final int INHERIT_RETAIN_PARAMS = HAS_CONDITIONAL_ACTIONS * 2;
         public static final int INHERIT_ASSIGN_STATE_PARAMS = INHERIT_RETAIN_PARAMS * 2;
         public static final int START_CUSTOM = INHERIT_ASSIGN_STATE_PARAMS * 2;
 
         public Flags()
         {
             setFlag(ALLOW_PAGE_CMD_PARAM | INHERIT_RETAIN_PARAMS | INHERIT_ASSIGN_STATE_PARAMS);
         }
 
         public FlagDefn[] getFlagsDefns()
         {
             return FLAG_DEFNS;
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
     }
 
     public class State extends NavigationPath.State
     {
 
     }
 
     private TemplateConsumerDefn templateConsumer;
     private ValueSource caption;
     private ValueSource title;
     private ValueSource heading;
     private ValueSource subHeading;
     private ValueSource retainParams;
     private ValueSource assignStateParams;
     private ValueSource redirect;
     private HtmlLayoutPanel bodyPanel;
     private TemplateProcessor bodyTemplate;
     private Command bodyCommand;
     private List pageTypesConsumed = new ArrayList();
 
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
 
     /**
      * When inheriting pages, we want our child pages to be the same class as us
      */
     public NavigationPage createPage() throws InstantiationException, IllegalAccessException
     {
         return (NavigationPage) this.getClass().newInstance();
     }
 
     public void addPage(NavigationPage page)
     {
         appendChild(page);
     }
 
     public NavigationPath.Flags createFlags()
     {
         return new Flags();
     }
 
     public NavigationPath.State constructState()
     {
         return new State();
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public boolean isValid(NavigationContext nc)
     {
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
     }
 
     public void exitPage(NavigationContext nc)
     {
 
     }
 
     public void makeStateChanges(NavigationContext nc)
     {
 
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
     public NavigationPage getNextPath(boolean checkChildren)
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
             result = HttpUtils.appendParams(vc, result, retainParamsVS.getTextValue(vc));
 
         return result;
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
     }
 
     /* -------------------------------------------------------------------------------------------------------------*/
 
     public HtmlLayoutPanel createPanels()
     {
         bodyPanel = new HtmlLayoutPanel();
         getFlags().setFlag(Flags.HAS_BODY);
         return bodyPanel;
     }
 
     public TemplateProcessor createBody()
     {
         return new com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor();
     }
 
     public void addBody(TemplateProcessor templateProcessor)
     {
         bodyTemplate = templateProcessor;
         getFlags().setFlag(Flags.HAS_BODY);
     }
 
     public boolean requireLogin(NavigationContext nc)
     {
         return true;
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
         if(getFlags().flagIsSet(Flags.ALLOW_PAGE_CMD_PARAM))
         {
             String commandSpec = nc.getRequest().getParameter(AbstractHttpServletCommand.PAGE_COMMAND_REQUEST_PARAM_NAME);
             if(commandSpec != null)
             {
                 HttpServletCommand command = (HttpServletCommand) Commands.getInstance().getCommand(commandSpec);
                 try
                 {
                     command.handleCommand(writer, nc, false);
                 }
                 catch (CommandException e)
                 {
                     log.error("Command error in body", e);
                     throw new ServletException(e);
                 }
                 return;
             }
         }
 
         if(bodyCommand != null)
         {
             try
             {
                 ((HttpServletCommand) bodyCommand).handleCommand(writer, nc, false);
             }
             catch (CommandException e)
             {
                 log.error("Command error in body", e);
                 throw new ServletException(e);
             }
             return;
         }
 
         if(bodyPanel != null)
         {
             bodyPanel.render(writer, nc, nc.getActiveTheme(), HtmlPanel.RENDERFLAGS_DEFAULT);
             return;
         }
 
         if(bodyTemplate != null)
         {
             bodyTemplate.process(writer, nc);
             return;
         }
 
         writer.write("Path '"+ nc.getActivePathFindResults().getSearchedForPath() +"' is a " + this.getClass().getName() + " class but has no body.");
     }
 
     public void handlePageFooter(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         NavigationSkin skin = nc.getSkin();
         if(skin != null) skin.renderPageFooter(writer, nc);
     }
 
     public boolean bodyAffectsNavigationContext(NavigationContext nc)
     {
         if(bodyPanel != null && bodyPanel.affectsNavigationContext(nc))
             return true;
         else
             return false;
     }
 
     public void handlePage(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         enterPage(nc);
         if(bodyAffectsNavigationContext(nc))
         {
             // render the body first and let it modify the navigation context
             StringWriter body = new StringWriter();
             handlePageBody(body, nc);
 
             handlePageMetaData(writer, nc);
             handlePageHeader(writer, nc);
             writer.write(body.getBuffer().toString());
             handlePageFooter(writer, nc);
         }
         else
         {
             // render the body "inline" (no need to buffer) since the body doesn't affect the context
             //try
             //{
                 handlePageMetaData(writer, nc);
                 handlePageHeader(writer, nc);
             //if(!ComponentCommandFactory.handleDefaultBodyItem(nc.getServletContext(), nc.getServlet(), nc.getRequest(), nc.getResponse()))
                 handlePageBody(writer, nc);
                 handlePageFooter(writer, nc);
             //}
             //catch (ComponentCommandException e)
             //{
             //    throw new NavigationPageException(e);
             //}
         }
         exitPage(nc);
     }
 }
