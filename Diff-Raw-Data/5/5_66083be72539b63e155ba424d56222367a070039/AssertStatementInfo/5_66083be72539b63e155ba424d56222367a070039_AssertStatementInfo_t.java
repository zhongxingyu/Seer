 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 /**
  * assert\NX
  * 
  * @author t-miyakeChigo
  *
  */
 @SuppressWarnings("serial")
 public final class AssertStatementInfo extends SingleStatementInfo {
 
     /**
      * AT[g𐶐
      * 
      * @param ownerSpace ÕubN
      * @param assertedExpression ؎
      * @param messageExpression bZ[W
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public AssertStatementInfo(final LocalSpaceInfo ownerSpace,
             final ExpressionInfo assertedExpression, final ExpressionInfo messageExpression,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(ownerSpace, fromLine, fromColumn, toLine, toColumn);
 
         if (null == assertedExpression) {
             throw new IllegalArgumentException("assertedExpressoin is null.");
         }
 
         this.assertedExpression = assertedExpression;
         this.messageExpression = messageExpression;
 
         this.assertedExpression.setOwnerExecutableElement(this);
         if (null != this.messageExpression) {
             this.messageExpression.setOwnerExecutableElement(this);
         }
 
     }
 
     /**
      * ؎Ԃ
      * 
      * @return@؎
      */
     public final ExpressionInfo getAssertedExpression() {
         return this.assertedExpression;
     }
 
     /**
      * bZ[WԂ
      * 
      * @return@bZ[W
      */
     public final ExpressionInfo getMessageExpression() {
         return this.messageExpression;
     }
 
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         SortedSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> usages = new TreeSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>>();
         usages.addAll(this.assertedExpression.getVariableUsages());
         usages.addAll(this.messageExpression.getVariableUsages());
         return Collections.unmodifiableSet(usages);
     }
 
     /**
      * `ꂽϐSetԂ
      * 
      * @return `ꂽϐSet
      */
     @Override
     public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         return VariableInfo.EmptySet;
     }
 
     /**
      * ĂяoSetԂ
      * 
      * @return ĂяoSet
      */
     @Override
     public Set<CallInfo<?>> getCalls() {
         final Set<CallInfo<?>> calls = new HashSet<CallInfo<?>>();
         final ExpressionInfo assertedExpression = this.getAssertedExpression();
         calls.addAll(assertedExpression.getCalls());
         final ExpressionInfo messageExpression = this.getMessageExpression();
         calls.addAll(messageExpression.getCalls());
         return Collections.unmodifiableSet(calls);
     }
 
     /**
      * ̃AT[g̃eLXg\iString^jԂ
      * 
      * @return ̃AT[g̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         StringBuilder sb = new StringBuilder();
         sb.append("assert ");
 
         final ExpressionInfo expression = this.getAssertedExpression();
         sb.append(expression.getText());
 
        sb.append(" : ");

        final ExpressionInfo message = this.getMessageExpression();
        sb.append(message.getText());

         sb.append(";");
 
         return sb.toString();
     }
 
     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public Set<ReferenceTypeInfo> getThrownExceptions() {
         final Set<ReferenceTypeInfo> thrownExceptions = new HashSet<ReferenceTypeInfo>();
         thrownExceptions.addAll(this.getAssertedExpression().getThrownExceptions());
         thrownExceptions.addAll(this.getMessageExpression().getThrownExceptions());
         return Collections.unmodifiableSet(thrownExceptions);
     }
 
     private final ExpressionInfo assertedExpression;
 
     private final ExpressionInfo messageExpression;
 
 }
