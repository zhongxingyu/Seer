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
 package org.jboss.aop;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.aop.advice.AdviceBinding;
 import org.jboss.aop.advice.AdviceStack;
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.advice.InterceptorFactory;
 import org.jboss.aop.advice.PrecedenceDef;
 import org.jboss.aop.introduction.AnnotationIntroduction;
 import org.jboss.aop.introduction.InterfaceIntroduction;
 import org.jboss.aop.metadata.ClassMetaDataBinding;
 import org.jboss.aop.metadata.ClassMetaDataLoader;
 import org.jboss.aop.microcontainer.lifecycle.LifecycleCallbackBinding;
 import org.jboss.aop.pointcut.CFlowStack;
 import org.jboss.aop.pointcut.DynamicCFlow;
 import org.jboss.aop.pointcut.Pointcut;
 import org.jboss.aop.pointcut.PointcutStats;
 import org.jboss.aop.pointcut.Typedef;
 import org.jboss.aop.pointcut.ast.ClassExpression;
 
 /**
  * Comment
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author adrian@jboss.org
  * @version $Revision$
  */
 public class Domain extends AspectManager
 {
    String name;
    protected AspectManager parent;
    protected boolean parentFirst;
    protected boolean inheritsDeclarations = true;
    protected boolean inheritsBindings = false;
 
    //Calculating the size of the collections containing this information is timeconsuming, we
    //only want to do this when adding/removing stuff
    protected boolean hasOwnPointcuts;
    protected boolean hasOwnBindings;
    protected boolean hasOwnAnnotationIntroductions;
    protected boolean hasOwnAnnotationOverrides;
    protected boolean hasOwnInterfaceIntroductions;
    protected boolean hasOwnTypedefs;
    protected boolean hasOwnPrecedenceDefs;
    protected boolean hasOwnClassMetaData;
    private static int sequenceNumber;
 
    public Domain(AspectManager manager, String name, boolean parentFirst)
    {
       this.parent = manager;
       this.parentFirst = parentFirst;
       this.name = name;
       manager.addSubDomainByName(this);
    }
 
    public String getDomainName()
    {
       return name;
    }
 
    public String getManagerFQN()
    {
       return parent.getManagerFQN() + name + "/";
    }
 
    public static String getDomainName(final Class clazz, final boolean forInstance)
    {
       String name = (String)AccessController.doPrivileged(new PrivilegedAction() {
 
          public Object run()
          {
             StringBuffer sb = new StringBuffer();
             sb.append(clazz.getName());
             sb.append("_");
             sb.append(System.identityHashCode(clazz.getClassLoader()));
 
             if (forInstance)
             {
                sb.append("_");
                sb.append(getNextSequenceNumber());
             }
             return sb.toString();
          }
       });
       return name;
    }
 
    private synchronized static int getNextSequenceNumber()
    {
       return sequenceNumber++;
    }
    /**
     * Inherits interceptor, aspect, advice stack definitions
     *
     * @param inheritsDeclarations
     */
    public void setInheritsDeclarations(boolean inheritsDeclarations)
    {
       this.inheritsDeclarations = inheritsDeclarations;
    }
 
    public void setInheritsBindings(boolean inheritBindings)
    {
       this.inheritsBindings = inheritBindings;
       if (inheritsBindings)
       {
          parent.subscribeSubDomain(this);
       }
       else
       {
          parent.unsubscribeSubDomain(this);
       }
    }
 
    @Override
    public LinkedHashMap getBindings()
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             LinkedHashMap map = new LinkedHashMap(parent.getBindings());
             map.putAll(this.bindings);
             return map;
          }
          else
          {
             LinkedHashMap map = new LinkedHashMap(this.bindings);
             map.putAll(parent.getBindings());
             return map;
          }
       }
       return super.getBindings();
    }
 
    public boolean hasOwnBindings()
    {
       return hasOwnBindings;
    }
 
    @Override
    public synchronized void addBinding(AdviceBinding binding)
    {
       hasOwnPointcuts = true;
       hasOwnBindings = true;
       super.addBinding(binding);
    }
    
    @Override
    public synchronized void removeBinding(String name)
    {
       super.removeBinding(name);
       hasOwnBindings = bindings.size() > 0;
    }
    
    @Override
    public synchronized void removeBindings(ArrayList binds)
    {
       super.removeBindings(binds);
       hasOwnBindings = bindings.size() > 0;
       hasOwnPointcuts = bindings.size() > 0;
    }
    
    public LinkedHashMap getPointcuts()
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             LinkedHashMap map = new LinkedHashMap(parent.getPointcuts());
             map.putAll(this.pointcuts);
             return map;
          }
          else
          {
             LinkedHashMap map = new LinkedHashMap(this.pointcuts);
             map.putAll(parent.getPointcuts());
             return map;
          }
       }
       return super.getPointcuts();
    }
 
    public boolean hasOwnPointcuts()
    {
       return hasOwnPointcuts;
    }
      
    @Override
    public synchronized void addPointcut(Pointcut pointcut)
    {
       hasOwnPointcuts = true;
       super.addPointcut(pointcut);
    }
 
    @Override
    public void removePointcut(String name)
    {
       super.removePointcut(name);
       hasOwnPointcuts = pointcuts.size() > 0;
    }
 
    @Override
    public LinkedHashMap getPointcutInfos()
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             LinkedHashMap map = new LinkedHashMap(parent.getPointcutInfos());
             map.putAll(this.pointcutInfos);
             return map;
          }
          else
          {
             LinkedHashMap map = new LinkedHashMap(this.pointcutInfos);
             map.putAll(parent.getPointcutInfos());
             return map;
          }
       }
       return super.getPointcutInfos();
    }
 
    @Override
    public List getAnnotationIntroductions()
    {
 
       if (inheritsBindings)
       {
          List result = new ArrayList();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             result.addAll(parent.getAnnotationIntroductions());
             synchronized (annotationIntroductions)
             {
                result = new ArrayList(annotationIntroductions.values());
             }
             return result;
          }
          else
          {
             synchronized (annotationIntroductions)
             {
                result = new ArrayList(annotationIntroductions.values());
             }
             result.addAll(parent.getAnnotationIntroductions());
             return result;
          }
       }
 
       return super.getAnnotationIntroductions();
    }
 
    public boolean hasOwnAnnotationIntroductions()
    {
       return hasOwnAnnotationIntroductions;
    }
 
    @Override
    public synchronized void addAnnotationIntroduction(AnnotationIntroduction pointcut)
    {
       hasOwnAnnotationIntroductions = true;
       super.addAnnotationIntroduction(pointcut);
    }
    
    @Override
    public void removeAnnotationIntroduction(AnnotationIntroduction pointcut)
    {
       super.removeAnnotationIntroduction(pointcut);
       hasOwnAnnotationIntroductions = annotationIntroductions.size() > 0;
    }
    
    @Override
    public List getAnnotationOverrides()
    {
       if (inheritsBindings)
       {
          ArrayList list = new ArrayList();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             list.addAll(parent.getAnnotationOverrides());
             synchronized (annotationOverrides)
             {
                list.addAll(annotationOverrides.values());
             }
             return list;
          }
          else
          {
             synchronized (annotationOverrides)
             {
                list.addAll(annotationOverrides.values());
             }
             list.addAll(parent.getAnnotationOverrides());
             return list;
          }
       }
       return super.getAnnotationOverrides();
    }
 
    public boolean hasOwnAnnotationOverrides()
    {
       return hasOwnAnnotationOverrides;
    }
 
    @Override
    public synchronized void addAnnotationOverride(AnnotationIntroduction pointcut)
    {
       hasOwnAnnotationOverrides = true;
       super.addAnnotationOverride(pointcut);
    }
 
    @Override
    public void removeAnnotationOverride(AnnotationIntroduction pointcut)
    {
       super.removeAnnotationOverride(pointcut);
       hasOwnAnnotationOverrides = annotationOverrides.size() > 0;
    }
    
    @Override
    public Map getInterfaceIntroductions()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getInterfaceIntroductions());
             synchronized (interfaceIntroductions)
             {
                map.putAll(interfaceIntroductions);
             }
             return map;
          }
          else
          {
             synchronized (interfaceIntroductions)
             {
                map.putAll(interfaceIntroductions);
             }
             map.putAll(parent.getInterfaceIntroductions());
             return map;
          }
       }
       return super.getInterfaceIntroductions();
    }
 
    public boolean hasOwnInterfaceIntroductions()
    {
       return hasOwnInterfaceIntroductions;
    }
 
    @Override
    public synchronized void addInterfaceIntroduction(InterfaceIntroduction pointcut)
    {
       hasOwnInterfaceIntroductions = true;
       super.addInterfaceIntroduction(pointcut);
    }
    
    @Override
    public void removeInterfaceIntroduction(String name)
    {
       super.removeInterfaceIntroduction(name);
       hasOwnInterfaceIntroductions = interfaceIntroductions.size() > 0;
    }
    
    @Override
    public Map getTypedefs()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getTypedefs());
             synchronized (typedefs)
             {
                map.putAll(typedefs);
             }
             return map;
          }
          else
          {
             synchronized (typedefs)
             {
                map.putAll(typedefs);
             }
             map.putAll(parent.getTypedefs());
             return map;
          }
       }
       return super.getTypedefs();
    }
 
 
    public boolean hasOwnTypedefs()
    {
       return hasOwnTypedefs;
    }
 
    @Override
    public synchronized void addTypedef(Typedef def) throws Exception
    {
       hasOwnTypedefs = true;
       super.addTypedef(def);
    }
 
    @Override
    public void removeTypedef(String name)
    {
       super.removeTypedef(name);
       hasOwnTypedefs = typedefs.size() > 0;
    }
 
    public Map getInterceptorStacks()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getInterceptorStacks());
             synchronized (interceptorStacks)
             {
                map.putAll(interceptorStacks);
             }
             return map;
          }
          else
          {
             synchronized (interceptorStacks)
             {
                map.putAll(interceptorStacks);
             }
             map.putAll(parent.getInterceptorStacks());
             return map;
          }
       }
       return super.getInterceptorStacks();
    }
 
    public Map getClassMetaDataLoaders()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getClassMetaDataLoaders());
             synchronized (classMetaDataLoaders)
             {
                map.putAll(classMetaDataLoaders);
             }
             return map;
          }
          else
          {
             synchronized (classMetaDataLoaders)
             {
                map.putAll(classMetaDataLoaders);
             }
             map.putAll(parent.getClassMetaDataLoaders());
             return map;
          }
       }
       return super.getClassMetaDataLoaders();
    }
 
    public Map getCflowStacks()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getCflowStacks());
             synchronized (cflowStacks)
             {
                map.putAll(cflowStacks);
             }
             return map;
          }
          else
          {
             synchronized (cflowStacks)
             {
                map.putAll(cflowStacks);
             }
             map.putAll(parent.getCflowStacks());
             return map;
          }
       }
       return super.getCflowStacks();
    }
 
    public Map getDynamicCFlows()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getDynamicCFlows());
             synchronized (dynamicCFlows)
             {
                map.putAll(dynamicCFlows);
             }
             return map;
          }
          else
          {
             synchronized (dynamicCFlows)
             {
                map.putAll(dynamicCFlows);
             }
             map.putAll(parent.getDynamicCFlows());
             return map;
          }
       }
       return super.getDynamicCFlows();
    }
 
    public Map getPerVMAspects()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getPerVMAspects());
             synchronized (perVMAspects)
             {
                map.putAll(perVMAspects);
             }
             return map;
          }
          else
          {
             synchronized (perVMAspects)
             {
                map.putAll(perVMAspects);
             }
             map.putAll(parent.getPerVMAspects());
             return map;
          }
       }
       return super.getPerVMAspects();
    }
 
    @Override
    public LinkedHashMap getPrecedenceDefs()
    {
       if (inheritsDeclarations)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             LinkedHashMap map = new LinkedHashMap(parent.getPrecedenceDefs());
             map.putAll(this.precedenceDefs);
             return map;
          }
          else
          {
             LinkedHashMap map = new LinkedHashMap(this.precedenceDefs);
             map.putAll(parent.getPrecedenceDefs());
             return map;
          }
       }
       return super.getPrecedenceDefs();
    }
 
    public boolean hasOwnPrecedenceDefs()
    {
       return hasOwnPrecedenceDefs;
    }
 
    @Override
    public void addPrecedence(PrecedenceDef precedenceDef)
    {
       hasOwnPrecedenceDefs = true;
       super.addPrecedence(precedenceDef);
    }
 
    @Override
    public void removePrecedence(String name)
    {
       super.removePrecedence(name);
       hasOwnPrecedenceDefs = precedenceDefs.size() > 0;
    }
 
    @Override
    public Map getClassMetaData()
    {
       if (inheritsBindings)
       {
          HashMap map = new HashMap();
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             map.putAll(parent.getClassMetaData());
             synchronized (classMetaData)
             {
                map.putAll(classMetaData);
             }
             return map;
          }
          else
          {
             synchronized (classMetaData)
             {
                map.putAll(classMetaData);
             }
             map.putAll(parent.getClassMetaData());
             return map;
          }
       }
       return super.getClassMetaData();
    }
 
    public boolean hasOwnClassMetaData()
    {
       return hasOwnClassMetaData;
    }
 
    @Override
    public void removeClassMetaData(String name)
    {
       super.removeClassMetaData(name);
       hasOwnClassMetaData = classMetaData.size() > 0;
    }
 
    @Override
    public void addClassMetaData(ClassMetaDataBinding meta)
    {
       hasOwnClassMetaData = true;
       super.addClassMetaData(meta);
    }
 
    public boolean hasOwnDataWithEffectOnAdvices()
    {
       return hasOwnBindings ||
       hasOwnPointcuts ||
       hasOwnAnnotationIntroductions ||
       hasOwnAnnotationOverrides ||
       hasOwnInterfaceIntroductions ||
       hasOwnTypedefs ||
       hasOwnPrecedenceDefs ||
       hasOwnClassMetaData;
    }
 
    public InterceptorFactory getInterceptorFactory(String name)
    {
       InterceptorFactory factory = null;
       if (parentFirst)
       {
          factory = parent.getInterceptorFactory(name);
          if (factory != null) return factory;
       }
       factory = super.getInterceptorFactory(name);
 
       if (factory != null) return factory;
       return parent.getInterceptorFactory(name);
    }
 
    public AdviceStack getAdviceStack(String name)
    {
       AdviceStack factory = null;
       if (parentFirst)
       {
          factory = parent.getAdviceStack(name);
          if (factory != null) return factory;
       }
       factory = super.getAdviceStack(name);
 
       if (factory != null) return factory;
       return parent.getAdviceStack(name);
    }
 
    public Object getPerVMAspect(AspectDefinition def)
    {
       return getPerVMAspect(def.getName());
    }
 
    public Object getPerVMAspect(String def)
    {
       Object factory = null;
       if (parentFirst)
       {
          factory = parent.getPerVMAspect(def);
          if (factory != null) return factory;
       }
       factory = super.getPerVMAspect(def);
 
       if (factory != null) return factory;
       return parent.getPerVMAspect(def);
    }
 
    public AspectDefinition getAspectDefinition(String name)
    {
       AspectDefinition factory = null;
       if (parentFirst)
       {
          factory = parent.getAspectDefinition(name);
          if (factory != null) return factory;
       }
       factory = super.getAspectDefinition(name);
 
       if (factory != null) return factory;
       return parent.getAspectDefinition(name);
    }
 
    public Typedef getTypedef(String name)
    {
       Typedef factory = null;
       if (parentFirst)
       {
          factory = parent.getTypedef(name);
          if (factory != null) return factory;
       }
       factory = super.getTypedef(name);
 
       if (factory != null) return factory;
       return parent.getTypedef(name);
    }
 
    public DomainDefinition getContainer(String name)
    {
       DomainDefinition container = null;
       if (parentFirst)
       {
          container = parent.getContainer(name);
          if (container != null) return container;
       }
       container = super.getContainer(name);
       if (container != null) return container;
       return parent.getContainer(name);
    }
 
 
 
    /**
     * Find a pointcut of with a given name
     */
    public Pointcut getPointcut(String name)
    {
       Pointcut pointcut = null;
 
       if (parentFirst)
       {
          pointcut = parent.getPointcut(name);
          if (pointcut != null) return pointcut;
       }
       pointcut = super.getPointcut(name);
       if (pointcut != null) return pointcut;
       return parent.getPointcut(name);
    }
 
    public void attachMetaData(ClassAdvisor advisor, Class clazz)
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             parent.attachMetaData(advisor, clazz);
             super.attachMetaData(advisor, clazz);
          }
          else
          {
             super.attachMetaData(advisor, clazz);
             parent.attachMetaData(advisor, clazz);
          }
       }
       else
       {
          super.attachMetaData(advisor, clazz);
       }
    }
 
    public CFlowStack getCFlowStack(String name)
    {
       if (inheritsDeclarations)
       {
          if (!parentFirst)
          {
             CFlowStack cflow = super.getCFlowStack(name);
             if (cflow == null)
             {
                cflow = parent.getCFlowStack(name);
             }
             return cflow;
          }
          else
          {
             CFlowStack cflow = parent.getCFlowStack(name);
             if (cflow == null)
             {
                cflow = super.getCFlowStack(name);
             }
             return cflow;
          }
       }
       else
       {
          return super.getCFlowStack(name);
       }
    }
 
 
    public DynamicCFlow getDynamicCFlow(String name)
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             DynamicCFlow cflow = super.getDynamicCFlow(name);
             if (cflow == null)
             {
                cflow = parent.getDynamicCFlow(name);
             }
             return cflow;
          }
          else
          {
             DynamicCFlow cflow = parent.getDynamicCFlow(name);
             if (cflow == null)
             {
                cflow = super.getDynamicCFlow(name);
             }
             return cflow;
          }
       }
       else
       {
          return super.getDynamicCFlow(name);
       }
    }
 
    public ClassMetaDataLoader findClassMetaDataLoader(String group)
    {
       if (inheritsDeclarations)
       {
          if (!parentFirst)
          {
             ClassMetaDataLoader loader = super.findClassMetaDataLoader(group);
             if (loader == null)
             {
                loader = parent.findClassMetaDataLoader(group);
             }
             return loader;
          }
          else
          {
             ClassMetaDataLoader loader = parent.findClassMetaDataLoader(group);
             if (loader == null)
             {
                loader = super.findClassMetaDataLoader(group);
             }
             return loader;
          }
       }
 
       return super.findClassMetaDataLoader(group);
    }
 
    public Map<String, LifecycleCallbackBinding> getLifecycleBindings()
    {
       if (inheritsBindings)
       {
          if (!parentFirst)
          {
             // when child first, parent bindings go in first so that they can be overridden by child.
             LinkedHashMap map = new LinkedHashMap(parent.getLifecycleBindings());
             map.putAll(super.getLifecycleBindings());
             return map;
          }
          else
          {
             LinkedHashMap map = new LinkedHashMap(super.getLifecycleBindings());
             map.putAll(parent.getLifecycleBindings());
             return map;
          }
       }
       return super.getLifecycleBindings();
    }
 
 
 
    //////////////////////////////////////////////////////////////////////////
    //Methods that should delegate to the top AspectManager
 
    public InterceptionMarkers getInterceptionMarkers()
    {
       return parent.getInterceptionMarkers();
    }
 
 
    @Deprecated
    protected Map getScopedClassLoaderDomains()
    {
       return parent.getScopedClassLoaderDomains();
    }
 
    /** Managed by the top-level aspect manager */
    protected Map getSubDomainsPerClass()
    {
       return parent.getSubDomainsPerClass();
    }
 
    /** Only set on a per vm basis */
    public ArrayList getExclude()
    {
       return parent.getExclude();
    }
 
    /** Only set on a per vm basis */
    public ArrayList getInclude()
    {
       return parent.getInclude();
    }
 
    /** Only set on a per vm basis */
    public ArrayList getIgnore()
    {
       return parent.getIgnore();
    }
 
    /** Only set on a per vm basis */
    public ClassExpression[] getIgnoreExpressions()
    {
       return parent.getIgnoreExpressions();
    }
 
    public DynamicAOPStrategy getDynamicAOPStrategy()
    {
       return parent.getDynamicAOPStrategy();
    }
 
    public void setDynamicAOPStrategy(DynamicAOPStrategy strategy)
    {
       parent.setDynamicAOPStrategy(strategy);
    }
 
    protected void updatePointcutStats(Pointcut pointcut)
    {
       parent.updatePointcutStats(pointcut);
    }
 
    protected void updateStats(PointcutStats stats)
    {
       parent.updateStats(stats);
    }
 
    public boolean isExecution()
    {
       return parent.isExecution();
    }
 
    public boolean isConstruction()
    {
       return parent.isConstruction();
    }
 
    public boolean isCall()
    {
       return parent.isCall();
    }
 
    public boolean isWithin()
    {
       return parent.isWithin();
    }
 
    public boolean isWithincode()
    {
       return parent.isWithincode();
    }
 
    public boolean isGet()
    {
       return parent.isGet();
    }
 
    public boolean isSet()
    {
       return parent.isSet();
    }
 
 
 }
