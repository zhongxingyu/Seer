 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * \bhĂяoCRXgN^Ăяőʂ̐eNX
  * 
  * @author higo
  *
  */
 public abstract class CallInfo<T extends CallableUnitInfo> extends ExpressionInfo {
 
     /**
      * @param callee Ă΂ĂIuWFNgČĂяoCz̃RXgN^̏ꍇnullĂD
      * @param ownerMethod I[i[\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     CallInfo(final T callee, final CallableUnitInfo ownerMethod, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(ownerMethod, fromLine, fromColumn, toLine, toColumn);
 
         this.arguments = new LinkedList<ExpressionInfo>();
         this.typeArguments = new LinkedList<ReferenceTypeInfo>();
 
         this.callee = callee;
 
         // \bhĂяo֌W\z
        this.callee.addCaller(ownerMethod);
     }
 
     /**
      * ̃\bhĂяo̎ǉDvOC͌ĂяoȂD
      * 
      * @param argument ǉ
      */
     public final void addArgument(final ExpressionInfo argument) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == argument) {
             throw new NullPointerException();
         }
 
         this.arguments.add(argument);
         argument.setOwnerExecutableElement(this);
     }
 
     /**
      * ̌Ăяo̎ǉDvOC͌ĂяoȂD
      * 
      * @param arguments ǉ
      */
     public final void addArguments(final List<ExpressionInfo> arguments) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == arguments) {
             throw new NullPointerException();
         }
 
         this.arguments.addAll(arguments);
 
         for (final ExpressionInfo argument : arguments) {
             argument.setOwnerExecutableElement(this);
         }
     }
 
     /**
      * ̃\bhĂяǒ^ǉDvOC͌ĂяoȂ
      * 
      * @param typeArgument ǉ^
      */
     public final void addTypeArgument(final ReferenceTypeInfo typeArgument) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeArgument) {
             throw new NullPointerException();
         }
 
         this.typeArguments.add(typeArgument);
     }
 
     /**
      * ̌Ăяǒ^ǉDvOC͌ĂяoȂD
      * 
      * @param typeArguments ǉ^
      */
     public final void addTypeArguments(final List<ReferenceTypeInfo> typeArguments) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeArguments) {
             throw new NullPointerException();
         }
 
         this.typeArguments.addAll(typeArguments);
     }
 
     /**
      * ̌Ăяo̎ListԂD
      * 
      * @return@̌Ăяo̎List
      */
     public List<ExpressionInfo> getArguments() {
         return Collections.unmodifiableList(this.arguments);
     }
 
     /**
      * ̌ĂяoɂϐgpQԂ
      * 
      * @return ̌ĂяoɂϐgpQ
      */
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         final SortedSet<VariableUsageInfo<?>> variableUsages = new TreeSet<VariableUsageInfo<?>>();
         for (final ExpressionInfo parameter : this.getArguments()) {
             variableUsages.addAll(parameter.getVariableUsages());
         }
         return Collections.unmodifiableSortedSet(variableUsages);
     }
 
     /**
      * ̌ĂяoŌĂяoĂ̂Ԃ
      * 
      * @return ̌ĂяoŌĂяoĂ
      */
     public final T getCallee() {
         return this.callee;
     }
 
     private final T callee;
 
     private final List<ExpressionInfo> arguments;
 
     private final List<ReferenceTypeInfo> typeArguments;
 }
