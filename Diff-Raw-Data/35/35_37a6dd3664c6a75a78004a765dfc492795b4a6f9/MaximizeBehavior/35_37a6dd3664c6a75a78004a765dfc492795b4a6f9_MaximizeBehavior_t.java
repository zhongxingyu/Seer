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
 package wicket.contrib.dojo.markup.html;
 
 import wicket.Component;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * Dojo behaviour that maximizes an HTML element to the whole window's viewport size
  */
 public class MaximizeBehavior extends AbstractRequireDojoBehavior
 {
 	/** container handler is attached to. */
 	private Component container;
 	
 	/**
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		this.container = getComponent();
 	}
 	
 	/**
 	 * @see wicket.contrib.dojo.DojoAjaxHandler#renderHead(wicket.markup.html.IHeaderResponse)
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderJavascript(generateDefinition(), MaximizeBehavior.class.getName());
 	}
 	
 	private String generateDefinition(){
 		StringBuffer toReturn = new StringBuffer();
 		toReturn.append("function maximize(id){\n");
 		toReturn.append("	var el = dojo.byId(id);\n");
 		toReturn.append("   var viewport = dojo.html.getViewport();\n");
		toReturn.append("	dojo.html.setContentBox(el, {width: dojo.html.getContentBox(el).width, height: viewport.height});\n");
 		toReturn.append("}\n");
 		toReturn.append("dojo.addOnLoad( function() { maximize('" + container.getMarkupId() + "'); });\n");
 		return toReturn.toString();
 	}
 	public void setRequire(RequireDojoLibs libs)
 	{
 		libs.add("dojo.html");
 	}
 
 	protected void respond(AjaxRequestTarget target)
 	{
 		// Not used
 	}
 }
