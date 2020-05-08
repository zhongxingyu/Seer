 /**
  * Expression.java
  * 
  * Wrapper for JEval
  * 
  * @author Agent Team
  */
 
 package edu.wheaton.simulator.expression;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Pattern;
 
 import net.sourceforge.jeval.EvaluationException;
 import net.sourceforge.jeval.Evaluator;
 import net.sourceforge.jeval.VariableResolver;
 import net.sourceforge.jeval.function.FunctionException;
 import edu.wheaton.simulator.behavior.CloneAgentAtPositionBehavior;
 import edu.wheaton.simulator.behavior.CloneBehavior;
 import edu.wheaton.simulator.behavior.ClonePrototype;
 import edu.wheaton.simulator.behavior.DieBehavior;
 import edu.wheaton.simulator.behavior.KillBehavior;
 import edu.wheaton.simulator.behavior.MoveBehavior;
 import edu.wheaton.simulator.behavior.MoveDownBehavior;
 import edu.wheaton.simulator.behavior.MoveLeftBehavior;
 import edu.wheaton.simulator.behavior.MoveRightBehavior;
 import edu.wheaton.simulator.behavior.MoveUpBehavior;
 import edu.wheaton.simulator.behavior.SetFieldBehavior;
 import edu.wheaton.simulator.behavior.SetFieldOfAgentBehavior;
 import edu.wheaton.simulator.entity.Entity;
 
 public class Expression {
 
 	/**
 	 * All variables that JEval evaluates are first passed to an associated
 	 * instance of VariableResolver. If the 'VR' returns a null then the JEval
 	 * 'Evaluator' will look for the variable name in its internal map. This
 	 * 'VR' implementation is solely for the purpose of enabling a shorter
 	 * Expression syntax: "#{this.x}" rather than something like
 	 * "getField('this','x')" which would require implementing a "getField"
 	 * ExpressionFunction.
 	 * 
 	 * @author bgarcia
 	 * 
 	 */
 	protected class EntityFieldResolver implements VariableResolver {
 
 		/**
 		 * Keeps track of all the entities in the grid
 		 */
 		private Map<String, Entity> entityMap;
 
 		/**
 		 * Default constructor
 		 */
 		protected EntityFieldResolver() {
 			entityMap = new HashMap<String, Entity>();
 		}
 
 		/**
 		 * Constructor for copying
 		 */
 		protected EntityFieldResolver(EntityFieldResolver resolver) {
 			entityMap = new HashMap<String, Entity>();
 			entityMap.putAll(resolver.entityMap);
 		}
 
 		/**
 		 * Given the name of an entity and a field (for example this.x), this
 		 * method provides the actual value of the field.
 		 * 
 		 * @param variableName
 		 * @return
 		 */
 		@Override
 		public String resolveVariable(String variableName)
 				throws FunctionException {
 
 			// splits with delimiter '.'
 			String[] args = variableName.split("\\x2e");
 
 			if (args.length != 2) {
 				return null;
 			}
 
 			String targetName = args[0];
 			String fieldName = args[1];
 
 			Entity target = getEntity(targetName);
 			if (target == null) {
 				throw new FunctionException("Target entity not found: "
 						+ targetName);
 			}
 			try {
 				String toReturn = target.getFieldValue(fieldName);
 				return toReturn;
 			} catch (NoSuchElementException e) {
 				throw new FunctionException("Target field not found: "
 						+ fieldName);
 			}
 		}
 
 		protected void setEntity(String name, Entity entity) {
 			entityMap.put(name, entity);
 		}
 
 		protected Entity getEntity(String name) {
 			return entityMap.get(name);
 		}
 	}
 
 	// boolean constants
 	public static final String TRUE = "1.0";
 	public static final String FALSE = "0.0";
 
 	private Evaluator evaluator;
 	private EntityFieldResolver resolver;
 	private Object expr;
 	private String exprStr;
 
 	private static HashMap<String, AbstractExpressionFunction> behaviorFunctions;
 	private static HashMap<String, AbstractExpressionFunction> conditionFunctions;
 
 	/**
 	 * Default constructor
 	 * 
 	 * The expression string is retrieved by calling expr.toString()
 	 */
 	public Expression(Object exprStr) {
 		setString(exprStr);
 		evaluator = new Evaluator();
 		resolver = new EntityFieldResolver();
 		evaluator.setVariableResolver(resolver);
 
 		// make all project-defined ExpressionFunction implementations
 		// recognizable by default
 		this.importFunction(new CloneBehavior());
 		this.importFunction(new DieBehavior());
 		this.importFunction(new KillBehavior());
 		this.importFunction(new MoveBehavior());
 		this.importFunction(new SetFieldBehavior());
 		this.importFunction(new IsSlotOpen());
 		this.importFunction(new GetFieldOfAgentAt());
 		this.importFunction(new IsValidCoord());
 		this.importFunction(new CloneAgentAtPositionBehavior());
 		this.importFunction(new SetFieldOfAgentBehavior());
 		this.importFunction(new ClonePrototype());
 		this.importFunction(new GetGlobalField());
 		this.importFunction(new MoveRightBehavior());
 		this.importFunction(new MoveLeftBehavior());
 		this.importFunction(new MoveUpBehavior());
 		this.importFunction(new MoveDownBehavior());
 
 		// make a hashmap of names and actual objects.
 		initializeFunctions();
 	}
 
 	/**
 	 * Helper method that adds all the behavior and conditional functions to
 	 * the HashMaps
 	 */
 	private static void initializeFunctions() {
 		behaviorFunctions = new HashMap<String, AbstractExpressionFunction>();
 		conditionFunctions = new HashMap<String, AbstractExpressionFunction>();
 		behaviorFunctions.put("clone", new CloneBehavior());
 		behaviorFunctions.put("cloneAgentAtPosition",
 				new CloneAgentAtPositionBehavior());
 		behaviorFunctions.put("die", new DieBehavior());
 		behaviorFunctions.put("kill", new KillBehavior());
 		behaviorFunctions.put("move", new MoveBehavior());
 		behaviorFunctions.put("setField", new SetFieldBehavior());
 		behaviorFunctions
 				.put("setFieldOfAgent", new SetFieldOfAgentBehavior());
 		behaviorFunctions.put("clonePrototype", new ClonePrototype());
 		behaviorFunctions.put("moveRight", new MoveRightBehavior());
 		behaviorFunctions.put("moveLeft", new MoveLeftBehavior());
 		behaviorFunctions.put("moveDown", new MoveDownBehavior());
 		behaviorFunctions.put("moveUp", new MoveUpBehavior());
 		conditionFunctions.put("getFieldOfAgent", new GetFieldOfAgentAt());
 		conditionFunctions.put("isSlotOpen", new IsSlotOpen());
 		conditionFunctions.put("isValidCoord", new IsValidCoord());	
 		conditionFunctions.put("getGlobalField", new GetGlobalField());	
 	}
 
 	/**
 	 * Copy constructor
 	 */
 	private Expression(Expression expr) {
 		evaluator = new Evaluator();
 		evaluator.setFunctions(expr.evaluator.getFunctions());
 		evaluator.setVariables(expr.evaluator.getVariables());
 		resolver = new EntityFieldResolver(expr.resolver);
 		evaluator.setVariableResolver(resolver);
 		setString(expr.expr);
 	}
 
 	protected Expression(Evaluator eval, EntityFieldResolver res) {
 		this.evaluator = eval;
 		this.resolver = res;
 	}
 
 	public static Map<String, AbstractExpressionFunction> getBehaviorFunction() {
 		if (behaviorFunctions == null)
 			initializeFunctions();
 		return behaviorFunctions;
 	}
 
 	public static Map<String, AbstractExpressionFunction> getConditionFunction() {
 		if (conditionFunctions == null)
 			initializeFunctions();
 		return conditionFunctions;
 	}
 
 	/**
 	 * Returns a properly formatted variable reference.
 	 * 
 	 * fGet("x") == "#{x}"
 	 * 
 	 * fGet("this.x") == "#{this.x}"
 	 * 
 	 * @param entityName
 	 * @param fieldName
 	 * @return
 	 */
 	private static String fGet(String variableName) {
 		return "#{" + variableName + "}";
 	}
 
 	/**
 	 * Returns a properly formatted string to be passed to an Expression
 	 * method.
 	 * 
 	 * "setField(" + fParams("this,x,8") + ")" == "setField('this','x',8)
 	 * 
 	 * @param params
 	 * @return
 	 */
 	public static String fParams(String params) {
 		params = params.replaceAll(" ", "");
 		String[] paramList = params.split(",");
 
 		for (int i = 0; i < paramList.length; ++i) {
 			paramList[i] = fParam(paramList[i]);
 		}
 
 		String toReturn = "";
 		for (int i = 0; i < paramList.length; ++i)
 			toReturn += paramList[i] + ",";
 
 		if (toReturn.isEmpty() == false)
 			toReturn = toReturn.substring(0, toReturn.length() - 1);
 
 		return toReturn;
 	}
 
 	/**
 	 * Returns a properly formatted string value
 	 * 
 	 * correctStrVal("I am a banana!") == "'I am a banana!'"
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public static String correctStrVal(String value) {
 		if (isStrVal(value))
 			return value;
 		return "'" + value + "'";
 	}
 
 	/**
 	 * 
 	 * When a reference is passed to a function (ex: "setField('this','x',8)")
 	 * This method can be used within the example method
 	 * SetFieldBehavior.execute(...) to eliminate the single quotes surrounding
 	 * the parameter, thus preventing someone from accidentally requesting a
 	 * field with the name "'x'" when they really meant "x".
 	 * 
 	 * @param entity
 	 * @param str
 	 * @return
 	 */
 	public static String correctNonStrVal(String str) {
 		if (isStrVal(str))
 			return str.substring(1, str.length() - 1);
 		return str;
 	}
 
 	private static boolean isStrVal(String str) {
 		return str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'';
 	}
 
 	/**
 	 * Returns a properly formatted string to be passed to an Expression
 	 * method.
 	 * 
 	 * "setField(" + fParam("this") + "," + fParam("x") + "," + fParam("8") +
 	 * ")" == "setField('this','x',8)
 	 * 
 	 * @param name
 	 * @return
 	 */
 	private static String fParam(String param) {
 		if (param.equalsIgnoreCase("true"))
 			return TRUE;
 		else if (param.equalsIgnoreCase("false"))
 			return FALSE;
 		else {
 			try {
 				return Double.valueOf(param).toString();
 			}
 
 			catch (Exception e) {
 				return correctStrVal(param);
 			}
 		}
 	}
 
 	/**
 	 * calls the copy constructor
 	 */
 	@Override
 	public Expression clone() {
 		return new Expression(this);
 	}
 
 	/**
 	 * sets the string that is evaluated by JEval/JEval-wrapper
 	 */
 	public void setString(Object exprStr) {
 		this.expr = exprStr;
 		this.exprStr = formatExpr(expr);
 	}
 
 	/**
 	 * Define a variable
 	 * 
 	 * @Param name Do not format this String as you must do when creating an
 	 *        expression String. Simply pass the desired variable name.
 	 */
 	public void importVariable(String name, String value) {
 		evaluator.putVariable(name, value);
 	}
 
 	/**
 	 * Make an entity recognizable by this expression and all functions called
 	 * within
 	 * 
 	 * @Param aliasName The name used to refer to the Entity in the expression
 	 *        String ("this", "other", etc.)
 	 */
 	public void importEntity(String aliasName, Entity entity) {
 		resolver.setEntity(aliasName, entity);
 	}
 
 	/**
 	 * Make an ExpressionFunction recognizable by this expression and all
 	 * functions called within
 	 */
 	public void importFunction(AbstractExpressionFunction function) {
 		evaluator.putFunction(function.toJEvalFunction());
 	}
 
 	/**
 	 * get an imported Entity
 	 */
 	public Entity getEntity(String aliasName) {
 		return resolver.getEntity(aliasName);
 	}
 
 	/**
 	 * get the value of an imported variable
 	 */
 	public String getVariableValue(String variableName)
 			throws EvaluationException {
 		return evaluator.getVariableValue(variableName);
 	}
 
 	/**
 	 * clear all variables added with 'importVariable'
 	 * 
 	 */
 	public void clearVariables() {
 		evaluator.clearVariables();
 	}
 
 	/**
 	 * clear all entities added with 'importEntity'
 	 */
 	public void clearEntities() {
 		resolver.entityMap.clear();
 	}
 
 	/**
 	 * clear all functions added with 'importFunction
 	 */
 	public void clearFunctions() {
 		evaluator.clearFunctions();
 	}
 
 	public Boolean evaluateBool() throws EvaluationException {
 		try {
 			return evaluator.getBooleanResult(exprStr);
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	public Double evaluateDouble() throws EvaluationException {
 		try {
 			return evaluator.getNumberResult(exprStr);
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	public String evaluateString() throws EvaluationException {
 		try {
 			return evaluator.evaluate(exprStr);
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return expr.toString();
 	}
 
 	public static Boolean evaluateBool(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateBool();
 	}
 
 	public static Double evaluateDouble(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateDouble();
 	}
 
 	public static String evaluateString(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateString();
 	}
 
 	/**
 	 * Parses/formats the parameter according to expression syntax
 	 * 
 	 * This method is called before evaluating the expression.
 	 */
 	private static String formatExpr(Object expr) {
 		String str = expr.toString();
 
 		// format booleans
 		str = formatBools(str);
 
 		String regexVariableRef = "\\b[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)?\\b(?![('])(?=([^']*'[^']*')*[^']*$)";
 
 		// string with all matches replaced with '@'
 		String temp = str.replaceAll(regexVariableRef, "@");
 
 		// list of all non-matching segments
 		String[] nonMatchingSegs = temp.split("@");
 
 		// construct string with all non-matches replaced with '@'
 		String temp2 = str;
 		for (String segment : nonMatchingSegs)
 			temp2 = Pattern.compile(segment, Pattern.LITERAL).matcher(temp2)
 					.replaceFirst("@");
 		// temp2 = temp2.replaceAll(segment, "@");
 
 		// list of all matches
 		String[] matches = temp2.split("@");
 
 		String toReturn = temp;
 		for (String match : matches) {
 			if (match.length() > 0) {
 				toReturn = toReturn.replaceFirst("@", fGet(match));
 			}
 		}
 
 		return toReturn;
 	}
 
 	/**
 	 * Uses regular expressions to replace all boolean values (no matter their
 	 * formatting) with actual, recognizable booleans.
 	 * 
 	 * @param str
 	 * @return
 	 */
 	private static String formatBools(String str) {
 		str = str.replaceAll("\\b[Tt][Rr][Uu][Ee]\\b", TRUE);
 		str = str.replaceAll("\\b[Ff][Aa][Ll][Ss][Ee]\\b", FALSE);
 		return str;
 	}
 }
