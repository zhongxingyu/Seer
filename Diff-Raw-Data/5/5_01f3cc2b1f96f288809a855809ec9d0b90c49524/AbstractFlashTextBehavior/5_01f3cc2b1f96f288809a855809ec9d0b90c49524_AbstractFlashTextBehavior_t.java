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
 package org.wicketstuff.sifr;
 
 /**
  * Base class for FlashTextBehavior.
  * 
  * @author Janne Hietam&auml;ki
  * 
  */
 import org.apache.wicket.Component;
 import org.apache.wicket.IClusterable;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.Response;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
 import org.apache.wicket.util.string.JavascriptUtils;
 
 public class AbstractFlashTextBehavior extends AbstractBehavior implements IHeaderContributor
 {
 	private static final long serialVersionUID = 1L;
 
 	private static final ResourceReference SIFR_JS = new JavascriptResourceReference(FlashTextBehavior.class, "sifr.js");
	private static final ResourceReference SIFR_SCREEN_CSS = new ResourceReference(FlashTextBehavior.class, "sIFR-screen.css");
 
	private static final ResourceReference SIFR_PRINT_CSS = new ResourceReference(FlashTextBehavior.class, "sIRF-print.css");
 
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderJavascriptReference(SIFR_JS);
 		response.renderCSSReference(SIFR_SCREEN_CSS, "screen");
 		response.renderCSSReference(SIFR_PRINT_CSS, "print");
 	}
 
 	protected void replaceElement(Component component, String id, FlashTextSettings settings)
 	{
 		Response response = component.getResponse();
 
 		response.write(JavascriptUtils.SCRIPT_OPEN_TAG);
 		response.write("if(typeof sIFR == \"function\")");
 		response.write("sIFR.replaceElement(\"" + id + "\", ");
 		response.write("named({sFlashSrc: \"" + RequestCycle.get().urlFor(settings.font) + "\"");
 
 		writeProperty(response, "sColor", settings.color);
 		writeProperty(response, "sWmode", settings.transparent != null ? "transparent" : null);
 		writeProperty(response, "sLinkColor", settings.linkColor);
 		writeProperty(response, "sHoverColor", settings.hoverColor);
 		writeProperty(response, "sBgColor", settings.bgColor);
 		writeProperty(response, "nPaddingTop", settings.paddingTop);
 		writeProperty(response, "nPaddingRight", settings.paddingRight);
 		writeProperty(response, "nPaddingBottom", settings.paddingBottom);
 		writeProperty(response, "nPaddingLeft", settings.paddingLeft);
 		response.write("}));");
 		response.write(JavascriptUtils.SCRIPT_CLOSE_TAG);
 
 	}
 
 	private void writeProperty(Response response, String key, Object value)
 	{
 		if (value != null)
 			response.write(", " + key + ":\"" + value.toString() + "\"");
 	}
 
 	public static class FlashTextSettings implements IClusterable
 	{
 		private static final long serialVersionUID = 1L;
 
 		ResourceReference font;
 		String color;
 		String linkColor;
 		String hoverColor;
 		String bgColor;
 		Integer paddingTop;
 		Integer paddingRight;
 		Integer paddingBottom;
 		Integer paddingLeft;
 		Boolean transparent;
 
 		public FlashTextSettings(ResourceReference font)
 		{
 			this.font = font;
 		}
 
 		public ResourceReference getFont()
 		{
 			return font;
 		}
 
 		public FlashTextSettings setFont(ResourceReference font)
 		{
 			this.font = font;
 			return this;
 		}
 
 		public String getColor()
 		{
 			return color;
 		}
 
 		public FlashTextSettings setColor(String color)
 		{
 			this.color = color;
 			return this;
 		}
 
 		public String getLinkColor()
 		{
 			return linkColor;
 		}
 
 		public FlashTextSettings setLinkColor(String linkColor)
 		{
 			this.linkColor = linkColor;
 			return this;
 		}
 
 		public String getHoverColor()
 		{
 			return hoverColor;
 		}
 
 		public FlashTextSettings setHoverColor(String hoverColor)
 		{
 			this.hoverColor = hoverColor;
 			return this;
 		}
 
 		public String getBgColor()
 		{
 			return bgColor;
 		}
 
 		public FlashTextSettings setBgColor(String bgColor)
 		{
 			this.bgColor = bgColor;
 			return this;
 		}
 
 		public Integer getPaddingTop()
 		{
 			return paddingTop;
 		}
 
 		public FlashTextSettings setPaddingTop(Integer paddingTop)
 		{
 			this.paddingTop = paddingTop;
 			return this;
 		}
 
 		public Integer getPaddingRight()
 		{
 			return paddingRight;
 		}
 
 		public FlashTextSettings setPaddingRight(Integer paddingRight)
 		{
 			this.paddingRight = paddingRight;
 			return this;
 		}
 
 		public Integer getPaddingBottom()
 		{
 			return paddingBottom;
 		}
 
 		public FlashTextSettings setPaddingBottom(Integer paddingBottom)
 		{
 			this.paddingBottom = paddingBottom;
 			return this;
 		}
 
 		public Integer getPaddingLeft()
 		{
 			return paddingLeft;
 		}
 
 		public FlashTextSettings setPaddingLeft(Integer paddingLeft)
 		{
 			this.paddingLeft = paddingLeft;
 			return this;
 		}
 
 		public Boolean isTransparent()
 		{
 			return transparent;
 		}
 
 		public FlashTextSettings setTransparent(Boolean transparent)
 		{
 			this.transparent = transparent;
 			return this;
 		}
 	}
 }
