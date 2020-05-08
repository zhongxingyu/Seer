 package foobar;
 
 import java.awt.BorderLayout;
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.swing.JPanel;
 
 import frontend.Bruno;
 
 /**
  * Foobar is a container class for FoobarField and FoobarSuggestions. The Foobar
  * is a fuzzy-matching, suggestive command interface for the Bruno text editor.
  * Fooables may be added to Foobar using the addFooable and addFooables methods.
  * All other methods in this class are only used within the package.
  * 
  * @author Frank Goodman
  * 
  */
 public final class Foobar extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The complete collection of Fooables callable from this Foobar
 	 */
 	private final Collection<Fooable> fooables;
 
 	/**
 	 * The field for Fooable query entries
 	 */
 	private final FoobarField field;
 
 	/**
 	 * The manager for Popups displaying suggested Fooables
 	 */
 	private final FoobarPopupManager popupManager;
 
 	/**
 	 * The parent to Foobar
 	 */
 	private Bruno parent;
 
 	/**
 	 * Create a new Foobar containing no Fooables.
 	 */
 	public Foobar(Bruno parent) {
 		super(new BorderLayout());
 
 		// Store a reference to the parent
 		this.parent = parent;
 
 		// Store all Fooables in a HashSet
 		this.fooables = new HashSet<>();
 
 		// Add the FoobarField to the Foobar
 		this.field = new FoobarField();
 		this.add(this.field);
 
 		// Create a FoobarPopupFactory
 		this.popupManager = new FoobarPopupManager(this);
 
 		this.setVisible(true);
 	}
 
 	/**
 	 * Add a new Fooable to the Foobar's list of Fooables.
 	 * 
 	 * @param f
 	 *            A Fooable
 	 */
 	public void addFooable(Fooable f) {
 		this.fooables.add(f);
 	}
 
 	/**
 	 * Add a collection of Fooables to the Foobar's list of Fooables
 	 * 
 	 * @param f
 	 *            A collection of Fooables
 	 */
 	public void addFooables(Collection<? extends Fooable> f) {
 		this.fooables.addAll(f);
 	}
 
 	/**
 	 * Remove a Fooable from the Foobar's list of Fooables.
 	 * 
 	 * @param f
 	 *            A Fooable
 	 */
 	public void removeFooable(Fooable f) {
 		this.fooables.remove(f);
 	}
 
 	/**
 	 * Remove a collection of Fooables from the Foobar's list of Fooables
 	 * 
 	 * @param f
 	 *            A collection of Fooables
 	 */
 	public void removeFooables(Collection<? extends Fooable> f) {
 		this.fooables.removeAll(f);
 	}
 
 	/**
 	 * Execute the selected Fooable, hide the suggested Fooables, and clear the
 	 * text field.
 	 */
 	protected void executeFooable() {
 		if (!this.field.getText().equals("")) {
 			// Get Fooable
 			Fooable f = this.getPopupManager().getSuggestions()
 					.getSelectedValue();
 
 			// Hide the suggested Fooables
 			this.popupManager.destroyPopup();
 
 			// Clear the text field
 			this.field.setText("");
 
 			// Execute action
 			if (f != null)
 				f.doAction();
 
 			// Focus back on text area
 			if (this.parent != null)
 				this.parent.requestFocusInWindow();
 		}
 	}
 
 	/**
 	 * Get the first suggested Fooable's name that starts with 'query' and
 	 * select it. If no Fooables are suggested, then the original value is
 	 * returned.
 	 * 
 	 * @param query
 	 *            The term to autocomplete
 	 * @return The autocompleted Fooable's name
 	 */
 	protected String completeFooable(String query) {
 		// Verify suggestions are present
 		if (this.getPopupManager().getSuggestions() != null) {
 			// Check each Fooable's starting character sequence
 			for (Fooable fooable : this.getPopupManager().getSuggestions()
 					.getFooables()) {
 				if (fooable.getName().startsWith(query))
 					return fooable.getName();
 			}
 		}
 
 		// Return the original query if no Fooables begin with 'query'
 		return query;
 	}
 
 	/**
 	 * Display the FoobarSuggestions popup with corresponding suggestions for
 	 * the term 'query'
 	 * 
 	 * @param query
 	 *            The term by which to search for suggestions
 	 */
 	protected void showSuggestions(String query) {
 		this.popupManager.createPopup(FoobarSuggester.getSuggestions(query,
 				this.fooables));
 	}
 
 	@Override
 	public boolean requestFocusInWindow() {
 		return this.field.requestFocusInWindow();
 	}
 
 	/**
 	 * Retrieve the FoobarPopupManager used by Foobar
 	 * 
 	 * @return The FoobarPopupManager used by Foobar
 	 */
 	protected FoobarPopupManager getPopupManager() {
 		return this.popupManager;
 	}

	/**
	 * Get the Foobar's text field to check for focus.
	 * 
	 * @return The text field used by Foobar
	 */
	public FoobarField getField() {
		return this.field;
	}
 }
