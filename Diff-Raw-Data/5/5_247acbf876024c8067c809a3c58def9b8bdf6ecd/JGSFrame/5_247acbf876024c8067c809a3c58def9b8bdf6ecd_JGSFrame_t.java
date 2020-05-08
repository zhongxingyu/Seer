 package nl.nikhef.jgridstart.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.BorderFactory;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.jdesktop.swingworker.SwingWorker;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateStoreWithDefault;
 import nl.nikhef.jgridstart.gui.util.CertificateSelection;
 import nl.nikhef.jgridstart.gui.util.TemplateButtonPanel;
 import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
 import nl.nikhef.jgridstart.util.PasswordCache;
 
 /** Main jGridstart application window */
 public class JGSFrame extends JFrame {
 
     private JPanel jContentPane = null;
     private JMenuBar jMenuBar = null;
     private JComponent certList = null; 
     private TemplateButtonPanel certInfoPane = null;
     private HashMap<String, JButton> certInfoButtons = null;
 
     private CertificateStoreWithDefault store = null;
     private CertificateSelection selection = null; 
     
     protected int identityIndex = -1;
     protected JMenu identityMenu = null;
     private ButtonGroup identityButtonGroup = null;
     private JSeparator identitySeparator = null;
     
     private AbstractButton viewCertificateList = null;
     
     private CertificatePair lastSelection = null;
     
     /**
      * This is the default constructor
      */
     public JGSFrame() {
 	super();
 	initialize();
     }
 
     /**
      * This method initializes this
      */
     private void initialize() {
 	
 	setName("jgridstart-main-window");
 	
 	store = new CertificateStoreWithDefault();
 	selection = new CertificateSelection(store);
 	PasswordCache.getInstance().setParent(this);
 	URLLauncherCertificate.setSelectionSource(selection);
 	
 	// setup gui
 	this.setMinimumSize(new Dimension(400, 150));
 	this.setPreferredSize(new Dimension(650, 350));
 	this.setContentPane(getJContentPane());
 	this.setTitle("jGridstart "+System.getProperty("jgridstart.version"));
 
 	// create actions; they register themselves
 	new ActionRequest(this, store, selection);
 	new ActionViewRequest(this, selection);
 	new ActionViewVerificationForm(this, selection);
 	new ActionImport(this, store, selection);
 	new ActionInstall(this, selection);
 	//new ActionRevoke(this, selection);
 	new ActionRenew(this, store, selection);
 	new ActionExport(this, selection);
 	new ActionMakeDefault(this, store, selection);
 	new ActionShowDetails(this, selection) {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		super.actionPerformed(e);
 		// update info pane as well
 		certInfoPane.refresh();
 	    }
 	};
 	new ActionViewLog(this);
 	new ActionViewCertificateList(this, certList, false);
 	new ActionChangeBrowser(this, selection);
 	new ActionRefresh(this, store) {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		super.actionPerformed(e);
 		// update info pane as well; TODO move into ActionRefresh itself
 		updateSelection();
 		certInfoPane.refresh();
 	    }
 	};
 	new ActionAbout(this);
 	// now that the actions are available, the menu can be created
 	this.setJMenuBar(getJMenuBar());
 	
 	// create buttons for template panel from actions
 	String[] actions = {
 		"import", "request",
		"viewrequest", /*"revoke",*/ "install",
		"renew"
 	};
 	certInfoButtons = new HashMap<String, JButton>();
 	for (int i=0; i<actions.length; i++) {
 	    JButton btn = new JButton(getAction(actions[i]));
 	    certInfoButtons.put(actions[i], btn);
 	    certInfoPane.addButton(btn, false, false);
 	}
 	    
 	// load certificates from default location
 	store.load();
 
 	// select first certificate if present
 	// TODO select default certificate
 	if (store.size() > 0)
 	    selection.setSelection(0);
 	// show certificate list only if multiple certificates present
 	setViewCertificateList(store.size() > 1);
 	// make sure ui is up-to-date
 	updateSelection();
     }
     
     /** Return the action associated with an id as registered by the action.
      * <p>
      * Make sure you only reference actions that have been created before.
      * Relevant Actions in this application should register themselves
      * with {@link nl.nikhef.jgridstart.gui.util.URLLauncher URLLauncher}
      * or {@link URLLauncherCertificate}. */
     protected Action getAction(String id) {
 	Action action = URLLauncherCertificate.getAction(id);
 	assert(action!=null);
 	return action;
     }
     
     /** Initialize content pane */
     private JPanel getJContentPane() {
 	if (jContentPane == null) {
 	    jContentPane = new JPanel();
 	    jContentPane.setLayout(new BorderLayout());
 	    certList = new JScrollPane(
 		    new ComponentCertificateList(store, selection),
 		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	    jContentPane.add(certList, BorderLayout.WEST);
 	    jContentPane.add(getJPanel(), BorderLayout.CENTER);
 	}
 	return jContentPane;
     }
 
     /** Initialize menu bar */
     @Override
     public JMenuBar getJMenuBar() {
 	if (jMenuBar == null) {
 	    JMenu menu;
 	    jMenuBar = new JMenuBar();
 
 	    // menu: Identities
 	    menu = new JMenu("Certificates");
 	    menu.setMnemonic('C');
 	    menu.add(new JMenuItem(getAction("request")));
 	    menu.add(new JMenuItem(getAction("import")));
 	    menu.addSeparator();
 	    // identity list managed by getJPanel()
 	    identityIndex = menu.getMenuComponentCount();
 	    identityMenu = menu;
 	    identityButtonGroup = new ButtonGroup();
 	    identitySeparator = new JSeparator();
 	    identitySeparator.setVisible(false);
 	    menu.add(identitySeparator);
 	    menu.add(new JMenuItem(new ActionQuit(this)));
 	    jMenuBar.add(menu);
 
 	    // menu: Certificate
 	    menu = new JMenu("Actions");
 	    menu.setMnemonic('A');
 	    menu.add(new JMenuItem(getAction("viewrequest")));
 	    menu.add(new JMenuItem(getAction("install")));
 	    menu.add(new JMenuItem(getAction("renew")));
 	    //menu.add(new JMenuItem(getAction("revoke")));
 	    menu.addSeparator();
 	    menu.add(new JMenuItem(getAction("export")));
 	    menu.add(new JMenuItem(getAction("makedefault")));
 	    menu.add(new JMenuItem("Change passphrase...", 'P')).setEnabled(false);
 	    menu.add(new JMenuItem(getAction("viewlog")));
 	    jMenuBar.add(menu);
 
 	    // menu: View
 	    menu = new JMenu("View");
 	    menu.setMnemonic('W');
 	    viewCertificateList = new JCheckBoxMenuItem(getAction("viewlist"));
 	    viewCertificateList.setSelected(false);
 	    menu.add(viewCertificateList);
 	    menu.add(new JMenuItem(getAction("refresh")));
 	    jMenuBar.add(menu);
 	    
 	    // menu: Help
 	    jMenuBar.add(Box.createHorizontalGlue());
 	    menu = new JMenu("Help");
 	    menu.setMnemonic('H');
 	    addHelpItems(menu);
 	    menu.addSeparator();
 	    menu.add(new JMenuItem(getAction("about")));
 	    jMenuBar.add(menu);
 	}
 	return jMenuBar;
     }
     
     /** Adds help items from global properties.
      * <p>
      * See the file {@code global.properties} for details, all properties starting
      * with {@code jgridstart.help} are used here.
      * 
      * @param menu
      */
     private void addHelpItems(JMenu menu) {
 	final String prefix = "jgridstart.help";
 	if (System.getProperty(prefix)==null) return;
 	String[] items = System.getProperty(prefix).split(",\\s*");
 	for (int i=0; i<items.length; i++) {
 	    String item = items[i];
 	    menu.add(new ActionOpenURL(this,
 		    System.getProperty(prefix+"."+item+".title"),
 		    System.getProperty(prefix+"."+item+".mnemonic").charAt(0),
 		    System.getProperty(prefix+"."+item+".url")));
 	}
     }
 
     /** Initialize certificate information pane */
     private JPanel getJPanel() {
 	if (certInfoPane == null) {
 	    certInfoPane = new TemplateButtonPanel();
 	    certInfoPane.setPreferredSize(new Dimension(500, 300));
 	    certInfoPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 	    
 	    // use SwingUtilities.invokeLater() to update the gui because
 	    // these may be originating from a different worker thread
 	    selection.addListSelectionListener(new ListSelectionListener() {
 		    public void valueChanged(ListSelectionEvent ev) {
 			if (ev.getValueIsAdjusting()) return;
 			SwingUtilities.invokeLater(new Runnable() {
 			    public void run() {
 				updateSelection();
 			    }
 			});			
 		    }
 	    });
 	    store.addListDataListener(new ListDataListener() {
 		// only single indices supported
 		public void intervalAdded(ListDataEvent e) {
 		    // TODO use SwingUtilities.invokeLater; problem with non-final ListDataEvent e
 		    int index = e.getIndex0();
 		    if (index < 0) return;
 		    // add item to menu if two or more items
 		    if (store.size() > 1) {
 			// if second item, also add first item and separator
 			if (store.size() == 2 && index == 1) {
 			    identitySeparator.setVisible(true);
 			    intervalAdded(new ListDataEvent(e.getSource(), e.getType(), 0, 0));
 			}
 			// then add this item
 			CertificatePair cert = store.get(index);
 			Action action = new ActionSelectCertificate(JGSFrame.this, cert, selection);
 			if (index<9)
 			    action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control "+(index+1)));
 			JRadioButtonMenuItem jrb = new JRadioButtonMenuItem(action);
 			//jrb.setText(cert.toString());
 			identityButtonGroup.add(jrb);
 			identityMenu.insert(jrb, identityIndex + index);
 			// show certificate list if we went from 1 to 2 certificates
 			if (store.size() == 2)
 			    setViewCertificateList(true);
 		    }
 		}
 		public void intervalRemoved(ListDataEvent e) {
 		    // TODO use SwingUtilities.invokeLater; problem with non-final ListDataEvent e
 		    // remove item from menu
 		    int index = e.getIndex0();
 		    if (index < 0) return;
 		    JMenuItem item = identityMenu.getItem(identityIndex + e.getIndex0());
 		    // select previous index if it was currently selected
 		    if (item.isSelected()) {
 			int newIdx = index - 1;
 			if (newIdx<0) newIdx=0;
 			selection.setSelection(newIdx);
 		    }
 		    identityButtonGroup.remove(item);
 		    identityMenu.remove(item);
 		    // if only one item left, also remove that one since it
 		    // adds no useful information for the user
 		    if (store.size() == 1 && index != 0) {
 			intervalRemoved(new ListDataEvent(e.getSource(), e.getType(), 0, 0));
 			identitySeparator.setVisible(false);
 		    }
 		}
 		public void contentsChanged(ListDataEvent e) {
 		    SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 			    updateSelection(); // for buttons
 			    certInfoPane.refresh();
 			}
 		    });			
 		}
 	    });
 	}
 	return certInfoPane;
     }
     
     /** Set the visibility of the {@link CertificateList}.
      * <p>
      * In Java 1.6 one could use the built-in functionality of
      * {@link Action}s, but as we want to support older versions as 
      * well, this is done by manually calling the menu item to
      * preserve the action-gui binding.
      * <p>
      * This requires selecting each view of the action when it
      * has a checkbox :( but I see no easy other way right now.
      */
     private void setViewCertificateList(boolean wantVisible) {
 	if (viewCertificateList.isSelected()!=wantVisible)
 	    viewCertificateList.doClick();
     }
     
     /** Effectuate a selection change in the gui.
      * <p>
      * The current selection is managed by {@link #selection}.
      */
     @SuppressWarnings("deprecation") // for Frame.setCursor()
     private void updateSelection() {
 	// update contents and load template
 	CertificatePair c = selection.getCertificatePair();
 	if (c!=null) {
 	    // certificate selected, show info and add buttons according to state
 	    certInfoPane.setData(c);
 	    certInfoPane.setDocument(getClass().getResource("certificate_info.html").toExternalForm());
 	} else {
 	    // no certificate selected, present signup page
 	    certInfoPane.setDocument(getClass().getResource("certificate_none_yet.html").toExternalForm());
 	}
 	boolean certPresent = false;
 	try {
 	    certPresent = (c!=null && c.getCertificate()!=null);
 	} catch (IOException e) { }
 	certInfoButtons.get("viewrequest").setVisible(c!=null && !certPresent);
 	certInfoButtons.get("install").setVisible(certPresent);
 	//certInfoButtons.get("revoke").setVisible(c!=null);
 	
 	certInfoButtons.get("import").setVisible(c==null);
 	certInfoButtons.get("request").setVisible(c==null);
	certInfoButtons.get("renew").setVisible(c!=null && Boolean.valueOf(c.getProperty("valid.notafter.warning")));
 	
 	// also update selected item in menu
 	if (store.size() > 1 && selection.getIndex() >= 0)
 	    identityMenu.getItem(identityIndex + selection.getIndex()).setSelected(true);
 	
 	// update status in background
 	if (lastSelection != c) {
 	    setCursor(Cursor.WAIT_CURSOR);
 	    new SwingWorker<Void, Void>() {
 		@Override
 		protected Void doInBackground() throws Exception {
 		    // XXX exceptions are lost; usually ok but maybe not always
 		    selection.getCertificatePair().refresh();
 		    return null;
 		}
 		@Override
 		protected void done() {
 		    setCursor(null);
 		}
 	    }.execute();
 	}
 	
 	lastSelection = c;
     }
 }
