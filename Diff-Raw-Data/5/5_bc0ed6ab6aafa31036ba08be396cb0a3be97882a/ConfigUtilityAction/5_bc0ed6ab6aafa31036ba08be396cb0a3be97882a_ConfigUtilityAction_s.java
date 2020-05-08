 package com.appnomic.appsone.dashboard.action.config;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.ParentPackage;
 import org.apache.struts2.convention.annotation.Result;
 
 import com.appnomic.appsone.dashboard.action.ActionConstants;
 import com.appnomic.appsone.dashboard.action.noc.AbstractNocAction;
 import com.appnomic.appsone.dashboard.config.AlertGridConfigManager;
 import com.appnomic.appsone.dashboard.config.ConfigType;
 import com.appnomic.appsone.dashboard.config.LevelDBManager;
 import com.appnomic.appsone.dashboard.config.attribute.*;
 import com.appnomic.appsone.dashboard.config.entity.AlertGridEntity;
 import com.appnomic.appsone.dashboard.viewobject.config.AlertGridConfigVO;
 import com.appnomic.appsone.dashboard.viewobject.config.PageListVO;
 import com.appnomic.appsone.dashboard.viewobject.config.TabListVO;
 import com.appnomic.appsone.dashboard.viewobject.config.base.BooleanAttributeVO;
 import com.appnomic.appsone.dashboard.viewobject.config.base.IntegerAttributeVO;
 import com.appnomic.appsone.dashboard.viewobject.config.base.StringAttributeVO;
 
 @ParentPackage("json-default")
 @Namespace("/config")
 public class ConfigUtilityAction extends AbstractNocAction {
 
 	private PageListVO [] pageListVO;
 	private Map<String, String[]> param;
 	private Map<String, String> levelDbMap;
 	
 	public Map<String, String> getLevelDbMap() {
 		return levelDbMap;
 	}
 
 	public void setLevelDbMap(Map<String, String> levelDbMap) {
 		this.levelDbMap = levelDbMap;
 	}
 
 	public PageListVO[] getPageListVO() {
 		return pageListVO;
 	}
 
 	public void setPageListVO(PageListVO[] pageListVO) {
 		this.pageListVO = pageListVO;
 	}
 
 	public Map<String, String[]> getParam() {
 		return param;
 	}
 
 	public void setParam(Map<String, String[]> param) {
 		this.param = param;
 	}
 
 	@Action(value="/config/pages", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "parameters,session,SUCCESS,ERROR,agcVO,levelDbMap",
 	        		"enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String pagesAction() {
 		param = getParameters();
 		pageListVO = getDummyList();
 		return SUCCESS;
 	}
 	
 	private PageListVO[] getDummyList() {
 		List<PageListVO> pageList = new ArrayList<PageListVO>();
 		
 		PageListVO pageListVO = new PageListVO();
 		pageListVO.setName("Alerts Grid");
 		pageListVO.setId(UUID.randomUUID().toString());
 		pageListVO.setType(ActionConstants.ACCTYPE.GRID.name());
 		pageList.add(pageListVO);
 		
 		/*pageListVO = new PageListVO();
 		pageListVO.setName("Clusters Grid");
 		pageListVO.setId(UUID.randomUUID().toString());
 		pageListVO.setType(ActionConstants.ACCTYPE.GRID.name());
 		pageList.add(pageListVO);*/
 		
 		pageListVO = new PageListVO();
 		pageListVO.setName("Transactions Grid");
 		pageListVO.setId(UUID.randomUUID().toString());
 		pageListVO.setType(ActionConstants.ACCTYPE.GRID.name());
 		pageList.add(pageListVO);
 		
		pageListVO = new PageListVO();
 		pageListVO.setName("Topology View");
 		pageListVO.setId(UUID.randomUUID().toString());
 		pageListVO.setType(ActionConstants.ACCTYPE.GRID.name());
 		pageList.add(pageListVO);
 		
 		pageListVO = new PageListVO();
 		pageListVO.setName("Global Config");
 		pageListVO.setId(UUID.randomUUID().toString());
		pageListVO.setType(ActionConstants.ACCTYPE.GRID.name());
 		pageList.add(pageListVO);
 		
 		PageListVO [] pageArray = pageList.toArray(new PageListVO[pageList.size()]);
 		return pageArray;
 	}
 	
 	////////////////////////////////////////////////////////////////////////////////////////////////
 
 	public static Integer getInteger(String name, Map<String, String[]> parameters) {
 		Integer myint = null;
 		try {
 			myint = Integer.parseInt(parameters.get(name)[0]);
 		} catch(Exception e) {
 			//e.printStackTrace();
 		}
 		return myint;
 	}
 	
 	public static Boolean getBoolean(String name, Map<String, String[]> parameters) {
 		Boolean mybool = null;
 		try {
 			mybool = Boolean.parseBoolean(parameters.get(name)[0]);
 		} catch(Exception e) {
 			//e.printStackTrace();
 		}
 		return mybool;
 	}
 	
 	
 	////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	@Action(value="/config/allConfigDump", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "parameters,session,SUCCESS,ERROR,age,agcVO,pageListVO",
 	        		"enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String allConfigDumpAction() {
 		LevelDBManager instance = null;
 		try {
 			instance = LevelDBManager.getInstance();
 			//instance.init();
 			levelDbMap = instance.getAllKeyValues();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(instance!=null) {
 				//instance.shutdown();
 			}
 		}
 		return SUCCESS;
 	}
 	
 	////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	
 }
