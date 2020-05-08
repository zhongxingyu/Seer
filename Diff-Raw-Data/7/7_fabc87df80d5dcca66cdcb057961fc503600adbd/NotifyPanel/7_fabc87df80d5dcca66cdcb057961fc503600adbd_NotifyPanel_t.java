 /*
  * NotifyPanel.java
  *
  * Created on October 9, 2007, 4:31 PM
  */
 
 package com.thinkparity.ophelia.browser.application.system.dialog;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.Icon;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.ophelia.browser.util.ImageIOUtil;
 
 /**
  * The NotifyPanel manages the list of notifications.
  * Associated with each notification type is a NotifyPage. The card
  * layout is used to display the appropriate page when a new notification
  * is displayed.
  *
  * @author raymond@thinkparity.com, robert@thinkparity.com
  * @version $Revision$
  */
 public class NotifyPanel extends SystemPanel {
 
     /** Close label icon. */
     private static final Icon CLOSE_ICON;
 
     /** Close label rollover icon. */
     private static final Icon CLOSE_ROLLOVER_ICON;
 
     /** A singleton list of notifications. */
     private static final List<Notification> NOTIFICATIONS;
 
     static {
         CLOSE_ICON = ImageIOUtil.readIcon("Dialog_CloseButton.png");
         CLOSE_ROLLOVER_ICON = ImageIOUtil.readIcon("Dialog_CloseButtonRollover.png");
         NOTIFICATIONS = new Vector<Notification>();
     }
 
     /** A <code>CardLayout</code>. */
     private final java.awt.CardLayout cardLayout;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JPanel contentJPanel = new javax.swing.JPanel();
     // End of variables declaration//GEN-END:variables
 
     /** The current <code>NotifyPage</code>. */
     private NotifyPage currentPage;
 
     /** The current notification index. */
     private int notificationIndex;
 
     /** The list of <code>NotifyPage</code>s. */
     private final List<NotifyPage> notifyPages;
 
     /** Creates new form NotifyPanel */
     public NotifyPanel() {
         super();
         initComponents();
         cardLayout = new java.awt.CardLayout();
         contentJPanel.setLayout(cardLayout);
         notifyPages = new ArrayList<NotifyPage>();
         notificationIndex = 0;
     }
 
     /**
      * Close notifications.
      * 
      * @param notificationId
      *            A notification id <code>String</code>.
      */
     void close(final String notificationId) {
         int index;
         while (-1 < (index = indexOf(notificationId))) {
             logger.logInfo("Closing notification {0}.", notificationId);
             closeNotificationPanel(index);
         }
     }
 
     /**
      * Display a notification.
      * If a notification is already displayed, the new
      * notification is not displayed until the old is processed,
      * unless it is higher priority.
      * 
      * @param notification
      *            A <code>Notification</code>.
      */
     void display(final Notification notification) {
         if (!isDuplicate(notification)) {
             NOTIFICATIONS.add(notification);
             displayNotificationPanel();
             reload();
         }
     }
 
     /**
      * Register a page for the card layout.
      * 
      * @param notifyPage
      *            A <code>NotifyPage</code>.
      */
     void registerPage(final NotifyPage notifyPage) {
         notifyPages.add(notifyPage);
         contentJPanel.add((Component)notifyPage, notifyPage.getPageName());
     }
 
     /**
      * Reload the connection status.
      */
     void reloadConnection() {
         reload();
     }
 
     private void closeJButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
         ((javax.swing.JButton) evt.getSource()).setIcon(CLOSE_ICON);
         // Close one notification, not all. This exposes the next notification, if any.
         closeNotificationPanel(notificationIndex);
     }//GEN-LAST:event_closeJButtonActionPerformed
 
     private void closeJButtonMouseEntered(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeJButtonMouseEntered
         ((javax.swing.JButton) evt.getSource()).setIcon(CLOSE_ROLLOVER_ICON);
     }//GEN-LAST:event_closeJButtonMouseEntered
 
     private void closeJButtonMouseExited(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeJButtonMouseExited
         ((javax.swing.JButton) evt.getSource()).setIcon(CLOSE_ICON);
     }//GEN-LAST:event_closeJButtonMouseExited
 
     /**
      * Close one entry of the notification panel.
      * 
      * @param index
      *          The index to close.
      */
     private void closeNotificationPanel(final int index) {
         NOTIFICATIONS.remove(index);
         if (NOTIFICATIONS.size() == 0) {
             disposeWindow();
         } else {
             displayNotificationPanel();
             reload();
         }
     }
 
     /**
      * Display the appropriate notification.
      * 
      * The highest priority notification is displayed.
      * If there are two or more notifications of the same priority,
      * the first is displayed.
      */
     private void displayNotificationPanel() {
         notificationIndex = 0;
         Integer priority = NOTIFICATIONS.get(notificationIndex).getPriority().toInteger();
         for (int i = 1; i < NOTIFICATIONS.size(); i++) {
             final Integer thisPriority = NOTIFICATIONS.get(i).getPriority().toInteger();
             if (priority < thisPriority) {
                 notificationIndex = i;
                 priority = thisPriority;
             }
         }
         setPage(getPage(NOTIFICATIONS.get(notificationIndex)));
     }
 
     /**
      * Get the notify page appropriate for the notification.
      * 
      * @return A <code>NotifyPage</code>.
      */
     private NotifyPage getPage(final Notification notification) {
         return lookupPage(notification.getType().toString());
     }
 
     /**
      * Obtain the index of the notification within the list.
      * 
      * @param notificationId
      *            A notification id <code>String</code>.
      * @return An <code>int</code> index or -1 if the notification does not exist.
      */
     private int indexOf(final String notificationId) {
         for (int i = 0; i < NOTIFICATIONS.size(); i++) {
             if (isMatchingId(NOTIFICATIONS.get(i), notificationId)) {
                 return i;
             }
         }
         return -1;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         final javax.swing.JButton invisibleJButton = new javax.swing.JButton();
         final javax.swing.JButton closeJButton = new javax.swing.JButton();
         final javax.swing.JLabel logoJLabel = new javax.swing.JLabel();
 
         invisibleJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Invisible14x14.png")));
         invisibleJButton.setBorderPainted(false);
         invisibleJButton.setContentAreaFilled(false);
         invisibleJButton.setFocusPainted(false);
         invisibleJButton.setFocusable(false);
         invisibleJButton.setMaximumSize(new java.awt.Dimension(14, 14));
         invisibleJButton.setMinimumSize(new java.awt.Dimension(14, 14));
         invisibleJButton.setPreferredSize(new java.awt.Dimension(14, 14));
 
         closeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Dialog_CloseButton.png")));
         closeJButton.setBorderPainted(false);
         closeJButton.setContentAreaFilled(false);
         closeJButton.setFocusPainted(false);
         closeJButton.setFocusable(false);
         closeJButton.setMaximumSize(new java.awt.Dimension(14, 14));
         closeJButton.setMinimumSize(new java.awt.Dimension(14, 14));
         closeJButton.setPreferredSize(new java.awt.Dimension(14, 14));
         closeJButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 closeJButtonMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 closeJButtonMouseExited(evt);
             }
         });
         closeJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeJButtonActionPerformed(evt);
             }
         });
 
         logoJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         logoJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/thinkParityLogo.png")));
 
         contentJPanel.setOpaque(false);
         javax.swing.GroupLayout contentJPanelLayout = new javax.swing.GroupLayout(contentJPanel);
         contentJPanel.setLayout(contentJPanelLayout);
         contentJPanelLayout.setHorizontalGroup(
             contentJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 277, Short.MAX_VALUE)
         );
         contentJPanelLayout.setVerticalGroup(
             contentJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 104, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(invisibleJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(logoJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(closeJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addComponent(contentJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(invisibleJButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(closeJButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(logoJLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(contentJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Determine if the specified page is the current page.
      * 
      * @param notifyPage
      *            A <code>NotifyPage</code>.
      * @return true if the specified page is the current page.
      */
     private Boolean isCurrentPage(final NotifyPage notifyPage) {
         return (null != currentPage && currentPage.equals(notifyPage));
     }
 
     /**
      * Determine if the notification is a duplicate of one in the list.
      * 
      * @param notification
      *            A <code>Notification</code>.
      * @return True if the notification is a duplicate of one in the list.    
      */
     private Boolean isDuplicate(final Notification notification) {
         for (int i = 0; i < NOTIFICATIONS.size(); i++) {
             if (NOTIFICATIONS.get(i).getId().equals(notification.getId())) {
                 return Boolean.TRUE;
             }
         }
         return Boolean.FALSE;
     }
 
     /**
      * Determine if the id matches by id or group id.
      * 
      * @param notification
      *            A <code>Notification</code>.
      * @param id
      *            An id <code>String</code>.
      * @return True if the id matches.
      */
     private Boolean isMatchingId(final Notification notification,
             final String id) {
         if (notification.getId().equals(id)) {
             return Boolean.TRUE;
         } else if (null != notification.getGroupId()) {
             return notification.getGroupId().equals(id);
         } else {
             return Boolean.FALSE;
         }
     }
 
     /**
      * Find the NotifyPage given the page name.
      * 
      * @param pageName
      *            The page name <code>String</code>.
      * @return A <code>NotifyPage</code>.
      */
     private NotifyPage lookupPage(final String pageName) {
         Assert.assertNotNull("Null page name in notification dialog.", pageName);
         NotifyPage foundPage = null;
         for (final NotifyPage notifyPage : notifyPages) {
             if (notifyPage.getPageName().equals(pageName)) {
                 foundPage = notifyPage;
             }
         }
         Assert.assertNotNull("Invalid page in notification dialog.", foundPage);
         return foundPage;
     }
 
     /**
      * Reload the notification panel.
      */
     private void reload() {
         if (NOTIFICATIONS.size() > 0) {
             SwingUtil.ensureDispatchThread(new Runnable() {
                 public void run() {
                     currentPage.reload(NOTIFICATIONS.get(notificationIndex));
                 }
             });
         }
     }
 
     /**
      * Set the card layout page.
      * 
      * @param page
      *            The <code>NotifyPage</code>.
      */
     private void setCardLayoutPage(final NotifyPage page) {
         SwingUtil.ensureDispatchThread(new Runnable() {
             public void run() {
                 cardLayout.show(contentJPanel, page.getPageName());
             }
         });
     }
 
     /**
      * Set the page.
      * 
      * @param page
      *            The <code>NotifyPage</code>.
      */
     private void setPage(final NotifyPage page) {
         Assert.assertNotNull("Null page in notification dialog.", page);
         if (!isCurrentPage(page)) {
             currentPage = page;
             setCardLayoutPage(page);
             reload();
             repaint();
         }
     }
 }
