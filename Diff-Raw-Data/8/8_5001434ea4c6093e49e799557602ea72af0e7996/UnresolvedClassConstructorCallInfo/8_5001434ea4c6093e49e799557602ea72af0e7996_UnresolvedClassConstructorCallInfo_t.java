 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.SuperConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ThisConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 public class UnresolvedClassConstructorCallInfo extends
         UnresolvedConstructorCallInfo<UnresolvedClassTypeInfo, ClassConstructorCallInfo> {
 
     public UnresolvedClassConstructorCallInfo(final UnresolvedClassTypeInfo classType) {
         super(classType);
     }
 
     public UnresolvedClassConstructorCallInfo(final UnresolvedClassTypeInfo classType,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(classType, fromLine, fromColumn, toLine, toColumn);
     }
 
     /**
      * Os
      */
     @Override
     public ClassConstructorCallInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
 
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
         final UnresolvedClassTypeInfo unresolvedReferenceType = this.getReferenceType();
         final ClassTypeInfo classType = (ClassTypeInfo) unresolvedReferenceType.resolve(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         final List<ConstructorInfo> constructors = NameResolver.getAvailableConstructors(classType);
 
         for (final ConstructorInfo constructor : constructors) {
 
             if (constructor.canCalledWith(actualParameters)) {
                 if (this instanceof UnresolvedThisConstructorCallInfo) {
                     this.resolvedInfo = new ThisConstructorCallInfo(classType, constructor,
                             usingMethod, fromLine, fromColumn, toLine, toColumn);
                 } else if (this instanceof UnresolvedSuperConstructorCallInfo) {
                     this.resolvedInfo = new SuperConstructorCallInfo(classType, constructor,
                             usingMethod, fromLine, fromColumn, toLine, toColumn);
                 } else {
                     this.resolvedInfo = new ClassConstructorCallInfo(classType, constructor,
                             usingMethod, fromLine, fromColumn, toLine, toColumn);
                 }
                 this.resolvedInfo.addArguments(actualParameters);
                 this.resolvedInfo.addTypeArguments(typeArguments);
                 return this.resolvedInfo;
             }
         }
 
         // ΏۃNXɒ`ꂽRXgN^ŊŶȂ̂ŁCONXɒ`ꂽRXgN^ĂяoĂ邱Ƃɂ
         {
             ClassInfo classInfo = classType.getReferencedClass();
             if (classInfo instanceof TargetClassInfo) {
                 classInfo = NameResolver.getExternalSuperClass(classInfo);
             }
             final ExternalConstructorInfo constructor = new ExternalConstructorInfo();
            if (null != classInfo) {
                constructor.setOuterUnit(classInfo);
            } else {
                constructor.setOuterUnit(ExternalClassInfo.UNKNOWN);
            }
             final List<ParameterInfo> externalParameters = ExternalParameterInfo.createParameters(
                     actualParameters, constructor);
             constructor.addParameters(externalParameters);
             this.resolvedInfo = new ClassConstructorCallInfo(classType, constructor, usingMethod,
                     fromLine, fromColumn, toLine, toColumn);
             this.resolvedInfo.addArguments(actualParameters);
             this.resolvedInfo.addTypeArguments(typeArguments);
             return this.resolvedInfo;
         }
     }
 }
