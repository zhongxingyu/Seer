 package keytool.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import keytool.model.MTCertificate;
 import keytool.model.MTPrivateKey;
 import keytool.model.Model;
 import keytool.model.ModelException;
 import keytool.view.MainWindow;
 import keytool.view.View;
 
 /*
 @startuml
 
 [*] --> StateWait
 StateWait --> StateSaving : menuSave
 StateSaving --> StateWait : FCopen
 StateSaving --> StateWait : FCcancel
 
 StateWait --> StateOpening : menuOpen
 StateOpening --> StateWait : FCopen
 StateOpening --> StateWait : FCcancel
 StateOpening --> StateOpeningFail : DefaultPasswordDontMatch
 StateOpeningFail --> StateWait : PWValidate
 StateOpeningFail --> StateWait : PWCancel
 
 StateWait --> StateExporting : itemExport
 StateExporting --> StateWait : FCopen
 StateExporting --> StateWait : FCcancel
 
 StateWait --> StateImporting : itemImport
 StateImporting --> StateChoosingKey : BtnChooseKey
 StateChoosingKey --> StateImporting : FCopen
 StateChoosingKey --> StateImporting : FCcancel
 StateImporting --> StateChoosingCertificate : BtnChooseCertificate
 StateChoosingCertificate --> StateImporting : FCopen
 StateChoosingCertificate --> StateImporting : FCcancel
 StateImporting --> StateWait : BtnValidate
 StateImporting --> StateWait : BtnCancel
 
 @enduml
  */
 public class Controller {
 	private View view;
 	private Model model;
 	private enum State {StateWAIT,
 									StateSAVING,
 									StateOPENING,
 									StateOPENINGFAIL,
 									StateIMPORTING,
 									StateEXPORTING,
 									StateCREATINGKEY,
 									StateCHOOSINGKEY,
 									StateCHOOSINGCERTIFICATE};
 	private State state;
 
 	public Controller(Model model, View view){
 		this.view = view;
 		this.model = model;
 		this.state = State.StateWAIT;
 
 		initMainWindowListener();
 		refreshKeysList();
 		refreshCertificateList();
 
 		initFCWindowListener();
 		initCreateKeyWindowListener();
 		initImportKeyWindowListener();
 		initPasswordWindowListener();
 	}
 
 	/* MainWindow */
 	private void initMainWindowListener() {
 		MainWindow mw = this.view.getMainWindow();
 		mw.addItemNewKeyStoreListener(new ItemNewListener());
 		mw.addItemOpenListener(new ItemOpenListener());
 		mw.addItemSaveListener(new ItemSaveListener());
 		mw.addItemSaveAsListener(new ItemSaveAsListener());
 		mw.addItemQuitListener(new ItemQuitListener());
 		mw.addBtnImportListener(new BtnImportListener());
 		mw.addBtnExportListener(new BtnExportListener());
 		mw.addBtnDeleteListener(new BtnDeleteListener());
 		mw.addBtnNewKeyListener(new BtnNewKeyListener());
 		mw.addKeyListListener(new ListKeysListener());
 		mw.addCertificatesListListener(new ListCertificatesListener());
 		mw.addChangeTabListener(new ChangeTabListener());
 		refreshMainWindow();
 	}
 
 	private void refreshLists() {
 		this.refreshKeysList();
 		this.refreshCertificateList();
 	}
 	
 	private void refreshKeysList() {
 		DefaultListModel list = new DefaultListModel();
 
 		try {
 			if(model.isInitalized())
 				list = this.model.getKeys();
 		} catch (ModelException e) {
 			this.view.createErrorWindow(e.getMessage());
 		} finally {
 			this.view.getMainWindow().setKeysList(list);
 		}
 	}
 	
 	private void refreshCertificateList() {
 		DefaultListModel list = new DefaultListModel();
 		// Si le modèle n'est pas initialisé, la liste reste vide
 		try {
 			if(model.isInitalized())
 				list = this.model.getCertificates();
 		} catch (ModelException e) {
 			this.view.createErrorWindow(e.getMessage());
 		} finally {
 			this.view.getMainWindow().setCertificatesList(list);
 		}
 	}
 
 	private void refreshDetails() {
 		if(!model.isInitalized())
 			view.getMainWindow().setDetails("");
 		else {
 			String details = "";
 			if(view.getMainWindow().isKeysTabSelected()) {
 				String selectedKey = view.getMainWindow().getSelectedKey();
 				try {
 					if(selectedKey != null)
 						details = model.getKey(selectedKey).getDetails();
 				} catch (ModelException e) {
 					view.createErrorWindow(e.getMessage());
 				}
 			} else if(view.getMainWindow().isCertificatesTabSelected()) {
 				String selectedCertificates = view.getMainWindow().getSelectedCertificate();
 				try {
 					if(selectedCertificates != null)
 						details = model.getCertificate(selectedCertificates).getDetails();
 				} catch (ModelException e) {
 					view.createErrorWindow(e.getMessage());
 				}
 			}
 			view.getMainWindow().setDetails(details);
 		}
 	}
 	
 	/**
 	 * Met à jour la fenêtre principale :
 	 * - active/désactive les boutons
 	 * - met à jour les listes
 	 */
 	private void refreshMainWindow() {
		// Details are computed from the lists
		// DO refresh the lists before the details
 		refreshLists();
		refreshDetails();
 		if(model.isInitalized())
 			this.setEnable(true);
 		else
 			this.setEnable(false);
 	}
 	
 	private void setEnable(boolean enable) {
 		view.getMainWindow().setEnabledFields(enable);
 	}
 
 	class ItemNewListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				model.newKeyStore();
 			} catch (ModelException e1) {
 				view.createErrorWindow(e1.getMessage());
 			}
 			refreshLists();
 		}
 	}
 	
 	class ItemOpenListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.getFileChooserWindow().setOpenDialog();
 			view.showFileChooserWindow();
 			state = State.StateOPENING;
 		}
 	}
 
 	class ItemQuitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.disposeAll();
 		}
 	}
 
 	class ItemSaveListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				model.save();
 			} catch (ModelException e1) {
 				view.createErrorWindow(e1.getMessage());
 			}
 		}
 	}
 
 	class ItemSaveAsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.getFileChooserWindow().setSaveDialog();
 			view.showFileChooserWindow();
 			state = State.StateSAVING;
 		}
 	}
 
 	class ChangeTabListener implements ChangeListener {
 		public void stateChanged(ChangeEvent e) {
 			refreshDetails();
 		}
 	}
 	
 	class BtnImportListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.showImportKeyWindow();
 			state = State.StateIMPORTING;
 		}
 	}
 
 	class BtnExportListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.showFileChooserWindow();
 			state = State.StateEXPORTING;
 		}
 	}
 	
 	class BtnDeleteListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				if(view.getMainWindow().isKeysTabSelected()) {
 					String alias = view.getMainWindow().getSelectedKey();
 					model.delEntry(alias);					
 				} else if(view.getMainWindow().isCertificatesTabSelected()) {
 					String alias = view.getMainWindow().getSelectedCertificate();
 					model.delEntry(alias);				
 				}
 			}
 			catch(ModelException e1) {
 				view.createErrorWindow(e1.getMessage());
 			}
 			refreshLists();
 		}
 	}
 	
 	class BtnNewKeyListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.showCreateKeyWindow();
 			state = State.StateCREATINGKEY;
 		}
 	}
 	
 	class ListKeysListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent arg0) {
 			refreshDetails();
 		}
 	}
 
 	class ListCertificatesListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent arg0) {
 			refreshDetails();
 		}
 	}
 	
 	/* FileChooserWindow */
 	private void initFCWindowListener() {
 		this.view.getFileChooserWindow().addFCActionListener(new FCActionListener());
 		this.view.getFileChooserWindow().addFCWindowListener(new FCWindowListener());
 	}
 
 	class FCActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
 				switch(state) {
 				case StateOPENING:
 					view.hideFileChooserWindow();
 					view.showPasswordWindow();
 					break;
 				case StateSAVING:
 					try {
 						view.hideFileChooserWindow();
						model.saveAs(view.getFileChooserWindow().getPath());
 					} catch (ModelException e1) {
 						view.createErrorWindow(e1.getMessage());
 					}
 					break;
 				case StateEXPORTING:
 					try {
 						if(view.getMainWindow().isKeysTabSelected()) {
 							String alias = view.getMainWindow().getSelectedKey();
 							if(! alias.equals(""))
 								model.getKey(alias).exportTo(view.getFileChooserWindow().getPath());
 								
 						} else if(view.getMainWindow().isCertificatesTabSelected()) {
 							String alias = view.getMainWindow().getSelectedCertificate();
 							if(! alias.equals(""))
 								model.getCertificate(alias).exportTo(view.getFileChooserWindow().getPath());
 							
 						}
 					} catch (ModelException e1) {
 						view.createErrorWindow(e1.getMessage());
 					}
 					view.hideFileChooserWindow();
 					break;
 				case StateCHOOSINGKEY:
 					view.getImportKeyWindow().setKeyFileField(view.getFileChooserWindow().getPath());
 					view.hideFileChooserWindow();
 					state = State.StateIMPORTING;
 					break;
 				case StateCHOOSINGCERTIFICATE:
 					view.getImportKeyWindow().setCertificateFileField(view.getFileChooserWindow().getPath());
 					view.hideFileChooserWindow();
 					state = State.StateIMPORTING;
 					break;
 				}
 			} else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
 				switch(state) {
 				case StateEXPORTING:
 				case StateOPENING:
 				case StateSAVING:
 					view.hideFileChooserWindow();
 					state = State.StateWAIT;
 					break;
 				case StateCHOOSINGKEY:
 				case StateCHOOSINGCERTIFICATE:
 					view.hideFileChooserWindow();
 					state = State.StateIMPORTING;
 					break;
 				}
 			}
 		}
 	}
 
 	class FCWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			switch(state) {
 			case StateEXPORTING:
 			case StateOPENING:
 			case StateSAVING:
 				view.hideFileChooserWindow();
 				state = State.StateWAIT;
 				break;
 			case StateCHOOSINGKEY:
 			case StateCHOOSINGCERTIFICATE:
 				view.hideFileChooserWindow();
 				state = State.StateIMPORTING;
 				break;
 			}
 		}
 	}
 	
 	/* CreateKeyWindow */
 	private void initCreateKeyWindowListener() {
 		this.view.getCreateKeyWindow().addBtnCancelListener(new CKWBtnCancelListener());
 		this.view.getCreateKeyWindow().addBtnValidateListener(new CKWBtnValidateListener());
 		this.view.getCreateKeyWindow().addCWWindowListener(new CKWWindowListener());
 	}
 	
 	class CKWBtnCancelListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			view.getCreateKeyWindow().resetField();
 			view.hideCreateKeyWindow();
 		}
 	}
 	
 	class CKWBtnValidateListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			try {
 				if(view.getCreateKeyWindow().getAliasField().equals(""))
 					throw new ModelException("L'alias ne doit pas être vide !");
 				
 				StringBuilder subject = new StringBuilder();
 				subject.append("CN="+view.getCreateKeyWindow().getNameField());
 				subject.append(", ");
 				subject.append("OU="+view.getCreateKeyWindow().getOUField());
 				subject.append(", ");
 				subject.append("O="+view.getCreateKeyWindow().getOrgField());
 				subject.append(", ");
 				subject.append("L="+view.getCreateKeyWindow().getCityField());
 				subject.append(", ");
 				subject.append("ST="+view.getCreateKeyWindow().getStateField());
 				subject.append(", ");
 				subject.append("C="+view.getCreateKeyWindow().getCountryField());
 
 				MTPrivateKey key = new MTPrivateKey(subject.toString());
 				key.addToKeyStore(model, view.getCreateKeyWindow().getAliasField());
 				view.hideCreateKeyWindow();
 				view.getCreateKeyWindow().resetField();
 				refreshKeysList();
 			} catch (ModelException e) {
 				view.createErrorWindow(e.getMessage());
 			}
 		}
 	}
 	
 	class CKWWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			view.getCreateKeyWindow().resetField();
 			view.hideCreateKeyWindow();
 		}
 	}
 	
 	/* ImportKeyWindow */
 	private void initImportKeyWindowListener() {
 		this.view.getImportKeyWindow().addBtnChooseKeyListener(new IKWBtnChooseKeyListener());
 		this.view.getImportKeyWindow().addBtnChooseCertificateListener(new IKWBtnChooseCertificateListener());
 		this.view.getImportKeyWindow().addBtnCancelListener(new IKWBtnCancelListener());
 		this.view.getImportKeyWindow().addBtnValidateListener(new IKWBtnValidateListener());
 		this.view.getImportKeyWindow().addIWWindowListener(new IKWWindowListener());
 	}
 	
 	class IKWBtnChooseKeyListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			view.showFileChooserWindow();
 			state = State.StateCHOOSINGKEY;
 		}
 	}
 	
 	class IKWBtnChooseCertificateListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			view.showFileChooserWindow();
 			state = State.StateCHOOSINGCERTIFICATE;
 		}
 	}
 	
 	class IKWBtnCancelListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			view.hideImportKeyWindow();
 			view.getImportKeyWindow().resetField();
 			state = State.StateWAIT;
 		}
 	}
 	
 	class IKWBtnValidateListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			try {
 				String certpath = view.getImportKeyWindow().getCertificateFileField();
 				String keypath = view.getImportKeyWindow().getKeyFileField();
 				String alias = view.getImportKeyWindow().getAliasField();
 				if(alias.isEmpty())
 					throw new ModelException("L'alias est obligatoire");
 				
 				if(keypath.isEmpty()) {
 					// Import a certificate
 					MTCertificate certificate;
 					try {
 						certificate = new MTCertificate(new FileInputStream(certpath));
 					} catch (FileNotFoundException e) {
 						throw new ModelException("Fichier non trouvé :"+certpath);
 					}
 					certificate.addToKeyStore(model, alias);
 				} else {
 					// Import a PrivateKey
 					MTPrivateKey key = new MTPrivateKey(keypath, certpath);
 					key.addToKeyStore(model, alias);
 				}
 				view.hideImportKeyWindow();
 				view.getImportKeyWindow().resetField();
 				refreshLists();
 			} catch (ModelException e) {
 				view.createErrorWindow(e.getMessage());
 			}
 		}
 	}
 	
 	class IKWWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			view.hideImportKeyWindow();
 			view.getImportKeyWindow().resetField();
 			state = State.StateWAIT;
 		}
 	}
 	
 	/* PasswordWindow */
 	private void initPasswordWindowListener() {
 		this.view.getPasswordWindow().addPasswordWindowListener(new PWCWindowListener());
 		this.view.getPasswordWindow().addBtnCancelListener(new PWBtnCancelListener());
 		this.view.getPasswordWindow().addBtnValidateListener(new PWBtnValidateListener());
 	}
 	
 	class PWBtnCancelListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			view.hidePasswordWindow();
 			view.resetPasswordWindow();
 			state = State.StateWAIT;
 		}
 	}
 	
 	class PWBtnValidateListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			try {
 				model.openKeyStore(view.getFileChooserWindow().getPath(), view.getPasswordWindow().getPasswordField());
 				view.hidePasswordWindow();
 
 			} catch (ModelException e) {
 				view.createErrorWindow(e.getMessage());
 				state = State.StateWAIT;
 			} finally {
 				refreshMainWindow();
 			}
 			view.resetPasswordWindow();
 			
 			state = State.StateWAIT;
 		}
 	}
 	
 	class PWCWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			view.hidePasswordWindow();
 			view.resetPasswordWindow();
 			state = State.StateWAIT;
 		}
 	}
 }
