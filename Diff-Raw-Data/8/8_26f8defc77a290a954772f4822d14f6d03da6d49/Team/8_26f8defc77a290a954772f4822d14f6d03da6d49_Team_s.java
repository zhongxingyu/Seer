 /*
  Copyright (C) 2012 Corey Edwards
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package org.zmonkey.beacon.data;
 
 import java.util.Vector;
 
 /**
  * User: corey
  * Date: 3/6/12
  * Time: 9:04 PM
  */
 public class Team implements Storable {
     public int number;
     public String objectives;
     public String notes;
     public String type;
     public String[] members;
 
     public Team(){
     }
     
     public void setMembers(String m){
         members = m.split(",");
     }
     
     public String getMembers(){
         if (members == null || members.length == 0){
             return "";
         }
         StringBuilder s = new StringBuilder();
         int i=0;
         for (; i<members.length-1; i++){
             s.append(members[i]);
             s.append("\n");
         }
         s.append(members[i]);
         return s.toString();
     }
     public StringBuilder store(StringBuilder s){
         if (s ==  null){
             s = new StringBuilder();
         }
         s.append("class=Team\n");
         if (number > 0) s.append(" number=" + number + "\n");
         if (objectives != null) s.append(" objectives=" + objectives + "\n");
         if (notes != null) s.append(" notes=" + notes + "\n");
         if (type != null) s.append(" type=" + type + "\n");
         if (members != null){
             for (String member : members){
                 s.append(" member=" + member + "\n");
             }
         }
         return s;
     }
 
     public void load(String s){
         if (s == null || s.equals("")){
             return;
         }
         String[] lines = s.split("\n");
         boolean me = false;
         Vector<String> v = new Vector<String>();
         for (String line : lines){
             if (me){
                 if (!line.equals("class=Team") && line.startsWith("class=")){
                     me = false;
                 }
                 else{
                     if (line.startsWith(" number=")){
                         number = Integer.parseInt(line.substring(8));
                     }
                     else if (line.startsWith(" objectives=")){
                         objectives = line.substring(12);
                     }
                     else if (line.startsWith(" notes=")){
                         notes = line.substring(7);
                     }
                     else if (line.startsWith(" type=")){
                         type = line.substring(6);
                     }
                     else if (line.startsWith(" member=")){
                         String member = line.substring(8);
                         v.add(member);
                     }
                 }
             }
             else{
                 if (line.equals("class=Team")){
                     me = true;
                 }
             }
         }
        //TODO: fix this vector to array bit
//        if (!v.isEmpty()){
//            members = (String []) v.toArray();
//        }
     }
 }
