 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.LocalParameterStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.BlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedLocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedUnitInfo;
 
 
 public class LocalParameterBuilder extends
        VariableBuilder<UnresolvedLocalVariableInfo, UnresolvedBlockInfo<? extends BlockInfo>> {
 
     public LocalParameterBuilder(BuildDataManager buildDataManager, ModifiersInterpriter interpriter) {
         this(buildDataManager, new ModifiersBuilder(), new TypeBuilder(buildDataManager),
                 new NameBuilder(), interpriter);
     }
 
     public LocalParameterBuilder(BuildDataManager buildDataManager,
             ModifiersBuilder modifiersBuilder, TypeBuilder typeBuilder, NameBuilder nameBuilder,
             ModifiersInterpriter interpriter) {
         super(buildDataManager, new LocalParameterStateManager(), modifiersBuilder, typeBuilder,
                 nameBuilder);
 
         this.interpriter = interpriter;
     }
 
     @Override
     protected UnresolvedLocalVariableInfo buildVariable(String[] name, UnresolvedTypeInfo type,
            ModifierInfo[] modifiers, UnresolvedBlockInfo<? extends BlockInfo> deifnitionBlock) {
         String varName = "";
         if (name.length > 0) {
             varName = name[0];
         }
 
         UnresolvedLocalVariableInfo local = new UnresolvedLocalVariableInfo(varName, type,
                deifnitionBlock);
         for (ModifierInfo modifier : modifiers) {
             local.addModifier(modifier);
         }
 
         if (null != interpriter) {
             // TODO           interpriter.interpirt(modifiers, parameter);
         }
 
         if (null != buildDataManager) {
             buildDataManager.addLocalParameter(local);
         }
 
         return local;
     }
 
     @Override
    protected UnresolvedBlockInfo<? extends BlockInfo> validateDefinitionSpace(
             UnresolvedUnitInfo<? extends UnitInfo> definitionUnit) {
        return definitionUnit instanceof UnresolvedBlockInfo ? (UnresolvedBlockInfo<? extends BlockInfo>) definitionUnit
                : null;
     }
 
     private final ModifiersInterpriter interpriter;
 }
