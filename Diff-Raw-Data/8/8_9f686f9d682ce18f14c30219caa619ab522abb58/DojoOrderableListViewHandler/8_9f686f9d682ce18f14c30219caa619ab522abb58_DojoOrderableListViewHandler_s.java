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
 
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * Handler for {@link DojoOrderableListViewContainer}
  * @author Vincent Demay
  *
  */
 public class DojoOrderableListViewHandler extends AbstractRequireDojoBehavior
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
 		response.renderString(generateDragDefinition(getComponent().getMarkupId()));
 
 	}
 	
 	private String generateDragDefinition(String id){
 		String toReturn = "";
 		toReturn += "<script language=\"JavaScript\" type=\"text/javascript\">\n";
 		toReturn += "function initDrag" + id + "(){\n";
 		toReturn += "	var children = document.getElementById('" + id + "').getElementsByTagName('div');\n";
 		toReturn += "	for(var i=0;  children.length > i ; i++){\n";
		toReturn += "		var drag = new dojo.dnd.HtmlDragSource(children[i], children[i].id);\n";
 		toReturn += "	}\n";
 		toReturn += "}\n";
 		toReturn += "dojo.event.connect(dojo, \"loaded\", \"initDrag" + id + "\");\n";
 		toReturn += "</script>\n";
 		return toReturn;
 	}
 
 }
