 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * UnresolvedClassInfoManager ǗNX
  * 
  * @author y-higo
  * 
  */
 public class UnresolvedClassInfoManager {
 
     /**
      * PIuWFNgԂ
      * 
      * @return PIuWFNg
      */
     public static UnresolvedClassInfoManager getInstance() {
         MetricsToolSecurityManager.getInstance().checkAccess();
         return SINGLETON;
     }
 
     /**
      * NXǉ
      * 
      * @param classInfo NX
      */
     public void addClass(final UnresolvedClassInfo classInfo) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         ClassKey classKey = new ClassKey(classInfo.getFullQualifiedName());
         this.classInfos.put(classKey, classInfo);
     }
 
     /**
      * NX̃ZbgԂ
      * 
      * @return NX̃Zbg
      */
     public Collection<UnresolvedClassInfo> getClassInfos() {
         return Collections.unmodifiableCollection(this.classInfos.values());
     }
 
     /**
      * NX Map ɕۑ邽߂̃L[
      * 
      * @author y-higo
      * 
      */
     class ClassKey implements Comparable<ClassKey> {
 
         /**
          * RXgN^DNX̊SC^
          * 
          * @param fullQualifiedName NX̊SC
          */
         ClassKey(final String[] fullQualifiedName) {
             
             // sȌĂяołȂ`FbN
             MetricsToolSecurityManager.getInstance().checkAccess();
             if (null == fullQualifiedName){
                 throw new NullPointerException();
             }
             
             this.fullQualifiedName = fullQualifiedName;
         }
 
         /**
          * L[Ԃ
          * 
          * @return L[D NX̊SC
          */
         public String[] getFullQualifiedName() {
             return this.fullQualifiedName;
         }
 
         /**
          * L[̏`
          */
         public int compareTo(final ClassKey classKey) {
             
             if (null == classKey){
                 throw new NullPointerException();
             }
             
             String[] fullQualifiedName = this.getFullQualifiedName();
             String[] correspondFullQualifiedName = classKey.getFullQualifiedName();
 
             if (fullQualifiedName.length > correspondFullQualifiedName.length) {
                 return 1;
             } else if (fullQualifiedName.length < correspondFullQualifiedName.length) {
                 return -1;
             } else {
                 for (int i = 0; i < fullQualifiedName.length; i++) {
                     int order = fullQualifiedName[i].compareTo(correspondFullQualifiedName[i]);
                     if (order != 0) {
                         return order;
                     }
                 }
 
                 return 0;
             }
         }
 
         /**
          * ̃NXƑΏۃNXǂ𔻒肷
          * 
          * @param o rΏۃIuWFNg
          * @return ꍇ trueCȂꍇ false
          */
         public boolean equals(Object o) {
 
             if (null == o){
                 throw new NullPointerException();
             }
             
             String[] fullQualifiedName = this.getFullQualifiedName();
            String[] correspondFullQualifiedName = ((UnresolvedClassInfo) o).getFullQualifiedName();
 
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
         public int hashCode() {
 
             StringBuffer buffer = new StringBuffer();
             String[] fullQualifiedName = this.getFullQualifiedName();
             for (int i = 0; i < fullQualifiedName.length; i++) {
                 buffer.append(fullQualifiedName[i]);
             }
 
             return buffer.toString().hashCode();
         }
 
         private final String[] fullQualifiedName;
     }
 
     /**
      * ȂRXgN^
      * 
      */
     private UnresolvedClassInfoManager() {
         this.classInfos = new HashMap<ClassKey, UnresolvedClassInfo>();
     }
 
     /**
      * PIuWFNgۑ邽߂̕ϐ
      */
     private final static UnresolvedClassInfoManager SINGLETON = new UnresolvedClassInfoManager();
 
     /**
      * UnresolvedClassInfo ۑ邽߂̃Zbg
      */
     private final Map<ClassKey, UnresolvedClassInfo> classInfos;
 }
