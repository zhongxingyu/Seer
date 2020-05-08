 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.esciencecenter.vbrowser.vrs.data;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.Date;
 
 import nl.esciencecenter.ptk.data.StringList;
 import nl.esciencecenter.ptk.object.Duplicatable;
 import nl.esciencecenter.ptk.presentation.Presentation;
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
 import nl.esciencecenter.vbrowser.vrs.exceptions.ValueParseException;
 import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
 
 /**
  * This class provides a high level interface to resource Attributes.
  * <p>
  * It is a <code> {type, name, value}</code> <em>triple</em>. 
  * <br>
  * The Attribute itself does not do any type checking, so casting is possible. A
  * getStringValue() after a setValue(int) will return the string representation
  * of the int. <br>
  * 
  * @author P.T. de Boer
  */
 public class Attribute implements Cloneable, Serializable, Duplicatable<Attribute>
 {
     private static final long serialVersionUID = -8999238098979470171L;
 
     private static ClassLogger logger;
     {
         logger = ClassLogger.getLogger(Attribute.class);
     }
 
     private static String[] booleanEnumValues = { "false", "true" };
 
     // ========================================================================
     // Class Methods
     // ========================================================================
 
     /** 
      * Parse String to Object. 
      */
     public static Object parseString(AttributeType toType, String strValue) throws Exception
     {
        if (toType == null)
             return null;

         switch (toType)
         {
             case BOOLEAN:
                 return Boolean.parseBoolean(strValue);
             case INT:
                 return Integer.parseInt(strValue);
             case LONG:
                 return Long.parseLong(strValue);
             case FLOAT:
                 return Float.parseFloat(strValue);
             case DOUBLE:
                 return Double.parseDouble(strValue);
             case STRING:
                 return strValue;
             case VRL:
                 return new VRL(strValue);
             case ENUM:
                 return strValue;
             case DATETIME:
             {
                 return strValue; // keep normalized DateTime String "as is".
             }
             default:
                 throw new ValueParseException("Cannot convert String:'" + strValue + "' to type:" + toType);
         }
     }
 
     private static Object duplicateValue(AttributeType type, Object object)
     {
         if (object == null)
             return null;
 
         switch (type)
         {
             case BOOLEAN:
                 return new Boolean((Boolean) object);
             case INT:
                 return new Integer((Integer) object);
             case LONG:
                 return new Long((Long) object);
             case FLOAT:
                 return new Float((Float) object);
             case DOUBLE:
                 return new Double((Double) object);
             case ENUM: // enums are stored as String
                 return new String((String) object);
             case DATETIME:
             { 
                 if (object instanceof Date)
                 {
                     return ((Date)object).clone(); 
                 }
                 else if (object instanceof String)
                 {
                     return new String((String) object);
                 }
                 else
                 {
                    throw new Error("Invalid DATETIME Type:"+object.getClass());  
                 }
             }
             case STRING:
                 return new String((String) object);
             case VRL:
                 return ((VRL) object).duplicate();
             case ANY:
             default:
             {
                 if (object instanceof Duplicatable)
                     return ((Duplicatable) object).duplicate(false);
 
                 throw new Error("Cannot clone/duplicate value object:" + object);
             }
         }
     }
 
     // ========================================================================
     // Instance
     // ========================================================================
 
     /** 
      * The name of the attribute 
      */
     private String name = null;
 
     /**
      * Generic Object as value. The default is to use String Representation
      */
     private Object value = null;
 
     /** 
      * Whether attribute is editable. 
      */
     private boolean editable = false;
 
     /** 
      * List of enum values, index enumValue determines which enum value is set 
      */
     private StringList enumValues = null;
 
     /** 
      * Index into enumValues[] so that: enumAvalues[enumIndex]==value 
      */
     private int enumIndex = -1;
 
     private boolean changed = false;
 
     /** Copy Constructor */
     public Attribute(Attribute source)
     {
         copyFrom(source);
     }
 
     protected Attribute(String name)
     {
         this.name = name;
     }
     
     /** Constructor to create a enum list of string */
     public Attribute(String name, String enumValues[], int enumVal)
     {
         init(name, new StringList(enumValues), enumVal);
     }
 
     /** Constructor to create a enum list of string */
     public Attribute(String name, StringList enumValues, int enumVal)
     {
         init(name, enumValues, enumVal);
     }
 
     public Attribute(String name, Boolean val)
     {
         init(AttributeType.BOOLEAN, name, val);
     }
 
     public Attribute(String name, Integer val)
     {
         init(AttributeType.INT, name, val);
     }
 
     public Attribute(String name, Long val)
     {
         init(AttributeType.LONG, name, val);
     }
 
     public Attribute(String name, Float val)
     {
         init(AttributeType.FLOAT, name, val);
     }
 
     public Attribute(String name, Double val)
     {
         init(AttributeType.DOUBLE, name, val);
     }
 
     public Attribute(String name, VRL vri)
     {
         init(AttributeType.VRL, name, vri);
     }
 
     public Attribute(String string, URI uri)
     {
         init(AttributeType.VRL, name, new VRL(uri));
     }
 
     /**
      * Create new Enumerated Attribute with enumVals as possible values and
      * defaultVal (which must be element of enumVals) as default.
      */
     public Attribute(String name, String[] enumVals, String defaultVal)
     {
         int index = 0; // use default of 0!
 
         if ((enumVals == null) || (enumVals.length <= 0))
             throw new NullPointerException("Cannot not have empty enum value list !");
         StringList enums = new StringList(enumVals);
 
         // robuustness! add defaultVal if not in enumVals!
         enums.add(defaultVal, true);
 
         index = enums.indexOf(defaultVal);
 
         if (index < 0)
             index = 0;
 
         init(name, enums, index);
     }
 
     /**
      * Named String {Type,Value} Tuple
      */
     public Attribute(String name, String value)
     {
         init(AttributeType.STRING, name, value);
     }
 
     /**
      * Custom named & explicit typed Attribute
      */
     public Attribute(AttributeType type, String name, Object value)
     {
         init(type, name, value);
     }
 
     /**
      * Date attribute, can also be used for Time values.
      */
     public Attribute(String name, Date date)
     {
         init(AttributeType.DATETIME, name, date); // Presentation.createNormalizedDateTimeString(date));
     }
     
     /**
      * Main init method to be called by other constructors. <br>
      * This method may only be used by contructors.
      * Object value must be a private owned copy! 
      */
     protected void init(AttributeType type, String name, Object value)
     {
         this.name = name;
         this.value = value;
         checkValueType(type, value);
     }
 
     protected void checkValueType(AttributeType type, Object value)
     {
         if (type == AttributeType.ANY)
             return;
 
         //Assert here ? 
         // Null type are either ANY or String. 
         if (value==null)
         {
             if (type!=AttributeType.ANY && type!=AttributeType.STRING)
                 throw new Error("Null objects resolve to ANY type (or String)!"); 
         }
         
         // basic object type checks: 
         AttributeType objType = AttributeType.getObjectType(value, AttributeType.STRING);
 
         // DateTime can be stored as normalized date-time string;
         if (type == AttributeType.DATETIME && objType == AttributeType.STRING)
             return;
 
         // Same for ENUM
         if (type == AttributeType.ENUM && objType == AttributeType.STRING)
             return;
 
         if (objType != type)
         {
             throw new Error("Object type is not the same as expected. Expected=" + type + ",parsed=" + objType
                     + ",value" + value);
         }
     }
 
     /** 
      * Initialize as Enum Type 
      */
     protected void init(String name, StringList enumValues, int enumIndex)
     {
         this.name = name;
         this.enumValues = enumValues;
         this.enumIndex = enumIndex;
 
         if ((enumValues != null) && (enumIndex >= 0) && (enumIndex < enumValues.size()))
         {
             this.value = enumValues.get(enumIndex);
         }
         else
         {
             logger.errorPrintf("Error enumIndex out of bound:%d\n", enumIndex);
             value = "";
         }
     }
 
     // master setter:
     private void _setValue(AttributeType type, Object object)
     {
         this.checkValueType(type, object);
         this.value = object;
     }
 
     protected void copyFrom(Attribute source)
     {
         // Duplicate String objects: !
         init(source.getType(), source.name, duplicateValue(source.getType(), source.value));
 
         this.editable = source.editable;
         this.enumIndex = source.enumIndex;
         this.enumValues = source.enumValues;
         this.changed = false; // new Attribute: reset 'changed' flag.
     }
 
     /**
      * Return duplicate of this object. This method returns the same class
      * instead of the object.clone() method All values are copied.
      * 
      * @return
      */
 
     public Attribute clone()
     {
         return new Attribute(this);
     }
 
     public Attribute duplicate()
     {
         return clone();
     }
 
     // =================================================================
     // Instance Getters/Setters
     // =================================================================
   
     /**
      * Get Type of Attribute. 
      */
     public AttributeType getType()
     {
         if (isEnum())
             return AttributeType.ENUM;
         
         return AttributeType.getObjectType(value, AttributeType.ANY); 
     }
     
     public boolean isType(AttributeType type)
     {
         return (getType()==type);
     }
        
     public boolean isEnum()
     {
         return (this.enumValues!=null); 
     }
     
     /**
      * Get Name of Attribute. Note that the Name may never change during the
      * lifetime of an VAttribute !
      */
     public String getName()
     {
         return this.name;
     }
     
     /**
      * Return Actual Object Value.
      */
     public Object getValue()
     {
         return value;
     }
 
     /**
      * Explicit return value as String. Performs toString() if object isn't a
      * string type
      */
     public String getStringValue()
     {
         if (value == null)
             return null;
 
         if (value instanceof String)
             return (String) value;
         
         // use own normalized time-date string; 
         if (value instanceof Date)
             return Presentation.createNormalizedDateTimeString((Date)value); 
 
         return value.toString();
     }
 
     /**
      * Get String array of enumeration types.
      */
     public String[] getEnumValues()
     {
         if (enumValues != null)
             return enumValues.toArray();
 
         if (getType() == AttributeType.BOOLEAN)
             return booleanEnumValues;
 
         return null;
     }
 
     /**
      * Return enum order of current value.
      */
     public int getEnumIndex()
     {
         if (isEnum())
             return enumIndex;
 
         if (getType() == AttributeType.BOOLEAN)
             return (getBooleanValue() ? 1 : 0);
 
         return 0;
     }
 
     public boolean hasEnumValue(String val)
     {
         return this.enumValues.contains(val);
     }
 
     public boolean hasSetValue(String val)
     {
         // Set is Enum:
         return this.hasEnumValue(val);
     }
 
     /**
      * Parse String to Object. 
      */
     public void setValueFromString(AttributeType type, String stringValue) throws Exception
     {
         this.value = parseString(type, stringValue);
     }
 
     public void setValue(int intVal)
     {
         _setValue(AttributeType.INT, new Integer(intVal));
     }
 
     public void setValue(long longVal)
     {
         _setValue(AttributeType.LONG, new Long(longVal));
     }
 
     public void setValue(float floatVal)
     {
         _setValue(AttributeType.FLOAT, new Float(floatVal));
     }
 
     public void setValue(double doubleVal)
     {
         _setValue(AttributeType.DOUBLE, new Double(doubleVal));
     }
 
     /** Reset changed flag. */
     public void setNotChanged()
     {
         this.changed = false;
     }
 
     /** Whether the value has changed since the last setNotChanged() */
     public boolean hasChanged()
     {
         return changed;
     }
 
     /**
      * Return true if this VAttribute is editable. This mean that the setValue()
      * methods can be used to change the value. Note that VAttribute by default
      * or NOT editable. use setEditable() to change this.
      */
     public boolean isEditable()
     {
         return this.editable;
     }
 
     public void setEditable(boolean b)
     {
         this.editable = b;
     }
 
     public int getIntValue()
     {
         if (value == null)
             return 0; // by definition;
 
         switch (getType())
         {
             case INT:
                 return ((Integer) value).intValue();
             case LONG:
                 return ((Long) value).intValue();
             case FLOAT:
                 return ((Float) value).intValue();
             case DOUBLE:
                 return ((Double) value).intValue();
             default:
                 return Integer.parseInt(getStringValue());
         }
     }
 
     public long getLongValue()
     {
         if (value == null)
             return 0; // by definition;
 
         switch (getType())
         {
             case INT:
                 return ((Integer) value).longValue();
             case LONG:
                 return ((Long) value).longValue();
             case FLOAT:
                 return ((Float) value).longValue();
             case DOUBLE:
                 return ((Double) value).longValue();
             default:
                 return Long.parseLong(getStringValue());
         }
     }
 
     public float getFloatValue()
     {
         if (value == null)
             return Float.NaN;
 
         switch (getType())
         {
             case INT:
                 return ((Integer) value).floatValue();
             case LONG:
                 return ((Long) value).floatValue();
             case FLOAT:
                 return ((Float) value).floatValue();
             case DOUBLE:
                 return ((Double) value).floatValue();
             default:
                 return Float.parseFloat(getStringValue()); // auto cast !
         }
     }
 
     public double getDoubleValue()
     {
         if (value == null)
             return Double.NaN;
 
         switch (getType())
         {
             case INT:
                 return ((Integer) value).doubleValue();
             case LONG:
                 return ((Long) value).doubleValue();
             case FLOAT:
                 return ((Float) value).doubleValue();
             case DOUBLE:
                 return ((Double) value).doubleValue();
             default:
                 return Long.parseLong(getStringValue());
         }
     }
 
     public boolean getBooleanValue()
     {
         if (value == null)
             return false; // by definition
 
         switch (getType())
         {
             case INT:
                 return (((Integer) value) != 0);
             case LONG:
                 return (((Long) value) != 0);
             case FLOAT:
                 return (((Float) value) != 0);
             case DOUBLE:
                 return (((Double) value).doubleValue() != 0);
             default:
                 return Boolean.parseBoolean(getStringValue());
         }
     }
 
     /**
      * Return as VRL. 
      * @throws VRLSyntaxException 
      */
     public VRL getVRL() throws VRLSyntaxException 
     {
         if (value == null)
             return null;
 
         if (value instanceof VRL)
             return (VRL) value;
 
         if (value instanceof String)
             return new VRL((String) value);
 
         return new VRL(value.toString());
     }
     
     /**
      * Return as VRL. 
      * Autocasts value to VRL object if possible. Return nulls otherwise!
      */
     public VRL getVRLorNull() 
     {
         if (value == null)
             return null;
 
         if (value instanceof VRL)
             return (VRL) value;
 
         try
         {
             if (value instanceof String)
                 return new VRL((String) value);
 
             return new VRL(value.toString());
         }
         catch (VRLSyntaxException e)
         {
             return null;
         }
     }
     /** Ignore case only makes sense for String like Attributes */
     public int compareToIgnoreCase(Attribute attr2)
     {
         return compareTo(attr2, true);
     }
 
     public int compareTo(Attribute attr2)
     {
         return compareTo(attr2, false);
     }
 
     /**
      * Compares this value to value of other VAttribute 'attr'. 
      * The type of this attribute is used and the other attribute is converted (casted) to this
      * type.
      * 
      * @param attr
      * @return
      */
     public int compareTo(Attribute attr, boolean ignoreCase)
     {
         // NULL comparison MUST match String compare !
         if (this.value == null)
         {
             // / (this.value==null) < (other.value!=null)
             if ((attr != null) && (attr.getValue() != null))
                 return -1;
             else
                 return 0; // null equals null
         }
 
         switch (getType())
         {
             case INT:
             case LONG:
             {
                 // use long both for int,long and time (millis!)
                 if (this.getLongValue() < attr.getLongValue())
                     return -1;
                 else if (this.getLongValue() > attr.getLongValue())
                     return 1;
                 else
                     return 0;
             }
             case DATETIME:
             {
                 // Use Date Value:
                 return this.getDateValue().compareTo(attr.getDateValue());
                 // break;
             }
             case FLOAT:
             {
                 if (this.getFloatValue() < attr.getFloatValue())
                     return -1;
                 else if (this.getFloatValue() > attr.getFloatValue())
                     return 1;
                 else
                     return 0;
                 // break;
             }
             case DOUBLE:
             {
                 if (this.getDoubleValue() < attr.getDoubleValue())
                     return -1;
                 else if (this.getDoubleValue() > attr.getDoubleValue())
                     return 1;
                 else
                     return 0;
             }
             case STRING:
             {
                 return StringUtil.compare((String) value, attr.getStringValue());
             }
             case VRL:
             {
                 VRL vrl=this.getVRLorNull();
                 if (vrl==null)
                     return -1; 
                 // use object compare
                 vrl.compareToObject(attr.getVRLorNull()); 
             }
             case ENUM:
             case ANY:
             default:
             {
                 String s1 = this.toString();
                 String s2 = attr.toString();
 
                 // Default use string reprentation
                 if (attr.getValue() != null)
                 {
                     if (ignoreCase)
                     {
                         return s1.compareToIgnoreCase(s2);
                     }
                     else
                     {
                         return s1.compareTo(s2);
                     }
                 }
                 else
                     return 1; // this >> null
                 // break;
             }
         }
     }
 
     public boolean hasName(String nname)
     {
         return (this.name.compareTo(nname) == 0);
     }
 
     public void setValue(boolean b)
     {
         _setValue(AttributeType.BOOLEAN, new Boolean(b));
     }
 
     public void setValue(AttributeType type, Object value)
     {
         _setValue(type, value);
     }
 
     public Date getDateValue()
     {
         if (this.value instanceof java.util.Date)
             return (Date) this.value;
 
         // Millies since epoch:
         if (getType() == AttributeType.LONG)
             Presentation.createDate(this.getLongValue());
 
         return Presentation.createDateFromNormalizedDateTimeString(getStringValue());
     }
 
     public static boolean isSectionName(String name)
     {
         if (name == null)
             return false;
 
         return (name.startsWith("[") || name.startsWith("-["));
     }
 
     @Override
     public boolean shallowSupported()
     {
         return false;
     }
 
     @Override
     public Attribute duplicate(boolean shallow)
     {
         if (shallow)
             logger.warnPrintf("Asked for a shallow copy when this isn't supported\n");
         return new Attribute(this);
     }
     
     public void setObjectValue(Object newValue)
     {
         this.value = newValue;
     }
 
     // ===============
     // Misc. Fuctions.
     // ===============
     
     /**
      * Pretty Print to String.
      */
     public String toString()
     {
         String enumStr = "";
 
         if (isEnum())
         {
             enumStr = ",{";
 
             if (this.enumValues != null)
                 for (int i = 0; i < this.enumValues.size(); i++)
                 {
                     enumStr = enumStr + enumValues.get(i);
                     if (i + 1 < enumValues.size())
                         enumStr = enumStr + ",";
                 }
 
             enumStr = enumStr + "}";
         }
 
         // convert to VAttribute Triplet
         return "{" 
                 + getType() + "," + name + "," + value + enumStr 
                 + ",[" + ((isEditable()) ? "E" : "") + ((hasChanged()) ? "C" : "") 
                 + "]}";
     }
     
 }
