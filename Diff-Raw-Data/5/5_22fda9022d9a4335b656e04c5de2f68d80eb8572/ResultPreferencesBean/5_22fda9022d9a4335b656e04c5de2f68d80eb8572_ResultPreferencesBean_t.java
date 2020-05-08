 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.search;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.faces.beans.SearchResultBean;
 import com.flexive.faces.beans.SelectBean;
 import com.flexive.faces.listener.JsfPhaseListener;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.faces.messages.FxFacesMsgWarn;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import static com.flexive.shared.EJBLookup.getResultPreferencesEngine;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.search.*;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import org.apache.myfaces.component.html.ext.HtmlDataTable;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.model.DataModel;
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 import java.util.*;
 
 /**
  * Bean for creating and updating result preferences.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class ResultPreferencesBean {
     private static final Log LOG = LogFactory.getLog(ResultPreferencesBean.class);
 
     /**
      * Tomahawk HtmlDataTable wrapper that exposes the internal data model.
      * We need this for other components depending on data tables that persist their
      * model in their viewState (aka preserveDataModel=true).
      */
     public static class WrappedHtmlDataTable extends HtmlDataTable {
         @SuppressWarnings({"MethodOverridesPrivateMethodOfSuperclass"})
         @Override
         public DataModel getDataModel() {
             return super.getDataModel();
         }
     }
 
     private long type = -1;
     private ResultViewType viewType = ResultViewType.LIST;
     private ResultLocation location = AdminResultLocations.ADMIN;
     private ResultPreferencesEdit resultPreferences = null;
     private String addPropertyName = null;
     private String addOrderByName = null;
     private SortDirection addOrderByDirection = SortDirection.ASCENDING;
     private int editColumnIndex = -1;
     private boolean forceSystemDefault;
 
     // cached select list
     private List<SelectItem> properties = null;
     private long cachedTypeId = -1;
     private Map<String, String> propertyLabelMap = null;
     private List<SelectItem> types;
 
     // form components
     private WrappedHtmlDataTable selectedColumnsTable = null;
 
 
     public ResultPreferencesBean() {
         parseRequestParameters();
     }
 
     /**
      * Parse the request parameters and perform actions as requested.
      * Works only if the ResultPreferencesBean remains request-scoped!
      */
     private void parseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 // no action requested
                 return;
             }
             if ("loadSystemDefault".equals(action)) {
                 forceSystemDefault = true;
             }
         } catch (Exception e) {
             LOG.error("Failed to parse request parameters: " + e.getMessage(), e);
         }
     }
 
     public String show() {
         return "resultPreferences";
     }
 
     public String save() {
         try {
             getResultPreferencesEngine().save(getResultPreferences(), type, viewType, location);
             new FxFacesMsgInfo("ResultPreferences.nfo.saved").addToContext();
         } catch (FxApplicationException e) {
             new FxFacesMsgErr("ResultPreferences.err.save", e).addToContext();
         }
         return show();
     }
 
     public void saveSystemDefault() {
         try {
             getResultPreferencesEngine().saveSystemDefault(getResultPreferences(), type, viewType, location);
             new FxFacesMsgInfo("ResultPreferences.nfo.savedSystemDefault").addToContext();
         } catch (FxApplicationException e) {
             new FxFacesMsgErr("ResultPreferences.err.save", e).addToContext();
         }
     }
 
    public void loadSystemDefault() {
        forceSystemDefault = true;
        resultPreferences = null;
    }

     public String cancel() {
         return ((SearchResultBean) FxJsfUtils.getManagedBean("fxSearchResultBean")).show();
     }
 
     public void addColumnProperty(ActionEvent event) {
         getResultPreferences().addSelectedColumn(new ResultColumnInfo(Table.CONTENT, addPropertyName, null));
         addPropertyName = null;
     }
 
     public void removeColumnProperty(ActionEvent event) {
         if (editColumnIndex == -1) {
             return;
         }
         try {
             final ResultColumnInfo info = getResultPreferences().removeSelectedColumn(editColumnIndex);
             // remove property from order by clause too
             List<ResultOrderByInfo> removeOrderByColumns = new ArrayList<ResultOrderByInfo>();
             for (ResultOrderByInfo orderByInfo : getResultPreferences().getOrderByColumns()) {
                 if (orderByInfo.getColumnName().equals(info.getColumnName())) {
                     removeOrderByColumns.add(orderByInfo);
                 }
             }
             for (ResultOrderByInfo removeInfo : removeOrderByColumns) {
                 getResultPreferences().removeOrderByColumn(removeInfo);
             }
         } catch (FxRuntimeException e) {
             new FxFacesMsgErr("ResultPreferences.err.removeRow", e).addToContext();
         }
     }
 
     public void moveColumnPropertyUp(ActionEvent event) {
         moveColumnProperty(-1);
     }
 
     public void moveColumnPropertyDown(ActionEvent event) {
         moveColumnProperty(1);
     }
 
     private void moveColumnProperty(int moveDelta) {
         if (editColumnIndex == -1) {
             return;
         }
         try {
             ResultColumnInfo info = getResultPreferences().removeSelectedColumn(editColumnIndex);
             getResultPreferences().addSelectedColumn(editColumnIndex + moveDelta, info);
         } catch (FxRuntimeException e) {
             new FxFacesMsgErr("ResultPreferences.err.moveRow", e).addToContext();
         }
     }
 
     public void moveOrderByPropertyUp(ActionEvent event) {
         moveOrderByProperty(-1);
     }
 
     public void moveOrderByPropertyDown(ActionEvent event) {
         moveOrderByProperty(1);
     }
 
     private void moveOrderByProperty(int moveDelta) {
         if (editColumnIndex == -1) {
             return;
         }
         try {
             ResultOrderByInfo info = getResultPreferences().removeOrderByColumn(editColumnIndex);
             getResultPreferences().addOrderByColumn(editColumnIndex + moveDelta, info);
         } catch (FxRuntimeException e) {
             new FxFacesMsgErr("ResultPreferences.err.moveRow", e).addToContext();
         }
     }
 
     public void addOrderByProperty(ActionEvent event) {
         getResultPreferences().addOrderByColumn(new ResultOrderByInfo(Table.CONTENT, addOrderByName, null,
                 addOrderByDirection));
     }
 
     public void removeOrderByProperty(ActionEvent event) {
         if (editColumnIndex == -1) {
             return;
         }
         try {
             getResultPreferences().removeOrderByColumn(editColumnIndex);
         } catch (FxRuntimeException e) {
             new FxFacesMsgErr("ResultPreferences.err.removeRow", e).addToContext();
         }
     }
 
     public void reloadPreferences(ActionEvent event) {
         this.resultPreferences = null;
     }
 
     public long getType() {
         return type;
     }
 
     public void setType(long type) {
         this.type = type;
     }
 
     public List<SelectItem> getTypes() throws FxApplicationException {
         if (types == null) {
             types = new ArrayList<SelectItem>(FxJsfUtils.getManagedBean(SelectBean.class).getTypes());
             // remove root type
             for (Iterator<SelectItem> iterator = types.iterator(); iterator.hasNext();) {
                 if (iterator.next().getValue().equals(FxType.ROOT_ID)) {
                     iterator.remove();
                     break;
                 }
             }
             types.add(0, new SelectItem(-1, FxJsfUtils.getLocalizedMessage("ResultPreferences.label.allTypes")));
         }
         return types;
     }
 
     public ResultViewType getViewType() {
         return viewType;
     }
 
     public void setViewType(ResultViewType viewType) {
         this.viewType = viewType;
     }
 
     public boolean isThumbnails() {
         return ResultViewType.THUMBNAILS.equals(viewType);
     }
 
     public ResultLocation getLocation() {
         return location;
     }
 
     public void setLocation(ResultLocation location) {
         this.location = location;
     }
 
     public String getAddPropertyName() {
         return addPropertyName;
     }
 
     public void setAddPropertyName(String addPropertyName) {
         this.addPropertyName = addPropertyName;
     }
 
     public int getEditColumnIndex() {
         return editColumnIndex;
     }
 
     public void setEditColumnIndex(int editColumnIndex) {
         this.editColumnIndex = editColumnIndex;
     }
 
     public SortDirection getAddOrderByDirection() {
         return addOrderByDirection;
     }
 
     public void setAddOrderByDirection(SortDirection addOrderByDirection) {
         this.addOrderByDirection = addOrderByDirection;
     }
 
     public String getAddOrderByName() {
         return addOrderByName;
     }
 
     public void setAddOrderByName(String addOrderByName) {
         this.addOrderByName = addOrderByName;
     }
 
     public boolean isCustomized() throws FxApplicationException {
         return getResultPreferencesEngine().isCustomized(type, viewType, location);
     }
 
     public boolean isForceSystemDefault() {
         return forceSystemDefault;
     }
 
     public void setForceSystemDefault(boolean forceSystemDefault) {
         this.forceSystemDefault = forceSystemDefault;
     }
 
     public ResultPreferencesEdit getResultPreferences() {
         if (resultPreferences == null) {
             try {
                 if (forceSystemDefault) {
                     resultPreferences = getResultPreferencesEngine().loadSystemDefault(type, viewType, location).getEditObject();
                 } else {
                     resultPreferences = getResultPreferencesEngine().load(type, viewType, location).getEditObject();
                 }
             } catch (FxNotFoundException e) {
                 new FxFacesMsgWarn("ResultPreferences.wng.notFound").addToContext();
                 resultPreferences = new ResultPreferences().getEditObject();
             } catch (FxApplicationException e) {
                 new FxFacesMsgErr("ResultPreferences.err.load", e).addToContext();
             }
         }
         return resultPreferences;
     }
 
     public List<SelectItem> getProperties() {
         if (properties == null || cachedTypeId != getType()) {
             final FxEnvironment environment = CacheAdmin.getFilteredEnvironment();
             final FxType type = environment.getType(getType() != -1 ? getType() : FxType.ROOT_ID);
             final List<FxPropertyAssignment> contentProperties = type.getAssignedProperties();
             properties = new ArrayList<SelectItem>(contentProperties.size() + 10);
             final MessageBean messageBean = MessageBean.getInstance();
             // add virtual properties...
             final SelectItemGroup virtualGroup = new SelectItemGroup(messageBean.getMessage("ResultPreferences.label.group.virtual"));
             virtualGroup.setSelectItems(new SelectItem[]{
                     new SelectItem("@pk", messageBean.getMessage("ResultPreferences.label.property.pk")),
                     new SelectItem("@path", messageBean.getMessage("ResultPreferences.label.property.path")),
                     new SelectItem("@permissions", messageBean.getMessage("ResultPreferences.label.property.permissions"))
             });
             properties.add(virtualGroup);
             // add type properties
             properties.add(filteredPropertiesGroup(contentProperties, messageBean.getMessage("ResultPreferences.label.group.type", type.getLabel().getBestTranslation()), false));
             // add derived properties
             properties.add(filteredPropertiesGroup(contentProperties, messageBean.getMessage("ResultPreferences.label.group.derived"), true));
 
             cachedTypeId = getType();
         }
         return properties;
     }
 
     private SelectItemGroup filteredPropertiesGroup(List<FxPropertyAssignment> contentProperties, String title, boolean includeDerived) {
         final SelectItemGroup group = new SelectItemGroup(title);
         final List<SelectItem> properties = new ArrayList<SelectItem>();
         for (FxPropertyAssignment assignment : contentProperties) {
             if (includeDerived && assignment.isDerivedAssignment() || (!includeDerived && !assignment.isDerivedAssignment())) {
                 properties.add(new SelectItem(assignment.getProperty().getName(), assignment.getProperty().getLabel().getBestTranslation()));
             }
         }
         // sort by label
         Collections.sort(properties, new FxJsfUtils.SelectItemSorter());
         group.setSelectItems(properties.toArray(new SelectItem[properties.size()]));
         return group;
     }
 
     public List<SelectItem> getSelectedProperties() {
         // caching this is not trivial because the selectedColumns list
         // is updated between phases by the datatable
         final FxEnvironment environment = CacheAdmin.getFilteredEnvironment();
         final List<ResultColumnInfo> columns;
         if (JsfPhaseListener.isInPhase(PhaseId.PROCESS_VALIDATIONS)) {
             //noinspection unchecked
             columns = (List) ((WrappedHtmlDataTable) getSelectedColumnsTable()).getDataModel().getWrappedData();
         } else {
             columns = getResultPreferences().getSelectedColumns();
         }
         List<SelectItem> result = new ArrayList<SelectItem>(columns.size());
         for (ResultColumnInfo info : columns) {
             result.add(new SelectItem(info.getPropertyName(), info.getLabel(environment)));
         }
         return result;
     }
 
     public HtmlDataTable getSelectedColumnsTable() {
         if (selectedColumnsTable == null) {
             selectedColumnsTable = new WrappedHtmlDataTable();
         } else {
             UIComponent parent = selectedColumnsTable.getParent();
             while (parent.getParent() != null) {
                 parent = parent.getParent();
             }
             if (parent instanceof UIViewRoot && parent != FacesContext.getCurrentInstance().getViewRoot()) {
                 // create new table when view root changes
                 System.out.println("View root changed - creating new wrapped table.");
                 selectedColumnsTable = new WrappedHtmlDataTable();
             }
         }
         return selectedColumnsTable;
     }
 
     public void setSelectedColumnsTable(HtmlDataTable selectedColumnsTable) {
         this.selectedColumnsTable = (WrappedHtmlDataTable) selectedColumnsTable;
     }
 
     /**
      * Provides the property labels for the result preferences page, including virtual properties like @pk.
      *
      * @return the property labels for the result preferences page, including virtual properties like @pk.
      */
     public Map<String, String> getPropertyLabelMap() {
         if (propertyLabelMap == null) {
             propertyLabelMap = FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<String, String>() {
                 private FxEnvironment environment = CacheAdmin.getFilteredEnvironment();
 
                 public String get(Object key) {
                     if (key == null) {
                         return null;
                     }
                     final String name = key.toString();
                     if (name.charAt(0) == '@') {
                         return MessageBean.getInstance().getMessage("ResultPreferences.label.property." + name.substring(1));
                     } else {
                         return environment.getProperty(name).getLabel().getBestTranslation();
                     }
                 }
             });
         }
         return propertyLabelMap;
     }
 }
