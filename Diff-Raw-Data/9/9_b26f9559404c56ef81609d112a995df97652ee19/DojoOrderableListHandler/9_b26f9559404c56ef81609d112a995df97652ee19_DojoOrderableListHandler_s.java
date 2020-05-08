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
 
 import java.util.HashMap;
 
 import wicket.IRequestTarget;
 import wicket.RequestCycle;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.contrib.dojo.templates.DojoPackagedTextTemplate;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * Handler for {@link DojoOrderableListContainer}
  * @author Vincent Demay
  *
  */
 public class DojoOrderableListHandler extends AbstractRequireDojoBehavior
 {
 
 	public void setRequire(RequireDojoLibs libs)
 	{
 
 	}
 
 	protected void respond(AjaxRequestTarget target)
 	{
 	
 	}
 
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		
 		DojoPackagedTextTemplate template = new DojoPackagedTextTemplate(this.getClass(), "DojoOrderableListHandlerTemplate.js");
 		
 		HashMap map = new HashMap();
		map.put("MarkupId", getComponent().getMarkupId());
		response.renderJavascript(template.asString(map), template.getWidgetUniqueKey(this.getComponent()));
 	
 		IRequestTarget target = RequestCycle.get().getRequestTarget();
 		if(!(target instanceof AjaxRequestTarget)){
			response.renderJavascript("dojo.event.connect(dojo, \"loaded\", \"initDrag" + getComponent().getMarkupId() + "\");\n", getComponent().getMarkupId() + "onLoad");
 		}
 	}
 	
 
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		super.onComponentReRendered(ajaxTarget);
		ajaxTarget.appendJavascript("initDrag" + getComponent().getMarkupId() + "();");
 	}
 
 }
