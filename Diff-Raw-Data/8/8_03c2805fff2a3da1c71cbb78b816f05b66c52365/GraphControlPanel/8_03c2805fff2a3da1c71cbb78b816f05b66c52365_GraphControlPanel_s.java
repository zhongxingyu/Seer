 package net.edmacdonald.playground.java2d;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 
 public class GraphControlPanel extends JPanel{
     private final Integer INITIAL_RADIUS = 5;
     private final Integer INITIAL_POINT_COUNT = 10;
     private final Integer INITIAL_H_TRANS = 0;
     private final Integer INITIAL_V_TRANS = 0;
     private final Integer INITIAL_ROT_ANG = 0;
 
     private JSlider radius = new JSlider(1, 10, INITIAL_RADIUS);
     private JSlider pointCount = new JSlider(3,12, INITIAL_POINT_COUNT);
     private JSlider horizontalTranslation = new JSlider(-10, 10, INITIAL_H_TRANS);
     private JSlider verticalTranslation = new JSlider(-10, 10, INITIAL_V_TRANS);
     private JSlider rotationAngle = new JSlider(0, 360, INITIAL_ROT_ANG);
 
     public GraphControlPanel() {
 
         GridLayout layout = new GridLayout(
                 5,  /* rows */
                 4,  /* columns */
                 2,  /* hgap */
                 0   /* vgap */
         );
 
         this.setLayout(layout);
 
         addLabelledSlider("Radius", radius, INITIAL_RADIUS);
         addLabelledSlider("Point Count", pointCount, INITIAL_POINT_COUNT);
         addLabelledSlider("Horizontal Translation", horizontalTranslation, INITIAL_H_TRANS);
         addLabelledSlider("Vertical Translation", verticalTranslation, INITIAL_V_TRANS);
         addLabelledSlider("Rotation Angle", rotationAngle, INITIAL_ROT_ANG);
     }
 
     public JSlider getRadius() {
         return radius;
     }
 
     public JSlider getPointCount() {
         return pointCount;
     }
 
     public JSlider getHorizontalTranslation() {
         return horizontalTranslation;
     }
 
     public JSlider getVerticalTranslation() {
         return verticalTranslation;
     }
 
     public JSlider getRotationAngle() {
         return rotationAngle;
     }
 
     private void addLabelledSlider(String label, final JSlider slider, final Integer resetValue){
         JLabel jlabel = new JLabel(label);
         final JLabel sliderValue = new JLabel(Integer.valueOf(slider.getValue()).toString());
         JButton reset = new JButton("Reset");
 
         slider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 sliderValue.setText(
                     Integer.valueOf(
                             ((JSlider) changeEvent.getSource()).getValue()
                     ).toString()
                 );
             }
         });
 
        reset.addChangeListener(new ChangeListener() {
             @Override
            public void stateChanged(ChangeEvent changeEvent) {
                 slider.setValue(resetValue);
             }
         });
 
         Border border = BorderFactory.createEtchedBorder();
         jlabel.setBorder(border);
         slider.setBorder(border);
         sliderValue.setBorder(border);
         reset.setBorder(border);
 
         this.add(jlabel);
         this.add(slider);
         this.add(sliderValue);
         this.add(reset);
     }
 }
