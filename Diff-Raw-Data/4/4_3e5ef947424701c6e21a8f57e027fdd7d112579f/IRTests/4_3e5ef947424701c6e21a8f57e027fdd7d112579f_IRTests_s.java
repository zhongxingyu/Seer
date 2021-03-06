 /******************************************************************************
  * Copyright (c) 2002 - 2006 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *****************************************************************************/
 /*
  * Created on Oct 3, 2005
  */
 package com.ibm.wala.cast.java.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.JarFile;
 
 import junit.framework.Assert;
 
 import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IClassLoader;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.classLoader.JarFileModule;
 import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
 import com.ibm.wala.classLoader.SourceFileModule;
 import com.ibm.wala.core.tests.util.WalaTestCase;
 import com.ibm.wala.eclipse.util.EclipseProjectPath;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.CallGraph;
 import com.ibm.wala.ipa.cha.IClassHierarchy;
 import com.ibm.wala.properties.WalaProperties;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.types.MethodReference;
 import com.ibm.wala.types.TypeName;
 import com.ibm.wala.types.TypeReference;
 import com.ibm.wala.util.*;
 import com.ibm.wala.util.collections.*;
 import com.ibm.wala.util.debug.Assertions;
 import com.ibm.wala.util.debug.Trace;
 import com.ibm.wala.util.warnings.WalaException;
 
 public abstract class IRTests extends WalaTestCase {
   public IRTests(String name) {
     super(name);
   }
 
   protected static String javaHomePath;
 
   protected static String testSrcPath = "." + File.separator + "testSrc";
 
   protected static List<String> rtJar;
 
   static List<IRAssertion> emptyList = Collections.emptyList();
 
   static {
     boolean found = false;
     try {
       Properties p = WalaProperties.loadProperties();
       javaHomePath = p.getProperty(WalaProperties.J2SE_DIR);
 
       if (new File(javaHomePath).isDirectory()) {
         if("Mac OS X".equals(System.getProperty("os.name"))) { //nick
 	  /**
 	   * todo: {@link WalaProperties#getJ2SEJarFiles()}
 	   */
 	  rtJar.add(javaHomePath + "/Classes/classes.jar");
 	  rtJar.add(javaHomePath + "/Classes/ui.jar");
 	} else {
 	  rtJar.add(javaHomePath + File.separator + "classes.jar");
 	  rtJar.add(javaHomePath + File.separator + "rt.jar");
 	  rtJar.add(javaHomePath + File.separator + "core.jar");
 	  rtJar.add(javaHomePath + File.separator + "vm.jar");
 	}
 	found = true;
       }
     } catch (WalaException e) {
       // no properties
     }
 
     if (! found) {
       javaHomePath = System.getProperty("java.home");
      rtJar = new LinkedList<String>();
       if("Mac OS X".equals(System.getProperty("os.name"))) { //nick
 	rtJar.add(javaHomePath + "/../Classes/classes.jar");
 	rtJar.add(javaHomePath + "/../Classes/ui.jar");
       } else {
 	rtJar.add(javaHomePath + File.separator + "lib" + File.separator + "rt.jar");
 	rtJar.add(javaHomePath + File.separator + "lib" + File.separator + "core.jar");
 	rtJar.add(javaHomePath + File.separator + "lib" + File.separator + "vm.jar");
 	rtJar.add(javaHomePath + File.separator + "lib" + File.separator + "classes.jar");
       }
     }
   }
 
   interface IRAssertion {
 
     void check(CallGraph cg) throws Exception;
 
   }
 
   protected static class EdgeAssertions implements IRAssertion {
     public final String srcDescriptor;
 
     public final List/* <String> */<String> tgtDescriptors = new ArrayList<String>();
 
     public EdgeAssertions(String srcDescriptor) {
       this.srcDescriptor = srcDescriptor;
     }
 
     public static EdgeAssertions make(String srcDescriptor, String tgtDescriptor) {
       EdgeAssertions ea = new EdgeAssertions(srcDescriptor);
       ea.tgtDescriptors.add(tgtDescriptor);
       return ea;
     }
 
     public static EdgeAssertions make(String srcDescriptor, String tgtDescriptor1, String tgtDescriptor2) {
       EdgeAssertions ea = new EdgeAssertions(srcDescriptor);
       ea.tgtDescriptors.add(tgtDescriptor1);
       ea.tgtDescriptors.add(tgtDescriptor2);
       return ea;
     }
 
     public static EdgeAssertions make(String srcDescriptor, String tgtDescriptor1, String tgtDescriptor2, String tgtDescriptor3) {
       EdgeAssertions ea = new EdgeAssertions(srcDescriptor);
       ea.tgtDescriptors.add(tgtDescriptor1);
       ea.tgtDescriptors.add(tgtDescriptor2);
       ea.tgtDescriptors.add(tgtDescriptor3);
       return ea;
     }
 
     public static EdgeAssertions make(String srcDescriptor, String tgtDescriptor1, String tgtDescriptor2, String tgtDescriptor3,
         String tgtDescriptor4) {
       EdgeAssertions ea = new EdgeAssertions(srcDescriptor);
       ea.tgtDescriptors.add(tgtDescriptor1);
       ea.tgtDescriptors.add(tgtDescriptor2);
       ea.tgtDescriptors.add(tgtDescriptor3);
       ea.tgtDescriptors.add(tgtDescriptor4);
       return ea;
     }
 
     public void check(CallGraph callGraph) {
       MethodReference srcMethod = descriptorToMethodRef(this.srcDescriptor, callGraph.getClassHierarchy());
       Set<CGNode> srcNodes = callGraph.getNodes(srcMethod);
 
       if (srcNodes.size() == 0) {
         Trace.println("Unreachable/non-existent method: " + srcMethod);
         return;
       }
       if (srcNodes.size() > 1) {
         Trace.println("Context-sensitive call graph?");
       }
 
       // Assume only one node for src method
       CGNode srcNode = srcNodes.iterator().next();
 
       for (String target : this.tgtDescriptors) {
         MethodReference tgtMethod = descriptorToMethodRef(target, callGraph.getClassHierarchy());
         // Assume only one node for target method
         Set<CGNode> tgtNodes = callGraph.getNodes(tgtMethod);
         if (tgtNodes.size() == 0) {
           Trace.println("Unreachable/non-existent method: " + tgtMethod);
           continue;
         }
         CGNode tgtNode = tgtNodes.iterator().next();
 
         boolean found = false;
         for (Iterator<? extends CGNode> succIter = callGraph.getSuccNodes(srcNode); succIter.hasNext();) {
           CGNode succ = succIter.next();
 
           if (tgtNode == succ) {
             found = true;
             break;
           }
         }
         if (!found) {
           Trace.println("Missing edge: " + srcMethod + " -> " + tgtMethod);
         }
       }
     }
   }
 
   protected static class SourceMapAssertion implements IRAssertion {
     private final String method;
     private final String variableName;
 
     private final int definingLineNumber;
 
     protected SourceMapAssertion(String method, String variableName, int definingLineNumber) {
       this.method = method;
       this.variableName = variableName;
       this.definingLineNumber = definingLineNumber;
     }
 
     public void check(CallGraph cg) {
 
         MethodReference mref = descriptorToMethodRef(method, cg.getClassHierarchy());
 
         for (CGNode cgNode : cg.getNodes(mref)) {
             Assert.assertTrue("failed for " + this.variableName + " in " + cgNode,
               this.check(cgNode.getMethod(), cgNode.getIR()));
         }
     }
 
     boolean check(IMethod m, IR ir) {
       Trace.println("check for " + variableName + " defined at " + definingLineNumber);
       SSAInstruction[] insts = ir.getInstructions();
       for (int i = 0; i < insts.length; i++) {
         if (insts[i] != null) {
           int ln = -1;
           try {
             ln = m.getLineNumber(i);
           } catch (InvalidClassFileException e) {
             e.printStackTrace();
             Assertions.UNREACHABLE();
           }
           if (ln == definingLineNumber) {
             Trace.println("  found " + insts[i] + " at " + ln);
             for (int j = 0; j < insts[i].getNumberOfDefs(); j++) {
               int def = insts[i].getDef(j);
               Trace.println("    looking at def " + j + ": " + def);
               String[] names = ir.getLocalNames(i, def);
               if (names != null) {
                 for (String name : names) {
                   Trace.println("      looking at name " + name);
                   if (name.equals(variableName)) {
                     return true;
                   }
                 }
               }
             }
           }
         }
       }
 
       return false;
     }
   }
 
   protected abstract String singleInputForTest();
 
   protected abstract String singlePkgInputForTest(String pkgName);
 
   protected Collection<String> singleTestSrc() {
     return Collections.singletonList(testSrcPath + File.separator + singleInputForTest());
   }
 
   protected Collection<String> singlePkgTestSrc(String pkgName) {
     return Collections.singletonList(testSrcPath + File.separator + singlePkgInputForTest(pkgName));
   }
 
   protected String[] simpleTestEntryPoint() {
     return new String[] { "L" + getName().substring(4) };
   }
 
   protected String[] simplePkgTestEntryPoint(String pkgName) {
     return new String[] { "L" + pkgName + "/" + getName().substring(4) };
   }
 
   protected abstract JavaSourceAnalysisEngine getAnalysisEngine(String[] mainClassDescriptors);
 
   public Pair runTest(Collection<String> sources, 
 		      List<String> libs,
 		      String[] mainClassDescriptors, 
 		      List<? extends IRAssertion> ca, 
 		      boolean assertReachable) 
   {
     try {
       JavaSourceAnalysisEngine engine = getAnalysisEngine(mainClassDescriptors);
 
       populateScope(engine, sources, libs);
 
       CallGraph callGraph = engine.buildDefaultCallGraph();
       Trace.println(callGraph.toString());
 
       // If we've gotten this far, IR has been produced.
       dumpIR(callGraph, assertReachable);
 
       // Now check any assertions as to source mapping
       for (IRAssertion IRAssertion : ca) {
         IRAssertion.check(callGraph);
       }
 
       return Pair.make(callGraph, engine.getPointerAnalysis());
 
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     }
   }
 
   private static void dumpIR(CallGraph cg, boolean assertReachable) throws IOException {
     Set<IMethod> unreachable = HashSetFactory.make();
     IClassHierarchy cha = cg.getClassHierarchy();
     IClassLoader sourceLoader = cha.getLoader(EclipseProjectPath.SOURCE_REF);
     for (Iterator iter = sourceLoader.iterateAllClasses(); iter.hasNext();) {
       IClass clazz = (IClass) iter.next();
 
       Trace.println(clazz);
       if (clazz.isInterface())
         continue;
 
       for (IMethod m : clazz.getDeclaredMethods()) {
         if (m.isAbstract()) {
           Trace.println(m);
         }
         else {
           Iterator nodeIter = cg.getNodes(m.getReference()).iterator();
           if (!nodeIter.hasNext()) {
             Trace.println("Method " + m.getReference() + " not reachable?");
             unreachable.add(m);
             continue;
           }
           CGNode node = (CGNode)nodeIter.next();
           Trace.println(node.getIR());
         }
       }
     }
 
     if (assertReachable) {
       Assert.assertTrue(unreachable.toString(), unreachable.isEmpty());
     }
   }
 
 
   /**
    *
    * @param srcMethodDescriptor a full method descriptor of the form ldr#type#methName#methSig
    * example: Source#Simple1#main#([Ljava/lang/String;)V
    * @param cha
    * @return
    */
   public static MethodReference descriptorToMethodRef(String srcMethodDescriptor, IClassHierarchy cha) {
     String[] ldrTypeMeth = srcMethodDescriptor.split("\\#");
 
     String loaderName = ldrTypeMeth[0];
     String typeStr = ldrTypeMeth[1];
     String methName = ldrTypeMeth[2];
     String methSig = ldrTypeMeth[3];
 
     TypeReference typeRef = findOrCreateTypeReference(loaderName, typeStr, cha);
 
     return MethodReference.findOrCreate(typeRef, methName, methSig);
   }
 
   static TypeReference findOrCreateTypeReference(String loaderName, String typeStr,
     IClassHierarchy cha) {
     ClassLoaderReference clr = findLoader(loaderName, cha);
     TypeName typeName = TypeName.string2TypeName("L" + typeStr);
     TypeReference typeRef = TypeReference.findOrCreate(clr, typeName);
     return typeRef;
   }
 
   private static ClassLoaderReference findLoader(String loaderName, IClassHierarchy cha) {
     Atom loaderAtom = Atom.findOrCreateUnicodeAtom(loaderName);
     IClassLoader[] loaders = cha.getLoaders();
     for (IClassLoader loader : loaders) {
       if (loader.getName() == loaderAtom) {
         return loader.getReference();
       }
     }
     Assertions.UNREACHABLE();
     return null;
   }
 
   protected static void populateScope(JavaSourceAnalysisEngine engine, Collection<String> sources, List<String> libs)
       throws IOException {
 
     boolean foundLib = false;
     for (String lib : libs) {
       File libFile = new File(lib);
       if (libFile.exists()) {
         foundLib = true;
         engine.addSystemModule(new JarFileModule(new JarFile(libFile)));
       }
     }
     Assertions._assert(foundLib);
 
     for (String srcFilePath : sources) {
       String srcFileName = srcFilePath.substring(srcFilePath.lastIndexOf(File.separator) + 1);
       File f = new File(srcFilePath);
       Assert.assertTrue(f.exists());
       if (f.isDirectory()) {
         engine.addSourceModule(new SourceDirectoryTreeModule(f));
       }
       else {
         engine.addSourceModule(new SourceFileModule(f, srcFileName));
       }
     }
   }
 }
