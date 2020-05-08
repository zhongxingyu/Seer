 package jp.ac.osaka_u.ist.sel.metricstool.main.parse.asm;
 
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.SuperTypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterizable;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VoidTypeInfo;
 

 /**
  * oCgR[h瓾𖼑O邽߂̃NX
  * 
  * @author higo
  *
  */
 public class Translator {
 
     public static String[] transrateName(final String unresolvedName) {
 
         if (null == unresolvedName) {
             throw new IllegalArgumentException();
         }
 
         final List<String> name = new LinkedList<String>();
         final StringTokenizer tokenizer = new StringTokenizer(unresolvedName, "/$");
         while (tokenizer.hasMoreElements()) {
             name.add(tokenizer.nextToken());
         }
         return name.toArray(new String[0]);
     }
 
     /**
      * ^𖼑ONX.
      * CÓCTypeParameterꍇ̂ݎw肷΂悢.
      * C^ɃWFlNX܂łꍇ̂ŁC
      * O͂Ǝw肷邱Ƃdv
      * 
      * @param unresolvedType
      * @param index
      * @param ownerUnit
      * @return
      */
     public static TypeInfo translateType(final String unresolvedType, final int index,
             final TypeParameterizable ownerUnit) {
 
         if (null == unresolvedType) {
             throw new IllegalArgumentException();
         }
 
         // ꕶȂ΁CprimitiveTypeVoidłȂ΂ȂȂ
         if (1 == unresolvedType.length()) {
             return translateSingleCharacterType(unresolvedType.charAt(0));
         }
 
         // '['Ŏn܂ĂƂ͔z
         else if ('[' == unresolvedType.charAt(0)) {
             final TypeInfo subType = translateType(unresolvedType.substring(1), index, ownerUnit);
 
             // ƂƔzȂΎ1₷
             if (subType instanceof ArrayTypeInfo) {
                 final ArrayTypeInfo subArrayType = (ArrayTypeInfo) subType;
                 final TypeInfo ElementType = subArrayType.getElementType();
                 final int dimension = subArrayType.getDimension();
                 return ArrayTypeInfo.getType(ElementType, dimension + 1);
             }
 
             //@złȂȂzɂ
             else {
                 return ArrayTypeInfo.getType(subType, 1);
             }
         }
 
         // złȂQƌ^̏ꍇ
         else if (('L' == unresolvedType.charAt(0))
                 && (';' == unresolvedType.charAt(unresolvedType.length() - 1))) {
 
             final ClassInfoManager classInfoManager = DataManager.getInstance()
                     .getClassInfoManager();
             final String unresolvedReferenceType = unresolvedType.substring(1, unresolvedType
                     .length() - 1);
 
             // WFlNXȂꍇ
             if ((-1 == unresolvedReferenceType.indexOf('<'))
                     && (-1 == unresolvedReferenceType.lastIndexOf('>'))) {
 
                 final String[] referenceTypeName = transrateName(unresolvedReferenceType);
                 ExternalClassInfo referenceClass = (ExternalClassInfo) classInfoManager
                         .getClassInfo(referenceTypeName);
                 if (null == referenceClass) {
                     referenceClass = new ExternalClassInfo(referenceTypeName);
                     classInfoManager.add(referenceClass);
                 }
                 return new ClassTypeInfo(referenceClass);
 
             }
 
             //@WFlNXꍇ
             else if ((0 <= unresolvedReferenceType.indexOf('<'))
                     && (0 <= unresolvedReferenceType.lastIndexOf('>'))) {
 
                 final String[] referenceTypeName = transrateName(unresolvedReferenceType.substring(
                         0, unresolvedReferenceType.indexOf('<')));
 
                 // NX̕
                 ExternalClassInfo referenceClass = (ExternalClassInfo) classInfoManager
                         .getClassInfo(referenceTypeName);
                 if (null == referenceClass) {
                     referenceClass = new ExternalClassInfo(referenceTypeName);
                     classInfoManager.add(referenceClass);
                 }
                 final ClassTypeInfo referenceType = new ClassTypeInfo(referenceClass);
 
                 //WFlNXCNXQƂɂ̏ǉ                
                 final String[] typeArguments = getTypeArguments(unresolvedReferenceType);
                 for (int i = 0; i < typeArguments.length; i++) {
                     final TypeInfo typeParameter = translateType(typeArguments[i], i, ownerUnit);
                     referenceType.addTypeArgument(typeParameter);
                 }
 
                 return referenceType;
 
             } else {
                 throw new IllegalStateException();
             }
         }
 
         // WFlNX(TE(ʂEȂĂ);)̏ꍇ
         else if ('T' == unresolvedType.charAt(0) && (':' != unresolvedType.charAt(1))
                 && ';' == unresolvedType.charAt(unresolvedType.length() - 1)) {
 
             final String identifier = unresolvedType.substring(1, unresolvedType.length() - 1);
             return new TypeParameterInfo(ownerUnit, identifier, index, null);
         }
 
         // WFlNX(+...;)̏ꍇ
         else if ('+' == unresolvedType.charAt(0)
                 && ';' == unresolvedType.charAt(unresolvedType.length() - 1)) {
 
             final String unresolvedExtendsName = unresolvedType.substring(1, unresolvedType
                     .length() - 1);
             final String[] extendsName = transrateName(unresolvedExtendsName);
             final ClassInfoManager classInfoManager = DataManager.getInstance()
                     .getClassInfoManager();
             ExternalClassInfo extendsClass = (ExternalClassInfo) classInfoManager
                     .getClassInfo(extendsName);
             if (null == extendsClass) {
                 extendsClass = new ExternalClassInfo(extendsName);
                 classInfoManager.add(extendsClass);
             }
             return new TypeParameterInfo(ownerUnit, "?", index, new ClassTypeInfo(extendsClass));
         }
 
         // WFlNX(-...;)̏ꍇ
         else if ('-' == unresolvedType.charAt(0)
                 && (';' == unresolvedType.charAt(unresolvedType.length() - 1))) {
 
             final String unresolvedSuperName = unresolvedType.substring(1,
                     unresolvedType.length() - 1);
             final String[] superName = transrateName(unresolvedSuperName);
             final ClassInfoManager classInfoManager = DataManager.getInstance()
                     .getClassInfoManager();
             ExternalClassInfo superClass = (ExternalClassInfo) classInfoManager
                     .getClassInfo(superName);
             if (null == superClass) {
                 superClass = new ExternalClassInfo(superName);
                 classInfoManager.add(superClass);
             }
             return new SuperTypeParameterInfo(ownerUnit, "?", index, null, new ClassTypeInfo(
                     superClass));
         }
 
         // WFlNX(T:Ljava/lang/Object;)̏ꍇ
        else if ((-1 != unresolvedType.indexOf(':'))
                 && (';' == unresolvedType.charAt(unresolvedType.length() - 1))) {
 
             final String identifier = unresolvedType.substring(0, unresolvedType.indexOf(':'));
             final String unresolvedExtendsType = unresolvedType.substring(unresolvedType
                     .lastIndexOf(':') + 1);
             final TypeInfo extendsType = translateType(unresolvedExtendsType, 0, ownerUnit);
             final TypeParameterInfo typeParameter = new TypeParameterInfo(ownerUnit, identifier,
                     index, extendsType);
             return typeParameter;
         }
 
         throw new IllegalArgumentException();
     }
 
     private static TypeInfo translateSingleCharacterType(final char c) {
 
         switch (c) {
         case 'Z':
             return PrimitiveTypeInfo.BOOLEAN;
         case 'C':
             return PrimitiveTypeInfo.CHAR;
         case 'B':
             return PrimitiveTypeInfo.BYTE;
         case 'S':
             return PrimitiveTypeInfo.SHORT;
         case 'I':
             return PrimitiveTypeInfo.INT;
         case 'F':
             return PrimitiveTypeInfo.FLOAT;
         case 'J':
             return PrimitiveTypeInfo.LONG;
         case 'D':
             return PrimitiveTypeInfo.DOUBLE;
         case 'V':
             return VoidTypeInfo.getInstance();
         default:
             throw new IllegalArgumentException();
         }
     }
 
     private static String[] getTypeArguments(final String type) {
 
         final List<String> typeArguments = new LinkedList<String>();
 
         int fromIndex = 0;
         int toIndex = 0;
         int nestLevel = 0;
         for (int index = 0; index < type.length(); index++) {
 
             // '<'̏
             if ('<' == type.charAt(index)) {
                 nestLevel += 1;
                 if (1 == nestLevel) {
                     fromIndex = index + 1;
                 }
             }
 
             // '>'̏
             else if ('>' == type.charAt(index)) {
                 nestLevel -= 1;
             }
 
             // ';'̏
             else if (';' == type.charAt(index)) {
 
                 if (1 == nestLevel) {
                     toIndex = index + 1;
                     typeArguments.add(type.substring(fromIndex, toIndex));
                     fromIndex = index + 1;
                 }
             }
 
             // '*'@̏
             else if ('*' == type.charAt(index)) {
 
                 if (1 == nestLevel) {
                     typeArguments.add("*");
                     fromIndex = index + 1;
                     toIndex = index + 1;
                 }
             }
         }
 
         if (0 != nestLevel) {
             throw new IllegalStateException();
         }
 
         return typeArguments.toArray(new String[0]);
     }
 }
