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
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.ResourceReference;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.link.ILinkListener;
 import wicket.markup.html.list.ListView;
 import wicket.markup.repeater.Item;
 import wicket.markup.repeater.RepeatingView;
 
 /**
  * Handler associated with {@link DojoSelectableListContainer}
  * @author Vincent Demay
  *
  */
 public class DojoSelectableListContainerHandler extends AbstractRequireDojoBehavior
 {
 	
 	//child of this container
 	private WebMarkupContainer child;
 
 	/**
 	 * 
 	 * @param listView
 	 */
	public DojoSelectableListContainerHandler()
 	{
 		super();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see wicket.contrib.dojo.AbstractRequireDojoBehavior#setRequire(wicket.contrib.dojo.AbstractRequireDojoBehavior.RequireDojoLibs)
 	 */
 	public void setRequire(RequireDojoLibs libs)
 	{
 		//DO Nothing, the Widget is in the package
 	}
 
 
 	protected final void respond(AjaxRequestTarget target)
 	{
 		ArrayList selected = new ArrayList();
 		String indexList[] = getComponent().getRequest().getParameters("select");
 		if (indexList == null){
 			if (((DojoSelectableListContainer)getComponent()).getSelected() != null){
 				((DojoSelectableListContainer)getComponent()).onChoose(target, ((DojoSelectableListContainer)getComponent()).getSelected().get(0));
 			}
 			else
 			{
 				((DojoSelectableListContainer)getComponent()).onChoose(target, null);
 			}
 		}else{
 			if (child instanceof ListView){
 				ListView listView = (ListView) child;
 				List all = listView.getList();
 				int pos;
 				for (int i=0; i < indexList.length; i++){
 					pos = Integer.parseInt(indexList[i]);
 					selected.add(all.get(pos));
 				}
 			}else if (child instanceof RepeatingView){
 				RepeatingView repeatingView = (RepeatingView) child;
 				Iterator ite = repeatingView.iterator();
 				List selectedIndexes = Arrays.asList(indexList);
 				int pos = 0;
 				while (ite.hasNext()){
 					Object element = ite.next();
 					if (selectedIndexes.contains(Integer.toString(pos))){
 						selected.add(((Item)element).getModelObject());
 					}
 					pos++;
 				}
 			}
 			
 			((DojoSelectableListContainer)getComponent()).setSelected(selected);
 			
 			((DojoSelectableListContainer)getComponent()).onSelection(target, selected);
 		}
 	}
 
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderCSSReference(new ResourceReference(DojoSelectableListContainer.class, "DojoSelectableListContainer.css"));
 		response.renderJavascriptReference(new ResourceReference(DojoSelectableListContainer.class, "SelectableTable.js"));
 		if (((DojoSelectableListContainer)getComponent()).getOverrideCssReference() != null){
 			response.renderCSSReference(((DojoSelectableListContainer)getComponent()).getOverrideCssReference());
 		}
 	}
 	
 	/**
 	 * @return javascript that will generate an ajax GET request to this
 	 *         behavior *
 	 * @param recordPageVersion
 	 *            if true the url will be encoded to execute on the current page
 	 *            version, otherwise url will be encoded to execute on the
 	 *            latest page version
 	 */
 	protected final CharSequence getCallbackScript()
 	{
 		return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl(true) + "' + getSelection('"+getComponent().getMarkupId()+"')", null,
 				null);
 	}
 	
 	/**
 	 * return javascript that will be used to respond to Double click
 	 * @return javascript that will be used to respond to Double click
 	 */
 	protected final CharSequence getDoubleClickCallbackScripts(){
 		if (((DojoSelectableListContainer) getComponent()).isAjaxModeOnChoose()){
 			return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl(true) + "'", null,null);
 		}else{
 			CharSequence url = ((DojoSelectableListContainer) getComponent()).urlFor(ILinkListener.INTERFACE);
 			return "window.location.href='" + url + "' + getSelection('"+getComponent().getMarkupId()+"') ";
 		}
 	}
 
 	/**
 	 * Add onSelect and on choose event listener
 	 */
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put("onSelect", getCallbackScript());
 		tag.put("onChoose", getDoubleClickCallbackScripts());
 	}
 

	public WebMarkupContainer getChild()
	{
		return child;
	}


	public void setChild(WebMarkupContainer child)
	{
		this.child = child;
	}

 	
 
 }
