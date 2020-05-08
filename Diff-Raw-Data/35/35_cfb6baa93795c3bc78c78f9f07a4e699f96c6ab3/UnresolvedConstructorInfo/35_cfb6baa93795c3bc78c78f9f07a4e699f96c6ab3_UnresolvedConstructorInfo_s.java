 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.StatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 public final class UnresolvedConstructorInfo extends
         UnresolvedCallableUnitInfo<TargetConstructorInfo> {
 
     public UnresolvedConstructorInfo(final UnresolvedClassInfo ownerClass) {
 
         super(ownerClass);
     }
 
     @Override
     public boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     @Override
     public TargetConstructorInfo getResolved() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     @Override
     public TargetConstructorInfo resolve(final TargetClassInfo usingClass,
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
 
         // CqCOCԂlCsC擾
         final Set<ModifierInfo> methodModifiers = this.getModifiers();
         final boolean privateVisible = this.isPrivateVisible();
         final boolean namespaceVisible = this.isNamespaceVisible();
         final boolean inheritanceVisible = this.isInheritanceVisible();
         final boolean publicVisible = this.isPublicVisible();
 
         final int constructorFromLine = this.getFromLine();
         final int constructorFromColumn = this.getFromColumn();
         final int constructorToLine = this.getToLine();
         final int constructorToColumn = this.getToColumn();
 
         // MethodInfo IuWFNg𐶐D
         this.resolvedInfo = new TargetConstructorInfo(methodModifiers, usingClass, privateVisible,
                 namespaceVisible, inheritanceVisible, publicVisible, constructorFromLine,
                 constructorFromColumn, constructorToLine, constructorToColumn);
 
         // ^p[^Cς݃RXgN^ɒǉ
         for (final UnresolvedTypeParameterInfo unresolvedTypeParameter : this.getTypeParameters()) {
 
             final TypeParameterInfo typeParameter = (TypeParameterInfo) unresolvedTypeParameter
                     .resolve(usingClass, this.resolvedInfo, classInfoManager, null, null);
             this.resolvedInfo.addTypeParameter(typeParameter);
         }
 
         // Cς݃RXgN^ɒǉ
         for (final UnresolvedParameterInfo unresolvedParameterInfo : this.getParameters()) {
 
             final TargetParameterInfo parameterInfo = unresolvedParameterInfo.resolve(usingClass,
                     this.resolvedInfo, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addParameter(parameterInfo);
         }
 
         //@ubNCς݃RXgN^IuWFNgɒǉ
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
 
     public boolean isInstanceMember() {
         return true;
     }
 
     public boolean isStaticMember() {
         return false;
     }
 
     public void setInstanceMember(boolean instance) {
     }
 
     /**
      * Oꂽi[邽߂̕ϐ
      */
     private TargetConstructorInfo resolvedInfo;
 }
