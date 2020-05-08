 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.NullTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VoidTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * Unresolved * Info  * Info 𓾂邽߂̖O[eBeBNX
  * 
  * @author y-higo
  * 
  */
 public final class NameResolver {
 
     /**
      * ^iUnresolvedTypeInfojς݌^iTypeInfojԂD Ής݌^񂪂Ȃꍇ null ԂD
      * 
      * @param unresolvedTypeInfo O^
      * @param usingClass ̖^݂ĂNX
      * @param usingMethod ̖^݂Ă郁\bhC\bhOłꍇ null ^
      * @param classInfoManager ^ɗpNXf[^x[X
      * @param fieldInfoManager ^ɗptB[hf[^x[X
      * @param methodInfoManager ^ɗp郁\bhf[^x[X
      * @param resolvCache ςUnresolvedTypeInfõLbV
      * @return Oꂽ^
      */
     public static TypeInfo resolveTypeInfo(final UnresolvedTypeInfo unresolvedTypeInfo,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == unresolvedTypeInfo) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if ((null != resolvedCache) && resolvedCache.containsKey(unresolvedTypeInfo)) {
             final TypeInfo type = resolvedCache.get(unresolvedTypeInfo);
             return type;
         }
 
         // v~eBu^̏ꍇ
         if (unresolvedTypeInfo instanceof PrimitiveTypeInfo) {
             return (PrimitiveTypeInfo) unresolvedTypeInfo;
 
             // void^̏ꍇ
         } else if (unresolvedTypeInfo instanceof VoidTypeInfo) {
             return (VoidTypeInfo) unresolvedTypeInfo;
 
         } else if (unresolvedTypeInfo instanceof NullTypeInfo) {
             return (NullTypeInfo) unresolvedTypeInfo;
 
             // Qƌ^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedReferenceTypeInfo) {
 
             // p\ȖOԂC^T
             final String[] referenceName = ((UnresolvedReferenceTypeInfo) unresolvedTypeInfo)
                     .getReferenceName();
             for (AvailableNamespaceInfo availableNamespace : ((UnresolvedReferenceTypeInfo) unresolvedTypeInfo)
                     .getAvailableNamespaces()) {
 
                 // OԖ.* ƂȂĂꍇ
                 if (availableNamespace.isAllClasses()) {
                     final String[] namespace = availableNamespace.getNamespace();
 
                     // OԂ̉ɂeNXɑ΂
                     for (ClassInfo classInfo : classInfoManager.getClassInfos(namespace)) {
                         final String className = classInfo.getClassName();
 
                         // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                         if (className.equals(referenceName[0])) {
 
                             // LbVpnbVe[uꍇ̓LbVǉ
                             if (null != resolvedCache) {
                                 resolvedCache.put(unresolvedTypeInfo, classInfo);
                             }
 
                             return classInfo;
                         }
                     }
 
                     // O.NX ƂȂĂꍇ
                 } else {
 
                     final String[] importName = availableNamespace.getImportName();
 
                     // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                     if (importName[importName.length - 1].equals(referenceName[0])) {
 
                         final String[] namespace = availableNamespace.getNamespace();
                         final String[] fullQualifiedName = new String[namespace.length
                                 + referenceName.length];
                         System.arraycopy(namespace, 0, fullQualifiedName, 0, namespace.length);
                         System.arraycopy(referenceName, 0, fullQualifiedName, namespace.length,
                                 referenceName.length);
                         final ClassInfo specifiedClassInfo = classInfoManager
                                 .getClassInfo(fullQualifiedName);
 
                         // LbVpnbVe[uꍇ̓LbVǉ
                         if (null != resolvedCache) {
                             resolvedCache.put(unresolvedTypeInfo, specifiedClassInfo);
                         }
 
                         // NXȂꍇ null Ԃ
                         return specifiedClassInfo;
                     }
                 }
             }
 
             // Ȃꍇ null Ԃ
             return null;
 
             // z^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedArrayTypeInfo) {
 
             final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo)
                     .getElementType();
             final int dimension = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo).getDimension();
 
             final TypeInfo elementType = NameResolver.resolveTypeInfo(unresolvedElementType,
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             if (elementType != null) {
 
                 final ArrayTypeInfo arrayType = ArrayTypeInfo.getType(elementType, dimension);
                 return arrayType;
             }
 
             // vf̌^sȂƂ null Ԃ
             return null;
 
             // NX̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedClassInfo) {
 
             final TypeInfo classInfo = (ClassInfo) ((UnresolvedClassInfo) unresolvedTypeInfo)
                     .getResolvedInfo();
             return classInfo;
 
             // tB[hgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedFieldUsage) {
 
             final TypeInfo classInfo = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // \bhĂяȍꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedMethodCall) {
 
             // (c)̃NX`擾
             final TypeInfo classInfo = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // GeBeBgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             final TypeInfo classInfo = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // zgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedArrayElementUsage) {
 
             final TypeInfo classInfo = NameResolver.resolveArrayElementUsage(
                     (UnresolvedArrayElementUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // ȊǑ^̏ꍇ̓G[
         } else {
             throw new IllegalArgumentException(unresolvedTypeInfo.toString()
                     + " is a wrong object!");
         }
     }
 
     /**
      * tB[hQƂCtB[hQƂsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param fieldReference tB[hQ
      * @param usingClass tB[hQƂsĂNX
      * @param usingMethod tB[hQƂsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[hQƂ̌^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveFieldReference(final UnresolvedFieldUsage fieldReference,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == fieldReference) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(fieldReference)) {
             final TypeInfo type = resolvedCache.get(fieldReference);
             return type;
         }
 
         // tB[h擾
         final String fieldName = fieldReference.getFieldName();
 
         // ě^
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldReference.getOwnerClassType();
         final TypeInfo fieldOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedFieldOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == fieldOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldReference);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, null);
 
             return null;
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (fieldOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃtB[h猟
             {
                 // p\ȃtB[hꗗ擾
                 final List<TargetFieldInfo> availableFields = NameResolver.getAvailableFields(
                         (TargetClassInfo) fieldOwnerClassType, usingClass);
 
                 // p\ȃtB[hCtB[hŌ
                 for (TargetFieldInfo availableField : availableFields) {
 
                     // vtB[hꍇ
                     if (fieldName.equals(availableField.getName())) {
                         usingMethod.addReferencee(availableField);
                         availableField.addReferencer(usingMethod);
 
                         // ς݃LbVɓo^
                         resolvedCache.put(fieldReference, availableField.getType());
 
                         return availableField.getType();
                     }
                 }
             }
 
             // p\ȃtB[hȂꍇ́CONXłeNX͂
             // ̃NX̕ϐgpĂƂ݂Ȃ
             {
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) fieldOwnerClassType);
                 if (!(fieldOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                             externalSuperClass);
                     usingMethod.addReferencee(fieldInfo);
                     fieldInfo.addReferencer(usingMethod);
                     fieldInfoManager.add(fieldInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(fieldReference, null);
 
                     // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(fieldReference);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldReference, null);
 
                 return null;
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addReferencee(fieldInfo);
             fieldInfo.addReferencer(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, null);
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("resolveFieldReference2: Here shouldn't be reached!");
         return null;
     }
 
     /**
      * tB[hCtB[hsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param fieldAssignment tB[h
      * @param usingClass tB[hsĂNX
      * @param usingMethod tB[hsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[ȟ^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveFieldAssignment(final UnresolvedFieldUsage fieldAssignment,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == fieldAssignment) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(fieldAssignment)) {
             final TypeInfo type = resolvedCache.get(fieldAssignment);
             return type;
         }
 
         // tB[h擾
         final String fieldName = fieldAssignment.getFieldName();
 
         // ě^
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldAssignment
                 .getOwnerClassType();
         final TypeInfo fieldOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedFieldOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == fieldOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldAssignment);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, null);
 
             return null;
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (fieldOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃtB[h猟
             {
                 // p\ȃtB[hꗗ擾
                 final List<TargetFieldInfo> availableFields = NameResolver.getAvailableFields(
                         (TargetClassInfo) fieldOwnerClassType, usingClass);
 
                 // p\ȃtB[hꗗCtB[hŌ
                 for (TargetFieldInfo availableField : availableFields) {
 
                     // vtB[hꍇ
                     if (fieldName.equals(availableField.getName())) {
                         usingMethod.addAssignmentee(availableField);
                         availableField.addAssignmenter(usingMethod);
 
                         // ς݃LbVɂɓo^
                         resolvedCache.put(fieldAssignment, availableField.getType());
 
                         return availableField.getType();
                     }
                 }
             }
 
             // p\ȃtB[hȂꍇ́CONXłeNX͂D
             // ̃NX̕ϐgpĂƂ݂Ȃ
             {
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) fieldOwnerClassType);
                 if (!(fieldOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                             externalSuperClass);
                     usingMethod.addAssignmentee(fieldInfo);
                     fieldInfo.addAssignmenter(usingMethod);
                     fieldInfoManager.add(fieldInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(fieldAssignment, null);
 
                     // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(fieldAssignment);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldAssignment, null);
 
                 return null;
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addAssignmentee(fieldInfo);
             fieldInfo.addAssignmenter(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, null);
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("resolveFieldAssignment2: Here shouldn't be reached!");
         return null;
     }
 
     /**
      * \bhĂяoC\bhĂяosĂ郁\bhɓo^D܂C\bh̕Ԃľ^ԂD
      * 
      * @param methodCall \bhĂяo
      * @param usingClass \bhĂяosĂNX
      * @param usingMethod \bhĂяosĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return \bhĂяoɑΉ MethodInfo
      */
     public static TypeInfo resolveMethodCall(final UnresolvedMethodCall methodCall,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == methodCall) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == methodInfoManager)
                 || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(methodCall)) {
             final TypeInfo type = resolvedCache.get(methodCall);
             return type;
         }
 
         // \bh̃VOl`擾
         final String methodName = methodCall.getMethodName();
         final boolean constructor = methodCall.isConstructor();
         final List<UnresolvedTypeInfo> unresolvedParameterTypes = methodCall.getParameterTypes();
 
         // \bh̖
         final List<TypeInfo> parameterTypes = new LinkedList<TypeInfo>();
         for (UnresolvedTypeInfo unresolvedParameterType : unresolvedParameterTypes) {
 
             TypeInfo parameterType = NameResolver.resolveTypeInfo(unresolvedParameterType,
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
             if (null == parameterType) {
                 if (unresolvedParameterType instanceof UnresolvedReferenceTypeInfo) {
                     parameterType = NameResolver
                             .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedParameterType);
                     classInfoManager.add((ExternalClassInfo) parameterType);
                 } else if (unresolvedParameterType instanceof UnresolvedArrayTypeInfo) {
                     final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                             .getElementType();
                     final int dimension = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                             .getDimension();
                     final TypeInfo elementType = NameResolver
                             .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                     classInfoManager.add((ExternalClassInfo) elementType);
                     parameterType = ArrayTypeInfo.getType(elementType, dimension);
                 }
             }
             parameterTypes.add(parameterType);
         }
 
         // ě^
         final UnresolvedTypeInfo unresolvedMethodOwnerClassType = methodCall.getOwnerClassType();
         final TypeInfo methodOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedMethodOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == methodOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(methodCall);
 
             // ς݃LbVɓo^
             resolvedCache.put(methodCall, null);
 
             return null;
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (methodOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃ\bh猟
             {
                 // p\ȃ\bhꗗ擾
                 final List<TargetMethodInfo> availableMethods = NameResolver.getAvailableMethods(
                         (TargetClassInfo) methodOwnerClassType, usingClass);
 
                 // p\ȃ\bhC\bhƈv̂
                 // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                 for (TargetMethodInfo availableMethod : availableMethods) {
 
                     // Ăяo\ȃ\bhꍇ
                     if (availableMethod.canCalledWith(methodName, parameterTypes)) {
                         usingMethod.addCallee(availableMethod);
                         availableMethod.addCaller(usingMethod);
 
                         // ς݃LbVɂɓo^
                         resolvedCache.put(methodCall, availableMethod.getReturnType());
 
                         return availableMethod.getReturnType();
                     }
                 }
             }
 
             // p\ȃ\bhȂꍇ́CONXłeNX͂D
             // ̃NX̃\bhgpĂƂ݂Ȃ
             {
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) methodOwnerClassType);
                 if (!(methodOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                             externalSuperClass, constructor);
                     final List<ParameterInfo> parameters = NameResolver
                             .createParameters(parameterTypes);
                     methodInfo.addParameters(parameters);
 
                     usingMethod.addCallee(methodInfo);
                     methodInfo.addCaller(usingMethod);
                     methodInfoManager.add(methodInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(methodCall, null);
 
                     // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(methodCall);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(methodCall, null);
 
                 return null;
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (methodOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                     (ExternalClassInfo) methodOwnerClassType, constructor);
             final List<ParameterInfo> parameters = NameResolver.createParameters(parameterTypes);
             methodInfo.addParameters(parameters);
 
             usingMethod.addCallee(methodInfo);
             methodInfo.addCaller(usingMethod);
             methodInfoManager.add(methodInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(methodCall, null);
 
             // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("resolveMethodCall3: Here shouldn't be reached!");
         return null;
     }
 
     /**
      * z^tB[h̗vfgpCz^tB[h̗vfgpsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param arrayElement z^tB[h̗vfgp
      * @param usingClass tB[hsĂNX
      * @param usingMethod tB[hsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[ȟ^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveArrayElementUsage(final UnresolvedArrayElementUsage arrayElement,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == arrayElement) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(arrayElement)) {
             final TypeInfo type = resolvedCache.get(arrayElement);
             return type;
         }
 
         // vfgpĂ関`^擾
         final UnresolvedTypeInfo unresolvedOwnerArrayType = arrayElement.getOwnerArrayType();
         final TypeInfo ownerArrayType = NameResolver.resolveTypeInfo(unresolvedOwnerArrayType,
                 usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                 resolvedCache);
 
         // ς݃LbVɓo^
         resolvedCache.put(arrayElement, ownerArrayType);
 
         return ownerArrayType;
     }
 
     /**
      * GeBeBgpCGeBeBgpsĂ郁\bhɓo^D܂CGeBeB̉ς݌^ԂD
      * 
      * @param entityUsage GeBeBgp
      * @param usingClass \bhĂяosĂNX
      * @param usingMethod \bhĂяosĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return \bhĂяoɑΉ MethodInfo
      */
     public static TypeInfo resolveEntityUsage(final UnresolvedEntityUsage entityUsage,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == entityUsage) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == methodInfoManager)
                 || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(entityUsage)) {
             final TypeInfo type = resolvedCache.get(entityUsage);
             return type;
         }
 
         // GeBeBQƖ擾
         final String[] name = entityUsage.getName();
 
         // p\ȃtB[hGeBeB
         {
             // ̃NXŗp\ȃtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = NameResolver
                     .getAvailableFields(usingClass);
 
             for (TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     usingMethod.addReferencee(availableFieldOfThisClass);
                     availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     TypeInfo ownerTypeInfo = availableFieldOfThisClass.getType();
                     for (int i = 1; i < name.length; i++) {
 
                         // e null Cǂ悤Ȃ
                         if (null == ownerTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, null);
 
                             return ownerTypeInfo;
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // ܂͗p\ȃtB[hꗗ擾
                             boolean found = false;
                             {
                                 // p\ȃtB[hꗗ擾
                                 final List<TargetFieldInfo> availableFields = NameResolver
                                         .getAvailableFields((TargetClassInfo) ownerTypeInfo,
                                                 usingClass);
 
                                 for (TargetFieldInfo availableField : availableFields) {
 
                                     // vtB[hꍇ
                                     if (name[i].equals(availableField.getName())) {
                                         usingMethod.addReferencee(availableField);
                                         availableField.addReferencer(usingMethod);
 
                                         ownerTypeInfo = availableField.getType();
                                         found = true;
                                     }
                                 }
                             }
 
                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                             {
                                 if (!found) {
 
                                     final ExternalClassInfo externalSuperClass = NameResolver
                                             .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                     if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                             && (null != externalSuperClass)) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], externalSuperClass);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = null;
                                     }
 
                                     // Ȃs
                                     usingMethod.addUnresolvedUsage(entityUsage);
 
                                     // ς݃LbVɓo^
                                     resolvedCache.put(entityUsage, null);
 
                                     return null;
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalClassInfo externalSuperClass = NameResolver
                                     .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                             if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                     && (null != externalSuperClass)) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         externalSuperClass);
 
                                 usingMethod.addReferencee(fieldInfo);
                                 fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 ownerTypeInfo = null;
                             }
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
 
                     return ownerTypeInfo;
                 }
             }
         }
 
         // p\ȃNXGeBeB
         {
 
             for (int length = 1; length <= name.length; length++) {
 
                 // 閼O(String[])쐬
                 final String[] searchingName = new String[length];
                 System.arraycopy(name, 0, searchingName, 0, length);
 
                 final ClassInfo searchingClass = classInfoManager.getClassInfo(searchingName);
                 if (null != searchingClass) {
 
                     TypeInfo ownerTypeInfo = searchingClass;
                     for (int i = length; i < name.length; i++) {
 
                         // e null Cǂ悤Ȃ
                         if (null == ownerTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, null);
 
                             return ownerTypeInfo;
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // ܂͗p\ȃtB[hꗗ擾
                             boolean found = false;
                             {
                                 // p\ȃtB[hꗗ擾
                                 final List<TargetFieldInfo> availableFields = NameResolver
                                         .getAvailableFields((TargetClassInfo) ownerTypeInfo,
                                                 usingClass);
 
                                 for (TargetFieldInfo availableField : availableFields) {
 
                                     // vtB[hꍇ
                                     if (name[i].equals(availableField.getName())) {
                                         usingMethod.addReferencee(availableField);
                                         availableField.addReferencer(usingMethod);
 
                                         ownerTypeInfo = availableField.getType();
                                         found = true;
                                     }
                                 }
                             }
 
                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                             {
                                 if (!found) {
 
                                     final ExternalClassInfo externalSuperClass = NameResolver
                                             .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                     if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                             && (null != externalSuperClass)) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], externalSuperClass);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = null;
                                     }
 
                                     // Ȃs
                                     usingMethod.addUnresolvedUsage(entityUsage);
 
                                     // ς݃LbVɓo^
                                     resolvedCache.put(entityUsage, null);
 
                                     return null;
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalClassInfo externalSuperClass = NameResolver
                                     .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                             if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                     && (null != externalSuperClass)) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         externalSuperClass);
 
                                 usingMethod.addReferencee(fieldInfo);
                                 fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 ownerTypeInfo = null;
                             }
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
 
                     return ownerTypeInfo;
                 }
             }
         }
 
         // Ȃs
         usingMethod.addUnresolvedUsage(entityUsage);
 
         // ς݃LbVɓo^
         resolvedCache.put(entityUsage, null);
 
         return null;
     }
 
     /**
      * ŗ^ꂽ^\ς݌^NX𐶐D ňƂė^̂́C\[XR[hp[XĂȂ^ł̂ŁCς݌^NX
      * ExternalClassInfo ƂȂD
      * 
      * @param unresolvedReferenceType ^
      * @return ς݌^
      */
     public static ExternalClassInfo createExternalClassInfo(
             final UnresolvedReferenceTypeInfo unresolvedReferenceType) {
 
         if (null == unresolvedReferenceType) {
             throw new NullPointerException();
         }
 
         // NX̎QƖ擾
         final String[] referenceName = unresolvedReferenceType.getReferenceName();
 
         // p\ȖOԂCNX̊S薼
         for (AvailableNamespaceInfo availableNamespace : unresolvedReferenceType
                 .getAvailableNamespaces()) {
 
             // OԖ.* ƂȂĂꍇ́C邱ƂłȂ
             if (availableNamespace.isAllClasses()) {
                 continue;
 
                 // O.NX ƂȂĂꍇ
             } else {
 
                 final String[] importName = availableNamespace.getImportName();
 
                 // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                 if (importName[importName.length - 1].equals(referenceName[0])) {
 
                     final String[] namespace = availableNamespace.getNamespace();
                     final String[] fullQualifiedName = new String[namespace.length
                             + referenceName.length];
                     System.arraycopy(namespace, 0, fullQualifiedName, 0, namespace.length);
                     System.arraycopy(referenceName, 0, fullQualifiedName, namespace.length,
                             referenceName.length);
 
                     final ExternalClassInfo classInfo = new ExternalClassInfo(fullQualifiedName);
                     return classInfo;
                 }
             }
         }
 
         // Ȃꍇ́COԂ UNKNOWN  ONX쐬
         final ExternalClassInfo unknownClassInfo = new ExternalClassInfo(
                 referenceName[referenceName.length - 1]);
         return unknownClassInfo;
     }
 
     /**
      * ŗ^ꂽ^ List Op[^ List 쐬CԂ
      * 
      * @param types ^List
      * @return Op[^ List
      */
     public static List<ParameterInfo> createParameters(final List<TypeInfo> types) {
 
         if (null == types) {
             throw new NullPointerException();
         }
 
         final List<ParameterInfo> parameters = new LinkedList<ParameterInfo>();
         for (TypeInfo type : types) {
             final ExternalParameterInfo parameter = new ExternalParameterInfo(type);
             parameters.add(parameter);
         }
 
         return Collections.unmodifiableList(parameters);
     }
 
     /**
      * ŗ^ꂽNX̐eNXłCONX(ExternalClassInfo)ł̂ԂD NXKwIɍłʂɈʒuONXԂD
      * YNX݂Ȃꍇ́C null ԂD
      * 
      * @param classInfo ΏۃNX
      * @return ŗ^ꂽNX̐eNXłCNXKwIɍłʂɈʒuONX
      */
     private static ExternalClassInfo getExternalSuperClass(final TargetClassInfo classInfo) {
 
         for (ClassInfo superClassInfo : classInfo.getSuperClasses()) {
 
             if (superClassInfo instanceof ExternalClassInfo) {
                 return (ExternalClassInfo) superClassInfo;
             }
 
             NameResolver.getExternalSuperClass((TargetClassInfo) superClassInfo);
         }
 
         return null;
     }
 
     /**
      * ũ݂NXvŗp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́Cũ݂NXvŒ`ĂtB[hCyт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD
      * p\ȃtB[h List Ɋi[ĂD Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param thisClass ݂̃NX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo thisClass) {
 
         if (null == thisClass) {
             throw new NullPointerException();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂtB[hꗗ擾
         availableFields.addAll(thisClass.getDefinedFields());
 
         // eNXŒ`ĂC̃NXANZX\ȃtB[h擾
         for (ClassInfo superClass : thisClass.getSuperClasses()) {
 
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClass);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ũ݂NXvŗp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́Cũ݂NXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * p\ȃ\bh List Ɋi[ĂD Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param thisClass ݂̃NX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo thisClass) {
 
         if (null == thisClass) {
             throw new NullPointerException();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`Ă郁\bhꗗ擾
         availableMethods.addAll(thisClass.getDefinedMethods());
 
         // eNXŒ`ĂC̃NXANZX\ȃ\bh擾
         for (ClassInfo superClass : thisClass.getSuperClasses()) {
 
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClass);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́CugpNXvŒ`ĂtB[hCyт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃtB[h擾D qNXŗp\ȃtB[hꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂtB[ĥCgpNXŗp\ȃtB[h擾
         // 2̃NXOԂĂꍇ
         if (usedClass.getNamespace().equals(usingClass.getNamespace())) {
 
             for (TargetFieldInfo field : usedClass.getDefinedFields()) {
                 if (field.isNamespaceVisible()) {
                     availableFields.add(field);
                 }
             }
 
             // ႤOԂĂꍇ
         } else {
             for (TargetFieldInfo field : usedClass.getDefinedFields()) {
                 if (field.isPublicVisible()) {
                     availableFields.add(field);
                 }
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃtB[h擾
         // List ɓ̂ŁCeNX̃tB[ȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClassInfo);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́CugpNXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃ\bh擾D qNXŗp\ȃ\bhꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`Ă郁\bĥCgpNXŗp\ȃ\bh擾
         // 2̃NXOԂĂꍇ
         if (usedClass.getNamespace().equals(usingClass.getNamespace())) {
 
             for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
                 if (method.isNamespaceVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             // ႤOԂĂꍇ
         } else {
             for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
                 if (method.isPublicVisible()) {
                     availableMethods.add(method);
                 }
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃ\bh擾
         // List ɓ̂ŁCeNX̃\bȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClassInfo);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ugpNXv̎qNXgpꍇɁCp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́CugpNXv͂̐eNXŒ`ĂtB[ĥCqNXANZX\ȃtB[hłD
      * qNXŗp\ȃtB[hꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param usedClass gpNX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFieldsInSubClasses(
             final TargetClassInfo usedClass) {
 
         if (null == usedClass) {
             throw new NullPointerException();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂCqNXANZX\ȃtB[h擾
         for (TargetFieldInfo field : usedClass.getDefinedFields()) {
             if (field.isInheritanceVisible()) {
                 availableFields.add(field);
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃtB[h擾
         // List ɓ̂ŁCeNX̃tB[ȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClassInfo);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ugpNXv̎qNXgpꍇɁCp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́Cugp郁\bhv͂̐eNXŒ`Ă郁\bĥCqNXANZX\ȃ\bhłD
      * qNXŗp\ȃ\bhꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param usedClass gpNX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethodsInSubClasses(
             final TargetClassInfo usedClass) {
 
         if (null == usedClass) {
             throw new NullPointerException();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`ĂCqNXANZX\ȃ\bh擾
         for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
             if (method.isInheritanceVisible()) {
                 availableMethods.add(method);
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃ\bh擾
         // List ɓ̂ŁCeNX̃\bȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClassInfo);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * o̓bZ[Wo͗p̃v^
      */
     private static final MessagePrinter out = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "NameResolver";
         }
     }, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "NameResolver";
         }
     }, MESSAGE_TYPE.ERROR);
 }
