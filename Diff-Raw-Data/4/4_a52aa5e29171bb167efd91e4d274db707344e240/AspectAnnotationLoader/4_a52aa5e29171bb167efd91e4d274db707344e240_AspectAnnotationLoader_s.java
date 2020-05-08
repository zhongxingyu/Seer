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
 
 import java.io.DataInputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javassist.CtClass;
 import javassist.CtPrimitiveType;
 import javassist.Modifier;
 import javassist.bytecode.AnnotationsAttribute;
 import javassist.bytecode.ClassFile;
 import javassist.bytecode.Descriptor;
 import javassist.bytecode.FieldInfo;
 import javassist.bytecode.MethodInfo;
 import javassist.bytecode.annotation.ArrayMemberValue;
 import javassist.bytecode.annotation.BooleanMemberValue;
 import javassist.bytecode.annotation.ClassMemberValue;
 import javassist.bytecode.annotation.MemberValue;
 import javassist.bytecode.annotation.StringMemberValue;
 
 import org.jboss.aop.advice.AdviceBinding;
 import org.jboss.aop.advice.AdviceFactory;
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.advice.AspectFactory;
 import org.jboss.aop.advice.AspectFactoryDelegator;
 import org.jboss.aop.advice.AspectFactoryWithClassLoader;
 import org.jboss.aop.advice.DynamicCFlowDefinition;
 import org.jboss.aop.advice.GenericAspectFactory;
 import org.jboss.aop.advice.Interceptor;
 import org.jboss.aop.advice.InterceptorFactory;
 import org.jboss.aop.advice.PrecedenceDef;
 import org.jboss.aop.advice.PrecedenceDefEntry;
 import org.jboss.aop.advice.Scope;
 import org.jboss.aop.advice.ScopedInterceptorFactory;
 import org.jboss.aop.annotation.factory.duplicate.javassist.AnnotationProxy;
 import org.jboss.aop.introduction.AnnotationIntroduction;
 import org.jboss.aop.introduction.InterfaceIntroduction;
 import org.jboss.aop.pointcut.CFlow;
 import org.jboss.aop.pointcut.CFlowStack;
 import org.jboss.aop.pointcut.DeclareDef;
 import org.jboss.aop.pointcut.DynamicCFlow;
 import org.jboss.aop.pointcut.Pointcut;
 import org.jboss.aop.pointcut.PointcutExpression;
 import org.jboss.aop.pointcut.Typedef;
 import org.jboss.aop.pointcut.TypedefExpression;
 import org.jboss.aop.pointcut.ast.ASTCFlowExpression;
 import org.jboss.aop.pointcut.ast.ASTStart;
 import org.jboss.aop.pointcut.ast.PointcutExpressionParser;
 import org.jboss.aop.pointcut.ast.TypeExpressionParser;
 import org.jboss.aop.util.MethodHashing;
 
 /**
  * Comment
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @version $Revision$
  */
 public class AspectAnnotationLoader
 {
    //TODO: We need something to undeploy everything...
 
    protected AspectManager manager;
    private ClassLoader cl; 
 
    public AspectAnnotationLoader(AspectManager manager)
    {
       this.manager = manager;
    }
 
    public void setClassLoader(ClassLoader cl)
    {
       this.cl = cl;
    }
    
    public ClassLoader getClassLoader()
    {
       if (cl == null)
       {
          return Thread.currentThread().getContextClassLoader();
       }
       return cl;
    }
 
    public void deployInputStreamIterator(Iterator it) throws Exception
    {
       while (it.hasNext())
       {
          InputStream stream = (InputStream) it.next();
          DataInputStream dstream = new DataInputStream(stream);
          ClassFile cf = null;
          try
          {
             cf = new ClassFile(dstream);
          }
          finally
          {
             dstream.close();
             stream.close();
          }
          deployClassFile(cf);
       }
    }
 
    public void deployClassFile(ClassFile cf) throws Exception
    {
       if (AspectManager.verbose) System.out.println("[debug] Looking for aspects in: " + cf.getName());
       AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible != null)
       {
          AspectDefinition def = deployAspect(visible, cf);
 
          if (def == null)
          {
             def = deployInterceptor(visible, cf);
          }
 
          if (def == null)
          {
             deployDynamicCFlow(visible, cf);
          }
 
          if (def == null)
          {
             if (!deployPreparedClass(visible, cf))
             {
                deployPrecedence(visible, cf);
             }
          }
          else
          {
             deployPointcuts(cf);
             deployMixins(cf);
             deployIntroductions(cf);
             deployTypedefs(cf);
             deployCFlowStackDefs(cf);
             deployPrepares(cf);
             deployAnnotationIntroductions(cf);
             deployDeclares(cf);
          }
       }
    }
    
    public void undeployInputStreamIterator(Iterator it) throws Exception
    {
       while (it.hasNext())
       {
          InputStream stream = (InputStream) it.next();
          DataInputStream dstream = new DataInputStream(stream);
          ClassFile cf = null;
          try
          {
             cf = new ClassFile(dstream);
          }
          finally
          {
             dstream.close();
             stream.close();
          }
          undeployClassFile(cf);
       }
    }
    
    public void undeployClassFile(ClassFile cf) throws Exception
    {
       if (AspectManager.verbose) System.out.println("[debug] Looking for aspects in: " + cf.getName());
       AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
       if (visible != null)
       {
          undeployAspect(visible, cf);
          undeployInterceptor(visible, cf);
          undeployDynamicCFlow(visible, cf);
          undeployPreparedClass(visible, cf);
          undeployPrecedence(visible, cf);
          undeployPointcuts(cf);
          undeployMixins(cf);
          undeployIntroductions(cf);
          undeployTypedefs(cf);
          undeployCFlowStackDefs(cf);
          undeployPrepares(cf);
          undeployAnnotationIntroductions(cf);
       }
    }
 
    private AspectDefinition deployAspect(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for Aspect
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Aspect.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Found @Aspect in: " + cf.getName());
          Aspect aspect = (Aspect) AnnotationProxy.createProxy(info, Aspect.class);
          Scope scope = aspect.scope();
          
          String[] interfaces = cf.getInterfaces();
          boolean isFactory = false;
          for (int i = 0; i < interfaces.length; i++)
          {
             if (interfaces[i].equals(AspectFactory.class.getName()))
             {
                isFactory = true;
                break;
             }
          }
          AspectFactory factory = null;
          if (isFactory)
          {
             factory = new AspectFactoryDelegator(cf.getName(), null);
             ((AspectFactoryWithClassLoader)factory).setClassLoader(cl);
          }
          else
          {
             factory = new GenericAspectFactory(cf.getName(), null);
             ((AspectFactoryWithClassLoader)factory).setClassLoader(cl);
          }
          AspectDefinition def = new AspectDefinition(cf.getName(), scope, factory);
          manager.addAspectDefinition(def);
          if (!isFactory)
          {
             deployAspectMethodBindings(cf, def);
          }
 
          return def;
       }
       return null;
    }
 
    private void undeployAspect(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for Aspect
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Aspect.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Undeploying @Aspect in: " + cf.getName());
          manager.removeAspectDefinition(cf.getName());
 
          undeployAspectMethodBindings(cf);
       }
    }
 
    private AspectDefinition deployInterceptor(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for InterceptorDef
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(InterceptorDef.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Found @InterceptorDef in: " + cf.getName());
          Aspect aspect = (Aspect) AnnotationProxy.createProxy(info, Aspect.class);
          Scope scope = aspect.scope();
 
          String[] interfaces = cf.getInterfaces();
          boolean isFactory = false;
          for (int i = 0; i < interfaces.length; i++)
          {
             if (interfaces[i].equals(AspectFactory.class.getName()))
             {
                isFactory = true;
                break;
             }
             else if (interfaces[i].equals(Interceptor.class.getName()))
             {
                break;
             }
          }
 
          AspectFactory aspectFactory;
          if (isFactory)
          {
             aspectFactory = new AspectFactoryDelegator(cf.getName(), null);
             ((AspectFactoryWithClassLoader)aspectFactory).setClassLoader(cl);
          }
          else
          {
             aspectFactory = new GenericAspectFactory(cf.getName(), null);
             ((AspectFactoryWithClassLoader)aspectFactory).setClassLoader(cl);
          }
 
          AspectDefinition def = new AspectDefinition(cf.getName(), scope, aspectFactory);
          manager.addAspectDefinition(def);
          ScopedInterceptorFactory factory = new ScopedInterceptorFactory(def);
          manager.addInterceptorFactory(factory.getName(), factory);
 
          deployInterceptorBindings(visible, cf, factory);
 
          return def;
       }
 
       return null;
    }
 
    private void undeployInterceptor(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for InterceptorDef
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(InterceptorDef.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Undeploying @InterceptorDef in: " + cf.getName());
          AnnotationProxy.createProxy(info, Aspect.class);
 
          manager.removeAspectDefinition(cf.getName());
          manager.removeInterceptorFactory(cf.getName());
          undeployInterceptorBindings(visible, cf);
       }
 
    }
 
    private void deployDynamicCFlow(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(DynamicCFlowDef.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Found @DynamicCFlowDef in: " + cf.getName());
          AnnotationProxy.createProxy(info, DynamicCFlowDef.class);
 
          String name = cf.getName();
          String clazz = cf.getName();
 
          String[] interfaces = cf.getInterfaces();
          boolean foundDCFlow = false;
          for (int i = 0; i < interfaces.length; i++)
          {
             if (interfaces[i].equals(DynamicCFlow.class.getName()))
             {
                foundDCFlow = true;
                break;
             }
          }
          if (!foundDCFlow) throw new RuntimeException("@DynamicCFlow annotated class: " + clazz + " must implement " + DynamicCFlow.class.getName());
 
          manager.addDynamicCFlow(name, new DynamicCFlowDefinition(null, clazz, name));
       }
    }
 
    private void undeployDynamicCFlow(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(DynamicCFlowDef.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Undeploying @DynamicCFlowDef in: " + cf.getName());
          String name = cf.getName();
          manager.removeDynamicCFlow(name);
       }
    }
 
    private boolean deployPreparedClass(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for Aspect
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Prepare.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Found top-level @Prepare in: " + cf.getName());
          Prepare prepare = (Prepare) AnnotationProxy.createProxy(info, Prepare.class);
 
          String name = cf.getName() + "." + visible.getName();
          String expr = replaceThisInExpr(prepare.value(), cf.getName());
          Pointcut p = new PointcutExpression(name, expr);
          manager.addPointcut(p);
          return true;
       }
       
       return false;
    }
 
    private void undeployPreparedClass(AnnotationsAttribute visible, ClassFile cf) throws Exception
    {
       //Check for Aspect
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Prepare.class.getName());
       if (info != null)
       {
          String name = cf.getName() + "." + visible.getName();
          manager.removePointcut(name);
       }
    }
    
    private void deployPrecedence(AnnotationsAttribute visible, ClassFile cf)throws Exception
    {
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Precedence.class.getName());
       if (info != null)
       {
          if (AspectManager.verbose) System.out.println("[debug] Found top-level @Precedence in: " + cf.getName());
          
          ArrayList entries = new ArrayList();
          Iterator fields = cf.getFields().iterator();
          while (fields.hasNext())
          {
             FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
             AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
             if (mgroup == null) continue;
             javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(PrecedenceInterceptor.class.getName());
             if (binfo != null)
             {
                //TODO Make sure it is an interceptor
                entries.add(new PrecedenceDefEntry(getFieldType(finfo), null));
             }
             else
             {
                binfo = mgroup.getAnnotation(PrecedenceAdvice.class.getName());
                if (binfo != null)
                {
                   PrecedenceAdvice advice = (PrecedenceAdvice) AnnotationProxy.createProxy(binfo, PrecedenceAdvice.class);
                   String method = advice.value();
                   entries.add(new PrecedenceDefEntry(getFieldType(finfo), method));
                }
             }
          }
          PrecedenceDefEntry[] pentries = (PrecedenceDefEntry[])entries.toArray(new PrecedenceDefEntry[entries.size()]); 
          PrecedenceDef precedenceDef = new PrecedenceDef(cf.getName(), pentries);
          manager.addPrecedence(precedenceDef);
       }
    }
    
    private void undeployPrecedence(AnnotationsAttribute visible, ClassFile cf)throws Exception
    {
       javassist.bytecode.annotation.Annotation info = visible.getAnnotation(Precedence.class.getName());
       if (info != null)
       {
          manager.removePrecedence(cf.getName());
       }
    }
 
    private void deployAspectMethodBindings(ClassFile cf, AspectDefinition def)
    throws Exception
    {
       Iterator methods = cf.getMethods().iterator();
       while (methods.hasNext())
       {
          javassist.bytecode.MethodInfo minfo = (javassist.bytecode.MethodInfo) methods.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Bind.class.getName());
          if (binfo == null) continue;
          Bind binding = (Bind) AnnotationProxy.createProxy(binfo, Bind.class);
          String pointcutString = binding.pointcut();
          String cflow = binding.cflow();
          if (cflow == null || cflow.trim().equals("")) cflow = null;
          ASTCFlowExpression cflowExpression = null;
          if (cflow != null)
          {
             cflowExpression = new PointcutExpressionParser(new StringReader(cflow)).CFlowExpression();
 
          }
          AdviceFactory factory = new AdviceFactory(def, minfo.getName());
          manager.addInterceptorFactory(factory.getName(), factory);
          InterceptorFactory[] fact = {factory};
          String name = getAspectMethodBindingName(cf, minfo);
          PointcutExpression pointcut = new PointcutExpression(name, pointcutString);
          AdviceBinding abinding = new AdviceBinding(name, pointcut, cflowExpression, cflow, fact);
          manager.addBinding(abinding);
       }
    }
 
 
    private void undeployAspectMethodBindings(ClassFile cf)
    throws Exception
    {
       Iterator methods = cf.getMethods().iterator();
       while (methods.hasNext())
       {
          javassist.bytecode.MethodInfo minfo = (javassist.bytecode.MethodInfo) methods.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Bind.class.getName());
          if (binfo == null) continue;
 
          String adviceName = cf.getName() + "." + minfo.getName();
          manager.removeInterceptorFactory(adviceName);
          String name = getAspectMethodBindingName(cf, minfo);
          manager.removePointcut(name);
          manager.removeBinding(name);
       }
    }
 
    private String getAspectMethodBindingName(ClassFile cf, MethodInfo minfo)throws Exception
    {
       String method = cf.getName() + "." + minfo.getName();
       String fullMethod = method + minfo.getDescriptor();
       return method + " " + MethodHashing.createHash(fullMethod);
    }
 
    private void deployInterceptorBindings(AnnotationsAttribute visible, ClassFile cf, InterceptorFactory factory)
    throws Exception
    {
       javassist.bytecode.annotation.Annotation binfo = visible.getAnnotation(Bind.class.getName());
       if (binfo == null) return;
       Bind bind = (Bind) AnnotationProxy.createProxy(binfo, Bind.class);
       String pointcutString = bind.pointcut();
       String cflow = bind.cflow();
       if (cflow == null || cflow.trim().equals("")) cflow = null;
       ASTCFlowExpression cflowExpression = null;
       if (cflow != null)
       {
          cflowExpression = new PointcutExpressionParser(new StringReader(cflow)).CFlowExpression();
 
       }
 
       String name = cf.getName();
       InterceptorFactory[] inters = {factory};
       Pointcut p = null;
       p = new PointcutExpression(name, pointcutString);
       AdviceBinding binding = new AdviceBinding(name, p, cflowExpression, cflow, inters);
       manager.addBinding(binding);
    }
 
    private void undeployInterceptorBindings(AnnotationsAttribute visible, ClassFile cf)
    throws Exception
    {
       javassist.bytecode.annotation.Annotation binfo = visible.getAnnotation(Bind.class.getName());
       if (binfo == null) return;
 
       String name = cf.getName();
       manager.removePointcut(name);
       manager.removeBinding(name);
    }
 
 
    private void deployPointcuts(ClassFile cf)
    throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          javassist.bytecode.FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(PointcutDef.class.getName());
          if (binfo == null) continue;
          PointcutDef pdef = (PointcutDef) AnnotationProxy.createProxy(binfo, PointcutDef.class);
 
          PointcutExpression pointcut = new PointcutExpression(getPointcutName(cf, finfo), pdef.value());
 
          manager.addPointcut(pointcut);
       }
    }
 
    private void undeployPointcuts(ClassFile cf)
    throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          javassist.bytecode.FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(PointcutDef.class.getName());
          if (binfo == null) continue;
          manager.removePointcut(getPointcutName(cf, finfo));
       }
    }
 
    private String getPointcutName(ClassFile cf, FieldInfo finfo)
    {
       return cf.getName() + "." + finfo.getName();
    }
 
    private void deployMixins(ClassFile cf)
    throws Exception
    {
       Iterator methods = cf.getMethods().iterator();
       while (methods.hasNext())
       {
          javassist.bytecode.MethodInfo minfo = (javassist.bytecode.MethodInfo) methods.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Mixin.class.getName());
          if (binfo == null) continue;
          
          //Since some of the values are of type Class, and method gets called by the system classloader (via Agent)
          // for loadtime aop in jdk 1.5 the classes we try to look at might not yet have been loaded. We're 
          //only after the names anyway, so we bypass the AnnotationProxy/ProxyMapCreator which tries to validate
          //class names by loading the (so far) non-existent class.
          
          //Mixin mixin = (Mixin) AnnotationProxy.createProxy(binfo, Mixin.class);
          //String target = mixin.target();
          //String typeExpression = mixin.typeExpression();
          //String[] interfaces = mixin.interfaces();
          //boolean isTransient = mixin.isTransient();
          
          MemberValue mv = binfo.getMemberValue("target");
          String target = (mv != null) ? ((ClassMemberValue) mv).getValue() : "java.lang.Class";//Note! this should be the same as the default in @Mixin
          mv = binfo.getMemberValue("typeExpression");
          String typeExpression = (mv != null) ? ((StringMemberValue) mv).getValue() : "";//Note! this should be the same as the default in @Mixin
 
          mv = binfo.getMemberValue("interfaces");
          MemberValue[] values = ((ArrayMemberValue) mv).getValue();
          String[] interfaces = new String[values.length];
          for (int i = 0; i < values.length; i++) interfaces[i] = ((ClassMemberValue) values[i]).getValue();
 
          mv = binfo.getMemberValue("isTransient");
          boolean isTransient = (mv != null) ? ((BooleanMemberValue) mv).getValue() : true;//Note! this should be the same as the default in @Mixin
 
          String name = cf.getName() + "." + minfo.getName(); //Name of the method defined on
          
          InterfaceIntroduction intro = null;
          String construction = name;
          switch(Descriptor.numOfParameters(minfo.getDescriptor()))
          {
             case 0:
                construction += "()";
                break;
             case 1:
                construction += "(this)";
                
 /*               
                String parameter = Descriptor.getParamDescriptor(minfo.getDescriptor());
                
                if (parameter.charAt(1) != 'L')
                {
                   String errorMessage = "Mixin creator method '" + name +
                   "' parameter is primitive type ";
                   char desc = parameter.charAt(1);
                   if (desc == ((CtPrimitiveType) CtClass.booleanType).getDescriptor())
                   {
                      errorMessage += "boolean";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.byteType).getDescriptor())
                   {
                      errorMessage += "byte";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.charType).getDescriptor())
                   {
                      errorMessage += "char";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.doubleType).getDescriptor())
                   {
                      errorMessage += "double";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.floatType).getDescriptor())
                   {
                      errorMessage += "float";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.intType).getDescriptor())
                   {
                      errorMessage += "int";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.longType).getDescriptor())
                   {
                      errorMessage += "long";
                   }
                   else if (desc == ((CtPrimitiveType) CtClass.shortType).getDescriptor())
                   {
                      errorMessage += "short";
                   }
                   else
                   {
                      break;
                   }
                   errorMessage += ".\n   It should have the introduction target type as parameter, or have no parameter at all.";
                   throw new RuntimeException(errorMessage);
 
                }*/
                break;
             default:
                throw new RuntimeException("Mixin creator method '" + name +
                      "' should not have more than one parameter.");
          }
          
          intro = createIntroduction(name, target, typeExpression, null, null, null);//cf.getName(), minfo.getName());         
          if (!Modifier.isStatic(minfo.getAccessFlags()) ||
                !Modifier.isPublic(minfo.getAccessFlags()))
          {
             throw new RuntimeException("Mixin creator method '" + name +
                   "' must be public and static.");
          }
          
          //Parse the descriptor to get the returntype of the method.
          String classname = getReturnType(minfo);
          
          intro.getMixins().add(new InterfaceIntroduction.Mixin(classname, interfaces, construction, isTransient));
 
          manager.addInterfaceIntroduction(intro);
       }
    }
 
    private void undeployMixins(ClassFile cf)
    throws Exception
    {
       Iterator methods = cf.getMethods().iterator();
       while (methods.hasNext())
       {
          javassist.bytecode.MethodInfo minfo = (javassist.bytecode.MethodInfo) methods.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Mixin.class.getName());
          if (binfo == null) continue;
 
          String name = cf.getName() + "." + minfo.getName(); //Name of the method defined on
          manager.removeInterfaceIntroduction(name);
       }
    }
 
    private void deployIntroductions(ClassFile cf)
    throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Introduction.class.getName());
          if (binfo == null) continue;
          
          //Since some of the values are of type Class, and method gets called by the system classloader (via Agent)
          // for loadtime aop in jdk 1.5 the classes we try to look at might not yet have been loaded. We're 
          //only after the names anyway, so we bypass the AnnotationProxy/ProxyMapCreator which tries to validate
          //class names by loading the (so far) non-existent class.
          
          //Introduction introduction = (Introduction) AnnotationProxy.createProxy(binfo, Introduction.class);
          //String target = introduction.target();
          //String  typeExpression = introduction.typeExpression();
          //String[] interfaces = introduction.interfaces();
          
          MemberValue mv = binfo.getMemberValue("target");
          String target = (mv != null) ? ((ClassMemberValue) mv).getValue() : "java.lang.Class";//Note! this should be the same as the default in @Interceptor
 
          mv = binfo.getMemberValue("typeExpression");
          String typeExpression = (mv != null) ? ((StringMemberValue) mv).getValue() : "";//Note! this should be the same as the default in @Interceptor
 
          mv = binfo.getMemberValue("interfaces");
          MemberValue[] values = ((ArrayMemberValue) mv).getValue();
          String[] interfaces = new String[values.length];
          for (int i = 0; i < values.length; i++) interfaces[i] = ((ClassMemberValue) values[i]).getValue();
 
          String name = cf.getName() + "." + finfo.getName(); //Name of the field defined on
 
          InterfaceIntroduction interfaceIntro = createIntroduction(name, target, typeExpression, interfaces, null, null);
          manager.addInterfaceIntroduction(interfaceIntro);
       }
    }
 
    private void undeployIntroductions(ClassFile cf)
    throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Introduction.class.getName());
          if (binfo == null) continue;
 
          String name = cf.getName() + "." + finfo.getName(); //Name of the field defined on
 
          manager.removeInterfaceIntroduction(name);
       }
    }
 
    private void deployTypedefs(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(TypeDef.class.getName());
          if (binfo == null) continue;
          TypeDef typeDefinition = (TypeDef) AnnotationProxy.createProxy(binfo, TypeDef.class);
 
          String name = getTypedefName(cf, finfo);
          String expr = typeDefinition.value();
          Typedef typedef = new TypedefExpression(name, expr);
          manager.addTypedef(typedef);
 
       }
    }
 
    private void undeployTypedefs(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(TypeDef.class.getName());
          if (binfo == null) continue;
 
          AnnotationProxy.createProxy(binfo, TypeDef.class);
 
          manager.removeTypedef(getTypedefName(cf, finfo));
 
       }
    }
 
    private String getTypedefName(ClassFile cf, FieldInfo finfo)
    {
       return cf.getName() + "." + finfo.getName();
    }
 
    private void deployCFlowStackDefs(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(CFlowStackDef.class.getName());
          if (binfo == null) continue;
          CFlowStackDef stackDef = (CFlowStackDef) AnnotationProxy.createProxy(binfo, CFlowStackDef.class);
 
          String name = getStackDefName(cf, finfo);
          CFlowDef[] cflows = stackDef.cflows();
          CFlowStack stack = new CFlowStack(name);
 
          for (int i = 0; i < cflows.length; i++)
          {
             CFlowDef cflow = cflows[i];
             boolean not = !cflow.called();
             stack.addCFlow(new CFlow(cflow.expr(), not));
          }
 
          manager.addCFlowStack(stack);
       }
    }
 
    private void undeployCFlowStackDefs(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(CFlowStackDef.class.getName());
          if (binfo == null) continue;
          AnnotationProxy.createProxy(binfo, CFlowStackDef.class);
 
          manager.removeCFlowStack(getStackDefName(cf, finfo));
       }
    }
 
    private String getStackDefName(ClassFile cf, FieldInfo finfo)
    {
       return cf.getName() + "." + finfo.getName();
    }
 
    private void deployPrepares(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Prepare.class.getName());
          if (binfo == null) continue;
          Prepare prepare = (Prepare) AnnotationProxy.createProxy(binfo, Prepare.class);
 
          String name = getPrepareName(cf, finfo);
          String expr = prepare.value();
          Pointcut p = new PointcutExpression(name, expr);
          manager.addPointcut(p);
       }
    }
 
    private void undeployPrepares(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(Prepare.class.getName());
          if (binfo == null) continue;
          AnnotationProxy.createProxy(binfo, Prepare.class);
 
          manager.removePointcut(getPrepareName(cf, finfo));
       }
    }
 
    private String getPrepareName(ClassFile cf, FieldInfo finfo)
    {
       return cf.getName() + "." + finfo.getName();
    }
    
    private void deployAnnotationIntroductions(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(AnnotationIntroductionDef.class.getName());
          if (binfo == null) continue;
          AnnotationIntroductionDef intro = (AnnotationIntroductionDef) AnnotationProxy.createProxy(binfo, AnnotationIntroductionDef.class);
 
          String expr = intro.expr();
          boolean invisible = intro.invisible();
          String annotation = intro.annotation();
 
          annotation = annotation.replace('\'', '"');
 
          AnnotationIntroduction annIntro = AnnotationIntroduction.createComplexAnnotationIntroduction(expr, annotation, invisible);
          manager.addAnnotationIntroduction(annIntro);
       }
    }
 
    private void undeployAnnotationIntroductions(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation binfo = mgroup.getAnnotation(AnnotationIntroductionDef.class.getName());
          if (binfo == null) continue;
          AnnotationIntroductionDef intro = (AnnotationIntroductionDef) AnnotationProxy.createProxy(binfo, AnnotationIntroductionDef.class);
 
          String expr = intro.expr();
          boolean invisible = intro.invisible();
          String annotation = intro.annotation();
 
          annotation = annotation.replace('\'', '"');
 
          AnnotationIntroduction annIntro = AnnotationIntroduction.createComplexAnnotationIntroduction(expr, annotation, invisible);
          manager.removeAnnotationIntroduction(annIntro);
       }
    }
 
    private void deployDeclares(ClassFile cf) throws Exception
    {
       Iterator fields = cf.getFields().iterator();
       while (fields.hasNext())
       {
          FieldInfo finfo = (javassist.bytecode.FieldInfo) fields.next();
          AnnotationsAttribute mgroup = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (mgroup == null) continue;
          javassist.bytecode.annotation.Annotation dwinfo = mgroup.getAnnotation(DeclareWarning.class.getName());
          javassist.bytecode.annotation.Annotation deinfo = mgroup.getAnnotation(DeclareError.class.getName());
          
          if (dwinfo == null && deinfo == null) continue;
          String name = getDeclareName(cf, finfo);
          if (dwinfo != null && deinfo != null) throw new RuntimeException("Cannot annotate " + name + " field with both DeclareError and DeclareWarning");
          
          String expr = null;
          String msg = null;
          boolean warning = false;
          if (deinfo != null)
          {
             DeclareError derror = (DeclareError) AnnotationProxy.createProxy(deinfo, DeclareError.class);
             expr = derror.expr();
             msg = derror.msg();
          }
          else
          {
             DeclareWarning dwarning = (DeclareWarning) AnnotationProxy.createProxy(dwinfo, DeclareWarning.class);
             expr = dwarning.expr();
             msg = dwarning.msg();
             warning = true;
          }
          DeclareDef def = new DeclareDef(name, expr, warning, msg);
 
          manager.addDeclare(def);
       }
    }
 
    private String getDeclareName(ClassFile cf, FieldInfo finfo)
    {
       return cf.getName() + "." + finfo.getName();
    }
 
    private InterfaceIntroduction createIntroduction(String name, String target, String typeExpression, String[] interfaces,
          String constructorClass, String constructorMethod)
    throws Exception
    {
       if (typeExpression != null && typeExpression.trim().equals(""))
       {
          typeExpression = null;
       }
 
       if (typeExpression != null && target != null && target.equals("java.lang.Class"))
       {
          target = null;
       }
 
       if (target == null && typeExpression == null)
       {
          throw new RuntimeException("No target nor a typeExpression attribute is defined for this @Mixin");
       }
 
       if (target == null && typeExpression == null)
       {
          throw new RuntimeException("You cannot define both a target and typeExpression attribute in the same @Mixin");
       }
 
 
       InterfaceIntroduction intro = null;
 
       if (target != null)
       {
          intro = new InterfaceIntroduction(name, target, interfaces, constructorClass, constructorMethod);
       }
       else
       {
          ASTStart start = new TypeExpressionParser(new StringReader(typeExpression)).Start();
          intro = new InterfaceIntroduction(name, start, interfaces, constructorClass, constructorMethod);
       }
 
       return intro;
    }
 
    private String getReturnType(MethodInfo minfo)
    {
       String descriptor = minfo.getDescriptor();
      int paramsEnd = descriptor.indexOf(";)");
      String classname = descriptor.substring(paramsEnd + 3, descriptor.length() - 1);
       classname = classname.replace('/', '.');
       return classname;
    }
    
    private String getFieldType(FieldInfo finfo)
    {
       //This will be of the form: Lorg/jboss/test/aop/annotated/AspectPerClass;
       String descriptor = finfo.getDescriptor();
       String classname = descriptor.substring(1, descriptor.length() - 1);
       classname = classname.replace('/', '.');
       return classname;
    }
 
    /**
     * Replace all occurrences of 'this' unless it happens to be part of
     * another word. For example (if class name is org.acme.Foo:
     * "all(this)" -> "all(org.acme.Foo)"
     * "all(org.Forthis)" -> "all(org.Forthis)"
     * "all(org.thisthing.Foo)" -> "all(org.thisthing.Foo)"
     *
     * @param s
     * @param classname
     * @return
     */
    private static String replaceThisInExpr(String expr, String classname)
    {
       final String THIS = "this";
 
       StringBuffer buf = new StringBuffer();
       int index = expr.indexOf(THIS);
       if (index == -1)
       {
          return expr;
       }
 
       int lastindex = 0;
       while (index != -1)
       {
          boolean isPartOfWord = false;
          if (index > 0)
          {
             char before = expr.charAt(index - 1);
             isPartOfWord = Character.isJavaIdentifierPart(before);
          }
 
          if (!isPartOfWord && index + THIS.length() < expr.length() - 1)
          {
             char after = expr.charAt(index + THIS.length());
             isPartOfWord = Character.isJavaIdentifierPart(after);
          }
 
          buf.append(expr.substring(lastindex, index));
 
          if (isPartOfWord)
          {
             buf.append(THIS);
          }
          else
          {
             buf.append(classname);
          }
 
          lastindex = index + THIS.length();
          index = expr.indexOf(THIS, lastindex);
       }
       buf.append(expr.substring(lastindex));
       return buf.toString();
    }
 }
