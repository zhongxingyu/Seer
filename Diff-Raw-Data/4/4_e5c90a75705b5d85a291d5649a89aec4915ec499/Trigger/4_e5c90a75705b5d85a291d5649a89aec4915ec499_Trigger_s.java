 /**
  * Trigger Class
  * 
  * "Triggers" are used to give agents certain behaviors. They represent a boolean expression as created by the user that, when met, causes the agent to perform a certain behavior.
  * Note: Triggers should have unique priorities within an agent; problems will be had if there are multiple triggers with the same priority values within an agent. 	 
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, Emmanuel Pederson, Chris Anderson and Simon Swenson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.expression.Expression;
 
 public class Trigger implements Comparable<Trigger> {
 
 	/**
 	 * A name to reference a trigger by
 	 */
 	private String name;
 
 	/**
 	 * Determines when this trigger should be evaluated.
 	 * Only affects the order of triggers within its particular Agent
 	 * in LinearUpdate. That is, the order of importance in LinearUpdate
 	 * for triggers is when the Owning Agent is reached->the trigger's priority.
 	 * 
 	 * In PriorityUpdate, priority supercedes Agent ordering, that is
 	 * trigger's priority->when the Owning Agent is reached.
 	 * 
 	 */
 	private int priority;
 	
 	/**
 	 * Used for AtomicUpdate
 	 * In each iteration, the condition is evaluated, and
 	 * the result is stored hear. It is later checked to see
 	 * whether or not the behavior should be fired.
 	 */
 	private boolean atomicConditionResult;
 
 	/**
 	 * Represents the conditions of whether or not the trigger fires.
 	 */
 	private Expression conditionExpression;
 
 	/**
 	 * The behavior that is executed when the trigger condition is met
 	 */
 	private Expression behaviorExpression;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param priority
 	 *            Triggers are checked in order of priority, with lower numbers
 	 *            coming first
 	 * @param conditions
 	 *            boolean expression this trigger represents
 	 */
 	public Trigger(String name, int priority, Expression conditionExpression,
 			Expression behavior) {
 		this.name = name;
 		this.priority = priority;
 		this.conditionExpression = conditionExpression;
 		this.behaviorExpression = behavior;
 	}
 
 	/**
 	 * Clone Constructor. Deep copy is not necessary at this point.
 	 * 
 	 * @param parent
 	 *            The trigger from which to clone.
 	 */
 	public Trigger(Trigger parent) {
 		name = parent.getName();
 		priority = parent.getPriority();
 		conditionExpression = parent.getConditions();
 		behaviorExpression = parent.getBehavior();
 	}
 
 	/**
 	 * Evaluates the boolean expression represented by this object and fires if
 	 * all conditions evaluate to true.
 	 * 
 	 * If someone wants to evaluate an expression to something other than
 	 * boolean, they will need to change this method or fire.
 	 * 
 	 * @return Boolean
 	 * @throws EvaluationException
 	 *             if the expression was invalid
 	 */
 	public void evaluate(Agent xThis) throws EvaluationException {
 		Expression condition = conditionExpression.clone();
 		Expression behavior = behaviorExpression.clone();
 
 		condition.importEntity("this", xThis);
 		behavior.importEntity("this", xThis);
 
 		boolean conditionResult = false;
 		try {
 			conditionResult = condition.evaluateBool();
 		} catch (Exception e) {
 			conditionResult = false;
 			System.out.println("Condition expression failed: "
 					+ condition.toString());
 		}
 
 		if (conditionResult) {
 			fire(behavior);
 		}
 	}
 	
 	/**
 	 * Just evaluates the condition and stores the result in atomicConditionResult
 	 * for later use (to see if the behavior is fired).
 	 * Vital for AtomicUpdate
 	 * @param xThis the owning Agent
 	 * @throws EvaluationException
 	 */
 	public void evaluateCond(Agent xThis) throws EvaluationException {
 		Expression condition = conditionExpression.clone();
 		
 		condition.importEntity("this", xThis);
 		
 		atomicConditionResult = false;
 		try {
 			atomicConditionResult = condition.evaluateBool();
		} catch (Exception e) {
 			atomicConditionResult = false;
 			System.out.println("Condition expression failed: "
 					+ condition.toString());
 		}
 	}
 	
 	/**
 	 * Checks atomicConditionResult to see whether or not the behavior should be fired
 	 * rather than evaluating the condition on the spot
 	 * Vital for AtomicUpdate
 	 * @param xThis
 	 * @throws EvaluationException
 	 */
 	public void atomicFire(Agent xThis) throws EvaluationException {
 		Expression behavior = behaviorExpression.clone();
 		
 		behavior.importEntity("this", xThis);
 		if (atomicConditionResult)
 			fire(behavior);
 	}
 
 	/**
 	 * Get the String representation of this trigger's firing condition
 	 * 
 	 * @return the firing condition
 	 */
 	public Expression getConditions() {
 		return conditionExpression;
 	}
 
 	/**
 	 * Sets the conditional expression.
 	 * @param e
 	 */
 	private void setCondition(Expression e){
 		conditionExpression = e;
 	}
 	
 	/**
 	 * Fires the trigger. Will depend on the Behavior object for this trigger.
 	 */
 	private static void fire(Expression behavior) throws EvaluationException {
 		try {
 			if (behavior.evaluateBool() == false)
 				System.err.println("behavior '" + behavior.toString()
 						+ "' failed");
 			else
 				System.out.println("behavior '" + behavior.toString()
 						+ "' succeeded");
 		} catch (EvaluationException e) {
 			System.err.println("malformed expression: " + behavior);
 			e.printStackTrace();
 			throw new EvaluationException("Behavior");
 		}
 	}
 
 	/**
 	 * Compares this trigger to another trigger based on priority
 	 * 
 	 * @param other
 	 *            The other trigger to compare to.
 	 * @return -1 if less, 0 if same, 1 if greater.
 	 */
 	@Override
 	public int compareTo(Trigger other) {
 		if (priority == other.priority) {
 			return 0;
 		} else if (priority > other.priority) {
 			return 1;
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Sets the behavior of the trigger.
 	 * 
 	 * @param behavior
 	 *            Behavior to be added to list
 	 */
 	public void setBehavior(Expression behavior) {
 		this.behaviorExpression = behavior;
 	}
 
 	/**
 	 * Gets the name of this Trigger
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public Expression getBehavior() {
 		return behaviorExpression;
 	}
 	
 	public int getPriority() {
 		return priority;
 	}
 
 	public static class Builder {
 
 		private Trigger trigger;
 
 		/**
 		 * HashMap of simple values to actual JEval appropriate input for
 		 * conditionals. Hashmap of field and value for prototypes.
 		 */
 		private HashMap<String, String> fieldValues;
 
 		/**
 		 * HashMap of simple values to actual JEval appropriate input for
 		 * conditionals. Hashmap of behavior and Jeval equivalent.
 		 */
 		private HashMap<String, String> behavioralValues;
 
 		/**
 		 * A HashMap of the operations that will be used in the JEval. Comes with all 
 		 * the usual functions, user may be able to add more. First string is name, second is what 
 		 * Jeval will use to evaluate.
 		 */
 		private HashMap<String, String> operations;
 		
 		/**
 		 * A HashMap of the functions/expressions that might be used in the JEval
 		 */
 		private HashMap<String, String> functions;
 		
 		/**
 		 * Constructor
 		 * 
 		 * @param p
 		 *            A prototype with just fields
 		 */
 		public Builder(Prototype p) {
 			trigger = new Trigger("", 0, null, null);
 			/*
 			 * TODO Need to be able to add functions.
 			 */
 			loadOperations(p);
 			loadFieldValues(p);	
 			loadBehaviors(p);
 			loadFunctions(p);
 		}
 		
 		/*
 		 * Method to store and initialize all the functions that a trigger may use. 
 		 */
 		private void loadFunctions(Prototype p){
 			functions = new HashMap<String,String>();
 			/**TODO
 			 * load all the functions that might be used. still not entirely sure 
 			 * how functions are gonna work in this. Need to figure out something
 			 * about more JCombo boxes or something.
 			 */
 		}
 		
 		/**
 		 * Method to store common operations and initialize them when a Builder is initialized.
 		 */
 		private void loadOperations(Prototype p){
 			operations = new HashMap<String, String>();
 			operations.put("OR", "||");
 			operations.put("AND", "&&");
 			operations.put("NOT_EQUALS", "!=");
 			operations.put("EQUALS", "==");
 			operations.put(">", ">");
 			operations.put("<", "<");
 			//TODO need to add all the basic operations we are using of JEval,
 		}
 		
 		/**
 		 * Loads all of the behaviors that we are using and their JEval equivalent.
 		 * @param p
 		 */
 		private void loadBehaviors(Prototype p){
 			behavioralValues = new HashMap<String, String>();
 			//TODO figure out what to do for behaviors too.
 		}
 		
 		/**
 		 * Method to initialize conditionalValues and behaviorableValues
 		 */
 		private void loadFieldValues(Prototype p){
 			fieldValues = new HashMap<String, String>();
 			for (Map.Entry<String, String> entry : p.getFieldMap().entrySet())
 			{
 			    fieldValues.put(entry.getKey(), "this."+entry.getKey());
 			}
 		}
 
 		/**
 		 * Sets the name of the Trigger
 		 * 
 		 * @param n
 		 */
 		public void addName(String n) {
 			trigger.name = n;
 		}
 
 		/**
 		 * Sets the priority of the Trigger
 		 * 
 		 * @param n
 		 */
 		public void addPriority(int p) {
 			trigger.priority = p;
 		}
 
 		/**
 		 * Returns a set of conditional values
 		 * 
 		 * @return Set of Strings
 		 */
 		public Set<String> conditionalValues() {
 			return fieldValues.keySet();
 		}
 
 		/**
 		 * Gets a set of possible behavioral values
 		 * 
 		 * @return Set of Strings
 		 */
 		public Set<String> behavioralValues() {
 			return behavioralValues.keySet();
 		}
 
 		/**
 		 * Passes the builder a string of values separated with a space the
 		 * correspond to a conditional
 		 * 
 		 * @param c
 		 */
 		public void addConditional(String c) {
 			String condition = "";
 			String[] stringArray = c.split(" ");
 			for (String a : stringArray){
 				condition+= lookThroughMapsForMatches(a);
 			}
 			trigger.setCondition(new Expression(condition));
 		}
 		
 		/**
 		 * Looks through the conditionalvalues, behavirovalues, function and operation 
 		 * hashmaps to convert text to something that can be evaluated by JEval.
 		 * If it doesn't find anything in the hashmaps, it returns the same string
 		 * Should only happen if user enters something new, like "weight > 0", the 0 part.
 		 * 
 		 * @param s
 		 */
 		private String lookThroughMapsForMatches(String s){
 			String toReturn= "";
 			toReturn = fieldValues.get(s);
 			if (toReturn != null)
 				return toReturn;
 			toReturn = behavioralValues.get(s);
 			if (toReturn != null)
 				return toReturn;
 			toReturn = operations.get(s);
 			if (toReturn != null)
 				return toReturn;
 			toReturn = functions.get(s);
 			if (toReturn != null)
 				return toReturn;
 			return s;
 		}
 
 		/**
 		 * Passes the builder a string of values separated with a space the
 		 * correspond to a behavior
 		 * 
 		 * @param b
 		 */
 		public void addBehavioral(String b) {
 			String behavior = "";
 			String[] stringArray = b.split(" ");
 			for (String a : stringArray){
 				behavior+= lookThroughMapsForMatches(a);
 			}
 			trigger.setCondition(new Expression(behavior));
 		}
 
 		/**
 		 * Whether or not the last conditional and behavioral are a valid
 		 * trigger
 		 * 
 		 * @return
 		 */
 		public boolean isValid() {
 			try {
 				trigger.getConditions().evaluateBool();
 				return true; 
 			} catch (Exception e) {
 				System.out.println("Condition expression failed: "
 						+ trigger.getConditions().toString());
 				return false;
 			}
 
 			/*
 			 * TODO Attempt to evaluate the trigger. If it throws an exception
 			 * then return false.
 			 */
 		}
 
 		/**
 		 * Provides the built trigger
 		 * 
 		 * @return
 		 */
 		public Trigger build() {
 			return trigger;
 		}
 
 	}
 }
