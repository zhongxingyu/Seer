 //Stephanie Reagle
 //CS 200
 package factory.swing;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import factory.managers.KitManager;
 import factory.managers.LaneManager;
 
 public class KitManPanel extends JPanel implements ActionListener{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5760501420685052853L;
 	//for Parts Assignment Tab
 	JLabel pLabel1 = new JLabel ("Item 1");
 	JLabel pLabel2 = new JLabel ("Item 2");
 	JLabel pLabel3 = new JLabel ("Item 3");
 	JLabel pLabel4 = new JLabel ("Item 4");
 	JLabel pLabel5 = new JLabel ("Item 5");
 	JLabel pLabel6 = new JLabel ("Item 6");
 	JLabel pLabel7 = new JLabel ("Item 7");
 	JLabel pLabel8 = new JLabel ("Item 8");
 
 	String[] items = { "None", "Head", "Hand", "Nose", "Eyes", "Feet", "Ears" };
 
 	JComboBox pItemComboBox1 = new JComboBox(items);
 	JComboBox pItemComboBox2 = new JComboBox(items);
 	JComboBox pItemComboBox3 = new JComboBox(items);
 	JComboBox pItemComboBox4 = new JComboBox(items);
 	JComboBox pItemComboBox5 = new JComboBox(items);
 	JComboBox pItemComboBox6 = new JComboBox(items);
 	JComboBox pItemComboBox7 = new JComboBox(items);
 	JComboBox pItemComboBox8 = new JComboBox(items);
 
 	JTextField pKitName = new JTextField( "Kit Name");
 	JButton pSave = new JButton("Save Kit Configuration");
 
 	//for Production Schedule Tab
 
 	String[] columnNames = {"Que Position", "Kit Type", "Kits Left", "ETA"};
 	Object[][] data = {
 	{"1", "Default", "19", "8:00"},
 	{"2", "Mr. Potato Head", "40", "9:00"},
 	{"3", "Mrs. Potato Head", "60", "10:00"},};
 
 	JTable rTable = new JTable(data, columnNames);
 
 	//for Modify Kit Tab
 
 	String[] kits = { "Default Kit", "Mr. Potato Head", "Mrs. Potato Head"};
 
 	JComboBox mKitComboBox = new JComboBox(kits);
 
 	/*kitComboBox.setSelectedIndex(2);
 	kitComboBox.addActionListener(this);*/
 
 	JLabel mLabel1 = new JLabel ("Item 1");
 	JLabel mLabel2 = new JLabel ("Item 2");
 	JLabel mLabel3 = new JLabel ("Item 3");
 	JLabel mLabel4 = new JLabel ("Item 4");
 	JLabel mLabel5 = new JLabel ("Item 5");
 	JLabel mLabel6 = new JLabel ("Item 6");
 	JLabel mLabel7 = new JLabel ("Item 7");
 	JLabel mLabel8 = new JLabel ("Item 8");
 
 	JComboBox mItemComboBox1 = new JComboBox(items);
 	JComboBox mItemComboBox2 = new JComboBox(items);
 	JComboBox mItemComboBox3 = new JComboBox(items);
 	JComboBox mItemComboBox4 = new JComboBox(items);
 	JComboBox mItemComboBox5 = new JComboBox(items);
 	JComboBox mItemComboBox6 = new JComboBox(items);
 	JComboBox mItemComboBox7 = new JComboBox(items);
 	JComboBox mItemComboBox8 = new JComboBox(items);
 
 	JTextField mKitName = new JTextField( "Default Kit");
 	JButton mSave = new JButton("Save Kit Configuration");
 	JButton mRemove = new JButton("Remove Kit");
 	
 	KitManager kitManager;
 
 	public KitManPanel(){
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 	       GridBagConstraints c = new GridBagConstraints();
 
 		JPanel partsAssignment = new JPanel();
 		partsAssignment.setLayout(new GridBagLayout());
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		partsAssignment.add(pLabel1, c);
 
 		c.gridy = 1;
 		partsAssignment.add(pLabel2, c);
 
 		c.gridy = 2;
 		partsAssignment.add(pLabel3, c);
 
 		c.gridy = 3;
 		partsAssignment.add(pLabel4, c);
 
 		c.gridy = 4;
 		partsAssignment.add(pLabel5, c);
 
 		c.gridy = 5;
 		partsAssignment.add(pLabel6, c);
 
 		c.gridy = 6;
 		partsAssignment.add(pLabel7, c);
 
 		c.gridy = 7;
 		partsAssignment.add(pLabel8, c);
 
 		c.gridy = 9;
 		partsAssignment.add(pKitName, c);
 
 		c.gridy = 10;
 		partsAssignment.add(pSave, c);
 
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 1;
 		c.gridy = 0;
 		partsAssignment.add(pItemComboBox1, c);
 
 		c.gridy = 1;
 		partsAssignment.add(pItemComboBox2, c);
 
 		c.gridy = 2;
 		partsAssignment.add(pItemComboBox3, c);
 
 		c.gridy = 3;
 		partsAssignment.add(pItemComboBox4, c);
 
 		c.gridy = 4;
 		partsAssignment.add(pItemComboBox5, c);
 
 		c.gridy = 5;
 		partsAssignment.add(pItemComboBox6, c);
 
 		c.gridy = 6;
 		partsAssignment.add(pItemComboBox7, c);
 
 		c.gridy = 7;
 		partsAssignment.add(pItemComboBox8, c);
 		
 		JPanel productionSchedule = new JPanel();
 		productionSchedule.setLayout(new GridBagLayout());
 		
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		productionSchedule.add(rTable, c);
 		
 		JPanel modifyKit = new JPanel();
 		modifyKit.setLayout(new GridBagLayout());
 
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		modifyKit.add(mKitComboBox, c);
 
 		c.gridy = 1;
 		modifyKit.add(mLabel1, c);
 
 		c.gridy = 2;
 		modifyKit.add(mLabel2, c);
 
 		c.gridy = 3;
 		modifyKit.add(mLabel3, c);
 
 		c.gridy = 4;
 		modifyKit.add(mLabel4, c);
 
 		c.gridy = 5;
 		modifyKit.add(mLabel5, c);
 
 		c.gridy = 6;
 		modifyKit.add(mLabel6, c);
 
 		c.gridy = 7;
 		modifyKit.add(mLabel7, c);
 
 		c.gridy = 8;
 		modifyKit.add(mLabel8, c);
 
 		c.gridx = 1;
 		c.gridy = 1;
 		modifyKit.add(mItemComboBox1, c);
 
 		c.gridy = 2;
 		modifyKit.add(mItemComboBox2, c);
 
 		c.gridy = 3;
 		modifyKit.add(mItemComboBox3, c);
 
 		c.gridy = 4;
 		modifyKit.add(mItemComboBox4, c);
 
 		c.gridy = 5;
 		modifyKit.add(mItemComboBox5, c);
 
 		c.gridy = 6;
 		modifyKit.add(mItemComboBox6, c);
 
 		c.gridy = 7;
 		modifyKit.add(mItemComboBox7, c);
 
 		c.gridy = 8;
 		modifyKit.add(mItemComboBox8, c);
 
 		c.gridy = 9;
 		modifyKit.add(mKitName, c);
 
 		c.gridy = 10;
 		modifyKit.add(mSave, c);
 
 		c.gridx = 1;
 		c.gridy = 10;
 		modifyKit.add(mRemove, c);
 
 		tabbedPane.addTab("Parts Assignment", partsAssignment);
 		tabbedPane.addTab("Production Schedule", productionSchedule);
 		tabbedPane.addTab("Modify Kit", modifyKit);
 		
 		add(tabbedPane);	
 	}
 	
 	public void setManager(KitManager k){
 		kitManager = k;
 	}
 	 //main method used for testing
 	//do not delete just comment out
 	/*
 	public static void main (String[] args){
 		KitManPanel k = new KitManPanel();
 		k.repaint();
 		k.setVisible(true);
 		k.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		k.setSize(400,450);
 		k.repaint(); 
 	}*/
 
	/*public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == psave) {
 
 		}
 		else if (ae.getSource() == msave) {
 		}
 		else if (ae.getSource() == mremove) {
 
 		}
 		
	}*/
 		
 }
