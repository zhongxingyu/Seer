 package org.configureme.parser.json;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.anotheria.util.StringUtils;
 
 import org.configureme.Environment;
 import org.configureme.environments.DynamicEnvironment;
 import org.configureme.parser.ArrayParsedAttribute;
 import org.configureme.parser.CompositeParsedAttribute;
 import org.configureme.parser.ConfigurationParser;
 import org.configureme.parser.ConfigurationParserException;
 import org.configureme.parser.ParsedAttribute;
 import org.configureme.parser.ParsedConfiguration;
 import org.configureme.parser.PlainParsedAttribute;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * ConfigurationParser implementation for JSON.
  * @author lrosenberg
  */
 public class JsonParser implements ConfigurationParser {
 	/**
 	 * The prefix string distinguishing names of environment JSON objects from composite JSON objects.
 	 * The names of last ones shall starts with this prefix.
 	 */
 	private static final String COMPOSITE_ATTR_PREFIX = "@";
 
 	@Override
 	public ParsedConfiguration parseConfiguration(String name, String content) throws ConfigurationParserException {
 
 		content = StringUtils.removeCComments(content);
 		content = StringUtils.removeCPPComments(content);
 
 		try {
 			JSONObject j = new JSONObject(content);
 			ParsedConfiguration pa = new ParsedConfiguration(name);
 
 			DynamicEnvironment env = new DynamicEnvironment();
 
 			String[] names = JSONObject.getNames(j);
 			if(names != null)
 				for (String key : names) {
 					List<? extends ParsedAttribute<?>> attList = parse(key, j.get(key), env);
 					for (ParsedAttribute<?> att : attList)
 						pa.addAttribute(att);
 				}
 
 			return pa;
 
 		} catch (JSONException e) {
 			throw new ConfigurationParserException("JSON Error", e);
 		}
 	}
 
 	private static List<? extends ParsedAttribute<?>> parse(String key, Object value, DynamicEnvironment environment) throws JSONException{
 		// an object value means a change in environment, let's see what it is
 		if (value instanceof JSONObject && key.startsWith(COMPOSITE_ATTR_PREFIX))
			return Arrays.asList(parseComposite(key, (JSONObject) value, environment));
		else if (value instanceof JSONArray && key.startsWith(COMPOSITE_ATTR_PREFIX))
			return Arrays.asList(parseArray(key, (JSONArray) value, environment));
		else if (value instanceof JSONObject)
			return parseObject(key, (JSONObject) value, environment);
 		else if (value instanceof JSONArray)
 			return Arrays.asList(parseArray(key, (JSONArray) value, environment));
 		else
			return Arrays.asList(new PlainParsedAttribute(key, (Environment)environment.clone(), JSONObject.NULL.equals(value) ? null : value.toString()));
 	}
 
 	private static List<ParsedAttribute<?>> parseObject(String key, JSONObject value, DynamicEnvironment environment) throws JSONException {
 		List<ParsedAttribute<?>> parsed = new ArrayList<ParsedAttribute<?>>();
 
 		environment.extendThis(key);
 		try {
 			String[] names = JSONObject.getNames(value);
 			if(names != null)
 				for (String subKey : names)
 					parsed.addAll(parse(subKey, value.get(subKey), environment));
 		} finally {
 			environment.reduceThis();
 		}
 
 		return parsed;
 	}
 
 	private static CompositeParsedAttribute parseComposite(String key, JSONObject value, DynamicEnvironment environment) throws JSONException {
		String[] names = JSONObject.getNames(value);
		if (names == null)
			return new CompositeParsedAttribute(stripKey(key), (Environment) environment.clone(), Collections.<ParsedAttribute<?>>emptyList());
 
		List<ParsedAttribute<?>> leafAttr = new ArrayList<ParsedAttribute<?>>();
		for (String subKey : names)
			leafAttr.addAll(parse(subKey, value.get(subKey), environment));
 
		return new CompositeParsedAttribute(stripKey(key), (Environment) environment.clone(), leafAttr);
 	}
 
 	private static ArrayParsedAttribute parseArray(String key, JSONArray value, DynamicEnvironment environment) throws JSONException {
 		List<ParsedAttribute<?>> parsed = new ArrayList<ParsedAttribute<?>>(value.length());
 		for (int index = 0; index < value.length(); index++)
 			parsed.addAll(parse(key, value.get(index), environment));
 
 		return new ArrayParsedAttribute(stripKey(key), (Environment) environment.clone(), parsed);
 	}
 
 	private static String stripKey(String key) {
 		return key.startsWith(COMPOSITE_ATTR_PREFIX)
 				? key.substring(COMPOSITE_ATTR_PREFIX.length())
 				: key;
 	}
 }
