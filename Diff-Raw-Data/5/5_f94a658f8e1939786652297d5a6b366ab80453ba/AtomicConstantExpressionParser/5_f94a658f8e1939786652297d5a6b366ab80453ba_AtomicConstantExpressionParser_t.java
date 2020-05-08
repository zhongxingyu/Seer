 package net.ivoa.pdl.interpreter.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.ivoa.parameter.model.AtomicConstantExpression;
 import net.ivoa.parameter.model.ParameterType;
 import visitors.GeneralParameterVisitor;
 import CommonsObjects.GeneralParameter;
 import exeptions.InvalidExpression;
 import exeptions.InvalidParameterException;
 
 public class AtomicConstantExpressionParser extends ExpressionWithPowerParser {
 
     private AtomicConstantExpression exp;
 
      AtomicConstantExpressionParser(AtomicConstantExpression exp) {
         super();
         this.exp = exp;
     }
 
     @Override
     public List<GeneralParameter> parse() throws InvalidExpression,
     InvalidParameterException {
         List<GeneralParameter> toReturn = new ArrayList<GeneralParameter>();
 
         List<GeneralParameter> power = null;
         if (null != this.exp.getPower()) {
             power = ExpressionParserFactory.getInstance()
                     .buildParser(this.exp.getPower()).parse();
         }
 
         // The interpretation of the expression without the operation part
         toReturn = withoutOperationParser(this.exp, power);
 
         if (null != this.exp.getOperation()) {
             // The interpretation of the expression by considering the
             // expression part
             toReturn = new OperationParser(this.exp.getOperation())
             .processOperation(toReturn);
         }
         return toReturn;
     }
 
     private List<GeneralParameter> withoutOperationParser(
             AtomicConstantExpression exp, List<GeneralParameter> power)
                     throws InvalidParameterException, InvalidExpression {
         // We create the list of general Parameters
 
         List<GeneralParameter> constantsDefined = new ArrayList<GeneralParameter>();
 
         ParameterType type = buildGeneralHandledParameterType(exp);
 
         for (int i = 0; i < exp.getConstant().size(); i++) {
             constantsDefined
             .add(new GeneralParameter(exp.getConstant().get(i), type,
                     "constant expression component "+i,
                     new GeneralParameterVisitor()));
         }
         return this.evaluatePower(constantsDefined, power);
     }
 
     private ParameterType buildGeneralHandledParameterType(
             AtomicConstantExpression exp2) throws InvalidParameterException {
         String type = exp.getConstantType().toString();
       
        
         switch (exp.getConstantType())
         {
         case INTEGER:
         case BOOLEAN:
         case DATE:
         case STRING:
             return exp.getConstantType();
 
         case REAL:
        	return exp.getConstantType();
        
             
         default:  
 
             throw new InvalidParameterException("No handled type " + type
                     + " for constant");
         }
     }
 
 }
