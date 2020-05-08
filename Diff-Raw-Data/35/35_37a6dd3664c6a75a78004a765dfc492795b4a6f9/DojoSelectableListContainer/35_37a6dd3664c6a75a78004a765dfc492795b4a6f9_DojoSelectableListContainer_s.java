 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package wicket.contrib.dojo.markup.html.list.table;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wicket.Component;
 import wicket.MarkupContainer;
 import wicket.ResourceReference;
 import wicket.WicketRuntimeException;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.DojoIdConstants;
 import wicket.contrib.dojo.widgets.StylingWebMarkupContainer;
 import wicket.markup.ComponentTag;
 import wicket.markup.MarkupStream;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.link.ILinkListener;
 import wicket.markup.html.list.ListView;
 import wicket.markup.repeater.RepeatingView;
 import wicket.model.IModel;
 
 /**
  * Selectable List container
  * <pre>
  * 		DojoSelectableListContainer container = new DojoSelectableListContainer(this, "container");
  * 		ListView list = new ListView(container, "list"){
  * 			[...]
  * 		};
  * </pre>
  * <b>The html (wicket:id="container" in the previous example) tag should be a &lt;table&gt;</b>
  * @author Vincent Demay
  *
  */
 public class DojoSelectableListContainer extends StylingWebMarkupContainer implements ILinkListener
 {
 	/**
 	 * List of selected objects
 	 */
 	private List selected;
 
 	private boolean enableMultipleSelect;
 	
 	private boolean enableAlternateRows;
 	
 	private String cssClass;
 	
 	private String alternateRowClass;
 	
 	/**
 	 * flag to know if on choose meke a ajax request or not
 	 */
 	private boolean ajaxModeOnChoose;
 	
 	/**
 	 * Allow user to set another css to overwrite the default one
 	 */
 	private ResourceReference overrideCssReference;
 	
 	//child
 	private WebMarkupContainer child;
 
 	/**
 	 * Construct the selectable list container
 	 * @param parent parent where this widget will be displayed
 	 * @param id container id
 	 */
 	public DojoSelectableListContainer(MarkupContainer parent, String id)
 	{
 		this(parent, id, null);
 	}
 
 	/**
 	 * Construct the selectable list container
 	 * @param parent parent where this widget will be displayed
 	 * @param id container id
 	 * @param model model
 	 */
 	public DojoSelectableListContainer(MarkupContainer parent, String id, IModel model)
 	{
 		super(parent, id, model);
 		enableMultipleSelect = true;
 		enableAlternateRows = true;
 		cssClass = "dojoSelectableList";
 		ajaxModeOnChoose = true;
 		alternateRowClass = "alternateRow";
 	}
 
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		if (getMarkupStream().atTag() && !"table".equals(getMarkupStream().getTag().getName())){
 			throw new WicketRuntimeException("Encountered tag name: '" + getMarkupStream().getTag().getName() + "', should be 'table'");
 		}
 		tag.put(DojoIdConstants.DOJO_TYPE, "SelectableTable");
 		tag.put("enableMultipleSelect", enableMultipleSelect + "");
 		tag.put("enableAlternateRows", enableAlternateRows + "");
 		tag.put("rowAlternateClass", alternateRowClass);
 		tag.put("class", cssClass);
 	}
 	
 	/**
 	 * Happen when dblclick and ajax enabled
 	 * <b>by default ajax is enabled</b>
 	 */
 	public final void onLinkClicked()
 	{
 		int selectIndex = Integer.parseInt(getRequest().getParameter("select"));
 		if (child instanceof ListView){
 			ListView listView = (ListView) child;
 			onNonAjaxChoose(listView.getList().get(selectIndex));
 		}//else TODO for RepeatingView
 		
 	}
 	
 	protected void onAttach()
 	{
 		super.onAttach();
 		this.child = getChild();
		add(new DojoSelectableListContainerHandler(child));
 	}
 	
 
 	// set to Empty the model
 	protected void onRender(MarkupStream markupStream)
 	{
 		super.onRender(markupStream);
 		this.selected = new ArrayList();
 	}
 
 	/**
 	 * Find the list view in children
 	 * if none or more than one throw an exception!
 	 * 
 	 * @return the child ListView of this container
 	 */
 	public WebMarkupContainer getChild()
 	{
 		ChildFinder visitor = new ChildFinder();
 		visitChildren(visitor);
 		return visitor.getChild();
 	}
 	
 	/*																									  *\
 	 * ---------------------------------------------------------------------------------------------------*
 	\*																								      */
 	
 	/**
 	 * Enable or not multipleSelection on items
 	 * @param enableMultipleSelect true to enable multiple selection on items, false otherwise
 	 */
 	public void enableMultipleSelect(boolean enableMultipleSelect){
 		this.enableMultipleSelect = enableMultipleSelect;
 	}
 	
 	/**
 	 * return true if multiple selection is enabled and false otherwise
 	 * @return  true if multiple selection is enabled and false otherwise
 	 */
 	public boolean multipleSelectEnabled(){
 		return this.enableMultipleSelect;
 	}
 	
 	/**
 	 * Enable or not alternate class on rows
 	 * @param enableAlternateRows true to enable alternate class on rows, false otherwise
 	 */
 	public void enableAlternateRows(boolean enableAlternateRows){
 		this.enableAlternateRows = enableAlternateRows;
 	}
 	
 	/**
 	 * return true if alternate class on rows is enabled and false otherwise
 	 * @return  true if alternate class on rows is enabled and false otherwise
 	 */
 	public boolean alternateRowsEnabled(){
 		return this.enableAlternateRows;
 	}
 
 	/**
 	 * Get the CSS class for the table, defaults to "dojoSelectableList".
 	 * It is used to render the table, selection and onMouse over.
 	 * TODO : more explanation
 	 * @return the table's CSS class
 	 */
 	public String getCssClass()
 	{
 		return cssClass;
 	}
 
 	/**
 	 * Override the default CSS class "dojoSelectableList" for the table
 	 * @param tbodyClass the new table body class
 	 */
 	public void setCssClass(String tbodyClass)
 	{
 		this.cssClass = tbodyClass;
 	}
 
 	/**
 	 * return a list containing all selected items
 	 * @return a list containing all selected items
 	 */
 	public List getSelected()
 	{
 		return selected;
 	}
 
 	/**
 	 * change the selected List
 	 * @param selected the new selected List
 	 */
 	public void setSelected(List selected)
 	{
 		this.selected = selected;
 	}
 	
 	/**
 	 * return boolean to know if ajax is enable on the choose(dblclick)
 	 * @return true if ajax is active on choose
 	 */
 	public boolean isAjaxModeOnChoose()
 	{
 		return ajaxModeOnChoose;
 	}
 	
 	/**
 	 * return the used css to overwrite the default one
 	 * @return the used css to overwrite the default one or null if none ios defined
 	 */
 	public ResourceReference getOverrideCssReference()
 	{
 		return overrideCssReference;
 	}
 
 	/**
 	 * set a css reference to overwrite the default one
 	 * @param overwriteCss  a css reference to overwrite the default one
 	 */
 	public void setOverrideCssReference(ResourceReference overwriteCss)
 	{
 		this.overrideCssReference = overwriteCss;
 	}
 
 	/**
 	 * set boolean to know if ajax is enable on the choose(dblclick)
 	 * @param ajaxModeOnChoose true if ajax is enable on the choose(dblclick)
 	 */
 	public void setAjaxModeOnChoose(boolean ajaxModeOnChoose)
 	{
 		this.ajaxModeOnChoose = ajaxModeOnChoose;
 	}
 	
 	/**
 	 * Triggered when selection change
 	 * @param target ajax target
 	 * @param selected List of selected item
 	 */
 	public void onSelection(AjaxRequestTarget target, List selected)
 	{
 		
 	}
 
 	/**
 	 * Triggered when double click on a table row and ajaxOnChoose is enabled
 	 * <b>by default ajax is enabled</b>
 	 * @param target ajax target
 	 * @param selected the object corresponding to the table row that has been choosen
 	 */
 	public void onChoose(AjaxRequestTarget target, Object selected)
 	{
 		
 	}
 	
 	/**
 	 * Triggered when double click on a table row and ajaxOnChoose is disabled
 	 * <b>by default ajax is enabled</b>
 	 * @param selected selected item
 	 */
 	public void onNonAjaxChoose(Object selected)
 	{
 		
 	}
 
 	public String getAlternateRowClass()
 	{
 		return alternateRowClass;
 	}
 
 	public void setAlternateRowClass(String alternateRowClass)
 	{
 		this.alternateRowClass = alternateRowClass;
 	}
 	
 	/***************************************************************************/
 	
 	private class ChildFinder implements IVisitor{
 		private WebMarkupContainer child = null;
 		private int listViewNumber = 0;
 		private int repeatingViewNumber = 0;
 		
 		public Object component(Component component)
 		{
 			if (component instanceof wicket.markup.html.list.ListView){
 				child = (ListView)component;
 				listViewNumber ++;
 			}
 			if (component instanceof RepeatingView){
 				child = (RepeatingView)component;
 				repeatingViewNumber ++;
 			}
 			return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
 		}
 		
 		public WebMarkupContainer getChild(){
 			if (listViewNumber != 1 && repeatingViewNumber != 1){
 				throw new WicketRuntimeException("A DojoSelectableListContainer should contain exactly one ListView or one RepeatingView as directly child");
 			}
 			//FIXME check for TR
 			/*if (!"tr".equals(listView.getMarkupStream().getTag().getName())){
 				throw new WicketRuntimeException("Tag name for a DojoSelectableListContinaner listView should be 'tr'");
 			}*/
 			return child;
 		}
 	}
 
 }
