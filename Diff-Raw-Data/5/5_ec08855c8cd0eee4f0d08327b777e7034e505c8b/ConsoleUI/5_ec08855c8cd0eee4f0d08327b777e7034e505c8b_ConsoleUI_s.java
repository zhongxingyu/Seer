 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.console.swingui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.grinder.console.ConsoleException;
 import net.grinder.console.model.Model;
 import net.grinder.console.model.SampleListener;
 import net.grinder.plugininterface.Test;
 import net.grinder.statistics.Statistics;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class ConsoleUI
 {
     private static ResourceBundle s_resources = null;
 
     private final static Font s_totalTPSFont =
 	new Font("helvetica", Font.ITALIC | Font.BOLD, 48);
 
     private final static NumberFormat s_twoDPFormat =
 	new DecimalFormat("0.00");
 
     private final HashMap m_actionTable;
     private final Model m_model;
     private final JLabel m_intervalLabel;
     private SummaryFrame m_summaryFrame = null;
     private Image m_logoImage = null;
 
     public ConsoleUI(Model model, ActionListener startHandler)
 	throws ConsoleException
     {
 	getResources();
 
 	final Action[] actions = {
 	    new StartGrinderAction(startHandler),
 	    new ResetAction(),
 	    new SummaryAction(),
 	    new ExitAction(),
 	};
 
 	m_actionTable = new HashMap();
 
 	for (int i=0; i<actions.length; i++) {
 	    m_actionTable.put(actions[i].getValue(Action.NAME), actions[i]);
 	}
 
 	m_model = model;
 
 	// Create a scrolled pane of test graphs.
         final JPanel testPanel = new JPanel();
 	testPanel.setLayout(new GridLayout(0, 2, 20, 0));
 
 	final Iterator testIterator = m_model.getTests().iterator();
 
 	while (testIterator.hasNext())
 	{
 	    final Test test = (Test)testIterator.next();
 
 	    final Integer testNumber = test.getTestNumber();
 	    final String description = test.getDescription();
 
 	    String label = "Test " + testNumber;
 
 	    if (description != null) {
 		label = label + " (" + description + ")";
 	    }
 
 	    final LabelledGraph testGraph = new LabelledGraph(label);
 
 	    m_model.addSampleListener(
 		testNumber,
 		new SampleListener() {
 			public void update(double tps, double peakTPS,
 					   Statistics total) {
 			    testGraph.add(tps, peakTPS, total);
 			}
 		    }
 		);
 
 	    testPanel.add(testGraph);
 	}
 
         final JScrollPane scrollPane = new JScrollPane(testPanel);
 
 	final LabelledGraph totalGraph = new LabelledGraph("Total",
 							   Color.darkGray);
 
 	m_model.addTotalSampleListener(
 	    new SampleListener() {
 		    public void update(double tps, double peakTPS,
 				       Statistics total) {
 			totalGraph.add(tps, peakTPS, total);
 		    }
 		}
 	    );
 
 	final JLabel totalTPSLabel = new JLabel("", SwingConstants.CENTER);
 	totalTPSLabel.setForeground(Color.black);
 	totalTPSLabel.setFont(s_totalTPSFont);
 
 	m_model.addTotalSampleListener(
 	    new SampleListener() {
 		    public void update(double tps, double peakTPS,
 				       Statistics total) {
 			totalTPSLabel.setText(
 			    s_twoDPFormat.format(tps) + " TPS");
 		    }
 		}
 	    );
 
 	final JLabel intervalSliderLabel = new JLabel("Sample interval");
 
 	final JSlider intervalSlider =
 	    new JSlider(100, 10000, m_model.getSampleInterval());
 	intervalSlider.setMajorTickSpacing(1000);
 	intervalSlider.setMinorTickSpacing(100);
 	intervalSlider.setPaintTicks(true);
 	intervalSlider.setSnapToTicks(true);
 
 	intervalSlider.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			m_model.setSampleInterval(intervalSlider.getValue());
 			setIntervalLabel();
 		    }
 		}
 	    );
 
 	m_intervalLabel = new JLabel();
 	m_intervalLabel.setPreferredSize(new Dimension(60, 16));
 	setIntervalLabel();
 
 	final JPanel intervalPanel = new JPanel();
 	intervalPanel.add(intervalSliderLabel);
 	intervalPanel.add(intervalSlider);
 	intervalPanel.add(m_intervalLabel);
 
 	final URL logoURL = getResource("logo.image");
 
 	final JPanel totalPanel = new JPanel();
 	totalPanel.setLayout(new GridLayout(0, 1));
 	totalPanel.add(totalGraph);
 	totalPanel.add(intervalPanel);
 	totalPanel.add(totalTPSLabel);
 
 	if (logoURL != null) {
 	    final ImageIcon imageIcon = new ImageIcon(logoURL);
 	    final JLabel logo = new JLabel(imageIcon);
 	    totalPanel.add(logo);
 
 	    m_logoImage = imageIcon.getImage();
 	}
 
 	final JPanel graphPanel = new JPanel();
 
 	graphPanel.setLayout(new BorderLayout());
 	graphPanel.add("West", totalPanel);
 	graphPanel.add(scrollPane);
 
 	// Create a panel to hold the tool bar and the test pane.
         final JPanel toolBarPanel = new JPanel();
 	toolBarPanel.setLayout(new BorderLayout());
 	toolBarPanel.add("North", createToolBar());
 	toolBarPanel.add("Center", graphPanel);
 
 	// Create the frame, containing the a menu and the top level pane.
 	final JFrame frame = new JFrame(getResourceString("title"));
         frame.addWindowListener(new WindowCloseAdapter());
 	final Container topLevelPane= frame.getContentPane();
 	topLevelPane.add("North", createMenuBar());
         topLevelPane.add("Center", toolBarPanel);
 
 	if (m_logoImage != null) {
 	    frame.setIconImage(m_logoImage);
 	}
 
         frame.pack();
         frame.show();
     }
 
     private JMenuBar createMenuBar()
     {
 	final JMenuBar menuBar = new JMenuBar();
 
 	final Iterator menuBarIterator =
 	    tokenise(getResourceString("menubar"));
 	
 	while (menuBarIterator.hasNext()) {
 	    final String menuKey = (String)menuBarIterator.next();
 	    final JMenu menu =
 		new JMenu(getResourceString(menuKey + ".menu.label"));
 
 	    final Iterator menuIterator =
 		tokenise(getResourceString(menuKey + ".menu"));
 
 	    while (menuIterator.hasNext()) {
 		final String menuItemKey = (String)menuIterator.next();
 
 		if ("-".equals(menuItemKey)) {
 		    menu.addSeparator();
 		}
 		else {
 		    final JMenuItem menuItem =
 			new JMenuItem(
 			    getResourceString(menuItemKey + ".label"));
 
 		    final URL imageURL = getResource(menuItemKey + ".image",
 						     false);
 
 		    if (imageURL != null) {
 			menuItem.setIcon(new ImageIcon(imageURL));
 		    }
 
 		    setAction(menuItem, menuItemKey);
 		    menu.add(menuItem);
 		}
 	    }
 
 	    menuBar.add(menu);
 	}
 
 	return menuBar;
     }
 
     private JToolBar createToolBar() 
     {
 	final JToolBar toolBar = new JToolBar();
 	
 	final Iterator toolBarIterator =
 	    tokenise(getResourceString("toolbar"));
 	
 	while (toolBarIterator.hasNext()) {
 	    final String toolKey = (String)toolBarIterator.next();
 
 	    if ("-".equals(toolKey)) {
 		toolBar.addSeparator();
 	    }
 	    else {
 		final URL url = getResource(toolKey + ".image");
 
 		if (url != null) {
 		    final JButton button = new JButton(new ImageIcon(url));
 		
 		    setAction(button, toolKey);
 
 		    final String tipString =
 			getResourceString(toolKey + ".tip", false);
 
 		    if (tipString != null) {
 			button.setToolTipText(tipString);
 		    }
 
 		    toolBar.add(button);
 		}
 	    }
 	}
 
 	return toolBar;
     }
 
     private void setAction(AbstractButton  button, String resourceKey)
     {
 	final String actionString = getResourceString(resourceKey + ".action");
 	final Action action = (Action)m_actionTable.get(actionString);
 
 	if (action != null) {
 	    action.addPropertyChangeListener(
 		new ActionChangedListener(button));
 
 	    button.addActionListener(action);
 	    button.setActionCommand(actionString);
 	    button.setEnabled(action.isEnabled());
 	}
 	else {
 	    System.err.println("Action '" + actionString + "' not found");
 	    button.setEnabled(false);
 	}
     }
 
     private void setIntervalLabel()
     {
 	m_intervalLabel.setText(m_model.getSampleInterval() + " ms");
     }
 
     private class ActionChangedListener implements PropertyChangeListener
     {
         final AbstractButton m_button;
         
         ActionChangedListener(AbstractButton button)
 	{
             super();
             m_button = button;
         }
 
         public void propertyChange(PropertyChangeEvent e) 
 	{
             final String propertyName = e.getPropertyName();
 
             if (e.getPropertyName().equals(Action.NAME))
 	    {
                 final String text = (String)e.getNewValue();
                 m_button.setText(text);
             }
 	    else if (propertyName.equals("enabled"))
 	    {
                 final Boolean enabledState = (Boolean)e.getNewValue();
                 m_button.setEnabled(enabledState.booleanValue());
             }
         }
     }
 
     private static final class WindowCloseAdapter extends WindowAdapter
     {
 	public void windowClosing(WindowEvent e)
 	{
 	    System.exit(0);
 	}
     }
 
     private class ExitAction extends AbstractAction
     {
 	ExitAction()
 	{
 	    super("exit");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    System.exit(0);
 	}
     }
 
     private class ResetAction extends AbstractAction
     {
 	ResetAction()
 	{
 	    super("reset");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_model.reset();
 	    LabelledGraph.resetPeak();
	    m_summaryFrame.hide();
 	}
     }
 
     private class StartGrinderAction extends AbstractAction
     {
 	private final ActionListener m_delegateAction;
 
 	StartGrinderAction(ActionListener delegateAction)
 	{
 	    super("start");
 	    m_delegateAction = delegateAction;
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_delegateAction.actionPerformed(e);
 	}
     }
 
     private class SummaryAction extends AbstractAction
     {
 	SummaryAction()
 	{
 	    super("summary");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    if (m_summaryFrame == null) {
 		synchronized(ConsoleUI.this) {
 		    if (m_summaryFrame == null) {
 			m_summaryFrame =
 			    new SummaryFrame(m_model,
 					     getResourceString(
 						 "summaryTitle"));
 
 			if (m_logoImage != null) {
 			    m_summaryFrame.setIconImage(m_logoImage);
 			}
 		    }
 		}
 	    }
 
 	    m_summaryFrame.displaySummary();
 	}
     }
 
     static void getResources()
 	throws ConsoleException
     {
 	try {
 	    s_resources = ResourceBundle.getBundle(
 		"net.grinder.console.swingui.resources.Console");
 	}
 	catch (MissingResourceException e) {
 	    throw new ConsoleException("Resource bundle not found");
 	}
     }
 
     private static Iterator tokenise(String string)
     {
 	final LinkedList list = new LinkedList();
 
 	final StringTokenizer t = new StringTokenizer(string);
 
 	while (t.hasMoreTokens()) {
 	    list.add(t.nextToken());
 	}
 
 	return list.iterator();
     }
 
     private String getResourceString(String key)
     {
 	return getResourceString(key, true);
     }
 
     private String getResourceString(String key, boolean warnIfMissing)
     {
 	try {
 	    return s_resources.getString(key);
 	}
 	catch (MissingResourceException e) {
 	    if (warnIfMissing) {
 		System.err.println(
 		    "Warning - resource " + key + " not specified");
 	    }
 	    
 	    return "";
 	}
     }
 
     private URL getResource(String key)
     {
 	return getResource(key, true);
     }
 
     private URL getResource(String key, boolean warnIfMissing)
     {
 	final String name = getResourceString(key, true);
 
 	if (name.length() == 0) {
 	    return null;
 	}
 	
 	final URL url = this.getClass().getResource("resources/" + name);
 
 	if (warnIfMissing && url == null) {
 	    System.err.println("Warning - could not load resource " + name);
 	}
 
 	return url;
     }
 }
