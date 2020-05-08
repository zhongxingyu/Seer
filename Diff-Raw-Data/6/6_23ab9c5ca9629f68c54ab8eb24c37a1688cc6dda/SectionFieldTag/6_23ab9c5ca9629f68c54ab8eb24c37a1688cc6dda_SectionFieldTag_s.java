 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.web.customization.tag.fields;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.MessageService;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.LayoutType;
 import com.orangeleap.tangerine.type.MessageResourceType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.web.customization.tag.AbstractTag;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.ExtTypeHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandlerHelper;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.grid.GridHandler;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import java.util.List;
 import java.util.Map;
 
 public class SectionFieldTag extends AbstractTag {
 
 	protected final Log logger = OLLogger.getLog(getClass());
 
 	private PageCustomizationService pageCustomizationService;
 	private TangerineUserHelper tangerineUserHelper;
 	private MessageService messageService;
 	private FieldService fieldService;
 	private String pageName;
 	private FieldHandlerHelper fieldHandlerHelper;
 	private GridHandler gridHandler;
 
 	public void setPageName(String pageName) {
 	    this.pageName = pageName;
 	}
 	
 	@Override
 	protected int doStartTagInternal() throws Exception {
 		ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.pageContext.getServletContext());
 		pageCustomizationService = (PageCustomizationService) appContext.getBean("pageCustomizationService");
 		tangerineUserHelper = (TangerineUserHelper) appContext.getBean("tangerineUserHelper");
 		messageService = (MessageService) appContext.getBean("messageService");
 		fieldService = (FieldService) appContext.getBean("fieldService");
 		fieldHandlerHelper = (FieldHandlerHelper) appContext.getBean("fieldHandlerHelper");
 		gridHandler = (GridHandler) appContext.getBean("gridHandler");
 
 		List<SectionDefinition> sectionDefs = pageCustomizationService.readSectionDefinitionsByPageTypeRoles(PageType.valueOf(pageName), tangerineUserHelper.lookupUserRoles());
 
 		writeSections(sectionDefs);
 
 		pageContext.getRequest().setAttribute(StringConstants.SECTION_DEFINITIONS, sectionDefs);
 		
 		return EVAL_BODY_INCLUDE;
 	}
 
 	protected void writeSections(List<SectionDefinition> sectionDefinitions) throws Exception {
 		if (sectionDefinitions != null) {
 			int oneColumnCount = 0;
 
 			for (int x = 0; x < sectionDefinitions.size(); x++) {
 				SectionDefinition sectionDef = sectionDefinitions.get(x);
 
 				List<SectionField> sectionFields = pageCustomizationService.readSectionFieldsBySection(sectionDef);
 
 				StringBuilder sb = new StringBuilder();
 				if (LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 					writeColumnsStart(sectionDef, sb);
 
 					/* First column */
 					writeSingleColumnStart(sectionDef, sb);
 					writeSectionField(sectionDef, sectionFields, sb, true);
 					writeSingleColumnEnd(sb);
 
 					/* Second column */
 					writeSingleColumnStart(sectionDef, sb);
 					writeSectionField(sectionDef, sectionFields, sb, false);
 					writeSingleColumnEnd(sb);
 
 					writeColumnsEnd(sb);
 				}
 				else if (LayoutType.ONE_COLUMN.equals(sectionDef.getLayoutType()) ||
 						LayoutType.ONE_COLUMN_HIDDEN.equals(sectionDef.getLayoutType())) {
 					int prevIndex = x - 1;
 					if (prevIndex >= 0) {
 						if ( ! LayoutType.isSingleColumnType(sectionDefinitions.get(prevIndex).getLayoutType())) {
 							writeColumnsStart(sectionDef, sb);
 						}
 						else if (oneColumnCount == 0) {
 							writeColumnsStart(sectionDef, sb);
 						}
 					}
 					else {
 						writeColumnsStart(sectionDef, sb);
 					}
 
 					writeSingleColumnStart(sectionDef, sb);
 					writeSectionField(sectionDef, sectionFields, sb);
 					writeSingleColumnEnd(sb);
 
 					oneColumnCount++;
 
 					int nextIndex = x + 1;
 
 					if (nextIndex < sectionDefinitions.size()) {
 						if ( ! LayoutType.isSingleColumnType(sectionDefinitions.get(nextIndex).getLayoutType())) {
 							writeColumnsEnd(sb);
 							oneColumnCount = 0;
 						}
 						else if (LayoutType.ONE_COLUMN.equals(sectionDefinitions.get(nextIndex).getLayoutType()) &&
 									oneColumnCount >= 2) {
 							writeColumnsEnd(sb);
 							oneColumnCount = 0;
 						}
 					}
 					else {
 						writeColumnsEnd(sb);
 						oneColumnCount = 0;
 					}
 				}
 				else if (LayoutType.GRID.equals(sectionDef.getLayoutType()) || LayoutType.SEARCH_GRID.equals(sectionDef.getLayoutType())) {
                     List<SectionField> allFields = pageCustomizationService.readSectionFieldsBySection(sectionDef);
                     assert allFields != null && !allFields.isEmpty();
 
                     List<SectionField> fields = pageCustomizationService.getFieldsExceptId(allFields);
                     Object entity = getTangerineForm().getDomainObject();
                     BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);
                     String entityType = StringUtils.uncapitalize(entity.getClass().getSimpleName());
                     boolean isListGrid = LayoutType.GRID.equals(sectionDef.getLayoutType());
                     String searchTypeValue = (String) pageContext.getRequest().getAttribute(StringConstants.SEARCH_TYPE);
 
                     sb.append("<script type='text/javascript'>");
                     sb.append("Ext.namespace('OrangeLeap.").append(entityType).append("');\n");
 
                     if (!isListGrid) {
                         sb.append("OrangeLeap.SearchStore = Ext.extend(Ext.data.JsonStore, {\n");
                         sb.append("sort: function(fieldName, dir) {\n");
                         sb.append("this.lastOptions.params['start'] = 0;\n");
                         sb.append("this.lastOptions.params['limit'] = 100;\n");
                         sb.append("this.lastOptions.params['autoLoad'] = false;\n");
                         sb.append("delete this.lastOptions.params['").append(StringConstants.FULLTEXT).append("'];\n");
                         sb.append("return OrangeLeap.SearchStore.superclass.sort.call(this, fieldName, dir);\n");
                         sb.append("}});\n");
                         sb.append("OrangeLeap.barTextItem = new Ext.Toolbar.TextItem({text: ' '});\n");
                     }
                     else {
                         sb.append("OrangeLeap.ListStore = Ext.extend(Ext.data.JsonStore, {\n");
                         sb.append("sort: function(fieldName, dir) {\n");
                         sb.append("this.lastOptions.params['start'] = 0;\n");
                         sb.append("this.lastOptions.params['limit'] = 100;\n");
                         sb.append("return OrangeLeap.ListStore.superclass.sort.call(this, fieldName, dir);\n");
                         sb.append("}});\n");
                     }
                     sb.append("Ext.onReady(function() {\n");
                     sb.append("Ext.QuickTips.init();\n");
                     sb.append("OrangeLeap.").append(entityType).append(".store = new ").append(isListGrid ? "OrangeLeap.ListStore" : "OrangeLeap.SearchStore").append("({\n");
 
                     String gridType = isListGrid ? "List" : (StringConstants.FULLTEXT.equals(searchTypeValue) ? "FullTextSearch" : "Search");
                     sb.append("url: '").append(entityType).append(gridType).append(".json");
                     if (isListGrid && (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID))) {
                         sb.append("?constituentId=");
                         if (bw.isReadableProperty(StringConstants.CONSTITUENT)) {
                             sb.append(((Constituent) bw.getPropertyValue(StringConstants.CONSTITUENT)).getId());
                         }
                         else if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                             sb.append(bw.getPropertyValue(StringConstants.CONSTITUENT_ID));
                         }
                     }
                     sb.append("',\n");
                     sb.append("totalProperty: 'totalRows',\n");
                     sb.append("root: 'rows',\n");
                     sb.append("remoteSort: true,\n");
                     sb.append("fields: [\n");
 
                     sb.append("{name: 'id', mapping: 'id', type: 'int'},\n");
                     if (isListGrid && (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID))) {
                         sb.append("{name: 'constituentId', mapping: 'constituentId', type: 'string'},\n");
                     }
                     else if (!isListGrid && bw.isReadableProperty(StringConstants.CONSTITUENT)) {
                         sb.append("{name: '").append(TangerineForm.escapeFieldName(StringConstants.CONSTITUENT_DOT_ID)).append("', mapping: '").append(TangerineForm.escapeFieldName(StringConstants.CONSTITUENT_DOT_ID)).append("', type: 'string'},\n");
                     }
 
                     int z = 0;
                     for (SectionField sectionFld : fields) {
                         String escapedFieldName = TangerineForm.escapeFieldName(sectionFld.getFieldPropertyName());
 
                         sb.append("{name: '").append(escapedFieldName).append("', ");
                         sb.append("mapping: '").append(escapedFieldName).append("', ");
                         String extType = ExtTypeHandler.findExtType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
                         sb.append("type: '").append(extType).append("'");
                         if (StringConstants.DATE.equals(extType)) {
                             sb.append(", dateFormat: '");
                             if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                                 sb.append("m-d-Y");
                             }
                             else {
                                 sb.append("Y-m-d H:i:s");
                             }
                             sb.append("'");
                         }
                         sb.append("}");
                         if (++z < fields.size()) {
                             sb.append(",\n");
                         }
                         else {
                             sb.append("\n");
                         }
                     }
                     sb.append("]\n");
                     String direction = getInitDirection(fields);
                     sb.append("});\n");
 
                     sb.append("OrangeLeap.").append(entityType).append(".bar = ");
                     if (isListGrid) {
                         sb.append("new Ext.PagingToolbar({\n");
                         sb.append("pageSize: 100,\n");
                         sb.append("stateEvents: ['change'],\n");
                         sb.append("stateId: '").append(entityType).append("PageBar',\n");
                         sb.append("stateful: true,\n");
                         sb.append("getState: function() {\n");
                         sb.append("var config = {};\n");
                         sb.append("config.start = this.cursor;\n");
                         sb.append("config.limit = this.pageSize;\n");
                         sb.append("var queryParams = OrangeLeap.getQueryParams();\n");
                         sb.append("if (queryParams) {\n");
                         sb.append("var thisConstituentId = queryParams['constituentId'];\n");
                         sb.append("if (thisConstituentId) {\n");
                         sb.append("config.constituentId = thisConstituentId;\n");
                         sb.append("}\n");
                         sb.append("}\n");
                         sb.append("return config;\n");
                         sb.append("},\n");
                         sb.append("applyState: function(state, config) {\n");
                         sb.append("var queryParams = OrangeLeap.getQueryParams();\n");
                         sb.append("if (queryParams) {\n");
                         sb.append("var thisConstituentId = queryParams['constituentId'];\n");
                         sb.append("if (!thisConstituentId || (thisConstituentId && (state.constituentId == thisConstituentId))) {\n");
                         sb.append("if (state.start) {\n");
                         sb.append("this.cursor = state.start;\n");
                         sb.append("}\n");
                         sb.append("if (state.limit) {\n");
                         sb.append("this.pageSize = state.limit;\n");
                         sb.append("}\n");
                         sb.append("}\n");
                         sb.append("}\n");
                         sb.append("},\n");
                         sb.append("store: OrangeLeap.").append(entityType).append(".store,\n");
                         sb.append("displayInfo: true,\n");
                         sb.append("displayMsg: '").append(TangerineMessageAccessor.getMessage("displayMsg")).append("',\n");
                         sb.append("emptyMsg: '").append(TangerineMessageAccessor.getMessage("emptyMsg")).append("'\n");
                         sb.append("});\n");
                     }
                     else {
                         sb.append("new Ext.Toolbar([\n");
                         sb.append("OrangeLeap.barTextItem\n");
                         sb.append("]);\n");
                     }
 
                     sb.append("OrangeLeap.").append(entityType).append(".grid = new Ext.grid.GridPanel({\n");
                     sb.append("stateId: '").append(entityType).append(gridType).append("',\n");
                     sb.append("stateEvents: ['columnmove', 'columnresize', 'sortchange', 'bodyscroll'],\n");
                     sb.append("stateful: true,\n");
                     sb.append("getState: function() {\n");
                     sb.append("var config = {};\n");
                     sb.append("var cm = this.getColumnModel();\n");
                     sb.append("var sortState = this.store.getSortState();\n");
                     sb.append("if (sortState) {\n");
                     sb.append("config.sf = sortState['field'];\n");
                     sb.append("config.sd = sortState['direction'];\n");
                     sb.append("}\n");
                     sb.append("config.ss = this.getView().getScrollState();\n");
                     sb.append("config.mc = [];\n");
                     sb.append("for (var i = 0; i < cm.config.length; i++) {\n");
                     sb.append("config.mc[i] = {};\n");
                     sb.append("config.mc[i].di = cm.config[i].dataIndex;\n");
                     sb.append("config.mc[i].h = cm.config[i].hidden;\n");
                     sb.append("config.mc[i].w = cm.config[i].width;\n");
                     sb.append("}\n");
                     sb.append("return config;\n");
                     sb.append("},\n");
                     sb.append("applyState: function(state, config) {\n");
                     sb.append("if (state.mc != null) {\n");
                     sb.append("var cm = this.getColumnModel();\n");
                     sb.append("for (var i = 0; i < state.mc.length; i++) {\n");
                     sb.append("var colIndex = cm.findColumnIndex(state.mc[i].di);\n");
                    sb.append("if (colIndex != -1)\n");
                     sb.append("if (colIndex != i) {\n");
                     sb.append("cm.moveColumn(colIndex, i);\n");
                     sb.append("}\n");
                     sb.append("cm.setHidden(i, state.mc[i].h);\n");
                     sb.append("cm.setColumnWidth(i, state.mc[i].w);\n");
                     sb.append("}\n");
                     sb.append("}\n");
                    sb.append("if (state.sf && state.sd) {\n");
                     sb.append("this.sortParams = { direction: state.sd, dataIndex: state.sf };\n");
                     sb.append("}\n");
                     sb.append("if (state.ss) {\n");
                     sb.append("this.getView().prevScrollState = state.ss;\n");
                     sb.append("}\n");
                     sb.append("},\n");
                     sb.append("store: OrangeLeap.").append(entityType).append(".store,\n");
                     sb.append("addClass: 'pointer',\n");
                     sb.append("columns: [\n");
 
                     int y = 0;
                     for (SectionField sectionFld : fields) {
                         sb.append("{header: '").append(sectionFld.getFieldDefinition().getDefaultLabel()).append("', ");
                         sb.append("dataIndex: '").append(TangerineForm.escapeFieldName(sectionFld.getFieldPropertyName())).append("', sortable: ");
                         sb.append( ! isListGrid && StringConstants.FULLTEXT.equals(searchTypeValue) ? Boolean.FALSE.toString() : Boolean.TRUE.toString());
 
                         String extType = ExtTypeHandler.findExtType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
                         if (ExtTypeHandler.EXT_FLOAT.equals(extType) || ExtTypeHandler.EXT_BOOLEAN.equals(extType) ||
                                 ExtTypeHandler.EXT_DATE.equals(extType) || ExtTypeHandler.EXT_STRING.equals(extType)) {
                             sb.append(", renderer: ");
                             if (ExtTypeHandler.EXT_DATE.equals(extType)) {
                                 sb.append("Ext.util.Format.dateRenderer('");
                                 if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                                     sb.append("m / Y");
                                 }
                                 else {
                                     sb.append("m-d-y g:ia");
                                 }
                                 sb.append("')\n");
                             }
                             else if (ExtTypeHandler.EXT_FLOAT.equals(extType)) {
                                 sb.append("OrangeLeap.amountRenderer\n");
                             }
                             else if (ExtTypeHandler.EXT_BOOLEAN.equals(extType)) {
                                 sb.append("OrangeLeap.booleanRenderer\n");
                             }
                             else {
                                 sb.append("function(value, metaData, record, rowIndex, colIndex, store) {");
                                 sb.append("return '<span ext:qtitle=\"").append(sectionFld.getFieldDefinition().getDefaultLabel()).append("\" ext:qwidth=\"250\" ext:qtip=\"' + value + '\">' + value + '</span>';");
                                 sb.append("}\n");
                             }
                         }
                         sb.append("}");
 
                         if (++y < fields.size()) {
                             sb.append(",\n");
                         }
                         else {
                             sb.append("\n");
                         }
                     }
                     sb.append("],\n");
                     sb.append("sm: new Ext.grid.RowSelectionModel({singleSelect: true}),\n");
                     sb.append("viewConfig: { forceFit: true },\n");
                     sb.append("height: ").append(isListGrid ? "600" : (StringConstants.FULLTEXT.equals(searchTypeValue) ? "600" : "400")).append(",\n");
                     sb.append("width: 780,\n");
                     sb.append("frame: true,\n");
                     sb.append("header: true,\n");
                     sb.append("title: '").append(TangerineMessageAccessor.getMessage(entityType)).append(" ");
                     sb.append(isListGrid ? TangerineMessageAccessor.getMessage("list") : TangerineMessageAccessor.getMessage("searchResults")).append("',\n");
                     sb.append("loadMask: true,\n");
                     sb.append("listeners: {\n");
                     sb.append("rowdblclick: function(grid, row, evt) {\n");
                     sb.append("var rec = grid.getSelectionModel().getSelected();\n");
                     sb.append("Ext.get(document.body).mask('").append(TangerineMessageAccessor.getMessage("loadingRecord")).append("');\n");
                     sb.append("window.location.href = \"");
                     if (StringConstants.PAYMENT_SOURCE.equals(entityType)) {
                         sb.append("paymentManagerEdit");
                     }
                     else if (StringConstants.ADDRESS.equals(entityType) || StringConstants.PHONE.equals(entityType) || StringConstants.EMAIL.equals(entityType)) {
                         sb.append(entityType).append("ManagerEdit");
                     }
                     else {
                         sb.append(entityType);
                     }
                     sb.append(".htm?");
                     sb.append(entityType).append("Id=\" + rec.data.id");
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                         sb.append(" + \"&constituentId=");
                         if (isListGrid) {
                             if (bw.isReadableProperty(StringConstants.CONSTITUENT)) {
                                 sb.append(((Constituent) bw.getPropertyValue(StringConstants.CONSTITUENT)).getId());
                             }
                             else if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                                 sb.append(bw.getPropertyValue(StringConstants.CONSTITUENT_ID));
                             }
                             sb.append("\"");
                         }
                         else {
                             sb.append("\" + rec.data['").append(TangerineForm.escapeFieldName(StringConstants.CONSTITUENT_DOT_ID)).append("']");
                         }
                     }
                     sb.append(";\n");
                     sb.append("}\n");
                     sb.append("},\n");
                     sb.append("bbar: OrangeLeap.").append(entityType).append(".bar,\n");
                     sb.append("renderTo: '").append(entityType).append("Grid'\n");
                     sb.append("});\n");
                     if (isListGrid) {
                         sb.append("var sortDir = '").append(direction).append("';\n");
                         sb.append("var sortProp = '").append(TangerineForm.escapeFieldName(fields.get(0).getFieldPropertyName())).append("';\n");
                         sb.append("var pageStart = 0;\n");
                         sb.append("var pageLimit = 100;\n");
                         sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams) {\n");
                         sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams.direction) {\n");
                         sb.append("sortDir = OrangeLeap.").append(entityType).append(".grid.sortParams.direction;\n");
                         sb.append("}\n");
                         sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams.dataIndex) {\n");
                         sb.append("sortProp = OrangeLeap.").append(entityType).append(".grid.sortParams.dataIndex;\n");
                         sb.append("}\n");
                         sb.append("}\n");
                         sb.append("if (OrangeLeap.").append(entityType).append(".bar.cursor) {\n");
                         sb.append("pageStart = OrangeLeap.").append(entityType).append(".bar.cursor;\n");
                         sb.append("}\n");
                         sb.append("if (OrangeLeap.").append(entityType).append(".bar.pageSize) {\n");
                         sb.append("pageLimit = OrangeLeap.").append(entityType).append(".bar.pageSize;\n");
                         sb.append("}\n");
                         sb.append("OrangeLeap.").append(entityType).append(".store.sortToggle[sortProp] = sortDir;\n");
                         sb.append("OrangeLeap.").append(entityType).append(".store.sortInfo = { field: sortProp, direction: sortDir };\n");
                         sb.append("OrangeLeap.").append(entityType).append(".store.load({params: {start: pageStart, limit: pageLimit, sort: sortProp, dir: sortDir}, callback: function(rec, options, success) {\n");
                         sb.append("var thisView = OrangeLeap.").append(entityType).append(".grid.getView();\n");
                         sb.append("if (thisView.prevScrollState) {\n");
                         sb.append("thisView.restoreScroll(thisView.prevScrollState);\n");
                         sb.append("}\n");
                         sb.append("}\n");
                         sb.append("});\n");
                     }
                     else {
                         if (Boolean.TRUE.toString().equalsIgnoreCase(pageContext.getRequest().getParameter("autoLoad"))) {
 
                             sb.append("var sortDir = '").append(direction).append("';\n");
                             sb.append("var sortProp = '").append(TangerineForm.escapeFieldName(fields.get(0).getFieldPropertyName())).append("';\n");
                             sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams) {\n");
                             sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams.direction) {\n");
                             sb.append("sortDir = OrangeLeap.").append(entityType).append(".grid.sortParams.direction;\n");
                             sb.append("}\n");
                             sb.append("if (OrangeLeap.").append(entityType).append(".grid.sortParams.dataIndex) {\n");
                             sb.append("sortProp = OrangeLeap.").append(entityType).append(".grid.sortParams.dataIndex;\n");
                             sb.append("}\n");
                             sb.append("}\n");
 
                             sb.append("OrangeLeap.").append(entityType).append(".store.sortToggle[sortProp] = (sortDir == 'ASC' ? 'DESC' : 'ASC');\n");
                             sb.append("OrangeLeap.").append(entityType).append(".store.sortInfo = { field: sortProp, direction: sortDir };\n");
                             sb.append("OrangeLeap.").append(entityType).append(".store.load({");
                             sb.append("callback: OrangeLeap.").append(entityType).append(".searchCallback, ");
                             sb.append("params: {start: 0, limit: 200, autoLoad: true, sort: sortProp, dir: sortDir ");
 
                             String searchFieldValue = pageContext.getRequest().getParameter(StringConstants.SEARCH_FIELD);
                             if (StringUtils.hasText(searchFieldValue)) {
                                 if (StringConstants.CONSTITUENT.equals(searchTypeValue)) {
                                     sb.append(", \"").append(StringConstants.LAST_NAME).append("\": '").append(searchFieldValue).append("' ");
                                 }
                                 else if (StringConstants.GIFT.equals(searchTypeValue)) {
                                     sb.append(", \"amount\": '").append(searchFieldValue).append("' ");
                                 }
                                 else if (StringConstants.FULLTEXT.equals(searchTypeValue)) {
                                     sb.append(", \"").append(StringConstants.FULLTEXT).append("\": '").append(searchFieldValue).append("' ");
                                 }
                             }
                             sb.append("}});\n");
                         }
                         if ( ! StringConstants.FULLTEXT.equals(searchTypeValue)) {
                             sb.append("$(\"#").append(entityType).append("SearchButton\").click(function() {\n");
                             sb.append("OrangeLeap.").append(entityType).append(".store.load({");
                             sb.append("callback: OrangeLeap.").append(entityType).append(".updateBar, ");
                             sb.append("params: {start: 0, limit: 200, autoLoad: false, sort: '");
                             sb.append(TangerineForm.escapeFieldName(fields.get(0).getFieldPropertyName())).append("', dir: '").append(direction).append("', ");
 
                             y = 0;
                             for (SectionField sectionFld : fields) {
                                 String escapedFieldName = TangerineForm.escapeFieldName(sectionFld.getFieldPropertyName());
                                 sb.append("\"").append(escapedFieldName).append("\"").append(": $(\"#").append(escapedFieldName).append("\").val() ");
                                 if (++y < fields.size()) {
                                     sb.append(", ");
                                 }
                             }
                             sb.append("}});\n");
                             sb.append("});\n");
                             sb.append("$(\"#").append(entityType).append("SearchForm\").submit(function() {\n");
                             sb.append("return false;\n");
                             sb.append("});\n");
                         }
                     }
                     sb.append("});\n");
                     if (!isListGrid) {
                         sb.append("OrangeLeap.").append(entityType).append(".searchCallback = function(r, options, success) {\n");
                         sb.append("if (success) {\n");
                         sb.append("var thisView = OrangeLeap.").append(entityType).append(".grid.getView();\n");
                         sb.append("if (thisView.prevScrollState) {\n");
                         sb.append("thisView.restoreScroll(thisView.prevScrollState);\n");
                         sb.append("}\n");
                         sb.append("var thisLength = this.getTotalCount();\n");
                         sb.append("if (thisLength == 0) {\n");
                         sb.append("var thisText = '").append(TangerineMessageAccessor.getMessage("emptyMsg")).append("';\n");
                         sb.append("}\n");
                         sb.append("else {\n");
                         sb.append("var thisText = '").append(TangerineMessageAccessor.getMessage("searchDisplayMsg")).append(" ' + thisLength + ' ").append(TangerineMessageAccessor.getMessage("searchResults")).append("';\n");
                         sb.append("}\n");
                         sb.append("OrangeLeap.barTextItem.setText(thisText);\n");
                         sb.append("};\n");
                         sb.append("};\n");
                     }
                     sb.append("</script>");
 				}
                 else if (LayoutType.TREE_GRID.equals(sectionDef.getLayoutType())) {
                     int nextIndex = x + 1;
                     if (nextIndex < sectionDefinitions.size()) {
                         if (LayoutType.TREE_GRID_HIDDEN_ROW.equals(sectionDefinitions.get(nextIndex).getLayoutType())) {
                             x += 1; // skip the next section (the hidden grid tree row) because it will be handled via ajax request
                         }
                     }
                     List<SectionField> allFields = pageCustomizationService.readSectionFieldsBySection(sectionDef);
                     assert allFields != null && !allFields.isEmpty();
 
                     List<SectionField> fields = pageCustomizationService.getFieldsExceptId(allFields);
                     Object entity = getTangerineForm().getDomainObject();
                     BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);
                     String entityType = StringUtils.uncapitalize(entity.getClass().getSimpleName());
 
                     sb.append("<script type='text/javascript'>");
                     sb.append("Ext.namespace('OrangeLeap.").append(entityType).append("');\n");
                     sb.append("OrangeLeap.").append(entityType).append(".controller = function() {\n");
                     sb.append("function createGrid() {\n");
                     sb.append("var record = Ext.data.Record.create([\n");
                     
                     sb.append("{name: 'id', mapping: 'id', type: 'int'},\n");
                     sb.append("{name: '_parent', type: 'auto'},\n");
                     sb.append("{name: '_is_leaf', type: 'bool'},\n");
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                         sb.append("{name: 'constituentId', mapping: 'constituentId', type: 'string'},\n");
                     }
 
                     int z = 0;
                     for (SectionField sectionFld : fields) {
                         sb.append("{name: 'a").append(z).append("', ");
                         sb.append("mapping: 'a").append(z).append("', ");
                         String extType = ExtTypeHandler.findExtType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
                         sb.append("type: '").append(extType).append("'");
                         if ("date".equals(extType)) {
                             sb.append(", dateFormat: '");
                             if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                                 sb.append("m-d-Y");
                             }
                             else {
                                 sb.append("Y-m-d H:i:s");
                             }
                             sb.append("'");
                         }
                         sb.append("}");
                         if (++z < fields.size()) {
                             sb.append(",\n");
                         }
                         else {
                             sb.append("\n");
                         }
                     }
                     sb.append("]);\n");
                     sb.append("var store = new Ext.ux.maximgb.tg.AdjacencyListStore({\n");
                     sb.append("remoteSort: true,\n");
                     sb.append("sortInfo: { field: 'a0', direction: '").append(getInitDirection(fields)).append("' },\n");
                     sb.append("url: '").append(entityType).append("List.json");
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                         sb.append("?constituentId=");
                         if (bw.isReadableProperty(StringConstants.CONSTITUENT)) {
                             sb.append(((Constituent) bw.getPropertyValue(StringConstants.CONSTITUENT)).getId());
                         }
                         else if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                             sb.append(bw.getPropertyValue(StringConstants.CONSTITUENT_ID));
                         }
                     }
                     sb.append("',\n");
                     sb.append("reader: new Ext.data.JsonReader({\n");
                     sb.append("id: 'id',\n");
                     sb.append("root: 'rows',\n");
                     sb.append("totalProperty: 'totalRows',\n");
                     sb.append("successProperty: 'success'\n");
                     sb.append("}, record)\n");
                     sb.append("});\n");
                     sb.append("var grid = new Ext.ux.maximgb.tg.GridPanel({\n");
                     sb.append("stateId: '").append(entityType).append("Grid',\n");
                     sb.append("stateEvents: ['columnmove', 'columnresize', 'sortchange', 'bodyscroll'],\n");
                     sb.append("stateful: true,\n");
                     sb.append("getState: function() {\n");
                     sb.append("var config = {};\n");
                     sb.append("var cm = this.getColumnModel();\n");
                     sb.append("var sortState = this.store.getSortState();\n");
                     sb.append("if (sortState) {\n");
                     sb.append("config.sf = sortState['field'];\n");
                     sb.append("config.sd = sortState['direction'];\n");
                     sb.append("}\n");
                     sb.append("config.ss = this.getView().getScrollState();\n");
                     sb.append("config.mc = [];\n");
                     sb.append("for (var i = 0; i < cm.config.length; i++) {\n");
                     sb.append("config.mc[i] = {};\n");
                     sb.append("config.mc[i].di = cm.config[i].dataIndex;\n");
                     sb.append("config.mc[i].h = cm.config[i].hidden;\n");
                     sb.append("config.mc[i].w = cm.config[i].width;\n");
                     sb.append("}\n");
                     sb.append("return config;\n");
                     sb.append("},\n");
                     sb.append("applyState: function(state, config) {\n");
                     sb.append("if (state.mc != null) {\n");
                     sb.append("var cm = this.getColumnModel();\n");
                     sb.append("for (var i = 0; i < state.mc.length; i++) {\n");
                     sb.append("var colIndex = cm.findColumnIndex(state.mc[i].di);\n");
                     sb.append("if (colIndex != -1)\n");
                     sb.append("if (colIndex != i) {\n");
                     sb.append("cm.moveColumn(colIndex, i);\n");
                     sb.append("}\n");
                     sb.append("cm.setHidden(i, state.mc[i].h);\n");
                     sb.append("cm.setColumnWidth(i, state.mc[i].w);\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("if (state.sf && state.sd) {\n");
                     sb.append("this.store.sortInfo = { direction: state.sd, field: state.sf };\n");
                     sb.append("this.store.sortToggle[state.sf] = state.sd;\n");
                     sb.append("}\n");
                     sb.append("if (state.ss) {\n");
                     sb.append("this.getView().prevScrollState = state.ss;\n");
                     sb.append("}\n");
                     sb.append("},\n");
                     sb.append("store: store,\n");
                     sb.append("master_column_id : 'a0',\n");
                     sb.append("columns: [\n");
                     
                     int y = 0;
                     for (SectionField sectionFld : fields) {
                         sb.append("{header: '").append(sectionFld.getFieldDefinition().getDefaultLabel()).append("', ");
                         sb.append("dataIndex: 'a").append(y).append("', sortable: true");
 
                         String extType = ExtTypeHandler.findExtType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
                         if (ExtTypeHandler.EXT_FLOAT.equals(extType) || ExtTypeHandler.EXT_BOOLEAN.equals(extType) ||
                                 ExtTypeHandler.EXT_DATE.equals(extType) || ExtTypeHandler.EXT_STRING.equals(extType)) {
                             sb.append(", renderer: ");
                             if (ExtTypeHandler.EXT_DATE.equals(extType)) {
                                 sb.append("Ext.util.Format.dateRenderer('");
                                 if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                                     sb.append("m / Y");
                                 }
                                 else {
                                     sb.append("m-d-y g:ia");
                                 }
                                 sb.append("')\n");
                             }
                             else if (ExtTypeHandler.EXT_FLOAT.equals(extType)) {
                                 sb.append("OrangeLeap.amountRenderer\n");
                             }
                             else if (ExtTypeHandler.EXT_BOOLEAN.equals(extType)) {
                                 sb.append("OrangeLeap.booleanRenderer\n");
                             }
                             else {
                                 sb.append("function(value, metaData, record, rowIndex, colIndex, store) {");
                                 sb.append("return '<span ext:qtitle=\"").append(sectionFld.getFieldDefinition().getDefaultLabel()).append("\" ext:qwidth=\"250\" ext:qtip=\"' + value + '\">' + value + '</span>';");
                                 sb.append("}\n");
                             }
                         }
                         sb.append("}");
 
                         if (++y < fields.size()) {
                             sb.append(",\n");
                         }
                         else {
                             sb.append("\n");
                         }
                     }
                     sb.append("],\n");
                     sb.append("bbar: new Ext.ux.maximgb.tg.PagingToolbar({\n");
                     sb.append("store: store,\n");
                     sb.append("pageSize: 100,\n");
                     sb.append("stateEvents: ['change'],\n");
                     sb.append("stateId: '").append(entityType).append("PageBar',\n");
                     sb.append("stateful: true,\n");
                     sb.append("getState: function() {\n");
                     sb.append("var config = {};\n");
                     sb.append("config.start = this.cursor;\n");
                     sb.append("config.limit = this.pageSize;\n");
                     sb.append("var queryParams = OrangeLeap.getQueryParams();\n");
                     sb.append("if (queryParams) {\n");
                     sb.append("var thisConstituentId = queryParams['constituentId'];\n");
                     sb.append("if (thisConstituentId) {\n");
                     sb.append("config.constituentId = thisConstituentId;\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("return config;\n");
                     sb.append("},\n");
                     sb.append("applyState: function(state, config) {\n");
                     sb.append("var queryParams = OrangeLeap.getQueryParams();\n");
                     sb.append("if (queryParams) {\n");
                     sb.append("var thisConstituentId = queryParams['constituentId'];\n");
                     sb.append("if (!thisConstituentId || (thisConstituentId && (state.constituentId == thisConstituentId))) {\n");
                     sb.append("if (state.start) {\n");
                     sb.append("this.cursor = state.start;\n");
                     sb.append("}\n");
                     sb.append("if (state.limit) {\n");
                     sb.append("this.pageSize = state.limit;\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("},\n");
                     sb.append("displayInfo: true,\n");
                     sb.append("displayMsg: '").append(TangerineMessageAccessor.getMessage("displayMsg")).append("',\n");
                     sb.append("emptyMsg: '").append(TangerineMessageAccessor.getMessage("emptyMsg")).append("'\n");
                     sb.append("}),\n");
                     sb.append("sm: new Ext.grid.RowSelectionModel({singleSelect: true}),\n");
                     sb.append("viewConfig: { forceFit: true },\n");
                     sb.append("height: 600,\n");
                     sb.append("width: 760,\n");
                     sb.append("frame: true,\n");
                     sb.append("header: true,\n");
                     sb.append("title: '").append(TangerineMessageAccessor.getMessage(entityType)).append(" ").append(TangerineMessageAccessor.getMessage("list")).append("',\n");
                     sb.append("loadMask: true,\n");
                     sb.append("listeners: {\n");
                     sb.append("rowdblclick: function(grid, row, evt) {\n");
                     sb.append("var rec = grid.getSelectionModel().getSelected();\n");
                     sb.append("if (rec) {\n");
                     sb.append("Ext.get(document.body).mask('").append(TangerineMessageAccessor.getMessage("loadingRecord")).append("');\n");
                     sb.append("var entityArray = rec.id.split(\"-\");\n");
                     sb.append("var entityType = entityArray[0];\n");
                     sb.append("var entityId = entityArray[1];\n");
                     sb.append("if (\"").append(StringConstants.PAYMENT_SOURCE).append("\" == entityType) {\n");
                     sb.append("entityType = \"paymentManagerEdit\";\n");
                     sb.append("}\n");
                     sb.append("else if (\"").append(StringConstants.ADDRESS).append("\" == entityType || \"").append(StringConstants.PHONE).append("\" == entityType || \"").append(StringConstants.EMAIL).append("\" == entityType) {\n");
                     sb.append("entityType += \"ManagerEdit\";\n");
                     sb.append("}\n");
                     sb.append("window.location.href = entityType + \".htm?\" + entityType + \"Id=\" + entityId");
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                         sb.append(" + \"&constituentId=");
                         if (bw.isReadableProperty(StringConstants.CONSTITUENT)) {
                             sb.append(((Constituent) bw.getPropertyValue(StringConstants.CONSTITUENT)).getId());
                         }
                         else if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
                             sb.append(bw.getPropertyValue(StringConstants.CONSTITUENT_ID));
                         }
                         sb.append("\"");
                     }
                     sb.append(";\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("},\n");
                     sb.append("renderTo: '").append(entityType).append("Grid'\n");
                     sb.append("});\n");
                     sb.append("function doCallback(rec, options, success) {\n");
                     sb.append("var thisView = grid.getView();\n");
                     sb.append("if (thisView.prevScrollState) {\n");
                     sb.append("thisView.restoreScroll(thisView.prevScrollState);\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("store.load({params: { start: grid.getBottomToolbar().cursor, limit: grid.getBottomToolbar().pageSize, sort: store.sortInfo.field, dir: store.sortInfo.direction}, callback: doCallback });\n");
                     sb.append("}\n");
                     sb.append("return {\n");
                     sb.append("init: function() {\n");
                     sb.append("createGrid();\n");
                     sb.append("}\n");
                     sb.append("}\n");
                     sb.append("}();\n");
                     sb.append("Ext.onReady(function() {\n");
                     sb.append("Ext.QuickTips.init();\n");
                     sb.append("OrangeLeap.").append(entityType).append(".controller.init();\n");
                     sb.append("});\n");
                     sb.append("</script>");
                 }
 				else if (LayoutType.DISTRIBUTION_LINE_GRID.equals(sectionDef.getLayoutType()) ||
                         LayoutType.DISTRIBUTION_LINE_GRID_DISPLAY.equals(sectionDef.getLayoutType())) {
                     boolean showDeleteButton = LayoutType.DISTRIBUTION_LINE_GRID.equals(sectionDef.getLayoutType()); 
 
 					gridHandler.writeGridBegin(pageName, "DistributionLines", sb);
 					writeSectionHeader(sectionDef, "gridSectionHeader", sb);
 					gridHandler.writeGridTableBegin(sectionDef, "distributionLines", sb);
 
 					boolean hasHiddenGridRow = false;
 					SectionDefinition hiddenSectionDef = null;
 					List<SectionField> hiddenSectionFields = null;
 					int nextIndex = x + 1;
 					if (nextIndex < sectionDefinitions.size()) {
 						hasHiddenGridRow = LayoutType.GRID_HIDDEN_ROW.equals(sectionDefinitions.get(nextIndex).getLayoutType()); 
 
 						if (hasHiddenGridRow) {
 							hiddenSectionDef = sectionDefinitions.get(nextIndex);
 							hiddenSectionFields = pageCustomizationService.readSectionFieldsBySection(hiddenSectionDef);
 
 							x += 1; // skip the next section (the hidden grid row) because we will handle it now
 						}
 					}
 					gridHandler.writeGridCols(sectionFields, hasHiddenGridRow, showDeleteButton, sb);
 					gridHandler.writeGridHeader(pageContext, sectionFields, hasHiddenGridRow, showDeleteButton, sb);
 
                     if (LayoutType.DISTRIBUTION_LINE_GRID.equals(sectionDef.getLayoutType())) {
                         gridHandler.writeGridTableBody(pageContext, sectionDef, sectionFields,
                                 hiddenSectionDef, hiddenSectionFields,
                                 getTangerineForm(), hasHiddenGridRow, true, showDeleteButton, sb); // this is the DUMMY row
                     }
 
 					gridHandler.writeGridTableBody(pageContext, sectionDef, sectionFields,
 							hiddenSectionDef, hiddenSectionFields,
 							getTangerineForm(), hasHiddenGridRow, false, showDeleteButton, sb); // this are the real rows
 
 					gridHandler.writeGridTableEnd(sb);
 					gridHandler.writeGridActions(sectionDef.getLayoutType(), getTangerineForm(), sb);
 					gridHandler.writeGridEnd(sb);
 				}
 				else if (LayoutType.ADJUSTED_DISTRIBUTION_LINE_GRID.equals(sectionDef.getLayoutType())) {
 					gridHandler.writeGridBegin(pageName, "DistributionLines", sb);
 					writeSectionHeader(sectionDef, "gridSectionHeader", sb);
 					gridHandler.writeGridTableBegin(sectionDef, "distributionLines", sb);
 
 					boolean hasHiddenGridRow = false;
 					SectionDefinition hiddenSectionDef = null;
 					List<SectionField> hiddenSectionFields = null;
 					int nextIndex = x + 1;
 					if (nextIndex < sectionDefinitions.size()) {
 						hasHiddenGridRow = LayoutType.GRID_HIDDEN_ROW.equals(sectionDefinitions.get(nextIndex).getLayoutType());
 
 						if (hasHiddenGridRow) {
 							hiddenSectionDef = sectionDefinitions.get(nextIndex);
 							hiddenSectionFields = pageCustomizationService.readSectionFieldsBySection(hiddenSectionDef);
 
 							x += 1; // skip the next section (the hidden grid row) because we will handle it now
 						}
 					}
 					gridHandler.writeGridCols(sectionFields, hasHiddenGridRow, false, sb);
 					gridHandler.writeGridHeader(pageContext, sectionFields, hasHiddenGridRow, false, sb);
 
 					gridHandler.writeGridTableBody(pageContext, sectionDef, sectionFields,
 							hiddenSectionDef, hiddenSectionFields,
 							getTangerineForm(), hasHiddenGridRow, false, false, sb); // this are the real rows
 
 					gridHandler.writeGridTableEnd(sb);
 					gridHandler.writeGridActions(sectionDef.getLayoutType(), getTangerineForm(), sb);
 					gridHandler.writeGridEnd(sb);
 				}
 				else if (LayoutType.GIFT_IN_KIND_GRID.equals(sectionDef.getLayoutType())) {
 					gridHandler.writeGridBegin(pageName, "Details", sb);
 					writeSectionHeader(sectionDef, "gridSectionHeader", sb);
 					gridHandler.writeGridTableBegin(sectionDef, "giftInKindDetails", sb);
 
 					boolean hasHiddenGridRow = false;
 					SectionDefinition hiddenSectionDef = null;
 					List<SectionField> hiddenSectionFields = null;
 					int nextIndex = x + 1;
 					if (nextIndex < sectionDefinitions.size()) {
 						hasHiddenGridRow = LayoutType.GRID_HIDDEN_ROW.equals(sectionDefinitions.get(nextIndex).getLayoutType());
 
 						if (hasHiddenGridRow) {
 							hiddenSectionDef = sectionDefinitions.get(nextIndex);
 							hiddenSectionFields = pageCustomizationService.readSectionFieldsBySection(hiddenSectionDef);
 
 							x += 1; // skip the next section (the hidden grid row) because we will handle it now
 						}
 					}
 					gridHandler.writeGridCols(sectionFields, hasHiddenGridRow, true, sb);
 					gridHandler.writeGridHeader(pageContext, sectionFields, hasHiddenGridRow, true, sb);
 
 					gridHandler.writeGridTableBody(pageContext, sectionDef, sectionFields,
 							hiddenSectionDef, hiddenSectionFields,
 							getTangerineForm(), hasHiddenGridRow, true, true, sb); // this is the DUMMY row
 
 					gridHandler.writeGridTableBody(pageContext, sectionDef, sectionFields,
 							hiddenSectionDef, hiddenSectionFields,
 							getTangerineForm(), hasHiddenGridRow, false, true, sb); // this are the real rows
 
 					gridHandler.writeGridTableEnd(sb);
 					gridHandler.writeGridActions(sectionDef.getLayoutType(), getTangerineForm(), sb);
 					gridHandler.writeGridEnd(sb);
 				}
 				println(sb);
 			}
 		}
 	}
 
 	protected void writeSectionHeader(SectionDefinition sectionDef, String headerClass, StringBuilder sb) {
 		sb.append("<h4 class=\"").append(headerClass).append("\">").append(getSectionHeader(sectionDef)).append("</h4>");
 	}
 
 	protected String getSectionHeader(SectionDefinition sectionDef) {
 		String messageValue = messageService.lookupMessage(MessageResourceType.SECTION_HEADER, sectionDef.getSectionName(), pageContext.getRequest().getLocale());
 		if (!StringUtils.hasText(messageValue)) {
 		    messageValue = sectionDef.getDefaultLabel();
 		}
 		return messageValue;
 	}
 
 	protected void writeColumnsStart(SectionDefinition sectionDef, StringBuilder sb) {
 		sb.append("<div class=\"columns ");
 		if (LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			sb.append(sectionDef.getSectionHtmlName());
 		}
 		sb.append("\" ");
 		if (LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			sb.append(" id=\"").append(sectionDef.getSectionHtmlName()).append("\"");
 		}
 		sb.append(">");
 		if (LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			writeSectionHeader(sectionDef, "formSectionHeader", sb);
 		}
 	}
 
 	protected void writeColumnsEnd(StringBuilder sb) {
 		sb.append("<div class=\"clearColumns\"></div></div>");
 	}
 
 	protected void writeSingleColumnStart(SectionDefinition sectionDef, StringBuilder sb) {
 		sb.append("<div class=\"column singleColumn ");
 		if ( ! LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			sb.append(sectionDef.getSectionHtmlName());
 		}
 		sb.append("\" ");
 		if (LayoutType.ONE_COLUMN_HIDDEN.equals(sectionDef.getLayoutType())) {
 			sb.append("style=\"display:none\" ");
 		}
 		if ( ! LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			sb.append("id=\"").append(sectionDef.getSectionHtmlName()).append("\"");
 		}
 		sb.append(">");
 		if (LayoutType.ONE_COLUMN.equals(sectionDef.getLayoutType()) || LayoutType.ONE_COLUMN_HIDDEN.equals(sectionDef.getLayoutType())) {
 			writeSectionHeader(sectionDef, "formSectionHeader", sb);
 		}
 		sb.append("<ul class=\"formFields width385\">");
 	}
 
 	protected void writeSingleColumnEnd(StringBuilder sb) {
 		sb.append("<li class=\"clear\"/></ul></div>");
 	}
 
 	protected void writeSectionField(SectionDefinition sectionDef, List<SectionField> sectionFields, StringBuilder sb) {
 		writeSectionField(sectionDef, sectionFields, sb, false);
 	}
 
 	protected void writeSectionField(SectionDefinition sectionDef, List<SectionField> sectionFields, StringBuilder sb, boolean firstColumn) {
 		Map<String, List<SectionField>> groupedSectionFields = fieldService.groupSectionFields(sectionFields);
 		List<SectionField> hiddenFields = groupedSectionFields.get(StringConstants.HIDDEN);
 
 		/* Display the hidden fields in the first column ONLY */
 		if (firstColumn) {
 			for (SectionField hiddenFld : hiddenFields) {
 				FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(hiddenFld.getFieldType());
 				if (fieldHandler != null) {
 					fieldHandler.handleField(pageContext, sectionDef, hiddenFields, hiddenFld,
 							getTangerineForm(), sb);
 				}
 			}
 		}
 
 		List<SectionField> displayedFields = groupedSectionFields.get(StringConstants.DISPLAYED);
 
 		int begin = 0;
 		int end = displayedFields.size();
 
 		if (LayoutType.TWO_COLUMN.equals(sectionDef.getLayoutType())) {
 			int split = (int) Math.ceil(((float)displayedFields.size()) / ((float)2));
 			if (firstColumn) {
 				end = split;
 			}
 			else {
 				begin = split;
 			}
 		}
 		for (int x = begin; x < end; x++) {
 			SectionField displayFld = displayedFields.get(x);
 			FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(displayFld.getFieldType());
 			if (fieldHandler != null) {
 				fieldHandler.handleField(pageContext, sectionDef, displayedFields, displayFld, getTangerineForm(), sb);
 			}
 		}
 	}
 
     public static String getInitDirection(List<SectionField> fields) {
         return FieldType.DATE.equals(fields.get(0).getFieldType()) ||
                 FieldType.DATE_TIME.equals(fields.get(0).getFieldType()) ||
                 FieldType.DATE_DISPLAY.equals(fields.get(0).getFieldType()) ? "DESC" : "ASC";
     }
 
 	private TangerineForm getTangerineForm() {
 		return (TangerineForm) getRequestAttribute(StringConstants.FORM);
 	}
 }
