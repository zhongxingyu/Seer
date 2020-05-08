 package org.muis.core.style;
 
 import java.util.Map;
 
 import org.muis.core.MuisAttribute.AttributeType;
 import org.muis.core.MuisException;
 
 /**
  * A style property that can affect the rendering of MUIS elements
  *
  * @param <T> The type of value the property supports
  */
 public final class StyleAttribute<T>
 {
 	/** The different types of attributes */
 	// public enum AttributeType
 	// {
 	// /** An attribute whose value can be either true or false */
 	// BOOLEAN,
 	// /** An attribute whose value must be an integer */
 	// INT,
 	// /** An attribute whose value must be a number */
 	// FLOAT,
 	// /** An attribute whose value must be one of a series of choices */
 	// ENUM,
 	// /** An attribute whose value must be a color */
 	// COLOR,
 	// /** An attribute specified as a muis type */
 	// MUIS_TYPE,
 	// /** An attribute whose value must match a predefined constant name */
 	// ARBITRARY;
 	// }
 
 	/** The style domain that the attribute belongs to */
 	public final StyleDomain domain;
 
 	/** The name of the attribute */
 	public final String name;
 
 	/** The type of the attribute */
 	public final AttributeType<T> type;
 
 	/** The default value for this attribute */
 	public final T theDefault;
 
 	/** The minimum value for this number attribute--will be Float#NaN if this attribute is not a number attribute. */
 	public final Comparable<T> min;
 
 	/** The maximum value for this number attribute--will be Float#NaN if this attribute is not a number attribute. */
 	public final Comparable<T> max;
 
 	/** An unmodifiable map of name-value pairs for values that may be referred to by name from the style attribute value. */
 	public final Map<String, T> namedValues;
 
 	private StyleAttribute(StyleDomain aDomain, String aName, AttributeType<T> aType, T defValue, Comparable<T> aMin, Comparable<T> aMax,
 		Map<String, T> aNamedValueSet)
 	{
 		domain = aDomain;
 		name = aName;
 		type = aType;
 		min = aMin;
 		max = aMax;
 		theDefault = defValue;
 		if(aNamedValueSet != null)
 		{
 			java.util.HashMap<String, T> copy = new java.util.HashMap<>(aNamedValueSet);
 			aNamedValueSet = java.util.Collections.unmodifiableMap(copy);
 		}
 		namedValues = aNamedValueSet;
 	}
 
 	/**
 	 * Parses a value for this attribute
 	 *
 	 * @param value The string value to parse
 	 * @param classView The class view to use for parsing if needed
 	 * @return A value of this attribute's type (unchecked--may not be valid)
 	 * @throws MuisException If the value cannot be parsed
 	 */
 	public T parse(String value, org.muis.core.MuisClassView classView) throws MuisException
 	{
 		if(namedValues != null && namedValues.containsKey(value))
 			return namedValues.get(value);
 		return type.parse(classView, value);
 	}
 
 	/**
 	 * Validates a value for this attribute
 	 *
 	 * @param value The value that might be set as a value for this attribute
 	 * @return An error to display if the value is not valid. Null if the value is valid.
 	 */
 	public String validate(Object value)
 	{
		if(namedValues.containsValue(value))
 			return null;
 		T val = type.cast(value);
 		if(val == null)
 			return value + " is not an instance of " + type.getType();
 		if(min != null && min.compareTo(val) > 0)
 			return "The value for property " + domain.getName() + "." + name + " must be at least " + min;
 		if(max != null && max.compareTo(val) < 0)
 			return "The value for property " + domain.getName() + "." + name + " must be at most " + max;
 		return null;
 	}
 
 	/**
 	 * Creates a style attribute
 	 *
 	 * @param <T> The type of value for the style attribute
 	 * @param domain The style domain to create the style attribute for
 	 * @param name The name of the style attribute to create
 	 * @param type The java type of values valid for the style attribute
 	 * @param def The default value for the style attribute
 	 * @param namedValues Name-value pairs of values that can be specified by name
 	 * @return The new style attribute
 	 */
 	public static <T> StyleAttribute<T> createStyle(StyleDomain domain, String name, AttributeType<T> type, T def, Object... namedValues)
 	{
 		return createStyle(domain, name, type, def, compileNamedValues(namedValues, type.getType()));
 	}
 
 	/**
 	 * Creates a style attribute
 	 *
 	 * @param <T> The type of value for the style attribute
 	 * @param domain The style domain to create the style attribute for
 	 * @param name The name of the style attribute to create
 	 * @param type The java type of values valid for the style attribute
 	 * @param def The default value for the style attribute
 	 * @param namedValues Name-value pairs of values that can be specified by name
 	 * @return The new style attribute
 	 */
 	public static <T> StyleAttribute<T> createStyle(StyleDomain domain, String name, AttributeType<T> type, T def,
 		Map<String, T> namedValues)
 	{
 		checkTypes(namedValues, type.getType());
 		return new StyleAttribute<T>(domain, name, type, def, null, null, namedValues);
 	}
 
 	/**
 	 * Creates a style attribute with possible minimum and maximum bounds
 	 *
 	 * @param <T> The type of value for the style attribute
 	 * @param domain The style domain to create the style attribute for
 	 * @param name The name of the style attribute to create
 	 * @param type The java type of values valid for the style attribute
 	 * @param def The default value for the style attribute
 	 * @param min The minimum value for the style attribute--may be null to be unspecified
 	 * @param max The maximum value for the style attribute--may be null to be unspecified
 	 * @param namedValues Name-value pairs of values that can be specified by name
 	 * @return The new style attribute
 	 */
 	public static <T extends Comparable<T>> StyleAttribute<T> createBoundedStyle(StyleDomain domain, String name, AttributeType<T> type,
 		T def, Comparable<T> min, Comparable<T> max, Object... namedValues)
 	{
 		return createStyle(domain, name, type, def, compileNamedValues(namedValues, type.getType()));
 	}
 
 	/**
 	 * Creates a style attribute with possible minimum and maximum bounds
 	 *
 	 * @param <T> The type of value for the style attribute
 	 * @param domain The style domain to create the style attribute for
 	 * @param name The name of the style attribute to create
 	 * @param type The java type of values valid for the style attribute
 	 * @param def The default value for the style attribute
 	 * @param min The minimum value for the style attribute--may be null to be unspecified
 	 * @param max The maximum value for the style attribute--may be null to be unspecified
 	 * @param namedValues Name-value pairs of values that can be specified by name
 	 * @return The new style attribute
 	 */
 	public static <T extends Comparable<T>> StyleAttribute<T> createBoundedStyle(StyleDomain domain, String name, AttributeType<T> type,
 		T def, Comparable<T> min, Comparable<T> max, Map<String, T> namedValues)
 	{
 		checkTypes(namedValues, type.getType());
 		return new StyleAttribute<T>(domain, name, type, def, min, max, namedValues);
 	}
 
 	private static <T> Map<String, T> compileNamedValues(Object [] nv, Class<T> type)
 	{
 		if(nv == null || nv.length == 0)
 			return null;
 		if(nv.length % 2 != 0)
 			throw new IllegalArgumentException("Named values must be pairs in the form name, " + type.getSimpleName() + ", name, "
 				+ type.getSimpleName() + "...");
 		java.util.HashMap<String, T> ret = new java.util.HashMap<>();
 		for(int i = 0; i < nv.length; i += 2)
 		{
 			if(!(nv[i] instanceof String) || !type.isInstance(nv[i + 1]))
 				throw new IllegalArgumentException("Named values must be pairs in the form name, " + type.getSimpleName() + ", name, "
 					+ type.getSimpleName() + "...");
 			if(ret.containsKey(nv[i]))
 				throw new IllegalArgumentException("Named value \"" + nv[i] + "\" specified multiple times");
 			ret.put((String) nv[i], type.cast(nv[i + 1]));
 		}
 		return ret;
 	}
 
 	private static <T> void checkTypes(Map<String, T> map, Class<T> type)
 	{
 		if(map == null)
 			return;
 		for(Map.Entry<?, ?> entry : map.entrySet())
 			if(!(entry.getKey() instanceof String) || !type.isInstance(entry.getValue()))
 				throw new IllegalArgumentException("name-value pairs must be typed String, " + type.getSimpleName());
 	}
 }
