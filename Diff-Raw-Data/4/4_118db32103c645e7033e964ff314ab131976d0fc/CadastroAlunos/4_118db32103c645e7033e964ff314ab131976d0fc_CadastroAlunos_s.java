 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.edu.utfpr.cm.tsi.projetointegrador.view;
 
 import br.edu.utfpr.cm.tsi.projetointegrador.DAO.AlunoDao;
 import br.edu.utfpr.cm.tsi.projetointegrador.entidade.Aluno;
 import br.edu.utfpr.cm.tsi.projetointegrador.entidade.Endereco;
 import br.edu.utfpr.cm.tsi.projetointegrador.enums.EstadoEnum;
 import br.edu.utfpr.cm.tsi.projetointegrador.util.MaskUtil;
 import br.edu.utfpr.cm.tsi.projetointegrador.util.Utilitarios;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFormattedTextField;
 import javax.swing.text.MaskFormatter;
 import br.edu.utfpr.cm.tsi.projetointegrador.util.FixedLengthDocumentUtil;
 
 /**
  *
  * @author Daniele
  */
 public class CadastroAlunos extends javax.swing.JDialog {
 
     private AlunoDao alunoDao;
     
     //   ValidadorAlunos valida=new ValidadorAlunos();
     boolean edicao = false;
 
     /**
      * Creates new form CadastroAlunos
      */
     public CadastroAlunos(java.awt.Frame parent, boolean modal) {
         initComponents();
         aplicarMascaras();
         setTitle("Cadastro de Alunos");
         setVisible(true);
         setLocationRelativeTo(null);
         pack();
         setResizable(false);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     }
     
     private void aplicarMascaras(){
         MaskUtil mask = new MaskUtil();
         try {
             mask.maskCep(jTextCep);
             mask.maskCpf(jTextCPF);
             mask.maskData(jTextDataNascimento);
             mask.maskTelFixo(jTextTelefone);
         } catch (ParseException ex) {
             Logger.getLogger(CadastroAlunos.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void setEditAluno(){
         jTextCodigo.setText(String.valueOf(AlunoDao.getAlunoSelecionado().getId()));
         jTextNome.setText(AlunoDao.getAlunoSelecionado().getNome());
         jTextCPF.setText(AlunoDao.getAlunoSelecionado().getCpf());
         jTextRG.setText(AlunoDao.getAlunoSelecionado().getRg());
         
         SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
         
         jTextDataNascimento.setText(format.format(AlunoDao.getAlunoSelecionado().getDataNascimento()));
         jTextEndereco.setText(AlunoDao.getAlunoSelecionado().getEndereco().getNomeEndereco());
         jTextNumero.setText(String.valueOf(AlunoDao.getAlunoSelecionado().getEndereco().getNumero()));
         jTextComplemento.setText(AlunoDao.getAlunoSelecionado().getEndereco().getComplemento());
         jTextBairro.setText(AlunoDao.getAlunoSelecionado().getEndereco().getBairro());
         jTextMunicipio.setText(AlunoDao.getAlunoSelecionado().getEndereco().getMunicipio());
         jComboEstado.setSelectedItem(AlunoDao.getAlunoSelecionado().getEndereco().getEstado().getUf());
         jTextTelefone.setText(AlunoDao.getAlunoSelecionado().getTelefone());
         jTextCep.setText(AlunoDao.getAlunoSelecionado().getEndereco().getCep());
         
         this.edicao = true;
     }
     
     private void setAluno(Aluno aluno) throws ParseException, Exception{
         aluno.setNome(jTextNome.getText().trim());
         aluno.setCpf(Utilitarios.formatString(jTextCPF.getText().trim()));
         aluno.setRg(jTextRG.getText().trim());
         SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy");
         aluno.setDataNascimento(data.parse(jTextDataNascimento.getText()));
         aluno.setEndereco(new Endereco());
         aluno.getEndereco().setNomeEndereco(jTextEndereco.getText().trim());
         aluno.getEndereco().setNumero(Integer.parseInt(jTextNumero.getText().trim()));
         aluno.getEndereco().setComplemento(jTextComplemento.getText().trim());
         aluno.getEndereco().setBairro(jTextBairro.getText().trim());
         aluno.getEndereco().setMunicipio(jTextMunicipio.getText().trim());
         aluno.getEndereco().setEstado(EstadoEnum.getEnum(jComboEstado.getSelectedItem().toString()));
         aluno.getEndereco().setCep(Utilitarios.formatString(jTextCep.getText().trim()));
         aluno.setTelefone(Utilitarios.formatString(jTextTelefone.getText().trim()));
         
         alunoDao = new AlunoDao();
         
             
         alunoDao.saveOrUpdate(aluno);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jColorChooser1 = new javax.swing.JColorChooser();
         jLabel1 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jTextNome = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jTextEndereco = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jButtonGravar = new javax.swing.JButton();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jTextNumero = new javax.swing.JTextField();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jTextRG = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jTextComplemento = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jTextBairro = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jTextMunicipio = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         jComboEstado = new javax.swing.JComboBox();
         jLabel16 = new javax.swing.JLabel();
         jLabel17 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jTextCodigo = new javax.swing.JTextField();
         jButton2 = new javax.swing.JButton();
         jLabel21 = new javax.swing.JLabel();
         jTextCep = new javax.swing.JFormattedTextField();
         jTextCPF = new javax.swing.JFormattedTextField();
         jTextDataNascimento = new javax.swing.JFormattedTextField();
         jTextTelefone = new javax.swing.JFormattedTextField();
         jLabel22 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel1.setText("Cadastro de Alunos");
 
         jLabel3.setText("Nome");
 
         jTextNome.setDocument(new FixedLengthDocumentUtil(100));
         jTextNome.setName(""); // NOI18N
         jTextNome.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextNomeActionPerformed(evt);
             }
         });
         jTextNome.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextNomeFocusLost(evt);
             }
         });
         jTextNome.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 jTextNomeKeyTyped(evt);
             }
         });
 
         jLabel4.setText("CPF");
 
         jLabel5.setText("Endereço");
 
         jTextEndereco.setDocument(new FixedLengthDocumentUtil(100));
         jTextEndereco.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextEnderecoActionPerformed(evt);
             }
         });
 
         jLabel6.setText("Telefone");
 
         jButtonGravar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cadastrar.png"))); // NOI18N
         jButtonGravar.setText("Gravar");
         jButtonGravar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonGravarActionPerformed(evt);
             }
         });
 
         jLabel7.setForeground(new java.awt.Color(255, 51, 0));
         jLabel7.setText("*");
 
         jLabel8.setText("Número");
 
         jTextNumero.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextNumeroActionPerformed(evt);
             }
         });
 
         jLabel9.setForeground(new java.awt.Color(255, 0, 0));
         jLabel9.setText("*");
 
         jLabel10.setText("RG");
 
         jLabel11.setForeground(new java.awt.Color(255, 0, 0));
         jLabel11.setText("*");
 
         jTextRG.setDocument(new FixedLengthDocumentUtil(8));
         jTextRG.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextRGActionPerformed(evt);
             }
         });
 
         jLabel2.setText("Data Nascimento");
 
         jLabel12.setText("Complemento");
 
         jTextComplemento.setDocument(new FixedLengthDocumentUtil(60));
 
         jLabel13.setText("Bairro");
 
         jTextBairro.setDocument(new FixedLengthDocumentUtil(30));
 
         jLabel14.setText("Municipio");
 
         jTextMunicipio.setDocument(new FixedLengthDocumentUtil(60));
 
         jLabel15.setText("Estado");
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/fechar.png"))); // NOI18N
         jButton1.setText("Fechar");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jComboEstado.setModel(new javax.swing.DefaultComboBoxModel(EstadoEnum.getUfs()));
         jComboEstado.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboEstadoActionPerformed(evt);
             }
         });
 
         jLabel16.setForeground(new java.awt.Color(0, 0, 204));
         jLabel16.setText("Dados pessoais ");
 
         jLabel17.setForeground(new java.awt.Color(51, 0, 204));
         jLabel17.setText("Contato");
 
         jLabel18.setForeground(new java.awt.Color(255, 0, 0));
         jLabel18.setText("*");
 
         jLabel20.setText("Código");
 
         jTextCodigo.setEnabled(false);
 
         jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pesquisar.png"))); // NOI18N
         jButton2.setText("Pesquisar");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel21.setText("Cep");
 
         jLabel22.setForeground(new java.awt.Color(255, 0, 0));
         jLabel22.setText("*");
 
         jLabel19.setText("Campos Obrigatórios");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(42, 42, 42)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addGap(0, 0, Short.MAX_VALUE)
                                 .addComponent(jButtonGravar)
                                 .addGap(18, 18, 18)
                                 .addComponent(jButton1))
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jLabel17)
                                 .addGap(3, 3, 3)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel5)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                         .addComponent(jTextEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(jLabel12)
                                             .addComponent(jLabel14)
                                             .addComponent(jLabel6))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                             .addComponent(jTextComplemento, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                             .addComponent(jTextMunicipio)
                                             .addComponent(jTextTelefone))))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel8)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                                         .addComponent(jTextNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel13)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addComponent(jTextBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(jLabel15)
                                             .addComponent(jLabel21))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                             .addComponent(jComboEstado, 0, 157, Short.MAX_VALUE)
                                             .addComponent(jTextCep)))))
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel20)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(18, 18, 18)
                                         .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel2)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextDataNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(jLabel16))
                                 .addGap(0, 0, Short.MAX_VALUE)))
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel4)
                             .addComponent(jLabel3))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jTextNome, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(0, 0, Short.MAX_VALUE))
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jLabel7)
                                 .addGap(27, 27, 27)
                                 .addComponent(jTextCPF, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jLabel10)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel11)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jTextRG, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
             .addGroup(layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel1)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel19)))
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(4, 4, 4)
                 .addComponent(jLabel1)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel20)
                     .addComponent(jTextCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton2))
                 .addGap(43, 43, 43)
                 .addComponent(jLabel16)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(jLabel9)
                     .addComponent(jTextNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(jLabel7)
                     .addComponent(jTextCPF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel10)
                     .addComponent(jLabel11)
                     .addComponent(jTextRG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(jLabel18)
                     .addComponent(jTextDataNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(31, 31, 31)
                 .addComponent(jLabel17)
                 .addGap(24, 24, 24)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel5)
                     .addComponent(jLabel8)
                     .addComponent(jTextNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel12)
                     .addComponent(jTextComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13)
                     .addComponent(jTextBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(jTextMunicipio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel15)
                     .addComponent(jComboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel21)
                         .addComponent(jTextCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jTextTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addGap(46, 46, 46)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonGravar))
                 .addGap(30, 30, 30)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel22)
                     .addComponent(jLabel19)))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jTextNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextNomeActionPerformed
         //if (jTextNome.getText().equals("   ")) {
         //  JOptionPane.showMessageDialog(null, "Por favor coloque seu nome");
         //jTextNom
         // jTextNome.addActionListener(new ActionListener(){  
         //    public void ActionPerformed(ActionEvent e){  
         //    if(jtextfield.getText().isEmpty()){  
         //        JOptionPane.showMessageDialog(null, "Inválido", "Erro", JOptionPane.ERROR_MESSAGE);  
         //   } else {  
         //Passou na validação  
         //Faz algo aqui  
         //  }  
         //  }  
 //});  
     }//GEN-LAST:event_jTextNomeActionPerformed
 
     private void jTextEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextEnderecoActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextEnderecoActionPerformed
 
     private void jButtonGravarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGravarActionPerformed
         if(!edicao){
             try {
                 this.setAluno(new Aluno());
             } catch (Exception ex) {
                 Logger.getLogger(CadastroAlunos.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         else{
             try {
                 this.setAluno(AlunoDao.getAlunoSelecionado());
             } catch (Exception ex) {
                 Logger.getLogger(CadastroAlunos.class.getName()).log(Level.SEVERE, null, ex);
             } 
         }
         this.limparCampos();
 
 
     }//GEN-LAST:event_jButtonGravarActionPerformed
 
     private void jTextNomeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextNomeKeyTyped
     }//GEN-LAST:event_jTextNomeKeyTyped
 
     private void jTextNomeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextNomeFocusLost
 //           if(jTextNome.getText().isEmpty()){
 //          JOptionPane.showMessageDialog(null, "Campo NOME  \n esta em branco!!", "Atenção", JOptionPane.ERROR_MESSAGE);
 //            jTextNome.requestFocus();
 //       }else{
 //           KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();  
 //            manager.focusNextComponent();  
 //       }
     }//GEN-LAST:event_jTextNomeFocusLost
 
     private void jTextNumeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextNumeroActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextNumeroActionPerformed
 
     private void jComboEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboEstadoActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jComboEstadoActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         this.dispose();
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jTextRGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextRGActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextRGActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         AlunoConsultas dialog = new AlunoConsultas(null, true);
         dialog.setLocation(getX()+50, getY()+50);
         dialog.setVisible(true);
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void limparCampos() {
         jTextNome.setText(" ");
         jTextCPF.setText(" ");
         jTextRG.setText("");
         jTextDataNascimento.setText(" ");
         jTextEndereco.setText(" ");
         jTextNumero.setText(" ");
         jTextNumero.setText(" ");
         jTextComplemento.setText(" ");
         jTextBairro.setText(" ");
         jTextMunicipio.setText(" ");
         jComboEstado.setSelectedIndex(0);
         jTextTelefone.setText(null);
     }
 
     /**
      * @param args the command line arguments
      */
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
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(CadastroAlunos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(CadastroAlunos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(CadastroAlunos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(CadastroAlunos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new CadastroAlunos(null, true).setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButtonGravar;
     private javax.swing.JColorChooser jColorChooser1;
     private javax.swing.JComboBox jComboEstado;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JTextField jTextBairro;
     private javax.swing.JFormattedTextField jTextCPF;
     private javax.swing.JFormattedTextField jTextCep;
     private javax.swing.JTextField jTextCodigo;
     private javax.swing.JTextField jTextComplemento;
     private javax.swing.JFormattedTextField jTextDataNascimento;
     private javax.swing.JTextField jTextEndereco;
     private javax.swing.JTextField jTextMunicipio;
     private javax.swing.JTextField jTextNome;
     private javax.swing.JTextField jTextNumero;
     private javax.swing.JTextField jTextRG;
     private javax.swing.JFormattedTextField jTextTelefone;
     // End of variables declaration//GEN-END:variables
 }
