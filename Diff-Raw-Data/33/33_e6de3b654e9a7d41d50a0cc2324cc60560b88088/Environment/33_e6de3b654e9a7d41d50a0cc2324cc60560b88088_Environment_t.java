 package com.gummy.core;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import com.gummy.primitives.Apply;
 import com.gummy.primitives.io.CloseInputPort;
 import com.gummy.primitives.io.CloseOutputPort;
 import com.gummy.primitives.io.Load;
 import com.gummy.primitives.io.OpenInputFile;
 import com.gummy.primitives.io.OpenOutputFile;
 import com.gummy.primitives.io.PeekChar;
 import com.gummy.primitives.io.Read;
 import com.gummy.primitives.io.ReadChar;
 import com.gummy.primitives.list.Car;
 import com.gummy.primitives.list.Cdr;
 import com.gummy.primitives.list.Cons;
 import com.gummy.primitives.predicates.Equal;
 import com.gummy.primitives.predicates.Eqv;
 import com.gummy.types.Procedure;
 import com.gummy.types.Symbol;
 
 public class Environment {
 
 	private Hashtable<Symbol, Object> values;
 	private Environment parent;
 
 	/**
 	 * This creates a new environment with no parent, and should be used
 	 * strictly for the global environment.
 	 */
 	public Environment() {
 		this(null);
 	}
 
 	/**
 	 * This creates a new environment which is based on a parent environment.
 	 * 
 	 * @param parent
 	 *            This is the parent environment.
 	 */
 	public Environment(Environment parent) {
 		this.parent = parent;
 		this.values = new Hashtable<Symbol, Object>();
 	}
 
 	/**
 	 * This initializes an environment with all the default procedures.
 	 * 
 	 * @param environment
 	 *            The environment to initialize.
 	 */
 	public static void initialize(Environment environment) {
 		environment.defineValue(Symbol.getSymbol("apply"), new Apply());
		environment.defineValue(Symbol.getSymbol("error"), new com.gummy.primitives.Error());
 		
 		environment.defineValue(Symbol.getSymbol("cons"), new Cons());
 		environment.defineValue(Symbol.getSymbol("list"),
 				new com.gummy.primitives.list.List());
 		environment.defineValue(Symbol.getSymbol("car"), new Car());
 		environment.defineValue(Symbol.getSymbol("cdr"), new Cdr());
 
 		// IO Procedures
 		Load load = new Load();
 		environment.defineValue(Symbol.getSymbol("load"), load);
 		environment.defineValue(Symbol.getSymbol("read"), new Read());
 		environment.defineValue(Symbol.getSymbol("read-char"), new ReadChar());
 		environment.defineValue(Symbol.getSymbol("peek-char"), new PeekChar());
 		environment.defineValue(Symbol.getSymbol("open-input-file"), new OpenInputFile());
 		environment.defineValue(Symbol.getSymbol("open-output-file"), new OpenOutputFile());
 		environment.defineValue(Symbol.getSymbol("close-input-port"), new CloseInputPort());
 		environment.defineValue(Symbol.getSymbol("close-output-port"), new CloseOutputPort());
 
 		environment.defineValue(Symbol.getSymbol("eqv?"), new Eqv());
 		environment.defineValue(Symbol.getSymbol("equal?"), new Equal());
 
 		// Load the core file
 		List<Object> arguments = new ArrayList<Object>(1);
 		arguments.add("core/core.scm".toCharArray());
 		load.apply(environment, arguments);
 
 	}
 
 	/**
 	 * @return The parent environment of this environment.
 	 */
 	public Environment getParent() {
 		return this.parent;
 	}
 
 	/**
 	 * This fetches the value for a given symbol by searching through this
 	 * environment, and all subsequent parent environments in order to enforce
 	 * scope.
 	 * 
 	 * @param symbol
 	 *            The symbol representing the variable.
 	 * @return The variable's value.
 	 * @throws InterpreterException
 	 *             if the value does not exist in the chain of environments.
 	 */
 	public Object getValue(Symbol symbol) throws InterpreterException {
 		Environment current = this;
 		Object value;
 
 		// Iterate through the environments rather then recursing to prevent
 		// the stack blowing due to a large environment chain.
 		do {
 			value = current.values.get(symbol);
 
 			if (value != null)
 				return value;
 		} while ((current = current.getParent()) != null);
 
 		throw new InterpreterException("Undefined variable: "
 				+ symbol.getSymbol());
 	}
 
 	/**
 	 * This defines a value in the current scope. It is destructive, and
 	 * therefore will override any value with this symbol in the <b>current</b>
 	 * scope.
 	 * 
 	 * @param symbol
 	 *            The symbol representing the variable.
 	 * @param value
 	 *            The value of the symbol.
 	 */
 	public void defineValue(Symbol symbol, Object value) {
 		this.values.put(symbol, value);
 	}
 
 	/**
 	 * This searches for a defined value in the environment chain and updates
 	 * it. Note that it will only update the value which is closest to this
 	 * environment.
 	 * 
 	 * @param symbol
 	 *            The symbol to update.
 	 * @param value
 	 *            The new value to update.
 	 * @throws InterpreterException
 	 *             if the value does not exist in the chain of environments.
 	 */
 	public void updateValue(Symbol symbol, Object value)
 			throws InterpreterException {
 		Environment current = this;
 
 		// Iterate through the environments rather then recursing to prevent
 		// the stack blowing due to a large environment chain.
 		do {
 			if (current.values.containsKey(symbol)) {
 				current.values.put(symbol, value);
 				return;
 			}
 		} while ((current = current.getParent()) != null);
 
 		throw new InterpreterException("Undefined variable: "
 				+ symbol.getSymbol());
 	}
 
 }
