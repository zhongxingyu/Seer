 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.storage;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A data storage path. This class encapsulates the path (directory
  * plus name) of an object, a file or an index. It also provides
  * some simple help methods to access and work with the path and to
  * locate the object addressed by it.
  *
  * @author Per Cederberg
  * @version 1.0
  */
 public class Path {
 
     /**
      * A root path constant. Note that several paths may be root
      * paths, so this is not a unique instance. It is only here
      * for convenience.
      */
     public static final Path ROOT = new Path("");
 
     /**
      * The path components. The last element in this array is the
      * object name, and any previous elements correspond to parent
      * indices (i.e. the parent path). The root index has a zero
      * length array.
      */
     private String[] parts = null;
 
     /**
      * The index flag. This flag is set if the path corresponds
      * to an index (a directory, a list of objects and files).
      */
     private boolean index = false;
 
     /**
      * Creates a new path from a string representation (similar to
      * a file system path).
      *
      * @param path           the string path to parse
      */
     public Path(String path) {
         this(null, path);
     }
 
     /**
      * Creates a new path from a parent and a child string
      * representation (similar to a file system path).
      *
      * @param parent         the parent index path
      * @param path           the string path to parse
      */
     public Path(Path parent, String path) {
         this.parts = (parent == null) ? ArrayUtils.EMPTY_STRING_ARRAY : parent.parts;
         path = StringUtils.stripStart(path, "/");
         this.index = path.equals("") || path.endsWith("/");
         path = StringUtils.stripEnd(path, "/");
         if (!path.equals("")) {
             String[] child = path.split("/");
             String[] res = new String[this.parts.length + child.length];
             for (int i = 0; i < this.parts.length; i ++) {
                 res[i] = this.parts[i];
             }
             for (int i = 0; i < child.length; i++) {
                 res[this.parts.length + i] = child[i];
             }
             this.parts = res;
         }
     }
 
     /**
      * Creates a new path from the specified parts.
      *
      * @param parts          the array of path components
      * @param isIndex        the index flag
      */
     public Path(String[] parts, boolean isIndex) {
         this.parts = parts;
         this.index = isIndex;
     }
 
     /**
      * Returns a string representation of this object.
      *
      * @return a string representation of this object
      */
     public String toString() {
        return "/" + toIdent(0);
     }
 
     /**
      * Returns an object identifier based on this path. The
      * identifier will start at the specified position in this path.
      *
      * @param pos            the position, 0 <= pos < length()
      *
      * @return an object identifier for this path (without prefix)
      *
      * @see #subPath(int)
      */
     public String toIdent(int pos) {
         StringBuilder buffer = new StringBuilder();
         for (int i = pos; i < parts.length; i++) {
             if (i > pos) {
                 buffer.append("/");
             }
             buffer.append(parts[i]);
         }
         if (index) {
             buffer.append("/");
         }
         return buffer.toString();
     }
 
     /**
      * Returns an object identifier based on this path. The
      * identifier will start after the specified prefix. If the
      * prefix does not match this path, it is ignored.
      *
      * @param prefix         the path prefix to remove
      *
      * @return an object identifier for this path (without prefix)
      */
     public String toIdent(Path prefix) {
         if (prefix != null && prefix.length() < length() && startsWith(prefix)) {
             return toIdent(prefix.length());
         } else {
             return toIdent(0);
         }
     }
 
     /**
      * Checks if this path is identical to another path. The two
      * paths will be considered equal if they have the same length,
      * all elements are equal and the index flag is identical.
      *
      * @param obj            the object to compare with
      *
      * @return true if the two paths are equal, or
      *         false otherwise
      */
     public boolean equals(Object obj) {
         return obj instanceof Path &&
                index == ((Path) obj).index &&
                Arrays.equals(parts, ((Path) obj).parts);
     }
 
     /**
      * Returns a hash code for this object.
      *
      * @return a hash code for this object
      */
     public int hashCode() {
         int result = 1;
         for (int i = 0; i < parts.length; i++) {
             result = 31 * result + parts[i].hashCode();
         }
         return result;
     }
 
     /**
      * Checks if this path corresponds to the root index.
      *
      * @return true if the path is for the root index, or
      *         false otherwise
      */
     public boolean isRoot() {
         return isIndex() && parts.length == 0;
     }
 
     /**
      * Checks if this path corresponds to an index.
      *
      * @return true if the path is an index, or
      *         false otherwise
      */
     public boolean isIndex() {
         return index;
     }
 
     /**
      * Checks if this path starts with the specified path. All the
      * path elements must match up to the length of the specified
      * path. As a special case, this method will return true if the
      * two paths are identical. It will also return true for a null
      * path.
      *
      * @param path           the path to compare with
      *
      * @return true if this path starts with the specified path, or
      *         false otherwise
      */
     public boolean startsWith(Path path) {
         if (path == null) {
             return true;
         } else if (parts.length < path.parts.length) {
             return false;
         }
         for (int i = 0; i < path.parts.length; i++) {
             if (!parts[i].equals(path.parts[i])) {
                 return false;
             }
         }
         if (parts.length == path.parts.length) {
             return index == path.index;
         } else {
             return path.index;
         }
     }
 
     /**
      * Returns the directory depth. The root index, and any objects
      * located directly there, have depth zero (0). For each
      * additional sub-level traversed, the depth is increased by
      * one (1). Objects and files in the storage tree will not
      * affect the depth.
      *
      * @return the path directory depth
      */
     public int depth() {
         return parts.length - (isIndex() ? 0 : 1);
     }
 
     /**
      * Returns the path length. The length contains the number of
      * elements in the path, counting both indices and any named
      * object or file. The length is always greater or equal to
      * the depth.
      *
      * @return the path length
      */
     public int length() {
         return parts.length;
     }
 
     /**
      * Returns the name of the last element in the path. This is
      * normally the object name.
      *
      * @return the object or index name, or
      *         null for the root index
      */
     public String name() {
         return (parts.length > 0) ? parts[parts.length - 1] : null;
     }
 
     /**
      * Returns the name of the path element at the specified position.
      * A zero position will return the first element traversed, i.e.
      * the one located in the root.
      *
      * @param pos            the position, 0 <= pos < length()
      *
      * @return the name of the element at the specified position, or
      *         null if the position is out of range
      */
     public String name(int pos) {
         if (0 <= pos && pos < parts.length) {
             return parts[pos];
         } else {
             return null;
         }
     }
 
     /**
      * Creates a new path to the parent index.
      *
      * @return a new path to the parent index
      */
     public Path parent() {
         if (isRoot()) {
             return this;
         } else {
             String[] newParts = new String[parts.length - 1];
             for (int i = 0; i < parts.length - 1; i++) {
                 newParts[i] = parts[i];
             }
             return new Path(newParts, true);
         }
     }
 
     /**
      * Creates a new path to a child index or object.
      *
      * @param name           the child name
      * @param isIndex        the index flag
      *
      * @return a new path to a child index or object
      */
     public Path child(String name, boolean isIndex) {
         String[] newParts = new String[parts.length + 1];
         for (int i = 0; i < parts.length; i++) {
             newParts[i] = parts[i];
         }
         newParts[newParts.length - 1] = name;
         return new Path(newParts, isIndex);
     }
 
     /**
      * Creates a new path to a descendant index or object.
      *
      * @param subpath        the relative descendant path
      *
      * @return a new path to a descendant index or object
      */
     public Path descendant(Path subpath) {
         String[] newParts = new String[parts.length + subpath.parts.length];
         for (int i = 0; i < parts.length; i++) {
             newParts[i] = parts[i];
         }
         for (int i = 0; i < subpath.parts.length; i++) {
             newParts[parts.length + i] = subpath.parts[i];
         }
         return new Path(newParts, subpath.index);
     }
 
     /**
      * Creates a new path that starts at the specified position in
      * this path. I.e. this method removes a path prefix.
      *
      * @param pos            the position, 0 <= pos < length()
      *
      * @return a new path with the prefix removed, or
      *         a root path if the position was out of range
      */
     public Path subPath(int pos) {
         int len = Math.max(Math.min(parts.length - pos, parts.length), 0);
         String[] newParts = new String[len];
         for (int i = 0; i < len; i++) {
             newParts[i] = parts[i + pos];
         }
         return new Path(newParts, index || len == 0);
     }
 }
