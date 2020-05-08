 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 // Definiert eine neue Klasse als Unterklasse von JFrame
 public class MeineGUI extends JFrame implements MusicPlayerListener
 {
     private JTextField jTextField1;
     private JButton jButton1,jButton2,jButton3;
     private JLabel jLabel1;
     private Timer updateLabel;
     
     private MusicPlayer mplayer = new MusicPlayer();
     
     public MeineGUI()
     {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         initComponents();
        pack();
        setVisible(true);
     }
     
     private void initComponents(){
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         jTextField1 = new JTextField();
         jButton1 = new JButton("Öffnen");
         jButton2 = new JButton("Play");
         jButton3 = new JButton("Stop");
         jLabel1 = new JLabel("0%");
         
         jTextField1.setColumns(50);
         
         jButton1.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 open(evt);
             }
         });
         
         jButton2.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 play(evt);
             }
         });
         
         jButton3.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 stop(evt);
             }
         });
         
         mplayer.addListener(this);
         
         setLayout(new FlowLayout());
         
         this.add(jTextField1);
         this.add(jButton1);
         this.add(jButton2);
         this.add(jButton3);
         this.add(jLabel1);
         
         pack();
         setVisible(true);
     }
     
     public void positionChanged(int newPosition){
         jLabel1.setText(String.valueOf(newPosition) + "%");
     }
     
     private void open(ActionEvent evt){
         mplayer.open(jTextField1.getText());
     }
     
     private void play(ActionEvent evt){
         mplayer.play();
     }
     
     
     private void stop(ActionEvent evt){
         mplayer.stop();
     }
     
     public static void main(String[] args){
         MeineGUI g = new MeineGUI();
     }
     
 }
