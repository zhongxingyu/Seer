 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * NX^\NX
  * 
  * @author higo
  * 
  */
 public class UnresolvedClassTypeInfo implements UnresolvedReferenceTypeInfo {
 
     /**
      * p\ȖOԖCQƖ^ď
      * 
      * @param availableNamespaces OԖ
      * @param referenceName QƖ
      */
     public UnresolvedClassTypeInfo(final Set<AvailableNamespaceInfo> availableNamespaces,
             final String[] referenceName) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == availableNamespaces) || (null == referenceName)) {
             throw new NullPointerException();
         }
 
         this.availableNamespaces = availableNamespaces;
         this.referenceName = referenceName;
         this.typeArguments = new LinkedList<UnresolvedReferenceTypeInfo>();
     }
 
     /**
      * ̖NX^łɉς݂ǂԂD
      * 
      * @return ς݂̏ꍇ trueCĂȂꍇ false
      */
     public boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     /**
      * ̖NX^̉ς݂̌^Ԃ
      */
     public TypeInfo getResolved() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     public TypeInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfoManager) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         //@PQƂ̏ꍇ
         if (this.isMoniminalReference()) {
 
             //@C|[gĂpbP[W̃NX猟
             for (final AvailableNamespaceInfo availableNamespace : this.getAvailableNamespaces()) {
 
                 // import aaa.bbb.*̏ꍇ (NX̕*)
                 if (availableNamespace.isAllClasses()) {
 
                     //@p\ȃNXꗗ擾C猟
                     final String[] namespace = availableNamespace.getNamespace();
                     for (final ClassInfo availableClass : classInfoManager.getClassInfos(namespace)) {
 
                         //@QƂĂNX
                         if (this.referenceName[0].equals(availableClass.getClassName())) {
                             this.resolvedInfo = new ClassTypeInfo(availableClass);
                             for (final UnresolvedTypeInfo unresolvedTypeArgument : this
                                     .getTypeArguments()) {
                                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                         usingClass, usingMethod, classInfoManager,
                                         fieldInfoManager, methodInfoManager);
                                 ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                             }
                             return this.resolvedInfo;
                         }
                     }
 
                     // import aaa.bbb.CCC̏ꍇ@(NX܂ŋLqĂ)
                 } else {
 
                     ClassInfo referencedClass = classInfoManager.getClassInfo(availableNamespace
                             .getImportName());
                     // null ̏ꍇ͊ONX̎QƂƂ݂Ȃ
                     if (null == referencedClass) {
                        
                        // import ̊ONXƂ̎QƂĂNXvȂꍇ͎ importT 
                        final String[] namespace = availableNamespace.getNamespace();
                        final String importedClassName = namespace[namespace.length - 1];
                        if(!this.referenceName[0].equals(importedClassName)){
                            continue;
                        }
                        
                         referencedClass = new ExternalClassInfo(availableNamespace.getImportName());
                         classInfoManager.add((ExternalClassInfo) referencedClass);
                     }
 
                     this.resolvedInfo = new ClassTypeInfo(referencedClass);
                     for (final UnresolvedTypeInfo unresolvedTypeArgument : this.getTypeArguments()) {
                         final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                 usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                 methodInfoManager);
                         ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                     }
                     return this.resolvedInfo;
                 }
             }
 
             // ftHgpbP[WNX
             for (final ClassInfo availableClass : classInfoManager.getClassInfos(new String[0])) {
 
                 // QƂĂNX
                 if (this.referenceName[0].equals(availableClass.getClassName())) {
                     this.resolvedInfo = new ClassTypeInfo(availableClass);
                     for (final UnresolvedTypeInfo unresolvedTypeArgument : this.getTypeArguments()) {
                         final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                 usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                 methodInfoManager);
                         ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                     }
                     return this.resolvedInfo;
                 }
             }
 
             // sȃNX^ł
             final ExternalClassInfo unknownReferencedClass = new ExternalClassInfo(
                     this.referenceName[0]);
             this.resolvedInfo = new ClassTypeInfo(unknownReferencedClass);
             return this.resolvedInfo;
 
             // QƂ̏ꍇ
         } else {
 
             //@C|[gĂNX̎qNX猟
             AVAILABLENAMESPACE: for (final AvailableNamespaceInfo availableNamespace : this
                     .getAvailableNamespaces()) {
 
                 // import aaa.bbb.*̏ꍇ (NX̕*)
                 if (availableNamespace.isAllClasses()) {
 
                     // p\ȃNXꗗ擾C猟
                     final String[] namespace = availableNamespace.getNamespace();
                     for (final ClassInfo availableClass : classInfoManager.getClassInfos(namespace)) {
 
                         //@QƂĂNX
                         if (this.referenceName[0].equals(availableClass.getClassName())) {
 
                             // ΏۃNXłȂꍇ͓NX͂킩Ȃ̂ŃXLbv
                             if (!(availableClass instanceof TargetClassInfo)) {
                                 continue AVAILABLENAMESPACE;
                             }
 
                             // ΏۃNX̏ꍇ́CɓNXǂčs
                             TargetClassInfo currentClass = (TargetClassInfo) availableClass;
                             INDEX: for (int index = 1; index < this.referenceName.length; index++) {
                                 final SortedSet<TargetInnerClassInfo> innerClasses = currentClass
                                         .getInnerClasses();
                                 for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                     if (this.referenceName[index].equals(innerClass.getClassName())) {
                                         currentClass = innerClass;
                                         continue INDEX;
                                     }
 
                                     // ɓB̂́CNXȂꍇ
                                     final ExternalClassInfo unknownReferencedClass = new ExternalClassInfo(
                                             this.referenceName[this.referenceName.length - 1]);
                                     this.resolvedInfo = new ClassTypeInfo(unknownReferencedClass);
                                     for (final UnresolvedTypeInfo unresolvedTypeArgument : this
                                             .getTypeArguments()) {
                                         final TypeInfo typeArgument = unresolvedTypeArgument
                                                 .resolve(usingClass, usingMethod,
                                                         classInfoManager, fieldInfoManager,
                                                         methodInfoManager);
                                         ((ClassTypeInfo) this.resolvedInfo)
                                                 .addTypeArgument(typeArgument);
                                     }
                                     return this.resolvedInfo;
                                 }
                             }
 
                             //@ɓB̂́CNXꍇ
                             this.resolvedInfo = new ClassTypeInfo(currentClass);
                             for (final UnresolvedTypeInfo unresolvedTypeArgument : this
                                     .getTypeArguments()) {
                                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                         usingClass, usingMethod, classInfoManager,
                                         fieldInfoManager, methodInfoManager);
                                 ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                             }
                             return this.resolvedInfo;
                         }
                     }
 
                     // import aaa.bbb.CCC̏ꍇ (NX܂ŋLqĂ)
                 } else {
 
                     ClassInfo importClass = classInfoManager.getClassInfo(availableNamespace
                             .getImportName());
 
                     //@null ̏ꍇ͂(O)NX\IuWFNg쐬 
                     if (null == importClass) {
                         importClass = new ExternalClassInfo(availableNamespace.getImportName());
                         classInfoManager.add((ExternalClassInfo) importClass);
                     }
 
                     // importClassΏۃNXłȂꍇ͓NX񂪂킩Ȃ̂ŃXLbv
                     if (!(importClass instanceof TargetClassInfo)) {
                         continue AVAILABLENAMESPACE;
                     }
 
                     // ΏۃNX̏ꍇ́CɓNXǂčs
                     TargetClassInfo currentClass = (TargetClassInfo) importClass;
                     INDEX: for (int index = 1; index < this.referenceName.length; index++) {
                         final SortedSet<TargetInnerClassInfo> innerClasses = currentClass
                                 .getInnerClasses();
                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                             if (this.referenceName[index].equals(innerClass.getClassName())) {
                                 currentClass = innerClass;
                                 continue INDEX;
                             }
 
                             // ɓB̂́CNXȂꍇ           
                             final ExternalClassInfo unknownReferencedClass = new ExternalClassInfo(
                                     this.referenceName[this.referenceName.length - 1]);
                             this.resolvedInfo = new ClassTypeInfo(unknownReferencedClass);
                             for (final UnresolvedTypeInfo unresolvedTypeArgument : this
                                     .getTypeArguments()) {
                                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                         usingClass, usingMethod, classInfoManager,
                                         fieldInfoManager, methodInfoManager);
                                 ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                             }
                             return this.resolvedInfo;
                         }
                     }
 
                     //@ɓB̂́CNXꍇ
                     this.resolvedInfo = new ClassTypeInfo(currentClass);
                     for (final UnresolvedTypeInfo unresolvedTypeArgument : this.getTypeArguments()) {
                         final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                 usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                 methodInfoManager);
                         ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
                     }
                     return this.resolvedInfo;
                 }
             }
 
             // sȃNX^ł
             final ExternalClassInfo unknownReferencedClass = new ExternalClassInfo(
                     this.referenceName[this.referenceName.length - 1]);
             this.resolvedInfo = new ClassTypeInfo(unknownReferencedClass);
             for (final UnresolvedTypeInfo unresolvedTypeArgument : this.getTypeArguments()) {
                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                         usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                 ((ClassTypeInfo) this.resolvedInfo).addTypeArgument(typeArgument);
             }
             return this.resolvedInfo;
         }
     }
 
     /**
      * p\ȖOԁC^̊SC^ď
      * @param referenceName ^̊SC
      */
     public UnresolvedClassTypeInfo(final String[] referenceName) {
         this(new HashSet<AvailableNamespaceInfo>(), referenceName);
     }
 
     /**
      * ^p[^gpǉ
      * 
      * @param typeParameterUsage ǉ^p[^gp
      */
     public final void addTypeArgument(final UnresolvedReferenceTypeInfo typeParameterUsage) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeParameterUsage) {
             throw new NullPointerException();
         }
 
         this.typeArguments.add(typeParameterUsage);
     }
 
     /**
      * ̃NXQƂŎgpĂ^p[^ List Ԃ
      * 
      * @return ̃NXQƂŎgpĂ^p[^ List
      */
     public final List<UnresolvedReferenceTypeInfo> getTypeArguments() {
         return Collections.unmodifiableList(this.typeArguments);
     }
 
     /**
      * ̎Qƌ^̖OԂ
      * 
      * @return ̎Qƌ^̖OԂ
      */
     public final String getTypeName() {
         return this.referenceName[this.referenceName.length - 1];
     }
 
     /**
      * ̎Qƌ^̎QƖԂ
      * 
      * @return ̎Qƌ^̎QƖԂ
      */
     public final String[] getReferenceName() {
         return this.referenceName;
     }
 
     /**
      * ̎Qƌ^̎QƖŗ^ꂽŌĕԂ
      * 
      * @param delimiter ɗp镶
      * @return ̎Qƌ^̎QƖŗ^ꂽŌ
      */
     public final String getReferenceName(final String delimiter) {
 
         if (null == delimiter) {
             throw new NullPointerException();
         }
 
         final StringBuilder sb = new StringBuilder(this.referenceName[0]);
         for (int i = 1; i < this.referenceName.length; i++) {
             sb.append(delimiter);
             sb.append(this.referenceName[i]);
         }
 
         return sb.toString();
     }
 
     /**
      * ̎Qƌ^̊S薼Ƃĉ\̂閼OԖ̈ꗗԂ
      * 
      * @return ̎Qƌ^̊S薼Ƃĉ\̂閼OԖ̈ꗗ
      */
     public final Set<AvailableNamespaceInfo> getAvailableNamespaces() {
         return this.availableNamespaces;
     }
 
     /**
      * ̎QƂPǂԂ
      * 
      * @return@PłꍇtrueCłȂꍇfalse
      */
     public final boolean isMoniminalReference() {
         return 1 == this.referenceName.length;
     }
 
     public final static UnresolvedClassTypeInfo getInstance(UnresolvedClassInfo referencedClass) {
         return new UnresolvedClassTypeInfo(referencedClass.getFullQualifiedName());
     }
 
     public final UnresolvedClassReferenceInfo getUsage() {
 
         UnresolvedClassReferenceInfo usage = new UnresolvedClassReferenceInfo(
                 this.availableNamespaces, this.referenceName);
         for (UnresolvedReferenceTypeInfo typeArgument : this.typeArguments) {
             usage.addTypeArgument(typeArgument);
         }
         return usage;
     }
 
     /**
      * p\ȖOԖۑ邽߂̕ϐCO̍ۂɗp
      */
     private final Set<AvailableNamespaceInfo> availableNamespaces;
 
     /**
      * QƖۑϐ
      */
     private final String[] referenceName;
 
     /**
      * ^ۑ邽߂̕ϐ
      */
     private final List<UnresolvedReferenceTypeInfo> typeArguments;
 
     private TypeInfo resolvedInfo;
 
 }
