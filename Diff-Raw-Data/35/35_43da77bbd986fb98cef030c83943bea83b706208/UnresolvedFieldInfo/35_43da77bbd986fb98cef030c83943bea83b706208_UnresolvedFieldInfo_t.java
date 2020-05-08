 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 /**
  * ASTp[XŎ擾tB[hꎞIɊi[邽߂̃NXD
  * 
  * 
  * @author y-higo
  * 
  */
 public final class UnresolvedFieldInfo extends UnresolvedVariableInfo implements
         VisualizableSetting, MemberSetting {
 
     /**
      * UnresolvedtB[hIuWFNgD tB[hƌ^C`ĂNX^Ȃ΂ȂȂD
      * 
      * @param name tB[h
      * @param type tB[ȟ^
      * @param ownerClass tB[h`ĂNX
      */
     public UnresolvedFieldInfo(final String name, final UnresolvedTypeInfo type,
             final UnresolvedClassInfo ownerClass) {
 
         super(name, type);
 
         if (null == ownerClass) {
             throw new NullPointerException();
         }
 
         this.ownerClass = ownerClass;
 
         this.privateVisible = false;
         this.inheritanceVisible = false;
         this.namespaceVisible = false;
         this.publicVisible = false;
 
         this.instance = true;
     }
 
     /**
      * ̃tB[h`Ă関NXԂ
      * 
      * @return ̃tB[h`Ă関NX
      */
     public UnresolvedClassInfo getOwnerClass() {
         return this.ownerClass;
     }
 
     /**
      * ̃tB[h`Ă関NXZbg
      * 
      * @param ownerClass ̃tB[h`Ă関NX
      */
     public void setOwnerClass(final UnresolvedClassInfo ownerClass) {
 
         if (null == ownerClass) {
             throw new NullPointerException();
         }
 
         this.ownerClass = ownerClass;
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
      * ̃tB[h`ĂNXۑ邽߂̕ϐ
      */
     private UnresolvedClassInfo ownerClass;
 
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
 }
