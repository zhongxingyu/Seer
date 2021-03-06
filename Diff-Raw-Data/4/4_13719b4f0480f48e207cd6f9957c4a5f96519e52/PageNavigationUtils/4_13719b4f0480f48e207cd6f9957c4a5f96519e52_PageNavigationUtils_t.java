 /**
  * Copyright (C) 2009 eXo Platform SAS.
  * 
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  * 
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.exoplatform.portal.webui.navigation;
 
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.portal.config.UserACL;
 import org.exoplatform.portal.config.UserPortalConfigService;
 import org.exoplatform.portal.config.model.PageNavigation;
 import org.exoplatform.portal.config.model.PageNode;
 import org.exoplatform.portal.config.model.PortalConfig;
 import org.exoplatform.portal.mop.Visibility;
 import org.exoplatform.services.organization.User;
 import org.exoplatform.services.resources.ResourceBundleManager;
 import org.exoplatform.webui.application.WebuiRequestContext;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 /**
  * Created by The eXo Platform SARL
  * Author : Nhu Dinh Thuan
  *          nhudinhthuan@exoplatform.com
  * Jun 27, 2007  
  */
 public class PageNavigationUtils
 {
 
    public static void removeNode(List<PageNode> list, String uri)
    {
       if (list == null)
          return;
       for (PageNode pageNode : list)
       {
          if (pageNode.getUri().equalsIgnoreCase(uri))
          {
             list.remove(pageNode);
             return;
          }
       }
    }
 
    public static PageNode[] searchPageNodesByUri(PageNode node, String uri)
    {
       if (node.getUri().equals(uri))
          return new PageNode[]{null, node};
       if (node.getChildren() == null)
          return null;
       List<PageNode> children = node.getChildren();
       for (PageNode ele : children)
       {
          PageNode[] returnNodes = searchPageNodesByUri(ele, uri);
          if (returnNodes != null)
          {
             if (returnNodes[0] == null)
                returnNodes[0] = node;
             return returnNodes;
          }
       }
       return null;
    }
 
    public static PageNode[] searchPageNodesByUri(PageNavigation nav, String uri)
    {
       if (nav.getNodes() == null)
          return null;
       List<PageNode> nodes = nav.getNodes();
       for (PageNode ele : nodes)
       {
          PageNode[] returnNodes = searchPageNodesByUri(ele, uri);
          if (returnNodes != null)
             return returnNodes;
       }
       return null;
    }
 
    public static PageNode searchPageNodeByUri(PageNode node, String uri)
    {
       if (node.getUri().equals(uri))
          return node;
       if (node.getChildren() == null)
          return null;
       List<PageNode> children = node.getChildren();
       for (PageNode ele : children)
       {
          PageNode returnNode = searchPageNodeByUri(ele, uri);
          if (returnNode != null)
             return returnNode;
       }
       return null;
    }
 
    public static PageNode searchPageNodeByUri(PageNavigation nav, String uri)
    {
       if (nav.getNodes() == null)
          return null;
       List<PageNode> nodes = nav.getNodes();
       for (PageNode ele : nodes)
       {
          PageNode returnNode = searchPageNodeByUri(ele, uri);
          if (returnNode != null)
             return returnNode;
       }
       return null;
    }
 
    public static Object searchParentNode(PageNavigation nav, String uri)
    {
       if (nav.getNodes() == null)
          return null;
       int last = uri.lastIndexOf("/");
       String parentUri = "";
       if (last > -1)
          parentUri = uri.substring(0, uri.lastIndexOf("/"));
       for (PageNode ele : nav.getNodes())
       {
          if (ele.getUri().equals(uri))
             return nav;
       }
       if (parentUri.equals(""))
          return null;
       return searchPageNodeByUri(nav, parentUri);
    }
 
    // Still keep this method to have compatibility with legacy code
    public static PageNavigation filter(PageNavigation nav, String userName) throws Exception
    {
       return filterNavigation(nav, userName, false);
    }
 
    /**
     * 
     * @param nav
     * @param userName
     * @param acceptNonDisplayedNode
     * @return
     * @throws Exception
     */
    public static PageNavigation filterNavigation(PageNavigation nav, String userName, boolean acceptNonDisplayedNode) throws Exception
    {
       PageNavigation filter = nav.clone();
       filter.setNodes(new ArrayList<PageNode>());
       
       WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
       ExoContainer container = context.getApplication().getApplicationServiceContainer();
       UserPortalConfigService userService =
          (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
       UserACL userACL = (UserACL)container.getComponentInstanceOfType(UserACL.class);
 
       for (PageNode node : nav.getNodes())
       {
          PageNode newNode = filterNodeNavigation(node, userName, acceptNonDisplayedNode, userService, userACL);
          if (newNode != null)
             filter.addNode(newNode);
       }
       return filter;
    }
 
    /**
     * PageNode won't be processed in following cases:
     * 
     * Case 1: Node 's visibility is SYSTEM and the user is not superuser or he is superuser but acceptNonDisplayNode = false
     * 
     * Case 2: Node 's visibility is not SYSTEM but the node is not display and the acceptNonDisplayedNode = false
     * 
     * Case 3: Node has non null pageReference but the associated Page does not exist
     * 
     * 
     * @param startNode
     * @param userName
     * @param userService
     * @return
     * @throws Exception
     */
    private static PageNode filterNodeNavigation(PageNode startNode, String userName, boolean acceptNonDisplayedNode,
       UserPortalConfigService userService, UserACL userACL) throws Exception
    {
     
       Visibility nodeVisibility = startNode.getVisibility();
       String pageReference = startNode.getPageReference();
 
       boolean doNothingCase_1 = nodeVisibility == Visibility.SYSTEM && (!userACL.getSuperUser().equals(userName) || !acceptNonDisplayedNode);
       boolean doNothingCase_2 = nodeVisibility != Visibility.SYSTEM && !startNode.isDisplay() && !acceptNonDisplayedNode;
       boolean doNothingCase_3 = (pageReference != null) && (userService.getPage(pageReference, userName) == null);
 
       
       
       if (doNothingCase_1 || doNothingCase_2 || doNothingCase_3)
       {
          return null;
       }
 
       PageNode cloneStartNode = startNode.clone();
       ArrayList<PageNode> filteredChildren = new ArrayList<PageNode>();
 
       List<PageNode> children = startNode.getChildren();
 
       if (children != null)
       {
          for (PageNode child : children)
          {
             PageNode filteredChildNode = filterNodeNavigation(child, userName, acceptNonDisplayedNode, userService, userACL);
             if (filteredChildNode != null)
             {
                filteredChildren.add(filteredChildNode);
             }
          }
       }
 
      //If are only accepting displayed nodes and If the node has no child and it does not point to any Page, then null is return
      if (!acceptNonDisplayedNode && filteredChildren.size() == 0 && cloneStartNode.getPageReference() == null)
       {
          return null;
       }
       cloneStartNode.setChildren(filteredChildren);
       return cloneStartNode;
    }
 
    public static PageNode filter(PageNode node, String userName, boolean acceptNonDisplayedNode) throws Exception
    {
       WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
       ExoContainer container = context.getApplication().getApplicationServiceContainer();
       UserPortalConfigService userService =
          (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
       UserACL userACL = (UserACL)container.getComponentInstanceOfType(UserACL.class);
 
       return filterNodeNavigation(node, userName, acceptNonDisplayedNode, userService, userACL);
    }
 
    public static void localizePageNavigation(PageNavigation nav, Locale locale, ResourceBundleManager i18nManager)
    {
       if (nav.getOwnerType().equals(PortalConfig.USER_TYPE))
          return;
       ResourceBundle res =
          i18nManager.getNavigationResourceBundle(locale.getLanguage(), nav.getOwnerType(), nav.getOwnerId());
       for (PageNode node : nav.getNodes())
       {
          resolveLabel(res, node);
       }
    }
 
    private static void resolveLabel(ResourceBundle res, PageNode node)
    {
       node.setResolvedLabel(res);
       if (node.getChildren() == null)
          return;
       for (PageNode childNode : node.getChildren())
       {
          resolveLabel(res, childNode);
       }
    }
 
    public static PageNavigation findNavigationByID(List<PageNavigation> all_Navigations, int id)
    {
       for (PageNavigation nav : all_Navigations)
       {
          if (nav.getId() == id)
          {
             return nav;
          }
       }
       return null;
    }
 }
