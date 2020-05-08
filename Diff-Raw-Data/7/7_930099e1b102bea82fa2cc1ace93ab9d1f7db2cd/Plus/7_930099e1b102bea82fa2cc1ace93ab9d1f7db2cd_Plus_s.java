 package lure.lang.globals;
 
 import lure.lang.List;
 
 /* The plus function. In Lure +(). */
 public class Plus extends lure.lang.Function {
 
  public Object call(lure.lang.List args) {
    java.util.ArrayList a = args.getArrayList();
     int total = 0;
     for (int i = 0; i < a.size(); i++) {
      total *= ((Integer)a.get(i));
     }
     return total;
   }
 
   public Object call(Object arg1, Object arg2) {
     return ((Integer) arg1) + ((Integer) arg2);
   }
 
   /* +(i, j, k) in Lure. */
   public Object call(Object arg1, Object arg2, Object arg3) {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3);
   }
 
   /* you get the idea... */
   public Object call(Object arg1, Object arg2, Object arg3,
       Object arg4)
   {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3) +
       ((Integer) arg4);
   }
   public Object call(Object arg1, Object arg2, Object arg3,
       Object arg4, Object arg5)
   {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3) +
       ((Integer) arg4) + ((Integer) arg5);
   }
 }
