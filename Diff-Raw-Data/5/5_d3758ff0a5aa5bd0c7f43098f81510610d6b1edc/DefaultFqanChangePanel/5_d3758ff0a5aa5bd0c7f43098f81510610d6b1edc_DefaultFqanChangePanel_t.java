 package grisu.frontend.view.swing;
 
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import grisu.control.ServiceInterface;
 import grisu.model.FqanEvent;
 import grisu.model.GrisuRegistryManager;
 import grisu.model.UserEnvironmentManager;
 import org.apache.commons.lang.StringUtils;
 import org.bushe.swing.event.EventBus;
 import org.bushe.swing.event.EventSubscriber;
 
 import javax.swing.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 public class DefaultFqanChangePanel extends JPanel implements
 EventSubscriber<FqanEvent> {
 
 	private final DefaultComboBoxModel voModel = new DefaultComboBoxModel();
 	private ServiceInterface si = null;
 	private final JComboBox comboBox;
 
 	private Thread fillThread = null;
 	private boolean externalChange = false;
 
 	/**
 	 * Create the panel.
 	 */
 	public DefaultFqanChangePanel() {
 		setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("147px"),
				ColumnSpec.decode("330px"), },
 				new RowSpec[] { RowSpec.decode("40px"), }));
 
 		final JLabel lblGroup = new JLabel("Submit for group:");
 		add(lblGroup, "1, 1, left, center");
 
 		comboBox = new JComboBox(voModel);
 		comboBox.setEditable(false);
		comboBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
 		comboBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 
 				if (externalChange) {
 					return;
 				}
 
 				if (si == null) {
 					return;
 				}
 
 				if (ItemEvent.SELECTED == e.getStateChange()) {
 
 					final String newVO = (String) voModel.getSelectedItem();
 					GrisuRegistryManager.getDefault(si)
 					.getUserEnvironmentManager().setCurrentFqan(newVO);
 
 				}
 
 			}
 		});
 		add(comboBox, "2, 1, fill, center");
 		EventBus.subscribe(FqanEvent.class, this);
 	}
 
 	public void fillComboBox() {
 
 		if (si == null) {
 			return;
 		}
 
 		if ((fillThread != null) && fillThread.isAlive()) {
 			return;
 		}
 
 		fillThread = new Thread() {
 			@Override
 			public void run() {
 
 				final UserEnvironmentManager uem = GrisuRegistryManager
 						.getDefault(si).getUserEnvironmentManager();
 				String old = (String) voModel.getSelectedItem();
 				if (StringUtils.isBlank(old)) {
 					old = uem.getCurrentFqan();
 				}
 
 				externalChange = true;
 
 				final String[] allVOs = uem.getAllAvailableJobFqans();
 
 				voModel.removeAllElements();
 
 				for (final String vo : allVOs) {
 					voModel.addElement(vo);
 				}
 
 				externalChange = false;
 
 				if (StringUtils.isNotBlank(old)
 						&& (voModel.getIndexOf(old) >= 0)) {
 					uem.setCurrentFqan(old);
 				} else {
 					old = (String) voModel.getElementAt(0);
 					if (StringUtils.isNotBlank(old)) {
 						uem.setCurrentFqan(old);
 					}
 				}
 
 			}
 		};
 		fillThread.start();
 
 	}
 
 	public void lockUI(final boolean lock) {
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				comboBox.setEnabled(!lock);
 			}
 		});
 	}
 
 	public synchronized void onEvent(final FqanEvent arg0) {
 
 		if (FqanEvent.DEFAULT_FQAN_CHANGED == arg0.getEvent_type()) {
 			SwingUtilities.invokeLater(new Thread() {
 				@Override
 				public void run() {
 					externalChange = true;
 					voModel.setSelectedItem(arg0.getFqan());
 					externalChange = false;
 				}
 			});
 		}
 
 	}
 
 	public void setServiceInterface(ServiceInterface si)
 			throws InterruptedException {
 		this.si = si;
 		fillComboBox();
 	}
 
 }
