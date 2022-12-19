package synthesis.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.declaration.CtClass;

public class SynthesisProcessor extends AbstractProcessor<CtClass>{

	private List<Pair<CtElement, String>> repairinfo=new ArrayList<Pair<CtElement, String>>();
	
	private List<CtElement> studyelement =new ArrayList<CtElement>();

    public SynthesisProcessor (List<Pair<CtElement, String>> detailedinfoforrepair) {
        this.repairinfo = detailedinfoforrepair;        
    }

    public CtElement getelement(CtElement in) {
    	for(int index=(studyelement.size()-1); index>=0; index--) {
    		if(studyelement.get(index).toString().equals(in.toString()))
    			return studyelement.get(index);
    	}
    	
    	return null;
    }
	@Override
	public void process(CtClass classinstudy) { 
		
        List<CtElement> ctelementlist = classinstudy.getElements(new TypeFilter(CtElement.class));
        for(int index=0; index< ctelementlist.size(); index++) {

        	if(whetherprocess(ctelementlist.get(index))) {

        		if(!(ctelementlist.get(index) instanceof CtFieldReference) && !(ctelementlist.get(index) instanceof CtTypeReference)
        			&& !(ctelementlist.get(index) instanceof CtVariableReference))
        		    studyelement.add(ctelementlist.get(index));
        	}
        }   
        
		for(int index=0; index<repairinfo.size(); index++) {
			
			Pair<CtElement, String> certainrepairinfo = repairinfo.get(index);
			CtElement left=certainrepairinfo.getLeft();
			String right=certainrepairinfo.getRight();
			CtElement candidate=getelement(left);

//				if(left instanceof CtTypeAccess) {
////					CtClass t = getFactory().Core().createClass();
////					t.setSimpleName(right);
////					candidate.replace(t.getReference());
//					CtTypeParameterReference ref = getFactory().Core().createTypeParameterReference();
//					ref.setSimpleName(right);
//					candidate.replace(ref);
//
//				}  else {
			if(candidate!=null) {
			 CtCodeSnippetExpression snippet = getFactory().Core().createCodeSnippetExpression();
		     snippet.setValue(right);
		     candidate.replace(snippet);
			}
			//	}
			
		}
	}
	
	public boolean whetherprocess(CtElement element){

          for(int index=0; index<repairinfo.size(); index++) {
			 Pair<CtElement, String> certainrepairinfo = repairinfo.get(index);
			 CtElement left=certainrepairinfo.getLeft();
			 if(whtherProcessElement(element, left)) {
				 return true;
			 }
          }     
          return false;
    }
	
	public boolean whtherProcessElement(CtElement elementinfile, CtElement elementserached) {
		
		if(elementinfile.toString().equals(elementserached.toString())) {
		
		   if(elementinfile.getPosition() instanceof NoSourcePosition || 
				elementserached.getPosition() instanceof NoSourcePosition) {
			// We find the parent method and we extract the parameters
			  CtMethod method0 = elementinfile.getParent(CtMethod.class);
			  CtMethod method1 = elementserached.getParent(CtMethod.class);
			  if (method0 != null && method1!=null) {
				 if(method0.toString().equals(method1.toString()))
						return true;
			  } else {
				 CtConstructor ctconstructor0 = elementinfile.getParent(CtConstructor.class); 
				 CtConstructor ctconstructor1 = elementserached.getParent(CtConstructor.class); 
				 
	             if(ctconstructor0!=null && ctconstructor1!=null) {
	                if(ctconstructor0.toString().equals(ctconstructor1.toString())) {
						return true;
	                }
	             }
			  }
			  return false;	
		   }
		
		   if(elementinfile.getPosition().getLine() == elementserached.getPosition().getLine()) {
			  if (elementserached.getParent()!=null && elementinfile.getParent()!=null) {
				  if(elementserached.getParent().toString().equals(elementinfile.getParent().toString())) {
					return true;
				 } else {
					return false;
				 }
			    } else {
				return false;
			  }
	  	     } else {
			   return false;
		     }
		  } else {
			return false;
		 }
	 }
 }
