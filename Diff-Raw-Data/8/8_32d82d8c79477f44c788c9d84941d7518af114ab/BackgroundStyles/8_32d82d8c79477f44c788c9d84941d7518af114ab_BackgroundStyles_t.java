 package org.muis.core.style;
 
 import org.muis.core.MuisAttribute;
 
 /** Contains style attributes pertaining to the background of a widget. These styles are supported by {@link org.muis.core.MuisElement} and
  * all of its subclasses unless the {@link org.muis.core.MuisElement#paintSelf(java.awt.Graphics2D, java.awt.Rectangle)} method is
  * overridden in a way that ignores them. */
 public class BackgroundStyles implements StyleDomain
 {
 	private StyleAttribute<?> [] theAttributes;
 
 	private BackgroundStyles()
 	{
 		theAttributes = new StyleAttribute[0];
 	}
 
 	private void register(StyleAttribute<?> attr)
 	{
 		theAttributes = prisms.util.ArrayUtils.add(theAttributes, attr);
 	}
 
 	private static final BackgroundStyles instance;
 
 	/** The color of a widget's background */
 	public static final StyleAttribute<java.awt.Color> color;
 
 	/** The transparency of a widget's background. Does not apply to the entire widget. */
 	public static final StyleAttribute<Double> transparency;
 
 	/** The radius of widget corners */
 	public static final StyleAttribute<Size> cornerRadius;
 
 	static
 	{
 		instance = new BackgroundStyles();
 		color = StyleAttribute.createStyle(instance, "color", MuisAttribute.colorAttr, new java.awt.Color(255, 255, 255));
 		instance.register(color);
 		transparency = StyleAttribute.createBoundedStyle(instance, "transparency", MuisAttribute.floatAttr, 0d, 0d, 1d);
 		instance.register(transparency);
 		cornerRadius = StyleAttribute.createBoundedStyle(instance, "corner-radius", SizeAttributeType.instance, new Size(3,
 			LengthUnit.pixels), new Size(), null);
		instance.register(cornerRadius);
 	}
 
 	/** @return The style domain for all background styles */
 	public static BackgroundStyles getDomainInstance()
 	{
 		return instance;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "bg";
 	}
 
 	@Override
 	public java.util.Iterator<StyleAttribute<?>> iterator()
 	{
 		return prisms.util.ArrayUtils.iterator(theAttributes);
 	}
 }
