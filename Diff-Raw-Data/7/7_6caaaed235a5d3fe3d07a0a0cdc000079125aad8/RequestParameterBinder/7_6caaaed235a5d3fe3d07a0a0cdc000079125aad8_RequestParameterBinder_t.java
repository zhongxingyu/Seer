 /**
  * Copyright (c) 2010 Ignasi Barrera
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.sjmvc.binding;
 
 import java.util.Map;
 
 import javax.servlet.ServletRequest;
 
 /**
  * {@link Binder} implementation that binds request parameters to the defined
  * target object.
  * 
  * @author Ignasi Barrera
  * 
  * @see Binder
  * @see BindingResult
  * @see BindingError
  */
 public class RequestParameterBinder<T> extends
 		AbstractBinder<T, ServletRequest>
 {
 	/** Prefix for the parameters that will be included in the binding process. */
 	private static final String BIND_PARAMETER_PREFIX = "model.";
 
 	/**
 	 * Creates the binder.
 	 * 
 	 * @param target The target of the binding.
 	 * @param source The request source of the binding.
 	 */
 	public RequestParameterBinder(T target, ServletRequest source)
 	{
 		super(target, source);
 	}
 
 	/**
 	 * Bind the given parameter map to the target object.
 	 * 
 	 * @param parameters The parameter map to bind.
 	 * @return The binding result.
 	 */
 	@Override
 	protected void doBind()
 	{
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> parameters = source.getParameterMap();
 
 		for (Map.Entry<String, String[]> param : parameters.entrySet())
 		{
			String paramName = param.getKey();
 			String[] values = param.getValue();
 
 			// Only bind the bindable parameters
			if (paramName.startsWith(BIND_PARAMETER_PREFIX))
 			{
				String name = paramName.replaceFirst(BIND_PARAMETER_PREFIX, "");
 				bindField(target, name, values);
 			}
 		}
 	}
 
 }
