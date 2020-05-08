 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * throw̏ۗLNX
  * 
  * @author t-miyake
  *
  */
 @SuppressWarnings("serial")
 public class ThrowStatementInfo extends SingleStatementInfo {
 
     /**
      * throwɂēO\ƈʒu^ď
      * 
      * @param ownerSpace 𒼐ڏL
      * @param thrownEpression throwɂēO\
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public ThrowStatementInfo(final LocalSpaceInfo ownerSpace, ExpressionInfo thrownEpression,
             int fromLine, int fromColumn, int toLine, int toColumn) {
         super(ownerSpace, fromLine, fromColumn, toLine, toColumn);
 
         if (null == thrownEpression) {
             throw new IllegalArgumentException("thrownExpression is null");
         }
         this.thrownEpression = thrownEpression;
 
         this.thrownEpression.setOwnerExecutableElement(this);
     }
 
     /**
      * throwɂēO\Ԃ
      * 
      * @return throwɂēO\
      */
     public final ExpressionInfo getThrownExpression() {
         return this.thrownEpression;
     }
 
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         return this.getThrownExpression().getVariableUsages();
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
         return this.getThrownExpression().getCalls();
     }
 
     /**
      * throw̃eLXg\i^jԂ
      * 
      * @return throw̃eLXg\i^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("throw ");
 
         final ExpressionInfo expression = this.getThrownExpression();
         sb.append(expression.getText());
 
         sb.append(";");
 
         return sb.toString();
     }
 
     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public Set<ClassTypeInfo> getThrownExceptions() {
         final Set<ClassTypeInfo> thrownExpressions = new HashSet<ClassTypeInfo>();
        if(this.getThrownExpression().getType() instanceof ClassTypeInfo){
            thrownExpressions.add((ClassTypeInfo) this.getThrownExpression().getType());
        }
         return Collections.unmodifiableSet(thrownExpressions);
     }
 
     /**
      * throwɂēO\
      */
     private final ExpressionInfo thrownEpression;
 
 }
