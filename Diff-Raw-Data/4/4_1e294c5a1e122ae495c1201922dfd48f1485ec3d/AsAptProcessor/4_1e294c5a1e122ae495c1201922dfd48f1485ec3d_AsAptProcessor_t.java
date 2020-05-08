 /*
 Copyright (c) 2004, Bruce Chapman
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this
     list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation and/or
     other materials provided with the distribution.
     * Neither the name of the Rapt Library nor the names of its contributors may be
     used to endorse or promote products derived from this software without specific
     prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.
 */
 
 package net.java.dev.rapt.sam;
 
 import com.sun.mirror.apt.*;
 import com.sun.mirror.declaration.*;
 import com.sun.mirror.type.*;
 import com.sun.mirror.util.*;
 import java.io.*;
 import java.util.*;
 
 import net.java.dev.rapt.generator.*;
 import net.java.dev.rapt.util.*;
 
 class AsAptProcessor implements AnnotationProcessor {
 
     AnnotationProcessorEnvironment env;
 
     AsAptProcessor(AnnotationProcessorEnvironment env) {
         this.env = env;
     }
     
     public void process() {
         Map3List<PackageDeclaration, //target's package
             TypeMirror, // @As.value()  ClassType or InterfaceType
             ClassDeclaration, // target's class
             MethodDeclaration> // targets
             taggedMethods = new Map3List<PackageDeclaration,TypeMirror,ClassDeclaration, MethodDeclaration>(10);
         final AnnotationTypeDeclaration asDecl =
             (AnnotationTypeDeclaration)env.getTypeDeclaration(As.class.getName());
         for(Declaration d : env.getDeclarationsAnnotatedWith(asDecl)) {
             MethodDeclaration md = (MethodDeclaration)d;
             // get the Declaration (of class) corresponding to the value of the annotation
             As ann = md.getAnnotation(As.class);
             TypeMirror asType=null;
             try {
                 ann.value(); // always fails - nominally returns Class<?>
             } catch (MirroredTypeException mte) {
                 asType = mte.getTypeMirror();
             }
             ClassDeclaration owner = (ClassDeclaration)md.getDeclaringType();
             PackageDeclaration pkg = owner.getPackage();
             taggedMethods.put(pkg,asType,owner,md);
 
         }
         for(PackageDeclaration pkg : taggedMethods.keys()) {
             for(TypeMirror asType : taggedMethods.keys(pkg)) {
                 try {
                     // open source file for this AsType in package
                     TypeDeclaration asWhat = asType instanceof ClassType
                         ? ((ClassType)asType).getDeclaration()
                         : ((InterfaceType)asType).getDeclaration() ;
                     String fname = pkg.getQualifiedName();
                     if(fname.length() == 0) {
                         fname = asWhat.getSimpleName();
                     } else {
                         fname = fname + "." + asWhat.getSimpleName();
                     }
                     fname += "s";
                     PrintWriter src = env.getFiler().createSourceFile(fname);
                     // write package declaration
                     if(! pkg.getQualifiedName().equals("")) {
                         src.format("package %s;%n", pkg.getQualifiedName());
                     }
                     // write importsAndHeader
                     src.format(importsAndHeader, asWhat.getSimpleName());
                     for(ClassDeclaration owner : taggedMethods.keys(pkg,asType)) {
                        //String finalOwner = owner.toString()+" owner";
                         for(NameSpace ns : matchUpMethods(asWhat, taggedMethods.values(pkg,asType,owner))) {
                            String finalOwner = owner.toString()+" owner";
                             StringBuilder methodSrc = new StringBuilder();
                             for(MethodPair pair : ns.methods) {
                                 // generate the method eg
                                 //     public void run() { owner.doSomething(); }
                                 methodSrc.append("            public ");
                                 methodSrc.append(pair.impl.getReturnType()).append(" ");
                                 methodSrc.append(pair.impl.getSimpleName()).append("(");
                                 boolean first = true;
                                 for(ParameterDeclaration pd : pair.impl.getParameters()) {
                                     if(! first) {
                                         methodSrc.append(", ");
                                         first = false;
                                     }
                                     methodSrc.append(pd.getType()).append(" ").append(pd.getSimpleName());
                                 }
                                 methodSrc.append(") {\n                ");
                                 if(! pair.impl.getReturnType().equals(env.getTypeUtils().getVoidType())) {
                                     methodSrc.append("return ");
                                 }
                                 methodSrc.append("owner.");
                                 methodSrc.append(pair.target.getSimpleName()).append('(');
                                 first=true;
                                 boolean anyAdditional= false;
                                 for(ParameterDeclaration pd : pair.target.getParameters()) {
                                     if(! first) {
                                         methodSrc.append(", ");
                                         first = false;
                                     }
                                     if (pd.getAnnotation(As.Additional.class) != null) {
                                          anyAdditional=true;
                                          finalOwner = finalOwner+", final " + pd.getType()+ " "+pd.getSimpleName();
                                          methodSrc.append(pd.getSimpleName());
                                     }
 
                                 }
                                 if (pair.impl.getParameters().size() != 0 && anyAdditional) methodSrc.append(",");
                                 
                                 for(ParameterDeclaration pd : pair.impl.getParameters()) {
                                     if(! first) {
                                         methodSrc.append(", ");
                                         first = false;
                                     }
                                     methodSrc.append(pd.getSimpleName());
                                 }
                                 
                                 methodSrc.append(");\n            }\n");
                             }
                             src.format(methodTemplate,asWhat, ns.name, finalOwner, "", methodSrc);
                         }
                     }
                     src.println("}");
                     src.close();
                 } catch (Exception ioe) {
                     env.getMessager().printError("internal");
                     ioe.printStackTrace();
                 }
             }
         }
     }
     
     // %%1 is asType
     /**
 class %1$ss {
     */
     @LongString
     private static String importsAndHeader = LongStrings.importsAndHeader();
 
         // %%1 is asType, %%2 is methodname %%3 is owner class
         // %%4 is LocalArgumentList, %%5 is method Chainer calls
     /**
     static final %1$s %2$s(final %3$s) {
         return new %1$s() {
 %5$s
         };
     }
     */
     @LongString
     private static String methodTemplate = LongStrings.methodTemplate();
 
     // %%1 is target method name, %%2 is arg list, %%3 is return type ( empty for void)
     /**
 %3$s owner.%1$s(%2$s);
     */
     @LongString static String methodChainer = LongStrings.methodChainer();
 
     /** a namespace is used to group targetted methods into a single anonymous
     object. Each targetted method should have a corresponding method which it implements
     , this is determined by matching args and return type, and where those are not unique
     by matching the string left when the namespace is removed from the target method name, with part
     of the As method names.
     */
     static private class NameSpace {
         NameSpace(String name) {
             this.name = name;
         }
         String name;
         List<MethodPair> methods = new ArrayList<MethodPair>();
     }
     static private class MethodPair {
         MethodPair(MethodDeclaration target) {
             this.target = target;
         }
         MethodDeclaration target;
         MethodDeclaration impl;
     }
 
     List<NameSpace> matchUpMethods(TypeDeclaration asWhat, Iterable<MethodDeclaration> targets) {
         Types typeUtils = env.getTypeUtils();
         Collection<? extends MethodDeclaration> implMethods = asWhat.getMethods();
         Map<String,NameSpace> namespaces = new HashMap<String,NameSpace>(10);
         // sort targets into name spaces
         for(MethodDeclaration target : targets) {
             // what is the namespace - initially the method name as tho @As(X.class, ns="*"  /* default */)
             As ann = target.getAnnotation(As.class);
             String nsname=ann.ns();
             if(nsname.equals("*")) nsname=target.getSimpleName();
             if(! namespaces.containsKey(nsname)) namespaces.put(nsname,new NameSpace(nsname));
             namespaces.get(nsname).methods.add(new MethodPair(target));
         }
         List<NameSpace> result = new ArrayList<NameSpace>();
         for(NameSpace ns : namespaces.values()) {
             // for each target method, find the implMethod
             boolean faulty = false;
             for(MethodPair pair : ns.methods) { // A sse below
                 MethodDeclaration tm = pair.target;
                 int matchCounter = 0;
                 MethodDeclaration match = null;
                 String matchNames = "";
                 try {
 implMethods:
                 for(MethodDeclaration im : implMethods) { // SHOULD invert with A so we can check if
                                                           // any abstract im's are not matched.
                     String remainingName = tm.getSimpleName().replace(ns.name,"");
                     if(im.getSimpleName().indexOf(remainingName) == -1) continue;
                     if(! tm.getReturnType().equals(im.getReturnType())) continue;
                     Collection<ParameterDeclaration> targetParams = tm.getParameters();
                     Collection<ParameterDeclaration> implParams = im.getParameters();
                     //count additional params
                     int numberOfAdditionalParams=0;
                     for  (ParameterDeclaration param: targetParams) {
                            if (param.getAnnotation(As.Additional.class) != null) numberOfAdditionalParams++;
                     }
                     //compare number of params
                     if( (targetParams.size() - numberOfAdditionalParams) != implParams.size() ) continue;
                     Iterator<ParameterDeclaration> ips = implParams.iterator();
                     for(ParameterDeclaration tpd : targetParams) {
                         if (tpd.getAnnotation(As.Additional.class) == null) {
                             if( ! ips.next().getType().equals(tpd.getType()) ) continue implMethods;    
                         }
                     }
                     // found 1 match
                     matchCounter++;
                     match = im;
                     matchNames += ", " + im.getSimpleName();
                 }
                 } catch (Exception ex) {
                     env.getMessager().printError(tm.getPosition(), ex.toString());
                 }
                 // did we just find one?
                 if(matchCounter == 1) {
                     pair.impl=match;
                 } else if(matchCounter == 0) {
                     env.getMessager().printError(tm.getPosition(), " cannot find matching method in " + asWhat);
                 } else {
                     env.getMessager().printError(tm.getPosition(),
                         "matches several methods (" + matchNames.substring(2) + ") in " + asWhat);
                 }
                 faulty |= matchCounter != 1;
             }
             if(! faulty) result.add(ns);
         }
         return result;
     }
 
     
 }
             
             
                
 
