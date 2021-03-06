 package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter;
 
 import java.util.List;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 
 public class OverloadedFunctionInstanceCall {
 	
 	final int[] functions;
 	final int[] constructors;
 	final Frame cf;
 	Frame env;
 	final IValue[] args;
 	final IValue[] constr_args;
 	final Type types;
 	int i = 0;
 	
 	final IValueFactory vf;
 	
 	public OverloadedFunctionInstanceCall(IValueFactory vf, Frame root, Frame cf, int[] functions, int[] constructors, int scopeIn, IValue[] args, Type types) {
 		this.vf = vf;
 		this.cf = cf;
 		this.functions = functions;
 		this.constructors = constructors;
 		this.args = args;
 		// Temporary
 		this.constr_args = new IValue[args.length - 1];
 		for(int i = 0; i < args.length - 1; i++) {
 			this.constr_args[i] = args[i];
 		}
 		this.types = types;
 
 		this.env = root;
 		if(scopeIn != -1) {
 			boolean found = false;
 			for(Frame env = cf; env != null; env = env.previousCallFrame) {
 				if (env.scopeId == scopeIn) {
 					this.env = env;
 					found = true;
 					break;
 				}
 			}
 			if(!found) {
 				throw new RuntimeException("Could not find matching scope when loading a nested overloaded function: " + scopeIn);
 			}
 		}
 		
 	}
 	
 	public OverloadedFunctionInstanceCall(IValueFactory vf, Frame cf, int[] functions, int[] constructors, Frame env, IValue[] args, Type types) {
 		this.vf = vf;
 		this.cf = cf;
 		this.functions = functions;
 		this.constructors = constructors;
 		this.env = env;
 		this.args = args;
 		// Temporary
 		this.constr_args = new IValue[args.length - 1];
 		for(int i = 0; i < args.length - 1; i++) {
 			this.constr_args[i] = args[i];
 		}
 		this.types = types;
 	}
 	
 	public Function nextFunction(List<Function> functionStore) {
 		if(types == null) {
			return functionStore.get(functions[i++]);
 		} else {
 			while(i < functions.length) {
 				Function fun = functionStore.get(functions[i++]);
 				// TODO: Re-think of the cases of polymorphic and varArgs function alternatives
 				// The most straightforward solution would be to check the arity and let pattern matching on formal parameters do the rest
 				for(Type type : types) {
 					if(fun.ftype == type) {
 						return fun;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	public Type nextConstructor(List<Type> constructorStore) {
 		if(types == null) {
 			return constructorStore.get(constructors[0]);
 		} else {
 			for(int index : constructors) {
 				Type constructor = constructorStore.get(index);
 				// TODO: Re-think of the cases of polymorphic and varArgs function alternatives
 				// The most straightforward solution would be to check the arity and let pattern matching on formal parameters do the rest
 				for(Type type : types) {
 					if(constructor == type) {
 						return constructor;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 }
