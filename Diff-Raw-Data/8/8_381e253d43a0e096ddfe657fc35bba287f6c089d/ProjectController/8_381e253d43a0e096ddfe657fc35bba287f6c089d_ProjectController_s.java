 package com.papteco.web.controllers;
 
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.google.common.collect.ImmutableMap;
 import com.papteco.web.beans.CreateProjectFormBean;
 import com.papteco.web.beans.FieldDef;
 import com.papteco.web.beans.FormatItem;
 import com.papteco.web.beans.PreserveNosBean;
 import com.papteco.web.beans.ProjectBean;
 import com.papteco.web.beans.ProjectShortcutBean;
 import com.papteco.web.beans.SearchShortcutBean;
 import com.papteco.web.services.PresNoServiceImpl;
 import com.papteco.web.services.ProjectServiceImpl;
 import com.papteco.web.utils.EncoderDecoderUtil;
 import com.papteco.web.utils.WebUtils;
 
 @Controller
 public class ProjectController extends BaseController {
 	@Autowired
 	private ProjectServiceImpl projectService;
 	
 	@Autowired
 	private PresNoServiceImpl presNoService;
 
 	@RequestMapping(method = RequestMethod.POST, value = "createProject")
 	@ResponseBody
 	public Map createProject(@RequestBody CreateProjectFormBean bean)
 			throws Exception {
 		ProjectBean tmpProject = new ProjectBean();
 		tmpProject.setProjectId(bean.getUniqueno());
 		tmpProject.setProjectCde(bean.getClientno() + "-"
 				+ genProjectCreateDate(bean) + "-" + bean.getUniqueno());
 		tmpProject.setClientNo(bean.getClientno());
 		tmpProject.setCreateDate(genProjectCreateDate(bean));
 		tmpProject.setUniqueNo(bean.getUniqueno());
 		tmpProject.setCreatedAt(new Date());
 		tmpProject.setCreatedBy("admin");
 		tmpProject.setShortDesc(bean.getShortdesc());
 		tmpProject.setLongDesc(bean.getLongdesc());
 		tmpProject.setFolderTree(this.sysConfig.prepareFolderStructure());
 
 		try {
 			if(projectService.isPrjIdExisting(bean.getUniqueno())){
 				return this.failMessage("The Unique No. was existing : "+bean.getUniqueno());
 			}else{
 				projectService.createProject(tmpProject,
 						this.sysConfig.prepareFolderStructure());
 
 				return this.successMessage(of("projectcode",
 						tmpProject.getProjectCde()));
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return this.failMessage(e.getMessage());
 		}
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "secure/doSearch")
 	@ResponseBody
 	public List doSearch(@RequestParam String searchClinetno,
 			@RequestParam String searchAnykey) throws Exception {
 		System.out.println(searchClinetno);
 		System.out.println(searchAnykey);
 		return WebUtils.toSearchGrid(searchClinetno, searchAnykey);
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "saveSearch")
 	@ResponseBody
 	public Map saveSearch(@RequestParam String searchClinetno,
 			@RequestParam String searchAnykey,
 			@RequestParam String searchSavName,
 			HttpSession session) throws Exception {
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		System.out.println(searchClinetno);
 		System.out.println(searchAnykey);
 		System.out.println(searchSavName);
 		projectService.saveSearchShortcut(username, searchSavName,
 				searchClinetno, searchAnykey);
 		return this.successMessage();
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getSearchShortcut")
 	@ResponseBody
 	public Map getSearchShortcut(HttpSession session) throws Exception {
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		System.out.println("getSearchShortcut");
 		StringBuilder sb = new StringBuilder();
 		
 		SearchShortcutBean schShortcut = projectService.getSearchShortcut(username);
 		if(schShortcut != null){
 			Iterator iter = schShortcut.getSearchShortcuts().entrySet().iterator();
 			while(iter.hasNext()){
 				Map.Entry sc = (Map.Entry)iter.next();
 				String key = sc.getKey().toString();
 				String[] value = (String[])sc.getValue();
 				sb.append("<li><span class='fileSearch'></span> <a href=\"#\" onclick=\"changetosearch('"+value[0]+"','"+value[1]+"')\" >"+key+"</a> <span class='remove' onclick=\"deleteSearchshortcut('"+key+"');\"></span></li>");
 
 			}
 		}
 		return this.successMessage(of("data", sb.toString()));
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getPrjShortcut")
 	@ResponseBody
 	public Map getPrjShortcut(HttpSession session) throws Exception {
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		System.out.println("getPrjShortcut");
 		StringBuilder sb = new StringBuilder();
 		ProjectShortcutBean prjShortcut = projectService.getPrjShortcut(username);
 		if(prjShortcut != null){
 			Iterator iter = prjShortcut.getPrjShortcuts().entrySet().iterator();
 			while(iter.hasNext()){
 				Map.Entry sc = (Map.Entry)iter.next();
 				String key = sc.getKey().toString();
 				String value = sc.getValue().toString();
 				sb.append("<li><span class='fileSuccess'></span> <a href='#' onclick=\"changetoprj('"+value+"')\" >"+key+"</a> <span class='remove' onclick=\"deleteprjshortcut(\'"+key+"');\"></span></li>");
 				
 			} 
 		}
 		return this.successMessage(of("data", sb.toString()));
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getPreserveNos")
 	@ResponseBody
 	public Map getPreserveNos() throws Exception {
 		
 		System.out.println("getPreserveNos");
 		PreserveNosBean presNoBean = presNoService.getPresNos();
 		return ImmutableMap.of("from", presNoBean.getPresNoFrom(),"to",presNoBean.getPresNoTo());
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "submitPresrvNos")
 	@ResponseBody
 	public Map submitPresrvNos(@RequestParam String datafrom,
 			@RequestParam String datato) throws Exception {
 		
 		System.out.println("submitPresrvNos - "+datafrom + " to " + datato);
 		int presNoFrom = Integer.valueOf(datafrom);
 		int presNoTo = Integer.valueOf(datato);
 		if(presNoFrom > presNoTo){
 			return this.failMessage("PresNoFrom should be little than PresNoTo.");
 		}
 		String issuecases = presNoService.isPresNoValidationPassed(presNoFrom, presNoTo);
 		if(StringUtils.isBlank(issuecases)){
 			presNoService.savePresNos(new PreserveNosBean(presNoFrom,presNoTo));
 		}else{
 			return this.failMessage("Cannot save the preserve numbers. ProjectId ["+issuecases+"] was existing.");
 		}
 		
 		return this.successMessage();
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "savePrjshortcut")
 	@ResponseBody
 	public Map saveSearch(@RequestParam String prjId,
 			@RequestParam String prjSavName, HttpSession session) throws Exception {
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		projectService.saveProjectShortcut(username, prjSavName, prjId);
 		return this.successMessage();
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "deleteprjshortcut")
 	@ResponseBody
 	public Map deleteprjshortcut(@RequestParam String delId, HttpSession session) throws Exception {
 		System.out.println("delId:"+delId);
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		projectService.deletePrjShortcut(username, delId);
 		return this.successMessage();
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "deleteSearchshortcut")
 	@ResponseBody
 	public Map deleteSearchshortcut(@RequestParam String delId, HttpSession session) throws Exception {
 		System.out.println("delId:"+delId);
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		projectService.deleteSearchShortcut(username, delId);
 		return this.successMessage();
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "getProject")
 	@ResponseBody
 	public Map getProject(@RequestParam String projectId) throws Exception {
 		System.out.println("getProject : "+projectId);
 		return WebUtils.toProjectSummaries(projectId);
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getNumberingFormat")
 	@ResponseBody
 	public Map getNumberingFormat(@RequestParam String docType,
 			@RequestParam String prjId,@RequestParam String revFileName,
 			HttpSession session) throws Exception {
 		String username = session.getAttribute("LOGIN_USER") != null?session.getAttribute("LOGIN_USER").toString():"";
 
 		System.out.println("doctype:"+docType+" prjId:"+prjId + " revFileName:"+URLDecoder.decode(revFileName));
 		String shortCode = docType + "("
 				+ this.sysConfig.getFolderNameByFolderCde(docType) + ")";
 		FormatItem formating = this.sysConfig.getFormatSetting().get(docType);
 		List<FieldDef> fieldSetting = this.sysConfig.getSeqAndDesc();
 		String clientno = "(?)"; // please change it by prjId
 		String ref = "(?)"; // please change it by prjId
 		return WebUtils.toNumberingFormat(prjId, shortCode, formating,
 				fieldSetting, clientno, ref,EncoderDecoderUtil.decodeURIComponent(revFileName), username);
 
 	}
 	
	@RequestMapping(method = RequestMethod.GET, value = "deleteProject")
 	@ResponseBody
	public Map deleteProject(@RequestParam String projectId)
 			throws Exception {
 		//DELETE PROJECT
		
 		return this.successMessage();
 	}
 	
 	private String genProjectCreateDate(CreateProjectFormBean bean) {
 		return bean.getCreateDate().substring(2, 6);
 	}
 }
