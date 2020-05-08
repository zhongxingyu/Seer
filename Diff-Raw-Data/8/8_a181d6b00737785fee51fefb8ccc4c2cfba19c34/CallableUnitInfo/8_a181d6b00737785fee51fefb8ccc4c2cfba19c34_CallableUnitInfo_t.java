 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * Ăяo\ȒP(\bhRXgN^)\NX
  * 
  * @author higo
  */
 
 public abstract class CallableUnitInfo extends LocalSpaceInfo implements Visualizable, Modifier,
         TypeParameterizable {
 
     /**
      * IuWFNg
      * 
      * @param modifiers CqSet
      * @param ownerClass LNX
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     CallableUnitInfo(final Set<ModifierInfo> modifiers, final ClassInfo ownerClass,
             final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(ownerClass, fromLine, fromColumn, toLine, toColumn);
 
         this.privateVisible = privateVisible;
         this.namespaceVisible = namespaceVisible;
         this.inheritanceVisible = inheritanceVisible;
         this.publicVisible = publicVisible;
 
         this.parameters = new LinkedList<ParameterInfo>();
 
         this.typeParameters = new LinkedList<TypeParameterInfo>();
         this.typeParameterUsages = new HashMap<TypeParameterInfo, TypeInfo>();
         this.thrownExceptions = new LinkedList<ClassTypeInfo>();
 
         this.unresolvedUsage = new HashSet<UnresolvedExpressionInfo<?>>();
 
         this.callers = new TreeSet<CallableUnitInfo>();
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.modifiers.addAll(modifiers);
     }
 
     /**
      * `ꂽϐSetԂ
      * 
      * @return `ꂽϐSet
      */
     @Override
     public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         final Set<VariableInfo<? extends UnitInfo>> definedVariables = new HashSet<VariableInfo<? extends UnitInfo>>();
         definedVariables.addAll(super.getDefinedVariables());
         definedVariables.addAll(this.getParameters());
         return Collections.unmodifiableSet(definedVariables);
     }
 
     /**
      * \bhԂ̏̎́C`ĂNXl邽߂ɒ`ĂD
      */
     @Override
     final public int compareTo(final Position o) {
 
         if (null == o) {
             throw new IllegalArgumentException();
         }
 
         if (o instanceof CallableUnitInfo) {
 
             final ClassInfo ownerClass = this.getOwnerClass();
             final ClassInfo correspondOwnerClass = ((CallableUnitInfo) o).getOwnerClass();
             final int classOrder = ownerClass.compareTo(correspondOwnerClass);
             if (classOrder != 0) {
                 return classOrder;
             }
         }
 
         return super.compareTo(o);
     }
 
     public int compareArgumentsTo(final CallableUnitInfo target) {
         // ̌Ŕr
         final int parameterNumber = this.getParameterNumber();
         final int correspondParameterNumber = target.getParameterNumber();
         if (parameterNumber < correspondParameterNumber) {
             return 1;
         } else if (parameterNumber > correspondParameterNumber) {
             return -1;
         } else {
 
             // ̌^ŔrD珇ԂɁD
             final Iterator<ParameterInfo> parameterIterator = this.getParameters().iterator();
             final Iterator<ParameterInfo> correspondParameterIterator = target.getParameters()
                     .iterator();
             while (parameterIterator.hasNext() && correspondParameterIterator.hasNext()) {
                 final ParameterInfo parameter = parameterIterator.next();
                 final ParameterInfo correspondParameter = correspondParameterIterator.next();
                 final String typeName = parameter.getType().getTypeName();
                 final String correspondTypeName = correspondParameter.getType().getTypeName();
                 final int typeOrder = typeName.compareTo(correspondTypeName);
                 if (typeOrder != 0) {
                     return typeOrder;
                 }
             }
 
             return 0;
         }
     }
 
     /**
      * ̃IuWFNgCŗ^ꂽgČĂяoƂł邩ǂ𔻒肷D
      * 
      * @param actualParameters ̃Xg
      * @return Ăяoꍇ trueCłȂꍇ false
      */
     boolean canCalledWith(final List<ExpressionInfo> actualParameters) {
 
         if (null == actualParameters) {
             throw new IllegalArgumentException();
         }
 
         // ̐Ȃꍇ͊YȂ
         final List<ParameterInfo> dummyParameters = this.getParameters();
         if (dummyParameters.size() != actualParameters.size()) {
             return false;
         }
 
         // ̌^擪`FbNȂꍇ͊YȂ
         final Iterator<ParameterInfo> dummyParameterIterator = dummyParameters.iterator();
         final Iterator<ExpressionInfo> actualParameterIterator = actualParameters.iterator();
         NEXT_PARAMETER: while (dummyParameterIterator.hasNext()
                 && actualParameterIterator.hasNext()) {
             final ParameterInfo dummyParameter = dummyParameterIterator.next();
             final ExpressionInfo actualParameter = actualParameterIterator.next();
 
             TypeInfo actualParameterType = actualParameter.getType();
 
             // ^p[^̏ꍇ͂̌p^߂
             if (actualParameterType instanceof TypeParameterInfo) {
                 final TypeInfo extendsType = ((TypeParameterInfo) actualParameterType)
                         .getExtendsType();
                 if (null != extendsType) {
                     actualParameterType = extendsType;
                 } else {
                     assert false : "Here should not be reached";
                 }
             }
 
             // Qƌ^̏ꍇ
             if (actualParameterType instanceof ClassTypeInfo) {
 
                 // ̌^̃NX擾
                 final ClassInfo actualParameterClass = ((ClassTypeInfo) actualParameterType)
                         .getReferencedClass();
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassTypeInfo)) {
                     return false;
                 }
 
                 // ̌^̃NX擾
                 final ClassInfo dummyParameterClass = ((ClassTypeInfo) dummyParameter.getType())
                         .getReferencedClass();
                
                // ̌^Object̏ꍇ́CĂяo\ł
                final ClassInfo objectClass = DataManager.getInstance().getClassInfoManager().getClassInfo(new String[]{"java", "lang", "Object"});
                if(dummyParameterClass.equals(objectClass)){
                    continue NEXT_PARAMETER;
                }
 
                 // CɑΏۃNXłꍇ́Čp֌WlD܂C̃TuNXłȂꍇ́CĂяo\ł͂Ȃ
                 if ((actualParameterClass instanceof TargetClassInfo)
                         && (dummyParameterClass instanceof TargetClassInfo)) {
 
                     // ƓQƌ^iNXjłȂC̃TuNXłȂꍇ͊YȂ
                     if (actualParameterClass.equals(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
 
                     } else if (actualParameterClass.isSubClass(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
 
                     } else {
                         return false;
                     }
 
                     // CɊONXłꍇ́Cꍇ̂݌Ăяo\Ƃ
                 } else if ((actualParameterClass instanceof ExternalClassInfo)
                         && (dummyParameterClass instanceof ExternalClassInfo)) {
 
                     if (actualParameterClass.equals(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
                     }
 
                     return false;
 
                     // ONXCΏۃNX̏ꍇ́C̃TuNXłꍇCĂяo\Ƃ
                 } else if ((actualParameterClass instanceof TargetClassInfo)
                         && (dummyParameterClass instanceof ExternalClassInfo)) {
 
                     if (actualParameterClass.isSubClass(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
                     }
 
                     return false;
 
                     // ΏۃNXCONX̏ꍇ́CĂяos\Ƃ
                 } else {
                     return false;
                 }
 
                 // v~eBu^̏ꍇ
             } else if (actualParameterType instanceof PrimitiveTypeInfo) {
 
                 // PrimitiveTypeInfo#equals gē̔D
                 // Ȃꍇ͊YȂ
                 // v~eBu^CvStringdummmyTypě^StringȂ瓙
                 // TODO NXStringł邪java.lang.Stringł͂ȂꍇC~XD
                 if (actualParameterType.equals(dummyParameter.getType())) {
                     continue NEXT_PARAMETER;
                 }
 
                 return false;
 
                 // z^̏ꍇ
             } else if (actualParameterType instanceof ArrayTypeInfo) {
 
                 if (!(dummyParameter.getType() instanceof ArrayTypeInfo)) {
                     return false;
                 }
 
                 if (!actualParameter.getType().equals(dummyParameter.getType())) {
                     return false;
                 }
 
                 continue NEXT_PARAMETER;
                 // TODO Javȁꍇ́C java.lang.object łOKȏKv
 
                 //  null ̏ꍇ
             } else if (actualParameter instanceof NullUsageInfo) {
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassInfo)) {
                     return false;
                 }
 
                 continue NEXT_PARAMETER;
                 // TODO Javȁꍇ́Cz^̏ꍇłOKȏKv
 
                 // ̌^łȂꍇ
             } else if (actualParameterType instanceof UnknownTypeInfo) {
 
                 // ̌^sȏꍇ́Č^ł낤ƂOKɂĂ
                 continue NEXT_PARAMETER;
 
             } else {
                 assert false : "Here shouldn't be reached!";
             }
         }
 
         return true;
     }
 
     /**
      * ̃\bḧ List ԂD
      * 
      * @return ̃\bḧ List
      */
     public final List<ParameterInfo> getParameters() {
         return Collections.unmodifiableList(this.parameters);
     }
 
     /**
      * ̃\bḧ̐Ԃ
      * 
      * @return ̃\bḧ̐
      */
     public final int getParameterNumber() {
         return this.parameters.size();
     }
 
     /**
      * ^p[^̎gpǉ
      * 
      * @param typeParameterInfo ^p[^ 
      * @param usedType ^p[^ɑĂ^
      */
     @Override
     public void addTypeParameterUsage(final TypeParameterInfo typeParameterInfo,
             final TypeInfo usedType) {
 
         if ((null == typeParameterInfo) || (null == usedType)) {
             throw new IllegalArgumentException();
         }
 
         this.typeParameterUsages.put(typeParameterInfo, usedType);
     }
 
     /**
      * ^p[^gp̃}bvԂ
      * 
      * @return ^p[^gp̃}bv
      */
     @Override
     public Map<TypeParameterInfo, TypeInfo> getTypeParameterUsages() {
         return Collections.unmodifiableMap(this.typeParameterUsages);
     }
 
     /**
      * Ŏw肳ꂽ^p[^ǉ
      * 
      * @param typeParameter ǉ^p[^
      */
     @Override
     public void addTypeParameter(final TypeParameterInfo typeParameter) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeParameter) {
             throw new NullPointerException();
         }
 
         this.typeParameters.add(typeParameter);
     }
 
     /**
      * w肳ꂽCfbNX̌^p[^Ԃ
      * 
      * @param index ^p[^̃CfbNX
      * @return@w肳ꂽCfbNX̌^p[^
      */
     @Override
     public TypeParameterInfo getTypeParameter(final int index) {
         return this.typeParameters.get(index);
     }
 
     /**
      * ^p[^ List ԂD
      * 
      * @return ̃NX̌^p[^ List
      */
     @Override
     public List<TypeParameterInfo> getTypeParameters() {
         return Collections.unmodifiableList(this.typeParameters);
     }
 
     /**
      * Ŏw肳ꂽOǉ
      * 
      * @param thrownException ǉO
      */
     public final void addThrownException(final ClassTypeInfo thrownException) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == thrownException) {
             throw new IllegalArgumentException();
         }
 
         this.thrownExceptions.add(thrownException);
     }
 
     /**
      * X[O List ԂD
      * 
      * @return X[O List
      */
     public final List<ClassTypeInfo> getThrownExceptions() {
         return Collections.unmodifiableList(this.thrownExceptions);
     }
 
     /**
      * ̌ĂяojbgŁCOłȂNXQƁCtB[hQƁEC\bhĂяoǉD vOCĂԂƃ^CG[D
      * 
      * @param entityUsage OłȂNXQƁCtB[hQƁEC\bhĂяo
      */
     public void addUnresolvedUsage(final UnresolvedExpressionInfo<?> entityUsage) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == entityUsage) {
             throw new NullPointerException();
         }
 
         this.unresolvedUsage.add(entityUsage);
     }
 
     /**
      * ̌ĂяojbgŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set ԂD
      * 
      * @return ̃\bhŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set
      */
     public Set<UnresolvedExpressionInfo<?>> getUnresolvedUsages() {
         return Collections.unmodifiableSet(this.unresolvedUsage);
     }
 
     /**
      * Cq Set Ԃ
      * 
      * @return Cq Set
      */
     @Override
     public Set<ModifierInfo> getModifiers() {
         return Collections.unmodifiableSet(this.modifiers);
     }
 
     /**
      * qNXQƉ\ǂԂ
      * 
      * @return qNXQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public boolean isInheritanceVisible() {
         return this.inheritanceVisible;
     }
 
     /**
      * OԂQƉ\ǂԂ
      * 
      * @return OԂQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public boolean isNamespaceVisible() {
         return this.namespaceVisible;
     }
 
     /**
      * NX̂ݎQƉ\ǂԂ
      * 
      * @return NX̂ݎQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public boolean isPrivateVisible() {
         return this.privateVisible;
     }
 
     /**
      * ǂłQƉ\ǂԂ
      * 
      * @return ǂłQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public boolean isPublicVisible() {
         return this.publicVisible;
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ǉDvOCĂԂƃ^CG[D
      * 
      * @param caller ǉĂяo\bh
      */
     public final void addCaller(final CallableUnitInfo caller) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == caller) {
             return;
         }
 
         this.callers.add(caller);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ SortedSet ԂD
      * 
      * @return ̃\bhĂяoĂ郁\bh SortedSet
      */
     public final SortedSet<CallableUnitInfo> getCallers() {
         return Collections.unmodifiableSortedSet(this.callers);
     }
 
     /**
      * CallableUnitInfõVOl`̃eLXg\Ԃ
      * 
      * @return CallableUnitInfõVOl`̃eLXg\
      */
     public abstract String getSignatureText();
 
     /**
      * NX̂ݎQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean privateVisible;
 
     /**
      * OԂQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean namespaceVisible;
 
     /**
      * qNXQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean inheritanceVisible;
 
     /**
      * ǂłQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean publicVisible;
 
     /**
      * Cqۑ邽߂̕ϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * ^p[^ۑϐ
      */
     private final List<TypeParameterInfo> typeParameters;
 
     /**
      * X[Oۑϐ
      */
     private final List<ClassTypeInfo> thrownExceptions;
 
     /**
      * ̃NXŎgpĂ^p[^ƎۂɌ^p[^ɑĂ^̃yA.
      * ̃NXŒ`Ă^p[^ł͂ȂD
      */
     private final Map<TypeParameterInfo, TypeInfo> typeParameterUsages;
 
     /**
      * ̃Xg̕ۑ邽߂̕ϐ
      */
     protected final List<ParameterInfo> parameters;
 
     /**
      * ̃\bhĂяoĂ郁\bhꗗۑ邽߂̕ϐ
      */
     private final SortedSet<CallableUnitInfo> callers;
 
     /**
      * OłȂNXQƁCtB[hQƁEC\bhĂяoȂǂۑ邽߂̕ϐ
      */
     private final transient Set<UnresolvedExpressionInfo<?>> unresolvedUsage;
 }
