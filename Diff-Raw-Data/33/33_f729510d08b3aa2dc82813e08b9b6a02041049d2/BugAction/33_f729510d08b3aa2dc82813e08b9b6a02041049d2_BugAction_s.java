 package com.xx.abel.action;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 
 import com.xx.abel.bean.Bug;
 import com.xx.abel.service.impl.UserServiceImpl;
 import com.xx.abel.service.intf.BugService;
 
 @Controller
 @Scope("prototype")
 public class BugAction {
 	@Autowired
 	private BugService bugService;
 	
 	public String save(){
 		bug.setUsers(UserServiceImpl.getUser());
 		bugService.save(bug);
 		return "list";
 	}
 	public String list(){
 		list = bugService.findAll();
 		return "success";
 	}
 	
 	private List<Bug> list;
 	
 	private Bug bug;
 	
 	public Bug getBug() {
 		return bug;
 	}
 	
 	public void setBug(Bug bug) {
 		this.bug = bug;
 	}
 	
 	public List<Bug> getList() {
 		return list;
 	}
 	
 	public void setList(List<Bug> list) {
 		this.list = list;
 	}
 
 }
