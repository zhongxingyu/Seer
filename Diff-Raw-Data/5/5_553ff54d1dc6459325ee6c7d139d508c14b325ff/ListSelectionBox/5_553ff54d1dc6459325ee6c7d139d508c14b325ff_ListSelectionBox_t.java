 package jfmi.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 
 /** A ListSelectionBox is a simply Swing Box which contains two lists. One
   list contains items for the user to select, the second contains items the
   user has already selected. The class provides methods for clients to 
   check which items are selected, set the list of items to be used, etc.
   */
 public class ListSelectionBox<T extends Comparable<T>> 
 	extends Box 
 	implements ActionListener {
 
 	// Private Instance Fields
 	private JButton addButton;
 	private JButton removeButton;
 
 	private MutableListModel<T> unselectedModel;
 	private JList<T> unselectedList;
 
 	private MutableListModel<T> selectedModel;
 	private JList<T> selectedList;
 
 	private JScrollPane unselectedScroller;
 	private JScrollPane selectedScroller;
 
 
 	//************************************************************
 	// PUBLIC Instance Methods
 	//************************************************************
 
 	/** Constructs a default ListSelectionBox with empty item lists.
 	  */
 	public ListSelectionBox()
 	{
 		this(null, null); 
 	}
 
 	/** Constructs a ListSelectionBox with the specified set of items available
 	  for selection.
 	  @param unselected the set of items to be listed for selection
 	  */
 	public ListSelectionBox(SortedSet<T> unselected)
 	{
 		this(unselected, null);
 	}
 
 	/** Constructs a ListSelectionBox with the specified set of selectable items,
 	  and the specified set of selected items.
 	  @param unselected the unselected set of selectable items
 	  @param selected the set of already selected items
 	  */
 	public ListSelectionBox(
 		SortedSet<T> unselected, 
 		SortedSet<T> selected
 	)
 	{
 		// Initialize instance
 		super(BoxLayout.X_AXIS);
 
 		// Initialize item sets
 		if (unselected == null) {
 			unselected = new TreeSet<T>();
 		}
 
 		if (selected == null) {
 			selected = new TreeSet<T>();
 		}
 
 		// Initialize buttons
 		addButton = new JButton("Add >>");
 		addButton.addActionListener(this);
 		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		removeButton = new JButton("<< Remove");
 		removeButton.addActionListener(this);
 		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		// Initialize lists
 		unselectedModel = new MutableListModel<T>(unselected);
 		unselectedList = new JList<T>(unselectedModel);
 
 		selectedModel = new MutableListModel<T>(selected);
 		selectedList = new JList<T>(unselectedModel);
 
 		// Initialize scrollpanes
 		unselectedScroller = new JScrollPane(unselectedList);
 		unselectedScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		selectedScroller = new JScrollPane(selectedList);
 		selectedScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		// Add components
 		Box unselectedBox = Box.createVerticalBox();
 		unselectedBox.add(unselectedScroller);
 		unselectedBox.add(addButton);
 
 		Box selectedBox = Box.createVerticalBox();
 		selectedBox.add(selectedScroller);
 		selectedBox.add(removeButton);
 
 		add(unselectedBox);
 		add(Box.createHorizontalStrut(10));
 		add(selectedBox);
 	}
 
 	/** Returns the list of selected items.
 	  */
 	public List<T> getSelectedItems()
 	{
 		return selectedModel.getDataCopy();
 	}
 
 	/** Returns the list of unselected items.
 	  */
 	public List<T> getUnselectedItems()
 	{
 		return unselectedModel.getDataCopy();
 	}
 
 	/** Sets the contents of the list of selected items to the specified
 	  set. A null argument clears the list.
	  @param items a set of items to be listed as selected
 	  */
 	public void setSelectedItems(SortedSet<T> items)
 	{
 		if (items != null) {
 			selectedModel = new MutableListModel<T>(items);
 		} else {
 			selectedModel = new MutableListModel<T>();
 		}
 
 		selectedList.setModel(selectedModel);
 	}
 
 	/** Sets the contents of the list of unselected items to the specified
 	  set. A null argument clears the list.
	  @param items a set of items to be listed as unselected
 	  */
 	public void setUnselectedItems(SortedSet<T> items)
 	{
 		if (items != null) {
 			unselectedModel = new MutableListModel<T>(items);
 		} else {
 			unselectedModel = new MutableListModel<T>();
 		}	
 
 		unselectedList.setModel(unselectedModel);
 	}
 
 	//************************************************************
 	// IMPLEMENTATION of ActionListener
 	//************************************************************
 
 	/** Specifies what action to perform when an ActionEvent is fired.
 	  @param e the ActionEvent to respond to
 	  */
 	public void actionPerformed(ActionEvent e)
 	{
 		Object src = e.getSource();
 
 		if (src == addButton) {
 			addButtonAction();
 		} else if (src == removeButton) {
 			removeButtonAction();
 		}
 	}
 
 
 	//************************************************************
 	// PRIVATE Instance Methods
 	//************************************************************
 
 	/** Specifies what happens when the user clicks the addButton.
 	  */
 	private void addButtonAction()
 	{
 		T value = unselectedList.getSelectedValue();
 
 		if (value == null) {
 			return;
 		}
 
 		unselectedModel.remove(value);
 		selectedModel.add(value);
 	}
 
 	/** Specifies what happens when the user clicks the removeButton.
 	  */
 	private void removeButtonAction()
 	{
 		T value = selectedList.getSelectedValue();
 
 		if (value == null) {
 			return;
 		}
 
 		selectedModel.remove(value);
 		unselectedModel.add(value);
 	}
 
 }
