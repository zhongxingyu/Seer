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
 
 package com.sangupta.shire.site;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import com.sangupta.shire.Shire;
 import com.sangupta.shire.core.Generator;
 import com.sangupta.shire.domain.NonRenderableResource;
 import com.sangupta.shire.domain.RenderableResource;
 import com.sangupta.shire.generators.BlogPagesGenerator;
 import com.sangupta.shire.generators.SiteMapGenerator;
 import com.sangupta.shire.model.Page;
 import com.sangupta.shire.model.TemplateData;
 
 public class SiteBuilder {
 
 	/**
 	 * List of registered generators
 	 */
 	private static final List<Generator> generators = new ArrayList<Generator>();
 	
 	static {
 		// add all default generators
 		generators.add(new SiteMapGenerator());
 		generators.add(new BlogPagesGenerator());
 	}
 	
 	/**
 	 * The shire instance over which this site builder is running
 	 */
 	private Shire shire = null;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param shire
 	 */
 	public SiteBuilder(Shire shire) {
 		this.shire = shire;
 	}
 
 	/**
 	 * Process the site.
 	 * 
 	 */
 	public void buildSite() {
 		// read all available layouts
 		// as given in _layouts folder
 		// and in _includes folder
 		this.shire.getLayoutManager().readLayoutsAndIncludes();
 		
 		// rename the older _site folder
 		// to create a backup
 		// we will delete it, if all goes all well
 		this.shire.getSiteBackup().backupOlderSite();
 		
 		boolean success = false;
 		try {
 			processSite();
 			success = true;
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		this.shire.getSiteBackup().performHouseKeeping(success);
 	}
 	
 	private void processSite() {
 		// build a list of all tags and categories
 		// that will be used site-wide
 		this.shire.getTemplateData().extractFromResources();
 		
 		// export all non-renderable resources
 		exportNonRenderableResources();
 
 		// build a list of all folders
 		// exclude the _includes, _layouts, _plugins and _site folders
 		processResources();
 		
 		// call all generators
 		// and pass these the needed values
 		invokeGenerators();
 		
 	}
 	
 	/**
 	 * @param nonRenderableResources
 	 */
 	private void exportNonRenderableResources() {
 		List<NonRenderableResource> nonRenderableResources = this.shire.getSiteDirectory().getNonRenderableResources();
 		for(NonRenderableResource nr : nonRenderableResources) {
 			this.shire.getSiteWriter().export(nr);
 		}
 	}
 
 	/**
 	 * Process each individual page from the site folder and export it.
 	 * 
 	 * @param resources
 	 */
 	private void processResources() {
 		List<RenderableResource> resources = this.shire.getSiteDirectory().getRenderableResources();
 		System.out.println("Found " + resources.size() + " files to process.");
 		
 		// start processing each file
 		for(RenderableResource resource : resources) {
 			// process the resource as needed
 			try {
 				renderPage(resource);
 			} catch (IOException e) {
 				System.out.println("Unable to process site resource: " + resource.getExportPath());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Method that processes one given file and renders it
 	 * to the destination site.
 	 * 
 	 * @param file
 	 * @throws IOException 
 	 */
 	private void renderPage(RenderableResource resource) throws IOException {
 		System.out.println("Processing " + resource.getExportPath() + "...");
 		
 		TemplateData templateData = this.shire.getTemplateData();
 		
 		String layoutName = null;
 		Properties frontMatter = null;
 
 		// if site has front matter
 		if(resource.hasFrontMatter()) {
 			// process the front matter
 			frontMatter = resource.getFrontMatter();
 
 			layoutName = frontMatter.getProperty("layout");
 			
 			// if the layout is null, only then check for default layout naming convention
 			// if the layout is empty - that indicates we do not want any layout applied
 			if(layoutName == null) {
 				layoutName = templateData.getSite().getDefaultLayoutName();
 			}
 		}
 		
 		// this also makes sure that the page
 		// attribute has been created
 		templateData.mergePageFrontMatter(frontMatter);
 		
 		if(layoutName != null) {
 			String content = resource.getOriginalContent();
 			
 			// add the unrendered content
 			Page page = templateData.getPage();
 			page.setContent(content);
 			page.setUrl(resource.getUrl());
 			
 			page.postProcessProperties();
 			
 			// now see if the page actually needs to be published
 			if(!page.isPublished()) {
 				// processing of this page was stopped
 				return;
 			}
 
 			// get the converted content in HTML
 			content = resource.getConvertedContent();
 			
 			// work out this HTML content with the current model data
 			try {
 				content = this.shire.getLayoutManager().layoutContent(layoutName, content, templateData);
 			} catch(Exception e) {
 				// eat up
 				System.out.println("Unable to layout file in template: " + resource.getFileHandle().getAbsolutePath());
 			}
 			
 			// now that we are done
 			// we need to write the file back to the destination
 			this.shire.getSiteWriter().export(resource, content);
 			
 			return;
 		}
 		
 		System.out.println("No layout name specified for file... copying it as a resource");
 	}
 	
 	/**
 	 * Method that invokes all available generators on site.
 	 * 
 	 */
 	private void invokeGenerators() {
 		if(SiteBuilder.generators == null || SiteBuilder.generators.size() == 0) {
 			// nothing to do
 			return;
 		}
 		
 		for(Generator generator : SiteBuilder.generators) {
 			System.out.println("Invoking generator: " + generator.getName());
 			
 			generator.execute(this.shire);
 		}
 	}
 
 }
