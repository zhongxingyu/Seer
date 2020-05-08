 package com.nbcedu.function.schoolmaster2.action;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.reflect.TypeToken;
 import com.nbcedu.function.schoolmaster2.biz.SM2ModuleBiz;
 import com.nbcedu.function.schoolmaster2.biz.SM2SubjectBiz;
 import com.nbcedu.function.schoolmaster2.biz.Sm2TypeBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.exception.DBException;
 import com.nbcedu.function.schoolmaster2.core.util.Struts2Util;
 import com.nbcedu.function.schoolmaster2.core.util.strings.StringUtil;
 import com.nbcedu.function.schoolmaster2.data.model.SM2SubjectMaster;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Module;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2SubjectUser;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Type;
 import com.nbcedu.function.schoolmaster2.utils.UCService;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.SubjectVo;
 
 @SuppressWarnings("serial")
 public class SubjectAction extends BaseAction{
 	
 	private static final Logger logger = Logger.getLogger(SubjectAction.class);
 	
 	private TSm2Subject subject = new TSm2Subject(); 
 	private SubjectVo subjectVo = new SubjectVo();
 	private TSm2Module module = new TSm2Module();
 	
 	private String moduleId;
 	private String typeId;
 	
 	private SM2SubjectBiz sm2SubjectBiz;
 	private Sm2TypeBiz sm2TypeBiz;
 	private SM2ModuleBiz moduleBiz;
 	
 	public String toAdd(){
 		List<TSm2Type> types = this.sm2TypeBiz.findByUserId(this.getUserId());
 		module = this.moduleBiz.findById(moduleId);
 		List<TSm2Subject> subjects = new ArrayList<TSm2Subject>();
 		
 		if(module.getFlag()==TSm2Module.FLAG_YOUGUANLIAN){
 			if(StringUtil.isEmpty(typeId)){
 				subjects = this.getNDZX(types.get(0).getId(),module.getId());
 			}else{
 				subjects = this.getNDZX(typeId,module.getId());
 			}
 		}
 		
 		this.getRequest().setAttribute("types", types);
 		this.getRequest().setAttribute("subjects", subjects);
 		return "subjectAdd";
 	}
 	
 	public String toUpdate(){
 		
 		List<TSm2Type> types = this.sm2TypeBiz.findByUserId(this.getUserId());
 		List<TSm2Subject> subjects = new ArrayList<TSm2Subject>();
 		module = this.moduleBiz.findById(moduleId);
 		
 		if(module.getFlag()==TSm2Module.FLAG_YOUGUANLIAN){
 			if(StringUtil.isEmpty(typeId)){
 				subjects = this.getNDZX(types.get(0).getId(),module.getId());
 			}else{
 				subjects = this.getNDZX(typeId,module.getId());
 			}
 		}
 		
 		subject = this.sm2SubjectBiz.findById(id);
 		this.getRequest().setAttribute("types", types);
 		this.getRequest().setAttribute("subjects", subjects);
 		return "subjectUpdate";
 	}
 	
 	private List<TSm2Subject> getNDZX(String typeId,String moduleId){
 		return  this.sm2SubjectBiz.findByTypeUser(this.getUserId(),typeId, "nianduzhongxin");
 	}
 	
 	public void findGuanLian(){
 		Struts2Util.renderJson(Utils.gson.toJson(getNDZX(typeId,moduleId), new TypeToken<List<TSm2Subject>>(){}.getType()), "encoding:UTF-8");
 	}
 	
 	public void add(){
 		subject.setCreateTime(new Date());
 		
 		String usersId = this.getRequest().getParameter("executeUsersId");
 		Set<TSm2SubjectUser> users = new HashSet<TSm2SubjectUser>();
 		for(String u : usersId.split(",")){
 			TSm2SubjectUser user =  new TSm2SubjectUser();
 			user.setUserId(u);
 			user.setUserName(UCService.findNameByUid(u));
 			users.add(user);
 		}
 		//如果可以关联则新建关联项也有自己的
 		TSm2Module 	module1 = this.moduleBiz.findById(subject.getModuleId());
 		if(module1.getFlag()==TSm2Module.FLAG_YOUGUANLIAN){
 			TSm2SubjectUser user1 =  new TSm2SubjectUser();
 			user1.setUserId(getUserId());
 			user1.setUserName(UCService.findNameByUid(getUserId()));
 			users.add(user1);
 		}
 		
 		Map<String,String> m =  UCService.findDepartmentByUid(this.getUserId());
 		subject.setDepartmentName(m.get("name"));
 		subject.setDepartmentId(m.get("id"));
 		subject.setLastUpdateTime(new Date());
 		subject.setStatus("new");
 		subject.setFlag(0);
 		subject.setProgress(0);
 		subject.setExcuteUsers(users);
 		String checkusersId = this.getRequest().getParameter("checkUsers");
 		if(!StringUtil.isEmpty(checkusersId)){
 			Set<SM2SubjectMaster> checkUsers = new HashSet<SM2SubjectMaster>();
 			for(String u : checkusersId.split(",")){
 				SM2SubjectMaster user =  new SM2SubjectMaster();
 				user.setUserUid(u);
 				user.setUserName(UCService.findNameByUid(u));
 				user.setFlag(0);
 				checkUsers.add(user);
 			}
 			subject.setCheckUsers(checkUsers);
 		}
 		subject.setCreaterId(this.getUserId());
 		subject.setCreaterName(UCService.findNameByUid(this.getUserId()));
 		this.sm2SubjectBiz.add(subject);
 		Struts2Util.renderText("0", "encoding:UTF-8");
 	}
	private <T> get(Class<T> c){
		
		return o;
	}
 	public void update(){
 		String usersId = this.getRequest().getParameter("executeUsersId");
 		TSm2Subject s = this.sm2SubjectBiz.load(subject.getId()); 
 		Set<TSm2SubjectUser> users = new HashSet<TSm2SubjectUser>();
 		for(String u : usersId.split(",")){
 			TSm2SubjectUser user =  new TSm2SubjectUser();
 			user.setUserId(u);
 			user.setUserName(UCService.findNameByUid(u));
 			users.add(user);
 		}
 		s.setExcuteUsers( users);
 		String checkusersId = this.getRequest().getParameter("checkUsers");
 		Set<SM2SubjectMaster> checkUsers = new HashSet<SM2SubjectMaster>();
 		for(String u : checkusersId.split(",")){
 			SM2SubjectMaster user =  new SM2SubjectMaster();
 			user.setUserUid(u);
 			user.setUserName(UCService.findNameByUid(u));
 			user.setFlag(0);
 			checkUsers.add(user);
 		}
 		s.setCheckUsers(checkUsers);
 		s.setLastUpdateTime(new Date());
 		s.setContent(subject.getContent());
 		s.setTitle(subject.getTitle());
 		s.setTypeId(subject.getTypeId());
 		s.setStatus("updated");
 		this.sm2SubjectBiz.modify(s);
 		
 		Struts2Util.renderText("0", "encoding:UTF-8");
 	}
 	/**
 	 * 主管执行者查询
 	 * @return
 	 */
 	public String find(){
 		module = this.moduleBiz.findById(subjectVo.getModuleId());
 //		判断角色 如果是主管则查询所有自己的，否则只查看主管指定执行者可看
 		if(Utils.isManager()){
 			subjectVo.setCreaterId(this.getUserId());
 			pm = this.sm2SubjectBiz.findByCreaterId(subjectVo);
 		}else{
 			subjectVo.setExcuteUserId(this.getUserId());
 			pm = this.sm2SubjectBiz.findByExceuteUserId(subjectVo);
 		}
 		return "list";
 	}
 	
 	public String findB(){
 		//判断模块是否为子模块，如果为子则查询父模块所有并跳转模块列表
 		module = this.moduleBiz.findById(subjectVo.getModuleId());
 		if(!StringUtil.isEmpty(module.getParentId())){
 			List<TSm2Subject> list = this.sm2SubjectBiz.
 			findByModuleIdExceuteUserId(module.getParentId(), this.getUserId());
 			this.getRequest().setAttribute("list",list);
 		}
 		return "listB";
 	}
 	
 	public void delete(){
 		this.sm2SubjectBiz.removeById(id);
 		Struts2Util.renderText("0", "encoding:UTF-8");
 	}
 	
 	public void findStatusCount(){
 		List<Map<String, String>> list = this.sm2SubjectBiz.findStatusCount(this.getUserId());
 		Struts2Util.renderJson(Utils.gson.toJson(list), "encoding:UTF-8");
 	}
 	
 	/**
 	 * 插旗异步方法
 	 */
 	public void stick(){
 		String subjectId = this.getRequest().getParameter("subjectId");
 		String flag = this.getRequest().getParameter("flag");
 		try {
 			this.sm2SubjectBiz.updateMasterFlag(Integer.parseInt(flag),subjectId,this.getUserId());
 		} catch (Exception e) {
 			Struts2Util.renderText("false", "encoding:UTF-8");
 			logger.error("插旗", e);
 		}
 		Struts2Util.renderText("success", "encoding:UTF-8");
 	}
 	
 	/**
 	 * 判断重名
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	public String isExist() throws IOException {
 //		String subjectTile = this.getRequest().getParameter("subjectTitle");
 //		if (this.sm2SubjectBiz.f) {
 //			AjaxHelper.writeFailurJSON(getResponse(), "主题重名");
 //		} else {
 //			AjaxHelper.writeSuccessJSON(getResponse());
 //		}
 		return null;
 	}
 	//查询转发
 	public String findAllTrans(){
 		try {
 			pm = sm2SubjectBiz.findAlltrans(subjectVo, this.getUserId());
 		} catch (DBException e) {
 			logger.error("查询转发出错",e);
 		}
 		return "transList";
 	}
 	/////////////////////////
 	/////getters&setters/////
 	/////////////////////////
 
 	public TSm2Subject getSubject() {
 		return subject;
 	}
 
 	public void setSubject(TSm2Subject subject) {
 		this.subject = subject;
 	}
 
 	public void setSm2SubjectBiz(SM2SubjectBiz sm2SubjectBiz) {
 		this.sm2SubjectBiz = sm2SubjectBiz;
 	}
 
 	public void setSm2TypeBiz(Sm2TypeBiz sm2TypeBiz) {
 		this.sm2TypeBiz = sm2TypeBiz;
 	}
 
 	public SubjectVo getSubjectVo() {
 		return subjectVo;
 	}
 
 	public void setSubjectVo(SubjectVo subjectVo) {
 		this.subjectVo = subjectVo;
 	}
 
 	public void setModuleBiz(SM2ModuleBiz moduleBiz) {
 		this.moduleBiz = moduleBiz;
 	}
 
 	public TSm2Module getModule() {
 		return module;
 	}
 
 	public void setModule(TSm2Module module) {
 		this.module = module;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 
 	public String getTypeId() {
 		return typeId;
 	}
 
 	public void setTypeId(String typeId) {
 		this.typeId = typeId;
 	}
 	
 }
  
