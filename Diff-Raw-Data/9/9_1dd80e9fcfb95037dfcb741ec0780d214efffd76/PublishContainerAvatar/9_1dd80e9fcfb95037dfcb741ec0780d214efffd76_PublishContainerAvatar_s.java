 /*
  * Created On: September 22, 2006, 11:15 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.DocumentFilter;
 
 import com.thinkparity.codebase.StringUtil;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.email.EMailFormatException;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.contact.Contact;
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
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author robert@thinkparity.com, raymond@thinkparity.com
  * @verison 1.1.2.18
  */
 public final class PublishContainerAvatar extends Avatar implements
         PublishContainerSwingDisplay {
 
     /** An instance of <code>UserUtils</code>. */
     private static final UserUtils USER_UTIL;
 
     static {
         USER_UTIL = UserUtils.getInstance();
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JPanel buttonBarJPanel = new javax.swing.JPanel();
     private final javax.swing.JTextArea commentJTextArea = new javax.swing.JTextArea();
     private final javax.swing.JTextArea emailsJTextArea = new javax.swing.JTextArea();
     private final javax.swing.JList namesJList = new javax.swing.JList();
     private final javax.swing.JScrollPane namesJScrollPane = new javax.swing.JScrollPane();
     private final javax.swing.JPanel progressBarJPanel = new javax.swing.JPanel();
     private final javax.swing.JButton publishJButton = ButtonFactory.create();
     private final javax.swing.JProgressBar publishJProgressBar = new javax.swing.JProgressBar();
     private final javax.swing.JLabel statusJLabel = new javax.swing.JLabel();
     // End of variables declaration//GEN-END:variables
 
     /** The publish to list <code>PublishContainerAvatarUserListModel</code>. */
     private final PublishContainerAvatarUserListModel namesListModel;
 
     /**
      * Creates PublishContainerAvatar.
      *
      */
     public PublishContainerAvatar() {
         super("PublishContainerDialog", BrowserConstants.DIALOGUE_BACKGROUND);
         this.namesListModel = new PublishContainerAvatarUserListModel();
         initComponents();
         addValidationListener(emailsJTextArea);
         bindEscapeKey();
         namesJScrollPane.getViewport().setOpaque(false);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#dispose()
      */
     public void dispose() {
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
             final String name = ((PublishContainerProvider) contentProvider).readContainerName(getInputContainerId());
             return getString("TitlePublish", new Object[] {name});
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
         final PublishContainerAvatarUserListModel model =
             (PublishContainerAvatarUserListModel) namesJList.getModel();
         if (model.isItemSelected()) {
             try {
                 extractEMails();
                 return Boolean.TRUE;
             } catch (final EMailFormatException efx) {}
         }
         return Boolean.FALSE;
     }
 
     public void reload() {
         reloadProgressBar();
         if (input != null) { 
             reloadPublishTo();           
             reloadComment();
             publishJButton.setEnabled(isInputValid());
             namesJScrollPane.getViewport().setViewPosition(new Point(0,0));
         }
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#resetProgressBar(java.lang.Long)
      * 
      */
     public void resetProgressBar(final Long containerId) {
         reloadProgressBar();
         publishJButton.setEnabled(isInputValid());
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
             /** @see java.io.Serializable */
             private static final long serialVersionUID = 1;
 
             /** @see javax.swing.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
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
     
     /**
      * Extract the user comment.
      * 
      * @return The user comment <code>String</code>.
      */
     private String extractComment() {
         return SwingUtil.extract(commentJTextArea, Boolean.TRUE);
     }
 
     /**
      * Extract the e-mail addresses. The e-mail addresses must be comma
      * separated.
      * 
      * @return A <code>List</code> of <code>EMail</code> addresses.
      */
     private List<EMail> extractEMails() {
         final String text = SwingUtil.extract(emailsJTextArea, Boolean.TRUE);
         final List<EMail> emails;
         if (null == text) {
             emails = Collections.emptyList();
         } else {
             final List<String> emailAddresses = StringUtil.tokenize(text, ",",
                     new ArrayList<String>());
             emails = new ArrayList<EMail>(emailAddresses.size());
             for (final String emailAddress : emailAddresses)
                 emails.add(EMailBuilder.parse(emailAddress.trim()));
         }
         return emails;
     }
 
     /**
      * Obtain the input container id.
      *
      * @return A container id.
      */
     private Long getInputContainerId() {
         if (input!=null) {
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
         if (input!=null) {
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
         if (input!=null) {
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
         final javax.swing.JLabel emailsJLabel = new javax.swing.JLabel();
         final javax.swing.JScrollPane emailsJScrollPane = new javax.swing.JScrollPane();
         final javax.swing.JLabel commentJLabel = new javax.swing.JLabel();
         final javax.swing.JScrollPane commentJScrollPane = new javax.swing.JScrollPane();
         final javax.swing.JLabel fillerJLabel = new javax.swing.JLabel();
         final javax.swing.JButton cancelJButton = ButtonFactory.create();
 
         namesJScrollPane.setBorder(null);
         namesJScrollPane.setOpaque(false);
         namesJList.setFont(Fonts.DialogFont);
         namesJList.setModel(namesListModel);
         namesJList.setCellRenderer(new PublishContainerAvatarUserCellRenderer());
         namesJList.setOpaque(false);
         namesJList.setVisibleRowCount(7);
         namesJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 namesJListMousePressed(evt);
             }
         });
 
         namesJScrollPane.setViewportView(namesJList);
 
         emailsJLabel.setFont(Fonts.DialogFont);
         emailsJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("PublishContainerDialog.Emails"));
 
         emailsJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         emailsJTextArea.setColumns(20);
         emailsJTextArea.setFont(Fonts.DialogTextEntryFont);
         emailsJTextArea.setLineWrap(true);
         emailsJScrollPane.setViewportView(emailsJTextArea);
 
         commentJLabel.setFont(Fonts.DialogFont);
         commentJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("PublishContainerDialog.Comment"));
         commentJLabel.setFocusable(false);
 
         commentJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         commentJTextArea.setFont(Fonts.DialogTextEntryFont);
         commentJTextArea.setLineWrap(true);
         commentJTextArea.setWrapStyleWord(true);
         commentJScrollPane.setViewportView(commentJTextArea);
 
         buttonBarJPanel.setOpaque(false);
         fillerJLabel.setFont(Fonts.DialogFont);
         fillerJLabel.setPreferredSize(new java.awt.Dimension(3, 14));
 
         publishJButton.setFont(Fonts.DialogButtonFont);
         publishJButton.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("PublishContainerDialog.publishJButton"));
         publishJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 publishJButtonActionPerformed(evt);
             }
         });
 
         cancelJButton.setFont(Fonts.DialogButtonFont);
         cancelJButton.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("PublishContainerDialog.Cancel"));
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
                         .add(fillerJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
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
             .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
             .add(statusJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
         );
         progressBarJPanelLayout.setVerticalGroup(
             progressBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(progressBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(statusJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(namesJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                     .add(emailsJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                     .add(emailsJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                     .add(commentJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                     .add(commentJScrollPane)
                     .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(namesJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(emailsJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(emailsJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(commentJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(commentJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(14, 14, 14)
                 .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
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
 
     private void namesJListMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_namesJListMousePressed
        if (e.getButton() == MouseEvent.BUTTON1) {
             PublishContainerAvatarUser user = (PublishContainerAvatarUser)((JList) e.getSource()).getSelectedValue();
             user.toggleSelected();
             final int listModelIndex = namesListModel.indexOf(user);
             if (listModelIndex >= 0) {
                 namesListModel.set(listModelIndex, user);
             }
             publishJButton.setEnabled(isInputValid());
         }
     }//GEN-LAST:event_namesJListMousePressed
 
     /**
      * Publish the container.
      */
     private void publishContainer() {
         final PublishType publishType = getInputPublishType();
         final Long containerId = getInputContainerId();  
         final PublishContainerAvatarUserListModel model = (PublishContainerAvatarUserListModel) namesJList.getModel();
         final List<EMail> emails = extractEMails();
         final List<TeamMember> teamMembers = model.getSelectedTeamMembers();
         final List<Contact> contacts = model.getSelectedContacts();
         
         // Publish
         switch (publishType) {
         case PUBLISH:
             getController().runPublishContainer(createMonitor(), containerId,
                     extractComment(), emails, contacts, teamMembers);
             break;
         case PUBLISH_VERSION:
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
      * Reload the comment control. Normally the control is blank and editable.
      * If we are publishing a specific version then load the existing comment
      * and don't allow edit.
      */
     private void reloadComment() {
         switch (getInputPublishType()) {
         case PUBLISH_VERSION:
             final Long containerId = getInputContainerId();
             final Long versionId = getInputVersionId();
             final String comment = ((PublishContainerProvider) contentProvider).readContainerVersionComment(containerId, versionId);
             commentJTextArea.setText(comment);
             commentJTextArea.setEditable(false);
             commentJTextArea.setFocusable(false);
             break;
         case PUBLISH:
             commentJTextArea.setText(null);
             commentJTextArea.setEditable(true);
             commentJTextArea.setFocusable(true);
             // NOCOMMIT This is here as a stopgap until more general code is in place.
             final Document document = commentJTextArea.getDocument();
             if (document instanceof AbstractDocument) {
                 ((AbstractDocument)document).setDocumentFilter(new DocumentSizeFilter(128));
             }
             break;
         default:
             Assert.assertUnreachable("Unknown publish type.");
         }
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
      * Reload the list of team members and contacts in the publish to list.
      *
      */
     private void reloadPublishTo() {
         namesListModel.clear();
 
         final List<TeamMember> teamMembers = readTeamMembers();
         final List<Contact> contacts = readContacts(teamMembers);
         final boolean autoSelect;
         switch (getPublishTypeSpecific()) {
         case PUBLISH_FIRST_TIME:
         case PUBLISH_VERSION:
             autoSelect = false;
             break;
         case PUBLISH_NOT_FIRST_TIME:
             autoSelect = true;            
             break;
         default:
             throw Assert.createUnreachable("Unknown publish type");
         }
         // populate team members
         if (autoSelect) {
             final User updatedBy = readLatestVersionUpdatedBy();
             final List<ArtifactReceipt> publishedTo = readLatestVersionPublishedTo();
             for (final TeamMember teamMember : teamMembers) {
                 namesListModel.addElement(new PublishContainerAvatarUser(
                         teamMember, isVersionRecipient(teamMember, updatedBy,
                                 publishedTo)));
             }
         } else {
             for (final TeamMember teamMember : teamMembers) {
                 namesListModel.addElement(new PublishContainerAvatarUser(
                         teamMember, Boolean.FALSE));
             }
         }
         // add a spacer if necessary to separate team members from contacts
         if (!teamMembers.isEmpty()) {
             namesListModel.addElement(new PublishContainerAvatarUser(null, Boolean.FALSE));
         }
         // populate contacts
         for (final Contact contact : contacts) {
             namesListModel.addElement(new PublishContainerAvatarUser(contact, Boolean.FALSE));
         }
     }
 
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
         
     // NOCOMMIT This is here as a stopgap to prevent a possible crash in beta, until more general
     // code is in place to handle max string length (ticket #610). Insert text is truncated if
     // too long. If replace text is too long it is ignored.
     private class DocumentSizeFilter extends DocumentFilter {
         final int maxCharacters;
 
         public DocumentSizeFilter(final int maxCharacters) {
             this.maxCharacters = maxCharacters;
         }
         /**
          * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass,
          *      int, java.lang.String, javax.swing.text.AttributeSet)
          * 
          */
         public void insertString(final FilterBypass fb, final int offs,
                                  final String str, final AttributeSet a)
             throws BadLocationException {
             if (null == str || (fb.getDocument().getLength() + str.length()) <= maxCharacters) {
                 super.insertString(fb, offs, str, a);
             } else {
                 final int length = maxCharacters - fb.getDocument().getLength();
                 super.insertString(fb, offs, str.substring(0, length), a);
             }
         }
         /**
          * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass,
          *      int, int, java.lang.String, javax.swing.text.AttributeSet)
          * 
          */
         public void replace(FilterBypass fb, int offs,
                             int length, 
                             String str, AttributeSet a)
             throws BadLocationException {
             if (null == str || (fb.getDocument().getLength() + str.length()
                     - length) <= maxCharacters) {
                 super.replace(fb, offs, length, str, a);
             }
         }
     }
 
     private class PublishContainerAvatarUserListModel extends DefaultListModel {
         
         public PublishContainerAvatarUserListModel() {
             super();
         }
         
         public List<Contact> getSelectedContacts() {
             List<Contact> selectedContacts = new ArrayList<Contact>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && (user.getUser() instanceof Contact)) {
                     selectedContacts.add((Contact)user.getUser());
                 }                
             }
 
             return selectedContacts;
         }
         
         public List<TeamMember> getSelectedTeamMembers() {
             List<TeamMember> selectedTeamMembers = new ArrayList<TeamMember>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && (user.getUser() instanceof TeamMember)) {
                     selectedTeamMembers.add((TeamMember)user.getUser());
                 }                
             }
             
             return selectedTeamMembers;
         }
         
         public Boolean isItemSelected() {
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
