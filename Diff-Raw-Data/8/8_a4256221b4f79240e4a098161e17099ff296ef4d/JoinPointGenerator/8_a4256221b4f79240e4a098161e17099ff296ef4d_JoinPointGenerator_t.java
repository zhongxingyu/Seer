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
 package org.jboss.aop.instrument;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewConstructor;
 import javassist.CtNewMethod;
 import javassist.Modifier;
 import javassist.NotFoundException;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.CallerConstructorInfo;
 import org.jboss.aop.GeneratedClassAdvisor;
 import org.jboss.aop.InstanceAdvisor;
 import org.jboss.aop.JoinPointInfo;
 import org.jboss.aop.advice.AdviceMethodProperties;
 import org.jboss.aop.advice.GeneratedAdvisorInterceptor;
 import org.jboss.aop.advice.Scope;
 import org.jboss.aop.advice.annotation.AdviceMethodFactory;
 import org.jboss.aop.joinpoint.Invocation;
 import org.jboss.aop.pointcut.ast.ASTCFlowExpression;
 import org.jboss.aop.pointcut.ast.ClassExpression;
 import org.jboss.aop.util.JavassistUtils;
 import org.jboss.aop.util.ReflectToJavassist;
 
 /** Creates the Joinpoint invocation replacement classes used with Generated advisors
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision$
  */
 public abstract class JoinPointGenerator
 {
    // TODO replace by enum
    protected static class JoinPointParameters {
       public static final JoinPointParameters ONLY_ARGS = new JoinPointParameters(false, -1, false, -1, 0, "");
       public static final JoinPointParameters TARGET_ARGS = new JoinPointParameters(true, 1, false, -1, 1, "$1");
       public static final JoinPointParameters CALLER_ARGS = new JoinPointParameters(false, -1, true, 1, 1, "$1");
       public static final JoinPointParameters TARGET_CALLER_ARGS = new JoinPointParameters(true, 1, true, 2, 2, "$1, $2");
       
       private boolean target;
       private boolean caller;
       
       private int targetIndex;
       private int callerIndex;
       private int firstArgIndex;
       private String listPrefix;
       
       private JoinPointParameters(boolean target, int targetIndex, boolean caller, int callerIndex, int firstArgIndex, String listPrefix)
       {
          this.target = target;
          this.targetIndex = targetIndex;
          this.caller = caller;
          this.callerIndex = callerIndex;
          this.firstArgIndex = firstArgIndex + 1;
          this.listPrefix = listPrefix;
       }
       
       public boolean hasTarget()
       {
          return target;
       }
       
       public int getTargetIndex()
       {
          return targetIndex;
       }
       
       public boolean hasCaller()
       {
          return caller;
       }
       
       public int getCallerIndex()
       {
          return callerIndex;
       }
       
       public int getFirstArgIndex()
       {
          return firstArgIndex;
       }
       
       public String declareArgsArray(int totalParameters)
       {
          if (++totalParameters == firstArgIndex)
          {
             return "Object[] " + ARGUMENTS + " = new Object[0];";
          }
          StringBuffer buffer = new StringBuffer("Object[] ");
          buffer.append(ARGUMENTS);
          buffer.append(" = new Object[]{($w)$");
          buffer.append(firstArgIndex);
          for (int i = firstArgIndex + 1; i < totalParameters; i++)
          {
             buffer.append(", ($w)$");
             buffer.append(i);
          }
          buffer.append("};");
          return buffer.toString();
       }
       
       public void appendParameterListWithArgs(StringBuffer code, CtClass[] parameterTypes, boolean beginWithComma)
       {
 
          int j = firstArgIndex - 1;
          int totalParameters = parameterTypes.length - j;
          if (listPrefix == "" && totalParameters == 0)
          {
             return;
          }
 
          if (beginWithComma)
          {
             code.append(", ");
          }
 
          code.append(listPrefix);
          if (totalParameters == 0)
          {
             return;
          }
          if (listPrefix.length() != 0)
          {
             code.append(", ");
          }
 
          castArgument(code, parameterTypes[j++], 0);
 
          for (int i = 1; i < totalParameters; i++, j++)
          {
             code.append(", ");
             castArgument(code, parameterTypes[j], i);
          }
       }
 
       private static final String[][] primitiveExtractions;
 
       static{
          primitiveExtractions = new String[][]{
                {"((Boolean) " + ARGUMENTS + " [", "]).booleanValue()"},
                {"((Integer) " + ARGUMENTS + "[", "]).intValue()"},
                {"((Double) " + ARGUMENTS + "[", "]).doubleValue()"},
                {"((Float) " + ARGUMENTS + "[", "]).floatValue()"},
                {"((Character) " + ARGUMENTS + "[", "]).charValue()"},
                {"((Byte) " + ARGUMENTS + " [", "]).byteValue()"},
                {"((Long) " + ARGUMENTS + "[", "]).longValue()"},
                {"((Short) " + ARGUMENTS + "[", "]).shortValue()"}
          };
       }
       private final void castArgument(StringBuffer code, CtClass expectedType, int i)
       {
 
          if (expectedType.isPrimitive())
          {
             String[] extraction = null;
             if (expectedType == CtClass.booleanType)
             {
                extraction = primitiveExtractions[0];
             }
             else if (expectedType == CtClass.intType)
             {
                extraction = primitiveExtractions[1];
             }
             else if (expectedType == CtClass.doubleType)
             {
                extraction = primitiveExtractions[2];
             }
             else if (expectedType == CtClass.floatType)
             {
                extraction = primitiveExtractions[3];
             }
             else if (expectedType == CtClass.charType)
             {
                extraction = primitiveExtractions[4];
             }
             else if (expectedType == CtClass.byteType)
             {
                extraction = primitiveExtractions[5];
             }
             else if (expectedType == CtClass.longType)
             {
                extraction = primitiveExtractions[6];
             }
             else if (expectedType == CtClass.shortType)
             {
                extraction = primitiveExtractions[7];
             }
             code.append(extraction[0]);
             code.append(i);
             code.append(extraction[1]);
          }
          else
          {
             code.append("(");
             code.append(expectedType.getName());
             code.append(") ");
             code.append(ARGUMENTS);
             code.append("[");
             code.append(i);
             code.append("]");
          }
       }
 
       public void appendArgsArrayList(StringBuffer code, CtClass[] parameterTypes)
       {
          int j = firstArgIndex - 1;
          int totalParameters = parameterTypes.length - j;
 
          if (totalParameters == 0)
          {
             return;
          }
          code.append("(");
          code.append(parameterTypes[j++].getName());
          code.append(") ");
          code.append(ARGUMENTS);
          code.append("[0]");
          
          for (int i = 1; i < totalParameters; i++, j++)
          {
             code.append(", (");
             code.append(parameterTypes[j].getName());
             code.append(") ");
             code.append(ARGUMENTS);
             code.append("[");
             code.append(i);
             code.append("]");
          }
       }
    }
    
    public static final String INFO_FIELD = "info";
    public static final String INVOKE_JOINPOINT = "invokeJoinpoint";
    public static final String INVOKE_TARGET = "invokeTarget";
    public static final String DISPATCH = "dispatch";
    protected static final String TARGET_FIELD = "tgt";
    protected static final String GENERATED_CLASS_ADVISOR = GeneratedClassAdvisor.class.getName();
    public static final String GENERATE_JOINPOINT_CLASS = "generateJoinPointClass";
    private static final String CURRENT_ADVICE = "super.currentInterceptor";
    public static final String JOINPOINT_FIELD_PREFIX = "joinpoint_";
    public static final String JOINPOINT_CLASS_PREFIX = "JoinPoint_";
    public static final String GENERATOR_PREFIX = "generator_";
    private static final String RETURN_VALUE = "ret";
    private static final String THROWABLE = "t";
    private static final String ARGUMENTS= " arguments";
    protected static final CtClass[] EMPTY_CTCLASS_ARRAY = new CtClass[0];
 
    private JoinPointInfo oldInfo;
    protected JoinPointInfo info;
    private JoinPointParameters parameters;
    private static int increment;
    private Class advisorClass;
    protected GeneratedClassAdvisor advisor;
    protected String joinpointClassName;
    protected String joinpointFieldName;
    private String joinpointFqn;
    private Field joinpointField;
    private Field generatorField;
    private boolean initialised;
    
    protected JoinPointGenerator(GeneratedClassAdvisor advisor, JoinPointInfo info,
          JoinPointParameters parameters)
    {
       this.info = info;
       this.parameters = parameters;
       this.advisor = advisor;
       this.advisorClass = advisor.getClass();
       Class[] interfaces = advisorClass.getInterfaces();
       
       for (int i = 0 ; i < interfaces.length ; i++)
       {
          if (interfaces[i].equals(InstanceAdvisor.class))
          {
             //The InstanceAdvisor extends the Advisor, which is what contains the JoinPoint field
             advisorClass = advisorClass.getSuperclass();
             break;
          }
       }
 
       initialiseJoinPointNames();
    
       findAdvisedField(advisorClass, info);
    }
    
    public void rebindJoinpoint(JoinPointInfo newInfo)
    {
       try
       {
          if (joinpointField == null) return;
          if (initialised && oldInfo != null)
          {
             //We are not changing any of the bindings
             if (oldInfo.equalChains(newInfo)) return;
          }
          oldInfo = info.copy();
          info = newInfo;
 
          joinpointField.set(advisor, null);
 
          if (info.getInterceptors() == null)
          {
             //Turn off interceptions, by setting the generator to null
             generatorField.set(advisor, null);
          }
          else
          {
             generatorField.set(advisor, this);
          }
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }   
    }
       
    /**
     * Called by the joinpoint if a interceptors were regenereated
     * Here for backwards compatiblity with AOP 1.5.0
     */
    public void generateJoinPointClass()
    {
       generateJoinPointClass(null);
    }
    
    /**
     * Called by the joinpoint if a interceptors were regenereated
     */
    public synchronized void generateJoinPointClass(ClassLoader classloader)
    {
       if (System.getSecurityManager() == null)
       {
          GenerateJoinPointClassAction.NON_PRIVILEGED.generateJoinPointClass(classloader, this);
       }
       else
       {
          GenerateJoinPointClassAction.PRIVILEGED.generateJoinPointClass(classloader, this);
       }
    }
     
    /**
     * Does the work for generateJoinPointClass()
     * @see JoinPointGenerator#generateJoinPointClass()
     */
    private void doGenerateJoinPointClass(ClassLoader classloader)
    {
       try
       {
          if (classloader  == null)
          {
             classloader = Thread.currentThread().getContextClassLoader();
          }
 
          AspectManager manager = AspectManager.instance();
          //ClassPool pool = manager.findClassPool(Thread.currentThread().getContextClassLoader());
          ClassPool pool = manager.findClassPool(classloader);
          GeneratedClassInfo generatedClass = generateJoinpointClass(pool, info);
          
          Class clazz = toClass(pool, generatedClass.getGenerated());
          Object obj = instantiateClass(clazz, generatedClass.getAroundSetups());
          
          joinpointField.set(advisor, obj);
       }
       catch (Throwable e)
       {
          e.printStackTrace();
          // AutoGenerated
          throw new RuntimeException("Error generating joinpoint class for joinpoint " + info, e);
          
       }
       initialised = true;
    }
 
    private Class toClass(ClassPool pool, CtClass ctclass) throws NotFoundException, CannotCompileException, ClassNotFoundException
    {
       return TransformerCommon.toClass(ctclass);
    }
    
    private Object instantiateClass(Class clazz, AdviceSetup[] aroundSetups) throws Exception
    {
       Constructor ctor = clazz.getConstructor(new Class[] {info.getClass()});
       Object obj;
       try
       {
          obj = ctor.newInstance(new Object[] {info});
       }
       catch (Exception e)
       {
          StringBuffer sb = new StringBuffer();
          throw new RuntimeException(debugClass(sb, clazz).toString());
       }
       
       for (int i = 0 ; i < aroundSetups.length ; i++)
       {
          if (aroundSetups[i].isNewCFlow())
          {
             Field field = clazz.getDeclaredField("cflow" + aroundSetups[i].useCFlowFrom());
             field.setAccessible(true);
             field.set(obj, aroundSetups[i].getCFlow());
          }
       }
       return obj;
    }
     
    private StringBuffer debugClass(StringBuffer sb, Class clazz)
    {
       sb.append("\n\t\t" + Modifier.toString(clazz.getModifiers()) + " " + clazz.getName() + " " + clazz.getClassLoader()); 
       Field[] fields = clazz.getDeclaredFields();
       for (int i = 0 ; i < fields.length ; i++)
       {
          sb.append("\n\t\t\t" + Modifier.toString(fields[i].getModifiers()) + " " + fields[i].getType().getName() + " " + fields[i].getName() + " " + fields[i].getType().getClassLoader());
       }
      
      Class superClass = clazz.getSuperclass();
      if (superClass != null && superClass != Object.class)
      {
         sb.append("\n\t\t\textends\n");
         debugClass(sb, superClass);
      }
      return sb;
    }
    
    
    private static synchronized int getIncrement()
    {
       return ++increment;
    }
    
    protected abstract void initialiseJoinPointNames();
 
    private GeneratedClassInfo generateJoinpointClass(ClassPool pool, JoinPointInfo newInfo) throws NotFoundException,
    CannotCompileException, ClassNotFoundException
    {
       CtClass superClass = pool.get(joinpointFqn);
       String className = getJoinpointClassName();
       try
       {
          CtClass clazz = TransformerCommon.makeClass(pool, className);
          clazz.setSuperclass(superClass);
          addUntransformableInterface(pool, clazz);
   
          AdviceSetupsByType setups = initialiseAdviceInfosAndAddFields(pool, clazz);
          
          createConstructors(pool, superClass, clazz, setups);
          createJoinPointInvokeMethod(
                superClass, 
                clazz, 
                isVoid(),
                setups);
   
          createInvokeNextMethod(clazz, isVoid(), setups.getAroundSetups());
   
          overrideDispatchMethods(superClass, clazz, newInfo);
          return new GeneratedClassInfo(clazz, setups.getAroundSetups());
       }
       catch (NotFoundException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
       catch (CannotCompileException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
       catch (ClassNotFoundException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
    }
 
    private String getJoinpointClassName()
    {
       Package pkg = advisor.getClass().getPackage();
       
       StringBuffer className = new StringBuffer();
       if (pkg != null)
       {
          className.append(pkg.getName());
          className.append(".");
       }
       className.append(joinpointClassName);
       className.append("_");
       className.append(getIncrement());
       
       return className.toString();
    }
 
    protected abstract boolean isVoid();
    protected abstract Class getReturnType(); 
    protected abstract AdviceMethodProperties getAdviceMethodProperties(AdviceSetup setup);
    
    protected boolean isCaller()
    {
       return false;
    }
    
    protected boolean hasCallingObject()
    {
       return false;
    }
    
    protected abstract boolean hasTargetObject();
    
    private boolean isStaticCall()
    {
       if (isCaller())
       {
          return !hasCallingObject();
       }
       else
       {
          return !hasTargetObject();
       }
    }
    
    private void findAdvisedField(Class advisorSuperClazz, JoinPointInfo info)
    {
       if (info.getClazz() == null)
       {
          return;
       }
       
       while (advisorSuperClazz != null && advisorSuperClazz.getDeclaringClass() != info.getClazz())
       {
          advisorSuperClazz = advisorSuperClazz.getSuperclass();
       }
       try
       {
          try
          {
             joinpointField = advisorSuperClazz.getDeclaredField(joinpointFieldName);
             SecurityActions.setAccessible(joinpointField);
             joinpointFqn = advisorSuperClazz.getDeclaringClass().getName() + "$" + joinpointClassName;
             
             try
             {
                generatorField = advisorSuperClazz.getDeclaredField(getJoinPointGeneratorFieldName());
                SecurityActions.setAccessible(generatorField);
             }
             catch(NoSuchFieldException e)
             {
                throw new RuntimeException("Found joinpoint field " + 
                      joinpointField.getName() + " in " + advisorSuperClazz.getName() +
                      " but no JoinPointGenerator field called " + getJoinPointGeneratorFieldName());
             }
          }
          catch (NoSuchFieldException e)
          {
             //GeneratedClassAdvisor is the base class for all generated advisors
             if (!advisorSuperClazz.getName().equals(GENERATED_CLASS_ADVISOR))
             {
                findAdvisedField(advisorSuperClazz.getSuperclass(), info);
             }
          }
       }
       catch (NoClassDefFoundError e)
       {
          throw e;
       }
    }
 
    private AdviceSetupsByType initialiseAdviceInfosAndAddFields(ClassPool pool, CtClass clazz) throws ClassNotFoundException, NotFoundException, CannotCompileException
    {
       HashMap<String, Integer> cflows = new HashMap<String, Integer>();
       AdviceSetup[] setups = new AdviceSetup[info.getInterceptors().length];
 
       for (int i = 0 ; i < info.getInterceptors().length ; i++)
       {
          setups[i] = new AdviceSetup(i, (GeneratedAdvisorInterceptor)info.getInterceptors()[i]);
          addAspectFieldAndGetter(pool, clazz, setups[i]);
          addCFlowFieldsAndGetters(pool, setups[i], clazz, cflows);
       }
    
       return new AdviceSetupsByType(setups);
    }
    
    private void addAspectFieldAndGetter(ClassPool pool, CtClass clazz, AdviceSetup setup) throws NotFoundException, CannotCompileException
    {
       CtClass aspectClass = setup.getAspectCtClass();
       
       if (!setup.shouldInvokeAspect())
       {
          return;
       }
 
       CtField field = new CtField(aspectClass, setup.getAspectFieldName(), clazz);
       field.setModifiers(Modifier.PRIVATE | Modifier.TRANSIENT);
       clazz.addField(field);
       
       String body = getAspectFieldGetterBody(setup); 
       CtMethod method = CtNewMethod.make(
             aspectClass, 
             setup.getAspectInitialiserName(), 
             new CtClass[0], 
             new CtClass[0], 
             body, 
             clazz);
       method.setModifiers(Modifier.PRIVATE);
       clazz.addMethod(method);
    }
 
    private String getAspectFieldGetterBody(AdviceSetup setup)
    {
       if (setup.requiresInstanceAdvisor())
       {
          String instanceAdvisor = (isCaller()) ?
                "org.jboss.aop.InstanceAdvisor ia = ((org.jboss.aop.Advised)callingObject)._getInstanceAdvisor();" :
                   "org.jboss.aop.InstanceAdvisor ia = ((org.jboss.aop.Advised)targetObject)._getInstanceAdvisor();";
                         
          return
             "{" +
             "   " + instanceAdvisor +
             "   org.jboss.aop.advice.GeneratedAdvisorInterceptor fw = (org.jboss.aop.advice.GeneratedAdvisorInterceptor)info.getInterceptors()[" + setup.getIndex() + "];" +
             "   Object o = fw.getPerInstanceAspect(info.getAdvisor(), info.getJoinpoint(), ia);" +
             "   return (" + setup.getAspectClass().getName() + ")o;" +
             "}";
       }
       else
       {
          return
             "{" +
             "   if (" + setup.getAspectFieldName() + " != null)" +
             "   {" +
             "      return " + setup.getAspectFieldName() + ";" +
             "   }" +
             "   org.jboss.aop.advice.GeneratedAdvisorInterceptor fw = (org.jboss.aop.advice.GeneratedAdvisorInterceptor)info.getInterceptors()[" + setup.getIndex() + "];" +
             "   Object o = fw.getAspect(info.getAdvisor(), info.getJoinpoint());" +
             "   return (" + setup.getAspectClass().getName() + ")o;" +
             "}";
       }
    }
    private void addCFlowFieldsAndGetters(ClassPool pool, AdviceSetup setup,
          CtClass clazz, HashMap<String, Integer> cflows)
          throws NotFoundException, CannotCompileException
    {
       if (setup.getCFlowString() != null)
       {
          Integer useCFlowIndex = cflows.get(setup.getCFlowString());
          if (useCFlowIndex == null)
          {
             useCFlowIndex = new Integer(setup.getIndex());
             cflows.put(setup.getCFlowString(), useCFlowIndex);
             
             CtField cflowX = new CtField(
                   pool.get(ASTCFlowExpression.class.getName()), 
                   "cflow" + useCFlowIndex, 
                   clazz);
             clazz.addField(cflowX);
             
             CtField matchesCFlowX = new CtField(
                   CtClass.booleanType, 
                   "matchesCflow" + useCFlowIndex, 
                   clazz);
             clazz.addField(matchesCFlowX);
             
             String initCFlowXBody = 
                "{" +
                "   org.jboss.aop.pointcut.CFlowMatcher matcher = new org.jboss.aop.pointcut.CFlowMatcher();" +
                "   return matcher.matches(" + cflowX.getName() + ", this);" +
                "}";
             CtMethod initCFlowX = CtNewMethod.make(
                   CtClass.booleanType,
                   "getCFlow" + useCFlowIndex,
                   new CtClass[0],
                   new CtClass[0],
                   initCFlowXBody,
                   clazz);
             clazz.addMethod(initCFlowX);
          }
          setup.setUseCFlowFrom(useCFlowIndex.intValue());
       }
    }
    
    private void createJoinPointInvokeMethod(CtClass superClass, CtClass clazz, boolean isVoid, AdviceSetupsByType setups) throws CannotCompileException, NotFoundException
    {
       CtMethod superInvoke = superClass.getDeclaredMethod(INVOKE_JOINPOINT);
       String code = null;
       try
       {
          code = createJoinpointInvokeBody(
                clazz, 
                setups,
                superInvoke.getExceptionTypes(), superInvoke.getParameterTypes());
          CtMethod invoke = CtNewMethod.make(
                superInvoke.getReturnType(), 
                superInvoke.getName(), 
                superInvoke.getParameterTypes(), 
                superInvoke.getExceptionTypes(), 
                code, 
                clazz);
          clazz.addMethod(invoke);
       }
       catch (CannotCompileException e)
       {
          throw new RuntimeException("Error compiling code for Joinpoint (" + info.getJoinpoint() +"): " + code + "\n - " + e + "\n - " + getMethodString(clazz, superInvoke.getName(), superInvoke.getParameterTypes()) + "\n - " + clazz.getName(), e);
       }
    }
    
    private String createJoinpointInvokeBody(CtClass joinpointClass,
          AdviceSetupsByType setups, CtClass[] declaredExceptions, CtClass[] parameterTypes)throws NotFoundException
    {
 
       StringBuffer code = new StringBuffer();
       code.append("{");
       if (!isVoid())
       {
          String ret = null;
          Class retType = getReturnType();
          if (retType.isPrimitive())
          {
             if (retType.equals(Boolean.TYPE)) ret = "false";
             else if (retType.equals(Character.TYPE)) ret = "'\\0'";
             else if (retType.equals(Byte.TYPE)) ret = "(byte)0";
             else if (retType.equals(Short.TYPE)) ret = "(short)0";
             else if (retType.equals(Integer.TYPE)) ret = "(int)0";
             else if (retType.equals(Long.TYPE)) ret = "0L";
             else if (retType.equals(Float.TYPE)) ret = "0.0f";
             else if (retType.equals(Double.TYPE)) ret =  "0.0d";
          }
          code.append("   " + ClassExpression.simpleType(getReturnType()) + "  " + RETURN_VALUE + " = " + ret + ";");
       }
       code.append("   try");
       code.append("   {");
       boolean argsFound = addInvokeCode(DefaultAdviceCallStrategy.getInstance(),
             setups.getBeforeSetups(), code);
       addAroundInvokeCode(code, setups, joinpointClass, argsFound, parameterTypes);
 
       argsFound = addInvokeCode(AfterAdviceCallStrategy.getInstance(),
             setups.getAfterSetups(), code) || argsFound;
       
       code.append("   }");
       code.append("   catch(java.lang.Throwable " + THROWABLE + ")");
       code.append("   {");
       argsFound = addInvokeCode(DefaultAdviceCallStrategy.getInstance(),
             setups.getThrowingSetups(), code) || argsFound;
       addHandleExceptionCode(code, declaredExceptions);
       code.append("   }");
       if (!isVoid())
       {
          code.append("   return " + RETURN_VALUE + ";");
       }
       code.append("}");;
       
       if (argsFound)
       {
          code.insert(1, parameters.declareArgsArray(parameterTypes.length));
       }
       return code.toString();
    }   
 
    private boolean addInvokeCode(AdviceCallStrategy callStrategy, AdviceSetup[] setups, StringBuffer code) throws NotFoundException
    {
       if (setups == null || setups.length == 0)
       {
          return false;
       }
       boolean argsFound = false;
       String key = callStrategy.generateKey(this);
       for (int i = 0 ; i < setups.length ; i++)
       {
          argsFound = callStrategy.appendAdviceCall(setups[i], key, code, this)
             || argsFound;
       }
       return argsFound;
    }
    
    
    private void addAroundInvokeCode(StringBuffer code, AdviceSetupsByType setups,
          CtClass joinpointClass, boolean argsFound, CtClass[] parameterTypes) throws NotFoundException
    {
       if (setups.getAroundSetups() != null)
       {
          StringBuffer aspects = new StringBuffer();
          StringBuffer cflows = new StringBuffer();
          
          AdviceSetup[] asetups = setups.getAllSetups(); 
          for (int i = 0 ; i < asetups.length ; i++)
          {
             if (asetups[i].requiresInstanceAdvisor())
             {
             }
             else
             {
                aspects.append(", ");
                aspects.append(asetups[i].getAspectFieldName());
             }
          
             if (asetups[i].isNewCFlow())
             {
                cflows.append(", cflow" + asetups[i].getIndex());
             }
          }
 
          code.append("      if(" + INFO_FIELD + ".getInterceptors() != null)");
          code.append("      {");
          code.append("         " + joinpointFqn + " jp = new " + joinpointClass.getName() + "(this");
          if (argsFound)
          {
             parameters.appendParameterListWithArgs(code, parameterTypes, true);
             
          }
          else
          {
             code.append(", $$");
          }
          
          code.append(aspects.toString() + cflows.toString() + ");");
          if (!isVoid())
          {
             code.append("          " + RETURN_VALUE + " = ($r)");
          }
          code.append("jp.invokeNext();");
          
          code.append("      }");
          code.append("      else");
          code.append("      {");
          
          addDispatchCode(code, parameterTypes, argsFound);
          
          code.append("      }");
       }
       else
       {
          addDispatchCode(code, parameterTypes, argsFound);
       }
    }
 
    private final void addDispatchCode(StringBuffer code, CtClass[] parameterTypes,
          boolean argsFound)
    {
       if (! isVoid())
       {
          code.append("          " + RETURN_VALUE + " = ");
       }
       code.append("super.dispatch(");
       if (argsFound)
       {
          parameters.appendParameterListWithArgs(code, parameterTypes, false);
       }
       else
       {
          code.append("$$");
       }
       code.append(");");
    }
    
    private void addHandleExceptionCode(StringBuffer code, CtClass[] declaredExceptions)
    {
       for (int i = 0 ; i < declaredExceptions.length ; i++)
       {
          code.append("if (t instanceof " + declaredExceptions[i].getName() + ")");
          code.append("   throw (" + declaredExceptions[i].getName() + ")t;");
       }
       
       code.append("if (t instanceof java.lang.RuntimeException)");
       code.append(   "throw t;");
       
       code.append("throw new java.lang.RuntimeException(t);");
    }
 
    private void createInvokeNextMethod(CtClass jp, boolean isVoid, AdviceSetup[] aroundSetups) throws NotFoundException, CannotCompileException
    {
       if (aroundSetups == null) return;
       
       CtMethod method = jp.getSuperclass().getSuperclass().getDeclaredMethod("invokeNext");
       CtMethod invokeNext = CtNewMethod.copy(method, jp, null);
       
       String code = createInvokeNextMethodBody(jp, isVoid, aroundSetups);
       
       try
       {
          invokeNext.setBody(code);
       }
       catch (CannotCompileException e)
       {
          throw new RuntimeException("Error creating invokeNext method: " + code, e);
       }
       
       jp.addMethod(invokeNext);
    }
 
    private String createInvokeNextMethodBody(CtClass jp, boolean isVoid, AdviceSetup[] aroundSetups) throws NotFoundException
    {
       final String returnStr = (isVoid) ? "" : "return ($w)";
 
       StringBuffer body = new StringBuffer();
       body.append("{");
       body.append("   try{");
       body.append("      switch(++" + CURRENT_ADVICE + "){");
       
       addInvokeCode(AroundAdviceCallStrategy.getInstance(), aroundSetups, body);
       body.append("      default:");
       body.append("         " + returnStr + "this.dispatch();");
       body.append("      }");
       body.append("   }finally{");
       body.append("      --" + CURRENT_ADVICE + ";");
       body.append("   }");
       body.append("   return null;");
       body.append("}");
       
       return body.toString();
    }
    
    private void createConstructors(ClassPool pool, CtClass superClass, CtClass clazz, AdviceSetupsByType setups) throws NotFoundException, CannotCompileException
    {
       CtConstructor[] superCtors = superClass.getDeclaredConstructors();
       if (superCtors.length != 2 && !this.getClass().equals(MethodJoinPointGenerator.class))
       {
          throw new RuntimeException("JoinPoints should only have 2 and only constructors, not " + superCtors.length);
       }
       else if (superCtors.length != 3 && this.getClass().equals(MethodJoinPointGenerator.class))
       {
          throw new RuntimeException("Method JoinPoints should only have 2 and only constructors, not " + superCtors.length);
       }
       
       int publicIndex = -1;
       int protectedIndex = -1;
       int defaultIndex = -1;
       
       for (int i = 0 ; i < superCtors.length ; i++)
       {
          int modifier = superCtors[i].getModifiers();
          if (Modifier.isPublic(modifier))
          {
             if (superCtors[i].getParameterTypes().length == 0) defaultIndex = i;
             else publicIndex = i;
          }
          else if (Modifier.isProtected(modifier))
          {
             protectedIndex = i;
          }
       }
       
       if (publicIndex < 0 || protectedIndex < 0)
       {
          throw new RuntimeException("One of the JoinPoint constructors should be public, the other protected");
       }
       
       if (defaultIndex >= 0)
       {
          createDefaultConstructor(superCtors[defaultIndex], clazz);
       }
       
       createPublicConstructor(superCtors[publicIndex], clazz, setups);
       createProtectedConstructor(pool, superCtors[protectedIndex], clazz, setups);
       createCopyConstructorAndMethod(pool, clazz);
    }
 
    /**
     * Currently only method joinpoints need serialization and thus a default ctor
     */
    private void createDefaultConstructor(CtConstructor superCtor, CtClass clazz) throws CannotCompileException
    {
       CtConstructor ctor = CtNewConstructor.defaultConstructor(clazz);
       clazz.addConstructor(ctor);
    }
    
    /**
     * This is the constructor that will be called by the GeneratedClassAdvisor, make sure it
     * initialises all the non-per-instance aspects
     */
    private void createPublicConstructor(CtConstructor superCtor, CtClass clazz, AdviceSetupsByType setups)throws CannotCompileException, NotFoundException
    {
       StringBuffer body = new StringBuffer();
       try
       {
          body.append("{super($$);");
   
          //Initialise all the aspects not scoped per_instance or per_joinpoint
          AdviceSetup[] allSetups = setups.getAllSetups();
          for (int i = 0 ; i < allSetups.length ; i++)
          {
             if (!allSetups[i].requiresInstanceAdvisor())
             {
                body.append(allSetups[i].getAspectFieldName() + " = " + allSetups[i].getAspectInitialiserName() + "();");
             }
          }
          
          body.append("}");
 
          CtConstructor ctor = CtNewConstructor.make(superCtor.getParameterTypes(), superCtor.getExceptionTypes(), body.toString(), clazz);
          ctor.setModifiers(superCtor.getModifiers());
          clazz.addConstructor(ctor);
       }
       catch (CannotCompileException e)
       {
          // AutoGenerated
          throw new CannotCompileException("Error compiling. Code \n" + body.toString(), e);
       }
    }
    
    /**
     * This is the constructor that will be called by the invokeJoinPoint() method, make sure it
     * copies across all the non-per-instance aspects
     */
    private void createProtectedConstructor(ClassPool pool, CtConstructor superCtor,
          CtClass clazz, AdviceSetupsByType setups)
          throws CannotCompileException, NotFoundException
    {
       
       CtClass[] superParams = superCtor.getParameterTypes();
       ArrayList<AdviceSetup> aspects = new ArrayList<AdviceSetup>(); 
       ArrayList<Integer> cflows = new ArrayList<Integer>();
       StringBuffer adviceInit = new StringBuffer(); 
       
       AdviceSetup[] allSetups = setups.getAllSetups();
       for (int i = 0 ; i < allSetups.length ; i++)
       {
          if (!allSetups[i].shouldInvokeAspect())
          {
             continue;
          }
          
          if (allSetups[i].requiresInstanceAdvisor())
          {
             adviceInit.append(allSetups[i].getAspectFieldName());
             adviceInit.append(" = ");
             adviceInit.append(allSetups[i].getAspectInitialiserName());
             adviceInit.append("();");
          }
          else
          {
             aspects.add(allSetups[i]);
          }
          
          if (allSetups[i].isNewCFlow())
          {
             cflows.add(allSetups[i].useCFlowFrom());
          }
       }
 
       StringBuffer cflowInit = new StringBuffer();
 
       //Set up the parameters
       CtClass[] params = new CtClass[superParams.length + aspects.size() + cflows.size()];
       System.arraycopy(superParams, 0, params, 0, superParams.length);
 
       for (int i = 0 ; i < aspects.size() ; i++)
       {
          AdviceSetup setup = (AdviceSetup)aspects.get(i);
          params[i + superParams.length] = setup.getAspectCtClass();
          adviceInit.append("this." + setup.getAspectFieldName() + " = $" + (i + superParams.length + 1) + ";");
       }
       final int aspectsLength = superParams.length + aspects.size();
       if (cflows.size() > 0 )
       {
          CtClass astCFlowExpr = pool.get(ASTCFlowExpression.class.getName());
          for (int i = 0 ; i < cflows.size() ; i++)
          {
             params[i + aspectsLength] = astCFlowExpr;
             cflowInit.append("cflow" + cflows.get(i) + "= $" + (i + aspectsLength + 1) + ";");
             cflowInit.append("matchesCflow" + cflows.get(i) + " = getCFlow" + allSetups[i].useCFlowFrom() + "();");
          }
       }
       
       StringBuffer body = new StringBuffer("{super(");
       for (int i = 0 ; i < superParams.length ; i++)
       {
          if (i > 0)
          {
             body.append(", ");
          }
          body.append("$" + (i + 1));
       
       }
       body.append(");");
       body.append(adviceInit.toString());
       body.append(cflowInit.toString());
       body.append("}");   
       CtConstructor ctor = CtNewConstructor.make(
           params, 
           superCtor.getExceptionTypes(),
           body.toString(),
           clazz);
          ctor.setModifiers(superCtor.getModifiers());
          clazz.addConstructor(ctor);
    }
 
 
    private void createCopyConstructorAndMethod(ClassPool pool, CtClass clazz) throws NotFoundException, CannotCompileException
    {
       //Add all fields from this class and all superclasses
       StringBuffer body = new StringBuffer();
       body.append("{");
       body.append("   super($1.info);");
 
       CtClass superClass = clazz;
       while (superClass != null && !superClass.getName().equals("java.lang.Object"))
       {
          CtField[] fields = superClass.getDeclaredFields();
          for (int i = 0 ; i < fields.length ; i++)
          {
             if (Modifier.isPrivate(fields[i].getModifiers()) && fields[i].getDeclaringClass() != clazz)
             {
                continue;
             }
             
             if (Modifier.isFinal(fields[i].getModifiers()) || Modifier.isStatic(fields[i].getModifiers()) )
             {
                continue;
             }
             
             body.append("   this." + fields[i].getName() + " = $1." + fields[i].getName() + ";");
          }
          superClass = superClass.getSuperclass();
       }
       body.append("}");
       
       CtConstructor copyCtor = CtNewConstructor.make(new CtClass[] {clazz}, new CtClass[0], body.toString(), clazz);
       copyCtor.setModifiers(Modifier.PRIVATE);
       clazz.addConstructor(copyCtor);
       
       CtMethod superCopy = pool.get(Invocation.class.getName()).getDeclaredMethod("copy");
       String copyBody = 
          "{" +
          "   return new " + clazz.getName() + "(this);" +
          "}";
       CtMethod copy = CtNewMethod.make(
             superCopy.getReturnType(), 
             superCopy.getName(), 
             new CtClass[0], 
             new CtClass[0], 
             copyBody, 
             clazz);
       clazz.setModifiers(Modifier.PUBLIC);
       clazz.addMethod(copy);
    }
 
    /**
     * Normal people don't want to override the dispatch method
     */
    protected void overrideDispatchMethods(CtClass superClass, CtClass clazz, JoinPointInfo newInfo) throws CannotCompileException, NotFoundException
    {     
    }
    
    /**
     * For ConByXXXX,  If target constructor is execution advised, replace it with a call to the constructor wrapper
     */
    protected void overrideDispatchMethods(CtClass superClass, CtClass clazz, CallerConstructorInfo cinfo) throws NotFoundException, CannotCompileException
    {
       if (cinfo.getWrappingMethod() == null)
       {
          return;
       }
       
       CtMethod[] superDispatches = JavassistUtils.getDeclaredMethodsWithName(superClass, DISPATCH);
       
       if (superDispatches.length > 2)
       {
          if (AspectManager.verbose) System.out.println("[warn] - Too many dispatch() methods found in " + superClass.getName());
       }
       
       for (int i = 0 ; i < superDispatches.length ; i++)
       {
          CtMethod wrapperMethod = ReflectToJavassist.methodToJavassist(cinfo.getWrappingMethod());
          CtClass[] params = wrapperMethod.getParameterTypes(); 
          
          StringBuffer parameters = new StringBuffer("(");
          if (superDispatches[i].getParameterTypes().length == 0)
          {
             //This is the no params version called by invokeNext() for around advices
             for (int j = 0 ; j < params.length ; j++)
             {
                if (j > 0)parameters.append(", ");
                parameters.append("arg" + j);
             }
          }
          else
          {
             //This is the no parameterized version called by invokeJoinPoint() when there are no around advices
             int offset = (hasCallingObject()) ? 1 : 0; 
             for (int j = 0 ; j < params.length ; j++)
             {
                if (j > 0)parameters.append(", ");
                parameters.append("$" + (j + offset + 1));
             }
          }
          parameters.append(")");
 
          String body = 
             "{ return " + cinfo.getConstructor().getDeclaringClass().getName() + "." + cinfo.getWrappingMethod().getName() +  parameters + ";}";
    
          try
          {
             CtMethod dispatch = CtNewMethod.make(
                   superDispatches[i].getReturnType(), 
                   superDispatches[i].getName(), 
                   superDispatches[i].getParameterTypes(), 
                   superDispatches[i].getExceptionTypes(), 
                   body, 
                   clazz);
             dispatch.setModifiers(superDispatches[i].getModifiers());
             clazz.addMethod(dispatch);
          }
          catch (CannotCompileException e)
          {
             throw new RuntimeException("Could not compile code " + body + " for method " + getMethodString(clazz, superDispatches[i].getName(), superDispatches[i].getParameterTypes()), e);
          }
          
       }
    }
    
    protected static void addUntransformableInterface(Instrumentor instrumentor, CtClass clazz) throws NotFoundException
    {
       addUntransformableInterface(instrumentor.getClassPool(), clazz);
    }
 
    protected static void addUntransformableInterface(ClassPool pool, CtClass clazz) throws NotFoundException
    {
       CtClass untransformable = pool.get(Untransformable.class.getName());
       clazz.addInterface(untransformable);
    }
 
    protected abstract String getJoinPointGeneratorFieldName();
    protected static String getMethodString(CtClass joinpoint, String method, CtClass[] params)
    {
       StringBuffer sb = new StringBuffer();
       sb.append(joinpoint);
       sb.append(".");
       sb.append("name");
       sb.append("(");
       for (int i = 0 ; i < params.length ; i++)
       {
          if (i > 0) sb.append(", ");
          sb.append(params[i].getName());
       }
       sb.append(")");
       
       return sb.toString();
    }
    
    protected class AdviceSetup
    {
       int index;
       Class aspectClass;
       CtClass aspectCtClass;
       String adviceName;
       Scope scope;
       String registeredName;
       String cflowString;
       ASTCFlowExpression cflowExpr;
       int cflowIndex;
       boolean isBefore;
       boolean isAfter;
       boolean isThrowing;
       AdviceMethodProperties adviceMethodProperties;
       
       AdviceSetup(int index, GeneratedAdvisorInterceptor ifw) throws ClassNotFoundException, NotFoundException
       {
          this.index = index;
          scope = ifw.getScope();
          adviceName = ifw.getAdviceName();
          registeredName = ifw.getRegisteredName();
          cflowString = ifw.getCFlowString();
          cflowExpr = ifw.getCflowExpression();
          if (ifw.isAspectFactory())
          {
             Object aspectInstance = ((GeneratedAdvisorInterceptor)info.getInterceptors()[index]).getAspect(info.getAdvisor(), info.getJoinpoint(), true);
             aspectClass = aspectInstance.getClass();
          }
          else
          {
             aspectClass = Thread.currentThread().getContextClassLoader().loadClass(ifw.getAspectClassName());
          }
          aspectCtClass = ReflectToJavassist.classToJavassist(aspectClass);
 
          isBefore = ifw.isBefore();
          isAfter = ifw.isAfter();
          isThrowing = ifw.isThrowing();
       }
       
       String getAdviceName()
       {
          return adviceName;
       }
       
       
       Class getAspectClass()
       {
          return aspectClass;
       }
       
       CtClass getAspectCtClass()
       {
          return aspectCtClass;
       }
       
       Scope getScope()
       {
          return scope;
       }
       
       int getIndex()
       {
          return index;
       }
       
       String getRegisteredName()
       {
          return registeredName;
       }
       
       String getAspectFieldName()
       {
          StringBuffer name = new StringBuffer();
          if (isAround())
          {
             name.append("around");
          }
          else if (isBefore())
          {
             name.append("before");
          }
          else if (isAfter())
          {
             name.append("after");
          }
          else if (isThrowing())
          {
             name.append("throwing");
          }
          else
          {
             if (AspectManager.verbose) System.out.println("[warn] Unsupported type of advice");
          }
          name.append(index + 1);
          return name.toString();
       }
       
       String getAspectInitialiserName()
       {
          StringBuffer name = new StringBuffer();
          if (isAround())
          {
             name.append("getAround");
          }
          else if (isBefore())
          {
             name.append("getBefore");
          }
          else if (isAfter())
          {
             name.append("getAfter");
          }
          else if (isThrowing())
          {
             name.append("getThrowing");
          }
          else
          {
             if (AspectManager.verbose) System.out.println("[warn] Unsupported type of advice");
          }
          name.append(index + 1);
          return name.toString();
       }
       
       boolean isPerInstance()
       {
          return scope == Scope.PER_INSTANCE;
       }
       
       boolean isPerJoinpoint()
       {
          return scope == Scope.PER_JOINPOINT;
       }
       
       boolean shouldInvokeAspect()
       {
          return !(isPerInstance() && isStaticCall());
       }
       
       boolean requiresInstanceAdvisor()
       {
          return (isPerInstance() || (isPerJoinpoint() && !isStaticCall()));
       }
       
       String getCFlowString()
       {
          return cflowString;
       }
       
       ASTCFlowExpression getCFlow()
       {
          return cflowExpr;
       }
       
       void setUseCFlowFrom(int index)
       {
          cflowIndex = index;
       }
       
       int useCFlowFrom()
       {
          return cflowIndex;
       }
       
       boolean isNewCFlow()
       {
          return (getCFlowString() != null && index == cflowIndex);
       }
 
       boolean isAfter()
       {
          return isAfter;
       }
 
       boolean isBefore()
       {
          return isBefore;
       }
 
       boolean isThrowing()
       {
          return isThrowing;
       }
       
       boolean isAround()
       {
          return !isAfter && !isBefore && !isThrowing;
       }
 
       public AdviceMethodProperties getAdviceMethodProperties()
       {
          return adviceMethodProperties;
       }
 
       public void setAdviceMethodProperties(AdviceMethodProperties adviceMethodProperties)
       {
          this.adviceMethodProperties = adviceMethodProperties;
       }
    }
    
    private class GeneratedClassInfo
    {
       CtClass generated;
       AdviceSetup[] aroundSetups;
       
       GeneratedClassInfo(CtClass generated, AdviceSetup[] aroundSetups)
       {
          this.generated = generated;
          this.aroundSetups = aroundSetups;
       }
       
       CtClass getGenerated()
       {
          return generated;
       }
       
       AdviceSetup[] getAroundSetups()
       {
          return (aroundSetups == null) ? new AdviceSetup[0] : aroundSetups;
       }
    }
    
    private class AdviceSetupsByType
    {
       AdviceSetup[] allSetups; 
       AdviceSetup[] beforeSetups;
       AdviceSetup[] afterSetups;
       AdviceSetup[] throwingSetups;
       AdviceSetup[] aroundSetups;
 
       AdviceSetupsByType(AdviceSetup[] setups)
       {
          allSetups = setups;
          ArrayList<AdviceSetup> beforeAspects = null;
          ArrayList<AdviceSetup> afterAspects = null;
          ArrayList<AdviceSetup> throwingAspects = null;
          ArrayList<AdviceSetup> aroundAspects = null;
 
          for (int i = 0 ; i < setups.length ; i++)
          {
             if (setups[i].isBefore())
             {
                if (beforeAspects == null) beforeAspects = new ArrayList<AdviceSetup>();
                
                AdviceMethodProperties properties = AdviceMethodFactory.BEFORE.findAdviceMethod(getAdviceMethodProperties(setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   beforeAspects.add(setups[i]);
                   continue;
                }
             }
             else if (setups[i].isAfter())
             {
                if (afterAspects == null) afterAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.AFTER.findAdviceMethod(getAdviceMethodProperties(setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   afterAspects.add(setups[i]);
                   continue;
                }
             }
             else if (setups[i].isThrowing())
             {
                if (throwingAspects == null) throwingAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.THROWING.findAdviceMethod(getAdviceMethodProperties(setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   throwingAspects.add(setups[i]);
                   continue;
                }
             }
             else
             {
                if (aroundAspects == null) aroundAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.AROUND.findAdviceMethod(getAdviceMethodProperties(setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   aroundAspects.add(setups[i]);
                   continue;
                }
             }
             
             if (AspectManager.verbose)
             {
                System.out.print("[warn] No matching advice called '" + setups[i].getAdviceName() + 
                      "' could be found in " + setups[i].getAspectClass().getName() +
                      " for joinpoint " + info + ":");
                System.out.println(AdviceMethodFactory.getAdviceMatchingMessage());
             }
          }
          beforeSetups = (beforeAspects == null) ? null : (AdviceSetup[])beforeAspects.toArray(new AdviceSetup[beforeAspects.size()]);
          afterSetups = (afterAspects == null) ? null : (AdviceSetup[])afterAspects.toArray(new AdviceSetup[afterAspects.size()]);
          throwingSetups = (throwingAspects == null) ? null : (AdviceSetup[])throwingAspects.toArray(new AdviceSetup[throwingAspects.size()]);
          aroundSetups = (aroundAspects == null) ? null : (AdviceSetup[])aroundAspects.toArray(new AdviceSetup[aroundAspects.size()]);
       }
 
       public AdviceSetup[] getAllSetups()
       {
          return allSetups;
       }
       
       public AdviceSetup[] getAfterSetups()
       {
          return afterSetups;
       }
 
       public AdviceSetup[] getAroundSetups()
       {
          return aroundSetups;
       }
 
       public AdviceSetup[] getBeforeSetups()
       {
          return beforeSetups;
       }
 
       public AdviceSetup[] getThrowingSetups()
       {
          return throwingSetups;
       }
    }
 
    private interface GenerateJoinPointClassAction
    {
       void generateJoinPointClass(ClassLoader classloader, JoinPointGenerator joinPointGenerator);
       
       GenerateJoinPointClassAction PRIVILEGED = new GenerateJoinPointClassAction()
       {
          public void generateJoinPointClass(final ClassLoader classloader, final JoinPointGenerator joinPointGenerator) 
          {
             try
             {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
                {
                   public Object run() throws Exception
                   {
                      joinPointGenerator.doGenerateJoinPointClass(classloader);
                      return null;
                   }
                });
             }
             catch (PrivilegedActionException e)
             {
                Exception actual = e.getException();
                if (actual instanceof RuntimeException)
                {
                   throw (RuntimeException)actual;
                }
                throw new RuntimeException(actual);
             }
          }
       };
 
       GenerateJoinPointClassAction NON_PRIVILEGED = new GenerateJoinPointClassAction()
       {
          public void generateJoinPointClass(ClassLoader classloader, JoinPointGenerator joinPointGenerator)
          {
             joinPointGenerator.doGenerateJoinPointClass(classloader);
          }
       };
    }
 
 
    private static abstract class AdviceCallStrategy
    {
       public abstract String generateKey(JoinPointGenerator generator);
       public abstract boolean appendAdviceCall(AdviceSetup setup, String key,
            StringBuffer code, JoinPointGenerator generator) throws NotFoundException;
       
       private final boolean appendAdviceCall(AdviceSetup setup, StringBuffer code,
             boolean isAround, JoinPointGenerator generator) 
       {
          AdviceMethodProperties properties = setup.getAdviceMethodProperties();
          if (properties == null)
          {
             return false;
          }
          code.append(setup.getAspectFieldName());
          code.append(".");
          code.append(setup.getAdviceName());
          code.append("(");
          
          
          final int[] args = properties.getArgs();
          boolean argsFound = false;
          if (args.length > 0)
          {
             final Class[] adviceParams = properties.getAdviceMethod().getParameterTypes();
             argsFound = appendParameters(code, args[0], adviceParams[0],
                   isAround, properties, generator);
             for (int i = 1 ; i < args.length ; i++)
             {
                code.append(", ");
                argsFound = appendParameters(code, args[i], adviceParams[i],
                      isAround, properties, generator) || argsFound;
             }
          }
          
          code.append(");");
          return argsFound;
       }
       
       private final boolean appendParameters(StringBuffer code, final int arg,
             final Class adviceParam, boolean isAround,
             AdviceMethodProperties properties, JoinPointGenerator generator)
       {
          code.append("(");
          // In case of overloaded methods javassist sometimes seems to pick up the wrong method - use explicit casts to get hold of the parameters
          code.append(ClassExpression.simpleType(adviceParam));
          code.append(")");
          switch(arg)
          {
          case AdviceMethodProperties.INVOCATION_ARG:
             code.append("this");
             break;
          case AdviceMethodProperties.JOINPOINT_ARG:
             code.append(INFO_FIELD);
             break;
          case AdviceMethodProperties.RETURN_ARG:
             code.append(RETURN_VALUE);
             break;
          case AdviceMethodProperties.THROWABLE_ARG:
             code.append(THROWABLE);
             break;
          case AdviceMethodProperties.TARGET_ARG:
             if (properties.getTargetType() == null)
             {
                code.append("null");
             }
             else if (isAround)
             {
                code.append(TARGET_FIELD);
             }
             else
             {
                code.append("$1");
             }
             return true;
          case AdviceMethodProperties.ARGS_ARG:
             code.append(ARGUMENTS);
             return true;
          default:
             if (isAround)
             {
                code.append("this.arg");
                code.append(arg);
             }
             else 
             {
                //The parameter array is 1-based, and the invokeJoinPoint method may also take the target and caller objects
                code.append("$");
                code.append(arg + generator.parameters.getFirstArgIndex());
             }
          }
          return false;
       }
    }
    
    private static class AroundAdviceCallStrategy extends AdviceCallStrategy
    {
       private static ThreadLocal<AroundAdviceCallStrategy> INSTANCE;
       
       static
       {
          INSTANCE = new ThreadLocal<AroundAdviceCallStrategy>();
          INSTANCE.set(new AroundAdviceCallStrategy());
       }
       
       public static final AroundAdviceCallStrategy getInstance()
       {
         AroundAdviceCallStrategy strategy = INSTANCE.get();
         if (strategy == null)
         {
            strategy = new AroundAdviceCallStrategy();
            INSTANCE.set(strategy);
         }
         return strategy;
       }
       
       private AroundAdviceCallStrategy() {}
       
       private int addedAdvice = 0;
       
       public String generateKey(JoinPointGenerator generator)
       {
          addedAdvice = 0;
          if (generator.isVoid())
          {
             return "";
          }
          return "return ($w)";
       }
       
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer code, JoinPointGenerator generator)
       {
          if (!setup.shouldInvokeAspect())
          {
             //We are invoking a static method/ctor, do not include advice in chain
             return false;
          }
       
          boolean result = false;
          AdviceMethodProperties properties = AdviceMethodFactory.AROUND.
             findAdviceMethod(generator.getAdviceMethodProperties(setup));
          if (properties == null || properties.getAdviceMethod() == null)
          {
             // throw new RuntimeException("DEBUG ONLY Properties was null " + 
             // aroundSetups[i].getAspectClass().getName() + "." + aroundSetups[i].getAdviceName());
             return false;
          }
          
          code.append("      case ");
          code.append(++addedAdvice);
          code.append(":");
          
          if (setup.getCFlowString() != null)
          {
             code.append("         if (matchesCflow" + setup.useCFlowFrom() + ")");
             code.append("         {");
             result = appendAroundCallString(code, key, setup, properties, generator);
             code.append("         }");
             code.append("         else");
             code.append("         {");
             code.append("            ");
             code.append(key);
             code.append(" invokeNext();");
             code.append("         }");
          }
          else
          {
             result = appendAroundCallString(code, key, setup, properties, generator);
          }
          
          code.append("      break;");
          return result;
       }
       
       
       public boolean appendAroundCallString(StringBuffer invokeNextBody,
             String returnStr, AdviceSetup setup, AdviceMethodProperties properties,
             JoinPointGenerator generator)
       {
          int[] args = properties.getArgs();
          
          final boolean firstParamIsInvocation =
             (args.length >= 1 && args[0] == AdviceMethodProperties.INVOCATION_ARG);
 
          if (!firstParamIsInvocation)
          {
             invokeNextBody.append("try{");
             invokeNextBody.append("   org.jboss.aop.joinpoint.CurrentInvocation.push(this); ");
          }
          invokeNextBody.append("   ");
          invokeNextBody.append(returnStr);
          invokeNextBody.append(" ");
          boolean result = super.appendAdviceCall(setup, invokeNextBody, true,
                generator);
          
          if (!firstParamIsInvocation)
          {
             invokeNextBody.append("}finally{");
             invokeNextBody.append("   org.jboss.aop.joinpoint.CurrentInvocation.pop(); ");
             invokeNextBody.append("}");
          }
          return result;
       }
    }
    
    private static class DefaultAdviceCallStrategy extends AdviceCallStrategy
    {
       private static DefaultAdviceCallStrategy INSTANCE = new DefaultAdviceCallStrategy();
       
       public static final DefaultAdviceCallStrategy getInstance()
       {
          return INSTANCE;
       }
       
       private DefaultAdviceCallStrategy() {}
 
       public String generateKey(JoinPointGenerator generator)
       {
          return null;
       }
       
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer code, JoinPointGenerator generator)
       {
          return super.appendAdviceCall(setup, code, false, generator);
       }
    }
    
    private static class AfterAdviceCallStrategy extends AdviceCallStrategy
    {
       private static AfterAdviceCallStrategy INSTANCE = new AfterAdviceCallStrategy();
       
       public static final AfterAdviceCallStrategy getInstance()
       {
          return INSTANCE;
       }
       
       private AfterAdviceCallStrategy() {}
 
       public String generateKey(JoinPointGenerator generator)
       {
          if (generator.isVoid())
          {
             return "";
          }
          return "          " + RETURN_VALUE + " = (" +
          generator.getReturnType().getName() + ")";
       }
 
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer code, JoinPointGenerator generator) throws NotFoundException
       {
          AdviceMethodProperties properties = setup.getAdviceMethodProperties();
          if (properties != null && !properties.isAdviceVoid())
          {
             code.append(key);
          }
          return super.appendAdviceCall(setup, code, false, generator);
       }
    }
 }
