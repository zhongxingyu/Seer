 package org.apache.felix.ipojo.everest.services;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 
 /**
  * An object that is used to locate a resource.
  */
 public class Path implements Iterable<String>, Comparable<Path> {
 
     /**
      * The root path.
      */
     private static final Path ROOT = new Path(new String[0], "/");
 
     /**
      * The path separator
      */
     public static final String SEPARATOR = "/";
 
     /**
      * The number of elements in this path.
      */
     private final int m_count;
 
     /**
      * The elements of this path.
      */
     private final String[] m_elements;
 
     /**
      * The string representation of this path.
      */
     private final String m_string;
 
     /**
      * Create a new path from the given elements and string representation.
      *
      * @param elements the elements of the path
      * @param string   the string representation of the path
      */
     private Path(String[] elements, String string) {
         m_elements = elements;
         m_count = m_elements.length;
         m_string = string;
     }
 
     // SIMPLE GETTERS
 
     /**
      * @return all the elements of this path
      */
     public String[] getElements() {
         return m_elements.clone();
     }
 
     /**
      * @return the number of elements in the path
      */
     public int getCount() {
         return m_count;
     }
 
     /**
      * Returns an element of this path.
      *
      * @param index the index of the element
      * @return the number of elements in the path
      * @throws IndexOutOfBoundsException if {@code index} is negative, or {@code index}  is greater than or equal to the number of elements
      */
     public String getElement(int index) throws IndexOutOfBoundsException {
         return m_elements[index];
     }
 
     /**
      * @return an iterator over the elements of this path.
      */
     public Iterator<String> iterator() {
         return Collections.unmodifiableList(Arrays.asList(m_elements)).iterator();
     }
 
     // STRUCTURE GETTERS
 
     /**
      * @return the parent of this path, or {@code null} if this path is the root path.
      */
     public Path getParent() {
         if (m_count == 0) {
             return null;
         }
         return getHead(m_count -1);
     }
 
     /**
      * @return the first element of this path
      * @throws IndexOutOfBoundsException if this path is the root path
      */
     public String getFirst() {
         return m_elements[0];
     }
 
     /**
      * @return the last element of this path
      * @throws IndexOutOfBoundsException if this path is the root path
      */
     public String getLast() {
         return m_elements[m_count-1];
     }
 
     /**
      * Get the head of this path.
      *
      * @param count the number of head elements to return
      * @return TODO
      * @throws IndexOutOfBoundsException if this path contains less than {@code level} elements
      */
     public Path getHead(int count) {
         if (count == 0) {
             return ROOT;
         }
         String[] elements = new String[count];
         int endIndex = 0;
         for (int i = 0; i < count; i++) {
             elements[i] = m_elements[i];
             endIndex += elements[i].length() + 1;
         }
         return new Path(elements, m_string.substring(0, endIndex));
     }
 
     /**
      * Get the tail of this path.
      *
      * @param count the number of tail elements to return
      * @return TODO
      * @throws IndexOutOfBoundsException if this path contains less than {@code level} elements
      */
     public Path getTail(int count) {
         if (count == 0) {
             return ROOT;
         }
         String[] elements = new String[count];
         int beginIndex = m_string.length();
         for (int i = 0; i < count; i++) {
             elements[i] = m_elements[m_count - count + i];
             beginIndex -= elements[i].length() + 1;
         }
         return new Path(elements, m_string.substring(beginIndex, m_string.length()));
     }
 
     public Path add(Path path) {
        if (m_count == 0) {
            return path;
        } else if (path.m_count == 0) {
            return this;
        }
         String[] elements = new String[m_count + path.m_count];
         System.arraycopy(m_elements, 0, elements, 0, m_count);
         System.arraycopy(path.m_elements, 0, elements, m_count, path.m_count);
         return new Path(elements, m_string + path.m_string);
     }
 
     public Path subtract(Path path) {
         if (path.m_count == 0) {
             return this;
         }
         if (path.m_count > m_count) {
             throw new IllegalArgumentException();
         }
         for (int i = 0; i < path.m_count; i++) {
             if (!m_elements[i].equals(path.m_elements[i])) {
                 throw new IllegalArgumentException();
             }
         }
         return getTail(m_count - path.m_count);
     }
 
     public boolean isAncestorOf(Path path) {
         if (m_count == 0) {
             // The root path is the ancestor of every path but the root.
             return path.m_count != 0;
         } else if (m_count >= path.m_count) {
             // Cannot be an ancestor with more elements
             return false;
         }
         for (int i = 0; i < m_count; i++) {
             if (!m_elements[i].equals(path.m_elements[i])) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean isDescendantOf(Path path) {
         if (m_count == 0) {
             // The root path descends from nothing.
             return false;
         } else if (path.m_count == 0) {
             // Every path descends from the root path (but the root path itself)
             return true;
         } else if (m_count <= path.m_count) {
             // Cannot be an descendant with less elements
             return false;
         }
         for (int i = 0; i < path.m_count; i++) {
             if (!m_elements[i].equals(path.m_elements[i])) {
                 return false;
             }
         }
         return true;
     }
 
     public static Path from(String pathName) {
         if (pathName == null) {
             throw new NullPointerException("null pathName");
         }
 
         // Path names MUST be absolute
         if (!pathName.startsWith(SEPARATOR)) {
             throw new IllegalArgumentException("invalid pathName: " + pathName);
         }
 
         // Remove the leading slash so it won't disturb the path analysis.
         String tmp = pathName.substring(1);
         if (tmp.isEmpty()) {
             // This is the root.
             return ROOT;
         }
 
         // Cut the path into elements.
         String[] elements = tmp.split(SEPARATOR);
 
         // Check that there are no empty element (caused by double slash) and that there is no trailing slash
         if (tmp.endsWith(SEPARATOR) || Arrays.asList(elements).contains("")) {
             throw new IllegalArgumentException("invalid pathName: " + pathName);
         }
 
         return new Path(elements, pathName);
     }
 
     public static Path fromElements(String... elements) {
         if (elements == null) {
             throw new NullPointerException("null elements");
         } else if (elements.length == 0) {
             // The only path with no element is the root.
             return ROOT;
         }
 
         // Check that no element contains the path separator
         // Meanwhile, build the string representation of the path
         StringBuilder sb = new StringBuilder();
         for (String e : elements) {
             if (e.contains(SEPARATOR) || e.isEmpty()) {
                 throw new IllegalArgumentException("invalid path element: " + e);
             }
             sb.append(SEPARATOR);
             sb.append(e);
         }
 
         return new Path(elements, sb.toString());
     }
 
 
     // UTILITY METHODS
 
     @Override
     public String toString() {
         return m_string;
     }
 
     @Override
     public int hashCode() {
         return m_string.hashCode();
     }
 
     @Override
     public boolean equals(Object that) {
         if (this == that) {
             return true;
         } else if (!(that instanceof Path)) {
             return false;
         }
         return m_string.equals(((Path) that).m_string);
     }
 
     public int compareTo(Path that) {
         return m_string.compareTo(that.m_string);
     }
 }
