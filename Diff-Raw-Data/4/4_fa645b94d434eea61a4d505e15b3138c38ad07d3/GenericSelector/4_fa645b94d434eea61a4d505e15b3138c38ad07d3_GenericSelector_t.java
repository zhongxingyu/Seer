 package editor;
 
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import configuration.Configuration;
 import editor.BigFrameworkGuy.ConfigType;
 
 
 public class GenericSelector<T> extends JPanel {
 	
 	public interface GenericListCallback<E> {
 		public List<E> getSelectionList();
 	}
 	
 	public interface CustomToString<E> {
 		public String toString(E obj);
 	}
 	
 	public interface SelectionChangeListener<E> {
 		public void selectionChanged(E newSelection);
 	}
 	
 	public static class ShowBFGName<E extends Configuration> implements CustomToString<E> {
 
 		@Override
 		public String toString(E obj) {
 			return (String) obj.getPropertyForName("bfgName").getValue();
 		}
 		
 	}
 	
 	public static class SelectConfigurations<E extends Configuration> implements GenericListCallback<E> {
 
 		private BigFrameworkGuy bfg;
 		private ConfigType type;
 		
 		public SelectConfigurations(BigFrameworkGuy bfg, ConfigType type) {
 			this.bfg = bfg;
 			this.type = type;
 		}
 		
 		@Override
 		public List<E> getSelectionList() {
 			List<E> ret = new ArrayList<E>();
 			for(Configuration c : bfg.getConfigurationsByType(type))
 				ret.add((E)c);
 			return ret;
 		}
 		
 	}
 	
 	private static final int WIDTH = 160;
 	protected static final int SPACING = 5;
 	
 	protected JButton button;
 	protected JLabel label;
 	protected JTextField textField;
 	protected GenericListCallback<T> callback;
 	protected CustomToString<T> customToString;
 	
 	private T selectedObject;
 	
 	private List<SelectionChangeListener<T>> observers = new ArrayList<GenericSelector.SelectionChangeListener<T>>();
 	
 	public GenericSelector(String label, GenericListCallback<T> callback)
 	{
 		this(label, callback, new CustomToString<T>() {
 			@Override
 			public String toString(T obj) {
 				return obj.toString();
 			}
 		});
 	}
 	
 	public GenericSelector(String label, GenericListCallback<T> callback, CustomToString<T> customToString)
 	{
 		this.callback = callback;
 		this.customToString = customToString;
 
 		button = new JButton("Select");
 		button.addActionListener(new ButtonClickEvent());
 		this.label = new JLabel(label);
 		
 		textField = new JTextField();
 		textField.setPreferredSize(new Dimension(WIDTH, 20));
 		textField.setEditable(false);
 		
 		add(this.label);
 		add(Box.createHorizontalStrut(SPACING));
 		add(textField);
 		add(Box.createHorizontalStrut(SPACING));
 		add(button);
 	}
 	
 	public void addSelectionChangeListener(SelectionChangeListener<T> listener)
 	{
 		observers.add(listener);
 	}
 	
 	public void removeSelectionChangeListener(SelectionChangeListener<T> listener)
 	{
 		observers.remove(listener);
 	}
 	
 	private void notifyObservers(T newSelection)
 	{
 		for(SelectionChangeListener<T> l : observers)
 			l.selectionChanged(newSelection);
 	}
 	
 	public void setListCallback(GenericListCallback<T> callback)
 	{
 		this.callback = callback;
 	}
 	
 	public T getSelectedObject()
 	{
 		return selectedObject;
 	}
 	
 	public void setSelectedObject(T obj)
 	{
 		selectedObject = obj;
 		if(obj != null)
 			textField.setText(customToString.toString(obj));
 		else
 			textField.setText("");
 		notifyObservers(selectedObject);
 	}
 	
 	protected List<ListItemWrapper> getWrappers(List<T> objs) {
 		List<ListItemWrapper> ret = new ArrayList<ListItemWrapper>();
 		for(T obj : objs)
 			ret.add(new ListItemWrapper(obj));
 		return ret;
 	}
 	
 	/**
 	 * @return
 	 * @throws HeadlessException
 	 */
 	protected T showSelectionDialog(List<T> selectableList) throws HeadlessException {
 		Object[] opts = getWrappers(selectableList).toArray();
 		Object o = JOptionPane.showInputDialog(
 				GenericSelector.this,
 				"Please select a " + label.getText() + ".",
 				label.getText() + " Selector",
 				JOptionPane.PLAIN_MESSAGE,
 				null,
 				opts,
				null);
		return (o != null) ? ((ListItemWrapper)o).wrapped : null;
 	}
 
 	/**
 	 * An event handler for when the select button is pressed.
 	 */
 	private class ButtonClickEvent implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			T o = showSelectionDialog(callback.getSelectionList());
 
 			// string was returned
 			if (o != null)
 			{
 				selectedObject = o;
 			    textField.setText(customToString.toString(selectedObject));
 			    notifyObservers(selectedObject);
 			}
 		}
 	}
 	
 	protected class ListItemWrapper
 	{
 		protected T wrapped;
 		
 		public ListItemWrapper(T wrapped) {
 			this.wrapped = wrapped;
 		}
 		
 		@Override
 		public String toString() {
 			return customToString.toString(wrapped);
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		JFrame frame = new JFrame();
 		GenericSelector<String> selector = new GenericSelector<String>("Strings", new GenericListCallback<String>() {
 			@Override
 			public List<String> getSelectionList() {
 				List<String> ret = new ArrayList<String>();
 				ret.add("fi");
 				ret.add("fo");
 				ret.add("fumb");
 				return ret;
 			}
 		}, new CustomToString<String>() {
 			@Override
 			public String toString(String obj) {
 				return obj.toUpperCase();
 			}
 		});
 		frame.setContentPane(selector);
 		selector.addSelectionChangeListener(new SelectionChangeListener<String>() {
 			@Override
 			public void selectionChanged(String newSelection) {
 				System.out.println("selection changed to:" + newSelection);
 			}
 		});
 		
 		
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 
 }
 
