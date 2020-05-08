 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
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
 
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.component.UIDataAdaptor;
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.javascript.AjaxScript;
 import org.ajax4jsf.javascript.JSFunction;
 import org.ajax4jsf.javascript.PrototypeScript;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.renderkit.RendererUtils;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.resource.InternetResource;
 import org.richfaces.component.Column;
 import org.richfaces.component.Row;
 import org.richfaces.component.UIDataTable;
 import org.richfaces.component.util.FormUtil;
 import org.richfaces.component.util.ViewUtil;
 import org.richfaces.context.RequestContext;
 import org.richfaces.model.Ordering;
 import org.richfaces.renderkit.html.iconimages.DataTableIconSortAsc;
 import org.richfaces.renderkit.html.iconimages.DataTableIconSortDesc;
 import org.richfaces.renderkit.html.iconimages.DataTableIconSortNone;
 
 /**
  * @author shura
  * 
  */
 public abstract class AbstractTableRenderer extends AbstractRowsRenderer {
 
 	private static final String SORT_DIV = ":sortDiv";
 
 	private static final String SORT_FILTER_PARAMETER = "fsp";
 	
 	private static final String FILTER_INPUT_FACET_NAME = "filterValueInput";
 
 	private static final String REQUIRES_SCRIPTS_PARAMETER = AbstractTableRenderer.class.getName() + ":REQUIRES_SCRIPTS";
 	
 	private final InternetResource[] REQUIRED_SCRIPTS = new InternetResource[] {
 		getResource(PrototypeScript.class.getName()),
 		getResource(AjaxScript.class.getName())
 	};
 	
 	/**
 	 * Encode data table body
 	 * @param context
 	 * @param table
 	 * @throws IOException
 	 */
 	public void encodeTBody (FacesContext context, UIDataTable table) throws IOException {
 	    String clientId = table.getClientId(context);
 	    ResponseWriter writer = context.getResponseWriter();
 	    writer.startElement("tbody", table);
 	    writer.writeAttribute("id", clientId + ":tb", null);
 	    
 	    encodeRows(context, table);
 	    
 	    writer.endElement("tbody");
 	}
 	
 	@Override
 	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
 	    	encodeTBody(context, (UIDataTable) component);
 	}
 	
 	/**
 	 * Helper method for rendering data table tBody only.
 	 * For HTML consistency it wrap tBody with extra fake "table" tag
 	 * @param context
 	 * @param table
 	 * @throws IOException
 	 */
 	public void encodeTBodyAjax (FacesContext context, UIDataTable table) throws IOException {
 	    	ResponseWriter writer = context.getResponseWriter();
 		writer.startElement("table", table);
 		encodeTBody(context, table);
 		writer.endElement("table");
 	}
 	
 	/**
 	 * Check whether to render tbody only by Ajax call
 	 * @param context
 	 * @param table
 	 * @return data table tBody only should be rendered
 	 */
 	public boolean renderBodyOnly(FacesContext context, UIDataTable table) {
 	    Map<String, String> map = context.getExternalContext().getRequestParameterMap();
 	    return table.isRendered() && SORT_FILTER_PARAMETER.equals(map.get(table.getClientId(context)));
 	}
 	
 	/**
 	 * Encode all table structure - colgroups definitions, caption, header,
 	 * footer
 	 * 
 	 * @param context
 	 * @param table
 	 * @throws IOException
 	 */
 	public void encodeTableStructure(FacesContext context, UIDataTable table)
 			throws IOException {
 		
 		Object key = table.getRowKey();
 		table.captureOrigValue(context);
 		table.setRowKey(context, null);
 		
 		encodeCaption(context, table);
 		
 		// Encode colgroup definition.
 		ResponseWriter writer = context.getResponseWriter();
 		writer.startElement("colgroup", table);
 		int columns = getColumnsCount(table);
 		writer.writeAttribute("span", String.valueOf(columns), null);
 		String columnsWidth = (String) table.getAttributes().get("columnsWidth");
 		
 		if (null != columnsWidth) {
 			String[] widths = columnsWidth.split(",");
 			for (int i = 0; i < widths.length; i++) {
 				writer.startElement("col", table);
 				writer.writeAttribute("width", widths[i], null);
 				writer.endElement("col");
 			}
 		}
 		writer.endElement("colgroup");
 
 		encodeHeader(context, table, columns);
 		encodeFooter(context, table, columns);
 		
 		table.setRowKey(context,key);
 		table.restoreOrigValue(context);
 	}
 
 	public void encodeHeader(FacesContext context, UIDataTable table,
 			int numberOfColumns) throws IOException {
 		
 		UIComponent header = table.getHeader();
 
 		boolean isEncodeHeaders = isColumnFacetPresent(table, "header") || 
//			isHeaderFactoryColumnAttributePresent(table, "sortBy") ||
//			isHeaderFactoryColumnAttributePresent(table, "comparator") ||
 			isHeaderFactoryColumnAttributePresent(table, "filterBy");
 		
 		if (header != null || isEncodeHeaders) {
 		    
 		    ResponseWriter writer = context.getResponseWriter();
 			writer.startElement("thead", table);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, "rich-table-thead", null);
 			String headerClass = (String) table.getAttributes().get("headerClass");
 			if (header != null) {
 				encodeTableHeaderFacet(context, numberOfColumns, writer, header,
 						"rich-table-header",
 						"rich-table-header-continue",
 						"rich-table-headercell",
 						headerClass, "th");
 			}
 
 			if (isEncodeHeaders) {
 				writer.startElement("tr", table);
 				encodeStyleClass(writer, null,
 						"rich-table-subheader", null,
 						headerClass);
 				
 				encodeHeaderFacets(context, writer, table.columns(),
 						"rich-table-subheadercell",
 						headerClass, "header", "th", numberOfColumns);
 				
 				writer.endElement("tr");
 			}
 			writer.endElement("thead");
 		}
 	}
 	
 	public boolean isColumnFacetPresent(UIDataTable table, String facetName) {
 		Iterator<UIComponent> columns = table.columns();
 		boolean result = false;
 		while(columns.hasNext() && !result) {
 			UIComponent component = columns.next();
 			if(isColumnRendered(component)){
 				if(null != component.getFacet(facetName)){
 					result = true;
 				} /*else if(component instanceof Column) {
 					Column column = (Column)component;
 					result = column.isSelfSorted();
 				}*/
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns true if specified attribute (when present on the column)
 	 * should generate header even if it is not specified on the table 
 	 * @param table - rendered UIDataTable
 	 * @param attributeName - attribute name
 	 * @return true if specified attribute should generate header on the table
 	 */
 	public boolean isHeaderFactoryColumnAttributePresent(UIDataTable table,
             String attributeName) {
         Iterator<UIComponent> columns = table.columns();
         boolean result = false;
         
         while (columns.hasNext() && !result) {
             UIComponent column = columns.next();
             if (isColumnRendered(column)) {
                 if (null != column.getValueExpression(attributeName)) {
                     result = true;
                 }
             }
         }
         return result;
     }
 
 	/**
 	 * @param component
 	 * @return
 	 */
 	protected boolean isColumnRendered(UIComponent component) {
 		try {
 			return component.isRendered();
 		} catch(Exception e){
 			// DO nothing, rendered binded to row variable;
 		}
 		return true;
 	}
 	
 	protected void encodeHeaderFacets(FacesContext context,
 			ResponseWriter writer, Iterator<UIComponent> headers,
 			String skinCellClass, String headerClass, String facetName,
 			String element, int colCount) throws IOException {
 		int t_colCount = 0;
 
 		HeaderEncodeStrategy richEncodeStrategy = new RichHeaderEncodeStrategy();
 		HeaderEncodeStrategy simpleEncodeStrategy = new SimpleHeaderEncodeStrategy();
 
 		while (headers.hasNext()) {
 			UIComponent column = (UIComponent) headers.next();
 			if (!isColumnRendered(column)) {
 			    continue;
 			}
 				
 		    Integer colspan = (Integer) column.getAttributes().get("colspan");
             if (colspan != null && colspan.intValue() > 0) {
 				t_colCount += colspan.intValue();
 			} else {
 				t_colCount++;
 			}
 			
 			if (t_colCount > colCount) {
 				break;
 			}
 
 			String classAttribute = facetName + "Class";
 			String columnHeaderClass = (String) column.getAttributes().get(classAttribute);
 			
 			writer.startElement(element, column);
 			encodeStyleClass(writer, null, skinCellClass, headerClass, columnHeaderClass);
 			writer.writeAttribute("scope", "col", null);
 			getUtils().encodeAttribute(context, column, "colspan");
 
 			boolean sortableColumn = column.getValueExpression("comparator") != null
 					|| column.getValueExpression("sortBy") != null;
 
 			HeaderEncodeStrategy strategy = (column instanceof org.richfaces.component.UIColumn 
 			        && "header".equals(facetName)) ? richEncodeStrategy : simpleEncodeStrategy;
 
 			strategy.encodeBegin(context, writer, column, facetName, sortableColumn);
 
 			UIComponent facet = column.getFacet(facetName);
 			if (facet != null && isColumnRendered(facet)) {
 				renderChild(context, facet);
 			}
 
 			strategy.encodeEnd(context, writer, column, facetName, sortableColumn);
 
 			writer.endElement(element);
 		}
 	}
 
 	public void encodeFooter(FacesContext context, UIDataTable table, int columns) 
 	                throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		UIComponent footer = table.getFooter();
 		boolean columnFacetPresent = isColumnFacetPresent(table,"footer");
 		Iterator<UIComponent> tableColumns = table.columns();
 		if (footer != null || columnFacetPresent) {
 			writer.startElement("tfoot", table);
 			String footerClass = (String) table.getAttributes().get("footerClass");
 
 			if (columnFacetPresent) {
 				writer.startElement("tr", table);
 				encodeStyleClass(writer, null,
 						"rich-table-subfooter", null,
 						footerClass);
 		
 				encodeHeaderFacets(context, writer, tableColumns,
 						"rich-table-subfootercell",
 						footerClass, "footer", "td",columns);
 				
 				writer.endElement("tr");
 			}
 			if (footer != null) {
 				encodeTableHeaderFacet(context, columns, writer, footer,
 						"rich-table-footer",
 						"rich-table-footer-continue",
 						"rich-table-footercell",
 						footerClass, "td");
 			}
 			writer.endElement("tfoot");
 		}
 
 	}
 
 	public void encodeOneRow(FacesContext context, TableHolder holder)
 			throws IOException {
 		UIDataTable table = (UIDataTable) holder.getTable();
 		ResponseWriter writer = context.getResponseWriter();
 		Iterator<UIComponent> iter = table.columns();
 		boolean firstColumn = true;
 		boolean firstRow = (holder.getRowCounter() == 0);
 		int currentColumn = 0;
 		UIComponent column = null;
 		while (iter.hasNext()) {
 			column = (UIComponent) iter.next();
 			// Start new row for first column - expect a case of the detail
 			// table, wich will be insert own row.
 			if (firstColumn && !(column instanceof Row)) {
 				String rowSkinClass = getRowSkinClass();
 				if (firstRow) {
 					String firstRowSkinClass = getFirstRowSkinClass();
 					if (firstRowSkinClass != null && firstRowSkinClass.length() != 0) {
 						if (rowSkinClass != null && rowSkinClass.length() != 0) {
 							rowSkinClass += " " + firstRowSkinClass;
 						} else {
 							rowSkinClass = firstRowSkinClass;
 						}
 					}
 				}
 				
 				encodeRowStart(context, rowSkinClass, 
 				        holder.getRowClass(), table, writer);
 			}
 			if (column instanceof Column) {
 				boolean breakBefore = ((Column) column).isBreakBefore()
 						|| column instanceof Row;
 				if (breakBefore && !firstColumn) {
 					// close current row
 					writer.endElement(HTML.TR_ELEMENT);
 					// reset columns counter.
 					currentColumn = 0;
 					// Start new row, expect a case of the detail table, wich
 					// will be insert own row.
 					if (!(column instanceof Row)) {
 						holder.nextRow();
 						encodeRowStart(context, holder.getRowClass(), table, writer);
 					}
 				}
 				
 				encodeCellChildren(context, column,
 						firstRow ? getFirstRowSkinClass() : null,
 						getRowSkinClass(), holder.getRowClass(),
 						getCellSkinClass(), holder.getColumnClass(currentColumn));
 				// renderChild(context, column);
 				if ((column instanceof Row) && iter.hasNext()) {
 					// Start new row for remained columns.
 					holder.nextRow();
 					encodeRowStart(context, holder.getRowClass(), table, writer);
 					// reset columns counter.
 					currentColumn = -1;
 				}
 			} else if (column.isRendered()) {
 				// UIColumn don't have own renderer
 				writer.startElement(HTML.td_ELEM, table);
 				getUtils().encodeId(context, column);
 				String columnClass = holder.getColumnClass(currentColumn);
 				encodeStyleClass(writer, null, getCellSkinClass(), null, columnClass);
 
 				// TODO - encode column attributes.
 				renderChildren(context, column);
 				writer.endElement(HTML.td_ELEM);
 			}
 			currentColumn++;
 			firstColumn = false;
 		}
 		// Close row if then is open.
 		if (!firstColumn && !(column instanceof Row)) {
 			writer.endElement(HTML.TR_ELEMENT);
 		}
 	}
 
 	protected void encodeRowStart(FacesContext context, String rowClass,
 			UIDataTable table, ResponseWriter writer) throws IOException {
 		encodeRowStart(context, getRowSkinClass(), rowClass, table, writer);
 	}
 
 	/**
 	 * @return
 	 */
 	protected String getRowSkinClass() {
 		return "rich-table-row";
 	}
 
 	/**
 	 * @return
 	 */
 	protected String getFirstRowSkinClass() {
 		return "rich-table-firstrow";
 	}
 
 	/**
 	 * @return
 	 */
 	protected String getCellSkinClass() {
 		return "rich-table-cell";
 	}
 
 	protected void encodeRowStart(FacesContext context, String skinClass,
 			String rowClass, UIDataTable table, ResponseWriter writer)
 			throws IOException {
 		writer.startElement(HTML.TR_ELEMENT, table);
 		encodeStyleClass(writer, null, skinClass, null, rowClass);
 		encodeRowEvents(context, table);
 	}
 
 	/**
 	 * Calculate total number of columns in table.
 	 * 
 	 * @param table
 	 * @return
 	 */
 	protected int getColumnsCount(UIDataTable table) {
 		int count = 0;
 		// check for exact value in component
 		Integer span = (Integer) table.getAttributes().get("columns");
 		if (null != span && span.intValue() != Integer.MIN_VALUE) {
 			count = span.intValue();
 		} else {
 			// calculate max html columns count for all columns/rows children.
 			Iterator<UIComponent> col = table.columns();
 			count = calculateRowColumns(col);
 		}
 		return count;
 	}
 
 	/**
 	 * Calculate max number of columns per row. For rows, recursive calculate
 	 * max length.
 	 * 
 	 * @param col -
 	 *            Iterator other all columns in table.
 	 * @return
 	 */
 	protected int calculateRowColumns(Iterator<UIComponent> col) {
 		int count = 0;
 		int currentLength = 0;
 		while (col.hasNext()) {
 			UIComponent column = (UIComponent) col.next();
 			if (column.isRendered()) {
 				if (column instanceof Row) {
 					// Store max calculated value of previsous rows.
 					if (currentLength > count) {
 						count = currentLength;
 					}
 					// Calculate number of columns in row.
 					currentLength = calculateRowColumns(((Row) column).columns());
 					// Store max calculated value
 					if (currentLength > count) {
 						count = currentLength;
 					}
 					currentLength = 0;
 				} else if (column instanceof Column) {
 					Column tableColumn = (Column) column;
 					// For new row, save length of previsous.
 					if (tableColumn.isBreakBefore()) {
 						if (currentLength > count) {
 							count = currentLength;
 						}
 						currentLength = 0;
 					}
 					Integer colspan = (Integer) column.getAttributes().get("colspan");
 					// Append colspan of this column
 					if (null != colspan && colspan.intValue() != Integer.MIN_VALUE) {
 						currentLength += colspan.intValue();
 					} else {
 						currentLength++;
 					}
 				} else if (column instanceof UIColumn) {
 					// UIColumn always have colspan == 1.
 					currentLength++;
 				}
 			}
 		}
 		if (currentLength > count) {
 			count = currentLength;
 		}
 		return count;
 	}
 	
 	@Override
 	protected void doDecode(FacesContext context, UIComponent component) {
 		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
 		String clientId = component.getClientId(context);
 		if (SORT_FILTER_PARAMETER.equals(map.get(clientId))) {
 			String sortColumnId = map.get(SORT_FILTER_PARAMETER);
 			List<UIComponent> list = component.getChildren();
 			UIDataTable table = (UIDataTable) component;
 			boolean isSingleSortMode = !"multi".equals(table.getSortMode());
 			for (Iterator<UIComponent> iterator = list.iterator(); iterator
 					.hasNext();) {
 				UIComponent child = iterator.next();
 				if (child instanceof org.richfaces.component.UIColumn) {
 					org.richfaces.component.UIColumn column = (org.richfaces.component.UIColumn) child;
 					if(column.isRendered()){
 						child.setId(child.getId());
 						if (sortColumnId != null) {
 							String columnClientId = child.getClientId(context);
 							if (sortColumnId.equals(columnClientId)) {
 								String id = child.getId();
 								Collection<Object> sortPriority = table.getSortPriority();
 								if (isSingleSortMode) {
 									sortPriority.clear();
 								}
 								if(!sortPriority.contains(id)) {
 									sortPriority.add(id);
 								}
 								column.toggleSortOrder();
 							} else if(isSingleSortMode){
 								column.setSortOrder(Ordering.UNSORTED);
 							}
 							
 							RequestContext requestContext = RequestContext.getInstance(context);
 							requestContext.setAttribute(columnClientId + SORT_DIV, Boolean.TRUE);
 						}
 						UIInput filterValueInput = (UIInput)child.getFacet(FILTER_INPUT_FACET_NAME);
 						if (null != filterValueInput) {
 							filterValueInput.setId(filterValueInput.getId());
 							filterValueInput.decode(context);
 							Object submittedValue = filterValueInput.getSubmittedValue();
 							if (null != submittedValue) {
 								column.setFilterValue(filterValueInput.getSubmittedValue().toString());
 							}
 						}
 					}	
 				}
 
 			}
 			
 			AjaxContext ajaxContext = AjaxContext.getCurrentInstance();
 			ajaxContext.addRegionsFromComponent(component);
 			ajaxContext.addComponentToAjaxRender(component);
 			ajaxContext.addRegionsFromComponent(component);
 			
 			ajaxContext.addRenderedArea(clientId + ":tb");
 			
 			// FIXME: check for correct client id.
 			// Now path & client id mixed here, it is possible that
 			// they will be different un case of dataTable in dataTable. 
 			
 			// Due to we are re render whole data table, Ajax runtime didn't add to reRender   
 			// ids of those childs that specified in reRender data table attribute
 			// so let's add them to ajax render areas here by hand
 			Set<String> ajaxRenderedAreas = ajaxContext.getAjaxRenderedAreas();
 			Set<String> areasToRender = ajaxContext.getAjaxAreasToRender();
 			for (String area : areasToRender) {
 			    // process only child components, all other should be added to render
 			    // automatically by ajax
 			    if (area.startsWith(NamingContainer.SEPARATOR_CHAR + clientId)) {
 				area = area.substring(1); // remove unnecessary start separator symbol
 				if (!area.equals(clientId) && !ajaxRenderedAreas.contains(area)) {
 				    ajaxContext.addRenderedArea(area);
 				}
 			    }
 			}
 		}
 	}
 	
 	@Override
 	public void encodeEnd(FacesContext context, UIComponent component)
 			throws IOException {
 		super.encodeEnd(context, component);
 		if (component instanceof UIDataTable) {
 			AjaxContext ajaxContext = AjaxContext.getCurrentInstance();
 			Set<String> ajaxRenderedAreas = ajaxContext.getAjaxRenderedAreas();
 			String clientId = ((UIDataTable) component).getBaseClientId(context);
 			if(ajaxRenderedAreas.contains(clientId+ ":tb")) {
 				ajaxRenderedAreas.remove(clientId);
 			}
 		}
 	}
 	
 	protected void addInplaceInput(FacesContext context, UIComponent column,
 			String buffer) throws IOException {
 		UIInput filterValueInput = (UIInput) column
 				.getFacet(FILTER_INPUT_FACET_NAME);
 		if (null == filterValueInput) {
 			filterValueInput = (UIInput) context.getApplication().createComponent(UIInput.COMPONENT_TYPE);
 			filterValueInput.setId(column.getId() + SORT_FILTER_PARAMETER);
 			filterValueInput.setImmediate(true);
 			column.getFacets().put(FILTER_INPUT_FACET_NAME, filterValueInput);
 			
 			//Event.stop requires prototype.js
 			setRequiresScripts(context);
 			
 			filterValueInput.getAttributes().put(HTML.onclick_ATTRIBUTE, "Event.stop(event);");
 			filterValueInput.getAttributes().put(HTML.STYLE_CLASS_ATTR, "rich-filter-input");
 		}
 		String filterEvent = (String) column.getAttributes().get("filterEvent");
 		if (null == filterEvent || "".equals(filterEvent)) {
 			filterEvent = "onchange";
 		}
 		
 		filterValueInput.getAttributes().put(filterEvent, buffer);		
 		filterValueInput.setValue(column.getAttributes().get("filterValue"));
 
 		getUtils().encodeBeginFormIfNessesary(context, column);
 		renderChild(context, filterValueInput);
 		getUtils().encodeEndFormIfNessesary(context, column);
 	}
 	
 	protected String buildAjaxFunction(FacesContext context, UIComponent column, boolean sortable) {
 		UIComponent table = column.getParent();
 		String id = table.getClientId(context);
 
 		//Ajax submission requires scripts
 		setRequiresScripts(context);
 		
 		JSFunction ajaxFunction = AjaxRendererUtils.buildAjaxFunction(table, context);
 		Map<String, Object> eventOptions = AjaxRendererUtils.buildEventOptions(context, table, true);
 		
 		
 		@SuppressWarnings("unchecked")
 		Map<String, Object> parameters = 
 		    (Map<String, Object>) eventOptions.get("parameters");
 		
 		parameters.put(id, SORT_FILTER_PARAMETER);
 		if (sortable) {
 			parameters.put(SORT_FILTER_PARAMETER, column.getClientId(context));
 		}
 		ajaxFunction.addParameter(eventOptions);
 		
 		StringBuffer buffer = new StringBuffer();
 		ajaxFunction.appendScript(buffer);
 		
 		return buffer.toString();
 	}
 	
 	protected void setRequiresScripts(FacesContext context) {
 		ExternalContext externalContext = context.getExternalContext();
 		Map<String, Object> requestMap = externalContext.getRequestMap();
 		if (requestMap.get(REQUIRES_SCRIPTS_PARAMETER) == null) {
 			requestMap.put(REQUIRES_SCRIPTS_PARAMETER, Boolean.TRUE);
 		}
 	}
 
 	protected boolean isRequiresScripts(FacesContext context) {
 		if (context == null) {
 
 			return false;
 		} else {
 			ExternalContext externalContext = context.getExternalContext();
 			Map<String, Object> requestMap = externalContext.getRequestMap();
 
 			return Boolean.TRUE.equals(requestMap.get(REQUIRES_SCRIPTS_PARAMETER));
 		}
 	}
 	
 	@Override
 	protected InternetResource[] getScripts() {
 		InternetResource[] resources = null;
 		if (isRequiresScripts(FacesContext.getCurrentInstance())) {
 			resources = REQUIRED_SCRIPTS;
 		}
 
 		return resources;
 	}
 	
 	protected class SimpleHeaderEncodeStrategy implements HeaderEncodeStrategy {
 
 		public void encodeBegin(FacesContext context, ResponseWriter writer,
 				UIComponent column, String facetName, boolean sortableColumn)
 				throws IOException {
 			
 		}
 
 		public void encodeEnd(FacesContext context, ResponseWriter writer,
 				UIComponent column, String facetName, boolean sortableColumn)
 				throws IOException {
 			
 		}
 
 
 	}
 	
 	protected class RichHeaderEncodeStrategy implements HeaderEncodeStrategy {
 
 		public void encodeBegin(FacesContext context, ResponseWriter writer,
 				UIComponent column, String facetName, boolean sortableColumn)
 					throws IOException {
 				org.richfaces.component.UIColumn col = 
 					(org.richfaces.component.UIColumn) column;
 				String columnClientId = col.getClientId(context);
 				String clientId = columnClientId + facetName;
 				writer.writeAttribute("id", clientId, null);
 				
 				if (sortableColumn && col.isSelfSorted()) {
 					FormUtil.throwEnclFormReqExceptionIfNeed(context, column.getParent());
 					writer.writeAttribute(HTML.onclick_ATTRIBUTE, buildAjaxFunction(context, column, true)
 							.toString(), null);
 					writer.writeAttribute(HTML.style_ATTRIBUTE, "cursor: pointer;", null);
 				}
 				
 				writer.startElement(HTML.DIV_ELEM, column);
 				writer.writeAttribute(HTML.id_ATTRIBUTE, clientId + SORT_DIV, null);
 				
 				RequestContext requestContext = RequestContext.getInstance(context);
 				if (Boolean.TRUE.equals(requestContext.getAttribute(columnClientId + SORT_DIV))) {
 					AjaxContext.getCurrentInstance().addRenderedArea(clientId + SORT_DIV);
 				}
 				
 				if (sortableColumn) {
 					writer.startElement(HTML.SPAN_ELEM, column);
 					writer.writeAttribute(HTML.class_ATTRIBUTE, "rich-table-sortable-header", null);
 				}
 		}
 
 		public void encodeEnd(FacesContext context, ResponseWriter writer,
 				UIComponent column, String facetName, boolean sortableColumn) throws IOException {
 				org.richfaces.component.UIColumn col = 
 					(org.richfaces.component.UIColumn) column;
 				if (sortableColumn) {
 					String imageUrl = null;
 					if (Ordering.ASCENDING.equals(col.getSortOrder())) {
 						if (null != col.getSortIconAscending()) {
 							imageUrl = ViewUtil.getResourceURL(col.getSortIconAscending(), context);
 						} else {
 							imageUrl = getResource(DataTableIconSortAsc.class.getName()).getUri(context, null);
 						}
 					} else if (Ordering.DESCENDING.equals(col.getSortOrder())) {
 						if (null != col.getSortIconDescending()) {
 							imageUrl = ViewUtil.getResourceURL(col.getSortIconDescending(), context);
 						} else {
 							imageUrl = getResource(DataTableIconSortDesc.class.getName()).getUri(context, null);
 						}
 					} else if (col.isSelfSorted()) {
 						if (null != col.getSortIcon()) {
 							imageUrl = ViewUtil.getResourceURL(col.getSortIcon(), context);
 						} else {
 							imageUrl = getResource(DataTableIconSortNone.class.getName()).getUri(context, null);
 						}
 					}
 					
 					if (imageUrl != null) {
 						writer.startElement(HTML.IMG_ELEMENT, column);
 						writer.writeAttribute(HTML.src_ATTRIBUTE, imageUrl, null);
 						writer.writeAttribute(HTML.alt_ATTRIBUTE, "", null);
 						writer.writeAttribute(HTML.width_ATTRIBUTE, "15", null);
 						writer.writeAttribute(HTML.height_ATTRIBUTE, "15", null);
 						writer.writeAttribute(HTML.class_ATTRIBUTE, "rich-sort-icon", null);
 						writer.endElement(HTML.IMG_ELEMENT);
 					}
 					writer.endElement(HTML.SPAN_ELEM);
 				}
 				
 				writer.endElement(HTML.DIV_ELEM);
 				
 				if (col.getFilterMethod() == null
 						&& col.getValueExpression("filterExpression") == null
 						&& col.getValueExpression("filterBy") != null) {
 					
 					writer.startElement(HTML.DIV_ELEM, column);
 					addInplaceInput(context, column, buildAjaxFunction(context, column, false));
 					writer.endElement(HTML.DIV_ELEM);
 				}
 		}
 	}
 	
 	@Override
 	protected void encodeRowEvents(FacesContext context, UIDataAdaptor table)
 			throws IOException {
 		super.encodeRowEvents(context, table);
 		RendererUtils utils2 = getUtils();
 		utils2.encodeAttribute(context, table, "onRowContextMenu", "oncontextmenu" );
 		
 	}
 }
