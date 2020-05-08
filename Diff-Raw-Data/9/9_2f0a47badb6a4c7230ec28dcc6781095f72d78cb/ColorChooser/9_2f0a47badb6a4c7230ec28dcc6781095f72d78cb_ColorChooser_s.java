 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.swing.BorderFactory;
 import javax.swing.JColorChooser;
 import javax.swing.JFrame;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 /**
  * Creates a color chooser for SLOGO
  * 
  * @author Yoshida
  * 
  */
 public class ColorChooser extends JFrame
         implements ChangeListener {
     
     private static final long serialVersionUID = 1L;
     private static final Dimension CHOOSER_BOUNDS = new Dimension(800, 600);
     private static final int MAX_RGB = 255;
     private static final String SET_PALLETE = "SetPalette";
     private static final String SET_PEN_COLOR = "SetPenColor";
     private JColorChooser myTcc;
     private View myView;
     private Color myColor;
     
     /**
      * Creates a color chooser with an various types of color.
      * 
      * @param view The SLogo view
      */
     public ColorChooser (View view) {
         myView = view;
         setTitle("SLogo Color Chooser");
         // Set up color chooser for setting text color
         myTcc = new JColorChooser();
         myTcc.getSelectionModel().addChangeListener(this);
         myTcc.setBorder(BorderFactory.createTitledBorder("Choose Pen Color"));
         myTcc.setPreferredSize(CHOOSER_BOUNDS);
         add(myTcc, BorderLayout.CENTER);
         pack();
         setVisible(true);
         addWindowListener(new java.awt.event.WindowAdapter() {
             @Override
             public void windowClosing (java.awt.event.WindowEvent windowEvent) {
                 float[] compArray = null;
                 compArray = myColor.getRGBColorComponents(compArray);
                 int red = (int) (compArray[0] * MAX_RGB);
                 int green = (int) (compArray[1] * MAX_RGB);
                 int blue = (int) (compArray[2] * MAX_RGB);
                 myView.getController().createRunInstruction(getResourceLocation(SET_PALLETE) + " " +
                                                             1 + " " + red + " " +
                                                             green + " " + blue);
                 myView.getController().createRunInstruction(getResourceLocation(SET_PEN_COLOR) +
                                                             " 1");
             }
         });
     }
     
     /**
      * Listens to a new color picked by the user.
      * 
      * @param e The event from the ColorChooser
      */
     public void stateChanged (ChangeEvent e) {
         myColor = myTcc.getColor();
     }
     
     private String getResourceLocation (String input) {
         String allCommands = myView.getResources().getString(input);
         String command = allCommands.split(",")[0];
         return command;
     }
 }
