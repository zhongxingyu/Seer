 /*
  * Created On: September 22, 2006, 11:15 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container;
 
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.text.AbstractDocument;
 
 import com.thinkparity.codebase.FuzzyDateFormat;
 import com.thinkparity.codebase.StringUtil;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.email.EMailFormatException;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.text.JTextComponentLengthFilter;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.container.ContainerConstraints;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.user.UserUtils;
 
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.ButtonFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.dialog.container.PublishContainerProvider;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.dialog.container.PublishContainerAvatarUserCellRenderer;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingMonitor;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.util.State;
 
 /**
  * <b>Title:</b>thinkParity OpheliaUI Publish Container Avatar<br>
  * <b>Description:</b><br>
  * 
  * @author robert@thinkparity.com, raymond@thinkparity.com
  * @verison 1.1.2.61
  */
 public final class PublishContainerAvatar extends Avatar implements
         PublishContainerSwingDisplay {
 
     /** A <code>FuzzyDateFormat</code>. */
     private static final FuzzyDateFormat FUZZY_DATE_FORMAT;
 
     /** An instance of <code>UserUtils</code>. */
     private static final UserUtils USER_UTIL;
 
     static {
         FUZZY_DATE_FORMAT = new FuzzyDateFormat();
         USER_UTIL = UserUtils.getInstance();
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JPanel buttonBarJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel contactsJLabel = new javax.swing.JLabel();
     private final javax.swing.JList contactsJList = new javax.swing.JList();
     private final javax.swing.JScrollPane contactsJScrollPane = new javax.swing.JScrollPane();
     private final javax.swing.JTextField emailsJTextField = new javax.swing.JTextField();
     private final javax.swing.JPanel progressBarJPanel = new javax.swing.JPanel();
     private final javax.swing.JButton publishJButton = ButtonFactory.create();
     private final javax.swing.JProgressBar publishJProgressBar = new javax.swing.JProgressBar();
     private final javax.swing.JLabel statusJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel teamMembersJLabel = new javax.swing.JLabel();
     private final javax.swing.JList teamMembersJList = new javax.swing.JList();
     private final javax.swing.JScrollPane teamMembersJScrollPane = new javax.swing.JScrollPane();
     private final javax.swing.JTextField versionNameJTextField = new javax.swing.JTextField();
     // End of variables declaration//GEN-END:variables
 
     /** The contacts list <code>PublishContainerAvatarUserListModel</code>. */
     private final PublishContainerAvatarUserListModel contactsListModel;
 
     /** An instance of <code>ContainerConstraints</code>. */
     private final ContainerConstraints containerConstraints;
 
     /** A list of email delimiters. */
     private List<String>emailDelimiters;
 
     /** The team members list <code>PublishContainerAvatarUserListModel</code>. */
     private final PublishContainerAvatarUserListModel teamMembersListModel;
 
     /** The default version name <code>String</code>. */
     private String defaultVersionName;
 
     /**
      * Creates PublishContainerAvatar.
      */
     public PublishContainerAvatar() {
         super("PublishContainerAvatar", BrowserConstants.DIALOGUE_BACKGROUND);
         this.containerConstraints = ContainerConstraints.getInstance();
         this.contactsListModel = new PublishContainerAvatarUserListModel();
         this.teamMembersListModel = new PublishContainerAvatarUserListModel();
         initEmailDelimiters();
         initComponents();
         addValidationListener(emailsJTextField);
         bindEscapeKey();
         contactsJScrollPane.getViewport().setOpaque(false);
         teamMembersJScrollPane.getViewport().setOpaque(false);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#dispose()
      */
     public void dispose() {
         showBusyIndicators(Boolean.FALSE);
         disposeWindow();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getAvatarTitle()
      */
     @Override
     public String getAvatarTitle() {
         if (input==null) {
             return getString("Title");
         } else {
             final PublishType publishType = getInputPublishType();
             final String name = ((PublishContainerProvider) contentProvider).readContainerName(getInputContainerId());
             if (publishType == PublishType.PUBLISH_VERSION) {
                 return getString("TitlePublishVersion", new Object[] {name});
             } else {
                 return getString("TitlePublish", new Object[] {name});
             }
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
      */
     @Override
     public AvatarId getId() {
         return AvatarId.DIALOG_CONTAINER_PUBLISH;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getState()
      */
     @Override
     public State getState() {
         return null;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#installProgressBar(java.lang.Long)
      *
      */
     public void installProgressBar(final Long containerId) {
         publishJProgressBar.setIndeterminate(true);
         progressBarJPanel.setVisible(true);
         buttonBarJPanel.setVisible(false);
         validate();
     }
 
     /**
      * Determine whether the user input for the dialog is valid.
      * 
      * @return True if the input is valid; false otherwise.
      */
     public Boolean isInputValid() {        
         final PublishContainerAvatarUserListModel teamMembersModel =
             (PublishContainerAvatarUserListModel) teamMembersJList.getModel();
         final PublishContainerAvatarUserListModel contactsModel =
             (PublishContainerAvatarUserListModel) contactsJList.getModel();
         if (teamMembersModel.isItemSelected() || contactsModel.isItemSelected()) {
             try {
                 extractEMails();
                 return Boolean.TRUE;
             } catch (final EMailFormatException efx) {}
         } else {
             try {
                 final List<EMail> emails = extractEMails();
                 return 0 < emails.size();
             } catch (final EMailFormatException efx) {}
         }
         return Boolean.FALSE;
     }
 
     public void reload() {
         reloadProgressBar();
         if (input != null) { 
             reloadPublishToLists();
             reloadEMails();
             reloadVersionName();
             publishJButton.setEnabled(isInputValid());
             contactsJScrollPane.getViewport().setViewPosition(new Point(0,0));
             teamMembersJScrollPane.getViewport().setViewPosition(new Point(0,0));
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#resetProgressBar(java.lang.Long)
      * 
      */
     public void resetProgressBar(final Long containerId) {
         reloadProgressBar();
         publishJButton.setEnabled(isInputValid());
         showBusyIndicators(Boolean.FALSE);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#setDetermination(java.lang.Long, java.lang.Integer)
      *
      */
     public void setDetermination(final Long containerId, final Integer steps) {
         publishJProgressBar.setMinimum(1);
         publishJProgressBar.setMaximum(steps);
         publishJProgressBar.setIndeterminate(false);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setState(com.thinkparity.ophelia.browser.platform.util.State)
      */
     @Override
     public void setState(State state) {        
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#updateProgress(java.lang.Long, java.lang.Integer, java.lang.String)
      *
      */
     public void updateProgress(final Long containerId, final Integer step,
             final String status) {
         publishJProgressBar.setValue(step);
         if (null != status) {
             statusJLabel.setText(status);
         } else {
             /* NOTE the space is deliberate (as opposed to an empty string) in
              * order to maintain vertical spacing. */
             statusJLabel.setText(" ");
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#validateInput()
      *
      */
     @Override
     protected void validateInput() {
         super.validateInput();
         try {
             extractEMails();
         } catch (final EMailFormatException efx) {
             addInputError(Separator.EmptyString.toString());
         }
 
         publishJButton.setEnabled(!containsInputErrors());
     }
 
     /**
      * Make the escape key behave like cancel.
      */
     private void bindEscapeKey() {
         bindEscapeKey("Cancel", new AbstractAction() {
             public void actionPerformed(final ActionEvent e) {
                 cancelJButtonActionPerformed(e);
             }
         });
     }
 
     private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
         disposeWindow();
     }//GEN-LAST:event_cancelJButtonActionPerformed
 
     private ThinkParitySwingMonitor createMonitor() {
         return new PublishContainerSwingMonitor(this, getInputContainerId());
     }
 
     private void contactsJListMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactsJListMousePressed
         listMousePressed(contactsListModel, e);
     }//GEN-LAST:event_contactsJListMousePressed
 
     /**
      * Enable or disable text entry in the dialog.
      * 
      * @param enable
      *            The enable <code>Boolean</code>.
      */
     private void enableTextEntry(final Boolean enable) {
         enableTextEntry(emailsJTextField, enable);
         enableTextEntry(versionNameJTextField, enable && PublishType.PUBLISH == getInputPublishType());
     }
 
     /**
      * Enable or disable text entry for a text field.
      * 
      * @param jTextField
      *            A <code>JTextField</code>.
      * @param enable
      *            The enable <code>Boolean</code>.
      */
     private void enableTextEntry(final javax.swing.JTextField jTextField, final Boolean enable) {
         jTextField.setEditable(enable);
         jTextField.setFocusable(enable);
         jTextField.setOpaque(enable);
     }
 
     /**
      * Extract the e-mail addresses. The e-mail addresses must be
      * separated by commas or semicolons.
      * 
      * @return A <code>List</code> of <code>EMail</code> addresses.
      */
     private List<EMail> extractEMails() {
         final String text = SwingUtil.extract(emailsJTextField, Boolean.TRUE);
         final List<EMail> emails;
         if (null == text) {
             emails = Collections.emptyList();
         } else {
             final List<String> emailAddresses = StringUtil.tokenize(text,
                     emailDelimiters, new ArrayList<String>());
             emails = new ArrayList<EMail>(emailAddresses.size());
             for (final String emailAddress : emailAddresses)
                 emails.add(EMailBuilder.parse(emailAddress.trim()));
         }
         return emails;
     }
 
     /**
      * Extract the version name.
      * Returns null if the user made no change to the default name.
      * 
      * @return A version name <code>String</code>.
      */
     private String extractVersionName() {
         final String versionName = SwingUtil.extract(versionNameJTextField, Boolean.TRUE);
        if (null == versionName || versionName.equals(defaultVersionName)) {
             return null;
         } else {
             return versionName;
         }
     }
 
     /**
      * Obtain the input container id.
      *
      * @return A container id.
      */
     private Long getInputContainerId() {
         if (input != null) {
             return (Long) ((Data) input).get(DataKey.CONTAINER_ID);
         } else {
             return null;
         }
     }
 
     /**
      * Obtain the input publish type.
      *
      * @return A PublishType.
      */
     private PublishType getInputPublishType() {
         if (input != null) {
             return (PublishType) ((Data) input).get(DataKey.PUBLISH_TYPE);
         } else {
             return null;
         }
     }
 
     /**
      * Obtain the input version id.
      *
      * @return A version id.
      */
     private Long getInputVersionId() {
         if (input != null) {
             return (Long) ((Data) input).get(DataKey.VERSION_ID);
         } else {
             return null;
         }
     }
 
     /**
      * Obtain the specific publish type.
      * 
      * @return A PublishTypeSpecific.
      */
     private PublishTypeSpecific getPublishTypeSpecific() {
         final PublishType publishType = getInputPublishType();
         if (publishType==PublishType.PUBLISH_VERSION) {
             return PublishTypeSpecific.PUBLISH_VERSION;
         } else {
             final Long containerId = getInputContainerId();
             final Long versionId = readLatestVersionId(containerId);
             if (null == versionId) {
                 return PublishTypeSpecific.PUBLISH_FIRST_TIME;
             } else {
                 return PublishTypeSpecific.PUBLISH_NOT_FIRST_TIME;
             }
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         final javax.swing.JLabel versionNameJLabel = new javax.swing.JLabel();
         final javax.swing.JSeparator versionNameJSeparator = new javax.swing.JSeparator();
         final javax.swing.JLabel emailsJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel fillerJLabel = new javax.swing.JLabel();
         final javax.swing.JButton cancelJButton = ButtonFactory.create();
 
         versionNameJLabel.setFont(Fonts.DialogFont);
         versionNameJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.VersionName"));
 
         versionNameJTextField.setFont(Fonts.DialogTextEntryFont);
         ((AbstractDocument) versionNameJTextField.getDocument()).setDocumentFilter(new JTextComponentLengthFilter(containerConstraints.getVersionName()));
 
         teamMembersJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.TeamMembers"));
 
         teamMembersJScrollPane.setBorder(null);
         teamMembersJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         teamMembersJScrollPane.setOpaque(false);
         teamMembersJList.setFont(Fonts.DialogFont);
         teamMembersJList.setModel(teamMembersListModel);
         teamMembersJList.setCellRenderer(new PublishContainerAvatarUserCellRenderer());
         teamMembersJList.setOpaque(false);
         teamMembersJList.setVisibleRowCount(4);
         teamMembersJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 teamMembersJListMousePressed(evt);
             }
         });
 
         teamMembersJScrollPane.setViewportView(teamMembersJList);
 
         contactsJLabel.setFont(Fonts.DialogFont);
         contactsJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.Contacts"));
 
         contactsJScrollPane.setBorder(null);
         contactsJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         contactsJScrollPane.setOpaque(false);
         contactsJList.setFont(Fonts.DialogFont);
         contactsJList.setModel(contactsListModel);
         contactsJList.setCellRenderer(new PublishContainerAvatarUserCellRenderer());
         contactsJList.setOpaque(false);
         contactsJList.setVisibleRowCount(4);
         contactsJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 contactsJListMousePressed(evt);
             }
         });
 
         contactsJScrollPane.setViewportView(contactsJList);
 
         emailsJLabel.setFont(Fonts.DialogFont);
         emailsJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.Emails"));
 
         emailsJTextField.setFont(Fonts.DialogTextEntryFont);
         ((AbstractDocument) emailsJTextField.getDocument()).setDocumentFilter(new JTextComponentLengthFilter(containerConstraints.getEMails()));
 
         buttonBarJPanel.setOpaque(false);
         fillerJLabel.setFont(Fonts.DialogFont);
         fillerJLabel.setPreferredSize(new java.awt.Dimension(3, 14));
 
         publishJButton.setFont(Fonts.DialogButtonFont);
         publishJButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.Publish"));
         publishJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 publishJButtonActionPerformed(evt);
             }
         });
 
         cancelJButton.setFont(Fonts.DialogButtonFont);
         cancelJButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.Cancel"));
         cancelJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelJButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout buttonBarJPanelLayout = new org.jdesktop.layout.GroupLayout(buttonBarJPanel);
         buttonBarJPanel.setLayout(buttonBarJPanelLayout);
         buttonBarJPanelLayout.setHorizontalGroup(
             buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(buttonBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonBarJPanelLayout.createSequentialGroup()
                         .add(publishJButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cancelJButton))
                     .add(buttonBarJPanelLayout.createSequentialGroup()
                         .add(fillerJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                         .add(71, 71, 71))))
         );
         buttonBarJPanelLayout.setVerticalGroup(
             buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(buttonBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(fillerJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(cancelJButton)
                     .add(publishJButton))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         progressBarJPanel.setOpaque(false);
         statusJLabel.setFont(Fonts.DialogFont);
         statusJLabel.setText("Uploading document.pdf...");
 
         publishJProgressBar.setBorder(null);
         publishJProgressBar.setBorderPainted(false);
         publishJProgressBar.setOpaque(false);
 
         org.jdesktop.layout.GroupLayout progressBarJPanelLayout = new org.jdesktop.layout.GroupLayout(progressBarJPanel);
         progressBarJPanel.setLayout(progressBarJPanelLayout);
         progressBarJPanelLayout.setHorizontalGroup(
             progressBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
             .add(statusJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
         );
         progressBarJPanelLayout.setVerticalGroup(
             progressBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(progressBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(statusJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(layout.createSequentialGroup()
                         .add(versionNameJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                         .add(260, 260, 260))
                     .add(layout.createSequentialGroup()
                         .add(versionNameJSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(teamMembersJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(contactsJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(teamMembersJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 341, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(versionNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 344, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE)
                         .add(contactsJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(emailsJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(emailsJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .add(layout.createSequentialGroup()
                         .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())))
         );
 
         layout.linkSize(new java.awt.Component[] {contactsJScrollPane, emailsJTextField, teamMembersJScrollPane, versionNameJTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(27, 27, 27)
                 .add(versionNameJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(versionNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(14, 14, 14)
                 .add(versionNameJSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(teamMembersJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(teamMembersJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(14, 14, 14)
                 .add(contactsJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(contactsJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(15, 15, 15)
                 .add(emailsJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(emailsJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(14, 14, 14)
                 .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Initialize the email delimiters.
      */
     private void initEmailDelimiters() {
         this.emailDelimiters = new ArrayList<String>(2);
         emailDelimiters.add(",");
         emailDelimiters.add(";");
     }
 
     /**
      * Determine whether or not the user is a version recipient. The user can be
      * a recipient if they are either the updated by user or in the publish to
      * list.
      * 
      * @param user
      *            A <code>User</code>.
      * @param updatedBy
      *            The updated by <code>User</code> of the previous version.
      * @param publishedTo
      *            The published to list of <code>ArtifactReceipt</code> of the previous version.
      * @return True if the user is a version recipient.
      */
     private boolean isVersionRecipient(final User user, final User updatedBy,
             final List<ArtifactReceipt> publishedTo) {
         final List<User> versionRecipientUsers = new ArrayList<User>(publishedTo.size()+1);
         versionRecipientUsers.add(updatedBy);
         for (final ArtifactReceipt artifactReceipt : publishedTo) {
             versionRecipientUsers.add(artifactReceipt.getUser());
         }
         return USER_UTIL.contains(versionRecipientUsers, user);
     }
 
     /**
      * Handle mouse press on a list.
      * 
      * @param listModel
      *            A <code>PublishContainerAvatarUserListModel</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void listMousePressed(final PublishContainerAvatarUserListModel listModel,
             final java.awt.event.MouseEvent e) {
         if (Cursor.WAIT_CURSOR != e.getComponent().getCursor().getType()) {
             if (e.getButton() == MouseEvent.BUTTON1 && !listModel.isEmpty()) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)((JList) e.getSource()).getSelectedValue();
                 user.toggleSelected();
                 final int listModelIndex = listModel.indexOf(user);
                 if (listModelIndex >= 0) {
                     listModel.set(listModelIndex, user);
                 }
                 publishJButton.setEnabled(isInputValid());
             }
         }
     }
 
     /**
      * Publish the container.
      */
     private void publishContainer() {
         final PublishType publishType = getInputPublishType();
         final Long containerId = getInputContainerId();
         final PublishContainerAvatarUserListModel contactsModel = 
             (PublishContainerAvatarUserListModel) contactsJList.getModel();
         final PublishContainerAvatarUserListModel teamMembersModel = 
             (PublishContainerAvatarUserListModel) teamMembersJList.getModel();
         final List<EMail> emails = extractEMails();
         final List<Contact> contacts = contactsModel.getSelectedContacts();
         final List<TeamMember> teamMembers = teamMembersModel.getSelectedTeamMembers();
         final String versionName = extractVersionName();
 
         // Publish
         switch (publishType) {
         case PUBLISH:
             showBusyIndicators(Boolean.TRUE);
             getController().runPublishContainer(createMonitor(), containerId,
                     versionName, emails, contacts, teamMembers);
             break;
         case PUBLISH_VERSION:
             showBusyIndicators(Boolean.TRUE);
             final Long versionId = getInputVersionId();
             getController().runPublishContainerVersion(createMonitor(),
                     containerId, versionId, emails, contacts, teamMembers);
             break;
         default:
             Assert.assertUnreachable("Unknown publish type.");
         }  
     }
 
     private void publishJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishJButtonActionPerformed
         if (isInputValid()) {
             publishJButton.setEnabled(false);
             publishContainer();
         }
     }//GEN-LAST:event_publishJButtonActionPerformed
 
     /**
      * Read contacts. The list does not include any contacts
      * that are in the list of team members.
      * 
      * @param teamMembers
      *          The list of team members.
      * 
      * @return The list of contacts.
      */
     private List<Contact> readContacts(List<TeamMember> teamMembers) {
         return ((PublishContainerProvider) contentProvider)
                 .readPublishToContacts(teamMembers);
     }
 
     /**
      * Get most recent version id, or null if there is no version.
      */
     private Long readLatestVersionId(final Long containerId) {
         return ((PublishContainerProvider)contentProvider).readLatestVersionId(containerId);
     } 
 
     /**
      * Read users that got this version.
      */
     private List<ArtifactReceipt> readLatestVersionPublishedTo() {
         return ((PublishContainerProvider) contentProvider).readLatestVersionPublishedTo(
                 getInputContainerId());
     }
 
     /**
      * Get the publisher of the most recent version.
      */
     private User readLatestVersionUpdatedBy() {
         return ((PublishContainerProvider) contentProvider).readLatestVersionUpdatedBy(
                 getInputContainerId());
     }
 
     /**
      * Read the team members for the container.
      * 
      * @return A <code>List</code> of <code>TeamMember</code>s.
      */
     private List<TeamMember> readTeamMembers() {
         return ((PublishContainerProvider) contentProvider).readPublishToTeam(
                 getInputContainerId());
     }
 
     /**
      * Reload the e-mail addresses text area.
      *
      */
     private void reloadEMails() {
         emailsJTextField.setText(null);
     }
 
     private void reloadProgressBar() {
         buttonBarJPanel.setVisible(true);
         progressBarJPanel.setVisible(false);
         /* NOTE the space is deliberate (as opposed to an empty string) in
          * order to maintain vertical spacing. */
         statusJLabel.setText(" ");
         validate();
     }
 
     /**
      * Reload the lists of team members and contacts.
      */
     private void reloadPublishToLists() {
         final PublishTypeSpecific publishType = getPublishTypeSpecific();
         final boolean autoSelect = (PublishTypeSpecific.PUBLISH_NOT_FIRST_TIME == publishType);
         teamMembersJLabel.setVisible(PublishTypeSpecific.PUBLISH_FIRST_TIME != publishType);
         teamMembersJScrollPane.setVisible(PublishTypeSpecific.PUBLISH_FIRST_TIME != publishType);
         if (PublishTypeSpecific.PUBLISH_FIRST_TIME == publishType) {
             contactsJLabel.setText(getString("ContactsFirstPublish"));
         } else {
             contactsJLabel.setText(getString("Contacts"));
         }
         teamMembersListModel.clear();
         contactsListModel.clear();
 
         // read team members and contacts
         final List<TeamMember> teamMembers = readTeamMembers();
         final List<Contact> contacts = readContacts(teamMembers);
 
         // populate team members
         if (autoSelect) {
             final User updatedBy = readLatestVersionUpdatedBy();
             final List<ArtifactReceipt> publishedTo = readLatestVersionPublishedTo();
             for (final TeamMember teamMember : teamMembers) {
                 teamMembersListModel.addElement(new PublishContainerAvatarUser(
                         teamMember, isVersionRecipient(teamMember, updatedBy,
                                 publishedTo)));
             }
         } else {
             for (final TeamMember teamMember : teamMembers) {
                 teamMembersListModel.addElement(new PublishContainerAvatarUser(
                         teamMember, Boolean.FALSE));
             }
         }
 
         // populate contacts
         for (final Contact contact : contacts) {
             contactsListModel.addElement(new PublishContainerAvatarUser(contact, Boolean.FALSE));
         }
     }
 
     /**
      * Reload the version name.
      */
     private void reloadVersionName() {
         switch (getInputPublishType()) {
         case PUBLISH_VERSION:
             final Long containerId = getInputContainerId();
             final Long versionId = getInputVersionId();
             final String name = ((PublishContainerProvider) contentProvider).readVersionName(containerId, versionId);
             versionNameJTextField.setText(name);
             enableTextEntry(versionNameJTextField, false);
             break;
         case PUBLISH:
             defaultVersionName = FUZZY_DATE_FORMAT.format(Calendar.getInstance());
             versionNameJTextField.setText(defaultVersionName);
             versionNameJTextField.selectAll();
             enableTextEntry(versionNameJTextField, true);
             break;
         default:
             Assert.assertUnreachable("Unknown publish type.");
         }
     }
 
     /**
      * Show or remove a busy cursor.
      * 
      * @param busy
      *            The busy <code>Boolean</code>.
      */
     private void showBusyCursor(final Boolean busy) {
         final java.awt.Cursor cursor = Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR);
         SwingUtil.setCursor(this, cursor);
         // NOTE Setting the cursor on the Avatar does not automatically
         // set the cursor on these subcomponents
         SwingUtil.setCursor(emailsJTextField, cursor);
         SwingUtil.setCursor(versionNameJTextField, cursor);
     }
 
     /**
      * Show or remove busy indicators for the dialog.
      * 
      * @param busy
      *            The busy <code>Boolean</code>.
      */
     private void showBusyIndicators(final Boolean busy) {
         showBusyCursor(busy);
         enableTextEntry(!busy);
     }
 
     private void teamMembersJListMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_teamMembersJListMousePressed
         listMousePressed(teamMembersListModel, e);
     }//GEN-LAST:event_teamMembersJListMousePressed
 
     public enum DataKey { CONTAINER_ID, PUBLISH_TYPE, VERSION_ID }
 
     public class PublishContainerAvatarUser {
 
         /** The selection status. */
         private Boolean selected;
 
         /** The user. */
         private User user;
 
         public PublishContainerAvatarUser(final User user, final Boolean selected) {
             this.user = user;
             this.selected = selected;
         }
 
         public String getExtendedName() {
             if (null == user.getTitle()) {
                 logger.logWarning("{0}{0}{0}TITLE IS NULL{0}{1}{0}{2}{0}{3}{0}{4}{0}{5}{0}{6}{0}{7}{0}{8}{0}{0}{0}",
                         "\r\n", user.getFlags(), user.getId(), user.getLocalId(),
                         user.getName(), user.getOrganization(),
                         user.getSimpleUsername(), user.getUsername(),
                         user.getClass());
             }
             return localization.getString("UserName",
                     new Object[] {user.getName(), user.getTitle(), user.getOrganization()} );
         }
 
         public User getUser() {
             return user;
         }
 
         public Boolean isSelected() {
             if (null == user) {
                 return Boolean.FALSE;
             } else {
                 return selected;
             }
         }
 
         public void toggleSelected() {
             selected = !selected;
         }
     }
 
     public enum PublishType { PUBLISH, PUBLISH_VERSION }
 
     private class PublishContainerAvatarUserListModel extends DefaultListModel {
 
         private PublishContainerAvatarUserListModel() {
             super();
         }
 
         private List<Contact> getSelectedContacts() {
             List<Contact> selectedContacts = new ArrayList<Contact>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && (user.getUser() instanceof Contact)) {
                     selectedContacts.add((Contact)user.getUser());
                 }                
             }
             return selectedContacts;
         }
 
         private List<TeamMember> getSelectedTeamMembers() {
             List<TeamMember> selectedTeamMembers = new ArrayList<TeamMember>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && (user.getUser() instanceof TeamMember)) {
                     selectedTeamMembers.add((TeamMember)user.getUser());
                 }                
             }
             return selectedTeamMembers;
         }
 
         private Boolean isItemSelected() {
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected()) {
                     return Boolean.TRUE;
                 }
             }
             return Boolean.FALSE;
         }
     }
 
     private enum PublishTypeSpecific { PUBLISH_FIRST_TIME, PUBLISH_NOT_FIRST_TIME, PUBLISH_VERSION }
 }
