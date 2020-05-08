 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.NameStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.MethodDefinitionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.ModifiersDefinitionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.MethodParameterStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.TypeDescriptionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.VariableDefinitionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.MethodDefinitionStateManager.METHOD_STATE_CHANGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMethodInfo;
 
 public class MethodBuilder extends CompoundDataBuilder<UnresolvedMethodInfo>{
     
     public MethodBuilder(BuildDataManager buildDataManager,ModifiersInterpriter interpriter) {
        this(buildDataManager,interpriter,new ModifiersBuilder(),new TypeBuilder(buildDataManager),
                new NameBuilder(),new MethodParameterBuilder(buildDataManager,interpriter));
     }
     
     public MethodBuilder(BuildDataManager targetDataManager,ModifiersInterpriter interpriter,
             ModifiersBuilder modifiersBuilder,TypeBuilder typeBuilder,
             NameBuilder nameBuilder,MethodParameterBuilder parameterbuilder){
         
         this.buildManager = targetDataManager;
         this.modifiersInterpriter = interpriter;
         this.modifierBuilder = modifiersBuilder;
         this.typeBuilder = typeBuilder;
         this.nameBuilder = nameBuilder;
         this.methodParameterBuilder = parameterbuilder;
         
         addInnerBuilder(modifiersBuilder);
         addInnerBuilder(typeBuilder);
         addInnerBuilder(nameBuilder);
         addInnerBuilder(parameterbuilder);
         
         modifiersBuilder.deactivate();
         typeBuilder.deactivate();
         nameBuilder.deactivate();
         parameterbuilder.deactivate();
         
         addStateManager(this.methodStateManager);
         addStateManager(this.parameterStateManager);
         addStateManager(new ModifiersDefinitionStateManager());
         addStateManager(new TypeDescriptionStateManager());
         addStateManager(new NameStateManager());
     }
     
     
     
     public void stateChangend(final StateChangeEvent<AstVisitEvent> event) {
         StateChangeEventType type = event.getType();
         
         if (type.equals(METHOD_STATE_CHANGE.ENTER_METHOD_DEF)){
             startMethodDefinition(event.getTrigger());
         } else if (type.equals(METHOD_STATE_CHANGE.EXIT_METHOD_DEF)){
             endMethodDefinition();
         } else if (type.equals(METHOD_STATE_CHANGE.ENTER_METHOD_BLOCK)){
             if (null != buildManager){
                 buildManager.enterMethodBlock();
             }
         } else if (isActive() && methodStateManager.isInPreDeclaration()){
             if (!parameterStateManager.isInDefinition()){
                 if (type.equals(ModifiersDefinitionStateManager.MODIFIERS_STATE.ENTER_MODIFIERS_DEF)){
                     if (null != modifierBuilder){
                         modifierBuilder.activate();
                     }
                 } else if (type.equals(ModifiersDefinitionStateManager.MODIFIERS_STATE.EXIT_MODIFIERS_DEF)){
                     if (null != modifierBuilder){
                         modifierBuilder.deactivate();
                         registModifiers();
                     }
                 } else if (type.equals(TypeDescriptionStateManager.TYPE_STATE.ENTER_TYPE)){
                     if (null != typeBuilder){
                         typeBuilder.activate();
                     }
                 } else if (type.equals(TypeDescriptionStateManager.TYPE_STATE.EXIT_TYPE)){
                     if (null != typeBuilder){
                         typeBuilder.deactivate();
                         registType();
                     }
                 } else if (type.equals(NameStateManager.NAME_STATE.ENTER_NAME)){
                     if (null != nameBuilder){
                         nameBuilder.activate();
                     }
                 } else if (type.equals(NameStateManager.NAME_STATE.EXIT_NAME)){
                     if (null != nameBuilder){
                         nameBuilder.deactivate();
                         registName();
                     }
                 }
             }
             
             if (type.equals(VariableDefinitionStateManager.VARIABLE_STATE.ENTER_VARIABLE_DEF)){
                 if (null != methodParameterBuilder){
                     methodParameterBuilder.activate();
                 }
             } else if (type.equals(VariableDefinitionStateManager.VARIABLE_STATE.EXIT_VARIABLE_DEF)){
                 if (null != methodParameterBuilder){
                     methodParameterBuilder.deactivate();
                    registParameter();
                 }
             }
         }
     }
     
     private void endMethodDefinition(){
         UnresolvedMethodInfo builtMethod = buildingMethodStack.pop();
         registBuiltData(builtMethod);
         
         if (null != buildManager){
             buildManager.endMethodDefinition();
         }
     }
     
     private void registModifiers(){
         if (!buildingMethodStack.isEmpty()){
             UnresolvedMethodInfo buildingMethod = buildingMethodStack.peek();
             ModifierInfo[] modifiers = modifierBuilder.popLastBuiltData();
             for(ModifierInfo modifier : modifiers){
                 buildingMethod.addModifier(modifier);
             }
             
             if (null != this.modifiersInterpriter){
                 modifiersInterpriter.interprit(modifiers,buildingMethod,buildingMethod);
             }
         }
     }
     
     private void registType(){
         if (!buildingMethodStack.isEmpty()){
             UnresolvedMethodInfo buildingMethod = buildingMethodStack.peek();
             buildingMethod.setReturnType(typeBuilder.popLastBuiltData());
         }
     }
     
     private void registName(){
         if (!buildingMethodStack.isEmpty()){
             UnresolvedMethodInfo buildingMethod = buildingMethodStack.peek();
             String[] name = nameBuilder.popLastBuiltData();
             if (name.length > 0){
                 buildingMethod.setMethodName(name[0]);
             }
         }
     }
     
    private void registParameter(){
        if (!buildingMethodStack.isEmpty()){
            UnresolvedMethodInfo buildingMethod = buildingMethodStack.peek();
            buildingMethod.adParameter(methodParameterBuilder.popLastBuiltData());
        }
    }
    
     private void startMethodDefinition(AstVisitEvent triggerEvent){
         UnresolvedMethodInfo newMethod = new UnresolvedMethodInfo();
         this.buildingMethodStack.push(newMethod);
         
         
         if (triggerEvent.getToken().isConstructorDefinition()){
             newMethod.setConstructor(true);
         }
         
         newMethod.setFromLine(triggerEvent.getStartLine());
         newMethod.setFromColumn(triggerEvent.getStartColumn());
         newMethod.setToLine(triggerEvent.getEndLine());
         newMethod.setToColumn(triggerEvent.getEndColumn());
         
         if (null != buildManager){
             buildManager.startMethodDefinition(newMethod);
         }
     }
     
     
     private Stack<UnresolvedMethodInfo> buildingMethodStack = new Stack<UnresolvedMethodInfo>();
     
     private final ModifiersInterpriter modifiersInterpriter;
     
     private final BuildDataManager buildManager;
     
     private final TypeBuilder typeBuilder;
     private final ModifiersBuilder modifierBuilder;
     private final NameBuilder nameBuilder;
     private final MethodParameterBuilder methodParameterBuilder;
     
     private final MethodDefinitionStateManager methodStateManager =  new MethodDefinitionStateManager();
     private final MethodParameterStateManager parameterStateManager = new MethodParameterStateManager();
 }
