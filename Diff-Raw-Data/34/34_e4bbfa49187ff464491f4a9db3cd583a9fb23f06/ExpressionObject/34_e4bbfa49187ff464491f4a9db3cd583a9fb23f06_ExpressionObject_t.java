 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawnsci.plotting.expression;
 
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.expressions.IExpressionEngine;
 import org.dawb.common.services.expressions.IExpressionService;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * An object which can be used to hold data about expressions in tables
  * of data (which data is AbstractDataset).
  * 
  * @author fcp94556
  *
  */
 class ExpressionObject implements IExpressionObject {
 	
 	private String expressionName;
 	private String expressionString;
 	private IVariableManager provider;
 	private IExpressionEngine engine;
 	private Reference<ILazyDataset>    lazySet;
 	private Reference<AbstractDataset> dataSet;
 	
 	public ExpressionObject(final IVariableManager provider, String expressionName, String expression) {
 		this.provider         = provider;
 		this.expressionName   = expressionName;
 		this.expressionString = expression;
 		
 		try {
 			IExpressionService service = (IExpressionService)ServiceManager.getService(IExpressionService.class);
 			this.engine = service.getExpressionEngine();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block, find out what happens when there is no service
 			e.printStackTrace();
 		}	
 	}
 
 	/**
 	 * @return Returns the expression.
 	 */
 	public String getExpressionString() {
 		return expressionString;
 	}
 
 	/**
 	 * @param expression The expression to set.
 	 */
 	public void setExpressionString(String expression) {
 		this.dataSet    = null;
 		this.lazySet    = null;
 		this.expressionString = expression;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((expressionName == null) ? 0 : expressionName.hashCode());
 		result = prime
 				* result
 				+ ((expressionString == null) ? 0 : expressionString.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ExpressionObject other = (ExpressionObject) obj;
 		if (expressionName == null) {
 			if (other.expressionName != null)
 				return false;
 		} else if (!expressionName.equals(other.expressionName))
 			return false;
 		if (expressionString == null) {
 			if (other.expressionString != null)
 				return false;
 		} else if (!expressionString.equals(other.expressionString))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return expressionString!=null ? expressionString : "";
 	}
 	
	private String validationReason;
	
 	public boolean isValid(IMonitor monitor) {
 		try {
			validationReason = null;
			
 			if (dataSet!=null) return true;
 			if (lazySet!=null) return true;
 			
 			//if (engine==null) engine = JexlUtils.getDawnJexlEngine();
 			engine.createExpression(expressionString);
 			Collection<String> names = engine.getVariableNamesFromExpression();
 			
 		    for (final String key : names) {
 		    	if (monitor.isCancelled()) return false;
 		    	if (!provider.isVariableName(key, monitor)) return false;
 			}
 			return true;
			
 		} catch (Exception ne) {
			validationReason = ne.getMessage();
 			return false;
 		}
 	}
 	
	public String getInvalidReason() {
		return validationReason;
	}
	
 	public List<String> getInvalidVariables(IMonitor monitor) {
 		List<String> ret = new ArrayList<String>(1);
 		try {			
 			//if (engine==null) engine = JexlUtils.getDawnJexlEngine();
 			engine.createExpression(expressionString);
 			Collection<String> names = engine.getVariableNamesFromExpression();
 			
 		    for (final String key : names) {
 		    	if (monitor.isCancelled()) return ret;
 		    	if (!provider.isVariableName(key, monitor)) ret.add(key);
 			}
 			return ret;
 		} catch (Exception ne) {
 			return null;
 		}
 	}
 
 		
 	public boolean containsVariable(String... variableNames) {
 		if (variableNames==null) return false;
 		try {
 			Collection<String> names = engine.getVariableNamesFromExpression();
 			
 			for (String name : variableNames) {
 				if (names.contains(name)) return true;
 			}
 			return false;
 		} catch (Exception ne) {
 			return false;
 		}
 	}
 	/**
 	 * Just gives a lazy the same size as one of the 
 	 */
 	public ILazyDataset getLazyDataSet(String suggestedName, IMonitor monitor) {
 		
 		if (lazySet!=null&&lazySet.get()!=null) return lazySet.get();
 		if (dataSet!=null&&dataSet.get()!=null) return dataSet.get();
 		lazySet = null;
 		
 		ILazyDataset lazy = null;
 		//if (engine==null) engine = JexlUtils.getDawnJexlEngine();
 		try {
 			engine.createExpression(expressionString);
 			
 			final Collection<String> variableNames = engine.getVariableNamesFromExpression();
 			
 			/**
 			 * TODO FIXME this means you cannot do something like dat:mean(x,0) where 
 			 * x is too large to fit in memory. 
 			 */
 			if (isCustomFunctionExpression(getExpressionString())) { // We evaluate the function in memory
 				
 				lazy = getDataSet(suggestedName, monitor);
 				
 			} else { 
 				
 				// Try to allow for slices meaning larger stacks can be used.
 				// Try for the largest rank.
 				int[] largestShape = null;
 				int   largestRank  = -1;
 			    for (final String variableName : variableNames) {
 			    	if (monitor.isCancelled()) return null;
 			        final ILazyDataset ld = provider.getLazyValue(variableName, monitor);
 			        if (ld!=null) { // We are going to copy it's shape
 			        	if (suggestedName==null) throw new RuntimeException("Please set a name for dataset "+getExpressionString());
 			        	if (ld.getRank()>largestRank) {
 			        		largestRank  = ld.getRank();
 			        		largestShape = ld.getShape();
 			        	}
 			        }
 			    }
 			    
 			    if (largestShape!=null) {
 		        	ILazyLoader loader = new ExpressionLazyLoader(suggestedName, getExpressionString(), provider);
 		        	lazy = new ExpressionLazyDataset(suggestedName, AbstractDataset.FLOAT64, largestShape, loader);
 			    }
 
 			}
 		    
 		} catch (Throwable ignored) {
 			
 		}
 		if (lazy!=null) lazySet = new SoftReference<ILazyDataset>(lazy);
 		return lazy;
 	}
 	
 	private static final Pattern function = Pattern.compile("[a-zA-Z]+\\:(.*)+");
 	
 	private boolean isCustomFunctionExpression(String expr) {
 		return function.matcher(expr).matches();
 	}
 
 	public AbstractDataset getDataSet(String suggestedName, IMonitor mon) throws Exception {
 		
 		if (dataSet!=null&&dataSet.get()!=null) return dataSet.get();
 		
 	    if (expressionString==null||provider==null) return new DoubleDataset();
 	    
 		final Map<String,Object> refs = getVariables(mon);
 		
 		engine.setLoadedVariables(refs);
 		
 		Object output = engine.evaluate();
         
 		//AbstractDataset ads = (AbstractDataset)ex.evaluate(context);
 		
 		if (!(output instanceof AbstractDataset))return null;
 		AbstractDataset ads = (AbstractDataset)output;
 		
 		if (suggestedName==null) {
 			ads.setName(getExpressionString());
 		} else {
 			ads.setName(suggestedName);
 		}
 		
 		if (lazySet!=null && lazySet.get() instanceof ExpressionLazyDataset) {
 			((ExpressionLazyDataset)lazySet.get()).setShapeSilently(ads.getShape());
 		}
 		if (ads!=null) dataSet = new SoftReference<AbstractDataset>(ads);
 		return ads;
 	}
 
 	private Map<String, Object> getVariables(IMonitor monitor) throws Exception {
 		
 		final Map<String,Object> refs = new HashMap<String,Object>(7);
 		
 		//if (engine==null) engine = JexlUtils.getDawnJexlEngine();
 		engine.createExpression(expressionString);
 		final Collection<String> variableNames = engine.getVariableNamesFromExpression();
 		final Collection<String> lazyNames     = engine.getLazyVariableNamesFromExpression();
 		
 	    for (String variableName : variableNames) {
 	    	if (monitor.isCancelled()) return null;
 	    	
 	    	Object data = null;
 	    	if (provider!=null && lazyNames!=null && lazyNames.contains(variableName)) {
 	    		data = provider.getLazyValue(variableName, monitor);
 	    	}
 	    	
 	    	// TODO FIXME - Not always 
 	    	if (provider!=null  && data==null) {
 	    		data = provider.getVariableValue(variableName, monitor);
 	    	}
 	    	if (data!=null) refs.put(variableName, data);
 		}
 	    
 		if (refs.isEmpty()) throw new Exception("No variables recognized in expression.");
 
 	    return refs;
 	}
 
 	/**
 	 * @param provider The provider to set.
 	 */
 	public void setProvider(IVariableManager provider) {
 		this.provider = provider;
 	}
 
 	/**
 	 * Clears the current calculated data set from memory.
 	 * Does not 
 	 */
 	public void clear() {
 		this.dataSet = null;
 		this.lazySet = null;
 	}
 
 
 	/**
 	 * Generates a safe expression name from an
 	 * unsafe data set name. Possibly might not be 
 	 * unique.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public static String getSafeName(String n) {
 		
 		if (n==null) return null;
 		
 		if (n.matches("[a-zA-Z0-9_]+")) return n;
 		
 		final StringBuilder buf = new StringBuilder();
 		for (char c : n.toCharArray()) {
 			if (String.valueOf(c).matches("[a-zA-Z0-9_]")) {
 				buf.append(c);
 			} else {
 				if (buf.length()<1 || "_".equals(buf.substring(buf.length()-1))) continue;
 				buf.append("_");
 			}
 		}
 		
 		if (buf.length()<1) {
 			buf.append("Invalid_name");
 		} else if (buf.substring(0, 1).matches("[0-9]")) {
 			buf.append("var", 0, 3);
 		}
 		
 		return buf.toString();
 	}
 
 	@Override
 	public Map<String, Object> getFunctions() {
 		if (engine==null) return null;
 		return engine.getFunctions();
 	}
 
 	public String getExpressionName() {
 		return expressionName;
 	}
 
 	public void setExpressionName(String expressionName) {
 		this.expressionName = expressionName;
 	}
 	
 	public IVariableManager getVariableManager() {
 		return provider;
 	}
 }
