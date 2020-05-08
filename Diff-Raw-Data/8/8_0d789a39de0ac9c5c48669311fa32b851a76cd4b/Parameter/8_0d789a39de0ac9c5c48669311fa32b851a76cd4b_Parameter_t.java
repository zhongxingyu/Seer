 package com.alexrnl.commons.arguments;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.alexrnl.commons.utils.object.AutoCompare;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 
 /**
  * A class which describe a parameter.<br />
  * Parameter are comparable, they will be sorted by their order field, then by their required field
  * (field required first) and finally by the shortest name of the parameter available.
  * @author Alex
  */
 public class Parameter implements Comparable<Parameter> {
 	/** The reference to the field of this parameter */
 	private final Field			field;
 	/** The names of this parameter */
 	private final Set<String>	names;
 	/** <code>true</code> if the parameter is required */
 	private final boolean		required;
 	/** The description of the parameter */
 	private final String		description;
 	/** The order of the parameter*/
 	private final int			order;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param field
 	 *        the field of this parameter.
 	 * @param names
 	 *        the names of the parameter.
 	 * @param required
 	 *        if the parameter is required.
 	 * @param description
 	 *        the description of the parameter.
 	 * @param order
 	 *        the order of the parameter.
 	 */
 	public Parameter (final Field field, final Collection<String> names, final boolean required,
 			final String description, final int order) {
 		super();
 		if (names.isEmpty()) {
 			throw new IllegalArgumentException("Cannot create parameter with no names");
 		}
 		
 		this.field = field;
 		this.names = new TreeSet<>(new Comparator<String>() {
 			@Override
 			public int compare (final String o1, final String o2) {
 				final int comparedLength = Integer.valueOf(o1.length()).compareTo(o2.length());
				if (comparedLength != 0) {
 					return comparedLength;
 				}
 				return o1.compareTo(o2);
 			}
 		});
 		this.names.addAll(names);
 		this.required = required;
 		this.description = description;
 		this.order = order;
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param field
 	 *        the field of this parameter.
 	 * @param param
 	 *        the {@link Param} annotation associated to the field.
 	 */
 	public Parameter (final Field field, final Param param) {
 		this(field, Arrays.asList(param.names()), param.required(), param.description(), param.order());
 	}
 	
 	/**
 	 * Return the attribute field.
 	 * @return the attribute field.
 	 */
 	public Field getField () {
 		return field;
 	}
 	
 	/**
 	 * Return the attribute names.
 	 * @return the attribute names.
 	 */
 	@com.alexrnl.commons.utils.object.Field
 	public Set<String> getNames () {
 		return Collections.unmodifiableSet(names);
 	}
 	
 	/**
 	 * Return the attribute required.
 	 * @return the attribute required.
 	 */
 	@com.alexrnl.commons.utils.object.Field
 	public boolean isRequired () {
 		return required;
 	}
 	
 	/**
 	 * Return the attribute description.
 	 * @return the attribute description.
 	 */
 	public String getDescription () {
 		return description;
 	}
 	
 	/**
 	 * Return the attribute order.
 	 * @return the attribute order.
 	 */
 	@com.alexrnl.commons.utils.object.Field
 	public int getOrder () {
 		return order;
 	}
 	
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 	
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof Parameter)) {
 			return false;
 		}
 		return AutoCompare.getInstance().compare(this, (Parameter) obj);
 	}
 	
 	@Override
 	public int compareTo (final Parameter o) {
 		final int compareOrder = Integer.valueOf(getOrder()).compareTo(o.getOrder());
 		// If order is enough to compare both parameters
 		if (compareOrder != 0) {
 			return compareOrder;
 		}
 		// Use the required parameter to compare if the order was not enough
 		final int compareRequired = Boolean.valueOf(o.isRequired()).compareTo(isRequired());
 		if (compareRequired != 0) {
 			return compareRequired;
 		}
 		// Compare their first names in the sets
 		return getNames().iterator().next().compareTo(o.getNames().iterator().next());
 	}
 	
 }
