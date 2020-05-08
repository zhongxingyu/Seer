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
 * $Id: NavigationPath.java,v 1.13 2003-01-28 20:48:50 roque.hernandez Exp $
  */
 
 package com.netspective.sparx.xaf.navigate;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.netspective.sparx.util.value.*;
 import com.netspective.sparx.util.ClassPath;
 
 public class NavigationPath
 {
     static public final long NAVGPATHFLAG_ISROOT    = 1;
     static public final long NAVGPATHFLAG_INVISIBLE = NAVGPATHFLAG_ISROOT * 2;
     static public final long NAVGPATHFLAG_HIDDEN = NAVGPATHFLAG_INVISIBLE * 2;
     static public final long NAVGPATHFLAG_READONLY = NAVGPATHFLAG_HIDDEN * 2;
     static public final long NAVGPATHFLAG_INITIAL_FOCUS = NAVGPATHFLAG_READONLY * 2;
     static public final long NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS = NAVGPATHFLAG_INITIAL_FOCUS * 2;
     static public final long FLDFLAG_STARTCUSTOM = NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS * 2;
 
     static public final String PATH_SEPARATOR = "/";
     static private int pathNumber = 0;
 
     /**
      * A Class that describes the Results of matching the Http Request with the available Paths in NavigationPath.
      */
     public class FindResults
     {
         private String searchForPath;
         private NavigationPath searched;
         private NavigationPath matchedPath;
         private String[] unmatchedItems;
 
         /**
          * Constructs a <code>FindResult</code> object that will contain the appropiate information regarding the matching up of
          * the path string in the search NavigationPath.
          * @param search  A <code>NavigationPath</code> object that will be used to search.
          * @param path  A <code>String</code> object that represents tha path that is being requested.
          */
         public FindResults(NavigationPath search, String path)
         {
             if (path == null || path.length() == 0)
                 path = "/";
 
             searchForPath = path;
             searched = search;
 
             matchedPath = (NavigationPath) search.getAbsolutePathsMap().get(path);
 
             if (matchedPath != null)
                 return;
 
             List unmatchedItemsList = new ArrayList();
             Map absPathsMap = search.getAbsolutePathsMap();
             String partialPath = path;
             boolean finished = false;
             while (matchedPath == null && !finished)
             {
                 int partialItemIndex = partialPath.lastIndexOf(PATH_SEPARATOR);
                 if (partialItemIndex == -1)
                 {
                     matchedPath = (NavigationPath) absPathsMap.get(partialPath);
                     if (matchedPath == null)
                         unmatchedItemsList.add(0, partialPath);
                     finished = true;
                 }
                 else
                 {
                     unmatchedItemsList.add(0, partialPath.substring(partialItemIndex + 1));
                     partialPath = partialPath.substring(0, partialItemIndex);
                     matchedPath = (NavigationPath) absPathsMap.get(partialPath);
                 }
             }
 
             unmatchedItems = (String[]) unmatchedItemsList.toArray(new String[unmatchedItemsList.size()]);
         }
 
         /**
          * Returns the requested path.
          * @return  String  A string that represents the requested path.
          */
         public String getSearchedForPath()
         {
             return searchForPath;
         }
 
         /**
          * Returns <code>NavigationPath</code> object that the requested path is being matched against.
          * @return  NavigationPath  The object that the path is being searched on.
          */
         public NavigationPath getSearchedInPath()
         {
             return searched;
         }
 
         /**
          * Returns the <code>NavigationPath</code> object which id matched the requested path, or there was an absolute path
          * registered with the string as its id.
          * @return NavigationPath
          */
         public NavigationPath getMatchedPath()
         {
             return matchedPath;
         }
 
         /**
          * Returns a String array that contains the portions of the path that could not be matched.
          * @return String[]  Array of unmatched path items
          */
         public String[] unmatchedPathItems()
         {
             return unmatchedItems;
         }
 
         /**
          * Returns a concatenated String of all of the elements of unmatchedPathItems with a "/" as a path separator.
          * @return String  Full unmatched path
          */
         public String getUnmatchedPath()
         {
             if (unmatchedItems == null || unmatchedItems.length == 0)
                 return null;
 
             StringBuffer result = new StringBuffer();
             for (int i = 0; i < unmatchedItems.length; i++)
             {
                 result.append(PATH_SEPARATOR);
                 result.append(unmatchedItems[i]);
             }
             return result.toString();
         }
     }
 
     private NavigationTree owner;
     private NavigationPath parent;
     private long flags;
     private String id;
     private String name;
     private SingleValueSource caption;
     private SingleValueSource title;
     private SingleValueSource heading;
     private SingleValueSource subHeading;
     private String actionImageUrl;
     private String entityImageUrl;
     private String controllerName;
     private List conditionalActions;
     private NavigationController controller;
     private List childrenList = new ArrayList();
     private Map childrenMap = new HashMap();
     private Map absPathMap = new HashMap();
     private String defaultChildId = null;
     private SingleValueSource url = null;
     private Map ancestorMap = new HashMap();
     private List ancestorsList = new ArrayList();
     private ValueSource retainParams = new ConfigurationExprValue();
     private boolean hasRetainParams = false;
     private int level = 0;
     private int maxLevel = 0;
 
     public NavigationPath()
     {
         pathNumber++;
         setName("navigation_path_" + pathNumber);
     }
 
     public NavigationPath(String name)
     {
         this();
         setName(name);
     }
 
     public boolean isRoot()
     {
         return flagIsSet(NAVGPATHFLAG_ISROOT);
     }
 
     public void setRoot(boolean root)
     {
         if(root) setFlag(NAVGPATHFLAG_ISROOT); else clearFlag(NAVGPATHFLAG_ISROOT);
         if(root)
         {
             setName(null);
             setParent(null);
         }
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String value)
     {
         id = value;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getCaption(ValueContext vc)
     {
         return caption.getValue(vc);
     }
 
     public void setCaption(String value)
     {
         caption = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     public String getTitle(ValueContext vc)
     {
         return title.getValue(vc);
     }
 
     public void setTitle(String value)
     {
         title = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     public String getHeading(ValueContext vc)
     {
         return heading.getValue(vc);
     }
 
     public void setHeading(String value)
     {
         heading = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     public NavigationTree getOwner()
     {
         return owner;
     }
 
     public void setOwner(NavigationTree value)
     {
         owner = value;
     }
 
     public NavigationPath getParent()
     {
         return parent;
     }
 
     public void setParent(NavigationPath value)
     {
         if(value != this)
         {
             parent = value;
             if(parent != null)
                 setLevel(parent.getLevel()+1);
         }
         else
             parent = null;
     }
 
     public int getLevel()
     {
         return level;
     }
 
     public void setLevel(int level)
     {
         this.level = level;
         setMaxLevel(level);
         for(NavigationPath activeParent = getParent(); activeParent != null; )
         {
             activeParent.setMaxLevel(level);
             activeParent = activeParent.getParent();
         }
         if(owner != null) owner.setMaxLevel(level);
     }
 
     public int getMaxLevel()
     {
         return maxLevel;
     }
 
     public void setMaxLevel(int maxLevel)
     {
         if(maxLevel > this.maxLevel)
             this.maxLevel = maxLevel;
     }
 
     public String getActionImageUrl()
     {
         return actionImageUrl;
     }
 
     public void setActionImageUrl(String actionImageUrl)
     {
         this.actionImageUrl = actionImageUrl;
     }
 
     public String getEntityImageUrl()
     {
         return entityImageUrl;
     }
 
     public void setEntityImageUrl(String entityImageUrl)
     {
         this.entityImageUrl = entityImageUrl;
     }
 
     public Map getAbsolutePathsMap()
     {
         return absPathMap;
     }
 
     public final long getFlags()
     {
         return flags;
     }
 
     public boolean flagIsSet(long flag)
     {
         return (flags & flag) == 0 ? false : true;
     }
 
     public void setFlag(long flag)
     {
         flags |= flag;
     }
 
     public void clearFlag(long flag)
     {
         flags &= ~flag;
     }
 
     public void setFlagRecursively(long flag)
     {
         flags |= flag;
         if(childrenList.size() > 0)
         {
             Iterator i = childrenList.iterator();
             while(i.hasNext())
                 ((NavigationPath) i.next()).setFlag(flag);
         }
     }
 
     public void clearFlagRecursively(long flag)
     {
         flags &= ~flag;
         if(childrenList.size() > 0)
         {
             Iterator i = childrenList.iterator();
             while(i.hasNext())
                 ((NavigationPath) i.next()).clearFlag(flag);
         }
     }
 
     /**
      * Returns the Map that contains all of its sibilings including itself.  It is basically obtained by getting a
      * reference to the parent and then get a map of all of its children.
      * @return  Map  A map object containing NavigationPath objects that represent the sibilings of the current object.
      */
     public Map getSibilingMap()
     {
         if (parent != null)
             return parent.getChildrenMap();
 
         if (owner != null)
             return owner.getChildrenMap();
 
         return null;
     }
 
     /**
      * Returns the List that contains all of its sibilings including itself.  It is basically obtained by getting a
      * reference to the parent and then get a list of all of its children.
      * @return  List  A list object containing NavigationPath objects that represent the sibilings of the current object.
      */
     public List getSibilingList()
     {
         if (parent != null)
             return parent.getChildrenList();
 
         if (owner != null)
             return owner.getChildrenList();
 
         return null;
     }
 
     /**
      * A method that returns the default class used to represent a path available.
      * @return Class Default class of the avaiable path
      */
     public Class getChildPathClass()
     {
         return NavigationPath.class;
     }
 
     /**
      * A method that returns the object to represent a new path.
      * This method can be overridden to allow the placement of other objects of type <code>NavigationPath</code>.
      * @return NavigationPath
      */
     public NavigationPath createChildPathInstance()
     {
         return new NavigationPath();
     }
 
     public void importFromXml(Element elem, NavigationPath parent)
     {
         Class defaultChildClass = getChildPathClass();
 
         NodeList children = elem.getChildNodes();
         for (int c = 0; c < children.getLength(); c++)
         {
             Node child = children.item(c);
             String childName = child.getNodeName();
 
             if (child.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             Element childElem = (Element) child;
 
             if(childName.equals("conditional"))
             {
                 importConditionalFromXml(childElem, parent);
                 continue;
             }
 
             if (childElem.getNodeName().equals("page"))
             {
                 String childClassName = childElem.getAttribute("class");
                 NavigationPath childPath = null;
                 if(childClassName.length() > 0)
                 {
                     ClassPath.InstanceGenerator instGen = new ClassPath.InstanceGenerator(childClassName, defaultChildClass, true);
                     childPath = (NavigationPath) instGen.getInstance();
                 }
                 else
                 {
                     childPath = createChildPathInstance();
                     childElem.setAttribute("class", childPath.getClass().getName());
                 }
 
                 if (parent.isRoot())
                     childPath.setOwner((NavigationTree) parent);
                 else
                     childPath.setOwner(parent.getOwner());
 
                 childPath.setParent(parent);
                 String id = childElem.getAttribute("id");
                 childPath.setId(id);
                 parent.getChildrenMap().put(childPath.getId(), childPath);
                 parent.getChildrenList().add(childPath);
                 parent.register(childPath);
 
                 String name = childElem.getAttribute("name");
                 if(name.length() > 0)
                     childPath.setName(name);
                 else
                 {
                     String[] pathItems = getPathItems(id);
                     childPath.setName(pathItems[pathItems.length-1]);
                 }
 
                 String caption = childElem.getAttribute("caption");
                 childPath.setCaption(caption);
 
                 String heading = childElem.getAttribute("heading");
                 if (heading.length() == 0)
                 {
                     heading = caption;
                     childElem.setAttribute("heading", caption);
                 }
 
                childPath.setHeading(heading);

                 String subHeading = childElem.getAttribute("sub-heading");
                 if (subHeading != null && subHeading.length() > 0)
                 {
                     childPath.setSubHeading(subHeading);
                 }
 
                 String title = childElem.getAttribute("title");
                 if (title.length() == 0)
                 {
                     title = heading;
                     childElem.setAttribute("title", heading);
                 }
                 childPath.setTitle(title);
 
                 String controllerName = childElem.getAttribute("controller");
                 NavigationController controller = null;
 
                 if (controllerName != null && controllerName.length() > 0)
                 {
                     controller = childPath.getOwner().getControllerByName(controllerName);
 
                 }
 
                 if (childPath.getParent().getController() != null) {
                     controllerName = childPath.getParent().getControllerName();
                     controller =  childPath.getParent().getController();
                 } else {
                     controllerName = "default";
                     controller = childPath.getOwner().getControllerByName(controllerName);
                 }
 
                 childPath.setControllerName(controllerName);
                 childPath.setController(controller);
 
                 String url = childElem.getAttribute("url");
                 if (url == null || url.length() == 0)
                 {
                     url = "config-expr:${create-app-url:" + controller.getUrl() + id + "}" + (controller.getRetainParamsSource() != null || controller.getRetainParamsSource().length() > 0 ? "?" + controller.getRetainParamsSource() : "");
                     childElem.setAttribute("url", id);
                 }
                 childPath.setUrl(url);
 
                 String permissions = childElem.getAttribute("permissions");
                 if (permissions != null && permissions.length() > 0)
                 {
                     NavigationConditionalApplyFlag permissionConditional = new NavigationConditionalApplyFlag(childPath);
                     permissionConditional.setNavigationPathFlag(NAVGPATHFLAG_INVISIBLE);
 
                     List permsList = new ArrayList();
                     StringTokenizer st = new StringTokenizer(permissions, ",");
                     while(st.hasMoreTokens())
                         permsList.add(st.nextToken());
                     permissionConditional.setHasPermissions((String[]) permsList.toArray(new String[permsList.size()]));
 
                     childPath.addConditionalAction(permissionConditional);
                 }
 
                 //TODO: It would simplfy the XML definition if we add a url-params if you want to have the defual URL but only define some params, which is in most cases what you want to do.
 
                 //TODO: develop a value source that will do the page hiding depending on entity.
 
                 //TODO: Asses if it's worth to inherit retain-params and a way to set them to nothing and make sure to take care of double entries.
                 String retainParams = childElem.getAttribute("retain-params");
                 if (retainParams != null && retainParams.length() > 0) {
                     childPath.setRetainParams(retainParams);
                 }
 
                 String visible = childElem.getAttribute("visible");
                 if (visible != null && "no".equals(visible))
                     childPath.setFlag(NAVGPATHFLAG_INVISIBLE);
 
                 String defaultChildId = childElem.getAttribute("default");
                 childPath.setDefaultChildId(defaultChildId);
 
                 //TODO: Tried to remove these statement and the variables but it's
                 //      being used in ACE.  Need to alter ACE to follow the new image
                 //      naming convention
                 String imageUrl = childElem.getAttribute("action-image-url");
                 if (imageUrl.length() > 0)
                     childPath.setActionImageUrl(url);
 
                 imageUrl = childElem.getAttribute("entity-image-url");
                 if (imageUrl.length() > 0)
                     childPath.setEntityImageUrl(url);
 
                 //if this is the first child then if there is no default defined for the parent set this id as the
                 //default.
                 if (c == 0)
                 {
                     String parentDefaultChildId = parent.getDefaultChildId();
                     if (parentDefaultChildId == null || parentDefaultChildId.length() == 0)
                         parent.setDefaultChildId(id);
                 }
 
                 childPath.generateAncestorList();
 
                 importFromXml(childElem, childPath);
             }
         }
     }
 
     public void importConditionalFromXml(Element elem, NavigationPath path)
     {
         String action = elem.getAttribute("action");
 
         if(action == null || action.length() == 0)
         {
             return;
         }
 
         NavigationConditionalAction actionInst = NavigationTreeManager.createConditional(action);
 
         if(actionInst != null)
         {
             actionInst.setPath(path);
             actionInst.importFromXml(elem);
             path.addConditionalAction(actionInst);
         }
     }
 
     public FindResults findPath(String path)
     {
         return new FindResults(this, path);
     };
 
     public String getAbsolutePath()
     {
         if (id != null)
             return id;
 
         String name = getName();
         StringBuffer path = name != null ? new StringBuffer(name) : new StringBuffer();
         NavigationPath active = getParent();
         while (active != null)
         {
             path.insert(0, PATH_SEPARATOR);
             String activeName = active.getName();
             if (activeName != null)
                 path.insert(0, activeName);
             active = active.getParent();
         }
         return path.toString();
     }
 
     public String getAbsolutePath(ValueContext vc)
     {
         String absPath = getAbsolutePath();
         if (vc == null)
             return absPath;
 
         HttpServletRequest request = (HttpServletRequest) vc.getRequest();
         return request.getContextPath() + request.getServletPath() + absPath;
     }
 
     public void register(NavigationPath path)
     {
         String absolutePath = path.getAbsolutePath();
         absPathMap.put(absolutePath, path);
         if (parent != null)
             parent.register(path);
         if (owner != null)
             owner.register(path);
     }
 
     protected NavigationPath addChild(String path, NavigationPath newChildInstance)
     {
         if (path == null || path.length() == 0 || path.equals("/"))
         {
             absPathMap.put("/", newChildInstance);
             if (parent != null) parent.getAbsolutePathsMap().put("/", newChildInstance);
             if (owner != null) owner.getAbsolutePathsMap().put("/", newChildInstance);
             return newChildInstance;
         }
 
         String[] items = getPathItems(path);
         return addChild(items, 0, newChildInstance);
     }
 
     protected NavigationPath addChild(String[] pathItems, int startIndex, NavigationPath newChildInstance)
     {
         String childName = pathItems[startIndex];
 
         NavigationPath child = (NavigationPath) childrenMap.get(childName);
         if (child == null)
         {
             child = newChildInstance != null ? newChildInstance : getChildPathInstance(childName);
             child.setOwner(owner);
             child.setParent(this);
 
             childrenMap.put(childName, child);
             childrenList.add(child);
             register(child);
             child.generateAncestorList();
         }
 
         if (startIndex < (pathItems.length - 1))
             return child.addChild(pathItems, startIndex + 1, newChildInstance);
         else
             return child;
     }
 
     /**
      * Get a child by its ID
      * @param id
      * @return NavigationPath
      */
     public NavigationPath getChildById(String id)
     {
         return (NavigationPath) childrenMap.get(id);
     }
 
     public List getChildrenList()
     {
         return childrenList;
     }
 
     public Map getChildrenMap()
     {
         return childrenMap;
     }
 
     public String getDebugHtml(ValueContext vc)
     {
         StringBuffer html = new StringBuffer();
         html.append(getAbsolutePath() + ": level " + getLevel() + " (max "+ getMaxLevel() +"), " + getClass().getName() + ", " + getCaption(vc) + ", " + getHeading(vc) + ", " + getTitle(vc));
 
         if(childrenList != null && childrenList.size() > 0)
         {
             html.append("<ol>Children");
             Iterator i = childrenList.iterator();
             while (i.hasNext())
             {
                 NavigationPath path = (NavigationPath) i.next();
                 html.append("<li>" + path.getDebugHtml(vc) + "</li>");
             }
             html.append("</ol>");
         }
 
         if(ancestorsList != null && ancestorsList.size() > 0)
         {
             html.append("<ol>Ancestors");
             Iterator i = ancestorsList.iterator();
             while (i.hasNext())
             {
                 NavigationPath path = (NavigationPath) i.next();
                 html.append("<li>" + path.getAbsolutePath(vc) + "</li>");
             }
             html.append("</ol>");
         }
 
         return html.toString();
     }
 
     static public String[] getPathItems(String path)
     {
         List items = new ArrayList();
         for (StringTokenizer st = new StringTokenizer(path, PATH_SEPARATOR); st.hasMoreTokens();)
             items.add(st.nextToken());
         return (String[]) items.toArray(new String[items.size()]);
     }
 
     /**
      * A method that returns the object to represent every path available.
      * This method can be overwritten to allow the placement of other objects of type <code>NavigationPath</code>.
      * @return NavigationPath
      */
     public NavigationPath getChildPathInstance(String name)
     {
         return new NavigationPath(name);
     }
 
     /**
      * A String that represents the Id of the default child.  This property is relevant when trying to determine which
      * elements are part of the active path when the current element is not a leaf of the tree.
      * @return String
      */
     public String getDefaultChildId()
     {
         return defaultChildId;
     }
 
     /**
      * A String that represents the Id of the default child.  This property is relevant when trying to determine which
      * elements are part of the active path when the current element is not a leaf of the tree.
      * @param  defaultChildId  The String representing the Id of the child that should be the default.
      */
     public void setDefaultChildId(String defaultChildId)
     {
         this.defaultChildId = defaultChildId;
     }
 
     /**
      * A ValueSource that represents what the URL of the element should be.  Unlike VIrtualPath, where its Id is the
      * URL that get rendered with the element, a NavigationPath's Id is simply to uniquely identify an element on the
      * tree.  If a url is not provided, the id is used for the url.
      * @return SingleValueSource
      */
     public SingleValueSource getUrl()
     {
         return url;
     }
 
     /**
      * A ValueSource that represents what the URL of the element should be.  Unlike VIrtualPath, where its Id is the
      * URL that get rendered with the element, a NavigationPath's Id is simply to uniquely identify an element on the
      * tree.  If a url is not provided, the id is used for the url.
      * @param  vc  An object of type ValueContext to enable us to get the value from a ValueSource.
      * @return String
      */
     public String getUrl(ValueContext vc)
     {
         return url != null ? url.getValue(vc) : ((HttpServletRequest) vc.getRequest()).getContextPath() + ((HttpServletRequest) vc.getRequest()).getServletPath() + getAbsolutePath();
     }
 
     /**
      * A ValueSource that represents what the URL of the element should be.  Unlike VIrtualPath, where its Id is the
      * URL that get rendered with the element, a NavigationPath's Id is simply to uniquely identify an element on the
      * tree.  If a url is not provided, the id is used for the url.
      * @param  url  The ValueSource to be used.
      */
     public void setUrl(SingleValueSource url)
     {
         this.url = url;
     }
 
     /**
      * A ValueSource that represents what the URL of the element should be.  Unlike VIrtualPath, where its Id is the
      * URL that get rendered with the element, a NavigationPath's Id is simply to uniquely identify an element on the
      * tree.  If a url is not provided, the id is used for the url.
      * @param  url  The String to obtain a reference to a ValueSource from the factory.
      */
     public void setUrl(String url)
     {
         this.url = ValueSourceFactory.getSingleOrStaticValueSource(url);
     }
 
     /**
      * A List that represents all of the ancestors of the current object.  This list is initialized by getting a
      * reference to the parents until the method getParent() returns null.  This List is relevant when determining what
      * elements to render on levels above the active path.
      * @return List
      */
     public List getAncestorsList()
     {
         return ancestorsList;
     }
 
     /**
      * Get the number of ancestors (parents) we have
      * @return int
      */
     public int getAncestorsCount()
     {
         return ancestorsList.size();
     }
 
     /**
      * A List that represents all of the ancestors of the current object.  This list is initialized by getting a
      * reference to the parents until the method getParent() returns null.  This List is relevant when determining what
      * elements to render on levels above the active path.
      * @param  ancestorsList  The List that represents all of the parents of the current object.
      */
     public void setAncestorsList(List ancestorsList)
     {
         this.ancestorsList = ancestorsList;
     }
 
     /**
      * A Map that represents all of the ancestors of the current object.  This list is initialized by getting a
      * reference to the parents until the method getParent() returns null.  This Map is relevant when determining what
      * elements to render on levels above the active path.
      * @return Map
      */
     public Map getAncestorMap()
     {
         return ancestorMap;
     }
 
     /**
      * A Map that represents all of the ancestors of the current object.  This map is initialized by getting a
      * reference to the parents until the method getParent() returns null.  This Map is relevant when determining what
      * elements to render on levels above the active path.
      * @param  ancestorMap  The Map that represents all of the parents of the current object.
      */
     public void setAncestorMap(Map ancestorMap)
     {
         this.ancestorMap = ancestorMap;
     }
 
     public String getControllerName() {
         return controllerName;
     }
 
     public void setControllerName(String controllerName) {
         this.controllerName = controllerName;
     }
 
     public NavigationController getController() {
         return controller;
     }
 
     public void setController(NavigationController controller) {
         this.controller = controller;
     }
 
     public ValueSource getRetainParams() {
         return retainParams;
     }
 
     public void setRetainParams(ValueSource retainParams) {
         this.retainParams = retainParams;
         hasRetainParams = true;
     }
 
     public String getRetainParams(ValueContext vc){
         if (hasRetainParams)
             return this.retainParams.getValue(vc);
         else
             return null;
     }
 
     public void setRetainParams(String retainParams){
         this.retainParams.initializeSource(retainParams);
         hasRetainParams = true;
     }
 
     public String getSubHeading(ValueContext vc) {
         return subHeading.getValue(vc);
     }
 
     public void setSubHeading(String value) {
         this.subHeading = ValueSourceFactory.getSingleOrStaticValueSource(value);;
     }
 
     /**
      * Determines whether the NavigationPath is part of the active path.
      * @param  nc  A context primarily to obtain the Active NavigationPath.
      * @return  <code>true</code> if the NavigationPath object is:
      *              1. The Active NavigationPath.
      *              2. In the ancestor list of the Active NavigationPath.
      *              3. One of the Default Children.
      */
     public boolean isInActivePath(NavigationPathContext nc)
     {
         //get the current NavigationPath
         NavigationPath currentNavTree = nc.getActivePath();
 
         if (this.getId().equals(currentNavTree.getId())) return true;
 
         //get the parents and for each set the property of current to true
         List ancestors = currentNavTree.getAncestorsList();
         for (int i = 0; i < ancestors.size(); i++)
         {
             NavigationPath navTree = (NavigationPath) ancestors.get(i);
             if (this.getId().equals(navTree.getId())) return true;
         }
 
         //get the default children if any and set the property of current to true
         Map childrenMap = currentNavTree.getChildrenMap();
         List childrenList = currentNavTree.getChildrenList();
         while (childrenMap != null && childrenList != null && !childrenMap.isEmpty() && !childrenList.isEmpty())
         {
             NavigationPath defaultChildNavTree = (NavigationPath) childrenMap.get(currentNavTree.getDefaultChildId());
             if (defaultChildNavTree == null)
             {
                 defaultChildNavTree = (NavigationPath) childrenList.get(0);
             }
             if (this.getId().equals(defaultChildNavTree.getId())) return true;
             childrenMap = defaultChildNavTree.getChildrenMap();
             childrenList = defaultChildNavTree.getChildrenList();
         }
         //get that children's children and set and keep going down the path
 
         return false;
     }
 
     /**
      * Populates the ancestorMap and ancestorList.  It looks at its parent and adds a refence to it in the collections.
      * It then keeps doing that, but now looking at its parent's parent until it returns null.  It then reverses the list
      * to maintain a 0=top structure.
      */
     private void generateAncestorList()
     {
         NavigationPath currentPath = this.getParent();
         if (this.getParent() != null)
         {
             List ancestorListReversed = new ArrayList();
             Map ancestorMap = new HashMap();
             while (currentPath != null)
             {
                 ancestorMap.put(currentPath.getId(), currentPath);
                 ancestorListReversed.add(currentPath);
                 currentPath = currentPath.getParent();
             }
             this.setAncestorMap(ancestorMap);
 
             List ancestorList = new ArrayList();
             for (int i = ancestorListReversed.size() - 1; i >= 0; i--)
             {
                 NavigationPath NavigationPath = (NavigationPath) ancestorListReversed.get(i);
                 ancestorList.add(NavigationPath);
             }
             this.setAncestorsList(ancestorList);
         }
     }
 
     public NavigationPath registerPage(String path, NavigationPage page)
     {
         page.setId(path);
         return addChild(path, page);
     }
 
     public void makeStateChanges(NavigationPathContext nc) {
         //The make state changes should affect the current navPath, its sibilings, its ancestors and the ancestor's sibilings and its children
         if (flagIsSet(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS))
             applyConditionals(conditionalActions, nc);
 
         List sibilings = this.getSibilingList();
         for (int i = 0; sibilings!= null && i < sibilings.size(); i++) {
             NavigationPath sibiling = (NavigationPath) sibilings.get(i);
             if (sibiling.flagIsSet(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(sibiling.getConditionalActions(), nc);
         }
 
         List ancestors = this.getAncestorsList();
         for (int i = 0; ancestors!= null && i < ancestors.size(); i++) {
             NavigationPath ancestor = (NavigationPath) ancestors.get(i);
             if (ancestor.flagIsSet(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(ancestor.getConditionalActions(), nc);
             List ancestorSibilings = ancestor.getSibilingList();
             for (int j = 0; ancestorSibilings != null && j < ancestorSibilings.size(); j++) {
                 NavigationPath ancestorSibiling = (NavigationPath) ancestorSibilings.get(j);
                 if (ancestorSibiling.flagIsSet(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS))
                     applyConditionals(ancestorSibiling.getConditionalActions(), nc);
             }
         }
 
         List children = this.getChildrenList();
         for (int i = 0; children!= null && i < children.size(); i++) {
             NavigationPath child = (NavigationPath) children.get(i);
             if (child.flagIsSet(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(child.getConditionalActions(), nc);
         }
     }
 
     public void applyConditionals(List conditionals, NavigationPathContext nc){
 
         if(conditionals != null)
         {
             for(int i = 0; i < conditionals.size(); i++)
             {
                 NavigationConditionalAction action = (NavigationConditionalAction) conditionals.get(i);
                 if(action instanceof NavigationConditionalApplyFlag)
                     ((NavigationConditionalApplyFlag) action).applyFlags(nc);
             }
         }
     }
 
     public void resolveResources(Map resources){
 
 		Map pageResources = (Map) resources.get(this.getId());
 		Map parentPageResources = (Map) resources.get(this.getParent().getId());
         if (parentPageResources == null) {
             //This will only happen for the first level where there is no parent and
             //we need to get the root resources.
             parentPageResources = (Map) resources.get("/");
         }
 
         if (pageResources == null ) {
             //We do not need to look at every ancestor and figure out, because the
             //the recursiveness occurs from top to bottom, so all of the parents should
             //have resolved their resources by the time we get to this point.
             resources.put(this.getId(), parentPageResources);
         } else {
 
             Map currentPageResources = new HashMap();
 
             if (parentPageResources != null)
                 currentPageResources.putAll(parentPageResources);
 
             currentPageResources.putAll(pageResources);
             resources.put(this.getId(), currentPageResources);
 		}
 
         if (this.childrenList != null) {
             for (int i = 0; i < childrenList.size(); i++) {
                 NavigationPath child = (NavigationPath) childrenList.get(i);
                 child.resolveResources(resources);
             }
         }
     }
 
     /**
      * Get a list of conditional actions
      *
      * @return List a list of conditional actions
      */
     public List getConditionalActions()
     {
         return conditionalActions;
     }
 
     /**
      * Adds a conditional action
      *
      * @param action conditional action object
      */
     public void addConditionalAction(NavigationConditionalAction action)
     {
         if (conditionalActions == null) {
             conditionalActions = new ArrayList();
             setFlag(NAVGPATHFLAG_HAS_CONDITIONAL_ACTIONS);
         }
 
         if (action != null)
             conditionalActions.add(action);
     }
 }
 
 
