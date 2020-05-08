 package nl.nikhef.jgridstart.gui;
 
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.BorderFactory;
 import javax.swing.JScrollPane;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.CertificateStore;
 import nl.nikhef.jgridstart.gui.util.PrintUtilities;
 import nl.nikhef.jgridstart.gui.util.TemplateButtonPane;
 import nl.nikhef.jgridstart.util.PasswordCache;
 
 public class JGSFrame extends JFrame {
 
     private JPanel jContentPane = null;
     private JMenuBar jMenuBar = null;
     private ComponentCertificateList certList = null; 
     private TemplateButtonPane certInfoPane = null;
 
     private CertificateStore store = null;
     private CertificateSelection selection = null; 
     
     protected int identityIndex = -1;
     protected JMenu identityMenu = null;
     private ButtonGroup identityButtonGroup = null;
     
     private AbstractButton viewCertificateList = null;
     private ActionViewCertificateList actionViewCertificateList = null;
 
     /**
      * This is the default constructor
      */
     public JGSFrame() {
 	super();
 	initialize();
     }
 
     /**
      * This method initializes this
      * 
      * @return void
      */
     private void initialize() {
 	store = new CertificateStore();
 	selection = new CertificateSelection(store);
 	PasswordCache.getInstance().setParent(this);
 	
 	this.setSize(550, 350);
 	this.setMinimumSize(new Dimension(400, 150));
 	this.setPreferredSize(new Dimension(600, 350));
 	this.setContentPane(getJContentPane());
 	this.setJMenuBar(getJMenuBar());
 	this.setTitle("jGridStart (development version)");
 
 	store.load(System.getProperty("user.home")+"/.globus-test");
 
 	setViewCertificateList(false);
 	if (store.size() > 0)
 	    selection.setSelection(0);
 	else
 	    updateSelection();
     }
 
     /**
      * This method initializes jContentPane
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getJContentPane() {
 	if (jContentPane == null) {
 	    jContentPane = new JPanel();
 	    jContentPane.setLayout(new BorderLayout());
 	    certList = new ComponentCertificateList(store, selection);
 	    jContentPane.add(certList, BorderLayout.WEST);
 	    jContentPane.add(getJPanel(), BorderLayout.CENTER);
 	}
 	return jContentPane;
     }
 
     /**
      * This method initializes jMenuBar	
      * 	
      * @return javax.swing.JMenuBar	
      */
     public JMenuBar getJMenuBar() {
 	if (jMenuBar == null) {
 	    JMenu menu;
 	    jMenuBar = new JMenuBar();
 
 	    // menu: Identities
 	    menu = new JMenu("Certificates");
 	    menu.setMnemonic('C');
 	    menu.add(new JMenuItem(new ActionRequest(this, store, selection)));
 	    menu.add(new JMenuItem(new ActionImport(this, store, selection)));
 	    menu.addSeparator();
 	    // identity list managed by getJPanel()
 	    identityIndex = menu.getMenuComponentCount();
 	    identityMenu = menu;
 	    identityButtonGroup = new ButtonGroup();
 	    menu.addSeparator();
 	    menu.add(new JMenuItem(new ActionQuit(this)));
 	    jMenuBar.add(menu);
 
 	    // menu: Certificate
 	    menu = new JMenu("Actions");
 	    menu.setMnemonic('A');
 	    menu.add(new JMenuItem("Request approval...", 'R'));
 	    menu.add(new JMenuItem("Install...", 'I'));
 	    menu.add(new JMenuItem("Request renewal...", 'N'));
 	    menu.add(new JMenuItem(new ActionRevoke(this)));
 	    menu.addSeparator();
 	    menu.add(new JMenuItem(new ActionExport(this, selection)));
 	    menu.add(new JMenuItem("Change passphrase...", 'P'));
 	    menu.add(new JMenuItem(new ActionViewLog(this)));
 	    jMenuBar.add(menu);
 	    menu.getItem(0).setEnabled(false);
 
 	    // menu: View
 	    menu = new JMenu("View");
 	    menu.setMnemonic('W');
 	    actionViewCertificateList = new ActionViewCertificateList(certList, false);
 	    viewCertificateList = new JCheckBoxMenuItem(actionViewCertificateList);
 	    viewCertificateList.setSelected(false);
 	    menu.add(viewCertificateList);
 	    menu.add(new JMenuItem("Personal details...", 'P'));
 	    menu.add(new JMenuItem(new ActionRefresh(this, store) {
 		public void actionPerformed(ActionEvent e) {
 		    super.actionPerformed(e);
 		    // update info pane as well; TODO move into ActionRefresh itself
 		    certInfoPane.refresh();
 		}
 	    }));
 	    jMenuBar.add(menu);
 	    
 	    // menu: Help
 	    jMenuBar.add(Box.createHorizontalGlue());
 	    menu = new JMenu("Help");
 	    menu.setMnemonic('H');
 	    menu.add(new JMenuItem(new ActionAbout(this)));
 	    jMenuBar.add(menu);
 	}
 	return jMenuBar;
     }
 
     /**
      * This method initializes jPanel	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel() {
 	if (certInfoPane == null) {
 	    certInfoPane = new TemplateButtonPane();
 	    certInfoPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 
 	    selection.addListSelectionListener(new ListSelectionListener() {
 		    public void valueChanged(ListSelectionEvent ev) {
 			if (ev.getValueIsAdjusting()) return;
 			updateSelection();
 		    }
 	    });
 	    store.addListDataListener(new ListDataListener() {
 		// only single indices supported
 		public void intervalAdded(ListDataEvent e) {
 		    // add item to menu
 		    if (e.getIndex0() < 0) return;
 		    CertificatePair cert = store.get(e.getIndex0());
 		    JRadioButtonMenuItem jrb = new JRadioButtonMenuItem();
 		    jrb.setText(cert.toString());
 		    jrb.addActionListener(
 			    new ActionSelectCertificate(JGSFrame.this, cert, selection));
 		    identityButtonGroup.add(jrb);
 		    identityMenu.insert(jrb, identityIndex + e.getIndex0());
 		    // show certificate list if we went from 1 to 2 certificates
 		    if (store.size() == 2)
 			setViewCertificateList(true);
 		}
 		public void intervalRemoved(ListDataEvent e) {
 		    // remove item from menu
 		    if (e.getIndex0() < 0) return;
 		    JMenuItem item = identityMenu.getItem(identityIndex + e.getIndex0());
 		    // select previous index if it was currently selected
 		    if (item.isSelected()) {
 			int newIdx = e.getIndex0() - 1;
 			if (newIdx<0) newIdx=0;
 			selection.setSelection(newIdx);
 		    }
 		    identityButtonGroup.remove(item);
 		    identityMenu.remove(item);
 		}
 		public void contentsChanged(ListDataEvent e) {
 		    // TODO update description and refresh contentpane(!)
 		}
 	    });
 	}
 	return certInfoPane;
     }
     
     /** Set the actionViewCertificateList and its gui views. Yes, this is terrible
      * to put it in a method like this, but I don't know at this moment how to do it
      * properly. TODO fix this
      */
     private void setViewCertificateList(boolean wantVisible) {
 	    actionViewCertificateList.putValue("SwingSelectedKey", new Boolean(wantVisible));
 	    viewCertificateList.setSelected(wantVisible);
 	    ActionEvent ev2 = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
 	    actionViewCertificateList.actionPerformed(ev2);
     }
     
     /** Effectuate a selection change in the gui. Current selection is
      * managed by the class variable selection. */
     private void updateSelection() {
 	try {
 	    certInfoPane.removeActions();
 	    // update contents and load template
 	    CertificatePair c = selection.getCertificatePair();
 	    if (c!=null) {
 		certInfoPane.setData(c);
 		certInfoPane.setPage(getClass().getResource("certificate_info.html"));
 		certInfoPane.addAction(new ActionRevoke(JGSFrame.this));
 	    } else {
 		certInfoPane.setPage(getClass().getResource("certificate_none_yet.html"));
 		certInfoPane.addAction(new ActionImport(JGSFrame.this, store, selection));
 		certInfoPane.addAction(new ActionRequest(JGSFrame.this, store, selection));
 	    }
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	// also update selected item in menu
	identityMenu.getItem(identityIndex + selection.getIndex()).setSelected(true);
     }

}  //  @jve:decl-index=0:visual-constraint="10,10"
