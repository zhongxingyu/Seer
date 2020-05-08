 /*
  * Copyright (C) 2013 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Afffero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.pypapi.ui;
 
 import com.trolltech.qt.core.QAbstractItemModel;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.*;
 
 /**
  *
  * @author AXIA Studio (http://www.axiastudio.com)
  */
 public class WikiDelegate extends QItemDelegate {
     
     public WikiDelegate(QTableView parent) {
         super(parent);
     }
 
     @Override
     public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
         Column column = ((ITableModel) index.model()).getColumns().get(index.column()); 
         if( column.getEditorType() == CellEditorType.STRING ){
             QAbstractItemModel model = index.model();
             Object data = model.data(index, Qt.ItemDataRole.DisplayRole);
             Qt.Alignment flags = Qt.AlignmentFlag.createQFlags();
             flags.set(Qt.AlignmentFlag.AlignVCenter);
             flags.set(Qt.AlignmentFlag.AlignLeft);
             String out = (String) data;
             if( out == null ){
                 out = "";
             }
             if( out.startsWith("<del>") && out.endsWith("</del>")){
                 QFont font = painter.font();
                 font.setStrikeOut(true);
                 painter.setFont(font);
                 out = out.substring(5, out.length()-6);
                 // XXX: left margin!
                 painter.drawText(option.rect(), flags.value(), " "+out);
             } else if( out.startsWith("'''") && out.endsWith("'''")){
                 QFont font = painter.font();
                 font.setBold(true);
                 painter.setFont(font);
                 out = out.substring(3, out.length()-3);
                 // XXX: left margin!
                 painter.drawText(option.rect(), flags.value(), " "+out);
             } else {
                 super.paint(painter, option, index);
             }
         } else {
             super.paint(painter, option, index);
         }
     }
 }
