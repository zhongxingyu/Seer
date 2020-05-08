 /*
  * $Id$
  */
 package org.xins.specs;
 
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Exception that indicates that a specified name was invalid for that type of
  * component.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.141
  */
 public final class InvalidNameException extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates the message the constructor can pass up to the superconstructor.
     *
     * @param type
     *    the type of component for which the name is considered invalid,
     *    cannot be <code>null</code>.
     *
     * @param name
     *    the name that is considered invalid, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null || name == null</code>.
     */
    private static final String createMessage(SpecType type, String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("type", type, "name", name);
 
       FastStringBuffer buffer = new FastStringBuffer(80);
       buffer.append("The specified name \"");
       buffer.append(name);
       buffer.append("\" is invalid for a component of type \"");
      buffer.append(type.getTypeName());
       buffer.append("\".");
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>InvalidNameException</code>.
     *
     * @param type
     *    the type of component for which the name is considered invalid,
     *    cannot be <code>null</code>.
     *
     * @param name
     *    the name that is considered invalid, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null || name == null</code>.
     */
    InvalidNameException(SpecType type, String name)
    throws IllegalArgumentException {
 
       super(createMessage(type, name));
 
       _type = type;
       _name = name;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The type of component for which the name is considered invalid. Never
     * <code>null</code>.
     */
    private final SpecType _type;
 
    /**
     * The name that is considered invalid. Never <code>null</code>.
     */
    private final String _name;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the type of component for which the name is considered invalid.
     *
     * @return
     *    the type of component for which the name is considered invalid, never
     *    <code>null</code>.
     */
    private final SpecType getType() {
       return _type;
    }
 
    /**
     * Returns the name that is considered invalid.
     *
     * @return
     *    the name that is considered invalid, never <code>null</code>.
     */
    private final String getName() {
       return _name;
    }
 }
