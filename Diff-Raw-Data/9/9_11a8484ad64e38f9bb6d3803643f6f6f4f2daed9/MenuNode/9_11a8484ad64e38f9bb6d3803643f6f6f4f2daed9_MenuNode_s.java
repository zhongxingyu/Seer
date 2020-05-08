 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.thruzero.common.web.model.nav;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.thruzero.common.core.infonode.InfoNodeElement;
 import com.thruzero.common.core.locator.ConfigLocator;
 import com.thruzero.common.web.util.WebUtils;
 
 /**
  * An instance represents a menu and/or menu-item, where a menu typically contains children whereas a menu-item doesn't.
  * Each MenuNode has a parent, a title and supports an optional payload of data (InfoNodeElement).
  *
  * Example uses:
  * <ul>
  *   <li>A particular menu or tab for navigation or context selection</li>
  *   <li>A particular menu item or sub-tab for navigation or context selection</li>
  * </ul>
  *
  * @author George Norman
  */
 public class MenuNode implements MenuStateHolder {
   private static final String PATH_SEPARATOR = ConfigLocator.locate().getValue(MenuNode.class.getName(), "pathSeparator", ".");
 
   private MenuStateHolder parent;
 
   /** Path of this menu element. */
   private MenuNodePath path;
 
   /** Title of this menu element. */
   private String title;
 
   /** Content associated with this menu element. */
   private InfoNodeElement payload;
 
   /** Child menu elements. */
   private Map<String, MenuNode> childNodes;
 
   // ------------------------------------------------------
   // MenuNodePath
   // ------------------------------------------------------
 
   /**
    * An instance represents the chain of ancestors leading to a particular MenuNode.
    */
   public static final class MenuNodePath {
     /** The individual segments that make up the path of a MenuNode. */
     private List<String> segments;
 
     /** Cached representation of this instance as a String. */
     private String segmentsAsString;
 
     /** Copies the parent segments to this instance and then appends the given id. */
     public MenuNodePath(MenuStateHolder parent, String id) {
      this.segments = parent.getPath() == null ? new ArrayList<String>() : new ArrayList<String>(parent.getPath().segments.size() + 1);
      if (parent.getPath() != null) {
         this.segments.addAll(parent.getPath().segments);
       }
       this.segments.add(id);
     }
 
     /** Create a MenuNodePath from the given segments (copies the given segments to this instance). */
     public MenuNodePath(String... segments) {
       this.segments = Arrays.asList(segments);
     }
 
     /** Create a MenuNodePath from the given string, using <code>PATH_SEPARATOR</code> to separate the segments. */
     public MenuNodePath(String segments) {
       this.segments = Arrays.asList(StringUtils.split(segments, PATH_SEPARATOR));
     }
 
     public String getAsString() {
       if (segmentsAsString == null) {
         StringBuffer buffer = new StringBuffer();
         String separator = "";
         for (String segment : segments) {
           buffer.append(separator).append(segment);
           separator = PATH_SEPARATOR;
         }
 
         segmentsAsString = buffer.toString();
       }
       return segmentsAsString;
     }
 
     /** Return the segment for the given ID. */
     public String getSegmentId(int index) {
       return segments.get(index);
     }
 
     /** Return the last segment ID (leaf ID). */
     public String getLastSegmentId() {
       return segments.get(segments.size() - 1);
     }
 
     public ListIterator<String> getSegmentIterator() {
       return segments.listIterator();
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((segmentsAsString == null) ? 0 : segmentsAsString.hashCode());
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (getClass() != obj.getClass())
         return false;
       MenuNodePath other = (MenuNodePath)obj;
       if (getAsString() == null) {
         if (other.getAsString() != null)
           return false;
       } else if (!getAsString().equals(other.getAsString()))
         return false;
       return true;
     }
 
     @Override
     public String toString() {
       return getAsString();
     }
   }
 
   // ============================================================================
   // MenuNode
   // ============================================================================
 
   public MenuNode(MenuStateHolder parent, String id, String title, InfoNodeElement payload) {
     this(parent, id, title, payload, null);
   }
 
   public MenuNode(MenuStateHolder parent, String id, String title, InfoNodeElement payload, Map<String, MenuNode> childElements) {
     this.parent = parent;
     this.path = new MenuNodePath(parent, id);
     this.title = title;
     this.payload = payload;
     this.childNodes = childElements == null ? new LinkedHashMap<String, MenuNode>() : childElements;
   }
 
   @Override
   public final MenuNodePath getPath() {
     return path;
   }
 
   @Override
   public MenuNodePath getActivePath() {
    return parent.getActivePath();
   }
 
   /**
    * Return the parent of this <code>MenuNode</code>, or null if this is a root. If a <code>MenuNode</code> is held
    * by a <code>MenuBar</code>, then the <code>MenuBar</code> will be the root of all top-level <code>MenuNode</code> instances.
    */
   public MenuStateHolder getParent() {
     return parent;
   }
 
   /** Return the ID of this MenuNode - the ID is the final segment of this instance's path. */
   public String getId() {
     return path.getLastSegmentId();
   }
 
   /** Return the default path that should be used when no path is specified. */
   public MenuNodePath getDefaultChildPath() {
     MenuNodePath result;
     MenuNode firstChild = getFirstChild();
 
     if (firstChild == null) {
       result = getPath();
     } else {
       result = firstChild.getPath();
     }
 
     return result;
   }
 
   public String getTitle() {
     return title;
   }
 
   /** Return the optional data associated with this instance. */
   public InfoNodeElement getPayload() {
     return payload;
   }
 
   /** Return the child <code>MenuNode</code> of this instance specified by the given <code>childId</code>. */
   public MenuNode getChild(String childId) {
     MenuNode result = childNodes.get(childId);
 
     return result;
   }
 
   /** Return all of the child nodes of this instance. */
   public Collection<MenuNode> getChildren() {
     return WebUtils.uiRepeatHack(childNodes.values());
   }
 
   public MenuNode getFirstChild() {
     MenuNode result = null;
 
     if (childNodes != null && !childNodes.isEmpty()) {
       result = childNodes.values().iterator().next();
     }
 
     return result;
   }
 
   /** Add a new child for this instance (positioned at the end of the child list). */
   public void addChild(MenuNode child) {
     childNodes.put(child.getId(), child);
   }
 
   public boolean isEmptyChildren() {
     return childNodes == null ? true : childNodes.isEmpty();
   }
 
   /** Return true if this node is the active node (identified by the active path). */
   public boolean isActive() {
     boolean result = false;
 
     if (getPath() != null && getActivePath() != null) {
       String activePath = getActivePath().getAsString() + PATH_SEPARATOR;
 
       result = activePath.contains(getPath().getAsString() + PATH_SEPARATOR);
     }
 
     return result;
   }
 }
