 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.renderkit.html;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.javascript.JSFunction;
 import org.ajax4jsf.model.DataVisitor;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.ajax4jsf.renderkit.RendererBase;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.resource.InternetResource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.richfaces.component.UIScrollableDataTable;
 import org.richfaces.component.util.ColumnUtil;
 import org.richfaces.event.scroll.ScrollEvent;
 import org.richfaces.event.sort.SortEvent;
 import org.richfaces.model.SortField;
 import org.richfaces.model.SortOrder;
 import org.richfaces.renderkit.CompositeRenderer;
 import org.richfaces.renderkit.RendererContributor;
 import org.richfaces.renderkit.ScriptOptions;
 import org.richfaces.utils.TemplateLoader;
 
 
 
 /**
  * @author Anton Belevich
  *
  */
 
 public abstract class ScrollableDataTableBaseRenderer extends HeaderResourcesRendererBase {
 	
 	private class CompositeRendererEnabler extends CompositeRenderer {
 		public CompositeRendererEnabler() {
 			addContributor(new SelectionRendererContributor());
 		}
 		@Override
 		protected Class<? extends UIComponent> getComponentClass() {
 			return ScrollableDataTableBaseRenderer.this.getComponentClass();
 		}
 		
 		@Override
 		public void mergeScriptOptions(ScriptOptions scriptOptions,
 				FacesContext context, UIComponent component) {
 			super.mergeScriptOptions(scriptOptions, context, component);
 	
 		}
 		
 		@Override
 		public String getScriptContributions(String varString,
 				FacesContext context, UIComponent component) {
 			return super.getScriptContributions(varString, context, component);
 		}
 		
 		@Override
 		public RendererContributor[] getContributors() {
 			return super.getContributors();
 		}
 
 		@Override
 		public InternetResource[] getScripts() {
 			return super.getScripts();
 		}
 		
 		@Override
 		public InternetResource[] getStyles() {
 			return super.getStyles();
 		}
 	}
 	
 	public static final String PARTIAL_UPDATE = "partialUpdate";
 	public static final String UPDATE_HEADER = "updateHeader";
 	
 	public static final String  FOOTER_PART = "footer";
 	
 	public static final String  HEADER_PART = "header";
 	
 	private static final String COLUMN_FROZEN_TYPE = "frozen"; 
 	
 	private static final String COLUMN_NORMAL_TYPE = "normal";
 	
 	private static final String PERSENTAGE_SUPPORT_ERROR_MSG = "columnsWidth property: Percentage values are not supported";
 	
 	private RendererBase cellTemplate = null;
 	
 	private RendererBase headerCellTemplate = null;
 	
 	private RendererBase footerCellTemplate = null;
 	
 	private RendererBase headerItselfTemplate = null;
 	
 	private CompositeRendererEnabler composite = new CompositeRendererEnabler();
 
 	private final Log log = LogFactory.getLog(ScrollableDataTableBaseRenderer.class);
 	
 	private final ColumnVisitor columnsWidthCounter = new ColumnVisitor(){
 
 		public int visit(FacesContext context, UIComponent column, ResponseWriter writer, ScrollableDataTableRendererState state) throws IOException {
 			
 			int prevWidth = 0;
 			int width = 0;
 						
 			
 			String widthPx = getColumnWidth(column);
 			widthPx = getFormattedWidth(widthPx);
 			prevWidth = state.getSumWidth();
 			width = prevWidth + Integer.parseInt(widthPx); 
 			state.setSumWidth(width);
 			
 			return 1;
 		}
 	};
 		
 	private final ColumnVisitor headerCellRenderer = new ExtendedColumnVisitor(){
 
 		public void renderContent(FacesContext context, UIComponent column, ResponseWriter writer, ScrollableDataTableRendererState state) throws IOException {
 			
 			
 			int cell_index = state.getCellIndex();
 			String client_id = state.getClientId();
 			state.addId(column.getId());			
 			headerCellTemplate = getHeaderCellTemplate();
 			
 			ComponentVariables variables = 
 				ComponentsVariableResolver.getVariables(headerCellTemplate, column);
 			String widthPx = getColumnWidth(column);
 			
 			widthPx = getFormattedWidth(widthPx);
 			int width = Integer.parseInt(widthPx);
 			
 			int sepOffset = 0;
 			
 			if(state.isFrozenColumn()){
 				sepOffset = state.getSepOffset().intValue() + width;
 			}else{
 				sepOffset = state.getSepOffset().intValue() + width + 1;
 				state.setSepOffset(new Integer(sepOffset));
 			}	
 			
 			
 			
 			variables.setVariable("client_id", client_id);
 			variables.setVariable("cell_index", new Integer(cell_index));
 			variables.setVariable("sepOffset", new Integer(sepOffset));
 			variables.setVariable("headerColumnClass", state.getColumnClass());
 			Boolean sorting = getColumnSorting(state.getGrid(), column.getId());
 			if (sorting != null) {
 				if (sorting.booleanValue()) {
 					variables.setVariable("headerColumnSortClass", "rich-sdt-header-sort-up");
 				} else {
 					variables.setVariable("headerColumnSortClass", "rich-sdt-header-sort-down");
 				}
 			}
 			//variables.setVariable("headerCellClass", state.get);
 			
 			headerCellTemplate.encodeBegin(context, column);
 			headerRenderer.visit(context, column, writer, state);
 			headerCellTemplate.encodeEnd(context, column);
 		}
 	};
 
 	private final ColumnVisitor styleRenderer = new ColumnVisitor(){
 
 		public int visit(FacesContext context, UIComponent column, ResponseWriter writer, ScrollableDataTableRendererState state) throws IOException {
 			
 			int cell_index = state.getCellIndex();
 			String widthPx = getColumnWidth(column);
 			widthPx = getFormattedWidth(widthPx);
 			int width = Integer.parseInt(widthPx);
 			
 			writer.writeText("#" + getNormalizedId(context, state.getGrid())+ " .rich-sdt-c-" + cell_index + " {", "width");
 			writer.writeText("width: " + width + "px;", "width");
 			writer.writeText("}", "width");			
 			return 0;
 		}
 	};
 	
 	private final ColumnVisitor headerRenderer = new ColumnVisitor() {
 		
 		public int visit(FacesContext context, UIComponent column,
 				ResponseWriter writer, ScrollableDataTableRendererState state)
 				throws IOException {
 			
 			ComponentVariables variables = 
 				ComponentsVariableResolver.getVariables(getHeaderItselfTemplate(), column); 
 			
 			int cell_index = state.getCellIndex();
 			String client_id = state.getClientId();
 			Boolean sorting = getColumnSorting(state.getGrid(), column.getId());
 			
 			if (sorting != null) {
 				if (sorting.booleanValue()) {
 					variables.setVariable("sortAscending", Boolean.TRUE);
 				} else {
 					variables.setVariable("sortDescending", Boolean.TRUE);
 				}
 			}
 			variables.setVariable("client_id", client_id);
 			variables.setVariable("cell_index", new Integer(cell_index));
 			
 			getHeaderItselfTemplate().encodeBegin(context, column);
 			
 			UIComponent header = column.getFacet(HEADER_PART);
 			
 			if(header != null){
 				renderChild(context, header);
 			}
 			getHeaderItselfTemplate().encodeEnd(context, column);
 			
 			return 0;
 		}
 		
 		
 		
 	};
 	
 	private final ColumnVisitor footerCellRenderer = new ExtendedColumnVisitor(){
 
 		public void renderContent(FacesContext context, UIComponent column, ResponseWriter writer, ScrollableDataTableRendererState state) throws IOException {
 			
 			int cell_index = state.getCellIndex();
 			String client_id = state.getClientId();
 			
 			footerCellTemplate = getFooterCellTemplate();
 			
 			ComponentVariables variables = ComponentsVariableResolver.getVariables(footerCellTemplate, column);
 			variables.setVariable("client_id", client_id);
 			variables.setVariable("cell_index", new Integer(cell_index));
 			variables.setVariable("footerColumnClass", state.getColumnClass());
 			
 			Boolean sorting = getColumnSorting(state.getGrid(), column.getId());
 			if (sorting != null) {
 				if (sorting.booleanValue()) {
 					variables.setVariable("footerColumnSortClass", "rich-sdt-footer-sort-up");
 				} else {
 					variables.setVariable("footerColumnSortClass", "rich-sdt-footer-sort-down");
 				}
 			}
 			
 			UIComponent component = column.getFacet(FOOTER_PART);
 			if(component != null){
 				footerCellTemplate.encodeBegin(context, column);
 				renderChild(context, component);
 				footerCellTemplate.encodeEnd(context, column);
 			} 	
 		}
 	};
 	
 	private final ColumnVisitor cellRenderer = new ExtendedColumnVisitor(){
 
 		public void renderContent(FacesContext context, UIComponent column, ResponseWriter writer, ScrollableDataTableRendererState state) throws IOException {
 			
 			String cell_id = state.getRowIndex()+ "_" + state.getCellIndex();
 			if (log.isTraceEnabled()) {
 				log.trace("cell_index: "  + cell_id);
 			}
 			
 			String client_id = state.getClientId();
 			int cell_index =  state.getCellIndex();
 			cellTemplate = getCellTemplate();
 			
 			ComponentVariables variables = ComponentsVariableResolver.getVariables(cellTemplate, column);
 			variables.setVariable("cell_id",cell_id);
 			variables.setVariable("client_id", client_id);
 			variables.setVariable("cell_index", new Integer(cell_index));
 			variables.setVariable("columnClass", state.getColumnClass());
 			Boolean sorting = getColumnSorting(state.getGrid(), column.getId());
 			if (sorting != null) {
 				if (sorting.booleanValue()) {
 					variables.setVariable("columnSortClass", "rich-sdt-column-sort-up");
 				} else {
 					variables.setVariable("columnSortClass", "rich-sdt-column-sort-down");
 				}
 			}
 					
 			cellTemplate.encodeBegin(context, column);
 			if(!state.isFake()) {
 				renderChildren(context, column);
 			}
 			cellTemplate.encodeEnd(context, column);
 		}
 		
 	};
 	
 	
 	private final DataVisitor rowsRenderer = new DataVisitor(){
 
 		public void process(FacesContext context, Object rowKey, Object argument) throws IOException {
 			
 			ScrollableDataTableRendererState state = (ScrollableDataTableRendererState)argument;
 			UIScrollableDataTable  grid = state.getGrid();
 			
 			grid.setRowKey(rowKey);
 						
 			if(grid.isRowAvailable() || state.isFake()){
 				String row_id;
 				
 				int index = state.getRowIndex();
 				
 				//state.setRowIndex(index);
 				
 				String baseClientId = grid.getBaseClientId(context);
 				
 				if(state.isFrozenPart()){
 					row_id = baseClientId + ":f:" + index;
 				}else{
 					row_id = baseClientId + ":n:" + index;
 				}
 				
 				ResponseWriter writer = context.getResponseWriter();
 				writer.startElement(HTML.TR_ELEMENT, grid);
 				state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 				getUtils().writeAttribute(writer, "id",row_id);
 				getUtils().writeAttribute(writer, "class","rich-sdt-row " + state.getRowClass());
 				addRowJavascriptEvents(writer, grid);
 				if (log.isDebugEnabled()) {
 					log.debug("rowIndex : " + index);
 				}
 				
 				ColumnWalker.iterateOverColumns(context, grid, cellRenderer, writer, state);
 				if(!state.isFrozenPart()){
 					writer.startElement("td", grid);
 					getUtils().writeAttribute(writer, "class","rich-sdt-c-f rich-sdt-column-cell " + state.getColumnClass(state.getCellIndex()));
 					writer.startElement(HTML.DIV_ELEM, grid);
 					getUtils().writeAttribute(writer, "class","rich-sdt-column-cell-body");
 					writer.endElement(HTML.DIV_ELEM);
 					writer.endElement("td");
 				}
 				writer.endElement(HTML.TR_ELEMENT);
 				state.nextRow();
 				state.setCellIndex(0);
 				
 			}	
 		}
 	};
 		
 	private final DataVisitor ajaxRowsRenderer = new DataVisitor(){
 
 		
 		public void process(FacesContext context, Object rowKey, Object argument) throws IOException {
 			
 			int columnsCount = 0;
 			
 			boolean frozenTRRendered = false;
 			
 			boolean normalTRRendered = false;
 			
 			ScrollableDataTableRendererState state = (ScrollableDataTableRendererState)argument;
 			AjaxContext ajaxContext = state.getAjaxContext();
 			ajaxContext.getResponseData();
 			
 			UIScrollableDataTable  grid = state.getGrid();
 			Collection<String> collection = grid.getResponseData();
 
 			grid.setRowKey(rowKey);
 			ResponseWriter writer = context.getResponseWriter();
 			state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 			
 			String row_id = null;
 			
 			String baseClientId = grid.getBaseClientId(context);
 			
 			for (Iterator<UIComponent> iter = grid.getChildren().iterator(); iter.hasNext(); ) {
 				UIComponent kid = (UIComponent) iter.next();
 				
 				if (kid.isRendered()) {
 					
 					if (kid instanceof UIColumn){						
 						if(state.isFrozenColumn() && !frozenTRRendered && state.getFrozenColumnCount() > 0){
 							
 							state.setFrozenPart(true);
 							frozenTRRendered = true;
 							
 							row_id = baseClientId + ":f:" +  state.getRowIndex();
 							writer.startElement("tr", grid);
 							getUtils().writeAttribute(writer, "class","rich-sdt-row " + state.getRowClass());
 							getUtils().writeAttribute(writer,"id",row_id);
 							addRowJavascriptEvents(writer, grid);
 							collection.add(row_id);
 																							
 						}else if(!state.isFrozenColumn() && !normalTRRendered){
 							
 							writeNormalTr(frozenTRRendered, state, grid, collection,
 									writer, baseClientId);
 							normalTRRendered = true;
 									
 						}
 
 						columnsCount += cellRenderer.visit(context, kid, writer, state);
 //						columnsCount += cellRenderer.visit(context, column, writer, state);
 
 
 						
 						
 						state.nextCell();
 					
 					}
 				
 				}
 			
 			}
 			if(!normalTRRendered){
 				writeNormalTr(frozenTRRendered, state, grid, collection,
 						writer, baseClientId);				
 			}
 			writer.startElement("td", grid);
 			getUtils().writeAttribute(writer, "class","rich-sdt-c-f rich-sdt-column-cell " + state.getColumnClass(state.getCellIndex()));
 			writer.startElement(HTML.DIV_ELEM, grid);
 			getUtils().writeAttribute(writer, "class","rich-sdt-column-cell-body");
 			writer.endElement(HTML.DIV_ELEM);
 			writer.endElement("td");
 			writer.endElement("tr");
 			state.setCellIndex(0);
 			state.nextRow();	
 		}
 
 		private void writeNormalTr(boolean frozenTRRendered,
 				ScrollableDataTableRendererState state,
 				UIScrollableDataTable grid, Collection<String> collection,
 				ResponseWriter writer, String baseClientId) throws IOException {
 			String row_id;
 			if(frozenTRRendered){
 				writer.endElement("tr");
 			}
 			
 			state.setFrozenPart(false);
 			row_id = baseClientId + ":n:" +  state.getRowIndex();
 			
 			writer.startElement("tr", grid);
 			getUtils().writeAttribute(writer,"id",row_id);
 			getUtils().writeAttribute(writer, "class","rich-sdt-row " + state.getRowClass());
 			addRowJavascriptEvents(writer, grid);
 			collection.add(row_id);
 		}
 	};
 
 	@Override
 	protected InternetResource[] getScripts() {
 		return composite.getScripts();
 	}
 	
 	@Override
 	protected InternetResource[] getStyles() {
 		return composite.getStyles();
 	}
 	
 	// temporary solution RF-957
 	public String getFormattedWidth(String receivedWidth ) throws IOException{
 		
 		String formattedWidth = receivedWidth;
 				
 		if(formattedWidth.endsWith("%")){
 			throw new FacesException(PERSENTAGE_SUPPORT_ERROR_MSG); 
 		}else if(formattedWidth.endsWith("px")){
 			formattedWidth = formattedWidth.substring(0,formattedWidth.indexOf("px"));
 		}
 	
 		return  formattedWidth;
 	}
 		
 	@Override
 	protected Class<? extends UIComponent> getComponentClass() {
 		return UIScrollableDataTable.class;
 	}
 	
 	public static String getJavaScriptVarName(FacesContext context, UIScrollableDataTable grid) {
 		
 		String name = grid.getScriptVar();
 		if (name == null) {
 			String id = grid.getBaseClientId(context);
 			name = "Richfaces_ScrollableGrid_" + id.replaceAll("[^A-Za-z0-9_]", "_");
 		}
 		
 		return "window." + name;
 	}
 	
 	public static String getNormalizedId(FacesContext context, UIScrollableDataTable grid) {
 		return grid.getBaseClientId(context).replaceAll("[^A-Za-z0-9_]", "_");
 	}
 	
 	public String createClientScrollableGrid(FacesContext context, UIScrollableDataTable grid) {
 		
 		ScrollableDataTableOptions  options = new ScrollableDataTableOptions(grid);
 		
 		options.addOption("normalizedId", getNormalizedId(context, grid));
 		
 		composite.mergeScriptOptions(options, context, grid);
 		
 		JSFunction function = new JSFunction("new ClientUI.controls.grid.ScrollableGrid");
 		function.addParameter(options);
 		return function.toScript();
 	}
 	
 	protected String getScriptContributions(FacesContext context, UIScrollableDataTable grid) {
 		return composite.getScriptContributions(getJavaScriptVarName(context, grid), context, grid);
 	}
 	
 	public boolean getRendersChildren() {
 		return true;
 	}
 	
 	public void renderGridBody(FacesContext context, UIScrollableDataTable grid, boolean isFrozen) throws IOException{
 		
 		final ScrollableDataTableRendererState state = ScrollableDataTableRendererState.getRendererState(context);
 		
 		if(isFrozen){
 			state.setColumType(COLUMN_FROZEN_TYPE);
 		}else{
 			state.setColumType(COLUMN_NORMAL_TYPE);
 		}
 		
 		if (log.isTraceEnabled()) {
 			log.trace("ScrollableDataTableBaseRenderer.renderGridBody(context, grid, isFrozen)");
 		}
 		
 		state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 		state.setFrozenPart(isFrozen);
 		state.setClientId(grid.getClientId(context));
 		
 		if (!isFrozen || state.getFrozenColumnCount() > 0) {
 			grid.walk(context, rowsRenderer, state);
 			int fakeRowsCount = grid.getRows() - grid.getRowCount();
 			state.setFake(true);
 			for (int i = 0; i < fakeRowsCount; i++) {
 				rowsRenderer.process(context, null, state);
 			}
 			state.setFake(false);
 		}
 		state.setRowIndex(ScrollableDataTableUtils.getClientRowIndex(grid));
 		grid.setRowKey(null);
 	}
 	
 	public void renderHeaders(FacesContext context, UIScrollableDataTable grid, boolean isFrozen)throws IOException{
 
 		ResponseWriter writer = context.getResponseWriter();
 		final ScrollableDataTableRendererState state = ScrollableDataTableRendererState.getRendererState(context);
 		
 		if(isFrozen){
 			state.setColumType(COLUMN_FROZEN_TYPE);
 		}else{
 			state.setColumType(COLUMN_NORMAL_TYPE);
 		}
 		
 		state.setClientId(grid.getClientId(context));
 		state.setPart(HEADER_PART);
 		state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 		state.setFrozenPart(isFrozen);
 		state.setClientId(grid.getClientId(context));
 		state.setSepOffset(new Integer(0));
 		ColumnWalker.iterateOverColumns(context, grid, headerCellRenderer, writer, state);
 	}
 	
 	public void renderStyle(FacesContext context, UIScrollableDataTable grid) throws IOException {
 		ScrollableDataTableRendererState state = ScrollableDataTableRendererState.getRendererState(context);
 		ColumnWalker.iterateOverColumns(context, grid, styleRenderer, context.getResponseWriter(), state);
 		ResponseWriter writer = context.getResponseWriter();
 		writer.writeText("#" + getNormalizedId(context, state.getGrid()) + " .rich-sdt-c-f {", "width");
 		writer.writeText("width: 0px;", "width");
 		writer.writeText("}", "width");			
 	}
 	
 	public void renderFooters(FacesContext context, UIScrollableDataTable grid, boolean isFrozen) throws IOException{
 			
 		ResponseWriter writer = context.getResponseWriter();
 		final ScrollableDataTableRendererState state = ScrollableDataTableRendererState.getRendererState(context);
 		
 		if(isFrozen){
 			state.setColumType(COLUMN_FROZEN_TYPE);
 		}else{
 			state.setColumType(COLUMN_NORMAL_TYPE);
 		}
 
 		state.setClientId(grid.getClientId(context));
 		state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 		state.setFrozenPart(isFrozen);
 		int colsCount = ColumnWalker.iterateOverColumns(context, grid, footerCellRenderer, writer, state);
 		int rowsCount = grid.getRowCount();
 		
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, grid);
 		variables.setVariable("rows_count", new Integer(rowsCount));
 		variables.setVariable("columns_count", new Integer(colsCount));
 		
 	}
 	
 	public void setUpState(FacesContext context, UIScrollableDataTable grid) {
 		ScrollableDataTableRendererState state = ScrollableDataTableRendererState.createState(context, grid);
 		state.setRowIndex(ScrollableDataTableUtils.getClientRowIndex(grid));
 	}
 			
 	public void tearDownState(FacesContext context, UIScrollableDataTable grid){
 		ScrollableDataTableRendererState.restoreState(context);
 	}
 	
 	public String getRowsAjaxUpdate(FacesContext context, UIScrollableDataTable grid){
 		
 		JSFunction function = AjaxRendererUtils.buildAjaxFunction(grid, context);
 		Map<String, Object> options = AjaxRendererUtils.buildEventOptions(context, grid);
 		options.put("oncomplete", AjaxFunctionBuilder.getOnComplete(context, grid, AjaxFunctionBuilder.SCROLL));
 
 		@SuppressWarnings("unchecked")
 		Map<String, Object> parametersMap = (Map<String, Object>) options.get("parameters");
 		
 		parametersMap.put(grid.getBaseClientId(context) + ":scroll", "");
 		function.addParameter(options);
 		String completeFunction = function.toScript()+"; return false;";
 		
 		return completeFunction;
 		
 	}
 
 	@Override
 	protected void doDecode(FacesContext context, UIComponent component) {
 		
 		super.doDecode(context, component);
 		
 
 		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
 		
 		if(component instanceof UIScrollableDataTable){
 		
 			UIScrollableDataTable grid = (UIScrollableDataTable)component;
 			ExternalContext externalContext = context.getExternalContext();
 			String clientId = grid.getClientId(context);
 			boolean sorted = false;	
 			Map<String, String> parameters = externalContext.getRequestParameterMap();
 			
 			String s_id = clientId + ":si";
 			grid.resetReqRowsCount();
 			String firstString = null;
 			if(parameters.containsKey(s_id)){
 				String options = (String)parameters.get(s_id);
 				grid.setScrollPos(options);
 				if(options.length() > 0){
 					String[] si = options.split(",");
 					firstString = si[1];
 					if (si.length >= 5) {
 						grid.setFirst(Integer.parseInt(si[4]));
 					} else {
 						grid.setFirst(Integer.parseInt(firstString));
 					}
 					component.getAttributes().put(ScrollableDataTableUtils.CLIENT_ROW_KEY, Integer.parseInt(si[3]));			
 				}
 			}
 					
 			composite.decode(context, component);
 			
 			if (firstString != null) {
 				grid.setFirst(Integer.parseInt(firstString));
 			}
 			
 			if(parameters.containsKey(clientId + ":sortColumn") &&
 			   parameters.containsKey(clientId + ":sortStartRow") && 
 			   parameters.containsKey(clientId + ":sortIndex")){ 
 				
 				String sortColumn = (String)parameters.get(clientId + ":sortColumn");
 				int sortDataIndex = Integer.parseInt((String)parameters.get(clientId + ":sortIndex"));
 				Integer sortStartRow = Integer.valueOf((String)parameters.get(clientId + ":sortStartRow"));
 				
 				String sortOrderString = 
 					(String) parameters.get(clientId + ":sortOrder");
 				
 				Boolean so = null;
 				
 				if (sortOrderString != null && sortOrderString.length() > 0 ) {
 					sortOrderString = sortOrderString.toLowerCase();
 					
 					if (sortOrderString.startsWith("a")) {
 						so = Boolean.TRUE;
 					} else if (sortOrderString.startsWith("d")){
 						so = Boolean.FALSE;
 					}
 				}
 				
 				
 				UIComponent column = grid.findComponent(sortColumn);
 				
 				if(ColumnUtil.isSortable(column)){
 			
 					sorted = true;
 					SortEvent sortEvent = new SortEvent(grid,sortColumn, grid.getRows(), sortDataIndex);
 					
 					sortEvent.setProposedOrder(so);
 					
 					sortEvent.setAttribute(ScrollableDataTableUtils.CLIENT_ROW_KEY,sortStartRow);
 					
 					if (ajaxContext.isAjaxRequest()) {
 						sortEvent.setAttribute(PARTIAL_UPDATE, Boolean.TRUE);
 						sortEvent.setAttribute(UPDATE_HEADER, Boolean.TRUE);
 					}
 					
 					
 					sortEvent.queue();
 				}	
 			}
 			
 			
 			if(parameters.containsKey(clientId + ":scroll") && !sorted){
 					
 				String submitedState = (String)parameters.get(clientId + "_state_input");
 				if (submitedState != null) {
 					boolean isEmpty = true;
 					
 					
 					String [] values = submitedState.split(",");
 					for (int i = 0; i < values.length; i++) {
 						isEmpty = isEmpty && values[i].equals(""); 
 					}
 					
 					int rows = 0;
 					int first = 0;
 					
 					if(!isEmpty){
 						rows = Integer.parseInt(values[0]);
 						first = Integer.parseInt(values[1]);
 						ScrollEvent scrollEvent = new ScrollEvent(grid,rows,first);
 
 						scrollEvent.setAttribute(ScrollableDataTableUtils.CLIENT_ROW_KEY,Integer.valueOf(values[2]));						
 						
 						if (ajaxContext.isAjaxRequest()) {
 							scrollEvent.setAttribute(PARTIAL_UPDATE, Boolean.TRUE);
 						}
 
 						scrollEvent.queue();
 						
 					}
 				}
 			}
 			
 			
 		}
 		
 		
 	}
 	
 	public void renderAjaxChildren(FacesContext context, UIComponent component)throws IOException{
 		
 		UIScrollableDataTable grid = (UIScrollableDataTable)component;
 		
 		
 		
 		ScrollableDataTableRendererState state = ScrollableDataTableRendererState.createState(context, grid);
 				
 		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
 		String client_id = grid.getClientId(context);
 		
 		state.setClientId(client_id);
 		state.setAjaxContext(ajaxContext);
 		state.setRowIndex(ScrollableDataTableUtils.getClientRowIndex(grid));
 		
 		if (log.isDebugEnabled()) {
 			log.debug("ScrollableDataTableBaseRenderer.renderAjaxChildren()");
 		}
 		
 		grid.getResponseData().clear();
 		
 		ResponseWriter writer = context.getResponseWriter();
 		writer.startElement("table", grid);
 		writer.startElement("tbody", grid);
 		
 		grid.walk(context, ajaxRowsRenderer, state);
 		int fakeRowsCount = grid.getRows() - grid.getRowCount();
 		ScrollableDataTableRendererState.restoreState(context);
 		grid.setRowKey(null);
 		state.setFake(true);
 		for (int i = 0; i < fakeRowsCount; i++) {
 			ajaxRowsRenderer.process(context, null, state);	
 		}
 		
 		state.setFake(false);
 		
 		writer.endElement("tbody");
 		writer.endElement("table");
 		String id = client_id+"_rows_input";
 		writer.startElement(HTML.INPUT_ELEM, grid);
 		writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
		writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
 		writer.writeAttribute(HTML.id_ATTRIBUTE, id, null);
 		writer.writeAttribute(HTML.NAME_ATTRIBUTE, id, null);
 		writer.writeAttribute(HTML.value_ATTRIBUTE, grid.getRowCount(), null);
 		writer.endElement(HTML.INPUT_ELEM);
 		ajaxContext.addRenderedArea(id);
 
 		renderHiddenScrollInput(context, grid);
 		ajaxContext.addRenderedArea(client_id+":si");
 		
 		ajaxContext.setResponseData(grid.getResponseData());
 		
 		ajaxContext.getAjaxRenderedAreas().remove(grid.getClientId(context));
 		ScrollableDataTableScrollData options = createOptions(grid);
 		ajaxContext.getResponseDataMap().put("options", options);
 		
 		//Then call contributors to write additional HTML content
 		contributorsEncodeHere(context, grid);
 	
 		if (shouldUpdateHeader(component)) {
 			ColumnWalker.iterateOverColumns(context, component, headerRenderer, writer, new ScrollableDataTableRendererState(context, null, grid));
 		}
 	}
 	
 	private ScrollableDataTableScrollData createOptions(UIScrollableDataTable grid){
 		
 		int index = grid.getFirst();
 		int startRow = ScrollableDataTableUtils.getClientRowIndex(grid);
 		int count = grid.getRows( )== 0 ? grid.getRowCount() : grid.getRows();
 		
 		ScrollableDataTableScrollData options = new ScrollableDataTableScrollData(index, startRow, count);
 				
 		return options;
 	}
 	
 	private boolean onlyPartialUpdateNeeded(UIComponent grid) {
 		Boolean b = (Boolean) grid.getAttributes().get(PARTIAL_UPDATE);
 		return b != null && b.booleanValue() && AjaxContext.getCurrentInstance().isAjaxRequest();
 	}
 
 	private boolean shouldUpdateHeader(UIComponent grid) {
 		Boolean b = (Boolean) grid.getAttributes().get(UPDATE_HEADER);
 		return b != null && b.booleanValue() && AjaxContext.getCurrentInstance().isAjaxRequest();
 	}
 	
 	@Override
 	public void encodeChildren(FacesContext context, UIComponent component
 								 )	throws IOException {
 		
 		if(onlyPartialUpdateNeeded(component)){
 			renderAjaxChildren(context, component);
 		}else{
 			super.encodeChildren(context, component);
 		}
 		
 	}
 	
 	@Override
 	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
 		
 		if(component instanceof UIScrollableDataTable){
 			UIScrollableDataTable grid = (UIScrollableDataTable)component;
 			setUpState(context, grid);
 		}
 		
 		if(!onlyPartialUpdateNeeded(component)){
 			super.encodeBegin(context, component);
 		}
 	}
 	
 	@Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		
 		if(component instanceof UIScrollableDataTable){
 			UIScrollableDataTable table = (UIScrollableDataTable)component;
 			if(!onlyPartialUpdateNeeded(table)){
 				Object rowKey = table.getRowKey();
 				table.setRowKey(context, null);
 				super.encodeEnd(context, table);
 				table.setRowKey(context, rowKey);
 			}	
 			
 			component.getAttributes().remove(PARTIAL_UPDATE);
 			component.getAttributes().remove(UPDATE_HEADER);
 		}
 	}
 	
 	public void setUpColumnsWidth(FacesContext context, UIScrollableDataTable grid) throws IOException{
 		ScrollableDataTableRendererState state = ScrollableDataTableRendererState.getRendererState(context);
 		state.setFrozenColumnCount(ScrollableDataTableUtils.getFrozenColumnsCount(grid));
 		ColumnWalker.iterateOverColumns(context, grid, columnsWidthCounter, null, state);		
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, grid);
 		int sumWidth = state.getSumWidth() + 200;
 		variables.setVariable("sumWidth", new Integer(sumWidth));
 	}
 
 	private	RendererBase getCellTemplate() {
 		if (cellTemplate == null) {
 			cellTemplate = TemplateLoader.loadTemplate("org.richfaces.renderkit.html.ScrollableDataTableCellRenderer");
 		}
 		return cellTemplate;
 	}
 	
 	private	RendererBase getHeaderCellTemplate() {
 		
 		if (headerCellTemplate == null) {
 			headerCellTemplate = TemplateLoader.loadTemplate("org.richfaces.renderkit.html.ScrollableDataTableHeaderCellRenderer");
 		}
 		return headerCellTemplate;
 	}
 	
 	private	RendererBase getFooterCellTemplate() {
 		
 		if (footerCellTemplate == null) {
 			footerCellTemplate = TemplateLoader.loadTemplate("org.richfaces.renderkit.html.ScrollableDataTableFooterCellRenderer");
 		}
 		return footerCellTemplate;
 	}
 	
 	
 	
 	private RendererBase getHeaderItselfTemplate() {
 		
 		if (headerItselfTemplate == null) {
 			headerItselfTemplate = TemplateLoader.loadTemplate("org.richfaces.renderkit.html.ScrollableDataTableHeaderItselfRenderer");
 		}
 		
 		return headerItselfTemplate;
 	}
 
 
 	public void renderHiddenScrollInput(FacesContext context, UIScrollableDataTable grid) throws IOException{
 		
 		ResponseWriter writer = context.getResponseWriter();
 		String id = grid.getClientId(context) + ":si";
 		writer.startElement("input", grid);
 
 		getUtils().writeAttribute(writer, "type", "hidden");
 		getUtils().writeAttribute(writer, "name", id);
 		getUtils().writeAttribute(writer, "id", id);
 		getUtils().writeAttribute(writer, "value", grid.getScrollPos());
 		
 		writer.endElement("input");
 		
 	}
 	
 	public void contributorsEncodeHere(FacesContext context, UIScrollableDataTable grid) throws IOException {
 		RendererContributor [] contribs = composite.getContributors();
 		
 		if (contribs != null) {
 			for (int i = 0; i < contribs.length; i++) {
 				RendererContributor rendererContributor = contribs[i];
 				
 				if (rendererContributor instanceof HTMLEncodingContributor) {
 					((HTMLEncodingContributor) rendererContributor).encode(context, grid);
 				}
 			}
 		}
 	}
 	
 	private void addRowJavascriptEvents(ResponseWriter writer, UIComponent component) {
 		String attribute = (String)component.getAttributes().get("onRowClick");
 		if (attribute != null) {
 			try {
 				getUtils().writeAttribute(writer, "onclick", attribute);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		attribute = (String)component.getAttributes().get("onRowMouseDown");
 		if (attribute != null) {
 			try {
 				getUtils().writeAttribute(writer, "onmousedown", attribute);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		attribute = (String)component.getAttributes().get("onRowMouseUp");
 		if (attribute != null) {
 			try {
 				getUtils().writeAttribute(writer, "onmouseup", attribute);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		attribute = (String)component.getAttributes().get("onRowDblClick");
 		if (attribute != null) {
 			try {
 				getUtils().writeAttribute(writer, "ondblclick", attribute);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private Boolean getColumnSorting(UIScrollableDataTable grid, String sortColumn) {
 
 		Boolean sorting = null; 
 
 		UIComponent column = grid.findComponent(sortColumn);
 		
 		String name = ColumnUtil.getColumnSorting(column);
 		
 		SortOrder sortOrder = grid.getSortOrder();
 		
 		if (sortOrder != null) {
 			SortField[] sortFields = sortOrder.getFields();
 			
 			if (sortFields != null) {
 				for (int i = 0; i < sortFields.length && sorting == null; i++) {
 					SortField sortField = sortFields[i];
 					
 					if (name != null && name.equals(sortField.getName())) {
 						sorting = sortField.getAscending();
 					}
 				}
 			}
 		}
 		
 		return sorting;
 	}
 
 	private String getColumnWidth(UIComponent column) {
 		String width = (String) column.getAttributes().get("width");
 		if (width == null) {
 			width = "100px";
 		}
 		return width;
 	}
 }
