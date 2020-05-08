 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dynamic.block.simulation;
 
 import com.sun.jna.Library;
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 import com.sun.jna.ptr.FloatByReference;
 import com.sun.jna.ptr.IntByReference;
 
 /**
  *
  * @author trblair
  */
 public interface HACDdylib extends Library {
 //    public class JNACluster extends Structure{
 //        public JNACluster(){
 //            
 //        }
 //        public JNACluster(Pointer pointer){
 //            super(pointer);
 //            this.read();
 //        }
 //        public Pointer floats;
 //        public int size;
 //        public int total;
 //    }
     void JNAConvexDecomposition(float[] inputConcave, int num_triangles, IntByReference pcount);
     
     
 }
