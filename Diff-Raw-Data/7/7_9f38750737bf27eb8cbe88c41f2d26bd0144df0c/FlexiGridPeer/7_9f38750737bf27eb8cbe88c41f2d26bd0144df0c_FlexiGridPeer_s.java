 /*
  * This file (FlexiGridPeer.java) is part of the Echolot Project (hereinafter "Echolot").
  * Copyright (C) 2008-2010 eXXcellent Solutions GmbH.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package de.exxcellent.echolot.webcontainer.sync.component;
 
 import de.exxcellent.echolot.model.flexi.FlexiColumnVisibility;
 import de.exxcellent.echolot.model.flexi.FlexiSortingModel;
 import de.exxcellent.echolot.model.flexi.FlexiSortingColumn;
 import de.exxcellent.echolot.model.flexi.FlexiRowSelection;
 import de.exxcellent.echolot.model.flexi.ResultsPerPageOption;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
 import com.thoughtworks.xstream.io.path.PathTrackingWriter;
 import de.exxcellent.echolot.SharedService;
 import de.exxcellent.echolot.app.FlexiGrid;
 import de.exxcellent.echolot.model.flexi.FlexiCell;
 import de.exxcellent.echolot.model.flexi.FlexiColumn;
 import de.exxcellent.echolot.model.flexi.FlexiColumnModel;
 import de.exxcellent.echolot.model.flexi.FlexiColumnsUpdate;
 import de.exxcellent.echolot.model.flexi.FlexiPage;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import nextapp.echo.app.Component;
 import nextapp.echo.app.util.Context;
 import nextapp.echo.webcontainer.AbstractComponentSynchronizePeer;
 import nextapp.echo.webcontainer.ContentType;
 import nextapp.echo.webcontainer.ServerMessage;
 import nextapp.echo.webcontainer.Service;
 import nextapp.echo.webcontainer.WebContainerServlet;
 import nextapp.echo.webcontainer.service.JavaScriptService;
 
 /**
  * A specialized {@link AbstractComponentSynchronizePeer} initializing the libraries for the {@link de.exxcellent.echolot.app.FlexiGrid} component
  * and responsible for transport of the properties to the javascript based echo3 client.
  *
  * @author Oliver Pehnke <o.pehnke@exxcellent.de>
  */
 
 public class FlexiGridPeer extends AbstractComponentSynchronizePeer {
     // Create a JavaScriptServices containing the FlexiGrid and FlexiGrid JavaScript code.
     private static final Service FLEXIGRID_SERVICE;
     private static final Service FLEXIGRID_SYNC_SERVICE;
     private static final String FLEXIGRID_STYLESHEET;
     
     static {
         FLEXIGRID_SERVICE = JavaScriptService.forResource("exxcellent.FlexiGridService", "js/flexigrid/flexigrid.js");
         FLEXIGRID_SYNC_SERVICE = JavaScriptService.forResource("exxcellent.FlexiGrid.Sync", "js/Sync.FlexiGrid.js");
         
         FLEXIGRID_STYLESHEET = "js/flexigrid/css/flexigrid/";
 
         /* Register JavaScriptService with the global service registry.*/
         WebContainerServlet.getServiceRegistry().add(FLEXIGRID_SERVICE);
         WebContainerServlet.getServiceRegistry().add(FLEXIGRID_SYNC_SERVICE);
 
         WebContainerServlet.getResourceRegistry().addPackage("FlexiGridStylesheet", FLEXIGRID_STYLESHEET);
         WebContainerServlet.getResourceRegistry().add("FlexiGridStylesheet", "flexigrid-template.css", ContentType.TEXT_CSS);
 
     }
     
     // * Flexi column converter
     // ------------------------
     private final class FlexiColumnConverter implements Converter {
         private final FlexiGrid fg;
 
         public FlexiColumnConverter(FlexiGrid fg) {
             this.fg = fg;
         }
         
         @Override
         public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
             PathTrackingWriter w = (PathTrackingWriter) writer;
             FlexiColumn column = (FlexiColumn) o;
             
             w.startNode("id", int.class);
             w.setValue(Integer.toString(column.getId()));
             w.endNode();
                         
             w.startNode("sortable", boolean.class);
             w.setValue(Boolean.toString(column.isSortable()));
             w.endNode();
             
             w.startNode("hide", boolean.class);
             w.setValue(Boolean.toString(column.isHided()));
             w.endNode();
             
             w.startNode("visible", boolean.class);
             w.setValue(Boolean.toString(column.isVisible()));
             w.endNode();
             
             w.startNode("tooltip", String.class);
             w.setValue(column.getTooltip());
             w.endNode();
             
             w.startNode("cell", FlexiCell.class);
             mc.convertAnother(column.getCell());
             w.endNode();
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean canConvert(Class type) {
             return type == FlexiColumn.class;
         }        
     }
     
     // * Flexi cell converter
     // ----------------------
     private final class FlexiCellConverter implements Converter {
         private final FlexiGrid fg;
 
         public FlexiCellConverter(FlexiGrid fg) {
             this.fg = fg;
         }
         
         @Override
         public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
             PathTrackingWriter w = (PathTrackingWriter) writer;
             FlexiCell cell = (FlexiCell) o;
                         
             w.startNode("rowId", int.class);
             w.setValue(Integer.toString(cell.getRowId()));
             w.endNode();
             
             w.startNode("colId", int.class);
             w.setValue(Integer.toString(cell.getColId()));
             w.endNode();  
             
             w.startNode("renderId", String.class);
            w.setValue("C." + cell.getVisibleComponent().getRenderId());
             w.endNode();
             
             w.startNode("componentIdx", Integer.class);
            w.setValue(cell.getVisibleComponent().getId());
             w.endNode();
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean canConvert(Class type) {
             return type == FlexiCell.class;
         }        
     }
        
     private static class ColumnUpdate {
         final int colId;
         final HashMap<String, Object> props;
 
         public ColumnUpdate(int colId, HashMap<String, Object> props) {
             this.colId = colId;
             this.props = props;
         }
     }
     
     // * FlexiColumnsUpdate converter
     // ----------------------
     private final class FlexiColumnsUpdateConverter implements Converter {
         private final FlexiGrid fg;
         private final Converter cupc;
         
         public FlexiColumnsUpdateConverter(FlexiGrid fg) {
             this.fg = fg;
             this.cupc = new Converter() {
                 @Override
                 public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
                       PathTrackingWriter w = (PathTrackingWriter) writer;
                       ColumnUpdate up = (ColumnUpdate) o;
                       w.startNode("ID", int.class);
                       w.setValue(Integer.toString(up.colId));
                       w.endNode();
 
                       w.startNode("props", HashMap.class);
                       mc.convertAnother(up.props);
                       w.endNode();
                 }
 
                 @Override
                 public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 @Override
                 public boolean canConvert(Class type) {
                     return type == ColumnUpdate.class;
                 }
             };
         }
         
         @Override
         public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
             PathTrackingWriter w = (PathTrackingWriter) writer;
             HashMap<Integer, HashMap<String, Object>> updates = (HashMap<Integer, HashMap<String, Object>>) o;
             for (Entry<Integer, HashMap<String, Object>> colUpdates : updates.entrySet()) {
                 w.startNode("", ColumnUpdate.class);
                 mc.convertAnother(new ColumnUpdate(colUpdates.getKey(), colUpdates.getValue()), cupc);
                 w.endNode();
             }
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean canConvert(Class type) {
             return type == HashMap.class;
         }        
     }
     
     
     // * The serializer used to serialize model instances.
     // ---------------------------------------------------
     private final XStream streamOut = new XStream(new JsonHierarchicalStreamDriver());
     private final XStream streamIn = new XStream(new JettisonMappedXmlDriver());    
     private FlexiCellConverter cellConverter = null;
     private FlexiColumnConverter columnConverter = null;
     private FlexiColumnsUpdateConverter columnsUpdateConverter = null;
         
     /**
      * Default constructor for a {@link FlexiGridPeer}. Registers an event peer for client events.
      */
     public FlexiGridPeer() {
         addOutputProperty(FlexiGrid.PROPERTY_TABLE_ROW_SELECTION);
         addOutputProperty(FlexiGrid.PROPERTY_ACTIVE_PAGE);
         addOutputProperty(FlexiGrid.PROPERTY_FLEXICOLUMNS_UPDATE);
         addOutputProperty(FlexiGrid.PROPERTY_RESULTS_PER_PAGE_OPTION);
         addOutputProperty(FlexiGrid.PROPERTY_HEADER_VISIBLE);
       
         //* Event fired when ActivePage is changed */
         addEvent(new EventPeer(FlexiGrid.INPUT_ACTIVE_PAGE_CHANGED,
                 FlexiGrid.TABLE_ACTIVE_PAGE_LISTENERS_CHANGED_PROPERTY,
                 Integer.class) {
             @Override
             public boolean hasListeners(Context context, Component c) {
                 return ((FlexiGrid) c).hasActivePageChangedListeners();
             }
 
             @Override
             public void processEvent(Context context, Component component, Object eventData) {
                 ((FlexiGrid) component).userActivePageChange((Integer) eventData);
             }
         });
         
         //* Event fired when client make selection */
         addEvent(new EventPeer(FlexiGrid.INPUT_TABLE_ROW_SELECTION_CHANGED,
                 FlexiGrid.TABLE_ROW_SELECTION_LISTENERS_CHANGED_PROPERTY,
                 String.class) {
             @Override
             public boolean hasListeners(Context context, Component c) {
                 return ((FlexiGrid) c).hasTableRowSelectionListeners();
             }
 
             @Override
             public void processEvent(Context context, Component component, Object eventData) {
                 final FlexiGrid flexigrid = (FlexiGrid) component;
                 final String jsonMessage = (String) eventData;
                 
                 /**
                  * <pre>
                  * Parse input JSON message:
                  * {"rowSelection": {
                  *      "allSelectedRowsIds": [1, 3, 4, 5],
                  *      "oldSelectedRowsIds": [1, 2, 3],
                  *      "newSelectedRowsIds" [4, 5],
                  *      "newUnselectedRowsIds": [2]
                  * }}
                  * </pre>
                  */
                 try {                    
                     final FlexiRowSelection rowSelection = (FlexiRowSelection) streamIn.fromXML(jsonMessage);
                     rowSelection.repair();
                     flexigrid.userTableRowSelection(rowSelection);
                 } catch (NumberFormatException e) {
                     throw new RuntimeException("Could not unmarshall rowSelection from JSON msg: '" + jsonMessage + "'", e);
                 }
             }
         });
         
         //* Event fired when ResultsPerPageOption is changed */
         addEvent(new EventPeer(FlexiGrid.INPUT_PROPERTY_RESULTS_PER_PAGE_OPTION_CHANGED, 
                 FlexiGrid.RESULTS_PER_PAGE_OPTION_LISTENERS_CHANGED_PROPERTY, Integer.class) {
             @Override
             public boolean hasListeners(Context context, Component c) {
                 return true;
             }
 
             @Override
             public void processEvent(Context context, Component component, Object eventData) {
                 final FlexiGrid flexigrid = (FlexiGrid) component;
                 final Integer initialOption = (Integer) eventData;
                 flexigrid.userResultsPerPageOptionChange(initialOption);
             }
         });
         
 
         // ---
         addEvent(new EventPeer(FlexiGrid.INPUT_TABLE_COLUMN_TOGGLE,
                 FlexiGrid.TABLE_COLUMNTOGGLE_LISTENERS_CHANGED_PROPERTY, String.class) {
             @Override
             public boolean hasListeners(Context context, Component c) {
                 return ((FlexiGrid) c).hasTableColumnToggleListeners();
             }
 
             @Override
             public void processEvent(Context context, Component component, Object eventData) {
                 final FlexiGrid flexigrid = (FlexiGrid) component;
                 final String jsonMessage = (String) eventData;
 
                 /**
                  * <pre>
                  * {"columnVisibility": {
                  *      "columnId": 0,
                  *      "visible": true
                  * }}
                  * </pre>
                  */
                 try {
                     final FlexiColumnVisibility columnToggle = (FlexiColumnVisibility) streamIn.fromXML(jsonMessage);
                     flexigrid.userTableColumnToggle(columnToggle);
                 } catch (NumberFormatException e) {
                     throw new RuntimeException("Could not unmarshall columnVisibility from JSON msg: '" + jsonMessage + "'", e);
                 }
             }
 
         });
         addEvent(new EventPeer(FlexiGrid.INPUT_TABLE_SORTING_CHANGE,
                 FlexiGrid.TABLE_COLUMNTOGGLE_LISTENERS_CHANGED_PROPERTY, String.class) {
             @Override
             public boolean hasListeners(Context context, Component c) {
                 return ((FlexiGrid) c).hasTableSortingChangeListeners();
             }
 
             @Override
             public void processEvent(Context context, Component component, Object eventData) {                
                 final FlexiGrid flexigrid = (FlexiGrid) component;
                 final String jsonMessage = (String) eventData;
                 /**
                  * <pre>
                  * {"sortingModel": {
                  *      "columns": {
                  *        "sortingColumn" : [{
                  *              "columnId": 0,
                  *              "sortOrder": "asc"
                  *          },
                  *          {
                  *           "columnId": 1,
                  *           "sortOrder": "desc"
                  *          }
                  *        }]
                  *   }}
                  * </pre>
                  */
                 try {
                     final FlexiSortingModel aSortingModel = (FlexiSortingModel) streamIn.fromXML(jsonMessage);
                     flexigrid.userTableSortingChange(aSortingModel);
                 } catch (NumberFormatException e) {
                     throw new RuntimeException("Could not unmarshall sortingModel from JSON msg== '" + jsonMessage + "'", e);
                 }
             }
         });
     }
     
     /**
      * @inheritDoc
      */
     @Override
     public void init(Context context, Component component) {
         super.init(context, component);
         
         // Obtain outgoing 'ServerMessage' for initial render.
         ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
         serverMessage.addLibrary(SharedService.ECHOCOMPONENTS_SERVICE.getId());
         serverMessage.addLibrary(SharedService.JQUERY_SERVICE.getId());
         
         // Add FlexiGrid JavaScript library to client.
         serverMessage.addLibrary(FLEXIGRID_SERVICE.getId());
         serverMessage.addLibrary(FLEXIGRID_SYNC_SERVICE.getId());
                 
         // * Configure input / output streams
         // ----------------------------------
         cellConverter = new FlexiCellConverter((FlexiGrid) component);
         columnConverter = new FlexiColumnConverter((FlexiGrid) component);
         columnsUpdateConverter = new FlexiColumnsUpdateConverter((FlexiGrid) component);
         
         // * JAVA to JSON
         // --------------        
         streamOut.registerConverter(cellConverter);
         streamOut.registerConverter(columnConverter);
 
         streamOut.alias("columnModel",          FlexiColumnModel.class);
         streamOut.alias("activePage",           FlexiPage.class);
         streamOut.alias("resultsPerPageOption", ResultsPerPageOption.class);
         streamOut.alias("sortingModel",         FlexiSortingModel.class);
         streamOut.alias("sortingColumn",        FlexiSortingColumn.class);
         streamOut.alias("columnsUpdate",        FlexiColumnsUpdate.class);
         streamOut.registerLocalConverter(FlexiColumnsUpdate.class, "updates", columnsUpdateConverter);
                 
         streamOut.processAnnotations(FlexiColumnModel.class);         
         streamOut.processAnnotations(FlexiPage.class);
         streamOut.processAnnotations(ResultsPerPageOption.class);
         streamOut.processAnnotations(FlexiSortingModel.class);
         streamOut.processAnnotations(FlexiSortingColumn.class);
         streamOut.processAnnotations(FlexiColumnsUpdate.class);
         
         // * JSON to JAVA
         // --------------
         streamIn.alias("rowSelection",         FlexiRowSelection.class);
         streamIn.alias("resultsPerPageOption", ResultsPerPageOption.class);
         streamIn.alias("columnVisibility",     FlexiColumnVisibility.class);        
         streamIn.alias("sortingModel",         FlexiSortingModel.class);
         streamIn.alias("sortingColumn",        FlexiSortingColumn.class);
         streamIn.alias("columnsUpdate",        FlexiColumnsUpdate.class);
                         
         streamIn.addImplicitArray(FlexiRowSelection.class,    "allSelectedRowsIds",   "asr");
         streamIn.addImplicitArray(FlexiRowSelection.class,    "oldSelectedRowsIds",   "osr");
         streamIn.addImplicitArray(FlexiRowSelection.class,    "newSelectedRowsIds",   "nsr");
         streamIn.addImplicitArray(FlexiRowSelection.class,    "newUnselectedRowsIds", "nur");
         streamIn.addImplicitArray(ResultsPerPageOption.class, "pageOptions",          int.class);
         
         streamIn.processAnnotations(FlexiRowSelection.class);
         streamIn.processAnnotations(FlexiColumnVisibility.class);
         streamIn.processAnnotations(FlexiSortingModel.class);
         streamIn.processAnnotations(FlexiSortingColumn.class);
         streamIn.processAnnotations(ResultsPerPageOption.class);
         streamIn.processAnnotations(FlexiColumnsUpdate.class);
         streamIn.setMode(XStream.NO_REFERENCES);
     }
     
 
     @Override
     public String getClientComponentType(boolean shortType) {
         // Return client-side component type name.
         return "exxcellent.FlexiGrid";
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Class getComponentClass() {
         // Return server-side Java class.
         return FlexiGrid.class;
     }
 
     /**
      * Over-ridden to handle requests for the {@link de.exxcellent.echolot.app.FlexiGrid} properties. The collection of tag instances are serialised
      * as a JSON stucture.
      *
      * @see nextapp.echo.webcontainer.ComponentSynchronizePeer#getOutputProperty(Context, Component, String, int)
      */
     @Override
     public Object getOutputProperty(final Context context, final Component component, final String propertyName, final int propertyIndex) {            
         if (FlexiGrid.PROPERTY_ACTIVE_PAGE.equals(propertyName)) {
             FlexiPage activePage = ((FlexiGrid) component).getActivePage();
             return activePage == null ? null : streamOut.toXML(activePage);
         } else if (FlexiGrid.PROPERTY_COLUMNMODEL.equals(propertyName)) {
             return streamOut.toXML(((FlexiGrid) component).getColumnModel());
         } else if (FlexiGrid.PROPERTY_RESULTS_PER_PAGE_OPTION.equals(propertyName)) {
             return streamOut.toXML(((FlexiGrid) component).getResultsPerPageOption());
         } else if (FlexiGrid.PROPERTY_SORTINGMODEL.equals(propertyName)) {
             return streamOut.toXML(((FlexiGrid) component).getSortingModel());
         } else if (FlexiGrid.PROPERTY_TABLE_ROW_SELECTION.equals(propertyName)) {
             return Arrays.toString(((FlexiGrid) component).getSelectedRowsIds());
         } else if (FlexiGrid.PROPERTY_FLEXICOLUMNS_UPDATE.equals(propertyName)) {
             FlexiColumnsUpdate updates = (FlexiColumnsUpdate) component.get(FlexiGrid.PROPERTY_FLEXICOLUMNS_UPDATE);
             String json = streamOut.toXML(updates);
             updates.clear();
             return json;
         }
         return super.getOutputProperty(context, component, propertyName, propertyIndex);
     }
 
 //  @Override
 //  public void storeInputProperty(Context cntxt, Component cmpnt, String string, int i, Object o)
 //  {
 //    super.storeInputProperty(cntxt, cmpnt, string, i, o);
 //  }
 //
 //  @Override
 //  public Class getInputPropertyClass(String string)
 //  {
 //    return super.getInputPropertyClass(string);
 //  }
 }
