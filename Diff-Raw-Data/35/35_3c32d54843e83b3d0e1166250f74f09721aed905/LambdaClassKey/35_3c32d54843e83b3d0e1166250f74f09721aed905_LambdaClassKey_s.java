 package org.wixpress.hoopoe.lambda;
 
 import java.util.Arrays;
 
 /**
  * @author Yoav
  * @since 10/6/11
  */
 public class LambdaClassKey {
     private RetType retType;
     private String[] names;
     private Class<?>[] types;
     private String code;
     private int hash;
 
     public LambdaClassKey(RetType retType, Var[] vars, Val[] vals, String code) {
         this.retType = retType;
         this.names = new String[vars.length + vals.length];
         this.types = new Class<?>[vars.length + vals.length];
         for (int i=0; i < vars.length; i++) {
             this.names[i] = vars[i].getName();
             this.types[i] = vars[i].boxedType();
         }
         for (int i=0; i < vals.length; i++) {
             this.names[vars.length+i] = vals[i].getName();
             this.types[vars.length+i] = vals[i].boxedType();
         }
         this.code = code;
         makeHashCode();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         LambdaClassKey that = (LambdaClassKey) o;
 
         return eq(code, that.code) &&
                 Arrays.equals(names, that.names) &&
                 eq(retType, that.retType) &&
                 Arrays.equals(types, that.types);
     }
 
     public boolean eq(Object o1, Object o2) {
         return !(o1 != null ? !o1.equals(o2) : o2 != null);
     }
 
     public void makeHashCode() {
         int result = retType != null ? retType.hashCode() : 0;
         result = 31 * result + (names != null ? Arrays.hashCode(names) : 0);
         result = 31 * result + (types != null ? Arrays.hashCode(types) : 0);
         result = 31 * result + (code != null ? code.hashCode() : 0);
         hash = result;
     }
 
     @Override
     public int hashCode() {
         return hash;
     }
 }
