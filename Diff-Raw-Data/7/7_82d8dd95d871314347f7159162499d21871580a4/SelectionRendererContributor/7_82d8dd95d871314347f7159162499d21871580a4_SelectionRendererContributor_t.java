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
 import java.util.Map;
 
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.model.DataVisitor;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIScrollableDataTable;
 import org.richfaces.model.selection.ClientSelection;
 import org.richfaces.model.selection.Selection;
 import org.richfaces.model.selection.SimpleSelection;
 import org.richfaces.renderkit.CompositeRenderer;
 import org.richfaces.renderkit.RendererContributor;
 import org.richfaces.renderkit.ScriptOptions;
 
 /**
  * @author Maksim Kaszynski
  * 
  */
 public class SelectionRendererContributor implements RendererContributor, HTMLEncodingContributor {
 
 
 	public static final String CLIENT_SELECTION = "clientSelection";
 	
 	public static final String getSelectionInputName(FacesContext context,
 			UIScrollableDataTable grid) {
 		String id = grid.getBaseClientId(context) + ":s";
 		
 		return id;
 	}
 
 	public void decode(FacesContext context, UIComponent component,
 			CompositeRenderer compositeRenderer) {
 		
 		final UIScrollableDataTable grid = (UIScrollableDataTable) component;
 		
 		if (grid.isSelectionEnabled()) {
 			ExternalContext externalContext = context.getExternalContext();
 			Map<String, String> requestParamMap = externalContext
 					.getRequestParameterMap();
 			Application application = context.getApplication();
 			String id = getSelectionInputName(context, grid);
 			String value = (String) requestParamMap.get(id);
 			Converter converter = application
 					.createConverter(ClientSelection.class);
 			ClientSelection _oldClientSelection = (ClientSelection) grid
 					.getAttributes().get(CLIENT_SELECTION);
 			final ClientSelection oldClientSelection = _oldClientSelection == null ? new ClientSelection()
 					: _oldClientSelection;
 			final ClientSelection clientSelection = (ClientSelection) converter
 					.getAsObject(context, grid, value);
 			final ScrollableDataTableRendererState state = ScrollableDataTableRendererState
 					.createState(context, grid);
 			state.setRowIndex(ScrollableDataTableUtils.getClientRowIndex(grid));
 			final SimpleSelection simpleSelection = grid.getSelection() == null ? new SimpleSelection()
 					: (SimpleSelection) grid.getSelection();
 			if (clientSelection.isReset() || clientSelection.isSelectAll()) {
 				simpleSelection.clear();
 				simpleSelection.setSelectAll(clientSelection.isSelectAll());
 			}
 			try {
 				grid.walk(context, new DataVisitor() {
 					public void process(FacesContext context, Object rowKey,
 							Object argument) throws IOException {
 
 						int i = state.getRowIndex();
 
 						if (shouldAddToSelection(i, oldClientSelection,
 								clientSelection)) {
 
 							simpleSelection.addKey(rowKey);
 
 						} else if (shouldRemoveFromSelection(i,
 								oldClientSelection, clientSelection)) {
 
 							simpleSelection.removeKey(rowKey);
 
 						}
 
 						if (i == clientSelection.getActiveRowIndex()) {
 							grid.setActiveRowKey(rowKey);
 						}
 						state.nextRow();
 
 					}
 				}, state);
 			} catch (IOException e) {
 				throw new FacesException(e);
 			}
 			grid.setSelection(simpleSelection);
 			ValueExpression selectionBinding = grid
 					.getValueExpression("selection");
 			if (selectionBinding != null) {
 				selectionBinding.setValue(context.getELContext(),
 						simpleSelection);
 			}
 			ScrollableDataTableRendererState.restoreState(context);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.richfaces.renderkit.RendererContributor#getAcceptableClass()
 	 */
 	public Class<?> getAcceptableClass() {
 		return UIScrollableDataTable.class;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.richfaces.renderkit.RendererContributor#getScriptContribution(javax.faces.context.FacesContext,
 	 *      javax.faces.component.UIComponent)
 	 */
 	public String getScriptContribution(FacesContext context,
 			UIComponent component) {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.richfaces.renderkit.RendererContributor#getScriptDependencies()
 	 */
 	public String[] getScriptDependencies() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.richfaces.renderkit.RendererContributor#getStyleDependencies()
 	 */
 	public String[] getStyleDependencies() {
 		return null;
 	}
 
 	public ScriptOptions buildOptions(FacesContext context,
 			UIComponent component) {
 		UIScrollableDataTable table = (UIScrollableDataTable) component;
 		ScriptOptions scriptOptions = new ScriptOptions(component);
 		if (table.isSelectionEnabled()) {
 			scriptOptions.addOption("selectionInput", getSelectionInputName(
 					context, table));
 			Map<String, Object> attributes = component.getAttributes();
 			Object attribut = attributes.get("selectedClass");
 			if (attribut == null) {
 				attribut = "";
 			}
 			scriptOptions.addOption("selectedClass", attribut);
 			attribut = attributes.get("activeClass");
 			if (attribut == null) {
 				attribut = "";
 			}
 			scriptOptions.addOption("activeClass", attribut);
 			scriptOptions.addOption("selectionMode", table.getSelectionMode());
 		}
 		return scriptOptions;
 	}
 	
 	
 	public void encode(FacesContext context, UIComponent component)
 			throws IOException {
 		
 		UIScrollableDataTable grid = (UIScrollableDataTable) component;
 		
 		if (grid.isSelectionEnabled()) {
 			encodeSelection(context, grid);
 			writeSelection(context, grid);
 		}
 	}
 	
 	
 	//Decide whether to add new row to selection based on comparison with old one
 	public boolean shouldAddToSelection(int i, ClientSelection oldSelection, ClientSelection newSelection) {
 		
 		return newSelection.isSelectAll() || 
 			(newSelection.isSelected(i) && (!oldSelection.isSelected(i) || newSelection.isReset())) ;
 	}
 
 	//Decide whether to remove new row to selection based on comparison with old one
 	public boolean shouldRemoveFromSelection(int i, ClientSelection oldSelection, ClientSelection newSelection) {
 		return !newSelection.isReset() && (!newSelection.isSelectAll() && (!newSelection.isSelected(i) && oldSelection.isSelected(i)));
 	}
 	
 
 	private void encodeSelection(FacesContext context, final UIScrollableDataTable grid) throws IOException {
 		final ScrollableDataTableRendererState state = ScrollableDataTableRendererState.createState(context, grid);
 		
 		state.setRowIndex(ScrollableDataTableUtils.getClientRowIndex(grid));
 		
 		final Selection gridSelection = 
 			grid.getSelection() == null ? 
 					new SimpleSelection() : 
 						grid.getSelection();
 		final ClientSelection clientSelection = new ClientSelection();
 		
 		grid.walk(context, 
 			new DataVisitor() {
 				public void process(FacesContext context, Object rowKey,
 						Object argument) throws IOException {
 				
 					if (gridSelection.isSelected(rowKey)) {
 
 						int i = state.getRowIndex();
 						
 						clientSelection.addIndex(i);
 					}
 					
 					if (rowKey.equals(grid.getActiveRowKey())) {
 						clientSelection.setActiveRowIndex(state.getRowIndex());
 					}
 					
 					state.nextRow();
 					
 				}
 			}, 
 		state);
 		
 		
 		ScrollableDataTableRendererState.restoreState(context);
 		
 		grid.getAttributes().put(CLIENT_SELECTION, clientSelection);
 	}
 	
 	/**
 	 * Get client selection from the component, transform it into string form,
 	 * and write it as hidden input
 	 * @param context
 	 * @param grid
 	 * @throws IOException
 	 */
 	public void writeSelection(FacesContext context, UIScrollableDataTable grid) 
 		throws IOException {
 		
 		Application application = context.getApplication();
 		
 		Converter converter = 
 			application.createConverter(ClientSelection.class);
 		
 		ClientSelection selection = (ClientSelection)grid.getAttributes().get(CLIENT_SELECTION);
 		String string = 
 			converter.getAsString(context, grid, selection);
 		
 		if (string == null) {
 			string = "";
 		}
 		
 		string += selection.getActiveRowIndex();
 		
 		String id = getSelectionInputName(context, grid);
 		
 		
 		ResponseWriter writer = context.getResponseWriter();
 		writer.startElement(HTML.INPUT_ELEM, grid);
 		writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
 		writer.writeAttribute(HTML.id_ATTRIBUTE, id, null);
		writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
 		writer.writeAttribute(HTML.NAME_ATTRIBUTE, id, null);
 		writer.writeAttribute(HTML.value_ATTRIBUTE, string, null);
 		writer.endElement(HTML.INPUT_ELEM);
 		
 		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
 		
 		if (ajaxContext.isAjaxRequest()) {
 			ajaxContext.addRenderedArea(id);
 		}
 		
 	}
 
 
 }
