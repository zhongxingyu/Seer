 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 /*
  * Generated, Do Not Modify
  */
 
 package org.icefaces.ace.component.column;
 
 import javax.faces.component.UIColumn;
 import javax.faces.context.FacesContext;
 import javax.el.ValueExpression;
 import javax.el.MethodExpression;
 import javax.faces.component.UIComponent;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.model.DataModel;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.model.filter.*;
 import org.icefaces.ace.component.celleditor.CellEditor;
 
 public class Column extends ColumnBase {
 	private static final String OPTIMIZED_PACKAGE = "org.icefaces.ace.component.";
     private int currGroupLength;
     // Toggled to true appropriately before first group rendering
     private boolean oddGroup = false;
     
     public Column() {
 		setRendererType(null);
 	}
 
     private CellEditor cellEditor = null;
     private FilterConstraint filterConstraint = null;
     
     private final static String STARTS_WITH_MATCH_MODE = "startsWith";
     private final static String ENDS_WITH_MATCH_MODE = "endsWith";
     private final static String CONTAINS_MATCH_MODE = "contains";
     private final static String EXACT_MATCH_MODE = "exact";
 
     public FilterConstraint getFilterConstraint() {
         String filterMatchMode = getFilterMatchMode();
 
         if(filterConstraint == null) {
             if(filterMatchMode.equals(STARTS_WITH_MATCH_MODE)) {
                 filterConstraint = new StartsWithFilterConstraint();
             } else if(filterMatchMode.equals(ENDS_WITH_MATCH_MODE)) {
                 filterConstraint = new EndsWithFilterConstraint();
             } else if(filterMatchMode.equals(CONTAINS_MATCH_MODE)) {
                 filterConstraint = new ContainsFilterConstraint();
             } else if(filterMatchMode.equals(EXACT_MATCH_MODE)) {
                 filterConstraint = new ExactFilterConstraint();
             }
         }
 
         return filterConstraint;
     }
 
 	protected FacesContext getFacesContext() {
 		return FacesContext.getCurrentInstance();
 	}
 
     public CellEditor getCellEditor() {
         if (cellEditor != null) return cellEditor;
 
         for (UIComponent child : getChildren()) {
             if (child instanceof CellEditor) {
                 cellEditor = (CellEditor)child;
                 return cellEditor;
             }
         }
 
         return null;
     }
 
     @Override
     public void setSortPriority(Integer i) {
         DataTable table = findParentTable(getFacesContext(), this);
         if (table != null) table.applySorting();
         super.setSortPriority(i);
     }
 
     protected DataTable findParentTable(FacesContext context, Column editor) {
         UIComponent parent = editor.getParent();
 
         while(parent != null)
             if (parent instanceof DataTable) return (DataTable) parent;
             else parent = parent.getParent();
 
         return null;
     }
 
     public int getCurrGroupLength() {
         return currGroupLength;
     }
 
     public void setCurrGroupLength(int currGroupLength) {
         this.currGroupLength = currGroupLength;
     }
 
     public boolean isOddGroup() {
         return oddGroup;
     }
 
     public void setOddGroup(boolean oddGroup) {
         this.oddGroup = oddGroup;
     }
 
     public boolean isNextColumnGrouped() {
         DataTable dataTable = findParentTable(getFacesContext(), this);
         int currentRow = dataTable.getRowIndex();
         Object currentValue = getGroupBy();
 
         if (currentValue != null) {
             dataTable.setRowIndex(currentRow + 1);
             Object nextValue = getGroupBy();
 
             dataTable.setRowIndex(currentRow);
 
             return currentValue.equals(nextValue);
         }
 
         return false;
     }
 
     public int findCurrGroupLength() {
         DataTable dataTable = findParentTable(getFacesContext(), this);
 
         int result = 0; // isNextColumnGrouped == true is known
         int currentRow = dataTable.getRowIndex();
         boolean keepCounting = true;
         Object currentValue = getGroupBy();
 
         // If this row doesn't break its group by rendering a conditional row after itself or
         // by being expanded, span more than this row
         boolean notExpanded  = !dataTable.getStateMap().get(dataTable.getRowData()).isExpanded();
         boolean noTailingRows = dataTable.getConditionalRows(currentRow, false).size() == 0;
         boolean lastExpanded = false;
         if (notExpanded && noTailingRows)
             while (keepCounting) {
                 dataTable.setRowIndex(currentRow + result + 1);
 
                 if (!dataTable.isRowAvailable()) break;
 
                 boolean expanded = dataTable.getStateMap().get(dataTable.getRowData()).isExpanded();
                 boolean hasConditionalRows = dataTable.getConditionalRows(currentRow + result + 1, true).size() > 0 || dataTable.getConditionalRows(currentRow + result, false).size() > 0;;
 
                 if (currentValue.equals(getGroupBy()) && !lastExpanded && !hasConditionalRows) {
                     lastExpanded = expanded;
                     result++;
                 }
                 else keepCounting = false;
             }
 
         dataTable.setRowIndex(currentRow);
         setCurrGroupLength(result);
         return result;  //To change body of created methods use File | Settings | File Templates.
     }
 
     public boolean isLastGroupDifferent() {
         DataTable dataTable = findParentTable(getFacesContext(), this);
         int currentRow = dataTable.getRowIndex();
         Object currentValue = getGroupBy();
 
         if (currentValue != null) {
             dataTable.setRowIndex(currentRow - 1);
             Object lastValue = getGroupBy();
 
             dataTable.setRowIndex(currentRow);
 
             return !currentValue.equals(lastValue);
         }
 
         return true;
     }
 
     @Override
     public java.lang.Object getSortBy() {
         Object retVal = super.getSortBy();
         if (retVal == null) return super.getGroupBy();
         else return retVal;
     }
 
     @Override
     public ValueExpression getValueExpression(String name) {
         ValueExpression retVal = super.getValueExpression(name);
         if (retVal == null && name.equals("sortBy"))
             return super.getValueExpression("groupBy");
         else
             return retVal;
     }
 }
