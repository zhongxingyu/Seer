 /*
 * Original Code developed and contributed by Prime Technology.
 * Subsequent Code Modifications Copyright 2011 ICEsoft Technologies Canada Corp. (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
 *
 * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
 *
 * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
 * Contributors: ICEsoft Technologies Canada Corp. (c)
 */
 
 package org.icefaces.ace.component.datatable;
 
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.rowexpander.RowExpander;
 import org.icefaces.ace.component.rowpanelexpander.RowPanelExpander;
 import org.icefaces.ace.component.tableconfigpanel.TableConfigPanel;
 import org.icefaces.ace.event.*;
 import org.icefaces.ace.model.MultipleExpressionComparator;
 import org.icefaces.ace.model.filter.ContainsFilterConstraint;
 import org.icefaces.ace.model.table.LazyDataModel;
 import org.icefaces.ace.model.table.*;
 import org.icefaces.ace.model.table.SortCriteria;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.collections.AllPredicate;
 import org.icefaces.ace.util.collections.AnyPredicate;
 import org.icefaces.ace.util.collections.Predicate;
 import org.icefaces.ace.util.collections.PropertyConstraintPredicate;
 
 import javax.el.MethodExpression;
 import javax.faces.application.Application;
 import javax.faces.application.NavigationHandler;
 import javax.faces.component.*;
 import javax.faces.component.behavior.AjaxBehavior;
 import javax.faces.component.behavior.ClientBehavior;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitHint;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PostValidateEvent;
 import javax.faces.event.PreValidateEvent;
 import javax.faces.model.*;
 import javax.swing.tree.TreeModel;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.sql.ResultSet;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.ResultDataModel;
 import javax.faces.model.ResultSetDataModel;
 import javax.faces.model.ScalarDataModel;
 
 public class DataTable extends DataTableBase {
     private static Logger log = Logger.getLogger(DataTable.class.getName());
     private static Class SQL_RESULT = null;
     public static final String CONTAINER_CLASS = "ui-datatable ui-widget";
     public static final String COLUMN_HEADER_CLASS = "ui-widget-header";
     public static final String COLUMN_HEADER_CONTAINER_CLASS = "ui-header-column";
     public static final String COLUMN_FOOTER_CLASS = "ui-widget-header";
     public static final String COLUMN_FOOTER_CONTAINER_CLASS = "ui-footer-column";
     public static final String DATA_CLASS = "ui-datatable-data ui-widget-content";
     public static final String EMPTY_DATA_CLASS = "ui-datatable-data-empty";
     public static final String ROW_CLASS = "";
     public static final String HEADER_CLASS = "ui-datatable-header ui-widget-header ui-corner-tl ui-corner-tr";
     public static final String HEADER_RIGHT_CLASS = "ui-header-right";
     public static final String FOOTER_CLASS = "ui-datatable-footer ui-widget-header ui-corner-bl ui-corner-br";
     public static final String HEAD_TEXT_CLASS = "ui-header-text";
     public static final String SORTABLE_COLUMN_CLASS = "ui-sortable-column";
     public static final String SORTABLE_COLUMN_CONTROL_CLASS = "ui-sortable-control";
     public static final String SORTABLE_COLUMN_ICON_CONTAINER = "ui-sortable-column-icon";
     public static final String SORTABLE_COLUMN_ICON_UP_CLASS = "ui-icon ui-icon-triangle-1-n";
     public static final String SORTABLE_COLUMN_ICON_DOWN_CLASS = "ui-icon ui-icon-triangle-1-s";
     public static final String SORTABLE_COLUMN_ORDER_CLASS = "ui-sortable-column-order";
     public static final String COLUMN_FILTER_CLASS = "ui-column-filter";
     public static final String UNSELECTABLE_ROW_CLASS = "ui-unselectable";
     public static final String REORDERABLE_COL_CLASS = "ui-reorderable-col";
     public static final String EXPANDED_ROW_CLASS = "ui-expanded-row";
     public static final String EXPANDED_ROW_CONTENT_CLASS = "ui-expanded-row-content";
     public static final String ROW_PANEL_TOGGLER_CLASS = "ui-row-panel-toggler";
     public static final String ROW_TOGGLER_CLASS = "ui-row-toggler";
     public static final String EDITABLE_COLUMN_CLASS = "ui-editable-column";
     public static final String CELL_EDITOR_CLASS = "ui-cell-editor";
     public static final String CELL_EDITOR_INPUT_CLASS = "ui-cell-editor-input";
     public static final String CELL_EDITOR_OUTPUT_CLASS = "ui-cell-editor-output";
     public static final String ROW_EDITOR_COLUMN_CLASS = "ui-row-editor-column";
     public static final String ROW_EDITOR_CLASS = "ui-row-editor";
     public static final String SELECTION_COLUMN_CLASS = "ui-selection-column";
     public static final String EVEN_ROW_CLASS = "ui-datatable-even";
     public static final String ODD_ROW_CLASS = "ui-datatable-odd";
     public static final String SCROLLABLE_X_CLASS = "ui-datatable-scroll-x";
     public static final String SCROLLABLE_CONTAINER_CLASS = "ui-datatable-scrollable";
     public static final String SCROLLABLE_HEADER_CLASS = "ui-datatable-scrollable-header";
     public static final String SCROLLABLE_BODY_CLASS = "ui-datatable-scrollable-body";
     public static final String SCROLLABLE_FOOTER_CLASS = "ui-datatable-scrollable-footer";
     public static final String COLUMN_RESIZER_CLASS = "ui-column-resizer";
 
     private Map<String, Column> filterMap;
     private Boolean newTreeDataModel = false;
 
     static {
         try {
             SQL_RESULT = Class.forName("javax.servlet.jsp.jstl.sql.Result");
         } catch (Throwable t)  {
             //ignore if sql.result not available
         }
     }
 
     /*#######################################################################*/
     /*###################### Overridden API #################################*/
     /*#######################################################################*/
     RowStateMap stateMap;
     @Override
     public RowStateMap getStateMap() {
         if (stateMap != null) return stateMap;
 
         stateMap = super.getStateMap();
         if (stateMap == null) {
             stateMap = new RowStateMap();
             super.setStateMap(stateMap);
         }
 
         return stateMap;
     }
 
     // Allow renderer to void state map between iterations to avoid
     // sharing stateMap due to caching
     // (caching is necessary to avoid attempting to load a stateMap when the clientId contains a row index)
     protected void clearCachedStateMap() {
         stateMap = null;
     }
 
     @Override
     public Object getValue() {
         Object superValue = super.getValue();
         int superValueHash;
         if (superValue != null) superValueHash = superValue.hashCode();
         else return null;
 
         if (valueHashCode == null || superValueHash != valueHashCode) {
             valueHashCode = superValueHash;
             applySorting();
             if (getFilteredData() != null) applyFilters();
             if (superValue != null && superValue instanceof List) {
                 List list = (List)superValue;
                 if (list.size() > 0 && list.get(0) instanceof Map.Entry) {
                     newTreeDataModel = true;
                 }
             }
         }
 
         List filteredValue = getFilteredData();
         return (filteredValue != null) ? filteredValue : super.getValue();
     }
 
     @Override
     protected DataModel getDataModel() {
         if (this.model != null) {
             return (model);
         }
 
         Object current = getValue();
 
         // If existing tree check for changes or return cached model
         if (hasTreeDataModel() || newTreeDataModel) {
             treeModel = new TreeDataModel((List)current);
             setDataModel(treeModel);
             newTreeDataModel = false;
             return treeModel;
         }
 
         if (current == null) {
             setDataModel(new ListDataModel(Collections.EMPTY_LIST));
         } else if (current instanceof DataModel) {
             setDataModel((DataModel) current);
         } else if (current instanceof List) {
             setDataModel(new ListDataModel((List) current));
         } else if (Object[].class.isAssignableFrom(current.getClass())) {
             setDataModel(new ArrayDataModel((Object[]) current));
         } else if (current instanceof ResultSet) {
             setDataModel(new ResultSetDataModel((ResultSet) current));
         } else if ((null != SQL_RESULT) && SQL_RESULT.isInstance(current)) {
             DataModel dataModel = new ResultDataModel();
             dataModel.setWrappedData(current);
             setDataModel(dataModel);
         } else {
             setDataModel(new ScalarDataModel(current));
         }
 
         return model;
     }
 
     private DataModel model;
     @Override
     protected void setDataModel(DataModel dataModel) {
         this.model = dataModel;
     }
 
     @Override
     public void broadcast(javax.faces.event.FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
 
         FacesContext context = FacesContext.getCurrentInstance();
         String outcome = null;
         MethodExpression me = null;
 
         if      (event instanceof SelectEvent)   me = getRowSelectListener();
         else if (event instanceof UnselectEvent) me = getRowUnselectListener();
         else if (event instanceof TableFilterEvent) me = getFilterListener();
 
         if (me != null) outcome = (String) me.invoke(context.getELContext(), new Object[] {event});
 
         if (outcome != null) {
             NavigationHandler navHandler = context.getApplication().getNavigationHandler();
             navHandler.handleNavigation(context, null, outcome);
             context.renderResponse();
         }
     }
 
     @Override
     public void processUpdates(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
 
         pushComponentToEL(context, this);
         //preUpdate(context);
         iterate(context, PhaseId.UPDATE_MODEL_VALUES);
         popComponentFromEL(context);
         // This is not a EditableValueHolder, so no further processing is required
     }
 
         @Override
         public void processDecodes(FacesContext context) {
            // Required to prevent input component processing on sort, filter, tableconf and pagination initiated submits.
            if (isDataManipulationRequest(context) || isTableConfigurationRequest(context)) {
                this.decode(context);
                context.renderResponse();
            } else {
                 if (context == null) {
                     throw new NullPointerException();
                 }
                 if (!isRendered()) {
                     return;
                 }
 
                 pushComponentToEL(context, this);
                 //super.preDecode() - private and difficult to port
                 iterate(context, PhaseId.APPLY_REQUEST_VALUES);
                 decode(context);
                 popComponentFromEL(context);
            }
 
         if (isFilterValueChanged() == true) {
             Map<String, String> params = context.getExternalContext().getRequestParameterMap();
             queueEvent(
                     new TableFilterEvent(this,
                             getFilterMap().get(params.get(getClientId(context) + "_filteredColumn")))
             );
         }
     }
 
     @Override
     public void processValidators(FacesContext context) {
 
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
         pushComponentToEL(context, this);
         Application app = context.getApplication();
         app.publishEvent(context, PreValidateEvent.class, this);
         //preValidate(context);
         iterate(context, PhaseId.PROCESS_VALIDATIONS);
         app.publishEvent(context, PostValidateEvent.class, this);
         popComponentFromEL(context);
     }
 
     @Override
     public int getFirst() {
         return isPaginator() ? super.getFirst() : 0;
     }
 
     @Override
     public void setRowIndex(int index) {
         Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
         
         super.setRowIndex(index);
         if (isRowAvailable() && index > -1) {
             requestMap.put(getRowStateVar(), getStateMap().get(getRowData()));
         }
     }
 
     
     
 
 
     /*#######################################################################*/
     /*###################### Public API #####################################*/
     /*#######################################################################*/
 
     /**
      * A public proxy to the getDataModel() method, intended for use in situations
      * where a sub-component needs access to a custom DataModel object.
      *
      * @return java.faces.model.DataModel instance currently used by this table
      */
     public DataModel getModel() {
         return getDataModel();
     }
 
     /**
      * A public proxy to the getDataModel() method, intended for use in situations
      * where a sub-component needs to null a cached DataModel to force regeneration.
      *
      * @return java.faces.model.DataModel instance currently used by this table
      */
     public void setModel(DataModel m) {
         try { setDataModel(null); }
         catch (UnsupportedOperationException uoe) {
             //MyFaces doesn't support this method and throws an UnsupportedOperationException
         }
     }
 
     /**
      * Determine if this DataTable is using a custom ICEFaces TreeDataModel.
      * @return true, if a TreeDataModel instance is the result of getDataModel()
      */
     public Boolean hasTreeDataModel() {
         return (treeModel != null);
     }
 
     /**
      * If a RowPanelExpander component is a child of this table, return it.
      * This is intended for table sub-components to vary their behavior varied
      * on the presence of RowPanelExpanders and/or RowExpanders.
      * @return RowPanelExpander child of the table, or null
      */
     public RowPanelExpander getPanelExpansion() {
         for (UIComponent kid : getChildren())
             if (kid instanceof RowPanelExpander) return (RowPanelExpander) kid;
         return null;
     }
 
     /**
      * If a RowExpander component is a child of this table, return it.
      * This is intended for table sub-components to vary their behavior varied
      * on the presence of RowPanelExpanders and/or RowExpanders.
      * @return RowExpander child of the table, or null
      */
     public RowExpander getRowExpansion() {
         for (UIComponent kid : getChildren())
             if (kid instanceof RowExpander) return (RowExpander) kid;
         return null;
     }
 
     /**
      * Generates the list of DataTable Column children, reordered according to the
      * column ordering property. Note this list doesn't return Column components used
      * in a ColumnGroup to define the header.
      * @return List of ACE Column Components.
      */
     public List<Column> getColumns() {
         ArrayList<Column> columns = new ArrayList<Column>();
         List<Integer> columnOrdering = generateColumnOrdering();
 
         ArrayList<Column> unordered = new ArrayList<Column>();
         Stack childStack = new Stack<UIComponent>();
         childStack.add(this);
         while (!childStack.empty()) {
             for (UIComponent child : ((UIComponent)childStack.pop()).getChildren()) {
                 if (!(child instanceof ColumnGroup) && !(child instanceof Column) && !(child instanceof DataTable)) {
                     if (child.getChildren().size() > 0) childStack.add(child);
                 } else if (child instanceof Column) unordered.add((Column) child);
             }
         }
 
         // Allow the ordering to grow beyond the current set of columns,
         // to allow persistence of order during column swaps.
         while (columnOrdering.size() < unordered.size()) columnOrdering.add(columnOrdering.size());
 
         for (Integer i : columnOrdering)
             if (i < unordered.size()) columns.add(unordered.get(i));
 
         return columns;
     }
 
     /**
      * Generates a list of DataTable Column children intended to render the header, either from the header segement, or
      * from the normal grouping of columns.
      * @return List of ACE Column Components.
      */
     public List<Column> getHeaderColumns() {
         return null;
     }
 
     /**
      * Associates this table with a particular TableConfigPanel component. That
      * table will render the launch controls, and be configured by the specified
      * panel.
      * @param TableConfigPanel component that will configure this table.
      */
     public void setTableConfigPanel(TableConfigPanel panel) {
         this.panel = panel;
         FacesContext c = FacesContext.getCurrentInstance();
         setTableConfigPanel(panel.getClientId(c)) ;
     }
 
     /**
      * Sets the property value of this dataTable to null.
      */
     public void resetValue() {
         setValue(null);
     }
 
     /**
      * Sets the position of pagination in the table to the first page.
      */
     public void resetPagination() {
         setFirst(0);
         setPage(1);
     }
 
     /**
      * Sets the property of this table to null, clears all filters and resets pagination.
      */
     public void reset() {
         resetValue();
         resetFilters();
         resetPagination();
     }
 
     /**
      * Blanks the filterValue property of each Column component and removes the
      * presently filtered set of data.
      */
     public void resetFilters() {
         for (Column c : getColumns()) {
             c.setFilterValue("");
         }
         setFilterValue("");
         setFilteredData(null);
     }
 
     /**
      * Processes any changes to sortPriority or sortAscending properties of Columns
      * to the data model; resorting the table according to the new settings.
      */
     public void applySorting() {
         sortOrderChanged = true;
     }
 
     /**
      * Processes any changes to the filterInput property of the Columns to the data model;
      * refiltering the data model to meet the new criteria.
      */
     public void applyFilters() {
         setFilteredData(null);
         clearDataModel = true;
         filterValueChanged = true;
     }
 
     public boolean isFilterValueChanged() {
         return (isConstantRefilter()) ? true : filterValueChanged;
     }
 
     public boolean isSortOrderChanged() {
         return sortOrderChanged;
     }
 
 
 
     /*#######################################################################*/
     /*###################### Protected API ##################################*/
     /*#######################################################################*/
     protected boolean isPaginationRequest(FacesContext x)         { return isIdPrefixedParamSet("_paging", x); }
     protected boolean isTableConfigurationRequest(FacesContext x) { return isIdPrefixedParamSet("_tableconf", x); }
     protected boolean isColumnReorderRequest(FacesContext x)      { return isIdPrefixedParamSet("_columnReorder", x); }
     protected boolean isSortRequest(FacesContext x)               { return isIdPrefixedParamSet("_sorting", x); }
     protected boolean isFilterRequest(FacesContext x)             { return isIdPrefixedParamSet("_filtering", x); }
     protected boolean isInstantSelectionRequest(FacesContext x)   { return isIdPrefixedParamSet("_instantSelectedRowIndex", x); }
     protected boolean isInstantUnselectionRequest(FacesContext x) { return isIdPrefixedParamSet("_instantUnselectedRowIndex", x); }
     protected boolean isScrollingRequest(FacesContext x)          { return isIdPrefixedParamSet("_scrolling", x); }
     protected boolean isDataManipulationRequest(FacesContext x)   { return isPaginationRequest(x) || isFilterRequest(x); }
 
 
     protected Map<String,Column> getFilterMap() {
         if (filterMap == null) {
             filterMap = new HashMap<String,Column>();
             ColumnGroup group = getColumnGroup("header");
             if (group != null) {
                 for (UIComponent child : group.getChildren())
                     if (child.isRendered()) for (UIComponent grandchild : child.getChildren())
                         if (grandchild.isRendered() && grandchild.getValueExpression("filterBy") != null)
                             filterMap.put(grandchild.getClientId(FacesContext.getCurrentInstance()) + "_filter", (Column)grandchild);
             } else
                 for (Column column : getColumns())
                     if (column.getValueExpression("filterBy") != null)
                         filterMap.put(column.getClientId(FacesContext.getCurrentInstance()) + "_filter", column);
         }
         return filterMap;
     }
 
     protected ColumnGroup getColumnGroup(String target) {
         for (UIComponent child : this.getChildren())
             if (child instanceof ColumnGroup) {
                 ColumnGroup colGroup = (ColumnGroup) child;
                 if (target.equals(colGroup.getType())) return colGroup;
             }
         return null;
     }
 
     protected SortCriteria[] getSortCriteria() {
         ArrayList<Column> sortableColumns = new ArrayList<Column>();
 
         ColumnGroup group = getColumnGroup("header");
         if (group != null) {
             for (UIComponent child : group.getChildren()) { // child is a Row
                 for (UIComponent headerRowChild : child.getChildren()) {
                     if (headerRowChild instanceof Column) {
                         Column c = (Column)headerRowChild;
                         if (c.getSortPriority() != null) {
                             sortableColumns.add(c);
                         }
                     }
                 }
             }
         } else {
             for (Column c : getColumns()) {
                 if (c.getSortPriority() != null) {
                     sortableColumns.add(c);
                 }
             }
         }
 
         Collections.sort(sortableColumns, new PriorityComparator());
 
         SortCriteria[] criterias = new SortCriteria[sortableColumns.size()];
         int i = 0;
         for (Column c : sortableColumns) {
             Comparator<Object> comp = c.getSortFunction();
             if (comp == null) criterias[i] = new SortCriteria(c.getValueExpression("sortBy"), c.isSortAscending());
             else criterias[i] = new SortCriteria(c.getValueExpression("sortBy"), c.isSortAscending(), comp);
             i++;
         }
         return criterias;
     }
 
     protected Map<String,String> getFilters() {
         HashMap<String, String> map = new HashMap<String, String>();
         for (Column c : getColumns()) {
             String value = c.getFilterValue();
             if (value != null && (value.length() > 0))
                 map.put(ComponentUtils.resolveField(c.getValueExpression("filterBy")), value);
         }
         return map;
     }
 
     protected List getFilteredData() {
         return this.filteredData;
     }
 
     protected boolean hasHeaders() {
         for (UIComponent c : getChildren()) {
             if (c instanceof Column && ((c.getFacet("header") != null) || (((Column)c).getHeaderText() != null))) return true;
             else if (c instanceof ColumnGroup && ((ColumnGroup)c).getType().equals("header")) return true;
         }
 
         return false;
     }
 
     public boolean hasSelectionClientBehaviour() {
         for (String eventId : getClientBehaviors().keySet()) {
             if (eventId.equals("select") || eventId.equals("deselect")) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean hasFooterColumn(List<Column> columns) {
         for (Column column : columns)
             if (column.getFacet("footer") != null || column.getFooterText() != null)
                 return true;
         return false;
     }
 
     protected boolean isSelectionEnabled() {
         return this.getSelectionMode() != null;
     }
 
     protected boolean isCellSelection() {
         String selectionMode = this.getSelectionMode();
         if (selectionMode != null) return selectionMode.indexOf("cell") != -1;
         else return false;
     }
 
     protected boolean isSingleSelectionMode() {
         String selectionMode = this.getSelectionMode();
         //String columnSelectionMode = this.getColumnSelectionMode();
         if (selectionMode != null)
             return selectionMode.equalsIgnoreCase("single") || selectionMode.equalsIgnoreCase("singlecell");
             //else if (columnSelectionMode != null)
             //return columnSelectionMode.equalsIgnoreCase("single");
         else return false;
     }
 
     protected void setFilteredData(List list) {
         this.filteredData = list;
     }
 
     protected TableConfigPanel findTableConfigPanel(FacesContext context) {
         if (panel == null & getTableConfigPanel() != null) {
             panel = (TableConfigPanel)this.findComponent(getTableConfigPanel());
 
             if (panel == null)
                 for (UIComponent child : getChildren())
                     if (child instanceof TableConfigPanel)
                         panel = (TableConfigPanel)child;
         }
         return panel;
     }
 
     protected void setColumnOrdering(String[] indexes) {
         ArrayList<Integer> ints = new ArrayList<Integer>();
         int i;
         for (String index : indexes)
             ints.add(Integer.parseInt(index));
 
         setColumnOrdering(ints);
     }
 
     protected void calculatePage() {
         int rows = this.getRows();
         int currentPage = this.getPage();
         int numberOfPages = (int) Math.ceil(this.getRowCount() * 1d / rows);
 
         // If paging to beyond the last page.
         if (currentPage > numberOfPages && numberOfPages > 0) {
             this.setPage(numberOfPages);
         } else if (currentPage < 1) {
             this.setPage(1);
         }
 
         this.setFirst((this.getPage()-1) * rows);
     }
 
     protected void processSorting() {
         Object value = getValue();
         if (value instanceof List) {
             List list = (List)value;
             SortCriteria[] criterias = getSortCriteria();
             String rowVar = getVar();
 
             if (criterias != null && criterias.length > 0) {
                 if (list.size() > 0 && list.get(0) instanceof Map.Entry)
                     Collections.sort(list, new EntryKeyComparatorWrapper(new MultipleExpressionComparator(criterias, rowVar)));
                 else
                     Collections.sort(list, new MultipleExpressionComparator(criterias, rowVar));
             }
         }
         sortOrderChanged = false;
     }
 
     protected List processFilters(FacesContext context) {
         try {
             Map<String, Column> filterMap = getFilterMap();
             String globalFilter = getFilterValue();
             List<Predicate> columnPredicates = new ArrayList<Predicate>();
             List<Predicate> globalPredicates = new ArrayList<Predicate>();
             boolean hasGlobalFilter = (globalFilter != null && !globalFilter.equals(""));
             if (hasGlobalFilter) globalFilter = globalFilter.toLowerCase();
 
             // Setup filter objects from column properties
             for (Column c : filterMap.values()) {
                 if (c.getFilterValue() != null && !c.getFilterValue().equals("")) {
                     columnPredicates.add(
                             new PropertyConstraintPredicate(context,
                                     c.getValueExpression("filterBy"),
                                     c.getFilterValue(),
                                     c.getFilterConstraint()));
                 }
                 //TODO: Add global filter constraint configurability
                 if (hasGlobalFilter)
                     globalPredicates.add(new PropertyConstraintPredicate(
                             context,
                             c.getValueExpression("filterBy"),
                             globalFilter,
                             new ContainsFilterConstraint()));
             }
 
             if (globalPredicates.size() + columnPredicates.size() == 0)
                 return null;
 
             List filteredData = new ArrayList();
             setFilteredData(null);
             DataModel model = getDataModel();
             TreeDataModel treeModel = hasTreeDataModel() ? (TreeDataModel)model : null;
             String rowVar = getVar();
             String rowStateVar = getRowStateVar();
 
             // If the global predicate is set, require one column must meet the criteria of the global predicate
             if (globalPredicates.size() > 0) columnPredicates.add(AnyPredicate.getInstance(globalPredicates));
             Predicate filterSet = AllPredicate.getInstance(columnPredicates);
             int index = 0;
 
             // UIData Iteration
             model.setRowIndex(index);
             while (model.isRowAvailable()) {
 
                 Object rowData = model.getRowData();
                 RowState rowState = getStateMap().get(rowData);
 
                 if (rowVar != null) context.getExternalContext().getRequestMap().put(rowVar, rowData);
                 context.getExternalContext().getRequestMap().put(rowStateVar, rowState);
 
                 if  (filterSet.evaluate(rowData)) {
                     if (treeModel != null) filteredData.add(treeModel.getRowEntry());
                     else filteredData.add(model.getRowData());
                 }
                 index++;
                 model.setRowIndex(index);
             }
             // Iteration clean up
             setRowIndex(-1);
             if (rowVar != null) context.getExternalContext().getRequestMap().remove(rowVar);
             context.getExternalContext().getRequestMap().remove(getRowStateVar());
             return filteredData;
         } finally {
             this.filterValueChanged = false;
         }
     }
 
     protected void loadLazyData() {
         LazyDataModel model = (LazyDataModel) getDataModel();
         model.setPageSize(getRows());
         model.setWrappedData(model.load(getFirst(), getRows(), getSortCriteria(), getFilters()));
     }
 
 
 
 
     /*#######################################################################*/
     /*###################### Private Methods ################################*/
     /*#######################################################################*/
     private boolean isIdPrefixedParamSet(String param, FacesContext x) {
         return x.getExternalContext().getRequestParameterMap().containsKey(this.getClientId(x) + param);
     }
 
     private List<Integer> generateColumnOrdering() {
         List<Integer> superOrder = super.getColumnOrdering();
         if (superOrder == null || superOrder.size() == 0) {
             ArrayList<Column> columns = new ArrayList<Column>();
             Stack childStack = new Stack<UIComponent>();
             childStack.add(this);
             while (!childStack.empty()) {
                 for (UIComponent child : ((UIComponent)childStack.pop()).getChildren()) {
                     if (!(child instanceof ColumnGroup) && !(child instanceof Column)) {
                         if (child.getChildren().size() > 0) childStack.add(child);
                     } else if (child instanceof Column) columns.add((Column) child);
                 }
             }
             ArrayList<Integer> ordering = new ArrayList<Integer>();
             int i=0;
             for (Object o : columns) ordering.add(i++);
             setColumnOrdering(ordering);
             return ordering;
         } else return superOrder;
     }
 
 
 
 
 
     /*#######################################################################*/
     /*###################### VisitTree Impl #################################*/
     /*#######################################################################*/
     @Override
     public boolean visitTree(VisitContext context, VisitCallback callback) {
         boolean ret = false;
         if (this.isVisitable(context)) {
             boolean visitRows = requiresRowIteration(context);
 
             int savedIndex = -1;
             if (visitRows) {
                 savedIndex = getRowIndex();
                 setRowIndex(-1);
             }
 
             this.pushComponentToEL(FacesContext.getCurrentInstance(), this);
             try {
                 VisitResult result = context.invokeVisitCallback(this, callback);
                 if (result.equals(VisitResult.COMPLETE)) return true;
                 if (doVisitChildren(context, visitRows) & result == VisitResult.ACCEPT) {
                     if (visitFacets(context, callback, visitRows)) return true;
                     if (visitColumnsAndColumnFacets(context, callback, visitRows)) return true;
                     if (visitRowsAndExpandedRows(context, callback, visitRows)) return true;
                 }
             } finally {
                 this.popComponentFromEL(FacesContext.getCurrentInstance());
                 //TODO: Fix this duplication
                 this.setRowIndex(savedIndex);
                 if (visitRows) setRowIndex(savedIndex);
             }
         }
         return ret;
     }
 
     private boolean requiresRowIteration(VisitContext ctx) {
         try { // Use JSF 2.1 hints if available
             return !ctx.getHints().contains(VisitHint.SKIP_ITERATION);
         } catch (NoSuchFieldError e) {
             FacesContext fctx = FacesContext.getCurrentInstance();
             return (!PhaseId.RESTORE_VIEW.equals(fctx.getCurrentPhaseId()));
         }
     }
 
     private boolean visitFacets(VisitContext context, VisitCallback callback, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values())
                 if (facet.visitTree(context, callback)) return true;
         }
         return false;
     }
 
     private boolean visitColumnsAndColumnFacets(VisitContext context, VisitCallback callback, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         if (getChildCount() > 0) {
             for (UIComponent column : getChildren()) {
                 if (column instanceof Column || column instanceof RowPanelExpander) {
                     VisitResult result = context.invokeVisitCallback(column, callback); // visit the column directly
                     if (result == VisitResult.COMPLETE) return true;
                     if (column.getFacetCount() > 0) {
                         for (UIComponent columnFacet : column.getFacets().values()) {
                             if (columnFacet.visitTree(context, callback)) {
                                 return true;
                             }
                         }
                     }
                 } else if (column instanceof ColumnGroup) {
                     UIComponent columnGroup = column;
                     for (UIComponent row : columnGroup.getChildren()) {
                         for (UIComponent c : row.getChildren()) {
                             if (c instanceof Column) {
                                 VisitResult result = context.invokeVisitCallback(c, callback); // visit the column directly
                                 if (result == VisitResult.COMPLETE) return true;
                                 if (c.getFacetCount() > 0) {
                                     for (UIComponent columnFacet : c.getFacets().values()) {
                                         if (columnFacet.visitTree(context, callback)) {
                                             return true;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean visitRowsAndExpandedRows(VisitContext context, VisitCallback callback, boolean visitRows) {
         int rows = 0;
         int offset = 0;
         int first = getFirst();
         Object model = this.getDataModel();
         RowPanelExpander panelExpander = getPanelExpansion();
         RowExpander rowExpander = getRowExpansion();
         boolean hasPanelExpansion = (panelExpander != null);
         boolean hasRowExpansion = (rowExpander != null);
 
         RowStateMap stateMap = null;
 
         if (visitRows) {
             stateMap = this.getStateMap();
             rows = getRows();
             // If a indeterminate number of rows are shown, visit all rows.
             if (rows == 0) rows = getRowCount();
         }
 
         while (true) {
             if (visitRows) {
                 if (offset >= rows) break;
                 this.setRowIndex(first + offset);
             }
 
             if (!visitRows || isRowAvailable()) {
                 RowState rowState = null;
                 if (visitRows) rowState = stateMap.get(getRowData());
 
                 // Check for tree case
                 if (hasTreeDataModel()) {
                     String currentRootId = "";
                     TreeDataModel dataModel = ((TreeDataModel)this.getDataModel());
                     // Handle row and loop down the tree if expanded.
                     try {
                         do {
                             if (log.isLoggable(Level.FINEST)) log.finest("Visiting Row Id: " + dataModel.getRowIndex());
 
                             // Visit row in tree case.
                             if (getChildCount() > 0) {
                                 for (UIComponent kid : getChildren()) {
                                     if (!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander)) {
                                         continue;
                                     }
                                     if (kid.getChildCount() > 0) {
                                         for (UIComponent grandkid : kid.getChildren()) {
                                             if (grandkid.visitTree(context, callback)) {
                                                 return true;
                                             }
                                         }
                                     }
                                 }
                             }
 
                             // Handle recursive case
                             // If this row is expanded and has children, set it as the root & keep looping.
                             if (rowState != null && rowState.isExpanded() && dataModel.getCurrentRowChildCount() > 0) {
                                 currentRootId =  currentRootId.equals("") ? (this.getRowIndex()+"") : (currentRootId + "." + getRowIndex());
                                 dataModel.setRootIndex(currentRootId);
                                 this.setRowIndex(0);
                             } else if (dataModel.getRowIndex() < dataModel.getRowCount()-1) {
                                 this.setRowIndex(dataModel.getRowIndex() + 1);
                             } else if (!currentRootId.equals("")) {
                                 // changing currrent node id to reflect pop
                                 this.setRowIndex(dataModel.pop() + 1);
                                 currentRootId = (currentRootId.lastIndexOf('.') != -1)  ? currentRootId.substring(0,currentRootId.lastIndexOf('.')) : "";
                                 if (log.isLoggable(Level.FINEST)) log.finest("Popping Root: " + currentRootId);
                             }
                             // Break out of expansion recursion to continue root node
                             if (currentRootId.equals("")) break;
                         } while (true);
                     } finally { dataModel.setRootIndex(null); }
                 } else {
                     // Visit row in plain model case.
                     if (getChildCount() > 0) {
                         for (UIComponent kid : getChildren()) {
                             if (!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander)) {
                                 continue;
                             }
                             if (kid.getChildCount() > 0) {
                                 for (UIComponent grandkid : kid.getChildren()) {
                                     if (grandkid.visitTree(context, callback)) {
                                         return true;
                                     }
                                 }
                             }
                         }
                     }
                 }
             } else return false;
 
             if (!visitRows) break;
             offset++;
         }
         return false;
     }
 
     private boolean doVisitChildren(VisitContext context, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
         assert(idsToVisit != null);
         // non-empty collection means we need to visit our children.
         return (!idsToVisit.isEmpty());
     }
 
 
 
 
 
     /*#######################################################################*/
     /*############### Fields & Custom State Saving Impl #####################*/
     /*#######################################################################*/
     protected java.lang.Boolean clearDataModel = false;
     // Subset of value reevaluated when necessary, see constantRefilter for more details.
     protected java.util.List filteredData;
     // Flag to process sorting, occurs pre-render, set true when a sort request comes in
     // by modifying a sort control, when applySorting() is called, or when the hashCode of
     // value changes. Initializes to true to enable the first render to process default sort
     // state set in the facelet source.
     protected java.lang.Boolean sortOrderChanged = true;
     // Cached treeModel reference evaluated when valueHashCode changes and the value is of the type
     protected org.icefaces.ace.model.table.TreeDataModel treeModel;
     // Detect when the hashCode of value hash changed to trigger check for TreeDataModel handling
     protected java.lang.Integer valueHashCode;
     // Flag to process filtering, occurs pre-render, set true when a filter request comes in
     // by modifying a filter input field, when applyFiltering() is called, or when the hashCode
     // of value changes. Initializes to true to enable the first render to process default filter
     // state set in the facelet source.
     protected java.lang.Boolean filterValueChanged = true;
     private TableConfigPanel panel;
 
     private Object[] values;
 
     public Object saveState(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (values == null) {
             values = new Object[7];
         }
         values[0] = super.saveState(context);
         values[1] = clearDataModel;
         values[2] = filteredData;
         values[3] = sortOrderChanged;
         values[4] = treeModel;
         values[5] = valueHashCode;
         values[6] = filterValueChanged;
         return (values);
     }
 
     public void restoreState(FacesContext context, Object state) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (state == null) {
             return;
         }
         values = (Object[]) state;
         super.restoreState(context, values[0]);
         clearDataModel = (java.lang.Boolean) values[1];
         filteredData = (java.util.List) values[2];
         sortOrderChanged = (java.lang.Boolean) values[3];
         treeModel = (org.icefaces.ace.model.table.TreeDataModel) values[4];
         valueHashCode = (java.lang.Integer) values[5];
         filterValueChanged = (java.lang.Boolean) values[6];
     }
 
 
 
 
 
     /*#######################################################################*/
     /*###################### ClientId Impl ##################################*/
     /*#######################################################################*/
     private String baseClientId = null;
     private int baseClientIdLength;
     private StringBuilder clientIdBuilder = null;
     private Boolean isNested = null;
     public String getBaseClientId(FacesContext context) {
         if (baseClientId == null && clientIdBuilder == null) {
             if (!isNestedWithinUIData()) {
                 clientIdBuilder = new StringBuilder(super.getClientId(context));
                 baseClientId = clientIdBuilder.toString();
                 baseClientIdLength = (baseClientId.length() + 1);
                 clientIdBuilder.append(UINamingContainer.getSeparatorChar(context));
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 clientIdBuilder = new StringBuilder();
             }
         }
         return baseClientId;
     }
 
     @Override
     public String getContainerClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         return this.getClientId(context);
     }
 
     @Override
     public String getClientId(FacesContext context) {
 
         if (context == null) {
             throw new NullPointerException();
         }
 
         // If baseClientId and clientIdBuilder are both null, this is the
         // first time that getClientId() has been called.
         // If we're not nested within another UIData, then:
         //   - create a new StringBuilder assigned to clientIdBuilder containing
         //   our client ID.
         //   - toString() the builder - this result will be our baseClientId
         //     for the duration of the component
         //   - append UINamingContainer.getSeparatorChar() to the builder
         //  If we are nested within another UIData, then:
         //   - create an empty StringBuilder that will be used to build
         //     this instance's ID
         if (baseClientId == null && clientIdBuilder == null) {
             if (!isNestedWithinUIData()) {
                 clientIdBuilder = new StringBuilder(super.getClientId(context));
                 baseClientId = clientIdBuilder.toString();
                 baseClientIdLength = (baseClientId.length() + 1);
                 clientIdBuilder.append(UINamingContainer.getSeparatorChar(context));
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 clientIdBuilder = new StringBuilder();
             }
         }
         int rowIndex = getRowIndex();
         if (rowIndex >= 0) {
             String cid;
             if (!isNestedWithinUIData()) {
                 // we're not nested, so the clientIdBuilder is already
                 // primed with clientID +
                 // UINamingContainer.getSeparatorChar().  Append the
                 // current rowIndex, and toString() the builder.  reset
                 // the builder to it's primed state.
                 if (hasTreeDataModel()) {
                     String rootIndex = ((TreeDataModel)getDataModel()).getRootIndex();
                     if (rootIndex != null && !rootIndex.equals(""))
                         rootIndex += "."+rowIndex;
                     else rootIndex = ""+rowIndex;
                     cid = clientIdBuilder.append(rootIndex).toString();
                 } else
                     cid = clientIdBuilder.append(rowIndex).toString();
 
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 // we're nested, so we have to build the ID from scratch
                 // each time.  Reuse the same clientIdBuilder instance
                 // for each call by resetting the length to 0 after
                 // the ID has been computed.
                 if (hasTreeDataModel()) {
                     String rootIndex = ((TreeDataModel)getDataModel()).getRootIndex();
                     if (rootIndex != null && !rootIndex.equals(""))
                         rootIndex += "."+rowIndex;
                     else rootIndex = ""+rowIndex;
                     cid = clientIdBuilder.append(super.getClientId(context))
                             .append(UINamingContainer.getSeparatorChar(context)).append(rootIndex)
                             .toString();
                 } else
                     cid = clientIdBuilder.append(super.getClientId(context)).toString();
 
                 clientIdBuilder.setLength(0);
                 }
             return (cid);
         } else {
             if (!isNestedWithinUIData()) {
                 // Not nested and no row available, so just return our baseClientId
                 return (baseClientId);
             } else {
                 // nested and no row available, return the result of getClientId().
                 // this is necessary as the client ID will reflect the row that
                 // this table represents
                 return super.getClientId(context);
             }
         }
     }
 
     private Boolean isNestedWithinUIData() {
         if (isNested == null) {
             UIComponent parent = this;
             while (null != (parent = parent.getParent())) {
                 if (parent instanceof UIData) {
                     isNested = Boolean.TRUE;
                     break;
                 }
             }
             if (isNested == null) {
                 isNested = Boolean.FALSE;
             }
             return isNested;
         } else return isNested;
     }
 
 
 
     /*#######################################################################*/
     /*###################### Private Classes ################################*/
     /*#######################################################################*/
     private class EntryKeyComparatorWrapper<T> implements Comparator {
         Comparator<T> comparator;
 
         public EntryKeyComparatorWrapper(Comparator<T> comparator) {
             this.comparator = comparator;
         }
 
         public int compare(Object o1, Object o2) {
             return comparator.compare(((Map.Entry<T, Object>) o1).getKey(), ((Map.Entry<T, Object>) o2).getKey());
         }
     }
 
     private class PriorityComparator implements Comparator<Column> {
         public int compare(Column object, Column object1) {
             return object.getSortPriority().compareTo(object1.getSortPriority());
         }
     }
 
 
 
 
 
 
     /*#######################################################################*/
     /*#################### UIData iterate() impl. ###########################*/
     /*#######################################################################*/
     private void iterate(FacesContext context, PhaseId phaseId) {
         // Process each facet of this component exactly once
         setRowIndex(-1);
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values()) {
                 if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                     facet.processDecodes(context);
                 } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                     facet.processValidators(context);
                 } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                     facet.processUpdates(context);
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         // Process each facet of our child UIColumn components exactly once
         setRowIndex(-1);
         if (getChildCount() > 0) {
             for (UIComponent column : getChildren()) {
                 if (!(column instanceof UIColumn) || !column.isRendered()) {
                     continue;
                 }
                 if (column.getFacetCount() > 0) {
                     for (UIComponent columnFacet : column.getFacets().values()) {
                         if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                             columnFacet.processDecodes(context);
                         } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                             columnFacet.processValidators(context);
                         } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                             columnFacet.processUpdates(context);
                         } else {
                             throw new IllegalArgumentException();
                         }
                     }
                 }
             }
         }
 
         // Visit tableConfigPanel if one is our child
         setRowIndex(-1);
         for (UIComponent kid : getChildren()) {
             if (!(kid instanceof TableConfigPanel) || !kid.isRendered()) {
                 continue;
             }
 
             if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                 kid.processDecodes(context);
             } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                 kid.processValidators(context);
             } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                 kid.processUpdates(context);
             } else {
                 throw new IllegalArgumentException();
             }
         }
 
         // Iterate over our UIColumn & PanelExpansion children, once per row
         int processed = 0;
         int rowIndex = getFirst() - 1;
         int rows = getRows();
         boolean inSubrows = false;
         RowPanelExpander panelExpander = getPanelExpansion();
         RowStateMap map = getStateMap();
         RowState rowState;
         Boolean expanded;
         
         while (true) {
             // Have we processed the requested number of rows?
             if (!inSubrows) processed = processed + 1;
             if ((rows > 0) && (processed > rows)) {
                 break;
             }
 
             // Expose the current row in the specified request attribute
             setRowIndex(++rowIndex);
             if (!isRowAvailable()) {
                 if (treeModel != null && treeModel.isRootIndexSet()) {
                     rowIndex = treeModel.pop()+1;
                     setRowIndex(rowIndex);
                     if (!treeModel.isRootIndexSet()) inSubrows = false;
                 }
                 else break; // Scrolled past the last row
             }
 
             rowState = map.get(getRowData());
             expanded = rowState.isExpanded();
 
             // Perform phase-specific processing as required
             // on the *children* of the UIColumn (facets have
             // been done a single time with rowIndex=-1 already)
             if (getChildCount() > 0) {
                 for (UIComponent kid : getChildren()) {
                     if ((!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander))
                             || !kid.isRendered()) {
                         continue;
                     }
                     if ((kid instanceof RowPanelExpander) && !expanded) {
                         continue;
                     }
                     if (kid.getChildCount() > 0) {
                         for (UIComponent grandkid : kid.getChildren()) {
                             if (!grandkid.isRendered()) {
                                 continue;
                             }
                             if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                                 grandkid.processDecodes(context);
                             } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                                 grandkid.processValidators(context);
                             } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                                 grandkid.processUpdates(context);
                             } else {
                                 throw new IllegalArgumentException();
                             }
                         }
                     }
                 }
             }
 
             if (expanded && hasTreeDataModel() && (panelExpander == null || rowState.getExpansionType() == RowState.ExpansionType.ROW)) {
                 if (treeModel.getCurrentRowChildCount() > 0) {
                     inSubrows = true;
                     treeModel.setRootIndex(
                             treeModel.getRootIndex().equals("") ?
                                     ""+getRowIndex() :
                                     treeModel.getRootIndex() + "." + getRowIndex());
                     rowIndex = -1;
                 }
             }
         }
 
         // Clean up after ourselves
         setRowIndex(-1);
     }
 }
