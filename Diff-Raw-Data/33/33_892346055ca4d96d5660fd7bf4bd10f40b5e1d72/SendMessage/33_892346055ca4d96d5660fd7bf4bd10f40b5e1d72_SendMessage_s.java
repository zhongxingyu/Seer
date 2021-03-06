 /**
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2006 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Lesser Public License (LGPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.sparkimpl.plugin.filetransfer.transfer.ui;
 
 import org.jdesktop.jdic.desktop.Desktop;
 import org.jdesktop.jdic.desktop.DesktopException;
 import org.jivesoftware.Spark;
 import org.jivesoftware.resource.Res;
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.filetransfer.FileTransfer;
 import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
 import org.jivesoftware.smackx.filetransfer.FileTransferManager;
 import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.ui.ContactItem;
 import org.jivesoftware.spark.ui.ContactList;
 import org.jivesoftware.spark.util.ByteFormat;
 import org.jivesoftware.spark.util.GraphicUtils;
 import org.jivesoftware.spark.util.SwingWorker;
 import org.jivesoftware.spark.util.log.Log;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 public class SendMessage extends JPanel {
     private JLabel imageLabel = new JLabel();
     private JLabel titleLabel = new JLabel();
     private JLabel fileLabel = new JLabel();
 
     private TransferButton cancelButton = new TransferButton();
     private JProgressBar progressBar = new JProgressBar();
     private File fileToSend;
     private OutgoingFileTransfer transfer;
 
 
     private TransferButton retryButton = new TransferButton();
 
     private FileTransferManager transferManager;
     private String fullJID;
     private String nickname;
 
     public SendMessage() {
         setLayout(new GridBagLayout());
 
         setBackground(new Color(250, 249, 242));
         add(imageLabel, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
         add(titleLabel, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         titleLabel.setFont(new Font("Dialog", Font.BOLD, 11));
         titleLabel.setForeground(new Color(211, 174, 102));
         add(fileLabel, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
 
         cancelButton.setText(Res.getString("cancel"));
         retryButton.setText(Res.getString("retry"));
 
         add(cancelButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
         add(retryButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
         retryButton.setVisible(false);
 
         retryButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     File file = new File(transfer.getFilePath());
                     transfer = transferManager.createOutgoingFileTransfer(fullJID);
                     transfer.sendFile(file, "Sending");
                 }
                 catch (XMPPException e1) {
                    e1.printStackTrace();
                 }
                 sendFile(transfer, transferManager, fullJID, nickname);
             }
         });
 
         cancelButton.setForeground(new Color(73, 113, 196));
        cancelButton.setFont(new Font("Dialog", Font.BOLD, 10));
         cancelButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(73, 113, 196)));
 
         retryButton.setForeground(new Color(73, 113, 196));
        retryButton.setFont(new Font("Dialog", Font.BOLD, 10));
         retryButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(73, 113, 196)));
 
 
         setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));
     }
 
     public void sendFile(final OutgoingFileTransfer transfer, FileTransferManager transferManager, final String jid, final String nickname) {
         this.transferManager = transferManager;
 
         cancelButton.setVisible(true);
         retryButton.setVisible(false);
         this.fullJID = jid;
         this.nickname = nickname;
 
         this.transfer = transfer;
         String fileName = transfer.getFileName();
         long fileSize = transfer.getFileSize();
         ByteFormat format = new ByteFormat();
         String text = format.format(fileSize);
 
         fileToSend = new File(transfer.getFilePath());
 
         fileLabel.setText(fileName + " (" + text + ")");
 
         ContactList contactList = SparkManager.getWorkspace().getContactList();
         ContactItem contactItem = contactList.getContactItemByJID(jid);
 
         titleLabel.setText(Res.getString("message.transfer.waiting.on.user", contactItem.getNickname()));
 
         if (isImage(fileName)) {
             try {
                 URL imageURL = new File(transfer.getFilePath()).toURL();
                 ImageIcon image = new ImageIcon(imageURL);
                 image = GraphicUtils.scaleImageIcon(image, 64, 64);
                 imageLabel.setIcon(image);
             }
             catch (MalformedURLException e) {
                 Log.error("Could not locate image.", e);
                 imageLabel.setIcon(SparkRes.getImageIcon(SparkRes.DOCUMENT_INFO_32x32));
             }
         }
         else {
             File file = new File(transfer.getFilePath());
             Icon icon = GraphicUtils.getIcon(file);
             imageLabel.setIcon(icon);
         }
         cancelButton.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent mouseEvent) {
                 transfer.cancel();
             }
         });
 
         cancelButton.addMouseListener(new MouseAdapter() {
             public void mouseEntered(MouseEvent e) {
                 cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
 
             }
 
             public void mouseExited(MouseEvent e) {
                 cancelButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         });
 
 
         progressBar.setMaximum((int)fileSize);
         progressBar.setVisible(false);
         progressBar.setStringPainted(true);
         add(progressBar, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 150, 0));
 
 
         SwingWorker worker = new SwingWorker() {
             public Object construct() {
                 while (true) {
                     try {
                         Thread.sleep(10);
                         FileTransfer.Status status = transfer.getStatus();
                         if (status == Status.error ||
                             status == Status.complete || status == Status.cancelled ||
                             status == Status.refused) {
                             break;
                         }
                         updateBar(transfer, nickname);
                     }
                     catch (InterruptedException e) {
                         Log.error("Unable to sleep thread.", e);
                     }
 
                 }
                 return "";
             }
 
             public void finished() {
                 updateBar(transfer, nickname);
             }
         };
 
         worker.start();
 
         makeClickable(imageLabel);
         makeClickable(titleLabel);
     }
 
     private void makeClickable(final JLabel label) {
         label.setToolTipText(Res.getString("message.click.to.open"));
 
         label.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 openFile(fileToSend);
             }
 
             public void mouseEntered(MouseEvent e) {
                 label.setCursor(new Cursor(Cursor.HAND_CURSOR));
 
             }
 
             public void mouseExited(MouseEvent e) {
                 label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         });
     }
 
     private void openFile(File downloadedFile) {
         try {
             if (!Spark.isMac()) {
                 try {
                     Desktop.open(downloadedFile);
                 }
                 catch (DesktopException e) {
                     Log.error(e);
                 }
             }
             else if (Spark.isMac()) {
                 Process child = Runtime.getRuntime().exec("open " + downloadedFile.getCanonicalPath());
             }
         }
         catch (IOException e1) {
             Log.error("Exception occured while opening file.", e1);
         }
     }
 
     private void updateBar(final OutgoingFileTransfer transfer, String nickname) {
         FileTransfer.Status status = transfer.getStatus();
         if (status == Status.negotiating_stream) {
             titleLabel.setText(Res.getString("message.negotiation.file.transfer", nickname));
         }
         else if (status == Status.error) {
             if (transfer.getException() != null) {
                 Log.error("Error occured during file transfer.", transfer.getException());
             }
             progressBar.setVisible(false);
             titleLabel.setText(Res.getString("message.unable.to.send.file", nickname));
             cancelButton.setVisible(false);
             retryButton.setVisible(true);
             showAlert(true);
         }
         else if (status == Status.in_progress) {
             titleLabel.setText(Res.getString("message.sending.file.to", nickname));
             showAlert(false);
             if (!progressBar.isVisible()) {
                 progressBar.setVisible(true);
             }
             progressBar.setValue((int)transfer.getBytesSent());
 
             ByteFormat format = new ByteFormat();
             String bytesSent = format.format(transfer.getBytesSent());
             progressBar.setString(bytesSent + " sent");
         }
         else if (status == Status.complete) {
             progressBar.setVisible(false);
             titleLabel.setText(Res.getString("message.you.have.sent", nickname));
             cancelButton.setVisible(false);
             showAlert(true);
         }
         else if (status == Status.cancelled) {
             progressBar.setVisible(false);
             titleLabel.setText(Res.getString("message.file.transfer.canceled"));
             cancelButton.setVisible(false);
             retryButton.setVisible(true);
             showAlert(true);
         }
         else if (status == Status.refused) {
             progressBar.setVisible(false);
             titleLabel.setText(Res.getString("message.file.transfer.rejected", nickname));
             cancelButton.setVisible(false);
             retryButton.setVisible(true);
             showAlert(true);
         }
 
     }
 
     private class TransferButton extends JButton {
 
         public TransferButton() {
             decorate();
         }
 
         /**
          * Create a new RolloverButton.
          *
          * @param text the button text.
          * @param icon the button icon.
          */
         public TransferButton(String text, Icon icon) {
             super(text, icon);
             decorate();
         }
 
 
         /**
          * Decorates the button with the approriate UI configurations.
          */
         private void decorate() {
             setBorderPainted(false);
             setOpaque(true);
 
             setContentAreaFilled(false);
             setMargin(new Insets(1, 1, 1, 1));
         }
 
     }
 
 
     private boolean isImage(String fileName) {
         fileName = fileName.toLowerCase();
 
         String[] imageTypes = {"jpeg", "gif", "jpg", "png"};
         for (int i = 0; i < imageTypes.length; i++) {
             if (fileName.endsWith(imageTypes[i])) {
                 return true;
             }
         }
 
         return false;
     }
 
     private void showAlert(boolean alert) {
         if (alert) {
             titleLabel.setForeground(new Color(211, 174, 102));
             setBackground(new Color(250, 249, 242));
         }
         else {
             setBackground(new Color(239, 245, 250));
             titleLabel.setForeground(new Color(65, 139, 179));
         }
     }
 
     public void cancelTransfer() {
         if (transfer != null) {
             transfer.cancel();
         }
     }
 
 }
