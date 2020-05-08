 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.BlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.StatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * xڂASTp[XŎ擾\bhꎞIɊi[邽߂̃NXD
  * 
  * 
  * @author higo
  * 
  */
 public final class UnresolvedMethodInfo extends UnresolvedCallableUnitInfo<TargetMethodInfo>
         implements MemberSetting {
 
     /**
      * \bh`IuWFNg
      * @param ownerClass ̃\bh錾ĂNX
      */
     public UnresolvedMethodInfo(final UnresolvedClassInfo ownerClass) {
         super(ownerClass);
 
         this.methodName = null;
         this.returnType = null;
         this.instance = true;
         this.resolvedInfo = null;
     }
 
     /**
      * ̖\bh񂪉Ă邩ǂԂ
      * 
      * @return ς݂̏ꍇ trueCłȂꍇ false
      */
     @Override
     public boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     /**
      * ς݃\bhԂ
      * 
      * @return ς݃\bh
      * @throws ܂ĂȂꍇɃX[
      */
     @Override
     public TargetMethodInfo getResolved() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * \bhCςݎQƂԂD
      * 
      * @param usingClass \bh̒`NX
      * @param usingMethod \bh̒`郁\bhĩ\bhĂ΂ꍇ͒ʏ null ZbgĂ͂j
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @return ς݃\bh
      */
     @Override
     public TargetMethodInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == classInfoManager) || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         // CqCOCԂlCsCRXgN^ǂCCCX^Xo[ǂ擾
         final Set<ModifierInfo> methodModifiers = this.getModifiers();
         final String methodName = this.getMethodName();
 
         final boolean privateVisible = this.isPrivateVisible();
         final boolean namespaceVisible = this.isNamespaceVisible();
         final boolean inheritanceVisible = this.isInheritanceVisible();
         final boolean publicVisible = this.isPublicVisible();
         final boolean instance = this.isInstanceMember();
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // MethodInfo IuWFNg𐶐D
         this.resolvedInfo = new TargetMethodInfo(methodModifiers, methodName, usingClass,
                 privateVisible, namespaceVisible, inheritanceVisible, publicVisible, instance,
                 fromLine, fromColumn, toLine, toColumn);
 
         // ^p[^Cς݃\bhɒǉ
         for (final UnresolvedTypeParameterInfo unresolvedTypeParameter : this.getTypeParameters()) {
 
             final TypeParameterInfo typeParameter = (TypeParameterInfo) unresolvedTypeParameter
                     .resolve(usingClass, this.resolvedInfo, classInfoManager, fieldInfoManager,
                             methodInfoManager);
             this.resolvedInfo.addTypeParameter(typeParameter);
         }
 
         // ԂlZbg
         final UnresolvedTypeInfo unresolvedMethodReturnType = this.getReturnType();
         TypeInfo methodReturnType = unresolvedMethodReturnType.resolve(usingClass, null,
                 classInfoManager, fieldInfoManager, methodInfoManager);
         assert methodReturnType != null : "resolveTypeInfo returned null!";
         if (methodReturnType instanceof UnknownTypeInfo) {
             if (unresolvedMethodReturnType instanceof UnresolvedClassReferenceInfo) {
 
                 final ExternalClassInfo classInfo = NameResolver
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedMethodReturnType);
                 methodReturnType = new ClassTypeInfo(classInfo);
                 for (final UnresolvedTypeInfo unresolvedTypeArgument : ((UnresolvedClassReferenceInfo) unresolvedMethodReturnType)
                         .getTypeArguments()) {
                     final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                             usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                     ((ClassTypeInfo) methodReturnType).addTypeArgument(typeArgument);
                 }
                 classInfoManager.add(classInfo);
 
             } else if (unresolvedMethodReturnType instanceof UnresolvedArrayTypeInfo) {
 
                 // TODO ^p[^̏i[
                 final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedMethodReturnType)
                         .getElementType();
                 final int dimension = ((UnresolvedArrayTypeInfo) unresolvedMethodReturnType)
                         .getDimension();
                final TypeInfo elementType = unresolvedElementType.resolve(usingClass,
                        usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                 methodReturnType = ArrayTypeInfo.getType(elementType, dimension);
 
             } else {
                 assert false : "Can't resolve method return type : "
                         + unresolvedMethodReturnType.toString();
             }
         }
         this.resolvedInfo.setReturnType(methodReturnType);
 
         // ǉ
         for (final UnresolvedParameterInfo unresolvedParameterInfo : this.getParameters()) {
 
            final TargetParameterInfo parameterInfo = unresolvedParameterInfo.resolve(
                    usingClass, this.resolvedInfo, classInfoManager, fieldInfoManager,
                    methodInfoManager);
             this.resolvedInfo.addParameter(parameterInfo);
         }
 
         //@ubNCςcaseGgIuWFNgɒǉ
         for (final UnresolvedStatementInfo<?> unresolvedStatement : this.getStatements()) {
            final StatementInfo statement = unresolvedStatement.resolve(usingClass, usingMethod,
                    classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addStatement(statement);
        }          
 
         // \bhŒ`Ăe[Jϐɑ΂
         for (final UnresolvedLocalVariableInfo unresolvedLocalVariable : this.getLocalVariables()) {
 
             final LocalVariableInfo localVariable = unresolvedLocalVariable.resolve(usingClass,
                     this.resolvedInfo, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addLocalVariable(localVariable);
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * \bhԂ
      * 
      * @return \bh
      */
     public String getMethodName() {
         return this.methodName;
     }
 
     /**
      * \bhZbg
      * 
      * @param methodName \bh
      */
     public void setMethodName(final String methodName) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodName) {
             throw new NullPointerException();
         }
 
         this.methodName = methodName;
     }
 
     /**
      * \bh̕Ԃľ^Ԃ
      * 
      * @return \bh̕Ԃľ^
      */
     public UnresolvedTypeInfo getReturnType() {
         return this.returnType;
     }
 
     /**
      * \bh̕ԂlZbg
      * 
      * @param returnType \bh̕Ԃl
      */
     public void setReturnType(final UnresolvedTypeInfo returnType) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == returnType) {
             throw new NullPointerException();
         }
 
         this.returnType = returnType;
     }
 
     /**
      * CX^Xo[ǂԂ
      * 
      * @return CX^Xo[̏ꍇ trueCłȂꍇ false
      */
     public boolean isInstanceMember() {
         return this.instance;
     }
 
     /**
      * X^eBbNo[ǂԂ
      * 
      * @return X^eBbNo[̏ꍇ trueCłȂꍇ false
      */
     public boolean isStaticMember() {
         return !this.instance;
     }
 
     /**
      * CX^Xo[ǂZbg
      * 
      * @param instance CX^Xo[̏ꍇ trueC X^eBbNo[̏ꍇ false
      */
     public void setInstanceMember(final boolean instance) {
         this.instance = instance;
     }
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     private String methodName;
 
     /**
      * \bh̕Ԃlۑ邽߂̕ϐ
      */
     private UnresolvedTypeInfo returnType;
 
     /**
      * CX^Xo[ǂۑ邽߂̕ϐ
      */
     private boolean instance;
 
     /**
      * Oꂽi[邽߂̕ϐ
      */
     private TargetMethodInfo resolvedInfo;
 }
