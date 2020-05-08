 package cz.muni.fi.pa165.rest;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import cz.muni.fi.pa165.library.dtos.CustomerTO;
 import cz.muni.fi.pa165.library.dtos.Department;
 import cz.muni.fi.pa165.library.dtos.ImpressionTO;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.Invocation;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 
 /**
  *
  * @author Mjartan
  */
 public class MainFrame extends javax.swing.JFrame {
 
    private static final String API_URL = "http://localhost:8084/library/rest";
     private WebTarget webTarget;
     private List<ImpressionTO> impressions;
     private List<CustomerTO> customers;
     private Gson gson;
 
     public MainFrame() {
         Client client = ClientBuilder.newClient();
         webTarget = client.target(API_URL);
         gson = new GsonBuilder().
                 registerTypeAdapter(LocalDate.class, new JodaTimeSerializer()).create();
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         lImpressions = new javax.swing.JList();
         jLabel1 = new javax.swing.JLabel();
         bLoadI = new javax.swing.JButton();
         bDeleteI = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         bInsertI = new javax.swing.JButton();
         tfIsbn = new javax.swing.JTextField();
         tfAuthor = new javax.swing.JTextField();
         tfName = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         tfDate = new javax.swing.JTextField();
         jScrollPane2 = new javax.swing.JScrollPane();
         lCustomers = new javax.swing.JList();
         jLabel6 = new javax.swing.JLabel();
         bLoadC = new javax.swing.JButton();
         bDeleteC = new javax.swing.JButton();
         jLabel8 = new javax.swing.JLabel();
         tfCName = new javax.swing.JTextField();
         jLabel11 = new javax.swing.JLabel();
         tfAddress = new javax.swing.JTextField();
         bInsertC = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         lImpressions.setModel(new ImpressionListModel());
         jScrollPane1.setViewportView(lImpressions);
 
         jLabel1.setText("Impressions:");
 
         bLoadI.setText("Load Impressions");
         bLoadI.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bLoadIActionPerformed(evt);
             }
         });
 
         bDeleteI.setText("Delete");
         bDeleteI.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bDeleteIActionPerformed(evt);
             }
         });
 
         jLabel2.setText("ISBN");
 
         jLabel3.setText("Author");
 
         jLabel4.setText("Name");
 
         bInsertI.setText("Insert");
         bInsertI.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bInsertIActionPerformed(evt);
             }
         });
 
         jLabel5.setText("Released");
 
         lCustomers.setModel(new CustomerListModel());
         jScrollPane2.setViewportView(lCustomers);
 
         jLabel6.setText("Customers");
 
         bLoadC.setText("Load Customers");
         bLoadC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bLoadCActionPerformed(evt);
             }
         });
 
         bDeleteC.setText("Delete");
         bDeleteC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bDeleteCActionPerformed(evt);
             }
         });
 
         jLabel8.setText("Name");
 
         jLabel11.setText("Address");
 
         bInsertC.setText("Insert");
         bInsertC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bInsertCActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel6)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(layout.createSequentialGroup()
                 .addGap(63, 63, 63)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(bLoadI, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(bDeleteI, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel3)
                             .addComponent(jLabel4)
                             .addComponent(jLabel2)
                             .addComponent(jLabel5))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfDate)
                             .addComponent(tfIsbn)
                             .addComponent(tfName)
                             .addComponent(tfAuthor)))
                     .addComponent(bInsertI, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(bLoadC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(bDeleteC, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel11)
                             .addComponent(jLabel8))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfCName)
                             .addComponent(tfAddress)))
                     .addComponent(bInsertC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(45, 45, 45))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(bLoadI)
                     .addComponent(bLoadC))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(bDeleteI)
                     .addComponent(bDeleteC))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(tfIsbn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel8)
                     .addComponent(tfCName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel3)
                         .addComponent(tfAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel11))
                     .addComponent(tfAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(bInsertC))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel5)
                     .addComponent(tfDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(bInsertI)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void bInsertIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bInsertIActionPerformed
         try {
             if (isFieldEmpty(tfName) || isFieldEmpty(tfAuthor) || isFieldEmpty(tfIsbn) || isFieldEmpty(tfDate)) {
                 showErrorDialog("Please fill in all fields.");
                 return;
             }
             ImpressionTO i = new ImpressionTO();
             i.setName(tfName.getText());
             i.setAuthor(tfAuthor.getText());
             i.setIsbn(tfIsbn.getText());
             try {
                 i.setReleaseDate(LocalDate.parse(tfDate.getText(), DateTimeFormat.forPattern("dd.MM.yyyy")));
             } catch (IllegalArgumentException e) {
                 showErrorDialog("Bad date format");
                 System.err.println(e);
                 return;
             }
             i.setDepartment(Department.CHILDREN);
 
             WebTarget resourceWebTarget = webTarget.path("impressions/");
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
 
             String json = gson.toJson(i, ImpressionTO.class);
 
             Response response = invocationBuilder.post(Entity.json(json));
             System.out.println(response.getStatus());
             bLoadIActionPerformed(null);
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bInsertIActionPerformed
 
     private void bDeleteIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteIActionPerformed
         try {
             if (impressions == null) {
                 return;
             }
             Long id = impressions.get(lImpressions.getSelectedIndex()).getId();
             getImpressionModel().delete(lImpressions.getSelectedIndex());
             WebTarget resourceWebTarget = webTarget.path("impressions/" + id);
             System.out.println("Deleting impression with id " + id + " on position " + lImpressions.getSelectedIndex());
 
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
 
             Response response = invocationBuilder.delete();
             System.out.println(response.getStatus());
             System.out.println(response.readEntity(String.class));
             bLoadIActionPerformed(null);//reload list
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bDeleteIActionPerformed
 
     private void bLoadIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLoadIActionPerformed
         try {
             WebTarget resourceWebTarget = webTarget.path("impressions");
 
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
             Response response = invocationBuilder.get();
 
             System.out.println(response.getStatus());
             String imString = response.readEntity(String.class);
             ImpressionTO[] ims = gson.fromJson(imString, ImpressionTO[].class);
             impressions = new ArrayList<ImpressionTO>(Arrays.asList(ims));
             getImpressionModel().setImpressions(impressions);
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bLoadIActionPerformed
 
     private void bDeleteCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteCActionPerformed
         try {
             if (customers == null) {
                 return;
             }
             Long id = customers.get(lCustomers.getSelectedIndex()).getId();
             getCustomerModel().delete(lCustomers.getSelectedIndex());
             WebTarget resourceWebTarget = webTarget.path("customers/" + id);
             System.out.println("Deleting customer with id " + id + " on position " + lCustomers.getSelectedIndex());
 
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
 
             Response response = invocationBuilder.delete();
             System.out.println(response.getStatus());
             System.out.println(response.readEntity(String.class));
             bLoadIActionPerformed(null);//reload list
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bDeleteCActionPerformed
 
     private void bLoadCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLoadCActionPerformed
         try {
             WebTarget resourceWebTarget = webTarget.path("customers");
 
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
             Response response = invocationBuilder.get();
 
             System.out.println(response.getStatus());
             String ctring = response.readEntity(String.class);
             CustomerTO[] cs = gson.fromJson(ctring, CustomerTO[].class);
             customers = new ArrayList<CustomerTO>(Arrays.asList(cs));
             getCustomerModel().setCustomers(customers);
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bLoadCActionPerformed
 
     private void bInsertCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bInsertCActionPerformed
         try {
             if (isFieldEmpty(tfCName) || isFieldEmpty(tfAddress)) {
                 showErrorDialog("Please fill in all fields.");
                 return;
             }
             CustomerTO c = new CustomerTO();
             c.setName(tfCName.getText());
             c.setAddress(tfAddress.getText());
 
             WebTarget resourceWebTarget = webTarget.path("customers/");
             Invocation.Builder invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON);
             invocationBuilder.header("accept", "application/json");
 
             String json = gson.toJson(c, CustomerTO.class);
 
             Response response = invocationBuilder.post(Entity.json(json));
             System.out.println(response.getStatus());
             bLoadCActionPerformed(null);
         } catch (Exception e) {
             showServerError();
             System.err.println(e);
         }
     }//GEN-LAST:event_bInsertCActionPerformed
 
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new MainFrame().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton bDeleteC;
     private javax.swing.JButton bDeleteI;
     private javax.swing.JButton bInsertC;
     private javax.swing.JButton bInsertI;
     private javax.swing.JButton bLoadC;
     private javax.swing.JButton bLoadI;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JList lCustomers;
     private javax.swing.JList lImpressions;
     private javax.swing.JTextField tfAddress;
     private javax.swing.JTextField tfAuthor;
     private javax.swing.JTextField tfCName;
     private javax.swing.JTextField tfDate;
     private javax.swing.JTextField tfIsbn;
     private javax.swing.JTextField tfName;
     // End of variables declaration//GEN-END:variables
 
     private ImpressionListModel getImpressionModel() {
         return (ImpressionListModel) lImpressions.getModel();
     }
 
     private CustomerListModel getCustomerModel() {
         return (CustomerListModel) lCustomers.getModel();
     }
 
     private void showErrorDialog(String text) {
         JOptionPane.showMessageDialog(this, text, "Error.", JOptionPane.ERROR_MESSAGE);
     }
 
     private void showServerError() {
         showErrorDialog("Failed to connect to the server");
     }
 
     private static boolean isFieldEmpty(javax.swing.JTextField tf) {
         return tf.getText().trim().isEmpty();
     }
 }
