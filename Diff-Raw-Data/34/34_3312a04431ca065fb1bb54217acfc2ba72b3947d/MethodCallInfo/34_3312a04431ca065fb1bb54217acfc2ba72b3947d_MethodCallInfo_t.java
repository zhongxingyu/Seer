 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;

 
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
 
        //@^p[^̏ꍇ
         final ClassTypeInfo callOwnerType = (ClassTypeInfo) this.getOwnerType();
        final List<TypeInfo> typeArguments = callOwnerType.getTypeArguments();

        // ^ꍇ́Č^Ԃ
        if (0 < typeArguments.size()) {
            final int typeParameterIndex = ((TypeParameterInfo) definitionType).getIndex();
            final TypeInfo typeArgument = typeArguments.get(typeParameterIndex);
            return typeArgument;

            // ^Ȃꍇ́CȌ^Ԃ
        } else {

            // Java@̏ꍇ (^p[^1.5瓱ꂽ)
            if (Settings.getLanguage().equals(LANGUAGE.JAVA15)) {
                final ClassInfo referencedClass = ClassInfoManager.getInstance().getClassInfo(
                        new String[] { "java", "lang", "Object" });
                final TypeInfo classType = new ClassTypeInfo(referencedClass);
                return classType;
            }
        }

        assert false : "Here shouldn't be reached!";
        return null;
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
