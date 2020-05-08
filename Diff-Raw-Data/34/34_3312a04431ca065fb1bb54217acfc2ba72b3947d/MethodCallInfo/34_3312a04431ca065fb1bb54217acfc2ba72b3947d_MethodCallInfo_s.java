 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 /**
  * \bhĂяo\NX
  * 
  * @author higo
  *
  */
 public final class MethodCallInfo extends CallInfo {
 
     /**
      * Ăяo郁\bh^ăIuWFNg
      * 
      * @param ownerType \bhĂяo̐ě^
      * @param ownerUsage \bhĂяo̐eGeBeB
      * @param callee ĂяoĂ郁\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public MethodCallInfo(final TypeInfo ownerType, final EntityUsageInfo ownerUsage,
             final MethodInfo callee, final int fromLine, final int fromColumn, final int toLine,
             final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
         if ((null == ownerType) || (null == callee) || (null == ownerUsage)) {
             throw new NullPointerException();
         }
 
         this.ownerType = ownerType;
         this.ownerUsage = ownerUsage;
         this.callee = callee;
     }
 
     /**
      * ̃\bhĂяǒ^Ԃ
      */
     @Override
     public TypeInfo getType() {
 
         final MethodInfo callee = this.getCallee();
         final TypeInfo definitionType = callee.getReturnType();
 
         // `̕Ԃl^p[^łȂ΂̂܂ܕԂ
         if (!(definitionType instanceof TypeParameterInfo)) {
             return definitionType;
         }
 
        //@^p[^̏ꍇ͌^Ԃ
        final int typeParameterIndex = ((TypeParameterInfo) definitionType).getIndex();
         final ClassTypeInfo callOwnerType = (ClassTypeInfo) this.getOwnerType();
        final TypeInfo typeArgument = callOwnerType.getTypeArgument(typeParameterIndex);
        return typeArgument;
     }
 
     /**
      * ̃\bhĂяoĂ^Ԃ
      * 
      * @return ̃\bhĂяoĂ^
      */
     public TypeInfo getOwnerType() {
         return this.ownerType;
     }
 
     /**
      * ̃\bhĂяoŌĂяoĂ郁\bhԂ
      * @return
      */
     public MethodInfo getCallee() {
         return this.callee;
     }
 
     /**
      * ̎i\bhĂяojɂϐp̈ꗗԂNX
      * 
      * @return ϐpSet
      */
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         final SortedSet<VariableUsageInfo<?>> variableUsages = new TreeSet<VariableUsageInfo<?>>(
                 super.getVariableUsages());
         variableUsages.addAll(this.ownerUsage.getVariableUsages());
         return Collections.unmodifiableSortedSet(variableUsages);
     }
 
     /**
      * ̃\bhĂяo̐eC܂肱̃\bhĂяoĂvfԂ
      * 
      * @return ̃\bhĂяo̐e
      */
     public final EntityUsageInfo getOwnerUsage() {
         return this.ownerUsage;
     }
 
     private final TypeInfo ownerType;
 
     private final MethodInfo callee;
 
     private final EntityUsageInfo ownerUsage;
 }
