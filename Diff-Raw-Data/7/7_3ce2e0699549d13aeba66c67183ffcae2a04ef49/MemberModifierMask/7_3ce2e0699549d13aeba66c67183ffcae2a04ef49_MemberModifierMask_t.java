 package sword.java.class_analyzer;
 
 public class MemberModifierMask extends ModifierMask {
 
     public MemberModifierMask(int mask) {
         super(mask);
     }
 
     public boolean isSynchronized() {
         return (mMask & 0x20) != 0;
     }
 
     public boolean isVolatile() {
         return (mMask & 0x40) != 0;
     }
 
     public boolean isTransient() {
         return (mMask & 0x80) != 0;
     }
 
     public boolean isNative() {
         return (mMask & 0x100) != 0;
     }
 
     @Override
     public String getModifiersString() {
         String result = getVisibilityString();
         if (result.length() != 0) {
             result = result + ' ';
         }
 
         if (isStatic()) {
             result = result + "static ";
         }
 
         if (isFinal()) {
             result = result + "final ";
         }
 
         if (isSynchronized()) {
             result = result + "synchronized ";
         }
 
         if (isVolatile()) {
             result = result + "volatile ";
         }
 
         if (isTransient()) {
             result = result + "transient ";
         }
 
         if (isNative()) {
             result = result + "native ";
         }
 
         if (isAbstract()) {
             result = result + "abstract ";
         }
 
        if (result.length() == 0) {
            return result;
        }
        else {
            return result.substring(0, result.length() - 1);
        }
     }
 }
