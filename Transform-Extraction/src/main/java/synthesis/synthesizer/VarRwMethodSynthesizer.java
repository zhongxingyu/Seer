package synthesis.synthesizer;

import java.util.List;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

public class VarRwMethodSynthesizer extends AbstractSynthesizer {

	public VarRwMethodSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {

		List<CtInvocation> invocationsFromClass=getInvocationsFromClass();
		List<CtConstructorCall> constructorcallsFromClass=getConstructorcallsFromClass();
	
		if(this.element instanceof CtVariableAccess) {
			CtVariableAccess var = (CtVariableAccess)(this.element);
			CtTypeReference type = var.getType();

			for(int index0=0;index0<invocationsFromClass.size();index0++) {
				CtInvocation understudy=invocationsFromClass.get(index0);
				CtTypeReference invocationtype = understudy.getType();
				
				if(compareTypes(type, invocationtype))
					this.returnednewcode.add(understudy.toString());
			}
			
			for(int index0=0;index0<constructorcallsFromClass.size();index0++) {
				CtConstructorCall understudy=constructorcallsFromClass.get(index0);
				CtTypeReference constructorcalltype = understudy.getType();

				if(compareTypes(type, constructorcalltype))
					this.returnednewcode.add(understudy.toString());
			}
		} else if(this.element instanceof CtTypeAccess) {
			
			CtTypeAccess typeaccess = (CtTypeAccess)(this.element);	
			CtClass parentClass = this.element.getParent(CtClass.class);
			List<CtTypeReference> inferredpotentionaltypes = inferPotentionalTypes ((CtTypeAccess)(this.element), parentClass);

            for(int index0=0;index0<invocationsFromClass.size();index0++) {
				
				CtInvocation understudy=invocationsFromClass.get(index0);
				CtTypeReference invocationtype = understudy.getType();
				
				if(compareInferredTypes(invocationtype, inferredpotentionaltypes))
					this.returnednewcode.add(understudy.toString());
			}
			
			for(int index0=0;index0<constructorcallsFromClass.size();index0++) {
				CtConstructorCall understudy=constructorcallsFromClass.get(index0);
				CtTypeReference constructorcalltype = understudy.getType();

				if(compareInferredTypes(constructorcalltype, inferredpotentionaltypes))
					this.returnednewcode.add(understudy.toString());
			}
		}		
	
	}
}
