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
 package org.wicketstuff.dojo.markup.html.form.validation.topdiv;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.wicketstuff.dojo.DojoIdConstants;
 import org.wicketstuff.dojo.widgets.StylingWebMarkupContainer;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.model.IModel;
 
 /**
  * A widget to display error in live on item. the message will be displayed on the top of a field
  * 
  * @author Vincent Demay
  *
  */
 @SuppressWarnings("serial")
 public class DojoErrorDiv extends StylingWebMarkupContainer {
 
 	/**
 	 * Constructs
 	 * @param id
 	 * @param model
 	 */
 	public DojoErrorDiv(String id, IModel model) {
 		super(id, model);
 		add(new DojoErrorDivHandler());
 	}
 
 	/**
 	 * Constructs
 	 * @param id
 	 */
 	public DojoErrorDiv(String id) {
 		super(id);
 		add(new DojoErrorDivHandler());
 	}
 
 	protected void onComponentTag(ComponentTag tag) {
 		super.onComponentTag(tag);
 		tag.put(DojoIdConstants.DOJO_TYPE, DojoIdConstants.DOJO_TYPE_ERRORDIV);
 	}
 	
 	/**
 	 * Show the div
 	 * @param target {@link AjaxRequestTarget}
 	 */
 	public void show(AjaxRequestTarget target){
 		target.appendJavascript("dojo.widget.byId('" + getMarkupId() + "').show()");
 	}
 	
 	/**
 	 * hide the div
 	 * @param target {@link AjaxRequestTarget}
 	 */
 	public void hide(AjaxRequestTarget target){
 		target.appendJavascript("dojo.widget.byId('" + getMarkupId() + "').hide()");
 	}
 	
 	/**
 	 * Place the div on the component c
 	 * @param target {@link AjaxRequestTarget}
 	 * @param c component where to show the bubble
 	 */
 	public void place(AjaxRequestTarget target, Component c){
 		target.appendJavascript("dojo.widget.byId('" + getMarkupId() + "').stickTo('" + c.getMarkupId() + "')");
 	}
 	
 	/**
 	 * Set the message
 	 * @param target {@link AjaxRequestTarget}
 	 * @param mess message to display
 	 */
 	public void setMessage (AjaxRequestTarget target, String mess){
		target.appendJavascript("dojo.widget.byId('" + getMarkupId() + "').setMessage(\"" + mess + "\")");
 	}
 
 }
