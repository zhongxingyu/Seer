 /*
  * Created on Feb 16, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.vfny.geoserver.action.data;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.util.MessageResources;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.action.HTMLEncoder;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.NameSpaceConfig;
 import org.vfny.geoserver.form.data.DataNamespacesSelectForm;
 import org.vfny.geoserver.global.UserContainer;
 
 /**
  * Select Namespaces for editing.
  * 
  * @author rgould, Refractions Research, Inc.
  * @author $Author: dmzwiers $ (last modification)
  * @version $Id: DataNamespacesSelectAction.java,v 1.8 2004/03/01 19:47:45 dmzwiers Exp $
  */
 public class DataNamespacesSelectAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             UserContainer user, HttpServletRequest request, HttpServletResponse response)
     throws IOException, ServletException {
    	
         DataNamespacesSelectForm namespacesForm = (DataNamespacesSelectForm) form;
 
         String action = namespacesForm.getAction();
 
         DataConfig dataConfig = (DataConfig) getDataConfig();
         NameSpaceConfig config = null;
         
         Locale locale = (Locale) request.getLocale();
         MessageResources messages = servlet.getResources();
         
         String edit = HTMLEncoder.decode(messages.getMessage(locale, "label.edit"));
         String delete = HTMLEncoder.decode(messages.getMessage(locale, "label.delete"));
         String _default = HTMLEncoder.decode(messages.getMessage(locale, "label.default"));
 
         String nsSelected = namespacesForm.getSelectedNamespace();
        
        if (nsSelected == null || nsSelected.equalsIgnoreCase(""))	// if it is empty (nothing selected)
        	return mapping.findForward("config.data.namespace");	// return to same page and do nothing
        
         if(nsSelected.endsWith("*")){
         	nsSelected = nsSelected.substring(0,nsSelected.lastIndexOf("*"));
         }
         config = (NameSpaceConfig) dataConfig.getNameSpace(nsSelected);
         if(config==null){
         	throw new NullPointerException();
         }
         getUserContainer(request).setNamespaceConfig(config);
         
         if (action.equals(delete)) 
         {
         	if (dataStoresUseNamespace(dataConfig,nsSelected))
         	{
         		//dont delete a namespace thats in use!
         		    ActionErrors errors = new ActionErrors();
         	        errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.namespace.namespaceInUse" ));            
         	        request.setAttribute(Globals.ERROR_KEY, errors); 
         	        return mapping.findForward("config.data.namespace");
         	}
             dataConfig.removeNameSpace(nsSelected);
             getApplicationState().notifyConfigChanged();
             
             getUserContainer(request).setNamespaceConfig(null);
             namespacesForm.reset(mapping, request);
             return mapping.findForward("config.data.namespace");
         }
         if (action.equals(_default)) {
         	if(!nsSelected
         			.equals(dataConfig.getDefaultNameSpace().getPrefix())){
         		dataConfig.setDefaultNameSpace(dataConfig.getNameSpace(nsSelected));
         		getApplicationState().notifyConfigChanged();
         	}
             
             getUserContainer(request).setNamespaceConfig(null);
             namespacesForm.reset(mapping, request);
             return mapping.findForward("config.data.namespace");
         }
         if( action.equals(edit)){
             getUserContainer(request).setNamespaceConfig(config);
             return mapping.findForward("config.data.namespace.editor");
         }
         ActionErrors errors = new ActionErrors();
         errors.add("submit",
             new ActionError("error.action.invalid", action ));            
         request.setAttribute(Globals.ERROR_KEY, errors);        
         return mapping.findForward("config.data.style");        
     }
 
 	/**
 	 *  return true if the namespace is being used by a datastore.
 	 *  You dont want to delete a namespace thats actually being used.
 	 * 
 	 * @param dataConfig
 	 * @param nsSelected
 	 * @return
 	 */
 	private boolean dataStoresUseNamespace(DataConfig dataConfig, String nsSelected) 
 	{
           List stores =  dataConfig.getDataStoreIds();
           
           Iterator it = stores.iterator();
           while (it.hasNext())
           {
               DataStoreConfig dsc = dataConfig.getDataStore( (String) it.next() );
               if (dsc.getNameSpaceId().equals(nsSelected) )
               	return true;              
           }
           return false;
 	}
 }
