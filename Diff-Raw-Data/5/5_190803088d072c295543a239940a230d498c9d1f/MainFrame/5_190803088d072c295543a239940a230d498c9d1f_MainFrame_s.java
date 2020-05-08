 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.util.Arrays;
 import java.util.Vector;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import main.IntervalledSpotPlayer;
 import main.ScheduledSpotPlayer;
 import main.Util;
 import main.Prefs;
 import main.SpotContainer;
 import main.SpotEntry;
 import main.SpotMachine;
 
 public class MainFrame extends JFrame implements ChangeListener, ActionListener, ItemListener {
 	private static final long serialVersionUID = 6219825567861104713L;
 	
 	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
 	
 	private RecordDialogue recordDialogue = null;
 	
 	public MainFrame(String title) {
 		super(title);
 		//setResizable(false);
 		setContentPane(new JPanel());
 		getContentPane().setLayout(new BorderLayout());
 		
 		getContentPane().add(createMenuBar(), BorderLayout.PAGE_START);
 		JPanel p = new JPanel();
 		getContentPane().add(p, BorderLayout.CENTER);
 		
 		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 		p.add(createCountdownPanel());
 		p.add(createUpperMainPanel());
 		p.add(new JSeparator(SwingConstants.HORIZONTAL));
 		p.add(createLowerMainPanel());
 	}
 	
 	private JTextField countdownTextField;
 	private JLabel nextSpotLabel;
 	
 	private JPanel createCountdownPanel() {
 		JPanel panel = new JPanel();
 		panel.add(new JLabel(Util.get().string("main-timenextspot-label") + ":"));
 		countdownTextField = new JTextField(
 				Util.get().millisToMinsSecsString(Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT))
 				);
 		countdownTextField.setEditable(false);
 		countdownTextField.setColumns(5);
 		panel.add(countdownTextField);
 		nextSpotLabel = new JLabel();
 		panel.add(nextSpotLabel);
 		return panel;
 	}
 	
 	private void setNextSpotLabel(int index, String name) {
 		nextSpotLabel.setText("(" + Util.get().string("main-timenextspot-numberabbrev-label") + " " + (index+1) + ", \"" + name + "\")");
 	}
 	
 	public void setNextSpotLabel(int index, SpotEntry spot) {
 		if (spot != null)
 			setNextSpotLabel(index, spot.getName());
 		else
 			setNextSpotLabel(0, "-");
 	}
 	
 	public void setCountDownFieldValue(long millis) {
 		countdownTextField.setText(Util.get().millisToMinsSecsString(millis));
 	}
 	
 	private JButton playButton;
 	private JButton pauseButton;
 	
 	private JPanel createUpperMainPanel() {
 		JPanel panel = new JPanel();
 		
 		playButton = new JButton(Util.get().string("main-play-button"), Util.get().createImageIcon("../resources/Play24.gif"));
 		playButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 	    playButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		playButton.addActionListener(this);
 		playButton.setActionCommand("play");
 		panel.add(playButton);
 		
 		pauseButton = new JButton(Util.get().string("main-pause-button"), Util.get().createImageIcon("../resources/Pause24.gif"));
 		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		pauseButton.addActionListener(this);
 		pauseButton.setActionCommand("pause");
 		pauseButton.setEnabled(false);
 		panel.add(pauseButton);
 		
 		JButton previousButton = new JButton(Util.get().string("main-previous-button"), Util.get().createImageIcon("../resources/StepBack24.gif"));
 		previousButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 	    previousButton.setHorizontalTextPosition(AbstractButton.CENTER);
 	    previousButton.addActionListener(this);
 		previousButton.setActionCommand("previous");
 		panel.add(previousButton);
 		
 		JButton nextButton = new JButton(Util.get().string("main-next-button"), Util.get().createImageIcon("../resources/StepForward24.gif"));
 		nextButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 	    nextButton.setHorizontalTextPosition(AbstractButton.CENTER);
 	    nextButton.addActionListener(this);
 		nextButton.setActionCommand("next");
 		panel.add(nextButton);
 		
 		return panel;
 	}
 	
 	public void setGUIPaused(boolean paused) {
 		playButton.setEnabled(paused);
 		pauseButton.setEnabled(!paused);
 		recordNewButton.setEnabled(paused);
 		removeFromAvailableButton.setEnabled(paused);
 	}
 	
 	private JPanel createLowerMainPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
 		panel.add(createAvailableSpotsPanel());
 		
 		JPanel activeSpotsPanel = new JPanel();
 		activeSpotsPanel.setLayout(new BoxLayout(activeSpotsPanel, BoxLayout.PAGE_AXIS));
 		panel.add(activeSpotsPanel);
 		
 		JPanel intervalledSpotsPanel = new JPanel();
 		intervalledSpotsPanel.setLayout(new BoxLayout(intervalledSpotsPanel, BoxLayout.LINE_AXIS));
 		intervalledSpotsPanel.add(createIntervalledSpotTransferPanel());
 		intervalledSpotsPanel.add(createIntervalledSpotsPanel());
 		activeSpotsPanel.add(intervalledSpotsPanel);
 		
 		JPanel scheduledSpotsPanel = new JPanel();
 		scheduledSpotsPanel.setLayout(new BoxLayout(scheduledSpotsPanel, BoxLayout.LINE_AXIS));
 		scheduledSpotsPanel.add(createScheduledSpotsTransferPanel());
 		scheduledSpotsPanel.add(createScheduledSpotsPanel());
 		activeSpotsPanel.add(scheduledSpotsPanel);
 		
 		return panel;
 	}
 	
 	private SpotList availableSpotList;
 	private JButton recordNewButton;
 	private JButton removeFromAvailableButton;
 	
 	private JPanel createAvailableSpotsPanel() {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createTitledBorder(Util.get().string("main-availablespots-label")));
 		
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		
 		SpotContainer availableSpots = SpotMachine.getAvailableSpots();
 		availableSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_AVAILABLE));
 		availableSpotList.getModel().replaceData(availableSpots.getDataCopy());
 		panel.add(availableSpotList.getContainingScrollPane());
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(2, 0));
 		
 		recordNewButton = new JButton(Util.get().string("main-recordnew-button"));
 		recordNewButton.addActionListener(this);
 		recordNewButton.setActionCommand("record");
 		buttonPanel.add(recordNewButton);
 		
 		removeFromAvailableButton = new JButton(Util.get().string("main-delete-button"));
 		removeFromAvailableButton.addActionListener(this);
 		removeFromAvailableButton.setActionCommand("removefromavailable");
 		buttonPanel.add(removeFromAvailableButton);
 		
 		JButton importButton = new JButton(""); //Util.get().string("main-import-button")
 		importButton.setEnabled(false);
 		importButton.addActionListener(this);
 		importButton.setActionCommand("importspot");
 		buttonPanel.add(importButton);
 		
 		JButton renameButton = new JButton(Util.get().string("main-rename-button"));
 		renameButton.addActionListener(this);
 		renameButton.setActionCommand("rename");
 		buttonPanel.add(renameButton);
 		
 		panel.add(buttonPanel);
 		
 		return panel;
 	}
 	
 	private JPanel createIntervalledSpotTransferPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		
 		JButton copyToIntervalledButton = new JButton(Util.get().createImageIcon("../resources/Forward24.gif"));
 		copyToIntervalledButton.setToolTipText(Util.get().string("main-copytointervalled-tooltip"));
 		copyToIntervalledButton.addActionListener(this);
 		copyToIntervalledButton.setActionCommand("copytointervalled");
 		panel.add(copyToIntervalledButton);
 	    
 		JButton removeFromIntervalledButton = new JButton(Util.get().createImageIcon("../resources/Back24.gif"));
 		removeFromIntervalledButton.setToolTipText(Util.get().string("main-removefromintervalled-tooltip"));
 		removeFromIntervalledButton.addActionListener(this);
 		removeFromIntervalledButton.setActionCommand("removefromintervalled");
 		panel.add(removeFromIntervalledButton);
 		
 		return panel;
 	}
 
 	private JSpinner minBetweenSpotsSpinner;
 	
 	private SpotList intervalledSpotList;
 	private JCheckBox repeatAllCheckBox;
 	
 	private JPanel createIntervalledSpotsPanel() {
 		JPanel mainPanel = new JPanel();
 		mainPanel.setBorder(BorderFactory.createTitledBorder(Util.get().string("main-intervalledspots-label")));
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		
 		while(SpotMachine.getIntervalledSpotPlayer() == null) {
 			Util.get().out("GUI; IntervalledSpotPlayer not initialized yet. Waiting a bit and then retrying.", Util.VERBOSITY_WARNING);
 			Util.get().threadSleep(500);
 		}
 		
 		IntervalledSpotPlayer intervalSpots = SpotMachine.getIntervalledSpotPlayer();
 		intervalledSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_INTERVALLED));
 		intervalledSpotList.getModel().replaceData(intervalSpots.getDataCopy());
 		intervalledSpotList.setNextSpot(intervalSpots.getNextSpotToPlayIndex());
 		if (intervalSpots.getNextSpotToPlayIndex() != -1 && intervalSpots.getNextSpotToPlay() != null)
 			setNextSpotLabel(
 					intervalSpots.getNextSpotToPlayIndex(), 
 					intervalSpots.getNextSpotToPlay().getName());
 		else
 			setNextSpotLabel(0, "-");
 		panel.add(intervalledSpotList.getContainingScrollPane());
 		repeatAllCheckBox = new JCheckBox(Util.get().string("main-repeatall-checkbox"));
 		repeatAllCheckBox.setToolTipText(Util.get().string("main-repeatall-tooltip"));
 		repeatAllCheckBox.setSelected(Prefs.prefs.getBoolean(Prefs.REPEAT_ALL, Prefs.REPEAT_ALL_DEFAULT));
 		repeatAllCheckBox.addItemListener(this);
 		panel.add(repeatAllCheckBox);
 		
 		JPanel spinnerPanel = new JPanel();
 		//spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.LINE_AXIS));
 		minBetweenSpotsSpinner = new JSpinner(new SpinnerNumberModel(
 				(double)Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT) / 1000 / 60,
 				0, 1000, 1));
 		minBetweenSpotsSpinner.addChangeListener(this);
 		spinnerPanel.add(minBetweenSpotsSpinner);
 		spinnerPanel.add(new JLabel(Util.get().string("main-minsbetweenspots-label")));
 		panel.add(spinnerPanel);
 		
 		mainPanel.add(panel);
 		mainPanel.add(createChangeOrderIntervalledPanel());
 		
 		return mainPanel;
 	}
 	
 	private JPanel createScheduledSpotsTransferPanel() {
 	    JPanel panel = new JPanel();
 	    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 	    
 	    JButton copyToScheduledButton = new JButton(Util.get().createImageIcon("../resources/Forward24.gif"));
	    copyToScheduledButton.setToolTipText("main-copytoscheduled-tooltip");
 	    copyToScheduledButton.addActionListener(this);
 	    copyToScheduledButton.setActionCommand("copytoscheduled");
 	    panel.add(copyToScheduledButton);
 	    
 	    JButton removeFromScheduledButton = new JButton(Util.get().createImageIcon("../resources/Back24.gif"));
	    removeFromScheduledButton.setToolTipText("main-removefromscheduled-tooltip");
 	    removeFromScheduledButton.addActionListener(this);
 	    removeFromScheduledButton.setActionCommand("removefromscheduled");
 	    panel.add(removeFromScheduledButton);
 	    
 	    return panel;
 	}
 	
 	private SpotList scheduledSpotList;
 	
 	private JPanel createScheduledSpotsPanel() {
 	    JPanel panel = new JPanel();
 	    panel.setBorder(BorderFactory.createTitledBorder(Util.get().string("main-scheduledspots-label")));
 	    
 	    while(SpotMachine.getScheduledSpotPlayer() == null) {
 	        Util.get().out("GUI: ScheduledSpotPlayer not initialized yet. Waiting a bit and then retrying.", Util.VERBOSITY_WARNING);
 	        Util.get().threadSleep(500);
 	    }
 	    
 	    ScheduledSpotPlayer scheduledSpots = SpotMachine.getScheduledSpotPlayer();
 	    scheduledSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_SCHEDULED));
 	    Vector<SpotEntry> v = scheduledSpots.getDataCopy();
 	    scheduledSpotList.getModel().replaceData(v);
 	    panel.add(scheduledSpotList.getContainingScrollPane());
 	    
 	    panel.add(createEditScheduledPanel());
 	    
 	    return panel;
 	}
 	
 	public SpotList getAvailableSpotList() {
 		return availableSpotList;
 	}
 	
 	public SpotList getIntervalledSpotList() {
 		return intervalledSpotList;
 	}
 	
 	public SpotList getScheduledSpotList() {
 	    return scheduledSpotList;
 	}
 	
 	public RecordDialogue getRecordDialogue() {
 		return recordDialogue;
 	}
 	
 	public void setRecordDialogueNull() {
 		recordDialogue = null;
 	}
 	
 	private JPanel createChangeOrderIntervalledPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		
 		JButton upButton = new JButton(Util.get().string("main-moveup-button"), Util.get().createImageIcon("../resources/Up24.gif"));
 		upButton.addActionListener(this);
 		upButton.setActionCommand("intervalledmoveup");
 		panel.add(upButton);
 		
 		JButton downButton = new JButton(Util.get().string("main-movedown-button"), Util.get().createImageIcon("../resources/Down24.gif"));
 		downButton.addActionListener(this);
 		downButton.setActionCommand("intervalledmovedown");
 		panel.add(downButton);
 		
 		return panel;
 	}
 	
 	private JPanel createEditScheduledPanel() {
 	    JPanel panel = new JPanel();
 	    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 	    
 	    JButton editButton = new JButton(Util.get().string("main-editscheduled-button"), Util.get().createImageIcon("../resources/Edit24.gif"));
 	    editButton.addActionListener(this);
 	    editButton.setActionCommand("editscheduled");
 	    panel.add(editButton);
 	    
 	    return panel;
 	}
 	
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu menu = new JMenu(Util.get().string("main-menu-file"));
 		menu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(menu);
 		
 		JMenuItem menuItem = new JMenuItem(Util.get().string("main-menu-file-prefs"));
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("prefs");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem(Util.get().string("main-menu-file-about") + " " + SpotMachine.PROGRAM_NAME);
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("aboutprogram");
 		menu.add(menuItem);
 		
 		return menuBar;
 	}
 
 	
 	public void stateChanged(ChangeEvent e) {
 		Util.get().out("ChangeListener: State changed!", Util.VERBOSITY_DEBUG_INFO);
 		JComponent source = (JComponent)e.getSource();
 		if (source == minBetweenSpotsSpinner) {
 			double readValue = (Double)((JSpinner)source).getValue();
 			SpotMachine.getIntervalledSpotPlayer().setMillisBetweenSpots((int)Math.round(readValue * 60 * 1000));
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		Util.get().out("ActionListener: Action performed! " + e.getActionCommand(), Util.VERBOSITY_DEBUG_INFO);
 		String action = e.getActionCommand();
 		if (action.equals("intervalledmoveup") || action.equals("intervalledmovedown")) {
 			int oldPos = intervalledSpotList.getSelectedRow();
 			if (oldPos != -1 && intervalledSpotList.getModel().getRowCount() > 0) {
 				int newPos;
 				if (action.equals("intervalledmoveup")) {
 					newPos = oldPos - 1;
 				} else {
 					newPos = oldPos + 1;
 				}
 				if (newPos < 0 || newPos > intervalledSpotList.getRowCount() - 1) {
 					Util.get().out("Trying to move an intervalled spot off-range. Ignored.", Util.VERBOSITY_WARNING);
 					return;
 				}
 				intervalledSpotList.swapRows(newPos, oldPos);
 				intervalledSpotList.getSelectionModel().setSelectionInterval(newPos, newPos);
 				SpotMachine.getIntervalledSpotPlayer().swapSpots(newPos, oldPos);
 				setNextSpotLabel(SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlayIndex(), SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlay().getName());
 			}
 		} else if (action.equals("play")) {
 			SpotMachine.getIntervalledSpotPlayer().setPaused(false);
 			setGUIPaused(false);
 		} else if (action.equals("pause")) {
 			SpotMachine.getIntervalledSpotPlayer().setPaused(true);
 			setGUIPaused(true);
 		} else if (action.equals("previous")) {
 			int prev = SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlayOneBackward();
 			intervalledSpotList.setNextSpot(prev);
 			setNextSpotLabel(prev, SpotMachine.getIntervalledSpotPlayer().getSpotAt(prev).getName());
 		} else if (action.equals("next")) {
 			int next = SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlayOneForward();
 			intervalledSpotList.setNextSpot(next);
 			setNextSpotLabel(next, SpotMachine.getIntervalledSpotPlayer().getSpotAt(next).getName());
 		} else if (action.equals("copytointervalled")) {
 			int selectedAvailable = availableSpotList.getSelectedRow();
 			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
 				int selectedIntervalled = intervalledSpotList.getSelectedRow();
 				SpotEntry source = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
 				SpotMachine.getIntervalledSpotPlayer().addToEnd(source);
 				intervalledSpotList.getModel().addToEnd(source);
 				if (SpotMachine.getIntervalledSpotPlayer().numberOfSpots() == 1) { // i.e., if this is the only spot
 					Util.get().out("Added spot to empty list. Setting next spot to that spot.", Util.VERBOSITY_DEBUG_INFO);
 					SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlay(0);
 					setNextSpotLabel(0, source.getName());
 					intervalledSpotList.setNextSpot(0);
 				}
 				intervalledSpotList.getSelectionModel().setSelectionInterval(selectedIntervalled, selectedIntervalled);
 			}
 		} else if (action.equals("removefromintervalled")) {
 			int selectedIntervalled = intervalledSpotList.getSelectedRow();
 			if (selectedIntervalled != -1  && intervalledSpotList.getModel().getRowCount() > 0) {
 				SpotMachine.getIntervalledSpotPlayer().remove(selectedIntervalled);
 				intervalledSpotList.remove(selectedIntervalled);
 				int newSelection = (selectedIntervalled-1 >= 0) ? selectedIntervalled-1 : 0;
 				intervalledSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
 			}
 		} else if (action.equals("removefromavailable")) {
 			int selectedAvailable = availableSpotList.getSelectedRow();
 			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
 				Object[] options = {Util.get().string("remove-yes"), Util.get().string("remove-no")};
 				int userChoise = JOptionPane.showOptionDialog(
 						this,
 						Util.get().string("remove-text"),
 						Util.get().string("remove-headline"),
 						JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.WARNING_MESSAGE,
 						null, // icon
 						options,
 						options[1]
 				);
 				
 				if (userChoise == 0) {
 					SpotEntry removedAvailableSpot = SpotMachine.getAvailableSpots().remove(selectedAvailable);
 					int[] removedIntervalSpots = SpotMachine.getIntervalledSpotPlayer().removeAllSpotsContaining(removedAvailableSpot);
 					Util.get().out("RemovedIntervalSpots: " + Arrays.toString(removedIntervalSpots), Util.VERBOSITY_DEBUG_INFO);
 					
 					Util.get().deleteFile(removedAvailableSpot.getFile());
 					
 					availableSpotList.remove(selectedAvailable);
 					intervalledSpotList.removeAll(removedIntervalSpots);
 					
 					int newSelection = (selectedAvailable-1 >= 0) ? selectedAvailable-1 : 0;
 					availableSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
 				}
 			}
 		} else if (action.equals("record")) {
 			(recordDialogue = new RecordDialogue()).setVisible(true);
 			this.setEnabled(false);
 		} else if (action.equals("importspot")) {
 			// TODO
 		} else if (action.equals("rename")) {
 			int selectedAvailable = availableSpotList.getSelectedRow();
 			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
 				SpotEntry spot = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
 				
 				String newName = null;
 				do {
 					newName = (String)JOptionPane.showInputDialog(
 							this,
 							Util.get().string("rename-text"),
 							Util.get().string("rename-headline"),
 							JOptionPane.PLAIN_MESSAGE,
 							null, // icon
 							null, // options, null gives text field
 							spot.getName()
 					);
 					if (newName != null)
 						newName = newName.trim();
 				} while (newName != null && newName.length() == 0);
 				
 				if (newName != null) {
 					SpotMachine.getAvailableSpots().renameSpot(selectedAvailable, newName);
 					availableSpotList.getModel().rename(selectedAvailable, newName);
 				}
 			}
         } else if (action.equals("copytoscheduled")) {
             int selectedAvailable = availableSpotList.getSelectedRow();
             if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
                 final SpotEntry source = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
                 final SchedulePropertiesDialogue spd = new SchedulePropertiesDialogue();
                 spd.setVisible(true);
                 this.setEnabled(false);
                 
                 /**
                  * The following thread will wait for the user to click ok
                  * in the properties dialogue, and then do the appropriate
                  * changes, followed by reenabling the main window.
                  * We can't just wait in the current thread, as it would halt
                  * the GUI, making the impression that the program has stalled
                  */
                 new Thread(new Runnable() {
                     public void run() {
                         while (spd.isVisible()) {
                             Util.get().threadSleep(200);
                         }
                         if (spd.isOkClicked()) {
                             SpotEntry addedSpot = SpotMachine.getScheduledSpotPlayer().addToEnd(source, spd.getSchedule());
                             SpotMachine.getMainFrame().getScheduledSpotList().getModel().addToEnd(addedSpot);
                         }
                         SpotMachine.getMainFrame().setEnabled(true);
                     }
                 }).start();
             }
         } else if (action.equals("removefromscheduled")) {
             int selectedScheduledSpot = scheduledSpotList.getSelectedRow();
             if (selectedScheduledSpot != -1  && scheduledSpotList.getModel().getRowCount() > 0) {
                 SpotMachine.getScheduledSpotPlayer().remove(selectedScheduledSpot);
                 scheduledSpotList.remove(selectedScheduledSpot);
                 int newSelection = (selectedScheduledSpot-1 >= 0) ? selectedScheduledSpot-1 : 0;
                 scheduledSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
             }
         } else if (action.equals("editscheduled")) {
             final int selectedScheduledSpot = scheduledSpotList.getSelectedRow();
             if (selectedScheduledSpot != -1 && scheduledSpotList.getModel().getRowCount() > 0) {
                 SpotEntry selectedSpot = SpotMachine.getScheduledSpotPlayer().getSpotAt(selectedScheduledSpot);
                 final SchedulePropertiesDialogue spd = new SchedulePropertiesDialogue(selectedSpot.getSchedule());
     		    spd.setVisible(true);
                 this.setEnabled(false);
                 
                 /**
                  * The following thread will wait for the user to click ok
                  * in the properties dialogue, and then do the appropriate
                  * changes, followed by reenabling the main window.
                  * We can't just wait in the current thread, as it would halt
                  * the GUI, making the impression that the program has stalled
                  */
                 new Thread(new Runnable() {
                     public void run() {
                         while (spd.isVisible()) {
                             Util.get().threadSleep(200);
                         }
                         if (spd.isOkClicked()) {
                             SpotMachine.getScheduledSpotPlayer().setNewScheduleForSpot(selectedScheduledSpot, spd.getSchedule());
                             SpotMachine.getMainFrame().getScheduledSpotList().getModel().setPlayAt(selectedScheduledSpot, spd.getSchedule());
                         }
                         SpotMachine.getMainFrame().setEnabled(true);
                     }
                 }).start();
             }
 		} else if (action.equals("aboutprogram")) {
 			JOptionPane.showMessageDialog(this,
 				    SpotMachine.PROGRAM_NAME + " " + Util.get().string("about-version") + " " + SpotMachine.PROGRAM_VERSION + "\n" +
 				    Util.get().string("about-author") + "\n" +
 				    (Util.get().string("about-translator").trim().equals("") ? "" : Util.get().string("about-translator") + "\n") +
 				    "http://pryds.eu/spotmachine\n" +
 				    "\n" +
 				    Util.get().wordWrap(Util.get().string("about-text"), 70),
 				    Util.get().string("about-headline") + " " + SpotMachine.PROGRAM_NAME,
 				    JOptionPane.INFORMATION_MESSAGE);
 		} else if (action.equals("prefs")) {
 			PreferencesDialogue preferencesDialogue = new PreferencesDialogue();
 			preferencesDialogue.setVisible(true);
 			this.setEnabled(false);
 		}
 	}
 
 	public void itemStateChanged(ItemEvent e) {
 		Util.get().out("ItemListener: Item state changed!", Util.VERBOSITY_DEBUG_INFO);
 		Object source = e.getItemSelectable();
 		if (source == repeatAllCheckBox) {
 			SpotMachine.getIntervalledSpotPlayer().setRepeatAll(repeatAllCheckBox.isSelected());
 		}
 	}
 }
