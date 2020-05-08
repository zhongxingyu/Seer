 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.command.helper;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class DBusHelper implements ICommandHelper {
 
 	public static final String MAGIC_MARKER = "[dbus]";
 	
 	public String compileMethodCall(String dest, String path, String command,
			List<String> args) {
		String out = "echo " + MAGIC_MARKER + ";" + "dbus-send --session --print-reply --dest=" + dest 
			+ " --type=\"method_call\" " + path + " " + command;
 		if (args != null) {
 			for (String arg : args) {
				out += " " + arg;
 			}
 		}
		return out;
 	}
 
 	public String compileMethodCall(String dest, String path, String command) {
		return compileMethodCall(dest, path, command, null);
 	}
 
 	public String evaluateArg(String rawArg) {
 		String dataType = "string:";
 		if (rawArg.equals("%b") || rawArg.equals("false") || rawArg.equals("true")) {
 			dataType = "boolean:";
 		}
 		else if (rawArg.equals("%i")) {
 			dataType = "int32:";
 		}
 		else if (rawArg.equals("%u")) {
 			dataType = "uint16:";
 		}
 		else if (rawArg.equals("%f")) {
 			dataType = "double:";
 		}
 		else if (rawArg.startsWith("uint16:")) {
 			// The data type was overridden
 			dataType = "";
 		}
 		else if (rawArg.contains(".")) {
 			// Attempt to parse as a float
 			try {
 				@SuppressWarnings("unused")
 				float test = Float.valueOf(rawArg);
 				dataType = "double:";
 			}
 			catch (NumberFormatException e) {
 				// Continue
 			}
 		}
 		else {
 			// Attempt to parse as an integer
 			try {
 				@SuppressWarnings("unused")
 				int test = Integer.valueOf(rawArg);
 				dataType = "int32:";
 			}
 			catch (NumberFormatException e) {
 				// Continue
 			}
 		}
 		return dataType + rawArg;
 	}
 	
 	public String evaluateOutputAsString(String rawOut) {
 		return evaluateOutputAsString(rawOut, true);
 	}
 	
 	private String evaluateOutputAsString(String rawOut, boolean primitive) {
 		String out = rawOut;
 		if (rawOut.contains("\n   ") || !primitive) {
 			if (primitive) out = rawOut.split("\n   ")[1].trim();
 			if (out.startsWith("int32")) {
 				out = out.substring(6);
 			}
 			else if (out.startsWith("uint16")) {
 				out = out.substring(7);
 			}
 			else if (out.startsWith("double")) {
 				out = out.substring(7);
 			}
 			else if (out.startsWith("string")) {
 				out = out.substring(7);
 				// Remove quotes
 				if (out.startsWith("\"")) {
 					out = out.substring(1, out.length() - 1);
 				}
 			}
 			else if (out.startsWith("boolean")) {
 				out = out.substring(8);
 			}
 			else {
 				// Data type not recognised, return empty string
 				out = "";
 			}
 			out = out.trim();
 		}
 		return out;
 	}
 	
 	public boolean evaluateOutputAsBoolean(String rawOut) {
 		String data = evaluateOutputAsString(rawOut, true);
 		return Boolean.parseBoolean(data);
 	}
 	
 	public Map<String, String> evaluateOutputAsMap(String rawOut) {
 		Map<String, String> out = new HashMap<String, String>();
 		while (rawOut.contains("dict entry(")) {
 			int sectionStart = rawOut.indexOf("dict entry(") + 12;
 			int sectionEnd = rawOut.indexOf(")\n", sectionStart);
 			String section = rawOut.substring(sectionStart, sectionEnd).trim();
 			String[] parts = section.split("\n");
 			parts[0] = parts[0].trim();
 			parts[1] = parts[1].trim();
 			
 			// Get the key/value pair and store in the map
 			String key = parts[0].substring(8, parts[0].length() - 1);
 			String value = evaluateOutputAsString(parts[1].substring(8).trim(), false);
 			out.put(key, value);
 			
 			// Trim off the section
 			rawOut = rawOut.substring(sectionEnd);
 		}
 		return out;
 	}
 }
