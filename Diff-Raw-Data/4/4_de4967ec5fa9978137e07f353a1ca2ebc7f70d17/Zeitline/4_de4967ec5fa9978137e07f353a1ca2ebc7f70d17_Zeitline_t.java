 package org.Zeitline;
 
 import org.Zeitline.Event.AbstractTimeEvent;
 import org.Zeitline.Event.ComplexEvent;
 import org.Zeitline.Event.Mask.AtomicEventMask;
 import org.Zeitline.Event.Mask.ComplexEventMask;
 import org.Zeitline.GUI.Action.*;
 import org.Zeitline.GUI.Graphics.IIconRepository;
 import org.Zeitline.GUI.Graphics.IconNames;
 import org.Zeitline.Plugin.Input.InputFilter;
 
 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 
 public class Zeitline implements TreeSelectionListener {
     public static final String APPLICATION_NAME = "Zeitline";
     public static final String APPLICATION_VERSION = "v0.3";
 
     protected EventTree tree;
     private ComplexEventMask cem;
     protected AtomicEventMask aem;
     private JSplitPane mainPane;
     private TimelineView timelines;
     protected JToolBar toolBar;
 
     protected int displayMode;
 
     private JFrame frame;
 
     protected JMenuItem menuMoveLeft, menuMoveRight;
 
     private final JFileChooser fileChooser;
     private final List<InputFilter> inputFilters;
     private final IIconRepository<ImageIcon> iconRepository;
 
     protected Action createFrom;
     protected Action createTimelineFrom;
     public Action importAction;
     protected Action moveLeft;
     protected Action moveRight;
     protected Action exitAction;
     protected Action removeEvents;
     protected Action toggleOrphan;
     protected Action clearAction;
     protected Action clearAllAction;
     protected Action filterQueryAction;
     protected Action cutAction;
     public Action pasteAction;
     protected Action findAction;
     protected Action emptyTimeline;
     protected Action deleteTimeline;
     protected Action aboutAction;
 
     protected Action testAction;
     protected Action testAction2;
     private Action saveAction;
     protected Action loadAction;
 
     private Transferable cutBuffer = null;
 
     public Zeitline(List<FileFilter> openFileFilters, List<InputFilter> inputFilters, IIconRepository<ImageIcon> iconRepository) {
         this.inputFilters = inputFilters;
         this.iconRepository = iconRepository;
 
         fileChooser = CreateOpenFileDialog(openFileFilters);
     }
 
     private static JFileChooser CreateOpenFileDialog(List<FileFilter> filters) {
         String currentDir = System.getProperty("user.dir");
         JFileChooser chooser = new JFileChooser(currentDir);
 
         for(final FileFilter filter: filters){
             chooser.addChoosableFileFilter(filter);
         }
 
         return chooser;
     }
 
     public void createAndShowGUI() {
         frame = new JFrame(APPLICATION_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         createMenuActions();
 
         JMenuBar topDropDownMenu = createMenuBar();
         getFrame().setJMenuBar(topDropDownMenu);
 
         Component contents = createComponents();
         getFrame().getContentPane().add(contents, BorderLayout.CENTER);
 
         getFrame().pack();
         getFrame().setSize(800, 600);
         getFrame().setVisible(true);
 
     }
 
     private ImageIcon getIcon(IconNames icon) {
         return iconRepository.getIcon(icon);
     }
 
 
     private void createMenuActions() {
         /* 'File' menu actions */
         saveAction = new SaveAction(this, getIcon(IconNames.FileSave), KeyEvent.VK_S);
         loadAction = new LoadAction(this, getIcon(IconNames.FileOpen), KeyEvent.VK_L);
         exitAction = new ExitAction(KeyEvent.VK_X);
 
         /* 'Edit' menu actions */
         cutAction = new CutAction(this, getIcon(IconNames.EditCut), KeyEvent.VK_T);
         pasteAction = new PasteAction(this, getIcon(IconNames.EditPaste), KeyEvent.VK_P);
         clearAction = new ClearAction(this, KeyEvent.VK_C);
         clearAllAction = new ClearAllAction(this, KeyEvent.VK_A);
         findAction = new FindAction(this, getIcon(IconNames.Find), KeyEvent.VK_D);
 
         /* 'Event' menu actions */
         createFrom = new CreateFromAction(this, getIcon(IconNames.CreateEvent), KeyEvent.VK_C);
         removeEvents = new RemoveEventsAction(this, getIcon(IconNames.DeleteEvent), KeyEvent.VK_R);
         importAction = new ImportAction(this, getIcon(IconNames.Import), KeyEvent.VK_I, inputFilters);
 
         /* 'Timeline' menu actions */
         emptyTimeline = new EmptyTimelineAction(this, getIcon(IconNames.NewTimeline), KeyEvent.VK_E);
         createTimelineFrom = new CreateTimelineFromAction(this, getIcon(IconNames.CreateTimeline), KeyEvent.VK_C);
         deleteTimeline = new DeleteTimelineAction(this, getIcon(IconNames.DeleteTimeline), KeyEvent.VK_D);
         moveLeft = new MoveLeftAction(this, getIcon(IconNames.MoveLeft), KeyEvent.VK_L);
         moveRight = new MoveRightAction(this, getIcon(IconNames.MoveRight), KeyEvent.VK_R);
         filterQueryAction = new FilterQueryAction(this, getIcon(IconNames.Filter), KeyEvent.VK_F);
         toggleOrphan = new ToggleOrphanAction(this, null, KeyEvent.VK_O);
 
         /* 'Help' menu actions */
         aboutAction = new AboutAction(this, KeyEvent.VK_A);
 
         /* Actions for testing new code */
         testAction = new TestAction(this, "TEST", KeyEvent.VK_T);
         testAction2 = new TestAction2(this, "TEST2", KeyEvent.VK_2);
     }
 
     public JMenuBar createMenuBar() {
 
         JMenuBar menuBar;
         JMenu menu;
         List<JMenu> menus = new ArrayList<JMenu>();
         List<Action> actions;
 
         menuBar = new JMenuBar();
 
         actions = asList(saveAction, loadAction, exitAction);
         menus.add(CreateMenu("File", actions, KeyEvent.VK_F));
 
         actions = asList(cutAction, pasteAction, clearAction, clearAllAction, findAction);
         menus.add(CreateMenu("Edit", actions, KeyEvent.VK_E));
 
         actions = asList(createFrom, removeEvents, importAction);
         menus.add(CreateMenu("Event", actions, KeyEvent.VK_N));
 
         actions = asList(emptyTimeline, createTimelineFrom, deleteTimeline, moveLeft, moveRight, filterQueryAction);
         menu = CreateMenu("Timeline", actions, KeyEvent.VK_T);
         JMenuItem menuItem = new JCheckBoxMenuItem(toggleOrphan);
         menu.add(menuItem);
         menus.add(menu);
 
         actions = asList();
         menu = CreateMenu("View", actions, KeyEvent.VK_V);
         JMenu submenu = CreateDateFormatSubMenu();
         menu.add(submenu);
         menus.add(menu);
 
         actions = asList(aboutAction);
         menus.add(CreateMenu("Help", actions, KeyEvent.VK_H));
 
         for(final JMenu menuToAdd: menus){
             menuBar.add(menuToAdd);
         }
 
         return menuBar;
     }
 
     private JMenu CreateDateFormatSubMenu() {
         JMenu submenu;
         JRadioButtonMenuItem rbMenuItem;
         submenu = new JMenu("Time Display");
         submenu.setMnemonic(KeyEvent.VK_D);
 
         ButtonGroup group = new ButtonGroup();
 
        rbMenuItem = new JRadioButtonMenuItem(new SetDisplayModeAction(this, "yyyy-mm-dd hh:mm:ss", KeyEvent.VK_Y, EventTree.DISPLAY_ALL));
         rbMenuItem.setSelected(true);
         group.add(rbMenuItem);
         submenu.add(rbMenuItem);
 
         rbMenuItem = new JRadioButtonMenuItem(new SetDisplayModeAction(this, "hh:mm:ss", KeyEvent.VK_H, EventTree.DISPLAY_HMS));
         group.add(rbMenuItem);
         submenu.add(rbMenuItem);
         return submenu;
     }
 
     private JToolBar createToolBar() {
 
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
 
         toolBar.add(createButton(loadAction));
         toolBar.add(createButton(saveAction));
         toolBar.addSeparator(new Dimension(16, 32));
         toolBar.add(createButton(cutAction));
         toolBar.add(createButton(pasteAction));
         toolBar.add(createButton(findAction));
         toolBar.addSeparator(new Dimension(16, 32));
         toolBar.add(createButton(importAction));
         toolBar.add(createButton(createFrom));
         toolBar.add(createButton(removeEvents));
         toolBar.addSeparator(new Dimension(16, 32));
         toolBar.add(createButton(moveLeft));
         toolBar.add(createButton(moveRight));
         toolBar.add(createButton(filterQueryAction));
         toolBar.add(createButton(emptyTimeline));
         toolBar.add(createButton(createTimelineFrom));
         toolBar.add(createButton(deleteTimeline));
 
         //	toolBar.add(testAction);
         //	toolBar.add(testAction2);
 
         return toolBar;
     }
 
     private Component createComponents() {
 
 //        long ts;
 //        Date afterInsert = new Date();
         toolBar = createToolBar();
 
         // Create panel that contains the Event masks
         JPanel maskOverlay = new JPanel();
         OverlayLayout layoutManager = new OverlayLayout(maskOverlay);
         maskOverlay.setLayout(layoutManager);
 
         cem = new ComplexEventMask();
         aem = new AtomicEventMask();
         maskOverlay.add(cem);
         maskOverlay.add(aem);
         maskOverlay.setMinimumSize(cem.getPreferredSize());
         cem.setVisible(false);
         aem.setVisible(false);
 
         timelines = new TimelineView(this, moveLeft, moveRight,
                 filterQueryAction, deleteTimeline,
                 getSaveAction(), pasteAction,
                 cutAction, clearAction, findAction,
                 getCem(), aem);
 
         mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 getTimelines(), new JScrollPane(maskOverlay));
 
 
         getMainPane().setOneTouchExpandable(true);
         getMainPane().setResizeWeight(1.0);
 
         JPanel mainCanvas = new JPanel(new BorderLayout());
 
         mainCanvas.add(toolBar, BorderLayout.PAGE_START);
         mainCanvas.add(getMainPane(), BorderLayout.CENTER);
 
         Date after = new Date();
         //	System.out.println("Drawing GUI: " + (after.getTime() - afterInsert.getTime()));
 
         return mainCanvas;
 
     }
 
     public void valueChanged(TreeSelectionEvent e) {
         EventTree tree = (EventTree) e.getSource();
 
         int count = tree.getSelectionCount();
 
         if (count != 1)
             pasteAction.setEnabled(false);
         else {
             AbstractTimeEvent te = (AbstractTimeEvent) tree.getLastSelectedPathComponent();
             pasteAction.setEnabled((te instanceof ComplexEvent) &&
                     (getCutBuffer() != null));
         }
 
         // TODO: once drag and drop for tabs works, we can remove
         //       the second condition
         if ((count == 0) || (tree.isPathSelected(new TreePath(new Object[]{tree.getModel().getRoot()})))) {
             cutAction.setEnabled(false);
             createFrom.setEnabled(false);
             createTimelineFrom.setEnabled(false);
             removeEvents.setEnabled(false);
         } else {
             cutAction.setEnabled(true);
             createFrom.setEnabled(true);
             createTimelineFrom.setEnabled(true);
             removeEvents.setEnabled(true);
         }
 
     }
 
     private void AddMenuItem(JMenu menu, Action action) {
         JMenuItem menuItem;
         menuItem = createMenuItem(action);
 
         menu.add(menuItem);
     }
 
     private JMenu CreateMenu(String name, List<Action> actions, int mnemonic) {
         JMenu menu = new JMenu(name);
 
         menu.setMnemonic(mnemonic);
         for(final Action action: actions) {
             AddMenuItem(menu, action);
         }
 
         return menu;
     }
 
     public JButton createButton(Action a) {
         JButton b = new JButton(a);
         b.setText(null);
         b.setMnemonic(0);
         String name = (String) a.getValue(a.NAME);
 
         InputMap imap = b.getInputMap(b.WHEN_IN_FOCUSED_WINDOW);
         KeyStroke ks = (KeyStroke) a.getValue(a.ACCELERATOR_KEY);
         imap.put(ks, name);
 
         return b;
     }
 
     public JMenuItem createMenuItem(Action a) {
 
         JMenuItem m = new JMenuItem(a);
         m.setIcon(null);
 
         return m;
 
     }
 
 
     public ComplexEventMask getCem() {
         return cem;
     }
 
     public JSplitPane getMainPane() {
         return mainPane;
     }
 
     public TimelineView getTimelines() {
         return timelines;
     }
 
     public Action getSaveAction() {
         return saveAction;
     }
 
     public Transferable getCutBuffer() {
         return cutBuffer;
     }
 
     public JFileChooser getFileChooser() {
         return fileChooser;
     }
 
     public void setCutBuffer(Transferable cutBuffer) {
         this.cutBuffer = cutBuffer;
     }
 
     public JFrame getFrame() {
         return frame;
     }
 
 }
