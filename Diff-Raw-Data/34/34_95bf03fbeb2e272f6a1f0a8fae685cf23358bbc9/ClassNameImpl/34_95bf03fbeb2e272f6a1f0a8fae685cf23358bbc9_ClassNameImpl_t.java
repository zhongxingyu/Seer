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
 
    private final String qualifiedName;
 
     private final String packageName;
 
    private final String name;

    ClassNameImpl(String qualifiedName, ImportManager importManager) {
        this.qualifiedName = qualifiedName;
         this.importManager = importManager;
        int lastDotIndex = qualifiedName.lastIndexOf('.');
         if (lastDotIndex == -1)  {
             packageName = null;
            name = qualifiedName;
         } else {
            packageName = qualifiedName.substring(0, lastDotIndex);
            name = qualifiedName.substring(lastDotIndex+1);
         }
     }
 
     @Override
     public String getPackageName() {
         return packageName;
     }
 
     @Override
     public String getName() {
        if (packageName == null || importManager == null || importManager.isImported(this)) {
             return name;
         } else {
            return qualifiedName;
         }
     }
 
     @Override
     public String getQualifiedName() {
        return qualifiedName;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof ClassNameImpl)) {
             return false;
         }
        return ((ClassNameImpl) obj).qualifiedName.equals(qualifiedName);
     }
 
     @Override
     public int hashCode() {
        return qualifiedName.hashCode();
     }
 
     @Override
     public String toString() {
         return getName();
     }
 }
 
