 package org.jpc.engine.jpl;
 
 import org.jpc.Jpc;
 import org.jpc.engine.prolog.AbstractPrologEngine;
 import org.jpc.query.Query;
 import org.jpc.term.Term;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JplPrologEngine extends AbstractPrologEngine {
 
 	private static Logger logger = LoggerFactory.getLogger(JplPrologEngine.class);
 
 	@Override
 	public void close() {
 		throw new UnsupportedOperationException();
 //		logger.info("Shutting down the JPL prolog engine ...");
 //		boolean result = query(new Atom("halt")).hasSolution(); //WARNING: the Java process would also dye. Commented out until finding another way to halt the JPL logic engine.
 //		if(result)
 //			logger.info("A Jpl prolog engine has been shut down.");
 //		else
 //			logger.warn("Impossible to shut down the prolog engine.");
 //		return result;
 	}
 	
 	@Override
 	public boolean isCloseable() {
 		return false;
 	}
 	
 	@Override
 	public boolean isMultiThreaded() {
 		return true;
 	}
 	
 	@Override
 	public Term asTerm(String termString) {
 		jpl.Term jplTerm = jpl.Util.textToTerm(termString);
 		return JplBridge.fromJplToJpc(jplTerm);
 	}
 	
 	@Override
	public Query basicQuery(Term goal, Jpc context) {
		return new JplQuery(this, goal, context);
 	}
 
 }
