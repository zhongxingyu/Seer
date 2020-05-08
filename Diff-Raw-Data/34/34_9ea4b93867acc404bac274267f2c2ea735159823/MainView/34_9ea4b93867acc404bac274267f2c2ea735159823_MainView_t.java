 package layarq.VIEW;
 import javax.swing.SpinnerNumberModel;
 import layarq.Objetos.LayoutTableModel;
 import layarq.CONTROLLER.MainController;
 
 /**
  *
  * @author cristian.oliveira
  */
 public class MainView extends View {
 
     MainController controller =  new MainController();
     
     /**
      * Creates new form MainView
      */
     public MainView() {
         this.setLocationRelativeTo(null);
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         txtArquivoLayout = new javax.swing.JTextField();
         btnSelecionarLayout = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblLayouts = new javax.swing.JTable();
         jLabel2 = new javax.swing.JLabel();
         txtArquivoValidar = new javax.swing.JTextField();
         btnSelecionarArquivo = new javax.swing.JButton();
         cbxLinha = new javax.swing.JSpinner();
         jLabel3 = new javax.swing.JLabel();
         btnAnalisar = new javax.swing.JButton();
         txtLinhaSelecionada = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jLabel1.setText("Selecione o Layout");
 
         btnSelecionarLayout.setText("Selec.");
         btnSelecionarLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelecionarLayoutActionPerformed(evt);
             }
         });
 
         tblLayouts.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Dados"
             }
         ));
         tblLayouts.setRowSelectionAllowed(false);
         jScrollPane1.setViewportView(tblLayouts);
 
         jLabel2.setText("Selecione o Arquivo para Analisar");
 
         btnSelecionarArquivo.setText("Selec.");
         btnSelecionarArquivo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelecionarArquivoActionPerformed(evt);
             }
         });
 
         jLabel3.setText("Linha");
 
         btnAnalisar.setText("Analisar");
         btnAnalisar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAnalisarActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(txtLinhaSelecionada)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                         .addComponent(jLabel3)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbxLinha, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAnalisar))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtArquivoValidar)
                            .addComponent(txtArquivoLayout))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(btnSelecionarLayout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSelecionarArquivo))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addGap(0, 0, 0)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtArquivoLayout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSelecionarLayout))
                 .addGap(0, 0, 0)
                 .addComponent(jLabel2)
                 .addGap(0, 0, 0)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtArquivoValidar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSelecionarArquivo))
                 .addGap(2, 2, 2)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cbxLinha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3)
                     .addComponent(btnAnalisar))
                 .addGap(1, 1, 1)
                 .addComponent(txtLinhaSelecionada, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSelecionarLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarLayoutActionPerformed
         txtArquivoLayout.setText(controller.selecionaLayout());
         tblLayouts.setModel(controller.getLayoutTableModel());
     }//GEN-LAST:event_btnSelecionarLayoutActionPerformed
 
     private void btnSelecionarArquivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarArquivoActionPerformed
         txtArquivoValidar.setText(controller.selecionaArquivo());
         cbxLinha.setModel(new SpinnerNumberModel(1, 1, controller.getQuantidadeLinhas(),1));
     }//GEN-LAST:event_btnSelecionarArquivoActionPerformed
 
     private void btnAnalisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalisarActionPerformed
         int linha = (Integer)cbxLinha.getValue();
         controller.selecionaLinha(linha-1);
         txtLinhaSelecionada.setText(controller.getLinhaSelecionada());
         tblLayouts.setModel(controller.getLayoutTableModel());
     }//GEN-LAST:event_btnAnalisarActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new MainView().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAnalisar;
     private javax.swing.JButton btnSelecionarArquivo;
     private javax.swing.JButton btnSelecionarLayout;
     private javax.swing.JSpinner cbxLinha;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTable tblLayouts;
     private javax.swing.JTextField txtArquivoLayout;
     private javax.swing.JTextField txtArquivoValidar;
     private javax.swing.JTextField txtLinhaSelecionada;
     // End of variables declaration//GEN-END:variables
 }
