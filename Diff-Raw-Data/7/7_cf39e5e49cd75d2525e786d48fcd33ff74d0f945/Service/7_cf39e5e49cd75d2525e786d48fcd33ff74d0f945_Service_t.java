 package com.bradchen.faces.rest;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public final class Service {
 
 	/**
 	 * Regex pattern to match parameters in service urls.
 	 */
 	public static final String PATTERN_PARAM = "\\{([\\w]+)\\}";
 
 	private final String url;
 
 	private final Pattern regex;
 
 	private final Pattern paramPattern;
 
 	private final HttpMethod method;
 
 	private final String mime;
 
 	private final Handler handler;
 
 	public Service(String url, HttpMethod method, String mime,
 			Handler handler) {
 		this.url = url;
 		this.regex = createRegexFromUrl();
 		this.paramPattern = Pattern.compile(PATTERN_PARAM);
 		this.method = method;
 		this.mime = mime;
 		this.handler = handler;
 	}
 
 	private Pattern createRegexFromUrl() {
 		// escape dots
 		String regex = url.replaceAll("\\.", "\\.");
 
 		// replace parameters with patterns
 		regex = regex.replaceAll("\\{[^}]+\\}", "([^/]+)");
 		return Pattern.compile(regex);
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public HttpMethod getHttpMethod() {
 		return method;
 	}
 
 	public String getMime() {
 		return mime;
 	}
 
 	public Handler getHandler() {
 		return handler;
 	}
 
	public boolean serves(HttpMethod httpMethod) {
		if (httpMethod == null) {
 			return true;
 		}
		return method.equals(httpMethod);
 	}
 
 	public boolean matches(String query) {
 		return regex.matcher(query).matches();
 	}
 
 	public Object invoke(Object bean, String query) {
 		try {
 			String[] params = getSortedParameterValues(query);
 			Method method = handler.findMethod(bean);
 			if (method == null) {
 				String message = "Unable to find the handler method specified:" +
 					handler.getMethodName();
 				throw new BeanMethodException(message);
 			}
 			return method.invoke(bean, (Object[])params);
 		} catch (IllegalArgumentException exception) {
 			String message = "Unable to invoke the handler method.";
 			throw new BeanMethodException(message, exception);
 		} catch (IllegalAccessException exception) {
 			String message = "Unable to invoke the handler method.";
 			throw new BeanMethodException(message, exception);
 		} catch (InvocationTargetException exception) {
 			String message = "Unable to invoke the handler method.";
 			throw new BeanMethodException(message, exception);
 		}
 	}
 
 	public String[] getSortedParameterValues(String query) {
 		String[] params = getSortedParameters();
 		Matcher matcher = regex.matcher(query);
 		Map<String, String> map = new HashMap<String, String>();
 		if (matcher.matches()) {
 			for (int i = 1; i <= params.length; i++) {
 				map.put(params[i - 1], matcher.group(i));
 			}
 		}
 
 		String[] result = new String[params.length];
 		for (int i = 0; i < params.length; i++) {
 			result[i] = map.get(handler.getParameters().get(i));
 		}
 		return result;
 	}
 
 	public String[] getSortedParameters() {
 		Matcher matcher = paramPattern.matcher(url);
 		int i = 0;
 		int max = handler.getParameters().size();
 		String[] result = new String[max];
 		while (matcher.find() && (i < max)) {
 			result[i] = matcher.group(1);
 			i++;
 		}
 		return result;
 	}
 
 }
