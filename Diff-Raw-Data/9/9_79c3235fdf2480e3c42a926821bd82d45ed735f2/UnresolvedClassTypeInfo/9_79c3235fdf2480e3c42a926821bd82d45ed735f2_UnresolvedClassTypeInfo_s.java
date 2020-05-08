 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassImportStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterizable;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * NX^\NX
  * 
  * @author higo
  * 
  */
 public class UnresolvedClassTypeInfo implements UnresolvedReferenceTypeInfo<ReferenceTypeInfo> {
 
     /**
      * p\ȖOԖCQƖ^ď
      * 
      * @param availableNamespaces OԖ
      * @param referenceName QƖ
      */
     public UnresolvedClassTypeInfo(
             final List<UnresolvedClassImportStatementInfo> availableNamespaces,
             final String[] referenceName) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == availableNamespaces) || (null == referenceName)) {
             throw new NullPointerException();
         }
 
         this.availableNamespaces = availableNamespaces;
         this.referenceName = Arrays.<String> copyOf(referenceName, referenceName.length);
         this.typeArguments = new LinkedList<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>>();
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
     @Override
     public ReferenceTypeInfo getResolved() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     @Override
     public ReferenceTypeInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         // import Ŏw肳ĂNXo^ĂȂȂCONXƂēo^
         for (final UnresolvedClassImportStatementInfo availableNamespace : this
                 .getAvailableNamespaces()) {
 
             if (!availableNamespace.isAll()) {
                 final String[] fullQualifiedName = availableNamespace.getImportName();
                 if (!classInfoManager.hasClassInfo(fullQualifiedName)) {
                     final ExternalClassInfo externalClassInfo = new ExternalClassInfo(
                             fullQualifiedName);
                     classInfoManager.add(externalClassInfo);
                 }
             }
         }
 
         // o^ĂNX猟o
         final String[] referenceName = this.getReferenceName();
         final Collection<ClassInfo> candidateClasses = classInfoManager
                 .getClassInfos(referenceName[referenceName.length - 1]);
 
         //QƂ̏ꍇ͊S薼ǂ𒲂ׂCPQƂ̏ꍇ̓ftHgpbP[W璲ׂ
         {
             final ClassInfo matchedClass = classInfoManager.getClassInfo(referenceName);
             if (null != matchedClass) {
                 final ClassTypeInfo classType = new ClassTypeInfo(matchedClass);
                 for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                         .getTypeArguments()) {
                     final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                             usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                     classType.addTypeArgument(typeArgument);
                 }
                 this.resolvedInfo = classType;
                 return this.resolvedInfo;
             }
         }
 
         // PQƂ̏ꍇ݂͌̃NX̍ŊONX̓NX猟
         if (this.isMoniminalReference()) {
 
            for (final ClassInfo innerClass : TargetClassInfo.getAccessibleInnerClasses(usingClass)) {
                 if (candidateClasses.contains(innerClass)) {
                     final ClassTypeInfo classType = new ClassTypeInfo(innerClass);
                     for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                             .getTypeArguments()) {
                         final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                         classType.addTypeArgument(typeArgument);
                     }
                     this.resolvedInfo = classType;
                     return this.resolvedInfo;
                 }
             }
         }
 
         // C|[gĂNX猟iPQƂ̏ꍇj
         if (this.isMoniminalReference()) {
             for (final UnresolvedClassImportStatementInfo unresolvedClassImportStatement : this
                     .getAvailableNamespaces()) {
 
                 final ClassImportStatementInfo classImportStatement = unresolvedClassImportStatement
                         .resolve(usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                 methodInfoManager);
                 for (final ClassInfo importedClass : classImportStatement.getImportedClasses()) {
                     if (candidateClasses.contains(importedClass)) {
                         final ClassTypeInfo classType = new ClassTypeInfo(importedClass);
                         for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                                 .getTypeArguments()) {
                             final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                     usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                     methodInfoManager);
                             classType.addTypeArgument(typeArgument);
                         }
                         this.resolvedInfo = classType;
                         return this.resolvedInfo;
                     }
                 }
             }
         }
 
         // C|[gĂNX猟iQƂ̏ꍇj 
         if (!this.isMoniminalReference()) {
 
             for (final UnresolvedClassImportStatementInfo unresolvedClassImportStatement : this
                     .getAvailableNamespaces()) {
 
                 final ClassImportStatementInfo classImportStatement = unresolvedClassImportStatement
                         .resolve(usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                 methodInfoManager);
 
                 for (final ClassInfo importedClass : classImportStatement.getImportedClasses()) {
 
                     for (final ClassInfo candidateClass : candidateClasses) {
 
                         final String[] candidateFQName = candidateClass.getFullQualifiedName();
 
                         CLASS: for (final ClassInfo accessibleInnerClass : TargetClassInfo
                                 .getAccessibleInnerClasses(importedClass)) {
 
                             final String[] availableFQName = accessibleInnerClass
                                     .getFullQualifiedName();
 
                             for (int index = 1; index <= referenceName.length; index++) {
                                 if (!availableFQName[availableFQName.length - index]
                                         .equals(referenceName[referenceName.length - index])) {
                                     continue CLASS;
                                 }
                             }
 
                             for (int index = 1; index <= referenceName.length; index++) {
                                 if (!candidateFQName[candidateFQName.length - index]
                                         .equals(referenceName[referenceName.length - index])) {
                                     continue CLASS;
                                 }
                             }
 
                             final ClassTypeInfo classType = new ClassTypeInfo(candidateClass);
                             for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                                     .getTypeArguments()) {
                                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                         usingClass, usingMethod, classInfoManager,
                                         fieldInfoManager, methodInfoManager);
                                 classType.addTypeArgument(typeArgument);
                             }
                             this.resolvedInfo = classType;
                             return this.resolvedInfo;
                         }
                     }
                 }
             }
         }
 
         // PQƂ̏ꍇ͌^p[^ǂ𒲂ׂ
         if (this.isMoniminalReference()) {
 
             TypeParameterizable typeParameterizableUnit = null != usingMethod ? usingMethod
                     : usingClass;
             do {
                 for (final TypeParameterInfo typeParameter : typeParameterizableUnit
                         .getTypeParameters()) {
                     if (typeParameter.getName().equals(referenceName[0])) {
                         this.resolvedInfo = new TypeParameterTypeInfo(typeParameter);
                         return this.resolvedInfo;
                     }
                 }
                 typeParameterizableUnit = typeParameterizableUnit.getOuterTypeParameterizableUnit();
             } while (null != typeParameterizableUnit);
         }
 
         //ɂ̂́CNXȂƂ
         if (this.isMoniminalReference()) {
 
             //System.out.println(referenceName[0]);
             final ExternalClassInfo externalClassInfo = new ExternalClassInfo(referenceName[0]);
             final ClassTypeInfo classType = new ClassTypeInfo(externalClassInfo);
             for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                     .getTypeArguments()) {
                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                         usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                 classType.addTypeArgument(typeArgument);
             }
             this.resolvedInfo = classType;
 
         } else {
 
             // C|[gQƖgݍ킹邱Ƃł邩
             // Ƃ΁C import A.B.C ŁCQƖCC.Dł΁CS薼A.B.C.D̃NX邱ƂɂȂ
             for (final UnresolvedClassImportStatementInfo availableNamespace : this
                     .getAvailableNamespaces()) {
 
                 if (!availableNamespace.isAll()) {
                     final String[] importedName = availableNamespace.getFullQualifiedName();
                     if (importedName[importedName.length - 1].equals(referenceName[0])) {
                         final String[] fqName = new String[referenceName.length
                                 + importedName.length - 1];
                         int index = 0;
                         for (; index < importedName.length; index++) {
                             fqName[index] = importedName[index];
                         }
                         for (int i = 1; i < referenceName.length; i++, index++) {
                             fqName[index] = referenceName[i];
                         }
 
                         final ExternalClassInfo externalClassInfo = new ExternalClassInfo(fqName);
                         final ClassTypeInfo classType = new ClassTypeInfo(externalClassInfo);
                         for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                                 .getTypeArguments()) {
                             final TypeInfo typeArgument = unresolvedTypeArgument.resolve(
                                     usingClass, usingMethod, classInfoManager, fieldInfoManager,
                                     methodInfoManager);
                             classType.addTypeArgument(typeArgument);
                         }
                         this.resolvedInfo = classType;
                         return this.resolvedInfo;
                     }
                 }
 
             }
 
             final ExternalClassInfo externalClassInfo = new ExternalClassInfo(referenceName);
             final ClassTypeInfo classType = new ClassTypeInfo(externalClassInfo);
             for (final UnresolvedTypeInfo<? extends ReferenceTypeInfo> unresolvedTypeArgument : this
                     .getTypeArguments()) {
                 final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                         usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                 classType.addTypeArgument(typeArgument);
             }
             this.resolvedInfo = classType;
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * p\ȖOԁC^̊SC^ď
      * @param referenceName ^̊SC
      */
     public UnresolvedClassTypeInfo(final String[] referenceName) {
         this(new LinkedList<UnresolvedClassImportStatementInfo>(), referenceName);
     }
 
     /**
      * ^p[^gpǉ
      * 
      * @param typeParameterUsage ǉ^p[^gp
      */
     public final void addTypeArgument(
             final UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo> typeParameterUsage) {
 
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
     public final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> getTypeArguments() {
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
         return Arrays.<String> copyOf(this.referenceName, this.referenceName.length);
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
     public final List<UnresolvedClassImportStatementInfo> getAvailableNamespaces() {
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
 
     /**
      * NX^ƁC̖Qƌ^Ԃ
      * 
      * @param referencedClass NX
      * @return ^ꂽNX̖Qƌ^
      */
     public final static UnresolvedClassTypeInfo getInstance(UnresolvedClassInfo referencedClass) {
         return new UnresolvedClassTypeInfo(referencedClass.getFullQualifiedName());
     }
 
     /**
      * ̖Qƌ^\NXQƂԂ
      * 
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      * @return ̖Qƌ^\NXQ
      */
     public final UnresolvedClassReferenceInfo getUsage(final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         UnresolvedClassReferenceInfo usage = new UnresolvedClassReferenceInfo(
                 this.availableNamespaces, this.referenceName);
         usage.setFromLine(fromLine);
         usage.setFromColumn(fromColumn);
         usage.setToLine(toLine);
         usage.setToColumn(toColumn);
 
         for (UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo> typeArgument : this.typeArguments) {
             usage.addTypeArgument(typeArgument);
         }
         return usage;
     }
 
     /**
      * p\ȖOԖۑ邽߂̕ϐCO̍ۂɗp
      */
     private final List<UnresolvedClassImportStatementInfo> availableNamespaces;
 
     /**
      * QƖۑϐ
      */
     private final String[] referenceName;
 
     /**
      * ^ۑ邽߂̕ϐ
      */
     private final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> typeArguments;
 
     private ReferenceTypeInfo resolvedInfo;
 
 }
