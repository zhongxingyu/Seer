 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.ImageObserver;
 import java.awt.image.ImageProducer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
import tests.LaunchEditor;

 import model.classes.Editor;
 
 public class MainForm extends JFrame {
     /**
 	 * 
 	 */
     private static final long      serialVersionUID     = 3140124981567096416L;
     public static final String     DEFAULT_TITLE        = "Text Editor";
     private static final String    DEFAULT_COMMAND_LINE = "Please, type your command here to start using the software";
     private static final int       DEFAULT_COLUMN_WIDTH = 40;
     private static final Dimension DEFAULT_DIMENSION    = new Dimension(640,
                                                                 480);
     private static final Dimension MINIMUM_DIMENSION    = new Dimension(500,
                                                                 480);
     private JLabel                 m_Text               = new JLabel(
                                                                 DEFAULT_COMMAND_LINE);
     private JTextField             m_Command            = new JTextField(
                                                                 DEFAULT_COMMAND_LINE,
                                                                 DEFAULT_COLUMN_WIDTH);
     private JButton                m_Validate           = new JButton(
                                                                 "Validate");
 
     public MainForm() {
         if (!System.getProperty("os.name").toLowerCase().contains("mac os")) {
             this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                    LaunchEditor.class.getResource("images/notes.png")));
         }
         this.setTitle(DEFAULT_TITLE);
         this.setSize(DEFAULT_DIMENSION);
         // this.setLayout(new GridLayout(2, 1));
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
         JPanel up = new JPanel(new FlowLayout());
         up.setMinimumSize(MINIMUM_DIMENSION);
         up.add(this.m_Text);
         up.setBorder(BorderFactory.createTitledBorder("Text"));
         JPanel down = new JPanel(new FlowLayout());
         down.add(this.m_Command);
         down.add(this.m_Validate);
         down.setBorder(BorderFactory.createTitledBorder("Management"));
         this.getContentPane().add(up, BorderLayout.NORTH);
         this.getContentPane().add(down, BorderLayout.SOUTH);
         this.setVisible(true);
    
     }
 
     public JTextField getCommand() {
         return this.m_Command;
     }
 
     public JLabel getText() {
         return this.m_Text;
     }
 
     public JButton getValidate() {
         return this.m_Validate;
     }
 }
