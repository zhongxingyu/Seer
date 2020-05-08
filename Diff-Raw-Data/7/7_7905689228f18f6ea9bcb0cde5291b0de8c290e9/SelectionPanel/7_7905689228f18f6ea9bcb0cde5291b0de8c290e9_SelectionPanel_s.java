 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.util;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class SelectionPanel extends Composite {
 
     private ListBox left;
     private ListBox right;
 
     public SelectionPanel(Collection items, 
                           ItemInfo itemInfo, 
                           Collection selectedValues,
                           int visibleItemCount,
                           String leftColTitle,
                           String rightColTitle) {
         super();
         
         FlowPanel panel = new FlowPanel();
         
         initWidget(panel);
         
         left = new ListBox();
         left.setVisibleItemCount(visibleItemCount);
         left.setMultipleSelect(true);
         
         right = new ListBox();
         right.setVisibleItemCount(visibleItemCount);
         right.setMultipleSelect(true);
         
         for (Iterator itr = items.iterator(); itr.hasNext();) {
             Object o = itr.next();
             
             String value = itemInfo.getValue(o);
             if (selectedValues != null && selectedValues.contains(value)) {
                 right.addItem(itemInfo.getText(o), value);
             } else {
                 left.addItem(itemInfo.getText(o), value);
             }
         }
         
         FlexTable table = new FlexTable();
         panel.add(table);
         FlowPanel leftCol = new FlowPanel();
         leftCol.add(new Label(leftColTitle));
         leftCol.add(left);
         table.setWidget(0, 0, leftCol);
         
        FlowPanel middle = new FlowPanel();
         Button button = new Button(">");
         button.setStyleName("smallButton");
         button.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 move(left, right);
             }
         });
         middle.add(button);
         middle.add(new Label(" "));
        button = new Button("<<");
         button.setStyleName("smallButton");
         button.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 move(right, left);
             }
         });
         middle.add(button);
         table.setWidget(0, 1, middle);
         table.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
         
         FlowPanel rightCol = new FlowPanel();
         rightCol.add(new Label(rightColTitle));
         rightCol.add(right);
         table.setWidget(0, 2, rightCol);
     }
     
     protected void move(ListBox from, ListBox to) {
         int i;
         while ((i = from.getSelectedIndex()) != -1)
         {
             String text = from.getItemText(i);
             String val = from.getValue(i);
 
             int dest = getDestination(to, text);
 
             to.insertItem(text, val, dest);
             from.removeItem(i);
         }
     }
 
     protected int getDestination(ListBox to, String text) {
         for (int i = 0; i < to.getItemCount(); i++)  {
             String txt = to.getItemText(i);
             
             if (txt.compareTo(text) > 0) 
                 return i;
         }
         
         return to.getItemCount();
     }
 
     public Collection getSelectedValues() {
         ArrayList values = new ArrayList();
         
         for (int i = 0; i < right.getItemCount(); i++) {
             values.add(right.getValue(i));
         }
         
         return values;
     }
     
     public interface ItemInfo {
         String getValue(Object o);
         String getText(Object o);
     }
 }
