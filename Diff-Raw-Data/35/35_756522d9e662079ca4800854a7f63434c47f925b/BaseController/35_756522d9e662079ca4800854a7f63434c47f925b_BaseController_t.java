 /**
  * 
  */
 package com.isesalud.support.components;
 
 import java.io.IOException;
 
 import com.isesalud.support.CompareUtil;
 import com.isesalud.support.JAASUtil;
 import com.isesalud.support.JSFContextUtil;
 
 /**
  * @author Haysoos
  *
  */
 @SuppressWarnings("serial")
 public abstract class BaseController extends BaseComponent
 {
 	
 	
 	
 	/**
 	 */
 	@Override
 	protected void init()
 	{
 		try
 		{
 			if (!CompareUtil.isEmpty(getViewRole()) && !getViewEnabled())
 			{
 				JSFContextUtil.redirect();
 			}
 		}
 		catch (IOException i)
 		{
 			throw new RuntimeException(i);
 		}
 	}
 
 	/**
 	 */
 	public Boolean getViewEnabled()
 	{
 		return JAASUtil.isAdmin() || JAASUtil.inRole(getViewRole());
 	}
 
 	/**
 	 */
 	protected abstract String getViewRole();
 }
