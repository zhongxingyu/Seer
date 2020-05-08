 package org.javamvc.view;
 
 import java.awt.Component;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.javamvc.GUIApplication;
 
 /**
  * A <code>View</code> is a component displayed on screen to the user. This
  * class provides methods for customizing the view.
  * 
  * @author Erich Schroeter
  */
 public abstract class View<C extends Component> {
 
 	/**
 	 * The view component. This is the object which, for all intents and
 	 * purposes, is the view.
 	 */
 	protected C view;
 	/** A reference to the GUI application. */
 	protected GUIApplication application;
 	/** A map of keys to subcomponents in the {@link #view}. */
 	protected Map<String, ? super Component> subcomponents;
 
 	/**
 	 * Constructs a <code>View</code> specifying the application the view
 	 * belongs to and the view component. This initializes the view by calling
	 * {@link #initializeView()}.
 	 * 
 	 * @param app
 	 *            the application this view belongs to
 	 * @param view
 	 *            the view component to be displayed on screen
 	 */
 	public View(GUIApplication app, C view) {
 		super();
 		setApplication(app);
 		this.view = view;
 		subcomponents = new HashMap<String, Component>();
 		initializeView();
 	}
 
 	/**
 	 * Initializes the view. This is where you should customize the view itself
 	 * and add components to the {@link #view}. This can be overridden in
 	 * derived classes to customize the view further. Make sure to call
 	 * <code>super.initializeView()</code> to ensure the default initialization
 	 * values are kept.
 	 */
 	protected abstract void initializeView();
 
 	/**
 	 * Returns the view object which, for all intents and purposes, is the view.
 	 * This is the GUI component which is displayed on the screen.
 	 * 
 	 * @return the view object
 	 */
 	public C getView() {
 		return view;
 	}
 
 	/**
 	 * Returns a map of keys to subcomponents in the {@link #view}. All
 	 * subcomponents to allow access to classes outside of the <code>View</code>
 	 * need to be added to the map.
 	 * 
 	 * @return the map of keys to subcomponents
 	 */
 	public Map<String, ? super Component> getSubcomponents() {
 		return subcomponents;
 	}
 
 	/**
 	 * Registers the <code>subcomponent</code> with the specified
 	 * <code>key</code>. The <code>subcomponent</code> may be accessed by
 	 * passing the <code>key</code> to {@link #getSubcomponent(String)}.
 	 * 
 	 * @see #unregisterSubcomponent(String)
 	 * @param key
 	 *            the key to access <code>subcomponent</code>
 	 * @param subcomponent
 	 *            the subcomponent being mapped by <code>key</code>
 	 */
 	protected void registerSubcomponent(String key, Component subcomponent) {
 		subcomponents.put(key, subcomponent);
 	}
 
 	/**
 	 * Unregisters the <code>subcomponent</code> mapped by the specified
 	 * <code>key</code>.
 	 * 
 	 * @see #registerSubcomponent(String, Component)
 	 * @param key
 	 *            the key to access <code>subcomponent</code>
 	 */
 	protected void unregisterSubcomponent(String key) {
 		subcomponents.remove(key);
 	}
 
 	/**
 	 * Returns the subcomponent mapped to the specified <code>key</code>.
 	 * 
 	 * @param key
 	 *            the key mapped to the desired subcomponent
 	 * @return the subcomponent mapped to <code>key</code>
 	 */
 	public Component getSubcomponent(String key) {
 		return (Component) subcomponents.get(key);
 	}
 
 	/**
 	 * Returns the reference to the GUI application this view belongs to.
 	 * 
 	 * @return the GUI application
 	 */
 	public GUIApplication getApplication() {
 		return application;
 	}
 
 	/**
 	 * Returns the reference to the GUI application this view belongs to.
 	 * 
 	 * @param application
 	 *            the GUI application
 	 */
 	public void setApplication(GUIApplication application) {
 		this.application = application;
 	}
 
 }
