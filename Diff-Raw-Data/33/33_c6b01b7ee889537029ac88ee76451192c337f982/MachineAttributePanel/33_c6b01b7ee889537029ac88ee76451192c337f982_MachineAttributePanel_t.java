 package AttributePanel;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.event.TableModelEvent;
 
 import All.Constants;
 import All.DrawingPanel;
 import Data.Machine;
 
 
 public class MachineAttributePanel extends NodeAttributesPanel{
 	public JButton setData;
 	public Machine selectedNode;
 	public DrawingPanel drawingPanel;
 	private JLabel averageSwitchingTimeLabel, averageNodalDelayLabel, totalSwitchesLabel, averageSwitchingTime, averageNodalDelay,
 	totalSwitches;
 	public MachineAttributePanel(DrawingPanel dp){
 		this.drawingPanel = dp;
 		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Machine Attributes Panel"));
 
 		setData = new JButton("Set Data");
 		setData.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(selectedNode != null){
 					selectedNode.name = name.getText();
 
 					selectedNode.ETH_IP = ETH_IP.getText();
 					selectedNode.ETH_HW = ETH_HW.getText();
 					selectedNode.WLS_IP = parseSeparatedString(WLS_IP.getText());
 //					selectedNode.WLS_HW = parseSeparatedString(WLS_HW.getText());
 
 					selectedNode.channels = parseChannels(channels.getText());
 
 					selectedNode.mobilityOption = mobilityOption.getSelectedItem().toString();
 					selectedNode.topologyOption = topologyOption.getSelectedItem().toString();
 					
 					drawingPanel.repaint();
 
 					System.out.println("Successfully updated the node:\n" + selectedNode);
 				}
 			}
 		});
 
		averageSwitchingTimeLabel = new JLabel("Switching time");
		averageNodalDelayLabel = new JLabel("Nodal delay");
		totalSwitchesLabel = new JLabel("No. of switches");
 
 		averageSwitchingTime = new JLabel();
 		averageNodalDelay = new JLabel();
 		totalSwitches = new JLabel();
 
 		JPanel all = new JPanel();
 
 		GroupLayout layout = new GroupLayout(all);
 		layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
 		all.setLayout(layout);
 		add(all);
 
 		Component gap = Box.createRigidArea(new Dimension(Constants.COMPONENTS_GAP, Constants.COMPONENTS_GAP));
 
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				    	   .addComponent(nameLabel)
 				      	   .addComponent(ETH_IPLabel)
 				      	   .addComponent(ETH_HWLabel)
 				      	   .addComponent(WLS_IPLabel)
 				      	   .addComponent(WLS_HWLabel)
 				      	   .addComponent(channelsLabel)
 				      	   .addComponent(mobilityOptionLabel)
 				      	   .addComponent(topologyOptionLabel)
 				      	   .addComponent(averageNodalDelayLabel)
 				      	   .addComponent(averageSwitchingTimeLabel)
 				      	   .addComponent(totalSwitchesLabel))
 			          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 		        		   .addComponent(name)
 				      	   .addComponent(ETH_IP)
 				      	   .addComponent(ETH_HW)
 				      	   .addComponent(WLS_IP)
 				      	   .addComponent(ps)
 				      	   .addComponent(channels)
 				      	   .addComponent(mobilityOption)
 				      	   .addComponent(topologyOption)
 				      	   .addComponent(averageNodalDelay)
 				      	   .addComponent(averageSwitchingTime)
 				      	   .addComponent(totalSwitches)
 				      	   .addComponent(setData))
 
 		);
 
 		layout.setVerticalGroup(
 		   layout.createSequentialGroup()
 		   	  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(nameLabel)
 		           .addComponent(name))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(ETH_IPLabel)
 		           .addComponent(ETH_IP))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(ETH_HWLabel)
 		           .addComponent(ETH_HW))
    	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(WLS_IPLabel)
 		           .addComponent(WLS_IP))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(WLS_HWLabel)
 		           .addComponent(ps))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(channelsLabel)
 		           .addComponent(channels))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(mobilityOptionLabel)
 		           .addComponent(mobilityOption))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(topologyOptionLabel)
 		           .addComponent(topologyOption))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(averageNodalDelayLabel)
 		           .addComponent(averageNodalDelay))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(averageSwitchingTimeLabel)
 		           .addComponent(averageSwitchingTime))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(totalSwitchesLabel)
 		           .addComponent(totalSwitches))
 		      .addComponent(setData)
 		);
 	}
 
 	public void setInfo(Machine node){
 		selectedNode = node;
 
 		//Fill the required information.
 		name.setText(node.name);
 		ETH_IP.setText(node.ETH_IP);
 		ETH_HW.setText(node.ETH_HW);
 		WLS_IP.setText(node.getWLS_IP());
 		WirelessInterfacesTable t = (WirelessInterfacesTable)WLS_HW_Table.getModel();
 		t.current.clear();
 		for(int i = 0; i < node.WLS_HW.size(); i++){
 			t.current.add(new WirelessTableRowEntry(node.WLS_Name.get(i), node.WLS_HW.get(i)));
 			WLS_HW_Table.getTableHeader().repaint();
 			WLS_HW_Table.tableChanged(new TableModelEvent(t));
 			WLS_HW_Table.repaint();
 		}
 
 		channels.setText(node.getChannels());
 
 		mobilityOption.setSelectedItem(node.mobilityOption);
 		topologyOption.setSelectedItem(node.topologyOption);
 		
 		averageNodalDelay.setText(node.averageNodalDelay + " ms");
 		averageSwitchingTime.setText(node.averageSwitchingTime + " ms");
 		totalSwitches.setText(node.totalSwitches + " switches");
 		
 	}
 
 }
 
