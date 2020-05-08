 /*
  * Copyright (c) 2012, Tobi Vollebregt
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.interpreter;
 
 import java.io.IOException;
 
 import org.spoofax.interpreter.ConcreteInterpreter;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.interpreter.terms.ITermPrinter;
 import org.spoofax.jsglr.client.ParseException;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 import org.spoofax.terms.AbstractTermFactory;
 import org.spoofax.terms.StrategoTerm;
 
 /**
  * Interpreter instance that can be tossed around as any StrategoTerm.
  * 
  * @author Tobi Vollebregt
  */
 public class SpoofaxInterpreterTerm extends StrategoTerm {
 
 	private static final long serialVersionUID = -4810841797754395882L;
 
 	private final ConcreteInterpreter interpreter;
 
 	private String lastError;
 
 	public SpoofaxInterpreterTerm(ITermFactory termFactory, IOAgent ioAgent) {
 		super(MUTABLE);
 
 		interpreter = new ConcreteInterpreter(termFactory);
 		interpreter.setIOAgent(ioAgent);
 	}
 
 	public void setLastError(String err) {
 		lastError = err;
 	}
 
 	public String getLastError() {
 		return lastError;
 	}
 
 	public IStrategoTerm current() {
 		return interpreter.current();
 	}
 
 	// Based on org.spoofax.interpreter.ui.SpoofaxInterpreter.
 	public boolean eval(String line) {
 		final IOAgent ioAgent = interpreter.getIOAgent();
 		String err = null;
 
 		setLastError(null);
 
 		try {
			if(!interpreter.parseAndInvoke(line)) {
				ioAgent.printError("command failed");
			}
			return true;
 		} catch (TokenExpectedException e) {
 			err = e.getMessage();
 		} catch (InterpreterErrorExit e) {
 			err = e.getMessage();
 		} catch (BadTokenException e) {
 			err = e.getMessage();
 		} catch (ParseException e) {
 			err = e.getMessage();
 		} catch (InterpreterExit e) {
 			err = e.getMessage();
 		} catch (UndefinedStrategyException e) {
 			err = e.getMessage();
 		} catch (SGLRException e) {
 			err = e.getMessage();
 		} catch (InterpreterException e) {
 			if(e.getCause() != null)
 				err = e.getCause().getMessage();
 			else
 				err = e.getMessage();
 		}
 
 		if (err != null) {
 			ioAgent.printError(err);
 			setLastError(err);
 		}
 
 		return false;
 	}
 
 	public int getSubtermCount() {
 		return 0;
 	}
 
 	public IStrategoTerm getSubterm(int index) {
 		throw new UnsupportedOperationException();
 	}
 
 	public IStrategoTerm[] getAllSubterms() {
 		return AbstractTermFactory.EMPTY;
 	}
 
 	public int getTermType() {
 		return BLOB;
 	}
 
 	public void prettyPrint(ITermPrinter pp) {
 		pp.print(toString());
 	}
 
 	public void writeAsString(Appendable output, int maxDepth)
 			throws IOException {
 		output.append(getClass().getName());
 	}
 
 	@Override
 	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
 		return second == this;
 	}
 
 	@Override
 	protected int hashFunction() {
 		return System.identityHashCode(this);
 	}
 
 }
