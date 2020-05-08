 /*
  * Copyright (C) 1998-2001 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 package org.webmacro.directive;
 
 /**
  * NOTE: this class is highly experimental at this point.
  * Use at your own risk!
  */
 
 import java.io.*;
 import java.util.*;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 import org.webmacro.resource.*;
 import org.webmacro.util.Settings;
 import org.webmacro.servlet.WebContext;
 
 public class BeanDirective extends Directive {
 
     private static final boolean DEBUG = true;
     private static final int BEAN_TARGET = 1;
     private static final int BEAN_CLASS_NAME = 2;
     private static final int BEAN_SCOPE = 3;
     private static final int BEAN_SCOPE_GLOBAL = 4;
     private static final int BEAN_SCOPE_APPLICATION = 5;
     private static final int BEAN_SCOPE_SESSION = 6;
     private static final int BEAN_SCOPE_PAGE = 7;
     private static final int BEAN_INITARGS = 8;
     private static final int BEAN_INITARGS_VAL = 9;
     private static final int BEAN_TYPE_STATIC = 10;
     private static final int BEAN_ON_NEW = 11;
     private static final int BEAN_ON_NEW_BLOCK = 12;
     
    private static final UndefinedMacro UNDEF = UndefinedMacro.getInstance();
    
     private static int[] BEAN_SCOPES = {
         BEAN_SCOPE_GLOBAL, BEAN_SCOPE_APPLICATION,
         BEAN_SCOPE_SESSION, BEAN_SCOPE_PAGE
     };
     
     private Variable target;
     private String   targetName;
     private String   _className;
     private int      scope;
     private boolean  isStaticClass;
     private Object   initArgObj;
     private Object[] initArgs;
     private Block    onNewBlock;
     private Log      _log;
     private List     _impliedPackages;
     private List     _allowedPackages;
     private Class    _class;
     private BeanConf beanConf;
     private Broker   _broker;
     
     private static final ArgDescriptor[]
     myArgs = new ArgDescriptor[] {
         new LValueArg(BEAN_TARGET),
         new AssignmentArg(),
         new QuotedStringArg(BEAN_CLASS_NAME),
        new OptionalGroup(1),
        new KeywordArg(BEAN_SCOPE, "scope"),
         new SingleOptionChoice(5),
         new OptionalGroup(1),
         new KeywordArg(BEAN_SCOPE_GLOBAL, "global"),
         new OptionalGroup(1),
         new KeywordArg(BEAN_SCOPE_APPLICATION, "application"),
         new OptionalGroup(1),
         new KeywordArg(BEAN_SCOPE_SESSION, "session"),
         new OptionalGroup(1),
         new KeywordArg(BEAN_SCOPE_PAGE, "page"),
         new OptionalGroup(1),
         new KeywordArg(BEAN_TYPE_STATIC, "static"),
         new OptionalGroup(2),
         new KeywordArg(BEAN_INITARGS, "initArgs"),
         new RValueArg(BEAN_INITARGS_VAL),
         new OptionalGroup(2),
         new KeywordArg(BEAN_ON_NEW, "onNew"),
         new BlockArg(BEAN_ON_NEW_BLOCK)
     };
     
     private static final DirectiveDescriptor
     myDescr = new DirectiveDescriptor("bean", null, myArgs, null);
     
     public static DirectiveDescriptor getDescriptor() {
         return myDescr;
     }
     
     public BeanDirective() {}
     
     public Object build(DirectiveBuilder builder, BuildContext bc)
     throws BuildException {        
         _broker = bc.getBroker();
         _log = _broker.getLog("directive");
         // BeanConf object is created by the init method when this directive
         // is registered by the DirectiveProvider
         beanConf = (BeanConf)_broker.getBrokerLocal("BeanDirective.Conf");
         if (beanConf == null){
             throw new BuildException(
             "Error building the #bean directive.  The directive has not been properly initialized!");
         }
         try {
             target = (Variable) builder.getArg(BEAN_TARGET, bc);
         }
         catch (ClassCastException e) {
             throw new NotVariableBuildException(myDescr.name, e);
         }
         targetName = target.getName();
         _className = (String)builder.getArg(BEAN_CLASS_NAME, bc);
         _class = classForName(_className);
         
         // check if bean is declared as static
         // this implies global scope and no constructor invocation
         isStaticClass = (builder.getArg(BEAN_TYPE_STATIC) != null);
         if (isStaticClass) scope = BEAN_SCOPE_GLOBAL;
         else {
             scope = getScope(builder, bc);
             // initArgs is only valid for non-static beans
             initArgObj = builder.getArg(BEAN_INITARGS_VAL);
             if (initArgObj instanceof Builder)
                 initArgObj = ((Builder)initArgObj).build(bc);
         }
         onNewBlock = (Block)builder.getArg(BEAN_ON_NEW_BLOCK, bc);                
         
         _log.debug("BeanDirective, target="+target
         +", className="+_className+", scope="+scope
         +", isStaticClass="+isStaticClass+", initArgs="+initArgObj);
         return this;
     }
     
     public void write(FastWriter out, Context context)
     throws PropertyException, IOException {
         Map globalBeans = BeanConf.globalBeans;
         Map appBeans = beanConf.appBeans;
         boolean isNew = false;
         
         try {
            while (initArgObj instanceof Macro && initArgObj != UNDEF) 
                 initArgObj = ((Macro) initArgObj).evaluate(context);
             
             // store init args in array
             if (initArgObj == null || initArgObj.getClass().isArray()){
                 initArgs = (Object[])initArgObj;
             } else {
                 initArgs = new Object[1];
                 initArgs[0] = initArgObj;
             }
             
             Object o = null;
             Class c = null;
             switch (scope){
                 case BEAN_SCOPE_GLOBAL:
                     synchronized (globalBeans){
                         o = globalBeans.get(targetName);
                         if (o == null){
                             if (isStaticClass){
                                 c = context.getBroker().classForName(_className);
                                 o = new org.webmacro.engine.StaticClassWrapper(c);
                             } else {
                                 //c = Class.forName(_className);
                                 //o = c.newInstance();
                                 o = instantiate(_className, initArgs);
                             }
                             isNew = true;
                             globalBeans.put(targetName,o);
                         }
                     }
                     break;
                     
                 case BEAN_SCOPE_APPLICATION:
                     synchronized (appBeans){
                         o = appBeans.get(targetName);
                         if (o == null){
                             o = instantiate(_className, initArgs);
                             isNew = true;
                             appBeans.put(targetName, o);
                         }
                     }
                     break;
                     
                 case BEAN_SCOPE_SESSION:
                     javax.servlet.http.HttpSession session = 
                         (javax.servlet.http.HttpSession)context.getProperty("Session");
                     //if (context instanceof WebContext){
                     if (session != null){
                         synchronized(session){
                             o = session.getAttribute(targetName);
                             if (o == null){
                                 o = instantiate(_className, initArgs);
                                 isNew = true;
                                 session.setAttribute(targetName, o);
                             }
                         }
                     } else {
                         PropertyException e = new PropertyException(
                             "#bean usage error: session scope is only valid with servlets!");
                         _broker.getEvaluationExceptionHandler().evaluate(
                             target, context, e);
                     }
                     break;
                 default:    
                 // make "page" the default scope
                 //case BEAN_SCOPE_PAGE:
                     // NOTE: page beans always overwrite anything in the context 
                     // with the same name
                     o = instantiate(_className, initArgs);
                     isNew = true;
                     if (o != null){
                         Class[] paramTypes = { Context.class };
                         try {
                             java.lang.reflect.Method m = o.getClass().getMethod("init", paramTypes);
                             if (m != null){
                                 Object[] args = { context };
                                 m.invoke(o, args);
                             }
                         }
                         catch (Exception e){ // ignore
                         }
                     }
                     break;
             }
             
             _log.debug("BeanDirective: Class " + _className + " loaded.");
             target.setValue(context, o);
         }
         catch (PropertyException e) {
             this._broker.getEvaluationExceptionHandler().evaluate(target, context, e);
         }
         catch (Exception e) {
             String errorText = "BeanDirective: Unable to load bean " + target + " of type " + _className;
             _log.error(errorText, e);
             writeWarning(errorText, context, out);
         }
         if (isNew && onNewBlock != null)
           onNewBlock.write(out, context);
             
     }
     
     public void accept(TemplateVisitor v) {
         v.beginDirective(myDescr.name);
         v.visitDirectiveArg("BeanTarget", target);
         v.visitDirectiveArg("BeanClass", _className);
         v.visitDirectiveArg("BeanScope", new Integer(scope));
         v.visitDirectiveArg("BeanIsStatic", new Boolean(isStaticClass));
         v.visitDirectiveArg("BeanInitArgs", initArgs);
         v.endDirective();
     }
     
     private Object instantiate(String className, Object[] args)
     throws Exception {
         Class c = classForName(className);
         return instantiate(c, args);
     }
     
     private Object instantiate(Class c, Object[] args)
     throws Exception {
         Object o = null;
         if (args == null){
             o = c.newInstance();
         }
         else {
             Exception lastException = null;
             java.lang.reflect.Constructor[] cons = c.getConstructors();
             for (int i=0; i<cons.length; i++){
                 if (cons[i].getParameterTypes().length == args.length){
                     // try to instantiate using this constructor
                     try {
                         o = cons[i].newInstance(args);
                         break; // if successful, we're done!
                     } catch (Exception e){
                         lastException = e;
                     }
                 }
             }
             if (o == null){
                 throw new InstantiationException(
                     "Unable to construct object of type " + c.getName()
                     + " using the supplied arguments: "
                     + java.util.Arrays.asList(args).toString());
             }
         }
         return o;
     }
        
     private static int getScope(DirectiveBuilder builder, BuildContext bc) throws org.webmacro.engine.BuildException {
         int scope = -1;
         
         for (int i=0; i<BEAN_SCOPES.length; i++){
             scope = BEAN_SCOPES[i];
             if (builder.getArg(scope) != null) break;
         }
         return scope;
     }
     
     public static void init(Broker b){
         // get configuration parameters
         synchronized (b){
             BeanConf bCfg = new BeanConf(b);
             b.setBrokerLocal("BeanDirective.Conf", bCfg);
             Log log = b.getLog("directive");
             log.debug("BeanDirective initialization - impliedPackages: " + bCfg.impliedPackages);
             log.debug("BeanDirective initialization - allowedPackages: " + bCfg.allowedPackages);
         }
     }        
     
     private Class classForName(String className) throws BuildException {
         Class c = null;
         Exception except = null;
         try {
             c = _broker.classForName(className);
         } 
         catch (Exception cnfe){
             except = cnfe;
             // try with implied packages prepended
             for (int i=0; i<beanConf.impliedPackages.size(); i++){
                 String s = (String)beanConf.impliedPackages.get(i);
                 try {
                     c = _broker.classForName(s + "." + className);
                     break;
                 } catch (Exception cnfe2){
                     except = cnfe2;
                 }
             }
             if (c == null){
                 throw new BuildException("Unable to load class " + className, except);
             }
         }
         
         if (!beanConf.allowedPackages.isEmpty()){
             // check if class is in a permitted package
             String pkg = c.getPackage().getName();
             if (!beanConf.allowedPackages.contains(pkg)){
                 throw new BuildException(
                     "You are not permitted to load classes from this package (" + pkg 
                     + ").  Check the \"BeanDirective.AllowedPackages\" parameter in the WebMacro.properties file.");
             }
         }
         return c;
     }
 }
 
 class BeanConf {
     static Map globalBeans = new HashMap(20);
     Map appBeans = new HashMap(20);
     List impliedPackages;
     List allowedPackages;
 
     public BeanConf(Broker b){
         String s = b.getSetting("BeanDirective.ImpliedPackages");
         impliedPackages = Arrays.asList(org.webmacro.servlet.TextTool.split(s, ","));
         s = b.getSetting("BeanDirective.AllowedPackages");
         allowedPackages = Arrays.asList(org.webmacro.servlet.TextTool.split(s, ","));
     }
     public Map getGlobalBeans(){ return globalBeans; }
     public Map getAppBeans(){ return appBeans; }
     public List getImpliedPackages(){ return impliedPackages; }
     public List getAllowedPackages(){ return allowedPackages; }
     
 }
