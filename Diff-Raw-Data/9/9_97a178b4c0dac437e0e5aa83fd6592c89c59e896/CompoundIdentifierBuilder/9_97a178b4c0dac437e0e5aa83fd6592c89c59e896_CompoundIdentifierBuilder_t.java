 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldUsage;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedVariableInfo;
 
 /**
  * 
  * 
  * @author kou-tngt
  */
 public class CompoundIdentifierBuilder extends ExpressionBuilder{
 
     /**
      * @param expressionManager
      */
     public CompoundIdentifierBuilder(ExpressionElementManager expressionManager, BuildDataManager buildManager) {
         super(expressionManager);
         this.buildDataManager = buildManager;
     }
     
     protected void afterExited(AstVisitEvent event){
         
         AstToken token = event.getToken();
         if (token.isNameSeparator()){
             buildCompoundIdentifierElement();
         }
     }
     
     protected void buildCompoundIdentifierElement(){
         ExpressionElement[] elements = getAvailableElements();
         
         if (elements.length == 2){
             ExpressionElement left = elements[0];
             ExpressionElement right = elements[1];
             
             if (right instanceof SingleIdentifierElement){
                //E͕ʂ͒P̎ʎq̂͂
                 
                 SingleIdentifierElement rightIdentifier = (SingleIdentifierElement)right;
                 String rightName = rightIdentifier.getName();
                 
                 UnresolvedTypeInfo leftElementType = null;
                 
                 if (left instanceof FieldOrMethodElement){
                     IdentifierElement leftIdentifier = (IdentifierElement)left;
                     leftElementType = leftIdentifier.resolveAsReferencedVariable(buildDataManager);
 //                }  else if (left instanceof SingleIdentifierElement){
 //                    //P̎ʎqȂA͕ϐȂ
 //                    SingleIdentifierElement leftIdentifier = (SingleIdentifierElement)left;
 //                    String leftName = leftIdentifier.getName();
 //                    UnresolvedVariableInfo variable = buildDataManager.getCurrentScopeVariable(leftName);
 //                    
 //                    if (null != variable){
 //                        //XR[vɕϐ݂
 //                        if (variable instanceof UnresolvedFieldInfo){
 //                            //̓tB[hł
 //                            leftElementType = new UnresolvedFieldUsage(buildDataManager.getAllAvaliableNames(),
 //                                    buildDataManager.getCurrentClass(),leftName);
 //                        } else {
 //                            leftElementType = variable.getType();
 //                        }
 //                    }
                 } else if (left.equals(InstanceSpecificElement.THIS)){
                     //thisȂE͂̃NX̃tB[h\bh
                     leftElementType = buildDataManager.getCurrentClass();
                 } else {
                     leftElementType = left.getType();
                 }
                 
                 if (null != leftElementType){
                     //̌^ł̂ŉE̓tB[h\bh낤
                     pushElement(new FieldOrMethodElement(leftElementType,rightName));
                 } else if (left instanceof IdentifierElement){
                     //ŜȂ񂩂悭񎯕ʎqƂĈ
                     pushElement(new CompoundIdentifierElement((IdentifierElement)left,rightName));
                 } else {
                     assert(false) : "Illegal state: unknown left element type.";
                 }
            } else if (right instanceof MethodCallElement){
                //a.new X ƂJava̓NXnewۂP[X̏ꍇ
                pushElement(right);
            } else {
                 assert(false) : "Illegal state: unexpected element type.";
             }
         } else {
             assert(false) : "Illegal state: two elements must be available.";
         }
     }
 
     @Override
     protected boolean isTriggerToken(AstToken token) {
         return token.isNameSeparator();
     }
     
     private final BuildDataManager buildDataManager;
 }
