 /*
  * Created On: September 22, 2006, 11:15 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container;
 
 import java.awt.Cursor;
 import java.awt.GridBagConstraints;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.KeyStroke;
 import javax.swing.text.AbstractDocument;
 
 import com.thinkparity.codebase.FuzzyDateFormat;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.text.JTextFieldLengthFilter;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.PublishedToEMail;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.container.ContainerConstraints;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.user.UserUtils;
 
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Colours;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.ButtonFactory;
 import com.thinkparity.ophelia.browser.application.browser.component.TextFactory;
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
     private final javax.swing.JLabel errorMessageJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel progressBarJPanel = new javax.swing.JPanel();
     private final javax.swing.JButton publishJButton = ButtonFactory.create();
     private final javax.swing.JProgressBar publishJProgressBar = new javax.swing.JProgressBar();
     private final javax.swing.JLabel publishToUserJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel publishToUserJPanel = new javax.swing.JPanel();
     private final javax.swing.JScrollPane publishToUserJScrollPane = new javax.swing.JScrollPane();
     private final javax.swing.JTextArea publishToUserJTextArea = new PublishToUserJTextArea(this);
     private final javax.swing.JLabel statusJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel teamMembersJLabel = new javax.swing.JLabel();
     private final javax.swing.JList teamMembersJList = new javax.swing.JList();
     private final javax.swing.JScrollPane teamMembersJScrollPane = new javax.swing.JScrollPane();
     private final javax.swing.JLabel versionNameJLabel = new javax.swing.JLabel();
     private final javax.swing.JTextField versionNameJTextField = TextFactory.create();
     // End of variables declaration//GEN-END:variables
 
     /** An instance of <code>ContainerConstraints</code>. */
     private final ContainerConstraints containerConstraints;
 
     /** The default version name <code>String</code>. */
     private String defaultVersionName;
 
     /** The team members list <code>PublishContainerAvatarUserListModel</code>. */
     private final PublishContainerAvatarUserListModel teamMembersListModel;
 
     /**
      * Creates PublishContainerAvatar.
      */
     public PublishContainerAvatar() {
         super("PublishContainerAvatar", BrowserConstants.DIALOGUE_BACKGROUND);
         this.containerConstraints = ContainerConstraints.getInstance();
         this.teamMembersListModel = new PublishContainerAvatarUserListModel();
         getPublishToUserControl().setLocalization(localization);
         initComponents();
         addValidationListener(publishToUserJTextArea);
         bindKeys();
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
         setCloseButtonEnabled(Boolean.FALSE);
         validate();
     }
 
     public void reload() {
         reloadProgressBar();
         if (input != null) {
             showBusyIndicators(Boolean.FALSE);
             reloadVersionName();
             reloadPublishToTeamMembersList();
             reloadPublishToUserControl();
             teamMembersJScrollPane.getViewport().setViewPosition(new Point(0,0));
             validateInput();
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#resetProgressBar(java.lang.Long)
      * 
      */
     public void resetProgressBar(final Long containerId) {
         reloadProgressBar();
         validateInput();
         showBusyIndicators(Boolean.FALSE);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#setDetermination(java.lang.Long, java.lang.Integer)
      *
      */
     public void setDetermination(final Long containerId, final Integer steps) {
         publishJProgressBar.setMinimum(0);
         publishJProgressBar.setMaximum(steps);
         publishJProgressBar.setIndeterminate(false);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerSwingDisplay#setError(java.lang.String)
      */
     public void setError(final String errorMessageKey) {
         addInputError(getString(errorMessageKey));
         errorMessageJLabel.setText(getInputErrors().get(0));
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
 
         // Check there is at least one person to publish to.
         // Note that the check for new participants is very basic at this point,
         // more complex validation is done when the publish button is pressed.
         final String publishTo = SwingUtil.extract(publishToUserJTextArea, Boolean.TRUE);
         final PublishContainerAvatarUserListModel teamMembersModel =
             (PublishContainerAvatarUserListModel) teamMembersJList.getModel();
         if (!teamMembersModel.isItemSelected() && null == publishTo) {
             addInputError(Separator.Space.toString());
         }
 
         errorMessageJLabel.setText(" ");
         if (containsInputErrors()) {
             errorMessageJLabel.setText(getInputErrors().get(0));
         }
         publishJButton.setEnabled(!containsInputErrors());
     }
 
     /**
      * Bind keys to actions.
      */
     private void bindKeys() {
         bindEscapeKey("Cancel", new AbstractAction() {
             public void actionPerformed(final ActionEvent e) {
                 cancelJButtonActionPerformed(e);
             }
         });
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                 InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
             public void actionPerformed(final ActionEvent e) {
                 publishJButtonActionPerformed(e);
             }
         });
     }
 
     private void cancelJButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
         disposeWindow();
     }//GEN-LAST:event_cancelJButtonActionPerformed
 
     /**
      * Get a combined list of emails.
      * 
      * @param emails
      *            A list of <code>EMail</code>.
      * @param publishToEMails
      *            A list of <code>PublishedToEMail</code>.
      * @return A list of <code>EMail</code>.
      */
     private List<EMail> combineEMails(final List<EMail> emails, final List<PublishedToEMail> publishToEMails) {
         final List<EMail> finalEmails = new ArrayList<EMail>();
         finalEmails.addAll(emails);
         for (final PublishedToEMail publishedToEMail : publishToEMails) {
             final EMail email = publishedToEMail.getEMail();
             if (!finalEmails.contains(email)) {
                 finalEmails.add(email);
             }
         }
         return finalEmails;
     }
 
     /**
      * Get a combined list of team members.
      * 
      * @param team1
      *            A <code>List</code> of <code>TeamMember</code>.
      * @param team2
      *            A <code>List</code> of <code>TeamMember</code>.
      * @return A combined <code>List</code> of <code>TeamMember</code>.
      */
     private List<TeamMember> combineTeamMembers(final List<TeamMember> team1, final List<TeamMember> team2) {
         final List<TeamMember> teamMembers = new ArrayList<TeamMember>();
         teamMembers.addAll(team1);
         for (final TeamMember teamMember : team2) {
             if (!teamMembers.contains(teamMember)) {
                 teamMembers.add(teamMember);
             }
         }
         return teamMembers;
     }
 
     private ThinkParitySwingMonitor createMonitor() {
         return new PublishContainerSwingMonitor(this, getInputContainerId());
     }
 
     /**
      * Enable or disable text entry in the dialog.
      * 
      * @param enable
      *            The enable <code>Boolean</code>.
      */
     private void enableTextEntry(final Boolean enable) {
         setEditable(publishToUserJTextArea, enable);
         setEditable(versionNameJTextField, enable && PublishType.PUBLISH == getInputPublishType());
         publishToUserJScrollPane.setOpaque(enable.booleanValue());
         publishToUserJScrollPane.getViewport().setOpaque(enable.booleanValue());
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
      * Get the publish to user control.
      * 
      * @return The <code>PublishToUserControl</code>.
      */
     private PublishToUserControl getPublishToUserControl() {
         return (PublishToUserControl)publishToUserJTextArea;
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
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JButton cancelJButton = ButtonFactory.create();
 
         versionNameJLabel.setFont(Fonts.DialogFont);
         versionNameJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.VersionName"));
 
         versionNameJTextField.setFont(Fonts.DialogTextEntryFont);
         ((AbstractDocument) versionNameJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(containerConstraints.getVersionName()));
         versionNameJTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 versionNameJTextFieldActionPerformed(evt);
             }
         });
 
         teamMembersJLabel.setFont(Fonts.DialogFont);
         teamMembersJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.PublishTo"));
 
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
 
         publishToUserJPanel.setLayout(new java.awt.GridBagLayout());
 
         publishToUserJPanel.setOpaque(false);
         publishToUserJLabel.setFont(Fonts.DialogFont);
         publishToUserJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("PublishContainerAvatar.PublishToUser"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
         publishToUserJPanel.add(publishToUserJLabel, gridBagConstraints);
 
         publishToUserJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         publishToUserJTextArea.setFont(Fonts.DialogTextEntryFont);
         publishToUserJTextArea.setLineWrap(true);
         publishToUserJTextArea.setTabSize(4);
         publishToUserJTextArea.setWrapStyleWord(true);
         publishToUserJScrollPane.setViewportView(publishToUserJTextArea);
 
         buttonBarJPanel.setOpaque(false);
         errorMessageJLabel.setFont(Fonts.DialogFont);
         errorMessageJLabel.setForeground(Colours.DIALOG_ERROR_TEXT_FG);
         errorMessageJLabel.setText("!Error Message!");
         errorMessageJLabel.setPreferredSize(new java.awt.Dimension(3, 14));
 
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
             .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, errorMessageJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(buttonBarJPanelLayout.createSequentialGroup()
                         .add(publishJButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cancelJButton)))
                 .addContainerGap())
         );
         buttonBarJPanelLayout.setVerticalGroup(
             buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(errorMessageJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(12, 12, 12)
                 .add(buttonBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(cancelJButton)
                     .add(publishJButton))
                 .addContainerGap())
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
             .add(progressBarJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(progressBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .add(statusJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                 .addContainerGap())
         );
         progressBarJPanelLayout.setVerticalGroup(
             progressBarJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, progressBarJPanelLayout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(statusJLabel)
                 .add(12, 12, 12)
                 .add(publishJProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .addContainerGap()
                         .add(teamMembersJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .add(26, 26, 26)
                         .add(publishToUserJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 328, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .add(26, 26, 26)
                         .add(teamMembersJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 326, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(16, 16, 16)))
                 .addContainerGap())
             .add(layout.createSequentialGroup()
                 .add(26, 26, 26)
                 .add(versionNameJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                 .add(26, 26, 26))
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(versionNameJLabel)
                 .addContainerGap(302, Short.MAX_VALUE))
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(publishToUserJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(27, 27, 27)
                 .add(versionNameJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(versionNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(15, 15, 15)
                 .add(teamMembersJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(teamMembersJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(10, 10, 10)
                 .add(publishToUserJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(publishToUserJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(27, 27, 27)
                 .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Initialize the components adjusting the vertical group
      * so that the space used is appropriate for the number of
      * team members.
      * 
      * @param numTeamMembers
      *            The <code>int</code> number of team members.
      */
     private void initComponentsAdjustVerticalSize(final int numTeamMembers) {
         int teamMembersVerticalSize = 72;
         if (4 > numTeamMembers) {
             teamMembersVerticalSize -= (4 - numTeamMembers) * 18;
         }
         // NOTE This is ugly because it repeats generated code in initComponents()
         // but I haven't thought of a better way.
         // This code should match the setVerticalGroup logic in initComponents()
         // but with the preferred size of the teamMembersJScrollPane changed.
         org.jdesktop.layout.GroupLayout layout = (org.jdesktop.layout.GroupLayout)getLayout();
         layout.setVerticalGroup(
                 layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                     .add(27, 27, 27)
                     .add(versionNameJLabel)
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(versionNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(15, 15, 15)
                     .add(teamMembersJLabel)
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(teamMembersJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, teamMembersVerticalSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(10, 10, 10)
                     .add(publishToUserJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(publishToUserJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(27, 27, 27)
                     .add(buttonBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(progressBarJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             );
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
                 validateInput();
             }
         }
     }
 
     /**
      * Publish the container.
      */
     private void publishContainer() {
         final PublishType publishType = getInputPublishType();
         final Long containerId = getInputContainerId();
         final PublishContainerAvatarUserListModel teamMembersModel = 
             (PublishContainerAvatarUserListModel) teamMembersJList.getModel();
 
         final List<Contact> contacts = getPublishToUserControl().extractContacts();
         final List<TeamMember> teamMembers = combineTeamMembers(getPublishToUserControl().extractTeamMembers(),
                 teamMembersModel.getSelectedTeamMembers());
         final List<EMail> emails = combineEMails(getPublishToUserControl().extractEMails(),
                 teamMembersModel.getSelectedEMailUsers());
         final String versionName = extractVersionName();
 
         // Publish
         switch (publishType) {
         case PUBLISH:
             showBusyIndicators(Boolean.TRUE);
             getController().runPublishContainer(createMonitor(), containerId,
                     versionName, emails, contacts, teamMembers);
             break;
         case PUBLISH_VERSION:
             disposeWindow();
             getController().runPublishContainerVersion(containerId,
                     getInputVersionId(), emails, contacts, teamMembers);
             break;
         default:
             Assert.assertUnreachable("Unknown publish type.");
         }
     }
 
     private void publishJButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishJButtonActionPerformed
         if (isInputValid()) {
             // check the input is also valid on the publish to user control
             if (!getPublishToUserControl().isInputValid()) {
                 addInputError(getPublishToUserControl().getInputError());
                 errorMessageJLabel.setText(getInputErrors().get(0));
             } else {
                 publishJButton.setEnabled(false);
                 publishContainer();
             }
         }
     }//GEN-LAST:event_publishJButtonActionPerformed
 
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
      * Read user emails that got this version.
      */
     private List<PublishedToEMail> readLatestVersionPublishedToEMails() {
         return ((PublishContainerProvider) contentProvider).readLatestVersionPublishedToEMails(
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
      * Read the version name.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return The version name <code>String</code>.
      */
     private String readVersionName(final Long containerId, final Long versionId) {
         return ((PublishContainerProvider) contentProvider).readVersionName(containerId, versionId);
     }
 
     /**
      * Read the version publish date.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return The version publish date <code>Calendar</code>.
      */
     private Calendar readVersionPublishDate(final Long containerId, final Long versionId) {
         return ((PublishContainerProvider) contentProvider).readVersionPublishDate(containerId, versionId);
     }
 
     private void reloadProgressBar() {
         buttonBarJPanel.setVisible(true);
         setCloseButtonEnabled(Boolean.TRUE);
         progressBarJPanel.setVisible(false);
         /* NOTE the space is deliberate (as opposed to an empty string) in
          * order to maintain vertical spacing. */
         statusJLabel.setText(" ");
         validate();
     }
 
     /**
      * Reload the list of team members.
      */
     private void reloadPublishToTeamMembersList() {
         final PublishTypeSpecific publishType = getPublishTypeSpecific();
         teamMembersListModel.clear();
 
         // read team members
         final List<TeamMember> teamMembers = readTeamMembers();
 
         // populate team members
         if (PublishTypeSpecific.PUBLISH_NOT_FIRST_TIME == publishType) {
             final User updatedBy = readLatestVersionUpdatedBy();
             final List<ArtifactReceipt> publishedTo = readLatestVersionPublishedTo();
             final List<PublishedToEMail> publishedToEMails = readLatestVersionPublishedToEMails();
             // add selected team members first, then published to emails, then non-selected team members
             for (final TeamMember teamMember : teamMembers) {
                 if (isVersionRecipient(teamMember, updatedBy, publishedTo)) {
                     teamMembersListModel.addElement(new PublishContainerAvatarUser(
                             teamMember, Boolean.TRUE));
                 }
             }
             for (final PublishedToEMail emailUser : publishedToEMails) {
                 teamMembersListModel.addElement(new PublishContainerAvatarUser(
                         emailUser, Boolean.TRUE));
             }
             for (final TeamMember teamMember : teamMembers) {
                 if (!isVersionRecipient(teamMember, updatedBy, publishedTo)) {
                     teamMembersListModel.addElement(new PublishContainerAvatarUser(
                             teamMember, Boolean.FALSE));
                 }
             }
         } else {
             for (final TeamMember teamMember : teamMembers) {
                 teamMembersListModel.addElement(new PublishContainerAvatarUser(
                         teamMember, Boolean.FALSE));
             }
         }
 
         // adjust the size of the team member control
        initComponentsAdjustVerticalSize(teamMembersListModel.size());
 
         // if there are no team members then hide the team member list and adjust labels
         final boolean teamList = (PublishTypeSpecific.PUBLISH_FIRST_TIME != publishType &&
                 teamMembersListModel.size() > 0);
         teamMembersJLabel.setVisible(teamList);
         teamMembersJScrollPane.setVisible(teamList);
         reloadPublishToUserLabel(teamList);
     }
 
     /**
      * Relaod the publish to user control.
      */
     private void reloadPublishToUserControl() {
         publishToUserJTextArea.setText(null);
         getPublishToUserControl().setContainerId(getInputContainerId());
         getPublishToUserControl().setContentProvider((PublishContainerProvider) contentProvider);
         getPublishToUserControl().reload();
     }
 
     /**
      * Reload the publish to user label.
      * 
      * @param teamList
      *            A <code>Boolean</code>, true if the team list is displayed.
      */
     private void reloadPublishToUserLabel(final Boolean teamList) {
         final java.awt.GridBagLayout layout = (java.awt.GridBagLayout)publishToUserJPanel.getLayout();
         final GridBagConstraints constraints = (GridBagConstraints)layout.getConstraints(publishToUserJLabel).clone();
         constraints.insets.left = teamList ? 16 : 0;
         layout.setConstraints(publishToUserJLabel, constraints);
         if (teamList) {
             publishToUserJLabel.setText(getString("PublishToUser"));
         } else {
             publishToUserJLabel.setText(getString("PublishToUserNoTeamMembers"));
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
             String name = readVersionName(containerId, versionId);
             if (null == name) {
                 name = FUZZY_DATE_FORMAT.format(readVersionPublishDate(containerId, versionId));
             }
             versionNameJTextField.setText(name);
             setEditable(versionNameJTextField, false);
             break;
         case PUBLISH:
             defaultVersionName = FUZZY_DATE_FORMAT.format(Calendar.getInstance());
             versionNameJTextField.setText(defaultVersionName);
             versionNameJTextField.selectAll();
             setEditable(versionNameJTextField, true);
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
         if (busy) {
             final java.awt.Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
             SwingUtil.setCursor(this, cursor);
             SwingUtil.setCursor(publishToUserJTextArea, cursor);
             SwingUtil.setCursor(versionNameJTextField, cursor);
         } else {
             final java.awt.Cursor cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
             SwingUtil.setCursor(publishToUserJTextArea, cursor);
             SwingUtil.setCursor(versionNameJTextField,
                     PublishType.PUBLISH == getInputPublishType() ? cursor : null);
             SwingUtil.setCursor(this, null);
         }
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
 
     private void versionNameJTextFieldActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionNameJTextFieldActionPerformed
         publishJButtonActionPerformed(evt);
     }//GEN-LAST:event_versionNameJTextFieldActionPerformed
 
     public enum DataKey { CONTAINER_ID, PUBLISH_TYPE, VERSION_ID }
 
     public class PublishContainerAvatarUser {
 
         /** The email user. */
         private PublishedToEMail emailUser;
 
         /** The selection status. */
         private Boolean selected;
 
         /** The user. */
         private User user;
 
         public PublishContainerAvatarUser(final PublishedToEMail emailUser, final Boolean selected) {
             this.emailUser = emailUser;
             this.selected = selected;
         }
 
         public PublishContainerAvatarUser(final User user, final Boolean selected) {
             this.user = user;
             this.selected = selected;
         }
 
         public PublishedToEMail getEMailUser() {
             return emailUser;
         }
 
         public String getExtendedName() {
             if (null != user && null == user.getTitle()) {
                 logger.logWarning("{0}{0}{0}TITLE IS NULL{0}{1}{0}{2}{0}{3}{0}{4}{0}{5}{0}{6}{0}{7}{0}{8}{0}{0}{0}",
                         "\r\n", user.getFlags(), user.getId(), user.getLocalId(),
                         user.getName(), user.getOrganization(),
                         user.getSimpleUsername(), user.getUsername(),
                         user.getClass());
             }
             if (isUser()) {
                 return localization.getString("UserName",
                         new Object[] {user.getName(), user.getTitle(), user.getOrganization()} );
             } else if (isEMailUser()) {
                 return emailUser.getEMail().toString();
             } else {
                 return null;
             }
         }
 
         public User getUser() {
             return user;
         }
 
         public Boolean isEMailUser() {
             return null != getEMailUser();
         }
 
         public Boolean isSelected() {
             if (null == user && null == emailUser) {
                 return Boolean.FALSE;
             } else {
                 return selected;
             }
         }
 
         public Boolean isUser() {
             return null != getUser();
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
 
         private List<PublishedToEMail> getSelectedEMailUsers() {
             List<PublishedToEMail> selectedEMailUsers = new ArrayList<PublishedToEMail>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && user.isEMailUser()) {
                     selectedEMailUsers.add(user.getEMailUser());
                 }
             }
             return selectedEMailUsers;
         }
 
         private List<TeamMember> getSelectedTeamMembers() {
             List<TeamMember> selectedTeamMembers = new ArrayList<TeamMember>();
             for (int index = 0; index < getSize(); index++) {
                 PublishContainerAvatarUser user = (PublishContainerAvatarUser)getElementAt(index);
                 if (user.isSelected() && user.isUser() && (user.getUser() instanceof TeamMember)) {
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
