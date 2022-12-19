package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

public class ConstantRepSynthesizer extends AbstractSynthesizer {

	public ConstantRepSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {
		
		CtClass parentClass = element.getParent(CtClass.class);
		List<CtLiteral> literalsFromClass = new ArrayList();
		if(parentClass!=null)
			  literalsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());
		
		String literaltype = getLiteralType((CtLiteral)(this.element));
		if (literalsFromClass.size() > 0) {
			for (CtLiteral literalFormFaulty : literalsFromClass) {
				
				String literalinclass = getLiteralType(literalFormFaulty);	
				if(literalinclass.equals(literaltype)) {
					if(literaltype.equals("char"))
					    this.returnednewcode.add("'"+literalFormFaulty.toString()+"'");
					else if(literaltype.equals("string"))
						this.returnednewcode.add("\""+literalFormFaulty.toString()+"\"");
					else this.returnednewcode.add(literalFormFaulty.toString());
				}
			}
		}
	}
	
	public static String getLiteralType(CtLiteral inputLiteral) {
		
	    String literaltype="";
		if(inputLiteral.toString().trim().startsWith("'")) {
			literaltype="char";
		}
		else if(inputLiteral.toString().trim().startsWith("\"")) {
			literaltype="string";
		}
		else if(inputLiteral.toString().indexOf("null")!=-1) {
			literaltype="null"; //ignore
		}
		else {
			if(inputLiteral.getValue().toString().equals("true")||inputLiteral.getValue().toString().equals("false")) {
				literaltype = "boolean";
			}
			else  {
				literaltype ="numerical";
			}
	    }
		return literaltype;
    }
}
