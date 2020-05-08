 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import java.lang.ref.WeakReference;
 import java.util.List;
 
 public class RootedProperty {
 
     private final WeakReference<Node> root;
     private final String path;
     private final List<ErrorCheck<?, ?>> errorChecks;
 
     RootedProperty(Node root, String path) {
         this.root = new WeakReference<>(root);
         this.path = path;
         errorChecks = root.getErrorChecks(path);
     }
 
     public Node getRoot() {
         return root.get();
     }
 
     public String getPath() {
         return path;
     }
 
     public <T> T getValue() {
         return root.get().get(path);
     }
 
     public <T> T getValue(Class<T> type) {
         return root.get().get(path, type);
     }
 
     public void setValue(Object content) {
         root.get().set(path, content);
     }
 
     public Class<?> getValueType(boolean runtime) {
         return root.get().getPropertyType(path, runtime);
     }
 
    public List<ErrorCheck<?, ?>> getErrorChecks() {
        return errorChecks;
     }
 
     public void updateErrorChecks() {
         errorChecks.clear();
         errorChecks.addAll(root.get().getErrorChecks(path));
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((path == null) ? 0 : path.hashCode());
         result = prime * result + ((root == null) ? 0 : root.get().hashCode());
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
         RootedProperty other = (RootedProperty) obj;
         if (path == null) {
             if (other.path != null)
                 return false;
         } else if (!path.equals(other.path))
             return false;
         if (root == null) {
             if (other.root != null)
                 return false;
         } else if (root.get() != other.root.get())
             return false;
         return true;
     }
 
     public List<?> getPossibleValues() {
         return root.get().getPossiblePropertyValues(path);
     }
 
 }
