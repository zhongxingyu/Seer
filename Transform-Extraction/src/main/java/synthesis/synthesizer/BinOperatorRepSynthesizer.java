package synthesis.synthesizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;

public class BinOperatorRepSynthesizer extends AbstractSynthesizer {

    List<BinaryOperatorKind> logicalOperator = new LinkedList<BinaryOperatorKind>(
    		Arrays.asList(BinaryOperatorKind.OR, BinaryOperatorKind.AND));
    
	List<BinaryOperatorKind> bitOperator = new LinkedList<BinaryOperatorKind>(Arrays.asList(BinaryOperatorKind.BITOR, BinaryOperatorKind.BITXOR,
			BinaryOperatorKind.BITAND));
	
	List<BinaryOperatorKind> compareOperator = new LinkedList<BinaryOperatorKind>(Arrays.asList(BinaryOperatorKind.EQ, BinaryOperatorKind.NE,
			BinaryOperatorKind.LT, BinaryOperatorKind.GT, BinaryOperatorKind.LE, BinaryOperatorKind.GE));
	
	List<BinaryOperatorKind> shiftOperator = new LinkedList<BinaryOperatorKind>(Arrays.asList(BinaryOperatorKind.SL, BinaryOperatorKind.SR,
			BinaryOperatorKind.USR));
	
	List<BinaryOperatorKind> mathOperator = new LinkedList<BinaryOperatorKind>(Arrays.asList(BinaryOperatorKind.PLUS, BinaryOperatorKind.MINUS,
			BinaryOperatorKind.MUL, BinaryOperatorKind.DIV, BinaryOperatorKind.MOD));
	
	List<BinaryOperatorKind> returnedoperator = new ArrayList<BinaryOperatorKind>();
	CtExpression left;
	CtExpression right;
	
	public BinOperatorRepSynthesizer(CtElement studyelement, TargetedRepairTran transform) {
		super(studyelement, transform);
	}
	
	@Override
	public void synthesize() {
		
		BinaryOperatorKind operatorkind = ((CtBinaryOperator)(this.element)).getKind();
		left=((CtBinaryOperator)(this.element)).getLeftHandOperand();
		right=((CtBinaryOperator)(this.element)).getRightHandOperand();

		if(logicalOperator.contains(operatorkind)) {
			 logicalOperator.remove(operatorkind);
			 returnedoperator=logicalOperator;
		} else if (bitOperator.contains(operatorkind)) {
			 bitOperator.remove(operatorkind);
			 returnedoperator=bitOperator;
		} else if (compareOperator.contains(operatorkind)) {
			 compareOperator.remove(operatorkind);
			 returnedoperator=compareOperator;
		} else if (shiftOperator.contains(operatorkind)) {
			shiftOperator.remove(operatorkind);
			 returnedoperator=shiftOperator;
		} else if (mathOperator.contains(operatorkind)) {
			mathOperator.remove(operatorkind);
			 returnedoperator=mathOperator;
		} else {};	
		
		transformToString(returnedoperator);
	}
	
	public void transformToString(List<BinaryOperatorKind> returnedoperator) {
		
		for(int index=0; index<returnedoperator.size(); index++) {
			BinaryOperatorKind operatorkind=returnedoperator.get(index);
			String returnedstring = getStringForOperator(operatorkind);
			if(!returnedstring.isEmpty())
				returnednewcode.add(returnedstring);
		}
	}
	
	public String getStringForOperator(BinaryOperatorKind operatorkind) {
		
		if(operatorkind==BinaryOperatorKind.OR)
			return left.toString()+"||"+right.toString();
		else if (operatorkind==BinaryOperatorKind.AND)
			return left.toString()+"&&"+right.toString();
		else if (operatorkind==BinaryOperatorKind.BITOR)
			return left.toString()+"|"+right.toString();
		else if (operatorkind==BinaryOperatorKind.BITXOR)
			return left.toString()+"^"+right.toString();
		else if (operatorkind==BinaryOperatorKind.BITAND)
			return left.toString()+"&"+right.toString();
		else if (operatorkind==BinaryOperatorKind.EQ)
			return left.toString()+"=="+right.toString();
		else if (operatorkind==BinaryOperatorKind.NE)
			return left.toString()+"!="+right.toString();
		else if (operatorkind==BinaryOperatorKind.LT)
			return left.toString()+"<"+right.toString();
		else if (operatorkind==BinaryOperatorKind.GT)
			return left.toString()+">"+right.toString();
		else if (operatorkind==BinaryOperatorKind.LE)
			return left.toString()+"<="+right.toString();
		else if (operatorkind==BinaryOperatorKind.GE)
			return left.toString()+">="+right.toString();
		else if (operatorkind==BinaryOperatorKind.SL)
			return left.toString()+"<<"+right.toString();
		else if (operatorkind==BinaryOperatorKind.SR)
			return left.toString()+">>"+right.toString();
		else if (operatorkind==BinaryOperatorKind.USR)
			return left.toString()+">>>"+right.toString();
		else if (operatorkind==BinaryOperatorKind.PLUS)
			return left.toString()+"+"+right.toString();
		else if (operatorkind==BinaryOperatorKind.MINUS)
			return left.toString()+"-"+right.toString();
		else if (operatorkind==BinaryOperatorKind.MUL)
			return left.toString()+"*"+right.toString();
		else if (operatorkind==BinaryOperatorKind.DIV)
			return left.toString()+"/"+right.toString();
		else if (operatorkind==BinaryOperatorKind.MOD)
			return left.toString()+"%"+right.toString();
		else if (operatorkind==BinaryOperatorKind.INSTANCEOF)
			return left.toString()+" instanceof "+right.toString();
		else return "";
	}
}
