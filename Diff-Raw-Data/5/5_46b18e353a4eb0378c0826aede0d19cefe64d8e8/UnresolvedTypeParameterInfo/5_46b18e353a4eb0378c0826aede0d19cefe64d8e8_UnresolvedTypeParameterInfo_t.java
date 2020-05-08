 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterizable;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ^p[^\ۃNX
  * 
  * @author higo
  * 
  */
 public class UnresolvedTypeParameterInfo implements Resolvable<TypeParameterInfo> {
 
     /**
      * ^p[^^ăIuWFNg
      * 
      * @param ownerUnit ̌^p[^`Ă郆jbg(NX or \bh)
      * @param name ^p[^
      * @param index Ԗڂ̌^p[^ł邩\
      */
     public UnresolvedTypeParameterInfo(final UnresolvedUnitInfo<?> ownerUnit, final String name,
             final int index) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == ownerUnit) || (null == name)) {
             throw new IllegalArgumentException();
         }
 
         // ownerUnit\bhNXłȂꍇ̓G[
         if ((!(ownerUnit instanceof UnresolvedClassInfo))
                 && (!(ownerUnit instanceof UnresolvedCallableUnitInfo<?>))) {
             throw new IllegalArgumentException();
         }
 
         this.ownerUnit = ownerUnit;
         this.name = name;
         this.index = index;
         this.extendsTypes = new ArrayList<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>>();
     }
 
     /**
      * ^p[^^ăIuWFNg
      * 
      * @param ownerUnit ̌^p[^`Ă郆jbg(NX or \bh)
      * @param name ^p[^
      * @param index Ԗڂ̌^p[^ł邩\
      * @param extendsType NX^
      */
     public UnresolvedTypeParameterInfo(final UnresolvedUnitInfo<?> ownerUnit, final String name,
             final int index,
             final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> extendsType) {
         this(ownerUnit, name, index);
         this.addExtendsType(extendsType);
     }
 
     /**
      * ɖOĂ邩ǂԂ
      * 
      * @return ɖOĂꍇ true, łȂꍇ false
      */
     public final boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     /**
      * OꂽԂ
      * 
      * @return Oꂽ
      * @throws NotResolvedException
      */
     public final TypeParameterInfo getResolved() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     /**
      * Os
      * 
      * @param usingClass OsGeBeBNX
      * @param usingMethod OsGeBeB郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * 
      * @return ς݂̃GeBeB
      */
     public TypeParameterInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfoManager) {
             throw new NullPointerException();
         }
 
        // ɉς݂łꍇ́CLbVԂ
        if (this.alreadyResolved()) {
            return this.getResolved();
        }

         //@^p[^̏Ljbg
         final UnresolvedUnitInfo<?> unresolvedOwnerUnit = this.getOwnerUnit();
         final TypeParameterizable ownerUnit = (TypeParameterizable) unresolvedOwnerUnit.resolve(
                 usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         final String name = this.getName();
         final int index = this.getIndex();
 
         this.resolvedInfo = new TypeParameterInfo(ownerUnit, name, index);
         return this.resolvedInfo;
     }
 
     /**
      * ̌^p[^錾Ă郆jbg(NX or \bh)Ԃ
      * 
      * @return ̌^p[^錾Ă郆jbg(NX or \bh)
      */
     public final UnresolvedUnitInfo<?> getOwnerUnit() {
         return this.ownerUnit;
     }
 
     /**
      * ^p[^Ԃ
      * 
      * @return ^p[^
      */
     public final String getName() {
         return this.name;
     }
 
     /**
      * ^p[^̃CfbNXԂ
      * 
      * @return@^p[^̃CfbNX
      */
     public final int getIndex() {
         return this.index;
     }
 
     /**
      * NX^ǉ
      * 
      * @param extendsType
      */
     public final void addExtendsType(
             final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> extendsType) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == extendsType) {
             throw new IllegalArgumentException();
         }
 
         this.extendsTypes.addAll(extendsType);
     }
 
     /**
      * NX̖^Ԃ
      * 
      * @return NX̖^
      */
     public final UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo> getExtendsType() {
         return this.extendsTypes.get(0);
     }
 
     public final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> getExtendsTypes() {
         return Collections.unmodifiableList(this.extendsTypes);
     }
 
     /**
      * NXǂԂ
      * 
      * @return NXꍇ true, Ȃꍇ false
      */
     public final boolean hasExtendsType() {
         return 0 < this.extendsTypes.size();
     }
 
     /**
      * ^p[^錾Ă郆jbgۑ邽߂̕ϐ
      */
     private final UnresolvedUnitInfo<?> ownerUnit;
 
     /**
      * ^p[^ۑ邽߂̕ϐ
      */
     private final String name;
 
     /**
      * NXۑ邽߂̕ϐ
      */
     private final List<UnresolvedReferenceTypeInfo<? extends ReferenceTypeInfo>> extendsTypes;
 
     /**
      * ^p[^̃CfbNXۑ邽߂̕ϐ
      */
     private final int index;
 
     /**
      * Oꂽۑ邽߂̕ϐ
      */
     protected TypeParameterInfo resolvedInfo;
 }
