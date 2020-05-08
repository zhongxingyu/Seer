 /**
  *
  * Shire - Blog aware static site generator 
  * Copyright (c) 2012, Sandeep Gupta
  * 
  * http://www.sangupta/projects/shire
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.shire.layouts;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang3.StringUtils;
 
import com.sangupta.jerry.util.AssertUtils;
 import com.sangupta.makeup.Makeup;
 import com.sangupta.makeup.layouts.Layout;
 import com.sangupta.makeup.layouts.LayoutType;
 import com.sangupta.shire.ExecutionOptions;
 import com.sangupta.shire.domain.RenderableResource;
 import com.sangupta.shire.model.TemplateData;
 import com.sangupta.shire.util.FrontMatterUtils;
 import com.sangupta.shire.util.ShireUtils;
 
 /**
  * Handles layout for the pages that need transformation.
  * 
  * @author sangupta
  * @since Feb 23, 2012
  */
 public class LayoutManager {
 	
 	private ExecutionOptions options = null;
 	
 	private Layout layout = null;
 	
 	/**
 	 * Keeps track of which all templates are extended templates (with front-matter) so that
 	 * they can be parsed effectively.
 	 */
 	private final Map<String, Boolean> extendedTemplates = new HashMap<String, Boolean>();
 	
 	/**
 	 * Stores front-matter stored with layouts, if any
 	 */
 	private final Map<String, RenderableResource> renderableLayouts = new HashMap<String, RenderableResource>();
 	
 	public LayoutManager(ExecutionOptions options) {
 		this.options = options;
 
 		LayoutType layoutType = options.getLayoutType();
 		switch(layoutType) {
 			case AutoDetect:
 				layoutType = autoDetectLayoutType();
 				break;
 				
 			default:
 				// do nothing
 				break;
 		}
 		
 		// reset-the layout type option
 		this.options.setLayoutType(layoutType);
 	}
 	
 	/**
 	 * Method that detects layout type based on files inside the
 	 * layouts folder.
 	 * 
 	 * @return
 	 */
 	private static LayoutType autoDetectLayoutType() {
 		return LayoutType.Velocity;
 	}
 
 	/**
 	 * Method to read the list of all layouts available in the library
 	 * and figure out the layout type if not specified.
 	 * 
 	 */
 	public void readLayoutsAndIncludes() {
 		final File layouts = ShireUtils.getFolder(options, options.getLayoutsFolderName());
 		final File includes = ShireUtils.getFolder(options, options.getIncludesFolderName());
 		
 		layout = Makeup.getLayout(options.getLayoutType());
 		
 		// set it
 		layout.initialize( new File[] { layouts, includes }, Makeup.getKnownCustomTags());
 	}
 	
 	/**
 	 * Render the given content with the given layout and model data. The content is added under the
 	 * field name of <code>content</code>. 
 	 * 
 	 * @param layoutName
 	 * @param content
 	 * @param templateData
 	 * @return
 	 */
 	public String layoutContent(String layoutName, final String content, final TemplateData templateData) {
 		Boolean extended = this.extendedTemplates.get(layoutName);
 		if(extended == null) {
 			extended = decipherTemplateType(layoutName);
 		}
 		
 		if(!extended) {
 			// this is a simple template, only build the model and render
 			return putContentInTemplate(layoutName, content, templateData);
 		}
 		
 		// this is an extended template
 		// resolve the extension - build the entire template first
 		final RenderableResource resource = this.renderableLayouts.get(layoutName);
 		final Properties properties = resource.getFrontMatter();
 
 		String parentLayoutName = properties.getProperty("layout");
 		
 		// if the layout is null, only then check for default layout naming convention
 		// if the layout is empty - that indicates we do not want any layout applied
 		if(parentLayoutName == null) {
 			parentLayoutName = templateData.getSite().getDefaultLayoutName();
 		}
 		
 		// do as if we are in a normal template
 		if(parentLayoutName == null) {
 			return putContentInTemplate(layoutName, content, templateData);
 		}
 		
 		final Map<String, Object> dataModel = getDataModel(templateData);
 		
 		// we have the parent layout Name
 		// get the actual layout code
 		// get the code in recursion
 		String childTemplateCode = null;
 		try {
 			childTemplateCode = resource.getOriginalContent();
 		} catch (IOException e) {
 			// e.printStackTrace();
 		}
 		
		// dataModel.put("content", childTemplateCode);
		String resolved = this.layoutContent(parentLayoutName, childTemplateCode, templateData);
 		
		if(AssertUtils.isNotEmpty(content)) {
			Map<String, Object> model = new HashMap<String, Object>();
			
			// parse the content for velocity tags
			model.put("content", this.layout.layoutWithTemplateCode(content, dataModel));
			resolved = this.layout.layoutWithTemplateCode(resolved, model);
		}
 		
		return resolved;
 	}
 	
 	/**
 	 * 
 	 * @param layoutName
 	 * @param content
 	 * @param templateData
 	 * @return
 	 */
 	private String putContentInTemplate(String layoutName, final String content, final TemplateData templateData) {
 		if(StringUtils.isEmpty(layoutName)) {
 			return content;
 		}
 		
 		if("nil".equals(layoutName)) {
 			return content;
 		}
 		
 		if(!layoutName.endsWith(".html")) {
 			layoutName = layoutName + ".html";
 		}
 		
 		// build up the final data model
 		final Map<String, Object> dataModel = getDataModel(templateData);
 		
 		// parse the contents of the page itself
 		// only if they are null
 		// content would be null for post pages - where multiple posts are laid out
 		String modifiedContent;
 
 		if(content != null) {
 			modifiedContent = layout.layoutWithTemplateCode(content, dataModel);
 		} else {
 			modifiedContent = content;
 		}
 		
 		// layout the contents
 		dataModel.put("content", modifiedContent);
 		return this.layout.layout(layoutName, dataModel);
 	}
 
 	/**
 	 * Method that detects whether a template is extended or not. This is decided based on the presence
 	 * of front matter in the template.
 	 * 
 	 * @param layoutName
 	 * @return
 	 */
 	private Boolean decipherTemplateType(String layoutName) {
 		if(this.extendedTemplates.containsKey(layoutName)) {
 			return this.extendedTemplates.get(layoutName);
 		}
 		
 		final File layouts = ShireUtils.getFolder(options, options.getLayoutsFolderName());
 		final File includes = ShireUtils.getFolder(options, options.getIncludesFolderName());
 		
 		File layout = new File(layouts, layoutName);
 		
 		// check if it exists
 		// if not, try includes
 		if(!layout.exists()) {
 			layout = new File(includes, layoutName);
 		}
 		
 		if(!layout.exists()) {
 			// the layout does not exist in any of the given folders
 			throw new IllegalArgumentException("No layout exists in layouts/includes");
 		}
 		
 		Properties properties = new Properties();
 		try {
 			int frontMatter = FrontMatterUtils.checkFileHasFrontMatter(layout, properties);
 			if(frontMatter > 0) {
 				this.extendedTemplates.put(layoutName, true);
 				RenderableResource renderableResource = new RenderableResource(layout, layout.getAbsoluteFile().getParentFile().getAbsolutePath(), properties, frontMatter);
 				this.renderableLayouts.put(layoutName, renderableResource);
 				return true;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.extendedTemplates.put(layoutName, false);
 		return false;
 	}
 	
 	/**
 	 * Create a data model out of this {@link TemplateData} object that can be
 	 * used with the {@link Layout}.
 	 * 
 	 * @param data
 	 *            the template data that needs to be converted
 	 * 
 	 * @return the converted data model as a {@link Map}
 	 * 
 	 * @throws <code>NullPointerException</code> if the provided
 	 *         {@link TemplateData} object is null
 	 * 
 	 */
 	private Map<String, Object> getDataModel(final TemplateData data) {
 		final Map<String, Object> model = new HashMap<String, Object>();
 		
 		model.put("site", data.getSite());
 		model.put("page", data.getPage());
 		model.put("paginator", data.getPaginator());
 		
 		return model;
 	}
 
 }
