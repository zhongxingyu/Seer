 /*
  * GametableFrame.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable;
 
 import java.awt.*;
 import java.awt.dnd.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import com.galactanet.gametable.prefs.PreferenceDescriptor;
 import com.galactanet.gametable.prefs.Preferences;
 
 
 
 // import java.util.ArrayList;
 
 /**
  * TODO: comment
  * 
  * @author sephalon
  */
 public class GametableFrame extends JFrame implements ComponentListener, DropTargetListener, DragGestureListener,
     PropertyChangeListener, ActionListener, KeyListener
 {
     class ToolButtonActionListener implements ActionListener
     {
         int m_id;
 
         ToolButtonActionListener(int id)
         {
             m_id = id;
         }
 
         /*
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             m_gametableCanvas.setActiveTool(m_id);
             m_gametableCanvas.requestFocus();
         }
     }
 
     class ToolButtonAbstractAction extends AbstractAction
     {
         int m_id;
 
         ToolButtonAbstractAction(int id)
         {
             m_id = id;
         }
 
         public void actionPerformed(ActionEvent event)
         {
             m_gametableCanvas.setActiveTool(m_id);
             m_gametableCanvas.requestFocus();
         }
     }
 
     // Global instance of the frame
     public static GametableFrame  g_gameTableFrame;
 
     // this gets bumped up every time the comm protocols change
     public final static int       COMM_VERSION               = 8;
 
     JPanel                        m_contentPane;
 
     JMenuBar                      m_menuBar                  = new JMenuBar();
 
     JMenu                         m_fileMenu                 = new JMenu("File");
     JMenuItem                     m_openMenuItem             = new JMenuItem("Open...");
     JMenuItem                     m_saveMenuItem             = new JMenuItem("Save");
     JMenuItem                     m_saveAsMenuItem           = new JMenuItem("Save As...");
     JMenuItem                     m_reacquirePogsMenuItem     = new JMenuItem("Reacquire Pogs");
     JMenuItem                     m_exitMenuItem             = new JMenuItem("Exit");
 
     JMenu                         m_networkMenu              = new JMenu("Network");
     JMenuItem                     m_hostMenuItem             = new JMenuItem("Host...");
     JMenuItem                     m_joinMenuItem             = new JMenuItem("Join...");
     JMenuItem                     m_disconnect               = new JMenuItem("Disconnect");
 
     JMenu                         m_mapMenu                  = new JMenu("Map");
     JMenuItem                     m_clearMapMenuItem        = new JMenuItem("Clear Map");
     JMenuItem                     m_recenter                 = new JMenuItem("Recenter All Players");
     JCheckBoxMenuItem             m_hexModeMenuItem          = new JCheckBoxMenuItem("Hex Mode");
 
     JMenu                         m_diceMacrosMenu           = new JMenu("Dice Macros");
     JMenuItem                     m_addDiceMacroMenuItem     = new JMenuItem("Add...");
     JMenuItem                     m_removeDiceMacroMenuItem  = new JMenuItem("Remove...");
 
     JMenu                         m_toolsMenu                = new JMenu("Tools");
 
     JMenu                         m_helpMenu                 = new JMenu("Help");
     JMenuItem                     m_aboutMenuItem            = new JMenuItem("About");
 
     JPanel                        m_propertiesArea           = new JPanel();
     public GametableCanvas        m_gametableCanvas          = new GametableCanvas();
     JList                         m_playerList               = new JList();
 
     boolean                       m_bInitted;
     Dimension                     m_prevRightSize            = new Dimension(-1, -1);
 
     public List                   m_players                  = new ArrayList();
 
     // which player I am
     public int                    m_myPlayerIdx;
 
     String                        m_defaultName              = "Noname";
     String                        m_defaultCharName          = "Nochar";
     String                        m_defaultIP                = "localhost";
     public int                    m_defaultPort              = DEFAULT_PORT;
     String                        m_defaultPassword          = "";
 
     JScrollPane                   m_playerListScrollPane     = new JScrollPane();
     JPanel                        m_textAndEntryPanel        = new JPanel();
     public JTextField             m_textEntry                = new JTextField();
     JScrollPane2                  m_textLogScroller          = new JScrollPane2();
     JTextPane                     m_textLog                  = new JTextPane();
     JSplitPane                    m_mapChatSplitPane         = new JSplitPane();
 
     boolean                       m_bFirstPaint              = true;
     boolean                       m_bDisregardDividerChanges = true;
     int                           m_prevDividerLocFromBottom = -1;
     JPanel                        m_textAreaPanel            = new JPanel();
     JPanel                        m_macroButtonsPanel        = new JPanel();
     public JSplitPane             m_mapPogSplitPane          = new JSplitPane();
     public PogsPanel              m_pogsPanel                = new PogsPanel();
     public PogsPanel              m_underlaysPanel           = new PogsPanel();
     JToolBar                      m_toolBar                  = new JToolBar();
     ButtonGroup                   m_toolButtonGroup          = new ButtonGroup();
 
     List                          m_macros                   = new ArrayList();
     List                          m_macroButtons             = new ArrayList();
 
     public final static int       NETSTATE_NONE              = 0;
     public final static int       NETSTATE_HOST              = 1;
     public final static int       NETSTATE_JOINED            = 2;
     public int                    m_netStatus                = NETSTATE_NONE;
 
     HostListenThread              m_hostListenThread;
 
     // full of Connection instances
     List                          m_connections              = new ArrayList();
     PacketPoller                  m_poller                   = new PacketPoller();
 
     public Color                  m_drawColor                = Color.BLACK;
 
     // window size and position
     Point                         m_windowPos;
     Dimension                     m_windowSize;
     boolean                       m_bMaximized;
 
     // a flag to tell the app
     // not to size or center us.
     public boolean                m_bLoadedState;
     public JTabbedPane            m_pogsTabbedPane           = new JTabbedPane();
     JComboBox                     m_colorCombo               = new JComboBox(g_comboColors);
 
     // full of Strings
     public List                   m_textSent                 = new ArrayList();
     int                           m_textSentLoc              = 0;
 
     public final static int       REJECT_INVALID_PASSWORD    = 0;
     public final static int       REJECT_VERSION_MISMATCH    = 1;
 
     public final static int       DEFAULT_PORT               = 6812;
 
     public final static Integer[] g_comboColors              = {
         new Integer(Color.BLACK.getRGB()), new Integer(Color.BLUE.getRGB()), new Integer(Color.RED.getRGB()),
         new Integer(Color.CYAN.getRGB()), new Integer(Color.YELLOW.getRGB()), new Integer(Color.GRAY.getRGB()),
         new Integer(Color.GREEN.getRGB()), new Integer(Color.ORANGE.getRGB()), new Integer(Color.WHITE.getRGB()),
                                                              };
 
     // The current file path used by save and open.
     // NULL if unset.
     public File                   m_actingFile;
 
     private ToolManager           m_toolManager              = new ToolManager();
     private JToggleButton         m_toolButtons[]            = null;
     private Preferences           m_preferences              = new Preferences();
     
     public ProgressSpinner		  m_progressSpinner			  = new ProgressSpinner();
 
     /**
      * Construct the frame
      */
     public GametableFrame()
     {
         setTitle(GametableApp.VERSION);
         g_gameTableFrame = this;
 
         this.addComponentListener(this);
         enableEvents(AWTEvent.WINDOW_EVENT_MASK);
         // try
         {
             m_contentPane = (JPanel)this.getContentPane();
             setSize(new Dimension(600, 500));
 
             m_openMenuItem.addActionListener(this);
             m_saveMenuItem.addActionListener(this);
             m_saveAsMenuItem.addActionListener(this);
             m_reacquirePogsMenuItem.addActionListener(this);
             m_exitMenuItem.addActionListener(this);
             m_clearMapMenuItem.setActionCommand("clearPogs");
             m_clearMapMenuItem.addActionListener(this);
             m_hexModeMenuItem.addActionListener(this);
             m_hostMenuItem.addActionListener(this);
             m_joinMenuItem.addActionListener(this);
             m_disconnect.addActionListener(this);
             m_recenter.addActionListener(this);
             m_addDiceMacroMenuItem.addActionListener(this);
             m_removeDiceMacroMenuItem.addActionListener(this);
             m_aboutMenuItem.addActionListener(this);
 
             m_contentPane.setLayout(new BorderLayout());
             m_contentPane.setMaximumSize(new Dimension(32767, 32767));
             m_contentPane.setPreferredSize(new Dimension(2000, 300));
             m_contentPane.setRequestFocusEnabled(true);
             m_propertiesArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
             m_propertiesArea.setMinimumSize(new Dimension(10, 10));
             m_propertiesArea.setPreferredSize(new Dimension(70, 100));
             m_propertiesArea.setRequestFocusEnabled(true);
             m_propertiesArea.setLayout(new BorderLayout());
             m_playerListScrollPane.setPreferredSize(new Dimension(180, 120));
             m_textAndEntryPanel.setLayout(new BorderLayout());
             m_textLog.setDoubleBuffered(true);
             m_textLog.setPreferredSize(new Dimension(500, 21));
             m_textEntry.addActionListener(new GametableFrame_m_textEntry_actionAdapter(this));
             m_textLogScroller.setDoubleBuffered(true);
             m_textLogScroller.setPreferredSize(new Dimension(450, 31));
             m_mapChatSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
             m_mapChatSplitPane.setContinuousLayout(true);
             m_mapChatSplitPane.setResizeWeight(1.0);
             m_gametableCanvas.setMaximumSize(new Dimension(32767, 32767));
             m_gametableCanvas.setPreferredSize(new Dimension(500, 400));
             m_textAndEntryPanel.setPreferredSize(new Dimension(500, 52));
             m_textAreaPanel.setLayout(new BorderLayout());
             m_mapPogSplitPane.setContinuousLayout(true);
             m_pogsPanel.setMaximumSize(new Dimension(32767, 32767));
             m_pogsPanel.setMinimumSize(new Dimension(1, 1));
             m_pogsPanel.setPreferredSize(new Dimension(20, 10));
             m_underlaysPanel.setMaximumSize(new Dimension(32767, 32767));
             m_underlaysPanel.setMinimumSize(new Dimension(1, 1));
             m_underlaysPanel.setPreferredSize(new Dimension(20, 10));
             m_macroButtonsPanel.setLayout(new BoxLayout(m_macroButtonsPanel, BoxLayout.Y_AXIS));
             m_macroButtonsPanel.setToolTipText("Macros");
             m_colorCombo.setMaximumSize(new Dimension(100, 21));
             m_toolBar.add(m_colorCombo, null);
             try
             {
                 m_toolManager.initialize();
                 int buttonSize = m_toolManager.getMaxIconSize();
                 int numTools = m_toolManager.getNumTools();
                 m_toolButtons = new JToggleButton[numTools];
                 for (int toolId = 0; toolId < numTools; toolId++)
                 {
                     ToolManager.Info info = m_toolManager.getToolInfo(toolId);
                     Image im = UtilityFunctions.createDrawableImage(buttonSize, buttonSize);
                     {
                         Graphics g = im.getGraphics();
                         Image icon = info.getIcon();
                         int offsetX = (buttonSize - icon.getWidth(null)) / 2;
                         int offsetY = (buttonSize - icon.getHeight(null)) / 2;
                         g.drawImage(info.getIcon(), offsetX, offsetY, null);
                         g.dispose();
                     }
 
                     JToggleButton button = new JToggleButton(new ImageIcon(im));
                     m_toolBar.add(button);
                     button.addActionListener(new ToolButtonActionListener(toolId));
                     m_toolButtonGroup.add(button);
                     m_toolButtons[toolId] = button;
 
                     String actionId = "tool" + toolId + "Action";
                     m_gametableCanvas.getActionMap().put(actionId, new ToolButtonAbstractAction(toolId));
                     m_gametableCanvas.getInputMap().put(KeyStroke.getKeyStroke(info.getQuickKey(), 0, false), actionId);
                     List prefs = info.getTool().getPreferences();
                     for (int i = 0; i < prefs.size(); i++)
                     {
                         m_preferences.addPreference((PreferenceDescriptor)prefs.get(i));
                     }
                 }
             }
             catch (IOException ioe)
             {
                 Log.log(Log.SYS, "Failure initializing tools.");
                 Log.log(Log.SYS, ioe);
             }
             m_menuBar.add(m_fileMenu);
             m_fileMenu.add(m_openMenuItem);
             m_fileMenu.add(m_saveMenuItem);
             m_fileMenu.add(m_saveAsMenuItem);
             m_fileMenu.add(m_reacquirePogsMenuItem);
             m_fileMenu.add(m_exitMenuItem);
             m_menuBar.add(m_networkMenu);
             m_menuBar.add(m_mapMenu);
             m_mapMenu.add(m_clearMapMenuItem);
             m_mapMenu.add(m_recenter);
             m_mapMenu.add(m_hexModeMenuItem);
             m_menuBar.add(m_diceMacrosMenu);
             m_menuBar.add(m_helpMenu);
             m_helpMenu.add(m_aboutMenuItem);
             m_textAndEntryPanel.add(m_textEntry, BorderLayout.SOUTH);
             m_textAndEntryPanel.add(m_textLogScroller, BorderLayout.CENTER);
             m_contentPane.add(m_toolBar, BorderLayout.NORTH);
             m_textLogScroller.getViewport().add(m_textLog, null);
             m_mapChatSplitPane.add(m_mapPogSplitPane, JSplitPane.TOP);
             m_mapPogSplitPane.add(m_gametableCanvas, JSplitPane.BOTTOM);
             m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.TOP);
             m_pogsTabbedPane.add(m_pogsPanel, "Pogs");
             m_pogsTabbedPane.add(m_underlaysPanel, "Underlays");
             m_mapChatSplitPane.add(m_propertiesArea, JSplitPane.BOTTOM);
             m_propertiesArea.add(m_playerListScrollPane, BorderLayout.WEST);
             m_propertiesArea.add(m_textAreaPanel, BorderLayout.CENTER);
             m_playerListScrollPane.getViewport().add(m_playerList, null);
             m_textAreaPanel.add(m_macroButtonsPanel, BorderLayout.WEST);
             m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
             m_mapChatSplitPane.addPropertyChangeListener(this);
             m_contentPane.add(m_mapChatSplitPane, BorderLayout.CENTER);
 
             m_disconnect.setEnabled(false);
 
             ColorComboCellRenderer renderer = new ColorComboCellRenderer();
             m_colorCombo.setRenderer(renderer);
 
             m_gametableCanvas.init(this);
             m_pogsPanel.init(m_gametableCanvas, true);
             m_underlaysPanel.init(m_gametableCanvas, false);
             addKeyListener(m_gametableCanvas);
 
             m_playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             m_myPlayerIdx = 0;
 
             m_textLog.setEditable(false);
             m_gametableCanvas.requestFocus();
             m_networkMenu.add(m_hostMenuItem);
             m_networkMenu.add(m_joinMenuItem);
             m_networkMenu.add(m_disconnect);
             m_diceMacrosMenu.add(m_addDiceMacroMenuItem);
             m_diceMacrosMenu.add(m_removeDiceMacroMenuItem);
 
             addMacroButton("d20", "d20");
 
             // start the poll thread
             m_poller.start();
 
             loadState(new File("autosave.grm"));
             loadPrefs();
 
             addPlayer(new Player(m_defaultName, m_defaultCharName));
 
             m_textEntry.addKeyListener(this);
             m_colorCombo.addActionListener(this);
             updateHexModeMenuItem();
             m_bInitted = true;
             setJMenuBar(m_menuBar);
         }
         // catch (Exception e)
         // {
         // Log.log(Log.SYS, e);
         // }
     }
 
     public static GametableFrame getGametableFrame()
     {
         return g_gameTableFrame;
     }
 
     public Preferences getPreferences()
     {
         return m_preferences;
     }
 
     public void updateWindowInfo()
     {
         // we only update our internal size and
         // position variables if we aren't maximized.
         if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
         {
             m_bMaximized = true;
         }
         else
         {
             m_bMaximized = false;
             m_windowSize = getSize();
             m_windowPos = getLocation();
         }
     }
 
     // interface overrides
     public void componentResized(ComponentEvent event)
     {
         // if (m_bInitted && m_prevDividerLocFromBottom != -1)
         // {
         // // user resize.
         // int newDividerLoc = m_mapChatSplitPane.getHeight() - m_prevDividerLocFromBottom;
         // m_bDisregardDividerChanges = true;
         // m_mapChatSplitPane.setDividerLocation(newDividerLoc);
         // m_bDisregardDividerChanges = false;
         // }
 
         updateWindowInfo();
     }
 
     public Player getMePlayer()
     {
         return (Player)m_players.get(m_myPlayerIdx);
     }
 
     public void componentMoved(ComponentEvent e)
     {
         updateWindowInfo();
     }
 
     public void componentShown(ComponentEvent e)
     {
     }
 
     public void componentHidden(ComponentEvent e)
     {
     }
 
     public void dragEnter(DropTargetDragEvent dtde)
     {
     }
 
     public void dragOver(DropTargetDragEvent dtde)
     {
     }
 
     public void dropActionChanged(DropTargetDragEvent dtde)
     {
     }
 
     public void dragExit(DropTargetEvent dte)
     {
     }
 
     public void dragGestureRecognized(DragGestureEvent dge)
     {
     }
 
     public void drop(final java.awt.dnd.DropTargetDropEvent e)
     {
     }
 
     public void rejectPacketReceived(int reason)
     {
         confirmJoined();
 
         // you got rejected!
         switch (reason)
         {
             case REJECT_INVALID_PASSWORD:
             {
                 logSystemMessage("Invalid Password. Connection refused.");
             }
             break;
 
             case REJECT_VERSION_MISMATCH:
             {
                 logSystemMessage("The host is running a different comm-version of Gametable. Connection refused.");
             }
             break;
         }
         disconnect();
     }
 
     public void recenterPacketReceived(int x, int y, int zoom)
     {
         m_gametableCanvas.doRecenterView(x, y, zoom);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             push(PacketManager.makeRecenterPacket(x, y, zoom));
         }
     }
 
     public void pogDataPacketReceived(int id, String s)
     {
         m_gametableCanvas.doSetPogData(id, s);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             push(PacketManager.makePogDataPacket(id, s));
         }
     }
 
     public void grmPacketReceived(byte grmFile[])
     {
         // only the host should ever get this packet. If a joiner gets it
         // for some reason, it should ignore it.
         // if we're offline, then sure, go ahead and load
         if (m_netStatus != NETSTATE_JOINED)
         {
             loadStateFromRawFileData(grmFile);
         }
     }
 
     public void pointPacketReceived(int plrIdx, int x, int y, boolean bPointing)
     {
         // we're not interested in point packets of our own hand
         if (plrIdx != m_myPlayerIdx)
         {
             Player plr = (Player)m_players.get(plrIdx);
             plr.setPoint(x, y);
             plr.setPointing(bPointing);
         }
 
         if (m_netStatus == NETSTATE_HOST)
         {
             push(PacketManager.makePointPacket(plrIdx, x, y, bPointing));
         }
 
         m_gametableCanvas.repaint();
     }
 
     public void movePogPacketReceived(int id, int newX, int newY)
     {
         m_gametableCanvas.doMovePog(id, newX, newY);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeMovePogPacket(id, newX, newY));
         }
     }
 
     public void removePogsPacketReceived(int ids[])
     {
         m_gametableCanvas.doRemovePogs(ids);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeRemovePogsPacket(ids));
         }
     }
 
     public void addPogPacketReceived(Pog pog)
     {
         m_gametableCanvas.doAddPog(pog);
 
         // update the next pog id if necessary
         if (pog.m_ID >= Pog.g_nextID)
         {
             Pog.g_nextID = pog.m_ID + 5;
         }
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeAddPogPacket(pog));
         }
     }
 
     public void erasePacketReceived(Rectangle r, boolean bColorSpecific, int color)
     {
         // erase the lines
         m_gametableCanvas.doErase(r, bColorSpecific, color);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeErasePacket(r, bColorSpecific, color));
         }
     }
 
     public void linesPacketReceived(LineSegment[] lines)
     {
         // add the lines to the array
         m_gametableCanvas.doAddLineSegments(lines);
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeLinesPacket(lines));
         }
     }
 
     public void textPacketReceived(String text)
     {
         if (m_netStatus == NETSTATE_HOST)
         {
             // if you're the host, push to all players
             postMessage(text);
         }
         else
         {
             // otherwise, just add it
             logMessage(text);
         }
     }
 
     public void hexModePacketReceived(boolean bHexMode)
     {
         // note the new hex mode
         m_gametableCanvas.m_bHexMode = bHexMode;
         updateHexModeMenuItem();
 
         if (m_netStatus == NETSTATE_HOST)
         {
             // if we're the host, send it to the clients
             push(PacketManager.makeHexModePacket(bHexMode));
         }
 
         repaint();
     }
 
     public int getPlayerIdx(Player plr)
     {
         return m_players.indexOf(plr);
     }
 
     public Player getPlayerFromConnection(Connection conn)
     {
         for (int i = 0; i < m_players.size(); i++)
         {
             Player plr = (Player)m_players.get(i);
             if (conn == plr.getConnection())
             {
                 return plr;
             }
         }
 
         return null;
     }
 
     public void connectionDropped(Connection conn)
     {
         if (m_netStatus == NETSTATE_JOINED)
         {
             // we lost our connection to the host
             logSystemMessage("Your connection to the host was lost.");
             disconnect();
 
             m_netStatus = NETSTATE_NONE;
             return;
         }
 
         // remove the connection
         m_connections.remove(conn);
 
         // find the player who owns that connection
         Player dead = getPlayerFromConnection(conn);
         if (dead != null)
         {
             // remove this player
             m_players.remove(dead);
             refreshPlayerListBox();
             sendCastInfo();
             postSystemMessage("" + dead.getPlayerName() + " has left the session");
         }
         else
         {
             postSystemMessage("Someone tried to log in, but was rejected.");
         }
     }
 
     public void push(byte[] packet)
     {
         if (m_netStatus == NETSTATE_JOINED)
         {
             if (m_connections.size() != 1)
             {
                 throw new IllegalStateException("joiner player does not have exactly 1 connection");
             }
         }
         for (int i = 0; i < m_connections.size(); i++)
         {
             Connection conn = (Connection)m_connections.get(i);
             conn.sendPacket(packet);
         }
     }
 
     public void send(byte[] packet, Player recipient)
     {
         if (recipient.getConnection() == null)
         {
             // uh...
             return;
         }
 
         recipient.getConnection().sendPacket(packet);
     }
 
     public void send(byte[] packet, Connection recipient)
     {
         recipient.sendPacket(packet);
     }
 
     public void confirmHost()
     {
         if (m_netStatus != NETSTATE_HOST)
         {
             throw new IllegalStateException("confirmHost failure");
         }
     }
 
     public void confirmJoined()
     {
         if (m_netStatus != NETSTATE_JOINED)
         {
             throw new IllegalStateException("confirmJoined failure");
         }
     }
 
     public boolean hostDlg()
     {
         JoinDialog dlg = new JoinDialog();
         dlg.setModal(true);
         dlg.setUpForHostDlg();
         dlg.setVisible(true);
 
         if (!dlg.m_bAccepted)
         {
             // they cancelled out
             return false;
         }
         return true;
     }
 
     public boolean inputConnectionIP()
     {
         JoinDialog dlg = new JoinDialog();
         dlg.setModal(true);
         dlg.setVisible(true);
 
         if (!dlg.m_bAccepted)
         {
             // they cancelled out
             return false;
         }
         return true;
     }
 
     public void updateCast(Player[] players, int ourIdx)
     {
         // you should only get this if you're a joiner
         confirmJoined();
 
         // set up the current cast
         m_players = new ArrayList();
         for (int i = 0; i < players.length; i++)
         {
             addPlayer(players[i]);
         }
 
         m_myPlayerIdx = ourIdx;
     }
 
     public void kick(Connection conn, int reason)
     {
         this.send(PacketManager.makeRejectPacket(reason), conn);
         conn.terminate();
         m_connections.remove(conn);
     }
 
     public void playerJoined(Connection connection, Player player, String password)
     {
         confirmHost();
 
         if (!m_defaultPassword.equals(password))
         {
             // rejected!
             kick(connection, REJECT_INVALID_PASSWORD);
             return;
         }
 
         // now we can associate a player with the connection
         connection.setQuarantined(false);
         player.setConnection(connection);
 
         // tell everyone about the new guy
         postSystemMessage(player.getPlayerName() + " has joined the session");
         addPlayer(player);
 
         sendCastInfo();
 
         // tell the new guy the entire state of the game
         // lines
         LineSegment[] lines = new LineSegment[m_gametableCanvas.getSharedMap().getNumLines()];
         for (int i = 0; i < m_gametableCanvas.getSharedMap().getNumLines(); i++)
         {
             lines[i] = m_gametableCanvas.getSharedMap().getLineAt(i);
         }
         send(PacketManager.makeLinesPacket(lines), player);
 
         // pogs
         for (int i = 0; i < m_gametableCanvas.getSharedMap().getNumPogs(); i++)
         {
             Pog pog = m_gametableCanvas.getSharedMap().getPogAt(i);
             send(PacketManager.makeAddPogPacket(pog), player);
         }
 
         // finally, have the player recenter on the host's view
         int viewCenterX = m_gametableCanvas.getWidth() / 2;
         int viewCenterY = m_gametableCanvas.getHeight() / 2;
 
         // convert to model coordinates
         Point modelCenter = m_gametableCanvas.viewToModel(viewCenterX, viewCenterY);
         send(PacketManager.makeRecenterPacket(modelCenter.x, modelCenter.y, m_gametableCanvas.m_zoom), player);
     }
 
     public void sendCastInfo()
     {
         // and we have to push this data out to everyone
         for (int i = 0; i < m_players.size(); i++)
         {
             Player recipient = (Player)m_players.get(i);
             byte[] castPacket = PacketManager.makeCastPacket(recipient);
             send(castPacket, recipient);
         }
     }
 
     public void newConnection(Connection conn)
     {
         // as hosts, we received a new connection
         // just throw that in a connection pool for now. We don't
         // create players until a connection gives us player info
         m_connections.add(conn);
 
         // now that it's added, we're ready for it.
         conn.start();
     }
 
     public void packetReceived(Connection conn, byte[] packet)
     {
         // synch here. after we get the packet, but before we process it.
         // this is all the synching we need on the comm end of things.
         // we will also need to synch every user entry point
         PacketManager.readPacket(conn, packet);
     }
 
     public void host()
     {
         if (m_netStatus == NETSTATE_HOST)
         {
             logSystemMessage("You are already hosting.");
             return;
         }
         if (m_netStatus == NETSTATE_JOINED)
         {
             logSystemMessage("You can not host until you disconnect from the game you joined.");
             return;
         }
 
         // get relevant infor from the user
         if (!hostDlg())
         {
             return;
         }
 
         // clear out all players
         m_players = new ArrayList();
         Player me = new Player(m_defaultName, m_defaultCharName);
         m_players.add(me);
         me.setHostPlayer(true);
         m_myPlayerIdx = 0;
 
         refreshPlayerListBox();
 
         m_hostListenThread = new HostListenThread();
         m_netStatus = NETSTATE_HOST;
         m_hostListenThread.start();
         m_poller.activate(true);
 
         logSystemMessage("Hosting on port: " + m_defaultPort);
 
         m_hostMenuItem.setEnabled(false);
         m_joinMenuItem.setEnabled(false);
         m_disconnect.setEnabled(true);
         setTitle(GametableApp.VERSION + " - " + me.getCharacterName());
     }
 
     public void hostThreadFailed()
     {
         logAlertMessage("Failed to host.");
         m_hostListenThread = null;
         disconnect();
     }
 
     public void join()
     {
         if (m_netStatus == NETSTATE_HOST)
         {
             logSystemMessage("You are hosting. If you wish to join a game, disconnect first.");
             return;
         }
         if (m_netStatus == NETSTATE_JOINED)
         {
             logSystemMessage("You are already in a game. You must disconnect before joining another.");
             return;
         }
 
         boolean res = inputConnectionIP();
         if (!res)
         {
             // they cancelled out
             return;
         }
 
         InetAddress addr = null;
         try
         {
             addr = InetAddress.getByName(m_defaultIP);
         }
         catch (UnknownHostException ex)
         {
             Log.log(Log.SYS, ex);
             logSystemMessage("Unable to resolve address. Failed to connect.");
             return;
         }
 
         // now we have the ip to connect to. Try to connect to it
         try
         {
             Socket sock = new Socket(addr, m_defaultPort);
             Connection conn = new Connection();
             conn.setQuarantined(false);
             conn.init(sock);
             m_connections.add(conn);
 
             // there should only be 1 connection when we're joining
             if (m_connections.size() != 1)
             {
                 throw new IllegalStateException("Multiple connections on a joiner");
             }
 
             // now that we've successfully made a connection, let the host know
             // who we are
             m_players = new ArrayList();
             Player me = new Player(m_defaultName, m_defaultCharName);
             m_players.add(me);
             m_myPlayerIdx = 0;
 
             // reset game data
             m_gametableCanvas.getSharedMap().setScroll(0, 0);
             m_gametableCanvas.getSharedMap().clearPogs();
             m_gametableCanvas.getSharedMap().clearLines();
             PacketManager.g_imagelessPogs.clear();
 
             // send the packet
             conn.sendPacket(PacketManager.makePlayerPacket(me, m_defaultPassword));
 
             // and now we're ready to pay attention
             m_netStatus = NETSTATE_JOINED;
             conn.start();
             m_poller.activate(true);
 
             logSystemMessage("Joined game");
 
             m_hostMenuItem.setEnabled(false);
             m_joinMenuItem.setEnabled(false);
             m_disconnect.setEnabled(true);
             setTitle(GametableApp.VERSION + " - " + me.getCharacterName());
         }
         catch (Exception ex)
         {
             Log.log(Log.SYS, ex);
             logSystemMessage("Failed to connect.");
             setTitle(GametableApp.VERSION);
         }
     }
 
     public void disconnect()
     {
         if (m_netStatus == NETSTATE_NONE)
         {
             logSystemMessage("Nothing to disconnect from.");
             return;
         }
 
         // drop all connections. Cease all listening. clear all packets
         for (int i = 0; i < m_connections.size(); i++)
         {
             Connection conn = (Connection)m_connections.get(i);
             conn.terminate();
         }
 
         m_connections = new ArrayList();
 
         m_poller.activate(false);
         if (m_netStatus == NETSTATE_HOST)
         {
             if (m_hostListenThread != null)
             {
                 m_hostListenThread.terminate();
                 m_hostListenThread = null;
             }
         }
 
         m_netStatus = NETSTATE_NONE;
         logSystemMessage("Disconnected.");
 
         m_hostMenuItem.setEnabled(true);
         m_joinMenuItem.setEnabled(true);
         m_disconnect.setEnabled(false);
 
         m_players = new ArrayList();
         refreshPlayerListBox();
         setTitle(GametableApp.VERSION);
     }
 
     public void eraseAllLines()
     {
         // erase with a rect big enough to nail everything
         Rectangle toErase = new Rectangle();
 
         toErase.x = Integer.MIN_VALUE / 2;
         toErase.y = Integer.MIN_VALUE / 2;
         toErase.width = Integer.MAX_VALUE;
         toErase.height = Integer.MAX_VALUE;
 
         // go to town
         m_gametableCanvas.erase(toErase, false, 0);
 
         repaint();
     }
 
     public void eraseAllPogs()
     {
         // make an int array of all the IDs
         int removeArray[] = new int[m_gametableCanvas.getSharedMap().getNumPogs()];
 
         for (int i = 0; i < m_gametableCanvas.getSharedMap().getNumPogs(); i++)
         {
             Pog pog = m_gametableCanvas.getSharedMap().getPogAt(i);
             removeArray[i] = pog.m_ID;
         }
 
         m_gametableCanvas.removePogs(removeArray);
     }
 
     public void eraseAll()
     {
         eraseAllLines();
         eraseAllPogs();
     }
 
     public void updateHexModeMenuItem()
     {
         if (this.m_gametableCanvas.m_bHexMode)
         {
             m_hexModeMenuItem.setState(true);
         }
         else
         {
             m_hexModeMenuItem.setState(false);
         }
     }
 
     public void actionPerformed(ActionEvent e)
     {
     	if (e.getSource() == m_clearMapMenuItem)
     	{
             int res = UtilityFunctions.yesNoDialog(this,
                     "This will clear all lines, pogs, and underlays on the entire map. Are you sure?", "Clear Map");
             if (res == UtilityFunctions.YES)
             {
                 eraseAllPogs();
                 eraseAllLines();
                 repaint();
             }
     	}
     	
         if (e.getSource() == m_colorCombo)
         {
             Integer col = (Integer)m_colorCombo.getSelectedItem();
             m_drawColor = new Color(col.intValue());
         }
 
         if (e.getSource() == m_exitMenuItem)
         {
             saveState(new File("autosave.grm"));
             savePrefs();
             System.exit(0);
         }
         if (e.getSource() == m_openMenuItem)
         {
             m_actingFile = UtilityFunctions.doFileOpenDialog("Open", "grm", true);
 
             int result = UtilityFunctions
                 .yesNoDialog(
                     this,
                     "This will load a map file, replacing all existing map data for you and all players in the session. Are you sure you want to do this?",
                     "Confirm Load");
             if (result == UtilityFunctions.YES)
             {
 
                 if (m_actingFile != null)
                 {
                     // clear the state
                     eraseAll();
 
                     // load
                     if (m_netStatus == NETSTATE_JOINED)
                     {
                         // joiners dispatch the save file to the host
                         // for processing
                         byte grmFile[] = UtilityFunctions.loadFileToArray(m_actingFile);
                         if (grmFile != null)
                         {
                             push(PacketManager.makeGrmPacket(grmFile));
                         }
                     }
                     else
                     {
                         // actually do the load if we're the host or offline
                         loadState(m_actingFile);
                     }
 
                     postSystemMessage(getMePlayer().getPlayerName() + " loads a new map.");
                 }
             }
         }
         if (e.getSource() == m_saveMenuItem)
         {
             if (m_actingFile == null)
             {
                 m_actingFile = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
             }
 
             if (m_actingFile != null)
             {
                 // save the file
                 saveState(m_actingFile);
             }
         }
         if (e.getSource() == m_saveAsMenuItem)
         {
             m_actingFile = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
             if (m_actingFile != null)
             {
                 // save the file
                 saveState(m_actingFile);
             }
         }
         if (e.getSource() == m_reacquirePogsMenuItem)
         {
         	// get the pogs stuff again. And refresh
         	m_pogsPanel.reaquirePogs();
        	m_underlaysPanel.reaquirePogs();
         }
 
         if (e.getSource() == m_hostMenuItem)
         {
             host();
         }
         if (e.getSource() == m_joinMenuItem)
         {
             join();
         }
         if (e.getSource() == m_disconnect)
         {
             disconnect();
         }
         if (e.getSource() == m_aboutMenuItem)
         {
             UtilityFunctions.msgBox(this, GametableApp.VERSION + " by Andy Weir and David Ghandehari", "Version");
         }
         if (e.getSource() == m_hexModeMenuItem)
         {
             m_gametableCanvas.m_bHexMode = !m_gametableCanvas.m_bHexMode;
             push(PacketManager.makeHexModePacket(m_gametableCanvas.m_bHexMode));
             repaint();
             updateHexModeMenuItem();
             postSystemMessage(getMePlayer().getPlayerName() + " changes the grid mode.");
         }
 
         if (e.getSource() == m_recenter)
         {
             int result = UtilityFunctions.yesNoDialog(this, "This will recenter everyone's map view to match yours, "
                 + "and will set their zoom levels to match yours. Are you sure you want to do this?", "Recenter?");
             if (result == UtilityFunctions.YES)
             {
                 // get our view center
                 int viewCenterX = m_gametableCanvas.getWidth() / 2;
                 int viewCenterY = m_gametableCanvas.getHeight() / 2;
 
                 // convert to model coordinates
                 Point modelCenter = m_gametableCanvas.viewToModel(viewCenterX, viewCenterY);
                 m_gametableCanvas.recenterView(modelCenter.x, modelCenter.y, m_gametableCanvas.m_zoom);
                 postSystemMessage(getMePlayer().getPlayerName() + " Recenters everyone's view!");
             }
         }
 
         if (e.getSource() == m_addDiceMacroMenuItem)
         {
             NewMacroDialog dlg = new NewMacroDialog();
             dlg.setModal(true);
             dlg.setVisible(true);
 
             if (dlg.m_bAccepted)
             {
                 // extrace the macro from the controls and add it
                 String name = dlg.m_nameEntry.getText();
                 String macro = dlg.m_rollEntry.getText();
                 addMacroButton(name, macro);
             }
         }
 
         if (e.getSource() == m_removeDiceMacroMenuItem)
         {
             Object[] list = m_macros.toArray();
             // give them a list of macros they can delete
             Object sel = JOptionPane.showInputDialog(this, "Select Dice Macro to remove:", "Remove Dice Macro",
                 JOptionPane.PLAIN_MESSAGE, null, list, list[0]);
             if (sel != null)
             {
                 m_macros.remove(sel);
                 rebuildMacroButtons();
             }
         }
 
         // check the macro buttons
         for (int i = 0; i < m_macroButtons.size(); i++)
         {
             if (e.getSource() == m_macroButtons.get(i))
             {
                 // found our man
                 DiceMacro macro = (DiceMacro)m_macros.get(i);
                 String result = macro.doMacro();
                 postMessage(result);
 
                 // send focus back where it belongs
                 m_gametableCanvas.requestFocus();
             }
         }
     }
 
     public void propertyChange(PropertyChangeEvent evt)
     {
         if (evt.getPropertyName().equals("dividerLocation"))
         {
             if (m_bDisregardDividerChanges)
             {
                 return;
             }
 
             // note the new divider location
             int fromBottom = m_mapChatSplitPane.getHeight() - m_mapChatSplitPane.getDividerLocation();
             if (fromBottom == 16)
             {
                 m_bDisregardDividerChanges = true;
                 m_mapChatSplitPane.setDividerLocation(getHeight() - m_prevDividerLocFromBottom);
                 m_bDisregardDividerChanges = false;
                 return;
             }
             m_prevDividerLocFromBottom = fromBottom;
         }
     }
 
     public void paint(Graphics g)
     {
         if (m_bFirstPaint)
         {
             if (!m_bLoadedState)
             {
                 m_mapChatSplitPane.setDividerLocation(0.7);
                 m_prevDividerLocFromBottom = m_mapChatSplitPane.getHeight() - m_mapChatSplitPane.getDividerLocation();
             }
 
             m_bFirstPaint = false;
             m_bDisregardDividerChanges = false;
             m_gametableCanvas.requestFocus();
 
             repaint();
         }
         super.paint(g);
     }
 
     public ToolManager getToolManager()
     {
         return m_toolManager;
     }
 
     public void setToolSelected(int toolId)
     {
         m_toolButtons[toolId].setSelected(true);
     }
 
     public void addPlayer(Object player)
     {
         m_players.add(player);
         refreshPlayerListBox();
     }
 
     public void refreshPlayerListBox()
     {
         Object[] playerList = m_players.toArray();
         m_playerList = new JList(playerList);
         PlayerListCellRenderer renderer = new PlayerListCellRenderer();
         m_playerList.setCellRenderer(renderer);
         m_playerListScrollPane.getViewport().add(m_playerList, null);
     }
 
     public void addMacroButton(String name, String macro)
     {
         DiceMacro newMacro = new DiceMacro();
         boolean res = newMacro.init(macro, name);
         if (!res)
         {
             logSystemMessage("Error in macro");
             return;
         }
         addMacroButton(newMacro);
     }
 
     public void addMacroButton(DiceMacro dm)
     {
         m_macros.add(dm);
         rebuildMacroButtons();
     }
 
     public void rebuildMacroButtons()
     {
         m_macroButtonsPanel.removeAll();
         m_macroButtons = new ArrayList();
 
         for (int i = 0; i < m_macros.size(); i++)
         {
             DiceMacro dm = (DiceMacro)m_macros.get(i);
 
             JButton newButton = new JButton();
             newButton.setMaximumSize(new Dimension(83, 20));
             newButton.setMinimumSize(new Dimension(83, 20));
             newButton.setPreferredSize(new Dimension(83, 20));
             newButton.setText(dm.toString());
             newButton.addActionListener(this);
 
             m_macroButtons.add(newButton);
             m_macroButtonsPanel.add(newButton);
         }
 
         m_macroButtonsPanel.revalidate();
 
         repaint();
     }
 
     public void logSystemMessage(String text)
     {
         logMessage(">>> " + text);
     }
 
     public void logAlertMessage(String text)
     {
         logMessage("!!! " + text);
     }
 
     public void logMessage(String text)
     {
         double prevHeight = m_textLog.getPreferredSize().getHeight();
         String log = m_textLog.getText();
         log += text;
         log += "\n";
         m_textLog.setText(log);
         double scrollerHeight = m_textLogScroller.getHeight();
 
         if (prevHeight > scrollerHeight)
         {
             m_textLogScroller.disregardNextPaint();
         }
     }
 
     protected void processWindowEvent(WindowEvent e)
     {
         super.processWindowEvent(e);
         if (e.getID() == WindowEvent.WINDOW_CLOSING)
         {
             saveState(new File("autosave.grm"));
             savePrefs();
             System.exit(0);
         }
     }
 
     public void keyTyped(KeyEvent e)
     {
     }
 
     public void keyPressed(KeyEvent e)
     {
         if (e.getSource() == m_textEntry)
         {
             int code = e.getKeyCode();
 
             if (code == KeyEvent.VK_UP)
             {
                 m_textSentLoc--;
                 if (m_textSentLoc < 0)
                 {
                     m_textSentLoc = 0;
                 }
                 else
                 {
                     m_textEntry.setText((String)m_textSent.get(m_textSentLoc));
                 }
             }
 
             if (code == KeyEvent.VK_DOWN)
             {
                 m_textSentLoc++;
 
                 if (m_textSentLoc > m_textSent.size())
                 {
                     m_textSentLoc = m_textSent.size();
                 }
                 else
                 {
                     if (m_textSentLoc == m_textSent.size())
                     {
                         m_textEntry.setText("");
                     }
                     else
                     {
                         m_textEntry.setText((String)m_textSent.get(m_textSentLoc));
                     }
                 }
             }
         }
     }
 
     public void keyReleased(KeyEvent e)
     {
     }
 
     void m_textEntry_actionPerformed(ActionEvent e)
     {
         // they hit return on the text bar
         String entered = m_textEntry.getText();
         if (entered.length() == 0)
         {
             // useless string.
             // return focus to the map
             m_gametableCanvas.requestFocus();
             return;
         }
 
         m_textSent.add(entered);
         m_textSentLoc = m_textSent.size();
 
         // parse for commands
         if (entered.charAt(0) == '/')
         {
             parseSlashCommand(entered);
         }
         else
         {
             postMessage(getMePlayer().getCharacterName() + "> " + entered);
         }
 
         m_textEntry.setText("");
     }
 
     public void parseSlashCommand(String text)
     {
         // get the command
         String[] words = breakIntoWords(text);
         if (words == null)
         {
             return;
         }
 
         // macro command
         if (words[0].equals("/macro"))
         {
             // macro command. this requires at least 2 parameters
             if (words.length < 3)
             {
                 // tell them the usage and bail
                 logSystemMessage("/macro usage: /macro macroName <dice roll in standard format>");
                 logSystemMessage("Examples: /macro Attack d20+8 ; /macro SneakDmg d4 + 2 + 4d6");
                 return;
             }
 
             // the second word is the name
             String name = words[1];
 
             // all subsiquent "words" are the die roll macro
             String dieStr = "";
             for (int i = 2; i < words.length; i++)
             {
                 dieStr += words[i];
                 dieStr += " ";
             }
 
             addMacroButton(name, dieStr);
         }
 
         // macro delete command
         if (words[0].equals("/macrodelete"))
         {
             // req. 1 param
             if (words.length == 1)
             {
                 logSystemMessage("/macrodelete usage: /macrodelete macroName (Case sensitive)");
                 return;
             }
 
             String name = words[1];
 
             // find and kill this macro
             for (int i = 0; i < m_macros.size(); i++)
             {
                 DiceMacro dm = (DiceMacro)m_macros.get(i);
                 if (dm.toString().equals(name))
                 {
                     // killify
                     m_macros.remove(i);
                     break;
                 }
             }
 
             rebuildMacroButtons();
         }
 
         if (words[0].equals("/roll"))
         {
             // req. 1 param
             if (words.length == 1)
             {
                 logSystemMessage("/roll usage: /roll <dice roll in standard format>");
                 logSystemMessage("Example: /roll 2d6 + 3d4 + 8");
                 return;
             }
 
             // all subsiquent params become the roll string
             String dieStr = "";
             for (int i = 1; i < words.length; i++)
             {
                 dieStr += words[i];
                 dieStr += " ";
             }
 
             DiceMacro dm = new DiceMacro();
             boolean res = dm.init(dieStr, "anon");
             if (res)
             {
                 String result = dm.doMacro();
                 postMessage(result);
             }
             else
             {
                 logSystemMessage("Invalid dice command.");
             }
         }
 
         if (words[0].equals("//") || words[0].equals("/help"))
         {
             // list macro commands
             logSystemMessage("/macro: macro a die roll");
             logSystemMessage("/macrodelete: deletes an unwanted macro");
             logSystemMessage("/roll: roll dice");
             logSystemMessage("// list all slash commands");
         }
     }
 
     public String[] breakIntoWords(String line)
     {
         boolean bDone = false;
         List words = new ArrayList();
         int start = 0;
         int end;
         while (!bDone)
         {
             end = line.indexOf(" ", start);
             String newWord;
             if (end == -1)
             {
                 bDone = true;
                 newWord = line.substring(start);
                 words.add(newWord);
             }
             else
             {
                 newWord = line.substring(start, end);
                 start = end + 1;
                 words.add(newWord);
             }
         }
 
         if (words.size() == 0)
         {
             return null;
         }
 
         String[] ret = new String[words.size()];
         for (int i = 0; i < ret.length; i++)
         {
             ret[i] = (String)words.get(i);
         }
 
         return ret;
     }
 
     public void postSystemMessage(String toSay)
     {
         postMessage(">>> " + toSay);
     }
 
     public void postAlertMessage(String toSay)
     {
         postMessage("!!! " + toSay);
     }
 
     public void postMessage(String toSay)
     {
         if (m_netStatus == NETSTATE_HOST)
         {
             // if you're the host, push to all players
             push(PacketManager.makeTextPacket(toSay));
 
             // add it to your own text log
             logMessage(toSay);
         }
         else if (m_netStatus == NETSTATE_JOINED)
         {
             // if you're a player, just post it to the GM
             push(PacketManager.makeTextPacket(toSay));
         }
         else
         {
             // if you're offline, just add it to the log
             logMessage(toSay);
         }
     }
 
     public void savePrefs()
     {
         try
         {
             FileOutputStream prefFile = new FileOutputStream("prefs.prf");
             DataOutputStream prefDos = new DataOutputStream(prefFile);
 
             prefDos.writeUTF(m_defaultName);
             prefDos.writeUTF(m_defaultCharName);
             prefDos.writeUTF(m_defaultIP);
             prefDos.writeInt(m_defaultPort);
             prefDos.writeUTF(m_defaultPassword);
             prefDos.writeInt(m_gametableCanvas.getSharedMap().getScrollX());
             prefDos.writeInt(m_gametableCanvas.getSharedMap().getScrollY());
             prefDos.writeInt(m_gametableCanvas.m_zoom);
 
             prefDos.writeInt(m_windowSize.width);
             prefDos.writeInt(m_windowSize.height);
             prefDos.writeInt(m_windowPos.x);
             prefDos.writeInt(m_windowPos.y);
             prefDos.writeBoolean(m_bMaximized);
 
             // divider locations
             prefDos.writeInt(m_mapChatSplitPane.getDividerLocation());
             prefDos.writeInt(m_mapPogSplitPane.getDividerLocation());
 
             prefDos.writeInt(m_macros.size());
             for (int i = 0; i < m_macros.size(); i++)
             {
                 DiceMacro dm = (DiceMacro)m_macros.get(i);
                 dm.writeToStream(prefDos);
             }
 
             prefDos.close();
             prefFile.close();
         }
         catch (FileNotFoundException ex1)
         {
             Log.log(Log.SYS, ex1);
         }
         catch (IOException ex1)
         {
             Log.log(Log.SYS, ex1);
         }
     }
 
     public void loadPrefs()
     {
         try
         {
             FileInputStream prefFile = new FileInputStream("prefs.prf");
             DataInputStream prefDis = new DataInputStream(prefFile);
 
             m_defaultName = prefDis.readUTF();
             m_defaultCharName = prefDis.readUTF();
             m_defaultIP = prefDis.readUTF();
             m_defaultPort = prefDis.readInt();
             m_defaultPassword = prefDis.readUTF();
             m_gametableCanvas.getSharedMap().setScroll(prefDis.readInt(), prefDis.readInt());
             m_gametableCanvas.setZoom(prefDis.readInt());
 
             m_windowSize.width = prefDis.readInt();
             m_windowSize.height = prefDis.readInt();
             m_windowPos.x = prefDis.readInt();
             m_windowPos.y = prefDis.readInt();
             m_bMaximized = prefDis.readBoolean();
             setSize(m_windowSize);
             setLocation(m_windowPos);
             if (m_bMaximized)
             {
                 setExtendedState(MAXIMIZED_BOTH);
             }
             else
             {
                 setExtendedState(NORMAL);
             }
 
             // divider locations
             m_bDisregardDividerChanges = true;
             m_mapChatSplitPane.setDividerLocation(prefDis.readInt());
             m_mapPogSplitPane.setDividerLocation(prefDis.readInt());
             m_bDisregardDividerChanges = false;
 
             m_macros = new ArrayList();
             int numMacros = prefDis.readInt();
             for (int i = 0; i < numMacros; i++)
             {
                 DiceMacro dm = new DiceMacro();
                 dm.initFromStream(prefDis);
                 addMacroButton(dm);
             }
 
             prefDis.close();
             prefFile.close();
 
             m_bLoadedState = true;
         }
         catch (FileNotFoundException ex1)
         {
             Log.log(Log.SYS, ex1);
         }
         catch (IOException ex1)
         {
             Log.log(Log.SYS, ex1);
         }
     }
 
     public void saveState(File file)
     {
         // save out all our data. The best way to do this is with packets, cause they're
         // already designed to pass data around.
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(baos);
 
         try
         {
             LineSegment[] lines = new LineSegment[m_gametableCanvas.getSharedMap().getNumLines()];
             for (int i = 0; i < m_gametableCanvas.getSharedMap().getNumLines(); i++)
             {
                 lines[i] = m_gametableCanvas.getSharedMap().getLineAt(i);
             }
             byte[] linesPacket = PacketManager.makeLinesPacket(lines);
             dos.writeInt(linesPacket.length);
             dos.write(linesPacket);
 
             // pogs
             for (int i = 0; i < m_gametableCanvas.getSharedMap().getNumPogs(); i++)
             {
                 Pog pog = m_gametableCanvas.getSharedMap().getPogAt(i);
                 byte[] pogsPacket = PacketManager.makeAddPogPacket(pog);
                 dos.writeInt(pogsPacket.length);
                 dos.write(pogsPacket);
             }
 
             // hex state
             byte hexModePacket[] = PacketManager.makeHexModePacket(m_gametableCanvas.m_bHexMode);
             dos.writeInt(hexModePacket.length);
             dos.write(hexModePacket);
 
             byte[] saveFileData = baos.toByteArray();
             FileOutputStream output = new FileOutputStream(file);
             DataOutputStream fileOut = new DataOutputStream(output);
             fileOut.writeInt(saveFileData.length);
             fileOut.write(saveFileData);
             output.close();
             fileOut.close();
             baos.close();
             dos.close();
         }
         catch (IOException ex)
         {
             Log.log(Log.SYS, ex);
             // failed to save. give up
         }
     }
 
     public void loadState(File file)
     {
         try
         {
             FileInputStream input = new FileInputStream(file);
             DataInputStream infile = new DataInputStream(input);
 
             // get the big hunk o daya
             int len = infile.readInt();
             byte[] saveFileData = new byte[len];
             infile.read(saveFileData);
 
             loadState(saveFileData);
 
             input.close();
             infile.close();
         }
         catch (FileNotFoundException ex)
         {
             Log.log(Log.SYS, ex);
         }
         catch (IOException ex)
         {
             Log.log(Log.SYS, ex);
         }
     }
 
     public void loadStateFromRawFileData(byte rawFileData[])
     {
         byte saveFileData[] = new byte[rawFileData.length - 4]; // a new array that lacks the first
         // int
         System.arraycopy(rawFileData, 4, saveFileData, 0, saveFileData.length);
         loadState(saveFileData);
     }
 
     public void loadState(byte saveFileData[])
     {
         try
         {
             // now we have to pick out the packets and send them in for processing one at a time
             DataInputStream walker = new DataInputStream(new ByteArrayInputStream(saveFileData));
             int read = 0;
             while (read < saveFileData.length)
             {
                 int packetLen = walker.readInt();
                 read += 4;
 
                 byte[] packet = new byte[packetLen];
                 walker.read(packet);
                 read += packetLen;
 
                 // dispatch the packet
                 PacketManager.readPacket(null, packet);
             }
         }
         catch (FileNotFoundException ex)
         {
             Log.log(Log.SYS, ex);
         }
         catch (IOException ex)
         {
             Log.log(Log.SYS, ex);
         }
 
         repaint();
     }
 }
 
 
 
 class GametableFrame_m_textEntry_actionAdapter implements java.awt.event.ActionListener
 {
     GametableFrame adaptee;
 
     GametableFrame_m_textEntry_actionAdapter(GametableFrame a)
     {
         adaptee = a;
     }
 
     public void actionPerformed(ActionEvent e)
     {
         adaptee.m_textEntry_actionPerformed(e);
     }
 }
