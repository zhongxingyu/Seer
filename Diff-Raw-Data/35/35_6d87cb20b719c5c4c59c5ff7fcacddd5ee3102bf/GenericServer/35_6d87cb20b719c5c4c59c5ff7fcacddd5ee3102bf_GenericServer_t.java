 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gorkem Ercan - initial API and implementation
  *     Naci M. Dai
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEJBModule;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Port;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.ServerMonitorManager;
 import org.eclipse.wst.server.core.model.IURLProvider;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 
 /**
  * Generic XML based server implementation.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServer extends ServerDelegate implements IURLProvider {
 
     private static final String ATTR_GENERIC_SERVER_MODULES = "Generic_Server_Modules_List";
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#publishStart(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus publishStart(IProgressMonitor monitor) {
 	    if(getModules().length<1)
 	        return new Status(IStatus.CANCEL,CorePlugin.PLUGIN_ID,0,GenericServerCoreMessages.cancelNoPublish,null);
 		return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "PublishingStarted", null);
 	}
 
 
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		Iterator iterator = getServerDefinition().getModule().iterator();
 	
 	    while(iterator.hasNext())   {
 	        Module module = (Module)iterator.next();
 	        for (int i = 0; i < add.length; i++) {
 	            if(add[i].getModuleType().getId().equals(module.getType()))
 	                return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "CanModifyModules", null);
 	        }
 	    }
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
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getModules()
 	 */
 	public org.eclipse.wst.server.core.IModule[] getModules() {
 		List modules = getAttribute(ATTR_GENERIC_SERVER_MODULES,(List)null);
 		List imodules = new ArrayList();
 		Iterator iterator = modules.iterator();
 		while(iterator.hasNext())
 		{
 		    String moduleId = (String)iterator.next();
 		    int sep = moduleId.indexOf(":");
 		    IProject project =ResourcesPlugin.getWorkspace().getRoot().getProject(moduleId.substring(0,sep));
 		    IModule[] ms = ServerUtil.getModules(project);
 		    for (int i = 0; i < ms.length; i++) {
                 if(ms[i].getId().equals(moduleId.substring(sep+1)))
                 	imodules.add(ms[i]);
             }
 	
 		}
 		if(modules!= null)
 		    return (IModule[])imodules.toArray(new IModule[imodules.size()]);
 		return new IModule[0];
 	}
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getChildModules(org.eclipse.wst.server.core.model.IModule[])
 	 */
 	public IModule[] getChildModules(IModule[] module) {
 		if (module[0] != null && module[0].getModuleType() != null) {
 			String type = module[0].getModuleType().getId();
 			if (module.length == 1 && "j2ee.ear".equals(type)) {
 				IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module[0]
 						.loadAdapter(IEnterpriseApplication.class, null);
 				if (enterpriseApplication.getModules() != null) {
 					return enterpriseApplication.getModules();
 				}
 			}
 		}
 		return new IModule[0];
 	}
 
 	/**
 	 * @return
 	 */
 	private Map getServerInstanceProperties() {
 		Map runtimeProperties =getRuntimeDelegate().getServerInstanceProperties();
 		Map serverProperties = getServerInstancePropertiesImpl();
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
 
 			String url = "http://localhost";
 			int port = 0;
 			
 			port = getHttpPort();
 			port =ServerMonitorManager.getInstance().getMonitoredPort(getServer(), port, "web");
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
		return CorePlugin.getDefault().getServerTypeDefinitionManager().getServerRuntimeDefinition(rtTypeId,getServerInstanceProperties());
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
 
        String type = module.getModuleType().getId();
        if (type.equals("j2ee.ejb")) {
             IEJBModule ejbModule = (IEJBModule) module.loadAdapter(IEJBModule.class,null);
             if (ejbModule != null) {
             	IStatus status = canModifyModules(new IModule[] { module }, null);
                 if (status == null || !status.isOK())
                     throw new CoreException(status);
                 IModule[] childs = doGetChildModules(module);
                 if(childs.length>0)
                 	return childs;
                 return new IModule[] { module };
             }
         }
         if (type.equals("j2ee.ear")) {
             IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,null);
             if (enterpriseApplication.getModules() != null) {
                 IStatus status = canModifyModules(new IModule[] { module },null);
                 if (status == null || !status.isOK())
                     throw new CoreException(status);
                 return new IModule[] { module };
             }
         }        
         if (type.equals("j2ee.web")) {
             IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class,null);
             if (webModule != null) {
                 IStatus status = canModifyModules(new IModule[] { module },null);
                 if (status == null || !status.isOK())
                     throw new CoreException(status);
                 IModule[] childs = doGetChildModules(module);
                 if(childs.length>0)
                 	return childs;
                 return new IModule[] { module };
             }
         }
         return null;
     }
 
 
 	private IModule[] doGetChildModules(IModule module) {
 		IModule[] ears = ServerUtil.getModules("j2ee.ear");
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
 
     public Map getServerInstancePropertiesImpl() {
  		return getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, new HashMap());
  	}
  	
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
  				String path= (String)getServerInstanceProperties().get(property.getId());
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
