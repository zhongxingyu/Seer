 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ASTp[XŎ擾NXꎞIɊi[邽߂̃NXD ȉ̏
  * 
  * <ul>
  * <li>Cq</li>
  * <li>O</li>
  * <li>^p[^ꗗ</li>
  * <li>NX</li>
  * <li>s</li>
  * <li>eNXꗗ</li>
  * <li>qNXꗗ</li>
  * <li>Ci[NXꗗ</li>
  * <li>`\bhꗗ</li>
  * <li>`tB[hꗗ</li>
  * </ul>
  * 
  * @author higo
  * 
  */
 public final class UnresolvedClassInfo extends UnresolvedUnitInfo<TargetClassInfo> implements
         VisualizableSetting, MemberSetting, ModifierSetting {
 
     /**
      * ȂRXgN^
      */
     public UnresolvedClassInfo() {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         this.namespace = null;
         this.className = null;
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.typeParameters = new LinkedList<UnresolvedTypeParameterInfo>();
         this.superClasses = new LinkedHashSet<UnresolvedClassTypeInfo>();
         this.innerClasses = new HashSet<UnresolvedClassInfo>();
         this.definedMethods = new HashSet<UnresolvedMethodInfo>();
         this.definedConstructors = new HashSet<UnresolvedConstructorInfo>();
         this.definedFields = new HashSet<UnresolvedFieldInfo>();
 
         this.privateVisible = false;
         this.inheritanceVisible = false;
         this.namespaceVisible = false;
         this.publicVisible = false;
 
         this.instance = true;
 
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
      * ^p[^ǉ
      * 
      * @param typeParameter ǉ関^p[^
      */
     public void addTypeParameter(final UnresolvedTypeParameterInfo typeParameter) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeParameter) {
             throw new NullPointerException();
         }
 
         this.typeParameters.add(typeParameter);
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
      * @return ̃NX̃nbVR[h
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
      * ^p[^ List Ԃ
      * 
      * @return ^p[^ List
      */
     public List<UnresolvedTypeParameterInfo> getTypeParameters() {
         return Collections.unmodifiableList(this.typeParameters);
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
      * eNXǉ
      * 
      * @param superClass eNX
      */
     public void addSuperClass(final UnresolvedClassTypeInfo superClass) {
 
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
      * `ĂRXgN^ǉ
      * 
      * @param definedConstructor `ĂRXgN^\bh
      */
     public void addDefinedConstructor(final UnresolvedConstructorInfo definedConstructor) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == definedConstructor) {
             throw new NullPointerException();
         }
 
         this.definedConstructors.add(definedConstructor);
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
     public Set<UnresolvedClassTypeInfo> getSuperClasses() {
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
      * `ĂRXgN^̃ZbgԂ
      * 
      * @return `ĂRXgN^̃Zbg
      */
     public Set<UnresolvedConstructorInfo> getDefinedConstructors() {
         return Collections.unmodifiableSet(this.definedConstructors);
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
      * OꂽԂ
      * 
      * @return Oꂽ
      * @throws NotResolvedException ĂȂꍇɃX[
      */
     @Override
     public TargetClassInfo getResolvedUnit() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * ɖOꂽǂԂ
      * 
      * @return OĂꍇ trueCłȂꍇ false
      */
     @Override
     public final boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     /**
      * ̖NX
      * 
      * @param usingClass NXC̃\bhĂяo̍ۂ null ZbgĂƎvD
      * @param usingMethod \bhC̃\bhĂяo̍ۂ null ZbgĂƎvD
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManger p郁\bh}l[W
      */
     @Override
     public TargetClassInfo resolveUnit(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfoManager) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolvedUnit();
         }
 
         // CqCS薼CsCCCX^Xo[ǂ擾
         final Set<ModifierInfo> modifiers = this.getModifiers();
         final String[] fullQualifiedName = this.getFullQualifiedName();
         final boolean privateVisible = this.isPrivateVisible();
         final boolean namespaceVisible = this.isNamespaceVisible();
         final boolean inheritanceVisible = this.isInheritanceVisible();
         final boolean publicVisible = this.isPublicVisible();
         final boolean instance = this.isInstanceMember();
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // ClassInfo IuWFNg쐬CClassInfoManagerɓo^
        this.resolvedInfo = null == this.outerClass ? new TargetClassInfo(modifiers,
                 fullQualifiedName, privateVisible, namespaceVisible, inheritanceVisible,
                 publicVisible, instance, fromLine, fromColumn, toLine, toColumn)
                 : new TargetInnerClassInfo(modifiers, fullQualifiedName, usingClass,
                         privateVisible, namespaceVisible, inheritanceVisible, publicVisible,
                         instance, fromLine, fromColumn, toLine, toColumn);
 
         return this.resolvedInfo;
     }
 
     /**
      * ̖NX`̖Qƌ^Ԃ
      * 
      * @return ̖NX`̖Qƌ^
      */
     public UnresolvedClassReferenceInfo getClassReference() {
         final UnresolvedClassReferenceInfo classReference = new UnresolvedFullQualifiedNameClassReferenceInfo(
                 this);
         return classReference;
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
      * Cqۑ邽߂̕ϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * ^p[^ۑ邽߂̕ϐ
      */
     private final List<UnresolvedTypeParameterInfo> typeParameters;
 
     /**
      * eNXۑ邽߂̃Zbg
      */
     private final Set<UnresolvedClassTypeInfo> superClasses;
 
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
      * `ĂRXgN^ۑ邽߂̃Zbg
      */
     private final Set<UnresolvedConstructorInfo> definedConstructors;
 
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
      * Oꂽi[邽߂̕ϐ
      */
     private TargetClassInfo resolvedInfo;
 }
