 /*
 Andrew Darwin
 CSC 420: Graphical User Interfaces
 Fall 2012
 */
 
 //package csc420.hw2;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.Graphics;
 import java.awt.Point;
 import javax.swing.border.MatteBorder;
 import javax.swing.BoxLayout;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JWindow;
 import javax.swing.plaf.*;
 import javax.swing.SpringLayout;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
//import com.jtattoo.plaf.*;
 
 public class CircleDraw
 {
 
 
   private static JFrame frame, colorChooserFrame;
   private static JColorChooser colorChooser;
   private static CustomCanvas canvas;
   private static JPanel canvasPanel;
   private static JSlider leftSlider, bottomSlider, sizeSlider;
   private static JButton showButton;
   private static final int sliderGradient = 2000;
 
 
 
 
 
   private static void createAndShowGUI()
   {
     frame = new JFrame("Circle Draw");
     frame.setPreferredSize(new Dimension(800, 600));
     frame.setMinimumSize(new Dimension(350, 300));
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     addComponentsToPane(frame.getContentPane());
     frame.pack();
     frame.setLocationRelativeTo(null);
     frame.setVisible(true);
   }
 
 
 
   public static int getXCoord()
   {
     Integer inputX = new Integer(bottomSlider.getValue());
     double xProportion = inputX.doubleValue()/sliderGradient;
     int canvasWidth = canvas.getWidth();
     int xCoordinate = (int)(canvasWidth*xProportion);
     return xCoordinate;
   }
   public static int getYCoord()
   {
     Integer inputY = new Integer(leftSlider.getValue());
     double yProportion = inputY.doubleValue()/sliderGradient;
     int canvasHeight = canvas.getHeight();
     int yCoordinate = (int)(canvasHeight*(1-yProportion));
     return yCoordinate;
   }
   public static int getRadius() { return sizeSlider.getValue(); }
   public static Color getColor() { return colorChooser == null ? Color.black : colorChooser.getColor(); }
 
 
   private static void resizeCircle(int amountToChangeRadius)
   {
     if (showButton.getText().equals("Hide"))
     {
       sizeSlider.setValue(canvas.getCircleRadius()-amountToChangeRadius);
     }
   }
 
   private static void setSlidersTo(int x, int y)
   {
     if (showButton.getText().equals("Hide"))
     {
       Integer canvasHeight = new Integer(canvas.getHeight());
       Integer canvasWidth = new Integer(canvas.getWidth());
       Integer xCoord = new Integer(x);
       Integer yCoord = new Integer(y);
       double yProportion = yCoord.doubleValue()/canvasHeight.doubleValue();
       double xProportion = xCoord.doubleValue()/canvasWidth.doubleValue();
       SliderChangeListener.bypassDrawing = true;
       leftSlider.setValue((int)((1-yProportion)*sliderGradient));
       bottomSlider.setValue((int)(xProportion*sliderGradient));
       SliderChangeListener.bypassDrawing = false;
       //drawCircle();
     }
   }
   protected static void drawCircle()
   {
     if (showButton.getText().equals("Hide"))
     {
       canvas.repaint();
     }
   }
 
 
 
 
   
   private static void displayColorChooser()
   {
     if (colorChooserFrame == null)
     {
       colorChooserFrame = new JFrame("Color Chooser");
       colorChooserFrame.setAlwaysOnTop(true);
       colorChooserFrame.setResizable(false);
       colorChooser = new JColorChooser(Color.black);
       colorChooser.getSelectionModel().addChangeListener(new ChangeListener()
       {
         public void stateChanged(ChangeEvent e)
         {
           CircleDraw.drawCircle();
         }
       });
       colorChooserFrame.add(colorChooser);
       colorChooserFrame.pack();
       colorChooserFrame.setLocationRelativeTo(null);
     }
     if (!colorChooserFrame.isVisible())
     {
       Point frameLocation = frame.getLocationOnScreen();
       int frameWidth = frame.getWidth();
       Toolkit toolkit = Toolkit.getDefaultToolkit();
       Dimension screenSize = toolkit.getScreenSize();
       int colorChooserWidth = colorChooserFrame.getWidth();
       if (frameLocation.x + frameWidth + colorChooserWidth < screenSize.width)
       {
         colorChooserFrame.setLocation(frameLocation.x + frameWidth, frameLocation.y);
       }
       else if (frameLocation.x  - colorChooserWidth >= 0)
       {
         colorChooserFrame.setLocation(frameLocation.x - colorChooserWidth, frameLocation.y);
       }
       else
       {
         colorChooserFrame.setLocation(screenSize.width-colorChooserWidth, frameLocation.y);
       }
       colorChooserFrame.setVisible(true);
     }
   }
 
 
 
 
 
   private static void addComponentsToPane(Container contentPane)
   {
     canvasPanel = new JPanel();
     canvasPanel.setLayout(new BorderLayout());
     canvasPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
     canvas = new CustomCanvas();
     //canvasPanel.add(canvas, BorderLayout.CENTER);
     SpringLayout frameLayout = new SpringLayout();
     frame.setLayout(frameLayout);
 
     leftSlider = new JSlider();
     leftSlider.setOrientation(SwingConstants.VERTICAL);
     leftSlider.setMinimum(0);
     leftSlider.setMaximum(sliderGradient);
     leftSlider.setValue(sliderGradient/2);
     /*
     JSlider rightSlider = new JSlider();
     rightSlider.setOrientation(SwingConstants.VERTICAL);
     JSlider topSlider = new JSlider();
     */
     bottomSlider = new JSlider();
     bottomSlider.setMinimum(0);
     bottomSlider.setMaximum(sliderGradient);
     bottomSlider.setValue(sliderGradient/2);
 
     sizeSlider = new JSlider();
     sizeSlider.setMinimum(5);
     sizeSlider.setMaximum(300);
     sizeSlider.setValue(50);
 
     showButton = new JButton("Show");
     showButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 
     JButton colorButton = new JButton("Color Chooser");
 
 
 
 
     //-------------------- Add Listeners --------------------
       //--------------- Mouse Listeners ---------------
         canvas.addMouseListener(new MouseListener()
         {
           public void mouseClicked(MouseEvent e) {}
           public void mouseEntered(MouseEvent e) {}
           public void mouseExited(MouseEvent e) {}
           public void mousePressed(MouseEvent e)
           {
             if (showButton.getText().equals("Hide"))
             {
               if (e.getButton() == MouseEvent.BUTTON1)
               {
                 Point point = e.getPoint();
                 setSlidersTo(point.x, point.y);
                 drawCircle();
               }
               else if (e.getButton() == MouseEvent.BUTTON3)
               {
                 canvas.setFilled(!canvas.isFilled());
                 drawCircle();
               }
             }
           }
           public void mouseReleased(MouseEvent e) {}
         });
 
         canvas.addMouseMotionListener(new MouseMotionListener()
         {
           public void mouseDragged(MouseEvent e)
           {
            if (showButton.getText().equals("Hide"))
             {
               Point point = e.getPoint();
               setSlidersTo(point.x, point.y);
               drawCircle();
             }
           }
           public void mouseMoved(MouseEvent e) {}
         });
 
         canvas.addMouseWheelListener(new MouseWheelListener()
         {
           public void mouseWheelMoved(MouseWheelEvent e)
           {
             int unitsToScroll = e.getUnitsToScroll();
             resizeCircle(unitsToScroll);
           }
         });
 
       //--------------- Slider Listeners ---------------
         SliderChangeListener changeListener = new SliderChangeListener();
         leftSlider.addChangeListener(changeListener);
         bottomSlider.addChangeListener(changeListener);
         sizeSlider.addChangeListener(changeListener);
 
       //--------------- Button/Action Listeners ---------------
         showButton.addActionListener(new ActionListener()
         {
           public void actionPerformed(ActionEvent e)
           {
             if (showButton.getText().equals("Show"))
             {
               showButton.setText("Hide");
               canvas.setHidden(false);
               drawCircle();
             }
             else if (showButton.getText().equals("Hide"))
             {
               showButton.setText("Show");
               canvas.setHidden(true);
               canvas.repaint();
             }
             else
             {
               JOptionPane.showMessageDialog(frame, "'Show' button text was set to some invalid value. Please restart this application", "Error", JOptionPane.OK_OPTION);
               frame.dispose();
               if (colorChooserFrame != null) colorChooserFrame.dispose();
               System.exit(0);
             }
           }
         });
 
         colorButton.addActionListener(new ActionListener()
         {
           public void actionPerformed(ActionEvent e)
           {
             displayColorChooser();
           }
         });
 
 
     //-------------------- Layout Setup --------------------
       //int sliderYMargin = 14;
       //int sliderXMargin = 13;
       int xMargin = 10;
       int yMargin = 10;
       int sliderBarWidth = 7;
 
       frameLayout.putConstraint(SpringLayout.SOUTH, colorButton, -1*yMargin, SpringLayout.SOUTH, contentPane);
       frameLayout.putConstraint(SpringLayout.WEST, colorButton, xMargin, SpringLayout.WEST, contentPane);
 
       frameLayout.putConstraint(SpringLayout.SOUTH, showButton, -1*yMargin, SpringLayout.SOUTH, contentPane);
       frameLayout.putConstraint(SpringLayout.EAST, showButton, -1*xMargin, SpringLayout.EAST, contentPane);
 
       frameLayout.putConstraint(SpringLayout.SOUTH, sizeSlider, -1*yMargin, SpringLayout.SOUTH, contentPane);
       frameLayout.putConstraint(SpringLayout.EAST, sizeSlider, -1*xMargin, SpringLayout.WEST, showButton);
       frameLayout.putConstraint(SpringLayout.WEST, sizeSlider, xMargin, SpringLayout.EAST, colorButton);
 
 
       Component tallestComponent = sizeSlider.getHeight() > showButton.getHeight() ? sizeSlider : showButton;
 
       frameLayout.putConstraint(SpringLayout.SOUTH, bottomSlider, -1*yMargin, SpringLayout.NORTH, tallestComponent);
       frameLayout.putConstraint(SpringLayout.EAST, bottomSlider, -1*xMargin,  SpringLayout.EAST, contentPane);
       frameLayout.putConstraint(SpringLayout.WEST, bottomSlider, 0,  SpringLayout.EAST, leftSlider);
 
 
       frameLayout.putConstraint(SpringLayout.NORTH, leftSlider, yMargin, SpringLayout.NORTH, contentPane);
       frameLayout.putConstraint(SpringLayout.SOUTH, leftSlider, 0, SpringLayout.NORTH, bottomSlider);
       frameLayout.putConstraint(SpringLayout.WEST, leftSlider, xMargin, SpringLayout.WEST, contentPane);
 
       frameLayout.putConstraint(SpringLayout.NORTH, canvasPanel, yMargin+sliderBarWidth, SpringLayout.NORTH, contentPane);
       frameLayout.putConstraint(SpringLayout.SOUTH, canvasPanel, -1*sliderBarWidth, SpringLayout.NORTH, bottomSlider);
       frameLayout.putConstraint(SpringLayout.EAST, canvasPanel, -1*(sliderBarWidth+xMargin), SpringLayout.EAST, contentPane);
       frameLayout.putConstraint(SpringLayout.WEST, canvasPanel, sliderBarWidth, SpringLayout.EAST, leftSlider);
 
 
     //-------------------- Add components to content pane --------------------
       canvasPanel.add(canvas, BorderLayout.CENTER);
       contentPane.add(canvasPanel);
       contentPane.add(leftSlider);
       contentPane.add(bottomSlider);
       contentPane.add(sizeSlider);
       contentPane.add(colorButton);
       contentPane.add(showButton);
       /*
       contentPane.add(rightSlider, BorderLayout.LINE_END);
       contentPane.add(topSlider, BorderLayout.PAGE_START);
       */
   }
 
 
 
 
   public static void main(String[] args)
   {
     try
     {
       UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      //UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
     }
     catch (UnsupportedLookAndFeelException e)
     {
     }
     catch (ClassNotFoundException e)
     {
     }
     catch (InstantiationException e)
     {
     }
     catch (IllegalAccessException e)
     {
     }
     SwingUtilities.invokeLater(new Runnable()
     {
       public void run()
       {
         createAndShowGUI();
       }
     });
   }
 }
