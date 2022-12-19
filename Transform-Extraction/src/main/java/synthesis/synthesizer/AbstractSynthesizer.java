package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public abstract class AbstractSynthesizer {

	protected CtElement element;
	protected TargetedRepairTran tran;
	
	ArrayList<String> returnednewcode = new ArrayList<String>();

	AbstractSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		this.element=studyelement;
		this.tran=transform;
	}
	
	public abstract void synthesize();
	
	public ArrayList<String> getNewCode() {
		
		Set<String> set = new HashSet<>(this.returnednewcode);
		this.returnednewcode.clear();
		this.returnednewcode.addAll(set);
		
		return this.returnednewcode;
	} 
	
	public void clearNewCode() {
		 this.returnednewcode.clear();
	} 
	
	public String commonChars(String str1, String str2) {
		 
		if (str1.length() > 0 & str2.length() > 0) {
			List<Character> s1 = new ArrayList<>();
			List<Character> s2 = new ArrayList<>();
 
			for (int i = 0; i < str1.length(); i++) {
				s1.add(str1.charAt(i));
			}
 
			for (int i = 0; i < str2.length(); i++) {
				s2.add(str2.charAt(i));
			}
 
			s1.retainAll(s2);
 
			StringBuilder sb = new StringBuilder();
 
			for (Character c : s1) {
				sb.append(c);
			}
			return sb.toString();
		} else
			return "";
	}
	
	public List<CtTypeReference> inferPotentionalTypes (CtInvocation ainvocation, CtClass parentclass) {

		List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();

		try {
			List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> 
			(e instanceof CtBinaryOperator)).stream()
					.map(CtBinaryOperator.class::cast).collect(Collectors.toList());		
			
			inferredpotentionaltypes.clear();
               
			CtTypeReference inferredtype = null;
			if(ainvocation.getType()==null) {
					for(CtBinaryOperator certainbinary: binaryOperatorInClass) {
						if(certainbinary.getLeftHandOperand() instanceof CtInvocation) {

							CtInvocation anotherinvocation=(CtInvocation)certainbinary.getLeftHandOperand();
							if(anotherinvocation.getExecutable().getSignature().equals(ainvocation.getExecutable().getSignature())
									&& certainbinary.getRightHandOperand().getType()!=null) {

								 inferredtype=certainbinary.getRightHandOperand().getType();
								 inferredpotentionaltypes.add(inferredtype);
								 break;
							}
						}
						
						if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
							CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
							if(anotherinvocation.getExecutable().getSignature().equals(ainvocation.getExecutable().getSignature())
									&& certainbinary.getLeftHandOperand().getType()!=null) {
								 inferredtype=certainbinary.getLeftHandOperand().getType();
								 inferredpotentionaltypes.add(inferredtype);
								 break;
							}
						}
				 }	
			} 		
		  } catch (Exception ex) {
		}
		
		return inferredpotentionaltypes;
     } 
	
	public List<CtTypeReference> inferPotentionalTypes (CtTypeAccess typeaccess, CtClass parentclass) {

		List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();

		try {
			List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> 
			(e instanceof CtBinaryOperator)).stream()
					.map(CtBinaryOperator.class::cast).collect(Collectors.toList());		
			
			inferredpotentionaltypes.clear();
               
			CtTypeReference inferredtype = null;
			
			for(CtBinaryOperator certainbinary: binaryOperatorInClass) {
				  
				if(certainbinary.getLeftHandOperand().getShortRepresentation().equals(typeaccess.getShortRepresentation())) {
					 inferredtype=certainbinary.getRightHandOperand().getType();
				     if(inferredtype!=null)
					     inferredpotentionaltypes.add(inferredtype);
				}
						
				if(certainbinary.getRightHandOperand().getShortRepresentation().equals(typeaccess.getShortRepresentation())) {
					 inferredtype=certainbinary.getLeftHandOperand().getType();
					 if(inferredtype!=null)
						 inferredpotentionaltypes.add(inferredtype);
				}
			 }			
		  } catch (Exception ex) {
		}
		
		return inferredpotentionaltypes;
     } 

	public boolean compareTypes(CtTypeReference t1, CtTypeReference t2) {
		try {		
			return t1 != null && t2 != null && (t1.toString().equals(t2.toString()) || 
					t1.toString().toLowerCase().endsWith(t2.toString().toLowerCase()) ||
					t2.toString().toLowerCase().endsWith(t1.toString().toLowerCase()) ||
					t1.equals(t2) || t1.isSubtypeOf(t2) || t2.isSubtypeOf(t1));
		} catch (Exception e) {
			return false;
		}
	}
	
    public boolean compareInferredTypes(CtTypeReference t1, List<CtTypeReference> potentionaltypes) {
		
		for(int i=0; i<potentionaltypes.size(); i++) {
			if(compareTypes(t1, potentionaltypes.get(i)))
				return true;
		}    
		return false;
	}
    
    protected List<CtInvocation> getInvocationsFromClass () {
    	CtClass parentClass=element.getParent(CtClass.class);
    	List<CtInvocation> invocationsFromClass;
    	
    	if(parentClass!=null)
    		invocationsFromClass = parentClass.getElements(e -> (e instanceof CtInvocation)).stream()
			.map(CtInvocation.class::cast).collect(Collectors.toList());
    	else invocationsFromClass = null;
    	
    	return invocationsFromClass;
	}
    
    protected List<CtConstructorCall> getConstructorcallsFromClass () {
	   CtClass parentClass=element.getParent(CtClass.class);
	   List<CtConstructorCall> constructorcallsFromClass;
    	
    	if(parentClass!=null)
    		constructorcallsFromClass = parentClass.getElements(e -> (e instanceof CtConstructorCall)).stream()
			.map(CtConstructorCall.class::cast).collect(Collectors.toList());
    	else constructorcallsFromClass = null;
    	
    	return constructorcallsFromClass;
	}
   
    protected List<CtMethod> getAllMethodsFromClass () {
	   CtClass parentClass=element.getParent(CtClass.class);
	   List<CtMethod> allmethods = new ArrayList<CtMethod>();
    	
	   try {
		    allmethods.addAll(parentClass.getAllMethods());
			if (parentClass != null && parentClass.getParent() instanceof CtClass) {
				CtClass parentParentClass = (CtClass) parentClass.getParent();
				allmethods.addAll(parentParentClass.getAllMethods());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allmethods;
	}
}
