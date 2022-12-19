package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import add.features.utils.StringDistance;
import add.features.utils.VariableResolver;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

public class VarRwVarSynthesizer extends AbstractSynthesizer {
	
	List<CtVariable> variables = new ArrayList<CtVariable>();
	
	public VarRwVarSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {
		
		List<CtVariable> inscopevariables = VariableResolver.searchVariablesInScope(this.element);

		if(this.element instanceof CtVariableAccess) {

			CtVariableAccess varaccess= (CtVariableAccess)(this.element);
			CtTypeReference type = varaccess.getType();

			for(int index0=0;index0<inscopevariables.size();index0++) {
				CtVariable understudy=inscopevariables.get(index0);
				CtTypeReference invocationtype = understudy.getType();
				
				if(compareTypes(type, invocationtype))
					this.variables.add(understudy);
			}
			
			int[] distance = new int[variables.size()];
			for(int index=0; index<variables.size(); index++) {
				distance[index]=StringDistance.calculate(varaccess.getVariable().getSimpleName(),
						variables.get(index).getSimpleName());
			}
			
			CtVariable[] varArray = new CtVariable[variables.size()];
			for(int i = 0; i < variables.size(); i++) 
				varArray[i] = variables.get(i);
			
			boolean swapped = true;
		    int j = 0;
		    int tmp;
		    CtVariable vartemp;
		    while (swapped) {
		        swapped = false;
		        j++;
		        for (int i = 0; i < distance.length - j; i++) {
		            if (distance[i] > distance[i + 1]) {
		                tmp = distance[i];
		                distance[i] = distance[i + 1];
		                distance[i + 1] = tmp;
		                
		                vartemp=varArray[i];
		                varArray[i]=varArray[i+1];
		                varArray[i+1]=vartemp;
		                
		                swapped = true;
		            }
		        }
		    }    
		    variables = Arrays.asList(varArray);
		    
			for(int i = 0; i < variables.size(); i++) {
				if(!variables.get(i).getSimpleName().equals(varaccess.getVariable().getSimpleName()))
				    this.returnednewcode.add(variables.get(i).getSimpleName());
			}
			
		} else if(this.element instanceof CtTypeAccess) {

			CtTypeAccess typeaccess = (CtTypeAccess)(this.element);	
			CtClass parentClass = this.element.getParent(CtClass.class);
			
			List<CtTypeAccess> TypeAccessFromClass = parentClass.getElements(e -> (e instanceof CtTypeAccess)).stream()
					.map(CtTypeAccess.class::cast).collect(Collectors.toList());
			
            for(int index0=0;index0<TypeAccessFromClass.size();index0++) {
				
            	CtTypeAccess understudy=TypeAccessFromClass.get(index0);
            	String commonchars= commonChars(typeaccess.toString(), 
						understudy.toString());
				String originalname=typeaccess.toString();
				double ratio=0.0;
				if(originalname.length()>0)
					ratio=Double.valueOf(commonchars.length())/Double.valueOf(originalname.length());
				
				if(ratio>0.3) {
					this.returnednewcode.add(understudy.toString());
				}
			}
            
            for(int index0=0;index0<inscopevariables.size();index0++) {
				CtVariable understudy=inscopevariables.get(index0);
				this.returnednewcode.add(understudy.getSimpleName());
			}
		}
	}
}
