 /*
  * $Id: XMLOutputter.java,v 1.119 2007/08/21 21:50:52 znerd Exp $
  */
 package org.znerd.xmlenc;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 
 /**
  * Stream-based XML outputter. Instances of this class are able to write XML
  * output to {@link Writer Writers}.
  *
  * <h3>Standards compliance</h3>
  *
  * This class is intended to produce output that conforms to the
  * <a href="http://www.w3.org/TR/2000/REC-xml-20001006">XML 1.0
  * Specification</a>. However, not all applicable restrictions are validated.
  * For example, it is currently not checked if names contain characters that
  * are invalid within a <em>Name</em> production.
  *
  * <p />Furthermore, not all possible XML documents can be produced. The
  * following limitations apply:
  *
  * <ul>
  *    <li>the name of the applicable encoding is always printed in the XML
  *        declaration, even though it may not be necessary;</li>
  *    <li>the <code>standalone</code> attribute is not supported in the XML
  *        declaration;</li>
  *    <li>internal DTD subsets are not supported;</li>
  *    <li>the strategy for inserting spacing is fixed: whitespace is kept to
  *        the minimum, still whitespace can be inserted manually using
  *        {@link #whitespace(String)};</li>
  *    <li>XML Namespaces are not explicitly supported, although this can be
  *        implemented on top of this class.</li>
  * </ul>
  *
  * <h3>Supported encodings</h3>
  *
  * The following encodings are supported:
  *
  * <ul>
  *    <li>UTF-8</li>
  *    <li>UTF-16</li>
  *    <li>ISO-10646-UCS-2</li>
  *    <li>ISO-10646-UCS-4</li>
  *    <li>ISO-10646-UTF-1</li>
  *    <li>US-ASCII (also known as ASCII)</li>
  *    <li>ISO-8859-<em>n</em>, where <em>n</em> is the part number</li>
  * </ul>
  *
  * <h3>Multi-threading</h3>
  *
  * This class is <em>not</em> thread-safe. Do not use it from multiple threads
  * at the same time.
  *
  * <h3>Exceptions</h3>
  *
  * Note that all methods check the state first and then check the
  * arguments. This means that if the state is incorrect and the arguments are
  * incorrect, then an {@link IllegalStateException} will be thrown.
  *
  * <p />If any of the writing methods generates an {@link IOException}, then
  * the state will be set to {@link #ERROR_STATE} and no more output can be
  * performed.
  *
  * <h3>Performance hints</h3>
  *
  * It is usually a good idea to let <code>XMLOutputter</code> instances
  * write to buffered {@link Writer Writers}. This typically improves
  * performance on large documents or relatively slow or blocking output
  * streams.
  *
  * <p />Instances of this class can be cached in a pool to reduce object
  * creations. Call {@link #reset()} (with no arguments) when storing an
  * instance in the pool. Use {@link #reset(Writer,String)} (with 2 arguments)
  * to re-initialize the instance after fetching it from the pool.
  *
  * @version $Revision: 1.119 $ $Date: 2007/08/21 21:50:52 $
  * @author Ernst de Haan (<a href="mailto:ernst@ernstdehaan.com">ernst@ernstdehaan.com</a>)
  * @author Jochen Schwoerer (j.schwoerer [at] web.de)
  *
  * @since xmlenc 0.19
  */
 public class XMLOutputter
 extends Object
 implements StatefulXMLEventListener {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Default indentation. This is the empty string, <code>""</code>, since by
     * default no indentation is performed.
     */
    public static final String DEFAULT_INDENTATION = "";
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XMLOutputter</code>. This sets the state to
     * {@link #UNINITIALIZED}.
     */
    public XMLOutputter() {
       _state            = XMLEventListenerStates.UNINITIALIZED;
       _elementStack     = new String[16];
       _quotationMark    = '"';
       _escapeAmpersands = true;
       _lineBreak        = LineBreak.NONE;
       _lineBreakChars   = _lineBreak._lineBreakChars;
       _indentation      = DEFAULT_INDENTATION;
    }
 
    /**
     * Constructs a new <code>XMLOutputter</code> for the specified
     * <code>Writer</code> and encoding. This sets the state to
     * {@link #BEFORE_XML_DECLARATION}.
     *
     * <p />The encoding will be stored exactly as passed, leaving the case
     * intact.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @param encoding
     *    the encoding, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #UNINITIALIZED} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #ERROR_STATE}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out == null || encoding == null</code>.
     *
     * @throws UnsupportedEncodingException
     *    if the specified encoding is not supported.
     */
    public XMLOutputter(Writer out, String encoding)
    throws IllegalStateException,
           IllegalArgumentException,
           UnsupportedEncodingException {
 
       this();
 
       // Initialize
       reset(out, encoding);
    }
 
 
    /**
     * Constructs a new <code>XMLOutputter</code> for the specified
     * <code>Writer</code> and <code>encoder</code>. This sets the state to
     * {@link #BEFORE_XML_DECLARATION}.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @param encoder
     *    the encoder, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #UNINITIALIZED} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #ERROR_STATE}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out == null || encoder == null</code>.
     *
     * @throws UnsupportedEncodingException
     *    if the specified encoding is not supported.
     */
    public XMLOutputter(Writer out, XMLEncoder encoder)
    throws IllegalStateException,
           IllegalArgumentException,
           UnsupportedEncodingException {
 
       this();
 
       // Initialize
       reset(out, encoder);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Flag that indicates whether anything has been output already.
     */
    private boolean _anythingOutput;
 
    /**
     * The output stream this outputter will write to.
     *
     * <p>This field is initialized by the constructor. It can never be
     * <code>null</code>.
     *
     * <p />The value of this field is returned by {@link #getWriter()}.
     */
    private Writer _out;
 
    /**
     * The encoder used to actually encode character streams.
     */
    private XMLEncoder _encoder;
 
    /**
     * The state of this outputter.
     */
    private XMLEventListenerState _state;
 
    /**
     * Stack of open elements.
     *
     * <p>This field is initialized by the constructor. It can never be
     * <code>null</code>.
     */
    private String[] _elementStack;
 
    /**
     * The size of the element stack. The actual capacity is
     * {@link #_elementStack}<code>.length</code>.
     */
    private int _elementStackSize;
 
    /**
     * The current quotation mark.
     *
     * <p />The value of this field can be set using
     * {@link #setQuotationMark(char)} and can be retrieved
     * using {@link #getQuotationMark()}.
     */
    private char _quotationMark;
 
    /**
     * Flag that indicates if ampersands should be escaped.
     */
    private boolean _escapeAmpersands = true;
 
    /**
     * The line break that is currently in use. Should never become
     * <code>null</code>.
     */
    private LineBreak _lineBreak = LineBreak.NONE;
 
    /**
     * The line break as a char array. Should never become <code>null</code>,
     * but can be a zero-length array.
     */
    private char[] _lineBreakChars = _lineBreak._lineBreakChars;
 
    /**
     * The currently used indentation string. Can never become
     * <code>null</code>.
     */
    private String _indentation;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks all invariants. This check should be performed at the end of
     * every method that changes the internal state of this object.
     *
     * @throws Error
     *    if the state of this <code>XMLOutputter</code> is invalid.
     */
    private final void checkInvariants()
    throws Error {
 
       if (_lineBreak == null) {
          throw new Error("_lineBreak == null");
       } else if (_lineBreak == LineBreak.NONE && _indentation.length() > 0) {
          throw new Error("_lineBreak == LineBreak.NONE && _indentation = \"" + _indentation + "\".");
       } else if (_elementStack == null) {
          throw new Error("_elementStack (" + _elementStack + " == null");
       } else if (_elementStackSize < 0) {
          throw new Error("_elementStackSize (" + _elementStackSize + ") < 0");
       }
    }
 
    /**
     * Writes the indentation to the output stream.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void writeIndentation()
    throws IOException {
 
       // Write indentation only if there is any
       if (_indentation.length() > 0) {
          int count = _elementStackSize - 1;
          for (int i = 0; i < count; i++) {
             _out.write(_indentation);
          }
       }
    }
 
    /**
     * Returns the output stream this outputter uses.
     *
     * @return
     *    the output stream of this encoding, only <code>null</code> if and
     *    only if the state is {@link #UNINITIALIZED}.
     */
    public final Writer getWriter() {
       return _out;
    }
 
    /**
     * Returns the encoding of this outputter.
     *
     * @return
     *    the encoding used by this outputter, only <code>null</code> if and
     *    only if the state is {@link #UNINITIALIZED}.
     */
    public final String getEncoding() {
       if (_encoder == null) {
          return null;
       } else {
          return _encoder.getEncoding();
       }
    }
 
    /**
     * Resets this <code>XMLOutputter</code>. The <code>Writer</code> and the
     * encoding will be set to <code>null</code>, the element stack will be
     * cleared, the state will be set to {@link #UNINITIALIZED}, the line break
     * will be set to {@link LineBreak#NONE} and the indentation will be set to
     * {@link #DEFAULT_INDENTATION} (an empty string).
     */
    public void reset() {
       _out              = null;
       _encoder          = null;
       _elementStackSize = 0;
       _state            = XMLEventListenerStates.UNINITIALIZED;
       _lineBreak        = LineBreak.NONE;
       _lineBreakChars   = _lineBreak._lineBreakChars;
       _indentation      = DEFAULT_INDENTATION;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Resets this <code>XMLOutputter</code> and configures it for the
     * specified output stream. This sets the state to
     * {@link #BEFORE_XML_DECLARATION} and clears the stack of open elements.
     *
     * @param out
     *    the new output stream, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out == null</code>.
     */
    private final void reset(Writer out)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (out == null) {
          throw new IllegalArgumentException("out == null");
       } 
 
       // Reset the fields
       _out              = out;
       _state            = XMLEventListenerStates.BEFORE_XML_DECLARATION;
       _elementStackSize = 0;
       _lineBreak        = LineBreak.NONE;
       _lineBreakChars   = _lineBreak._lineBreakChars;
       _indentation      = DEFAULT_INDENTATION;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Resets this <code>XMLOutputter</code> and configures it for the
     * specified output stream and encoding. This resets the state to
     * {@link #BEFORE_XML_DECLARATION} and clears the stack of open elements.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @param encoding
     *    the encoding, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out == null || encoding == null</code>.
     *
     * @throws UnsupportedEncodingException
     *    if the specified encoding is not supported.
     */
    public final void reset(Writer out, String encoding)
    throws IllegalArgumentException,
           UnsupportedEncodingException {
 
       // Check arguments
       if (encoding == null) {
          throw new IllegalArgumentException("encoding == null");
       }
 
       reset(out);
 
       // Store the fields
       _encoder = XMLEncoder.getEncoder(encoding);
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Resets this <code>XMLOutputter</code> and configures it for the
     * specified output stream and encoder. This resets the state to
     * {@link #BEFORE_XML_DECLARATION} and clears the stack of open elements.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @param encoder
     *    the encoder, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out == null || encoder == null</code>.
     *
     * @throws UnsupportedEncodingException
     *    if the specified encoding is not supported.
     */
    public final void reset(Writer out, XMLEncoder encoder)
    throws IllegalArgumentException,
           UnsupportedEncodingException {
 
       // Check arguments
       if (encoder == null) {
          throw new IllegalArgumentException("encoder == null");
       }
 
       reset(out);
 
       // Store the fields
       _encoder = encoder;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Sets the state of this outputter. Normally, it is not necessary to call
     * this method.
     *
     * <p />Calling this method with {@link #UNINITIALIZED} as the state is
     * equivalent to calling {@link #reset()}.
     *
     * <p />Caution: This method can be used to let this class generate invalid
     * XML.
     *
     * @param newState
     *    the new state, not <code>null</code>.
     *
     * @param newElementStack
     *    the new element stack, if <code>newState == START_TAG_OPEN
     *    || newState == WITHIN_ELEMENT</code> then it should be
     *    non-<code>null</code> and containing no <code>null</code> elements,
     *    otherwise it must be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>newState == null
     *          || (newState == {@link #START_TAG_OPEN} &amp;&amp; newElementStack == null)
     *          || (newState == {@link #WITHIN_ELEMENT} &amp;&amp; newElementStack == null)
     *          || (newState != {@link #START_TAG_OPEN} &amp;&amp; newState != {@link #WITHIN_ELEMENT} &amp;&amp; newElementStack != null)
     *          || newElementStack[<i>n</i>] == null</code> (where <code>0 &lt;= <i>n</i> &lt; newElementStack.length</code>).
     *
     * @since xmlenc 0.22
     */
    public final void setState(XMLEventListenerState newState, String[] newElementStack)
    throws IllegalArgumentException {
 
       // Check arguments
       if (newState == null) {
          throw new IllegalArgumentException("newState == null");
       } else if (   newState == XMLEventListenerStates.START_TAG_OPEN
                  && newElementStack == null) {
          throw new IllegalArgumentException("newState == START_TAG_OPEN && newElementStack == null");
       } else if (   newState == XMLEventListenerStates.WITHIN_ELEMENT
                  && newElementStack == null) {
          throw new IllegalArgumentException("newState == WITHIN_ELEMENT && newElementStack == null");
       } else if (   newState != XMLEventListenerStates.START_TAG_OPEN
                  && newState != XMLEventListenerStates.WITHIN_ELEMENT
                  && newElementStack != null) {
          throw new IllegalArgumentException("newState != START_TAG_OPEN && newState != WITHIN_ELEMENT && newElementStack != null");
       }
 
       if (newElementStack != null) {
          for (int i = 0; i < newElementStack.length; i++) {
             if (newElementStack[i] == null) {
                throw new IllegalArgumentException("newElementStack[" + i + "] == null");
             }
          }
 
          if (newElementStack.length > _elementStack.length) {
             try {
                _elementStack = new String[newElementStack.length + 16];
             } catch (OutOfMemoryError error) {
                _elementStack = new String[newElementStack.length];
             }
          }
          System.arraycopy(newElementStack, 0, _elementStack, 0, newElementStack.length);
       }
 
       if (newState == XMLEventListenerStates.UNINITIALIZED) {
          reset();
       } else {
          _state            = newState;
          _elementStackSize = newElementStack == null
                            ? 0
                            : newElementStack.length;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Returns the current state of this outputter.
     *
     * @return
     *    the current state, cannot be <code>null</code>.
     */
    public final XMLEventListenerState getState() {
       return _state;
    }
 
    /**
     * Checks if escaping is currently enabled. If escaping is enabled, then
     * all ampersand characters (<code>'&amp;'</code>) are replaced by the
     * character entity reference <code>"&amp;amp;"</code>. This affects
     * PCDATA string printing ({@link #pcdata(String)} and
     * {@link #pcdata(char[],int,int)}) and attribute value printing
     * ({@link #attribute(String,String)}).
     *
     * @return
     *    <code>true</code> if escaping is enabled, <code>false</code>
     *    otherwise.
     */
    public final boolean isEscaping() {
       return _escapeAmpersands;
    }
 
    /**
     * Sets if ampersands should be escaped. This affects PCDATA string
     * printing ({@link #pcdata(String)} and
     * {@link #pcdata(char[],int,int)}) and attribute value printing
     * ({@link #attribute(String,String)}).
     *
     * <p />If ampersands are not escaped, then entity references can be
     * printed.
     *
     * @param escapeAmpersands
     *    <code>true</code> if ampersands should be escaped, <code>false</code>
     *    otherwise.
     *
     * @since xmlenc 0.24
     */
    public final void setEscaping(boolean escapeAmpersands) {
       _escapeAmpersands = escapeAmpersands;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Returns a copy of the element stack. The returned array will be a new
     * array. The size of the array will be equal to the element stack size
     * (see {@link #getElementStackSize()}.
     *
     * @return
     *    a newly constructed array that contains all the element types
     *    currently on the element stack, or <code>null</code> if there are no
     *    elements on the stack.
     *
     * @since xmlenc 0.22
     */
    public final String[] getElementStack() {
       if (_elementStackSize == 0) {
          return null;
       } else {
          String[] newStack = new String[_elementStackSize];
          System.arraycopy(_elementStack, 0, newStack, 0, _elementStackSize);
          return newStack;
       }
    }
 
    /**
     * Returns the current depth of open elements.
     *
     * @return
     *    the open element depth, always &gt;= 0.
     *
     * @since xmlenc 0.22
     */
    public final int getElementStackSize() {
       return _elementStackSize;
    }
 
    /**
     * Returns the current capacity for the stack of open elements.
     *
     * @return
     *    the open element stack capacity, always &gt;=
     *    {@link #getElementStackSize()}.
     *
     * @since xmlenc 0.28
     */
    public final int getElementStackCapacity() {
       return _elementStack.length;
    }
 
    /**
     * Sets the capacity for the stack of open elements. The new capacity must
     * at least allow the stack to contain the current open elements.
     *
     * @param newCapacity
     *    the new capacity, &gt;= {@link #getElementStackSize()}.
     *
     * @throws IllegalArgumentException
     *    if <code>newCapacity &lt; {@link #getElementStackSize()}</code>.
     *
     * @throws OutOfMemoryError
     *    if a new array cannot be allocated; this object will still be usable,
     *    but the capacity will remain unchanged.
     */
    public final void setElementStackCapacity(int newCapacity)
    throws IllegalArgumentException, OutOfMemoryError {
 
       // Check argument
       if (newCapacity < _elementStack.length) {
          throw new IllegalArgumentException("newCapacity < getElementStackSize()");
       }
 
       int currentCapacity = _elementStack.length;
 
       // Short-circuit if possible
       if (currentCapacity == newCapacity) {
          return;
       }
 
       String[] newStack = new String[newCapacity];
       System.arraycopy(_elementStack, 0, newStack, 0, _elementStackSize);
       _elementStack = newStack;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Sets the quotation mark character to use. This character is printed
     * before and after an attribute value. It can be either the single or the
     * double quote character.
     *
     * <p />The default quotation mark character is <code>'"'</code>.
     *
     * @param c
     *    the character to put around attribute values, either
     *    <code>'\''</code> or <code>'"'</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>c != '\'' &amp;&amp; c != '"'</code>.
     */
    public final void setQuotationMark(char c)
    throws IllegalArgumentException {
 
       // Accept apostrophe and quote
       if (c == '\'' || c == '"') {
          _quotationMark = c;
 
       // Deny any other character
       } else {
          throw new IllegalArgumentException("c != '\\'' && c != '\"'");
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Gets the quotation mark character. This character is used to mark the
     * start and end of an attribute value.
     *
     * <p />The default quotation mark character is <code>'"'</code>.
     *
     * @return
     *    the character to put around attribute values, either
     *    <code>'\''</code> or <code>'"'</code>.
     */
    public final char getQuotationMark() {
       return _quotationMark;
    }
 
    /**
     * Sets the type of line break to use. If the line break is set to
     * <code>LineBreak.NONE</code>, then the indentation is reset to an empty
     * string.
     *
     * @param lineBreak
     *    the line break to use; specifying <code>null</code> as the argument
     *    is equivalent to specifying {@link LineBreak#NONE}.
     */
    public final void setLineBreak(LineBreak lineBreak) {
 
       // Copy to the field, but convert null to LineBreak.NONE
       _lineBreak = lineBreak != null
                  ? lineBreak
                  : LineBreak.NONE;
 
       // Get the corresponding characters in a separate field
       _lineBreakChars = _lineBreak._lineBreakChars;
 
       // If there is no line break, there shall be no indentation
       if (_lineBreak == LineBreak.NONE) {
          _indentation = "";
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Returns the currently used line break.
     *
     * @return
     *    the currently used line break, never <code>null</code>.
     */
    public final LineBreak getLineBreak() {
       return _lineBreak;
    }
   
    /**
     * Sets the string to be used for indentation. A line break must be set
     * prior to calling this method.
     *
     * <p>Only space and tab characters are allowed for the indentation. 
     *
     * @param indentation
     *    the character string used for indentation, or <code>null</code> if
     *    {@link #DEFAULT_INDENTATION the default indentation} should be used.
     *
     * @throws IllegalStateException
     *    if <code>{@link #getLineBreak()} == LineBreak.NONE</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>indentation<code> contains characters that are neither a
     *    space nor a tab.
     */
    public final void setIndentation(String indentation)
    throws IllegalStateException {
 
       // Preparation: Convert null to the default indentation
       indentation = indentation != null
                   ? indentation
                   : DEFAULT_INDENTATION;
 
       // Check preconditions
       if (_lineBreak == LineBreak.NONE) {
          throw new IllegalStateException("getLineBreak() == LineBreak.NONE");
       }
       int length = indentation.length();
       for (int i = 0; i < length; i++) {
          char ch = indentation.charAt(i);
          if (ch != ' ' && ch != '\t') {
             String message = "indentation.charAt("
                            + i
                            + ") = "
                            + (int) ch
                            + ", while only space and tab are allowed.";
             throw new IllegalArgumentException(message);
          }
       }
 
       // Update the internal field
       _indentation = indentation;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Returns the string currently used for indentation.
     *
     * @return
     *    the character string used for indentation, never <code>null</code>.
     */
    public final String getIndentation() {
       return _indentation;
    }
 
    /**
     * Closes an open start tag.
     *
     * <p>TODO: Document why this is in a separate method.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private void closeStartTag()
    throws IOException {
       _out.write('>');
    }
 
    /**
     * Writes the XML declaration. This method always prints the name of the
     * encoding. The case of the encoding is as it was specified during
     * initialization (or re-initialization).
     *
     * <p />If the encoding is set to <code>"ISO-8859-1"</code>, then this
     * method will produce the following output:
     *
     * <blockquote><code>&lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;</code></blockquote>
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION}</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void declaration()
    throws IllegalStateException,
           IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write the output
       _encoder.declaration(_out);
 
       // Change the state
       _state = XMLEventListenerStates.BEFORE_DTD_DECLARATION;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes a document type declaration.
     *
     * <p />An external subset can be specified using either a
     * <em>system identifier</em> (alone), or using both a
     * <em>public identifier</em> and a <em>system identifier</em>. It can
     * never be specified using a <em>public identifier</em> alone.
     *
     * <p />For example, for XHTML 1.0 the public identifier is:
     *
     * <blockquote><code>-//W3C//DTD XHTML 1.0 Transitional//EN</code></blockquote>
     *
     * <p />while the system identifier is:
     *
     * <blockquote><code>http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd</code></blockquote>
     *
     * <p />The output is typically similar to this:
     *
     * <blockquote><code>&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;</code></blockquote>
     *
     * or alternatively, if only the <em>system identifier</em> is specified:
     *
     * <blockquote><code>&lt;!DOCTYPE html SYSTEM "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;</code></blockquote>
     *
     * @param name
     *    the name of the document type, not <code>null</code>.
     *
     * @param publicID
     *    the public identifier, can be <code>null</code>, but if not, then it
     *    must match the
     *    <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-PubidLiteral"><em>PubidLiteral</em> production</a>
     *    in the XML 1.0 Specification, when quoted.
     *
     * @param systemID
     *    the system identifier, can be <code>null</code>, but if not, then
     *    it must match the
     *    <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-SystemLiteral"><em>SystemLiteral</em> production</a>
     *    in the XML 1.0 Specification, when quoted.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null ||
     *          (publicID != null &amp;&amp; systemID == null)</code>.
     *
     * @throws InvalidXMLException
     *    if the specified name does not match the
     *    <a href="http://www.w3.org/TR/REC-xml#NT-Name"><em>Name</em> production</a>
     *    (see {@link XMLChecker#checkName(String)}).
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void dtd(String name, String publicID, String systemID)
    throws IllegalStateException,
           IllegalArgumentException,
           InvalidXMLException,
           IOException {
 
       // TODO: Respect _quotationMark
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       // Check arguments
       if (name == null) {
          throw new IllegalArgumentException("name == null");
       } else if (publicID != null && systemID == null) {
          throw new IllegalArgumentException("Found public identifier, but no system identifier.");
       }
 
       // Check productions
       XMLChecker.checkName(name);
       if (publicID != null) {
          XMLChecker.checkPubidLiteral("\"" + publicID + "\"");
       }
       if (systemID != null) {
          XMLChecker.checkSystemLiteral("\"" + systemID + "\"");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write a line break, if necessary
       if (oldState == XMLEventListenerStates.BEFORE_DTD_DECLARATION) {
          _out.write(_lineBreakChars);
       }
 
       // Write the DTD reference
       _out.write("<!DOCTYPE ");
       _out.write(name);
       if (publicID != null) {
          _out.write(" PUBLIC \"");
          _out.write(publicID);
          _out.write('"');
          _out.write(' ');
          _out.write('"');
          _out.write(systemID);
          _out.write('"');
       } else if (systemID != null) {
          _out.write(" SYSTEM \"");
          _out.write(systemID);
          _out.write('"');
       }
       closeStartTag();
 
       // Change the state
       _state = XMLEventListenerStates.BEFORE_ROOT_ELEMENT;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes an element start tag. The element type name will be stored in the
     * internal element stack. If necessary, the capacity of this stack will be
     * extended.
     *
     * @param type
     *    the type of the tag to start, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void startTag(String type)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION &&
           _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION &&
           _state != XMLEventListenerStates.BEFORE_ROOT_ELEMENT    &&
           _state != XMLEventListenerStates.START_TAG_OPEN         &&
           _state != XMLEventListenerStates.WITHIN_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (type == null) {
          throw new IllegalArgumentException("type == null");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Increase the stack size if necessary
       if (_elementStackSize == _elementStack.length) {
          String[] newStack;
          try {
             newStack = new String[(_elementStackSize + 1) * 2];
          } catch (OutOfMemoryError error) {
             newStack = new String[_elementStackSize + 1];
          }
          System.arraycopy(_elementStack, 0, newStack, 0, _elementStackSize);
          _elementStack = newStack;
       }
 
       // Store the element type name on the stack
       _elementStack[_elementStackSize] = type;
       _elementStackSize++;
 
       // Close start tag if necessary
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _out.write('>');
       }
 
       // Write line break and indentation, except if this is first output
       if (oldState != XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          _out.write(_lineBreakChars);
          writeIndentation();
       }
 
       _out.write('<');
 
       // Escape the element name, if necessary
       _out.write(type);
 
       // Change the state
       _state = XMLEventListenerStates.START_TAG_OPEN;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Adds an attribute to the current element, with a <code>String</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || value == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, String value)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (name == null || value == null) {
          if (name == null && value == null) {
             throw new IllegalArgumentException("name == null && value == null");
          } else if (name == null) {
             throw new IllegalArgumentException("name == null");
          } else {
             throw new IllegalArgumentException("value == null");
          }
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       _encoder.attribute(_out, name, value, _quotationMark, _escapeAmpersands);
 
       // Reset the state
       _state = XMLEventListenerStates.START_TAG_OPEN;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Adds an attribute to the current element, with a <code>boolean</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, boolean value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, value ? "true" : "false");
    }
 
    /**
     * Adds an attribute to the current element, with a <code>byte</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, byte value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Byte.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with a <code>short</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, short value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Short.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with an <code>int</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, int value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Integer.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with a <code>long</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, long value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Long.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with a <code>float</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, float value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Float.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with a <code>double</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param value
     *    the value of the attribute.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, double value)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Double.toString(value));
    }
 
    /**
     * Adds an attribute to the current element, with a <code>char</code>
     * value. There must currently be an open element.
     *
     * <p />The attribute value is surrounded by the quotation mark character 
     * (see {@link #getQuotationMark()}).
     *
     * @param name
     *    the name of the attribute, not <code>null</code>.
     *
     * @param c
     *    the value of the attribute, a single character.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void attribute(String name, char c)
    throws IllegalStateException, IllegalArgumentException, IOException {
       attribute(name, Character.toString(c));
    }
 
    /**
     * Writes the end tag for the element on top of the stack.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void endTag()
    throws IllegalStateException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.START_TAG_OPEN) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       String type = _elementStack[_elementStackSize-1];
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _out.write('/');
          _out.write('>');
       } else {
          _out.write(_lineBreakChars);
          writeIndentation();
 
          _out.write('<');
          _out.write('/');
          _out.write(type);
          closeStartTag();
       }
 
       _elementStackSize--;
 
       // Change the state
       if (_elementStackSize == 0) {
          _state = XMLEventListenerStates.AFTER_ROOT_ELEMENT;
       } else {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes end tags for elements on the stack until (and including) an
     * element that matches the specified name.
     *
     * @param type
     *    the type of the tag to end, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>
     *
     * @throws IllegalArgumentException
     *    if <code>type == null</code>.
     *
     * @throws NoSuchElementException
     *    if an element of the specified type could not be found on the stack.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     *
     * @since xmlenc 0.53
     */
    public final void endTag(String type)
    throws IllegalStateException,
           NoSuchElementException,
           IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.START_TAG_OPEN) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
      String typeFound;
       do {
         typeFound = _elementStack[_elementStackSize-1];
 
          // Write output
          if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
             _out.write('/');
             closeStartTag();
          } else {
             _out.write(_lineBreakChars);
             writeIndentation();
 
             _out.write('<');
             _out.write('/');
             _out.write(typeFound);
             closeStartTag();
          }
 
          _elementStackSize--;
       } while (! type.equals(typeFound) && _elementStackSize > 0);
 
       // Make sure the element was indeed found
       if (! type.equals(typeFound)) {
          throw new NoSuchElementException("No element of type \"" + type + "\" was found on the stack of open elements.");
       }
 
       // Change the state
       if (_elementStackSize == 0) {
          _state = XMLEventListenerStates.AFTER_ROOT_ELEMENT;
       } else {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes an empty tag. This is equivalent to calling:
     *
     * <blockquote><code>startTag(type);
     * <br/>endTag();</code></blockquote>
     *
     * @param type
     *    the type of the tag to start, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     *
     * @since xmlenc 0.53
     */
    public final void emptyTag(String type)
    throws IllegalStateException, IllegalArgumentException, IOException {
       startTag(type);
       endTag();
    }
 
 
    /**
     * Writes the specified <code>String</code> as PCDATA.
     *
     * @param text
     *    the PCDATA text to be written, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     *
     * @throws InvalidXMLException
     *    if the specified text contains an invalid character.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void pcdata(String text)
    throws IllegalStateException, IllegalArgumentException, InvalidXMLException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (text == null) {
          throw new IllegalArgumentException("text == null");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
          _out.write(_lineBreakChars);
       }
       _encoder.text(_out, text, _escapeAmpersands);
 
       // Change the state
       _state = XMLEventListenerStates.WITHIN_ELEMENT;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes the specified character array as PCDATA.
     *
     * @param ch
     *    the character array containing the text to be written, not
     *    <code>null</code>.
     *
     * @param start
     *    the start index in the array, must be &gt;= 0 and it must be &lt;
     *    <code>ch.length</code>.
     *
     * @param length
     *    the number of characters to read from the array, must be &gt; 0.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>
     *
     * @throws IllegalArgumentException
     *    if <code>ch     ==    null
     *          || start  &lt;  0
     *          || start  &gt;= ch.length
     *          || length &lt;  0</code>.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>start + length &gt; ch.length</code>.
     *
     * @throws InvalidXMLException
     *    if the specified text contains an invalid character.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void pcdata(char[] ch, int start, int length)
    throws IllegalStateException, IllegalArgumentException, IndexOutOfBoundsException, InvalidXMLException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (ch == null) {
          throw new IllegalArgumentException("ch == null");
       } else if (start < 0) {
          throw new IllegalArgumentException("start (" + start + ") < 0");
       } else if (start >= ch.length) {
          throw new IllegalArgumentException("start (" + start + ") >= ch.length (" + ch.length + ')');
       } else if (length < 0) {
          throw new IllegalArgumentException("length < 0");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
       }
       _encoder.text(_out, ch, start, length, _escapeAmpersands);
 
       // Change the state
       _state = XMLEventListenerStates.WITHIN_ELEMENT;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes the specified ignorable whitespace. Ignorable whitespace may be
     * written anywhere in XML output stream, except above the XML declaration.
     *
     * <p />If the state equals {@link #BEFORE_XML_DECLARATION}, then it will be set to
     * {@link #BEFORE_DTD_DECLARATION}, otherwise if the state is
     * {@link #START_TAG_OPEN} then it will be set to {@link #WITHIN_ELEMENT},
     * otherwise the state will not be changed.
     *
     * @param whitespace
     *    the ignorable whitespace to be written, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>whitespace == null</code>.
     *
     * @throws InvalidXMLException
     *    if the specified character string contains a character that is
     *    invalid as whitespace.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void whitespace(String whitespace)
    throws IllegalStateException, IllegalArgumentException, InvalidXMLException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION &&
           _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION &&
           _state != XMLEventListenerStates.BEFORE_ROOT_ELEMENT &&
           _state != XMLEventListenerStates.START_TAG_OPEN &&
           _state != XMLEventListenerStates.WITHIN_ELEMENT &&
           _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (whitespace == null) {
          throw new IllegalArgumentException("whitespace == null");
       }
 
       XMLEventListenerState oldState = _state;
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
       }
 
       // Do the actual output
       _encoder.whitespace(_out, whitespace);
 
       // Change state
       if (oldState == XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          _state = XMLEventListenerStates.BEFORE_DTD_DECLARATION;
       } else if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       } else {
          _state = oldState;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes text from the specified character array as ignorable whitespace.
     * Ignorable whitespace may be written anywhere in XML output stream,
     * except above the XML declaration.
     *
     * <p />This method does not check if the string actually contains
     * whitespace.
     *
     * <p />If the state equals {@link #BEFORE_XML_DECLARATION}, then it will be set to
     * {@link #BEFORE_DTD_DECLARATION}, otherwise if the state is
     * {@link #START_TAG_OPEN} then it will be set to {@link #WITHIN_ELEMENT},
     * otherwise the state will not be changed.
     *
     * @param ch
     *    the character array containing the text to be written, not
     *    <code>null</code>.
     *
     * @param start
     *    the start index in the array, must be &gt;= 0 and it must be &lt;
     *    <code>ch.length</code>.
     *
     * @param length
     *    the number of characters to read from the array, must be &gt; 0.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>ch     ==    null
     *          || start  &lt;  0
     *          || start  &gt;= ch.length
     *          || length &lt;  0</code>.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>start + length &gt; ch.length</code>.
     *
     * @throws InvalidXMLException
     *    if the specified character string contains a character that is
     *    invalid as whitespace.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void whitespace(char[] ch, int start, int length)
    throws IllegalStateException, IllegalArgumentException, IndexOutOfBoundsException, InvalidXMLException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_ROOT_ELEMENT
        && _state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (ch == null) {
          throw new IllegalArgumentException("ch == null");
       } else if (start < 0) {
          throw new IllegalArgumentException("start (" + start + ") < 0");
       } else if (start >= ch.length) {
          throw new IllegalArgumentException("start (" + start + ") >= ch.length (" + ch.length + ')');
       } else if (length < 0) {
          throw new IllegalArgumentException("length < 0");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
       }
 
       // Do the actual output
       _encoder.whitespace(_out, ch, start, length);
 
       // Change state
       if (oldState == XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          _state = XMLEventListenerStates.BEFORE_DTD_DECLARATION;
       } else if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       } else {
          _state = oldState;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes the specified comment. The comment should not contain the string
     * <code>"--"</code>.
     *
     * <p />If the state equals {@link #BEFORE_XML_DECLARATION}, then it will be set to
     * {@link #BEFORE_DTD_DECLARATION}, otherwise if the state is
     * {@link #START_TAG_OPEN} then it will be set to {@link #WITHIN_ELEMENT},
     * otherwise the state will not be changed.
     *
     * @param text
     *    the text for the comment be written, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     *
     * @throws InvalidXMLException
     *    if the specified text contains an invalid character.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void comment(String text)
    throws IllegalStateException, IllegalArgumentException, InvalidXMLException, IOException {
 
       // Check arguments
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_ROOT_ELEMENT
        && _state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (text == null) {
          throw new IllegalArgumentException("text == null");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Write output
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _out.write('>');
          _out.write('<');
          _out.write('!');
          _out.write('-');
          _out.write('-');
       } else {
          _out.write('<');
          _out.write('!');
          _out.write('-');
          _out.write('-');
       }
       _encoder.text(_out, text, _escapeAmpersands);
       _out.write('-');
       _out.write('-');
       _out.write('>');
 
       _out.write(_lineBreakChars);
 
       // Change state
       if (oldState == XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          _state = XMLEventListenerStates.BEFORE_DTD_DECLARATION;
       } else if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       } else {
          _state = oldState;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes a processing instruction. A target and an optional instruction
     * should be specified.
     *
     * <p />A processing instruction can appear above and below the root
     * element, and between elements. It cannot appear inside an element start
     * or end tag, nor inside a comment. Processing instructions cannot be
     * nested.
     *
     * <p />If the state equals {@link #BEFORE_XML_DECLARATION}, then it will be set to
     * {@link #BEFORE_DTD_DECLARATION}, otherwise the state will not be
     * changed.
     *
     * @param target
     *    an identification of the application at which the instruction is
     *    targeted, not <code>null</code>.
     *
     * @param instruction
     *    the instruction, can be <code>null</code>, which is equivalent to an
     *    empty string.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #BEFORE_XML_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_DTD_DECLARATION} &amp;&amp;
     *             getState() != {@link #BEFORE_ROOT_ELEMENT} &amp;&amp;
     *             getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void pi(String target, String instruction)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.BEFORE_XML_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_DTD_DECLARATION
        && _state != XMLEventListenerStates.BEFORE_ROOT_ELEMENT
        && _state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (target == null) {
          throw new IllegalArgumentException("target == null");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Complete the start tag if necessary
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
       }
 
       // Write the Processing Instruction
       _out.write('<');
       _out.write('?');
       _out.write(target);
       if (instruction != null) {
          _out.write(' ');
          _out.write(instruction);
       }
       _out.write('?');
       _out.write('>');
 
       // Change the state
       if (oldState == XMLEventListenerStates.BEFORE_XML_DECLARATION) {
          _state = XMLEventListenerStates.BEFORE_DTD_DECLARATION;
       } else if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          _state = XMLEventListenerStates.WITHIN_ELEMENT;
       } else {
          _state = oldState;
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Writes a CDATA section.
     *
     * <p />A CDATA section can contain any string, except
     * <code>"]]&gt;"</code>. This will, however, not be checked by this
     * method.
     *
     * <p />Left angle brackets and ampersands will be output in their literal
     * form; they need not (and cannot) be escaped using
     * <code>"&amp;lt;"</code> and <code>"&amp;amp;"</code>.
     *
     * <p />If the specified string is empty (i.e.
     * <code>"".equals(text)</code>, then nothing will be output.
     *
     * <p />If the specified string contains characters that cannot be printed
     * in this encoding, then the result is undefined.
     *
     * @param text
     *    the contents of the CDATA section, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT}</code>
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void cdata(String text)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
 
       // Check arguments
       } else if (text == null) {
          throw new IllegalArgumentException("text == null");
       }
 
       // Temporarily set the state to ERROR_STATE. Unless an exception is
       // thrown in the write methods, it will be reset to a valid state.
       XMLEventListenerState oldState = _state;
       _state = XMLEventListenerStates.ERROR_STATE;
 
       // Complete the start tag if necessary
       if (oldState == XMLEventListenerStates.START_TAG_OPEN) {
          closeStartTag();
       }
 
       _out.write("<![CDATA[");
       _out.write(text);
       _out.write(']');
       _out.write(']');
       _out.write('>');
 
       // Change state
       _state = XMLEventListenerStates.WITHIN_ELEMENT;
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Closes all open elements. After calling this method, only the
     * {@link #whitespace(String)} method can be called.
     *
     * <p />If you would like to flush the output stream as well, call
     * {@link #endDocument()} instead.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void close()
    throws IllegalStateException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN
        && _state != XMLEventListenerStates.WITHIN_ELEMENT
        && _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       while (_elementStackSize > 0) {
          endTag();
       }
 
       // State has changed, check
       checkInvariants();
    }
 
    /**
     * Finishes the XML output. All open elements will be closed and the
     * underlying output stream will be flushed using
     * {@link #getWriter()}.{@link java.io.Writer#flush() flush()}.
     *
     * <p />After calling this method, the state is changed to
     * {@link #DOCUMENT_ENDED}, so that no more output can be
     * written until this outputter is reset.
     *
     * @throws IllegalStateException
     *    if <code>getState() != {@link #START_TAG_OPEN} &amp;&amp;
     *             getState() != {@link #WITHIN_ELEMENT} &amp;&amp;
     *             getState() != {@link #AFTER_ROOT_ELEMENT}</code>.
     *
     * @throws IOException
     *    if an I/O error occurs; this will set the state to
     *    {@link #ERROR_STATE}.
     */
    public final void endDocument()
    throws IllegalStateException, IOException {
 
       // Check state
       if (_state != XMLEventListenerStates.START_TAG_OPEN &&
           _state != XMLEventListenerStates.WITHIN_ELEMENT &&
           _state != XMLEventListenerStates.AFTER_ROOT_ELEMENT) {
          throw new IllegalStateException("getState() == " + _state);
       }
 
       // Close all open elements
       close();
 
       // Flush the output stream
       _out.flush();
 
       // Finally change the state
       _state = XMLEventListenerStates.DOCUMENT_ENDED;
 
       // State has changed, check
       checkInvariants();
    }
 }
