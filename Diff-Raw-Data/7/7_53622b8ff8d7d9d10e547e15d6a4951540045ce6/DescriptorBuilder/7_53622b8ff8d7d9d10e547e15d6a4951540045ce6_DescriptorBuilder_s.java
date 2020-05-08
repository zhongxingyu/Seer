 /*
  * $Id$
  */
 package org.xins.util.service;
 
 import java.net.MalformedURLException;
 import java.util.StringTokenizer;
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.PropertyReader;
 
 /**
  * Builder that can build a <code>Descriptor</code> object based on a set of
  * properties.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.105
  */
 public final class DescriptorBuilder extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Logger for this class.
     */
    public static final Logger LOG = Logger.getLogger(DescriptorBuilder.class.getName());
 
    /**
     * Delimiter between tokens within a property value. This is the comma
     * character <code>','</code>.
     */
    public static final char DELIMITER = ',';
 
    /**
     * Delimiters between tokens within a property value.
     */
    private static final String DELIMITER_AS_STRING = String.valueOf(DELIMITER);
 
    /**
     * Name identifying an actual service descriptor.
     */
    public static final String SERVICE_DESCRIPTOR_TYPE = "service";
 
    /**
     * Name identifying a group of service descriptors.
     */
    public static final String GROUP_DESCRIPTOR_TYPE = "group";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs the message for the
     * <code>DescriptorBuilder.PropertyValueException</code> constructor.
     *
     * @param propertyName
     *    the name of the property, cannot be <code>null</code>.
     *
     * @param propertyValue
     *    the value of the property, cannot be <code>null</code>.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>propertyName == null || propertyValue == null</code>.
     */
    private static final String createPropertyValueExceptionMessage(String propertyName,
                                                                    String propertyValue,
                                                                    String detail)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("propertyName",  propertyName,
                                      "propertyValue", propertyValue);
 
       // Construct the message
       FastStringBuffer buffer = new FastStringBuffer(70);
       buffer.append("Property \"");
       buffer.append(propertyName);
       buffer.append("\" is set to invalid value \"");
       buffer.append(propertyValue);
      if (message == null) {
          buffer.append("\".");
       } else {
          buffer.append("\": ");
         buffer.append(message);
       }
 
       return buffer.toString();
    }
 
    /**
     * Tokenizes the specified string. The {@link #DELIMITER_AS_STRING} will be
     * used as the token delimiter. Every token will be one element in the
     * returned {@link String} array.
     *
     * @param s
     *    the {@link String} to tokenize, cannot be <code>null</code>.
     *
     * @return
     *    the list of tokens as a {@link String} array, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>.
     */
    private static String[] tokenize(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Create a StringTokenizer
       StringTokenizer tokenizer = new StringTokenizer(s, DELIMITER_AS_STRING);
 
       // Create a new array to store the tokens in
       int count = tokenizer.countTokens();
       String[] tokens = new String[count];
 
       // Copy all tokens into the array
       for (int i = 0; i < count; i++) {
          tokens[i] = tokenizer.nextToken().trim();
       }
 
       return tokens;
    }
 
    /**
     * Builds a <code>Descriptor</code> based on the specified set of
     * properties.
     *
     * @param properties
     *    the properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the base for the property names, cannot be <code>null</code>.
     *
     * @return
     *    the {@link Descriptor} that was built, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws DescriptorBuilder.Exception
     *    if <code>properties == null || propertyName == null</code>.
     */
    public static Descriptor build(PropertyReader properties,
                                   String         propertyName)
    throws IllegalArgumentException, DescriptorBuilder.Exception {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
       return build(properties, propertyName, null);
    }
 
    /**
     * Builds a <code>Descriptor</code> based on the specified set of
     * properties, specifying base property and reference.
     *
     * @param properties
     *    the properties to read from, should not be <code>null</code>.
     *
     * @param baseProperty
     *    the name of the base property, should not be <code>null</code>.
     *
     * @param reference
     *    the name of the reference, relative to the base property, can be
     *    <code>null</code>.
     *
     * @return
     *    the {@link Descriptor} that was built, never <code>null</code>.
     *
     * @throws DescriptorBuilder.Exception
     *    if <code>properties == null || propertyName == null</code>.
     */
    private static Descriptor build(PropertyReader properties,
                                    String         baseProperty,
                                    String         reference)
    throws DescriptorBuilder.Exception {
 
       // Determine the property name
       String propertyName = reference == null
                           ? baseProperty
                           : baseProperty + '.' + reference;
 
       // Get the value of the property
       String value = properties.get(propertyName);
       if (value == null) {
          throw new DescriptorBuilder.Exception("Property \"" + propertyName + "\" not found.");
       }
 
       // Tokenize the value
       String[] tokens = tokenize(value);
       int tokenCount = tokens.length;
       if (tokenCount < 3) {
          throw new PropertyValueException(propertyName, value, "Expected at least 3 tokens.");
       }
 
       // Determine the type
       String descriptorType = tokens[0];
 
       // Parse service descriptor
       if (SERVICE_DESCRIPTOR_TYPE.equals(descriptorType)) {
          if (tokenCount != 3) {
             throw new PropertyValueException(propertyName, value, "Expected URL and time-out.");
          }
          String url = tokens[1];
          int timeOut;
          try {
             timeOut = Integer.parseInt(tokens[2]);
          } catch (NumberFormatException nfe) {
             throw new PropertyValueException(propertyName, value, "Unable to parse time-out.");
          }
 
          try {
             return new ServiceDescriptor(url, timeOut);
          } catch (MalformedURLException exception) {
             LOG.error("URL \"" + url + "\" is malformed.", exception);
             throw new PropertyValueException(propertyName, value, "Malformed URL.");
          }
 
       // Parse group descriptor
       } else if (GROUP_DESCRIPTOR_TYPE.equals(descriptorType)) {
 
          GroupDescriptor.Type groupType = GroupDescriptor.getType(tokens[1]);
          if (groupType == null) {
             throw new PropertyValueException(propertyName, value, "Unrecognized group descriptor type.");
          }
 
          int memberCount = tokenCount - 2;
          Descriptor[] members = new Descriptor[memberCount];
          for (int i = 0; i < memberCount; i++) {
             members[i] = build(properties, baseProperty, tokens[i + 2]);
          }
          return new GroupDescriptor(groupType, members);
 
       // Unrecognized descriptor type
       } else {
          throw new PropertyValueException(propertyName, value, "Expected valid descriptor type: either \"" + SERVICE_DESCRIPTOR_TYPE + "\" or \"" + GROUP_DESCRIPTOR_TYPE + "\".");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>DescriptorBuilder</code>.
     */
    private DescriptorBuilder() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Exception thrown if a service descriptor object could not be built.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.105
     */
    public static class Exception extends java.lang.Exception {
 
       //----------------------------------------------------------------------
       // Constructor
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>DescriptorBuilder.Exception</code> with the
        * spedified detail message.
        *
        * @param message
        *    the detail message, can be <code>null</code>.
        */
       Exception(String message) {
          super(message);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
    }
 
    /**
     * Exception thrown if a service descriptor object could not be built due
     * to an invalid property value.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.105
     */
    public static final class PropertyValueException extends DescriptorBuilder.Exception {
 
       //----------------------------------------------------------------------
       // Constructor
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new
        * <code>DescriptorBuilder.PropertyValueException</code>.
        *
        * @param propertyName
        *    the name of the property, cannot be <code>null</code>.
        *
        * @param propertyValue
        *    the value of the property, cannot be <code>null</code>.
        *
        * @param detail
        *    the detail message, can be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>propertyName == null || propertyValue == null</code>.
        */
       PropertyValueException(String propertyName,
                              String propertyValue,
                              String detail)
       throws IllegalArgumentException {
 
          // Check preconditions, create the exception message and pass it to
          // the superconstructor
          super(createPropertyValueExceptionMessage(propertyName,
                                                    propertyValue,
                                                    detail));
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
    }
 }
