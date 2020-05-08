 /**
  * Copyright 2011-2012 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 /**
  * 
  */
 package fr.dutra.confluence2wordpress.macro;
 
 import java.util.Map;
 
 import com.atlassian.confluence.content.render.xhtml.ConversionContext;
 import com.atlassian.confluence.core.ContentEntityObject;
 import com.atlassian.confluence.macro.Macro;
 import com.atlassian.confluence.macro.MacroExecutionException;
 import com.atlassian.confluence.setup.settings.SettingsManager;
 
 import fr.dutra.confluence2wordpress.core.toc.Heading;
 import fr.dutra.confluence2wordpress.core.toc.TOCBuilder;
 import fr.dutra.confluence2wordpress.core.toc.TOCException;
 import fr.dutra.confluence2wordpress.core.velocity.VelocityHelper;
 import fr.dutra.confluence2wordpress.util.UrlUtils;
 
 
 public class TOCMacro implements Macro {
 
 	private SettingsManager settingsManager;
 
 	private VelocityHelper velocityHelper = new VelocityHelper();
 
 	private static final String ORDERED = "ordered";
 	
 	public TOCMacro(SettingsManager settingsManager) {
 		this.settingsManager = settingsManager;
 	}
 
 	@Override
 	public String execute(Map<String, String> paramMap, String paramString, ConversionContext paramConversionContext) throws MacroExecutionException {
 		ContentEntityObject page = paramConversionContext.getEntity();
 		String storage = page.getBodyAsString();
 		TOCBuilder builder = new TOCBuilder();
 		Heading toc;
 		try {
 			toc = builder.buildTOC(storage, paramConversionContext.getPageContext());
 		} catch (TOCException e) {
 			throw new MacroExecutionException(e);
 		}
 		Boolean ordered = Boolean.valueOf(paramMap.get(ORDERED));
 		String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
 		String absolute = UrlUtils.absolutize(page.getUrlPath(), baseUrl);
 		return velocityHelper.generateTOC(toc, ordered, absolute);
 	}
 
 	@Override
 	public BodyType getBodyType() {
 		return BodyType.NONE;
 	}
 
 	@Override
 	public OutputType getOutputType() {
 		return OutputType.BLOCK;
 	}
 
 }
