 package grisu.frontend.view.swing.jobcreation.widgets;
 
 import grisu.control.ServiceInterface;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.border.TitledBorder;
 
 import org.vpac.historyRepeater.HistoryManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class Memory extends AbstractWidget {
 
	private static final String[] DEFAULT_MEMORY = new String[] { "0", "1",
 		"2", "4", "8", "16", "32", "64" };
 
 	private final DefaultComboBoxModel memoryModel = new DefaultComboBoxModel(
 			DEFAULT_MEMORY);
 
 	private JComboBox comboBox;
 	private ServiceInterface si;
 	private HistoryManager hm;
 	private final String historyKey = null;
 
 	/**
 	 * Create the panel.
 	 */
 	public Memory() {
 		super();
 		setBorder(new TitledBorder(null, "Memory", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getComboBox(), "2, 2, fill, default");
 
 	}
 
 	private JComboBox getComboBox() {
 		if (comboBox == null) {
 			comboBox = new JComboBox(memoryModel);
 			comboBox.addItemListener(new ItemListener() {
 
 				public void itemStateChanged(ItemEvent itemevent) {
 					if (itemevent.getStateChange() == ItemEvent.SELECTED) {
 						getPropertyChangeSupport().firePropertyChange("memory",
 								-1, getValue());
 
 					}
 				}
 			});
 		}
 		return comboBox;
 	}
 
 	public Long getMemory() {
 		final String integerString = (String) getComboBox().getSelectedItem();
 		try {
 			final Long result = Long.parseLong(integerString) * 1024L;
 			return result;
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 			return -1L;
 		}
 	}
 
 	@Override
 	public String getValue() {
 		return getMemory().toString();
 	}
 
 	@Override
 	public void lockUI(boolean lock) {
 
 		getComboBox().setEnabled(!lock);
 
 	}
 
 	private void setMemory(Long memory) {
 		memory = memory / 1024L;
 		if (memoryModel.getIndexOf(memory.toString()) < 0) {
 			memoryModel.addElement(memory.toString());
 		}
 		memoryModel.setSelectedItem(memory.toString());
 	}
 
 	@Override
 	public void setValue(String value) {
 
 		try {
 			final Long temp = Long.parseLong(value);
 			setMemory(temp);
 		} catch (final Exception e) {
 			myLogger.debug("Can't set memory: " + e.getLocalizedMessage());
 		}
 
 	}
 }
