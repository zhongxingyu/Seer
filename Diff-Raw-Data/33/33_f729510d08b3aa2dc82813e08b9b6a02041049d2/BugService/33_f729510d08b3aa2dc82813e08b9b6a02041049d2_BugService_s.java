 package com.xx.abel.service.intf;
 
 import java.util.List;
 
 import com.xx.abel.bean.Bug;
 
 public interface BugService {
 	
 	public List<Bug> findAll();
 	
 	public void save(Bug bug);
 
 }
