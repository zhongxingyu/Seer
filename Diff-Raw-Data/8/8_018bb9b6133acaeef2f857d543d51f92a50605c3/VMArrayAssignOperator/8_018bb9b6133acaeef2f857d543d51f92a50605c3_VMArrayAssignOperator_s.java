 package cz.cvut.fit.mirun.lemavm.assignment;
 
 import cz.cvut.fit.mirun.lemavm.exceptions.VMEvaluationException;
 import cz.cvut.fit.mirun.lemavm.structures.VMArray;
 import cz.cvut.fit.mirun.lemavm.structures.builtin.VMString;
 import cz.cvut.fit.mirun.lemavm.structures.classes.VMEnvironment;
 import cz.cvut.fit.mirun.lemavm.structures.operators.VMArrayAccessOperator;
import cz.cvut.fit.mirun.lemavm.structures.operators.VMOperator;
 import cz.cvut.fit.mirun.lemavm.utils.ParsingUtils;
 
 public final class VMArrayAssignOperator extends VMAssignOperator {
 
 	private final Object index;
 	private final Object value;
 
 	public VMArrayAssignOperator(String name, String type, Object index,
 			Object value) {
 		super(name, type, false);
 		if (index == null || value == null) {
 			throw new NullPointerException();
 		}
 		this.index = index;
 		this.value = value;
 	}
 
 	@Override
 	public Object evaluate(VMEnvironment env) {
 		final VMArray arr = env.getBinding(name, VMArray.class);
 		if (arr == null) {
 			throw new VMEvaluationException("Array with name " + name
 					+ " not found.");
 		}
 		final int ind = VMArrayAccessOperator.resolveArrayIndex(index, env);
 		Object val = resolveValue(env);
 		arr.set(ind, val);
 		return null;
 	}
 
 	private Object resolveValue(VMEnvironment env) {
 		Object val = null;
		if (value instanceof VMOperator) {
			val = ((VMOperator) value).evaluate(env);
 			return val;
 		} else if (value instanceof String) {
 			final String str = (String) value;
 			if (str.startsWith("\"")) {
 				val = new VMString(str.substring(1, str.length() - 1));
 				return val;
 			}
 			final Boolean b = ParsingUtils.tryParsingBoolean(str);
 			if (b != null) {
 				val = b;
 				return val;
 			}
 			final Number n = ParsingUtils.tryParsingNumber(str);
 			if (n != null) {
 				val = n;
 				return val;
 			}
 			val = env.getBinding(str, Object.class);
 			if (val == null) {
 				throw new VMEvaluationException("No variable with name " + str
 						+ " found in this environment.");
 			}
 		} else {
 			val = value;
 		}
 		return value;
 	}
 }
