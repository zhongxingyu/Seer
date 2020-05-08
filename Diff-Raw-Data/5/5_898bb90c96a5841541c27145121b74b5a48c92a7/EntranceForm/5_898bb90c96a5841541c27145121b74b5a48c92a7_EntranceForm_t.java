 import javax.swing.*;
 import javax.swing.border.*;
 import javax.accessibility.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 // classe que desempenha o papel do formulário/tela inicial do programa
 // necessário para escolher o nome do usuário antes de entrar na sala
 public class EntranceForm extends JPanel {
 
   // JPanels abaixo usados para organizar a informação
   private JPanel centralContainer, inputContainer1, inputContainer2, submitContainer;
   private JTextField txtNome, txtSala;
 
   public EntranceForm() {
     JTextField txt;
     JPanel detailPanel;
     JLabel label;
     JButton btn;
 
     setDefaultLayoutTo(this); // o algoritmo a ser utilizado para dispor os elementos internos (BoxLayout)
     setBorder(new EmptyBorder(new Insets(20,20,20,20))); // definindo as margens internas do form
     setOpaque(true); // garantir que o conteúdo seja mostrado quando esse JPanel for o contentPane
 
     centralContainer = new JPanel();
     setDefaultLayoutTo(centralContainer);
     centralContainer.setBorder(titledBorder("Conecte-se a sala"));
     centralContainer.add(createPadding(10)); // cria uma margem interna no topo de 10px
     add(Box.createVerticalGlue());
     add(centralContainer);
     add(Box.createVerticalGlue());
 
     // primeira linha do formulario
     inputContainer1 = new JPanel();
     inputContainer1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
     label = new JLabel("Seu nome:");
     label.setPreferredSize(new Dimension(100,20));
     label.setHorizontalAlignment(JLabel.RIGHT);
     inputContainer1.add(label);
     inputContainer1.add(createPadding(10));
     txtNome = new JTextField(10);
     // OBS.: implementar, se der tempo, limitação de qtde. de caracteres
     inputContainer1.add(txtNome);
     inputContainer1.add(createPadding(20));
     centralContainer.add(inputContainer1);
     centralContainer.add(createPadding(10));
 
     // segunda linha do formulário
     inputContainer2 = new JPanel();
     inputContainer2.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
     label = new JLabel("Nome da sala:");
     label.setPreferredSize(new Dimension(100,20));
     label.setHorizontalAlignment(JLabel.RIGHT);
     inputContainer2.add(label);
     inputContainer2.add(createPadding(10));
     txtSala = new JTextField(10);
     // OBS.: implementar, se der tempo, limitação de qtde. de caracteres
     inputContainer2.add(txtSala);
     inputContainer2.add(createPadding(20));
     centralContainer.add(inputContainer2);
     centralContainer.add(createPadding(20));
 
     // adicionando a ultima linha dessa tela: o botao de entrar na sala
     submitContainer = new JPanel();
     submitContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
     btn = new JButton("Entrar na sala");
     submitContainer.add(btn);
     submitContainer.add(createPadding(20));
     centralContainer.add(submitContainer);
     centralContainer.add(createPadding(10));
 
     // adicionando o evento de processar os campos quando houver clique do botao
     btn.addMouseListener(new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
        if(txtNome.getText().trim().equals("")) {
          JOptionPane.showMessageDialog(getRootPane(), "Voce precisa informar um nome valido antes.");
          txtNome.requestFocus();
         }
       }
     });
   }
 
   private Container setDefaultLayoutTo(Container obj) {
     obj.setLayout(new BoxLayout(obj, BoxLayout.Y_AXIS));
     return obj;
   }
 
   // método de comodidade para criar enchimentos
   private Component createPadding(int d) {
     return Box.createRigidArea(new Dimension(d, d));
   }
 
   // comodidade para-se criar borda com título
   private AbstractBorder titledBorder(String title) {
     return BorderFactory.createTitledBorder(title);
   }
 
 }
