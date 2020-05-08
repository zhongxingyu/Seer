 package de.htwg.wzzrd.aview;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.Box;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.SoftBevelBorder;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import de.htwg.wzzrd.aview.gui.AddAIDialog;
 import de.htwg.wzzrd.aview.gui.HandCardPanel;
 import de.htwg.wzzrd.aview.gui.JoinDialog;
 import de.htwg.wzzrd.aview.gui.LogTable;
 import de.htwg.wzzrd.aview.gui.ScoreTable;
 import de.htwg.wzzrd.aview.gui.StartDialog;
 import de.htwg.wzzrd.aview.gui.StartServerDialog;
 import de.htwg.wzzrd.aview.gui.TablePanel;
 import de.htwg.wzzrd.aview.gui.TimedJLabel;
 import de.htwg.wzzrd.model.GameNetworkMissingException;
 import de.htwg.wzzrd.model.Language;
 import de.htwg.wzzrd.model.LogInterface;
 import de.htwg.wzzrd.model.PlayerInterface;
 import de.htwg.wzzrd.model.Status;
 import de.htwg.wzzrd.model.StatusItems;
 import de.htwg.wzzrd.model.UIDataInterface;
 import de.htwg.wzzrd.model.network.PacketFactoryInterface;
 import de.htwg.wzzrd.util.IObservable;
 import de.htwg.wzzrd.util.IObserver;
 
 /**
  * GUI
  * 
  * @author chhauss
  * 
  */
 public class GUI implements IObserver {
     private final class JoinGameListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             JoinDialog jd = new JoinDialog(pfactory);
             jd.setVisible(true);
 
             if (jd.getResult() == JoinDialog.Result.OK) {
                 try {
                     pfactory.waitForReady();
                     frm.setTitle(String.format("SE2013WS-13-Wizard: %s", pfactory.getPlayername()));
                     btnJoin.setEnabled(false);
                     btnStartGame.setEnabled(true);
                 } catch (GameNetworkMissingException e1) {
                     LOG.warn("Not connected to server!");
                 } catch (InterruptedException e1) {
                     LOG.warn("Interrupted while waiting for PacketFactory!");
                 }
             }
         }
     }
 
     private final class ForecastListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             if (pfactory.hasGameNetwork()) {
                 try {
                     pfactory.sendSetForecastPacket((Integer) spinner.getValue());
                 } catch (IOException e1) {
                     JOptionPane.showMessageDialog(frm, Language.getTrans("failed-to-send-packet", "Could not send packet to server."));
                 }
             } else {
                 JOptionPane.showMessageDialog(frm, Language.getTrans("not-connected", "You are not connected to a server! Please connect to server and try again."), Language.getTrans("not-connected-title", "Not connected to server!"), JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     private final class KIPanelListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             AddAIDialog.showDialog();
         }
     }
 
     private final class StartServerListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent arg0) {
             StartServerDialog.showDialog(pfactory);
         }
     }
 
     private final class ActionLogListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             logtable.show();
         }
     }
 
     private final class ScoreTableListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             scores.show();
         }
     }
 
     private final class StartGameListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             if (pfactory.hasGameNetwork()) {
                 StartDialog sd = new StartDialog(pfactory, status);
                 sd.setVisible(true);
 
                 if (sd.getResult() == StartDialog.Result.OK) {
                     btnStartGame.setEnabled(false);
                     tablecards.repaint();
                 }
             } else {
                 JOptionPane.showMessageDialog(frm, Language.getTrans("not-connected", "You are not connected to a server! Please connect to server and try again."), Language.getTrans("not-connected-title", "Not connected to server!"), JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     private static final int SPACE3 = 47;
     private static final int SPACE2 = 180;
     private static final int SPACE = 150;
     private static final int TABLESIZE = 10;
     private static final int FONTSIZE = 11;
     private static final int SPINNERFONTSIZE = 45;
     private static final int TABLEHEIGHT = 170;
    private static final int HANDPANELHEIGHT = 220;
     private static final int CARDPANELDISTANCE = 2;
     private static final int BUTTONDISTANCE = 20;
     private static final Logger LOG = LogManager.getLogger("GUI");
     private static final int MAX_LOG_LINES = 40;
     private static final int ERROR_SHOW_TIME = 3000;
 
     private JFrame frm;
     private ScoreTable scores;
     private LogTable logtable;
 
     private PacketFactoryInterface pfactory;
 
     private JSpinner spinner;
 
     private TablePanel tablecards;
     private HandCardPanel handcards;
 
     private JButton btnForecast;
     private JButton btnJoin;
     private JButton btnStartGame;
     private JTextArea lblStatus;
     private TimedJLabel lblMessage;
 
     private Status status = new Status();
 
     /**
      * Create the application.
      * 
      * @wbp.parser.entryPoint
      */
     public GUI() {
 
         initialize();
 
         this.tablecards = new TablePanel();
         this.scores = new ScoreTable();
         this.logtable = new LogTable();
         frm.getContentPane().add(tablecards, BorderLayout.CENTER);
 
         frm.pack();
         frm.setMinimumSize(frm.getSize());
         this.frm.setVisible(true);
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e1) {}
 
         frm = new JFrame();
         frm.setTitle("SE2013WS-13-Wizard: ");
         frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         JMenuBar menuBar = new JMenuBar();
         frm.setJMenuBar(menuBar);
 
         btnJoin = new JButton("Join Game");
         btnJoin.addActionListener(new JoinGameListener());
         menuBar.add(btnJoin);
 
         btnStartGame = new JButton("Start Game");
         btnStartGame.setEnabled(false);
         btnStartGame.addActionListener(new StartGameListener());
         menuBar.add(btnStartGame);
 
         Component horizontalStrut = Box.createHorizontalStrut(BUTTONDISTANCE);
         menuBar.add(horizontalStrut);
 
         JButton btnShowScoreTable = new JButton("Show Score Table");
         btnShowScoreTable.addActionListener(new ScoreTableListener());
         menuBar.add(btnShowScoreTable);
 
         JButton btnActionLog = new JButton("Show Action Log");
         btnActionLog.addActionListener(new ActionLogListener());
         menuBar.add(btnActionLog);
 
         JButton btnStartServer = new JButton("Start Server");
         btnStartServer.addActionListener(new StartServerListener());
         menuBar.add(btnStartServer);
 
         JButton btnNewButton2 = new JButton("Show KI-Panel");
         btnNewButton2.addActionListener(new KIPanelListener());
         menuBar.add(btnNewButton2);
 
         JPanel panel = new JPanel();
         panel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
         frm.getContentPane().add(panel, BorderLayout.SOUTH);
         panel.setLayout(new BorderLayout(0, 0));
 
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(CARDPANELDISTANCE, HANDPANELHEIGHT));
         scrollPane.setBorder(new CompoundBorder(new EmptyBorder(CARDPANELDISTANCE, CARDPANELDISTANCE, 0, CARDPANELDISTANCE), new EtchedBorder(EtchedBorder.LOWERED, null, null)));
         scrollPane.setAlignmentY(0.0f);
         scrollPane.setAlignmentX(0.0f);
         panel.add(scrollPane, BorderLayout.CENTER);
 
         lblMessage = new TimedJLabel(ERROR_SHOW_TIME);
         frm.getContentPane().add(lblMessage, BorderLayout.NORTH);
 
         handcards = new HandCardPanel(lblMessage, status);
        handcards.setMinimumSize(new Dimension(1120, 10));
         handcards.setBorder(null);
         scrollPane.setViewportView(handcards);
 
         JPanel panel1 = new JPanel();
         panel1.setPreferredSize(new Dimension(TABLEHEIGHT, TABLESIZE));
         panel.add(panel1, BorderLayout.EAST);
 
         spinner = new JSpinner();
         spinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
         spinner.setFont(new Font("Tahoma", Font.PLAIN, SPINNERFONTSIZE));
         spinner.setEnabled(false);
 
         btnForecast = new JButton("Vorhersage abgeben");
         btnForecast.setEnabled(false);
         btnForecast.addActionListener(new ForecastListener());
 
         lblStatus = new JTextArea();
         lblStatus.setBackground(UIManager.getColor("Label.background"));
         lblStatus.setWrapStyleWord(true);
         lblStatus.setFont(new Font("Monospaced", Font.PLAIN, FONTSIZE));
         lblStatus.setLineWrap(true);
         lblStatus.setEditable(false);
 
         GroupLayout glPanel1 = new GroupLayout(panel1);
         glPanel1.setHorizontalGroup(glPanel1.createParallelGroup(Alignment.TRAILING).addGroup(glPanel1.createSequentialGroup().addContainerGap().addGroup(glPanel1.createParallelGroup(Alignment.TRAILING).addComponent(lblStatus, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, SPACE2, Short.MAX_VALUE).addComponent(spinner, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, SPACE2, Short.MAX_VALUE).addComponent(btnForecast, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, SPACE2, Short.MAX_VALUE)).addContainerGap()));
         glPanel1.setVerticalGroup(glPanel1.createParallelGroup(Alignment.TRAILING).addGroup(glPanel1.createSequentialGroup().addContainerGap().addComponent(lblStatus, GroupLayout.DEFAULT_SIZE, SPACE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(spinner, GroupLayout.PREFERRED_SIZE, SPACE3, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnForecast).addContainerGap()));
         panel1.setLayout(glPanel1);
     }
 
     @Override
     public void update(IObservable ui) {
         if (!tablecards.initDone()) {
             tablecards.init(ui.getUIData().getPlayers().length);
         }
         if (scores.notInitialized()) {
             scores.initTable(ui.getUIData());
         }
 
         // shortcut
         UIDataInterface d = ui.getUIData();
 
         spinner.setModel(new SpinnerNumberModel(0, 0, ui.getUIData().getStats().getCurrentRoundNo(), 1));
         buildLog(d.getLog());
 
         handcards.setCards(ui.getCardsOnHand());
 
         tablecards.updateTable(d);
         scores.updateTable(d);
 
         // disable ui
         disableAllGameElements();
         switch (ui.getUIData().getState()) {
         case BETTING:
             lblStatus.setText(String.format(Language.getTrans("gui-betting", "It is %s's turn to predict now!"), d.getActivePlayer().getName()));
             spinner.setEnabled(true);
             btnForecast.setEnabled(true);
             break;
         case PLAYING:
             lblStatus.setText(String.format(Language.getTrans("gui-playing", "It is %s's turn to play now!"), d.getActivePlayer().getName()));
             handcards.setButtonsEnabled(true);
             break;
         case FINISHED:
             StringBuilder sb = new StringBuilder();
             sb.append("The game is over. Winner:\n");
             for (PlayerInterface p : d.getStats().getWinningPlayers()) {
                 sb.append(String.format("  %s\n", p.getName()));
             }
             JOptionPane.showMessageDialog(frm, sb.toString(), "Finished Game!", JOptionPane.INFORMATION_MESSAGE);
             break;
         }
         if (!ui.getUIData().getActivePlayer().getName().equals(pfactory.getPlayername())) {
             disableAllGameElements();
             LOG.debug("keeping controls disabled, not your turn...");
         } else {
             lblStatus.setBackground(Color.GREEN);
         }
         this.frm.repaint();
     }
 
     private void buildLog(LogInterface log) {
         StringBuilder sb = new StringBuilder();
 
         for (int i = 0; i < MAX_LOG_LINES; i++) {
             sb.append(log.getLine(i, MAX_LOG_LINES)).append('\n');
         }
         this.logtable.setText(sb.toString());
     }
 
     @Override
     public void setPacketFactory(PacketFactoryInterface pfactory) {
         assert (this.pfactory == null);
         this.pfactory = pfactory;
         this.handcards.setPacketFactory(pfactory);
     }
 
     private void disableAllGameElements() {
         spinner.setEnabled(false);
         btnForecast.setEnabled(false);
         handcards.setButtonsEnabled(false);
         lblStatus.setBackground(UIManager.getColor("Label.background"));
         btnJoin.setEnabled(false);
         btnStartGame.setEnabled(false);
     }
 
     @Override
     public void setStatus(StatusItems status, String text) {
         this.status.setStatus(status, text);
     }
 
     @Override
     public void disableJoins() {
         btnJoin.setEnabled(false);
         btnStartGame.setEnabled(true);
     }
 
     @Override
     public void notifyDisconnected() {
         JOptionPane.showMessageDialog(null, "Server closed connection!");
     }
 }
