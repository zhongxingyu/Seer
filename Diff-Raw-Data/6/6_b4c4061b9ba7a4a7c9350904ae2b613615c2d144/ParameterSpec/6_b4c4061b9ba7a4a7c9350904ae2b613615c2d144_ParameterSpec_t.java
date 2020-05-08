 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.spec;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.text.TextUtils;
 import org.xins.common.types.Type;
 
 /**
  * Specification of a parameter.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.3.0
  */
 public final class ParameterSpec {
 
    /**
     * The reference class, never <code>null</code>.
     */
    private final Class _reference;
 
    /**
     * Name of the parameter, never <code>null</code>.
     */
    private final String _parameterName;
 
    /**
     * Type of the parameter, can be <code>null</code>.
     */
    private Type _parameterType;
 
    /**
     * Flags indicating if this parameter is required.
     */
    private final boolean _required;
 
    /**
     * Description of the parameter, never <code>null</code>.
     */
    private final String _description;
 
    /**
     * Creates a new instance of <code>ParameterSpec</code>.
     *
     * @param reference
     *    the reference class, cannot be <code>null</code>.
     *
     * @param name
     *    the name of the parameter, cannot be <code>null</code>.
     *
     * @param type
     *    the type of the parameter, can be <code>null</code>.
     *
     * @param required
     *    <code>true</code> if the parameter is required, <code>false</code> otherwise.
     *
     * @param description
     *    the description of the parameter, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>reference == null || name == null || type == null || description == null</code>.
     *
     * @throws InvalidSpecificationException
     *    if the type is not recognized.
     */
    ParameterSpec(Class reference, String name, String type, boolean required, String description)
    throws IllegalArgumentException, InvalidSpecificationException {
       MandatoryArgumentChecker.check("reference", reference, "name", name, "description", description);
       _reference     = reference;
       _parameterName = name;
       _parameterType = getType(type);
       _required      = required;
       _description   = description;
    }
 
    /**
     * Gets the name of the parameter.
     *
     * @return
     *    the name of the parameter, never <code>null</code>.
     */
    public String getName() {
 
       return _parameterName;
    }
 
    /**
     * Gets the description of the parameter.
     *
     * @return
     *    the description of the parameter, never <code>null</code>.
     */
    public String getDescription() {
 
       return _description;
    }
 
    /**
     * Returns whether the parameter is mandatory.
     *
     * @return
     *    <code>true</code> if the parameter is required, <code>false</code> otherwise.
     */
    public boolean isRequired() {
 
       return _required;
    }
 
    /**
     * Gets the type of the parameter.
     *
     * @return
     *    the type of the parameter, never <code>null</code>.
     */
    public Type getType() {
 
       return _parameterType;
    }
 
    /**
     * Gets the type of the parameter.
     *
     * @param typeName
     *    the name of the type, can be <code>null</code>.
     *
     * @return
     *    the type of the parameter, never <code>null</code>.
     *
     * @throws InvalidSpecificationException
     *    if the type is not recognized.
     */
    private Type getType(String typeName) throws InvalidSpecificationException {
 
       if (typeName == null || typeName.equals("") || typeName.equals("_text")) {
          return org.xins.common.types.standard.Text.SINGLETON;
       } else if (typeName.equals("_int8")) {
          return org.xins.common.types.standard.Int8.SINGLETON;
       } else if (typeName.equals("_int16")) {
          return org.xins.common.types.standard.Int16.SINGLETON;
       } else if (typeName.equals("_int32")) {
          return org.xins.common.types.standard.Int32.SINGLETON;
       } else if (typeName.equals("_int64")) {
          return org.xins.common.types.standard.Int64.SINGLETON;
       } else if (typeName.equals("_float32")) {
          return org.xins.common.types.standard.Float32.SINGLETON;
       } else if (typeName.equals("_float64")) {
          return org.xins.common.types.standard.Float64.SINGLETON;
       } else if (typeName.equals("_boolean")) {
          return org.xins.common.types.standard.Boolean.SINGLETON;
       } else if (typeName.equals("_date")) {
          return org.xins.common.types.standard.Date.SINGLETON;
       } else if (typeName.equals("_timestamp")) {
          return org.xins.common.types.standard.Timestamp.SINGLETON;
       } else if (typeName.equals("_base64")) {
          return org.xins.common.types.standard.Base64.SINGLETON;
       } else if (typeName.equals("_descriptor")) {
          return org.xins.common.types.standard.Descriptor.SINGLETON;
       } else if (typeName.equals("_properties")) {
          return org.xins.common.types.standard.Properties.SINGLETON;
       } else if (typeName.equals("_url")) {
          return org.xins.common.types.standard.URL.SINGLETON;
      } else if (typeName.equals("_hex")) {
         return org.xins.common.types.standard.Hex.SINGLETON;
      } else if (typeName.equals("_list")) {
         return org.xins.common.types.standard.List.SINGLETON;
      } else if (typeName.equals("_set")) {
         return org.xins.common.types.standard.Set.SINGLETON;
       } else if (typeName.charAt(0) != '_') {
          String className = _reference.getName();
          int truncatePos = className.lastIndexOf(".capi.CAPI");
          if (truncatePos == -1) {
             truncatePos = className.lastIndexOf(".api.APIImpl");
          }
          try {
             if (typeName.indexOf('/') != -1) {
                typeName = typeName.substring(typeName.indexOf('/') + 1);
             }
             typeName = TextUtils.firstCharUpper(typeName);
             String typeClassName = className.substring(0, truncatePos) + ".types." + typeName;
             Class typeClass = Class.forName(typeClassName);
             Type type = (Type) typeClass.getField("SINGLETON").get(null);
             return type;
          } catch (Exception ex) {
             throw new InvalidSpecificationException("Invalid type: " + typeName + ".", ex);
          }
       }
       throw new InvalidSpecificationException("Invalid type: " + typeName + ".");
    }
 }
