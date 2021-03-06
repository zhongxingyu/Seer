 /*
  *    Geotools2 - OpenSource mapping toolkit
  *    http://geotools.org
  *    (C) 2002, Geotools Project Managment Committee (PMC)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  */
 package org.geotools.feature;
 
 import com.vividsolutions.jts.geom.Geometry;
 import java.math.*;
 
 
 /**
  * Simple, immutable class to store attributes.  This class should be
  * sufficient for all simple (ie. non-schema) attribute implementations of
  * this interface.
  *
  * @author Rob Hranac, VFNY
  * @author Chris Holmes, TOPP
 * @version $Id: DefaultAttributeType.java,v 1.12 2003/09/10 17:29:56 ianschneider Exp $
  */
 public class DefaultAttributeType implements AttributeType {
     /** Name of this attribute. */
     protected final String name;
 
     /** Class type of this attribute. */
     protected final Class type;
 
     /** Indicates if nulls are allowed for this attribute */
     protected final boolean nillable;
 
     /**
      * Constructor with name and type.
      *
      * @param name Name of this attribute.
      * @param type Class type of this attribute.
      * @param nillable If nulls are allowed for the attribute of this type.
      *
      * @task REVISIT: make this protected?  I think it's only used by facotries
      *       at this time.
      */
     public DefaultAttributeType(String name, Class type, boolean nillable) {
         this.name = (name == null) ? "" : name;
         this.type = (type == null) ? Object.class : type;
         this.nillable = nillable;
     }
 
     /**
      * False, since it is not a schema.
      *
      * @return False.
      */
     public boolean isNested() {
         return false;
     }
 
     /**
      * Gets the name of this attribute.
      *
      * @return The name of this attribute.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Gets the type of this attribute.  All attributes that are assigned to
      * this AttributeType must be an instance of this class.  Subclasses are
      * allowed as well.
      *
      * @return The class that the attribute must match to be valid for this
      *         AttributeType.
      */
     public Class getType() {
         return type;
     }
 
     /**
      * Returns whether nulls are allowed for this attribute.
      *
      * @return true if nulls are permitted, false otherwise.
      */
     public boolean isNillable() {
         return nillable;
     }
 
     /**
      * Override of hashCode.
      *
      * @return hashCode for this object.
      */
     public int hashCode() {
         return name.hashCode() * type.hashCode();
     }
 
     /**
      * Override of equals.
      *
      * @param other the object to be tested for equality.
      *
      * @return whether other is equal to this attribute Type.
      */
     public boolean equals(Object other) {
         if (other == null) {
             return false;
         }
 
         AttributeType att = (AttributeType) other;
 
         if (name == null) {
             if (att.getName() != null) {
                 return false;
             }
         }
 
         if (!name.equals(att.getName())) {
             return false;
         }
 
         if (!type.equals(att.getType())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Returns whether the attribute is a geometry.
      *
      * @return true if the attribute's type is a geometry.
      */
     public boolean isGeometry() {
         return Geometry.class.isAssignableFrom(this.type);
     }
 
     /**
      * Gets a representation of this object as a string.
      *
      * @return A representation of this object as a string
      */
     public String toString() {
         String details = "name=" + name;
         details += (" , type=" + type);
         details += (" , nillable=" + nillable);
 
         return "DefaultAttributeType [" + details + "]";
     }
 
     /**
      * Allows this AttributeType to convert an argument to its prefered storage
      * type. If no parsing is possible, returns the original value. If a parse
      * is attempted, yet fails (i.e. a poor decimal format) throw the
      * Exception. This is mostly for use internally in Features, but
      * implementors should simply follow the rules to be safe.
      *
      * @param value the object to attempt parsing of.
      *
      * @return <code>value</code> converted to the preferred storage of this
      *         <code>AttributeType</code>.  If no parsing was possible then
      *         the same object is returned.
      *
      * @throws IllegalArgumentException if parsing is attempted and is
      *         unsuccessful.
      */
     public Object parse(Object value) throws IllegalArgumentException {
         return value;
     }
 
     /**
      * Whether the tested object passes the validity constraints of  this
      * AttributeType.  At a minimum it should be of the correct class
      * specified by {@link #getType()}, non-null if isNillable is
      * <tt>false</tt>, and a geometry if isGeometry is <tt>true</tt>.  If The
      * object does not validate then an IllegalArgumentException reporting the
      * error in validation should be thrown.
      *
      * @param attribute The object to be tested for validity.
      *
      * @throws IllegalArgumentException if the object does not validate.
      */
     public void validate(Object attribute) throws IllegalArgumentException {
         if (attribute == null) {
             if (!isNillable()) {
                 throw new IllegalArgumentException(getName()
                     + " is not nillable");
             }
 
             return;
         }
 
         if ((attribute != null) && !type.isAssignableFrom(attribute.getClass())) {
             throw new IllegalArgumentException(attribute.getClass().getName()
                 + " is not an acceptable class for " + getName()
                 + " as it is not assignable from " + type);
         }
     }
 
     /**
      * Class that represents a Numeric.
      *
      * @task REVISIT: we need a more coherent and obvious AttributeType
      *       hierarchy.  Better documentation as well.
      */
     static class Numeric extends DefaultAttributeType {
         /**
          * Constructor with name, type and nillable.  Type should always be a
          * Number class.
          *
          * @param name Name of this attribute.
          * @param type Class type of this attribute.
          * @param nillable If nulls are allowed for the attribute of this type.
          *
          * @throws IllegalArgumentException is type is not a Number.
          *
          * @task REVISIT: protected?
          */
         public Numeric(String name, Class type, boolean nillable)
             throws IllegalArgumentException {
             super(name, type, nillable);
 
             if (!Number.class.isAssignableFrom(type)) {
                 throw new IllegalArgumentException(
                     "Numeric requires Number class, " + "not " + type);
             }
         }
 
         /**
          * Allows this AttributeType to convert an argument to its prefered
          * storage type. If no parsing is possible, returns the original
          * value. If a parse is attempted, yet fails (i.e. a poor decimal
          * format) throw the Exception. This is mostly for use internally in
          * Features, but implementors should simply follow the rules to be
          * safe.
          *
          * @param value the object to attempt parsing of.
          *
          * @return <code>value</code> converted to the preferred storage of
          *         this <code>AttributeType</code>.  If no parsing was
          *         possible then the same object is returned.
          *
          * @throws IllegalArgumentException if parsing is attempted and is
          *         unsuccessful.
          * @throws RuntimeException If it is coded wrong.
          *
          * @task REVISIT: When type is Number, should we always be using
          *       Double?
          */
         public Object parse(Object value)
             throws IllegalArgumentException, RuntimeException {
                 
             // handle null values first
             if (value == null) {
                 return value;
             }
 
             // no parse needed here if types are compatable
             if (value.getClass() == type || type.isAssignableFrom(value.getClass()))
                 return value;
             
             // convert one Number to our preferred type
             if (value instanceof Number) 
                 return convertNumber( (Number) value);
             
             // parse a String to our preferred type
             // note, this is the final parsing attempt !
             Object parsed = parseFromString(value.toString());
             
             if (parsed != null)
                 return parsed;
 
             throw new RuntimeException(
                 "DefaultAttributeType.Numeric is coded wrong");
         }
         
         protected Object parseFromString(String value) throws IllegalArgumentException {
             Number parsed = null;
             
             if (type == Byte.class) {
                 return Byte.decode(value);
             }
             
             if (type == Short.class) {
                 return Short.decode(value);
             }
             
             if (type == Integer.class) {
                 return Integer.decode(value);
             }
             
             if (type == Float.class) {
                 return Float.valueOf(value);
             }
             
             if (type == Double.class) {
                 return Double.valueOf(value);
             }
             
             if (type == Long.class) {
                 return Long.decode(value);
             }
             
             if (type == BigInteger.class) {
                 return new BigInteger(value);   
             }
             
             if (type == BigDecimal.class) {
                 return new BigDecimal(value);   
             }
             
             if (Number.class.isAssignableFrom(type)) {
                 return new Double(value);
             }
             
             return null;
         }
         
         protected Object convertNumber(Number number) {
             if (type == Byte.class) {
                 return new Byte(number.byteValue());
             }
             
             if (type == Short.class) {
                 return new Short(number.shortValue());
             }
             
             if (type == Integer.class) {
                 return new Integer(number.intValue());
             }
             
             if (type == Float.class) {
                 return new Float(number.floatValue());
             }
             
             if (type == Double.class) {
                 return new Double(number.doubleValue());
             }
             
             if (type == Long.class) {
                 return new Long(number.longValue());
             }
             
             if (type == BigInteger.class) {
                 return BigInteger.valueOf(number.longValue());
             }
             
             if (type == BigDecimal.class) {
                 return BigDecimal.valueOf(number.longValue());
             }
             
             throw new RuntimeException("DefaultAttribute.Numeric cannot parse " + number);
         }
     }
 
     /**
      * AttributeType that validates a Feature.
      *
      * @task REVISIT: hierarchy?
      */
     static class Feature extends DefaultAttributeType {
         /** The featureType to use for validation. */
         private final FeatureType featureType;
 
         /**
          * Constructor with name, type and nillable.
          *
          * @param name Name of this attribute.
          * @param type The FeatureType to use for validation.
          * @param nillable If nulls are allowed for the attribute of this type.
          */
         public Feature(String name, FeatureType type, boolean nillable) {
             super(name, org.geotools.feature.Feature.class, nillable);
             this.featureType = type;
         }
         
 
         /**
          * Whether the tested object is a Feature and its attributes validate
          * against the featureType.   An IllegalArgumentException reporting
          * the error in validation is thrown if validation fails..
          *
          * @param attribute The object to be tested for validity.
          *
          * @throws IllegalArgumentException if the object does not validate.
          */
         public void validate(Object attribute) throws IllegalArgumentException {
             super.validate(attribute);
 
             org.geotools.feature.Feature att = (org.geotools.feature.Feature) attribute;
 
             if (!(att.getFeatureType().isDescendedFrom(featureType)
                     || att.getFeatureType().equals(featureType))) {
                 throw new IllegalArgumentException(
                     "Not correct FeatureType, expected " + featureType
                     + " got " + att.getFeatureType());
             }
         }
     }
     
     static class Textual extends DefaultAttributeType {
         public Textual (String name, boolean nillable) {
            super(name,CharSequence.class,nillable);
         }
         public Object parse(Object value) throws IllegalArgumentException {
             if (value == null) 
                 return value;
             
             // string is immutable, so lets keep it
             if (value instanceof String)
                 return value;
             
             // other char sequences are not mutable, create a String from it.
             // this also covers any other cases...
             return value.toString();
         }
     }
     
     static class Temporal extends DefaultAttributeType {
         // this might be right, maybe not, but anyway, its a default formatting
         static java.text.DateFormat format = java.text.DateFormat.getInstance();
         public Temporal (String name, boolean nillable) {
             super(name, java.util.Date.class, nillable);
         }
         public Object parse(Object value) throws IllegalArgumentException {
             if (value == null)
                 return value;
             
             if (type.isAssignableFrom(value.getClass()))
                 return value;
             
             if (value instanceof Number) {
                 return new java.util.Date( ((Number)value).longValue() );
             }
             
             if (value instanceof java.util.Calendar) {
                 return ((java.util.Calendar) value).getTime();
             }
             
             try {
                 return format.parse(value.toString());
             } catch (java.text.ParseException pe) {
                 throw new IllegalArgumentException("unable to parse " + value + " as Date");   
             }
         }
     }
 }
