 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.types.Type;
 import org.xins.types.TypeValueException;
 
 /**
  * Session.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.52
  */
 public final class Session
 extends Object {
 
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
     * Constructs a new <code>Session</code> with the specified ID.
     *
     * @param api
     *    the API this session is associated with, cannot be <code>null</code>.
     *
     * @param id
     *    the identifier for this session, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || id == null</code>.
     */
    public Session(API api, Object id)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("api", api, "id", id);
       _sessionIDType = api.getSessionIDType();
       _id  = id;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The session ID type associated with this session, never
     * <code>null</code>.
     */
    private final SessionIDType _sessionIDType;
 
    /**
     * The identifier for this session.
     */
    private final Object _id;
 
    /**
     * Attributes for this session. This map contains {@link String} keys and
     * {@link Object} values.
     *
     * <p>This field is lazily initialized, so it is initially
     * <code>null</code>.
     */
    private Map _attributes;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the session ID type for this session.
     *
     * @return
     *    the session ID type associated with this session, never
     *    <code>null</code>.
     */
    public SessionIDType getSessionIDType() {
       return _sessionIDType;
    }
 
    /**
     * Gets the identifier.
     *
     * @return
     *    the identifier, never <code>null</code>.
     */
    public Object getID() {
       return _id;
    }
 
    /**
     * Gets the identifier, converted to a string.
     *
     * @return
     *    the session ID, converted to a {@link String}, never
     *    <code>null</code>.
     */
    public String getIDString() {
       try {
          return getSessionIDType().toString(_id);
       } catch (TypeValueException exception) {
          String message = "Caught unexpected " + exception.getClass().getName() + '.';
          Library.RUNTIME_LOG.error(message, exception);
          throw new Error(message);
       }
    }
 
    /**
     * Sets or resets the specified attribute. If the specified value is
     * <code>null</code> then the attribute setting will be removed (if it
     * existed at all).
     *
     * @param key
     *    the attribute key, cannot be <code>null</code>.
     *
     * @param value
     *    the attribute value, or <code>null</code> if the attribute should be
     *    reset.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public void setAttribute(String key, Object value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       boolean debugEnabled = Library.RUNTIME_LOG.isDebugEnabled();
 
       // If necessary init the Map and then store the entry
       if (_attributes == null) {
 
          if (value != null) {
             // Log this event
            Log.log_5006(key, value.getClass().getName(), value.toString());
 
             // Perform the actual action
             _attributes = new HashMap(89);
             _attributes.put(key, value);
          } else {
             Log.log_5007(key);
          }
 
       // If the value is null, then remove the entry
       } else if (value == null) {
          Log.log_5007(key);
          _attributes.remove(key);
          // XXX: Check if the map is now empty and set it to null?
 
       // Otherwise store a new entry
       } else {
          // Log this event
         Log.log_5006(key, value.getClass().getName(), value.toString());
 
          // Perform the actual action
          _attributes.put(key, value);
       }
    }
 
    /**
     * Gets all attributes and their values.
     *
     * @return
     *    the modifiable map of attributes, or <code>null</code> if there are
     *    no attributes.
     (
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. This method allows
     *    direct changing of the underlying collection. Also, it interferes
     *    with the logging of session attribute get and set operations.
     *    Although this method is deprecated, it still works as described.
     */
    public Map getAttributes() {
 
       // TODO: Return an unmodifiable Map ?
 
       return _attributes;
    }
 
    /**
     * Gets the value of the attribute with the specified key.
     *
     * @param key
     *    the attribute key, cannot be <code>null</code>.
     *
     * @return
     *    the attribute value, or <code>null</code> if the attribute is not
     *    set.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object getAttribute(String key)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
      Object value;

       if (_attributes != null) {

         // Get the value by key from the Map
         value = _attributes.get(key);
 
          // Log this event
         if (value != null) {
            Log.log_5008(key, value.getClass().getName(), value.toString());
         } else {
            Log.log_5009(key);
         }
 
      // The Map does not exist, consequently the value is null
       } else {
         Log.log_5009(key);
         value = null;
       }

      return value;
    }
 
    public String toString() {
       return _id.toString();
    }
 }
