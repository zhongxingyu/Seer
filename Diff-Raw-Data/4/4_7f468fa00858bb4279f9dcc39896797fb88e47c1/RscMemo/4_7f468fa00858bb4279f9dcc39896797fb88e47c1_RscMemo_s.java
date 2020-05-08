 package org.magnolialang.memo;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.rascalmpl.interpreter.IEvaluatorContext;
 import org.rascalmpl.interpreter.result.ICallableValue;
 import org.rascalmpl.values.ValueFactoryFactory;
 
 public class RscMemo {
 	private final IValueFactory	vf;
 
 
 	public RscMemo() {
 		this(ValueFactoryFactory.getValueFactory());
 	}
 
 
 	public RscMemo(IValueFactory vf) {
 		this.vf = vf;
 	}
 
 
 	public IValue memo(IValue fun, IEvaluatorContext ctx) {
		//if(true)
		//	return fun;
 		if(fun instanceof ICallableValue) {
 			ICallableValue callable = (ICallableValue) fun;
 			callable.getEval().getStdErr().println(fun.getClass().getCanonicalName());
 			ICallableValue memoCallable = new CallableMemo(callable, new MemoContext());
 			return memoCallable;
 		}
 		else
 			throw new org.rascalmpl.interpreter.staticErrors.NonWellformedTypeError("Expected callable argument", ctx.getCurrentAST());
 	}
 }
