 package fedora.client.objecteditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 import fedora.client.Administrator;
 
 import fedora.server.types.gen.Datastream;
 
 /**
  * Shows a tabbed pane, one for each datastream in the object, and one
  * special tab for "New...", which handles the creation of new datastreams.
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DatastreamsPane
         extends JPanel
         implements PotentiallyDirty, TabDrawer {
 
     private String m_pid;
     private JTabbedPane m_tabbedPane;
     private DatastreamPane[] m_datastreamPanes;
     private ObjectEditorFrame m_owner;
     private ArrayList m_dsListeners;
     private Datastream[] m_currentVersions;
     private Map m_currentVersionMap;
 
     public String[] ALL_KNOWN_MIMETYPES = new String[] {"text/xml",
             "text/plain", "text/html", "text/html+xml", "text/svg+xml",
             "image/jpeg", "image/gif", "image/bmp", "application/postscript",
             "application/ms-word", "application/pdf", "application/zip"};
     public String[] XML_MIMETYPE = new String[] {"text/xml"};
 
     static ImageIcon newIcon=new ImageIcon(Administrator.cl.getResource("images/standard/general/New16.gif"));
 
 
     /**
      * Build the pane.
      */
     public DatastreamsPane(ObjectEditorFrame owner, String pid)
             throws Exception {
         m_pid=pid;
         m_owner=owner;
         m_currentVersionMap=new HashMap();
         // this(m_tabbedPane)
         m_dsListeners=new ArrayList();
 
             // m_tabbedPane(DatastreamPane[])
 
             m_tabbedPane=new JTabbedPane(SwingConstants.LEFT);
             m_currentVersions=Administrator.APIM.
                     getDatastreams(pid, null, null);
             m_datastreamPanes=new DatastreamPane[m_currentVersions.length];
             for (int i=0; i<m_currentVersions.length; i++) {
                 m_currentVersionMap.put(m_currentVersions[i].getID(), m_currentVersions[i]);
                 m_datastreamPanes[i]=new DatastreamPane(
                         owner,
                         pid,
                         Administrator.APIM.getDatastreamHistory(
                                 pid,
                                 m_currentVersions[i].getID()),
                         this);
                 StringBuffer tabLabel=new StringBuffer();
                 tabLabel.append(m_currentVersions[i].getID());
                 m_tabbedPane.add(tabLabel.toString(), m_datastreamPanes[i]);
                 m_tabbedPane.setToolTipTextAt(i, m_currentVersions[i].getMIMEType()
                         + " - " + m_currentVersions[i].getLabel() + " ("
                         + m_currentVersions[i].getControlGroup().toString() + ")");
                 colorTabForState(m_currentVersions[i].getID(), m_currentVersions[i].getState());
             }
             m_tabbedPane.add("New...", new JPanel());
 
         setLayout(new BorderLayout());
         add(m_tabbedPane, BorderLayout.CENTER);
 
         doNew(XML_MIMETYPE, false);
     }
 
     public Map getCurrentVersionMap() {
         return m_currentVersionMap;
     }
 
     public void colorTabForState(String id, String s) {
         int i=getTabIndex(id);
         if (s.equals("I")) {
             m_tabbedPane.setBackgroundAt(i, Administrator.INACTIVE_COLOR);
         } else if (s.equals("D")) {
             m_tabbedPane.setBackgroundAt(i, Administrator.DELETED_COLOR);
         } else {
             m_tabbedPane.setBackgroundAt(i, Administrator.ACTIVE_COLOR);
         }
     }
 
     /**
      * Set the content of the "New..." JPanel to a fresh new datastream
      * entry panel, and switch to it, if needed.
      */
     public void doNew(String[] dropdownMimeTypes, boolean makeSelected) {
         int i=getTabIndex("New...");
         m_tabbedPane.setComponentAt(i, new NewDatastreamPane(dropdownMimeTypes));
         m_tabbedPane.setToolTipTextAt(i, "Add a new datastream to this object");
         m_tabbedPane.setIconAt(i, newIcon);
         m_tabbedPane.setBackgroundAt(i, Administrator.DEFAULT_COLOR);
         if (makeSelected) {
             m_tabbedPane.setSelectedIndex(i);
         }
     }
 
     private int getTabIndex(String id) {
         int i=m_tabbedPane.indexOfTab(id);
         if (i!=-1) return i;
         return m_tabbedPane.indexOfTab(id+"*");
     }
 
     public void setDirty(String id, boolean isDirty) {
         int i=getTabIndex(id);
         if (isDirty) {
             m_tabbedPane.setTitleAt(i, id + "*");
         } else {
             m_tabbedPane.setTitleAt(i, id);
         }
     }
 
     /**
      * Refresh the content of the tab for the indicated datastream with the
      * latest information from the server.
      */
     protected void refresh(String dsID) {
         int i=getTabIndex(dsID);
         try {
             Datastream[] versions=Administrator.APIM.getDatastreamHistory(m_pid, dsID);
             m_currentVersionMap.put(dsID, versions[0]);
             System.out.println("New create date is: " + versions[0].getCreateDate());
             DatastreamPane replacement=new DatastreamPane(m_owner, m_pid, versions, this);
             m_datastreamPanes[i]=replacement;
             m_tabbedPane.setComponentAt(i, replacement);
             m_tabbedPane.setToolTipTextAt(i, versions[0].getMIMEType()
                     + " - " + versions[0].getLabel() + " ("
                     + versions[0].getControlGroup().toString() + ")");
             colorTabForState(dsID, versions[0].getState());
             setDirty(dsID, false);
             fireDatastreamModified(versions[0]);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(Administrator.getDesktop(),
                     e.getMessage() + "\nTry re-opening the object viewer.",
                     "Error while refreshing",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public void addDatastreamListener(DatastreamListener dsl) {
         m_dsListeners.add(dsl);
     }
 
     protected void fireDatastreamAdded(Datastream ds) {
         for (int i=0; i<m_dsListeners.size(); i++) {
             DatastreamListener l=(DatastreamListener) m_dsListeners.get(i);
             l.datastreamAdded(ds);
         }
     }
 
     protected void fireDatastreamModified(Datastream ds) {
         for (int i=0; i<m_dsListeners.size(); i++) {
             DatastreamListener l=(DatastreamListener) m_dsListeners.get(i);
             l.datastreamModified(ds);
         }
     }
 
     protected void fireDatastreamPurged(String dsID) {
         for (int i=0; i<m_dsListeners.size(); i++) {
             DatastreamListener l=(DatastreamListener) m_dsListeners.get(i);
             l.datastreamPurged(dsID);
         }
     }
 
     /**
      * Add a new tab with a new datastream.
      */
     protected void addDatastreamTab(String dsID) throws Exception {
         DatastreamPane[] newArray=new DatastreamPane[m_datastreamPanes.length+1];
         for (int i=0; i<m_datastreamPanes.length; i++) {
             newArray[i]=m_datastreamPanes[i];
         }
         Datastream[] versions=Administrator.APIM.getDatastreamHistory(m_pid, dsID);
         m_currentVersionMap.put(dsID, versions[0]);
         newArray[m_datastreamPanes.length]=new DatastreamPane(m_owner,
                         m_pid, versions, this);
         // swap the arrays
         m_datastreamPanes=newArray;
         int newIndex=getTabIndex("New...");
         m_tabbedPane.add(m_datastreamPanes[m_datastreamPanes.length-1], newIndex);
         m_tabbedPane.setTitleAt(newIndex, dsID);
         m_tabbedPane.setToolTipTextAt(newIndex, versions[0].getMIMEType()
                 + " - " + versions[0].getLabel() + " ("
                 + versions[0].getControlGroup().toString() + ")");
         colorTabForState(dsID, versions[0].getState());
         m_tabbedPane.setSelectedIndex(newIndex);
         doNew(XML_MIMETYPE, false);
         fireDatastreamAdded(versions[0]);
     }
 
     protected void remove(String dsID) {
         int i=getTabIndex(dsID);
         m_tabbedPane.remove(i);
         m_currentVersionMap.remove(dsID);
         // also remove it from the array
         DatastreamPane[] newArray=new DatastreamPane[m_datastreamPanes.length-1];
         for (int x=0; x<m_datastreamPanes.length; x++) {
             if (x<i) {
                 newArray[x]=m_datastreamPanes[x];
             } else if (x>i) {
                 newArray[x-1]=m_datastreamPanes[x-1];
             }
         }
         m_datastreamPanes=newArray;
         // then make sure dirtiness indicators are corrent
         m_owner.indicateDirtiness();
         fireDatastreamPurged(dsID);
     }
 
     public boolean isDirty() {
         for (int i=0; i<m_datastreamPanes.length; i++) {
             if (m_datastreamPanes[i].isDirty()) return true;
         }
         return false;
     }
 
     public void addRows(JComponent[] left, JComponent[] right,
             GridBagLayout gridBag, Container container) {
         GridBagConstraints c=new GridBagConstraints();
         c.insets=new Insets(0, 4, 4, 4);
         for (int i=0; i<left.length; i++) {
             c.anchor=GridBagConstraints.NORTHWEST;
             c.gridwidth=GridBagConstraints.RELATIVE; //next-to-last
             c.fill=GridBagConstraints.NONE;      //reset to default
             c.weightx=0.0;                       //reset to default
             gridBag.setConstraints(left[i], c);
             container.add(left[i]);
 
             c.gridwidth=GridBagConstraints.REMAINDER;     //end row
             if (!(right[i] instanceof JComboBox)) {
                 c.fill=GridBagConstraints.HORIZONTAL;
             } else {
                 c.anchor=GridBagConstraints.NORTHWEST;
             }
             c.weightx=1.0;
             gridBag.setConstraints(right[i], c);
             container.add(right[i]);
         }
 
     }
 
     public class NewDatastreamPane
             extends JPanel implements ActionListener {
 
         JTextField m_labelTextField;
         JTextField m_idTextField;
         JTextField m_formatURITextField;
         JTextField m_altIDsTextField;
         JTextField m_referenceTextField;
         JTextArea m_controlGroupTextArea;
         JComboBox m_mimeComboBox;
         CardLayout m_contentCard;
         JPanel m_specificPane;
         TextContentEditor m_xEditor=null;
         TextContentEditor m_mEditor=null;
         JPanel m_erPane;
         JButton m_erViewButton;
         ContentViewer m_erViewer;
 
         String m_controlGroup;
         String m_lastSelectedMimeType;
         File m_managedFile;
         JComponent m_mCenter;
         JPanel m_mPane;
 
         static final String X_DESCRIPTION="Metadata that is stored and managed inside the "
                 + "repository.  This must be well-formed XML and will be "
                 + "stripped of processing instructions and comments."
                 + "Use of XML namespaces is optional and schema validity is "
                 + "not enforced by the repository.";
         static final String M_DESCRIPTION="Arbitary content that is stored and managed inside the "
                 + "repository.  This is similar to internal XML metadata, but it does not have "
                 + "any format restrictions, and is delieved as-is from the repository.";
         static final String E_DESCRIPTION="Content that is not managed by Fedora, "
                 + "and is ultimately hosted on some other server.  Each time the "
                 + "content is accessed, Fedora will request it from its host and "
                 + "send it to the client.";
         static final String R_DESCRIPTION="Fedora will send clients a redirect to the URL "
                 + "you specify for this datastream.  This is useful in situations where the content "
                 + "must be delivered by a special streaming server, it contains "
                 + "relative hyperlinks, or there are licensing or access restrictions that prevent "
                 + "it from being proxied.";
 
         private JComboBox m_stateComboBox;
         private String m_initialState;
 
         public NewDatastreamPane(String[] dropdownMimeTypes) {
 
             JComponent[] left=new JComponent[] { new JLabel("State"),
                                                  new JLabel("ID"),
                                                  new JLabel("Label"),
                                                  new JLabel("MIME Type"),
                                                  new JLabel("Format URI"),
                                                  new JLabel("Alternate IDs"),
                                                  new JLabel("Control Group") };
 
             m_stateComboBox=new JComboBox(new String[] {"Active",
                                                         "Inactive",
                                                         "Deleted"});
             m_initialState="A";
             m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
             Administrator.constrainHeight(m_stateComboBox);
             m_stateComboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     m_initialState=((String) m_stateComboBox.getSelectedItem()).substring(0,1);
                     if (m_initialState.equals("A")) {
                         m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
                     } else if (m_initialState.equals("I")) {
                         m_stateComboBox.setBackground(Administrator.INACTIVE_COLOR);
                     } else if (m_initialState.equals("D")) {
                         m_stateComboBox.setBackground(Administrator.DELETED_COLOR);
                     }
                 }
             });
 
             m_labelTextField=new JTextField("Enter a label here.");
 
             m_idTextField=new JTextField("");
             m_formatURITextField = new JTextField("");
             m_altIDsTextField = new JTextField("");
 
             m_mimeComboBox=new JComboBox(dropdownMimeTypes);
             Administrator.constrainHeight(m_mimeComboBox);
             m_mimeComboBox.setEditable(true);
             JPanel controlGroupPanel=new JPanel();
             JRadioButton xButton=new JRadioButton("Internal XML Metadata");
             xButton.setSelected(true);
             m_controlGroup="X";
             xButton.setActionCommand("X");
             xButton.addActionListener(this);
             JRadioButton mButton=new JRadioButton("Managed Content");
             mButton.setActionCommand("M");
             mButton.addActionListener(this);
             JRadioButton eButton=new JRadioButton("External Referenced Content");
             eButton.setActionCommand("E");
             eButton.addActionListener(this);
             JRadioButton rButton=new JRadioButton("Redirect");
             rButton.setActionCommand("R");
             rButton.addActionListener(this);
             ButtonGroup group=new ButtonGroup();
             group.add(xButton); group.add(mButton); group.add(eButton); group.add(rButton);
             controlGroupPanel.setLayout(new GridLayout(0, 1));
             controlGroupPanel.add(xButton);
             controlGroupPanel.add(mButton);
             controlGroupPanel.add(eButton);
             controlGroupPanel.add(rButton);
             JPanel controlGroupOuterPanel=new JPanel(new BorderLayout());
             controlGroupOuterPanel.add(controlGroupPanel, BorderLayout.WEST);
             m_controlGroupTextArea=new JTextArea(X_DESCRIPTION);
             m_controlGroupTextArea.setLineWrap(true);
             m_controlGroupTextArea.setEditable(false);
             m_controlGroupTextArea.setWrapStyleWord(true);
             m_controlGroupTextArea.setBackground(controlGroupOuterPanel.getBackground());
 
             controlGroupOuterPanel.add(m_controlGroupTextArea, BorderLayout.CENTER);
 
             JComponent[] right=new JComponent[] { m_stateComboBox, 
                                                   m_idTextField, 
                                                   m_labelTextField, 
                                                   m_mimeComboBox, 
                                                   m_formatURITextField,
                                                   m_altIDsTextField,
                                                   controlGroupOuterPanel };
 
             JPanel commonPane=new JPanel();
             GridBagLayout grid=new GridBagLayout();
             commonPane.setLayout(grid);
             addRows(left, right, grid, commonPane);
 
             m_lastSelectedMimeType=(String) m_mimeComboBox.getSelectedItem();
             m_mimeComboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     String cur=(String) m_mimeComboBox.getSelectedItem();
                     if (!cur.equals(m_lastSelectedMimeType)) {
                         // X: remove the xml parsing restriction if needed
                         m_xEditor.setXML(cur.endsWith("+xml") || cur.endsWith("/xml"));
                         // E/R: in any case, remove the prior viewer
                         if (m_erViewer!=null) {
                             m_erPane.remove(m_erViewer.getComponent());
                             m_erPane.add(new JLabel(), BorderLayout.CENTER);
                             m_erPane.validate();
                         }
                         if (ContentHandlerFactory.hasViewer(cur)) {
                             m_erViewButton.setEnabled(true);
                         } else {
                             m_erViewButton.setEnabled(false);
                         }
                         // remember the mime type
                         m_lastSelectedMimeType=cur;
                     }
                 }
             });
 
 /*            right=new JComponent[] { m_mdClassComboBox, m_mdTypeComboBox };
             JPanel xTopPane=new JPanel();
             grid=new GridBagLayout();
             xTopPane.setLayout(grid);
             addRows(left, right, grid, xTopPane);
 */
             try {
                 m_xEditor=new TextContentEditor();
                 m_xEditor.init("text/plain", new ByteArrayInputStream(
                         new String("Enter content here, or click \"Import\" below.").
                         getBytes("UTF-8")), false);
                 m_xEditor.setXML(true); // inline xml is always going to be xml,
                                         // initted as text/plain because empty!=valid xml
             } catch (Exception e) { }
             JPanel xBottomPane=new JPanel();
             xBottomPane.setLayout(new FlowLayout());
             JButton xImportButton=new JButton("Import...");
             Administrator.constrainHeight(xImportButton);
             xImportButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     ImportDialog imp=new ImportDialog();
                     if (imp.file!=null) {
                         try {
                             m_xEditor.setContent(new FileInputStream(imp.file));
                         } catch (Exception e) {
                             String msg=e.getMessage();
                             if( msg.indexOf("Error parsing as XML") != -1) {
                               msg = "Imported text does not contain valid XML.\n"
                                   + "Inline XML Metadata datastreams must contain valid XML.";
                             }
                             JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                     msg, "Import Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     }
                 }
             });
             xBottomPane.add(xImportButton);
             JPanel xPane=new JPanel();
             xPane.setLayout(new BorderLayout());
 //            xPane.add(xTopPane, BorderLayout.NORTH);
             xPane.add(m_xEditor.getComponent(), BorderLayout.CENTER);
             xPane.add(xBottomPane, BorderLayout.SOUTH);
 
             // Managed Content Datastream....
             // SOUTH: [Import]
             JPanel mBottomPane=new JPanel(new FlowLayout());
             JButton mImportButton=new JButton("Import...");
             Administrator.constrainHeight(mImportButton);
             mImportButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     ImportDialog imp=new ImportDialog();
                     if (imp.file!=null) {
                         try {
                             // see if we should put a viewer up, or just
                             // a label that says they're importing.
                             JComponent newCenter;
                             String curMime=(String) m_mimeComboBox.getSelectedItem();
                             if (ContentHandlerFactory.hasViewer(curMime)) {
                                 ContentViewer viewer=ContentHandlerFactory.
                                         getViewer(curMime,
                                         new FileInputStream(imp.file));
                                 newCenter=viewer.getComponent();
                             } else {
                                 String importString;
                                 if (imp.url!=null) {
                                     importString="Will import " + imp.url;
                                 } else {
                                     importString="Will import " + imp.file.getPath();
                                 }
                                 newCenter=new JLabel(importString);
                             }
                             // now remove the old center component (if needed),
                             // and add newCenter, then validate
                             if (m_mCenter!=null) {
                                 m_mPane.remove(m_mCenter);
                             }
                             m_mCenter=newCenter;
                             m_mPane.add(m_mCenter, BorderLayout.CENTER);
                             m_mPane.validate();
                             // lastly, set the file we're importing
                             m_managedFile=imp.file;
                         } catch (Exception e) {
                             JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                     e.getMessage(), "Import Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     }
                 }
             });
             mBottomPane.add(mImportButton);
 
             m_mPane=new JPanel(new BorderLayout());
             m_mPane.add(mBottomPane, BorderLayout.SOUTH);
 
             // External Referenced or Redirect Datastream....
             //
             // NORTH: Location  __________________
             // SOUTH:        [View]
             // preview button's actionlistener will only pull up a viewer
             // if the selected mime type is something we have a viewer for.
             JPanel erTopPane=new JPanel(new BorderLayout());
             erTopPane.add(new JLabel("Location  "), BorderLayout.WEST);
             m_referenceTextField=new JTextField("http://");
             erTopPane.add(m_referenceTextField, BorderLayout.CENTER);
 
             JPanel erBottomPane=new JPanel(new FlowLayout());
             m_erViewButton=new JButton("View");
             Administrator.constrainHeight(m_erViewButton);
             m_erViewButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     // get a viewer and put it in the middle of m_erPane
                     // we assume we can get a viewer here because
                     // the view button wouldn't be enabled if that weren't
                     // the case
                     try {
                         String mimeType=(String) m_mimeComboBox.getSelectedItem();
                         m_erViewer=ContentHandlerFactory.getViewer(
                                 mimeType,
                                 Administrator.DOWNLOADER.
                                         get(m_referenceTextField.getText()));
                         m_erPane.add(m_erViewer.getComponent(), BorderLayout.CENTER);
                         m_erPane.validate();
                     } catch (Exception e) {
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                 e.getMessage(), "View error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             });
             erBottomPane.add(m_erViewButton);
             m_erPane=new JPanel(new BorderLayout());
             m_erPane.add(erTopPane, BorderLayout.NORTH);
             m_erPane.add(erBottomPane, BorderLayout.SOUTH);
 
             m_specificPane=new JPanel();
             m_contentCard=new CardLayout();
             m_specificPane.setLayout(m_contentCard);
             m_specificPane.add(xPane, "X");
             m_specificPane.add(m_mPane, "M");
             m_specificPane.add(m_erPane, "ER");
 
             JPanel entryPane=new JPanel();
             entryPane.setLayout(new BorderLayout());
             entryPane.setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createEtchedBorder(),
                     BorderFactory.createEmptyBorder(4,4,4,4)
                     ));
             entryPane.add(commonPane, BorderLayout.NORTH);
             entryPane.add(m_specificPane, BorderLayout.CENTER);
 
             JButton saveButton=new JButton("Save Datastream");
             Administrator.constrainHeight(saveButton);
             saveButton.setActionCommand("Save");
             saveButton.addActionListener(this);
 
             JPanel buttonPane=new JPanel();
             buttonPane.setLayout(new FlowLayout());
             buttonPane.add(saveButton);
 
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
             add(entryPane, BorderLayout.CENTER);
             add(buttonPane, BorderLayout.SOUTH);
         }
 
         public void actionPerformed(ActionEvent evt) {
             String cmd=evt.getActionCommand();
             if (cmd.equals("X")) {
                 m_controlGroupTextArea.setText(X_DESCRIPTION);
                 m_contentCard.show(m_specificPane, "X");
                 m_controlGroup="X";
                 this.removeMIMETypeItems();
             } else if (cmd.equals("M")) {
                 m_controlGroupTextArea.setText(M_DESCRIPTION);
                 m_contentCard.show(m_specificPane, "M");
                 m_controlGroup="M";
                 if(this.m_mimeComboBox.getItemCount() == 1) {
                    this.addMIMETypeItems();
                 }
             } else if (cmd.equals("E")) {
                 m_controlGroupTextArea.setText(E_DESCRIPTION);
                 m_contentCard.show(m_specificPane, "ER");
                 m_controlGroup="E";
                 if(this.m_mimeComboBox.getItemCount() == 1) {
                    this.addMIMETypeItems();
                 }
             } else if (cmd.equals("R")) {
                 m_controlGroupTextArea.setText(R_DESCRIPTION);
                 m_contentCard.show(m_specificPane, "ER");
                 m_controlGroup="R";
                 if(this.m_mimeComboBox.getItemCount() == 1) {
                    this.addMIMETypeItems();
                 }
             } else if (cmd.equals("Save")) {
                 try {
                     // try to save... first set common values for call
                     String pid=m_pid;
                     String dsID=m_idTextField.getText().trim();
                     if (dsID.equals("")) dsID = null;
                     String trimmed = m_altIDsTextField.getText().trim();
                     String[] altIDs;
                     if (trimmed.length() == 0) {
                        altIDs = new String[0];
                     } else if (trimmed.indexOf(" ") == -1) {
                         altIDs = new String[] { trimmed };
                     } else {
                         altIDs = trimmed.split("\\s");
                     }
                     String formatURI = m_formatURITextField.getText().trim();
                     if (formatURI.length() == 0) {
                         formatURI = null;
                     }
                     String label=m_labelTextField.getText();
                     String mimeType=(String) m_mimeComboBox.getSelectedItem();
                     String location=null;
                     if (m_controlGroup.equals("X")) {
                         // m_xEditor
                         location=Administrator.UPLOADER.upload(m_xEditor.getContent());
                     } else if (m_controlGroup.equals("M")) {
                         // get the imported file
                         if (m_managedFile==null) {
                             throw new IOException("Content must be specified first.");
                         }
                         location=Administrator.UPLOADER.upload(m_managedFile);
                     } else { // must be E/R
                         location=m_referenceTextField.getText();
                     }
                     String newID = Administrator.APIM.addDatastream(
                                        pid, 
                                        dsID, 
                                        altIDs,
                                        label,
                                        true, // DEFAULT_VERSIONABLE
                                        mimeType, 
                                        formatURI,
                                        location, 
                                        m_controlGroup, 
                                        m_initialState,
                                        "DatastreamsPane generated this logMessage."); // DEFAULT_LOGMESSAGE
                     addDatastreamTab(newID);
                 } catch (Exception e) {
                    e.printStackTrace();
                     String msg = e.getMessage();
                     if (msg.indexOf("Content is not allowed in prolog") != -1) {
                       msg = "Text entered is not valid XML.\n"
                           + "Internal XML Metadata datastreams must contain valid XML.";
                     }
                     JOptionPane.showMessageDialog(Administrator.getDesktop(),
                             msg, "Error saving new datastream",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
 
         public void addMIMETypeItems() {
           for (int i=1; i<ALL_KNOWN_MIMETYPES.length; i++) {
             this.m_mimeComboBox.addItem(ALL_KNOWN_MIMETYPES[i]);
           }
           this.m_mimeComboBox.setPreferredSize(new Dimension(150,20));
         }
 
         public void removeMIMETypeItems() {
           for (int i=1; i<ALL_KNOWN_MIMETYPES.length; i++) {
             this.m_mimeComboBox.removeItem(ALL_KNOWN_MIMETYPES[i]);
           }
           this.m_mimeComboBox.setPreferredSize(new Dimension(150,20));
         }
     }
 
 }
