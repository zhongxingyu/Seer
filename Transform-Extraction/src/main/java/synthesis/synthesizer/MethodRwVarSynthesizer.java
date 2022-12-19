package synthesis.synthesizer;

import java.util.List;
import add.features.utils.VariableResolver;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

public class MethodRwVarSynthesizer extends AbstractSynthesizer {

	public MethodRwVarSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {

		CtTypeReference invocationtype = ((CtInvocation)(this.element)).getType();
		List<CtVariable> variablesinscope = VariableResolver.searchVariablesInScope(this.element);
        
		if(invocationtype!=null) {
			for(CtVariable var:variablesinscope) {
				if(compareTypes(var.getType(),invocationtype))
					this.returnednewcode.add(var.toString());
			}
		} else {
			CtClass parentClass = this.element.getParent(CtClass.class);
			List<CtTypeReference> inferredpotentionaltypes = inferPotentionalTypes ((CtInvocation)(this.element), parentClass);

			for(CtVariable var:variablesinscope) {
				if(compareInferredTypes(var.getType(),inferredpotentionaltypes))
					this.returnednewcode.add(var.toString());
			}
		}
	}
}
