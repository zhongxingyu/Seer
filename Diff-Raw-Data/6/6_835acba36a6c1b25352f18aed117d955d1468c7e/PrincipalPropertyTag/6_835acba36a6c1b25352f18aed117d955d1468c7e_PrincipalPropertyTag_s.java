 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.deliver.taglib.management;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
 import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
 import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.taglib.TemplateControllerTag;
 
 public class PrincipalPropertyTag extends TemplateControllerTag 
 {
     private final static Logger logger = Logger.getLogger(PrincipalPropertyTag.class.getName());
 
 	private static final long serialVersionUID = 4050206323348354355L;
 
 	private String userName;
 	private InfoGluePrincipal principal;
 	private String attributeName;
 	private Integer languageId 				= null;
     private boolean defeatCaches			= false;
     private boolean allowAnonymousProperty 	= false;
     private boolean includeRoles 			= true;
     private boolean includeGroups 			= true;
 
     public PrincipalPropertyTag()
     {
         super();
     }
 
 	public int doEndTag() throws JspException
     {
 		if(languageId == null)
     		languageId = getController().getLanguageId();
     	
         //Here we store the defeat caches setting for later reset
         boolean previousDefeatCaches = getController().getDeliveryContext().getDefeatCaches();
 		try
 		{
	        Map<Class, List<Object>> entities = new HashMap<Class, List<Object>>();
	        entities.put(UserPropertiesImpl.class, Collections.EMPTY_LIST);
	        entities.put(GroupPropertiesImpl.class, Collections.EMPTY_LIST);
	        entities.put(RolePropertiesImpl.class, Collections.EMPTY_LIST);
	        getController().getDeliveryContext().setDefeatCaches(defeatCaches, entities);
 
 		    if(userName != null && !userName.equals(""))
 		    {
 				if(!allowAnonymousProperty && userName.equalsIgnoreCase(CmsPropertyHandler.getAnonymousUser()))
 				{
 					setResultAttribute("Anonymous not allowed to have properties");
 					logger.warn("Anonymous not allowed to have properties unless stated. URL:" + this.getController().getOriginalFullURL() + "\nComponentName:" + this.getController().getComponentLogic().getInfoGlueComponent().getName());
 				}
 				else
 					setResultAttribute(this.getController().getPrincipalPropertyValue(getController().getPrincipal(userName), attributeName, languageId));
 		    }
 		    else if(principal != null)
 		    {
 				if(!allowAnonymousProperty && principal.getName().equalsIgnoreCase(CmsPropertyHandler.getAnonymousUser()))
 				{
 					setResultAttribute("");
 					logger.warn("Anonymous not allowed to have properties unless stated. URL:" + this.getController().getOriginalFullURL() + "\nComponentName:" + this.getController().getComponentLogic().getInfoGlueComponent().getName());
 				}
 				else
 					setResultAttribute(getController().getPrincipalPropertyValue(principal, attributeName, languageId));
 			}
 		    else
 		    {
 		    	setResultAttribute(getController().getPrincipalPropertyValue(attributeName, languageId));
 		    }
 		}
 		finally
 		{
 	        //Resetting the defeatcaches setting
 	        getController().getDeliveryContext().setDefeatCaches(previousDefeatCaches, new HashMap<Class, List<Object>>());
 	        languageId 		= null;
 	        userName 		= null;
 	        principal 		= null;
 		    defeatCaches 	= false;
 		    allowAnonymousProperty = false;
 		    includeRoles = false;
 		    includeGroups = false;
 		}
 		
         return EVAL_PAGE;
     }
 
     public void setUserName(final String userName) throws JspException
     {
         this.userName = evaluateString("principal", "userName", userName);
     }
 
     public void setPrincipal(final String principalString) throws JspException
     {
         this.principal = (InfoGluePrincipal)evaluate("principal", "principal", principalString, InfoGluePrincipal.class);
     }
 
     public void setAttributeName(final String attributeName) throws JspException
     {
         this.attributeName = evaluateString("principal", "attributeName", attributeName);
     }
 
     public void setLanguageId(final String languageIdString) throws JspException
     {
  	   this.languageId = this.evaluateInteger("principal", "languageId", languageIdString);
     }
     
     public void setIncludeRoles(final String includeRoles) throws JspException
     {
         this.includeRoles = (Boolean)evaluate("principal", "includeRoles", includeRoles, Boolean.class);
     }
 
     public void setIncludeGroups(final String includeGroups) throws JspException
     {
         this.includeGroups = (Boolean)evaluate("principal", "includeGroups", includeGroups, Boolean.class);
     }
 
     public void setAllowAnonymousProperty(final String allowAnonymousProperty) throws JspException
     {
         this.allowAnonymousProperty = (Boolean)evaluate("principal", "allowAnonymousProperty", allowAnonymousProperty, Boolean.class);
     }
 
     public void setDefeatCaches(final String defeatCaches) throws JspException
     {
         this.defeatCaches = (Boolean)evaluate("principal", "defeatCaches", defeatCaches, Boolean.class);
     }
 }
