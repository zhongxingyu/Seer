 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.pixel.rulebased.util;
 
 import evopaint.pixel.rulebased.interfaces.IHTML;
 import evopaint.pixel.rulebased.interfaces.INamed;
 import java.io.Serializable;
 import javax.swing.DefaultComboBoxModel;
 
 /**
  *
  * @author tam
  */
 public class ObjectComparisonOperator implements INamed, IHTML, Serializable {
     public static final int TYPE_EQUAL = 0;
     public static final int TYPE_NOT_EQUAL = 1;
     
     public static final ObjectComparisonOperator EQUAL = new ObjectComparisonOperator(TYPE_EQUAL);
     public static final ObjectComparisonOperator NOT_EQUAL = new ObjectComparisonOperator(TYPE_NOT_EQUAL);
 
     private int type;
 
     public int getType() {
         return type;
     }
 
     public String getName() {
         return toString();
     }
     
     @Override
     public String toString() {
         switch (this.type) {
             case TYPE_EQUAL: return "==";
             case TYPE_NOT_EQUAL: return "!=";
         }
         assert(false);
         return null;
     }
 
     public String toHTML() {
         return toString();
     }
 
     public boolean compare(Object a, Object b) {
         switch (this.type) {
             case TYPE_EQUAL: return a == b; // this is an ObjectID comparison
             case TYPE_NOT_EQUAL: return a != b; // this is an ObjectID comparison
         }
         assert(false);
         return false;
     }
 
     public static DefaultComboBoxModel createComboBoxModel() {
         DefaultComboBoxModel ret = new DefaultComboBoxModel();
         ret.addElement(EQUAL);
         ret.addElement(NOT_EQUAL);
         return ret;
     }
 
     // preserve singleton through serialization
     public Object readResolve() {
         switch (this.type) {
            case TYPE_EQUAL: return ObjectComparisonOperator.TYPE_EQUAL;
            case TYPE_NOT_EQUAL: return ObjectComparisonOperator.TYPE_NOT_EQUAL;
         }
         return null;
     }
 
     private ObjectComparisonOperator(int type) {
         this.type = type;
     }
 }
