 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
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
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 
 package org.icefaces.ace.component.celleditor;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.FacesContext;
 import javax.el.ValueExpression;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.datatable.DataTableConstants;
 import org.icefaces.ace.component.roweditor.RowEditor;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.resources.ICEResourceDependencies;
 
 import java.util.*;
 
 @ICEResourceDependencies({
 
 })
 public class CellEditor extends CellEditorBase {
     Map<String, Object> requestMap;
     String rowStateVar;
 
     @Override
     public Iterator<UIComponent> getFacetsAndChildren() {
         Iterator<UIComponent> result;
         int childCount = this.getChildCount(),
             facetCount = this.getFacetCount();
 
         if (requestMap == null)
             requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
 
         if (rowStateVar == null)
             rowStateVar = getRowStateVar();
 
        // not in an iterative table visit, return default impl
        if (rowStateVar == null) return super.getFacetsAndChildren();

         RowState rowState = (RowState) requestMap.get(rowStateVar);
 
         // not in an iterative table visit, return default impl
         if (rowState == null) return super.getFacetsAndChildren();
 
         List<String> selectedEditorIds = rowState.getActiveCellEditorIds();
 
         // If there are neither facets nor children
         if (0 == childCount && 0 == facetCount) {
             result = new ArrayList<UIComponent>().iterator();
         }
         // If there are only facets and no children
         else if (0 == childCount) {
             List<UIComponent> facets = new ArrayList<UIComponent>();
 
             if (selectedEditorIds.contains(getId())) facets.add(getFacet("input"));
             else facets.add(getFacet("output"));
 
             List<UIComponent> unmodifiable = Collections.unmodifiableList(facets);
             result = unmodifiable.iterator();
         }
         // If there are only children and no facets
         else if (0 == facetCount) {
             List<UIComponent> unmodifiable =
                     Collections.unmodifiableList(getChildren());
             result = unmodifiable.iterator();
         }
         // If there are both children and facets
         else {
             List<UIComponent> children = getChildren();
 
             if (selectedEditorIds.contains(getId())) children.add(getFacet("input"));
             else children.add(getFacet("output"));
 
             List<UIComponent> unmodifiable = Collections.unmodifiableList(children);
             result = unmodifiable.iterator();
         }
 
         return result;
     }
 
     private DataTable findParentTable() {
         UIComponent parent = getParent();
 
         while (parent != null)
             if (parent instanceof DataTable) return (DataTable) parent;
             else parent = parent.getParent();
 
         return null;
     }
 
     private String getRowStateVar() {
         DataTable table = findParentTable();
 
        return table != null ? table.getRowStateVar() : null;
     }
 }
