 /*
  * UpdateAccountAvatar.java
  *
  * Created on July 25, 2007, 4:36 PM
  */
 
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.profile;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Calendar;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.text.AbstractDocument;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.swing.text.JTextFieldLengthFilter;
 
 import com.thinkparity.codebase.model.profile.payment.PaymentInfo;
 import com.thinkparity.codebase.model.profile.payment.PaymentInfoConstraints;
 
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Colours;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.ButtonFactory;
 import com.thinkparity.ophelia.browser.application.browser.component.LabelFactory;
 import com.thinkparity.ophelia.browser.application.browser.component.TextFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.dialog.profile.UpdateAccountProvider;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.util.State;
 import com.thinkparity.ophelia.browser.util.swing.CardNameCellRenderer;
 
 /**
  * <b>Title:</b>thinkParity OpheliaUI Update Account Avatar<br>
  * <b>Description:</b><br>
  * 
  * @author robert@thinkparity.com
  * @version 1.1.2.1
  */
 public final class UpdatePaymentInfoAvatar extends Avatar {
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JButton cancelJButton = ButtonFactory.create();
     private final javax.swing.JComboBox cardExpiryMonthJComboBox = new javax.swing.JComboBox();
     private final javax.swing.JComboBox cardExpiryYearJComboBox = new javax.swing.JComboBox();
     private final javax.swing.JComboBox cardNameJComboBox = new javax.swing.JComboBox();
     private final javax.swing.JTextField cardNumberJTextField = TextFactory.create(Fonts.DialogTextEntryFont);
     private final javax.swing.JTextField cardholderNameJTextField = TextFactory.create(Fonts.DialogTextEntryFont);
     private final javax.swing.JLabel errorMessageJLabel = LabelFactory.create(Fonts.DialogFont, Colours.DIALOG_ERROR_TEXT_FG);
     private final javax.swing.JButton okJButton = ButtonFactory.create();
     // End of variables declaration//GEN-END:variables
 
     /** The country <code>DefaultComboBoxModel</code>. */
     private final DefaultComboBoxModel cardExpiryMonthModel;
 
     /** The country <code>DefaultComboBoxModel</code>. */
     private final DefaultComboBoxModel cardExpiryYearModel;
 
     /** The country <code>DefaultComboBoxModel</code>. */
     private final DefaultComboBoxModel cardNameModel;
 
     /** An instance of payment info constraints. */
     private final PaymentInfoConstraints constraints;
 
     /**
      * Create UpdateAccountAvatar.
      *
      */
     public UpdatePaymentInfoAvatar() {
         super("UpdateAccountAvatar", BrowserConstants.DIALOGUE_BACKGROUND);
         this.constraints = PaymentInfoConstraints.getInstance();
         this.cardExpiryMonthModel = new DefaultComboBoxModel();
         this.cardNameModel = new DefaultComboBoxModel();
         this.cardExpiryYearModel = new DefaultComboBoxModel();
         initExpiryMonthModel();
         initExpiryYearModel();
         initTypeModel();
         initComponents();
         addValidationListeners();
         addFocusListeners();
         bindKeys();
         cardNameJComboBox.setRenderer(new CardNameCellRenderer("CardName", getLocalization()));
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
      *
      */
     @Override
     public AvatarId getId() {
         return AvatarId.DIALOG_PROFILE_UPDATE_PAYMENT_INFO;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getState()
      *
      */
     @Override
     public State getState() {
         return null;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#reload()
      * 
      */
     public void reload() {
         reloadCreditCardControls();
         selectExpiryMonth();
         selectExpiryYear();
         validateInput();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setState(com.thinkparity.ophelia.browser.platform.util.State)
      *
      */
     @Override
     public void setState(final State state) {
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#validateInput()
      *
      */
     @Override
     public final void validateInput() {
         validateInput(Boolean.FALSE);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#isInputValid()
      */
     @Override
     protected Boolean isInputValid() {
         validateInput(Boolean.TRUE);
         return !containsInputErrors();
     }
 
     /**
      * Add focus listeners for the input controls.
      */
     private void addFocusListeners() {
         final FocusListener focusListener = new FocusListener() {
             public void focusGained(FocusEvent e) {
                 validateInput();
             }
             public void focusLost(FocusEvent e) {
                 validateInput();
             }
         };
         cardNumberJTextField.addFocusListener(focusListener);
         cardExpiryMonthJComboBox.addFocusListener(focusListener);
         cardExpiryYearJComboBox.addFocusListener(focusListener);
     }
 
     /**
      * Add validation listeners.
      */
     private void addValidationListeners() {
         addValidationListener(cardholderNameJTextField);
         addValidationListener(cardNumberJTextField);
         final ItemListener itemListener = new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 validateInput();
             }
         };
         cardNameJComboBox.addItemListener(itemListener);
         cardExpiryMonthJComboBox.addItemListener(itemListener);
         cardExpiryYearJComboBox.addItemListener(itemListener);
     }
 
     /**
      * Bind the escape and enter keys.
      */
     private void bindKeys() {
         bindEscapeKey("Cancel", new AbstractAction() {
             public void actionPerformed(final ActionEvent e) {
                 cancelJButtonActionPerformed(e);
             }
         });
         bindEnterKey("Enter", new AbstractAction() {
             public void actionPerformed(final ActionEvent e) {
                 if (cancelJButton.isFocusOwner()) {
                     cancelJButtonActionPerformed(e);
                 } else {
                     okJButtonActionPerformed(e);
                 }
             }
         });
     }
 
     private void cancelJButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
         disposeWindow();
     }//GEN-LAST:event_cancelJButtonActionPerformed
 
     /**
      * Extract the payment info.
      * 
      * @return A <code>PaymentInfo</code>.
      */
     private PaymentInfo extractPaymentInfo() {
         return utils.extractPaymentInfo(cardExpiryMonthJComboBox,
                 cardExpiryYearJComboBox, cardNumberJTextField, cardNameJComboBox);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         final javax.swing.JLabel creditInfoTitleJLabel = LabelFactory.create(Fonts.DialogFont);
         final javax.swing.JLabel cardNameJLabel = LabelFactory.create(Fonts.DialogFont);
         final javax.swing.JLabel cardNumberJLabel = LabelFactory.create(Fonts.DialogFont);
         final javax.swing.JLabel cardholderNameJLabel = LabelFactory.create(Fonts.DialogFont);
         final javax.swing.JLabel cardExpiryDateJLabel = LabelFactory.create(Fonts.DialogFont);
         final javax.swing.JLabel licenseAgreementJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
 
         setOpaque(false);
         creditInfoTitleJLabel.setFont(Fonts.DialogFont);
         creditInfoTitleJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.CreditInfoTitle"));
 
         cardNameJLabel.setFont(Fonts.DialogFont);
         cardNameJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.CardType"));
 
         cardNameJComboBox.setFont(Fonts.DialogTextEntryFont);
         cardNameJComboBox.setModel(cardNameModel);
 
         cardNumberJLabel.setFont(Fonts.DialogFont);
         cardNumberJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.CardNumber"));
 
         cardNumberJTextField.setFont(Fonts.DialogTextEntryFont);
         ((AbstractDocument) cardNumberJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(constraints.getCardNumber()));
 
         cardholderNameJLabel.setFont(Fonts.DialogFont);
        cardholderNameJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpgradeAccountAvatar.Payment.Cardholder"));
 
         cardholderNameJTextField.setFont(Fonts.DialogTextEntryFont);
         ((AbstractDocument) cardholderNameJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(constraints.getCardholderName()));
 
         cardExpiryDateJLabel.setFont(Fonts.DialogFont);
         cardExpiryDateJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.CardDate"));
 
         cardExpiryMonthJComboBox.setFont(Fonts.DialogTextEntryFont);
         cardExpiryMonthJComboBox.setModel(cardExpiryMonthModel);
 
         cardExpiryYearJComboBox.setFont(Fonts.DialogTextEntryFont);
         cardExpiryYearJComboBox.setModel(cardExpiryYearModel);
 
         errorMessageJLabel.setFont(Fonts.DialogFont);
         errorMessageJLabel.setForeground(Colours.DIALOG_ERROR_TEXT_FG);
         errorMessageJLabel.setText("!Error Message!");
 
         licenseAgreementJLabel.setFont(Fonts.DialogFont);
         licenseAgreementJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.LicenseAgreement"));
         licenseAgreementJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 licenseAgreementJLabelMousePressed(evt);
             }
         });
 
         okJButton.setFont(Fonts.DialogButtonFont);
         okJButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.OK"));
         okJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 okJButtonActionPerformed(evt);
             }
         });
 
         cancelJButton.setFont(Fonts.DialogButtonFont);
         cancelJButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("UpdateAccountAvatar.Cancel"));
         cancelJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelJButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(creditInfoTitleJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(16, 16, 16)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(cardNumberJLabel)
                             .addComponent(cardNameJLabel)
                             .addComponent(cardholderNameJLabel)
                             .addComponent(cardExpiryDateJLabel))
                         .addGap(14, 14, 14)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(cardExpiryMonthJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(cardExpiryYearJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(cardNameJComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(cardNumberJTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(cardholderNameJTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE))
                     .addComponent(errorMessageJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(licenseAgreementJLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                         .addComponent(okJButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cancelJButton)))
                 .addContainerGap())
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelJButton, okJButton});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(27, 27, 27)
                 .addComponent(creditInfoTitleJLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cardNameJLabel)
                     .addComponent(cardNameJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cardNumberJLabel)
                     .addComponent(cardNumberJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cardholderNameJLabel)
                     .addComponent(cardholderNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cardExpiryDateJLabel)
                     .addComponent(cardExpiryYearJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(cardExpiryMonthJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(15, 15, 15)
                 .addComponent(errorMessageJLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(licenseAgreementJLabel)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(cancelJButton)
                         .addComponent(okJButton)))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Initialize the list of credit card months.
      * 
      */
     private void initExpiryMonthModel() {
         final short min = constraints.getCardExpiryMonth().getMinValue().shortValue();
         final short max = constraints.getCardExpiryMonth().getMaxValue().shortValue();
         for (short month = min; month <= max; month++) {
             cardExpiryMonthModel.addElement(Short.valueOf(month));
         }
     }
 
     /**
      * Initialize the list of credit card years.
      * 
      */
     private void initExpiryYearModel() {
         final short min = constraints.getCardExpiryYear().getMinValue().shortValue();
         final short max = constraints.getCardExpiryYear().getMaxValue().shortValue();
         for (short year = min; year <= max; year++) {
             cardExpiryYearModel.addElement(Short.valueOf(year));
         }
     }
 
     /**
      * Initialize the list of credit card types.
      * 
      */
     private void initTypeModel() {
         for (final PaymentInfo.CardName cardName : PaymentInfo.CardName.values()) {
             cardNameModel.addElement(cardName);
         }
     }
 
     /**
      * Determine if the model is online.
      * 
      * @return True if the model is online.
      */
     private boolean isOnline() {
         return ((UpdateAccountProvider) contentProvider).isOnline().booleanValue();
     }
 
     /**
      * Handle the licence agreement mouse pressed event.
      * The dialog is closed and the license agreement dialog is displayed.
      * NOTE Changes the user may have made are lost.
      * 
      * @param evt
      *            A <code>MouseEvent</code>.
      */
     private void licenseAgreementJLabelMousePressed(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_licenseAgreementJLabelMousePressed
         disposeWindow();
         getController().displayLicenseAgreementDialog();
     }//GEN-LAST:event_licenseAgreementJLabelMousePressed
 
     private void okJButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed
         if (isInputValid()) {
             disposeWindow();
             runUpdateAccountInfo();
         }
     }//GEN-LAST:event_okJButtonActionPerformed
 
     /**
      * Reload the credit card controls.
      * 
      */
     private void reloadCreditCardControls() {
         cardNameJComboBox.setSelectedIndex(0);
         cardNumberJTextField.setText(null);
         cardExpiryMonthJComboBox.setSelectedIndex(0);
         cardExpiryYearJComboBox.setSelectedIndex(0);
         if (null != input) {
             final PaymentInfo paymentInfo = (PaymentInfo) input;
             cardNameJComboBox.setSelectedItem(paymentInfo.getCardName());
             cardNumberJTextField.setText(paymentInfo.getCardNumber());
             cardExpiryMonthJComboBox.setSelectedItem(paymentInfo.getCardExpiryMonth());
             cardExpiryYearJComboBox.setSelectedItem(paymentInfo.getCardExpiryYear());
         }
     }
 
     /**
      * Invoke the update account info action.
      * 
      */
     private void runUpdateAccountInfo() {
         final PaymentInfo paymentInfo = extractPaymentInfo();
         getController().runUpdatePaymentInfo(paymentInfo);
     }
 
     /**
      * Select the current month.
      * 
      */
     private void selectExpiryMonth() {
         final Calendar now = DateUtil.getInstance();
         final short nowMonth = (short) (now.get(Calendar.MONTH) + 1);
         cardExpiryMonthModel.setSelectedItem(Short.valueOf(nowMonth));
     }
 
     /**
      * Select the current year.
      * 
      */
     private void selectExpiryYear() {
         final Calendar now = Calendar.getInstance();
         final short nowYear = (short) now.get(Calendar.YEAR);
         cardExpiryYearModel.setSelectedItem(Short.valueOf(nowYear));
     }
 
     /**
      * Validate input.
      * 
      * @param ignoreFocus
      *            A <code>Boolean</code> to ignore focus or not.
      */
     private void validateInput(final Boolean ignoreFocus) {
         super.validateInput();
         final PaymentInfo paymentInfo = extractPaymentInfo();
 
         // check online
         final Boolean online = isOnline();
         if (!online) {
             addInputError(getString("ErrorOffline"));
         }
 
         final int minimumCardNumberLength = constraints.getCardNumber().getMinLength();
         if (null == paymentInfo.getCardNumber()) {
             addInputError(Separator.Space.toString());
         } else if (paymentInfo.getCardNumber().length() < minimumCardNumberLength) {
             if (ignoreFocus || !cardNumberJTextField.isFocusOwner()) {
                 addInputError(getString("ErrorCardNumberInvalid"));
             }
         }
 
         final Calendar now = Calendar.getInstance();
         final short nowYear = (short) now.get(Calendar.YEAR);
         final short nowMonth = (short) (now.get(Calendar.MONTH) + 1);
         if (nowYear == paymentInfo.getCardExpiryYear() &&
                 nowMonth > paymentInfo.getCardExpiryMonth()) {
             if (ignoreFocus
                    || (!cardExpiryMonthJComboBox.isFocusOwner() && !cardExpiryYearJComboBox.isFocusOwner())) {
                 addInputError(getString("ErrorCardExpired"));
             }
         }
 
         errorMessageJLabel.setText(" ");
         if (containsInputErrors()) {
             errorMessageJLabel.setText(getInputErrors().get(0));
         }
         okJButton.setEnabled(!containsInputErrors());
     }
 
     /** <b>Title:</b>Update Payment Info Data Key<br> */
     public enum DataKey { PAYMENT_INFO }
 }
