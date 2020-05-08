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
 package org.eclipse.jst.server.generic.internal.core;
 
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
 import org.eclipse.jst.server.generic.core.CorePlugin;
 import org.eclipse.jst.server.generic.core.GenericServerCoreMessages;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Port;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.jst.server.j2ee.IWebModule;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.IURLProvider;
 import org.eclipse.wst.server.core.model.RuntimeDelegate;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 import org.eclipse.wst.server.core.util.ServerPort;
 
 /**
  * Generic XML based server implementation.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServer extends ServerDelegate implements IURLProvider {
 
     private static final String ATTR_GENERIC_SERVER_MODULES = "Generic_Server_Modules_List";
     private ServerRuntime fServerDefinition;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#publishStart(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus publishStart(IProgressMonitor monitor) {
 	    if(getModules().length<1)
 	        return new Status(IStatus.CANCEL,CorePlugin.PLUGIN_ID,0,GenericServerCoreMessages.getString("cancelNoPublish"),null);
 		return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "PublishingStarted", null);
 	}
 
 
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		Iterator iterator = getServerDefinition().getModule().iterator();
 		
 		while(iterator.hasNext())   {
 	        Module module = (Module)iterator.next();
 	        if("j2ee.web".equals(module.getType()))
 	            return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "CanModifyModules", null);
 	    }
 		return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, GenericServerCoreMessages.getString("moduleNotCompatible"), null);
 	}
 	
 	private String createModuleId(IModule module)
 	{
 	    return module.getProject().getName()+":"+module.getId();
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
                String modlId = createModuleId(add[i]);
                if(modules.contains(modlId)==false)
                     modules.add(modlId);
             }
         }
         if(remove!=null && remove.length>0 && modules!=null)
         {
             for (int i = 0; i < remove.length; i++) {
                 modules.remove(createModuleId(remove[i]));
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
                 if(ms[i].getId().equals(moduleId.substring(sep+1)));
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
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getChildModules(org.eclipse.wst.server.core.model.IModule)
 	 */
 	public IModule[] getChildModules(IModule module) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getParentModules(org.eclipse.wst.server.core.model.IModule)
 	 */
 	public IModule[] getParentModules(IModule module) throws CoreException {
 			//FIXME This is valid for only web modules. A generic server should support any 
 			// kind of j2ee module. Fix this after the server architectures are determined.
         IWebModule webModule  = (IWebModule)module.getAdapter(IWebModule.class);
         if (webModule!=null) {
 			IStatus status = canModifyModules(new IModule[] { module }, null);
 			if (status == null || !status.isOK())
 				throw new CoreException(status);
 			return new IModule[] { module };
 		}
 		return null;
 	
 	}
 
 	/**
 	 * @return
 	 */
 	private Map getServerInstanceProperties() {
 		Map runtimeProperties =getRuntimeDelegate().getAttribute(
 				GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, new HashMap());
 		Map serverProperties = getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,new HashMap(1));
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
 	public org.eclipse.wst.server.core.IServerPort[] getServerPorts() {
 		List ports = new ArrayList();
 		Iterator pIter = this.getServerDefinition().getPort().iterator();
 		while (pIter.hasNext()) {
 			Port element = (Port) pIter.next();
 			int port = Integer.parseInt(getServerDefinition().getResolver().resolveProperties(element.getNo()));
 			ports.add(new ServerPort("server", element.getName(), port, element.getProtocol()));		
 		}
 	
 		return (org.eclipse.wst.server.core.IServerPort[])ports.toArray(new org.eclipse.wst.server.core.IServerPort[ports.size()]);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wtp.server.core.model.IURLProvider#getModuleRootURL(org.eclipse.wtp.server.core.model.IModule)
 	 */
 	public URL getModuleRootURL(IModule module) {
 
		try {			
            if (module == null || module.getAdapter(IWebModule.class)==null )
 				return null;
 
 			String url = "http://localhost";
 			int port = 0;
 			
 			port = getHttpPort();
 			port = ServerCore.getServerMonitorManager().getMonitoredPort(getServer(), port, "web");
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
 	private int getHttpPort() {
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
 
     public ServerRuntime getServerDefinition() {
     	if (fServerDefinition == null)
     		fServerDefinition = CorePlugin.getDefault()
     				.getServerTypeDefinitionManager()
     				.getServerRuntimeDefinition(getRuntimeDelegate().getAttribute(
     								GenericServerRuntime.SERVER_DEFINITION_ID,
     								""), getServerInstanceProperties());
     	return fServerDefinition;
     }
 
     private RuntimeDelegate getRuntimeDelegate()
     {
        return (RuntimeDelegate)getServer().getRuntime().getAdapter(RuntimeDelegate.class);
     }
 
 
 
 }
