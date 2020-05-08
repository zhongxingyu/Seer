 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * NX̏ۗLNXDȉ̏D
  * <ul>
  * <li>NX</li>
  * <li>Cq</li>
  * <li>OԁipbP[Wj</li>
  * <li>s</li>
  * <li>pĂNX</li>
  * <li>pĂNX</li>
  * <li>QƂĂNX</li>
  * <li>QƂĂNX</li>
  * <li>NX</li>
  * <li>̃NXŒ`Ă郁\bh</li>
  * <li>̃NXŒ`ĂtB[h</li>
  * </ul>
  * 
  * @author y-higo
  * 
  */
 public class TargetClassInfo extends ClassInfo implements Visualizable, Member, Position {
 
     /**
      * OԖCNX^ăNXIuWFNg
      * 
      * @param modifiers Cq Set
      * @param namespace OԖ
      * @param className NX
      * @param loc s
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
     public TargetClassInfo(final Set<ModifierInfo> modifiers, final NamespaceInfo namespace,
             final String className, final int loc, final boolean privateVisible,
             final boolean namespaceVisible, final boolean inheritanceVisible,
             final boolean publicVisible, final boolean instance, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(namespace, className);
 
         if (null == modifiers) {
             throw new NullPointerException();
         }
 
         if (loc < 0) {
             throw new IllegalAccessError("LOC is must be 0 or more!");
         }
 
         this.loc = loc;
         this.modifiers = new HashSet<ModifierInfo>();
         this.innerClasses = new TreeSet<TargetInnerClassInfo>();
         this.definedMethods = new TreeSet<TargetMethodInfo>();
         this.definedFields = new TreeSet<TargetFieldInfo>();
 
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
      * S薼^āCNXIuWFNg
      * 
      * @param modifiers Cq Set
      * @param fullQualifiedName S薼
      * @param loc s
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
     public TargetClassInfo(final Set<ModifierInfo> modifiers, final String[] fullQualifiedName,
             final int loc, final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final boolean instance,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         super(fullQualifiedName);
 
         if (null == modifiers) {
             throw new NullPointerException();
         }
 
         if (loc < 0) {
             throw new IllegalAccessError("LOC is must be 0 or more!");
         }
 
         this.loc = loc;
         this.modifiers = new HashSet<ModifierInfo>();
         this.innerClasses = new TreeSet<TargetInnerClassInfo>();
         this.definedMethods = new TreeSet<TargetMethodInfo>();
         this.definedFields = new TreeSet<TargetFieldInfo>();
 
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
      * ̃NXɃCi[NXǉDvOCĂԂƃ^CG[D
      * 
      * @param innerClass ǉCi[NX
      */
     public final void addInnerClass(final TargetInnerClassInfo innerClass) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == innerClass) {
             throw new NullPointerException();
         }
 
         this.innerClasses.add(innerClass);
     }
 
     /**
      * ̃NX̍sԂ
      * 
      * @return ̃NX̍s
      */
     public final int getLOC() {
         return this.loc;
     }
 
     /**
      * ̃NX̏Cq Set Ԃ
      * 
      * @return ̃NX̏Cq Set
      */
     public final Set<ModifierInfo> getModifiers() {
         return Collections.unmodifiableSet(this.modifiers);
     }
 
     /**
      * ̃NX̃Ci[NX SortedSet ԂD
      * 
      * @return Ci[NX SortedSet
      */
     public final SortedSet<TargetInnerClassInfo> getInnerClasses() {
         return Collections.unmodifiableSortedSet(this.innerClasses);
     }
 
     /**
      * ̃NXɒ`ꂽ\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param definedMethod ǉ`ꂽ\bh
      */
     public final void addDefinedMethod(final TargetMethodInfo definedMethod) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == definedMethod) {
             throw new NullPointerException();
         }
 
         this.definedMethods.add(definedMethod);
     }
 
     /**
      * ̃NXɒ`ꂽtB[hǉDvOCĂԂƃ^CG[D
      * 
      * @param definedField ǉ`ꂽtB[h
      */
     public final void addDefinedField(final TargetFieldInfo definedField) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == definedField) {
             throw new NullPointerException();
         }
 
         this.definedFields.add(definedField);
     }
 
     /**
      * ̃NXɒ`Ă郁\bh SortedSet ԂD
      * 
      * @return `Ă郁\bh SortedSet
      */
     public final SortedSet<TargetMethodInfo> getDefinedMethods() {
         return Collections.unmodifiableSortedSet(this.definedMethods);
     }
 
     /**
      * ̃NXɒ`ĂtB[h SortedSet ԂD
      * 
      * @return `ĂtB[h SortedSet
      */
     public final SortedSet<TargetFieldInfo> getDefinedFields() {
         return Collections.unmodifiableSortedSet(this.definedFields);
     }
 
     /**
      * qNXQƉ\ǂԂ
      * 
      * @return qNXQƉ\ȏꍇ true, łȂꍇ false
      */
     public final boolean isInheritanceVisible() {
        return this.inheritanceVisible;
     }
 
     /**
      * OԂQƉ\ǂԂ
      * 
      * @return OԂQƉ\ȏꍇ true, łȂꍇ false
      */
     public final boolean isNamespaceVisible() {
         return this.namespaceVisible;
     }
 
     /**
      * NX̂ݎQƉ\ǂԂ
      * 
      * @return NX̂ݎQƉ\ȏꍇ true, łȂꍇ false
      */
     public final boolean isPrivateVisible() {
        return this.privateVisible;
     }
 
     /**
      * ǂłQƉ\ǂԂ
      * 
      * @return ǂłQƉ\ȏꍇ true, łȂꍇ false
      */
     public final boolean isPublicVisible() {
         return this.publicVisible;
     }
 
     /**
      * CX^Xo[ǂԂ
      * 
      * @return CX^Xo[̏ꍇ trueCłȂꍇ false
      */
     public final boolean isInstanceMember() {
         return this.instance;
     }
 
     /**
      * X^eBbNo[ǂԂ
      * 
      * @return X^eBbNo[̏ꍇ trueCłȂꍇ false
      */
     public final boolean isStaticMember() {
         return !this.instance;
     }
 
     /**
      * JnsԂ
      * 
      * @return Jns
      */
     public final int getFromLine() {
         return this.fromLine;
     }
 
     /**
      * JnԂ
      * 
      * @return Jn
      */
     public final int getFromColumn() {
         return this.fromColumn;
     }
 
     /**
      * IsԂ
      * 
      * @return Is
      */
     public final int getToLine() {
         return this.toLine;
     }
 
     /**
      * IԂ
      * 
      * @return I
      */
     public final int getToColumn() {
         return this.toColumn;
     }
 
     /**
      * sۑ邽߂̕ϐ
      */
     private final int loc;
 
     /**
      * Cqۑϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * ̃NX̓NXꗗۑ邽߂̕ϐDڂ̓NX݂̂ۗLD
      */
     private final SortedSet<TargetInnerClassInfo> innerClasses;
 
     /**
      * ̃NXŒ`Ă郁\bhꗗۑ邽߂̕ϐD
      */
     private final SortedSet<TargetMethodInfo> definedMethods;
 
     /**
      * ̃NXŒ`ĂtB[hꗗۑ邽߂̕ϐD
      */
     private final SortedSet<TargetFieldInfo> definedFields;
 
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
