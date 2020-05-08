 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.TypeParameterStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedCallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedSuperTypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedUnitInfo;
 
 
 /**
  * ^p[^\zr_[
  * 
  * @author kou-tngt, t-miyake
  *
  */
 public class TypeParameterBuilder extends CompoundDataBuilder<UnresolvedTypeParameterInfo> {
 
     /**
      * ɗ^ꂽ\zf[^̊Ǘ҂ƃftHg̖Or_[C^r_[pď
      * @param buildDataManager@\zf[^̊Ǘ
      */
     public TypeParameterBuilder(final BuildDataManager buildDataManager) {
         this(buildDataManager, new NameBuilder(), new TypeBuilder(buildDataManager));
     }
 
     /**
      * ɗ^ꂽ\zf[^̊ǗҁCOr_[C^r_[pď
      * @param buildDataManager@\zf[^̊Ǘ
      * @param nameBuilder@Or_[
      * @param typeBuilder@^r_[
      */
     public TypeParameterBuilder(final BuildDataManager buildDataManager,
             final NameBuilder nameBuilder, final TypeBuilder typeBuilder) {
         if (null == buildDataManager) {
             throw new NullPointerException("buildDataManager is null.");
         }
 
         if (null == nameBuilder) {
             throw new NullPointerException("nameBuilder is null.");
         }
 
         if (null == typeBuilder) {
             throw new NullPointerException("typeBuilder is null.");
         }
 
         this.buildDataManager = buildDataManager;
         this.nameBuilder = nameBuilder;
         this.typeBuilder = typeBuilder;
 
         //r_[̓o^
         this.addInnerBuilder(nameBuilder);
         this.addInnerBuilder(typeBuilder);
 
         //Ԓʒm󂯎肽̂o^
         this.addStateManager(new TypeParameterStateManager());
     }
 
     /**
      * ԕωCxg̒ʒm󂯂郁\bhD
      * @param event ԕωCxg
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.StateDrivenDataBuilder#stateChangend(jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent)
      */
     @Override
     public void stateChangend(final StateChangeEvent<AstVisitEvent> event) {
         final StateChangeEventType type = event.getType();
 
         if (this.isActive()) {
             if (type.equals(TypeParameterStateManager.TYPE_PARAMETER.ENTER_TYPE_PARAMETER_DEF)) {
                 //^p[^`ɓ̂ł΂
                 this.nameBuilder.activate();
                 this.typeBuilder.activate();
                 this.inTypeParameterDefinition = true;
             } else if (type
                     .equals(TypeParameterStateManager.TYPE_PARAMETER.EXIT_TYPE_PARAMETER_DEF)) {
                 //^p[^`ÎŁCf[^\zČn
                 this.buildTypeParameter();
 
                 this.nameBuilder.deactivate();
                 this.typeBuilder.deactivate();
                 this.lowerBoundsType = null;
                 this.upperBoundsType = null;
                 this.nameBuilder.clearBuiltData();
                 this.typeBuilder.clearBuiltData();
                 this.inTypeParameterDefinition = false;
 
             } else if (this.inTypeParameterDefinition) {
                 //^p[^`Ȃł̏o
                 if (type.equals(TypeParameterStateManager.TYPE_PARAMETER.ENTER_TYPE_LOWER_BOUNDS)) {
                     //^̉錾̂Ō^\z΂
                     this.nameBuilder.deactivate();
                     this.typeBuilder.activate();
                 } else if (type
                         .equals(TypeParameterStateManager.TYPE_PARAMETER.EXIT_TYPE_LOWER_BOUNDS)) {
                     //^̉\z
                     this.lowerBoundsType = this.builtTypeBounds();
                 } else if (type
                         .equals(TypeParameterStateManager.TYPE_PARAMETER.ENTER_TYPE_UPPER_BOUNDS)) {
                     //^̏錾̂ō\z΂
                     this.nameBuilder.deactivate();
                     this.typeBuilder.activate();
                 } else if (type
                         .equals(TypeParameterStateManager.TYPE_PARAMETER.EXIT_TYPE_UPPER_BOUNDS)) {
                     //^̏\z
                     this.upperBoundsType = this.builtTypeBounds();
                 }
             }
         }
     }
 
     /**
      * ^p[^`̏IɌĂяoC^p[^\z郁\bh
      * ̃\bhI[o[Ch邱ƂŁC^p[^`ŔCӂ̏s킹邱ƂłD
      */
     protected void buildTypeParameter() {
         //^p[^̖O炨
         assert (this.nameBuilder.getBuiltDataCount() == 1);
 
         final String[] name = this.nameBuilder.getFirstBuiltData();
 
         //^p[^̖OɕĂĂ
         assert (name.length == 1);
 
         final UnresolvedUnitInfo<? extends UnitInfo> ownerUnit = this.buildDataManager
                 .getCurrentUnit();
 
         assert (ownerUnit instanceof UnresolvedCallableUnitInfo)
                 || (ownerUnit instanceof UnresolvedClassInfo) : "Illegal state: not parametrized unit";
 
         //^̏񉺌擾
         final UnresolvedTypeInfo<? extends TypeInfo> upperBounds = this.getUpperBounds();
         final UnresolvedTypeInfo<? extends TypeInfo> lowerBounds = this.getLowerBounds();
         final int index = buildDataManager.getCurrentTypeParameterCount();
 
         UnresolvedTypeParameterInfo parameter = null;
 
        if (null == upperBounds || upperBounds instanceof UnresolvedReferenceTypeInfo
                && null == lowerBounds || lowerBounds instanceof UnresolvedReferenceTypeInfo) {
             if (null == lowerBounds) {
                 //ȂΕʂɍ
                 parameter = new UnresolvedTypeParameterInfo(ownerUnit, name[0], index,
                         (UnresolvedReferenceTypeInfo<?>) upperBounds);
             } else {
                 //ꍇ͂
                 parameter = new UnresolvedSuperTypeParameterInfo(ownerUnit, name[0], index,
                         (UnresolvedReferenceTypeInfo<?>) upperBounds,
                         (UnresolvedReferenceTypeInfo<?>) lowerBounds);
             }
         } else {
             // ȂƂJavał͂ɓBĂ͂Ȃ(^p[^͎Qƌ^`łȂ)
             // TODO C#̏ꍇ͌^p[^v~eBu^̏ꍇ̂őΏKv
             assert false : "Illegal state: type parameter is not reference type";
         }
         //ŌɃf[^Ǘ҂ɓo^
         this.buildDataManager.addTypeParameger(parameter);
     }
 
     /**
      * ^̏ԂD
      * @return@^̏
      */
     protected UnresolvedTypeInfo<? extends TypeInfo> getUpperBounds() {
         return this.upperBoundsType;
     }
 
     /**
      * ^̉ԂD
      * @return@^̉
      */
     protected UnresolvedTypeInfo<? extends TypeInfo> getLowerBounds() {
         return this.lowerBoundsType;
     }
 
     /**
      * Ōɍ\zꂽ^̏ԂD
      * @return@Ōɍ\zꂽ^
      */
     protected UnresolvedTypeInfo<? extends TypeInfo> builtTypeBounds() {
         return this.typeBuilder.getLastBuildData();
     }
 
     /**
      * O\zsr_[ԂD
      * @return@O\zsr_[
      */
     protected NameBuilder getNameBuilder() {
         return this.nameBuilder;
     }
 
     /**
      * ^\zr_[Ԃ
      * @return@^\zr_[
      */
     protected TypeBuilder getTypeBuilder() {
         return this.typeBuilder;
     }
 
     /**
      * O\zsr_[
      */
     private final NameBuilder nameBuilder;
 
     /**
      * ^\zr_[
      */
     private final TypeBuilder typeBuilder;
 
     /**
      * \z̊Ǘ
      */
     private final BuildDataManager buildDataManager;
 
     /**
      * ^p[^̏
      */
     private UnresolvedTypeInfo<? extends TypeInfo> upperBoundsType;
 
     /**
      * ^p[^̉
      */
     private UnresolvedTypeInfo<? extends TypeInfo> lowerBoundsType;
 
     /**
      * ^p[^`ɂ邩ǂ\
      */
     private boolean inTypeParameterDefinition = false;
 
 }
