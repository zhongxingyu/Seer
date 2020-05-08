 // This file is part of Q-Cumberless Testing.
 //
 // Q-Cumberless Testing is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // Q-Cumberless Testing is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with Q-Cumberless Testing.  If not, see <http://www.gnu.org/licenses/>.
 //
 // Copyright 2012
 
 // Daniel Andersen (dani_ande@yahoo.dk)
 
 package com.trollsahead.qcumberless.model;
 
 import com.trollsahead.qcumberless.util.Util;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Tag {
     private String tags = "";
     
     public Tag(String tags) {
         this.tags = tags;
         streamlineTags();
     }
     
     public String toString() {
         return tags;
     }
 
     public boolean hasTags() {
         return !Util.isEmpty(tags);
     }
 
     public Set<String> toSet() {
         Set<String> set = new HashSet<String>();
         if (!hasTags()) {
             return set;
         }
         String[] array = tags.split("@");
         for (String tag : array) {
             tag = tag.replaceAll("@", "").trim();
             if (!Util.isEmpty(tag)) {
                 set.add(tag);
             }
         }
         return set;
     }
 
     public List<String> toList() {
         return new ArrayList<String>(toSet());
     }
 
     public void add(String tag) {
         if (Util.isEmpty(tags)) {
             tags = "";
         }
         if (!tag.startsWith("@")) {
             tags += " @" + tag;
         } else {
             tags += " " + tag;
         }
         streamlineTags();
     }
 
     public void remove(String tag) {
        tag = !tag.startsWith("@") ? "@" + tag : tag;
        tags = tags.replaceAll(tag, "");
         streamlineTags();
     }
 
     private void streamlineTags() {
         if (tags == null) {
             return;
         }
         tags = tags
                 .replaceAll("  ", " ")
                 .trim();
     }
 }
