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
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 
 import com.sangupta.jerry.util.HtmlUtils;
 import com.sangupta.shire.ExecutionOptions;
 import com.sangupta.shire.domain.GeneratedResource;
 import com.sangupta.shire.domain.NonRenderableResource;
 import com.sangupta.shire.domain.RenderableResource;
 import com.sangupta.shire.domain.Resource;
 
 /**
  * Class that writes a given {@link ProcessableSiteFile} to the exported site.
  * 
  * @author sangupta
  * @since 0.1.0
  * @added Feb 23, 2012
  */
 public class SiteWriter {
 	
 	private ExecutionOptions options = null;
 	
 	/**
 	 * The source root folder, where all the resources are placed.
 	 * This is also the folder where _config.yml file is found.
 	 */
 	private File sourceFolder = null;
 	
 	/**
 	 * The absolute path of the source folder.
 	 */
 	private String sourceBase = null;
 
 	/**
 	 * File handle to the root of export folder
 	 */
 	private File siteFolder = null;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param options
 	 */
 	public SiteWriter(ExecutionOptions options) {
 		this.options = options;
 		this.sourceFolder = options.getParentFolder().getAbsoluteFile();
 		this.sourceBase = sourceFolder.getAbsolutePath();
 	}
 
 	/**
 	 * Create a new site folder, if it does not exists.
 	 * 
 	 */
 	public void createSiteExportFolder() {
 		File file = new File(this.options.getParentFolder(), this.options.getSiteFolderName());
 		if(file.exists() && file.isDirectory()) {
 			throw new IllegalStateException("Site folder already exists... looks like the backup failed. aborting!");
 		}
 		
 		boolean success = file.mkdirs();
 		if(!success) {
 			throw new RuntimeException("Unable to create site export directory.");
 		}
 		
 		this.siteFolder = file;
 	}
 
 	/**
 	 * Exports the given file to the site export location
 	 * creating all child folders that may be necessary.
 	 * 
 	 * @param siteFile
 	 */
 	public void export(RenderableResource resource, String pageContents) {
 		// start the export process
 		String path = resource.getExportPath();
 		path = siteFolder.getAbsolutePath() + File.separator + path;
 		
 		File exportFile = new File(path);
 
 		pageContents = HtmlUtils.tidyHtml(pageContents);
 
 		try {
 			FileUtils.writeStringToFile(exportFile, pageContents);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void export(Resource resource) {
 		// start the export process
 		String path = resource.getExportPath();
 		path = siteFolder.getAbsolutePath() + File.separator + path;
 		
 		File exportFile = new File(path);
 
 		if(resource instanceof NonRenderableResource) {
 			try {
 				FileUtils.copyFile(resource.getFileHandle(), exportFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			return;
 		}
 		
 		if(resource instanceof GeneratedResource) {
 			System.out.println("Exporting generated resource to: " + exportFile.getAbsolutePath());
 			try {
 				FileUtils.writeStringToFile(exportFile, ((GeneratedResource) resource).getContent());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Extracts the base relative path of the file with respect to the
 	 * source folder of the site.
 	 *  
 	 * @param path
 	 * @return
 	 */
 	public String createBasePath(String path) {
 		if(path.startsWith(sourceBase)) {
 			path = path.substring(sourceBase.length());
 		}
 		
 		return path;
 	}
 
 }
