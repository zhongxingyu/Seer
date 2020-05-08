 /*
  * $Id$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  * Copyright (c) 2005, Topicus B.V.
  * All rights reserved.
  */
 package org.apache.wicket.security.hive.authorization;
 
 import org.apache.wicket.security.hive.authorization.Permission;
 
 public class TestPermission extends Permission
 {
 	private String actions = "";
 
 	public TestPermission(String name)
 	{
 		super(name);
 	}
 
 	public TestPermission(String name, String actions)
 	{
 		super(name);
 		this.actions = actions;
 	}
 
 	public boolean equals(Object obj)
 	{
 		if (obj == this)
 			return true;
 		if (obj == null)
 			return false;
 		if (obj.getClass().equals(this.getClass()))
 		{
 			TestPermission other = (TestPermission) obj;
 			return other.getName().equals(getName()) && other.getActions().equals(getActions());
 		}
 		return false;
 	}
 
 	public String getActions()
 	{
 		return actions;
 	}
 
 	public int hashCode()
 	{
 		return getName().hashCode();
 	}
 
 	public boolean implies(Permission permission)
 	{
 		if (permission == this)
 			return true;
 		if (permission == null)
 			return false;
 		if (permission.getClass().equals(this.getClass()))
 		{
 			TestPermission other = (TestPermission) permission;
			return other.getName().equals(getName())
					&& getActions().indexOf(other.getActions()) > -1;
 		}
 		return false;
 	}
 
 }
