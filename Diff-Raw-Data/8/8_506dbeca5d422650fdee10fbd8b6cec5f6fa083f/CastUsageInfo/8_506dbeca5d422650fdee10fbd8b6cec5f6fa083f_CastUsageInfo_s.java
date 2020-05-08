 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * LXg̎gp\NX
  * 
  * @author higo
  *
  */
 public final class CastUsageInfo extends EntityUsageInfo {
 
     /**
      * Kvȏ^ăIuWFNg
      * 
      * @param castType LXǧ^
      * @param castedUsage LXgvf
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public CastUsageInfo(final TypeInfo castType, final ExpressionInfo castedUsage,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == castType || null == castedUsage) {
             throw new IllegalArgumentException();
         }
 
         this.castType = castType;
         this.castedUsage = castedUsage;
     }
 
     /**
      * ̃LXǧ^Ԃ
      * 
      * @return ̃LXǧ^
      */
     @Override
     public TypeInfo getType() {
         return this.castType;
     }
 
     /**
      * LXgvfԂ
      * 
      * @return LXgvf
      */
     public ExpressionInfo getCastedUsage() {
         return this.castedUsage;
     }
 
     /**
      * ̎iLXggpjɂϐp̈ꗗԂ
      * 
      * @return ϐpSet
      */
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         return this.getCastedUsage().getVariableUsages();
     }
 
     /**
      * ̃LXggp̃eLXg\iString^jԂ
      * 
      * @return ̃LXggp̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("(");
 
        final ExpressionInfo expression = this.getCastedUsage();
        sb.append(expression.getText());
 
         sb.append(")");
 
         return sb.toString();
     }
 
     private final TypeInfo castType;
 
     private final ExpressionInfo castedUsage;
 }
