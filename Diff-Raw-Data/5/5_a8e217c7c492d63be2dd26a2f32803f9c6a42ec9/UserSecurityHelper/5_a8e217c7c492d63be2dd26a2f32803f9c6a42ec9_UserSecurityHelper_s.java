 /*
  * Copyright (c) 2003, 2004 Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: 19/03/2004 - 18:56:49
  * The JForum Project
  * http://www.jforum.net
  * 
 * $Id: UserSecurityHelper.java,v 1.3 2004/09/19 05:13:16 rafaelsteil Exp $
  */
 package net.jforum.security;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * @author Rafael Steil
  */
 public class UserSecurityHelper 
 {
 	public static void mergeUserGroupRoles(RoleCollection userRoles, ArrayList groupsRolesList)
 	{
 		Map newRolesMap = new HashMap();
 		
 		for (Iterator iter = groupsRolesList.iterator(); iter.hasNext(); ) {
 			RoleCollection rc = (RoleCollection)iter.next();
 			
 			for (Iterator rcIter = rc.iterator(); rcIter.hasNext(); ) {
 				final Role role = (Role)rcIter.next();
 				Role userRole = userRoles.get(role.getName());
 				
 				if (userRole == null) {
 					if (newRolesMap.containsKey(role.getName())) {
 						((ArrayList)newRolesMap.get(role.getName())).add(role);
 					}
 					else {
 						newRolesMap.put(role.getName(), new ArrayList() {{ add(role); }});
 					}
 				}
 				else {
 					// Merge the little bastards
 					for (Iterator vIter = role.getValues().iterator(); vIter.hasNext(); ) {
 						RoleValue gRv = (RoleValue)vIter.next();
 						RoleValue uRv = userRole.getValues().get(gRv.getValue()); 
 						
 						if (uRv == null) {
							userRole.getValues().add(gRv);
 						} 
 					}
 				}
 			}
 		}
 		
 		for (Iterator iter = newRolesMap.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry entry = (Map.Entry)iter.next();
 
 			Role newRole = new Role();
 			newRole.setName((String)entry.getKey());
 			newRole.setType(PermissionControl.ROLE_DENY);
 			
 			ArrayList roles = (ArrayList)entry.getValue();
 			for (Iterator rolesIter = roles.iterator(); rolesIter.hasNext(); ) {
 				Role role = (Role)rolesIter.next();
 				newRole.setId(role.getId());
 				
 				// Check if is a single permission ( eg, no children values )
 				// We're assuming here that if the call to getValue() of the current 
 				// role object returns 0, all other related roles will also return 0
 				if (role.getValues().size() == 0) {
 					if (role.getType() == PermissionControl.ROLE_ALLOW) {
 						newRole.setType(PermissionControl.ROLE_ALLOW);
 						break;
 					}
 				}
 				else {
 					// Ok, we have some children ( like forums or categories ids )
 					// Iterate for all values of the current role, checking the 
 					// access rights of each one
 					for (Iterator valuesIter = role.getValues().iterator(); valuesIter.hasNext(); ) {
 						RoleValue rv = (RoleValue)valuesIter.next();
 						RoleValue currentValue = newRole.getValues().get(rv.getValue());
 						
 						if (currentValue == null) {
 							newRole.getValues().add(rv);
 						}
 						else {
 							// We already have some value with this name in the collection
 							// Time to check for its rights
 							if (rv.getType() != currentValue.getType() 
 								&& rv.getType() == PermissionControl.ROLE_ALLOW) {
 								newRole.getValues().remove(currentValue);
 								currentValue.setType(PermissionControl.ROLE_ALLOW);
 								newRole.getValues().add(currentValue);
 							}
 						}
 					}
 				}
 			}
 			
 			userRoles.add(newRole);
 		}
 	}
 }
