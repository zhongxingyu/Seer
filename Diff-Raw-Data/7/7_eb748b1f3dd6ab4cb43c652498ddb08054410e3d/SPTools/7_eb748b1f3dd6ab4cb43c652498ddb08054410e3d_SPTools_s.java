 package eu.tpmusielak.securephoto.tools;
 
 import eu.tpmusielak.securephoto.container.SPImage;
 import eu.tpmusielak.securephoto.container.SPImageRoll;
 import eu.tpmusielak.securephoto.verification.VerificationFactorData;
 import eu.tpmusielak.securephoto.verification.Verifier;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.Arrays;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Tomasz P. Musielak
  * Date: 22/05/12
  * Time: 20:46
  */
 public class SPTools implements ActionListener, ChangeListener {
     private final static String APPLICATION_NAME = "SecurePhoto Tools";
 
 
     private enum FileType {
         JPG, SPI, SPR, UNK;
 
         public static FileType getFileType(File file) {
             if (file.getName().endsWith(SPImage.DEFAULT_EXTENSION)) {
                 return SPI;
             } else if (file.getName().endsWith(SPImageRoll.DEFAULT_EXTENSION)) {
                 return SPR;
             } else if (file.getName().toLowerCase().endsWith("jpg")) {
                 return JPG;
             }
             return UNK;
         }
     }
 
     private JButton openButton;
     private JButton exitButton;
     private JPanel mainPanel;
     private JButton prevButton;
     private JButton nextButton;
     private JPanel imagePanel;
     private JSplitPane splitPane;
     private JTextPane textPane;
     private JLabel imageLabel;
     private JSpinner frameNumberSpinner;
     private JLabel frameCountLabel;
     private JButton newSPRButton;
     private JButton addToSPRButton;
     private JButton saveButton;
     private JButton exportAsJPGButton;
     private JButton verifyButton;
 
     private static JMenuBar menuBar;
     private JMenu fileMenu;
     private JMenuItem openMenuItem, saveMenuItem, saveAsMenuItem, exitMenuItem;
 
 
     final JFileChooser openFileChooser = new JFileChooser();
     final JFileChooser saveFileChooser = new JFileChooser();
 
     private File currentFile;
 
     private SPImage currentSPImage;
     private SPImageRoll currentSPRoll;
     private BufferedImage currentImage;
 
     private int frameCount = 0;
     private int frameNumber = -1;
 
     private static JFrame mainFrame;
 
     private TextPaneHandler textPaneHandler;
 
 
     private void createUIComponents() {
         menuBar = new JMenuBar();
 
         fileMenu = new JMenu("File");
         openMenuItem = new JMenuItem("Open");
         saveAsMenuItem = new JMenuItem("Save As...");
         saveMenuItem = new JMenuItem("Save");
         exitMenuItem = new JMenuItem("Exit");
 
         fileMenu.setMnemonic(KeyEvent.VK_F);
         menuBar.add(fileMenu);
 
         openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
         fileMenu.add(openMenuItem);
 
         fileMenu.addSeparator();
 
         saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
         fileMenu.add(saveMenuItem);
 
         fileMenu.add(saveAsMenuItem);
 
         fileMenu.addSeparator();
 
         exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
         fileMenu.add(exitMenuItem);
     }
 
     public SPTools() {
         openButton.addActionListener(SPTools.this);
         exitButton.addActionListener(SPTools.this);
         prevButton.addActionListener(SPTools.this);
         nextButton.addActionListener(SPTools.this);
         newSPRButton.addActionListener(SPTools.this);
         addToSPRButton.addActionListener(SPTools.this);
         saveButton.addActionListener(SPTools.this);
         exportAsJPGButton.addActionListener(SPTools.this);
         verifyButton.addActionListener(SPTools.this);
 
         splitPane.setDividerLocation(0.75d);
         textPane.setEditable(false);
         setNaviButtonsState(false);
         exportAsJPGButton.setEnabled(false);
 
         setFrameNumber(-1);
         setFrameCount(0);
         textPaneHandler = new TextPaneHandler(textPane);
         SpinnerModel model = new SpinnerNumberModel(0, 0, 0, 0);
         frameNumberSpinner.setModel(model);
         frameNumberSpinner.addChangeListener(this);
     }
 
 
     /**
      * Invoked when an action occurs.
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         Object source = e.getSource();
 
         if (source == openButton) {
             openFileAction();
         } else if (source == newSPRButton) {
             saveFileChooser.resetChoosableFileFilters();
             saveFileChooser.addChoosableFileFilter(new SPRFileFilter());
 
             int retVal = saveFileChooser.showSaveDialog(mainPanel);
             if (retVal == JFileChooser.APPROVE_OPTION) {
                 newSPR(saveFileChooser.getSelectedFile());
             }
         } else if (source == addToSPRButton) {
             openFileChooser.resetChoosableFileFilters();
             openFileChooser.addChoosableFileFilter(new JPGFileFilter());
             openFileChooser.addChoosableFileFilter(new SPIFileFilter());
 
             int retVal = openFileChooser.showOpenDialog(mainPanel);
 
             if (retVal == JFileChooser.APPROVE_OPTION) {
                 addImageToSPR(openFileChooser.getSelectedFile());
             }
         } else if (source == exportAsJPGButton) {
             saveFileChooser.resetChoosableFileFilters();
             saveFileChooser.addChoosableFileFilter(new JPGFileFilter());
 
             File outputFile = getFileWithoutExtension(currentFile);
             saveFileChooser.setSelectedFile(outputFile);
 
             int retVal = saveFileChooser.showSaveDialog(mainPanel);
             if (retVal == JFileChooser.APPROVE_OPTION) {
                 exportJPG(saveFileChooser.getSelectedFile());
             }
 
             saveFileChooser.setSelectedFile(null);
         } else if (source == prevButton) {
             prevButtonAction();
         } else if (source == nextButton) {
             nextButtonAction();
         } else if (source == verifyButton) {
             verifyButtonAction();
         } else if (source == exitButton) {
             System.exit(0);
         } else if (source == frameNumberSpinner) {
 
         }
 
 
     }
 
     private void verifyButtonAction() {
         boolean result;
 
         if (currentSPRoll == null) {
             result = currentSPImage.checkIntegrity();
         } else {
             result = currentSPRoll.checkIntegrity();
         }
 
         String message;
         int messageType;
         if (result) {
             message = "Hash verification positive";
             messageType = JOptionPane.INFORMATION_MESSAGE;
         } else {
             message = "Hash verification negative";
             messageType = JOptionPane.WARNING_MESSAGE;
         }
 
         JOptionPane.showMessageDialog(mainFrame, message, "Verification result", messageType);
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
         SpinnerModel model = frameNumberSpinner.getModel();
 
         int number = (Integer) model.getValue();
 
         setFrameNumber(number - 1);
         loadFrame();
         displayImage();
     }
 
 
     private void nextButtonAction() {
         if (currentSPRoll == null)
             return;
 
         frameNumberSpinner.setValue(frameNumberSpinner.getNextValue());
     }
 
     private void prevButtonAction() {
         if (currentSPRoll == null)
             return;
         frameNumberSpinner.setValue(frameNumberSpinner.getPreviousValue());
     }
 
     private void openFileAction() {
         resetFrameCounters();
         openFileChooser.resetChoosableFileFilters();
         openFileChooser.addChoosableFileFilter(new InputFileFilter());
         int retVal = openFileChooser.showOpenDialog(mainPanel);
 
         if (retVal == JFileChooser.APPROVE_OPTION) {
             openFile(openFileChooser.getSelectedFile());
 
             displayImage();
 
             if (currentImage != null)
                 exportAsJPGButton.setEnabled(true);
 
             mainFrame.setTitle(APPLICATION_NAME + " - " + currentFile.getName());
         }
     }
 
 
     private void exportJPG(File file) {
         if (!file.getName().endsWith(".jpg")) {
             file = new File(file.getAbsolutePath() + ".jpg");
         }
 
         try {
            ImageIO.write(currentImage, "JPG", file);
             // TODO: Exception handling
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     private void newSPR(File file) {
         if (!file.getName().endsWith("." + SPImageRoll.DEFAULT_EXTENSION)) {
             file = new File(file.getAbsolutePath() + "." + SPImageRoll.DEFAULT_EXTENSION);
         }
 
         SPImageRoll imageRoll = new SPImageRoll(file);
 
         openFile(file);
     }
 
     private void addImageToSPR(File selectedFile) {
         SPImage spImage = null;
         BufferedImage image = null;
 
         FileType fileType = FileType.getFileType(selectedFile);
 
         switch (fileType) {
             case SPI:
                 try {
                     spImage = SPImage.fromFile(selectedFile);
                     // TODO: Exception handling
                 } catch (IOException e) {
                     return;
                 } catch (ClassNotFoundException e) {
                     return;
                 }
                 break;
             case JPG:
                 image = openJPEGFile(selectedFile);
                 break;
             default:
                 return;
         }
 
         // Should never hapen
         if (currentSPRoll == null)
             return;
 
         // If currentSPImage is null create a new SPImage
         if (spImage == null) {
             //No image to load - nothing to do
             if (image == null)
                 return;
 
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             try {
                 ImageIO.write(image, "jpg", byteArrayOutputStream);
                 byte[] imageBytes = byteArrayOutputStream.toByteArray();
                 byteArrayOutputStream.close();
                 spImage = SPImage.getInstance(imageBytes, null, currentSPRoll.getCurrentHash());
             } catch (IOException e) {
                 e.printStackTrace();
                 return;
             }
         }
 
         // Add image to roll
         currentSPRoll.addImage(spImage);
         openFile(currentFile);
     }
 
     private void openFile(File file) {
         currentFile = file;
 
         currentSPImage = null;
         currentSPRoll = null;
 
         clearImage();
         textPaneHandler.clear();
 
         FileType fileType = FileType.getFileType(file);
 
         switch (fileType) {
             case SPI:
                 currentImage = openSPIFile(file);
                 break;
             case SPR:
                 currentImage = openSPRFile(file);
                 break;
             case JPG:
                 currentImage = openJPEGFile(file);
                 break;
             default:
         }
     }
 
 
     private void setNaviButtonsState(boolean state) {
         addToSPRButton.setEnabled(state);
     }
 
     private BufferedImage openJPEGFile(File file) {
         BufferedImage image = null;
         try {
             image = ImageIO.read(file);
             setNaviButtonsState(false);
             verifyButton.setEnabled(false);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return image;
     }
 
     private BufferedImage openSPIFile(File file) {
         BufferedImage image = null;
         try {
             currentSPImage = SPImage.fromFile(file);
             byte[] imageBytes = currentSPImage.getImageData();
 
             image = ImageIO.read(new ByteArrayInputStream(imageBytes));
             setNaviButtonsState(false);
             printVerifierData(currentSPImage);
             verifyButton.setEnabled(true);
         } catch (IOException e) {
             textPaneHandler.showException(Arrays.toString(e.getStackTrace()));
         } catch (ClassNotFoundException e) {
             textPaneHandler.showException(Arrays.toString(e.getStackTrace()));
         }
         return image;
     }
 
 
     private BufferedImage openSPRFile(File file) {
         BufferedImage image = null;
         try {
             currentSPRoll = SPImageRoll.fromFile(file);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         if (currentSPRoll == null)
             return image;
 
         textPaneHandler.setRollInfo(currentSPRoll.toString());
         addToSPRButton.setEnabled(true);
         verifyButton.setEnabled(true);
 
         int count = currentSPRoll.getFrameCount();
         if (count < 1)
             return image;
 
         int frameCount = currentSPRoll.getFrameCount();
 
         setFrameCount(frameCount);
         setFrameNumber(0);
 
         frameNumberSpinner.setModel(new SpinnerNumberModel(1, 1, frameCount, 1));
 
         try {
             currentSPImage = currentSPRoll.getFrame(frameNumber);
             byte[] imageBytes = currentSPImage.getImageData();
 
             image = ImageIO.read(new ByteArrayInputStream(imageBytes));
             printVerifierData(currentSPImage);
         } catch (IOException e) {
             textPaneHandler.showException(Arrays.toString(e.getStackTrace()));
         }
 
         setNaviButtonsState(true);
 
         return image;
     }
 
     private void loadFrame() {
         try {
             currentSPImage = currentSPRoll.getFrame(frameNumber);
             byte[] imageBytes = currentSPImage.getImageData();
             currentImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
         } catch (IOException e) {
             e.printStackTrace();
         } catch (NullPointerException e) {
             textPaneHandler.showException("Cannot display image");
         }
         printVerifierData(currentSPImage);
     }
 
     private void setFrameCount(int count) {
         frameCount = count;
         frameCountLabel.setText(String.valueOf(frameCount));
     }
 
     private void setFrameNumber(int number) {
         frameNumber = number;
 
         if (frameNumber == (frameCount - 1)) {
             nextButton.setEnabled(false);
         } else {
             nextButton.setEnabled(true);
         }
         if (frameNumber <= 0) {
             prevButton.setEnabled(false);
         } else {
             prevButton.setEnabled(true);
         }
     }
 
     private void resetFrameCounters() {
         setFrameCount(0);
         setFrameNumber(-1);
         frameNumberSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
     }
 
 
     public void printVerifierData(SPImage image) {
         if (image == null)
             return;
 
         StringBuilder sb = new StringBuilder();
 
         Map<Class<Verifier>, VerificationFactorData> verificationFactorData = image.getVerificationFactorData();
         for (VerificationFactorData data : verificationFactorData.values()) {
             sb.append(data.toString());
             sb.append('\n');
         }
         textPaneHandler.setFrameInfo(sb.toString());
     }
 
     private void clearImage() {
         currentImage = null;
         imageLabel.setIcon(null);
         imageLabel.revalidate();
     }
 
     private void displayImage() {
         if (currentImage == null)
             return;
 
         int imgHeight = currentImage.getHeight();
         int imgWidth = currentImage.getWidth();
 
         double maxDimImg = Math.max(imgHeight, imgWidth);
         double maxDimWindow = Math.max(imagePanel.getHeight(), imagePanel.getWidth());
 
         double scalingFactor = maxDimWindow / maxDimImg;
 
         int scaledWidth = (int) Math.ceil(imgWidth * scalingFactor);
         int scaledHeight = (int) Math.ceil(imgHeight * scalingFactor);
 
         Image scaledPicture = currentImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
 
         imageLabel.setIcon(new ImageIcon(scaledPicture));
     }
 
 
     private class InputFileFilter extends FileFilter {
         @Override
         public boolean accept(File f) {
             String name = f.getName();
             int dotIndex = name.lastIndexOf('.');
             String ext = name.substring(dotIndex + 1).toLowerCase();
 
             return ext.equals(SPImage.DEFAULT_EXTENSION)
                     || ext.equals(SPImageRoll.DEFAULT_EXTENSION)
                     || ext.equals("jpg")
                     || f.isDirectory();
         }
 
         @Override
         public String getDescription() {
             return "SPImage, SPImageRoll or JPEG";
         }
 
     }
 
     private class SPIFileFilter extends FileFilter {
         @Override
         public boolean accept(File f) {
             return f.isDirectory() || f.getName().endsWith(SPImage.DEFAULT_EXTENSION);
         }
 
         @Override
         public String getDescription() {
             return "SPImage file (*." + SPImage.DEFAULT_EXTENSION.toLowerCase() + ")";
         }
     }
 
 
     private class SPRFileFilter extends FileFilter {
         @Override
         public boolean accept(File f) {
             return f.isDirectory() || f.getName().endsWith(SPImageRoll.DEFAULT_EXTENSION);
         }
 
         @Override
         public String getDescription() {
             return "SPImageRoll (*." + SPImageRoll.DEFAULT_EXTENSION.toLowerCase() + ")";
         }
     }
 
     private class JPGFileFilter extends FileFilter {
         @Override
         public boolean accept(File f) {
             return f.isDirectory() || f.getName().endsWith("jpg");
         }
 
         @Override
         public String getDescription() {
             return "JPG File (*.jpg)";
         }
     }
 
     private class TextPaneHandler {
         private JTextPane textPane;
         private String rollInfo;
         private String frameInfo;
 
 
         public TextPaneHandler(JTextPane textPane) {
             this.textPane = textPane;
         }
 
         public void setRollInfo(String rollInfo) {
             this.rollInfo = rollInfo;
             update();
         }
 
         public void setFrameInfo(String frameInfo) {
             this.frameInfo = frameInfo;
             update();
         }
 
         public void clear() {
             rollInfo = "";
             frameInfo = "";
             update();
         }
 
         private void update() {
             StringBuilder sb = new StringBuilder();
             sb.append(rollInfo);
             sb.append('\n');
             sb.append(frameInfo);
 
             textPane.setText(sb.toString());
         }
 
         public void showException(String s) {
             textPane.setText(s);
         }
     }
 
     private static File getFileWithoutExtension(File file) {
         String filePath = file.getAbsolutePath();
         int index = filePath.lastIndexOf('.');
         String filePathWithoutName = filePath.substring(0, index);
 
         return new File(filePathWithoutName);
     }
 
 
     public static void main(String[] args) {
         mainFrame = new JFrame(APPLICATION_NAME);
         mainFrame.setContentPane(new SPTools().mainPanel);
         mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         mainFrame.pack();
         mainFrame.setJMenuBar(menuBar);
         mainFrame.setVisible(true);
         mainFrame.setResizable(false);
     }
 }
