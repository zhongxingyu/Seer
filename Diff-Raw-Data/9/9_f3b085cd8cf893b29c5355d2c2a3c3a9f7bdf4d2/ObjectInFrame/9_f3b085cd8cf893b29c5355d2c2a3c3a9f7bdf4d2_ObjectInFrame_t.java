 package jvm;
 
 public class ObjectInFrame implements frame.VMAccess
 {
     public ObjectInFrame(String name, int offset, String signature) {
 	n = name;
 	o = offset;
 	s = signature;
     }
 
     public String toString() {
 	return "jvm.ObjectInFrame(" + o + ", " + n + ", " + s + ")";
     }
 
     public String declare() {
	return ".var " + o + " is '" + n + "' " + s;
     }
 
     public String load() {
 	switch(o) {
 	case 0: return "aload_0 ; " + n;
 	case 1: return "aload_1 ; " + n;
 	case 2: return "aload_2 ; " + n;
 	case 3: return "aload_3 ; " + n;
 	default:
 	    return "aload " + o + " ; " + n;
 	}
     }
 
     public String store() {
 	switch(o) {
 	case 0: return "astore_0 ; " + n;
 	case 1: return "astore_1 ; " + n;
 	case 2: return "astore_2 ; " + n;
 	case 3: return "astore_3 ; " + n;
 	default:
 	    return "astore " + o + " ; " + n;
 	}
     }
 
     private int o;
     private String n;
     private String s;
 }
