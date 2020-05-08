 package de.goca.logicsim.model;
 
 /**
  * Eine Eigenschaft eines Part-Objektes.
  * 
  * @author Felix Treede
  * @param <T>
  *            the attribute type
  * 
  */
 public class PartAttribute<T>
 {
 	
 	/**
 	 * Das Standard-Attribut "Breite", das, sofern unterstützt, die Verwendung einzelner Gatter ohne Aufwand mehrmals
 	 * nebeneinander ermöglicht.
 	 */
 	public static final PartAttribute<Integer> WIDTH = new PartAttribute<Integer>("width", Integer.class,
 			Integer.valueOf(1), Integer.valueOf(1), null);
 	
 	private String id;
 	
 	private Class<T> valueClass;
 	
 	private T defaultValue;
 	
 	private Comparable<? super T> minimum, maximum;
 	
 	/**
 	 * 
 	 * @param name
 	 *            the attribute id, which should be unique for the part.
 	 * @param valueClass
 	 *            the value type
 	 * @param defaultValue
 	 *            standard-Wert (nicht <code>null</code>)
 	 */
 	public PartAttribute(String name, Class<T> valueClass, T defaultValue)
 	{
 		if (name == null)
 		{
 			throw new NullPointerException("Name may not be null");
 		}
 		if (valueClass == null)
 		{
 			throw new NullPointerException("ValueClass may not be null");
 		}
 		if (defaultValue == null)
 		{
 			throw new NullPointerException("defaultValue may not be null");
 		}
 		
 		this.id = name;
 		this.valueClass = valueClass;
 		this.defaultValue = defaultValue;
 	}
 	
 	/**
 	 * Erlaubt die Spezifikation von Minimal- und Maximalwerten.
 	 * 
 	 * @param id
 	 * @param valueClass
 	 * @param defaultValue
 	 *            standard-Wert (nicht <code>null</code>)
 	 * @param minimum
 	 * @param maximum
 	 */
 	public PartAttribute(String id, Class<T> valueClass, T defaultValue, Comparable<? super T> minimum,
 			Comparable<? super T> maximum)
 	{
 //		this.id = id;
 //		this.valueClass = valueClass;
 //		this.defaultValue = defaultValue;
 		
 		this(id, valueClass, defaultValue);
 		
 		if ((minimum != null && minimum.compareTo(defaultValue) > 0)
 				|| (maximum != null && maximum.compareTo(defaultValue) < 0))
 		{
 			throw new IllegalArgumentException("defaultValue is out of the specified range");
 		}
 		
 		this.minimum = minimum;
 		this.maximum = maximum;
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 		{
 			return true;
 		}
 		if (obj == null)
 		{
 			return false;
 		}
 		if (this.getClass() != obj.getClass())
 		{
 			return false;
 		}
 		PartAttribute<?> other = (PartAttribute<?>) obj;
 		return this.id.equals(other.id);
 	}
 	
 	/**
 	 * Gibt den standard-wert der Eigenschaft zurück.
 	 * 
 	 * @return
 	 */
 	public T getDefaultValue()
 	{
 		return this.defaultValue;
 	}
 	
 	/**
 	 * 
 	 * @return die Eigenschafts-ID
 	 */
 	public String getId()
 	{
 		return this.id;
 	}
 	
 	/**
 	 * 
 	 * @return den Datentyp des Attributs
 	 */
 	public Class<T> getValueClass()
 	{
 		return this.valueClass;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + this.id.hashCode();
 		return result;
 	}
 	
 	/**
 	 * Überprüft, ob der gegebene Wert für diese Eigenschaft erlaubt ist.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public boolean isValid(T value)
 	{
 		if (!this.valueClass.isInstance(value))
 		{
 			return false;
 		}
 		
//FIXME	Warum laufen die Tests durch, wenn das < zu einem > wird?
		if (this.minimum != null && this.minimum.compareTo(value) < 0)
//		if (this.minimum != null && this.minimum.compareTo(value) > 0)
 		{
 			return false;
 		}
 		
//FIXME	Warum laufen die Tests durch, wenn das > zu einem < wird?
		if (this.maximum != null && this.maximum.compareTo(value) > 0)
//		if (this.maximum != null && this.maximum.compareTo(value) < 0)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return this.id;
 	}
 }
