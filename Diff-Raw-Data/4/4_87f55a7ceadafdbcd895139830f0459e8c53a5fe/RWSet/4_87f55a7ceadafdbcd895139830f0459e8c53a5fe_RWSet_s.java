 package depend;
 
 import java.util.Set;
 
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IField;
 import com.ibm.wala.classLoader.IMethod;
 
 /****
  * 
  * auxiliary class denoting a pair of sets (of field references):
  * one to characterize the field reads of one method and
  * another to characterize the field writes of one method
  * 
  *
  ***/
 public class RWSet {
   
   public static AccessInfo makeAccessInfo(
       IClass accessClass, 
       IMethod accessMethod, 
       int accessLineNumber, 
       IField ifield) {    
     return new AccessInfo(accessClass, accessMethod, accessLineNumber, ifield);
   }
   
   protected Set<AccessInfo> readSet, writeSet;
   
   public RWSet(Set<AccessInfo> readSet, Set<AccessInfo> writeSet) {
     super();
     this.readSet = readSet;
     this.writeSet = writeSet;
   }
 
   /**
    * Returns a new <code>RWSet</code> by merging this readSet/writeSet with <code>other</code>'s readSet/writeSet.
    * Changes in the source <code>RWSet</code>s won't affect the returned set or vice-versa.
    * @param other the other <code>RWSet</code> to merge with this
    * @return a new RWSet representing the union of this' readSet/writeSet and <code>other</code>'s readSet/writeSet  
    */
   public RWSet merge(RWSet other){
     // TODO: Deal with null readSet/writeSets? Or we are assume they can't be null?
 //    Map<Set<FieldReference>,String> mergedReadSet = new HashSet<FieldReference>(this.readSet);
 //    mergedReadSet.addAll(other.readSet);
 //    Set<FieldReference> mergedWriteSet = new HashSet<FieldReference>(this.writeSet);
 //    mergedWriteSet.addAll(other.writeSet);
 //    return new RWSet(mergedReadSet, mergedWriteSet);
     return null;
   }
   
   public String toString() {
     StringBuffer sb = new StringBuffer();
     sb.append("READS FROM:");
     sb.append("\n");
     for (AccessInfo readAccessInfo: readSet) {
       sb.append(readAccessInfo.toString() + "\n");
     }
    System.out.println("WRITES TO:");
     for (AccessInfo writeAccessInfo: writeSet) {
       sb.append(writeAccessInfo.toString() + "\n");
     }
     return sb.toString();
   }
 }
