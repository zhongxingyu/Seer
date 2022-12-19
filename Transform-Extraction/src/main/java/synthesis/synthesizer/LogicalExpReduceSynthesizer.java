package synthesis.synthesizer;

import java.util.List;

import add.features.detector.spoon.LogicalExpressionAnalyzer;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public class LogicalExpReduceSynthesizer extends AbstractSynthesizer {
	
	public LogicalExpReduceSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {

		List<CtBinaryOperator> logicalOperators = LogicalExpressionAnalyzer.getAllLogicalOperators(this.element);
		
		String originalstring= this.element.toString();
		
		if(logicalOperators.size()>0) {
			
			for(int index=0; index<logicalOperators.size(); index++) {
				CtBinaryOperator studyoperator = logicalOperators.get(index);
				CtExpression left = studyoperator.getLeftHandOperand();
				CtExpression right = studyoperator.getRightHandOperand();
                
				if(studyoperator.getKind()==BinaryOperatorKind.OR) {
					String replacedstringleft= originalstring.replaceFirst(left.toString(), "false");
					this.returnednewcode.add(replacedstringleft);

					String replacedstringright= originalstring.replaceFirst(right.toString(), "false");
					this.returnednewcode.add(replacedstringright);
				} 
				
				if(studyoperator.getKind()==BinaryOperatorKind.AND) {
					String replacedstringleft= originalstring.replaceFirst(left.toString(), "true");
					this.returnednewcode.add(replacedstringleft);

					String replacedstringright= originalstring.replaceFirst(right.toString(), "true");
					this.returnednewcode.add(replacedstringright);
				} 
			}
		}
	}
}
