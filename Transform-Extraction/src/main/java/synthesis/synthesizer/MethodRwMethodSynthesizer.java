package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.List;

import add.features.utils.VariableResolver;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

public class MethodRwMethodSynthesizer extends AbstractSynthesizer {
	
	public MethodRwMethodSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {

		List<CtInvocation> invocationsFromClass=getInvocationsFromClass();
		List<CtConstructorCall> constructorcallsFromClass=getConstructorcallsFromClass();
		List<CtMethod> allmethods=getAllMethodsFromClass();
		List<CtVariable> variables = VariableResolver.searchVariablesInScope(this.element);

		if(this.element instanceof CtInvocation) {
			CtInvocation invocation = (CtInvocation)(this.element);
			List<CtExpression<?>> experlistoriginal=invocation.getArguments();

			for(int index0=0;index0<invocationsFromClass.size();index0++) {
				
				CtInvocation understudy=invocationsFromClass.get(index0);
				if(understudy.getExecutable().getSimpleName().equals(invocation.getExecutable().getSimpleName()) &&
						invocation.getArguments().size()!=understudy.getArguments().size()) {
					List<String> argument = new ArrayList<String>();
					List<CtExpression<?>> experlist=understudy.getArguments();
					int size=experlist.size();
					this.returnednewcode.add(understudy.toString());

					for(int i=0; i<size; i++) {
						CtTypeReference type = experlist.get(i).getType();
						boolean found=false;
						for(int j=0; j<experlistoriginal.size(); j++) {
							CtTypeReference typeoriginal = experlistoriginal.get(j).getType();
                            if(compareTypes(type, typeoriginal)) {
                            	argument.add(experlistoriginal.get(j).toString());
                            	found=true;
                            	break;
                            }	
						}
						
						if(!found) {
							for(int indexinner=0; indexinner<variables.size(); indexinner++) {
								CtVariable varinscope=variables.get(indexinner);
								CtTypeReference typevar=varinscope.getType();
								if(compareTypes(type, typevar)) {
	                            	argument.add(varinscope.toString());
	                            	break;
	                            }
							}
						}
					}
					
					String invocationstring=understudy.getExecutable().getSimpleName()+"(";
					
					for(int arguindex=0; arguindex<argument.size();arguindex++) {
						invocationstring+=argument.get(arguindex);
						if(arguindex!=(argument.size()-1))
							invocationstring+=",";
					}
					invocationstring+=")";
					
					this.returnednewcode.add(invocationstring);
				} else {
					if(invocation.getArguments().size()==understudy.getArguments().size()) {
						String commonchars= commonChars(invocation.getExecutable().getSimpleName(), 
								understudy.getExecutable().getSimpleName());
						String originalname=invocation.getExecutable().getSimpleName();
						double ratio=0.0;
						if(originalname.length()>0)
							ratio=Double.valueOf(commonchars.length())/Double.valueOf(originalname.length());
						
						List<String> argument = new ArrayList<String>();
						if(ratio>0.3) {
							for(int j=0; j<experlistoriginal.size(); j++) {
	                            argument.add(experlistoriginal.get(j).toString());
							}
							
							String invocationstring=understudy.getExecutable().getSimpleName()+"(";
							for(int arguindex=0; arguindex<argument.size();arguindex++) {
								invocationstring+=argument.get(arguindex);
								if(arguindex!=(argument.size()-1))
									invocationstring+=",";
							}
							invocationstring+=")";	
							this.returnednewcode.add(invocationstring);
						}	
					}
				}
			}
			
			for(int index1=0;index1<allmethods.size();index1++) {
				CtMethod understudy=allmethods.get(index1);
				
				if(understudy.getSimpleName().equals(invocation.getExecutable().getSimpleName())&&
						invocation.getArguments().size()!=understudy.getParameters().size()) {
					int size=understudy.getParameters().size();
					List<String> argument = new ArrayList<String>();
					List<CtParameter<?>> experlist=understudy.getParameters();

					for(int i=0; i<size; i++) {
						CtTypeReference type = experlist.get(i).getType();
						boolean found=false;
						for(int j=0; j<experlistoriginal.size(); j++) {
							CtTypeReference typeoriginal = experlistoriginal.get(j).getType();
                            if(compareTypes(type, typeoriginal)) {
                            	argument.add(experlistoriginal.get(j).toString());
                            	found=true;
                            	break;
                            }	
						}
						
						if(!found) {
							for(int indexinner=0; indexinner<variables.size(); indexinner++) {
								CtVariable varinscope=variables.get(indexinner);
								CtTypeReference typevar=varinscope.getType();
								if(compareTypes(type, typevar)) {
	                            	argument.add(varinscope.toString());
	                            	break;
	                            }
							}
						}
					}
					
					String invocationstring=understudy.getSimpleName()+"(";
					
					for(int arguindex=0; arguindex<argument.size();arguindex++) {
						invocationstring+=argument.get(arguindex);
						if(arguindex!=(argument.size()-1))
							invocationstring+=",";
					}
					invocationstring+=")";	
					this.returnednewcode.add(invocationstring);
				} else {
					if(invocation.getArguments().size()==understudy.getParameters().size()) {
						String commonchars= commonChars(invocation.getExecutable().getSimpleName(), 
								understudy.getSimpleName());
						String originalname=invocation.getExecutable().getSimpleName();
						double ratio=0.0;
						if(originalname.length()>0)
							ratio=Double.valueOf(commonchars.length())/Double.valueOf(originalname.length());
						
						List<String> argument = new ArrayList<String>();
						if(ratio>0.3) {
							for(int j=0; j<experlistoriginal.size(); j++) {
	                            argument.add(experlistoriginal.get(j).toString());
							}
							
							String invocationstring=understudy.getSimpleName()+"(";
							for(int arguindex=0; arguindex<argument.size();arguindex++) {
								invocationstring+=argument.get(arguindex);
								if(arguindex!=(argument.size()-1))
									invocationstring+=",";
							}
							invocationstring+=")";	
							this.returnednewcode.add(invocationstring);
						}	
					}
				}
			}	
		} else if(this.element instanceof CtConstructorCall) {

			CtConstructorCall invocation = (CtConstructorCall)(this.element);
			List<CtExpression<?>> experlistoriginal=invocation.getArguments();
			
			for(int index0=0;index0<constructorcallsFromClass.size();index0++) {
				CtConstructorCall understudy=constructorcallsFromClass.get(index0);
				
				if(understudy.getExecutable().getSimpleName().equals(invocation.getExecutable().getSimpleName())&&
						invocation.getArguments().size()!=understudy.getArguments().size()) {
					List<String> argument = new ArrayList<String>();
					List<CtExpression<?>> experlist=understudy.getArguments();
					int size=experlist.size();

					for(int i=0; i<size; i++) {
						CtTypeReference type = experlist.get(i).getType();
						boolean found=false;
						for(int j=0; j<experlistoriginal.size(); j++) {
							CtTypeReference typeoriginal = experlistoriginal.get(j).getType();
                            if(compareTypes(type, typeoriginal)) {
                            	argument.add(experlistoriginal.get(j).toString());
                            	found=true;
                            	break;
                            }	
						}
						
						if(!found) {
							for(int indexinner=0; indexinner<variables.size(); indexinner++) {
								CtVariable varinscope=variables.get(indexinner);
								CtTypeReference typevar=varinscope.getType();
								if(compareTypes(type, typevar)) {
	                            	argument.add(varinscope.toString());
	                            	break;
	                            }
							}
						}
					}
					
					String invocationstring=understudy.getExecutable().getSimpleName()+"(";
					
					for(int arguindex=0; arguindex<argument.size();arguindex++) {
						invocationstring+=argument.get(arguindex);
						if(arguindex!=(argument.size()-1))
							invocationstring+=",";
					}
					invocationstring+=")";
					
					this.returnednewcode.add(invocationstring);
				}
			}
		}		
	}
}
