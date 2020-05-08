 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.xml;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Builder for <code>Element</code> instances.
  *
  * <p>Note that this class is not thread-safe. It should not be used from
  * different threads at the same time. This applies even to read operations.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class ElementBuilder extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class. This field is not <code>null</code>.
     */
    private static final String CLASSNAME = ElementBuilder.class.getName();
 
    /**
     * Initial state for the builder.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * State that indicates that the builder has started to build the element.
     */
    private static final State STARTED = new State("STARTED");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>ElementBuilder</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
     */
    public ElementBuilder() {
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       _state = INITIAL;
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
 
    /**
     * Creates a new <code>ElementBuilder</code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    public ElementBuilder(String localName)
    throws IllegalArgumentException {
       this(null, localName);
    }
 
    /**
     * Creates a new <code>ElementBuilder</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null<code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    public ElementBuilder(String namespaceURI, String localName)
    throws IllegalArgumentException {
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       _state = INITIAL;
       startElement(namespaceURI, localName);
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The current state. Never <code>null</code>.
     */
    private State _state;
 
    /**
     * The current Element that this builder is building. Never <code>null</code>.
     */
    private Element _element;
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public void setAttribute(String localName, String value)
    throws IllegalArgumentException {
       if (_state == INITIAL) {
          final String METHODNAME = "setAttribute(String,String)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       setAttribute(null, localName, value);
    }
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null<code>.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public void setAttribute(String namespaceURI, String localName, String value)
    throws IllegalArgumentException, IllegalStateException {
       if (_state == INITIAL) {
          final String METHODNAME = "setAttribute(String,String,String)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       _element.setAttribute(namespaceURI, localName, value);
    }
 
    /**
     * Adds a new child element.
     *
     * @param child
     *    the new child to add to this element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>child == null || child == <em>this</em></code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public void addChild(Element child) 
    throws IllegalArgumentException, IllegalStateException {
       if (_state == INITIAL) {
          final String METHODNAME = "addChild(Element)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       _element.addChild(child);
    }
 
    /**
     * Sets the character content. The existing character content, if any, is
     * replaced
     *
     * @param text
     *    the character content for this element, or <code>null</code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    void setText(String text) throws IllegalStateException {
       if (_state == INITIAL) {
          final String METHODNAME = "setText(String)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       _element.setText(text);
    }
 
    /**
     * Starts to create a new {@link Element} with the given local name.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public void startElement(String localName) {
       if (_state != INITIAL) {
          final String METHODNAME = "startElement(String)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       startElement(null, localName);
    }
 
    /**
     * Starts to create a new <code>Element</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null<code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public void startElement(String namespaceURI, String localName)
    throws IllegalArgumentException {
       if (_state != INITIAL) {
          final String METHODNAME = "startElement(String, String)";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       _element = new Element(namespaceURI, localName);
       _state = STARTED;
    }
 
    /**
     * Creates the {@link Element}.
     * If you want to reuse this <code>ElementBuilder</code> you will need
     * to call the {@link #startElement(String)} or 
     * {@link #startElement(String,String)} method.
     *
     * @throws IllegalStateException
     *    if the <code>ElementBuilder</code> is in an incorrect state.
     */
    public Element createElement() {
       if (_state != STARTED) {
          final String METHODNAME = "createElement()";
          String message = "_state=" + _state;
          Log.log_1050(CLASSNAME, METHODNAME, message);
          throw new IllegalStateException("Unexpected state: " + message + ". Programming error suspected.");
       }
       _state = INITIAL;
       return _element;
    }
 
    /**
     * State of the builder.
     *
     * @version $Revision$ $Date$
     */
    private static final class State extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>State</code> object.
        *
        * @param name
        *    the name of this state, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       private State(String name) throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          _name = name;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The name of this state. Cannot be <code>null</code>.
        */
       private final String _name;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the name of this state.
        *
        * @return
        *    the name of this state, cannot be <code>null</code>.
        */
       public String getName() {
          return _name;
       }
 
       /**
        * Returns a textual representation of this object.
        *
        * @return
        *    the name of this state, never <code>null</code>.
        */
       public String toString() {
          return _name;
       }
    }
 }
