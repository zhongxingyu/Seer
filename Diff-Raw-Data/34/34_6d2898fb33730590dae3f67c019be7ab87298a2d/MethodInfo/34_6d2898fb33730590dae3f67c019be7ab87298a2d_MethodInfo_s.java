 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 public abstract class MethodInfo implements Comparable<MethodInfo>, Resolved {
 
     /**
      * \bhIuWFNg
      * 
      * @param methodName \bh
      * @param Ԃľ^
      * @param \bh`ĂNX
      * @param RXgN^ǂ
      */
     public MethodInfo(final String methodName, final TypeInfo returnType,
             final ClassInfo ownerClass, final boolean constructor) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == methodName) || (null == returnType) || (null == ownerClass)) {
             throw new NullPointerException();
         }
 
         this.methodName = methodName;
         this.returnType = returnType;
         this.ownerClass = ownerClass;
         this.constructor = constructor;
 
         this.parameters = new LinkedList<ParameterInfo>();
         this.callees = new TreeSet<MethodInfo>();
         this.callers = new TreeSet<MethodInfo>();
         this.overridees = new TreeSet<MethodInfo>();
         this.overriders = new TreeSet<MethodInfo>();
     }
 
     /**
      * \bhԂ̏֌W`郁\bhDȉ̏ŏ߂D
      * <ol>
      * <li>\bh`ĂNX̖OԖ</li>
      * <li>\bh`ĂNX̃NX</li>
      * <li>\bh</li>
      * <li>\bḧ̌</li>
      * <li>\bḧ̌^i珇ԂɁj</li>
      */
     public int compareTo(final MethodInfo method) {
 
         if (null == method) {
             throw new NullPointerException();
         }
 
         // NXIuWFNg compareTo pD
         // NX̖OԖCNXrɗpĂD
         ClassInfo ownerClass = this.getOwnerClass();
         ClassInfo correspondOwnerClass = method.getOwnerClass();
         final int classOrder = ownerClass.compareTo(correspondOwnerClass);
         if (classOrder != 0) {
             return classOrder;
         } else {
 
             // \bhŔr
             String name = this.getMethodName();
             String correspondName = method.getMethodName();
             final int methodNameOrder = name.compareTo(correspondName);
             if (methodNameOrder != 0) {
                 return methodNameOrder;
             } else {
 
                 // ̌Ŕr
                 final int parameterNumber = this.getParameterNumber();
                 final int correspondParameterNumber = method.getParameterNumber();
                 if (parameterNumber < correspondParameterNumber) {
                     return 1;
                 } else if (parameterNumber > correspondParameterNumber) {
                     return -1;
                 } else {
 
                     // ̌^ŔrD珇ԂɁD
                     Iterator<ParameterInfo> parameterIterator = this.getParameters().iterator();
                     Iterator<ParameterInfo> correspondParameterIterator = method.getParameters()
                             .iterator();
                     while (parameterIterator.hasNext() && correspondParameterIterator.hasNext()) {
                         ParameterInfo parameter = parameterIterator.next();
                         ParameterInfo correspondParameter = correspondParameterIterator.next();
                         String typeName = parameter.getName();
                         String correspondTypeName = correspondParameter.getName();
                         final int typeOrder = typeName.compareTo(correspondTypeName);
                         if (typeOrder != 0) {
                             return typeOrder;
                         }
                     }
 
                     return 0;
                 }
 
             }
         }
     }
 
     /**
      * ̃\bhCŗ^ꂽgČĂяoƂł邩ǂ𔻒肷D
      * 
      * @param methodName \bh
      * @param actualParameterTypes ̌^̃Xg
      * @return Ăяoꍇ trueCłȂꍇ false
      */
     public final boolean canCalledWith(final String methodName,
             final List<TypeInfo> actualParameterTypes) {
 
         if ((null == methodName) || (null == actualParameterTypes)) {
             throw new NullPointerException();
         }
 
         // \bhȂꍇ͊YȂ
         if (!methodName.equals(this.getMethodName())) {
             return false;
         }
 
         // ̐Ȃꍇ͊YȂ
         final List<ParameterInfo> dummyParameters = this.getParameters();
         if (dummyParameters.size() != actualParameterTypes.size()) {
             return false;
         }
 
         // ̌^擪`FbNȂꍇ͊YȂ
         final Iterator<ParameterInfo> dummyParameterIterator = dummyParameters.iterator();
         final Iterator<TypeInfo> actualParameterTypeIterator = actualParameterTypes.iterator();
         while (dummyParameterIterator.hasNext() && actualParameterTypeIterator.hasNext()) {
             final ParameterInfo dummyParameter = dummyParameterIterator.next();
             final TypeInfo actualParameterType = actualParameterTypeIterator.next();
 
             // Qƌ^̏ꍇ
             if (actualParameterType instanceof ClassInfo) {
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassInfo)) {
                     return false;
                 }
 
                 // ƓQƌ^iNXjłȂC̃TuNXłȂꍇ͊YȂ
                 if (actualParameterType.equals(dummyParameter.getType())) {
 
                 } else if (((ClassInfo) actualParameterType).isSubClass((ClassInfo) dummyParameter
                         .getType())) {
 
                }else{
                     return false;
                 }
 
                 // v~eBu^̏ꍇ
             } else if (actualParameterType instanceof PrimitiveTypeInfo) {
 
                 // PrimitiveTypeInfo#equals gē̔D
                 // Ȃꍇ͊YȂ
                 if (!actualParameterType.equals(dummyParameter.getType())) {
                     return false;
                 }
 
                 //  null ̏ꍇ
             } else if (actualParameterType instanceof NullTypeInfo) {
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassInfo)) {
                     return false;
                 }
 
                 // ̌^łȂꍇ
             } else if (actualParameterType instanceof UnknownTypeInfo) {
                 // ̌^sȏꍇ́Č^ł낤ƂOKɂĂ
            
            }else {
                 assert false : "Here shouldn't be reached!";
             }
         }
 
         return true;
     }
 
     /**
      * ̃\bh̖OԂ
      * 
      * @return \bh
      */
     public final String getMethodName() {
         return this.methodName;
     }
 
     /**
      * ̃\bh`ĂNXԂD
      * 
      * @return ̃\bh`ĂNX
      */
     public final ClassInfo getOwnerClass() {
         return this.ownerClass;
     }
 
     /**
      * ̃\bh̕Ԃľ^Ԃ
      * 
      * @return Ԃľ^
      */
     public final TypeInfo getReturnType() {
         return this.returnType;
     }
 
     /**
      * ̃\bhRXgN^ǂԂD
      * 
      * @return RXgN^łꍇ trueCłȂꍇ false
      */
     public boolean isConstuructor() {
         return this.constructor;
     }
 
     /**
      * ̃\bḧǉD public 錾Ă邪C vOČĂяo͂͂D
      * 
      * @param parameter ǉ
      */
     public void addParameter(final ParameterInfo parameter) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == parameter) {
             throw new NullPointerException();
         }
 
         this.parameters.add(parameter);
     }
 
     /**
      * ̃\bḧǉD public 錾Ă邪C vOČĂяo͂͂D
      * 
      * @param parameters ǉQ
      */
     public void addParameters(final List<ParameterInfo> parameters) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == parameters) {
             throw new NullPointerException();
         }
 
         this.parameters.addAll(parameters);
     }
 
     /**
      * ̃\bḧ̐Ԃ
      * 
      * @return ̃\bḧ̐
      */
     public int getParameterNumber() {
         return this.parameters.size();
     }
 
     /**
      * ̃\bḧ List ԂD
      * 
      * @return ̃\bḧ List
      */
     public List<ParameterInfo> getParameters() {
         return Collections.unmodifiableList(this.parameters);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param callee ǉĂяo郁\bh
      */
     public void addCallee(final MethodInfo callee) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == callee) {
             throw new NullPointerException();
         }
 
         this.callees.add(callee);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param caller ǉĂяo\bh
      */
     public void addCaller(final MethodInfo caller) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == caller) {
             throw new NullPointerException();
         }
 
         this.callers.add(caller);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param overridee ǉI[o[ChĂ郁\bh
      */
     public void addOverridee(final MethodInfo overridee) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == overridee) {
             throw new NullPointerException();
         }
 
         this.overridees.add(overridee);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param overrider ǉI[o[ChĂ郁\bh
      * 
      */
     public void addOverrider(final MethodInfo overrider) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == overrider) {
             throw new NullPointerException();
         }
 
         this.overriders.add(overrider);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhĂяoĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getCallees() {
         return Collections.unmodifiableSortedSet(this.callees);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhĂяoĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getCallers() {
         return Collections.unmodifiableSortedSet(this.callers);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhI[o[ChĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getOverridees() {
         return Collections.unmodifiableSortedSet(this.overridees);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhI[o[ChĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getOverriders() {
         return Collections.unmodifiableSortedSet(this.overriders);
     }
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     private final String methodName;
 
     /**
      * ĂNXۑ邽߂̕ϐ
      */
     private final ClassInfo ownerClass;
 
     /**
      * Ԃľ^ۑ邽߂̕ϐ
      */
     private final TypeInfo returnType;
 
     /**
      * ̃\bhRXgN^ǂۑ邽߂̕ϐ
      */
     private final boolean constructor;
 
     /**
      * ̃Xg̕ۑ邽߂̕ϐ
      */
     protected final List<ParameterInfo> parameters;
 
     /**
      * ̃\bhĂяoĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> callees;
 
     /**
      * ̃\bhĂяoĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> callers;
 
     /**
      * ̃\bhI[o[ChĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overridees;
 
     /**
      * I[o[ChĂ郁\bhۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overriders;
 }
