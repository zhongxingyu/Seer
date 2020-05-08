 package com.papteco.web.controllers;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
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
 import com.papteco.web.beans.ProjectBean;
 import com.papteco.web.beans.ProjectShortcutBean;
 import com.papteco.web.beans.SearchShortcutBean;
 import com.papteco.web.services.ProjectService;
 import com.papteco.web.utils.WebUtils;
 
 @Controller
 public class ProjectController extends BaseController {
 	@Autowired
 	private ProjectService projectService;
 
 	@RequestMapping(method = RequestMethod.POST, value = "createProject")
 	@ResponseBody
 	public Map createProject(@RequestBody CreateProjectFormBean bean)
 			throws Exception {
 		ProjectBean tmpProject = new ProjectBean();
 		tmpProject.setProjectCde(bean.getClientno() + "-"
 				+ genProjectCreateDate(bean) + "-" + bean.getUniqueno());
 		tmpProject.setClientNo(bean.getClientno());
 		tmpProject.setCreateDate(genProjectCreateDate(bean));
 		tmpProject.setUniqueNo(bean.getUniqueno());
 		tmpProject.setCreatedAt(new Date());
 		tmpProject.setCreatedBy("wasadmin");
 		tmpProject.setShortDesc(bean.getShortdesc());
 		tmpProject.setLongDesc(bean.getLongdesc());
 		tmpProject.setFolderTree(this.sysConfig.prepareFolderStructure());
 
 		try {
 			projectService.createProject(tmpProject,
 					this.sysConfig.prepareFolderStructure());
 
 			return ImmutableMap.of("type", "success", "projectcode",
 					tmpProject.getProjectCde());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return ImmutableMap
 					.of("type", "failure", "message", e.getMessage());
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "doSearch")
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
 			@RequestParam String searchSavName) throws Exception {
 
 		System.out.println(searchClinetno);
 		System.out.println(searchAnykey);
 		System.out.println(searchSavName);
 		projectService.saveSearchShortcut("conygychen", searchSavName,
 				searchClinetno, searchAnykey);
 		return ImmutableMap.of("type", "success");
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getSearchShortcut")
 	@ResponseBody
 	public Map getSearchShortcut() throws Exception {
 		
 		System.out.println("getSearchShortcut");
 		StringBuilder sb = new StringBuilder();
 		
 		SearchShortcutBean schShortcut = projectService.getSearchShortcut("conygychen");
 		if(schShortcut != null){
 			Iterator iter = schShortcut.getSearchShortcuts().entrySet().iterator();
 			while(iter.hasNext()){
 				Map.Entry sc = (Map.Entry)iter.next();
 				String key = sc.getKey().toString();
 				String[] value = (String[])sc.getValue();
 				sb.append("<li><span class='fileSearch'></span> <a href=\"#\" onclick=\"changetosearch('"+value[0]+"','"+value[1]+"')\" >"+key+"</a> <span class='remove' onclick=\"deleteSearchshortcut('"+key+"');\"></span></li>");
 
 			}
 		}
 		return ImmutableMap.of("data", sb.toString());
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getPrjShortcut")
 	@ResponseBody
 	public Map getPrjShortcut() throws Exception {
 		
 		System.out.println("getPrjShortcut");
 		StringBuilder sb = new StringBuilder();
 		ProjectShortcutBean prjShortcut = projectService.getPrjShortcut("conygychen");
 		if(prjShortcut != null){
 			Iterator iter = prjShortcut.getPrjShortcuts().entrySet().iterator();
 			while(iter.hasNext()){
 				Map.Entry sc = (Map.Entry)iter.next();
 				String key = sc.getKey().toString();
 				int value = Integer.valueOf(sc.getValue().toString());
				sb.append("<li><span class='fileSuccess'></span> <a href='#' onclick='changetoprj("+value+")' >"+key+"</a> <span class='remove' onclick='deleteprjshortcut("+value+");'></span></li>");
 				
 			}
 		}
 		return ImmutableMap.of("data", sb.toString());
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "getPreserveNos")
 	@ResponseBody
 	public Map getPreserveNos() throws Exception {
 		
 		System.out.println("getPreserveNos");
 		
 
 		return ImmutableMap.of("from", 1,"to",2);
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "submitPresrvNos")
 	@ResponseBody
 	public Map submitPresrvNos() throws Exception {
 		
 		System.out.println("submitPresrvNos");
 		
 
 		return ImmutableMap.of("type","success");
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "savePrjshortcut")
 	@ResponseBody
 	public Map saveSearch(@RequestParam String prjId,
 			@RequestParam String prjSavName) throws Exception {
 
 		projectService.saveProjectShortcut("conygychen", prjSavName, prjId);
 		return ImmutableMap.of("type", "success");
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "deleteprjshortcut")
 	@ResponseBody
 	public Map deleteprjshortcut(@RequestParam String delId) throws Exception {
 		System.out.println("delId:"+delId);

 		return ImmutableMap.of("type", "success");
 
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "deleteSearchshortcut")
 	@ResponseBody
 	public Map deleteSearchshortcut(@RequestParam String delId) throws Exception {
 		System.out.println("delId:"+delId);
 		projectService.deleteSearchShortcut("conygychen", delId);
 		return ImmutableMap.of("type", "success");
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "getProject")
 	@ResponseBody
 	public Map getProject(@RequestParam String projectId) throws Exception {
 		System.out.println(projectId);
 		return WebUtils.toProjectSummaries(Integer.valueOf(projectId));
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "getNumberingFormat")
 	@ResponseBody
 	public Map getNumberingFormat(@RequestParam String docType,
 			@RequestParam String prjId) throws Exception {
 		String shortCode = docType + "("
 				+ this.sysConfig.getFolderNameByFolderCde(docType) + ")";
 		FormatItem formating = this.sysConfig.getFormatSetting().get(docType);
 		List<FieldDef> fieldSetting = this.sysConfig.getSeqAndDesc();
 		String clientno = "(?)"; // please change it by prjId
 		String ref = "(?)"; // please change it by prjId
 		return WebUtils.toNumberingFormat(prjId, shortCode, formating,
 				fieldSetting, clientno, ref);
 
 	}
 
 	private String genProjectCreateDate(CreateProjectFormBean bean) {
 		return bean.getCreateDate().substring(2, 6);
 	}
 }
