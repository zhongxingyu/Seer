 import java.awt.*;
 import java.util.*;
 import java.io.File;
 import javax.swing.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import javax.sound.sampled.*;
 
 
 class DieGrafik 
 {
     private static JFrame frame = new JFrame("Bahnhof Gterglck");
     private static JPanel panel = new Zeichner();
     private static Datenmodell datenmodell;
     private static Steuerung steuerung;
     private static ArrayList<Zug> zuege = new ArrayList<Zug>();
     
     private static int[][] signale = new int[25][2];
     private static int[][] weichen = new int[24][6];
     
     private static JFrame frame1;
     private static JRadioButton[] starts = new JRadioButton[4];
     private static JRadioButton[] ziele = new JRadioButton[4];
     private static ButtonGroup groupStart = new ButtonGroup();
     private static ButtonGroup groupZiel = new ButtonGroup();
     private static JTextField textArea = new JTextField();
     private static JComboBox signalBox;
     private static JComboBox weichenBox;
     private static int startnr = -1;
     private static int zielnr = -1;
     private static String[] errorSnds;
     private static ImageIcon fehlerBild;
     public DieGrafik(Datenmodell d, Steuerung s)
     {
         datenmodell = d;
         steuerung = s;
         starts[0] = new JRadioButton("Lindau");
         starts[1] = new JRadioButton("Zerbst");
         starts[2] = new JRadioButton("Barby");
         starts[3] = new JRadioButton("Prdel");
         ziele[0] = new JRadioButton("Lindau ");
         ziele[1] = new JRadioButton("Zerbst ");
         ziele[2] = new JRadioButton("Barby ");
         ziele[3] = new JRadioButton("Prdel ");
         ItemListener ils = new AuswahlListener();
         ItemListener ilz = new AuswahlListener();
         for(int i = 0; i < 4; i++)
         {
             starts[i].addItemListener(ils);
             ziele[i].addItemListener(ilz);
         }
         signale[0][0] = 620;
         signale[0][1] = 468;
         signale[1][0] = 848;
         signale[1][1] = 468;
         signale[2][0] = 1122;
         signale[2][1] = 468;
         signale[3][0] = 544;
         signale[3][1] = 498;
         signale[4][0] = 625;
         signale[4][1] = 484;
         signale[5][0] = 701;
         signale[5][1] = 482;
         signale[6][0] = 849;
         signale[6][1] = 484;
         signale[7][0] = 1114;
         signale[7][1] = 484;
         signale[8][0] = 701;
         signale[8][1] = 498;
         signale[9][0] = 1022;
         signale[9][1] = 498;
         signale[10][0] = 614;
         signale[10][1] = 450;
         signale[11][0] = 787;
         signale[11][1] = 416;
         signale[12][0] = 766;
         signale[12][1] = 432;
         signale[13][0] = 775;
         signale[13][1] = 451;
         signale[14][0] = 839;
         signale[14][1] = 418;
         signale[15][0] = 174;
         signale[15][1] = 581;
         signale[16][0] = 283;
         signale[16][1] = 567;
         signale[17][0] = 500;
         signale[17][1] = 581;
         signale[18][0] = 165;
         signale[18][1] = 600;
         signale[19][0] = 284;
         signale[19][1] = 585;
         signale[20][0] = 499;
         signale[20][1] = 600;
         signale[21][0] = 304;
         signale[21][1] = 602;
         signale[22][0] = 654;
         signale[22][1] = 602;
         signale[23][0] = 507;
         signale[23][1] = 616;
         signale[24][0] = 1058;
         signale[24][1] = 506;
         
         weichen[0][0] = 580;
         weichen[0][1] = 480;
         weichen[0][2] = 560;
         weichen[0][3] = 480;
         weichen[0][4] = 570;
         weichen[0][5] = 495;
         
         weichen[1][0] = 570;
         weichen[1][1] = 495;
         weichen[1][2] = 590;
         weichen[1][3] = 495;
         weichen[1][4] = 580;
         weichen[1][5] = 480;
         
         weichen[2][0] = 610;
         weichen[2][1] = 480;
         weichen[2][2] = 630;
         weichen[2][3] = 480;
         weichen[2][4] = 620;
         weichen[2][5] = 460;
         
         weichen[3][0] = 820;
         weichen[3][1] = 430;
         weichen[3][2] = 800;
         weichen[3][3] = 430;
         weichen[3][4] = 810;
         weichen[3][5] = 415;
         
         weichen[4][0] = 790;
         weichen[4][1] = 430;
         weichen[4][2] = 770;
         weichen[4][3] = 430;
         weichen[4][4] = 775;
         weichen[4][5] = 450;
         errorSnds = new String[] {"Gottes Willa.wav", "wtfwarum.wav"};
         los();
     }
     private static void los()
     {
         frame.getContentPane().add(panel);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setBounds(0,0,1280,985);
         frame.addKeyListener(new TastenListener());
         frame.addMouseListener(new MausListener());
         frame.setVisible(true);
     }
     
     public static void update()
     {
         frame.repaint();
     }
     
     public static void fehlerAusgeben(String fehlerAusgabe)
     {
         JOptionPane.showMessageDialog(frame,fehlerAusgabe,"Error",JOptionPane.ERROR_MESSAGE,fehlerBild);
     }
     
     public static void textAusgeben(String text)
     {
         JOptionPane.showMessageDialog(frame,text,"",JOptionPane.INFORMATION_MESSAGE);
     }
     
     
     private static class Zeichner extends JPanel
     {
         public void paintComponent(Graphics g)
         {
             Graphics2D g2d = (Graphics2D) g;
             String pName = "Gleise 1280x985.jpg";
             ImageIcon ii;
             if (new File("../img/" + pName).exists()) {
                 ii = new ImageIcon("../img/" + pName);
             }
             else
             {
                 ii = new ImageIcon(getClass().getResource(pName));
             }
             ii.paintIcon(this, g2d, 0, 0);
             pName = "gleich klatscht et.jpg";
             if (new File("../img/" + pName).exists()) {
                 fehlerBild = new ImageIcon("../img/" + pName);
             }
             else
             {
                 fehlerBild = new ImageIcon(getClass().getResource(pName));
             }
             g2d.setPaint(Color.black);
             g2d.drawPolygon(new int[] {97,270,270,97},new int[] {123,123,97,97},4);
             g2d.setPaint(Color.white);
             g2d.drawPolygon(new int[] {99,328,328,99},new int[] {121,121,99,99},4);
             g2d.setPaint(Color.black);
             g2d.drawString("Enter: Zug erstellen",105,115);
             g2d.drawString("Leertaste: Weichen & Signale verndern",105,135);
             for(int i = 0; i < 25; i++)
             {
                 if(datenmodell.s[i].getStellung())
                     g2d.setPaint(Color.red);
                 else
                     g2d.setPaint(Color.green);
                 g2d.fillOval(signale[i][0],signale[i][1],8,8);
             }
             g2d.setPaint(Color.black);
             for(int i = 0; i < 23; i++)
             {
                 if(datenmodell.w[i].getStellung())
                 {
                     g2d.drawLine(weichen[i][0],weichen[i][1],weichen[i][2],weichen[i][3]);
                 }
                 else
                 {
                     g2d.drawLine(weichen[i][0],weichen[i][1],weichen[i][4],weichen[i][5]);
                 }
             }
         }
     }
     
     private static class TastenListener implements KeyListener
     {
         public void keyPressed(KeyEvent k)
         {
             if(k.getKeyCode() == KeyEvent.VK_ENTER)
             {
                 frame1 = new JFrame("Zug erstellen");
                 JPanel panel = new JPanel(new GridLayout(2,2,20,20));
                 JPanel startZielPanel = new JPanel(new GridLayout(5,2,10,10));
                 JPanel namePanel = new JPanel(new GridLayout(2,1,10,10));
                 
                 groupStart.add(starts[0]);
                 groupZiel.add(ziele[0]);
                 groupStart.add(starts[1]);
                 groupZiel.add(ziele[1]);
                 groupStart.add(starts[2]);
                 groupZiel.add(ziele[2]);
                 groupStart.add(starts[3]);
                 groupZiel.add(ziele[3]);
                 
                 startZielPanel.add(new JLabel("Start"));
                 startZielPanel.add(new JLabel("Ziel"));
                 startZielPanel.add(starts[0]);
                 startZielPanel.add(ziele[0]);
                 startZielPanel.add(starts[1]);
                 startZielPanel.add(ziele[1]);
                 startZielPanel.add(starts[2]);
                 startZielPanel.add(ziele[2]);
                 startZielPanel.add(starts[3]);
                 startZielPanel.add(ziele[3]);
                 panel.add(startZielPanel);
                 
                 namePanel.add(new JLabel("Name/Bezeichnung des Zuges"));
                 namePanel.add(textArea);
                 panel.add(namePanel);
                 
                 JButton button1 = new JButton("Zug erstellen");
                 button1.addActionListener(new KnopfListener());
                 button1.setPreferredSize(new Dimension(100,40));
                 panel.add(button1);
                 
                 frame1.getContentPane().add(panel);
                 frame1.setBounds(50,50,400,400);
                 frame1.repaint();
                 frame1.pack();
                 frame1.setVisible(true);
             }
             else if(k.getKeyCode() == KeyEvent.VK_ESCAPE)
             {
                 System.exit(0);
             }
             else if(k.getKeyCode() == KeyEvent.VK_SPACE)
             {
                 JFrame frame2 = new JFrame("Signale & Weichen verndern");
                 JPanel panel = new JPanel(new GridLayout(2,3));
                 signalBox = new JComboBox(datenmodell.s);
                 panel.add(signalBox);
                 JButton rotButton = new JButton("auf rot stellen");
                 JButton gruenButton = new JButton("auf grn stellen");
                 panel.add(rotButton);
                 panel.add(gruenButton);
                 weichenBox = new JComboBox(datenmodell.w);
                 panel.add(weichenBox);
                 JButton plusButton = new JButton("auf plus stellen");
                 JButton minusButton = new JButton("auf minus stellen");
                 panel.add(plusButton);
                 panel.add(minusButton);
                 UmstellListener u = new UmstellListener();
                 rotButton.addActionListener(u);
                 gruenButton.addActionListener(u);
                 plusButton.addActionListener(u);
                 minusButton.addActionListener(u);
                 frame2.getContentPane().add(panel);
                 frame2.setBounds(50,50,400,400);
                 frame2.setVisible(true);
                 frame2.pack();
             }
             else if(k.getKeyCode() == KeyEvent.VK_B)
             {
                 steuerung.blockFahren();
             }
         }
         
         public void keyReleased(KeyEvent k)
         {
             
         }
         
         public void keyTyped(KeyEvent k)
         {
             
         }
     }
     
     private static class MausListener implements MouseListener
     {
         public void mouseEntered(MouseEvent m)
         {
         
         }
         
         public void mouseExited(MouseEvent m)
         {
             
         }
         
         public void mouseClicked(MouseEvent m)
         {
             System.out.println("("+m.getX()+"/"+m.getY()+")");
         }
         
         public void mousePressed(MouseEvent m)
         {
         
         }
         
         public void mouseReleased(MouseEvent m)
         {
         
         }
     }
     
     private static class KnopfListener implements ActionListener
     {
         public void actionPerformed(ActionEvent a)
         {
             if((startnr >= 0)&&(zielnr >= 0))
             {
                 if((textArea.getText() != null)&&(!(textArea.getText().equals(""))))
                 {
                     zuege.add(steuerung.zugErstellen(startnr,zielnr,textArea.getText()));
                     frame1.setVisible(false);
                     groupStart.clearSelection();
                     groupZiel.clearSelection();
                     textArea.setText(null);
                     startnr = -1;
                     zielnr = -1;
                 }
                 else
                 {
                    clipMachen(errorSnds[((int)(Math.random()*errorSnds.length))]).start();
                     fehlerAusgeben("Der Zug hat keinen Bezeichner!");
                 }
             }
             else
             {
                clipMachen(errorSnds[((int)(Math.random()*errorSnds.length))]).start();
                 fehlerAusgeben("Kein Start und/oder Ziel ausgewhlt!");
             }
         }
         
         private Clip clipMachen(String name)
         {
             Clip clip = null;
             File file;
             try
             {
                 if (new File("../sounds/" + name).exists()) {
                     file = new File("../sounds/" + name);
                 }
                 else
                 {
                     file = new File(getClass().getResource(name)+"");
                 }
                 AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                 clip = AudioSystem.getClip();
                 clip.open(audioIn);
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
             return clip;
         }
     }
     private static class AuswahlListener implements ItemListener
     {
         boolean ausgewaehlt = false;
         public void itemStateChanged(ItemEvent a)
         {
             if(!ausgewaehlt)
             {
                 JRadioButton knopf = ((JRadioButton)a.getItem());
                 for(int i = 0; i < 4; i++)
                 {
                     if(starts[i].getText().equals(knopf.getText()))
                     {
                         ziele[i].setEnabled(false);
                         startnr = i;
                     }
                     if(ziele[i].getText().equals(knopf.getText()))
                     {
                         starts[i].setEnabled(false);
                         zielnr = i;
                     }
                 }
                 ausgewaehlt = true;
             }
             else
             {
                 JRadioButton knopf = ((JRadioButton)a.getItem());
                 for(int i = 0; i < 4; i++)
                 {
                     if(starts[i].getText().equals(knopf.getText()))
                         ziele[i].setEnabled(true);
                     if(ziele[i].getText().equals(knopf.getText()))
                         starts[i].setEnabled(true);
                 }
                 ausgewaehlt = false;
             }
         }
     }
     private static class UmstellListener implements ActionListener
     {
         public void actionPerformed(ActionEvent a)
         {
             if((((JButton)a.getSource()).getText().equals("auf rot stellen")))
             {
                 datenmodell.s[signalBox.getSelectedIndex()].setStellung(true);
             }
             else if((((JButton)a.getSource()).getText().equals("auf grn stellen")))
             {
                 datenmodell.s[signalBox.getSelectedIndex()].setStellung(false);
             }
             else if((((JButton)a.getSource()).getText().equals("auf plus stellen")))
             {
                 datenmodell.w[weichenBox.getSelectedIndex()].setStellung(true);
             }
             else if((((JButton)a.getSource()).getText().equals("auf minus stellen")))
             {
                 datenmodell.w[weichenBox.getSelectedIndex()].setStellung(false);
             }
             update();
         }
     }
 }
