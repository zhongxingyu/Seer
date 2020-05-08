 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.impl.component;
 
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.*;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.component.html.HtmlForm;
 import javax.faces.component.visit.VisitHint;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.*;
 import javax.faces.model.ArrayDataModel;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.ResultDataModel;
 import javax.faces.model.ResultSetDataModel;
 import javax.faces.model.ScalarDataModel;
 import java.io.IOException;
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.render.Renderer;
 import javax.faces.view.Location;
 import java.util.Collection;
 
 
 /**
  * This is an extended version of UIData, which allows any UISeries type of
  * component to have any type of children, it is not restricted to use the
  * column component as a first child.
  */
 public class UISeriesBase extends HtmlDataTable implements SeriesStateHolder {
     private static final String SHARED_STRING_BUILDER_KEY
             = "javax.faces.component.UIComponentBase.SHARED_STRING_BUILDER";
     private static Class javax_servlet_jsp_jstl_sql_Result_class = null;
     static {
         try {
             javax_servlet_jsp_jstl_sql_Result_class = Class.forName(
                 "javax.servlet.jsp.jstl.sql.Result");
         }
         catch(Exception e) {}
     }
 
     private String clientId = null;
     
     protected transient DataModel dataModel = null;
     private int rowIndex = -1;
     protected Map savedChildren = new HashMap();
     protected Map savedSeriesState = new HashMap();
     private String var = null;
     private String varStatus;
 
 
     public UISeriesBase() {
         super();
     }
 
     /**
      * @see javax.faces.component.UIData#isRowAvailable()
      */
     public boolean isRowAvailable() {
         return (getDataModel().isRowAvailable());
     }
 
     public Map getSavedChildren(){
     	return savedChildren;
     }
 
     public Map getSavedSeriesState() {
         return savedSeriesState;
     }
 
     /**
      * @see javax.faces.component.UIData#getRowCount()
      */
     public int getRowCount() {
         return (getDataModel().getRowCount());
     }
 
 
     /**
      * @see javax.faces.component.UIData#getRowData()
      */
     public Object getRowData() {
         return (getDataModel().getRowData());
     }
 
 
     /**
      * @see javax.faces.component.UIData#getRowIndex()
      */
     public int getRowIndex() {
         return (this.rowIndex);
     }
 
     public void setRowIndex(int rowIndex) {
         FacesContext facesContext = getFacesContext();
         // Save current state for the previous row index
         saveChildrenState(facesContext);
         // remove or load the current row data as a request scope attribute
         processCurrentRowData(facesContext, rowIndex);
         // Reset current state information for the new row index
         restoreChildrenState(facesContext);
     }
 
     private void processCurrentRowData(FacesContext facesContext,
                                        int rowIndex) {
         this.rowIndex = rowIndex;
         DataModel model = getDataModel();
         model.setRowIndex(rowIndex);
 
         if (var != null || varStatus != null) {
             Map requestMap = facesContext.getExternalContext().getRequestMap();
             if (rowIndex == -1) {
                 removeRowFromRequestMap(requestMap);
             } else if (isRowAvailable()) {
                 // Indexes are inclusive
                 int firstIndex = getFirst();
                 int lastIndex;
                 int rows = getRows();
                 if (rows == 0) {
                     lastIndex = model.getRowCount() - 1;
                 }
                 else {
                     lastIndex = firstIndex + rows - 1;
                 }
                 loadRowToRequestMap(requestMap, firstIndex, lastIndex, rowIndex);
             } else {
                 removeRowFromRequestMap(requestMap);
             }
         }
     }
 
     private void loadRowToRequestMap(Map requestMap, int begin, int end, int index) {
         if (var != null) {
             requestMap.put(var, getRowData());
         }
         if (varStatus != null) {
             requestMap.put(varStatus, new VarStatus(begin, end, index));
         }
     }
 
     private void removeRowFromRequestMap(Map requestMap) {
         if (var != null) {
             requestMap.remove(var);
         }
         if (varStatus != null) {
             requestMap.remove(varStatus);
         }
     }
 
     /**
      * @see javax.faces.component.UIData#getVar()
      */
     public String getVar() {
         return (this.var);
     }
 
 
     /**
      * @see javax.faces.component.UIData#setVar(String)
      */
     public void setVar(String var) {
         this.var = var;
     }
 
     /**
      * @return The name of the entry is the request map,
      * where the VarStatus object will be put.
      */
     public String getVarStatus() {
         return this.varStatus;
     }
 
     public void setVarStatus(String varStatus) {
         this.varStatus = varStatus;
     }
 
     /**
      * @see javax.faces.component.UIData#setValue(Object)
      */
     public void setValue(Object value) {
         this.dataModel = null;
         super.setValue(value);
     }
 
 
     public void setValueBinding(String name, ValueBinding binding) {
         if ("value".equals(name)) {
             this.dataModel = null;
         } else if ("var".equals(name) || "rowIndex".equals(name)) {
             throw new IllegalArgumentException();
         }
         super.setValueBinding(name, binding);
     }
 
     public void setId(String id) {
         super.setId(id);
         this.clientId = null;
         //System.out.println("UISeriesBase.setId()  " + id);
     }
 
     /**
      * @see javax.faces.component.UIData#getClientId(FacesContext)
      */
     public String getClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
 
         if (clientId != null)
             return clientId;
 
         //boolean idWasNull = false;
         String id = getId();
         if (id == null) {
             // Although this is an error prone side effect, we automatically create a new id
             // just to be compatible to the RI
 
             // The documentation of UniqueIdVendor says that this interface should be implemented by
             // components that also implements NamingContainer. The only component that does not implement
             // NamingContainer but UniqueIdVendor is UIViewRoot. Anyway we just can't be 100% sure about this
             // fact, so it is better to scan for the closest UniqueIdVendor. If it is not found use
             // viewRoot.createUniqueId, otherwise use UniqueIdVendor.createUniqueId(context,seed).
             UniqueIdVendor parentUniqueIdVendor = findParentUniqueIdVendor(this);
             if (parentUniqueIdVendor == null) {
                 UIViewRoot viewRoot = context.getViewRoot();
                 if (viewRoot != null) {
                     id = viewRoot.createUniqueId();
                 }
                 else {
                     // The RI throws a NPE
                     String location = getComponentLocation(this);
                     throw new FacesException("Cannot create clientId. No id is assigned for component"
                             + " to create an id and UIViewRoot is not defined: "
                             + getPathToComponent(this)
                             + (location != null ? " created from: " + location : ""));
                 }
             }
             else {
                 id = parentUniqueIdVendor.createUniqueId(context, null);
             }
             setId(id);
             // We remember that the id was null and log a warning down below
             // idWasNull = true;
         }
         UIComponent namingContainer = findParentNamingContainer(this, false);
         if (namingContainer != null) {
             String containerClientId = namingContainer.getContainerClientId(context);
             if (containerClientId != null) {
                 StringBuilder bld = getSharedStringBuilder(context);
                 clientId = bld.append(containerClientId).append(UINamingContainer.getSeparatorChar(context)).append(id).toString();
             }
             else {
                 clientId = id;
             }
         }
         else {
             clientId = id;
         }
         Renderer renderer = getRenderer(context);
         if (renderer != null) {
             clientId = renderer.convertClientId(context, clientId);
         }
 
         return clientId;
     }
 
     /**
      * Logic for this method is borrowed from MyFaces
      *
      * @param facesContext
      * @return
      */
     @Override
     public String getContainerClientId(FacesContext facesContext) {
         String clientId = getClientId(facesContext);
 
         int rowIndex = getRowIndex();
         if (rowIndex == -1) {
             //System.out.println("UISeries.getContainerClientId [NOT ITER] : " + getClass().getSimpleName() + " : " + clientId);
            rowIndex = 0;
         }
 
         StringBuilder bld = getSharedStringBuilder(facesContext);
         String ret = bld.append(clientId).append(UINamingContainer.getSeparatorChar(facesContext)).append(rowIndex).toString();
         //System.out.println("UISeries.getContainerClientId [ITER] : " + getClass().getSimpleName() + " : " + ret);
         return ret;
     }
 
     /**
      * Logic for this method is borrowed from MyFaces
      *
      * @param facesContext
      * @return
      */
     private static StringBuilder getSharedStringBuilder(FacesContext facesContext) {
         Map<Object, Object> attributes = facesContext.getAttributes();
         StringBuilder sb = (StringBuilder) attributes.get(SHARED_STRING_BUILDER_KEY);
         if (sb == null) {
             sb = new StringBuilder();
             attributes.put(SHARED_STRING_BUILDER_KEY, sb);
         }
         sb.setLength(0);
         return sb;
     }
 
     /**
      * Logic for this method is borrowed from MyFaces
      *
      * @param component
      * @return
      */
     private String getComponentLocation(UIComponent component) {
         Location location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
         if (location != null) {
             return location.toString();
         }
         return null;
     }
 
     private String getPathToComponent(UIComponent component) {
         StringBuffer buf = new StringBuffer();
 
         if (component == null) {
             buf.append("{Component-Path : ");
             buf.append("[null]}");
             return buf.toString();
         }
 
         getPathToComponent(component, buf);
 
         buf.insert(0, "{Component-Path : ");
         buf.append("}");
 
         return buf.toString();
     }
 
     private void getPathToComponent(UIComponent component, StringBuffer buf) {
         if (component == null) {
             return;
         }
 
         StringBuffer intBuf = new StringBuffer();
 
         intBuf.append("[Class: ");
         intBuf.append(component.getClass().getName());
         if (component instanceof UIViewRoot) {
             intBuf.append(",ViewId: ");
             intBuf.append(((UIViewRoot) component).getViewId());
         }
         else {
             intBuf.append(",Id: ");
             intBuf.append(component.getId());
         }
         intBuf.append("]");
 
         buf.insert(0, intBuf.toString());
 
         getPathToComponent(component.getParent(), buf);
     }
 
     static UniqueIdVendor findParentUniqueIdVendor(UIComponent component) {
         UIComponent parent = component.getParent();
 
         while (parent != null) {
             if (parent instanceof UniqueIdVendor) {
                 return (UniqueIdVendor) parent;
             }
             parent = parent.getParent();
         }
         return null;
     }
 
     static UIComponent findParentNamingContainer(UIComponent component, boolean returnRootIfNotFound) {
         UIComponent parent = component.getParent();
         if (returnRootIfNotFound && parent == null) {
             return component;
         }
         while (parent != null) {
             if (parent instanceof NamingContainer) {
                 return parent;
             }
             if (returnRootIfNotFound) {
                 UIComponent nextParent = parent.getParent();
                 if (nextParent == null) {
                     return parent; // Root
                 }
                 parent = nextParent;
             }
             else {
                 parent = parent.getParent();
             }
         }
         return null;
     }
 
     /**
      * @see javax.faces.component.UIData#queueEvent(FacesEvent)
      */
     public void queueEvent(FacesEvent event) {
         FacesEvent rowEvent = new RowEvent(this, event, getRowIndex());
         // ICE-4822 : Don't have UISeries let its superclass UIData broadcast
         // events too, since then we have redundant events. So, do behaviour
         // of UIComponentBase.queueEvent(FacesEvent)
         //super.queueEvent(rowEvent);
         UIComponent parent = getParent();
         if (parent == null) {
             throw new IllegalStateException();
         } else {
             parent.queueEvent(rowEvent);
         }
     }
 
 
     /**
      * @see javax.faces.component.UIData#broadcast(FacesEvent)
      */
     /*
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         if (!(event instanceof RowEvent)) {
             super.broadcast(event);
             return;
         }
 
         // fire row specific event
         ((RowEvent) event).broadcast();
         return;
     }
     */
     public void broadcast(FacesEvent event)
           throws AbortProcessingException {
 
         if (!(event instanceof RowEvent)) {
             super.broadcast(event);
             return;
         }
         FacesContext context = FacesContext.getCurrentInstance();
         // Set up the correct context and fire our wrapped event
         RowEvent revent = (RowEvent) event;
         if (isNestedWithinUIData()) {
             dataModel = null;
         }
         int oldRowIndex = getRowIndex();
         setRowIndex(revent.getRowIndex());
         FacesEvent rowEvent = revent.getFacesEvent();
         UIComponent source = rowEvent.getComponent();
         UIComponent compositeParent = null;
         try {
             if (!UIComponent.isCompositeComponent(source)) {
                 compositeParent = UIComponent.getCompositeComponentParent(source);
             }
             if (compositeParent != null) {
                 compositeParent.pushComponentToEL(context, null);
             }
             source.pushComponentToEL(context, null);
             source.broadcast(rowEvent);
         } finally {
             source.popComponentFromEL(context);
             if (compositeParent != null) {
                 compositeParent.popComponentFromEL(context);
             }
         }
         setRowIndex(oldRowIndex);
     }
 
 
 
     /**
      * @see javax.faces.component.UIData#encodeBegin(FacesContext)
      */
     public void encodeBegin(FacesContext context) throws IOException {
         dataModel = null;
         if (!keepSaved(context)) {
             savedChildren = new HashMap();
         }
         synchWithPaginator();
         super.encodeBegin(context);
     }
 
 
 
     public void processDecodes(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
         pushComponentToEL(context, this);
         dataModel = null;
         if (null == savedChildren || !keepSaved(context)) {
             savedChildren = new HashMap();
         }
 
         iterate(context, PhaseId.APPLY_REQUEST_VALUES);
         decode(context);
         popComponentFromEL(context);
     }
 
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
         if (isNestedWithinUIData()) {
             dataModel = null;
         }
         iterate(context, PhaseId.PROCESS_VALIDATIONS);
         app.publishEvent(context, PostValidateEvent.class, this);
         popComponentFromEL(context);
     }
 
 
     public void processUpdates(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
         pushComponentToEL(context, this);
         if (isNestedWithinUIData()) {
             dataModel = null;
         }
         iterate(context, PhaseId.UPDATE_MODEL_VALUES);
         popComponentFromEL(context);
     }
 
     // ICE-7809 MyFaces' default impl of this method throws an UnsupportedOperationException
     protected void setDataModel(DataModel dataModel) {
         this.dataModel = dataModel;
     }
 
     /**
      * <p>Return the DataModel containing the Objects that will be iterated over
      * when this component is rendered.</p>
      *
      * @return
      */
     protected DataModel getDataModel() {
         if (null != this.dataModel) {
             return (dataModel);
         }
 
         Object currentValue = getValue();
 
         if (null == currentValue) {
             this.dataModel = new ListDataModel(Collections.EMPTY_LIST);
         } else if (currentValue instanceof DataModel) {
             this.dataModel = (DataModel) currentValue;
         } else if (currentValue instanceof List) {
             this.dataModel = new ListDataModel((List) currentValue);
         } else if (Object[].class.isAssignableFrom(currentValue.getClass())) {
             this.dataModel = new ArrayDataModel((Object[]) currentValue);
         } else if (currentValue instanceof ResultSet) {
             this.dataModel = new ResultSetDataModel((ResultSet) currentValue);
         } else if (javax_servlet_jsp_jstl_sql_Result_class != null &&
                    javax_servlet_jsp_jstl_sql_Result_class.isInstance(currentValue)) {
             this.dataModel = new ResultDataModel();
             this.dataModel.setWrappedData(currentValue);
         } else {
             this.dataModel = wrapDataModel(currentValue);
         }
         if (null == this.dataModel) {
             this.dataModel = new ScalarDataModel(currentValue);
         }
 
         return (dataModel);
     }
 
     protected DataModel wrapDataModel(Object currentValue) {
         return null;
     }
 
     protected void iterate(FacesContext facesContext, PhaseId phase) {
         // clear rowIndex
         setRowIndex(-1);
 
         int rowsProcessed = 0;
         int currentRowIndex = getFirst() - 1;
         int displayedRows = getRows();
         // loop over dataModel processing each row once
         while (1 == 1) {
             // break if we have processed the number of rows requested
             if ((displayedRows > 0) && (++rowsProcessed > displayedRows)) {
                 break;
             }
             // process the row at currentRowIndex
             setRowIndex(++currentRowIndex);
             // break if we've moved past the last row
             if (!isRowAvailable()) {
                 break;
             }
             // loop over children and facets
             Iterator children = getFacetsAndChildren();
             while (children.hasNext()) {
                 UIComponent child = (UIComponent) children.next();
                 if (phase == PhaseId.APPLY_REQUEST_VALUES) {
                     child.processDecodes(facesContext);
                 } else if (phase == PhaseId.PROCESS_VALIDATIONS) {
                     child.processValidators(facesContext);
                 } else if (phase == PhaseId.UPDATE_MODEL_VALUES) {
                     child.processUpdates(facesContext);
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         // clear rowIndex
         setRowIndex(-1);
     }
 
     /**
      * <p>Return true when we need to keep the child state to display error
      * messages.</p>
      *
      * @param facesContext
      * @return
      */
     private boolean keepSaved(FacesContext facesContext) {
         if (maximumSeverityAtLeastError(facesContext)) {
             return true;
         }
         // return true if this component is nested inside a UIData
         return (isNestedWithinUIData());
     }
 
     private boolean maximumSeverityAtLeastError(FacesContext facesContext) {
         FacesMessage.Severity maximumSeverity = facesContext.getMaximumSeverity();
         return ( (maximumSeverity != null) &&
                  (FacesMessage.SEVERITY_ERROR.compareTo(maximumSeverity) <= 0) );
     }
 
     private boolean isNestedWithinUIData() {
         UIComponent parent = this;
         while (null != (parent = parent.getParent())) {
             if (parent instanceof UIData || "facelets.ui.Repeat".equals(parent.getRendererType())) {
                 return true;
             }
         }
         return (false);
     }
 
 
     protected void restoreChildrenState(FacesContext facesContext) {
         Iterator children = getFacetsAndChildren();
         while (children.hasNext()) {
             UIComponent child = (UIComponent) children.next();
             restoreChildState(facesContext, child);
         }
     }
 
 
     protected void restoreChildState(FacesContext facesContext,
                                      UIComponent component) {
         String id = component.getId();
         //System.out.println("UISeriesBase.restoreChildState()  Doer: " + getClass().getSimpleName() + "  On: " + component.getClass().getSimpleName() + "  id: " + id);
         if (!isValid(id)) {
             return;
         }
         component.setId(id);
         restoreChild(facesContext, component);
         Iterator children = component.getFacetsAndChildren();
         while (children.hasNext()) {
             restoreChildState(facesContext, (UIComponent) children.next());
         }
     }
 
     protected void saveChild(FacesContext facesContext, UIComponent component) {
         if (component instanceof EditableValueHolder) {
             EditableValueHolder input = (EditableValueHolder) component;
             String clientId = component.getClientId(facesContext);
             ChildState state = (ChildState) savedChildren.get(clientId);
             if (state == null) {
                 state = new ChildState();
                 savedChildren.put(clientId, state);
             }
             state.setValue(input.getLocalValue());
             state.setValid(input.isValid());
             state.setSubmittedValue(input.getSubmittedValue());
             state.setLocalValueSet(input.isLocalValueSet());
         } else if (component instanceof HtmlForm) {
             String clientId = component.getClientId(facesContext);
             Boolean isThisFormSubmitted = (Boolean) savedChildren.get(clientId);
             if (isThisFormSubmitted == null) {
                 isThisFormSubmitted =
                         new Boolean(((HtmlForm) component).isSubmitted());
                 savedChildren.put(clientId, isThisFormSubmitted);
             }
         }
         if(component instanceof SeriesStateHolder) {
             SeriesStateHolder ssh = (SeriesStateHolder) component;
             String clientId = component.getClientId(facesContext);
             Object state = ssh.saveSeriesState(facesContext);
             savedSeriesState.put(clientId, state);
         }
     }
 
     protected void restoreChild(FacesContext facesContext,
                                 UIComponent component) {
         if (component instanceof EditableValueHolder) {
             EditableValueHolder input = (EditableValueHolder) component;
             String clientId = component.getClientId(facesContext);
             ChildState state = (ChildState) savedChildren.get(clientId);
             if (state == null) {
                 state = new ChildState();
             }
             input.setValue(state.getValue());
             input.setValid(state.isValid());
             input.setSubmittedValue(state.getSubmittedValue());
             input.setLocalValueSet(state.isLocalValueSet());
         } else if (component instanceof HtmlForm) {
             String clientId = component.getClientId(facesContext);
             Boolean isThisFormSubmitted = (Boolean) savedChildren.get(clientId);
             if (isThisFormSubmitted == null) {
                 isThisFormSubmitted = Boolean.FALSE;
             }
             ((HtmlForm) component)
                     .setSubmitted(isThisFormSubmitted.booleanValue());
         }
         if(component instanceof SeriesStateHolder) {
             SeriesStateHolder ssh = (SeriesStateHolder) component;
             String clientId = component.getClientId(facesContext);
             Object state = savedSeriesState.get(clientId);
             if(state != null) {
                 ssh.restoreSeriesState(facesContext, state);
             }
         }
     }
 
     protected void saveChildrenState(FacesContext facesContext) {
         Iterator children = getFacetsAndChildren();
         while (children.hasNext()) {
             UIComponent child = (UIComponent) children.next();
             saveChildState(facesContext, child);
         }
     }
 
     protected void saveChildState(FacesContext facesContext,
                                   UIComponent component) {
         saveChild(facesContext, component);
         Iterator children = component.getFacetsAndChildren();
         while (children.hasNext()) {
             saveChildState(facesContext, (UIComponent) children.next());
         }
     }
 
     public Object saveSeriesState(FacesContext facesContext) {
         Object[] values = new Object[1];
         values[0] = new Integer(getFirst());
         return values;
     }
 
     public void restoreSeriesState(FacesContext facesContext, Object state) {
         Object[] values = (Object[]) state;
         Integer first = (Integer) values[0];
         if (first != null && first.intValue() != getFirst())
             setFirst(first.intValue());
     }
 
 
     //  Event associated with the specific rowindex
     public class RowEvent extends FacesEvent {
         private FacesEvent event = null;
         private int eventRowIndex = -1;
 
         public RowEvent(UIComponent component, FacesEvent event,
                         int eventRowIndex) {
             super(component);
             this.event = event;
             this.eventRowIndex = eventRowIndex;
         }
 
         public int getRowIndex() {
             return eventRowIndex;
         }
 
         public FacesEvent getFacesEvent() {
             return (this.event);
         }
 
         public void setFacesEvent(FacesEvent event) {
             this.event = event;
         }
         public boolean isAppropriateListener(FacesListener listener) {
             return false;
         }
 
         public void processListener(FacesListener listener) {
             throw new IllegalStateException();
         }
 
         public PhaseId getPhaseId() {
             return (this.event.getPhaseId());
         }
 
         public void setPhaseId(PhaseId phaseId) {
             this.event.setPhaseId(phaseId);
         }
 
         public void broadcast() {
             int oldRowIndex = getRowIndex();
             setRowIndex(eventRowIndex);
             event.getComponent().broadcast(event);
             setRowIndex(oldRowIndex);
         }
     }
 
     public Object saveState(FacesContext context) {
         Object values[] = new Object[6];
         values[0] = super.saveState(context);
         values[1] = new Integer(rowIndex);
         values[2] = savedChildren;
         values[3] = savedSeriesState;
         values[4] = var;
         values[5] = varStatus;
         return (values);
     }
 
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         rowIndex = ((Integer) values[1]).intValue();
         savedChildren = (Map) values[2];
         savedSeriesState = (Map) values[3];
         var = (String) values[4];
         varStatus = (String) values[5];
     }
 
     private boolean isValid(String id) {
         if (id == null) {
             return false;
         }
         if (!Character.isLetter(id.charAt(0)) && (id.charAt(0) != '_')) {
             return false;
         }
         return true;
     }
 
     public void ensureFirstRowInRange() {
         int numRowsTotal = getRowCount(); // could be -1
         int numRowsToShow = getRows();    // always >= 0
         int firstRowIdx = getFirst();     // always >= 0
 
         if (numRowsTotal <= 0) {
             // value of "first" could be from backing bean, therefore don't set indiscriminately
             if (firstRowIdx != 0) {
                 setFirst(0);
             }
         } else if (firstRowIdx >= numRowsTotal) {
             if (numRowsToShow == 0) {
                 setFirst(0); // all rows in one page
             } else { // first row of last page
                 setFirst((numRowsTotal - 1) / numRowsToShow * numRowsToShow);
             }
         }
     }
 
     protected void synchWithPaginator() {
     }
 
     /**
      * <p class="changed_added_2_0"><span
      * class="changed_modified_2_0_rev_a">Override</span> the behavior
      * in {@link UIComponent#visitTree} to handle iteration
      * correctly.</p>
      *
      * <div class="changed_added_2_0">
 
      * <p>If the {@link UIComponent#isVisitable} method of this instance
      * returns <code>false</code>, take no action and return.</p>
 
      * <p>Call {@link UIComponent#pushComponentToEL} and
      * invoke the visit callback on this <code>UIData</code> instance as
      * described in {@link UIComponent#visitTree}.  Let the result of
      * the invoctaion be <em>visitResult</em>.  If <em>visitResult</em>
      * is {@link VisitResult#COMPLETE}, take no further action and
      * return <code>true</code>.  Otherwise, determine if we need to
      * visit our children.  The default implementation calls {@link
      * VisitContext#getSubtreeIdsToVisit} passing <code>this</code> as
      * the argument.  If the result of that call is non-empty, let
      * <em>doVisitChildren</em> be <code>true</code>.  If
      * <em>doVisitChildren</em> is <code>true</code> and
      * <em>visitResult</em> is {@link VisitResult#ACCEPT}, take the
      * following action.<p>
 
      * <ul>
 
      * 	  <li><p>If this component has facets, call {@link
      * 	  UIComponent#getFacets} on this instance and invoke the
      * 	  <code>values()</code> method.  For each
      * 	  <code>UIComponent</code> in the returned <code>Map</code>,
      * 	  call {@link UIComponent#visitTree}.</p></li>
 
      * 	  <li>
 
      * <div class="changed_modified_2_0_rev_a">
 
      *  <p>If this component has children, for each
      * 	  <code>UIColumn</code> child:</p>
      *
      *    <p>Call {@link VisitContext#invokeVisitCallback} on that
           <code>UIColumn</code> instance.
      *    If such a call returns <code>true</code>, terminate visiting and
           return <code>true</code> from this method.</p>
      *
      *    <p>If the child <code>UIColumn</code> has facets, call
      *    {@link UIComponent#visitTree} on each one.</p>
      *
      *    <p>Take no action on non-<code>UIColumn</code> children.</p>
      *
      * </div>
      * </li>
      *
      *    <li>
 
      * <div class="changed_modified_2_0_rev_a">
      *
      * <p>Save aside the result of a call to {@link
      *    #getRowIndex}.</p>
 
      *    <p>For each child component of this <code>UIData</code> that is
      *    also an instance of {@link UIColumn},
      *    </p>
 
      * 	  <p>Iterate over the rows.</p>
 
      * </div>
 
      * <ul>
 
      * 	  <li><p>Let <em>rowsToProcess</em> be the return from {@link
      * 	  #getRows}.  </p></li>
 
      * 	  <li><p>Let <em>rowIndex</em> be the return from {@link
      * 	  #getFirst} - 1.</p></li>
 
      * 	  <li><p>While the number of rows processed is less than
      * 	  <em>rowsToProcess</em>, take the following actions.</p>
 
      * <p>Call {@link #setRowIndex}, passing the current row index.</p>
 
      * <p>If {@link #isRowAvailable} returns <code>false</code>, take no
      * further action and return <code>false</code>.</p>
      *
      * <p class="changed_modified_2_0_rev_a">>Call {@link
      * UIComponent#visitTree} on each of the children of this
      * <code>UIColumn</code> instance.</p>
 
      *     </li>
 
      * </ul>
 
      *    </li>
 
      * </ul>
 
      * <p>Call {@link #popComponentFromEL} and restore the saved row
      * index with a call to {@link #setRowIndex}.</p>
 
      * <p>Return <code>false</code> to allow the visiting to
      * continue.</p>
 
      * </div>
      *
      * @param context the <code>VisitContext</code> that provides
      * context for performing the visit.
      *
      * @param callback the callback to be invoked for each node
      * encountered in the visit.
 
      * @throws NullPointerException if any of the parameters are
      * <code>null</code>.
 
      *
      */
     @Override
     public boolean visitTree(VisitContext context,
                              VisitCallback callback) {
 
         // First check to see whether we are visitable.  If not
         // short-circuit out of this subtree, though allow the
         // visit to proceed through to other subtrees.
         if (!isVisitable(context))
             return false;
 
         FacesContext facesContext = context.getFacesContext();
         // NOTE: that the visitRows local will be obsolete once the
         //       appropriate visit hints have been added to the API
         boolean visitRows = requiresRowIteration(context);;
 
         // Clear out the row index is one is set so that
         // we start from a clean slate.
         int oldRowIndex = -1;
         if (visitRows) {
             oldRowIndex = getRowIndex();
             setRowIndex(-1);
         }
 
         // Push ourselves to EL
         pushComponentToEL(facesContext, null);
 
         try {
 
             // Visit ourselves.  Note that we delegate to the
             // VisitContext to actually perform the visit.
             VisitResult result = context.invokeVisitCallback(this, callback);
 
             // If the visit is complete, short-circuit out and end the visit
             if (result == VisitResult.COMPLETE)
                 return true;
 
             // Visit children, short-circuiting as necessary
             // NOTE: that the visitRows parameter will be obsolete once the
             //       appropriate visit hints have been added to the API
             if ((result == VisitResult.ACCEPT) && doVisitChildren(context, visitRows)) {
 
                 // First visit facets
                 // NOTE: that the visitRows parameter will be obsolete once the
                 //       appropriate visit hints have been added to the API
                 if (visitFacets(context, callback, visitRows))
                     return true;
 
                 // Next column facets
                 // NOTE: that the visitRows parameter will be obsolete once the
                 //       appropriate visit hints have been added to the API
                 if (visitColumnsAndColumnFacets(context, callback, visitRows))
                     return true;
 
                 // And finally, visit rows
                 // NOTE: that the visitRows parameter will be obsolete once the
                 //       appropriate visit hints have been added to the API
                 if (visitRows(context, callback, visitRows))
                     return true;
             }
         }
         finally {
             // Clean up - pop EL and restore old row index
             popComponentFromEL(facesContext);
             if (visitRows) {
                 setRowIndex(oldRowIndex);
             }
         }
 
         // Return false to allow the visit to continue
         return false;
     }
 
     /**
      * Called by {@link UIData#visitTree} to determine whether or not the
      * <code>visitTree</code> implementation should visit the rows of UIData
      * or by manipulating the row index before visiting the components themselves.
      *
      * Once we have the appropriate Visit hints for state saving, this method
      * will become obsolete.
      *
      * @param ctx the <code>FacesContext</code> for the current request
      *
      * @return true if row index manipulation is required by the visit to this
      *  UIData instance
      */
     private boolean requiresRowIteration(VisitContext ctx) {
         try { // Use JSF 2.1 hints if available
             return !ctx.getHints().contains(VisitHint.SKIP_ITERATION);
         } catch (NoSuchFieldError e) {
             FacesContext fctx = FacesContext.getCurrentInstance();
             return (!PhaseId.RESTORE_VIEW.equals(fctx.getCurrentPhaseId()));
         }
     }
 
     // Tests whether we need to visit our children as part of
     // a tree visit
     private boolean doVisitChildren(VisitContext context, boolean visitRows) {
 
         // Just need to check whether there are any ids under this
         // subtree.  Make sure row index is cleared out since
         // getSubtreeIdsToVisit() needs our row-less client id.
         if (visitRows) {
             setRowIndex(-1);
         }
         Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
         assert(idsToVisit != null);
 
         // All ids or non-empty collection means we need to visit our children.
         return (!idsToVisit.isEmpty());
     }
 
     // Visit each facet of this component exactly once.
     private boolean visitFacets(VisitContext context,
                                 VisitCallback callback,
                                 boolean visitRows) {
 
         if (visitRows) {
             setRowIndex(-1);
         }
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values()) {
                 if (facet.visitTree(context, callback))
                     return true;
             }
         }
 
         return false;
     }
 
     // Visit each UIColumn and any facets it may have defined exactly once
     private boolean visitColumnsAndColumnFacets(VisitContext context,
                                                 VisitCallback callback,
                                                 boolean visitRows) {
         if (visitRows) {
             setRowIndex(-1);
         }
         if (getChildCount() > 0) {
             for (UIComponent column : getChildren()) {
                 if (column instanceof UIColumn) {
                     VisitResult result = context.invokeVisitCallback(column, callback); // visit the column directly
                     if (result == VisitResult.COMPLETE) {
                         return true;
                     }
                     if (column.getFacetCount() > 0) {
                         for (UIComponent columnFacet : column.getFacets().values()) {
                             if (columnFacet.visitTree(context, callback)) {
                                 return true;
                             }
                         }
                     }
                 }
             }
         }
 
         return false;
     }
 
     // Visit each column and row
     private boolean visitRows(VisitContext context,
                               VisitCallback callback,
                               boolean visitRows) {
 
         // first, visit all columns
         if (getChildCount() > 0) {
             for (UIComponent kid : getChildren()) {
                 if (kid.visitTree(context, callback)) {
                     return true;
                 }
             }
         }
 
         // Iterate over our UIColumn children, once per row
         int processed = 0;
         int rowIndex = 0;
         int rows = 0;
         if (visitRows) {
             rowIndex = getFirst() - 1;
             rows = getRows();
         }
 
         while (true) {
 
             // Have we processed the requested number of rows?
             if (visitRows) {
                 if ((rows > 0) && (++processed > rows)) {
                     break;
                 }
                 // Expose the current row in the specified request attribute
                 setRowIndex(++rowIndex);
                 if (!isRowAvailable()) {
                     break; // Scrolled past the last row
                 }
             }
 
             // Visit as required on the *children* of the UIColumn
             // (facets have been done a single time with rowIndex=-1 already)
             if (getChildCount() > 0) {
                 for (UIComponent kid : getChildren()) {
                     if (!(kid instanceof UIColumn)) {
                         if (kid.visitTree(context, callback)) {
                             return true;
                         }
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
 
             if (!visitRows) {
                 break;
             }
 
         }
 
         return false;
     }
 
 static class ChildState implements Serializable {
 
     private Object submittedValue;
     private boolean valid = true;
     private Object value;
     private boolean localValueSet;
 
     Object getSubmittedValue() {
         return submittedValue;
     }
 
     void setSubmittedValue(Object submittedValue) {
         this.submittedValue = submittedValue;
     }
 
     boolean isValid() {
         return valid;
     }
 
     void setValid(boolean valid) {
         this.valid = valid;
     }
 
     Object getValue() {
         return value;
     }
 
     public void setValue(Object value) {
         this.value = value;
     }
 
     boolean isLocalValueSet() {
         return localValueSet;
     }
 
     public void setLocalValueSet(boolean localValueSet) {
         this.localValueSet = localValueSet;
     }
 
 
 }
 }
 
