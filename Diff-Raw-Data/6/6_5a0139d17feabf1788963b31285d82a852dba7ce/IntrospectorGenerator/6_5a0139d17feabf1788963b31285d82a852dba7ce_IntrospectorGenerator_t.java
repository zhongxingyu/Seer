 /*
  * IntrospectorGenerator.java
  *
  * Created on July 15, 2007, 2:21 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.totsp.gwittir.rebind.beans;
 
 import com.google.gwt.core.ext.Generator;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JParameterizedType;
 import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
 import com.google.gwt.core.ext.typeinfo.JType;
 import com.google.gwt.core.ext.typeinfo.JTypeParameter;
 import com.google.gwt.core.ext.typeinfo.NotFoundException;
 import com.google.gwt.core.ext.typeinfo.TypeOracle;
 import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
 import com.google.gwt.user.rebind.SourceWriter;
 
 import com.totsp.gwittir.client.beans.SelfDescribed;
 import com.totsp.gwittir.client.beans.annotations.Introspectable;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 
 /**
  *
  * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
  */
 public class IntrospectorGenerator extends Generator {
     private String implementationName = com.totsp.gwittir.client.beans.Introspector.class.getSimpleName() + "_Impl";
     private String packageName = com.totsp.gwittir.client.beans.Introspector.class.getCanonicalName().substring(
             0,
             com.totsp.gwittir.client.beans.Introspector.class.getCanonicalName().lastIndexOf(".")
         );
     private String methodsImplementationName = "MethodsList";
     private JType objectType = null;
   
 
     /** Creates a new instance of IntrospectorGenerator */
     public IntrospectorGenerator() {
     }
 
     private boolean box(JType type, SourceWriter writer) {
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.INT)) {
             writer.print("new Integer( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.LONG)) {
             writer.print("new Long( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.FLOAT)) {
             writer.print("new Float( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.DOUBLE)) {
             writer.print("new Double( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.CHAR)) {
             writer.print("new Character( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.BYTE)) {
             writer.print("new Byte( ");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.BOOLEAN)) {
             writer.print("new Boolean( ");
 
             return true;
         }
 
         return false;
     }
 
     private int find(MethodWrapper[] search, MethodWrapper match) {
         for (int i = 0; i < search.length; i++) {
             if (search[i].equals(match)) {
                 return i;
             }
         }
 
         //System.out.println("NO MATCH FOR: " + match.toString());
         throw new RuntimeException(match.toString());
     }
 
     private MethodWrapper[] findMethods(TreeLogger logger, List introspectables) {
         HashSet methods = new HashSet();
 
         for (Iterator it = introspectables.iterator(); it.hasNext();) {
             BeanResolver info = (BeanResolver) it.next();
             logger.branch(TreeLogger.DEBUG, "Method Scanning: " + info.getType().getQualifiedSourceName(), null);
 
             try {
                 if (info.getProperties().size() == 0) {
                     continue;
                 }
 
                 Collection<Property> pds = info.getProperties().values();
 
                 for ( Property p : pds) {
                    
                     if (p.getReadMethod() != null) {
                         p.getReadMethod().hashWithType = true;
                         methods.add(p.getReadMethod());
                     }
 
                     if (p.getWriteMethod() != null) {
                         p.getWriteMethod().hashWithType = true;
                         methods.add(p.getWriteMethod());
                     }
                 }
             } catch (Exception e) {
                 logger.log(TreeLogger.ERROR, "Unable to introspect class. Is class a bean?", e);
             }
         }
 
         MethodWrapper[] results = new MethodWrapper[methods.size()];
         Iterator it = methods.iterator();
 
         for (int i = 0; it.hasNext(); i++) {
             results[i] = (MethodWrapper) it.next();
         }
 
         return results;
     }
 
     public String generate(TreeLogger logger, GeneratorContext context, String typeName)
         throws UnableToCompleteException {
         try {
             this.objectType = context.getTypeOracle().getType("java.lang.Object");
         } catch (NotFoundException ex) {
             logger.log(TreeLogger.ERROR, typeName, ex);
 
             return null;
         }
 
         List introspectables = this.getIntrospectableTypes(logger, context.getTypeOracle());
 
         MethodWrapper[] methods = this.findMethods(logger, introspectables);
 
         ClassSourceFileComposerFactory mcf = new ClassSourceFileComposerFactory(
                 this.packageName,
                 this.methodsImplementationName
             );
         mcf.addImport(com.totsp.gwittir.client.beans.Method.class.getCanonicalName());
 
         PrintWriter methodsPrintWriter = context.tryCreate(logger, this.packageName, this.methodsImplementationName);
 
         if (methodsPrintWriter != null) {
             SourceWriter methodsWriter = mcf.createSourceWriter(context, methodsPrintWriter);
             this.writeMethods(logger, methods, methodsWriter);
             methodsWriter.println("}");
             context.commit(logger, methodsPrintWriter);
         }
 
         ClassSourceFileComposerFactory cfcf = new ClassSourceFileComposerFactory(
                 this.packageName, this.implementationName
             );
         cfcf.addImplementedInterface(typeName);
         cfcf.addImport("java.util.HashMap");
         cfcf.addImport(com.totsp.gwittir.client.beans.Method.class.getCanonicalName());
         cfcf.addImport(com.totsp.gwittir.client.beans.Property.class.getCanonicalName());
         cfcf.addImport(com.totsp.gwittir.client.beans.BeanDescriptor.class.getCanonicalName());
 
         PrintWriter printWriter = context.tryCreate(logger, packageName, implementationName);
 
         if (printWriter == null) {
             return packageName + "." + implementationName;
         }
 
         SourceWriter writer = cfcf.createSourceWriter(context, printWriter);
         this.writeIntrospectables(logger, introspectables, methods, writer);
         this.writeResolver(introspectables, writer);
 
         writer.println("public BeanDescriptor getDescriptor( Object object ){ ");
         writer.indent();
         writer.println("if( object instanceof "+SelfDescribed.class.getCanonicalName()+" ) return ((SelfDescribed)object).__descriptor();");
         for (Iterator it = introspectables.iterator(); it.hasNext();) {
             BeanResolver resolver = (BeanResolver) it.next();
             writer.println("if( object instanceof " + resolver.getType().getQualifiedSourceName() + " ) {");
             writer.indent();
 
             String name = resolver.getType().getQualifiedSourceName().replaceAll("\\.", "_");
             logger.log(TreeLogger.DEBUG, "Writing : " + name, null);
             writer.print("return " + name + " == null ? " + name + " = ");
             this.writeBeanDescriptor(logger, resolver, methods, writer);
             writer.print(": " + name + ";");
             writer.outdent();
             writer.println("}");
         }
 
         writer.println(" throw new IllegalArgumentException(\"Unknown type\" + object.getClass() ); ");
         writer.outdent();
         writer.println("}");
         writer.outdent();
         writer.println("}");
 
         context.commit(logger, printWriter);
 
         return packageName + "." + implementationName;
     }
 
     private List<BeanResolver> getIntrospectableTypes(TreeLogger logger, TypeOracle oracle) {
         ArrayList<BeanResolver> results = new ArrayList<BeanResolver>();
         HashSet<BeanResolver> resolvers = new HashSet<BeanResolver>();
 
         try {
             JClassType[] types = oracle.getTypes();
             JClassType introspectable = oracle.getType(
                         com.totsp.gwittir.client.beans.Introspectable.class.getCanonicalName()
                     );
 
             for (JClassType type : types) {
 //                if(type.getQualifiedSourceName().endsWith("gwittir.client.ui.TextBox")){
 //                logger.log(
 //                        TreeLogger.WARN,
 //                        type.getQualifiedSourceName() + " is assignable to " + introspectable + " " +
 //                        type.isAssignableTo(introspectable) + " isInterface = " + type.isInterface() +
 //                        "isIntrospectable = "+isIntrospectable(logger,type),
 //                        null
 //                    );
 //                }
 
 
                 if (
                         (isIntrospectable(logger, type)  ||
                         type.isAssignableTo(introspectable) )
                           && (type.isInterface() == null)) {
 
                     resolvers.add(new BeanResolver(logger, type));
                 }
             }
 
             // Do a crazy assed sort to make sure least
             // assignable types are at the bottom of the list
             results.addAll(resolvers);
             results.addAll( this.getFileDeclaredTypes(logger, oracle) );
             boolean swap = true;
 
             while (swap) {
                 swap = false;
 
                 for (int i = results.size() - 1; i >= 0; i--) {
                     BeanResolver type = (BeanResolver) results.get(i);
 
                     for (int j = i - 1; j >= 0; j--) {
                         BeanResolver check = (BeanResolver) results.get(j);
 
                         if (type.getType().isAssignableTo(check.getType())) {
                             results.set(i, check);
                             results.set(j, type);
 
							type = check;
							
                             swap = true;
                         }
                     }
                 }
             }
         } catch (Exception e) {
             logger.log(TreeLogger.ERROR, "Unable to finad Introspectable types.", e);
         }
 //        for(BeanResolver rs:results){
 //            logger.log(TreeLogger.ERROR, rs.toString());
 //        }
         return results;
     }
 
     private List<BeanResolver> getFileDeclaredTypes(TreeLogger logger, TypeOracle oralce ) throws UnableToCompleteException {
         ArrayList<BeanResolver> results = new ArrayList<BeanResolver>();
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         try{
             Enumeration<URL> introspections = ctxLoader.getResources( "gwittir-introspection.properties");
             while(introspections.hasMoreElements() ){
                 URL propsUrl = introspections.nextElement();
                 logger.log(TreeLogger.Type.INFO, "Loading: "+propsUrl.toString() );
                 Properties props = new Properties();
                 props.load( propsUrl.openStream() );
                 for(Entry entry : props.entrySet()){
                     String className = entry.getKey().toString();
                     String[] includedProps = entry.getValue().toString().split(",");
                     JClassType type = oralce.findType(className);
                     if(type == null ){
                         logger.log(TreeLogger.Type.ERROR, "Unable to find type "+className+" declared in "+propsUrl);
                         throw new UnableToCompleteException();
                     }
                     results.add( new BeanResolver(logger, type, includedProps));
                 }
 
             }
 
         } catch(IOException ioe){
             logger.log(TreeLogger.Type.WARN, "Exception looking for properties files", ioe);
         }
         return results;
     }
 
     private boolean isIntrospectable(TreeLogger logger, JType type){
         if( type == null ) return false;
         JClassType ct = type.isClassOrInterface();
         if(ct != null){
             if( ct.getAnnotation(Introspectable.class) != null ){
                 return true;
             }
             for( JClassType iface : ct.getImplementedInterfaces() ){
                 if( isIntrospectable(logger, iface) ){
 
                     return true;
                 }
             }
             if( isIntrospectable(logger, ct.getSuperclass() ) ){
                 return true;
             }
         }
         return false;
     }
 
     private boolean unbox(JType type, String reference, SourceWriter writer) {
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.INT)) {
             writer.print("((Integer) " + reference + ").intValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.LONG)) {
             writer.print("((Long) " + reference + ").longValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.FLOAT)) {
             writer.print("((Float) " + reference + ").floatValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.DOUBLE)) {
             writer.print("((Double) " + reference + ").doubleValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.CHAR)) {
             writer.print("((Character) " + reference + ").charValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.BYTE)) {
             writer.print("((Byte) " + reference + ").byteValue()");
 
             return true;
         }
 
         if ((type.isPrimitive() != null) && (type.isPrimitive() == JPrimitiveType.BOOLEAN)) {
             writer.print("((Boolean) " + reference + ").booleanValue()");
 
             return true;
         }
 
         writer.print("(" + type.getQualifiedSourceName() + ") " + reference);
 
         return false;
     }
 
     private void writeBeanDescriptor(
         TreeLogger logger, BeanResolver info, MethodWrapper[] methods, SourceWriter writer
     ) {
         writer.println("new BeanDescriptor() { ");
         writer.indent();
         writer.println("private HashMap lookup;");
         writer.println("private Property[] properties;");
         writer.println("public Property[] getProperties(){");
         writer.indent();
 
         {
             writer.println("if( this.properties != null ) ");
             writer.indentln("return this.properties;");
             writer.println("this.properties = new Property[" + (info.getProperties().size()) + "];");
 
             Collection pds = info.getProperties().values();
             String[] propertyNames = new String[pds.size()];
             logger.log(TreeLogger.SPAM, "" + (pds == null), null);
 
             
             if (pds != null) {
                 int i = 0;
 
                 for (Iterator it = pds.iterator(); it.hasNext(); i++) {
                     Property p = (Property) it.next();
                     propertyNames[i] = p.getName();
                     writer.println("{");
                     writer.indent();
 
                     writer.print("Method readMethod = ");
 
                     if (p.getReadMethod() == null) {
                         writer.println("null;");
                     } else {
                         writer.println(
                                 this.packageName + "." + this.methodsImplementationName + ".METHOD_" +
                                 +this.find(methods, p.getReadMethod()) + ";"
                             );
                     }
 
                     writer.print("Method writeMethod = ");
 
                     if (p.getWriteMethod() == null) {
                         writer.println("null;");
                     } else {
                         writer.println(
                                 this.packageName + "." + this.methodsImplementationName + ".METHOD_" +
                                 +this.find(methods, p.getWriteMethod()) + ";"
                             );
                     }
 
                     logger.log(TreeLogger.DEBUG, p.getName() + " " + p.getType().getQualifiedSourceName(), null);
                     JType ptype = this.resolveType(p.getType() );
                     
                     logger.log(TreeLogger.DEBUG, p.getName() + " (Erased) " + ptype.getQualifiedSourceName(), null);
                     writer.println(
                             "this.properties[" + (i) + "] = new Property( \"" + p.getName() + "\", " +
                             ((p.getType() != null) ? ptype.getQualifiedSourceName(): "Object") +
                             ".class,  readMethod, writeMethod );"
                         );
                     writer.outdent();
                     writer.println("}");
                 }
             }
 
             writer.println("return this.properties;");
         }
 
         writer.outdent();
         writer.println("} //end getProperties()");
         writer.println("public Property getProperty( String name ) {");
         writer.indent();
         //TODO Rewrite this to a nested if loop using the propertyNames parameter.
         writer.println("Property p = null;");
         writer.println("if( this.lookup != null ) {");
         writer.indentln("p = (Property) lookup.get(name); ");
         writer.println("} else {");
         writer.indent();
         writer.println("this.lookup = new HashMap();");
         writer.println("Property[] props = this.getProperties(); ");
         writer.println("for( int i=0; i < props.length; i++ ) {");
         writer.indent();
         writer.println("this.lookup.put( props[i].getName(), props[i] );");
         writer.outdent();
         writer.println("}");
         writer.println("p = (Property) this.lookup.get(name);");
         writer.outdent();
         writer.println("}");
         writer.println(
                 "if( p == null ) throw new RuntimeException(\"Couldn't find property \"+name+\" for " +
                 info.getType().getQualifiedSourceName() + "\");"
             );
         writer.println("else return p;");
         writer.outdent();
         writer.println("}");
 
         writer.outdent();
         writer.print("}");
     }
 
     private void writeIntrospectables(
         TreeLogger logger, List introspectables, MethodWrapper[] methods, SourceWriter writer
     ) {
         for (Iterator it = introspectables.iterator(); it.hasNext();) {
             BeanResolver bean = (BeanResolver) it.next();
 
             logger.branch(TreeLogger.DEBUG, "Introspecting: " + bean.getType().getQualifiedSourceName(), null);
 
             try {
                 if (bean.getProperties().size() == 0) {
                     continue;
                 }
 
                 writer.print("private static BeanDescriptor ");
                 writer.print(bean.getType().getQualifiedSourceName().replaceAll("\\.", "_"));
 
                 writer.println(" = null;");
             } catch (Exception e) {
                 logger.log(TreeLogger.ERROR, "Unable to introspect class. Is class a bean?", e);
             }
         }
     }
 
     private void writeMethod(TreeLogger logger, MethodWrapper method, SourceWriter writer) {
         JType ptype =  this.resolveType(method.getDeclaringType());
         
         writer.println("new Method(){ ");
         writer.indent();
         writer.println("public String getName() {");
         writer.indentln("return \"" + method.getBaseMethod().getName() + "\";");
         writer.println(" }");
         writer.println("public Object invoke( Object target, Object[] args ) throws Exception {");
         writer.indent();
         writer.println(ptype.getQualifiedSourceName() + " casted =");
         writer.println("(" + ptype.getQualifiedSourceName() + ") target;");
         logger.log(
                 TreeLogger.SPAM,
                 "Method: " + method.getBaseMethod().getName() + " " +
                 method.getBaseMethod().getReturnType().getQualifiedSourceName(),
                 null
             );
 
         if (!(method.getBaseMethod().getReturnType().isPrimitive() == JPrimitiveType.VOID)) {
             writer.print("return ");
         }
 
         JType type = this.resolveType(method.getBaseMethod().getReturnType());
 
         boolean boxed = this.box(type, writer);
         writer.print("casted." + method.getBaseMethod().getName() + "(");
 
         if (method.getBaseMethod().getParameters() != null) {
             for (int j = 0; j < method.getBaseMethod().getParameters().length; j++) {
                 JType arg = this.resolveType(method.getBaseMethod().getParameters()[j].getType());
                 
                 this.unbox(arg, "args[" + j + "]", writer);
 
                 if (j != (method.getBaseMethod().getParameters().length - 1)) {
                     writer.print(", ");
                 }
             }
         }
 
         writer.print(")");
 
         if (boxed) {
             writer.print(")");
         }
 
         writer.println(";");
 
         if (method.getBaseMethod().getReturnType().getQualifiedSourceName().equals("void")) {
             writer.println("return null;");
         }
 
         writer.outdent();
         writer.println("}");
         writer.outdent();
         writer.println("};");
     }
 
     private void writeMethods(TreeLogger logger, MethodWrapper[] methods, SourceWriter writer) {
         for (int i = 0; i < methods.length; i++) {
             writer.print("public static final Method METHOD_" + i + " = ");
             writeMethod(logger, methods[i], writer);
         }
     }
 
     private void writeResolver(List introspectables, SourceWriter writer) {
         writer.println("public Class resolveClass(Object object){");
         writer.indent();
 
         for (Iterator it = introspectables.iterator(); it.hasNext();) {
             BeanResolver type = (BeanResolver) it.next();
             writer.println(
                     "if( object instanceof " + type.getType().getQualifiedSourceName() + " ) return " +
                     type.getType().getQualifiedSourceName() + ".class;"
                 );
         }
 
         writer.println("throw new RuntimeException( \"Object \"+object+\"could not be resolved.\" );");
         writer.outdent();
         writer.println("}");
     }
     
     private JType resolveType(final JType type){
         JType ret = type;
         JParameterizedType pt = type.isParameterized();
         if( pt != null ){
             ret = pt.getRawType();
         }
         JTypeParameter tp = ret.isTypeParameter();
         if( tp != null ){
             ret =tp.getBaseType();
         }
         return ret;
     }
 }
