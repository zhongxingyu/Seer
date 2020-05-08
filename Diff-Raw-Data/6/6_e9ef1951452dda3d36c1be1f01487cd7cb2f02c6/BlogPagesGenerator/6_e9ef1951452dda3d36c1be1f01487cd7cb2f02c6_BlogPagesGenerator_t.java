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
 
 package com.sangupta.shire.generators;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.sangupta.jerry.util.AssertUtils;
 import com.sangupta.shire.Shire;
 import com.sangupta.shire.core.Generator;
 import com.sangupta.shire.domain.GeneratedResource;
 import com.sangupta.shire.domain.RenderableResource;
 import com.sangupta.shire.model.FrontMatterConstants;
 import com.sangupta.shire.model.Page;
 import com.sangupta.shire.model.Paginator;
 import com.sangupta.shire.model.PostComparatorOnDate;
 import com.sangupta.shire.model.ResourceComparatorOnDate;
 import com.sangupta.shire.model.TagOrCategory;
 import com.sangupta.shire.model.TemplateData;
 
 /**
  * Generator that generates pagination pages for blogs configured
  * in the site. 
  * 
  * @author sangupta
  *
  */
 public class BlogPagesGenerator implements Generator {
 	
 	private static final String PAGINATION_LAYOUT_FOR_POSTS = "post-listing.html";
 	
 	private static final String BLOG_ARCHIVE_LAYOUT = "blog-archive.html";
 	
 	private static final int BATCH_SIZE = 5;
 	
 	/**
 	 * The instance of the site which is being built
 	 */
 	private Shire shire = null;
 	
 	/**
 	 *
 	 * @see com.sangupta.shire.core.Generator#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Blog pages";
 	}
 
 	/**
 	 *
 	 * @see com.sangupta.shire.core.Generator#runBeforeResourceProcessing()
 	 */
 	@Override
 	public boolean runBeforeResourceProcessing() {
 		return false;
 	}
 
 	/**
 	 *
 	 * @see com.sangupta.shire.core.Generator#execute(com.sangupta.shire.model.TemplateData, java.util.List, com.sangupta.shire.site.SiteWriter)
 	 */
 	@Override
 	public void execute(Shire shire) {
 		// set the shire object so that we can execute all methods downstream
 		this.shire = shire;
 		
 		// check if we have some blog folders in there
 		List<String> blogFolders = new ArrayList<String>();
 		for(File dotFile : shire.getSiteDirectory().getDotFiles()) {
 			if(dotFile.getName().equals(".blog")) {
 				blogFolders.add(dotFile.getParentFile().getAbsolutePath());
 			}
 		}
 		
 		if(blogFolders.isEmpty()) {
 			// nothind to do
 			return;
 		}
 		
 		// start building pages for each individual blog in there
 		try {
 			processBlogResources(blogFolders);
 		} catch (IOException e) {
 			System.out.println("Unable to generate blog pages for pagination, categorites, tags, and year-month archives.");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Process all resources that are marked under the <code>.blog</code> folder
 	 * to generate additional pages.
 	 *  
 	 * @param blogFolders
 	 * @param resources
 	 * @param siteWriter 
 	 * @param model 
 	 * @throws IOException 
 	 */
 	private void processBlogResources(List<String> blogFolders) throws IOException {
 		List<RenderableResource> resources = this.shire.getSiteDirectory().getRenderableResources();
 		
 		Map<String, List<RenderableResource>> pages = getBlogPages(blogFolders, resources);
 		TemplateData model = this.shire.getTemplateData();
 		
 		// start processing each blog individually
 		for(String blog : blogFolders) {
 			List<RenderableResource> list = pages.get(blog);
 			processBlog(blog, list, model);
 		}
 	}
 	
 	/**
 	 * @param blogPath the absolute base path to the root of the blog folder
 	 * @param list the list of all posts in this blog
 	 * @throws IOException 
 	 */
 	private void processBlog(final String blogPath, List<RenderableResource> list, TemplateData model) throws IOException {
 		if(list == null || list.isEmpty()) {
 			return;
 		}
 		
 		// fetch the blog name from the folder
 		final String blogName = FileUtils.readFileToString(new File(blogPath + "/.blog"));
 		
 		// sort the list in reverse chronological order
 		Collections.sort(list, new ResourceComparatorOnDate()); 
 		
 		// extract all individual tag names, category names, and dates for archives
 		List<TagOrCategory> tags = extractAllTagsOrCategories(list, FrontMatterConstants.TAGS, blogPath);
 		List<TagOrCategory> categories = extractAllTagsOrCategories(list, FrontMatterConstants.CATEGORIES, blogPath);
 		
 		// sort the collections on name
 		Collections.sort(tags);
 		Collections.sort(categories);
 		
 		// add these to model
 		model.getSite().setTags(tags);
 		model.getSite().setCategories(categories);
 		
 		// build all the pagination pages
 		final List<Page> allPages = new ArrayList<Page>();
 		
 		for(RenderableResource rr : list) {
 			allPages.add(rr.getResourcePost());
 		}
 		
 		// set all posts in this model
 		model.getSite().addAllPosts(allPages);
 		
 		// and sort them for reverse chronological order
 		model.getSite().sortPosts();
 		
 		doPaginationPages(blogName, blogPath, allPages, model);
 		
 		// build all the blog pages - archives
 		
 		// build all tag and categories pages
 		for(TagOrCategory tag : tags) {
 			doPaginationPages(blogName, tag.getBaseURL(), tag.getPosts(), model);
 		}
 		
 		for(TagOrCategory cat : categories) {
 			doPaginationPages(blogName, cat.getBaseURL(), cat.getPosts(), model);
 		}
 		
 		// generate the single archive page
 		createBlogArchive(blogName, model, allPages, blogPath + "/archive.html");
 	}
 
 	/**
 	 * Create the blog archive page that contains the complete list of all posts
 	 * ever made in the site.
 	 * 
 	 * @param model
 	 */
 	private void createBlogArchive(final String blogName, final TemplateData model, final List<Page> allPosts, final String filePath) {
//		model.getSite().getPosts().clear();
//		model.getSite().addAllPosts(allPosts);
//		model.getSite().sortPosts();
 		
 		// clear up the page data
 		Page page = new Page(null, null);
 		page.setTitle(blogName);
 		model.setPage(page);
 
 		model.setPaginator(null);
 		
 		// put the model into the actual template
 		String content = this.shire.getLayoutManager().layoutContent(BLOG_ARCHIVE_LAYOUT, null, model); 
 		
 		// create the resource to export
 		GeneratedResource resource = new GeneratedResource(this.shire.getSiteWriter().createBasePath(filePath), content);
 		
 		// export the page
 		this.shire.getSiteWriter().export(resource);
 	}
 
 	/**
 	 * @param list
 	 * @return
 	 */
 	private List<TagOrCategory> extractAllTagsOrCategories(List<RenderableResource> list, final String propertyName, final String blogPath) {
 		List<TagOrCategory> result = new ArrayList<TagOrCategory>();
 		
 		// create a single re-usable object to prevent excessive
 		// garbage collection
 		TagOrCategory tagOrCategory; // = new TagOrCategory();
 		
 		// build up a list of all details
 		for(RenderableResource resource : list) {
 			String categoryLine = resource.getFrontMatterProperty(propertyName);
 			if(AssertUtils.isEmpty(categoryLine)) {
 				continue;
 			}
 			
 			String[] tokens = StringUtils.split(categoryLine, FrontMatterConstants.TAG_CATEGORY_SEPARATOR);
 			for(String token : tokens) {
 				token = token.trim();
 				if(!token.isEmpty()) {
 					tagOrCategory = new TagOrCategory();
 					tagOrCategory.setName(token);
 					
 					int index = result.indexOf(tagOrCategory);
 					if(index == -1) {
 						tagOrCategory = new TagOrCategory(token, buildTagOrCategoryUrl(blogPath, propertyName, token));
 						result.add(tagOrCategory);
 					} else {
 						tagOrCategory = result.get(index);
 					}
 					
 					tagOrCategory.addPost(resource.getResourcePost());
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * @param category
 	 * @return
 	 */
 	private String buildTagOrCategoryUrl(final String blogPath, final String folder, final String name) {
 		return this.shire.getSiteWriter().createBasePath(blogPath + "/" + folder + "/" + name);
 	}
 
 	/**
 	 * @param blogName
 	 * @param basePath
 	 * @param list
 	 * @param model
 	 */
 	private void doPaginationPages(final String blogName, final String basePath, List<Page> list, TemplateData model) {
 		// sort this list based on chronologically descending order
 		Collections.sort(list, new PostComparatorOnDate());
 		
 		// create the home page with the top 5 entries
 		int batches = (list.size() / BATCH_SIZE);
 		if(list.size() % BATCH_SIZE > 0) {
 			batches++;
 		}
 		
 		for(int index = 0; index < batches; index++) {
 			int lastIndex = (index + 1) * BATCH_SIZE;
 			if(lastIndex > list.size()) {
 				lastIndex = list.size();
 			}
 			
 			List<Page> pages = list.subList(index * BATCH_SIZE, lastIndex);
 			if(pages.size() == 0) {
 				continue;
 			}
 			
 			String name;
 			// decide the name
 			if(index == 0) {
 				name = basePath + "/index.html";
 			} else {
 				name = basePath + "/page" + index + ".html";
 			}
 			
 			int nextPage = index + 1;
 			if(index == (batches - 1)) {
 				nextPage = -1;
 			}
 			
 			try {
 				pushBatchPage(blogName, pages, model, name, index, nextPage, batches);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// generate the single archive page for this particular tag
 		createBlogArchive(blogName, model, list, basePath + "/archive.html");
 	}
 
 	/**
 	 * @param pages
 	 * @param model
 	 * @param siteWriter
 	 * @param name
 	 * @throws IOException 
 	 */
 	private void pushBatchPage(final String blogName, final List<Page> pages, final TemplateData model, final String name, final int currentPage, final int nextPage, final int totalNumberOfPages) throws IOException {
 		// build the model template
 		Paginator paginator = new  Paginator();
 		List<Page> posts = new ArrayList<Page>();
 		
 		model.setPaginator(paginator);
 		
 		// set all the variables for posts as described in
 		// https://github.com/mojombo/jekyll/wiki/Template-Data#Paginator
 		paginator.setPerPage(BATCH_SIZE);
 		paginator.setPosts(posts);
 		paginator.setTotalPosts(pages.size());
 		paginator.setTotalPages(totalNumberOfPages);
 		paginator.setPage(currentPage);
 		if(currentPage < totalNumberOfPages) {
 			paginator.setNextPage(nextPage);
 		} else {
 			paginator.setNextPage(-1);
 		}
 		paginator.setPreviousPage(currentPage - 1);
 		
 		// clear up the page data
 		Page page = new Page(null, null);
 		page.setTitle(blogName);
 		model.setPage(page);
 		
 		// build the data for each page
 		String content;
 		for(Page post : pages) {
 			posts.add(post);
 		}
 		
 		// put the model into the actual template
 		content = this.shire.getLayoutManager().layoutContent(PAGINATION_LAYOUT_FOR_POSTS, null, model); 
 		
 		// create the resource to export
 		GeneratedResource resource = new GeneratedResource(this.shire.getSiteWriter().createBasePath(name), content);
 		
 		// export the page
 		this.shire.getSiteWriter().export(resource);
 	}
 
 	/**
 	 * Construct a list of all post pages per blog.
 	 * 
 	 * @param blogFolders
 	 * @param resources
 	 * @return
 	 */
 	private Map<String, List<RenderableResource>> getBlogPages(List<String> blogFolders, List<RenderableResource> resources) {
 		Map<String, List<RenderableResource>> blogPages = new HashMap<String, List<RenderableResource>>();
 		
 		for(String blogFolder : blogFolders) {
 			List<RenderableResource> list = new ArrayList<RenderableResource>();
 			blogPages.put(blogFolder, list);
 			
 			for(RenderableResource renderableResource : resources) {
 				String filePath = renderableResource.getFileHandle().getAbsolutePath();
 				if(filePath.startsWith(blogFolder)) {
 					list.add(renderableResource);
 				}
 			}
 		}
 		
 		return blogPages;
 	}
 
 }
