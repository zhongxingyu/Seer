 /*
  * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
  * or visit www.oracle.com if you need additional information or have any
  * questions.
  */
 
 package com.sun.tools.javah;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.ElementFilter;
 
 import com.sun.tools.javah.staticjni.ArrayCallback;
 import com.sun.tools.javah.staticjni.Callback;
 import com.sun.tools.javah.staticjni.ExceptionCallback;
 import com.sun.tools.javah.staticjni.FieldCallback;
 
 /**
  * Header file generator for JNI.
  *
  * Supports only the creation of the body of the frontier.
  */
 public class StaticJNIFrontierBody extends StaticJNIGen {
     StaticJNIFrontierBody(Util util, StaticJNIClassHelper helper) {
         super(util, helper);
     }
 
     protected String getFileSuffix() {
         return "_frontier.c";
     }
 
     public String getIncludes() {
     	return "";
     }
     public String getIncludes( CharSequence className ) {
         return "#include \"" + baseFileName(className) + "_frontier.h\"";
     }
 
     public void write(OutputStream o, TypeElement clazz) throws Util.Exit {
     	super.write( o, clazz );
         try {
             String cname = mangler.mangle(clazz.getQualifiedName(), Mangle.Type.CLASS);
             PrintWriter pw = wrapWriter(o);
             
             pw.println(getIncludes(clazz.getQualifiedName()));
             
             pw.println(cppGuardBegin());
             
             pw.println( "#ifndef STATICJNI_THREAD_ENV" );
             pw.println( "JNIEnv *thread_env;" );
             pw.println( "#endif" );
             
             /* List of callbacks to Java */
           //  Set<ExecutableElement> callbacks = new HashSet<>();
           //  Set<VariableElement> callbacks_to_field = new HashSet<>();
 
             /* Write statics. */
             List<VariableElement> classfields = getAllFields(clazz);
             for (VariableElement v: classfields) {
                 if (!v.getModifiers().contains(Modifier.STATIC))
                     continue;
                 String s = null;
                 s = defineForStatic(clazz, v);
                 if (s != null) {
                     pw.println(s);
                 }
             }
 
             /* Write outgoing methods. */
             List<ExecutableElement> classmethods = ElementFilter.methodsIn(clazz.getEnclosedElements());
             for (ExecutableElement md: classmethods) {
                 if(md.getModifiers().contains(Modifier.NATIVE)){
                     TypeMirror mtr = types.erasure(md.getReturnType());
                     String sig = signature(md);
                     TypeSignature newtypesig = new TypeSignature(elems);
                     CharSequence methodName = md.getSimpleName();
                     boolean longName = false; /* if overloaded */
                     for (ExecutableElement md2: classmethods) {
                         if ((md2 != md)
                             && (methodName.equals(md2.getSimpleName()))
                             && (md2.getModifiers().contains(Modifier.NATIVE)))
                             longName = true;
 
                     }
                     pw.println("/*");
                     pw.println(" * Class:     " + cname);
                     pw.println(" * Method:    "
                             + mangler.mangle(methodName, Mangle.Type.FIELDSTUB));
                     pw.println(" * Signature: "
                             + newtypesig.getTypeSignature(sig, mtr));
                     pw.println(" */");
 
                     List<? extends VariableElement> paramargs = md.getParameters();
                     List<TypeMirror> args = new ArrayList<TypeMirror>();
                     for (VariableElement p: paramargs) {
                         args.add(types.erasure(p.asType()));
                     }
                     
                     // impl declaration
 
                 	pw.print("extern " + staticjniType( mtr ) + " " + getCImplName( md, clazz, longName ) + "( " );
 
                     List<String> sArgs = new ArrayList<String>();
                 	if (!md.getModifiers().contains(Modifier.STATIC))
                 		sArgs.add( staticjniType( clazz.asType() ) );
                 	
                     for (TypeMirror arg: args) sArgs.add( staticjniType(arg) );
                     
                     if ( !sArgs.isEmpty() )
                     	pw.print( sArgs.get(0) );
                     for (int i = 1; i < sArgs.size(); i++)
                     	pw.print( ", " + sArgs.get(i) );
                     
                 	pw.println( " );" );
                     
                     
                     // implementation of outgoing calls (Java -> C)
                     // Normal JNI function impl
                     pw.println("JNIEXPORT "
                             + jniType(mtr)
                             + " JNICALL "
                             + mangler.mangleMethod(md, clazz,
                                     (longName) ? Mangle.Type.METHOD_JNI_LONG
                                             : Mangle.Type.METHOD_JNI_SHORT));
                     pw.print("  (JNIEnv *env, ");
                     if (md.getModifiers().contains(Modifier.STATIC))
                         pw.print("jclass self");
                     else
                         pw.print("jobject self");
 
                     // for (TypeMirror arg: args) {
                     for (int a = 0; a < args.size(); a++)
                         pw.print(", " + jniType(args.get(a)) + " "
                                 + paramargs.get(a).getSimpleName());
 
                     pw.println(") {");
 
                     pw.println("\tthread_env = env;");
 
                     pw.print("\t");
 
                     // implementation of outgoing calls (Java -> C)
                     if (mtr.getKind() != TypeKind.VOID)
                         pw.print(staticjniType(mtr) + " rval = ");
 
                     // prepare/transform arg for C
                     pw.print(getCImplName(md, clazz, longName) + "( ");
 
                     sArgs = new ArrayList<String>();
                     
                     // self type
                     if (!md.getModifiers().contains(Modifier.STATIC))
                     	sArgs.add(castToStaticjni(clazz.asType(), "self"));
 
                     // args
                     for (int a = 0; a < args.size(); a++)
                     	sArgs.add( castToStaticjni(args.get(a), paramargs.get(a)
                                         .getSimpleName().toString()));
 
                     if ( !sArgs.isEmpty() )
                     	pw.print( sArgs.get(0) );
                     for (int i = 1; i < sArgs.size(); i++)
                     	pw.print( ", " + sArgs.get(i) );
                     
                     pw.println(" );");
 
                     if (mtr.getKind() != TypeKind.VOID)
                         pw.println("\treturn " + castFromStaticjni(mtr, "rval")
                                 + ";");
 
                     pw.println("}");
                 }
             }
 
             for ( Callback c : helper.callbacks) {
                 
                 /*** Normal callbacks ***/
                 ExecutableElement m = c.meth;
                 TypeMirror rtm = m.getReturnType();
                 String signature = normalSignature( c );
 
                 List<? extends VariableElement> paramargs = m.getParameters();
                 
                 pw.println(signature + " {");
 
                 /* Implementation of callback */
 
                 if (m.getModifiers().contains(Modifier.STATIC)) {
                     pw.println("\tjclass jclass = (*thread_env)->FindClass( thread_env, \""
                             + c.recvType.getQualifiedName().toString().replace('.', '/') + "\" );");
                     pw.println("\tif ( jclass == 0 ) {");
                     pw.println("\t\tfprintf( stderr, \"Cannot find class for " + c.toString() + "\\n\" );");
                     pw.println("\t}");
                 } else {
                     pw.println("\tjclass jclass = (*thread_env)->GetObjectClass( thread_env, "
                             + castFromStaticjni(clazz.asType(), "self") + " );");
                     pw.println("\tif ( jclass == 0 ) {");
                    pw.println("\t\t(*thread_env)->FatalError( thread_env, \"Cannot find class for " + c.toString() + "\" );");
                     pw.println("\t}");
                 }
 
                 String sig = signature(m);
                 TypeSignature newtypesig = new TypeSignature(elems);
 
                 if (m.getModifiers().contains(Modifier.STATIC)) {
                     pw.println("\tjmethodID jmeth = (*thread_env)->GetStaticMethodID( thread_env, jclass, \""
                             + m.getSimpleName().toString()
                             + "\", \""
                             + newtypesig.getTypeSignature(sig, rtm) + "\" );");
                 } else {
                     pw.println("\tjmethodID jmeth = (*thread_env)->GetMethodID( thread_env, jclass, \""
                             + m.getSimpleName().toString()
                             + "\", \""
                             + newtypesig.getTypeSignature(sig, rtm) + "\" );");
                 }
                 pw.println("\tif ( jmeth == 0 ) {");
                pw.println("\t\t(*thread_env)->FatalError( thread_env, \"Cannot find method: "
                        + m.getSimpleName().toString() + "\" );");
                 pw.println("\t}");
 
                 // actual call
                 pw.print("\t");
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.print(jniType(rtm) + " rval = ");
 
                 pw.print("(*thread_env)->Call"
                         + (m.getModifiers().contains(Modifier.STATIC) ? "Static"
                                 : ""));
                 pw.print(getCallSuffixForReturn(rtm));
                 pw.print("( thread_env, "
                         + (m.getModifiers().contains(Modifier.STATIC)? "jclass": castFromStaticjni(clazz.asType(), "self"))
                         + ", " );
                 pw.print("jmeth");
                 for (VariableElement p : paramargs) {
                     TypeMirror arg = types.erasure(p.asType());
                     pw.print(", "
                             + castFromStaticjni(arg, p.getSimpleName()
                                     .toString()));
                 }
                 pw.println(" );");
 
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.println("\treturn " + castToStaticjni(rtm, "rval") + ";");
 
                 pw.println("}");
             }
             
             for ( FieldCallback c: helper.fieldCallbacks ) { // TODO
                 
                 /*** Setter ***/
                 TypeMirror rtm = c.field.asType();
                 String signature = fieldSetterSignature( c );
                 TypeSignature newtypesig = new TypeSignature(elems);
                 
                 pw.println(signature + " {");
 
                 /* Implementation of callback */
 
                 if (c.field.getModifiers().contains(Modifier.STATIC)) {
                 	String qname = c.recvType.getQualifiedName().toString().replace('.', '/');
                 	if ( c.recvType.getEnclosingElement().getKind() == ElementKind.CLASS ) {
                 		int i = qname.lastIndexOf('/');
                 		qname = qname.substring(0,i) + "$" + qname.substring(i+1);
                 	}
             		System.out.println( qname + " " + c.recvType.getKind() + " " + c.recvType.getEnclosingElement().getKind() );
                     pw.println("\tjclass jclass = (*thread_env)->FindClass( thread_env, \""
                             + qname + "\" );");
                 } else {
                     pw.println("\tjclass jclass = (*thread_env)->GetObjectClass( thread_env, "
                             + castFromStaticjni(clazz.asType(), "self") + " );");
                 }
                 pw.println("\tif ( jclass == 0 ) {");
                 pw.println("\t\t(*thread_env)->FatalError( thread_env, \"Cannot find class for " + c.toString() + "\" );");
                 pw.println("\t}"); 
 
                 if (c.field.getModifiers().contains(Modifier.STATIC)) {
                     pw.println("\tjfieldID jfield = (*thread_env)->GetStaticFieldID( thread_env, jclass, \""
                             + c.field.getSimpleName().toString() + "\", \"" +
                             newtypesig.getTypeSignature(types.erasure( c.field.asType()).toString())
                             + "\" );"); // TODO correct signature!
                 } else {
                     pw.println("\tjfieldID jfield = (*thread_env)->GetFieldID( thread_env, jclass, \""
                             + c.field.getSimpleName().toString() + "\", \"" +
                             newtypesig.getTypeSignature(types.erasure( c.field.asType()).toString())
                             + "\" );"); // TODO correct signature!
                 }
                 pw.println("\tif ( jfield == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find field: "
                         + c.field.getSimpleName().toString() + "\\n\" );");
                 pw.println("\t}");
 
                 // actual call
                 pw.println("\t" + jniType(rtm) + " value = " + castFromStaticjni(rtm, "in_value") + ";");
 
                 pw.print("\t(*thread_env)->Set"
                         + (c.field.getModifiers().contains(Modifier.STATIC) ? "Static"
                                 : "") );
                 pw.print(getCallTypeForReturn(rtm) + "Field");
                 pw.print("( thread_env, " );
                 if (c.field.getModifiers().contains(Modifier.STATIC))
                 	pw.print( "jclass" + ", " );
                 else
                 	pw.print( castFromStaticjni(clazz.asType(), "self") + ", " );
                 pw.print( "jfield, ");
                 pw.print(castFromStaticjni(rtm, "value"));
                 pw.println(" );");
 
                 pw.println("}");
                 
                 
                 /*** Getter ***/
                 signature = fieldGetterSignature( c );
                 
                 pw.println(signature + " {");
 
                 /* Implementation of callback */
 
                 if (c.field.getModifiers().contains(Modifier.STATIC)) {
                 	String qname = c.recvType.getQualifiedName().toString().replace('.', '/');
                 	if ( c.recvType.getEnclosingElement().getKind() == ElementKind.CLASS ) { // TODO try all levels
                 		int i = qname.lastIndexOf('/');
                 		qname = qname.substring(0,i) + "$" + qname.substring(i+1);
                 		System.out.println( "TOP_LEVEL" );
                 	}
             		System.out.println( qname );
                     pw.println("\tjclass jclass = (*thread_env)->FindClass( thread_env, \""
                             + qname + "\" );");
                 } else {
                     pw.println("\tjclass jclass = (*thread_env)->GetObjectClass( thread_env, "
                             + castFromStaticjni(clazz.asType(), "self") + " );");
                 }
                 pw.println("\tif ( jclass == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find class for " + c.toString() + "\\n\" );");
                 pw.println("\t}"); 
 
                 if (c.field.getModifiers().contains(Modifier.STATIC)) {
                     pw.println("\tjfieldID jfield = (*thread_env)->GetStaticFieldID( thread_env, jclass, \""
                             + c.field.getSimpleName().toString() + "\", \"" +
                             newtypesig.getTypeSignature(types.erasure( c.field.asType()).toString())
                             + "\" );"); // TODO correct signature!
                 } else {
                     pw.println("\tjfieldID jfield = (*thread_env)->GetFieldID( thread_env, jclass, \""
                             + c.field.getSimpleName().toString() + "\", \"" +
                             newtypesig.getTypeSignature(types.erasure( c.field.asType()).toString())
                             + "\" );"); // TODO correct signature!
                 }
                 pw.println("\tif ( jfield == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find field: "
                         + c.field.getSimpleName().toString() + "\\n\" );");
                 pw.println("\t}");
 
                 // actual call
                 pw.print("\t" + jniType(rtm) + " rval = ");
 
                 pw.print("\t(*thread_env)->Get"
                         + (c.field.getModifiers().contains(Modifier.STATIC) ? "Static"
                                 : "") );
                 pw.print(getCallTypeForReturn(rtm) + "Field");
                 pw.print("( thread_env, " );
                 if (c.field.getModifiers().contains(Modifier.STATIC))
                 	pw.print( "jclass" + ", " );
                 else
                 	pw.print( castFromStaticjni(clazz.asType(), "self") + ", " );
                 pw.println( "jfield );");
 
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.println("\treturn " + castToStaticjni(rtm, "rval") + ";");
 
                 pw.println("}");
             }
             
             for ( Callback c: helper.superCallbacks ) {
                 
                 /*** Super ***/
                 ExecutableElement m = c.meth;
                 TypeMirror rtm = m.getReturnType();
                 String signature = superSignature( c );
 
                 List<? extends VariableElement> paramargs = m.getParameters();
                 
                 pw.println(signature + " {");
 
                 /* Implementation of callback */
 
                 if (!m.getModifiers().contains(Modifier.STATIC)) {
                     pw.println("\tjclass jclass = (*thread_env)->GetObjectClass( thread_env, "
                             + castFromStaticjni(clazz.asType(), "self") + " );");
                     pw.println("\tif ( jclass == 0 ) {");
                     pw.println("\t\tfprintf( stderr, \"Cannot find class for " + c.toString() + "\\n\" );");
                     pw.println("\t}");
                     
                     // retreive super class
                     // jclass GetSuperclass(JNIEnv *env, jclass clazz);
                     pw.print( "\tjclass = (*thread_env)->GetSuperclass(thread_env, jclass );" );
 
                     String sig = signature(m);
                     TypeSignature newtypesig = new TypeSignature(elems);
 
                     pw.println("\tjmethodID jmeth = (*thread_env)->GetMethodID( thread_env, jclass, \""
                             + m.getSimpleName().toString()
                             + "\", \""
                             + newtypesig.getTypeSignature(sig, rtm) + "\" );");
                     pw.println("\tif ( jmeth == 0 ) {");
                     pw.println("\t\tfprintf( stderr, \"Cannot find method: "
                             + m.getSimpleName().toString() + "\\n\" );");
                     pw.println("\t}");
                 }
 
                 // actual call
                 pw.print("\t");
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.print(jniType(rtm) + " rval = ");
 
                 pw.print("(*thread_env)->CallNonvirtual" // using Non-virtual
                         + (m.getModifiers().contains(Modifier.STATIC) ? "Static"
                                 : ""));
                 pw.print(getCallSuffixForReturn(rtm));
                 pw.print("( thread_env, "
                         + castFromStaticjni(clazz.asType(), "self") + ", ");
                 pw.print("jclass, ");
                 pw.print("jmeth");
                 for (VariableElement p : paramargs) {
                     TypeMirror arg = types.erasure(p.asType());
                     pw.print(", "
                             + castFromStaticjni(arg, p.getSimpleName()
                                     .toString()));
                 }
                 pw.println(" );");
 
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.println("\treturn " + castToStaticjni(rtm, "rval") + ";");
 
                 pw.println("}");
             }
             
             for ( Callback c: helper.constCallbacks ) {
                 
                 /*** Constructor ***/
                 String signature = constructorSignature( c );
                 TypeMirror rtm = c.recvType.asType();
                 
                 pw.println(signature + " {");
 
                 pw.println("\tjclass jclass = (*thread_env)->FindClass( thread_env, \""
                         + c.recvType.getQualifiedName().toString().replace('.', '/') + "\" );");
                 pw.println("\tif ( jclass == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find class for " + c.toString() + "\\n\" );");
                 pw.println("\t}");
 
                 TypeSignature newtypesig = new TypeSignature(elems);
                 String tsig = newtypesig.getTypeSignature(this.signature(c.meth), rtm);
                 
                 // replace return type with V
                 int li = tsig.lastIndexOf(')');
                 tsig = tsig.substring(0,li+1) + "V";
 
                 pw.println("\tjmethodID jmeth = (*thread_env)->GetMethodID( thread_env, jclass, \"<init>\", \"" + tsig + "\" );");
                 pw.println("\tif ( jmeth == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find constructor: "
                         + c.meth.getSimpleName().toString() + "\\n\" );");
                 pw.println("\t}");
 
                 // actual call
                 pw.print("\t" + jniType(rtm) + " rval = ");
 
                 pw.print("\t(*thread_env)->NewObject" );
                 pw.print("( thread_env, jclass, jmeth );" );
 
                 if (rtm.getKind() != TypeKind.VOID)
                     pw.println("\treturn " + castToStaticjni(rtm, "rval") + ";");
 
                 pw.println("}");
             }
             
             for ( ExceptionCallback c: helper.exceptionCallbacks ) {
             	String signature = throwSignature( c );
             	
             	String guard = "STATICJNI_THROW_" + types.asElement(c.exceptionType).getSimpleName();
             	pw.println( "#ifndef " + guard );
             	pw.println( "#define " + guard );
                 pw.println(signature + " {");
                 
                 pw.println("\tjclass jclass = (*thread_env)->FindClass( thread_env, \""
                         + ((TypeElement)types.asElement(c.exceptionType)).getQualifiedName().toString().replace('.', '/') + "\" );");
                 pw.println("\tif ( jclass == 0 ) {");
                 pw.println("\t\tfprintf( stderr, \"Cannot find class for " + c.toString() + "\\n\" );");
                 pw.println("\t}");
 
                 pw.println("\tif ( (*thread_env)->ThrowNew(thread_env, jclass, msg ) ) {");
                 pw.println("\t\tfprintf( stderr, \"Throw failed for " + c.toString() + "\\n\" );");
                 pw.println("\t}");
                 
                 pw.println("\t(*thread_env)->DeleteLocalRef(thread_env, jclass );");
                 
                 pw.println("}");
             	pw.println( "#endif" );
             }
             
             for ( ArrayCallback c: helper.arrayCallbacks ) {
             	String get_sig_local = accessArrayGet(c, clazz);
             	String release_sig_local = accessArrayRelease(c, clazz);
             	String length_sig_local = accessArrayLength(c, clazz);
             	
             	// Get array
                 pw.println();
                 pw.println( staticjniType(c.arrayType.getComponentType()) + " *" + get_sig_local  + "( " + staticjniType(c.arrayType) + " value ) {" );
                 pw.print( "\treturn (*thread_env)->Get" );
                 pw.print( getCallTypeForReturn( c.arrayType.getComponentType() ) );
                 pw.print( "ArrayElements" );
                 pw.println( "( thread_env, value, NULL );");
                 pw.println( "};" );
 
             	// Release array
                 pw.println();
                 pw.println( "void " + release_sig_local + "( " + staticjniType(c.arrayType) + " value, " + staticjniType( c.arrayType.getComponentType() ) + "* ncopy ) {" );
                 pw.print( "\treturn (*thread_env)->Release" );
                 pw.print( getCallTypeForReturn( c.arrayType.getComponentType() ) );
                 pw.print( "ArrayElements" );
                 pw.println( "( thread_env, value, ncopy, 0 );");
                 pw.println( "};" );
 
             	// Length of array
                 String guard = "STATICJNI_GET_LENGTH_" + length_sig_local;
                 pw.println( "#ifndef "+ guard );
                 pw.println( "#define "+ guard );
                 pw.println( "" );
                 pw.println( "jint " + length_sig_local + "( " + staticjniType(c.arrayType) + " value ) {" );
                 pw.print( "\treturn (*thread_env)->GetArrayLength" );
                 pw.println( "( thread_env, value );");
                 pw.println( "};" );
                 pw.println( "#endif" );
             }
 
             pw.println(cppGuardEnd());
         } catch (TypeSignature.SignatureException e) {
             util.error("jni.sigerror", e.getMessage());
         }
     }
     
     String getCallSuffixForReturn( TypeMirror t ) {
     	switch ( t.getKind() ) {
 			case VOID:     return "VoidMethod";
 			case BOOLEAN:  return "BooleanMethod";
 			case BYTE:     return "ByteMethod";
 			case CHAR:     return "CharMethod";
 			case SHORT:    return "ShortMethod";
 			case INT:      return "IntMethod";
 			case LONG:     return "LongMethod";
 			case FLOAT:    return "FloatMethod";
 			case DOUBLE:   return "DoubleMethod";
 			default:       return "ObjectMethod";
 		}
     }
 
     String getCallTypeForReturn(TypeMirror t) {
         switch (t.getKind()) {
         case VOID:
             return "Void";
         case BOOLEAN:
             return "Boolean";
         case BYTE:
             return "Byte";
         case CHAR:
             return "Char";
         case SHORT:
             return "Short";
         case INT:
             return "Int";
         case LONG:
             return "Long";
         case FLOAT:
             return "Float";
         case DOUBLE:
             return "Double";
 		default:
 			return "Object";
 		}
 	}
 
     void tryToRegisterNativeCall(TypeElement clazz, String name,
     		Set<ExecutableElement> callbacks,
     		Set<VariableElement> callbacks_to_field ) {
     	
 		List<ExecutableElement> methods = ElementFilter.methodsIn( clazz.getEnclosedElements() );
 		for (ExecutableElement m: methods) {
 			if ( name.toString().equals( m.getSimpleName().toString() ) ) {
 				callbacks.add(m);
 				return;
 			}
 		}
 			
 		List<VariableElement> fields = ElementFilter.fieldsIn( clazz.getEnclosedElements() );
 		for ( VariableElement f: fields ) {
 			if ( f.getSimpleName().toString().equals( name ) ) {
 				callbacks_to_field.add( f );
 				return;
 			}
 		}
 		
 		util.error("err.staticjni.targetnotfound", name ); // TODO intl
     }
 }
