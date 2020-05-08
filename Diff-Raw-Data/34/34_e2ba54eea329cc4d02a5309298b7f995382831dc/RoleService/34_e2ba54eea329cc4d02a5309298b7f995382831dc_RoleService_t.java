 package com.sensi.service;
 
 import java.util.List;
 
 import com.sensi.domain.Role;
 
 public interface RoleService {
 
 	/** Role */
 	public Long countRoles();
 	
	public Role findRole(String name);
 	
 	public List<Role> findRoles();
 	
 	public void save(Role role);
 	
 	public void delete(Role role);
 	
 }
