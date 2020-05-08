 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.navigate;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.sparx.navigate.listener.NavigationPathFinalizeContentsListener;
 import com.netspective.sparx.navigate.listener.NavigationPathListener;
 import com.netspective.sparx.navigate.listener.NavigationPathListenerPlaceholder;
 import com.netspective.sparx.navigate.listener.NavigationPathMakeStateChangesListener;
 
 public class NavigationPath
 {
     public static final int INHERIT_PATH_FLAGS_FROM_PARENT = 0;
 
     static public final String PATH_SEPARATOR = "/";
 
     public class State
     {
         private NavigationPathFlags flags;
 
         public State()
         {
             this.flags = (NavigationPathFlags) NavigationPath.this.getFlags().cloneFlags();
             this.flags.setStateFlags(true);
         }
 
         public NavigationPathFlags getFlags()
         {
             return flags;
         }
 
         public NavigationPath getPath()
         {
             return NavigationPath.this;
         }
     }
 
     private Log log = LogFactory.getLog(NavigationPath.class);
     private Log securityLog;
     private NavigationTree owner;
     private NavigationPath parent;
     private NavigationPathFlags flags;
     private String qualifiedName;
     private String name;
     private List childrenList = new ArrayList();
     private Map childrenMap = new HashMap();
     private Map descendantsByQualifiedName = new HashMap();
     private NavigationConditionalActions conditionalActions = new NavigationConditionalActions();
     private Map ancestorMap = new HashMap();
     private List ancestorsList = new ArrayList();
     private List finalizeContentsListeners = new ArrayList();
     private List makeStateChangesListeners = new ArrayList();
     private NavigationPath defaultChild;
     private boolean defaultChildOfParent;
     private int maxChildLevel;
     private int level;
     private Map attributes = new HashMap();
 
     public NavigationPath(NavigationTree owner)
     {
         setOwner(owner);
         flags = createFlags();
     }
 
     public Log getLog()
     {
         return log;
     }
 
     public Log getSecurityLog()
     {
         return securityLog;
     }
 
     public Object getAttribute(String attributeId)
     {
         return attributes.get(attributeId);
     }
 
     public void removeAttribute(String attributeId)
     {
         attributes.remove(attributeId);
     }
 
     public void setAttribute(String attributeId, Object attributeValue)
     {
         attributes.put(attributeId, attributeValue);
     }
 
     /**
      * Calculate the absolute path of the given relativePath assuming it was relative to this path.
      *
      * @param relativePath The relative path to use
      *
      * @return The absolute path of the relative path based on this path
      */
     public String getAbsPathRelativeToThisPath(String relativePath)
     {
         StringBuffer result = new StringBuffer(getQualifiedName());
         if(!(result.charAt(result.length() - 1) == '/'))
             result.append('/');
         if(relativePath.startsWith("/"))
             result.append(relativePath.substring(1));
         else
             result.append(relativePath);
         return result.toString();
     }
 
     public int size()
     {
         int result = 1; // start with self
         for(int i = 0; i < childrenList.size(); i++)
             result += ((NavigationPath) childrenList.get(i)).size();
 
         return result;
     }
 
     public NavigationPathListener createListener()
     {
         return new NavigationPathListenerPlaceholder();
     }
 
     public void addListener(NavigationPathListener listener)
     {
         if(listener instanceof NavigationPathFinalizeContentsListener)
             finalizeContentsListeners.add(listener);
         else if(listener instanceof NavigationPathMakeStateChangesListener)
             makeStateChangesListeners.add(listener);
     }
 
     public void finalizeContents()
     {
         for(int i = 0; i < childrenList.size(); i++)
             ((NavigationPath) childrenList.get(i)).finalizeContents();
 
         for(int i = 0; i < finalizeContentsListeners.size(); i++)
             ((NavigationPathFinalizeContentsListener) finalizeContentsListeners.get(i)).finalizeNavigationPathContents(this);
 
         final String logName = getClass().getName() + "." + (getOwner().getName() + getQualifiedName()).replace('/', '.');
         log = LogFactory.getLog(logName);
         securityLog = LogFactory.getLog("security." + logName);
     }
 
     public void makeStateChanges(NavigationContext nc)
     {
         //The make state changes should affect the current navPath, its sibilings, its ancestors and the ancestor's sibilings and its children
         if(getFlags().flagIsSet(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS))
             applyConditionals(conditionalActions.getActions(), nc);
 
         List sibilings = this.getSibilingList();
         for(int i = 0; sibilings != null && i < sibilings.size(); i++)
         {
             NavigationPath sibiling = (NavigationPath) sibilings.get(i);
             if(sibiling.getFlags().flagIsSet(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(sibiling.getConditionals().getActions(), nc);
         }
 
         List ancestors = this.getAncestorsList();
         for(int i = 0; ancestors != null && i < ancestors.size(); i++)
         {
             NavigationPath ancestor = (NavigationPath) ancestors.get(i);
             if(ancestor.getFlags().flagIsSet(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(ancestor.getConditionals().getActions(), nc);
             List ancestorSibilings = ancestor.getSibilingList();
             for(int j = 0; ancestorSibilings != null && j < ancestorSibilings.size(); j++)
             {
                 NavigationPath ancestorSibiling = (NavigationPath) ancestorSibilings.get(j);
                 if(ancestorSibiling.getFlags().flagIsSet(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS))
                     applyConditionals(ancestorSibiling.getConditionals().getActions(), nc);
             }
         }
 
         List children = this.getChildrenList();
         for(int i = 0; children != null && i < children.size(); i++)
         {
             NavigationPath child = (NavigationPath) children.get(i);
             if(child.getFlags().flagIsSet(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS))
                 applyConditionals(child.getConditionals().getActions(), nc);
         }
 
         for(int i = 0; i < makeStateChangesListeners.size(); i++)
             ((NavigationPathMakeStateChangesListener) makeStateChangesListeners.get(i)).makeNavigationPathStateChanges(this, nc);
     }
 
     public State constructState()
     {
         return new State();
     }
 
     public String getQualifiedName()
     {
         if(null == qualifiedName)
         {
             StringBuffer sb = new StringBuffer();
             if(parent != null)
                 sb.append(parent.getQualifiedName());
 
             if(sb.length() == 0 || sb.charAt(sb.length() - 1) != '/')
                 sb.append(PATH_SEPARATOR);
 
             sb.append(getName());
 
             setQualifiedName(sb.toString());
         }
 
         return qualifiedName;
     }
 
     public String getQualifiedNameIncludingTreeId()
     {
         return "/" + owner.getName() + getQualifiedName();
     }
 
     public void setQualifiedName(String qName)
     {
         qualifiedName = qName;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getName()
     {
         return name;
     }
 
     public NavigationTree getOwner()
     {
         return owner;
     }
 
     protected void setOwner(NavigationTree value)
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
             setLevel(parent.getLevel() + 1);
             if(defaultChildOfParent)
                 parent.setDefaultChild(this);
             generateAncestorList();
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
         setMaxChildLevel(level);
         for(NavigationPath activeParent = getParent(); activeParent != null;)
         {
             activeParent.setMaxChildLevel(level);
             activeParent = activeParent.getParent();
         }
     }
 
     public int getMaxChildLevel()
     {
         return maxChildLevel;
     }
 
     public void setMaxChildLevel(int maxChildLevel)
     {
         if(maxChildLevel > this.maxChildLevel)
             this.maxChildLevel = maxChildLevel;
         owner.setMaxLevel(level);
     }
 
     /**
      * Get a list of conditional actions
      *
      * @return List a list of conditional actions
      */
     public NavigationConditionalActions getConditionals()
     {
         return conditionalActions;
     }
 
     public void addConditional(NavigationConditionalAction action)
     {
         conditionalActions.addAction(action);
         getFlags().setFlag(NavigationPathFlags.HAS_CONDITIONAL_ACTIONS);
     }
 
     public NavigationConditionalAction createConditional()
     {
         return new NavigationConditionalAction(this);
     }
 
     public NavigationConditionalAction createConditional(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(NavigationConditionalAction.class.isAssignableFrom(cls))
         {
             Constructor c = cls.getConstructor(new Class[]{NavigationPath.class});
             return (NavigationConditionalAction) c.newInstance(new Object[]{this});
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
     public void applyConditionals(List conditionals, NavigationContext nc)
     {
         if(conditionals != null)
         {
             for(int i = 0; i < conditionals.size(); i++)
             {
                 NavigationConditionalAction action = (NavigationConditionalAction) conditionals.get(i);
                 action.execute(nc);
             }
         }
     }
 
     public NavigationPathFlags createFlags()
     {
         return new NavigationPathFlags();
     }
 
     public NavigationPathFlags getFlags()
     {
         return flags;
     }
 
     public void setFlags(NavigationPathFlags flags)
     {
         this.flags.copy(flags);
     }
 
     public void setFlagRecursively(long flag)
     {
         //flags.setFlag(flag);
         if(childrenList.size() > 0)
         {
             Iterator i = childrenList.iterator();
             while(i.hasNext())
                 ((NavigationPath) i.next()).getFlags().setFlag(flag);
         }
     }
 
     public void clearFlagRecursively(long flag)
     {
         //flags.clearFlag(flag);
         if(childrenList.size() > 0)
         {
             Iterator i = childrenList.iterator();
             while(i.hasNext())
                 ((NavigationPath) i.next()).getFlags().clearFlag(flag);
         }
     }
 
     /**
      * Returns the Map that contains all of its sibilings including itself.  It is basically obtained by getting a
      * reference to the parent and then get a map of all of its children.
      *
      * @return Map  A map object containing NavigationPath objects that represent the sibilings of the current object.
      */
     public Map getSibilingMap()
     {
         if(parent != null)
             return parent.getChildrenMap();
 
         return null;
     }
 
     /**
      * Returns the List that contains all of its sibilings including itself.  It is basically obtained by getting a
      * reference to the parent and then get a list of all of its children.
      *
      * @return List  A list object containing NavigationPath objects that represent the sibilings of the current object.
      */
     public List getSibilingList()
     {
         if(parent != null)
             return parent.getChildrenList();
 
         return null;
     }
 
     public void registerChild(NavigationPath path)
     {
         descendantsByQualifiedName.put(path.getQualifiedName(), path);
         if(parent != null)
             parent.registerChild(path);
         owner.register(path);
     }
 
     public void unregisterChild(NavigationPath path)
     {
         descendantsByQualifiedName.remove(path.getQualifiedName());
         if(parent != null)
             parent.unregisterChild(path);
         owner.unregister(path);
     }
 
     public void appendChild(NavigationPath path)
     {
         path.setParent(this);
         childrenList.add(path);
         childrenMap.put(path.getName(), path);
         registerChild(path);
     }
 
     public void removeChild(NavigationPath path)
     {
         childrenList.remove(path);
         childrenMap.remove(path.getName());
         unregisterChild(path);
     }
 
     public void removeAllChildren()
     {
         if(childrenList.size() == 0)
             return;
 
         NavigationPath[] children = (NavigationPath[]) childrenList.toArray(new NavigationPath[childrenList.size()]);
         for(int i = 0; i < children.length; i++)
             removeChild(children[i]);
     }
 
     /**
      * Get a child by its ID
      *
      * @return NavigationPath
      */
     public NavigationPath getChildByName(String id)
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
 
     public NavigationPath getDefaultChild()
     {
         return defaultChild;
     }
 
     public void setDefaultChild(NavigationPath defaultChild)
     {
         this.defaultChild = defaultChild;
     }
 
     public boolean isDefault()
     {
         return defaultChildOfParent;
     }
 
     public void setDefault(boolean defaultChildOfParent)
     {
         this.defaultChildOfParent = defaultChildOfParent;
         if(defaultChildOfParent && getParent() != null)
             getParent().setDefaultChild(this);
     }
 
     public Map getAncestorMap()
     {
         return ancestorMap;
     }
 
     public void setAncestorMap(Map ancestorMap)
     {
         this.ancestorMap = ancestorMap;
     }
 
     public List getAncestorsList()
     {
         return ancestorsList;
     }
 
     public void setAncestorsList(List ancestorsList)
     {
         this.ancestorsList = ancestorsList;
     }
 
     /**
      * Populates the ancestorMap and ancestorList.  It looks at its parent and adds a refence to it in the collections.
      * It then keeps doing that, but now looking at its parent's parent until it returns null.  It then reverses the list
      * to maintain a 0=top structure.
      */
     protected void generateAncestorList()
     {
         NavigationPath activePath = this.getParent();
         if(getParent() != null)
         {
             List ancestorListReversed = new ArrayList();
             Map ancestorMap = new HashMap();
             while(activePath != null)
             {
                 ancestorMap.put(activePath.getQualifiedName(), activePath);
                 ancestorListReversed.add(activePath);
                 activePath = activePath.getParent();
             }
             setAncestorMap(ancestorMap);
 
             List ancestorList = new ArrayList();
             for(int i = ancestorListReversed.size() - 1; i >= 0; i--)
             {
                 NavigationPath NavigationPath = (NavigationPath) ancestorListReversed.get(i);
                 ancestorList.add(NavigationPath);
             }
             setAncestorsList(ancestorList);
         }
     }
 
     public String toString()
     {
         StringBuffer html = new StringBuffer();
         int atLevel = getLevel();
         for(int i = 0; i < atLevel; i++)
             html.append("  ");
 
         html.append(getQualifiedName() + ": level " + getLevel() + " (max " + getMaxChildLevel() + "), class: " + getClass().getName() + "\n");
 
         if(childrenList != null && childrenList.size() > 0)
         {
             Iterator i = childrenList.iterator();
             while(i.hasNext())
             {
                 NavigationPath path = (NavigationPath) i.next();
                 html.append(path.toString());
             }
         }
 
         return html.toString();
     }
 }
