 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ForeachBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 public class UnresolvedForeachBlockInfo extends UnresolvedBlockInfo<ForeachBlockInfo> {
 
     /**
      * ÕubN^āCforeach ubN
      * 
      * @param outerSpace ÕubN
      */
     public UnresolvedForeachBlockInfo(final UnresolvedLocalSpaceInfo<?> outerSpace) {
         super(outerSpace);
     }
 
     /**
      * ̖ for ubN
      * 
      * @param usingClass NX
      * @param usingMethod \bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      */
     @Override
     public ForeachBlockInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         //  foreacḧʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
        
         // JԂp̎擾
         final UnresolvedExpressionInfo<?> unresolvedIteratorExpression = this
                 .getIteratorExpression();
         final ExpressionInfo iteratorExpression = unresolvedIteratorExpression.resolve(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         // ŐԂ擾
         final UnresolvedLocalSpaceInfo<?> unresolvedLocalSpace = this.getOuterSpace();
         final LocalSpaceInfo outerSpace = unresolvedLocalSpace.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
 
         this.resolvedInfo = new ForeachBlockInfo(usingClass, outerSpace, fromLine, fromColumn,
                 toLine, toColumn, iteratorExpression);
 
         // JԂp̕ϐ擾
         final UnresolvedLocalVariableInfo unresolvedIteratorVariable = this.getIteratorVariable();
         final LocalVariableInfo iteratorVariable = unresolvedIteratorVariable.resolve(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
         this.resolvedInfo.setIteratorVariable(iteratorVariable);
 
        // ubNCς݃IuWFNgɒǉ
        this.resolveInnerBlock(usingClass, usingMethod, classInfoManager, fieldInfoManager,
                methodInfoManager);
        
         return this.resolvedInfo;
     }
 
     /**
      * ϐ`ݒ肷
      * 
      * @param iteraotorVariableDeclaration ϐ`
      */
     public void setIteratorVariable(final UnresolvedLocalVariableInfo iteraotorVariable) {
         this.iteratorVariable = iteraotorVariable;
     }
 
     /**
      * JԂp̎ݒ肷
      * 
      * @param iteratorExpression JԂp̎
      */
     public void setIteratorExpression(final UnresolvedExpressionInfo<?> iteratorExpression) {
         this.iteratorExpression = iteratorExpression;
     }
 
     /**
      * ϐ`Ԃ
      * 
      * @return ϐ`
      */
     public UnresolvedLocalVariableInfo getIteratorVariable() {
         return this.iteratorVariable;
     }
 
     /**
      * JԂp̎Ԃ
      * 
      * @return JԂp̎
      */
     public UnresolvedExpressionInfo<?> getIteratorExpression() {
         return this.iteratorExpression;
     }
 
     private UnresolvedLocalVariableInfo iteratorVariable;
 
     private UnresolvedExpressionInfo<?> iteratorExpression;
 }
