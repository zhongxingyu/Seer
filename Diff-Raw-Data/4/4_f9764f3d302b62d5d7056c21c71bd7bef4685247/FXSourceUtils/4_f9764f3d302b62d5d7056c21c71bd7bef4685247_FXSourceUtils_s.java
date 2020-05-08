 package org.netbeans.api.javafx.editor;
 
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Type.MethodType;
 import com.sun.tools.javac.code.TypeTags;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javafx.code.FunctionType;
 import com.sun.tools.javafx.code.JavafxTypes;
 
 /**
  * 
  * @author Anton Chechel
  */
 public final class FXSourceUtils {
     
    private static final char[] CODE_COMPL_SUBST_BREAKERS = {' ', '\t', ';', '.', '(', ')', '\n', '\r'};
 
     private FXSourceUtils() {
     }
 
     public static String typeToString(JavafxTypes types, Type type) {
         String suffix = "";
         if (type instanceof FunctionType) {
             MethodType mtype = ((FunctionType) type).asMethodType();
             return "function" + methodToString(types, mtype);
         }
 
         if (types.isSequence(type)) {
             suffix = "[ ]";
             type = types.elementType(type);
         }
         switch (type.tag) {
             case TypeTags.DOUBLE:
                 return "Number" + suffix;
 
             case TypeTags.INT:
                 return "Integer" + suffix;
 
             case TypeTags.VOID:
                 return "Void" + suffix;
 
             default:
                 return type.toString() + suffix;
         }
     }
 
     public static String methodToString(JavafxTypes types, MethodType mtype) {
         StringBuilder s = new StringBuilder();
         s.append("(");
         if (mtype == null) {
             s.append("???");
         } else {
             List<Type> args = mtype.argtypes;
             for (List<Type> l = args; l.nonEmpty(); l = l.tail) {
                 if (l != args) {
                     s.append(", ");
                 }
                 s.append(':');
                 s.append(typeToString(types, l.head));
             }
         }
         s.append("):");
         s.append(mtype == null ? "???" : typeToString(types, mtype.restype));
         return s.toString();
     }
 
     public static int getSubstitutionLenght(final String text, final int offset, int length) {
         if (text == null) {
             return length;
         }
         
         int index = offset + text.length();
         for (int i = 0; i < CODE_COMPL_SUBST_BREAKERS.length; i++) {
             int k = text.indexOf(CODE_COMPL_SUBST_BREAKERS[i], offset);
             if (k != -1 && k < index) {
                 index = k;
             }
         }
         int ret = index - offset;
         if (length > ret) {
             ret = length;
         }
         return ret;
//        return index > length ?  : length;
     }
 }
