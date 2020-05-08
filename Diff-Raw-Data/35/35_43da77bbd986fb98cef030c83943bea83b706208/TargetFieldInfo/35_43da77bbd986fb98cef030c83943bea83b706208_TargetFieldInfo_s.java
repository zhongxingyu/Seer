 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 
 
 /**
  * ΏۃNXɒ`ĂtB[h̏NXD
  * 
  * @author y-higo
  */
 public final class TargetFieldInfo extends FieldInfo implements Visualizable, Member {
 
     /**
      * tB[hIuWFNg
      * 
      * @param modidiers Cq Set
      * @param name O
      * @param type ^
      * @param ownerClass ̃tB[h`ĂNX
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
     public TargetFieldInfo(final Set<ModifierInfo> modifiers, final String name,
             final TypeInfo type, final ClassInfo ownerClass, final boolean privateVisible,
             final boolean namespaceVisible, final boolean inheritanceVisible,
             final boolean publicVisible, final boolean instance, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(modifiers, name, type, ownerClass, fromLine, fromColumn, toLine, toColumn);
 
         this.privateVisible = privateVisible;
         this.namespaceVisible = namespaceVisible;
         this.inheritanceVisible = inheritanceVisible;
         this.publicVisible = publicVisible;
 
         this.instance = instance;
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
 }
