 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.BasicPropertyReader;
 
 /**
  * The data element received from the server when any.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public class DataElement implements Cloneable {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new DataElement with the specified qualified name.
     *
     * @param name
     *    the type of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    DataElement(String name) throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       // Initialize all fields
       _name       = name;
       _children   = new ArrayList();
       _attributes = new BasicPropertyReader();
 
       // XXX: Lazily initialize children and attributes?
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this element. This field is never <code>null</code>.
     */
    private final String _name;
 
    /**
     * The sub-elements of this element. This field is never <code>null</code>.
     */
    private final List _children;
 
    /**
     * The attributes of this elements. This field is never <code>null</code>.
     */
    private final BasicPropertyReader _attributes;
 
    /**
     * The character data content for this element. Can be <code>null</code>.
     */
    private String _text;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the name of the element.
     *
     * @return element
     *    the name of this element, cannot be <code>null</code>.
     */
    public String getName() {
       return _name;
    }
 
    /**
     * Adds a new child to this element.
     *
     * @param element
     *    the new child to add to this element, cannot be <code>null</code>.
     */
    void addChild(DataElement element) {
       _children.add(element);
    }
 
    /**
     * Adds an attribute to this element. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param name
     *    the name of the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value of the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    void addAttribute(String name, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       _attributes.set(name, value);
    }
 
    /**
     * Sets the character data content.
     *
     * @param text
     *    the character data content for this element, can be <code>null</code>.
     */
    void setText(String text) {
       _text = text;
    }
 
    /**
     * Gets the list of the attributes.
     *
     * @return
     *    an {@link Iterator} returning each attribute name as a
     *    {@link String}; can be <code>null</code>, if the DataElement has no
     *    elements.
     */
    public Iterator getAttributes() {
       if (_attributes.size() == 0) {
          return null;
       }
       return _attributes.getNames();
    }
 
    /**
     * Gets the value of an attribute.
     *
     * @param name
     *    the name of the attribute, cannot be <code>null</code>.
     *
     * @return
     *    the value of the attribute, or <code>null</code> if the attribute is
     *    either not set or set to <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String get(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       return _attributes.get(name);
    }
 
    /**
     * Gets the list of all child elements.
     *
     * @return
     *    an unmodifiable {@link List} containing all child elements; each
     *    element in the list is another <code>DataElement</code> instance;
     *    never <code>null</code>.
     */
    public List getChildElements() {
 
       List children;
 
       // If there are no children, then return an immutable empty List
       if (_children.size() == 0) {
          children = Collections.EMPTY_LIST;
 
       // Otherwise return an immutable view of the list of children
       } else {
          children = Collections.unmodifiableList(_children);
       }
 
       return children;
    }
 
    /**
     * Gets the list of child elements that match specified name.
     *
     * @param name
     *    the name for the child elements to match, cannot be
     *    <code>null</code>.
     *
     * @return
     *    a {@link List} containing each child element that matches the
     *    specified name as another <code>DataElement</code> instance;
     *    never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public List getChildElements(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       List matches;
 
       // If there are no children, then return null
       if (_children.size() == 0) {
          matches = Collections.EMPTY_LIST;
 
       // There are children, find all matching ones
       } else {
          matches = new ArrayList();
          Iterator it = _children.iterator();
          while (it.hasNext()) {
             DataElement child = (DataElement) it.next();
             if (name.equals(child.getName())) {
                matches.add(child);
             }
          }
 
          // If there are no matching children, then return null
          if (matches.size() == 0) {
             matches = Collections.EMPTY_LIST;
 
          // Otherwise return an immutable list with all matches
          } else {
             matches = Collections.unmodifiableList(matches);
          }
       }
 
       return matches;
    }
 
    /**
     * Gets the text of this element.
     *
     * @return
     *    the text of this element or <code>null</code> if no text has been
     *    specified for this element.
     */
    public String getText() {
       return _text;
    }
 
    /**
     * Clones this object. The clone will have the same name and equivalent
     * attributes, children and character data content.
     *
     * @return
     *    the clone of this object, never <code>null</code>.
     */
    public Object clone() {
 
       // Construct a new DataElement, copy the name
       DataElement clone = new DataElement(getName());
 
       // Copy the children
      Iterator itChildren = getChildren();
      while (itChildren.hasNext()) {
         clone.addChild((DataElement) ((DataElement)itChildren.next()).clone());
       }
 
       // Copy the attributes
       Iterator itAttributes = getAttributes();
       while (itAttributes.hasNext()) {
          String nextKey = (String) itAttributes.next();
          clone.addAttribute(nextKey, get(nextKey));
       }
 
       // Copy the character data content
       clone.setText(getText());
 
       return clone;
    }
 }
