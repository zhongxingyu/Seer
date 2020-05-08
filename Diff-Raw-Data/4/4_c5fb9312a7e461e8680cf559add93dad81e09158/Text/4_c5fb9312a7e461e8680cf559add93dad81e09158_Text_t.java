 package com.edinarobotics.scouting.definitions.database.types;
 
 /**
  * Defines a text data type for use in scouting data.
  * This data can store any string value that can be stored
  * in a Java class {@code String} value.
  * Many of its methods are implemented similarly to
 * the methods in {@link java.lang.String}.<br/>
 * @see java.lang.String
  */
 public class Text implements Data{
 	private String value;
 	
 	/**
 	 * Constructs a new {@link Text} object representing the
 	 * given {@code String} value.
 	 * @param value The value to store in this object.
 	 */
 	public Text(String value)
 	{
 		this.value = value;
 	}
 	
 	/**
 	 * Returns the {@code String} value stored in this {@link Text} object.
 	 * @return The exact {@code String} value stored in this object.
 	 */
 	public String getValue()
 	{
 		return value;
 	}
 	
 	/**
 	 * Compares this object to another object. The result is
 	 * {@code true} if and only if the argument {@code obj}
 	 * is also a {@link Text} object that
 	 * contains the same {@code String}
 	 * value as this object.
 	 * @param obj The object to be compared to this
 	 * {@link Text} object.
 	 * @return {@code true} if the objects are equal as
 	 * described above, {@code false} otherwise.
 	 */
 	public boolean equals(Object obj)
 	{
 		return value == obj.toString();
 	}
 	
 	/**
 	 * Returns a {@link String} object representing this
 	 * {@link Text} object's value. The value is calculated
 	 * using {@link Long#toString()}.
 	 * @return A {@link String} object representing the
 	 * value of this object in base 10.
 	 * @see Long#toString()
 	 */
 	public String toString()
 	{
 		return getValue();
 	}
 	
 	/**
 	 * Follows the contract of {@link Data#serializeToString()}.
 	 * Gives a {@link String} representation of this object.
 	 * @return A {@link String} representing the value
 	 * stored in this {@link Text} object.
 	 * In this case it is an alias for {@link Text#toString()}.
 	 * @see Data#serializeToString()
 	 * @see Text#toString()
 	 */
 	public String serializeToString()
 	{
 		return getValue();
 	}
 }
