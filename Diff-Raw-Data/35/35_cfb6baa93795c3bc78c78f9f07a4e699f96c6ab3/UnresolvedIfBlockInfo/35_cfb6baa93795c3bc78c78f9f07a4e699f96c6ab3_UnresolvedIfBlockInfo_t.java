 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalClauseInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ElseBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.IfBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.StatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ifubN\NX
  * 
  * @author higo
  * 
  */
 public final class UnresolvedIfBlockInfo extends UnresolvedConditionalBlockInfo<IfBlockInfo> {
 
     /**
      * if ubN
      */
     public UnresolvedIfBlockInfo(final UnresolvedLocalSpaceInfo<?> outerSpace) {
         super(outerSpace);
 
         this.sequentElseBlock = null;
     }
 
     /**
      * ̖ if ubN
      * 
      * @param usingClass NX
      * @param usingMethod \bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManger p郁\bh}l[W
      */
     @Override
     public IfBlockInfo resolve(final TargetClassInfo usingClass,
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
 
         //  if ̏߂
         final UnresolvedConditionalClauseInfo unresolvedConditionalClause = this
                 .getConditionalClause();
         final ConditionalClauseInfo conditionalClause = unresolvedConditionalClause.resolve(
                 usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         //  if̈ʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         final LocalSpaceInfo outerSpace = this.getOuterSpace().getResolved();
 
         this.resolvedInfo = new IfBlockInfo(usingClass, usingMethod, conditionalClause, outerSpace,
                 fromLine, fromColumn, toLine, toColumn);
 
         // elseubNꍇ͉
         if (this.hasElseBlock()) {
             final UnresolvedElseBlockInfo unresolvedElseBlockInfo = this.getSequentElseBlock();
             final ElseBlockInfo sequentBlockInfo = unresolvedElseBlockInfo.resolve(usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.setSequentElseBlock(sequentBlockInfo);
         }
 
         //@ubNCςifubNIuWFNgɒǉ
         for (final UnresolvedStatementInfo<?> unresolvedStatement : this.getStatements()) {
             final StatementInfo statement = unresolvedStatement.resolve(usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addStatement(statement);
         }        
 
         // [JϐCςifubNIuWFNgɒǉ
         for (final UnresolvedLocalVariableInfo unresolvedVariable : this.getLocalVariables()) {
             final LocalVariableInfo variable = unresolvedVariable.resolve(usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addLocalVariable(variable);
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * ΉelseubNԂ
      * @return ΉelseubNDΉelseubN݂Ȃꍇnull
      */
     public UnresolvedElseBlockInfo getSequentElseBlock() {
         return this.sequentElseBlock;
     }
 
     /**
      * ΉelseubNZbg
      * @param elseBlock ΉelseubN
      */
     public void setSequentElseBlock(UnresolvedElseBlockInfo elseBlock) {
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         if (null == elseBlock) {
             throw new IllegalArgumentException("elseBlock is null");
         }
 
         this.sequentElseBlock = elseBlock;
     }
 
     /**
      * ΉelseubN݂邩ǂ\
      * @return ΉelseubN݂Ȃtrue
      */
     public boolean hasElseBlock() {
         return null != this.sequentElseBlock;
     }
 
     /**
      * ΉelseubNۑϐ
      */
     private UnresolvedElseBlockInfo sequentElseBlock;
 
 }
