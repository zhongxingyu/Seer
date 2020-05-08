 package org.dawnsci.jexl.internal;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.jexl2.Expression;
 import org.apache.commons.jexl2.JexlEngine;
 import org.apache.commons.jexl2.JexlException;
 import org.apache.commons.jexl2.MapContext;
 import org.apache.commons.jexl2.Script;
 import org.dawb.common.services.expressions.ExpressionEngineEvent;
 import org.dawb.common.services.expressions.IExpressionEngine;
 import org.dawb.common.services.expressions.IExpressionEngineListener;
 
 import uk.ac.diamond.scisoft.analysis.dataset.Image;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class ExpressionEngineImpl implements IExpressionEngine{
 	
 	private JexlEngine jexl;
 	private Expression expression;
 	private MapContext context;
 	private HashSet<IExpressionEngineListener> expressionListeners;
 	private Thread job;
 	private Callable<Object> callable;
 	
 	public ExpressionEngineImpl() {
 		//Create the Jexl engine with the DatasetArthmetic object to allows basic
 		//mathematical calculations to be performed on Datasets
 		jexl = new JexlEngine(null, new DatasetArithmetic(false),null,null);
 
 		//Add some useful functions to the engine
 		Map<String,Object> funcs = new HashMap<String,Object>();
 		funcs.put("dnp", Maths.class);
 		funcs.put("dat", JexlGeneralFunctions.class);
 		funcs.put("im",  Image.class);
 		funcs.put("lz",  JexlLazyFunctions.class);
 		funcs.put("bcv", JexlImageProcessingFunctions.class);
 		//TODO determine which function classes should be loaded as default
 //		funcs.put("fft", FFT.class);
 //		funcs.put("plt", SDAPlotter.class);
 		jexl.setFunctions(funcs);
 		
 		expressionListeners = new HashSet<IExpressionEngineListener>();
 	}
 
 	@Override
 	public void createExpression(String expr) throws Exception {
 		this.expression = jexl.createExpression(expr);
 		
 		checkFunctions(expr);
 	}
 
 	/**
 	 * TODO FIXME Must be better way than this...
 	 * @param expr
 	 * @throws Exception
 	 */
 	private void checkFunctions(String expr)  throws Exception {
 		
 		// We do not support the . operator for now because
 		// otherwise http://jira.diamond.ac.uk/browse/SCI-1731
		//if  (expr.indexOf('.')>-1) {
		//	throw new Exception("The dot operator '.' is not supported.");
		//}
 
 		// We now evaluate the expression to try and trap invalid functions.
 		try {
 			
 			final Script script = jexl.createScript(expr);
 			Set<List<String>> names = script.getVariables();
 			Collection<String> vars = unpack(names);
 			
 			final Map<String,Object> dummy = new HashMap<String,Object>(vars.size());
 			for (String name : vars) dummy.put(name, 1);
 			MapContext dCnxt = new MapContext(dummy);
 			
 			expression.evaluate(dCnxt);
 			
 		} catch (JexlException ne) {
 			if (ne.getMessage().toLowerCase().contains("no such function namespace")) {
 				final String  msg = ne.toString();
 				final String[] segs = msg.split(":");
 				throw new Exception(segs[3], ne);
 			}
 
 		} catch (Exception ignored) {
 			// We allow the expression but it might fail later
 		}		
 	}
 
 
 	@Override
 	public Object evaluate() throws Exception {
 		checkAndCreateContext();
 		return expression.evaluate(context);
 	}
 	
 	@Override
 	public void addLoadedVariables(Map<String, Object> variables) {
 		if (context == null) {
 			context = new MapContext(variables);
 			return;
 		}
 		
 		for (String name : variables.keySet()) {
 			context.set(name, variables.get(name));
 		}
 	}
 	@Override
 	public void addLoadedVariable(String name, Object value) {
 		checkAndCreateContext();
 		context.set(name, value);
 	}
 
 	@Override
 	public Map<String, Object> getFunctions() {
 		return jexl.getFunctions();
 	}
 
 	@Override
 	public void setFunctions(Map<String, Object> functions) {
 		jexl.setFunctions(functions);
 	}
 
 	@Override
 	public void setLoadedVariables(Map<String, Object> variables) {
 		context = new MapContext(variables);
 	}
 
 	@Override
 	public Collection<String> getVariableNamesFromExpression() {
 		try {
 			final Script script = jexl.createScript(expression.getExpression());
 			Set<List<String>> names = script.getVariables();
 			return unpack(names);
 		} catch (Exception e){
 			return null;
 		}
 	}
 
 	private Collection<String> unpack(Set<List<String>> dottednames) {
 		Collection<String> ret = new LinkedHashSet<String>();
 		for (List<String> dottedname : dottednames) {
 			if (dottedname.size() == 0) {
 				continue;
 			} else if (dottedname.size() == 1) {
 				ret.add(dottedname.get(0));
 			} else {
 				StringBuilder name = new StringBuilder();
 				for (String namePart : dottedname) {
 					name.append(namePart);
 					name.append(".");
 				}
 				String assembledName = name.substring(0, name.length() - 1);
 				ret.add(assembledName);
 			}
 		}
 		return ret;
 	}
 
 
 	/**
 	 * TODO FIXME Currently does a regular expression. If there was a 
 	 * way in JEXL of getting the variables for a given function it would 
 	 * be better than what we do: which is a regular expression!
 	 * 
 	 * lz:rmean(fred,0)                     -> fred
 	 * 10*lz:rmean(fred,0)                  -> fred
 	 * 10*lz:rmean(fred,0)+dat:mean(bill,0) -> fred
 	 * lz:rmean(fred,0)+lz:rmean(bill,0)    -> fred, bill
 	 * lz:func(fred*10,bill,ted*2)          -> fred, bill, ted
 	 */
 	@Override
 	public Collection<String> getLazyVariableNamesFromExpression() {
         try {
             final String expr = expression.getExpression();
             return getLazyVariables(expr);
         } catch (Exception e){
 			return null;
 		}
 	}
 	
 	private static final Pattern lazyPattern = Pattern.compile("lz\\:[a-zA-Z0-9_]+\\({1}([a-zA-Z0-9_ \\,\\*\\+\\-\\\\]+)\\){1}");
 
 	private Collection<String> getLazyVariables(String expr) {
 
 		final Collection<String> found = new HashSet<String>(4);
         final Matcher          matcher = lazyPattern.matcher(expr);
  		while (matcher.find()) {
 			final String      exprLine = matcher.group(1);
 			final Script      script   = jexl.createScript(exprLine.replace(',','+'));
 			Set<List<String>> names    = script.getVariables();
 			Collection<String> v = unpack(names);
 			found.addAll(v);
 		}
         return found;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
 		 test("");
 		 test("lz:rmean(fred,0)", "fred");
 		 test("10*lz:rmean(fred,0)", "fred");
 		 test("10*lz:rmean(fred,0)+dat:mean(bill,0)", "fred");
 		 test("10*lz:rmean(larry,0)+dat:mean(lz,0)", "larry");
 		 test("10*lz:rmean(larry,0)+lz:mean(lz,0)", "larry", "lz");
 		 test("lz:rmean(fred,0)+lz:rmean(bill,0)", "fred", "bill");
 		 test("lz:func(fred*10,bill,ted*2)", "fred", "bill", "ted");
 		 test("lz:func(fred*10,bill,ted*2)+lz:func(p+10,q+20,r-2)", "fred", "bill", "ted", "p", "q", "r");
 		 
 		 // TODO FIXME This means you cannot use brackets inside lz:xxx( ... )
 		 //test("lz:func(sin(fred)*10,bill,ted*2)", "fred", "bill", "ted");
 	}
 	
 	private static void test(String expr, String... vars) throws Exception {
 		
 		ExpressionEngineImpl     impl  = new ExpressionEngineImpl();
 		final Collection<String> found = impl.getLazyVariables(expr);
 		
 		if (found.size()!=vars.length) throw new Exception("Incorrect number of variables found!");
 		if (!found.containsAll(Arrays.asList(vars))) throw new Exception("Incorrect number of variables found!");
 		System.out.println(found);
 		System.out.println(">>>> Ok <<<<");
 	}
 
 	private void checkAndCreateContext() {
 		if (context == null) {
 			context = new MapContext();
 		}
 	}
 
 	@Override
 	public void addExpressionEngineListener(IExpressionEngineListener listener) {
 		expressionListeners.add(listener);
 	}
 
 
 	@Override
 	public void removeExpressionEngineListener(
 			IExpressionEngineListener listener) {
 		expressionListeners.remove(listener);
 	}
 
 
 	@Override
 	public void evaluateWithEvent(IMonitor mon) {
 		if (expression == null) return;
 
 		final Script script = jexl.createScript(expression.getExpression());
 
 		checkAndCreateContext();
 
 		callable = script.callable(context);
 
 		final ExecutorService service = Executors.newCachedThreadPool();
 		final IMonitor monitor = mon == null ? new IMonitor.Stub() : mon;
 
 
 		if (job == null) {
 			job = new Thread("Expression Calculation") {
 
 				@Override
 				public void run() {
 					String exp = expression.getExpression();
 					try {
 						
 						Future<Object> future = service.submit(callable);
 
 						while (!future.isDone() && !monitor.isCancelled()) {
 							Thread.sleep(100);
 						}
 
 						if (future.isDone()) {
 							Object result = future.get();
 							ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, result, exp);
 							fireExpressionListeners(event);
 							return;
 						}
 
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (Exception e) {
 						ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, e, exp);
 						fireExpressionListeners(event);
 					}
 					
 					ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, null, exp);
 					fireExpressionListeners(event);
 
 					return;
 				}
 				
 			};
 
 		}
 		job.start();
 	}
 
 	private void fireExpressionListeners(ExpressionEngineEvent event) {
 		for (IExpressionEngineListener listener : expressionListeners){
 			listener.calculationDone(event);
 		}
 	}
 
 	@Override
 	public Object getLoadedVariable(String name) {
 		if (context == null) return null;
 		return context.get(name);
 	}
 }
