 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Port;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.IURLProvider;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 
 /**
  * Generic XML based server implementation.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServer extends ServerDelegate implements IURLProvider {
 
     private static final String ATTR_GENERIC_SERVER_MODULES = "Generic_Server_Modules_List";
 
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		List moduleTypes = getServerDefinition().getModule();
         int found =0;
 		for (int i = 0; i < add.length; i++) {
 			for(int j=0;j<moduleTypes.size();j++){
 				 Module module = (Module)moduleTypes.get(j);
 				 if(add[i].getModuleType()!= null && add[i].getModuleType().getId().equals(module.getType())){
 					 found++;
 					 break;
 				  }
 			}
         }
 		if(found==add.length)
 			return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "CanModifyModules", null);
 		return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, GenericServerCoreMessages.moduleNotCompatible, null);
 	}
 	
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerDelegate#modifyModules(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
      */
     public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
       
         List modules = this.getAttribute(ATTR_GENERIC_SERVER_MODULES,(List)null);
         
         if(add!=null&& add.length>0)
         {
             if(modules==null) {
                modules=new ArrayList(add.length);
             }
             for (int i = 0; i < add.length; i++) {
                
                if(modules.contains(add[i].getId())==false)
                     modules.add(add[i].getId());
             }
         }
         if(remove!=null && remove.length>0 && modules!=null)
         {
             for (int i = 0; i < remove.length; i++) {
                 modules.remove(remove[i].getId());
              }
         }
         if(modules!=null)    
             setAttribute(ATTR_GENERIC_SERVER_MODULES,modules);
         
     }
 
  	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getChildModules(org.eclipse.wst.server.core.model.IModule[])
 	 */
 	public IModule[] getChildModules(IModule[] module) {
 		if (module[0] != null && module[0].getModuleType() != null) {
 			if (module.length == 1 && "jst.ear".equals(module[0].getModuleType().getId())) {
 				IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module[0]
 						.loadAdapter(IEnterpriseApplication.class, null);
 				if (enterpriseApplication != null) {
 					IModule[] earModules = enterpriseApplication.getModules(); 
 					if ( earModules != null) {
 						return earModules;
 					}
 				}
 			}
 		}
 		return new IModule[0];
 	}
 
 	/**
 	 * Returns the server instance properties including runtime properties. 
 	 * 
 	 * @return server instance properties.
 	 */
 	private Map getInstanceProperties() {
 		Map runtimeProperties =getRuntimeDelegate().getServerInstanceProperties();
 		Map serverProperties = getServerInstanceProperties();
 		Map instanceProperties = new HashMap(runtimeProperties.size()+serverProperties.size());
 		instanceProperties.putAll(runtimeProperties);
 		instanceProperties.putAll(serverProperties);
 		return instanceProperties;
 	}
 	
  	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IMonitorableServer#getServerPorts()
 	 */
 	public org.eclipse.wst.server.core.ServerPort[] getServerPorts() {
 		List ports = new ArrayList();
 		Iterator pIter = this.getServerDefinition().getPort().iterator();
 		while (pIter.hasNext()) {
 			Port element = (Port) pIter.next();
 			int port = Integer.parseInt(getServerDefinition().getResolver().resolveProperties(element.getNo()));
 			ports.add(new ServerPort("server", element.getName(), port, element.getProtocol()));		
 		}
 	
 		return (org.eclipse.wst.server.core.ServerPort[])ports.toArray(new org.eclipse.wst.server.core.ServerPort[ports.size()]);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wtp.server.core.model.IURLProvider#getModuleRootURL(org.eclipse.wtp.server.core.model.IModule)
 	 */
 	public URL getModuleRootURL(IModule module) {
 
 		try {			
             if (module == null || module.loadAdapter(IWebModule.class,null)==null )
 				return null;
 
             String host = getServer().getHost();
 			String url = "http://"+host;
 			int port = 0;
 			
 			port = getHttpPort();
 			port =ServerUtil.getMonitoredPort(getServer(), port, "web");
 			if (port != 80)
 				url += ":" + port;
 
 			url += "/"+module.getName();
 
 			if (!url.endsWith("/"))
 				url += "/";
 
 			return new URL(url);
 		} catch (Exception e) {
 			Trace.trace("Could not get root URL", e);
 			return null;
 		}
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected int getHttpPort() {
 		int port=-1;
 		Iterator pIter = this.getServerDefinition().getPort().iterator();
 		while (pIter.hasNext()) {
 			Port aPort = (Port) pIter.next();
 			if(port== -1)
 				port = Integer.parseInt(getServerDefinition().getResolver().resolveProperties(aPort.getNo()));
 			else if( "http".equals(aPort.getProtocol() ) )
 				port = Integer.parseInt(aPort.getNo());	
 		}
 		if( port == -1)
 			port = 8080;
 		return port;
 	}
 
 	/**
 	 * Returns the ServerRuntime that represents the .serverdef
 	 * file for this server. 
 	 * @return server runtime
 	 */
     public ServerRuntime getServerDefinition(){
 		String rtTypeId = getServer().getRuntime().getRuntimeType().getId();
 		return CorePlugin.getDefault().getServerTypeDefinitionManager().getServerRuntimeDefinition(rtTypeId,getInstanceProperties());
 	}
 
     private GenericServerRuntime getRuntimeDelegate(){
     	return (GenericServerRuntime)getServer().getRuntime().loadAdapter(GenericServerRuntime.class,null);
      }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.server.core.model.ServerDelegate#getRootModules(org.eclipse.wst.server.core.IModule)
      */
     public IModule[] getRootModules(IModule module) throws CoreException {
      	IStatus status = canModifyModules(new IModule[] { module }, null);
        if (status != null && !status.isOK())
            return null;
         IModule[] childs = doGetParentModules(module);
         if(childs.length>0)
         	return childs;
         return new IModule[] { module };
     }
 
 
 	private IModule[] doGetParentModules(IModule module) {
 		IModule[] ears = ServerUtil.getModules("jst.ear");
 		ArrayList list = new ArrayList();
 		for (int i = 0; i < ears.length; i++) {
 			IEnterpriseApplication ear = (IEnterpriseApplication)ears[i].loadAdapter(IEnterpriseApplication.class,null);
 			IModule[] childs = ear.getModules();
 			for (int j = 0; j < childs.length; j++) {
 				if(childs[j].equals(module))
 					list.add(ears[i]);
 			}
 		}
 		return (IModule[])list.toArray(new IModule[list.size()]);
 	}
 	/**
 	 * Returns the server properties.
 	 * @return Map of properties.
 	 */
     public Map getServerInstanceProperties() {
  		return getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, new HashMap());
  	}
  	/**
  	 * Change the server instance properties.
  	 * 
  	 * @param map
  	 */
  	public void setServerInstanceProperties(Map map) {
  		setAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, map);
  	}
  	/**
  	 * Checks if the properties set for this server is valid. 
  	 * @return
  	 */
  	public IStatus validate() {
  		List props = this.getServerDefinition().getProperty();
  		for(int i=0;i<props.size();i++)
  		{
  			Property property =(Property)props.get(i);
  			if(property.getType().equals(Property.TYPE_DIRECTORY) || property.getType().equals(Property.TYPE_FILE))
  			{
  				String path= (String)getInstanceProperties().get(property.getId());
  				if(path!=null && !pathExist(path))
  					return  new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, GenericServerCoreMessages.bind(GenericServerCoreMessages.invalidPath,path), null);
  			}
  		}
  		return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "", null);
  	}
 	private boolean pathExist(String path){
 		File f = new File(path);
 		return f.exists();
 	}
  	
  	
 }
