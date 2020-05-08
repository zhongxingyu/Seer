 package uk.co.brotherlogic.jarpur;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import uk.co.brotherlogic.jarpur.replacers.Replacer;
 
 public abstract class TemplatePage extends Page {
 
 	Pattern percMatcher = Pattern.compile("(\\%\\%.*?\\%\\%)");
 
 	DateFormat df = DateFormat.getDateInstance();
 
 	protected abstract Map<String, Object> convertParams(
 			List<String> paramList, Map<String, String> paramMap);
 
 	@Override
 	public String generate(List<String> paramList, Map<String, String> paramMap)
 			throws IOException {
 
 		String className = this.getClass().getCanonicalName();
 
 		// Build the template
 		StringBuffer template_data = new StringBuffer();
 		BufferedReader reader;
 		System.err.println("GENERATE");
 		reader = new BufferedReader(new FileReader(base
 				+ className.replace(".", "/") + ".html"));
 		for (String line = reader.readLine(); line != null; line = reader
 				.readLine())
 			template_data.append(line + "\n");
 
 		// Apply the template
 		return doReplace(template_data.toString(), convertParams(paramList,
 				paramMap));
 	}
 
 	protected Object resolveMethodWithParameter(Object obj, String methodName,
 			Map<String, Object> paramMap) {
 		int firstBracket = methodName.indexOf('(');
 		String method = methodName.substring(0, firstBracket);
 		String parameter = methodName.substring(firstBracket + 1, methodName
 				.length() - 1);
 
 		Method[] methodArr = obj.getClass().getMethods();
 		for (Method method2 : methodArr) {
 			if (method2.getName().equals(method)) {
 				try {
 					return method2.invoke(obj, new Object[] { paramMap
 							.get(parameter) });
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	protected Object resolveMethod(Object obj, String methodName,
 			Map<String, Object> paramMap) {
 
 		if (methodName.contains("("))
 			return resolveMethodWithParameter(obj, methodName, paramMap);
 		try {
 			Class cls = obj.getClass();
 			Method m = cls.getMethod(methodName, new Class[0]);
 			Object ret = m.invoke(obj, new Object[0]);
 			return ret;
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	protected Object resolve(String replace, Map<String, Object> paramMap) {
 
 		String[] elems = replace.split("\\.");
 		if (paramMap.containsKey(elems[0])) {
 			Object obj = paramMap.get(elems[0]);
 			for (int i = 1; i < elems.length; i++) {
 				obj = resolveMethod(obj, elems[i], paramMap);
 			}
 			return obj;
 		}
 
 		return "UNABLE TO REPLACE";
 	}
 
 	Pattern forPattern = Pattern.compile("for (.*?) in (.*?) :(.*?)%");
 
 	/*
 	 * protected StringReplace replaceWithForLoop(StringBuffer buffer, int
 	 * startPoint, int endPoint, Map<String, Object> paramMap) {
 	 * 
 	 * // Get the properties of the for loop Matcher forMatch =
 	 * forPattern.matcher(buffer.substring(startPoint, endPoint)); if
 	 * (forMatch.find()) {
 	 * 
 	 * String label = forMatch.group(3);
 	 * 
 	 * // Find the end of the for loop int startOfEnd =
 	 * buffer.indexOf("%%endfor:" + label + "%%"); String replacementText =
 	 * buffer.substring(endPoint, startOfEnd);
 	 * 
 	 * String replaceString = "";
 	 * 
 	 * String newParam = forMatch.group(1); String collection =
 	 * forMatch.group(2);
 	 * 
 	 * Collection<Object> objects = (Collection<Object>) resolve( collection,
 	 * paramMap); for (Object object : objects) { StringBuffer temp = new
 	 * StringBuffer(replacementText); paramMap.put(newParam, object);
 	 * doReplace(temp, paramMap); replaceString += temp.toString(); }
 	 * 
 	 * paramMap.remove(newParam);
 	 * 
 	 * // Do the replace return new StringReplace(startPoint, startOfEnd + 11 +
 	 * label.length(), replaceString); }
 	 * 
 	 * return null; }
 	 */
 
 	protected String doReplace(String pageText, Map<String, Object> paramMap) {
 		PageParser parser = new PageParser();
 		Replacer repl = parser.parsePage(this, pageText);
 		return repl.process(this, paramMap);
 	}
 
 	protected String convert(Object obj) {
 		if (obj instanceof Calendar)
 			return df.format(((Calendar) obj).getTime());
 		else
 			return obj.toString();
 	}
 }
