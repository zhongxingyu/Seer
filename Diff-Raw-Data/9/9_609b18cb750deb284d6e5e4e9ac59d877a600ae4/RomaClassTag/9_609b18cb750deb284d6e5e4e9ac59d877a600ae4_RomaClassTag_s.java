 /*
  * Copyright 2008 Luigi Dell'Aquila (luigi.dellaquila@assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.romaframework.aspect.view.html.taglib;
 
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 import org.romaframework.aspect.view.html.constants.TransformerConstants;
 import org.romaframework.aspect.view.html.transformer.Transformer;
 import org.romaframework.aspect.view.html.transformer.helper.TransformerHelper;
 import org.romaframework.aspect.view.html.transformer.manager.TransformerManager;
 import org.romaframework.aspect.view.html.transformer.plain.HtmlViewPojoTransformer;
 import org.romaframework.core.Roma;
 
 /**
  * This tag renders an entire POJO as an html fragment
  * 
  * @author Luigi Dell'Aquila
  * 
  */
 public class RomaClassTag extends RomaAbstractTab {
 
 	private String	exclude;
 
 	protected String	part;
 
 	public String getPart() {
 		return part;
 	}
 
 	public void setPart(final String part) {
 		this.part = part;
 	}
 	
 	public String getExclude() {
 		return exclude;
 	}
 
 	public void setExclude(final String exclude) {
 		this.exclude = exclude;
 	}
 
 	@Override
 	public int doStartTag() {
 
 		final Object currentForm = pageContext.getRequest().getAttribute(RequestConstants.CURRENT_REQUEST_FORM);
 		if (currentForm != null && currentForm instanceof HtmlViewRenderable) {
 			try {
 				if (part != null && part.equals(TransformerConstants.PART_ID)) {
 					// pageContext.getOut().flush();
 					pageContext.getOut().print(TransformerHelper.getInstance().getHtmlId((HtmlViewRenderable) currentForm, null));
 				} else {
					pageContext.getOut().flush();
 					final TransformerManager transformerManager = Roma.component(TransformerManager.class);
 					final Transformer transformer = transformerManager.getComponent(HtmlViewPojoTransformer.NAME);
 					transformer.transform((HtmlViewRenderable) currentForm, pageContext.getOut());
 				}
 			} catch (final Exception e) {
 				e.printStackTrace();// TODO handle exception
 			}
 			// TODO add exclusion logic
 		}
 		return SKIP_BODY;
 	}
 }
