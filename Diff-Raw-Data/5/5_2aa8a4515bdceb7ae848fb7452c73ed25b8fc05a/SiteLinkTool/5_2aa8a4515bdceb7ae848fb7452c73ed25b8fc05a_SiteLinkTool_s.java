 /*******************************************************************************
  * Poor Man's CMS (pmcms) - A very basic CMS generating static html pages.
  * http://pmcms.sourceforge.net
  * Copyright (C) 2004-2013 by Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  ******************************************************************************/
 package de.thischwa.pmcms.view.context.object;
 
 import java.io.File;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import de.thischwa.pmcms.Constants;
 import de.thischwa.pmcms.configuration.InitializationManager;
 import de.thischwa.pmcms.configuration.PropertiesManager;
 import de.thischwa.pmcms.exception.FatalException;
 import de.thischwa.pmcms.livecycle.PojoHelper;
 import de.thischwa.pmcms.model.InstanceUtil;
 import de.thischwa.pmcms.model.domain.OrderableInfo;
 import de.thischwa.pmcms.model.domain.PoInfo;
 import de.thischwa.pmcms.model.domain.PoPathInfo;
 import de.thischwa.pmcms.model.domain.pojo.Level;
 import de.thischwa.pmcms.model.domain.pojo.Page;
 import de.thischwa.pmcms.tool.PathTool;
 import de.thischwa.pmcms.view.ViewMode;
 import de.thischwa.pmcms.view.context.IContextObjectNeedPojoHelper;
 import de.thischwa.pmcms.view.context.IContextObjectNeedViewMode;
 import de.thischwa.pmcms.view.renderer.RenderData;
 
 /**
  * Context objects for building links to user relevant resources. The resources must be inside the site folder
  * defined in the property 'pmcms.site.dir.layoutresources', except it's defined with #{@link SiteLinkTool#addResource(String)}.
  */
 @Component("sitelinktool")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class SiteLinkTool implements IContextObjectNeedPojoHelper, IContextObjectNeedViewMode {
 	private String linkToResourceString = null;
 	private Level currentLevel = null;
 	private boolean isExportView;
     private PojoHelper pojoHelper; 
 	private File siteDir;
 	private String welcomePageName;
 	
 	@Autowired private PropertiesManager propertiesManager;
 	@Autowired private RenderData renderData;
 
 	@Override
 	public void setPojoHelper(final PojoHelper pojoHelper) {
 		this.pojoHelper = pojoHelper;
 		this.currentLevel = pojoHelper.getLevel();
 		siteDir = PoPathInfo.getSiteDirectory(pojoHelper.getSite());
 		welcomePageName = propertiesManager.getSiteProperty("pmcms.site.export.file.welcome");
 	}
 
 	@Override
 	public void setViewMode(final ViewMode viewMode) {
 		isExportView = (viewMode == ViewMode.EXPORT);
 	}
     	
 
 	/**
 	 * Set the link to a resource file of the site.
 	 * 
 	 * @param resource The name of the resource file, e.g. 'format.css', 'js/jquery.js'
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getResource(final String resource) {
 		String res = String.format("%s/%s", propertiesManager.getSiteProperty("pmcms.site.dir.layoutresources"), resource);
 		if (isExportView) {
 			setResource(PathTool.getURLRelativePathToRoot(this.currentLevel).concat(res));
 			addResource(res);
 		} else {
 			setResource(Constants.LINK_IDENTICATOR_SITE_RESOURCE + PathTool.getURLFromFile(res));
 		}
 		return this;
 	}
 	
 	/**
 	 * Added a resource that has to copy while export.
 	 * 
 	 * @param resource The name of the resource file, e.g. 'robot.txt'.
 	 */
 	public void addResource(final String resource) {
 		if(isExportView) {
 			File file = new File(siteDir, resource).getAbsoluteFile();
 			renderData.addFile(file);		
 		}
 	}
 
 	/**
 	 * Set the link to style-sheet (named 'format.css') of the site.<br/>
 	 * Just a wrapper to {@link #getResource(String)}.
 	 * 
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getCss() {
 		return getResource("format.css");
 	}
 	
 	/**
 	 * Set the link to the committed level.
 	 * 
 	 * @param levelLinkTo
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getLevel(final Level levelLinkTo) {
 		if (levelLinkTo == null)
 			throw new FatalException("Link to level shouldn't be null!");
 		if (isExportView) {
 			setResource(PathTool.getURLRelativePathToLevel(currentLevel, levelLinkTo)
 						.concat(StringUtils.defaultIfEmpty(welcomePageName, "PAGE_NOT_EXISTS")));
 		} else {
 			if (PoInfo.getRootPage(levelLinkTo) != null)
 				setPageForPreview(PoInfo.getRootPage(levelLinkTo));
 		}
 		return this;
 	}
 	
 	/**
 	 * Set the link to the 'direct' sublevel of the current level with the desired name.
 	 * 
 	 * @param levelName
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getLevelByName(final String levelName) {
 		Level level = PoInfo.getLevelByName(this.pojoHelper.getLevel(), levelName);
 		return this.getLevel(level);
 	}
 
 
 	/**
 	 * Set the link to the committed page.
 	 * 
 	 * @param pageTo
 	 */
 	public SiteLinkTool getPage(final Page pageTo) {
 		if (isExportView) {
 			String pageName;
 			if (PoInfo.isWelcomePage(pageTo) || OrderableInfo.isFirst(pageTo))
 				pageName = welcomePageName;
 			else
 				pageName = pageTo.getName().concat(".").concat(propertiesManager.getSiteProperty("pmcms.site.export.file.extension"));
 			String levelName = PathTool.getURLRelativePathToLevel(this.currentLevel, pageTo.getParent());
 			if (StringUtils.isNotBlank(levelName) && !levelName.endsWith("/"))
 				levelName = levelName.concat("/");
 			setResource(levelName.concat(pageName));
 		} else
 			setPageForPreview(pageTo);
 		return this;
 	}
 	
 	
 	/**
 	 * Set the link to the previous page of the committed one.
 	 * 
 	 * @param currentPage
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getPreviousPage(final Page currentPage) {
 		if (!OrderableInfo.hasPrevious(currentPage)) {
 			setResource("NO_PAGE_FOUND");
 		} else {
 			Page pageTo = (Page) OrderableInfo.getPrevious(currentPage);
 			getPage(pageTo);
 		}
 		return this;
 	}
 
 	/**
 	 * Set the link to the next page of the committed one.
 	 * 
 	 * @param currentPage
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getNextPage(final Page currentPage) {
 		if (!OrderableInfo.hasNext(currentPage)) {
 			setResource("NO_PAGE_FOUND");
 		} else {
 			Page pageTo = (Page) OrderableInfo.getNext(currentPage);
 			getPage(pageTo);
 		}
 		return this;
 	}
 	
 	/**
 	 * Set the link to the css for the editor's commit button.<br>
	 * <b>Should only used internally. It's needed for the default button style in the fckeditor form.</b>
 	 * 
 	 * @return SiteLinkTool
 	 */
 	public SiteLinkTool getButtonCss() {
 		setResource("/"+PathTool.getURLFromFile(InitializationManager.getDefaultResourcesPath().concat("editor-button.css")));
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return this.linkToResourceString;
 	}
 
 	private void setResource(final String resource) {
 		this.linkToResourceString = resource;
 	}
 
 	private void setPageForPreview(final Page pageForPreview) {
 		String pojoDescriptor = Constants.LINK_TYPE_PAGE;
 		if (InstanceUtil.isGallery(pageForPreview))
 			pojoDescriptor = Constants.LINK_TYPE_GALLERY;
 		StringBuilder link = new StringBuilder();
 		link.append("/").append(Constants.LINK_IDENTICATOR_PREVIEW).append("?id=").append(pageForPreview.getId()).append("&amp;");
		link.append(Constants.LINK_TYPE_DESCRIPTOR).append("=").append(pojoDescriptor).append("&amp;");
 		setResource(link.toString());
 	}
 }
