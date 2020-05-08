 /*
  * Created on 10.01.2005
  *
  */
 package hoplugins.conv;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.ScrollPane;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import plugins.ISpieler;
 
 /**
  * @author Thorsten Dietz
  *  
  */
 public class TxtExpDialog extends JDialog {
 	private int width, height;
 	private JPanel optionPanel;
 	private JPanel southPanel;
 	private JPanel middlePanel;
 	private JCheckBox chk_columnNames;
 	private JList list;
 	private TxtExpListener listener;
 	private JTextField txt_limiter;
 	private JComboBox filterBox;
 
 	protected TxtExpDialog(JFrame frame) {
 		super(frame,RSC.PROP_PLAYERS+" => "+RSC.PROP_FILE);
 		width = (int) GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getMaximumWindowBounds().getWidth();
 		height = (int) GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getMaximumWindowBounds().getHeight();
 		setLocation((width - 400) / 2, (height - 320) / 2);
 		setSize(400, 320);
 		listener = new TxtExpListener();
 		initialize();
 	}
 
 	private void initialize() {
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(getMiddlePanel(), BorderLayout.CENTER);
 		getContentPane().add(getSouthPanel(), BorderLayout.SOUTH);
 
 	}
 
 	private JPanel getSouthPanel() {
 		if (southPanel == null) {
 
 			southPanel = RSC.MINIMODEL.getGUI().createImagePanel();
 			((FlowLayout) southPanel.getLayout())
 					.setAlignment(FlowLayout.RIGHT);
 
 			JButton okButton = new JButton(RSC.PROP_ADD);
 			okButton.setActionCommand(RSC.PROP_ADD);
 			okButton.addActionListener(listener);
 
 			JButton cancelButton = new JButton(RSC.PROP_CANCEL);
 			cancelButton.setActionCommand(RSC.ACT_CANCEL);
 			cancelButton.addActionListener(listener);
 
 			southPanel.add(okButton);
 			southPanel.add(cancelButton);
 
 		}
 		return southPanel;
 	}
 
 	private JPanel getMiddlePanel() {
 		if (middlePanel == null) {
 
 			middlePanel = RSC.MINIMODEL.getGUI().createImagePanel();
 			middlePanel.setLayout(new BorderLayout());
 			ScrollPane scrolli = new ScrollPane();
 			scrolli.add(getList());
 			//middlePanel.add(new JLabel("Select Columns:"),BorderLayout.NORTH);
 			middlePanel.add(scrolli, BorderLayout.CENTER);
 			middlePanel.add(getOptionPanel(), BorderLayout.WEST);
 
 		}
 		return middlePanel;
 	}
 
 	private JPanel getOptionPanel() {
 		if (optionPanel == null) {
 			optionPanel = RSC.MINIMODEL.getGUI().createImagePanel();
 			optionPanel.setPreferredSize(new Dimension(200, 100));
 			optionPanel.setLayout(new GridBagLayout());
 
 			GridBagConstraints c = new GridBagConstraints();
 
 			c.anchor = GridBagConstraints.NORTHWEST;
 			c.insets = new Insets(15, 5, 5, 5);
 			c.fill = GridBagConstraints.NONE;
 
 			add(optionPanel, new JLabel("Separator:"), 0, 0, c);
 			add(optionPanel, getLimiterField(), 1, 0, c);
 
 			add(optionPanel, new JLabel("Columnnames:"), 0, 1, c);
 			add(optionPanel, getColumnNamesCheck(), 1, 1, c);
 			add(optionPanel, new JLabel("Group:"), 0, 2, c);
 			add(optionPanel, getFilterBox(), 1, 2, c);
 
 		}
 		return optionPanel;
 	}
 
 	private JComboBox getFilterBox() {
 		if (filterBox == null) {
 			ImageIcon [] smilies = new ImageIcon[6];
 			for (int i = 0; i < smilies.length; i++) {
 				smilies[i] = new javax.swing.ImageIcon(RSC.MINIMODEL.getHelper().makeColorTransparent(
 				        RSC.MINIMODEL.getHelper().loadImage("gui/bilder/smilies/"+RSC.TEAM[i]), 210, 210, 185, 255, 255, 255));
 			}
 			filterBox = new JComboBox(smilies);
 		}
 		return filterBox;
 	}
 
 	private JCheckBox getColumnNamesCheck() {
 		if (chk_columnNames == null) {
 			chk_columnNames = new JCheckBox();
 		}
 		return chk_columnNames;
 	}
 
 	protected JList getList() {
 		if (list == null) {
 			list = new JList(RSC.playerColumns);
 		}
 		return list;
 	}
 
 	public void writeFile(File file) throws Exception {
 		String limiter = getLimiterField().getText();
 		Object[] selected = getList().getSelectedValues();
 		Vector players = RSC.MINIMODEL.getAllSpieler();
 		//FileOutputStream out = new FileOutputStream(file, true);
 		//OutputStreamWriter fileWriter = new OutputStreamWriter(out,"UTF8");
 		FileWriter fileWriter = new FileWriter(file);
 		if (getColumnNamesCheck().isSelected()) {
 			for (int i = 0; i < selected.length; i++) {
 				fileWriter.write(((CColumn) selected[i]).getDisplay());
 				if (i < selected.length - 1)
 					fileWriter.write(limiter);
 			}
 			fileWriter.write("\n");
 		}
 
 		for (Iterator iter = players.iterator(); iter.hasNext();) {
 			ISpieler player = (ISpieler) iter.next();
 			if (getFilterBox().getSelectedIndex() > 0) {
 				String group = RSC.TEAM[getFilterBox().getSelectedIndex()];
 				if (group.equals(player.getTeamInfoSmilie())) {
 					for (int i = 0; i < selected.length; i++) {
 						fileWriter.write(((CColumn) selected[i]).getPlayerValue(player));
 						if (i < selected.length - 1)
 							fileWriter.write(limiter);
 					}
 
 					fileWriter.write("\n");
 
 				}
 			}
 		}
 		fileWriter.flush();
 		fileWriter.close();
 	}
 
 	
 
 	
 
 	private JTextField getLimiterField() {
 		if (txt_limiter == null) {
 			txt_limiter = new JTextField();
 			txt_limiter.setPreferredSize(new Dimension(100, 23));
 			txt_limiter.setHorizontalAlignment(SwingConstants.CENTER);
 			txt_limiter.setText(";");
 		}
 		return txt_limiter;
 	}
 
 	private void add(JPanel parent, Component comp, int x, int y,
 			GridBagConstraints c) {
 		c.gridx = x;
 		c.gridy = y;
 		parent.add(comp, c);
 	}
 }
