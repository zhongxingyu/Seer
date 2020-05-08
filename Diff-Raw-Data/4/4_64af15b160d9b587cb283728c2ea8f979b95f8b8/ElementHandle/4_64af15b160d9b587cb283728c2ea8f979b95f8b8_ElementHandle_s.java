 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.api.javafx.source;
 
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symtab;
 import com.sun.tools.javac.model.JavacElements;
 import com.sun.tools.javafx.api.JavafxcTaskImpl;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.List;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.ArrayType;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.ErrorType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 
 /**
  * Represents a handle for {@link Element} which can be kept and later resolved
  * by another javac. The javac {@link Element}s are valid only in a single run
  * of a {@link CancellableTask}. A client needing to
  * keep a reference to an {@link Element} and use it in another {@link CancellableTask}
  * must serialize it into an {@link ElementHandle}.
  * Currently not all {@link Element}s can be serialized. See {@link #create} for details.
  * <div class="nonnormative">
  * <p>
  * Typical usage of {@link ElementHandle} is as follows:
  * </p>
  * <pre>
  * final ElementHandle[] elementHandle = new ElementHandle[1];
  * javafxSource.runUserActionTask(new Task&lt;CompilationController>() {
  *     public void run(CompilationController compilationController) {
  *         compilationController.toPhase(Phase.ANALYZED);
  *         CompilationUnitTree cu = compilationController.getTree();
  *         List&lt;? extends Tree> types = getTypeDecls(cu);
  *         Tree tree = getInterestingElementTree(types);
  *         Element element = compilationController.getElement(tree);
  *         elementHandle[0] = ElementHandle.create(element);
  *    }
  * }, true);
  *
  * otherJavafxSource.runUserActionTask(new Task&lt;CompilationController>() {
  *     public void run(CompilationController compilationController) {
  *         compilationController.toPhase(Phase.RESOLVED);
  *         Element element = elementHandle[0].resolve(compilationController);
  *         // ....
  *    }
  * }, true);
  * </pre>
  * </div>
  * @author Tomas Zezula
  */
 public class ElementHandle<T extends Element> {
 
     private ElementKind kind;
     private String[] signatures;
 
     public ElementHandle(final ElementKind kind, String[] signatures) {
         assert kind != null;
         assert signatures != null;
         this.kind = kind;
         this.signatures = signatures;
     }
 
     /**
      * Resolves an {@link Element} from the {@link ElementHandle}.
      * @param compilationInfo representing the {@link javax.tools.CompilationTask}
      * in which the {@link Element} should be resolved.
      * @return resolved subclass of {@link Element} or null if the elment does not exist on
      * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
      */
     @SuppressWarnings ("unchecked")     // NOI18N
     public T resolve (final CompilationInfo compilationInfo) {
         if (compilationInfo == null) throw new IllegalArgumentException();
         assert compilationInfo.impl != null;
 
         return resolveImpl (compilationInfo.impl.getJavafxcTask());
     }
 
     public ElementKind getKind() {
         return kind;
     }
     
     public String[] getSignatures() {
         return signatures.clone();
     }
     
     /**
      * Tests if the handle has the same signature as the parameter.
      * The handles with the same signatures are resolved into the same
      * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
      * the different {@link Element}s in the different {@link javax.tools.JavaCompiler} tasks.
      * @param handle to be checked
      * @return true if the handles resolve into the same {@link Element}s
      * in the same {@link javax.tools.JavaCompiler} task.
      */
     public boolean signatureEquals (final ElementHandle<? extends Element> handle) {
          if (!isSameKind (this.kind, handle.kind) || this.signatures.length != handle.signatures.length) {
              return false;
          }
          for (int i=0; i<signatures.length; i++) {
              if (!signatures[i].equals(handle.signatures[i])) {
                  return false;
              }
          }
          return true;
     }
 
     private static boolean isSameKind (ElementKind k1, ElementKind k2) {
         return k1 == k2 || (k1 == ElementKind.OTHER && k2.isClass()) || (k2 == ElementKind.OTHER && k1.isClass());
     }
     
     private T resolveImpl (final JavafxcTaskImpl jt) {
         switch (this.kind) {            
             case PACKAGE:
                 assert signatures.length == 1;
                 return (T) jt.getElements().getPackageElement(signatures[0]);
             case CLASS:
             case INTERFACE:
             case ENUM:
             case ANNOTATION_TYPE:
             case OTHER: // ??
                 assert signatures.length == 1;
                 return (T)getTypeElementByBinaryName(signatures[0], jt);
             case METHOD:
             case CONSTRUCTOR:            
             {
                 assert signatures.length == 3;
                 final TypeElement type = getTypeElementByBinaryName (signatures[0], jt);
                 if (type != null) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (this.kind == member.getKind()) {
                            String[] desc = createExecutableDescriptor((ExecutableElement)member);
                            assert desc.length == 3;
                            if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                return (T) member;
                            }
                        }
                    }
                 }
                 break;
             }
             
             case INSTANCE_INIT:
             case STATIC_INIT:
             {
                 assert signatures.length == 2;
                 final TypeElement type = getTypeElementByBinaryName (signatures[0], jt);
                 if (type != null) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (this.kind == member.getKind()) {
                            String[] desc = createExecutableDescriptor((ExecutableElement)member);
                            assert desc.length == 2;
                            if (this.signatures[1].equals(desc[1])) {
                                return (T) member;
                            }
                        }
                    }
                 }
                 break;
             }
 
             case FIELD:
             case ENUM_CONSTANT:
             {
                 assert signatures.length == 3;
                 final TypeElement type = getTypeElementByBinaryName (signatures[0], jt);
                 if (type != null) {
                     final List<? extends Element> members = type.getEnclosedElements();
                     for (Element member : members) {
                         if (this.kind == member.getKind()) {
                             String[] desc = createFieldDescriptor((VariableElement)member);
                             assert desc.length == 3;
                             if (signatures[1].equals(desc[1]) && signatures[2].equals(desc[2])) {
                                 return (T) member;
                             }
                         }
                     }
                 }
                 break;
             }
 /*            case TYPE_PARAMETER:
             {
                 if (signatures.length == 2) {
                      TypeElement type = getTypeElementByBinaryName (signatures[0], jt);
                      if (type != null) {
                          List<? extends TypeParameterElement> tpes = type.getTypeParameters();
                          for (TypeParameterElement tpe : tpes) {
                              if (tpe.getSimpleName().contentEquals(signatures[1])) {
                                  return (T)tpe;
                              }
                          }
                      }
                 }
                 else if (signatures.length == 4) {
                     final TypeElement type = getTypeElementByBinaryName (signatures[0], jt);
                     if (type != null) {
                         final List<? extends Element> members = type.getEnclosedElements();
                         for (Element member : members) {
                             if (member.getKind() == ElementKind.METHOD || member.getKind() == ElementKind.CONSTRUCTOR) {
                                 String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                                 assert desc.length == 3;
                                 if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                     assert member instanceof ExecutableElement;
                                     List<? extends TypeParameterElement> tpes =((ExecutableElement)member).getTypeParameters();
                                     for (TypeParameterElement tpe : tpes) {
                                         if (tpe.getSimpleName().contentEquals(signatures[3])) {
                                             return (T) tpe;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 else {
                     throw new IllegalStateException ();
                 }
                 break;
             }*/
             default:
                 throw new IllegalStateException ();
         }
         return null;
     }
     
     /**
      * Factory method for creating {@link ElementHandle}.
      * @param element for which the {@link ElementHandle} should be created. Permitted
      * {@link ElementKind}s
      * are: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
      * {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#ANNOTATION_TYPE}, {@link ElementKind#METHOD},
      * {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
      * {@link ElementKind#FIELD}, and {@link ElementKind#ENUM_CONSTANT}.
      * @return a new {@link ElementHandle}
      * @throws IllegalArgumentException if the element is of an unsupported {@link ElementKind}
      */
     public static<T extends Element> ElementHandle<T> create (final T element) throws IllegalArgumentException {
         assert element != null;
         ElementKind kind = element.getKind();
         String[] signatures;
         switch (kind) {
             case PACKAGE:
                 assert element instanceof PackageElement;
                 signatures = new String[]{((PackageElement)element).getQualifiedName().toString()};
                 break;
             case CLASS:
             case INTERFACE:
             case ENUM:
             case ANNOTATION_TYPE:
                 assert element instanceof TypeElement;
                 signatures = new String[] {encodeClassNameOrArray((TypeElement)element)};
                 break;
             case METHOD:
             case CONSTRUCTOR:                
             case INSTANCE_INIT:
             case STATIC_INIT:
                 assert element instanceof ExecutableElement;
                 signatures = createExecutableDescriptor((ExecutableElement)element);
                 break;
             case LOCAL_VARIABLE: // space magic
             case FIELD:
             case ENUM_CONSTANT:
                 assert element instanceof VariableElement;
                 signatures = createFieldDescriptor((VariableElement)element);
                 break;
 /*            case TYPE_PARAMETER:
                 assert element instanceof TypeParameterElement;
                 TypeParameterElement tpe = (TypeParameterElement) element;
                 Element ge = tpe.getGenericElement();
                 ElementKind gek = ge.getKind();
                 if (gek.isClass() || gek.isInterface()) {
                     assert ge instanceof TypeElement;
                     signatures = new String[2];
                     signatures[0] = ClassFileUtil.encodeClassNameOrArray((TypeElement)ge);
                     signatures[1] = tpe.getSimpleName().toString();
                 }
                 else if (gek == ElementKind.METHOD || gek == ElementKind.CONSTRUCTOR) {
                     assert ge instanceof ExecutableElement;
                     String[] _sigs = ClassFileUtil.createExecutableDescriptor((ExecutableElement)ge);
                     signatures = new String[_sigs.length + 1];
                     System.arraycopy(_sigs, 0, signatures, 0, _sigs.length);
                     signatures[_sigs.length] = tpe.getSimpleName().toString();
                 }
                 else {
                     throw new IllegalArgumentException(gek.toString());
                 }
                 break;*/
             default:
                 throw new IllegalArgumentException(kind.toString());
         }
         return new ElementHandle<T> (kind, signatures);
     }
 
 
     private static String encodeClassNameOrArray(TypeElement td) {
         assert td != null;
         CharSequence qname = td.getQualifiedName();
         TypeMirror enclosingType = td.getEnclosingElement().asType();
         if (qname != null && enclosingType != null && enclosingType.getKind() == TypeKind.NONE && "Array".equals(qname.toString())) {     //NOI18N
             return "[";  //NOI18N
         } else {
             return encodeClassName(td);
         }
     }
 
     private static String encodeClassName(TypeElement td) {
         assert td != null;
         StringBuilder sb = new StringBuilder();
         encodeClassName(td, sb, '.');    // NOI18N
         return sb.toString();
     }
 
     private static void encodeType(final TypeMirror type, final StringBuilder sb) {
         if (type == null) {
             sb.append('?');
             return;
         }
         switch (type.getKind()) {
             case VOID:
                 sb.append('V');	    // NOI18N
                 break;
             case BOOLEAN:
                 sb.append('Z');	    // NOI18N
                 break;
             case BYTE:
                 sb.append('B');	    // NOI18N
                 break;
             case SHORT:
                 sb.append('S');	    // NOI18N
                 break;
             case INT:
                 sb.append('I');	    // NOI18N
                 break;
             case LONG:
                 sb.append('J');	    // NOI18N
                 break;
             case CHAR:
                 sb.append('C');	    // NOI18N
                 break;
             case FLOAT:
                 sb.append('F');	    // NOI18N
                 break;
             case DOUBLE:
                 sb.append('D');	    // NOI18N
                 break;
             case ARRAY:
                 sb.append('[');	    // NOI18N
                 assert type instanceof ArrayType;
                 encodeType(((ArrayType) type).getComponentType(), sb);
                 break;
             case DECLARED: {
                 sb.append('L');	    // NOI18N
                 TypeElement te = (TypeElement) ((DeclaredType) type).asElement();
                 encodeClassName(te, sb, '/');
                 sb.append(';');	    // NOI18N
                 break;
             }
             case ERROR: {                
                 TypeElement te = (TypeElement) ((ErrorType) type).asElement();
                 if (te != null) {
                     sb.append('L');
                     encodeClassName(te, sb,'/');
                     sb.append(';');	    // NOI18N
                     break;
                 } // else fall through
             }
             default:
                 throw new IllegalArgumentException(type.getKind().toString());
         }
     }
 
     private static void encodeClassName(TypeElement te, final StringBuilder sb, final char separator) {
         sb.append(((Symbol.ClassSymbol) te).flatname.toString().replace('.', separator));
     }
 
     private static String[] createExecutableDescriptor(final ExecutableElement ee) {
         assert ee != null;
         final ElementKind kind = ee.getKind();
         final String[] result = (kind == ElementKind.STATIC_INIT || kind == ElementKind.INSTANCE_INIT) ? new String[2] : new String[3];
         final Element enclosingType = ee.getEnclosingElement();
         assert enclosingType instanceof TypeElement;
         result[0] = encodeClassNameOrArray((TypeElement) enclosingType);
         if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
             final StringBuilder retType = new StringBuilder();
             if (kind == ElementKind.METHOD) {
                 result[1] = ee.getSimpleName().toString();
                 encodeType(ee.getReturnType(), retType);
             } else {
                 result[1] = "<init>";   // NOI18N
                 retType.append('V');    // NOI18N
             }
             StringBuilder sb = new StringBuilder();
             sb.append('(');             // NOI18N
             for (VariableElement pd : ee.getParameters()) {
                 encodeType(pd.asType(), sb);
             }
             sb.append(')');             // NOI18N
             sb.append(retType);
             result[2] = sb.toString();
         } else if (kind == ElementKind.INSTANCE_INIT) {
             result[1] = "<init>";       // NOI18N
         } else if (kind == ElementKind.STATIC_INIT) {
             result[1] = "<cinit>";      // NOI18N
         } else {
             throw new IllegalArgumentException();
         }
         return result;
     }
 
     private static String[] createFieldDescriptor(final VariableElement ve) {
         assert ve != null;
         String[] result = new String[3];
         Element enclosingElement = ve.getEnclosingElement();
 
        assert enclosingElement instanceof TypeElement;
         result[0] = encodeClassNameOrArray((TypeElement) enclosingElement);
         result[1] = ve.getSimpleName().toString();
         StringBuilder sb = new StringBuilder();
         encodeType(ve.asType(), sb);
         result[2] = sb.toString();
         return result;
     }
 
     private static TypeElement getTypeElementByBinaryName (final String signature, final JavafxcTaskImpl jt) {
         if (isArray(signature)) {
             return Symtab.instance(jt.getContext()).arrayClass;
         }
         else {
             final JavacElements elements = jt.getElements();                    
             // return (TypeElement) elements.getTypeElementByBinaryName(signature);
             return (TypeElement) elements.getTypeElement(signature.replace('$', '.'));
         }
     }
     
     private static boolean isArray (String signature) {
         return signature.length() == 1 && signature.charAt(0) == '[';
     }
 
     public org.netbeans.api.java.source.ElementHandle toJava() {
         try {
             // Load the right version of the ElementKind class and convert our instance to it
             Class ekClass = org.netbeans.api.java.source.ElementHandle.class.getClassLoader().loadClass("javax.lang.model.element.ElementKind");
             Object ekInstance = Enum.valueOf(ekClass, getKind().name());
 
             String[] sig = getSignatures();
             Class strArrClass = sig.getClass();
 
             Constructor ehCtor = org.netbeans.api.java.source.ElementHandle.class.getDeclaredConstructor(ekClass, strArrClass);
             ehCtor.setAccessible(true);
             org.netbeans.api.java.source.ElementHandle eh = (org.netbeans.api.java.source.ElementHandle) ehCtor.newInstance(ekInstance, sig);
             return eh;
         } catch (Exception ex) {
         }
         return null;
     }
     
     public static ElementHandle fromJava(org.netbeans.api.java.source.ElementHandle eh) {
         try {
             Method getKind = org.netbeans.api.java.source.ElementHandle.class.getDeclaredMethod("getKind");
             Object o = getKind.invoke(eh); //eh.getKind() - java's ElementKind type, can't reference directly
 
             ElementKind kind = Enum.valueOf(ElementKind.class, o.toString());
             
             Method getSignature = org.netbeans.api.java.source.ElementHandle.class.getDeclaredMethod("getSignature");
             getSignature.setAccessible(true);
             String[] signatures = (String[]) getSignature.invoke(eh);
             return new ElementHandle(kind, signatures);
         } catch (Exception e) {
         }
         return null;
     }
 }
