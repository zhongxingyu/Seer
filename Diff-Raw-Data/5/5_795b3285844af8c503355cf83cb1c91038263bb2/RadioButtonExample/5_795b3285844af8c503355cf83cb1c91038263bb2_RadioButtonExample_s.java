 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 
 public class RadioButtonExample extends JFrame
 {
     private final int WINDOW_WIDTH = 400;
     private final int WINDOW_HEIGHT = 150;
     private JPanel panel;
     private JLabel instruction;
     private JTextField tempInput;
 
     private JRadioButton toCelsius;
     private JRadioButton toFahrenheit;
     private JButton chooser;
     private ButtonGroup buttonGroup;
 //    private JFileChooser fileChooser;
 
     // Menu Variables
     private JMenuBar myMenuBar;
     private JMenu fileMenu;
     private JMenuItem open;
     private JMenuItem newFile;
     private JMenuItem save;
 
     public RadioButtonExample()
     {
         super("Temperature Converter");
 
         setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         buildMenuBar();
 
         buildPanel();
 
         add(panel);
 
        pack();

         setVisible(true);
     }
 
     private void buildMenuBar()
     {
         myMenuBar = new JMenuBar();
 
         buildFileMenu();
 
         myMenuBar.add(fileMenu);
 
         setJMenuBar(myMenuBar);
     }
 
     private void buildFileMenu()
     {
        fileMenu = new JMenu();
         fileMenu.setMnemonic(KeyEvent.VK_F);
 
         newFile = new JMenuItem("New");
         newFile.setMnemonic(KeyEvent.VK_N);
         open = new JMenuItem("Open");
         open.setMnemonic(KeyEvent.VK_O);
         save = new JMenuItem("Save");
         save.setMnemonic(KeyEvent.VK_S);
 
         fileMenu.add(newFile);
         fileMenu.add(open);
         fileMenu.add(save);
     }
 
     private void buildPanel()
     {
         panel = new JPanel();
 
         instruction = new JLabel("Please enter a temperature:");
         tempInput = new JTextField(20);
         toCelsius = new JRadioButton("Celsius");
         toFahrenheit = new JRadioButton("Fahrenheit");
         chooser = new JButton("Choose a color");
 //        fileChooser = new JFileChooser();
 
         buttonGroup = new ButtonGroup();
         buttonGroup.add(toCelsius);
         buttonGroup.add(toFahrenheit);
 
         toCelsius.addActionListener(new TemperatureListener());
         toFahrenheit.addActionListener(new TemperatureListener());
         chooser.addActionListener(new ColorChooserListener());
         toCelsius.setMnemonic(KeyEvent.VK_C);
         toFahrenheit.setMnemonic(KeyEvent.VK_H);
         toCelsius.setToolTipText("Convert temperature to Celsius");
         toFahrenheit.setToolTipText("Convert temperature to Fahrenheit");
         chooser.setMnemonic(KeyEvent.VK_I);
 
         panel.add(instruction);
         panel.add(tempInput);
         panel.add(toCelsius);
         panel.add(toFahrenheit);
         panel.add(chooser);
     }
 
     private class TemperatureListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             String input;
             double result = 0;
 
             input = tempInput.getText();
 
             if (e.getSource() == toCelsius)
             {
                 result = (Double.parseDouble(input) - 32) * 5 / 9;
             }
             else if (e.getSource() == toFahrenheit)
             {
                 result = Double.parseDouble(input) * 1.8 + 32;
             }
 
             String output = "" + result;
             tempInput.setText(output);
 
         }
     }
 
    /* private class ChooserListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             int status = fileChooser.showSaveDialog(null);
             if(status == JFileChooser.APPROVE_OPTION)
             {
                 File f = fileChooser.getSelectedFile();
                 String fileName = f.getPath();
                 JOptionPane.showMessageDialog(null, "You selected " + fileName);
             }
         }
     }
 */
     private class ColorChooserListener implements ActionListener
     {
         public void actionPerformed(ActionEvent actionEvent)
         {
             Color c = JColorChooser.showDialog(null, "Please select a color:", Color.LIGHT_GRAY);
             chooser.setBackground(c);
         }
     }
 
     public static void main(String[] args)
     {
         new RadioButtonExample();
     }
 }
