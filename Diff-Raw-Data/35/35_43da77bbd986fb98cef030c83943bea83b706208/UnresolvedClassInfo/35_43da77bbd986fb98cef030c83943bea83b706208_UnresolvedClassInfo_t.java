 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ASTp[XŎ擾NXꎞIɊi[邽߂̃NXD ȉ̏
  * 
  * <ul>
  * <li>Cq</li>
  * <li>O</li>
  * <li>NX</li>
  * <li>s</li>
  * <li>eNXꗗ</li>
  * <li>qNXꗗ</li>
  * <li>Ci[NXꗗ</li>
  * <li>`\bhꗗ</li>
  * <li>`tB[hꗗ</li>
  * <li>t@Cʒu</li>
  * </ul>
  * 
  * @author y-higo
  * 
  */
 public final class UnresolvedClassInfo implements UnresolvedTypeInfo, VisualizableSetting,
         MemberSetting, PositionSetting, Unresolved<ClassInfo> {
 
     /**
      * ȂRXgN^
      */
     public UnresolvedClassInfo() {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         this.namespace = null;
         this.className = null;
         this.loc = 0;
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.superClasses = new LinkedHashSet<UnresolvedTypeInfo>();
         this.innerClasses = new HashSet<UnresolvedClassInfo>();
         this.definedMethods = new HashSet<UnresolvedMethodInfo>();
         this.definedFields = new HashSet<UnresolvedFieldInfo>();
 
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
      * ̃NXƑΏۃNXǂ𔻒肷
      * 
      * @param o rΏۃNX
      */
     @Override
     public boolean equals(final Object o) {
 
         if (null == o) {
             throw new NullPointerException();
         }
 
         if (!(o instanceof UnresolvedClassInfo)) {
             return false;
         }
 
         final String[] fullQualifiedName = this.getFullQualifiedName();
         final String[] correspondFullQualifiedName = ((UnresolvedClassInfo) o)
                 .getFullQualifiedName();
 
         if (fullQualifiedName.length != correspondFullQualifiedName.length) {
             return false;
         }
 
         for (int i = 0; i < fullQualifiedName.length; i++) {
             if (!fullQualifiedName[i].equals(correspondFullQualifiedName[i])) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * ̃NX̃nbVR[hԂ
      * 
      * @param ̃NX̃nbVR[h
      */
     @Override
     public int hashCode() {
 
         final StringBuffer buffer = new StringBuffer();
         final String[] fullQualifiedName = this.getFullQualifiedName();
         for (int i = 0; i < fullQualifiedName.length; i++) {
             buffer.append(fullQualifiedName[i]);
         }
 
         return buffer.toString().hashCode();
     }
 
     /**
      * OԖԂ
      * 
      * @return OԖ
      */
     public String[] getNamespace() {
         return this.namespace;
     }
 
     /**
      * NX擾
      * 
      * @return NX
      */
     public String getClassName() {
         return this.className;
     }
 
     /**
      * ̃NX̊SCԂ
      * 
      * @return ̃NX̊SC
      */
     public String[] getFullQualifiedName() {
 
         final String[] namespace = this.getNamespace();
         final String[] fullQualifiedName = new String[namespace.length + 1];
 
         for (int i = 0; i < namespace.length; i++) {
             fullQualifiedName[i] = namespace[i];
         }
         fullQualifiedName[fullQualifiedName.length - 1] = this.getClassName();
 
         return fullQualifiedName;
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
      * OԖۑ.OԖȂꍇ͒0̔z^邱ƁD
      * 
      * @param namespace OԖ
      */
     public void setNamespace(final String[] namespace) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == namespace) {
             throw new NullPointerException();
         }
 
         this.namespace = namespace;
     }
 
     /**
      * NXۑ
      * 
      * @param className
      */
     public void setClassName(final String className) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == className) {
             throw new NullPointerException();
         }
 
         this.className = className;
     }
 
     /**
      * s擾
      * 
      * @return s
      */
     public int getLOC() {
         return this.loc;
     }
 
     /**
      * sۑ
      * 
      * @param loc s
      */
     public void setLOC(final int loc) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (loc < 0) {
             throw new IllegalArgumentException("LOC must be o or more!");
         }
 
         this.loc = loc;
     }
 
     /**
      * eNXǉ
      * 
      * @param superClass eNX
      */
     public void addSuperClass(final UnresolvedReferenceTypeInfo superClass) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == superClass) {
             throw new NullPointerException();
         }
 
         this.superClasses.add(superClass);
     }
 
     /**
      * Ci[NXǉ
      * 
      * @param innerClass Ci[NX
      */
     public void addInnerClass(final UnresolvedClassInfo innerClass) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == innerClass) {
             throw new NullPointerException();
         }
 
         this.innerClasses.add(innerClass);
     }
 
     /**
      * `Ă郁\bhǉ
      * 
      * @param definedMethod `Ă郁\bh
      */
     public void addDefinedMethod(final UnresolvedMethodInfo definedMethod) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == definedMethod) {
             throw new NullPointerException();
         }
 
         this.definedMethods.add(definedMethod);
     }
 
     /**
      * `ĂtB[hǉ
      * 
      * @param definedField `ĂtB[h
      */
     public void addDefinedField(final UnresolvedFieldInfo definedField) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == definedField) {
             throw new NullPointerException();
         }
 
         this.definedFields.add(definedField);
     }
 
     /**
      * eNX̃ZbgԂ
      * 
      * @return eNX̃Zbg
      */
     public Set<UnresolvedTypeInfo> getSuperClasses() {
         return Collections.unmodifiableSet(this.superClasses);
     }
 
     /**
      * Ci[NX̃ZbgԂ
      * 
      * @return Ci[NX̃Zbg
      */
     public Set<UnresolvedClassInfo> getInnerClasses() {
         return Collections.unmodifiableSet(this.innerClasses);
     }
 
     /**
      * ONXԂ
      * 
      * @return ONX. ONXȂꍇnull
      */
     public UnresolvedClassInfo getOuterClass() {
         return this.outerClass;
     }
 
     /**
      * `Ă郁\bh̃ZbgԂ
      * 
      * @return `Ă郁\bh̃Zbg
      */
     public Set<UnresolvedMethodInfo> getDefinedMethods() {
         return Collections.unmodifiableSet(this.definedMethods);
     }
 
     /**
      * `ĂtB[h̃Zbg
      * 
      * @return `ĂtB[h̃Zbg
      */
     public Set<UnresolvedFieldInfo> getDefinedFields() {
         return Collections.unmodifiableSet(this.definedFields);
     }
 
     /**
      * ^Ԃ
      * 
      * @return ^
      */
     public String getTypeName() {
 
         final String delimiter = Settings.getLanguage().getNamespaceDelimiter();
         final StringBuffer buffer = new StringBuffer();
 
         for (final String name : this.getFullQualifiedName()) {
             buffer.append(name);
             buffer.append(delimiter);
         }
 
         int delimiterLength = 0;
         if (null != delimiter) {
             delimiterLength = delimiter.length();
         }
 
         return buffer.substring(0, buffer.length() - delimiterLength);
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
      * ONXZbg
      * 
      * @param outerClass ONX
      */
     public void setOuterClass(final UnresolvedClassInfo outerClass) {
         this.outerClass = outerClass;
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
     public ClassInfo getResolvedInfo() {
         return this.resolvedInfo;
     }
 
     /**
      * OꂽZbg
      * 
      * @param resolvedInfo Oꂽ
      */
     public void setResolvedInfo(final ClassInfo resolvedInfo) {
 
         if (null == resolvedInfo) {
             throw new NullPointerException();
         }
 
         this.resolvedInfo = resolvedInfo;
     }
 
     /**
      * OԖۑ邽߂̕ϐ
      */
     private String[] namespace;
 
     /**
      * NXۑ邽߂̕ϐ
      */
     private String className;
 
     /**
      * sۑ邽߂̕ϐ
      */
     private int loc;
 
     /**
      * Cqۑ邽߂̕ϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * eNXۑ邽߂̃Zbg
      */
     private final Set<UnresolvedTypeInfo> superClasses;
 
     /**
      * Ci[NXۑ邽߂̃Zbg
      */
     private final Set<UnresolvedClassInfo> innerClasses;
 
     /**
      * ÕNXێϐ
      */
     private UnresolvedClassInfo outerClass;
 
     /**
      * `Ă郁\bhۑ邽߂̃Zbg
      */
     private final Set<UnresolvedMethodInfo> definedMethods;
 
     /**
      * `ĂtB[hۑ邽߂̃Zbg
      */
     private final Set<UnresolvedFieldInfo> definedFields;
 
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
     private ClassInfo resolvedInfo;
 }
