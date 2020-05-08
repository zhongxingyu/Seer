 
 
 /* First created by JCasGen Tue Oct 16 18:27:09 EDT 2012 */
 
 import org.apache.uima.jcas.JCas; 
 import org.apache.uima.jcas.JCasRegistry;
 import org.apache.uima.jcas.cas.TOP_Type;
 
 import org.apache.uima.jcas.tcas.Annotation;
 
 
 /** gene
  * Updated by JCasGen Wed Oct 17 12:39:11 EDT 2012
 * XML source: D:/0.cmu/2012.fall/2.SE/workspace/11791/hw1-suyoung1/src/main/resources/descriptors/MyAnnotator.xml
  * @generated */
 public class MyGene extends Annotation {
   /** @generated
    * @ordered 
    */
   @SuppressWarnings ("hiding")
   public final static int typeIndexID = JCasRegistry.register(MyGene.class);
   /** @generated
    * @ordered 
    */
   @SuppressWarnings ("hiding")
   public final static int type = typeIndexID;
   /** @generated  */
   @Override
   public              int getTypeIndexID() {return typeIndexID;}
  
   /** Never called.  Disable default constructor
    * @generated */
   protected MyGene() {/* intentionally empty block */}
     
   /** Internal - constructor used by generator 
    * @generated */
   public MyGene(int addr, TOP_Type type) {
     super(addr, type);
     readObject();
   }
   
   /** @generated */
   public MyGene(JCas jcas) {
     super(jcas);
     readObject();   
   } 
 
   /** @generated */  
   public MyGene(JCas jcas, int begin, int end) {
     super(jcas);
     setBegin(begin);
     setEnd(end);
     readObject();
   }   
 
   /** <!-- begin-user-doc -->
     * Write your own initialization here
     * <!-- end-user-doc -->
   @generated modifiable */
   private void readObject() {/*default - does nothing empty block */}
      
 }
 
     
