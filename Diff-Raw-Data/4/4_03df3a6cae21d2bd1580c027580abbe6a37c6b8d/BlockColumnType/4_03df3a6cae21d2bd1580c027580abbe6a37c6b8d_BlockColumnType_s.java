 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.infine.android.devoxx.ui.widget;
 
 public enum BlockColumnType {
 
     REGISTRATION("Registration",false, 1, 2), // Nom , Affichage favoris, Numero colonne, Nombre de colonnes
     BREAKFAST("Breakfast",false, 0, 1),
     LUNCH("Lunch",false, 0, 1),
     TALK("Conference",true, 0, 2),
     QUICKY("Quickie",false, 1, 1),
     BOF("BOF (Bird of a Feather)",true, 0, 2),
    UNIVERSITY("University",true, 0, 2),
    HANDS_ON("Hand's on Labs",true, 2, 1),
     KEYNOTE("Keynote",false, 0, 3),
     BREAK("Break",false, 0, 1),
     PARTY("Party",false, 2, 1),
     COFFEE_BREAK("Coffee Break",false, 0, 1),
     CODE_STORY("Code Story",true, 2, 1),
     OTHER_FORMAT("Other format",true, 2, 1),
     HACKATON("Hackaton",true, 2, 1),
     TOOLS_IN_ACTION("Tools-in-Action",true, 0, 2),
     UNDEFINED("undefined",false, 999, 1);
     
     private String type;
     
     private int numColumn;
     
     private int colspan;
     
     private boolean showStar;
     
     private BlockColumnType(String type, boolean showStar, int numColumn, int colspan) {
         this.type = type;
         this.showStar = showStar;
         this.numColumn = numColumn;
         this.colspan = colspan;
     }
     
     public String getType() {
         return type;
     }
     
     public int getNumColumn() {
         return numColumn;
     }
     
     public int getColspan() {
         return colspan;
     }
     
     public boolean showStar(){
     	return showStar;
     }
     
     public static BlockColumnType typeOf(String type) {
         if (type == null || "".equals(type.trim())) {
             return UNDEFINED;
         } else {
             for (BlockColumnType v : BlockColumnType.values()) {
                 if (v.getType().equals(type)) {
                     return v;
                 }
             }
         }
         return UNDEFINED;
     }
 
     public static BlockColumnType typeOf(int numColumn) {
         for (BlockColumnType v : BlockColumnType.values()) {
             if (v.getNumColumn() == numColumn) {
                 return v;
             }
         }
         return UNDEFINED;
     }
 }
