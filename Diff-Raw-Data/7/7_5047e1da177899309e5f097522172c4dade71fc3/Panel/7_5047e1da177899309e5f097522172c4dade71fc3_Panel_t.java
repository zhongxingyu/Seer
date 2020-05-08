 package org.bentokit.krispi.ui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 import org.bentokit.krispi.Plugin;
 import org.bentokit.krispi.PluginManager;
 
 public class Panel extends JScrollPane {
 	/**
 	 * Generated serialVersionUID - TH 20101111
 	 */
 	private static final long serialVersionUID = -1143444618485619750L;
 
 	public Panel(PluginManager manager) {
 		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		JPanel viewport = new JPanel();
 		viewport.setLayout(new BorderLayout());
 		viewport.add(new PluginListPanel(manager),BorderLayout.NORTH);
 		viewport.add(new JPanel(),BorderLayout.CENTER);
 		this.setViewportView(viewport);
 	}
 	
 	class PluginListPanel extends JPanel implements ActionListener {
 		/**
 		 * Generated serialVersionUID - TH 20101115
 		 */
 		private static final long serialVersionUID = -7886109644252996547L;
 		
 		private PluginManager manager;
 		private Map<JCheckBox,Plugin> checkboxMap;
 
 		public PluginListPanel(PluginManager manager) {
 			this.manager = manager;
 
 			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//			this.setPreferredSize(new Dimension(300,200));
 
 			checkboxMap = new HashMap<JCheckBox,Plugin>();
 			this.removeAll();
 			if (this.manager.getAvailablePlugins().size() <= 0) {
 				JPanel pluginPanel = new JPanel();
 				JPanel textPanel = new JPanel();
 
 				textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
 				textPanel.add(new JLabel("No plugins available."),BorderLayout.CENTER);
 
 				pluginPanel.setLayout(new BorderLayout());
 				pluginPanel.add(textPanel,BorderLayout.CENTER);
 				
				this.add(pluginPanel);
 			} else {
 				for (Plugin plugin : this.manager.getAvailablePlugins()) {
 					JPanel pluginPanel = new JPanel();
 					JPanel textPanel = new JPanel();
 					JPanel checkboxPanel = new JPanel();
 					JCheckBox activeCheckbox = new JCheckBox();
 					activeCheckbox.addActionListener(this);
 					if (plugin.isActive()) activeCheckbox.setSelected(true);
 					
 					textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
 					textPanel.add(new JLabel("<html><b>"+plugin.getName()+"</b></html>"),BorderLayout.CENTER);
 					textPanel.add(new JLabel("<html><i>"+plugin.getDescription()+"</i></html>"),BorderLayout.CENTER);
 					
 					checkboxPanel.add(activeCheckbox);
 					
 					pluginPanel.setLayout(new BorderLayout());
 					pluginPanel.setBorder(new EmptyBorder(5,5,5,5));
 					pluginPanel.add(new JLabel(plugin.getIcon()),BorderLayout.WEST);
 					pluginPanel.add(textPanel,BorderLayout.CENTER);
 					pluginPanel.add(checkboxPanel,BorderLayout.EAST);
 					
 					checkboxMap.put(activeCheckbox,plugin);
 					this.add(pluginPanel);
 				}
 			}
			this.validate();
 		}
 		
 		public void actionPerformed(ActionEvent event) {
 			boolean ok = this.manager.setActive(checkboxMap.get(event.getSource()),((JCheckBox)event.getSource()).isSelected());
 			if (!ok) System.err.println("No such plugin.");
 		}
 	}
 }
