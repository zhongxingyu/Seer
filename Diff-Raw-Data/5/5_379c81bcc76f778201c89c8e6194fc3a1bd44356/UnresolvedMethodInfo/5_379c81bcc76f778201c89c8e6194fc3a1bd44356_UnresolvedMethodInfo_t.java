 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.Resolved;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * xڂASTp[XŎ擾\bhꎞIɊi[邽߂̃NXD
  * 
  * 
  * @author y-higo
  * 
  */
 public class UnresolvedMethodInfo implements VisualizableSetting, MemberSetting, PositionSetting,
         Unresolved {
 
     /**
      * \bh`IuWFNg
      */
     public UnresolvedMethodInfo() {
 
         this.methodName = null;
         this.returnType = null;
         this.ownerClass = null;
         this.constructor = false;
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.parameterInfos = new LinkedList<UnresolvedParameterInfo>();
         this.methodCalls = new HashSet<UnresolvedMethodCall>();
         this.fieldReferences = new HashSet<UnresolvedFieldUsage>();
         this.fieldAssignments = new HashSet<UnresolvedFieldUsage>();
         this.localVariables = new HashSet<UnresolvedLocalVariableInfo>();
 
         this.privateVisible = false;
         this.inheritanceVisible = false;
         this.namespaceVisible = false;
         this.publicVisible = false;
 
         this.instance = true;
 
         this.fromLine = 0;
         this.fromColumn = 0;
         this.toLine = 0;
         this.toColumn = 0;
 
         this.resolvedInfo = null;
     }
 
     /**
      * \bh`IuWFNg
      * 
      * @param methodName \bh
      * @param returnType Ԃľ^
      * @param ownerClass ̃\bh`ĂNX
      * @param constructor RXgN^ǂ
      */
     public UnresolvedMethodInfo(final String methodName, final UnresolvedTypeInfo returnType,
             final UnresolvedClassInfo ownerClass, final boolean constructor) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == methodName) || (null == returnType) || (null == ownerClass)) {
             throw new NullPointerException();
         }
 
         this.methodName = methodName;
         this.returnType = returnType;
         this.ownerClass = ownerClass;
         this.constructor = constructor;
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.parameterInfos = new LinkedList<UnresolvedParameterInfo>();
         this.methodCalls = new HashSet<UnresolvedMethodCall>();
         this.fieldReferences = new HashSet<UnresolvedFieldUsage>();
         this.fieldAssignments = new HashSet<UnresolvedFieldUsage>();
         this.localVariables = new HashSet<UnresolvedLocalVariableInfo>();
 
         this.privateVisible = false;
         this.inheritanceVisible = false;
         this.namespaceVisible = false;
         this.publicVisible = false;
     }
 
     /**
      * RXgN^ǂԂ
      * 
      * @return RXgN^̏ꍇ trueCłȂꍇ false
      */
     public boolean isConstructor() {
         return this.constructor;
     }
 
     /**
      * RXgN^ǂZbg
      * 
      * @param constructor RXgN^ǂ
      */
     public void setConstructor(final boolean constructor) {
         this.constructor = constructor;
     }
 
     /**
      * Cq Set Ԃ
      * 
      * @return Cq Set
      */
     public Set<ModifierInfo> getModifiers() {
         return Collections.unmodifiableSet(this.modifiers);
     }
 
     /**
      * Cqǉ
      * 
      * @param modifier ǉCq
      */
     public void addModifier(final ModifierInfo modifier) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == modifier) {
             throw new NullPointerException();
         }
 
         this.modifiers.add(modifier);
     }
 
     /**
      * \bhԂ
      * 
      * @return \bh
      */
     public String getMethodName() {
         return this.methodName;
     }
 
     /**
      * \bhZbg
      * 
      * @param methodName \bh
      */
     public void setMethodName(final String methodName) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodName) {
             throw new NullPointerException();
         }
 
         this.methodName = methodName;
     }
 
     /**
      * \bh̕Ԃľ^Ԃ
      * 
      * @return \bh̕Ԃľ^
      */
     public UnresolvedTypeInfo getReturnType() {
         return this.returnType;
     }
 
     /**
      * \bh̕ԂlZbg
      * 
      * @param returnType \bh̕Ԃl
      */
     public void setReturnType(final UnresolvedTypeInfo returnType) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == returnType) {
             throw new NullPointerException();
         }
 
         this.returnType = returnType;
     }
 
     /**
      * ̃\bh`ĂNXԂ
      * 
      * @return ̃\bh`ĂNX
      */
     public UnresolvedClassInfo getOwnerClass() {
         return this.ownerClass;
     }
 
     /**
      * \bh`ĂNXZbg
      * 
      * @param ownerClass \bh`ĂNX
      */
     public void setOwnerClass(final UnresolvedClassInfo ownerClass) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == ownerClass) {
             throw new NullPointerException();
         }
 
         this.ownerClass = ownerClass;
     }
 
     /**
      * \bhɈǉ
      * 
      * @param parameterInfo ǉ
      */
     public void adParameter(final UnresolvedParameterInfo parameterInfo) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == parameterInfo) {
             throw new NullPointerException();
         }
 
         this.parameterInfos.add(parameterInfo);
     }
 
     /**
      * \bhĂяoǉ
      * 
      * @param methodCall \bhĂяo
      */
     public void addMethodCall(final UnresolvedMethodCall methodCall) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodCall) {
             throw new NullPointerException();
         }
 
         this.methodCalls.add(methodCall);
     }
 
     /**
      * tB[hQƂǉ
      * 
      * @param fieldUsage tB[hQ
      */
     public void addFieldReference(final UnresolvedFieldUsage fieldUsage) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fieldUsage) {
             throw new NullPointerException();
         }
 
         this.fieldReferences.add(fieldUsage);
     }
 
     /**
      * tB[hǉ
      * 
      * @param fieldUsage tB[h
      */
     public void addFieldAssignment(final UnresolvedFieldUsage fieldUsage) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fieldUsage) {
             throw new NullPointerException();
         }
 
         this.fieldAssignments.add(fieldUsage);
     }
 
     /**
      * [Jϐǉ
      * 
      * @param localVariable [Jϐ
      */
     public void addLocalVariable(final UnresolvedLocalVariableInfo localVariable) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == localVariable) {
             throw new NullPointerException();
         }
 
         this.localVariables.add(localVariable);
     }
 
     /**
      * \bḧ̃XgԂ
      * 
      * @return \bḧ̃Xg
      */
     public List<UnresolvedParameterInfo> getParameterInfos() {
         return Collections.unmodifiableList(this.parameterInfos);
     }
 
     /**
      * \bhĂяo Set Ԃ
      * 
      * @return \bhĂяo Set
      */
     public Set<UnresolvedMethodCall> getMethodCalls() {
         return Collections.unmodifiableSet(this.methodCalls);
     }
 
     /**
      * tB[hQƂ Set Ԃ
      * 
      * @return tB[hQƂ Set
      */
     public Set<UnresolvedFieldUsage> getFieldReferences() {
         return Collections.unmodifiableSet(this.fieldReferences);
     }
 
     /**
      * tB[h Set Ԃ
      * 
      * @return tB[h Set
      */
     public Set<UnresolvedFieldUsage> getFieldAssignments() {
         return Collections.unmodifiableSet(this.fieldAssignments);
     }
 
     /**
      * `Ă郍[Jϐ Set Ԃ
      * 
      * @return `Ă郍[Jϐ Set
      */
     public Set<UnresolvedLocalVariableInfo> getLocalVariables() {
         return Collections.unmodifiableSet(this.localVariables);
     }
 
     /**
      * ̃\bh̍sԂ
      * 
      * @return \bh̍s
      */
     public int getLOC() {
         return this.loc;
     }
 
     /**
      * ̃\bh̍sۑ
      * 
      * @param loc ̃\bh̍s
      */
     public void setLOC(final int loc) {
         this.loc = loc;
     }
 
     /**
      * qNXQƉ\ǂݒ肷
      * 
      * @param inheritanceVisible qNXQƉ\ȏꍇ trueCłȂꍇ false
      */
     public void setInheritanceVisible(final boolean inheritanceVisible) {
         this.inheritanceVisible = inheritanceVisible;
     }
 
     /**
      * OԓQƉ\ǂݒ肷
      * 
      * @param namespaceVisible OԂQƉ\ȏꍇ trueCłȂꍇ false
      */
     public void setNamespaceVisible(final boolean namespaceVisible) {
         this.namespaceVisible = namespaceVisible;
     }
 
     /**
      * NX̂ݎQƉ\ǂݒ肷
      * 
      * @param privateVisible NX̂ݎQƉ\ȏꍇ trueCłȂꍇ false
      */
     public void setPrivateVibible(final boolean privateVisible) {
         this.privateVisible = privateVisible;
     }
 
     /**
      * ǂłQƉ\ǂݒ肷
      * 
      * @param publicVisible ǂłQƉ\ȏꍇ trueCłȂꍇ false
      */
     public void setPublicVisible(final boolean publicVisible) {
         this.publicVisible = publicVisible;
     }
 
     /**
      * qNXQƉ\ǂԂ
      * 
      * @return qNXQƉ\ȏꍇ true, łȂꍇ false
      */
     public boolean isInheritanceVisible() {
        return this.inheritanceVisible;
     }
 
     /**
      * OԂQƉ\ǂԂ
      * 
      * @return OԂQƉ\ȏꍇ true, łȂꍇ false
      */
     public boolean isNamespaceVisible() {
         return this.namespaceVisible;
     }
 
     /**
      * NX̂ݎQƉ\ǂԂ
      * 
      * @return NX̂ݎQƉ\ȏꍇ true, łȂꍇ false
      */
     public boolean isPrivateVisible() {
        return this.privateVisible;
     }
 
     /**
      * ǂłQƉ\ǂԂ
      * 
      * @return ǂłQƉ\ȏꍇ true, łȂꍇ false
      */
     public boolean isPublicVisible() {
         return this.publicVisible;
     }
 
     /**
      * CX^Xo[ǂԂ
      * 
      * @return CX^Xo[̏ꍇ trueCłȂꍇ false
      */
     public boolean isInstanceMember() {
         return this.instance;
     }
 
     /**
      * X^eBbNo[ǂԂ
      * 
      * @return X^eBbNo[̏ꍇ trueCłȂꍇ false
      */
     public boolean isStaticMember() {
         return !this.instance;
     }
 
     /**
      * CX^Xo[ǂZbg
      * 
      * @param instance CX^Xo[̏ꍇ trueC X^eBbNo[̏ꍇ false
      */
     public void setInstanceMember(final boolean instance) {
         this.instance = instance;
     }
 
     /**
      * JnsZbg
      * 
      * @param fromLine Jns
      */
     public void setFromLine(final int fromLine) {
 
         if (fromLine < 0) {
             throw new IllegalArgumentException();
         }
 
         this.fromLine = fromLine;
     }
 
     /**
      * JnZbg
      * 
      * @param fromColumn Jn
      */
     public void setFromColumn(final int fromColumn) {
 
         if (fromColumn < 0) {
             throw new IllegalArgumentException();
         }
 
         this.fromColumn = fromColumn;
     }
 
     /**
      * IsZbg
      * 
      * @param toLine Is
      */
     public void setToLine(final int toLine) {
 
         if (toLine < 0) {
             throw new IllegalArgumentException();
         }
 
         this.toLine = toLine;
     }
 
     /**
      * IZbg
      * 
      * @param toColumn I
      */
     public void setToColumn(final int toColumn) {
 
         if (toColumn < 0) {
             throw new IllegalArgumentException();
         }
 
         this.toColumn = toColumn;
     }
 
     /**
      * JnsԂ
      * 
      * @return Jns
      */
     public int getFromLine() {
         return this.fromLine;
     }
 
     /**
      * JnԂ
      * 
      * @return Jn
      */
     public int getFromColumn() {
         return this.fromColumn;
     }
 
     /**
      * IsԂ
      * 
      * @return Is
      */
     public int getToLine() {
         return this.toLine;
     }
 
     /**
      * IԂ
      * 
      * @return I
      */
     public int getToColumn() {
         return this.toColumn;
     }
 
     /**
      * OꂽԂ
      * 
      * @return Oꂽ
      */
     public Resolved getResolvedInfo() {
         return this.resolvedInfo;
     }
 
     /**
      * OꂽZbg
      * 
      * @param resolvedInfo Oꂽ
      */
     public void setResolvedInfo(final Resolved resolvedInfo) {
 
         if (null == resolvedInfo) {
             throw new NullPointerException();
         }
 
         if (!(resolvedInfo instanceof MethodInfo)) {
             throw new IllegalArgumentException();
         }
 
         this.resolvedInfo = resolvedInfo;
     }
 
     /**
      * Cqۑ
      */
     private Set<ModifierInfo> modifiers;
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     private String methodName;
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     private final List<UnresolvedParameterInfo> parameterInfos;
 
     /**
      * \bh̕Ԃlۑ邽߂̕ϐ
      */
     private UnresolvedTypeInfo returnType;
 
     /**
      * ̃\bh`ĂNXۑ邽߂̕ϐ
      */
     private UnresolvedClassInfo ownerClass;
 
     /**
      * RXgN^ǂ\ϐ
      */
     private boolean constructor;
 
     /**
      * \bhĂяoۑϐ
      */
     private final Set<UnresolvedMethodCall> methodCalls;
 
     /**
      * tB[hQƂۑϐ
      */
     private final Set<UnresolvedFieldUsage> fieldReferences;
 
     /**
      * tB[hۑϐ
      */
     private final Set<UnresolvedFieldUsage> fieldAssignments;
 
     /**
      * ̃\bhŒ`Ă郍[Jϐۑϐ
      */
     private final Set<UnresolvedLocalVariableInfo> localVariables;
 
     /**
      * \bh̍sۑ邽߂̕ϐ
      */
     private int loc;
 
     /**
      * NX̂ݎQƉ\ǂۑ邽߂̕ϐ
      */
     private boolean privateVisible;
 
     /**
      * OԂQƉ\ǂۑ邽߂̕ϐ
      */
     private boolean namespaceVisible;
 
     /**
      * qNXQƉ\ǂۑ邽߂̕ϐ
      */
     private boolean inheritanceVisible;
 
     /**
      * ǂłQƉ\ǂۑ邽߂̕ϐ
      */
     private boolean publicVisible;
 
     /**
      * CX^Xo[ǂۑ邽߂̕ϐ
      */
     private boolean instance;
 
     /**
      * Jnsۑ邽߂̕ϐ
      */
     private int fromLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private int fromColumn;
 
     /**
      * Isۑ邽߂̕ϐ
      */
     private int toLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private int toColumn;
 
     /**
      * Oꂽi[邽߂̕ϐ
      */
     private Resolved resolvedInfo;
 }
