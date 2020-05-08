 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.selector.table;
 
 import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
 import com.od.jtimeseries.ui.selector.shared.SelectorPanel;
 import com.od.jtimeseries.ui.timeseries.RemoteChartingTimeSeries;
 import com.od.jtimeseries.ui.util.PopupTriggerMouseAdapter;
 import com.od.swing.action.ListSelectionActionModel;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 25-May-2009
  * Time: 11:37:55
  * To change this template use File | Settings | File Templates.
  */
 public class TableSelector extends SelectorPanel {
 
     private TimeSeriesContext rootContext;
     private java.util.List<Action> seriesActions;
     private String selectionText;
     private BeanPerRowModel<RemoteChartingTimeSeries> tableModel;
     private SortableTable timeSeriesTable;
     private JPopupMenu tablePopupMenu;
     public TableColumnManager tableColumnManager;
 
     public TableSelector(ListSelectionActionModel<RemoteChartingTimeSeries> seriesActionModel,
                          TimeSeriesContext rootContext,
                          java.util.List<Action> seriesActions,
                          String selectionText) {
         super(seriesActionModel);
         this.rootContext = rootContext;
         this.seriesActions = seriesActions;
         this.selectionText = selectionText;
         createTable();
         refreshSeries();
         createPopupMenu();
 
         setLayout(new BorderLayout());
         add(new JScrollPane(timeSeriesTable), BorderLayout.CENTER);
         addSeriesSelectionListener();
     }
 
     public TableColumnManager getTableColumnManager() {
         return tableColumnManager;
     }
 
     public void setColumns(List<ColumnSettings> columnSettings) {
         tableColumnManager.setColumns(columnSettings);
     }
 
     public List<ColumnSettings> getColumnSettings() {
         return tableColumnManager.getColumnSettings();
     }
 
     private void createPopupMenu() {
         tablePopupMenu = new JPopupMenu("Series Actions");
         for ( Action a : seriesActions) {
             tablePopupMenu.add(a);
         }
 
         timeSeriesTable.addMouseListener(
             new PopupTriggerMouseAdapter(tablePopupMenu, timeSeriesTable)
         );
     }
 
     private void createTable() {
         tableModel = new TableModelCreator().createTableModel();
         tableColumnManager = new TableColumnManager(tableModel, selectionText);
         timeSeriesTable = new TimeSeriesTable(tableModel, tableColumnManager);
     }
 
     private void addSeriesSelectionListener() {
         timeSeriesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         timeSeriesTable.getSelectionModel().addListSelectionListener(
             new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                    if ( ! e.getValueIsAdjusting() && timeSeriesTable.getSelectedRow() > -1 ) {
                        int modelRow = TableModelWrapperUtils.getActualRowAt(timeSeriesTable.getModel(),timeSeriesTable.getSelectedRow());
                        RemoteChartingTimeSeries series = tableModel.getObject(modelRow);
                         getSeriesActionModel().setSelected(series);
                         fireSelectedForDescription(series);
                     }
                 }
             }
         );
     }
 
     public void refreshSeries() {
         tableModel.clear();
         List<IdentifiableTimeSeries> l = rootContext.findAllTimeSeries().getAllMatches();
         List<RemoteChartingTimeSeries> timeSeries = new ArrayList<RemoteChartingTimeSeries>();
         for ( IdentifiableTimeSeries i : l) {
             if ( i instanceof RemoteChartingTimeSeries ) {
                 timeSeries.add((RemoteChartingTimeSeries)i);
             }
         }
         tableModel.addObjects(timeSeries);
     }
 
     public void removeSeries(java.util.List<RemoteChartingTimeSeries> series) {
         for (RemoteChartingTimeSeries s : series ) {
             tableModel.removeObject(s);
         }
     }
 
     public void addAllDynamicColumns() {
         tableColumnManager.addAllDynamicColumns();    
     }
 }
