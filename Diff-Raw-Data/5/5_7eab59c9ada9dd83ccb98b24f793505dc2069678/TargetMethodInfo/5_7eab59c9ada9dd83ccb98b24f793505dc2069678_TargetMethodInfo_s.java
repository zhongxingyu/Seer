 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * Ώۃ\bh̏ۗLNXD ȉ̏D
  * <ul>
  * <li>\bh</li>
  * <li>Cq</li>
  * <li>Ԃľ^</li>
  * <li>̃Xg</li>
  * <li>s</li>
  * <li>Rg[Oti΂炭͖j</li>
  * <li>[Jϐ</li>
  * <li>ĂNX</li>
  * <li>ĂяoĂ郁\bh</li>
  * <li>ĂяoĂ郁\bh</li>
  * <li>I[o[ChĂ郁\bh</li>
  * <li>I[o[ChĂ郁\bh</li>
  * <li>QƂĂtB[h</li>
  * <li>ĂtB[h</li>
  * </ul>
  * 
  * @author y-higo
  * 
  */
 public final class TargetMethodInfo extends MethodInfo implements Visualizable, Member, Position {
 
     /**
      * \bhIuWFNgD ȉ̏񂪈Ƃė^Ȃ΂ȂȂD
      * <ul>
      * <li>\bh</li>
      * <li>VOl`</li>
      * <li>LĂNX</li>
      * <li>RXgN^ǂ</li>
      * <li>s</li>
      * </ul>
      * 
      * @param modifier Cq
      * @param name \bh
      * @param returnType Ԃľ^DRXgN^̏ꍇ́C̃NX̌^^D
      * @param ownerClass LĂNX
      * @param constructor RXgN^ǂDRXgN^̏ꍇ true,łȂꍇ falseD
      * @param loc \bh̍s
      * @param privateVisible NX̂ݎQƉ\
      * @param namespaceVisible OԂQƉ\
      * @param inheritanceVisible qNXQƉ\
      * @param publicVisible ǂłQƉ\
      * @param instance CX^Xo[ǂ
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public TargetMethodInfo(final Set<ModifierInfo> modifiers, final String name,
             final TypeInfo returnType, final ClassInfo ownerClass, final boolean constructor,
             final int loc, final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final boolean instance,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         super(name, returnType, ownerClass, constructor);
 
         if (null == modifiers) {
             throw new NullPointerException();
         }
 
         if (loc < 0) {
             throw new IllegalArgumentException("LOC must be 0 or more!");
         }
 
         this.loc = loc;
         this.modifiers = new HashSet<ModifierInfo>();
         this.localVariables = new TreeSet<LocalVariableInfo>();
         this.referencees = new TreeSet<FieldInfo>();
         this.assignmentees = new TreeSet<FieldInfo>();
         this.unresolvedUsage = new HashSet<UnresolvedTypeInfo>();
 
         this.modifiers.addAll(modifiers);
 
         this.privateVisible = privateVisible;
         this.namespaceVisible = namespaceVisible;
         this.inheritanceVisible = inheritanceVisible;
         this.publicVisible = publicVisible;
 
         this.instance = instance;
         
         this.fromLine = fromLine;
         this.fromColumn = fromColumn;
         this.toLine = toLine;
         this.toColumn = toColumn;
     }
 
     /**
      * ̃\bhŒ`Ă郍[JϐǉD public 錾Ă邪C vOČĂяo͂͂D
      * 
      * @param localVariable ǉ
      */
     public void addLocalVariable(final LocalVariableInfo localVariable) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == localVariable) {
             throw new NullPointerException();
         }
 
         this.localVariables.add(localVariable);
     }
 
     /**
      * ̃\bhQƂĂϐǉDvOCĂԂƃ^CG[D
      * 
      * @param referencee ǉQƂĂϐ
      */
     public void addReferencee(final FieldInfo referencee) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == referencee) {
             throw new NullPointerException();
         }
 
         this.referencees.add(referencee);
     }
 
     /**
      * ̃\bhsĂϐǉDvOCĂԂƃ^CG[D
      * 
      * @param assignmentee ǉĂϐ
      */
     public void addAssignmentee(final FieldInfo assignmentee) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == assignmentee) {
             throw new NullPointerException();
         }
 
         this.assignmentees.add(assignmentee);
     }
 
     /**
      * ̃\bhŁCOłȂNXQƁCtB[hQƁEC\bhĂяoǉD vOCĂԂƃ^CG[D
      * 
      * @param unresolvedType OłȂNXQƁCtB[hQƁEC\bhĂяo
      */
     public void addUnresolvedUsage(final UnresolvedTypeInfo unresolvedType) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == unresolvedType) {
             throw new NullPointerException();
         }
 
         this.unresolvedUsage.add(unresolvedType);
     }
 
     /**
      * ̃\bhŒ`Ă郍[Jϐ SortedSet ԂD
      * 
      * @return ̃\bhŒ`Ă郍[Jϐ SortedSet
      */
     public SortedSet<LocalVariableInfo> getLocalVariables() {
         return Collections.unmodifiableSortedSet(this.localVariables);
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
      * ̃\bh̍sԂ
      * 
      * @return ̃\bh̍s
      */
     public int getLOC() {
         return this.loc;
     }
 
     /**
      * ̃\bhQƂĂtB[h SortedSet ԂD
      * 
      * @return ̃\bhQƂĂtB[h SortedSet
      */
     public SortedSet<FieldInfo> getReferencees() {
         return Collections.unmodifiableSortedSet(this.referencees);
     }
 
     /**
      * ̃\bhĂtB[h SortedSet ԂD
      * 
      * @return ̃\bhĂtB[h SortedSet
      */
     public SortedSet<FieldInfo> getAssignmentees() {
         return Collections.unmodifiableSortedSet(this.assignmentees);
     }
 
     /**
      * ̃\bhŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set ԂD
      * 
      * @return ̃\bhŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set
      */
     public Set<UnresolvedTypeInfo> getUnresolvedUsages() {
         return Collections.unmodifiableSet(this.unresolvedUsage);
     }
 
     /**
      * qNXQƉ\ǂԂ
      * 
      * @return qNXQƉ\ȏꍇ true, łȂꍇ false
      */
     public boolean isInheritanceVisible() {
        return this.privateVisible;
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
        return this.inheritanceVisible;
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
      * sۑ邽߂̕ϐ
      */
     private final int loc;
 
     /**
      * Cqۑ邽߂̕ϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * ̃\bh̓Œ`Ă郍[Jϐ
      */
     private final SortedSet<LocalVariableInfo> localVariables;
 
     /**
      * QƂĂtB[hꗗۑ邽߂̕ϐ
      */
     private final SortedSet<FieldInfo> referencees;
 
     /**
      * ĂtB[hꗗۑ邽߂̕ϐ
      */
     private final SortedSet<FieldInfo> assignmentees;
 
     /**
      * OłȂNXQƁCtB[hQƁEC\bhĂяoȂǂۑ邽߂̕ϐ
      */
     private final Set<UnresolvedTypeInfo> unresolvedUsage;
 
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
      * CX^Xo[ǂۑ邽߂̕ϐ
      */
     private final boolean instance;
 
     /**
      * Jnsۑ邽߂̕ϐ
      */
     private final int fromLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private final int fromColumn;
 
     /**
      * Isۑ邽߂̕ϐ
      */
     private final int toLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private final int toColumn;
 }
