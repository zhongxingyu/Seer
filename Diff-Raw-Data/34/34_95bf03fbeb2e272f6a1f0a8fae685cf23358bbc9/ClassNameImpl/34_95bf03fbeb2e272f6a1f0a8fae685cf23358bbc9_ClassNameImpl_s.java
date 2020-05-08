 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.ast;
 
 import fr.jamgotchian.abcd.core.type.ClassName;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 class ClassNameImpl implements ClassName {
 
     private final ImportManager importManager;
 
    private final String name;
 
     private final String packageName;
 
    ClassNameImpl(String qname, ImportManager importManager) {
         this.importManager = importManager;
        int lastDotIndex = qname.lastIndexOf('.');
         if (lastDotIndex == -1)  {
             packageName = null;
            name = qname;
         } else {
            packageName = qname.substring(0, lastDotIndex);
            name = qname.substring(lastDotIndex+1);
         }
     }
 
     @Override
     public String getPackageName() {
         return packageName;
     }
 
     @Override
     public String getName() {
        if (packageName == null || importManager.isImported(this)) {
             return name;
         } else {
            return getQualifiedName();
         }
     }
 
     @Override
     public String getQualifiedName() {
        return packageName + '.' + name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof ClassNameImpl)) {
             return false;
         }
        return ((ClassNameImpl) obj).name.equals(name);
     }
 
     @Override
     public int hashCode() {
        return name.hashCode();
     }
 
     @Override
     public String toString() {
         return getName();
     }
 }
 
