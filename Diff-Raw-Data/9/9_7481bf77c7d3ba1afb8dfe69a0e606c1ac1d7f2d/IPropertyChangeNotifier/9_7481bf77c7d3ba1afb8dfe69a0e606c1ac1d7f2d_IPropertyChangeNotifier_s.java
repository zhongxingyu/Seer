 package org.javamvc;
 
 import java.beans.PropertyChangeListener;
 
 /**
  * The <code>PropertyChangeNotifier</code> interface provides methods for
  * objects to notify {@link PropertyChangeListener} instances. This is simply a
  * convenience interface for automatically including methods via an IDE (such as
  * Eclipse) auto-generate feature.
  * <p>
  * Classes implementing this interface will need to provide methods for
  * notifying the <code>PropertyChangeListener</code>s such as
  * <ul>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, Object oldValue,
  * Object newValue);</code></li>
  * </ul>
  * or for specific data types
  * <ul>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, boolean oldValue,
  * boolean newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, int oldValue, int
  * newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, byte oldValue,
  * byte newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, char oldValue,
  * char newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, short oldValue,
  * short newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, long oldValue,
  * long newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, float oldValue,
  * float newValue);</code></li>
  * <li>
  * <code>protected void firePropertyChange(String propertyName, double oldValue,
  * double newValue);</code></li>
  * </ul>
  * <p>
  * A implementation example is provided below to cut down on implementation
  * time.
  * 
  * <pre>
  *  {@code
  *  
  * 	
  * 	/** Used for managing property listeners. *&#047;
  * 	protected PropertyChangeSupport properties;
  * 
  * 	//
  * 	// PropertyChangeNotifier members
  * 	//
  * 
  * /**
  *  * Add a PropertyChangeListener to the listener list. The listener is
  *  * registered for all properties. The same listener object may be added more
  *  * than once, and will be called as many times as it is added. If
  *  * &lt;code&gt;listener&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;, no exception is thrown and no
  *  * action is taken.
  *  * 
  *  * @param listener
  *  *            the property change listener to be added
  *  *&#047;
  * public void addPropertyChangeListener(PropertyChangeListener listener) {
  * 	properties.addPropertyChangeListener(listener);
  * }
  * 
  * /**
  *  * Add a PropertyChangeListener for a specific property. The listener will
  *  * be invoked only when a call on firePropertyChange names that specific
  *  * property. The same listener object may be added more than once. For each
  *  * property, the listener will be invoked the number of times it was added
  *  * for that property. If &lt;code&gt;propertyName&lt;/code&gt; or &lt;code&gt;listener&lt;/code&gt;
  *  * is &lt;code&gt;null&lt;/code&gt;, no exception is thrown and no action is taken.
  *  * 
  *  * @param propertyName
  *  *            the name of the property to listen on
  *  * @param listener
  *  *            the property change listener to be added
  *  *&#047;
  * public void addPropertyChangeListener(String propertyName,
  * 		PropertyChangeListener listener) {
  * 	properties.addPropertyChangeListener(propertyName, listener);
  * }
  * 
  * /**
  *  * Removes a &lt;code&gt;PropertyChangeListener&lt;/code&gt; from the listener list.
  *  * This removes a PropertyChangeListener that was registered for all
  *  * properties. If &lt;code&gt;listener&lt;/code&gt; was added more than once to the same
  *  * event source, it will be notified one less time after being removed. If
  *  * &lt;code&gt;listener&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;, or was never added, no
  *  * exception is thrown and no action is taken.
  *  * 
  *  * @param listener
  *  *            the property change listener to be removed
  *  *&#047;
  * public void removePropertyChangeListener(PropertyChangeListener listener) {
  * 	properties.removePropertyChangeListener(listener);
  * }
  * 
  * /**
  *  * Remove a PropertyChangeListener for a specific property. If
  *  * &lt;code&gt;listener&lt;/code&gt; was added more than once to the same event source
  *  * for the specified property, it will be notified one less time after being
  *  * removed. If &lt;code&gt;propertyName&lt;/code&gt; is null, no exception is thrown and
  *  * no action is taken. If &lt;code&gt;listener&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;, or was
  *  * never added for the specified property, no exception is thrown and no
  *  * action is taken.
  *  * 
  *  * @param propertyName
  *  *            the name of the property that was listened on
  *  * @param listener
  *  *            the property change listener to be removed
  *  *&#047;
  * public void removePropertyChangeListener(String propertyName,
  * 		PropertyChangeListener listener) {
  * 	properties.removePropertyChangeListener(propertyName, listener);
  * }
  * 
  * /**
  *  * Returns an array of all the listeners that were added to the
  *  * PropertyChangeSupport object with addPropertyChangeListener().
  *  * &lt;p&gt;
  *  * If some listeners have been added with a named property, then the
  *  * returned array will be a mixture of PropertyChangeListeners and
  *  * &lt;code&gt;PropertyChangeListenerProxy&lt;/code&gt;s. If the calling method is
  *  * interested in distinguishing the listeners then it must test each element
  *  * to see if it's a &lt;code&gt;PropertyChangeListenerProxy&lt;/code&gt;, perform the
  *  * cast, and examine the parameter.
  *  * 
  *  * &lt;pre&gt;
  *  * PropertyChangeListener[] listeners = bean.getPropertyChangeListeners();
  *  * for (int i = 0; i &lt; listeners.length; i++) {
  *  * 	if (listeners[i] instanceof PropertyChangeListenerProxy) {
  *  * 		PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listeners[i];
  *  * 		if (proxy.getPropertyName().equals(&quot;foo&quot;)) {
  *  * 			// proxy is a PropertyChangeListener which was associated
  *  * 			// with the property named &quot;foo&quot;
  *  * 		}
  *  * 	}
  *  * }
  *  * &lt;/pre&gt;
  *  * 
  *  * @return all of the &lt;code&gt;PropertyChangeListener&lt;/code&gt;s added or an empty
  *  *         array if no listeners have been added
  *  *&#047;
  * public PropertyChangeListener[] getPropertyChangeListeners() {
  * 	return properties.getPropertyChangeListeners();
  * }
  * 
  * /**
  *  * Returns an array of all the listeners which have been associated with the
  *  * named property.
  *  * 
  *  * @param propertyName
  *  *            the name of the property being listened to
  *  * @return all of the &lt;code&gt;PropertyChangeListeners&lt;/code&gt; associated with
  *  *         the named property. If no such listeners have been added, or if
  *  *         &lt;code&gt;propertyName&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;, an empty array is
  *  *         returned.
  *  *&#047;
  * public PropertyChangeListener[] getPropertyChangeListeners(
  * 		String propertyName) {
  * 	return properties.getPropertyChangeListeners(propertyName);
  * }
  * 
  * /**
  *  * Report a bound property update to any registered listeners. No event is
  *  * fired if old and new are equal and non-null.
  *  * &lt;p&gt;
  *  * This is merely a convenience wrapper around the more general
  *  * firePropertyChange method that takes &lt;code&gt;PropertyChangeEvent&lt;/code&gt;
  *  * value.
  *  * 
  *  * @param propertyName
  *  *            one of the property names listed above
  *  * @param oldValue
  *  *            the old value of the property
  *  * @param newValue
  *  *            the new value of the property
  *  *&#047;
  * protected void firePropertyChange(String propertyName, Object oldValue,
  * 		Object newValue) {
  * 	properties.firePropertyChange(propertyName, oldValue, newValue);
  * }
 * 
  * </pre>
  * 
  * @author Erich Schroeter
  */
 public interface IPropertyChangeNotifier {
 
 	/**
 	 * Adds a <code>PropertyChangeListener</code> to the listener list. The
 	 * listener is registered for all bound properties of this class.
 	 * <p>
 	 * If <code>listener</code> is <code>null</code>, no exception is thrown and
 	 * no action is performed.
 	 * <p>
 	 * <em><b>NOTE:</b> It would be wise if classes implementing this method 
 	 * provide within the Javadoc a list of the class properties available.</em>
 	 * 
 	 * @param listener
 	 *            the property change listener to be added
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener);
 
 	/**
 	 * Adds a <code>PropertyChangeListener</code> to the listener list for a
 	 * specific property of this class.
 	 * <p>
 	 * If <code>propertyName</code> or <code>listener</code> is
 	 * <code>null</code>, no exception is thrown and no action is performed.
 	 * <p>
 	 * <em><b>NOTE:</b> It would be wise if classes implementing this method 
 	 * provide within the Javadoc a list of the class properties available.</em>
 	 * 
 	 * @param propertyName
 	 *            one of the property names listed above
 	 *            <em>(provided the implementor included them)</em>
 	 * @param listener
 	 *            the property change listener to be added
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener);
 
 	/**
 	 * Removes a <code>PropertyChangeListener</code> from the listener list.
 	 * This method should be used to remove PropertyChangeListeners that were
 	 * registered for all bound properties of this class.
 	 * <p>
 	 * If <code>listener</code> is <code>null</code>, no exception is thrown and
 	 * no action is performed.
 	 * <p>
 	 * <em><b>NOTE:</b> It would be wise if classes implementing this method 
 	 * provide within the Javadoc a list of the class properties available.</em>
 	 * 
 	 * @param listener
 	 *            the property change listener to be removed
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener);
 
 	/**
 	 * Removes a <code>PropertyChangeListener</code> from the listener list for
 	 * a specific property of this class.
 	 * <p>
 	 * If <code>propertyName</code> or <code>listener</code> is
 	 * <code>null</code>, no exception is thrown and no action is performed.
 	 * <p>
 	 * <em><b>NOTE:</b> It would be wise if classes implementing this method 
 	 * provide within the Javadoc a list of the class properties available.</em>
 	 * 
 	 * @param propertyName
 	 *            one of the property names listed above
 	 *            <em>(provided the implementor included them)</em>
 	 * @param listener
 	 *            the property change listener to be removed
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener);
 
 	/**
 	 * Returns the list of all the property change listeners registered for all
 	 * properties of this class.
 	 * 
 	 * @return all of this class's <code>PropertyChangeListener</code>s or
 	 *         <code>null</code> if no property change listeners are currently
 	 *         registered.
 	 */
 	public PropertyChangeListener[] getPropertyChangeListeners();
 
 	/**
 	 * Returns the list of all the property change listeners registered for a
 	 * specific property of this class.
 	 * <p>
 	 * <em><b>NOTE:</b> It would be wise if classes implementing this method 
 	 * provide within the Javadoc a list of the class properties available.</em>
 	 * 
 	 * @param propertyName
 	 *            one of the property names listed above
 	 *            <em>(provided the implementor included them)</em>
 	 * @return all of this class's <code>PropertyChangeListener</code>s or
 	 *         <code>null</code> if no property change listeners are currently
 	 *         registered.
 	 */
 	public PropertyChangeListener[] getPropertyChangeListeners(
 			String propertyName);
 
 }
