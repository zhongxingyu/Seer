 package lesson4;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Line2D;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 11.03.12
  * Time: 18:25
  */
 
 public class ButtonPanel extends JPanel implements ActionListener {
 
     private JButton buttonUp;
     private JButton buttonDown;
     private JButton buttonLeft;
     private JButton buttonRight;
     private JButton buttonLighter;
     private JButton buttonDarker;
 
     private JLabel textHint;
 
     private double point_x = 100;
     private double point_y = 150;
     private double point_w = 300;
     private double point_h = 180;
     
     private int alpha = 128;
    private final int SHIFT = 20;
 
     public ButtonPanel(){
         initButton();
         initTextHint();
         initAction();
     }
 
     private void initButton(){
 
         buttonUp = new JButton( "Выше" );
         buttonDown = new JButton( "Ниже" );
         buttonLeft = new JButton( "Левее" );
         buttonRight = new JButton( "Правее" );
         buttonLighter = new JButton( "Светлее" );
         buttonDarker = new JButton( "Темнее" );
 
         Dimension dimension = new Dimension( 125, 25 );
 
         buttonUp.setPreferredSize( dimension );
         buttonDown.setPreferredSize( dimension );
         buttonLeft.setPreferredSize( dimension );
         buttonRight.setPreferredSize( dimension );
         buttonLighter.setPreferredSize( dimension );
         buttonDarker.setPreferredSize( dimension );
 
         this.add(buttonUp);
         this.add(buttonDown);
         this.add(buttonLeft);
         this.add(buttonRight);
         this.add(buttonLighter);
         this.add(buttonDarker);
 
     }
 
     private void initTextHint(){
         textHint = new JLabel();
         textHint.setText("Нажмите кнопку");
         this.add( textHint );
     }
 
     private void initAction(){
         buttonUp.addActionListener( this );
         buttonDown.addActionListener( this );
         buttonLeft.addActionListener( this );
         buttonRight.addActionListener( this );
         buttonLighter.addActionListener( this );
         buttonDarker.addActionListener( this );
     }
 
     @Override
     public void paintComponent(Graphics g){
         super.paintComponent(g);
 
         Graphics2D g2 = (Graphics2D)g;
 
         g2.setStroke( new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,30) );
         g2.setColor( new Color(255, 0, 0, alpha) );
         g2.draw( new Line2D.Double(point_x, point_y, point_w, point_h) );
 
     }
 
     @Override
     public void actionPerformed(ActionEvent event){
 
         Object source = event.getSource();
 
             if ( source == buttonUp ){
                if( point_y > 100 ){
                    this.moveTo(0, -10, 0, -10, buttonUp );
                }
             }
             if( source == buttonDown ){
                if( point_y < 320 ){
                    this.moveTo(0, 10, 0, 10, buttonDown);
                }
             }
             if( source == buttonLeft ){
                if( point_x > 20 ){
                    this.moveTo(-10, 0, -10, 0, buttonLeft);
                }
             }
             if( source == buttonRight ){
                if( point_x < 170 ){
                    this.moveTo(10, 0, 10, 0, buttonRight);
                }
             }
             if( source == buttonLighter ){
                 this.setLighter();
             }
             if( source == buttonDarker ){
                 this.setDarker();
             }
 
         this.repaint();
     }
 
     private void moveTo( double x, double y, double width, double height, JButton button ){
 
        this.point_x += x;
        this.point_y += y;
        this.point_w += width;
        this.point_h += height;
 
         this.setTextHint( button );
     }
 
     private void setLighter(){
         alpha = ((alpha - SHIFT) < SHIFT) ? SHIFT : (alpha -= SHIFT);
         setTextHint( buttonLighter );
     }
 
     private void setDarker(){
         alpha = ((alpha + SHIFT) > 255) ? 255 : (alpha += SHIFT);
         setTextHint( buttonDarker );
     }
 
     private void setTextHint( JButton button ){
         textHint.setText( "Вы нажали кнопку " + button.getText() );
     }
 
 }
