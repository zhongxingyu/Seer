 /*
  * JHistoryMenuItem.java
  * Copyright (C) 2005 by:
  *
  *----------------------------
  * cismet GmbH
  * Goebenstrasse 40
  * 66117 Saarbruecken
  * http://www.cismet.de
  *----------------------------
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *----------------------------
  * Author:
  * thorsten.hell@cismet.de
  *----------------------------
  *
  * Created on 8. Dezember 2006, 14:22
  *
  */
 
 package de.cismet.tools.gui.historybutton;
 
 import javax.swing.JMenuItem;
 
 /**
  *
  * @author thorsten.hell@cismet.de
  */
 public  class JHistoryMenuItem extends JMenuItem {
         private Object object=null;
         private int position=-1;
         public JHistoryMenuItem(Object o,int position){
             object=o;
             this.position=position;
         }
 
         public Object getObject() {
             return object;
         }
 
         public void setObject(Object object) {
             this.object = object;
         }
 
         public int getPosition() {
             return position;
         }
         
         public String getText() {
             if (object!=null) {
                 return object.toString();
             }
             else {
                return "";
             }
         }
     }
