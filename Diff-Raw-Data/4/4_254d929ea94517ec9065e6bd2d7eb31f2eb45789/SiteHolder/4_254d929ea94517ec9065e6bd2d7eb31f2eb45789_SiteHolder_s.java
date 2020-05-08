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
 package de.thischwa.pmcms.livecycle;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import de.thischwa.pmcms.configuration.PropertiesManager;
 import de.thischwa.pmcms.model.InstanceUtil;
 import de.thischwa.pmcms.model.domain.PoPathInfo;
 import de.thischwa.pmcms.model.domain.pojo.APoormansObject;
 import de.thischwa.pmcms.model.domain.pojo.ASiteResource;
 import de.thischwa.pmcms.model.domain.pojo.Gallery;
 import de.thischwa.pmcms.model.domain.pojo.Image;
 import de.thischwa.pmcms.model.domain.pojo.Level;
 import de.thischwa.pmcms.model.domain.pojo.Macro;
 import de.thischwa.pmcms.model.domain.pojo.Page;
 import de.thischwa.pmcms.model.domain.pojo.Site;
 import de.thischwa.pmcms.model.domain.pojo.Template;
 import de.thischwa.pmcms.tool.PropertiesTool;
 import de.thischwa.pmcms.view.renderer.VelocityUtils;
 
 /**
  * Spring-managed bean to hold a {@link Site}, its {@link VelocityEngine} and maintains rendered images.
  */
 @Component
 public class SiteHolder {
 	private static Logger logger = Logger.getLogger(SiteHolder.class);
 	private Site site;
 	private VelocityEngine velocityEngine;
 	private Set<File> justRendering = Collections.synchronizedSet(new HashSet<File>());
 
 	private AtomicInteger lastID = new AtomicInteger(0);
 	private ConcurrentMap<Integer, APoormansObject<?>> poPerID = new ConcurrentHashMap<Integer, APoormansObject<?>>();
 	
 	@Autowired private PropertiesManager pm;
 	
 	public void clear() {
 		poPerID.clear();
 		lastID = new AtomicInteger(0);
 	}
 	
 	public void mark(Level level) {
 		if(InstanceUtil.isSite(level)) {
 			Site s = (Site)level;
 			for(ASiteResource r : s.getMacros())
 				mark(r);
 			for(ASiteResource r : s.getTemplates())
 				mark(r);
 			if(s.getLayoutTemplate() != null)
 				mark(s.getLayoutTemplate());
		}
		mark((APoormansObject<?>) level);
 		for(Page p : level.getPages())
 			mark(p);
 		if(level.hasSublevels()) {
 			for(Level l : level.getSublevels())
 				mark(l);
 		}
 	}
 	
 	private void mark(Page page) {
 		mark((APoormansObject<?>)page);
 		if(InstanceUtil.isGallery(page)) {
 			Gallery g = (Gallery)page;
 			for(Image i : g.getImages())
 				mark((APoormansObject<?>)i);
 		}
 	}
 		
 	public void mark(APoormansObject<?> po) {
 		if(po.getId() == APoormansObject.UNSET_VALUE)
 			po.setId(lastID.getAndIncrement());
 		if(po.getId() >= lastID.get())
 			lastID.set(po.getId()+1);
 		poPerID.put(po.getId(), po);
 	}
 
 	public APoormansObject<?> get(int id) {
 		return poPerID.get(id);
 	}
 
 	/**
 	 * DON'T USE IT IN A SERVLET CONTEXT
 	 * 
 	 * @return
 	 */
 	public Site getSite() {
 		return site;
 	}
 
 	public void setSite(final Site site) {
 		clear();
 		lastID.set(0);
 		this.site = site;
 		if(site != null) {
 			int id = -1;
 			for(Template t : site.getTemplates()) {
 				if(t.getId() > id)
 					id = t.getId();
 			}
 			for(Macro m : site.getMacros()) {
 				if(m.getId() > id)
 					id = m.getId();
 			}
 			if(id > -1)
 				lastID.set(id + 1);
 			mark(site);
 			loadSiteProperties(site);
 		}
 		
 		reconfigVelocityEngine();
 		justRendering.clear();
 	}
 	
 	private void loadSiteProperties(final Site site) {
 		File configDir = PoPathInfo.getSiteConfigurationDirectory(site); 
 		File propertiesFile = new File(configDir, "site.properties");
 		if(!propertiesFile.exists()) {
 			logger.debug(String.format("No properties found for [%s].", site.getUrl()));
 			return;
 		}
 		try {
 			pm.setSiteProperties(PropertiesTool.loadProperties(new BufferedInputStream(new FileInputStream(propertiesFile))));
 			logger.info(String.format("Properties for [%s] successful loaded.", site.getUrl()));
 		} catch (FileNotFoundException e) {
 		}
 	}
 	
 
 	/**
 	 * Same as <code>setSite(null)</code>.
 	 */
 	public void deleteSite() {
 		setSite(null);
 	}
 
 	/**
 	 * @return the velocityEngine
 	 */
 	public VelocityEngine getVelocityEngine() {
 		return velocityEngine;
 	}
 
 	public void addJustRendering(File file) {
 		if (site != null)
 			justRendering.add(file);
 	}
 
 	public void removeJustRendering(File file) {
 		if (site != null)
 			justRendering.remove(file);
 	}
 
 	public boolean containsJustRendering(File file) {
 		return justRendering.contains(file);
 	}
 
 	public void reconfigVelocityEngine() {
 		velocityEngine = (site == null) ? null : VelocityUtils.getSiteEngine(site);
 	}
 }
