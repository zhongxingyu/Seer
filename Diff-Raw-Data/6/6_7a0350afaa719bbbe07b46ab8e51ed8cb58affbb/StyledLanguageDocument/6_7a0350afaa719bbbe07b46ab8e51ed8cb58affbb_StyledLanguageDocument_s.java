 package de.unisiegen.tpml.graphics;
 
 import java.awt.Color;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.languages.AbstractLanguageScanner;
 import de.unisiegen.tpml.core.languages.Language;
 import de.unisiegen.tpml.core.languages.LanguageParser;
 import de.unisiegen.tpml.core.languages.LanguageParserException;
 import de.unisiegen.tpml.core.languages.LanguageScanner;
 import de.unisiegen.tpml.core.languages.LanguageScannerException;
 import de.unisiegen.tpml.core.languages.LanguageSymbol;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStyle;
 import de.unisiegen.tpml.core.util.beans.Bean;
 
 /**
  * An implementation of the {@link javax.swing.text.StyledDocument} interface to enable syntax
  * highlighting using the lexer of the current {@link de.unisiegen.tpml.core.languages.Language}.
  *
  * @author Benedikt Meurer
  * @version $Id$
  *
  * @see javax.swing.text.DefaultStyledDocument
  */
 public class StyledLanguageDocument extends DefaultStyledDocument implements Bean {
 	//
 	// Constants
 	//
 
   /**
    * Empty array of language exceptions.
    */
   private static final LanguageScannerException[] EMPTY_ARRAY = new LanguageScannerException[0];
 
   /**
    * The {@link Logger} for this class.
    * 
    * @see Logger
    */
   private static final Logger logger = Logger.getLogger(StyledLanguageDocument.class);
   
 	/**
 	 * The unique serialization identifier of this class. 
 	 */
 	private static final long serialVersionUID = 5866657214159718809L;
 	
 	
 	
 	//
 	// Attributes
 	//
 
   /**
    * If any <code>PropertyChangeListeners</code> have been registered,
    * the <code>changeSupport</code> field describes them.
    *
    * @serial
    * @see #addPropertyChangeListener(PropertyChangeListener)
    * @see #addPropertyChangeListener(String, PropertyChangeListener)
    * @see #removePropertyChangeListener(PropertyChangeListener)
    * @see #removePropertyChangeListener(String, PropertyChangeListener)
    * @see #firePropertyChange(String, boolean, boolean)
    * @see #firePropertyChange(String, int, int)
    * @see #firePropertyChange(String, Object, Object)
    */
   protected PropertyChangeSupport changeSupport;
   
   /**
 	 * The {@link Language} for which this document was allocated.
 	 */
 	private Language language;
 	
 	/**
 	 * The current exceptions from the {@link #language}s scanner.
 	 * 
 	 * @see #getExceptions()
 	 * @see #getExceptions(int)
 	 */
   private LanguageScannerException exceptions[];
   
   /**
    * The attributes default style.
    */
   private SimpleAttributeSet normalSet = new SimpleAttributeSet();
   
   /**
    * The attributes for the various {@link PrettyStyle}s.
    */
   private HashMap<PrettyStyle, SimpleAttributeSet> attributes = new HashMap<PrettyStyle, SimpleAttributeSet>();
 	
 	
 	
 	//
 	// Constructor
 	//
 	
 	/**
 	 * Allocates a new <code>StyledLanguageDocument</code> for the given <code>language</code>, where
 	 * the <code>language</code> is used to determine the scanner (aka lexer) for the documents content
 	 * and thereby dictates the syntax highlighting.
 	 * 
 	 * @param language the {@link Language} for which to allocate a document.
 	 * 
 	 * @throws NullPointerException if the <code>language</code> is <code>null</code>.
 	 */
 	public StyledLanguageDocument(Language language) {
 		if (language == null) {
 			throw new NullPointerException("language is null");
 		}
 		this.language = language;
 	  
     // setup the normal attribute set
     StyleConstants.setForeground(this.normalSet, Color.BLACK);
     StyleConstants.setBold(this.normalSet, false);
 
     // setup the comment set
     SimpleAttributeSet commentSet = new SimpleAttributeSet();
     StyleConstants.setForeground(commentSet, new Color(0.0f, 0.6f, 0.0f));
     StyleConstants.setItalic(commentSet, true);
     this.attributes.put(PrettyStyle.COMMENT, commentSet);
 
     // setup the constant set
     SimpleAttributeSet constantSet = new SimpleAttributeSet();
     StyleConstants.setForeground(constantSet, new Color(0.0f, 0.0f, 0.6f));
     StyleConstants.setBold(constantSet, true);
     this.attributes.put(PrettyStyle.CONSTANT, constantSet);
 
     // setup the keyword set
     SimpleAttributeSet keywordSet = new SimpleAttributeSet();
     StyleConstants.setForeground(keywordSet, new Color(0.6f, 0.0f, 0.0f));
     StyleConstants.setBold(keywordSet, true);
     this.attributes.put(PrettyStyle.KEYWORD, keywordSet);
     
     // setup the type set
     SimpleAttributeSet typeSet = new SimpleAttributeSet();
     StyleConstants.setForeground(typeSet, new Color(0.0f, 0.6f, 0.0f));
     StyleConstants.setBold(typeSet, true);
     this.attributes.put(PrettyStyle.TYPE, typeSet);
   }
 
   
 
   //
   // Accessors
   //
   
   /**
    * Returns the current {@link LanguageScannerException}s that were detected while trying to interpret
    * the token stream.
    * 
    * @return the exceptions.
    * 
    * @see #getExceptions(int)
    */
   public LanguageScannerException[] getExceptions() {
     return (this.exceptions != null) ? this.exceptions : EMPTY_ARRAY;
   }
   
   /**
    * Returns the exception at the given <code>index</code>.
    * 
    * @param index the index of the exception to return.
    * 
    * @return the exception at the <code>index</code>.
    * 
    * @throws ArrayIndexOutOfBoundsException if the <code>index</code> is out of bounds.
    * 
    * @see #getExceptions()
    */
   public LanguageScannerException getExceptions(int index) {
     return getExceptions()[index];
   }
   
   
   
   //
   // Primitives
   //
   
   /**
    * Returns the {@link Expression} for the program text within
    * this document. Throws an exception if a parsing error occurred.
    *  
    * @return the {@link Expression} for the program text.
    * 
    * @throws Exception  
    */
   public Expression getExpression() throws Exception {
     return this.language.newParser(new StringReader(getText(0, getLength()))).parse();
   }
   
   
   
   //
   // Change handling
   //
   
   /**
    * {@inheritDoc}
    *
    * @see javax.swing.text.AbstractDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
    */
   public void insertString(int offset, String str, AttributeSet set) throws BadLocationException {
     super.insertString(offset, str, set);
     processChanged();
   }
   
   /**
    * {@inheritDoc}
    *
    * @see javax.swing.text.Document#remove(int, int)
    */
   public void remove(int offset, int length) throws BadLocationException {
     super.remove(offset, length);
     processChanged();
   }
 
   /**
    * Processes the document content after a change.
    * 
    * @throws BadLocationException if the processing failed.
    */
   public void processChanged() throws BadLocationException {
     // reset the character attributes
     setCharacterAttributes(0, getLength(), this.normalSet, true);
     
     // allocate a list to collect the exceptions
     LanguageScannerException[] exceptions = null;
 
     try {
       // start with first character
       int offset = 0;
       
       // determine the document content
       String content = getText(offset, getLength());
       
       // allocate the scanner (initially)
       final LanguageScanner scanner = this.language.newScanner(new StringReader(content));
       
       // collect the tokens returned by the scanner
       final LinkedList<LanguageSymbol> symbols = new LinkedList<LanguageSymbol>();
       
       // determine the tokens for the content
       for (;;) {
         try {
           // read the next token from the scanner
           LanguageSymbol symbol = scanner.nextSymbol();
           if (symbol == null)
             break;
           
           // add the token to our list
           symbols.add(symbol);
           
           // check if we have an attribute set for the token
           SimpleAttributeSet set = this.attributes.get(scanner.getStyleBySymbol(symbol));
           if (set == null)
             set = this.normalSet;
           
           // apply the character attribute set
           setCharacterAttributes(offset + symbol.getLeft(), symbol.getRight() - symbol.getLeft(), set, true);
         }
         catch (LanguageScannerException e) {
           // setup the error attribute set
           SimpleAttributeSet errorSet = new SimpleAttributeSet();
           StyleConstants.setForeground(errorSet, Color.RED);
           StyleConstants.setUnderline(errorSet, true);
           errorSet.addAttribute("exception", e);
           
           // apply the error character attribute set to indicate the syntax error
           setCharacterAttributes(offset + e.getLeft(), e.getRight() - e.getLeft(), errorSet, false);
           
           // adjust the offset to point after the error
           offset += e.getRight();
           
           // skip the problematic characters
           content = content.substring(e.getRight());
           
           // restart the scanner after the error
           scanner.restart(new StringReader(content));
           
           // add the exception to our list
           if (exceptions == null) {
             exceptions = new LanguageScannerException[] { e };
           }
           else {
             LanguageScannerException[] newExceptions = new LanguageScannerException[exceptions.length + 1];
             System.arraycopy(exceptions, 0, newExceptions, 0, exceptions.length);
             newExceptions[exceptions.length] = e;
             exceptions = newExceptions;
           }
         }
       }
       
       // check if the scanner is happy
       if (exceptions == null) {
 				// allocate a parser based on a scanner that operates on the previously collected
       	// tokens from the scanner step above...
 				LanguageParser parser = this.language.newParser(new AbstractLanguageScanner() {
 					public void restart(Reader reader) { throw new UnsupportedOperationException();	}
 					public LanguageSymbol nextSymbol() throws IOException, LanguageScannerException {	return (!symbols.isEmpty()) ? symbols.poll() : null; }
					public PrettyStyle getStyleBySymbolId(int id) { return ((AbstractLanguageScanner)scanner).getStyleBySymbolId(id); }
 				});
 				
 				// ...and try to parse the token stream
 				try {
 					parser.parse();
 				}
 				catch (LanguageParserException e) {
 					// setup the error attribute set
 					SimpleAttributeSet errorSet = new SimpleAttributeSet();
 					StyleConstants.setForeground(errorSet, Color.RED);
 					StyleConstants.setUnderline(errorSet, true);
 					errorSet.addAttribute("exception", e);
 
 					// check if this is unexpected end of file
 					if (e.getLeft() < 0 && e.getRight() < 0) {
 						setCharacterAttributes(getLength(), getLength(), errorSet, false);
 					}
 					else {
 						// apply the error character attribute set to indicate the syntax error
 						setCharacterAttributes(e.getLeft(), e.getRight(), errorSet, false);
 					}
 					
 					// add the exception to our list
 					exceptions = new LanguageScannerException[] { e };
 				}
       }
     }
     catch (Exception e) {
     	logger.warn("Failed to process changes in the styled language document", e);
     }
     
     // update the exceptions property if necessary
     if (this.exceptions != exceptions) {
       LanguageScannerException[] oldExceptions = this.exceptions;
       this.exceptions = exceptions;
       firePropertyChange("exceptions", oldExceptions, this.exceptions);
     }
   }
 
 
 
   //
   // Listener registration
   //
   
   /**
    * Adds a {@link PropertyChangeListener} to the listener list. The listener
    * is registered for all bound properties of the derived class.
    * 
    * If <code>listener</code> is <code>null</code>, no exception is thrown
    * and no action is performed.
    * 
    * @param listener the {@link PropertyChangeListener} to be added.
    * 
    * @see #getPropertyChangeListeners()
    * @see #removePropertyChangeListener(PropertyChangeListener)
    */
   public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
     if (listener == null) {
       return;
     }
     if (this.changeSupport == null) {
       this.changeSupport = new PropertyChangeSupport(this);
     }
     this.changeSupport.addPropertyChangeListener(listener);
   }
   
   /**
    * Removes a {@link PropertyChangeListener} from the listener list. This method
    * should be used to remove PropertyChangeListeners that were registered for all
    * bound properties of this class.
    * 
    * If <code>listener</code> is <code>null</code>, no exception is thrown and
    * no action is performed.
    * 
    * @param listener the {@link PropertyChangeListener} to be removed.
    * 
    * @see #addPropertyChangeListener(PropertyChangeListener)
    * @see #getPropertyChangeListeners()
    */
   public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
     if (listener == null || this.changeSupport == null) {
       return;
     }
     this.changeSupport.removePropertyChangeListener(listener);
   }
   
   /**
    * Returns an array of all the property change listeners registered
    * on this object.
    * 
    * @return all of this object's {@link PropertyChangeListener}s or an
    *         empty array if no property change listeners are currently
    *         registered.
    *
    * @see #addPropertyChangeListener(PropertyChangeListener)
    * @see #removePropertyChangeListener(PropertyChangeListener)
    */
   public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
     if (this.changeSupport == null) {
       return new PropertyChangeListener[0];
     }
     return this.changeSupport.getPropertyChangeListeners();
   }
   
   /**
    * Adds a {@link PropertyChangeListener} to the listener list for a specific
    * property. The specified property may be user-defined, or one of the properties
    * provided by the object.
    * 
    * If <code>listener</code> is <code>null</code>, no exception is thrown and
    * no action is performed.
    * 
    * @param propertyName one of the property names of the object.
    * @param listener the {@link PropertyChangeListener} to be added.
    * 
    * @see #removePropertyChangeListener(String, PropertyChangeListener)
    * @see #getPropertyChangeListeners(String)
    */
   public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
     if (listener == null) {
       return;
     }
     if (this.changeSupport == null) {
       this.changeSupport = new PropertyChangeSupport(this);
     }
     this.changeSupport.addPropertyChangeListener(propertyName, listener);
   }
   
   /**
    * Removes a {@link PropertyChangeListener} from the listener list for a specific
    * property. This method should be used to remove {@link PropertyChangeListener}s
    * that were registered for a specific bound property.
    *
    * If <code>listener</code> is <code>null</code>, no exception is thrown and no
    * action is performed.
    * 
    * @param propertyName a valid property name.
    * @param listener the {@link PropertyChangeListener} to be removed.
    * 
    * @see #addPropertyChangeListener(String, PropertyChangeListener)
    * @see #getPropertyChangeListeners(String)
    */
   public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
     if (listener == null || this.changeSupport == null) {
       return;
     }
     this.changeSupport.removePropertyChangeListener(propertyName, listener);
   }
   
   /**
    * Returns an array of all the listeners which have been associated 
    * with the named property.
    *
    * @param propertyName a valid property name.
    * 
    * @return all of the {@link PropertyChangeListeners} associated with
    *         the named property or an empty array if no listeners have 
    *         been added
    */
   public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
     if (this.changeSupport == null) {
       return new PropertyChangeListener[0];
     }
     return this.changeSupport.getPropertyChangeListeners(propertyName);
   }
   
   
   
   //
   // Listener invocation
   //
   
   /**
    * Support for reporting bound property changes for Object properties. 
    * This method can be called when a bound property has changed and it will
    * send the appropriate {@link PropertyChangeEvent} to any registered
    * {@link PropertyChangeListener}s.
    *
    * @param propertyName the property whose value has changed.
    * @param oldValue the property's previous value.
    * @param newValue the property's new value.
    */
   protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
     PropertyChangeSupport changeSupport = this.changeSupport;
     if (changeSupport == null) {
       return;
     }
     changeSupport.firePropertyChange(propertyName, oldValue, newValue);
   }
 
   /**
    * Support for reporting bound property changes for boolean properties. 
    * This method can be called when a bound property has changed and it will
    * send the appropriate {@link PropertyChangeEvent} to any registered
    * {@link PropertyChangeListener}s.
    *
    * @param propertyName the propery whose value has changed.
    * @param oldValue the property's previous value.
    * @param newValue the property's new value.
    */
   protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
     PropertyChangeSupport changeSupport = this.changeSupport;
     if (changeSupport == null) {
       return;
     }
     changeSupport.firePropertyChange(propertyName, oldValue, newValue);
   }
   
   /**
    * Support for reporting bound property changes for boolean properties. 
    * This method can be called when a bound property has changed and it will
    * send the appropriate {@link PropertyChangeEvent} to any registered
    * {@link PropertyChangeListener}s.
    *
    * @param propertyName the propery whose value has changed.
    * @param oldValue the property's previous value.
    * @param newValue the property's new value.
    */
   protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
     PropertyChangeSupport changeSupport = this.changeSupport;
     if (changeSupport == null) {
       return;
     }
     changeSupport.firePropertyChange(propertyName, oldValue, newValue);
   }
 }
