 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.math.*;
 
 /**
   *
   * Beschreibung
   *
   * @version 1.0 vom 21.09.2011
   * @author  Martin Engelking
   */
 
 public class Rekursion extends JFrame {
   // Anfang Attribute
   private Label label1 = new Label();
   private Label label2 = new Label();
   private Button fibonaccilButton = new Button();
   private Button fakultaetButton = new Button();
   private Button button1 = new Button();
   private TextField eingabeFeld = new TextField();
   // Ende Attribute
 
   public Rekursion(String title) {
     // Frame-Initialisierung
     super(title);
     setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     int frameWidth = 488; 
     int frameHeight = 198;
     setSize(frameWidth, frameHeight);
     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     int x = (d.width - getSize().width) / 2;
     int y = (d.height - getSize().height) / 2;
     setLocation(x, y);
     setResizable(false);
     Container cp = getContentPane();
     cp.setLayout(null);
     // Anfang Komponenten
 
     label1.setBounds(16, 72, 75, 25);
     label1.setText("Eingabe");
     label1.setFont(new Font("Fixedsys", Font.BOLD, 16));
     cp.add(label1);
     label2.setBounds(16, 8, 451, 49);
     label2.setText("Ergebnis: ");
     label2.setFont(new Font("Fixedsys", Font.BOLD, 22));
     cp.add(label2);
     fibonaccilButton.setBounds(16, 112, 161, 33);
     fibonaccilButton.setLabel("Fibonacci");
     fibonaccilButton.addActionListener(new ActionListener() { 
       public void actionPerformed(ActionEvent evt) { 
         fibonaccilButton_ActionPerformed(evt);
       }
     });
     cp.add(fibonaccilButton);
     fakultaetButton.setBounds(184, 112, 161, 33);
    fakultaetButton.setLabel("Fakultt");
     fakultaetButton.addActionListener(new ActionListener() { 
       public void actionPerformed(ActionEvent evt) { 
         fakultaetButton_ActionPerformed(evt);
       }
     });
     cp.add(fakultaetButton);
     button1.setBounds(352, 112, 113, 33);
     button1.setLabel("Ende");
     button1.addActionListener(new ActionListener() { 
       public void actionPerformed(ActionEvent evt) { 
         button1_ActionPerformed(evt);
       }
     });
     cp.add(button1);
     eingabeFeld.setBounds(104, 72, 361, 25);
     cp.add(eingabeFeld);
     // Ende Komponenten
 
     setVisible(true);
   }
 
   // Anfang Methoden
   public void fibonaccilButton_ActionPerformed(ActionEvent evt) {
    label2.setText("Ergebnis: " + fib(new BigInteger(eingabeFeld.getText())));
   }
 
   public void fakultaetButton_ActionPerformed(ActionEvent evt) {
    label2.setText("Ergebnis: " + fakultaet(new BigInteger(eingabeFeld.getText())));
   }
   
   public BigInteger fib(BigInteger n) {
   return n.compareTo(BigInteger.ONE)!=1 ? BigInteger.ONE : n.multiply(fakultaet(n.subtract(BigInteger.ONE)));
   }
   
   public BigInteger fakultaet(BigInteger n) {
   return n.compareTo(BigInteger.ONE)==0 ? BigInteger.ZERO : n.subtract(BigInteger.ONE).add(fib(n.subtract(BigInteger.ONE)));
   }
 
   public void button1_ActionPerformed(ActionEvent evt) {
     dispose();
   }
 
   // Ende Methoden
 
   public static void main(String[] args) {
     new Rekursion("beispiel1");
   }
 }
