 package wicket.contrib.scriptaculous;
 
 import java.util.Iterator;
 import java.util.Map;
 
 
 public class JavascriptBuilder {
 	private StringBuffer buffer = new StringBuffer();
 
 	public void addLine(String line) {
 		buffer.append(line).append("\n");
 	}
 	public String buildScriptTagString() {
 		return "\n<script type=\"text/javascript\">\n" +
 		buffer.toString() +
 		"</script>\n";
 	}
 
 	public String formatAsJavascriptHash(Map options) {
 		if (options.isEmpty()) {
 			return "{}";
 		}
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("{");
 		for (Iterator iter = options.keySet().iterator(); iter.hasNext();)
 		{
 			String key = (String)iter.next();
 			Object value = options.get(key);
 
 			buffer.append("\n");
 			buffer.append("  ").append(key).append(": ");
 			buffer.append(formatJavascriptValue(value));

			if (iter.hasNext()) {
				buffer.append(", ");
			}
 		}
 		buffer.append("\n");
 		buffer.append("}");
 		return buffer.toString();
 	}
 	private String formatJavascriptValue(Object value) {
 		if (value instanceof String) {
 			return "'" + (String) value + "'";
 		}
 		if (value instanceof Map) {
 			return formatAsJavascriptHash((Map)value);
 		}
 		if (value instanceof Boolean) {
 			return ((Boolean)value).toString();
 		}
 		if (value instanceof JavascriptFunction) {
 			return ((JavascriptFunction)value).getFunction();
 		}
 		return value.toString();
 	}
 	public void addOptions(Map options)
 	{
 		addLine(formatAsJavascriptHash(options));
 	}
 
 	public static class JavascriptFunction {
 		private String function;
 
 		public JavascriptFunction(String function) {
 			this.function = function;
 		}
 		public String getFunction() {
 			return function;
 		}
 	}
 }
