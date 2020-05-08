 package de.fhro.inf.p3.uebung07;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class WildesWuseln extends JFrame {
     private static final long serialVersionUID = 1L;
     private static final int GROESSE = 400;
     private static final int SHIFT_INTERVAL = 50;
     private static final int MAX_SIZE = 10;
     private static final int MAX_VECTOR = 20;
     private ShapeShifter shapeShifter = new ShapeShifter(SHIFT_INTERVAL);
 
     private static final ShapeStrategie strategieDefault = new ShapeStrategieDefault();
    private static final ShapeStrategie strategieRndDirection = new ShapeStrategieRandomDirection(MAX_VECTOR);
    private static final ShapeStrategie strategieBreath = new ShapeStrategieBreath(MAX_SIZE);
 
     public WildesWuseln() {
         JPanel knopfPanel = new JPanel();
         JPanel shapePanel = new JPanel() {
             private static final long serialVersionUID = 1L;
 
             public void update(Graphics g) {
                 paint(g);
             }
 
             public void paint(Graphics g) {
                 super.paint(g);
                 shapeShifter.paint(g);
             }
         };
 
         JLabel statusLabel = new JLabel("");
 
         shapeShifter.setComponents(shapePanel, statusLabel);
         shapeShifter.setStrategie(strategieDefault);
 
         /*JButton buttonNeueKugel = new JButton("Neue Kugel");
         buttonNeueKugel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.add(new MovingShape(new Kugel(Zufall.color(),
                         Zufall.size(MAX_SIZE)), Zufall.loc(getBounds().width), Zufall.loc(getBounds().height),
                         Zufall.speed(MAX_VECTOR), Zufall.speed(MAX_VECTOR)));
             }
         });
         JButton button100NeueKugeln = new JButton("100 Neue Kugeln");
         button100NeueKugeln.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 for (int i = 0; i < 100; ++i)
                     shapeShifter.add(new MovingShape(new Kugel(Zufall.color(),
                             Zufall.size(MAX_SIZE)), Zufall.loc(getBounds().width), Zufall.loc(getBounds().height),
                             Zufall.speed(MAX_VECTOR), Zufall.speed(MAX_VECTOR)));
             }
         });*/
 
         JButton buttonNeueKugelFW = new JButton("Neue Kugel (fw)");
         buttonNeueKugelFW.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.add(new MovingShape(KugelFactory.getInstance().createKugel(Zufall.color(),
                         Zufall.size(MAX_SIZE)), Zufall.loc(getBounds().width), Zufall.loc(getBounds().height),
                         Zufall.speed(MAX_VECTOR), Zufall.speed(MAX_VECTOR)));
             }
         });
         JButton button100NeueKugelnFW = new JButton("100 Neue Kugeln (fw)");
         button100NeueKugelnFW.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 for (int i = 0; i < 100; ++i)
                     shapeShifter.add(new MovingShape(KugelFactory.getInstance().createKugel(Zufall.color(),
                             Zufall.size(MAX_SIZE)), Zufall.loc(getBounds().width), Zufall.loc(getBounds().height),
                             Zufall.speed(MAX_VECTOR), Zufall.speed(MAX_VECTOR)));
             }
         });
 
         JRadioButton buttonSwitchStrategieDefault = new JRadioButton("Default Strategie");
         buttonSwitchStrategieDefault.setSelected(true);
         buttonSwitchStrategieDefault.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.setStrategie(strategieDefault);
             }
         });
         JRadioButton buttonSwitchStrategieRndDirection = new JRadioButton("Random Direction Strategie");
         buttonSwitchStrategieRndDirection.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.setStrategie(strategieRndDirection);
             }
         });
         JRadioButton buttonSwitchStrategieBreath = new JRadioButton("Random Direction Strategie");
         buttonSwitchStrategieBreath.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.setStrategie(strategieBreath);
             }
         });
         ButtonGroup switchStrg = new ButtonGroup();
         switchStrg.add(buttonSwitchStrategieDefault);
         switchStrg.add(buttonSwitchStrategieRndDirection);
         switchStrg.add(buttonSwitchStrategieBreath);
 
 
         JButton buttonClear = new JButton("Aufräumen!");
         buttonClear.setBackground(Color.RED);
         buttonClear.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 shapeShifter.clear();
             }
         });
 
         setTitle("Wildes Wuseln");
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         shapePanel.setSize(GROESSE, GROESSE);
         this.setSize(GROESSE + 100, GROESSE);
         getContentPane().setLayout(new BorderLayout());
         knopfPanel.setLayout(new GridLayout(8, 1));
         /*knopfPanel.add(buttonNeueKugel);
         knopfPanel.add(button100NeueKugeln);*/
         knopfPanel.add(buttonNeueKugelFW);
         knopfPanel.add(button100NeueKugelnFW);
         knopfPanel.add(buttonSwitchStrategieDefault);
         knopfPanel.add(buttonSwitchStrategieRndDirection);
         knopfPanel.add(buttonSwitchStrategieBreath);
         knopfPanel.add(statusLabel);
         knopfPanel.add(buttonClear);
 
         getContentPane().add("West", knopfPanel);
         getContentPane().add("Center", shapePanel);
     }
 
     public static void main(String[] args) {
         new WildesWuseln().setVisible(true);
     }
 }
