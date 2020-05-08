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
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.wicketstuff.dojo.AbstractRequireDojoBehavior;
 
 /**
  * Ajaxhandler to be bound to FormComponents.<br/> This handler executes wicket
  * validation on set event (e.g. onblur, onchange) with an Ajax server call.<br/>
  * The Form component on error is displayed with a div on it containing the error message
  * 
  * 
  * @author Vincent Demay
  */
 @SuppressWarnings("serial")
 public class DojoErrorDivValidationAjaxBehavior extends AbstractRequireDojoBehavior
 {
 
 	/** name event, like onblur. */
 	private final String eventName;
 
 	/** component this handler is attached to. */
 	private FormComponent formComponent;
 	
 	/** bubble */
 	DojoErrorDiv error;
 
 
 	/**
 	 * Default constructor which uses node's current background color when
 	 * component is valid.
 	 * 
 	 * @param eventName
 	 * @see #eventName
 	 */
 	public DojoErrorDivValidationAjaxBehavior(String eventName, DojoErrorDiv error)
 	{
 		if (eventName == null)
 		{
 			throw new NullPointerException("argument eventName must be not null");
 		}
 		this.eventName = eventName;
 		this.error = error;
 	}
 
 	
 	public final void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 	}
 	
 	/**
 	 * Bind this handler to the FormComponent and set the corresponding HTML id
 	 * attribute.
 	 * @param tag tag
 	 */
 	protected void onComponentTag(ComponentTag tag){
 		super.onComponentTag(tag);
 
 		getComponent().setOutputMarkupId(true);
 		
 		Component c = getComponent();
 		if (!(c instanceof FormComponent))
 		{
 			throw new WicketRuntimeException("This handler must be bound to FormComponents");
 		}
 		formComponent = (FormComponent)c;
 		tag.put(eventName, "javascript:" + "var wcall=wicketAjaxGet('" + getCallbackUrl() + "&amp;" + formComponent.getInputName() + "=' + this.value, function() { }, function() { });return !wcall;");
 		if (!eventName.equals("onclick")){
 			tag.put("onclick", "javascript:" + "var wcall=wicketAjaxGet('" + getCallbackUrl() + "&amp;" + formComponent.getInputName() + "=' + this.value, function() { }, function() { });return !wcall;");
 		}
 	}
 
 
 	protected void respond(AjaxRequestTarget target)
 	{
 		formComponent.validate();	
 		if (!formComponent.isValid())
 		{
 			target.appendJavascript(
 					"dojo.html.addClass(dojo.byId('" + formComponent.getMarkupId() + "'), 'dojoWiketValidationError')"
 					);
 					
			error.setMessage(target, formComponent.getFeedbackMessage().getMessage().toString());
 			error.show(target);
 			error.place(target, formComponent);
 			
 		}
 		else{
 			target.appendJavascript(
 					"dojo.html.removeClass(dojo.byId('" + formComponent.getMarkupId() + "'), 'dojoWiketValidationError')"
 					);
 			
 			error.hide(target);
 		}
 	}
 
 	public void setRequire(RequireDojoLibs libs) {
 		libs.add("dojoWicket.widget.ErrorDisplayer");
 		libs.add("dojo.html.*");
 	}
 }
