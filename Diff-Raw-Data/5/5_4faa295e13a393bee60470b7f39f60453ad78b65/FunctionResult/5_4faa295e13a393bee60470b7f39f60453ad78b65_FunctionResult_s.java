 /*
  * $Id$
  */
 package org.xins.server;
 
 /**
  * State of a <code>Responder</code>.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class FunctionResult {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>FunctionResult</code> instance.
     */
    public FunctionResult(String code) {
       _builder = new CallResultBuilder();
       _builder.startResponse(code);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The object used to create and store the XML structure of the result.
     */
    private CallResultBuilder _builder;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the object that is responsible for constructing the call result
     * object.
     *
     * @return
     *    the {@link CallResultBuilder}, never <code>null</code>.
     */
    protected CallResultBuilder getResultBuilder() {
       return _builder;
    }
 
    /**
     * Returns the <code>CallResult</code>
     *
     * @return
     *    the {@link CallResult}, never <code>null</code>.
     */
    CallResult getCallResult() {
 
       // If the output parameters are invalid, return an error result
      if (checkOutputParameters() != null) {
         return _errorOutputResult.getResultBuilder();
 
       // Otherwise return the built result
       } else {
          return _builder;
       }
    }
 
    /**
     * Checks that the output parameters are set as specified. If a parameter
     * is missing or if the value for it is invalid, then an
     * <code>InvalidResponseResult</code> is returned. Otherwise the parameters
     * are considered valid, and <code>null</code> is returned.
     *
     * <p>The implementation of this method in class {@link FunctionResult}
     * always returns <code>null</code>.
     *
     * @return
     *    an {@link InvalidResponseResult} instance if at least one output
     *    parameter is missing or invalid, or <code>null</code> otherwise.
     */
    protected InvalidResponseResult checkOutputParameters() {
       return null;
    }
 
    /**
     * Adds an output parameter to the result. The name and the value must
     * both be specified.
     *
     * @param name
     *    the name of the output parameter, not <code>null</code> and not an
     *    empty string.
     *
     * @param value
     *    the value of the output parameter, not <code>null</code> and not an
     *    empty string.
     */
    protected void param(String name, String value) {
       _builder.param(name, value);
    }
 
    /**
     * Gets the value of the specified parameter.
     *
     * @param name
     *    the parameter element name, not <code>null</code>.
     *
     * @return
     *    string containing the value of the parameter element,
     *    not <code>null</code>.
     */
    protected String getParameter(String name) {
       return _builder.getParameter(name);
    }
 
    /**
     * Add a new JDOM element.
     */
    protected void addJDOMElement(org.jdom.Element element) {
       _builder.startTag(element.getName());
       java.util.Iterator itAttributes = element.getAttributes().iterator();
       while (itAttributes.hasNext()) {
          org.jdom.Attribute nextAttribute = (org.jdom.Attribute) itAttributes.next();
          _builder.attribute(nextAttribute.getName(), nextAttribute.getValue());
       }
       java.util.Iterator itSubElements = element.getChildren().iterator();
       while (itSubElements.hasNext()) {
          org.jdom.Element nextChild = (org.jdom.Element) itSubElements.next();
          addJDOMElement(nextChild);
       }
       _builder.endTag();
    }
 }
