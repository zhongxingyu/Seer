 package base.hldd.structure.models.utils;
 
 import base.*;
 import base.hldd.structure.nodes.Node;
 import base.hldd.structure.nodes.utils.Condition;
 import base.hldd.structure.variables.*;
import base.hldd.structure.variables.Variable;
 import base.hldd.visitors.DependentVariableReplacer;
 import base.vhdl.structure.*;
 import parsers.vhdl.PackageParser;
 
 import java.math.BigInteger;
 import java.util.*;
 
 /**
  * @author Anton Chepurov
  */
 public class ModelManager implements TypeResolver {
 
 	private VariableManager variableManager;
 
 	private ConstantVariable constant0;
 	private ConstantVariable constant1;
 
 	public ModelManager() {
 		variableManager = new VariableManager();
 	}
 
 	public void addVariable(AbstractVariable newVariable) {
 		variableManager.addVariable(newVariable);
 	}
 
 	public void addVariable(String varName, AbstractVariable newVariable) {
 		variableManager.addVariable(varName, newVariable);
 	}
 
 	public void removeVariable(AbstractVariable variableToRemove) {
 		variableManager.removeVariable(variableToRemove);
 	}
 
 	public ConstantVariable getConstant0() throws Exception {
 		if (constant0 == null) {
 			constant0 = (ConstantVariable) convertOperandToVariable(new OperandImpl("0"), Type.BIT_TYPE, false, null);
 		}
 		return constant0;
 	}
 
 	public ConstantVariable getConstant1() throws Exception {
 		if (constant1 == null) {
 			constant1 = (ConstantVariable) convertOperandToVariable(new OperandImpl("1"), Type.BIT_TYPE, false, null);
 		}
 		return constant1;
 	}
 
 	public void rebase(AbstractVariable variableToRebase, AbstractVariable newBaseVariable) {
 
 		/* Remove old variable from hash */
 		removeVariable(variableToRebase);
 		/* Remove base variable from hash */
 		removeVariable(newBaseVariable);
 
		if (newBaseVariable instanceof Variable) {
			((Variable) newBaseVariable).setDelay(variableToRebase.isDelay());
		}

 		/* Replace base variable */
 		((GraphVariable) variableToRebase).setBaseVariable(newBaseVariable);
 		/* ReAdd updated old variable to hash */
 		addVariable(variableToRebase);
 
 		replaceInModel(newBaseVariable, new RangeVariableHolder(variableToRebase, null));
 	}
 
 	public void replaceWithRange(AbstractVariable variableToReplace, RangeVariableHolder replacingVarHolder) {
 		AbstractVariable replacingVariable = replacingVarHolder.getVariable();
 
 		/* Remove old variable from hash */
 		removeVariable(variableToReplace);
 		/* Add new variable to hash */
 		addVariable(replacingVariable);
 
 		/* Replace old variable with new one in Functions and GraphVariables */
 		replaceInModel(variableToReplace, replacingVarHolder);
 	}
 
 	private void replaceInModel(AbstractVariable variableToReplace, RangeVariableHolder replacingVarHolder) {
 
 		boolean flattenToBits = (replacingVarHolder == null);
 		if (flattenToBits) {
 			checkBitVariablesAvailability(variableToReplace);
 		}
 
 		AbstractVariable replacingVariable = flattenToBits ? null : replacingVarHolder.getVariable();
 		/* Replace old variable with new one in Functions and GraphVariables */
 		for (AbstractVariable absVariable : variableManager.getVariables()) {
 
 			if (absVariable instanceof FunctionVariable) {
 				FunctionVariable functionVariable = (FunctionVariable) absVariable;
 				for (RangeVariableHolder operand : functionVariable.getOperands()) {
 					if (operand.getVariable() == variableToReplace) {
 						if (flattenToBits) {
 							replacingVariable = generateBitRangeVariable(variableToReplace, operand.getRange());
 							operand.setVariable(replacingVariable);
 							operand.setRange(null);
 						} else {
 							operand.setVariable(replacingVariable);
 							if (replacingVarHolder.isRange()) {
 								//todo: Range.absoluteFor()...
 								operand.setRange(replacingVarHolder.getRange());
 							}
 						}
 					}
 				}
 
 			} else if (absVariable instanceof GraphVariable) {
 				GraphVariable graphVariable = (GraphVariable) absVariable;
 				try {
 					graphVariable.traverse(flattenToBits ?
 							new DependentVariableReplacer.FlattenerToBits(variableToReplace, this) :
 							new DependentVariableReplacer(variableToReplace, replacingVarHolder)
 					);
 				} catch (Exception e) {
 					throw new RuntimeException("Error while traversing GraphVariable with DependentVariableReplacer: "
 							+ e.getMessage(), e); /* should never happen */
 				}
 			}
 		}
 	}
 
 	private void checkBitVariablesAvailability(AbstractVariable variableToReplace) {
 		for (int i = 0; i < variableToReplace.getLength().getHighest(); i++) {
 			String bitVariableName = generateBitVariableName(variableToReplace, new Range(i, i));
 			AbstractVariable bitVariable = getVariable(bitVariableName);
 			if (bitVariable == null) {
 				throw new RuntimeException("Cannot flatten variable " + variableToReplace.getName() +
 						" to bits. BIT RANGE variable " + bitVariableName + " is not found.");
 			}
 		}
 	}
 
 	public AbstractVariable generateBitRangeVariable(AbstractVariable wholeVariable, Range range) {
 		return getVariable(generateBitVariableName(wholeVariable, range));
 	}
 
 	private String generateBitVariableName(AbstractVariable wholeVariable, Range range) {
 
 		if (range == null) {
 			throw new RuntimeException("Cannot flatten variable " + wholeVariable.getName() +
 					" to bits. WHOLE variable is in use.");
 		}
 		if (range.length() != 1) {
 			throw new RuntimeException("Cannot flatten variable " + wholeVariable.getName() +
 					" to bits. NON-BIT range of the variable is in use: " + range);
 		}
 		return new OperandImpl(wholeVariable.getName(), range, false).toString();
 	}
 
 	public RangeVariableHolder convertConditionalStmt(Expression conditionalStmt, boolean flattenCondition, SourceLocation source) throws Exception {
 		boolean inverted = conditionalStmt.isInverted();
 
 		/* Create FUNCTION */
 		FunctionVariable functionVariable;
 		if (flattenCondition) {
 			functionVariable = createCompositeFunction(conditionalStmt, source);
 			if (functionVariable != null) {
 				return new RangeVariableHolder(functionVariable, null, adjustBooleanCondition(1, inverted));
 			}
 		}
 
 		functionVariable = createFunction(conditionalStmt, false, source);
 		return detectTrueValueAndSimplify(functionVariable, inverted); //todo: don't represent NOT as isInverted(). Use Function instead. And remove isInverted() parameter from here and further on. But think carefully: it may be a deeply internal INV in condition (there INV-s are preserved as functions)
 	}
 
 	/**
 	 * Both, obtains correct true value for the functionVariable (puts it into the returned object),
 	 * and tries to simplify boolean condition. Simplification is possible is EQ/NEQ compares
 	 * a variable to a 1-bit constant.
 	 *
 	 * @param functionVariable functions whose true value is to be detected
 	 * @param inverted		 is the source {@link base.vhdl.structure.Expression} is inverted
 	 * @return holder of a variable, its range and its true value
 	 * @throws HLDDException if CompositeFunctionVariable is specified as a parameter
 	 */
 	private RangeVariableHolder detectTrueValueAndSimplify(FunctionVariable functionVariable, boolean inverted) throws HLDDException {
 
 		if (functionVariable instanceof CompositeFunctionVariable) {
 			throw new HLDDException("ModelManager: detectTrueValueAndSimplify(): FunctionVariable is expected as a parameter. Received: CompositeFunctionVariable.");
 		}
 		RangeVariableHolder originalFunction = new RangeVariableHolder(functionVariable, null, adjustBooleanCondition(1, inverted));
 
 		Operator operator = functionVariable.getOperator();
 		List<RangeVariableHolder> operands = functionVariable.getOperands();
 
 		boolean isNEQ = operator == Operator.NEQ;
 		if (operator == Operator.EQ || isNEQ) {
 			ConstantVariable constantOperand;
 			RangeVariableHolder variableOperand;
 
 			RangeVariableHolder leftOp = operands.get(0);
 			RangeVariableHolder rightOp = operands.get(1);
 			if (rightOp.getVariable() instanceof ConstantVariable) {
 				constantOperand = (ConstantVariable) rightOp.getVariable();
 				variableOperand = leftOp;
 			} else if (leftOp.getVariable() instanceof ConstantVariable) {
 				constantOperand = (ConstantVariable) leftOp.getVariable();
 				variableOperand = rightOp;
 			} else {
 				return originalFunction; // not compared to a Constant
 			}
 
 			if (constantOperand.getLength().length() > 1) { // more than a single bit is used
 				return originalFunction;
 			}
 
 			// Remove EQ/NEQ FunctionVariable, for it is not needed
 			removeVariable(functionVariable);
 
 			int initialCondition = constantOperand.getValue().intValue();
 			return new RangeVariableHolder(variableOperand.getVariable(),
 					variableOperand.getRange(), adjustBooleanCondition(initialCondition, inverted ^ isNEQ));
 		} else {
 			return originalFunction;
 		}
 
 	}
 
 	public RangeVariableHolder extractBooleanDependentVariable(AbstractOperand abstractOperand, boolean useComposites) throws Exception {
 		if (abstractOperand instanceof Expression) {
 			return convertConditionalStmt((Expression) abstractOperand, useComposites, null);
 		} else if (abstractOperand instanceof OperandImpl) {
 			OperandImpl operand = (OperandImpl) abstractOperand;
 			return new RangeVariableHolder(getVariable(operand.getName()), operand.getRange(), getBooleanValueFromOperand(operand));
 		}
 		throw new Exception("Dependent variable is being extracted from : " + abstractOperand +
 				"\nCurrently extraction of Dependent Variables is only supported for " +
 				Expression.class.getSimpleName() + " and " + OperandImpl.class.getSimpleName());
 	}
 
 	private CompositeFunctionVariable createCompositeFunction(Expression condition, SourceLocation source) throws Exception {
 		if (!condition.isCompositeCondition()) {
 			return null;
 		}
 
 		Operator compositeOperator = condition.getOperator();
 		List<RangeVariableHolder> compositeElements = new LinkedList<RangeVariableHolder>();
 
 		for (AbstractOperand operand : condition.getOperands()) {
 			/* All operands of a CompositeCondition are Expressions, not OperandImpls */
 			if (operand instanceof Expression) {
 				Expression operandExpression = (Expression) operand;
 				compositeElements.add(convertConditionalStmt(operandExpression, false, source));
 			}
 		}
 
 		return new CompositeFunctionVariable(compositeOperator, compositeElements.toArray(new RangeVariableHolder[compositeElements.size()]));
 	}
 
 	/**
 	 * @param funcOperand  either Expression or inverted Operand to create a Function from
 	 * @param isTransition flag marks whether the expression originates from transition
 	 *                     (<code>true</code> value) or from condition (<code>false</code> value).
 	 *                     Depending on this, <i>inversion</i> in expression is either ignored (in case
 	 *                     of conditions), or treated as a Function (in case of transition).
 	 * @param source	   VHDL source
 	 * @return FunctionVariable which represents the specified operand
 	 * @throws Exception cause {@link #convertOperandToVariable(AbstractOperand, Type, boolean, SourceLocation) cause }
 	 */
 	private FunctionVariable createFunction(AbstractOperand funcOperand, boolean isTransition, SourceLocation source) throws Exception {
 		FunctionVariable functionVariable;
 
 		if (funcOperand instanceof OperandImpl) {
 			/* Check the operand to be inverted */
 			if (!funcOperand.isInverted())
 				throw new Exception("Function is being created from a non-inverted operand: " + funcOperand);
 			/* Here isTransition was previously false. True cannot be, actually. todo: WTF? See CRC.vhd, transition of ips_xfr_wait */
 			functionVariable = createInversionFunction(funcOperand, isTransition, source);
 
 		} else if (funcOperand instanceof Expression) {
 			Expression expression = (Expression) funcOperand;
 			List<RangeVariableHolder> rangeAssignmentVarList;
 			if (expression.isInverted() && isTransition) {
 				/* Create NOT function on the base of underlying function */
 				/* NOT (REG1 AND REG2) --- in condition NOT should be ignored
 														  (it's taken into account when obtaining
 														   condition value)
 								 * NOT (REG1 AND REG2) --- in transition NOT should be treated as a Function
 								 * */
 				functionVariable = createInversionFunction(expression, isTransition, source);
 
 			} else if (!(rangeAssignmentVarList = getRangeAssignmentVariablesFor(expression)).isEmpty()) {
 				functionVariable = doCreateFinalFunction(Operator.CAT, source,
 						rangeAssignmentVarList.toArray(new RangeVariableHolder[rangeAssignmentVarList.size()]));
 			} else {
 				/* Create and collect operands */
 				RangeVariableHolder[] operandsHolders = new RangeVariableHolder[expression.getOperands().size()];
 				if (isTransition) {
 					/* Transition */
 					int i = 0;
 					for (AbstractOperand operand : expression.getOperands()) {
 						operandsHolders[i++] = new RangeVariableHolder(convertOperandToVariable(operand, null,
 								isTransition, source),
 								operand.getRange());
 					}
 
 				} else {
 					/* CONDITION */
 					int i = 0;
 					AbstractVariable operandVariable;
 					Range operandRange;
 					for (AbstractOperand operand : expression.getOperands()) {
 						operandRange = operand.getRange();
 						if (operand instanceof Expression) {
 							/* Treat operand as condition and extract dependent variable from it */
 							RangeVariableHolder depVariableHolder = convertConditionalStmt((Expression) operand, false, source);
 							operandVariable = getIdenticalVariable(depVariableHolder.getVariable());
 							operandRange = depVariableHolder.getRange();
 							/* If extracted dependent variable is OperandImpl or an inverted Expression,
 							* then take into account its value (e.g. cs_read='0' -> "cs_read" with true_value = 0).
 							* In Functions, if some operand is inversed or with true_value = 0, then all we can do
 							* is to substitute this operand with its INV function. */
 							if (depVariableHolder.getTrueValue() != 1) {
 								/* Create INV function */
 								operandVariable = getIdenticalVariable(
 										doCreateFinalInversionFunction(operandVariable, operandRange, source));
 							}
 						} else if (operand instanceof OperandImpl) {
 							operandVariable = convertOperandToVariable(operand, null, false, source);
 						} else throw new Exception("Unexpected situation while creating function: " +
 								"operand is neither " + Expression.class.getSimpleName() +
 								" nor " + OperandImpl.class.getSimpleName());
 						operandsHolders[i++] = new RangeVariableHolder(operandVariable, operandRange);
 					}
 
 				}
 				functionVariable = doCreateFinalFunction(expression.getOperator(), source, operandsHolders);
 
 			}
 
 
 		} else {
 			throw new Exception("Unexpected situation while creating function: " +
 					"funcOperand is neither " + Expression.class.getSimpleName() +
 					" nor " + OperandImpl.class.getSimpleName());
 		}
 
 
 		/* Search for existent identical one */
 		return (FunctionVariable) getIdenticalVariable(functionVariable);
 	}
 
 	private FunctionVariable doCreateFinalInversionFunction(AbstractVariable operandVariable, Range operandRange, SourceLocation source) {
 		/* Create Function */
 		FunctionVariable invFunctionVariable = new FunctionVariable(Operator.INV, generateFunctionNameIdx(Operator.INV));
 		invFunctionVariable.setSource(source);
 		/* Add a single operand */
 		try {
 			invFunctionVariable.addOperand(operandVariable, operandRange);
 		} catch (Exception e) {
 			throw new RuntimeException("Unexpected exception: " +
 					"Cannot add a single operand (" + operandVariable + ") to " + Operator.INV + " Function");
 		}
 		return invFunctionVariable;
 	}
 
 	private FunctionVariable doCreateFinalFunction(Operator operator, SourceLocation source, RangeVariableHolder... operandVariables) throws Exception {
 		/* Create new Function Variable */
 		FunctionVariable functionVariable = new FunctionVariable(operator, generateFunctionNameIdx(operator));
 		functionVariable.setSource(source);
 		/* Add operand variables one by one to the new Function Variable */
 		for (RangeVariableHolder operandVarHolder : operandVariables) {
 			try {
 				functionVariable.addOperand(operandVarHolder.getVariable(), operandVarHolder.getRange());
 			} catch (Exception e) {
 				if (!e.getMessage().startsWith(FunctionVariable.FAILED_ADD_OPERAND_TEXT))
 					throw e;
 				/* If Function Variable cannot accept operands anymore, add this Function Variable (1) as an
 				* operand to the new Function Variable (2), replace the link of the Function Variable being
 				* created to the new Function Variable (2) and proceed with adding operands, this time - to
 				* the new Function Variable (2). */
 
 				/* Tune so far created functionVariable (1) and add it to collector */
 				tuneFunctionVariable(functionVariable);
 				functionVariable = (FunctionVariable) getIdenticalVariable(functionVariable);
 				/* Create new Function */
 				FunctionVariable newFunction = new FunctionVariable(operator, generateFunctionNameIdx(operator));
 				newFunction.setSource(source);
 				/* Make the previously created function an operand of the new one */
 				newFunction.addOperand(functionVariable, null);
 				/* Make the operand that failed to be added an operand of the new function */
 				newFunction.addOperand(operandVarHolder.getVariable(), operandVarHolder.getRange());
 				/* Change the link of functionVariable to the new one */
 				functionVariable = newFunction;
 			}
 		}
 		/* Tune functionVariable */
 		tuneFunctionVariable(functionVariable);
 		return functionVariable;
 	}
 
 	/**
 	 * Method performs additional manipulations with function parameters.
 	 * Manipulations include:
 	 * <br>- Replacing DIVISION and MULTIPLICATION by power of 2 with SHIFTS
 	 * <br>- Deciding between SIGNED and UNSIGNED modifications of the 4
 	 * comparison operators: GT / U_GT, LT / U_LT, GE / U_GE, LE / U_LE.
 	 *
 	 * @param functionVariable variable to tune
 	 * @throws Exception {@link #convertOperandToVariable(AbstractOperand, Type, boolean, SourceLocation)}.
 	 */
 	private void tuneFunctionVariable(FunctionVariable functionVariable) throws Exception {
 		/* Replace DIVISION and MULTIPLICATION by power of 2 with SHIFTS */
 		Operator operator = functionVariable.getOperator();
 		List<RangeVariableHolder> operandHolders = functionVariable.getOperands();
 		ValueAndIndexHolder powerOf2Constant;
 		//todo: temporarily comment this for Toha (was if (false && ...))
 		if (/*false && */(operator == Operator.MULT || operator == Operator.DIV)
 				&& (powerOf2Constant = findPowerOf2Constant(operandHolders)) != null) {
 			/* Substitute operator with SHIFT (mult -> SHIFT_LEFT, div -> SHIFT_RIGHT): */
 			functionVariable.setOperator(operator == Operator.MULT ? Operator.SHIFT_LEFT : Operator.SHIFT_RIGHT);
 			functionVariable.setNameIdx(generateFunctionNameIdx(functionVariable.getOperator()));
 			/* Create SHIFT step operand */
 			AbstractVariable shiftStepOpeVariable = variableManager.getConstantByValue(powerOf2Constant.value);
 			/* Get the operand being shifted. Here assume that MULT and DIV have 2 operands only. */
 			RangeVariableHolder shiftedOperandHolder = operandHolders.get(invertBit(powerOf2Constant.index));
 			/* Set the shiftedOperand as left operand */
 			functionVariable.setOperand(0, shiftedOperandHolder);
 			/* Set the shiftStepOpeVariable as right operand */
 			functionVariable.setOperand(1, new RangeVariableHolder(shiftStepOpeVariable, null));
 		}
 
 		/* Decide between SIGNED and UNSIGNED modifications of comparison operators */
 		if (operandHolders.size() == 2 && !operandHolders.get(0).getVariable().isSigned()
 				&& !operandHolders.get(1).getVariable().isSigned()) {
 			Operator wasOperator = operator;
 			if (operator == Operator.GT) {
 				operator = Operator.U_GT;
 			} else if (operator == Operator.LT) {
 				operator = Operator.U_LT;
 			} else if (operator == Operator.GE) {
 				operator = Operator.U_GE;
 			} else if (operator == Operator.LE) {
 				operator = Operator.U_LE;
 			}
 			if (wasOperator != operator) {
 				functionVariable.setOperator(operator);
 				functionVariable.setNameIdx(generateFunctionNameIdx(operator));
 			}
 		}
 	}
 
 	/**
 	 * Searches amongst the specified operands list for the first met
 	 * {@link ConstantVariable} that's value is a power of 2.
 	 * todo: Note that range is not taken into account here
 	 *
 	 * @param operandHolders list of Range Variable Holders to search in
 	 * @return value of the first met {@link ConstantVariable} that's
 	 *         value is a power of 2, or <code>null</code> if no such a
 	 *         {@link ConstantVariable} is fount in the specified list.
 	 */
 	private ValueAndIndexHolder findPowerOf2Constant(List<RangeVariableHolder> operandHolders) {
 		for (int i = 0; i < operandHolders.size(); i++) {
 			AbstractVariable variable = operandHolders.get(i).getVariable();
 			if (variable instanceof ConstantVariable) {
 				double power = Math.log10(((ConstantVariable) variable).getValue().doubleValue()) / Math.log10(2);
 				if (power == ((int) power)) { // if the power is an INTEGER (i.e. the whole number, not a decimal)
 					return new ValueAndIndexHolder(BigInteger.valueOf((long) power), i); /* ;*/
 				}
 			}
 		}
 		return null;
 	}
 
 	public ConstantVariable extractSubConstant(ConstantVariable baseConstant, Range rangeToExtract) throws HLDDException {
 
 		if (baseConstant == null) {
 			return null;
 		}
 		if (rangeToExtract == null) {
 			throw new IllegalArgumentException("null rangeToExtract is not allowed as an input parameter for SubConstant extraction");
 		}
 
 		ConstantVariable subConstant = baseConstant.subRange(rangeToExtract);
 
 		return variableManager.getConstantByValue(subConstant.getValue(), subConstant.getLength());
 	}
 
 	public void concatenateRangeAssignments(String wholeVariableName, List<OperandImpl> rangeAssignments) throws Exception {
 
 		FunctionVariable catFunction = createCatFunction(rangeAssignments);
 
 		AbstractVariable wholeVariable = getVariable(wholeVariableName);
 
 		createAndReplaceNewGraph(wholeVariable, new Node.Builder(catFunction).build(), false); // isDelay(varName) was at first "true".
 	}
 
 	/**
 	 * @param oldGraphVariable old variable on the base of which to build the
 	 *                         new one and what to replace with the new variable
 	 * @param graphVarRootNode root node of the new variable
 	 * @param isDelay		  Delay-flag to set to the new GraphVariable
 	 * @return the newly created GraphVariable
 	 */
 	public GraphVariable createAndReplaceNewGraph(AbstractVariable oldGraphVariable, Node graphVarRootNode, boolean isDelay) {
 		GraphVariable newGraphVariable = new GraphVariable(oldGraphVariable, graphVarRootNode);
 		newGraphVariable.setDelay(isDelay);
 		replaceWithRange(oldGraphVariable, new RangeVariableHolder(newGraphVariable, null));
 		return newGraphVariable;
 	}
 
 	private FunctionVariable createCatFunction(List<OperandImpl> rangeAssignmentList) throws Exception {
 		/* Sort by index */
 		Collections.sort(rangeAssignmentList, new OperandImplComparator());
 		/* Create CAT Expression */
 		Expression catExpression = new Expression(Operator.CAT, false);
 		/* Add all inputs to the expression */
 		for (OperandImpl rangeAssignment : rangeAssignmentList) {
 			catExpression.addOperand(rangeAssignment);
 		}
 		/* Add the Expression to ModelCollector */
 		return (FunctionVariable) convertOperandToVariable(catExpression, null, true, null);
 	}
 
 	public void flattenVariableToBits(AbstractVariable wholeVariable) {
 
 		replaceInModel(wholeVariable, null);
 
 		removeVariable(wholeVariable);
 	}
 
 	@Override
 	public Type resolveType(String objectName) {
 		AbstractVariable abstractVariable = getVariable(objectName);
 		if (abstractVariable != null) {
 			return abstractVariable.getType();
 		}
 		ConstantVariable constantVariable = getConstant(objectName);
 		if (constantVariable != null) {
 			return constantVariable.getType();
 		}
 		return null;
 	}
 
 	private class OperandImplComparator implements Comparator<OperandImpl> {
 		public int compare(OperandImpl o1, OperandImpl o2) {
 			/* NB! Larger ranges should be catted earlier, so swap the compared ranges */
 			return o2.getRange().compareTo(o1.getRange());
 		}
 	}
 
 	private class ValueAndIndexHolder {
 		private final BigInteger value;
 		private final int index;
 
 		public ValueAndIndexHolder(BigInteger value, int index) {
 			this.value = value;
 			this.index = index;
 		}
 	}
 
 	private FunctionVariable createInversionFunction(AbstractOperand operand, boolean isTransition, SourceLocation source) throws Exception {
 		/* Temporarily make the operand non-inversed to prevent
 		* infinite recursion and create corresponding variable */
 		operand.setInverted(false);
 		/* Create Function */
 		FunctionVariable invFunctionVariable
 				= doCreateFinalInversionFunction(convertOperandToVariable(operand, null, isTransition, source),
 				operand.getRange(), source);
 		/* Restore initial inverted state */
 		operand.setInverted(true);
 		return invFunctionVariable;
 	}
 
 
 	/**
 	 * <b>Note, that</b> the returned list (if it is not empty) contains
 	 * instances of {@link base.hldd.structure.variables.GraphVariable} for
 	 * what it is guaranteed that all the
 	 * {@link base.hldd.structure.variables.GraphVariable#baseVariable}-s
 	 * are instances of {@link base.hldd.structure.variables.RangeVariable}.
 	 *
 	 * @param expression where to extract RangeVariables from
 	 * @return a <code>non-empty</code> list of
 	 *         {@link RangeVariableHolder}-s, if the
 	 *         specified expression is CAT with all its operands being
 	 *         instances of
 	 *         {@link base.hldd.structure.variables.RangeVariable} (all in all
 	 *         --- a Complete Variable with range assignments). Otherwise an empty list
 	 *         is returned.
 	 */
 	private List<RangeVariableHolder> getRangeAssignmentVariablesFor(Expression expression) {
 		LinkedList<RangeVariableHolder> returnList = new LinkedList<RangeVariableHolder>();
 		LinkedList<RangeVariableHolder> emptyList = new LinkedList<RangeVariableHolder>();
 		if (expression.getOperator() != Operator.CAT) return emptyList;
 		/* Check all operands to be RangeVariables */
 		for (AbstractOperand operand : expression.getOperands()) {
 			if (!(operand instanceof OperandImpl)) return emptyList;
 			OperandImpl operandImpl = (OperandImpl) operand;
 			if (!operandImpl.isRange()) return emptyList;
 			/* Compose name of RangeVariable */
 			Range range = operandImpl.getRange();
 			String varName = operandImpl.getName() + range;
 			/* Obtain the RangeVariable (all range GraphVariables have been set by this point) */
 			AbstractVariable operandVariable = getVariable(varName);
 			if (operandVariable != null && (operandVariable instanceof RangeVariable ||
 					(operandVariable instanceof GraphVariable
 							&& ((GraphVariable) operandVariable).getBaseVariable() instanceof RangeVariable))) {
 				/* Provide NULL range here, since it is already included in the base variable as RangeVariable. */
 				returnList.add(new RangeVariableHolder(operandVariable, null));
 			} else {
 				return emptyList;
 			}
 		}
 		return returnList;
 	}
 
 	/**
 	 * Returns either an identical variable already residing in the collector
 	 * or the variableToFind, previously adding it to collector.
 	 * todo: internals of this method should be moved to VariableManager
 	 *
 	 * @param variableToFind variable to search for amongst existent variables
 	 * @return an existent identical variable or the desired variable if an existent is not found
 	 * @throws Exception cause {@link #addVariable(AbstractVariable) cause}
 	 */
 	AbstractVariable getIdenticalVariable(AbstractVariable variableToFind) throws Exception {
 
 		if (variableToFind instanceof ConstantVariable) {
 			/* Search amongst CONSTANTS */
 			for (ConstantVariable constVariable : variableManager.getConstants()) {
 				if (constVariable.isIdenticalTo(variableToFind)) {
 					return constVariable;
 				}
 			}
 		} else {
 			/* Search amongst VARIABLES */
 			for (AbstractVariable variable : variableManager.getVariables()) {
 				if (variable.isIdenticalTo(variableToFind)) {
 					copySourceToFunction(variableToFind, variable);
 					return variable;
 				}
 			}
 		}
 
 		/* Identical variable was not found,
 		* so add the desired variable to the variables and return it */
 		addVariable(variableToFind);
 		return variableToFind;
 	}
 
 	private void copySourceToFunction(AbstractVariable variableToFind, AbstractVariable variable) {
 		if (variableToFind instanceof FunctionVariable) {
 
 			FunctionVariable functionToFind = (FunctionVariable) variableToFind;
 			FunctionVariable function = (FunctionVariable) variable;
 
 			function.addSource(functionToFind.getSource());
 		}
 	}
 
 	public int getBooleanValueFromOperand(AbstractOperand abstractOperand) {
 		return adjustBooleanCondition(1, abstractOperand.isInverted());
 	}
 
 	public static int adjustBooleanCondition(int booleanCondition, boolean inverted) {
 		return inverted ? invertBit(booleanCondition) : booleanCondition;
 	}
 
 	public static int invertBit(int bit) {
 		return bit == 0 ? 1 : 0;
 	}
 
 	/**
 	 * Extracts a variable out of an <b>operand</b>.
 	 * If it's a function, then creates it and adds to variables.
 	 * Otherwise searches for it amongst internal variables.
 	 *
 	 * @param operand	  base operand to extract variable from
 	 * @param targetType   desired length of the variable or <code>null<code> if doesn't matter.
 	 *                     <b>NB!</b> This currently matters for {@link ConstantVariable}-s only.
 	 * @param isTransition {@link #createFunction(base.vhdl.structure.AbstractOperand, boolean, SourceLocation)}
 	 * @param source	   VHDL source
 	 * @return a ConstantVariable, FunctionVariable or Variable based on the <code>operand</code>
 	 * @throws Exception if the <code>operand</code> contains an undeclared variable
 	 */
 	public AbstractVariable convertOperandToVariable(AbstractOperand operand, Type targetType, boolean isTransition, SourceLocation source) throws Exception {
 
 		if (operand instanceof Expression || operand instanceof OperandImpl && operand.isInverted()) {
 
 			return createFunction(operand, isTransition, source);
 
 		} else if (operand instanceof OperandImpl) {
 			OperandImpl operandImpl = (OperandImpl) operand;
 			if (operandImpl.isArray()) {
 				Type elType = targetType.getArrayElementType();
 				Map<Condition, ConstantVariable> arrayValues = new TreeMap<Condition, ConstantVariable>();
 				for (OperandImpl.Element element : operandImpl.getElements()) {
 
 					AbstractVariable elVar = convertOperandToVariable(element.operand, elType, isTransition, source);
 					if (!(elVar instanceof ConstantVariable)) {
 						throw new Exception("Non-constant array element found: " + elVar.getClass().getSimpleName());
 					}
 					arrayValues.put(element.index, (ConstantVariable) elVar);
 				}
 				return new ConstantVariable(arrayValues);
 			}
 			String variableName = operandImpl.getName();
 			BigInteger constantValue = PackageParser.parseConstantValue(variableName);
 			if (constantValue != null) {
 				/* Get CONSTANT by VALUE*/
 				//todo: Jaan: different constants for different contexts
 				Range targetLength = targetType != null ? targetType.getLength() : null;
 				return variableManager.getConstantByValue(constantValue,
 						operand.getLength() != null ? operand.getLength() : targetLength);
 
 			} else {
 				if (operandImpl.isDynamicRange()) {
 					variableName = OperandImpl.generateNameForDynamicRangeRead(operandImpl);
 				}
 				/* Get VARIABLE by NAME */
 				AbstractVariable variable = variableManager.getVariableByName(variableName);
 				if (variable != null) return variable;
 				/* Get CONSTANT by NAME */
 				ConstantVariable constant = variableManager.getConstantByName(variableName);
 				if (constant != null) return constant;
 			}
 			throw new Exception("Undeclared VARIABLE (" + variableName + ") is used in the following operand: " + operand);
 
 		} else if (operand instanceof UserDefinedFunction) {
 			UserDefinedFunction udFunction = (UserDefinedFunction) operand;
 			String udFunctionName = udFunction.getUserDefinedFunction();
 			List<AbstractOperand> udFunctionOperands = udFunction.getOperands();
 			// Create User Defined Function Variable
 			UserDefinedFunctionVariable udFunctionVariable =
 					new UserDefinedFunctionVariable(udFunctionName,
 							generateUDFunctionNameIdx(udFunctionName),
 							udFunctionOperands.size(), operand.getLength());
 			udFunctionVariable.setSource(source);
 			// Create and add operands to UD Function Variable
 			for (AbstractOperand udFunctionOperand : udFunctionOperands) {
 				udFunctionVariable.addOperand(convertOperandToVariable(udFunctionOperand, null, true, source), udFunctionOperand.getRange());
 			}
 			// Search for identical and add variable to collector if needed
 			return getIdenticalVariable(udFunctionVariable);
 
 		} else if (operand == null) {
 			return null;
 		}
 		throw new Exception("Obtaining VHDL structure variables from " + operand.getClass().getSimpleName() +
 				"instances is not supported");
 	}
 
 	public Condition convertOperandsToCondition(OperandImpl[] conditionOperands) {
 		int[] values = new int[conditionOperands.length];
 		for (int i = 0; i < conditionOperands.length; i++) {
 			String operandName = conditionOperands[i].getName();
 
 			ConstantVariable constant = variableManager.getConstantByName(operandName);
 			values[i] = constant != null ? constant.getValue().intValue() : PackageParser.parseConstantValue(operandName).intValue();
 		}
 		return Condition.createCondition(values);
 	}
 
 	/**
 	 * Generates a name for function variables ADDER, MULT, DIV etc
 	 * and user defined functions like f_ComputeCrc16() in crc.vhd.
 	 *
 	 * @param operator type of function (ADDER, MULT, DIV etc)
 	 * @return the order number of the function
 	 */
 	private int generateFunctionNameIdx(Operator operator) {
 		return deriveNextIdx(variableManager.getFunctions(operator));
 	}
 
 	private int generateUDFunctionNameIdx(String userDefinedOperator) {
 		return deriveNextIdx(variableManager.getUDFunctions(userDefinedOperator));
 	}
 
 	private int deriveNextIdx(Collection<FunctionVariable> functions) {
 		int largestIndexUsed = -1;
 		for (FunctionVariable functionVariable : functions) {
 			int nameIndex = functionVariable.getNameIdx();
 			if (nameIndex > largestIndexUsed) {
 				largestIndexUsed = nameIndex;
 			}
 		}
 		return largestIndexUsed == -1 ? 1 : largestIndexUsed + 1;
 	}
 
 	public Collection<ConstantVariable> getConstants() {
 		return variableManager.getConstants();
 	}
 
 	public Collection<AbstractVariable> getVariables() {
 		return variableManager.getVariables();
 	}
 
 	public AbstractVariable getVariable(String variableName) {
 		return variableManager.getVariableByName(variableName);
 	}
 
 	public ConstantVariable getConstant(String constantName) {
 		return variableManager.getConstantByName(constantName);
 	}
 
 	/* AUXILIARY classes */
 
 	public class CompositeFunctionVariable extends FunctionVariable {
 		private Operator compositeOperator;
 		private RangeVariableHolder[] functionVariables;
 
 		public CompositeFunctionVariable(Operator compositeOperator, RangeVariableHolder... functionVariables) {
 			this.compositeOperator = compositeOperator;
 			this.functionVariables = functionVariables;
 		}
 
 		public Operator getCompositeOperator() {
 			return compositeOperator;
 		}
 
 		public RangeVariableHolder[] getFunctionVariables() {
 			return functionVariables;
 		}
 
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < functionVariables.length; i++) {
 				sb.append(functionVariables[i]).append("\n");
 				if (i < functionVariables.length - 1) {
 					sb.append(compositeOperator).append("\n");
 				}
 			}
 			return sb.toString();
 		}
 	}
 
 }
