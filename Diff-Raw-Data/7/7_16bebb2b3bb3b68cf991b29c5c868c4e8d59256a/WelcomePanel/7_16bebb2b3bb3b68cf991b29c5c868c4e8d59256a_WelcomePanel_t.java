 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View;
 
 import java.awt.CardLayout;
 import javax.swing.JPanel;
 import melt.Controller;
 
 /**
  *
  * @author Maria
  */
 public class WelcomePanel extends javax.swing.JPanel {
 
     private JPanel contentPane;
     private Controller controller;
    
     /**
      * Creates new form WelcomePanel
      */
     public WelcomePanel() {
         initComponents();
     }
 
     public WelcomePanel(JPanel panel, Controller controller) {
         contentPane = panel;
         setOpaque(true);
         this.controller = controller;
         initComponents();
     }
     
 
 
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         btnCreateTest = new javax.swing.JButton();
         btnTakeTest = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         btnMarkTest = new javax.swing.JButton();
 
         jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 36)); // NOI18N
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setText("Language Testing");
 
         btnCreateTest.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         btnCreateTest.setText("Setter");
         btnCreateTest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCreateTestActionPerformed(evt);
             }
         });
 
         btnTakeTest.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         btnTakeTest.setText("Student");
         btnTakeTest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTakeTestActionPerformed(evt);
             }
         });
 
         jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 36)); // NOI18N
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel2.setText("Manchester English");
 
         btnMarkTest.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         btnMarkTest.setText("Marker");
         btnMarkTest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMarkTestActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addGap(126, 126, 126)
                 .addComponent(btnCreateTest, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(76, 76, 76)
                 .addComponent(btnTakeTest, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(58, 58, 58)
                 .addComponent(btnMarkTest, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(70, 70, 70)
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel1)
                 .addGap(185, 185, 185)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnTakeTest, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnCreateTest, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnMarkTest, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(214, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Change the card to the staff panel.
      * @param evt 
      */
     private void btnCreateTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateTestActionPerformed
         // TODO add your handling code here:
         CardLayout cardLayout = (CardLayout) contentPane.getLayout();
         cardLayout.show(contentPane, "createTest");
     }//GEN-LAST:event_btnCreateTestActionPerformed
 
     /**
      * Change the card to the student panel.
      * @param evt 
      */
     private void btnTakeTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTakeTestActionPerformed
       CardLayout cardLayout5 = (CardLayout) contentPane.getLayout();
       cardLayout5.show(contentPane, "testList");
       
     }//GEN-LAST:event_btnTakeTestActionPerformed
 
     private void btnMarkTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkTestActionPerformed
         // TODO add your handling code here:
         CardLayout cardLayout = (CardLayout) contentPane.getLayout();
         cardLayout.show(contentPane, "markTest");
     }//GEN-LAST:event_btnMarkTestActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCreateTest;
     private javax.swing.JButton btnMarkTest;
     private javax.swing.JButton btnTakeTest;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     // End of variables declaration//GEN-END:variables
 }
