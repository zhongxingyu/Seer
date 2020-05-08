 package kohelma.swing;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 public class KohelmaGUI {
     JPanel mainPanel;
     JTabbedPane tabbedPane1;
     JTextField training_textfields_hiddenNeurons;
     JComboBox training_comboboxes_setsList;
     JButton training_buttons_removeSet;
     JButton training_buttons_addSet;
     JTextField training_textfields_trainingReps;
     JTextField training_textfields_errorThreshold;
     JTextField training_textfields_learnRate;
     JButton training_buttons_startStopTraining;
     JButton training_buttons_saveNet;
     JButton recognition_buttons_loadImage;
     JButton recognition_buttons_saveText;
     JTextPane recognition_textpanes_text;
     JPanel helpPanel;
     JTextPane helpTextPane;
     JPanel trainingPanel;
     JPanel recognitionPanel;
     JTextField training_texfields_imageWidth;
     JTextField training_texfields_imageHeight;
     JLabel training_labels_inputNeurons;
     JLabel training_labels_outputNeurons;
     JLabel training_labels_trainingStatus;
     JLabel training_labels_netPath;
     JButton training_buttons_newNet;
     JButton training_buttons_loadNet;
     JLabel recognition_labels_imagePath;
 }
