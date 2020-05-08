 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package javax.faces.component;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.FacesListener;
 import javax.faces.event.PhaseId;
 import javax.faces.internal.AssertionUtil;
 import javax.faces.internal.NamingContainerUtil;
 import javax.faces.internal.SavedState;
 import javax.faces.internal.UIDataUtil;
 import javax.faces.model.DataModel;
 
 /**
  * @author shot
  * @author manhole
  * 
  * TODO TEST
  */
 public class UIData extends UIComponentBase implements NamingContainer {
 
     public static final String COMPONENT_FAMILY = "javax.faces.Data";
 
     public static final String COMPONENT_TYPE = "javax.faces.Data";
 
     private static final String FOOTER_FACET_NAME = "footer";
 
     private static final String HEADER_FACET_NAME = "header";
 
     private static final String FIRST_BINDING_NAME = "first";
 
     private static final String VALUE_BINDING_NAME = "value";
 
     private static final String ROWS_BINDING_NAME = "rows";
 
     private int first = 0;
 
     private int rowIndex = 0;
 
     private int rows = 0;
 
     private boolean firstSet = false;
 
     private boolean rowsSet = false;
 
     private Map savedMap = new HashMap();
 
     private transient DataModel model = null;
 
     private Object value = null;
 
     private String var = null;
 
     private static final String DEFAULT_RENDER_TYPE = "javax.faces.Table";
 
     public UIData() {
         super();
         setRendererType(DEFAULT_RENDER_TYPE);
     }
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public void setId(String id) {
         super.setId(id);
         NamingContainerUtil.refreshDescendantComponentClientId(this);
     }
 
     public int getFirst() {
         if (firstSet) {
             return first;
         }
         Integer firstValue = (Integer) ComponentUtils_.getValueBindingValue(this,
                 FIRST_BINDING_NAME);
         return (firstValue != null) ? firstValue.intValue() : first;
     }
 
     public void setFirst(int first) {
         AssertionUtil.assertIntegerNotNegative(first, "first");
         this.first = first;
         firstSet = true;
     }
 
     public UIComponent getFooter() {
         return getFacet(FOOTER_FACET_NAME);
     }
 
     public void setFooter(UIComponent footer) {
         AssertionUtil.assertNotNull("footer", footer);
         getFacets().put(FOOTER_FACET_NAME, footer);
     }
 
     public UIComponent getHeader() {
         return getFacet(HEADER_FACET_NAME);
     }
 
     public void setHeader(UIComponent header) {
         AssertionUtil.assertNotNull("header", header);
         getFacets().put(HEADER_FACET_NAME, header);
     }
 
     public boolean isRowAvailable() {
         return getDataModel().isRowAvailable();
     }
 
     public int getRowCount() {
         return getDataModel().getRowCount();
     }
 
     public Object getRowData() {
         return getDataModel().getRowData();
     }
 
     public int getRowIndex() {
         return rowIndex;
     }
 
     public void setRowIndex(int rowIndex) {
         saveDescendantState();
         // int previous = rowIndex_;
         this.rowIndex = rowIndex;
         DataModel model = getDataModel();
         model.setRowIndex(rowIndex);
 
         String var = getVar();
         if (var != null) {
             Map requestMap = getFacesContext().getExternalContext()
                     .getRequestMap();
             if (rowIndex == -1) {
                 requestMap.remove(var);
             } else if (isRowAvailable()) {
                 requestMap.put(var, getRowData());
             } else {
                 requestMap.remove(var);
             }
         }
         restoreDescendantState();
     }
 
     public int getRows() {
         if (rowsSet) {
             return rows;
         }
         Integer value = (Integer) ComponentUtils_.getValueBindingValue(this,
                 ROWS_BINDING_NAME);
         return (value != null) ? value.intValue() : rows;
     }
 
     public void setRows(int rows) {
         AssertionUtil.assertIntegerNotNegative(rows, "rows");
         this.rows = rows;
         rowsSet = true;
     }
 
     public String getVar() {
         return var;
     }
 
     public void setVar(String var) {
         this.var = var;
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object[] values = (Object[]) state;
         super.restoreState(context, values[0]);
         first = ((Integer) values[1]).intValue();
         firstSet = ((Boolean) values[2]).booleanValue();
         rowIndex = ((Integer) values[3]).intValue();
         rows = ((Integer) values[4]).intValue();
         rowsSet = ((Boolean) values[5]).booleanValue();
         savedMap = (Map) values[6];
         value = (Object) values[7];
         var = (String) values[8];
     }
 
     public Object saveState(FacesContext context) {
         Object[] values = new Object[9];
         values[0] = super.saveState(context);
         values[1] = new Integer(first);
         values[2] = firstSet ? Boolean.TRUE : Boolean.FALSE;
         values[3] = new Integer(rowIndex);
         values[4] = new Integer(rows);
         values[5] = rowsSet ? Boolean.TRUE : Boolean.FALSE;
         values[6] = savedMap;
         values[7] = value;
         values[8] = var;
         return values;
     }
 
     public Object getValue() {
         if (value != null) {
             return value;
         }
         return ComponentUtils_.getValueBindingValue(this, VALUE_BINDING_NAME);
     }
 
     public void setValue(Object value) {
         this.value = value;
         model = null;
     }
 
     public void setValueBinding(String name, ValueBinding vb) {
         AssertionUtil.assertNotNull("name", name);
         if (name.equals("var") || name.equals("rowIndex")) {
             throw new IllegalArgumentException("setValueBinding");
         } else if (name.equals(VALUE_BINDING_NAME)) {
             model = null;
         }
         super.setValueBinding(name, vb);
     }
 
     public String getClientId(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         String clientId = super.getClientId(context);
         if (rowIndex >= 0) {
             clientId = clientId + NamingContainer.SEPARATOR_CHAR + rowIndex;
         }
         return clientId;
     }
 
     public void queueEvent(FacesEvent event) {
         AssertionUtil.assertNotNull("event", event);
         super.queueEvent(new FacesEventWrapper(event, getRowIndex(), this));
     }
 
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         AssertionUtil.assertNotNull("event", event);
         if (!(event instanceof FacesEventWrapper)) {
             super.broadcast(event);
             return;
         }
 
         FacesEventWrapper e = (FacesEventWrapper) event;
         int rowIndex = getRowIndex();
         setRowIndex(e.getRowIndex());
         FacesEvent orgEvent = e.getFacesEvent();
         orgEvent.getComponent().broadcast(orgEvent);
         setRowIndex(rowIndex);
     }
 
     public void encodeBegin(FacesContext context) throws IOException {
         AssertionUtil.assertNotNull("context", context);
         resetModelAndSavedState();
         super.encodeBegin(context);
     }
 
     public void processDecodes(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
        resetModelAndSavedState();
         processAppropriateAction(context, PhaseId.APPLY_REQUEST_VALUES);
         try {
             decode(context);
         } catch (RuntimeException e) {
             context.renderResponse();
             throw e;
         }
     }
 
     public void processUpdates(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
        resetModelAndSavedState();
         processAppropriateAction(context, PhaseId.UPDATE_MODEL_VALUES);
     }
 
     public void processValidators(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
        resetModelAndSavedState();
         processAppropriateAction(context, PhaseId.PROCESS_VALIDATIONS);
     }
 
     protected void processAppropriateAction(FacesContext context,
             PhaseId phaseId) {
         processFacets(context, phaseId);
         processColumns(context, phaseId);
         processColumnsChildren(context, phaseId);
     }
 
     protected void processFacets(FacesContext context, PhaseId phaseId) {
         setRowIndex(-1);
         for (Iterator facets = getFacets().keySet().iterator(); facets
                 .hasNext();) {
             UIComponent facet = (UIComponent) getFacets().get(facets.next());
             ComponentUtils_.processAppropriatePhaseAction(context, facet,
                     phaseId);
         }
     }
 
     protected void processColumns(FacesContext context, PhaseId phaseId) {
         setRowIndex(-1);
         for (Iterator columns = getChildren().iterator(); columns.hasNext();) {
             UIComponent column = (UIComponent) columns.next();
             if (!(column instanceof UIColumn)) {
                 continue;
             }
             if (!column.isRendered()) {
                 continue;
             }
             for (Iterator columnFacets = column.getFacets().keySet().iterator(); columnFacets
                     .hasNext();) {
                 UIComponent columnFacet = (UIComponent) column.getFacets().get(
                         columnFacets.next());
                 ComponentUtils_.processAppropriatePhaseAction(context,
                         columnFacet, phaseId);
             }
         }
     }
 
     protected void processColumnsChildren(FacesContext context, PhaseId phaseId) {
         int first = getFirst();
         int rows = getRows();
         int last = (rows != 0) ? (first + rows) : getRowCount();
         for (int rowIndex = first; rowIndex < last; rowIndex++) {
             setRowIndex(rowIndex);
             if (!isRowAvailable()) {
                 continue;
             }
             for (Iterator children = getChildren().iterator(); children
                     .hasNext();) {
                 UIComponent child = (UIComponent) children.next();
                 if (!(child instanceof UIColumn)) {
                     continue;
                 }
                 for (Iterator grandChildren = child.getChildren().iterator(); grandChildren
                         .hasNext();) {
                     UIComponent grandChild = (UIComponent) grandChildren.next();
                     if (!grandChild.isRendered()) {
                         continue;
                     }
                     ComponentUtils_.processAppropriatePhaseAction(context,
                             grandChild, phaseId);
                 }
             }
         }
         setRowIndex(-1);
     }
 
     protected boolean keepSavedState(FacesContext context) {
         String clientId = null;
         for (Iterator children = savedMap.keySet().iterator(); children
                 .hasNext();) {
             clientId = (String) children.next();
             if (keepSavedState0(context, clientId)) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean keepSavedState0(FacesContext context, String clientId) {
         FacesMessage message = null;
         for (Iterator messages = context.getMessages(clientId); messages
                 .hasNext();) {
             message = (FacesMessage) messages.next();
             if (message.getSeverity().compareTo(FacesMessage.SEVERITY_ERROR) >= 0) {
                 return true;
             }
         }
         return false;
     }
 
     protected void saveDescendantState() {
         FacesContext context = getFacesContext();
         for (Iterator itr = getChildren().iterator(); itr.hasNext();) {
             UIComponent child = (UIComponent) itr.next();
             if (child instanceof UIColumn) {
                 saveDescendantState(child, context);
             }
         }
     }
 
     protected void saveDescendantState(UIComponent component,
             FacesContext context) {
         if (component instanceof EditableValueHolder) {
             String clientId = component.getClientId(context);
             EditableValueHolder holder = (EditableValueHolder) component;
             SavedState state = (SavedState) savedMap.get(clientId);
             if (state == null) {
                 state = new SavedState();
                 savedMap.put(clientId, state);
             }
             state.setLocalValue(holder.getLocalValue());
             state.setValid(holder.isValid());
             state.setSubmittedValue(holder.getSubmittedValue());
             state.setLocalValueSet(holder.isLocalValueSet());
         }
         for (Iterator children = component.getChildren().iterator(); children
                 .hasNext();) {
             saveDescendantState((UIComponent) children.next(), context);
         }
     }
 
     protected void restoreDescendantState() {
         FacesContext context = getFacesContext();
         for (Iterator itr = getChildren().iterator(); itr.hasNext();) {
             UIComponent child = (UIComponent) itr.next();
             if (child instanceof UIColumn) {
                 restoreDescendantState(child, context);
             }
         }
     }
 
     protected void restoreDescendantState(UIComponent component,
             FacesContext context) {
 
         forceComponentIdClear(component);
 
         if (component instanceof EditableValueHolder) {
             EditableValueHolder holder = (EditableValueHolder) component;
             String clientId = component.getClientId(context);
             SavedState state = (SavedState) savedMap.get(clientId);
             if (state == null) {
                 state = new SavedState();
             }
             holder.setValue(state.getLocalValue());
             holder.setValid(state.isValid());
             holder.setSubmittedValue(state.getSubmittedValue());
             holder.setLocalValueSet(state.isLocalValueSet());
         }
         for (Iterator children = component.getChildren().iterator(); children
                 .hasNext();) {
             restoreDescendantState((UIComponent) children.next(), context);
         }
     }
 
     protected DataModel getDataModel() {
         if (model != null) {
             return model;
         }
         model = UIDataUtil.getSuitableDataModel(getValue());
         return model;
     }
 
     private void resetModelAndSavedState() {
         model = null;
         if (savedMap != null) {
             savedMap.clear();
         }
     }
 
     private void forceComponentIdClear(UIComponent component) {
         NamingContainerUtil.refreshClientId(component);
     }
 
     static class FacesEventWrapper extends FacesEvent {
 
         private static final long serialVersionUID = 1L;
 
         private FacesEvent event_ = null;
 
         private int index_ = -1;
 
         public FacesEventWrapper(FacesEvent event, int index,
                 UIComponent component) {
             super(component);
             event_ = event;
             index_ = index;
         }
 
         public FacesEvent getFacesEvent() {
             return event_;
         }
 
         public int getRowIndex() {
             return index_;
         }
 
         public PhaseId getPhaseId() {
             return event_.getPhaseId();
         }
 
         public void setPhaseId(PhaseId phaseId) {
             event_.setPhaseId(phaseId);
         }
 
         public boolean isAppropriateListener(FacesListener listener) {
             return event_.isAppropriateListener(listener);
         }
 
         public void processListener(FacesListener listener) {
             event_.processListener(listener);
         }
 
         public UIComponent getComponent() {
             return event_.getComponent();
         }
 
         public void queue() {
             event_.queue();
         }
     }
 }
