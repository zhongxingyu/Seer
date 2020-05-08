 package evopaint.gui;
 
 
 import evopaint.Configuration;
 import evopaint.EvoPaint;
 import evopaint.Paint;
 import evopaint.commands.*;
 import evopaint.gui.listeners.SelectionListenerFactory;
 import evopaint.gui.rulesetmanager.JRuleSetManager;
 import evopaint.pixel.rulebased.RuleSet;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 
 public class MainFrame extends JFrame {
     private Container contentPane;
     private JPanel mainPanel;
     private MenuBar menuBar;
     private Showcase showcase;
     private JOptionsPanel jOptionsPanel;
     private ToolBox toolBox;
     private SelectionToolBox selectionToolBox;
     private PaintOptionsPanel paintPanel;
     private JPanel leftPanel;
     private JRuleSetManager jRuleSetManager;
     private Configuration configuration;
 
     private Class activeTool = null;
     private ResumeCommand resumeCommand;
     private PauseCommand pauseCommand;
     private ZoomCommand zoomOutCommand;
     private ZoomCommand zoomInCommand;
 
     private int runLevelBeforeRuleSetManager;
 
     public Configuration getConfiguration() {
         return configuration;
     }
 
     public MainFrame(final Configuration configuration, EvoPaint evopaint) {
         this.configuration = configuration;
         this.contentPane = getContentPane();
 
         resumeCommand = new ResumeCommand(configuration);
         pauseCommand = new PauseCommand(configuration);
         zoomInCommand = new ZoomInCommand(showcase);
         zoomOutCommand = new ZoomOutCommand(showcase);
 
         CommandFactory commandFactory = new CommandFactory(configuration);
 
         try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception e) {
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception ee) {
                 // mu!
             }
         }
         ToolTipManager.sharedInstance().setInitialDelay(500);
         ToolTipManager.sharedInstance().setReshowDelay(300);
 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         getContentPane().setLayout(new CardLayout());
 
         this.mainPanel = new JPanel();
         mainPanel.setLayout(new BorderLayout(0, 0));
         add(mainPanel, "main");
         this.jRuleSetManager = new JRuleSetManager(configuration,
                 new RuleSetManagerOKListener(), new RuleSetManagerCancelListener());
         jRuleSetManager.setVisible(false);
         add(jRuleSetManager, "rule manager");
 
         this.jOptionsPanel = new JOptionsPanel(configuration);
         this.showcase = new Showcase(configuration, this, configuration.world, evopaint.getPerception(), commandFactory);
         this.menuBar = new MenuBar(configuration, evopaint, new SelectionListenerFactory(showcase), showcase);
         setJMenuBar(menuBar);
 
         addKeyListener(new MainFrameKeyListener());
 
 
         // some hand crafted GUI stuff for the panel on the left
         leftPanel = new JPanel();
         leftPanel.setLayout(new GridBagLayout());
         mainPanel.add(leftPanel, BorderLayout.WEST);
 
         JEvolutionPlayerPanel evolutionPlayer = new JEvolutionPlayerPanel(configuration);
         GridBagConstraints constraintsLeftPanel = new GridBagConstraints();
         constraintsLeftPanel.anchor = GridBagConstraints.CENTER;
         leftPanel.add(evolutionPlayer, constraintsLeftPanel);
 
         JPanel wrapperPanelLeft = new JPanel();
         wrapperPanelLeft.setLayout(new GridBagLayout());
         wrapperPanelLeft.setBackground(new Color(0xF2F2F5));
         wrapperPanelLeft.setBorder(new LineBorder(Color.GRAY));
         GridBagConstraints constraintsWrapper = new GridBagConstraints();
         constraintsWrapper.anchor = GridBagConstraints.NORTHWEST;
         constraintsWrapper.insets = new Insets(3, 0, 3, 0);
 
         constraintsLeftPanel.insets = new Insets(10, 10, 10, 10);
         constraintsLeftPanel.gridy = 1;
         leftPanel.add(wrapperPanelLeft, constraintsLeftPanel);
 
         toolBox = new ToolBox(this);
         constraintsWrapper.gridy = 0;
         constraintsWrapper.fill = GridBagConstraints.HORIZONTAL;
         wrapperPanelLeft.add(toolBox, constraintsWrapper);
         
         JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.HORIZONTAL;
         wrapperPanelLeft.add(separator, constraintsWrapper);
         
         selectionToolBox = new SelectionToolBox(showcase);
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.NONE;
         wrapperPanelLeft.add(selectionToolBox, constraintsWrapper);
 
         separator = new JSeparator(JSeparator.HORIZONTAL);
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.HORIZONTAL;
         wrapperPanelLeft.add(separator, constraintsWrapper);
 
         paintPanel = new PaintOptionsPanel(configuration, new OpenRuleSetManagerListener());
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.NONE;
         wrapperPanelLeft.add(paintPanel, constraintsWrapper);
 
         separator = new JSeparator(JSeparator.HORIZONTAL);
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.HORIZONTAL;
         wrapperPanelLeft.add(separator, constraintsWrapper);
 
         constraintsWrapper.gridy++;
         constraintsWrapper.fill = GridBagConstraints.NONE;
         wrapperPanelLeft.add(jOptionsPanel, constraintsWrapper);
 
         JPanel wrapperPanelRight = new JPanel();
         wrapperPanelRight.setBackground(Color.WHITE);
         wrapperPanelRight.setLayout(new GridBagLayout());
         wrapperPanelRight.add(showcase);
         // and the right side
        final JScrollPane showCaseScrollPane = new JScrollPane(wrapperPanelRight,
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
         // THIS IS NOT OBJECT ORIENTED! IT TOOK HOURS TO FIND THIS SHIT!
         showCaseScrollPane.setViewportBorder(null); 
         // showCaseScrollPane.getViewport().setBackground(new JPanel().getBackground());
         // ^ does not seem to work for viewports workaround v
 
         // not needed, but don't want to forget about: showCaseScrollPane.getViewport().setOpaque(false);
 
         // and if we do not set a null border we get a nice 1px solix black border "for free"
         // the JScrollPane is by far the most unpolished Swing component I have seen so far
         // edit: well that changed. SynthUI and JTree is my new favorite combo
         showCaseScrollPane.setBorder(new LineBorder(Color.GRAY));
 
         showCaseScrollPane.getViewport().addMouseWheelListener(showcase);
         mainPanel.add(showCaseScrollPane, BorderLayout.CENTER);
 
         setPreferredSize(new Dimension(800, 600));
 
         this.pack();
         this.setVisible(true);
 
         toolBox.setInitialFocus();
     }
 
     public ToolBox getToolBox() {
         return toolBox;
     }
 
     public Class getActiveTool() {
         return activeTool;
     }
 
     public void setActiveTool(Class activeTool) {
         this.activeTool = activeTool;
         jOptionsPanel.displayOptions(activeTool);
     }
 
     public Showcase getShowcase() {
         return showcase;
     }
     public void resize() {
         this.pack();
     }
 
     public void setConfiguration(Configuration conf) {
         this.configuration = conf;
     }
 
     public void setPaint(Paint paint) {
         paintPanel.setPaint(paint);
     }
 
     private class MainFrameKeyListener implements KeyListener {
 
         public void keyTyped(KeyEvent e) {
             // System.out.println(""+e.getK)
             //System.out.println("adsf");
             //throw new UnsupportedOperationException("Not supported yet.");
         }
 
         public void keyPressed(KeyEvent e) {
             if (e.getKeyCode() == KeyEvent.VK_PLUS) {
                 zoomInCommand.execute();
             } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                 zoomOutCommand.execute();
             } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                 if (configuration.runLevel < Configuration.RUNLEVEL_RUNNING) {
                     pauseCommand.execute();
                 } else {
                     resumeCommand.execute();
                 }
             }
         }
 
         public void keyReleased(KeyEvent e) {
             //throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 
     private class RuleSetManagerOKListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             RuleSet ruleSet = jRuleSetManager.getSelectedRuleSet();
             assert (ruleSet != null);
             //System.out.println(ruleSet);
             configuration.paint = new Paint(configuration, configuration.paint.getColorMode(),
                     Paint.RULE_SET,
                     configuration.paint.getColor(),
                     ruleSet);
             paintPanel.setRuleSet(ruleSet);
             ((CardLayout)contentPane.getLayout()).show(contentPane, "main");
             menuBar.setVisible(true);
             configuration.runLevel = runLevelBeforeRuleSetManager;
         }
 
     }
 
     private class RuleSetManagerCancelListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             ((CardLayout)contentPane.getLayout()).show(contentPane, "main");
             menuBar.setVisible(true);
             configuration.runLevel = runLevelBeforeRuleSetManager;
         }
 
     }
 
     private class OpenRuleSetManagerListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             ((CardLayout)contentPane.getLayout()).show(contentPane, "rule manager");
             menuBar.setVisible(false);
             runLevelBeforeRuleSetManager = configuration.runLevel;
             configuration.runLevel = Configuration.RUNLEVEL_STOP;
         }
     }
 }
