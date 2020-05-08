 package jvm;
 
 public class IntegerInFrame implements frame.VMAccess
 {
     public IntegerInFrame(String name, int offset, String signature) {
 	n = name;
 	o = offset;
 	s = signature;
     }
 
     public String toString() {
 	return "jvm.IntegerInFrame(" + o + ", " + n + ", " + s + ")";
     }
 
     public String declare() {
	return ".var " + o + " is '" + n + "' " + s;
     }
 
     public String load() {
 	switch(o) {
 	case 0: return "iload_0 ; " + n;
 	case 1: return "iload_1 ; " + n;
 	case 2: return "iload_2 ; " + n;
 	case 3: return "iload_3 ; " + n;
 	default:
 	    return "iload " + o + " ; " + n;
 	}
     }
 
     public String store() {
 	switch(o) {
 	case 0: return "istore_0 ; " + n;
 	case 1: return "istore_1 ; " + n;
 	case 2: return "istore_2 ; " + n;
 	case 3: return "istore_3 ; " + n;
 	default:
 	    return "istore " + o + " ; " + n;
 	}
     }
 
     private int o;
     private String n;
     private String s;
 }
