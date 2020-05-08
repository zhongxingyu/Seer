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
 package wicket.contrib.dojo.markup.html.list;
 
 import java.util.List;
 
 import wicket.MarkupContainer;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.dojodnd.DojoDragContainer;
 import wicket.contrib.dojo.dojodnd.DojoDropContainer;
 
 
 /**
  * DojoOrderableListViewContainer should contains a {@link DojoOrderableListView} and nothing else.
  * This container and it child allow top make a DragAndDropableList. Model associated with the {@link DojoOrderableListView}
  * will be automaticaly updated during dnd
  * <p>
  * <b>Sample</b>
  * <pre>
  * public class OrderableList extends WebPage {
  * 
  * 	static final List objList  = new  ArrayList();
  * 
  * 	
  * 	public OrderableList() {
  * 		super();
  * 		if (objList.size() == 0){
  * 			objList.add("foo1");
  * 			objList.add("bar1");
  * 			objList.add("foo2");
  * 			objList.add("bar2");
  * 			objList.add("foo3");
  * 			objList.add("bar3");
  * 			objList.add("foo4");
  * 			objList.add("bar4");
  * 			objList.add("foo5");
  * 			objList.add("bar5");
  * 			objList.add("foo6");
  * 			objList.add("bar6");
  * 		}
  * 		DojoOrderableListViewContainer container = new DojoOrderableListViewContainer("container");
  * 		add(container);
  * 		DojoOrderableListView list = new DojoOrderableListView(container, "list", objList){
  * 
  * 			protected void populateItem(ListItem item) {
  * 				item.add(new Label("label",(String)item.getModelObject()));
  * 				item.add(new DojoOrderableListRemover("remover", item));
  * 				
  * 			}
  * 			
  * 		};
  * 		container.add(list);
  * 	}
  * 
  * }
  * </pre>
  * </p>
  * @author Vincent Demay
  */
 public class DojoOrderableListViewContainer extends DojoDropContainer
 {
 
 	/**
 	 * Construct
 	 * @param id id
 	 */
 	public DojoOrderableListViewContainer(String id)
 	{
 		super(id);
 	}
 
 
 	/**
 	 * 
 	 * @param container
 	 * @param oldPosition
 	 * @param newPosition
 	 * @param target
 	 */
 	public void onDrop(DojoOrderableListView container,  int oldPosition, int newPosition, AjaxRequestTarget target)
 	{
 		if (oldPosition != newPosition){
 			List list = container.getList();
 			Object drag = list.remove(oldPosition);
 			list.add(newPosition, drag);
 			container.setList(list);
 			target.appendJavascript(getChangeIDScript());
 		}
 		onDrop(null, newPosition);
 	}
 	
 	private String getChangeIDScript(){
 		String containerId = getMarkupId();
 		String changeId = "";
 		changeId += "var children = document.getElementById('" + containerId + "').getElementsByTagName('div');\n";
 		changeId += "for(var i=0; children.length > i ; i++){\n";
 		changeId += "	children[i].id = '" + containerId + "_" + "list" + "_'+i\n";   //FIXME : replace List it is not very Generic
 		changeId += "}";
 		return changeId;
 		
 	}
 	
 	/**
 	 * Called after the model is updated. Use this method to e.g. update the
 	 * persistent model. Does nothing by default.
 	 */
 	protected void onAjaxModelUpdated(AjaxRequestTarget target)
 	{
 		String dragSource = getRequest().getParameter("dragSource");
 		int newPosition = Integer.parseInt(getRequest().getParameter("position"));
 		MarkupContainer container = getPage(); 
 		String[] ids = dragSource.split("_");
 		for (int i=0; i < ids.length - 1; i++){
 			container = (MarkupContainer)container.get(ids[i]);
 		}
 		int oldPosition = Integer.parseInt(ids[ids.length - 1]);
 		onDrop((DojoOrderableListView) container, oldPosition, newPosition, target);  
 	}
 
 	/**
 	 * Event Triggered when an item is dnd
 	 * @param container DragMoved
 	 * @param position odl DragMoved position
 	 */
 	public void onDrop(DojoDragContainer container, int newPosition){}
 
 }
