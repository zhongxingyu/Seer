 /*
  * Copyright (c) Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.gui.recordings;
 
 import static lazybones.LazyBones.getTranslation;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.JTree;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import lazybones.LazyBones;
 import lazybones.Player;
 import lazybones.RecordingManager;
 import lazybones.VDRCallback;
 import lazybones.actions.DeleteRecordingAction;
 import lazybones.actions.GetRecordingDetailsAction;
 import lazybones.gui.components.RecordingDetailsPanel;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.responses.highlevel.Folder;
 import org.hampelratte.svdrp.responses.highlevel.Recording;
 import org.hampelratte.svdrp.responses.highlevel.TreeNode;
 import org.hampelratte.svdrp.sorting.RecordingAlphabeticalComparator;
 import org.hampelratte.svdrp.sorting.RecordingIsCutComparator;
 import org.hampelratte.svdrp.sorting.RecordingIsNewComparator;
 import org.hampelratte.svdrp.sorting.RecordingStarttimeComparator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import util.ui.Localizer;
 
 public class RecordingManagerPanel extends JPanel implements ActionListener, ItemListener {
 
     private static final long serialVersionUID = 1L;
 
     private static transient Logger logger = LoggerFactory.getLogger(RecordingManagerPanel.class);
 
     private final JComboBox sortStrategySelector = new JComboBox();
     private final JToggleButton orderSelector = new JToggleButton();
 
     private JScrollPane scrollPane = null;
     private final RecordingTreeModel recordingTreeModel = new RecordingTreeModel();
     private final JTree recordingTree = new JTree();
 
     private final RecordingDetailsPanel recordingDetailsPanel = new RecordingDetailsPanel();
     private JButton buttonSync = null;
     private JButton buttonRemove = null;
 
     private JButton expandAll = null;
     private JButton collapseAll = null;
 
     private final JPopupMenu popup = new JPopupMenu();
 
     public RecordingManagerPanel() {
         initGUI();
         loadSettings();
     }
 
     private void loadSettings() {
         boolean ascending = Boolean.parseBoolean(LazyBones.getProperties().getProperty("recording.order.ascending", "true"));
         int strategyIndex = Integer.parseInt(LazyBones.getProperties().getProperty("recording.order.strategy", "0"));
         sortStrategySelector.setSelectedIndex(strategyIndex);
         orderSelector.setSelected(!ascending);
 
         SortStrategy strategy = (SortStrategy) sortStrategySelector.getSelectedItem();
         recordingTreeModel.sortBy(strategy.getComparator(), ascending);
     }
 
     /**
      * This method initializes the GUI
      * 
      */
     private void initGUI() {
         createContextMenu();
 
         this.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
         int y = 0;
         gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gbc.gridx = 0;
         gbc.gridy = y++;
         gbc.weightx = 1.0;
         gbc.weighty = 0;
         gbc.gridwidth = 2;
         gbc.insets = new java.awt.Insets(0, 5, 0, 5);
         JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
         sortPanel.add(sortStrategySelector);
         sortStrategySelector.addItem(new SortStrategy(new RecordingAlphabeticalComparator(), getTranslation("sort.alphabetical", "alphabetical")));
         sortStrategySelector.addItem(new SortStrategy(new RecordingStarttimeComparator(), getTranslation("sort.chronological", "chronological")));
         sortStrategySelector.addItem(new SortStrategy(new RecordingIsNewComparator(), getTranslation("sort.new_recordings", "new recordings")));
         sortStrategySelector.addItem(new SortStrategy(new RecordingIsCutComparator(), getTranslation("sort.cut_recordings", "cut recordings")));
         sortStrategySelector.addItemListener(this);
         sortPanel.add(orderSelector);
         orderSelector.setIcon(LazyBones.getInstance().getIcon("lazybones/sort_ascending.png"));
         orderSelector.setSelectedIcon(LazyBones.getInstance().getIcon("lazybones/sort_descending.png"));
         orderSelector.setToolTipText(getTranslation("sort.ascending", "ascending"));
         orderSelector.setPreferredSize(new Dimension(26, 26));
         orderSelector.addActionListener(this);
 
         expandAll = new JButton(LazyBones.getInstance().getIcon("lazybones/list-add.png"));
         expandAll.addActionListener(this);
         expandAll.setPreferredSize(new Dimension(26, 26));
         expandAll.setToolTipText(getTranslation("expand_all", "expand all"));
         sortPanel.add(expandAll);
         collapseAll = new JButton(LazyBones.getInstance().getIcon("lazybones/list-remove.png"));
         collapseAll.addActionListener(this);
         collapseAll.setPreferredSize(new Dimension(26, 26));
         collapseAll.setToolTipText(getTranslation("collapse_all", "collapse all"));
         sortPanel.add(collapseAll);
 
         this.add(sortPanel, gbc);
 
         gbc.fill = java.awt.GridBagConstraints.BOTH;
         gbc.gridx = 0;
         gbc.gridy = y++;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.insets = new java.awt.Insets(0, 10, 10, 10);
         gbc.gridwidth = 1;
         recordingTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         recordingTree.setRootVisible(true);
         recordingTree.addTreeSelectionListener(new TreeSelectionListener() {
             @Override
             public void valueChanged(TreeSelectionEvent e) {
                 TreeNode selected = (TreeNode) e.getPath().getLastPathComponent();
                 if (selected instanceof Recording) {
                     buttonRemove.setEnabled(true);
                 } else {
                     buttonRemove.setEnabled(false);
                 }
             }
         });
 
         // initialize out special tree renderer
         recordingTree.setRowHeight(25);
         boolean renderBackground = UIManager.getDefaults().getBoolean("Tree.rendererFillBackground");
         UIManager.getDefaults().put("Tree.rendererFillBackground", false);
         recordingTree.setCellRenderer(new RecordingTreeRenderer());
         UIManager.getDefaults().put("Tree.rendererFillBackground", renderBackground);
 
         recordingTree.setModel(recordingTreeModel);
         scrollPane = new JScrollPane(recordingTree);
         scrollPane.getVerticalScrollBar().setUnitIncrement(10);
         this.add(scrollPane, gbc);
         ToolTipManager.sharedInstance().registerComponent(recordingTree);
 
         // gbc.fill = java.awt.GridBagConstraints.VERTICAL;
         gbc.gridx = 1;
         gbc.weightx = .1;
         gbc.insets = new java.awt.Insets(0, 0, 10, 10);
         recordingTree.addTreeSelectionListener(recordingDetailsPanel);
         recordingDetailsPanel.setBorder(BorderFactory.createTitledBorder(getTranslation("details", "Details")));
         recordingDetailsPanel.setPreferredSize(new Dimension(300, 300));
         recordingDetailsPanel.setMinimumSize(new Dimension(300, 300));
         recordingDetailsPanel.setMaximumSize(new Dimension(300, 300));
         this.add(recordingDetailsPanel, gbc);
 
         gbc.insets = new java.awt.Insets(0, 10, 10, 10);
         gbc.gridx = 0;
         gbc.gridy = y++;
         gbc.weighty = 0;
         buttonSync = new JButton(getTranslation("resync", "Synchronize"));
         buttonSync.setIcon(LazyBones.getInstance().createImageIcon("action", "view-refresh", 16));
         buttonSync.addActionListener(this);
         buttonSync.setActionCommand("SYNC");
         this.add(buttonSync, gbc);
 
         gbc.insets = new java.awt.Insets(0, 0, 10, 10);
         gbc.gridx = 1;
         buttonRemove = new JButton(getTranslation("delete_recording", "Delete Recording"));
         buttonRemove.setIcon(LazyBones.getInstance().createImageIcon("action", "edit-delete", 16));
         buttonRemove.addActionListener(this);
         buttonRemove.setActionCommand("DELETE");
         this.add(buttonRemove, gbc);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         Recording rec = null;
         boolean itemSelected = false;
         TreePath selection = recordingTree.getSelectionPath();
         if (selection != null) {
             itemSelected = true;
             TreeNode treeNode = (TreeNode) selection.getLastPathComponent();
             if (treeNode instanceof Recording) {
                 rec = (Recording) treeNode;
             }
         }
 
         if ("DELETE".equals(e.getActionCommand()) && itemSelected) {
             deleteRecording(rec, selection);
         } else if ("INFO".equals(e.getActionCommand()) && itemSelected) {
             createRecordingDetailsDialog(rec);
         } else if ("SYNC".equals(e.getActionCommand())) {
             RecordingManager.getInstance().synchronize();
         } else if ("PLAY".equals(e.getActionCommand()) && itemSelected) {
             Player.play(rec);
         } else if ("PLAY_ON_VDR".equals(e.getActionCommand()) && itemSelected) {
             RecordingManager.getInstance().playOnVdr(rec);
         } else if (e.getSource() == orderSelector) {
             SortStrategy strategy = (SortStrategy) sortStrategySelector.getSelectedItem();
             boolean ascending = !orderSelector.isSelected();
             recordingTreeModel.sortBy(strategy.getComparator(), ascending);
             orderSelector.setToolTipText(ascending ? getTranslation("sort.ascending", "ascending") : getTranslation("sort.descending", "descending"));
             LazyBones.getProperties().setProperty("recording.order.ascending", Boolean.toString(ascending));
         } else if (e.getSource() == expandAll) {
             expandAll(recordingTree, true);
         } else if (e.getSource() == collapseAll) {
             expandAll(recordingTree, false);
         }
     }
 
     private void deleteRecording(final Recording rec, final TreePath tp) {
         if (tp.getLastPathComponent() instanceof Recording) {
             scrollPane.setEnabled(false);
             recordingTree.setEnabled(false);
             buttonRemove.setEnabled(false);
             buttonSync.setEnabled(false);
             final boolean hasFocus = recordingTree.hasFocus();
             VDRCallback<DeleteRecordingAction> callback = new VDRCallback<DeleteRecordingAction>() {
                 @Override
                 public void receiveResponse(DeleteRecordingAction cmd, Response response) {
                     if (!cmd.isSuccess()) {
                         logger.error(cmd.getResponse().getMessage());
                         recordingTree.setEnabled(true);
                         buttonRemove.setEnabled(true);
                         buttonSync.setEnabled(true);
                         if (hasFocus) {
                             recordingTree.requestFocus();
                         }
                     } else {
                         // RecordingManager.getInstance().getRecordings().remove(rec);
                         // recordingTreeModel.remove(tp);
 
                         RecordingManager.getInstance().synchronize(new Runnable() {
                             @Override
                             public void run() {
                                 scrollPane.setEnabled(true);
                                 recordingTree.setEnabled(true);
                                 buttonRemove.setEnabled(true);
                                 buttonSync.setEnabled(true);
                                 if (hasFocus) {
                                     recordingTree.requestFocus();
                                 }
                             }
                         });
                     }
                 }
             };
             DeleteRecordingAction dra = new DeleteRecordingAction(rec, callback);
             dra.enqueue();
         }
     }
 
     private void createRecordingDetailsDialog(Recording rec) {
         VDRCallback<GetRecordingDetailsAction> callback = new VDRCallback<GetRecordingDetailsAction>() {
             @Override
             public void receiveResponse(GetRecordingDetailsAction cmd, Response response) {
                 if (cmd.isSuccess()) {
                     final JDialog dialog = new JDialog();
                     dialog.getContentPane().add(new RecordingDetailsPanel(cmd.getRecording()));
                     dialog.setSize(400, 300);
                     dialog.setLocation(LazyBones.getInstance().getMainDialog().getLocation());
                     dialog.setVisible(true);
                     dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                 }
             }
         };
         GetRecordingDetailsAction grda = new GetRecordingDetailsAction(rec, callback);
         grda.enqueue();
     }
 
     private void createContextMenu() {
         JMenuItem menuDelete = new JMenuItem(Localizer.getLocalization(Localizer.I18N_DELETE));
         menuDelete.addActionListener(this);
         menuDelete.setActionCommand("DELETE");
         menuDelete.setIcon(LazyBones.getInstance().createImageIcon("actions", "edit-delete", 16));
         JMenuItem menuInfo = new JMenuItem(getTranslation("recording_info", "Show information"));
         menuInfo.addActionListener(this);
         menuInfo.setActionCommand("INFO");
         menuInfo.setIcon(LazyBones.getInstance().createImageIcon("actions", "edit-find", 16));
         JMenu menuPlay = new JMenu(getTranslation("playback", "Playback"));
         menuPlay.setIcon(LazyBones.getInstance().createImageIcon("actions", "media-playback-start", 16));
         JMenuItem menuPlayLocal = new JMenuItem(getTranslation("playback.local", "Play"));
         menuPlayLocal.addActionListener(this);
         menuPlayLocal.setActionCommand("PLAY");
         menuPlayLocal.setIcon(LazyBones.getInstance().createImageIcon("actions", "media-playback-start", 16));
         JMenuItem menuPlayOnVdr = new JMenuItem(getTranslation("playback.vdr", "Play on VDR"));
         menuPlayOnVdr.addActionListener(this);
         menuPlayOnVdr.setActionCommand("PLAY_ON_VDR");
         menuPlayOnVdr.setIcon(LazyBones.getInstance().createImageIcon("actions", "media-playback-start", 16));
         menuPlay.add(menuPlayLocal);
         menuPlay.add(menuPlayOnVdr);
         JMenuItem menuSync = new JMenuItem(getTranslation("resync", "Synchronize with VDR"));
         menuSync.addActionListener(this);
         menuSync.setActionCommand("SYNC");
         menuSync.setIcon(LazyBones.getInstance().createImageIcon("actions", "view-refresh", 16));
         popup.add(menuInfo);
         popup.add(menuPlay);
         popup.add(menuDelete);
         popup.add(menuSync);
 
         recordingTree.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 mayTriggerPopup(e);
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 mayTriggerPopup(e);
             }
 
             private void mayTriggerPopup(MouseEvent e) {
                 if (recordingTree.isEnabled()) {
                     if (e.isPopupTrigger()) {
                         Point p = e.getPoint();
                         int selectedRow = recordingTree.getRowForLocation(p.x, p.y);
                         recordingTree.setSelectionRow(selectedRow);
                         TreeNode treeNode = (TreeNode) recordingTree.getSelectionPath().getLastPathComponent();
                         if (treeNode instanceof Recording) {
                             popup.setLocation(e.getPoint());
                             popup.show(e.getComponent(), e.getX(), e.getY());
                         }
                     }
                 }
             }
         });
 
         recordingTree.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.getSource() == recordingTree) {
                     if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                         TreePath selected = recordingTree.getSelectionPath();
                         if (selected != null) {
                             TreeNode treeNode = (TreeNode) selected.getLastPathComponent();
                             if (treeNode instanceof Folder) {
                                 logger.warn("Deletion of folders is not supported.");
                             } else {
                                 logger.info("Deleting recording [{}]", treeNode.getDisplayTitle());
                                 deleteRecording((Recording) treeNode, selected);
                             }
                         }
                     }
                 }
             }
         });
     }
 
     @Override
     public void itemStateChanged(ItemEvent e) {
         if (e.getStateChange() == ItemEvent.SELECTED) {
             SortStrategy strategy = (SortStrategy) e.getItem();
             boolean ascending = !orderSelector.isSelected();
             recordingTreeModel.sortBy(strategy.getComparator(), ascending);
             LazyBones.getProperties().setProperty("recording.order.strategy", Integer.toString(sortStrategySelector.getSelectedIndex()));
         }
     }
 
     private class SortStrategy {
         private final Comparator<Recording> comparator;
         private final String description;
 
         public SortStrategy(Comparator<Recording> comparator, String description) {
             super();
             this.comparator = comparator;
             this.description = description;
         }
 
         public Comparator<Recording> getComparator() {
             return comparator;
         }
 
         public String getDescription() {
             return description;
         }
 
         @Override
         public String toString() {
             return getDescription();
         }
     }
 
     public void expandAll(JTree tree, boolean expand) {
         TreeNode root = (TreeNode)tree.getModel().getRoot();
 
         // Traverse tree from root
         expandAll(tree, new TreePath(root), expand);
 
         // reopen the root node, after collapse all
         if (!expand) {
             tree.expandPath(new TreePath(root));
         }
     }
 
     private void expandAll(JTree tree, TreePath parent, boolean expand) {
         // Traverse children
         TreeNode node = (TreeNode) parent.getLastPathComponent();
         if (node instanceof Folder) {
             Iterator<TreeNode> iter = ((Folder)node).getChildren().iterator();
             while (iter.hasNext()) {
                 TreeNode n = iter.next();
                 TreePath path = parent.pathByAddingChild(n);
                 expandAll(tree, path, expand);
             }
         }
 
         // Expansion or collapse must be done bottom-up
         if (expand) {
             tree.expandPath(parent);
         } else {
             tree.collapsePath(parent);
         }
     }
 }
