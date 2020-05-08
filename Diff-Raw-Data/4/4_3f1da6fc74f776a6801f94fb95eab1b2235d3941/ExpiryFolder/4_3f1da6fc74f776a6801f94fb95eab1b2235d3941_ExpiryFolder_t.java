 /*
  * $Id$
  */
 package org.xins.util.collections.expiry;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
 * Expiry folder. Contains values indexed by key. Entries in this folder will
 * expire after a predefined amount of time, unless they're accessed within that
  * timeframe.
  *
  * <p>This class is thread-safe.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class ExpiryFolder
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ExpiryFolder</code>.
     *
     * @param strategy
     *    the strategy that should be applied, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>strategy == null</code>.
     */
    public ExpiryFolder(ExpiryStrategy strategy)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("strategy", strategy);
 
       // Initialize fields
       _strategy         = strategy;
       _recentlyAccessed = new HashMap(89);
       _slotCount        = strategy.getSlotCount();
       _slots            = new Map[_slotCount];
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The strategy used. This field cannot be <code>null</code>.
     */
    private final ExpiryStrategy _strategy;
 
    /**
     * The most recently accessed entries. This field cannot be
     * <code>null</code>. The entries in this map will expire after
     * {@link ExpiryStrategy#getTimeOut()} milliseconds, plus at maximum
     * {@link ExpiryStrategy#getPrecision()} milliseconds.
     */
    private Map _recentlyAccessed;
 
    /**
     * Number of active slots. Always equals
     * {@link #_slots}<code>.length</code>.
     */
    private final int _slotCount;
 
    /**
     * Slots to contain the maps with entries that are not the most recently
     * accessed. The further back in the array, the faster the entries will
     * expire.
     */
    private final Map[] _slots;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Touches the entry that is identified by the specified key.
     *
     * @param key
     *    the key that identifies the entry, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     *
     * @throws NoSuchEntryException
     *    if <code>get(key) == null</code>.
     */
    public abstract void touch(Object key) throws NoSuchEntryException;
 
    /**
     * Notifies this map that the precision time frame has passed since the
     * last tick.
     *
     * <p>If any entries are expirable, they will be removed from this map.
     */
    void tick() {
       int lastSlotIndex = _slotCount - 1;
 
       synchronized (_recentlyAccessed) {
          Map toBeExpired = _slots[lastSlotIndex];
          // TODO: Map doNotExpire = _validator.validateExpiry(toBeExpired);
          for (int i = lastSlotIndex; i > 0; i--) {
             _slots[i] = _slots[i - 1];
          }
          _slots[0] = _recentlyAccessed;
          _recentlyAccessed = new HashMap(89);
       }
    }
 
    /**
     * Gets the number of entries.
     *
     * @return
     *    the number of entries in this expiry folder, always &gt;= 0.
     */
    public int size() {
       int size;
       int slotCount = _slots.length;
       synchronized (_recentlyAccessed) {
          synchronized (_slots) {
             size = _recentlyAccessed.size();
             for (int i = 0; i < slotCount; i++) {
                size += _slots[i].size();
             }
          }
       }
       return size;
    }
 
    /**
     * Checks if this folder is completely empty.
     *
     * @return
     *    <code>true</code> if and only if there are no entries in this folder.
     */
    public boolean isEmpty() {
 
       synchronized (_recentlyAccessed) {
          if (_recentlyAccessed.isEmpty() == false) {
             return false;
          }
       }
 
       int slotCount = _slots.length;
       for (int i = 0; i < slotCount; i++) {
          Map slot = _slots[i];
          synchronized (slot) {
             if (slot.isEmpty() == false) {
                return false;
             }
          }
       }
 
       return true;
    }
 
    /**
     * Gets the value associated with a key.
     *
     * @param key
     *    the key to lookup, cannot be <code>null</code>.
     *
     * @return
     *    the value associated with the specified key, or <code>null</code> if
     *    and only if this folder does not contain an entry with the specified
     *    key.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object get(Object key) throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       // Check _recentlyAccessed
       synchronized (_recentlyAccessed) {
          Object o = _recentlyAccessed.get(key);
          if (o != null) {
             return o;
          }
       }
 
       // Check all slots
       int slotCount = _slots.length;
       for (int i = 0; i < slotCount; i++) {
          Map slot = _slots[i];
          synchronized (slot) {
             Object o = slot.get(key);
             if (o != null) {
                return o;
             }
          }
       }
 
       return null;
    }
 
    /**
     * Associates the specified value with the specified key in this folder.
     *
     * @param key
     *    they key for the entry, cannot be <code>null</code>.
     *
     * @param value
     *    they value for the entry, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null || value == null</code>.
     */
    public void put(Object key, Object value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key, "value", value);
 
       synchronized (_recentlyAccessed) {
          _recentlyAccessed.put(key, value);
       }
    }
 
    /**
     * Removes all entries.
     */
    public void clear() {
       int slotCount = _slots.length;
       synchronized (_recentlyAccessed) {
          _recentlyAccessed.clear();
 
          for (int i = 0; i < slotCount; i++) {
             Map slot = _slots[i];
             synchronized (slot) {
                slot.clear();
             }
          }
       }
    }
 }
