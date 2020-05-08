 package me.ael.jwake;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 /**
  * jwake is a small GUI that stores machines and their MAC addresses, and can
  * wake them over the network by sending Wake-on-LAN packets.
  * 
  * @version 0.7.0
  * @author Alex Lindeman <alex@ael.me>
  */
 public class JWakeWindow extends JFrame implements ActionListener, MouseListener
 {
 	private static final long serialVersionUID = -251105713894579029L;
 	
 	private static final String VERSION = "0.7.0";
 	private static final Font ADDRESS_FONT = new Font("Lucida Console", Font.PLAIN, 16);
 	
 	private static final String SEPARATOR = ",";
 	private static final String STORAGE = "machines.txt";
 	
 	private List<Machine> machines;
 
 	private JFrame frame;
 	private JPanel layout;
 	
 	private JLabel status;
 	private JLabel about;
 
 	private JTable machineTable;
 	private JButton add;
 	private JButton remove;
 
 	private JTextField wakeup;
 	private JButton wake;
 	
 	private JLabel link;
 
 	public static void main(String[] args)
 	{
 		new JWakeWindow();
 	}
 
 	public JWakeWindow()
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e) { }
 
 		// main frame
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		frame.setMinimumSize(new Dimension(320, 240));
 		frame.setSize(new Dimension(320, 360));
 		frame.setLocationRelativeTo(null);
 		frame.setTitle("jwake " + VERSION);
 		
 		layout = new JPanel(new BorderLayout());
 		layout.setBorder(new EmptyBorder(10, 10, 10, 10));
 		
 		// lower chunk
 		JPanel lower = new JPanel(new BorderLayout());
 		lower.setBorder(new EmptyBorder(10, 0, 0, 0));
 		
 		// wake panel
 		JPanel top = new JPanel();
 		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
 		
 		wakeup = new JTextField();
 		wakeup.setFont(ADDRESS_FONT);
 		top.add(wakeup);
 		
 		top.add(Box.createHorizontalStrut(5));
 		
 		wake = new JButton("Wake");
 		wake.addActionListener(this);
 		frame.getRootPane().setDefaultButton(wake);
 		top.add(wake);
 		
 		// status bar
 		JPanel bottom = new JPanel();
 		bottom.setBorder(new EmptyBorder(5, 0, 0, 0));
 		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
 		
 		Font statusFont = new Font(layout.getFont().getFamily(), Font.PLAIN, 9);
 		
 		status = new JLabel("Idle");
 		status.setFont(statusFont);
 		status.setForeground(Color.GRAY);
 		bottom.add(status);
 		
 		bottom.add(Box.createHorizontalGlue());
 		
 		about = new JLabel("ael.me/jwake");
 		about.addMouseListener(this);
 		about.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		about.setFont(statusFont);
 		about.setForeground(Color.GRAY);
 		bottom.add(about);
 		
 		lower.add(top, BorderLayout.NORTH);
 		lower.add(bottom, BorderLayout.SOUTH);
 		
 		// upper chunk
 		JPanel upper = new JPanel(new BorderLayout());
 		
 		// machine table
 		loadMachines();
 		
 		machineTable = new JTable();
 		machineTable.setFillsViewportHeight(true);
 		machineTable.setShowGrid(false);
 		machineTable.setRowHeight(machineTable.getFont().getSize() + 8);
 		machineTable.setModel(new MachineTableModel(machines));
 		machineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  
 		machineTable.addMouseListener(this);
 		
 		JScrollPane scroll = new JScrollPane(machineTable);
 		upper.add(scroll, BorderLayout.CENTER);
 		
 		// machine management buttons
 		JPanel buttons = new JPanel();
 		buttons.setBorder(new EmptyBorder(5, 0, 0, 0));
 		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
 		
		ImageIcon addIcon = new ImageIcon(getClass().getResource("res/add.png"), "Add");
 		add = new JButton(addIcon);
 		add.setBorder(new EmptyBorder(4, 6, 4, 6));
 		add.addActionListener(this);
 		buttons.add(add);
 		
		ImageIcon removeIcon = new ImageIcon(getClass().getResource("res/remove.png"), "Remove");
 		remove = new JButton(removeIcon);
 		remove.setBorder(new EmptyBorder(4, 6, 4, 6));
 		remove.setEnabled(false);
 		remove.addActionListener(this);
 		buttons.add(remove);
 		
 		upper.add(buttons, BorderLayout.SOUTH);
 		
 		layout.add(upper, BorderLayout.CENTER);
 		layout.add(lower, BorderLayout.SOUTH);
 		
 		frame.add(layout);
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Loads the machine list from file.
 	 *
 	 * <p>
 	 * The machine list is in CSV format as
 	 * <code>Machine.name</code>,<code>Machine.address</code>. The columns are
 	 * separated by the value of <code>SEPARATOR</code>.
 	 * </p>
 	 * 
 	 * @param filename File to read from
 	 */
 	private void loadMachines()
 	{
 		machines = new LinkedList<Machine>();
 		
 		try
 		{
 			BufferedReader loader = new BufferedReader(new FileReader(STORAGE));
 			
 			String line;
 			while ((line = loader.readLine()) != null)
 			{
 				Machine m = new Machine(line.split(SEPARATOR));
 				machines.add(m);
 			}
 			
 			status.setText("Loaded " + machines.size() + (machines.size() == 1 ? " entry" : " entries") + " from " + STORAGE);
 			loader.close();
 		}
 		catch (Exception e)
 		{
 			status.setText("No machines loaded");
 		}
 	}
 	
 	/**
 	 * Writes the machine list to file.
 	 */
 	private void writeMachines()
 	{
 		try
 		{
 			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(STORAGE)));
 			
 			for (Machine m : machines)
 			{
 				StringBuffer line = new StringBuffer();
 				
 				line.append(m.name);
 				line.append(SEPARATOR);
 				line.append(m.address);
 				
 				w.write(line.toString());
 				w.newLine();
 			}
 			
 			w.flush();
 			w.close();
 		}
 		catch (IOException e)
 		{
 			JOptionPane.showMessageDialog(frame,  "Couldn't write machines list to file.", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Handles button events from the main interface.
 	 */
 	public void actionPerformed(ActionEvent click)
 	{
 		if (click.getSource() == wake)
 		{
 			String target = wakeup.getText();
 			if (target.length() > 0)
 			{
 				status.setText("Sending wakeup signal...");
 				try
 				{
 					WakeOnLan wol = new WakeOnLan(wakeup.getText());
 					wol.send();
 					status.setText("Wakeup signal sent successfully");
 				}
 				catch (IllegalArgumentException e)
 				{
 					status.setText(e.getMessage());
 				}
 				catch (Exception e)
 				{
 					status.setText(e.getMessage());
 				}
 			}
 			else
 			{
 				status.setText("Enter a MAC address");
 			}
 		}
 		
 		if (click.getSource() == add)
 		{
 			JTextField name = new JTextField();
 			JTextField address = new JTextField(wakeup.getText());
 			address.setFont(ADDRESS_FONT);
 			
 			JComponent inputs[] = {
 					new JLabel("Name"),
 					name,
 					new JLabel("MAC Address"),
 					address
 			};
 			
 			String[] options = { "Add", "Cancel" };
 			
 			int result = JOptionPane.showOptionDialog(frame, inputs, "Add a new machine", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
 			if (result == JOptionPane.OK_OPTION)
 			{
 				String newName = name.getText();
 				String newAddress = address.getText();
 				
 				newName = newName.replaceAll("[^A-Za-z0-9.-_ ]", "");
 				newAddress = newAddress.replaceAll("[^A-Fa-f0-9-:]", "");
 				
 				if (newName.length() > 0 && newAddress.length() > 0)
 				{
 					try
 					{
 						Machine m = new Machine(newName, newAddress);
 						
 						machines.add(m);
 						writeMachines();
 						status.setText("Added '" + m.name + "'");
 						
 						machineTable.clearSelection();
 						remove.setEnabled(false);
 						machineTable.repaint();
 					}
 					catch (IllegalArgumentException e)
 					{
 						JOptionPane.showMessageDialog(frame, e.getMessage(), "Problem adding machine", JOptionPane.WARNING_MESSAGE);
 					}
 				}
 			}
 		}
 		
 		if (click.getSource() == remove)
 		{
 			if (machineTable.getSelectedRowCount() > 0)
 			{
 				int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this machine?", "Delete a machine", JOptionPane.YES_NO_OPTION);
 				if (result == JOptionPane.YES_OPTION)
 				{
 					int row = machineTable.getSelectedRow();
 					String removing = machines.get(row).name;
 					
 					machines.remove(row);
 					writeMachines();
 					status.setText("Removed '" + removing + "'");
 					
 					machineTable.clearSelection();
 					remove.setEnabled(false);
 					machineTable.repaint();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles mouse clicks on the main interface.
 	 */
 	public void mouseClicked(MouseEvent click)
 	{
 		if (click.getSource() == about)
 		{
			ImageIcon icon = new ImageIcon(getClass().getResource("res/icon-48.png"));
 			
 			JLabel hero = new JLabel("jwake " + VERSION, SwingConstants.CENTER);
 			hero.setFont(new Font(layout.getFont().getFamily(), Font.BOLD, 18));
 			
 			link = new JLabel("<html><a href='#'>ael.me/jwake</a></html>", SwingConstants.CENTER);
 			link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			link.addMouseListener(this);
 			
 			JComponent[] panel = {
 					new JLabel(icon, SwingConstants.CENTER),
 					hero,
 					new JLabel("Created by Alex Lindeman", SwingConstants.CENTER),
 					link
 			};
 
 			JOptionPane.showMessageDialog(frame, panel, "About jwake", JOptionPane.PLAIN_MESSAGE);
 		}
 		
 		if (click.getSource() == link)
 		{
 			if (Desktop.isDesktopSupported())
 			{
 				try
 				{
 					URI location = new URI("http://ael.me/jwake/");
 					Desktop.getDesktop().browse(location);
 				}
 				catch (Exception e) { }
 			}
 		}
 			
 		if (click.getSource() == machineTable)
 		{
 			if (machineTable.getSelectedRowCount() > 0)
 			{
 				remove.setEnabled(true);
 
 				if (click.getClickCount() >= 2)
 				{
 					Machine m = machines.get(machineTable.getSelectedRow());
 					wakeup.setText(m.address);
 				}
 			}
 			else
 			{
 				remove.setEnabled(false);
 			}
 		}
 	}
 	
     public void mouseEntered(MouseEvent e) { }
     public void mouseExited(MouseEvent e) { }
     public void mousePressed(MouseEvent e) { }
     public void mouseReleased(MouseEvent e) { }
 }
