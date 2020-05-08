 package serialclient;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 
 public class SerialView extends JPanel{
 	private JTextArea out, in;
 	private SerialControl control;
 
 	public SerialView(SerialControl control){
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints constraints = new GridBagConstraints();
 		setLayout(gridbag);
 
 		this.control = control;
		control.addListener(new SerialListener(){
 			public void bytesReceived(final byte[] bytes, final int length){
 				SwingUtilities.invokeLater(new Runnable(){
 					public void run(){
 						out.append(Utilities.bytesToHexString(bytes, length));
 					}
 				});
 			}
		});
 
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = 1;
 		constraints.weighty = 0;
 		constraints.gridx = 0;
 		constraints.gridy = GridBagConstraints.RELATIVE;
 
 		// sub controls
 		JPanel controls = new JPanel();
 		controls.setLayout(new FlowLayout(FlowLayout.LEFT));
 		gridbag.setConstraints(controls, constraints);
 		add(controls);
 
 		JButton send = new JButton("send");
 		controls.add(send);
 		send.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				try{
 					SerialView.this.control.send(Utilities.hexStringToBytes(in.getText()));
 				}
 				catch(SerialException ex){
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		constraints.weighty = 1;
 
 		// input/output panels
 		JSplitPane io = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
 		io.setResizeWeight(0.5);
 		gridbag.setConstraints(io, constraints);
 		add(io);
 
 		out = new JTextArea();
 		out.setEnabled(false);
 		out.setLineWrap(true);
 		io.setRightComponent(new JScrollPane(out));
 
 		in = new JTextArea();
 		in.setLineWrap(true);
 		io.setLeftComponent(new JScrollPane(in));
 	}
 }
