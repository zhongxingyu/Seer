 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.kernel.plugins.dependency;
 
 import java.security.AccessControlContext;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.*;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.metadata.spi.*;
 import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
 import org.jboss.beans.metadata.plugins.AbstractParameterMetaData;
 import org.jboss.dependency.plugins.AbstractControllerContext;
 import org.jboss.dependency.plugins.AbstractDependencyInfo;
 import org.jboss.dependency.spi.*;
 import org.jboss.joinpoint.spi.TargettedJoinpoint;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.metadata.KernelMetaDataRepository;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.metadata.spi.repository.MetaDataRepository;
 import org.jboss.metadata.spi.scope.ScopeKey;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * Controller context.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractKernelControllerContext extends AbstractControllerContext implements KernelControllerContext
 {
    /** The default actions */
    private static final KernelControllerContextActions actions = KernelControllerContextActions.getInstance();
 
    /** The no instantiate actions */
    private static final KernelControllerContextActions noInstantiate = KernelControllerContextActions.getNoInstantiate();
 
    /** The BeanInfo */
    protected BeanInfo info;
 
    /** The meta data */
    protected BeanMetaData metaData;
 
    /** The access control context */
    protected AccessControlContext accessContext;
 
    /** Did we do a describeVisit */
    protected boolean isDescribeProcessed;
 
    /** The scope */
    protected ScopeKey scope;
 
    /**
     * Create an abstract controller context
     *
     * @param info     the bean info
     * @param metaData the meta data
     * @param target   any target object
     */
    public AbstractKernelControllerContext(BeanInfo info, BeanMetaData metaData, Object target)
    {
       super(metaData.getName(), target == null ? actions : noInstantiate, new AbstractDependencyInfo(), target);
       this.info = info;
       this.metaData = metaData;
       ControllerMode mode = metaData.getMode();
       if (mode != null)
          setMode(mode);
       if (System.getSecurityManager() != null)
          accessContext = AccessController.getContext();
    }
 
    public Kernel getKernel()
    {
       KernelController controller = (KernelController) getController();
       if (controller == null)
          throw new IllegalStateException("Context is not installed in controller");
       return controller.getKernel();
    }
 
    public BeanInfo getBeanInfo()
    {
       return info;
    }
 
    /**
     * Set the bean info
     *
     * @param info the bean info
     */
    public void setBeanInfo(BeanInfo info)
    {
       this.info = info;
       infoprocessMetaData();
       flushJBossObjectCache();
    }
 
    public BeanMetaData getBeanMetaData()
    {
       return metaData;
    }
 
    public MetaData getMetaData()
    {
       KernelController controller = (KernelController) getController();
       if (controller == null)
          throw new IllegalStateException("Context is not associated with a controller");
       MetaDataRepository repository = controller.getKernel().getMetaDataRepository().getMetaDataRepository();
       ScopeKey scope = getScope();
       return repository.getMetaData(scope);
    }
 
    public ScopeKey getScope()
    {
       if (scope == null)
       {
          // Bootstrap (probably not really a good idea?)
          KernelController controller = (KernelController) getController();
          if (controller == null)
             return null;
          KernelMetaDataRepository repository = controller.getKernel().getMetaDataRepository();
          scope = repository.getFullScope(this);
       }
       return scope;
    }
 
    public void setScope(ScopeKey key)
    {
       this.scope = key;
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       if (metaData != null)
          buffer.append(" metadata=").append(metaData);
       super.toString(buffer);
    }
 
    public void setController(Controller controller)
    {
       super.setController(controller);
       preprocessMetaData();
    }
 
    /**
     * Preprocess the metadata for this context
     */
    protected void preprocessMetaData()
    {
       if (metaData == null)
          return;
       PreprocessMetaDataVisitor visitor = new PreprocessMetaDataVisitor(metaData);
       AccessController.doPrivileged(visitor);
    }
 
    /**
     * Preprocess the metadata for this context
     */
    protected void infoprocessMetaData()
    {
       if (info == null)
       {
          removeClassContextReference();
          return;
       }
       if (isDescribeProcessed) return;
       DescribedMetaDataVisitor visitor = new DescribedMetaDataVisitor(metaData);
       AccessController.doPrivileged(visitor);
       isDescribeProcessed = true;
    }
 
    /**
     * Get the access control context of the code that created this context.<p>
     * <p/>
     * This will be null when there is no security manager.
     *
     * @return any access control context
     */
    protected AccessControlContext getAccessControlContext()
    {
       return accessContext;
    }
 
    private void removeClassContextReference()
    {
       DependencyInfo dependencyInfo = getDependencyInfo();
       if (dependencyInfo != null)
       {
          Set dependencys = dependencyInfo.getIDependOn(ClassContextDependencyItem.class);
          for(Iterator it = dependencys.iterator(); it.hasNext();)
          {
             DependencyItem di = (DependencyItem) it.next();
             di.unresolved(getController());
          }
       }
    }
 
    public Object get(final String name) throws Throwable
    {
       return execute(new JoinPointCreator()
       {
          public boolean isSecure()
          {
             return false;
          }
 
          public TargettedJoinpoint createJoinpoint(ClassLoader cl, KernelConfigurator configurator) throws Throwable
          {
             return configurator.getPropertyGetterJoinPoint(getBeanInfo(), name);
          }
       });
    }
 
    public void set(final String name, final Object value) throws Throwable
    {
       execute(new JoinPointCreator()
       {
          public TargettedJoinpoint createJoinpoint(ClassLoader cl, KernelConfigurator configurator) throws Throwable
          {
             return configurator.getPropertySetterJoinPoint(getBeanInfo(), name, cl, new AbstractValueMetaData(value));
          }
       });
    }
 
    public Object invoke(final String name, final Object[] parameters, final String[] signature) throws Throwable
    {
       return execute(new JoinPointCreator()
       {
          public TargettedJoinpoint createJoinpoint(ClassLoader cl, KernelConfigurator configurator) throws Throwable
          {
             List<ParameterMetaData> params = new ArrayList<ParameterMetaData>();
             if (parameters != null)
             {
                for(int i = 0; i < parameters.length; i++)
                {
                  AbstractParameterMetaData pmd = new AbstractParameterMetaData(parameters[i]);
                   // setting it as it was; we don't want the actual value class
                  pmd.setType(signature[i]);
                   pmd.setIndex(i);
                   params.add(pmd);
                }
             }
             return configurator.getMethodJoinPoint(getBeanInfo(), cl, name, params, false, true);
          }
       });
    }
 
    public ClassLoader getClassLoader() throws Throwable
    {
       return Configurator.getClassLoader(getBeanMetaData());
    }
 
    protected Object execute(JoinPointCreator creator) throws Throwable
    {
       KernelController controller = (KernelController) getController();
       final KernelConfigurator configurator = controller.getKernel().getConfigurator();
       final ClassLoader cl = getClassLoader();
       TargettedJoinpoint joinpoint = creator.createJoinpoint(cl, configurator);
       joinpoint.setTarget(getTarget());
       // do we need this? - only GenericBeanFactoryPropertyDependencyTC is failing without
       if (creator.isSecure())
       {
          return KernelControllerContextAction.dispatchJoinPoint(this, joinpoint);
       }
       else
       {
          return joinpoint.dispatch();
       }
    }
 
    protected abstract class AbstractMetaDataVistor implements MetaDataVisitor, PrivilegedAction<Object>
    {
       /**
        * The current context for when the dependencies are required
        */
       protected ControllerState contextState = ControllerState.INSTANTIATED;
 
       /**
        * The metadata
        */
       protected BeanMetaData bmd;
 
       /**
        * Visited branch stack
        */
       protected Stack<MetaDataVisitorNode> visitorNodeStack;
 
       protected AbstractMetaDataVistor(BeanMetaData bmd)
       {
          this.bmd = bmd;
          this.visitorNodeStack = new Stack<MetaDataVisitorNode>();
       }
 
       public void initialVisit(MetaDataVisitorNode node)
       {
          visitorNodeStack.push(node);
          try
          {
             internalInitialVisit(node);
          }
          finally
          {
             visitorNodeStack.pop();
          }
       }
 
       public void describeVisit(MetaDataVisitorNode node)
       {
          visitorNodeStack.push(node);
          try
          {
             internalDescribeVisit(node);
          }
          finally
          {
             visitorNodeStack.pop();
          }
       }
 
       protected void internalInitialVisit(MetaDataVisitorNode node)
       {
       }
 
       protected void internalDescribeVisit(MetaDataVisitorNode node)
       {
       }
 
       public KernelControllerContext getControllerContext()
       {
          return AbstractKernelControllerContext.this;
       }
 
       public ControllerState getContextState()
       {
          return contextState;
       }
 
       public void addDependency(DependencyItem dependency)
       {
          getDependencyInfo().addIDependOn(dependency);
       }
 
       public void setContextState(ControllerState contextState)
       {
          this.contextState = contextState;
       }
 
       public Stack<MetaDataVisitorNode> visitorNodeStack()
       {
          return visitorNodeStack;
       }
 
    }
 
    /**
     * A visitor for the metadata that looks for dependencies.
     */
    protected class PreprocessMetaDataVisitor extends AbstractMetaDataVistor
    {
       /**
        * Create a new MetaDataVisitor.
        * 
        * @param bmd the bean metadata
        */
       public PreprocessMetaDataVisitor(BeanMetaData bmd)
       {
          super(bmd);
       }
 
       /**
        * Visit the bean metadata node, this is the starting point
        */
       public Object run()
       {
          bmd.initialVisit(this);
          visitorNodeStack = null;
          return null;
       }
 
       /**
        * Visit a node
        *
        * @param node the node
        */
       protected void internalInitialVisit(MetaDataVisitorNode node)
       {
          boolean trace = log.isTraceEnabled();
          if (trace)
             log.trace("Initial visit node " + node);
 
          // Visit the children of this node
          Iterator children = node.getChildren();
          if (children != null)
          {
             ControllerState restoreState = contextState;
             while (children.hasNext())
             {
                MetaDataVisitorNode child = (MetaDataVisitorNode) children.next();
                try
                {
                   child.initialVisit(this);
                }
                finally
                {
                   contextState = restoreState;
                }
             }
          }
       }
 
    }
 
    /**
     * A visitor for the metadata that looks for dependencies.
     */
    protected class DescribedMetaDataVisitor extends AbstractMetaDataVistor
    {
       /**
        * Create a new MetaDataVisitor.
        * 
        * @param bmd the bean meta data
        */
       public DescribedMetaDataVisitor(BeanMetaData bmd)
       {
          super(bmd);
       }
 
       /**
        * Visit the bean metadata node, this is the starting point
        */
       public Object run()
       {
          bmd.describeVisit(this);
          visitorNodeStack = null;
          return null;
       }
 
       /**
        * Visit a node
        *
        * @param node the node
        */
       protected void internalDescribeVisit(MetaDataVisitorNode node)
       {
          boolean trace = log.isTraceEnabled();
          if (trace)
             log.trace("Describe visit node " + node);
 
          // Visit the children of this node
          Iterator children = node.getChildren();
          if (children != null)
          {
             ControllerState restoreState = contextState;
             while (children.hasNext())
             {
                MetaDataVisitorNode child = (MetaDataVisitorNode) children.next();
                try
                {
                   child.describeVisit(this);
                }
                finally
                {
                   contextState = restoreState;
                }
             }
          }
       }
    }
 
    private abstract class JoinPointCreator
    {
       public boolean isSecure()
       {
          return true;
       }
 
       protected abstract TargettedJoinpoint createJoinpoint(ClassLoader cl, KernelConfigurator configurator) throws Throwable;
    }
 
 }
