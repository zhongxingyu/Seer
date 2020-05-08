 /*
  * JBoss, Home of Professional Open Source
  * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @authors tag. See the copyright.txt in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.ejb3.heks.businessobject;
 
 import org.jboss.beans.metadata.api.annotations.Inject;
 import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
 import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
 import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
 import org.jboss.ejb3.proxy.impl.factory.session.stateful.StatefulSessionProxyFactory;
 import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiStatefulSessionRegistrar;
 import org.jboss.ejb3.proxy.javassist.JavassistProxyFactory;
 import org.jboss.ejb3.session.SessionContainer;
 import org.jboss.ejb3.stateful.StatefulContainer;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
 import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 
 /**
  * Create a BusinessObjectFactory that allows no-interfaces to work.
  * 
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class EJB31StatefulBusinessObjectFactory extends AbstractBusinessObjectFactory
 {
    private Kernel kernel;
 
    public EJB31StatefulBusinessObjectFactory()
    {
 
    }
    
    @Deprecated
    public EJB31StatefulBusinessObjectFactory(Kernel kernel)
    {
       setKernel(kernel);
    }
 
    private static boolean arrayContains(Object a[], Object relevant)
    {
       for(Object o : a)
       {
          if(o.equals(relevant))
             return true;
       }
       return false;
    }
 
    /**
     * A direct copy out of LegacyStatefulBusinessObjectFactory
     */
    protected <B> B createLegacyBusinessObject(SessionContainer container, Serializable sessionId, Class<B> businessInterface, ViewType type)
    {
       boolean isRemote = type == ViewType.REMOTE;
       
       // Obtain SFSB JNDI Registrar
       String sfsbJndiRegistrarObjectStoreBindName = ((StatefulContainer) container).getJndiRegistrarBindName();
       JndiStatefulSessionRegistrar sfsbJndiRegistrar = Ejb3RegistrarLocator.locateRegistrar().lookup(
             sfsbJndiRegistrarObjectStoreBindName, JndiStatefulSessionRegistrar.class);
 
       // Get the metadata
       JBossSessionBeanMetaData smd = container.getMetaData();
 
       // Get the appropriate JNDI Name
       String jndiName = !isRemote ? smd.getLocalJndiName() : smd.getJndiName();
 
       // Find the Proxy Factory Key for this SFSB
       String proxyFactoryKey = sfsbJndiRegistrar.getProxyFactoryRegistryKey(jndiName, smd, !isRemote);
 
       // Lookup the Proxy Factory in the Object Store
       StatefulSessionProxyFactory proxyFactory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey,
             StatefulSessionProxyFactory.class);
 
       // Create a new business proxy
       Object proxy = proxyFactory.createProxyBusiness(sessionId, businessInterface.getName());
 
       // Return the Proxy
       return businessInterface.cast(proxy);
    }
    
    protected <B> B createNoInterfaceView(SessionContainer container, Serializable sessionId, Class<B> intf)
    {
       Class<?> beanClass = container.getBeanClass();
 
       String name = container.getDeploymentQualifiedName();
       KernelControllerContext endpointContext = (KernelControllerContext) kernel.getController().getContext(name, null);
       
       // create an invocation handler
       InvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(endpointContext, sessionId, intf);
 
       // Now create the proxy
       Object noInterfaceView = new JavassistProxyFactory().createProxy(new Class<?>[] {beanClass}, invocationHandler);
       return intf.cast(noInterfaceView);
    }
 
    protected ViewType getViewType(SessionContainer container, Class<?> intf)
    {
       // try to see if we can pass the preconditions of LegacyStatefulBusinessObjectFactory?
 
       // a direct copy out of LegacyStatefulBusinessObjectFactory
       // less risky than rolling our own thing.
 
       Class<?>[] remoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container);
       if(arrayContains(remoteInterfaces, intf))
          return ViewType.REMOTE;
 
       Class<?>[] localInterfaces = ProxyFactoryHelper.getLocalAndBusinessLocalInterfaces(container);
       if(arrayContains(localInterfaces, intf))
          return ViewType.LOCAL;
 
       // copied out of EJB3NoInterfaceDeployer
       
       JBossSessionBeanMetaData beanMetaData = container.getMetaData();
       if(!(beanMetaData instanceof JBossSessionBean31MetaData))
          return ViewType.UNKNOWN;
 
       JBossSessionBean31MetaData sessionBean31MetaData = (JBossSessionBean31MetaData) beanMetaData;
       if(!sessionBean31MetaData.isNoInterfaceBean())
          return ViewType.UNKNOWN;
 
      // it could still be an unknown view, make sure it's the no-interface view
      if(intf.equals(container.getBeanClass()))
         return ViewType.NO_INTERFACE;

      return ViewType.UNKNOWN;
    }
 
    @Inject
    public void setKernel(Kernel kernel)
    {
       this.kernel = kernel;
    }
 }
