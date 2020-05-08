 package cz.cuni.mff.d3s.spl.core.impl.formula;
 
 import cz.cuni.mff.d3s.spl.core.Data;
 import cz.cuni.mff.d3s.spl.core.Formula;
 import cz.cuni.mff.d3s.spl.core.MathematicalInterpretation;
 import cz.cuni.mff.d3s.spl.core.Result;
 
 public class Comparison implements Formula {
 	public enum Operator {
 		LT,
 		GT
 	}
 	
 	private class NamedDataSource {
 		public Data data;
 		public String name;
 		public NamedDataSource(String name) {
 			this.name = name;
 		}
		public void bind(String sourceName, Data source) {
			if (name.equals(sourceName)) {
				data = source;
 			}
 		}
 		public boolean valid() {
 			return data != null;
 		}
 	}
 	
 	private NamedDataSource left;
 	private NamedDataSource right;
 	private Operator operator;
 	private MathematicalInterpretation interpretation;
 	
 	public Comparison(String left, String right, Operator op) {
 		this.left = new NamedDataSource(left);
 		this.right = new NamedDataSource(right);
 		operator = op;
 	}
 	
 	@Override
 	public void setInterpreation(MathematicalInterpretation interpretation) {
 		this.interpretation = interpretation;
 	}
 
 	@Override
 	public void bind(String variable, Data data) {
 		left.bind(variable, data);
 		right.bind(variable, data);
 	}
 
 	@Override
 	public Result evaluate() {
 		if (!left.valid() || !right.valid()) {
 			// TODO: throw exception?
 			return Result.CANNOT_COMPUTE;
 		}
 		
 		switch (operator) {
 		case LT:
 			return interpretation.isGreaterThan(right.data.getStatisticSnapshot(), left.data.getStatisticSnapshot());
 		case GT:
 			return interpretation.isGreaterThan(left.data.getStatisticSnapshot(), right.data.getStatisticSnapshot());
 		default:
 			assert false : "Unreachable branch reached :-(.";
 		}
 		// Make the compiler happy.
 		return Result.CANNOT_COMPUTE;
 	}
 
 }
