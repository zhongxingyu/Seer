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
 
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 import org.romaframework.aspect.view.html.constants.TransformerConstants;
 import org.romaframework.aspect.view.html.exception.HtmlViewTagExceptionProcessingException;
 import org.romaframework.aspect.view.html.transformer.helper.TransformerHelper;
 
 /**
  * This tag renders the attribute of a POJO as an html fragment
  * 
  * @author Luigi Dell'Aquila
  * 
  */
 public class RomaFieldTag extends RomaAbstractTab {
 
 	protected String	name;
 
 	protected boolean	js	= true;
 
 	protected boolean	css	= true;
 
 	protected String	part;
 
 	public String getPart() {
 		return part;
 	}
 
 	public void setPart(final String part) {
 		this.part = part;
 	}
 
 	@Override
 	public int doStartTag() {
 		final Object currentForm = pageContext.getRequest().getAttribute(RequestConstants.CURRENT_REQUEST_FORM);
 		if (currentForm != null && currentForm instanceof HtmlViewConfigurableEntityForm) {
 			try {
 				final String[] attributePath = getName().split("\\.");
 				HtmlViewConfigurableEntityForm form = (HtmlViewConfigurableEntityForm) currentForm;
 				HtmlViewAbstractComponent result = null;
 				for (int i = 0; i < attributePath.length; i++) {
 					final HtmlViewAbstractComponent child = (HtmlViewAbstractComponent) form.getChildComponent(attributePath[i]);
 					if (i < attributePath.length - 1 && !(child instanceof HtmlViewConfigurableEntityForm)) {
 						throw new HtmlViewTagExceptionProcessingException("Error processing field " + getName());
 					}
 					if (i < attributePath.length - 1) {
 						form = (HtmlViewConfigurableEntityForm) child;
 					}
 					result = child;
 				}
 				if (result != null) {
 					if (part != null && part.equals(TransformerConstants.PART_ID)) {
 //						pageContext.getOut().flush();
 						pageContext.getOut().print(TransformerHelper.getInstance().getHtmlId(result, null));
 					} else if (part != null) {
 						// pageContext.getOut().println(result.renderPart(part));
 //						pageContext.getOut().flush();
 						
 						result.renderPart(part, pageContext.getOut());
 					} else {
 						// pageContext.getOut().println(result.render());
 //						pageContext.getOut().flush();
 						result.renderPart(part, pageContext.getOut());
 					}
 				}
				pageContext.getOut().flush();
 			} catch (final Exception e) {
 				log.error("Error processing field " + getName(), e);
 				try {
 					pageContext.getOut().print("Error processing field " + getName() + ": " + e);
 				} catch (final Exception ex) {
 				}
 			}
 		}
 		return SKIP_BODY;
 	}
 
 	/**
 	 * returns the name of the attribute represented by this tag
 	 * 
 	 * @return the name of the attribute represented by this tag
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * sets the name of the attribute represented by this tag <br/>
 	 * You can use dot-separated syntax to navigate POJO attributes
 	 * 
 	 * @param name
 	 *          the name of the attribute represented by this tag
 	 */
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public boolean isJs() {
 		return js;
 	}
 
 	public void setJs(final boolean js) {
 		this.js = js;
 	}
 
 	public boolean isCss() {
 		return css;
 	}
 
 	public void setCss(final boolean css) {
 		this.css = css;
 	}
 }
