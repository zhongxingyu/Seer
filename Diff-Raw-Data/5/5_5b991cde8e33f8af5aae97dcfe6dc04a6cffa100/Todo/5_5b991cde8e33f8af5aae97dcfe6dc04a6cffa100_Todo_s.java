 package net.clonecomputers.lab.todo;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.text.DateFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class Todo {
 	
 	private JButton removeButton;
 	private JList alarmList;
 	private DefaultListModel listModel = new DefaultListModel();
 	private AlarmSettingsPanel alarmSettings;
 	private Set<AlarmListItem> alarms = new HashSet<AlarmListItem>();
 	private JFrame window;
 	
 	private int lastSelectedIndex;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		final Todo todo = new Todo();
 		loadAlarms(todo);
 		EventQueue.invokeLater(new Runnable() {
 
 			//@Override
 			public void run() {
 				todo.initGui();
 			}
 			
 		});
 	}
 	
 	private static void loadAlarms(Todo todo) {
 		//TODO write code here!
 		todo.alarms.add(new AlarmListItem("Test", new ScheduledAlarm(2013, 5, 1, 12, 0))); //just a test thing
 	}
 	
 	private void initGui() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace(); //ho hum we can't use system look and feel
 		}
 		window = new JFrame("clone todo");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JPanel contentPane = new JPanel(new BorderLayout(30, 0));
 		contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
 		contentPane.add(getAlarmListPanel(), BorderLayout.WEST);
 		alarmSettings = new AlarmSettingsPanel(this);
 		contentPane.add(alarmSettings, BorderLayout.EAST);
 		window.setContentPane(contentPane);
 		window.setResizable(false);
 		window.pack();
 		window.setVisible(true);
 	}
 	
 	private JPanel getAlarmListPanel() {
 		JPanel listPanel = new JPanel(new BorderLayout(0, 3));
 		listPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		for(AlarmListItem a : alarms) {
 			listModel.addElement(a);
 		}
 		alarmList = new JList(listModel);
 		alarmList.setFixedCellHeight(80);
 		alarmList.setFixedCellWidth(350);
 		alarmList.setCellRenderer(new AlarmListCellRenderer());
 		alarmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		alarmList.setDragEnabled(false);
 		alarmList.clearSelection();
 		alarmList.addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				boolean isSelectionEmpty = alarmList.isSelectionEmpty();
 				if(isSelectionEmpty) {
 					if(!alarmSettings.hidePanel()) {
 						alarmList.setSelectedIndex(lastSelectedIndex);
 					}
 				} else {
 					if(!alarmSettings.show((AlarmListItem) alarmList.getSelectedValue())) {
 						alarmList.setSelectedIndex(lastSelectedIndex);
 					}
 				}
 				removeButton.setEnabled(!isSelectionEmpty);
 				lastSelectedIndex = alarmList.getSelectedIndex();
 			}
 		});
 		lastSelectedIndex = alarmList.getSelectedIndex();
 		listPanel.add(alarmList, BorderLayout.NORTH);
 		listPanel.add(getAddRemoveButtons(), BorderLayout.SOUTH);
 		listPanel.addComponentListener(new ComponentAdapter() {
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				Component c = e.getComponent();
 				if(c.getWidth() != 354) c.setSize(354, c.getHeight());
 			}
 			
 		});
 		return listPanel;
 	}
 	
 	private JPanel getAddRemoveButtons() {
 		JPanel buttonPanel = new JPanel();
 		JButton addButton = new JButton("+");
 		removeButton = new JButton("-");
 		addButton.addActionListener(new ActionListener() {
 
 			//@Override
 			public void actionPerformed(ActionEvent e) {
 				openNewAlarm();
 			}
 			
 		});
 		removeButton.addActionListener(new ActionListener() {
 			
 			//@Override
 			public void actionPerformed(ActionEvent e) {
 				removeSelectedAlarm();
 			}
 			
 		});
 		addButton.setPreferredSize(new Dimension(20, 20));
 		removeButton.setPreferredSize(new Dimension(20, 20));
 		buttonPanel.add(addButton);
 		buttonPanel.add(removeButton);
 		removeButton.setEnabled(!alarmList.isSelectionEmpty());
 		return buttonPanel;
 	}
 	
 	public void clearSelection() {
 		alarmList.clearSelection();
 	}
 	
 	public void pack() {
 		window.pack();
 	}
 	
 	private void openNewAlarm() {
		if(alarmSettings.show(new AlarmListItem("", new ScheduledAlarm()))) {
			alarmList.clearSelection();
		}
 	}
 	
 	private void removeSelectedAlarm() {
 		if(alarmSettings.hidePanel()) {
 			alarms.remove(alarmList.getSelectedValue());
 			listModel.remove(alarmList.getSelectedIndex());
 			window.pack();
 		}
 		//TODO make this act like you are actually going to delete an alarm (not that you are simply closing the alarm)
 	}
 	
 	public void saveAlarm(AlarmListItem alarm) {
 		if(alarms.add(alarm)) {
 			listModel.addElement(alarm);
 			alarmList.setSelectedValue(alarm, true);
 		}
 		alarmList.updateUI();
 		window.pack();
 		
 	}
 	
 	public class AlarmListCellRenderer extends Component implements ListCellRenderer {
 		
 		private static final long serialVersionUID = -323811720627355823L;
 		
 		private Image img;
 		private Image selImg;
 		
 		private boolean isSelected;
 		
 		private AlarmListItem cellValue = null;
 		
 		public AlarmListCellRenderer() {
 			img = Toolkit.getDefaultToolkit().createImage("resources/Louis/ListItemBackground.jpg");
 			selImg = Toolkit.getDefaultToolkit().createImage("resources/Louis/SelectedListItemBackground.jpg");
 		}
 		
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			this.isSelected = isSelected;
 			if(value instanceof AlarmListItem) {
 				cellValue = (AlarmListItem) value;
 			}
 			return this;
 		}
 		
 		@Override
 		public void paint(Graphics g) {
 			if(isSelected) {
 				g.drawImage(selImg, 0, 0, null);
 			} else {
 				g.drawImage(img, 0, 0, null);
 			}
 			if(cellValue != null) {
 				Font normalFont = g.getFont();
 				Font bigFont = new Font(normalFont.getName(), Font.PLAIN, 15);
 				g.setFont(bigFont);
 				g.setColor(Color.BLACK);
 				g.drawString(cellValue.getName(), 10, 20);
 				g.setFont(normalFont);
 				g.drawString("Alarm: " + cellValue.getAlarm(), 18, 37);
 				g.drawString("Next Alarm Time: " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(cellValue.getAlarm().getSoonestDate()), 18, 53);
 			} else {
 				throw new NullPointerException("Cell Value " + cellValue + " is null");
 			}
 		}
 		
 	}
 
 }
