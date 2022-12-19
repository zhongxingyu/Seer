package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;

public class UnwrapMethodSynthesizer extends AbstractSynthesizer {
	
	List<CtExpression> arguments = new ArrayList<CtExpression>();
	
	public UnwrapMethodSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {
		
		if(this.element instanceof CtInvocation) {
			
			arguments = ((CtInvocation)(this.element)).getArguments();
			
		} else if (this.element instanceof CtConstructorCall) {
			
			arguments = ((CtConstructorCall)(this.element)).getArguments();
		}
		
		for(int index=0; index<arguments.size(); index++) {
			this.returnednewcode.add(arguments.get(index).toString());
		}
	}
}
