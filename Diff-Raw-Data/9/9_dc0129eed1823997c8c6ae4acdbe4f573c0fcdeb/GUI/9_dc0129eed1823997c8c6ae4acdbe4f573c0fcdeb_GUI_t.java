 import net.miginfocom.swing.MigLayout;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 public class GUI {
     private static JLayeredPane layeredPane;
     private static JScrollPane scrollPane1;
     private static JScrollPane scrollPane2;
     private static JFrame frame;
     private static JLabel label;
     private static JButton startButton;
     private static JButton showButton;
     private static JButton writeButton;
     private static JButton clearButton;
     private static JCheckBox slowCheckBox;
     private static JCheckBox fastCheckBox;
 
     private static JCheckBox housesCheckBox;
     private static JProgressBar progressBar;
     private static BufferedImage bufferedImage;
     private static Graphics2D graphics2D;
     private static ImageIcon imageIcon;
     private static JLabel splitterLabel;
     private static JLabel mainCableLabel;
     private static JLabel secondaryCableLabel;
     private String IMG_PATH = "result.png";
 
     public GUI(String frameName) throws IOException {
         frame = new JFrame(frameName) {{
             setMinimumSize(new Dimension(500, 500));
             setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
             setLayout(new MigLayout("wrap 7, fill"));
             KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
             manager.addKeyEventDispatcher(new MyKeyDispatcher());
         }};
 
         imageIcon = new ImageIcon(IMG_PATH);
 
         layeredPane = new JLayeredPane();
         layeredPane.setOpaque(true);
         layeredPane.setLocation(0, 0);
         layeredPane.setSize(frame.getSize());
         layeredPane.addComponentListener(new componentListener());
 
         startButton = new JButton("Проложить");
         startButton.addActionListener(new StartButtonListener());
 
 
         showButton = new JButton("Просмотр");
         showButton.addActionListener(new ShowButtonListener());
 
         writeButton = new JButton("Сохранить изображение");
         writeButton.addActionListener(new WriteButtonListener());
 
         clearButton = new JButton("Сброс");
         clearButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JMaps.clearHousesList();
                 GUI.get2DGraphicsBufferedImage().setColor(get2DGraphicsBufferedImage().getBackground());
                 GUI.get2DGraphicsBufferedImage().setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                 GUI.get2DGraphicsBufferedImage().fillRect(0,0, bufferedImage.getWidth(), bufferedImage.getHeight());
                 GUI.get2DGraphicsBufferedImage().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                 GUI.setsplitterLabel("            ");
                 GUI.setsecondaryCableLabel("            ");
                 GUI.setmainCableLabel("            ");
                 GUI.refreshControls();
                GUI.setProgressBarValue(0);
             }
         });
 
         progressBar = new JProgressBar();
         progressBar.setSize(100, 30);
         progressBar.setMinimum(0);
         progressBar.setMaximum(100);
         progressBar.setVisible(true);
         progressBar.setStringPainted(true);
 
         housesCheckBox = new JCheckBox("Добавить абонента");
         slowCheckBox = new JCheckBox("optimal");
         slowCheckBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (getStateOfSlowCheckBox())
                     setStateOfFastCheckBox(false);
                 else if (!getStateOfSlowCheckBox())
                     setStateOfFastCheckBox(true);
             }
         });
         fastCheckBox = new JCheckBox("fast");
         fastCheckBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (getStateOfFastCheckBox())
                     setStateOfSlowCheckBox(false);
                 else if (!getStateOfFastCheckBox())
                     setStateOfSlowCheckBox(true);
             }
         });
 
         scrollPane1 = new JScrollPane();
         scrollPane1.setLocation(0, 0);
         scrollPane1.getVerticalScrollBar().setUnitIncrement(60);
         scrollPane1.getHorizontalScrollBar().setUnitIncrement(60);
         scrollPane1.setOpaque(true);
         scrollPane1.setViewportView(new JLabel(imageIcon));
 
         scrollPane2 = new JScrollPane();
         scrollPane2.setLocation(0, 0);
         scrollPane2.getVerticalScrollBar().setUnitIncrement(60);
         scrollPane2.getHorizontalScrollBar().setUnitIncrement(60);
         scrollPane2.getViewport().setOpaque(false);
         bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
         graphics2D = bufferedImage.createGraphics();
 
         label = new JLabel(new ImageIcon(bufferedImage));
         scrollPane2.getViewport().setBackground(new Color(0, 0, 0, 0.0f));
         scrollPane2.setBackground(new Color(0, 0, 0, 0.0f));
         scrollPane2.getViewport().setOpaque(true);
         scrollPane2.setViewportView(label);
         scrollPane2.getViewport().addChangeListener(new scrollStateChanged());
         label.addMouseListener(new mouseListener());
         label.setBackground(new Color(0, 0, 0, 0.0f));
 
         layeredPane.add(scrollPane1, 0);
         layeredPane.add(scrollPane2, 0);
 
         frame.add(startButton, "grow, span 1 2, height ::30");
         frame.add(showButton, "grow, span 1 2, height ::30");
         frame.add(writeButton, "grow, span 1 2, height ::30");
         frame.add(clearButton, "grow, span 1 2, height ::30");
 
         frame.add(fastCheckBox, "grow, height ::15");
         //frame.add(stationCheckBox, "center");
 
         frame.add(housesCheckBox, "center, span 1 2, height ::30");
         frame.add(progressBar, "grow, span 1 2, height ::30");
 
         frame.add(slowCheckBox, "grow, height ::15");
 
         frame.add(layeredPane, "span, h 100%, grow, wrap");
 
         splitterLabel = new JLabel("           ");
         mainCableLabel = new JLabel("                   ");
         secondaryCableLabel = new JLabel("                    ");
 
         frame.add(new JLabel("Сплиттер 1х8   PS-108-А5-9В15-N:"), "right");
         frame.add(splitterLabel, "left");
         frame.add(new JLabel("ОКСНМ-10-01-0.22-8: "), "right");
         frame.add(mainCableLabel, "left");
         frame.add(new JLabel("ДОТС-П-08А: "), "right");
         frame.add(secondaryCableLabel, "left");
 
         frame.pack();
         frame.setVisible(true);
     }
 
     public static void setsplitterLabel(String text) {
         GUI.splitterLabel.setText(text);
     }
 
     public static void setmainCableLabel(String text) {
         GUI.mainCableLabel.setText(text);
     }
 
     public static void setsecondaryCableLabel(String text) {
         GUI.secondaryCableLabel.setText(text);
     }
 
     public static void setProgressBarValue(int value) {
         GUI.progressBar.setValue(value);
     }
 
     public static void updateProgressBar() {
         GUI.progressBar.update(GUI.progressBar.getGraphics());
     }
 
     public static boolean getStateOfHousesCheckBox() {
         return housesCheckBox.isSelected();
     }
 
     public static boolean getStateOfFastCheckBox() {
         return fastCheckBox.isSelected();
     }
 
     public static boolean getStateOfSlowCheckBox() {
         return slowCheckBox.isSelected();
     }
     public static void setStateOfFastCheckBox(boolean state) {
         fastCheckBox.setSelected(state);
     }
 
     public static void setStateOfSlowCheckBox(boolean state) {
         slowCheckBox.setSelected(state);
     }
 
     public static void repaintMainFrame() {
         GUI.frame.repaint();
     }
 
     public static void repaintScrollPane2() {
         scrollPane2.repaint();
     }
 
     public static void repaintScrollPane1() {
         scrollPane1.repaint();
     }
 
     public static void repaintLabel() {
         GUI.label.repaint();
     }
 
     public static Graphics2D get2DGraphicsBufferedImage() {
         return GUI.graphics2D;
     }
 
     public static BufferedImage getImage(int zoomLevel) {
         double zoom = (double) zoomLevel / 100;
         BufferedImage bufferedImage1 = new BufferedImage((int) Math.round(imageIcon.getIconWidth() * zoom), (int) Math.round(imageIcon.getIconHeight() * zoom), BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2 = bufferedImage1.createGraphics();
         g2.drawImage(imageIcon.getImage(), 0, 0, (int) Math.round(imageIcon.getIconWidth() * zoom), (int) Math.round(imageIcon.getIconHeight() * zoom), null);
         g2.drawImage(bufferedImage, 0, 0, (int) Math.round(bufferedImage.getWidth() * zoom), (int) Math.round(bufferedImage.getHeight() * zoom), null);
         g2.dispose();
         return bufferedImage1;
     }
 
     public static void scrollBothPaneH(String str, int delta) {
         if (str == "inc") {
             scrollPane1.getHorizontalScrollBar().setValue(scrollPane1.getHorizontalScrollBar().getValue() + delta);
             scrollPane2.getHorizontalScrollBar().setValue(scrollPane2.getHorizontalScrollBar().getValue() + delta);
         } else if (str == "dec") {
             scrollPane1.getHorizontalScrollBar().setValue(scrollPane1.getHorizontalScrollBar().getValue() - delta);
             scrollPane2.getHorizontalScrollBar().setValue(scrollPane2.getHorizontalScrollBar().getValue() - delta);
         }
     }
 
     public static void scrollBothPaneV(String str, int delta) {
         if (str == "inc") {
             scrollPane1.getVerticalScrollBar().setValue(scrollPane1.getVerticalScrollBar().getValue() - delta);
             scrollPane2.getVerticalScrollBar().setValue(scrollPane2.getVerticalScrollBar().getValue() - delta);
         } else if (str == "dec") {
             scrollPane1.getVerticalScrollBar().setValue(scrollPane1.getVerticalScrollBar().getValue() + delta);
             scrollPane2.getVerticalScrollBar().setValue(scrollPane2.getVerticalScrollBar().getValue() + delta);
         }
     }
 
     public static void setScrollPane1Size(Dimension dimension) {
         scrollPane1.setSize(dimension);
     }
 
     public static void setScrollPane2Size(Dimension dimension) {
         scrollPane2.setSize(dimension);
     }
 
     public static Dimension getLayeredPaneSize() {
         return layeredPane.getSize();
     }
 
     public static void scrollPane1SetValueHScrollBar(Integer value) {
         scrollPane1.getHorizontalScrollBar().setValue(value);
     }
 
     public static void scrollPane2SetValueHScrollBar(Integer value) {
         scrollPane2.getHorizontalScrollBar().setValue(value);
     }
 
     public static void scrollPane1SetValueVScrollBar(Integer value) {
         scrollPane1.getVerticalScrollBar().setValue(value);
     }
 
     public static void scrollPane2SetValueVScrollBar(Integer value) {
         scrollPane2.getVerticalScrollBar().setValue(value);
     }
 
     public static Integer scrollPane1getValueHScrollBar() {
         return scrollPane1.getHorizontalScrollBar().getValue();
     }
 
     public static Integer scrollPane2getValueHScrollBar() {
         return scrollPane2.getHorizontalScrollBar().getValue();
     }
 
     public static Integer scrollPane1getValueVScrollBar() {
         return scrollPane1.getVerticalScrollBar().getValue();
     }
 
     public static Integer scrollPane2getValueVScrollBar() {
         return scrollPane2.getVerticalScrollBar().getValue();
     }
 
     public static Dimension getBufferedImageSize() {
         return new Dimension(GUI.bufferedImage.getWidth(), GUI.bufferedImage.getHeight());
     }
 
     public static void refreshControls() {
         scrollStateChanged.stateChange();
         repaintLabel();
         repaintScrollPane2();
     }
 }
