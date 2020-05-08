 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * RXgN^Ăяoۑ邽߂̃NX
  * 
  * @author t-miyake, higo
  *
  */
 public class UnresolvedConstructorCallInfo extends UnresolvedCallInfo<ConstructorCallInfo> {
 
     /**
      * RXgN^ĂяosQƌ^^ăIuWFNg
      * 
      * @param unresolvedReferenceType RXgN^Ăяos^
      */
     public UnresolvedConstructorCallInfo(
             final UnresolvedReferenceTypeInfo<?> unresolvedReferenceType) {
 
         if (null == unresolvedReferenceType) {
             throw new IllegalArgumentException();
         }
 
         this.unresolvedReferenceType = unresolvedReferenceType;
     }
 
     /**
      * RXgN^ĂяosQƌ^^ď
      * @param unresolvedReferenceType RXgN^Ăяos^
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public UnresolvedConstructorCallInfo(
             final UnresolvedReferenceTypeInfo<?> unresolvedReferenceType, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         this(unresolvedReferenceType);
 
         this.setFromLine(fromLine);
         this.setFromColumn(fromColumn);
         this.setToLine(toLine);
         this.setToColumn(toColumn);
     }
 
     /**
      * Os
      */
     @Override
     public ConstructorCallInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         //@ʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // RXgN^̃VOl`擾
         final List<ExpressionInfo> actualParameters = super.resolveArguments(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
         final List<ReferenceTypeInfo> typeArguments = super.resolveTypeArguments(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         //@RXgN^̌^
         final UnresolvedTypeInfo<?> unresolvedReferenceType = this.getReferenceType();
         final TypeInfo referenceType = unresolvedReferenceType.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
         assert referenceType instanceof ClassTypeInfo : "Illegal type was found";
 
         final List<TargetConstructorInfo> constructors = NameResolver
                 .getAvailableConstructors((ClassTypeInfo) referenceType);
 
         for (final ConstructorInfo constructor : constructors) {
 
             if (constructor.canCalledWith(actualParameters)) {
                 this.resolvedInfo = new ConstructorCallInfo((ReferenceTypeInfo) referenceType,
                         constructor, usingMethod, fromLine, fromColumn, toLine, toColumn);
                 this.resolvedInfo.addArguments(actualParameters);
                 this.resolvedInfo.addTypeArguments(typeArguments);
                 return this.resolvedInfo;
             }
         }
 
         // ΏۃNXɒ`ꂽRXgN^ŊŶȂ̂ŁCONXɒ`ꂽRXgN^ĂяoĂ邱Ƃɂ
         {
             ClassInfo classInfo = ((ClassTypeInfo) referenceType).getReferencedClass();
             if (classInfo instanceof TargetClassInfo) {
                final ExternalClassInfo externalSuperClass = NameResolver
                        .getExternalSuperClass((TargetClassInfo) classInfo);
             }
             final ExternalConstructorInfo constructor = new ExternalConstructorInfo(classInfo);
             this.resolvedInfo = new ConstructorCallInfo((ReferenceTypeInfo) referenceType,
                     constructor, usingMethod, fromLine, fromColumn, toLine, toColumn);
             this.resolvedInfo.addArguments(actualParameters);
             this.resolvedInfo.addTypeArguments(typeArguments);
             return this.resolvedInfo;
         }
     }
 
     /**
      * ̖RXgN^Ăяǒ^Ԃ
      * 
      * @return ̖RXgN^Ăяǒ^
      */
     public UnresolvedReferenceTypeInfo<?> getReferenceType() {
         return this.unresolvedReferenceType;
     }
 
     private final UnresolvedReferenceTypeInfo<?> unresolvedReferenceType;
 
 }
