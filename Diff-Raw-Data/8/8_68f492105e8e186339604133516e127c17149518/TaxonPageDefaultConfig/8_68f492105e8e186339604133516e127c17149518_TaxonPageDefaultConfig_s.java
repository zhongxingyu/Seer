 /**
  * $Id: TaxonPageDefaultConfig.java,v 1.1.1.1 2007/08/01 19:11:27 kasiedu Exp $
  *
  * Copyright (c) 2007  University of Massachusetts Boston
  *
  * Authors: Jacob K Asiedu
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  */
 
 package project.efg.util.utils;
 
 
 
 
 
 
 import org.apache.log4j.Logger;
 
 
 import project.efg.templates.taxonPageTemplates.TaxonPageTemplateType;
 import project.efg.templates.taxonPageTemplates.TaxonPageTemplates;
 import project.efg.templates.taxonPageTemplates.XslFileNamesType;
 import project.efg.templates.taxonPageTemplates.XslPage;
 import project.efg.templates.taxonPageTemplates.XslPageType;
 import project.efg.util.factory.SpringFactory;
 import project.efg.util.interfaces.EFGImportConstants;
 
 
 
 /**
  * This servlet receives input from author about configuration of a Taxon page
  * for a datasource and creates a TaxonPageTemplate for that datasource.
  */
 public class TaxonPageDefaultConfig {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 
 	private String mainTableName;
 	private DBObject dbObject;
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(TaxonPageDefaultConfig.class);
 		} catch (Exception ee) {
 		}
 	}
 /**
  * 
  * @param dbObject
  * @param templateConfigHome
  * @param mainTableName
  */
 	public TaxonPageDefaultConfig(DBObject dbObject, 
 			String templateConfigHome, 
 			String mainTableName) {
 		this(dbObject,mainTableName);
 
 	}
 	/**
 	 * 
 	 * @param dbObject
 	 * @param mainTableName
 	 */
 	public TaxonPageDefaultConfig(DBObject dbObject, 
 			String mainTableName) {
 		this.dbObject= dbObject;
 		this.mainTableName = mainTableName;
 	}
 	/**
 	 * 
 	 * @param datasourceName
 	 * @param displayName
 	 * @return
 	 */
 	public boolean processNew(String datasourceName, 
 			String displayName) {		
 		try {
 			log.debug("About to call createEFGTemplate With dispName: " + displayName);
 			log.debug(" and dsName: " + datasourceName);
 
 			if(createEFGTemplate(datasourceName.toLowerCase(), 
 					displayName)){
 				log.debug("About to call insertIntoTemplateTable With dispName: " + displayName);
 				log.debug(" and dsName: " + datasourceName);
 			
 				this.insertIntoTemplateTable(datasourceName,
 						displayName);
 				return true;
 			}
 			else{
 				log.debug("createEFGTemplate returned false");
 				
 			
 			}
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 		}
 		return false;
 	}
 	/**
 	 * 
 	 * @param oldDsName
 	 * @param newDSName
 	 * @param displayName
 	 * @return
 	 */
 	public boolean cloneTemplate(
 			String oldDsName, 
 			String newDSName,
 			String displayName
 			) {
 		
 		boolean isDone = false;
 		try {
 			isDone = cloneOldTemplate(oldDsName, newDSName, displayName);
 			if(isDone){
 				this.insertIntoTemplateTable(newDSName,displayName);
 			}
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 		}
 		return isDone;
 	}
 	/**
 	 * 
 	 * @param datafn
 	 * @param displayName
 	 * @param type
 	 * @param uniqueName
 	 */
 	private void add2TemplateObject(
 			String datafn, 
 			String displayName,
 			String type, 
 			String uniqueName){
 		StringBuffer querySearch = new StringBuffer("/");
 		querySearch.append(EFGImportConstants.EFG_APPS);
 		
 		String xslName = EFGImportConstants.DEFAULT_SEARCH_FILE;
 		if(EFGImportConstants.SEARCH_SEARCH_TYPE.equals(type)){
 			xslName = EFGImportConstants.DEFAULT_SEARCH_PAGE_FILE;
 			querySearch.append("/searchPageBuilder?dataSourceName=");
 		}
 		else{
 			querySearch.append("/search?dataSourceName=");
 		}
 		
 
 		querySearch.append(datafn);
 		querySearch.append("&");
 		querySearch.append(EFGImportConstants.ALL_TABLE_NAME);
 		querySearch.append("=");
 		querySearch.append(this.mainTableName);
 		querySearch.append("&");
 		querySearch.append(EFGImportConstants.XSL_STRING);
 		querySearch.append("=");
 		querySearch.append(xslName);
 		querySearch.append("&");
 		querySearch.append(EFGImportConstants.SEARCH_TYPE_STR);
 		querySearch.append("=");
 		querySearch.append(type);
 		querySearch.append("&");
 		querySearch.append(EFGImportConstants.DISPLAY_FORMAT);
 		querySearch.append("=");
 		querySearch.append(EFGImportConstants.HTML);
 		querySearch.append("&");
 		
 		querySearch.append(EFGImportConstants.MAX_DISPLAY);
 		querySearch.append("=");
		if(type == null){
 			querySearch.append("1");
 		}	
 		else{
 			querySearch.append(EFGImportConstants.NUMBER_OF_TAXON_ON_PAGE);
 		}
 		
 		
 		String key = querySearch.toString();
 		
 		TemplateObject templateObject = SpringFactory.getTemplateObject();
 		EFGDisplayObject displayObject = SpringFactory.getDisplayObject();
 		displayObject.setDisplayName(displayName);
 		displayObject.setDatasourceName(datafn);
 		
 		templateObject.setTemplateName(uniqueName);
 		templateObject.setDisplayObject(displayObject);
 		TemplateMapObjectHandler.add2TemplateMap(
 				key,
 				templateObject,
 				this.dbObject);
 	}
 	/**
 	 * 
 	 * @param datasourceName
 	 * @param displayName
 	 */
 	private void insertIntoTemplateTable(
 			String datasourceName,
 			String displayName){
 	
 		try {	
 			String uniqueName = EFGImportConstants.DEFAULT_PLATES_DISPLAY;
 			
 			String plates = EFGImportConstants.SEARCH_PLATES_TYPE;
 			String lists = EFGImportConstants.SEARCH_LISTS_TYPE;
 			String search = EFGImportConstants.SEARCH_SEARCH_TYPE;
			
 			this.add2TemplateObject(datasourceName, 
 					displayName,
 					plates, 
 					uniqueName);
 			
 			uniqueName = EFGImportConstants.DEFAULT_LISTS_DISPLAY;
 			this.add2TemplateObject(datasourceName, 
 					displayName,
 					lists, 
 					uniqueName);
 			uniqueName = EFGImportConstants.DEFAULT_SEARCH_DISPLAY;
 			this.add2TemplateObject(datasourceName, 
 					displayName,
 					search, 
 					uniqueName);
 			
 			
 			uniqueName = EFGImportConstants.DEFAULT_TAXON_DISPLAY;
 			this.add2TemplateObject(datasourceName, 
 					displayName,
					null, 
 					uniqueName);
 			
 			//create the file here
 		} catch (Exception ee) {
 			ee.printStackTrace();
 			return;
 		}
 	}
 	/**
 	 * 
 	 * @param oldDSName
 	 * @param newDSName
 	 * @param displayName
 	 * @return
 	 */
 	private boolean cloneOldTemplate(String oldDSName, String newDSName,
 			String displayName) {
 		
 		TaxonPageTemplates tps = 
 			TemplateMapObjectHandler.getTemplateFromDB(dbObject, 
 				null, 
 				oldDSName, 
 				this.mainTableName);
 
 			if(tps != null){				
 				return this.insertIntoEFGRDBTable(tps,newDSName.toLowerCase(),displayName);
 			}
 			
 			return false;
 	}
 
 	
 	/**
 	 * 
 	 * @param datasourceName
 	 * @return true if object was successfully written to database
 	 * false otherwise
 	 */
 	private boolean insertIntoEFGRDBTable(TaxonPageTemplates tps,
 			String datasourceName, String displayName) {
 		String mutex = "";
 		synchronized (mutex) {	
 		try {
 			this.insertIntoTable(displayName,datasourceName,tps);
 			return true;
 		} catch (Exception ee) {
 			log.error(ee.getMessage());		
 		}
 		return false;
 		}
 	}
 	/**
 	 * @param dbObject2
 	 * @param displayName
 	 * @param datasourceName
 	 * @param tps
 	 * @param mainTableName2
 	 */
 	private void insertIntoTable( 
 			String displayName, 
 			String datasourceName, 
 			TaxonPageTemplates tps) {
 		log.debug("About to call updateDatabase With dispName: " + displayName);
 		log.debug(" and dsName: " + datasourceName);
 
 		TemplateMapObjectHandler.updateDatabase(this.dbObject, 
 				tps, 
 				datasourceName, 
 				this.mainTableName);
 		
 	}
 	/**
 	 * 
 	 * @param dsName
 	 * @return
 	 */
 	private boolean createEFGTemplate(String dsName, String displayName) {
 		TaxonPageTemplates tps = new TaxonPageTemplates();
 		// if it already exists add stuff to it..
 		TaxonPageTemplateType tp = new TaxonPageTemplateType();
 		tp.setDatasourceName(dsName.toLowerCase());
 		
 		XslFileNamesType xsls = new XslFileNamesType();
 
 		XslPageType xslPageType = getXSLPageType(
 				EFGImportConstants.DEFAULT_TAXON_PAGE_FILE, 
 				EFGImportConstants.DEFAULT_TAXON_DISPLAY);
 		xsls.setXslTaxonPages(xslPageType);
 		//write to templateObject
 
 		xslPageType = getXSLPageType(
 				EFGImportConstants.DEFAULT_SEARCH_FILE, 
 				EFGImportConstants.DEFAULT_PLATES_DISPLAY);
 		xsls.setXslPlatePages(xslPageType);
 
 		xslPageType = getXSLPageType(
 				EFGImportConstants.DEFAULT_SEARCH_FILE, 
 				EFGImportConstants.DEFAULT_LISTS_DISPLAY);
 		xsls.setXslListPages(xslPageType);
 
 		tp.setXSLFileNames(xsls);
 		tps.addTaxonPageTemplate(tp);
 		log.debug("About to call insertIntoEFGRDBTable With dispName: " + displayName);
 		log.debug(" and dsName: " + dsName);
 
 		return this.insertIntoEFGRDBTable(tps,dsName.toLowerCase(),
 				displayName);
 	}
 
 	private XslPageType getXSLPageType(String xslFile, String displayname) {
 		XslPageType xslPageType = new XslPageType();
 		XslPage xslPage = new XslPage();
 		xslPage.setIsDefault(true);
 		xslPage.setFileName(xslFile);
 		
 		xslPage.setDisplayName(displayname);
 		xslPage.setGuid(EFGUniqueID.getID()+"");
 		//generate a guid
 	
 		xslPageType.addXslPage(xslPage);
 		return xslPageType;
 	}
 
 }
