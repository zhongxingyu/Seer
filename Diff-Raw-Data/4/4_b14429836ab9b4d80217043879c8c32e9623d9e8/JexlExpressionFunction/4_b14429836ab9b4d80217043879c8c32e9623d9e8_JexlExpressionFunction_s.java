 package org.dawnsci.common.widgets.gda.function.jexl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.expressions.IExpressionEngine;
 import org.dawb.common.services.expressions.IExpressionService;
 
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
 
 // no serial to be consistent with rest of AFunction hierarchy
 @SuppressWarnings("serial")
 public class JexlExpressionFunction extends AFunction {
 	/**
 	 * The name of the required variable (the x in y(x)=...)
 	 */
 	private static final String X = "x";
 	private static final String NAME = "JexlExpressionFunction";
 	private static final String DESC = "y(x) = a jexl expression";
 
 	/**
 	 * Type of errors that {@link JexlExpressionFunction#setExpression(String)}
 	 * can raise.
 	 * <p>
 	 * Apart from {@link #NO_ERROR} all other errors mean that there will be no
 	 * parameters reported by {@link IFunction#getParameters()}
 	 */
 	public static enum JexlExpressionFunctionError {
 		/**
 		 * There is no error with the current expression. It is ready to be
 		 * evaluated.
 		 */
 		NO_ERROR,
 		/**
 		 * There is no expression set.
 		 */
 		NO_EXPRESSION,
 		/**
 		 * The expression engine failed to load/be created. Catastrophic
 		 * failure.
 		 */
 		NO_ENGINE,
 
 		/**
 		 * The expression passed to the expression engine failed. Examine the
 		 * underlying exception (caught from calling setExpression)
 		 */
 		INVALID_EXPRESSION,
 
 		/**
 		 * The expression was parsed by the expression engine, but it had no "x"
 		 * variable so is invalid.
 		 */
 		NO_X,
 	}
 
 	public static class JexlExpressionFunctionException extends Exception {
 		private JexlExpressionFunctionError error;
 
 		private JexlExpressionFunctionException(
 				JexlExpressionFunctionError error) {
 			super(error.toString());
 			this.error = error;
 		}
 
 		public JexlExpressionFunctionException(
 				JexlExpressionFunctionError error, Exception e) {
 			super(error.toString(), e);
 			this.error = error;
 		}
 
 		/**
 		 * Get the Error enum.
 		 *
 		 * @return the error
 		 */
 		public JexlExpressionFunctionError getError() {
 			return error;
 		}
 	}
 
 	private String jexlExpression = null;
 	private transient IExpressionEngine engine;
 	private transient JexlExpressionFunctionError expressionError = JexlExpressionFunctionError.NO_EXPRESSION;
 	private transient Map<String, IParameter> beforeParametersMap;
 	private transient Map<String, Object> jexlLoadedValues;
 	private transient IExpressionService service;
 
 	/**
 	 * Create a JexlExpressionFunction with no expression yet. Until the
 	 * expression is set the error status will be
 	 * {@link JexlExpressionFunctionError#NO_EXPRESSION}
 	 */
 	public JexlExpressionFunction() {
 		super(0);
 		init(null);
 	}
 
 	/**
 	 * Accessible for tests only. The other constructors get the expression
 	 * service from the {@link ServiceManager}
 	 *
 	 * @param service
 	 *            custom Expression Service or <code>null</code> to get it from
 	 *            the {@link ServiceManager}
 	 */
 	public JexlExpressionFunction(IExpressionService service) {
 		super(0);
 		init(service);
 	}
 
 	/**
 	 * Create a JexlExpressionFunction with an initial expression. Convenience
 	 * for calling {@link JexlExpressionFunction#JexlExpressionFunction()}
 	 * followed by {@link JexlExpressionFunction#setExpression(String)}
 	 *
 	 * @param jexlExpression
 	 *            initial expression.
 	 */
 	public JexlExpressionFunction(String jexlExpression) {
 		super(0);
 		init(null);
 		try {
 			setExpression(jexlExpression);
 		} catch (JexlExpressionFunctionException e) {
 			// The error state is saved in expressionError
 		}
 	}
 
 	/**
 	 * Accessible for tests only. The other constructors get the expression
 	 * service from the {@link ServiceManager}
 	 *
 	 * @param service
 	 *            custom Expression Service or <code>null</code> to get it from
 	 *            the {@link ServiceManager}
 	 * @param jexlExpression
 	 *            The jexlExpression to process
 	 */
 	public JexlExpressionFunction(IExpressionService service,
 			String jexlExpression) {
 		super(0);
 		init(service);
 		try {
 			setExpression(jexlExpression);
 		} catch (JexlExpressionFunctionException e) {
 			// The error state is saved in expressionError
 		}
 	}
 
 	private void init(IExpressionService service) {
 		name = NAME;
 		description = DESC;
 
 		try {
 			if (service == null) {
 				service = (IExpressionService) ServiceManager.getService(
 						IExpressionService.class, true);
 			}
 			this.service = service;
 			this.engine = service.getExpressionEngine();
 
 			Map<String, Object> func = engine.getFunctions();
 			// add to functions then set back
 			func.put("func", JexlFunctionConnector.class);
 			engine.setFunctions(func);
 		} catch (Exception e) {
 			// No engine available
 			this.engine = null;
 		}
 	}
 
 	/**
 	 * Returns the current error state. See {@link JexlExpressionFunctionError}
 	 * for details
 	 *
 	 * @return the error state.
 	 */
 	public JexlExpressionFunctionError getExpressionError() {
 		return expressionError;
 	}
 
 	/**
 	 * Get the currently in use expression.
 	 *
 	 * @return the currently in use expression.
 	 */
 	public String getExpression() {
 		return jexlExpression;
 	}
 
 	/**
 	 * Set a new Jexl expression to use.
 	 * <p>
 	 * This method attempts to preserve IParameters from the last successful
 	 * call to setExpression so that modifying an expression in a UI does not
 	 * cause previously set parameters to be lost.
 	 *
 	 * @param jexlExpression
 	 * @throws JexlExpressionFunctionException
 	 */
 	public void setExpression(String jexlExpression)
 			throws JexlExpressionFunctionException {
 		if (expressionError == JexlExpressionFunctionError.NO_ERROR) {
 			// Save the last working set of parameters before updating
 			beforeParametersMap = new HashMap<>();
 			IParameter[] beforeParameters = getParameters();
 			for (IParameter beforeParam : beforeParameters) {
 				beforeParametersMap.put(beforeParam.getName(), beforeParam);
 			}
 		}
 
 		parameters = new IParameter[0];
 		setDirty(true);
 
 		this.jexlExpression = jexlExpression;
 		if (engine == null) {
 			expressionError = JexlExpressionFunctionError.NO_ENGINE;
 			throw new JexlExpressionFunctionException(expressionError);
 		}
 
 		if (jexlExpression == null) {
 			expressionError = JexlExpressionFunctionError.NO_EXPRESSION;
 			throw new JexlExpressionFunctionException(expressionError);
 		}
 
 		try {
 			engine.createExpression(jexlExpression);
 		} catch (Exception e) {
 			// save these errors?
 			expressionError = JexlExpressionFunctionError.INVALID_EXPRESSION;
 			throw new JexlExpressionFunctionException(expressionError, e);
 		}
 
 		Collection<String> parameterNames = engine
 				.getVariableNamesFromExpression();
 		if (!parameterNames.contains(X)) {
 			expressionError = JexlExpressionFunctionError.NO_X;
 			throw new JexlExpressionFunctionException(expressionError);
 		}
 
 		// No error, update parameters
 		expressionError = JexlExpressionFunctionError.NO_ERROR;
 
 		List<IParameter> newParamsList = new ArrayList<>();
 		for (String paramName : parameterNames) {
 			if (X.equals(paramName))
 				continue;
 			if (beforeParametersMap != null
 					&& beforeParametersMap.containsKey(paramName)) {
 				newParamsList.add(beforeParametersMap.get(paramName));
 			} else {
 				Parameter parameter = new Parameter();
 				parameter.setName(paramName);
 				newParamsList.add(parameter);
 			}
 		}
 		parameters = newParamsList
 				.toArray(new IParameter[parameterNames.size() - 1]);
 	}
 
 	private void calcCachedParameters() {
 		int noOfParameters = getNoOfParameters();
 		jexlLoadedValues = new HashMap<String, Object>(noOfParameters + 1);
 		for (int i = 0; i < noOfParameters; i++) {
 			jexlLoadedValues.put(getParameter(i).getName(),
 					getParameterValue(i));
 		}
 
 		setDirty(false);
 	}
 
 	@Override
 	public double val(double... values) {
 		// TODO this isn't actually implemented fully, just
 		// done as an example based on ExpressionFittingExample that
 		// Jacob wrote. One of the question is do we need a different
 		// Jexl expression handler for processing datasets rather than
 		// values
 
 		if (expressionError != JexlExpressionFunctionError.NO_ERROR)
 			// where to record this error?
 			return 0;
 
 		if (isDirty())
 			calcCachedParameters();
 
 		jexlLoadedValues.put(X, values[0]);
 
 		engine.addLoadedVariables(jexlLoadedValues);
 
 		Object ob;
 		try {
 			ob = engine.evaluate();
 		} catch (Exception e) {
 			// where to record this error?
 			return 0;
 		}
 		try {
 			return (double) ob;
 		} catch (ClassCastException cce) {
 			// where to record this error?
 			return 0;
 		}
 	}
 
 	@Override
 	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
 		if (expressionError != JexlExpressionFunctionError.NO_ERROR)
 			// where to record this error?
 			return;
 
 		if (isDirty())
 			calcCachedParameters();
 
 		// TODO review implementation
 		it.reset();
 		double[] coords = it.getCoordinates();
 		int i = 0;
 		double[] buffer = data.getData();
 		while (it.hasNext()) {
 			buffer[i++] = val(coords[0]);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return 31 * super.hashCode()
 				+ ((jexlExpression == null) ? 0 : jexlExpression.hashCode());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!super.equals(obj))
 			return false;
 		JexlExpressionFunction other = (JexlExpressionFunction) obj;
 
 		if (jexlExpression == null)
 			return other.jexlExpression == null;
 		return jexlExpression.equals(other.jexlExpression);
 	}
 
 	@Override
 	public JexlExpressionFunction copy() {
 		IParameter[] localParameters = getParameters();
 		JexlExpressionFunction function = new JexlExpressionFunction(service,
 				jexlExpression);
 
 		for (int i = 0; i < localParameters.length; i++) {
 			IParameter p = localParameters[i];
 			function.parameters[i] = new Parameter(p);
 		}
 		return function;
 	}
 
 	public IExpressionEngine getEngine() {
 		return engine;
 	}
 
 	@Override
 	public boolean isValid() {
 		return super.isValid()
 				&& getExpressionError() == JexlExpressionFunctionError.NO_ERROR;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer out = new StringBuffer();
 		int n = getNoOfParameters();
 		out.append(String.format("'%s %s' has %d parameters:\n", name,
 				getExpression(), n));
 		for (int i = 0; i < n; i++) {
 			IParameter p = getParameter(i);
 			out.append(String.format("%d) %s = %g in range [%g, %g]\n", i,
 					p.getName(), p.getValue(), p.getLowerLimit(),
 					p.getUpperLimit()));
 		}
 		return out.toString();
 	}
 }
